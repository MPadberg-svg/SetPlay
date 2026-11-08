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
import com.example.data.model.ParentalSettingsEntity
import com.example.data.model.PlaylistEntity
import com.example.data.model.StreamEntity
import com.example.data.model.TournamentPreferenceEntity
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

