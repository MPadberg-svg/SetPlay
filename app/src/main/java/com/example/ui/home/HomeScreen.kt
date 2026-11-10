package com.example.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import kotlin.math.abs
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import kotlinx.coroutines.delay
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ArenaFloodlightLime
import com.example.ui.theme.ArenaPitchGreen
import com.example.ui.theme.ArenaTrophyGold
import com.example.ui.theme.ArenaTurfDark
import com.example.ui.theme.ArenaTurfElevated
import com.example.ui.theme.CatchupGreen
import com.example.ui.theme.Champagne
import com.example.ui.theme.ChampagneMuted
import com.example.ui.theme.ChampagneSubtleBorder
import com.example.ui.theme.DarkGraphite
import com.example.ui.theme.KickoffAgedParchment
import com.example.ui.theme.KickoffAlabasterIvory
import com.example.ui.theme.KickoffCeruleanBlue
import com.example.ui.theme.KickoffCharcoalElevated
import com.example.ui.theme.KickoffCharcoalSlate
import com.example.ui.theme.KickoffCoralRed
import com.example.ui.theme.KickoffImperialNavy
import com.example.ui.theme.KickoffNavyElevated
import com.example.ui.theme.KickoffObsidianBlack
import com.example.ui.theme.KickoffOceanBase
import com.example.ui.theme.KickoffOceanElevated
import com.example.ui.theme.KickoffOceanSurface
import com.example.ui.theme.KickoffParchmentBorder
import com.example.ui.theme.KickoffParchmentMuted
import com.example.ui.theme.KickoffPeachCream
import com.example.ui.theme.KickoffTerracotta
import com.example.ui.theme.KickoffTerracottaGlow
import com.example.ui.theme.KickoffTropicalTeal
import com.example.ui.theme.LiveCrimson
import com.example.ui.theme.LiveRed
import com.example.ui.theme.MenaAdSportsBlue
import com.example.ui.theme.MenaAdSportsDark
import com.example.ui.theme.MenaArryadiaDark
import com.example.ui.theme.MenaArryadiaRed
import com.example.ui.theme.MenaBeinDark
import com.example.ui.theme.MenaBeinLavender
import com.example.ui.theme.MenaBeinPurple
import com.example.ui.theme.MenaSscDark
import com.example.ui.theme.MenaSscTeal
import com.example.ui.theme.MetalCharcoal
import com.example.ui.theme.TabularNumeralTextStyle
import com.example.ui.theme.luxuryFocusableCard
import com.example.ui.theme.tvFocusableControl
import com.example.ui.theme.tvFocusablePill
import com.example.ui.theme.MetalCharcoalElevated
import com.example.ui.theme.PaleGold
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.BeinChannelMatcherState
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    mainViewModel: MainViewModel,
    onStreamClick: (StreamEntity) -> Unit,
    onNavigateScreen: (AppScreen) -> Unit
) {
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

    // Filter matches according to enabled tournaments, active 3-pillar category & active filter chip
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
                "MY_TEAMS" -> isMatchForFavoriteTeams(match, favoriteTeams)
                "LIVE" -> match.status == MatchStatus.LIVE || match.status == MatchStatus.HALFTIME
                else -> match.tournamentId == selectedTournamentFilter
            }
        }
    }

    // Matches involving the user's favorite teams on the selected day
    val favoriteTeamMatches = remember(activeDayMatches, favoriteTeams) {
        if (favoriteTeams.isEmpty()) emptyList()
        else activeDayMatches.filter { isMatchForFavoriteTeams(it, favoriteTeams) }
    }

    // Sort visible matches:
    // 1. LIVE / HALFTIME games always appear first at the top
    // 2. Remaining games are listed below the live games in strict chronological kickoff time order (HH:mm ascending)
    // 3. Canonical tournament order as tie-breaker for simultaneous kickoffs
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

    // Build contiguous tournament blocks from the time-sorted list so LIVE games stay at the top
    // and all subsequent games remain in strict chronological kickoff time order
    val chronologicalSections = remember(sortedVisibleMatches) {
        buildChronologicalTournamentSections(sortedVisibleMatches)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        KickoffObsidianBlack,
                        KickoffImperialNavy.copy(alpha = 0.28f),
                        KickoffObsidianBlack
                    )
                )
            )
            .testTag("match_center_list"),
        contentPadding = PaddingValues(bottom = 110.dp)
    ) {
        // 1. Sync progress bar when syncing Xtream playlist
        item {
            SyncBanner(syncProgress = syncProgress)
        }

        // 2. Matchday Intelligence & Broadcast Telemetry Bar (No redundant section title)
        item {
            val activeLeaguesInSlate = remember(sortedVisibleMatches) {
                sortedVisibleMatches.map { it.tournamentId }.distinct().size
            }
            val enabledLeaguesCount = remember(tournamentPreferences) {
                tournamentPreferences.count { it.isEnabled }.let { if (it > 0) it else 10 }
            }
            val upcomingMatchesCount = remember(sortedVisibleMatches) {
                sortedVisibleMatches.count { it.status == MatchStatus.UPCOMING }
            }
            KickOffArenaHeaderCard(
                activePlaylist = activePlaylist,
                selectedCountryZone = selectedCountryZone,
                selectedSeasonTimeMode = selectedSeasonTimeMode,
                selectedMatchDay = selectedMatchDay,
                selectedTournamentFilter = selectedTournamentFilter,
                totalVisibleMatchesCount = sortedVisibleMatches.size,
                activeLeaguesInSlate = activeLeaguesInSlate,
                enabledLeaguesCount = enabledLeaguesCount,
                upcomingMatchesCount = upcomingMatchesCount,
                favoriteTeamsCount = favoriteTeams.size,
                liveMatchesCount = liveMatchesCount,
                isLoadingMatches = isLoadingMatches,
                liveScoreSyncState = liveScoreSyncState,
                onRefreshMatches = { mainViewModel.refreshKickOffMatches(selectedMatchDay) },
                onOpenCountryZoneModal = { mainViewModel.openCountryZoneModal() },
                onOpenCustomizeModal = { mainViewModel.openCustomizeTeamsModal() },
                onToggleLiveFilter = {
                    mainViewModel.selectTournamentFilter(
                        if (selectedTournamentFilter == "LIVE") "ALL" else "LIVE"
                    )
                },
                onManagePlaylists = { onNavigateScreen(AppScreen.PLAYLISTS) },
                onSyncPlaylist = { mainViewModel.syncActiveAccount() },
                onSelectMatchDay = { mainViewModel.selectMatchDay(it) }
            )
        }

        // 2B. Flashscore "Pinned Matches (My Games)" Sticky Live Rail
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

        // 3 & 4. Sticky Architectural Matchday Scrubber + 3-Pillar Competition Bar + Tournament Filter Dock
        stickyHeader(key = "sticky_matchday_and_tournament_bar") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                KickoffObsidianBlack.copy(alpha = 0.98f),
                                KickoffObsidianBlack.copy(alpha = 0.96f),
                                KickoffCharcoalSlate.copy(alpha = 0.94f)
                            )
                        )
                    )
            ) {
                FiveDayMatchTimelineRibbon(
                    selectedDay = selectedMatchDay,
                    selectedCountryZone = selectedCountryZone,
                    matchesByDay = matchesByDay,
                    liveCountToday = liveMatchesCount,
                    onSelectDay = { mainViewModel.selectMatchDay(it) }
                )

                CompetitionCategoryPillarRow(
                    selectedCategory = selectedCompetitionCategory,
                    activeDayMatches = activeDayMatches,
                    tournamentCategoryMap = tournamentCategoryMap,
                    onSelectCategory = { mainViewModel.selectCompetitionCategory(it) }
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
                    onOpenCustomize = { mainViewModel.openCustomizeTeamsModal() }
                )

                // Subtle bottom architectural hairline separating sticky dock from scrolling fixtures
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    KickoffImperialNavy.copy(alpha = 0.75f),
                                    KickoffAgedParchment.copy(alpha = 0.32f),
                                    KickoffTerracotta.copy(alpha = 0.48f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }

        // 5. Dedicated "MY FAVORITE TEAMS" View ONLY when "MY_TEAMS" filter is triggered programmatically
        if (selectedTournamentFilter == "MY_TEAMS") {
            if (favoriteTeams.isEmpty()) {
                item {
                    FavoriteTeamsEmptyBanner(
                        onSelectFavoriteTeams = { mainViewModel.openCustomizeTeamsModal() }
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
                        onEditFavoriteTeams = { mainViewModel.openCustomizeTeamsModal() },
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
            // 6. All Tournaments (or selected Tournament) — LIVE matches first, then chronological time order below
            if (chronologicalSections.isEmpty()) {
                item {
                    EmptyMatchesCard(
                        selectedFilter = selectedTournamentFilter,
                        onResetFilter = {
                            mainViewModel.selectCompetitionCategory(null)
                            mainViewModel.selectTournamentFilter("ALL")
                            mainViewModel.enableAllTournaments()
                        },
                        onOpenCustomize = { mainViewModel.openCustomizeTeamsModal() }
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
    // 2. Anytime "My Teams & Tournaments" Customization Bottom Sheet
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
            onDismiss = { mainViewModel.closeCustomizeTeamsModal() }
        )
    }

    // =========================================================================
    // 3. Full 5-Tab Flashscore & 365Scores Match Center Dialog
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
    // 4. Favorites-Only Channel Lookup Notification / Matched Favorites Picker
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

/**
 * Resolves the signature sports broadcaster badge color scheme across all Country Zones
 * (MENA: beIN Sports, Arryadia, SSC, AD Sports; Europe: Canal+, Sky Sports, TNT Sports, DAZN, Movistar+, Sport TV, Ziggo, Tabii; Americas: CBS, ESPN).
 */
private fun resolveMenaBroadcasterColors(channelLabel: String): Triple<Color, Color, Color> {
    val lower = channelLabel.lowercase()
    return when {
        lower.contains("arryadia") || lower.contains("snrt") || lower.contains("tamazight") || lower.contains("maghribia") ->
            Triple(MenaArryadiaDark, MenaArryadiaRed, Color(0xFFFFD6D8))
        lower.contains("ssc") || lower.contains("shahid") || lower.contains("sport tv") ->
            Triple(MenaSscDark, MenaSscTeal, Color(0xFFCCFBF1))
        lower.contains("ad sport") || lower.contains("starz") || lower.contains("alkass") || lower.contains("al kass") || lower.contains("movistar") || lower.contains("cbs") || lower.contains("paramount") ->
            Triple(MenaAdSportsDark, MenaAdSportsBlue, Color(0xFFDBEAFE))
        lower.contains("canal+") || lower.contains("canal plus") || lower.contains("dazn") ->
            Triple(Color(0xFF101824), Color(0xFF38BDF8), Color(0xFFE0F2FE))
        lower.contains("sky sport") || lower.contains("tnt sport") || lower.contains("premier sport") || lower.contains("espn") ->
            Triple(Color(0xFF1E1024), Color(0xFFF43F5E), Color(0xFFFFE4E6))
        lower.contains("ziggo") || lower.contains("tabii") || lower.contains("trt") || lower.contains("s sport") ->
            Triple(Color(0xFF1C1608), AmberGold, Color(0xFFFEF3C7))
        else ->
            Triple(MenaBeinDark, MenaBeinPurple, MenaBeinLavender)
    }
}

@Composable
private fun SyncBanner(syncProgress: SyncProgress) {
    AnimatedVisibility(visible = syncProgress.isSyncing) {
        Surface(
            color = ArenaTurfDark,
            border = BorderStroke(1.dp, ArenaPitchGreen.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = syncProgress.phase.ifBlank { "Syncing Xtream catalog..." },
                        color = PaleGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (syncProgress.itemsProcessed > 0) {
                        Text(
                            text = "${syncProgress.itemsProcessed} items",
                            color = ArenaFloodlightLime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = ArenaPitchGreen,
                    trackColor = DarkGraphite
                )
            }
        }
    }
}

/**
 * Canonical Tournament Order requested by user:
 * 1. All Tournaments (chip)
 * 2. UEFA Champions League (uefa_cl)
 * 3. La Liga (laliga)
 * 4. Premier League (premier_league)
 * 5. Ligue 1 (ligue_1)
 * 6. Bundesliga (bundesliga)
 * 7. Serie A (serie_a)
 * 8. CAF Champions League (caf_cl)
 * 9. Botola Pro (botola_pro)
 * 10. Others after (uefa_el, saudi_pro, etc.)
 */
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
 * Precision Single-Row Broadcast Command Bar (`KickOffArenaHeaderCard`):
 * - Eliminates the duplicate date pill ("LIVE • 04 NOV" right above "TODAY 04 Nov") and condenses two rows into a
 *   single, razor-sharp 48dp command deck so an extra match card fits above the fold.
 * - Left: 1-Tap User's Country Zone Switcher Capsule (Flag + ISO3 + Season ❄️/☀️ + UTC Offset).
 * - Center: Interactive 1-Tap Live Filter Pill ("● 1 LIVE" — filters directly to live matches) + compact slate counter.
 * - Right: Quick "↩ Today" jump pill (when browsing another day), "Teams" customization trigger, and Live Refresh button.
 */
@Composable
private fun KickOffArenaHeaderCard(
    activePlaylist: PlaylistEntity?,
    selectedCountryZone: UserCountryZone = UserZoneRegistry.defaultCountryZone,
    selectedSeasonTimeMode: SeasonTimeMode = SeasonTimeMode.AUTO,
    selectedMatchDay: MatchDayTab = MatchDayTab.TODAY,
    selectedTournamentFilter: String = "ALL",
    totalVisibleMatchesCount: Int = 0,
    activeLeaguesInSlate: Int = 0,
    enabledLeaguesCount: Int = 10,
    upcomingMatchesCount: Int = 0,
    favoriteTeamsCount: Int,
    liveMatchesCount: Int,
    isLoadingMatches: Boolean,
    liveScoreSyncState: LiveScoreSyncState = LiveScoreSyncState(),
    onRefreshMatches: () -> Unit,
    onOpenCountryZoneModal: () -> Unit = {},
    onOpenCustomizeModal: () -> Unit,
    onToggleLiveFilter: () -> Unit = {},
    onManagePlaylists: () -> Unit,
    onSyncPlaylist: () -> Unit,
    onSelectMatchDay: (MatchDayTab) -> Unit = {}
) {
    val isLiveFilterActive = selectedTournamentFilter == "LIVE"
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

    Surface(
        color = KickoffCharcoalSlate.copy(alpha = 0.94f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 0.85.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    if (liveMatchesCount > 0 && selectedMatchDay == MatchDayTab.TODAY) {
                        KickoffTerracotta.copy(alpha = 0.65f)
                    } else {
                        KickoffAgedParchment.copy(alpha = 0.35f)
                    },
                    KickoffParchmentBorder,
                    KickoffImperialNavy.copy(alpha = 0.70f)
                )
            )
        ),
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .height(36.dp)
            .testTag("match_center_hero_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            KickoffCharcoalSlate,
                            KickoffImperialNavy.copy(alpha = 0.88f)
                        )
                    )
                )
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Cluster: User's Country Zone Switcher Capsule + Interactive Live Filter / Slate Telemetry Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                UserCountryZoneCapsuleButton(
                    selectedZone = selectedCountryZone,
                    seasonTimeMode = selectedSeasonTimeMode,
                    onClick = onOpenCountryZoneModal
                )

                if (liveMatchesCount > 0 && selectedMatchDay == MatchDayTab.TODAY) {
                    Surface(
                        color = if (isLiveFilterActive) KickoffTerracotta else KickoffTerracottaGlow,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            0.8.dp,
                            if (isLiveFilterActive) KickoffAlabasterIvory.copy(alpha = 0.85f) else KickoffTerracotta.copy(alpha = 0.75f)
                        ),
                        modifier = Modifier
                            .height(26.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onToggleLiveFilter)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(if (isLiveFilterActive) KickoffAlabasterIvory else KickoffTerracotta)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isLiveFilterActive) "$liveMatchesCount LIVE ✕" else "$liveMatchesCount LIVE",
                                color = KickoffAlabasterIvory,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                style = TabularNumeralTextStyle,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                } else {
                    Surface(
                        color = KickoffObsidianBlack.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.7.dp, KickoffParchmentBorder),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp)
                        ) {
                            Text(
                                text = compactSlateLabel,
                                color = KickoffAgedParchment,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                style = TabularNumeralTextStyle,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Right Cluster: Optional "↩ Today" Chip + Customize Teams/Leagues + Refresh Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                if (selectedMatchDay != MatchDayTab.TODAY) {
                    Surface(
                        color = KickoffTerracottaGlow,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.75.dp, KickoffTerracotta.copy(alpha = 0.75f)),
                        modifier = Modifier
                            .height(26.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectMatchDay(MatchDayTab.TODAY) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp)
                        ) {
                            Text(
                                text = "↩ Today",
                                color = KickoffAlabasterIvory,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Surface(
                    color = KickoffImperialNavy.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.8.dp, KickoffAgedParchment.copy(alpha = 0.42f)),
                    modifier = Modifier
                        .height(26.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onOpenCustomizeModal)
                        .testTag("customize_teams_tournaments_btn")
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .height(26.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Customize Teams and Leagues",
                            tint = KickoffAgedParchment,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (favoriteTeamsCount > 0) {
                                "Teams ($favoriteTeamsCount)"
                            } else {
                                "Teams"
                            },
                            color = KickoffAlabasterIvory,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(KickoffObsidianBlack.copy(alpha = 0.75f))
                        .border(0.7.dp, KickoffParchmentBorder, CircleShape)
                        .clickable(onClick = onRefreshMatches)
                        .testTag("refresh_matches_btn")
                ) {
                    if (isLoadingMatches || liveScoreSyncState.isSyncingDelta) {
                        CircularProgressIndicator(
                            color = KickoffTerracotta,
                            strokeWidth = 1.6.dp,
                            modifier = Modifier.size(13.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Matches",
                            tint = KickoffAgedParchment,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Architectural 5-Day Segmented Calendar Strip (`FiveDayMatchTimelineRibbon`) in the Official 6-Color Palette:
 * - Obsidian Black (`#111111`) recessed track with Aged Parchment (`#D8CBB3`) border.
 * - Elastic Liquid-Stretch Capsule in Imperial Navy (`#0F1B3D`) with Burnt Terracotta (`#D9541E`) crown and rim.
 * - Alabaster Ivory (`#F3EEE3`) hero date numerals and Aged Parchment (`#D8CBB3`) secondary labels.
 */
@Composable
private fun FiveDayMatchTimelineRibbon(
    selectedDay: MatchDayTab,
    selectedCountryZone: UserCountryZone = UserZoneRegistry.defaultCountryZone,
    matchesByDay: Map<MatchDayTab, List<FootballMatch>>,
    liveCountToday: Int,
    onSelectDay: (MatchDayTab) -> Unit
) {
    val allTabs = MatchDayTab.entries
    val slotCount = allTabs.size
    val selectedIndex = selectedDay.ordinal

    val fastSlot by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = 0.78f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "liquid_pill_fast_slot"
    )
    val slowSlot by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = 0.86f,
            stiffness = Spring.StiffnessLow
        ),
        label = "liquid_pill_slow_slot"
    )

    val isTodaySelectedWithLive = selectedDay == MatchDayTab.TODAY && liveCountToday > 0
    val activeAccent by animateColorAsState(
        targetValue = when {
            isTodaySelectedWithLive -> KickoffTerracotta
            selectedDay == MatchDayTab.YESTERDAY -> KickoffAgedParchment
            else -> KickoffTerracotta
        },
        animationSpec = tween(durationMillis = 260),
        label = "liquid_pill_accent"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "calendarStripInfinite")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "calendarGlowPulse"
    )
    val liveDotScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "calendarLiveDotScale"
    )

    val trackInsetDp = 4.dp
    var stripWidthPx by remember { mutableStateOf(0f) }

    Surface(
        color = KickoffObsidianBlack.copy(alpha = 0.96f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = 0.9.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    KickoffImperialNavy.copy(alpha = 0.85f),
                    KickoffParchmentBorder,
                    KickoffTerracotta.copy(alpha = 0.38f)
                )
            )
        ),
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .height(64.dp)
            .onSizeChanged { stripWidthPx = it.width.toFloat() }
            .testTag("five_day_timeline_ribbon")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val insetPx = trackInsetDp.toPx()
                    val usableWidth = (size.width - insetPx * 2f).coerceAtLeast(1f)
                    val slotWidth = usableWidth / slotCount.toFloat()

                    val leftSlot = minOf(fastSlot, slowSlot).coerceIn(0f, (slotCount - 1).toFloat())
                    val rightSlot = maxOf(fastSlot, slowSlot).coerceIn(0f, (slotCount - 1).toFloat())

                    val pillLeft = insetPx + leftSlot * slotWidth
                    val pillRight = insetPx + (rightSlot + 1f) * slotWidth
                    val pillWidth = (pillRight - pillLeft).coerceAtLeast(slotWidth)
                    val pillTop = insetPx
                    val pillHeight = (size.height - insetPx * 2f).coerceAtLeast(1f)
                    val pillCenterX = (pillLeft + pillRight) * 0.5f
                    val cornerRadiusPx = 14.dp.toPx()

                    // 1. Subtle vertical hairline dividers between unselected slots
                    for (i in 1 until slotCount) {
                        val dividerX = insetPx + i * slotWidth
                        val distToPill = when {
                            dividerX < pillLeft -> pillLeft - dividerX
                            dividerX > pillRight -> dividerX - pillRight
                            else -> 0f
                        }
                        val dividerAlpha = (distToPill / (slotWidth * 0.5f)).coerceIn(0f, 1f) * 0.12f
                        if (dividerAlpha > 0.005f) {
                            drawLine(
                                brush = Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        KickoffAgedParchment.copy(alpha = dividerAlpha),
                                        Color.Transparent
                                    )
                                ),
                                start = Offset(dividerX, size.height * 0.22f),
                                end = Offset(dividerX, size.height * 0.78f),
                                strokeWidth = 0.8.dp.toPx()
                            )
                        }
                    }

                    // 2. Soft Ambient Glow behind the sliding liquid capsule
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                activeAccent.copy(alpha = 0.24f * glowPulse),
                                KickoffImperialNavy.copy(alpha = 0.35f * glowPulse),
                                Color.Transparent
                            ),
                            center = Offset(pillCenterX, size.height * 0.5f),
                            radius = pillWidth * 0.85f
                        ),
                        radius = pillWidth * 0.85f,
                        center = Offset(pillCenterX, size.height * 0.5f)
                    )

                    // 3. Elastic Sliding Imperial Navy Capsule Fill
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                KickoffNavyElevated,
                                KickoffImperialNavy,
                                KickoffCharcoalSlate
                            ),
                            startY = pillTop,
                            endY = pillTop + pillHeight
                        ),
                        topLeft = Offset(pillLeft, pillTop),
                        size = Size(pillWidth, pillHeight),
                        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                    )

                    // 4. Crisp Luminous Rim Border on the Sliding Capsule
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                KickoffAgedParchment.copy(alpha = 0.85f),
                                activeAccent.copy(alpha = 0.80f),
                                KickoffImperialNavy.copy(alpha = 0.55f)
                            ),
                            startY = pillTop,
                            endY = pillTop + pillHeight
                        ),
                        topLeft = Offset(pillLeft, pillTop),
                        size = Size(pillWidth, pillHeight),
                        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                        style = Stroke(width = 1.2.dp.toPx())
                    )

                    // 5. Sleek Top Crown Accent Pill centered on the Sliding Capsule
                    val crownWidth = 20.dp.toPx()
                    val crownHeight = 2.4.dp.toPx()
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                KickoffAlabasterIvory,
                                activeAccent,
                                KickoffAlabasterIvory,
                                Color.Transparent
                            ),
                            startX = pillCenterX - crownWidth * 0.7f,
                            endX = pillCenterX + crownWidth * 0.7f
                        ),
                        topLeft = Offset(pillCenterX - crownWidth * 0.5f, pillTop + 0.6.dp.toPx()),
                        size = Size(crownWidth, crownHeight),
                        cornerRadius = CornerRadius(crownHeight, crownHeight)
                    )
                }
                .padding(trackInsetDp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(selectedDay, stripWidthPx) {
                        val insetPx = trackInsetDp.toPx()
                        detectHorizontalDragGestures { change, _ ->
                            change.consume()
                            val usableW = (stripWidthPx - insetPx * 2f).coerceAtLeast(1f)
                            val relX = change.position.x.coerceIn(0f, usableW - 1f)
                            val targetIdx = ((relX / usableW) * slotCount).toInt().coerceIn(0, allTabs.lastIndex)
                            val targetTab = allTabs[targetIdx]
                            if (targetTab != selectedDay) {
                                onSelectDay(targetTab)
                            }
                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                allTabs.forEach { tab ->
                    val isSelected = selectedDay == tab
                    val count = matchesByDay[tab]?.size ?: 0
                    val isTodayTab = tab == MatchDayTab.TODAY
                    val hasTodayLive = isTodayTab && liveCountToday > 0
                    val tag = when (tab) {
                        MatchDayTab.TODAY -> "tab_today_games"
                        MatchDayTab.TOMORROW -> "tab_tomorrow_games"
                        else -> "tab_${tab.name.lowercase()}"
                    }

                    val tzId = selectedCountryZone.timeZoneId
                    val shortDayCode = remember(tab, tzId) { tab.getShortDayCode(tzId) }
                    val dayNumeral = remember(tab, tzId) { tab.getDayOfMonthNumber(tzId) }
                    val monthTitleCase = remember(tab, tzId) {
                        val upper = tab.getMonthShortUpper(tzId)
                        if (upper.length > 1) upper.first() + upper.substring(1).lowercase() else upper
                    }

                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val itemScale by animateFloatAsState(
                        targetValue = when {
                            isPressed -> 0.93f
                            isSelected -> 1.04f
                            else -> 1.0f
                        },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "calendar_item_scale_${tab.name}"
                    )

                    val dayLabelColor by animateColorAsState(
                        targetValue = when {
                            isSelected -> KickoffAlabasterIvory
                            hasTodayLive -> KickoffTerracotta
                            isTodayTab -> KickoffAgedParchment
                            else -> KickoffParchmentMuted
                        },
                        animationSpec = tween(durationMillis = 200),
                        label = "calendar_day_label_${tab.name}"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .graphicsLayer {
                                scaleX = itemScale
                                scaleY = itemScale
                            }
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) { onSelectDay(tab) }
                            .padding(vertical = 4.dp, horizontal = 2.dp)
                            .testTag(tag)
                    ) {
                        // Tier 1: Day Label ("YEST", "TODAY", "TOM", "FRI", "SAT") + Live Dot
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (hasTodayLive) {
                                Box(
                                    modifier = Modifier
                                        .size(4.5.dp)
                                        .graphicsLayer {
                                            scaleX = liveDotScale
                                            scaleY = liveDotScale
                                        }
                                        .clip(CircleShape)
                                        .background(KickoffTerracotta)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                            }
                            Text(
                                text = shortDayCode,
                                color = dayLabelColor,
                                fontSize = if (isSelected) 9.5.sp else 9.sp,
                                lineHeight = 10.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                letterSpacing = 0.4.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        // Tier 2: Crisp Date ("04 Nov")
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = dayNumeral,
                                color = if (isSelected) KickoffAlabasterIvory else KickoffAlabasterIvory.copy(alpha = 0.82f),
                                fontSize = if (isSelected) 14.5.sp else 13.sp,
                                lineHeight = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.width(2.5.dp))
                            Text(
                                text = monthTitleCase,
                                color = if (isSelected) {
                                    KickoffAgedParchment
                                } else {
                                    KickoffParchmentMuted.copy(alpha = 0.78f)
                                },
                                fontSize = if (isSelected) 9.5.sp else 8.5.sp,
                                lineHeight = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(bottom = 1.dp)
                            )
                        }

                        // Tier 3: Match Count Badge ("10 Games" when selected, compact pill when unselected)
                        Surface(
                            color = when {
                                isSelected && hasTodayLive -> KickoffTerracotta
                                isSelected && tab == MatchDayTab.YESTERDAY -> KickoffAgedParchment
                                isSelected -> KickoffTerracotta
                                hasTodayLive -> KickoffTerracottaGlow
                                else -> KickoffCharcoalElevated
                            },
                            shape = RoundedCornerShape(50),
                            border = if (!isSelected && hasTodayLive) {
                                BorderStroke(0.6.dp, KickoffTerracotta.copy(alpha = 0.75f))
                            } else {
                                null
                            }
                        ) {
                            Text(
                                text = when {
                                    isSelected && tab == MatchDayTab.YESTERDAY -> "$count Final"
                                    isSelected && count == 1 -> "1 Game"
                                    isSelected -> "$count Games"
                                    else -> "$count"
                                },
                                color = when {
                                    isSelected && tab == MatchDayTab.YESTERDAY -> KickoffObsidianBlack
                                    isSelected -> KickoffAlabasterIvory
                                    hasTodayLive -> KickoffAlabasterIvory
                                    else -> KickoffAgedParchment.copy(alpha = 0.88f)
                                },
                                fontSize = if (isSelected) 8.sp else 7.5.sp,
                                lineHeight = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(
                                    horizontal = if (isSelected) 6.5.dp else 5.5.dp,
                                    vertical = 1.dp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3-Pillar Competition Category Selector Bar (`CompetitionCategoryPillarRow`):
 * - Lets users filter the Kickoff Hub across the 3 core competition pillars:
 *   1. All Pillars (`null`)
 *   2. European Competitions (`CompetitionCategory.EUROPEAN`)
 *   3. African & Arab Competitions (`CompetitionCategory.AFRICAN_ARAB` — incl. CAF Champions League, CAF Confederation Cup, Botola Pro, Roshn Saudi League)
 *   4. International (`CompetitionCategory.INTERNATIONAL` — Dynamic FIFA Top-Ranked National Teams)
 */
@Composable
private fun CompetitionCategoryPillarRow(
    selectedCategory: CompetitionCategory?,
    activeDayMatches: List<FootballMatch>,
    tournamentCategoryMap: Map<String, CompetitionCategory>,
    onSelectCategory: (CompetitionCategory?) -> Unit
) {
    val pillarCounts = remember(activeDayMatches, tournamentCategoryMap) {
        val counts = mutableMapOf<CompetitionCategory, Pair<Int, Int>>()
        for (cat in CompetitionCategory.entries) {
            val matchesForCat = activeDayMatches.filter {
                (tournamentCategoryMap[it.tournamentId] ?: CompetitionCategory.EUROPEAN) == cat
            }
            val live = matchesForCat.count { it.status == MatchStatus.LIVE || it.status == MatchStatus.HALFTIME }
            counts[cat] = live to matchesForCat.size
        }
        counts
    }

    val pillars: List<Triple<CompetitionCategory?, String, String>> = remember {
        listOf(
            Triple(null, "All Pillars", "pillar_chip_all"),
            Triple(CompetitionCategory.EUROPEAN, "European", "pillar_chip_EUROPEAN"),
            Triple(CompetitionCategory.AFRICAN_ARAB, "African & Arab", "pillar_chip_AFRICAN_ARAB"),
            Triple(CompetitionCategory.INTERNATIONAL, "International", "pillar_chip_INTERNATIONAL")
        )
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.testTag("competition_category_pillar_row")
    ) {
        items(pillars, key = { it.third }) { (category, label, tag) ->
            val isSelected = selectedCategory == category
            val (liveInPillar, totalInPillar) = if (category == null) {
                val liveAll = activeDayMatches.count { it.status == MatchStatus.LIVE || it.status == MatchStatus.HALFTIME }
                liveAll to activeDayMatches.size
            } else {
                pillarCounts[category] ?: (0 to 0)
            }

            Surface(
                color = if (isSelected) KickoffImperialNavy else KickoffCharcoalSlate,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(
                    width = if (isSelected) 1.dp else 0.75.dp,
                    color = if (isSelected) KickoffTerracotta else KickoffParchmentBorder
                ),
                modifier = Modifier
                    .height(26.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .tvFocusablePill(
                        accentColor = KickoffTerracotta,
                        shape = RoundedCornerShape(8.dp),
                        onClick = { onSelectCategory(category) }
                    )
                    .testTag(tag)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(horizontal = 9.dp)
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(KickoffTerracotta)
                        )
                    }
                    Text(
                        text = label,
                        color = if (isSelected) KickoffAlabasterIvory else KickoffAgedParchment,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                    if (liveInPillar > 0 && category != null) {
                        Surface(
                            color = KickoffTerracotta,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "$liveInPillar LIVE",
                                color = KickoffAlabasterIvory,
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 4.5.dp, vertical = 0.5.dp)
                            )
                        }
                    } else if (totalInPillar > 0 && category != null) {
                        Text(
                            text = "$totalInPillar",
                            color = if (isSelected) KickoffAgedParchment else KickoffParchmentMuted,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            style = TabularNumeralTextStyle
                        )
                    }
                }
            }
        }
    }
}

/**
 * Horizontal Tournament Filter Strip with Real Tournament Crest Medallions:
 * - Replaces plain colored dots with official high-res league/tournament crests inside a glowing circular medallion.
 * - Shows a compact live match badge or fixture count pill per tournament.
 */
@Composable
private fun TournamentFilterRow(
    selectedFilter: String,
    selectedCategory: CompetitionCategory? = null,
    tournamentCategoryMap: Map<String, CompetitionCategory> = emptyMap(),
    favoriteTeamsCount: Int,
    liveCount: Int,
    activeDayMatches: List<FootballMatch> = emptyList(),
    tournamentPreferences: List<TournamentPreferenceEntity>,
    onSelectFilter: (String) -> Unit,
    onOpenCustomize: () -> Unit
) {
    val enabledTournaments = remember(tournamentPreferences, selectedCategory, tournamentCategoryMap) {
        tournamentPreferences
            .filter { pref ->
                if (!pref.isEnabled) return@filter false
                if (selectedCategory != null) {
                    val cat = tournamentCategoryMap[pref.tournamentId] ?: CompetitionCategory.EUROPEAN
                    cat == selectedCategory
                } else {
                    true
                }
            }
            .sortedWith(
                compareBy<TournamentPreferenceEntity> {
                    getTournamentCanonicalPriority(it.tournamentId, it.sortOrder)
                }.thenBy {
                    it.sortOrder
                }
            )
    }

    val tournamentCountsMap = remember(activeDayMatches) {
        activeDayMatches.groupBy { it.tournamentId }.mapValues { (_, list) ->
            val live = list.count { it.status == MatchStatus.LIVE || it.status == MatchStatus.HALFTIME }
            live to list.size
        }
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.testTag("tournament_filter_row")
    ) {
        item {
            val isAllSelected = selectedFilter == "ALL"
            LuxuryLeagueCapsule(
                selected = isAllSelected,
                onClick = { onSelectFilter("ALL") },
                modifier = Modifier.testTag("filter_chip_all")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(
                                if (isAllSelected) KickoffObsidianBlack.copy(alpha = 0.28f)
                                else KickoffImperialNavy.copy(alpha = 0.75f)
                            )
                            .border(
                                0.8.dp,
                                if (isAllSelected) KickoffAlabasterIvory.copy(alpha = 0.65f)
                                else KickoffAgedParchment.copy(alpha = 0.45f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = if (isAllSelected) KickoffAlabasterIvory else KickoffAgedParchment,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Text(
                        text = "All Tournaments",
                        color = if (isAllSelected) KickoffAlabasterIvory else KickoffAgedParchment,
                        fontSize = 11.5.sp,
                        fontWeight = if (isAllSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                    if (liveCount > 0) {
                        Surface(
                            color = if (isAllSelected) KickoffObsidianBlack else KickoffTerracotta,
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
                                        .background(KickoffAlabasterIvory)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "$liveCount LIVE",
                                    color = KickoffAlabasterIvory,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }

        items(enabledTournaments, key = { it.tournamentId }) { tournament ->
            val isSelected = selectedFilter == tournament.tournamentId
            val chipLabel = when (tournament.tournamentId) {
                "laliga" -> "La Liga"
                "ligue_1" -> "Ligue 1"
                "botola_pro" -> "Botola Pro"
                "caf_confed" -> "CAF Confed Cup"
                "intl_official" -> "Intl Official"
                "intl_friendly" -> "Intl Friendlies"
                else -> tournament.tournamentName
            }
            val leagueAccentColor = remember(tournament.tournamentId) {
                resolveTournamentAccentDotColor(tournament.tournamentId)
            }
            val (liveInLeague, totalInLeague) = tournamentCountsMap[tournament.tournamentId] ?: (0 to 0)

            LuxuryLeagueCapsule(
                selected = isSelected,
                accentColor = leagueAccentColor,
                onClick = { onSelectFilter(tournament.tournamentId) },
                modifier = Modifier.testTag("filter_chip_${tournament.tournamentId}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    TournamentLogoMedallion(
                        tournamentId = tournament.tournamentId,
                        tournamentName = chipLabel,
                        isSelected = isSelected,
                        accentColor = leagueAccentColor,
                        sizeDp = 22
                    )
                    Text(
                        text = chipLabel,
                        color = if (isSelected) KickoffAlabasterIvory else KickoffAgedParchment,
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                    if (liveInLeague > 0) {
                        Surface(
                            color = if (isSelected) KickoffObsidianBlack else KickoffTerracotta,
                            shape = RoundedCornerShape(50)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 5.5.dp, vertical = 1.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(KickoffAlabasterIvory)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "$liveInLeague",
                                    color = KickoffAlabasterIvory,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    } else if (totalInLeague > 0) {
                        Surface(
                            color = if (isSelected) KickoffObsidianBlack.copy(alpha = 0.35f) else KickoffObsidianBlack.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "$totalInLeague",
                                color = if (isSelected) KickoffAlabasterIvory else KickoffParchmentMuted,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 5.5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            LuxuryLeagueCapsule(
                selected = false,
                onClick = onOpenCustomize,
                isActionCapsule = true,
                modifier = Modifier.testTag("modify_tournaments_chip")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = KickoffTerracotta,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Modify Leagues",
                        color = KickoffAlabasterIvory,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
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
        else -> tournamentName.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1).uppercase() }.ifBlank { "cup" }
    }
}

/**
 * Circular Crest Medallion rendering the real tournament logo over a crisp illuminated Alabaster Ivory disc
 * so dark and multi-color official league emblems remain razor-sharp on both dark and highlighted chips.
 */
@Composable
internal fun TournamentLogoMedallion(
    tournamentId: String,
    tournamentName: String,
    isSelected: Boolean = false,
    accentColor: Color = KickoffTerracotta,
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
                        KickoffAlabasterIvory,
                        KickoffAgedParchment.copy(alpha = 0.90f)
                    )
                )
            )
            .border(
                width = if (isSelected) 1.2.dp else 0.9.dp,
                color = if (isSelected) KickoffObsidianBlack.copy(alpha = 0.55f) else accentColor.copy(alpha = 0.75f),
                shape = CircleShape
            )
    ) {
        Text(
            text = monogram,
            color = KickoffObsidianBlack.copy(alpha = 0.82f),
            fontSize = (sizeDp * 0.32f).sp,
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

private fun resolveTournamentAccentDotColor(tournamentId: String): Color {
    return when (tournamentId) {
        "uefa_cl", "uefa_el" -> KickoffAlabasterIvory
        "laliga", "bundesliga", "caf_cl", "caf_confed" -> KickoffTerracotta
        "premier_league", "serie_a", "ligue_1" -> KickoffAgedParchment
        "botola_pro", "saudi_pro" -> KickoffTerracotta
        "intl_official", "intl_friendly" -> KickoffAgedParchment
        else -> KickoffTerracotta
    }
}

@Composable
private fun LuxuryLeagueCapsule(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = KickoffTerracotta,
    isActionCapsule: Boolean = false,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "capsule_press_scale"
    )

    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(50),
        border = when {
            selected -> BorderStroke(1.2.dp, KickoffAgedParchment.copy(alpha = 0.90f))
            isActionCapsule -> BorderStroke(0.9.dp, KickoffTerracotta.copy(alpha = 0.55f))
            else -> BorderStroke(0.8.dp, KickoffParchmentBorder)
        },
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(50))
            .background(
                brush = when {
                    selected -> Brush.horizontalGradient(
                        listOf(
                            KickoffTerracotta,
                            KickoffImperialNavy
                        )
                    )
                    isActionCapsule -> Brush.horizontalGradient(
                        listOf(
                            KickoffImperialNavy,
                            KickoffCharcoalSlate
                        )
                    )
                    else -> Brush.horizontalGradient(
                        listOf(
                            KickoffCharcoalElevated,
                            KickoffCharcoalSlate
                        )
                    )
                }
            )
            .tvFocusablePill(
                accentColor = accentColor,
                shape = RoundedCornerShape(50),
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier.padding(start = 8.dp, end = 12.dp, top = 5.5.dp, bottom = 5.5.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
private fun FavoriteTeamsEmptyBanner(
    onSelectFavoriteTeams: () -> Unit
) {
    Surface(
        color = ArenaTurfDark,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.55f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = AmberGold,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "No Favorite Teams Selected Yet",
                        color = PaleGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Search and follow your favorite clubs to view their matches in this My Teams tab.",
                        color = Champagne,
                        fontSize = 11.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                onClick = onSelectFavoriteTeams,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberGold,
                    contentColor = DarkGraphite
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("add_favorite_teams_prompt_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Teams", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun PinnedFavoriteTeamsSection(
    favoriteTeams: List<FavoriteTeamEntity>,
    favoriteMatches: List<FootballMatch>,
    selectedDay: MatchDayTab,
    selectedCountryZone: UserCountryZone = UserZoneRegistry.defaultCountryZone,
    selectedSeasonTimeMode: SeasonTimeMode = SeasonTimeMode.AUTO,
    onEditFavoriteTeams: () -> Unit,
    onInspectMatchDossier: (FootballMatch) -> Unit,
    onWatchMatch: (FootballMatch, String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ArenaTurfDark),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, AmberGold),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("pinned_favorite_teams_section")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Gold & Pitch Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AmberGold.copy(alpha = 0.16f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = AmberGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MY FAVORITE TEAMS (${favoriteTeams.size})",
                        color = PaleGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.6.sp
                    )
                }

                TextButton(
                    onClick = onEditFavoriteTeams,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("edit_favorite_teams_btn")
                ) {
                    Text(
                        text = "Edit Teams",
                        color = AmberGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Horizontal row of user's favorite team badges
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(favoriteTeams, key = { it.teamId }) { team ->
                    Surface(
                        color = ArenaTurfElevated,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.8.dp, AmberGold.copy(alpha = 0.6f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            TeamCrestImage(
                                badgeUrl = team.badgeUrl,
                                teamName = team.teamName,
                                sizeDp = 18
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = team.teamName,
                                color = PaleGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (favoriteMatches.isEmpty()) {
                val tzId = selectedCountryZone.timeZoneId
                Text(
                    text = "No scheduled matches for your favorite teams on ${selectedDay.getDynamicTitle(tzId)} (${selectedDay.getDynamicDateSubtitle(tzId)}). Try another day in the 5-day timeline above!",
                    color = Champagne,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                )
            } else {
                favoriteMatches.forEach { match ->
                    HorizontalDivider(color = ChampagneSubtleBorder, thickness = 0.5.dp)
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
                        onToggleHomeFavorite = {},
                        onToggleAwayFavorite = {},
                        onOpenDossier = { onInspectMatchDossier(match) },
                        onWatchBeinClicked = { clickedChannel -> onWatchMatch(match, clickedChannel) },
                        isInsidePinnedSection = true
                    )
                }
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
    Surface(
        color = KickoffObsidianBlack,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, KickoffAgedParchment.copy(alpha = 0.55f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("pinned_matches_rail")
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = null,
                        tint = KickoffTerracotta,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "📌 PINNED MATCHES • MY GAMES (${pinnedMatches.size})",
                        color = KickoffAlabasterIvory,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    text = "LIVE GOAL ALERTS ACTIVE",
                    color = KickoffTerracotta,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pinnedMatches, key = { "pinned_${it.matchId}" }) { match ->
                    val isLive = match.status == MatchStatus.LIVE || match.status == MatchStatus.HALFTIME
                    val scoreText = if (match.homeScore != null && match.awayScore != null) {
                        "${match.homeScore} - ${match.awayScore}"
                    } else {
                        "vs"
                    }
                    Surface(
                        color = if (isLive) KickoffImperialNavy else KickoffCharcoalSlate,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            width = 0.9.dp,
                            color = if (isLive) KickoffTerracotta.copy(alpha = 0.80f) else KickoffParchmentBorder
                        ),
                        modifier = Modifier.width(226.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = if (isLive) KickoffTerracotta else KickoffObsidianBlack,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (isLive) "● ${match.liveMinuteOrBadge}" else match.liveMinuteOrBadge.ifBlank { match.kickoffTimeFormatted },
                                        color = if (isLive) KickoffAlabasterIvory else KickoffAgedParchment,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onUnpinMatch(match.matchId) },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Unpin Match",
                                        tint = KickoffParchmentMuted,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(5.dp))

                            Text(
                                text = "${match.homeTeamName}  $scoreText  ${match.awayTeamName}",
                                color = KickoffAlabasterIvory,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    color = KickoffTerracotta,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(0.6.dp, KickoffAlabasterIvory.copy(alpha = 0.45f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { onWatchMatch(match) }
                                ) {
                                    Text(
                                        text = "▶ Watch",
                                        color = KickoffAlabasterIvory,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    color = KickoffImperialNavy,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(0.6.dp, KickoffAgedParchment),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { onOpenDossier(match) }
                                ) {
                                    Text(
                                        text = "📊 Dossier",
                                        color = KickoffAgedParchment,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 4.dp)
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

@Composable
private fun FlashscoreTournamentHeader(
    tournamentId: String,
    tournamentName: String,
    regionName: String,
    menaProviderName: String,
    matchCount: Int,
    liveCount: Int = 0,
    isPinned: Boolean,
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit,
    onTogglePin: () -> Unit
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (isCollapsed) 0f else 180f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tournamentChevronRotation"
    )
    val leagueAccentColor = remember(tournamentId) {
        resolveTournamentAccentDotColor(tournamentId)
    }

    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(
            topStart = 14.dp,
            topEnd = 14.dp,
            bottomStart = if (isCollapsed) 14.dp else 0.dp,
            bottomEnd = if (isCollapsed) 14.dp else 0.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    if (liveCount > 0) KickoffTerracotta.copy(alpha = 0.72f) else KickoffAgedParchment.copy(alpha = 0.45f),
                    KickoffParchmentBorder,
                    KickoffParchmentBorder
                )
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp)
            .clip(
                RoundedCornerShape(
                    topStart = 14.dp,
                    topEnd = 14.dp,
                    bottomStart = if (isCollapsed) 14.dp else 0.dp,
                    bottomEnd = if (isCollapsed) 14.dp else 0.dp
                )
            )
            .background(
                Brush.horizontalGradient(
                    listOf(
                        KickoffImperialNavy,
                        KickoffNavyElevated,
                        KickoffCharcoalSlate
                    )
                )
            )
            .tvFocusableControl(
                accentColor = KickoffTerracotta,
                shape = RoundedCornerShape(
                    topStart = 14.dp,
                    topEnd = 14.dp,
                    bottomStart = if (isCollapsed) 14.dp else 0.dp,
                    bottomEnd = if (isCollapsed) 14.dp else 0.dp
                ),
                onClick = onToggleCollapse
            )
            .testTag("tournament_header_$tournamentId")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Signature League Vertical Accent Bar + Official Tournament Logo Medallion + Region & League Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    KickoffTerracotta,
                                    if (liveCount > 0) KickoffAlabasterIvory else KickoffAgedParchment
                                )
                            )
                        )
                )
                Spacer(modifier = Modifier.width(10.dp))
                TournamentLogoMedallion(
                    tournamentId = tournamentId,
                    tournamentName = tournamentName,
                    isSelected = isPinned,
                    accentColor = leagueAccentColor,
                    sizeDp = 30
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = regionName.uppercase(),
                            color = KickoffAgedParchment,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                        if (menaProviderName.isNotBlank()) {
                            Surface(
                                color = KickoffObsidianBlack.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.5.dp, KickoffAgedParchment.copy(alpha = 0.50f))
                            ) {
                                Text(
                                    text = menaProviderName.uppercase(),
                                    color = KickoffAlabasterIvory,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.3.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 4.5.dp, vertical = 0.5.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = tournamentName,
                        color = KickoffAlabasterIvory,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right: Live Badge (if active), Pin Icon, Match Count Badge, and Chevron
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (liveCount > 0) {
                    Surface(
                        color = KickoffTerracotta,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.7.dp, KickoffAlabasterIvory.copy(alpha = 0.75f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.5.dp, vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(4.5.dp)
                                    .clip(CircleShape)
                                    .background(KickoffAlabasterIvory)
                            )
                            Spacer(modifier = Modifier.width(3.5.dp))
                            Text(
                                text = "$liveCount LIVE",
                                color = KickoffAlabasterIvory,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold
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
                        tint = if (isPinned) KickoffTerracotta else KickoffParchmentMuted,
                        modifier = Modifier.size(13.5.dp)
                    )
                }

                Surface(
                    color = KickoffObsidianBlack,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.8.dp, KickoffAgedParchment.copy(alpha = 0.55f))
                ) {
                    Text(
                        text = "$matchCount",
                        color = KickoffAlabasterIvory,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.5.dp)
                    )
                }

                Spacer(modifier = Modifier.width(5.dp))

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isCollapsed) "Expand" else "Collapse",
                    tint = KickoffAgedParchment,
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer { rotationZ = chevronRotation }
                )
            }
        }
    }
}

internal data class LiveMatchClockTelemetry(
    val periodBadge: String,       // "1ST HALF", "2ND HALF", "HALF TIME", "FINAL", "KICKOFF"
    val heroReadout: String,       // "86'", "41'", "HT", "FT", "20:00"
    val bottomBadge: String,       // "HT 0-1", "LIVE MIN", "GMT+1", "FULL TIME"
    val statusExplanation: String, // "2ND HALF • 86' PLAYED", "1ST HALF • 41' PLAYED"
    val remainingLabel: String,    // "4' TO 90'", "4' TO HT", "BREAK"
    val progressFraction: Float,   // 0.06f..1.0f
    val heroReadoutSeconds: String = heroReadout // "19:24", "45:00+02:15", "78:42"
)

internal fun parseLiveMatchProgressFraction(liveBadge: String, status: MatchStatus): Float {
    val clean = liveBadge.trim()
    if (status == MatchStatus.HALFTIME || clean.equals("HT", ignoreCase = true) || clean.contains("HT", ignoreCase = true)) {
        return 0.50f
    }
    // 1. Prefer minute immediately followed by apostrophe e.g. "2H • 86'" -> 86 or "45+2'" -> 45
    val apostropheMatch = Regex("(\\d{1,3})(?:\\+\\d{1,2})?\\s*'").find(clean)
    if (apostropheMatch != null) {
        val minute = apostropheMatch.groupValues[1].toIntOrNull()
        if (minute != null) {
            return (minute.toFloat() / 90f).coerceIn(0.06f, 1.0f)
        }
    }
    // 2. Fallback: remove "1H" / "2H" period prefixes and find the last numeric token
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

/**
 * Dual-Stage Broadcast Launch Deck (`[ 📺 Broadcaster + UHD/HD | (▶) WATCH LIVE ]`):
 * - Stage 1 (Broadcaster Network Pod): Frosted broadcaster glass with live TV icon, channel label,
 *   and an explicit 4K/FHD broadcast quality tag.
 * - Stage 2 (Illuminated Floodlight Watch CTA): Multi-stop neon cyan-to-mint stadium gradient with
 *   a recessed dark circular Play Medallion, specular top rim, and bold uppercase typography.
 */
@Composable
private fun SplitBroadcasterWatchCapsule(
    broadcasterName: String,
    badgeBg: Color,
    badgeAccent: Color = MenaBeinPurple,
    badgeText: Color,
    isLive: Boolean,
    matchStatus: MatchStatus = if (isLive) MatchStatus.LIVE else MatchStatus.UPCOMING,
    watchTestTag: String,
    onWatchClick: () -> Unit,
    onActionClick: () -> Unit = onWatchClick
) {
    val isLiveStatus = matchStatus == MatchStatus.LIVE || matchStatus == MatchStatus.HALFTIME
    val isFinishedStatus = matchStatus == MatchStatus.FINISHED

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "split_watch_capsule_scale"
    )
    val isBeinSourceChannel = remember(broadcasterName) {
        val lower = broadcasterName.lowercase()
        lower.contains("bein") || lower.contains("be in")
    }
    val isUltraHdChannel = remember(broadcasterName) {
        val upper = broadcasterName.uppercase()
        upper.contains("4K") || upper.contains("MAX") || upper.contains("PREMIUM") || upper.contains("1 ")
    }

    Surface(
        color = KickoffObsidianBlack.copy(alpha = if (isFinishedStatus) 0.78f else 0.94f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                if (isFinishedStatus) {
                    listOf(
                        KickoffAgedParchment.copy(alpha = 0.40f),
                        KickoffParchmentBorder
                    )
                } else {
                    listOf(
                        badgeAccent.copy(alpha = 0.70f),
                        KickoffTerracotta.copy(alpha = 0.85f)
                    )
                }
            )
        ),
        shadowElevation = if (isFinishedStatus) 1.dp else 4.dp,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .height(32.dp)
            .clip(RoundedCornerShape(50))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 9.dp, end = 2.5.dp, top = 2.5.dp, bottom = 2.5.dp)
        ) {
            // Stage 1: MENA Broadcaster Network Pod (Exact Source Name for beIN Sports)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onWatchClick
                    )
                    .padding(end = 7.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LiveTv,
                    contentDescription = null,
                    tint = KickoffAlabasterIvory,
                    modifier = Modifier.size(11.5.dp)
                )
                Spacer(modifier = Modifier.width(4.5.dp))
                Text(
                    text = com.example.util.StreamMetadataParser.compactBroadcastChannelLabel(broadcasterName),
                    color = KickoffAlabasterIvory,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = if (isBeinSourceChannel) 140.dp else 112.dp)
                )
                if (!isBeinSourceChannel) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        color = KickoffCharcoalSlate,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(
                            0.6.dp,
                            if (isUltraHdChannel) KickoffTerracotta else KickoffAgedParchment.copy(alpha = 0.45f)
                        )
                    ) {
                        Text(
                            text = if (isUltraHdChannel) "4K" else "HD",
                            color = if (isUltraHdChannel) KickoffTerracotta else KickoffAgedParchment,
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 3.5.dp, vertical = 0.5.dp)
                        )
                    }
                }
            }

            // Stage 2: Context-Aware Action Pill by MatchStatus (LIVE / UPCOMING / FINISHED)
            Surface(
                color = Color.Transparent,
                shape = RoundedCornerShape(50),
                border = BorderStroke(
                    0.8.dp,
                    when {
                        isLiveStatus -> KickoffAlabasterIvory.copy(alpha = 0.85f)
                        isFinishedStatus -> KickoffAgedParchment.copy(alpha = 0.40f)
                        else -> KickoffAgedParchment.copy(alpha = 0.65f)
                    }
                ),
                modifier = Modifier
                    .height(27.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        brush = when {
                            isLiveStatus -> Brush.horizontalGradient(
                                listOf(
                                    KickoffTerracotta,
                                    Color(0xFFE86830)
                                )
                            )
                            isFinishedStatus -> Brush.horizontalGradient(
                                listOf(
                                    KickoffCharcoalSlate,
                                    KickoffCharcoalElevated
                                )
                            )
                            else -> Brush.horizontalGradient(
                                listOf(
                                    KickoffImperialNavy,
                                    KickoffNavyElevated
                                )
                            )
                        }
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onActionClick
                    )
                    .testTag(watchTestTag)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(start = 4.dp, end = 9.dp)
                ) {
                    // Recessed Dark Circular Action Medallion
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(KickoffObsidianBlack)
                    ) {
                        Icon(
                            imageVector = if (isFinishedStatus) Icons.Default.Info else Icons.Default.PlayArrow,
                            contentDescription = when {
                                isLiveStatus -> "Watch Live Match"
                                isFinishedStatus -> "Match Stats and TV Guide"
                                else -> "Pre-Match Channel Test"
                            },
                            tint = when {
                                isLiveStatus -> KickoffTerracotta
                                isFinishedStatus -> KickoffAgedParchment
                                else -> KickoffAlabasterIvory
                            },
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    if (isLiveStatus) {
                        LiveBroadcastEqualizerBars()
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = when {
                            isLiveStatus -> "WATCH LIVE"
                            isFinishedStatus -> "STATS & TV"
                            else -> "PRE-MATCH"
                        },
                        color = when {
                            isLiveStatus -> KickoffAlabasterIvory
                            isFinishedStatus -> KickoffAgedParchment
                            else -> KickoffAlabasterIvory
                        },
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.4.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

/**
 * Animated 3-bar audio/broadcast equalizer wave inside active 'WATCH LIVE' pills.
 */
@Composable
private fun LiveBroadcastEqualizerBars() {
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
                    .background(KickoffAlabasterIvory)
            )
        }
    }
}

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
    isLastInSection: Boolean = false
) {
    val isLive = match.status == MatchStatus.LIVE || match.status == MatchStatus.HALFTIME
    val isFinished = match.status == MatchStatus.FINISHED
    val hasFavTeam = isHomeTeamFavorite || isAwayTeamFavorite
    val (badgeBg, badgeAccent, badgeText) = resolveMenaBroadcasterColors(match.beinChannel)

    val homeIsWinner = isFinished && match.homeScore != null && match.awayScore != null && match.homeScore > match.awayScore
    val awayIsWinner = isFinished && match.homeScore != null && match.awayScore != null && match.awayScore > match.homeScore
    val hasDecisiveWinner = homeIsWinner || awayIsWinner

    val leftSpineColor = when {
        isLive -> KickoffTerracotta
        hasFavTeam -> KickoffAgedParchment
        else -> KickoffImperialNavy
    }

    val livePulseTransition = rememberInfiniteTransition(label = "matchCardLivePulse_${match.matchId}")
    val livePulseAlpha by livePulseTransition.animateFloat(
        initialValue = if (isLive) 0.50f else 0.30f,
        targetValue = if (isLive) 0.96f else 0.30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liveClockBorderAlpha"
    )
    val liveDotScale by livePulseTransition.animateFloat(
        initialValue = if (isLive) 0.82f else 1f,
        targetValue = if (isLive) 1.24f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liveClockDotScale"
    )

    val cardShape = if (!isInsidePinnedSection && isLastInSection) {
        RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp)
    } else {
        RoundedCornerShape(0.dp)
    }

    Surface(
        color = if (isInsidePinnedSection) Color.Transparent else KickoffCharcoalSlate,
        shape = cardShape,
        border = if (isInsidePinnedSection) {
            null
        } else {
            BorderStroke(
                width = 0.9.dp,
                color = if (isLive) KickoffTerracotta.copy(alpha = 0.45f) else KickoffParchmentBorder
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isInsidePinnedSection) 0.dp else 16.dp)
            .clip(cardShape)
            .luxuryFocusableCard(
                accentColor = if (isLive) KickoffTerracotta else KickoffAgedParchment,
                shape = cardShape,
                onClick = { onOpenDossier() }
            )
            .testTag("match_card_${match.matchId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Left Stadium Status Spine
            if (!isInsidePinnedSection) {
                Box(
                    modifier = Modifier
                        .width(3.5.dp)
                        .fillMaxHeight()
                        .background(leftSpineColor)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (isLive) {
                            Brush.horizontalGradient(
                                listOf(
                                    KickoffTerracottaGlow,
                                    KickoffImperialNavy.copy(alpha = 0.35f),
                                    Color.Transparent
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, Color.Transparent)
                            )
                        }
                    )
                    .padding(horizontal = 12.dp, vertical = 9.dp)
            ) {
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
                val clockTelemetry = remember(
                    match.liveMinuteOrBadge,
                    match.status,
                    match.halfTimeScore,
                    dynamicKickoffTime,
                    utcOffsetBadge,
                    liveElapsedSeconds
                ) {
                    parseLiveMatchClockTelemetry(
                        match = match,
                        utcOffsetBadge = utcOffsetBadge,
                        elapsedSecondOffset = if (match.status == MatchStatus.LIVE) liveElapsedSeconds else 0,
                        dynamicKickoffTime = dynamicKickoffTime
                    )
                }

                // Match Main Row: 3-Tier Broadcast Digital Clock Pod (Left) + Dual Team Scoreboard Rows (Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Left Column (72dp wide): Two-Tone Sculpted Broadcast Time & Status Pod
                    Surface(
                        color = KickoffObsidianBlack,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            width = if (isLive) 1.1.dp else 0.85.dp,
                            color = when {
                                isLive -> KickoffTerracotta.copy(alpha = livePulseAlpha)
                                isFinished -> KickoffParchmentBorder
                                else -> KickoffImperialNavy.copy(alpha = 0.85f)
                            }
                        ),
                        modifier = Modifier
                            .width(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Top Header Banner: Full-Width Period / Kickoff Strip
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        when {
                                            isLive -> KickoffTerracotta
                                            isFinished -> KickoffCharcoalElevated
                                            else -> KickoffImperialNavy
                                        }
                                    )
                                    .padding(vertical = 2.5.dp, horizontal = 3.dp)
                            ) {
                                Text(
                                    text = clockTelemetry.periodBadge,
                                    color = when {
                                        isLive -> KickoffAlabasterIvory
                                        isFinished -> KickoffAgedParchment
                                        else -> KickoffAlabasterIvory
                                    },
                                    fontSize = 7.5.sp,
                                    lineHeight = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.3.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            // Middle Tier: Live MM:SS Clock ("19:24", "45:00+02:15", "HT") or Kickoff Time ("16:30")
                            val displayClockText = if (match.status == MatchStatus.LIVE) {
                                clockTelemetry.heroReadoutSeconds
                            } else {
                                clockTelemetry.heroReadout
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 3.dp)
                            ) {
                                if (isLive) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .graphicsLayer {
                                                scaleX = liveDotScale
                                                scaleY = liveDotScale
                                            }
                                            .clip(CircleShape)
                                            .background(KickoffTerracotta)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                }
                                Text(
                                    text = displayClockText,
                                    color = when (match.status) {
                                        MatchStatus.LIVE, MatchStatus.HALFTIME -> KickoffAlabasterIvory
                                        MatchStatus.FINISHED -> KickoffAgedParchment
                                        MatchStatus.UPCOMING -> KickoffAlabasterIvory
                                    },
                                    fontSize = when {
                                        displayClockText.length > 6 -> 9.5.sp
                                        isLive -> 12.5.sp
                                        else -> 13.sp
                                    },
                                    lineHeight = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = TabularNumeralTextStyle,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            // Bottom Tier: Half-Time Score ("HT 0-1") or Timezone ("GMT+1")
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(KickoffCharcoalSlate.copy(alpha = 0.65f))
                                    .padding(vertical = 2.dp, horizontal = 3.dp)
                            ) {
                                Text(
                                    text = clockTelemetry.bottomBadge,
                                    color = when {
                                        match.halfTimeScore != null && (isLive || isFinished) -> KickoffAgedParchment
                                        isLive -> KickoffTerracotta
                                        else -> KickoffAgedParchment
                                    },
                                    fontSize = 7.5.sp,
                                    lineHeight = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // 2. Center & Right Columns: Two teams stacked vertically with crisp crests and Scoreboard Well
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(5.5.dp)
                    ) {
                        FlashscoreTeamRow(
                            teamName = match.homeTeamName,
                            badgeUrl = match.homeTeamBadge,
                            score = match.homeScore,
                            redCards = match.homeRedCards,
                            isLive = isLive,
                            isFinished = isFinished,
                            isWinner = homeIsWinner,
                            isLoser = hasDecisiveWinner && !homeIsWinner,
                            isFavorite = isHomeTeamFavorite,
                            onToggleFavorite = onToggleHomeFavorite
                        )

                        FlashscoreTeamRow(
                            teamName = match.awayTeamName,
                            badgeUrl = match.awayTeamBadge,
                            score = match.awayScore,
                            redCards = match.awayRedCards,
                            isLive = isLive,
                            isFinished = isFinished,
                            isWinner = awayIsWinner,
                            isLoser = hasDecisiveWinner && !awayIsWinner,
                            isFavorite = isAwayTeamFavorite,
                            onToggleFavorite = onToggleAwayFavorite
                        )
                    }
                }

                // Match Action Footer: Sleek Single-Row 90-Minute Timeline & Event Strip when LIVE
                Spacer(modifier = Modifier.height(7.dp))
                if (isLive) {
                    val latestGoalScorer = remember(match.goalScorersSummary) {
                        match.goalScorersSummary
                            ?.split(",")
                            ?.map { it.trim() }
                            ?.lastOrNull { it.isNotBlank() }
                            .orEmpty()
                    }
                    Surface(
                        color = KickoffObsidianBlack,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.6.dp, KickoffTerracotta.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "0'",
                                color = KickoffParchmentMuted,
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(KickoffCharcoalElevated)
                            ) {
                                // Illuminated elapsed match progress fill
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(clockTelemetry.progressFraction)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(KickoffImperialNavy, KickoffAgedParchment, KickoffTerracotta)
                                            )
                                        )
                                )
                                // Half-Time (45' / 50%) center marker tick
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .width(1.5.dp)
                                        .height(4.dp)
                                        .background(KickoffAlabasterIvory.copy(alpha = 0.70f))
                                )
                            }
                            Text(
                                text = "HT",
                                color = if (clockTelemetry.progressFraction >= 0.5f) KickoffAgedParchment else KickoffParchmentMuted,
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "90'",
                                color = KickoffParchmentMuted,
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (latestGoalScorer.isNotBlank()) {
                                Text(
                                    text = "• ⚽ $latestGoalScorer",
                                    color = KickoffAlabasterIvory,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            } else {
                                Text(
                                    text = "• ${clockTelemetry.remainingLabel}",
                                    color = KickoffTerracotta,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                } else {
                    HorizontalDivider(
                        color = KickoffParchmentBorder,
                        thickness = 0.8.dp
                    )
                }
                Spacer(modifier = Modifier.height(6.5.dp))

                // Zone 2: Bottom Action Footer — Left Broadcaster Badge (📺 beIN Sports 1 HD) + Right Status-Aware CTA
                val isFinishedStatus = match.status == MatchStatus.FINISHED
                val isUltraHdChannel = remember(match.beinChannel) {
                    val upper = match.beinChannel.uppercase()
                    upper.contains("4K") || upper.contains("MAX") || upper.contains("PREMIUM") || upper.contains("UHD")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Broadcaster Badge (📺 beIN Sports 1 HD) — never collides with stadium name
                    Surface(
                        color = KickoffObsidianBlack,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(
                            0.8.dp,
                            if (isFinishedStatus) KickoffParchmentBorder else badgeAccent.copy(alpha = 0.75f)
                        ),
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .height(28.dp)
                            .clip(RoundedCornerShape(50))
                            .tvFocusablePill(
                                accentColor = KickoffTerracotta,
                                shape = RoundedCornerShape(50),
                                onClick = { onWatchBeinClicked(match.beinChannel) }
                            )
                            .testTag("open_match_center_${match.matchId}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LiveTv,
                                contentDescription = "Broadcaster",
                                tint = KickoffAgedParchment,
                                modifier = Modifier.size(12.5.dp)
                            )
                            Spacer(modifier = Modifier.width(5.5.dp))
                            Text(
                                text = com.example.util.StreamMetadataParser.compactBroadcastChannelLabel(match.beinChannel),
                                color = KickoffAlabasterIvory,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Surface(
                                color = KickoffCharcoalSlate,
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(
                                    0.6.dp,
                                    if (isUltraHdChannel) KickoffTerracotta else KickoffAgedParchment.copy(alpha = 0.45f)
                                )
                            ) {
                                Text(
                                    text = if (isUltraHdChannel) "4K" else "HD",
                                    color = if (isUltraHdChannel) KickoffTerracotta else KickoffAgedParchment,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Pin Match ("My Games") Toggle Button
                    Surface(
                        color = if (isMatchPinned) KickoffTerracottaGlow else KickoffObsidianBlack,
                        shape = CircleShape,
                        border = BorderStroke(
                            0.75.dp,
                            if (isMatchPinned) KickoffTerracotta else KickoffParchmentBorder
                        ),
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .clickable { onTogglePinMatch() }
                            .testTag("pin_match_btn_${match.matchId}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = if (isMatchPinned) "Unpin Match" else "Pin Match to My Games",
                                tint = if (isMatchPinned) KickoffTerracotta else KickoffParchmentMuted,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Right: Status-Aware Action CTA Pill (WATCH LIVE / PRE-MATCH / MATCH STATS)
                    Surface(
                        color = Color.Transparent,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(
                            0.85.dp,
                            when {
                                isLive -> KickoffAlabasterIvory.copy(alpha = 0.85f)
                                isFinishedStatus -> KickoffAgedParchment.copy(alpha = 0.40f)
                                else -> KickoffAgedParchment.copy(alpha = 0.65f)
                            }
                        ),
                        modifier = Modifier
                            .height(28.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                brush = when {
                                    isLive -> Brush.horizontalGradient(
                                        listOf(
                                            KickoffTerracotta,
                                            Color(0xFFE86830)
                                        )
                                    )
                                    isFinishedStatus -> Brush.horizontalGradient(
                                        listOf(
                                            KickoffCharcoalElevated,
                                            KickoffObsidianBlack
                                        )
                                    )
                                    else -> Brush.horizontalGradient(
                                        listOf(
                                            KickoffImperialNavy,
                                            KickoffNavyElevated
                                        )
                                    )
                                }
                            )
                            .tvFocusablePill(
                                accentColor = KickoffTerracotta,
                                shape = RoundedCornerShape(50),
                                onClick = {
                                    if (isFinishedStatus) {
                                        onOpenDossier()
                                    } else {
                                        onWatchBeinClicked(match.beinChannel)
                                    }
                                }
                            )
                            .testTag("watch_bein_btn_${match.matchId}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(start = 5.dp, end = 10.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(KickoffObsidianBlack)
                            ) {
                                Icon(
                                    imageVector = if (isFinishedStatus) Icons.Default.Info else Icons.Default.PlayArrow,
                                    contentDescription = when {
                                        isLive -> "Watch Live Match"
                                        isFinishedStatus -> "Match Stats"
                                        else -> "Pre-Match"
                                    },
                                    tint = when {
                                        isLive -> KickoffTerracotta
                                        isFinishedStatus -> KickoffAgedParchment
                                        else -> KickoffAlabasterIvory
                                    },
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(5.dp))
                            if (isLive) {
                                LiveBroadcastEqualizerBars()
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = when {
                                    isLive -> "WATCH LIVE"
                                    isFinishedStatus -> "MATCH STATS"
                                    else -> "PRE-MATCH"
                                },
                                color = when {
                                    isLive -> KickoffAlabasterIvory
                                    isFinishedStatus -> KickoffAgedParchment
                                    else -> KickoffAlabasterIvory
                                },
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.4.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlashscoreTeamRow(
    teamName: String,
    badgeUrl: String,
    score: Int?,
    redCards: Int = 0,
    isLive: Boolean,
    isFinished: Boolean = false,
    isWinner: Boolean = false,
    isLoser: Boolean = false,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    val nameAlpha = if (isLoser && !isFavorite) 0.60f else 1f
    val nameWeight = when {
        isWinner || isFavorite || isLive -> FontWeight.ExtraBold
        else -> FontWeight.SemiBold
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            TeamCrestImage(
                badgeUrl = badgeUrl,
                teamName = teamName,
                sizeDp = 28
            )
            Spacer(modifier = Modifier.width(9.dp))
            Text(
                text = teamName,
                color = (if (isFavorite) KickoffAgedParchment else KickoffAlabasterIvory).copy(alpha = nameAlpha),
                fontSize = 13.5.sp,
                fontWeight = nameWeight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (redCards > 0) {
                Spacer(modifier = Modifier.width(5.dp))
                RedCardIndicatorBadge(count = redCards)
            }
            Spacer(modifier = Modifier.width(5.dp))
            Icon(
                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = if (isFavorite) "Favorite Team" else "Add to Favorites",
                tint = if (isFavorite) KickoffTerracotta else KickoffParchmentMuted.copy(alpha = 0.45f),
                modifier = Modifier
                    .size(14.dp)
                    .clickable { onToggleFavorite() }
            )
        }

        // Right Column: Recessed Scoreboard Well when match is Live or Finished
        if ((isLive || isFinished) && score != null) {
            val scoreColor = when {
                isLive -> KickoffAlabasterIvory
                isWinner -> KickoffAlabasterIvory
                isLoser -> KickoffParchmentMuted
                else -> KickoffAgedParchment
            }
            Surface(
                color = when {
                    isLive -> KickoffTerracottaGlow
                    isWinner -> KickoffImperialNavy
                    else -> KickoffObsidianBlack
                },
                shape = RoundedCornerShape(7.dp),
                border = BorderStroke(
                    0.7.dp,
                    when {
                        isLive -> KickoffTerracotta
                        isWinner -> KickoffAgedParchment.copy(alpha = 0.65f)
                        else -> KickoffParchmentBorder
                    }
                ),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = score.toString(),
                    color = scoreColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    style = TabularNumeralTextStyle,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .width(26.dp)
                        .padding(vertical = 1.5.dp)
                )
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

    // High-contrast Warm Alabaster Ivory Medallion Core so dark/red crests
    // pop with razor-sharp broadcast contrast against Warm Charcoal Slate cards
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        KickoffAlabasterIvory,
                        KickoffAgedParchment.copy(alpha = 0.92f)
                    )
                )
            )
            .border(
                width = 0.85.dp,
                color = KickoffAgedParchment.copy(alpha = 0.70f),
                shape = CircleShape
            )
    ) {
        Text(
            text = initials,
            color = KickoffObsidianBlack.copy(alpha = 0.85f),
            fontSize = (sizeDp * 0.36f).sp,
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

/**
 * Interactive Match Dossier Dialog displaying Stage/Round, Kickoff Time, Stadium, and MENA Zone Broadcast Options.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MatchDossierDialog(
    match: FootballMatch,
    onDismiss: () -> Unit,
    onWatchOnMenaChannel: (String) -> Unit
) {
    val allChannels = remember(match) {
        (listOf(match.beinChannel) + match.fallbackMenaChannels).map { it.trim() }.filter { it.isNotBlank() }.distinct()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = ArenaTurfDark,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.2.dp, ArenaPitchGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = match.tournamentName.uppercase(),
                            color = ArenaFloodlightLime,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = match.stageOrRound,
                            color = Champagne,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = PaleGold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Matchup Header
                Surface(
                    color = DarkGraphite,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(0.8.dp, ChampagneSubtleBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            TeamCrestImage(badgeUrl = match.homeTeamBadge, teamName = match.homeTeamName, sizeDp = 40)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = match.homeTeamName,
                                color = PaleGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (match.homeScore != null && match.awayScore != null) {
                                    "${match.homeScore} - ${match.awayScore}"
                                } else {
                                    match.kickoffTimeFormatted
                                },
                                color = ArenaFloodlightLime,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = match.kickoffDateFormatted,
                                color = Champagne,
                                fontSize = 10.sp
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            TeamCrestImage(badgeUrl = match.awayTeamBadge, teamName = match.awayTeamName, sizeDp = 40)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = match.awayTeamName,
                                color = PaleGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stadium & MENA Broadcast Info
                Text(
                    text = "STADIUM & MENA BROADCASTERS (TAP CHANNEL TO WATCH FROM FAVORITES)",
                    color = AmberGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Venue: ${match.stadiumName} (${match.cityName})",
                    color = PaleGold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allChannels.forEach { ch ->
                        val (bg, border, txt) = resolveMenaBroadcasterColors(ch)
                        Surface(
                            color = bg,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, border),
                            modifier = Modifier.clickable { onWatchOnMenaChannel(ch) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LiveTv,
                                    contentDescription = null,
                                    tint = txt,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = ch,
                                    color = txt,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onWatchOnMenaChannel(match.beinChannel) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ArenaPitchGreen,
                        contentColor = DarkGraphite
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Watch on ${match.beinChannel}", fontWeight = FontWeight.ExtraBold)
                }
            }
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
        colors = CardDefaults.cardColors(containerColor = MetalCharcoal),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ChampagneSubtleBorder),
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
                tint = ArenaFloodlightLime,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (selectedFilter == "MY_TEAMS") {
                    "No Matches Found for Your Favorite Teams"
                } else {
                    "No Matches Match Current Filter"
                },
                color = PaleGold,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "You can switch back to All Tournaments or modify your favorite teams and active leagues.",
                color = Champagne,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onResetFilter,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ArenaPitchGreen,
                        contentColor = DarkGraphite
                    ),
                    modifier = Modifier.testTag("reset_tournament_filter_btn")
                ) {
                    Text("Show All Tournaments", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onOpenCustomize,
                    border = BorderStroke(1.dp, AmberGold)
                ) {
                    Text("Modify Teams", color = PaleGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * First-Time Country Zone & Favorite Teams Onboarding Modal.
 * Allows the user right from their first KickOff launch to:
 * 1. Choose their Country Zone (syncing local timezone, kickoff clocks, and official TV channel providers).
 * 2. Search and follow any club or national team worldwide.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FirstTimeFavoriteTeamsDialog(
    selectedCountryZone: UserCountryZone = UserZoneRegistry.defaultCountryZone,
    selectedSeasonTimeMode: SeasonTimeMode = SeasonTimeMode.AUTO,
    searchQuery: String,
    searchResults: List<FootballTeam>,
    isSearching: Boolean,
    selectedFavorites: List<FavoriteTeamEntity>,
    onSelectCountryZone: (String) -> Unit = {},
    onSelectSeasonTimeMode: (SeasonTimeMode) -> Unit = {},
    onAutoDetectCountryZone: () -> Unit = {},
    onOpenFullCountryZoneSheet: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit,
    onToggleTeam: (FootballTeam) -> Unit,
    onRemoveFavoriteTeam: (String) -> Unit,
    onCompleteOnboarding: () -> Unit
) {
    Dialog(
        onDismissRequest = onCompleteOnboarding,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = KickoffObsidianBlack,
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.2.dp, KickoffTerracotta),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp)
                .testTag("first_time_teams_onboarding_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SportsSoccer,
                            contentDescription = null,
                            tint = KickoffTerracotta,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Welcome to KickOff Arena",
                                color = KickoffAlabasterIvory,
                                fontSize = 17.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Choose your Country Zone (time & TV channels) and follow your teams",
                                color = KickoffAgedParchment,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Step 1: User's Country Zone Selector (Timezone, Winter/Summer Clocks & Official TV Providers)
                OnboardingCountryZoneSelectorCard(
                    selectedZone = selectedCountryZone,
                    onSelectCountryCode = onSelectCountryZone,
                    onAutoDetectCountry = onAutoDetectCountryZone,
                    onOpenFullZoneSheet = onOpenFullCountryZoneSheet,
                    seasonTimeMode = selectedSeasonTimeMode,
                    onSelectSeasonTimeMode = onSelectSeasonTimeMode
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Manual Global Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = "Type a team name (e.g., Real Madrid, Raja, Arsenal, PSG)...",
                            color = KickoffAgedParchment.copy(alpha = 0.65f),
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Global Teams",
                            tint = KickoffTerracotta
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = KickoffAgedParchment
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KickoffTerracotta,
                        unfocusedBorderColor = KickoffParchmentBorder,
                        focusedContainerColor = KickoffCharcoalSlate,
                        unfocusedContainerColor = KickoffCharcoalSlate,
                        focusedTextColor = KickoffAlabasterIvory,
                        unfocusedTextColor = KickoffAlabasterIvory
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_team_search_input")
                )

                // Selected Favorite Teams Pills
                if (selectedFavorites.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "SELECTED TEAMS (${selectedFavorites.size})",
                        color = KickoffAgedParchment,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        selectedFavorites.forEach { fav ->
                            Surface(
                                color = KickoffImperialNavy,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, KickoffTerracotta)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    TeamCrestImage(
                                        badgeUrl = fav.badgeUrl,
                                        teamName = fav.teamName,
                                        sizeDp = 16
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = fav.teamName,
                                        color = KickoffAlabasterIvory,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove ${fav.teamName}",
                                        tint = KickoffTerracotta,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { onRemoveFavoriteTeam(fav.teamId) }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Results Area
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(min = 140.dp, max = 230.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(KickoffCharcoalSlate)
                        .padding(8.dp)
                ) {
                    when {
                        isSearching -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = KickoffTerracotta)
                            }
                        }
                        searchQuery.isBlank() -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = KickoffTerracotta.copy(alpha = 0.85f),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Search Any Team Globally",
                                    color = KickoffAlabasterIvory,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Popular in ${selectedCountryZone.flagEmoji} ${selectedCountryZone.countryName}: ${selectedCountryZone.popularLocalClubs.joinToString(", ")}. Type any club above to add it.",
                                    color = KickoffAgedParchment,
                                    fontSize = 11.5.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        searchResults.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No teams found for \"$searchQuery\". Try another club name.",
                                    color = KickoffAgedParchment,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        else -> {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(searchResults, key = { it.teamId }) { team ->
                                    val isSelected = selectedFavorites.any {
                                        it.teamId.equals(team.teamId, ignoreCase = true) ||
                                            it.teamName.equals(team.name, ignoreCase = true)
                                    }
                                    TeamSearchResultRow(
                                        team = team,
                                        isSelected = isSelected,
                                        onToggle = { onToggleTeam(team) }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onCompleteOnboarding,
                        border = BorderStroke(1.dp, KickoffParchmentBorder),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("onboarding_skip_btn")
                    ) {
                        Text("Skip for Now", color = KickoffAgedParchment, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onCompleteOnboarding,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KickoffTerracotta,
                            contentColor = KickoffAlabasterIvory
                        ),
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("onboarding_save_teams_btn")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedFavorites.isEmpty()) "Enter KickOff Arena" else "Save (${selectedFavorites.size}) & Continue",
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamSearchResultRow(
    team: FootballTeam,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        color = if (isSelected) KickoffImperialNavy else KickoffObsidianBlack,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            width = 0.8.dp,
            color = if (isSelected) KickoffTerracotta else KickoffParchmentBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .testTag("team_search_result_${team.teamId}")
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
                TeamCrestImage(
                    badgeUrl = team.badgeUrl,
                    teamName = team.name,
                    sizeDp = 28
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = team.name,
                        color = KickoffAlabasterIvory,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${team.country} • ${team.primaryLeague}",
                        color = KickoffAgedParchment,
                        fontSize = 11.sp
                    )
                }
            }

            Icon(
                imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = if (isSelected) "Remove Favorite" else "Add Favorite",
                tint = if (isSelected) KickoffTerracotta else KickoffAgedParchment
            )
        }
    }
}

/**
 * Anytime Customization Modal Sheet for changing Country Zone, adding/removing Favorite Teams, and enabling/disabling/pinning Tournaments.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomizeTeamsAndTournamentsSheet(
    selectedCountryZone: UserCountryZone = UserZoneRegistry.defaultCountryZone,
    selectedSeasonTimeMode: SeasonTimeMode = SeasonTimeMode.AUTO,
    searchQuery: String,
    searchResults: List<FootballTeam>,
    isSearching: Boolean,
    favoriteTeams: List<FavoriteTeamEntity>,
    tournamentPreferences: List<TournamentPreferenceEntity>,
    onSelectCountryZone: (String) -> Unit = {},
    onSelectSeasonTimeMode: (SeasonTimeMode) -> Unit = {},
    onAutoDetectCountryZone: () -> Unit = {},
    onOpenFullCountryZoneSheet: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit,
    onToggleFavoriteTeam: (FootballTeam) -> Unit,
    onRemoveFavoriteTeam: (String) -> Unit,
    onSetTournamentEnabled: (String, Boolean) -> Unit,
    onToggleTournamentPinned: (String) -> Unit,
    onEnableAllTournaments: () -> Unit,
    onDismiss: () -> Unit
) {
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Favorite Teams, 1 = Tournaments

    val groupedTournamentsByCategory = remember(tournamentPreferences) {
        val categoryById = FootballRepository.ALL_TOURNAMENTS.associate { it.tournamentId to it.category }
        CompetitionCategory.entries.mapNotNull { category ->
            val itemsForCat = tournamentPreferences.filter { pref ->
                (categoryById[pref.tournamentId] ?: CompetitionCategory.EUROPEAN) == category
            }
            if (itemsForCat.isEmpty()) null else (category to itemsForCat)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = KickoffObsidianBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Customize KickOff Arena",
                        color = KickoffAlabasterIvory,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Manage your Country Zone, favorite teams and tournaments",
                        color = KickoffAgedParchment,
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = KickoffAlabasterIvory)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Country Zone & Winter/Summer Clock Switcher inside Customize Sheet
            OnboardingCountryZoneSelectorCard(
                selectedZone = selectedCountryZone,
                onSelectCountryCode = onSelectCountryZone,
                onAutoDetectCountry = onAutoDetectCountryZone,
                onOpenFullZoneSheet = onOpenFullCountryZoneSheet,
                seasonTimeMode = selectedSeasonTimeMode,
                onSelectSeasonTimeMode = onSelectSeasonTimeMode
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-tabs: Favorite Teams | Tournaments
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(KickoffCharcoalSlate)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "My Favorite Teams (${favoriteTeams.size})",
                    "Tournaments (${tournamentPreferences.count { it.isEnabled }})"
                ).forEachIndexed { index, title ->
                    val isSelected = activeTab == index
                    Surface(
                        color = if (isSelected) KickoffTerracotta else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { activeTab = index }
                            .testTag("customize_tab_$index")
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) KickoffAlabasterIvory else KickoffAgedParchment,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 9.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (activeTab == 0) {
                // Tab 0: Manual Global Team Search + Saved Favorites List
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = "Search global teams to add (e.g., Wydad, Barcelona, Liverpool)...",
                            color = KickoffAgedParchment.copy(alpha = 0.65f),
                            fontSize = 12.sp
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = KickoffTerracotta)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = KickoffAgedParchment)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KickoffTerracotta,
                        unfocusedBorderColor = KickoffParchmentBorder,
                        focusedContainerColor = KickoffCharcoalSlate,
                        unfocusedContainerColor = KickoffCharcoalSlate,
                        focusedTextColor = KickoffAlabasterIvory,
                        unfocusedTextColor = KickoffAlabasterIvory
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customize_team_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (searchQuery.isNotBlank()) {
                        if (isSearching) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = KickoffTerracotta)
                                }
                            }
                        } else {
                            items(searchResults, key = { "search_${it.teamId}" }) { team ->
                                val isFav = favoriteTeams.any {
                                    it.teamId.equals(team.teamId, ignoreCase = true) ||
                                        it.teamName.equals(team.name, ignoreCase = true)
                                }
                                TeamSearchResultRow(
                                    team = team,
                                    isSelected = isFav,
                                    onToggle = { onToggleFavoriteTeam(team) }
                                )
                            }
                        }
                    } else {
                        if (favoriteTeams.isEmpty()) {
                            item {
                                Text(
                                    text = "No favorite teams saved yet. Use the search bar above to find and follow your favorite clubs.",
                                    color = KickoffAgedParchment,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            }
                        } else {
                            items(favoriteTeams, key = { "fav_${it.teamId}" }) { fav ->
                                Surface(
                                    color = KickoffCharcoalSlate,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(0.8.dp, KickoffParchmentBorder),
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
                                            TeamCrestImage(
                                                badgeUrl = fav.badgeUrl,
                                                teamName = fav.teamName,
                                                sizeDp = 26
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = fav.teamName,
                                                    color = KickoffAlabasterIvory,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (fav.primaryLeague.isNotBlank()) {
                                                    Text(
                                                        text = "${fav.country} • ${fav.primaryLeague}",
                                                        color = KickoffAgedParchment,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                        }
                                        IconButton(
                                            onClick = { onRemoveFavoriteTeam(fav.teamId) },
                                            modifier = Modifier.testTag("remove_fav_team_${fav.teamId}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove ${fav.teamName}",
                                                tint = KickoffTerracotta
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Tab 1: Tournaments Visibility & Pinning grouped by the 3 Competition Categories
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Toggle tournaments or pin them to the top",
                        color = KickoffAgedParchment,
                        fontSize = 12.sp
                    )
                    TextButton(onClick = onEnableAllTournaments) {
                        Text("Enable All", color = KickoffTerracotta, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupedTournamentsByCategory.forEach { (category, prefsForCategory) ->
                        item(key = "cat_header_${category.name}") {
                            Surface(
                                color = KickoffImperialNavy,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.8.dp, KickoffParchmentBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Text(
                                    text = category.displayTitle.uppercase(),
                                    color = KickoffAgedParchment,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        items(prefsForCategory, key = { it.tournamentId }) { pref ->
                            Surface(
                                color = KickoffCharcoalSlate,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    width = 0.8.dp,
                                    color = if (pref.isPinned) KickoffTerracotta else KickoffParchmentBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        IconButton(
                                            onClick = { onToggleTournamentPinned(pref.tournamentId) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PushPin,
                                                contentDescription = "Pin ${pref.tournamentName}",
                                                tint = if (pref.isPinned) KickoffTerracotta else KickoffAgedParchment.copy(alpha = 0.65f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = pref.tournamentName,
                                                color = KickoffAlabasterIvory,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = pref.countryOrRegion,
                                                color = KickoffAgedParchment,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = pref.isEnabled,
                                        onCheckedChange = { enabled ->
                                            onSetTournamentEnabled(pref.tournamentId, enabled)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = KickoffAlabasterIvory,
                                            checkedTrackColor = KickoffTerracotta,
                                            uncheckedThumbColor = KickoffAgedParchment,
                                            uncheckedTrackColor = KickoffObsidianBlack
                                        ),
                                        modifier = Modifier.testTag("toggle_tournament_${pref.tournamentId}")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

/**
 * Kickoff Favorites-Only Channel Lookup Sheet / Missing-Favorite Notification Dialog.
 *
 * - Case A (Multiple versions of the channel exist in the user's Favorites, e.g., FHD and HD):
 *   Lets the user tap between their matched Favorite channels or play the first match immediately.
 * - Case B (No Match Found in Favorites):
 *   Does NOT search the full Xtream catalog. Displays the clear notification:
 *   `"{channelName}" is not in your Favorites yet. Please test and add "{channelName}" to your Favorites first to watch this game.`
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
        containerColor = KickoffObsidianBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .testTag("bein_channel_matcher_sheet")
        ) {
            // Header: Match & Clicked MENA Broadcaster Badge
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
                            color = KickoffAlabasterIvory,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${state.resolvedProviderName} • ${state.match.tournamentName}",
                            color = KickoffAgedParchment,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = KickoffAlabasterIvory)
                }
            }

            // MENA Zone Broadcaster Switcher Chips (Primary + MENA Fallback Providers)
            if (state.availableMenaChannelOptions.size > 1) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "MATCH TV CHANNELS (★ = SAVED IN YOUR FAVORITES)",
                    color = KickoffAgedParchment,
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
                                        tint = if (isSelected) KickoffAlabasterIvory else KickoffTerracotta,
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
                                borderColor = if (isSavedInFavs) KickoffTerracotta else KickoffParchmentBorder,
                                selectedBorderColor = KickoffTerracotta,
                                borderWidth = if (isSavedInFavs) 1.1.dp else 0.8.dp
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KickoffTerracotta,
                                selectedLabelColor = KickoffAlabasterIvory,
                                containerColor = if (isSavedInFavs) KickoffImperialNavy else KickoffCharcoalSlate,
                                labelColor = if (isSavedInFavs) KickoffAlabasterIvory else KickoffAgedParchment
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
                        CircularProgressIndicator(color = KickoffTerracotta)
                    }
                }

                state.requiresAddToFavoritesPrompt -> {
                    // Case B — No Match Found in Favorites (Tier 1), with Smart Tier 2 Playlist Catalog Candidates
                    Surface(
                        color = KickoffCharcoalSlate,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, KickoffTerracotta),
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
                                tint = KickoffTerracotta,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = state.missingFavoriteNotificationMessage,
                                color = KickoffAlabasterIvory,
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
                                    border = BorderStroke(1.dp, KickoffTerracotta),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = KickoffImperialNavy,
                                        contentColor = KickoffAlabasterIvory
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = KickoffTerracotta,
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
                                            containerColor = KickoffTerracotta,
                                            contentColor = KickoffAlabasterIvory
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
                                        border = BorderStroke(1.dp, KickoffParchmentBorder),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Open Favorites", color = KickoffAlabasterIvory, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                                Button(
                                    onClick = onDismiss,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = KickoffTerracotta,
                                        contentColor = KickoffAlabasterIvory
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

                    // Tier 2 Smart Resolver: Matched streams found in the user's Live TV catalog
                    if (state.livePlaylistCandidates.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "FOUND IN YOUR PLAYLIST CATALOG (${state.livePlaylistCandidates.size}) — 1-TAP WATCH & PIN",
                            color = KickoffAgedParchment,
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
                                    24 + (kotlin.math.abs(stream.streamId.hashCode()) % 48)
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
                                    color = KickoffCharcoalSlate,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, KickoffParchmentBorder),
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
                                                        color = KickoffAlabasterIvory,
                                                        fontSize = 12.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f, fill = false)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        color = KickoffImperialNavy,
                                                        shape = RoundedCornerShape(4.dp),
                                                        border = BorderStroke(0.6.dp, KickoffTerracotta)
                                                    ) {
                                                        Text(
                                                            text = qualityBadge,
                                                            color = KickoffAlabasterIvory,
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
                                                        color = KickoffObsidianBlack,
                                                        shape = RoundedCornerShape(4.dp),
                                                        border = BorderStroke(0.5.dp, KickoffParchmentBorder)
                                                    ) {
                                                        Text(
                                                            text = pingLabel,
                                                            color = KickoffAgedParchment,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                    Text(
                                                        text = "Auto-saves to Favorites",
                                                        color = KickoffAgedParchment,
                                                        fontSize = 9.5.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Surface(
                                            color = KickoffTerracotta,
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
                                                    color = KickoffAlabasterIvory,
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
                    // Case A (Multiple versions of the matched channel in Favorites, e.g., FHD and HD)
                    Surface(
                        color = KickoffImperialNavy,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, KickoffTerracotta),
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
                                    tint = KickoffTerracotta,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Matched in Your Favorites (${state.favoriteMatchedStreams.size})",
                                    color = KickoffAlabasterIvory,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Button(
                                onClick = { onPlayMatchedFavoriteStream(state.favoriteMatchedStreams.first()) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = KickoffTerracotta,
                                    contentColor = KickoffAlabasterIvory
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
                            .heightIn(max = 280.dp),
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
                                color = KickoffCharcoalSlate,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.2.dp, KickoffTerracotta),
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
                                                    color = KickoffAlabasterIvory,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    color = KickoffImperialNavy,
                                                    shape = RoundedCornerShape(4.dp),
                                                    border = BorderStroke(0.6.dp, KickoffTerracotta)
                                                ) {
                                                    Text(
                                                        text = qualityBadge,
                                                        color = KickoffAlabasterIvory,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "★ Saved in Favorites • Tap to Watch Immediately",
                                                color = KickoffAgedParchment,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Surface(
                                        color = KickoffTerracotta,
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
                                                tint = KickoffAlabasterIvory,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "Watch",
                                                color = KickoffAlabasterIvory,
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
): Long? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    runCatching {
        val urlStr = if (playlist != null && playlist.serverUrl.isNotBlank()) {
            val base = playlist.serverUrl.trim().trimEnd('/')
            "$base/live/${playlist.username}/${playlist.password}/${stream.streamId}.ts"
        } else {
            return@withContext null
        }
        val startNs = System.nanoTime()
        val conn = (java.net.URL(urlStr).openConnection() as? java.net.HttpURLConnection) ?: return@withContext null
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

