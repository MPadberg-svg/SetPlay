package com.example

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoSizeSelectLarge
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import com.example.ui.theme.tvFocusableControl
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.model.PlaylistEntity
import com.example.data.model.StreamEntity
import com.example.ui.components.SetPlaySectionNavIcon
import com.example.ui.favorites.FavoritesScreen
import com.example.ui.home.HomeScreen
import com.example.ui.home.KickoffSettingsHexGearIcon
import com.example.ui.livetv.LiveTvScreen
import com.example.ui.livetv.LiveTvViewModel
import com.example.ui.parental.ParentalControlsScreen
import com.example.ui.player.PlayerScreen
import com.example.ui.player.PlayerViewModel
import com.example.ui.playlists.PlaylistsScreen
import com.example.ui.search.GlobalSearchScreen
import com.example.data.sync.SyncRole
import com.example.ui.sync.TvSyncCompanionBar
import com.example.ui.sync.TvSyncModalSheet
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ArenaFloodlightLime
import com.example.ui.theme.Champagne
import com.example.ui.theme.DarkGraphite
import com.example.ui.theme.FavoritesBotanicalBorder
import com.example.ui.theme.FavoritesEvergreenSurface
import com.example.ui.theme.FavoritesObsidianBase
import com.example.ui.theme.FavoritesPaleMint
import com.example.ui.theme.FavoritesSagePrimary
import com.example.ui.theme.KickoffCeruleanBlue
import com.example.ui.theme.KickoffCoralRed
import com.example.ui.theme.KickoffFloodlightVolt
import com.example.ui.theme.KickoffFloodlightWhite
import com.example.ui.theme.KickoffLiveCrimson
import com.example.ui.theme.KickoffOceanBase
import com.example.ui.theme.KickoffOceanSurface
import com.example.ui.theme.KickoffPeachCream
import com.example.ui.theme.KickoffPitchBlack
import com.example.ui.theme.KickoffPitchEmerald
import com.example.ui.theme.KickoffStadiumForest
import com.example.ui.theme.KickoffTrophyGold
import com.example.ui.theme.KickoffTurfSurface
import com.example.data.repository.MatchStatus
import com.example.ui.theme.KickoffTropicalTeal
import com.example.ui.theme.LiveCrimson
import com.example.ui.theme.LiveRed
import com.example.ui.theme.MetalCharcoal
import com.example.ui.theme.MoviesAquamarine
import com.example.ui.theme.MoviesAtlanticTeal
import com.example.ui.theme.MoviesCyanTeal
import com.example.ui.theme.MoviesMintSpring
import com.example.ui.theme.MoviesSapphireNavy
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PaleGold
import com.example.ui.theme.SeriesEmerald
import com.example.ui.theme.SeriesForestSurface
import com.example.ui.theme.SeriesIceAqua
import com.example.ui.theme.SeriesMistTeal
import com.example.ui.theme.SeriesPineBase
import com.example.ui.theme.SeriesSeaJade
import com.example.ui.theme.TvBroadcastScarlet
import com.example.ui.theme.TvCrimsonRose
import com.example.ui.theme.TvMidnightSlate
import com.example.ui.theme.TvPlumMagenta
import com.example.ui.theme.TvTwilightIndigo
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.vod.VodScreen
import com.example.ui.vod.VodTab
import com.example.ui.vod.VodViewModel
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.util.SecurityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by viewModels()
    private val liveTvViewModel: LiveTvViewModel by viewModels()
    private val vodViewModel: VodViewModel by viewModels()

    private val isInSystemPipModeFlow = MutableStateFlow(false)

    @kotlin.OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isInSystemPip by isInSystemPipModeFlow.collectAsStateWithLifecycle()
            MyApplicationTheme {
                MainAppContent(
                    mainViewModel = mainViewModel,
                    playerViewModel = playerViewModel,
                    liveTvViewModel = liveTvViewModel,
                    vodViewModel = vodViewModel,
                    isInSystemPipMode = isInSystemPip
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.refreshSyncedTvStatusOnResume()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val hasActiveStream = playerViewModel.uiState.value.currentStream != null &&
            playerViewModel.uiState.value.isPlaying
        if (hasActiveStream && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build()
                enterPictureInPictureMode(params)
            } catch (_: Exception) {}
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInSystemPipModeFlow.value = isInPictureInPictureMode
    }
}

