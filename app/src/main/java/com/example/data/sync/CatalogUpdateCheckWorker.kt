package com.example.data.sync

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.data.local.StreamVaultDatabase
import com.example.data.model.PlaylistEntity
import com.example.data.remote.GlobalPopularityIndexManager
import com.example.data.remote.XtreamClient
import com.example.data.repository.IptvRepository
import com.example.util.SecurityManager
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Lightweight periodic background probe (default every 12 hours) that detects catalog updates
 * without ever downloading full stream catalogs in the background:
 * 1. Fetches only tiny category lists (~10–30 KB) per section and compares IDs against Room.
 * 2. Performs conditional `HEAD` / `If-None-Match` probes when an ETag or Content-Length is cached.
 * 3. Refreshes the V15 Global Popularity Index at most once per 24h and silently re-ranks local
 *    `vod_catalog_groups` when playback is inactive.
 * 4. Flags `sync_section_state.pendingUpdateAvailable = true` with a summary and posts a single
 *    non-intrusive notification when new updates are detected.
 */
class CatalogUpdateCheckWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private data class ProbeSpec(
        val sectionType: String,
        val label: String,
        val catAction: String,
        val streamAction: String
    )

    override suspend fun doWork(): Result = withContext(PlaybackResourceGovernor.syncDispatcher) {
        if (CatalogTaskOrchestrator.hasActiveOrPendingTasks(applicationContext)) {
            Log.d(TAG, "Skipping background update probe while catalog sync tasks are active or pending")
            return@withContext Result.success()
        }

        val repository = IptvRepository.getInstance(applicationContext)
        val playlist = repository.getActivePlaylistOnce()
            ?: return@withContext Result.success()

        if (playlist.serverUrl.isBlank() || playlist.username.isBlank()) {
            return@withContext Result.success()
        }

        val db = StreamVaultDatabase.getInstance(applicationContext)
        val syncStateDao = db.syncSectionStateDao()
        val xtreamClient = XtreamClient(IptvRepository.syncOkHttpClient)
        val plainPassword = SecurityManager.decryptCredential(playlist.password)

        val specs = listOf(
            ProbeSpec("LIVE", "Live TV", "get_live_categories", "get_live_streams"),
            ProbeSpec("MOVIE", "Movies", "get_vod_categories", "get_vod_streams"),
            ProbeSpec("SERIES", "Series", "get_series_categories", "get_series")
        )

        val detectedSummaries = ArrayList<String>()
        var encounteredTransientIo = false

        for (spec in specs) {
            PlaybackResourceGovernor.throttleIfPlaybackActive()
            try {
                val (remoteCatsRaw, _) = xtreamClient.fetchCategoriesWithAdultIds(
                    serverUrl = playlist.serverUrl,
                    username = playlist.username,
                    password = plainPassword,
                    action = spec.catAction,
                    categoryType = spec.sectionType,
                    playlistId = playlist.id
                )
                val remoteCatIds = HashSet<String>(remoteCatsRaw.size * 2 + 8)
                for (cat in remoteCatsRaw) {
                    if (!SecurityManager.isAdultContent(cat.categoryName) && cat.categoryId.isNotBlank()) {
                        remoteCatIds.add(cat.categoryId)
                    }
                }

                val localCats = repository.getCategoriesOnce(playlist.id, spec.sectionType)
                val localCatIds = HashSet<String>(localCats.size * 2 + 8)
                for (cat in localCats) {
                    if (cat.categoryId.isNotBlank()) {
                        localCatIds.add(cat.categoryId)
                    }
                }

                var newCategoryCount = 0
                if (localCatIds.isNotEmpty()) {
                    for (rid in remoteCatIds) {
                        if (rid !in localCatIds) {
                            newCategoryCount++
                        }
                    }
                }

                val savedState = syncStateDao.getSectionOnce(playlist.id, spec.sectionType)
                val headChanged = probeConditionalHead(
                    playlist = playlist,
                    plainPassword = plainPassword,
                    streamAction = spec.streamAction,
                    savedEtag = savedState?.etag.orEmpty(),
                    savedContentLength = savedState?.lastContentLength ?: -1L
                )

                val summary = when {
                    newCategoryCount == 1 -> "1 new category available"
                    newCategoryCount > 1 -> "$newCategoryCount new categories available"
                    headChanged -> "Catalog updates available"
                    else -> ""
                }

                if (summary.isNotEmpty()) {
                    repository.markPendingUpdate(
                        playlistId = playlist.id,
                        sectionType = spec.sectionType,
                        summary = summary
                    )
                    detectedSummaries.add("${spec.label}: $summary")
                }
            } catch (ioe: IOException) {
                encounteredTransientIo = true
                Log.w(TAG, "Lightweight probe IO error for ${spec.sectionType}: ${ioe.message}")
            } catch (e: Exception) {
                Log.w(TAG, "Lightweight probe failed for ${spec.sectionType}: ${e.message}")
            }
        }

        // Refresh V15 Global Popularity Index at most once per 24h; route re-rank/refresh through
        // the single serialized CatalogSyncScheduler instead of launching parallel rebuilds.
        val cloudIndexUpdated = runCatching {
            GlobalPopularityIndexManager.ensureLoaded(applicationContext, IptvRepository.syncOkHttpClient)
        }.getOrDefault(false)

        if (cloudIndexUpdated && !PlaybackResourceGovernor.isPlaybackActive.value) {
            runCatching {
                if (repository.countStreamsByType(playlist.id, "movie") > 0 &&
                    !PlaybackResourceGovernor.isPlaybackActive.value
                ) {
                    CatalogTaskOrchestrator.enqueueSection(
                        context = applicationContext,
                        playlistId = playlist.id,
                        sectionType = "MOVIE",
                        priority = TaskPriority.BACKGROUND_CHECK
                    )
                }
                if (repository.countStreamsByType(playlist.id, "series") > 0 &&
                    !PlaybackResourceGovernor.isPlaybackActive.value
                ) {
                    CatalogTaskOrchestrator.enqueueSection(
                        context = applicationContext,
                        playlistId = playlist.id,
                        sectionType = "SERIES",
                        priority = TaskPriority.BACKGROUND_CHECK
                    )
                }
            }
        }

        if (detectedSummaries.isNotEmpty()) {
            postUpdateNotification(applicationContext, detectedSummaries)
        }

        if (encounteredTransientIo && detectedSummaries.isEmpty() && runAttemptCount < 2) {
            Result.retry()
        } else {
            Result.success()
        }
    }

    /**
     * Performs a zero-body HTTP `HEAD` check with `If-None-Match` when a previous `ETag` or
     * `Content-Length` is stored for this section. Returns `true` only if the remote server
     * explicitly reports a different ETag or a different positive Content-Length.
     */
    private fun probeConditionalHead(
        playlist: PlaylistEntity,
        plainPassword: String,
        streamAction: String,
        savedEtag: String,
        savedContentLength: Long
    ): Boolean {
        if (savedEtag.isBlank() && savedContentLength <= 0L) {
            return false
        }
        val targetUrl = XtreamClient.buildPlayerApiUrl(
            serverUrl = playlist.serverUrl,
            username = playlist.username,
            password = plainPassword,
            queryParams = mapOf("action" to streamAction)
        ) ?: return false

        val reqBuilder = Request.Builder()
            .url(targetUrl)
            .head()
            .header("User-Agent", PROBE_USER_AGENT)
        if (savedEtag.isNotBlank()) {
            reqBuilder.header("If-None-Match", savedEtag)
        }

        return try {
            IptvRepository.syncOkHttpClient.newCall(reqBuilder.build()).execute().use { response ->
                when (response.code) {
                    304 -> false
                    in 200..299 -> {
                        val remoteEtag = response.header("ETag")?.trim().orEmpty()
                        val remoteContentLength = response.header("Content-Length")?.trim()?.toLongOrNull() ?: -1L
                        val etagChanged = savedEtag.isNotBlank() && remoteEtag.isNotBlank() && remoteEtag != savedEtag
                        val lengthChanged = savedContentLength > 0L && remoteContentLength > 0L && remoteContentLength != savedContentLength
                        etagChanged || lengthChanged
                    }
                    else -> false
                }
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun postUpdateNotification(context: Context, sectionSummaries: List<String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                if (manager != null) {
                    val channel = NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        "Catalog Updates",
                        NotificationManager.IMPORTANCE_LOW
                    ).apply {
                        description = "Notifies when new Live TV, Movies, or Series categories are detected"
                        setShowBadge(false)
                    }
                    manager.createNotificationChannel(channel)
                }
            }

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
            val contentIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID,
                launchIntent,
                pendingIntentFlags
            )

            val contentText = sectionSummaries.joinToString(" • ")
            val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_sync_noanim)
                .setContentTitle("SetPlay catalog updates available")
                .setContentText(contentText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(contentIntent)
                .build()

            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }.onFailure { err ->
            Log.w(TAG, "Skipped posting catalog update notification: ${err.message}")
        }
    }

    companion object {
        private const val TAG = "CatalogUpdateCheck"
        private const val PROBE_USER_AGENT = "SetPlay-IPTV/3.0"

        const val UNIQUE_WORK_NAME = "setplay_catalog_update_check"
        const val PREFS_NAME = "setplay_sync_prefs"
        const val KEY_INTERVAL_HOURS = "intervalHours"
        const val KEY_WIFI_ONLY = "wifiOnly"
        const val DEFAULT_INTERVAL_HOURS = 12L
        const val NOTIFICATION_CHANNEL_ID = "setplay_catalog_updates"
        const val NOTIFICATION_ID = 5042

        val SUPPORTED_INTERVAL_HOURS = setOf(0L, 6L, 12L, 24L)

        fun normalizeIntervalHours(intervalHours: Long): Long {
            return when {
                intervalHours <= 0L -> 0L
                intervalHours <= 6L -> 6L
                intervalHours <= 12L -> 12L
                else -> 24L
            }
        }

        fun getConfiguredIntervalHours(context: Context): Long {
            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val stored = prefs.getLong(KEY_INTERVAL_HOURS, DEFAULT_INTERVAL_HOURS)
            return normalizeIntervalHours(stored)
        }

        fun isWifiOnlyConfigured(context: Context): Boolean {
            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_WIFI_ONLY, false)
        }

        /**
         * Persists user auto-update check preferences (`intervalHours` in `{0, 6, 12, 24}`,
         * `wifiOnly`) and schedules or cancels the unique periodic [WorkManager] probe.
         * When `intervalHours == 0L` (manual only), cancels the scheduled periodic work.
         */
        fun schedulePeriodicUpdateCheck(
            context: Context,
            intervalHours: Long = getConfiguredIntervalHours(context),
            wifiOnly: Boolean = isWifiOnlyConfigured(context)
        ) {
            val appCtx = context.applicationContext
            val normalizedHours = normalizeIntervalHours(intervalHours)

            appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putLong(KEY_INTERVAL_HOURS, normalizedHours)
                .putBoolean(KEY_WIFI_ONLY, wifiOnly)
                .apply()

            runCatching {
                val workManager = WorkManager.getInstance(appCtx)
                if (normalizedHours == 0L) {
                    workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
                } else {
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(
                            if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
                        )
                        .setRequiresBatteryNotLow(true)
                        .build()

                    val request = PeriodicWorkRequestBuilder<CatalogUpdateCheckWorker>(
                        normalizedHours,
                        TimeUnit.HOURS
                    )
                        .setConstraints(constraints)
                        .addTag(UNIQUE_WORK_NAME)
                        .build()

                    workManager.enqueueUniquePeriodicWork(
                        UNIQUE_WORK_NAME,
                        ExistingPeriodicWorkPolicy.UPDATE,
                        request
                    )
                }
            }.onFailure { err ->
                Log.w(TAG, "Unable to schedule periodic update check: ${err.message}")
            }
        }
    }
}
