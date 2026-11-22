package com.example.data.sync

import com.example.data.model.SyncSectionStateEntity
import java.util.Locale

enum class SyncStatus {
    IDLE,
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    INTERRUPTED
}

enum class SyncStage(
    val label: String,
    val startPct: Int,
    val endPct: Int
) {
    IDLE("Idle", 0, 0),
    CONNECTING_AND_CHECKING("Connecting & checking updates", 0, 5),
    DOWNLOADING_AND_DIFFING("Downloading & comparing catalog", 5, 55),
    PARSING_AND_VALIDATING("Parsing & validating changes", 55, 65),
    GROUPING_AND_ORGANIZING("Grouping & ranking versions", 65, 85),
    PERSISTING_DATABASE("Saving changes to database", 85, 95),
    FINALIZING("Finalizing catalog", 95, 100)
}

data class SyncTelemetry(
    val playlistId: Long = 0L,
    val sectionType: String = "",
    val status: SyncStatus = SyncStatus.IDLE,
    val stage: SyncStage = SyncStage.IDLE,
    val sessionId: String = "",
    val isIndeterminate: Boolean = false,
    val progressPercent: Int = 0,
    val totalDiscovered: Int = 0,
    val itemsDownloaded: Int = 0,
    val itemsProcessed: Int = 0,
    val itemsUnchanged: Int = 0,
    val itemsAdded: Int = 0,
    val itemsUpdated: Int = 0,
    val itemsRemoved: Int = 0,
    val itemsSkipped: Int = 0,
    val itemsGrouped: Int = 0,
    val itemsPersisted: Int = 0,
    val itemsRemaining: Int = 0,
    val lastSuccessfulSyncMs: Long = 0L,
    val lastAttemptedSyncMs: Long = 0L,
    val pendingUpdateAvailable: Boolean = false,
    val pendingUpdateSummary: String = "",
    val etag: String = "",
    val lastModified: String = "",
    val lastContentLength: Long = -1L,
    val errorMessage: String = ""
) {
    val isActive: Boolean
        get() = status == SyncStatus.PENDING || status == SyncStatus.RUNNING

    fun toEntity(
        targetPlaylistId: Long = this.playlistId,
        targetSectionType: String = this.sectionType
    ): SyncSectionStateEntity {
        val clampedPct = if (status == SyncStatus.COMPLETED) {
            100
        } else {
            progressPercent.coerceIn(0, 99)
        }
        return SyncSectionStateEntity(
            playlistId = targetPlaylistId,
            sectionType = normalizeSyncSectionType(targetSectionType),
            status = status.name,
            stage = stage.name,
            sessionId = sessionId,
            isIndeterminate = isIndeterminate,
            progressPercent = clampedPct,
            totalDiscovered = totalDiscovered.coerceAtLeast(0),
            itemsDownloaded = itemsDownloaded.coerceAtLeast(0),
            itemsProcessed = itemsProcessed.coerceAtLeast(0),
            itemsUnchanged = itemsUnchanged.coerceAtLeast(0),
            itemsAdded = itemsAdded.coerceAtLeast(0),
            itemsUpdated = itemsUpdated.coerceAtLeast(0),
            itemsRemoved = itemsRemoved.coerceAtLeast(0),
            itemsSkipped = itemsSkipped.coerceAtLeast(0),
            itemsGrouped = itemsGrouped.coerceAtLeast(0),
            itemsPersisted = itemsPersisted.coerceAtLeast(0),
            lastSuccessfulSyncMs = lastSuccessfulSyncMs.coerceAtLeast(0L),
            lastAttemptedSyncMs = lastAttemptedSyncMs.coerceAtLeast(0L),
            pendingUpdateAvailable = pendingUpdateAvailable,
            pendingUpdateSummary = pendingUpdateSummary,
            etag = etag,
            lastModified = lastModified,
            lastContentLength = lastContentLength,
            errorMessage = errorMessage
        )
    }

    companion object {
        fun normalizeSyncSectionType(raw: String): String {
            return when (raw.trim().uppercase(Locale.US)) {
                "LIVE", "LIVE_TV" -> "LIVE"
                "MOVIE", "MOVIES", "VOD" -> "MOVIE"
                "SERIES" -> "SERIES"
                else -> raw.trim().uppercase(Locale.US)
            }
        }

        /**
         * Computes truthful progress during [SyncStage.DOWNLOADING_AND_DIFFING].
         *
         * Never invents percentages from stale playlist counts:
         * - When `contentLength > 0`, maps `bytesRead / contentLength` into `5..55` (determinate).
         * - When `contentLength <= 0`, marks `isIndeterminate = true` and keeps the stage start
         *   percent so UI displays live measurable item counters instead of a fake percentage.
         */
        fun computeDownloadStageProgress(
            bytesRead: Long,
            contentLength: Long,
            previousPercent: Int = SyncStage.DOWNLOADING_AND_DIFFING.startPct
        ): Pair<Int, Boolean> {
            val stage = SyncStage.DOWNLOADING_AND_DIFFING
            if (contentLength <= 0L) {
                return previousPercent.coerceIn(stage.startPct, stage.endPct) to true
            }
            val safeBytes = bytesRead.coerceIn(0L, contentLength)
            val span = stage.endPct - stage.startPct
            val mapped = stage.startPct + ((safeBytes * span.toLong()) / contentLength).toInt()
            val monotonic = maxOf(previousPercent, mapped).coerceIn(stage.startPct, stage.endPct)
            return monotonic to false
        }

        /**
         * Maps known work counts inside a non-download stage into `[stage.startPct, stage.endPct]`
         * while guaranteeing monotonic non-decreasing progress and never emitting `100` prior to
         * final database commit.
         */
        fun computeStageProgress(
            stage: SyncStage,
            processed: Int,
            total: Int,
            previousPercent: Int = 0,
            allowComplete: Boolean = false
        ): Int {
            val maxAllowed = if (allowComplete) 100 else 99
            if (stage == SyncStage.IDLE) return 0
            val mapped = if (total <= 0) {
                stage.startPct
            } else {
                val clampedProcessed = processed.coerceIn(0, total)
                val span = (stage.endPct - stage.startPct).coerceAtLeast(0)
                stage.startPct + ((clampedProcessed.toLong() * span.toLong()) / total.toLong()).toInt()
            }
            return maxOf(previousPercent, mapped).coerceIn(stage.startPct, minOf(stage.endPct, maxAllowed))
        }
    }
}

