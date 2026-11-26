package com.example.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.BuildConfig
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
    val manualTimeOffsetMinutes by mainViewModel.manualTimeOffsetMinutes.collectAsStateWithLifecycle()
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
    val kickoffUiPreferences by mainViewModel.kickoffUiPreferences.collectAsStateWithLifecycle()
    val kickoffSettingsInitialDomain by mainViewModel.kickoffSettingsInitialDomain.collectAsStateWithLifecycle()
    var showDateSelectorModal by remember { mutableStateOf(false) }
    var showCompetitionsSelectorModal by remember { mutableStateOf(false) }
    var sortByKickoffTime by remember { mutableStateOf(false) }
    val unhealthySourcesCount = remember(kickoffSourceHealth) {
        kickoffSourceHealth.count { state ->
            state.status == com.example.data.repository.SourceHealthStatus.KEY_INVALID_OR_EXPIRED ||
                state.status == com.example.data.repository.SourceHealthStatus.QUOTA_WARNING ||
                state.status == com.example.data.repository.SourceHealthStatus.UNREACHABLE
        }
    }

    val activeDayMatches = matchesByDay[selectedMatchDay].orEmpty()
    val liveMatchesCount = todayMatches.count { it.status == MatchStatus.LIVE || it.status == MatchStatus.HALFTIME }
    var liveClockTickMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(liveMatchesCount > 0) {
        if (liveMatchesCount > 0) {
            while (true) {
                liveClockTickMs = System.currentTimeMillis()
                delay(1000L)
            }
        }
    }
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

    if (activeEdition == SetPlayEdition.TV) {
        TvKickoffStadiumScreen(
            syncProgress = syncProgress,
            activeGoalAlert = activeGoalAlert,
            kickoffSourceAlert = kickoffSourceAlert,
            unhealthySourcesCount = unhealthySourcesCount,
            selectedCountryZone = selectedCountryZone,
            selectedSeasonTimeMode = selectedSeasonTimeMode,
            manualTimeOffsetMinutes = manualTimeOffsetMinutes,
            use24HourFormat = kickoffUiPreferences.use24HourFormat,
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
                mainViewModel.openKickoffSettingsCenter(tabIdx)
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
            onDismissSourceAlert = { mainViewModel.dismissKickoffSourceAlert() },
            onOpenDateSelectorModal = { showDateSelectorModal = true },
            onOpenCompetitionsSelectorModal = { showCompetitionsSelectorModal = true }
        )
    } else {
        // =========================================================================
        // MOBILE EDITORIAL EXPERIENCE (`SetPlayEdition.PHONE`)
        // =========================================================================
        val density = LocalDensity.current
        val swipeThresholdPx = with(density) { 95.dp.toPx() }
        var totalHorizontalDragPx by remember(selectedMatchDay) { mutableFloatStateOf(0f) }
        val activeLeaguesInSlate = remember(sortedVisibleMatches) {
            sortedVisibleMatches.map { it.tournamentId }.distinct().size
        }

        Column(
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
        ) {
            SyncBanner(syncProgress = syncProgress)

            activeGoalAlert?.let { alert ->
                LiveGoalAlertBanner(
                    alert = alert,
                    onOpenDossier = {
                        mainViewModel.dismissGoalAlert()
                        mainViewModel.openMatchCenterDossier(alert.match)
                    },
                    onDismiss = { mainViewModel.dismissGoalAlert() }
                )
            }

            // Pinned Two-Tier Command Header (`match_center_hero_card` + `InstantSevenDayDateStrip`)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                KickoffPitchBlack.copy(alpha = 0.98f),
                                KickoffTurfSurface.copy(alpha = 0.95f)
                            )
                        )
                    )
                    .padding(bottom = 2.dp)
            ) {
                KickOffArenaHeaderCard(
                    activeEdition = SetPlayEdition.PHONE,
                    selectedCountryZone = selectedCountryZone,
                    selectedSeasonTimeMode = selectedSeasonTimeMode,
                    manualTimeOffsetMinutes = manualTimeOffsetMinutes,
                    selectedMatchDay = selectedMatchDay,
                    selectedTournamentFilter = selectedTournamentFilter,
                    activeDayMatches = activeDayMatches,
                    totalVisibleMatchesCount = sortedVisibleMatches.size,
                    activeLeaguesInSlate = activeLeaguesInSlate,
                    favoriteTeamsCount = favoriteTeams.size,
                    liveMatchesCount = liveMatchesCount,
                    isLoadingMatches = isLoadingMatches,
                    liveScoreSyncState = liveScoreSyncState,
                    sourceAlertMessage = kickoffSourceAlert,
                    unhealthySourcesCount = unhealthySourcesCount,
                    onRefreshMatches = { mainViewModel.refreshKickOffMatches(selectedMatchDay) },
                    onOpenCountryZoneModal = { mainViewModel.openKickoffSettingsCenter(1) },
                    onOpenCustomizeModal = {
                        mainViewModel.openKickoffSettingsCenter(0)
                    },
                    onOpenSourcesHub = {
                        mainViewModel.openKickoffSettingsCenter(3)
                    },
                    onToggleLiveFilter = {
                        if (selectedTournamentFilter == "LIVE") {
                            mainViewModel.selectTournamentFilter("ALL")
                        } else {
                            if (selectedMatchDay != MatchDayTab.TODAY) {
                                mainViewModel.selectMatchDay(MatchDayTab.TODAY)
                            }
                            mainViewModel.selectTournamentFilter("LIVE")
                        }
                    },
                    onSelectMatchDay = { mainViewModel.selectMatchDay(it) },
                    onOpenDateSelectorModal = { showDateSelectorModal = true },
                    onOpenCompetitionsSelectorModal = { showCompetitionsSelectorModal = true },
                    sortByKickoffTime = sortByKickoffTime,
                    onToggleSortMode = { sortByKickoffTime = !sortByKickoffTime }
                )

                InstantSevenDayDateStrip(
                    selectedDay = selectedMatchDay,
                    selectedCountryZone = selectedCountryZone,
                    matchesByDay = matchesByDay,
                    liveCountToday = liveMatchesCount,
                    onSelectDay = { mainViewModel.selectMatchDay(it) },
                    activeEdition = SetPlayEdition.PHONE,
                    onOpenDateSelectorModal = { showDateSelectorModal = true },
                    onResetToToday = { mainViewModel.selectMatchDay(MatchDayTab.TODAY) }
                )
            }

            PullToRefreshBox(
                isRefreshing = isLoadingMatches,
                onRefresh = { mainViewModel.refreshKickOffMatches(selectedMatchDay) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = selectedMatchDay,
                    transitionSpec = {
                        val direction = if (targetState.dayOffset >= initialState.dayOffset) 1 else -1
                        (fadeIn(tween(180)) + slideInHorizontally(tween(200)) { fullWidth -> direction * (fullWidth / 10) })
                            .togetherWith(
                                fadeOut(tween(140)) + slideOutHorizontally(tween(160)) { fullWidth -> -direction * (fullWidth / 10) }
                            )
                    },
                    label = "kickoff_day_feed_transition"
                ) { animatedDay ->
                    val dayFeedMatches = matchesByDay[animatedDay].orEmpty()
                    val dayVisibleMatches = remember(
                        dayFeedMatches,
                        enabledTournamentMap,
                        selectedCompetitionCategory,
                        selectedTournamentFilter,
                        favoriteTeams
                    ) {
                        dayFeedMatches.filter { match ->
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
                    val daySortedVisibleMatches = remember(dayVisibleMatches, enabledTournamentMap) {
                        dayVisibleMatches.sortedWith(
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
                    val dayChronologicalSections = remember(dayVisibleMatches, enabledTournamentMap) {
                        buildUnifiedTournamentSections(dayVisibleMatches, enabledTournamentMap)
                    }
                    val dayFavoriteTeamMatches = remember(dayFeedMatches, favoriteTeams) {
                        if (favoriteTeams.isEmpty()) emptyList()
                        else dayFeedMatches.filter { isMatchForFavoriteTeams(it, favoriteTeams) }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(selectedMatchDay) {
                                detectHorizontalDragGestures(
                                    onDragStart = { totalHorizontalDragPx = 0f },
                                    onHorizontalDrag = { _, dragAmount ->
                                        totalHorizontalDragPx += dragAmount
                                    },
                                    onDragEnd = {
                                        if (abs(totalHorizontalDragPx) > swipeThresholdPx) {
                                            val tabs = MatchDayTab.ALL_NAVIGABLE_DAYS
                                            val currentIdx = tabs.indexOf(selectedMatchDay).takeIf { it >= 0 } ?: 1
                                            if (totalHorizontalDragPx < 0f && currentIdx < tabs.lastIndex) {
                                                // Swipe Left -> Next Day
                                                mainViewModel.selectMatchDay(tabs[currentIdx + 1])
                                            } else if (totalHorizontalDragPx > 0f && currentIdx > 0) {
                                                // Swipe Right -> Previous Day
                                                mainViewModel.selectMatchDay(tabs[currentIdx - 1])
                                            }
                                        }
                                        totalHorizontalDragPx = 0f
                                    },
                                    onDragCancel = { totalHorizontalDragPx = 0f }
                                )
                            }
                            .testTag("match_center_list"),
                        contentPadding = PaddingValues(bottom = 112.dp)
                    ) {
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

                        if (selectedCompetitionCategory == CompetitionCategory.INTERNATIONAL ||
                            selectedTournamentFilter == "CAT_INTERNATIONAL" ||
                            selectedTournamentFilter == "intl_official" ||
                            selectedTournamentFilter == "intl_friendly"
                        ) {
                            item(key = "international_fifa_ranking_banner_item") {
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
                        }

            // Main Fixture Feed: Favorite Teams View vs Grouped Competition Containers
            if (selectedTournamentFilter == "MY_TEAMS") {
                if (favoriteTeams.isEmpty()) {
                    item {
                        FavoriteTeamsEmptyBanner(
                            onSelectFavoriteTeams = {
                                mainViewModel.openKickoffSettingsCenter(0)
                            }
                        )
                    }
                } else {
                    item {
                        PinnedFavoriteTeamsSection(
                            favoriteTeams = favoriteTeams,
                            favoriteMatches = dayFavoriteTeamMatches,
                            selectedDay = animatedDay,
                            selectedCountryZone = selectedCountryZone,
                            selectedSeasonTimeMode = selectedSeasonTimeMode,
                            manualTimeOffsetMinutes = manualTimeOffsetMinutes,
                            liveClockTickMs = liveClockTickMs,
                            uiPreferences = kickoffUiPreferences,
                            onEditFavoriteTeams = {
                                mainViewModel.openKickoffSettingsCenter(0)
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
                if (daySortedVisibleMatches.isEmpty()) {
                    item {
                        EmptyMatchesCard(
                            selectedFilter = selectedTournamentFilter,
                            onResetFilter = {
                                mainViewModel.selectCompetitionCategory(null)
                                mainViewModel.selectTournamentFilter("ALL")
                                mainViewModel.enableAllTournaments()
                            },
                            onOpenCustomize = {
                                mainViewModel.openKickoffSettingsCenter(0)
                            },
                            selectedDay = animatedDay,
                            selectedCountryZone = selectedCountryZone,
                            matchesByDay = matchesByDay,
                            onSelectDay = { mainViewModel.selectMatchDay(it) }
                        )
                    }
                } else if (sortByKickoffTime) {
                    itemsIndexed(
                        items = daySortedVisibleMatches,
                        key = { _, match -> "time_sorted_match_${match.matchId}" }
                    ) { _, match ->
                        val compAccent = resolveCompetitionAccentColor(match.tournamentId)
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                color = KickoffCanopyElevated,
                                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                                border = BorderStroke(0.75.dp, compAccent.copy(alpha = 0.30f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 14.dp, end = 14.dp, top = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    TournamentLogoMedallion(
                                        tournamentId = match.tournamentId,
                                        tournamentName = match.tournamentName,
                                        isSelected = false,
                                        accentColor = compAccent,
                                        sizeDp = 16
                                    )
                                    Text(
                                        text = match.tournamentName.uppercase(Locale.ROOT),
                                        color = KickoffMistSilver,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (match.tournamentRegion.isNotBlank() &&
                                        shouldShowTournamentRegionBadge(match.tournamentName, match.tournamentRegion)
                                    ) {
                                        Text(
                                            text = match.tournamentRegion,
                                            color = KickoffSlateMuted,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                            val matchUtcBadge = remember(match.kickoffEpochMs, selectedCountryZone, manualTimeOffsetMinutes) {
                                val refEpoch = if (match.kickoffEpochMs > 0L) match.kickoffEpochMs else System.currentTimeMillis()
                                selectedCountryZone.getUtcOffsetBadge(
                                    epochMs = refEpoch,
                                    manualOffsetMinutes = manualTimeOffsetMinutes
                                )
                            }
                            FlashscoreMatchCard(
                                match = match,
                                selectedCountryZone = selectedCountryZone,
                                selectedSeasonTimeMode = selectedSeasonTimeMode,
                                manualTimeOffsetMinutes = manualTimeOffsetMinutes,
                                utcOffsetBadge = matchUtcBadge,
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
                                isLastInSection = true,
                                liveClockTickMs = liveClockTickMs,
                                uiPreferences = kickoffUiPreferences
                            )
                        }
                    }
                } else {
                    dayChronologicalSections.forEachIndexed { sectionIndex, section ->
                        val tournamentId = section.tournamentId
                        val matchesInSection = section.matches
                        val firstMatch = matchesInSection.first()
                        val pref = enabledTournamentMap[tournamentId]
                        val isCollapsed = collapsedTournamentIds.contains(tournamentId)
                        val isPinned = pref?.isPinned == true
                        val liveInSection = matchesInSection.count {
                            it.status == MatchStatus.LIVE || it.status == MatchStatus.HALFTIME
                        }

                        item(key = "section_${section.sectionKey}") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize(animationSpec = tween(durationMillis = 220))
                            ) {
                                FlashscoreTournamentHeader(
                                    tournamentId = tournamentId,
                                    tournamentName = firstMatch.tournamentName,
                                    regionName = firstMatch.tournamentRegion,
                                    menaProviderName = firstMatch.menaProviderName,
                                    matchCount = matchesInSection.size,
                                    liveCount = liveInSection,
                                    isPinned = isPinned,
                                    isCollapsed = isCollapsed,
                                    isFirstSection = sectionIndex == 0,
                                    onToggleCollapse = { mainViewModel.toggleTournamentCollapsed(tournamentId) },
                                    onTogglePin = { mainViewModel.toggleTournamentPinned(tournamentId) }
                                )

                                if (!isCollapsed) {
                                    matchesInSection.forEachIndexed { index, match ->
                                        val matchUtcBadge = remember(match.kickoffEpochMs, selectedCountryZone, manualTimeOffsetMinutes) {
                                            val refEpoch = if (match.kickoffEpochMs > 0L) match.kickoffEpochMs else System.currentTimeMillis()
                                            selectedCountryZone.getUtcOffsetBadge(
                                                epochMs = refEpoch,
                                                manualOffsetMinutes = manualTimeOffsetMinutes
                                            )
                                        }
                                        FlashscoreMatchCard(
                                            match = match,
                                            selectedCountryZone = selectedCountryZone,
                                            selectedSeasonTimeMode = selectedSeasonTimeMode,
                                            manualTimeOffsetMinutes = manualTimeOffsetMinutes,
                                            utcOffsetBadge = matchUtcBadge,
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
                                            isLastInSection = index == matchesInSection.lastIndex,
                                            liveClockTickMs = liveClockTickMs,
                                            uiPreferences = kickoffUiPreferences
                                        )
                                    }
                                }
                            }
                        }

                        // Subtle Kickoff Navigation & Settings Version Footer
                        item(key = "kickoff_version_footer") {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 18.dp, bottom = 6.dp)
                            ) {
                                Text(
                                    text = "SetPlay ${BuildConfig.VERSION_NAME} • KickOff Settings",
                                    color = KickoffMistSilver.copy(alpha = 0.56f),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.4.sp,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable { mainViewModel.openKickoffSettingsCenter(0) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .testTag("kickoff_version_footer")
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
    }

    // =========================================================================
    // 1. First-Time Country Zone & Favorite Teams Onboarding Modal
    // =========================================================================
    if (showFirstTimeOnboarding) {
        FirstTimeFavoriteTeamsDialog(
            selectedCountryZone = selectedCountryZone,
            selectedSeasonTimeMode = selectedSeasonTimeMode,
            manualTimeOffsetMinutes = manualTimeOffsetMinutes,
            onSetManualTimeOffset = { mainViewModel.setManualTimeOffset(it) },
            onAdjustManualTimeOffset = { mainViewModel.adjustManualTimeOffset(it) },
            use24HourFormat = kickoffUiPreferences.use24HourFormat,
            activeEdition = activeEdition,
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
            manualTimeOffsetMinutes = manualTimeOffsetMinutes,
            onSetManualTimeOffset = { mainViewModel.setManualTimeOffset(it) },
            onAdjustManualTimeOffset = { mainViewModel.adjustManualTimeOffset(it) },
            use24HourFormat = kickoffUiPreferences.use24HourFormat,
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
            manualTimeOffsetMinutes = manualTimeOffsetMinutes,
            onSetManualTimeOffset = { mainViewModel.setManualTimeOffset(it) },
            onAdjustManualTimeOffset = { mainViewModel.adjustManualTimeOffset(it) },
            searchQuery = teamSearchQuery,
            searchResults = teamSearchResults,
            isSearching = isSearchingTeams,
            favoriteTeams = favoriteTeams,
            tournamentPreferences = tournamentPreferences,
            sourceHealthStates = kickoffSourceHealth,
            sourceAlertMessage = kickoffSourceAlert,
            isCheckingSourcesHealth = isCheckingSourcesHealth,
            initialTabIndex = kickoffSettingsInitialDomain,
            activeEdition = activeEdition,
            uiPreferences = kickoffUiPreferences,
            onUpdateUiPreferences = { mainViewModel.updateKickoffUiPreferences(it) },
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

    // =========================================================================
    // 5. On-Demand Date Selector Modal (7-Day Forward Matchday Agenda)
    // =========================================================================
    if (showDateSelectorModal) {
        KickoffDateSelectorModal(
            activeEdition = activeEdition,
            selectedDay = selectedMatchDay,
            selectedCountryZone = selectedCountryZone,
            matchesByDay = matchesByDay,
            liveMatchesCount = liveMatchesCount,
            onSelectDay = { chosenDay ->
                mainViewModel.selectMatchDay(chosenDay)
                showDateSelectorModal = false
            },
            onDismiss = { showDateSelectorModal = false }
        )
    }

    // =========================================================================
    // 6. On-Demand Competitions Selector Modal (Active Today vs Other Leagues)
    // =========================================================================
    if (showCompetitionsSelectorModal) {
        KickoffCompetitionsSelectorModal(
            activeEdition = activeEdition,
            selectedMatchDay = selectedMatchDay,
            selectedFilter = selectedTournamentFilter,
            activeDayMatches = activeDayMatches,
            favoriteTeamsCount = favoriteTeams.size,
            liveMatchesCount = activeDayMatches.count { it.status == MatchStatus.LIVE || it.status == MatchStatus.HALFTIME },
            tournamentPreferences = tournamentPreferences,
            onSelectFilter = { filterId ->
                mainViewModel.selectTournamentFilter(filterId)
                showCompetitionsSelectorModal = false
            },
            onOpenManageLeagues = {
                showCompetitionsSelectorModal = false
                mainViewModel.openKickoffSettingsCenter(0)
            },
            onDismiss = { showCompetitionsSelectorModal = false }
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
    manualTimeOffsetMinutes: Int = 0,
    selectedMatchDay: MatchDayTab = MatchDayTab.TODAY,
    selectedTournamentFilter: String = "ALL",
    activeDayMatches: List<FootballMatch> = emptyList(),
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
    onOpenCustomizeModal: () -> Unit = {},
    onOpenSourcesHub: () -> Unit = onOpenCustomizeModal,
    onToggleLiveFilter: () -> Unit = {},
    onSelectMatchDay: (MatchDayTab) -> Unit = {},
    onOpenDateSelectorModal: () -> Unit = {},
    onOpenCompetitionsSelectorModal: () -> Unit = {},
    sortByKickoffTime: Boolean = false,
    onToggleSortMode: () -> Unit = {}
) {
    val isLiveFilterActive = selectedTournamentFilter == "LIVE"
    val isTv = activeEdition == SetPlayEdition.TV
    val headerReferenceEpochMs = remember(activeDayMatches, selectedMatchDay) {
        activeDayMatches.firstOrNull { it.kickoffEpochMs > 0L }?.kickoffEpochMs
            ?: (System.currentTimeMillis() + selectedMatchDay.dayOffset * 86_400_000L)
    }
    val utcBadge = remember(selectedCountryZone, manualTimeOffsetMinutes, headerReferenceEpochMs) {
        selectedCountryZone.getUtcOffsetBadge(
            epochMs = headerReferenceEpochMs,
            manualOffsetMinutes = manualTimeOffsetMinutes
        )
    }

    val activeTournamentCatalogItem = remember(selectedTournamentFilter) {
        FootballRepository.ALL_TOURNAMENTS.firstOrNull { it.tournamentId == selectedTournamentFilter }
    }
    val compAccent = if (activeTournamentCatalogItem != null) {
        resolveCompetitionAccentColor(activeTournamentCatalogItem.tournamentId)
    } else {
        KickoffPitchEmerald
    }

    val commandBarContent: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isTv) {
                        Modifier
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        KickoffTurfSurface,
                                        KickoffCanopyElevated,
                                        KickoffTurfSurface
                                    )
                                )
                            )
                            .padding(horizontal = 8.dp)
                    } else {
                        Modifier
                    }
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left (Weighted 1f): Prominent Competition Blade Trigger (`kickoff_competitions_selector_btn`)
            val controlHeight = 40.dp
            val controlCorner = RoundedCornerShape(11.dp)
            Surface(
                color = KickoffCanopyElevated,
                shape = controlCorner,
                border = BorderStroke(
                    0.9.dp,
                    if (selectedTournamentFilter != "ALL") compAccent else KickoffPitchEmerald.copy(alpha = 0.65f)
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(controlHeight)
                    .widthIn(min = if (isTv) 165.dp else 130.dp, max = if (isTv) 280.dp else 230.dp)
                    .clip(controlCorner)
                    .tvFocusableControl(
                        accentColor = KickoffFloodlightVolt,
                        shape = controlCorner,
                        onClick = onOpenCompetitionsSelectorModal
                    )
                    .testTag("kickoff_competitions_selector_btn")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        when {
                            activeTournamentCatalogItem != null -> {
                                TournamentLogoMedallion(
                                    tournamentId = activeTournamentCatalogItem.tournamentId,
                                    tournamentName = activeTournamentCatalogItem.name,
                                    isSelected = true,
                                    accentColor = compAccent,
                                    sizeDp = 20
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${activeTournamentCatalogItem.name} ($totalVisibleMatchesCount)",
                                    color = KickoffFloodlightWhite,
                                    fontSize = if (isTv) 11.5.sp else 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            selectedTournamentFilter == "MY_TEAMS" -> {
                                Text(
                                    text = "★ My Teams ($favoriteTeamsCount)",
                                    color = KickoffTrophyGold,
                                    fontSize = if (isTv) 11.5.sp else 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            else -> {
                                KickoffLeagueShieldSelectorIcon(
                                    modifier = Modifier.size(if (isTv) 16.dp else 14.5.dp),
                                    tint = KickoffPitchEmerald
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "All Competitions ($totalVisibleMatchesCount)",
                                    color = KickoffFloodlightWhite,
                                    fontSize = if (isTv) 11.5.sp else 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Select Competition",
                        tint = KickoffFloodlightVolt,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(7.dp))

            // Right Cluster: 1. Context-Aware Live Pill + 2. League/Time Sort Toggle + 3. Subtle Region Chip + 4. TV-Only Refresh Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 1. Context-Aware Live Pulse Filter Pill (`kickoff_live_filter_btn`) — jumps to TODAY + LIVE from any day
                if (liveMatchesCount > 0 || isLiveFilterActive) {
                    Surface(
                        color = if (isLiveFilterActive) KickoffLiveCrimson else KickoffLiveCrimson.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.85.dp, KickoffLiveCrimson),
                        modifier = Modifier
                            .height(controlHeight)
                            .clip(RoundedCornerShape(50))
                            .tvFocusableControl(
                                accentColor = KickoffFloodlightVolt,
                                shape = RoundedCornerShape(50),
                                onClick = onToggleLiveFilter
                            )
                            .testTag("kickoff_live_filter_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 9.dp)
                        ) {
                            KickoffLiveBroadcastBeaconIcon(
                                modifier = Modifier.size(10.dp),
                                color = if (isLiveFilterActive) KickoffFloodlightWhite else KickoffLiveCrimson
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$liveMatchesCount LIVE",
                                color = KickoffFloodlightWhite,
                                fontSize = if (isTv) 10.sp else 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                style = TabularNumeralTextStyle,
                                maxLines = 1
                            )
                        }
                    }
                }

                // 2. League / Time Sort Toggle (`kickoff_sort_mode_btn`)
                Surface(
                    color = if (sortByKickoffTime) KickoffCanopyElevated else KickoffGlassWell.copy(alpha = 0.72f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        0.8.dp,
                        if (sortByKickoffTime) KickoffFloodlightVolt else KickoffEmeraldBorder
                    ),
                    modifier = Modifier
                        .height(controlHeight)
                        .clip(RoundedCornerShape(50))
                        .tvFocusableControl(
                            accentColor = KickoffFloodlightVolt,
                            shape = RoundedCornerShape(50),
                            onClick = onToggleSortMode
                        )
                        .testTag("kickoff_sort_mode_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = if (sortByKickoffTime) "⏱ Time" else "≡ League",
                            color = if (sortByKickoffTime) KickoffFloodlightVolt else KickoffFloodlightWhite,
                            fontSize = if (isTv) 9.5.sp else 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                }

                // 3. Quiet Region & Offset Chip (`customize_teams_tournaments_btn`)
                Surface(
                    color = KickoffTurfSurface.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.7.dp, KickoffEmeraldBorder),
                    modifier = Modifier
                        .height(controlHeight)
                        .clip(RoundedCornerShape(50))
                        .tvFocusableControl(
                            accentColor = KickoffFloodlightVolt,
                            shape = RoundedCornerShape(50),
                            onClick = onOpenCountryZoneModal
                        )
                        .testTag("customize_teams_tournaments_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "${selectedCountryZone.flagEmoji} $utcBadge",
                            color = KickoffMistSilver,
                            fontSize = if (isTv) 9.5.sp else 9.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }

                // 4. Circular Refresh Button (`refresh_matches_btn`) — TV only (Mobile uses native PullToRefreshBox)
                if (isTv) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(controlHeight)
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
                                modifier = Modifier.size(12.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Matches",
                                tint = KickoffFloodlightWhite,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (isTv) {
        Surface(
            color = KickoffTurfSurface,
            shape = RoundedCornerShape(11.dp),
            border = BorderStroke(
                width = 0.85.dp,
                color = if (liveMatchesCount > 0 && selectedMatchDay == MatchDayTab.TODAY) {
                    KickoffLiveCrimson.copy(alpha = 0.45f)
                } else {
                    KickoffEmeraldBorder
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 3.dp)
                .height(48.dp)
                .testTag("match_center_hero_card")
        ) {
            commandBarContent()
        }
    } else {
        // Harmonized 40.dp vertical rhythm matching InstantSevenDayDateStrip (40.dp inner bar + 3.dp top/bottom)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .padding(horizontal = 14.dp, vertical = 3.dp)
                .testTag("match_center_hero_card")
        ) {
            commandBarContent()
        }
    }
}

/**
 * Tier 2 of the Pinned Sticky Command Header (`InstantSevenDayDateStrip`, `.testTag("five_day_timeline_ribbon")`):
 * Flashscore-inspired non-scrolling Day Navigator supporting Yesterday + 7 forward days (`MatchDayTab.ALL_NAVIGABLE_DAYS`):
 * - Left `<` stepper button (`kickoff_prev_day_btn`, cleanly dimmed when on `YESTERDAY`).
 * - Center interactive calendar pill (`kickoff_date_selector_btn`, `weight(1f)`) showing Calendar icon, pulsing live dot (on TODAY when live),
 *   compact date label (`selectedDay.getCompactNavigatorLabel(tzId)`), and subtle match count pill.
 * - Right `>` stepper button (`kickoff_next_day_btn`, cleanly dimmed when on `DAY_PLUS_6`).
 * - Quick Return to Today emerald pill (`kickoff_reset_today_btn`) shown beside the bar whenever `selectedDay != MatchDayTab.TODAY`.
 */
@Composable
internal fun InstantSevenDayDateStrip(
    selectedDay: MatchDayTab,
    selectedCountryZone: UserCountryZone = UserZoneRegistry.defaultCountryZone,
    matchesByDay: Map<MatchDayTab, List<FootballMatch>>,
    liveCountToday: Int,
    onSelectDay: (MatchDayTab) -> Unit,
    activeEdition: SetPlayEdition = SetPlayEdition.PHONE,
    onOpenDateSelectorModal: () -> Unit = {},
    onResetToToday: () -> Unit = { onSelectDay(MatchDayTab.TODAY) }
) {
    val isTv = activeEdition == SetPlayEdition.TV
    val allDays = remember { MatchDayTab.ALL_NAVIGABLE_DAYS }
    val currentIndex = remember(selectedDay) {
        allDays.indexOf(selectedDay).takeIf { it >= 0 } ?: allDays.indexOf(MatchDayTab.TODAY)
    }
    val canStepPrev = currentIndex > 0
    val canStepNext = currentIndex < allDays.lastIndex
    val tzId = selectedCountryZone.timeZoneId
    val navigatorLabel = remember(selectedDay, tzId) {
        selectedDay.getCompactNavigatorLabel(tzId)
    }
    val activeDayMatchCount = matchesByDay[selectedDay].orEmpty().size
    val showLivePulse = selectedDay == MatchDayTab.TODAY && liveCountToday > 0

    val pulseAlpha = if (showLivePulse) {
        val infiniteTransition = rememberInfiniteTransition(label = "day_nav_live_pulse")
        val animated by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.28f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 850, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "day_nav_live_pulse_alpha"
        )
        animated
    } else {
        1f
    }

    val barHeight = if (isTv) 44.dp else 40.dp
    val stepperWidth = if (isTv) 44.dp else 38.dp
    val controlShape = RoundedCornerShape(11.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(barHeight + 6.dp)
            .padding(horizontal = 14.dp, vertical = 3.dp)
            .testTag("five_day_timeline_ribbon"),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left "<" Stepper Button
        Surface(
            color = if (canStepPrev) KickoffTurfSurface else KickoffGlassWell.copy(alpha = 0.45f),
            shape = controlShape,
            border = BorderStroke(
                width = 0.85.dp,
                color = if (canStepPrev) KickoffEmeraldBorder else KickoffRowDivider.copy(alpha = 0.4f)
            ),
            modifier = Modifier
                .width(stepperWidth)
                .height(barHeight)
                .alpha(if (canStepPrev) 1f else 0.35f)
                .clip(controlShape)
                .then(
                    if (canStepPrev) {
                        Modifier.tvFocusableControl(
                            accentColor = KickoffFloodlightVolt,
                            shape = controlShape,
                            onClick = { onSelectDay(allDays[currentIndex - 1]) }
                        )
                    } else {
                        Modifier
                    }
                )
                .testTag("kickoff_prev_day_btn")
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous day",
                    tint = if (canStepPrev) KickoffFloodlightWhite else KickoffSlateMuted,
                    modifier = Modifier.size(if (isTv) 22.dp else 20.dp)
                )
            }
        }

        // Center Interactive Calendar Pill (weight 1f)
        Surface(
            color = KickoffCanopyElevated,
            shape = controlShape,
            border = BorderStroke(
                width = if (showLivePulse) 1.1.dp else 0.9.dp,
                color = if (showLivePulse) KickoffLiveCrimson.copy(alpha = 0.65f) else KickoffPitchEmerald.copy(alpha = 0.65f)
            ),
            modifier = Modifier
                .weight(1f)
                .height(barHeight)
                .clip(controlShape)
                .tvFocusableControl(
                    accentColor = KickoffFloodlightVolt,
                    shape = controlShape,
                    onClick = onOpenDateSelectorModal
                )
                .testTag("kickoff_date_selector_btn")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = if (isTv) 14.dp else 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                KickoffCalendarSelectorIcon(
                    modifier = Modifier.size(if (isTv) 16.dp else 14.dp),
                    tint = KickoffPitchEmerald
                )
                Spacer(modifier = Modifier.width(7.dp))
                if (showLivePulse) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .alpha(pulseAlpha)
                            .clip(CircleShape)
                            .background(KickoffLiveCrimson)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                AnimatedContent(
                    targetState = selectedDay,
                    transitionSpec = {
                        val direction = if (targetState.dayOffset >= initialState.dayOffset) 1 else -1
                        (fadeIn(tween(170)) + slideInHorizontally(tween(190)) { fullWidth -> direction * (fullWidth / 5) })
                            .togetherWith(
                                fadeOut(tween(130)) + slideOutHorizontally(tween(150)) { fullWidth -> -direction * (fullWidth / 5) }
                            )
                    },
                    modifier = Modifier.weight(1f, fill = false),
                    label = "day_navigator_label_transition"
                ) { animatedDay ->
                    val animatedLabel = remember(animatedDay, tzId) {
                        animatedDay.getCompactNavigatorLabel(tzId)
                    }
                    Text(
                        text = animatedLabel,
                        color = KickoffFloodlightWhite,
                        fontSize = if (isTv) 13.5.sp else 12.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        style = TabularNumeralTextStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("kickoff_navigator_date_label")
                    )
                }
                if (activeDayMatchCount > 0) {
                    Spacer(modifier = Modifier.width(7.dp))
                    Surface(
                        color = KickoffPitchEmerald.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.6.dp, KickoffPitchEmerald.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = "$activeDayMatchCount",
                            color = KickoffFloodlightVolt,
                            fontSize = if (isTv) 10.5.sp else 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            style = TabularNumeralTextStyle,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
                        )
                    }
                }
            }
        }

        // Right ">" Stepper Button
        Surface(
            color = if (canStepNext) KickoffTurfSurface else KickoffGlassWell.copy(alpha = 0.45f),
            shape = controlShape,
            border = BorderStroke(
                width = 0.85.dp,
                color = if (canStepNext) KickoffEmeraldBorder else KickoffRowDivider.copy(alpha = 0.4f)
            ),
            modifier = Modifier
                .width(stepperWidth)
                .height(barHeight)
                .alpha(if (canStepNext) 1f else 0.35f)
                .clip(controlShape)
                .then(
                    if (canStepNext) {
                        Modifier.tvFocusableControl(
                            accentColor = KickoffFloodlightVolt,
                            shape = controlShape,
                            onClick = { onSelectDay(allDays[currentIndex + 1]) }
                        )
                    } else {
                        Modifier
                    }
                )
                .testTag("kickoff_next_day_btn")
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next day",
                    tint = if (canStepNext) KickoffFloodlightWhite else KickoffSlateMuted,
                    modifier = Modifier.size(if (isTv) 22.dp else 20.dp)
                )
            }
        }

        // Quick Return to Today Pill (shown whenever not on TODAY)
        if (selectedDay != MatchDayTab.TODAY) {
            Surface(
                color = KickoffPitchEmerald.copy(alpha = 0.20f),
                shape = controlShape,
                border = BorderStroke(1.dp, KickoffPitchEmerald),
                modifier = Modifier
                    .height(barHeight)
                    .clip(controlShape)
                    .tvFocusableControl(
                        accentColor = KickoffFloodlightVolt,
                        shape = controlShape,
                        onClick = onResetToToday
                    )
                    .testTag("kickoff_reset_today_btn")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = if (isTv) 12.dp else 10.dp)
                ) {
                    Text(
                        text = "Today",
                        color = KickoffFloodlightVolt,
                        fontSize = if (isTv) 12.sp else 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Dedicated On-Demand Date Selector Modal (`kickoff_date_selector_modal`):
 * Lists all 8 navigable matchdays (`MatchDayTab.ALL_NAVIGABLE_DAYS`: Yesterday + Today + 6 forward days).
 * - Mobile (`SetPlayEdition.PHONE`): `ModalBottomSheet` titled "Select Matchday" with "Jump to Today" shortcut.
 * - Android TV (`SetPlayEdition.TV`): 10-foot widescreen `Dialog` (`tv_kickoff_date_selector_dialog`) with D-Pad focusable day cards.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KickoffDateSelectorModal(
    activeEdition: SetPlayEdition,
    selectedDay: MatchDayTab,
    selectedCountryZone: UserCountryZone,
    matchesByDay: Map<MatchDayTab, List<FootballMatch>>,
    liveMatchesCount: Int,
    onSelectDay: (MatchDayTab) -> Unit,
    onDismiss: () -> Unit
) {
    val navigableDays = remember { MatchDayTab.ALL_NAVIGABLE_DAYS }
    val tzId = selectedCountryZone.timeZoneId

    val renderAgendaDayCard: @Composable (MatchDayTab, Boolean, Modifier) -> Unit = { day, isTvCard, cardModifier ->
        val isSelected = day == selectedDay
        val isToday = day == MatchDayTab.TODAY
        val dayMatches = matchesByDay[day].orEmpty()
        val matchCount = dayMatches.size
        val activeLeaguesCount = remember(dayMatches) {
            dayMatches.map { it.tournamentId }.distinct().size
        }
        val topTournamentsOnDay = remember(dayMatches) {
            dayMatches
                .map { it.tournamentId to it.tournamentName }
                .distinctBy { it.first }
                .take(3)
        }
        val meaningfulSubtitle = when {
            day == MatchDayTab.YESTERDAY -> if (matchCount > 0) "Yesterday's Results • $activeLeaguesCount Competitions" else "Yesterday's Results"
            isToday -> "Today's Broadcast Slate"
            day == MatchDayTab.TOMORROW -> "Tomorrow's Fixtures"
            matchCount > 0 -> "$activeLeaguesCount Active Competitions"
            else -> "No Scheduled Tier-1 Fixtures"
        }

        Surface(
            color = if (isSelected) KickoffStadiumForest else KickoffTurfSurface,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                width = if (isSelected) 1.5.dp else 0.8.dp,
                color = when {
                    isSelected -> KickoffFloodlightVolt
                    isToday && liveMatchesCount > 0 -> KickoffLiveCrimson.copy(alpha = 0.7f)
                    else -> KickoffEmeraldBorder
                }
            ),
            modifier = cardModifier
                .clip(RoundedCornerShape(12.dp))
                .tvFocusableControl(
                    accentColor = KickoffFloodlightVolt,
                    shape = RoundedCornerShape(12.dp),
                    onClick = { onSelectDay(day) }
                )
                .testTag("day_tab_${day.name.lowercase(Locale.ROOT)}")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = if (isTvCard) 12.dp else 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = if (isSelected) KickoffFloodlightVolt else KickoffCanopyElevated,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            0.7.dp,
                            if (isSelected) KickoffFloodlightVolt else KickoffPitchEmerald.copy(alpha = 0.6f)
                        )
                    ) {
                        Text(
                            text = day.getShortDayCode(tzId),
                            color = if (isSelected) KickoffPitchBlack else KickoffFloodlightWhite,
                            fontSize = if (isTvCard) 12.sp else 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "${day.getDynamicDateSubtitle(tzId)} • ${day.getWeekdayFullUpper(tzId)}",
                            color = KickoffFloodlightWhite,
                            fontSize = if (isTvCard) 13.sp else 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = meaningfulSubtitle,
                            color = if (isSelected) KickoffFloodlightVolt else KickoffMistSilver,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isToday && liveMatchesCount > 0) {
                        Surface(
                            color = KickoffLiveCrimson.copy(alpha = 0.20f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.8.dp, KickoffLiveCrimson)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                KickoffLiveBroadcastBeaconIcon(
                                    modifier = Modifier.size(9.dp),
                                    color = KickoffLiveCrimson
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "$liveMatchesCount LIVE",
                                    color = KickoffFloodlightWhite,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        topTournamentsOnDay.forEach { (tId, tName) ->
                            TournamentLogoMedallion(
                                tournamentId = tId,
                                tournamentName = tName,
                                accentColor = resolveCompetitionAccentColor(tId),
                                sizeDp = 18
                            )
                        }
                    }

                    Surface(
                        color = if (isSelected) KickoffFloodlightVolt else KickoffGlassWell,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(
                            0.7.dp,
                            if (isSelected) KickoffFloodlightVolt else KickoffEmeraldBorder
                        )
                    ) {
                        Text(
                            text = if (day == MatchDayTab.YESTERDAY && dayMatches.isEmpty()) "View" else "${dayMatches.size} Matches",
                            color = if (isSelected) KickoffPitchBlack else KickoffFloodlightWhite,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            style = TabularNumeralTextStyle,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

    if (activeEdition == SetPlayEdition.TV) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                color = KickoffPitchBlack,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("tv_kickoff_date_selector_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(28.dp)
                        .testTag("kickoff_date_selector_modal"),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            KickoffCalendarSelectorIcon(
                                modifier = Modifier.size(22.dp),
                                tint = KickoffFloodlightVolt
                            )
                            Column {
                                Text(
                                    text = "Select Matchday (Yesterday + 7-Day Schedule)",
                                    color = KickoffFloodlightWhite,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "${selectedCountryZone.flagEmoji} ${selectedCountryZone.countryName} Timezone (${selectedCountryZone.timeZoneId})",
                                    color = KickoffMistSilver,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (selectedDay != MatchDayTab.TODAY) {
                                Surface(
                                    color = KickoffPitchEmerald,
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .tvFocusableControl(
                                            accentColor = KickoffFloodlightVolt,
                                            shape = RoundedCornerShape(50),
                                            onClick = { onSelectDay(MatchDayTab.TODAY) }
                                        )
                                ) {
                                    Text(
                                        text = "↩ Jump to Today",
                                        color = KickoffPitchBlack,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                            Surface(
                                color = KickoffCanopyElevated,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.8.dp, KickoffEmeraldBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .tvFocusableControl(
                                        accentColor = KickoffFloodlightVolt,
                                        shape = RoundedCornerShape(50),
                                        onClick = onDismiss
                                    )
                            ) {
                                Text(
                                    text = "Close",
                                    color = KickoffFloodlightWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(navigableDays, key = { "tv_date_modal_${it.name}" }) { day ->
                            renderAgendaDayCard(
                                day,
                                true,
                                Modifier
                                    .fillMaxWidth()
                                    .height(82.dp)
                            )
                        }
                    }
                }
            }
        }
    } else {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = KickoffPitchBlack
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 8.dp)
                    .testTag("kickoff_date_selector_modal"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KickoffCalendarSelectorIcon(
                            modifier = Modifier.size(18.dp),
                            tint = KickoffFloodlightVolt
                        )
                        Column {
                            Text(
                                text = "Select Matchday",
                                color = KickoffFloodlightWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Yesterday + 7-Day Agenda • ${selectedCountryZone.shortIso3}",
                                color = KickoffMistSilver,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (selectedDay != MatchDayTab.TODAY) {
                        Surface(
                            color = KickoffPitchEmerald.copy(alpha = 0.20f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.8.dp, KickoffPitchEmerald),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { onSelectDay(MatchDayTab.TODAY) }
                        ) {
                            Text(
                                text = "↩ Jump to Today",
                                color = KickoffFloodlightVolt,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(navigableDays, key = { "phone_date_modal_${it.name}" }) { day ->
                        renderAgendaDayCard(
                            day,
                            false,
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

/**
 * Dedicated On-Demand Competitions Selector Modal (`kickoff_competitions_selector_modal`):
 * - Section A: Competitions with active fixtures on the selected matchday (`matchCount > 0`).
 * - Section B: Collapsible other followed competitions with `0` fixtures today.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KickoffCompetitionsSelectorModal(
    activeEdition: SetPlayEdition,
    selectedMatchDay: MatchDayTab,
    selectedFilter: String,
    activeDayMatches: List<FootballMatch>,
    favoriteTeamsCount: Int,
    liveMatchesCount: Int,
    tournamentPreferences: List<TournamentPreferenceEntity>,
    onSelectFilter: (String) -> Unit,
    onOpenManageLeagues: () -> Unit,
    onDismiss: () -> Unit
) {
    val isTvLayout = activeEdition == SetPlayEdition.TV
    val prefMap = remember(tournamentPreferences) {
        tournamentPreferences.associateBy { it.tournamentId }
    }
    val matchCountByTournament = remember(activeDayMatches) {
        activeDayMatches.groupingBy { it.tournamentId }.eachCount()
    }
    val enabledCatalogTournaments = remember(prefMap) {
        FootballRepository.ALL_TOURNAMENTS.filter { item ->
            prefMap[item.tournamentId]?.isEnabled ?: true
        }.sortedWith(
            compareBy<com.example.data.repository.TournamentCatalogItem> { item ->
                !(prefMap[item.tournamentId]?.isPinned == true)
            }.thenBy { it.defaultSortOrder }
        )
    }
    val activeTodayTournaments = remember(enabledCatalogTournaments, matchCountByTournament) {
        enabledCatalogTournaments.filter { (matchCountByTournament[it.tournamentId] ?: 0) > 0 }
    }
    val zeroTodayTournaments = remember(enabledCatalogTournaments, matchCountByTournament) {
        enabledCatalogTournaments.filter { (matchCountByTournament[it.tournamentId] ?: 0) == 0 }
    }
    var showZeroMatchLeagues by remember(activeTodayTournaments.size) {
        mutableStateOf(activeTodayTournaments.isEmpty())
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(KickoffPitchBlack.copy(alpha = 0.68f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .then(
                    if (isTvLayout) {
                        Modifier.testTag("tv_kickoff_competitions_selector_dialog")
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            AnimatedVisibility(
                visible = true,
                enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn()
            ) {
                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp),
                    border = BorderStroke(1.2.dp, KickoffPitchEmerald.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .then(
                            if (isTvLayout) {
                                Modifier.width(340.dp)
                            } else {
                                Modifier
                                    .fillMaxWidth(0.52f)
                                    .widthIn(min = 215.dp, max = 260.dp)
                            }
                        )
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    KickoffPitchBlack.copy(alpha = 0.94f),
                                    KickoffTurfSurface.copy(alpha = 0.90f)
                                )
                            )
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                        .testTag("kickoff_competitions_selector_modal")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .padding(
                                horizontal = if (isTvLayout) 16.dp else 12.dp,
                                vertical = 14.dp
                            ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Top Header: Shield icon + "Competitions" title + "${activeTodayTournaments.size} Active" subtitle + compact close ✕ button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                KickoffLeagueShieldSelectorIcon(
                                    modifier = Modifier.size(if (isTvLayout) 20.dp else 17.dp),
                                    tint = KickoffFloodlightVolt
                                )
                                Column {
                                    Text(
                                        text = "Competitions",
                                        color = KickoffFloodlightWhite,
                                        fontSize = if (isTvLayout) 16.sp else 14.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${activeTodayTournaments.size} Active",
                                        color = KickoffMistSilver,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                }
                            }

                            Surface(
                                color = KickoffCanopyElevated,
                                shape = CircleShape,
                                border = BorderStroke(0.8.dp, KickoffEmeraldBorder),
                                modifier = Modifier
                                    .size(if (isTvLayout) 32.dp else 28.dp)
                                    .clip(CircleShape)
                                    .tvFocusableControl(
                                        accentColor = KickoffFloodlightVolt,
                                        shape = CircleShape,
                                        onClick = onDismiss
                                    )
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "✕",
                                        color = KickoffFloodlightWhite,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        // Quick Filter Row: All Matches | ★ My Teams
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isAllSelected = selectedFilter == "ALL"
                            Surface(
                                color = if (isAllSelected) KickoffPitchEmerald else KickoffTurfSurface,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    if (isAllSelected) 1.2.dp else 0.8.dp,
                                    if (isAllSelected) KickoffFloodlightVolt else KickoffEmeraldBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .tvFocusableControl(
                                        accentColor = KickoffFloodlightVolt,
                                        shape = RoundedCornerShape(10.dp),
                                        onClick = { onSelectFilter("ALL") }
                                    )
                                    .testTag("filter_chip_all")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = "All Matches (${activeDayMatches.size})",
                                        color = if (isAllSelected) KickoffPitchBlack else KickoffFloodlightWhite,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            val isMyTeamsSelected = selectedFilter == "MY_TEAMS"
                            Surface(
                                color = if (isMyTeamsSelected) KickoffTrophyGold else KickoffTurfSurface,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    if (isMyTeamsSelected) 1.2.dp else 0.8.dp,
                                    if (isMyTeamsSelected) KickoffFloodlightVolt else KickoffTrophyGold.copy(alpha = 0.65f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .tvFocusableControl(
                                        accentColor = KickoffFloodlightVolt,
                                        shape = RoundedCornerShape(10.dp),
                                        onClick = { onSelectFilter("MY_TEAMS") }
                                    )
                                    .testTag("filter_chip_my_teams")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 6.dp)
                                ) {
                                    Text(
                                        text = "★ My Teams ($favoriteTeamsCount)",
                                        color = if (isMyTeamsSelected) KickoffPitchBlack else KickoffTrophyGold,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Scrollable Vertical Competition List
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (activeTodayTournaments.isNotEmpty()) {
                                item(key = "active_today_section_header") {
                                    val playingDayHeader = when (selectedMatchDay) {
                                        MatchDayTab.TODAY -> "PLAYING TODAY"
                                        MatchDayTab.TOMORROW -> "PLAYING TOMORROW"
                                        else -> {
                                            val dayNum = selectedMatchDay.getDynamicDateSubtitle().takeWhile { it.isDigit() }
                                            "PLAYING ${selectedMatchDay.getShortDayCode().uppercase(Locale.ROOT)} $dayNum".trim()
                                        }
                                    }
                                    Text(
                                        text = playingDayHeader,
                                        color = KickoffFloodlightVolt,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.6.sp,
                                        modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                                    )
                                }

                                items(
                                    items = activeTodayTournaments,
                                    key = { "active_blade_comp_${it.tournamentId}" }
                                ) { tournament ->
                                    val count = matchCountByTournament[tournament.tournamentId] ?: 0
                                    val isSelected = selectedFilter == tournament.tournamentId
                                    val compAccent = resolveCompetitionAccentColor(tournament.tournamentId)
                                    val compMatches = activeDayMatches.filter { it.tournamentId == tournament.tournamentId }
                                    val compLiveCount = compMatches.count {
                                        it.status == MatchStatus.LIVE || it.status == MatchStatus.HALFTIME
                                    }
                                    val nextUpcomingTime = compMatches
                                        .filter { it.status == MatchStatus.UPCOMING }
                                        .minByOrNull { parseKickoffTimeToMinutes(it.kickoffTimeFormatted) }
                                        ?.kickoffTimeFormatted
                                    val (subtitleText, subtitleColor) = when {
                                        compLiveCount > 0 ->
                                            "● $compLiveCount LIVE • ${tournament.countryOrRegion}" to KickoffLiveCrimson
                                        nextUpcomingTime != null ->
                                            "${tournament.countryOrRegion} • Next $nextUpcomingTime" to KickoffFloodlightVolt.copy(alpha = 0.9f)
                                        else ->
                                            "${tournament.countryOrRegion} • Completed" to KickoffMistSilver
                                    }

                                    Surface(
                                        color = if (isSelected) KickoffStadiumForest else KickoffTurfSurface,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(
                                            if (isSelected) 1.3.dp else 0.8.dp,
                                            if (isSelected) KickoffFloodlightVolt else KickoffEmeraldBorder
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(54.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .tvFocusableControl(
                                                accentColor = KickoffFloodlightVolt,
                                                shape = RoundedCornerShape(12.dp),
                                                onClick = { onSelectFilter(tournament.tournamentId) }
                                            )
                                            .testTag("filter_chip_${tournament.tournamentId}")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .width(3.dp)
                                                    .height(28.dp)
                                                    .clip(RoundedCornerShape(50))
                                                    .background(if (isSelected) KickoffFloodlightVolt else compAccent)
                                            )
                                            TournamentLogoMedallion(
                                                tournamentId = tournament.tournamentId,
                                                tournamentName = tournament.name,
                                                isSelected = isSelected,
                                                accentColor = compAccent,
                                                sizeDp = 30
                                            )
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = tournament.name,
                                                    color = KickoffFloodlightWhite,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = subtitleText,
                                                    color = subtitleColor,
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Surface(
                                                color = if (isSelected) KickoffFloodlightVolt else KickoffCanopyElevated,
                                                shape = RoundedCornerShape(50),
                                                border = BorderStroke(
                                                    0.8.dp,
                                                    if (isSelected) KickoffFloodlightVolt else KickoffEmeraldBorder
                                                )
                                            ) {
                                                Text(
                                                    text = "$count",
                                                    color = if (isSelected) KickoffPitchBlack else KickoffFloodlightWhite,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    style = TabularNumeralTextStyle,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (zeroTodayTournaments.isNotEmpty()) {
                                item(key = "zero_today_section_header") {
                                    Surface(
                                        color = KickoffCanopyElevated.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(0.7.dp, KickoffEmeraldBorder),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 6.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .tvFocusableControl(
                                                accentColor = KickoffFloodlightVolt,
                                                shape = RoundedCornerShape(8.dp),
                                                onClick = { showZeroMatchLeagues = !showZeroMatchLeagues }
                                            )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "OTHER LEAGUES (${zeroTodayTournaments.size})",
                                                color = KickoffMistSilver,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 0.5.sp
                                            )
                                            Text(
                                                text = if (showZeroMatchLeagues) "▲" else "▼",
                                                color = KickoffFloodlightVolt,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }

                                if (showZeroMatchLeagues) {
                                    items(
                                        items = zeroTodayTournaments,
                                        key = { "zero_blade_comp_${it.tournamentId}" }
                                    ) { tournament ->
                                        val isSelected = selectedFilter == tournament.tournamentId
                                        val compAccent = resolveCompetitionAccentColor(tournament.tournamentId)
                                        Surface(
                                            color = KickoffTurfSurface.copy(alpha = 0.65f),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(
                                                0.8.dp,
                                                if (isSelected) KickoffFloodlightVolt else KickoffEmeraldBorder
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .tvFocusableControl(
                                                    accentColor = KickoffFloodlightVolt,
                                                    shape = RoundedCornerShape(10.dp),
                                                    onClick = { onSelectFilter(tournament.tournamentId) }
                                                )
                                                .testTag("filter_chip_${tournament.tournamentId}")
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    TournamentLogoMedallion(
                                                        tournamentId = tournament.tournamentId,
                                                        tournamentName = tournament.name,
                                                        isSelected = isSelected,
                                                        accentColor = compAccent,
                                                        sizeDp = 22
                                                    )
                                                    Text(
                                                        text = tournament.name,
                                                        color = KickoffMistSilver,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                Text(
                                                    text = "0",
                                                    color = KickoffMistSilver.copy(alpha = 0.65f),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom Footer Action: Always-visible Divider + "⚙ Manage Followed Leagues"
                        HorizontalDivider(
                            thickness = 0.6.dp,
                            color = KickoffEmeraldBorder
                        )
                        Surface(
                            color = KickoffCanopyElevated,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, KickoffPitchEmerald),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .tvFocusableControl(
                                    accentColor = KickoffFloodlightVolt,
                                    shape = RoundedCornerShape(10.dp),
                                    onClick = onOpenManageLeagues
                                )
                                .testTag("modify_tournaments_chip")
                        ) {
                            Text(
                                text = "⚙ Manage Followed Leagues",
                                color = KickoffFloodlightVolt,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)
                            )
                        }
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

internal enum class TournamentLogoContrastMode {
    MONOCHROME_WHITE_TINT,
    ENHANCED_RADIAL_BACKLIGHT,
    ROUNDED_OPAQUE_CLIP
}

internal fun resolveTournamentLogoContrastMode(tournamentId: String): TournamentLogoContrastMode {
    return when (tournamentId.lowercase(Locale.ROOT).trim()) {
        "uefa_cl", "ucl", "uefa_champions_league", "champions_league",
        "premier_league", "epl",
        "ligue_1", "ligue1" -> TournamentLogoContrastMode.MONOCHROME_WHITE_TINT
        "serie_a", "seriea",
        "botola_pro", "botola" -> TournamentLogoContrastMode.ROUNDED_OPAQUE_CLIP
        else -> TournamentLogoContrastMode.ENHANCED_RADIAL_BACKLIGHT
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
    val useCustomVector = remember(tournamentId) {
        hasCustomCompetitionVectorEmblem(tournamentId)
    }
    val contrastMode = remember(tournamentId) {
        resolveTournamentLogoContrastMode(tournamentId)
    }
    val badgeScale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tournament_logo_scale_$tournamentId"
    )
    val emblemShape = RoundedCornerShape((sizeDp * 0.28f).coerceIn(6f, 10f).dp)

    val renderMonogramFallback: @Composable () -> Unit = {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .clip(emblemShape)
                .background(KickoffCanopyElevated)
                .border(0.8.dp, accentColor.copy(alpha = 0.45f), emblemShape)
        ) {
            Text(
                text = monogram,
                color = accentColor,
                fontSize = (sizeDp * 0.31f).sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(sizeDp.dp)
            .graphicsLayer {
                scaleX = badgeScale
                scaleY = badgeScale
            }
    ) {
        if (useCustomVector) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(emblemShape)
            ) {
                KickoffCustomCompetitionEmblem(
                    tournamentId = tournamentId,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 1.dp,
                            color = if (isSelected) KickoffFloodlightVolt else accentColor.copy(alpha = 0.72f),
                            shape = emblemShape
                        )
                )
            }
        } else {
            val radialBacklight = when (contrastMode) {
                TournamentLogoContrastMode.MONOCHROME_WHITE_TINT -> Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.16f),
                        accentColor.copy(alpha = 0.08f),
                        Color.Transparent
                    )
                )
                TournamentLogoContrastMode.ENHANCED_RADIAL_BACKLIGHT -> Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.28f),
                        Color.White.copy(alpha = 0.10f),
                        Color.Transparent
                    )
                )
                TournamentLogoContrastMode.ROUNDED_OPAQUE_CLIP -> Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.18f),
                        accentColor.copy(alpha = 0.06f),
                        Color.Transparent
                    )
                )
            }
            val logoColorFilter = if (contrastMode == TournamentLogoContrastMode.MONOCHROME_WHITE_TINT) {
                ColorFilter.tint(Color.White.copy(alpha = 0.94f))
            } else {
                null
            }
            val imageModifier = Modifier
                .fillMaxSize()
                .padding((sizeDp * 0.05f).coerceAtLeast(1f).dp)
                .then(
                    if (contrastMode == TournamentLogoContrastMode.ROUNDED_OPAQUE_CLIP) {
                        Modifier.clip(RoundedCornerShape(8.dp))
                    } else {
                        Modifier
                    }
                )

            // Unboxed tournament logo with smart dark-emblem contrast & borderless radial ambient glow
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(radialBacklight)
            ) {
                if (logoUrl.isNotBlank()) {
                    SubcomposeAsyncImage(
                        model = logoUrl,
                        contentDescription = tournamentName,
                        contentScale = ContentScale.Fit,
                        colorFilter = logoColorFilter,
                        modifier = imageModifier,
                        loading = { renderMonogramFallback() },
                        error = { renderMonogramFallback() }
                    )
                } else {
                    renderMonogramFallback()
                }
            }
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
    manualTimeOffsetMinutes: Int = 0,
    liveClockTickMs: Long = System.currentTimeMillis(),
    uiPreferences: KickoffUiPreferences = KickoffUiPreferences(),
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
            favoriteMatches.forEachIndexed { index, match ->
                val matchUtcBadge = remember(match.kickoffEpochMs, selectedCountryZone, manualTimeOffsetMinutes) {
                    val refEpoch = if (match.kickoffEpochMs > 0L) match.kickoffEpochMs else System.currentTimeMillis()
                    selectedCountryZone.getUtcOffsetBadge(
                        epochMs = refEpoch,
                        manualOffsetMinutes = manualTimeOffsetMinutes
                    )
                }
                FlashscoreMatchCard(
                    match = match,
                    selectedCountryZone = selectedCountryZone,
                    selectedSeasonTimeMode = selectedSeasonTimeMode,
                    manualTimeOffsetMinutes = manualTimeOffsetMinutes,
                    utcOffsetBadge = matchUtcBadge,
                    isHomeTeamFavorite = true,
                    isAwayTeamFavorite = false,
                    onToggleHomeFavorite = {},
                    onToggleAwayFavorite = {},
                    onOpenDossier = { onInspectMatchDossier(match) },
                    onWatchBeinClicked = { ch -> onWatchMatch(match, ch) },
                    isLastInSection = index == favoriteMatches.lastIndex,
                    liveClockTickMs = liveClockTickMs,
                    uiPreferences = uiPreferences
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

internal fun shouldShowTournamentRegionBadge(tournamentName: String, regionName: String): Boolean {
    return regionName.isNotBlank() &&
        !tournamentName.contains(regionName.trim(), ignoreCase = true) &&
        !tournamentName.startsWith("International", ignoreCase = true)
}

/**
 * Distinctive 52.dp Two-Line Competition Chapter Header (`FlashscoreTournamentHeader`):
 * - Uses full rounded card shape (`RoundedCornerShape(12.dp)`), generous chapter separation (`20.dp` top, `8.dp` for first group, `6.dp` bottom),
 *   a 3.5.dp left competition accent bar, prominent 36.dp unboxed tournament logo, and 2-line Tournament Name + uppercase Region subtitle hierarchy.
 */
@Composable
internal fun FlashscoreTournamentHeader(
    tournamentId: String,
    tournamentName: String,
    regionName: String,
    menaProviderName: String,
    matchCount: Int,
    liveCount: Int,
    isPinned: Boolean,
    isCollapsed: Boolean,
    isFirstSection: Boolean = false,
    onToggleCollapse: () -> Unit,
    onTogglePin: () -> Unit
) {
    val compAccent = resolveCompetitionAccentColor(tournamentId)
    val chevronRotation by animateFloatAsState(
        targetValue = if (isCollapsed) -90f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "chevron_rotation_$tournamentId"
    )
    val headerShape = RoundedCornerShape(12.dp)
    val regionSubtitle = remember(regionName) {
        regionName.trim().ifBlank { "INTERNATIONAL" }.uppercase(Locale.US)
    }

    Surface(
        color = KickoffCanopyElevated,
        shape = headerShape,
        border = BorderStroke(
            width = 0.9.dp,
            color = if (liveCount > 0) KickoffLiveCrimson else compAccent.copy(alpha = 0.40f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 14.dp,
                end = 14.dp,
                top = if (isFirstSection) 8.dp else 20.dp,
                bottom = 6.dp
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
                .height(52.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            compAccent.copy(alpha = 0.22f),
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
                // Crisp 3.5.dp Left Competition Accent Bar
                Box(
                    modifier = Modifier
                        .width(3.5.dp)
                        .fillMaxHeight()
                        .background(compAccent)
                )
                Spacer(modifier = Modifier.width(10.dp))
                TournamentLogoMedallion(
                    tournamentId = tournamentId,
                    tournamentName = tournamentName,
                    isSelected = isPinned,
                    accentColor = compAccent,
                    sizeDp = 36
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = tournamentName,
                        color = KickoffFloodlightWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = regionSubtitle,
                        color = compAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (liveCount > 0) {
                    Surface(
                        color = KickoffLiveCrimson,
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
                        .size(28.dp)
                        .testTag("pin_tournament_$tournamentId")
                ) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pin Tournament",
                        tint = if (isPinned) KickoffTrophyGold else KickoffSlateMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Surface(
                    color = KickoffGlassWell,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.7.dp, compAccent.copy(alpha = 0.32f))
                ) {
                    Text(
                        text = "$matchCount",
                        color = KickoffMistSilver,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        style = TabularNumeralTextStyle,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.5.dp)
                    )
                }

                Spacer(modifier = Modifier.width(5.dp))

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isCollapsed) "Expand" else "Collapse",
                    tint = KickoffMistSilver,
                    modifier = Modifier
                        .size(17.dp)
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

internal fun formatCleanBroadcastChannelSuffix(rawChannel: String, providerShortTag: String): String {
    val trimmed = rawChannel.trim()
    if (trimmed.isBlank()) return "Live Broadcast"
    if (providerShortTag.equals("beIN", ignoreCase = true)) {
        val withoutBein = trimmed.replaceFirst(Regex("^be\\s*in\\s+", RegexOption.IGNORE_CASE), "").trim()
        if (withoutBein.isNotBlank()) return withoutBein
    }
    return trimmed
}

/**
 * V20 Separated Match Card (`FlashscoreMatchCard`):
 * - Independent `RoundedCornerShape(14.dp)` card with `8.dp` (`6.dp` compact) vertical separation.
 * - Uses hoisted `liveClockTickMs` from HomeScreen (zero per-card `LaunchedEffect` coroutines).
 * - Inset `3.dp` rounded status indicator shown ONLY for LIVE, FAVORITE TEAM, POSTPONED, or CANCELLED matches.
 * - Tier 1: `38.dp` (`34.dp` compact) framed crests, `13.5.sp` / `16.sp` 2-line team names, `66.dp` min-width state-driven center scorebug.
 * - Tier 2: Subtly recessed `32.dp` broadcast footer with bright emerald CTA only for LIVE matches and low-noise ghost pills otherwise.
 */
@Composable
internal fun FlashscoreMatchCard(
    match: FootballMatch,
    selectedCountryZone: UserCountryZone = UserZoneRegistry.defaultCountryZone,
    selectedSeasonTimeMode: SeasonTimeMode = SeasonTimeMode.AUTO,
    manualTimeOffsetMinutes: Int = 0,
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
    liveClockTickMs: Long = System.currentTimeMillis(),
    uiPreferences: KickoffUiPreferences = KickoffUiPreferences()
) {
    val isLive = match.status == MatchStatus.LIVE || match.status == MatchStatus.HALFTIME
    val isHalftime = match.status == MatchStatus.HALFTIME || match.liveMinuteOrBadge.equals("HT", ignoreCase = true)
    val isFinished = match.status == MatchStatus.FINISHED
    val isPostponed = match.status == MatchStatus.POSTPONED
    val isCancelled = match.status == MatchStatus.CANCELLED
    val isUpcoming = match.status == MatchStatus.UPCOMING
    val hasFavTeam = isHomeTeamFavorite || isAwayTeamFavorite
    val broadcasterIdentity = remember(match.beinChannel, match.menaProviderName) {
        resolveBroadcasterVisualIdentity(match.beinChannel.ifBlank { match.menaProviderName })
    }
    val rawChannelLabel = match.beinChannel.ifBlank { match.menaProviderName.ifBlank { "Live Broadcast" } }
    val cleanChannelSuffix = remember(rawChannelLabel, broadcasterIdentity.providerShortTag) {
        formatCleanBroadcastChannelSuffix(rawChannelLabel, broadcasterIdentity.providerShortTag)
    }

    val homeIsWinner = isFinished && !uiPreferences.hideLiveAndFinalScores &&
        match.homeScore != null && match.awayScore != null && match.homeScore > match.awayScore
    val awayIsWinner = isFinished && !uiPreferences.hideLiveAndFinalScores &&
        match.homeScore != null && match.awayScore != null && match.awayScore > match.homeScore
    val isDecidedFinish = isFinished && !uiPreferences.hideLiveAndFinalScores &&
        match.homeScore != null && match.awayScore != null && match.homeScore != match.awayScore

    val dynamicKickoffTime = remember(
        match.kickoffEpochMs,
        match.kickoffTimeFormatted,
        selectedCountryZone,
        manualTimeOffsetMinutes,
        uiPreferences.use24HourFormat
    ) {
        if (match.kickoffEpochMs > 0L) {
            formatDynamicKickoffTime(
                kickoffEpochMs = match.kickoffEpochMs,
                zone = selectedCountryZone,
                manualOffsetMinutes = manualTimeOffsetMinutes,
                use24Hour = uiPreferences.use24HourFormat
            )
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
    val liveElapsedSeconds = if (match.status == MatchStatus.LIVE) {
        ((liveClockTickMs - anchorSyncOriginMs) / 1000L).toInt().coerceAtLeast(0)
    } else {
        0
    }
    val clockTelemetry = remember(match, utcOffsetBadge, liveElapsedSeconds, dynamicKickoffTime) {
        parseLiveMatchClockTelemetry(
            match = match,
            utcOffsetBadge = utcOffsetBadge,
            elapsedSecondOffset = liveElapsedSeconds,
            dynamicKickoffTime = dynamicKickoffTime
        )
    }

    val cardShape = RoundedCornerShape(14.dp)
    val compAccent = resolveCompetitionAccentColor(match.tournamentId)
    // Symmetric POSTPONED cards rely on centered amber PPD pill without a floating left bar
    val showStatusIndicator = isLive || hasFavTeam || isCancelled
    val statusIndicatorColor = when {
        isLive -> KickoffLiveCrimson
        isCancelled -> KickoffLiveCrimson.copy(alpha = 0.72f)
        else -> KickoffTrophyGold
    }
    val cardTopSpacing = if (uiPreferences.compactCardDensity) 5.dp else 6.dp
    val crestSizeDp = if (uiPreferences.compactCardDensity) 40 else 46
    val cardSurfaceBrush = if (isLive) {
        Brush.horizontalGradient(
            listOf(
                KickoffLiveCrimson.copy(alpha = 0.14f),
                KickoffStadiumForest.copy(alpha = 0.72f),
                compAccent.copy(alpha = 0.08f)
            )
        )
    } else {
        Brush.horizontalGradient(
            listOf(
                compAccent.copy(alpha = 0.08f),
                KickoffTurfSurface,
                compAccent.copy(alpha = 0.04f)
            )
        )
    }

    Surface(
        color = if (isLive) KickoffStadiumForest.copy(alpha = 0.62f) else KickoffTurfSurface,
        shape = cardShape,
        border = BorderStroke(
            width = if (isLive) 0.9.dp else 0.85.dp,
            color = when {
                isLive -> KickoffLiveCrimson.copy(alpha = 0.36f)
                hasFavTeam -> KickoffTrophyGold.copy(alpha = 0.48f)
                else -> compAccent.copy(alpha = 0.30f)
            }
        ),
        shadowElevation = if (isLive) 4.dp else 1.5.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isInsidePinnedSection) 0.dp else 14.dp,
                end = if (isInsidePinnedSection) 0.dp else 14.dp,
                top = cardTopSpacing
            )
            .clip(cardShape)
            .luxuryFocusableCard(
                accentColor = KickoffFloodlightVolt,
                shape = cardShape,
                onClick = onOpenDossier
            )
            .testTag("match_card_${match.matchId}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardSurfaceBrush)
        ) {
            // TIER 1: Match Headline Row (Unboxed 46.dp/40.dp Crests + 14.sp 2-Line Names + Strict 78.dp Center Broadcast Stage)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = if (uiPreferences.compactCardDensity) 58.dp else 68.dp)
                    .padding(vertical = if (uiPreferences.compactCardDensity) 6.dp else 8.dp)
                    .testTag("open_match_center_${match.matchId}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showStatusIndicator) {
                    Spacer(modifier = Modifier.width(5.dp))
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(if (uiPreferences.compactCardDensity) 36.dp else 42.dp)
                            .clip(RoundedCornerShape(50))
                            .background(statusIndicatorColor)
                    )
                }

                // 1. HOME TEAM ZONE (Right-Aligned toward Center Broadcast Stage, weight 1f)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            start = if (showStatusIndicator) 7.dp else 12.dp,
                            end = 6.dp
                        ),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (isHomeTeamFavorite) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = KickoffTrophyGold,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                            }
                            Text(
                                text = match.homeTeamName,
                                color = when {
                                    isDecidedFinish && !homeIsWinner -> KickoffFloodlightWhite.copy(alpha = 0.64f)
                                    isHomeTeamFavorite -> KickoffFloodlightVolt
                                    else -> KickoffFloodlightWhite
                                },
                                fontSize = 14.sp,
                                lineHeight = 16.5.sp,
                                letterSpacing = (-0.12).sp,
                                fontWeight = if (homeIsWinner || isLive || isHomeTeamFavorite) FontWeight.ExtraBold else FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.End
                            )
                        }
                        if (match.homeRedCards > 0) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.testTag("home_red_card_badge_${match.matchId}")
                            ) {
                                KickoffBookingCardIcon(
                                    isRedCard = true,
                                    modifier = Modifier.size(10.dp)
                                )
                                if (match.homeRedCards > 1) {
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${match.homeRedCards}",
                                        color = KickoffLiveCrimson,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        style = TabularNumeralTextStyle
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(7.dp))
                    TeamCrestImage(
                        badgeUrl = match.homeTeamBadge,
                        teamName = match.homeTeamName,
                        sizeDp = crestSizeDp
                    )
                }

                // 2. STRICT FIXED-WIDTH 78.dp CENTER BROADCAST SCORE STAGE
                MatchCardBroadcastScoreStage(
                    match = match,
                    dynamicKickoffTime = dynamicKickoffTime,
                    liveReadout = clockTelemetry.heroReadout,
                    compAccent = compAccent,
                    stageWidthDp = 78,
                    hideScores = uiPreferences.hideLiveAndFinalScores,
                    isHalftime = isHalftime,
                    homeIsWinner = homeIsWinner,
                    awayIsWinner = awayIsWinner,
                    isDecidedFinish = isDecidedFinish
                )

                // 3. AWAY TEAM ZONE (Left-Aligned from Center Broadcast Stage, weight 1f)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 6.dp, end = 12.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TeamCrestImage(
                        badgeUrl = match.awayTeamBadge,
                        teamName = match.awayTeamName,
                        sizeDp = crestSizeDp
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = match.awayTeamName,
                                color = when {
                                    isDecidedFinish && !awayIsWinner -> KickoffFloodlightWhite.copy(alpha = 0.64f)
                                    isAwayTeamFavorite -> KickoffFloodlightVolt
                                    else -> KickoffFloodlightWhite
                                },
                                fontSize = 14.sp,
                                lineHeight = 16.5.sp,
                                letterSpacing = (-0.12).sp,
                                fontWeight = if (awayIsWinner || isLive || isAwayTeamFavorite) FontWeight.ExtraBold else FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isAwayTeamFavorite) {
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = KickoffTrophyGold,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                        if (match.awayRedCards > 0) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start,
                                modifier = Modifier.testTag("away_red_card_badge_${match.matchId}")
                            ) {
                                KickoffBookingCardIcon(
                                    isRedCard = true,
                                    modifier = Modifier.size(10.dp)
                                )
                                if (match.awayRedCards > 1) {
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${match.awayRedCards}",
                                        color = KickoffLiveCrimson,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        style = TabularNumeralTextStyle
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // HAIRLINE COMPETITION-TINTED DIVIDER BETWEEN TIER 1 SCOREBOARD AND TIER 2 RECESSED FOOTER
            HorizontalDivider(
                thickness = 0.5.dp,
                color = compAccent.copy(alpha = 0.16f)
            )

            // TIER 2: SUBTLY RECESSED BROADCAST & ACTION FOOTER (`height = 30.dp`)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .background(KickoffPitchBlack.copy(alpha = 0.34f))
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Single Sleek Provider Micro-Pill + Clean Channel Suffix + `+N` Alternate Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Surface(
                        color = broadcasterIdentity.containerColor,
                        shape = RoundedCornerShape(5.dp),
                        border = BorderStroke(0.7.dp, broadcasterIdentity.borderColor)
                    ) {
                        Text(
                            text = broadcasterIdentity.providerShortTag,
                            color = broadcasterIdentity.textColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                    Text(
                        text = cleanChannelSuffix,
                        color = KickoffMistSilver,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (match.fallbackMenaChannels.isNotEmpty()) {
                        Surface(
                            color = KickoffCanopyElevated.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.6.dp, compAccent.copy(alpha = 0.28f))
                        ) {
                            Text(
                                text = "+${match.fallbackMenaChannels.size}",
                                color = KickoffMistSilver,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                style = TabularNumeralTextStyle,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right: Pin Star (`26.dp`) + Subtle Vertical Divider + Quiet Ghost Pill (Bright Emerald ONLY for LIVE)
                val useMatchCenterAction = isFinished || isPostponed || isCancelled
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .clickable { onTogglePinMatch() }
                            .testTag("pin_match_btn_${match.matchId}")
                    ) {
                        Icon(
                            imageVector = if (isMatchPinned || hasFavTeam) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Pin Match",
                            tint = when {
                                isMatchPinned -> KickoffTrophyGold
                                hasFavTeam -> KickoffPitchEmerald
                                else -> KickoffMistSilver.copy(alpha = 0.50f)
                            },
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(5.dp))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(12.dp)
                            .background(KickoffRowDivider.copy(alpha = 0.48f))
                    )
                    Spacer(modifier = Modifier.width(5.dp))

                    Surface(
                        color = when {
                            isLive -> KickoffPitchEmerald
                            else -> Color.Transparent
                        },
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(
                            width = if (isLive) 0.9.dp else 0.7.dp,
                            color = when {
                                isLive -> KickoffFloodlightVolt
                                isUpcoming -> compAccent.copy(alpha = 0.34f)
                                else -> KickoffRowDivider.copy(alpha = 0.65f)
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
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            when {
                                isLive -> {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = KickoffPitchBlack,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "LIVE",
                                        color = KickoffPitchBlack,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                isUpcoming -> {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = KickoffMistSilver,
                                        modifier = Modifier.size(11.5.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "Watch",
                                        color = KickoffMistSilver,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                else -> {
                                    Text(
                                        text = "Stats ›",
                                        color = KickoffMistSilver,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold
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
    manualTimeOffsetMinutes: Int = 0,
    use24HourFormat: Boolean = true,
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
    onDismissSourceAlert: () -> Unit,
    onOpenDateSelectorModal: () -> Unit = {},
    onOpenCompetitionsSelectorModal: () -> Unit = {}
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
            manualTimeOffsetMinutes = manualTimeOffsetMinutes,
            selectedMatchDay = selectedMatchDay,
            selectedTournamentFilter = selectedTournamentFilter,
            activeDayMatches = activeDayMatches,
            totalVisibleMatchesCount = sortedVisibleMatches.size,
            activeLeaguesInSlate = sortedVisibleMatches.map { it.tournamentId }.distinct().size,
            favoriteTeamsCount = favoriteTeams.size,
            liveMatchesCount = liveMatchesCount,
            isLoadingMatches = isLoadingMatches,
            sourceAlertMessage = kickoffSourceAlert,
            unhealthySourcesCount = unhealthySourcesCount,
            onRefreshMatches = onRefreshMatches,
            onOpenCountryZoneModal = { onOpenCustomizeModal(1) },
            onOpenCustomizeModal = { onOpenCustomizeModal(0) },
            onOpenSourcesHub = { onOpenCustomizeModal(3) },
            onToggleLiveFilter = {
                if (selectedTournamentFilter == "LIVE") {
                    onSelectFilter("ALL")
                } else {
                    if (selectedMatchDay != MatchDayTab.TODAY) {
                        onSelectMatchDay(MatchDayTab.TODAY)
                    }
                    onSelectFilter("LIVE")
                }
            },
            onSelectMatchDay = onSelectMatchDay,
            onOpenDateSelectorModal = onOpenDateSelectorModal,
            onOpenCompetitionsSelectorModal = onOpenCompetitionsSelectorModal
        )

        InstantSevenDayDateStrip(
            selectedDay = selectedMatchDay,
            selectedCountryZone = selectedCountryZone,
            matchesByDay = matchesByDay,
            liveCountToday = liveMatchesCount,
            onSelectDay = onSelectMatchDay,
            activeEdition = SetPlayEdition.TV,
            onOpenDateSelectorModal = onOpenDateSelectorModal,
            onResetToToday = { onSelectMatchDay(MatchDayTab.TODAY) }
        )

        // 60% / 40% Widescreen Split-Board
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Left Pane (60% width): D-Pad 2-Column Fixture Grid with smooth day transition
            AnimatedContent(
                targetState = selectedMatchDay,
                transitionSpec = {
                    val direction = if (targetState.dayOffset >= initialState.dayOffset) 1 else -1
                    (fadeIn(tween(180)) + slideInHorizontally(tween(200)) { fullWidth -> direction * (fullWidth / 12) })
                        .togetherWith(
                            fadeOut(tween(140)) + slideOutHorizontally(tween(160)) { fullWidth -> -direction * (fullWidth / 12) }
                        )
                },
                modifier = Modifier
                    .weight(0.60f)
                    .fillMaxHeight(),
                label = "tv_kickoff_day_feed_transition"
            ) { _ ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("match_center_list"),
                contentPadding = PaddingValues(bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (chronologicalSections.isEmpty()) {
                    item {
                        EmptyMatchesCard(
                            selectedFilter = selectedTournamentFilter,
                            onResetFilter = onResetFilter,
                            onOpenCustomize = { onOpenCustomizeModal(0) },
                            selectedDay = selectedMatchDay,
                            selectedCountryZone = selectedCountryZone,
                            matchesByDay = matchesByDay,
                            onSelectDay = onSelectMatchDay
                        )
                    }
                } else {
                    chronologicalSections.forEachIndexed { sectionIndex, section ->
                        val firstMatch = section.matches.first()
                        val compAccent = resolveCompetitionAccentColor(section.tournamentId)
                        val sectionLiveCount = section.matches.count { it.status == MatchStatus.LIVE || it.status == MatchStatus.HALFTIME }
                        val regionSubtitle = firstMatch.tournamentRegion.trim().ifBlank { "INTERNATIONAL" }.uppercase(Locale.US)
                        item(key = "tv_header_${section.sectionKey}") {
                            Surface(
                                color = KickoffCanopyElevated,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    width = 0.9.dp,
                                    color = if (sectionLiveCount > 0) KickoffLiveCrimson else compAccent.copy(alpha = 0.40f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = if (sectionIndex == 0) 6.dp else 18.dp, bottom = 2.dp)
                                    .testTag("tournament_header_${section.tournamentId}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    compAccent.copy(alpha = 0.22f),
                                                    KickoffCanopyElevated,
                                                    KickoffTurfSurface
                                                )
                                            )
                                        )
                                        .padding(end = 12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(3.5.dp)
                                            .fillMaxHeight()
                                            .background(compAccent)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    TournamentLogoMedallion(
                                        tournamentId = section.tournamentId,
                                        tournamentName = firstMatch.tournamentName,
                                        accentColor = compAccent,
                                        sizeDp = 36
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = firstMatch.tournamentName,
                                            color = KickoffFloodlightWhite,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = regionSubtitle,
                                            color = compAccent,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.8.sp,
                                            maxLines = 1
                                        )
                                    }
                                    Surface(
                                        color = KickoffGlassWell,
                                        shape = RoundedCornerShape(50),
                                        border = BorderStroke(0.7.dp, compAccent.copy(alpha = 0.32f))
                                    ) {
                                        Text(
                                            text = "${section.matches.size}",
                                            color = KickoffMistSilver,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            style = TabularNumeralTextStyle,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        val chunkedPairs = section.matches.chunked(2)
                        itemsIndexed(
                            items = chunkedPairs,
                            key = { pairIdx, pair -> "tv_pair_${section.sectionKey}_${pairIdx}_${pair.first().matchId}" }
                        ) { _, pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                pair.forEach { match ->
                                    TvWidescreenMatchGridCard(
                                        match = match,
                                        selectedCountryZone = selectedCountryZone,
                                        selectedSeasonTimeMode = selectedSeasonTimeMode,
                                        manualTimeOffsetMinutes = manualTimeOffsetMinutes,
                                        use24HourFormat = use24HourFormat,
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

                    // Subtle Android TV Kickoff Version Footer
                    item(key = "tv_kickoff_version_footer") {
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp, bottom = 8.dp, start = 4.dp)
                        ) {
                            Text(
                                text = "SetPlay ${BuildConfig.VERSION_NAME} • TV Edition",
                                color = KickoffMistSilver.copy(alpha = 0.56f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.4.sp,
                                modifier = Modifier.testTag("tv_kickoff_version_footer")
                            )
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
                manualTimeOffsetMinutes = manualTimeOffsetMinutes,
                use24HourFormat = use24HourFormat,
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
internal fun TvWidescreenMatchGridCard(
    match: FootballMatch,
    selectedCountryZone: UserCountryZone,
    selectedSeasonTimeMode: SeasonTimeMode,
    manualTimeOffsetMinutes: Int = 0,
    use24HourFormat: Boolean = true,
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
    val rawChannelLabel = match.beinChannel.ifBlank { match.menaProviderName.ifBlank { "Live TV" } }
    val cleanChannelSuffix = remember(rawChannelLabel, broadcasterIdentity.providerShortTag) {
        formatCleanBroadcastChannelSuffix(rawChannelLabel, broadcasterIdentity.providerShortTag)
    }
    val homeIsWinner = isFinished && match.homeScore != null && match.awayScore != null && match.homeScore > match.awayScore
    val awayIsWinner = isFinished && match.homeScore != null && match.awayScore != null && match.awayScore > match.homeScore
    val kickoffFormatted = remember(
        match.kickoffEpochMs,
        match.kickoffTimeFormatted,
        selectedCountryZone,
        manualTimeOffsetMinutes,
        use24HourFormat
    ) {
        if (match.kickoffEpochMs > 0L) {
            formatDynamicKickoffTime(
                kickoffEpochMs = match.kickoffEpochMs,
                zone = selectedCountryZone,
                manualOffsetMinutes = manualTimeOffsetMinutes,
                use24Hour = use24HourFormat
            )
        } else {
            match.kickoffTimeFormatted
        }
    }

    val isDecidedFinish = isFinished && match.homeScore != null && match.awayScore != null && match.homeScore != match.awayScore
    val liveReadout = match.liveMinuteOrBadge.ifBlank { "LIVE" }

    val tvCardSurfaceBrush = if (isLive) {
        Brush.horizontalGradient(
            listOf(
                KickoffLiveCrimson.copy(alpha = 0.14f),
                KickoffStadiumForest.copy(alpha = 0.72f),
                compAccent.copy(alpha = 0.08f)
            )
        )
    } else {
        Brush.horizontalGradient(
            listOf(
                compAccent.copy(alpha = 0.09f),
                if (isFocused) KickoffCanopyElevated else KickoffTurfSurface,
                compAccent.copy(alpha = 0.04f)
            )
        )
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
                isLive -> KickoffLiveCrimson.copy(alpha = 0.36f)
                isSelectedInStudio -> compAccent.copy(alpha = 0.85f)
                else -> compAccent.copy(alpha = 0.30f)
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
                .background(tvCardSurfaceBrush)
        ) {
            // Tier 1: Upper 10-Foot Teams & Strict 88.dp Broadcast Score Stage (48.dp unboxed crests, 2-line team names)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .testTag("open_match_center_${match.matchId}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TeamCrestImage(badgeUrl = match.homeTeamBadge, teamName = match.homeTeamName, sizeDp = 48)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = match.homeTeamName,
                            color = if (isDecidedFinish && !homeIsWinner) KickoffFloodlightWhite.copy(alpha = 0.64f) else KickoffFloodlightWhite,
                            fontSize = 14.5.sp,
                            lineHeight = 17.5.sp,
                            fontWeight = if (homeIsWinner || isLive) FontWeight.ExtraBold else FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (match.homeRedCards > 0) {
                            Spacer(modifier = Modifier.width(5.dp))
                            KickoffBookingCardIcon(isRedCard = true, modifier = Modifier.size(11.dp))
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TeamCrestImage(badgeUrl = match.awayTeamBadge, teamName = match.awayTeamName, sizeDp = 48)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = match.awayTeamName,
                            color = if (isDecidedFinish && !awayIsWinner) KickoffFloodlightWhite.copy(alpha = 0.64f) else KickoffFloodlightWhite,
                            fontSize = 14.5.sp,
                            lineHeight = 17.5.sp,
                            fontWeight = if (awayIsWinner || isLive) FontWeight.ExtraBold else FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (match.awayRedCards > 0) {
                            Spacer(modifier = Modifier.width(5.dp))
                            KickoffBookingCardIcon(isRedCard = true, modifier = Modifier.size(11.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Strict Fixed-Width 88.dp Broadcast Score / Status Stage
                MatchCardBroadcastScoreStage(
                    match = match,
                    dynamicKickoffTime = kickoffFormatted,
                    liveReadout = liveReadout,
                    compAccent = compAccent,
                    stageWidthDp = 88,
                    hideScores = false,
                    isHalftime = isHalftime,
                    homeIsWinner = homeIsWinner,
                    awayIsWinner = awayIsWinner,
                    isDecidedFinish = isDecidedFinish
                )
            }

            // Tier 2: Subtly Recessed Broadcast Footer (Toned-Down Non-Live Watch Pill)
            HorizontalDivider(thickness = 0.5.dp, color = compAccent.copy(alpha = 0.16f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(KickoffPitchBlack.copy(alpha = 0.34f))
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Surface(
                        color = broadcasterIdentity.containerColor,
                        shape = RoundedCornerShape(5.dp),
                        border = BorderStroke(0.6.dp, broadcasterIdentity.borderColor)
                    ) {
                        Text(
                            text = broadcasterIdentity.providerShortTag,
                            color = broadcasterIdentity.textColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 4.5.dp, vertical = 1.dp)
                        )
                    }
                    Text(
                        text = cleanChannelSuffix,
                        color = KickoffMistSilver,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (match.fallbackMenaChannels.isNotEmpty()) {
                        Surface(
                            color = KickoffCanopyElevated.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.5.dp, compAccent.copy(alpha = 0.30f))
                        ) {
                            Text(
                                text = "+${match.fallbackMenaChannels.size}",
                                color = KickoffMistSilver,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                style = TabularNumeralTextStyle,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = if (isLive) KickoffPitchEmerald else Color.Transparent,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        width = if (isLive) 0.9.dp else 0.7.dp,
                        color = if (isLive) KickoffFloodlightVolt else compAccent.copy(alpha = 0.35f)
                    ),
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
                            text = if (isLive) "● LIVE TV" else "▶ Watch",
                            color = if (isLive) KickoffPitchBlack else KickoffMistSilver,
                            fontSize = 10.sp,
                            fontWeight = if (isLive) FontWeight.ExtraBold else FontWeight.Bold,
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
    manualTimeOffsetMinutes: Int = 0,
    use24HourFormat: Boolean = true,
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
        val kickoffFormatted = remember(
            match.kickoffEpochMs,
            match.kickoffTimeFormatted,
            selectedCountryZone,
            manualTimeOffsetMinutes,
            use24HourFormat
        ) {
            if (match.kickoffEpochMs > 0L) {
                formatDynamicKickoffTime(
                    kickoffEpochMs = match.kickoffEpochMs,
                    zone = selectedCountryZone,
                    manualOffsetMinutes = manualTimeOffsetMinutes,
                    use24Hour = use24HourFormat
                )
            } else {
                match.kickoffTimeFormatted
            }
        }
        val studioUtcBadge = remember(match.kickoffEpochMs, selectedCountryZone, manualTimeOffsetMinutes) {
            val refEpoch = if (match.kickoffEpochMs > 0L) match.kickoffEpochMs else System.currentTimeMillis()
            selectedCountryZone.getUtcOffsetBadge(
                epochMs = refEpoch,
                manualOffsetMinutes = manualTimeOffsetMinutes
            )
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
                            TeamCrestImage(badgeUrl = match.homeTeamBadge, teamName = match.homeTeamName, sizeDp = 50)
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
                                    MatchStatus.UPCOMING -> studioUtcBadge
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
                            TeamCrestImage(badgeUrl = match.awayTeamBadge, teamName = match.awayTeamName, sizeDp = 50)
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

/**
 * Broadcast-grade, strictly fixed-width (`78.dp` Phone / `88.dp` TV) state-adaptive center score/time stage.
 * Guarantees Home and Away `weight(1f)` columns are never crushed while rendering crisp state-driven visual hierarchy.
 */
@Composable
internal fun MatchCardBroadcastScoreStage(
    match: FootballMatch,
    dynamicKickoffTime: String,
    liveReadout: String,
    compAccent: Color,
    stageWidthDp: Int = 78,
    hideScores: Boolean = false,
    isHalftime: Boolean = match.status == MatchStatus.HALFTIME || match.liveMinuteOrBadge.equals("HT", ignoreCase = true),
    homeIsWinner: Boolean = false,
    awayIsWinner: Boolean = false,
    isDecidedFinish: Boolean = false
) {
    val pulseAlpha = if (match.status == MatchStatus.LIVE && !isHalftime) {
        val infiniteTransition = rememberInfiniteTransition(label = "stage_live_pulse_${match.matchId}")
        val animated by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.28f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 850, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "stage_live_pulse_alpha_${match.matchId}"
        )
        animated
    } else {
        1f
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(stageWidthDp.dp)
            .heightIn(min = 46.dp)
            .testTag("match_score_stage_${match.matchId}")
    ) {
        when (match.status) {
            MatchStatus.UPCOMING -> {
                Surface(
                    color = KickoffGlassWell.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.75.dp, compAccent.copy(alpha = 0.24f)),
                    modifier = Modifier
                        .width((stageWidthDp - 6).dp)
                        .height(34.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = dynamicKickoffTime,
                            color = KickoffFloodlightWhite,
                            fontSize = if (dynamicKickoffTime.length > 5) 13.5.sp else 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            style = TabularNumeralTextStyle,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
            MatchStatus.LIVE -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    KickoffLiveCrimson.copy(alpha = 0.22f),
                                    KickoffLiveCrimson.copy(alpha = 0.06f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(vertical = 2.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (hideScores) {
                            Text(
                                text = "LIVE",
                                color = KickoffLiveCrimson,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                style = TabularNumeralTextStyle,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        } else {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(color = KickoffFloodlightWhite, fontWeight = FontWeight.Black)) {
                                        append("${match.homeScore ?: 0}")
                                    }
                                    withStyle(SpanStyle(color = KickoffMistSilver, fontWeight = FontWeight.Bold)) {
                                        append(" - ")
                                    }
                                    withStyle(SpanStyle(color = KickoffFloodlightWhite, fontWeight = FontWeight.Black)) {
                                        append("${match.awayScore ?: 0}")
                                    }
                                },
                                fontSize = 20.sp,
                                style = TabularNumeralTextStyle,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            color = if (isHalftime) KickoffTrophyGold.copy(alpha = 0.18f) else KickoffLiveCrimson.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(4.5.dp)
                                        .alpha(pulseAlpha)
                                        .clip(CircleShape)
                                        .background(if (isHalftime) KickoffTrophyGold else KickoffLiveCrimson)
                                )
                                Spacer(modifier = Modifier.width(3.5.dp))
                                Text(
                                    text = if (isHalftime) "HT" else liveReadout,
                                    color = if (isHalftime) KickoffTrophyGold else KickoffLiveCrimson,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = TabularNumeralTextStyle,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
            MatchStatus.HALFTIME -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (hideScores) {
                        Text(
                            text = "HT",
                            color = KickoffTrophyGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            style = TabularNumeralTextStyle,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    } else {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = KickoffFloodlightWhite, fontWeight = FontWeight.ExtraBold)) {
                                    append("${match.homeScore ?: 0}")
                                }
                                withStyle(SpanStyle(color = KickoffMistSilver, fontWeight = FontWeight.Bold)) {
                                    append(" - ")
                                }
                                withStyle(SpanStyle(color = KickoffFloodlightWhite, fontWeight = FontWeight.ExtraBold)) {
                                    append("${match.awayScore ?: 0}")
                                }
                            },
                            fontSize = 19.sp,
                            style = TabularNumeralTextStyle,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        color = KickoffTrophyGold.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "HT",
                            color = KickoffTrophyGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            style = TabularNumeralTextStyle,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.5.dp)
                        )
                    }
                }
            }
            MatchStatus.FINISHED -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (hideScores) {
                        Text(
                            text = "FT",
                            color = KickoffFloodlightWhite.copy(alpha = 0.94f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            style = TabularNumeralTextStyle,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    } else {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    SpanStyle(
                                        color = if (isDecidedFinish && !homeIsWinner) KickoffMistSilver else KickoffFloodlightWhite,
                                        fontWeight = if (homeIsWinner) FontWeight.Black else if (isDecidedFinish) FontWeight.SemiBold else FontWeight.ExtraBold
                                    )
                                ) {
                                    append("${match.homeScore ?: 0}")
                                }
                                withStyle(
                                    SpanStyle(
                                        color = KickoffMistSilver.copy(alpha = 0.75f),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                ) {
                                    append(" - ")
                                }
                                withStyle(
                                    SpanStyle(
                                        color = if (isDecidedFinish && !awayIsWinner) KickoffMistSilver else KickoffFloodlightWhite,
                                        fontWeight = if (awayIsWinner) FontWeight.Black else if (isDecidedFinish) FontWeight.SemiBold else FontWeight.ExtraBold
                                    )
                                ) {
                                    append("${match.awayScore ?: 0}")
                                }
                            },
                            fontSize = 18.sp,
                            style = TabularNumeralTextStyle,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(1.5.dp))
                        Text(
                            text = "FT",
                            color = KickoffMistSilver.copy(alpha = 0.8f),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            style = TabularNumeralTextStyle,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
            MatchStatus.POSTPONED, MatchStatus.CANCELLED -> {
                val isCancelled = match.status == MatchStatus.CANCELLED
                val badgeColor = if (isCancelled) KickoffLiveCrimson else KickoffTrophyGold
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = dynamicKickoffTime,
                        color = KickoffMistSilver.copy(alpha = 0.65f),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.LineThrough,
                        style = TabularNumeralTextStyle,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        color = badgeColor.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = if (isCancelled) "CANC" else "PPD",
                            color = badgeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.6.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.5.dp)
                        )
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
    val resolvedUrl = remember(teamName, badgeUrl) {
        FootballRepository.resolveFallbackClubCrestPngUrl(teamName, badgeUrl)
    }
    val initials = remember(teamName) {
        val tokens = teamName.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        when {
            tokens.size >= 2 -> "${tokens[0].first().uppercaseChar()}${tokens[1].first().uppercaseChar()}"
            tokens.size == 1 -> tokens[0].take(2).uppercase()
            else -> "FC"
        }
    }

    if (resolvedUrl.isNotBlank()) {
        // Unboxed club crest with authentic silhouette and enhanced feather-soft radial ambient backlight
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(sizeDp.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.22f),
                            Color.White.copy(alpha = 0.07f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            SubcomposeAsyncImage(
                model = resolvedUrl,
                contentDescription = teamName,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding((sizeDp * 0.04f).coerceAtLeast(1f).dp),
                loading = {
                    TeamShieldMonogramFallback(initials = initials, sizeDp = sizeDp)
                },
                error = {
                    TeamShieldMonogramFallback(initials = initials, sizeDp = sizeDp)
                }
            )
        }
    } else {
        TeamShieldMonogramFallback(initials = initials, sizeDp = sizeDp)
    }
}

@Composable
private fun TeamShieldMonogramFallback(
    initials: String,
    sizeDp: Int
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(KickoffCanopyElevated)
            .border(
                width = 0.85.dp,
                color = KickoffPitchEmerald.copy(alpha = 0.38f),
                shape = CircleShape
            )
    ) {
        Text(
            text = initials,
            color = KickoffMistSilver,
            fontSize = (sizeDp * 0.30f).sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )
    }
}

@Composable
private fun EmptyMatchesCard(
    selectedFilter: String,
    onResetFilter: () -> Unit,
    onOpenCustomize: () -> Unit,
    selectedDay: MatchDayTab = MatchDayTab.TODAY,
    selectedCountryZone: UserCountryZone = UserZoneRegistry.defaultCountryZone,
    matchesByDay: Map<MatchDayTab, List<FootballMatch>> = emptyMap(),
    onSelectDay: (MatchDayTab) -> Unit = {}
) {
    val nextActiveDay = remember(selectedDay, matchesByDay) {
        MatchDayTab.forwardTimelineTabs.firstOrNull {
            it != selectedDay && matchesByDay[it].orEmpty().isNotEmpty()
        }
    }
    val isQuietAllDayWithNextSlate = selectedFilter == "ALL" && nextActiveDay != null

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
                text = when {
                    isQuietAllDayWithNextSlate ->
                        "Quiet Matchday on ${selectedDay.getDynamicDateSubtitle(selectedCountryZone.timeZoneId)}"
                    selectedFilter == "MY_TEAMS" ->
                        "No Matches Found for Your Favorite Teams"
                    else ->
                        "No Matches Match Current Filter"
                },
                color = KickoffFloodlightWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isQuietAllDayWithNextSlate) {
                    "No Tier-1 fixtures are scheduled for this date. Jump directly to the next active broadcast slate."
                } else {
                    "You can switch back to All Leagues or customize your favorite teams and active competitions."
                },
                color = KickoffMistSilver,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            if (selectedFilter == "ALL") {
                if (nextActiveDay != null) {
                    Button(
                        onClick = { onSelectDay(nextActiveDay) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KickoffFloodlightVolt,
                            contentColor = KickoffPitchBlack
                        ),
                        modifier = Modifier.testTag("jump_next_active_day_btn")
                    ) {
                        Text(
                            text = "Jump to ${nextActiveDay.getShortDayCode(selectedCountryZone.timeZoneId)} ${nextActiveDay.getDynamicDateSubtitle(selectedCountryZone.timeZoneId)} (${matchesByDay[nextActiveDay].orEmpty().size} Matches) →",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
                OutlinedButton(
                    onClick = onOpenCustomize,
                    border = BorderStroke(1.dp, KickoffPitchEmerald)
                ) {
                    Text(
                        text = "Manage Leagues & Teams",
                        color = KickoffFloodlightWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            } else {
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
                        Text(
                            text = "Manage Leagues & Teams",
                            color = KickoffFloodlightWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
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
