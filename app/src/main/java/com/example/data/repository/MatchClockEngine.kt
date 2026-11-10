package com.example.data.repository

import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

/**
 * Canonical match lifecycle phases for the epoch-anchored local match clock & horizon state machine.
 */
enum class MatchPeriodPhase(val progressionOrder: Int) {
    PRE_MATCH(0),
    FIRST_HALF(1),
    FIRST_HALF_STOPPAGE(2),
    HALFTIME_BREAK(3),
    SECOND_HALF(4),
    SECOND_HALF_STOPPAGE(5),
    EXTRA_TIME(6),
    PENALTIES(7),
    FULL_TIME(8)
}

/**
 * Immutable epoch-anchored reference point for a match's live clock.
 *
 * Instead of naively incrementing a string regex every poll cycle or fabricating halftime/full-time
 * transitions from wall-clock guesses, [MatchClockAnchor] stores the verified provider phase and
 * derives [periodStartEpochMs] so any UI surface can render a smooth 1Hz `MM:SS` clock while
 * respecting strict period horizon boundaries (`45:00` -> `45:00+MM:SS`, `HT` hold, `90:00` -> `90:00+MM:SS`).
 */
data class MatchClockAnchor(
    val matchId: String,
    val phase: MatchPeriodPhase,
    val periodStartEpochMs: Long,          // Derived epoch when 1H (min 0) or 2H (min 45) or ET (min 90) started
    val verifiedMinuteAtSync: Int,         // Provider-verified minute at sync (e.g., 12, 45, 67)
    val verifiedAtEpochMs: Long,           // Wall-clock epoch when provider state was verified
    val officialAddedMinutes: Int? = null, // Verified fourth-official stoppage time (e.g., 4 for +4', 7 for +7')
    val providerName: String = "Primary",
    val providerAuthorityRank: Int = 50
)

/**
 * Evaluated clock state at a specific instant (`nowEpochMs`) produced by [MatchClockEngine.evaluateClockAt].
 */
data class MatchClockSnapshot(
    val phase: MatchPeriodPhase,
    val baseMinute: Int,
    val stoppageMinute: Int,
    val subMinuteSeconds: Int,
    val officialAddedMinutes: Int?,
    val isAwaitingProviderPhaseConfirmation: Boolean,
    val formattedMinuteBadge: String,
    val formattedSecondsClock: String,
    val periodHeaderBadge: String,
    val progressFraction: Float
)

/**
 * Epoch-anchored local match clock & provider authority engine for Kickoff.
 *
 * Key Guarantees:
 * 1. **Epoch-Anchored Continuity**: Derives `periodStartEpochMs` from verified upstream telemetry so
 *    1Hz `MM:SS` ticks remain monotonic across polls and screen transitions.
 * 2. **Strict Horizon Verification Guard**:
 *    - When 1st Half elapsed time crosses `45:00`, transitions to [MatchPeriodPhase.FIRST_HALF_STOPPAGE]
 *      (`45:00+MM:SS`) and awaits upstream `"HT"` confirmation (never invents halftime locally).
 *    - During [MatchPeriodPhase.HALFTIME_BREAK], holds strictly at `"HT"` until upstream confirms `"2H"`.
 *    - When 2nd Half elapsed time crosses `90:00`, transitions to [MatchPeriodPhase.SECOND_HALF_STOPPAGE]
 *      (`90:00+MM:SS`) and awaits upstream `"FT"` confirmation (never invents full-time locally).
 * 3. **Provider Authority Hierarchy**: Resolves conflicts across multi-source feeds (`API-Football` >
 *    `365Scores` > `SportAPI.ai` > `ESPN` > `Football-Data.org` > `TheSportsDB`) without regressing
 *    verified match phases or live goal counts.
 */
object MatchClockEngine {

    private val anchorRegistry = ConcurrentHashMap<String, MatchClockAnchor>()

    const val AUTHORITY_API_FOOTBALL = 100
    const val AUTHORITY_365_SCORES = 92
    const val AUTHORITY_SPORT_API_AI = 88
    const val AUTHORITY_ESPN = 82
    const val AUTHORITY_FOOTBALL_DATA_ORG = 74
    const val AUTHORITY_THESPORTSDB = 50
    const val AUTHORITY_FALLBACK = 10

    fun clearRegistryForTests() {
        anchorRegistry.clear()
    }

    fun getAnchor(matchId: String): MatchClockAnchor? = anchorRegistry[matchId]

