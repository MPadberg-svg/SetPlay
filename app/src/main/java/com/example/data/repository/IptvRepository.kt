package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.local.CategoryDao
import com.example.data.local.EpgDao
import com.example.data.local.ParentalSettingsDao
import com.example.data.local.PlaylistDao
import com.example.data.local.StreamDao
import com.example.data.local.StreamVaultDatabase
import com.example.data.model.CategoryEntity
import com.example.data.model.CategoryWithCount
import com.example.data.model.EpgProgramEntity
import com.example.data.model.ParentalSettingsEntity
import com.example.data.model.PlaylistEntity
import com.example.data.model.StreamEntity
import com.example.data.remote.XtreamAuthResponse
import com.example.data.remote.XtreamClient
import com.example.util.SecurityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import org.json.JSONArray
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

data class SyncProgress(
    val isSyncing: Boolean = false,
    val phase: String = "",
    val itemsProcessed: Int = 0,
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
    private val xtreamClient: XtreamClient = XtreamClient(sharedOkHttpClient)
) {
    private val _syncProgress = MutableStateFlow(SyncProgress())
    val syncProgress = _syncProgress.asStateFlow()

    private val isSyncInProgress = AtomicBoolean(false)

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
        categoryDao.purgeAdultCategories()
        streamDao.purgeStreamsInDeletedCategories()
        streamDao.purgeAdultStreamsByName()
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

            // 2. Fetch categories (Live, VOD, Series) — adult categories are already dropped by XtreamClient
            _syncProgress.value = SyncProgress(isSyncing = true, phase = "Syncing categories…")
            val liveCats = xtreamClient.fetchCategories(
                resolvedServerUrl, playlist.username, plainPassword,
                action = "get_live_categories", categoryType = "LIVE", playlistId = playlist.id
            ).filterNot { SecurityManager.isAdultContent(it.categoryName) }
            val vodCats = xtreamClient.fetchCategories(
                resolvedServerUrl, playlist.username, plainPassword,
                action = "get_vod_categories", categoryType = "MOVIE", playlistId = playlist.id
            ).filterNot { SecurityManager.isAdultContent(it.categoryName) }
            val seriesCats = xtreamClient.fetchCategories(
                resolvedServerUrl, playlist.username, plainPassword,
                action = "get_series_categories", categoryType = "SERIES", playlistId = playlist.id
            ).filterNot { SecurityManager.isAdultContent(it.categoryName) }

            val allowedLiveCatIds = liveCats.map { it.categoryId }.toSet()
            val allowedVodCatIds = vodCats.map { it.categoryId }.toSet()
            val allowedSeriesCatIds = seriesCats.map { it.categoryId }.toSet()

            val allCats = liveCats + vodCats + seriesCats
            if (allCats.isNotEmpty()) {
                categoryDao.clearCategoriesForPlaylist(playlist.id)
                categoryDao.insertCategories(allCats)
                val lockedCategoriesJson = getParentalSettingsOnce().lockedCategoriesJson
                val lockedCategoryIds = try {
                    val arr = JSONArray(lockedCategoriesJson)
                    buildList {
                        for (i in 0 until arr.length()) add(arr.getString(i))
                    }
                } catch (_: Exception) {
                    emptyList()
                }
                lockedCategoryIds.forEach { catId ->
                    categoryDao.setCategoryLocked(playlist.id, catId, true)
                }
            }

            // Snapshot existing favorite streamIds and playback positions before replace so user state survives re-sync
            val liveFavIds = streamDao.getFavoriteProviderIds(playlist.id, "live")
            val vodFavIds = streamDao.getFavoriteProviderIds(playlist.id, "movie")
            val seriesFavIds = streamDao.getFavoriteProviderIds(playlist.id, "series")

            val livePositions = streamDao.getSavedStreamPositions(playlist.id, "live")
            val vodPositions = streamDao.getSavedStreamPositions(playlist.id, "movie")
            val seriesPositions = streamDao.getSavedStreamPositions(playlist.id, "series")

            // 3. Stream Live channels in bounded batches
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
                batchSize = 500
            ) { batch, _ ->
                if (!liveFirstBatchCleared) {
                    streamDao.deleteStreamsByType(playlist.id, "live")
                    liveFirstBatchCleared = true
                }
                val safeBatch = batch.filter { item ->
                    !SecurityManager.isAdultContent(item.name) &&
                        (allowedLiveCatIds.isEmpty() || item.categoryId.isBlank() || item.categoryId in allowedLiveCatIds)
                }
                if (safeBatch.isNotEmpty()) {
                    streamDao.insertStreamsBatch(safeBatch)
                    liveInsertedCount += safeBatch.size
                }
                _syncProgress.value = SyncProgress(
                    isSyncing = true,
                    phase = "Ingesting Live TV ($liveInsertedCount channels)…",
                    itemsProcessed = liveInsertedCount
                )
            }
            if (liveFavIds.isNotEmpty()) {
                liveFavIds.chunked(400).forEach { chunk ->
                    streamDao.restoreFavorites(playlist.id, "live", chunk)
                }
            }
            livePositions.forEach { snap ->
                streamDao.restoreStreamPosition(playlist.id, "live", snap.streamId, snap.lastPositionMs, snap.watchHistoryTimestamp)
            }

            // 4. Stream VOD movies in bounded batches
            _syncProgress.value = SyncProgress(isSyncing = true, phase = "Syncing Movies catalog…")
            var vodFirstBatchCleared = false
            var vodInsertedCount = 0
            xtreamClient.streamCatalogEntries(
                serverUrl = resolvedServerUrl,
                username = playlist.username,
                password = plainPassword,
                action = "get_vod_streams",
                streamType = "movie",
                playlistId = playlist.id,
                batchSize = 500
            ) { batch, _ ->
                if (!vodFirstBatchCleared) {
                    streamDao.deleteStreamsByType(playlist.id, "movie")
                    vodFirstBatchCleared = true
                }
                val safeBatch = batch.filter { item ->
                    !SecurityManager.isAdultContent(item.name) &&
                        (allowedVodCatIds.isEmpty() || item.categoryId.isBlank() || item.categoryId in allowedVodCatIds)
                }
                if (safeBatch.isNotEmpty()) {
                    streamDao.insertStreamsBatch(safeBatch)
                    vodInsertedCount += safeBatch.size
                }
                _syncProgress.value = SyncProgress(
                    isSyncing = true,
                    phase = "Ingesting Movies ($vodInsertedCount titles)…",
                    itemsProcessed = vodInsertedCount
                )
            }
            if (vodFavIds.isNotEmpty()) {
                vodFavIds.chunked(400).forEach { chunk ->
                    streamDao.restoreFavorites(playlist.id, "movie", chunk)
                }
            }
            vodPositions.forEach { snap ->
                streamDao.restoreStreamPosition(playlist.id, "movie", snap.streamId, snap.lastPositionMs, snap.watchHistoryTimestamp)
            }

            // 5. Stream Series in bounded batches
            _syncProgress.value = SyncProgress(isSyncing = true, phase = "Syncing Series catalog…")
            var seriesFirstBatchCleared = false
            var seriesInsertedCount = 0
            xtreamClient.streamCatalogEntries(
                serverUrl = resolvedServerUrl,
                username = playlist.username,
                password = plainPassword,
                action = "get_series",
                streamType = "series",
                playlistId = playlist.id,
                batchSize = 500
            ) { batch, _ ->
                if (!seriesFirstBatchCleared) {
                    streamDao.deleteStreamsByType(playlist.id, "series")
                    seriesFirstBatchCleared = true
                }
                val safeBatch = batch.filter { item ->
                    !SecurityManager.isAdultContent(item.name) &&
                        (allowedSeriesCatIds.isEmpty() || item.categoryId.isBlank() || item.categoryId in allowedSeriesCatIds)
                }
                if (safeBatch.isNotEmpty()) {
                    streamDao.insertStreamsBatch(safeBatch)
                    seriesInsertedCount += safeBatch.size
                }
                _syncProgress.value = SyncProgress(
                    isSyncing = true,
                    phase = "Ingesting Series ($seriesInsertedCount shows)…",
                    itemsProcessed = seriesInsertedCount
                )
            }
            if (seriesFavIds.isNotEmpty()) {
                seriesFavIds.chunked(400).forEach { chunk ->
                    streamDao.restoreFavorites(playlist.id, "series", chunk)
                }
            }
            seriesPositions.forEach { snap ->
                streamDao.restoreStreamPosition(playlist.id, "series", snap.streamId, snap.lastPositionMs, snap.watchHistoryTimestamp)
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
            Result.success(Unit)
        } catch (e: Exception) {
            val safeErr = SecurityManager.redactSensitiveText(e.message, playlist.username, plainPassword)
            Log.e("IptvRepository", "Sync failed: $safeErr")
            _syncProgress.value = SyncProgress(isSyncing = false, error = safeErr)
            Result.failure(Exception(safeErr))
        } finally {
            isSyncInProgress.set(false)
        }
    }

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
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build()
        }

        @Volatile
        private var INSTANCE: IptvRepository? = null

        fun getInstance(context: Context): IptvRepository {
            val appContext = context.applicationContext
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
                        xtreamClient = XtreamClient(sharedOkHttpClient)
                    ).also { INSTANCE = it }
                }
            }
        }

        @Volatile
        private var episodePrefs: SharedPreferences? = null
        private val episodeResumeCache = ConcurrentHashMap<String, EpisodeResumeInfo>()
        private val _episodeResumeStateFlow = MutableStateFlow<Map<String, EpisodeResumeInfo>>(emptyMap())

        fun initEpisodePreferences(context: Context) {
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
