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

@Database(
    entities = [
        PlaylistEntity::class,
        CategoryEntity::class,
        StreamEntity::class,
        EpgProgramEntity::class,
        ParentalSettingsEntity::class,
        FavoriteTeamEntity::class,
        TournamentPreferenceEntity::class
    ],
    version = 4,
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

        @Suppress("DEPRECATION")
        fun getInstance(context: Context): StreamVaultDatabase {
            com.example.data.repository.IptvRepository.initEpisodePreferences(context.applicationContext)
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StreamVaultDatabase::class.java,
                    "streamvault_database.db"
                )
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
