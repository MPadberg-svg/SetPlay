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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.KickoffTropicalTeal
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
import com.example.util.SecurityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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
                    onDrawerMenuClick = { showNavigationMenuSheet = true },
                    onCastOrAccountsClick = { mainViewModel.setScreen(AppScreen.PLAYLISTS) },
                    onSearchClick = { mainViewModel.setScreen(AppScreen.GLOBAL_SEARCH) }
                )
            },
            bottomBar = {
                BottomStreamingNav(
                    currentScreen = currentScreen,
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
        else -> Triple(DarkGraphite, AmberGold, "VIP SUITE")
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
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tactile Frosted Glass Hamburger Button
                GhostTopBarIconButton(
                    icon = Icons.Default.Menu,
                    contentDescription = "Navigation Drawer Menu",
                    iconTint = Color.White.copy(alpha = 0.92f),
                    accentColor = sectionPrimaryAccent,
                    onClick = onDrawerMenuClick,
                    testTag = "top_drawer_menu_btn"
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Signature Set ▶ Play Wordmark with Section-Specific Spectrum Underline + Section Capsule
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onCastOrAccountsClick() }
                        .padding(vertical = 2.dp)
                        .testTag("setplay_creative_wordmark")
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Set",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Light,
                                letterSpacing = 0.5.sp
                            )
                            // Illuminated Section-Colored Play Prism between "Set" and "Play"
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .size(20.dp)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                sectionPrimaryAccent.copy(alpha = 0.32f),
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
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Play",
                                color = AmberGold,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.3.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Signature Section-Specific Spectrum Gradient Underline Bar
                        Box(
                            modifier = Modifier
                                .width(110.dp)
                                .height(2.6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.horizontalGradient(colors = sectionUnderlineStops)
                                )
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Section-Specific Identity Capsule (as shown in reference header)
                    Surface(
                        color = sectionPrimaryAccent.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, sectionPrimaryAccent.copy(alpha = 0.65f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(sectionPrimaryAccent)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = sectionBadgeTitle,
                                color = Color.White,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.6.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Tactile Frosted Glass Cast & Search Icon Buttons
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

            // Luminous Section-Accent Bottom Gradient Beam
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                sectionPrimaryAccent.copy(alpha = 0.65f),
                                AmberGold.copy(alpha = 0.45f),
                                Color.Transparent
                            )
                        )
                    )
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

private data class LuxuryNavItemSpec(
    val screen: AppScreen,
    val label: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val activeLabelColor: Color,
    val activeIconTint: Color
)

/**
 * Sculpted Obsidian Glass Island Dock with Morphing Horizontal Active Capsule:
 * - Active tab expands into an illuminated gradient capsule (`[ (⚽) KickOff ]`) with a top neon floodlight beam.
 * - Inactive tabs sit in a recessed obsidian-glass dock with frosted circular icon medallions and micro-labels.
 */
@Composable
fun BottomStreamingNav(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit
) {
    val targetDockSurface = when (currentScreen) {
        AppScreen.HOME -> Color(0xFF061620)
        AppScreen.LIVE_TV -> Color(0xFF171D28)
        AppScreen.MOVIES -> Color(0xFF092534)
        AppScreen.SERIES -> Color(0xFF0B2726)
        AppScreen.FAVORITES -> Color(0xFF051A18)
        else -> MetalCharcoal
    }

    val navContainerColor by animateColorAsState(
        targetValue = targetDockSurface,
        animationSpec = tween(durationMillis = 300),
        label = "bottom_dock_surface"
    )

    val navItems = remember {
        listOf(
            LuxuryNavItemSpec(
                screen = AppScreen.HOME,
                label = "KickOff",
                filledIcon = Icons.Filled.SportsSoccer,
                outlinedIcon = Icons.Outlined.SportsSoccer,
                primaryAccent = KickoffTropicalTeal,
                secondaryAccent = KickoffPeachCream,
                activeLabelColor = DarkGraphite,
                activeIconTint = KickoffTropicalTeal
            ),
            LuxuryNavItemSpec(
                screen = AppScreen.LIVE_TV,
                label = "Live TV",
                filledIcon = Icons.Filled.LiveTv,
                outlinedIcon = Icons.Outlined.LiveTv,
                primaryAccent = TvBroadcastScarlet,
                secondaryAccent = AmberGold,
                activeLabelColor = Color.White,
                activeIconTint = TvBroadcastScarlet
            ),
            LuxuryNavItemSpec(
                screen = AppScreen.MOVIES,
                label = "Movies",
                filledIcon = Icons.Filled.Movie,
                outlinedIcon = Icons.Outlined.Movie,
                primaryAccent = MoviesCyanTeal,
                secondaryAccent = MoviesMintSpring,
                activeLabelColor = DarkGraphite,
                activeIconTint = MoviesCyanTeal
            ),
            LuxuryNavItemSpec(
                screen = AppScreen.SERIES,
                label = "Series",
                filledIcon = Icons.Filled.VideoLibrary,
                outlinedIcon = Icons.Outlined.VideoLibrary,
                primaryAccent = SeriesIceAqua,
                secondaryAccent = SeriesMistTeal,
                activeLabelColor = SeriesPineBase,
                activeIconTint = SeriesIceAqua
            ),
            LuxuryNavItemSpec(
                screen = AppScreen.FAVORITES,
                label = "Favorites",
                filledIcon = Icons.Filled.Star,
                outlinedIcon = Icons.Outlined.StarOutline,
                primaryAccent = FavoritesSagePrimary,
                secondaryAccent = FavoritesPaleMint,
                activeLabelColor = FavoritesObsidianBase,
                activeIconTint = FavoritesSagePrimary
            )
        )
    }

    val activeItemSpec = navItems.firstOrNull { it.screen == currentScreen } ?: navItems.first()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 14.dp, vertical = 7.dp)
            .testTag("bottom_nav")
    ) {
        Surface(
            color = navContainerColor.copy(alpha = 0.96f),
            shape = RoundedCornerShape(30.dp),
            tonalElevation = 14.dp,
            shadowElevation = 18.dp,
            border = BorderStroke(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        activeItemSpec.primaryAccent.copy(alpha = 0.65f),
                        Color.White.copy(alpha = 0.14f),
                        AmberGold.copy(alpha = 0.40f)
                    )
                )
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { item ->
                    val isSelected = currentScreen == item.screen
                    val itemWeight by animateFloatAsState(
                        targetValue = if (isSelected) 1.42f else 0.88f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "nav_weight_${item.label}"
                    )
                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.06f else 0.96f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "nav_icon_scale_${item.label}"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(itemWeight)
                            .clip(RoundedCornerShape(22.dp))
                            .clickable { onScreenSelected(item.screen) }
                            .padding(horizontal = 2.dp, vertical = 2.dp)
                            .testTag("nav_item_${item.screen.name.lowercase()}")
                    ) {
                        // Top Floodlight Laser Beam on Active Tab
                        Box(
                            modifier = Modifier
                                .width(if (isSelected) 24.dp else 0.dp)
                                .height(2.5.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isSelected) {
                                        Brush.horizontalGradient(
                                            listOf(item.secondaryAccent, item.primaryAccent)
                                        )
                                    } else {
                                        Brush.horizontalGradient(
                                            listOf(Color.Transparent, Color.Transparent)
                                        )
                                    }
                                )
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        if (isSelected) {
                            // Expanded Illuminated Horizontal Capsule for Active Section
                            Surface(
                                color = Color.Transparent,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.9.dp, item.secondaryAccent.copy(alpha = 0.75f)),
                                modifier = Modifier
                                    .height(38.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                item.secondaryAccent,
                                                item.primaryAccent
                                            )
                                        )
                                    )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(DarkGraphite)
                                    ) {
                                        Icon(
                                            imageVector = item.filledIcon,
                                            contentDescription = item.label,
                                            tint = item.activeIconTint,
                                            modifier = Modifier
                                                .size(15.dp)
                                                .graphicsLayer {
                                                    scaleX = iconScale
                                                    scaleY = iconScale
                                                }
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.label,
                                        color = item.activeLabelColor,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        } else {
                            // Recessed Frosted Glass Icon Pod + Crisp Micro-Label for Inactive Tabs
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.05f))
                                ) {
                                    Icon(
                                        imageVector = item.outlinedIcon,
                                        contentDescription = item.label,
                                        tint = Champagne.copy(alpha = 0.75f),
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.label,
                                    color = Champagne.copy(alpha = 0.72f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
