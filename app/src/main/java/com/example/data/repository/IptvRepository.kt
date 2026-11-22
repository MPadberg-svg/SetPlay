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
import com.example.data.local.StreamVaultDatabase
import com.example.data.local.VodCatalogGroupDao
import com.example.data.model.CategoryEntity
import com.example.data.model.CategoryWithCount
import com.example.data.model.EpgProgramEntity
import com.example.data.model.GroupedVodStream
import com.example.data.model.ParentalSettingsEntity
import com.example.data.model.PlaylistEntity
import com.example.data.model.StreamEntity
import com.example.data.model.VodCatalogGroupEntity
import com.example.data.model.VodGroupingNormalizer
import com.example.data.remote.GlobalPopularityIndexManager
import com.example.data.remote.XtreamAuthResponse
import com.example.data.remote.XtreamClient
import com.example.util.SecurityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import okhttp3.OkHttpClient
import org.json.JSONArray
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

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
    private val xtreamClient: XtreamClient = XtreamClient(sharedOkHttpClient),
    private val vodGroupDao: VodCatalogGroupDao? = null,
    private val database: StreamVaultDatabase? = null,
    private val appContext: Context? = null
) {
    private val _syncProgress = MutableStateFlow(SyncProgress())
    val syncProgress = _syncProgress.asStateFlow()

    private val _moviesPipelineState = MutableStateFlow(VodPipelineState())
    val moviesPipelineState: StateFlow<VodPipelineState> = _moviesPipelineState.asStateFlow()

    private val _seriesPipelineState = MutableStateFlow(VodPipelineState())
    val seriesPipelineState: StateFlow<VodPipelineState> = _seriesPipelineState.asStateFlow()

    private val isSyncInProgress = AtomicBoolean(false)
    private val moviesSyncLock = AtomicBoolean(false)
    private val seriesSyncLock = AtomicBoolean(false)
    private val indexingScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val activeIndexJobs = ConcurrentHashMap<String, AtomicBoolean>()

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

    private class GroupAccumulator(
        var representativeStreamDbId: Long,
        var representativeStreamId: Long,
        var representativeRawName: String,
        var representativeScore: Int,
        var tmdbId: String,
        var posterUrl: String,
        var rating: String,
        var ratingNumeric: Double,
        var releaseYear: Int,
        var maxAddedEpoch: Long,
        var genres: String,
        var primaryCategoryId: String,
        var defaultVersionTag: String,
        var containerExtension: String,
        var versionCount: Int,
        val categoryIds: LinkedHashSet<String>
    )

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
     * Streams lightweight [com.example.data.local.StreamIndexProjection] rows for [streamType]
     * (`"movie"` or `"series"`) in bounded 10,000-row chunks on [Dispatchers.Default], uses
     * Stage 1 pre-computed `canonicalGroupKey` / `versionTag` (or computes them in-memory on the
     * fly for legacy rows without ever writing back to `streams`), selects the highest-preference
     * representative copy per group, calculates Bayesian `popularityScore`, and atomically writes
     * materialized rows into `vod_catalog_groups` while emitting determinate progress into that
     * section's independent [VodPipelineState].
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

                val groupsMap = HashMap<String, GroupAccumulator>(4096)
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

                    for (s in chunk) {
                        val effectiveYear = if (s.releaseYear > 0) {
                            s.releaseYear
                        } else {
                            VodGroupingNormalizer.extractReleaseYear(s.name, "")
                        }
                        val groupKey = if (s.canonicalGroupKey.isNotBlank()) {
                            s.canonicalGroupKey
                        } else {
                            VodGroupingNormalizer.computeCanonicalGroupKey(
                                streamType = normalizedType,
                                rawName = s.name,
                                tmdbId = s.tmdbId,
                                releaseYear = effectiveYear,
                                streamId = s.streamId,
                                posterUrl = s.streamIcon
                            )
                        }
                        val versionTag = if (s.versionTag.isNotBlank()) {
                            s.versionTag
                        } else {
                            VodGroupingNormalizer.extractVersionTagWithCategory(
                                rawName = s.name,
                                categoryName = categoryNameMap[s.categoryId].orEmpty()
                            )
                        }

                        val hasPoster = s.streamIcon.isNotBlank()
                        val prefScore = VodGroupingNormalizer.scoreVersionPreference(
                            versionTag = versionTag,
                            hasPoster = hasPoster,
                            containerExt = s.containerExtension,
                            ratingNumeric = s.ratingNumeric
                        )

                        val existing = groupsMap[groupKey]
                        if (existing == null) {
                            val catSet = LinkedHashSet<String>(4)
                            if (s.categoryId.isNotBlank()) catSet.add(s.categoryId)
                            groupsMap[groupKey] = GroupAccumulator(
                                representativeStreamDbId = s.id,
                                representativeStreamId = s.streamId,
                                representativeRawName = s.name,
                                representativeScore = prefScore,
                                tmdbId = s.tmdbId,
                                posterUrl = s.streamIcon,
                                rating = s.rating,
                                ratingNumeric = s.ratingNumeric,
                                releaseYear = effectiveYear,
                                maxAddedEpoch = s.addedEpoch,
                                genres = s.genres,
                                primaryCategoryId = s.categoryId,
                                defaultVersionTag = versionTag,
                                containerExtension = s.containerExtension,
                                versionCount = 1,
                                categoryIds = catSet
                            )
                        } else {
                            existing.versionCount += 1
                            if (existing.tmdbId.isBlank() && s.tmdbId.isNotBlank()) {
                                existing.tmdbId = s.tmdbId
                            }
                            if (s.categoryId.isNotBlank()) {
                                existing.categoryIds.add(s.categoryId)
                            }
                            if (s.addedEpoch > existing.maxAddedEpoch) {
                                existing.maxAddedEpoch = s.addedEpoch
                            }
                            if (s.ratingNumeric > existing.ratingNumeric) {
                                existing.ratingNumeric = s.ratingNumeric
                                if (s.rating.isNotBlank()) existing.rating = s.rating
                            }
                            if (existing.releaseYear == 0 && effectiveYear > 0) {
                                existing.releaseYear = effectiveYear
                            }
                            if (existing.genres.isBlank() && s.genres.isNotBlank()) {
                                existing.genres = s.genres
                            }
                            if (existing.posterUrl.isBlank() && hasPoster) {
                                existing.posterUrl = s.streamIcon
                            }
                            if (prefScore > existing.representativeScore) {
                                existing.representativeScore = prefScore
                                existing.representativeStreamDbId = s.id
                                existing.representativeStreamId = s.streamId
                                existing.representativeRawName = s.name
                                if (hasPoster) existing.posterUrl = s.streamIcon
                                if (s.categoryId.isNotBlank()) existing.primaryCategoryId = s.categoryId
                                existing.defaultVersionTag = versionTag
                                existing.containerExtension = s.containerExtension
                            }
                        }
                    }

                    processed += chunk.size
                    offset += chunk.size
                    val progressPct = ((processed.toLong() * 85L) / totalCount.coerceAtLeast(1).toLong())
                        .toInt()
                        .coerceIn(5, 85)
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
                    if (chunk.size < chunkSize) break
                }

                // Best-effort load of V15 Inverted Cloud Global Popularity Index (fail-silent if offline)
                val ctx = appContext ?: cachedAppContext
                if (ctx != null) {
                    runCatching {
                        GlobalPopularityIndexManager.ensureLoaded(ctx, sharedOkHttpClient)
                    }
                }

                val materialized = ArrayList<VodCatalogGroupEntity>(groupsMap.size)
                for ((groupKey, acc) in groupsMap) {
                    val cleanTitle = VodGroupingNormalizer.extractCleanDisplayTitle(acc.representativeRawName)
                    val upperTag = acc.defaultVersionTag.uppercase(Locale.US)
                    val isPreferredLang = upperTag.contains("ENGLISH") ||
                        upperTag.contains("ARABIC") ||
                        upperTag.contains("FRENCH") ||
                        upperTag.contains("VOSTFR") ||
                        upperTag.contains("MULTI")
                    val globalScore = GlobalPopularityIndexManager.lookupGlobalScore(
                        streamType = normalizedType,
                        tmdbId = acc.tmdbId,
                        cleanTitle = cleanTitle,
                        releaseYear = acc.releaseYear
                    )
                    val popularityScore = VodGroupingNormalizer.computePopularityQualityScore(
                        ratingNumeric = acc.ratingNumeric,
                        versionCount = acc.versionCount,
                        releaseYear = acc.releaseYear,
                        hasPoster = acc.posterUrl.isNotBlank(),
                        isPreferredLang = isPreferredLang,
                        globalTmdbScore = globalScore
                    )
                    val categoryIdsCsv = if (acc.categoryIds.isEmpty()) {
                        ""
                    } else {
                        ",${acc.categoryIds.joinToString(",")},"
                    }

                    materialized.add(
                        VodCatalogGroupEntity(
                            playlistId = playlistId,
                            streamType = normalizedType,
                            groupKey = groupKey,
                            representativeStreamDbId = acc.representativeStreamDbId,
                            representativeStreamId = acc.representativeStreamId,
                            cleanTitle = cleanTitle,
                            posterUrl = acc.posterUrl,
                            rating = acc.rating,
                            ratingNumeric = acc.ratingNumeric,
                            releaseYear = acc.releaseYear,
                            addedEpoch = acc.maxAddedEpoch,
                            genres = acc.genres,
                            primaryCategoryId = acc.primaryCategoryId,
                            categoryIdsCsv = categoryIdsCsv,
                            versionCount = acc.versionCount,
                            defaultVersionTag = acc.defaultVersionTag,
                            containerExtension = acc.containerExtension.ifBlank { "mp4" },
                            popularityScore = popularityScore
                        )
                    )
                }

                updateVodPipelineState(
                    normalizedType,
                    VodPipelineState(
                        phase = VodPipelinePhase.ORGANIZING,
                        progressPercent = 92,
                        processedCount = processed,
                        totalCount = totalCount,
                        statusText = "Finalizing ${materialized.size} $sectionLabel titles…"
                    )
                )

                runInDbTransaction {
                    dao.deleteGroupsByType(playlistId, normalizedType)
                    for (batch in materialized.chunked(2_000)) {
                        dao.insertGroupsBatch(batch)
                    }
                }

                updateVodPipelineState(
                    normalizedType,
                    VodPipelineState(
                        phase = VodPipelinePhase.READY,
                        progressPercent = 100,
                        processedCount = totalCount,
                        totalCount = totalCount,
                        statusText = "${materialized.size} $sectionLabel ready"
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

        var versions = streamDao.getVersionsByGroupKey(playlistId, streamType, canonicalGroupKey)

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
     * Synchronizes the account catalog with streaming bounded batches.
     * Prevents race conditions and preserves previous catalog on failure.
     */
    suspend fun syncPlaylist(playlist: PlaylistEntity): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isSyncInProgress.compareAndSet(false, true)) {
            return@withContext Result.failure(IllegalStateException("A synchronization job is already running"))
        }

        _syncProgress.value = SyncProgress(isSyncing = true, phase = "Connecting to server…")
        val plainPassword = SecurityManager.decryptCredential(playlist.password)

        try {
            // 1. Authenticate & Verify
            val authResult = xtreamClient.authenticate(
                playlist.serverUrl,
                playlist.username,
                plainPassword
            )

            if (authResult.isFailure) {
                val rawErr = authResult.exceptionOrNull()?.message ?: "Authentication failed"
                val safeErr = SecurityManager.redactSensitiveText(rawErr, playlist.username, plainPassword)
                _syncProgress.value = SyncProgress(isSyncing = false, error = safeErr)
                isSyncInProgress.set(false)
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

            // 2. Fetch categories (Live, VOD, Series) while tracking explicit adult category IDs
            _syncProgress.value = SyncProgress(isSyncing = true, phase = "Syncing categories…")
            val (liveCatsRaw, adultLiveCatIds) = xtreamClient.fetchCategoriesWithAdultIds(
                resolvedServerUrl, playlist.username, plainPassword,
                action = "get_live_categories", categoryType = "LIVE", playlistId = playlist.id
            )
            val liveCats = liveCatsRaw.filterNot { SecurityManager.isAdultContent(it.categoryName) }

            val (vodCatsRaw, adultVodCatIds) = xtreamClient.fetchCategoriesWithAdultIds(
                resolvedServerUrl, playlist.username, plainPassword,
                action = "get_vod_categories", categoryType = "MOVIE", playlistId = playlist.id
            )
            val vodCats = vodCatsRaw.filterNot { SecurityManager.isAdultContent(it.categoryName) }

            val (seriesCatsRaw, adultSeriesCatIds) = xtreamClient.fetchCategoriesWithAdultIds(
                resolvedServerUrl, playlist.username, plainPassword,
                action = "get_series_categories", categoryType = "SERIES", playlistId = playlist.id
            )
            val seriesCats = seriesCatsRaw.filterNot { SecurityManager.isAdultContent(it.categoryName) }

            val allCats = liveCats + vodCats + seriesCats
            if (allCats.isNotEmpty()) {
                runInDbTransaction {
                    categoryDao.clearCategoriesForPlaylist(playlist.id)
                    categoryDao.insertCategories(allCats)
                }
                restoreLockedCategories(playlist.id)
            }

            // Snapshot existing favorite streamIds and playback positions before replace so user state survives re-sync
            val liveFavIds = streamDao.getFavoriteProviderIds(playlist.id, "live")
            val vodFavIds = streamDao.getFavoriteProviderIds(playlist.id, "movie")
            val seriesFavIds = streamDao.getFavoriteProviderIds(playlist.id, "series")

            val livePositions = streamDao.getSavedStreamPositions(playlist.id, "live")
            val vodPositions = streamDao.getSavedStreamPositions(playlist.id, "movie")
            val seriesPositions = streamDao.getSavedStreamPositions(playlist.id, "series")

            // 3. Stage 1: Stream Live channels in bounded 2,000-row transactional batches
            _syncProgress.value = SyncProgress(isSyncing = true, phase = "Syncing Live TV channels…")
            var liveFirstBatchCleared = false
            var liveInsertedCount = 0
            xtreamClient.streamCatalogEntries(
                serverUrl = resolvedServerUrl,
                username = playlist.username,
                password = plainPassword,
                action = "get_live_streams",
                streamType = "live",
                playlistId = playlist.id,
                batchSize = 2000
            ) { batch, _ ->
                val safeBatch = batch.filter { item ->
                    !SecurityManager.isAdultContent(item.name) &&
                        (item.categoryId.isBlank() || item.categoryId !in adultLiveCatIds)
                }
                if (safeBatch.isNotEmpty()) {
                    runInDbTransaction {
                        if (!liveFirstBatchCleared) {
                            streamDao.deleteStreamsByType(playlist.id, "live")
                            liveFirstBatchCleared = true
                        }
                        streamDao.insertStreamsBatch(safeBatch)
                    }
                    liveInsertedCount += safeBatch.size
                }
                _syncProgress.value = SyncProgress(
                    isSyncing = true,
                    phase = "Ingesting Live TV ($liveInsertedCount channels)…",
                    itemsProcessed = liveInsertedCount
                )
            }
            if (liveFavIds.isNotEmpty() || livePositions.isNotEmpty()) {
                runInDbTransaction {
                    liveFavIds.chunked(400).forEach { chunk ->
                        streamDao.restoreFavorites(playlist.id, "live", chunk)
                    }
                    livePositions.forEach { snap ->
                        streamDao.restoreStreamPosition(playlist.id, "live", snap.streamId, snap.lastPositionMs, snap.watchHistoryTimestamp)
                    }
                }
            }

            // 4. Stage 1: Stream VOD movies in bounded 2,000-row transactional batches with category-aware pre-computation
            val vodCategoryNameMap = vodCats.associate { it.categoryId to it.categoryName }
            _syncProgress.value = SyncProgress(isSyncing = true, phase = "Syncing Movies catalog…")
            updateVodPipelineState(
                "movie",
                VodPipelineState(
                    phase = VodPipelinePhase.DOWNLOADING,
                    progressPercent = 5,
                    totalCount = playlist.vodCount,
                    statusText = "Downloading Movies catalog…"
                )
            )
            var vodFirstBatchCleared = false
            var vodInsertedCount = 0
            xtreamClient.streamCatalogEntries(
                serverUrl = resolvedServerUrl,
                username = playlist.username,
                password = plainPassword,
                action = "get_vod_streams",
                streamType = "movie",
                playlistId = playlist.id,
                batchSize = 2000,
                categoryNameMap = vodCategoryNameMap
            ) { batch, _ ->
                val safeBatch = batch.filter { item ->
                    !SecurityManager.isAdultContent(item.name) &&
                        (item.categoryId.isBlank() || item.categoryId !in adultVodCatIds)
                }
                if (safeBatch.isNotEmpty()) {
                    runInDbTransaction {
                        if (!vodFirstBatchCleared) {
                            streamDao.deleteStreamsByType(playlist.id, "movie")
                            vodFirstBatchCleared = true
                        }
                        streamDao.insertStreamsBatch(safeBatch)
                    }
                    vodInsertedCount += safeBatch.size
                }
                val vodDownloadPct = computeDownloadProgressPercent(vodInsertedCount, playlist.vodCount)
                updateVodPipelineState(
                    "movie",
                    VodPipelineState(
                        phase = VodPipelinePhase.DOWNLOADING,
                        progressPercent = vodDownloadPct,
                        processedCount = vodInsertedCount,
                        totalCount = playlist.vodCount.coerceAtLeast(vodInsertedCount),
                        statusText = "Downloading Movies ($vodInsertedCount titles)…"
                    )
                )
                _syncProgress.value = SyncProgress(
                    isSyncing = true,
                    phase = "Ingesting Movies ($vodInsertedCount titles)…",
                    itemsProcessed = vodInsertedCount
                )
            }
            if (vodFavIds.isNotEmpty() || vodPositions.isNotEmpty()) {
                runInDbTransaction {
                    vodFavIds.chunked(400).forEach { chunk ->
                        streamDao.restoreFavorites(playlist.id, "movie", chunk)
                    }
                    vodPositions.forEach { snap ->
                        streamDao.restoreStreamPosition(playlist.id, "movie", snap.streamId, snap.lastPositionMs, snap.watchHistoryTimestamp)
                    }
                }
            }

            // 5. Stage 1: Stream Series in bounded 2,000-row transactional batches (100% parity with Movies)
            val seriesCategoryNameMap = seriesCats.associate { it.categoryId to it.categoryName }
            _syncProgress.value = SyncProgress(isSyncing = true, phase = "Syncing Series catalog…")
            updateVodPipelineState(
                "series",
                VodPipelineState(
                    phase = VodPipelinePhase.DOWNLOADING,
                    progressPercent = 5,
                    totalCount = playlist.seriesCount,
                    statusText = "Downloading Series catalog…"
                )
            )
            var seriesFirstBatchCleared = false
            var seriesInsertedCount = 0
            xtreamClient.streamCatalogEntries(
                serverUrl = resolvedServerUrl,
                username = playlist.username,
                password = plainPassword,
                action = "get_series",
                streamType = "series",
                playlistId = playlist.id,
                batchSize = 2000,
                categoryNameMap = seriesCategoryNameMap
            ) { batch, _ ->
                val safeBatch = batch.filter { item ->
                    !SecurityManager.isAdultContent(item.name) &&
                        (item.categoryId.isBlank() || item.categoryId !in adultSeriesCatIds)
                }
                if (safeBatch.isNotEmpty()) {
                    runInDbTransaction {
                        if (!seriesFirstBatchCleared) {
                            streamDao.deleteStreamsByType(playlist.id, "series")
                            seriesFirstBatchCleared = true
                        }
                        streamDao.insertStreamsBatch(safeBatch)
                    }
                    seriesInsertedCount += safeBatch.size
                }
                val seriesDownloadPct = computeDownloadProgressPercent(seriesInsertedCount, playlist.seriesCount)
                updateVodPipelineState(
                    "series",
                    VodPipelineState(
                        phase = VodPipelinePhase.DOWNLOADING,
                        progressPercent = seriesDownloadPct,
                        processedCount = seriesInsertedCount,
                        totalCount = playlist.seriesCount.coerceAtLeast(seriesInsertedCount),
                        statusText = "Downloading Series ($seriesInsertedCount shows)…"
                    )
                )
                _syncProgress.value = SyncProgress(
                    isSyncing = true,
                    phase = "Ingesting Series ($seriesInsertedCount shows)…",
                    itemsProcessed = seriesInsertedCount
                )
            }
            if (seriesFavIds.isNotEmpty() || seriesPositions.isNotEmpty()) {
                runInDbTransaction {
                    seriesFavIds.chunked(400).forEach { chunk ->
                        streamDao.restoreFavorites(playlist.id, "series", chunk)
                    }
                    seriesPositions.forEach { snap ->
                        streamDao.restoreStreamPosition(playlist.id, "series", snap.streamId, snap.lastPositionMs, snap.watchHistoryTimestamp)
                    }
                }
            }

            // Final multi-layer SQLite sweep to guarantee zero adult categories or streams remain
            purgeAllAdultContent()

            val liveCount = liveInsertedCount
            val vodCount = vodInsertedCount
            val seriesCount = seriesInsertedCount

            // Update counts in playlist
            val finalPlaylist = updatedPlaylist.copy(
                channelCount = liveCount,
                vodCount = vodCount,
                seriesCount = seriesCount
            )
            playlistDao.updatePlaylist(finalPlaylist)

            _syncProgress.value = SyncProgress(
                isSyncing = false,
                phase = "Complete! Synced $liveCount Live, $vodCount Movies, $seriesCount Series.",
                itemsProcessed = liveCount + vodCount + seriesCount
            )

            // Stage 2: Independent asynchronous read-only materialization into vod_catalog_groups
            if (vodCount > 0 && vodGroupDao != null) {
                updateVodPipelineState(
                    "movie",
                    VodPipelineState(
                        phase = VodPipelinePhase.ORGANIZING,
                        progressPercent = 0,
                        processedCount = 0,
                        totalCount = vodCount,
                        statusText = "Organizing Movies…"
                    )
                )
                indexingScope.launch {
                    rebuildVodCatalogGroups(playlist.id, "movie", emitSyncStatus = false)
                }
            } else {
                updateVodPipelineState(
                    "movie",
                    VodPipelineState(
                        phase = VodPipelinePhase.READY,
                        progressPercent = 100,
                        processedCount = vodCount,
                        totalCount = vodCount,
                        statusText = "Movies ready"
                    )
                )
            }

            if (seriesCount > 0 && vodGroupDao != null) {
                updateVodPipelineState(
                    "series",
                    VodPipelineState(
                        phase = VodPipelinePhase.ORGANIZING,
                        progressPercent = 0,
                        processedCount = 0,
                        totalCount = seriesCount,
                        statusText = "Organizing Series…"
                    )
                )
                indexingScope.launch {
                    rebuildVodCatalogGroups(playlist.id, "series", emitSyncStatus = false)
                }
            } else {
                updateVodPipelineState(
                    "series",
                    VodPipelineState(
                        phase = VodPipelinePhase.READY,
                        progressPercent = 100,
                        processedCount = seriesCount,
                        totalCount = seriesCount,
                        statusText = "Series ready"
                    )
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            val safeErr = SecurityManager.redactSensitiveText(e.message, playlist.username, plainPassword)
            Log.e("IptvRepository", "Sync failed: $safeErr")
            _syncProgress.value = SyncProgress(isSyncing = false, error = safeErr)
            if (_moviesPipelineState.value.phase == VodPipelinePhase.DOWNLOADING) {
                updateVodPipelineState(
                    "movie",
                    VodPipelineState(phase = VodPipelinePhase.ERROR, statusText = "Movies sync failed", error = safeErr)
                )
            }
            if (_seriesPipelineState.value.phase == VodPipelinePhase.DOWNLOADING) {
                updateVodPipelineState(
                    "series",
                    VodPipelineState(phase = VodPipelinePhase.ERROR, statusText = "Series sync failed", error = safeErr)
                )
            }
            Result.failure(Exception(safeErr))
        } finally {
            isSyncInProgress.set(false)
        }
    }

    private fun computeDownloadProgressPercent(processed: Int, expectedTotal: Int): Int {
        if (expectedTotal > 0) {
            return ((processed.toLong() * 90L) / expectedTotal.toLong()).toInt().coerceIn(5, 95)
        }
        return (10 + (processed / 2_000) * 3).coerceIn(10, 90)
    }

    /**
     * Granular per-section catalog refresh (`"LIVE"`, `"MOVIE"`, or `"SERIES"`).
     * Movies and Series acquire completely independent locks (`moviesSyncLock` / `seriesSyncLock`)
     * and update only their own [VodPipelineState] (`moviesPipelineState` / `seriesPipelineState`)
     * without blocking each other or mutating `_syncProgress`. Live TV continues using
     * `isSyncInProgress` and `_syncProgress` for compatibility.
     */
    suspend fun syncPlaylistSection(playlist: PlaylistEntity, sectionType: String): Result<Unit> = withContext(Dispatchers.IO) {
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
            else -> isSyncInProgress
        }

        if (!sectionLock.compareAndSet(false, true)) {
            return@withContext Result.failure(IllegalStateException("A $sectionLabel synchronization job is already running"))
        }

        val expectedTotal = when (streamType) {
            "movie" -> playlist.vodCount
            "series" -> playlist.seriesCount
            else -> playlist.channelCount
        }
        if (isVodSection) {
            updateVodPipelineState(
                streamType,
                VodPipelineState(
                    phase = VodPipelinePhase.DOWNLOADING,
                    progressPercent = 5,
                    processedCount = 0,
                    totalCount = expectedTotal,
                    statusText = "Connecting to sync $sectionLabel…"
                )
            )
        } else {
            _syncProgress.value = SyncProgress(isSyncing = true, phase = "Refreshing $sectionLabel…")
        }

        val plainPassword = SecurityManager.decryptCredential(playlist.password)

        try {
            val authResult = xtreamClient.authenticate(
                playlist.serverUrl,
                playlist.username,
                plainPassword
            )
            if (authResult.isFailure) {
                val rawErr = authResult.exceptionOrNull()?.message ?: "Authentication failed"
                val safeErr = SecurityManager.redactSensitiveText(rawErr, playlist.username, plainPassword)
                if (isVodSection) {
                    updateVodPipelineState(
                        streamType,
                        VodPipelineState(
                            phase = VodPipelinePhase.ERROR,
                            progressPercent = 0,
                            statusText = "$sectionLabel sync failed",
                            error = safeErr
                        )
                    )
                } else {
                    _syncProgress.value = SyncProgress(isSyncing = false, error = safeErr)
                }
                return@withContext Result.failure(Exception(safeErr))
            }

            val authData = authResult.getOrNull()
            val resolvedServerUrl = authData?.resolvedServerUrl?.takeIf { it.isNotBlank() } ?: playlist.serverUrl.trim()

            // 1. Fetch & replace ONLY this section's categories while tracking explicit adult category IDs
            if (isVodSection) {
                updateVodPipelineState(
                    streamType,
                    VodPipelineState(
                        phase = VodPipelinePhase.DOWNLOADING,
                        progressPercent = 10,
                        processedCount = 0,
                        totalCount = expectedTotal,
                        statusText = "Fetching $sectionLabel categories…"
                    )
                )
            }
            val (sectionCatsRaw, adultCatIds) = xtreamClient.fetchCategoriesWithAdultIds(
                resolvedServerUrl, playlist.username, plainPassword,
                action = catAction, categoryType = catType, playlistId = playlist.id
            )
            val sectionCats = sectionCatsRaw.filterNot { SecurityManager.isAdultContent(it.categoryName) }
            val sectionCategoryNameMap = if (isVodSection) {
                sectionCats.associate { it.categoryId to it.categoryName }
            } else {
                emptyMap()
            }

            if (sectionCats.isNotEmpty()) {
                runInDbTransaction {
                    categoryDao.clearCategoriesByType(playlist.id, catType)
                    categoryDao.insertCategories(sectionCats)
                }
                restoreLockedCategories(playlist.id)
            }

            // 2. Snapshot favorites & positions for this section's streamType
            val favIds = streamDao.getFavoriteProviderIds(playlist.id, streamType)
            val savedPositions = streamDao.getSavedStreamPositions(playlist.id, streamType)

            // 3. Stage 1: Stream & replace ONLY this section's catalog entries in 2,000-row transactional batches
            var firstBatchCleared = false
            var insertedCount = 0
            xtreamClient.streamCatalogEntries(
                serverUrl = resolvedServerUrl,
                username = playlist.username,
                password = plainPassword,
                action = streamAction,
                streamType = streamType,
                playlistId = playlist.id,
                batchSize = 2000,
                categoryNameMap = sectionCategoryNameMap
            ) { batch, _ ->
                val safeBatch = batch.filter { item ->
                    !SecurityManager.isAdultContent(item.name) &&
                        (item.categoryId.isBlank() || item.categoryId !in adultCatIds)
                }
                if (safeBatch.isNotEmpty()) {
                    runInDbTransaction {
                        if (!firstBatchCleared) {
                            streamDao.deleteStreamsByType(playlist.id, streamType)
                            firstBatchCleared = true
                        }
                        streamDao.insertStreamsBatch(safeBatch)
                    }
                    insertedCount += safeBatch.size
                }
                if (isVodSection) {
                    val downloadPct = computeDownloadProgressPercent(insertedCount, expectedTotal)
                    updateVodPipelineState(
                        streamType,
                        VodPipelineState(
                            phase = VodPipelinePhase.DOWNLOADING,
                            progressPercent = downloadPct,
                            processedCount = insertedCount,
                            totalCount = expectedTotal.coerceAtLeast(insertedCount),
                            statusText = "Downloading $sectionLabel ($insertedCount items)…"
                        )
                    )
                } else {
                    _syncProgress.value = SyncProgress(
                        isSyncing = true,
                        phase = "Refreshing $sectionLabel ($insertedCount items)…",
                        itemsProcessed = insertedCount
                    )
                }
            }

            if (favIds.isNotEmpty() || savedPositions.isNotEmpty()) {
                runInDbTransaction {
                    favIds.chunked(400).forEach { chunk ->
                        streamDao.restoreFavorites(playlist.id, streamType, chunk)
                    }
                    savedPositions.forEach { snap ->
                        streamDao.restoreStreamPosition(playlist.id, streamType, snap.streamId, snap.lastPositionMs, snap.watchHistoryTimestamp)
                    }
                }
            }

            purgeAllAdultContent()

            val currentStored = playlistDao.getPlaylistById(playlist.id) ?: playlist.encryptedForStorage()
            val updatedPlaylist = when (catType) {
                "LIVE" -> currentStored.copy(channelCount = insertedCount, lastSyncTimestamp = System.currentTimeMillis())
                "MOVIE" -> currentStored.copy(vodCount = insertedCount, lastSyncTimestamp = System.currentTimeMillis())
                else -> currentStored.copy(seriesCount = insertedCount, lastSyncTimestamp = System.currentTimeMillis())
            }
            playlistDao.updatePlaylist(updatedPlaylist)

            if (isVodSection) {
                if (insertedCount > 0 && vodGroupDao != null) {
                    updateVodPipelineState(
                        streamType,
                        VodPipelineState(
                            phase = VodPipelinePhase.ORGANIZING,
                            progressPercent = 0,
                            processedCount = 0,
                            totalCount = insertedCount,
                            statusText = "Organizing $sectionLabel…"
                        )
                    )
                    // Stage 2: Decoupled read-only background materialization into vod_catalog_groups
                    indexingScope.launch {
                        rebuildVodCatalogGroups(playlist.id, streamType, emitSyncStatus = false)
                    }
                } else {
                    updateVodPipelineState(
                        streamType,
                        VodPipelineState(
                            phase = VodPipelinePhase.READY,
                            progressPercent = 100,
                            processedCount = insertedCount,
                            totalCount = insertedCount,
                            statusText = "$sectionLabel ready"
                        )
                    )
                }
            } else {
                _syncProgress.value = SyncProgress(
                    isSyncing = false,
                    phase = "Complete! Refreshed $insertedCount $sectionLabel items.",
                    itemsProcessed = insertedCount
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            val safeErr = SecurityManager.redactSensitiveText(e.message, playlist.username, plainPassword)
            Log.e("IptvRepository", "Section sync ($sectionType) failed: $safeErr")
            if (isVodSection) {
                updateVodPipelineState(
                    streamType,
                    VodPipelineState(
                        phase = VodPipelinePhase.ERROR,
                        progressPercent = 0,
                        statusText = "$sectionLabel sync failed",
                        error = safeErr
                    )
                )
            } else {
                _syncProgress.value = SyncProgress(isSyncing = false, error = safeErr)
            }
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
         * Globally shared OkHttpClient with HTTP/2 multiplexing & connection pool
         * shared across Xtream catalog sync, VOD/Series metadata, and ExoPlayer media transport.
         */
        val sharedOkHttpClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .connectionPool(ConnectionPool(10, 5, TimeUnit.MINUTES))
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(150, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build()
        }

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
                    IptvRepository(
                        playlistDao = db.playlistDao(),
                        categoryDao = db.categoryDao(),
                        streamDao = db.streamDao(),
                        epgDao = db.epgDao(),
                        parentalDao = db.parentalSettingsDao(),
                        xtreamClient = XtreamClient(sharedOkHttpClient),
                        vodGroupDao = db.vodGroupDao(),
                        database = db,
                        appContext = appContext
                    ).also { INSTANCE = it }
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