    /**
     * Determines the upstream provider name and authority rank for a [FootballMatch] record.
     */
    fun resolveProviderAuthority(
        match: FootballMatch,
        explicitProvider: String? = null
    ): Pair<String, Int> {
        if (!explicitProvider.isNullOrBlank()) {
            val rank = when (explicitProvider.lowercase(Locale.ROOT)) {
                "api-football", "apifootball" -> AUTHORITY_API_FOOTBALL
                "365scores" -> AUTHORITY_365_SCORES
                "sportapi.ai", "sportapi" -> AUTHORITY_SPORT_API_AI
                "espn" -> AUTHORITY_ESPN
                "football-data.org", "footballdata" -> AUTHORITY_FOOTBALL_DATA_ORG
                "thesportsdb", "tsdb" -> AUTHORITY_THESPORTSDB
                else -> AUTHORITY_THESPORTSDB
            }
            return explicitProvider to rank
        }
        val id = match.matchId.lowercase(Locale.ROOT)
        return when {
            id.startsWith("apif_") || match.apiFootballFixtureId.isNotBlank() ->
                "API-Football" to AUTHORITY_API_FOOTBALL
            id.startsWith("365_") ->
                "365Scores" to AUTHORITY_365_SCORES
            id.startsWith("sportapi_") ->
                "SportAPI.ai" to AUTHORITY_SPORT_API_AI
            id.startsWith("espn_") || match.espnEventId.isNotBlank() ->
                "ESPN" to AUTHORITY_ESPN
            id.startsWith("fdorg_") ->
                "Football-Data.org" to AUTHORITY_FOOTBALL_DATA_ORG
            id.startsWith("tsdb_") || match.sportsDbEventId.isNotBlank() ->
                "TheSportsDB" to AUTHORITY_THESPORTSDB
            else ->
                "Primary" to AUTHORITY_FALLBACK
        }
    }

