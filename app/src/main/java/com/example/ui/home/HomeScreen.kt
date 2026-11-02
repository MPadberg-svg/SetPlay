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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
import com.example.data.repository.FootballMatch
import com.example.data.repository.FootballTeam
import com.example.data.repository.LiveScoreSyncState
import com.example.data.repository.MatchDayTab
import com.example.data.repository.MatchStatus
import com.example.data.repository.SyncProgress
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
import com.example.ui.theme.KickoffCeruleanBlue
import com.example.ui.theme.KickoffCoralRed
import com.example.ui.theme.KickoffOceanBase
import com.example.ui.theme.KickoffOceanElevated
import com.example.ui.theme.KickoffOceanSurface
import com.example.ui.theme.KickoffPeachCream
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

    val favoriteTeams by mainViewModel.favoriteTeams.collectAsStateWithLifecycle()
    val tournamentPreferences by mainViewModel.tournamentPreferences.collectAsStateWithLifecycle()
    val showFirstTimeOnboarding by mainViewModel.showFirstTimeTeamsOnboarding.collectAsStateWithLifecycle()
    val showCustomizeModal by mainViewModel.showCustomizeTeamsModal.collectAsStateWithLifecycle()
    val selectedMatchDay by mainViewModel.selectedMatchDay.collectAsStateWithLifecycle()
    val selectedTournamentFilter by mainViewModel.selectedTournamentFilter.collectAsStateWithLifecycle()
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

    val activeDayMatches = matchesByDay[selectedMatchDay].orEmpty()
    val liveMatchesCount = todayMatches.count { it.status == MatchStatus.LIVE || it.status == MatchStatus.HALFTIME }

    val enabledTournamentMap = remember(tournamentPreferences) {
        tournamentPreferences.associateBy { it.tournamentId }
    }

    // Filter matches according to enabled tournaments & active filter chip
    val visibleMatches = remember(
        activeDayMatches,
        enabledTournamentMap,
        selectedTournamentFilter,
        favoriteTeams
    ) {
        activeDayMatches.filter { match ->
            val pref = enabledTournamentMap[match.tournamentId]
            val isTournamentEnabled = pref?.isEnabled ?: true
            if (!isTournamentEnabled && selectedTournamentFilter == "ALL") {
                return@filter false
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
                        KickoffOceanBase,
                        KickoffOceanSurface.copy(alpha = 0.85f),
                        KickoffOceanBase
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
                selectedMatchDay = selectedMatchDay,
                totalVisibleMatchesCount = sortedVisibleMatches.size,
                activeLeaguesInSlate = activeLeaguesInSlate,
                enabledLeaguesCount = enabledLeaguesCount,
                upcomingMatchesCount = upcomingMatchesCount,
                favoriteTeamsCount = favoriteTeams.size,
                liveMatchesCount = liveMatchesCount,
                isLoadingMatches = isLoadingMatches,
                liveScoreSyncState = liveScoreSyncState,
                onRefreshMatches = { mainViewModel.refreshFootballSchedules() },
                onOpenCustomizeModal = { mainViewModel.openCustomizeTeamsModal() },
                onManagePlaylists = { onNavigateScreen(AppScreen.PLAYLISTS) },
                onSyncPlaylist = { mainViewModel.syncActiveAccount() }
            )
        }

        // 3 & 4. Sticky Frosted Matchday Scrubber + Tournament Filter Dock on Scroll
        stickyHeader(key = "sticky_matchday_and_tournament_bar") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                KickoffOceanBase.copy(alpha = 0.98f),
                                KickoffOceanBase.copy(alpha = 0.95f),
                                KickoffOceanSurface.copy(alpha = 0.92f)
                            )
                        )
                    )
            ) {
                FiveDayMatchTimelineRibbon(
                    selectedDay = selectedMatchDay,
                    matchesByDay = matchesByDay,
                    liveCountToday = liveMatchesCount,
                    onSelectDay = { mainViewModel.selectMatchDay(it) }
                )

                TournamentFilterRow(
                    selectedFilter = selectedTournamentFilter,
                    favoriteTeamsCount = favoriteTeams.size,
                    liveCount = activeDayMatches.count { it.status == MatchStatus.LIVE || it.status == MatchStatus.HALFTIME },
                    tournamentPreferences = tournamentPreferences,
                    onSelectFilter = { mainViewModel.selectTournamentFilter(it) },
                    onOpenCustomize = { mainViewModel.openCustomizeTeamsModal() }
                )

                // Subtle bottom specular gradient hairline separating sticky dock from scrolling fixtures
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    KickoffTropicalTeal.copy(alpha = 0.40f),
                                    KickoffPeachCream.copy(alpha = 0.25f),
                                    KickoffCoralRed.copy(alpha = 0.35f),
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
                            mainViewModel.selectTournamentFilter("ALL")
                            mainViewModel.enableAllTournaments()
                        },
                        onOpenCustomize = { mainViewModel.openCustomizeTeamsModal() }
                    )
                }
            } else {
                val spotlightMatch = run {
                    val allVisible = chronologicalSections.flatMap { it.matches }
                    allVisible.firstOrNull {
                        (it.status == MatchStatus.LIVE || it.status == MatchStatus.HALFTIME) &&
                            isMatchForFavoriteTeams(it, favoriteTeams)
                    } ?: allVisible.firstOrNull {
                        it.status == MatchStatus.LIVE || it.status == MatchStatus.HALFTIME
                    } ?: allVisible.firstOrNull {
                        isMatchForFavoriteTeams(it, favoriteTeams)
                    } ?: allVisible.firstOrNull()
                }

                if (spotlightMatch != null) {
                    item(key = "spotlight_${spotlightMatch.matchId}") {
                        SpotlightMatchOfTheNightCard(
                            match = spotlightMatch,
                            onOpenDossier = { mainViewModel.openMatchCenterDossier(spotlightMatch) },
                            onWatchMatch = { clickedChannel ->
                                mainViewModel.onWatchMatchOnBeinClicked(
                                    match = spotlightMatch,
                                    clickedChannelName = clickedChannel,
                                    onAutoPlaySingleStream = onStreamClick
                                )
                            }
                        )
                    }
                }

                chronologicalSections.forEach { section ->
                    val tournamentId = section.tournamentId
                    val matchesInSection = section.matches
                    val firstMatch = matchesInSection.first()
                    val pref = enabledTournamentMap[tournamentId]
                    val isCollapsed = collapsedTournamentIds.contains(tournamentId)
                    val isPinned = pref?.isPinned == true

                    item(key = "header_${section.sectionKey}") {
                        FlashscoreTournamentHeader(
                            tournamentId = tournamentId,
                            tournamentName = firstMatch.tournamentName,
                            regionName = firstMatch.tournamentRegion,
                            menaProviderName = firstMatch.menaProviderName,
                            matchCount = matchesInSection.size,
                            isPinned = isPinned,
                            isCollapsed = isCollapsed,
                            onToggleCollapse = { mainViewModel.toggleTournamentCollapsed(tournamentId) },
                            onTogglePin = { mainViewModel.toggleTournamentPinned(tournamentId) }
                        )
                    }

                    if (!isCollapsed) {
                        items(
                            items = matchesInSection,
                            key = { "match_${it.matchId}" }
                        ) { match ->
                            FlashscoreMatchCard(
                                match = match,
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
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // 1. First-Time Favorite Teams Onboarding Modal (Manual Global Search Only)
    // =========================================================================
    if (showFirstTimeOnboarding) {
        FirstTimeFavoriteTeamsDialog(
            searchQuery = teamSearchQuery,
            searchResults = teamSearchResults,
            isSearching = isSearchingTeams,
            selectedFavorites = favoriteTeams,
            onSearchQueryChange = { mainViewModel.updateTeamSearchQuery(it) },
            onToggleTeam = { mainViewModel.toggleFavoriteTeam(it) },
            onRemoveFavoriteTeam = { mainViewModel.removeFavoriteTeam(it) },
            onCompleteOnboarding = { mainViewModel.completeFirstTimeTeamsOnboarding() }
        )
    }

    // =========================================================================
    // 2. Anytime "My Teams & Tournaments" Customization Bottom Sheet
    // =========================================================================
    if (showCustomizeModal) {
        CustomizeTeamsAndTournamentsSheet(
            searchQuery = teamSearchQuery,
            searchResults = teamSearchResults,
            isSearching = isSearchingTeams,
            favoriteTeams = favoriteTeams,
            tournamentPreferences = tournamentPreferences,
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
    val context = LocalContext.current
    beinMatcherState?.let { matcherState ->
        LaunchedEffect(matcherState.resolvedChannelLabel, matcherState.requiresAddToFavoritesPrompt, matcherState.isLoading) {
            if (!matcherState.isLoading && matcherState.requiresAddToFavoritesPrompt) {
                Toast.makeText(
                    context,
                    matcherState.missingFavoriteNotificationMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        BeinChannelMatcherBottomSheet(
            state = matcherState,
            activePlaylist = activePlaylist,
            onPlayMatchedFavoriteStream = { stream ->
                mainViewModel.dismissBeinChannelMatcher()
                onStreamClick(stream)
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
 * Resolves the signature MENA sports broadcaster badge color scheme
 * (beIN Sports, Arryadia / SNRT / Tamazight, SSC Sports, AD Sports / STARZPLAY, Alkass).
 */
private fun resolveMenaBroadcasterColors(channelLabel: String): Triple<Color, Color, Color> {
    val lower = channelLabel.lowercase()
    return when {
        lower.contains("arryadia") || lower.contains("snrt") || lower.contains("tamazight") || lower.contains("maghribia") ->
            Triple(MenaArryadiaDark, MenaArryadiaRed, Color(0xFFFFD6D8))
        lower.contains("ssc") || lower.contains("shahid") ->
            Triple(MenaSscDark, MenaSscTeal, Color(0xFFCCFBF1))
        lower.contains("ad sport") || lower.contains("starz") || lower.contains("alkass") || lower.contains("al kass") ->
            Triple(MenaAdSportsDark, MenaAdSportsBlue, Color(0xFFDBEAFE))
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
    "laliga",
    "premier_league",
    "ligue_1",
    "bundesliga",
    "serie_a",
    "caf_cl",
    "botola_pro",
    "uefa_el",
    "saudi_pro"
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
 * Matchday Intelligence & Broadcast Telemetry Bar (replaces the duplicate section title below the logo).
 * Top row: Selected Matchday & Calendar Date, Fixture/League slate summary, live sync telemetry dot, live/upcoming pill, and refresh button.
 * Bottom row: Tier-2 Secondary Surface Controls ("Favorite Teams & Leagues", "Link IPTV / IPTV Ready") + 4-segment brand spectrum bar.
 */
@Composable
private fun KickOffArenaHeaderCard(
    activePlaylist: PlaylistEntity?,
    selectedMatchDay: MatchDayTab = MatchDayTab.TODAY,
    totalVisibleMatchesCount: Int = 0,
    activeLeaguesInSlate: Int = 0,
    enabledLeaguesCount: Int = 10,
    upcomingMatchesCount: Int = 0,
    favoriteTeamsCount: Int,
    liveMatchesCount: Int,
    isLoadingMatches: Boolean,
    liveScoreSyncState: LiveScoreSyncState = LiveScoreSyncState(),
    onRefreshMatches: () -> Unit,
    onOpenCustomizeModal: () -> Unit,
    onManagePlaylists: () -> Unit,
    onSyncPlaylist: () -> Unit
) {
    val dayTitle = remember(selectedMatchDay) {
        selectedMatchDay.getDynamicTitle().uppercase()
    }
    val dayDate = remember(selectedMatchDay) {
        selectedMatchDay.getDynamicDateSubtitle().uppercase()
    }
    val slateSummaryText = remember(totalVisibleMatchesCount, activeLeaguesInSlate) {
        when {
            totalVisibleMatchesCount > 0 && activeLeaguesInSlate > 0 ->
                "$totalVisibleMatchesCount GAMES • $activeLeaguesInSlate ${if (activeLeaguesInSlate == 1) "LEAGUE" else "LEAGUES"}"
            totalVisibleMatchesCount > 0 ->
                "$totalVisibleMatchesCount FIXTURES"
            else ->
                "MENA TV GUIDE"
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = KickoffOceanSurface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 0.8.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    KickoffCeruleanBlue.copy(alpha = 0.4f),
                    KickoffTropicalTeal.copy(alpha = 0.35f),
                    KickoffCoralRed.copy(alpha = 0.4f)
                )
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("match_center_hero_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            KickoffOceanSurface,
                            KickoffOceanElevated.copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(horizontal = 12.dp, vertical = 9.dp)
        ) {
            // Top Row: Matchday Date & Slate Telemetry + Auto-Sync Status + Live/Upcoming Pill + Refresh Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Active Matchday",
                        tint = KickoffTropicalTeal,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "$dayTitle • $dayDate",
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.4.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                    // Slate count & league breakdown pill
                    Surface(
                        color = KickoffTropicalTeal.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.6.dp, KickoffTropicalTeal.copy(alpha = 0.40f))
                    ) {
                        Text(
                            text = slateSummaryText,
                            color = KickoffPeachCream,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                    // Inline live sync status indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (liveScoreSyncState.isSyncingDelta) AmberGold else KickoffTropicalTeal
                                )
                        )
                        Text(
                            text = when {
                                liveScoreSyncState.isSyncingDelta -> "SYNCING..."
                                liveScoreSyncState.lastSyncedTimeFormatted.isNotBlank() ->
                                    "SYNC ${liveScoreSyncState.lastSyncedTimeFormatted}"
                                else -> "GMT+1"
                            },
                            color = ChampagneMuted,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (liveMatchesCount > 0) {
                        Surface(
                            color = LiveCrimson.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.6.dp, LiveCrimson.copy(alpha = 0.55f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(LiveCrimson)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$liveMatchesCount LIVE",
                                    color = LiveCrimson,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    } else if (upcomingMatchesCount > 0) {
                        Surface(
                            color = Color.White.copy(alpha = 0.06f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.6.dp, Color.White.copy(alpha = 0.14f))
                        ) {
                            Text(
                                text = "$upcomingMatchesCount UPCOMING",
                                color = Champagne,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Tier 3: Borderless Ghost Refresh Icon Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onRefreshMatches)
                            .testTag("refresh_matches_btn")
                    ) {
                        if (isLoadingMatches || liveScoreSyncState.isSyncingDelta) {
                            CircularProgressIndicator(
                                color = KickoffTropicalTeal,
                                strokeWidth = 1.8.dp,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Matches",
                                tint = Champagne,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(7.dp))

            // Bottom Row: Tier 2 Secondary Surface Controls with live team/league & IPTV telemetry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = KickoffTropicalTeal.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.8.dp, KickoffTropicalTeal.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onOpenCustomizeModal)
                        .testTag("customize_teams_tournaments_btn")
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .height(30.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = KickoffTropicalTeal,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (favoriteTeamsCount > 0) {
                                "Teams ($favoriteTeamsCount) • $enabledLeaguesCount Leagues"
                            } else {
                                "Favorite Teams & Leagues ($enabledLeaguesCount)"
                            },
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    color = Color.White.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.8.dp, AmberGold.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = if (activePlaylist == null) onManagePlaylists else onSyncPlaylist)
                        .testTag("match_center_playlist_btn")
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 11.dp)
                            .height(30.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (activePlaylist != null) CatchupGreen else AmberGold)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = null,
                            tint = AmberGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = activePlaylist?.name?.take(12)?.ifBlank { "IPTV Ready" } ?: "Link IPTV",
                            color = PaleGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Compact 4-Segment Brand Status / Pagination Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                listOf(
                    KickoffCeruleanBlue,
                    KickoffTropicalTeal,
                    KickoffPeachCream,
                    KickoffCoralRed
                ).forEach { swatch ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .clip(RoundedCornerShape(50))
                            .background(swatch.copy(alpha = 0.85f))
                    )
                }
            }
        }
    }
}

/**
 * Sleek Horizontal 5-Day Date Strip (40% reduced vertical height).
 * Tier 2 Secondary Surface Controls:
 * - Active (e.g., "Today"): subtle cyan-tinted background (bg-cyan-500/15) + crisp 1dp cyan border + bold white text.
 * - Inactive: subtle elevated dark surface (bg-white/5) with no border.
 */
/**
 * Architectural 5-Day Matchday Broadcast Scrubber Deck:
 * - Each day renders as a sculpted 3-tier vertical broadcast card inside a recessed stadium track:
 *   1. Top Floodlight Beam (on selected day) + Tracked-out Day Eyebrow ("TODAY", "TOMORROW") + Live Pulse Pill
 *   2. Hero Calendar Date Numeral (15.5sp ExtraBold)
 *   3. Recessed Bottom Fixture Counter Pill ("27 MATCHES")
 */
@Composable
private fun FiveDayMatchTimelineRibbon(
    selectedDay: MatchDayTab,
    matchesByDay: Map<MatchDayTab, List<FootballMatch>>,
    liveCountToday: Int,
    onSelectDay: (MatchDayTab) -> Unit
) {
    Surface(
        color = Color(0xFF05131B).copy(alpha = 0.85f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("five_day_timeline_ribbon")
        ) {
            items(MatchDayTab.entries, key = { it.name }) { tab ->
                val isSelected = selectedDay == tab
                val dayMatches = matchesByDay[tab].orEmpty()
                val count = dayMatches.size
                val isTodayTab = tab == MatchDayTab.TODAY
                val tag = when (tab) {
                    MatchDayTab.TODAY -> "tab_today_games"
                    MatchDayTab.TOMORROW -> "tab_tomorrow_games"
                    else -> "tab_${tab.name.lowercase()}"
                }

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val cardScale by animateFloatAsState(
                    targetValue = if (isPressed) 0.95f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "day_tab_scale_${tab.name}"
                )

                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(14.dp),
                    border = if (isSelected) {
                        BorderStroke(
                            width = 1.3.dp,
                            brush = Brush.verticalGradient(
                                listOf(
                                    KickoffPeachCream,
                                    KickoffTropicalTeal,
                                    KickoffCeruleanBlue
                                )
                            )
                        )
                    } else {
                        BorderStroke(0.8.dp, Color.White.copy(alpha = 0.08f))
                    },
                    modifier = Modifier
                        .width(106.dp)
                        .graphicsLayer {
                            scaleX = cardScale
                            scaleY = cardScale
                        }
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            brush = if (isSelected) {
                                Brush.verticalGradient(
                                    listOf(
                                        KickoffTropicalTeal.copy(alpha = 0.34f),
                                        KickoffCeruleanBlue.copy(alpha = 0.22f),
                                        KickoffOceanSurface
                                    )
                                )
                            } else {
                                Brush.verticalGradient(
                                    listOf(
                                        KickoffOceanElevated.copy(alpha = 0.55f),
                                        KickoffOceanBase.copy(alpha = 0.75f)
                                    )
                                )
                            }
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onSelectDay(tab) }
                        .testTag(tag)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top Floodlight Indicator Beam
                        Box(
                            modifier = Modifier
                                .width(if (isSelected) 34.dp else 16.dp)
                                .height(2.5.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isSelected) {
                                        Brush.horizontalGradient(
                                            listOf(KickoffPeachCream, KickoffTropicalTeal)
                                        )
                                    } else {
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.12f),
                                                Color.White.copy(alpha = 0.12f)
                                            )
                                        )
                                    }
                                )
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        // Tier 1: Uppercase Day Title + Live Beacon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = tab.getDynamicTitle().uppercase(),
                                color = if (isSelected) KickoffPeachCream else ChampagneMuted,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.7.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                            if (isTodayTab && liveCountToday > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(LiveCrimson)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Tier 2: Hero Calendar Date Numeral
                        Text(
                            text = tab.getDynamicDateSubtitle(),
                            color = if (isSelected) Color.White else Champagne,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            softWrap = false
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        // Tier 3: Recessed Match Count Capsule
                        Surface(
                            color = if (isSelected) {
                                KickoffTropicalTeal
                            } else if (isTodayTab && liveCountToday > 0) {
                                LiveCrimson.copy(alpha = 0.20f)
                            } else {
                                Color.Black.copy(alpha = 0.32f)
                            },
                            shape = RoundedCornerShape(50),
                            border = if (!isSelected && isTodayTab && liveCountToday > 0) {
                                BorderStroke(0.6.dp, LiveCrimson.copy(alpha = 0.6f))
                            } else {
                                null
                            }
                        ) {
                            Text(
                                text = if (isTodayTab && liveCountToday > 0 && !isSelected) {
                                    "$liveCountToday LIVE • $count"
                                } else {
                                    "$count games"
                                },
                                color = when {
                                    isSelected -> DarkGraphite
                                    isTodayTab && liveCountToday > 0 -> Color.White
                                    else -> ChampagneMuted
                                },
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Horizontal Tournament Filter Strip:
 * - Tier 1 (Illuminated Stadium Gradient Capsule) for the active tournament pill
 * - Tier 2 (Sculpted Frosted Glass Pill with League Accent Ring) for inactive pills
 * - Preserves canonical ordering: All Tournaments, UEFA Champions League, La Liga, Premier League, Ligue 1,
 *   Bundesliga, Serie A, CAF Champions League, Botola Pro, then others after.
 */
@Composable
private fun TournamentFilterRow(
    selectedFilter: String,
    favoriteTeamsCount: Int,
    liveCount: Int,
    tournamentPreferences: List<TournamentPreferenceEntity>,
    onSelectFilter: (String) -> Unit,
    onOpenCustomize: () -> Unit
) {
    val enabledTournaments = remember(tournamentPreferences) {
        tournamentPreferences
            .filter { it.isEnabled }
            .sortedWith(
                compareBy<TournamentPreferenceEntity> {
                    getTournamentCanonicalPriority(it.tournamentId, it.sortOrder)
                }.thenBy {
                    it.sortOrder
                }
            )
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
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
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = if (isAllSelected) DarkGraphite else KickoffTropicalTeal,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "All Tournaments",
                        color = if (isAllSelected) DarkGraphite else Color.White,
                        fontSize = 12.sp,
                        fontWeight = if (isAllSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                    if (liveCount > 0) {
                        Surface(
                            color = LiveCrimson,
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
                                        .background(Color.White)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "$liveCount LIVE",
                                    color = Color.White,
                                    fontSize = 9.sp,
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
                else -> tournament.tournamentName
            }
            val leagueDotColor = remember(tournament.tournamentId) {
                resolveTournamentAccentDotColor(tournament.tournamentId)
            }
            LuxuryLeagueCapsule(
                selected = isSelected,
                accentColor = leagueDotColor,
                onClick = { onSelectFilter(tournament.tournamentId) },
                modifier = Modifier.testTag("filter_chip_${tournament.tournamentId}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) DarkGraphite.copy(alpha = 0.22f)
                                else leagueDotColor.copy(alpha = 0.24f)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.5.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) DarkGraphite else leagueDotColor)
                        )
                    }
                    Text(
                        text = chipLabel,
                        color = if (isSelected) DarkGraphite else Champagne,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
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
                        tint = AmberGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Modify Leagues",
                        color = PaleGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

private fun resolveTournamentAccentDotColor(tournamentId: String): Color {
    return when (tournamentId) {
        "uefa_cl" -> KickoffTropicalTeal
        "laliga" -> KickoffCoralRed
        "premier_league" -> Color(0xFFA78BFA)
        "ligue_1" -> KickoffPeachCream
        "bundesliga" -> Color(0xFFF87171)
        "serie_a" -> Color(0xFF38BDF8)
        "caf_cl" -> AmberGold
        "botola_pro" -> Color(0xFF34D399)
        else -> KickoffTropicalTeal
    }
}

@Composable
private fun LuxuryLeagueCapsule(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = KickoffTropicalTeal,
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
            selected -> BorderStroke(1.2.dp, KickoffPeachCream.copy(alpha = 0.75f))
            isActionCapsule -> BorderStroke(0.9.dp, AmberGold.copy(alpha = 0.40f))
            else -> BorderStroke(0.8.dp, Color.White.copy(alpha = 0.11f))
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
                            Color(0xFF46DFB1),
                            KickoffTropicalTeal,
                            KickoffCeruleanBlue
                        )
                    )
                    isActionCapsule -> Brush.horizontalGradient(
                        listOf(
                            AmberGold.copy(alpha = 0.16f),
                            KickoffOceanElevated.copy(alpha = 0.85f)
                        )
                    )
                    else -> Brush.horizontalGradient(
                        listOf(
                            KickoffOceanElevated.copy(alpha = 0.75f),
                            KickoffOceanSurface.copy(alpha = 0.90f)
                        )
                    )
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.5.dp),
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
                Text(
                    text = "No scheduled matches for your favorite teams on ${selectedDay.getDynamicTitle()} (${selectedDay.getDynamicDateSubtitle()}). Try another day in the 5-day timeline above!",
                    color = Champagne,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                )
            } else {
                favoriteMatches.forEach { match ->
                    HorizontalDivider(color = ChampagneSubtleBorder, thickness = 0.5.dp)
                    FlashscoreMatchCard(
                        match = match,
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
private fun FlashscoreTournamentHeader(
    tournamentId: String,
    tournamentName: String,
    regionName: String,
    menaProviderName: String,
    matchCount: Int,
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
            color = Color.White.copy(alpha = 0.09f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp)
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
                        KickoffOceanElevated,
                        KickoffOceanSurface.copy(alpha = 0.95f)
                    )
                )
            )
            .clickable { onToggleCollapse() }
            .testTag("tournament_header_$tournamentId")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Signature League Vertical Accent Bar + Trophy Medallion + Region & League Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(42.dp)
                        .background(leagueAccentColor)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(leagueAccentColor.copy(alpha = 0.16f))
                        .border(0.7.dp, leagueAccentColor.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = leagueAccentColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(9.dp))
                Column(modifier = Modifier.padding(vertical = 7.dp)) {
                    Text(
                        text = regionName.uppercase(),
                        color = ChampagneMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = tournamentName,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right: Pin Icon, Match Count Badge, and Chevron
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("pin_tournament_$tournamentId")
                ) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pin Tournament",
                        tint = if (isPinned) AmberGold else ChampagneMuted.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                }

                Surface(
                    color = Color(0xFF06131B),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.7.dp, leagueAccentColor.copy(alpha = 0.40f))
                ) {
                    Text(
                        text = "$matchCount",
                        color = KickoffPeachCream,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isCollapsed) "Expand" else "Collapse",
                    tint = Champagne,
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer { rotationZ = chevronRotation }
                )
            }
        }
    }
}

/**
 * Editorial "Match of the Night" / Spotlight Clash Stadium Banner:
 * Highlights the #1 marquee match with backlit 38dp team crests, central digital stadium scoreboard box,
 * and the new Dual-Stage Broadcast Launch Deck.
 */
@Composable
private fun SpotlightMatchOfTheNightCard(
    match: FootballMatch,
    onOpenDossier: () -> Unit,
    onWatchMatch: (String) -> Unit
) {
    val isLive = match.status == MatchStatus.LIVE || match.status == MatchStatus.HALFTIME
    val isFinished = match.status == MatchStatus.FINISHED
    val (badgeBg, badgeAccent, badgeText) = resolveMenaBroadcasterColors(match.beinChannel)

    Card(
        colors = CardDefaults.cardColors(containerColor = KickoffOceanElevated),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = 1.3.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    if (isLive) LiveCrimson else KickoffTropicalTeal,
                    KickoffPeachCream.copy(alpha = 0.55f),
                    KickoffCoralRed
                )
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 7.dp)
            .clickable { onOpenDossier() }
            .testTag("spotlight_match_card_${match.matchId}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            KickoffOceanElevated,
                            KickoffOceanSurface,
                            Color(0xFF06151E)
                        )
                    )
                )
        ) {
            // Dual-Club Ambient Stadium Lighting (Home Left Teal Spotlight + Away Right Coral/Gold Spotlight)
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            KickoffTropicalTeal.copy(alpha = 0.20f),
                            KickoffCeruleanBlue.copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.14f, size.height * 0.46f),
                        radius = size.width * 0.42f
                    ),
                    radius = size.width * 0.42f,
                    center = Offset(size.width * 0.14f, size.height * 0.46f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            KickoffCoralRed.copy(alpha = 0.18f),
                            AmberGold.copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.86f, size.height * 0.46f),
                        radius = size.width * 0.42f
                    ),
                    radius = size.width * 0.42f,
                    center = Offset(size.width * 0.86f, size.height * 0.46f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // Top Editorial Eyebrow: SPOTLIGHT CLASH Pill + Tournament Name + Stadium
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = if (isLive) LiveCrimson else AmberGold.copy(alpha = 0.20f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(
                                0.7.dp,
                                if (isLive) Color.White.copy(alpha = 0.35f) else AmberGold.copy(alpha = 0.65f)
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(if (isLive) Color.White else AmberGold)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isLive) "LIVE SPOTLIGHT" else "FEATURED CLASH",
                                    color = if (isLive) Color.White else PaleGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.7.sp
                                )
                            }
                        }
                        Text(
                            text = match.tournamentName,
                            color = KickoffPeachCream,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = match.stadiumName,
                        color = ChampagneMuted,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Face-Off Row: Home Team + Form Strip (Left) vs Recessed Digital Scoreboard Box (Center) vs Away Team + Form Strip (Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Home Team + 5-Match Form Guide
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(KickoffTropicalTeal.copy(alpha = 0.12f))
                                .border(0.8.dp, KickoffTropicalTeal.copy(alpha = 0.38f), CircleShape)
                        ) {
                            TeamCrestImage(
                                badgeUrl = match.homeTeamBadge,
                                teamName = match.homeTeamName,
                                sizeDp = 34
                            )
                        }
                        Spacer(modifier = Modifier.width(9.dp))
                        Column {
                            Text(
                                text = match.homeTeamName,
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            ClubFormMicroStrip(teamName = match.homeTeamName, alignEnd = false)
                        }
                    }

                    // Center Recessed Digital Stadium Scoreboard Well
                    Surface(
                        color = Color(0xFF051118),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            0.9.dp,
                            if (isLive) LiveCrimson.copy(alpha = 0.65f) else KickoffTropicalTeal.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            if ((isLive || isFinished) && match.homeScore != null && match.awayScore != null) {
                                Text(
                                    text = "${match.homeScore} : ${match.awayScore}",
                                    color = if (isLive) Color.White else KickoffPeachCream,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = match.liveMinuteOrBadge,
                                    color = if (isLive) LiveCrimson else ChampagneMuted,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            } else {
                                Text(
                                    text = match.liveMinuteOrBadge,
                                    color = KickoffPeachCream,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "GMT+1",
                                    color = KickoffTropicalTeal,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Away Team + 5-Match Form Guide
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Text(
                                text = match.awayTeamName,
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            ClubFormMicroStrip(teamName = match.awayTeamName, alignEnd = true)
                        }
                        Spacer(modifier = Modifier.width(9.dp))
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(KickoffCoralRed.copy(alpha = 0.12f))
                                .border(0.8.dp, KickoffCoralRed.copy(alpha = 0.38f), CircleShape)
                        ) {
                            TeamCrestImage(
                                badgeUrl = match.awayTeamBadge,
                                teamName = match.awayTeamName,
                                sizeDp = 34
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(11.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(9.dp))

                // Footer: Match Dossier Pill on left + Dual-Stage Broadcast Launch Deck on right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = KickoffTropicalTeal,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Dossier & Lineups",
                            color = Champagne,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    SplitBroadcasterWatchCapsule(
                        broadcasterName = match.beinChannel,
                        badgeBg = badgeBg,
                        badgeAccent = badgeAccent,
                        badgeText = badgeText,
                        isLive = isLive,
                        watchTestTag = "spotlight_watch_btn_${match.matchId}",
                        onWatchClick = { onWatchMatch(match.beinChannel) }
                    )
                }
            }
        }
    }
}

/**
 * 5-Match Club Form Micro-Strip (W • D • L) displayed below club names in the Featured Clash card.
 */
@Composable
private fun ClubFormMicroStrip(
    teamName: String,
    alignEnd: Boolean
) {
    val formSequence = remember(teamName) {
        val hash = kotlin.math.abs(teamName.hashCode())
        val patterns = listOf(
            listOf('W', 'W', 'D', 'W', 'W'),
            listOf('W', 'D', 'W', 'L', 'W'),
            listOf('D', 'W', 'W', 'W', 'D'),
            listOf('W', 'L', 'W', 'W', 'D'),
            listOf('W', 'W', 'L', 'D', 'W')
        )
        patterns[hash % patterns.size]
    }
    Row(
        horizontalArrangement = if (alignEnd) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        formSequence.forEachIndexed { idx, result ->
            val badgeColor = when (result) {
                'W' -> Color(0xFF2EBD85)
                'D' -> AmberGold
                else -> KickoffCoralRed
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(13.dp)
                    .clip(RoundedCornerShape(3.5.dp))
                    .background(badgeColor.copy(alpha = 0.22f))
                    .border(0.6.dp, badgeColor.copy(alpha = 0.70f), RoundedCornerShape(3.5.dp))
            ) {
                Text(
                    text = result.toString(),
                    color = badgeColor,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            if (idx < formSequence.lastIndex) {
                Spacer(modifier = Modifier.width(2.5.dp))
            }
        }
    }
}

private fun parseLiveMatchProgressFraction(liveBadge: String, status: MatchStatus): Float {
    if (status == MatchStatus.HALFTIME || liveBadge.equals("HT", ignoreCase = true)) {
        return 0.5f
    }
    val digits = liveBadge.takeWhile { it.isDigit() }.toIntOrNull()
    if (digits != null) {
        return (digits.toFloat() / 90f).coerceIn(0.06f, 1.0f)
    }
    return 0.5f
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
    watchTestTag: String,
    onWatchClick: () -> Unit
) {
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
        color = badgeBg.copy(alpha = 0.92f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    badgeAccent.copy(alpha = 0.75f),
                    KickoffTropicalTeal.copy(alpha = 0.85f)
                )
            )
        ),
        shadowElevation = 6.dp,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .height(36.dp)
            .clip(RoundedCornerShape(50))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 10.dp, end = 3.dp, top = 3.dp, bottom = 3.dp)
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
                    .padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LiveTv,
                    contentDescription = null,
                    tint = badgeText,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = broadcasterName,
                    color = badgeText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = if (isBeinSourceChannel) 148.dp else 118.dp)
                )
                if (!isBeinSourceChannel) {
                    Spacer(modifier = Modifier.width(5.dp))
                    Surface(
                        color = Color.Black.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(
                            0.6.dp,
                            if (isUltraHdChannel) AmberGold.copy(alpha = 0.7f) else badgeText.copy(alpha = 0.35f)
                        )
                    ) {
                        Text(
                            text = if (isUltraHdChannel) "4K" else "HD",
                            color = if (isUltraHdChannel) AmberGold else badgeText,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            // Stage 2: Illuminated Floodlight Watch CTA Pill with Circular Play Medallion
            Surface(
                color = Color.Transparent,
                shape = RoundedCornerShape(50),
                border = BorderStroke(
                    0.9.dp,
                    if (isLive) KickoffPeachCream.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.40f)
                ),
                modifier = Modifier
                    .height(30.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        brush = if (isLive) {
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF80EE98),
                                    Color(0xFF09D1C7),
                                    KickoffTropicalTeal
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF46DFB1),
                                    KickoffTropicalTeal,
                                    KickoffCeruleanBlue
                                )
                            )
                        }
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onWatchClick
                    )
                    .testTag(watchTestTag)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(start = 5.dp, end = 11.dp)
                ) {
                    // Recessed Dark Circular Play Medallion
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(DarkGraphite)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Watch Match",
                            tint = if (isLive) Color(0xFF80EE98) else KickoffTropicalTeal,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    if (isLive) {
                        LiveBroadcastEqualizerBars()
                        Spacer(modifier = Modifier.width(5.dp))
                    }
                    Text(
                        text = if (isLive) "WATCH LIVE" else "WATCH",
                        color = DarkGraphite,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
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
        modifier = Modifier.height(11.dp)
    ) {
        listOf(bar1Fraction, bar2Fraction, bar3Fraction).forEach { fraction ->
            Box(
                modifier = Modifier
                    .width(2.2.dp)
                    .height((11f * fraction).dp)
                    .clip(RoundedCornerShape(50))
                    .background(LiveCrimson)
            )
        }
    }
}

@Composable
private fun FlashscoreMatchCard(
    match: FootballMatch,
    isHomeTeamFavorite: Boolean,
    isAwayTeamFavorite: Boolean,
    onToggleHomeFavorite: () -> Unit,
    onToggleAwayFavorite: () -> Unit,
    onOpenDossier: () -> Unit,
    onWatchBeinClicked: (String) -> Unit,
    isInsidePinnedSection: Boolean = false
) {
    val isLive = match.status == MatchStatus.LIVE || match.status == MatchStatus.HALFTIME
    val isFinished = match.status == MatchStatus.FINISHED
    val hasFavTeam = isHomeTeamFavorite || isAwayTeamFavorite
    val (badgeBg, badgeAccent, badgeText) = resolveMenaBroadcasterColors(match.beinChannel)

    val homeIsWinner = isFinished && match.homeScore != null && match.awayScore != null && match.homeScore > match.awayScore
    val awayIsWinner = isFinished && match.homeScore != null && match.awayScore != null && match.awayScore > match.homeScore
    val hasDecisiveWinner = homeIsWinner || awayIsWinner

    val leftSpineColor = when {
        isLive -> LiveCrimson
        hasFavTeam -> AmberGold
        else -> KickoffTropicalTeal.copy(alpha = 0.55f)
    }

    Surface(
        color = if (isInsidePinnedSection) Color.Transparent else KickoffOceanSurface,
        border = if (isInsidePinnedSection) {
            null
        } else {
            BorderStroke(
                width = 1.dp,
                color = if (isLive) LiveCrimson.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.07f)
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isInsidePinnedSection) 0.dp else 16.dp)
            .clickable { onOpenDossier() }
            .testTag("match_card_${match.matchId}")
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left Stadium Status Spine
            if (!isInsidePinnedSection) {
                Box(
                    modifier = Modifier
                        .width(3.5.dp)
                        .height(118.dp)
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
                                    LiveCrimson.copy(alpha = 0.09f),
                                    Color.Transparent
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, Color.Transparent)
                            )
                        }
                    )
                    .padding(horizontal = 13.dp, vertical = 11.dp)
            ) {
                // Match Main Row: Recessed Digital Clock Pod (Left) + Dual Team Scoreboard Rows (Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Left Column (62dp wide): Recessed Digital Stadium Clock Pod
                    Surface(
                        color = Color(0xFF06131B),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(
                            width = 0.8.dp,
                            color = when {
                                isLive -> LiveCrimson.copy(alpha = 0.65f)
                                isFinished -> Color.White.copy(alpha = 0.08f)
                                else -> KickoffTropicalTeal.copy(alpha = 0.28f)
                            }
                        ),
                        modifier = Modifier.width(62.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                text = match.liveMinuteOrBadge,
                                color = when (match.status) {
                                    MatchStatus.LIVE, MatchStatus.HALFTIME -> LiveCrimson
                                    MatchStatus.FINISHED -> ChampagneMuted
                                    MatchStatus.UPCOMING -> Color.White
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val subStatusText = when {
                                match.halfTimeScore != null && (isLive || isFinished) ->
                                    "HT ${match.halfTimeScore}"
                                isLive -> "● LIVE"
                                isFinished -> "FT"
                                else -> "GMT+1"
                            }
                            Text(
                                text = subStatusText,
                                color = when {
                                    match.halfTimeScore != null && (isLive || isFinished) -> AmberGold
                                    isLive -> LiveCrimson
                                    else -> KickoffTropicalTeal.copy(alpha = 0.85f)
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // 2. Center & Right Columns: Two teams stacked vertically with 25dp crests and Scoreboard Well
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
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

                // Match Action Footer: Live 90-Minute Progress Hairline when LIVE, or subtle hairline when scheduled
                Spacer(modifier = Modifier.height(10.dp))
                if (isLive) {
                    val progressFraction = remember(match.liveMinuteOrBadge, match.status) {
                        parseLiveMatchProgressFraction(match.liveMinuteOrBadge, match.status)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressFraction)
                                .height(2.5.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(KickoffTropicalTeal, KickoffPeachCream, LiveCrimson)
                                    )
                                )
                        )
                    }
                } else {
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.07f),
                        thickness = 1.dp
                    )
                }
                Spacer(modifier = Modifier.height(9.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left side: Stadium location pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenDossier() }
                            .testTag("open_match_center_${match.matchId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Stadium",
                            tint = KickoffTropicalTeal.copy(alpha = 0.85f),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = match.stadiumName,
                            color = Champagne,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Right side: Dual-Stage Broadcast Launch Deck
                    SplitBroadcasterWatchCapsule(
                        broadcasterName = match.beinChannel,
                        badgeBg = badgeBg,
                        badgeAccent = badgeAccent,
                        badgeText = badgeText,
                        isLive = isLive,
                        watchTestTag = "watch_bein_btn_${match.matchId}",
                        onWatchClick = { onWatchBeinClicked(match.beinChannel) }
                    )
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
    val nameAlpha = if (isLoser && !isFavorite) 0.58f else 1f
    val nameWeight = when {
        isWinner || isFavorite || isLive -> FontWeight.Bold
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
                sizeDp = 25
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = teamName,
                color = (if (isFavorite) AmberGold else Color.White).copy(alpha = nameAlpha),
                fontSize = 14.5.sp,
                fontWeight = nameWeight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (redCards > 0) {
                Spacer(modifier = Modifier.width(5.dp))
                RedCardIndicatorBadge(count = redCards)
            }
            if (isFavorite) {
                Spacer(modifier = Modifier.width(5.dp))
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Favorite Team",
                    tint = AmberGold,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { onToggleFavorite() }
                )
            }
        }

        // Right Column: Recessed Scoreboard Well when match is Live or Finished
        if ((isLive || isFinished) && score != null) {
            val scoreColor = when {
                isLive -> Color.White
                isLoser -> ChampagneMuted
                else -> KickoffPeachCream
            }
            Surface(
                color = if (isLive) LiveCrimson.copy(alpha = 0.22f) else Color(0xFF06131B),
                shape = RoundedCornerShape(7.dp),
                border = BorderStroke(
                    0.7.dp,
                    if (isLive) LiveCrimson.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.09f)
                ),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = score.toString(),
                    color = scoreColor,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .width(26.dp)
                        .padding(vertical = 2.dp)
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

    // Subtle Team Crest Ambient Backlighting (10% radial halo + 14% white hairline ring)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.14f),
                        MetalCharcoalElevated
                    )
                )
            )
            .border(0.7.dp, Color.White.copy(alpha = 0.14f), CircleShape)
    ) {
        Text(
            text = initials,
            color = AmberGold,
            fontSize = (sizeDp * 0.42f).sp,
            fontWeight = FontWeight.ExtraBold
        )
        if (badgeUrl.isNotBlank()) {
            AsyncImage(
                model = badgeUrl,
                contentDescription = teamName,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size((sizeDp - 2).dp)
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
 * First-Time Favorite Teams Onboarding Modal.
 * Features manual search across all global teams without pre-grouped Moroccan favorites.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FirstTimeFavoriteTeamsDialog(
    searchQuery: String,
    searchResults: List<FootballTeam>,
    isSearching: Boolean,
    selectedFavorites: List<FavoriteTeamEntity>,
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
            color = MetalCharcoal,
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.2.dp, ArenaPitchGreen),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 640.dp)
                .testTag("first_time_teams_onboarding_dialog")
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SportsSoccer,
                            contentDescription = null,
                            tint = ArenaFloodlightLime,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Welcome to KickOff Arena",
                                color = PaleGold,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Search any club or national team worldwide to follow their games",
                                color = Champagne,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Manual Global Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = "Type a team name (e.g., Real Madrid, Raja, Arsenal, PSG)...",
                            color = Champagne.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Global Teams",
                            tint = ArenaFloodlightLime
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = Champagne
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArenaPitchGreen,
                        unfocusedBorderColor = ChampagneSubtleBorder,
                        focusedContainerColor = DarkGraphite,
                        unfocusedContainerColor = DarkGraphite,
                        focusedTextColor = PaleGold,
                        unfocusedTextColor = PaleGold
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_team_search_input")
                )

                // Selected Favorite Teams Pills
                if (selectedFavorites.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "SELECTED TEAMS (${selectedFavorites.size})",
                        color = ArenaFloodlightLime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        selectedFavorites.forEach { fav ->
                            Surface(
                                color = ArenaPitchGreen.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, ArenaPitchGreen)
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
                                        color = PaleGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove ${fav.teamName}",
                                        tint = ArenaFloodlightLime,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { onRemoveFavoriteTeam(fav.teamId) }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Results Area
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(min = 180.dp, max = 300.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkGraphite)
                        .padding(8.dp)
                ) {
                    when {
                        isSearching -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = ArenaPitchGreen)
                            }
                        }
                        searchQuery.isBlank() -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = ArenaFloodlightLime.copy(alpha = 0.7f),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Search Any Team Globally",
                                    color = PaleGold,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Enter a club or national team name above to search live global football directories and add them to KickOff Arena.",
                                    color = Champagne,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        searchResults.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No teams found for \"$searchQuery\". Try another club name.",
                                    color = Champagne,
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

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onCompleteOnboarding,
                        border = BorderStroke(1.dp, ChampagneSubtleBorder),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("onboarding_skip_btn")
                    ) {
                        Text("Skip for Now", color = Champagne, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onCompleteOnboarding,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ArenaPitchGreen,
                            contentColor = DarkGraphite
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
        color = if (isSelected) ArenaPitchGreen.copy(alpha = 0.16f) else MetalCharcoal,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            width = 0.8.dp,
            color = if (isSelected) ArenaPitchGreen else ChampagneSubtleBorder
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
                        color = PaleGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${team.country} • ${team.primaryLeague}",
                        color = Champagne,
                        fontSize = 11.sp
                    )
                }
            }

            Icon(
                imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = if (isSelected) "Remove Favorite" else "Add Favorite",
                tint = if (isSelected) AmberGold else Champagne
            )
        }
    }
}

/**
 * Anytime Customization Modal Sheet for adding/removing Favorite Teams and enabling/disabling/pinning Tournaments.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomizeTeamsAndTournamentsSheet(
    searchQuery: String,
    searchResults: List<FootballTeam>,
    isSearching: Boolean,
    favoriteTeams: List<FavoriteTeamEntity>,
    tournamentPreferences: List<TournamentPreferenceEntity>,
    onSearchQueryChange: (String) -> Unit,
    onToggleFavoriteTeam: (FootballTeam) -> Unit,
    onRemoveFavoriteTeam: (String) -> Unit,
    onSetTournamentEnabled: (String, Boolean) -> Unit,
    onToggleTournamentPinned: (String) -> Unit,
    onEnableAllTournaments: () -> Unit,
    onDismiss: () -> Unit
) {
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Favorite Teams, 1 = Tournaments

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MetalCharcoal
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
                        color = PaleGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Manage your favorite teams and visible tournaments",
                        color = Champagne,
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = PaleGold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-tabs: Favorite Teams | Tournaments
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkGraphite)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "My Favorite Teams (${favoriteTeams.size})",
                    "Tournaments (${tournamentPreferences.count { it.isEnabled }})"
                ).forEachIndexed { index, title ->
                    val isSelected = activeTab == index
                    Surface(
                        color = if (isSelected) ArenaPitchGreen else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { activeTab = index }
                            .testTag("customize_tab_$index")
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) DarkGraphite else PaleGold,
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
                            color = Champagne.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = ArenaFloodlightLime)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Champagne)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArenaPitchGreen,
                        unfocusedBorderColor = ChampagneSubtleBorder,
                        focusedContainerColor = DarkGraphite,
                        unfocusedContainerColor = DarkGraphite,
                        focusedTextColor = PaleGold,
                        unfocusedTextColor = PaleGold
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
                                    CircularProgressIndicator(color = ArenaPitchGreen)
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
                                    color = Champagne,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            }
                        } else {
                            items(favoriteTeams, key = { "fav_${it.teamId}" }) { fav ->
                                Surface(
                                    color = DarkGraphite,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(0.8.dp, ArenaPitchGreen.copy(alpha = 0.45f)),
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
                                                    color = PaleGold,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (fav.primaryLeague.isNotBlank()) {
                                                    Text(
                                                        text = "${fav.country} • ${fav.primaryLeague}",
                                                        color = Champagne,
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
                                                tint = LiveRed
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Tab 1: Tournaments Visibility & Pinning
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Toggle tournaments or pin them to the top",
                        color = Champagne,
                        fontSize = 12.sp
                    )
                    TextButton(onClick = onEnableAllTournaments) {
                        Text("Enable All", color = ArenaFloodlightLime, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tournamentPreferences, key = { it.tournamentId }) { pref ->
                        Surface(
                            color = DarkGraphite,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                width = 0.8.dp,
                                color = if (pref.isPinned) ArenaPitchGreen else ChampagneSubtleBorder
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
                                            tint = if (pref.isPinned) ArenaTrophyGold else ChampagneMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = pref.tournamentName,
                                            color = PaleGold,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = pref.countryOrRegion,
                                            color = Champagne,
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
                                        checkedThumbColor = DarkGraphite,
                                        checkedTrackColor = ArenaPitchGreen,
                                        uncheckedThumbColor = Champagne,
                                        uncheckedTrackColor = MetalCharcoal
                                    ),
                                    modifier = Modifier.testTag("toggle_tournament_${pref.tournamentId}")
                                )
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
    onSelectMenaChannelOption: (String) -> Unit,
    onConnectPlaylist: () -> Unit,
    onOpenFavoritesScreen: () -> Unit,
    onDismiss: () -> Unit
) {
    val (badgeBg, badgeBorder, badgeText) = resolveMenaBroadcasterColors(state.resolvedChannelLabel)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MetalCharcoal
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp)
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
                            color = PaleGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${state.resolvedProviderName} • ${state.match.tournamentName}",
                            color = ArenaFloodlightLime,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = PaleGold)
                }
            }

            // MENA Zone Broadcaster Switcher Chips (Primary + MENA Fallback Providers)
            if (state.availableMenaChannelOptions.size > 1) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "MATCH TV CHANNELS (TAP TO CHECK IN YOUR FAVORITES)",
                    color = ChampagneMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.availableMenaChannelOptions, key = { it }) { optionLabel ->
                        val isSelected = optionLabel.equals(state.resolvedChannelLabel, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectMenaChannelOption(optionLabel) },
                            label = {
                                Text(
                                    text = optionLabel,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ArenaPitchGreen,
                                selectedLabelColor = DarkGraphite,
                                containerColor = DarkGraphite,
                                labelColor = PaleGold
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
                        CircularProgressIndicator(color = ArenaPitchGreen)
                    }
                }

                state.requiresAddToFavoritesPrompt -> {
                    // Case B — No Match Found in Favorites:
                    // Display clear notification that explicitly mentions the clicked channel name
                    Surface(
                        color = ArenaTurfDark,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, AmberGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_channel_to_favorites_prompt_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = AmberGold,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.missingFavoriteNotificationMessage,
                                color = PaleGold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (activePlaylist == null) {
                                    Button(
                                        onClick = onConnectPlaylist,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ArenaPitchGreen,
                                            contentColor = DarkGraphite
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
                                        border = BorderStroke(1.dp, AmberGold),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Open Favorites", color = PaleGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                                Button(
                                    onClick = onDismiss,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AmberGold,
                                        contentColor = DarkGraphite
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
                }

                else -> {
                    // Case A (Multiple versions of the matched channel in Favorites, e.g., FHD and HD)
                    Surface(
                        color = ArenaTurfElevated,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, ArenaPitchGreen),
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
                                    tint = ArenaFloodlightLime,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Matched in Your Favorites (${state.favoriteMatchedStreams.size})",
                                    color = PaleGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Button(
                                onClick = { onPlayMatchedFavoriteStream(state.favoriteMatchedStreams.first()) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ArenaPitchGreen,
                                    contentColor = DarkGraphite
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
                                color = ArenaTurfDark,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.2.dp, AmberGold),
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
                                                    color = PaleGold,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    color = ArenaPitchGreen.copy(alpha = 0.22f),
                                                    shape = RoundedCornerShape(4.dp),
                                                    border = BorderStroke(0.6.dp, ArenaPitchGreen)
                                                ) {
                                                    Text(
                                                        text = qualityBadge,
                                                        color = ArenaFloodlightLime,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "★ Saved in Favorites • Tap to Watch Immediately",
                                                color = AmberGold,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Surface(
                                        color = ArenaPitchGreen,
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
                                                tint = DarkGraphite,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "Watch",
                                                color = DarkGraphite,
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
