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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.EventNote
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import com.example.ui.theme.CinemaMarqueeGold
import com.example.ui.theme.CinemaRuby
import com.example.ui.theme.DarkGraphite
import com.example.ui.theme.LiveCoralAccent
import com.example.ui.theme.LiveCrimson
import com.example.ui.theme.LiveRed
import com.example.ui.theme.MetalCharcoal
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PaleGold
import com.example.ui.theme.SeriesEmerald
import com.example.ui.theme.SeriesJadeAccent
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
        Scaffold(
            containerColor = DarkGraphite,
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
                when (currentScreen) {
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
                    icon = Icons.Default.EventNote,
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
    val sectionAccent = when (currentScreen) {
        AppScreen.LIVE_TV -> LiveCrimson
        AppScreen.MOVIES -> CinemaMarqueeGold
        AppScreen.SERIES -> SeriesJadeAccent
        else -> AmberGold
    }
    val sectionBadgeLabel = when (currentScreen) {
        AppScreen.LIVE_TV -> "LIVE STUDIO"
        AppScreen.MOVIES -> "CINEMA 4K"
        AppScreen.SERIES -> "SERIES VAULT"
        AppScreen.FAVORITES -> "VIP FAVORITES"
        else -> "ROYAL IPTV"
    }

    Surface(
        color = MetalCharcoal,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Navigation Drawer Icon
            IconButton(
                onClick = onDrawerMenuClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("top_drawer_menu_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Navigation Drawer Menu",
                    tint = PaleGold,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Creative Two-Tone SetPlay Wordmark with Play-Prism 'P' & Section Accent Underline
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onCastOrAccountsClick() }
                    .testTag("setplay_creative_wordmark")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Set",
                        color = PaleGold,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 0.8.sp
                    )
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = AmberGold,
                        modifier = Modifier
                            .padding(horizontal = 1.dp)
                            .size(18.dp)
                    )
                    Text(
                        text = "Play",
                        color = AmberGold,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = sectionAccent.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.8.dp, sectionAccent.copy(alpha = 0.85f))
                    ) {
                        Text(
                            text = sectionBadgeLabel,
                            color = PaleGold,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Tapered Section-Aware Gold Underline Accent
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp, bottom = 2.dp)
                        .width(104.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(AmberGold, sectionAccent, Color.Transparent)
                            )
                        )
                )

                Text(
                    text = activePlaylist?.name ?: "Tap to connect Xtream account",
                    color = Champagne,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right: Cast / Server Icon and Search Icon
            IconButton(
                onClick = onCastOrAccountsClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("top_playlists_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Cast,
                    contentDescription = "Cast & Xtream Servers",
                    tint = AmberGold,
                    modifier = Modifier.size(21.dp)
                )
            }

            IconButton(
                onClick = onSearchClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("top_search_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = PaleGold,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun BottomStreamingNav(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit
) {
    NavigationBar(
        containerColor = MetalCharcoal,
        contentColor = PaleGold,
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("bottom_nav")
    ) {
        // Exact order requested: Home, Live TV, Movies, Series, and Favorites
        val items = listOf(
            Triple(AppScreen.HOME, "Home", Icons.Default.Home),
            Triple(AppScreen.LIVE_TV, "Live TV", Icons.Default.LiveTv),
            Triple(AppScreen.MOVIES, "Movies", Icons.Default.Movie),
            Triple(AppScreen.SERIES, "Series", Icons.Default.VideoLibrary),
            Triple(AppScreen.FAVORITES, "Favorites", Icons.Default.Star)
        )

        items.forEach { (screen, label, icon) ->
            val isSelected = currentScreen == screen
            val (indicatorColor, selectedIconColor, selectedLabelColor) = when (screen) {
                AppScreen.LIVE_TV -> Triple(LiveCrimson, PaleGold, LiveCoralAccent)
                AppScreen.MOVIES -> Triple(CinemaMarqueeGold, DarkGraphite, CinemaMarqueeGold)
                AppScreen.SERIES -> Triple(SeriesEmerald, PaleGold, SeriesJadeAccent)
                else -> Triple(AmberGold, DarkGraphite, PaleGold)
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onScreenSelected(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedIconColor,
                    selectedTextColor = selectedLabelColor,
                    indicatorColor = indicatorColor,
                    unselectedIconColor = Champagne,
                    unselectedTextColor = Champagne
                ),
                modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
            )
        }
    }
}
