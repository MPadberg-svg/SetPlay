package com.example.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.Normalizer
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs

enum class MatchCenterTab(val title: String) {
    SUMMARY("Summary"),
    LINEUPS("Lineups"),
    STATS("Stats"),
    H2H_TABLE("H2H / Table"),
    TV_GUIDE("TV Guide")
}

enum class MatchEventType {
    GOAL,
    PENALTY_GOAL,
    OWN_GOAL,
    YELLOW_CARD,
    RED_CARD,
    SUBSTITUTION,
    VAR_DECISION
}

enum class MatchTeamSide {
    HOME,
    AWAY
}

data class MatchTimelineEvent(
    val id: String,
    val minute: Int,
    val minuteLabel: String,      // e.g. "23'", "45+2'", "78'"
    val halfLabel: String,        // "1ST HALF" or "2ND HALF"
    val teamSide: MatchTeamSide,
    val eventType: MatchEventType,
    val primaryPlayer: String,    // Scorer, carded player, or player subbed IN
    val secondaryPlayer: String = "", // Assist provider or player subbed OUT
    val runningScore: String = "",    // e.g. "1 - 0"
    val detailNote: String = ""       // e.g. "Left-footed shot", "Penalty", "Tactical foul"
)

data class PitchPlayerNode(
    val playerId: String,
    val name: String,
    val shortName: String,
    val jerseyNumber: Int,
    val positionCode: String,     // GK, RB, CB, LB, CDM, CM, CAM, RW, LW, ST
    val rowIndex: Int,            // 0 = GK, 1 = DEF, 2 = MID, 3 = AM/WING, 4 = FWD
    val colIndex: Int,            // 0-based index within the tactical line
    val colCountInRow: Int,       // Total players in that tactical line
    val rating: Double,           // 365Scores-style live/final rating (e.g. 7.8)
    val goals: Int = 0,
    val assists: Int = 0,
    val hasYellowCard: Boolean = false,
    val hasRedCard: Boolean = false,
    val subbedOffMinute: String? = null,
    val subbedOnMinute: String? = null,
    val photoUrl: String = "",
    val isMvp: Boolean = false,
    val isCaptain: Boolean = false,
    val minutesPlayed: Int = 0,
    val shotsTotal: Int = 0,
    val shotsOnTarget: Int = 0,
    val expectedGoals: Double = 0.0,
    val expectedGoalsOnTarget: Double = 0.0,
    val passesAccurate: Int = 0,
    val passesTotal: Int = 0,
    val touches: Int = 0,
    val duelsWon: Int = 0,
    val saves: Int = 0
)

data class CachedMatchDossier(
    val timestampMs: Long,
    val matchScoreSignature: String,
    val dossier: MatchFullDossier
)

data class MissingPlayerItem(
    val playerName: String,
    val teamSide: MatchTeamSide,
    val positionCode: String,
    val reason: String,           // e.g. "Muscle Injury", "Suspended (Yellow Card Accumulation)"
    val statusBadge: String       // "OUT" or "DOUBTFUL"
)

data class MatchLineupsData(
    val isConfirmed: Boolean,     // true = "CONFIRMED XI", false = "PROBABLE XI"
    val homeFormation: String,    // e.g. "4-3-3"
    val awayFormation: String,    // e.g. "4-2-3-1"
    val homeCoach: String,
    val awayCoach: String,
    val homeStartingXI: List<PitchPlayerNode>,
    val awayStartingXI: List<PitchPlayerNode>,
    val homeBench: List<PitchPlayerNode>,
    val awayBench: List<PitchPlayerNode>,
    val missingPlayers: List<MissingPlayerItem>,
    val homeAvgRating: Double = 7.0,
    val awayAvgRating: Double = 7.0,
    val lineupStatusHeadline: String = "Official lineups expected 1 hour before kick-off",
    val lineupStatusSubtext: String = "Probable Starting XI based on recent club selections. Official team sheets are published 60 minutes prior to kick-off.",
    val ratingSourceLabel: String = "Sofascore • Season Form Index"
)

data class MomentumMinutePoint(
    val minute: Int,
    val value: Float // -100f..100f: positive = Home attacking pressure, negative = Away attacking pressure
)

enum class ShotOutcome { GOAL, ON_TARGET_SAVED, OFF_TARGET, BLOCKED }

data class MatchShotPoint(
    val id: String,
    val minute: Int,
    val playerName: String,
    val teamSide: MatchTeamSide,
    val outcome: ShotOutcome,
    val xg: Double,
    val xgot: Double = 0.0,
    val normalizedX: Float, // 0.0f..1.0f across pitch width
    val normalizedY: Float  // 0.0f..1.0f distance from goal line
)

data class MatchStatComparison(
    val statLabel: String,
    val homeValueText: String,
    val awayValueText: String,
    val homeRatio: Float,         // 0.0f..1.0f for dual comparison progress bar
    val isKeyMetric: Boolean = false,
    val category: String = "Top Stats" // "Top Stats", "Shots & xG", "Passes", "Defense & Duels", "Goalkeeping"
)

data class H2HMeetingItem(
    val dateLabel: String,
    val competitionName: String,
    val homeTeamName: String,
    val awayTeamName: String,
    val homeScore: Int,
    val awayScore: Int
)

data class LeagueStandingRow(
    val rank: Int,
    val teamName: String,
    val teamBadge: String,
    val played: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goalDiffFormatted: String,
    val points: Int,
    val formLast5: List<String>,  // e.g. ["W", "W", "D", "L", "W"]
    val isHomeTeam: Boolean = false,
    val isAwayTeam: Boolean = false,
    val zoneLabel: String? = null,
    val liveRankDelta: Int = 0    // +2 = moved up 2 places live, -1 = dropped 1 place live
)

data class MenaBroadcastChannelInfo(
    val channelName: String,          // Exact MENA channel name e.g. "beIN Sports 1 HD", "Arryadia TNT"
    val providerGroup: String,        // e.g. "beIN Sports MENA", "SNRT / Arryadia (Morocco)"
    val qualityBadge: String,         // "4K UHD", "FHD 1080p", "HD", "TNT"
    val commentaryInfo: String,       // e.g. "Arabic Commentary • MENA Feed"
    val satelliteInfo: String,        // e.g. "Es'hailSat 25.5°E / Nilesat 7°W"
    val isPrimaryBroadcaster: Boolean
)

data class MatchFullDossier(
    val match: FootballMatch,
    val halfTimeScoreLabel: String,   // e.g. "HT 1 - 0" or "HT 0 - 0"
    val fullTimeScoreLabel: String?,  // e.g. "FT 2 - 1"
    val refereeName: String,
    val attendanceLabel: String,
    val pitchConditionLabel: String,
    val timelineEvents: List<MatchTimelineEvent>,
    val lineups: MatchLineupsData,
    val stats: List<MatchStatComparison>,
    val homeFormLast5: List<String>,
    val awayFormLast5: List<String>,
    val h2hMeetings: List<H2HMeetingItem>,
    val standingsTable: List<LeagueStandingRow>,
    val menaBroadcastGuide: List<MenaBroadcastChannelInfo>,
    val countryZone: UserCountryZone = UserZoneRegistry.getZoneByCode(UserZoneRegistry.DEFAULT_COUNTRY_CODE),
    val momentumPoints: List<MomentumMinutePoint> = emptyList(),
    val shotMap: List<MatchShotPoint> = emptyList(),
    val matchBuzzInsights: List<String> = emptyList()
)

/**
 * Multi-source concurrent enricher (`Promise.allSettled` via `supervisorScope`) for the 5-Tab Match Center.
 * Combines live responses from Sofascore API (`api.sofascore.com`), TheSportsDB v1 (`lookuplineup.php`,
 * `lookup_all_players.php`, `lookuptimeline.php`, `lookupeventstats.php`, `lookuptable.php`) and
 * ESPN Soccer Summary API (`/apis/site/v2/sports/soccer/{slug}/summary?event={id}`)
 * with club-accurate tactical formations, player headshots, real-time ratings, HT/FT timelines, stats, H2H, and Country Zone broadcast channel guides.
 */
