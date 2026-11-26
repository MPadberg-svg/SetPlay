package com.example.data.sync

import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.room.withTransaction
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.data.local.StreamVaultDatabase
import com.example.data.model.SyncSectionStateEntity
import com.example.data.repository.IptvRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/**
 * Room-backed Single Source of Truth (SSOT) catalog task orchestrator.
 *
 * Eliminates RAM-only pending queues, split-brain inline sync loops, and Resume priority
 * inversions by persisting every task's priority, FIFO sequence, lease owner, and heartbeat
 * atomically in Room (`sync_section_state`) behind [orchestrationMutex].
 */
object CatalogTaskOrchestrator {
    private const val TAG = "CatalogTaskOrchestrator"

    const val UNIQUE_RUNNER_NAME = "setplay_catalog_sync_queue"
    const val STALE_LEASE_TIMEOUT_MS = 45_000L
    const val HEARTBEAT_INTERVAL_MS = 5_000L
    const val STATUS_QUEUED_WAITING = "Queued (waiting for active sync…)"
    const val STATUS_STARTING = "Starting sync…"

    private val orchestrationMutex = Mutex()
    private val sequenceCounter = AtomicLong(0L)
    private val runnerWakeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    internal var lastAppContext: Context? = null

    private fun nextMonotonicSeq(): Long {
        while (true) {
            val prev = sequenceCounter.get()
            val candidate = maxOf(prev + 1L, System.currentTimeMillis())
            if (sequenceCounter.compareAndSet(prev, candidate)) {
                return candidate
            }
        }
    }

    /**
     * Atomically enqueues or upgrades a single `(playlistId, sectionType)` task in Room
     * and wakes the single WorkManager runner strictly AFTER the Room write commits.
     */
    suspend fun enqueueSection(
        context: Context,
        playlistId: Long,
        sectionType: String,
        priority: TaskPriority,
        categoryId: String? = null,
        forceFull: Boolean = false
    ): Boolean = withContext(Dispatchers.IO) {
        if (playlistId <= 0L) return@withContext false
        val appContext = context.applicationContext
        lastAppContext = appContext
        val normalizedSection = SyncTelemetry.normalizeSyncSectionType(sectionType)
        val cleanCategoryId = categoryId?.trim().orEmpty()

        var committedSummary: String? = null
        var committed = false

        orchestrationMutex.withLock {
            val now = System.currentTimeMillis()
            val dao = StreamVaultDatabase.getInstance(appContext).syncSectionStateDao()

            // 1. Recover any stale worker leases (> 45s without heartbeat)
            dao.recoverStaleRunningTasks(
                staleBeforeMs = now - STALE_LEASE_TIMEOUT_MS,
                nowMs = now
            )

            // 2. Inspect authoritative Room state
            val existing = dao.getSectionOnce(playlistId, normalizedSection)
            val currentStatus = CatalogTaskStateMachine.parseStatus(existing?.status)

            // 3. Idempotent no-op if already RUNNING (strictly forbids RUNNING -> PENDING)
            if (currentStatus == SyncStatus.RUNNING) {
                return@withContext false
            }

            val runningTasks = dao.getRunningTasks()
            val anotherSectionRunning = runningTasks.any { running ->
                running.playlistId != playlistId || running.sectionType != normalizedSection
            }
            val summary = if (anotherSectionRunning) STATUS_QUEUED_WAITING else STATUS_STARTING

            // 4. If already PENDING, upgrade priority/category/forceFull in-place
            if (currentStatus == SyncStatus.PENDING && existing != null) {
                if (!CatalogTaskStateMachine.requireValidTransition(
                        currentStatus,
                        SyncStatus.PENDING,
                        "enqueueSection-upgrade($playlistId,$normalizedSection)"
                    )
                ) {
                    return@withContext false
                }
                val upgradedPriority = if (priority.rank < existing.queuePriority) {
                    priority.rank
                } else {
                    existing.queuePriority
                }
                val upgradedCategory = if (cleanCategoryId.isNotEmpty()) {
                    cleanCategoryId
                } else {
                    existing.requestedCategoryId
                }
                val upgradedForceFull = existing.forceFullReconcile || forceFull
                dao.upsert(
                    existing.copy(
                        queuePriority = upgradedPriority,
                        requestedCategoryId = upgradedCategory,
                        forceFullReconcile = upgradedForceFull,
                        pendingUpdateSummary = summary,
                        lastAttemptedSyncMs = now,
                        errorMessage = ""
                    )
                )
                committedSummary = summary
                committed = true
            } else {
                // 5. Validate FSM transition into PENDING and persist synchronously to Room
                if (!CatalogTaskStateMachine.requireValidTransition(
                        currentStatus,
                        SyncStatus.PENDING,
                        "enqueueSection($playlistId,$normalizedSection)"
                    )
                ) {
                    return@withContext false
                }
                val pendingEntity = SyncSectionStateEntity(
                    playlistId = playlistId,
                    sectionType = normalizedSection,
                    status = SyncStatus.PENDING.name,
                    stage = "PENDING",
                    sessionId = existing?.sessionId.orEmpty(),
                    isIndeterminate = true,
                    progressPercent = 0,
                    totalDiscovered = existing?.totalDiscovered ?: 0,
                    itemsDownloaded = 0,
                    itemsProcessed = 0,
                    itemsUnchanged = 0,
                    itemsAdded = 0,
                    itemsUpdated = 0,
                    itemsRemoved = 0,
                    itemsSkipped = 0,
                    itemsGrouped = 0,
                    itemsPersisted = 0,
                    lastSuccessfulSyncMs = existing?.lastSuccessfulSyncMs ?: 0L,
                    lastAttemptedSyncMs = now,
                    pendingUpdateAvailable = false,
                    pendingUpdateSummary = summary,
                    etag = existing?.etag.orEmpty(),
                    lastModified = existing?.lastModified.orEmpty(),
                    lastContentLength = existing?.lastContentLength ?: -1L,
                    errorMessage = "",
                    queuePriority = priority.rank,
                    queueSequence = nextMonotonicSeq(),
                    requestedCategoryId = cleanCategoryId,
                    forceFullReconcile = forceFull,
                    ownerWorkerId = "",
                    heartbeatMs = 0L
                )
                dao.upsert(pendingEntity)
                committedSummary = summary
                committed = true
            }
        }

        if (!committed) return@withContext false

        committedSummary?.let { text ->
            runCatching {
                IptvRepository.getInstance(appContext).notifySectionQueued(
                    sectionType = normalizedSection,
                    statusText = text
                )
            }
        }

        // 6. Wake the single WorkManager runner ONLY AFTER Room upsert has committed
        val forceWake = priority == TaskPriority.USER_RESUME ||
            priority == TaskPriority.CATEGORY_REFRESH ||
            priority == TaskPriority.USER_SECTION_SYNC
        ensureRunnerActive(appContext, forceWakeIfIdle = forceWake)
        true
    }