    /**
     * Parses a [FootballMatch]'s status and `liveMinuteOrBadge` into a [MatchClockAnchor] anchored at [nowEpochMs].
     */
    fun deriveAnchorFromMatch(
        match: FootballMatch,
        nowEpochMs: Long = System.currentTimeMillis(),
        providerName: String? = null,
        deterministicSubMinuteSeed: Boolean = true
    ): MatchClockAnchor {
        val (resolvedProvider, authorityRank) = resolveProviderAuthority(match, providerName)
        val cleanBadge = match.liveMinuteOrBadge.trim()
        val upperBadge = cleanBadge.uppercase(Locale.ROOT)

        val seedSeconds = if (deterministicSubMinuteSeed) {
            (abs(match.matchId.hashCode()) % 52).toLong()
        } else {
            0L
        }

        if (match.status == MatchStatus.UPCOMING) {
            val koEpoch = if (match.kickoffEpochMs > 0L) match.kickoffEpochMs else nowEpochMs
            return MatchClockAnchor(
                matchId = match.matchId,
                phase = MatchPeriodPhase.PRE_MATCH,
                periodStartEpochMs = koEpoch,
                verifiedMinuteAtSync = 0,
                verifiedAtEpochMs = nowEpochMs,
                officialAddedMinutes = null,
                providerName = resolvedProvider,
                providerAuthorityRank = authorityRank
            )
        }

        if (match.status == MatchStatus.FINISHED || upperBadge == "FT" || upperBadge == "AET" || upperBadge == "FINAL") {
            return MatchClockAnchor(
                matchId = match.matchId,
                phase = MatchPeriodPhase.FULL_TIME,
                periodStartEpochMs = nowEpochMs - 90 * 60_000L,
                verifiedMinuteAtSync = 90,
                verifiedAtEpochMs = nowEpochMs,
                officialAddedMinutes = null,
                providerName = resolvedProvider,
                providerAuthorityRank = authorityRank
            )
        }

        if (match.status == MatchStatus.HALFTIME || upperBadge == "HT" || upperBadge == "HALF TIME") {
            return MatchClockAnchor(
                matchId = match.matchId,
                phase = MatchPeriodPhase.HALFTIME_BREAK,
                periodStartEpochMs = nowEpochMs - 45 * 60_000L,
                verifiedMinuteAtSync = 45,
                verifiedAtEpochMs = nowEpochMs,
                officialAddedMinutes = null,
                providerName = resolvedProvider,
                providerAuthorityRank = authorityRank
            )
        }

        if (upperBadge.contains("PEN")) {
            return MatchClockAnchor(
                matchId = match.matchId,
                phase = MatchPeriodPhase.PENALTIES,
                periodStartEpochMs = nowEpochMs - 120 * 60_000L,
                verifiedMinuteAtSync = 120,
                verifiedAtEpochMs = nowEpochMs,
                officialAddedMinutes = null,
                providerName = resolvedProvider,
                providerAuthorityRank = authorityRank
            )
        }

        // Check for stoppage format: "45:00+02:15", "45+2'", "1H • 45+3'", "90+6'"
        val explicitStoppageMmSs = Regex("(\\d{1,3}):(\\d{2})\\+(\\d{1,2}):(\\d{2})").find(cleanBadge)
        val apostropheStoppage = Regex("(\\d{1,3})\\+(\\d{1,2})\\s*'").find(cleanBadge)

        if (explicitStoppageMmSs != null || apostropheStoppage != null) {
            val baseMin = explicitStoppageMmSs?.groupValues?.get(1)?.toIntOrNull()
                ?: apostropheStoppage?.groupValues?.get(1)?.toIntOrNull()
                ?: 45
            val stopMin = explicitStoppageMmSs?.groupValues?.get(3)?.toIntOrNull()
                ?: apostropheStoppage?.groupValues?.get(2)?.toIntOrNull()
                ?: 1
            val stopSec = explicitStoppageMmSs?.groupValues?.get(4)?.toIntOrNull()?.toLong() ?: seedSeconds
            val isSecondHalfStoppage = baseMin >= 90 || upperBadge.contains("2H")
            val phase = if (isSecondHalfStoppage) {
                MatchPeriodPhase.SECOND_HALF_STOPPAGE
            } else {
                MatchPeriodPhase.FIRST_HALF_STOPPAGE
            }
            val periodDurationMin = 45L
            val elapsedInPeriodMs = (periodDurationMin + stopMin) * 60_000L + stopSec * 1_000L
            return MatchClockAnchor(
                matchId = match.matchId,
                phase = phase,
                periodStartEpochMs = nowEpochMs - elapsedInPeriodMs,
                verifiedMinuteAtSync = baseMin + stopMin,
                verifiedAtEpochMs = nowEpochMs,
                officialAddedMinutes = stopMin,
                providerName = resolvedProvider,
                providerAuthorityRank = authorityRank
            )
        }

        // Standard minute format: "19:24", "1H • 38'", "2H • 86'", "67'"
        val explicitMmSs = Regex("(\\d{1,3}):(\\d{2})").find(cleanBadge)
        val apostropheMin = Regex("(\\d{1,3})\\s*'").find(cleanBadge)
        val withoutHalfPrefix = cleanBadge.replace(Regex("\\b[12]H\\b", RegexOption.IGNORE_CASE), "")
        val fallbackMin = Regex("\\d{1,3}").find(withoutHalfPrefix)?.value?.toIntOrNull()

        val parsedMinute = explicitMmSs?.groupValues?.get(1)?.toIntOrNull()
            ?: apostropheMin?.groupValues?.get(1)?.toIntOrNull()
            ?: fallbackMin

        val explicitSec = explicitMmSs?.groupValues?.get(2)?.toIntOrNull()?.toLong() ?: seedSeconds

        if (parsedMinute != null) {
            return when {
                parsedMinute > 90 || upperBadge.contains("ET") -> {
                    val elapsedEtMin = (parsedMinute - 90).coerceAtLeast(0)
                    MatchClockAnchor(
                        matchId = match.matchId,
                        phase = MatchPeriodPhase.EXTRA_TIME,
                        periodStartEpochMs = nowEpochMs - (elapsedEtMin * 60_000L + explicitSec * 1_000L),
                        verifiedMinuteAtSync = parsedMinute,
                        verifiedAtEpochMs = nowEpochMs,
                        officialAddedMinutes = null,
                        providerName = resolvedProvider,
                        providerAuthorityRank = authorityRank
                    )
                }
                parsedMinute > 45 || upperBadge.contains("2H") -> {
                    val elapsed2HMin = (parsedMinute - 45).coerceAtLeast(0)
                    MatchClockAnchor(
                        matchId = match.matchId,
                        phase = MatchPeriodPhase.SECOND_HALF,
                        periodStartEpochMs = nowEpochMs - (elapsed2HMin * 60_000L + explicitSec * 1_000L),
                        verifiedMinuteAtSync = parsedMinute,
                        verifiedAtEpochMs = nowEpochMs,
                        officialAddedMinutes = null,
                        providerName = resolvedProvider,
                        providerAuthorityRank = authorityRank
                    )
                }
                else -> {
                    val elapsed1HMin = parsedMinute.coerceAtLeast(1)
                    MatchClockAnchor(
                        matchId = match.matchId,
                        phase = MatchPeriodPhase.FIRST_HALF,
                        periodStartEpochMs = nowEpochMs - (elapsed1HMin * 60_000L + explicitSec * 1_000L),
                        verifiedMinuteAtSync = elapsed1HMin,
                        verifiedAtEpochMs = nowEpochMs,
                        officialAddedMinutes = null,
                        providerName = resolvedProvider,
                        providerAuthorityRank = authorityRank
                    )
                }
            }
        }

        // Fallback when upstream says LIVE / 1H / 2H without an explicit minute integer:
        // Anchor against kickoffEpochMs if known and realistic, without inventing HT or FT transitions.
        val isExplicit2H = upperBadge.contains("2H")
        if (isExplicit2H) {
            return MatchClockAnchor(
                matchId = match.matchId,
                phase = MatchPeriodPhase.SECOND_HALF,
                periodStartEpochMs = nowEpochMs - (15 * 60_000L + seedSeconds * 1_000L),
                verifiedMinuteAtSync = 60,
                verifiedAtEpochMs = nowEpochMs,
                officialAddedMinutes = null,
                providerName = resolvedProvider,
                providerAuthorityRank = authorityRank
            )
        }

        val elapsedFromKickoffMin = if (match.kickoffEpochMs > 0L && nowEpochMs > match.kickoffEpochMs) {
            ((nowEpochMs - match.kickoffEpochMs) / 60_000L).toInt()
        } else {
            15
        }
        val safe1HMin = elapsedFromKickoffMin.coerceIn(1, 45)
        return MatchClockAnchor(
            matchId = match.matchId,
            phase = MatchPeriodPhase.FIRST_HALF,
            periodStartEpochMs = nowEpochMs - (safe1HMin * 60_000L + seedSeconds * 1_000L),
            verifiedMinuteAtSync = safe1HMin,
            verifiedAtEpochMs = nowEpochMs,
            officialAddedMinutes = null,
            providerName = resolvedProvider,
            providerAuthorityRank = authorityRank
        )
    }

