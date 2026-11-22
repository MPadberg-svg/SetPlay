package com.example.data.sync

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.repository.IptvRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Persistent WorkManager worker for granular per-section catalog synchronization
 * (`"LIVE"`, `"MOVIE"`, or `"SERIES"`). Survives app backgrounding, handles cancellation cleanly
 * by marking `sync_section_state` as `INTERRUPTED`, and retries transient network errors.
 */
class CatalogSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val playlistId = inputData.getLong(KEY_PLAYLIST_ID, -1L)
        val rawSectionType = inputData.getString(KEY_SECTION_TYPE).orEmpty().trim()
        val categoryId = inputData.getString(KEY_CATEGORY_ID)?.trim()?.takeIf { it.isNotEmpty() }
        val forceFull = inputData.getBoolean(KEY_FORCE_FULL, false)

        if (playlistId <= 0L || rawSectionType.isEmpty()) {
            Log.w(TAG, "Invalid inputs: playlistId=$playlistId sectionType=$rawSectionType")
            return Result.failure(errorOutputData("Invalid playlist id or section type"))
        }

        val normalizedSection = SyncTelemetry.normalizeSyncSectionType(rawSectionType)
        val repository = IptvRepository.getInstance(applicationContext)
        val playlist = repository.getPlaylistById(playlistId)
        if (playlist == null) {
            Log.w(TAG, "Playlist $playlistId not found for $normalizedSection sync")
            return Result.failure(errorOutputData("Playlist $playlistId not found"))
        }

        return try {
            val syncResult = withContext(PlaybackResourceGovernor.syncDispatcher) {
                repository.syncPlaylistSection(
                    playlist = playlist,
                    sectionType = normalizedSection,
                    categoryId = categoryId,
                    forceFullReconcile = forceFull
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
        const val KEY_ERROR = "error"

        fun uniqueWorkName(playlistId: Long, sectionType: String): String {
            val normalized = SyncTelemetry.normalizeSyncSectionType(sectionType)
            return "setplay_sync_${playlistId}_$normalized"
        }

        /**
         * Enqueues a unique background sync job for a single section (`LIVE`, `MOVIE`, or `SERIES`).
         * Uses [ExistingWorkPolicy.KEEP] so Movies, Series, and Live TV run independently without
         * cancelling or duplicating an already-active sync for the same section.
         */
        fun enqueueSectionSync(
            context: Context,
            playlistId: Long,
            sectionType: String,
            categoryId: String? = null,
            forceFull: Boolean = false,
            expedited: Boolean = false
        ): String {
            val normalizedSection = SyncTelemetry.normalizeSyncSectionType(sectionType)
            val workName = uniqueWorkName(playlistId, normalizedSection)
            val inputBuilder = Data.Builder()
                .putLong(KEY_PLAYLIST_ID, playlistId)
                .putString(KEY_SECTION_TYPE, normalizedSection)
                .putBoolean(KEY_FORCE_FULL, forceFull)
            if (!categoryId.isNullOrBlank()) {
                inputBuilder.putString(KEY_CATEGORY_ID, categoryId.trim())
            }

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val requestBuilder = OneTimeWorkRequestBuilder<CatalogSyncWorker>()
                .setInputData(inputBuilder.build())
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15L, TimeUnit.SECONDS)
                .addTag(workName)

            if (expedited) {
                requestBuilder.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            }

            runCatching {
                WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                    workName,
                    ExistingWorkPolicy.KEEP,
                    requestBuilder.build()
                )
            }.onFailure { err ->
                Log.w(TAG, "Unable to enqueue WorkManager job $workName: ${err.message}")
            }

            return workName
        }
    }
}
