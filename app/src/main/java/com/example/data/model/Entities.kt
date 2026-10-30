package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "playlists"
)
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val serverUrl: String,
    val username: String,
    val password: String,
    val isActive: Boolean = true,
    val status: String = "Active",
    val expDate: String = "",
    val maxConnections: String = "1",
    val lastSyncTimestamp: Long = 0L,
    val channelCount: Int = 0,
    val vodCount: Int = 0,
    val seriesCount: Int = 0
)

@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["playlistId", "type", "categoryId"], unique = true),
        Index(value = ["playlistId", "type", "categoryName"])
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistId: Long,
    val categoryId: String,
    val categoryName: String,
    val type: String, // "LIVE", "MOVIE", "SERIES"
    val isLocked: Boolean = false
)

data class CategoryWithCount(
    val categoryId: String,
    val categoryName: String,
    val type: String,
    val isLocked: Boolean,
    val streamCount: Int
)

data class CategoryCountTuple(
    val categoryId: String,
    val streamCount: Int
)

@Entity(
    tableName = "streams",
    indices = [
        Index(value = ["playlistId", "streamType", "streamId"], unique = true),
        Index(value = ["playlistId", "streamType", "categoryId"]),
        Index(value = ["playlistId", "isFavorite"]),
        Index(value = ["playlistId", "watchHistoryTimestamp"]),
        Index(value = ["name"])
    ]
)
data class StreamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistId: Long,
    val streamId: Long,
    val categoryId: String,
    val name: String,
    val streamType: String, // "live", "movie", "series"
    val streamIcon: String = "",
    val epgChannelId: String = "",
    val rating: String = "",
    val tvArchive: Int = 0, // Catchup indicator: 1 = supported
    val tvArchiveDuration: Int = 0, // Catchup duration in days
    val containerExtension: String = "ts",
    val isFavorite: Boolean = false,
    val isLocked: Boolean = false,
    val watchHistoryTimestamp: Long = 0L,
    val lastPositionMs: Long = 0L
)

@Entity(
    tableName = "epg_programs",
    indices = [
        Index(value = ["playlistId", "streamId"]),
        Index(value = ["startEpoch", "endEpoch"])
    ]
)
data class EpgProgramEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistId: Long,
    val streamId: Long,
    val epgChannelId: String = "",
    val title: String,
    val description: String = "",
    val startEpoch: Long,
    val endEpoch: Long
)

@Entity(tableName = "parental_settings")
data class ParentalSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val pin: String = "0000",
    val isEnabled: Boolean = false,
    val lockedCategoriesJson: String = "[]" // JSON array of category IDs
)
