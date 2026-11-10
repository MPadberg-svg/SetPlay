package com.example.data.repository

/**
 * Upstream football data providers ranked by live telemetry authority and VAR accuracy.
 */
enum class MatchDataProvider(
    val authorityWeight: Int,
    val displayName: String
) {
    API_FOOTBALL_V3(100, "API-Football v3"),
    SCORES_365(92, "365Scores"),
    SPORT_API_AI(88, "SportAPI.ai"),
    ESPN_SCOREBOARD(82, "ESPN Scoreboard"),
    FOOTBALL_DATA_ORG(74, "Football-Data.org"),
    THESPORTSDB_V1(50, "TheSportsDB v1");

    companion object {
        val THE_SPORTS_DB: MatchDataProvider = THESPORTSDB_V1
    }
}

/**
 * Single provider observation for a match fixture at a point in time.
 */
data class ProviderMatchObservation(
    val provider: MatchDataProvider,
    val match: FootballMatch,
    val observedAtEpochMs: Long = System.currentTimeMillis(),
    val officialAddedMinutes: Int? = null
)

/**
 * Multi-provider conflict resolution engine for Kickoff fixtures.
 *
 * Key Guarantees:
 * 1. **VAR Disallowed-Goal Rollback**: Rather than naively taking `maxOf(homeScore)` across all sources
 *    (which permanently locks a disallowed pre-VAR goal reported by a lower-authority feed like `THESPORTSDB_V1`),
 *    resolves the score pair by weighted provider authority consensus (`API_FOOTBALL_V3` + `ESPN_SCOREBOARD`
 *    outrank stale `THESPORTSDB_V1` pre-VAR scores).
 * 2. **Crest & Metadata Preservation**: Preserves high-resolution `/tiny` team badges from `THESPORTSDB_V1`
 *    and merges broadcast channels, fixture IDs, and venue metadata from all contributing providers.
 */
object ProviderAuthorityEngine {

    fun resolveMultiProviderFixture(
        observations: List<ProviderMatchObservation>,
        nowEpochMs: Long = System.currentTimeMillis()
    ): FootballMatch {
        require(observations.isNotEmpty()) { "observations must not be empty" }

        val sorted = observations.sortedWith(
            compareByDescending<ProviderMatchObservation> { it.provider.authorityWeight }
                .thenByDescending { it.observedAtEpochMs }
        )

        val primary = sorted.first().match

        // 1. Resolve status by highest-authority non-upcoming observation
        val authoritativeLiveOrFinished = sorted.firstOrNull { it.match.status != MatchStatus.UPCOMING }
            ?: sorted.first()
        val resolvedStatus = authoritativeLiveOrFinished.match.status
        val resolvedBadge = authoritativeLiveOrFinished.match.liveMinuteOrBadge

        // 2. Resolve score with VAR Disallowed-Goal Rollback via weighted provider authority
        val scoredObservations = sorted.filter {
            it.match.homeScore != null && it.match.awayScore != null
        }

        val (resolvedHomeScore, resolvedAwayScore) = if (scoredObservations.isEmpty()) {
            primary.homeScore to primary.awayScore
        } else {
            val groupedByScore = scoredObservations.groupBy {
                (it.match.homeScore ?: 0) to (it.match.awayScore ?: 0)
            }
            val winningEntry = groupedByScore.entries.maxWithOrNull(
                compareBy<Map.Entry<Pair<Int, Int>, List<ProviderMatchObservation>>> { entry ->
                    entry.value.sumOf { it.provider.authorityWeight }
                }.thenBy { entry ->
                    entry.value.maxOf { it.provider.authorityWeight }
                }.thenBy { entry ->
                    entry.value.maxOf { it.observedAtEpochMs }
                }
            )
            val pair = winningEntry?.key ?: (scoredObservations.first().match.homeScore!! to scoredObservations.first().match.awayScore!!)
            pair.first to pair.second
        }

        // 3. Preserve /tiny badges from TheSportsDB when available, falling back to any non-blank badge
        val bestHomeBadge = sorted.map { it.match.homeTeamBadge }
            .firstOrNull { it.contains("/tiny", ignoreCase = true) }
            ?: sorted.map { it.match.homeTeamBadge }.firstOrNull { it.isNotBlank() }
            ?: primary.homeTeamBadge

        val bestAwayBadge = sorted.map { it.match.awayTeamBadge }
            .firstOrNull { it.contains("/tiny", ignoreCase = true) }
            ?: sorted.map { it.match.awayTeamBadge }.firstOrNull { it.isNotBlank() }
            ?: primary.awayTeamBadge

        // 4. Merge cross-provider IDs and broadcast channels
        val apiFootballId = sorted.map { it.match.apiFootballFixtureId }.firstOrNull { it.isNotBlank() }.orEmpty()
        val espnId = sorted.map { it.match.espnEventId }.firstOrNull { it.isNotBlank() }.orEmpty()
        val sportsDbId = sorted.map { it.match.sportsDbEventId }.firstOrNull { it.isNotBlank() }.orEmpty()
        val mergedSourceTv = sorted.flatMap { it.match.sourceTvChannels }.distinct()
        val mergedFallbackTv = sorted.flatMap { it.match.fallbackMenaChannels }.distinct()

        val resolvedMatch = primary.copy(
            status = resolvedStatus,
            liveMinuteOrBadge = resolvedBadge,
            homeScore = resolvedHomeScore,
            awayScore = resolvedAwayScore,
            homeTeamBadge = bestHomeBadge,
            awayTeamBadge = bestAwayBadge,
            apiFootballFixtureId = apiFootballId,
            espnEventId = espnId,
            sportsDbEventId = sportsDbId,
            sourceTvChannels = if (mergedSourceTv.isNotEmpty()) mergedSourceTv else primary.sourceTvChannels,
            fallbackMenaChannels = if (mergedFallbackTv.isNotEmpty()) mergedFallbackTv else primary.fallbackMenaChannels
        )

        // Register/update the clock anchor from the winning observation
        val officialAdded = sorted.mapNotNull { it.officialAddedMinutes }.firstOrNull()
        MatchClockEngine.recordVerifiedObservation(
            matchId = resolvedMatch.matchId,
            liveMinuteOrBadge = resolvedMatch.liveMinuteOrBadge,
            status = resolvedMatch.status,
            observedAtEpochMs = nowEpochMs,
            officialAddedMinutes = officialAdded,
            providerName = authoritativeLiveOrFinished.provider.displayName
        )

        return resolvedMatch
    }
}
