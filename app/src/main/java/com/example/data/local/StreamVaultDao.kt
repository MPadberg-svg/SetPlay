package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.CategoryCountTuple
import com.example.data.model.CategoryEntity
import com.example.data.model.CategoryWithCount
import com.example.data.model.EpgProgramEntity
import com.example.data.model.FavoriteTeamEntity
import com.example.data.model.GroupedVodStream
import com.example.data.model.KickoffDaySnapshotEntity
import com.example.data.model.ParentalSettingsEntity
import com.example.data.model.PlaylistEntity
import com.example.data.model.StreamEntity
import com.example.data.model.SyncSectionStateEntity
import com.example.data.model.TournamentPreferenceEntity
import com.example.data.model.VodCatalogGroupEntity
import com.example.data.model.VodCollectionEntity
import com.example.data.model.VodGroupMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY isActive DESC, id DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE isActive = 1 LIMIT 1")
    fun getActivePlaylist(): Flow<PlaylistEntity?>

    @Query("SELECT * FROM playlists WHERE isActive = 1 LIMIT 1")
    suspend fun getActivePlaylistOnce(): PlaylistEntity?

    @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
    suspend fun getPlaylistById(id: Long): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("""
        UPDATE playlists 
        SET name = :name, serverUrl = :serverUrl, username = :username, password = :password 
        WHERE id = :id
    """)
    suspend fun updatePlaylistCredentials(
        id: Long,
        name: String,
        serverUrl: String,
        username: String,
        password: String
    )

    @Query("UPDATE playlists SET isActive = CASE WHEN id = :playlistId THEN 1 ELSE 0 END")
    suspend fun setActivePlaylist(playlistId: Long)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE playlistId = :playlistId AND type = :type ORDER BY categoryName ASC")
    fun getCategories(playlistId: Long, type: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE playlistId = :playlistId AND type = :type ORDER BY categoryName ASC")
    suspend fun getCategoriesOnce(playlistId: Long, type: String): List<CategoryEntity>

    @Query("""
        SELECT * FROM categories
        WHERE playlistId = :playlistId
          AND type = :type
          AND (:query = '' OR categoryName LIKE '%' || :query || '%')
        ORDER BY categoryName ASC
    """)
    fun searchCategoriesFast(
        playlistId: Long,
        type: String,
        query: String
    ): Flow<List<CategoryEntity>>

    @Query("""
        SELECT c.categoryId AS categoryId,
               c.categoryName AS categoryName,
               c.type AS type,
               c.isLocked AS isLocked,
               COUNT(s.id) AS streamCount
        FROM categories c
        LEFT JOIN streams s 
          ON s.playlistId = c.playlistId 
         AND s.streamType = CASE 
                WHEN c.type = 'LIVE' THEN 'live' 
                WHEN c.type = 'MOVIE' THEN 'movie' 
                ELSE 'series' 
             END 
         AND s.categoryId = c.categoryId
        WHERE c.playlistId = :playlistId 
          AND c.type = :type
          AND (:query = '' OR c.categoryName LIKE '%' || :query || '%')
        GROUP BY c.categoryId
        ORDER BY c.categoryName ASC
    """)
    fun searchCategoriesWithCounts(
        playlistId: Long,
        type: String,
        query: String
    ): Flow<List<CategoryWithCount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories WHERE playlistId = :playlistId")
    suspend fun clearCategoriesForPlaylist(playlistId: Long)

    @Query("DELETE FROM categories WHERE playlistId = :playlistId AND type = :type")
    suspend fun clearCategoriesByType(playlistId: Long, type: String)

    @Query("UPDATE categories SET isLocked = :isLocked WHERE playlistId = :playlistId AND categoryId = :categoryId")
    suspend fun setCategoryLocked(playlistId: Long, categoryId: String, isLocked: Boolean)

    @Query("""
        DELETE FROM categories
        WHERE UPPER(categoryName) LIKE '%+18%'
           OR UPPER(categoryName) LIKE '%18+%'
           OR UPPER(categoryName) LIKE '%ADULT%'
           OR UPPER(categoryName) LIKE '%XXX%'
           OR UPPER(categoryName) LIKE '%PORN%'
           OR UPPER(categoryName) LIKE '%EROTIC%'
           OR UPPER(categoryName) LIKE '%NSFW%'
           OR UPPER(categoryName) LIKE '%PLAYBOY%'
           OR UPPER(categoryName) LIKE '%BRAZZERS%'
           OR UPPER(categoryName) LIKE '%ONLYFANS%'
           OR UPPER(categoryName) LIKE '%HENTAI%'
           OR UPPER(categoryName) LIKE '%XVIDEOS%'
           OR UPPER(categoryName) LIKE '%XNXX%'
           OR categoryName LIKE '%للكبار%'
    """)
    suspend fun purgeAdultCategories(): Int

    @Query("DELETE FROM categories WHERE playlistId NOT IN (SELECT id FROM playlists)")
    suspend fun purgeOrphanCategories(): Int
}

data class StreamPositionSnapshot(
    val streamId: Long,
    val lastPositionMs: Long,
    val watchHistoryTimestamp: Long
)

data class StreamIndexProjection(
    val id: Long,
    val streamId: Long,
    val categoryId: String,
    val name: String,
    val streamIcon: String,
    val rating: String,
    val ratingNumeric: Double,
    val containerExtension: String,
    val tmdbId: String,
    val canonicalGroupKey: String,
    val versionTag: String,
    val addedEpoch: Long,
    val releaseYear: Int,
    val genres: String
)

data class StreamHashTuple(
    val streamId: Long,
    val metadataHash: Long
)

@Dao
interface StreamDao {
    @Query("""
        SELECT categoryId AS categoryId, COUNT(*) AS streamCount
        FROM streams
        WHERE playlistId = :playlistId AND streamType = :streamType
        GROUP BY categoryId
    """)
    fun getCategoryStreamCounts(playlistId: Long, streamType: String): Flow<List<CategoryCountTuple>>

