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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
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
import com.example.ui.epg.EpgGuideScreen
import com.example.ui.favorites.FavoritesScreen
import com.example.ui.home.HomeScreen
import com.example.ui.livetv.LiveTvScreen
import com.example.ui.livetv.LiveTvViewModel
import com.example.ui.parental.ParentalControlsScreen
import com.example.ui.player.PlayerScreen
import com.example.ui.player.PlayerViewModel
import com.example.ui.playlists.PlaylistsScreen
import com.example.ui.search.GlobalSearchScreen
import com.example.ui.theme.AmberGold
import com.example.ui.theme.Champagne
import com.example.ui.theme.DarkGraphite
import com.example.ui.theme.FavoritesBotanicalBorder
import com.example.ui.theme.FavoritesEvergreenSurface
import com.example.ui.theme.FavoritesObsidianBase
import com.example.ui.theme.FavoritesPaleMint
import com.example.ui.theme.FavoritesSagePrimary
import com.example.ui.theme.KickoffCeruleanBlue
import com.example.ui.theme.KickoffCoralRed
import com.example.ui.theme.KickoffOceanBase
import com.example.ui.theme.KickoffOceanSurface
import com.example.ui.theme.KickoffPeachCream
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

    val selectedMatchDay by mainViewModel.selectedMatchDay.collectAsStateWithLifecycle()
    val matchesByDay by mainViewModel.matchesByDay.collectAsStateWithLifecycle()
    val todayMatches by mainViewModel.todayMatches.collectAsStateWithLifecycle()
    val favorites by mainViewModel.favorites.collectAsStateWithLifecycle()
    val liveCategories by liveTvViewModel.categories.collectAsStateWithLifecycle()
    val movieCategories by vodViewModel.movieCategories.collectAsStateWithLifecycle()
    val seriesCategories by vodViewModel.seriesCategories.collectAsStateWithLifecycle()

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
            AppScreen.EPG -> "Live TV Program Guide"
            AppScreen.PLAYLISTS -> activePlaylist?.name ?: "Account Management"
            AppScreen.GLOBAL_SEARCH -> "Channels, Movies & Series"
            AppScreen.PARENTAL_SETTINGS -> "PIN & Category Locks"
        }
    }

    var isFullScreenPlayer by remember { mutableStateOf(false) }
    var showNavigationMenuSheet by remember { mutableStateOf(false) }
    var pendingStreamToPlay by remember { mutableStateOf<StreamEntity?>(null) }
    var showPinDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

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
    BackHandler(enabled = isFullScreenPlayer || currentScreen != AppScreen.HOME) {
        if (isFullScreenPlayer) {
            isFullScreenPlayer = false
        } else {
            mainViewModel.setScreen(AppScreen.HOME)
        }
    }

    // Function to safely attempt playback with parental check
    val requestPlayStream: (StreamEntity) -> Unit = { stream ->
        scope.launch {
            val isLocked = stream.isLocked || mainViewModel.repository.isCategoryLocked(stream.categoryId)
            if (isLocked && !isParentalUnlocked) {
                pendingStreamToPlay = stream
                showPinDialog = true
            } else {
                isFullScreenPlayer = true
                activePlaylist?.let { playlist ->
                    playerViewModel.playStream(stream, playlist)
                }
            }
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
            }
        )
    } else {
        val targetSectionBaseColor = when (currentScreen) {
            AppScreen.HOME -> KickoffOceanBase
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

        Scaffold(
            containerColor = sectionBaseColor,
            topBar = {
                TopStreamingBar(
                    currentScreen = currentScreen,
                    activePlaylist = activePlaylist,
                    sectionDetailSummary = topSectionDetailText,
                    liveMatchesCount = liveMatchesCount,
                    onDrawerMenuClick = { showNavigationMenuSheet = true },
                    onCastOrAccountsClick = { mainViewModel.setScreen(AppScreen.PLAYLISTS) },
                    onSearchClick = { mainViewModel.setScreen(AppScreen.GLOBAL_SEARCH) }
                )
            },
            bottomBar = {
                BottomStreamingNav(
                    currentScreen = currentScreen,
                    isStreamPlaying = playerUiState.currentStream != null && playerUiState.isPlaying,
                    liveMatchesCount = liveMatchesCount,
                    favoritesCount = favorites.size,
                    onScreenSelected = { screen ->
                        mainViewModel.setScreen(screen)
                    }
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .testTag("main_scaffold")
        ) { innerPadding ->
            BoxWithConstraints(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
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
                            onChannelClick = requestPlayStream
                        )
                        AppScreen.MOVIES -> VodScreen(
                            sectionTab = VodTab.MOVIES,
                            viewModel = vodViewModel,
                            activePlaylist = activePlaylist,
                            onItemClick = requestPlayStream
                        )
                        AppScreen.SERIES -> VodScreen(
                            sectionTab = VodTab.SERIES,
                            viewModel = vodViewModel,
                            activePlaylist = activePlaylist,
                            onItemClick = requestPlayStream
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
                        AppScreen.EPG -> EpgGuideScreen(
                            viewModel = mainViewModel,
                            activePlaylist = activePlaylist,
                            onChannelClick = requestPlayStream
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

                // Floating Draggable In-App Picture-in-Picture (PiP) Video Window
                if (playerUiState.currentStream != null) {
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
    }

    // Royal Gold Navigation Drawer Menu Sheet (Accounts, EPG Guide, Parental Controls)
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
                    title = "Live TV EPG Guide",
                    subtitle = "Browse program schedules & catchup archive",
                    icon = Icons.AutoMirrored.Filled.EventNote,
                    onClick = {
                        showNavigationMenuSheet = false
                        mainViewModel.setScreen(AppScreen.EPG)
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

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Salted SHA-256 Parental PIN Challenge Modal with Brute-Force Rate Limiting
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = {
                showPinDialog = false
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
                        "This stream belongs to a locked category. Enter your 4-digit PIN to watch (Default: 0000):",
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
                                    isFullScreenPlayer = true
                                    activePlaylist?.let { playlist ->
                                        playerViewModel.playStream(stream, playlist)
                                    }
                                }
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
                    Text("Unlock & Play", color = DarkGraphite, fontWeight = FontWeight.ExtraBold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPinDialog = false
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
                    .background(MetalCharcoal)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stream.name,
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
                        .size(26.dp)
                        .background(AmberGold, CircleShape)
                        .testTag("pip_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = DarkGraphite,
                        modifier = Modifier.size(15.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = { isLargePip = !isLargePip },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoSizeSelectLarge,
                        contentDescription = "Resize PiP",
                        tint = Champagne,
                        modifier = Modifier.size(15.dp)
                    )
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    IconButton(
                        onClick = onEnterSystemPip,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureInPictureAlt,
                            contentDescription = "System PiP",
                            tint = AmberGold,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onExpandFullScreen,
                    modifier = Modifier
                        .size(26.dp)
                        .testTag("pip_expand_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInFull,
                        contentDescription = "Expand Fullscreen",
                        tint = PaleGold,
                        modifier = Modifier.size(15.dp)
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
    onDrawerMenuClick: () -> Unit,
    onCastOrAccountsClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val (targetSurface, targetPrimary, sectionBadgeTitle) = when (currentScreen) {
        AppScreen.HOME -> Triple(KickoffOceanBase, KickoffTropicalTeal, "KICKOFF ARENA")
        AppScreen.LIVE_TV -> Triple(TvMidnightSlate, TvBroadcastScarlet, "LIVE TV")
        AppScreen.MOVIES -> Triple(MoviesSapphireNavy, MoviesCyanTeal, "MOVIES")
        AppScreen.SERIES -> Triple(SeriesPineBase, SeriesMistTeal, "SERIES")
        AppScreen.FAVORITES -> Triple(FavoritesObsidianBase, FavoritesSagePrimary, "FAVORITES")
        AppScreen.EPG -> Triple(TvMidnightSlate, AmberGold, "EPG GUIDE")
        AppScreen.PLAYLISTS -> Triple(DarkGraphite, AmberGold, "XTREAM SERVERS")
        AppScreen.GLOBAL_SEARCH -> Triple(DarkGraphite, KickoffTropicalTeal, "GLOBAL SEARCH")
        AppScreen.PARENTAL_SETTINGS -> Triple(DarkGraphite, AmberGold, "PARENTAL LOCK")
    }

    val sectionUnderlineStops = when (currentScreen) {
        AppScreen.HOME -> listOf(AmberGold, KickoffTropicalTeal, KickoffPeachCream, KickoffCoralRed)
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
                    .clickable { onCastOrAccountsClick() }
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

            Spacer(modifier = Modifier.weight(1f))

            GhostTopBarIconButton(
                icon = Icons.Default.Cast,
                contentDescription = "Cast & Xtream Servers",
                iconTint = if (activePlaylist != null) AmberGold else Color.White.copy(alpha = 0.88f),
                accentColor = sectionPrimaryAccent,
                showStatusDot = activePlaylist != null,
                statusDotColor = sectionPrimaryAccent,
                onClick = onCastOrAccountsClick,
                testTag = "top_playlists_btn"
            )

            Spacer(modifier = Modifier.width(6.dp))

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
            .clickable(onClick = onClick)
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

private enum class NavKineticStyle {
    SOCCER_ORBIT,
    BROADCAST_PULSE,
    CLAPPER_SNAP,
    CASCADE_LIFT,
    STAR_CROWN
}

private data class LuxuryNavItemSpec(
    val screen: AppScreen,
    val label: String,
    val defaultMicroTag: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val crestNeonColor: Color,
    val crestCoreHighlight: Color,
    val chamberTint: Color,
    val activeLabelColor: Color,
    val kineticStyle: NavKineticStyle
)

/**
 * SetPlay "Chronograph Orbital & Wave-Crest" Luxury Bottom Navigation Dock:
 * 1. Hydrodynamic Wave-Crest Physics: Dual-spring (`fastSlot` + `slowSlot`) computes real-time
 *    in-flight velocity so the wave crest widens and dips slightly while gliding across tabs,
 *    then surges up to its full sculpted apex upon arrival.
 * 2. 5 Bespoke Luxury Section Chambers:
 *    - Active Section elevates into a 3D Sapphire & Rotating Sweep-Bezel Jewel Medallion with
 *      section-specific kinetic icon choreography (Soccer 360° orbit, Broadcast pulse, Cinema
 *      clapper snap, Series cascade pop, Favorites celestial star burst) and an illuminated
 *      micro-status stage pill underneath ("X LIVE", "ON AIR", "4K CINEMA", "EPISODES", "SAVED").
 *    - Inactive Sections rest inside recessed smoked-glass wells with top specular rims,
 *      automotive instrument divider ticks, and live contextual telemetry micro-badges
 *      (live match counter on KickOff, animated 3-bar equalizer on Live TV, saved counter on Favorites).
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

    val targetDockSurface = when (currentScreen) {
        AppScreen.HOME -> Color(0xFF06151E)
        AppScreen.LIVE_TV -> Color(0xFF131622)
        AppScreen.MOVIES -> Color(0xFF061928)
        AppScreen.SERIES -> Color(0xFF081C1B)
        AppScreen.FAVORITES -> Color(0xFF061816)
        else -> MetalCharcoal
    }

    val navContainerColor by animateColorAsState(
        targetValue = targetDockSurface,
        animationSpec = tween(durationMillis = 340),
        label = "bottom_dock_surface"
    )

    val navItems = remember {
        listOf(
            LuxuryNavItemSpec(
                screen = AppScreen.HOME,
                label = "KickOff",
                defaultMicroTag = "MATCHES",
                filledIcon = Icons.Filled.SportsSoccer,
                outlinedIcon = Icons.Outlined.SportsSoccer,
                crestNeonColor = Color(0xFF2EC4B6),       // Vibrant KickOff Tropical Cyan-Teal
                crestCoreHighlight = KickoffPeachCream,   // Warm Sand-Gold Crown Highlight
                chamberTint = KickoffTropicalTeal,
                activeLabelColor = Color.White,
                kineticStyle = NavKineticStyle.SOCCER_ORBIT
            ),
            LuxuryNavItemSpec(
                screen = AppScreen.LIVE_TV,
                label = "Live TV",
                defaultMicroTag = "CHANNELS",
                filledIcon = Icons.Filled.LiveTv,
                outlinedIcon = Icons.Outlined.LiveTv,
                crestNeonColor = Color(0xFFFF4D5E),       // Vibrant Broadcast Crimson-Scarlet
                crestCoreHighlight = AmberGold,           // SetPlay Royal Gold Highlight
                chamberTint = TvBroadcastScarlet,
                activeLabelColor = Color.White,
                kineticStyle = NavKineticStyle.BROADCAST_PULSE
            ),
            LuxuryNavItemSpec(
                screen = AppScreen.MOVIES,
                label = "Movies",
                defaultMicroTag = "4K CINEMA",
                filledIcon = Icons.Filled.Movie,
                outlinedIcon = Icons.Outlined.Movie,
                crestNeonColor = Color(0xFF38E4AE),       // Luminous Cinema Mint-Cyan
                crestCoreHighlight = MoviesMintSpring,
                chamberTint = MoviesCyanTeal,
                activeLabelColor = Color.White,
                kineticStyle = NavKineticStyle.CLAPPER_SNAP
            ),
            LuxuryNavItemSpec(
                screen = AppScreen.SERIES,
                label = "Series",
                defaultMicroTag = "EPISODES",
                filledIcon = Icons.Filled.VideoLibrary,
                outlinedIcon = Icons.Outlined.VideoLibrary,
                crestNeonColor = SeriesIceAqua,           // Series Ice-Aqua Neon
                crestCoreHighlight = PaleGold,
                chamberTint = SeriesSeaJade,
                activeLabelColor = Color.White,
                kineticStyle = NavKineticStyle.CASCADE_LIFT
            ),
            LuxuryNavItemSpec(
                screen = AppScreen.FAVORITES,
                label = "Favorites",
                defaultMicroTag = "COLLECTION",
                filledIcon = Icons.Filled.Star,
                outlinedIcon = Icons.Outlined.StarOutline,
                crestNeonColor = FavoritesSagePrimary,    // Botanical Emerald-Sage Neon
                crestCoreHighlight = AmberGold,
                chamberTint = FavoritesBotanicalBorder,
                activeLabelColor = FavoritesPaleMint,
                kineticStyle = NavKineticStyle.STAR_CROWN
            )
        )
    }

    val selectedIndex = remember(currentScreen, navItems) {
        navItems.indexOfFirst { it.screen == currentScreen }.let { if (it >= 0) it else 0 }
    }
    val activeItemSpec = navItems[selectedIndex]

    // Dual-spring hydrodynamic wave physics:
    // - `fastSlot` leads the wave crest movement
    // - `slowSlot` trails slightly, allowing us to measure in-flight velocity (`transitStretch`)
    //   so the wave stretches horizontally and dips slightly in transit before surging up on arrival
    val fastSlot by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "wave_crest_fast_slot"
    )
    val slowSlot by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = 0.84f,
            stiffness = Spring.StiffnessLow
        ),
        label = "wave_crest_slow_slot"
    )

    // Continuous luxury chronograph animations:
    // 1. `auroraPulse` breathes the spotlight chamber and ambient halo
    // 2. `orbitalAngle` rotates the 3D sapphire-gold sweep bezel around the active section orb
    val infiniteTransition = rememberInfiniteTransition(label = "luxury_nav_chronograph")
    val auroraPulse by infiniteTransition.animateFloat(
        initialValue = 0.84f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aurora_pulse_factor"
    )
    val orbitalAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbital_bezel_angle"
    )

    val neonPrimary by animateColorAsState(
        targetValue = activeItemSpec.crestNeonColor,
        animationSpec = tween(280),
        label = "crest_neon_primary"
    )
    val neonCore by animateColorAsState(
        targetValue = activeItemSpec.crestCoreHighlight,
        animationSpec = tween(280),
        label = "crest_neon_core"
    )
    val chamberAccent by animateColorAsState(
        targetValue = activeItemSpec.chamberTint,
        animationSpec = tween(280),
        label = "chamber_accent"
    )

    val crestHeightDp = 18.dp
    val dockBodyHeightDp = 62.dp
    val totalHeightDp = crestHeightDp + dockBodyHeightDp
    val horizontalInsetDp = 12.dp
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .height(totalHeightDp)
            .testTag("bottom_nav")
    ) {
        val slotCount = navItems.size
        val dockWidthPx = with(density) { maxWidth.toPx() }
        val insetPxForGesture = with(density) { horizontalInsetDp.toPx() }

        // 1. Sculpted Wave-Crest Vessel, Precision Instrument Chambers & Multi-Layer Neon-Gold Rim
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val barTopY = crestHeightDp.toPx()
            val barBottomY = size.height
            val barBodyHeightPx = barBottomY - barTopY

            val insetPx = horizontalInsetDp.toPx()
            val usableWidthPx = (size.width - insetPx * 2f).coerceAtLeast(1f)
            val slotWidthPx = if (slotCount > 0) usableWidthPx / slotCount else usableWidthPx

            // Hydrodynamic wave state from dual springs
            val transitStretch = abs(fastSlot - slowSlot).coerceIn(0f, 1.2f)
            val effectiveSlot = fastSlot * 0.74f + slowSlot * 0.26f

            // While in transit between tabs, the wave lowers slightly and widens like a liquid wave;
            // when locking onto the target tab, it surges up to its full sculpted apex
            val dynamicDipPx = (transitStretch * 3.8.dp.toPx()).coerceAtMost(4.2.dp.toPx())
            val crestPeakY = 1.5.dp.toPx() + dynamicDipPx

            val crestCenterX = insetPx + (effectiveSlot + 0.5f) * slotWidthPx
            val baseHalfWidth = (slotWidthPx * 0.65f).coerceIn(32.dp.toPx(), 46.dp.toPx())
            val crestHalfWidth = baseHalfWidth * (1f + transitStretch * 0.22f)

            val crestStartX = crestCenterX - crestHalfWidth
            val crestEndX = crestCenterX + crestHalfWidth

            // Adaptive stadium corner radii so the wave peak blends seamlessly even at index 0 & 4
            val bottomCornerR = (barBodyHeightPx * 0.48f).coerceAtMost(29.dp.toPx())
            val maxTopCornerR = (barBodyHeightPx * 0.46f).coerceAtMost(26.dp.toPx())
            val leftTopCornerR = minOf(maxTopCornerR, (crestStartX - 1.dp.toPx()).coerceAtLeast(12.dp.toPx()))
            val rightTopCornerR = minOf(maxTopCornerR, (size.width - crestEndX - 1.dp.toPx()).coerceAtLeast(12.dp.toPx()))

            val safeCrestStartX = crestStartX.coerceAtLeast(leftTopCornerR)
            val safeCrestEndX = crestEndX.coerceAtMost(size.width - rightTopCornerR)

            val dockPath = Path().apply {
                moveTo(leftTopCornerR, barTopY)

                if (safeCrestStartX > leftTopCornerR) {
                    lineTo(safeCrestStartX, barTopY)
                }

                // Left ascending cubic-bezier S-curve rising smoothly to the wave crest apex
                cubicTo(
                    x1 = safeCrestStartX + (crestCenterX - safeCrestStartX) * 0.46f,
                    y1 = barTopY,
                    x2 = crestCenterX - (crestCenterX - safeCrestStartX) * 0.38f,
                    y2 = crestPeakY,
                    x3 = crestCenterX,
                    y3 = crestPeakY
                )

                // Right descending cubic-bezier S-curve flowing smoothly back to the top bar edge
                cubicTo(
                    x1 = crestCenterX + (safeCrestEndX - crestCenterX) * 0.38f,
                    y1 = crestPeakY,
                    x2 = safeCrestEndX - (safeCrestEndX - crestCenterX) * 0.46f,
                    y2 = barTopY,
                    x3 = safeCrestEndX,
                    y3 = barTopY
                )

                if (safeCrestEndX < size.width - rightTopCornerR) {
                    lineTo(size.width - rightTopCornerR, barTopY)
                }

                arcTo(
                    rect = Rect(
                        left = size.width - 2f * rightTopCornerR,
                        top = barTopY,
                        right = size.width,
                        bottom = barTopY + 2f * rightTopCornerR
                    ),
                    startAngleDegrees = -90f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                lineTo(size.width, barBottomY - bottomCornerR)

                arcTo(
                    rect = Rect(
                        left = size.width - 2f * bottomCornerR,
                        top = barBottomY - 2f * bottomCornerR,
                        right = size.width,
                        bottom = barBottomY
                    ),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                lineTo(bottomCornerR, barBottomY)

                arcTo(
                    rect = Rect(
                        left = 0f,
                        top = barBottomY - 2f * bottomCornerR,
                        right = 2f * bottomCornerR,
                        bottom = barBottomY
                    ),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                lineTo(0f, barTopY + leftTopCornerR)

                arcTo(
                    rect = Rect(
                        left = 0f,
                        top = barTopY,
                        right = 2f * leftTopCornerR,
                        bottom = barTopY + 2f * leftTopCornerR
                    ),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                close()
            }

            // A. Breathing Ambient Outer Crest Halo (glowing softly above the rising wave peak)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        neonPrimary.copy(alpha = 0.34f * auroraPulse),
                        neonCore.copy(alpha = 0.14f * auroraPulse),
                        Color.Transparent
                    ),
                    center = Offset(crestCenterX, barTopY),
                    radius = crestHalfWidth * 1.38f
                ),
                radius = crestHalfWidth * 1.38f,
                center = Offset(crestCenterX, barTopY)
            )

            // B. Deep Obsidian-Glass Base Fill with subtle vertical depth gradient
            drawPath(
                path = dockPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        navContainerColor.copy(alpha = 0.97f),
                        Color(0xFF060C14).copy(alpha = 0.99f),
                        Color(0xFF03060A)
                    ),
                    startY = crestPeakY,
                    endY = barBottomY
                )
            )

            // C. Clipped Interior Lighting: Instrument Dividers, Spotlight Chamber & Laser Horizon
            clipPath(dockPath) {
                val chamberHalfWidth = slotWidthPx * 0.49f
                val chamberLeft = crestCenterX - chamberHalfWidth
                val chamberRight = crestCenterX + chamberHalfWidth

                // 1. Subtle Top Specular Glass Reflection across inactive bar shoulders
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.065f),
                            Color.Transparent
                        ),
                        startY = barTopY,
                        endY = barTopY + 20.dp.toPx()
                    ),
                    topLeft = Offset(0f, barTopY),
                    size = androidx.compose.ui.geometry.Size(size.width, 20.dp.toPx())
                )

                // 2. Luxury Instrument Cluster Divider Hairlines between inactive slots
                for (dividerIndex in 1 until slotCount) {
                    val dividerX = insetPx + dividerIndex * slotWidthPx
                    val distFromCrest = abs(dividerX - crestCenterX) / slotWidthPx
                    val dividerAlpha = ((distFromCrest - 0.55f) / 0.65f).coerceIn(0f, 1f) * 0.085f
                    if (dividerAlpha > 0.005f) {
                        drawLine(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = dividerAlpha),
                                    Champagne.copy(alpha = dividerAlpha * 0.6f),
                                    Color.Transparent
                                ),
                                startY = barTopY + 10.dp.toPx(),
                                endY = barBottomY - 10.dp.toPx()
                            ),
                            start = Offset(dividerX, barTopY + 10.dp.toPx()),
                            end = Offset(dividerX, barBottomY - 10.dp.toPx()),
                            strokeWidth = 0.8.dp.toPx()
                        )
                    }
                }

                // 3. Sculpted Spotlight Chamber Column under the active wave peak
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            neonPrimary.copy(alpha = 0.34f * auroraPulse),
                            chamberAccent.copy(alpha = 0.22f * auroraPulse),
                            chamberAccent.copy(alpha = 0.06f)
                        ),
                        startY = crestPeakY,
                        endY = barBottomY
                    ),
                    topLeft = Offset(chamberLeft, crestPeakY),
                    size = androidx.compose.ui.geometry.Size(chamberHalfWidth * 2f, barBottomY - crestPeakY),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx(), 18.dp.toPx())
                )

                // 4. Crisp Left & Right Spotlight Curtain Filaments
                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            neonCore.copy(alpha = 0.38f * auroraPulse),
                            neonPrimary.copy(alpha = 0.16f),
                            Color.Transparent
                        ),
                        startY = barTopY - 2.dp.toPx(),
                        endY = barBottomY - 4.dp.toPx()
                    ),
                    start = Offset(chamberLeft, barTopY - 2.dp.toPx()),
                    end = Offset(chamberLeft, barBottomY - 4.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            neonCore.copy(alpha = 0.38f * auroraPulse),
                            neonPrimary.copy(alpha = 0.16f),
                            Color.Transparent
                        ),
                        startY = barTopY - 2.dp.toPx(),
                        endY = barBottomY - 4.dp.toPx()
                    ),
                    start = Offset(chamberRight, barTopY - 2.dp.toPx()),
                    end = Offset(chamberRight, barBottomY - 4.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )

                // 5. Concentrated Radial Spotlight Bloom right inside the wave crest behind the lifted orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            neonCore.copy(alpha = 0.38f * auroraPulse),
                            neonPrimary.copy(alpha = 0.28f * auroraPulse),
                            Color.Transparent
                        ),
                        center = Offset(crestCenterX, barTopY + 4.dp.toPx()),
                        radius = crestHalfWidth * 1.02f
                    ),
                    radius = crestHalfWidth * 1.02f,
                    center = Offset(crestCenterX, barTopY + 4.dp.toPx())
                )

                // 6. SetPlay Signature Gold-to-Neon Laser Pedestal Bar at the base of the active section
                val floorBarWidth = 28.dp.toPx()
                val floorBarHeight = 2.4.dp.toPx()
                val floorBarLeft = crestCenterX - floorBarWidth / 2f
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            AmberGold.copy(alpha = 0.92f),
                            neonCore,
                            neonPrimary,
                            AmberGold.copy(alpha = 0.92f),
                            Color.Transparent
                        ),
                        startX = floorBarLeft - 4.dp.toPx(),
                        endX = floorBarLeft + floorBarWidth + 4.dp.toPx()
                    ),
                    topLeft = Offset(floorBarLeft, barBottomY - 2.8.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(floorBarWidth, floorBarHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(floorBarHeight, floorBarHeight)
                )
            }

            // D. Multi-Layered Neon Rim Stroke along the entire Wave-Crest & Stadium Contour
            drawPath(
                path = dockPath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        neonPrimary.copy(alpha = 0.42f * auroraPulse),
                        neonPrimary.copy(alpha = 0.16f * auroraPulse),
                        neonPrimary.copy(alpha = 0.04f)
                    ),
                    center = Offset(crestCenterX, crestPeakY + 6.dp.toPx()),
                    radius = size.width * 0.56f
                ),
                style = Stroke(
                    width = 5.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            drawPath(
                path = dockPath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        neonCore,
                        neonPrimary,
                        neonPrimary.copy(alpha = 0.78f),
                        neonPrimary.copy(alpha = 0.38f)
                    ),
                    center = Offset(crestCenterX, crestPeakY),
                    radius = size.width * 0.86f
                ),
                style = Stroke(
                    width = 1.75.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Inner 3D Specular Crystal Crown Arc along the wave crest apex
            val crownArcPath = Path().apply {
                moveTo(safeCrestStartX + 4.dp.toPx(), barTopY + 1.5.dp.toPx())
                cubicTo(
                    x1 = safeCrestStartX + (crestCenterX - safeCrestStartX) * 0.46f,
                    y1 = barTopY + 1.5.dp.toPx(),
                    x2 = crestCenterX - (crestCenterX - safeCrestStartX) * 0.38f,
                    y2 = crestPeakY + 1.8.dp.toPx(),
                    x3 = crestCenterX,
                    y3 = crestPeakY + 1.8.dp.toPx()
                )
                cubicTo(
                    x1 = crestCenterX + (safeCrestEndX - crestCenterX) * 0.38f,
                    y1 = crestPeakY + 1.8.dp.toPx(),
                    x2 = safeCrestEndX - (safeCrestEndX - crestCenterX) * 0.46f,
                    y2 = barTopY + 1.5.dp.toPx(),
                    x3 = safeCrestEndX - 4.dp.toPx(),
                    y3 = barTopY + 1.5.dp.toPx()
                )
            }
            drawPath(
                path = crownArcPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        neonCore.copy(alpha = 0.75f),
                        Color.White.copy(alpha = 0.95f),
                        neonCore.copy(alpha = 0.75f),
                        Color.Transparent
                    ),
                    startX = safeCrestStartX,
                    endX = safeCrestEndX
                ),
                style = Stroke(
                    width = 1.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }

        // 2. Interactive 5-Section Concierge Row: Supports Both Tap & Horizontal Wave-Scrubbing Gesture
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalInsetDp)
                .pointerInput(navItems, currentScreen, dockWidthPx) {
                    detectHorizontalDragGestures { change, _ ->
                        change.consume()
                        val usableW = (dockWidthPx - insetPxForGesture * 2f).coerceAtLeast(1f)
                        val relX = change.position.x.coerceIn(0f, usableW - 1f)
                        val targetIdx = ((relX / usableW) * navItems.size).toInt().coerceIn(0, navItems.lastIndex)
                        val targetScreen = navItems[targetIdx].screen
                        if (targetScreen != currentScreen) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onScreenSelected(targetScreen)
                        }
                    }
                },
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            navItems.forEach { item ->
                val isSelected = currentScreen == item.screen

                // Dynamic live micro-tag for the active stage pill
                val activeMicroTag = when (item.screen) {
                    AppScreen.HOME -> if (liveMatchesCount > 0) "$liveMatchesCount LIVE" else item.defaultMicroTag
                    AppScreen.LIVE_TV -> if (isStreamPlaying) "ON AIR" else item.defaultMicroTag
                    AppScreen.FAVORITES -> if (favoritesCount > 0) "$favoritesCount SAVED" else item.defaultMicroTag
                    else -> item.defaultMicroTag
                }

                // Dynamic live badge text for inactive smoked-glass wells
                val inactiveBadgeCountText = when (item.screen) {
                    AppScreen.HOME -> if (liveMatchesCount > 0) liveMatchesCount.coerceAtMost(99).toString() else null
                    AppScreen.FAVORITES -> if (favoritesCount > 0) favoritesCount.coerceAtMost(99).toString() else null
                    else -> null
                }

                LuxuryBottomNavSectionSlot(
                    item = item,
                    isSelected = isSelected,
                    activeMicroTag = activeMicroTag,
                    inactiveBadgeText = inactiveBadgeCountText,
                    showLiveEqualizer = item.screen == AppScreen.LIVE_TV && isStreamPlaying,
                    hasLivePulseDot = (item.screen == AppScreen.HOME && liveMatchesCount > 0) ||
                        (item.screen == AppScreen.LIVE_TV && isStreamPlaying),
                    orbitalAngle = orbitalAngle,
                    auroraPulse = auroraPulse,
                    totalHeightDp = totalHeightDp,
                    onSelect = {
                        if (currentScreen != item.screen) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onScreenSelected(item.screen)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(totalHeightDp)
                        .testTag("nav_item_${item.screen.name.lowercase()}")
                )
            }
        }
    }
}

@Composable
private fun LuxuryBottomNavSectionSlot(
    item: LuxuryNavItemSpec,
    isSelected: Boolean,
    activeMicroTag: String,
    inactiveBadgeText: String?,
    showLiveEqualizer: Boolean,
    hasLivePulseDot: Boolean,
    orbitalAngle: Float,
    auroraPulse: Float,
    totalHeightDp: androidx.compose.ui.unit.Dp,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Active medallion lifts into the sculpted wave crest apex; inactive well sits cleanly in the dock body
    val medallionOffsetY by animateDpAsState(
        targetValue = if (isSelected) (-5).dp else 2.dp,
        animationSpec = spring(
            dampingRatio = 0.66f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "medallion_offset_${item.label}"
    )

    val medallionScale by animateFloatAsState(
        targetValue = if (isSelected) 1.12f else 0.96f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "medallion_scale_${item.label}"
    )

    val activeProgress by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = 240),
        label = "active_progress_${item.label}"
    )

    // Section-specific kinetic choreography when selected
    val targetKineticRotation = remember(isSelected, item.kineticStyle) {
        if (!isSelected) {
            0f
        } else {
            when (item.kineticStyle) {
                NavKineticStyle.SOCCER_ORBIT -> 360f
                NavKineticStyle.STAR_CROWN -> 72f
                NavKineticStyle.CLAPPER_SNAP -> -12f
                NavKineticStyle.BROADCAST_PULSE -> 0f
                NavKineticStyle.CASCADE_LIFT -> 0f
            }
        }
    }

    val kineticRotationZ by animateFloatAsState(
        targetValue = targetKineticRotation,
        animationSpec = spring(
            dampingRatio = 0.62f,
            stiffness = Spring.StiffnessLow
        ),
        label = "kinetic_rot_${item.label}"
    )

    // Settle clapper tilt back to 0° after initial snap while keeping subtle playful poise
    val iconDisplayRotation = when (item.kineticStyle) {
        NavKineticStyle.CLAPPER_SNAP -> kineticRotationZ * (1f - activeProgress * 0.65f)
        else -> kineticRotationZ
    }

    val labelColor by animateColorAsState(
        targetValue = if (isSelected) item.activeLabelColor else Champagne.copy(alpha = 0.68f),
        animationSpec = tween(220),
        label = "label_color_${item.label}"
    )

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSelect
            )
            .padding(bottom = 5.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            // 1. Medallion / Smoked-Glass Well Container
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(y = medallionOffsetY)
                    .size(38.dp)
                    .graphicsLayer {
                        scaleX = medallionScale
                        scaleY = medallionScale
                    }
            ) {
                // Custom Canvas for 3D Sapphire Medallion (Active) & Recessed Smoked-Glass Well (Inactive)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val maxRadius = size.minDimension / 2f

                    if (activeProgress > 0.01f) {
                        // A. Outer Ambient Jewel Bloom
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    item.crestNeonColor.copy(alpha = 0.45f * activeProgress * auroraPulse),
                                    item.crestCoreHighlight.copy(alpha = 0.18f * activeProgress),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = maxRadius * 1.15f
                            ),
                            radius = maxRadius * 1.15f,
                            center = center
                        )

                        // B. Deep Sapphire-Obsidian Jewel Core Sphere
                        val orbRadius = maxRadius * 0.88f
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    item.chamberTint.copy(alpha = 0.92f * activeProgress),
                                    Color(0xFF07131E).copy(alpha = 0.96f * activeProgress),
                                    Color(0xFF040910).copy(alpha = 0.98f * activeProgress)
                                ),
                                center = Offset(center.x - orbRadius * 0.25f, center.y - orbRadius * 0.28f),
                                radius = orbRadius * 1.35f
                            ),
                            radius = orbRadius,
                            center = center
                        )

                        // C. Rotating Chronograph Sweep-Gradient Bezel Ring
                        rotate(degrees = orbitalAngle, pivot = center) {
                            drawCircle(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        item.crestNeonColor.copy(alpha = 0.95f * activeProgress),
                                        item.crestCoreHighlight.copy(alpha = 0.98f * activeProgress),
                                        Color.White.copy(alpha = 0.90f * activeProgress),
                                        item.crestNeonColor.copy(alpha = 0.25f * activeProgress),
                                        AmberGold.copy(alpha = 0.85f * activeProgress),
                                        item.crestNeonColor.copy(alpha = 0.95f * activeProgress)
                                    ),
                                    center = center
                                ),
                                radius = orbRadius,
                                center = center,
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                        }

                        // D. Top-Left Sapphire Crystal Specular Crescent Highlight
                        drawArc(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.55f * activeProgress),
                                    item.crestCoreHighlight.copy(alpha = 0.20f * activeProgress),
                                    Color.Transparent
                                ),
                                start = Offset(center.x - orbRadius, center.y - orbRadius),
                                end = Offset(center.x + orbRadius * 0.3f, center.y + orbRadius * 0.3f)
                            ),
                            startAngle = 195f,
                            sweepAngle = 120f,
                            useCenter = false,
                            topLeft = Offset(center.x - orbRadius * 0.78f, center.y - orbRadius * 0.78f),
                            size = androidx.compose.ui.geometry.Size(orbRadius * 1.56f, orbRadius * 1.56f),
                            style = Stroke(width = 1.1.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    if (activeProgress < 0.99f) {
                        // Recessed Smoked-Glass Capsule Well for Inactive Sections
                        val inactiveAlpha = 1f - activeProgress
                        val wellRadius = maxRadius * 0.82f
                        drawCircle(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.045f * inactiveAlpha),
                                    Color(0xFF050B12).copy(alpha = 0.55f * inactiveAlpha)
                                ),
                                startY = center.y - wellRadius,
                                endY = center.y + wellRadius
                            ),
                            radius = wellRadius,
                            center = center
                        )
                        // Subtle top glass rim border on inactive well
                        drawCircle(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.14f * inactiveAlpha),
                                    item.crestNeonColor.copy(alpha = 0.08f * inactiveAlpha),
                                    Color.Transparent
                                ),
                                startY = center.y - wellRadius,
                                endY = center.y + wellRadius
                            ),
                            radius = wellRadius,
                            center = center,
                            style = Stroke(width = 0.85.dp.toPx())
                        )
                    }
                }

                // Dual-Layer Holographic Icon with Kinetic Choreography
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.graphicsLayer {
                        rotationZ = iconDisplayRotation
                    }
                ) {
                    if (isSelected) {
                        // Soft illuminated core fill
                        Icon(
                            imageVector = item.filledIcon,
                            contentDescription = null,
                            tint = item.crestCoreHighlight.copy(alpha = 0.32f),
                            modifier = Modifier.size(21.dp)
                        )
                        // Crisp bright neon-white foreground icon
                        Icon(
                            imageVector = item.outlinedIcon,
                            contentDescription = item.label,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = item.outlinedIcon,
                            contentDescription = item.label,
                            tint = Champagne.copy(alpha = 0.68f),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                // Live Contextual Telemetry Badges on Medallion / Smoked-Glass Wells
                if (showLiveEqualizer) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 3.dp, y = (-1).dp)
                    ) {
                        LiveEqualizerMiniBadge(
                            accentColor = LiveRed,
                            auroraPulse = auroraPulse
                        )
                    }
                } else if (inactiveBadgeText != null) {
                    val isLiveMatchBadge = item.screen == AppScreen.HOME && hasLivePulseDot
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 5.dp, y = (-1).dp)
                            .background(
                                color = if (isLiveMatchBadge) LiveRed else Color(0xFF081520).copy(alpha = 0.96f),
                                shape = RoundedCornerShape(50)
                            )
                            .border(
                                width = 0.85.dp,
                                color = if (isLiveMatchBadge) Color.White.copy(alpha = 0.85f) else item.crestNeonColor.copy(alpha = 0.78f),
                                shape = RoundedCornerShape(50)
                            )
                            .padding(horizontal = 4.5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = inactiveBadgeText,
                            color = if (isLiveMatchBadge) Color.White else item.crestCoreHighlight,
                            fontSize = 7.5.sp,
                            lineHeight = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.15.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            // 2. Section Title Nameplate with clean breathing room above the laser pedestal
            Text(
                text = item.label,
                color = labelColor,
                fontSize = if (isSelected) 11.sp else 9.8.sp,
                lineHeight = 12.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                letterSpacing = if (isSelected) 0.35.sp else 0.15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

/**
 * Miniature 3-bar animated broadcast equalizer badge shown on the Live TV section
 * when a stream is actively playing while browsing another section.
 */
@Composable
private fun LiveEqualizerMiniBadge(
    accentColor: Color,
    auroraPulse: Float
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(1.2.dp),
        modifier = Modifier
            .background(
                color = Color(0xFF0A1018).copy(alpha = 0.94f),
                shape = RoundedCornerShape(5.dp)
            )
            .border(
                width = 0.7.dp,
                color = accentColor.copy(alpha = 0.75f),
                shape = RoundedCornerShape(5.dp)
            )
            .padding(horizontal = 3.dp, vertical = 2.dp)
            .height(8.dp)
    ) {
        val bar1Fraction = (0.45f + (auroraPulse - 0.84f) * 3.2f).coerceIn(0.35f, 1f)
        val bar2Fraction = (1f - (auroraPulse - 0.84f) * 2.8f).coerceIn(0.40f, 1f)
        val bar3Fraction = (0.60f + (auroraPulse - 0.84f) * 2.4f).coerceIn(0.35f, 1f)

        listOf(bar1Fraction, bar2Fraction, bar3Fraction).forEach { fraction ->
            Box(
                modifier = Modifier
                    .width(1.6.dp)
                    .height((7.dp * fraction).coerceAtLeast(2.5.dp))
                    .background(
                        color = accentColor,
                        shape = RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}


