package com.example.data.repository

import android.content.Context
import com.example.BuildConfig
import com.example.data.local.FavoriteTeamDao
import com.example.data.local.StreamDao
import com.example.data.local.TournamentPreferenceDao
import com.example.data.model.FavoriteTeamEntity
import com.example.data.model.StreamEntity
import com.example.data.model.TournamentPreferenceEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * 5-Day Interactive Matchday Timeline for SetPlay KickOff Arena.
 * Upgrades the previous 2-day view into a full 5-day match week (Yesterday, Today, Tomorrow, +2 Days, +3 Days)
 * formatted in Morocco timezone (Africa/Casablanca • GMT+1).
 */
enum class MatchDayTab(val defaultLabel: String, val dayOffset: Int) {
    YESTERDAY("Yesterday", -1),
    TODAY("Today", 0),
    TOMORROW("Tomorrow", 1),
    DAY_PLUS_2("+2 Days", 2),
    DAY_PLUS_3("+3 Days", 3);

    fun getDynamicTitle(): String {
        return when (this) {
            YESTERDAY -> "Yesterday"
            TODAY -> "Today"
            TOMORROW -> "Tomorrow"
            DAY_PLUS_2, DAY_PLUS_3 -> {
                val cal = Calendar.getInstance(TimeZone.getTimeZone("Africa/Casablanca"))
                cal.add(Calendar.DAY_OF_YEAR, dayOffset)
                SimpleDateFormat("EEE d", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("Africa/Casablanca")
                }.format(cal.time)
            }
        }
    }

    fun getDynamicDateSubtitle(): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Africa/Casablanca"))
        cal.add(Calendar.DAY_OF_YEAR, dayOffset)
        return SimpleDateFormat("dd MMM", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Africa/Casablanca")
        }.format(cal.time)
    }

    // Backwards-compatible property used by UI components
    val label: String
        get() = getDynamicTitle()
}

enum class MatchStatus {
    LIVE,
    HALFTIME,
    UPCOMING,
    FINISHED
}

data class FootballTeam(
    val teamId: String,
    val name: String,
    val shortName: String,
    val badgeUrl: String,
    val country: String,
    val primaryLeague: String
)

data class TournamentCatalogItem(
    val tournamentId: String,
    val name: String,
    val countryOrRegion: String,
    val espnSlug: String,
    val sportsDbLeagueId: String,
    val primaryMenaProvider: String,
    val defaultBeinPool: List<String>,
    val fallbackMenaPool: List<String>,
    val defaultSortOrder: Int
)

data class FootballMatch(
    val matchId: String,
    val tournamentId: String,
    val tournamentName: String,
    val tournamentRegion: String,
    val dayTab: MatchDayTab,
    val kickoffTimeFormatted: String, // e.g. "20:00" (Morocco GMT+1)
    val kickoffDateFormatted: String, // e.g. "Today • GMT+1"
    val status: MatchStatus,
    val liveMinuteOrBadge: String,    // e.g. "67'", "1H", "HT", "2H", "FT", "20:00"
    val homeTeamId: String,
    val homeTeamName: String,
    val homeTeamBadge: String,
    val homeScore: Int?,
    val awayTeamId: String,
    val awayTeamName: String,
    val awayTeamBadge: String,
    val awayScore: Int?,
    val stadiumName: String,
    val cityName: String,
    val beinChannel: String,          // Primary MENA broadcaster channel (beIN Sports or regional MENA rights holder)
    val beinChannelNumberHint: String,
    val menaProviderName: String = "beIN Sports", // e.g., "beIN Sports", "Arryadia TNT", "SSC Sports", "AD Sports", "SNRT"
    val fallbackMenaChannels: List<String> = emptyList(), // Alternate MENA broadcasters if primary doesn't offer the game
    val sourceTvChannels: List<String> = emptyList(), // Exact TV channel names as provided directly by the upstream source
    val stageOrRound: String = "Regular Season",
    val sportsDbEventId: String = "",
    val espnEventId: String = "",
    val halfTimeScore: String? = null,      // e.g. "1 - 0"
    val fullTimeScore: String? = null,      // e.g. "2 - 1"
    val homeRedCards: Int = 0,
    val awayRedCards: Int = 0,
    val goalScorersSummary: String? = null  // e.g. "Mbappé 18', Bellingham 67'"
)

data class MenaChannelMatchResult(
    val favoriteMatches: List<StreamEntity>,
    val livePlaylistCandidates: List<StreamEntity>,
    val resolvedChannelLabel: String,
    val resolvedProviderName: String,
    val usedMenaFallbackProvider: Boolean,
    val availableMenaChannelOptions: List<String>,
    val optionsAvailableInFavorites: Set<String> = emptySet()
)

internal data class SportsDbTvBroadcastEntry(
    val eventId: String,
    val eventTitleNormalized: String,
    val channelName: String,
    val country: String,
    val eventTitleRaw: String = ""
)

internal data class NormalizedChannelProfile(
    val normalizedText: String,
    val canonicalProvider: String,
    val channelNumbers: List<String>,
    val subBrandTokens: Set<String>,
    val languageTokens: Set<String> = emptySet(),
    val is4kFeed: Boolean = false,
    val canonicalKey: String = ""
)

data class LiveScoreSyncState(
    val isPollingActive: Boolean = false,
    val isSyncingDelta: Boolean = false,
    val lastSyncedEpochMs: Long = 0L,
    val lastSyncedTimeFormatted: String = "",
    val pollCycleCount: Int = 0,
    val activeLiveMatchesCount: Int = 0,
    val primaryLiveSource: String = "SportAPI.ai Live"
)

/**
 * Dual-API Hybrid Football Service tailored for Morocco & MENA region:
 * - API #1: SportAPI.ai (`https://sportapi.ai/api`) — Primary for Live Scores, Real-Time Match Status (`1H`, `HT`, `2H`, `FT`) & Fixtures.
 *   Authenticated via `X-Api-Key: BuildConfig.SPORTAPI_AI_KEY` and `Accept: application/json`.
 *   Rate-limited to 2,000 requests/day; polled every 60s only when at least one displayed match is currently `LIVE`.
 * - API #2: TheSportsDB Free v1 API (`https://www.thesportsdb.com/api/v1/json/123`) — Primary for TV Broadcast Listings (`eventstv.php`),
 *   High-Res `/tiny` Team Badges (`strBadge/tiny`, `strHomeTeamBadge/tiny`, `strAwayTeamBadge/tiny`), and League Schedules (`eventsday.php` + Botola Pro `4520`).
 *   Rate-limited to 30 requests/minute.
 */