@OptIn(UnstableApi::class)
@ExperimentalMaterial3Api
@Composable
fun MainAppContent(
    mainViewModel: MainViewModel,
    playerViewModel: PlayerViewModel,
    liveTvViewModel: LiveTvViewModel,
    vodViewModel: VodViewModel,
    isInSystemPipMode: Boolean = false
) {
    val currentScreen by mainViewModel.currentScreen.collectAsStateWithLifecycle()
    val activePlaylist by mainViewModel.activePlaylist.collectAsStateWithLifecycle()
    val isParentalUnlocked by mainViewModel.isParentalUnlockedThisSession.collectAsStateWithLifecycle()
    val playerUiState by playerViewModel.uiState.collectAsStateWithLifecycle()
    val activeGoalAlert by mainViewModel.activeGoalAlert.collectAsStateWithLifecycle()
    val tvSyncState by mainViewModel.tvSyncState.collectAsStateWithLifecycle()
    val tvSyncErrorMessage by mainViewModel.tvSyncErrorMessage.collectAsStateWithLifecycle()

    val selectedMatchDay by mainViewModel.selectedMatchDay.collectAsStateWithLifecycle()
    val matchesByDay by mainViewModel.matchesByDay.collectAsStateWithLifecycle()
    val todayMatches by mainViewModel.todayMatches.collectAsStateWithLifecycle()
    val favorites by mainViewModel.favorites.collectAsStateWithLifecycle()
    val recentlyWatched by mainViewModel.recentlyWatched.collectAsStateWithLifecycle()
    val liveCategories by liveTvViewModel.categories.collectAsStateWithLifecycle()
    val movieCategories by vodViewModel.movieCategories.collectAsStateWithLifecycle()
    val seriesCategories by vodViewModel.seriesCategories.collectAsStateWithLifecycle()
    val kickoffSourceHealth by mainViewModel.kickoffSourceHealth.collectAsStateWithLifecycle()
    val kickoffSourceAlert by mainViewModel.kickoffSourceAlert.collectAsStateWithLifecycle()
    val hasKickoffSourceIssue = remember(kickoffSourceAlert, kickoffSourceHealth) {
        val customKeyBroken = kickoffSourceHealth.any {
            it.isUsingCustomKey && it.status == com.example.data.repository.SourceHealthStatus.KEY_INVALID_OR_EXPIRED
        }
        val healthyCount = kickoffSourceHealth.count {
            it.status == com.example.data.repository.SourceHealthStatus.HEALTHY
        }
        kickoffSourceAlert != null || customKeyBroken || healthyCount < 2
    }

    val liveMatchesCount = remember(todayMatches) {
        todayMatches.count { it.status == MatchStatus.LIVE || it.status == MatchStatus.HALFTIME }
    }
    val activeDayMatches = remember(matchesByDay, selectedMatchDay) {
        matchesByDay[selectedMatchDay].orEmpty()
    }
    val activeDayLeaguesCount = remember(activeDayMatches) {
        activeDayMatches.map { it.tournamentId }.distinct().size
    }
    val topSectionDetailText = remember(
        currentScreen,
        selectedMatchDay,
        activeDayMatches.size,
        activeDayLeaguesCount,
        activePlaylist,
        liveCategories.size,
        movieCategories.size,
        seriesCategories.size,
        favorites.size
    ) {
        when (currentScreen) {
            AppScreen.HOME -> {
                val dayLabel = selectedMatchDay.getDynamicTitle()
                if (activeDayMatches.isNotEmpty()) {
                    "$dayLabel • ${activeDayMatches.size} Games • $activeDayLeaguesCount Leagues"
                } else {
                    "$dayLabel • Live Match Center"
                }
            }
            AppScreen.LIVE_TV -> {
                if (activePlaylist != null && activePlaylist!!.channelCount > 0) {
                    "%,d Channels • ${liveCategories.size} Categories".format(activePlaylist!!.channelCount)
                } else {
                    "Live Channels & EPG"
                }
            }
            AppScreen.MOVIES -> {
                if (activePlaylist != null && activePlaylist!!.vodCount > 0) {
                    "%,d Movies • ${movieCategories.size} Genres".format(activePlaylist!!.vodCount)
                } else {
                    "Cinema VOD Library"
                }
            }
            AppScreen.SERIES -> {
                if (activePlaylist != null && activePlaylist!!.seriesCount > 0) {
                    "%,d Series • ${seriesCategories.size} Genres".format(activePlaylist!!.seriesCount)
                } else {
                    "Complete Seasons & Episodes"
                }
            }
            AppScreen.FAVORITES -> {
                val liveFavs = favorites.count { it.streamType.equals("live", ignoreCase = true) }
                "${favorites.size} Saved • $liveFavs Live TV"
            }
            AppScreen.PLAYLISTS -> activePlaylist?.name ?: "Account Management"
            AppScreen.GLOBAL_SEARCH -> "Channels, Movies & Series"
            AppScreen.PARENTAL_SETTINGS -> "PIN & Category Locks"
        }
    }

    var isFullScreenPlayer by remember { mutableStateOf(false) }
    var showNavigationMenuSheet by remember { mutableStateOf(false) }
    var showTvSyncSheet by remember { mutableStateOf(false) }
    var openTvSyncSheetOnHostTab by remember { mutableStateOf(false) }
    var pendingStreamToPlay by remember { mutableStateOf<StreamEntity?>(null) }
    var pendingUnlockCallback by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showPinDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    val editionOverride by mainViewModel.editionOverride.collectAsStateWithLifecycle()
    val activeEdition = remember(editionOverride, context) {
        com.example.util.EditionResolver.resolveEdition(context)
    }

    var lastTvBackPressEpochMs by remember { androidx.compose.runtime.mutableLongStateOf(0L) }
    var showTvExitConfirmBanner by remember { mutableStateOf(false) }

    LaunchedEffect(showTvExitConfirmBanner) {
        if (showTvExitConfirmBanner) {
            kotlinx.coroutines.delay(2500L)
            showTvExitConfirmBanner = false
        }
    }

    LaunchedEffect(activeEdition) {
        if (activeEdition == com.example.util.SetPlayEdition.TV && tvSyncState.role != SyncRole.TV_HOST) {
            mainViewModel.startTvHostMode()
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                mainViewModel.refreshTvSyncStatusOnResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(isFullScreenPlayer) {
        mainViewModel.isTvInFullScreenPlayback = isFullScreenPlayer
    }

    LaunchedEffect(
        playerUiState.currentStream?.name,
        playerUiState.currentStream?.streamId,
        playerUiState.currentStream?.streamType,
        playerUiState.currentPositionMs,
        playerUiState.isPlaying,
        playerUiState.selectedWyzieLanguageCode,
        playerUiState.lastSelectedSubtitleLanguageCode
    ) {
        mainViewModel.tvSyncManager.tvPlaybackStatusProvider = {
            val latestPlayerState = playerViewModel.uiState.value
            mainViewModel.tvSyncState.value.copy(
                tvNowPlayingTitle = latestPlayerState.currentStream?.name.orEmpty(),
                tvNowPlayingStreamId = latestPlayerState.currentStream?.streamId?.toString().orEmpty(),
                tvNowPlayingType = latestPlayerState.currentStream?.streamType.orEmpty(),
                tvNowPlayingPositionMs = latestPlayerState.currentPositionMs,
                tvIsPlaying = latestPlayerState.isPlaying,
                tvActiveSubtitleLang = (latestPlayerState.selectedWyzieLanguageCode ?: latestPlayerState.lastSelectedSubtitleLanguageCode).orEmpty()
            )
        }
    }

    LaunchedEffect(mainViewModel.tvSyncManager) {
        mainViewModel.tvSyncManager.onControlCommandReceived = { cmd ->
            if (mainViewModel.tvSyncState.value.role == SyncRole.TV_HOST) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main.immediate) {
                    when (cmd.action.uppercase()) {
                        "TOGGLE_PLAY_PAUSE" -> playerViewModel.togglePlayPause()
                        "STOP" -> {
                            playerViewModel.stopAndClosePlayer()
                            isFullScreenPlayer = false
                        }
                        "ZAP_NEXT" -> playerViewModel.zapChannelOffset(1)
                        "ZAP_PREV" -> playerViewModel.zapChannelOffset(-1)
                        "SEEK_RELATIVE" -> {
                            if (cmd.seekSeconds != 0) {
                                playerViewModel.seekRelative(cmd.seekSeconds.toLong())
                            }
                        }
                        "SET_SUBTITLE_LANG" -> {
                            val lang = cmd.value.takeIf { !it.equals("off", ignoreCase = true) && it.isNotBlank() }
                            playerViewModel.chooseSubtitleLanguageFromPrompt(lang)
                        }
                    }
                }
            }
        }
        launch {
            mainViewModel.tvSyncManager.incomingPlayCommands.collect { cmd ->
                if (mainViewModel.tvSyncState.value.role == SyncRole.TV_HOST) {
                    val resolved = mainViewModel.resolveOrBuildStreamForTvCommand(cmd)
                    if (resolved != null) {
                        val (stream, playlist) = resolved
                        isFullScreenPlayer = true
                        playerViewModel.playStream(stream, playlist)
                        if (cmd.preferredSubtitleLang.isNotBlank()) {
                            playerViewModel.chooseSubtitleLanguageFromPrompt(cmd.preferredSubtitleLang)
                        }
                    }
                }
            }
        }
    }

    // If Android System PiP is active, show only the clean video surface
    if (isInSystemPipMode && playerUiState.currentStream != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = playerViewModel.player
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                },
                update = { view ->
                    view.player = playerViewModel.player
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        return
    }

    // Handle back press:
    // 1. If in fullscreen player -> minimize to Floating In-App PiP window so video continues while browsing
    // 2. If on a secondary screen -> return to Home
    // 3. On SetPlayEdition.TV when already on Home -> 2-Stage TV Back-Button Exit Guard (requires 2nd Back within 2500ms)
    BackHandler(
        enabled = isFullScreenPlayer ||
            currentScreen != AppScreen.HOME ||
            activeEdition == com.example.util.SetPlayEdition.TV
    ) {
        if (isFullScreenPlayer) {
            isFullScreenPlayer = false
        } else if (currentScreen != AppScreen.HOME) {
            mainViewModel.setScreen(AppScreen.HOME)
        } else if (activeEdition == com.example.util.SetPlayEdition.TV) {
            val now = System.currentTimeMillis()
            if (now - lastTvBackPressEpochMs <= 2500L) {
                activity?.finish()
            } else {
                lastTvBackPressEpochMs = now
                showTvExitConfirmBanner = true
            }
        }
    }

    // Function to safely attempt playback with parental check & Single-Connection Safe TV Sync routing
    val requestPlayStream: (StreamEntity) -> Unit = { stream ->
        scope.launch {
            val isLocked = stream.isLocked || mainViewModel.repository.isCategoryLocked(stream.categoryId)
            if (isLocked && !isParentalUnlocked) {
                pendingUnlockCallback = null
                pendingStreamToPlay = stream
                showPinDialog = true
            } else if (tvSyncState.role == SyncRole.PHONE_CONTROLLER && tvSyncState.isSyncActive) {
                // Single-Connection Safe: stop local phone player and wait 300ms for socket release BEFORE delegating stream to TV Host
                val wasLocalPlayerActive = playerUiState.currentStream != null
                playerViewModel.stopAndClosePlayer()
                isFullScreenPlayer = false
                if (wasLocalPlayerActive) {
                    kotlinx.coroutines.delay(300L)
                }
                mainViewModel.playStreamOnSyncedTv(stream, activePlaylist)
            } else {
                isFullScreenPlayer = true
                activePlaylist?.let { playlist ->
                    playerViewModel.playStream(stream, playlist)
                }
            }
        }
    }

    // Function to gate browsing a locked category behind Parental PIN when Parental Lock is active
    val requestUnlockCategory: (String, () -> Unit) -> Unit = { _, onUnlocked ->
        if (!isParentalUnlocked) {
            pendingStreamToPlay = null
            pendingUnlockCallback = onUnlocked
            showPinDialog = true
        } else {
            onUnlocked()
        }
    }

    // Fullscreen Media3 Player Overlay
    if (isFullScreenPlayer && playerUiState.currentStream != null && activePlaylist != null) {
        PlayerScreen(
            viewModel = playerViewModel,
            playlist = activePlaylist!!,
            onBack = {
                isFullScreenPlayer = false
            },
            onEnterInAppPip = {
                isFullScreenPlayer = false
            },
            onToggleFavorite = { stream ->
                scope.launch {
                    mainViewModel.repository.toggleFavorite(stream.id, !stream.isFavorite)
                }
            },
            activeGoalAlert = activeGoalAlert,
            onWatchGoalMatch = { match ->
                mainViewModel.dismissGoalAlert()
                mainViewModel.onWatchMatchOnBeinClicked(
                    match = match,
                    onAutoPlaySingleStream = { stream ->
                        requestPlayStream(stream)
                    }
                )
            },
            onDismissGoalAlert = {
                mainViewModel.dismissGoalAlert()
            },
            canSendToTv = activeEdition == com.example.util.SetPlayEdition.PHONE &&
                (tvSyncState.isSyncActive || mainViewModel.tvSyncManager.hasSavedTvHost()),
            activeEdition = activeEdition,
            onSendToTv = {
                val streamToHandover = playerUiState.currentStream?.copy(
                    lastPositionMs = playerUiState.currentPositionMs
                )
                playerViewModel.stopAndClosePlayer()
                isFullScreenPlayer = false
                if (streamToHandover != null) {
                    scope.launch {
                        // Strict 300ms socket release mutex before TV connects to IPTV server
                        kotlinx.coroutines.delay(300L)
                        if (tvSyncState.role == SyncRole.PHONE_CONTROLLER && tvSyncState.isSyncActive) {
                            mainViewModel.playStreamOnSyncedTv(streamToHandover, activePlaylist)
                        } else if (mainViewModel.tvSyncManager.hasSavedTvHost()) {
                            mainViewModel.reconnectToLastTv {
                                mainViewModel.playStreamOnSyncedTv(streamToHandover, activePlaylist)
                            }
                        } else {
                            openTvSyncSheetOnHostTab = false
                            showTvSyncSheet = true
                        }
                    }
                }
            }
        )
    } else {
        val targetSectionBaseColor = when (currentScreen) {
            AppScreen.HOME -> KickoffPitchBlack
            AppScreen.LIVE_TV -> TvMidnightSlate
            AppScreen.MOVIES -> MoviesSapphireNavy
            AppScreen.SERIES -> SeriesPineBase
            AppScreen.FAVORITES -> FavoritesObsidianBase
            else -> DarkGraphite
        }
        val sectionBaseColor by animateColorAsState(
            targetValue = targetSectionBaseColor,
            animationSpec = tween(durationMillis = 380),
            label = "section_base_bg"
        )

        val renderMainScreenContent: @Composable (Modifier) -> Unit = { containerModifier ->
            BoxWithConstraints(
                modifier = containerModifier.fillMaxSize()
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        val forward = targetState.ordinal >= initialState.ordinal
                        val enterSlide = slideInHorizontally(
                            animationSpec = tween(durationMillis = 260),
                            initialOffsetX = { fullWidth -> if (forward) fullWidth / 14 else -fullWidth / 14 }
                        ) + fadeIn(animationSpec = tween(durationMillis = 240))
                        val exitSlide = slideOutHorizontally(
                            animationSpec = tween(durationMillis = 190),
                            targetOffsetX = { fullWidth -> if (forward) -fullWidth / 18 else fullWidth / 18 }
                        ) + fadeOut(animationSpec = tween(durationMillis = 160))
                        enterSlide togetherWith exitSlide
                    },
                    label = "luxury_section_transition",
                    modifier = Modifier.fillMaxSize()
                ) { targetScreen ->
                    when (targetScreen) {
                        AppScreen.HOME -> HomeScreen(
                            mainViewModel = mainViewModel,
                            onStreamClick = requestPlayStream,
                            onNavigateScreen = { screen ->
                                mainViewModel.setScreen(screen)
                            }
                        )
                        AppScreen.LIVE_TV -> LiveTvScreen(
                            viewModel = liveTvViewModel,
                            activePlaylist = activePlaylist,
                            onChannelClick = requestPlayStream,
                            isParentalUnlocked = isParentalUnlocked,
                            onRequestUnlockCategory = { category, onUnlocked ->
                                requestUnlockCategory(category.categoryId, onUnlocked)
                            }
                        )
                        AppScreen.MOVIES -> VodScreen(
                            sectionTab = VodTab.MOVIES,
                            viewModel = vodViewModel,
                            activePlaylist = activePlaylist,
                            onItemClick = requestPlayStream,
                            isParentalUnlocked = isParentalUnlocked,
                            onRequestUnlockCategory = requestUnlockCategory,
                            recentlyWatched = recentlyWatched
                        )
                        AppScreen.SERIES -> VodScreen(
                            sectionTab = VodTab.SERIES,
                            viewModel = vodViewModel,
                            activePlaylist = activePlaylist,
                            onItemClick = requestPlayStream,
                            isParentalUnlocked = isParentalUnlocked,
                            onRequestUnlockCategory = requestUnlockCategory,
                            recentlyWatched = recentlyWatched,
                            onSeriesEpisodeQueueBuilt = { queue ->
                                playerViewModel.setSeriesEpisodeQueue(queue)
                            }
                        )
                        AppScreen.FAVORITES -> FavoritesScreen(
                            viewModel = mainViewModel,
                            activePlaylist = activePlaylist,
                            onStreamClick = requestPlayStream,
                            onToggleFavorite = { stream ->
                                scope.launch {
                                    mainViewModel.repository.toggleFavorite(stream.id, !stream.isFavorite)
                                }
                            }
                        )
                        AppScreen.GLOBAL_SEARCH -> GlobalSearchScreen(
                            viewModel = mainViewModel,
                            activePlaylist = activePlaylist,
                            onStreamClick = requestPlayStream
                        )
                        AppScreen.PLAYLISTS -> PlaylistsScreen(
                            viewModel = mainViewModel
                        )
                        AppScreen.PARENTAL_SETTINGS -> ParentalControlsScreen(
                            viewModel = mainViewModel,
                            activePlaylist = activePlaylist
                        )
                    }
                }

                // 15-Minute Halftime Return Banner when browsing VOD/Live TV outside fullscreen player
                if (playerUiState.showHalftimeReturnPrompt && playerUiState.halftimeSavedStream != null) {
                    val savedChannelTitle = playerUiState.halftimeSavedStream?.name ?: "Match Channel"
                    Surface(
                        color = Color(0xF0101622),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, AmberGold),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp, start = 16.dp, end = 16.dp)
                            .testTag("halftime_return_banner")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "⚽ 2nd Half Starting — Return to $savedChannelTitle",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Surface(
                                color = AmberGold,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .clickable {
                                        playerViewModel.returnToHalftimeChannel()
                                        isFullScreenPlayer = true
                                    }
                                    .testTag("halftime_return_confirm_button")
                            ) {
                                Text(
                                    text = "Return Now",
                                    color = Color.Black,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            IconButton(
                                onClick = { playerViewModel.dismissHalftimeReturnPrompt() },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // 2-Stage TV Back-Button Exit Guard Banner
                if (showTvExitConfirmBanner && activeEdition == com.example.util.SetPlayEdition.TV) {
                    Surface(
                        color = Color(0xF50B131E),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.2.dp, AmberGold),
                        shadowElevation = 12.dp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 22.dp)
                            .testTag("tv_exit_confirm_banner")
                    ) {
                        Text(
                            text = "Press Back again to exit SetPlay TV",
                            color = PaleGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                        )
                    }
                }

                // Floating Draggable In-App Picture-in-Picture (PiP) Video Window (Phone & Tablet touch edition only)
                if (activeEdition == com.example.util.SetPlayEdition.PHONE && playerUiState.currentStream != null) {
                    FloatingInAppPipWindow(
                        playerViewModel = playerViewModel,
                        stream = playerUiState.currentStream!!,
                        isPlaying = playerUiState.isPlaying,
                        resolutionLabel = playerUiState.resolutionLabel,
                        maxContainerWidthPx = constraints.maxWidth.toFloat(),
                        maxContainerHeightPx = constraints.maxHeight.toFloat(),
                        onExpandFullScreen = { isFullScreenPlayer = true },
                        onTogglePlayPause = { playerViewModel.togglePlayPause() },
                        onEnterSystemPip = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activity != null) {
                                try {
                                    val params = PictureInPictureParams.Builder()
                                        .setAspectRatio(Rational(16, 9))
                                        .build()
                                    activity.enterPictureInPictureMode(params)
                                } catch (_: Exception) {}
                            }
                        },
                        onCloseAndStop = {
                            playerViewModel.stopAndClosePlayer()
                            isFullScreenPlayer = false
                        }
                    )
                }
            }
        }

        if (activeEdition == com.example.util.SetPlayEdition.TV) {
            // =========================================================================
            // 10-FOOT WIDESCREEN ANDROID TV SHELL (`SetPlayEdition.TV`)
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(sectionBaseColor)
                    .testTag("main_scaffold")
            ) {
                TvLeftNavigationRail(
                    currentScreen = currentScreen,
                    liveMatchesCount = liveMatchesCount,
                    favoritesCount = favorites.size,
                    tvSyncState = tvSyncState,
                    onScreenSelected = { screen ->
                        mainViewModel.setScreen(screen)
                    },
                    onOpenTvSyncQr = {
                        mainViewModel.startTvHostMode()
                        openTvSyncSheetOnHostTab = true
                        showTvSyncSheet = true
                    },
                    hasKickoffSourceIssue = hasKickoffSourceIssue,
                    onOpenKickoffSettings = {
                        mainViewModel.openKickoffSettingsCenter(if (hasKickoffSourceIssue) 3 else 0)
                    }
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    renderMainScreenContent(Modifier.weight(1f))
                }
            }
        } else {
            // =========================================================================
            // TOUCH-FIRST LUXURY PHONE & TABLET SHELL (`SetPlayEdition.PHONE`)
            // =========================================================================
            Scaffold(
                containerColor = sectionBaseColor,
                topBar = {
                    TopStreamingBar(
                        currentScreen = currentScreen,
                        activePlaylist = activePlaylist,
                        sectionDetailSummary = topSectionDetailText,
                        liveMatchesCount = liveMatchesCount,
                        halftimeRemainingSeconds = playerUiState.halftimeTimerRemainingSeconds,
                        isTvSyncActive = tvSyncState.isSyncActive,
                        hasKickoffSourceIssue = hasKickoffSourceIssue,
                        onReturnToHalftimeMatch = {
                            playerViewModel.returnToHalftimeChannel()
                            isFullScreenPlayer = true
                        },
                        onDrawerMenuClick = { showNavigationMenuSheet = true },
                        onTvSyncClick = {
                            openTvSyncSheetOnHostTab = false
                            showTvSyncSheet = true
                        },
                        onCastOrAccountsClick = { mainViewModel.setScreen(AppScreen.PLAYLISTS) },
                        onSearchClick = { mainViewModel.setScreen(AppScreen.GLOBAL_SEARCH) },
                        onKickoffSettingsClick = {
                            mainViewModel.openKickoffSettingsCenter(if (hasKickoffSourceIssue) 3 else 0)
                        }
                    )
                },
                bottomBar = {},
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("main_scaffold")
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding())
                ) {
                    renderMainScreenContent(Modifier.fillMaxSize())

                    // Non-interactive soft vertical fade scrim so content glides seamlessly underneath the floating glass dock
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(96.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        sectionBaseColor.copy(alpha = 0.72f),
                                        sectionBaseColor.copy(alpha = 0.94f)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                    ) {
                        if (tvSyncState.role == SyncRole.PHONE_CONTROLLER && tvSyncState.isSyncActive) {
                            TvSyncCompanionBar(
                                syncState = tvSyncState,
                                onTogglePlayPauseOnTv = {
                                    mainViewModel.sendControlToSyncedTv(action = "TOGGLE_PLAY_PAUSE")
                                },
                                onZapPrevOnTv = {
                                    mainViewModel.sendControlToSyncedTv(action = "ZAP_PREV")
                                },
                                onZapNextOnTv = {
                                    mainViewModel.sendControlToSyncedTv(action = "ZAP_NEXT")
                                },
                                onPullStreamToPhone = {
                                    mainViewModel.pullStreamFromTvToPhone { pulledStream, playlist ->
                                        isFullScreenPlayer = true
                                        playerViewModel.playStream(pulledStream, playlist)
                                    }
                                },
                                onLeaveSyncMode = {
                                    mainViewModel.leaveTvSyncMode()
                                },
                                onOpenSyncSheet = {
                                    openTvSyncSheetOnHostTab = false
                                    showTvSyncSheet = true
                                }
                            )
                        }
                        BottomStreamingNav(
                            currentScreen = currentScreen,
                            isStreamPlaying = playerUiState.currentStream != null && playerUiState.isPlaying,
                            liveMatchesCount = liveMatchesCount,
                            favoritesCount = favorites.size,
                            onScreenSelected = { screen ->
                                mainViewModel.setScreen(screen)
                            }
                        )
                    }
                }
            }
        }
    }

    // Royal Gold Navigation Drawer Menu Sheet (Accounts, Parental Controls, Edition Switcher)
    if (showNavigationMenuSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNavigationMenuSheet = false },
            containerColor = MetalCharcoal
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "SetPlay Luxury Suite",
                            color = PaleGold,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = activePlaylist?.let { "Active Server: ${it.name}" } ?: "No Xtream account connected",
                            color = Champagne,
                            fontSize = 12.sp
                        )
                    }
                    Surface(
                        color = PaleGold,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "VIP SECURE",
                            color = DarkGraphite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                DrawerMenuOptionRow(
                    title = "Xtream Accounts & Servers",
                    subtitle = "AES-256-GCM encrypted credentials & catalog sync",
                    icon = Icons.Default.Dns,
                    onClick = {
                        showNavigationMenuSheet = false
                        mainViewModel.setScreen(AppScreen.PLAYLISTS)
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                DrawerMenuOptionRow(
                    title = "Parental Lock & PIN Controls",
                    subtitle = "Salted SHA-256 PIN protection & category locks",
                    icon = Icons.Default.Security,
                    onClick = {
                        showNavigationMenuSheet = false
                        mainViewModel.setScreen(AppScreen.PARENTAL_SETTINGS)
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                DrawerMenuOptionRow(
                    title = "Wi-Fi Phone ↔ TV QR Companion",
                    subtitle = if (tvSyncState.isSyncActive) {
                        "Connected (${tvSyncState.role.name}) • 1-Connection Safe"
                    } else {
                        "Pair phone & Android TV over local Wi-Fi via QR or 6-digit PIN"
                    },
                    icon = Icons.Default.Tv,
                    onClick = {
                        showNavigationMenuSheet = false
                        openTvSyncSheetOnHostTab = false
                        showTvSyncSheet = true
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showTvSyncSheet) {
        TvSyncModalSheet(
            syncState = tvSyncState,
            qrPayload = mainViewModel.tvSyncManager.buildQrPairingPayload(),
            hasSavedTvHost = mainViewModel.tvSyncManager.hasSavedTvHost(),
            savedTvHostName = mainViewModel.tvSyncManager.getSavedTvHostName(),
            savedTvHostSummary = mainViewModel.tvSyncManager.getSavedTvHostSummary(),
            statusErrorMessage = tvSyncErrorMessage,
            activeEdition = activeEdition,
            defaultToTvHostTab = openTvSyncSheetOnHostTab,
            onStartTvHostMode = { mainViewModel.startTvHostMode() },
            onStartPhoneDiscovery = { mainViewModel.startPhoneSyncDiscovery() },
            onConnectToTv = { ip, port, pin ->
                mainViewModel.connectPhoneToTv(
                    ip = ip,
                    port = port,
                    pin = pin,
                    onSuccess = {
                        playerViewModel.stopAndClosePlayer()
                        isFullScreenPlayer = false
                        showTvSyncSheet = false
                    }
                )
            },
            onReconnectLastTv = {
                mainViewModel.reconnectToLastTv(
                    onSuccess = {
                        playerViewModel.stopAndClosePlayer()
                        isFullScreenPlayer = false
                        showTvSyncSheet = false
                    }
                )
            },
            onLeaveSyncMode = {
                mainViewModel.leaveTvSyncMode()
            },
            onDismiss = {
                showTvSyncSheet = false
            }
        )
    }

    // Salted SHA-256 Parental PIN Challenge Modal with Brute-Force Rate Limiting
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = {
                showPinDialog = false
                pendingStreamToPlay = null
                pendingUnlockCallback = null
                enteredPin = ""
                pinError = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = AmberGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Parental PIN Required", color = PaleGold, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        if (pendingStreamToPlay != null) {
                            "This stream belongs to a locked category. Enter your 4-digit PIN to watch (Default: 0000):"
                        } else {
                            "This category is protected by Parental Lock. Enter your 4-digit PIN to browse (Default: 0000):"
                        },
                        color = Champagne,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) enteredPin = it },
                        placeholder = { Text("4 Digits", color = Champagne.copy(alpha = 0.6f)) },
                        singleLine = true,
                        isError = pinError != null,
                        supportingText = pinError?.let { { Text(it, color = LiveRed) } },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberGold,
                            unfocusedBorderColor = Champagne.copy(alpha = 0.4f),
                            focusedContainerColor = DarkGraphite,
                            unfocusedContainerColor = DarkGraphite,
                            focusedTextColor = PaleGold,
                            unfocusedTextColor = PaleGold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pin_input_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val lockoutSec = SecurityManager.getPinLockoutRemainingSeconds()
                            if (lockoutSec > 0) {
                                pinError = "Too many failed attempts. Try again in ${lockoutSec}s."
                                return@launch
                            }
                            if (mainViewModel.repository.verifyParentalPin(enteredPin)) {
                                mainViewModel.unlockParentalSession()
                                showPinDialog = false
                                pendingStreamToPlay?.let { stream ->
                                    if (tvSyncState.role == SyncRole.PHONE_CONTROLLER && tvSyncState.isSyncActive) {
                                        playerViewModel.stopAndClosePlayer()
                                        isFullScreenPlayer = false
                                        mainViewModel.playStreamOnSyncedTv(stream, activePlaylist)
                                    } else {
                                        isFullScreenPlayer = true
                                        activePlaylist?.let { playlist ->
                                            playerViewModel.playStream(stream, playlist)
                                        }
                                    }
                                }
                                pendingUnlockCallback?.invoke()
                                pendingStreamToPlay = null
                                pendingUnlockCallback = null
                                enteredPin = ""
                                pinError = null
                            } else {
                                val newLockout = SecurityManager.getPinLockoutRemainingSeconds()
                                pinError = if (newLockout > 0) {
                                    "PIN locked for ${newLockout}s due to repeated failures."
                                } else {
                                    "Incorrect PIN. Try again."
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = DarkGraphite),
                    modifier = Modifier.testTag("pin_submit_button")
                ) {
                    Text(
                        if (pendingStreamToPlay != null) "Unlock & Play" else "Unlock Category",
                        color = DarkGraphite,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPinDialog = false
                    pendingStreamToPlay = null
                    pendingUnlockCallback = null
                    enteredPin = ""
                    pinError = null
                }) {
                    Text("Cancel", color = Champagne)
                }
            },
            containerColor = MetalCharcoal
        )
    }
}