    @Query("SELECT * FROM streams WHERE playlistId = :playlistId AND streamType = :streamType ORDER BY id ASC LIMIT :limit")
    fun getStreamsInitial(playlistId: Long, streamType: String, limit: Int = 180): Flow<List<StreamEntity>>

    @Query("SELECT * FROM streams WHERE playlistId = :playlistId AND streamType = :streamType AND categoryId = :categoryId ORDER BY id ASC LIMIT :limit")
    fun getStreamsByCategoryFast(playlistId: Long, streamType: String, categoryId: String, limit: Int = 180): Flow<List<StreamEntity>>

    @Query("""
        SELECT * FROM streams
        WHERE playlistId = :playlistId
          AND streamType = :streamType
          AND name LIKE '%' || :query || '%'
        ORDER BY id ASC
        LIMIT :limit
    """)
    fun searchStreamsByTypeFast(playlistId: Long, streamType: String, query: String, limit: Int = 180): Flow<List<StreamEntity>>

    @Query("""
        SELECT * FROM streams
        WHERE playlistId = :playlistId
          AND streamType = :streamType
          AND categoryId = :categoryId
          AND name LIKE '%' || :query || '%'
        ORDER BY id ASC
        LIMIT :limit
    """)
    fun searchStreamsInCategoryFast(
        playlistId: Long,
        streamType: String,
        categoryId: String,
        query: String,
        limit: Int = 180
    ): Flow<List<StreamEntity>>

    @Query("SELECT * FROM streams WHERE playlistId = :playlistId AND streamType = :type ORDER BY name ASC LIMIT :limit OFFSET :offset")
    fun getStreamsPaged(playlistId: Long, type: String, limit: Int, offset: Int): Flow<List<StreamEntity>>

    @Query("SELECT * FROM streams WHERE playlistId = :playlistId AND streamType = :type AND categoryId = :categoryId ORDER BY name ASC LIMIT :limit OFFSET :offset")
    fun getStreamsByCategoryPaged(playlistId: Long, type: String, categoryId: String, limit: Int, offset: Int): Flow<List<StreamEntity>>

    @Query("SELECT * FROM streams WHERE playlistId = :playlistId AND streamType = :type AND categoryId = :categoryId ORDER BY name ASC LIMIT 100")
    suspend fun getStreamsByCategoryOnce(playlistId: Long, type: String, categoryId: String): List<StreamEntity>

    // Category-filtered search for Live TV
    @Query("""
        SELECT * FROM streams 
        WHERE playlistId = :playlistId 
          AND streamType = 'live'
          AND (:categoryId IS NULL OR :categoryId = '' OR categoryId = :categoryId)
          AND name LIKE '%' || :query || '%'
        ORDER BY id ASC
        LIMIT :limit
    """)
    fun searchLiveChannels(playlistId: Long, query: String, categoryId: String?, limit: Int = 180): Flow<List<StreamEntity>>

    // General global search across Live, Movies, and Series
    @Query("""
        SELECT * FROM streams 
        WHERE playlistId = :playlistId 
          AND (:streamType IS NULL OR streamType = :streamType)
          AND name LIKE '%' || :query || '%'
        LIMIT 150
    """)
    fun searchAllStreams(playlistId: Long, query: String, streamType: String?): Flow<List<StreamEntity>>

