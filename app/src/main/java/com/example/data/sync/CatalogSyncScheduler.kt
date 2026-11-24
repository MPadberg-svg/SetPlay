package com.example.data.sync

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.example.data.local.StreamVaultDatabase
import com.example.data.model.SyncSectionStateEntity
import com.example.data.repository.IptvRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/**
 * Priority tiers for the single serialized SetPlay catalog synchronization queue.
 * Lower [rank] executes first:
 * - P0 ([CATEGORY_REFRESH]): Explicit category-scoped pull-to-refresh.
 * - P1 ([LIVE_SYNC]): Live TV catalog synchronization.
 * - P2 ([VOD_SYNC]): Series & Movies synchronization (FIFO within P2).
 * - P3 ([BACKGROUND_CHECK]): Low-priority background sync jobs.
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
 * Centralized single-queue catalog synchronization scheduler.
 *
 * All section sync requests (`LIVE`, `MOVIE`, `SERIES`) flow through a single serialized
 * WorkManager chain ([UNIQUE_QUEUE_NAME]) so Movies, Series, and Live TV never download
 * or parse simultaneously against the same Xtream server.
 */
object CatalogSyncScheduler {
    private const val TAG = "CatalogSyncScheduler"
    const val UNIQUE_QUEUE_NAME = "setplay_catalog_sync_queue"
    const val STATUS_QUEUED_WAITING = "Queued (waiting for active sync…)"
    const val STATUS_QUEUED = "Queued…"

    private val schedulerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val sequenceGenerator = AtomicLong(0L)
    private val queueLock = Any()

    // Keyed by "${playlistId}_${normalizedSection}" for O(1) deduplication
    private val pendingQueue = LinkedHashMap<String, QueuedSyncRequest>()

    @Volatile
    private var activeRequestKey: String? = null

    fun defaultPriorityFor(sectionType: String, categoryId: String?): SyncPriority {
        if (!categoryId.isNullOrBlank()) return SyncPriority.CATEGORY_REFRESH
        return when (SyncTelemetry.normalizeSyncSectionType(sectionType)) {
            "LIVE" -> SyncPriority.LIVE_SYNC
            else -> SyncPriority.VOD_SYNC
        }
    }

    private fun dedupKey(playlistId: Long, normalizedSection: String): String {
        return "${playlistId}_$normalizedSection"
    }

