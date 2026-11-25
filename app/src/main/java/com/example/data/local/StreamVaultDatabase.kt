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
import com.example.data.model.SyncSectionStateEntity
import com.example.data.model.TournamentPreferenceEntity
import com.example.data.model.VodCatalogGroupEntity
import com.example.data.model.VodCatalogGroupFtsEntity
import com.example.data.model.VodCollectionEntity
import com.example.data.model.VodGroupMemberEntity

@Database(
    entities = [
        PlaylistEntity::class,
        CategoryEntity::class,
        StreamEntity::class,
        EpgProgramEntity::class,
        ParentalSettingsEntity::class,
        FavoriteTeamEntity::class,
        TournamentPreferenceEntity::class,
        VodCatalogGroupEntity::class,
        VodCatalogGroupFtsEntity::class,
        VodGroupMemberEntity::class,
        SyncSectionStateEntity::class,
        VodCollectionEntity::class
    ],
    version = 9,
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
    abstract fun syncSectionStateDao(): SyncSectionStateDao

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

        val MIGRATION_5_6: Migration = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE VIRTUAL TABLE IF NOT EXISTS `vod_catalog_groups_fts` USING FTS4(`cleanTitle` TEXT NOT NULL, `genres` TEXT NOT NULL, content=`vod_catalog_groups`)"
                )
                db.execSQL(
                    "CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_vod_catalog_groups_fts_BEFORE_UPDATE BEFORE UPDATE ON `vod_catalog_groups` BEGIN DELETE FROM `vod_catalog_groups_fts` WHERE `docid`=OLD.`rowid`; END"
                )
                db.execSQL(
                    "CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_vod_catalog_groups_fts_BEFORE_DELETE BEFORE DELETE ON `vod_catalog_groups` BEGIN DELETE FROM `vod_catalog_groups_fts` WHERE `docid`=OLD.`rowid`; END"
                )
                db.execSQL(
                    "CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_vod_catalog_groups_fts_AFTER_UPDATE AFTER UPDATE ON `vod_catalog_groups` BEGIN INSERT INTO `vod_catalog_groups_fts`(`docid`, `cleanTitle`, `genres`) VALUES (NEW.`rowid`, NEW.`cleanTitle`, NEW.`genres`); END"
                )
                db.execSQL(
                    "CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_vod_catalog_groups_fts_AFTER_INSERT AFTER INSERT ON `vod_catalog_groups` BEGIN INSERT INTO `vod_catalog_groups_fts`(`docid`, `cleanTitle`, `genres`) VALUES (NEW.`rowid`, NEW.`cleanTitle`, NEW.`genres`); END"
                )
                db.execSQL(
                    "INSERT INTO `vod_catalog_groups_fts`(`vod_catalog_groups_fts`) VALUES('rebuild')"
                )
            }
        }

        val MIGRATION_6_7: Migration = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE streams ADD COLUMN metadataHash INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_streams_playlistId_streamType_streamId_metadataHash` ON `streams` (`playlistId`, `streamType`, `streamId`, `metadataHash`)"
                )
                db.execSQL("ALTER TABLE vod_catalog_groups ADD COLUMN platformTags TEXT NOT NULL DEFAULT ''")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `vod_group_members` (
                        `playlistId` INTEGER NOT NULL,
                        `streamType` TEXT NOT NULL,
                        `groupKey` TEXT NOT NULL,
                        `streamDbId` INTEGER NOT NULL,
                        PRIMARY KEY(`playlistId`, `streamType`, `groupKey`, `streamDbId`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_vod_group_members_playlistId_streamType_groupKey` ON `vod_group_members` (`playlistId`, `streamType`, `groupKey`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_vod_group_members_streamDbId` ON `vod_group_members` (`streamDbId`)"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `sync_section_state` (
                        `playlistId` INTEGER NOT NULL,
                        `sectionType` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `stage` TEXT NOT NULL,
                        `sessionId` TEXT NOT NULL,
                        `isIndeterminate` INTEGER NOT NULL,
                        `progressPercent` INTEGER NOT NULL,
                        `totalDiscovered` INTEGER NOT NULL,
                        `itemsDownloaded` INTEGER NOT NULL,
                        `itemsProcessed` INTEGER NOT NULL,
                        `itemsUnchanged` INTEGER NOT NULL,
                        `itemsAdded` INTEGER NOT NULL,
                        `itemsUpdated` INTEGER NOT NULL,
                        `itemsRemoved` INTEGER NOT NULL,
                        `itemsSkipped` INTEGER NOT NULL,
                        `itemsGrouped` INTEGER NOT NULL,
                        `itemsPersisted` INTEGER NOT NULL,
                        `lastSuccessfulSyncMs` INTEGER NOT NULL,
                        `lastAttemptedSyncMs` INTEGER NOT NULL,
                        `pendingUpdateAvailable` INTEGER NOT NULL,
                        `pendingUpdateSummary` TEXT NOT NULL,
                        `etag` TEXT NOT NULL,
                        `lastModified` TEXT NOT NULL,
                        `lastContentLength` INTEGER NOT NULL,
                        `errorMessage` TEXT NOT NULL,
                        PRIMARY KEY(`playlistId`, `sectionType`)
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_7_8: Migration = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE vod_catalog_groups ADD COLUMN collectionId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE vod_catalog_groups ADD COLUMN collectionName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE vod_catalog_groups ADD COLUMN collectionOrder INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_vod_catalog_groups_playlistId_streamType_collectionId` ON `vod_catalog_groups` (`playlistId`, `streamType`, `collectionId`)"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `vod_collections` (
                        `playlistId` INTEGER NOT NULL,
                        `collectionId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `posterUrl` TEXT NOT NULL,
                        `movieCount` INTEGER NOT NULL,
                        `popularityScore` REAL NOT NULL,
                        PRIMARY KEY(`playlistId`, `collectionId`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_vod_collections_playlistId_popularityScore` ON `vod_collections` (`playlistId`, `popularityScore`)"
                )
            }
        }

        val MIGRATION_8_9: Migration = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sync_section_state ADD COLUMN queuePriority INTEGER NOT NULL DEFAULT 99")
                db.execSQL("ALTER TABLE sync_section_state ADD COLUMN queueSequence INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sync_section_state ADD COLUMN requestedCategoryId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE sync_section_state ADD COLUMN forceFullReconcile INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sync_section_state ADD COLUMN ownerWorkerId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE sync_section_state ADD COLUMN heartbeatMs INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val walHardeningCallback = object : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                db.query("PRAGMA busy_timeout = 5000").close()
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
                    .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                    .addCallback(walHardeningCallback)
                    .addMigrations(
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