    // Favorites
    @Query("SELECT * FROM streams WHERE playlistId = :playlistId AND isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteStreams(playlistId: Long): Flow<List<StreamEntity>>

    @Query("SELECT * FROM streams WHERE playlistId = :playlistId AND isFavorite = 1 AND streamType = :type ORDER BY name ASC")
    fun getFavoriteStreamsByType(playlistId: Long, type: String): Flow<List<StreamEntity>>

    @Query("SELECT * FROM streams WHERE playlistId = :playlistId AND isFavorite = 1 AND streamType = 'live' ORDER BY name ASC")
    suspend fun getFavoriteLiveStreamsOnce(playlistId: Long): List<StreamEntity>

    @Query("UPDATE streams SET isFavorite = :isFav WHERE id = :id")
    suspend fun toggleFavorite(id: Long, isFav: Boolean)

    // Watch history / Recently watched
    @Query("SELECT * FROM streams WHERE playlistId = :playlistId AND watchHistoryTimestamp > 0 ORDER BY watchHistoryTimestamp DESC LIMIT 20")
    fun getRecentlyWatched(playlistId: Long): Flow<List<StreamEntity>>

    @Query("UPDATE streams SET watchHistoryTimestamp = :timestamp WHERE id = :id")
    suspend fun updateWatchHistory(id: Long, timestamp: Long)

    @Query("SELECT streamId FROM streams WHERE playlistId = :playlistId AND streamType = :type AND isFavorite = 1")
    suspend fun getFavoriteProviderIds(playlistId: Long, type: String): List<Long>

    @Query("UPDATE streams SET isFavorite = 1 WHERE playlistId = :playlistId AND streamType = :type AND streamId IN (:streamIds)")
    suspend fun restoreFavorites(playlistId: Long, type: String, streamIds: List<Long>)

    @Query("SELECT streamId, lastPositionMs, watchHistoryTimestamp FROM streams WHERE playlistId = :playlistId AND streamType = :type AND (lastPositionMs > 0 OR watchHistoryTimestamp > 0)")
    suspend fun getSavedStreamPositions(playlistId: Long, type: String): List<StreamPositionSnapshot>

    @Query("UPDATE streams SET lastPositionMs = :lastPositionMs, watchHistoryTimestamp = :watchHistoryTimestamp WHERE playlistId = :playlistId AND streamType = :type AND streamId = :streamId")
    suspend fun restoreStreamPosition(playlistId: Long, type: String, streamId: Long, lastPositionMs: Long, watchHistoryTimestamp: Long)

    @Query("UPDATE streams SET lastPositionMs = :positionMs, watchHistoryTimestamp = :timestamp WHERE id = :id")
    suspend fun updatePlaybackPosition(id: Long, positionMs: Long, timestamp: Long)

    @Query("SELECT * FROM streams WHERE playlistId = :playlistId AND streamType = 'live' AND categoryId = :categoryId ORDER BY name ASC LIMIT 120")
    suspend fun getSiblingLiveChannels(playlistId: Long, categoryId: String): List<StreamEntity>

    @Query("SELECT * FROM streams WHERE id = :id LIMIT 1")
    suspend fun getStreamById(id: Long): StreamEntity?

    @Query("SELECT * FROM streams WHERE playlistId = :playlistId AND streamId = :streamId LIMIT 1")
    suspend fun getStreamByProviderId(playlistId: Long, streamId: Long): StreamEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreamsBatch(streams: List<StreamEntity>)

    @Query("DELETE FROM streams WHERE playlistId = :playlistId")
    suspend fun clearStreamsForPlaylist(playlistId: Long)

    @Query("DELETE FROM streams WHERE playlistId = :playlistId AND streamType = :streamType")
    suspend fun deleteStreamsByType(playlistId: Long, streamType: String)

    @Query("""
        DELETE FROM streams
        WHERE categoryId != ''
          AND EXISTS (
              SELECT 1 FROM categories c
              WHERE c.playlistId = streams.playlistId
          )
          AND NOT EXISTS (
              SELECT 1 FROM categories c
              WHERE c.playlistId = streams.playlistId
                AND c.categoryId = streams.categoryId
                AND c.type = CASE
                    WHEN streams.streamType = 'live' THEN 'LIVE'
                    WHEN streams.streamType = 'movie' THEN 'MOVIE'
                    ELSE 'SERIES'
                END
          )
    """)
    suspend fun purgeStreamsInDeletedCategories(): Int

    @Query("""
        DELETE FROM streams
        WHERE UPPER(name) LIKE '%+18%'
           OR UPPER(name) LIKE '%18+%'
           OR UPPER(name) LIKE '%ADULT%'
           OR UPPER(name) LIKE '%XXX%'
           OR UPPER(name) LIKE '%PORN%'
           OR UPPER(name) LIKE '%EROTIC%'
           OR UPPER(name) LIKE '%NSFW%'
           OR UPPER(name) LIKE '%PLAYBOY%'
           OR UPPER(name) LIKE '%BRAZZERS%'
           OR UPPER(name) LIKE '%ONLYFANS%'
           OR UPPER(name) LIKE '%HENTAI%'
           OR UPPER(name) LIKE '%XVIDEOS%'
           OR UPPER(name) LIKE '%XNXX%'
           OR name LIKE '%للكبار%'
    """)
    suspend fun purgeAdultStreamsByName(): Int

    @Query("SELECT COUNT(*) FROM streams WHERE playlistId = :playlistId AND streamType = :type")
    suspend fun countStreams(playlistId: Long, type: String): Int

    @Query("SELECT COALESCE((SELECT id FROM playlists WHERE isActive = 1 LIMIT 1), (SELECT id FROM playlists ORDER BY id DESC LIMIT 1), 0)")
    suspend fun getActiveOrLatestPlaylistIdOnce(): Long

    @Query("""
        SELECT * FROM streams
        WHERE (:playlistId <= 0 OR playlistId = :playlistId)
          AND streamType = 'live'
          AND name LIKE '%' || :keyword || '%'
        ORDER BY isFavorite DESC, name ASC
        LIMIT 200
    """)
    suspend fun searchLiveStreamsByKeyword(playlistId: Long, keyword: String): List<StreamEntity>

    @Query("SELECT * FROM streams WHERE playlistId = :playlistId AND streamType = 'series' AND categoryId = :categoryId AND streamId > :currentStreamId ORDER BY streamId ASC LIMIT 1")
    suspend fun getNextSeriesStreamFallback(playlistId: Long, categoryId: String, currentStreamId: Long): StreamEntity?

    @Query("UPDATE streams SET watchHistoryTimestamp = 0, lastPositionMs = 0 WHERE watchHistoryTimestamp > 0 AND watchHistoryTimestamp < :sixtyDaysAgoMs")
    suspend fun clearExpiredWatchHistory(sixtyDaysAgoMs: Long): Int

    @Query("DELETE FROM streams WHERE playlistId NOT IN (SELECT id FROM playlists)")
    suspend fun purgeOrphanStreams(): Int

    // --- Movies & Series SQL-Level Deduplication & Discovery Queries ---

    @Query("""
        SELECT *, COUNT(*) AS versionCount
        FROM streams
        WHERE playlistId = :playlistId
          AND streamType = :streamType
        GROUP BY CASE WHEN canonicalGroupKey != '' THEN canonicalGroupKey ELSE CAST(id AS TEXT) END
        ORDER BY MAX(addedEpoch) DESC, MAX(ratingNumeric) DESC, id ASC
        LIMIT :limit
    """)
    fun getDeduplicatedStreams(
        playlistId: Long,
        streamType: String,
        limit: Int = 180
    ): Flow<List<GroupedVodStream>>

    @Query("""
        SELECT *, COUNT(*) AS versionCount
        FROM streams
        WHERE playlistId = :playlistId
          AND streamType = :streamType
          AND categoryId = :categoryId
        GROUP BY CASE WHEN canonicalGroupKey != '' THEN canonicalGroupKey ELSE CAST(id AS TEXT) END
        ORDER BY MAX(addedEpoch) DESC, MAX(ratingNumeric) DESC, id ASC
        LIMIT :limit
    """)
    fun getDeduplicatedStreamsByCategory(
        playlistId: Long,
        streamType: String,
        categoryId: String,
        limit: Int = 180
    ): Flow<List<GroupedVodStream>>

    @Query("""
        SELECT *, COUNT(*) AS versionCount
        FROM streams
        WHERE playlistId = :playlistId
          AND streamType = :streamType
          AND (:categoryId IS NULL OR :categoryId = '' OR categoryId = :categoryId)
          AND name LIKE '%' || :query || '%'
        GROUP BY CASE WHEN canonicalGroupKey != '' THEN canonicalGroupKey ELSE CAST(id AS TEXT) END
        ORDER BY MAX(ratingNumeric) DESC, MAX(addedEpoch) DESC, id ASC
        LIMIT :limit
    """)
    fun searchDeduplicatedStreams(
        playlistId: Long,
        streamType: String,
        categoryId: String?,
        query: String,
        limit: Int = 180
    ): Flow<List<GroupedVodStream>>

    @Query("""
        SELECT *
        FROM streams
        WHERE playlistId = :playlistId
          AND streamType = :streamType
          AND canonicalGroupKey = :canonicalGroupKey
        ORDER BY ratingNumeric DESC, id ASC
    """)
    suspend fun getVersionsByGroupKey(
        playlistId: Long,
        streamType: String,
        canonicalGroupKey: String
    ): List<StreamEntity>

    @Query("""
        SELECT *
        FROM streams
        WHERE playlistId = :playlistId
          AND streamType = :streamType
          AND tmdbId = :tmdbId
          AND tmdbId != ''
        ORDER BY ratingNumeric DESC, id ASC
        LIMIT 50
    """)
    suspend fun getVersionsByTmdbId(
        playlistId: Long,
        streamType: String,
        tmdbId: String
    ): List<StreamEntity>

    @Query("""
        SELECT *
        FROM streams
        WHERE playlistId = :playlistId
          AND streamType = :streamType
          AND name LIKE '%' || :titleQuery || '%'
        ORDER BY ratingNumeric DESC, id ASC
        LIMIT 80
    """)
    suspend fun getVersionsByTitleLike(
        playlistId: Long,
        streamType: String,
        titleQuery: String
    ): List<StreamEntity>

    @Query("""
        SELECT *, COUNT(*) AS versionCount
        FROM streams
        WHERE playlistId = :playlistId
          AND streamType = :streamType
        GROUP BY CASE WHEN canonicalGroupKey != '' THEN canonicalGroupKey ELSE CAST(id AS TEXT) END
        ORDER BY MAX(addedEpoch) DESC, MAX(ratingNumeric) DESC, id ASC
        LIMIT :limit
    """)
    fun getRecentlyAddedDeduplicated(
        playlistId: Long,
        streamType: String,
        limit: Int = 20
    ): Flow<List<GroupedVodStream>>

    @Query("""
        SELECT *, COUNT(*) AS versionCount
        FROM streams
        WHERE playlistId = :playlistId
          AND streamType = :streamType
          AND ratingNumeric >= 6.5
          AND streamIcon != ''
        GROUP BY CASE WHEN canonicalGroupKey != '' THEN canonicalGroupKey ELSE CAST(id AS TEXT) END
        ORDER BY MAX(ratingNumeric) DESC, MAX(addedEpoch) DESC, id ASC
        LIMIT :limit
    """)
    fun getTopRatedDeduplicated(
        playlistId: Long,
        streamType: String,
        limit: Int = 20
    ): Flow<List<GroupedVodStream>>

    @Query("""
        SELECT *, COUNT(*) AS versionCount
        FROM streams
        WHERE playlistId = :playlistId
          AND streamType = :streamType
          AND (
              genres LIKE '%' || :genreKeyword || '%'
              OR categoryId IN (
                  SELECT categoryId FROM categories
                  WHERE playlistId = :playlistId
                    AND categoryName LIKE '%' || :genreKeyword || '%'
              )
          )
        GROUP BY CASE WHEN canonicalGroupKey != '' THEN canonicalGroupKey ELSE CAST(id AS TEXT) END
        ORDER BY MAX(ratingNumeric) DESC, MAX(addedEpoch) DESC, id ASC
        LIMIT :limit
    """)
    fun getGenreShelfDeduplicated(
        playlistId: Long,
        streamType: String,
        genreKeyword: String,
        limit: Int = 20
    ): Flow<List<GroupedVodStream>>

    @Query("""
        SELECT *, COUNT(*) AS versionCount
        FROM streams
        WHERE playlistId = :playlistId
          AND streamType = :streamType
          AND (:excludeGroupKey = '' OR canonicalGroupKey != :excludeGroupKey)
          AND (
              genres LIKE '%' || :genreKeyword || '%'
              OR categoryId IN (
                  SELECT categoryId FROM categories
                  WHERE playlistId = :playlistId
                    AND categoryName LIKE '%' || :genreKeyword || '%'
              )
          )
        GROUP BY CASE WHEN canonicalGroupKey != '' THEN canonicalGroupKey ELSE CAST(id AS TEXT) END
        ORDER BY MAX(ratingNumeric) DESC, MAX(addedEpoch) DESC, id ASC
        LIMIT :limit
    """)
    fun getSimilarTitlesDeduplicated(
        playlistId: Long,
        streamType: String,
        genreKeyword: String,
        excludeGroupKey: String,
        limit: Int = 15
    ): Flow<List<GroupedVodStream>>

    @Query("""
        SELECT *, COUNT(*) AS versionCount
        FROM streams
        WHERE playlistId = :playlistId
          AND streamType = :streamType
          AND (
              categoryId IN (:categoryIds)
              OR (:genreKeyword != '' AND genres LIKE '%' || :genreKeyword || '%')
          )
        GROUP BY CASE WHEN canonicalGroupKey != '' THEN canonicalGroupKey ELSE CAST(id AS TEXT) END
        HAVING MAX(watchHistoryTimestamp) <= 0
        ORDER BY MAX(ratingNumeric) DESC, MAX(addedEpoch) DESC, id ASC
        LIMIT :limit
    """)
    fun getRecommendedByCategoriesOrGenres(
        playlistId: Long,
        streamType: String,
        categoryIds: List<String>,
        genreKeyword: String,
        limit: Int = 20
    ): Flow<List<GroupedVodStream>>

    // Chunked streaming helpers for low-RAM read-only background materialization into vod_catalog_groups
    @Query("SELECT * FROM streams WHERE playlistId = :playlistId AND streamType = :streamType ORDER BY id ASC LIMIT :limit OFFSET :offset")
    suspend fun getStreamsChunkByType(
        playlistId: Long,
        streamType: String,
        limit: Int,
        offset: Int
    ): List<StreamEntity>

    @Query("SELECT id, streamId, categoryId, name, streamIcon, rating, ratingNumeric, containerExtension, tmdbId, canonicalGroupKey, versionTag, addedEpoch, releaseYear, genres FROM streams WHERE playlistId = :playlistId AND streamType = :streamType ORDER BY id ASC LIMIT :limit OFFSET :offset")
    suspend fun getStreamIndexProjectionsChunk(
        playlistId: Long,
        streamType: String,
        limit: Int,
        offset: Int
    ): List<StreamIndexProjection>

    @Query("SELECT id, streamId, categoryId, name, streamIcon, rating, ratingNumeric, containerExtension, tmdbId, canonicalGroupKey, versionTag, addedEpoch, releaseYear, genres FROM streams WHERE playlistId = :playlistId AND streamType = :streamType AND id > :lastId ORDER BY id ASC LIMIT :limit")
    suspend fun getStreamIndexProjectionsAfterId(
        playlistId: Long,
        streamType: String,
        lastId: Long,
        limit: Int
    ): List<StreamIndexProjection>

    @Query("SELECT * FROM streams WHERE id = :id LIMIT 1")
    suspend fun getStreamByDbId(id: Long): StreamEntity?

    // Phase 5A diff-sync & partial-reindex DAO queries
    @Query("""
        SELECT streamId, metadataHash
        FROM streams
        WHERE playlistId = :playlistId
          AND streamType = :streamType
    """)
    suspend fun getStreamHashesByType(
        playlistId: Long,
        streamType: String
    ): List<StreamHashTuple>

    @Query("""
        SELECT streamId, metadataHash
        FROM streams
        WHERE playlistId = :playlistId
          AND streamType = :streamType
          AND categoryId = :categoryId
    """)
    suspend fun getStreamHashesByCategory(
        playlistId: Long,
        streamType: String,
        categoryId: String
    ): List<StreamHashTuple>

    @Query("""
        UPDATE streams
        SET categoryId = :categoryId,
            name = :name,
            streamIcon = :streamIcon,
            epgChannelId = :epgChannelId,
            rating = :rating,
            tvArchive = :tvArchive,
            tvArchiveDuration = :tvArchiveDuration,
            containerExtension = :containerExtension,
            tmdbId = :tmdbId,
            canonicalGroupKey = :canonicalGroupKey,
            versionTag = :versionTag,
            addedEpoch = :addedEpoch,
            ratingNumeric = :ratingNumeric,
            releaseYear = :releaseYear,
            genres = :genres,
            metadataHash = :metadataHash
        WHERE playlistId = :playlistId
          AND streamType = :streamType
          AND streamId = :streamId
    """)
    suspend fun updateStreamMetadataByProviderId(
        playlistId: Long,
        streamType: String,
        streamId: Long,
        categoryId: String,
        name: String,
        streamIcon: String,
        epgChannelId: String,
        rating: String,
        tvArchive: Int,
        tvArchiveDuration: Int,
        containerExtension: String,
        tmdbId: String,
        canonicalGroupKey: String,
        versionTag: String,
        addedEpoch: Long,
        ratingNumeric: Double,
        releaseYear: Int,
        genres: String,
        metadataHash: Long
    )

    @Query("""
        DELETE FROM streams
        WHERE playlistId = :playlistId
          AND streamType = :streamType
          AND streamId IN (:streamIds)
    """)
    suspend fun deleteStreamsByProviderIds(
        playlistId: Long,
        streamType: String,
        streamIds: List<Long>
    )

    @Query("""
        SELECT id, streamId, categoryId, name, streamIcon, rating, ratingNumeric, containerExtension, tmdbId, canonicalGroupKey, versionTag, addedEpoch, releaseYear, genres
        FROM streams
        WHERE playlistId = :playlistId
          AND streamType = :streamType
          AND canonicalGroupKey IN (:groupKeys)
        ORDER BY id ASC
    """)
    suspend fun getStreamsByCanonicalKeys(
        playlistId: Long,
        streamType: String,
        groupKeys: List<String>
    ): List<StreamIndexProjection>
}

@Dao
interface VodCatalogGroupDao {
    @Query("DELETE FROM vod_catalog_groups WHERE playlistId = :playlistId AND streamType = :streamType")
    suspend fun deleteGroupsByType(playlistId: Long, streamType: String)