    /**
     * Registers or updates the [MatchClockAnchor] for [match] in the thread-safe anchor registry.
     *
     * Preserves monotonic sub-minute continuity if the match is already in the same phase and the
     * newly polled minute is within +/- 90 seconds of the locally running anchored clock.
     */
    fun registerOrUpdateAnchor(
        match: FootballMatch,
        nowEpochMs: Long = System.currentTimeMillis(),
        providerName: String? = null
    ): MatchClockAnchor {
        val candidate = deriveAnchorFromMatch(
            match = match,
            nowEpochMs = nowEpochMs,
            providerName = providerName
        )
        val existing = anchorRegistry[match.matchId]
        if (existing == null) {
            anchorRegistry[match.matchId] = candidate
            return candidate
        }

        // Never regress from FULL_TIME back to an earlier phase unless explicitly forced
        if (existing.phase == MatchPeriodPhase.FULL_TIME && candidate.phase != MatchPeriodPhase.FULL_TIME) {
            return existing
        }

        // Prevent lower-authority provider without explicit minute from regressing a advanced phase
        if (candidate.phase.progressionOrder < existing.phase.progressionOrder &&
            candidate.providerAuthorityRank < existing.providerAuthorityRank
        ) {
            return existing
        }

        // If the match is still in the same running half and the drift between the existing anchor
        // and the new provider minute is small (<= 90 seconds), keep `existing.periodStartEpochMs`
        // so the 1Hz MM:SS display never jumps or resets its seconds counter mid-minute.
        if (existing.phase == candidate.phase &&
            (candidate.phase == MatchPeriodPhase.FIRST_HALF || candidate.phase == MatchPeriodPhase.SECOND_HALF)
        ) {
            val driftMs = abs(existing.periodStartEpochMs - candidate.periodStartEpochMs)
            if (driftMs <= 90_000L) {
                val updated = existing.copy(
                    verifiedMinuteAtSync = maxOf(existing.verifiedMinuteAtSync, candidate.verifiedMinuteAtSync),
                    verifiedAtEpochMs = nowEpochMs,
                    officialAddedMinutes = candidate.officialAddedMinutes ?: existing.officialAddedMinutes,
                    providerName = if (candidate.providerAuthorityRank >= existing.providerAuthorityRank) {
                        candidate.providerName
                    } else {
                        existing.providerName
                    },
                    providerAuthorityRank = maxOf(existing.providerAuthorityRank, candidate.providerAuthorityRank)
                )
                anchorRegistry[match.matchId] = updated
                return updated
            }
        }

        anchorRegistry[match.matchId] = candidate
        return candidate
    }

