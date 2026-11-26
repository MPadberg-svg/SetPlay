package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.data.repository.DataFreshness
import com.example.data.repository.FootballMatch
import com.example.data.repository.FootballRepository
import com.example.data.repository.MatchDayTab
import com.example.data.repository.MatchStatus
import com.example.data.repository.UserCountryZone
import com.example.data.repository.UserZoneRegistry
import com.example.data.repository.formatDynamicKickoffTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.util.Locale

/**
 * Compact canonical fixture record loaded from SetPlay's 6-hourly static GitHub Pages
 * schedule window (`docs/kickoff/schedule_window.json`).
 */
data class StaticKickoffFixture(
    val canonicalId: String,
    val tournamentId: String,
    val tournamentName: String,
    val tournamentRegion: String,
    val kickoffEpochMs: Long,
    val status: String,
    val liveMinuteOrBadge: String,
    val homeTeamId: String,
    val homeTeamName: String,
    val homeTeamBadge: String,
    val homeScore: Int?,
    val awayTeamId: String,
    val awayTeamName: String,
    val awayTeamBadge: String,
    val awayScore: Int?,
    val stadiumName: String,
    val espnEventId: String,
    val sourceProvider: String
)

/**
 * Va2 ETag-cached GitHub Pages static Kickoff schedule client.
 *
 * Performs a 100% read-only HTTP GET with `If-None-Match` ETag validation (0-byte HTTP 304
 * when unchanged) at most once every 2 hours, caching the 8-day schedule window locally in
 * `context.filesDir/kickoff_schedule_window.json`. Fails silently when offline or before the
 * first GitHub Actions run has published the snapshot.
 */
object KickoffStaticIndexManager {
    const val DEFAULT_SCHEDULE_URL = "https://MPadberg-svg.github.io/SetPlay/kickoff/schedule_window.json"

    private const val TAG = "KickoffStaticIndex"
    private const val CACHE_FILE_NAME = "kickoff_schedule_window.json"
    private const val META_FILE_NAME = "kickoff_schedule_window.meta"
    private const val REFRESH_INTERVAL_MS = 2L * 60L * 60L * 1000L
    private const val STALE_SNAPSHOT_THRESHOLD_MS = 12L * 60L * 60L * 1000L
    private const val PLACEHOLDER_HOST_MARKER = "YOUR_GITHUB_USERNAME"

    private data class ScheduleSnapshot(
        val schemaVersion: Int = 1,
        val generatedAtEpochMs: Long = 0L,
        val windowStartDate: String = "",
        val windowEndDate: String = "",
        val days: Map<String, List<StaticKickoffFixture>> = emptyMap()
    )

    @Volatile
    private var snapshot: ScheduleSnapshot = ScheduleSnapshot()

    @Volatile
    private var diskLoaded: Boolean = false

    private val loadMutex = Mutex()

