package com.example.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.StreamVaultDatabase
import com.example.data.local.SyncSectionStateDao
import com.example.data.model.SyncSectionStateEntity
import com.example.data.repository.IptvRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Single Room-draining WorkManager runner for serialized per-section catalog synchronization.
 *
 * Always pulls work strictly from Room via [CatalogTaskOrchestrator.claimNextTask], maintains
 * a 5-second lease heartbeat while running, never strands queued sections on single-section
 * failure, and never enters WorkManager exponential backoff while other tasks are ready.
 */
class CatalogSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val workerId = id.toString()
        val repository = IptvRepository.getInstance(applicationContext)
        val dao = StreamVaultDatabase.getInstance(applicationContext).syncSectionStateDao()

        while (true) {
            val task = CatalogTaskOrchestrator.claimNextTask(applicationContext, workerId)
                ?: break

            executeClaimedTask(
                task = task,
                workerId = workerId,
                repository = repository,
                dao = dao
            )
        }

        // Race-free re-check: if a new task arrived as the drain loop exited, ensure runner wakes.
        if (CatalogTaskOrchestrator.hasPendingTasks(applicationContext)) {
            CatalogTaskOrchestrator.ensureRunnerActive(
                context = applicationContext,
                forceWakeIfIdle = false
            )
        }

        return Result.success()
    }

    private suspend fun executeClaimedTask(
        task: SyncSectionStateEntity,
        workerId: String,
        repository: IptvRepository,
        dao: SyncSectionStateDao
    ) {
        val playlistId = task.playlistId
        val normalizedSection = SyncTelemetry.normalizeSyncSectionType(task.sectionType)

        coroutineScope {
            val heartbeatJob = launch(Dispatchers.IO) {
                while (isActive) {
                    delay(CatalogTaskOrchestrator.HEARTBEAT_INTERVAL_MS)
                    runCatching {
                        dao.updateHeartbeat(
                            playlistId = playlistId,
                            sectionType = normalizedSection,
                            ownerWorkerId = workerId,
                            heartbeatMs = System.currentTimeMillis()
                        )
                    }
                }
            }

            try {
                val playlist = repository.getPlaylistById(playlistId)
                if (playlist == null) {
                    Log.w(TAG, "Playlist $playlistId not found for $normalizedSection sync")
                    CatalogTaskOrchestrator.markTaskTerminal(
                        context = applicationContext,
                        playlistId = playlistId,
                        sectionType = normalizedSection,
                        workerId = workerId,
                        terminalStatus = SyncStatus.FAILED,
                        errorMessage = "Playlist $playlistId not found"
                    )
                    return@coroutineScope
                }

                val categoryId = task.requestedCategoryId.trim().takeIf { it.isNotEmpty() }
                val syncResult = withContext(PlaybackResourceGovernor.syncDispatcher) {
                    repository.syncPlaylistSection(
                        playlist = playlist,
                        sectionType = normalizedSection,
                        categoryId = categoryId,
                        forceFullReconcile = task.forceFullReconcile
                    )
                }

                if (syncResult.isSuccess) {
                    CatalogTaskOrchestrator.releaseLeaseIfStillRunning(
                        context = applicationContext,
                        playlistId = playlistId,
                        sectionType = normalizedSection,
                        workerId = workerId
                    )
                } else {
                    val err = syncResult.exceptionOrNull()
                    val terminalStatus = if (isTransientNetworkFailure(err)) {
                        SyncStatus.INTERRUPTED
                    } else {
                        SyncStatus.FAILED
                    }
                    val msg = err?.message ?: "Sync failed for $normalizedSection"
                    Log.w(TAG, "Section $normalizedSection ended with $terminalStatus (continuing queue): $msg")
                    CatalogTaskOrchestrator.markTaskTerminal(
                        context = applicationContext,
                        playlistId = playlistId,
                        sectionType = normalizedSection,
                        workerId = workerId,
                        terminalStatus = terminalStatus,
                        errorMessage = msg
                    )
                }
            } catch (ce: CancellationException) {
                withContext(NonCancellable) {
                    CatalogTaskOrchestrator.markTaskTerminal(
                        context = applicationContext,
                        playlistId = playlistId,
                        sectionType = normalizedSection,
                        workerId = workerId,
                        terminalStatus = SyncStatus.INTERRUPTED,
                        errorMessage = ""
                    )
                }
                throw ce
            } catch (ioe: IOException) {
                val msg = ioe.message ?: "Network error during $normalizedSection sync"
                Log.w(TAG, "Transient network error for $normalizedSection (marking INTERRUPTED and continuing queue): $msg")
                CatalogTaskOrchestrator.markTaskTerminal(
                    context = applicationContext,
                    playlistId = playlistId,
                    sectionType = normalizedSection,
                    workerId = workerId,
                    terminalStatus = SyncStatus.INTERRUPTED,
                    errorMessage = msg
                )
            } catch (e: Exception) {
                val msg = e.message ?: "Unexpected error during $normalizedSection sync"
                Log.e(TAG, "Unexpected error during $normalizedSection sync (marking FAILED and continuing queue): $msg")
                CatalogTaskOrchestrator.markTaskTerminal(
                    context = applicationContext,
                    playlistId = playlistId,
                    sectionType = normalizedSection,
                    workerId = workerId,
                    terminalStatus = SyncStatus.FAILED,
                    errorMessage = msg
                )
            } finally {
                heartbeatJob.cancel()
            }
        }
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
         * Delegates all section sync requests through [CatalogSyncScheduler] ->
         * [CatalogTaskOrchestrator]'s Room-backed serialized queue.
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
