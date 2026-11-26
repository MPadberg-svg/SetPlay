package com.example.data.remote

import com.example.BuildConfig
import com.example.data.repository.DataFreshness
import com.example.data.repository.FootballMatch
import com.example.data.repository.FootballRepository
import com.example.data.repository.MatchDayTab
import com.example.data.repository.MatchStatus
import com.example.data.repository.SeasonTimeMode
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
import java.util.Locale

/**
 * Va3 Cloudflare Worker Live Edge client.
 *
 * When [BuildConfig.KICKOFF_EDGE_BASE_URL] is configured with an `https://` Worker URL,
 * queries `GET /v1/live` with `If-None-Match` ETag support (0-byte HTTP 304 when unchanged)
 * so live match updates coalesce at the edge without exposing upstream API keys on devices.
 * Returns `null` cleanly whenever unconfigured or unreachable so [FootballRepository] falls
 * back seamlessly to its Phase 1–2 direct adapters.
 */
object KickoffEdgeClient {
    private const val PLACEHOLDER_SUBDOMAIN_MARKER = "<subdomain>"
    private const val PLACEHOLDER_KEY_MARKER = "YOUR_"

    private val fetchMutex = Mutex()

    @Volatile
    private var cachedBaseUrl: String = ""

    @Volatile
    private var cachedEtag: String = ""

    @Volatile
    private var cachedRawJson: String = ""

    /**
     * Returns `true` ONLY when [baseUrl] is a non-blank `https://` URL without template placeholders.
     */
    fun isConfigured(baseUrl: String = BuildConfig.KICKOFF_EDGE_BASE_URL): Boolean {
        val clean = baseUrl.trim()
        if (clean.isEmpty()) return false
        if (!clean.startsWith("https://", ignoreCase = true)) return false
        if (clean.contains(PLACEHOLDER_SUBDOMAIN_MARKER, ignoreCase = true)) return false
        if (clean.contains(PLACEHOLDER_KEY_MARKER, ignoreCase = true)) return false
        return clean.length > "https://".length
    }

    /**
     * Fetches the edge-cached canonical live delta from `GET "$cleanBase/v1/live"`.
     * - Returns `null` immediately when `!isConfigured(baseUrl)` (zero network calls).
     * - Sends `If-None-Match` when an ETag is cached; returns localized cached delta on HTTP 304.
     * - Returns `null` cleanly on any timeout, network exception, or non-200/304 status.
     */
    suspend fun fetchLiveDelta(
        okHttpClient: OkHttpClient,
        zone: UserCountryZone,
        manualOffsetMinutes: Int,
        baseUrl: String = BuildConfig.KICKOFF_EDGE_BASE_URL
    ): List<FootballMatch>? {
        if (!isConfigured(baseUrl)) return null
        val cleanBase = baseUrl.trim().trimEnd('/')
        val liveUrl = "$cleanBase/v1/live"

        return withContext(Dispatchers.IO) {
            fetchMutex.withLock {
                runCatching {
                    val reqBuilder = Request.Builder()
                        .url(liveUrl)
                        .get()
                        .header("Accept", "application/json")

                    val activeEtag = if (cachedBaseUrl == cleanBase) cachedEtag else ""
                    if (activeEtag.isNotBlank() && cachedRawJson.isNotBlank()) {
                        reqBuilder.header("If-None-Match", activeEtag)
                    }

                    okHttpClient.newCall(reqBuilder.build()).execute().use { response ->
                        when (response.code) {
                            304 -> {
                                if (cachedBaseUrl == cleanBase && cachedRawJson.isNotBlank()) {
                                    val updatedEtag = response.header("ETag")?.trim().orEmpty()
                                    if (updatedEtag.isNotBlank()) {
                                        cachedEtag = updatedEtag
                                    }
                                    parseLiveDeltaJson(cachedRawJson, zone, manualOffsetMinutes)
                                } else {
                                    null
                                }
                            }
                            200 -> {
                                val bodyStr = response.body?.string().orEmpty()
                                if (bodyStr.isBlank()) {
                                    null
                                } else {
                                    val parsed = parseLiveDeltaJson(bodyStr, zone, manualOffsetMinutes)
                                    cachedBaseUrl = cleanBase
                                    cachedRawJson = bodyStr
                                    cachedEtag = response.header("ETag")?.trim().orEmpty()
                                    parsed
                                }
                            }
                            else -> null
                        }
                    }
                }.getOrNull()
            }
        }
    }