    /**
     * Hydrates the static schedule window from `context.filesDir/kickoff_schedule_window.json`
     * if present, and refreshes from [url] at most once every 2 hours using `If-None-Match`.
     * Always fails silently without throwing when offline, unconfigured, or receiving 404/5xx.
     *
     * @return `true` ONLY when a fresh HTTP 200 payload was downloaded and parsed cleanly; `false` otherwise.
     */
    suspend fun ensureLoaded(
        context: Context,
        okHttpClient: OkHttpClient,
        url: String = DEFAULT_SCHEDULE_URL
    ): Boolean = withContext(Dispatchers.IO) {
        loadMutex.withLock {
            val cacheFile = File(context.filesDir, CACHE_FILE_NAME)
            val metaFile = File(context.filesDir, META_FILE_NAME)

            // 1. Hydrate from on-disk cache once per process lifetime
            if (!diskLoaded && cacheFile.exists() && cacheFile.length() > 2L) {
                runCatching {
                    val cachedJson = cacheFile.readText(Charsets.UTF_8)
                    val parsed = parseScheduleWindowInternal(cachedJson)
                    if (parsed != null) {
                        snapshot = parsed
                    }
                }.onFailure { err ->
                    Log.w(TAG, "Failed reading cached kickoff schedule window: ${err.message}")
                }
                diskLoaded = true
            }

            // 2. Skip network fetch when URL is blank or still has the template placeholder
            val cleanUrl = url.trim()
            if (cleanUrl.isEmpty() || cleanUrl.contains(PLACEHOLDER_HOST_MARKER, ignoreCase = true)) {
                return@withLock false
            }

            // 3. Respect 2-hour refresh throttle
            val (savedEtag, lastFetchMs) = readMeta(metaFile)
            val nowMs = System.currentTimeMillis()
            if (lastFetchMs > 0L && (nowMs - lastFetchMs) < REFRESH_INTERVAL_MS && cacheFile.exists()) {
                return@withLock false
            }

            // 4. Perform strictly read-only GET with ETag caching (HTTP 304 = 0-byte transfer)
            var downloadedFresh = false
            runCatching {
                val reqBuilder = Request.Builder()
                    .url(cleanUrl)
                    .get()
                if (savedEtag.isNotBlank() && cacheFile.exists()) {
                    reqBuilder.header("If-None-Match", savedEtag)
                }

                okHttpClient.newCall(reqBuilder.build()).execute().use { response ->
                    when (response.code) {
                        304 -> {
                            val updatedEtag = response.header("ETag")?.trim().orEmpty().ifBlank { savedEtag }
                            writeMeta(metaFile, updatedEtag, nowMs)
                        }
                        200 -> {
                            val bodyStr = response.body?.string().orEmpty()
                            if (bodyStr.isNotBlank()) {
                                val parsed = parseScheduleWindowInternal(bodyStr)
                                if (parsed != null && parsed.days.values.any { it.isNotEmpty() }) {
                                    val tempFile = File(context.filesDir, "$CACHE_FILE_NAME.tmp")
                                    tempFile.writeText(bodyStr, Charsets.UTF_8)
                                    if (!tempFile.renameTo(cacheFile)) {
                                        cacheFile.writeText(bodyStr, Charsets.UTF_8)
                                        tempFile.delete()
                                    }
                                    val newEtag = response.header("ETag")?.trim().orEmpty()
                                    writeMeta(metaFile, newEtag, nowMs)
                                    snapshot = parsed
                                    diskLoaded = true
                                    downloadedFresh = true
                                }
                            }
                        }
                        else -> {
                            // Fail silently (e.g., 404 before first GitHub Actions run)
                        }
                    }
                }
            }.onFailure {
                // Offline or transient network error — fail silently to existing/empty snapshot
            }
            downloadedFresh
        }
    }

    /**
     * Parses a canonical `schedule_window.json` payload into a map of `YYYY-MM-DD -> List<StaticKickoffFixture>`.
     * Exposed internally for deterministic offline unit testing.
     */
    internal fun parseScheduleWindowJson(rawJson: String): Map<String, List<StaticKickoffFixture>> {
        return parseScheduleWindowInternal(rawJson)?.days ?: emptyMap()
    }

    /**
     * Deterministic offline test helper that loads a `schedule_window.json` payload directly into memory.
     */
    internal fun loadFromJsonStringForTest(rawJson: String) {
        snapshot = parseScheduleWindowInternal(rawJson) ?: ScheduleSnapshot()
        diskLoaded = true
    }

    internal fun clearForTest() {
        snapshot = ScheduleSnapshot()
        diskLoaded = false
    }

    fun isSnapshotFresh(nowMs: Long = System.currentTimeMillis()): Boolean {
        val current = snapshot
        if (current.days.isEmpty()) return false
        val generatedAt = current.generatedAtEpochMs
        if (generatedAt <= 0L) return true
        return (nowMs - generatedAt) <= STALE_SNAPSHOT_THRESHOLD_MS
    }

