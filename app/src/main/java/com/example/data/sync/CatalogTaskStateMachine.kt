package com.example.data.sync

import android.util.Log
import java.util.Locale

/**
 * Unified priority hierarchy for the Room-backed SetPlay catalog task orchestrator.
 * Lower [rank] always drains first:
 * - 0 ([USER_RESUME]): Explicit user resume of an interrupted or failed sync.
 * - 1 ([CATEGORY_REFRESH]): Explicit category-scoped refresh.
 * - 2 ([USER_SECTION_SYNC]): Explicit user-triggered section refresh or Live TV priority sync.
 * - 3 ([INITIAL_ACCOUNT_SYNC]): Full account onboarding sync (LIVE -> SERIES -> MOVIE).
 * - 4 ([BACKGROUND_CHECK]): Background periodic refresh / re-ranking.
 */
enum class TaskPriority(val rank: Int) {
    USER_RESUME(0),
    CATEGORY_REFRESH(1),
    USER_SECTION_SYNC(2),
    INITIAL_ACCOUNT_SYNC(3),
    BACKGROUND_CHECK(4)
}

/**
 * Strict Finite State Machine (FSM) governing all `sync_section_state.status` transitions.
 *
 * Strictly forbids illegal transitions such as `RUNNING -> PENDING` (which previously caused
 * queue corruption and split-brain duplicate runs) or stale worker writes overwriting `IDLE`
 * or `PENDING` rows after cancellation/re-enqueue.
 */
object CatalogTaskStateMachine {
    private const val TAG = "CatalogTaskStateMachine"

    fun parseStatus(raw: String?): SyncStatus {
        if (raw.isNullOrBlank()) return SyncStatus.IDLE
        return try {
            SyncStatus.valueOf(raw.trim().uppercase(Locale.US))
        } catch (_: Exception) {
            SyncStatus.IDLE
        }
    }

    /**
     * Authoritative transition table:
     * - `IDLE` -> `{ PENDING, IDLE }`
     * - `PENDING` -> `{ RUNNING, PENDING, IDLE, FAILED }`
     * - `RUNNING` -> `{ RUNNING, COMPLETED, INTERRUPTED, FAILED, IDLE }` (FORBIDS `RUNNING -> PENDING`)
     * - `COMPLETED | INTERRUPTED | FAILED` -> `{ PENDING, IDLE }`
     */
    fun canTransition(from: SyncStatus, to: SyncStatus): Boolean {
        return when (from) {
            SyncStatus.IDLE -> to == SyncStatus.PENDING || to == SyncStatus.IDLE
            SyncStatus.PENDING -> to == SyncStatus.RUNNING ||
                to == SyncStatus.PENDING ||
                to == SyncStatus.IDLE ||
                to == SyncStatus.FAILED
            SyncStatus.RUNNING -> to == SyncStatus.RUNNING ||
                to == SyncStatus.COMPLETED ||
                to == SyncStatus.INTERRUPTED ||
                to == SyncStatus.FAILED ||
                to == SyncStatus.IDLE
            SyncStatus.COMPLETED,
            SyncStatus.INTERRUPTED,
            SyncStatus.FAILED -> to == SyncStatus.PENDING || to == SyncStatus.IDLE
        }
    }

    /**
     * Validates a state transition, logging a warning and returning `false` if illegal
     * so callers abort corrupted writes instead of mutating Room state.
     */
    fun requireValidTransition(
        from: SyncStatus,
        to: SyncStatus,
        contextTag: String = ""
    ): Boolean {
        if (canTransition(from, to)) return true
        val detail = if (contextTag.isNotBlank()) " [$contextTag]" else ""
        Log.w(TAG, "Blocked illegal sync state transition: $from -> $to$detail")
        return false
    }
}
