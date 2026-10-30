package com.example.data.repository

import android.util.Log
import com.example.data.local.CategoryDao
import com.example.data.local.EpgDao
import com.example.data.local.ParentalSettingsDao
import com.example.data.local.PlaylistDao
import com.example.data.local.StreamDao
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.util.concurrent.atomic.AtomicBoolean

data class SyncProgress(
    val isSyncing: Boolean = false,
    val phase: String = "",
    val itemsProcessed: Int = 0,
    val error: String? = null
)

class IptvRepository(
    private val playlistDao: PlaylistDao,
    private val categoryDao: CategoryDao,
    private val streamDao: StreamDao,
    private val epgDao: EpgDao,
    private val parentalDao: ParentalSettingsDao,
    private val xtreamClient: XtreamClient = XtreamClient()
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

    // Streams & Fast Indexed Queries
    fun queryStreamsFast(
        playlistId: Long,
        streamType: String,
        categoryId: String?,
        query: String
    ): Flow<List<StreamEntity>> {
        val cleanQuery = query.trim()
        val hasCategory = !categoryId.isNullOrBlank()
        val hasQuery = cleanQuery.isNotEmpty()

        return when {
            !hasCategory && !hasQuery -> streamDao.getStreamsInitial(playlistId, streamType, limit = 180)
            hasCategory && !hasQuery -> streamDao.getStreamsByCategoryFast(playlistId, streamType, categoryId!!, limit = 200)
            !hasCategory && hasQuery -> streamDao.searchStreamsByTypeFast(playlistId, streamType, cleanQuery, limit = 150)
            else -> streamDao.searchStreamsInCategoryFast(playlistId, streamType, categoryId!!, cleanQuery, limit = 150)
        }
    }

    fun searchLiveChannels(playlistId: Long, query: String, categoryId: String?): Flow<List<StreamEntity>> {
        return queryStreamsFast(playlistId, "live", categoryId, query)
    }

    fun searchAllStreams(playlistId: Long, query: String, streamType: String?): Flow<List<StreamEntity>> {
        if (streamType != null) {
            return queryStreamsFast(playlistId, streamType, null, query)
        }
        return streamDao.searchAllStreams(playlistId, query.trim(), null)
    }

    fun getStreamsByCategory(playlistId: Long, type: String, categoryId: String): Flow<List<StreamEntity>> {
        return streamDao.getStreamsByCategoryFast(playlistId, type, categoryId, limit = 200)
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
            val updatedPlaylist = playlist.encryptedForStorage().copy(
                status = userInfo?.status ?: "Active",
                expDate = userInfo?.expDate ?: "",
                maxConnections = userInfo?.maxConnections ?: "1",
                lastSyncTimestamp = System.currentTimeMillis()
            )
            playlistDao.updatePlaylist(updatedPlaylist)

            // 2. Fetch categories (Live, VOD, Series)
            _syncProgress.value = SyncProgress(isSyncing = true, phase = "Syncing categories…")
            val liveCats = xtreamClient.fetchCategories(
                playlist.serverUrl, playlist.username, plainPassword,
                action = "get_live_categories", categoryType = "LIVE", playlistId = playlist.id
            )
            val vodCats = xtreamClient.fetchCategories(
                playlist.serverUrl, playlist.username, plainPassword,
                action = "get_vod_categories", categoryType = "MOVIE", playlistId = playlist.id
            )
            val seriesCats = xtreamClient.fetchCategories(
                playlist.serverUrl, playlist.username, plainPassword,
                action = "get_series_categories", categoryType = "SERIES", playlistId = playlist.id
            )

            val allCats = liveCats + vodCats + seriesCats
            if (allCats.isNotEmpty()) {
                categoryDao.insertCategories(allCats)
            }

            // Snapshot existing favorite streamIds before replace so user favorites survive re-sync
            val liveFavIds = streamDao.getFavoriteProviderIds(playlist.id, "live")
            val vodFavIds = streamDao.getFavoriteProviderIds(playlist.id, "movie")
            val seriesFavIds = streamDao.getFavoriteProviderIds(playlist.id, "series")

            // 3. Stream Live channels in bounded batches
            _syncProgress.value = SyncProgress(isSyncing = true, phase = "Syncing Live TV channels…")
            val liveCount = xtreamClient.streamCatalogEntries(
                serverUrl = playlist.serverUrl,
                username = playlist.username,
                password = plainPassword,
                action = "get_live_streams",
                streamType = "live",
                playlistId = playlist.id,
                batchSize = 500
            ) { batch, processedSoFar ->
                streamDao.insertStreamsBatch(batch)
                _syncProgress.value = SyncProgress(
                    isSyncing = true,
                    phase = "Ingesting Live TV ($processedSoFar channels)…",
                    itemsProcessed = processedSoFar
                )
            }
            if (liveFavIds.isNotEmpty()) {
                liveFavIds.chunked(400).forEach { chunk ->
                    streamDao.restoreFavorites(playlist.id, "live", chunk)
                }
            }

            // 4. Stream VOD movies in bounded batches
            _syncProgress.value = SyncProgress(isSyncing = true, phase = "Syncing Movies catalog…")
            val vodCount = xtreamClient.streamCatalogEntries(
                serverUrl = playlist.serverUrl,
                username = playlist.username,
                password = plainPassword,
                action = "get_vod_streams",
                streamType = "movie",
                playlistId = playlist.id,
                batchSize = 500
            ) { batch, processedSoFar ->
                streamDao.insertStreamsBatch(batch)
                _syncProgress.value = SyncProgress(
                    isSyncing = true,
                    phase = "Ingesting Movies ($processedSoFar titles)…",
                    itemsProcessed = processedSoFar
                )
            }
            if (vodFavIds.isNotEmpty()) {
                vodFavIds.chunked(400).forEach { chunk ->
                    streamDao.restoreFavorites(playlist.id, "movie", chunk)
                }
            }

            // 5. Stream Series in bounded batches
            _syncProgress.value = SyncProgress(isSyncing = true, phase = "Syncing Series catalog…")
            val seriesCount = xtreamClient.streamCatalogEntries(
                serverUrl = playlist.serverUrl,
                username = playlist.username,
                password = plainPassword,
                action = "get_series",
                streamType = "series",
                playlistId = playlist.id,
                batchSize = 500
            ) { batch, processedSoFar ->
                streamDao.insertStreamsBatch(batch)
                _syncProgress.value = SyncProgress(
                    isSyncing = true,
                    phase = "Ingesting Series ($processedSoFar shows)…",
                    itemsProcessed = processedSoFar
                )
            }
            if (seriesFavIds.isNotEmpty()) {
                seriesFavIds.chunked(400).forEach { chunk ->
                    streamDao.restoreFavorites(playlist.id, "series", chunk)
                }
            }

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
}
