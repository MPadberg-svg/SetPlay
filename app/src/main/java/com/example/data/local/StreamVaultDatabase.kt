package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.CategoryEntity
import com.example.data.model.EpgProgramEntity
import com.example.data.model.ParentalSettingsEntity
import com.example.data.model.PlaylistEntity
import com.example.data.model.StreamEntity

@Database(
    entities = [
        PlaylistEntity::class,
        CategoryEntity::class,
        StreamEntity::class,
        EpgProgramEntity::class,
        ParentalSettingsEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class StreamVaultDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun categoryDao(): CategoryDao
    abstract fun streamDao(): StreamDao
    abstract fun epgDao(): EpgDao
    abstract fun parentalSettingsDao(): ParentalSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: StreamVaultDatabase? = null

        fun getInstance(context: Context): StreamVaultDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StreamVaultDatabase::class.java,
                    "streamvault_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