/**
 * Floating Draggable In-App Picture-in-Picture (PiP) Video Window.
 */
@OptIn(UnstableApi::class)
@Composable
fun FloatingInAppPipWindow(
    playerViewModel: PlayerViewModel,
    stream: StreamEntity,
    isPlaying: Boolean,
    resolutionLabel: String,
    maxContainerWidthPx: Float,
    maxContainerHeightPx: Float,
    onExpandFullScreen: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onEnterSystemPip: () -> Unit,
    onCloseAndStop: () -> Unit
) {
    val density = LocalDensity.current
    var isLargePip by remember { mutableStateOf(false) }

    val pipWidthDp = if (isLargePip) 268.dp else 216.dp
    val pipHeightDp = if (isLargePip) 168.dp else 138.dp

    val pipWidthPx = with(density) { pipWidthDp.toPx() }
    val pipHeightPx = with(density) { pipHeightDp.toPx() }
    val marginPx = with(density) { 12.dp.toPx() }

    val defaultX = (maxContainerWidthPx - pipWidthPx - marginPx).coerceAtLeast(marginPx)
    val defaultY = (maxContainerHeightPx - pipHeightPx - marginPx).coerceAtLeast(marginPx)

    var offsetX by remember(isLargePip) { mutableFloatStateOf(defaultX) }
    var offsetY by remember(isLargePip) { mutableFloatStateOf(defaultY) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MetalCharcoal),
        border = BorderStroke(1.5.dp, AmberGold),
        elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
        modifier = Modifier
            .offset {
                IntOffset(
                    x = offsetX.roundToInt().coerceIn(
                        0,
                        (maxContainerWidthPx - pipWidthPx).roundToInt().coerceAtLeast(0)
                    ),
                    y = offsetY.roundToInt().coerceIn(
                        0,
                        (maxContainerHeightPx - pipHeightPx).roundToInt().coerceAtLeast(0)
                    )
                )
            }
            .size(width = pipWidthDp, height = pipHeightDp)
            .pointerInput(maxContainerWidthPx, maxContainerHeightPx, isLargePip) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val maxX = (maxContainerWidthPx - pipWidthPx).coerceAtLeast(0f)
                    val maxY = (maxContainerHeightPx - pipHeightPx).coerceAtLeast(0f)
                    offsetX = (offsetX + dragAmount.x).coerceIn(0f, maxX)
                    offsetY = (offsetY + dragAmount.y).coerceIn(0f, maxY)
                }
            }
            .testTag("mini_player_dock")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black)
                    .clickable { onExpandFullScreen() }
            ) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = playerViewModel.player
                            useController = false
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        }
                    },
                    update = { playerView ->
                        playerView.player = playerViewModel.player
                    },
                    modifier = Modifier.fillMaxSize()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                ) {
                    Surface(
                        color = DarkGraphite.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.5.dp, AmberGold)
                    ) {
                        Text(
                            text = if (isPlaying) "PiP LIVE" else "PAUSED",
                            color = AmberGold,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    if (resolutionLabel.isNotEmpty()) {
                        Surface(
                            color = PaleGold,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = resolutionLabel,
                                color = DarkGraphite,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onCloseAndStop,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(26.dp)
                        .background(DarkGraphite.copy(alpha = 0.85f), CircleShape)
                        .border(0.5.dp, Champagne.copy(alpha = 0.5f), CircleShape)
                        .testTag("pip_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Player",
                        tint = PaleGold,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(MetalCharcoal)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = com.example.util.StreamMetadataParser.cleanDisplayTitle(stream.name),
                    color = PaleGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onExpandFullScreen() }
                )

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .size(24.dp)
                        .background(AmberGold, CircleShape)
                        .testTag("pip_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = DarkGraphite,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.width(3.dp))

                IconButton(
                    onClick = { isLargePip = !isLargePip },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoSizeSelectLarge,
                        contentDescription = "Resize PiP",
                        tint = Champagne,
                        modifier = Modifier.size(14.dp)
                    )
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    IconButton(
                        onClick = onEnterSystemPip,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureInPictureAlt,
                            contentDescription = "System PiP",
                            tint = AmberGold,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onExpandFullScreen,
                    modifier = Modifier
                        .size(24.dp)
                        .testTag("pip_expand_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInFull,
                        contentDescription = "Expand Fullscreen",
                        tint = PaleGold,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerMenuOptionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        color = DarkGraphite,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Champagne.copy(alpha = 0.25f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(AmberGold, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = DarkGraphite, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = PaleGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = Champagne, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun TopStreamingBar(
    currentScreen: AppScreen = AppScreen.HOME,
    activePlaylist: PlaylistEntity?,
    sectionDetailSummary: String = "",
    liveMatchesCount: Int = 0,
    halftimeRemainingSeconds: Int? = null,
    isTvSyncActive: Boolean = false,
    hasKickoffSourceIssue: Boolean = false,
    onReturnToHalftimeMatch: () -> Unit = {},
    onDrawerMenuClick: () -> Unit,
    onTvSyncClick: () -> Unit = {},
    onCastOrAccountsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onKickoffSettingsClick: () -> Unit = onSearchClick
) {
    val (targetSurface, targetPrimary, sectionBadgeTitle) = when (currentScreen) {
        AppScreen.HOME -> Triple(KickoffPitchBlack, KickoffPitchEmerald, "KICKOFF ARENA")
        AppScreen.LIVE_TV -> Triple(TvMidnightSlate, TvBroadcastScarlet, "LIVE TV")
        AppScreen.MOVIES -> Triple(MoviesSapphireNavy, MoviesCyanTeal, "MOVIES")
        AppScreen.SERIES -> Triple(SeriesPineBase, SeriesMistTeal, "SERIES")
        AppScreen.FAVORITES -> Triple(FavoritesObsidianBase, FavoritesSagePrimary, "FAVORITES")
        AppScreen.PLAYLISTS -> Triple(DarkGraphite, AmberGold, "XTREAM SERVERS")
        AppScreen.GLOBAL_SEARCH -> Triple(DarkGraphite, KickoffPitchEmerald, "GLOBAL SEARCH")
        AppScreen.PARENTAL_SETTINGS -> Triple(DarkGraphite, AmberGold, "PARENTAL LOCK")
    }

    val sectionUnderlineStops = when (currentScreen) {
        AppScreen.HOME -> listOf(KickoffPitchEmerald, KickoffFloodlightVolt, KickoffTrophyGold, KickoffLiveCrimson)
        AppScreen.LIVE_TV -> listOf(AmberGold, TvBroadcastScarlet, TvCrimsonRose, TvPlumMagenta)
        AppScreen.MOVIES -> listOf(MoviesMintSpring, MoviesAquamarine, MoviesCyanTeal, MoviesAtlanticTeal)
        AppScreen.SERIES -> listOf(SeriesIceAqua, SeriesMistTeal, SeriesSeaJade, AmberGold)
        AppScreen.FAVORITES -> listOf(FavoritesPaleMint, FavoritesSagePrimary, FavoritesBotanicalBorder, AmberGold)
        else -> listOf(AmberGold, PaleGold, Champagne, AmberGold)
    }

    val barSurfaceColor by animateColorAsState(targetSurface, tween(300), label = "top_bar_surface")
    val sectionPrimaryAccent by animateColorAsState(targetPrimary, tween(300), label = "top_bar_primary")

    Surface(
        color = barSurfaceColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GhostTopBarIconButton(
                icon = Icons.Default.Menu,
                contentDescription = "Navigation Drawer Menu",
                iconTint = Color.White.copy(alpha = 0.92f),
                accentColor = sectionPrimaryAccent,
                onClick = onDrawerMenuClick,
                testTag = "top_drawer_menu_btn"
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .tvFocusableControl(
                        accentColor = sectionPrimaryAccent,
                        shape = RoundedCornerShape(8.dp),
                        onClick = onCastOrAccountsClick
                    )
                    .padding(horizontal = 2.dp, vertical = 1.dp)
                    .testTag("setplay_creative_wordmark")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Set",
                        color = Color.White,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 0.5.sp
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .size(18.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        sectionPrimaryAccent.copy(alpha = 0.35f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = sectionPrimaryAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Play",
                        color = AmberGold,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.3.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .height(2.5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Brush.horizontalGradient(colors = sectionUnderlineStops))
                )
            }

            if (currentScreen != AppScreen.HOME) {
                Spacer(modifier = Modifier.width(10.dp))

                Surface(
                    color = sectionPrimaryAccent.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.8.dp, sectionPrimaryAccent.copy(alpha = 0.55f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.5.dp)
                                .clip(CircleShape)
                                .background(sectionPrimaryAccent)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = sectionBadgeTitle,
                            color = Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (halftimeRemainingSeconds != null && halftimeRemainingSeconds > 0) {
                val mm = halftimeRemainingSeconds / 60
                val ss = halftimeRemainingSeconds % 60
                Surface(
                    color = AmberGold.copy(alpha = 0.20f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, AmberGold),
                    modifier = Modifier
                        .tvFocusableControl(
                            accentColor = AmberGold,
                            shape = RoundedCornerShape(50),
                            onClick = onReturnToHalftimeMatch
                        )
                        .testTag("top_bar_halftime_countdown_badge")
                ) {
                    Text(
                        text = String.format(java.util.Locale.US, "⏱️ HT %02d:%02d", mm, ss),
                        color = AmberGold,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            GhostTopBarIconButton(
                icon = Icons.Default.Tv,
                contentDescription = "Wi-Fi Phone to TV QR Sync",
                iconTint = if (isTvSyncActive) ArenaFloodlightLime else Color.White.copy(alpha = 0.88f),
                accentColor = if (isTvSyncActive) ArenaFloodlightLime else sectionPrimaryAccent,
                showStatusDot = isTvSyncActive,
                statusDotColor = ArenaFloodlightLime,
                onClick = onTvSyncClick,
                testTag = "top_tv_sync_btn"
            )

            Spacer(modifier = Modifier.width(6.dp))

            GhostTopBarIconButton(
                icon = Icons.Default.Dns,
                contentDescription = "Xtream Accounts & Servers",
                iconTint = if (activePlaylist != null) AmberGold else Color.White.copy(alpha = 0.88f),
                accentColor = sectionPrimaryAccent,
                showStatusDot = activePlaylist != null,
                statusDotColor = sectionPrimaryAccent,
                onClick = onCastOrAccountsClick,
                testTag = "top_playlists_btn"
            )

            Spacer(modifier = Modifier.width(6.dp))

            if (currentScreen == AppScreen.HOME) {
                val settingsIconTint = if (hasKickoffSourceIssue) KickoffTrophyGold else KickoffFloodlightVolt
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(
                            width = 0.8.dp,
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.18f),
                                    KickoffPitchEmerald.copy(alpha = 0.30f)
                                )
                            ),
                            shape = CircleShape
                        )
                        .tvFocusableControl(
                            accentColor = KickoffPitchEmerald,
                            shape = CircleShape,
                            onClick = onKickoffSettingsClick
                        )
                        .padding(7.dp)
                        .testTag("top_search_btn")
                ) {
                    KickoffSettingsHexGearIcon(
                        modifier = Modifier.size(19.dp),
                        tint = settingsIconTint
                    )
                    if (hasKickoffSourceIssue) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(6.5.dp)
                                .clip(CircleShape)
                                .background(KickoffLiveCrimson)
                        )
                    }
                }
            } else {
                GhostTopBarIconButton(
                    icon = Icons.Default.Search,
                    contentDescription = "Search",
                    iconTint = Color.White.copy(alpha = 0.88f),
                    accentColor = sectionPrimaryAccent,
                    onClick = onSearchClick,
                    testTag = "top_search_btn"
                )
            }
        }
    }
}

@Composable
private fun GhostTopBarIconButton(
    icon: ImageVector,
    contentDescription: String,
    iconTint: Color,
    accentColor: Color = KickoffTropicalTeal,
    showStatusDot: Boolean = false,
    statusDotColor: Color = AmberGold,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.06f))
            .border(
                width = 0.8.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.18f),
                        accentColor.copy(alpha = 0.22f)
                    )
                ),
                shape = CircleShape
            )
            .tvFocusableControl(
                accentColor = accentColor,
                shape = CircleShape,
                onClick = onClick
            )
            .padding(7.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(19.dp)
        )
        if (showStatusDot) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(6.5.dp)
                    .clip(CircleShape)
                    .background(statusDotColor)
            )
        }
    }
}