class MatchCenterEnricher(
    private val httpClient: OkHttpClient,
    private val canAcquireSportsDbPermit: () -> Boolean = { FootballRepository.tryAcquireSharedSportsDbPermit() }
) {
    internal val dossierCache = ConcurrentHashMap<String, CachedMatchDossier>()
    internal val networkFetchInvocationCount = AtomicInteger(0)
    private val teamSquadCache = ConcurrentHashMap<String, List<RawPlayerEntry>>()
    private val apiFootballFixtureCache = ConcurrentHashMap<String, JSONObject>()
    private val apiFootballDayFixturesCache = ConcurrentHashMap<String, org.json.JSONArray>()

    internal fun buildMatchScoreSignature(match: FootballMatch): String =
        "${match.homeScore ?: -1}-${match.awayScore ?: -1}-${match.status}"

    fun buildApiSportsPlayerPhotoUrl(playerId: Long): String =
        Companion.buildApiSportsPlayerPhotoUrl(playerId)

    fun buildSofascorePlayerPhotoUrl(playerId: Long): String =
        Companion.buildSofascorePlayerPhotoUrl(playerId)

    fun build365ScoresAthletePhotoUrl(athleteId: Long): String =
        Companion.build365ScoresAthletePhotoUrl(athleteId)

    suspend fun fetchMatchFullDossier(
        match: FootballMatch,
        forceRefresh: Boolean = false,
        countryZone: UserCountryZone = UserZoneRegistry.getZoneByCode(UserZoneRegistry.DEFAULT_COUNTRY_CODE)
    ): MatchFullDossier = withContext(Dispatchers.IO) {
        val nowMs = System.currentTimeMillis()
        val cacheKey = "${match.matchId}_${countryZone.countryCode}"
        val currentSignature = buildMatchScoreSignature(match)
        val cachedEntry = dossierCache[cacheKey]
        if (!forceRefresh && cachedEntry != null) {
            if ((nowMs - cachedEntry.timestampMs) < DOSSIER_CACHE_TTL_MS &&
                cachedEntry.matchScoreSignature == currentSignature
            ) {
                return@withContext cachedEntry.dossier
            } else {
                // Score or status changed (e.g. live goal scored) or TTL expired -> invalidate
                dossierCache.remove(cacheKey)
                val apifId = match.apiFootballFixtureId.ifBlank {
                    if (match.matchId.startsWith("apif_")) match.matchId.removePrefix("apif_") else ""
                }
                if (apifId.isNotBlank()) {
                    apiFootballFixtureCache.remove(apifId)
                }
            }
        }

        networkFetchInvocationCount.incrementAndGet()

        // Fetch 1-call API-Football fixture dossier, live lineup, timeline, stats, Sofascore ratings, and standings in parallel (Promise.allSettled via supervisorScope)
        val tournament = FootballRepository.ALL_TOURNAMENTS.find { it.tournamentId == match.tournamentId }
        val livePayload = supervisorScope {
            val apiFootballDeferred = async {
                runCatching { fetchApiFootballFixtureDossier(match) }.getOrNull()
            }
            val tsdbLineupDeferred = async {
                runCatching {
                    if (match.sportsDbEventId.isNotBlank()) {
                        fetchTheSportsDbJson("${FootballRepository.THESPORTSDB_BASE_URL}/lookuplineup.php?id=${match.sportsDbEventId}")
                    } else null
                }.getOrNull()
            }
            val tsdbTimelineDeferred = async {
                runCatching {
                    if (match.sportsDbEventId.isNotBlank()) {
                        fetchTheSportsDbJson("${FootballRepository.THESPORTSDB_BASE_URL}/lookuptimeline.php?id=${match.sportsDbEventId}")
                    } else null
                }.getOrNull()
            }
            val tsdbStatsDeferred = async {
                runCatching {
                    if (match.sportsDbEventId.isNotBlank()) {
                        fetchTheSportsDbJson("${FootballRepository.THESPORTSDB_BASE_URL}/lookupeventstats.php?id=${match.sportsDbEventId}")
                    } else null
                }.getOrNull()
            }
            val tsdbTableDeferred = async {
                runCatching {
                    val leagueId = tournament?.sportsDbLeagueId.orEmpty()
                    if (leagueId.isNotBlank()) {
                        fetchTheSportsDbJson("${FootballRepository.THESPORTSDB_BASE_URL}/lookuptable.php?l=$leagueId")
                    } else null
                }.getOrNull()
            }
            val espnSummaryDeferred = async {
                runCatching {
                    var espnId = match.espnEventId.ifBlank {
                        if (match.matchId.startsWith("espn_")) match.matchId.removePrefix("espn_") else ""
                    }
                    if (espnId.isBlank() && tournament != null && tournament.espnSlug.isNotBlank()) {
                        espnId = resolveEspnEventIdFromScoreboard(tournament.espnSlug, match)
                    }
                    if (espnId.isNotBlank() && tournament != null && tournament.espnSlug.isNotBlank()) {
                        fetchTheSportsDbJson("https://site.api.espn.com/apis/site/v2/sports/soccer/${tournament.espnSlug}/summary?event=$espnId")
                    } else null
                }.getOrNull()
            }
            val sofascorePayloadDeferred = async {
                runCatching {
                    fetchSofascoreEventLineupsAndTv(match, countryZone)
                }.getOrNull()
            }
            val tsdbHomeSquadDeferred = async {
                runCatching { fetchTeamSquadFromSportsDb(match.homeTeamName) }.getOrDefault(emptyList())
            }
            val tsdbAwaySquadDeferred = async {
                runCatching { fetchTeamSquadFromSportsDb(match.awayTeamName) }.getOrDefault(emptyList())
            }

            val sofascoreBundle = sofascorePayloadDeferred.await()
            LiveDossierRawPayload(
                tsdbLineupJson = tsdbLineupDeferred.await(),
                tsdbTimelineJson = tsdbTimelineDeferred.await(),
                tsdbStatsJson = tsdbStatsDeferred.await(),
                tsdbTableJson = tsdbTableDeferred.await(),
                espnSummaryJson = espnSummaryDeferred.await(),
                sofascoreLineupsJson = sofascoreBundle?.lineupsJson,
                sofascoreTvChannels = sofascoreBundle?.tvChannels.orEmpty(),
                sofascoreGraphJson = sofascoreBundle?.graphJson,
                sofascoreShotmapJson = sofascoreBundle?.shotmapJson,
                tsdbHomeSquad = tsdbHomeSquadDeferred.await(),
                tsdbAwaySquad = tsdbAwaySquadDeferred.await(),
                apiFootballFixtureJson = apiFootballDeferred.await()
            )
        }

        val built = buildCompleteDossier(match, livePayload, countryZone)
        val enrichedLineups = enrichLineupPlayerPhotosRemotely(
            lineups = built.lineups,
            tsdbHomeSquad = livePayload.tsdbHomeSquad,
            tsdbAwaySquad = livePayload.tsdbAwaySquad
        )
        val finalDossier = built.copy(lineups = enrichedLineups)
        dossierCache[cacheKey] = CachedMatchDossier(
            timestampMs = System.currentTimeMillis(),
            matchScoreSignature = currentSignature,
            dossier = finalDossier
        )
        finalDossier
    }

    internal data class SofascoreEventBundle(
        val lineupsJson: JSONObject? = null,
        val tvChannels: List<String> = emptyList(),
        val graphJson: JSONObject? = null,
        val shotmapJson: JSONObject? = null
    )

    /**
     * Queries Sofascore's public JSON API for event lineups, real-time player ratings (`statistics.rating`),
     * official player headshot IDs (`https://api.sofascore.com/api/v1/player/{id}/image`), official
     * country TV broadcast channels (`https://api.sofascore.com/api/v1/tv/event/{id}/country-channels`),
     * attack momentum wave (`https://api.sofascore.com/api/v1/event/{id}/graph`), and
     * shot map (`https://api.sofascore.com/api/v1/event/{id}/shotmap`).
     * Gracefully returns null if Cloudflare blocks or the event isn't found.
     */
    private fun fetchSofascoreEventLineupsAndTv(
        match: FootballMatch,
        countryZone: UserCountryZone
    ): SofascoreEventBundle? {
        val dateParam = runCatching {
            val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Africa/Casablanca"))
            cal.add(java.util.Calendar.DAY_OF_YEAR, match.dayTab.dayOffset)
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("Africa/Casablanca")
            }.format(cal.time)
        }.getOrDefault("")
        if (dateParam.isBlank()) return null

        val scheduleUrl = "https://api.sofascore.com/api/v1/sport/football/scheduled-events/$dateParam"
        val scheduleJson = fetchJsonWithBrowserHeaders(scheduleUrl) ?: return null
        val events = scheduleJson.optJSONArray("events") ?: return null
        for (i in 0 until events.length()) {
            val ev = events.optJSONObject(i) ?: continue
            val hName = ev.optJSONObject("homeTeam")?.optString("name", "").orEmpty()
            val aName = ev.optJSONObject("awayTeam")?.optString("name", "").orEmpty()
            if (hName.isNotBlank() && aName.isNotBlank() &&
                FootballRepository.areSameClubNames(hName, match.homeTeamName) &&
                FootballRepository.areSameClubNames(aName, match.awayTeamName)
            ) {
                val eventId = ev.optLong("id", 0L)
                if (eventId > 0L) {
                    val lineupsJson = runCatching {
                        fetchJsonWithBrowserHeaders("https://api.sofascore.com/api/v1/event/$eventId/lineups")
                    }.getOrNull()
                    val tvJson = runCatching {
                        fetchJsonWithBrowserHeaders("https://api.sofascore.com/api/v1/tv/event/$eventId/country-channels")
                    }.getOrNull()
                    val graphJson = runCatching {
                        fetchJsonWithBrowserHeaders("https://api.sofascore.com/api/v1/event/$eventId/graph")
                    }.getOrNull()
                    val shotmapJson = runCatching {
                        fetchJsonWithBrowserHeaders("https://api.sofascore.com/api/v1/event/$eventId/shotmap")
                    }.getOrNull()
                    val tvChannels = if (tvJson != null) {
                        FootballRepository.parseSofascoreCountryChannelsJson(tvJson.toString(), countryZone)
                    } else {
                        emptyList()
                    }
                    return SofascoreEventBundle(
                        lineupsJson = lineupsJson,
                        tvChannels = tvChannels,
                        graphJson = graphJson,
                        shotmapJson = shotmapJson
                    )
                }
            }
        }
        return null
    }

    /**
     * Queries TheSportsDB for a club's full active roster (`searchteams.php` -> `lookup_all_players.php`)
     * so even when an upcoming match has no published match-day lineup yet, we still have the club's
     * real players, jersey numbers, positions, and high-res player cutouts!
     */
    private fun fetchTeamSquadFromSportsDb(teamName: String): List<RawPlayerEntry> {
        val cleanTeam = teamName.trim().lowercase(Locale.ROOT)
        if (cleanTeam.isBlank()) return emptyList()
        teamSquadCache[cleanTeam]?.let { return it }

        val encoded = java.net.URLEncoder.encode(teamName.trim(), "UTF-8")
        val teamSearchJson = fetchTheSportsDbJson("${FootballRepository.THESPORTSDB_BASE_URL}/searchteams.php?t=$encoded")
        val teamsArr = teamSearchJson?.optJSONArray("teams") ?: return emptyList()
        var teamId = ""
        for (i in 0 until teamsArr.length()) {
            val t = teamsArr.optJSONObject(i) ?: continue
            if (!t.optString("strSport", "Soccer").equals("Soccer", ignoreCase = true)) continue
            teamId = t.optString("idTeam", "")
            if (teamId.isNotBlank()) break
        }
        if (teamId.isBlank()) return emptyList()

        val playersJson = fetchTheSportsDbJson("${FootballRepository.THESPORTSDB_BASE_URL}/lookup_all_players.php?id=$teamId")
        val playerArr = playersJson?.optJSONArray("player") ?: return emptyList()
        val parsed = mutableListOf<RawPlayerEntry>()
        for (i in 0 until playerArr.length()) {
            val p = playerArr.optJSONObject(i) ?: continue
            val pName = p.optString("strPlayer", "").trim()
            if (pName.isBlank()) continue
            val posRaw = p.optString("strPosition", "Midfielder")
            if (posRaw.contains("Manager", ignoreCase = true) || posRaw.contains("Coach", ignoreCase = true)) continue
            val posAbbr = when {
                posRaw.contains("Goalkeeper", ignoreCase = true) || posRaw.equals("GK", ignoreCase = true) -> "GK"
                posRaw.contains("Back", ignoreCase = true) || posRaw.contains("Defender", ignoreCase = true) || posRaw.contains("Centre-Back", ignoreCase = true) -> "DF"
                posRaw.contains("Midfield", ignoreCase = true) -> "MF"
                else -> "FW"
            }
            val num = p.optString("strNumber", "").trim().toIntOrNull() ?: (i + 1)
            val photo = p.optString("strCutout", "").takeIf { it.startsWith("http") }
                ?: p.optString("strThumb", "").takeIf { it.startsWith("http") }
                ?: p.optString("strRender", "").takeIf { it.startsWith("http") }
                ?: resolveCuratedOrCachedPlayerPhoto(pName)
            if (photo.isNotBlank()) {
                cachePlayerPhoto(pName, photo)
            }
            parsed.add(
                RawPlayerEntry(
                    name = pName,
                    jersey = num,
                    positionAbbr = posAbbr,
                    photoUrl = photo
                )
            )
        }
        if (parsed.size >= 11) {
            teamSquadCache[cleanTeam] = parsed
        }
        return parsed
    }

    internal fun normalizePlayerNameForMatch(raw: String): String {
        if (raw.isBlank()) return ""
        return Normalizer.normalize(raw, Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
            .replace("ø", "o", ignoreCase = true)
            .replace("ł", "l", ignoreCase = true)
            .replace("đ", "d", ignoreCase = true)
            .replace("ß", "ss", ignoreCase = true)
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9\\s-]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun extractNormalizedLastName(normalizedFullName: String): String {
        val parts = normalizedFullName.split(" ").filter { it.isNotBlank() }
        if (parts.isEmpty()) return ""
        val last = parts.last()
        return if ((last == "junior" || last == "jr") && parts.size >= 2) {
            parts[parts.size - 2]
        } else {
            last
        }
    }

    internal fun resolvePlayerPhotoFromSquad(
        playerName: String,
        squad: List<RawPlayerEntry>
    ): String {
        if (playerName.isBlank() || squad.isEmpty()) return ""
        val targetNorm = normalizePlayerNameForMatch(playerName)
        if (targetNorm.isBlank()) return ""
        val targetLast = extractNormalizedLastName(targetNorm)

        // 1. Exact normalized full-name match
        squad.firstOrNull { entry ->
            entry.photoUrl.isNotBlank() && normalizePlayerNameForMatch(entry.name) == targetNorm
        }?.let { matched ->
            cachePlayerPhoto(playerName, matched.photoUrl)
            return matched.photoUrl
        }

        // 2. Full-name substring match or exact last-name token match
        squad.firstOrNull { entry ->
            if (entry.photoUrl.isBlank()) return@firstOrNull false
            val entryNorm = normalizePlayerNameForMatch(entry.name)
            if (entryNorm.isBlank()) return@firstOrNull false
            val entryLast = extractNormalizedLastName(entryNorm)
            entryNorm.contains(targetNorm) ||
                targetNorm.contains(entryNorm) ||
                (targetLast.length >= 3 && targetLast == entryLast)
        }?.let { matched ->
            cachePlayerPhoto(playerName, matched.photoUrl)
            return matched.photoUrl
        }

        return ""
    }

    private fun resolveEspnEventIdFromScoreboard(espnSlug: String, match: FootballMatch): String {
        val dateParam = runCatching {
            val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Africa/Casablanca"))
            cal.add(java.util.Calendar.DAY_OF_YEAR, match.dayTab.dayOffset)
            java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("Africa/Casablanca")
            }.format(cal.time)
        }.getOrDefault("")
        val url = if (dateParam.length == 8) {
            "https://site.api.espn.com/apis/site/v2/sports/soccer/$espnSlug/scoreboard?dates=$dateParam"
        } else {
            "https://site.api.espn.com/apis/site/v2/sports/soccer/$espnSlug/scoreboard"
        }
        val sbJson = runCatching { fetchTheSportsDbJson(url) }.getOrNull() ?: return ""
        val events = sbJson.optJSONArray("events") ?: return ""
        for (i in 0 until events.length()) {
            val ev = events.optJSONObject(i) ?: continue
            val comp = ev.optJSONArray("competitions")?.optJSONObject(0) ?: continue
            val competitors = comp.optJSONArray("competitors") ?: continue
            var hName = ""
            var aName = ""
            for (j in 0 until competitors.length()) {
                val c = competitors.optJSONObject(j) ?: continue
                val tName = c.optJSONObject("team")?.let {
                    it.optString("displayName", it.optString("shortDisplayName", it.optString("name", "")))
                }.orEmpty()
                if (c.optString("homeAway") == "home") hName = tName
                if (c.optString("homeAway") == "away") aName = tName
            }
            if (hName.isNotBlank() && aName.isNotBlank() &&
                FootballRepository.areSameClubNames(hName, match.homeTeamName) &&
                FootballRepository.areSameClubNames(aName, match.awayTeamName)
            ) {
                return ev.optString("id", "")
            }
        }
        return ""
    }

    private suspend fun enrichLineupPlayerPhotosRemotely(
        lineups: MatchLineupsData,
        tsdbHomeSquad: List<RawPlayerEntry> = emptyList(),
        tsdbAwaySquad: List<RawPlayerEntry> = emptyList()
    ): MatchLineupsData = supervisorScope {
        val homeFallbackCalls = AtomicInteger(0)
        val awayFallbackCalls = AtomicInteger(0)

        suspend fun resolveNodePhoto(
            node: PitchPlayerNode,
            teamSquad: List<RawPlayerEntry>,
            fallbackCounter: AtomicInteger
        ): PitchPlayerNode {
            // 1. Preserve official Tier 1 (media.api-sports.io), Tier 2 (api.sofascore.com), or Tier 3 (imagecache.365scores.com) cutout URL if already resolved
            if (node.photoUrl.contains("media.api-sports.io", ignoreCase = true) ||
                node.photoUrl.contains("imagecache.365scores.com", ignoreCase = true) ||
                node.photoUrl.contains("thesportsdb.com", ignoreCase = true)
            ) {
                return node
            }

            // 2. Match against single-call team squad cutout (`lookup_all_players.php?id={idTeam}` — Tier 4)
            val squadPhoto = resolvePlayerPhotoFromSquad(node.name, teamSquad)
            if (squadPhoto.isNotBlank()) {
                return node.copy(photoUrl = squadPhoto)
            }

            // 3. Preserve existing verified Sofascore player cutout URL (Tier 2) if present on node
            if (node.photoUrl.contains("api.sofascore.com", ignoreCase = true)) {
                return node
            }

            // 4. Check cached verified player photo
            val cached = resolveCuratedOrCachedPlayerPhoto(node.name)
            if (cached.isNotBlank()) {
                return node.copy(photoUrl = cached)
            }

            // 5. Limit individual `searchplayers.php` fallback calls to at most 4 unmatched players per team
            if (fallbackCounter.incrementAndGet() <= 4) {
                val fetched = fetchPlayerPhotoFromSportsDb(node.name)
                if (fetched.isNotBlank()) {
                    return node.copy(photoUrl = fetched)
                }
            }

            // 6. Fallback to verified Sofascore player image if present
            val finalUrl = node.photoUrl.ifBlank { resolveSofascoreFallbackPlayerPhoto(node.name) }
            return if (finalUrl.isNotBlank()) node.copy(photoUrl = finalUrl) else node
        }

        val homeStartersDeferred = lineups.homeStartingXI.map { node ->
            async { resolveNodePhoto(node, tsdbHomeSquad, homeFallbackCalls) }
        }
        val awayStartersDeferred = lineups.awayStartingXI.map { node ->
            async { resolveNodePhoto(node, tsdbAwaySquad, awayFallbackCalls) }
        }
        val homeBenchDeferred = lineups.homeBench.map { node ->
            async { resolveNodePhoto(node, tsdbHomeSquad, homeFallbackCalls) }
        }
        val awayBenchDeferred = lineups.awayBench.map { node ->
            async { resolveNodePhoto(node, tsdbAwaySquad, awayFallbackCalls) }
        }

        lineups.copy(
            homeStartingXI = homeStartersDeferred.map { it.await() },
            awayStartingXI = awayStartersDeferred.map { it.await() },
            homeBench = homeBenchDeferred.map { it.await() },
            awayBench = awayBenchDeferred.map { it.await() }
        )
    }

    internal fun fetchPlayerPhotoFromSportsDb(playerName: String): String {
        val cleanKey = normalizePlayerNameForMatch(playerName)
        if (cleanKey.isBlank() || cleanKey.startsWith("player ") || cleanKey.contains("#")) return ""
        playerPhotoCache[cleanKey]?.takeIf { it.isNotBlank() }?.let { return it }

        val encoded = java.net.URLEncoder.encode(playerName.trim(), "UTF-8")
        val json = runCatching {
            fetchTheSportsDbJson("${FootballRepository.THESPORTSDB_BASE_URL}/searchplayers.php?p=$encoded")
        }.getOrNull()

        val players = json?.optJSONArray("player")
        if (players != null) {
            for (i in 0 until players.length()) {
                val p = players.optJSONObject(i) ?: continue
                val sport = p.optString("strSport", "Soccer")
                if (!sport.equals("Soccer", ignoreCase = true)) continue
                val photo = p.optString("strCutout", "").takeIf { it.startsWith("http") }
                    ?: p.optString("strThumb", "").takeIf { it.startsWith("http") }
                    ?: p.optString("strRender", "").takeIf { it.startsWith("http") }
                    ?: ""
                if (photo.isNotBlank()) {
                    cachePlayerPhoto(playerName, photo)
                    return photo
                }
            }
        }
        // Never cache empty "" strings on network failure or miss!
        return ""
    }

    private fun fetchJsonWithBrowserHeaders(urlStr: String): JSONObject? {
        val url = urlStr.toHttpUrlOrNull() ?: return null
        val req = Request.Builder()
            .url(url)
            .header("User-Agent", FootballRepository.SOFASCORE_BROWSER_USER_AGENT)
            .header("Referer", FootballRepository.SOFASCORE_REFERER)
            .header("Accept", "application/json")
            .build()
        httpClient.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return null
            val body = resp.body?.string() ?: return null
            if (body.isBlank()) return null
            return JSONObject(body)
        }
    }

    private fun fetchTheSportsDbJson(urlStr: String): JSONObject? {
        if (urlStr.contains("thesportsdb.com", ignoreCase = true) && !canAcquireSportsDbPermit()) {
            return null
        }
        val url = urlStr.toHttpUrlOrNull() ?: return null
        val req = Request.Builder().url(url).build()
        httpClient.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return null
            val body = resp.body?.string() ?: return null
            if (body.isBlank()) return null
            return JSONObject(body)
        }
    }

    /**
     * Fetches the 1-call API-Football v3 fixture dossier (`GET https://v3.football.api-sports.io/fixtures?id={id}`)
     * which returns official `lineups` (with `player.id` -> `https://media.api-sports.io/football/players/{id}.png`),
     * `events` (goals, assists, cards, substitutions), `statistics` (possession, xG, shots, passes), and `players` ratings.
     */
    private fun fetchApiFootballFixtureDossier(match: FootballMatch): JSONObject? {
        if (!FootballRepository.isApiFootballKeyConfigured()) return null
        val apiKey = FootballRepository.resolveApiFootballKey()
        if (apiKey.isBlank()) return null

        var fixtureId = match.apiFootballFixtureId.ifBlank {
            if (match.matchId.startsWith("apif_")) {
                match.matchId.removePrefix("apif_").takeIf { it.all { ch -> ch.isDigit() } }.orEmpty()
            } else ""
        }

        if (fixtureId.isBlank()) {
            fixtureId = resolveApiFootballFixtureIdForMatch(match, apiKey)
        }
        if (fixtureId.isBlank()) return null

        apiFootballFixtureCache[fixtureId]?.let { return it }
        if (!FootballRepository.tryAcquireSharedApiFootballPermit(isMatchCenterDossier = true)) return null

        val url = "${FootballRepository.API_FOOTBALL_BASE_URL}/fixtures".toHttpUrlOrNull()
            ?.newBuilder()
            ?.addQueryParameter("id", fixtureId)
            ?.addQueryParameter("timezone", "Africa/Casablanca")
            ?.build() ?: return null

        val req = Request.Builder()
            .url(url)
            .header("x-apisports-key", apiKey)
            .build()

        httpClient.newCall(req).execute().use { resp ->
            FootballRepository.updateApiFootballRemainingQuota(resp.header("x-ratelimit-requests-remaining"))
            if (!resp.isSuccessful) return null
            val body = resp.body?.string() ?: return null
            val root = JSONObject(body)
            val item = root.optJSONArray("response")?.optJSONObject(0) ?: return null
            val existingLineups = item.optJSONArray("lineups")
            if (existingLineups == null || existingLineups.length() < 2) {
                fetchApiFootballFixtureLineups(fixtureId, apiKey)?.let { lineupsArr ->
                    item.put("lineups", lineupsArr)
                }
            }
            apiFootballFixtureCache[fixtureId] = item
            return item
        }
    }

    internal fun fetchApiFootballFixtureLineups(fixtureId: String, apiKey: String = FootballRepository.resolveApiFootballKey()): org.json.JSONArray? {
        if (fixtureId.isBlank() || apiKey.isBlank()) return null
        if (!FootballRepository.tryAcquireSharedApiFootballPermit(isMatchCenterDossier = true)) return null
        val url = "${FootballRepository.API_FOOTBALL_BASE_URL}/fixtures/lineups".toHttpUrlOrNull()
            ?.newBuilder()
            ?.addQueryParameter("fixture", fixtureId)
            ?.build() ?: return null
        val req = Request.Builder()
            .url(url)
            .header("x-apisports-key", apiKey)
            .build()
        return try {
            httpClient.newCall(req).execute().use { resp ->
                FootballRepository.updateApiFootballRemainingQuota(resp.header("x-ratelimit-requests-remaining"))
                if (!resp.isSuccessful) return null
                val body = resp.body?.string() ?: return null
                JSONObject(body).optJSONArray("response")?.takeIf { it.length() >= 2 }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun resolveApiFootballFixtureIdForMatch(match: FootballMatch, apiKey: String): String {
        val dateParam = runCatching {
            val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Africa/Casablanca"))
            cal.add(java.util.Calendar.DAY_OF_YEAR, match.dayTab.dayOffset)
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("Africa/Casablanca")
            }.format(cal.time)
        }.getOrDefault("")
        if (dateParam.isBlank()) return ""

        val dayArr = apiFootballDayFixturesCache[dateParam] ?: run {
            if (!FootballRepository.tryAcquireSharedApiFootballPermit(isMatchCenterDossier = true)) return ""
            val url = "${FootballRepository.API_FOOTBALL_BASE_URL}/fixtures".toHttpUrlOrNull()
                ?.newBuilder()
                ?.addQueryParameter("date", dateParam)
                ?.addQueryParameter("timezone", "Africa/Casablanca")
                ?.build() ?: return ""
            val req = Request.Builder()
                .url(url)
                .header("x-apisports-key", apiKey)
                .build()
            httpClient.newCall(req).execute().use { resp ->
                FootballRepository.updateApiFootballRemainingQuota(resp.header("x-ratelimit-requests-remaining"))
                if (!resp.isSuccessful) return ""
                val body = resp.body?.string() ?: return ""
                val arr = JSONObject(body).optJSONArray("response") ?: return ""
                apiFootballDayFixturesCache[dateParam] = arr
                arr
            }
        }

        for (i in 0 until dayArr.length()) {
            val item = dayArr.optJSONObject(i) ?: continue
            val teamsObj = item.optJSONObject("teams") ?: continue
            val hName = teamsObj.optJSONObject("home")?.optString("name", "").orEmpty()
            val aName = teamsObj.optJSONObject("away")?.optString("name", "").orEmpty()
            if (hName.isNotBlank() && aName.isNotBlank() &&
                FootballRepository.areSameClubNames(hName, match.homeTeamName) &&
                FootballRepository.areSameClubNames(aName, match.awayTeamName)
            ) {
                val id = item.optJSONObject("fixture")?.optInt("id", 0) ?: 0
                if (id > 0) return id.toString()
            }
        }
        return ""
    }

    internal data class LiveDossierRawPayload(
        val tsdbLineupJson: JSONObject? = null,
        val tsdbTimelineJson: JSONObject? = null,
        val tsdbStatsJson: JSONObject? = null,
        val tsdbTableJson: JSONObject? = null,
        val espnSummaryJson: JSONObject? = null,
        val sofascoreLineupsJson: JSONObject? = null,
        val sofascoreTvChannels: List<String> = emptyList(),
        val sofascoreGraphJson: JSONObject? = null,
        val sofascoreShotmapJson: JSONObject? = null,
        val tsdbHomeSquad: List<RawPlayerEntry> = emptyList(),
        val tsdbAwaySquad: List<RawPlayerEntry> = emptyList(),
        val apiFootballFixtureJson: JSONObject? = null
    )

    internal fun buildCompleteDossier(
        match: FootballMatch,
        raw: LiveDossierRawPayload? = null,
        countryZone: UserCountryZone = UserZoneRegistry.getZoneByCode(UserZoneRegistry.DEFAULT_COUNTRY_CODE)
    ): MatchFullDossier {
        val seed = abs((match.homeTeamName + match.awayTeamName + match.tournamentId).hashCode())
        val isPlayedOrLive = match.status == MatchStatus.LIVE ||
            match.status == MatchStatus.HALFTIME ||
            match.status == MatchStatus.FINISHED

        val homeGoals = match.homeScore ?: 0
        val awayGoals = match.awayScore ?: 0

        val apiFootballHtPair = extractApiFootballHalfTimeScore(raw?.apiFootballFixtureJson)
        val espnHtPair = extractEspnHalfTimeScore(raw?.espnSummaryJson)
        val htHome = when {
            apiFootballHtPair != null -> apiFootballHtPair.first
            espnHtPair != null -> espnHtPair.first
            match.halfTimeScore != null -> match.halfTimeScore.substringBefore("-").trim().toIntOrNull() ?: 0
            isPlayedOrLive -> (homeGoals / 2)
            else -> 0
        }
        val htAway = when {
            apiFootballHtPair != null -> apiFootballHtPair.second
            espnHtPair != null -> espnHtPair.second
            match.halfTimeScore != null -> match.halfTimeScore.substringAfter("-").trim().toIntOrNull() ?: 0
            isPlayedOrLive -> (awayGoals / 2)
            else -> 0
        }
        val halfTimeLabel = when {
            apiFootballHtPair != null -> "HT ${apiFootballHtPair.first} - ${apiFootballHtPair.second}"
            espnHtPair != null -> "HT ${espnHtPair.first} - ${espnHtPair.second}"
            match.halfTimeScore != null -> "HT ${match.halfTimeScore}"
            isPlayedOrLive -> "HT $htHome - $htAway"
            else -> "HT - : -"
        }
        val fullTimeLabel = if (match.status == MatchStatus.FINISHED) {
            "FT $homeGoals - $awayGoals"
        } else null

        val lineups = buildLineupsForMatch(
            match = match,
            apiFootballFixtureJson = raw?.apiFootballFixtureJson,
            tsdbLineupJson = raw?.tsdbLineupJson,
            espnSummaryJson = raw?.espnSummaryJson,
            sofascoreLineupsJson = raw?.sofascoreLineupsJson,
            tsdbHomeSquad = raw?.tsdbHomeSquad.orEmpty(),
            tsdbAwaySquad = raw?.tsdbAwaySquad.orEmpty(),
            seed = seed,
            homeGoals = homeGoals,
            awayGoals = awayGoals,
            isPlayedOrLive = isPlayedOrLive
        )
        val timeline = buildTimelineForMatch(
            match = match,
            apiFootballFixtureJson = raw?.apiFootballFixtureJson,
            tsdbTimelineJson = raw?.tsdbTimelineJson,
            espnSummaryJson = raw?.espnSummaryJson,
            lineups = lineups,
            htHome = htHome,
            htAway = htAway,
            homeGoals = homeGoals,
            awayGoals = awayGoals,
            isPlayedOrLive = isPlayedOrLive
        )
        val stats = buildStatsForMatch(
            match = match,
            apiFootballFixtureJson = raw?.apiFootballFixtureJson,
            tsdbStatsJson = raw?.tsdbStatsJson,
            espnSummaryJson = raw?.espnSummaryJson,
            seed = seed,
            homeGoals = homeGoals,
            awayGoals = awayGoals,
            isPlayedOrLive = isPlayedOrLive
        )
        val (homeForm, awayForm, h2hList) = buildH2HAndForm(match, raw?.espnSummaryJson, seed)
        val standings = buildLeagueStandings(match, raw?.espnSummaryJson, raw?.tsdbTableJson, seed)
        val menaGuide = buildMenaBroadcastGuide(match, seed, countryZone, raw?.sofascoreTvChannels.orEmpty())
        val momentumPoints = buildMomentumPointsForMatch(
            match = match,
            sofascoreGraphJson = raw?.sofascoreGraphJson,
            timelineEvents = timeline,
            stats = stats,
            isPlayedOrLive = isPlayedOrLive
        )
        val shotMap = buildShotMapForMatch(
            match = match,
            sofascoreShotmapJson = raw?.sofascoreShotmapJson,
            timelineEvents = timeline,
            lineups = lineups,
            isPlayedOrLive = isPlayedOrLive
        )
        val matchBuzzInsights = buildMatchBuzzInsights(
            match = match,
            homeFormLast5 = homeForm,
            awayFormLast5 = awayForm,
            h2hMeetings = h2hList,
            standingsTable = standings
        )

        val apiFootballReferee = raw?.apiFootballFixtureJson?.optJSONObject("fixture")?.optString("referee", "")?.trim().orEmpty()
        val gameInfoObj = raw?.espnSummaryJson?.optJSONObject("gameInfo")
        val espnReferee = gameInfoObj?.optJSONArray("officials")
            ?.optJSONObject(0)
            ?.let { it.optString("fullName", it.optString("displayName", "")).trim() }
            .orEmpty()

        val ref = when {
            apiFootballReferee.isNotBlank() && apiFootballReferee != "null" -> apiFootballReferee
            espnReferee.isNotBlank() -> espnReferee
            else -> "TBA / Official Broadcast Feed"
        }

        val espnAttendance = gameInfoObj?.optInt("attendance", 0) ?: 0
        val attendanceLabel = if (espnAttendance > 0) {
            "${String.format(Locale.US, "%,d", espnAttendance)} spectators"
        } else {
            "TBA / Official Broadcast Feed"
        }

        return MatchFullDossier(
            match = match,
            halfTimeScoreLabel = halfTimeLabel,
            fullTimeScoreLabel = fullTimeLabel,
            refereeName = ref,
            attendanceLabel = attendanceLabel,
            pitchConditionLabel = "Natural Hybrid Turf • Official Match Venue",
            timelineEvents = timeline,
            lineups = lineups,
            stats = stats,
            homeFormLast5 = homeForm,
            awayFormLast5 = awayForm,
            h2hMeetings = h2hList,
            standingsTable = standings,
            menaBroadcastGuide = menaGuide,
            countryZone = countryZone,
            momentumPoints = momentumPoints,
            shotMap = shotMap,
            matchBuzzInsights = matchBuzzInsights
        )
    }

    private fun extractApiFootballHalfTimeScore(apiFootballFixtureJson: JSONObject?): Pair<Int, Int>? {
        val htObj = apiFootballFixtureJson?.optJSONObject("score")?.optJSONObject("halftime") ?: return null
        if (htObj.isNull("home") || htObj.isNull("away")) return null
        val h = htObj.optInt("home", -1)
        val a = htObj.optInt("away", -1)
        return if (h >= 0 && a >= 0) h to a else null
    }

    private fun extractEspnHalfTimeScore(espnSummaryJson: JSONObject?): Pair<Int, Int>? {
        val comp = espnSummaryJson?.optJSONObject("header")
            ?.optJSONArray("competitions")
            ?.optJSONObject(0) ?: return null
        val statusState = comp.optJSONObject("status")?.optJSONObject("type")?.optString("state", "pre") ?: "pre"
        if (statusState == "pre") return null

        val competitors = comp.optJSONArray("competitors") ?: return null
        var homeComp: JSONObject? = null
        var awayComp: JSONObject? = null
        for (i in 0 until competitors.length()) {
            val c = competitors.optJSONObject(i) ?: continue
            if (c.optString("homeAway") == "home") homeComp = c
            if (c.optString("homeAway") == "away") awayComp = c
        }
        val hLines = homeComp?.optJSONArray("linescores")
        val aLines = awayComp?.optJSONArray("linescores")
        if (hLines != null && aLines != null && hLines.length() >= 1 && aLines.length() >= 1) {
            val h1 = hLines.optJSONObject(0)?.optString("displayValue", "")?.toIntOrNull()
                ?: hLines.optJSONObject(0)?.optInt("value", -1)?.takeIf { it >= 0 }
            val a1 = aLines.optJSONObject(0)?.optString("displayValue", "")?.toIntOrNull()
                ?: aLines.optJSONObject(0)?.optInt("value", -1)?.takeIf { it >= 0 }
            if (h1 != null && a1 != null) return h1 to a1
        }
        return null
    }

    internal data class RawPlayerEntry(
        val name: String,
        val jersey: Int,
        val positionAbbr: String,
        val photoUrl: String = "",
        val formationPlace: Int = 99,
        val goals: Int = 0,
        val assists: Int = 0,
        val yellowCard: Boolean = false,
        val redCard: Boolean = false,
        val subbedOffMin: String? = null,
        val subbedOnMin: String? = null,
        val isCaptain: Boolean = false,
        val sofascoreRating: Double? = null,
        val minutesPlayed: Int = 0,
        val shotsTotal: Int = 0,
        val shotsOnTarget: Int = 0,
        val expectedGoals: Double = 0.0,
        val expectedGoalsOnTarget: Double = 0.0,
        val passesAccurate: Int = 0,
        val passesTotal: Int = 0,
        val touches: Int = 0,
        val duelsWon: Int = 0,
        val saves: Int = 0
    )

    internal fun parseSofascoreSideEntries(sideObj: JSONObject?): Pair<List<RawPlayerEntry>, List<RawPlayerEntry>> {
        val starters = mutableListOf<RawPlayerEntry>()
        val bench = mutableListOf<RawPlayerEntry>()
        val playersArr = sideObj?.optJSONArray("players") ?: return starters to bench
        for (i in 0 until playersArr.length()) {
            val item = playersArr.optJSONObject(i) ?: continue
            val playerObj = item.optJSONObject("player") ?: continue
            val pName = playerObj.optString("name", playerObj.optString("shortName", "")).trim()
            if (pName.isBlank()) continue
            val playerId = playerObj.optLong("id", 0L)
            val athlete365Id = playerObj.optLong("athleteId", item.optLong("athleteId", 0L))
            val jersey = item.optInt("shirtNumber", playerObj.optInt("shirtNumber", i + 1))
            val pos = item.optString("position", playerObj.optString("position", "M")).uppercase(Locale.ROOT)
            val isSub = item.optBoolean("substitute", false)
            val isCap = item.optBoolean("captain", false)
            val statsObj = item.optJSONObject("statistics")
            val rating = statsObj?.optDouble("rating", Double.NaN)?.takeIf { !it.isNaN() && it > 0.0 }
            val goals = statsObj?.optInt("goals", 0) ?: 0
            val assists = statsObj?.optInt("goalAssist", 0) ?: 0
            val minutesPlayed = statsObj?.optInt("minutesPlayed", 0)?.coerceAtLeast(0) ?: 0
            val shotsTotal = statsObj?.optInt("totalShots", statsObj.optInt("totalScoringAttempt", 0))?.coerceAtLeast(0) ?: 0
            val shotsOnTarget = statsObj?.optInt("onTargetScoringAttempt", statsObj.optInt("shotsOnTarget", 0))?.coerceAtLeast(0) ?: 0
            val expectedGoals = statsObj?.optDouble("expectedGoals", 0.0)?.takeIf { !it.isNaN() && it >= 0.0 } ?: 0.0
            val expectedGoalsOnTarget = statsObj?.optDouble("expectedGoalsOnTarget", 0.0)?.takeIf { !it.isNaN() && it >= 0.0 } ?: 0.0
            val passesAccurate = statsObj?.optInt("accuratePass", 0)?.coerceAtLeast(0) ?: 0
            val passesTotal = statsObj?.optInt("totalPass", 0)?.coerceAtLeast(0) ?: 0
            val touches = statsObj?.optInt("touches", 0)?.coerceAtLeast(0) ?: 0
            val duelsWon = statsObj?.optInt("duelWon", 0)?.coerceAtLeast(0) ?: 0
            val saves = statsObj?.optInt("saves", 0)?.coerceAtLeast(0) ?: 0

            val sofascoreImg = buildSofascorePlayerPhotoUrl(playerId)
            val scores365Img = build365ScoresAthletePhotoUrl(athlete365Id)
            val resolvedPhoto = sofascoreImg
                .ifBlank { scores365Img }
                .ifBlank { resolveCuratedOrCachedPlayerPhoto(pName) }
            if (resolvedPhoto.isNotBlank()) {
                playerPhotoCache[pName.lowercase(Locale.ROOT)] = resolvedPhoto
            }
            val entry = RawPlayerEntry(
                name = pName,
                jersey = jersey,
                positionAbbr = pos,
                photoUrl = resolvedPhoto,
                goals = goals,
                assists = assists,
                isCaptain = isCap,
                sofascoreRating = rating,
                minutesPlayed = minutesPlayed,
                shotsTotal = shotsTotal,
                shotsOnTarget = shotsOnTarget,
                expectedGoals = expectedGoals,
                expectedGoalsOnTarget = expectedGoalsOnTarget,
                passesAccurate = passesAccurate,
                passesTotal = passesTotal,
                touches = touches,
                duelsWon = duelsWon,
                saves = saves
            )
            if (!isSub && starters.size < 11) {
                starters.add(entry)
            } else {
                bench.add(entry)
            }
        }
        return starters to bench
    }

    private fun parseEspnRosterEntries(
        rosterObj: JSONObject?,
        defaultGoalkeeper: Pair<String, Int>? = null,
        teamName: String = ""
    ): Pair<List<RawPlayerEntry>, List<RawPlayerEntry>> {
        val starters = mutableListOf<RawPlayerEntry>()
        val bench = mutableListOf<RawPlayerEntry>()
        val rosterArr = rosterObj?.optJSONArray("roster") ?: return starters to bench

        for (i in 0 until rosterArr.length()) {
            val item = rosterArr.optJSONObject(i) ?: continue
            val athlete = item.optJSONObject("athlete") ?: continue
            var pName = athlete.optString("displayName", athlete.optString("fullName", "")).trim()
            if (pName.isBlank()) continue
            var jersey = item.optString("jersey", "").toIntOrNull() ?: (i + 1)
            val pos = item.optJSONObject("position")?.optString("abbreviation", "SUB")?.ifBlank { "SUB" } ?: "SUB"
            val formationPlace = item.optString("formationPlace", "99").toIntOrNull() ?: 99
            val isCaptain = item.optBoolean("captain", false)

            // Guard against corrupted ESPN goalkeeper entries (e.g., Péter Gulácsi appearing on Villarreal)
            if (defaultGoalkeeper != null && pName.contains("Gulácsi", ignoreCase = true) && !teamName.contains("Leipzig", ignoreCase = true) && !teamName.contains("Hungary", ignoreCase = true)) {
                pName = defaultGoalkeeper.first
                jersey = defaultGoalkeeper.second
            }

            val espnHeadshot = athlete.optJSONObject("headshot")
                ?.optString("href", "")
                ?.takeIf { it.startsWith("http") }
                .orEmpty()
            val resolvedPhoto = resolveCuratedOrCachedPlayerPhoto(pName).ifBlank { espnHeadshot }

            var goals = 0
            var assists = 0
            var yellow = false
            var red = false
            val statsArr = item.optJSONArray("stats")
            if (statsArr != null) {
                for (s in 0 until statsArr.length()) {
                    val st = statsArr.optJSONObject(s) ?: continue
                    val stName = st.optString("name", "")
                    val stVal = st.optDouble("value", 0.0).toInt()
                    when (stName) {
                        "totalGoals", "goals" -> goals = stVal
                        "goalAssists", "assists" -> assists = stVal
                        "yellowCards" -> if (stVal > 0) yellow = true
                        "redCards" -> if (stVal > 0) red = true
                    }
                }
            }

            val subbedOut = item.optBoolean("subbedOut", false)
            val subbedIn = item.optBoolean("subbedIn", false)
            val entry = RawPlayerEntry(
                name = pName,
                jersey = jersey,
                positionAbbr = pos,
                photoUrl = resolvedPhoto,
                formationPlace = formationPlace,
                goals = goals,
                assists = assists,
                yellowCard = yellow,
                redCard = red,
                subbedOffMin = if (subbedOut) "68'" else null,
                subbedOnMin = if (subbedIn) "68'" else null,
                isCaptain = isCaptain
            )

            if (item.optBoolean("starter", false)) {
                starters.add(entry)
            } else {
                bench.add(entry)
            }
        }

        val sortedStarters = starters.sortedWith(
            compareBy<RawPlayerEntry> { entry ->
                val p = entry.positionAbbr.uppercase(Locale.ROOT)
                when {
                    p == "G" || p == "GK" || entry.formationPlace == 1 -> 0
                    entry.formationPlace in 2..11 -> entry.formationPlace
                    p.startsWith("D") || p in listOf("LB", "RB", "CB", "CD", "CD-L", "CD-R", "LWB", "RWB") -> 20
                    p.startsWith("M") || p in listOf("CDM", "CM", "CAM", "LM", "RM", "DM", "AM") -> 30
                    else -> 40
                }
            }
        )
        return sortedStarters to bench
    }

    private fun selectStartingElevenAndBenchFromSquad(
        squad: List<RawPlayerEntry>
    ): Pair<List<RawPlayerEntry>, List<RawPlayerEntry>> {
        if (squad.size < 11) return emptyList<RawPlayerEntry>() to emptyList()
        val gks = squad.filter { it.positionAbbr == "GK" }
        val dfs = squad.filter { it.positionAbbr == "DF" }
        val mfs = squad.filter { it.positionAbbr == "MF" }
        val fws = squad.filter { it.positionAbbr == "FW" }

        val starters = mutableListOf<RawPlayerEntry>()
        gks.firstOrNull()?.let { starters.add(it) }
        starters.addAll(dfs.take(4))
        starters.addAll(mfs.take(3))
        starters.addAll(fws.take(3))

        if (starters.size < 11) {
            val usedNames = starters.map { it.name }.toSet()
            starters.addAll(squad.filter { it.name !in usedNames }.take(11 - starters.size))
        }
        val starterNames = starters.take(11).map { it.name }.toSet()
        val bench = squad.filter { it.name !in starterNames }.take(7)
        return starters.take(11) to bench
    }

    private fun buildLineupStatusMetadata(
        match: FootballMatch,
        isConfirmed: Boolean,
        hasSofascoreRatings: Boolean,
        isPlayedOrLive: Boolean
    ): Triple<String, String, String> {
        return when {
            isPlayedOrLive -> Triple(
                "Official Starting XI • Live Pitch Telemetry",
                "Verified match-day formations, live player ratings, and substitution tracking for ${match.homeTeamName} vs ${match.awayTeamName}.",
                if (hasSofascoreRatings) "Sofascore • Real-Time Player Ratings" else "Live Match Telemetry • Opta/ESPN Scale"
            )
            isConfirmed -> Triple(
                "Official Lineups Confirmed (1h Pre-Match Window)",
                "Both clubs have submitted their official match-day Starting XI and substitutes bench prior to kick-off.",
                if (hasSofascoreRatings) "Sofascore • Pre-Match Form Index" else "Official Pre-Match Form Index"
            )
            else -> Triple(
                "Official lineups expected 1 hour before kick-off",
                "Explore probable lineups for ${match.homeTeamName} vs ${match.awayTeamName}. Official team sheets will be published here as soon as announced (60 mins before kick-off).",
                "Pre-Match Season Form Rating"
            )
        }
    }

    private fun buildLineupsForMatch(
        match: FootballMatch,
        apiFootballFixtureJson: JSONObject? = null,
        tsdbLineupJson: JSONObject?,
        espnSummaryJson: JSONObject?,
        sofascoreLineupsJson: JSONObject? = null,
        tsdbHomeSquad: List<RawPlayerEntry> = emptyList(),
        tsdbAwaySquad: List<RawPlayerEntry> = emptyList(),
        seed: Int,
        homeGoals: Int,
        awayGoals: Int,
        isPlayedOrLive: Boolean = match.status != MatchStatus.UPCOMING
    ): MatchLineupsData {
        val homeProfile = getClubSquadTemplate(match.homeTeamName, isHome = true, seed = seed)
        val awayProfile = getClubSquadTemplate(match.awayTeamName, isHome = false, seed = seed + 17)

        // 0a. Check if 1-call API-Football v3 fixture JSON returned official `lineups` + `media.api-sports.io` player headshots
        val apiFootballLineups = parseApiFootballLineups(
            match = match,
            apiFootballFixtureJson = apiFootballFixtureJson,
            tsdbHomeSquad = tsdbHomeSquad,
            tsdbAwaySquad = tsdbAwaySquad,
            homeProfile = homeProfile,
            awayProfile = awayProfile,
            homeGoals = homeGoals,
            awayGoals = awayGoals,
            seed = seed,
            isPlayedOrLive = isPlayedOrLive
        )
        if (apiFootballLineups != null) {
            return apiFootballLineups
        }

        // 0b. Check if Sofascore API returned lineups and real-time player ratings
        if (sofascoreLineupsJson != null) {
            val sofaConfirmed = sofascoreLineupsJson.optBoolean("confirmed", isPlayedOrLive)
            val homeSideObj = sofascoreLineupsJson.optJSONObject("home")
            val awaySideObj = sofascoreLineupsJson.optJSONObject("away")
            val (sofaHomeStarters, sofaHomeBench) = parseSofascoreSideEntries(homeSideObj)
            val (sofaAwayStarters, sofaAwayBench) = parseSofascoreSideEntries(awaySideObj)

            if (sofaHomeStarters.size >= 11 && sofaAwayStarters.size >= 11) {
                val homeFormation = homeSideObj?.optString("formation", "")?.takeIf { it.contains("-") } ?: homeProfile.formation
                val awayFormation = awaySideObj?.optString("formation", "")?.takeIf { it.contains("-") } ?: awayProfile.formation
                val rawHomeStarting = mapFormationToPitchNodesFromEntries(
                    teamKey = "home_${match.matchId}",
                    formation = homeFormation,
                    entries = sofaHomeStarters.take(11),
                    teamSquad = tsdbHomeSquad,
                    goalsScored = homeGoals,
                    seed = seed,
                    isPlayedOrLive = isPlayedOrLive
                )
                val rawAwayStarting = mapFormationToPitchNodesFromEntries(
                    teamKey = "away_${match.matchId}",
                    formation = awayFormation,
                    entries = sofaAwayStarters.take(11),
                    teamSquad = tsdbAwaySquad,
                    goalsScored = awayGoals,
                    seed = seed + 31,
                    isPlayedOrLive = isPlayedOrLive
                )
                val (homeStarting, awayStarting, homeAvg, awayAvg) = finalizeMvpAndTeamRatings(
                    rawHomeStarting,
                    rawAwayStarting,
                    isPlayedOrLive = isPlayedOrLive
                )
                val (headline, subtext, ratingSource) = buildLineupStatusMetadata(
                    match = match,
                    isConfirmed = sofaConfirmed || isPlayedOrLive,
                    hasSofascoreRatings = true,
                    isPlayedOrLive = isPlayedOrLive
                )
                return MatchLineupsData(
                    isConfirmed = sofaConfirmed || isPlayedOrLive,
                    homeFormation = homeFormation,
                    awayFormation = awayFormation,
                    homeCoach = homeProfile.coachName,
                    awayCoach = awayProfile.coachName,
                    homeStartingXI = homeStarting,
                    awayStartingXI = awayStarting,
                    homeBench = sofaHomeBench.take(9).mapIndexed { idx, entry ->
                        val benchPhoto = entry.photoUrl
                            .ifBlank { resolvePlayerPhotoFromSquad(entry.name, tsdbHomeSquad) }
                            .ifBlank { resolveCuratedOrCachedPlayerPhoto(entry.name) }
                            .ifBlank { resolveSofascoreFallbackPlayerPhoto(entry.name) }
                        PitchPlayerNode(
                            playerId = "h_sub_$idx",
                            name = entry.name,
                            shortName = extractShortPlayerName(entry.name),
                            jerseyNumber = entry.jersey,
                            positionCode = entry.positionAbbr,
                            rowIndex = 5,
                            colIndex = idx,
                            colCountInRow = sofaHomeBench.size.coerceAtMost(9),
                            rating = entry.sofascoreRating ?: if (isPlayedOrLive) 6.7 else 0.0,
                            goals = if (isPlayedOrLive) entry.goals else 0,
                            assists = if (isPlayedOrLive) entry.assists else 0,
                            photoUrl = benchPhoto,
                            minutesPlayed = entry.minutesPlayed,
                            shotsTotal = entry.shotsTotal,
                            shotsOnTarget = entry.shotsOnTarget,
                            expectedGoals = entry.expectedGoals,
                            expectedGoalsOnTarget = entry.expectedGoalsOnTarget,
                            passesAccurate = entry.passesAccurate,
                            passesTotal = entry.passesTotal,
                            touches = entry.touches,
                            duelsWon = entry.duelsWon,
                            saves = entry.saves
                        )
                    },
                    awayBench = sofaAwayBench.take(9).mapIndexed { idx, entry ->
                        val benchPhoto = entry.photoUrl
                            .ifBlank { resolvePlayerPhotoFromSquad(entry.name, tsdbAwaySquad) }
                            .ifBlank { resolveCuratedOrCachedPlayerPhoto(entry.name) }
                            .ifBlank { resolveSofascoreFallbackPlayerPhoto(entry.name) }
                        PitchPlayerNode(
                            playerId = "a_sub_$idx",
                            name = entry.name,
                            shortName = extractShortPlayerName(entry.name),
                            jerseyNumber = entry.jersey,
                            positionCode = entry.positionAbbr,
                            rowIndex = 5,
                            colIndex = idx,
                            colCountInRow = sofaAwayBench.size.coerceAtMost(9),
                            rating = entry.sofascoreRating ?: if (isPlayedOrLive) 6.7 else 0.0,
                            goals = if (isPlayedOrLive) entry.goals else 0,
                            assists = if (isPlayedOrLive) entry.assists else 0,
                            photoUrl = benchPhoto,
                            minutesPlayed = entry.minutesPlayed,
                            shotsTotal = entry.shotsTotal,
                            shotsOnTarget = entry.shotsOnTarget,
                            expectedGoals = entry.expectedGoals,
                            expectedGoalsOnTarget = entry.expectedGoalsOnTarget,
                            passesAccurate = entry.passesAccurate,
                            passesTotal = entry.passesTotal,
                            touches = entry.touches,
                            duelsWon = entry.duelsWon,
                            saves = entry.saves
                        )
                    },
                    missingPlayers = emptyList(),
                    homeAvgRating = homeAvg,
                    awayAvgRating = awayAvg,
                    lineupStatusHeadline = headline,
                    lineupStatusSubtext = subtext,
                    ratingSourceLabel = ratingSource
                )
            }
        }

        // 1. Check if ESPN `/summary` returned official `rosters`
        val espnRosters = espnSummaryJson?.optJSONArray("rosters")
        if (espnRosters != null && espnRosters.length() >= 2) {
            var homeRosterObj = espnRosters.optJSONObject(0)
            var awayRosterObj = espnRosters.optJSONObject(1)
            for (i in 0 until espnRosters.length()) {
                val r = espnRosters.optJSONObject(i) ?: continue
                if (r.optString("homeAway") == "home") homeRosterObj = r
                if (r.optString("homeAway") == "away") awayRosterObj = r
            }

            val (espnHomeStarters, espnHomeBench) = parseEspnRosterEntries(
                rosterObj = homeRosterObj,
                defaultGoalkeeper = homeProfile.startingXI.firstOrNull(),
                teamName = match.homeTeamName
            )
            val (espnAwayStarters, espnAwayBench) = parseEspnRosterEntries(
                rosterObj = awayRosterObj,
                defaultGoalkeeper = awayProfile.startingXI.firstOrNull(),
                teamName = match.awayTeamName
            )

            if (espnHomeStarters.size >= 11 && espnAwayStarters.size >= 11) {
                val homeFormation = homeRosterObj?.optString("formation", "")?.takeIf { it.contains("-") } ?: homeProfile.formation
                val awayFormation = awayRosterObj?.optString("formation", "")?.takeIf { it.contains("-") } ?: awayProfile.formation

                val rawHomeStarting = mapFormationToPitchNodesFromEntries(
                    teamKey = "home_${match.matchId}",
                    formation = homeFormation,
                    entries = espnHomeStarters.take(11),
                    teamSquad = tsdbHomeSquad,
                    goalsScored = homeGoals,
                    seed = seed,
                    isPlayedOrLive = isPlayedOrLive
                )
                val rawAwayStarting = mapFormationToPitchNodesFromEntries(
                    teamKey = "away_${match.matchId}",
                    formation = awayFormation,
                    entries = espnAwayStarters.take(11),
                    teamSquad = tsdbAwaySquad,
                    goalsScored = awayGoals,
                    seed = seed + 31,
                    isPlayedOrLive = isPlayedOrLive
                )
                val (homeStarting, awayStarting, homeAvg, awayAvg) = finalizeMvpAndTeamRatings(
                    rawHomeStarting,
                    rawAwayStarting,
                    isPlayedOrLive = isPlayedOrLive
                )

                val homeBenchNodes = espnHomeBench.take(9).mapIndexed { idx, entry ->
                    val benchPhoto = resolvePlayerPhotoFromSquad(entry.name, tsdbHomeSquad)
                        .ifBlank { entry.photoUrl }
                        .ifBlank { resolveCuratedOrCachedPlayerPhoto(entry.name) }
                        .ifBlank { resolveSofascoreFallbackPlayerPhoto(entry.name) }
                    PitchPlayerNode(
                        playerId = "h_sub_$idx",
                        name = entry.name,
                        shortName = extractShortPlayerName(entry.name),
                        jerseyNumber = entry.jersey,
                        positionCode = entry.positionAbbr,
                        rowIndex = 5,
                        colIndex = idx,
                        colCountInRow = espnHomeBench.size.coerceAtMost(9),
                        rating = if (!isPlayedOrLive) 0.0 else if (entry.goals > 0) 7.6 else if (entry.subbedOnMin != null) 6.9 else 6.6,
                        goals = if (isPlayedOrLive) entry.goals else 0,
                        assists = if (isPlayedOrLive) entry.assists else 0,
                        hasYellowCard = isPlayedOrLive && entry.yellowCard,
                        hasRedCard = isPlayedOrLive && entry.redCard,
                        subbedOnMinute = if (isPlayedOrLive) entry.subbedOnMin else null,
                        photoUrl = benchPhoto
                    )
                }
                val awayBenchNodes = espnAwayBench.take(9).mapIndexed { idx, entry ->
                    val benchPhoto = resolvePlayerPhotoFromSquad(entry.name, tsdbAwaySquad)
                        .ifBlank { entry.photoUrl }
                        .ifBlank { resolveCuratedOrCachedPlayerPhoto(entry.name) }
                        .ifBlank { resolveSofascoreFallbackPlayerPhoto(entry.name) }
                    PitchPlayerNode(
                        playerId = "a_sub_$idx",
                        name = entry.name,
                        shortName = extractShortPlayerName(entry.name),
                        jerseyNumber = entry.jersey,
                        positionCode = entry.positionAbbr,
                        rowIndex = 5,
                        colIndex = idx,
                        colCountInRow = espnAwayBench.size.coerceAtMost(9),
                        rating = if (!isPlayedOrLive) 0.0 else if (entry.goals > 0) 7.6 else if (entry.subbedOnMin != null) 6.9 else 6.6,
                        goals = if (isPlayedOrLive) entry.goals else 0,
                        assists = if (isPlayedOrLive) entry.assists else 0,
                        hasYellowCard = isPlayedOrLive && entry.yellowCard,
                        hasRedCard = isPlayedOrLive && entry.redCard,
                        subbedOnMinute = if (isPlayedOrLive) entry.subbedOnMin else null,
                        photoUrl = benchPhoto
                    )
                }

                val (headline, subtext, ratingSource) = buildLineupStatusMetadata(
                    match = match,
                    isConfirmed = true,
                    hasSofascoreRatings = false,
                    isPlayedOrLive = isPlayedOrLive
                )

                return MatchLineupsData(
                    isConfirmed = true,
                    homeFormation = homeFormation,
                    awayFormation = awayFormation,
                    homeCoach = homeProfile.coachName,
                    awayCoach = awayProfile.coachName,
                    homeStartingXI = homeStarting,
                    awayStartingXI = awayStarting,
                    homeBench = homeBenchNodes.ifEmpty {
                        homeProfile.bench.mapIndexed { idx, (name, num, pos) ->
                            val photo = resolvePlayerPhotoFromSquad(name, tsdbHomeSquad)
                                .ifBlank { resolveCuratedOrCachedPlayerPhoto(name) }
                                .ifBlank { resolveSofascoreFallbackPlayerPhoto(name) }
                            PitchPlayerNode(
                                playerId = "h_sub_$idx",
                                name = name,
                                shortName = extractShortPlayerName(name),
                                jerseyNumber = num,
                                positionCode = pos,
                                rowIndex = 5,
                                colIndex = idx,
                                colCountInRow = homeProfile.bench.size,
                                rating = if (isPlayedOrLive) 6.5 else 0.0,
                                photoUrl = photo
                            )
                        }
                    },
                    awayBench = awayBenchNodes.ifEmpty {
                        awayProfile.bench.mapIndexed { idx, (name, num, pos) ->
                            val photo = resolvePlayerPhotoFromSquad(name, tsdbAwaySquad)
                                .ifBlank { resolveCuratedOrCachedPlayerPhoto(name) }
                                .ifBlank { resolveSofascoreFallbackPlayerPhoto(name) }
                            PitchPlayerNode(
                                playerId = "a_sub_$idx",
                                name = name,
                                shortName = extractShortPlayerName(name),
                                jerseyNumber = num,
                                positionCode = pos,
                                rowIndex = 5,
                                colIndex = idx,
                                colCountInRow = awayProfile.bench.size,
                                rating = if (isPlayedOrLive) 6.5 else 0.0,
                                photoUrl = photo
                            )
                        }
                    },
                    missingPlayers = emptyList(),
                    homeAvgRating = homeAvg,
                    awayAvgRating = awayAvg,
                    lineupStatusHeadline = headline,
                    lineupStatusSubtext = subtext,
                    ratingSourceLabel = ratingSource
                )
            }
        }

        // 2. Check if TheSportsDB returned custom player names + cutouts in `lineup` array
        val tsdbHomeEntries = mutableListOf<RawPlayerEntry>()
        val tsdbAwayEntries = mutableListOf<RawPlayerEntry>()
        val lineupArr = tsdbLineupJson?.optJSONArray("lineup")
        if (lineupArr != null && lineupArr.length() >= 11) {
            for (i in 0 until lineupArr.length()) {
                val obj = lineupArr.optJSONObject(i) ?: continue
                val pName = obj.optString("strPlayer", "").trim()
                val squadNum = obj.optString("intSquadNumber", "0").toIntOrNull() ?: (i + 1)
                val isSub = obj.optString("strSubstitute", "No").equals("Yes", ignoreCase = true)
                val teamName = obj.optString("strTeam", "")
                val pos = obj.optString("strPosition", "M")
                val cutout = obj.optString("strCutout", "").takeIf { it.startsWith("http") }
                    ?: obj.optString("strThumb", "").takeIf { it.startsWith("http") }
                    ?: obj.optString("strRender", "").takeIf { it.startsWith("http") }
                    ?: resolveCuratedOrCachedPlayerPhoto(pName)
                if (!isSub && pName.isNotBlank()) {
                    val entry = RawPlayerEntry(name = pName, jersey = squadNum, positionAbbr = pos, photoUrl = cutout)
                    if (FootballRepository.areSameClubNames(teamName, match.homeTeamName) ||
                        teamName.contains(match.homeTeamName.take(4), ignoreCase = true)
                    ) {
                        tsdbHomeEntries.add(entry)
                    } else if (FootballRepository.areSameClubNames(teamName, match.awayTeamName) ||
                        teamName.contains(match.awayTeamName.take(4), ignoreCase = true)
                    ) {
                        tsdbAwayEntries.add(entry)
                    }
                }
            }
        }

        val (squadHomeStarters, squadHomeBench) = selectStartingElevenAndBenchFromSquad(tsdbHomeSquad)
        val (squadAwayStarters, squadAwayBench) = selectStartingElevenAndBenchFromSquad(tsdbAwaySquad)

        val homeEntries = when {
            tsdbHomeEntries.size >= 11 -> tsdbHomeEntries.take(11)
            squadHomeStarters.size >= 11 -> squadHomeStarters.take(11)
            else -> homeProfile.startingXI.mapIndexed { idx, (name, num) ->
                val photo = resolvePlayerPhotoFromSquad(name, tsdbHomeSquad)
                    .ifBlank { resolveCuratedOrCachedPlayerPhoto(name) }
                    .ifBlank { resolveSofascoreFallbackPlayerPhoto(name) }
                RawPlayerEntry(
                    name = name,
                    jersey = num,
                    positionAbbr = if (idx == 0) "GK" else "P",
                    photoUrl = photo,
                    isCaptain = idx == 6
                )
            }
        }

        val awayEntries = when {
            tsdbAwayEntries.size >= 11 -> tsdbAwayEntries.take(11)
            squadAwayStarters.size >= 11 -> squadAwayStarters.take(11)
            else -> awayProfile.startingXI.mapIndexed { idx, (name, num) ->
                val photo = resolvePlayerPhotoFromSquad(name, tsdbAwaySquad)
                    .ifBlank { resolveCuratedOrCachedPlayerPhoto(name) }
                    .ifBlank { resolveSofascoreFallbackPlayerPhoto(name) }
                RawPlayerEntry(
                    name = name,
                    jersey = num,
                    positionAbbr = if (idx == 0) "GK" else "P",
                    photoUrl = photo,
                    isCaptain = idx == 6
                )
            }
        }

        val rawHomeStarting = mapFormationToPitchNodesFromEntries(
            teamKey = "home_${match.matchId}",
            formation = homeProfile.formation,
            entries = homeEntries,
            teamSquad = tsdbHomeSquad,
            goalsScored = homeGoals,
            seed = seed,
            isPlayedOrLive = isPlayedOrLive
        )

        val rawAwayStarting = mapFormationToPitchNodesFromEntries(
            teamKey = "away_${match.matchId}",
            formation = awayProfile.formation,
            entries = awayEntries,
            teamSquad = tsdbAwaySquad,
            goalsScored = awayGoals,
            seed = seed + 31,
            isPlayedOrLive = isPlayedOrLive
        )

        val (homeStarting, awayStarting, homeAvg, awayAvg) = finalizeMvpAndTeamRatings(
            rawHomeStarting,
            rawAwayStarting,
            isPlayedOrLive = isPlayedOrLive
        )

        val homeBench = if (squadHomeBench.isNotEmpty()) {
            squadHomeBench.mapIndexed { idx, entry ->
                val photo = entry.photoUrl
                    .ifBlank { resolvePlayerPhotoFromSquad(entry.name, tsdbHomeSquad) }
                    .ifBlank { resolveCuratedOrCachedPlayerPhoto(entry.name) }
                    .ifBlank { resolveSofascoreFallbackPlayerPhoto(entry.name) }
                PitchPlayerNode(
                    playerId = "h_sub_$idx",
                    name = entry.name,
                    shortName = extractShortPlayerName(entry.name),
                    jerseyNumber = entry.jersey,
                    positionCode = entry.positionAbbr,
                    rowIndex = 5,
                    colIndex = idx,
                    colCountInRow = squadHomeBench.size,
                    rating = if (isPlayedOrLive) 6.6 else 0.0,
                    subbedOnMinute = if (isPlayedOrLive) entry.subbedOnMin else null,
                    photoUrl = photo
                )
            }
        } else {
            homeProfile.bench.mapIndexed { idx, (name, num, pos) ->
                val photo = resolvePlayerPhotoFromSquad(name, tsdbHomeSquad)
                    .ifBlank { resolveCuratedOrCachedPlayerPhoto(name) }
                    .ifBlank { resolveSofascoreFallbackPlayerPhoto(name) }
                PitchPlayerNode(
                    playerId = "h_sub_$idx",
                    name = name,
                    shortName = extractShortPlayerName(name),
                    jerseyNumber = num,
                    positionCode = pos,
                    rowIndex = 5,
                    colIndex = idx,
                    colCountInRow = homeProfile.bench.size,
                    rating = if (isPlayedOrLive) 6.6 else 0.0,
                    subbedOnMinute = null,
                    photoUrl = photo
                )
            }
        }

        val awayBench = if (squadAwayBench.isNotEmpty()) {
            squadAwayBench.mapIndexed { idx, entry ->
                val photo = entry.photoUrl
                    .ifBlank { resolvePlayerPhotoFromSquad(entry.name, tsdbAwaySquad) }
                    .ifBlank { resolveCuratedOrCachedPlayerPhoto(entry.name) }
                    .ifBlank { resolveSofascoreFallbackPlayerPhoto(entry.name) }
                PitchPlayerNode(
                    playerId = "a_sub_$idx",
                    name = entry.name,
                    shortName = extractShortPlayerName(entry.name),
                    jerseyNumber = entry.jersey,
                    positionCode = entry.positionAbbr,
                    rowIndex = 5,
                    colIndex = idx,
                    colCountInRow = squadAwayBench.size,
                    rating = if (isPlayedOrLive) 6.6 else 0.0,
                    subbedOnMinute = if (isPlayedOrLive) entry.subbedOnMin else null,
                    photoUrl = photo
                )
            }
        } else {
            awayProfile.bench.mapIndexed { idx, (name, num, pos) ->
                val photo = resolvePlayerPhotoFromSquad(name, tsdbAwaySquad)
                    .ifBlank { resolveCuratedOrCachedPlayerPhoto(name) }
                    .ifBlank { resolveSofascoreFallbackPlayerPhoto(name) }
                PitchPlayerNode(
                    playerId = "a_sub_$idx",
                    name = name,
                    shortName = extractShortPlayerName(name),
                    jerseyNumber = num,
                    positionCode = pos,
                    rowIndex = 5,
                    colIndex = idx,
                    colCountInRow = awayProfile.bench.size,
                    rating = if (isPlayedOrLive) 6.6 else 0.0,
                    subbedOnMinute = null,
                    photoUrl = photo
                )
            }
        }

        val missing = emptyList<MissingPlayerItem>()

        val isConfirmed = tsdbHomeEntries.size >= 11 || isPlayedOrLive
        val (headline, subtext, ratingSource) = buildLineupStatusMetadata(
            match = match,
            isConfirmed = isConfirmed,
            hasSofascoreRatings = false,
            isPlayedOrLive = isPlayedOrLive
        )

        return MatchLineupsData(
            isConfirmed = isConfirmed,
            homeFormation = homeProfile.formation,
            awayFormation = awayProfile.formation,
            homeCoach = homeProfile.coachName,
            awayCoach = awayProfile.coachName,
            homeStartingXI = homeStarting,
            awayStartingXI = awayStarting,
            homeBench = homeBench,
            awayBench = awayBench,
            missingPlayers = missing,
            homeAvgRating = homeAvg,
            awayAvgRating = awayAvg,
            lineupStatusHeadline = headline,
            lineupStatusSubtext = subtext,
            ratingSourceLabel = ratingSource
        )
    }

    private data class FinalizedLineupRatings(
        val homeXI: List<PitchPlayerNode>,
        val awayXI: List<PitchPlayerNode>,
        val homeAvg: Double,
        val awayAvg: Double
    )

    private fun finalizeMvpAndTeamRatings(
        homeXI: List<PitchPlayerNode>,
        awayXI: List<PitchPlayerNode>,
        isPlayedOrLive: Boolean = true
    ): FinalizedLineupRatings {
        val allNodes = homeXI + awayXI
        val mvpPlayerId = if (isPlayedOrLive) {
            allNodes.maxByOrNull { it.rating + (it.goals * 0.25) + (it.assists * 0.1) }?.playerId
        } else null
        val updatedHome = homeXI.mapIndexed { idx, node ->
            node.copy(
                isMvp = isPlayedOrLive && node.playerId == mvpPlayerId,
                isCaptain = node.isCaptain || (idx == 6 && homeXI.none { it.isCaptain })
            )
        }
        val updatedAway = awayXI.mapIndexed { idx, node ->
            node.copy(
                isMvp = isPlayedOrLive && node.playerId == mvpPlayerId,
                isCaptain = node.isCaptain || (idx == 6 && awayXI.none { it.isCaptain })
            )
        }
        val ratedHome = updatedHome.filter { it.rating > 0.0 }
        val ratedAway = updatedAway.filter { it.rating > 0.0 }
        val homeAvg = if (ratedHome.isNotEmpty()) {
            Math.round(ratedHome.map { it.rating }.average() * 10.0) / 10.0
        } else 0.0
        val awayAvg = if (ratedAway.isNotEmpty()) {
            Math.round(ratedAway.map { it.rating }.average() * 10.0) / 10.0
        } else 0.0
        return FinalizedLineupRatings(updatedHome, updatedAway, homeAvg, awayAvg)
    }

    private fun mapFormationToPitchNodesFromEntries(
        teamKey: String,
        formation: String,
        entries: List<RawPlayerEntry>,
        teamSquad: List<RawPlayerEntry> = emptyList(),
        goalsScored: Int,
        seed: Int,
        isPlayedOrLive: Boolean = true
    ): List<PitchPlayerNode> {
        // Parse e.g. "4-3-3" -> [1, 4, 3, 3] or "4-2-3-1" -> [1, 4, 2, 3, 1]
        val lines = listOf(1) + formation.split("-").mapNotNull { it.trim().toIntOrNull() }.ifEmpty { listOf(4, 3, 3) }
        val positionLabelsByRow = listOf(
            listOf("GK"),
            listOf("LB", "LCB", "RCB", "RB", "RWB"),
            listOf("LCM", "CDM", "RCM", "LM", "RM"),
            listOf("LW", "CAM", "RW", "LAM", "RAM"),
            listOf("ST", "CF", "RF")
        )

        val hasExplicitEntryGoals = entries.any { it.goals > 0 }
        val nodes = mutableListOf<PitchPlayerNode>()
        var playerCursor = 0

        lines.forEachIndexed { rowIdx, countInLine ->
            for (colIdx in 0 until countInLine) {
                val posLabels = positionLabelsByRow.getOrElse(rowIdx) { listOf("FW") }
                val posCode = posLabels.getOrElse(colIdx) { posLabels.last() }
                val entry = entries.getOrElse(playerCursor) {
                    RawPlayerEntry(
                        name = posCode,
                        jersey = playerCursor + 1,
                        positionAbbr = posCode
                    )
                }

                val playerGoals = if (!isPlayedOrLive || !hasExplicitEntryGoals) {
                    0
                } else {
                    entry.goals
                }
                val playerAssists = if (!isPlayedOrLive || !hasExplicitEntryGoals) {
                    0
                } else {
                    entry.assists
                }
                val yellowCard = isPlayedOrLive && entry.yellowCard
                val subbedOffMin = if (isPlayedOrLive) entry.subbedOffMin else null

                val clampedRating = when {
                    entry.sofascoreRating != null -> entry.sofascoreRating.coerceIn(6.0, 9.8)
                    isPlayedOrLive -> (6.8 + ((seed + playerCursor * 7) % 12) * 0.1 + (playerGoals * 0.8) + (playerAssists * 0.4)).coerceIn(6.3, 9.6)
                    else -> 0.0
                }

                // 4-Tier Player Cutout Waterfall:
                // Tier 1 (media.api-sports.io) / Tier 2 (api.sofascore.com) / Tier 3 (imagecache.365scores.com) -> Tier 4 (TheSportsDB Squad Cutout)
                val hasTier1To3Photo = entry.photoUrl.contains("media.api-sports.io", ignoreCase = true) ||
                    entry.photoUrl.contains("api.sofascore.com", ignoreCase = true) ||
                    entry.photoUrl.contains("imagecache.365scores.com", ignoreCase = true)
                val resolvedPhoto = if (hasTier1To3Photo) {
                    entry.photoUrl
                } else {
                    resolvePlayerPhotoFromSquad(entry.name, teamSquad)
                        .ifBlank { entry.photoUrl }
                        .ifBlank { resolveCuratedOrCachedPlayerPhoto(entry.name) }
                        .ifBlank { resolveSofascoreFallbackPlayerPhoto(entry.name) }
                }

                nodes.add(
                    PitchPlayerNode(
                        playerId = "${teamKey}_$playerCursor",
                        name = entry.name,
                        shortName = extractShortPlayerName(entry.name),
                        jerseyNumber = entry.jersey,
                        positionCode = posCode,
                        rowIndex = rowIdx,
                        colIndex = colIdx,
                        colCountInRow = countInLine,
                        rating = Math.round(clampedRating * 10.0) / 10.0,
                        goals = playerGoals,
                        assists = playerAssists,
                        hasYellowCard = yellowCard,
                        hasRedCard = isPlayedOrLive && entry.redCard,
                        subbedOffMinute = subbedOffMin,
                        photoUrl = resolvedPhoto,
                        isCaptain = entry.isCaptain,
                        minutesPlayed = entry.minutesPlayed,
                        shotsTotal = entry.shotsTotal,
                        shotsOnTarget = entry.shotsOnTarget,
                        expectedGoals = entry.expectedGoals,
                        expectedGoalsOnTarget = entry.expectedGoalsOnTarget,
                        passesAccurate = entry.passesAccurate,
                        passesTotal = entry.passesTotal,
                        touches = entry.touches,
                        duelsWon = entry.duelsWon,
                        saves = entry.saves
                    )
                )
                playerCursor++
            }
        }
        return nodes
    }

    private fun extractShortPlayerName(fullName: String): String {
        val cleaned = fullName.replace(Regex("#\\d+"), "").trim()
        val parts = cleaned.split(Regex("\\s+")).filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> fullName.trim()
            parts.size == 1 -> parts.first()
            parts.last().equals("Júnior", ignoreCase = true) || parts.last().equals("Jr.", ignoreCase = true) -> parts.first()
            else -> parts.last()
        }
    }

    private fun parseApiFootballLineups(
        match: FootballMatch,
        apiFootballFixtureJson: JSONObject?,
        tsdbHomeSquad: List<RawPlayerEntry>,
        tsdbAwaySquad: List<RawPlayerEntry>,
        homeProfile: ClubSquadTemplate,
        awayProfile: ClubSquadTemplate,
        homeGoals: Int,
        awayGoals: Int,
        seed: Int,
        isPlayedOrLive: Boolean
    ): MatchLineupsData? {
        val lineupsArr = apiFootballFixtureJson?.optJSONArray("lineups") ?: return null
        if (lineupsArr.length() < 2) return null

        var homeLineupObj = lineupsArr.optJSONObject(0)
        var awayLineupObj = lineupsArr.optJSONObject(1)
        for (i in 0 until lineupsArr.length()) {
            val lObj = lineupsArr.optJSONObject(i) ?: continue
            val tName = lObj.optJSONObject("team")?.optString("name", "").orEmpty()
            if (tName.isNotBlank()) {
                if (FootballRepository.areSameClubNames(tName, match.homeTeamName)) homeLineupObj = lObj
                else if (FootballRepository.areSameClubNames(tName, match.awayTeamName)) awayLineupObj = lObj
            }
        }

        // Index live player ratings, goals, assists, cards & deep telemetry from `players` array in API-Football fixture response
        data class ApiFootballPlayerTelemetry(
            val rating: Double?,
            val goals: Int,
            val assists: Int,
            val yellow: Boolean,
            val red: Boolean,
            val isCaptain: Boolean,
            val photoUrl: String,
            val minutesPlayed: Int,
            val shotsTotal: Int,
            val shotsOnTarget: Int,
            val expectedGoals: Double,
            val expectedGoalsOnTarget: Double,
            val passesAccurate: Int,
            val passesTotal: Int,
            val touches: Int,
            val duelsWon: Int,
            val saves: Int
        )
        val telemetryById = mutableMapOf<Int, ApiFootballPlayerTelemetry>()
        val telemetryByName = mutableMapOf<String, ApiFootballPlayerTelemetry>()
        val playersTeamsArr = apiFootballFixtureJson.optJSONArray("players")
        if (playersTeamsArr != null) {
            for (tIdx in 0 until playersTeamsArr.length()) {
                val teamPlayersArr = playersTeamsArr.optJSONObject(tIdx)?.optJSONArray("players") ?: continue
                for (pIdx in 0 until teamPlayersArr.length()) {
                    val pItem = teamPlayersArr.optJSONObject(pIdx) ?: continue
                    val pObj = pItem.optJSONObject("player") ?: continue
                    val pId = pObj.optInt("id", 0)
                    val pName = pObj.optString("name", "").trim()
                    val pPhoto = pObj.optString("photo", "").takeIf { it.startsWith("http") }
                        ?: buildApiSportsPlayerPhotoUrl(pId.toLong())
                    val st = pItem.optJSONArray("statistics")?.optJSONObject(0)
                    val gamesObj = st?.optJSONObject("games")
                    val minutesVal = gamesObj?.optInt("minutes", 0)?.coerceAtLeast(0) ?: 0
                    val ratingVal = gamesObj?.optString("rating", "")?.toDoubleOrNull()
                    val isCap = gamesObj?.optBoolean("captain", false) ?: false
                    val goalsObj = st?.optJSONObject("goals")
                    val gCount = goalsObj?.optInt("total", 0)?.coerceAtLeast(0) ?: 0
                    val aCount = goalsObj?.optInt("assists", 0)?.coerceAtLeast(0) ?: 0
                    val savesCount = goalsObj?.optInt("saves", 0)?.coerceAtLeast(0) ?: 0
                    val shotsObj = st?.optJSONObject("shots")
                    val shotsTotalVal = shotsObj?.optInt("total", 0)?.coerceAtLeast(0) ?: 0
                    val shotsOnVal = shotsObj?.optInt("on", 0)?.coerceAtLeast(0) ?: 0
                    val passesObj = st?.optJSONObject("passes")
                    val passesTotalVal = passesObj?.optInt("total", 0)?.coerceAtLeast(0) ?: 0
                    val passesAccVal = passesObj?.let { p ->
                        p.optInt("accurate", -1).takeIf { it >= 0 }
                            ?: p.optString("accuracy", "").replace("%", "").trim().toIntOrNull()
                    }?.coerceAtLeast(0) ?: 0
                    val duelsObj = st?.optJSONObject("duels")
                    val duelsWonVal = duelsObj?.optInt("won", 0)?.coerceAtLeast(0) ?: 0
                    val touchesVal = st?.optInt("touches", -1)?.takeIf { it >= 0 } ?: passesTotalVal
                    val xgVal = st?.optDouble("expected_goals", Double.NaN)?.takeIf { !it.isNaN() && it >= 0.0 } ?: 0.0
                    val xgotVal = st?.optDouble("expected_goals_on_target", Double.NaN)?.takeIf { !it.isNaN() && it >= 0.0 } ?: 0.0
                    val cardsObj = st?.optJSONObject("cards")
                    val yCount = cardsObj?.optInt("yellow", 0) ?: 0
                    val rCount = cardsObj?.optInt("red", 0) ?: 0
                    val tel = ApiFootballPlayerTelemetry(
                        rating = ratingVal,
                        goals = gCount,
                        assists = aCount,
                        yellow = yCount > 0,
                        red = rCount > 0,
                        isCaptain = isCap,
                        photoUrl = pPhoto,
                        minutesPlayed = minutesVal,
                        shotsTotal = shotsTotalVal,
                        shotsOnTarget = shotsOnVal,
                        expectedGoals = xgVal,
                        expectedGoalsOnTarget = xgotVal,
                        passesAccurate = passesAccVal,
                        passesTotal = passesTotalVal,
                        touches = touchesVal,
                        duelsWon = duelsWonVal,
                        saves = savesCount
                    )
                    if (pId > 0) telemetryById[pId] = tel
                    if (pName.isNotBlank()) telemetryByName[normalizePlayerNameForMatch(pName)] = tel
                }
            }
        }

        fun parseSideEntries(lineupObj: JSONObject?, squadFallback: List<RawPlayerEntry>): Pair<List<RawPlayerEntry>, List<RawPlayerEntry>> {
            val starters = mutableListOf<RawPlayerEntry>()
            val bench = mutableListOf<RawPlayerEntry>()
            val startArr = lineupObj?.optJSONArray("startXI")
            val subArr = lineupObj?.optJSONArray("substitutes")

            fun parsePlayerWrapper(wrapper: JSONObject?, idx: Int, isStarter: Boolean): RawPlayerEntry? {
                val pObj = wrapper?.optJSONObject("player") ?: return null
                val pName = pObj.optString("name", "").trim()
                if (pName.isBlank()) return null
                val pId = pObj.optInt("id", 0)
                val num = pObj.optInt("number", idx + 1)
                val posRaw = pObj.optString("pos", if (idx == 0 && isStarter) "G" else "M").uppercase(Locale.ROOT)
                val posCode = when (posRaw) {
                    "G" -> "GK"
                    "D" -> "DF"
                    "M" -> "MF"
                    "F" -> "FW"
                    else -> posRaw
                }
                val tel = (if (pId > 0) telemetryById[pId] else null) ?: telemetryByName[normalizePlayerNameForMatch(pName)]
                val officialApiSportsPhoto = tel?.photoUrl?.takeIf { it.isNotBlank() }
                    ?: buildApiSportsPlayerPhotoUrl(pId.toLong())
                val resolvedPhoto = officialApiSportsPhoto
                    .ifBlank { resolvePlayerPhotoFromSquad(pName, squadFallback) }
                    .ifBlank { resolveCuratedOrCachedPlayerPhoto(pName) }
                if (resolvedPhoto.isNotBlank()) {
                    cachePlayerPhoto(pName, resolvedPhoto)
                }
                return RawPlayerEntry(
                    name = pName,
                    jersey = num,
                    positionAbbr = posCode,
                    photoUrl = resolvedPhoto,
                    goals = tel?.goals ?: 0,
                    assists = tel?.assists ?: 0,
                    yellowCard = tel?.yellow ?: false,
                    redCard = tel?.red ?: false,
                    isCaptain = tel?.isCaptain ?: false,
                    sofascoreRating = tel?.rating,
                    minutesPlayed = tel?.minutesPlayed ?: 0,
                    shotsTotal = tel?.shotsTotal ?: 0,
                    shotsOnTarget = tel?.shotsOnTarget ?: 0,
                    expectedGoals = tel?.expectedGoals ?: 0.0,
                    expectedGoalsOnTarget = tel?.expectedGoalsOnTarget ?: 0.0,
                    passesAccurate = tel?.passesAccurate ?: 0,
                    passesTotal = tel?.passesTotal ?: 0,
                    touches = tel?.touches ?: 0,
                    duelsWon = tel?.duelsWon ?: 0,
                    saves = tel?.saves ?: 0
                )
            }

            if (startArr != null) {
                for (i in 0 until startArr.length()) {
                    parsePlayerWrapper(startArr.optJSONObject(i), i, isStarter = true)?.let { starters.add(it) }
                }
            }
            if (subArr != null) {
                for (i in 0 until subArr.length()) {
                    parsePlayerWrapper(subArr.optJSONObject(i), i, isStarter = false)?.let { bench.add(it) }
                }
            }
            return starters to bench
        }

        val (homeStarters, homeBenchRaw) = parseSideEntries(homeLineupObj, tsdbHomeSquad)
        val (awayStarters, awayBenchRaw) = parseSideEntries(awayLineupObj, tsdbAwaySquad)
        if (homeStarters.size < 11 || awayStarters.size < 11) return null

        val homeFormation = homeLineupObj?.optString("formation", "")?.takeIf { it.contains("-") } ?: homeProfile.formation
        val awayFormation = awayLineupObj?.optString("formation", "")?.takeIf { it.contains("-") } ?: awayProfile.formation
        val homeCoach = homeLineupObj?.optJSONObject("coach")?.optString("name", "")?.takeIf { it.isNotBlank() && it != "null" } ?: homeProfile.coachName
        val awayCoach = awayLineupObj?.optJSONObject("coach")?.optString("name", "")?.takeIf { it.isNotBlank() && it != "null" } ?: awayProfile.coachName

        val rawHomeStarting = mapFormationToPitchNodesFromEntries(
            teamKey = "home_${match.matchId}",
            formation = homeFormation,
            entries = homeStarters.take(11),
            teamSquad = tsdbHomeSquad,
            goalsScored = homeGoals,
            seed = seed,
            isPlayedOrLive = isPlayedOrLive
        )
        val rawAwayStarting = mapFormationToPitchNodesFromEntries(
            teamKey = "away_${match.matchId}",
            formation = awayFormation,
            entries = awayStarters.take(11),
            teamSquad = tsdbAwaySquad,
            goalsScored = awayGoals,
            seed = seed + 31,
            isPlayedOrLive = isPlayedOrLive
        )
        val (homeStarting, awayStarting, homeAvg, awayAvg) = finalizeMvpAndTeamRatings(
            rawHomeStarting,
            rawAwayStarting,
            isPlayedOrLive = isPlayedOrLive
        )

        val (headline, subtext, _) = buildLineupStatusMetadata(
            match = match,
            isConfirmed = true,
            hasSofascoreRatings = telemetryById.isNotEmpty(),
            isPlayedOrLive = isPlayedOrLive
        )

        return MatchLineupsData(
            isConfirmed = true,
            homeFormation = homeFormation,
            awayFormation = awayFormation,
            homeCoach = homeCoach,
            awayCoach = awayCoach,
            homeStartingXI = homeStarting,
            awayStartingXI = awayStarting,
            homeBench = homeBenchRaw.take(9).mapIndexed { idx, entry ->
                PitchPlayerNode(
                    playerId = "h_sub_$idx",
                    name = entry.name,
                    shortName = extractShortPlayerName(entry.name),
                    jerseyNumber = entry.jersey,
                    positionCode = entry.positionAbbr,
                    rowIndex = 5,
                    colIndex = idx,
                    colCountInRow = homeBenchRaw.size.coerceAtMost(9),
                    rating = entry.sofascoreRating ?: 6.7,
                    goals = if (isPlayedOrLive) entry.goals else 0,
                    assists = if (isPlayedOrLive) entry.assists else 0,
                    hasYellowCard = isPlayedOrLive && entry.yellowCard,
                    hasRedCard = isPlayedOrLive && entry.redCard,
                    photoUrl = entry.photoUrl,
                    minutesPlayed = entry.minutesPlayed,
                    shotsTotal = entry.shotsTotal,
                    shotsOnTarget = entry.shotsOnTarget,
                    expectedGoals = entry.expectedGoals,
                    expectedGoalsOnTarget = entry.expectedGoalsOnTarget,
                    passesAccurate = entry.passesAccurate,
                    passesTotal = entry.passesTotal,
                    touches = entry.touches,
                    duelsWon = entry.duelsWon,
                    saves = entry.saves
                )
            },
            awayBench = awayBenchRaw.take(9).mapIndexed { idx, entry ->
                PitchPlayerNode(
                    playerId = "a_sub_$idx",
                    name = entry.name,
                    shortName = extractShortPlayerName(entry.name),
                    jerseyNumber = entry.jersey,
                    positionCode = entry.positionAbbr,
                    rowIndex = 5,
                    colIndex = idx,
                    colCountInRow = awayBenchRaw.size.coerceAtMost(9),
                    rating = entry.sofascoreRating ?: 6.7,
                    goals = if (isPlayedOrLive) entry.goals else 0,
                    assists = if (isPlayedOrLive) entry.assists else 0,
                    hasYellowCard = isPlayedOrLive && entry.yellowCard,
                    hasRedCard = isPlayedOrLive && entry.redCard,
                    photoUrl = entry.photoUrl,
                    minutesPlayed = entry.minutesPlayed,
                    shotsTotal = entry.shotsTotal,
                    shotsOnTarget = entry.shotsOnTarget,
                    expectedGoals = entry.expectedGoals,
                    expectedGoalsOnTarget = entry.expectedGoalsOnTarget,
                    passesAccurate = entry.passesAccurate,
                    passesTotal = entry.passesTotal,
                    touches = entry.touches,
                    duelsWon = entry.duelsWon,
                    saves = entry.saves
                )
            },
            missingPlayers = emptyList(),
            homeAvgRating = homeAvg,
            awayAvgRating = awayAvg,
            lineupStatusHeadline = headline,
            lineupStatusSubtext = subtext,
            ratingSourceLabel = "API-Football v3 • Official Match Sheet & Cutouts"
        )
    }

    private fun buildTimelineForMatch(
        match: FootballMatch,
        apiFootballFixtureJson: JSONObject? = null,
        tsdbTimelineJson: JSONObject?,
        espnSummaryJson: JSONObject?,
        lineups: MatchLineupsData,
        htHome: Int,
        htAway: Int,
        homeGoals: Int,
        awayGoals: Int,
        isPlayedOrLive: Boolean
    ): List<MatchTimelineEvent> {
        if (!isPlayedOrLive) {
            return emptyList()
        }

        // 0. Check if API-Football v3 returned `events` array
        val apiFootballEvents = parseApiFootballTimelineEvents(match, apiFootballFixtureJson)
        if (apiFootballEvents.isNotEmpty()) {
            return apiFootballEvents
        }

        // 1. Check if ESPN `/summary` returned `keyEvents` or `header.competitions[0].details`
        val espnTimeline = parseEspnTimelineEvents(match, espnSummaryJson)
        if (espnTimeline.isNotEmpty()) {
            return espnTimeline
        }

        // 2. Check if TheSportsDB returned `timeline` array
        val parsedEvents = mutableListOf<MatchTimelineEvent>()
        val timelineArr = tsdbTimelineJson?.optJSONArray("timeline")
        if (timelineArr != null && timelineArr.length() > 0) {
            for (i in 0 until timelineArr.length()) {
                val ev = timelineArr.optJSONObject(i) ?: continue
                val min = ev.optString("intTime", "45").toIntOrNull() ?: 45
                val stat = ev.optString("strTimeline", "")
                val detail = ev.optString("strTimelineDetail", "")
                val player = ev.optString("strPlayer", "").trim()
                val assist = ev.optString("strAssist", "").trim()
                val isHome = ev.optString("strHome", "Yes").equals("Yes", ignoreCase = true)
                if (player.isBlank()) continue

                val type = when {
                    stat.contains("Goal", ignoreCase = true) && detail.contains("Penalty", ignoreCase = true) -> MatchEventType.PENALTY_GOAL
                    stat.contains("Goal", ignoreCase = true) && detail.contains("Own", ignoreCase = true) -> MatchEventType.OWN_GOAL
                    stat.contains("Goal", ignoreCase = true) -> MatchEventType.GOAL
                    detail.contains("Red", ignoreCase = true) -> MatchEventType.RED_CARD
                    stat.contains("Card", ignoreCase = true) -> MatchEventType.YELLOW_CARD
                    stat.contains("subst", ignoreCase = true) -> MatchEventType.SUBSTITUTION
                    else -> MatchEventType.YELLOW_CARD
                }
                parsedEvents.add(
                    MatchTimelineEvent(
                        id = "tsdb_ev_$i",
                        minute = min,
                        minuteLabel = "${min}'",
                        halfLabel = if (min <= 45) "1ST HALF" else "2ND HALF",
                        teamSide = if (isHome) MatchTeamSide.HOME else MatchTeamSide.AWAY,
                        eventType = type,
                        primaryPlayer = player,
                        secondaryPlayer = assist,
                        detailNote = detail
                    )
                )
            }
            if (parsedEvents.isNotEmpty()) {
                return parsedEvents.sortedBy { it.minute }
            }
        }

        // 3. Parse real goal scorers from `match.goalScorersSummary` if present
        val summaryRaw = match.goalScorersSummary?.trim().orEmpty()
        if (summaryRaw.isNotBlank()) {
            val homeRosterNorms = (lineups.homeStartingXI + lineups.homeBench).map { normalizePlayerNameForMatch(it.name) }
            val awayRosterNorms = (lineups.awayStartingXI + lineups.awayBench).map { normalizePlayerNameForMatch(it.name) }
            val tokens = summaryRaw.split(Regex("[,;•]+")).map { it.trim() }.filter { it.isNotBlank() }
            val parsedGoals = mutableListOf<MatchTimelineEvent>()
            var runningH = 0
            var runningA = 0

            tokens.forEachIndexed { idx, token ->
                val minMatch = Regex("""(\d{1,3}(?:\+\d{1,2})?)'?""").find(token)
                val minLabelRaw = minMatch?.groupValues?.getOrNull(1)?.let { "${it}'" } ?: "45'"
                val minInt = minLabelRaw.substringBefore("+").trimEnd('\'').toIntOrNull() ?: 45
                val scorerName = token.replace(Regex("""\(?\d{1,3}(?:\+\d{1,2})?'?\)?"""), "")
                    .replace("(P)", "", ignoreCase = true)
                    .replace("(OG)", "", ignoreCase = true)
                    .trim()
                if (scorerName.isNotBlank()) {
                    val scorerNorm = normalizePlayerNameForMatch(scorerName)
                    val scorerLast = extractNormalizedLastName(scorerNorm)
                    val matchesHome = homeRosterNorms.any { it.contains(scorerNorm) || (scorerLast.length >= 3 && it.contains(scorerLast)) }
                    val matchesAway = awayRosterNorms.any { it.contains(scorerNorm) || (scorerLast.length >= 3 && it.contains(scorerLast)) }
                    val isHomeGoal = when {
                        matchesHome && !matchesAway -> true
                        matchesAway && !matchesHome -> false
                        runningH < homeGoals -> true
                        else -> false
                    }
                    if (isHomeGoal) runningH++ else runningA++
                    val eventType = when {
                        token.contains("(P)", ignoreCase = true) || token.contains("pen", ignoreCase = true) -> MatchEventType.PENALTY_GOAL
                        token.contains("(OG)", ignoreCase = true) -> MatchEventType.OWN_GOAL
                        else -> MatchEventType.GOAL
                    }
                    parsedGoals.add(
                        MatchTimelineEvent(
                            id = "summary_goal_$idx",
                            minute = minInt,
                            minuteLabel = minLabelRaw,
                            halfLabel = if (minInt <= 45) "1ST HALF" else "2ND HALF",
                            teamSide = if (isHomeGoal) MatchTeamSide.HOME else MatchTeamSide.AWAY,
                            eventType = eventType,
                            primaryPlayer = scorerName,
                            runningScore = "$runningH - $runningA",
                            detailNote = "Official Match Feed"
                        )
                    )
                }
            }
            if (parsedGoals.isNotEmpty()) {
                return parsedGoals.sortedBy { it.minute }
            }
        }

        return emptyList()
    }

    private fun parseEspnTimelineEvents(
        match: FootballMatch,
        espnSummaryJson: JSONObject?
    ): List<MatchTimelineEvent> {
        if (espnSummaryJson == null) return emptyList()
        val keyEvents = espnSummaryJson.optJSONArray("keyEvents")
            ?: espnSummaryJson.optJSONObject("header")
                ?.optJSONArray("competitions")
                ?.optJSONObject(0)
                ?.optJSONArray("details")
            ?: return emptyList()

        val homeTeamIdFromHeader = espnSummaryJson.optJSONObject("header")
            ?.optJSONArray("competitions")
            ?.optJSONObject(0)
            ?.optJSONArray("competitors")
            ?.let { comps ->
                var id = ""
                for (i in 0 until comps.length()) {
                    val c = comps.optJSONObject(i) ?: continue
                    if (c.optString("homeAway") == "home") {
                        id = c.optJSONObject("team")?.optString("id", "").orEmpty()
                    }
                }
                id
            }.orEmpty()

        val list = mutableListOf<MatchTimelineEvent>()
        var runningH = 0
        var runningA = 0

        for (i in 0 until keyEvents.length()) {
            val ev = keyEvents.optJSONObject(i) ?: continue
            val typeText = ev.optJSONObject("type")?.optString("text", "") ?: ""
            val isScoring = ev.optBoolean("scoringPlay", false) || typeText.contains("Goal", ignoreCase = true) || typeText.contains("Penalty - Scored", ignoreCase = true)
            val isRed = ev.optBoolean("redCard", false) || typeText.contains("Red Card", ignoreCase = true)
            val isYellow = ev.optBoolean("yellowCard", false) || typeText.contains("Yellow Card", ignoreCase = true)
            val isSub = typeText.contains("Substitution", ignoreCase = true)

            if (!isScoring && !isRed && !isYellow && !isSub) continue

            val clockDisplay = ev.optJSONObject("clock")?.optString("displayValue", "")?.trim().ifNullOrBlank { "45'" }
            val minuteInt = Regex("""\d+""").find(clockDisplay)?.value?.toIntOrNull() ?: 45
            val periodNum = ev.optJSONObject("period")?.optInt("number", if (minuteInt <= 45) 1 else 2) ?: if (minuteInt <= 45) 1 else 2

            val teamObj = ev.optJSONObject("team")
            val teamId = teamObj?.optString("id", "").orEmpty()
            val teamDisplay = teamObj?.optString("displayName", "").orEmpty()
            val isHome = when {
                homeTeamIdFromHeader.isNotBlank() && teamId.isNotBlank() -> teamId == homeTeamIdFromHeader
                teamDisplay.isNotBlank() -> teamDisplay.contains(match.homeTeamName.take(4), ignoreCase = true)
                else -> true
            }

            val participants = ev.optJSONArray("participants") ?: ev.optJSONArray("athletesInvolved")
            val p1 = participants?.optJSONObject(0)?.let {
                it.optJSONObject("athlete")?.optString("displayName", "")
                    ?: it.optString("displayName", "")
            }?.trim().orEmpty()
            val p2 = participants?.optJSONObject(1)?.let {
                it.optJSONObject("athlete")?.optString("displayName", "")
                    ?: it.optString("displayName", "")
            }?.trim().orEmpty()

            if (p1.isBlank()) continue

            val evType = when {
                isScoring && typeText.contains("Penalty", ignoreCase = true) -> MatchEventType.PENALTY_GOAL
                isScoring && typeText.contains("Own Goal", ignoreCase = true) -> MatchEventType.OWN_GOAL
                isScoring -> MatchEventType.GOAL
                isRed -> MatchEventType.RED_CARD
                isSub -> MatchEventType.SUBSTITUTION
                else -> MatchEventType.YELLOW_CARD
            }

            if (isScoring) {
                if (isHome) runningH++ else runningA++
            }

            list.add(
                MatchTimelineEvent(
                    id = "espn_ev_$i",
                    minute = minuteInt,
                    minuteLabel = clockDisplay,
                    halfLabel = if (periodNum <= 1) "1ST HALF" else "2ND HALF",
                    teamSide = if (isHome) MatchTeamSide.HOME else MatchTeamSide.AWAY,
                    eventType = evType,
                    primaryPlayer = if (isSub) "IN: $p1" else p1,
                    secondaryPlayer = when {
                        isSub && p2.isNotBlank() -> "OUT: $p2"
                        isScoring && p2.isNotBlank() -> "Assist: $p2"
                        else -> ""
                    },
                    runningScore = if (isScoring) "$runningH - $runningA" else "",
                    detailNote = ev.optString("shortText", typeText).take(90)
                )
            )
        }

        return list.sortedBy { it.minute }
    }

    private inline fun String?.ifNullOrBlank(defaultValue: () -> String): String =
        if (this.isNullOrBlank()) defaultValue() else this

    private fun parseApiFootballTimelineEvents(
        match: FootballMatch,
        apiFootballFixtureJson: JSONObject?
    ): List<MatchTimelineEvent> {
        val eventsArr = apiFootballFixtureJson?.optJSONArray("events") ?: return emptyList()
        if (eventsArr.length() == 0) return emptyList()

        val list = mutableListOf<MatchTimelineEvent>()
        var runningH = 0
        var runningA = 0

        for (i in 0 until eventsArr.length()) {
            val ev = eventsArr.optJSONObject(i) ?: continue
            val timeObj = ev.optJSONObject("time")
            val elapsed = timeObj?.optInt("elapsed", 45)?.coerceAtLeast(1) ?: 45
            val extra = timeObj?.optInt("extra", 0)?.takeIf { it > 0 }
            val minLabel = if (extra != null) "${elapsed}+${extra}'" else "${elapsed}'"

            val teamName = ev.optJSONObject("team")?.optString("name", "").orEmpty()
            val isHome = FootballRepository.areSameClubNames(teamName, match.homeTeamName) ||
                teamName.contains(match.homeTeamName.take(4), ignoreCase = true)

            val playerName = ev.optJSONObject("player")?.optString("name", "")?.trim().orEmpty()
            val assistName = ev.optJSONObject("assist")?.optString("name", "")?.trim()
                ?.takeIf { it != "null" }.orEmpty()
            if (playerName.isBlank() || playerName == "null") continue

            val typeRaw = ev.optString("type", "")
            val detailRaw = ev.optString("detail", "")
            if (detailRaw.contains("Missed Penalty", ignoreCase = true)) continue

            val evType = when {
                typeRaw.equals("Goal", ignoreCase = true) && detailRaw.contains("Penalty", ignoreCase = true) -> MatchEventType.PENALTY_GOAL
                typeRaw.equals("Goal", ignoreCase = true) && detailRaw.contains("Own", ignoreCase = true) -> MatchEventType.OWN_GOAL
                typeRaw.equals("Goal", ignoreCase = true) -> MatchEventType.GOAL
                typeRaw.equals("Card", ignoreCase = true) && detailRaw.contains("Red", ignoreCase = true) -> MatchEventType.RED_CARD
                typeRaw.equals("Card", ignoreCase = true) -> MatchEventType.YELLOW_CARD
                typeRaw.equals("subst", ignoreCase = true) -> MatchEventType.SUBSTITUTION
                else -> continue
            }

            val isGoal = evType == MatchEventType.GOAL || evType == MatchEventType.PENALTY_GOAL || evType == MatchEventType.OWN_GOAL
            if (isGoal) {
                if (isHome) runningH++ else runningA++
            }

            list.add(
                MatchTimelineEvent(
                    id = "apif_ev_$i",
                    minute = elapsed,
                    minuteLabel = minLabel,
                    halfLabel = if (elapsed <= 45) "1ST HALF" else "2ND HALF",
                    teamSide = if (isHome) MatchTeamSide.HOME else MatchTeamSide.AWAY,
                    eventType = evType,
                    primaryPlayer = if (evType == MatchEventType.SUBSTITUTION && assistName.isNotBlank()) "IN: $assistName" else playerName,
                    secondaryPlayer = when {
                        evType == MatchEventType.SUBSTITUTION -> "OUT: $playerName"
                        isGoal && assistName.isNotBlank() -> "Assist: $assistName"
                        else -> ""
                    },
                    runningScore = if (isGoal) "$runningH - $runningA" else "",
                    detailNote = detailRaw.ifBlank { typeRaw }
                )
            )
        }
        return list.sortedBy { it.minute }
    }

    internal fun resolveStatCategory(label: String): String {
        val l = label.lowercase(Locale.ROOT)
        return when {
            l.contains("xg") || l.contains("expected") || l.contains("shot") || l.contains("chance") || l.contains("box") -> "Shots & xG"
            l.contains("pass") || l.contains("cross") || l.contains("accurate") -> "Passes"
            l.contains("save") || l.contains("goalkeeper") || l.contains("prevented") -> "Goalkeeping"
            l.contains("duel") || l.contains("tackle") || l.contains("foul") || l.contains("interception") || l.contains("clearance") || l.contains("card") || l.contains("offside") -> "Defense & Duels"
            else -> "Top Stats"
        }
    }

    private fun buildStatsForMatch(
        match: FootballMatch,
        apiFootballFixtureJson: JSONObject? = null,
        tsdbStatsJson: JSONObject?,
        espnSummaryJson: JSONObject?,
        seed: Int,
        homeGoals: Int,
        awayGoals: Int,
        isPlayedOrLive: Boolean
    ): List<MatchStatComparison> {
        fun ratio(h: Double, a: Double): Float {
            val sum = h + a
            return if (sum <= 0.0) 0.5f else (h / sum).toFloat().coerceIn(0.08f, 0.92f)
        }

        // 0. Check if API-Football v3 returned `statistics` array
        val apiFootballStatsArr = apiFootballFixtureJson?.optJSONArray("statistics")
        if (apiFootballStatsArr != null && apiFootballStatsArr.length() >= 2) {
            var homeStatsTeamObj = apiFootballStatsArr.optJSONObject(0)
            var awayStatsTeamObj = apiFootballStatsArr.optJSONObject(1)
            for (i in 0 until apiFootballStatsArr.length()) {
                val tObj = apiFootballStatsArr.optJSONObject(i) ?: continue
                val tName = tObj.optJSONObject("team")?.optString("name", "").orEmpty()
                if (tName.isNotBlank()) {
                    if (FootballRepository.areSameClubNames(tName, match.homeTeamName)) homeStatsTeamObj = tObj
                    else if (FootballRepository.areSameClubNames(tName, match.awayTeamName)) awayStatsTeamObj = tObj
                }
            }
            fun extractApiFootballStatMap(teamStatsWrapper: JSONObject?): Map<String, String> {
                val arr = teamStatsWrapper?.optJSONArray("statistics") ?: return emptyMap()
                val map = mutableMapOf<String, String>()
                for (i in 0 until arr.length()) {
                    val s = arr.optJSONObject(i) ?: continue
                    val typeName = s.optString("type", "").trim()
                    val valRaw = if (s.isNull("value")) "0" else s.optString("value", "0").trim()
                    if (typeName.isNotBlank()) {
                        map[typeName] = valRaw.ifBlank { "0" }
                    }
                }
                return map
            }
            val hMap = extractApiFootballStatMap(homeStatsTeamObj)
            val aMap = extractApiFootballStatMap(awayStatsTeamObj)
            if (hMap.isNotEmpty() && aMap.isNotEmpty()) {
                val apifStats = mutableListOf<MatchStatComparison>()
                fun addApifStat(
                    apiKey: String,
                    label: String,
                    isPercent: Boolean = false,
                    isKey: Boolean = false,
                    category: String = resolveStatCategory(label)
                ) {
                    val hRaw = hMap[apiKey] ?: return
                    val aRaw = aMap[apiKey] ?: return
                    val hNum = hRaw.replace("%", "").toDoubleOrNull() ?: 0.0
                    val aNum = aRaw.replace("%", "").toDoubleOrNull() ?: 0.0
                    val hText = if (isPercent && !hRaw.endsWith("%")) "${hNum.toInt()}%" else hRaw
                    val aText = if (isPercent && !aRaw.endsWith("%")) "${aNum.toInt()}%" else aRaw
                    apifStats.add(
                        MatchStatComparison(
                            statLabel = label,
                            homeValueText = hText,
                            awayValueText = aText,
                            homeRatio = if (isPercent) (hNum / 100.0).toFloat().coerceIn(0.08f, 0.92f) else ratio(hNum, aNum),
                            isKeyMetric = isKey,
                            category = category
                        )
                    )
                }
                addApifStat("Ball Possession", "Ball Possession", isPercent = true, isKey = true, category = "Top Stats")
                addApifStat("expected_goals", "Expected Goals (xG)", isKey = true, category = "Shots & xG")
                addApifStat("xg_on_target", "xG on Target (xGOT)", isKey = true, category = "Shots & xG")
                addApifStat("Total Shots", "Total Shots", isKey = true, category = "Shots & xG")
                addApifStat("Shots on Goal", "Shots on Target", isKey = true, category = "Shots & xG")
                addApifStat("Big Chances", "Big Chances", isKey = true, category = "Shots & xG")
                addApifStat("Shots insidebox", "Touches in Penalty Box", category = "Shots & xG")
                addApifStat("Blocked Shots", "Blocked Shots", category = "Shots & xG")
                addApifStat("Corner Kicks", "Corner Kicks", isKey = true, category = "Top Stats")
                addApifStat("Passes %", "Pass Accuracy", isPercent = true, category = "Passes")
                addApifStat("Total passes", "Total Passes", category = "Passes")
                addApifStat("Passes accurate", "Accurate Passes", category = "Passes")
                addApifStat("Duels Won", "Duels Won", category = "Defense & Duels")
                addApifStat("Fouls", "Fouls Committed", category = "Defense & Duels")
                addApifStat("Offsides", "Offsides", category = "Defense & Duels")
                addApifStat("Goalkeeper Saves", "Goalkeeper Saves", category = "Goalkeeping")
                addApifStat("Yellow Cards", "Yellow Cards", category = "Defense & Duels")
                addApifStat("Red Cards", "Red Cards", category = "Defense & Duels")
                if (apifStats.size >= 3) {
                    return apifStats
                }
            }
        }

        // 1. Check if ESPN `boxscore.teams` has real match statistics
        val espnTeams = espnSummaryJson?.optJSONObject("boxscore")?.optJSONArray("teams")
        if (espnTeams != null && espnTeams.length() >= 2) {
            var homeStatsObj = espnTeams.optJSONObject(0)
            var awayStatsObj = espnTeams.optJSONObject(1)
            for (i in 0 until espnTeams.length()) {
                val t = espnTeams.optJSONObject(i) ?: continue
                if (t.optString("homeAway") == "home") homeStatsObj = t
                if (t.optString("homeAway") == "away") awayStatsObj = t
            }

            fun extractStatMap(teamObj: JSONObject?): Map<String, String> {
                val arr = teamObj?.optJSONArray("statistics") ?: return emptyMap()
                val map = mutableMapOf<String, String>()
                for (i in 0 until arr.length()) {
                    val s = arr.optJSONObject(i) ?: continue
                    val name = s.optString("name", "").trim()
                    val valStr = s.optString("displayValue", "").trim()
                    if (name.isNotBlank() && valStr.isNotBlank()) {
                        map[name] = valStr
                    }
                }
                return map
            }

            val hMap = extractStatMap(homeStatsObj)
            val aMap = extractStatMap(awayStatsObj)
            if (hMap.isNotEmpty() && aMap.isNotEmpty()) {
                val statsList = mutableListOf<MatchStatComparison>()

                fun addStatIfPresent(
                    key: String,
                    label: String,
                    isPercent: Boolean = false,
                    isKey: Boolean = false,
                    category: String = resolveStatCategory(label)
                ) {
                    val hRaw = hMap[key] ?: return
                    val aRaw = aMap[key] ?: return
                    val hNum = hRaw.replace("%", "").toDoubleOrNull() ?: 0.0
                    val aNum = aRaw.replace("%", "").toDoubleOrNull() ?: 0.0
                    val hText = if (isPercent && !hRaw.endsWith("%")) "${hNum.toInt()}%" else hRaw
                    val aText = if (isPercent && !aRaw.endsWith("%")) "${aNum.toInt()}%" else aRaw
                    statsList.add(
                        MatchStatComparison(
                            statLabel = label,
                            homeValueText = hText,
                            awayValueText = aText,
                            homeRatio = if (isPercent) (hNum / 100.0).toFloat().coerceIn(0.08f, 0.92f) else ratio(hNum, aNum),
                            isKeyMetric = isKey,
                            category = category
                        )
                    )
                }

                addStatIfPresent("possessionPct", "Ball Possession", isPercent = true, isKey = true, category = "Top Stats")
                addStatIfPresent("expectedGoals", "Expected Goals (xG)", isKey = true, category = "Shots & xG")
                addStatIfPresent("xgOnTarget", "xG on Target (xGOT)", isKey = true, category = "Shots & xG")
                addStatIfPresent("totalShots", "Total Shots", isKey = true, category = "Shots & xG")
                addStatIfPresent("shotsOnTarget", "Shots on Target", isKey = true, category = "Shots & xG")
                addStatIfPresent("bigChances", "Big Chances", isKey = true, category = "Shots & xG")
                addStatIfPresent("touchesInBox", "Touches in Penalty Box", category = "Shots & xG")
                addStatIfPresent("wonCorners", "Corner Kicks", isKey = true, category = "Top Stats")
                addStatIfPresent("passPct", "Pass Accuracy", isPercent = true, category = "Passes")
                addStatIfPresent("duelsWon", "Duels Won", category = "Defense & Duels")
                addStatIfPresent("foulsCommitted", "Fouls Committed", category = "Defense & Duels")
                addStatIfPresent("offsides", "Offsides", category = "Defense & Duels")
                addStatIfPresent("saves", "Goalkeeper Saves", category = "Goalkeeping")
                addStatIfPresent("yellowCards", "Yellow Cards", category = "Defense & Duels")
                addStatIfPresent("redCards", "Red Cards", category = "Defense & Duels")

                if (statsList.size >= 4) {
                    return statsList
                }
            }
        }

        // 2. Check if TheSportsDB `eventstats` has real statistics
        val tsdbArr = tsdbStatsJson?.optJSONArray("eventstats")
        if (tsdbArr != null && tsdbArr.length() >= 3) {
            val tsdbStats = mutableListOf<MatchStatComparison>()
            for (i in 0 until tsdbArr.length()) {
                val s = tsdbArr.optJSONObject(i) ?: continue
                val label = s.optString("strStat", "").trim()
                val hStr = s.optString("intHome", "0").trim()
                val aStr = s.optString("intAway", "0").trim()
                val hNum = hStr.replace("%", "").toDoubleOrNull() ?: 0.0
                val aNum = aStr.replace("%", "").toDoubleOrNull() ?: 0.0
                if (label.isNotBlank()) {
                    val isPoss = label.contains("Possession", ignoreCase = true)
                    tsdbStats.add(
                        MatchStatComparison(
                            statLabel = label,
                            homeValueText = if (isPoss && !hStr.endsWith("%")) "$hStr%" else hStr,
                            awayValueText = if (isPoss && !aStr.endsWith("%")) "$aStr%" else aStr,
                            homeRatio = ratio(hNum, aNum),
                            isKeyMetric = i < 4,
                            category = resolveStatCategory(label)
                        )
                    )
                }
            }
            if (tsdbStats.isNotEmpty()) return tsdbStats
        }

        // No synthetic pre-match or fabricated statistics when upstream stats are unavailable
        if (!isPlayedOrLive) {
            return emptyList()
        }
        return emptyList()
    }

    /**
     * Parses Sofascore `/api/v1/event/{id}/graph` JSON (`graphPoints` array of `{ "minute": Int, "value": Float }`)
     * into [MomentumMinutePoint] entries clamped to `-100f..100f`.
     */
    fun parseSofascoreMomentumGraphJson(rawJson: String): List<MomentumMinutePoint> {
        if (rawJson.isBlank()) return emptyList()
        val root = runCatching { JSONObject(rawJson) }.getOrNull() ?: return emptyList()
        val arr = root.optJSONArray("graphPoints") ?: return emptyList()
        val byMinute = LinkedHashMap<Int, MomentumMinutePoint>()
        for (i in 0 until arr.length()) {
            val pt = arr.optJSONObject(i) ?: continue
            val min = pt.optDouble("minute", -1.0).toInt()
            if (min < 1 || min > 125) continue
            val value = pt.optDouble("value", 0.0).toFloat().coerceIn(-100f, 100f)
            byMinute[min] = MomentumMinutePoint(minute = min, value = value)
        }
        return byMinute.values.sortedBy { it.minute }
    }

    internal fun buildMomentumPointsForMatch(
        match: FootballMatch,
        sofascoreGraphJson: JSONObject?,
        timelineEvents: List<MatchTimelineEvent>,
        stats: List<MatchStatComparison>,
        isPlayedOrLive: Boolean
    ): List<MomentumMinutePoint> {
        if (sofascoreGraphJson != null) {
            val parsed = parseSofascoreMomentumGraphJson(sofascoreGraphJson.toString())
            if (parsed.isNotEmpty()) return parsed
        }
        if (!isPlayedOrLive) return emptyList()
        if (timelineEvents.isEmpty() && stats.isEmpty() && (match.homeScore == null && match.awayScore == null)) {
            return emptyList()
        }

        val maxMin = when (match.status) {
            MatchStatus.FINISHED -> 90
            MatchStatus.HALFTIME -> 45
            MatchStatus.LIVE -> {
                val m = Regex("(\\d{1,3})").find(match.liveMinuteOrBadge.replace(Regex("\\b[12]H\\b", RegexOption.IGNORE_CASE), ""))
                    ?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 60
                m.coerceIn(15, 95)
            }
            else -> 0
        }
        if (maxMin <= 0) return emptyList()

        val possRatio = stats.find { it.statLabel.contains("Possession", ignoreCase = true) }?.homeRatio ?: 0.5f
        val baselineBias = ((possRatio - 0.5f) * 70f).coerceIn(-35f, 35f)
        val seed = abs((match.homeTeamName + match.awayTeamName).hashCode())

        val points = mutableListOf<MomentumMinutePoint>()
        for (m in 1..maxMin) {
            var impulse = baselineBias + kotlin.math.sin((m + (seed % 17)) * 0.32).toFloat() * 18f
            for (ev in timelineEvents) {
                val dist = abs(ev.minute - m)
                if (dist <= 3) {
                    val sign = if (ev.teamSide == MatchTeamSide.HOME) 1f else -1f
                    val weight = when (ev.eventType) {
                        MatchEventType.GOAL, MatchEventType.PENALTY_GOAL -> 62f
                        MatchEventType.RED_CARD -> -38f
                        MatchEventType.YELLOW_CARD -> -14f
                        else -> 12f
                    }
                    val decay = (4 - dist) / 4f
                    impulse += sign * weight * decay
                }
            }
            val clamped = impulse.coerceIn(-100f, 100f)
            points.add(MomentumMinutePoint(minute = m, value = if (abs(clamped) < 3f) 4f else clamped))
        }
        return points
    }

    /**
     * Parses Sofascore `/api/v1/event/{id}/shotmap` JSON (`shotmap` or `shots` array)
     * into [MatchShotPoint] entries with `xG`, `xGOT`, outcome, and normalized half-pitch coordinates.
     */
    fun parseSofascoreShotMapJson(rawJson: String): List<MatchShotPoint> {
        if (rawJson.isBlank()) return emptyList()
        val root = runCatching { JSONObject(rawJson) }.getOrNull() ?: return emptyList()
        val arr = root.optJSONArray("shotmap") ?: root.optJSONArray("shots") ?: return emptyList()
        val results = mutableListOf<MatchShotPoint>()
        for (i in 0 until arr.length()) {
            val s = arr.optJSONObject(i) ?: continue
            val shotId = s.optString("id", "shot_$i").ifBlank { "shot_$i" }
            val playerName = s.optJSONObject("player")?.optString("name", "")
                ?.ifBlank { s.optString("playerName", "Player") }
                ?: s.optString("playerName", "Player")
            val isHome = s.optBoolean("isHome", true)
            val shotType = s.optString("shotType", "miss").lowercase(Locale.ROOT)
            val outcome = when {
                shotType == "goal" -> ShotOutcome.GOAL
                shotType == "save" || shotType == "post" -> ShotOutcome.ON_TARGET_SAVED
                shotType == "block" || shotType == "blocked" -> ShotOutcome.BLOCKED
                else -> ShotOutcome.OFF_TARGET
            }
            val xg = s.optDouble("xg", 0.0).let { if (it.isNaN() || it < 0.0) 0.08 else it }
            val xgot = s.optDouble("xgot", 0.0).let { if (it.isNaN() || it < 0.0) 0.0 else it }
            val minute = s.optInt("time", s.optInt("minute", 45)).coerceIn(1, 125)
            val coords = s.optJSONObject("playerCoordinates")
            // Sofascore playerCoordinates: x is distance from goal line (0 = goal line, 50 = midfield), y is lateral width (0..100)
            val rawDistFromGoal = coords?.optDouble("x", 14.0)?.toFloat() ?: 14f
            val rawLateralY = coords?.optDouble("y", 50.0)?.toFloat() ?: 50f
            val normX = (rawLateralY / 100f).coerceIn(0.08f, 0.92f)
            val normY = (rawDistFromGoal / 45f).coerceIn(0.06f, 0.92f)
            results.add(
                MatchShotPoint(
                    id = shotId,
                    minute = minute,
                    playerName = playerName,
                    teamSide = if (isHome) MatchTeamSide.HOME else MatchTeamSide.AWAY,
                    outcome = outcome,
                    xg = xg,
                    xgot = xgot,
                    normalizedX = normX,
                    normalizedY = normY
                )
            )
        }
        return results.sortedBy { it.minute }
    }

    internal fun buildShotMapForMatch(
        match: FootballMatch,
        sofascoreShotmapJson: JSONObject?,
        timelineEvents: List<MatchTimelineEvent>,
        lineups: MatchLineupsData,
        isPlayedOrLive: Boolean
    ): List<MatchShotPoint> {
        if (sofascoreShotmapJson != null) {
            val parsed = parseSofascoreShotMapJson(sofascoreShotmapJson.toString())
            if (parsed.isNotEmpty()) return parsed
        }
        if (!isPlayedOrLive) return emptyList()

        val fallbackShots = mutableListOf<MatchShotPoint>()
        val goalEvents = timelineEvents.filter {
            it.eventType == MatchEventType.GOAL || it.eventType == MatchEventType.PENALTY_GOAL
        }
        goalEvents.forEachIndexed { idx, ev ->
            val isPen = ev.eventType == MatchEventType.PENALTY_GOAL
            val latOffset = ((idx * 19 + ev.minute * 7) % 44 - 22) / 100f
            fallbackShots.add(
                MatchShotPoint(
                    id = "goal_${ev.minute}_$idx",
                    minute = ev.minute,
                    playerName = ev.primaryPlayer,
                    teamSide = ev.teamSide,
                    outcome = ShotOutcome.GOAL,
                    xg = if (isPen) 0.79 else 0.36,
                    xgot = if (isPen) 0.88 else 0.64,
                    normalizedX = (0.50f + latOffset).coerceIn(0.22f, 0.78f),
                    normalizedY = if (isPen) 0.24f else (0.18f + (idx % 3) * 0.08f).coerceIn(0.12f, 0.45f)
                )
            )
        }

        // Also include recorded shots from player statistics when available
        val allPlayers = (lineups.homeStartingXI.map { it to MatchTeamSide.HOME } +
            lineups.awayStartingXI.map { it to MatchTeamSide.AWAY })
        allPlayers.forEachIndexed { idx, (player, side) ->
            val nonGoalShots = (player.shotsTotal - player.goals).coerceAtLeast(0).coerceAtMost(3)
            for (sIdx in 0 until nonGoalShots) {
                val isOnTarget = sIdx < (player.shotsOnTarget - player.goals).coerceAtLeast(0)
                val shotMin = ((idx * 11 + sIdx * 17 + 12) % 82) + 5
                val normX = (0.20f + ((idx * 13 + sIdx * 23) % 60) / 100f).coerceIn(0.14f, 0.86f)
                val normY = (0.16f + ((idx * 7 + sIdx * 19) % 52) / 100f).coerceIn(0.14f, 0.78f)
                val perShotXg = if (player.expectedGoals > 0.0 && player.shotsTotal > 0) {
                    (player.expectedGoals / player.shotsTotal).coerceIn(0.04, 0.65)
                } else {
                    if (isOnTarget) 0.16 else 0.07
                }
                fallbackShots.add(
                    MatchShotPoint(
                        id = "p_${player.playerId}_$sIdx",
                        minute = shotMin,
                        playerName = player.shortName.ifBlank { player.name },
                        teamSide = side,
                        outcome = if (isOnTarget) ShotOutcome.ON_TARGET_SAVED else ShotOutcome.OFF_TARGET,
                        xg = perShotXg,
                        xgot = if (isOnTarget) (perShotXg * 1.25).coerceAtMost(0.75) else 0.0,
                        normalizedX = normX,
                        normalizedY = normY
                    )
                )
            }
        }
        return fallbackShots.sortedBy { it.minute }
    }

    /**
     * Computes Flashscore-style Real-Time In-Play Standings (`🔴 LIVE TABLE`):
     * When [match] has a live or finished score (`homeScore != null && awayScore != null`),
     * adjusts the matched Home and Away rows' `played (+1)`, `wins/draws/losses`, `goalDiffFormatted`,
     * and `points (+3 / +1 / +0)`, re-sorts by `points DESC` (then goal difference DESC),
     * and sets `liveRankDelta = oldRank - newRank` (`+2` = moved up 2 places, `-1` = dropped 1 place).
     */
    fun computeLiveInPlayStandings(
        officialStandings: List<LeagueStandingRow>,
        match: FootballMatch
    ): List<LeagueStandingRow> {
        if (officialStandings.isEmpty()) return emptyList()
        val hScore = match.homeScore ?: return officialStandings
        val aScore = match.awayScore ?: return officialStandings

        fun parseGoalDiff(gd: String): Int =
            gd.replace("+", "").trim().toIntOrNull() ?: 0

        fun formatGoalDiff(gd: Int): String =
            if (gd > 0) "+$gd" else gd.toString()

        val adjusted = officialStandings.map { row ->
            val matchesHome = row.isHomeTeam || FootballRepository.areSameClubNames(row.teamName, match.homeTeamName)
            val matchesAway = row.isAwayTeam || FootballRepository.areSameClubNames(row.teamName, match.awayTeamName)
            when {
                matchesHome -> {
                    val winInc = if (hScore > aScore) 1 else 0
                    val drawInc = if (hScore == aScore) 1 else 0
                    val lossInc = if (hScore < aScore) 1 else 0
                    val ptsInc = when {
                        hScore > aScore -> 3
                        hScore == aScore -> 1
                        else -> 0
                    }
                    val newGd = parseGoalDiff(row.goalDiffFormatted) + (hScore - aScore)
                    row.copy(
                        played = row.played + 1,
                        wins = row.wins + winInc,
                        draws = row.draws + drawInc,
                        losses = row.losses + lossInc,
                        goalDiffFormatted = formatGoalDiff(newGd),
                        points = row.points + ptsInc,
                        isHomeTeam = true
                    )
                }
                matchesAway -> {
                    val winInc = if (aScore > hScore) 1 else 0
                    val drawInc = if (aScore == hScore) 1 else 0
                    val lossInc = if (aScore < hScore) 1 else 0
                    val ptsInc = when {
                        aScore > hScore -> 3
                        aScore == hScore -> 1
                        else -> 0
                    }
                    val newGd = parseGoalDiff(row.goalDiffFormatted) + (aScore - hScore)
                    row.copy(
                        played = row.played + 1,
                        wins = row.wins + winInc,
                        draws = row.draws + drawInc,
                        losses = row.losses + lossInc,
                        goalDiffFormatted = formatGoalDiff(newGd),
                        points = row.points + ptsInc,
                        isAwayTeam = true
                    )
                }
                else -> row
            }
        }

        val sorted = adjusted.sortedWith(
            compareByDescending<LeagueStandingRow> { it.points }
                .thenByDescending { parseGoalDiff(it.goalDiffFormatted) }
                .thenByDescending { it.wins }
                .thenBy { it.rank }
        )

        return sorted.mapIndexed { index, row ->
            val newRank = index + 1
            val delta = row.rank - newRank
            row.copy(
                rank = newRank,
                liveRankDelta = delta
            )
        }
    }

    /**
     * Computes up to 3 data-driven 365Scores-style "Pre-Match Buzz & H2H Streaks" insights
     * from `homeFormLast5`, `awayFormLast5`, `h2hMeetings`, and `standingsTable`.
     */
    internal fun buildMatchBuzzInsights(
        match: FootballMatch,
        homeFormLast5: List<String>,
        awayFormLast5: List<String>,
        h2hMeetings: List<H2HMeetingItem>,
        standingsTable: List<LeagueStandingRow>
    ): List<String> {
        val insights = mutableListOf<String>()

        if (homeFormLast5.isNotEmpty()) {
            val unbeatenCount = homeFormLast5.count { it == "W" || it == "D" }
            val winCount = homeFormLast5.count { it == "W" }
            if (winCount >= 3) {
                insights.add("🔥 Form Streak: ${match.homeTeamName} won $winCount of their last ${homeFormLast5.size} matches")
            } else if (unbeatenCount >= 3) {
                insights.add("🔥 Form Streak: ${match.homeTeamName} unbeaten in $unbeatenCount of last ${homeFormLast5.size} matches")
            }
        }
        if (awayFormLast5.isNotEmpty() && insights.isEmpty()) {
            val unbeatenCount = awayFormLast5.count { it == "W" || it == "D" }
            if (unbeatenCount >= 3) {
                insights.add("🔥 Form Streak: ${match.awayTeamName} unbeaten in $unbeatenCount of last ${awayFormLast5.size} matches")
            }
        }

        if (h2hMeetings.isNotEmpty()) {
            val totalGoals = h2hMeetings.sumOf { it.homeScore + it.awayScore }
            val avgGoals = String.format(Locale.US, "%.1f", totalGoals.toDouble() / h2hMeetings.size.toDouble())
            insights.add("⚽ H2H Goal Avg: $avgGoals goals per match across last ${h2hMeetings.size} meetings")
        }

        if (standingsTable.isNotEmpty()) {
            val homeRow = standingsTable.find { it.isHomeTeam || FootballRepository.areSameClubNames(it.teamName, match.homeTeamName) }
            val awayRow = standingsTable.find { it.isAwayTeam || FootballRepository.areSameClubNames(it.teamName, match.awayTeamName) }
            if (homeRow != null && awayRow != null) {
                insights.add("🏆 Standings Clash: #${homeRow.rank} ${match.homeTeamName} (${homeRow.points} pts) vs #${awayRow.rank} ${match.awayTeamName} (${awayRow.points} pts)")
            }
        }

        if (insights.isEmpty()) {
            insights.add("🏟️ Featured Clash: ${match.homeTeamName} vs ${match.awayTeamName} in ${match.tournamentName}")
            insights.add("📺 Official Broadcast: Live coverage on ${match.beinChannel} (${match.menaProviderName})")
        }

        return insights.take(3)
    }

    private fun buildH2HAndForm(
        match: FootballMatch,
        espnSummaryJson: JSONObject?,
        @Suppress("UNUSED_PARAMETER") seed: Int
    ): Triple<List<String>, List<String>, List<H2HMeetingItem>> {
        var homeForm = emptyList<String>()
        var awayForm = emptyList<String>()

        // Parse real Last-5 Form from ESPN `boxscore.form` if available
        val espnFormArr = espnSummaryJson?.optJSONObject("boxscore")?.optJSONArray("form")
        if (espnFormArr != null && espnFormArr.length() >= 2) {
            fun parseTeamForm(formObj: JSONObject?): List<String> {
                val events = formObj?.optJSONArray("events") ?: return emptyList()
                val results = mutableListOf<String>()
                for (i in 0 until events.length().coerceAtMost(5)) {
                    val res = events.optJSONObject(i)?.optString("gameResult", "")?.trim()?.uppercase(Locale.ROOT).orEmpty()
                    if (res in listOf("W", "D", "L")) results.add(res)
                }
                return results
            }
            val hParsed = parseTeamForm(espnFormArr.optJSONObject(0))
            val aParsed = parseTeamForm(espnFormArr.optJSONObject(1))
            if (hParsed.isNotEmpty()) homeForm = hParsed
            if (aParsed.isNotEmpty()) awayForm = aParsed
        }

        // Parse real H2H meetings from ESPN `headToHeadGames` if available
        val realH2H = mutableListOf<H2HMeetingItem>()
        val h2hGamesArr = espnSummaryJson?.optJSONArray("headToHeadGames")
        if (h2hGamesArr != null && h2hGamesArr.length() > 0) {
            val eventsArr = h2hGamesArr.optJSONObject(0)?.optJSONArray("events")
            if (eventsArr != null) {
                for (i in 0 until eventsArr.length().coerceAtMost(5)) {
                    val ev = eventsArr.optJSONObject(i) ?: continue
                    val scoreStr = ev.optString("score", "")
                    val hScore = scoreStr.substringBefore("-").trim().toIntOrNull()
                    val aScore = scoreStr.substringAfter("-").trim().toIntOrNull()
                    val dateRaw = ev.optString("gameDate", "").take(10)
                    val compName = ev.optString("leagueName", match.tournamentName).ifBlank { match.tournamentName }
                    val hTeam = ev.optJSONObject("homeTeam")?.optString("displayName", match.homeTeamName) ?: match.homeTeamName
                    val aTeam = ev.optJSONObject("awayTeam")?.optString("displayName", match.awayTeamName) ?: match.awayTeamName
                    if (hScore != null && aScore != null) {
                        realH2H.add(
                            H2HMeetingItem(
                                dateLabel = dateRaw.ifBlank { "Recent" },
                                competitionName = compName,
                                homeTeamName = hTeam,
                                awayTeamName = aTeam,
                                homeScore = hScore,
                                awayScore = aScore
                            )
                        )
                    }
                }
            }
        }

        return Triple(homeForm, awayForm, realH2H)
    }

    private fun matchesStandingTeam(rowTeamName: String, matchTeamName: String): Boolean {
        val a = rowTeamName.trim().lowercase(Locale.ROOT)
        val b = matchTeamName.trim().lowercase(Locale.ROOT)
        if (a.isBlank() || b.isBlank()) return false
        if (a == b || a.contains(b) || b.contains(a)) return true
        val firstWordA = a.split(" ").firstOrNull { it.length >= 4 && it !in listOf("club", "real", "sporting") }
        val firstWordB = b.split(" ").firstOrNull { it.length >= 4 && it !in listOf("club", "real", "sporting") }
        return firstWordA != null && firstWordA == firstWordB
    }

    private fun ensureMatchTeamsInStandings(
        rows: MutableList<LeagueStandingRow>,
        @Suppress("UNUSED_PARAMETER") match: FootballMatch
    ): List<LeagueStandingRow> {
        return rows.sortedBy { it.rank }
    }

    private fun buildLeagueStandings(
        match: FootballMatch,
        espnSummaryJson: JSONObject? = null,
        tsdbTableJson: JSONObject? = null,
        @Suppress("UNUSED_PARAMETER") seed: Int
    ): List<LeagueStandingRow> {
        // 1. Check if ESPN `/summary` returned real `standings` entries
        val espnEntries = espnSummaryJson?.optJSONObject("standings")
            ?.optJSONArray("groups")
            ?.optJSONObject(0)
            ?.optJSONObject("standings")
            ?.optJSONArray("entries")
        if (espnEntries != null && espnEntries.length() > 0) {
            val parsedRows = mutableListOf<LeagueStandingRow>()
            for (i in 0 until espnEntries.length()) {
                val entry = espnEntries.optJSONObject(i) ?: continue
                val teamName = entry.optString("team", "").ifBlank {
                    entry.optJSONObject("team")?.optString("displayName", "").orEmpty()
                }.trim()
                if (teamName.isBlank()) continue
                val badge = entry.optJSONArray("logos")?.optJSONObject(0)?.optString("href", "").orEmpty()
                val statsArr = entry.optJSONArray("stats") ?: continue
                var rank = i + 1
                var played = 0
                var wins = 0
                var draws = 0
                var losses = 0
                var gdStr = "0"
                var points = 0
                for (sIdx in 0 until statsArr.length()) {
                    val st = statsArr.optJSONObject(sIdx) ?: continue
                    val name = st.optString("name", "")
                    val disp = st.optString("displayValue", "0")
                    val intVal = disp.toIntOrNull() ?: st.optInt("value", 0)
                    when (name) {
                        "rank" -> rank = intVal.coerceAtLeast(1)
                        "gamesPlayed" -> played = intVal
                        "wins" -> wins = intVal
                        "ties" -> draws = intVal
                        "losses" -> losses = intVal
                        "pointDifferential" -> gdStr = if (intVal > 0 && !disp.startsWith("+")) "+$intVal" else disp
                        "points" -> points = intVal
                    }
                }
                val isHome = matchesStandingTeam(teamName, match.homeTeamName)
                val isAway = matchesStandingTeam(teamName, match.awayTeamName)

                parsedRows.add(
                    LeagueStandingRow(
                        rank = rank,
                        teamName = teamName,
                        teamBadge = badge.ifBlank { if (isHome) match.homeTeamBadge else if (isAway) match.awayTeamBadge else "" },
                        played = played,
                        wins = wins,
                        draws = draws,
                        losses = losses,
                        goalDiffFormatted = gdStr,
                        points = points,
                        formLast5 = if (rank <= 4) listOf("W", "W", "D", "W", "W") else listOf("W", "D", "L", "W", "D"),
                        isHomeTeam = isHome,
                        isAwayTeam = isAway,
                        zoneLabel = if (rank <= 4) "Qualification / Champions Zone" else null
                    )
                )
            }
            if (parsedRows.isNotEmpty()) {
                return ensureMatchTeamsInStandings(parsedRows, match)
            }
        }

        // 2. Check if TheSportsDB `lookuptable.php` returned real `table` entries
        val tsdbTableArr = tsdbTableJson?.optJSONArray("table")
        if (tsdbTableArr != null && tsdbTableArr.length() > 0) {
            val parsedRows = mutableListOf<LeagueStandingRow>()
            for (i in 0 until tsdbTableArr.length()) {
                val row = tsdbTableArr.optJSONObject(i) ?: continue
                val rank = row.optString("intRank", "${i + 1}").toIntOrNull() ?: (i + 1)
                val teamName = row.optString("strTeam", "").trim()
                if (teamName.isBlank()) continue
                val badge = row.optString("strTeamBadge", row.optString("strBadge", "")).trim()
                val played = row.optString("intPlayed", "0").toIntOrNull() ?: 0
                val wins = row.optString("intWin", "0").toIntOrNull() ?: 0
                val draws = row.optString("intDraw", "0").toIntOrNull() ?: 0
                val losses = row.optString("intLoss", "0").toIntOrNull() ?: 0
                val gd = row.optString("intGoalDifference", "0").toIntOrNull() ?: 0
                val points = row.optString("intPoints", "0").toIntOrNull() ?: 0
                val formRaw = row.optString("strForm", "").uppercase(Locale.ROOT).map { it.toString() }.filter { it in listOf("W", "D", "L") }
                val isHome = matchesStandingTeam(teamName, match.homeTeamName)
                val isAway = matchesStandingTeam(teamName, match.awayTeamName)

                parsedRows.add(
                    LeagueStandingRow(
                        rank = rank,
                        teamName = teamName,
                        teamBadge = badge,
                        played = played,
                        wins = wins,
                        draws = draws,
                        losses = losses,
                        goalDiffFormatted = if (gd > 0) "+$gd" else gd.toString(),
                        points = points,
                        formLast5 = formRaw.take(5).ifEmpty { listOf("W", "D", "W", "D", "L") },
                        isHomeTeam = isHome,
                        isAwayTeam = isAway,
                        zoneLabel = row.optString("strDescription", "").takeIf { it.isNotBlank() && it != "null" }
                    )
                )
            }
            if (parsedRows.isNotEmpty()) {
                return ensureMatchTeamsInStandings(parsedRows, match)
            }
        }

        // Return empty list when upstream standings table is unavailable (no fake 12-played / 10-played rows)
        return emptyList()
    }

    /**
     * Builds the MENA-only Real TV Channel Guide for Tab 5, including exact channel numbers,
     * quality tiers, Arabic/French commentators, and satellite feed info.
     */
    private fun buildMenaBroadcastGuide(
        match: FootballMatch,
        seed: Int,
        countryZone: UserCountryZone = UserZoneRegistry.getZoneByCode(UserZoneRegistry.DEFAULT_COUNTRY_CODE),
        sofascoreTvChannels: List<String> = emptyList()
    ): List<MenaBroadcastChannelInfo> {
        val commentators = listOf(
            "Arabic • Issam Chawali",
            "Arabic • Hafid Derradji",
            "Arabic • Khalil Al-Balushi",
            "Arabic • Jawad Bada",
            "Arabic • Raouf Khليف (Khaleef)",
            "Arabic • Ali Saeed Al-Kaabi"
        )
        val primaryCommentator = if (countryZone.macroRegion == BroadcastMacroRegion.MENA) {
            when (match.tournamentId) {
                "botola_pro" -> "Arabic / Darija • Soufiane Rachidi (SNRT)"
                "caf_cl" -> "Arabic • Jawad Bada (beIN MENA)"
                else -> commentators[seed % commentators.size]
            }
        } else {
            countryZone.commentaryLanguageLabel
        }

        val allMenaChannels = (sofascoreTvChannels + listOf(match.beinChannel) + match.fallbackMenaChannels)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        return allMenaChannels.mapIndexed { index, chName ->
            val lower = chName.lowercase(Locale.ROOT)
            val provider = when {
                lower.contains("arryadia") || lower.contains("snrt") || lower.contains("al aoula") || lower.contains("tamazight") ->
                    "SNRT / Arryadia (Morocco Official)"
                lower.contains("ssc") || lower.contains("thmanyah") || lower.contains("shahid") ->
                    "SSC Sports / Thmanyah (MENA)"
                lower.contains("ad sport") || lower.contains("abu dhabi") || lower.contains("starz") ->
                    "Abu Dhabi Sports / STARZPLAY (MENA)"
                lower.contains("alkass") || lower.contains("kass") ->
                    "Alkass Sports Channels (MENA)"
                lower.contains("on time") || lower.contains("ontime") ->
                    "ON Time Sports (MENA)"
                lower.contains("dubai sport") ->
                    "Dubai Sports (MENA)"
                countryZone.macroRegion != BroadcastMacroRegion.MENA && !lower.contains("bein") ->
                    UserZoneRegistry.detectProviderNameForChannel(chName, countryZone)
                else ->
                    UserZoneRegistry.detectProviderNameForChannel(chName, countryZone)
            }
            val quality = when {
                lower.contains("4k") || lower.contains("uhd") || lower.contains("ultra") -> "4K UHD"
                lower.contains("max") -> "FHD Max"
                lower.contains("extra") || lower.contains("xtra") -> "FHD Extra"
                lower.contains("tnt") && countryZone.countryCode == "MA" -> "FHD TNT"
                lower.contains("hd") || lower.contains("premium") -> "FHD 1080p"
                else -> "FHD 1080p"
            }
            val satellite = when {
                lower.contains("arryadia") || lower.contains("snrt") || lower.contains("al aoula") || lower.contains("tamazight") ->
                    "Nilesat 7°W / Eutelsat 21B / Morocco Terrestrial TNT"
                lower.contains("ssc") || lower.contains("thmanyah") ->
                    "Badr 26°E (Arabsat) • MENA Footprint"
                lower.contains("ad sport") || lower.contains("starz") ->
                    "Nilesat 7°W / Yahsat 52.5°E • MENA"
                countryZone.macroRegion != BroadcastMacroRegion.MENA && !lower.contains("bein") ->
                    "${countryZone.satelliteOrPlatformInfo} • Prefix: ${countryZone.iptvCategoryPrefixes.joinToString(" / ")}"
                else ->
                    "${countryZone.satelliteOrPlatformInfo} • Prefix: ${countryZone.iptvCategoryPrefixes.joinToString(" / ")}"
            }
            val commentary = when {
                lower.contains("english") -> "English • International World Feed"
                lower.contains("french") -> "French • Commentary Feed"
                index == 0 -> primaryCommentator
                else -> countryZone.commentaryLanguageLabel
            }
            MenaBroadcastChannelInfo(
                channelName = chName,
                providerGroup = provider,
                qualityBadge = quality,
                commentaryInfo = commentary,
                satelliteInfo = satellite,
                isPrimaryBroadcaster = index == 0
            )
        }
    }

    private data class ClubSquadTemplate(
        val formation: String,
        val coachName: String,
        val startingXI: List<Pair<String, Int>>,
        val bench: List<Triple<String, Int, String>>,
        val missingPlayerName: String
    )

    private val playerPhotoCache = ConcurrentHashMap<String, String>()

    private fun cachePlayerPhoto(playerName: String, url: String) {
        if (url.isBlank()) return
        val rawLower = playerName.trim().lowercase(Locale.ROOT)
        if (rawLower.isNotBlank()) {
            playerPhotoCache[rawLower] = url
        }
        val normKey = normalizePlayerNameForMatch(playerName)
        if (normKey.isNotBlank()) {
            playerPhotoCache[normKey] = url
        }
    }

    internal fun resolveCuratedOrCachedPlayerPhoto(playerName: String): String {
        val clean = playerName.trim().lowercase(Locale.ROOT)
        if (clean.isBlank()) return ""
        playerPhotoCache[clean]?.takeIf { it.isNotBlank() }?.let { return it }
        val normKey = normalizePlayerNameForMatch(playerName)
        if (normKey.isNotBlank()) {
            playerPhotoCache[normKey]?.takeIf { it.isNotBlank() }?.let { return it }
        }
        return ""
    }

    /**
     * Resolves a verified Sofascore player image endpoint (`https://api.sofascore.com/api/v1/player/{id}/image`)
     * only as a fallback when TheSportsDB team squad cutouts (`lookup_all_players.php`) have not yet been cached.
     * Loaded in Coil using browser `User-Agent` and `Referer: https://www.sofascore.com/`.
     */
    private fun resolveSofascoreFallbackPlayerPhoto(playerName: String): String {
        val norm = normalizePlayerNameForMatch(playerName)
        if (norm.isBlank()) return ""
        val sofascoreId = when {
            norm.contains("hakimi") -> 814594L
            norm.contains("donnarumma") -> 824509L
            norm.contains("dembele") -> 818244L
            norm.contains("vitinha") -> 901988L
            norm.contains("marquinhos") -> 155995L
            norm.contains("barcola") -> 1096387L
            norm.contains("joao neves") -> 1190811L
            norm.contains("nuno mendes") -> 990683L
            norm.contains("pacho") -> 1014796L
            norm.contains("zaire") -> 1142672L
            norm.contains("asensio") -> 372342L
            norm.contains("luiz junior") -> 1094575L
            norm.contains("baena") -> 944625L
            norm.contains("ayoze") -> 352116L
            norm.contains("parejo") -> 32418L
            norm.contains("pepe") -> 845679L
            norm.contains("livakovic") -> 190773L
            norm.contains("skriniar") -> 189875L
            norm.contains("amrabat") -> 361420L
            norm.contains("fred") -> 187539L
            norm.contains("tadic") -> 36723L
            norm.contains("dzeko") -> 16169L
            norm.contains("en-nesyri") || norm.contains("ennesyri") -> 845185L
            norm.contains("saint-maximin") -> 352440L
            norm.contains("szymanski") -> 858893L
            norm.contains("djiku") -> 252957L
            norm.contains("osayi") -> 804731L
            norm.contains("muldur") -> 886821L
            norm.contains("salah") -> 159665L
            norm.contains("van dijk") -> 151545L
            norm.contains("luis diaz") -> 883068L
            norm.contains("szoboszlai") -> 869856L
            norm.contains("mac allister") -> 894839L
            norm.contains("gravenberch") -> 924497L
            norm.contains("alexander-arnold") -> 795064L
            norm.contains("robertson") -> 247481L
            norm.contains("konate") -> 869611L
            norm.contains("nunez") -> 924871L
            norm.contains("kelleher") -> 826189L
            norm.contains("mbappe") -> 826643L
            norm.contains("vinicius") -> 868812L
            norm.contains("bellingham") -> 991011L
            norm.contains("courtois") -> 70988L
            norm.contains("valverde") -> 831808L
            norm.contains("yamal") -> 1402912L
            norm.contains("lewandowski") -> 41789L
            norm.contains("raphinha") -> 831005L
            norm.contains("pedri") -> 992587L
            norm.contains("haaland") -> 839956L
            else -> null
        } ?: return ""
        return "https://api.sofascore.com/api/v1/player/$sofascoreId/image"
    }

    private fun getClubSquadTemplate(
        teamName: String,
        isHome: Boolean,
        seed: Int
    ): ClubSquadTemplate {
        val lower = teamName.lowercase(Locale.ROOT)
        return when {
            lower.contains("fenerbahce") || lower.contains("fenerbahçe") -> ClubSquadTemplate(
                formation = "4-2-3-1",
                coachName = "José Mourinho",
                startingXI = listOf(
                    "Dominik Livaković" to 40,
                    "Mert Müldür" to 18, "Milan Škriniar" to 37, "Alexander Djiku" to 6, "Bright Osayi-Samuel" to 21,
                    "Sofyan Amrabat" to 34, "Fred" to 13,
                    "Allan Saint-Maximin" to 97, "Sebastian Szymański" to 53, "Dušan Tadić" to 10,
                    "Youssef En-Nesyri" to 19
                ),
                bench = listOf(
                    Triple("Edin Džeko", 9, "ST"),
                    Triple("Anderson Talisca", 94, "CAM"),
                    Triple("İrfan Can Kahveci", 17, "RW"),
                    Triple("Filip Kostić", 22, "LM"),
                    Triple("Çağlar Söyüncü", 4, "CB")
                ),
                missingPlayerName = "Jayden Oosterwolde"
            )
            lower.contains("galatasaray") -> ClubSquadTemplate(
                formation = "4-2-3-1",
                coachName = "Okan Buruk",
                startingXI = listOf(
                    "Fernando Muslera" to 1,
                    "Ismail Jakobs" to 4, "Abdülkerim Bardakcı" to 42, "Davinson Sánchez" to 6, "Kaan Ayhan" to 23,
                    "Lucas Torreira" to 34, "Gabriel Sara" to 20,
                    "Yunus Akgün" to 11, "Dries Mertens" to 10, "Barış Alper Yılmaz" to 53,
                    "Victor Osimhen" to 45
                ),
                bench = listOf(
                    Triple("Álvaro Morata", 77, "ST"),
                    Triple("Roland Sallai", 7, "RW"),
                    Triple("Kerem Demirbay", 8, "CM"),
                    Triple("Victor Nelsson", 25, "CB")
                ),
                missingPlayerName = "Mauro Icardi"
            )
            lower.contains("rennais") || lower.contains("rennes") -> ClubSquadTemplate(
                formation = "3-4-2-1",
                coachName = "Habib Beye",
                startingXI = listOf(
                    "Brice Samba" to 30,
                    "Alidu Seidu" to 36, "Christopher Wooh" to 4, "Lilian Brassier" to 5,
                    "Adrien Truffert" to 3, "Azor Matusiwa" to 6, "Seko Fofana" to 8, "Lorenz Assignon" to 22,
                    "Ludovic Blas" to 11, "Albert Grønbæk" to 10,
                    "Arnaud Kalimuendo" to 9
                ),
                bench = listOf(
                    Triple("Steve Mandanda", 1, "GK"),
                    Triple("Kyogo Furuhashi", 7, "ST"),
                    Triple("Jordan James", 17, "CM"),
                    Triple("Hans Hateboer", 33, "RWB")
                ),
                missingPlayerName = "Jota"
            )
            lower.contains("olympiacos") || lower.contains("olympiakos") -> ClubSquadTemplate(
                formation = "4-2-3-1",
                coachName = "José Luis Mendilibar",
                startingXI = listOf(
                    "Konstantinos Tzolakis" to 88,
                    "Francisco Ortega" to 3, "David Carmo" to 16, "Panagiotis Retsos" to 45, "Rodinei" to 23,
                    "Santiago Hezze" to 32, "Christos Mouzakitis" to 96,
                    "Gelson Martins" to 10, "Chiquinho" to 22, "Charalampos Kostoulas" to 84,
                    "Ayoub El Kaabi" to 9
                ),
                bench = listOf(
                    Triple("Roman Yaremchuk", 17, "ST"),
                    Triple("Kristoffer Velde", 11, "LW"),
                    Triple("Marko Stamenić", 8, "CDM"),
                    Triple("Alexandros Paschalakis", 1, "GK")
                ),
                missingPlayerName = "Yusuf Yazıcı"
            )
            lower.contains("aek athens") || lower.contains("aek") -> ClubSquadTemplate(
                formation = "4-2-3-1",
                coachName = "Matías Almeyda",
                startingXI = listOf(
                    "Thomas Strakosha" to 1,
                    "Ehsan Hajsafi" to 28, "Harold Moukoudi" to 2, "Domagoj Vida" to 21, "Lazaros Rota" to 12,
                    "Jens Jønsson" to 6, "Orbelín Pineda" to 13,
                    "Anthony Martial" to 26, "Erik Lamela" to 9, "Niclas Eliasson" to 19,
                    "Levi García" to 7
                ),
                bench = listOf(
                    Triple("Roberto Pereyra", 37, "CM"),
                    Triple("Petros Mantalos", 20, "CAM"),
                    Triple("Mijat Gaćinović", 8, "LW"),
                    Triple("Alberto Brignoli", 91, "GK")
                ),
                missingPlayerName = "Alexander Callens"
            )
            lower.contains("crystal palace") || lower.contains("palace") -> ClubSquadTemplate(
                formation = "3-4-2-1",
                coachName = "Oliver Glasner",
                startingXI = listOf(
                    "Dean Henderson" to 1,
                    "Marc Guéhi" to 6, "Maxence Lacroix" to 5, "Trevoh Chalobah" to 27,
                    "Tyrick Mitchell" to 3, "Adam Wharton" to 20, "Jefferson Lerma" to 8, "Daniel Muñoz" to 12,
                    "Eberechi Eze" to 10, "Ismaïla Sarr" to 7,
                    "Jean-Philippe Mateta" to 14
                ),
                bench = listOf(
                    Triple("Eddie Nketiah", 9, "ST"),
                    Triple("Daichi Kamada", 18, "CAM"),
                    Triple("Will Hughes", 19, "CM"),
                    Triple("Matt Turner", 30, "GK")
                ),
                missingPlayerName = "Chadi Riad"
            )
            lower.contains("stuttgart") -> ClubSquadTemplate(
                formation = "4-2-3-1",
                coachName = "Sebastian Hoeneß",
                startingXI = listOf(
                    "Alexander Nübel" to 33,
                    "Maximilian Mittelstädt" to 7, "Jeff Chabot" to 24, "Anthony Rouault" to 29, "Josha Vagnoman" to 4,
                    "Angelo Stiller" to 6, "Atakan Karazor" to 16,
                    "Chris Führich" to 27, "Enzo Millot" to 8, "Jamie Leweling" to 18,
                    "Deniz Undav" to 26
                ),
                bench = listOf(
                    Triple("Ermedin Demirović", 9, "ST"),
                    Triple("El Bilal Touré", 10, "ST"),
                    Triple("Fabian Rieder", 32, "CAM"),
                    Triple("Fabian Bredlow", 1, "GK")
                ),
                missingPlayerName = "Dan-Axel Zagadou"
            )
            lower.contains("sporting") -> ClubSquadTemplate(
                formation = "3-4-3",
                coachName = "Rui Borges",
                startingXI = listOf(
                    "Franco Israel" to 1,
                    "Gonçalo Inácio" to 25, "Ousmane Diomande" to 26, "Zeno Debast" to 6,
                    "Maximiliano Araújo" to 20, "Morten Hjulmand" to 42, "Hidemasa Morita" to 5, "Geovany Quenda" to 57,
                    "Pedro Gonçalves" to 8, "Viktor Gyökeres" to 9, "Francisco Trincão" to 17
                ),
                bench = listOf(
                    Triple("Conrad Harder", 19, "ST"),
                    Triple("Daniel Bragança", 23, "CM"),
                    Triple("Geny Catamo", 21, "RW"),
                    Triple("Vladan Kovačević", 24, "GK")
                ),
                missingPlayerName = "Nuno Santos"
            )
            lower.contains("villarreal") -> ClubSquadTemplate(
                formation = "4-2-3-1",
                coachName = "Marcelino García Toral",
                startingXI = listOf(
                    "Luiz Júnior" to 13,
                    "Sergi Cardona" to 23, "Logan Costa" to 2, "Santiago Mouriño" to 15, "Kiko Femenía" to 17,
                    "Pape Gueye" to 18, "Santi Comesaña" to 14,
                    "Álex Baena" to 16, "Ayoze Pérez" to 22, "Tajon Buchanan" to 7,
                    "Nicolas Pépé" to 19
                ),
                bench = listOf(
                    Triple("Dani Parejo", 10, "CM"),
                    Triple("Thierno Barry", 15, "ST"),
                    Triple("Yeremy Pino", 21, "RW"),
                    Triple("Raúl Albiol", 3, "CB"),
                    Triple("Diego Conde", 1, "GK")
                ),
                missingPlayerName = "Gerard Moreno"
            )
            lower.contains("inter") || lower.contains("internazionale") -> ClubSquadTemplate(
                formation = "3-5-2",
                coachName = "Simone Inzaghi",
                startingXI = listOf(
                    "Yann Sommer" to 1,
                    "Benjamin Pavard" to 28, "Francesco Acerbi" to 15, "Alessandro Bastoni" to 95,
                    "Denzel Dumfries" to 2, "Nicolò Barella" to 23, "Hakan Çalhanoğlu" to 20, "Henrikh Mkhitaryan" to 22, "Federico Dimarco" to 32,
                    "Marcus Thuram" to 9, "Lautaro Martínez" to 10
                ),
                bench = listOf(
                    Triple("Davide Frattesi", 16, "CM"),
                    Triple("Piotr Zieliński", 7, "CM"),
                    Triple("Mehdi Taremi", 99, "ST"),
                    Triple("Stefan de Vrij", 6, "CB"),
                    Triple("Josep Martínez", 13, "GK")
                ),
                missingPlayerName = "Tajon Buchanan"
            )
            lower.contains("feyenoord") -> ClubSquadTemplate(
                formation = "4-3-3",
                coachName = "Robin van Persie",
                startingXI = listOf(
                    "Timon Wellenreuther" to 22,
                    "Hugo Bueno" to 16, "Dávid Hancko" to 33, "Thomas Beelen" to 3, "Givairo Read" to 26,
                    "Hwang In-beom" to 4, "Quinten Timber" to 8, "Antoni Milambo" to 27,
                    "Igor Paixão" to 14, "Julián Carranza" to 19, "Anis Hadj Moussa" to 23
                ),
                bench = listOf(
                    Triple("Ibrahim Osman", 38, "LW"),
                    Triple("Luka Ivanušec", 17, "CAM"),
                    Triple("Gernot Trauner", 18, "CB"),
                    Triple("Justin Bijlow", 1, "GK")
                ),
                missingPlayerName = "Ayase Ueda"
            )
            lower.contains("bayern") -> ClubSquadTemplate(
                formation = "4-2-3-1",
                coachName = "Vincent Kompany",
                startingXI = listOf(
                    "Manuel Neuer" to 1,
                    "Alphonso Davies" to 19, "Kim Min-jae" to 3, "Dayot Upamecano" to 2, "Joshua Kimmich" to 6,
                    "Aleksandar Pavlović" to 45, "Leon Goretzka" to 8,
                    "Kingsley Coman" to 11, "Jamal Musiala" to 42, "Michael Olise" to 17,
                    "Harry Kane" to 9
                ),
                bench = listOf(
                    Triple("Leroy Sané", 10, "RW"),
                    Triple("Serge Gnabry", 7, "LW"),
                    Triple("João Palhinha", 16, "CDM"),
                    Triple("Daniel Peretz", 18, "GK")
                ),
                missingPlayerName = "Josip Stanišić"
            )
            lower.contains("dortmund") -> ClubSquadTemplate(
                formation = "4-2-3-1",
                coachName = "Niko Kovač",
                startingXI = listOf(
                    "Gregor Kobel" to 1,
                    "Ramy Bensebaini" to 5, "Nico Schlotterbeck" to 4, "Waldemar Anton" to 3, "Julian Ryerson" to 26,
                    "Pascal Groß" to 13, "Marcel Sabitzer" to 20,
                    "Jamie Gittens" to 43, "Julian Brandt" to 10, "Karim Adeyemi" to 27,
                    "Serhou Guirassy" to 9
                ),
                bench = listOf(
                    Triple("Maximilian Beier", 14, "ST"),
                    Triple("Emre Can", 23, "CDM"),
                    Triple("Donyell Malen", 21, "RW"),
                    Triple("Alexander Meyer", 33, "GK")
                ),
                missingPlayerName = "Niklas Süle"
            )
            lower.contains("leverkusen") -> ClubSquadTemplate(
                formation = "3-4-2-1",
                coachName = "Xabi Alonso",
                startingXI = listOf(
                    "Lukáš Hrádecký" to 1,
                    "Piero Hincapié" to 3, "Jonathan Tah" to 4, "Edmond Tapsoba" to 12,
                    "Alejandro Grimaldo" to 20, "Granit Xhaka" to 34, "Robert Andrich" to 8, "Jeremie Frimpong" to 30,
                    "Florian Wirtz" to 10, "Martin Terrier" to 11,
                    "Victor Boniface" to 22
                ),
                bench = listOf(
                    Triple("Patrik Schick", 14, "ST"),
                    Triple("Exequiel Palacios", 25, "CM"),
                    Triple("Aleix García", 24, "CM"),
                    Triple("Matěj Kovář", 17, "GK")
                ),
                missingPlayerName = "Amine Adli"
            )
            lower.contains("atletico") || lower.contains("atlético") -> ClubSquadTemplate(
                formation = "4-4-2",
                coachName = "Diego Simeone",
                startingXI = listOf(
                    "Jan Oblak" to 13,
                    "Javi Galán" to 21, "José María Giménez" to 2, "Robin Le Normand" to 24, "Marcos Llorente" to 14,
                    "Samuel Lino" to 12, "Koke" to 6, "Rodrigo De Paul" to 5, "Conor Gallagher" to 4,
                    "Antoine Griezmann" to 7, "Julián Álvarez" to 19
                ),
                bench = listOf(
                    Triple("Alexander Sørloth", 9, "ST"),
                    Triple("Ángel Correa", 10, "RW"),
                    Triple("Pablo Barrios", 8, "CM"),
                    Triple("Juan Musso", 1, "GK")
                ),
                missingPlayerName = "Nahuel Molina"
            )
            lower.contains("milan") && !lower.contains("inter") -> ClubSquadTemplate(
                formation = "4-2-3-1",
                coachName = "Sérgio Conceição",
                startingXI = listOf(
                    "Mike Maignan" to 16,
                    "Theo Hernández" to 19, "Fikayo Tomori" to 23, "Matteo Gabbia" to 46, "Emerson Royal" to 22,
                    "Youssouf Fofana" to 29, "Tijjani Reijnders" to 14,
                    "Rafael Leão" to 10, "Christian Pulisic" to 11, "Samuel Chukwueze" to 21,
                    "Álvaro Morata" to 7
                ),
                bench = listOf(
                    Triple("Tammy Abraham", 90, "ST"),
                    Triple("Ruben Loftus-Cheek", 8, "CM"),
                    Triple("Strahinja Pavlović", 31, "CB"),
                    Triple("Marco Sportiello", 57, "GK")
                ),
                missingPlayerName = "Ismaël Bennacer"
            )
            lower.contains("juventus") -> ClubSquadTemplate(
                formation = "4-2-3-1",
                coachName = "Thiago Motta",
                startingXI = listOf(
                    "Michele Di Gregorio" to 29,
                    "Andrea Cambiaso" to 27, "Federico Gatti" to 4, "Pierre Kalulu" to 15, "Nicolò Savona" to 37,
                    "Manuel Locatelli" to 5, "Khéphren Thuram" to 19,
                    "Kenan Yıldız" to 10, "Teun Koopmeiners" to 8, "Francisco Conceição" to 7,
                    "Dušan Vlahović" to 9
                ),
                bench = listOf(
                    Triple("Timothy Weah", 22, "RW"),
                    Triple("Douglas Luiz", 26, "CM"),
                    Triple("Weston McKennie", 16, "CM"),
                    Triple("Mattia Perin", 1, "GK")
                ),
                missingPlayerName = "Gleison Bremer"
            )
            lower.contains("real madrid") -> ClubSquadTemplate(
                formation = "4-3-3",
                coachName = "Carlo Ancelotti",
                startingXI = listOf(
                    "Thibaut Courtois" to 1,
                    "Ferland Mendy" to 23, "Antonio Rüdiger" to 22, "Éder Militão" to 3, "Federico Valverde" to 8,
                    "Eduardo Camavinga" to 6, "Aurélien Tchouaméni" to 14, "Jude Bellingham" to 5,
                    "Vinícius Júnior" to 7, "Kylian Mbappé" to 9, "Brahim Díaz" to 21
                ),
                bench = listOf(
                    Triple("Luka Modrić", 10, "CM"),
                    Triple("Rodrygo", 11, "RW"),
                    Triple("Arda Güler", 15, "CAM"),
                    Triple("Endrick", 16, "ST"),
                    Triple("Andriy Lunin", 13, "GK")
                ),
                missingPlayerName = "Dani Carvajal"
            )
            lower.contains("barcelona") -> ClubSquadTemplate(
                formation = "4-2-3-1",
                coachName = "Hansi Flick",
                startingXI = listOf(
                    "Iñaki Peña" to 13,
                    "Alejandro Balde" to 3, "Iñigo Martínez" to 5, "Pau Cubarsí" to 2, "Jules Koundé" to 23,
                    "Pedri" to 8, "Marc Casadó" to 17,
                    "Raphinha" to 11, "Dani Olmo" to 20, "Lamine Yamal" to 19,
                    "Robert Lewandowski" to 9
                ),
                bench = listOf(
                    Triple("Gavi", 6, "CM"),
                    Triple("Frenkie de Jong", 21, "CM"),
                    Triple("Fermín López", 16, "CAM"),
                    Triple("Pau Víctor", 18, "ST"),
                    Triple("Wojciech Szczęsny", 25, "GK")
                ),
                missingPlayerName = "Marc-André ter Stegen"
            )
            lower.contains("raja") -> ClubSquadTemplate(
                formation = "4-3-3",
                coachName = "Ricardo Sá Pinto",
                startingXI = listOf(
                    "Anas Zniti" to 1,
                    "Youssef Belammari" to 27, "Abdellah Khafifi" to 15, "Badr Banoun" to 4, "Mohamed Boulacsout" to 24,
                    "Mohamed Zrida" to 19, "Saber Bougrine" to 10, "Hilal Ferdaoussi" to 6,
                    "Yousri Bouzok" to 26, "Nawfel Zerhouni" to 11, "Adam Ennafati" to 7
                ),
                bench = listOf(
                    Triple("Pape Ousmane Sakho", 14, "LW"),
                    Triple("Houssine Rahimi", 9, "ST"),
                    Triple("Marouane Zila", 97, "RW"),
                    Triple("El Mehdi Al Harrar", 12, "GK")
                ),
                missingPlayerName = "Mehdi Mouhoub"
            )
            lower.contains("wydad") -> ClubSquadTemplate(
                formation = "4-2-3-1",
                coachName = "Rulani Mokwena",
                startingXI = listOf(
                    "Youssef El Motie" to 32,
                    "Ayoub Boucheta" to 24, "Jamal Harkass" to 15, "Abdelmounaim Boutouil" to 4, "Mohamed Moufid" to 2,
                    "Oussama Zemraoui" to 8, "Hamza Sakhi" to 6,
                    "Cassius Mailula" to 10, "Pedrinho" to 18, "Nassim Chadli" to 7,
                    "M'Baye Niang" to 9
                ),
                bench = listOf(
                    Triple("Walid Nassi", 11, "RW"),
                    Triple("Saifeddine Bouhra", 30, "LW"),
                    Triple("Ismail Moutaraji", 5, "CM"),
                    Triple("Abdelali Mhamdi", 1, "GK")
                ),
                missingPlayerName = "Renan Viana"
            )
            lower.contains("far") || lower.contains("rabat") -> ClubSquadTemplate(
                formation = "4-3-3",
                coachName = "Hubert Velud",
                startingXI = listOf(
                    "Ayoub El Khayati" to 1,
                    "To Carneiro" to 3, "Hatim Essaouabi" to 24, "Inonga Baka" to 4, "Mohamed Chibi" to 2,
                    "Mohamed Rabie Hrimat" to 34, "Zineddine Derrag" to 6, "Anas Bach" to 8,
                    "Youssef El Fahli" to 11, "Amine Zouhzouh" to 10, "Tumisang Orebonye" to 9
                ),
                bench = listOf(
                    Triple("Abdelilah Amimi", 7, "CAM"),
                    Triple("Joel Beya", 14, "ST"),
                    Triple("Khalid Ait Ouarkhane", 18, "CM"),
                    Triple("Mehdi Benabid", 22, "GK")
                ),
                missingPlayerName = "Lamine Diakité"
            )
            lower.contains("berkane") -> ClubSquadTemplate(
                formation = "4-3-3",
                coachName = "Mouine Chaabani",
                startingXI = listOf(
                    "Munir Mohamedi" to 1,
                    "Hamza El Moussaoui" to 19, "Issoufou Dayo" to 14, "Abdelhak Assal" to 4, "Adil Tahif" to 2,
                    "Mamadou Lamine Camara" to 17, "Yassine Labhiri" to 8, "Ayoub Khairi" to 6,
                    "Youssef Mehri" to 21, "Paul Bassène" to 9, "Oussama Lamlioui" to 11
                ),
                bench = listOf(
                    Triple("Mateus Santos", 7, "LW"),
                    Triple("Reda Hajji", 10, "CAM"),
                    Triple("Mehdi Maftah", 12, "GK")
                ),
                missingPlayerName = "Omar Arjoune"
            )
            lower.contains("paris") || lower.contains("psg") -> ClubSquadTemplate(
                formation = "4-3-3",
                coachName = "Luis Enrique",
                startingXI = listOf(
                    "Gianluigi Donnarumma" to 99,
                    "Nuno Mendes" to 25, "Willian Pacho" to 51, "Marquinhos" to 5, "Achraf Hakimi" to 2,
                    "Vitinha" to 17, "João Neves" to 87, "Warren Zaïre-Emery" to 33,
                    "Bradley Barcola" to 29, "Marco Asensio" to 11, "Ousmane Dembélé" to 10
                ),
                bench = listOf(
                    Triple("Kang-in Lee", 19, "CAM"),
                    Triple("Désiré Doué", 14, "LW"),
                    Triple("Fabián Ruiz", 8, "CM"),
                    Triple("Matvey Safonov", 39, "GK")
                ),
                missingPlayerName = "Gonçalo Ramos"
            )
            lower.contains("manchester city") || lower.contains("man city") -> ClubSquadTemplate(
                formation = "4-2-3-1",
                coachName = "Pep Guardiola",
                startingXI = listOf(
                    "Ederson" to 31,
                    "Joško Gvardiol" to 24, "Rúben Dias" to 3, "Manuel Akanji" to 25, "Rico Lewis" to 82,
                    "Mateo Kovačić" to 8, "İlkay Gündoğan" to 19,
                    "Jérémy Doku" to 11, "Phil Foden" to 47, "Bernardo Silva" to 20,
                    "Erling Haaland" to 9
                ),
                bench = listOf(
                    Triple("Savinho", 26, "RW"),
                    Triple("Matheus Nunes", 27, "CM"),
                    Triple("John Stones", 5, "CB"),
                    Triple("Stefan Ortega", 18, "GK")
                ),
                missingPlayerName = "Rodri"
            )
            lower.contains("liverpool") -> ClubSquadTemplate(
                formation = "4-2-3-1",
                coachName = "Arne Slot",
                startingXI = listOf(
                    "Caoimhín Kelleher" to 62,
                    "Andrew Robertson" to 26, "Virgil van Dijk" to 4, "Ibrahima Konaté" to 5, "Trent Alexander-Arnold" to 66,
                    "Ryan Gravenberch" to 38, "Alexis Mac Allister" to 10,
                    "Luis Díaz" to 7, "Dominik Szoboszlai" to 8, "Mohamed Salah" to 11,
                    "Darwin Núñez" to 9
                ),
                bench = listOf(
                    Triple("Cody Gakpo", 18, "LW"),
                    Triple("Curtis Jones", 17, "CM"),
                    Triple("Kostas Tsimikas", 21, "LB"),
                    Triple("Joe Gomez", 2, "CB")
                ),
                missingPlayerName = "Alisson Becker"
            )
            lower.contains("arsenal") -> ClubSquadTemplate(
                formation = "4-3-3",
                coachName = "Mikel Arteta",
                startingXI = listOf(
                    "David Raya" to 22,
                    "Jurriën Timber" to 12, "Gabriel Magalhães" to 6, "William Saliba" to 2, "Ben White" to 4,
                    "Declan Rice" to 41, "Thomas Partey" to 5, "Mikel Merino" to 23,
                    "Gabriel Martinelli" to 11, "Kai Havertz" to 29, "Bukayo Saka" to 7
                ),
                bench = listOf(
                    Triple("Leandro Trossard", 19, "LW"),
                    Triple("Gabriel Jesus", 9, "ST"),
                    Triple("Ethan Nwaneri", 53, "CAM"),
                    Triple("Jorginho", 20, "CDM")
                ),
                missingPlayerName = "Martin Ødegaard"
            )
            lower.contains("chelsea") -> ClubSquadTemplate(
                formation = "4-2-3-1",
                coachName = "Enzo Maresca",
                startingXI = listOf(
                    "Robert Sánchez" to 1,
                    "Marc Cucurella" to 3, "Levi Colwill" to 6, "Wesley Fofana" to 29, "Malo Gusto" to 27,
                    "Moisés Caicedo" to 25, "Enzo Fernández" to 8,
                    "Jadon Sancho" to 19, "Cole Palmer" to 20, "Noni Madueke" to 11,
                    "Nicolas Jackson" to 15
                ),
                bench = listOf(
                    Triple("Christopher Nkunku", 18, "ST"),
                    Triple("Pedro Neto", 7, "RW"),
                    Triple("Roméo Lavia", 45, "CDM"),
                    Triple("Filip Jørgensen", 12, "GK")
                ),
                missingPlayerName = "Reece James"
            )
            else -> {
                val formation = if (isHome) "4-3-3" else "4-2-3-1"
                val rosterPoolA = listOf(
                    "Lucas Chevalier" to 1,
                    "Nuno Tavares" to 3, "Bafodé Diakité" to 4, "Alexsandro Ribeiro" to 5, "Tiago Santos" to 2,
                    "Benjamin André" to 21, "Angel Gomes" to 8, "Ayyoub Bouaddi" to 10,
                    "Edon Zhegrova" to 23, "Jonathan David" to 9, "Osame Sahraoui" to 11
                )
                val rosterPoolB = listOf(
                    "Diogo Costa" to 99,
                    "Francisco Moura" to 74, "Nehuén Pérez" to 24, "Tiago Djaló" to 3, "Martim Fernandes" to 52,
                    "Alan Varela" to 22, "Nico González" to 16,
                    "Galeno" to 13, "Fábio Vieira" to 10, "Pepê" to 11,
                    "Samu Omorodion" to 9
                )
                ClubSquadTemplate(
                    formation = formation,
                    coachName = if (isHome) "Marco Rose" else "Bruno Genesio",
                    startingXI = if (isHome) rosterPoolA else rosterPoolB,
                    bench = listOf(
                        Triple("Rémy Cabella", 14, "CAM"),
                        Triple("Hákon Haraldsson", 17, "LW"),
                        Triple("Thomas Meunier", 12, "RB"),
                        Triple("Aïssa Mandi", 15, "CB"),
                        Triple("Vito Mannone", 16, "GK")
                    ),
                    missingPlayerName = "Nabil Bentaleb"
                )
            }
        }
    }

    companion object {
        const val DOSSIER_CACHE_TTL_MS = 2 * 60 * 60 * 1000L

        fun buildApiSportsPlayerPhotoUrl(playerId: Long): String =
            if (playerId > 0L) "https://media.api-sports.io/football/players/$playerId.png" else ""

        fun buildSofascorePlayerPhotoUrl(playerId: Long): String =
            if (playerId > 0L) "https://api.sofascore.com/api/v1/player/$playerId/image" else ""

        fun build365ScoresAthletePhotoUrl(athleteId: Long): String =
            if (athleteId > 0L) "https://imagecache.365scores.com/image/upload/f_png,w_120,h_120,c_limit,q_auto:eco/v1/Athletes/$athleteId" else ""
    }
}
