# SetPlay Task Orchestration Rules (Mandatory Engineering Contract)

This document defines the non-negotiable concurrency, state-machine, and task-scheduling invariants for SetPlay's catalog synchronization engine (`app/src/main/java/com/example/data/sync/` and `IptvRepository`). Every developer and coding agent modifying sync, playlist lifecycle, or ViewModel refresh triggers **MUST** obey these rules.

---

## 1. The 8 Task-Management Invariants

1. **Single Source of Truth (SSOT) in Room (`sync_section_state`, Room v9)**
   - The `sync_section_state` Room table is the **only** queue store.
   - Task metadata (`status`, `queuePriority`, `queueSequence`, `requestedCategoryId`, `forceFullReconcile`, `ownerWorkerId`, `heartbeatMs`) lives in SQLite behind `CatalogTaskOrchestrator.orchestrationMutex`.
   - Zero RAM-only pending queues (`pendingQueue`, `activeRequestKey`) are permitted anywhere in the codebase.

2. **Strict Finite State Machine (`CatalogTaskStateMachine`)**
   - Every status mutation must pass `CatalogTaskStateMachine.requireValidTransition(from, to)`.
   - Allowed transitions:
     - `IDLE` → `{ PENDING, IDLE }`
     - `PENDING` → `{ RUNNING, PENDING, IDLE, FAILED }`
     - `RUNNING` → `{ RUNNING, COMPLETED, INTERRUPTED, FAILED, IDLE }` (**`RUNNING → PENDING` is strictly forbidden**)
     - `COMPLETED | INTERRUPTED | FAILED` → `{ PENDING, IDLE }`
   - Illegal transitions must be rejected and logged — never silently persisted.

3. **Deterministic Priority Hierarchy (`TaskPriority`)**
   - Tasks drain strictly by `ORDER BY queuePriority ASC, queueSequence ASC`:
     - `P0` — `USER_RESUME` (`rank = 0`): Explicit user Resume of an `INTERRUPTED` or `FAILED` section; always preempts waiting sections.
     - `P1` — `CATEGORY_REFRESH` (`rank = 1`): Explicit category-scoped pull-to-refresh.
     - `P2` — `USER_SECTION_SYNC` (`rank = 2`): Explicit user-triggered full section sync.
     - `P3` — `INITIAL_ACCOUNT_SYNC` (`rank = 3`): Onboarding / full account sync (`LIVE` → `SERIES` → `MOVIE` via monotonic `queueSequence`).
     - `P4` — `BACKGROUND_CHECK` (`rank = 4`): Low-priority periodic re-rank / background refresh.
   - Re-enqueuing an already-`PENDING` task upgrades `queuePriority` in-place if the new rank is higher (smaller integer) and merges `requestedCategoryId` / `forceFullReconcile`.

4. **Single-Lane Serialized Execution (`CatalogSyncWorker`)**
   - Only one `CatalogSyncWorker` runs at a time under `UNIQUE_RUNNER_NAME = "setplay_catalog_sync_queue"`.
   - `ensureRunnerActive(...)` always passes `Data.EMPTY` so the worker claims tasks exclusively from Room via `CatalogTaskOrchestrator.claimNextTask(...)`.
   - If a worker is already `RUNNING`, `ensureRunnerActive` never replaces or interrupts it.
   - A failure in one section marks that section `FAILED` (or `INTERRUPTED` for transient network errors) and **continues** the drain loop so remaining `PENDING` sections are never stranded.

5. **Lease Ownership & 45-Second Heartbeat Watchdog**
   - `claimNextTask(context, workerId)` atomically persists `status = RUNNING`, `ownerWorkerId = workerId`, and `heartbeatMs = now`.
   - While executing a claimed task, `CatalogSyncWorker` updates `heartbeatMs` every **5 seconds** via `SyncSectionStateDao.updateHeartbeat(...)`.
   - Before enqueuing or claiming tasks, `CatalogTaskOrchestrator` runs `recoverStaleRunningTasks(now - 45_000L, now)` so any worker killed by the OS or crashed mid-sync is automatically transitioned to `INTERRUPTED` with its lease cleared, unblocking the queue.

6. **Account-Switch & Deletion Cancellation**
   - Switching active playlists (`switchPlaylist`, `syncSpecificPlaylist`, `addCustomPlaylist`, `applySyncedVaultFromPeer`) or deleting a playlist (`deletePlaylist`) **MUST** call `CatalogTaskOrchestrator.cancelPlaylistTasks(context, previousPlaylistId)` before activating or enqueueing the new playlist.
   - Cancelled tasks reset cleanly to `IDLE` in Room and cancel the active WorkManager runner if owned by the cancelled playlist.