    /**
     * Converts cached [StaticKickoffFixture] records for [isoDate] into localized [FootballMatch]
     * instances using [zone] and [manualOffsetMinutes], tagging freshness as [DataFreshness.CACHED]
     * (or [DataFreshness.STALE] when the snapshot is older than 12 hours).
     */
    fun getMatchesForIsoDate(
        isoDate: String,
        dayTab: MatchDayTab,
        zone: UserCountryZone,
        manualOffsetMinutes: Int,
        nowMs: Long = System.currentTimeMillis()
    ): List<FootballMatch> {
        val current = snapshot
        val fixtures = current.days[isoDate.trim()].orEmpty()
        if (fixtures.isEmpty()) return emptyList()

        val generatedAt = current.generatedAtEpochMs
        val isStale = generatedAt > 0L && (nowMs - generatedAt) > STALE_SNAPSHOT_THRESHOLD_MS
        val freshness = if (isStale) DataFreshness.STALE else DataFreshness.CACHED
        val moroccoZone = UserZoneRegistry.ALL_COUNTRY_ZONES.firstOrNull { it.countryCode == "MA" } ?: zone
        val catalogById = FootballRepository.ALL_TOURNAMENTS.associateBy { it.tournamentId }

        return fixtures.map { fixture ->
            val parsedStatus = parseMatchStatus(fixture.status)
            val kickoffEpoch = fixture.kickoffEpochMs
            val localizedKickoffTime = if (kickoffEpoch > 0L) {
                formatDynamicKickoffTime(
                    kickoffEpochMs = kickoffEpoch,
                    zone = zone,
                    manualOffsetMinutes = manualOffsetMinutes
                )
            } else {
                "TBD"
            }
            val referenceMoroccoTime = if (kickoffEpoch > 0L) {
                formatDynamicKickoffTime(
                    kickoffEpochMs = kickoffEpoch,
                    zone = moroccoZone,
                    manualOffsetMinutes = 0
                )
            } else {
                localizedKickoffTime
            }
            val badgeText = when {
                fixture.liveMinuteOrBadge.isNotBlank() -> fixture.liveMinuteOrBadge
                parsedStatus == MatchStatus.FINISHED -> "FT"
                parsedStatus == MatchStatus.HALFTIME -> "HT"
                parsedStatus == MatchStatus.POSTPONED -> "PPD"
                parsedStatus == MatchStatus.CANCELLED -> "CANC"
                else -> localizedKickoffTime
            }

            val catalogItem = catalogById[fixture.tournamentId]
            val defaultChannel = catalogItem?.defaultBeinPool?.firstOrNull().orEmpty().ifBlank { "beIN Sports 1 HD" }
            val defaultProvider = catalogItem?.primaryMenaProvider.orEmpty().ifBlank { "beIN Sports" }
            val fallbackChannels = catalogItem?.fallbackMenaPool ?: emptyList()

            val effectiveOffsetMins = zone.getEffectiveOffsetMinutes(
                com.example.data.repository.SeasonTimeMode.AUTO,
                if (kickoffEpoch > 0L) kickoffEpoch else nowMs
            ) + manualOffsetMinutes
            val sign = if (effectiveOffsetMins >= 0) "+" else "-"
            val absMins = kotlin.math.abs(effectiveOffsetMins)
            val offsetLabel = if (absMins % 60 == 0) {
                "GMT$sign${absMins / 60}"
            } else {
                String.format(Locale.US, "GMT%s%d:%02d", sign, absMins / 60, absMins % 60)
            }

            FootballMatch(
                matchId = fixture.canonicalId.ifBlank {
                    "${fixture.tournamentId}|${isoDate}|${fixture.homeTeamName}|${fixture.awayTeamName}"
                },
                tournamentId = fixture.tournamentId,
                tournamentName = fixture.tournamentName.ifBlank { catalogItem?.name ?: fixture.tournamentId },
                tournamentRegion = fixture.tournamentRegion.ifBlank { catalogItem?.countryOrRegion ?: "International" },
                dayTab = dayTab,
                kickoffTimeFormatted = localizedKickoffTime,
                kickoffDateFormatted = "${dayTab.getDynamicTitle(zone.timeZoneId)} • $offsetLabel",
                status = parsedStatus,
                liveMinuteOrBadge = badgeText,
                homeTeamId = fixture.homeTeamId,
                homeTeamName = fixture.homeTeamName,
                homeTeamBadge = fixture.homeTeamBadge,
                homeScore = fixture.homeScore,
                awayTeamId = fixture.awayTeamId,
                awayTeamName = fixture.awayTeamName,
                awayTeamBadge = fixture.awayTeamBadge,
                awayScore = fixture.awayScore,
                stadiumName = fixture.stadiumName,
                cityName = "",
                beinChannel = defaultChannel,
                beinChannelNumberHint = "",
                menaProviderName = defaultProvider,
                fallbackMenaChannels = fallbackChannels,
                espnEventId = fixture.espnEventId,
                referenceMoroccoTime = referenceMoroccoTime,
                kickoffEpochMs = kickoffEpoch,
                sourceProvider = fixture.sourceProvider.ifBlank { "kickoff_static_index" },
                fetchedAtEpochMs = if (generatedAt > 0L) generatedAt else nowMs,
                dataFreshness = freshness
            )
        }
    }

