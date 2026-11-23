package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.room.withTransaction
import com.example.data.local.CategoryDao
import com.example.data.local.EpgDao
import com.example.data.local.ParentalSettingsDao
import com.example.data.local.PlaylistDao
import com.example.data.local.StreamDao
import com.example.data.local.StreamIndexProjection
import com.example.data.local.StreamVaultDatabase
import com.example.data.local.SyncSectionStateDao
import com.example.data.local.VodCatalogGroupDao
import com.example.data.model.CategoryEntity
import com.example.data.model.CategoryWithCount
import com.example.data.model.EpgProgramEntity
import com.example.data.model.GroupedVodStream
import com.example.data.model.ParentalSettingsEntity
import com.example.data.model.PlaylistEntity
import com.example.data.model.StreamEntity
import com.example.data.model.VodCatalogGroupEntity
import com.example.data.model.VodCollectionEntity
import com.example.data.model.VodGroupingNormalizer
import com.example.data.remote.GlobalCollectionsIndexManager
import com.example.data.remote.GlobalPopularityIndexManager
import com.example.data.remote.XtreamAuthResponse
import com.example.data.remote.XtreamClient
import com.example.data.sync.CatalogDiffEngine
import com.example.data.sync.CatalogUpdateCheckWorker
import com.example.data.sync.PlaybackResourceGovernor
import com.example.data.sync.SyncStage
import com.example.data.sync.SyncStatus
import com.example.data.sync.SyncTelemetry
import com.example.data.sync.toSyncTelemetry
import com.example.util.SecurityManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.json.JSONArray
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

data class SyncProgress(
    val isSyncing: Boolean = false,
    val phase: String = "",
    val itemsProcessed: Int = 0,
    val error: String? = null
)

enum class VodPipelinePhase {
    IDLE,
    DOWNLOADING,
    ORGANIZING,
    READY,
    ERROR
}

data class VodPipelineState(
    val phase: VodPipelinePhase = VodPipelinePhase.IDLE,
    val progressPercent: Int = 0,
    val processedCount: Int = 0,
    val totalCount: Int = 0,
    val statusText: String = "",
    val error: String? = null
)

data class EpisodeResumeInfo(
    val positionMs: Long,
    val durationMs: Long = 0L,
    val updatedAtMs: Long = System.currentTimeMillis()
)