class FootballRepository(
    context: Context,
    private val favoriteTeamDao: FavoriteTeamDao,
    private val tournamentPreferenceDao: TournamentPreferenceDao,
    private val streamDao: StreamDao
) {
    private val prefs = context.getSharedPreferences("setplay_football_prefs", Context.MODE_PRIVATE)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    // Rate-limiters & caches:
    // 1. SportAPI.ai: 2,000 requests/day max, 55-second cache for live polls
    private val sportApiDayCache = ConcurrentHashMap<String, Pair<Long, List<FootballMatch>>>()
    private val sportApiDailyCounter = AtomicInteger(0)

    // 2. TheSportsDB v1/json/123: 30 requests/minute sliding window + per-day cache
    private val sportsDbRequestTimestamps = ConcurrentLinkedDeque<Long>()
    private val sportsDbEventsCache = ConcurrentHashMap<String, Pair<Long, List<FootballMatch>>>()
    private val sportsDbTvCache = ConcurrentHashMap<String, Pair<Long, List<SportsDbTvBroadcastEntry>>>()

    // 3. Optional API-Football cache (15-min TTL)
    private val apiFootballDayCache = ConcurrentHashMap<String, Pair<Long, List<FootballMatch>>>()

    // 4. 5-Tab Match Center Enricher (Lineups, 2D Pitch, Timeline, Stats, H2H/Standings, MENA TV Guide)
    private val matchCenterEnricher = MatchCenterEnricher(httpClient)

    suspend fun fetchMatchFullDossier(
        match: FootballMatch,
        forceRefresh: Boolean = false
    ): MatchFullDossier = matchCenterEnricher.fetchMatchFullDossier(match, forceRefresh)

    val favoriteTeamsFlow: Flow<List<FavoriteTeamEntity>> = favoriteTeamDao.getAllFavoriteTeams()
    val tournamentPreferencesFlow: Flow<List<TournamentPreferenceEntity>> =
        tournamentPreferenceDao.getAllTournamentPreferences()

    fun hasCompletedTeamsOnboarding(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setTeamsOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    suspend fun toggleFavoriteTeam(team: FootballTeam) {
        val current = favoriteTeamDao.getAllFavoriteTeamsOnce()
        val exists = current.any {
            it.teamId.equals(team.teamId, ignoreCase = true) ||
                it.teamName.equals(team.name, ignoreCase = true)
        }
        if (exists) {
            val match = current.first {
                it.teamId.equals(team.teamId, ignoreCase = true) ||
                    it.teamName.equals(team.name, ignoreCase = true)
            }
            favoriteTeamDao.removeFavoriteTeam(match.teamId)
        } else {
            favoriteTeamDao.insertFavoriteTeam(
                FavoriteTeamEntity(
                    teamId = team.teamId,
                    teamName = team.name,
                    shortName = team.shortName.ifBlank { team.name.take(3).uppercase() },
                    badgeUrl = toTinySportsDbBadgeUrl(team.badgeUrl),
                    country = team.country,
                    primaryLeague = team.primaryLeague
                )
            )
        }
    }

    suspend fun removeFavoriteTeam(teamId: String) {
        favoriteTeamDao.removeFavoriteTeam(teamId)
    }

    suspend fun ensureDefaultTournamentsInitialized() {
        val existing = tournamentPreferenceDao.getAllTournamentPreferencesOnce()
        if (existing.isEmpty()) {
            val defaults = ALL_TOURNAMENTS.map { item ->
                TournamentPreferenceEntity(
                    tournamentId = item.tournamentId,
                    tournamentName = item.name,
                    countryOrRegion = item.countryOrRegion,
                    isEnabled = true,
                    isPinned = item.defaultSortOrder <= 3,
                    sortOrder = item.defaultSortOrder
                )
            }
            tournamentPreferenceDao.upsertPreferences(defaults)
        } else {
            val catalogMap = ALL_TOURNAMENTS.associateBy { it.tournamentId }
            val synced = existing.map { pref ->
                val catalogItem = catalogMap[pref.tournamentId]
                if (catalogItem != null && pref.sortOrder != catalogItem.defaultSortOrder) {
                    pref.copy(
                        tournamentName = catalogItem.name,
                        sortOrder = catalogItem.defaultSortOrder
                    )
                } else {
                    pref
                }
            }
            tournamentPreferenceDao.upsertPreferences(synced)
        }
    }

    suspend fun setTournamentEnabled(tournamentId: String, enabled: Boolean) {
        val existing = tournamentPreferenceDao.getAllTournamentPreferencesOnce()
            .find { it.tournamentId == tournamentId }
        if (existing != null) {
            tournamentPreferenceDao.upsertPreference(existing.copy(isEnabled = enabled))
        }
    }

    suspend fun toggleTournamentPinned(tournamentId: String) {
        val existing = tournamentPreferenceDao.getAllTournamentPreferencesOnce()
            .find { it.tournamentId == tournamentId }
        if (existing != null) {
            tournamentPreferenceDao.upsertPreference(existing.copy(isPinned = !existing.isPinned))
        }
    }

    suspend fun enableAllTournaments() {
        val existing = tournamentPreferenceDao.getAllTournamentPreferencesOnce()
        tournamentPreferenceDao.upsertPreferences(existing.map { it.copy(isEnabled = true) })
    }

    /**
     * Searches global football teams manually by user query across:
     * 1. TheSportsDB v1 API (`https://www.thesportsdb.com/api/v1/json/123/searchteams.php?t={TeamName}`) with `/tiny` badge URLs
     * 2. SportAPI.ai (`https://sportapi.ai/api`) if configured
     * 3. ESPN Public Soccer Team Search API (`site.web.api.espn.com/apis/common/v3/search`)
     * 4. Built-in global team index for instant zero-latency matching
     */
    suspend fun searchGlobalTeams(query: String): List<FootballTeam> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            return@withContext emptyList()
        }

        val combined = LinkedHashMap<String, FootballTeam>()

        // 1. Instant local global index match so partial queries immediately return crests
        GLOBAL_TEAM_INDEX.filter { team ->
            team.name.contains(trimmed, ignoreCase = true) ||
                team.shortName.contains(trimmed, ignoreCase = true) ||
                team.country.contains(trimmed, ignoreCase = true) ||
                team.primaryLeague.contains(trimmed, ignoreCase = true)
        }.forEach { team ->
            combined[normalizeTeamNameForMerge(team.name)] = team.copy(
                badgeUrl = toTinySportsDbBadgeUrl(team.badgeUrl)
            )
        }

        // 2. Live API queries in parallel with fault-tolerant supervisorScope + runCatching (Promise.allSettled equivalent)
        supervisorScope {
            val sportsDbDeferred = async { runCatching { searchTeamsFromTheSportsDb(trimmed) }.getOrDefault(emptyList()) }
            val sportApiDeferred = async { runCatching { searchTeamsFromSportApiAi(trimmed) }.getOrDefault(emptyList()) }
            val espnDeferred = async { runCatching { searchTeamsFromEspn(trimmed) }.getOrDefault(emptyList()) }
            val apiFootballDeferred = async { runCatching { searchTeamsFromApiFootball(trimmed) }.getOrDefault(emptyList()) }

            // Prioritize TheSportsDB first for `/tiny` high-res badges
            val liveResults = awaitAll(sportsDbDeferred, sportApiDeferred, espnDeferred, apiFootballDeferred).flatten()
            for (team in liveResults) {
                val key = normalizeTeamNameForMerge(team.name).ifBlank { team.name.lowercase(Locale.ROOT) }
                val existing = combined[key]
                if (existing == null) {
                    combined[key] = team
                } else if ((existing.badgeUrl.isBlank() || !existing.badgeUrl.endsWith("/tiny")) && team.badgeUrl.isNotBlank()) {
                    combined[key] = existing.copy(badgeUrl = team.badgeUrl)
                }
            }
        }

        combined.values.take(35).toList()
    }

    fun isSportApiKeyConfigured(): Boolean {
        val key = try {
            BuildConfig.SPORTAPI_AI_KEY
        } catch (_: Exception) {
            ""
        }
        return key.isNotBlank() &&
            key != "YOUR_SPORTAPI_AI_KEY" &&
            !key.startsWith("MY_") &&
            key != "null"
    }

    private fun isApiFootballKeyConfigured(): Boolean {
        val key = try {
            BuildConfig.API_FOOTBALL_KEY
        } catch (_: Exception) {
            ""
        }
        return key.isNotBlank() &&
            key != "YOUR_API_FOOTBALL_KEY" &&
            !key.startsWith("MY_") &&
            key != "null"
    }

    /**
     * Enforces TheSportsDB Free v1 rate limit of 30 requests/minute.
     */
    private fun canAcquireSportsDbPermit(): Boolean {
        val now = System.currentTimeMillis()
        while (true) {
            val oldest = sportsDbRequestTimestamps.peekFirst() ?: break
            if (now - oldest > 60_000L) {
                sportsDbRequestTimestamps.pollFirst()
            } else {
                break
            }
        }
        if (sportsDbRequestTimestamps.size >= THESPORTSDB_MAX_REQ_PER_MINUTE) {
            return false
        }
        sportsDbRequestTimestamps.addLast(now)
        return true
    }

    /**
     * Appends `/tiny` to TheSportsDB badge URLs (`strBadge`, `strHomeTeamBadge`, `strAwayTeamBadge`)
     * for fast mobile rendering, as specified in the TheSportsDB v1 API integration guidelines.
     */
    fun toTinySportsDbBadgeUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        if (trimmed.isBlank() || trimmed == "null") return ""
        return if (trimmed.contains("thesportsdb.com", ignoreCase = true) && !trimmed.endsWith("/tiny")) {
            "${trimmed.trimEnd('/')}/tiny"
        } else {
            trimmed
        }
    }

    /**
     * Normalizes team names by stripping accents/diacritics and common club prefixes/suffixes
     * ("FC", "AC", "CA", "Club", "CF", "SC", "AS", "RS") so records from SportAPI.ai and TheSportsDB
     * merge accurately.
     */
    fun normalizeTeamNameForMerge(rawName: String): String {
        if (rawName.isBlank()) return ""
        val decomposed = Normalizer.normalize(rawName, Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        val baseTokens = decomposed
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { token ->
                token.isNotBlank() && token !in IGNORED_TEAM_TOKENS
            }
        val finalTokens = if (baseTokens.size > 1 && baseTokens.last() == "athletic") {
            baseTokens.dropLast(1)
        } else {
            baseTokens
        }
        return finalTokens.joinToString(" ").trim()
    }

    /**
     * Resolves common club aliases & abbreviations so TV broadcast entries (`eventstv.php`, ESPN, etc.)
     * reliably link to the corresponding fixture even when one source uses "Man City" and the other uses "Manchester City",
     * or "PSG" vs "Paris Saint-Germain", "Inter" vs "Internazionale", "Raja" vs "Raja Casablanca".
     */
    internal fun canonicalizeClubForTvMatch(rawName: String): Set<String> {
        val norm = normalizeTeamNameForMerge(rawName)
        if (norm.isBlank()) return emptySet()
        val aliases = mutableSetOf(norm)
        when {
            norm.contains("manchester city") || norm == "man city" || norm == "mancity" -> {
                aliases.addAll(listOf("manchester city", "man city"))
            }
            norm.contains("manchester united") || norm == "man utd" || norm == "man united" -> {
                aliases.addAll(listOf("manchester united", "man utd", "man united"))
            }
            norm.contains("paris saint") || norm == "psg" || norm == "paris sg" -> {
                aliases.addAll(listOf("paris saint germain", "paris sg", "psg"))
            }
            norm.contains("internazionale") || norm == "inter" || norm == "inter milan" -> {
                aliases.addAll(listOf("internazionale", "inter milan", "inter"))
            }
            norm.contains("atletico madrid") || norm == "atletico" || norm == "atl madrid" -> {
                aliases.addAll(listOf("atletico madrid", "atletico"))
            }
            norm.contains("tottenham") || norm == "spurs" -> {
                aliases.addAll(listOf("tottenham hotspur", "tottenham", "spurs"))
            }
            norm.contains("bayern") -> {
                aliases.addAll(listOf("bayern munich", "bayern munchen", "bayern"))
            }
            norm.contains("dortmund") || norm == "bvb" -> {
                aliases.addAll(listOf("borussia dortmund", "dortmund"))
            }
            norm.contains("leverkusen") -> {
                aliases.addAll(listOf("bayer leverkusen", "leverkusen"))
            }
            norm.contains("monchengladbach") || norm.contains("gladbach") -> {
                aliases.addAll(listOf("borussia monchengladbach", "monchengladbach", "gladbach"))
            }
            norm.contains("barcelona") || norm == "barca" -> {
                aliases.addAll(listOf("barcelona", "barca"))
            }
            norm.contains("real madrid") -> {
                aliases.add("real madrid")
            }
            norm.contains("raja") -> {
                aliases.addAll(listOf("raja", "raja casablanca", "raja athletic"))
            }
            norm.contains("wydad") -> {
                aliases.addAll(listOf("wydad", "wydad casablanca"))
            }
            norm.contains("far rabat") || norm == "far" -> {
                aliases.addAll(listOf("far rabat", "far"))
            }
            norm.contains("berkane") -> {
                aliases.addAll(listOf("berkane", "renaissance berkane"))
            }
            norm.contains("ahly") && !norm.contains("shabab") && !norm.contains("jeddah") -> {
                aliases.addAll(listOf("ahly", "ahly cairo"))
            }
        }
        return aliases
    }

    internal fun doesTvEntryMatchFixtureTeams(
        entry: SportsDbTvBroadcastEntry,
        homeTeamName: String,
        awayTeamName: String
    ): Boolean {
        val normEvent = entry.eventTitleNormalized
        if (normEvent.isBlank()) return false
        val homeAliases = canonicalizeClubForTvMatch(homeTeamName)
        val awayAliases = canonicalizeClubForTvMatch(awayTeamName)
        if (homeAliases.isEmpty() || awayAliases.isEmpty()) return false

        val homeMatches = homeAliases.any { alias ->
            alias.isNotBlank() && normEvent.contains(alias)
        }
        val awayMatches = awayAliases.any { alias ->
            alias.isNotBlank() && normEvent.contains(alias)
        }
        return homeMatches && awayMatches
    }

    /**
     * TheSportsDB Team Search & Badge Lookup:
     * `GET https://www.thesportsdb.com/api/v1/json/123/searchteams.php?t={TeamName}`
     * Appends `/tiny` to `strBadge` / `strTeamBadge` URLs for fast mobile rendering.
     */
    private fun searchTeamsFromTheSportsDb(query: String): List<FootballTeam> {
        if (!canAcquireSportsDbPermit()) return emptyList()
        return try {
            val url = "$THESPORTSDB_BASE_URL/searchteams.php".toHttpUrlOrNull()
                ?.newBuilder()
                ?.addQueryParameter("t", query)
                ?.build() ?: return emptyList()

            val request = Request.Builder().url(url).build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()
                val root = JSONObject(body)
                val teams = root.optJSONArray("teams") ?: return emptyList()
                val results = mutableListOf<FootballTeam>()
                for (i in 0 until teams.length()) {
                    val t = teams.optJSONObject(i) ?: continue
                    val sport = t.optString("strSport", "Soccer")
                    if (!sport.equals("Soccer", ignoreCase = true)) continue
                    val id = t.optString("idTeam", "")
                    val name = t.optString("strTeam", "").trim()
                    if (name.isBlank()) continue
                    val shortName = t.optString("strTeamShort", name.take(3).uppercase())
                    val rawBadge = t.optString("strBadge", t.optString("strTeamBadge", ""))
                    val tinyBadge = toTinySportsDbBadgeUrl(rawBadge)
                    val country = t.optString("strCountry", "Global")
                    val league = t.optString("strLeague", "Football")
                    results.add(
                        FootballTeam(
                            teamId = "tsdb_${id.ifBlank { name.lowercase(Locale.ROOT) }}",
                            name = name,
                            shortName = shortName.ifBlank { name.take(3).uppercase() },
                            badgeUrl = tinyBadge,
                            country = country,
                            primaryLeague = league
                        )
                    )
                }
                results
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun searchTeamsFromSportApiAi(query: String): List<FootballTeam> {
        if (!isSportApiKeyConfigured()) return emptyList()
        if (sportApiDailyCounter.get() >= SPORTAPI_MAX_REQ_PER_DAY) return emptyList()
        return try {
            val url = "$SPORTAPI_BASE_URL/v1/football/teams/search".toHttpUrlOrNull()
                ?.newBuilder()
                ?.addQueryParameter("q", query)
                ?.build() ?: return emptyList()

            val request = Request.Builder()
                .url(url)
                .header("X-Api-Key", BuildConfig.SPORTAPI_AI_KEY)
                .header("Accept", "application/json")
                .build()

            sportApiDailyCounter.incrementAndGet()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()
                val root = JSONObject(body)
                val arr = root.optJSONArray("data")
                    ?: root.optJSONArray("teams")
                    ?: root.optJSONArray("results")
                    ?: return emptyList()
                val results = mutableListOf<FootballTeam>()
                for (i in 0 until minOf(arr.length(), 20)) {
                    val item = arr.optJSONObject(i) ?: continue
                    val id = item.optString("id", item.optString("team_id", ""))
                    val name = item.optString("name", item.optString("short_name", "")).trim()
                    if (name.isBlank()) continue
                    val shortName = item.optString("short_code", item.optString("code", name.take(3).uppercase()))
                    val logo = item.optString("logo", item.optString("badge", item.optString("image_path", "")))
                    val country = item.optString("country", "Global")
                    val league = item.optString("league", "$country Football")
                    results.add(
                        FootballTeam(
                            teamId = "sportapi_${id.ifBlank { name.lowercase(Locale.ROOT) }}",
                            name = name,
                            shortName = shortName,
                            badgeUrl = logo,
                            country = country,
                            primaryLeague = league
                        )
                    )
                }
                results
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun searchTeamsFromApiFootball(query: String): List<FootballTeam> {
        if (!isApiFootballKeyConfigured()) return emptyList()
        return try {
            val url = "https://v3.football.api-sports.io/teams".toHttpUrlOrNull()
                ?.newBuilder()
                ?.addQueryParameter("search", query)
                ?.build() ?: return emptyList()

            val request = Request.Builder()
                .url(url)
                .header("x-apisports-key", BuildConfig.API_FOOTBALL_KEY)
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()
                val root = JSONObject(body)
                val arr = root.optJSONArray("response") ?: return emptyList()
                val list = mutableListOf<FootballTeam>()
                for (i in 0 until minOf(arr.length(), 20)) {
                    val item = arr.optJSONObject(i) ?: continue
                    val teamObj = item.optJSONObject("team") ?: continue
                    val id = teamObj.optInt("id", 0).toString()
                    val name = teamObj.optString("name", "").trim()
                    if (name.isBlank()) continue
                    val code = teamObj.optString("code", name.take(3).uppercase())
                    val country = teamObj.optString("country", "International")
                    val logo = teamObj.optString("logo", "")
                    list.add(
                        FootballTeam(
                            teamId = "apif_$id",
                            name = name,
                            shortName = code,
                            badgeUrl = logo,
                            country = country,
                            primaryLeague = "$country Football"
                        )
                    )
                }
                list
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun searchTeamsFromEspn(query: String): List<FootballTeam> {
        return try {
            val url = "https://site.web.api.espn.com/apis/common/v3/search".toHttpUrlOrNull()
                ?.newBuilder()
                ?.addQueryParameter("query", query)
                ?.addQueryParameter("limit", "20")
                ?.addQueryParameter("type", "team")
                ?.addQueryParameter("sport", "soccer")
                ?.build() ?: return emptyList()

            val request = Request.Builder().url(url).build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()
                val root = JSONObject(body)
                val items = root.optJSONArray("items") ?: return emptyList()
                val results = mutableListOf<FootballTeam>()
                for (i in 0 until items.length()) {
                    val item = items.optJSONObject(i) ?: continue
                    val id = item.optString("id", "")
                    val displayName = item.optString("displayName", "").trim()
                    if (displayName.isBlank()) continue
                    val shortName = item.optString("abbreviation", displayName.take(3).uppercase())
                    val logos = item.optJSONArray("logos")
                    val logoUrl = if (logos != null && logos.length() > 0) {
                        logos.optJSONObject(0)?.optString("href", "") ?: ""
                    } else ""
                    val league = item.optString("league", "Football")
                    results.add(
                        FootballTeam(
                            teamId = "espn_${id.ifBlank { displayName.lowercase(Locale.ROOT) }}",
                            name = displayName,
                            shortName = shortName,
                            badgeUrl = logoUrl,
                            country = "Global",
                            primaryLeague = league
                        )
                    )
                }
                results
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Dual-API Hybrid Pipeline (`fetchMatchesForDay`):
     *
     * 1. **Fetch & Merge Fixtures + Live Scores in Parallel (`coroutineScope` + `async` + `runCatching` = `Promise.allSettled`)**:
     *    - Fetches the selected date's matches from **TheSportsDB v1 (`123`)** (`eventsday.php?d={YYYY-MM-DD}&s=Soccer` +
     *      specific league events including Moroccan Botola Pro `4520`), **TheSportsDB TV Broadcast Listings**
     *      (`eventstv.php?d={YYYY-MM-DD}&s=Soccer`), AND **SportAPI.ai** (`https://sportapi.ai/api`) in parallel.
     *    - Normalizes team names (strips accents, "FC", "AC", "CA", "Club") to match records between SportAPI.ai and TheSportsDB.
     *    - Uses **SportAPI.ai** as the primary source of truth for real-time scores, minute clock, and live status (`1H`, `HT`, `2H`, `FT`),
     *      and **TheSportsDB** for high-res `/tiny` team badges (`strHomeTeamBadge/tiny`, `strAwayTeamBadge/tiny`) and league schedules.
     *
     * 2. **TV Channel Provider Resolution (TheSportsDB `eventstv.php` + Smart MENA Fallback Map)**:
     *    - Cross-references each match `idEvent` (and normalized team pair) with `eventstv.php?d={YYYY-MM-DD}&s=Soccer` (`tvevents` -> `strChannel`).
     *    - Filters and prioritizes MENA & regional broadcasters first (`beIN Sports`, `Arryadia`, `SNRT`, `Tamazight`, `SSC`, `AD Sports`, `Alkass`).
     *    - Applies the **Smart MENA Fallback Map** if `eventstv.php` returns no MENA channel for a match.
     */
    suspend fun fetchMatchesForDay(
        dayTab: MatchDayTab,
        forceLiveRefresh: Boolean = false
    ): List<FootballMatch> = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Africa/Casablanca"))
        calendar.add(Calendar.DAY_OF_YEAR, dayTab.dayOffset)
        val targetDate = calendar.time

        val espnDateStr = SimpleDateFormat("yyyyMMdd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Africa/Casablanca")
        }.format(targetDate)

        val isoDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Africa/Casablanca")
        }.format(targetDate)

        // Execute all API calls in parallel with fault-tolerant supervisorScope + runCatching (Promise.allSettled behavior)
        val parallelResults = supervisorScope {
            val sportApiDeferred = async {
                runCatching { fetchSportApiAiDayMatches(isoDateStr, dayTab, forceLiveRefresh) }
                    .getOrDefault(emptyList())
            }
            val sportsDbAllSoccerDeferred = async {
                runCatching { fetchTheSportsDbDayMatches(isoDateStr, dayTab, forceLiveRefresh) }
                    .getOrDefault(emptyList())
            }
            val sportsDbLeagueJobs = ALL_TOURNAMENTS
                .filter { it.sportsDbLeagueId.isNotBlank() }
                .map { tournament ->
                    async {
                        runCatching { fetchTheSportsDbLeagueEvents(isoDateStr, tournament.sportsDbLeagueId, dayTab) }
                            .getOrDefault(emptyList())
                    }
                }
            val sportsDbTvDeferred = async {
                runCatching { fetchTheSportsDbTvListings(isoDateStr) }
                    .getOrDefault(emptyList())
            }
            val espnJobs = ALL_TOURNAMENTS.filter { it.espnSlug.isNotBlank() }.map { tournament ->
                async {
                    runCatching { fetchEspnLeagueScoreboard(tournament, espnDateStr, isoDateStr, dayTab) }
                        .getOrDefault(emptyList())
                }
            }
            val apiFootballDeferred = async {
                runCatching { fetchApiFootballDayMatches(isoDateStr, dayTab) }
                    .getOrDefault(emptyList())
            }

            HybridParallelPayload(
                sportApiMatches = sportApiDeferred.await(),
                sportsDbMatches = sportsDbAllSoccerDeferred.await() + sportsDbLeagueJobs.awaitAll().flatten(),
                tvBroadcasts = sportsDbTvDeferred.await(),
                espnMatches = espnJobs.awaitAll().flatten(),
                apiFootballMatches = apiFootballDeferred.await()
            )
        }

        val mergedMap = LinkedHashMap<String, FootballMatch>()

        fun buildMatchPairKey(homeName: String, awayName: String): String {
            val h = normalizeTeamNameForMerge(homeName).ifBlank { homeName.lowercase(Locale.ROOT).trim() }
            val a = normalizeTeamNameForMerge(awayName).ifBlank { awayName.lowercase(Locale.ROOT).trim() }
            return "${h}__${a}"
        }

        // Step 1: Seed with TheSportsDB v1 (`123`) fixtures (authoritative for `/tiny` badges & `idEvent`)
        for (tsdbMatch in parallelResults.sportsDbMatches) {
            val key = buildMatchPairKey(tsdbMatch.homeTeamName, tsdbMatch.awayTeamName)
            val existing = mergedMap[key]
            if (existing == null) {
                mergedMap[key] = tsdbMatch
            } else {
                mergedMap[key] = mergeMatchRecords(primaryLive = existing, badgeAndTvSource = tsdbMatch)
            }
        }

        // Step 2: Merge SportAPI.ai fixtures & real-time live scores (authoritative for live scores, minute clock & `1H`/`HT`/`2H`/`FT` status)
        for (sportApiMatch in parallelResults.sportApiMatches) {
            val key = buildMatchPairKey(sportApiMatch.homeTeamName, sportApiMatch.awayTeamName)
            val existing = mergedMap[key]
            if (existing == null) {
                mergedMap[key] = sportApiMatch
            } else {
                // SportAPI.ai is the source of truth for real-time scores, minute clock, and live status;
                // TheSportsDB is the source of truth for `/tiny` high-res badges and `sportsDbEventId`.
                mergedMap[key] = mergeMatchRecords(primaryLive = sportApiMatch, badgeAndTvSource = existing)
            }
        }

        // Step 3: Supplement with API-Football & ESPN real-time scoreboard fixtures
        for (extraMatch in (parallelResults.apiFootballMatches + parallelResults.espnMatches)) {
            val key = buildMatchPairKey(extraMatch.homeTeamName, extraMatch.awayTeamName)
            val existing = mergedMap[key]
            if (existing == null) {
                mergedMap[key] = extraMatch
            } else {
                // Prefer real-time scoreboard status/scores/clock over static fixture entries while preserving `/tiny` badges & IDs
                val preferExtraLive = (existing.status == MatchStatus.UPCOMING && extraMatch.status != MatchStatus.UPCOMING) ||
                    (existing.matchId.startsWith("tsdb_") && (extraMatch.status != MatchStatus.UPCOMING || extraMatch.espnEventId.isNotBlank()))
                mergedMap[key] = if (preferExtraLive) {
                    mergeMatchRecords(primaryLive = extraMatch, badgeAndTvSource = existing)
                } else {
                    mergeMatchRecords(primaryLive = existing, badgeAndTvSource = extraMatch)
                }
            }
        }

        // Step 4: Cross-reference all matches with TheSportsDB `eventstv.php` TV listings + Slot-Aware MENA Broadcaster Resolver
        resolveDayMatchesTvChannelsWithMenaPriority(mergedMap.values.toList(), parallelResults.tvBroadcasts)
    }

    private data class HybridParallelPayload(
        val sportApiMatches: List<FootballMatch>,
        val sportsDbMatches: List<FootballMatch>,
        val tvBroadcasts: List<SportsDbTvBroadcastEntry>,
        val espnMatches: List<FootballMatch>,
        val apiFootballMatches: List<FootballMatch>
    )

    /**
     * Merges a live-authoritative match record (SportAPI.ai / ESPN Live) with a badge/TV-authoritative match record (TheSportsDB).
     */
    internal fun mergeMatchRecords(
        primaryLive: FootballMatch,
        badgeAndTvSource: FootballMatch
    ): FootballMatch {
        val bestHomeBadge = when {
            badgeAndTvSource.homeTeamBadge.endsWith("/tiny") -> badgeAndTvSource.homeTeamBadge
            primaryLive.homeTeamBadge.endsWith("/tiny") -> primaryLive.homeTeamBadge
            badgeAndTvSource.homeTeamBadge.isNotBlank() -> toTinySportsDbBadgeUrl(badgeAndTvSource.homeTeamBadge)
            else -> toTinySportsDbBadgeUrl(primaryLive.homeTeamBadge)
        }
        val bestAwayBadge = when {
            badgeAndTvSource.awayTeamBadge.endsWith("/tiny") -> badgeAndTvSource.awayTeamBadge
            primaryLive.awayTeamBadge.endsWith("/tiny") -> primaryLive.awayTeamBadge
            badgeAndTvSource.awayTeamBadge.isNotBlank() -> toTinySportsDbBadgeUrl(badgeAndTvSource.awayTeamBadge)
            else -> toTinySportsDbBadgeUrl(primaryLive.awayTeamBadge)
        }
        val resolvedEventId = badgeAndTvSource.sportsDbEventId.ifBlank { primaryLive.sportsDbEventId }
        val resolvedEspnId = primaryLive.espnEventId.ifBlank { badgeAndTvSource.espnEventId }
        val resolvedStadium = if (primaryLive.stadiumName != "Main Stadium" && primaryLive.stadiumName.isNotBlank()) {
            primaryLive.stadiumName
        } else {
            badgeAndTvSource.stadiumName
        }
        val mergedFallbacks = deduplicateMenaChannelOptions(
            primaryLive.fallbackMenaChannels + badgeAndTvSource.fallbackMenaChannels
        )
        val mergedSourceTv = (primaryLive.sourceTvChannels + badgeAndTvSource.sourceTvChannels)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        // If badgeAndTvSource carried an explicit source TV channel (or primaryLive only had a generic placeholder),
        // preserve the source TV channel on the merged record.
        val resolvedBeinChannel = when {
            primaryLive.sourceTvChannels.isNotEmpty() -> primaryLive.sourceTvChannels.first()
            badgeAndTvSource.sourceTvChannels.isNotEmpty() -> badgeAndTvSource.sourceTvChannels.first()
            isGenericPlaceholderMenaChannel(primaryLive.beinChannel) && !isGenericPlaceholderMenaChannel(badgeAndTvSource.beinChannel) ->
                badgeAndTvSource.beinChannel
            else -> primaryLive.beinChannel
        }

        return primaryLive.copy(
            beinChannel = resolvedBeinChannel,
            homeTeamBadge = bestHomeBadge,
            awayTeamBadge = bestAwayBadge,
            stadiumName = resolvedStadium,
            sportsDbEventId = resolvedEventId,
            espnEventId = resolvedEspnId,
            fallbackMenaChannels = mergedFallbacks,
            sourceTvChannels = mergedSourceTv,
            halfTimeScore = primaryLive.halfTimeScore ?: badgeAndTvSource.halfTimeScore,
            fullTimeScore = primaryLive.fullTimeScore ?: badgeAndTvSource.fullTimeScore,
            goalScorersSummary = primaryLive.goalScorersSummary ?: badgeAndTvSource.goalScorersSummary,
            homeRedCards = maxOf(primaryLive.homeRedCards, badgeAndTvSource.homeRedCards),
            awayRedCards = maxOf(primaryLive.awayRedCards, badgeAndTvSource.awayRedCards)
        )
    }

    /**
     * Resolves MENA TV Broadcast Channels across the entire day's schedule so concurrent matches
     * in beIN-broadcast competitions receive distinct, authentic beIN Sports MENA channels
     * (`beIN Sports 1 HD` through `beIN Sports 9 HD`, `beIN Sports 1 4K`, `beIN Sports 1 English`, `beIN Sports 1 French`,
     * `beIN Sports Xtra 1..3`, `beIN Sports Max 4..6`), and non-beIN competitions display the real MENA broadcaster
     * (`AD Sports Premium 1`, `STARZPLAY`, `Arryadia HD`, `SSC 1 HD`, etc.).
     *
     * Simultaneous matches across beIN competitions at the same kickoff time coordinate their slot indices
     * so two concurrent games never collide on the same beIN channel number.
     */
    internal fun resolveDayMatchesTvChannelsWithMenaPriority(
        matches: List<FootballMatch>,
        tvBroadcasts: List<SportsDbTvBroadcastEntry>
    ): List<FootballMatch> {
        if (matches.isEmpty()) return emptyList()

        // Track slot indices per tournament+time AND track already-claimed beIN channel numbers at each kickoff time
        // so simultaneous games across the beIN network get distinct channels.
        val slotCounterByGroupAndTime = HashMap<String, Int>()
        val claimedBeinChannelsByTime = HashMap<String, MutableSet<String>>()

        // Order matches so marquee games and higher-priority tournaments claim flagship channels first
        val indexedMatches = matches.withIndex().sortedWith(
            compareBy<IndexedValue<FootballMatch>> { it.value.kickoffTimeFormatted }
                .thenByDescending { isMarqueeMenaMatch(it.value.homeTeamName, it.value.awayTeamName) }
                .thenBy { tournamentPriorityRank(it.value.tournamentId) }
                .thenBy { it.index }
        )

        val resolvedByOriginalIndex = arrayOfNulls<FootballMatch>(matches.size)

        for (indexed in indexedMatches) {
            val match = indexed.value
            val isBeinTournament = isBeinProvidedTournament(match.tournamentId, match.tournamentName)
            val groupKey = if (isBeinTournament) {
                "bein_${match.tournamentId}_${match.kickoffTimeFormatted}"
            } else {
                "nonbein_${match.tournamentId}_${match.kickoffTimeFormatted}"
            }
            val currentSlotIdx = slotCounterByGroupAndTime.getOrDefault(groupKey, 0)
            slotCounterByGroupAndTime[groupKey] = currentSlotIdx + 1

            val claimedSet = claimedBeinChannelsByTime.getOrPut(match.kickoffTimeFormatted) { mutableSetOf() }

            val resolvedMatch = resolveMatchTvChannelsWithMenaPriority(
                match = match,
                tvBroadcasts = tvBroadcasts,
                slotIndex = currentSlotIdx,
                claimedChannelsAtKickoff = claimedSet
            )

            if (isBeinTournament && resolvedMatch.beinChannel.isNotBlank()) {
                val normKey = normalizeChannelNameForFavoritesMatch(resolvedMatch.beinChannel).canonicalKey
                if (normKey.isNotBlank()) {
                    claimedSet.add(normKey)
                }
            }

            resolvedByOriginalIndex[indexed.index] = resolvedMatch
        }

        return resolvedByOriginalIndex.filterNotNull()
    }

    private fun tournamentPriorityRank(tournamentId: String): Int {
        return ALL_TOURNAMENTS.firstOrNull { it.tournamentId == tournamentId }?.defaultSortOrder ?: 99
    }

    /**
     * Step 2 of Hybrid Pipeline: TV Channel Provider Resolution (TheSportsDB `eventstv.php` + Canonical MENA Broadcaster Engine).
     *
     * Rules:
     * 1. **If an upstream source (`eventstv.php`, `strTVStation`, `tv_channel`, `broadcasts`, or custom source) provides a specific MENA channel**:
     *    Preserves the exact channel name from the source (e.g., `"beIN Sports max 5"`, `"beIN Sports 1 HD"`, `"beIN Sports 1 4K"`, `"beIN Sports Xtra 1"`).
     * 2. **If beIN Sports provides the competition and no specific numbered channel was in the raw feed**:
     *    Allocates an authentic **beIN Sports MENA** channel according to real beIN Sports league participation:
     *    - UEFA Champions League: `beIN Sports 1 HD` .. `beIN Sports 6 HD`, `beIN Sports 1 4K`, `beIN Sports 1 English`, `beIN Sports 1 French`, `beIN Sports Xtra 1`
     *    - Premier League: `beIN Sports 1 HD`, `beIN Sports 2 HD`, `beIN Sports 1 English`, `beIN Sports 3 HD`, `beIN Sports 4 HD`, `beIN Sports 1 4K`
     *    - LaLiga EA Sports: `beIN Sports 3 HD`, `beIN Sports 1 HD`, `beIN Sports 2 HD`, `beIN Sports 4 HD`, `beIN Sports 1 4K`
     *    - Ligue 1 McDonald's: `beIN Sports 4 HD`, `beIN Sports 2 HD`, `beIN Sports 1 French`, `beIN Sports 5 HD`, `beIN Sports Max 4`
     *    - Bundesliga: `beIN Sports 5 HD`, `beIN Sports 6 HD`, `beIN Sports 4 HD`, `beIN Sports Xtra 1`, `beIN Sports Max 5`
     *    - CAF Champions League: `beIN Sports 6 HD`, `beIN Sports 5 HD`, `Arryadia TNT`, `ON Time Sports 1`
     *    - UEFA Europa League: `beIN Sports 1 HD` .. `beIN Sports 6 HD`, `beIN Sports Xtra 1`
     * 3. **If beIN Sports does NOT provide the game**:
     *    Displays the real MENA broadcaster (`AD Sports Premium 1`, `STARZPLAY`, `Arryadia HD`, `Arryadia TNT`, `SSC 1 HD`, `SSC Extra 1`, etc.).
     */
    internal fun resolveMatchTvChannelsWithMenaPriority(
        match: FootballMatch,
        tvBroadcasts: List<SportsDbTvBroadcastEntry>,
        slotIndex: Int = 0,
        claimedChannelsAtKickoff: Set<String> = emptySet()
    ): FootballMatch {
        val isMarquee = isMarqueeMenaMatch(match.homeTeamName, match.awayTeamName)

        val matchingTvEntries = tvBroadcasts.filter { entry ->
            (match.sportsDbEventId.isNotBlank() && entry.eventId == match.sportsDbEventId) ||
                doesTvEntryMatchFixtureTeams(entry, match.homeTeamName, match.awayTeamName)
        }

        val rawSlotFallbacks = getSmartMenaFallbackChannels(
            tournamentId = match.tournamentId,
            tournamentName = match.tournamentName,
            slotIndex = slotIndex,
            isMarquee = isMarquee,
            homeTeamName = match.homeTeamName,
            awayTeamName = match.awayTeamName
        )

        // Reorder slotFallbacks so any channel not yet claimed at this exact kickoff time is preferred first
        val slotFallbacks = if (claimedChannelsAtKickoff.isEmpty()) {
            rawSlotFallbacks
        } else {
            val unclaimed = rawSlotFallbacks.filter { ch ->
                normalizeChannelNameForFavoritesMatch(ch).canonicalKey !in claimedChannelsAtKickoff
            }
            val claimed = rawSlotFallbacks.filter { ch ->
                normalizeChannelNameForFavoritesMatch(ch).canonicalKey in claimedChannelsAtKickoff
            }
            unclaimed + claimed
        }

        val defaultSlotPrimary = slotFallbacks.firstOrNull() ?: rawSlotFallbacks.first()
        val defaultChannelNum = extractChannelNumberOrDefault(defaultSlotPrimary, (slotIndex % 9) + 1)
        val defaultBeinTier = extractBeinTierOrDefault(defaultSlotPrimary, "HD")

        // 1. Channels from TheSportsDB `eventstv.php` (beIN Sports names preserved EXACTLY as from source,
        //    while generic un-numbered placeholders like "beIN Sports MENA" are resolved to the match's authentic channel)
        val menaChannelsFromTvDb = matchingTvEntries
            .map { it.channelName.trim() }
            .filter { isMenaBroadcasterChannel(it) && !isGenericPlaceholderMenaChannel(it) }
            .map { rawCh ->
                formatCanonicalMenaChannelName(
                    rawChannel = rawCh,
                    defaultChannelNum = defaultChannelNum,
                    defaultBeinTier = defaultBeinTier
                )
            }
            .distinct()
            .sortedBy { menaBroadcasterPriorityRank(it, match.tournamentId) }

        // 2. Channels explicitly provided by upstream match payload (`strTVStation`, `tv_channel`, `broadcast`, or explicit custom channel)
        val upstreamSourceChannels = match.sourceTvChannels
            .map { it.trim() }
            .filter { it.isNotBlank() && isMenaBroadcasterChannel(it) && !isGenericPlaceholderMenaChannel(it) }
            .map { rawCh ->
                formatCanonicalMenaChannelName(
                    rawChannel = rawCh,
                    defaultChannelNum = defaultChannelNum,
                    defaultBeinTier = defaultBeinTier
                )
            }

        val existingSpecificChannels = (listOf(match.beinChannel) + match.fallbackMenaChannels)
            .map { it.trim() }
            .filter { it.isNotBlank() && isMenaBroadcasterChannel(it) && !isGenericPlaceholderMenaChannel(it) }
            .map { rawCh ->
                formatCanonicalMenaChannelName(
                    rawChannel = rawCh,
                    defaultChannelNum = defaultChannelNum,
                    defaultBeinTier = defaultBeinTier
                )
            }

        // If `match.beinChannel` was explicitly set to a custom source channel (not from default fallback pools),
        // treat it as a source channel so its exact name is preserved first.
        val isCustomExplicitBein = match.beinChannel.isNotBlank() &&
            !isGenericPlaceholderMenaChannel(match.beinChannel) &&
            isMenaBroadcasterChannel(match.beinChannel) &&
            match.beinChannel.trim() !in DEFAULT_GENERATED_POOL_CHANNELS

        val prioritizedSourceChannels = buildList {
            addAll(menaChannelsFromTvDb)
            addAll(upstreamSourceChannels)
            if (isCustomExplicitBein) {
                add(
                    formatCanonicalMenaChannelName(
                        rawChannel = match.beinChannel,
                        defaultChannelNum = defaultChannelNum,
                        defaultBeinTier = defaultBeinTier
                    )
                )
            }
        }

        // Deduplicate equivalent channel variations (e.g. 'beIN Sports max 5' vs 'beIN Sports 5 Max')
        // while preserving the exact source channel name first.
        val combinedChannels = deduplicateMenaChannelOptions(
            prioritizedSourceChannels + slotFallbacks + existingSpecificChannels
        )

        val primaryChannel = combinedChannels.firstOrNull() ?: defaultSlotPrimary
        val fallbackList = combinedChannels.drop(1)
        val providerName = detectMenaProviderFromLabel(primaryChannel)

        val hGoals = match.homeScore ?: 0
        val aGoals = match.awayScore ?: 0
        val realHt = when {
            match.halfTimeScore != null -> match.halfTimeScore
            match.status == MatchStatus.HALFTIME && match.homeScore != null && match.awayScore != null ->
                "$hGoals - $aGoals"
            else -> null
        }
        val realFt = match.fullTimeScore ?: if (match.status == MatchStatus.FINISHED && match.homeScore != null && match.awayScore != null) {
            "$hGoals - $aGoals"
        } else null

        return match.copy(
            beinChannel = primaryChannel,
            beinChannelNumberHint = extractBeinHint(primaryChannel),
            menaProviderName = providerName,
            fallbackMenaChannels = fallbackList,
            sourceTvChannels = (menaChannelsFromTvDb + upstreamSourceChannels).distinct(),
            halfTimeScore = realHt,
            fullTimeScore = realFt
        )
    }

    /**
     * Detects un-numbered or region-generic beIN Sports placeholder strings (such as `"beIN Sports"`,
     * `"beIN Sports MENA"`, `"beIN Sports Connect"`, `"beIN Sports Arabia"`, `"beIN Sports HD"`,
     * `"beIN Sports 1 MENA"`, `"beIN Sports 2 MENA"`, `"beIN SPORTS CONNECT Arabia"`) so the resolver
     * upgrades them to the match's specific numbered beIN Sports channel (`beIN Sports 1 HD` .. `beIN Sports 9 HD`).
     */
    private fun isGenericPlaceholderMenaChannel(rawChannel: String): Boolean {
        val lower = rawChannel.trim().lowercase(Locale.ROOT)
            .replace(Regex("\\s+"), " ")
        if (lower in setOf(
                "bein sports 1 mena",
                "bein sports 2 mena",
                "bein sports mena",
                "bein sports",
                "bein sport",
                "bein sports hd",
                "bein sports connect",
                "bein sports connect arabia",
                "bein connect",
                "bein connect mena",
                "bein sports arabia",
                "bein sports france",
                "bein sports us",
                "bein sports usa",
                "bein sports en espanol",
                "bein sports australia",
                "tod",
                "tod tv"
            )
        ) {
            return true
        }
        // If it's a beIN channel with NO channel number and NO specific sub-brand (4K, AFC, News), treat as generic
        if ((lower.contains("bein") || lower.contains("be in")) &&
            !Regex("""\b\d+\b""").containsMatchIn(lower) &&
            !lower.contains("4k") &&
            !lower.contains("uhd") &&
            !lower.contains("afc") &&
            !lower.contains("one") &&
            !lower.contains("two") &&
            !lower.contains("three")
        ) {
            return true
        }
        return false
    }

    internal fun isBeinProvidedTournament(tournamentId: String, tournamentName: String = ""): Boolean {
        val lowerName = tournamentName.lowercase(Locale.ROOT)
        if (tournamentId in setOf("botola_pro", "serie_a", "saudi_pro")) return false
        if (lowerName.contains("botola") ||
            lowerName.contains("coupe du tr") ||
            lowerName.contains("serie a") ||
            lowerName.contains("coppa italia") ||
            lowerName.contains("saudi") ||
            lowerName.contains("roshn")
        ) {
            return false
        }
        return true
    }

    private fun isMarqueeMenaMatch(homeTeamName: String, awayTeamName: String): Boolean {
        val combined = "${homeTeamName.lowercase(Locale.ROOT)} ${awayTeamName.lowercase(Locale.ROOT)}"
        val marqueeKeywords = listOf(
            "real madrid", "barcelona", "manchester city", "liverpool", "arsenal",
            "bayern", "paris saint-germain", "psg", "inter", "internazionale",
            "milan", "juventus", "chelsea", "manchester united", "atletico madrid",
            "dortmund", "raja", "wydad", "as far", "berkane", "al ahly", "al hilal", "al nassr"
        )
        return marqueeKeywords.any { combined.contains(it) }
    }

    private fun extractChannelNumberOrDefault(channelLabel: String, defaultNum: Int): Int {
        val withoutRes = channelLabel.lowercase(Locale.ROOT)
            .replace("4k", " ")
            .replace("2160p", " ")
            .replace("1080p", " ")
            .replace("1080i", " ")
            .replace("720p", " ")
            .replace("h265", " ")
            .replace("h264", " ")
        val num = Regex("""\b([1-9]\d?)\b""").find(withoutRes)?.groupValues?.get(1)?.toIntOrNull()
        return num ?: defaultNum
    }

    private fun extractBeinTierOrDefault(channelLabel: String, defaultTier: String): String {
        val lower = channelLabel.lowercase(Locale.ROOT)
        return when {
            lower.contains("4k") || lower.contains("uhd") -> "4K"
            lower.contains("max") -> "Max"
            lower.contains("extra") || lower.contains("xtra") -> "Extra"
            lower.contains("premium") -> "Premium"
            lower.contains("english") || Regex("""\ben\b""").containsMatchIn(lower) -> "English"
            lower.contains("french") || Regex("""\bfr\b""").containsMatchIn(lower) -> "French"
            lower.contains("afc") -> "AFC"
            Regex("""\bhd\b""").containsMatchIn(lower) -> "HD"
            else -> defaultTier
        }
    }

    /**
     * Formats MENA broadcaster channels:
     * - **If beIN Sports provides the game**:
     *   Preserves and displays the name of the channel from the source **EXACTLY as it is** (`trimmed`),
     *   e.g. `"beIN Sports max 5"`, `"beIN Sports HD 2 Max"`, `"beIN Sports Xtra 1"`, `"beIN Sports 1 4K"`, `"beIN Sports 1 HD"`.
     * - **If beIN Sports does NOT provide the game**:
     *   Returns the authentic MENA broadcaster channel name (`AD Sports Premium 1`, `STARZPLAY`,
     *   `Arryadia HD`, `Arryadia TNT`, `Al Aoula HD`, `Tamazight HD`, `SSC 1 HD`, `SSC Extra 1`, `Alkass One HD`, `ON Time Sports 1`, etc.).
     */
    fun formatCanonicalMenaChannelName(
        rawChannel: String,
        defaultChannelNum: Int = 1,
        defaultBeinTier: String = "HD"
    ): String {
        val trimmed = rawChannel.trim()
        if (trimmed.isBlank()) return "beIN Sports $defaultChannelNum $defaultBeinTier"
        val lower = trimmed.lowercase(Locale.ROOT)

        // 1. If beIN Sports provides the game: display the channel name from the source EXACTLY as it is
        if (lower.contains("bein") || lower.contains("be in")) {
            return trimmed
        }

        // 2. Non-beIN MENA Broadcasters — preserve/normalize real channel name for the MENA Zone
        val num = extractChannelNumberOrDefault(trimmed, 1)
        return when {
            lower.contains("arryadia") || lower.contains("riyadia") -> {
                if (lower.contains("tnt")) "Arryadia TNT" else "Arryadia HD"
            }
            lower.contains("al aoula") || lower.contains("aoula") -> "Al Aoula HD"
            lower.contains("tamazight") -> "Tamazight HD"
            lower.contains("maghribia") -> "Al Maghribia"
            lower.contains("snrt") -> "SNRT Live"
            lower.contains("starz") -> "STARZPLAY"
            lower.contains("ad sport") || lower.contains("abu dhabi") -> {
                when {
                    lower.contains("asia") || lower.contains("afc") -> "AD Sports Asia $num"
                    else -> "AD Sports Premium $num"
                }
            }
            lower.contains("thmanyah") -> "Thmanyah Sports $num"
            lower.contains("ssc") -> {
                when {
                    lower.contains("extra") || lower.contains("xtra") -> "SSC Extra $num"
                    lower.contains("4k") || lower.contains("uhd") -> "SSC $num 4K"
                    else -> "SSC $num HD"
                }
            }
            lower.contains("shahid") -> "Shahid VIP"
            lower.contains("alkass") || lower.contains("al kass") || lower.contains("kass") -> {
                val wordNum = when {
                    lower.contains("two") || num == 2 -> "Two"
                    lower.contains("three") || num == 3 -> "Three"
                    lower.contains("four") || num == 4 -> "Four"
                    else -> "One"
                }
                "Alkass $wordNum HD"
            }
            lower.contains("on time") || lower.contains("ontime") -> "ON Time Sports $num"
            lower.contains("dubai sport") -> "Dubai Sports $num"
            lower.contains("sharjah sport") -> "Sharjah Sports"
            lower.contains("kuwait sport") -> "Kuwait Sports"
            lower.contains("oman sport") -> "Oman Sports"
            lower.contains("bahrain sport") -> "Bahrain Sports"
            lower.contains("iraqia") -> "Iraqia Sports"
            lower.contains("watania") -> "El Watania $num"
            else -> trimmed
        }
    }

    private fun isMenaBroadcasterChannel(channelName: String): Boolean {
        val lower = channelName.lowercase(Locale.ROOT)
        return MENA_BROADCASTER_KEYWORDS.any { keyword -> lower.contains(keyword) }
    }

    private fun menaBroadcasterPriorityRank(channelName: String, tournamentId: String = ""): Int {
        val lower = channelName.lowercase(Locale.ROOT)
        // If competition is exclusively on a non-beIN MENA network, prioritize that network first
        if (tournamentId == "serie_a" && (lower.contains("ad sport") || lower.contains("starz"))) return 0
        if (tournamentId == "botola_pro" && (lower.contains("arryadia") || lower.contains("snrt") || lower.contains("al aoula") || lower.contains("tamazight"))) return 0
        if (tournamentId == "saudi_pro" && (lower.contains("ssc") || lower.contains("thmanyah") || lower.contains("shahid"))) return 0

        return when {
            lower.contains("bein") -> 1
            lower.contains("arryadia") || lower.contains("riyadia") -> 2
            lower.contains("snrt") || lower.contains("al aoula") || lower.contains("maghribia") -> 3
            lower.contains("tamazight") -> 4
            lower.contains("ssc") || lower.contains("thmanyah") -> 5
            lower.contains("ad sport") || lower.contains("abu dhabi") || lower.contains("starz") -> 6
            lower.contains("alkass") || lower.contains("al kass") -> 7
            lower.contains("on time") || lower.contains("dubai sport") -> 8
            else -> 99
        }
    }

    /**
     * Authentic beIN Sports MENA & Regional Broadcaster Participation Map:
     *
     * Reflects how **beIN Sports MENA** actually allocates its linear and overflow channels across competitions:
     * - **Flagship Channels (`beIN Sports 1 HD` & `beIN Sports 2 HD`)**:
     *   Premier League top clashes, UEFA Champions League headline fixtures, LaLiga El Clásico / Madrid / Barça.
     * - **`beIN Sports 3 HD`**: Dedicated primary channel for **LaLiga EA Sports** + simultaneous UCL / EPL fixtures.
     * - **`beIN Sports 4 HD`**: Dedicated primary channel for **Ligue 1 McDonald's** + UCL / EPL / LaLiga overflow.
     * - **`beIN Sports 5 HD`**: Dedicated primary channel for **German Bundesliga** + European / CAF overflow.
     * - **`beIN Sports 6 HD`**: Dedicated primary channel for **CAF Champions League / Confederation Cup** & **UEFA Europa League**.
     * - **`beIN Sports 7 HD`, `8 HD`, `9 HD`**: Simultaneous multi-match UEFA Champions League / Europa League matchdays.
     * - **Language Feeds (`beIN Sports 1 English`, `beIN Sports 2 English`, `beIN Sports 1 French`)**: English & French commentary feeds.
     * - **Overflow & Tournament Feeds (`beIN Sports 1 4K`, `beIN Sports Xtra 1..3`, `beIN Sports Max 4..6`, `beIN Sports 1 AFC`)**:
     *   Included as additional selectable options in the match's channel switcher.
     */
    fun getSmartMenaFallbackChannels(
        tournamentId: String,
        tournamentName: String,
        slotIndex: Int = 0,
        isMarquee: Boolean = false,
        homeTeamName: String = "",
        awayTeamName: String = ""
    ): List<String> {
        val lowerName = tournamentName.lowercase(Locale.ROOT)
        val idx = slotIndex.coerceAtLeast(0)

        return when {
            // 1. Botola Pro Inwi & Coupe du Trône (Morocco — SNRT / Arryadia)
            tournamentId == "botola_pro" ||
                lowerName.contains("botola") ||
                lowerName.contains("coupe du tr") ||
                lowerName.contains("throne cup") -> {
                val pool = listOf("Arryadia HD", "Arryadia TNT", "Al Aoula HD", "Tamazight HD")
                val primary = pool[idx % pool.size]
                (listOf(primary) + pool.filter { it != primary } + listOf("SNRT Live")).distinct()
            }

            // 2. Italian Serie A & Coppa Italia (MENA — AD Sports & STARZPLAY)
            tournamentId == "serie_a" ||
                lowerName.contains("serie a") ||
                lowerName.contains("coppa italia") -> {
                val pool = listOf("AD Sports Premium 1", "AD Sports Premium 2", "STARZPLAY")
                val primary = pool[idx % pool.size]
                (listOf(primary) + pool.filter { it != primary }).distinct()
            }

            // 3. Roshn Saudi League & King's Cup (MENA — SSC Sports / Thmanyah)
            tournamentId == "saudi_pro" ||
                lowerName.contains("saudi") ||
                lowerName.contains("roshn") -> {
                val pool = listOf("SSC 1 HD", "SSC Extra 1", "SSC Extra 2", "SSC 2 HD", "Thmanyah Sports 1")
                val primary = pool[idx % pool.size]
                (listOf(primary) + pool.filter { it != primary }).distinct()
            }

            // 4. CAF Champions League & Confederation Cup (beIN Sports 6 HD / 5 HD + terrestrial host broadcasters)
            tournamentId == "caf_cl" ||
                lowerName.contains("caf champions") ||
                lowerName.contains("confederation cup") ||
                lowerName.contains("caf ") -> {
                val beinCafPool = listOf(
                    "beIN Sports 6 HD",
                    "beIN Sports 5 HD",
                    "beIN Sports 3 HD",
                    "beIN Sports 4 HD",
                    "beIN Sports Xtra 1"
                )
                val primary = beinCafPool[idx % beinCafPool.size]
                val regionalExtras = buildList {
                    val combinedTeams = "${homeTeamName.lowercase(Locale.ROOT)} ${awayTeamName.lowercase(Locale.ROOT)}"
                    if (combinedTeams.contains("raja") || combinedTeams.contains("wydad") || combinedTeams.contains("far") || combinedTeams.contains("berkane")) {
                        add("Arryadia TNT")
                    }
                    if (combinedTeams.contains("ahly") || combinedTeams.contains("zamalek") || combinedTeams.contains("pyramids")) {
                        add("ON Time Sports 1")
                    }
                    if (isEmpty()) {
                        add("Arryadia TNT")
                    }
                }
                (listOf(primary) + regionalExtras + beinCafPool.filter { it != primary }).distinct()
            }

            // 5. UEFA Champions League (beIN Sports 1 HD .. 8 HD + 1 4K + 1 English + 1 French + Xtra 1)
            tournamentId == "uefa_cl" || lowerName.contains("uefa champions") -> {
                val uclPool = listOf(
                    "beIN Sports 1 HD",
                    "beIN Sports 2 HD",
                    "beIN Sports 3 HD",
                    "beIN Sports 4 HD",
                    "beIN Sports 5 HD",
                    "beIN Sports 6 HD",
                    "beIN Sports 7 HD",
                    "beIN Sports 8 HD",
                    "beIN Sports Xtra 1"
                )
                val primary = uclPool[idx % uclPool.size]
                val next1 = uclPool[(idx + 1) % uclPool.size]
                val next2 = uclPool[(idx + 2) % uclPool.size]
                (listOf(
                    primary,
                    next1,
                    next2,
                    "beIN Sports 1 4K",
                    "beIN Sports 1 English",
                    "beIN Sports 1 French",
                    "beIN Sports Xtra 1"
                ) + uclPool.take(4)).distinct()
            }

            // 6. Spanish LaLiga EA Sports (beIN Sports 3 HD primary + 1 HD / 2 HD for Real Madrid & Barcelona + 4 HD)
            tournamentId == "laliga" || lowerName.contains("la liga") || lowerName.contains("laliga") -> {
                val laligaPool = if (isMarquee) {
                    listOf(
                        "beIN Sports 1 HD",
                        "beIN Sports 3 HD",
                        "beIN Sports 2 HD",
                        "beIN Sports 4 HD",
                        "beIN Sports 1 4K"
                    )
                } else {
                    listOf(
                        "beIN Sports 3 HD",
                        "beIN Sports 1 HD",
                        "beIN Sports 2 HD",
                        "beIN Sports 4 HD",
                        "beIN Sports 5 HD"
                    )
                }
                val primary = laligaPool[idx % laligaPool.size]
                val next1 = laligaPool[(idx + 1) % laligaPool.size]
                (listOf(primary, next1, "beIN Sports 1 HD", "beIN Sports 3 HD", "beIN Sports 1 4K", "beIN Sports 1 English")).distinct()
            }

            // 7. English Premier League (beIN Sports 1 HD & 2 HD primary + 1 English + 3 HD..6 HD for simultaneous 16:00 kickoffs)
            tournamentId == "premier_league" || lowerName.contains("premier league") -> {
                val eplPool = listOf(
                    "beIN Sports 1 HD",
                    "beIN Sports 2 HD",
                    "beIN Sports 3 HD",
                    "beIN Sports 4 HD",
                    "beIN Sports 5 HD",
                    "beIN Sports 6 HD",
                    "beIN Sports Xtra 1"
                )
                val primary = eplPool[idx % eplPool.size]
                val next1 = eplPool[(idx + 1) % eplPool.size]
                (listOf(
                    primary,
                    next1,
                    "beIN Sports 1 English",
                    "beIN Sports 2 English",
                    "beIN Sports 1 4K",
                    "beIN Sports 1 HD",
                    "beIN Sports 2 HD"
                )).distinct()
            }

            // 8. French Ligue 1 McDonald's (beIN Sports 4 HD primary + 2 HD + 1 French + Max 4..6)
            tournamentId == "ligue_1" || lowerName.contains("ligue 1") -> {
                val ligue1Pool = listOf(
                    "beIN Sports 4 HD",
                    "beIN Sports 2 HD",
                    "beIN Sports 5 HD",
                    "beIN Sports 6 HD",
                    "beIN Sports 1 French",
                    "beIN Sports Max 4"
                )
                val primary = ligue1Pool[idx % ligue1Pool.size]
                val next1 = ligue1Pool[(idx + 1) % ligue1Pool.size]
                (listOf(primary, next1, "beIN Sports 1 French", "beIN Sports 4 HD", "beIN Sports Max 4")).distinct()
            }

            // 9. German Bundesliga (beIN Sports 5 HD primary + 6 HD + 4 HD + Xtra 1 + Max 5)
            tournamentId == "bundesliga" || lowerName.contains("bundesliga") -> {
                val bundesPool = listOf(
                    "beIN Sports 5 HD",
                    "beIN Sports 6 HD",
                    "beIN Sports 4 HD",
                    "beIN Sports 7 HD",
                    "beIN Sports Xtra 1",
                    "beIN Sports Max 5"
                )
                val primary = bundesPool[idx % bundesPool.size]
                val next1 = bundesPool[(idx + 1) % bundesPool.size]
                (listOf(primary, next1, "beIN Sports 5 HD", "beIN Sports Xtra 1")).distinct()
            }

            // 10. UEFA Europa League & Conference League (beIN Sports 1 HD .. 8 HD + Xtra 1..2)
            tournamentId == "uefa_el" || lowerName.contains("europa") -> {
                val uelPool = listOf(
                    "beIN Sports 1 HD",
                    "beIN Sports 2 HD",
                    "beIN Sports 3 HD",
                    "beIN Sports 4 HD",
                    "beIN Sports 5 HD",
                    "beIN Sports 6 HD",
                    "beIN Sports 7 HD",
                    "beIN Sports 8 HD",
                    "beIN Sports Xtra 1"
                )
                val primary = uelPool[idx % uelPool.size]
                val next1 = uelPool[(idx + 1) % uelPool.size]
                (listOf(primary, next1, "beIN Sports 1 English", "beIN Sports Xtra 1")).distinct()
            }

            else -> {
                val defaultPool = listOf(
                    "beIN Sports 1 HD",
                    "beIN Sports 2 HD",
                    "beIN Sports 3 HD",
                    "beIN Sports 4 HD",
                    "beIN Sports 1 4K"
                )
                val primary = defaultPool[idx % defaultPool.size]
                val next1 = defaultPool[(idx + 1) % defaultPool.size]
                listOf(primary, next1)
            }
        }
    }

    /**
     * Dedicated Background Polling Pipeline (`pollLiveScoresAndMinutes`):
     * Polls **SportAPI.ai** (`/v1/football/matches/live`, `/v1/football/livescores`, `/v1/football/matches?date={YYYY-MM-DD}`)
     * in parallel with real-time scoreboard deltas and merges live scores, match minute updates (`1H • 38'`, `HT`, `2H • 67'`, `90+3'`, `FT`),
     * half-time/full-time breakdowns, red cards, and goal scorer summaries into the active day's schedule without requiring manual user refresh.
     */
    suspend fun pollLiveScoresAndMinutes(
        dayTab: MatchDayTab,
        existingMatches: List<FootballMatch>
    ): List<FootballMatch> = withContext(Dispatchers.IO) {
        if (existingMatches.isEmpty()) {
            return@withContext fetchMatchesForDay(dayTab = dayTab, forceLiveRefresh = true)
        }

        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Africa/Casablanca"))
        calendar.add(Calendar.DAY_OF_YEAR, dayTab.dayOffset)
        val targetDate = calendar.time

        val espnDateStr = SimpleDateFormat("yyyyMMdd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Africa/Casablanca")
        }.format(targetDate)

        val isoDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Africa/Casablanca")
        }.format(targetDate)

        val (sportApiLiveMatches, espnLiveMatches, apiFootballMatches, sportsDbDayMatches) = supervisorScope {
            val sportApiLiveDeferred = async {
                runCatching {
                    val liveFeed = if (dayTab == MatchDayTab.TODAY) {
                        fetchSportApiAiLiveMatches(isoDateStr, dayTab)
                    } else {
                        emptyList()
                    }
                    val dayFeed = fetchSportApiAiDayMatches(isoDateStr, dayTab, forceLiveRefresh = true)
                    // Prefer dedicated live feed entries first, followed by day feed
                    (liveFeed + dayFeed)
                }.getOrDefault(emptyList())
            }
            val espnDeferred = async {
                runCatching {
                    ALL_TOURNAMENTS.filter { it.espnSlug.isNotBlank() }.map { tournament ->
                        async {
                            runCatching { fetchEspnLeagueScoreboard(tournament, espnDateStr, isoDateStr, dayTab) }
                                .getOrDefault(emptyList())
                        }
                    }.awaitAll().flatten()
                }.getOrDefault(emptyList())
            }
            val apiFootballDeferred = async {
                runCatching { fetchApiFootballDayMatches(isoDateStr, dayTab) }
                    .getOrDefault(emptyList())
            }
            val sportsDbDeferred = async {
                runCatching { fetchTheSportsDbDayMatches(isoDateStr, dayTab, forceLiveRefresh = true) }
                    .getOrDefault(emptyList())
            }

            QuadrupleLiveDelta(
                sportApiMatches = sportApiLiveDeferred.await(),
                espnMatches = espnDeferred.await(),
                apiFootballMatches = apiFootballDeferred.await(),
                sportsDbMatches = sportsDbDeferred.await()
            )
        }

        fun buildMatchPairKey(homeName: String, awayName: String): String {
            val h = normalizeTeamNameForMerge(homeName).ifBlank { homeName.lowercase(Locale.ROOT).trim() }
            val a = normalizeTeamNameForMerge(awayName).ifBlank { awayName.lowercase(Locale.ROOT).trim() }
            return "${h}__${a}"
        }

        val mergedMap = LinkedHashMap<String, FootballMatch>()
        for (existing in existingMatches) {
            mergedMap[buildMatchPairKey(existing.homeTeamName, existing.awayTeamName)] = existing
        }

        // 1. Merge TheSportsDB updates (preserving existing live status if already live)
        for (tsdbMatch in sportsDbDayMatches) {
            val key = buildMatchPairKey(tsdbMatch.homeTeamName, tsdbMatch.awayTeamName)
            val existing = mergedMap[key]
            if (existing == null) {
                mergedMap[key] = tsdbMatch
            } else {
                val primary = if (tsdbMatch.status != MatchStatus.UPCOMING) tsdbMatch else existing
                mergedMap[key] = mergeMatchRecords(primaryLive = primary, badgeAndTvSource = tsdbMatch)
            }
        }

        // 2. Merge SportAPI.ai real-time live scores & match minutes (Primary live source of truth)
        for (sportApiMatch in sportApiLiveMatches) {
            val key = buildMatchPairKey(sportApiMatch.homeTeamName, sportApiMatch.awayTeamName)
            val existing = mergedMap[key]
            if (existing == null) {
                mergedMap[key] = sportApiMatch
            } else {
                mergedMap[key] = mergeMatchRecords(primaryLive = sportApiMatch, badgeAndTvSource = existing)
            }
        }

        // 3. Supplement with real-time scoreboard deltas (API-Football & ESPN)
        for (extraMatch in (apiFootballMatches + espnLiveMatches)) {
            val key = buildMatchPairKey(extraMatch.homeTeamName, extraMatch.awayTeamName)
            val existing = mergedMap[key]
            if (existing == null) {
                mergedMap[key] = extraMatch
            } else {
                val preferExtraLive = (existing.status == MatchStatus.UPCOMING && extraMatch.status != MatchStatus.UPCOMING) ||
                    (extraMatch.status == MatchStatus.LIVE || extraMatch.status == MatchStatus.HALFTIME || extraMatch.status == MatchStatus.FINISHED)
                mergedMap[key] = if (preferExtraLive) {
                    mergeMatchRecords(primaryLive = extraMatch, badgeAndTvSource = existing)
                } else {
                    mergeMatchRecords(primaryLive = existing, badgeAndTvSource = extraMatch)
                }
            }
        }

        val tvBroadcasts = sportsDbTvCache[isoDateStr]?.second.orEmpty()
        val syncedMatches = mergedMap.values.map { match ->
            synchronizeElapsedMatchClock(match, dayTab)
        }
        resolveDayMatchesTvChannelsWithMenaPriority(syncedMatches, tvBroadcasts)
    }

    private data class QuadrupleLiveDelta(
        val sportApiMatches: List<FootballMatch>,
        val espnMatches: List<FootballMatch>,
        val apiFootballMatches: List<FootballMatch>,
        val sportsDbMatches: List<FootballMatch>
    )

    /**
     * Ensures that if a match on `TODAY` is marked `LIVE` by an upstream feed without an explicit minute integer
     * (e.g. raw badge `"LIVE"`, `"1H"`, or `"2H"`), its elapsed minute clock (`1H • 34'`, `HT`, `2H • 71'`)
     * is calculated accurately from its Morocco `GMT+1` kickoff time so the live minute counter ticks smoothly.
     */
    internal fun synchronizeElapsedMatchClock(
        match: FootballMatch,
        dayTab: MatchDayTab
    ): FootballMatch {
        if (dayTab != MatchDayTab.TODAY || match.status != MatchStatus.LIVE) {
            return match
        }
        val badge = match.liveMinuteOrBadge.trim()
        // If the API already supplied an explicit minute (e.g., "1H • 38'", "67'", "90+3'"), keep it
        if (badge.contains("'") && badge.any { it.isDigit() }) {
            return match
        }
        val parts = match.kickoffTimeFormatted.trim().split(":")
        if (parts.size < 2) return match
        val koHour = parts[0].filter { it.isDigit() }.toIntOrNull() ?: return match
        val koMin = parts[1].take(2).filter { it.isDigit() }.toIntOrNull() ?: return match

        val nowCal = Calendar.getInstance(TimeZone.getTimeZone("Africa/Casablanca"))
        val nowTotalMin = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE)
        val koTotalMin = koHour * 60 + koMin
        val elapsedSinceKo = nowTotalMin - koTotalMin

        if (elapsedSinceKo < 0 || elapsedSinceKo > 140) return match

        val computedBadge = when {
            elapsedSinceKo <= 45 -> "1H • ${elapsedSinceKo.coerceAtLeast(1)}'"
            elapsedSinceKo in 46..48 -> "1H • 45+${elapsedSinceKo - 45}'"
            elapsedSinceKo in 49..62 -> "HT"
            elapsedSinceKo in 63..108 -> {
                val secondHalfMinute = (45 + (elapsedSinceKo - 62)).coerceIn(46, 90)
                "2H • ${secondHalfMinute}'"
            }
            else -> {
                val added = (elapsedSinceKo - 108).coerceIn(1, 9)
                "2H • 90+${added}'"
            }
        }
        val computedStatus = if (computedBadge == "HT") MatchStatus.HALFTIME else MatchStatus.LIVE
        return match.copy(
            status = computedStatus,
            liveMinuteOrBadge = computedBadge
        )
    }

    /**
     * Queries SportAPI.ai's dedicated real-time live/in-play endpoints:
     * - `GET https://sportapi.ai/api/v1/football/matches/live`
     * - `GET https://sportapi.ai/api/v1/football/livescores`
     * - `GET https://sportapi.ai/api/v1/football/matches?date={YYYY-MM-DD}&live=true`
     */
    private fun fetchSportApiAiLiveMatches(
        isoDate: String,
        dayTab: MatchDayTab
    ): List<FootballMatch> {
        if (!isSportApiKeyConfigured()) return emptyList()
        if (sportApiDailyCounter.get() >= SPORTAPI_MAX_REQ_PER_DAY) return emptyList()

        val liveEndpoints = listOf(
            "$SPORTAPI_BASE_URL/v1/football/matches/live",
            "$SPORTAPI_BASE_URL/v1/football/livescores",
            "$SPORTAPI_BASE_URL/v1/football/matches?date=$isoDate&live=true",
            "$SPORTAPI_BASE_URL/football/matches/live"
        )

        for (endpoint in liveEndpoints) {
            try {
                val url = endpoint.toHttpUrlOrNull() ?: continue
                val request = Request.Builder()
                    .url(url)
                    .header("X-Api-Key", BuildConfig.SPORTAPI_AI_KEY)
                    .header("Accept", "application/json")
                    .header("Cache-Control", "no-cache")
                    .build()

                sportApiDailyCounter.incrementAndGet()
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use
                    val body = response.body?.string() ?: return@use
                    val parsed = parseSportApiAiMatchesJson(body, dayTab)
                    if (parsed.isNotEmpty()) {
                        return parsed
                    }
                }
            } catch (_: Exception) {
                // Proceed to next candidate path
            }
        }
        return emptyList()
    }

    /**
     * API #1: SportAPI.ai (`https://sportapi.ai/api`)
     * - Primary for Live Scores, Real-Time Match Status (`1H`, `HT`, `2H`, `FT`) & Fixtures.
     * - Headers: `X-Api-Key: BuildConfig.SPORTAPI_AI_KEY` and `Accept: application/json`.
     * - Rate Limit Management: 2,000 requests/day. Uses a 25-second cache during active live polling and 10-minute cache for non-live tabs.
     */
    private fun fetchSportApiAiDayMatches(
        isoDate: String,
        dayTab: MatchDayTab,
        forceLiveRefresh: Boolean
    ): List<FootballMatch> {
        if (!isSportApiKeyConfigured()) return emptyList()
        if (sportApiDailyCounter.get() >= SPORTAPI_MAX_REQ_PER_DAY) {
            return sportApiDayCache[isoDate]?.second ?: emptyList()
        }

        val now = System.currentTimeMillis()
        val cached = sportApiDayCache[isoDate]
        val cacheTtlMs = when {
            forceLiveRefresh -> 25_000L
            dayTab == MatchDayTab.TODAY -> 45_000L
            else -> 10 * 60_000L
        }
        if (cached != null && (now - cached.first) < cacheTtlMs && cached.second.isNotEmpty()) {
            return cached.second
        }

        val candidatePaths = listOf(
            "$SPORTAPI_BASE_URL/v1/football/matches?date=$isoDate",
            "$SPORTAPI_BASE_URL/football/matches?date=$isoDate",
            "$SPORTAPI_BASE_URL/matches?date=$isoDate&sport=football"
        )

        for (endpoint in candidatePaths) {
            try {
                val url = endpoint.toHttpUrlOrNull() ?: continue
                val requestBuilder = Request.Builder()
                    .url(url)
                    .header("X-Api-Key", BuildConfig.SPORTAPI_AI_KEY)
                    .header("Accept", "application/json")
                if (forceLiveRefresh) {
                    requestBuilder.header("Cache-Control", "no-cache")
                }

                sportApiDailyCounter.incrementAndGet()
                httpClient.newCall(requestBuilder.build()).execute().use { response ->
                    if (!response.isSuccessful) return@use
                    val body = response.body?.string() ?: return@use
                    val parsed = parseSportApiAiMatchesJson(body, dayTab)
                    if (parsed.isNotEmpty()) {
                        sportApiDayCache[isoDate] = now to parsed
                        return parsed
                    }
                }
            } catch (_: Exception) {
                // Continue to fallback path or allow TheSportsDB to populate seamlessly
            }
        }

        return cached?.second ?: emptyList()
    }

    internal fun parseSportApiAiMatchesJson(
        rawJson: String,
        dayTab: MatchDayTab
    ): List<FootballMatch> {
        val trimmed = rawJson.trim()
        if (trimmed.isBlank()) return emptyList()

        val rootObj = if (trimmed.startsWith("{")) JSONObject(trimmed) else null
        val matchesArray: JSONArray = when {
            trimmed.startsWith("[") -> JSONArray(trimmed)
            rootObj != null -> {
                rootObj.optJSONArray("data")
                    ?: rootObj.optJSONArray("matches")
                    ?: rootObj.optJSONArray("fixtures")
                    ?: rootObj.optJSONArray("events")
                    ?: rootObj.optJSONArray("response")
                    ?: return emptyList()
            }
            else -> return emptyList()
        }

        val results = mutableListOf<FootballMatch>()
        for (i in 0 until matchesArray.length()) {
            val item = matchesArray.optJSONObject(i) ?: continue

            val leagueObj = item.optJSONObject("league")
                ?: item.optJSONObject("tournament")
                ?: item.optJSONObject("competition")
            val leagueName = leagueObj?.optString("name", "")
                ?.ifBlank { item.optString("league_name", item.optString("tournament_name", "")) }
                ?: item.optString("league_name", "")
            val countryName = leagueObj?.optString("country", "")
                ?.ifBlank { item.optString("country", "") }
                ?: ""

            val matchedTournament = matchTournamentCatalog("$countryName $leagueName") ?: continue

            val homeObj = item.optJSONObject("home_team")
                ?: item.optJSONObject("homeTeam")
                ?: item.optJSONObject("teams")?.optJSONObject("home")
            val awayObj = item.optJSONObject("away_team")
                ?: item.optJSONObject("awayTeam")
                ?: item.optJSONObject("teams")?.optJSONObject("away")

            val homeName = (homeObj?.optString("name", "") ?: item.optString("home_name", "")).trim()
            val awayName = (awayObj?.optString("name", "") ?: item.optString("away_name", "")).trim()
            if (homeName.isBlank() || awayName.isBlank()) continue

            val homeBadge = homeObj?.optString("logo", homeObj.optString("badge", "")) ?: ""
            val awayBadge = awayObj?.optString("logo", awayObj.optString("badge", "")) ?: ""

            val statusObj = item.optJSONObject("status")
            val rawStatusCode = (
                statusObj?.optString("short", statusObj.optString("code", statusObj.optString("type", "")))
                    ?: item.optString("status_short", item.optString("status", "NS"))
                ).trim()

            val minuteVal = statusObj?.optInt("elapsed", statusObj.optInt("minute", 0))
                ?.takeIf { it > 0 }
                ?: item.optInt("minute", item.optInt("elapsed", 0))

            val extraMinuteVal = statusObj?.optInt("extra", statusObj.optInt("extra_minute", statusObj.optInt("added_time", 0)))
                ?.takeIf { it > 0 }
                ?: item.optInt("extra_minute", item.optInt("added_time", 0))

            val startIso = item.optString("start_time", item.optString("date", item.optString("kickoff", "")))
            val kickoffTime = formatIsoToMoroccoTime(startIso)

            val (matchStatus, liveBadge) = parseLiveStatusAndMinuteBadge(
                rawStatus = rawStatusCode,
                minute = minuteVal,
                extraMinute = extraMinuteVal,
                kickoffTime = kickoffTime
            )

            val scoresObj = item.optJSONObject("scores")
                ?: item.optJSONObject("score")
                ?: item.optJSONObject("goals")
            val homeScore = if (matchStatus == MatchStatus.UPCOMING) {
                null
            } else {
                scoresObj?.optInt("home", -1)?.takeIf { it >= 0 }
                    ?: homeObj?.optInt("score", -1)?.takeIf { it >= 0 }
                    ?: item.optString("home_score", "").toIntOrNull()
            }
            val awayScore = if (matchStatus == MatchStatus.UPCOMING) {
                null
            } else {
                scoresObj?.optInt("away", -1)?.takeIf { it >= 0 }
                    ?: awayObj?.optInt("score", -1)?.takeIf { it >= 0 }
                    ?: item.optString("away_score", "").toIntOrNull()
            }

            // Extract real Half-Time score if present in SportAPI.ai payload
            val htObj = scoresObj?.optJSONObject("halftime") ?: scoresObj?.optJSONObject("ht")
            val htScoreStr = when {
                htObj != null -> {
                    val hHt = htObj.optInt("home", -1)
                    val aHt = htObj.optInt("away", -1)
                    if (hHt >= 0 && aHt >= 0) "$hHt - $aHt" else null
                }
                scoresObj?.optString("ht", "")?.contains("-") == true ->
                    scoresObj.optString("ht").replace("-", " - ").replace(Regex("\\s+"), " ").trim()
                item.optString("ht_score", "").contains("-") ->
                    item.optString("ht_score").replace("-", " - ").replace(Regex("\\s+"), " ").trim()
                else -> null
            }

            val homeRedCards = homeObj?.optInt("red_cards", 0)
                ?: item.optJSONObject("cards")?.optJSONObject("home")?.optInt("red", 0)
                ?: 0
            val awayRedCards = awayObj?.optInt("red_cards", 0)
                ?: item.optJSONObject("cards")?.optJSONObject("away")?.optInt("red", 0)
                ?: 0

            val goalScorersList = mutableListOf<String>()
            val eventsArr = item.optJSONArray("events") ?: item.optJSONArray("goals_list")
            if (eventsArr != null) {
                for (eIdx in 0 until eventsArr.length()) {
                    val ev = eventsArr.optJSONObject(eIdx) ?: continue
                    val type = ev.optString("type", "").lowercase(Locale.ROOT)
                    if (type.contains("goal")) {
                        val player = ev.optString("player", ev.optJSONObject("player")?.optString("name", "").orEmpty()).trim()
                        val min = ev.optString("minute", ev.optString("time", "")).trim()
                        if (player.isNotBlank()) {
                            goalScorersList.add(if (min.isNotBlank()) "$player ${min.trimEnd('\'')}'" else player)
                        }
                    }
                }
            }

            val isMarquee = isMarqueeMenaMatch(homeName, awayName)
            val smartFallbacks = getSmartMenaFallbackChannels(
                tournamentId = matchedTournament.tournamentId,
                tournamentName = matchedTournament.name,
                slotIndex = i,
                isMarquee = isMarquee,
                homeTeamName = homeName,
                awayTeamName = awayName
            )
            val rawTvChannelList = buildList {
                add(item.optString("tv_channel", ""))
                add(item.optString("broadcast", ""))
                add(item.optString("tv_channels", ""))
                val channelsArr = item.optJSONArray("channels") ?: item.optJSONArray("broadcasters") ?: item.optJSONArray("tv_stations")
                if (channelsArr != null) {
                    for (cIdx in 0 until channelsArr.length()) {
                        val cObj = channelsArr.optJSONObject(cIdx)
                        if (cObj != null) {
                            add(cObj.optString("name", cObj.optString("channel", cObj.optString("tv_name", ""))))
                        } else {
                            add(channelsArr.optString(cIdx, ""))
                        }
                    }
                }
            }
            val upstreamMenaChannels = rawTvChannelList
                .flatMap { it.split(",", ";", "/", "|") }
                .map { it.trim() }
                .filter { it.isNotBlank() && isMenaBroadcasterChannel(it) && !isGenericPlaceholderMenaChannel(it) }
                .map {
                    formatCanonicalMenaChannelName(
                        rawChannel = it,
                        defaultChannelNum = (i % 9) + 1,
                        defaultBeinTier = "HD"
                    )
                }

            val combinedMenaChannels = deduplicateMenaChannelOptions(upstreamMenaChannels + smartFallbacks)
            val primaryChannel = combinedMenaChannels.firstOrNull() ?: matchedTournament.defaultBeinPool.first()

            results.add(
                FootballMatch(
                    matchId = "sportapi_${item.optString("id", "$i")}",
                    tournamentId = matchedTournament.tournamentId,
                    tournamentName = matchedTournament.name,
                    tournamentRegion = matchedTournament.countryOrRegion,
                    dayTab = dayTab,
                    kickoffTimeFormatted = kickoffTime,
                    kickoffDateFormatted = "${dayTab.getDynamicTitle()} • GMT+1",
                    status = matchStatus,
                    liveMinuteOrBadge = liveBadge,
                    homeTeamId = "sportapi_${homeObj?.optString("id", homeName) ?: homeName}",
                    homeTeamName = homeName,
                    homeTeamBadge = homeBadge,
                    homeScore = homeScore,
                    awayTeamId = "sportapi_${awayObj?.optString("id", awayName) ?: awayName}",
                    awayTeamName = awayName,
                    awayTeamBadge = awayBadge,
                    awayScore = awayScore,
                    stadiumName = item.optJSONObject("venue")?.optString("name", "Main Stadium")
                        ?: item.optString("venue", "Main Stadium").ifBlank { "Main Stadium" },
                    cityName = item.optJSONObject("venue")?.optString("city", matchedTournament.countryOrRegion)
                        ?: matchedTournament.countryOrRegion,
                    beinChannel = primaryChannel,
                    beinChannelNumberHint = extractBeinHint(primaryChannel),
                    menaProviderName = detectMenaProviderFromLabel(primaryChannel),
                    fallbackMenaChannels = combinedMenaChannels.drop(1),
                    sourceTvChannels = upstreamMenaChannels,
                    stageOrRound = "${matchedTournament.name} • SportAPI.ai Live",
                    halfTimeScore = htScoreStr,
                    fullTimeScore = if (matchStatus == MatchStatus.FINISHED && homeScore != null && awayScore != null) {
                        "$homeScore - $awayScore"
                    } else null,
                    homeRedCards = homeRedCards,
                    awayRedCards = awayRedCards,
                    goalScorersSummary = goalScorersList.take(4).joinToString(", ").ifBlank { null }
                )
            )
        }
        return results
    }

    /**
     * Parses SportAPI.ai / TheSportsDB status codes into `MatchStatus` and formatted badge (`1H`, `HT`, `2H`, `FT`, or minute clock).
     */
    internal fun parseLiveStatusAndMinuteBadge(
        rawStatus: String,
        minute: Int,
        kickoffTime: String,
        extraMinute: Int = 0
    ): Pair<MatchStatus, String> {
        val upper = rawStatus.uppercase(Locale.ROOT).trim()
        val minuteStr = when {
            minute > 0 && extraMinute > 0 -> "${minute}+${extraMinute}'"
            minute > 0 -> "${minute}'"
            else -> ""
        }
        return when {
            upper == "HT" || upper.contains("HALF TIME") || upper == "HALFTIME" ->
                MatchStatus.HALFTIME to "HT"

            upper == "1H" || upper.contains("1ST") || upper == "FIRST_HALF" ->
                MatchStatus.LIVE to (if (minuteStr.isNotBlank()) "1H • $minuteStr" else "1H")

            upper == "2H" || upper.contains("2ND") || upper == "SECOND_HALF" ->
                MatchStatus.LIVE to (if (minuteStr.isNotBlank()) "2H • $minuteStr" else "2H")

            upper == "LIVE" || upper == "INPLAY" || upper == "IN_PLAY" || upper == "ET" || upper == "PEN" -> {
                val periodPrefix = when {
                    upper == "ET" -> "ET • "
                    upper == "PEN" -> "PEN • "
                    minute in 1..45 -> "1H • "
                    minute > 45 -> "2H • "
                    else -> ""
                }
                MatchStatus.LIVE to (if (minuteStr.isNotBlank()) "$periodPrefix$minuteStr" else upper)
            }

            upper == "FT" || upper == "AET" || upper == "AP" || upper.contains("FINISH") || upper.contains("ENDED") ->
                MatchStatus.FINISHED to "FT"

            else -> MatchStatus.UPCOMING to kickoffTime
        }
    }

    /**
     * API #2: TheSportsDB Free v1 API (`https://www.thesportsdb.com/api/v1/json/123`)
     * Endpoint 1: Daily Soccer Events (`GET /eventsday.php?d={YYYY-MM-DD}&s=Soccer`)
     */
    private fun fetchTheSportsDbDayMatches(
        isoDate: String,
        dayTab: MatchDayTab,
        forceLiveRefresh: Boolean = false
    ): List<FootballMatch> {
        val now = System.currentTimeMillis()
        val cached = sportsDbEventsCache[isoDate]
        val ttl = if (forceLiveRefresh) 55_000L else 5 * 60_000L
        if (cached != null && (now - cached.first) < ttl && cached.second.isNotEmpty()) {
            return cached.second
        }
        if (!canAcquireSportsDbPermit()) {
            return cached?.second ?: emptyList()
        }

        return try {
            val url = "$THESPORTSDB_BASE_URL/eventsday.php".toHttpUrlOrNull()
                ?.newBuilder()
                ?.addQueryParameter("d", isoDate)
                ?.addQueryParameter("s", "Soccer")
                ?.build() ?: return emptyList()

            val request = Request.Builder().url(url).build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return cached?.second ?: emptyList()
                val body = response.body?.string() ?: return cached?.second ?: emptyList()
                val parsed = parseTheSportsDbEventsJson(body, dayTab)
                if (parsed.isNotEmpty()) {
                    sportsDbEventsCache[isoDate] = now to parsed
                }
                parsed
            }
        } catch (_: Exception) {
            cached?.second ?: emptyList()
        }
    }

    /**
     * API #2: TheSportsDB Free v1 API (`https://www.thesportsdb.com/api/v1/json/123`)
     * Endpoint 2: Specific League Events (e.g., Moroccan Botola Pro `4520`)
     * `GET /eventsday.php?d={YYYY-MM-DD}&l={idLeague}`
     */
    private fun fetchTheSportsDbLeagueEvents(
        isoDate: String,
        idLeague: String,
        dayTab: MatchDayTab
    ): List<FootballMatch> {
        val cacheKey = "${isoDate}_L_${idLeague}"
        val now = System.currentTimeMillis()
        val cached = sportsDbEventsCache[cacheKey]
        if (cached != null && (now - cached.first) < 5 * 60_000L) {
            return cached.second
        }
        if (!canAcquireSportsDbPermit()) {
            return cached?.second ?: emptyList()
        }

        return try {
            val url = "$THESPORTSDB_BASE_URL/eventsday.php".toHttpUrlOrNull()
                ?.newBuilder()
                ?.addQueryParameter("d", isoDate)
                ?.addQueryParameter("l", idLeague)
                ?.build() ?: return emptyList()

            val request = Request.Builder().url(url).build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return cached?.second ?: emptyList()
                val body = response.body?.string() ?: return cached?.second ?: emptyList()
                val parsed = parseTheSportsDbEventsJson(body, dayTab, forcedLeagueId = idLeague)
                sportsDbEventsCache[cacheKey] = now to parsed
                parsed
            }
        } catch (_: Exception) {
            cached?.second ?: emptyList()
        }
    }

    private fun parseTheSportsDbEventsJson(
        body: String,
        dayTab: MatchDayTab,
        forcedLeagueId: String? = null
    ): List<FootballMatch> {
        val root = JSONObject(body)
        val events = root.optJSONArray("events") ?: return emptyList()
        val list = mutableListOf<FootballMatch>()

        for (i in 0 until events.length()) {
            val ev = events.optJSONObject(i) ?: continue
            val idEvent = ev.optString("idEvent", "")
            val idLeague = ev.optString("idLeague", forcedLeagueId ?: "")
            val leagueName = ev.optString("strLeague", "").trim()

            val matchedTournament = matchTournamentBySportsDbIdOrName(idLeague, leagueName) ?: continue

            val homeName = ev.optString("strHomeTeam", "").trim()
            val awayName = ev.optString("strAwayTeam", "").trim()
            if (homeName.isBlank() || awayName.isBlank()) continue

            val timestampRaw = ev.optString("strTimestamp", "").trim()
            val dateEventRaw = ev.optString("dateEvent", "").trim()
            val strTimeRaw = ev.optString("strTime", "20:00:00").trim()
            val timeRaw = when {
                timestampRaw.isNotBlank() && timestampRaw != "null" -> formatIsoToMoroccoTime(timestampRaw)
                dateEventRaw.isNotBlank() && strTimeRaw.isNotBlank() && strTimeRaw != "null" ->
                    formatIsoToMoroccoTime("${dateEventRaw}T${strTimeRaw.take(8)}Z")
                else -> strTimeRaw.take(5).ifBlank { "20:00" }
            }
            val venue = ev.optString("strVenue", "").takeIf { it.isNotBlank() && it != "null" } ?: "Main Stadium"
            val city = ev.optString("strCity", matchedTournament.countryOrRegion)
                .takeIf { it.isNotBlank() && it != "null" } ?: matchedTournament.countryOrRegion

            // Append `/tiny` to strHomeTeamBadge and strAwayTeamBadge for fast mobile rendering
            val homeBadge = toTinySportsDbBadgeUrl(ev.optString("strHomeTeamBadge", ev.optString("strBadge", "")))
            val awayBadge = toTinySportsDbBadgeUrl(ev.optString("strAwayTeamBadge", ""))

            val rawHomeScore = ev.optString("intHomeScore", "").takeIf { it != "null" }?.toIntOrNull()
            val rawAwayScore = ev.optString("intAwayScore", "").takeIf { it != "null" }?.toIntOrNull()
            val statusRaw = ev.optString("strStatus", "").takeIf { it != "null" }.orEmpty().trim()
            val roundNum = ev.optString("intRound", "").takeIf { it != "null" }.orEmpty().trim()

            val isNotStarted = statusRaw.equals("NS", ignoreCase = true) ||
                statusRaw.contains("Not Started", ignoreCase = true) ||
                statusRaw.contains("Postponed", ignoreCase = true) ||
                statusRaw.contains("Cancelled", ignoreCase = true)

            val (status, badgeText) = when {
                statusRaw.contains("FT", true) || statusRaw.contains("Finished", true) || statusRaw.contains("AET", true) || statusRaw.contains("Pen", true) ->
                    MatchStatus.FINISHED to "FT"
                statusRaw.contains("HT", true) || statusRaw.contains("Halftime", true) ->
                    MatchStatus.HALFTIME to "HT"
                statusRaw.contains("1H", true) ->
                    MatchStatus.LIVE to "1H"
                statusRaw.contains("2H", true) ->
                    MatchStatus.LIVE to "2H"
                statusRaw.contains("Live", true) || statusRaw.contains("In Play", true) ->
                    MatchStatus.LIVE to "LIVE"
                !isNotStarted && rawHomeScore != null && rawAwayScore != null && (dayTab == MatchDayTab.YESTERDAY || dayTab == MatchDayTab.TODAY) ->
                    MatchStatus.FINISHED to "FT"
                else ->
                    MatchStatus.UPCOMING to timeRaw
            }

            val homeScore = if (status == MatchStatus.UPCOMING) null else rawHomeScore
            val awayScore = if (status == MatchStatus.UPCOMING) null else rawAwayScore

            val isMarquee = isMarqueeMenaMatch(homeName, awayName)
            val smartFallbacks = getSmartMenaFallbackChannels(
                tournamentId = matchedTournament.tournamentId,
                tournamentName = matchedTournament.name,
                slotIndex = i,
                isMarquee = isMarquee,
                homeTeamName = homeName,
                awayTeamName = awayName
            )
            val strTvStation = ev.optString("strTVStation", "").takeIf { it != "null" }.orEmpty()
            val eventLevelMenaChannels = strTvStation
                .split(",", ";", "/")
                .map { it.trim() }
                .filter { it.isNotBlank() && isMenaBroadcasterChannel(it) }
                .map {
                    formatCanonicalMenaChannelName(
                        rawChannel = it,
                        defaultChannelNum = (i % 9) + 1,
                        defaultBeinTier = if (isMarquee && i == 0) "4K" else "Max"
                    )
                }

            val combinedMenaChannels = deduplicateMenaChannelOptions(eventLevelMenaChannels + smartFallbacks)
            val primaryChannel = combinedMenaChannels.firstOrNull()
                ?: matchedTournament.defaultBeinPool[i % matchedTournament.defaultBeinPool.size]

            list.add(
                FootballMatch(
                    matchId = "tsdb_${idEvent.ifBlank { "$i" }}",
                    tournamentId = matchedTournament.tournamentId,
                    tournamentName = matchedTournament.name,
                    tournamentRegion = matchedTournament.countryOrRegion,
                    dayTab = dayTab,
                    kickoffTimeFormatted = timeRaw,
                    kickoffDateFormatted = "${dayTab.getDynamicTitle()} • GMT+1",
                    status = status,
                    liveMinuteOrBadge = badgeText,
                    homeTeamId = "tsdb_${ev.optString("idHomeTeam", homeName)}",
                    homeTeamName = homeName,
                    homeTeamBadge = homeBadge,
                    homeScore = homeScore,
                    awayTeamId = "tsdb_${ev.optString("idAwayTeam", awayName)}",
                    awayTeamName = awayName,
                    awayTeamBadge = awayBadge,
                    awayScore = awayScore,
                    stadiumName = venue,
                    cityName = city,
                    beinChannel = primaryChannel,
                    beinChannelNumberHint = extractBeinHint(primaryChannel),
                    menaProviderName = detectMenaProviderFromLabel(primaryChannel),
                    fallbackMenaChannels = combinedMenaChannels.drop(1),
                    sourceTvChannels = eventLevelMenaChannels,
                    stageOrRound = if (roundNum.isNotBlank()) "Matchday $roundNum" else matchedTournament.name,
                    sportsDbEventId = idEvent,
                    fullTimeScore = if (status == MatchStatus.FINISHED && homeScore != null && awayScore != null) "$homeScore - $awayScore" else null
                )
            )
        }
        return list
    }

    /**
     * API #2: TheSportsDB Free v1 API (`https://www.thesportsdb.com/api/v1/json/123`)
     * Endpoint 3: Daily TV Broadcast Channels (`GET /eventstv.php?d={YYYY-MM-DD}&s=Soccer`)
     */
    private fun fetchTheSportsDbTvListings(isoDate: String): List<SportsDbTvBroadcastEntry> {
        val now = System.currentTimeMillis()
        val cached = sportsDbTvCache[isoDate]
        if (cached != null && (now - cached.first) < 10 * 60_000L) {
            return cached.second
        }
        if (!canAcquireSportsDbPermit()) {
            return cached?.second ?: emptyList()
        }

        return try {
            val url = "$THESPORTSDB_BASE_URL/eventstv.php".toHttpUrlOrNull()
                ?.newBuilder()
                ?.addQueryParameter("d", isoDate)
                ?.addQueryParameter("s", "Soccer")
                ?.build() ?: return emptyList()

            val request = Request.Builder().url(url).build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return cached?.second ?: emptyList()
                val body = response.body?.string() ?: return cached?.second ?: emptyList()
                val root = JSONObject(body)
                val tvevents = root.optJSONArray("tvevents")
                    ?: root.optJSONArray("events")
                    ?: return emptyList()
                val entries = mutableListOf<SportsDbTvBroadcastEntry>()

                for (i in 0 until tvevents.length()) {
                    val tvObj = tvevents.optJSONObject(i) ?: continue
                    val idEvent = tvObj.optString("idEvent", "").trim()
                    val strChannel = tvObj.optString("strChannel", "").trim()
                    if (strChannel.isBlank()) continue
                    val strEvent = tvObj.optString("strEvent", "")
                    val strCountry = tvObj.optString("strCountry", "")
                    entries.add(
                        SportsDbTvBroadcastEntry(
                            eventId = idEvent,
                            eventTitleNormalized = normalizeTeamNameForMerge(strEvent),
                            channelName = strChannel,
                            country = strCountry,
                            eventTitleRaw = strEvent
                        )
                    )
                }
                sportsDbTvCache[isoDate] = now to entries
                entries
            }
        } catch (_: Exception) {
            cached?.second ?: emptyList()
        }
    }

    private fun fetchEspnLeagueScoreboard(
        tournament: TournamentCatalogItem,
        espnDate: String,
        expectedMoroccoIsoDate: String,
        dayTab: MatchDayTab
    ): List<FootballMatch> {
        return try {
            val url = "https://site.api.espn.com/apis/site/v2/sports/soccer/${tournament.espnSlug}/scoreboard"
                .toHttpUrlOrNull()
                ?.newBuilder()
                ?.addQueryParameter("dates", espnDate)
                ?.build() ?: return emptyList()

            val request = Request.Builder().url(url).build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()
                val root = JSONObject(body)
                val events = root.optJSONArray("events") ?: return emptyList()
                val list = mutableListOf<FootballMatch>()

                for (i in 0 until events.length()) {
                    val ev = events.optJSONObject(i) ?: continue
                    val eventId = ev.optString("id", "${tournament.tournamentId}_$i")
                    val dateIso = ev.optString("date", "")
                    val eventMoroccoDate = formatIsoToMoroccoDate(dateIso)
                    if (eventMoroccoDate.isNotBlank() && eventMoroccoDate != expectedMoroccoIsoDate) {
                        continue
                    }

                    val competitions = ev.optJSONArray("competitions") ?: continue
                    val comp = competitions.optJSONObject(0) ?: continue

                    val venueObj = comp.optJSONObject("venue")
                    val stadiumName = venueObj?.optString("fullName", "")?.ifBlank { "Main Stadium" } ?: "Main Stadium"
                    val cityName = venueObj?.optJSONObject("address")?.optString("city", "") ?: tournament.countryOrRegion

                    val statusObj = comp.optJSONObject("status") ?: ev.optJSONObject("status")
                    val statusType = statusObj?.optJSONObject("type")
                    val stateStr = statusType?.optString("state", "pre") ?: "pre"
                    val shortDetail = statusType?.optString("shortDetail", "") ?: ""
                    val displayClock = statusObj?.optString("displayClock", "") ?: ""
                    val periodNum = statusObj?.optInt("period", 0) ?: 0

                    val competitors = comp.optJSONArray("competitors") ?: continue
                    if (competitors.length() < 2) continue

                    var homeComp = competitors.optJSONObject(0)
                    var awayComp = competitors.optJSONObject(1)
                    for (cIdx in 0 until competitors.length()) {
                        val c = competitors.optJSONObject(cIdx) ?: continue
                        if (c.optString("homeAway") == "home") homeComp = c
                        if (c.optString("homeAway") == "away") awayComp = c
                    }

                    val homeTeamObj = homeComp?.optJSONObject("team") ?: continue
                    val awayTeamObj = awayComp?.optJSONObject("team") ?: continue

                    val homeTeamIdRaw = homeTeamObj.optString("id", "")
                    val awayTeamIdRaw = awayTeamObj.optString("id", "")
                    val homeName = homeTeamObj.optString("displayName", homeTeamObj.optString("name", "")).trim()
                    val awayName = awayTeamObj.optString("displayName", awayTeamObj.optString("name", "")).trim()
                    if (homeName.isBlank() || awayName.isBlank()) continue

                    val homeLogo = homeTeamObj.optString("logo", "")
                    val awayLogo = awayTeamObj.optString("logo", "")

                    val kickoffTime = formatIsoToMoroccoTime(dateIso)

                    val (matchStatus, badgeText) = when (stateStr.lowercase(Locale.ROOT)) {
                        "in" -> {
                            when {
                                shortDetail.contains("HT", ignoreCase = true) || shortDetail.contains("Half", ignoreCase = true) ->
                                    MatchStatus.HALFTIME to "HT"
                                shortDetail.contains("1st", ignoreCase = true) || periodNum == 1 ->
                                    MatchStatus.LIVE to (if (displayClock.isNotBlank()) "1H • $displayClock" else "1H")
                                shortDetail.contains("2nd", ignoreCase = true) || periodNum >= 2 ->
                                    MatchStatus.LIVE to (if (displayClock.isNotBlank()) "2H • $displayClock" else "2H")
                                else -> MatchStatus.LIVE to (displayClock.ifBlank { "LIVE" })
                            }
                        }
                        "post" -> MatchStatus.FINISHED to "FT"
                        else -> MatchStatus.UPCOMING to kickoffTime
                    }

                    val homeScore = if (matchStatus == MatchStatus.UPCOMING) null else homeComp?.optString("score")?.toIntOrNull()
                    val awayScore = if (matchStatus == MatchStatus.UPCOMING) null else awayComp?.optString("score")?.toIntOrNull()

                    // Parse real 1st-half linescore when match has reached/passed Half-Time
                    val isHalftimeOrLater = matchStatus == MatchStatus.HALFTIME ||
                        matchStatus == MatchStatus.FINISHED ||
                        (matchStatus == MatchStatus.LIVE && (periodNum >= 2 || shortDetail.contains("2nd", ignoreCase = true)))

                    val homeLinescores = homeComp?.optJSONArray("linescores")
                    val awayLinescores = awayComp?.optJSONArray("linescores")
                    var htScoreStr: String? = null
                    if (isHalftimeOrLater && homeLinescores != null && awayLinescores != null &&
                        homeLinescores.length() >= 1 && awayLinescores.length() >= 1
                    ) {
                        val h1 = homeLinescores.optJSONObject(0)?.optInt("value", -1) ?: -1
                        val a1 = awayLinescores.optJSONObject(0)?.optInt("value", -1) ?: -1
                        if (h1 >= 0 && a1 >= 0) {
                            htScoreStr = "$h1 - $a1"
                        }
                    }

                    // Parse real goal scorers, red cards, and 1st-half goals from `comp.details` if available
                    val detailsArr = comp.optJSONArray("details")
                    var homeRedCount = 0
                    var awayRedCount = 0
                    var detailsHtHome = 0
                    var detailsHtAway = 0
                    val scorerParts = mutableListOf<String>()

                    if (detailsArr != null && detailsArr.length() > 0) {
                        for (dIdx in 0 until detailsArr.length()) {
                            val dObj = detailsArr.optJSONObject(dIdx) ?: continue
                            val teamId = dObj.optJSONObject("team")?.optString("id", "") ?: ""
                            val isHomeEvent = teamId.isNotBlank() && teamId == homeTeamIdRaw
                            val isScoring = dObj.optBoolean("scoringPlay", false)
                            val isRed = dObj.optBoolean("redCard", false)
                            val clockDisplay = dObj.optJSONObject("clock")?.optString("displayValue", "")?.trim().orEmpty()
                            val clockSeconds = dObj.optJSONObject("clock")?.optInt("value", 0) ?: 0
                            val athletes = dObj.optJSONArray("athletesInvolved")
                            val playerName = athletes?.optJSONObject(0)?.let {
                                it.optString("shortName", it.optString("displayName", "")).trim()
                            }.orEmpty()

                            if (isScoring) {
                                if (clockSeconds in 1..2700 || clockDisplay.startsWith("45+")) {
                                    if (isHomeEvent) detailsHtHome++ else detailsHtAway++
                                }
                                if (playerName.isNotBlank()) {
                                    scorerParts.add(if (clockDisplay.isNotBlank()) "$playerName $clockDisplay" else playerName)
                                }
                            }
                            if (isRed) {
                                if (isHomeEvent) homeRedCount++ else awayRedCount++
                            }
                        }
                        if (htScoreStr == null && isHalftimeOrLater && homeScore != null && awayScore != null) {
                            htScoreStr = "$detailsHtHome - $detailsHtAway"
                        }
                    }

                    // Extract any MENA/beIN broadcast channels present in ESPN's `broadcasts` or `geoBroadcasts` arrays
                    val espnBroadcastChannels = mutableListOf<String>()
                    val broadcastsArr = comp.optJSONArray("broadcasts")
                    if (broadcastsArr != null) {
                        for (bIdx in 0 until broadcastsArr.length()) {
                            val bObj = broadcastsArr.optJSONObject(bIdx) ?: continue
                            val namesArr = bObj.optJSONArray("names")
                            if (namesArr != null) {
                                for (nIdx in 0 until namesArr.length()) {
                                    val chName = namesArr.optString(nIdx, "").trim()
                                    if (chName.isNotBlank() && isMenaBroadcasterChannel(chName) && !isGenericPlaceholderMenaChannel(chName)) {
                                        espnBroadcastChannels.add(chName)
                                    }
                                }
                            }
                        }
                    }
                    val geoBroadcastsArr = comp.optJSONArray("geoBroadcasts")
                    if (geoBroadcastsArr != null) {
                        for (gIdx in 0 until geoBroadcastsArr.length()) {
                            val gObj = geoBroadcastsArr.optJSONObject(gIdx) ?: continue
                            val mediaName = gObj.optJSONObject("media")?.optString("shortName", "")?.trim().orEmpty()
                            if (mediaName.isNotBlank() && isMenaBroadcasterChannel(mediaName) && !isGenericPlaceholderMenaChannel(mediaName)) {
                                espnBroadcastChannels.add(mediaName)
                            }
                        }
                    }

                    val isMarquee = isMarqueeMenaMatch(homeName, awayName)
                    val smartFallbacks = getSmartMenaFallbackChannels(
                        tournamentId = tournament.tournamentId,
                        tournamentName = tournament.name,
                        slotIndex = i,
                        isMarquee = isMarquee,
                        homeTeamName = homeName,
                        awayTeamName = awayName
                    )
                    val combinedChannels = deduplicateMenaChannelOptions(espnBroadcastChannels + smartFallbacks)
                    val primaryChannel = combinedChannels.firstOrNull() ?: tournament.defaultBeinPool[i % tournament.defaultBeinPool.size]
                    val channelHint = extractBeinHint(primaryChannel)

                    list.add(
                        FootballMatch(
                            matchId = "espn_$eventId",
                            tournamentId = tournament.tournamentId,
                            tournamentName = tournament.name,
                            tournamentRegion = tournament.countryOrRegion,
                            dayTab = dayTab,
                            kickoffTimeFormatted = kickoffTime,
                            kickoffDateFormatted = "${dayTab.getDynamicTitle()} • GMT+1",
                            status = matchStatus,
                            liveMinuteOrBadge = badgeText,
                            homeTeamId = "espn_${homeTeamIdRaw.ifBlank { homeName }}",
                            homeTeamName = homeName,
                            homeTeamBadge = homeLogo,
                            homeScore = homeScore,
                            awayTeamId = "espn_${awayTeamIdRaw.ifBlank { awayName }}",
                            awayTeamName = awayName,
                            awayTeamBadge = awayLogo,
                            awayScore = awayScore,
                            stadiumName = stadiumName,
                            cityName = cityName,
                            beinChannel = primaryChannel,
                            beinChannelNumberHint = channelHint,
                            menaProviderName = detectMenaProviderFromLabel(primaryChannel),
                            fallbackMenaChannels = combinedChannels.drop(1),
                            sourceTvChannels = espnBroadcastChannels.distinct(),
                            stageOrRound = tournament.name,
                            sportsDbEventId = "",
                            espnEventId = eventId,
                            halfTimeScore = htScoreStr,
                            fullTimeScore = if (matchStatus == MatchStatus.FINISHED && homeScore != null && awayScore != null) "$homeScore - $awayScore" else null,
                            homeRedCards = homeRedCount,
                            awayRedCards = awayRedCount,
                            goalScorersSummary = scorerParts.take(4).joinToString(", ").ifBlank { null }
                        )
                    )
                }
                list
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun fetchApiFootballDayMatches(
        isoDate: String,
        dayTab: MatchDayTab
    ): List<FootballMatch> {
        if (!isApiFootballKeyConfigured()) return emptyList()

        val now = System.currentTimeMillis()
        val cached = apiFootballDayCache[isoDate]
        if (cached != null && (now - cached.first) < 15 * 60 * 1000L && cached.second.isNotEmpty()) {
            return cached.second
        }

        return try {
            val url = "https://v3.football.api-sports.io/fixtures".toHttpUrlOrNull()
                ?.newBuilder()
                ?.addQueryParameter("date", isoDate)
                ?.addQueryParameter("timezone", "Africa/Casablanca")
                ?.build() ?: return emptyList()

            val request = Request.Builder()
                .url(url)
                .header("x-apisports-key", BuildConfig.API_FOOTBALL_KEY)
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()
                val root = JSONObject(body)
                val errorsObj = root.optJSONObject("errors")
                if (errorsObj != null && errorsObj.length() > 0) {
                    return emptyList()
                }
                val arr = root.optJSONArray("response") ?: return emptyList()
                val list = mutableListOf<FootballMatch>()

                for (i in 0 until arr.length()) {
                    val item = arr.optJSONObject(i) ?: continue
                    val leagueObj = item.optJSONObject("league") ?: continue
                    val leagueId = leagueObj.optInt("id", -1)
                    val leagueName = leagueObj.optString("name", "")
                    val country = leagueObj.optString("country", "")
                    val roundStr = leagueObj.optString("round", "Regular Season")
                    val matchedTournament = matchApiFootballLeague(leagueId, country, leagueName) ?: continue

                    val fixtureObj = item.optJSONObject("fixture") ?: continue
                    val venueObj = fixtureObj.optJSONObject("venue")
                    val stadiumName = venueObj?.optString("name", "")?.ifBlank { "Main Stadium" } ?: "Main Stadium"
                    val cityName = venueObj?.optString("city", "")?.ifBlank { matchedTournament.countryOrRegion }
                        ?: matchedTournament.countryOrRegion

                    val statusObj = fixtureObj.optJSONObject("status")
                    val shortStatus = statusObj?.optString("short", "NS") ?: "NS"
                    val elapsed = statusObj?.optInt("elapsed", 0) ?: 0

                    val teamsObj = item.optJSONObject("teams") ?: continue
                    val homeObj = teamsObj.optJSONObject("home") ?: continue
                    val awayObj = teamsObj.optJSONObject("away") ?: continue
                    val goalsObj = item.optJSONObject("goals")

                    val kickoffTime = formatIsoToMoroccoTime(fixtureObj.optString("date", ""))
                    val (status, badge) = parseLiveStatusAndMinuteBadge(shortStatus, elapsed, kickoffTime)

                    val homeName = homeObj.optString("name", "")
                    val awayName = awayObj.optString("name", "")
                    val smartFallbacks = getSmartMenaFallbackChannels(
                        tournamentId = matchedTournament.tournamentId,
                        tournamentName = matchedTournament.name,
                        slotIndex = list.size,
                        isMarquee = isMarqueeMenaMatch(homeName, awayName),
                        homeTeamName = homeName,
                        awayTeamName = awayName
                    )
                    val primaryChannel = smartFallbacks.firstOrNull()
                        ?: matchedTournament.defaultBeinPool[list.size % matchedTournament.defaultBeinPool.size]

                    list.add(
                        FootballMatch(
                            matchId = "apif_${fixtureObj.optInt("id", i)}",
                            tournamentId = matchedTournament.tournamentId,
                            tournamentName = matchedTournament.name,
                            tournamentRegion = matchedTournament.countryOrRegion,
                            dayTab = dayTab,
                            kickoffTimeFormatted = kickoffTime,
                            kickoffDateFormatted = "${dayTab.getDynamicTitle()} • GMT+1",
                            status = status,
                            liveMinuteOrBadge = badge,
                            homeTeamId = "apif_${homeObj.optInt("id", 0)}",
                            homeTeamName = homeName,
                            homeTeamBadge = homeObj.optString("logo", ""),
                            homeScore = if (status == MatchStatus.UPCOMING) null else goalsObj?.optInt("home"),
                            awayTeamId = "apif_${awayObj.optInt("id", 0)}",
                            awayTeamName = awayName,
                            awayTeamBadge = awayObj.optString("logo", ""),
                            awayScore = if (status == MatchStatus.UPCOMING) null else goalsObj?.optInt("away"),
                            stadiumName = stadiumName,
                            cityName = cityName,
                            beinChannel = primaryChannel,
                            beinChannelNumberHint = extractBeinHint(primaryChannel),
                            menaProviderName = detectMenaProviderFromLabel(primaryChannel),
                            fallbackMenaChannels = smartFallbacks.drop(1),
                            stageOrRound = roundStr
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    apiFootballDayCache[isoDate] = now to list
                }
                list
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun matchApiFootballLeague(
        leagueId: Int,
        country: String,
        leagueName: String
    ): TournamentCatalogItem? {
        return when (leagueId) {
            200, 201 -> ALL_TOURNAMENTS.first { it.tournamentId == "botola_pro" }
            12, 20, 6 -> ALL_TOURNAMENTS.first { it.tournamentId == "caf_cl" }
            2 -> ALL_TOURNAMENTS.first { it.tournamentId == "uefa_cl" }
            140, 143 -> ALL_TOURNAMENTS.first { it.tournamentId == "laliga" }
            39, 45, 48 -> ALL_TOURNAMENTS.first { it.tournamentId == "premier_league" }
            135, 137 -> ALL_TOURNAMENTS.first { it.tournamentId == "serie_a" }
            61, 66 -> ALL_TOURNAMENTS.first { it.tournamentId == "ligue_1" }
            78, 81 -> ALL_TOURNAMENTS.first { it.tournamentId == "bundesliga" }
            307 -> ALL_TOURNAMENTS.first { it.tournamentId == "saudi_pro" }
            3, 848 -> ALL_TOURNAMENTS.first { it.tournamentId == "uefa_el" }
            else -> {
                if (country.equals("Morocco", ignoreCase = true) && leagueName.contains("Botola", ignoreCase = true)) {
                    ALL_TOURNAMENTS.first { it.tournamentId == "botola_pro" }
                } else {
                    null
                }
            }
        }
    }


    /**
     * Checks whether two channel names represent the exact same broadcaster & channel
     * across naming variations (e.g., `'beIN Sports max 5'`, `'beIN Sports max 05'`,
     * `'beIN Sports 5 max'`, `'beIN Sports 05 max'`, or `'AR| BEIN SPORTS 02 HD'` vs `'beIN Sports 2 HD'`).
     */
    fun areEquivalentMenaChannels(channelA: String, channelB: String): Boolean {
        if (channelA.equals(channelB, ignoreCase = true)) return true
        val profileA = normalizeChannelNameForFavoritesMatch(channelA)
        val profileB = normalizeChannelNameForFavoritesMatch(channelB)
        if (profileA.canonicalProvider.isBlank() || profileB.canonicalProvider.isBlank()) return false
        return profileA.canonicalKey == profileB.canonicalKey
    }

    /**
     * Deduplicates a list of channel options by canonical channel equivalence while preserving
     * the first (source-authoritative) label for each unique channel.
     */
    fun deduplicateMenaChannelOptions(channels: List<String>): List<String> {
        val result = mutableListOf<String>()
        for (raw in channels) {
            val trimmed = raw.trim()
            if (trimmed.isBlank()) continue
            if (result.none { existing -> areEquivalentMenaChannels(existing, trimmed) }) {
                result.add(trimmed)
            }
        }
        return result
    }

    /**
     * Strict Favorites-Only MENA Broadcaster Channel Matcher.
     *
     * Searches EXCLUSIVELY within the user's saved **Favorites** channels list (`isFavorite = true`).
     * Does NOT search the full Xtream catalog.
     *
     * Features:
     * 1. Recognizes naming, word-order, prefix, codec, and zero-padded number variations
     *    (such as `'AR| BEIN SPORTS 02 HD'`, `'[01] VIP | beIN Sports 2 FHD'`, `'beIN Sports Premium 2 HD'`,
     *    `'beIN Sports max 5'`, `'beIN Sports max 05'`, `'beIN Sports 5 max'`, and `'beIN Sports 05 max'`)
     *    as the exact same channel.
     * 2. Computes `optionsAvailableInFavorites`: the subset of `availableMenaChannelOptions` that currently
     *    have at least one matching stream in the user's Favorites, so the UI can highlight available options with a `★` badge.
     */
    suspend fun findMenaChannelsWithFavoritesPriority(
        playlistId: Long,
        match: FootballMatch,
        overrideChannelLabel: String? = null
    ): MenaChannelMatchResult = withContext(Dispatchers.IO) {
        val targetChannel = (overrideChannelLabel ?: match.beinChannel).trim()
        val baseOptions = deduplicateMenaChannelOptions(
            match.sourceTvChannels + listOf(match.beinChannel) + match.fallbackMenaChannels
        )

        // Update the corresponding option in availableMenaChannelOptions if targetChannel is a recognized variation
        // (e.g., 'beIN Sports 05 max' vs 'beIN Sports max 5') so the active option chip stays in sync.
        val updatedOptions = if (targetChannel.isBlank()) {
            baseOptions
        } else {
            var replacedEquivalent = false
            val mapped = baseOptions.map { option ->
                if (areEquivalentMenaChannels(option, targetChannel)) {
                    replacedEquivalent = true
                    targetChannel
                } else {
                    option
                }
            }
            if (replacedEquivalent) mapped else (listOf(targetChannel) + baseOptions).distinct()
        }

        // Search ONLY within the user's saved Favorites channels list
        val allFavoriteStreams = streamDao.getFavoriteLiveStreamsOnce(playlistId)
        val favoriteMatches = matchFavoriteChannelsStrictly(allFavoriteStreams, targetChannel)

        val optionsInFavorites = if (allFavoriteStreams.isEmpty()) {
            emptySet()
        } else {
            updatedOptions.filter { optionLabel ->
                matchFavoriteChannelsStrictly(allFavoriteStreams, optionLabel).isNotEmpty()
            }.toSet()
        }

        val resolvedProvider = detectMenaProviderFromLabel(targetChannel)

        MenaChannelMatchResult(
            favoriteMatches = favoriteMatches,
            livePlaylistCandidates = emptyList(), // Never search full Xtream catalog
            resolvedChannelLabel = targetChannel,
            resolvedProviderName = resolvedProvider,
            usedMenaFallbackProvider = overrideChannelLabel != null &&
                !areEquivalentMenaChannels(targetChannel, match.beinChannel),
            availableMenaChannelOptions = updatedOptions,
            optionsAvailableInFavorites = optionsInFavorites
        )
    }

    /**
     * Normalizes both a Kickoff channel name and a Favorite channel's name before comparing:
     * 1. Converts to lowercase and strips diacritics.
     * 2. Isolates the substring starting at the MENA broadcaster keyword (`bein`, `arryadia`, `ssc`, `adsport`, etc.)
     *    so IPTV playlist numbers or category prefixes before the channel name (e.g. `"[01] AR | beIN Sports 02 HD"`,
     *    `"14 - VIP ┃ beIN Sports 1 FHD"`) never corrupt `channelNumbers`, while preserving any language prefix (`EN|`, `FR|`).
     * 3. Strips all IPTV codecs, resolutions, frame rates, audio tags, and server suffixes (`MP4A-LATM`, `H.264`, `HEVC`,
     *    `2160p`, `1080p`, `720p`, `50fps`, `Backup 1`, `Server 2`) BEFORE splitting letter-digit boundaries.
     * 4. Splits attached letter-digit tokens (e.g. `max05` -> `max 05`, `05max` -> `05 max`, `hd02` -> `hd 02`, `bein2` -> `bein 2`).
     * 5. Canonicalizes zero-padded channel numbers (`02` -> `2`, `05` -> `5`, `01` -> `1`) and extracts sub-brand
     *    (`max`, `extra`, `afc`, `premium`) and language (`en`, `fr`, `es`) tokens.
     */
    internal fun normalizeChannelNameForFavoritesMatch(rawName: String): NormalizedChannelProfile {
        val lowerFull = Normalizer.normalize(rawName, Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
            .lowercase(Locale.ROOT)

        // Detect if rawName has an explicit language tag in the prefix or body before stripping
        val hasExplicitEnglish = Regex("""\b(english|eng|en)\b""").containsMatchIn(lowerFull)
        val hasExplicitFrench = Regex("""\b(french|fra|fr)\b""").containsMatchIn(lowerFull)
        val hasExplicitSpanish = Regex("""\b(spanish|espanol|esp|es)\b""").containsMatchIn(lowerFull)
        val is4kFeed = Regex("""\b(4k|uhd|2160p|2160)\b""").containsMatchIn(lowerFull)

        // Locate the start of the MENA broadcaster name so leading playlist numbers ("[01] VIP | ") are discarded
        val providerStartMatch = BROADCASTER_START_REGEX.find(lowerFull)
        val relevantSubstring = if (providerStartMatch != null) {
            lowerFull.substring(providerStartMatch.range.first)
        } else {
            lowerFull
        }

        // 1. Strip codecs, audio formats, server/backup suffixes, and resolution tags BEFORE splitting letter-digit tokens
        val step1 = relevantSubstring
            .replace("mp4a-latm", " ")
            .replace(Regex("""\b(mp4a|mpeg4|mpeg2|hevc|avc|vp9|av1|aac|ac3|eac3|latm|dd\+?|dolby)\b"""), " ")
            .replace(Regex("""\b([hx]\.?26[45]|5\.1|2\.0)\b"""), " ")
            .replace(Regex("""\b(2160p|2160|1080p|1080i|1080|720p|720|576p|576|480p|480|50fps|60fps|25fps|30fps|fps)\b"""), " ")
            .replace(Regex("""\b(server|srv|backup|bkp|source|src|feed|stream|ch)\s*#?\s*\d+\b"""), " ")
            .replace(Regex("""\b(ar|ma|mar|dz|tn|eg|ksa|ae|qa|fr|en|uk|us)\s*[:|┃\-]"""), " ")
            .replace(Regex("""[|┃\[\]()/\\:_\-+.,]+"""), " ")
            .replace(
                Regex("""\b(hevc|4k|fhd|uhd|hd|sd|h265|h264|x265|x264|tnt|vip|raw|live|mena|arabia|connect|ar|ara|arabic|ma|mar|low|med|high)\b"""),
                " "
            )

        // 2. Separate attached letter-digit boundaries (e.g., "max05" -> "max 05", "05max" -> "05 max", "hd02" -> "hd 02", "bein2" -> "bein 2")
        val step2 = step1
            .replace(Regex("""([a-z])(\d)"""), "$1 $2")
            .replace(Regex("""(\d)([a-z])"""), "$1 $2")
            .replace(
                Regex("""\b(hevc|4k|fhd|uhd|hd|sd|tnt|vip|raw|live|mena|arabia|connect|english|eng|en|french|fra|fr|spanish|espanol|esp|es|ar|ara|arabic|ma|mar)\b"""),
                " "
            )

        // 3. Common MENA provider alias & sub-brand synonym normalization + zero-padded number canonicalization
        val cleaned = step2
            .replace(Regex("""\bbe\s*in\s*sports?\b"""), "bein")
            .replace(Regex("""\bbein\s*sports?\b"""), "bein")
            .replace(Regex("""\bbe\s*in\b"""), "bein")
            .replace(Regex("""\b(al\s*riyadia|arriyadia|riyadia|snrt|al\s*aoula)\b"""), "arryadia")
            .replace(Regex("""\bssc\s*sports?\b"""), "ssc")
            .replace(Regex("""\b(abu\s*dhabi\s*sports?|ad\s*sports?)\b"""), "adsport")
            .replace(Regex("""\bal\s*kass(\s*sports?)?\b"""), "alkass")
            .replace(Regex("""\bon\s*time(\s*sports?)?\b"""), "ontime")
            .replace(Regex("""\bstarz\s*play(\s*sports?)?\b"""), "starzplay")
            .replace(Regex("""\bthmanyah(\s*sports?)?\b"""), "thmanyah")
            .replace(Regex("""\bdubai\s*sports?\b"""), "dubaisport")
            // Sub-brand synonym normalization
            .replace(Regex("""\b(xtra|extra)\b"""), "extra")
            .replace(Regex("""\b(maxx|max)\b"""), "max")
            .replace(Regex("""\b(prem|premium)\b"""), "premium")
            .replace(Regex("""\b(asia|asian)\b"""), "afc")
            // Number word normalization
            .replace(Regex("""\bone\b"""), "1")
            .replace(Regex("""\btwo\b"""), "2")
            .replace(Regex("""\bthree\b"""), "3")
            .replace(Regex("""\bfour\b"""), "4")
            .replace(Regex("""\bfive\b"""), "5")
            .replace(Regex("""\bsix\b"""), "6")
            .replace(Regex("""\bseven\b"""), "7")
            .replace(Regex("""\beight\b"""), "8")
            .replace(Regex("""\bnine\b"""), "9")
            .replace(Regex("""\bten\b"""), "10")
            // Strip leading zeros on channel numbers ("05" -> "5", "02" -> "2", "01" -> "1", while "10" stays "10")
            .replace(Regex("""\b0+([1-9]\d*)\b"""), "$1")
            .replace(Regex("""\s+"""), " ")
            .trim()

        val canonicalProvider = when {
            Regex("""\bbein\b""").containsMatchIn(cleaned) -> "bein"
            Regex("""\barryadia\b""").containsMatchIn(cleaned) -> "arryadia"
            Regex("""\btamazight\b""").containsMatchIn(cleaned) -> "tamazight"
            Regex("""\bmaghribia\b""").containsMatchIn(cleaned) -> "maghribia"
            Regex("""\bssc\b""").containsMatchIn(cleaned) -> "ssc"
            Regex("""\bthmanyah\b""").containsMatchIn(cleaned) -> "thmanyah"
            Regex("""\badsport\b""").containsMatchIn(cleaned) -> "adsport"
            Regex("""\bstarzplay\b""").containsMatchIn(cleaned) -> "starzplay"
            Regex("""\balkass\b""").containsMatchIn(cleaned) -> "alkass"
            Regex("""\bontime\b""").containsMatchIn(cleaned) -> "ontime"
            Regex("""\bdubaisport\b""").containsMatchIn(cleaned) -> "dubaisport"
            else -> cleaned.split(" ").firstOrNull().orEmpty()
        }

        val numbers = Regex("""\b(\d+)\b""")
            .findAll(cleaned)
            .map { matchResult ->
                val rawDigits = matchResult.groupValues[1]
                rawDigits.trimStart('0').ifEmpty { "0" }
            }
            .filter { numStr ->
                val n = numStr.toIntOrNull() ?: 0
                n in 1..99
            }
            .distinct()
            .toList()

        val subBrands = setOf("premium", "extra", "max", "afc", "news", "global", "nba")
            .filter { token -> Regex("""\b$token\b""").containsMatchIn(cleaned) }
            .toSet()

        val languageTokens = buildSet {
            if (hasExplicitEnglish) add("en")
            if (hasExplicitFrench) add("fr")
            if (hasExplicitSpanish) add("es")
        }

        // Note: For beIN Sports MENA, "Premium 1..3" are the legacy names for flagship "beIN Sports 1..3 HD",
        // whereas "max", "extra", "afc" are distinct overflow/tournament sub-brands.
        val distinctSubBrandsForKey = subBrands - "premium"
        val canonicalKey = buildString {
            append(canonicalProvider)
            append("|nums=").append(numbers.sorted().joinToString(","))
            append("|brands=").append(distinctSubBrandsForKey.sorted().joinToString(","))
            append("|lang=").append(languageTokens.sorted().joinToString(","))
            if (is4kFeed && numbers.isEmpty()) {
                append("|4k=true")
            }
        }

        return NormalizedChannelProfile(
            normalizedText = cleaned,
            canonicalProvider = canonicalProvider,
            channelNumbers = numbers,
            subBrandTokens = subBrands,
            languageTokens = languageTokens,
            is4kFeed = is4kFeed,
            canonicalKey = canonicalKey
        )
    }

    /**
     * Multi-Tier Strict Favorites Channel Matcher (`matchFavoriteChannelsStrictly`):
     *
     * Matches the Kickoff `channelLabel` exclusively against the user's saved `favoriteStreams`:
     * - **Strict Provider & Number Isolation**:
     *   `"beIN Sports 1 HD"` / `"beIN Sports 1 4K"` ONLY matches channel `1` (`1` or `01`), and NEVER matches
     *   `"beIN Sports 10"`, `"beIN Sports 11"`, `"beIN Sports 12"`, or `"beIN Sports 4"`.
     * - **Prefix & Codec Immunity**:
     *   `"AR| BEIN SPORTS 02 HD"` (even with `MP4A-LATM` or `[01]` prefix) normalizes to `bein` + `["2"]`
     *   and matches `"beIN Sports 2 HD"` with top priority.
     * - **Sub-Brand Separation (`STANDARD`/`PREMIUM` vs `MAX` vs `EXTRA` vs `AFC`)**:
     *   - Non-sport beIN channels (`beIN Sports News`, `beIN Sports Global`, `beIN Sports NBA`) are excluded.
     *   - When the target channel is a standard flagship channel (`"beIN Sports 2 HD"`, `"beIN Sports 1 4K"`),
     *     Favorites that belong to overflow sub-brands (`Max`, `Extra`/`Xtra`, `AFC`) are strictly rejected so
     *     `Max 2` or `Xtra 2` never hijacks `beIN Sports 2 HD`.
     *   - `beIN Sports Premium 1..3` in Favorites is recognized as equivalent to flagship `beIN Sports 1..3 HD`.
     *   - When the target specifies an overflow sub-brand (e.g., `"beIN Sports max 5"` or `"beIN Sports 4 Max"`):
     *     - **Tier 1**: Exact sub-brand match (`Max 5`, `max 05`, `5 max`, `05 max`). If any Tier 1 match exists in Favorites,
     *       returns ONLY Tier 1 matches.
     *     - **Tier 2**: If the user has no `Max` channel for that number in Favorites, falls back to standard `"beIN Sports N HD"`
     *       with the exact same channel number (`4`).
     */
    internal fun matchFavoriteChannelsStrictly(
        favoriteStreams: List<StreamEntity>,
        channelLabel: String
    ): List<StreamEntity> {
        if (favoriteStreams.isEmpty()) return emptyList()
        val targetProfile = normalizeChannelNameForFavoritesMatch(channelLabel)
        if (targetProfile.canonicalProvider.isBlank()) return emptyList()

        val targetHardSubBrands = targetProfile.subBrandTokens intersect HARD_SUB_BRANDS

        data class RankedFavoriteMatch(
            val stream: StreamEntity,
            val tier: Int, // Tier 1 = Exact sub-brand & language, Tier 2 = Same sub-brand / alternate language, Tier 3 = Standard number fallback
            val score: Int
        )

        val matchedWithRank = favoriteStreams.mapNotNull { favStream ->
            val favProfile = normalizeChannelNameForFavoritesMatch(favStream.name)

            // 1. Canonical provider must match (after MENA alias normalization)
            if (favProfile.canonicalProvider != targetProfile.canonicalProvider) {
                return@mapNotNull null
            }

            // Exclude non-football beIN channels (News, Global, NBA) unless explicitly requested
            if (favProfile.subBrandTokens.any { it in NON_FOOTBALL_SUB_BRANDS } &&
                targetProfile.subBrandTokens.none { it in NON_FOOTBALL_SUB_BRANDS }
            ) {
                return@mapNotNull null
            }

            val favHardSubBrands = favProfile.subBrandTokens intersect HARD_SUB_BRANDS

            // 2. Strict Channel Number Check using word-boundary regex on zero-normalized numbers (\b1\b, \b2\b, \b5\b)
            if (targetProfile.channelNumbers.isNotEmpty()) {
                val isUnnumbered4kMatch = targetProfile.canonicalProvider == "bein" &&
                    targetProfile.is4kFeed &&
                    targetProfile.channelNumbers == listOf("1") &&
                    favProfile.is4kFeed &&
                    favProfile.channelNumbers.isEmpty() &&
                    favHardSubBrands.isEmpty()

                if (!isUnnumbered4kMatch) {
                    val allNumbersMatchWordBoundary = targetProfile.channelNumbers.all { num ->
                        Regex("""\b${Regex.escape(num)}\b""").containsMatchIn(favProfile.normalizedText)
                    }
                    if (!allNumbersMatchWordBoundary || favProfile.channelNumbers != targetProfile.channelNumbers) {
                        return@mapNotNull null
                    }
                }
            } else if (targetProfile.is4kFeed && targetProfile.canonicalProvider == "bein") {
                // Target is "beIN 4K" / "beIN Sports 4K": match either un-numbered 4K or channel 1
                val valid4kOrCh1 = (favProfile.is4kFeed && favProfile.channelNumbers.isEmpty()) ||
                    favProfile.channelNumbers == listOf("1")
                if (!valid4kOrCh1) return@mapNotNull null
            }

            // 3. Sub-brand & Language Tier Evaluation
            var matchTier = 1
            if (targetHardSubBrands.isEmpty()) {
                // Target is a STANDARD / PREMIUM / 4K / Language channel (e.g. "beIN Sports 2 HD", "beIN Sports 1 4K", "SSC 1 HD").
                // NEVER allow overflow sub-brands ("max", "extra", "afc") to match a standard channel!
                if (favHardSubBrands.isNotEmpty() && targetProfile.channelNumbers.isNotEmpty()) {
                    return@mapNotNull null
                }
            } else {
                // Target explicitly requests a hard sub-brand ("max", "extra", or "afc")
                when {
                    favHardSubBrands == targetHardSubBrands -> {
                        // Exact sub-brand match (e.g., 'beIN Sports max 5' -> 'beIN Sports max 05', '5 max', '05 max')
                        matchTier = 1
                    }
                    favHardSubBrands.isEmpty() && targetProfile.canonicalProvider == "bein" -> {
                        // Fallback to standard flagship channel with the exact same number ONLY if no exact sub-brand exists in Favorites
                        matchTier = 3
                    }
                    else -> {
                        // Conflicting sub-brand (e.g., target is 'Max' but Favorite is 'Extra' or 'AFC') -> reject
                        return@mapNotNull null
                    }
                }
            }

            // 4. Language Feed Check (English / French / Arabic default)
            val languageBonus: Int
            if (targetProfile.languageTokens.isNotEmpty()) {
                if (favProfile.languageTokens == targetProfile.languageTokens) {
                    languageBonus = 35
                } else if (favProfile.languageTokens.isEmpty()) {
                    if (matchTier == 1) matchTier = 2
                    languageBonus = 0
                } else {
                    // Target is English but Favorite is French (or vice versa) -> reject if standard number
                    return@mapNotNull null
                }
            } else {
                // Target is default/Arabic feed (e.g. "beIN Sports 2 HD"): prefer default/Arabic Favorites over EN/FR feeds
                if (favProfile.languageTokens.isEmpty()) {
                    languageBonus = 25
                } else {
                    if (matchTier == 1) matchTier = 2
                    languageBonus = -15
                }
            }

            // 5. Rank within tier by sub-brand exactness + video quality: 4K/UHD -> FHD/1080 -> HD -> SD
            val lowerRaw = favStream.name.lowercase(Locale.ROOT)
            var qualityScore = 100 + languageBonus
            if (targetHardSubBrands.isNotEmpty() && favHardSubBrands == targetHardSubBrands) {
                qualityScore += 50
            }
            if (targetProfile.is4kFeed && favProfile.is4kFeed) {
                qualityScore += 45
            }
            if (lowerRaw.contains("4k") || lowerRaw.contains("uhd") || lowerRaw.contains("2160")) qualityScore += 30
            else if (lowerRaw.contains("fhd") || lowerRaw.contains("1080")) qualityScore += 20
            else if (lowerRaw.contains("hd") || lowerRaw.contains("720")) qualityScore += 10

            RankedFavoriteMatch(
                stream = favStream,
                tier = matchTier,
                score = qualityScore
            )
        }

        if (matchedWithRank.isEmpty()) return emptyList()

        // Always return the highest priority tier available in Favorites:
        // If Tier 1 matches exist (exact sub-brand & language), never mix in Tier 2 or Tier 3 fallback streams!
        val bestTier = matchedWithRank.minOf { it.tier }
        val filteredMatches = matchedWithRank.filter { it.tier == bestTier }

        return filteredMatches
            .sortedWith(compareByDescending<RankedFavoriteMatch> { it.score }.thenBy { it.stream.name })
            .map { it.stream }
    }

    fun detectMenaProviderFromLabel(channelLabel: String): String {
        val lower = channelLabel.lowercase(Locale.ROOT)
        return when {
            lower.contains("arryadia") || lower.contains("riyadia") || lower.contains("snrt") || lower.contains("al aoula") || lower.contains("tamazight") || lower.contains("maghribia") ->
                "Arryadia / SNRT (Morocco)"
            lower.contains("ssc") || lower.contains("thmanyah") || lower.contains("shahid") ->
                "SSC Sports (MENA)"
            lower.contains("ad sport") || lower.contains("abu dhabi") || lower.contains("starz") ->
                "AD Sports / STARZPLAY"
            lower.contains("kass") || lower.contains("alkass") ->
                "Alkass Sports (MENA)"
            lower.contains("on time") || lower.contains("ontime") ->
                "ON Time Sports (MENA)"
            lower.contains("dubai sport") ->
                "Dubai Sports (MENA)"
            else ->
                "beIN Sports MENA"
        }
    }

    private fun formatIsoToMoroccoTime(isoString: String): String {
        if (isoString.isBlank()) return "20:00"
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm"
        )
        for (pattern in patterns) {
            try {
                val parser = SimpleDateFormat(pattern, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val parsed: Date = parser.parse(isoString) ?: continue
                val formatter = SimpleDateFormat("HH:mm", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("Africa/Casablanca")
                }
                return formatter.format(parsed)
            } catch (_: Exception) {
            }
        }
        return "20:00"
    }

    private fun extractBeinHint(beinLabel: String): String {
        return beinLabel
            .lowercase(Locale.ROOT)
            .replace("bein sports", "")
            .replace("bein", "")
            .replace("mena", "")
            .replace("arryadia", "")
            .replace("snrt", "")
            .replace("tamazight", "")
            .replace("ssc", "")
            .replace("ad sports", "")
            .replace("alkass", "")
            .replace("al kass", "")
            .replace("hd", "")
            .replace("live", "")
            .replace("tnt", "")
            .trim()
            .ifBlank { "1" }
    }

    private fun matchTournamentBySportsDbIdOrName(
        sportsDbLeagueId: String,
        rawLeagueName: String
    ): TournamentCatalogItem? {
        if (sportsDbLeagueId.isNotBlank()) {
            val byId = ALL_TOURNAMENTS.firstOrNull { it.sportsDbLeagueId == sportsDbLeagueId }
            if (byId != null) return byId
        }
        return matchTournamentCatalog(rawLeagueName)
    }

    private fun matchTournamentCatalog(rawLeague: String): TournamentCatalogItem? {
        val lower = rawLeague.lowercase(Locale.ROOT)
        return when {
            lower.contains("botola") || lower.contains("moroccan") || lower.contains("coupe du tr") ->
                ALL_TOURNAMENTS.first { it.tournamentId == "botola_pro" }
            lower.contains("caf champions") || lower.contains("caf confederation") ->
                ALL_TOURNAMENTS.first { it.tournamentId == "caf_cl" }
            lower.contains("uefa champions") || lower == "champions league" ->
                ALL_TOURNAMENTS.first { it.tournamentId == "uefa_cl" }
            lower.contains("la liga") || lower.contains("laliga") || lower.contains("spanish primera") ->
                ALL_TOURNAMENTS.first { it.tournamentId == "laliga" }
            lower.contains("english premier league") || lower == "premier league" ->
                ALL_TOURNAMENTS.first { it.tournamentId == "premier_league" }
            lower.contains("italian serie a") || lower == "serie a" ->
                ALL_TOURNAMENTS.first { it.tournamentId == "serie_a" }
            lower.contains("french ligue 1") || lower == "ligue 1" ->
                ALL_TOURNAMENTS.first { it.tournamentId == "ligue_1" }
            lower.contains("german bundesliga") || lower == "bundesliga" ->
                ALL_TOURNAMENTS.first { it.tournamentId == "bundesliga" }
            lower.contains("saudi pro") || lower.contains("roshn") ->
                ALL_TOURNAMENTS.first { it.tournamentId == "saudi_pro" }
            lower.contains("uefa europa") || lower.contains("europa league") || lower.contains("europa conference") ->
                ALL_TOURNAMENTS.first { it.tournamentId == "uefa_el" }
            else -> null
        }
    }

    private fun formatIsoToMoroccoDate(isoString: String): String {
        if (isoString.isBlank()) return ""
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd"
        )
        for (pattern in patterns) {
            try {
                val parser = SimpleDateFormat(pattern, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val parsed: Date = parser.parse(isoString) ?: continue
                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("Africa/Casablanca")
                }
                return formatter.format(parsed)
            } catch (_: Exception) {
            }
        }
        return ""
    }

    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "has_completed_teams_onboarding_v1"

        // API #1: SportAPI.ai Base URL & 2,000 req/day ceiling
        const val SPORTAPI_BASE_URL = "https://sportapi.ai/api"
        private const val SPORTAPI_MAX_REQ_PER_DAY = 2000

        // API #2: TheSportsDB Free v1 API with shared public key 123 & 60 req/min ceiling
        const val THESPORTSDB_BASE_URL = "https://www.thesportsdb.com/api/v1/json/123"
        const val BOTOLA_PRO_SPORTSDB_LEAGUE_ID = "4520"
        private const val THESPORTSDB_MAX_REQ_PER_MINUTE = 60

        private val IGNORED_TEAM_TOKENS = setOf(
            "fc", "ac", "ca", "club", "cf", "sc", "as", "rs", "de", "al", "el"
        )

        private val MENA_BROADCASTER_KEYWORDS = listOf(
            "bein",
            "be in",
            "arryadia",
            "riyadia",
            "snrt",
            "al aoula",
            "tamazight",
            "maghribia",
            "ssc",
            "thmanyah",
            "shahid",
            "ad sport",
            "abu dhabi",
            "starz",
            "alkass",
            "al kass",
            "on time",
            "ontime",
            "dubai sport",
            "sharjah sport",
            "kuwait sport",
            "oman sport",
            "bahrain sport",
            "iraqia",
            "watania"
        )

        private val HARD_SUB_BRANDS = setOf("max", "extra", "afc")
        private val NON_FOOTBALL_SUB_BRANDS = setOf("news", "global", "nba")

        private val BROADCASTER_START_REGEX = Regex(
            """\b(be\s*in|bein|arryadia|arriyadia|riyadia|al\s*riyadia|snrt|al\s*aoula|tamazight|maghribia|ssc|thmanyah|shahid|ad\s*sport|abu\s*dhabi|starz|alkass|al\s*kass|on\s*time|ontime|dubai\s*sport|sharjah\s*sport|kuwait\s*sport|oman\s*sport|bahrain\s*sport|iraqia|watania)\b"""
        )

        private val DEFAULT_GENERATED_POOL_CHANNELS = setOf(
            "beIN Sports 1 HD",
            "beIN Sports 2 HD",
            "beIN Sports 3 HD",
            "beIN Sports 4 HD",
            "beIN Sports 5 HD",
            "beIN Sports 6 HD",
            "beIN Sports 7 HD",
            "beIN Sports 8 HD",
            "beIN Sports 9 HD",
            "beIN Sports 1 4K",
            "beIN Sports 1 Max",
            "beIN Sports 2 Max",
            "beIN Sports 3 Max",
            "beIN Sports 4 Max",
            "beIN Sports 5 Max",
            "beIN Sports 6 Max",
            "beIN Sports Max 4",
            "beIN Sports Max 5",
            "beIN Sports 1 Extra",
            "beIN Sports 2 Extra",
            "beIN Sports 3 Extra",
            "beIN Sports 4 Extra",
            "beIN Sports 5 Extra",
            "beIN Sports Xtra 1",
            "beIN Sports Xtra 2",
            "beIN Sports 1 English",
            "beIN Sports 2 English",
            "beIN Sports 1 French",
            "Arryadia HD",
            "Arryadia TNT",
            "Al Aoula HD",
            "Tamazight HD",
            "SNRT Live",
            "AD Sports Premium 1",
            "AD Sports Premium 2",
            "STARZPLAY",
            "SSC 1 HD",
            "SSC 2 HD",
            "SSC Extra 1",
            "SSC Extra 2",
            "Thmanyah Sports 1",
            "ON Time Sports 1"
        )

        val ALL_TOURNAMENTS = listOf(
            TournamentCatalogItem(
                tournamentId = "uefa_cl",
                name = "UEFA Champions League",
                countryOrRegion = "Europe",
                espnSlug = "uefa.champions",
                sportsDbLeagueId = "4480",
                primaryMenaProvider = "beIN Sports MENA",
                defaultBeinPool = listOf("beIN Sports 1 HD", "beIN Sports 2 HD", "beIN Sports 3 HD", "beIN Sports 4 HD", "beIN Sports 5 HD", "beIN Sports 6 HD", "beIN Sports 1 4K"),
                fallbackMenaPool = listOf("beIN Sports 2 HD", "beIN Sports 3 HD", "beIN Sports 1 English", "beIN Sports 1 4K"),
                defaultSortOrder = 1
            ),
            TournamentCatalogItem(
                tournamentId = "laliga",
                name = "LaLiga EA Sports",
                countryOrRegion = "Spain",
                espnSlug = "esp.1",
                sportsDbLeagueId = "4335",
                primaryMenaProvider = "beIN Sports MENA",
                defaultBeinPool = listOf("beIN Sports 3 HD", "beIN Sports 1 HD", "beIN Sports 2 HD", "beIN Sports 4 HD", "beIN Sports 1 4K"),
                fallbackMenaPool = listOf("beIN Sports 3 HD", "beIN Sports 1 HD", "beIN Sports 1 4K"),
                defaultSortOrder = 2
            ),
            TournamentCatalogItem(
                tournamentId = "premier_league",
                name = "Premier League",
                countryOrRegion = "England",
                espnSlug = "eng.1",
                sportsDbLeagueId = "4328",
                primaryMenaProvider = "beIN Sports MENA",
                defaultBeinPool = listOf("beIN Sports 1 HD", "beIN Sports 2 HD", "beIN Sports 3 HD", "beIN Sports 4 HD", "beIN Sports 1 English", "beIN Sports 1 4K"),
                fallbackMenaPool = listOf("beIN Sports 1 HD", "beIN Sports 2 HD", "beIN Sports 1 English"),
                defaultSortOrder = 3
            ),
            TournamentCatalogItem(
                tournamentId = "ligue_1",
                name = "Ligue 1 McDonald's",
                countryOrRegion = "France",
                espnSlug = "fra.1",
                sportsDbLeagueId = "4334",
                primaryMenaProvider = "beIN Sports MENA",
                defaultBeinPool = listOf("beIN Sports 4 HD", "beIN Sports 2 HD", "beIN Sports 1 French", "beIN Sports 5 HD", "beIN Sports Max 4"),
                fallbackMenaPool = listOf("beIN Sports 4 HD", "beIN Sports 1 French", "beIN Sports Max 4"),
                defaultSortOrder = 4
            ),
            TournamentCatalogItem(
                tournamentId = "bundesliga",
                name = "Bundesliga",
                countryOrRegion = "Germany",
                espnSlug = "ger.1",
                sportsDbLeagueId = "4331",
                primaryMenaProvider = "beIN Sports MENA",
                defaultBeinPool = listOf("beIN Sports 5 HD", "beIN Sports 6 HD", "beIN Sports 4 HD", "beIN Sports Xtra 1", "beIN Sports Max 5"),
                fallbackMenaPool = listOf("beIN Sports 5 HD", "beIN Sports 6 HD", "beIN Sports Xtra 1"),
                defaultSortOrder = 5
            ),
            TournamentCatalogItem(
                tournamentId = "serie_a",
                name = "Serie A",
                countryOrRegion = "Italy",
                espnSlug = "ita.1",
                sportsDbLeagueId = "4332",
                primaryMenaProvider = "AD Sports / STARZPLAY",
                defaultBeinPool = listOf("AD Sports Premium 1", "AD Sports Premium 2", "STARZPLAY"),
                fallbackMenaPool = listOf("AD Sports Premium 2", "STARZPLAY"),
                defaultSortOrder = 6
            ),
            TournamentCatalogItem(
                tournamentId = "caf_cl",
                name = "CAF Champions League",
                countryOrRegion = "Africa",
                espnSlug = "caf.champions",
                sportsDbLeagueId = "4486",
                primaryMenaProvider = "beIN Sports MENA",
                defaultBeinPool = listOf("beIN Sports 6 HD", "beIN Sports 5 HD", "Arryadia TNT", "ON Time Sports 1"),
                fallbackMenaPool = listOf("Arryadia TNT", "ON Time Sports 1", "beIN Sports 5 HD"),
                defaultSortOrder = 7
            ),
            TournamentCatalogItem(
                tournamentId = "botola_pro",
                name = "Botola Pro Inwi",
                countryOrRegion = "Morocco",
                espnSlug = "mar.1",
                sportsDbLeagueId = "4520",
                primaryMenaProvider = "Arryadia / SNRT",
                defaultBeinPool = listOf("Arryadia HD", "Arryadia TNT", "Al Aoula HD", "Tamazight HD"),
                fallbackMenaPool = listOf("Arryadia TNT", "Al Aoula HD", "Tamazight HD", "SNRT Live"),
                defaultSortOrder = 8
            ),
            TournamentCatalogItem(
                tournamentId = "uefa_el",
                name = "UEFA Europa League",
                countryOrRegion = "Europe",
                espnSlug = "uefa.europa",
                sportsDbLeagueId = "4481",
                primaryMenaProvider = "beIN Sports MENA",
                defaultBeinPool = listOf("beIN Sports 1 HD", "beIN Sports 2 HD", "beIN Sports 3 HD", "beIN Sports 4 HD", "beIN Sports 5 HD", "beIN Sports 6 HD"),
                fallbackMenaPool = listOf("beIN Sports 2 HD", "beIN Sports 3 HD", "beIN Sports Xtra 1"),
                defaultSortOrder = 9
            ),
            TournamentCatalogItem(
                tournamentId = "saudi_pro",
                name = "Roshn Saudi League",
                countryOrRegion = "Saudi Arabia",
                espnSlug = "ksa.1",
                sportsDbLeagueId = "4668",
                primaryMenaProvider = "SSC Sports",
                defaultBeinPool = listOf("SSC 1 HD", "SSC Extra 1", "SSC Extra 2", "Thmanyah Sports 1"),
                fallbackMenaPool = listOf("SSC Extra 1", "SSC Extra 2", "Thmanyah Sports 1"),
                defaultSortOrder = 10
            )
        )

        val GLOBAL_TEAM_INDEX = listOf(
            // Global & European Clubs
            FootballTeam("team_realmadrid", "Real Madrid", "RMA", "https://a.espncdn.com/i/teamlogos/soccer/500/86.png", "Spain", "LaLiga EA Sports"),
            FootballTeam("team_barcelona", "FC Barcelona", "BAR", "https://a.espncdn.com/i/teamlogos/soccer/500/83.png", "Spain", "LaLiga EA Sports"),
            FootballTeam("team_atletico", "Atlético Madrid", "ATM", "https://a.espncdn.com/i/teamlogos/soccer/500/1068.png", "Spain", "LaLiga EA Sports"),
            FootballTeam("team_sevilla", "Sevilla FC", "SEV", "https://a.espncdn.com/i/teamlogos/soccer/500/243.png", "Spain", "LaLiga EA Sports"),
            FootballTeam("team_realsociedad", "Real Sociedad", "RSO", "https://a.espncdn.com/i/teamlogos/soccer/500/89.png", "Spain", "LaLiga EA Sports"),
            FootballTeam("team_villarreal", "Villarreal CF", "VIL", "https://a.espncdn.com/i/teamlogos/soccer/500/102.png", "Spain", "LaLiga EA Sports"),
            FootballTeam("team_betis", "Real Betis", "BET", "https://a.espncdn.com/i/teamlogos/soccer/500/244.png", "Spain", "LaLiga EA Sports"),
            FootballTeam("team_mancity", "Manchester City", "MCI", "https://a.espncdn.com/i/teamlogos/soccer/500/382.png", "England", "Premier League"),
            FootballTeam("team_arsenal", "Arsenal", "ARS", "https://a.espncdn.com/i/teamlogos/soccer/500/359.png", "England", "Premier League"),
            FootballTeam("team_liverpool", "Liverpool", "LIV", "https://a.espncdn.com/i/teamlogos/soccer/500/364.png", "England", "Premier League"),
            FootballTeam("team_chelsea", "Chelsea", "CHE", "https://a.espncdn.com/i/teamlogos/soccer/500/363.png", "England", "Premier League"),
            FootballTeam("team_manutd", "Manchester United", "MUN", "https://a.espncdn.com/i/teamlogos/soccer/500/360.png", "England", "Premier League"),
            FootballTeam("team_tottenham", "Tottenham Hotspur", "TOT", "https://a.espncdn.com/i/teamlogos/soccer/500/367.png", "England", "Premier League"),
            FootballTeam("team_newcastle", "Newcastle United", "NEW", "https://a.espncdn.com/i/teamlogos/soccer/500/361.png", "England", "Premier League"),
            FootballTeam("team_astonvilla", "Aston Villa", "AVL", "https://a.espncdn.com/i/teamlogos/soccer/500/362.png", "England", "Premier League"),
            FootballTeam("team_psg", "Paris Saint-Germain", "PSG", "https://a.espncdn.com/i/teamlogos/soccer/500/160.png", "France", "Ligue 1 McDonald's"),
            FootballTeam("team_marseille", "Olympique de Marseille", "OM", "https://a.espncdn.com/i/teamlogos/soccer/500/176.png", "France", "Ligue 1 McDonald's"),
            FootballTeam("team_monaco", "AS Monaco", "ASM", "https://a.espncdn.com/i/teamlogos/soccer/500/174.png", "France", "Ligue 1 McDonald's"),
            FootballTeam("team_lyon", "Olympique Lyonnais", "OL", "https://a.espncdn.com/i/teamlogos/soccer/500/167.png", "France", "Ligue 1 McDonald's"),
            FootballTeam("team_lille", "LOSC Lille", "LIL", "https://a.espncdn.com/i/teamlogos/soccer/500/166.png", "France", "Ligue 1 McDonald's"),
            FootballTeam("team_bayern", "Bayern Munich", "BAY", "https://a.espncdn.com/i/teamlogos/soccer/500/132.png", "Germany", "Bundesliga"),
            FootballTeam("team_dortmund", "Borussia Dortmund", "BVB", "https://a.espncdn.com/i/teamlogos/soccer/500/124.png", "Germany", "Bundesliga"),
            FootballTeam("team_leverkusen", "Bayer Leverkusen", "B04", "https://a.espncdn.com/i/teamlogos/soccer/500/131.png", "Germany", "Bundesliga"),
            FootballTeam("team_leipzig", "RB Leipzig", "RBL", "https://a.espncdn.com/i/teamlogos/soccer/500/11420.png", "Germany", "Bundesliga"),
            FootballTeam("team_inter", "Inter Milan", "INT", "https://a.espncdn.com/i/teamlogos/soccer/500/110.png", "Italy", "Serie A"),
            FootballTeam("team_acmilan", "AC Milan", "MIL", "https://a.espncdn.com/i/teamlogos/soccer/500/103.png", "Italy", "Serie A"),
            FootballTeam("team_juventus", "Juventus", "JUV", "https://a.espncdn.com/i/teamlogos/soccer/500/111.png", "Italy", "Serie A"),
            FootballTeam("team_napoli", "Napoli", "NAP", "https://a.espncdn.com/i/teamlogos/soccer/500/114.png", "Italy", "Serie A"),
            FootballTeam("team_roma", "AS Roma", "ROM", "https://a.espncdn.com/i/teamlogos/soccer/500/104.png", "Italy", "Serie A"),
            // Moroccan, African & Arab Clubs (with TheSportsDB `/tiny` badges)
            FootballTeam("team_raja", "Raja Club Athletic", "RCA", "https://r2.thesportsdb.com/images/media/team/badge/xsuwtw1473538028.png/tiny", "Morocco", "Botola Pro Inwi"),
            FootballTeam("team_wydad", "Wydad AC", "WAC", "https://r2.thesportsdb.com/images/media/team/badge/vwpvry1467462651.png/tiny", "Morocco", "Botola Pro Inwi"),
            FootballTeam("team_far", "AS FAR Rabat", "FAR", "https://r2.thesportsdb.com/images/media/team/badge/k7333w1579976542.png/tiny", "Morocco", "Botola Pro Inwi"),
            FootballTeam("team_berkane", "RS Berkane", "RSB", "https://r2.thesportsdb.com/images/media/team/badge/8y9v5v1547392341.png/tiny", "Morocco", "Botola Pro Inwi"),
            FootballTeam("team_fus", "FUS Rabat", "FUS", "", "Morocco", "Botola Pro Inwi"),
            FootballTeam("team_mas", "MAS Fès", "MAS", "", "Morocco", "Botola Pro Inwi"),
            FootballTeam("team_irt", "Ittihad Tanger", "IRT", "", "Morocco", "Botola Pro Inwi"),
            FootballTeam("team_husa", "Hassania Agadir", "HUSA", "", "Morocco", "Botola Pro Inwi"),
            FootballTeam("team_ocs", "Olympic Safi", "OCS", "", "Morocco", "Botola Pro Inwi"),
            FootballTeam("team_mat", "Moghreb Tétouan", "MAT", "", "Morocco", "Botola Pro Inwi"),
            FootballTeam("team_codm", "CODM Meknès", "CODM", "", "Morocco", "Botola Pro Inwi"),
            FootballTeam("team_difaa", "Difaâ Hassani El Jadidi", "DHJ", "", "Morocco", "Botola Pro Inwi"),
            FootballTeam("team_alahly", "Al Ahly SC", "AHL", "https://r2.thesportsdb.com/images/media/team/badge/17499d1547391568.png/tiny", "Egypt", "CAF Champions League"),
            FootballTeam("team_zamalek", "Zamalek SC", "ZSC", "", "Egypt", "CAF Confederations Cup"),
            FootballTeam("team_esperance", "Espérance de Tunis", "EST", "", "Tunisia", "CAF Champions League"),
            FootballTeam("team_sundowns", "Mamelodi Sundowns", "SUN", "", "South Africa", "CAF Champions League"),
            FootballTeam("team_alhilal", "Al Hilal SFC", "HIL", "https://a.espncdn.com/i/teamlogos/soccer/500/929.png", "Saudi Arabia", "Roshn Saudi League"),
            FootballTeam("team_alnassr", "Al Nassr FC", "NAS", "https://a.espncdn.com/i/teamlogos/soccer/500/8171.png", "Saudi Arabia", "Roshn Saudi League"),
            FootballTeam("team_alittihad", "Al Ittihad Club", "ITT", "", "Saudi Arabia", "Roshn Saudi League"),
            FootballTeam("team_alahli_ksa", "Al Ahli Saudi FC", "AHL", "", "Saudi Arabia", "Roshn Saudi League"),
            // National Teams
            FootballTeam("nat_morocco", "Morocco National Team", "MAR", "https://a.espncdn.com/i/teamlogos/countries/500/mar.png", "Morocco", "International / AFCON"),
            FootballTeam("nat_spain", "Spain National Team", "ESP", "https://a.espncdn.com/i/teamlogos/countries/500/esp.png", "Spain", "International"),
            FootballTeam("nat_france", "France National Team", "FRA", "https://a.espncdn.com/i/teamlogos/countries/500/fra.png", "France", "International"),
            FootballTeam("nat_argentina", "Argentina National Team", "ARG", "https://a.espncdn.com/i/teamlogos/countries/500/arg.png", "Argentina", "International"),
            FootballTeam("nat_brazil", "Brazil National Team", "BRA", "https://a.espncdn.com/i/teamlogos/countries/500/bra.png", "Brazil", "International"),
            FootballTeam("nat_portugal", "Portugal National Team", "POR", "https://a.espncdn.com/i/teamlogos/countries/500/por.png", "Portugal", "International")
        )
    }
}
