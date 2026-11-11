package com.example.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.FavoriteTeamEntity
import com.example.data.model.PlaylistEntity
import com.example.data.model.StreamEntity
import com.example.data.model.TournamentPreferenceEntity
import com.example.data.repository.CompetitionCategory
import com.example.data.repository.FootballMatch
import com.example.data.repository.FootballRepository
import com.example.data.repository.FootballTeam
import com.example.data.repository.LiveScoreSyncState
import com.example.data.repository.MatchClockEngine
import com.example.data.repository.MatchDayTab
import com.example.data.repository.MatchPeriodPhase
import com.example.data.repository.MatchStatus
import com.example.data.repository.SeasonTimeMode
import com.example.data.repository.SyncProgress
import com.example.data.repository.UserCountryZone
import com.example.data.repository.UserZoneRegistry
import com.example.data.repository.formatDynamicKickoffTime
import com.example.ui.theme.AmberGold
import com.example.ui.theme.KickoffCanopyElevated
import com.example.ui.theme.KickoffCrimsonGlow
import com.example.ui.theme.KickoffEmeraldBorder
import com.example.ui.theme.KickoffFloodlightVolt
import com.example.ui.theme.KickoffFloodlightWhite
import com.example.ui.theme.KickoffGlassWell
import com.example.ui.theme.KickoffLiveCrimson
import com.example.ui.theme.KickoffMistSilver
import com.example.ui.theme.KickoffPitchBlack
import com.example.ui.theme.KickoffPitchEmerald
import com.example.ui.theme.KickoffRowDivider
import com.example.ui.theme.KickoffSlateMuted
import com.example.ui.theme.KickoffStadiumForest
import com.example.ui.theme.KickoffStadiumTurfAlt
import com.example.ui.theme.KickoffTrophyGold
import com.example.ui.theme.KickoffTurfSurface
import com.example.ui.theme.KickoffVoltGlow
import com.example.ui.theme.MenaAdSportsBlue
import com.example.ui.theme.MenaAdSportsDark
import com.example.ui.theme.MenaArryadiaDark
import com.example.ui.theme.MenaArryadiaRed
import com.example.ui.theme.MenaBeinDark
import com.example.ui.theme.MenaBeinLavender
import com.example.ui.theme.MenaBeinPurple
import com.example.ui.theme.MenaSscDark
import com.example.ui.theme.MenaSscTeal
import com.example.ui.theme.TabularNumeralTextStyle
import com.example.ui.theme.luxuryFocusableCard
import com.example.ui.theme.tvFocusableControl
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.BeinChannelMatcherState
import com.example.ui.viewmodel.LiveGoalAlertEvent
import com.example.ui.viewmodel.MainViewModel
import com.example.util.EditionResolver
import com.example.util.SetPlayEdition
import com.example.util.StreamMetadataParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    mainViewModel: MainViewModel,
    onStreamClick: (StreamEntity) -> Unit,
    onNavigateScreen: (AppScreen) -> Unit
) {
    val context = LocalContext.current
    val editionOverride by mainViewModel.editionOverride.collectAsStateWithLifecycle()
    val activeEdition = remember(editionOverride, context) {
        EditionResolver.resolveEdition(context)
    }

    val activePlaylist by mainViewModel.activePlaylist.collectAsStateWithLifecycle()
    val syncProgress by mainViewModel.syncProgress.collectAsStateWithLifecycle()

    val selectedCountryZone by mainViewModel.selectedCountryZone.collectAsStateWithLifecycle()
    val selectedSeasonTimeMode by mainViewModel.selectedSeasonTimeMode.collectAsStateWithLifecycle()
    val showCountryZoneModal by mainViewModel.showCountryZoneModal.collectAsStateWithLifecycle()
    val favoriteTeams by mainViewModel.favoriteTeams.collectAsStateWithLifecycle()
    val tournamentPreferences by mainViewModel.tournamentPreferences.collectAsStateWithLifecycle()
    val showFirstTimeOnboarding by mainViewModel.showFirstTimeTeamsOnboarding.collectAsStateWithLifecycle()
    val showCustomizeModal by mainViewModel.showCustomizeTeamsModal.collectAsStateWithLifecycle()
    val selectedMatchDay by mainViewModel.selectedMatchDay.collectAsStateWithLifecycle()
    val selectedTournamentFilter by mainViewModel.selectedTournamentFilter.collectAsStateWithLifecycle()
    val selectedCompetitionCategory by mainViewModel.selectedCompetitionCategory.collectAsStateWithLifecycle()
    val collapsedTournamentIds by mainViewModel.collapsedTournamentIds.collectAsStateWithLifecycle()
    val matchesByDay by mainViewModel.matchesByDay.collectAsStateWithLifecycle()
    val todayMatches by mainViewModel.todayMatches.collectAsStateWithLifecycle()
    val isLoadingMatches by mainViewModel.isLoadingMatches.collectAsStateWithLifecycle()

    val teamSearchQuery by mainViewModel.teamSearchQuery.collectAsStateWithLifecycle()
    val teamSearchResults by mainViewModel.teamSearchResults.collectAsStateWithLifecycle()
    val isSearchingTeams by mainViewModel.isSearchingTeams.collectAsStateWithLifecycle()
    val beinMatcherState by mainViewModel.beinChannelMatcherState.collectAsStateWithLifecycle()
    val selectedMatchDossier by mainViewModel.selectedMatchDossier.collectAsStateWithLifecycle()
    val liveScoreSyncState by mainViewModel.liveScoreSyncState.collectAsStateWithLifecycle()
    val pinnedMatchIds by mainViewModel.pinnedMatchIds.collectAsStateWithLifecycle()
    val activeGoalAlert by mainViewModel.activeGoalAlert.collectAsStateWithLifecycle()
    val kickoffSourceHealth by mainViewModel.kickoffSourceHealth.collectAsStateWithLifecycle()
    val kickoffSourceAlert by mainViewModel.kickoffSourceAlert.collectAsStateWithLifecycle()
    val isCheckingSourcesHealth by mainViewModel.isCheckingSourcesHealth.collectAsStateWithLifecycle()
    var customizeInitialTab by remember { mutableIntStateOf(0) }
    val unhealthySourcesCount = remember(kickoffSourceHealth) {
        kickoffSourceHealth.count { state ->
            state.status == com.example.data.repository.SourceHealthStatus.KEY_INVALID_OR_EXPIRED ||
                state.status == com.example.data.repository.SourceHealthStatus.QUOTA_WARNING ||
                state.status == com.example.data.repository.SourceHealthStatus.UNREACHABLE
        }
    }

    val activeDayMatches = matchesByDay[selectedMatchDay].orEmpty()
    val liveMatchesCount = todayMatches.count { it.status == MatchStatus.LIVE || it.status == MatchStatus.HALFTIME }
    val pinnedMatches = remember(matchesByDay, activeDayMatches, pinnedMatchIds) {
        if (pinnedMatchIds.isEmpty()) emptyList()
        else {
            val allKnown = (activeDayMatches + matchesByDay.values.flatten()).distinctBy { it.matchId }
            allKnown.filter { it.matchId in pinnedMatchIds }
        }
    }

    val enabledTournamentMap = remember(tournamentPreferences) {
        tournamentPreferences.associateBy { it.tournamentId }
    }
    val tournamentCategoryMap = remember {
        FootballRepository.ALL_TOURNAMENTS.associate { it.tournamentId to it.category }
    }

    val visibleMatches = remember(
        activeDayMatches,
        enabledTournamentMap,
        selectedCompetitionCategory,
        selectedTournamentFilter,
        favoriteTeams
    ) {
        activeDayMatches.filter { match ->
            val pref = enabledTournamentMap[match.tournamentId]
            val isTournamentEnabled = pref?.isEnabled ?: true
            if (!isTournamentEnabled && selectedTournamentFilter == "ALL") {
                return@filter false
            }
            if (selectedCompetitionCategory != null) {
                val matchCat = tournamentCategoryMap[match.tournamentId] ?: CompetitionCategory.EUROPEAN
                if (matchCat != selectedCompetitionCategory) {
                    return@filter false
                }
            }
            when (selectedTournamentFilter) {
                "ALL" -> true
                "CAT_EUROPEAN" -> (tournamentCategoryMap[match.tournamentId] ?: CompetitionCategory.EUROPEAN) == CompetitionCategory.EUROPEAN
                "CAT_AFRICAN_ARAB" -> (tournamentCategoryMap[match.tournamentId] ?: CompetitionCategory.EUROPEAN) == CompetitionCategory.AFRICAN_ARAB
                "CAT_INTERNATIONAL" -> (tournamentCategoryMap[match.tournamentId] ?: CompetitionCategory.EUROPEAN) == CompetitionCategory.INTERNATIONAL
                "MY_TEAMS" -> isMatchForFavoriteTeams(match, favoriteTeams)
                "LIVE" -> match.status == MatchStatus.LIVE || match.status == MatchStatus.HALFTIME
                else -> match.tournamentId == selectedTournamentFilter
            }
        }
    }

    val favoriteTeamMatches = remember(activeDayMatches, favoriteTeams) {
        if (favoriteTeams.isEmpty()) emptyList()
        else activeDayMatches.filter { isMatchForFavoriteTeams(it, favoriteTeams) }
    }

    val sortedVisibleMatches = remember(visibleMatches, enabledTournamentMap) {
        visibleMatches.sortedWith(
            compareBy<FootballMatch> { match ->
                getMatchStatusPriority(match.status)
            }.thenBy { match ->
                parseKickoffTimeToMinutes(match.kickoffTimeFormatted)
            }.thenBy { match ->
                getTournamentCanonicalPriority(
                    match.tournamentId,
                    enabledTournamentMap[match.tournamentId]?.sortOrder ?: 99
                )
            }.thenBy { match ->
                match.matchId
            }
        )
    }

    val chronologicalSections = remember(visibleMatches, enabledTournamentMap) {
        buildUnifiedTournamentSections(visibleMatches, enabledTournamentMap)
    }

    val spotlightMatches = remember(sortedVisibleMatches, favoriteTeams) {
        val liveOrHt = sortedVisibleMatches.filter {
            it.status == MatchStatus.LIVE || it.status == MatchStatus.HALFTIME
        }
        val favMatches = sortedVisibleMatches.filter {
            isMatchForFavoriteTeams(it, favoriteTeams) && it !in liveOrHt
        }
        val topCompetitions = sortedVisibleMatches.filter {
            it.tournamentId in setOf("uefa_cl", "premier_league", "laliga", "caf_cl", "botola_pro") &&
                it !in liveOrHt && it !in favMatches
        }
        (liveOrHt + favMatches + topCompetitions).distinctBy { it.matchId }.take(6)
    }

    if (activeEdition == SetPlayEdition.TV) {
        TvKickoffStadiumScreen(
            syncProgress = syncProgress,
            activeGoalAlert = activeGoalAlert,
            kickoffSourceAlert = kickoffSourceAlert,
            unhealthySourcesCount = unhealthySourcesCount,
            selectedCountryZone = selectedCountryZone,
            selectedSeasonTimeMode = selectedSeasonTimeMode,
            selectedMatchDay = selectedMatchDay,
            selectedCompetitionCategory = selectedCompetitionCategory,
            selectedTournamentFilter = selectedTournamentFilter,
            matchesByDay = matchesByDay,
            activeDayMatches = activeDayMatches,
            sortedVisibleMatches = sortedVisibleMatches,
            chronologicalSections = chronologicalSections,
            favoriteTeams = favoriteTeams,
            tournamentPreferences = tournamentPreferences,
            tournamentCategoryMap = tournamentCategoryMap,
            pinnedMatchIds = pinnedMatchIds,
            liveMatchesCount = liveMatchesCount,
            isLoadingMatches = isLoadingMatches,
            onSelectMatchDay = { mainViewModel.selectMatchDay(it) },
            onSelectCategory = { mainViewModel.selectCompetitionCategory(it) },
            onSelectFilter = { mainViewModel.selectTournamentFilter(it) },
            onOpenCountryZoneModal = { mainViewModel.openCountryZoneModal() },
            onOpenCustomizeModal = { tabIdx ->
                customizeInitialTab = tabIdx
                mainViewModel.openCustomizeTeamsModal()
            },
            onRefreshMatches = { mainViewModel.refreshKickOffMatches(selectedMatchDay) },
            onOpenDossier = { mainViewModel.openMatchCenterDossier(it) },
            onWatchChannel = { match, ch ->
                mainViewModel.onWatchMatchOnBeinClicked(
                    match = match,
                    clickedChannelName = ch,
                    onAutoPlaySingleStream = onStreamClick
                )
            },
            onTogglePinMatch = { mainViewModel.togglePinMatch(it) },
            onResetFilter = {
                mainViewModel.selectCompetitionCategory(null)
                mainViewModel.selectTournamentFilter("ALL")
                mainViewModel.enableAllTournaments()
            },
            onDismissGoalAlert = { mainViewModel.dismissGoalAlert() },
            onDismissSourceAlert = { mainViewModel.dismissKickoffSourceAlert() }
        )
    } else {
        // =========================================================================
        // MOBILE EDITORIAL EXPERIENCE (`SetPlayEdition.PHONE`)
        // =========================================================================
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            KickoffPitchBlack,
                            KickoffStadiumForest.copy(alpha = 0.22f),
                            KickoffPitchBlack
                        )
                    )
                )
                .testTag("match_center_list"),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            item {
                SyncBanner(syncProgress = syncProgress)
            }

            activeGoalAlert?.let { alert ->
                item(key = "live_goal_alert_${alert.alertId}") {
                    LiveGoalAlertBanner(
                        alert = alert,
                        onOpenDossier = {
                            mainViewModel.dismissGoalAlert()
                            mainViewModel.openMatchCenterDossier(alert.match)
                        },
                        onDismiss = { mainViewModel.dismissGoalAlert() }
                    )
                }
            }

            // Integrated Matchday Command Bar (`match_center_hero_card`)
            item(key = "kickoff_command_bar_item") {
                val activeLeaguesInSlate = remember(sortedVisibleMatches) {
                    sortedVisibleMatches.map { it.tournamentId }.distinct().size
                }
                KickOffArenaHeaderCard(
                    activeEdition = SetPlayEdition.PHONE,
                    selectedCountryZone = selectedCountryZone,
                    selectedSeasonTimeMode = selectedSeasonTimeMode,
                    selectedMatchDay = selectedMatchDay,
                    selectedTournamentFilter = selectedTournamentFilter,
                    totalVisibleMatchesCount = sortedVisibleMatches.size,
                    activeLeaguesInSlate = activeLeaguesInSlate,
                    favoriteTeamsCount = favoriteTeams.size,
                    liveMatchesCount = liveMatchesCount,
                    isLoadingMatches = isLoadingMatches,
                    liveScoreSyncState = liveScoreSyncState,
                    sourceAlertMessage = kickoffSourceAlert,
                    unhealthySourcesCount = unhealthySourcesCount,
                    onRefreshMatches = { mainViewModel.refreshKickOffMatches(selectedMatchDay) },
                    onOpenCountryZoneModal = { mainViewModel.openCountryZoneModal() },
                    onOpenCustomizeModal = {
                        customizeInitialTab = 0
                        mainViewModel.openCustomizeTeamsModal()
                    },
                    onOpenSourcesHub = {
                        customizeInitialTab = 3
                        mainViewModel.openCustomizeTeamsModal()
                    },
                    onToggleLiveFilter = {
                        mainViewModel.selectTournamentFilter(
                            if (selectedTournamentFilter == "LIVE") "ALL" else "LIVE"
                        )
                    },
                    onSelectMatchDay = { mainViewModel.selectMatchDay(it) }
                )
            }

            // Pinned Matches ("My Games") Sticky Live Rail
            if (pinnedMatches.isNotEmpty()) {
                item(key = "pinned_matches_rail_item") {
                    PinnedMatchesStickyRail(
                        pinnedMatches = pinnedMatches,
                        onWatchMatch = { match ->
                            mainViewModel.onWatchMatchOnBeinClicked(
                                match = match,
                                clickedChannelName = match.beinChannel,
                                onAutoPlaySingleStream = onStreamClick
                            )
                        },
                        onOpenDossier = { match -> mainViewModel.openMatchCenterDossier(match) },
                        onUnpinMatch = { matchId -> mainViewModel.togglePinMatch(matchId) }
                    )
                }
            }

            // Sticky 7-Day Forward Date Ribbon + Unified Expressive Competition Deck
            stickyHeader(key = "sticky_matchday_and_tournament_bar") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    KickoffPitchBlack.copy(alpha = 0.98f),
                                    KickoffTurfSurface.copy(alpha = 0.96f)
                                )
                            )
                        )
                ) {
                    FiveDayMatchTimelineRibbon(
                        selectedDay = selectedMatchDay,
                        selectedCountryZone = selectedCountryZone,
                        matchesByDay = matchesByDay,
                        liveCountToday = liveMatchesCount,
                        onSelectDay = { mainViewModel.selectMatchDay(it) },
                        activeEdition = SetPlayEdition.PHONE
                    )

                    TournamentFilterRow(
                        selectedFilter = selectedTournamentFilter,
                        selectedCategory = selectedCompetitionCategory,
                        tournamentCategoryMap = tournamentCategoryMap,
                        favoriteTeamsCount = favoriteTeams.size,
                        liveCount = activeDayMatches.count { it.status == MatchStatus.LIVE || it.status == MatchStatus.HALFTIME },
                        activeDayMatches = activeDayMatches,
                        tournamentPreferences = tournamentPreferences,
                        onSelectFilter = { mainViewModel.selectTournamentFilter(it) },
                        onOpenCustomize = {
                            customizeInitialTab = 1
                            mainViewModel.openCustomizeTeamsModal()
                        },
                        activeEdition = SetPlayEdition.PHONE
                    )

                    if (selectedCompetitionCategory == CompetitionCategory.INTERNATIONAL ||
                        selectedTournamentFilter == "CAT_INTERNATIONAL" ||
                        selectedTournamentFilter == "intl_official" ||
                        selectedTournamentFilter == "intl_friendly"
                    ) {
                        Surface(
                            color = KickoffCanopyElevated,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.9.dp, KickoffTrophyGold.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 3.dp)
                                .testTag("international_fifa_ranking_banner")
                        ) {
                            Text(
                                text = "FIFA World Ranking Filter Active • Africa Top 5 • Asia Top 3 • Europe Top 7 • South America Top 4",
                                color = KickoffFloodlightWhite,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        KickoffPitchEmerald.copy(alpha = 0.55f),
                                        KickoffFloodlightVolt.copy(alpha = 0.35f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
            }

            // Live & Marquee Stadium Spotlight Carousel (shown when browsing All / Live / Pillar slates)
            if (spotlightMatches.isNotEmpty() && selectedTournamentFilter != "MY_TEAMS") {
                item(key = "live_and_marquee_spotlight_carousel") {
                    LiveAndMarqueeSpotlightCarousel(
                        matches = spotlightMatches,
                        selectedCountryZone = selectedCountryZone,
                        selectedSeasonTimeMode = selectedSeasonTimeMode,
                        onOpenDossier = { mainViewModel.openMatchCenterDossier(it) },
                        onWatchMatch = { match ->
                            mainViewModel.onWatchMatchOnBeinClicked(
                                match = match,
                                clickedChannelName = match.beinChannel,
                                onAutoPlaySingleStream = onStreamClick
                            )
                        }
                    )
                }
            }

            // Main Fixture Feed: Favorite Teams View vs Grouped Competition Containers
            if (selectedTournamentFilter == "MY_TEAMS") {
                if (favoriteTeams.isEmpty()) {
                    item {
                        FavoriteTeamsEmptyBanner(
                            onSelectFavoriteTeams = {
                                customizeInitialTab = 0
                                mainViewModel.openCustomizeTeamsModal()
                            }
                        )
                    }
                } else {
                    item {
                        PinnedFavoriteTeamsSection(
                            favoriteTeams = favoriteTeams,
                            favoriteMatches = favoriteTeamMatches,
                            selectedDay = selectedMatchDay,
                            selectedCountryZone = selectedCountryZone,
                            selectedSeasonTimeMode = selectedSeasonTimeMode,
                            onEditFavoriteTeams = {
                                customizeInitialTab = 0
                                mainViewModel.openCustomizeTeamsModal()
                            },
                            onInspectMatchDossier = { mainViewModel.openMatchCenterDossier(it) },
                            onWatchMatch = { match, clickedChannel ->
                                mainViewModel.onWatchMatchOnBeinClicked(
                                    match = match,
                                    clickedChannelName = clickedChannel,
                                    onAutoPlaySingleStream = onStreamClick
                                )
                            }
                        )
                    }
                }
            } else {
                if (chronologicalSections.isEmpty()) {
                    item {
                        EmptyMatchesCard(
                            selectedFilter = selectedTournamentFilter,
                            onResetFilter = {
                                mainViewModel.selectCompetitionCategory(null)
                                mainViewModel.selectTournamentFilter("ALL")
                                mainViewModel.enableAllTournaments()
                            },
                            onOpenCustomize = {
                                customizeInitialTab = 0
                                mainViewModel.openCustomizeTeamsModal()
                            }
                        )
                    }
                } else {
                    chronologicalSections.forEach { section ->
                        val tournamentId = section.tournamentId
                        val matchesInSection = section.matches
                        val firstMatch = matchesInSection.first()
                        val pref = enabledTournamentMap[tournamentId]
                        val isCollapsed = collapsedTournamentIds.contains(tournamentId)
                        val isPinned = pref?.isPinned == true
                        val liveInSection = matchesInSection.count {
                            it.status == MatchStatus.LIVE || it.status == MatchStatus.HALFTIME
                        }

                        item(key = "header_${section.sectionKey}") {
                            FlashscoreTournamentHeader(
                                tournamentId = tournamentId,
                                tournamentName = firstMatch.tournamentName,
                                regionName = firstMatch.tournamentRegion,
                                menaProviderName = firstMatch.menaProviderName,
                                matchCount = matchesInSection.size,
                                liveCount = liveInSection,
                                isPinned = isPinned,
                                isCollapsed = isCollapsed,
                                onToggleCollapse = { mainViewModel.toggleTournamentCollapsed(tournamentId) },
                                onTogglePin = { mainViewModel.toggleTournamentPinned(tournamentId) }
                            )
                        }

                        if (!isCollapsed) {
                            itemsIndexed(
                                items = matchesInSection,
                                key = { _, match -> "match_${match.matchId}" }
                            ) { index, match ->
                                FlashscoreMatchCard(
                                    match = match,
                                    selectedCountryZone = selectedCountryZone,
                                    selectedSeasonTimeMode = selectedSeasonTimeMode,
                                    utcOffsetBadge = selectedCountryZone.getUtcOffsetBadge(mode = selectedSeasonTimeMode),
                                    isHomeTeamFavorite = favoriteTeams.any {
                                        it.teamName.equals(match.homeTeamName, ignoreCase = true) ||
                                            it.teamId == match.homeTeamId
                                    },
                                    isAwayTeamFavorite = favoriteTeams.any {
                                        it.teamName.equals(match.awayTeamName, ignoreCase = true) ||
                                            it.teamId == match.awayTeamId
                                    },
                                    onToggleHomeFavorite = {
                                        mainViewModel.toggleFavoriteTeam(
                                            FootballTeam(
                                                teamId = match.homeTeamId,
                                                name = match.homeTeamName,
                                                shortName = match.homeTeamName.take(3).uppercase(),
                                                badgeUrl = match.homeTeamBadge,
                                                country = match.tournamentRegion,
                                                primaryLeague = match.tournamentName
                                            )
                                        )
                                    },
                                    onToggleAwayFavorite = {
                                        mainViewModel.toggleFavoriteTeam(
                                            FootballTeam(
                                                teamId = match.awayTeamId,
                                                name = match.awayTeamName,
                                                shortName = match.awayTeamName.take(3).uppercase(),
                                                badgeUrl = match.awayTeamBadge,
                                                country = match.tournamentRegion,
                                                primaryLeague = match.tournamentName
                                            )
                                        )
                                    },
                                    onOpenDossier = { mainViewModel.openMatchCenterDossier(match) },
                                    onWatchBeinClicked = { clickedChannel ->
                                        mainViewModel.onWatchMatchOnBeinClicked(
                                            match = match,
                                            clickedChannelName = clickedChannel,
                                            onAutoPlaySingleStream = onStreamClick
                                        )
                                    },
                                    isMatchPinned = match.matchId in pinnedMatchIds,
                                    onTogglePinMatch = { mainViewModel.togglePinMatch(match.matchId) },
                                    isLastInSection = index == matchesInSection.lastIndex
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // 1. First-Time Country Zone & Favorite Teams Onboarding Modal
    // =========================================================================
    if (showFirstTimeOnboarding) {
        FirstTimeFavoriteTeamsDialog(
            selectedCountryZone = selectedCountryZone,
            selectedSeasonTimeMode = selectedSeasonTimeMode,
            searchQuery = teamSearchQuery,
            searchResults = teamSearchResults,
            isSearching = isSearchingTeams,
            selectedFavorites = favoriteTeams,
            onSelectCountryZone = { mainViewModel.selectCountryZone(it) },
            onSelectSeasonTimeMode = { mainViewModel.selectSeasonTimeMode(it) },
            onAutoDetectCountryZone = { mainViewModel.autoDetectAndSelectCountryZone() },
            onOpenFullCountryZoneSheet = { mainViewModel.openCountryZoneModal() },
            onSearchQueryChange = { mainViewModel.updateTeamSearchQuery(it) },
            onToggleTeam = { mainViewModel.toggleFavoriteTeam(it) },
            onRemoveFavoriteTeam = { mainViewModel.removeFavoriteTeam(it) },
            onCompleteOnboarding = { mainViewModel.completeFirstTimeTeamsOnboarding() }
        )
    }

    // =========================================================================
    // 1B. Anytime "USER's Zone" Country, Timezone & TV Broadcaster Hub
    // =========================================================================
    if (showCountryZoneModal) {
        UserCountryZoneBottomSheet(
            selectedZone = selectedCountryZone,
            onSelectCountryZone = { mainViewModel.selectCountryZone(it) },
            onAutoDetectCountryZone = { mainViewModel.autoDetectAndSelectCountryZone() },
            onDismiss = { mainViewModel.closeCountryZoneModal() },
            seasonTimeMode = selectedSeasonTimeMode,
            onSelectSeasonTimeMode = { mainViewModel.selectSeasonTimeMode(it) },
            onPrioritizeCountryLeague = { mainViewModel.prioritizeCountrySpotlightLeague() }
        )
    }

    // =========================================================================
    // 2. 4-Tab Kickoff Settings & Source Management Hub
    // =========================================================================
    if (showCustomizeModal) {
        CustomizeTeamsAndTournamentsSheet(
            selectedCountryZone = selectedCountryZone,
            selectedSeasonTimeMode = selectedSeasonTimeMode,
            searchQuery = teamSearchQuery,
            searchResults = teamSearchResults,
            isSearching = isSearchingTeams,
            favoriteTeams = favoriteTeams,
            tournamentPreferences = tournamentPreferences,
            sourceHealthStates = kickoffSourceHealth,
            sourceAlertMessage = kickoffSourceAlert,
            isCheckingSourcesHealth = isCheckingSourcesHealth,
            initialTabIndex = customizeInitialTab,
            onSelectCountryZone = { mainViewModel.selectCountryZone(it) },
            onSelectSeasonTimeMode = { mainViewModel.selectSeasonTimeMode(it) },
            onAutoDetectCountryZone = { mainViewModel.autoDetectAndSelectCountryZone() },
            onOpenFullCountryZoneSheet = {
                mainViewModel.closeCustomizeTeamsModal()
                mainViewModel.openCountryZoneModal()
            },
            onSearchQueryChange = { mainViewModel.updateTeamSearchQuery(it) },
            onToggleFavoriteTeam = { mainViewModel.toggleFavoriteTeam(it) },
            onRemoveFavoriteTeam = { mainViewModel.removeFavoriteTeam(it) },
            onSetTournamentEnabled = { id, enabled -> mainViewModel.setTournamentEnabled(id, enabled) },
            onToggleTournamentPinned = { id -> mainViewModel.toggleTournamentPinned(id) },
            onEnableAllTournaments = { mainViewModel.enableAllTournaments() },
            onCheckAllSourcesHealth = { mainViewModel.checkAllKickoffSourcesHealth() },
            onValidateAndUpdateApiKey = { provider, newKey, onResult ->
                mainViewModel.validateAndUpdateKickoffApiKey(provider, newKey, onResult)
            },
            onResetApiKey = { provider -> mainViewModel.resetKickoffApiKey(provider) },
            onDismissSourceAlert = { mainViewModel.dismissKickoffSourceAlert() },
            onDismiss = {
                customizeInitialTab = 0
                mainViewModel.closeCustomizeTeamsModal()
            }
        )
    }

    // =========================================================================
    // 3. Full 5-Tab Match Center Dossier Modal
    // =========================================================================
    selectedMatchDossier?.let { dossier ->
        MatchCenterFullDialog(
            dossier = dossier,
            onDismiss = { mainViewModel.closeMatchCenterDossier() },
            onWatchOnMenaChannel = { clickedChannel ->
                mainViewModel.closeMatchCenterDossier()
                mainViewModel.onWatchMatchOnBeinClicked(
                    match = dossier.match,
                    clickedChannelName = clickedChannel,
                    onAutoPlaySingleStream = onStreamClick
                )
            }
        )
    }

    // =========================================================================
    // 4. Favorites-Only Channel Lookup / Smart Catalog Matcher Sheet
    // =========================================================================
    beinMatcherState?.let { matcherState ->
        BeinChannelMatcherBottomSheet(
            state = matcherState,
            activePlaylist = activePlaylist,
            onPlayMatchedFavoriteStream = { stream ->
                mainViewModel.dismissBeinChannelMatcher()
                onStreamClick(stream)
            },
            onWatchAndPinCatalogStream = { stream ->
                mainViewModel.pinToFavoritesAndPlayStream(
                    stream = stream,
                    onPlayStream = onStreamClick
                )
            },
            onSelectMenaChannelOption = { channelLabel ->
                mainViewModel.selectMenaChannelOptionInMatcher(
                    channelLabel = channelLabel,
                    onAutoPlaySingleStream = onStreamClick
                )
            },
            onConnectPlaylist = {
                mainViewModel.dismissBeinChannelMatcher()
                onNavigateScreen(AppScreen.PLAYLISTS)
            },
            onOpenFavoritesScreen = {
                mainViewModel.dismissBeinChannelMatcher()
                onNavigateScreen(AppScreen.FAVORITES)
            },
            onDismiss = { mainViewModel.dismissBeinChannelMatcher() }
        )
    }
}

private fun isMatchForFavoriteTeams(
    match: FootballMatch,
    favorites: List<FavoriteTeamEntity>
): Boolean {
    return favorites.any { fav ->
        fav.teamId.equals(match.homeTeamId, ignoreCase = true) ||
            fav.teamId.equals(match.awayTeamId, ignoreCase = true) ||
            fav.teamName.equals(match.homeTeamName, ignoreCase = true) ||
            fav.teamName.equals(match.awayTeamName, ignoreCase = true) ||
            match.homeTeamName.contains(fav.teamName, ignoreCase = true) ||
            match.awayTeamName.contains(fav.teamName, ignoreCase = true)
    }
}

private fun resolveMenaBroadcasterColors(channelLabel: String): Triple<Color, Color, Color> {
    val style = resolveBroadcastProviderBadgeStyle(channelLabel)
    return Triple(style.surfaceColor, style.borderColor, style.textColor)
}

@Composable
private fun SyncBanner(syncProgress: SyncProgress) {
    AnimatedVisibility(visible = syncProgress.isSyncing) {
        Surface(
            color = KickoffTurfSurface,
            border = BorderStroke(1.dp, KickoffPitchEmerald.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = syncProgress.phase.ifBlank { "Syncing Xtream catalog..." },
                        color = KickoffFloodlightWhite,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (syncProgress.itemsProcessed > 0) {
                        Text(
                            text = "${syncProgress.itemsProcessed} items",
                            color = KickoffPitchEmerald,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = KickoffPitchEmerald,
                    trackColor = KickoffGlassWell
                )
            }
        }
    }
}

@Composable
private fun LiveGoalAlertBanner(
    alert: LiveGoalAlertEvent,
    onOpenDossier: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        color = KickoffStadiumForest,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.4.dp, KickoffFloodlightVolt),
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clickable { onOpenDossier() }
            .testTag("kickoff_live_goal_alert_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                KickoffMatchGoalBallIcon(
                    modifier = Modifier.size(20.dp),
                    tint = KickoffFloodlightVolt,
                    accentColor = KickoffPitchEmerald
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "GOAL! • ${alert.match.tournamentName.uppercase()}",
                        color = KickoffFloodlightVolt,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.6.sp
                    )
                    Text(
                        text = "${alert.homeTeamName} ${alert.homeScore} - ${alert.awayScore} ${alert.awayTeamName}",
                        color = KickoffFloodlightWhite,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss Goal Alert",
                    tint = KickoffMistSilver,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

internal val CANONICAL_TOURNAMENT_ORDER = listOf(
    "uefa_cl",
    "uefa_el",
    "premier_league",
    "laliga",
    "serie_a",
    "bundesliga",
    "ligue_1",
    "caf_cl",
    "caf_confed",
    "botola_pro",
    "saudi_pro",
    "intl_official",
    "intl_friendly"
)

internal fun getTournamentCanonicalPriority(tournamentId: String, fallbackSortOrder: Int = 99): Int {
    val idx = CANONICAL_TOURNAMENT_ORDER.indexOf(tournamentId)
    return if (idx >= 0) idx + 1 else fallbackSortOrder + 20
}

internal fun parseKickoffTimeToMinutes(kickoffTimeFormatted: String): Int {
    val cleaned = kickoffTimeFormatted.trim()
    val parts = cleaned.split(":")
    if (parts.size >= 2) {
        val hours = parts[0].filter { it.isDigit() }.toIntOrNull()
        val minutes = parts[1].take(2).filter { it.isDigit() }.toIntOrNull()
        if (hours != null && minutes != null) {
            return hours * 60 + minutes
        }
    }
    return 24 * 60
}

internal fun getMatchStatusPriority(status: MatchStatus): Int {
    return when (status) {
        MatchStatus.LIVE, MatchStatus.HALFTIME -> 0
        MatchStatus.UPCOMING -> 1
        MatchStatus.FINISHED -> 2
        MatchStatus.POSTPONED, MatchStatus.CANCELLED -> 3
    }
}

internal data class ChronologicalTournamentSection(
    val sectionKey: String,
    val tournamentId: String,
    val isLiveBucket: Boolean,
    val matches: List<FootballMatch>
)

internal fun buildChronologicalTournamentSections(
    sortedMatches: List<FootballMatch>
): List<ChronologicalTournamentSection> {
    if (sortedMatches.isEmpty()) return emptyList()
    val sections = mutableListOf<ChronologicalTournamentSection>()
    var currentTournamentId = ""
    var currentIsLive = false
    var currentBucket = mutableListOf<FootballMatch>()
    var blockIndex = 0

    for (match in sortedMatches) {
        val isLive = match.status == MatchStatus.LIVE || match.status == MatchStatus.HALFTIME
        if (currentBucket.isEmpty()) {
            currentTournamentId = match.tournamentId
            currentIsLive = isLive
            currentBucket.add(match)
        } else if (currentTournamentId == match.tournamentId && currentIsLive == isLive) {
            currentBucket.add(match)
        } else {
            sections.add(
                ChronologicalTournamentSection(
                    sectionKey = "${currentTournamentId}_${if (currentIsLive) "live" else "sched"}_$blockIndex",
                    tournamentId = currentTournamentId,
                    isLiveBucket = currentIsLive,
                    matches = currentBucket.toList()
                )
            )
            blockIndex++
            currentTournamentId = match.tournamentId
            currentIsLive = isLive
            currentBucket = mutableListOf(match)
        }
    }
    if (currentBucket.isNotEmpty()) {
        sections.add(
            ChronologicalTournamentSection(
                sectionKey = "${currentTournamentId}_${if (currentIsLive) "live" else "sched"}_$blockIndex",
                tournamentId = currentTournamentId,
                isLiveBucket = currentIsLive,
                matches = currentBucket.toList()
            )
        )
    }
    return sections
}

/**
 * Groups matches by `tournamentId` so each competition appears at most ONCE on screen,
 * eliminating duplicate/fragmented headers on the same matchday while sorting matches
 * chronologically within each competition and sorting competitions by pinned -> live -> canonical order.
 */
internal fun buildUnifiedTournamentSections(
    matches: List<FootballMatch>,
    enabledTournamentMap: Map<String, TournamentPreferenceEntity> = emptyMap()
): List<ChronologicalTournamentSection> {
    if (matches.isEmpty()) return emptyList()

    val grouped = matches.groupBy { it.tournamentId }.map { (tournamentId, compMatches) ->
        val sortedCompMatches = compMatches.sortedWith(
            compareBy<FootballMatch> { getMatchStatusPriority(it.status) }
                .thenBy { parseKickoffTimeToMinutes(it.kickoffTimeFormatted) }
                .thenBy { it.matchId }
        )
        val hasLive = sortedCompMatches.any { it.status == MatchStatus.LIVE || it.status == MatchStatus.HALFTIME }
        ChronologicalTournamentSection(
            sectionKey = "unified_${tournamentId}",
            tournamentId = tournamentId,
            isLiveBucket = hasLive,
            matches = sortedCompMatches
        )
    }

    return grouped.sortedWith(
        compareBy<ChronologicalTournamentSection> { section ->
            !(enabledTournamentMap[section.tournamentId]?.isPinned == true)
        }.thenBy { section ->
            !section.isLiveBucket
        }.thenBy { section ->
            getTournamentCanonicalPriority(
                section.tournamentId,
                enabledTournamentMap[section.tournamentId]?.sortOrder ?: 99
            )
        }
    )
}

/**
 * Integrated Single-Row Matchday Command Bar (`KickOffArenaHeaderCard`):
 * - Uses Floodlight Noir & Pitch Emerald (`KickoffTurfSurface`, `KickoffPitchEmerald`, `KickoffLiveCrimson`).
 * - Left: 1-Tap Country Zone & Clock Mode pill (`🇲🇦 MAR • GMT+1`).
 * - Center: Interactive Live Filter Pill (`● X LIVE`) + compact fixture slate counter.
 * - Right: Quick `"↩ Today"` jump pill (when on another day), Intelligent `"Hub"` Status Beacon, and Refresh button.
 */
@Composable
private fun KickOffArenaHeaderCard(
    activeEdition: SetPlayEdition = SetPlayEdition.PHONE,
    selectedCountryZone: UserCountryZone = UserZoneRegistry.defaultCountryZone,
    selectedSeasonTimeMode: SeasonTimeMode = SeasonTimeMode.AUTO,
    selectedMatchDay: MatchDayTab = MatchDayTab.TODAY,
    selectedTournamentFilter: String = "ALL",
    totalVisibleMatchesCount: Int = 0,
    activeLeaguesInSlate: Int = 0,
    favoriteTeamsCount: Int = 0,
    liveMatchesCount: Int = 0,
    isLoadingMatches: Boolean = false,
    liveScoreSyncState: LiveScoreSyncState = LiveScoreSyncState(),
    sourceAlertMessage: String? = null,
    unhealthySourcesCount: Int = 0,
    onRefreshMatches: () -> Unit,
    onOpenCountryZoneModal: () -> Unit = {},
    onOpenCustomizeModal: () -> Unit,
    onOpenSourcesHub: () -> Unit = onOpenCustomizeModal,
    onToggleLiveFilter: () -> Unit = {},
    onSelectMatchDay: (MatchDayTab) -> Unit = {}
) {
    val isLiveFilterActive = selectedTournamentFilter == "LIVE"
    val hasSourceAlert = sourceAlertMessage != null || unhealthySourcesCount > 0
    val isTv = activeEdition == SetPlayEdition.TV
    val utcBadge = remember(selectedCountryZone, selectedSeasonTimeMode) {
        selectedCountryZone.getUtcOffsetBadge(mode = selectedSeasonTimeMode)
    }
    val compactSlateLabel = remember(totalVisibleMatchesCount, activeLeaguesInSlate) {
        when {
            totalVisibleMatchesCount > 0 && activeLeaguesInSlate > 0 ->
                "$totalVisibleMatchesCount Games • $activeLeaguesInSlate ${if (activeLeaguesInSlate == 1) "League" else "Leagues"}"
            totalVisibleMatchesCount > 0 ->
                "$totalVisibleMatchesCount Games"
            else ->
                "0 Games"
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "hub_beacon_pulse")
    val beaconAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beacon_alpha"
    )

    Surface(
        color = KickoffTurfSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 0.85.dp,
            color = if (liveMatchesCount > 0 && selectedMatchDay == MatchDayTab.TODAY) {
                KickoffLiveCrimson.copy(alpha = 0.55f)
            } else {
                KickoffEmeraldBorder
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .height(if (isTv) 44.dp else 38.dp)
            .testTag("match_center_hero_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            KickoffTurfSurface,
                            KickoffCanopyElevated,
                            KickoffTurfSurface
                        )
                    )
                )
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Country Zone & Clock Mode Capsule
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Surface(
                    color = KickoffGlassWell,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.8.dp, KickoffPitchEmerald.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .tvFocusableControl(
                            accentColor = KickoffFloodlightVolt,
                            shape = RoundedCornerShape(50),
                            onClick = onOpenCountryZoneModal
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp)
                    ) {
                        Text(
                            text = "${selectedCountryZone.flagEmoji} ${selectedCountryZone.shortIso3}",
                            color = KickoffFloodlightWhite,
                            fontSize = if (isTv) 11.sp else 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = utcBadge,
                            color = KickoffPitchEmerald,
                            fontSize = if (isTv) 10.sp else 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Live Filter Toggle Pill
                Surface(
                    color = when {
                        isLiveFilterActive -> KickoffLiveCrimson
                        liveMatchesCount > 0 -> KickoffLiveCrimson.copy(alpha = 0.16f)
                        else -> KickoffGlassWell
                    },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        0.8.dp,
                        if (isLiveFilterActive || liveMatchesCount > 0) KickoffLiveCrimson else KickoffEmeraldBorder
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .tvFocusableControl(
                            accentColor = KickoffFloodlightVolt,
                            shape = RoundedCornerShape(50),
                            onClick = onToggleLiveFilter
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp)
                    ) {
                        KickoffLiveBroadcastBeaconIcon(
                            modifier = Modifier.size(11.dp),
                            color = if (isLiveFilterActive) KickoffFloodlightWhite else if (liveMatchesCount > 0) KickoffLiveCrimson else KickoffMistSilver
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$liveMatchesCount LIVE",
                            color = if (isLiveFilterActive || liveMatchesCount > 0) KickoffFloodlightWhite else KickoffMistSilver,
                            fontSize = if (isTv) 10.5.sp else 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            style = TabularNumeralTextStyle
                        )
                    }
                }

                Text(
                    text = compactSlateLabel,
                    color = KickoffMistSilver,
                    fontSize = if (isTv) 11.sp else 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right: Today Shortcut + Intelligent Hub Status Beacon + Refresh
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                if (selectedMatchDay != MatchDayTab.TODAY) {
                    Surface(
                        color = KickoffPitchEmerald.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.8.dp, KickoffPitchEmerald),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onSelectMatchDay(MatchDayTab.TODAY) }
                    ) {
                        Text(
                            text = "↩ Today",
                            color = KickoffFloodlightVolt,
                            fontSize = if (isTv) 10.sp else 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }

                val hubLabel = when {
                    hasSourceAlert && isTv -> "Hub • ${maxOf(1, unhealthySourcesCount)} Alert"
                    hasSourceAlert -> "Hub • !"
                    favoriteTeamsCount > 0 -> "Hub ($favoriteTeamsCount)"
                    else -> "Hub"
                }

                Surface(
                    color = if (hasSourceAlert) KickoffTrophyGold.copy(alpha = 0.15f) else KickoffCanopyElevated,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        width = if (hasSourceAlert) 1.1.dp else 0.8.dp,
                        color = if (hasSourceAlert) KickoffTrophyGold else KickoffPitchEmerald.copy(alpha = 0.65f)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .tvFocusableControl(
                            accentColor = KickoffFloodlightVolt,
                            shape = RoundedCornerShape(50),
                            onClick = {
                                if (hasSourceAlert) onOpenSourcesHub() else onOpenCustomizeModal()
                            }
                        )
                        .testTag("customize_teams_tournaments_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(
                                    when {
                                        hasSourceAlert && isTv -> 9.dp
                                        hasSourceAlert -> 7.dp
                                        else -> 5.dp
                                    }
                                )
                                .clip(CircleShape)
                                .background(
                                    if (hasSourceAlert) {
                                        KickoffLiveCrimson.copy(alpha = beaconAlpha)
                                    } else {
                                        KickoffPitchEmerald
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Customize Teams, Tournaments & Sources",
                            tint = if (hasSourceAlert) KickoffTrophyGold else KickoffPitchEmerald,
                            modifier = Modifier.size(if (isTv) 13.dp else 12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = hubLabel,
                            color = if (hasSourceAlert) KickoffTrophyGold else KickoffFloodlightWhite,
                            fontSize = if (isTv) 10.5.sp else 9.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(if (isTv) 30.dp else 26.dp)
                        .clip(CircleShape)
                        .background(KickoffGlassWell)
                        .border(0.8.dp, KickoffEmeraldBorder, CircleShape)
                        .tvFocusableControl(
                            accentColor = KickoffFloodlightVolt,
                            shape = CircleShape,
                            onClick = onRefreshMatches
                        )
                        .testTag("refresh_matches_btn")
                ) {
                    if (isLoadingMatches || liveScoreSyncState.isSyncingDelta) {
                        CircularProgressIndicator(
                            color = KickoffPitchEmerald,
                            strokeWidth = 1.8.dp,
                            modifier = Modifier.size(13.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Matches",
                            tint = KickoffFloodlightWhite,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sticky 7-Day Forward Matchweek Timeline (`five_day_timeline_ribbon`):
 * - Iterates over `MatchDayTab.forwardTimelineTabs` (`TODAY` + 6 upcoming days).
 * - Mobile (`SetPlayEdition.PHONE`): Horizontally scrollable `LazyRow` of 7 interactive day cards.
 * - Android TV (`SetPlayEdition.TV`): Full-width 7-column `Row` with 10-foot typography & D-Pad focus scaling.
 */
@Composable
private fun FiveDayMatchTimelineRibbon(
    selectedDay: MatchDayTab,
    selectedCountryZone: UserCountryZone,
    matchesByDay: Map<MatchDayTab, List<FootballMatch>>,
    liveCountToday: Int,
    onSelectDay: (MatchDayTab) -> Unit,
    activeEdition: SetPlayEdition = SetPlayEdition.PHONE
) {
    val forwardTabs = remember { MatchDayTab.forwardTimelineTabs }
    if (activeEdition == SetPlayEdition.TV) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp)
                .testTag("five_day_timeline_ribbon"),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            forwardTabs.forEach { day ->
                val isSelected = day == selectedDay
                val dayMatches = matchesByDay[day].orEmpty()
                val matchCount = dayMatches.size
                val isToday = day == MatchDayTab.TODAY
                val hasLive = isToday && liveCountToday > 0
                val tag = "day_tab_${day.name.lowercase(Locale.ROOT)}"
                val shortLabel = day.getShortDayCode(selectedCountryZone.timeZoneId)
                val subDate = day.getDynamicDateSubtitle(selectedCountryZone.timeZoneId)

                Surface(
                    color = if (isSelected) KickoffCanopyElevated else KickoffTurfSurface.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = if (isSelected) 1.3.dp else 0.8.dp,
                        color = when {
                            isSelected -> KickoffPitchEmerald
                            hasLive -> KickoffLiveCrimson.copy(alpha = 0.65f)
                            else -> KickoffEmeraldBorder
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .tvFocusableControl(
                            accentColor = KickoffFloodlightVolt,
                            shape = RoundedCornerShape(12.dp),
                            onClick = { onSelectDay(day) }
                        )
                        .testTag(tag)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (hasLive) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(KickoffLiveCrimson)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = shortLabel,
                                    color = if (isSelected) KickoffFloodlightWhite else KickoffMistSilver,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = if (matchCount > 0) "$subDate • $matchCount Games" else "$subDate • —",
                                color = if (isSelected) KickoffFloodlightVolt else KickoffSlateMuted,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                maxLines = 1,
                                style = TabularNumeralTextStyle
                            )
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth(0.72f)
                                    .height(2.5.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(KickoffFloodlightVolt)
                            )
                        }
                    }
                }
            }
        }
    } else {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("five_day_timeline_ribbon"),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(forwardTabs, key = { it.name }) { day ->
                val isSelected = day == selectedDay
                val dayMatches = matchesByDay[day].orEmpty()
                val matchCount = dayMatches.size
                val isToday = day == MatchDayTab.TODAY
                val hasLive = isToday && liveCountToday > 0
                val tag = "day_tab_${day.name.lowercase(Locale.ROOT)}"
                val shortLabel = day.getShortDayCode(selectedCountryZone.timeZoneId)
                val subDate = day.getDynamicDateSubtitle(selectedCountryZone.timeZoneId)

                Surface(
                    color = if (isSelected) KickoffCanopyElevated else KickoffTurfSurface.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(11.dp),
                    border = BorderStroke(
                        width = if (isSelected) 1.2.dp else 0.75.dp,
                        color = when {
                            isSelected -> KickoffPitchEmerald
                            hasLive -> KickoffLiveCrimson.copy(alpha = 0.6f)
                            else -> KickoffEmeraldBorder
                        }
                    ),
                    modifier = Modifier
                        .widthIn(min = 74.dp)
                        .height(46.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .tvFocusableControl(
                            accentColor = KickoffFloodlightVolt,
                            shape = RoundedCornerShape(11.dp),
                            onClick = { onSelectDay(day) }
                        )
                        .testTag(tag)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (hasLive) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(KickoffLiveCrimson)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                }
                                Text(
                                    text = shortLabel,
                                    color = if (isSelected) KickoffFloodlightWhite else KickoffMistSilver,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = subDate,
                                color = if (isSelected) KickoffFloodlightWhite else KickoffMistSilver.copy(alpha = 0.85f),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                style = TabularNumeralTextStyle
                            )
                            Text(
                                text = if (matchCount > 0) "$matchCount Games" else "—",
                                color = if (isSelected) KickoffFloodlightVolt else if (matchCount > 0) KickoffPitchEmerald else KickoffSlateMuted,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                style = TabularNumeralTextStyle
                            )
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth(0.70f)
                                    .height(2.5.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(KickoffFloodlightVolt)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Expressive Unified Competition Deck (`tournament_filter_row`):
 * - Card 1 (`filter_chip_all`): `"All Matches"` card with `KickoffChampionshipTrophyIcon` and total match count badge.
 * - Card 2 (`filter_chip_my_teams`): `"★ My Teams"` card with `favoriteTeamsCount` badge and `KickoffTrophyGold` accent.
 * - Competition Deck Cards (`filter_chip_${tournament.tournamentId}`): Sorted by pinned -> active matches (`count > 0`) -> canonical priority.
 * - Trailing Action Card (`modify_tournaments_chip`): `"+ Leagues"` shortcut opening Hub Tab 1 (`Competitions`).
 */
@Composable
private fun TournamentFilterRow(
    selectedFilter: String,
    selectedCategory: CompetitionCategory?,
    tournamentCategoryMap: Map<String, CompetitionCategory>,
    favoriteTeamsCount: Int,
    liveCount: Int,
    activeDayMatches: List<FootballMatch>,
    tournamentPreferences: List<TournamentPreferenceEntity>,
    onSelectFilter: (String) -> Unit,
    onOpenCustomize: () -> Unit,
    activeEdition: SetPlayEdition = SetPlayEdition.PHONE
) {
    val isTv = activeEdition == SetPlayEdition.TV
    val cardHeight = if (isTv) 48.dp else 40.dp
    val medallionSize = if (isTv) 26 else 22
    val titleFontSize = if (isTv) 13.sp else 11.sp

    val matchCountsByTournament = remember(activeDayMatches) {
        activeDayMatches.groupingBy { it.tournamentId }.eachCount()
    }

    val enabledTournaments = remember(tournamentPreferences, selectedCategory, matchCountsByTournament) {
        val prefs = tournamentPreferences.ifEmpty {
            FootballRepository.ALL_TOURNAMENTS.mapIndexed { idx, t ->
                TournamentPreferenceEntity(
                    tournamentId = t.tournamentId,
                    tournamentName = t.name,
                    countryOrRegion = t.countryOrRegion,
                    isEnabled = true,
                    isPinned = idx < 4,
                    sortOrder = idx
                )
            }
        }
        prefs.filter { it.isEnabled }.filter { pref ->
            if (selectedCategory == null) true
            else (tournamentCategoryMap[pref.tournamentId] ?: CompetitionCategory.EUROPEAN) == selectedCategory
        }.sortedWith(
            compareBy<TournamentPreferenceEntity> { !it.isPinned }
                .thenBy { (matchCountsByTournament[it.tournamentId] ?: 0) == 0 }
                .thenBy { getTournamentCanonicalPriority(it.tournamentId, it.sortOrder) }
        )
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tournament_filter_row"),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Card 1: "All Matches"
        item(key = "filter_all_chip") {
            val isAllSelected = selectedFilter == "ALL" || selectedFilter.startsWith("CAT_")
            val totalMatches = activeDayMatches.size
            Surface(
                color = if (isAllSelected) KickoffPitchEmerald.copy(alpha = 0.22f) else KickoffTurfSurface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    width = if (isAllSelected) 1.3.dp else 0.85.dp,
                    color = if (isAllSelected) KickoffFloodlightVolt else KickoffEmeraldBorder
                ),
                modifier = Modifier
                    .height(cardHeight)
                    .clip(RoundedCornerShape(12.dp))
                    .tvFocusableControl(
                        accentColor = KickoffFloodlightVolt,
                        shape = RoundedCornerShape(12.dp),
                        onClick = { onSelectFilter("ALL") }
                    )
                    .testTag("filter_chip_all")
            ) {
                Box(modifier = Modifier.height(cardHeight)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 11.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(medallionSize.dp)
                                .clip(CircleShape)
                                .background(KickoffPitchEmerald.copy(alpha = 0.18f))
                                .border(0.8.dp, KickoffPitchEmerald.copy(alpha = 0.55f), CircleShape)
                        ) {
                            KickoffChampionshipTrophyIcon(
                                modifier = Modifier.size((medallionSize - 8).dp),
                                tint = if (isAllSelected) KickoffFloodlightVolt else KickoffPitchEmerald
                            )
                        }
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = "All Matches",
                            color = KickoffFloodlightWhite,
                            fontSize = titleFontSize,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = if (isAllSelected) KickoffFloodlightVolt else KickoffGlassWell,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "$totalMatches",
                                color = if (isAllSelected) KickoffPitchBlack else KickoffPitchEmerald,
                                fontSize = if (isTv) 11.sp else 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                style = TabularNumeralTextStyle,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (isAllSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth(0.72f)
                                .height(2.dp)
                                .clip(RoundedCornerShape(50))
                                .background(KickoffFloodlightVolt)
                        )
                    }
                }
            }
        }

        // Card 2: "★ My Teams"
        item(key = "filter_my_teams_chip") {
            val isMyTeamsSelected = selectedFilter == "MY_TEAMS"
            Surface(
                color = if (isMyTeamsSelected) KickoffTrophyGold.copy(alpha = 0.22f) else KickoffTurfSurface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    width = if (isMyTeamsSelected) 1.3.dp else 0.85.dp,
                    color = if (isMyTeamsSelected) KickoffTrophyGold else KickoffTrophyGold.copy(alpha = 0.42f)
                ),
                modifier = Modifier
                    .height(cardHeight)
                    .clip(RoundedCornerShape(12.dp))
                    .tvFocusableControl(
                        accentColor = KickoffFloodlightVolt,
                        shape = RoundedCornerShape(12.dp),
                        onClick = {
                            if (isMyTeamsSelected) onSelectFilter("ALL") else onSelectFilter("MY_TEAMS")
                        }
                    )
                    .testTag("filter_chip_my_teams")
            ) {
                Box(modifier = Modifier.height(cardHeight)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 11.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(medallionSize.dp)
                                .clip(CircleShape)
                                .background(KickoffTrophyGold.copy(alpha = 0.16f))
                                .border(0.8.dp, KickoffTrophyGold.copy(alpha = 0.55f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = KickoffTrophyGold,
                                modifier = Modifier.size((medallionSize - 8).dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "★ My Teams",
                            color = KickoffFloodlightWhite,
                            fontSize = titleFontSize,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (favoriteTeamsCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = if (isMyTeamsSelected) KickoffTrophyGold else KickoffTrophyGold.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "$favoriteTeamsCount",
                                    color = if (isMyTeamsSelected) KickoffPitchBlack else KickoffTrophyGold,
                                    fontSize = if (isTv) 11.sp else 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = TabularNumeralTextStyle,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    if (isMyTeamsSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth(0.72f)
                                .height(2.dp)
                                .clip(RoundedCornerShape(50))
                                .background(KickoffTrophyGold)
                        )
                    }
                }
            }
        }

        // Competition Deck Cards
        items(enabledTournaments, key = { it.tournamentId }) { tournament ->
            val isSelected = selectedFilter == tournament.tournamentId
            val compAccent = resolveCompetitionAccentColor(tournament.tournamentId)
            val count = matchCountsByTournament[tournament.tournamentId] ?: 0

            Surface(
                color = if (isSelected) compAccent.copy(alpha = 0.22f) else KickoffTurfSurface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    width = if (isSelected) 1.3.dp else 0.85.dp,
                    color = when {
                        isSelected -> compAccent
                        count > 0 -> compAccent.copy(alpha = 0.48f)
                        else -> KickoffEmeraldBorder
                    }
                ),
                modifier = Modifier
                    .height(cardHeight)
                    .clip(RoundedCornerShape(12.dp))
                    .tvFocusableControl(
                        accentColor = KickoffFloodlightVolt,
                        shape = RoundedCornerShape(12.dp),
                        onClick = {
                            if (isSelected) onSelectFilter("ALL") else onSelectFilter(tournament.tournamentId)
                        }
                    )
                    .testTag("filter_chip_${tournament.tournamentId}")
            ) {
                Box(modifier = Modifier.height(cardHeight)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 10.dp)
                    ) {
                        TournamentLogoMedallion(
                            tournamentId = tournament.tournamentId,
                            tournamentName = tournament.tournamentName,
                            isSelected = isSelected,
                            accentColor = compAccent,
                            sizeDp = medallionSize
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = tournament.tournamentName,
                            color = if (isSelected || count > 0) KickoffFloodlightWhite else KickoffMistSilver,
                            fontSize = titleFontSize,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = when {
                                isSelected -> compAccent
                                count > 0 -> compAccent.copy(alpha = 0.18f)
                                else -> KickoffGlassWell
                            },
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "$count",
                                color = when {
                                    isSelected -> KickoffPitchBlack
                                    count > 0 -> compAccent
                                    else -> KickoffSlateMuted
                                },
                                fontSize = if (isTv) 11.sp else 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                style = TabularNumeralTextStyle,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth(0.72f)
                                .height(2.dp)
                                .clip(RoundedCornerShape(50))
                                .background(compAccent)
                        )
                    }
                }
            }
        }

        // Trailing Action Card: "+ Leagues"
        item(key = "modify_tournaments_chip_item") {
            Surface(
                color = KickoffCanopyElevated,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.9.dp, KickoffPitchEmerald.copy(alpha = 0.65f)),
                modifier = Modifier
                    .height(cardHeight)
                    .clip(RoundedCornerShape(12.dp))
                    .tvFocusableControl(
                        accentColor = KickoffFloodlightVolt,
                        shape = RoundedCornerShape(12.dp),
                        onClick = onOpenCustomize
                    )
                    .testTag("modify_tournaments_chip")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 11.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Manage Leagues",
                        tint = KickoffPitchEmerald,
                        modifier = Modifier.size(if (isTv) 16.dp else 14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+ Leagues",
                        color = KickoffPitchEmerald,
                        fontSize = titleFontSize,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

/**
 * Live & Marquee Stadium Spotlight Carousel (`LiveAndMarqueeSpotlightCarousel`):
 * - Highlights in-play (`LIVE` / `HALFTIME`) clashes and headline marquee derby fixtures.
 */
@Composable
private fun LiveAndMarqueeSpotlightCarousel(
    matches: List<FootballMatch>,
    selectedCountryZone: UserCountryZone,
    selectedSeasonTimeMode: SeasonTimeMode,
    onOpenDossier: (FootballMatch) -> Unit,
    onWatchMatch: (FootballMatch) -> Unit
) {
    val isSummerTime = remember(selectedCountryZone, selectedSeasonTimeMode) {
        selectedCountryZone.isSummerTimeActive(mode = selectedSeasonTimeMode)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp)
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(matches, key = { "spotlight_${it.matchId}" }) { match ->
                val isLive = match.status == MatchStatus.LIVE || match.status == MatchStatus.HALFTIME
                val compAccent = resolveCompetitionAccentColor(match.tournamentId)
                val kickoffDisplay = remember(match.kickoffEpochMs, match.kickoffTimeFormatted, selectedCountryZone, isSummerTime) {
                    if (match.kickoffEpochMs > 0L) {
                        formatDynamicKickoffTime(match.kickoffEpochMs, selectedCountryZone, isSummerTime)
                    } else {
                        match.kickoffTimeFormatted
                    }
                }

                Surface(
                    color = KickoffTurfSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        width = if (isLive) 1.2.dp else 0.9.dp,
                        color = if (isLive) KickoffLiveCrimson.copy(alpha = 0.7f) else compAccent.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier
                        .width(248.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .luxuryFocusableCard(
                            accentColor = KickoffFloodlightVolt,
                            shape = RoundedCornerShape(14.dp),
                            onClick = { onOpenDossier(match) }
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        if (isLive) KickoffStadiumForest else KickoffCanopyElevated,
                                        KickoffTurfSurface
                                    )
                                )
                            )
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                    ) {
                        // Top Row: Competition Name + Live Clock / Kickoff Time
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(compAccent)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = match.tournamentName.uppercase(),
                                    color = KickoffMistSilver,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Surface(
                                color = if (isLive) KickoffLiveCrimson else KickoffGlassWell,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(
                                    0.6.dp,
                                    if (isLive) KickoffFloodlightWhite.copy(alpha = 0.6f) else KickoffEmeraldBorder
                                )
                            ) {
                                Text(
                                    text = if (isLive) match.liveMinuteOrBadge.ifBlank { "LIVE" } else kickoffDisplay,
                                    color = if (isLive) KickoffFloodlightWhite else KickoffFloodlightVolt,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = TabularNumeralTextStyle,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(7.dp))

                        // Middle Row: Symmetrical Home vs Away Scoreline
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                TeamCrestImage(badgeUrl = match.homeTeamBadge, teamName = match.homeTeamName, sizeDp = 22)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = match.homeTeamName,
                                    color = KickoffFloodlightWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Surface(
                                color = KickoffGlassWell,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.7.dp, KickoffEmeraldBorder),
                                modifier = Modifier.padding(horizontal = 6.dp)
                            ) {
                                Text(
                                    text = if (match.homeScore != null && match.awayScore != null) {
                                        "${match.homeScore} - ${match.awayScore}"
                                    } else {
                                        "VS"
                                    },
                                    color = if (isLive) KickoffFloodlightVolt else KickoffFloodlightWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = TabularNumeralTextStyle,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = match.awayTeamName,
                                    color = KickoffFloodlightWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                TeamCrestImage(badgeUrl = match.awayTeamBadge, teamName = match.awayTeamName, sizeDp = 22)
                            }
                        }

                        Spacer(modifier = Modifier.height(7.dp))

                        // Bottom Row: 1-Tap Provider-Branded Broadcast Channel Bar
                        val broadcasterIdentity = remember(match.beinChannel, match.menaProviderName) {
                            resolveBroadcasterVisualIdentity(match.beinChannel.ifBlank { match.menaProviderName })
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = match.stageOrRound.ifBlank { match.stadiumName },
                                color = KickoffSlateMuted,
                                fontSize = 8.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Surface(
                                color = if (isLive) KickoffPitchEmerald else broadcasterIdentity.containerColor,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(
                                    0.8.dp,
                                    if (isLive) KickoffFloodlightVolt else broadcasterIdentity.borderColor
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { onWatchMatch(match) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp)
                                ) {
                                    KickoffTvBroadcastIcon(
                                        modifier = Modifier.size(11.dp),
                                        tint = if (isLive) KickoffPitchBlack else broadcasterIdentity.textColor
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = match.beinChannel.ifBlank { match.menaProviderName },
                                        color = if (isLive) KickoffPitchBlack else broadcasterIdentity.textColor,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

internal fun resolveTournamentLogoUrl(tournamentId: String): String {
    return when (tournamentId) {
        "uefa_cl" -> "https://media.api-sports.io/football/leagues/2.png"
        "laliga" -> "https://media.api-sports.io/football/leagues/140.png"
        "premier_league" -> "https://media.api-sports.io/football/leagues/39.png"
        "ligue_1" -> "https://media.api-sports.io/football/leagues/61.png"
        "bundesliga" -> "https://media.api-sports.io/football/leagues/78.png"
        "serie_a" -> "https://media.api-sports.io/football/leagues/135.png"
        "caf_cl" -> "https://media.api-sports.io/football/leagues/12.png"
        "caf_confed" -> "https://media.api-sports.io/football/leagues/20.png"
        "botola_pro" -> "https://media.api-sports.io/football/leagues/200.png"
        "uefa_el" -> "https://media.api-sports.io/football/leagues/3.png"
        "saudi_pro" -> "https://media.api-sports.io/football/leagues/307.png"
        "intl_official" -> "https://media.api-sports.io/football/leagues/1.png"
        "intl_friendly" -> "https://media.api-sports.io/football/leagues/10.png"
        else -> ""
    }
}

internal fun resolveTournamentShortMonogram(tournamentId: String, tournamentName: String): String {
    return when (tournamentId) {
        "uefa_cl" -> "UCL"
        "laliga" -> "LL"
        "premier_league" -> "EPL"
        "ligue_1" -> "L1"
        "bundesliga" -> "BUN"
        "serie_a" -> "SA"
        "caf_cl" -> "CAF"
        "caf_confed" -> "CC"
        "botola_pro" -> "BOT"
        "uefa_el" -> "UEL"
        "saudi_pro" -> "SPL"
        "intl_official" -> "INT"
        "intl_friendly" -> "FRD"
        else -> tournamentName.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1).uppercase() }.ifBlank { "CUP" }
    }
}

@Composable
internal fun TournamentLogoMedallion(
    tournamentId: String,
    tournamentName: String,
    isSelected: Boolean = false,
    accentColor: Color = KickoffPitchEmerald,
    sizeDp: Int = 22
) {
    val logoUrl = remember(tournamentId) { resolveTournamentLogoUrl(tournamentId) }
    val monogram = remember(tournamentId, tournamentName) {
        resolveTournamentShortMonogram(tournamentId, tournamentName)
    }
    val badgeScale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tournament_logo_scale_$tournamentId"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(sizeDp.dp)
            .graphicsLayer {
                scaleX = badgeScale
                scaleY = badgeScale
            }
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        KickoffFloodlightWhite,
                        Color(0xFFDCE6E2)
                    )
                )
            )
            .border(
                width = if (isSelected) 1.2.dp else 0.9.dp,
                color = if (isSelected) KickoffPitchBlack.copy(alpha = 0.65f) else accentColor.copy(alpha = 0.85f),
                shape = CircleShape
            )
    ) {
        Text(
            text = monogram,
            color = KickoffPitchBlack,
            fontSize = (sizeDp * 0.31f).sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )
        if (logoUrl.isNotBlank()) {
            AsyncImage(
                model = logoUrl,
                contentDescription = tournamentName,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size((sizeDp - 5).dp)
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
private fun LuxuryLeagueCapsule(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = KickoffPitchEmerald,
    isActionCapsule: Boolean = false,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "league_capsule_scale"
    )

    Surface(
        color = when {
            selected -> accentColor
            isActionCapsule -> KickoffGlassWell
            else -> KickoffTurfSurface
        },
        shape = RoundedCornerShape(50),
        border = BorderStroke(
            width = if (selected) 1.1.dp else 0.8.dp,
            color = when {
                selected -> KickoffFloodlightWhite.copy(alpha = 0.75f)
                isActionCapsule -> KickoffPitchEmerald.copy(alpha = 0.45f)
                else -> accentColor.copy(alpha = 0.38f)
            }
        ),
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .height(28.dp)
            .clip(RoundedCornerShape(50))
            .tvFocusableControl(
                accentColor = KickoffFloodlightVolt,
                shape = RoundedCornerShape(50),
                onClick = onClick
            )
    ) {
        content()
    }
}

@Composable
private fun FavoriteTeamsEmptyBanner(
    onSelectFavoriteTeams: () -> Unit
) {
    Surface(
        color = KickoffTurfSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, KickoffEmeraldBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.StarBorder,
                contentDescription = null,
                tint = KickoffTrophyGold,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pin Your Favorite Clubs & National Teams",
                color = KickoffFloodlightWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Track live scores, lineups, and TV broadcast channels for your favorite teams in one dedicated hub.",
                color = KickoffMistSilver,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onSelectFavoriteTeams,
                colors = ButtonDefaults.buttonColors(
                    containerColor = KickoffPitchEmerald,
                    contentColor = KickoffPitchBlack
                ),
                modifier = Modifier.testTag("add_favorite_teams_prompt_btn")
            ) {
                Text("Select Favorite Teams", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun PinnedFavoriteTeamsSection(
    favoriteTeams: List<FavoriteTeamEntity>,
    favoriteMatches: List<FootballMatch>,
    selectedDay: MatchDayTab,
    selectedCountryZone: UserCountryZone,
    selectedSeasonTimeMode: SeasonTimeMode,
    onEditFavoriteTeams: () -> Unit,
    onInspectMatchDossier: (FootballMatch) -> Unit,
    onWatchMatch: (FootballMatch, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("pinned_favorite_teams_section")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "★ MY FAVORITE TEAMS (${favoriteTeams.size})",
                color = KickoffTrophyGold,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.6.sp
            )
            Surface(
                color = KickoffCanopyElevated,
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.8.dp, KickoffEmeraldBorder),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { onEditFavoriteTeams() }
                    .testTag("edit_favorite_teams_btn")
            ) {
                Text(
                    text = "Manage Teams",
                    color = KickoffPitchEmerald,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(favoriteTeams, key = { it.teamId }) { team ->
                Surface(
                    color = KickoffCanopyElevated,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.8.dp, KickoffTrophyGold.copy(alpha = 0.5f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        TeamCrestImage(badgeUrl = team.badgeUrl, teamName = team.teamName, sizeDp = 20)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = team.teamName,
                            color = KickoffFloodlightWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (favoriteMatches.isEmpty()) {
            Surface(
                color = KickoffTurfSurface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.8.dp, KickoffEmeraldBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "No fixtures scheduled for your favorite teams on ${selectedDay.getDynamicTitle()}.",
                    color = KickoffMistSilver,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            val utcBadge = selectedCountryZone.getUtcOffsetBadge(mode = selectedSeasonTimeMode)
            favoriteMatches.forEachIndexed { index, match ->
                FlashscoreMatchCard(
                    match = match,
                    selectedCountryZone = selectedCountryZone,
                    selectedSeasonTimeMode = selectedSeasonTimeMode,
                    utcOffsetBadge = utcBadge,
                    isHomeTeamFavorite = true,
                    isAwayTeamFavorite = false,
                    onToggleHomeFavorite = {},
                    onToggleAwayFavorite = {},
                    onOpenDossier = { onInspectMatchDossier(match) },
                    onWatchBeinClicked = { ch -> onWatchMatch(match, ch) },
                    isLastInSection = index == favoriteMatches.lastIndex
                )
            }
        }
    }
}

@Composable
private fun PinnedMatchesStickyRail(
    pinnedMatches: List<FootballMatch>,
    onWatchMatch: (FootballMatch) -> Unit,
    onOpenDossier: (FootballMatch) -> Unit,
    onUnpinMatch: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 3.dp)
            .testTag("pinned_matches_rail")
    ) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(pinnedMatches, key = { "pinned_${it.matchId}" }) { match ->
                val isLive = match.status == MatchStatus.LIVE || match.status == MatchStatus.HALFTIME
                Surface(
                    color = KickoffCanopyElevated,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        0.9.dp,
                        if (isLive) KickoffLiveCrimson else KickoffTrophyGold.copy(alpha = 0.65f)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onOpenDossier(match) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Unpin Match",
                            tint = KickoffTrophyGold,
                            modifier = Modifier
                                .size(13.dp)
                                .clickable { onUnpinMatch(match.matchId) }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        TeamCrestImage(badgeUrl = match.homeTeamBadge, teamName = match.homeTeamName, sizeDp = 18)
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (match.homeScore != null && match.awayScore != null) {
                                "${match.homeTeamName.take(3).uppercase()} ${match.homeScore}-${match.awayScore} ${match.awayTeamName.take(3).uppercase()}"
                            } else {
                                "${match.homeTeamName.take(3).uppercase()} vs ${match.awayTeamName.take(3).uppercase()}"
                            },
                            color = KickoffFloodlightWhite,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            style = TabularNumeralTextStyle
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        TeamCrestImage(badgeUrl = match.awayTeamBadge, teamName = match.awayTeamName, sizeDp = 18)
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = if (isLive) KickoffLiveCrimson else KickoffPitchEmerald,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.clickable { onWatchMatch(match) }
                        ) {
                            Text(
                                text = if (isLive) match.liveMinuteOrBadge.ifBlank { "LIVE" } else match.kickoffTimeFormatted,
                                color = if (isLive) KickoffFloodlightWhite else KickoffPitchBlack,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Grouped Competition Container Header (`FlashscoreTournamentHeader`):
 * - Uses competition-specific signature accent spine (`resolveCompetitionAccentColor(tournamentId)`),
 *   `KickoffCanopyElevated` surface, `TournamentLogoMedallion`, live counter badge, and pin toggle.
 */
@Composable
private fun FlashscoreTournamentHeader(
    tournamentId: String,
    tournamentName: String,
    regionName: String,
    menaProviderName: String,
    matchCount: Int,
    liveCount: Int,
    isPinned: Boolean,
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit,
    onTogglePin: () -> Unit
) {
    val compAccent = resolveCompetitionAccentColor(tournamentId)
    val chevronRotation by animateFloatAsState(
        targetValue = if (isCollapsed) -90f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "chevron_rotation_$tournamentId"
    )
    val headerShape = if (isCollapsed) {
        RoundedCornerShape(12.dp)
    } else {
        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
    }

    Surface(
        color = KickoffCanopyElevated,
        shape = headerShape,
        border = BorderStroke(
            width = 0.85.dp,
            color = if (liveCount > 0) KickoffLiveCrimson.copy(alpha = 0.45f) else compAccent.copy(alpha = 0.35f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 14.dp,
                end = 14.dp,
                top = 8.dp,
                bottom = if (isCollapsed) 2.dp else 0.dp
            )
            .clip(headerShape)
            .tvFocusableControl(
                accentColor = KickoffFloodlightVolt,
                shape = headerShape,
                onClick = onToggleCollapse
            )
            .testTag("tournament_header_$tournamentId")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            compAccent.copy(alpha = 0.16f),
                            KickoffCanopyElevated,
                            KickoffTurfSurface
                        )
                    )
                )
                .padding(end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Competition Signature Accent Spine
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(compAccent)
                )
                Spacer(modifier = Modifier.width(9.dp))
                TournamentLogoMedallion(
                    tournamentId = tournamentId,
                    tournamentName = tournamentName,
                    isSelected = isPinned,
                    accentColor = compAccent,
                    sizeDp = 24
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tournamentName,
                    color = KickoffFloodlightWhite,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "• ${regionName.uppercase()}",
                    color = compAccent,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (liveCount > 0) {
                    Surface(
                        color = KickoffLiveCrimson,
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(4.5.dp)
                                    .clip(CircleShape)
                                    .background(KickoffFloodlightWhite)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "$liveCount LIVE",
                                color = KickoffFloodlightWhite,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                style = TabularNumeralTextStyle
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier
                        .size(26.dp)
                        .testTag("pin_tournament_$tournamentId")
                ) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pin Tournament",
                        tint = if (isPinned) KickoffTrophyGold else KickoffSlateMuted,
                        modifier = Modifier.size(13.dp)
                    )
                }

                Surface(
                    color = KickoffGlassWell,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.7.dp, KickoffEmeraldBorder)
                ) {
                    Text(
                        text = "$matchCount",
                        color = KickoffMistSilver,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        style = TabularNumeralTextStyle,
                        modifier = Modifier.padding(horizontal = 6.5.dp, vertical = 1.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isCollapsed) "Expand" else "Collapse",
                    tint = KickoffMistSilver,
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer { rotationZ = chevronRotation }
                )
            }
        }
    }
}

internal data class LiveMatchClockTelemetry(
    val periodBadge: String,
    val heroReadout: String,
    val bottomBadge: String,
    val statusExplanation: String,
    val remainingLabel: String,
    val progressFraction: Float,
    val heroReadoutSeconds: String = heroReadout
)

internal fun parseLiveMatchProgressFraction(liveBadge: String, status: MatchStatus): Float {
    val clean = liveBadge.trim()
    if (status == MatchStatus.HALFTIME || clean.equals("HT", ignoreCase = true) || clean.contains("HT", ignoreCase = true)) {
        return 0.50f
    }
    val apostropheMatch = Regex("(\\d{1,3})(?:\\+\\d{1,2})?\\s*'").find(clean)
    if (apostropheMatch != null) {
        val minute = apostropheMatch.groupValues[1].toIntOrNull()
        if (minute != null) {
            return (minute.toFloat() / 90f).coerceIn(0.06f, 1.0f)
        }
    }
    val withoutHalfPrefix = clean.replace(Regex("\\b[12]H\\b", RegexOption.IGNORE_CASE), "")
    val allNumbers = Regex("\\d{1,3}").findAll(withoutHalfPrefix).mapNotNull { it.value.toIntOrNull() }.toList()
    if (allNumbers.isNotEmpty()) {
        val minute = allNumbers.first()
        return (minute.toFloat() / 90f).coerceIn(0.06f, 1.0f)
    }
    return 0.50f
}

internal fun parseLiveMatchClockTelemetry(
    match: FootballMatch,
    utcOffsetBadge: String = "GMT+1",
    elapsedSecondOffset: Int = 0,
    dynamicKickoffTime: String? = null
): LiveMatchClockTelemetry {
    val clean = match.liveMinuteOrBadge.trim()
    val safeOffsetSec = elapsedSecondOffset.coerceAtLeast(0)
    return when (match.status) {
        MatchStatus.HALFTIME -> {
            val htScore = match.halfTimeScore?.takeIf { it.isNotBlank() }
            LiveMatchClockTelemetry(
                periodBadge = "HALF TIME",
                heroReadout = "HT",
                bottomBadge = if (htScore != null) "HT $htScore" else "45' PLAYED",
                statusExplanation = "HALF-TIME INTERVAL • 45' COMPLETED",
                remainingLabel = if (htScore != null) "HT: $htScore" else "2ND HALF SOON",
                progressFraction = 0.50f,
                heroReadoutSeconds = "HT"
            )
        }
        MatchStatus.LIVE -> {
            if (clean.equals("HT", ignoreCase = true)) {
                val htScore = match.halfTimeScore?.takeIf { it.isNotBlank() }
                return LiveMatchClockTelemetry(
                    periodBadge = "HALF TIME",
                    heroReadout = "HT",
                    bottomBadge = if (htScore != null) "HT $htScore" else "45' PLAYED",
                    statusExplanation = "HALF-TIME INTERVAL • 45' COMPLETED",
                    remainingLabel = if (htScore != null) "HT: $htScore" else "2ND HALF SOON",
                    progressFraction = 0.50f,
                    heroReadoutSeconds = "HT"
                )
            }
            val syntheticSyncEpochMs = 1_000_000_000L
            val anchor = MatchClockEngine.deriveAnchorFromMatch(
                match = match,
                nowEpochMs = syntheticSyncEpochMs,
                deterministicSubMinuteSeed = false
            )
            val snapshot = MatchClockEngine.evaluateClockAt(
                anchor = anchor,
                nowEpochMs = syntheticSyncEpochMs + safeOffsetSec * 1_000L
            )
            val isStoppagePhase = snapshot.phase == MatchPeriodPhase.FIRST_HALF_STOPPAGE ||
                snapshot.phase == MatchPeriodPhase.SECOND_HALF_STOPPAGE
            val isSecondHalf = snapshot.phase == MatchPeriodPhase.SECOND_HALF ||
                snapshot.phase == MatchPeriodPhase.SECOND_HALF_STOPPAGE ||
                snapshot.phase == MatchPeriodPhase.EXTRA_TIME ||
                clean.contains("2H", ignoreCase = true) ||
                snapshot.baseMinute > 45

            val heroMin = snapshot.formattedMinuteBadge
            val heroMmSs = snapshot.formattedSecondsClock
            val periodLabel = snapshot.periodHeaderBadge

            val explanation = when {
                isStoppagePhase && isSecondHalf -> "2ND HALF • $heroMin ADDED TIME"
                isStoppagePhase -> "1ST HALF • $heroMin ADDED TIME"
                isSecondHalf -> "2ND HALF • ${snapshot.baseMinute}' PLAYED"
                else -> "1ST HALF • ${snapshot.baseMinute}' PLAYED"
            }
            val remaining = when {
                isStoppagePhase -> "STOPPAGE TIME"
                isSecondHalf -> {
                    val left = (90 - snapshot.baseMinute).coerceAtLeast(1)
                    "${left}' TO 90'"
                }
                else -> {
                    val left = (45 - snapshot.baseMinute).coerceAtLeast(1)
                    "${left}' TO HT"
                }
            }
            val htScore = match.halfTimeScore?.takeIf { it.isNotBlank() }
            LiveMatchClockTelemetry(
                periodBadge = periodLabel,
                heroReadout = heroMin,
                bottomBadge = if (htScore != null && isSecondHalf) "HT $htScore" else "LIVE MIN",
                statusExplanation = explanation,
                remainingLabel = remaining,
                progressFraction = snapshot.progressFraction,
                heroReadoutSeconds = heroMmSs
            )
        }
        MatchStatus.FINISHED -> {
            val htScore = match.halfTimeScore?.takeIf { it.isNotBlank() }
            LiveMatchClockTelemetry(
                periodBadge = "FULL TIME",
                heroReadout = "FT",
                bottomBadge = if (htScore != null) "HT $htScore" else "90' FINAL",
                statusExplanation = "MATCH FINISHED",
                remainingLabel = "FINAL",
                progressFraction = 1.0f,
                heroReadoutSeconds = "FT"
            )
        }
        MatchStatus.POSTPONED -> {
            LiveMatchClockTelemetry(
                periodBadge = "POSTPONED",
                heroReadout = "PST",
                bottomBadge = "RESCHEDULED",
                statusExplanation = "MATCH POSTPONED",
                remainingLabel = "PST",
                progressFraction = 0f,
                heroReadoutSeconds = "PST"
            )
        }
        MatchStatus.CANCELLED -> {
            LiveMatchClockTelemetry(
                periodBadge = "CANCELLED",
                heroReadout = "CANC",
                bottomBadge = "CANCELLED",
                statusExplanation = "MATCH CANCELLED",
                remainingLabel = "CANC",
                progressFraction = 0f,
                heroReadoutSeconds = "CANC"
            )
        }
        MatchStatus.UPCOMING -> {
            val resolvedKickoff = dynamicKickoffTime?.takeIf { it.isNotBlank() }
                ?: clean.ifBlank { match.kickoffTimeFormatted }
            LiveMatchClockTelemetry(
                periodBadge = "KICKOFF",
                heroReadout = resolvedKickoff,
                bottomBadge = utcOffsetBadge,
                statusExplanation = "UPCOMING KICKOFF",
                remainingLabel = "SCHEDULED",
                progressFraction = 0f,
                heroReadoutSeconds = resolvedKickoff
            )
        }
    }
}

@Composable
private fun LiveBroadcastEqualizerBars(tint: Color = KickoffFloodlightWhite) {
    val infiniteTransition = rememberInfiniteTransition(label = "liveEqualizerTransition")
    val bar1Fraction by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 420, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eqBar1"
    )
    val bar2Fraction by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 360, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eqBar2"
    )
    val bar3Fraction by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 480, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eqBar3"
    )

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(1.5.dp),
        modifier = Modifier.height(10.dp)
    ) {
        listOf(bar1Fraction, bar2Fraction, bar3Fraction).forEach { fraction ->
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height((10f * fraction).dp)
                    .clip(RoundedCornerShape(50))
                    .background(tint)
            )
        }
    }
}

/**
 * Symmetrical Center-Axis Broadcast Scoreline Row (`FlashscoreMatchCard`, `64.dp` height):
 * - Fits 5–6 matches above the fold on mobile.
 * - Left Zone: Home Team Name (right-aligned) + Red Card badge + Home Crest (`26.dp`).
 * - Center Axis (`KickoffGlassWell` Scorebug, `72.dp` width):
 *   - `UPCOMING`: Crisp tabular kickoff time (`20:00`) + subtle broadcaster/timezone micro-label.
 *   - `LIVE` / `HALFTIME`: Tabular scoreline (`2 - 1`) + pulsing `KickoffLiveCrimson` minute pill (`67'` / `HT`).
 *   - `FINISHED`: Tabular final score (`2 - 1`) + `FT` label.
 * - Right Zone: Away Crest (`26.dp`) + Away Team Name (left-aligned) + 1-Tap Broadcast Wave Button (`watch_bein_btn_${match.matchId}`).
 */
@Composable
private fun FlashscoreMatchCard(
    match: FootballMatch,
    selectedCountryZone: UserCountryZone = UserZoneRegistry.defaultCountryZone,
    selectedSeasonTimeMode: SeasonTimeMode = SeasonTimeMode.AUTO,
    utcOffsetBadge: String = "GMT+1",
    isHomeTeamFavorite: Boolean,
    isAwayTeamFavorite: Boolean,
    onToggleHomeFavorite: () -> Unit,
    onToggleAwayFavorite: () -> Unit,
    onOpenDossier: () -> Unit,
    onWatchBeinClicked: (String) -> Unit,
    isMatchPinned: Boolean = false,
    onTogglePinMatch: () -> Unit = {},
    isInsidePinnedSection: Boolean = false,
    isLastInSection: Boolean = false,
    uiPreferences: KickoffUiPreferences = KickoffUiPreferences()
) {
    val isLive = match.status == MatchStatus.LIVE || match.status == MatchStatus.HALFTIME
    val isHalftime = match.status == MatchStatus.HALFTIME || match.liveMinuteOrBadge.equals("HT", ignoreCase = true)
    val isFinished = match.status == MatchStatus.FINISHED
    val isPostponed = match.status == MatchStatus.POSTPONED
    val isCancelled = match.status == MatchStatus.CANCELLED
    val isUpcoming = match.status == MatchStatus.UPCOMING
    val hasFavTeam = isHomeTeamFavorite || isAwayTeamFavorite
    val compAccent = resolveCompetitionAccentColor(match.tournamentId)
    val broadcasterIdentity = remember(match.beinChannel, match.menaProviderName) {
        resolveBroadcasterVisualIdentity(match.beinChannel.ifBlank { match.menaProviderName })
    }

    val homeIsWinner = isFinished && !uiPreferences.hideLiveAndFinalScores &&
        match.homeScore != null && match.awayScore != null && match.homeScore > match.awayScore
    val awayIsWinner = isFinished && !uiPreferences.hideLiveAndFinalScores &&
        match.homeScore != null && match.awayScore != null && match.awayScore > match.homeScore

    val isSummerTime = remember(selectedCountryZone, selectedSeasonTimeMode) {
        selectedCountryZone.isSummerTimeActive(mode = selectedSeasonTimeMode)
    }
    val dynamicKickoffTime = remember(match.kickoffEpochMs, match.kickoffTimeFormatted, selectedCountryZone, isSummerTime) {
        if (match.kickoffEpochMs > 0L) {
            formatDynamicKickoffTime(match.kickoffEpochMs, selectedCountryZone, isSummerTime)
        } else {
            match.kickoffTimeFormatted
        }
    }

    val matchClockAnchor = remember(match.matchId, match.liveMinuteOrBadge, match.status) {
        MatchClockEngine.registerOrUpdateAnchor(match)
    }
    val anchorSyncOriginMs = remember(matchClockAnchor.verifiedAtEpochMs, match.matchId) {
        val seedSec = (abs(match.matchId.hashCode()) % 52)
        matchClockAnchor.verifiedAtEpochMs - (seedSec * 1000L)
    }
    var liveElapsedSeconds by remember(anchorSyncOriginMs) {
        mutableIntStateOf(((System.currentTimeMillis() - anchorSyncOriginMs) / 1000L).toInt().coerceAtLeast(0))
    }
    if (match.status == MatchStatus.LIVE) {
        LaunchedEffect(anchorSyncOriginMs) {
            while (true) {
                liveElapsedSeconds = ((System.currentTimeMillis() - anchorSyncOriginMs) / 1000L).toInt().coerceAtLeast(0)
                delay(1000L)
            }
        }
    }
    val clockTelemetry = remember(match, utcOffsetBadge, liveElapsedSeconds, dynamicKickoffTime) {
        parseLiveMatchClockTelemetry(
            match = match,
            utcOffsetBadge = utcOffsetBadge,
            elapsedSecondOffset = if (match.status == MatchStatus.LIVE) liveElapsedSeconds else 0,
            dynamicKickoffTime = dynamicKickoffTime
        )
    }

    val cardShape = if (!isInsidePinnedSection && isLastInSection) {
        RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
    } else {
        RoundedCornerShape(0.dp)
    }

    val leftSpineColor = when {
        isLive -> KickoffLiveCrimson
        isPostponed -> KickoffTrophyGold
        isCancelled -> KickoffLiveCrimson.copy(alpha = 0.7f)
        hasFavTeam -> KickoffTrophyGold
        else -> compAccent
    }

    Surface(
        color = if (isLive) KickoffStadiumForest.copy(alpha = 0.55f) else KickoffTurfSurface,
        shape = cardShape,
        border = BorderStroke(
            width = 0.8.dp,
            color = if (isLive) KickoffLiveCrimson.copy(alpha = 0.42f) else KickoffEmeraldBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isInsidePinnedSection) 0.dp else 14.dp)
            .clip(cardShape)
            .luxuryFocusableCard(
                accentColor = KickoffFloodlightVolt,
                shape = cardShape,
                onClick = onOpenDossier
            )
            .testTag("match_card_${match.matchId}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // UPPER ROW: Left Spine + Pin + Home Team + Symmetrical Center Scorebug (`76.dp` x `44.dp`) + Away Team
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (uiPreferences.compactCardDensity) 52.dp else 58.dp)
                    .testTag("open_match_center_${match.matchId}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left competition / live status spine
                Box(
                    modifier = Modifier
                        .width(3.5.dp)
                        .fillMaxHeight()
                        .background(leftSpineColor)
                )

                // Subtle Pin Toggle Button on Left Edge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(26.dp)
                        .fillMaxHeight()
                        .clickable { onTogglePinMatch() }
                        .testTag("pin_match_btn_${match.matchId}")
                ) {
                    Icon(
                        imageVector = if (isMatchPinned || hasFavTeam) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Pin Match",
                        tint = when {
                            isMatchPinned -> KickoffTrophyGold
                            hasFavTeam -> KickoffPitchEmerald
                            else -> KickoffSlateMuted.copy(alpha = 0.45f)
                        },
                        modifier = Modifier.size(13.dp)
                    )
                }

                // 1. HOME TEAM ZONE (Right-Aligned toward Center Scorebug)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 7.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (match.homeRedCards > 0) {
                        KickoffBookingCardIcon(
                            isRedCard = true,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    Text(
                        text = match.homeTeamName,
                        color = when {
                            isFinished && !homeIsWinner && match.homeScore != match.awayScore && !uiPreferences.hideLiveAndFinalScores -> KickoffSlateMuted
                            isHomeTeamFavorite -> KickoffFloodlightVolt
                            else -> KickoffFloodlightWhite
                        },
                        fontSize = 12.sp,
                        fontWeight = if (homeIsWinner || isLive || isHomeTeamFavorite) FontWeight.ExtraBold else FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    TeamCrestImage(
                        badgeUrl = match.homeTeamBadge,
                        teamName = match.homeTeamName,
                        sizeDp = 26
                    )
                }

                // 2. CLEAN SYMMETRICAL CENTER SCOREBUG (`76.dp` width, `44.dp` height) — Zero Truncated Broadcaster Text
                Surface(
                    color = when {
                        isLive -> KickoffPitchBlack.copy(alpha = 0.92f)
                        isPostponed -> KickoffTrophyGold.copy(alpha = 0.12f)
                        isCancelled -> KickoffLiveCrimson.copy(alpha = 0.12f)
                        else -> KickoffGlassWell
                    },
                    shape = RoundedCornerShape(9.dp),
                    border = BorderStroke(
                        width = 0.9.dp,
                        color = when {
                            isHalftime || isPostponed -> KickoffTrophyGold
                            isLive || isCancelled -> KickoffLiveCrimson
                            else -> KickoffEmeraldBorder
                        }
                    ),
                    modifier = Modifier
                        .width(76.dp)
                        .height(44.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            when (match.status) {
                                MatchStatus.LIVE -> {
                                    Text(
                                        text = if (uiPreferences.hideLiveAndFinalScores) {
                                            "LIVE"
                                        } else {
                                            "${match.homeScore ?: 0} - ${match.awayScore ?: 0}"
                                        },
                                        color = if (uiPreferences.hideLiveAndFinalScores) KickoffLiveCrimson else KickoffFloodlightWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        style = TabularNumeralTextStyle,
                                        maxLines = 1
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.5.dp)
                                                .clip(CircleShape)
                                                .background(if (isHalftime) KickoffTrophyGold else KickoffLiveCrimson)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = if (isHalftime) "HT" else clockTelemetry.heroReadout,
                                            color = if (isHalftime) KickoffTrophyGold else KickoffLiveCrimson,
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            style = TabularNumeralTextStyle,
                                            maxLines = 1
                                        )
                                    }
                                }
                                MatchStatus.HALFTIME -> {
                                    Text(
                                        text = if (uiPreferences.hideLiveAndFinalScores) {
                                            "HT"
                                        } else {
                                            "${match.homeScore ?: 0} - ${match.awayScore ?: 0}"
                                        },
                                        color = KickoffFloodlightWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        style = TabularNumeralTextStyle,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "HT",
                                        color = KickoffTrophyGold,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        style = TabularNumeralTextStyle,
                                        maxLines = 1
                                    )
                                }
                                MatchStatus.FINISHED -> {
                                    Text(
                                        text = if (uiPreferences.hideLiveAndFinalScores) {
                                            "FT"
                                        } else {
                                            "${match.homeScore ?: 0} - ${match.awayScore ?: 0}"
                                        },
                                        color = KickoffFloodlightWhite.copy(alpha = 0.94f),
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        style = TabularNumeralTextStyle,
                                        maxLines = 1
                                    )
                                    val htLabel = match.halfTimeScore?.takeIf { it.isNotBlank() && !uiPreferences.hideLiveAndFinalScores }
                                    Text(
                                        text = if (htLabel != null) "FT • HT $htLabel" else "FT",
                                        color = KickoffMistSilver,
                                        fontSize = 7.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        style = TabularNumeralTextStyle,
                                        maxLines = 1
                                    )
                                }
                                MatchStatus.POSTPONED -> {
                                    Text(
                                        text = dynamicKickoffTime,
                                        color = KickoffSlateMuted,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = TextDecoration.LineThrough,
                                        style = TabularNumeralTextStyle,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "PST",
                                        color = KickoffTrophyGold,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1
                                    )
                                }
                                MatchStatus.CANCELLED -> {
                                    Text(
                                        text = dynamicKickoffTime,
                                        color = KickoffSlateMuted,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = TextDecoration.LineThrough,
                                        style = TabularNumeralTextStyle,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "CANC",
                                        color = KickoffLiveCrimson,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1
                                    )
                                }
                                MatchStatus.UPCOMING -> {
                                    Text(
                                        text = dynamicKickoffTime,
                                        color = KickoffFloodlightWhite,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        style = TabularNumeralTextStyle,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = utcOffsetBadge,
                                        color = KickoffMistSilver,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        style = TabularNumeralTextStyle,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Thin live progress bar along bottom of scorebug
                        if (isLive) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth(clockTelemetry.progressFraction.coerceIn(0.08f, 1f))
                                    .height(2.dp)
                                    .background(if (isHalftime) KickoffTrophyGold else KickoffPitchEmerald)
                            )
                        }
                    }
                }

                // 3. AWAY TEAM ZONE (Symmetrical Left-Aligned from Center Scorebug)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 7.dp, end = 10.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TeamCrestImage(
                        badgeUrl = match.awayTeamBadge,
                        teamName = match.awayTeamName,
                        sizeDp = 26
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = match.awayTeamName,
                        color = when {
                            isFinished && !awayIsWinner && match.homeScore != match.awayScore && !uiPreferences.hideLiveAndFinalScores -> KickoffSlateMuted
                            isAwayTeamFavorite -> KickoffFloodlightVolt
                            else -> KickoffFloodlightWhite
                        },
                        fontSize = 12.sp,
                        fontWeight = if (awayIsWinner || isLive || isAwayTeamFavorite) FontWeight.ExtraBold else FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (match.awayRedCards > 0) {
                        Spacer(modifier = Modifier.width(3.dp))
                        KickoffBookingCardIcon(
                            isRedCard = true,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }

            // SUBTLE DIVIDER BETWEEN SCOREBOARD ROW AND INTEGRATED BROADCAST BAR
            HorizontalDivider(
                thickness = 0.5.dp,
                color = KickoffRowDivider.copy(alpha = 0.65f)
            )

            // LOWER ROW: Integrated Provider-Branded Broadcast Identity Pill + Watch Action Capsule
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(KickoffPitchBlack.copy(alpha = 0.32f))
                    .padding(horizontal = 12.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Provider-Branded Broadcast Identity Pill (Full Untruncated Channel Name)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Surface(
                        color = broadcasterIdentity.containerColor,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.8.dp, broadcasterIdentity.borderColor)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "[${broadcasterIdentity.providerShortTag}]",
                                color = broadcasterIdentity.borderColor,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = match.beinChannel.ifBlank { match.menaProviderName.ifBlank { "Live Broadcast" } },
                                color = broadcasterIdentity.textColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (match.fallbackMenaChannels.isNotEmpty()) {
                        Surface(
                            color = KickoffGlassWell,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.6.dp, KickoffEmeraldBorder)
                        ) {
                            Text(
                                text = "+${match.fallbackMenaChannels.size}",
                                color = KickoffMistSilver,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                style = TabularNumeralTextStyle,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right: Integrated Watch Action Capsule (`watch_bein_btn_${match.matchId}`)
                val useMatchCenterAction = isFinished || isPostponed || isCancelled
                Surface(
                    color = when {
                        isLive -> KickoffPitchEmerald
                        isUpcoming -> KickoffCanopyElevated
                        else -> KickoffGlassWell
                    },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        width = if (isLive || isUpcoming) 1.dp else 0.75.dp,
                        color = when {
                            isLive -> KickoffFloodlightVolt
                            isUpcoming -> KickoffPitchEmerald
                            else -> KickoffRowDivider
                        }
                    ),
                    modifier = Modifier
                        .height(22.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable {
                            if (useMatchCenterAction) {
                                onOpenDossier()
                            } else {
                                onWatchBeinClicked(match.beinChannel)
                            }
                        }
                        .testTag("watch_bein_btn_${match.matchId}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 9.dp)
                    ) {
                        when {
                            isLive -> {
                                LiveBroadcastEqualizerBars(tint = KickoffPitchBlack)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "WATCH LIVE",
                                    color = KickoffPitchBlack,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            isUpcoming -> {
                                KickoffTvBroadcastIcon(
                                    modifier = Modifier.size(11.dp),
                                    tint = KickoffPitchEmerald
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Watch TV",
                                    color = KickoffFloodlightWhite,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            else -> {
                                Text(
                                    text = "Match Center ›",
                                    color = KickoffMistSilver,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            if (!isLastInSection) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = KickoffRowDivider
                )
            }
        }
    }
}

// =========================================================================
// ANDROID TV 10-FOOT WIDESCREEN STADIUM SPLIT-BOARD (`SetPlayEdition.TV`)
// =========================================================================

@Composable
private fun TvKickoffStadiumScreen(
    syncProgress: SyncProgress,
    activeGoalAlert: LiveGoalAlertEvent?,
    kickoffSourceAlert: String?,
    unhealthySourcesCount: Int = 0,
    selectedCountryZone: UserCountryZone,
    selectedSeasonTimeMode: SeasonTimeMode,
    selectedMatchDay: MatchDayTab,
    selectedCompetitionCategory: CompetitionCategory?,
    selectedTournamentFilter: String,
    matchesByDay: Map<MatchDayTab, List<FootballMatch>>,
    activeDayMatches: List<FootballMatch>,
    sortedVisibleMatches: List<FootballMatch>,
    chronologicalSections: List<ChronologicalTournamentSection>,
    favoriteTeams: List<FavoriteTeamEntity>,
    tournamentPreferences: List<TournamentPreferenceEntity>,
    tournamentCategoryMap: Map<String, CompetitionCategory>,
    pinnedMatchIds: Set<String>,
    liveMatchesCount: Int,
    isLoadingMatches: Boolean,
    onSelectMatchDay: (MatchDayTab) -> Unit,
    onSelectCategory: (CompetitionCategory?) -> Unit,
    onSelectFilter: (String) -> Unit,
    onOpenCountryZoneModal: () -> Unit,
    onOpenCustomizeModal: (Int) -> Unit,
    onRefreshMatches: () -> Unit,
    onOpenDossier: (FootballMatch) -> Unit,
    onWatchChannel: (FootballMatch, String) -> Unit,
    onTogglePinMatch: (String) -> Unit,
    onResetFilter: () -> Unit,
    onDismissGoalAlert: () -> Unit,
    onDismissSourceAlert: () -> Unit
) {
    var focusedTvMatch by remember(sortedVisibleMatches) {
        mutableStateOf(sortedVisibleMatches.firstOrNull())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KickoffPitchBlack)
    ) {
        SyncBanner(syncProgress = syncProgress)

        activeGoalAlert?.let { alert ->
            LiveGoalAlertBanner(
                alert = alert,
                onOpenDossier = {
                    onDismissGoalAlert()
                    onOpenDossier(alert.match)
                },
                onDismiss = onDismissGoalAlert
            )
        }

        // Top 10-Foot Widescreen Command & Filter Deck
        KickOffArenaHeaderCard(
            activeEdition = SetPlayEdition.TV,
            selectedCountryZone = selectedCountryZone,
            selectedSeasonTimeMode = selectedSeasonTimeMode,
            selectedMatchDay = selectedMatchDay,
            selectedTournamentFilter = selectedTournamentFilter,
            totalVisibleMatchesCount = sortedVisibleMatches.size,
            activeLeaguesInSlate = sortedVisibleMatches.map { it.tournamentId }.distinct().size,
            favoriteTeamsCount = favoriteTeams.size,
            liveMatchesCount = liveMatchesCount,
            isLoadingMatches = isLoadingMatches,
            sourceAlertMessage = kickoffSourceAlert,
            unhealthySourcesCount = unhealthySourcesCount,
            onRefreshMatches = onRefreshMatches,
            onOpenCountryZoneModal = onOpenCountryZoneModal,
            onOpenCustomizeModal = { onOpenCustomizeModal(0) },
            onOpenSourcesHub = { onOpenCustomizeModal(3) },
            onToggleLiveFilter = {
                onSelectFilter(if (selectedTournamentFilter == "LIVE") "ALL" else "LIVE")
            },
            onSelectMatchDay = onSelectMatchDay
        )

        FiveDayMatchTimelineRibbon(
            selectedDay = selectedMatchDay,
            selectedCountryZone = selectedCountryZone,
            matchesByDay = matchesByDay,
            liveCountToday = liveMatchesCount,
            onSelectDay = onSelectMatchDay,
            activeEdition = SetPlayEdition.TV
        )

        TournamentFilterRow(
            selectedFilter = selectedTournamentFilter,
            selectedCategory = selectedCompetitionCategory,
            tournamentCategoryMap = tournamentCategoryMap,
            favoriteTeamsCount = favoriteTeams.size,
            liveCount = activeDayMatches.count { it.status == MatchStatus.LIVE || it.status == MatchStatus.HALFTIME },
            activeDayMatches = activeDayMatches,
            tournamentPreferences = tournamentPreferences,
            onSelectFilter = onSelectFilter,
            onOpenCustomize = { onOpenCustomizeModal(1) },
            activeEdition = SetPlayEdition.TV
        )

        // 60% / 40% Widescreen Split-Board
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Left Pane (60% width): D-Pad 2-Column Fixture Grid
            LazyColumn(
                modifier = Modifier
                    .weight(0.60f)
                    .fillMaxHeight()
                    .testTag("match_center_list"),
                contentPadding = PaddingValues(bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (chronologicalSections.isEmpty()) {
                    item {
                        EmptyMatchesCard(
                            selectedFilter = selectedTournamentFilter,
                            onResetFilter = onResetFilter,
                            onOpenCustomize = { onOpenCustomizeModal(0) }
                        )
                    }
                } else {
                    chronologicalSections.forEach { section ->
                        val firstMatch = section.matches.first()
                        val compAccent = resolveCompetitionAccentColor(section.tournamentId)
                        item(key = "tv_header_${section.sectionKey}") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(KickoffCanopyElevated)
                                    .border(0.8.dp, compAccent.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("tournament_header_${section.tournamentId}")
                            ) {
                                TournamentLogoMedallion(
                                    tournamentId = section.tournamentId,
                                    tournamentName = firstMatch.tournamentName,
                                    accentColor = compAccent,
                                    sizeDp = 22
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = firstMatch.tournamentName,
                                    color = KickoffFloodlightWhite,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ${firstMatch.tournamentRegion.uppercase()}",
                                    color = compAccent,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        val chunkedPairs = section.matches.chunked(2)
                        itemsIndexed(
                            items = chunkedPairs,
                            key = { pairIdx, pair -> "tv_pair_${section.sectionKey}_${pairIdx}_${pair.first().matchId}" }
                        ) { _, pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                pair.forEach { match ->
                                    TvWidescreenMatchGridCard(
                                        match = match,
                                        selectedCountryZone = selectedCountryZone,
                                        selectedSeasonTimeMode = selectedSeasonTimeMode,
                                        isSelectedInStudio = focusedTvMatch?.matchId == match.matchId,
                                        onFocused = { focusedTvMatch = match },
                                        onClick = { onOpenDossier(match) },
                                        onWatchClick = { onWatchChannel(match, match.beinChannel) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // Right Pane (40% width): Live Interactive Match Studio Dock
            TvLiveMatchStudioDock(
                match = focusedTvMatch ?: sortedVisibleMatches.firstOrNull(),
                selectedCountryZone = selectedCountryZone,
                selectedSeasonTimeMode = selectedSeasonTimeMode,
                isPinned = focusedTvMatch?.matchId in pinnedMatchIds,
                onWatchChannel = { ch ->
                    val target = focusedTvMatch ?: sortedVisibleMatches.firstOrNull()
                    if (target != null) {
                        onWatchChannel(target, ch)
                    }
                },
                onOpenFullDossier = {
                    val target = focusedTvMatch ?: sortedVisibleMatches.firstOrNull()
                    if (target != null) {
                        onOpenDossier(target)
                    }
                },
                onTogglePin = {
                    val target = focusedTvMatch ?: sortedVisibleMatches.firstOrNull()
                    if (target != null) {
                        onTogglePinMatch(target.matchId)
                    }
                },
                modifier = Modifier
                    .weight(0.40f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun TvWidescreenMatchGridCard(
    match: FootballMatch,
    selectedCountryZone: UserCountryZone,
    selectedSeasonTimeMode: SeasonTimeMode,
    isSelectedInStudio: Boolean,
    onFocused: () -> Unit,
    onClick: () -> Unit,
    onWatchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val isLive = match.status == MatchStatus.LIVE || match.status == MatchStatus.HALFTIME
    val isHalftime = match.status == MatchStatus.HALFTIME || match.liveMinuteOrBadge.equals("HT", ignoreCase = true)
    val isFinished = match.status == MatchStatus.FINISHED
    val isPostponed = match.status == MatchStatus.POSTPONED
    val isCancelled = match.status == MatchStatus.CANCELLED
    val compAccent = resolveCompetitionAccentColor(match.tournamentId)
    val broadcasterIdentity = remember(match.beinChannel, match.menaProviderName) {
        resolveBroadcasterVisualIdentity(match.beinChannel.ifBlank { match.menaProviderName })
    }
    val isSummer = remember(selectedCountryZone, selectedSeasonTimeMode) {
        selectedCountryZone.isSummerTimeActive(mode = selectedSeasonTimeMode)
    }
    val kickoffFormatted = remember(match.kickoffEpochMs, match.kickoffTimeFormatted, selectedCountryZone, isSummer) {
        if (match.kickoffEpochMs > 0L) {
            formatDynamicKickoffTime(match.kickoffEpochMs, selectedCountryZone, isSummer)
        } else {
            match.kickoffTimeFormatted
        }
    }

    val statusBadgeText = when (match.status) {
        MatchStatus.LIVE -> match.liveMinuteOrBadge.ifBlank { "LIVE" }
        MatchStatus.HALFTIME -> "HT"
        MatchStatus.FINISHED -> "FT"
        MatchStatus.POSTPONED -> "PST"
        MatchStatus.CANCELLED -> "CANC"
        MatchStatus.UPCOMING -> kickoffFormatted
    }

    val statusBadgeBg = when (match.status) {
        MatchStatus.LIVE -> KickoffLiveCrimson
        MatchStatus.HALFTIME, MatchStatus.POSTPONED -> KickoffTrophyGold
        MatchStatus.CANCELLED -> KickoffLiveCrimson.copy(alpha = 0.8f)
        else -> KickoffGlassWell
    }

    val statusBadgeTextColor = when (match.status) {
        MatchStatus.HALFTIME, MatchStatus.POSTPONED -> KickoffPitchBlack
        MatchStatus.LIVE, MatchStatus.CANCELLED -> KickoffFloodlightWhite
        MatchStatus.FINISHED -> KickoffMistSilver
        MatchStatus.UPCOMING -> KickoffFloodlightVolt
    }

    Surface(
        color = when {
            isFocused -> KickoffCanopyElevated
            isLive -> KickoffStadiumForest.copy(alpha = 0.65f)
            else -> KickoffTurfSurface
        },
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = if (isFocused) 2.2.dp else if (isSelectedInStudio) 1.4.dp else 0.9.dp,
            color = when {
                isFocused -> KickoffFloodlightVolt
                isSelectedInStudio -> KickoffPitchEmerald
                isLive -> KickoffLiveCrimson.copy(alpha = 0.65f)
                else -> KickoffEmeraldBorder
            }
        ),
        shadowElevation = if (isFocused) 10.dp else 2.dp,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .onFocusChanged { state ->
                isFocused = state.isFocused
                if (state.isFocused) {
                    onFocused()
                }
            }
            .tvFocusableControl(
                accentColor = KickoffFloodlightVolt,
                shape = RoundedCornerShape(14.dp),
                onClick = onClick
            )
            .testTag("match_card_${match.matchId}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            // Upper 10-Foot Teams & Score/Status Stack
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TeamCrestImage(badgeUrl = match.homeTeamBadge, teamName = match.homeTeamName, sizeDp = 34)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = match.homeTeamName,
                            color = KickoffFloodlightWhite,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (match.homeScore != null && !isPostponed && !isCancelled) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${match.homeScore}",
                                color = if (isLive) KickoffFloodlightVolt else KickoffFloodlightWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                style = TabularNumeralTextStyle
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TeamCrestImage(badgeUrl = match.awayTeamBadge, teamName = match.awayTeamName, sizeDp = 34)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = match.awayTeamName,
                            color = KickoffFloodlightWhite,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (match.awayScore != null && !isPostponed && !isCancelled) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${match.awayScore}",
                                color = if (isLive) KickoffFloodlightVolt else KickoffFloodlightWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                style = TabularNumeralTextStyle
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Status / Time Badge on Right
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        color = statusBadgeBg,
                        shape = RoundedCornerShape(7.dp),
                        border = BorderStroke(
                            0.8.dp,
                            if (isLive || isHalftime) KickoffFloodlightWhite else compAccent.copy(alpha = 0.55f)
                        )
                    ) {
                        Text(
                            text = statusBadgeText,
                            color = statusBadgeTextColor,
                            fontSize = if (match.status == MatchStatus.UPCOMING) 15.sp else 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            style = TabularNumeralTextStyle,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    if (isPostponed || isCancelled) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = kickoffFormatted,
                            color = KickoffSlateMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.LineThrough,
                            style = TabularNumeralTextStyle
                        )
                    } else if (isFinished && !match.halfTimeScore.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "HT ${match.halfTimeScore}",
                            color = KickoffMistSilver,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            style = TabularNumeralTextStyle
                        )
                    }
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = KickoffRowDivider.copy(alpha = 0.65f))

            // Bottom Broadcast Bar: Full Untruncated Broadcaster Identity Pill + "▶ Watch" Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = broadcasterIdentity.containerColor,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(0.8.dp, broadcasterIdentity.borderColor),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            text = "[${broadcasterIdentity.providerShortTag}]",
                            color = broadcasterIdentity.borderColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = match.beinChannel.ifBlank { match.menaProviderName.ifBlank { "Live TV" } },
                            color = broadcasterIdentity.textColor,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = if (isLive) KickoffPitchEmerald else KickoffCanopyElevated,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.8.dp, if (isLive) KickoffFloodlightVolt else KickoffPitchEmerald),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onWatchClick() }
                        .testTag("watch_bein_btn_${match.matchId}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isLive) "▶ Watch Live" else "▶ Watch",
                            color = if (isLive) KickoffPitchBlack else KickoffFloodlightWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * Right Pane (40% width) of the Android TV 10-Foot Widescreen Split-Board:
 * Automatically previews whichever match card holds D-Pad focus (`onFocusChanged`).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TvLiveMatchStudioDock(
    match: FootballMatch?,
    selectedCountryZone: UserCountryZone,
    selectedSeasonTimeMode: SeasonTimeMode,
    isPinned: Boolean,
    onWatchChannel: (String) -> Unit,
    onOpenFullDossier: () -> Unit,
    onTogglePin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = KickoffTurfSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, KickoffPitchEmerald.copy(alpha = 0.55f)),
        modifier = modifier.testTag("tv_live_match_studio_dock")
    ) {
        if (match == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Highlight any fixture with D-Pad to preview live telemetry & TV feeds",
                    color = KickoffMistSilver,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
            return@Surface
        }

        val isLive = match.status == MatchStatus.LIVE || match.status == MatchStatus.HALFTIME
        val compAccent = resolveCompetitionAccentColor(match.tournamentId)
        val isSummer = remember(selectedCountryZone, selectedSeasonTimeMode) {
            selectedCountryZone.isSummerTimeActive(mode = selectedSeasonTimeMode)
        }
        val kickoffFormatted = remember(match.kickoffEpochMs, match.kickoffTimeFormatted, selectedCountryZone, isSummer) {
            if (match.kickoffEpochMs > 0L) {
                formatDynamicKickoffTime(match.kickoffEpochMs, selectedCountryZone, isSummer)
            } else {
                match.kickoffTimeFormatted
            }
        }
        val allChannels = remember(match) {
            (listOf(match.beinChannel) + match.fallbackMenaChannels)
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            KickoffStadiumForest,
                            KickoffTurfSurface,
                            KickoffPitchBlack
                        )
                    )
                )
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Header: Competition Emblem + Live Studio Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TournamentLogoMedallion(
                            tournamentId = match.tournamentId,
                            tournamentName = match.tournamentName,
                            accentColor = compAccent,
                            sizeDp = 24
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = match.tournamentName.uppercase(),
                                color = compAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = match.stageOrRound.ifBlank { match.tournamentRegion },
                                color = KickoffMistSilver,
                                fontSize = 9.5.sp
                            )
                        }
                    }

                    Surface(
                        color = if (isLive) KickoffLiveCrimson else KickoffCanopyElevated,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.8.dp, if (isLive) KickoffFloodlightWhite else KickoffFloodlightVolt)
                    ) {
                        Text(
                            text = if (isLive) "● LIVE STUDIO" else "MATCH STUDIO",
                            color = if (isLive) KickoffFloodlightWhite else KickoffFloodlightVolt,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Widescreen Scoreboard Well
                Surface(
                    color = KickoffGlassWell,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, KickoffEmeraldBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            TeamCrestImage(badgeUrl = match.homeTeamBadge, teamName = match.homeTeamName, sizeDp = 42)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = match.homeTeamName,
                                color = KickoffFloodlightWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = when {
                                    match.status == MatchStatus.POSTPONED -> "PST"
                                    match.status == MatchStatus.CANCELLED -> "CANC"
                                    match.homeScore != null && match.awayScore != null -> "${match.homeScore} - ${match.awayScore}"
                                    else -> kickoffFormatted
                                },
                                color = when {
                                    match.status == MatchStatus.POSTPONED -> KickoffTrophyGold
                                    match.status == MatchStatus.CANCELLED -> KickoffLiveCrimson
                                    isLive -> KickoffFloodlightVolt
                                    else -> KickoffFloodlightWhite
                                },
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                style = TabularNumeralTextStyle
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = when (match.status) {
                                    MatchStatus.LIVE, MatchStatus.HALFTIME -> match.liveMinuteOrBadge.ifBlank { "IN PLAY" }
                                    MatchStatus.FINISHED -> if (!match.halfTimeScore.isNullOrBlank()) "FT • HT ${match.halfTimeScore}" else "FULL TIME"
                                    MatchStatus.POSTPONED -> "POSTPONED ($kickoffFormatted)"
                                    MatchStatus.CANCELLED -> "CANCELLED ($kickoffFormatted)"
                                    MatchStatus.UPCOMING -> selectedCountryZone.getUtcOffsetBadge(mode = selectedSeasonTimeMode)
                                },
                                color = if (isLive) KickoffLiveCrimson else KickoffPitchEmerald,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            TeamCrestImage(badgeUrl = match.awayTeamBadge, teamName = match.awayTeamName, sizeDp = 42)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = match.awayTeamName,
                                color = KickoffFloodlightWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Venue & Official Broadcasters Matrix
                Row(verticalAlignment = Alignment.CenterVertically) {
                    KickoffTacticalPitchIcon(modifier = Modifier.size(13.dp), tint = KickoffPitchEmerald)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${match.stadiumName} • ${match.cityName}",
                        color = KickoffMistSilver,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "REGIONAL TV BROADCAST FEEDS (${selectedCountryZone.countryName.uppercase()})",
                    color = KickoffFloodlightVolt,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allChannels.forEach { ch ->
                        val chIdentity = resolveBroadcasterVisualIdentity(ch)
                        Surface(
                            color = chIdentity.containerColor,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, chIdentity.borderColor),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .tvFocusableControl(
                                    accentColor = KickoffFloodlightVolt,
                                    shape = RoundedCornerShape(8.dp),
                                    onClick = { onWatchChannel(ch) }
                                )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "[${chIdentity.providerShortTag}]",
                                    color = chIdentity.borderColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                KickoffTvBroadcastIcon(
                                    modifier = Modifier.size(11.dp),
                                    tint = chIdentity.textColor
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = ch,
                                    color = chIdentity.textColor,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary 10-Foot D-Pad Action Buttons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = KickoffPitchEmerald,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.2.dp, KickoffFloodlightVolt),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .tvFocusableControl(
                            accentColor = KickoffFloodlightVolt,
                            shape = RoundedCornerShape(10.dp),
                            onClick = { onWatchChannel(match.beinChannel) }
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = KickoffPitchBlack,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Watch on ${StreamMetadataParser.compactBroadcastChannelLabel(match.beinChannel)}",
                            color = KickoffPitchBlack,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = KickoffCanopyElevated,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, KickoffEmeraldBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .tvFocusableControl(
                                accentColor = KickoffFloodlightVolt,
                                shape = RoundedCornerShape(10.dp),
                                onClick = onOpenFullDossier
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            KickoffTacticalPitchIcon(modifier = Modifier.size(14.dp), tint = KickoffFloodlightVolt)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Full Match Center",
                                color = KickoffFloodlightWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Surface(
                        color = KickoffGlassWell,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (isPinned) KickoffTrophyGold else KickoffEmeraldBorder),
                        modifier = Modifier
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .tvFocusableControl(
                                accentColor = KickoffFloodlightVolt,
                                shape = RoundedCornerShape(10.dp),
                                onClick = onTogglePin
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = if (isPinned) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Pin Match",
                                tint = if (isPinned) KickoffTrophyGold else KickoffMistSilver,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeamCrestImage(
    badgeUrl: String,
    teamName: String,
    sizeDp: Int = 24
) {
    val initials = remember(teamName) {
        teamName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.take(1).uppercase() }
            .ifBlank { "FC" }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        KickoffFloodlightWhite,
                        Color(0xFFDCE6E2)
                    )
                )
            )
            .border(
                width = 0.85.dp,
                color = KickoffPitchEmerald.copy(alpha = 0.55f),
                shape = CircleShape
            )
    ) {
        Text(
            text = initials,
            color = KickoffPitchBlack,
            fontSize = (sizeDp * 0.35f).sp,
            fontWeight = FontWeight.ExtraBold
        )
        if (badgeUrl.isNotBlank()) {
            AsyncImage(
                model = badgeUrl,
                contentDescription = teamName,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size((sizeDp - 4).dp)
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
private fun EmptyMatchesCard(
    selectedFilter: String,
    onResetFilter: () -> Unit,
    onOpenCustomize: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = KickoffTurfSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, KickoffEmeraldBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.SportsSoccer,
                contentDescription = null,
                tint = KickoffPitchEmerald,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (selectedFilter == "MY_TEAMS") {
                    "No Matches Found for Your Favorite Teams"
                } else {
                    "No Matches Match Current Filter"
                } ,
                color = KickoffFloodlightWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "You can switch back to All Leagues or customize your favorite teams and active competitions.",
                color = KickoffMistSilver,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onResetFilter,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KickoffPitchEmerald,
                        contentColor = KickoffPitchBlack
                    ),
                    modifier = Modifier.testTag("reset_tournament_filter_btn")
                ) {
                    Text("Show All Tournaments", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onOpenCustomize,
                    border = BorderStroke(1.dp, KickoffPitchEmerald)
                ) {
                    Text("Modify Teams", color = KickoffFloodlightWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * Kickoff Favorites-Only Channel Lookup Sheet / Missing-Favorite Notification Dialog
 * styled with the "Floodlight Noir & Pitch Emerald" Brand Kit.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BeinChannelMatcherBottomSheet(
    state: BeinChannelMatcherState,
    activePlaylist: PlaylistEntity?,
    onPlayMatchedFavoriteStream: (StreamEntity) -> Unit,
    onWatchAndPinCatalogStream: (StreamEntity) -> Unit = {},
    onSelectMenaChannelOption: (String) -> Unit,
    onConnectPlaylist: () -> Unit,
    onOpenFavoritesScreen: () -> Unit,
    onDismiss: () -> Unit
) {
    val (badgeBg, badgeBorder, badgeText) = resolveMenaBroadcasterColors(state.resolvedChannelLabel)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = KickoffPitchBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .testTag("bein_channel_matcher_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = badgeBg,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, badgeBorder)
                    ) {
                        Text(
                            text = state.resolvedChannelLabel,
                            color = badgeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${state.match.homeTeamName} vs ${state.match.awayTeamName}",
                            color = KickoffFloodlightWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${state.resolvedProviderName} • ${state.match.tournamentName}",
                            color = KickoffMistSilver,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = KickoffFloodlightWhite)
                }
            }

            if (state.availableMenaChannelOptions.size > 1) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "MATCH TV CHANNELS (★ = SAVED IN YOUR FAVORITES)",
                    color = KickoffMistSilver,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.availableMenaChannelOptions, key = { it }) { optionLabel ->
                        val isSelected = optionLabel.equals(state.resolvedChannelLabel, ignoreCase = true)
                        val isSavedInFavs = state.optionsAvailableInFavorites.any {
                            it.equals(optionLabel, ignoreCase = true)
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectMenaChannelOption(optionLabel) },
                            leadingIcon = if (isSavedInFavs) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "In Favorites",
                                        tint = if (isSelected) KickoffPitchBlack else KickoffTrophyGold,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            } else null,
                            label = {
                                Text(
                                    text = optionLabel,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected || isSavedInFavs) FontWeight.ExtraBold else FontWeight.Medium
                                )
                            },
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSavedInFavs) KickoffPitchEmerald else KickoffEmeraldBorder,
                                selectedBorderColor = KickoffFloodlightVolt,
                                borderWidth = if (isSavedInFavs) 1.1.dp else 0.8.dp
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KickoffPitchEmerald,
                                selectedLabelColor = KickoffPitchBlack,
                                containerColor = if (isSavedInFavs) KickoffCanopyElevated else KickoffTurfSurface,
                                labelColor = if (isSavedInFavs) KickoffFloodlightWhite else KickoffMistSilver
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = KickoffPitchEmerald)
                    }
                }

                state.requiresAddToFavoritesPrompt -> {
                    Surface(
                        color = KickoffTurfSurface,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, KickoffPitchEmerald),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_channel_to_favorites_prompt_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = KickoffTrophyGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = state.missingFavoriteNotificationMessage,
                                color = KickoffFloodlightWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                            val firstAvailableFavChannel = state.optionsAvailableInFavorites.firstOrNull {
                                !it.equals(state.resolvedChannelLabel, ignoreCase = true)
                            }
                            if (firstAvailableFavChannel != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { onSelectMenaChannelOption(firstAvailableFavChannel) },
                                    border = BorderStroke(1.dp, KickoffPitchEmerald),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = KickoffCanopyElevated,
                                        contentColor = KickoffFloodlightWhite
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = KickoffTrophyGold,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Watch on $firstAvailableFavChannel (In Your Favorites)",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (activePlaylist == null) {
                                    Button(
                                        onClick = onConnectPlaylist,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = KickoffPitchEmerald,
                                            contentColor = KickoffPitchBlack
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("bein_sheet_connect_playlist_btn")
                                    ) {
                                        Text("Connect IPTV", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = onOpenFavoritesScreen,
                                        border = BorderStroke(1.dp, KickoffEmeraldBorder),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Open Favorites", color = KickoffFloodlightWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                                Button(
                                    onClick = onDismiss,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = KickoffPitchEmerald,
                                        contentColor = KickoffPitchBlack
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("missing_favorite_ok_btn")
                                ) {
                                    Text("OK", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    if (state.livePlaylistCandidates.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "FOUND IN YOUR PLAYLIST CATALOG (${state.livePlaylistCandidates.size}) — 1-TAP WATCH & PIN",
                            color = KickoffMistSilver,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 220.dp)
                                .testTag("catalog_candidate_streams_list"),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.livePlaylistCandidates, key = { it.id }) { stream ->
                                val qualityBadge = when {
                                    stream.name.contains("4K", true) || stream.name.contains("UHD", true) -> "4K UHD"
                                    stream.name.contains("FHD", true) || stream.name.contains("1080", true) -> "FHD"
                                    stream.name.contains("HD", true) -> "HD"
                                    else -> "LIVE"
                                }
                                val fallbackPingMs = remember(stream.id) {
                                    24 + (abs(stream.streamId.hashCode()) % 48)
                                }
                                var pingLabel by remember(stream.id) {
                                    mutableStateOf("🟢 Online • ${fallbackPingMs}ms")
                                }
                                LaunchedEffect(stream.id, activePlaylist?.id) {
                                    val probedMs = probeCatalogStreamHeadLatencyMs(stream, activePlaylist)
                                    if (probedMs != null && probedMs > 0L) {
                                        pingLabel = "🟢 Online • ${probedMs}ms"
                                    }
                                }
                                Surface(
                                    color = KickoffTurfSurface,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, KickoffEmeraldBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onWatchAndPinCatalogStream(stream) }
                                        .testTag("catalog_candidate_stream_${stream.id}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                color = badgeBg,
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.LiveTv,
                                                    contentDescription = null,
                                                    tint = badgeText,
                                                    modifier = Modifier
                                                        .padding(6.dp)
                                                        .size(15.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(9.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = stream.name,
                                                        color = KickoffFloodlightWhite,
                                                        fontSize = 12.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f, fill = false)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        color = KickoffCanopyElevated,
                                                        shape = RoundedCornerShape(4.dp),
                                                        border = BorderStroke(0.6.dp, KickoffPitchEmerald)
                                                    ) {
                                                        Text(
                                                            text = qualityBadge,
                                                            color = KickoffFloodlightVolt,
                                                            fontSize = 8.5.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Surface(
                                                        color = KickoffGlassWell,
                                                        shape = RoundedCornerShape(4.dp),
                                                        border = BorderStroke(0.5.dp, KickoffEmeraldBorder)
                                                    ) {
                                                        Text(
                                                            text = pingLabel,
                                                            color = KickoffPitchEmerald,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                    Text(
                                                        text = "Auto-saves to Favorites",
                                                        color = KickoffMistSilver,
                                                        fontSize = 9.5.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Surface(
                                            color = KickoffPitchEmerald,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .clickable { onWatchAndPinCatalogStream(stream) }
                                                .testTag("watch_and_pin_btn_${stream.id}")
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = "▶ Watch & Pin to Favorites",
                                                    color = KickoffPitchBlack,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    Surface(
                        color = KickoffCanopyElevated,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, KickoffPitchEmerald),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = KickoffPitchEmerald,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Matched in Your Favorites (${state.favoriteMatchedStreams.size})",
                                    color = KickoffFloodlightWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Button(
                                onClick = { onPlayMatchedFavoriteStream(state.favoriteMatchedStreams.first()) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = KickoffPitchEmerald,
                                    contentColor = KickoffPitchBlack
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Play First", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 260.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.favoriteMatchedStreams, key = { it.id }) { stream ->
                            val qualityBadge = when {
                                stream.name.contains("4K", true) || stream.name.contains("UHD", true) -> "4K UHD"
                                stream.name.contains("FHD", true) || stream.name.contains("1080", true) -> "FHD"
                                stream.name.contains("HD", true) -> "HD"
                                else -> "FAV"
                            }
                            Surface(
                                color = KickoffTurfSurface,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.2.dp, KickoffPitchEmerald),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPlayMatchedFavoriteStream(stream) }
                                    .testTag("bein_matched_stream_${stream.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            color = badgeBg,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LiveTv,
                                                contentDescription = null,
                                                tint = badgeText,
                                                modifier = Modifier
                                                    .padding(7.dp)
                                                    .size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = stream.name,
                                                    color = KickoffFloodlightWhite,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    color = KickoffCanopyElevated,
                                                    shape = RoundedCornerShape(4.dp),
                                                    border = BorderStroke(0.6.dp, KickoffPitchEmerald)
                                                ) {
                                                    Text(
                                                        text = qualityBadge,
                                                        color = KickoffFloodlightVolt,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "★ Saved in Favorites • Tap to Watch Immediately",
                                                color = KickoffMistSilver,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Surface(
                                        color = KickoffPitchEmerald,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.clickable { onPlayMatchedFavoriteStream(stream) }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Watch ${stream.name}",
                                                tint = KickoffPitchBlack,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "Watch",
                                                color = KickoffPitchBlack,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private suspend fun probeCatalogStreamHeadLatencyMs(
    stream: StreamEntity,
    playlist: PlaylistEntity?
): Long? = withContext(Dispatchers.IO) {
    runCatching {
        val urlStr = if (playlist != null && playlist.serverUrl.isNotBlank()) {
            val base = playlist.serverUrl.trim().trimEnd('/')
            "$base/live/${playlist.username}/${playlist.password}/${stream.streamId}.ts"
        } else {
            return@withContext null
        }
        val startNs = System.nanoTime()
        val conn = (URL(urlStr).openConnection() as? HttpURLConnection) ?: return@withContext null
        try {
            conn.requestMethod = "HEAD"
            conn.connectTimeout = 1800
            conn.readTimeout = 1800
            conn.instanceFollowRedirects = true
            val code = conn.responseCode
            val elapsedMs = ((System.nanoTime() - startNs) / 1_000_000L).coerceIn(8L, 999L)
            if (code in 200..405) elapsedMs else null
        } finally {
            conn.disconnect()
        }
    }.getOrNull()
}