private data class LiquidPrismNavSpec(
    val screen: AppScreen,
    val label: String,
    val dockGlassTint: Color,
    val accentPrimary: Color,
    val accentHighlight: Color,
    val labelColor: Color
)

/**
 * SetPlay "Liquid Prism Beam" Floating Glass Dock (`BottomStreamingNav`):
 * - Zero-seam floating hardware-glass capsule (`height = 60.dp`, `RoundedCornerShape(30.dp)`).
 * - Smooth 280ms adaptive color cross-fade across KickOff, Live TV, Movies, Series, and Favorites.
 * - Gliding `2.5.dp` top prismatic light-beam + soft inner radial ambient spotlight synced to the
 *   morphing expanding horizontal active pill (`1.95f` active weight vs `1.0f` inactive icon slots).
 * - Bespoke Compose Canvas navigation icons (`SetPlaySectionNavIcon`) + horizontal swipe-to-glide.
 */
@Composable
fun BottomStreamingNav(
    currentScreen: AppScreen,
    isStreamPlaying: Boolean = false,
    liveMatchesCount: Int = 0,
    favoritesCount: Int = 0,
    onScreenSelected: (AppScreen) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    val navItems = remember {
        listOf(
            LiquidPrismNavSpec(
                screen = AppScreen.HOME,
                label = "KickOff",
                dockGlassTint = Color(0xFF0A1612),
                accentPrimary = KickoffPitchEmerald,
                accentHighlight = KickoffFloodlightVolt,
                labelColor = KickoffFloodlightWhite
            ),
            LiquidPrismNavSpec(
                screen = AppScreen.LIVE_TV,
                label = "Live TV",
                dockGlassTint = Color(0xFF1A111A),
                accentPrimary = Color(0xFFFF4D5E),
                accentHighlight = AmberGold,
                labelColor = Color.White
            ),
            LiquidPrismNavSpec(
                screen = AppScreen.MOVIES,
                label = "Movies",
                dockGlassTint = Color(0xFF071D2E),
                accentPrimary = MoviesCyanTeal,
                accentHighlight = Color(0xFF38E4AE),
                labelColor = Color.White
            ),
            LiquidPrismNavSpec(
                screen = AppScreen.SERIES,
                label = "Series",
                dockGlassTint = Color(0xFF09201F),
                accentPrimary = SeriesSeaJade,
                accentHighlight = SeriesIceAqua,
                labelColor = Color.White
            ),
            LiquidPrismNavSpec(
                screen = AppScreen.FAVORITES,
                label = "Favorites",
                dockGlassTint = Color(0xFF0A1E19),
                accentPrimary = FavoritesSagePrimary,
                accentHighlight = PaleGold,
                labelColor = FavoritesPaleMint
            )
        )
    }

    val selectedIndex = remember(currentScreen, navItems) {
        navItems.indexOfFirst { it.screen == currentScreen }.coerceAtLeast(0)
    }
    val activeSpec = navItems[selectedIndex]

    val dockGlassTint by animateColorAsState(
        targetValue = activeSpec.dockGlassTint,
        animationSpec = tween(durationMillis = 280),
        label = "prism_dock_glass_tint"
    )
    val accentPrimary by animateColorAsState(
        targetValue = activeSpec.accentPrimary,
        animationSpec = tween(durationMillis = 280),
        label = "prism_accent_primary"
    )
    val accentHighlight by animateColorAsState(
        targetValue = activeSpec.accentHighlight,
        animationSpec = tween(durationMillis = 280),
        label = "prism_accent_highlight"
    )
    val activeLabelColor by animateColorAsState(
        targetValue = activeSpec.labelColor,
        animationSpec = tween(durationMillis = 280),
        label = "prism_active_label_color"
    )

    // Active tab center normalized across expanded active weight (1.95f) + 4 inactive slots (1.0f each = 5.95f total)
    val targetCenterFraction = (selectedIndex + 0.975f) / 5.95f
    val activeCenterFraction by animateFloatAsState(
        targetValue = targetCenterFraction,
        animationSpec = spring(
            dampingRatio = 0.76f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "prism_beam_center_fraction"
    )

    val swipeStepPx = with(density) { 36.dp.toPx() }

    Surface(
        color = dockGlassTint.copy(alpha = 0.92f),
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.22f),
                    accentPrimary.copy(alpha = 0.34f)
                )
            )
        ),
        shadowElevation = 14.dp,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .height(60.dp)
            .testTag("bottom_nav")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(currentScreen) {
                    var dragAccumPx = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { dragAccumPx = 0f },
                        onDragEnd = { dragAccumPx = 0f },
                        onDragCancel = { dragAccumPx = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            dragAccumPx += dragAmount
                            if (dragAccumPx >= swipeStepPx) {
                                val nextIdx = (selectedIndex + 1).coerceAtMost(navItems.lastIndex)
                                if (nextIdx != selectedIndex) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onScreenSelected(navItems[nextIdx].screen)
                                }
                                dragAccumPx = 0f
                            } else if (dragAccumPx <= -swipeStepPx) {
                                val prevIdx = (selectedIndex - 1).coerceAtLeast(0)
                                if (prevIdx != selectedIndex) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onScreenSelected(navItems[prevIdx].screen)
                                }
                                dragAccumPx = 0f
                            }
                        }
                    )
                }
        ) {
            // 1. Gliding Top Prismatic Light-Beam & Inner Radial Ambient Spotlight
            Canvas(modifier = Modifier.fillMaxSize()) {
                val insetPx = 6.dp.toPx()
                val usableW = (size.width - insetPx * 2f).coerceAtLeast(1f)
                val activeCenterX = insetPx + usableW * activeCenterFraction

                // Soft inner radial ambient spotlight behind the active expanding pill
                val spotlightRadius = size.height * 1.15f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentPrimary.copy(alpha = 0.22f),
                            accentHighlight.copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        center = Offset(activeCenterX, size.height * 0.5f),
                        radius = spotlightRadius
                    ),
                    radius = spotlightRadius,
                    center = Offset(activeCenterX, size.height * 0.5f)
                )

                // Gliding 2.5.dp-tall Top Prismatic Light-Beam (36.dp wide) along y = 1.dp
                val beamHalfW = 18.dp.toPx()
                val beamY = 1.25.dp.toPx()
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            accentHighlight,
                            accentPrimary,
                            Color.Transparent
                        ),
                        startX = activeCenterX - beamHalfW,
                        endX = activeCenterX + beamHalfW
                    ),
                    start = Offset(activeCenterX - beamHalfW, beamY),
                    end = Offset(activeCenterX + beamHalfW, beamY),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 2. Morphing Expanding Horizontal Active Pill + Icon-Only Inactive Slots
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { item ->
                    val isSelected = currentScreen == item.screen
                    val slotWeight by animateFloatAsState(
                        targetValue = if (isSelected) 1.95f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = 0.78f,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "prism_slot_weight_${item.screen.name}"
                    )

                    LiquidPrismNavTabSlot(
                        item = item,
                        isSelected = isSelected,
                        accentPrimary = accentPrimary,
                        accentHighlight = accentHighlight,
                        activeLabelColor = activeLabelColor,
                        showKickoffLiveDot = item.screen == AppScreen.HOME && !isSelected && liveMatchesCount > 0,
                        showLiveTvDot = item.screen == AppScreen.LIVE_TV && !isSelected && isStreamPlaying,
                        favoritesBadgeCount = if (item.screen == AppScreen.FAVORITES && !isSelected && favoritesCount > 0) {
                            favoritesCount.coerceAtMost(99)
                        } else {
                            0
                        },
                        onSelect = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onScreenSelected(item.screen)
                        },
                        modifier = Modifier
                            .weight(slotWeight)
                            .fillMaxHeight()
                            .testTag("nav_${item.screen.name.lowercase()}")
                    )
                }
            }
        }
    }
}

