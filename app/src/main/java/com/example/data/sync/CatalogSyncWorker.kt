package com.example.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.example.data.repository.IptvRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Persistent WorkManager worker for serialized per-section catalog synchronization
 * (`"LIVE"`, `"MOVIE"`, or `"SERIES"`). All enqueues route through [CatalogSyncScheduler]'s
 * single unique WorkManager queue ([CatalogSyncScheduler.UNIQUE_QUEUE_NAME]) so Live TV,
 * Series, and Movies never download or process concurrently against the same Xtream server.
 */
class CatalogSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val fallbackPlaylistId = inputData.getLong(KEY_PLAYLIST_ID, -1L)
        val fallbackSection = inputData.getString(KEY_SECTION_TYPE).orEmpty().trim()
        val fallbackCategoryId = inputData.getString(KEY_CATEGORY_ID)?.trim()?.takeIf { it.isNotEmpty() }
        val fallbackForceFull = inputData.getBoolean(KEY_FORCE_FULL, false)
        val fallbackPriorityRank = inputData.getInt(KEY_PRIORITY, SyncPriority.VOD_SYNC.rank)
        val fallbackPriority = SyncPriority.entries.firstOrNull { it.rank == fallbackPriorityRank }
            ?: CatalogSyncScheduler.defaultPriorityFor(fallbackSection, fallbackCategoryId)

        val fallbackRequest = if (fallbackPlaylistId > 0L && fallbackSection.isNotEmpty()) {
            QueuedSyncRequest(
                playlistId = fallbackPlaylistId,
                sectionType = SyncTelemetry.normalizeSyncSectionType(fallbackSection),
                categoryId = fallbackCategoryId,
                forceFull = fallbackForceFull,
                priority = fallbackPriority
            )
        } else {
            null
        }

        val initialRequest = CatalogSyncScheduler.pollNextPendingRequest(
            context = applicationContext,
            fallbackRequest = fallbackRequest
        )
        if (initialRequest == null) {
            Log.w(TAG, "No valid sync request found: playlistId=$fallbackPlaylistId sectionType=$fallbackSection")
            return Result.failure(errorOutputData("Invalid playlist id or section type"))
        }

        var currentRequest: QueuedSyncRequest? = initialRequest
        var finalResult: Result = Result.success()

        while (currentRequest != null) {
            val req = currentRequest
            val stepResult = executeSingleRequest(req)
            if (stepResult is Result.Retry) {
                return stepResult
            }
            if (stepResult is Result.Failure) {
                finalResult = stepResult
            }

            // Drain any additional pending requests in strict priority order while keeping single-queue serialization
            currentRequest = if (CatalogSyncScheduler.hasPendingRequests()) {
                CatalogSyncScheduler.pollNextPendingRequest(
                    context = applicationContext,
                    fallbackRequest = null
                )
            } else {
                null
            }
        }

        return finalResult
    }

    private suspend fun executeSingleRequest(request: QueuedSyncRequest): Result {
        val playlistId = request.playlistId
        val normalizedSection = SyncTelemetry.normalizeSyncSectionType(request.sectionType)
        val repository = IptvRepository.getInstance(applicationContext)

        return try {
            val playlist = repository.getPlaylistById(playlistId)
            if (playlist == null) {
                Log.w(TAG, "Playlist $playlistId not found for $normalizedSection sync")
                return Result.failure(errorOutputData("Playlist $playlistId not found"))
            }

            val syncResult = withContext(PlaybackResourceGovernor.syncDispatcher) {
                repository.syncPlaylistSection(
                    playlist = playlist,
                    sectionType = normalizedSection,
                    categoryId = request.categoryId,
                    forceFullReconcile = request.forceFull
                )
            }

            if (syncResult.isSuccess) {
                Result.success()
            } else {
                val err = syncResult.exceptionOrNull()
                val isTransientIo = isTransientNetworkFailure(err)
                if (isTransientIo && runAttemptCount < MAX_RETRY_ATTEMPTS) {
                    Log.w(TAG, "Transient sync failure for $normalizedSection (attempt ${runAttemptCount + 1}), scheduling retry")
                    Result.retry()
                } else {
                    val msg = err?.message ?: "Sync failed for $normalizedSection"
                    Log.e(TAG, "Sync failed for $normalizedSection: $msg")
                    Result.failure(errorOutputData(msg))
                }
            }
        } catch (ce: CancellationException) {
            withContext(NonCancellable) {
                repository.markSectionInterrupted(playlistId, normalizedSection)
            }
            throw ce
        } catch (ioe: IOException) {
            if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                Log.w(TAG, "Transient IOException during $normalizedSection sync (attempt ${runAttemptCount + 1}), retrying")
                Result.retry()
            } else {
                val msg = ioe.message ?: "Network error during $normalizedSection sync"
                Log.e(TAG, "Max retries reached for $normalizedSection sync: $msg")
                Result.failure(errorOutputData(msg))
            }
        } catch (e: Exception) {
            val msg = e.message ?: "Unexpected error during $normalizedSection sync"
            Log.e(TAG, "Unexpected error during $normalizedSection sync: $msg")
            Result.failure(errorOutputData(msg))
        } finally {
            CatalogSyncScheduler.markRequestFinished(playlistId, normalizedSection)
        }
    }

    private fun errorOutputData(message: String): Data {
        return Data.Builder()
            .putString(KEY_ERROR, message.take(512))
            .build()
    }

    private fun isTransientNetworkFailure(error: Throwable?): Boolean {
        if (error == null) return false
        var cursor: Throwable? = error
        var depth = 0
        while (cursor != null && depth < 5) {
            if (cursor is IOException) return true
            val msg = cursor.message.orEmpty()
            if (msg.contains("timeout", ignoreCase = true) ||
                msg.contains("Unable to reach", ignoreCase = true) ||
                msg.contains("Failed to connect", ignoreCase = true) ||
                msg.contains("Socket", ignoreCase = true) ||
                msg.contains("HTTP 5", ignoreCase = true) ||
                msg.contains("HTTP 429", ignoreCase = true)
            ) {
                return true
            }
            cursor = cursor.cause
            depth++
        }
        return false
    }

    companion object {
        private const val TAG = "CatalogSyncWorker"
        private const val MAX_RETRY_ATTEMPTS = 3

        const val KEY_PLAYLIST_ID = "playlist_id"
        const val KEY_SECTION_TYPE = "section_type"
        const val KEY_CATEGORY_ID = "category_id"
        const val KEY_FORCE_FULL = "force_full"
        const val KEY_PRIORITY = "priority"
        const val KEY_ERROR = "error"

        fun uniqueWorkName(playlistId: Long, sectionType: String): String {
            val normalized = SyncTelemetry.normalizeSyncSectionType(sectionType)
            return "setplay_sync_${playlistId}_$normalized"
        }

        /**
         * Delegates all section sync requests to [CatalogSyncScheduler]'s centralized single
         * serialized queue so Movies, Series, and Live TV never compete against the same host.
         */
        fun enqueueSectionSync(
            context: Context,
            playlistId: Long,
            sectionType: String,
            categoryId: String? = null,
            forceFull: Boolean = false,
            expedited: Boolean = false
        ): String {
            return CatalogSyncScheduler.enqueueSectionSync(
                context = context,
                playlistId = playlistId,
                sectionType = sectionType,
                categoryId = categoryId,
                forceFull = forceFull,
                priority = CatalogSyncScheduler.defaultPriorityFor(sectionType, categoryId),
                expedited = expedited
            )
        }
    }
}