7. **Playback Isolation**
   - Streaming playback uses dedicated `IptvRepository.playbackOkHttpClient`; catalog sync uses isolated `IptvRepository.syncOkHttpClient` and throttles via `PlaybackResourceGovernor`.
   - `CatalogUpdateCheckWorker` checks `CatalogTaskOrchestrator.hasActiveOrPendingTasks(applicationContext)` on entry and exits immediately with `Result.success()` whenever user/account sync work is active or pending.

8. **Zero Fire-and-Forget State Writes**
   - Room upserts (`PENDING`, `RUNNING`, terminal transitions) must commit synchronously inside `CatalogTaskOrchestrator` **before** calling `ensureRunnerActive(...)` or returning to callers.
   - No background `schedulerScope.launch { dao.upsert(...) }` writes that can race with `CatalogSyncWorker` startup.

---

## 2. Forbidden Anti-Patterns Checklist

Before merging any change, verify none of the following exist:

- [ ] **NEVER** call `repository.syncPlaylistSection(...)` or `repository.rebuildVodCatalogGroups(...)` directly from a `ViewModel` or Composable.
- [ ] **NEVER** run a competing inline `for (sec in listOf("LIVE", "SERIES", "MOVIE"))` loop inside `viewModelScope` or outside `CatalogSyncWorker`.
- [ ] **NEVER** store queued/active sync state in RAM maps (`LinkedHashMap`, `ConcurrentHashMap`, `@Volatile activeKey`).
- [ ] **NEVER** transition `RUNNING → PENDING` or overwrite an active `RUNNING` task on duplicate tap / resume.
- [ ] **NEVER** bypass `CatalogTaskStateMachine.requireValidTransition(...)` when persisting `sync_section_state` updates.
- [ ] **NEVER** return `Result.retry()` from `CatalogSyncWorker` while other `PENDING` tasks remain in Room (prevents WorkManager exponential backoff from blocking the entire queue).
- [ ] **NEVER** fire asynchronous, un-awaited DB writes to mark `PENDING` after waking WorkManager.
- [ ] **NEVER** omit `cancelPlaylistTasks(...)` when switching or deleting a playlist.

---

## 3. Required Regression Test Gate

Any PR or change touching `data/sync/`, `StreamVaultDao.kt`, `StreamVaultDatabase.kt`, `IptvRepository.kt`, or ViewModel sync triggers **MUST** pass 100% of the following tests in `app/src/test/java/com/example/ExampleRobolectricTest.kt` plus both Phone and TV APK debug builds:

1. **User Reproduction (Interrupted Movie Resume)**: Account sync (`LIVE`, `SERIES`, `MOVIE`) → complete `LIVE` → interrupt `MOVIE` while `SERIES` is `PENDING` → `resumeSection(MOVIE)` gets `P0` (`USER_RESUME`), claims ahead of `SERIES`, and `SERIES` drains right after `MOVIE`.
2. **Strict FSM Guard**: `RUNNING → PENDING` is rejected; `PENDING → RUNNING → INTERRUPTED → PENDING → RUNNING → COMPLETED` succeeds.
3. **Repeated Resume / Double-Tap Idempotency**: 5 rapid `resumeSection` / `enqueueSection` calls create 0 duplicates and never corrupt a `RUNNING` row.
4. **45-Second Stale Lease Watchdog**: A `RUNNING` row with `heartbeatMs < now - 45_000L` recovers to `INTERRUPTED` and unblocks the next `PENDING` task.
5. **Failure Isolation**: A `FAILED` section never strands subsequent `PENDING` sections.
6. **Account Switch Cancellation**: Switching from Playlist 1 to Playlist 2 cancels Playlist 1's `PENDING`/`RUNNING` rows before enqueueing Playlist 2.
7. **Resume While Another Task Is Running**: Resuming `MOVIE` while `LIVE` is `RUNNING` keeps `LIVE` running and promotes `MOVIE` to `P0` ahead of `P3 SERIES`.

Verification commands:
```bash
./gradlew :app:testDebugUnitTest assembleDebug -PSETPLAY_TARGET=PHONE
./gradlew assembleDebug -PSETPLAY_TARGET=TV
```
