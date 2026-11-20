package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CategoryEntity
import com.example.data.model.EpgProgramEntity
import com.example.data.model.FavoriteTeamEntity
import com.example.data.model.ParentalSettingsEntity
import com.example.data.model.PlaylistEntity
import com.example.data.model.StreamEntity
import com.example.data.model.TournamentPreferenceEntity
import com.example.data.model.VodCatalogGroupEntity

@Database(
    entities = [
        PlaylistEntity::class,
        CategoryEntity::class,
        StreamEntity::class,
        EpgProgramEntity::class,
        ParentalSettingsEntity::class,
        FavoriteTeamEntity::class,
        TournamentPreferenceEntity::class,
        VodCatalogGroupEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class StreamVaultDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun categoryDao(): CategoryDao
    abstract fun streamDao(): StreamDao
    abstract fun epgDao(): EpgDao
    abstract fun parentalSettingsDao(): ParentalSettingsDao
    abstract fun favoriteTeamDao(): FavoriteTeamDao
    abstract fun tournamentPreferenceDao(): TournamentPreferenceDao
    abstract fun vodGroupDao(): VodCatalogGroupDao

    companion object {
        @Volatile
        private var INSTANCE: StreamVaultDatabase? = null

        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE streams ADD COLUMN tmdbId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE streams ADD COLUMN canonicalGroupKey TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE streams ADD COLUMN versionTag TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE streams ADD COLUMN addedEpoch INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE streams ADD COLUMN ratingNumeric REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE streams ADD COLUMN releaseYear INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE streams ADD COLUMN genres TEXT NOT NULL DEFAULT ''")

                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_streams_playlistId_streamType_canonicalGroupKey` ON `streams` (`playlistId`, `streamType`, `canonicalGroupKey`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_streams_playlistId_streamType_addedEpoch` ON `streams` (`playlistId`, `streamType`, `addedEpoch`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_streams_playlistId_streamType_ratingNumeric` ON `streams` (`playlistId`, `streamType`, `ratingNumeric`)"
                )
            }
        }

        val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `vod_catalog_groups` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `playlistId` INTEGER NOT NULL,
                        `streamType` TEXT NOT NULL,
                        `groupKey` TEXT NOT NULL,
                        `representativeStreamDbId` INTEGER NOT NULL,
                        `representativeStreamId` INTEGER NOT NULL,
                        `cleanTitle` TEXT NOT NULL,
                        `posterUrl` TEXT NOT NULL,
                        `rating` TEXT NOT NULL,
                        `ratingNumeric` REAL NOT NULL,
                        `releaseYear` INTEGER NOT NULL,
                        `addedEpoch` INTEGER NOT NULL,
                        `genres` TEXT NOT NULL,
                        `primaryCategoryId` TEXT NOT NULL,
                        `categoryIdsCsv` TEXT NOT NULL,
                        `versionCount` INTEGER NOT NULL,
                        `defaultVersionTag` TEXT NOT NULL,
                        `containerExtension` TEXT NOT NULL,
                        `popularityScore` REAL NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_vod_catalog_groups_playlistId_streamType_groupKey` ON `vod_catalog_groups` (`playlistId`, `streamType`, `groupKey`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_vod_catalog_groups_playlistId_streamType_popularityScore` ON `vod_catalog_groups` (`playlistId`, `streamType`, `popularityScore`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_vod_catalog_groups_playlistId_streamType_addedEpoch` ON `vod_catalog_groups` (`playlistId`, `streamType`, `addedEpoch`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_vod_catalog_groups_playlistId_streamType_primaryCategoryId` ON `vod_catalog_groups` (`playlistId`, `streamType`, `primaryCategoryId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_vod_catalog_groups_playlistId_streamType_cleanTitle` ON `vod_catalog_groups` (`playlistId`, `streamType`, `cleanTitle`)"
                )
            }
        }

        @Suppress("DEPRECATION")
        fun getInstance(context: Context): StreamVaultDatabase {
            com.example.data.repository.IptvRepository.initEpisodePreferences(context.applicationContext)
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StreamVaultDatabase::class.java,
                    "streamvault_database.db"
                )
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