    /**
     * Evaluates the match clock at [nowEpochMs] with strict horizon verification guards:
     * - 1st Half never self-transitions to `"HT"`; past 45:00 it enters `FIRST_HALF_STOPPAGE` (`45:00+MM:SS`).
     * - Halftime Break holds at `"HT"` until upstream confirms 2nd Half kickoff.
     * - 2nd Half never self-transitions to `"FT"`; past 90:00 it enters `SECOND_HALF_STOPPAGE` (`90:00+MM:SS`).
     */
    fun evaluateClockAt(
        anchor: MatchClockAnchor,
        nowEpochMs: Long = System.currentTimeMillis()
    ): MatchClockSnapshot {
        return when (anchor.phase) {
            MatchPeriodPhase.PRE_MATCH -> {
                MatchClockSnapshot(
                    phase = MatchPeriodPhase.PRE_MATCH,
                    baseMinute = 0,
                    stoppageMinute = 0,
                    subMinuteSeconds = 0,
                    officialAddedMinutes = null,
                    isAwaitingProviderPhaseConfirmation = false,
                    formattedMinuteBadge = "KO",
                    formattedSecondsClock = "00:00",
                    periodHeaderBadge = "KICKOFF",
                    progressFraction = 0f
                )
            }

            MatchPeriodPhase.HALFTIME_BREAK -> {
                MatchClockSnapshot(
                    phase = MatchPeriodPhase.HALFTIME_BREAK,
                    baseMinute = 45,
                    stoppageMinute = 0,
                    subMinuteSeconds = 0,
                    officialAddedMinutes = null,
                    isAwaitingProviderPhaseConfirmation = true,
                    formattedMinuteBadge = "HT",
                    formattedSecondsClock = "HT",
                    periodHeaderBadge = "HALF TIME",
                    progressFraction = 0.50f
                )
            }

            MatchPeriodPhase.FULL_TIME -> {
                MatchClockSnapshot(
                    phase = MatchPeriodPhase.FULL_TIME,
                    baseMinute = 90,
                    stoppageMinute = 0,
                    subMinuteSeconds = 0,
                    officialAddedMinutes = null,
                    isAwaitingProviderPhaseConfirmation = false,
                    formattedMinuteBadge = "FT",
                    formattedSecondsClock = "FT",
                    periodHeaderBadge = "FULL TIME",
                    progressFraction = 1.0f
                )
            }

            MatchPeriodPhase.PENALTIES -> {
                MatchClockSnapshot(
                    phase = MatchPeriodPhase.PENALTIES,
                    baseMinute = 120,
                    stoppageMinute = 0,
                    subMinuteSeconds = 0,
                    officialAddedMinutes = null,
                    isAwaitingProviderPhaseConfirmation = true,
                    formattedMinuteBadge = "PEN",
                    formattedSecondsClock = "PEN",
                    periodHeaderBadge = "PENALTIES",
                    progressFraction = 1.0f
                )
            }

            MatchPeriodPhase.FIRST_HALF, MatchPeriodPhase.FIRST_HALF_STOPPAGE -> {
                val elapsedSec = ((nowEpochMs - anchor.periodStartEpochMs) / 1000L).coerceAtLeast(0L)
                val regulationHorizonSec = 45L * 60L
                if (elapsedSec < regulationHorizonSec && anchor.phase == MatchPeriodPhase.FIRST_HALF) {
                    val totalMin = (elapsedSec / 60L).toInt().coerceIn(1, 45)
                    val sec = (elapsedSec % 60L).toInt()
                    MatchClockSnapshot(
                        phase = MatchPeriodPhase.FIRST_HALF,
                        baseMinute = totalMin,
                        stoppageMinute = 0,
                        subMinuteSeconds = sec,
                        officialAddedMinutes = anchor.officialAddedMinutes,
                        isAwaitingProviderPhaseConfirmation = false,
                        formattedMinuteBadge = "${totalMin}'",
                        formattedSecondsClock = String.format(Locale.US, "%02d:%02d", elapsedSec / 60L, sec),
                        periodHeaderBadge = "1ST HALF",
                        progressFraction = (totalMin / 90f).coerceIn(0.06f, 0.50f)
                    )
                } else {
                    // Horizon Guard: Crossed 45:00 without upstream "HT" confirmation -> FIRST_HALF_STOPPAGE
                    val overSec = (elapsedSec - regulationHorizonSec).coerceAtLeast(0L)
                    val stopMin = ((overSec / 60L).toInt()).coerceAtLeast(anchor.officialAddedMinutes ?: 1)
                    val stopDisplayMin = (overSec / 60L).toInt()
                    val stopDisplaySec = (overSec % 60L).toInt()
                    MatchClockSnapshot(
                        phase = MatchPeriodPhase.FIRST_HALF_STOPPAGE,
                        baseMinute = 45,
                        stoppageMinute = stopMin.coerceAtLeast(1),
                        subMinuteSeconds = stopDisplaySec,
                        officialAddedMinutes = anchor.officialAddedMinutes,
                        isAwaitingProviderPhaseConfirmation = true,
                        formattedMinuteBadge = "45+${stopDisplayMin.coerceAtLeast(1)}'",
                        formattedSecondsClock = String.format(
                            Locale.US,
                            "45:00+%02d:%02d",
                            stopDisplayMin,
                            stopDisplaySec
                        ),
                        periodHeaderBadge = "STOPPAGE",
                        progressFraction = 0.50f
                    )
                }
            }

            MatchPeriodPhase.SECOND_HALF, MatchPeriodPhase.SECOND_HALF_STOPPAGE -> {
                val elapsed2HSec = ((nowEpochMs - anchor.periodStartEpochMs) / 1000L).coerceAtLeast(0L)
                val totalMatchSec = 45L * 60L + elapsed2HSec
                val regulationHorizonSec = 90L * 60L
                if (totalMatchSec < regulationHorizonSec && anchor.phase == MatchPeriodPhase.SECOND_HALF) {
                    val totalMin = (totalMatchSec / 60L).toInt().coerceIn(45, 90)
                    val sec = (totalMatchSec % 60L).toInt()
                    MatchClockSnapshot(
                        phase = MatchPeriodPhase.SECOND_HALF,
                        baseMinute = totalMin,
                        stoppageMinute = 0,
                        subMinuteSeconds = sec,
                        officialAddedMinutes = anchor.officialAddedMinutes,
                        isAwaitingProviderPhaseConfirmation = false,
                        formattedMinuteBadge = "${totalMin}'",
                        formattedSecondsClock = String.format(Locale.US, "%02d:%02d", totalMatchSec / 60L, sec),
                        periodHeaderBadge = "2ND HALF",
                        progressFraction = (totalMin / 90f).coerceIn(0.50f, 0.99f)
                    )
                } else {
                    // Horizon Guard: Crossed 90:00 without upstream "FT" confirmation -> SECOND_HALF_STOPPAGE
                    val overSec = (totalMatchSec - regulationHorizonSec).coerceAtLeast(0L)
                    val stopMin = ((overSec / 60L).toInt()).coerceAtLeast(anchor.officialAddedMinutes ?: 1)
                    val stopDisplayMin = (overSec / 60L).toInt()
                    val stopDisplaySec = (overSec % 60L).toInt()
                    MatchClockSnapshot(
                        phase = MatchPeriodPhase.SECOND_HALF_STOPPAGE,
                        baseMinute = 90,
                        stoppageMinute = stopMin.coerceAtLeast(1),
                        subMinuteSeconds = stopDisplaySec,
                        officialAddedMinutes = anchor.officialAddedMinutes,
                        isAwaitingProviderPhaseConfirmation = true,
                        formattedMinuteBadge = "90+${stopDisplayMin.coerceAtLeast(1)}'",
                        formattedSecondsClock = String.format(
                            Locale.US,
                            "90:00+%02d:%02d",
                            stopDisplayMin,
                            stopDisplaySec
                        ),
                        periodHeaderBadge = "STOPPAGE",
                        progressFraction = 1.0f
                    )
                }
            }

            MatchPeriodPhase.EXTRA_TIME -> {
                val elapsedEtSec = ((nowEpochMs - anchor.periodStartEpochMs) / 1000L).coerceAtLeast(0L)
                val totalMatchSec = 90L * 60L + elapsedEtSec
                val totalMin = (totalMatchSec / 60L).toInt().coerceIn(91, 125)
                val sec = (totalMatchSec % 60L).toInt()
                MatchClockSnapshot(
                    phase = MatchPeriodPhase.EXTRA_TIME,
                    baseMinute = totalMin,
                    stoppageMinute = 0,
                    subMinuteSeconds = sec,
                    officialAddedMinutes = anchor.officialAddedMinutes,
                    isAwaitingProviderPhaseConfirmation = false,
                    formattedMinuteBadge = "${totalMin}'",
                    formattedSecondsClock = String.format(Locale.US, "%02d:%02d", totalMatchSec / 60L, sec),
                    periodHeaderBadge = "EXTRA TIME",
                    progressFraction = 1.0f
                )
            }
        }
    }