    /**
     * Enqueues a section sync onto the single serialized [UNIQUE_QUEUE_NAME] WorkManager queue.
     *
     * - Deduplicates requests: if the exact `(playlistId, sectionType)` is already `RUNNING` or
     *   `PENDING`, skips enqueuing a duplicate (unless upgrading a queued request to higher
     *   priority / category-scoped / forceFull).
     * - Immediately persists `SyncStatus.PENDING` (`stage = "PENDING"`) on `sync_section_state`
     *   so UI immediately reflects queued status (including `"Queued (waiting for active sync…)"`
     *   when another section is currently `RUNNING`).
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
        val key = dedupKey(playlistId, normalizedSection)

        var shouldEnqueueWork = false
        var waitingForActiveSync = false
        var shouldReplaceIdleBackoff = false

        synchronized(queueLock) {
            if (activeRequestKey == key) {
                // Exact (playlistId, sectionType) is already RUNNING — deduplicate
                return UNIQUE_QUEUE_NAME
            }
            shouldReplaceIdleBackoff = userInitiated && activeRequestKey == null

            val existingPending = pendingQueue[key]
            if (existingPending != null) {
                // Deduplicate; only upgrade metadata in-place if higher priority or forceFull requested
                val upgradedPriority = if (priority.rank < existingPending.priority.rank) {
                    priority
                } else {
                    existingPending.priority
                }
                val upgradedCategory = cleanCategoryId ?: existingPending.categoryId
                val upgradedForce = existingPending.forceFull || forceFull
                pendingQueue[key] = existingPending.copy(
                    categoryId = upgradedCategory,
                    forceFull = upgradedForce,
                    priority = upgradedPriority
                )
                if (!shouldReplaceIdleBackoff) {
                    return UNIQUE_QUEUE_NAME
                }
                waitingForActiveSync = false
                shouldEnqueueWork = true
            } else {
                waitingForActiveSync = activeRequestKey != null || pendingQueue.isNotEmpty()
                val seq = sequenceGenerator.incrementAndGet()
                pendingQueue[key] = QueuedSyncRequest(
                    playlistId = playlistId,
                    sectionType = normalizedSection,
                    categoryId = cleanCategoryId,
                    forceFull = forceFull,
                    priority = priority,
                    enqueuedSeq = seq
                )
                shouldEnqueueWork = true
            }
        }

        if (!shouldEnqueueWork) return UNIQUE_QUEUE_NAME

        // Persist PENDING state immediately on IO so UI reflects queued status across screens/restarts
        schedulerScope.launch {
            persistPendingStateIfNotRunning(
                context = appContext,
                playlistId = playlistId,
                normalizedSection = normalizedSection,
                waitingHint = waitingForActiveSync
            )
        }

        val inputBuilder = Data.Builder()
            .putLong(CatalogSyncWorker.KEY_PLAYLIST_ID, playlistId)
            .putString(CatalogSyncWorker.KEY_SECTION_TYPE, normalizedSection)
            .putBoolean(CatalogSyncWorker.KEY_FORCE_FULL, forceFull)
            .putInt(CatalogSyncWorker.KEY_PRIORITY, priority.rank)
        if (cleanCategoryId != null) {
            inputBuilder.putString(CatalogSyncWorker.KEY_CATEGORY_ID, cleanCategoryId)
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val requestBuilder = OneTimeWorkRequestBuilder<CatalogSyncWorker>()
            .setInputData(inputBuilder.build())
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15L, TimeUnit.SECONDS)
            .addTag(UNIQUE_QUEUE_NAME)
            .addTag(CatalogSyncWorker.uniqueWorkName(playlistId, normalizedSection))

        if (expedited) {
            requestBuilder.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        }

        val workPolicy = if (shouldReplaceIdleBackoff) {
            ExistingWorkPolicy.REPLACE
        } else {
            ExistingWorkPolicy.KEEP
        }

        runCatching {
            WorkManager.getInstance(appContext).enqueueUniqueWork(
                UNIQUE_QUEUE_NAME,
                workPolicy,
                requestBuilder.build()
            )
        }.onFailure { err ->
            Log.w(TAG, "Unable to enqueue serialized WorkManager job for $key: ${err.message}")
        }

        return UNIQUE_QUEUE_NAME
    }

    /**
     * Race-free drain continuation: schedules a follow-up [CatalogSyncWorker] with empty
     * [Data.EMPTY] (no fallbackRequest) using [ExistingWorkPolicy.APPEND_OR_REPLACE] so
     * any request that arrived in the millisecond the previous worker exited its drain loop
     * is guaranteed to execute without ever re-running an already-completed section.
     */
    internal fun ensureDrainWorkerScheduled(context: Context) {
        val appContext = context.applicationContext
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val drainRequest = OneTimeWorkRequestBuilder<CatalogSyncWorker>()
            .setInputData(Data.EMPTY)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15L, TimeUnit.SECONDS)
            .addTag(UNIQUE_QUEUE_NAME)
            .build()

