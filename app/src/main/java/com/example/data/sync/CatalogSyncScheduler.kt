package com.example.data.sync

import android.content.Context
import com.example.data.local.StreamVaultDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

/**
 * Legacy priority tiers mapped onto [TaskPriority] inside [CatalogTaskOrchestrator].
 * Preserved for full backward compatibility with existing UI callers and unit tests.
 */
enum class SyncPriority(val rank: Int) {
    CATEGORY_REFRESH(0),
    LIVE_SYNC(1),
    VOD_SYNC(2),
    BACKGROUND_CHECK(3)
}

data class QueuedSyncRequest(
    val playlistId: Long,
    val sectionType: String,
    val categoryId: String? = null,
    val forceFull: Boolean = false,
    val priority: SyncPriority = SyncPriority.VOD_SYNC,
    val enqueuedSeq: Long = 0L
)

/**
 * Thin backward-compatible facade delegating 100% of catalog sync orchestration to
 * [CatalogTaskOrchestrator] (Room-backed Single Source of Truth).
 *
 * Holds zero RAM-only queue maps and spawns zero asynchronous DB-write coroutines:
 * every Room write commits synchronously before returning or waking WorkManager.
 */
object CatalogSyncScheduler {
    const val UNIQUE_QUEUE_NAME = CatalogTaskOrchestrator.UNIQUE_RUNNER_NAME
    const val STATUS_QUEUED_WAITING = CatalogTaskOrchestrator.STATUS_QUEUED_WAITING
    const val STATUS_QUEUED = "Queued…"

    private const val FACADE_TEST_WORKER_ID = "facade-scheduler-worker"

    fun defaultPriorityFor(sectionType: String, categoryId: String?): SyncPriority {
        if (!categoryId.isNullOrBlank()) return SyncPriority.CATEGORY_REFRESH
        return when (SyncTelemetry.normalizeSyncSectionType(sectionType)) {
            "LIVE" -> SyncPriority.LIVE_SYNC
            else -> SyncPriority.VOD_SYNC
        }
    }

    private fun mapToTaskPriority(
        priority: SyncPriority,
        categoryId: String?,
        userInitiated: Boolean,
        existingStatus: SyncStatus
    ): TaskPriority {
        if (!categoryId.isNullOrBlank() || priority == SyncPriority.CATEGORY_REFRESH) {
            return TaskPriority.CATEGORY_REFRESH
        }
        if (userInitiated &&
            (existingStatus == SyncStatus.INTERRUPTED || existingStatus == SyncStatus.FAILED)
        ) {
            return TaskPriority.USER_RESUME
        }
        if (userInitiated && priority == SyncPriority.VOD_SYNC) {
            return TaskPriority.USER_SECTION_SYNC
        }
        return when (priority) {
            SyncPriority.CATEGORY_REFRESH -> TaskPriority.CATEGORY_REFRESH
            SyncPriority.LIVE_SYNC -> TaskPriority.USER_SECTION_SYNC
            SyncPriority.VOD_SYNC -> TaskPriority.INITIAL_ACCOUNT_SYNC
            SyncPriority.BACKGROUND_CHECK -> TaskPriority.BACKGROUND_CHECK
        }
    }

    private fun mapToLegacyPriority(
        sectionType: String,
        queuePriority: Int,
        requestedCategoryId: String
    ): SyncPriority {
        return when {
            requestedCategoryId.isNotBlank() || queuePriority == TaskPriority.CATEGORY_REFRESH.rank ->
                SyncPriority.CATEGORY_REFRESH
            queuePriority == TaskPriority.BACKGROUND_CHECK.rank ->
                SyncPriority.BACKGROUND_CHECK
            sectionType == "LIVE" && queuePriority <= TaskPriority.USER_SECTION_SYNC.rank ->
                SyncPriority.LIVE_SYNC
            else -> SyncPriority.VOD_SYNC
        }
    }