    /**
     * Evaluates whether [candidate] should win live state authority over [existing] when merging
     * multi-provider match records (`API-Football`, `365Scores`, `SportAPI.ai`, `ESPN`, `Football-Data.org`, `TheSportsDB`).
     */
    fun shouldPreferCandidateByAuthority(
        existing: FootballMatch,
        candidate: FootballMatch
    ): Boolean {
        // 1. Live or Finished always beats Upcoming
        if (existing.status == MatchStatus.UPCOMING && candidate.status != MatchStatus.UPCOMING) return true
        if (candidate.status == MatchStatus.UPCOMING && existing.status != MatchStatus.UPCOMING) return false

        // 2. Verified Finished state wins over earlier states
        if (candidate.status == MatchStatus.FINISHED && existing.status != MatchStatus.FINISHED) return true
        if (existing.status == MatchStatus.FINISHED && candidate.status != MatchStatus.FINISHED) return false

        // 3. Explicit minute badge beats generic "LIVE" / "1H" / "2H" without digits
        val existingHasExplicitMinute = existing.liveMinuteOrBadge.contains("'") && existing.liveMinuteOrBadge.any { it.isDigit() }
        val candidateHasExplicitMinute = candidate.liveMinuteOrBadge.contains("'") && candidate.liveMinuteOrBadge.any { it.isDigit() }
        if (candidateHasExplicitMinute && !existingHasExplicitMinute) return true
        if (existingHasExplicitMinute && !candidateHasExplicitMinute) return false

        // 4. Higher verified live goal count wins (goals are monotonic except rare VAR disallow, handled by higher authority)
        val existingTotalGoals = (existing.homeScore ?: 0) + (existing.awayScore ?: 0)
        val candidateTotalGoals = (candidate.homeScore ?: 0) + (candidate.awayScore ?: 0)
        if (candidateTotalGoals > existingTotalGoals) return true
        if (existingTotalGoals > candidateTotalGoals) return false

        // 5. Phase progression order (e.g., 2H > HT > 1H)
        val existingAnchor = deriveAnchorFromMatch(existing, deterministicSubMinuteSeed = false)
        val candidateAnchor = deriveAnchorFromMatch(candidate, deterministicSubMinuteSeed = false)
        if (candidateAnchor.phase.progressionOrder > existingAnchor.phase.progressionOrder) return true
        if (existingAnchor.phase.progressionOrder > candidateAnchor.phase.progressionOrder) return false

        // 6. Provider Authority Rank tie-breaker
        if (candidateAnchor.providerAuthorityRank > existingAnchor.providerAuthorityRank) return true
        if (existingAnchor.providerAuthorityRank > candidateAnchor.providerAuthorityRank) return false

        // 7. More advanced verified minute within the same phase
        return candidateAnchor.verifiedMinuteAtSync > existingAnchor.verifiedMinuteAtSync
    }