    @Query("""
        DELETE FROM vod_catalog_groups
        WHERE playlistId = :playlistId
          AND streamType = :streamType
          AND groupKey IN (:groupKeys)
    """)
    suspend fun deleteGroupsByKeys(
        playlistId: Long,
        streamType: String,
        groupKeys: List<String>
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupsBatch(groups: List<VodCatalogGroupEntity>)

    @Query("DELETE FROM vod_group_members WHERE playlistId = :playlistId AND streamType = :streamType")
    suspend fun deleteGroupMembersByType(playlistId: Long, streamType: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMembersBatch(members: List<VodGroupMemberEntity>)

    @Query("""
        DELETE FROM vod_group_members
        WHERE playlistId = :playlistId
          AND streamType = :streamType
          AND groupKey IN (:groupKeys)
    """)
    suspend fun deleteGroupMembersByKeys(
        playlistId: Long,
        streamType: String,
        groupKeys: List<String>
    )

    @Query("""
        SELECT s.*
        FROM streams s
        INNER JOIN vod_group_members m ON s.id = m.streamDbId
        WHERE m.playlistId = :playlistId
          AND m.streamType = :streamType
          AND m.groupKey = :groupKey
        ORDER BY s.id ASC
    """)
    suspend fun getMemberStreamsForGroup(
        playlistId: Long,
        streamType: String,
        groupKey: String
    ): List<StreamEntity>

    @Query("SELECT COUNT(*) FROM vod_catalog_groups WHERE playlistId = :playlistId AND streamType = :streamType")
    suspend fun countGroupsByType(playlistId: Long, streamType: String): Int

    @Query("""
        SELECT *
        FROM vod_catalog_groups
        WHERE playlistId = :playlistId
          AND streamType = :streamType
          AND groupKey = :groupKey
        LIMIT 1
    """)
    suspend fun getGroupByKey(
        playlistId: Long,
        streamType: String,
        groupKey: String
    ): VodCatalogGroupEntity?

    @Query("""
        SELECT *
        FROM vod_catalog_groups
        WHERE playlistId = :playlistId
          AND streamType = :streamType
        ORDER BY popularityScore DESC, addedEpoch DESC
        LIMIT :limit
    """)
    fun getTopPopularGroups(
        playlistId: Long,
        streamType: String,
        limit: Int = 20
    ): Flow<List<VodCatalogGroupEntity>>

    @Query("""
        SELECT *
        FROM (
            SELECT *
            FROM vod_catalog_groups
            WHERE playlistId = :playlistId
              AND streamType = :streamType
            ORDER BY addedEpoch DESC
            LIMIT (:limit * 4)
        )
        ORDER BY (addedEpoch / 86400) DESC,
                 CASE
                     WHEN UPPER(defaultVersionTag) LIKE '%ENGLISH%'
                       OR UPPER(defaultVersionTag) LIKE '%ARABIC%'
                       OR UPPER(defaultVersionTag) LIKE '%FRENCH%'
                       OR UPPER(defaultVersionTag) LIKE '%VOSTFR%'
                       OR UPPER(defaultVersionTag) LIKE '%MULTI%'
                     THEN 1
                     ELSE 0
                 END DESC,
                 popularityScore DESC,
                 addedEpoch DESC
        LIMIT :limit
    """)
    fun getRecentlyAddedGroups(
        playlistId: Long,
        streamType: String,
        limit: Int = 20
    ): Flow<List<VodCatalogGroupEntity>>

    @Query("""
        SELECT *
        FROM vod_catalog_groups
        WHERE playlistId = :playlistId
          AND streamType = :streamType
          AND (
              genres LIKE '%' || :genreKeyword || '%'
              OR primaryCategoryId IN (
                  SELECT categoryId FROM categories
                  WHERE playlistId = :playlistId
                    AND categoryName LIKE '%' || :genreKeyword || '%'
              )
          )
        ORDER BY popularityScore DESC, addedEpoch DESC
        LIMIT :limit
    """)
    fun getGenreShelfGroups(
        playlistId: Long,
        streamType: String,
        genreKeyword: String,
        limit: Int = 20
    ): Flow<List<VodCatalogGroupEntity>>

    @Query("""
        SELECT *
        FROM vod_catalog_groups
        WHERE playlistId = :playlistId
          AND streamType = :streamType
          AND (
              primaryCategoryId = :categoryId
              OR categoryIdsCsv LIKE '%,' || :categoryId || ',%'
          )
        ORDER BY popularityScore DESC, addedEpoch DESC
        LIMIT :limit
    """)
    fun getGroupsByCategory(
        playlistId: Long,
        streamType: String,
        categoryId: String,
        limit: Int = 180
    ): Flow<List<VodCatalogGroupEntity>>

    @Query("""
        SELECT *
        FROM vod_catalog_groups
        WHERE playlistId = :playlistId
          AND streamType = :streamType
        ORDER BY popularityScore DESC, addedEpoch DESC
        LIMIT :limit
    """)
    fun getAllGroupsPaged(
        playlistId: Long,
        streamType: String,
        limit: Int = 180
    ): Flow<List<VodCatalogGroupEntity>>

    @Query("""
        SELECT *
        FROM vod_catalog_groups
        WHERE playlistId = :playlistId
          AND streamType = :streamType
          AND (
              :categoryId IS NULL
              OR :categoryId = ''
              OR primaryCategoryId = :categoryId
              OR categoryIdsCsv LIKE '%,' || :categoryId || ',%'
          )
          AND cleanTitle LIKE '%' || :query || '%'
        ORDER BY popularityScore DESC, addedEpoch DESC
        LIMIT :limit
    """)
    fun searchGroups(
        playlistId: Long,
        streamType: String,
        categoryId: String?,
        query: String,
        limit: Int = 180
    ): Flow<List<VodCatalogGroupEntity>>

    @Query("""
        SELECT g.*
        FROM vod_catalog_groups g
        JOIN vod_catalog_groups_fts ON g.id = vod_catalog_groups_fts.docid
        WHERE vod_catalog_groups_fts MATCH :ftsQuery
          AND g.playlistId = :playlistId
          AND g.streamType = :streamType
          AND (
              :categoryId IS NULL
              OR :categoryId = ''
              OR g.primaryCategoryId = :categoryId
              OR g.categoryIdsCsv LIKE '%,' || :categoryId || ',%'
          )
        ORDER BY g.popularityScore DESC, g.addedEpoch DESC
        LIMIT :limit
    """)
    fun searchGroupsFts(
        playlistId: Long,
        streamType: String,
        categoryId: String?,
        ftsQuery: String,
        limit: Int = 180
    ): Flow<List<VodCatalogGroupEntity>>

    @Query("""
        SELECT *
        FROM vod_catalog_groups
        WHERE playlistId = :playlistId
          AND streamType = :streamType
          AND platformTags LIKE '%,' || UPPER(:platformKeyword) || ',%'
        ORDER BY popularityScore DESC, addedEpoch DESC
        LIMIT :limit
    """)
    fun getPlatformShelfGroups(
        playlistId: Long,
        streamType: String,
        platformKeyword: String,
        limit: Int = 24
    ): Flow<List<VodCatalogGroupEntity>>

    @Query("""
        SELECT *
        FROM vod_catalog_groups
        WHERE playlistId = :playlistId
          AND streamType = :streamType
          AND (:excludeGroupKey = '' OR groupKey != :excludeGroupKey)
          AND (
              genres LIKE '%' || :genreKeyword || '%'
              OR primaryCategoryId IN (
                  SELECT categoryId FROM categories
                  WHERE playlistId = :playlistId
                    AND categoryName LIKE '%' || :genreKeyword || '%'
              )
          )
        ORDER BY popularityScore DESC, addedEpoch DESC
        LIMIT :limit
    """)
    suspend fun getSimilarGroupsByGenre(
        playlistId: Long,
        streamType: String,
        genreKeyword: String,
        excludeGroupKey: String,
        limit: Int = 15
    ): List<VodCatalogGroupEntity>

    @Query("""
        SELECT *
        FROM vod_catalog_groups
        WHERE playlistId = :playlistId
          AND streamType = :streamType
          AND representativeStreamId NOT IN (:excludeStreamIds)
          AND (
              genres LIKE '%' || :genreKeyword || '%'
              OR primaryCategoryId IN (
                  SELECT categoryId FROM categories
                  WHERE playlistId = :playlistId
                    AND categoryName LIKE '%' || :genreKeyword || '%'
              )
          )
        ORDER BY popularityScore DESC, addedEpoch DESC
        LIMIT :limit
    """)
    suspend fun getRecommendedGroupsBySingleGenre(
        playlistId: Long,
        streamType: String,
        genreKeyword: String,
        excludeStreamIds: List<Long>,
        limit: Int = 20
    ): List<VodCatalogGroupEntity>

    suspend fun getRecommendedGroupsByGenre(
        playlistId: Long,
        streamType: String,
        genreKeywords: List<String>,
        excludeStreamIds: List<Long>,
        limit: Int = 20
    ): List<VodCatalogGroupEntity> {
        val safeExclude = excludeStreamIds.ifEmpty { listOf(-1L) }
        val cleanKeywords = genreKeywords.map { it.trim() }.filter { it.isNotEmpty() }.distinct().take(4)
        if (cleanKeywords.isEmpty()) return emptyList()
        val merged = LinkedHashMap<String, VodCatalogGroupEntity>()
        for (keyword in cleanKeywords) {
            val rows = getRecommendedGroupsBySingleGenre(
                playlistId = playlistId,
                streamType = streamType,
                genreKeyword = keyword,
                excludeStreamIds = safeExclude,
                limit = limit
            )
            for (row in rows) {
                if (!merged.containsKey(row.groupKey)) {
                    merged[row.groupKey] = row
                }
            }
        }
        return merged.values
            .sortedWith(
                compareByDescending<VodCatalogGroupEntity> { it.popularityScore }
                    .thenByDescending { it.addedEpoch }
            )
            .take(limit)
    }

    @Query("DELETE FROM vod_collections WHERE playlistId = :playlistId")
    suspend fun deleteCollectionsForPlaylist(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollectionsBatch(collections: List<VodCollectionEntity>)

    @Query("""
        SELECT *
        FROM vod_collections
        WHERE playlistId = :playlistId
        ORDER BY popularityScore DESC, movieCount DESC, name ASC
        LIMIT :limit
    """)
    fun observeCollections(
        playlistId: Long,
        limit: Int = 40
    ): Flow<List<VodCollectionEntity>>

    @Query("""
        SELECT *
        FROM vod_catalog_groups
        WHERE playlistId = :playlistId
          AND streamType = 'movie'
          AND collectionId = :collectionId
        ORDER BY collectionOrder ASC, releaseYear ASC, cleanTitle ASC
    """)
    fun getGroupsByCollection(
        playlistId: Long,
        collectionId: String
    ): Flow<List<VodCatalogGroupEntity>>
}

@Dao
interface EpgDao {
    @Query("SELECT * FROM epg_programs WHERE playlistId = :playlistId AND streamId = :streamId AND endEpoch >= :now ORDER BY startEpoch ASC LIMIT 10")
    fun getUpcomingPrograms(playlistId: Long, streamId: Long, now: Long): Flow<List<EpgProgramEntity>>

    @Query("SELECT * FROM epg_programs WHERE playlistId = :playlistId AND streamId = :streamId AND :now BETWEEN startEpoch AND endEpoch LIMIT 1")
    suspend fun getCurrentProgram(playlistId: Long, streamId: Long, now: Long): EpgProgramEntity?

    @Query("SELECT * FROM epg_programs WHERE playlistId = :playlistId AND streamId = :streamId AND endEpoch < :now ORDER BY startEpoch DESC LIMIT 20")
    suspend fun getCatchupPrograms(playlistId: Long, streamId: Long, now: Long): List<EpgProgramEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpgPrograms(programs: List<EpgProgramEntity>)

    @Query("DELETE FROM epg_programs WHERE playlistId = :playlistId")
    suspend fun clearEpgForPlaylist(playlistId: Long)

    @Query("DELETE FROM epg_programs WHERE endEpoch < :nowEpochSec")
    suspend fun purgeExpiredEpgPrograms(nowEpochSec: Long): Int
}

@Dao
interface ParentalSettingsDao {
    @Query("SELECT * FROM parental_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<ParentalSettingsEntity?>

    @Query("SELECT * FROM parental_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsOnce(): ParentalSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: ParentalSettingsEntity)
}

@Dao
interface FavoriteTeamDao {
    @Query("SELECT * FROM favorite_teams ORDER BY addedTimestamp ASC")
    fun getAllFavoriteTeams(): Flow<List<FavoriteTeamEntity>>

    @Query("SELECT * FROM favorite_teams ORDER BY addedTimestamp ASC")
    suspend fun getAllFavoriteTeamsOnce(): List<FavoriteTeamEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteTeam(team: FavoriteTeamEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteTeams(teams: List<FavoriteTeamEntity>)

    @Query("DELETE FROM favorite_teams WHERE teamId = :teamId")
    suspend fun removeFavoriteTeam(teamId: String)

    @Query("SELECT COUNT(*) FROM favorite_teams")
    suspend fun countFavoriteTeams(): Int
}

@Dao
interface TournamentPreferenceDao {
    @Query("SELECT * FROM tournament_preferences ORDER BY sortOrder ASC, tournamentName ASC")
    fun getAllTournamentPreferences(): Flow<List<TournamentPreferenceEntity>>

    @Query("SELECT * FROM tournament_preferences ORDER BY sortOrder ASC, tournamentName ASC")
    suspend fun getAllTournamentPreferencesOnce(): List<TournamentPreferenceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPreference(preference: TournamentPreferenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPreferences(preferences: List<TournamentPreferenceEntity>)
}

@Dao
interface SyncSectionStateDao {
    @Query("SELECT * FROM sync_section_state WHERE playlistId = :playlistId ORDER BY sectionType ASC")
    fun observeAllForPlaylist(playlistId: Long): Flow<List<SyncSectionStateEntity>>

    @Query("SELECT * FROM sync_section_state WHERE playlistId = :playlistId AND sectionType = :sectionType LIMIT 1")
    fun observeSection(playlistId: Long, sectionType: String): Flow<SyncSectionStateEntity?>

    @Query("SELECT * FROM sync_section_state WHERE playlistId = :playlistId AND sectionType = :sectionType LIMIT 1")
    suspend fun getSectionOnce(playlistId: Long, sectionType: String): SyncSectionStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: SyncSectionStateEntity)

    @Query("""
        UPDATE sync_section_state
        SET status = 'INTERRUPTED',
            stage = 'INTERRUPTED',
            ownerWorkerId = '',
            heartbeatMs = 0,
            lastAttemptedSyncMs = :nowMs
        WHERE status IN ('RUNNING', 'PENDING')
    """)
    suspend fun markAllRunningAsInterrupted(nowMs: Long)

    @Query("""
        SELECT * FROM sync_section_state
        WHERE status = 'PENDING'
        ORDER BY queuePriority ASC, queueSequence ASC
        LIMIT 1
    """)
    suspend fun getNextPendingTask(): SyncSectionStateEntity?

    @Query("SELECT * FROM sync_section_state WHERE status = 'RUNNING'")
    suspend fun getRunningTasks(): List<SyncSectionStateEntity>

    @Query("""
        SELECT * FROM sync_section_state
        WHERE playlistId = :playlistId
          AND status = 'PENDING'
        ORDER BY queuePriority ASC, queueSequence ASC
    """)
    suspend fun getPendingTasksForPlaylist(playlistId: Long): List<SyncSectionStateEntity>

    @Query("SELECT COUNT(*) FROM sync_section_state WHERE status IN ('RUNNING', 'PENDING')")
    suspend fun countActiveOrPending(): Int

    @Query("SELECT COUNT(*) FROM sync_section_state WHERE status = 'PENDING'")
    suspend fun countPending(): Int

    @Query("""
        UPDATE sync_section_state
        SET heartbeatMs = :heartbeatMs
        WHERE playlistId = :playlistId
          AND sectionType = :sectionType
          AND status = 'RUNNING'
          AND ownerWorkerId = :ownerWorkerId
    """)
    suspend fun updateHeartbeat(
        playlistId: Long,
        sectionType: String,
        ownerWorkerId: String,
        heartbeatMs: Long
    )

    @Query("""
        UPDATE sync_section_state
        SET status = 'INTERRUPTED',
            ownerWorkerId = '',
            heartbeatMs = 0,
            lastAttemptedSyncMs = :nowMs,
            errorMessage = 'Interrupted (stale worker lease recovered)'
        WHERE status = 'RUNNING'
          AND heartbeatMs > 0
          AND heartbeatMs < :staleBeforeMs
    """)
    suspend fun recoverStaleRunningTasks(staleBeforeMs: Long, nowMs: Long): Int

    @Query("""
        UPDATE sync_section_state
        SET status = 'INTERRUPTED',
            ownerWorkerId = '',
            heartbeatMs = 0,
            lastAttemptedSyncMs = :nowMs,
            errorMessage = 'Interrupted (process restarted)'
        WHERE status = 'RUNNING'
          AND (heartbeatMs = 0 OR heartbeatMs < :staleBeforeMs)
    """)
    suspend fun recoverOrphanedRunningOnStartup(staleBeforeMs: Long, nowMs: Long): Int

    @Query("""
        UPDATE sync_section_state
        SET status = 'IDLE',
            stage = 'IDLE',
            ownerWorkerId = '',
            heartbeatMs = 0,
            queuePriority = 99,
            requestedCategoryId = '',
            forceFullReconcile = 0,
            pendingUpdateSummary = ''
        WHERE playlistId = :playlistId
          AND status IN ('PENDING', 'RUNNING')
    """)
    suspend fun cancelAllForPlaylist(playlistId: Long)

    @Query("DELETE FROM sync_section_state WHERE status IN ('PENDING', 'RUNNING')")
    suspend fun clearPendingAndRunningForTest()
}

@Dao
interface KickoffDaySnapshotDao {
    @Query("SELECT * FROM kickoff_day_snapshots WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getSnapshotOnce(dateKey: String): KickoffDaySnapshotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSnapshot(snapshot: KickoffDaySnapshotEntity)

    @Query("DELETE FROM kickoff_day_snapshots WHERE fetchedAtEpochMs < :cutoffEpochMs")
    suspend fun pruneOlderThan(cutoffEpochMs: Long)
}