    /**
     * Explicit user Resume entry point. Always assigns [TaskPriority.USER_RESUME] (rank 0)
     * so a resumed section NEVER loses priority to waiting lower-priority sections.
     */
    suspend fun resumeSection(
        context: Context,
        playlistId: Long,
        sectionType: String
    ): Boolean {
        return enqueueSection(
            context = context,
            playlistId = playlistId,
            sectionType = sectionType,
            priority = TaskPriority.USER_RESUME,
            categoryId = null,
            forceFull = false
        )
    }

    /**
     * Atomically enqueues `LIVE` (seq+1), `SERIES` (seq+2), and `MOVIE` (seq+3) at
     * [TaskPriority.INITIAL_ACCOUNT_SYNC] in a single Room transaction, then wakes the runner.
     */
    suspend fun enqueueAccountSync(
        context: Context,
        playlistId: Long
    ) = withContext(Dispatchers.IO) {
        if (playlistId <= 0L) return@withContext
        val appContext = context.applicationContext
        lastAppContext = appContext

        val queuedNotifications = ArrayList<Pair<String, String>>(3)

        orchestrationMutex.withLock {
            val now = System.currentTimeMillis()
            val db = StreamVaultDatabase.getInstance(appContext)
            val dao = db.syncSectionStateDao()

            dao.recoverStaleRunningTasks(
                staleBeforeMs = now - STALE_LEASE_TIMEOUT_MS,
                nowMs = now
            )

            val runningTasks = dao.getRunningTasks()
            val hasAnyRunning = runningTasks.isNotEmpty()
            val sections = listOf("LIVE", "SERIES", "MOVIE")

            db.withTransaction {
                var queuedEarlierInBatch = false
                for (section in sections) {
                    val existing = dao.getSectionOnce(playlistId, section)
                    val currentStatus = CatalogTaskStateMachine.parseStatus(existing?.status)
                    if (currentStatus == SyncStatus.RUNNING) {
                        queuedEarlierInBatch = true
                    } else {
                        val summary = if (hasAnyRunning || queuedEarlierInBatch) {
                            STATUS_QUEUED_WAITING
                        } else {
                            STATUS_STARTING
                        }

                        if (currentStatus == SyncStatus.PENDING && existing != null) {
                            val upgradedPriority = minOf(existing.queuePriority, TaskPriority.INITIAL_ACCOUNT_SYNC.rank)
                            dao.upsert(
                                existing.copy(
                                    queuePriority = upgradedPriority,
                                    pendingUpdateSummary = summary,
                                    lastAttemptedSyncMs = now,
                                    errorMessage = ""
                                )
                            )
                            queuedNotifications.add(section to summary)
                            queuedEarlierInBatch = true
                        } else if (CatalogTaskStateMachine.requireValidTransition(
                                currentStatus,
                                SyncStatus.PENDING,
                                "enqueueAccountSync($playlistId,$section)"
                            )
                        ) {
                            val pendingEntity = SyncSectionStateEntity(
                                playlistId = playlistId,
                                sectionType = section,
                                status = SyncStatus.PENDING.name,
                                stage = "PENDING",
                                sessionId = existing?.sessionId.orEmpty(),
                                isIndeterminate = true,
                                progressPercent = 0,
                                totalDiscovered = existing?.totalDiscovered ?: 0,
                                itemsDownloaded = 0,
                                itemsProcessed = 0,
                                itemsUnchanged = 0,
                                itemsAdded = 0,
                                itemsUpdated = 0,
                                itemsRemoved = 0,
                                itemsSkipped = 0,
                                itemsGrouped = 0,
                                itemsPersisted = 0,
                                lastSuccessfulSyncMs = existing?.lastSuccessfulSyncMs ?: 0L,
                                lastAttemptedSyncMs = now,
                                pendingUpdateAvailable = false,
                                pendingUpdateSummary = summary,
                                etag = existing?.etag.orEmpty(),
                                lastModified = existing?.lastModified.orEmpty(),
                                lastContentLength = existing?.lastContentLength ?: -1L,
                                errorMessage = "",
                                queuePriority = TaskPriority.INITIAL_ACCOUNT_SYNC.rank,
                                queueSequence = nextMonotonicSeq(),
                                requestedCategoryId = "",
                                forceFullReconcile = false,
                                ownerWorkerId = "",
                                heartbeatMs = 0L
                            )
                            dao.upsert(pendingEntity)
                            queuedNotifications.add(section to summary)
                            queuedEarlierInBatch = true
                        }
                    }
                }
            }
        }

        val repo = runCatching { IptvRepository.getInstance(appContext) }.getOrNull()
        if (repo != null) {
            for ((section, summary) in queuedNotifications) {
                repo.notifySectionQueued(sectionType = section, statusText = summary)
            }
        }

        ensureRunnerActive(appContext, forceWakeIfIdle = true)
    }