    /**
     * Records a verified observation directly from phase/badge parameters and stores it in [anchorRegistry].
     */
    fun recordVerifiedObservation(
        matchId: String,
        liveMinuteOrBadge: String,
        status: MatchStatus = MatchStatus.LIVE,
        observedAtEpochMs: Long = System.currentTimeMillis(),
        officialAddedMinutes: Int? = null,
        providerName: String = "API-Football"
    ): MatchClockAnchor {
        val syntheticMatch = FootballMatch(
            matchId = matchId,
            tournamentId = "uefa_cl",
            tournamentName = "UEFA Champions League",
            tournamentRegion = "Europe",
            dayTab = MatchDayTab.TODAY,
            kickoffTimeFormatted = "20:00",
            kickoffDateFormatted = "Today",
            status = status,
            liveMinuteOrBadge = liveMinuteOrBadge,
            homeTeamId = "h_$matchId",
            homeTeamName = "Home",
            homeTeamBadge = "",
            homeScore = 0,
            awayTeamId = "a_$matchId",
            awayTeamName = "Away",
            awayTeamBadge = "",
            awayScore = 0,
            stadiumName = "",
            cityName = "",
            beinChannel = "beIN Sports 1 HD",
            beinChannelNumberHint = "1"
        )
        val derived = deriveAnchorFromMatch(
            match = syntheticMatch,
            nowEpochMs = observedAtEpochMs,
            providerName = providerName,
            deterministicSubMinuteSeed = false
        ).let { base ->
            if (officialAddedMinutes != null) {
                base.copy(officialAddedMinutes = officialAddedMinutes)
            } else {
                base
            }
        }
        anchorRegistry[matchId] = derived
        return derived
    }