@Composable
private fun LiquidPrismNavTabSlot(
    item: LiquidPrismNavSpec,
    isSelected: Boolean,
    accentPrimary: Color,
    accentHighlight: Color,
    activeLabelColor: Color,
    showKickoffLiveDot: Boolean,
    showLiveTvDot: Boolean,
    favoritesBadgeCount: Int,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isTvFocused by remember { mutableStateOf(false) }

    val pillShape = RoundedCornerShape(23.dp)
    val pillBackground by animateColorAsState(
        targetValue = if (isSelected) accentPrimary.copy(alpha = 0.20f) else Color.Transparent,
        animationSpec = tween(durationMillis = 220),
        label = "prism_pill_bg_${item.screen.name}"
    )
    val pillBorderColor by animateColorAsState(
        targetValue = when {
            isTvFocused -> AmberGold
            isSelected -> accentPrimary.copy(alpha = 0.62f)
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 220),
        label = "prism_pill_border_${item.screen.name}"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(46.dp)
            .clip(pillShape)
            .background(pillBackground, pillShape)
            .then(
                if (isSelected || isTvFocused) {
                    Modifier.border(
                        width = if (isTvFocused) 1.5.dp else 1.dp,
                        color = pillBorderColor,
                        shape = pillShape
                    )
                } else {
                    Modifier
                }
            )
            .semantics { contentDescription = item.label }
            .onFocusChanged { isTvFocused = it.isFocused }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSelect
            )
            .padding(horizontal = if (isSelected) 10.dp else 4.dp)
    ) {
        if (isSelected) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                SetPlaySectionNavIcon(
                    screen = item.screen,
                    isSelected = true,
                    tint = accentHighlight,
                    accentTint = accentPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.label,
                    color = activeLabelColor,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.2.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            Box(contentAlignment = Alignment.Center) {
                SetPlaySectionNavIcon(
                    screen = item.screen,
                    isSelected = false,
                    tint = Color.White.copy(alpha = 0.58f),
                    accentTint = Color.Transparent,
                    modifier = Modifier.size(21.dp)
                )

                if (showKickoffLiveDot) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 3.dp, y = (-2).dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(KickoffLiveCrimson)
                    )
                } else if (showLiveTvDot) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 3.dp, y = (-2).dp)
                            .size(5.5.dp)
                            .clip(CircleShape)
                            .background(LiveRed)
                    )
                } else if (favoritesBadgeCount > 0) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 8.dp, y = (-4).dp)
                            .background(
                                color = KickoffTurfSurface.copy(alpha = 0.96f),
                                shape = RoundedCornerShape(50)
                            )
                            .border(
                                width = 0.75.dp,
                                color = item.accentPrimary.copy(alpha = 0.72f),
                                shape = RoundedCornerShape(50)
                            )
                            .padding(horizontal = 4.dp, vertical = 0.5.dp)
                    ) {
                        Text(
                            text = favoritesBadgeCount.toString(),
                            color = item.accentHighlight,
                            fontSize = 7.sp,
                            lineHeight = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

private data class TvRailNavEntry(
    val label: String,
    val testTag: String,
    val targetScreen: AppScreen? = null,
    val systemIcon: ImageVector? = null,
    val badgeText: String? = null,
    val hasCollapsedDot: Boolean = false,
    val dotColor: Color = KickoffLiveCrimson,
    val isPrimaryMedia: Boolean = true,
    val customAction: (() -> Unit)? = null
)

/**
 * Dedicated 10-Foot Widescreen Android TV "Collapsible Prism Glass Rail" (`testTag("tv_left_navigation_rail")`):
 * - Expand-on-focus collapsible geometry (`74.dp` collapsed ↔ `236.dp` expanded via spring animation).
 * - Adaptive 280ms section color palette matching `currentScreen` (KickOff, Live TV, Movies, Series, Favorites).
 * - Clean 2-zone hierarchy with zero leading emojis:
 *     1) Top Zone: 5 Primary Media Destinations powered by bespoke `SetPlaySectionNavIcon` (`23.dp`).
 *     2) Bottom Zone: 4 Quieter System Utilities (`19.dp` muted silver icons) + Live Sync Telemetry Pod.
 * - Unmistakable 10-foot Selected (left `3.dp × 24.dp` glowing vertical prism beam + adaptive tint)
 *   vs. D-Pad Focused (`1.04f` spring scale + `2.dp` luminous focus border + crisp white bold label) states.
 */
@Composable
fun TvLeftNavigationRail(
    currentScreen: AppScreen,
    liveMatchesCount: Int,
    favoritesCount: Int,
    tvSyncState: com.example.data.sync.TvSyncSessionState,
    onScreenSelected: (AppScreen) -> Unit,
    onOpenTvSyncQr: () -> Unit,
    modifier: Modifier = Modifier,
    hasKickoffSourceIssue: Boolean = false,
    onOpenKickoffSettings: () -> Unit = {}
) {
    var isRailFocused by remember { mutableStateOf(false) }

    val railWidth by animateDpAsState(
        targetValue = if (isRailFocused) 236.dp else 74.dp,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tv_prism_rail_width"
    )

    // Adaptive Section Color Palette (280ms smooth cross-fade)
    val (targetGlassTint, targetAccentPrimary, targetAccentHighlight) = when (currentScreen) {
        AppScreen.HOME -> Triple(Color(0xFF0A1612), KickoffPitchEmerald, KickoffFloodlightVolt)
        AppScreen.LIVE_TV -> Triple(Color(0xFF1A111A), Color(0xFFFF4D5E), AmberGold)
        AppScreen.MOVIES -> Triple(Color(0xFF071D2E), MoviesCyanTeal, Color(0xFF38E4AE))
        AppScreen.SERIES -> Triple(Color(0xFF09201F), SeriesSeaJade, SeriesIceAqua)
        AppScreen.FAVORITES -> Triple(Color(0xFF0A1E19), FavoritesSagePrimary, PaleGold)
        else -> Triple(Color(0xFF0A1612), KickoffPitchEmerald, KickoffFloodlightVolt)
    }

    val railGlassTint by animateColorAsState(
        targetValue = targetGlassTint,
        animationSpec = tween(durationMillis = 280),
        label = "tv_rail_glass_tint"
    )
    val railAccentPrimary by animateColorAsState(
        targetValue = targetAccentPrimary,
        animationSpec = tween(durationMillis = 280),
        label = "tv_rail_accent_primary"
    )
    val railAccentHighlight by animateColorAsState(
        targetValue = targetAccentHighlight,
        animationSpec = tween(durationMillis = 280),
        label = "tv_rail_accent_highlight"
    )

    val primaryEntries = remember(liveMatchesCount, favoritesCount) {
        listOf(
            TvRailNavEntry(
                label = "KickOff",
                testTag = "nav_home",
                targetScreen = AppScreen.HOME,
                badgeText = if (liveMatchesCount > 0) "$liveMatchesCount LIVE" else null,
                hasCollapsedDot = liveMatchesCount > 0,
                dotColor = KickoffLiveCrimson,
                isPrimaryMedia = true
            ),
            TvRailNavEntry(
                label = "Live TV",
                testTag = "nav_live_tv",
                targetScreen = AppScreen.LIVE_TV,
                isPrimaryMedia = true
            ),
            TvRailNavEntry(
                label = "Movies",
                testTag = "nav_movies",
                targetScreen = AppScreen.MOVIES,
                isPrimaryMedia = true
            ),
            TvRailNavEntry(
                label = "Series",
                testTag = "nav_series",
                targetScreen = AppScreen.SERIES,
                isPrimaryMedia = true
            ),
            TvRailNavEntry(
                label = "Favorites",
                testTag = "nav_favorites",
                targetScreen = AppScreen.FAVORITES,
                badgeText = if (favoritesCount > 0) favoritesCount.coerceAtMost(99).toString() else null,
                isPrimaryMedia = true
            )
        )
    }

    val utilityEntries = remember(currentScreen, hasKickoffSourceIssue) {
        listOf(
            if (currentScreen == AppScreen.HOME) {
                TvRailNavEntry(
                    label = "KickOff Settings",
                    testTag = "top_search_btn",
                    systemIcon = Icons.Default.Tune,
                    badgeText = if (hasKickoffSourceIssue) "ALERT" else null,
                    hasCollapsedDot = hasKickoffSourceIssue,
                    dotColor = KickoffTrophyGold,
                    isPrimaryMedia = false,
                    customAction = onOpenKickoffSettings
                )
            } else {
                TvRailNavEntry(
                    label = "Global Search",
                    testTag = "top_search_btn",
                    targetScreen = AppScreen.GLOBAL_SEARCH,
                    systemIcon = Icons.Default.Search,
                    isPrimaryMedia = false
                )
            },
            TvRailNavEntry(
                label = "Phone Sync QR",
                testTag = "top_tv_sync_btn",
                systemIcon = Icons.Default.Tv,
                isPrimaryMedia = false,
                customAction = onOpenTvSyncQr
            ),
            TvRailNavEntry(
                label = "Xtream Servers",
                testTag = "top_playlists_btn",
                targetScreen = AppScreen.PLAYLISTS,
                systemIcon = Icons.Default.Dns,
                isPrimaryMedia = false
            ),
            TvRailNavEntry(
                label = "Parental Lock",
                testTag = "tv_rail_parental_lock_btn",
                targetScreen = AppScreen.PARENTAL_SETTINGS,
                systemIcon = Icons.Default.Security,
                isPrimaryMedia = false
            )
        )
    }

    val isPhonePaired = tvSyncState.connectedPeerName.isNotBlank()
    val pinDisplay = tvSyncState.pairingPin.ifBlank { "READY" }
    val ipDisplay = tvSyncState.localIpAddress.ifBlank { "LAN Wi-Fi" }
    val activeRailItemFocusRequester = remember { FocusRequester() }
    val allEntries = primaryEntries + utilityEntries
    val hasMatchingTarget = allEntries.any { it.targetScreen != null && it.targetScreen == currentScreen }

    LaunchedEffect(currentScreen) {
        runCatching { activeRailItemFocusRequester.requestFocus() }
    }

    Row(
        modifier = modifier
            .fillMaxHeight()
            .onFocusChanged { focusState ->
                isRailFocused = focusState.hasFocus
            }
    ) {
        Column(
            modifier = Modifier
                .width(railWidth)
                .fillMaxHeight()
                .background(railGlassTint.copy(alpha = 0.95f))
                .padding(horizontal = 10.dp, vertical = 14.dp)
                .testTag("tv_left_navigation_rail"),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Brand Header: Compact SetPlay ▶ emblem when collapsed; full "Set ▶ Play" wordmark when expanded
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (isRailFocused) Arrangement.Start else Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(railAccentPrimary, railAccentHighlight)
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "SetPlay TV",
                        tint = KickoffPitchBlack,
                        modifier = Modifier.size(20.dp)
                    )
                }

                AnimatedVisibility(
                    visible = isRailFocused,
                    enter = fadeIn(tween(180)) + expandHorizontally(expandFrom = Alignment.Start),
                    exit = fadeOut(tween(140)) + shrinkHorizontally(shrinkTowards = Alignment.Start)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "Set ▶ Play",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = "TV EDITION",
                                color = railAccentHighlight,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Clean 2-Zone Navigation Stack (Top Zone: 5 Primary Media + Divider + Bottom Zone: 4 System Utilities)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Top Zone: 5 Primary Media Destinations
                primaryEntries.forEachIndexed { index, entry ->
                    val isSelected = entry.targetScreen != null && entry.targetScreen == currentScreen
                    val isInitialFocusTarget = isSelected || (!hasMatchingTarget && index == 0)
                    TvPrismRailRow(
                        entry = entry,
                        isSelected = isSelected,
                        isRailExpanded = isRailFocused,
                        railAccentPrimary = railAccentPrimary,
                        railAccentHighlight = railAccentHighlight,
                        focusRequester = if (isInitialFocusTarget) activeRailItemFocusRequester else null,
                        onClick = {
                            if (entry.customAction != null) {
                                entry.customAction.invoke()
                            } else if (entry.targetScreen != null) {
                                onScreenSelected(entry.targetScreen)
                            }
                        }
                    )
                }

                // Clean Visual Separator between Primary Media and System Utilities
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 8.dp)
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.10f))
                )

                // Bottom Zone: 4 Quieter System Utilities
                utilityEntries.forEach { entry ->
                    val isSelected = entry.targetScreen != null && entry.targetScreen == currentScreen
                    TvPrismRailRow(
                        entry = entry,
                        isSelected = isSelected,
                        isRailExpanded = isRailFocused,
                        railAccentPrimary = railAccentPrimary,
                        railAccentHighlight = railAccentHighlight,
                        focusRequester = if (isSelected) activeRailItemFocusRequester else null,
                        onClick = {
                            if (entry.customAction != null) {
                                entry.customAction.invoke()
                            } else if (entry.targetScreen != null) {
                                onScreenSelected(entry.targetScreen)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Bottom Live Sync Telemetry Pod (`tv_rail_sync_status_pod`)
            var isSyncPodFocused by remember { mutableStateOf(false) }
            val syncPodScale by animateFloatAsState(
                targetValue = if (isSyncPodFocused) 1.04f else 1.0f,
                animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow),
                label = "tv_sync_pod_scale"
            )
            Surface(
                color = Color.White.copy(alpha = if (isSyncPodFocused) 0.11f else 0.05f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    width = if (isSyncPodFocused) 2.dp else 0.9.dp,
                    color = when {
                        isSyncPodFocused -> railAccentHighlight
                        isPhonePaired -> ArenaFloodlightLime.copy(alpha = 0.75f)
                        else -> railAccentPrimary.copy(alpha = 0.35f)
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = syncPodScale
                        scaleY = syncPodScale
                    }
                    .clip(RoundedCornerShape(14.dp))
                    .onFocusChanged { isSyncPodFocused = it.isFocused }
                    .clickable(onClick = onOpenTvSyncQr)
                    .focusable()
                    .testTag("tv_rail_sync_status_pod")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (isRailFocused) Arrangement.Start else Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (isRailFocused) 10.dp else 6.dp, vertical = 9.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Cast,
                            contentDescription = "Wi-Fi Phone Sync",
                            tint = if (isPhonePaired) ArenaFloodlightLime else Color.White.copy(alpha = 0.78f),
                            modifier = Modifier.size(18.dp)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 3.dp, y = (-2).dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isPhonePaired) ArenaFloodlightLime else railAccentHighlight)
                        )
                    }

                    AnimatedVisibility(
                        visible = isRailFocused,
                        enter = fadeIn(tween(180)) + expandHorizontally(expandFrom = Alignment.Start),
                        exit = fadeOut(tween(130)) + shrinkHorizontally(shrinkTowards = Alignment.Start)
                    ) {
                        Column(modifier = Modifier.padding(start = 10.dp)) {
                            Text(
                                text = if (isPhonePaired) "Phone Connected" else "Sync PIN: $pinDisplay",
                                color = if (isPhonePaired) ArenaFloodlightLime else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isPhonePaired) {
                                    tvSyncState.connectedPeerName
                                } else {
                                    "$ipDisplay:${tvSyncState.serverPort}"
                                },
                                color = Color.White.copy(alpha = 0.64f),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Right-edge 1.dp Specular Adaptive Border
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(railAccentPrimary.copy(alpha = 0.30f))
        )
    }
}

