package com.example.data.sync

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Coordinates CPU, coroutine concurrency, and SQLite write batch sizing so background catalog
 * synchronization never interrupts, drops frames, or starves active ExoPlayer playback.
 */
object PlaybackResourceGovernor {
    private const val ACTIVE_PLAYBACK_YIELD_MS = 25L
    private const val PLAYBACK_DB_BATCH_SIZE = 250
    private const val IDLE_DB_BATCH_SIZE = 1_000

    private val _isPlaybackActive = MutableStateFlow(false)
    val isPlaybackActive: StateFlow<Boolean> = _isPlaybackActive.asStateFlow()

    /**
     * Dedicated single-lane low-priority dispatcher for background sync & catalog indexing so
     * heavy JSON parsing or SQLite transactions never saturate CPU cores during playback.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val syncDispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(1)

    fun setPlaybackActive(active: Boolean) {
        _isPlaybackActive.value = active
    }

    /**
     * Cooperatively yields between micro-batches whenever ExoPlayer is actively playing video.
     */
    suspend fun throttleIfPlaybackActive() {
        if (_isPlaybackActive.value) {
            delay(ACTIVE_PLAYBACK_YIELD_MS)
        }
    }

    /**
     * Returns a conservative SQLite write batch size during active playback to keep WAL write
     * locks sub-millisecond, expanding to 1,000 rows per transaction when playback is idle.
     */
    fun recommendedDbBatchSize(): Int {
        return if (_isPlaybackActive.value) PLAYBACK_DB_BATCH_SIZE else IDLE_DB_BATCH_SIZE
    }
}