    /**
     * Computes the half-prefixed live badge (e.g. `"1H • 22'"`, `"2H • 90+7'"`, `"HT"`, `"FT"`)
     * purely from the local [MatchClockAnchor] and [nowEpochMs] without any network calls.
     * Respects [MatchClockAnchor.officialAddedMinutes] so stoppage time can advance to `+7'` or higher
     * rather than being hard-capped at `+5'`.
     */
    fun computeLocalBadgeFromAnchor(
        anchor: MatchClockAnchor,
        nowEpochMs: Long = System.currentTimeMillis()
    ): String {
        return when (anchor.phase) {
            MatchPeriodPhase.PRE_MATCH -> "KO"
            MatchPeriodPhase.HALFTIME_BREAK -> "HT"
            MatchPeriodPhase.FULL_TIME -> "FT"
            MatchPeriodPhase.PENALTIES -> "PEN"
            MatchPeriodPhase.FIRST_HALF, MatchPeriodPhase.FIRST_HALF_STOPPAGE -> {
                val elapsedMin = (((nowEpochMs - anchor.periodStartEpochMs) / 60_000L).toInt()).coerceAtLeast(1)
                if (elapsedMin <= 45 && anchor.phase == MatchPeriodPhase.FIRST_HALF) {
                    "1H • ${elapsedMin}'"
                } else {
                    val rawStop = (elapsedMin - 45).coerceAtLeast(1)
                    val maxStop = anchor.officialAddedMinutes ?: 15
                    val boundedStop = rawStop.coerceIn(1, maxStop)
                    "1H • 45+${boundedStop}'"
                }
            }
            MatchPeriodPhase.SECOND_HALF, MatchPeriodPhase.SECOND_HALF_STOPPAGE -> {
                val elapsed2HMin = (((nowEpochMs - anchor.periodStartEpochMs) / 60_000L).toInt()).coerceAtLeast(0)
                val totalMin = (45 + elapsed2HMin).coerceAtLeast(46)
                if (totalMin <= 90 && anchor.phase == MatchPeriodPhase.SECOND_HALF) {
                    "2H • ${totalMin}'"
                } else {
                    val rawStop = (totalMin - 90).coerceAtLeast(1)
                    val maxStop = anchor.officialAddedMinutes ?: 15
                    val boundedStop = rawStop.coerceIn(1, maxStop)
                    "2H • 90+${boundedStop}'"
                }
            }
            MatchPeriodPhase.EXTRA_TIME -> {
                val elapsedEtMin = (((nowEpochMs - anchor.periodStartEpochMs) / 60_000L).toInt()).coerceAtLeast(1)
                "ET • ${(90 + elapsedEtMin).coerceIn(91, 120)}'"
            }
        }
    }

    /**
     * Returns `true` when a match is inside a critical phase-transition verification window:
     * - End of 1st Half (`>= 42'` or in `FIRST_HALF_STOPPAGE`),
     * - End of Halftime break (`>= 13` minutes elapsed since halftime was verified),
     * - End of 2nd Half (`>= 87'` or in `SECOND_HALF_STOPPAGE`),
     * - Extra Time or Penalties.
     */
    fun isMatchInVerificationHorizon(
        anchor: MatchClockAnchor,
        nowEpochMs: Long = System.currentTimeMillis()
    ): Boolean {
        return when (anchor.phase) {
            MatchPeriodPhase.FIRST_HALF -> {
                val elapsedMin = ((nowEpochMs - anchor.periodStartEpochMs) / 60_000L).toInt()
                elapsedMin >= 42
            }
            MatchPeriodPhase.FIRST_HALF_STOPPAGE -> true
            MatchPeriodPhase.HALFTIME_BREAK -> {
                val elapsedHalftimeMin = ((nowEpochMs - anchor.verifiedAtEpochMs) / 60_000L).toInt()
                elapsedHalftimeMin >= 13
            }
            MatchPeriodPhase.SECOND_HALF -> {
                val totalMin = 45 + ((nowEpochMs - anchor.periodStartEpochMs) / 60_000L).toInt()
                totalMin >= 87
            }
            MatchPeriodPhase.SECOND_HALF_STOPPAGE,
            MatchPeriodPhase.EXTRA_TIME,
            MatchPeriodPhase.PENALTIES -> true
            MatchPeriodPhase.PRE_MATCH -> nowEpochMs >= anchor.periodStartEpochMs - 2 * 60_000L
            MatchPeriodPhase.FULL_TIME -> false
        }
    }
}