    /**
     * Cancels all `PENDING` and `RUNNING` tasks for [playlistId] in Room, cancels the active
     * WorkManager runner if that playlist owned it, and wakes the runner again if other playlists
     * still have pending work.
     */
    suspend fun cancelPlaylistTasks(
        context: Context,
        playlistId: Long
    ) = withContext(Dispatchers.IO) {
        if (playlistId <= 0L) return@withContext
        val appContext = context.applicationContext
        lastAppContext = appContext

        var playlistOwnedRunningWorker = false
        var remainingPending = false

        orchestrationMutex.withLock {
            val dao = StreamVaultDatabase.getInstance(appContext).syncSectionStateDao()
            val runningTasks = dao.getRunningTasks()
            playlistOwnedRunningWorker = runningTasks.any { running -> running.playlistId == playlistId }
            dao.cancelAllForPlaylist(playlistId)
            remainingPending = dao.countPending() > 0
        }

        if (playlistOwnedRunningWorker) {
            runCatching {
                WorkManager.getInstance(appContext).cancelUniqueWork(UNIQUE_RUNNER_NAME)
            }.onFailure { err ->
                Log.w(TAG, "Unable to cancel WorkManager runner for playlist $playlistId: ${err.message}")
            }
        }
        if (remainingPending) {
            ensureRunnerActive(appContext, forceWakeIfIdle = true)
        }
    }

    /**
     * Atomically claims the highest-priority `PENDING` task from Room for [workerId].
     *
     * 1. Recovers stale leases older than 45 seconds.
     * 2. Verifies no other live worker holds a `RUNNING` row (strict single-lane exclusion).
     * 3. Picks `getNextPendingTask()` (`ORDER BY queuePriority ASC, queueSequence ASC LIMIT 1`).
     * 4. Validates `PENDING -> RUNNING` via [CatalogTaskStateMachine].
     * 5. Atomically persists `status = RUNNING`, `ownerWorkerId = workerId`, `heartbeatMs = now`.
     */
    suspend fun claimNextTask(
        context: Context,
        workerId: String
    ): SyncSectionStateEntity? = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        lastAppContext = appContext