    /**
     * Delegates synchronously to [CatalogTaskOrchestrator.enqueueSection] so Room commits
     * before returning and before WorkManager is woken.
     */
    fun enqueueSectionSync(
        context: Context,
        playlistId: Long,
        sectionType: String,
        categoryId: String? = null,
        forceFull: Boolean = false,
        priority: SyncPriority = defaultPriorityFor(sectionType, categoryId),
        expedited: Boolean = false,
        userInitiated: Boolean = false
    ): String {
        if (playlistId <= 0L) return UNIQUE_QUEUE_NAME
        val appContext = context.applicationContext
        val normalizedSection = SyncTelemetry.normalizeSyncSectionType(sectionType)
        val cleanCategoryId = categoryId?.trim()?.takeIf { it.isNotEmpty() }

        runBlocking(Dispatchers.IO) {
            val existingRow = runCatching {
                StreamVaultDatabase.getInstance(appContext)
                    .syncSectionStateDao()
                    .getSectionOnce(playlistId, normalizedSection)
            }.getOrNull()
            val existingStatus = CatalogTaskStateMachine.parseStatus(existingRow?.status)
            val resolvedPriority = mapToTaskPriority(
                priority = priority,
                categoryId = cleanCategoryId,
                userInitiated = userInitiated,
                existingStatus = existingStatus
            )
            CatalogTaskOrchestrator.enqueueSection(
                context = appContext,
                playlistId = playlistId,
                sectionType = normalizedSection,
                priority = resolvedPriority,
                categoryId = cleanCategoryId,
                forceFull = forceFull
            )
        }
        return UNIQUE_QUEUE_NAME
    }

    internal fun ensureDrainWorkerScheduled(context: Context) {
        CatalogTaskOrchestrator.ensureRunnerActive(
            context = context.applicationContext,
            forceWakeIfIdle = false
        )
    }

    internal suspend fun pollNextPendingRequest(
        context: Context,
        fallbackRequest: QueuedSyncRequest?
    ): QueuedSyncRequest? {
        val claimed = CatalogTaskOrchestrator.claimNextTask(
            context = context.applicationContext,
            workerId = FACADE_TEST_WORKER_ID
        )
        if (claimed != null) {
            return QueuedSyncRequest(
                playlistId = claimed.playlistId,
                sectionType = claimed.sectionType,
                categoryId = claimed.requestedCategoryId.takeIf { it.isNotBlank() },
                forceFull = claimed.forceFullReconcile,
                priority = mapToLegacyPriority(
                    sectionType = claimed.sectionType,
                    queuePriority = claimed.queuePriority,
                    requestedCategoryId = claimed.requestedCategoryId
                ),
                enqueuedSeq = claimed.queueSequence
            )
        }
        if (fallbackRequest != null &&
            fallbackRequest.playlistId > 0L &&
            fallbackRequest.sectionType.isNotBlank()
        ) {
            return fallbackRequest.copy(
                sectionType = SyncTelemetry.normalizeSyncSectionType(fallbackRequest.sectionType)
            )
        }
        return null
    }

    internal fun markRequestFinished(playlistId: Long, sectionType: String) {
        val ctx = CatalogTaskOrchestrator.lastAppContext ?: return
        runBlocking(Dispatchers.IO) {
            CatalogTaskOrchestrator.markTaskTerminal(
                context = ctx,
                playlistId = playlistId,
                sectionType = sectionType,
                workerId = null,
                terminalStatus = SyncStatus.COMPLETED,
                errorMessage = ""
            )
        }
    }

    internal fun hasPendingRequests(): Boolean {
        val ctx = CatalogTaskOrchestrator.lastAppContext ?: return false
        return runBlocking(Dispatchers.IO) {
            CatalogTaskOrchestrator.hasPendingTasks(ctx)
        }
    }

    internal fun isSectionQueuedOrRunning(playlistId: Long, sectionType: String): Boolean {
        val ctx = CatalogTaskOrchestrator.lastAppContext ?: return false
        return runBlocking(Dispatchers.IO) {
            CatalogTaskOrchestrator.isSectionQueuedOrRunning(ctx, playlistId, sectionType)
        }
    }

    internal fun clearForTest() {
        runBlocking(Dispatchers.IO) {
            CatalogTaskOrchestrator.clearForTest()
        }
    }
}