@Composable
private fun TvPrismRailRow(
    entry: TvRailNavEntry,
    isSelected: Boolean,
    isRailExpanded: Boolean,
    railAccentPrimary: Color,
    railAccentHighlight: Color,
    focusRequester: FocusRequester?,
    onClick: () -> Unit
) {
    var isItemFocused by remember { mutableStateOf(false) }

    val focusScale by animateFloatAsState(
        targetValue = if (isItemFocused) 1.04f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tv_rail_item_scale_${entry.testTag}"
    )

    val rowBackground by animateColorAsState(
        targetValue = when {
            isSelected -> railAccentPrimary.copy(alpha = 0.20f)
            isItemFocused -> Color.White.copy(alpha = 0.10f)
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 200),
        label = "tv_rail_item_bg_${entry.testTag}"
    )

    val rowShape = RoundedCornerShape(14.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .graphicsLayer {
                scaleX = focusScale
                scaleY = focusScale
            }
            .clip(rowShape)
            .background(rowBackground, rowShape)
            .then(
                if (isItemFocused) {
                    Modifier.border(
                        width = 2.dp,
                        color = railAccentHighlight,
                        shape = rowShape
                    )
                } else if (isSelected) {
                    Modifier.border(
                        width = 0.9.dp,
                        color = railAccentPrimary.copy(alpha = 0.45f),
                        shape = rowShape
                    )
                } else {
                    Modifier
                }
            )
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .onFocusChanged { isItemFocused = it.isFocused }
            .semantics { contentDescription = entry.label }
            .clickable(onClick = onClick)
            .focusable()
            .testTag(entry.testTag)
    ) {
        // Active Section Indicator: 3.dp × 24.dp glowing vertical left prismatic light-beam
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 2.dp)
                    .width(3.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(railAccentHighlight, railAccentPrimary)
                        )
                    )
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isRailExpanded) Arrangement.Start else Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isRailExpanded) 12.dp else 4.dp)
        ) {
            // Icon + Collapsed Micro Status Dot
            Box(contentAlignment = Alignment.Center) {
                if (entry.isPrimaryMedia && entry.targetScreen != null) {
                    val iconTint = when {
                        isSelected -> railAccentHighlight
                        isItemFocused -> Color.White
                        else -> Color.White.copy(alpha = 0.68f)
                    }
                    SetPlaySectionNavIcon(
                        screen = entry.targetScreen,
                        isSelected = isSelected,
                        tint = iconTint,
                        accentTint = if (isSelected) railAccentPrimary else Color.Transparent,
                        modifier = Modifier.size(23.dp)
                    )
                } else if (entry.systemIcon != null) {
                    val utilTint = when {
                        isSelected -> railAccentHighlight
                        isItemFocused -> Color.White
                        else -> Color.White.copy(alpha = 0.70f)
                    }
                    Icon(
                        imageVector = entry.systemIcon,
                        contentDescription = null,
                        tint = utilTint,
                        modifier = Modifier.size(19.dp)
                    )
                }

                if (!isRailExpanded && entry.hasCollapsedDot) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 3.dp, y = (-2).dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(entry.dotColor)
                    )
                }
            }

            // Expanded Label + Optional Live / Favorites / Alert Badge Pill
            AnimatedVisibility(
                visible = isRailExpanded,
                enter = fadeIn(tween(180)) + expandHorizontally(expandFrom = Alignment.Start),
                exit = fadeOut(tween(130)) + shrinkHorizontally(shrinkTowards = Alignment.Start)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp)
                ) {
                    Text(
                        text = entry.label,
                        color = when {
                            isItemFocused -> Color.White
                            isSelected -> Color.White
                            entry.isPrimaryMedia -> Color.White.copy(alpha = 0.80f)
                            else -> Color.White.copy(alpha = 0.70f)
                        },
                        fontSize = if (entry.isPrimaryMedia) 13.sp else 12.sp,
                        fontWeight = when {
                            isItemFocused -> FontWeight.ExtraBold
                            isSelected -> FontWeight.Bold
                            else -> FontWeight.Medium
                        },
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (!entry.badgeText.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = if (entry.hasCollapsedDot) {
                                entry.dotColor.copy(alpha = 0.24f)
                            } else {
                                railAccentPrimary.copy(alpha = 0.22f)
                            },
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(
                                width = 0.8.dp,
                                color = if (entry.hasCollapsedDot) entry.dotColor else railAccentPrimary
                            )
                        ) {
                            Text(
                                text = entry.badgeText,
                                color = Color.White,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}