fun SyncSectionStateEntity.toSyncTelemetry(): SyncTelemetry {
    val parsedStatus = try {
        SyncStatus.valueOf(status.trim().uppercase(Locale.US))
    } catch (_: Exception) {
        SyncStatus.IDLE
    }
    val parsedStage = try {
        SyncStage.valueOf(stage.trim().uppercase(Locale.US))
    } catch (_: Exception) {
        SyncStage.IDLE
    }
    val remaining = if (totalDiscovered > 0) {
        (totalDiscovered - itemsProcessed).coerceAtLeast(0)
    } else {
        0
    }
    return SyncTelemetry(
        playlistId = playlistId,
        sectionType = sectionType,
        status = parsedStatus,
        stage = parsedStage,
        sessionId = sessionId,
        isIndeterminate = isIndeterminate,
        progressPercent = progressPercent.coerceIn(0, 100),
        totalDiscovered = totalDiscovered,
        itemsDownloaded = itemsDownloaded,
        itemsProcessed = itemsProcessed,
        itemsUnchanged = itemsUnchanged,
        itemsAdded = itemsAdded,
        itemsUpdated = itemsUpdated,
        itemsRemoved = itemsRemoved,
        itemsSkipped = itemsSkipped,
        itemsGrouped = itemsGrouped,
        itemsPersisted = itemsPersisted,
        itemsRemaining = remaining,
        lastSuccessfulSyncMs = lastSuccessfulSyncMs,
        lastAttemptedSyncMs = lastAttemptedSyncMs,
        pendingUpdateAvailable = pendingUpdateAvailable,
        pendingUpdateSummary = pendingUpdateSummary,
        etag = etag,
        lastModified = lastModified,
        lastContentLength = lastContentLength,
        errorMessage = errorMessage
    )
}