    private fun parseMatchStatus(raw: String): MatchStatus {
        return when (raw.trim().uppercase(Locale.ROOT)) {
            "LIVE" -> MatchStatus.LIVE
            "HALFTIME", "HT" -> MatchStatus.HALFTIME
            "FINISHED", "FT" -> MatchStatus.FINISHED
            "POSTPONED", "PPD" -> MatchStatus.POSTPONED
            "CANCELLED", "CANCELED", "CANC" -> MatchStatus.CANCELLED
            else -> MatchStatus.UPCOMING
        }
    }

    private fun parseScheduleWindowInternal(rawJson: String): ScheduleSnapshot? {
        if (rawJson.isBlank()) return null
        return try {
            val root = JSONObject(rawJson)
            val schemaVersion = root.optInt("schemaVersion", 1)
            val generatedAtEpochMs = root.optLong("generatedAtEpochMs", 0L)
            val windowStartDate = root.optString("windowStartDate", "").trim()
            val windowEndDate = root.optString("windowEndDate", "").trim()
            val daysObj = root.optJSONObject("days") ?: return null

            val parsedDays = LinkedHashMap<String, List<StaticKickoffFixture>>()
            val keys = daysObj.keys()
            while (keys.hasNext()) {
                val isoDate = keys.next().trim()
                val arr = daysObj.optJSONArray(isoDate) ?: continue
                val list = ArrayList<StaticKickoffFixture>(arr.length())
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val homeName = obj.optString("homeTeamName", "").trim()
                    val awayName = obj.optString("awayTeamName", "").trim()
                    val tournamentId = obj.optString("tournamentId", "").trim()
                    if (homeName.isEmpty() || awayName.isEmpty() || tournamentId.isEmpty()) continue

                    val homeScore = if (obj.has("homeScore") && !obj.isNull("homeScore")) {
                        runCatching { obj.getInt("homeScore") }.getOrNull()
                    } else {
                        null
                    }
                    val awayScore = if (obj.has("awayScore") && !obj.isNull("awayScore")) {
                        runCatching { obj.getInt("awayScore") }.getOrNull()
                    } else {
                        null
                    }

                    list.add(
                        StaticKickoffFixture(
                            canonicalId = obj.optString("canonicalId", "").trim(),
                            tournamentId = tournamentId,
                            tournamentName = obj.optString("tournamentName", "").trim(),
                            tournamentRegion = obj.optString("tournamentRegion", "").trim(),
                            kickoffEpochMs = obj.optLong("kickoffEpochMs", 0L),
                            status = obj.optString("status", "UPCOMING").trim(),
                            liveMinuteOrBadge = obj.optString("liveMinuteOrBadge", "").trim(),
                            homeTeamId = obj.optString("homeTeamId", "").trim(),
                            homeTeamName = homeName,
                            homeTeamBadge = obj.optString("homeTeamBadge", "").trim(),
                            homeScore = homeScore,
                            awayTeamId = obj.optString("awayTeamId", "").trim(),
                            awayTeamName = awayName,
                            awayTeamBadge = obj.optString("awayTeamBadge", "").trim(),
                            awayScore = awayScore,
                            stadiumName = obj.optString("stadiumName", "").trim(),
                            espnEventId = obj.optString("espnEventId", "").trim(),
                            sourceProvider = obj.optString("sourceProvider", "kickoff_static_index").trim()
                        )
                    )
                }
                parsedDays[isoDate] = list
            }

            ScheduleSnapshot(
                schemaVersion = schemaVersion,
                generatedAtEpochMs = generatedAtEpochMs,
                windowStartDate = windowStartDate,
                windowEndDate = windowEndDate,
                days = parsedDays
            )
        } catch (e: Exception) {
            Log.w(TAG, "Malformed kickoff schedule window JSON: ${e.message}")
            null
        }
    }

    private fun readMeta(metaFile: File): Pair<String, Long> {
        if (!metaFile.exists()) return "" to 0L
        return runCatching {
            val lines = metaFile.readLines(Charsets.UTF_8)
            val etag = lines.getOrNull(0)?.trim().orEmpty()
            val ts = lines.getOrNull(1)?.trim()?.toLongOrNull() ?: 0L
            etag to ts
        }.getOrDefault("" to 0L)
    }

    private fun writeMeta(metaFile: File, etag: String, timestampMs: Long) {
        runCatching {
            metaFile.writeText("$etag\n$timestampMs", Charsets.UTF_8)
        }
    }
}