        orchestrationMutex.withLock {
            val now = System.currentTimeMillis()
            val dao = StreamVaultDatabase.getInstance(appContext).syncSectionStateDao()

            dao.recoverStaleRunningTasks(
                staleBeforeMs = now - STALE_LEASE_TIMEOUT_MS,
                nowMs = now
            )

            val runningTasks = dao.getRunningTasks()
            if (runningTasks.isNotEmpty()) {
                return@withLock null
            }

            val next = dao.getNextPendingTask() ?: return@withLock null
            val currentStatus = CatalogTaskStateMachine.parseStatus(next.status)
            if (!CatalogTaskStateMachine.requireValidTransition(
                    currentStatus,
                    SyncStatus.RUNNING,
                    "claimNextTask(${next.playlistId},${next.sectionType})"
                )
            ) {
                return@withLock null
            }

            val claimed = next.copy(
                status = SyncStatus.RUNNING.name,
                stage = SyncStage.CONNECTING_AND_CHECKING.name,
                ownerWorkerId = workerId,
                heartbeatMs = now,
                lastAttemptedSyncMs = now,
                errorMessage = ""
            )
            dao.upsert(claimed)
            claimed
        }
    }

    /**
     * Inspects WorkManager's unique runner ([UNIQUE_RUNNER_NAME]):
     * - If called on the Main thread, dispatches onto [runnerWakeScope] (`Dispatchers.IO`) so
     *   UI ViewModels never block the main looper waiting on WorkManager's ListenableFuture.
     * - If already on a background thread (Worker or unit test), executes synchronously inline.
     */
    fun ensureRunnerActive(
        context: Context,
        forceWakeIfIdle: Boolean
    ) {
        val appContext = context.applicationContext
        lastAppContext = appContext

        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnerWakeScope.launch {
                ensureRunnerActiveInternal(appContext, forceWakeIfIdle)
            }
        } else {
            ensureRunnerActiveInternal(appContext, forceWakeIfIdle)
        }
    }

    private fun ensureRunnerActiveInternal(
        appContext: Context,
        forceWakeIfIdle: Boolean
    ) {
        runCatching {
            val wm = WorkManager.getInstance(appContext)
            val infos = runCatching {
                wm.getWorkInfosForUniqueWork(UNIQUE_RUNNER_NAME).get(2L, TimeUnit.SECONDS)
            }.getOrDefault(emptyList())

            val alreadyRunning = infos.any { info -> info.state == WorkInfo.State.RUNNING }
            if (alreadyRunning) {
                return
            }

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val requestBuilder = OneTimeWorkRequestBuilder<CatalogSyncWorker>()
                .setInputData(Data.EMPTY)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15L, TimeUnit.SECONDS)
                .addTag(UNIQUE_RUNNER_NAME)

            if (forceWakeIfIdle) {
                requestBuilder.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            }

            val policy = if (forceWakeIfIdle) {
                ExistingWorkPolicy.REPLACE
            } else {
                ExistingWorkPolicy.KEEP
            }

            wm.enqueueUniqueWork(UNIQUE_RUNNER_NAME, policy, requestBuilder.build())
        }.onFailure { err ->
            Log.w(TAG, "Unable to ensure CatalogSyncWorker active: ${err.message}")
        }
    }

    /**
     * Chains a follow-up [CatalogSyncWorker] using [ExistingWorkPolicy.APPEND_OR_REPLACE] so a
     * currently `RUNNING` worker can safely schedule a post-loop drain without hitting an
     * `alreadyRunning` no-op.
     */
    internal fun enqueueChainedDrainWorker(context: Context) {
        val appContext = context.applicationContext
        lastAppContext = appContext

        runCatching {
            val wm = WorkManager.getInstance(appContext)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<CatalogSyncWorker>()
                .setInputData(Data.EMPTY)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15L, TimeUnit.SECONDS)
                .addTag(UNIQUE_RUNNER_NAME)
                .build()

            wm.enqueueUniqueWork(
                UNIQUE_RUNNER_NAME,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                request
            )
        }.onFailure { err ->
            Log.w(TAG, "Unable to enqueue chained CatalogSyncWorker drain: ${err.message}")
        }
    }

    /**
     * Marks a claimed task as terminal (`COMPLETED`, `FAILED`, or `INTERRUPTED`) and releases
     * its lease atomically if it is still `RUNNING` under [workerId] (or unowned).
     */
    suspend fun markTaskTerminal(
        context: Context,
        playlistId: Long,
        sectionType: String,
        workerId: String?,
        terminalStatus: SyncStatus,
        errorMessage: String = ""
    ) = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        lastAppContext = appContext
        val normalizedSection = SyncTelemetry.normalizeSyncSectionType(sectionType)

        orchestrationMutex.withLock {
            val dao = StreamVaultDatabase.getInstance(appContext).syncSectionStateDao()
            val existing = dao.getSectionOnce(playlistId, normalizedSection) ?: return@withLock
            val currentStatus = CatalogTaskStateMachine.parseStatus(existing.status)

            // If another process/cancel already moved this row out of RUNNING (e.g., to IDLE or PENDING
            // or already terminal with released lease), do not corrupt it.
            if (currentStatus != SyncStatus.RUNNING) {
                return@withLock
            }
            if (!workerId.isNullOrBlank() &&
                existing.ownerWorkerId.isNotBlank() &&
                existing.ownerWorkerId != workerId
            ) {
                return@withLock
            }
            if (!CatalogTaskStateMachine.requireValidTransition(
                    currentStatus,
                    terminalStatus,
                    "markTaskTerminal($playlistId,$normalizedSection)"
                )
            ) {
                return@withLock
            }

            val now = System.currentTimeMillis()
            dao.upsert(
                existing.copy(
                    status = terminalStatus.name,
                    isIndeterminate = false,
                    progressPercent = if (terminalStatus == SyncStatus.COMPLETED) 100 else existing.progressPercent.coerceIn(0, 99),
                    lastSuccessfulSyncMs = if (terminalStatus == SyncStatus.COMPLETED) {
                        existing.lastSuccessfulSyncMs.takeIf { it > 0L } ?: now
                    } else {
                        existing.lastSuccessfulSyncMs
                    },
                    lastAttemptedSyncMs = now,
                    errorMessage = if (errorMessage.isNotBlank()) errorMessage else existing.errorMessage,
                    queuePriority = 99,
                    requestedCategoryId = "",
                    forceFullReconcile = false,
                    ownerWorkerId = "",
                    heartbeatMs = 0L
                )
            )
        }
    }

    suspend fun releaseLeaseIfStillRunning(
        context: Context,
        playlistId: Long,
        sectionType: String,
        workerId: String
    ) {
        markTaskTerminal(
            context = context,
            playlistId = playlistId,
            sectionType = sectionType,
            workerId = workerId,
            terminalStatus = SyncStatus.COMPLETED,
            errorMessage = ""
        )
    }

    suspend fun hasActiveOrPendingTasks(context: Context): Boolean = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        lastAppContext = appContext
        orchestrationMutex.withLock {
            val now = System.currentTimeMillis()
            val dao = StreamVaultDatabase.getInstance(appContext).syncSectionStateDao()
            dao.recoverStaleRunningTasks(
                staleBeforeMs = now - STALE_LEASE_TIMEOUT_MS,
                nowMs = now
            )
            dao.countActiveOrPending() > 0
        }
    }

    suspend fun hasPendingTasks(context: Context): Boolean = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        lastAppContext = appContext
        orchestrationMutex.withLock {
            val dao = StreamVaultDatabase.getInstance(appContext).syncSectionStateDao()
            dao.countPending() > 0
        }
    }

    suspend fun isSectionQueuedOrRunning(
        context: Context,
        playlistId: Long,
        sectionType: String
    ): Boolean = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        lastAppContext = appContext
        val normalizedSection = SyncTelemetry.normalizeSyncSectionType(sectionType)
        orchestrationMutex.withLock {
            val dao = StreamVaultDatabase.getInstance(appContext).syncSectionStateDao()
            val row = dao.getSectionOnce(playlistId, normalizedSection) ?: return@withLock false
            val status = CatalogTaskStateMachine.parseStatus(row.status)
            status == SyncStatus.PENDING || status == SyncStatus.RUNNING
        }
    }

    internal suspend fun clearForTest(context: Context? = lastAppContext) {
        val appContext = context?.applicationContext ?: return
        orchestrationMutex.withLock {
            runCatching {
                StreamVaultDatabase.getInstance(appContext)
                    .syncSectionStateDao()
                    .clearPendingAndRunningForTest()
            }
        }
    }
}