    /**
     * Parses canonical `/v1/live` JSON into localized [FootballMatch] objects stamped with
     * [DataFreshness.LIVE] and `fetchedAtEpochMs`. Exposed internally for offline unit testing.
     */
    internal fun parseLiveDeltaJson(
        rawJson: String,
        zone: UserCountryZone,
        manualOffsetMinutes: Int
    ): List<FootballMatch> {
        if (rawJson.isBlank()) return emptyList()
        return runCatching {
            val root = JSONObject(rawJson)
            val generatedAtMs = root.optLong("generatedAtEpochMs", System.currentTimeMillis())
            val rootProvider = root.optString("sourceProvider", "kickoff_edge").trim()
                .ifBlank { "kickoff_edge" }
            val matchesArr = root.optJSONArray("matches") ?: return@runCatching emptyList()
            val moroccoZone = UserZoneRegistry.ALL_COUNTRY_ZONES.firstOrNull { it.countryCode == "MA" } ?: zone
            val catalogById = FootballRepository.ALL_TOURNAMENTS.associateBy { it.tournamentId }

            val results = ArrayList<FootballMatch>(matchesArr.length())
            for (i in 0 until matchesArr.length()) {
                val obj = matchesArr.optJSONObject(i) ?: continue
                val tournamentId = obj.optString("tournamentId", "").trim()
                val homeName = obj.optString("homeTeamName", "").trim()
                val awayName = obj.optString("awayTeamName", "").trim()
                if (tournamentId.isEmpty() || homeName.isEmpty() || awayName.isEmpty()) continue

                val kickoffEpochMs = obj.optLong("kickoffEpochMs", 0L)
                val status = parseMatchStatus(obj.optString("status", "LIVE"))
                val localizedKickoff = if (kickoffEpochMs > 0L) {
                    formatDynamicKickoffTime(
                        kickoffEpochMs = kickoffEpochMs,
                        zone = zone,
                        manualOffsetMinutes = manualOffsetMinutes
                    )
                } else {
                    "LIVE"
                }
                val referenceMoroccoTime = if (kickoffEpochMs > 0L) {
                    formatDynamicKickoffTime(
                        kickoffEpochMs = kickoffEpochMs,
                        zone = moroccoZone,
                        manualOffsetMinutes = 0
                    )
                } else {
                    localizedKickoff
                }
                val rawBadge = obj.optString("liveMinuteOrBadge", "").trim()
                val badge = when {
                    rawBadge.isNotBlank() -> rawBadge
                    status == MatchStatus.HALFTIME -> "HT"
                    status == MatchStatus.FINISHED -> "FT"
                    status == MatchStatus.POSTPONED -> "PPD"
                    status == MatchStatus.CANCELLED -> "CANC"
                    status == MatchStatus.LIVE -> "LIVE"
                    else -> localizedKickoff
                }

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

                val catalogItem = catalogById[tournamentId]
                val defaultChannel = catalogItem?.defaultBeinPool?.firstOrNull().orEmpty()
                    .ifBlank { "beIN Sports 1 HD" }
                val defaultProvider = catalogItem?.primaryMenaProvider.orEmpty()
                    .ifBlank { "beIN Sports" }
                val fallbackChannels = catalogItem?.fallbackMenaPool ?: emptyList()

                val effectiveOffsetMins = zone.getEffectiveOffsetMinutes(
                    SeasonTimeMode.AUTO,
                    if (kickoffEpochMs > 0L) kickoffEpochMs else generatedAtMs
                ) + manualOffsetMinutes
                val sign = if (effectiveOffsetMins >= 0) "+" else "-"
                val absMins = kotlin.math.abs(effectiveOffsetMins)
                val offsetLabel = if (absMins % 60 == 0) {
                    "GMT$sign${absMins / 60}"
                } else {
                    String.format(Locale.US, "GMT%s%d:%02d", sign, absMins / 60, absMins % 60)
                }

                val canonicalId = obj.optString("canonicalId", "").trim()
                val matchSource = obj.optString("sourceProvider", "").trim().ifBlank { rootProvider }

                results.add(
                    FootballMatch(
                        matchId = canonicalId.ifBlank {
                            "${tournamentId}|live|${homeName}|${awayName}"
                        },
                        tournamentId = tournamentId,
                        tournamentName = obj.optString("tournamentName", "").trim()
                            .ifBlank { catalogItem?.name ?: tournamentId },
                        tournamentRegion = obj.optString("tournamentRegion", "").trim()
                            .ifBlank { catalogItem?.countryOrRegion ?: "International" },
                        dayTab = MatchDayTab.TODAY,
                        kickoffTimeFormatted = localizedKickoff,
                        kickoffDateFormatted = "${MatchDayTab.TODAY.getDynamicTitle(zone.timeZoneId)} • $offsetLabel",
                        status = status,
                        liveMinuteOrBadge = badge,
                        homeTeamId = obj.optString("homeTeamId", "").trim(),
                        homeTeamName = homeName,
                        homeTeamBadge = obj.optString("homeTeamBadge", "").trim(),
                        homeScore = homeScore,
                        awayTeamId = obj.optString("awayTeamId", "").trim(),
                        awayTeamName = awayName,
                        awayTeamBadge = obj.optString("awayTeamBadge", "").trim(),
                        awayScore = awayScore,
                        stadiumName = obj.optString("stadiumName", "").trim(),
                        cityName = "",
                        beinChannel = defaultChannel,
                        beinChannelNumberHint = "",
                        menaProviderName = defaultProvider,
                        fallbackMenaChannels = fallbackChannels,
                        espnEventId = obj.optString("espnEventId", "").trim(),
                        apiFootballFixtureId = obj.optString("apiFootballFixtureId", "").trim(),
                        referenceMoroccoTime = referenceMoroccoTime,
                        kickoffEpochMs = kickoffEpochMs,
                        sourceProvider = matchSource,
                        fetchedAtEpochMs = generatedAtMs,
                        dataFreshness = DataFreshness.LIVE
                    )
                )
            }
            results
        }.getOrDefault(emptyList())
    }

    internal fun clearCacheForTest() {
        cachedBaseUrl = ""
        cachedEtag = ""
        cachedRawJson = ""
    }

    private fun parseMatchStatus(raw: String): MatchStatus {
        return when (raw.trim().uppercase(Locale.ROOT)) {
            "LIVE" -> MatchStatus.LIVE
            "HALFTIME", "HT" -> MatchStatus.HALFTIME
            "FINISHED", "FT" -> MatchStatus.FINISHED
            "POSTPONED", "PPD" -> MatchStatus.POSTPONED
            "CANCELLED", "CANCELED", "CANC" -> MatchStatus.CANCELLED
            "UPCOMING" -> MatchStatus.UPCOMING
            else -> MatchStatus.LIVE
        }
    }
}