        runCatching {
            WorkManager.getInstance(appContext).enqueueUniqueWork(
                UNIQUE_QUEUE_NAME,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                drainRequest
            )
        }.onFailure { err ->
            Log.w(TAG, "Unable to schedule drain continuation worker: ${err.message}")
        }
    }

    /**
     * Pops the highest-priority pending request (P0 category -> P1 LIVE -> P2 SERIES/MOVIE FIFO
     * -> P3 background check) from the in-memory queue, falling back to persisted `PENDING` rows
     * in `sync_section_state` (for process-restart recovery) or the WorkManager [fallbackRequest].
     */
    internal suspend fun pollNextPendingRequest(
        context: Context,
        fallbackRequest: QueuedSyncRequest?
    ): QueuedSyncRequest? {
        synchronized(queueLock) {
            if (pendingQueue.isNotEmpty()) {
                var bestKey: String? = null
                var bestReq: QueuedSyncRequest? = null
                for ((k, req) in pendingQueue) {
                    if (bestReq == null ||
                        req.priority.rank < bestReq.priority.rank ||
                        (req.priority.rank == bestReq.priority.rank && req.enqueuedSeq < bestReq.enqueuedSeq)
                    ) {
                        bestKey = k
                        bestReq = req
                    }
                }
                if (bestKey != null && bestReq != null) {
                    pendingQueue.remove(bestKey)
                    activeRequestKey = bestKey
                    return bestReq
                }
            }
        }

        // Process-restart fallback: check persisted PENDING rows in sync_section_state for active playlist
        val dbFallback = runCatching {
            val db = StreamVaultDatabase.getInstance(context.applicationContext)
            val activePlaylist = db.playlistDao().getActivePlaylistOnce()
            if (activePlaylist != null) {
                val pendingRows = listOf("LIVE", "SERIES", "MOVIE").mapNotNull { sec ->
                    val row = db.syncSectionStateDao().getSectionOnce(activePlaylist.id, sec)
                    if (row != null && row.status.equals(SyncStatus.PENDING.name, ignoreCase = true)) {
                        QueuedSyncRequest(
                            playlistId = activePlaylist.id,
                            sectionType = sec,
                            categoryId = null,
                            forceFull = false,
                            priority = defaultPriorityFor(sec, null),
                            enqueuedSeq = row.lastAttemptedSyncMs
                        )
                    } else {
                        null
                    }
                }
                pendingRows.minWithOrNull(
                    compareBy<QueuedSyncRequest> { it.priority.rank }
                        .thenBy { it.enqueuedSeq }
                )
            } else {
                null
            }
        }.getOrNull()

        if (dbFallback != null) {
            val key = dedupKey(dbFallback.playlistId, dbFallback.sectionType)
            synchronized(queueLock) {
                activeRequestKey = key
            }
            return dbFallback
        }

        if (fallbackRequest != null && fallbackRequest.playlistId > 0L && fallbackRequest.sectionType.isNotBlank()) {
            val normalized = SyncTelemetry.normalizeSyncSectionType(fallbackRequest.sectionType)
            val key = dedupKey(fallbackRequest.playlistId, normalized)
            synchronized(queueLock) {
                activeRequestKey = key
            }
            return fallbackRequest.copy(sectionType = normalized)
        }

        return null
    }

    internal fun markRequestFinished(playlistId: Long, sectionType: String) {
        val key = dedupKey(playlistId, SyncTelemetry.normalizeSyncSectionType(sectionType))
        synchronized(queueLock) {
            if (activeRequestKey == key) {
                activeRequestKey = null
            }
        }
    }

    internal fun hasPendingRequests(): Boolean {
        synchronized(queueLock) {
            return pendingQueue.isNotEmpty()
        }
    }

    internal fun isSectionQueuedOrRunning(playlistId: Long, sectionType: String): Boolean {
        val key = dedupKey(playlistId, SyncTelemetry.normalizeSyncSectionType(sectionType))
        synchronized(queueLock) {
            return activeRequestKey == key || pendingQueue.containsKey(key)
        }
    }

    internal fun clearForTest() {
        synchronized(queueLock) {
            pendingQueue.clear()
            activeRequestKey = null
        }
    }

    private suspend fun persistPendingStateIfNotRunning(
        context: Context,
        playlistId: Long,
        normalizedSection: String,
        waitingHint: Boolean
    ) {
        runCatching {
            val db = StreamVaultDatabase.getInstance(context)
            val dao = db.syncSectionStateDao()
            val existing = dao.getSectionOnce(playlistId, normalizedSection)
            if (existing != null && existing.status.equals(SyncStatus.RUNNING.name, ignoreCase = true)) {
                return
            }

            var otherRunning = waitingHint
            if (!otherRunning) {
                for (sec in listOf("LIVE", "MOVIE", "SERIES")) {
                    if (sec != normalizedSection) {
                        val row = dao.getSectionOnce(playlistId, sec)
                        if (row != null && row.status.equals(SyncStatus.RUNNING.name, ignoreCase = true)) {
                            otherRunning = true
                            break
                        }
                    }
                }
            }

            val queuedText = if (otherRunning) STATUS_QUEUED_WAITING else STATUS_QUEUED
            val nowMs = System.currentTimeMillis()
            val entity = SyncSectionStateEntity(
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
                lastAttemptedSyncMs = nowMs,
                pendingUpdateAvailable = false,
                pendingUpdateSummary = queuedText,
                etag = existing?.etag.orEmpty(),
                lastModified = existing?.lastModified.orEmpty(),
                lastContentLength = existing?.lastContentLength ?: -1L,
                errorMessage = ""
            )
            dao.upsert(entity)
            IptvRepository.getInstance(context).notifySectionQueued(
                sectionType = normalizedSection,
                statusText = queuedText
            )
        }
    }
}
