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
                    activeDayMatches = activeDayMatches,
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
                                isLastInSection = index == matchesInSection.lastIndex
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
 * Streamlined Matchday & Section Details Bar (free of decorative color lines, Link IPTV clutter, and duplicate titles).
 * Combines the active matchday badge, live/upcoming match counts, slate & league metrics, and quick Teams/Leagues customization + refresh controls.
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
                "$totalVisibleMatchesCount Matches • $activeLeaguesInSlate ${if (activeLeaguesInSlate == 1) "League" else "Leagues"}"
            totalVisibleMatchesCount > 0 ->
                "$totalVisibleMatchesCount Fixtures"
            else ->
                "No Fixtures Scheduled"
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = KickoffOceanSurface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 0.8.dp,
            color = KickoffTropicalTeal.copy(alpha = 0.32f)
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
                            KickoffOceanElevated.copy(alpha = 0.88f)
                        )
                    )
                )
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            // Top Row: Matchday Pill (Left) + Customize & Refresh Controls (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = KickoffTropicalTeal.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.7.dp, KickoffTropicalTeal.copy(alpha = 0.48f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Active Matchday",
                            tint = KickoffTropicalTeal,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "$dayTitle • $dayDate",
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.3.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = KickoffTropicalTeal.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.8.dp, KickoffTropicalTeal.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .height(30.dp)
                            .clip(RoundedCornerShape(10.dp))
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
                                contentDescription = "Customize Teams and Leagues",
                                tint = KickoffTropicalTeal,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (favoriteTeamsCount > 0) {
                                    "Teams ($favoriteTeamsCount)"
                                } else {
                                    "Teams & Leagues"
                                },
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.06f))
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
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            // Bottom Row: Full Untruncated Slate Telemetry, Live Count Pill, and Active Tournaments
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        text = slateSummaryText,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    if (liveMatchesCount > 0) {
                        Surface(
                            color = LiveCrimson.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.6.dp, LiveCrimson.copy(alpha = 0.65f))
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
                                    text = "$liveMatchesCount LIVE NOW",
                                    color = Color.White,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    } else if (upcomingMatchesCount > 0) {
                        Text(
                            text = "• $upcomingMatchesCount Upcoming",
                            color = KickoffPeachCream,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Text(
                    text = if (favoriteTeamsCount > 0) {
                        "$favoriteTeamsCount Fav • $enabledLeaguesCount Tournaments"
                    } else {
                        "$enabledLeaguesCount Active Tournaments"
                    },
                    color = ChampagneMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
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
    val infiniteTransition = rememberInfiniteTransition(label = "dateBarLivePulse")
    val liveDotScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liveDotScale"
    )
    val liveDotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liveDotAlpha"
    )

    Surface(
        color = Color(0xFF05131B).copy(alpha = 0.90f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(0.9.dp, Color.White.copy(alpha = 0.09f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
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
                val pillScale by animateFloatAsState(
                    targetValue = when {
                        isPressed -> 0.94f
                        isSelected -> 1.02f
                        else -> 1f
                    },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "day_pill_scale_${tab.name}"
                )
                val horizontalPad by animateDpAsState(
                    targetValue = if (isSelected) 13.dp else 10.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "day_pill_pad_${tab.name}"
                )
                val indicatorWidth by animateDpAsState(
                    targetValue = if (isSelected) 22.dp else 0.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "day_pill_indicator_${tab.name}"
                )

                val compactDayTitle = remember(tab) {
                    when (tab) {
                        MatchDayTab.YESTERDAY -> "YEST"
                        MatchDayTab.TODAY -> "TODAY"
                        MatchDayTab.TOMORROW -> "TOM"
                        else -> tab.getDynamicTitle().take(3).uppercase()
                    }
                }
                val dateNumeral = remember(tab) {
                    tab.getDynamicDateSubtitle()
                }

                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(50),
                    border = if (isSelected) {
                        BorderStroke(
                            width = 1.1.dp,
                            brush = Brush.horizontalGradient(
                                listOf(
                                    KickoffPeachCream,
                                    KickoffTropicalTeal,
                                    KickoffCeruleanBlue
                                )
                            )
                        )
                    } else {
                        BorderStroke(0.7.dp, Color.White.copy(alpha = 0.07f))
                    },
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = pillScale
                            scaleY = pillScale
                        }
                        .clip(RoundedCornerShape(50))
                        .background(
                            brush = if (isSelected) {
                                Brush.horizontalGradient(
                                    listOf(
                                        KickoffTropicalTeal.copy(alpha = 0.34f),
                                        KickoffCeruleanBlue.copy(alpha = 0.25f),
                                        KickoffOceanSurface
                                    )
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(
                                        KickoffOceanElevated.copy(alpha = 0.50f),
                                        KickoffOceanBase.copy(alpha = 0.65f)
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
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = horizontalPad, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            if (isTodayTab && liveCountToday > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .graphicsLayer {
                                            scaleX = liveDotScale
                                            scaleY = liveDotScale
                                            alpha = liveDotAlpha
                                        }
                                        .clip(CircleShape)
                                        .background(LiveCrimson)
                                )
                            }

                            Text(
                                text = compactDayTitle,
                                color = if (isSelected) KickoffPeachCream else ChampagneMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp,
                                maxLines = 1,
                                softWrap = false
                            )

                            Text(
                                text = dateNumeral,
                                color = if (isSelected) Color.White else Champagne.copy(alpha = 0.88f),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false
                            )

                            Surface(
                                color = when {
                                    isSelected -> KickoffTropicalTeal
                                    isTodayTab && liveCountToday > 0 -> LiveCrimson.copy(alpha = 0.22f)
                                    else -> Color.Black.copy(alpha = 0.35f)
                                },
                                shape = RoundedCornerShape(50),
                                border = if (!isSelected && isTodayTab && liveCountToday > 0) {
                                    BorderStroke(0.6.dp, LiveCrimson.copy(alpha = 0.65f))
                                } else {
                                    null
                                }
                            ) {
                                Text(
                                    text = "$count",
                                    color = when {
                                        isSelected -> DarkGraphite
                                        isTodayTab && liveCountToday > 0 -> Color.White
                                        else -> ChampagneMuted
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }

                        if (indicatorWidth > 0.dp) {
                            Spacer(modifier = Modifier.height(2.5.dp))
                            Box(
                                modifier = Modifier
                                    .width(indicatorWidth)
                                    .height(2.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(KickoffPeachCream, KickoffTropicalTeal)
                                        )
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
 * Horizontal Tournament Filter Strip with Real Tournament Crest Medallions:
 * - Replaces plain colored dots with official high-res league/tournament crests inside a glowing circular medallion.
 * - Shows a compact live match badge or fixture count pill per tournament.
 */
@Composable
private fun TournamentFilterRow(
    selectedFilter: String,
    favoriteTeamsCount: Int,
    liveCount: Int,
    activeDayMatches: List<FootballMatch> = emptyList(),
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
                                if (isAllSelected) DarkGraphite.copy(alpha = 0.18f)
                                else KickoffTropicalTeal.copy(alpha = 0.16f)
                            )
                            .border(
                                0.8.dp,
                                if (isAllSelected) DarkGraphite.copy(alpha = 0.35f)
                                else KickoffTropicalTeal.copy(alpha = 0.55f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = if (isAllSelected) DarkGraphite else KickoffTropicalTeal,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Text(
                        text = "All Tournaments",
                        color = if (isAllSelected) DarkGraphite else Color.White,
                        fontSize = 11.5.sp,
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
                        color = if (isSelected) DarkGraphite else Champagne,
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                    if (liveInLeague > 0) {
                        Surface(
                            color = LiveCrimson,
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
                                        .background(Color.White)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "$liveInLeague",
                                    color = Color.White,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    } else if (totalInLeague > 0) {
                        Surface(
                            color = if (isSelected) DarkGraphite.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "$totalInLeague",
                                color = if (isSelected) DarkGraphite else ChampagneMuted,
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
                        tint = AmberGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Modify Leagues",
                        color = PaleGold,
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
        "botola_pro" -> "https://media.api-sports.io/football/leagues/200.png"
        "uefa_el" -> "https://media.api-sports.io/football/leagues/3.png"
        "saudi_pro" -> "https://media.api-sports.io/football/leagues/307.png"
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
        "botola_pro" -> "BOT"
        "uefa_el" -> "UEL"
        "saudi_pro" -> "SPL"
        else -> tournamentName.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1).uppercase() }.ifBlank { "cup" }
    }
}

/**
 * Circular Crest Medallion rendering the real tournament logo over a crisp illuminated porcelain/glass disc
 * so dark and multi-color official league emblems remain razor-sharp on both dark and highlighted chips.
 */
@Composable
internal fun TournamentLogoMedallion(
    tournamentId: String,
    tournamentName: String,
    isSelected: Boolean = false,
    accentColor: Color = KickoffTropicalTeal,
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
                        Color.White.copy(alpha = 0.94f),
                        Color(0xFFE6F0F5).copy(alpha = 0.88f)
                    )
                )
            )
            .border(
                width = if (isSelected) 1.2.dp else 0.9.dp,
                color = if (isSelected) DarkGraphite.copy(alpha = 0.55f) else accentColor.copy(alpha = 0.75f),
                shape = CircleShape
            )
    ) {
        Text(
            text = monogram,
            color = DarkGraphite.copy(alpha = 0.75f),
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
            selected -> BorderStroke(1.2.dp, KickoffPeachCream.copy(alpha = 0.80f))
            isActionCapsule -> BorderStroke(0.9.dp, AmberGold.copy(alpha = 0.40f))
            else -> BorderStroke(0.8.dp, accentColor.copy(alpha = 0.28f))
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
                            KickoffOceanElevated.copy(alpha = 0.80f),
                            KickoffOceanSurface.copy(alpha = 0.92f)
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
            color = if (liveCount > 0) LiveCrimson.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.09f)
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
                        KickoffOceanElevated,
                        KickoffOceanSurface.copy(alpha = 0.96f)
                    )
                )
            )
            .clickable { onToggleCollapse() }
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
                        .height(40.dp)
                        .background(if (liveCount > 0) LiveCrimson else leagueAccentColor)
                )
                Spacer(modifier = Modifier.width(10.dp))
                TournamentLogoMedallion(
                    tournamentId = tournamentId,
                    tournamentName = tournamentName,
                    isSelected = isPinned,
                    accentColor = leagueAccentColor,
                    sizeDp = 28
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = regionName.uppercase(),
                            color = ChampagneMuted,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                        if (menaProviderName.isNotBlank()) {
                            Text(
                                text = "• ${menaProviderName.uppercase()}",
                                color = leagueAccentColor.copy(alpha = 0.85f),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
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

            // Right: Live Badge (if active), Pin Icon, Match Count Badge, and Chevron
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (liveCount > 0) {
                    Surface(
                        color = LiveCrimson.copy(alpha = 0.20f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.7.dp, LiveCrimson.copy(alpha = 0.70f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(4.5.dp)
                                    .clip(CircleShape)
                                    .background(LiveCrimson)
                            )
                            Spacer(modifier = Modifier.width(3.5.dp))
                            Text(
                                text = "$liveCount LIVE",
                                color = Color.White,
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
                        tint = if (isPinned) AmberGold else ChampagneMuted.copy(alpha = 0.65f),
                        modifier = Modifier.size(13.5.dp)
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
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 7.5.dp, vertical = 1.5.dp)
                    )
                }

                Spacer(modifier = Modifier.width(5.dp))

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

internal data class LiveMatchClockTelemetry(
    val periodBadge: String,       // "1ST HALF", "2ND HALF", "HALF TIME", "FINAL", "KICKOFF"
    val heroReadout: String,       // "86'", "41'", "HT", "FT", "20:00"
    val bottomBadge: String,       // "HT 0-1", "LIVE MIN", "GMT+1", "FULL TIME"
    val statusExplanation: String, // "2ND HALF • 86' PLAYED", "1ST HALF • 41' PLAYED"
    val remainingLabel: String,    // "4' TO 90'", "4' TO HT", "BREAK"
    val progressFraction: Float    // 0.06f..1.0f
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

internal fun parseLiveMatchClockTelemetry(match: FootballMatch): LiveMatchClockTelemetry {
    val clean = match.liveMinuteOrBadge.trim()
    return when (match.status) {
        MatchStatus.HALFTIME -> {
            val htScore = match.halfTimeScore?.takeIf { it.isNotBlank() }
            LiveMatchClockTelemetry(
                periodBadge = "HALF TIME",
                heroReadout = "HT",
                bottomBadge = if (htScore != null) "HT $htScore" else "45' PLAYED",
                statusExplanation = "HALF-TIME INTERVAL • 45' COMPLETED",
                remainingLabel = if (htScore != null) "HT: $htScore" else "2ND HALF SOON",
                progressFraction = 0.50f
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
                    progressFraction = 0.50f
                )
            }
            val apostropheMatch = Regex("(\\d{1,3})(\\+\\d{1,2})?\\s*'").find(clean)
            val withoutHalfPrefix = clean.replace(Regex("\\b[12]H\\b", RegexOption.IGNORE_CASE), "")
            val fallbackMin = Regex("\\d{1,3}").find(withoutHalfPrefix)?.value?.toIntOrNull()
            val baseMinute = apostropheMatch?.groupValues?.getOrNull(1)?.toIntOrNull() ?: fallbackMin ?: 45
            val stoppageSuffix = apostropheMatch?.groupValues?.getOrNull(2).orEmpty()
            val heroMin = "${baseMinute}${stoppageSuffix}'"

            val isSecondHalf = clean.contains("2H", ignoreCase = true) || baseMinute > 45
            val periodLabel = when {
                baseMinute > 90 -> "EXTRA TIME"
                stoppageSuffix.isNotEmpty() -> "STOPPAGE"
                isSecondHalf -> "2ND HALF"
                else -> "1ST HALF"
            }
            val explanation = when {
                stoppageSuffix.isNotEmpty() && isSecondHalf -> "2ND HALF • $heroMin ADDED TIME"
                stoppageSuffix.isNotEmpty() -> "1ST HALF • $heroMin ADDED TIME"
                isSecondHalf -> "2ND HALF • ${baseMinute}' PLAYED"
                else -> "1ST HALF • ${baseMinute}' PLAYED"
            }
            val remaining = when {
                stoppageSuffix.isNotEmpty() -> "STOPPAGE TIME"
                isSecondHalf -> {
                    val left = (90 - baseMinute).coerceAtLeast(1)
                    "${left}' TO 90'"
                }
                else -> {
                    val left = (45 - baseMinute).coerceAtLeast(1)
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
                progressFraction = parseLiveMatchProgressFraction(clean, match.status)
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
                progressFraction = 1.0f
            )
        }
        MatchStatus.UPCOMING -> {
            LiveMatchClockTelemetry(
                periodBadge = "KICKOFF",
                heroReadout = clean.ifBlank { match.kickoffTimeFormatted },
                bottomBadge = "GMT+1",
                statusExplanation = "UPCOMING KICKOFF",
                remainingLabel = "SCHEDULED",
                progressFraction = 0f
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
        shadowElevation = 4.dp,
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
                    tint = badgeText,
                    modifier = Modifier.size(11.5.dp)
                )
                Spacer(modifier = Modifier.width(4.5.dp))
                Text(
                    text = broadcasterName,
                    color = badgeText,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = if (isBeinSourceChannel) 140.dp else 112.dp)
                )
                if (!isBeinSourceChannel) {
                    Spacer(modifier = Modifier.width(4.dp))
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
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 3.5.dp, vertical = 0.5.dp)
                        )
                    }
                }
            }

            // Stage 2: Illuminated Floodlight Watch CTA Pill with Circular Play Medallion
            Surface(
                color = Color.Transparent,
                shape = RoundedCornerShape(50),
                border = BorderStroke(
                    0.8.dp,
                    if (isLive) KickoffPeachCream.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.40f)
                ),
                modifier = Modifier
                    .height(27.dp)
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
                    modifier = Modifier.padding(start = 4.dp, end = 9.dp)
                ) {
                    // Recessed Dark Circular Play Medallion
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(DarkGraphite)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Watch Match",
                            tint = if (isLive) Color(0xFF80EE98) else KickoffTropicalTeal,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    if (isLive) {
                        LiveBroadcastEqualizerBars()
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = if (isLive) "WATCH LIVE" else "WATCH",
                        color = DarkGraphite,
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
        isLive -> LiveCrimson
        hasFavTeam -> AmberGold
        else -> KickoffTropicalTeal.copy(alpha = 0.55f)
    }

    val livePulseTransition = rememberInfiniteTransition(label = "matchCardLivePulse_${match.matchId}")
    val livePulseAlpha by livePulseTransition.animateFloat(
        initialValue = if (isLive) 0.45f else 0.28f,
        targetValue = if (isLive) 0.95f else 0.28f,
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
        color = if (isInsidePinnedSection) Color.Transparent else KickoffOceanSurface,
        shape = cardShape,
        border = if (isInsidePinnedSection) {
            null
        } else {
            BorderStroke(
                width = 0.9.dp,
                color = if (isLive) LiveCrimson.copy(alpha = 0.32f) else Color.White.copy(alpha = 0.07f)
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isInsidePinnedSection) 0.dp else 16.dp)
            .clip(cardShape)
            .clickable { onOpenDossier() }
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
                                    LiveCrimson.copy(alpha = 0.10f),
                                    KickoffOceanSurface.copy(alpha = 0.40f),
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
                val clockTelemetry = remember(match.liveMinuteOrBadge, match.status, match.halfTimeScore, match.kickoffTimeFormatted) {
                    parseLiveMatchClockTelemetry(match)
                }

                // Match Main Row: 3-Tier Broadcast Digital Clock Pod (Left) + Dual Team Scoreboard Rows (Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Left Column (70dp wide): Crystal-Clear 3-Tier Broadcast Clock Pod
                    Surface(
                        color = if (isLive) Color(0xFF1A0810) else Color(0xFF06131B),
                        shape = RoundedCornerShape(11.dp),
                        border = BorderStroke(
                            width = if (isLive) 1.1.dp else 0.8.dp,
                            color = when {
                                isLive -> LiveCrimson.copy(alpha = livePulseAlpha)
                                isFinished -> Color.White.copy(alpha = 0.10f)
                                else -> KickoffTropicalTeal.copy(alpha = 0.30f)
                            }
                        ),
                        modifier = Modifier.width(70.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp)
                        ) {
                            // Top Tier: Explicit Period Pill ("1ST HALF", "2ND HALF", "HALF TIME", "KICKOFF", "FULL TIME")
                            Surface(
                                color = when {
                                    isLive -> LiveCrimson.copy(alpha = 0.24f)
                                    isFinished -> Color.White.copy(alpha = 0.08f)
                                    else -> KickoffTropicalTeal.copy(alpha = 0.16f)
                                },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = clockTelemetry.periodBadge,
                                    color = when {
                                        isLive -> KickoffPeachCream
                                        isFinished -> ChampagneMuted
                                        else -> KickoffTropicalTeal
                                    },
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.2.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            // Middle Tier: Hero Match Minute ("86'", "41'", "HT") or Kickoff Time ("20:00")
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
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
                                            .background(LiveCrimson)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                }
                                Text(
                                    text = clockTelemetry.heroReadout,
                                    color = when (match.status) {
                                        MatchStatus.LIVE, MatchStatus.HALFTIME -> Color.White
                                        MatchStatus.FINISHED -> ChampagneMuted
                                        MatchStatus.UPCOMING -> Color.White
                                    },
                                    fontSize = if (isLive) 13.5.sp else 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            Spacer(modifier = Modifier.height(1.5.dp))

                            // Bottom Tier: Half-Time Score ("HT 0-1") or Status Sub-label ("LIVE MIN" / "GMT+1")
                            Text(
                                text = clockTelemetry.bottomBadge,
                                color = when {
                                    match.halfTimeScore != null && (isLive || isFinished) -> AmberGold
                                    isLive -> LiveCrimson.copy(alpha = 0.95f)
                                    else -> KickoffTropicalTeal.copy(alpha = 0.85f)
                                },
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                softWrap = false
                            )
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

                // Match Action Footer: Real-Time Dual-Half Telemetry & 90-Minute Timeline when LIVE
                Spacer(modifier = Modifier.height(8.dp))
                if (isLive) {
                    val latestGoalScorer = remember(match.goalScorersSummary) {
                        match.goalScorersSummary
                            ?.split(",")
                            ?.map { it.trim() }
                            ?.lastOrNull { it.isNotBlank() }
                            .orEmpty()
                    }
                    Surface(
                        color = Color(0xFF071620).copy(alpha = 0.90f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.6.dp, LiveCrimson.copy(alpha = 0.30f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Telemetry Status Row: Clear human-readable match state + HT score / Latest Goal / Remaining time
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(LiveCrimson)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = clockTelemetry.statusExplanation,
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.2.sp
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (latestGoalScorer.isNotBlank()) {
                                        Text(
                                            text = "⚽ $latestGoalScorer",
                                            color = KickoffPeachCream,
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (match.halfTimeScore != null) {
                                        Surface(
                                            color = AmberGold.copy(alpha = 0.16f),
                                            shape = RoundedCornerShape(4.dp),
                                            border = BorderStroke(0.5.dp, AmberGold.copy(alpha = 0.55f))
                                        ) {
                                            Text(
                                                text = "HT ${match.halfTimeScore}",
                                                color = AmberGold,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 4.5.dp, vertical = 1.dp)
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = clockTelemetry.remainingLabel,
                                            color = KickoffTropicalTeal,
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }

                            // Dual-Half 90-Minute Progress Timeline Bar (0' --- HT 45' --- 90')
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = "0'",
                                    color = ChampagneMuted,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(Color.White.copy(alpha = 0.10f))
                                ) {
                                    // Illuminated elapsed match progress fill
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(clockTelemetry.progressFraction)
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(KickoffTropicalTeal, KickoffPeachCream, LiveCrimson)
                                                )
                                            )
                                    )
                                    // Half-Time (45' / 50%) center marker tick
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .width(1.5.dp)
                                            .height(4.dp)
                                            .background(Color.White.copy(alpha = 0.55f))
                                    )
                                }
                                Text(
                                    text = "HT",
                                    color = if (clockTelemetry.progressFraction >= 0.5f) AmberGold else ChampagneMuted,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "90'",
                                    color = ChampagneMuted,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.06f),
                        thickness = 0.8.dp
                    )
                }
                Spacer(modifier = Modifier.height(7.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left side: Compact Stadium & Stage/Round info pill
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
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (match.stageOrRound.isNotBlank()) {
                                "${match.stadiumName} • ${match.stageOrRound}"
                            } else {
                                match.stadiumName
                            },
                            color = Champagne.copy(alpha = 0.88f),
                            fontSize = 10.5.sp,
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
                sizeDp = 24
            )
            Spacer(modifier = Modifier.width(9.dp))
            Text(
                text = teamName,
                color = (if (isFavorite) AmberGold else Color.White).copy(alpha = nameAlpha),
                fontSize = 13.5.sp,
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
                        .size(13.dp)
                        .clickable { onToggleFavorite() }
                )
            }
        }

        // Right Column: Recessed Scoreboard Well when match is Live or Finished
        if ((isLive || isFinished) && score != null) {
            val scoreColor = when {
                isLive -> Color.White
                isWinner -> KickoffPeachCream
                isLoser -> ChampagneMuted
                else -> Color.White
            }
            Surface(
                color = when {
                    isLive -> LiveCrimson.copy(alpha = 0.24f)
                    isWinner -> KickoffTropicalTeal.copy(alpha = 0.16f)
                    else -> Color(0xFF06131B)
                },
                shape = RoundedCornerShape(7.dp),
                border = BorderStroke(
                    0.7.dp,
                    when {
                        isLive -> LiveCrimson.copy(alpha = 0.70f)
                        isWinner -> KickoffTropicalTeal.copy(alpha = 0.45f)
                        else -> Color.White.copy(alpha = 0.09f)
                    }
                ),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = score.toString(),
                    color = scoreColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
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
                    text = "MATCH TV CHANNELS (★ = SAVED IN YOUR FAVORITES)",
                    color = ChampagneMuted,
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
                                        tint = if (isSelected) DarkGraphite else AmberGold,
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
                                borderColor = if (isSavedInFavs) AmberGold else ChampagneSubtleBorder,
                                selectedBorderColor = ArenaPitchGreen,
                                borderWidth = if (isSavedInFavs) 1.1.dp else 0.8.dp
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ArenaPitchGreen,
                                selectedLabelColor = DarkGraphite,
                                containerColor = if (isSavedInFavs) AmberGold.copy(alpha = 0.12f) else DarkGraphite,
                                labelColor = if (isSavedInFavs) AmberGold else PaleGold
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
                            val firstAvailableFavChannel = state.optionsAvailableInFavorites.firstOrNull {
                                !it.equals(state.resolvedChannelLabel, ignoreCase = true)
                            }
                            if (firstAvailableFavChannel != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = { onSelectMenaChannelOption(firstAvailableFavChannel) },
                                    border = BorderStroke(1.dp, ArenaPitchGreen),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = ArenaPitchGreen.copy(alpha = 0.14f),
                                        contentColor = ArenaFloodlightLime
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = AmberGold,
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