class IptvRepository(
    private val playlistDao: PlaylistDao,
    private val categoryDao: CategoryDao,
    private val streamDao: StreamDao,
    private val epgDao: EpgDao,
    private val parentalDao: ParentalSettingsDao,
    private val xtreamClient: XtreamClient = XtreamClient(syncOkHttpClient),
    private val vodGroupDao: VodCatalogGroupDao? = null,
    private val database: StreamVaultDatabase? = null,
    private val appContext: Context? = null,
    private val syncSectionStateDao: SyncSectionStateDao? = database?.syncSectionStateDao()
) {
    private val _syncProgress = MutableStateFlow(SyncProgress())
    val syncProgress = _syncProgress.asStateFlow()

    private val _moviesPipelineState = MutableStateFlow(VodPipelineState())
    val moviesPipelineState: StateFlow<VodPipelineState> = _moviesPipelineState.asStateFlow()

    private val _seriesPipelineState = MutableStateFlow(VodPipelineState())
    val seriesPipelineState: StateFlow<VodPipelineState> = _seriesPipelineState.asStateFlow()

    private val isSyncInProgress = AtomicBoolean(false)
    private val liveSyncLock = AtomicBoolean(false)
    private val moviesSyncLock = AtomicBoolean(false)
    private val seriesSyncLock = AtomicBoolean(false)
    private val indexingScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val activeIndexJobs = ConcurrentHashMap<String, AtomicBoolean>()

    init {
        val stateDao = syncSectionStateDao
        if (stateDao != null) {
            indexingScope.launch(Dispatchers.IO) {
                runCatching {
                    stateDao.markAllRunningAsInterrupted(System.currentTimeMillis())
                }
            }
        }
    }

    fun observeSectionSyncState(playlistId: Long, sectionType: String): Flow<SyncTelemetry> {
        val normalized = SyncTelemetry.normalizeSyncSectionType(sectionType)
        val dao = syncSectionStateDao ?: return flowOf(
            SyncTelemetry(playlistId = playlistId, sectionType = normalized)
        )
        return dao.observeSection(playlistId, normalized).map { entity ->
            entity?.toSyncTelemetry() ?: SyncTelemetry(playlistId = playlistId, sectionType = normalized)
        }
    }

    private suspend fun persistSyncTelemetry(telemetry: SyncTelemetry) {
        val dao = syncSectionStateDao ?: return
        runCatching {
            dao.upsert(telemetry.toEntity())
        }
    }

    suspend fun markSectionInterrupted(playlistId: Long, sectionType: String) {
        val dao = syncSectionStateDao ?: return
        val normalized = SyncTelemetry.normalizeSyncSectionType(sectionType)
        runCatching {
            val existing = dao.getSectionOnce(playlistId, normalized)
            val base = existing?.toSyncTelemetry()
                ?: SyncTelemetry(playlistId = playlistId, sectionType = normalized)
            dao.upsert(
                base.copy(
                    status = SyncStatus.INTERRUPTED,
                    isIndeterminate = false,
                    lastAttemptedSyncMs = System.currentTimeMillis()
                ).toEntity(playlistId, normalized)
            )
        }
    }

    suspend fun markPendingUpdate(playlistId: Long, sectionType: String, summary: String) {
        val dao = syncSectionStateDao ?: return
        val normalized = SyncTelemetry.normalizeSyncSectionType(sectionType)
        runCatching {
            val existing = dao.getSectionOnce(playlistId, normalized)
            val base = existing?.toSyncTelemetry()
                ?: SyncTelemetry(playlistId = playlistId, sectionType = normalized)
            dao.upsert(
                base.copy(
                    pendingUpdateAvailable = summary.isNotBlank(),
                    pendingUpdateSummary = summary
                ).toEntity(playlistId, normalized)
            )
        }
    }

    internal fun notifySectionQueued(sectionType: String, statusText: String) {
        when (SyncTelemetry.normalizeSyncSectionType(sectionType)) {
            "MOVIE" -> {
                val cur = _moviesPipelineState.value
                if (cur.phase != VodPipelinePhase.DOWNLOADING && cur.phase != VodPipelinePhase.ORGANIZING) {
                    _moviesPipelineState.value = VodPipelineState(
                        phase = VodPipelinePhase.IDLE,
                        progressPercent = 0,
                        statusText = statusText
                    )
                }
            }
            "SERIES" -> {
                val cur = _seriesPipelineState.value
                if (cur.phase != VodPipelinePhase.DOWNLOADING && cur.phase != VodPipelinePhase.ORGANIZING) {
                    _seriesPipelineState.value = VodPipelineState(
                        phase = VodPipelinePhase.IDLE,
                        progressPercent = 0,
                        statusText = statusText
                    )
                }
            }
        }
    }

    private fun updateVodPipelineState(streamType: String, state: VodPipelineState) {
        when (streamType.trim().lowercase(Locale.US)) {
            "movie", "movies", "vod" -> _moviesPipelineState.value = state
            "series" -> _seriesPipelineState.value = state
        }
    }

    private suspend fun runInDbTransaction(block: suspend () -> Unit) {
        val db = database
        if (db != null) {
            db.withTransaction { block() }
        } else {
            block()
        }
    }

    private fun PlaylistEntity.decrypted(): PlaylistEntity {
        return copy(password = SecurityManager.decryptCredential(password))
    }

    private fun PlaylistEntity.encryptedForStorage(): PlaylistEntity {
        return copy(
            serverUrl = serverUrl.trim(),
            username = username.trim(),
            password = SecurityManager.encryptCredential(password.trim())
        )
    }

    // Playlists (decrypted transparently in memory, encrypted with AES-GCM at rest in SQLite)
    val allPlaylists: Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists().map { list ->
        list.map { it.decrypted() }
    }
    val activePlaylist: Flow<PlaylistEntity?> = playlistDao.getActivePlaylist().map { it?.decrypted() }

    suspend fun getActivePlaylistOnce(): PlaylistEntity? = playlistDao.getActivePlaylistOnce()?.decrypted()

    suspend fun getPlaylistById(id: Long): PlaylistEntity? = playlistDao.getPlaylistById(id)?.decrypted()

    suspend fun addOrUpdatePlaylist(playlist: PlaylistEntity): Long {
        return playlistDao.insertPlaylist(playlist.encryptedForStorage())
    }

    suspend fun updatePlaylistCredentials(
        id: Long,
        name: String,
        serverUrl: String,
        username: String,
        password: String
    ) {
        val plainPass = SecurityManager.decryptCredential(password.trim())
        val encryptedPass = SecurityManager.encryptCredential(plainPass)
        playlistDao.updatePlaylistCredentials(
            id = id,
            name = name.ifBlank { "Xtream Account" },
            serverUrl = serverUrl.trim(),
            username = username.trim(),
            password = encryptedPass
        )
    }

    suspend fun testAccountConnection(
        serverUrl: String,
        username: String,
        password: String
    ): Result<XtreamAuthResponse> {
        return xtreamClient.authenticate(
            serverUrl = serverUrl.trim(),
            username = username.trim(),
            password = SecurityManager.decryptCredential(password.trim())
        )
    }

    suspend fun setActivePlaylist(id: Long) {
        playlistDao.setActivePlaylist(id)
    }

    suspend fun deletePlaylist(id: Long) {
        vodGroupDao?.deleteGroupsByType(id, "movie")
        vodGroupDao?.deleteGroupsByType(id, "series")
        vodGroupDao?.deleteGroupMembersByType(id, "movie")
        vodGroupDao?.deleteGroupMembersByType(id, "series")
        vodGroupDao?.deleteCollectionsForPlaylist(id)
        streamDao.clearStreamsForPlaylist(id)
        categoryDao.clearCategoriesForPlaylist(id)
        epgDao.clearEpgForPlaylist(id)
        playlistDao.deletePlaylist(id)
        clearEpisodePositionsForPlaylist(id)
    }

    // Categories
    fun getCategories(playlistId: Long, type: String): Flow<List<CategoryEntity>> {
        return categoryDao.getCategories(playlistId, type)
    }

    /**
     * Combines the <2ms indexed category lookup with an index-only GROUP BY count scan.
     * Emits categories immediately via onStart on counts so UI never waits or flashes empty.
     */
    fun searchCategoriesWithCounts(playlistId: Long, type: String, query: String): Flow<List<CategoryWithCount>> {
        val streamType = when (type.uppercase()) {
            "LIVE" -> "live"
            "MOVIE" -> "movie"
            else -> "series"
        }
        val categoriesFlow = categoryDao.searchCategoriesFast(playlistId, type, query.trim())
        val countsFlow = streamDao.getCategoryStreamCounts(playlistId, streamType)
            .onStart { emit(emptyList()) }

        return combine(categoriesFlow, countsFlow) { cats, counts ->
            val countMap = counts.associate { it.categoryId to it.streamCount }
            cats.map { cat ->
                CategoryWithCount(
                    categoryId = cat.categoryId,
                    categoryName = cat.categoryName,
                    type = cat.type,
                    isLocked = cat.isLocked,
                    streamCount = countMap[cat.categoryId] ?: 0
                )
            }
        }
    }

    suspend fun getCategoriesOnce(playlistId: Long, type: String): List<CategoryEntity> {
        return categoryDao.getCategoriesOnce(playlistId, type)
    }

    // Streams & Fast Indexed Queries (with progressive dynamic limit & deterministic ORDER BY id ASC)
    fun queryStreamsFast(
        playlistId: Long,
        streamType: String,
        categoryId: String?,
        query: String,
        limit: Int = DEFAULT_PAGE_SIZE
    ): Flow<List<StreamEntity>> {
        val cleanQuery = query.trim()
        val hasCategory = !categoryId.isNullOrBlank()
        val hasQuery = cleanQuery.isNotEmpty()
        val safeLimit = limit.coerceAtLeast(1)

        return when {
            !hasCategory && !hasQuery -> streamDao.getStreamsInitial(playlistId, streamType, limit = safeLimit)
            hasCategory && !hasQuery -> streamDao.getStreamsByCategoryFast(playlistId, streamType, categoryId!!, limit = safeLimit)
            !hasCategory && hasQuery -> streamDao.searchStreamsByTypeFast(playlistId, streamType, cleanQuery, limit = safeLimit)
            else -> streamDao.searchStreamsInCategoryFast(playlistId, streamType, categoryId!!, cleanQuery, limit = safeLimit)
        }
    }

    // --- Materialized <5ms Movies & Series Catalog, Versions, Discovery Shelves & Stage 2 Indexer ---

    private fun VodCatalogGroupEntity.toGroupedVodStream(): GroupedVodStream {
        return GroupedVodStream(
            stream = StreamEntity(
                id = representativeStreamDbId,
                playlistId = playlistId,
                streamId = representativeStreamId,
                categoryId = primaryCategoryId,
                name = cleanTitle,
                streamType = streamType,
                streamIcon = posterUrl,
                rating = rating,
                containerExtension = containerExtension.ifBlank { "mp4" },
                canonicalGroupKey = groupKey,
                versionTag = defaultVersionTag,
                addedEpoch = addedEpoch,
                ratingNumeric = ratingNumeric,
                releaseYear = releaseYear,
                genres = genres
            ),
            versionCount = versionCount.coerceAtLeast(1)
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun queryGroupsWithFallback(
        playlistId: Long,
        streamType: String,
        primary: (VodCatalogGroupDao) -> Flow<List<VodCatalogGroupEntity>>,
        fallback: () -> Flow<List<GroupedVodStream>>
    ): Flow<List<GroupedVodStream>> {
        val dao = vodGroupDao ?: return fallback()
        return primary(dao).flatMapLatest { groups ->
            if (groups.isNotEmpty()) {
                flowOf(groups.map { it.toGroupedVodStream() })
            } else {
                val hasIndexedCatalog = withContext(Dispatchers.IO) {
                    dao.countGroupsByType(playlistId, streamType) > 0
                }
                if (hasIndexedCatalog) {
                    flowOf(emptyList())
                } else {
                    fallback()
                }
            }
        }
    }

    /**
     * Stage 2 Asynchronous Background Indexer (100% Read-Only on `streams`):
     * Streams lightweight [StreamIndexProjection] rows for [streamType] (`"movie"` or `"series"`)
     * in bounded 10,000-row chunks on [Dispatchers.Default], runs O(N) lossless multi-bridge
     * Union-Find grouping via [CatalogDiffEngine.buildLosslessGroups], and atomically writes
     * materialized rows into BOTH `vod_catalog_groups` and `vod_group_members` while emitting
     * determinate progress into that section's independent [VodPipelineState].
     */
    suspend fun rebuildVodCatalogGroups(
        playlistId: Long,
        streamType: String,
        emitSyncStatus: Boolean = false
    ) {
        val dao = vodGroupDao ?: return
        val normalizedType = streamType.trim().lowercase(Locale.US)
        if (normalizedType != "movie" && normalizedType != "series") return

        val lockKey = "${playlistId}_$normalizedType"
        val lock = activeIndexJobs.computeIfAbsent(lockKey) { AtomicBoolean(false) }
        if (!lock.compareAndSet(false, true)) return

        val sectionLabel = if (normalizedType == "movie") "Movies" else "Series"
        withContext(Dispatchers.Default) {
            var processed = 0
            var totalCount = 0
            try {
                totalCount = streamDao.countStreams(playlistId, normalizedType)
                if (totalCount <= 0) {
                    runInDbTransaction {
                        dao.deleteGroupsByType(playlistId, normalizedType)
                        dao.deleteGroupMembersByType(playlistId, normalizedType)
                        if (normalizedType == "movie") {
                            dao.deleteCollectionsForPlaylist(playlistId)
                        }
                    }
                    updateVodPipelineState(
                        normalizedType,
                        VodPipelineState(
                            phase = VodPipelinePhase.READY,
                            progressPercent = 100,
                            processedCount = 0,
                            totalCount = 0,
                            statusText = "$sectionLabel ready"
                        )
                    )
                    return@withContext
                }

                updateVodPipelineState(
                    normalizedType,
                    VodPipelineState(
                        phase = VodPipelinePhase.ORGANIZING,
                        progressPercent = 5,
                        processedCount = 0,
                        totalCount = totalCount,
                        statusText = "Organizing $sectionLabel (0 / $totalCount)…"
                    )
                )

                val catType = if (normalizedType == "movie") "MOVIE" else "SERIES"
                val categoryNameMap = categoryDao.getCategoriesOnce(playlistId, catType)
                    .associate { it.categoryId to it.categoryName }
                val existingSectionTelemetry = syncSectionStateDao
                    ?.getSectionOnce(playlistId, catType)
                    ?.toSyncTelemetry()
                    ?: SyncTelemetry(playlistId = playlistId, sectionType = catType)
                var latestGroupTelemetry = existingSectionTelemetry.copy(
                    playlistId = playlistId,
                    sectionType = catType,
                    status = SyncStatus.RUNNING,
                    stage = SyncStage.GROUPING_AND_ORGANIZING,
                    isIndeterminate = false,
                    progressPercent = SyncStage.GROUPING_AND_ORGANIZING.startPct,
                    totalDiscovered = maxOf(existingSectionTelemetry.totalDiscovered, totalCount),
                    itemsGrouped = 0,
                    errorMessage = ""
                )
                persistSyncTelemetry(latestGroupTelemetry)

                val allProjections = ArrayList<StreamIndexProjection>(totalCount)
                val chunkSize = 10_000
                var offset = 0

                while (true) {
                    val chunk = streamDao.getStreamIndexProjectionsChunk(
                        playlistId = playlistId,
                        streamType = normalizedType,
                        limit = chunkSize,
                        offset = offset
                    )
                    if (chunk.isEmpty()) break

                    allProjections.addAll(chunk)
                    processed += chunk.size
                    offset += chunk.size

                    val progressPct = ((processed.toLong() * 65L) / totalCount.coerceAtLeast(1).toLong())
                        .toInt()
                        .coerceIn(5, 65)
                    updateVodPipelineState(
                        normalizedType,
                        VodPipelineState(
                            phase = VodPipelinePhase.ORGANIZING,
                            progressPercent = progressPct,
                            processedCount = processed,
                            totalCount = totalCount,
                            statusText = "Organizing $sectionLabel ($processed / $totalCount)…"
                        )
                    )
                    PlaybackResourceGovernor.throttleIfPlaybackActive()
                    if (chunk.size < chunkSize) break
                }

                // Best-effort load of V15/V16 Inverted Cloud Global Popularity & Collections Indexes (fail-silent if offline)
                val ctx = appContext ?: cachedAppContext
                if (ctx != null) {
                    runCatching {
                        GlobalPopularityIndexManager.ensureLoaded(ctx, syncOkHttpClient)
                    }
                    runCatching {
                        GlobalCollectionsIndexManager.ensureLoaded(ctx, syncOkHttpClient)
                    }
                }

                var groupPersistJob: Job? = null
                var lastGroupDbWriteMs = 0L

                val (materializedGroups, materializedMembers, materializedCollections) = CatalogDiffEngine.buildLosslessGroups(
                    playlistId = playlistId,
                    streamType = normalizedType,
                    streams = allProjections,
                    categoryNameMap = categoryNameMap,
                    onProgress = { groupedCount, totalItems ->
                        val mappedPct = SyncTelemetry.computeStageProgress(
                            stage = SyncStage.GROUPING_AND_ORGANIZING,
                            processed = groupedCount,
                            total = totalItems,
                            previousPercent = latestGroupTelemetry.progressPercent
                        )
                        latestGroupTelemetry = latestGroupTelemetry.copy(
                            status = SyncStatus.RUNNING,
                            stage = SyncStage.GROUPING_AND_ORGANIZING,
                            isIndeterminate = false,
                            progressPercent = mappedPct,
                            totalDiscovered = maxOf(latestGroupTelemetry.totalDiscovered, totalItems),
                            itemsGrouped = groupedCount
                        )
                        updateVodPipelineState(
                            normalizedType,
                            VodPipelineState(
                                phase = VodPipelinePhase.ORGANIZING,
                                progressPercent = mappedPct,
                                processedCount = groupedCount,
                                totalCount = totalItems,
                                statusText = "Grouping $sectionLabel ($groupedCount / $totalItems)…"
                            )
                        )
                        val nowMs = System.currentTimeMillis()
                        if (groupedCount >= totalItems || (nowMs - lastGroupDbWriteMs) >= 500L) {
                            lastGroupDbWriteMs = nowMs
                            val snapshot = latestGroupTelemetry
                            val prevJob = groupPersistJob
                            groupPersistJob = indexingScope.launch(Dispatchers.IO) {
                                prevJob?.join()
                                persistSyncTelemetry(snapshot)
                            }
                        }
                    }
                )
                groupPersistJob?.join()

                latestGroupTelemetry = latestGroupTelemetry.copy(
                    stage = SyncStage.PERSISTING_DATABASE,
                    progressPercent = 90,
                    itemsGrouped = materializedGroups.size
                )
                persistSyncTelemetry(latestGroupTelemetry)

                updateVodPipelineState(
                    normalizedType,
                    VodPipelineState(
                        phase = VodPipelinePhase.ORGANIZING,
                        progressPercent = 90,
                        processedCount = processed,
                        totalCount = totalCount,
                        statusText = "Saving ${materializedGroups.size} $sectionLabel titles…"
                    )
                )

                val groupBatchSize = PlaybackResourceGovernor.recommendedDbBatchSize()
                val memberBatchSize = (groupBatchSize * 2).coerceAtMost(2_000)
                runInDbTransaction {
                    dao.deleteGroupsByType(playlistId, normalizedType)
                    dao.deleteGroupMembersByType(playlistId, normalizedType)
                    if (normalizedType == "movie") {
                        dao.deleteCollectionsForPlaylist(playlistId)
                    }
                }
                PlaybackResourceGovernor.throttleIfPlaybackActive()

                for (batch in materializedGroups.chunked(groupBatchSize)) {
                    runInDbTransaction {
                        dao.insertGroupsBatch(batch)
                    }
                    PlaybackResourceGovernor.throttleIfPlaybackActive()
                }
                for (batch in materializedMembers.chunked(memberBatchSize)) {
                    runInDbTransaction {
                        dao.insertGroupMembersBatch(batch)
                    }
                    PlaybackResourceGovernor.throttleIfPlaybackActive()
                }
                if (normalizedType == "movie" && materializedCollections.isNotEmpty()) {
                    for (batch in materializedCollections.chunked(groupBatchSize)) {
                        runInDbTransaction {
                            dao.insertCollectionsBatch(batch)
                        }
                        PlaybackResourceGovernor.throttleIfPlaybackActive()
                    }
                }

                val doneMs = System.currentTimeMillis()
                persistSyncTelemetry(
                    latestGroupTelemetry.copy(
                        status = SyncStatus.COMPLETED,
                        stage = SyncStage.FINALIZING,
                        isIndeterminate = false,
                        progressPercent = 100,
                        itemsGrouped = materializedGroups.size,
                        lastSuccessfulSyncMs = latestGroupTelemetry.lastSuccessfulSyncMs.takeIf { it > 0L } ?: doneMs,
                        errorMessage = ""
                    )
                )

                updateVodPipelineState(
                    normalizedType,
                    VodPipelineState(
                        phase = VodPipelinePhase.READY,
                        progressPercent = 100,
                        processedCount = totalCount,
                        totalCount = totalCount,
                        statusText = "${materializedGroups.size} $sectionLabel ready"
                    )
                )
            } catch (e: Exception) {
                val safeErr = SecurityManager.redactSensitiveText(e.message ?: "Indexing failed")
                Log.e("IptvRepository", "Stage 2 VOD indexing failed for $normalizedType: $safeErr")
                updateVodPipelineState(
                    normalizedType,
                    VodPipelineState(
                        phase = VodPipelinePhase.ERROR,
                        progressPercent = 0,
                        processedCount = processed,
                        totalCount = totalCount,
                        statusText = "Failed to organize $sectionLabel",
                        error = safeErr
                    )
                )
            } finally {
                lock.set(false)
            }
        }
    }

    /**
     * Partial incremental Stage 2 recompute for small Smart Diff deltas (`1..2_000` changed rows).
     * Re-groups only affected canonical keys and atomically updates `vod_catalog_groups` and
     * `vod_group_members` without scanning the entire catalog.
     */
    private suspend fun recomputeAffectedVodGroups(
        playlistId: Long,
        streamType: String,
        dirtyGroupKeys: Set<String>,
        categoryNameMap: Map<String, String>
    ): Pair<Int, Int> {
        val dao = vodGroupDao ?: return 0 to 0
        if (dirtyGroupKeys.isEmpty()) return 0 to 0

        val normalizedType = streamType.trim().lowercase(Locale.US)
        val projectionsById = LinkedHashMap<Long, StreamIndexProjection>()
        val keysToReplace = LinkedHashSet<String>(dirtyGroupKeys)

        for (keyChunk in dirtyGroupKeys.chunked(500)) {
            val rows = streamDao.getStreamsByCanonicalKeys(playlistId, normalizedType, keyChunk)
            for (row in rows) {
                projectionsById[row.id] = row
                if (row.canonicalGroupKey.isNotBlank()) {
                    keysToReplace.add(row.canonicalGroupKey)
                }
            }
        }

        // Also include any surviving member streams previously attached to these group keys
        for (groupKey in dirtyGroupKeys) {
            val existingMembers = dao.getMemberStreamsForGroup(playlistId, normalizedType, groupKey)
            for (member in existingMembers) {
                if (!projectionsById.containsKey(member.id)) {
                    projectionsById[member.id] = StreamIndexProjection(
                        id = member.id,
                        streamId = member.streamId,
                        categoryId = member.categoryId,
                        name = member.name,
                        streamIcon = member.streamIcon,
                        rating = member.rating,
                        ratingNumeric = member.ratingNumeric,
                        containerExtension = member.containerExtension,
                        tmdbId = member.tmdbId,
                        canonicalGroupKey = member.canonicalGroupKey,
                        versionTag = member.versionTag,
                        addedEpoch = member.addedEpoch,
                        releaseYear = member.releaseYear,
                        genres = member.genres
                    )
                }
                if (member.canonicalGroupKey.isNotBlank()) {
                    keysToReplace.add(member.canonicalGroupKey)
                }
            }
        }

        val groupingResult = CatalogDiffEngine.buildLosslessGroups(
            playlistId = playlistId,
            streamType = normalizedType,
            streams = projectionsById.values.toList(),
            categoryNameMap = categoryNameMap
        )
        val newGroups = groupingResult.groups.toMutableList()
        val newMembers = groupingResult.members
        val collectionsToUpsert = LinkedHashMap<String, VodCollectionEntity>()
        for (c in groupingResult.collections) {
            collectionsToUpsert[c.collectionId] = c
        }
        for (g in newGroups) {
            keysToReplace.add(g.groupKey)
        }

        // Preserve Level-2 collection linkage for dirty movie groups belonging to an existing franchise
        if (normalizedType == "movie" && newGroups.isNotEmpty()) {
            val dirtyMatchByCol = HashMap<String, ArrayList<Pair<Int, com.example.data.remote.CollectionMatch>>>()
            for (idx in newGroups.indices) {
                val g = newGroups[idx]
                val repTmdb = projectionsById[g.representativeStreamDbId]?.tmdbId.orEmpty()
                val colMatch = GlobalCollectionsIndexManager.lookupCollection(
                    tmdbId = repTmdb,
                    cleanTitle = g.cleanTitle,
                    releaseYear = g.releaseYear
                )
                if (colMatch != null) {
                    var bucket = dirtyMatchByCol[colMatch.collectionId]
                    if (bucket == null) {
                        bucket = ArrayList(2)
                        dirtyMatchByCol[colMatch.collectionId] = bucket
                    }
                    bucket.add(idx to colMatch)
                }
            }
            for ((colId, dirtyEntries) in dirtyMatchByCol) {
                val existingSiblings = dao.getGroupsByCollection(playlistId, colId).first()
                    .filter { it.groupKey !in keysToReplace }
                val totalDistinctCount = existingSiblings.size + dirtyEntries.size
                if (totalDistinctCount >= 2) {
                    var colName = ""
                    var officialPoster = ""
                    var fallbackPoster = ""
                    var bestScore = Double.NEGATIVE_INFINITY
                    val scores = ArrayList<Double>(totalDistinctCount)

                    for (sib in existingSiblings) {
                        if (colName.isEmpty() && sib.collectionName.isNotBlank()) colName = sib.collectionName
                        if (sib.popularityScore > bestScore && sib.posterUrl.isNotBlank()) {
                            bestScore = sib.popularityScore
                            fallbackPoster = sib.posterUrl
                        }
                        scores.add(sib.popularityScore)
                    }
                    for ((idx, match) in dirtyEntries) {
                        val updatedGroup = newGroups[idx].copy(
                            collectionId = match.collectionId,
                            collectionName = match.collectionName,
                            collectionOrder = match.releaseOrder
                        )
                        newGroups[idx] = updatedGroup
                        if (colName.isEmpty() && match.collectionName.isNotBlank()) colName = match.collectionName
                        if (officialPoster.isEmpty() && match.posterUrl.isNotBlank()) officialPoster = match.posterUrl
                        if (updatedGroup.popularityScore > bestScore && updatedGroup.posterUrl.isNotBlank()) {
                            bestScore = updatedGroup.popularityScore
                            fallbackPoster = updatedGroup.posterUrl
                        }
                        scores.add(updatedGroup.popularityScore)
                    }
                    if (colName.isNotEmpty()) {
                        scores.sortDescending()
                        val topSlice = scores.size.coerceAtMost(3)
                        var topSum = 0.0
                        for (k in 0 until topSlice) topSum += scores[k]
                        val avgTop = if (topSlice > 0) topSum / topSlice else 0.0
                        val colScore = avgTop + kotlin.math.ln(totalDistinctCount.toDouble() + 1.0) * 2.5
                        collectionsToUpsert[colId] = VodCollectionEntity(
                            playlistId = playlistId,
                            collectionId = colId,
                            name = colName,
                            posterUrl = officialPoster.ifBlank { fallbackPoster },
                            movieCount = totalDistinctCount,
                            popularityScore = colScore
                        )
                    }
                }
            }
        }

        runInDbTransaction {
            for (keyChunk in keysToReplace.chunked(500)) {
                dao.deleteGroupsByKeys(playlistId, normalizedType, keyChunk)
                dao.deleteGroupMembersByKeys(playlistId, normalizedType, keyChunk)
            }
            for (batch in newGroups.chunked(2_000)) {
                dao.insertGroupsBatch(batch)
            }
            for (batch in newMembers.chunked(4_000)) {
                dao.insertGroupMembersBatch(batch)
            }
            if (normalizedType == "movie" && collectionsToUpsert.isNotEmpty()) {
                dao.insertCollectionsBatch(collectionsToUpsert.values.toList())
            }
        }
        return newGroups.size to newMembers.size
    }

    /**
     * Automatically builds `vod_catalog_groups` in the background for upgraded databases where
     * raw [StreamEntity] rows exist but materialized group rows have not yet been populated.
     */
    suspend fun ensureVodCatalogIndexed(playlistId: Long, streamType: String) {
        val dao = vodGroupDao ?: return
        val normalizedType = streamType.trim().lowercase(Locale.US)
        if (normalizedType != "movie" && normalizedType != "series") return

        val (groupCount, rawCount) = withContext(Dispatchers.IO) {
            dao.countGroupsByType(playlistId, normalizedType) to streamDao.countStreams(playlistId, normalizedType)
        }
        if (groupCount == 0 && rawCount > 0) {
            rebuildVodCatalogGroups(playlistId, normalizedType, emitSyncStatus = false)
        }
    }

    private fun buildFtsPrefixQuery(rawQuery: String): String? {
        if (rawQuery.isBlank()) return null
        var asciiAlnumCount = 0
        for (ch in rawQuery) {
            if (ch.code > 127 && ch.isLetter()) {
                return null
            }
            if ((ch in 'a'..'z') || (ch in 'A'..'Z') || (ch in '0'..'9')) {
                asciiAlnumCount++
            }
        }
        if (asciiAlnumCount < 3) return null

        val tokens = ArrayList<String>()
        val currentToken = StringBuilder()
        for (ch in rawQuery) {
            if ((ch in 'a'..'z') || (ch in 'A'..'Z') || (ch in '0'..'9')) {
                currentToken.append(ch.lowercaseChar())
            } else if (currentToken.isNotEmpty()) {
                tokens.add(currentToken.toString())
                currentToken.setLength(0)
            }
        }
        if (currentToken.isNotEmpty()) {
            tokens.add(currentToken.toString())
        }
        if (tokens.isEmpty()) return null
        return tokens.joinToString(" ") { "$it*" }
    }

    fun queryDeduplicatedVodStreams(
        playlistId: Long,
        streamType: String,
        categoryId: String?,
        query: String,
        limit: Int = DEFAULT_PAGE_SIZE
    ): Flow<List<GroupedVodStream>> {
        val cleanQuery = query.trim()
        val hasCategory = !categoryId.isNullOrBlank()
        val hasQuery = cleanQuery.isNotEmpty()
        val safeLimit = limit.coerceAtLeast(1)
        val ftsQuery = if (hasQuery) buildFtsPrefixQuery(cleanQuery) else null

        return queryGroupsWithFallback(
            playlistId = playlistId,
            streamType = streamType,
            primary = { dao ->
                when {
                    hasQuery && ftsQuery != null -> dao.searchGroupsFts(
                        playlistId = playlistId,
                        streamType = streamType,
                        categoryId = categoryId?.takeIf { it.isNotBlank() },
                        ftsQuery = ftsQuery,
                        limit = safeLimit
                    )
                    hasQuery -> dao.searchGroups(
                        playlistId = playlistId,
                        streamType = streamType,
                        categoryId = categoryId?.takeIf { it.isNotBlank() },
                        query = cleanQuery,
                        limit = safeLimit
                    )
                    hasCategory -> dao.getGroupsByCategory(
                        playlistId = playlistId,
                        streamType = streamType,
                        categoryId = categoryId!!,
                        limit = safeLimit
                    )
                    else -> dao.getAllGroupsPaged(
                        playlistId = playlistId,
                        streamType = streamType,
                        limit = safeLimit
                    )
                }
            },
            fallback = {
                when {
                    hasQuery -> streamDao.searchDeduplicatedStreams(
                        playlistId = playlistId,
                        streamType = streamType,
                        categoryId = categoryId?.takeIf { it.isNotBlank() },
                        query = cleanQuery,
                        limit = safeLimit
                    )
                    hasCategory -> streamDao.getDeduplicatedStreamsByCategory(
                        playlistId = playlistId,
                        streamType = streamType,
                        categoryId = categoryId!!,
                        limit = safeLimit
                    )
                    else -> streamDao.getDeduplicatedStreams(
                        playlistId = playlistId,
                        streamType = streamType,
                        limit = safeLimit
                    )
                }
            }
        )
    }

    suspend fun getStreamByDbId(id: Long): StreamEntity? {
        return streamDao.getStreamByDbId(id)
    }

    suspend fun getVodVersionsForGroup(
        playlistId: Long,
        streamType: String,
        canonicalGroupKey: String,
        fallbackStream: StreamEntity
    ): List<StreamEntity> = withContext(Dispatchers.IO) {
        val catType = if (streamType.equals("movie", ignoreCase = true)) "MOVIE" else "SERIES"
        val categoryNameMap = categoryDao.getCategoriesOnce(playlistId, catType)
            .associate { it.categoryId to it.categoryName }

        fun enrichVersionTag(entity: StreamEntity): StreamEntity {
            val catName = categoryNameMap[entity.categoryId].orEmpty()
            val computed = VodGroupingNormalizer.extractVersionTagWithCategory(entity.name, catName)
            val resolvedTag = computed.ifBlank { entity.versionTag }
            return if (resolvedTag != entity.versionTag) entity.copy(versionTag = resolvedTag) else entity
        }

        if (canonicalGroupKey.isBlank()) {
            val dbRep = fallbackStream.id.takeIf { it > 0L }?.let { streamDao.getStreamByDbId(it) }
            return@withContext listOf(enrichVersionTag(dbRep ?: fallbackStream))
        }

        // Lossless Phase 5B lookup: resolve exact Union-Find members via vod_group_members FIRST
        var versions = vodGroupDao?.getMemberStreamsForGroup(playlistId, streamType, canonicalGroupKey).orEmpty()

        if (versions.isEmpty()) {
            versions = streamDao.getVersionsByGroupKey(playlistId, streamType, canonicalGroupKey)
        }

        // Legacy fallback for un-rewritten databases where streams.canonicalGroupKey may be blank:
        // match by tmdbId, clean title, or representativeStreamDbId from vod_catalog_groups.
        if (versions.isEmpty()) {
            val groupRow = vodGroupDao?.getGroupByKey(playlistId, streamType, canonicalGroupKey)
            val tmdbPrefix = "tmdb_${streamType.lowercase(Locale.US)}_"
            val tmdbFromKey = if (canonicalGroupKey.startsWith(tmdbPrefix)) {
                canonicalGroupKey.removePrefix(tmdbPrefix).trim()
            } else {
                ""
            }
            val effectiveTmdbId = fallbackStream.tmdbId.trim().ifBlank { tmdbFromKey }

            if (effectiveTmdbId.isNotBlank()) {
                versions = streamDao.getVersionsByTmdbId(playlistId, streamType, effectiveTmdbId)
            }

            if (versions.isEmpty()) {
                val targetCleanTitle = groupRow?.cleanTitle?.trim()?.ifBlank { null }
                    ?: VodGroupingNormalizer.extractCleanDisplayTitle(fallbackStream.name)
                if (targetCleanTitle.length >= 2) {
                    val candidates = streamDao.getVersionsByTitleLike(playlistId, streamType, targetCleanTitle)
                    val exactKeyMatches = ArrayList<StreamEntity>()
                    val cleanTitleMatches = ArrayList<StreamEntity>()
                    for (candidate in candidates) {
                        val candidateYear = if (candidate.releaseYear > 0) {
                            candidate.releaseYear
                        } else {
                            VodGroupingNormalizer.extractReleaseYear(candidate.name, "")
                        }
                        val computedKey = candidate.canonicalGroupKey.ifBlank {
                            VodGroupingNormalizer.computeCanonicalGroupKey(
                                streamType = streamType,
                                rawName = candidate.name,
                                tmdbId = candidate.tmdbId,
                                releaseYear = candidateYear,
                                streamId = candidate.streamId,
                                posterUrl = candidate.streamIcon
                            )
                        }
                        if (computedKey == canonicalGroupKey) {
                            exactKeyMatches.add(candidate)
                        } else {
                            val candidateClean = VodGroupingNormalizer.extractCleanDisplayTitle(candidate.name)
                            val yearCompatible = fallbackStream.releaseYear == 0 ||
                                candidateYear == 0 ||
                                candidateYear == fallbackStream.releaseYear
                            if (candidateClean.equals(targetCleanTitle, ignoreCase = true) && yearCompatible) {
                                cleanTitleMatches.add(candidate)
                            }
                        }
                    }
                    versions = if (exactKeyMatches.isNotEmpty()) exactKeyMatches else cleanTitleMatches
                }
            }

            if (versions.isEmpty()) {
                val repId = groupRow?.representativeStreamDbId ?: fallbackStream.id
                val repRow = if (repId > 0L) streamDao.getStreamByDbId(repId) else null
                if (repRow != null) {
                    versions = listOf(repRow)
                }
            }
        }

        if (versions.isEmpty()) {
            return@withContext listOf(enrichVersionTag(fallbackStream))
        }
        versions
            .map { enrichVersionTag(it) }
            .sortedWith(
                compareByDescending<StreamEntity> {
                    VodGroupingNormalizer.scoreVersionPreference(
                        versionTag = it.versionTag,
                        hasPoster = it.streamIcon.isNotBlank(),
                        containerExt = it.containerExtension,
                        ratingNumeric = it.ratingNumeric
                    )
                }.thenBy { it.id }
            )
    }

    fun getRecentlyAddedVod(
        playlistId: Long,
        streamType: String,
        limit: Int = 20
    ): Flow<List<GroupedVodStream>> {
        val safeLimit = limit.coerceAtLeast(1)
        return queryGroupsWithFallback(
            playlistId = playlistId,
            streamType = streamType,
            primary = { dao ->
                dao.getRecentlyAddedGroups(
                    playlistId = playlistId,
                    streamType = streamType,
                    limit = safeLimit
                )
            },
            fallback = {
                streamDao.getRecentlyAddedDeduplicated(
                    playlistId = playlistId,
                    streamType = streamType,
                    limit = safeLimit
                )
            }
        )
    }

    fun getTopRatedVod(
        playlistId: Long,
        streamType: String,
        limit: Int = 20
    ): Flow<List<GroupedVodStream>> {
        val safeLimit = limit.coerceAtLeast(1)
        return queryGroupsWithFallback(
            playlistId = playlistId,
            streamType = streamType,
            primary = { dao ->
                dao.getTopPopularGroups(
                    playlistId = playlistId,
                    streamType = streamType,
                    limit = safeLimit
                )
            },
            fallback = {
                streamDao.getTopRatedDeduplicated(
                    playlistId = playlistId,
                    streamType = streamType,
                    limit = safeLimit
                )
            }
        )
    }

    fun getGenreShelfVod(
        playlistId: Long,
        streamType: String,
        genreKeyword: String,
        limit: Int = 20
    ): Flow<List<GroupedVodStream>> {
        val cleanGenre = genreKeyword.trim()
        if (cleanGenre.isEmpty()) return flowOf(emptyList())
        val safeLimit = limit.coerceAtLeast(1)
        return queryGroupsWithFallback(
            playlistId = playlistId,
            streamType = streamType,
            primary = { dao ->
                dao.getGenreShelfGroups(
                    playlistId = playlistId,
                    streamType = streamType,
                    genreKeyword = cleanGenre,
                    limit = safeLimit
                )
            },
            fallback = {
                streamDao.getGenreShelfDeduplicated(
                    playlistId = playlistId,
                    streamType = streamType,
                    genreKeyword = cleanGenre,
                    limit = safeLimit
                )
            }
        )
    }

    fun getPlatformShelfVod(
        playlistId: Long,
        streamType: String,
        platformKeyword: String,
        limit: Int = 24
    ): Flow<List<GroupedVodStream>> {
        val rawKeyword = platformKeyword.trim()
        if (rawKeyword.isEmpty()) return flowOf(emptyList())
        val sqlKeyword = when (rawKeyword.lowercase(Locale.US)) {
            "prime video" -> "Prime"
            "apple tv+" -> "Apple"
            "disney+" -> "Disney"
            else -> rawKeyword
        }
        val safeLimit = limit.coerceAtLeast(1)
        return queryGroupsWithFallback(
            playlistId = playlistId,
            streamType = streamType,
            primary = { dao ->
                dao.getPlatformShelfGroups(
                    playlistId = playlistId,
                    streamType = streamType,
                    platformKeyword = sqlKeyword,
                    limit = safeLimit
                )
            },
            fallback = {
                streamDao.getGenreShelfDeduplicated(
                    playlistId = playlistId,
                    streamType = streamType,
                    genreKeyword = sqlKeyword,
                    limit = safeLimit
                )
            }
        )
    }

    fun observeMovieCollections(
        playlistId: Long,
        limit: Int = 40
    ): Flow<List<VodCollectionEntity>> {
        val dao = vodGroupDao ?: return flowOf(emptyList())
        return dao.observeCollections(playlistId, limit.coerceAtLeast(1))
    }

    fun getCollectionMovies(
        playlistId: Long,
        collectionId: String
    ): Flow<List<GroupedVodStream>> {
        val cleanCollectionId = collectionId.trim()
        if (cleanCollectionId.isEmpty()) return flowOf(emptyList())
        val dao = vodGroupDao ?: return flowOf(emptyList())
        return dao.getGroupsByCollection(playlistId, cleanCollectionId).map { groups ->
            groups.map { it.toGroupedVodStream() }
        }
    }

    fun getCollectionMovieGroups(
        playlistId: Long,
        collectionId: String
    ): Flow<List<VodCatalogGroupEntity>> {
        val cleanCollectionId = collectionId.trim()
        if (cleanCollectionId.isEmpty()) return flowOf(emptyList())
        val dao = vodGroupDao ?: return flowOf(emptyList())
        return dao.getGroupsByCollection(playlistId, cleanCollectionId)
    }

    suspend fun getSimilarVodTitles(
        playlistId: Long,
        streamType: String,
        genreKeyword: String,
        excludeGroupKey: String,
        limit: Int = 15
    ): List<GroupedVodStream> = withContext(Dispatchers.IO) {
        val cleanGenre = genreKeyword.trim()
        if (cleanGenre.isEmpty()) return@withContext emptyList()
        val safeLimit = limit.coerceAtLeast(1)
        val dao = vodGroupDao
        if (dao != null && dao.countGroupsByType(playlistId, streamType) > 0) {
            return@withContext dao.getSimilarGroupsByGenre(
                playlistId = playlistId,
                streamType = streamType,
                genreKeyword = cleanGenre,
                excludeGroupKey = excludeGroupKey,
                limit = safeLimit
            ).map { it.toGroupedVodStream() }
        }
        streamDao.getSimilarTitlesDeduplicated(
            playlistId = playlistId,
            streamType = streamType,
            genreKeyword = cleanGenre,
            excludeGroupKey = excludeGroupKey,
            limit = safeLimit
        ).first()
    }

    fun getCategoryStreamCounts(playlistId: Long, streamType: String): Flow<Map<String, Int>> {
        return streamDao.getCategoryStreamCounts(playlistId, streamType).map { tuples ->
            tuples.associate { it.categoryId to it.streamCount }
        }
    }

    suspend fun countStreamsByType(playlistId: Long, streamType: String): Int = withContext(Dispatchers.IO) {
        streamDao.countStreams(playlistId, streamType)
    }

    /**
     * Extracts canonical recommendation genres (`Action`, `Crime`, `Drama`, `Thriller`,
     * `Sci-Fi`, `Comedy`, `Documentary`) from a stream's raw genre metadata string.
     */
    private fun extractNormalizedRecommendationGenres(rawGenres: String): List<String> {
        if (rawGenres.isBlank()) return emptyList()
        val found = LinkedHashSet<String>()
        val tokens = rawGenres.split(Regex("[,/|•&;]+"))
        for (token in tokens) {
            val lower = token.trim().lowercase(Locale.US)
            if (lower.isEmpty()) continue
            if (lower.contains("action")) found.add("Action")
            if (lower.contains("crime")) found.add("Crime")
            if (lower.contains("drama")) found.add("Drama")
            if (lower.contains("thriller")) found.add("Thriller")
            if (lower.contains("sci-fi") || lower.contains("scifi") || lower.contains("sci fi") || lower.contains("science fiction")) {
                found.add("Sci-Fi")
            }
            if (lower.contains("comedy") || lower.contains("comed")) found.add("Comedy")
            if (lower.contains("documentary") || lower.contains("docu")) found.add("Documentary")
        }
        return found.toList()
    }

    /**
     * Generates personalized VOD/Series recommendations strictly from the normalized genres
     * (`Action`, `Crime`, `Drama`, `Thriller`, `Sci-Fi`, `Comedy`, `Documentary`) of the user's
     * real watch history and favorites of the same [streamType]. Never matches raw broad
     * `categoryId`s and never fabricates items when no genre signals exist.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getRecommendedVodForUser(
        playlistId: Long,
        streamType: String,
        limit: Int = 20
    ): Flow<List<GroupedVodStream>> {
        val safeLimit = limit.coerceAtLeast(1)
        val watchedFlow = streamDao.getRecentlyWatched(playlistId)
            .map { list -> list.filter { it.streamType == streamType } }
        val favoritesFlow = streamDao.getFavoriteStreamsByType(playlistId, streamType)

        return combine(watchedFlow, favoritesFlow) { watched, favorites ->
            watched + favorites
        }.flatMapLatest { signals ->
            if (signals.isEmpty()) {
                flowOf(emptyList())
            } else {
                val normalizedGenres = signals
                    .flatMap { stream -> extractNormalizedRecommendationGenres(stream.genres) }
                    .groupingBy { it }
                    .eachCount()
                    .entries
                    .sortedByDescending { it.value }
                    .take(4)
                    .map { it.key }

                if (normalizedGenres.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val dao = vodGroupDao
                    val useIndexed = dao != null && withContext(Dispatchers.IO) {
                        dao.countGroupsByType(playlistId, streamType) > 0
                    }
                    if (useIndexed) {
                        val indexedDao = dao!!
                        val excludeIds = signals.map { it.streamId }.distinct()
                        val excludeIdSet = excludeIds.toHashSet()
                        val excludeGroupKeys = signals
                            .mapNotNull { it.canonicalGroupKey.takeIf(String::isNotBlank) }
                            .toHashSet()

                        val byGenre = withContext(Dispatchers.IO) {
                            indexedDao.getRecommendedGroupsByGenre(
                                playlistId = playlistId,
                                streamType = streamType,
                                genreKeywords = normalizedGenres,
                                excludeStreamIds = excludeIds,
                                limit = safeLimit
                            )
                        }
                        val filtered = byGenre
                            .filter { g ->
                                g.groupKey !in excludeGroupKeys &&
                                    g.representativeStreamId !in excludeIdSet
                            }
                            .take(safeLimit)
                            .map { it.toGroupedVodStream() }
                        flowOf(filtered)
                    } else {
                        streamDao.getRecommendedByCategoriesOrGenres(
                            playlistId = playlistId,
                            streamType = streamType,
                            categoryIds = listOf("__NO_CATEGORY_SIGNAL__"),
                            genreKeyword = normalizedGenres.first(),
                            limit = safeLimit
                        )
                    }
                }
            }
        }
    }

    fun searchLiveChannels(
        playlistId: Long,
        query: String,
        categoryId: String?,
        limit: Int = DEFAULT_PAGE_SIZE
    ): Flow<List<StreamEntity>> {
        return queryStreamsFast(playlistId, "live", categoryId, query, limit = limit)
    }

    fun searchAllStreams(playlistId: Long, query: String, streamType: String?): Flow<List<StreamEntity>> {
        if (streamType != null) {
            return queryStreamsFast(playlistId, streamType, null, query)
        }
        return streamDao.searchAllStreams(playlistId, query.trim(), null)
    }

    fun getStreamsByCategory(
        playlistId: Long,
        type: String,
        categoryId: String,
        limit: Int = DEFAULT_PAGE_SIZE
    ): Flow<List<StreamEntity>> {
        return streamDao.getStreamsByCategoryFast(playlistId, type, categoryId, limit = limit.coerceAtLeast(1))
    }

    suspend fun getStreamsByCategoryOnce(playlistId: Long, type: String, categoryId: String): List<StreamEntity> {
        return streamDao.getStreamsByCategoryOnce(playlistId, type, categoryId)
    }

    fun getFavoriteStreams(playlistId: Long): Flow<List<StreamEntity>> {
        return streamDao.getFavoriteStreams(playlistId)
    }

    fun getRecentlyWatched(playlistId: Long): Flow<List<StreamEntity>> {
        return streamDao.getRecentlyWatched(playlistId)
    }

    suspend fun toggleFavorite(streamId: Long, isFavorite: Boolean) {
        streamDao.toggleFavorite(streamId, isFavorite)
    }

    suspend fun updateWatchHistory(streamId: Long) {
        streamDao.updateWatchHistory(streamId, System.currentTimeMillis())
    }

    suspend fun updatePlaybackPosition(streamId: Long, positionMs: Long) {
        streamDao.updatePlaybackPosition(streamId, positionMs, System.currentTimeMillis())
    }

    // Per-Episode Playback Resume Tracking keyed by "${playlistId}_ep_${episodeId}"
    val episodeResumeMap: StateFlow<Map<String, EpisodeResumeInfo>> = _episodeResumeStateFlow.asStateFlow()

    fun buildEpisodeResumeKey(playlistId: Long, episodeId: Long): String {
        return "${playlistId}_ep_${episodeId}"
    }

    fun getEpisodeResumeInfo(playlistId: Long, episodeId: Long): EpisodeResumeInfo? {
        val key = buildEpisodeResumeKey(playlistId, episodeId)
        episodeResumeCache[key]?.let { return it }
        val prefs = episodePrefs ?: return null
        val savedPos = prefs.getLong("${key}_pos", 0L)
        if (savedPos <= 0L) return null
        val savedDur = prefs.getLong("${key}_dur", 0L)
        val savedAt = prefs.getLong("${key}_ts", 0L)
        val info = EpisodeResumeInfo(positionMs = savedPos, durationMs = savedDur, updatedAtMs = savedAt)
        episodeResumeCache[key] = info
        _episodeResumeStateFlow.value = episodeResumeCache.toMap()
        return info
    }

    fun getEpisodePositionMs(playlistId: Long, episodeId: Long): Long {
        return getEpisodeResumeInfo(playlistId, episodeId)?.positionMs ?: 0L
    }

    fun getEpisodeDurationMs(playlistId: Long, episodeId: Long): Long {
        return getEpisodeResumeInfo(playlistId, episodeId)?.durationMs ?: 0L
    }

    fun saveEpisodePositionMs(
        playlistId: Long,
        episodeId: Long,
        positionMs: Long,
        durationMs: Long = 0L
    ) {
        val key = buildEpisodeResumeKey(playlistId, episodeId)
        val clampedPos = positionMs.coerceAtLeast(0L)
        val existingDur = episodeResumeCache[key]?.durationMs ?: episodePrefs?.getLong("${key}_dur", 0L) ?: 0L
        val effectiveDur = if (durationMs > 0L) durationMs else existingDur
        val now = System.currentTimeMillis()
        val info = EpisodeResumeInfo(
            positionMs = clampedPos,
            durationMs = effectiveDur,
            updatedAtMs = now
        )
        episodeResumeCache[key] = info
        _episodeResumeStateFlow.value = episodeResumeCache.toMap()
        episodePrefs?.edit()
            ?.putLong("${key}_pos", clampedPos)
            ?.putLong("${key}_dur", effectiveDur)
            ?.putLong("${key}_ts", now)
            ?.apply()
    }

    fun clearEpisodePositionsForPlaylist(playlistId: Long) {
        val prefix = "${playlistId}_ep_"
        val keysToRemove = episodeResumeCache.keys.filter { it.startsWith(prefix) }
        keysToRemove.forEach { episodeResumeCache.remove(it) }
        _episodeResumeStateFlow.value = episodeResumeCache.toMap()
        episodePrefs?.let { prefs ->
            val editor = prefs.edit()
            prefs.all.keys.filter { it.startsWith(prefix) }.forEach { editor.remove(it) }
            editor.apply()
        }
    }

    suspend fun getSiblingLiveChannels(playlistId: Long, categoryId: String): List<StreamEntity> {
        return streamDao.getSiblingLiveChannels(playlistId, categoryId)
    }

    suspend fun getStreamById(id: Long): StreamEntity? {
        return streamDao.getStreamById(id)
    }

    // EPG
    fun getUpcomingEpg(playlistId: Long, streamId: Long): Flow<List<EpgProgramEntity>> {
        val now = System.currentTimeMillis() / 1000
        return epgDao.getUpcomingPrograms(playlistId, streamId, now)
    }

    suspend fun getCurrentEpg(playlistId: Long, streamId: Long): EpgProgramEntity? {
        val now = System.currentTimeMillis() / 1000
        return epgDao.getCurrentProgram(playlistId, streamId, now)
    }

    suspend fun getCatchupEpg(playlistId: Long, streamId: Long): List<EpgProgramEntity> {
        val now = System.currentTimeMillis() / 1000
        return epgDao.getCatchupPrograms(playlistId, streamId, now)
    }

    suspend fun refreshEpgForStream(playlist: PlaylistEntity, streamId: Long) {
        val epgList = xtreamClient.fetchShortEpg(
            serverUrl = playlist.serverUrl,
            username = playlist.username,
            password = SecurityManager.decryptCredential(playlist.password),
            streamId = streamId,
            playlistId = playlist.id
        )
        if (epgList.isNotEmpty()) {
            epgDao.insertEpgPrograms(epgList)
        }
    }

    suspend fun fetchSeriesDetails(
        playlist: PlaylistEntity,
        seriesStream: StreamEntity
    ): com.example.data.remote.SeriesDetails {
        return xtreamClient.fetchSeriesDetails(
            serverUrl = playlist.serverUrl,
            username = playlist.username,
            password = SecurityManager.decryptCredential(playlist.password),
            seriesId = seriesStream.streamId,
            fallbackTitle = seriesStream.name,
            fallbackCover = seriesStream.streamIcon,
            fallbackRating = seriesStream.rating
        )
    }

    suspend fun fetchVodMovieDetails(
        playlist: PlaylistEntity?,
        movieStream: StreamEntity
    ): com.example.data.remote.VodMovieDetails = withContext(Dispatchers.IO) {
        if (playlist == null || playlist.username.isBlank()) {
            val base = com.example.data.remote.VodMovieDetails(
                vodId = movieStream.streamId,
                title = movieStream.name,
                coverUrl = movieStream.streamIcon,
                backdropUrl = movieStream.streamIcon,
                rating = movieStream.rating,
                containerExtension = movieStream.containerExtension.ifBlank { "mp4" }
            )
            return@withContext xtreamClient.enrichVodMovieDetailsWithTmdb(
                baseDetails = base,
                rawTitle = movieStream.name
            )
        }
        xtreamClient.fetchVodMovieDetails(
            serverUrl = playlist.serverUrl,
            username = playlist.username,
            password = SecurityManager.decryptCredential(playlist.password),
            vodId = movieStream.streamId,
            fallbackTitle = movieStream.name,
            fallbackCover = movieStream.streamIcon,
            fallbackRating = movieStream.rating,
            fallbackExtension = movieStream.containerExtension
        )
    }

    // Parental Controls (Salted SHA-256 PIN Hashing + Rate Limiting)
    val parentalSettings: Flow<ParentalSettingsEntity?> = parentalDao.getSettings()

    suspend fun getParentalSettingsOnce(): ParentalSettingsEntity {
        return parentalDao.getSettingsOnce() ?: ParentalSettingsEntity()
    }

    suspend fun updateParentalPin(pin: String) {
        val current = getParentalSettingsOnce()
        val hashedPin = SecurityManager.hashPin(pin.trim())
        parentalDao.saveSettings(current.copy(pin = hashedPin))
    }

    suspend fun verifyParentalPin(candidatePin: String): Boolean {
        val current = getParentalSettingsOnce()
        val isMatch = SecurityManager.verifyPin(candidatePin, current.pin)
        // Migrate legacy plaintext PIN to salted SHA-256 hash on successful verification
        if (isMatch && !current.pin.startsWith(SecurityManager.HASHED_PIN_PREFIX)) {
            parentalDao.saveSettings(current.copy(pin = SecurityManager.hashPin(candidatePin)))
        }
        return isMatch
    }

    suspend fun toggleParentalEnabled(enabled: Boolean) {
        val current = getParentalSettingsOnce()
        parentalDao.saveSettings(current.copy(isEnabled = enabled))
    }

    suspend fun setCategoryLocked(playlistId: Long, categoryId: String, isLocked: Boolean) {
        val current = getParentalSettingsOnce()
        val currentList = try {
            val arr = JSONArray(current.lockedCategoriesJson)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) list.add(arr.getString(i))
            list
        } catch (_: Exception) {
            mutableListOf()
        }

        if (isLocked && !currentList.contains(categoryId)) {
            currentList.add(categoryId)
        } else if (!isLocked) {
            currentList.remove(categoryId)
        }

        val jsonArr = JSONArray(currentList)
        parentalDao.saveSettings(current.copy(lockedCategoriesJson = jsonArr.toString()))
        categoryDao.setCategoryLocked(playlistId, categoryId, isLocked)
    }

    suspend fun isCategoryLocked(categoryId: String): Boolean {
        val current = getParentalSettingsOnce()
        if (!current.isEnabled) return false
        return try {
            val arr = JSONArray(current.lockedCategoriesJson)
            for (i in 0 until arr.length()) {
                if (arr.getString(i) == categoryId) return true
            }
            false
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Re-applies persisted category locks from ParentalSettingsEntity.lockedCategoriesJson
     * to CategoryEntity rows after a catalog re-sync (which uses OnConflictStrategy.REPLACE).
     */
    suspend fun restoreLockedCategories(playlistId: Long) {
        val current = getParentalSettingsOnce()
        val lockedIds = try {
            val arr = JSONArray(current.lockedCategoriesJson)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) list.add(arr.getString(i))
            list
        } catch (_: Exception) {
            emptyList()
        }
        lockedIds.forEach { catId ->
            categoryDao.setCategoryLocked(playlistId, catId, true)
        }
    }

    /**
     * Permanently purges any Adult/+18 categories and streams from the local SQLite database.
     * Invoked automatically on app startup and after every catalog synchronization.
     */
    suspend fun purgeAllAdultContent() = withContext(Dispatchers.IO) {
        val purgedAdultCats = categoryDao.purgeAdultCategories()
        if (purgedAdultCats > 0) {
            streamDao.purgeStreamsInDeletedCategories()
        }
        streamDao.purgeAdultStreamsByName()
    }

    /**
     * Executes automatic SQLite database housekeeping:
     * 1. Clears watch history and resume positions older than 60 days without deleting catalog streams.
     * 2. Deletes orphan streams and categories belonging to deleted playlists.
     * 3. Deletes expired EPG program cache rows (`endEpoch < nowEpochSec`).
     * 4. Purges any cached adult/+18 categories and streams.
     */
    suspend fun purgeExpiredHousekeepingData(nowEpochMs: Long = System.currentTimeMillis()) = withContext(Dispatchers.IO) {
        val sixtyDaysAgoMs = nowEpochMs - 60L * 24L * 60L * 60L * 1000L
        val nowEpochSec = nowEpochMs / 1000L
        streamDao.clearExpiredWatchHistory(sixtyDaysAgoMs)
        streamDao.purgeOrphanStreams()
        categoryDao.purgeOrphanCategories()
        epgDao.purgeExpiredEpgPrograms(nowEpochSec)
        purgeAllAdultContent()
    }

    /**
     * Full account synchronization routed sequentially through [syncPlaylistSection]
     * (`LIVE` -> `SERIES` -> `MOVIE`) so only one catalog downloads and processes at a time
     * against the Xtream host with unified `sync_section_state` telemetry and zero overlap.
     */
    suspend fun syncPlaylist(playlist: PlaylistEntity): Result<Unit> = withContext(PlaybackResourceGovernor.syncDispatcher) {
        if (!isSyncInProgress.compareAndSet(false, true)) {
            return@withContext Result.failure(IllegalStateException("A synchronization job is already running"))
        }

        _syncProgress.value = SyncProgress(isSyncing = true, phase = "Connecting to server…")
        val plainPassword = SecurityManager.decryptCredential(playlist.password)

        try {
            // 1. Authenticate & refresh account metadata
            val authResult = xtreamClient.authenticate(
                playlist.serverUrl,
                playlist.username,
                plainPassword
            )

            if (authResult.isFailure) {
                val rawErr = authResult.exceptionOrNull()?.message ?: "Authentication failed"
                val safeErr = SecurityManager.redactSensitiveText(rawErr, playlist.username, plainPassword)
                _syncProgress.value = SyncProgress(isSyncing = false, error = safeErr)
                return@withContext Result.failure(Exception(safeErr))
            }

            val authData = authResult.getOrNull()
            val userInfo = authData?.userInfo
            val resolvedServerUrl = authData?.resolvedServerUrl?.takeIf { it.isNotBlank() } ?: playlist.serverUrl.trim()
            val updatedPlaylist = playlist.copy(serverUrl = resolvedServerUrl).encryptedForStorage().copy(
                status = userInfo?.status ?: "Active",
                expDate = userInfo?.expDate ?: "",
                maxConnections = userInfo?.maxConnections ?: "1",
                lastSyncTimestamp = System.currentTimeMillis()
            )
            playlistDao.updatePlaylist(updatedPlaylist)
            val workingPlaylist = updatedPlaylist.decrypted()

            // 2. Execute LIVE -> SERIES -> MOVIE strictly sequentially via syncPlaylistSection
            val orderedSections = listOf("LIVE", "SERIES", "MOVIE")
            var firstFailure: Throwable? = null

            for (sec in orderedSections) {
                val sectionResult = syncPlaylistSection(
                    playlist = workingPlaylist,
                    sectionType = sec,
                    categoryId = null,
                    forceFullReconcile = false
                )
                if (sectionResult.isFailure && firstFailure == null) {
                    firstFailure = sectionResult.exceptionOrNull()
                }
            }

            val liveCount = streamDao.countStreams(playlist.id, "live")
            val vodCount = streamDao.countStreams(playlist.id, "movie")
            val seriesCount = streamDao.countStreams(playlist.id, "series")

            val latestStored = playlistDao.getPlaylistById(playlist.id) ?: updatedPlaylist
            playlistDao.updatePlaylist(
                latestStored.copy(
                    channelCount = liveCount,
                    vodCount = vodCount,
                    seriesCount = seriesCount,
                    lastSyncTimestamp = System.currentTimeMillis()
                )
            )

            if (firstFailure != null) {
                val safeErr = SecurityManager.redactSensitiveText(
                    firstFailure.message ?: "Catalog synchronization failed",
                    playlist.username,
                    plainPassword
                )
                _syncProgress.value = SyncProgress(isSyncing = false, error = safeErr)
                Result.failure(Exception(safeErr))
            } else {
                _syncProgress.value = SyncProgress(
                    isSyncing = false,
                    phase = "Complete! Synced $liveCount Live, $vodCount Movies, $seriesCount Series.",
                    itemsProcessed = liveCount + vodCount + seriesCount
                )
                Result.success(Unit)
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            val safeErr = SecurityManager.redactSensitiveText(e.message, playlist.username, plainPassword)
            Log.e("IptvRepository", "Sync failed: $safeErr")
            _syncProgress.value = SyncProgress(isSyncing = false, error = safeErr)
            Result.failure(Exception(safeErr))
        } finally {
            isSyncInProgress.set(false)
        }
    }

    /**
     * Granular per-section Smart Incremental Diff synchronization (`"LIVE"`, `"MOVIE"`, or `"SERIES"`).
     *
     * Key architectural guarantees:
     * - Independent locks (`liveSyncLock`, `moviesSyncLock`, `seriesSyncLock`) allow LIVE, MOVIES,
     *   and SERIES to sync without blocking one another.
     * - Never calls `deleteStreamsByType`: preserves `isFavorite`, `isLocked`, `watchHistoryTimestamp`,
     *   and `lastPositionMs` in-place via `updateStreamMetadataByProviderId`, skipping unchanged rows
     *   with 0 SQLite writes via 64-bit FNV-1a `metadataHash`.
     * - Conditional HTTP (`If-None-Match` / `If-Modified-Since` / HTTP 304) short-circuits unchanged
     *   catalogs with zero writes unless [forceFullReconcile] or [categoryId] is set.
     * - Safe Removal Validation protects against truncated/partial provider responses.
     * - Truthful 6-stage monotonic telemetry persisted to `sync_section_state` (`100%` emitted ONLY
     *   after the final database commit in [SyncStage.FINALIZING]).
     */
    suspend fun syncPlaylistSection(
        playlist: PlaylistEntity,
        sectionType: String,
        categoryId: String? = null,
        forceFullReconcile: Boolean = false
    ): Result<Unit> = withContext(PlaybackResourceGovernor.syncDispatcher) {
        val normalizedSection = sectionType.trim().uppercase(Locale.US)
        val (catAction, catType, streamAction, streamType, sectionLabel) = when (normalizedSection) {
            "LIVE", "LIVE_TV" -> SectionSyncSpec("get_live_categories", "LIVE", "get_live_streams", "live", "Live TV")
            "MOVIE", "MOVIES", "VOD" -> SectionSyncSpec("get_vod_categories", "MOVIE", "get_vod_streams", "movie", "Movies")
            "SERIES" -> SectionSyncSpec("get_series_categories", "SERIES", "get_series", "series", "Series")
            else -> return@withContext Result.failure(IllegalArgumentException("Unsupported sectionType: $sectionType"))
        }

        val isVodSection = streamType == "movie" || streamType == "series"
        val sectionLock = when (streamType) {
            "movie" -> moviesSyncLock
            "series" -> seriesSyncLock
            else -> liveSyncLock
        }

        if (!sectionLock.compareAndSet(false, true)) {
            return@withContext Result.failure(IllegalStateException("A $sectionLabel synchronization job is already running"))
        }

        val cleanCategoryId = categoryId?.trim()?.takeIf { it.isNotEmpty() }
        val isCategoryScoped = cleanCategoryId != null
        val existingSyncState = syncSectionStateDao?.getSectionOnce(playlist.id, catType)
        val priorLastSuccessMs = existingSyncState?.lastSuccessfulSyncMs ?: 0L
        val priorEtag = existingSyncState?.etag.orEmpty()
        val priorLastModified = existingSyncState?.lastModified.orEmpty()
        val sendEtag = if (!forceFullReconcile && !isCategoryScoped && priorEtag.isNotBlank()) priorEtag else null
        val sendLastModified = if (!forceFullReconcile && !isCategoryScoped && priorLastModified.isNotBlank()) priorLastModified else null

        val sessionId = UUID.randomUUID().toString()
        val attemptStartMs = System.currentTimeMillis()
        var lastPersistedAtMs = 0L
        var lastPersistedStage = SyncStage.IDLE
        var lastPersistedStatus = SyncStatus.IDLE

        var telemetry = SyncTelemetry(
            playlistId = playlist.id,
            sectionType = catType,
            status = SyncStatus.RUNNING,
            stage = SyncStage.CONNECTING_AND_CHECKING,
            sessionId = sessionId,
            isIndeterminate = false,
            progressPercent = 1,
            lastSuccessfulSyncMs = priorLastSuccessMs,
            lastAttemptedSyncMs = attemptStartMs,
            etag = priorEtag,
            lastModified = priorLastModified,
            lastContentLength = existingSyncState?.lastContentLength ?: -1L
        )

        fun mirrorLegacyUiState(t: SyncTelemetry, statusText: String) {
            if (isVodSection) {
                val mappedPhase = when (t.status) {
                    SyncStatus.COMPLETED -> VodPipelinePhase.READY
                    SyncStatus.FAILED -> VodPipelinePhase.ERROR
                    SyncStatus.RUNNING, SyncStatus.PENDING -> when (t.stage) {
                        SyncStage.GROUPING_AND_ORGANIZING,
                        SyncStage.PERSISTING_DATABASE,
                        SyncStage.FINALIZING -> VodPipelinePhase.ORGANIZING
                        else -> VodPipelinePhase.DOWNLOADING
                    }
                    else -> VodPipelinePhase.IDLE
                }
                val displayedPct = when {
                    t.status == SyncStatus.COMPLETED -> 100
                    t.status == SyncStatus.FAILED -> 0
                    t.isIndeterminate -> 0
                    else -> t.progressPercent.coerceIn(0, 99)
                }
                updateVodPipelineState(
                    streamType,
                    VodPipelineState(
                        phase = mappedPhase,
                        progressPercent = displayedPct,
                        processedCount = t.itemsProcessed,
                        totalCount = t.totalDiscovered,
                        statusText = statusText,
                        error = t.errorMessage.takeIf { it.isNotBlank() }
                    )
                )
            } else {
                _syncProgress.value = SyncProgress(
                    isSyncing = t.status == SyncStatus.RUNNING || t.status == SyncStatus.PENDING,
                    phase = statusText,
                    itemsProcessed = t.itemsProcessed,
                    error = t.errorMessage.takeIf { it.isNotBlank() }
                )
            }
        }

        suspend fun emitTelemetry(
            updated: SyncTelemetry,
            statusText: String,
            forceDbWrite: Boolean = false
        ) {
            val isTerminal = updated.status == SyncStatus.COMPLETED ||
                updated.status == SyncStatus.FAILED ||
                updated.status == SyncStatus.INTERRUPTED
            val monotonicPct = when (updated.status) {
                SyncStatus.COMPLETED -> 100
                SyncStatus.FAILED -> updated.progressPercent.coerceIn(0, 99)
                else -> maxOf(telemetry.progressPercent, updated.progressPercent).coerceIn(0, 99)
            }
            val normalizedTelemetry = updated.copy(
                progressPercent = monotonicPct,
                itemsRemaining = if (updated.totalDiscovered > 0) {
                    (updated.totalDiscovered - updated.itemsProcessed).coerceAtLeast(0)
                } else {
                    0
                }
            )
            telemetry = normalizedTelemetry
            mirrorLegacyUiState(normalizedTelemetry, statusText)

            val now = System.currentTimeMillis()
            val stageChanged = normalizedTelemetry.stage != lastPersistedStage ||
                normalizedTelemetry.status != lastPersistedStatus
            if (forceDbWrite || isTerminal || stageChanged || (now - lastPersistedAtMs >= 500L)) {
                persistSyncTelemetry(normalizedTelemetry)
                lastPersistedAtMs = now
                lastPersistedStage = normalizedTelemetry.stage
                lastPersistedStatus = normalizedTelemetry.status
            }
        }

        val plainPassword = SecurityManager.decryptCredential(playlist.password)

        try {
            // Stage 1: CONNECTING_AND_CHECKING (0..5%)
            emitTelemetry(
                updated = telemetry.copy(
                    status = SyncStatus.RUNNING,
                    stage = SyncStage.CONNECTING_AND_CHECKING,
                    progressPercent = 1,
                    isIndeterminate = false
                ),
                statusText = "Connecting to sync $sectionLabel…",
                forceDbWrite = true
            )

            val authResult = xtreamClient.authenticate(
                playlist.serverUrl,
                playlist.username,
                plainPassword
            )
            if (authResult.isFailure) {
                val rawErr = authResult.exceptionOrNull()?.message ?: "Authentication failed"
                val safeErr = SecurityManager.redactSensitiveText(rawErr, playlist.username, plainPassword)
                emitTelemetry(
                    updated = telemetry.copy(
                        status = SyncStatus.FAILED,
                        isIndeterminate = false,
                        errorMessage = safeErr
                    ),
                    statusText = "$sectionLabel sync failed",
                    forceDbWrite = true
                )
                return@withContext Result.failure(Exception(safeErr))
            }

            val authData = authResult.getOrNull()
            val resolvedServerUrl = authData?.resolvedServerUrl?.takeIf { it.isNotBlank() } ?: playlist.serverUrl.trim()

            emitTelemetry(
                updated = telemetry.copy(
                    stage = SyncStage.CONNECTING_AND_CHECKING,
                    progressPercent = 3
                ),
                statusText = "Fetching $sectionLabel categories…",
                forceDbWrite = false
            )

            val (sectionCatsRaw, adultCatIds) = xtreamClient.fetchCategoriesWithAdultIds(
                resolvedServerUrl, playlist.username, plainPassword,
                action = catAction, categoryType = catType, playlistId = playlist.id
            )
            val sectionCats = sectionCatsRaw.filterNot { SecurityManager.isAdultContent(it.categoryName) }
            if (!isCategoryScoped && sectionCats.isNotEmpty()) {
                runInDbTransaction {
                    categoryDao.clearCategoriesByType(playlist.id, catType)
                    categoryDao.insertCategories(sectionCats)
                }
                restoreLockedCategories(playlist.id)
            }
            val sectionCategoryNameMap = if (isVodSection) {
                val sourceCats = if (sectionCats.isNotEmpty()) sectionCats else categoryDao.getCategoriesOnce(playlist.id, catType)
                sourceCats.associate { it.categoryId to it.categoryName }
            } else {
                emptyMap()
            }

            val hashRows = if (isCategoryScoped) {
                streamDao.getStreamHashesByCategory(playlist.id, streamType, cleanCategoryId!!)
            } else {
                streamDao.getStreamHashesByType(playlist.id, streamType)
            }
            val existingHashes = HashMap<Long, Long>(hashRows.size * 2 + 16)
            for (row in hashRows) {
                existingHashes[row.streamId] = row.metadataHash
            }
            val isInitialSync = existingHashes.isEmpty()

            emitTelemetry(
                updated = telemetry.copy(
                    stage = SyncStage.CONNECTING_AND_CHECKING,
                    progressPercent = 5
                ),
                statusText = "Checking $sectionLabel catalog updates…",
                forceDbWrite = true
            )

            // Stage 2: DOWNLOADING_AND_DIFFING (5..55%)
            var responseContentLength = -1L
            var responseEtag: String? = null
            var responseLastModified: String? = null
            var httpNotModified = false
            val latestBytesRead = AtomicLong(0L)

            var itemsDownloaded = 0
            var itemsAdded = 0
            var itemsUpdated = 0
            var itemsUnchanged = 0
            var itemsSkipped = 0
            var itemsPersisted = 0
            var lastDownloadTelemetryItems = 0
            var lastDownloadTelemetryMs = 0L
            val seenStreamIds = HashSet<Long>(existingHashes.size + 256)
            val dirtyGroupKeys = LinkedHashSet<String>()

            suspend fun maybeEmitDownloadTelemetry() {
                val nowMs = System.currentTimeMillis()
                val itemsDelta = itemsDownloaded - lastDownloadTelemetryItems
                if (itemsDelta >= 5_000 || (nowMs - lastDownloadTelemetryMs) >= 500L) {
                    lastDownloadTelemetryItems = itemsDownloaded
                    lastDownloadTelemetryMs = nowMs
                    val (dlPct, isIndet) = SyncTelemetry.computeDownloadStageProgress(
                        bytesRead = latestBytesRead.get(),
                        contentLength = responseContentLength,
                        previousPercent = telemetry.progressPercent
                    )
                    emitTelemetry(
                        updated = telemetry.copy(
                            stage = SyncStage.DOWNLOADING_AND_DIFFING,
                            progressPercent = dlPct,
                            isIndeterminate = isIndet,
                            itemsDownloaded = itemsDownloaded,
                            itemsProcessed = itemsDownloaded,
                            itemsUnchanged = itemsUnchanged,
                            itemsAdded = itemsAdded,
                            itemsUpdated = itemsUpdated,
                            itemsSkipped = itemsSkipped,
                            itemsPersisted = itemsPersisted,
                            lastContentLength = responseContentLength
                        ),
                        statusText = "Downloading $sectionLabel ($itemsDownloaded items)…"
                    )
                }
            }

            val streamResult = xtreamClient.streamCatalogEntries(
                serverUrl = resolvedServerUrl,
                username = playlist.username,
                password = plainPassword,
                action = streamAction,
                streamType = streamType,
                playlistId = playlist.id,
                batchSize = 2000,
                categoryNameMap = sectionCategoryNameMap,
                categoryId = cleanCategoryId,
                etag = sendEtag,
                lastModified = sendLastModified,
                onHttpMeta = { contentLength, respEtag, respLastMod, notModified ->
                    responseContentLength = contentLength
                    responseEtag = respEtag
                    responseLastModified = respLastMod
                    httpNotModified = notModified
                    if (!notModified) {
                        val isIndet = contentLength <= 0L
                        val nextState = telemetry.copy(
                            stage = SyncStage.DOWNLOADING_AND_DIFFING,
                            progressPercent = maxOf(telemetry.progressPercent, SyncStage.DOWNLOADING_AND_DIFFING.startPct),
                            isIndeterminate = isIndet,
                            lastContentLength = contentLength
                        )
                        telemetry = nextState
                        mirrorLegacyUiState(nextState, "Downloading $sectionLabel…")
                    }
                },
                onBytesRead = { bytes ->
                    latestBytesRead.set(bytes)
                    if (responseContentLength > 0L) {
                        val (pct, isIndet) = SyncTelemetry.computeDownloadStageProgress(
                            bytesRead = bytes,
                            contentLength = responseContentLength,
                            previousPercent = telemetry.progressPercent
                        )
                        if (pct > telemetry.progressPercent) {
                            val nextState = telemetry.copy(
                                stage = SyncStage.DOWNLOADING_AND_DIFFING,
                                progressPercent = pct,
                                isIndeterminate = isIndet
                            )
                            telemetry = nextState
                            mirrorLegacyUiState(
                                nextState,
                                "Downloading $sectionLabel ($itemsDownloaded items)…"
                            )
                        }
                    }
                },
                existingHashes = existingHashes,
                onUnchangedBatch = { unchangedIds ->
                    seenStreamIds.addAll(unchangedIds)
                    itemsDownloaded += unchangedIds.size
                    itemsUnchanged += unchangedIds.size
                    maybeEmitDownloadTelemetry()
                    PlaybackResourceGovernor.throttleIfPlaybackActive()
                }
            ) { batch, _ ->
                val toInsert = ArrayList<StreamEntity>(batch.size / 2 + 16)
                val toUpdate = ArrayList<StreamEntity>(batch.size / 4 + 16)

                for (item in batch) {
                    val isAdultItem = SecurityManager.isAdultContent(item.name) ||
                        (item.categoryId.isNotBlank() && item.categoryId in adultCatIds)
                    if (isAdultItem) {
                        itemsSkipped++
                    } else {
                        itemsDownloaded++
                        seenStreamIds.add(item.streamId)
                        val oldHash = existingHashes[item.streamId]
                        if (oldHash == null) {
                            toInsert.add(item)
                            itemsAdded++
                            if (isVodSection && item.canonicalGroupKey.isNotBlank()) {
                                dirtyGroupKeys.add(item.canonicalGroupKey)
                            }
                        } else if (oldHash != item.metadataHash) {
                            toUpdate.add(item)
                            itemsUpdated++
                            if (isVodSection && item.canonicalGroupKey.isNotBlank()) {
                                dirtyGroupKeys.add(item.canonicalGroupKey)
                            }
                        } else {
                            itemsUnchanged++
                        }
                    }
                }

                if (toInsert.isNotEmpty() || toUpdate.isNotEmpty()) {
                    val dbBatchSize = PlaybackResourceGovernor.recommendedDbBatchSize()
                    if (toInsert.isNotEmpty()) {
                        for (insertChunk in toInsert.chunked(dbBatchSize)) {
                            runInDbTransaction {
                                streamDao.insertStreamsBatch(insertChunk)
                            }
                            PlaybackResourceGovernor.throttleIfPlaybackActive()
                        }
                    }
                    if (toUpdate.isNotEmpty()) {
                        for (updateChunk in toUpdate.chunked(dbBatchSize)) {
                            runInDbTransaction {
                                for (u in updateChunk) {
                                    streamDao.updateStreamMetadataByProviderId(
                                        playlistId = u.playlistId,
                                        streamType = u.streamType,
                                        streamId = u.streamId,
                                        categoryId = u.categoryId,
                                        name = u.name,
                                        streamIcon = u.streamIcon,
                                        epgChannelId = u.epgChannelId,
                                        rating = u.rating,
                                        tvArchive = u.tvArchive,
                                        tvArchiveDuration = u.tvArchiveDuration,
                                        containerExtension = u.containerExtension,
                                        tmdbId = u.tmdbId,
                                        canonicalGroupKey = u.canonicalGroupKey,
                                        versionTag = u.versionTag,
                                        addedEpoch = u.addedEpoch,
                                        ratingNumeric = u.ratingNumeric,
                                        releaseYear = u.releaseYear,
                                        genres = u.genres,
                                        metadataHash = u.metadataHash
                                    )
                                }
                            }
                            PlaybackResourceGovernor.throttleIfPlaybackActive()
                        }
                    }
                    itemsPersisted += toInsert.size + toUpdate.size
                } else {
                    PlaybackResourceGovernor.throttleIfPlaybackActive()
                }

                maybeEmitDownloadTelemetry()
            }

            // Fast-path HTTP 304 Not Modified: zero stream writes
            if (streamResult.notModified || httpNotModified) {
                val existingGroupCount = if (isVodSection && vodGroupDao != null) {
                    vodGroupDao.countGroupsByType(playlist.id, streamType)
                } else {
                    0
                }
                val needsInitialMaterialization = isVodSection && vodGroupDao != null &&
                    existingGroupCount == 0 && existingHashes.isNotEmpty()
                if (!needsInitialMaterialization) {
                    val nowMs = System.currentTimeMillis()
                    val finalCount = streamDao.countStreams(playlist.id, streamType)
                    emitTelemetry(
                        updated = telemetry.copy(
                            status = SyncStatus.COMPLETED,
                            stage = SyncStage.FINALIZING,
                            isIndeterminate = false,
                            progressPercent = 100,
                            totalDiscovered = finalCount,
                            itemsProcessed = finalCount,
                            itemsUnchanged = finalCount,
                            itemsGrouped = existingGroupCount,
                            lastSuccessfulSyncMs = nowMs,
                            etag = responseEtag ?: priorEtag,
                            lastModified = responseLastModified ?: priorLastModified,
                            pendingUpdateAvailable = false,
                            pendingUpdateSummary = "",
                            errorMessage = ""
                        ),
                        statusText = "$sectionLabel up to date",
                        forceDbWrite = true
                    )
                    return@withContext Result.success(Unit)
                }
            }

            // Stage 3: PARSING_AND_VALIDATING (55..65%)
            itemsSkipped += streamResult.skippedAdult
            val totalDiscovered = itemsDownloaded
            emitTelemetry(
                updated = telemetry.copy(
                    stage = SyncStage.PARSING_AND_VALIDATING,
                    isIndeterminate = false,
                    progressPercent = maxOf(telemetry.progressPercent, SyncStage.PARSING_AND_VALIDATING.startPct),
                    totalDiscovered = totalDiscovered,
                    itemsDownloaded = itemsDownloaded,
                    itemsProcessed = itemsDownloaded,
                    itemsUnchanged = itemsUnchanged,
                    itemsAdded = itemsAdded,
                    itemsUpdated = itemsUpdated,
                    itemsSkipped = itemsSkipped,
                    itemsPersisted = itemsPersisted
                ),
                statusText = "Validating $sectionLabel changes…",
                forceDbWrite = true
            )

            // Safe Removal Validation (only when full section completed cleanly)
            var itemsRemoved = 0
            var pendingWarning = ""
            var forceFullVodRebuildFromRemovals = false

            if (!isCategoryScoped && streamResult.completedCleanly && !streamResult.notModified) {
                val missingIds = ArrayList<Long>()
                for (existingId in existingHashes.keys) {
                    if (existingId !in seenStreamIds) {
                        missingIds.add(existingId)
                    }
                }
                if (missingIds.isNotEmpty()) {
                    if (existingHashes.size > 100 && seenStreamIds.size < (existingHashes.size / 2)) {
                        pendingWarning = "Skipped removing ${missingIds.size} $sectionLabel items: provider returned partial catalog (${seenStreamIds.size}/${existingHashes.size})"
                        Log.w("IptvRepository", pendingWarning)
                    } else {
                        if (isVodSection) {
                            if (missingIds.size <= 200) {
                                for (missingId in missingIds) {
                                    val oldStream = streamDao.getStreamByProviderId(playlist.id, missingId)
                                    if (oldStream != null && oldStream.canonicalGroupKey.isNotBlank()) {
                                        dirtyGroupKeys.add(oldStream.canonicalGroupKey)
                                    }
                                }
                            } else {
                                forceFullVodRebuildFromRemovals = true
                            }
                        }
                        val deleteBatchSize = PlaybackResourceGovernor.recommendedDbBatchSize().coerceAtMost(500)
                        for (chunk in missingIds.chunked(deleteBatchSize)) {
                            runInDbTransaction {
                                streamDao.deleteStreamsByProviderIds(playlist.id, streamType, chunk)
                            }
                            PlaybackResourceGovernor.throttleIfPlaybackActive()
                        }
                        itemsRemoved = missingIds.size
                        itemsPersisted += itemsRemoved
                    }
                }
            }

            purgeAllAdultContent()

            emitTelemetry(
                updated = telemetry.copy(
                    stage = SyncStage.PARSING_AND_VALIDATING,
                    progressPercent = SyncStage.PARSING_AND_VALIDATING.endPct,
                    itemsRemoved = itemsRemoved,
                    itemsPersisted = itemsPersisted,
                    pendingUpdateAvailable = pendingWarning.isNotEmpty(),
                    pendingUpdateSummary = pendingWarning
                ),
                statusText = "Validated $sectionLabel catalog…",
                forceDbWrite = true
            )

            // Stage 4 & 5: GROUPING_AND_ORGANIZING (65..85%) & PERSISTING_DATABASE (85..95%)
            var itemsGrouped = 0
            if (isVodSection && vodGroupDao != null) {
                val ctx = appContext ?: cachedAppContext
                val popularityUpdated = if (ctx != null) {
                    runCatching {
                        GlobalPopularityIndexManager.ensureLoaded(ctx, syncOkHttpClient)
                    }.getOrDefault(false)
                } else {
                    false
                }
                val collectionsUpdated = if (ctx != null) {
                    runCatching {
                        GlobalCollectionsIndexManager.ensureLoaded(ctx, syncOkHttpClient)
                    }.getOrDefault(false)
                } else {
                    false
                }
                val cloudIndexUpdated = popularityUpdated || (streamType == "movie" && collectionsUpdated)

                val existingGroupCount = vodGroupDao.countGroupsByType(playlist.id, streamType)
                val dirtyCount = itemsAdded + itemsUpdated + itemsRemoved

                val canSkipStage2 = !isInitialSync &&
                    !forceFullReconcile &&
                    !cloudIndexUpdated &&
                    !forceFullVodRebuildFromRemovals &&
                    dirtyCount == 0 &&
                    existingGroupCount > 0

                val canPartialRecompute = !isInitialSync &&
                    !forceFullReconcile &&
                    !cloudIndexUpdated &&
                    !forceFullVodRebuildFromRemovals &&
                    dirtyCount in 1..2_000 &&
                    existingGroupCount > 0 &&
                    dirtyGroupKeys.isNotEmpty()

                if (canSkipStage2) {
                    itemsGrouped = existingGroupCount
                    emitTelemetry(
                        updated = telemetry.copy(
                            stage = SyncStage.GROUPING_AND_ORGANIZING,
                            progressPercent = SyncStage.GROUPING_AND_ORGANIZING.endPct,
                            itemsGrouped = itemsGrouped
                        ),
                        statusText = "$sectionLabel catalog unchanged",
                        forceDbWrite = false
                    )
                    emitTelemetry(
                        updated = telemetry.copy(
                            stage = SyncStage.PERSISTING_DATABASE,
                            progressPercent = SyncStage.PERSISTING_DATABASE.endPct,
                            itemsGrouped = itemsGrouped
                        ),
                        statusText = "Saving $sectionLabel metadata…",
                        forceDbWrite = false
                    )
                } else if (canPartialRecompute) {
                    emitTelemetry(
                        updated = telemetry.copy(
                            stage = SyncStage.GROUPING_AND_ORGANIZING,
                            progressPercent = 72
                        ),
                        statusText = "Updating ${dirtyGroupKeys.size} changed $sectionLabel groups…",
                        forceDbWrite = true
                    )

                    val (updatedGroups, _) = withContext(Dispatchers.Default) {
                        recomputeAffectedVodGroups(
                            playlistId = playlist.id,
                            streamType = streamType,
                            dirtyGroupKeys = dirtyGroupKeys,
                            categoryNameMap = sectionCategoryNameMap
                        )
                    }
                    itemsGrouped = vodGroupDao.countGroupsByType(playlist.id, streamType)
                    itemsPersisted += updatedGroups

                    emitTelemetry(
                        updated = telemetry.copy(
                            stage = SyncStage.PERSISTING_DATABASE,
                            progressPercent = SyncStage.PERSISTING_DATABASE.endPct,
                            itemsGrouped = itemsGrouped,
                            itemsPersisted = itemsPersisted
                        ),
                        statusText = "Saved $updatedGroups updated $sectionLabel titles…",
                        forceDbWrite = true
                    )
                } else {
                    // Full lossless Union-Find grouping (read-only on streams)
                    val totalStreamRows = streamDao.countStreams(playlist.id, streamType)
                    emitTelemetry(
                        updated = telemetry.copy(
                            stage = SyncStage.GROUPING_AND_ORGANIZING,
                            progressPercent = SyncStage.GROUPING_AND_ORGANIZING.startPct,
                            totalDiscovered = maxOf(totalDiscovered, totalStreamRows),
                            itemsGrouped = 0
                        ),
                        statusText = "Organizing $sectionLabel (0 / $totalStreamRows)…",
                        forceDbWrite = true
                    )

                    if (totalStreamRows <= 0) {
                        runInDbTransaction {
                            vodGroupDao.deleteGroupsByType(playlist.id, streamType)
                            vodGroupDao.deleteGroupMembersByType(playlist.id, streamType)
                            if (streamType == "movie") {
                                vodGroupDao.deleteCollectionsForPlaylist(playlist.id)
                            }
                        }
                        itemsGrouped = 0
                    } else {
                        val allProjections = ArrayList<StreamIndexProjection>(totalStreamRows)
                        val chunkSize = 10_000
                        var offset = 0
                        while (true) {
                            val chunk = streamDao.getStreamIndexProjectionsChunk(
                                playlistId = playlist.id,
                                streamType = streamType,
                                limit = chunkSize,
                                offset = offset
                            )
                            if (chunk.isEmpty()) break
                            allProjections.addAll(chunk)
                            offset += chunk.size

                            // Reading projections stays within 65..67% so progress never jumps to 80% while itemsGrouped == 0
                            val readPct = (SyncStage.GROUPING_AND_ORGANIZING.startPct +
                                ((allProjections.size.toLong() * 2L) / maxOf(totalStreamRows, allProjections.size).toLong()).toInt())
                                .coerceIn(SyncStage.GROUPING_AND_ORGANIZING.startPct, 67)
                            emitTelemetry(
                                updated = telemetry.copy(
                                    stage = SyncStage.GROUPING_AND_ORGANIZING,
                                    progressPercent = maxOf(telemetry.progressPercent, readPct),
                                    itemsGrouped = 0
                                ),
                                statusText = "Loading $sectionLabel (${allProjections.size} / $totalStreamRows)…"
                            )
                            PlaybackResourceGovernor.throttleIfPlaybackActive()
                            if (chunk.size < chunkSize) break
                        }

                        var groupingPersistJob: Job? = null
                        var lastGroupPersistAtMs = 0L

                        val (materializedGroups, materializedMembers, materializedCollections) = withContext(Dispatchers.Default) {
                            CatalogDiffEngine.buildLosslessGroups(
                                playlistId = playlist.id,
                                streamType = streamType,
                                streams = allProjections,
                                categoryNameMap = sectionCategoryNameMap,
                                onProgress = { groupedCount, totalItems ->
                                    val mappedPct = SyncTelemetry.computeStageProgress(
                                        stage = SyncStage.GROUPING_AND_ORGANIZING,
                                        processed = groupedCount,
                                        total = totalItems,
                                        previousPercent = telemetry.progressPercent
                                    )
                                    val nextState = telemetry.copy(
                                        stage = SyncStage.GROUPING_AND_ORGANIZING,
                                        isIndeterminate = false,
                                        progressPercent = mappedPct,
                                        totalDiscovered = maxOf(telemetry.totalDiscovered, totalItems),
                                        itemsGrouped = groupedCount
                                    )
                                    telemetry = nextState
                                    mirrorLegacyUiState(
                                        nextState,
                                        "Grouping $sectionLabel ($groupedCount / $totalItems)…"
                                    )
                                    val nowMs = System.currentTimeMillis()
                                    if (groupedCount >= totalItems || (nowMs - lastGroupPersistAtMs) >= 500L) {
                                        lastGroupPersistAtMs = nowMs
                                        val prevJob = groupingPersistJob
                                        groupingPersistJob = indexingScope.launch(Dispatchers.IO) {
                                            prevJob?.join()
                                            persistSyncTelemetry(nextState)
                                        }
                                    }
                                }
                            )
                        }
                        groupingPersistJob?.join()
                        itemsGrouped = materializedGroups.size

                        emitTelemetry(
                            updated = telemetry.copy(
                                stage = SyncStage.PERSISTING_DATABASE,
                                progressPercent = SyncStage.PERSISTING_DATABASE.startPct + 2,
                                itemsGrouped = itemsGrouped
                            ),
                            statusText = "Saving ${materializedGroups.size} $sectionLabel titles…",
                            forceDbWrite = true
                        )

                        val groupBatchSize = PlaybackResourceGovernor.recommendedDbBatchSize()
                        val memberBatchSize = (groupBatchSize * 2).coerceAtMost(2_000)
                        runInDbTransaction {
                            vodGroupDao.deleteGroupsByType(playlist.id, streamType)
                            vodGroupDao.deleteGroupMembersByType(playlist.id, streamType)
                            if (streamType == "movie") {
                                vodGroupDao.deleteCollectionsForPlaylist(playlist.id)
                            }
                        }
                        PlaybackResourceGovernor.throttleIfPlaybackActive()

                        for (batch in materializedGroups.chunked(groupBatchSize)) {
                            runInDbTransaction {
                                vodGroupDao.insertGroupsBatch(batch)
                            }
                            PlaybackResourceGovernor.throttleIfPlaybackActive()
                        }
                        for (batch in materializedMembers.chunked(memberBatchSize)) {
                            runInDbTransaction {
                                vodGroupDao.insertGroupMembersBatch(batch)
                            }
                            PlaybackResourceGovernor.throttleIfPlaybackActive()
                        }
                        if (streamType == "movie" && materializedCollections.isNotEmpty()) {
                            for (batch in materializedCollections.chunked(groupBatchSize)) {
                                runInDbTransaction {
                                    vodGroupDao.insertCollectionsBatch(batch)
                                }
                                PlaybackResourceGovernor.throttleIfPlaybackActive()
                            }
                        }
                        itemsPersisted += materializedGroups.size
                    }

                    emitTelemetry(
                        updated = telemetry.copy(
                            stage = SyncStage.PERSISTING_DATABASE,
                            progressPercent = SyncStage.PERSISTING_DATABASE.endPct,
                            itemsGrouped = itemsGrouped,
                            itemsPersisted = itemsPersisted
                        ),
                        statusText = "Saved $itemsGrouped $sectionLabel titles…",
                        forceDbWrite = true
                    )
                }
            } else {
                emitTelemetry(
                    updated = telemetry.copy(
                        stage = SyncStage.PERSISTING_DATABASE,
                        progressPercent = SyncStage.PERSISTING_DATABASE.endPct,
                        itemsPersisted = itemsPersisted
                    ),
                    statusText = "Saving $sectionLabel channels…",
                    forceDbWrite = false
                )
            }

            // Stage 6: FINALIZING (95..100%) — emit 100% ONLY after final commit
            emitTelemetry(
                updated = telemetry.copy(
                    stage = SyncStage.FINALIZING,
                    progressPercent = 96
                ),
                statusText = "Finalizing $sectionLabel catalog…",
                forceDbWrite = true
            )

            val finalSectionCount = streamDao.countStreams(playlist.id, streamType)
            val completedAtMs = System.currentTimeMillis()
            val currentStored = playlistDao.getPlaylistById(playlist.id) ?: playlist.encryptedForStorage()
            val updatedPlaylist = when (catType) {
                "LIVE" -> currentStored.copy(channelCount = finalSectionCount, lastSyncTimestamp = completedAtMs)
                "MOVIE" -> currentStored.copy(vodCount = finalSectionCount, lastSyncTimestamp = completedAtMs)
                else -> currentStored.copy(seriesCount = finalSectionCount, lastSyncTimestamp = completedAtMs)
            }
            playlistDao.updatePlaylist(updatedPlaylist)

            val savedEtag = if (!isCategoryScoped && streamResult.completedCleanly) {
                responseEtag ?: priorEtag
            } else {
                priorEtag
            }
            val savedLastModified = if (!isCategoryScoped && streamResult.completedCleanly) {
                responseLastModified ?: priorLastModified
            } else {
                priorLastModified
            }

            val completionText = if (isVodSection) {
                "${if (itemsGrouped > 0) itemsGrouped else finalSectionCount} $sectionLabel ready"
            } else {
                "Complete! Refreshed $finalSectionCount $sectionLabel items."
            }

            emitTelemetry(
                updated = telemetry.copy(
                    status = SyncStatus.COMPLETED,
                    stage = SyncStage.FINALIZING,
                    isIndeterminate = false,
                    progressPercent = 100,
                    totalDiscovered = finalSectionCount,
                    itemsDownloaded = itemsDownloaded,
                    itemsProcessed = finalSectionCount,
                    itemsUnchanged = itemsUnchanged,
                    itemsAdded = itemsAdded,
                    itemsUpdated = itemsUpdated,
                    itemsRemoved = itemsRemoved,
                    itemsSkipped = itemsSkipped,
                    itemsGrouped = itemsGrouped,
                    itemsPersisted = itemsPersisted,
                    lastSuccessfulSyncMs = completedAtMs,
                    etag = savedEtag,
                    lastModified = savedLastModified,
                    pendingUpdateAvailable = pendingWarning.isNotEmpty(),
                    pendingUpdateSummary = pendingWarning,
                    errorMessage = ""
                ),
                statusText = completionText,
                forceDbWrite = true
            )

            Result.success(Unit)
        } catch (ce: CancellationException) {
            withContext(NonCancellable) {
                emitTelemetry(
                    updated = telemetry.copy(
                        status = SyncStatus.INTERRUPTED,
                        isIndeterminate = false,
                        lastSuccessfulSyncMs = priorLastSuccessMs,
                        etag = priorEtag,
                        lastModified = priorLastModified
                    ),
                    statusText = "$sectionLabel sync interrupted",
                    forceDbWrite = true
                )
            }
            throw ce
        } catch (e: Exception) {
            val safeErr = SecurityManager.redactSensitiveText(e.message, playlist.username, plainPassword)
            Log.e("IptvRepository", "Section sync ($sectionType) failed: $safeErr")
            emitTelemetry(
                updated = telemetry.copy(
                    status = SyncStatus.FAILED,
                    isIndeterminate = false,
                    lastSuccessfulSyncMs = priorLastSuccessMs,
                    etag = priorEtag,
                    lastModified = priorLastModified,
                    errorMessage = safeErr
                ),
                statusText = "$sectionLabel sync failed",
                forceDbWrite = true
            )
            Result.failure(Exception(safeErr))
        } finally {
            sectionLock.set(false)
        }
    }

    private data class SectionSyncSpec(
        val catAction: String,
        val catType: String,
        val streamAction: String,
        val streamType: String,
        val sectionLabel: String
    )

    companion object {
        const val DEFAULT_PAGE_SIZE = 180
        private const val EPISODE_RESUME_PREFS_NAME = "setplay_episode_resume_prefs"

        /**
         * Dedicated OkHttpClient with an isolated [Dispatcher] and [ConnectionPool] reserved
         * exclusively for ExoPlayer video transport and [com.example.data.remote.WyzieSubtitleService].
         * Zero socket or dispatcher contention with background Xtream catalog sync.
         */
        val playbackOkHttpClient: OkHttpClient by lazy {
            val playbackDispatcher = Dispatcher().apply {
                maxRequests = 32
                maxRequestsPerHost = 8
            }
            OkHttpClient.Builder()
                .dispatcher(playbackDispatcher)
                .connectionPool(ConnectionPool(8, 5, TimeUnit.MINUTES))
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build()
        }

        /**
         * Dedicated low-concurrency OkHttpClient for Xtream catalog synchronization and
         * [GlobalPopularityIndexManager] background downloads (`maxRequestsPerHost = 2`,
         * `readTimeout = 150s`), isolated from live/VOD video playback sockets.
         */
        val syncOkHttpClient: OkHttpClient by lazy {
            val syncDispatcher = Dispatcher().apply {
                maxRequests = 8
                maxRequestsPerHost = 2
            }
            OkHttpClient.Builder()
                .dispatcher(syncDispatcher)
                .connectionPool(ConnectionPool(4, 3, TimeUnit.MINUTES))
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(150, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build()
        }

        /**
         * Backward-compatible alias pointing to [playbackOkHttpClient].
         */
        val sharedOkHttpClient: OkHttpClient
            get() = playbackOkHttpClient

        @Volatile
        private var INSTANCE: IptvRepository? = null

        @Volatile
        private var cachedAppContext: Context? = null

        fun getInstance(context: Context): IptvRepository {
            val appContext = context.applicationContext
            cachedAppContext = appContext
            initEpisodePreferences(appContext)
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: run {
                    val db = StreamVaultDatabase.getInstance(appContext)
                    val repo = IptvRepository(
                        playlistDao = db.playlistDao(),
                        categoryDao = db.categoryDao(),
                        streamDao = db.streamDao(),
                        epgDao = db.epgDao(),
                        parentalDao = db.parentalSettingsDao(),
                        xtreamClient = XtreamClient(syncOkHttpClient),
                        vodGroupDao = db.vodGroupDao(),
                        database = db,
                        appContext = appContext,
                        syncSectionStateDao = db.syncSectionStateDao()
                    )
                    INSTANCE = repo
                    runCatching {
                        CatalogUpdateCheckWorker.schedulePeriodicUpdateCheck(appContext)
                    }
                    repo
                }
            }
        }

        @Volatile
        private var episodePrefs: SharedPreferences? = null
        private val episodeResumeCache = ConcurrentHashMap<String, EpisodeResumeInfo>()
        private val _episodeResumeStateFlow = MutableStateFlow<Map<String, EpisodeResumeInfo>>(emptyMap())

        fun initEpisodePreferences(context: Context) {
            cachedAppContext = context.applicationContext
            if (episodePrefs == null) {
                synchronized(this) {
                    if (episodePrefs == null) {
                        val prefs = context.applicationContext.getSharedPreferences(
                            EPISODE_RESUME_PREFS_NAME,
                            Context.MODE_PRIVATE
                        )
                        episodePrefs = prefs
                        loadEpisodePreferencesIntoCache(prefs)
                    }
                }
            }
        }

        private fun loadEpisodePreferencesIntoCache(prefs: SharedPreferences) {
            val allEntries = prefs.all
            for ((rawKey, value) in allEntries) {
                if (rawKey.endsWith("_pos") && value is Long && value > 0L) {
                    val baseKey = rawKey.removeSuffix("_pos")
                    val dur = prefs.getLong("${baseKey}_dur", 0L)
                    val ts = prefs.getLong("${baseKey}_ts", 0L)
                    episodeResumeCache[baseKey] = EpisodeResumeInfo(
                        positionMs = value,
                        durationMs = dur,
                        updatedAtMs = ts
                    )
                }
            }
            _episodeResumeStateFlow.value = episodeResumeCache.toMap()
        }

        fun resetEpisodePreferencesForTesting() {
            episodeResumeCache.clear()
            _episodeResumeStateFlow.value = emptyMap()
            episodePrefs?.edit()?.clear()?.apply()
        }
    }
}
