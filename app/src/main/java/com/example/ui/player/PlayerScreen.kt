package com.example.ui.player

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ClosedCaptionDisabled
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.data.model.PlaylistEntity
import com.example.data.model.StreamEntity
import com.example.data.remote.WyzieSubtitleOption
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CatchupGreen
import com.example.ui.theme.Champagne
import com.example.ui.theme.DarkGraphite
import com.example.ui.theme.LiveRed
import com.example.ui.theme.MetalCharcoal
import com.example.ui.theme.MetalCharcoalElevated
import com.example.ui.theme.MoviesCyanTeal
import com.example.ui.theme.MoviesMintSpring
import com.example.ui.theme.PaleGold
import com.example.ui.theme.SeriesIceAqua
import com.example.ui.theme.SeriesSeaJade
import com.example.ui.theme.TvBroadcastScarlet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(UnstableApi::class)
@ExperimentalMaterial3Api
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    playlist: PlaylistEntity,
    onBack: () -> Unit,
    onEnterInAppPip: () -> Unit = onBack,
    onToggleFavorite: (StreamEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val view = LocalView.current
    val activity = context as? Activity

    // True Full-Screen Immersive System Bars Management (Netflix / VLC / MX Player Pro standard):
    // Hides both statusBars() and navigationBars() (phone 3-button / gesture bar) with
    // BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE and extends video into short-edge cutout areas
    // so the phone's navigation buttons never eat part of the screen while watching.
    DisposableEffect(activity, view) {
        val window = activity?.window
        val insetsController = if (window != null) {
            WindowCompat.getInsetsController(window, view)
        } else {
            null
        }
        val previousKeepScreenOn = view.keepScreenOn
        val previousCutoutMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && window != null) {
            window.attributes.layoutInDisplayCutoutMode
        } else {
            0
        }

        view.keepScreenOn = true
        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val attrs = window.attributes
                attrs.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                window.attributes = attrs
            }
        }
        insetsController?.let { controller ->
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }

        onDispose {
            view.keepScreenOn = previousKeepScreenOn
            if (window != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val attrs = window.attributes
                attrs.layoutInDisplayCutoutMode = previousCutoutMode
                window.attributes = attrs
            }
            insetsController?.show(WindowInsetsCompat.Type.systemBars())
            viewModel.resetScreenOrientationToPortrait(activity)
        }
    }

    // Re-enforce system navigation & status bar hiding whenever controls toggle or orientation changes
    LaunchedEffect(
        uiState.isControlsVisible,
        uiState.isTouchLocked,
        uiState.isLandscapeLocked,
        uiState.isPlaying
    ) {
        val window = activity?.window ?: return@LaunchedEffect
        val controller = WindowCompat.getInsetsController(window, view)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }

    var showAudioSheet by remember { mutableStateOf(false) }
    var showSubtitleSheet by remember { mutableStateOf(false) }
    var showSpeedSheet by remember { mutableStateOf(false) }
    var showCatchupSheet by remember { mutableStateOf(false) }
    var showBufferSheet by remember { mutableStateOf(false) }
    var showSleepTimerSheet by remember { mutableStateOf(false) }

    // Dynamic Luxury Accent Palette tailored to stream type (Live TV vs Movies vs Series)
    val streamTypeLower = uiState.currentStream?.streamType?.lowercase(Locale.ROOT) ?: "live"
    val primaryNeonAccent = when (streamTypeLower) {
        "movie" -> MoviesMintSpring
        "series" -> SeriesIceAqua
        else -> AmberGold
    }
    val secondaryChamberAccent = when (streamTypeLower) {
        "movie" -> MoviesCyanTeal
        "series" -> SeriesSeaJade
        else -> TvBroadcastScarlet
    }

    // Continuous luxury chronograph animation for the center Play/Pause Sovereign Orb
    val infiniteTransition = rememberInfiniteTransition(label = "player_luxury_chronograph")
    val orbitalAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "player_orb_angle"
    )
    val livePulse by infiniteTransition.animateFloat(
        initialValue = 0.78f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "player_live_pulse"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF03060A))
            .testTag("player_screen")
    ) {
        val screenWidth = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val screenHeight = constraints.maxHeight.toFloat().coerceAtLeast(1f)

        // 1. Edge-to-Edge Media3 Video Surface with Pinch-to-Zoom & Pan
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = viewModel.player
                    useController = false
                    resizeMode = when (uiState.aspectRatioMode) {
                        AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                        AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                        AspectRatioMode.RATIO_16_9 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                        AspectRatioMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    }
                }
            },
            update = { playerView ->
                playerView.player = viewModel.player
                playerView.resizeMode = when (uiState.aspectRatioMode) {
                    AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    AspectRatioMode.RATIO_16_9 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                    AspectRatioMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = uiState.videoZoomScale,
                    scaleY = uiState.videoZoomScale,
                    translationX = uiState.videoPanOffsetX,
                    translationY = uiState.videoPanOffsetY
                )
        )

        // 2. Gesture Detector Layer (Tap, Double-Tap, Vertical Brightness/Volume Swipe, Horizontal VOD Scrub, Pinch-to-Zoom)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(uiState.isTouchLocked, uiState.isLive) {
                    detectTapGestures(
                        onTap = {
                            if (uiState.isChannelZapperOpen) {
                                viewModel.toggleChannelZapper(false)
                            } else {
                                viewModel.toggleControls()
                            }
                        },
                        onDoubleTap = { offset ->
                            if (!uiState.isTouchLocked && !uiState.isLive) {
                                if (offset.x < screenWidth / 2f) {
                                    viewModel.seekRelative(-10)
                                } else {
                                    viewModel.seekRelative(10)
                                }
                            } else if (!uiState.isTouchLocked && uiState.isLive) {
                                viewModel.toggleAspectRatio()
                            }
                        }
                    )
                }
                .pointerInput(uiState.isTouchLocked) {
                    if (uiState.isTouchLocked) return@pointerInput
                    detectTransformGestures { _, pan, zoom, _ ->
                        if (abs(zoom - 1.0f) > 0.01f || uiState.videoZoomScale > 1.02f) {
                            viewModel.onTransformGesture(zoom, pan.x, pan.y)
                        }
                    }
                }
                .pointerInput(uiState.isTouchLocked, uiState.isLive) {
                    if (uiState.isTouchLocked) return@pointerInput
                    var totalDragY = 0f
                    var totalDragX = 0f
                    var isHorizontal = false
                    detectDragGestures(
                        onDragStart = {
                            totalDragY = 0f
                            totalDragX = 0f
                            isHorizontal = false
                        },
                        onDragEnd = {
                            if (isHorizontal) {
                                viewModel.onScrubComplete()
                            }
                        },
                        onDragCancel = {
                            if (isHorizontal) {
                                viewModel.onScrubComplete()
                            }
                        },
                        onDrag = { change, dragAmount ->
                            if (uiState.videoZoomScale > 1.05f) return@detectDragGestures
                            change.consume()
                            totalDragX += dragAmount.x
                            totalDragY += dragAmount.y

                            if (!uiState.isLive && abs(totalDragX) > abs(totalDragY) && abs(totalDragX) > 24f) {
                                isHorizontal = true
                                val scrubDeltaSec = (dragAmount.x / 10f).toLong()
                                viewModel.onScrubSwipe(scrubDeltaSec)
                            } else if (!isHorizontal && abs(totalDragY) > 6f) {
                                val positionX = change.position.x
                                val normalizedDelta = -dragAmount.y / screenHeight
                                if (positionX < screenWidth * 0.45f) {
                                    viewModel.onBrightnessSwipe(normalizedDelta, activity)
                                } else if (positionX > screenWidth * 0.55f) {
                                    viewModel.onVolumeSwipeRatio(normalizedDelta)
                                }
                            }
                        }
                    )
                }
        )

        // 3. Cinema-Grade Wyzie Online Subtitle Overlay (Movies & Series)
        val subtitleBottomPad by animateDpAsState(
            targetValue = if (uiState.isControlsVisible && !uiState.isTouchLocked) 118.dp else 28.dp,
            animationSpec = tween(220),
            label = "subtitle_bottom_offset"
        )
        if (!uiState.isLive && !uiState.currentSubtitleText.isNullOrBlank()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 32.dp, end = 32.dp, bottom = subtitleBottomPad)
                    .testTag("player_active_subtitle_overlay")
            ) {
                Surface(
                    color = Color(0xD9050A12),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.8.dp, primaryNeonAccent.copy(alpha = 0.32f))
                ) {
                    Text(
                        text = uiState.currentSubtitleText!!,
                        color = Color(0xFFFDFBF7),
                        fontSize = (17f * uiState.subtitleScaleFactor).sp,
                        lineHeight = (23f * uiState.subtitleScaleFactor).sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(0f, 2f),
                                blurRadius = 4f
                            )
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                    )
                }
            }
        }

        // 4. Gesture Feedback Overlays (Brightness, Volume + 200% Audio Boost, Scrub)
        if (uiState.gestureBrightness != null) {
            GestureBadge(
                icon = { Icon(Icons.Default.BrightnessMedium, contentDescription = null, tint = AmberGold) },
                label = "Display Brightness",
                value = "${uiState.brightnessPercent}%",
                progress = uiState.brightnessPercent / 100f,
                isBoosted = false,
                accentColor = primaryNeonAccent,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (uiState.gestureVolume != null) {
            val volPct = uiState.gestureVolume!!
            val isBoost = volPct > 100
            GestureBadge(
                icon = {
                    Icon(
                        if (volPct == 0) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = if (isBoost) LiveRed else AmberGold
                    )
                },
                label = if (isBoost) "VLC Audio Boost" else "Master Volume",
                value = "$volPct%",
                progress = (volPct / 200f).coerceIn(0f, 1f),
                isBoosted = isBoost,
                accentColor = if (isBoost) LiveRed else primaryNeonAccent,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (uiState.gestureSeekSeconds != null) {
            val sec = uiState.gestureSeekSeconds!!
            val prefix = if (sec >= 0) "+$sec" else "$sec"
            GestureBadge(
                icon = {
                    Icon(
                        if (sec >= 0) Icons.Default.FastForward else Icons.Default.FastRewind,
                        contentDescription = null,
                        tint = AmberGold
                    )
                },
                label = "Timeline Scrub",
                value = "$prefix s",
                progress = 1f,
                isBoosted = false,
                accentColor = primaryNeonAccent,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Pinch-to-Zoom Reset Pill when zoomed in
        if (uiState.videoZoomScale > 1.02f) {
            Surface(
                color = Color(0xE608121E),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, AmberGold),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 74.dp)
                    .clickable { viewModel.resetZoomAndPan() }
                    .testTag("player_reset_zoom_pill")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.ZoomOutMap, contentDescription = null, tint = AmberGold, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Zoom ${String.format(Locale.US, "%.1fx", uiState.videoZoomScale)} • Tap to Reset",
                        color = PaleGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // 5. Luxury Buffering Halo Indicator
        if (uiState.isBuffering) {
            Box(
                modifier = Modifier
                    .size(78.dp)
                    .background(Color(0xD9060E18), CircleShape)
                    .border(
                        width = 1.2.dp,
                        brush = Brush.linearGradient(listOf(AmberGold, primaryNeonAccent)),
                        shape = CircleShape
                    )
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = AmberGold,
                    trackColor = primaryNeonAccent.copy(alpha = 0.2f),
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        // 6. Error Banner with Instant Retry
        if (uiState.error != null) {
            Surface(
                color = Color(0xF01C090D),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, LiveRed.copy(alpha = 0.8f)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 76.dp, start = 20.dp, end = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = uiState.error!!,
                        color = PaleGold,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { viewModel.retryCurrentStream() },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = DarkGraphite),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("player_retry_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = DarkGraphite, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Retry", color = DarkGraphite, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        // 7. Touch Lock Mode Capsule (MX Player Pro style)
        if (uiState.isTouchLocked && uiState.isControlsVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .displayCutoutPadding()
                    .padding(20.dp)
            ) {
                Surface(
                    color = Color(0xEB08121C),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.2.dp, AmberGold),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .clickable { viewModel.toggleTouchLock() }
                        .testTag("player_unlock_touch_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Unlock Screen", tint = AmberGold, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = "Touch Locked • Tap to Unlock",
                            color = PaleGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // 8. Sovereign Cinema & Broadcast Cockpit HUD Overlay (when unlocked)
        AnimatedVisibility(
            visible = uiState.isControlsVisible && !uiState.isTouchLocked,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(180)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xEB04080F),
                                Color(0x4004080F),
                                Color.Transparent,
                                Color(0x4D04080F),
                                Color(0xF204080F)
                            )
                        )
                    )
                    .displayCutoutPadding()
            ) {
                // =========================================================================
                // A. TOP SOVEREIGN BROADCAST & CINEMA HEADER CONSOLE
                // =========================================================================
                Surface(
                    color = Color(0xD907101A),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                AmberGold.copy(alpha = 0.55f),
                                primaryNeonAccent.copy(alpha = 0.40f),
                                Champagne.copy(alpha = 0.22f)
                            )
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF0A1624),
                                        secondaryChamberAccent.copy(alpha = 0.16f),
                                        Color(0xFF08111C)
                                    )
                                )
                            )
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Glass Back Medallion
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(38.dp)
                                .background(Color(0xFF101D2C), CircleShape)
                                .border(0.8.dp, Champagne.copy(alpha = 0.30f), CircleShape)
                                .testTag("player_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = PaleGold,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Channel Crest / VOD Poster Medallion
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            primaryNeonAccent.copy(alpha = 0.22f),
                                            Color(0xFF0B1520)
                                        )
                                    )
                                )
                                .border(
                                    width = 0.9.dp,
                                    color = primaryNeonAccent.copy(alpha = 0.45f),
                                    shape = RoundedCornerShape(11.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            val iconUrl = uiState.currentStream?.streamIcon.orEmpty()
                            if (iconUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = iconUrl,
                                    contentDescription = uiState.currentStream?.name,
                                    contentScale = if (uiState.isLive) ContentScale.Fit else ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(if (uiState.isLive) 4.dp else 0.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = when (streamTypeLower) {
                                        "movie" -> Icons.Default.Movie
                                        "series" -> Icons.Default.VideoLibrary
                                        else -> Icons.Default.Tv
                                    },
                                    contentDescription = null,
                                    tint = AmberGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Stream Title & Telemetry Ribbon
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 6.dp)
                        ) {
                            Text(
                                text = uiState.currentStream?.name ?: "SetPlay Stream",
                                color = Color.White,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.2.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                // Section Identity Micro-Badge
                                Surface(
                                    color = secondaryChamberAccent.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(5.dp),
                                    border = BorderStroke(0.6.dp, primaryNeonAccent.copy(alpha = 0.65f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .background(
                                                    if (uiState.isLive) LiveRed else primaryNeonAccent,
                                                    CircleShape
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(3.5.dp))
                                        Text(
                                            text = when (streamTypeLower) {
                                                "movie" -> "CINEMA"
                                                "series" -> "SERIES"
                                                else -> "ON AIR"
                                            },
                                            color = PaleGold,
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.4.sp
                                        )
                                    }
                                }

                                if (uiState.currentProgram != null) {
                                    Text(
                                        text = "Now: ${uiState.currentProgram?.title}",
                                        color = Champagne,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                } else if (!uiState.isLive && uiState.activeWyzieOption != null) {
                                    Text(
                                        text = "${uiState.activeWyzieOption!!.flagEmoji} ${uiState.activeWyzieOption!!.languageDisplay}",
                                        color = primaryNeonAccent,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (uiState.resolutionLabel.isNotEmpty()) {
                                    TechBadge(
                                        text = uiState.resolutionLabel,
                                        isHighlight = uiState.is4K,
                                        accentColor = primaryNeonAccent
                                    )
                                }
                                if (uiState.videoCodecLabel.isNotEmpty()) {
                                    TechBadge(
                                        text = uiState.videoCodecLabel,
                                        accentColor = primaryNeonAccent
                                    )
                                }
                                if (uiState.audioCodecLabel.isNotEmpty()) {
                                    TechBadge(
                                        text = uiState.audioCodecLabel,
                                        accentColor = primaryNeonAccent
                                    )
                                }
                                if (uiState.fpsLabel.isNotEmpty()) {
                                    TechBadge(
                                        text = uiState.fpsLabel,
                                        accentColor = primaryNeonAccent
                                    )
                                }
                            }
                        }

                        // Right-Side Luxury Action Cluster
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            // Screen Rotation Toggle
                            LuxuryHeaderIconWell(
                                icon = Icons.Default.ScreenRotation,
                                contentDescription = "Rotate Landscape / Portrait",
                                tint = if (uiState.isLandscapeLocked) AmberGold else PaleGold,
                                isActive = uiState.isLandscapeLocked,
                                onClick = { viewModel.toggleScreenOrientation(activity) },
                                modifier = Modifier.testTag("player_rotate_top_button")
                            )

                            // Last Channel Recall Capsule
                            if (uiState.previousStream != null) {
                                Surface(
                                    color = Color.Transparent,
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, PaleGold.copy(alpha = 0.7f)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(AmberGold, Color(0xFFF5B942))
                                            )
                                        )
                                        .clickable { viewModel.recallPreviousChannel() }
                                        .testTag("player_recall_channel_button")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Replay,
                                            contentDescription = "Previous Channel",
                                            tint = DarkGraphite,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Recall",
                                            color = DarkGraphite,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }

                            // Live Channel Zapper Drawer Toggle
                            if (uiState.isLive && uiState.siblingChannels.isNotEmpty()) {
                                LuxuryHeaderIconWell(
                                    icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                                    contentDescription = "Channel List Drawer",
                                    tint = AmberGold,
                                    isActive = uiState.isChannelZapperOpen,
                                    onClick = { viewModel.toggleChannelZapper() },
                                    modifier = Modifier.testTag("player_zapper_drawer_button")
                                )
                            }

                            // Catchup Archive Button
                            if (uiState.currentStream?.tvArchive == 1 || uiState.catchupPrograms.isNotEmpty()) {
                                LuxuryHeaderIconWell(
                                    icon = Icons.Default.History,
                                    contentDescription = "Catchup Archive",
                                    tint = CatchupGreen,
                                    isActive = false,
                                    onClick = { showCatchupSheet = true },
                                    modifier = Modifier.testTag("player_catchup_button")
                                )
                            }

                            // Favorite Star Button
                            val isFav = uiState.currentStream?.isFavorite == true
                            LuxuryHeaderIconWell(
                                icon = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Favorite",
                                tint = if (isFav) AmberGold else PaleGold,
                                isActive = isFav,
                                onClick = { uiState.currentStream?.let { onToggleFavorite(it) } },
                                modifier = Modifier.testTag("player_favorite_button")
                            )

                            // In-App Picture-in-Picture (PiP) Button
                            LuxuryHeaderIconWell(
                                icon = Icons.Default.PictureInPictureAlt,
                                contentDescription = "Picture in Picture",
                                tint = primaryNeonAccent,
                                isActive = false,
                                onClick = { onEnterInAppPip() },
                                modifier = Modifier.testTag("player_pip_button")
                            )
                        }
                    }
                }

                // =========================================================================
                // B. LEFT & RIGHT EDGE-LIT VERTICAL TELEMETRY TOWERS (BRIGHTNESS & VOLUME)
                // =========================================================================
                VerticalSideControlBar(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.BrightnessMedium,
                            contentDescription = "Brightness",
                            tint = AmberGold,
                            modifier = Modifier.size(15.dp)
                        )
                    },
                    valueLabel = "${uiState.brightnessPercent}%",
                    progress = (uiState.brightnessPercent / 100f).coerceIn(0f, 1f),
                    isBoosted = false,
                    accentColor = primaryNeonAccent,
                    onStepUp = {
                        viewModel.setBrightnessPercent(uiState.brightnessPercent + 10, activity)
                        viewModel.showControls()
                    },
                    onStepDown = {
                        viewModel.setBrightnessPercent(uiState.brightnessPercent - 10, activity)
                        viewModel.showControls()
                    },
                    onVerticalDragRatio = { deltaRatio ->
                        viewModel.onBrightnessSwipe(deltaRatio, activity)
                        viewModel.showControls()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp)
                        .testTag("player_left_brightness_bar")
                )

                VerticalSideControlBar(
                    icon = {
                        Icon(
                            imageVector = if (uiState.volumePercent == 0) {
                                Icons.AutoMirrored.Filled.VolumeOff
                            } else {
                                Icons.AutoMirrored.Filled.VolumeUp
                            },
                            contentDescription = "Volume & Audio Boost",
                            tint = if (uiState.volumePercent > 100) LiveRed else AmberGold,
                            modifier = Modifier.size(15.dp)
                        )
                    },
                    valueLabel = "${uiState.volumePercent}%",
                    progress = (uiState.volumePercent / 200f).coerceIn(0f, 1f),
                    isBoosted = uiState.volumePercent > 100,
                    accentColor = primaryNeonAccent,
                    onStepUp = {
                        viewModel.setVolumePercent(uiState.volumePercent + 10)
                        viewModel.showControls()
                    },
                    onStepDown = {
                        viewModel.setVolumePercent(uiState.volumePercent - 10)
                        viewModel.showControls()
                    },
                    onVerticalDragRatio = { deltaRatio ->
                        viewModel.onVolumeSwipeRatio(deltaRatio)
                        viewModel.showControls()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                        .testTag("player_right_volume_bar")
                )

                // =========================================================================
                // C. CENTER CHRONOGRAPH ORBITAL TRANSPORT HUB
                // =========================================================================
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(22.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.isLive && uiState.siblingChannels.size > 1) {
                        LuxuryTransportSatelliteButton(
                            icon = Icons.Default.KeyboardArrowUp,
                            microLabel = "CH -",
                            contentDescription = "Previous Channel",
                            accentColor = primaryNeonAccent,
                            onClick = { viewModel.zapChannelOffset(-1) },
                            modifier = Modifier.testTag("player_prev_channel")
                        )
                    }

                    if (!uiState.isLive) {
                        LuxuryTransportSatelliteButton(
                            icon = Icons.Default.FastRewind,
                            microLabel = "-10s",
                            contentDescription = "Rewind 10s",
                            accentColor = primaryNeonAccent,
                            onClick = { viewModel.seekRelative(-10) },
                            modifier = Modifier.testTag("player_rewind_10")
                        )
                    }

                    // Primary 3D Sovereign Play/Pause Chronograph Medallion
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .clickable { viewModel.togglePlayPause() }
                            .testTag("player_play_pause")
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val maxR = size.minDimension / 2f

                            // 1. Breathing Outer Ambient Halo
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        AmberGold.copy(alpha = 0.42f * livePulse),
                                        primaryNeonAccent.copy(alpha = 0.18f * livePulse),
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = maxR
                                ),
                                radius = maxR,
                                center = center
                            )

                            // 2. Rotating Chronograph Sweep-Gradient Outer Bezel
                            val bezelR = maxR * 0.88f
                            rotate(degrees = orbitalAngle, pivot = center) {
                                drawCircle(
                                    brush = Brush.sweepGradient(
                                        colors = listOf(
                                            AmberGold,
                                            PaleGold,
                                            primaryNeonAccent,
                                            Color.White.copy(alpha = 0.9f),
                                            AmberGold.copy(alpha = 0.35f),
                                            AmberGold
                                        ),
                                        center = center
                                    ),
                                    radius = bezelR,
                                    center = center,
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }

                            // 3. Royal Gold Sculpted Medallion Disc
                            val coreR = maxR * 0.78f
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        PaleGold,
                                        AmberGold,
                                        Color(0xFFC98B1D)
                                    ),
                                    center = Offset(center.x - coreR * 0.25f, center.y - coreR * 0.25f),
                                    radius = coreR * 1.35f
                                ),
                                radius = coreR,
                                center = center
                            )

                            // 4. Top-Left Crystal Specular Arc
                            drawArc(
                                color = Color.White.copy(alpha = 0.65f),
                                startAngle = 195f,
                                sweepAngle = 110f,
                                useCenter = false,
                                topLeft = Offset(center.x - coreR * 0.84f, center.y - coreR * 0.84f),
                                size = androidx.compose.ui.geometry.Size(coreR * 1.68f, coreR * 1.68f),
                                style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        Icon(
                            imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                            tint = DarkGraphite,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    if (!uiState.isLive) {
                        LuxuryTransportSatelliteButton(
                            icon = Icons.Default.FastForward,
                            microLabel = "+10s",
                            contentDescription = "Forward 10s",
                            accentColor = primaryNeonAccent,
                            onClick = { viewModel.seekRelative(10) },
                            modifier = Modifier.testTag("player_forward_10")
                        )
                    }

                    if (uiState.isLive && uiState.siblingChannels.size > 1) {
                        LuxuryTransportSatelliteButton(
                            icon = Icons.Default.KeyboardArrowDown,
                            microLabel = "CH +",
                            contentDescription = "Next Channel",
                            accentColor = primaryNeonAccent,
                            onClick = { viewModel.zapChannelOffset(1) },
                            modifier = Modifier.testTag("player_next_channel")
                        )
                    }
                }

                // =========================================================================
                // D. BOTTOM FLOATING GLASS COCKPIT DECK
                // =========================================================================
                Surface(
                    color = Color(0xDF07101A),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                primaryNeonAccent.copy(alpha = 0.45f),
                                AmberGold.copy(alpha = 0.55f),
                                primaryNeonAccent.copy(alpha = 0.45f)
                            )
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF0C1826).copy(alpha = 0.92f),
                                        Color(0xFF060C14).copy(alpha = 0.98f)
                                    )
                                )
                            )
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                    ) {
                        // Upper Tier: VOD Scrubber Timeline OR Live Broadcast Telemetry Ribbon
                        if (!uiState.isLive) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatTime(uiState.currentPositionMs),
                                    color = PaleGold,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Slider(
                                    value = if (uiState.durationMs > 0) uiState.currentPositionMs.toFloat() else 0f,
                                    onValueChange = { pos -> viewModel.seekToPosition(pos.toLong()) },
                                    valueRange = 0f..(uiState.durationMs.toFloat().coerceAtLeast(1f)),
                                    colors = SliderDefaults.colors(
                                        thumbColor = AmberGold,
                                        activeTrackColor = primaryNeonAccent,
                                        inactiveTrackColor = MetalCharcoalElevated
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(28.dp)
                                        .padding(horizontal = 8.dp)
                                        .testTag("player_timeline_slider")
                                )
                                Text(
                                    text = formatTime(uiState.durationMs),
                                    color = Champagne,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            // Live TV Broadcast Status + EPG Progress Ribbon
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Glowing Live Broadcast Pill
                                Surface(
                                    color = LiveRed.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(0.8.dp, LiveRed.copy(alpha = 0.75f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.5.dp)
                                                .graphicsLayer {
                                                    scaleX = livePulse
                                                    scaleY = livePulse
                                                }
                                                .background(LiveRed, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "LIVE BROADCAST",
                                            color = Color.White,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                if (uiState.upcomingPrograms.size > 1) {
                                    Text(
                                        text = "Next: ${uiState.upcomingPrograms[1].title}",
                                        color = Champagne,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                } else if (uiState.currentProgram != null) {
                                    Text(
                                        text = uiState.currentProgram!!.title,
                                        color = Champagne,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Text(
                                        text = "Direct Ultra-Low Latency Stream",
                                        color = Champagne.copy(alpha = 0.72f),
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                if (uiState.sleepTimerRemainingMinutes != null) {
                                    Surface(
                                        color = AmberGold.copy(alpha = 0.18f),
                                        shape = RoundedCornerShape(50),
                                        border = BorderStroke(0.7.dp, AmberGold.copy(alpha = 0.6f)),
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text(
                                            text = "Sleep ${uiState.sleepTimerRemainingMinutes}m",
                                            color = AmberGold,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Surface(
                                    color = Color(0xFF101D2C),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(0.7.dp, Champagne.copy(alpha = 0.25f))
                                ) {
                                    Text(
                                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                                        color = PaleGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                                    )
                                }
                            }
                        }

                        // Lower Tier: Unified Luxury Smoked-Glass Control Pills
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Touch Lock Pill
                            LuxuryConsoleActionPill(
                                icon = Icons.Default.LockOpen,
                                label = "Lock",
                                isHighlighted = false,
                                accentColor = AmberGold,
                                onClick = { viewModel.toggleTouchLock() },
                                modifier = Modifier.testTag("player_touch_lock_button")
                            )

                            // 2. Audio Tracks & Boost Pill
                            LuxuryConsoleActionPill(
                                icon = Icons.AutoMirrored.Filled.VolumeUp,
                                label = if (uiState.volumePercent > 100) "Audio ${uiState.volumePercent}%" else "Audio",
                                isHighlighted = uiState.volumePercent > 100,
                                accentColor = AmberGold,
                                onClick = { showAudioSheet = true },
                                modifier = Modifier.testTag("player_audio_tracks")
                            )

                            // 3. Online Subtitles Studio Pill (Movies & Series ONLY)
                            if (!uiState.isLive) {
                                val hasActiveSub = uiState.activeWyzieOption != null ||
                                    uiState.subtitleTracks.any { it.isSelected }
                                val subLabel = when {
                                    uiState.activeWyzieOption != null -> {
                                        "${uiState.activeWyzieOption!!.flagEmoji} ${uiState.activeWyzieOption!!.languageCode.uppercase(Locale.ROOT)}"
                                    }
                                    uiState.subtitleTracks.any { it.isSelected } -> "CC ON"
                                    else -> "Subtitles"
                                }
                                LuxuryConsoleActionPill(
                                    icon = Icons.Default.ClosedCaption,
                                    label = subLabel,
                                    isHighlighted = hasActiveSub,
                                    accentColor = primaryNeonAccent,
                                    onClick = {
                                        showSubtitleSheet = true
                                        if (uiState.wyzieLanguageGroups.isEmpty() && !uiState.isSearchingOnlineSubtitles) {
                                            viewModel.searchOnlineSubtitles()
                                        }
                                    },
                                    modifier = Modifier.testTag("player_subtitle_tracks")
                                )
                            }

                            // 4. Sleep Timer Pill
                            LuxuryConsoleActionPill(
                                icon = Icons.Default.Bedtime,
                                label = if (uiState.sleepTimerRemainingMinutes != null) {
                                    "${uiState.sleepTimerRemainingMinutes}m"
                                } else {
                                    "Sleep"
                                },
                                isHighlighted = uiState.sleepTimerRemainingMinutes != null,
                                accentColor = AmberGold,
                                onClick = { showSleepTimerSheet = true },
                                modifier = Modifier.testTag("player_sleep_timer_button")
                            )

                            // 5. Screen Orientation Pill (Landscape / Rotate)
                            LuxuryConsoleActionPill(
                                icon = Icons.Default.ScreenRotation,
                                label = if (uiState.isLandscapeLocked) "Landscape" else "Rotate",
                                isHighlighted = uiState.isLandscapeLocked,
                                accentColor = AmberGold,
                                onClick = { viewModel.toggleScreenOrientation(activity) },
                                modifier = Modifier.testTag("player_rotate_button")
                            )

                            // 6. Buffer Profile Selector Pill
                            LuxuryConsoleActionPill(
                                icon = Icons.Default.Memory,
                                label = when (uiState.bufferProfile) {
                                    BufferProfile.FAST_ZAP -> "Zap"
                                    BufferProfile.BALANCED -> "Auto"
                                    BufferProfile.STABLE_4K -> "4K Buf"
                                },
                                isHighlighted = uiState.bufferProfile != BufferProfile.BALANCED,
                                accentColor = primaryNeonAccent,
                                onClick = { showBufferSheet = true },
                                modifier = Modifier.testTag("player_buffer_button")
                            )

                            // 7. Playback Speed Pill (Movies & Series ONLY)
                            if (!uiState.isLive) {
                                LuxuryConsoleActionPill(
                                    icon = Icons.Default.Speed,
                                    label = "${uiState.playbackSpeed}x",
                                    isHighlighted = uiState.playbackSpeed != 1.0f,
                                    accentColor = primaryNeonAccent,
                                    onClick = { showSpeedSheet = true },
                                    modifier = Modifier.testTag("player_speed_button")
                                )
                            }

                            // 8. Aspect Ratio Toggle Pill
                            LuxuryConsoleActionPill(
                                icon = Icons.Default.AspectRatio,
                                label = uiState.aspectRatioMode.name.replace("RATIO_", "").replace("_", ":"),
                                isHighlighted = uiState.aspectRatioMode != AspectRatioMode.FIT,
                                accentColor = AmberGold,
                                onClick = { viewModel.toggleAspectRatio() },
                                modifier = Modifier.testTag("player_aspect_ratio_button")
                            )
                        }
                    }
                }
            }
        }

        // 9. In-Player Quick Channel Zapper Drawer
        AnimatedVisibility(
            visible = uiState.isChannelZapperOpen,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Surface(
                color = Color(0xF5070E17),
                border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.35f)),
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    .testTag("in_player_channel_zapper")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .displayCutoutPadding()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Quick Channel Zapper",
                                color = PaleGold,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "${uiState.siblingChannels.size} channels in category",
                                color = Champagne,
                                fontSize = 11.sp
                            )
                        }
                        Surface(
                            color = Color(0xFF122030),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.8.dp, AmberGold.copy(alpha = 0.5f)),
                            modifier = Modifier.clickable { viewModel.toggleChannelZapper(false) }
                        ) {
                            Text(
                                text = "Close",
                                color = AmberGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.siblingChannels, key = { it.id }) { ch ->
                            val isCurrent = ch.id == uiState.currentStream?.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isCurrent) AmberGold else Color(0xFF0E1A28))
                                    .border(
                                        width = 1.dp,
                                        color = if (isCurrent) PaleGold else Champagne.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        viewModel.playStream(ch, playlist)
                                        viewModel.toggleChannelZapper(false)
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isCurrent) DarkGraphite else MetalCharcoalElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (ch.streamIcon.isNotEmpty()) {
                                        AsyncImage(
                                            model = ch.streamIcon,
                                            contentDescription = ch.name,
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(2.dp)
                                        )
                                    } else {
                                        Icon(Icons.Default.Tv, contentDescription = null, tint = AmberGold, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = ch.name,
                                    color = if (isCurrent) DarkGraphite else PaleGold,
                                    fontSize = 13.sp,
                                    fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isCurrent) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(DarkGraphite, CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // =================================================================================
    // MODAL SHEETS (AUDIO, WYZIE ONLINE SUBTITLE STUDIO, CATCHUP, BUFFER, SLEEP, SPEED)
    // =================================================================================

    // 1. Audio Tracks & VLC Audio Boost Modal Sheet
    if (showAudioSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAudioSheet = false },
            containerColor = Color(0xFF0A1420)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Audio Tracks & VLC Audio Boost",
                    color = PaleGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Volume & Audio Boost: ${uiState.volumePercent}% ${
                        if (uiState.volumePercent > 100) "(+${((uiState.volumePercent - 100) * 0.12f).roundToInt()} dB Boost)" else ""
                    }",
                    color = if (uiState.volumePercent > 100) AmberGold else Champagne,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Slider(
                    value = uiState.volumePercent.toFloat(),
                    onValueChange = { viewModel.setVolumePercent(it.roundToInt(), showHudBadge = false) },
                    valueRange = 0f..200f,
                    colors = SliderDefaults.colors(
                        thumbColor = AmberGold,
                        activeTrackColor = AmberGold,
                        inactiveTrackColor = DarkGraphite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                if (uiState.audioTracks.isEmpty()) {
                    Text("Default Stream Audio Track", color = Champagne, fontSize = 14.sp)
                } else {
                    LazyColumn {
                        items(uiState.audioTracks) { track ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectAudioTrack(track)
                                        showAudioSheet = false
                                    }
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = track.name,
                                    color = if (track.isSelected) AmberGold else PaleGold,
                                    fontSize = 14.sp,
                                    fontWeight = if (track.isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (track.isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = AmberGold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 2. Wyzie Online Subtitle Studio Modal Sheet (Movies & Series)
    if (showSubtitleSheet && !uiState.isLive) {
        WyzieSubtitleStudioSheet(
            uiState = uiState,
            accentColor = primaryNeonAccent,
            onDismiss = { showSubtitleSheet = false },
            onSearchCustom = { query, season, episode ->
                viewModel.searchOnlineSubtitles(query, season, episode)
            },
            onSelectLanguage = { langCode ->
                viewModel.selectOnlineSubtitleLanguage(langCode)
            },
            onSelectWyzieOption = { option ->
                viewModel.applyOnlineSubtitleOption(option) {
                    showSubtitleSheet = false
                }
            },
            onSelectEmbeddedTrack = { track ->
                viewModel.selectSubtitleTrack(track)
                showSubtitleSheet = false
            },
            onTurnSubtitlesOff = {
                viewModel.selectSubtitleTrack(null)
                viewModel.clearOnlineSubtitles()
                showSubtitleSheet = false
            },
            onAdjustSyncOffset = { deltaMs ->
                viewModel.adjustSubtitleSyncOffset(deltaMs)
            },
            onResetSyncOffset = {
                viewModel.resetSubtitleSyncOffset()
            },
            onSelectScaleFactor = { scale ->
                viewModel.setSubtitleScaleFactor(scale)
            }
        )
    }

    // 3. Catchup Archive Modal Sheet
    if (showCatchupSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCatchupSheet = false },
            containerColor = Color(0xFF0A1420)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = AmberGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Catchup Archive & Timeshift", color = PaleGold, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Jump back in time on ${uiState.currentStream?.name ?: "this channel"}",
                    color = Champagne,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        uiState.currentStream?.let { viewModel.playStream(it, playlist) }
                        showCatchupSheet = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = DarkGraphite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Watch Live Broadcast Now", color = DarkGraphite, fontWeight = FontWeight.ExtraBold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (uiState.catchupPrograms.isNotEmpty()) {
                    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.catchupPrograms) { prog ->
                            val durationMin = ((prog.endEpoch - prog.startEpoch) / 60).toInt().coerceAtLeast(15)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkGraphite)
                                    .clickable {
                                        uiState.currentStream?.let { stream ->
                                            viewModel.playStream(stream, playlist, prog.startEpoch, durationMin)
                                        }
                                        showCatchupSheet = false
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prog.title, color = PaleGold, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = "${sdf.format(Date(prog.startEpoch * 1000))} ($durationMin min)",
                                        color = Champagne,
                                        fontSize = 11.sp
                                    )
                                }
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play Archive", tint = AmberGold)
                            }
                        }
                    }
                } else {
                    Text("Quick Timeshift Rewind:", color = Champagne, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    val nowSec = System.currentTimeMillis() / 1000
                    listOf(1 to "1 Hour Ago", 2 to "2 Hours Ago", 3 to "3 Hours Ago", 6 to "6 Hours Ago").forEach { (hours, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    uiState.currentStream?.let { stream ->
                                        viewModel.playStream(
                                            stream = stream,
                                            playlist = playlist,
                                            catchupStartEpoch = nowSec - (hours * 3600L),
                                            catchupDurationMin = 60
                                        )
                                    }
                                    showCatchupSheet = false
                                }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(label, color = PaleGold, fontSize = 14.sp)
                            Icon(Icons.Default.History, contentDescription = null, tint = AmberGold)
                        }
                    }
                }
            }
        }
    }

    // 4. Buffer Profile Modal Sheet
    if (showBufferSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBufferSheet = false },
            containerColor = Color(0xFF0A1420)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Text("Stream Buffer & Latency Control", color = PaleGold, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Adjust buffer size to prioritize fast channel zapping or freeze-free 4K playback.", color = Champagne, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
                BufferProfile.entries.forEach { profile ->
                    val isSelected = uiState.bufferProfile == profile
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setBufferProfile(profile)
                                showBufferSheet = false
                            }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.label,
                                color = if (isSelected) AmberGold else PaleGold,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(text = profile.description, color = Champagne, fontSize = 11.sp)
                        }
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = AmberGold)
                        }
                    }
                }
            }
        }
    }

    // 5. Sleep Timer Modal Sheet
    if (showSleepTimerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSleepTimerSheet = false },
            containerColor = Color(0xFF0A1420)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Text("Sleep Timer", color = PaleGold, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(12.dp))
                val options = listOf(null to "Off", 15 to "15 Minutes", 30 to "30 Minutes", 60 to "60 Minutes", 90 to "90 Minutes")
                options.forEach { (mins, label) ->
                    val isSelected = uiState.sleepTimerRemainingMinutes == mins
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setSleepTimer(mins)
                                showSleepTimerSheet = false
                            }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) AmberGold else PaleGold,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = AmberGold)
                        }
                    }
                }
            }
        }
    }

    // 6. Playback Speed Modal Sheet (VOD only)
    if (showSpeedSheet && !uiState.isLive) {
        ModalBottomSheet(
            onDismissRequest = { showSpeedSheet = false },
            containerColor = Color(0xFF0A1420)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Text("Playback Speed", color = PaleGold, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(12.dp))
                val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                speeds.forEach { speed ->
                    val isSelected = uiState.playbackSpeed == speed
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setPlaybackSpeed(speed)
                                showSpeedSheet = false
                            }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${speed}x ${if (speed == 1.0f) "(Normal)" else ""}",
                            color = if (isSelected) AmberGold else PaleGold,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = AmberGold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * SetPlay Online Subtitle Studio Modal Sheet (Powered by Wyzie Subs API):
 * - Step 1: User selects their preferred Subtitle Language (Arabic, English, French, Spanish, etc.).
 * - Step 2: Displays the curated list of Wyzie subtitle options for that language so the user
 *   can tap any option to immediately start watching with it.
 * - Includes custom Title/IMDb search, Series Season/Episode refinement, Subtitle Sync Offset,
 *   and Subtitle Text Scale controls.
 */
@ExperimentalMaterial3Api
@Composable
private fun WyzieSubtitleStudioSheet(
    uiState: PlayerUiState,
    accentColor: Color,
    onDismiss: () -> Unit,
    onSearchCustom: (String, Int?, Int?) -> Unit,
    onSelectLanguage: (String) -> Unit,
    onSelectWyzieOption: (WyzieSubtitleOption) -> Unit,
    onSelectEmbeddedTrack: (TrackItem) -> Unit,
    onTurnSubtitlesOff: () -> Unit,
    onAdjustSyncOffset: (Long) -> Unit,
    onResetSyncOffset: () -> Unit,
    onSelectScaleFactor: (Float) -> Unit
) {
    val isSeries = uiState.currentStream?.streamType?.equals("series", ignoreCase = true) == true
    var searchInput by remember(uiState.resolvedMediaQuery?.cleanTitle) {
        mutableStateOf(uiState.resolvedMediaQuery?.cleanTitle ?: uiState.currentStream?.name.orEmpty())
    }
    var seasonInput by remember(uiState.resolvedMediaQuery?.season) {
        mutableStateOf((uiState.resolvedMediaQuery?.season ?: 1).toString())
    }
    var episodeInput by remember(uiState.resolvedMediaQuery?.episode) {
        mutableStateOf((uiState.resolvedMediaQuery?.episode ?: 1).toString())
    }

    val selectedGroup = remember(uiState.wyzieLanguageGroups, uiState.selectedWyzieLanguageCode) {
        uiState.wyzieLanguageGroups.find { it.languageCode == uiState.selectedWyzieLanguageCode }
            ?: uiState.wyzieLanguageGroups.firstOrNull()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF07111C)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .navigationBarsPadding()
        ) {
            // 1. Studio Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(accentColor.copy(alpha = 0.16f), CircleShape)
                            .border(1.dp, accentColor.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Subtitles,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Online Subtitle Studio",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(7.dp))
                            Surface(
                                color = AmberGold.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.7.dp, AmberGold.copy(alpha = 0.65f))
                            ) {
                                Text(
                                    text = "WYZIE SUBS",
                                    color = AmberGold,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        val matchedId = uiState.resolvedMediaQuery?.imdbId
                            ?: uiState.resolvedMediaQuery?.tmdbId?.let { "TMDB:$it" }
                        Text(
                            text = buildString {
                                append(uiState.resolvedMediaQuery?.cleanTitle ?: uiState.currentStream?.name ?: "VOD")
                                if (!matchedId.isNullOrBlank()) append(" • $matchedId")
                                if (uiState.wyzieLanguageGroups.isNotEmpty()) {
                                    append(" • ${uiState.wyzieLanguageGroups.size} Languages")
                                }
                            },
                            color = Champagne,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Turn Subtitles Off Pill
                val allOff = uiState.activeWyzieOption == null && uiState.subtitleTracks.none { it.isSelected }
                Surface(
                    color = if (allOff) AmberGold.copy(alpha = 0.2f) else Color(0xFF101E2E),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.8.dp, if (allOff) AmberGold else Champagne.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .clickable { onTurnSubtitlesOff() }
                        .testTag("subtitle_off_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ClosedCaptionDisabled,
                            contentDescription = "Subtitles Off",
                            tint = if (allOff) AmberGold else Champagne,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Off",
                            color = if (allOff) AmberGold else PaleGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Compact Title / IMDb ID Search & Season/Episode Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = searchInput,
                    onValueChange = { searchInput = it },
                    placeholder = {
                        Text("Movie/Series title or IMDb ID (tt...)", color = Champagne.copy(alpha = 0.5f), fontSize = 12.sp)
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = PaleGold,
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = Champagne.copy(alpha = 0.28f),
                        focusedContainerColor = Color(0xFF0C1826),
                        unfocusedContainerColor = Color(0xFF0C1826)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("wyzie_search_input")
                )

                if (isSeries) {
                    OutlinedTextField(
                        value = seasonInput,
                        onValueChange = { seasonInput = it.filter { ch -> ch.isDigit() }.take(2) },
                        label = { Text("S", fontSize = 10.sp, color = Champagne) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = PaleGold,
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Champagne.copy(alpha = 0.28f)
                        ),
                        modifier = Modifier.width(56.dp)
                    )
                    OutlinedTextField(
                        value = episodeInput,
                        onValueChange = { episodeInput = it.filter { ch -> ch.isDigit() }.take(3) },
                        label = { Text("E", fontSize = 10.sp, color = Champagne) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = PaleGold,
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Champagne.copy(alpha = 0.28f)
                        ),
                        modifier = Modifier.width(58.dp)
                    )
                }

                Button(
                    onClick = {
                        onSearchCustom(
                            searchInput,
                            seasonInput.toIntOrNull(),
                            episodeInput.toIntOrNull()
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = DarkGraphite
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("wyzie_search_button")
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search Subtitles", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Find", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            // 3. Active Subtitle Timing Sync & Font Size Controls (shown when an online subtitle is active)
            if (uiState.activeWyzieOption != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFF0D1B2A),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sync Offset Controls
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = "Sync: ${String.format(Locale.US, "%+.1fs", uiState.subtitleSyncOffsetMs / 1000f)}",
                                color = PaleGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            SmallStudioChip(label = "-0.5s", onClick = { onAdjustSyncOffset(-500L) })
                            SmallStudioChip(label = "+0.5s", onClick = { onAdjustSyncOffset(500L) })
                            if (uiState.subtitleSyncOffsetMs != 0L) {
                                SmallStudioChip(label = "0.0s", onClick = onResetSyncOffset)
                            }
                        }

                        // Subtitle Size Controls
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(0.85f to "S", 1.0f to "M", 1.2f to "L", 1.4f to "XL").forEach { (scale, label) ->
                                val selected = abs(uiState.subtitleScaleFactor - scale) < 0.05f
                                Surface(
                                    color = if (selected) accentColor else Color(0xFF142436),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.clickable { onSelectScaleFactor(scale) }
                                ) {
                                    Text(
                                        text = label,
                                        color = if (selected) DarkGraphite else Champagne,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Loading / Status / Step 1 (Language Selection) & Step 2 (Subtitle Options)
            if (uiState.isSearchingOnlineSubtitles || uiState.isDownloadingSubtitle) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = accentColor, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (uiState.isDownloadingSubtitle) {
                                "Downloading & syncing subtitle cues..."
                            } else {
                                "Searching Wyzie Subs for available languages..."
                            },
                            color = PaleGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else if (uiState.wyzieLanguageGroups.isNotEmpty()) {
                // STEP 1: CHOOSE SUBTITLE LANGUAGE
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = AmberGold,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "STEP 1 • CHOOSE LANGUAGE",
                        color = AmberGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.6.sp
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wyzie_language_row")
                ) {
                    items(uiState.wyzieLanguageGroups, key = { it.languageCode }) { group ->
                        val isLangSelected = selectedGroup?.languageCode == group.languageCode
                        val bg by animateColorAsState(
                            targetValue = if (isLangSelected) accentColor.copy(alpha = 0.22f) else Color(0xFF0E1B2A),
                            label = "lang_chip_bg_${group.languageCode}"
                        )
                        Surface(
                            color = bg,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                width = if (isLangSelected) 1.3.dp else 0.8.dp,
                                color = if (isLangSelected) accentColor else Champagne.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .clickable { onSelectLanguage(group.languageCode) }
                                .testTag("wyzie_lang_${group.languageCode}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(text = group.flagEmoji, fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = group.languageDisplay,
                                        color = if (isLangSelected) Color.White else PaleGold,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "${group.options.size} option${if (group.options.size == 1) "" else "s"}",
                                        color = if (isLangSelected) accentColor else Champagne.copy(alpha = 0.7f),
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // STEP 2: CHOOSE SUBTITLE OPTION FOR SELECTED LANGUAGE
                if (selectedGroup != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "STEP 2 • SELECT ${selectedGroup.languageDisplay.uppercase(Locale.ROOT)} SUBTITLE TO WATCH",
                            color = PaleGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "${selectedGroup.options.size} available",
                            color = Champagne,
                            fontSize = 10.5.sp
                        )
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 250.dp)
                            .testTag("wyzie_options_list")
                    ) {
                        itemsIndexed(
                            selectedGroup.options,
                            key = { idx, opt -> "${opt.id}_$idx" }
                        ) { idx, option ->
                            val isOptionActive = uiState.activeWyzieOption?.url == option.url
                            Surface(
                                color = if (isOptionActive) accentColor.copy(alpha = 0.18f) else Color(0xFF0C1826),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(
                                    width = if (isOptionActive) 1.2.dp else 0.8.dp,
                                    color = if (isOptionActive) accentColor else Champagne.copy(alpha = 0.22f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectWyzieOption(option) }
                                    .testTag("wyzie_option_${idx + 1}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "${option.flagEmoji} Option #${idx + 1} • ${option.languageDisplay}",
                                                color = if (isOptionActive) accentColor else Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Surface(
                                                color = Color(0xFF15263A),
                                                shape = RoundedCornerShape(5.dp),
                                                border = BorderStroke(0.5.dp, Champagne.copy(alpha = 0.35f))
                                            ) {
                                                Text(
                                                    text = option.format,
                                                    color = PaleGold,
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                                )
                                            }
                                            Surface(
                                                color = AmberGold.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(5.dp),
                                                border = BorderStroke(0.5.dp, AmberGold.copy(alpha = 0.5f))
                                            ) {
                                                Text(
                                                    text = option.source,
                                                    color = AmberGold,
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                                )
                                            }
                                            if (option.isHearingImpaired) {
                                                Surface(
                                                    color = Color(0xFF203246),
                                                    shape = RoundedCornerShape(5.dp)
                                                ) {
                                                    Text(
                                                        text = "HI",
                                                        color = Champagne,
                                                        fontSize = 8.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = option.mediaName,
                                            color = Champagne.copy(alpha = 0.82f),
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Surface(
                                        color = if (isOptionActive) accentColor else AmberGold,
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isOptionActive) Icons.Default.Check else Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = DarkGraphite,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = if (isOptionActive) "Active" else "Watch",
                                                color = DarkGraphite,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Status or empty message
                Surface(
                    color = Color(0xFF0C1826),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.8.dp, Champagne.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.onlineSubtitleStatusMessage
                            ?: "Enter the Movie/Series title or IMDb ID (e.g. tt15398776) above and tap Find to load Wyzie subtitles.",
                        color = Champagne,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // 5. Embedded Stream Subtitle Tracks (if container has built-in subtitle tracks)
            if (uiState.subtitleTracks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "EMBEDDED STREAM SUBTITLES",
                    color = Champagne,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.subtitleTracks) { track ->
                        val selected = track.isSelected && uiState.activeWyzieOption == null
                        Surface(
                            color = if (selected) AmberGold.copy(alpha = 0.22f) else Color(0xFF0E1B2A),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.8.dp, if (selected) AmberGold else Champagne.copy(alpha = 0.25f)),
                            modifier = Modifier.clickable { onSelectEmbeddedTrack(track) }
                        ) {
                            Text(
                                text = track.name,
                                color = if (selected) AmberGold else PaleGold,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SmallStudioChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        color = Color(0xFF15263A),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(0.6.dp, Champagne.copy(alpha = 0.35f)),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            color = PaleGold,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun LuxuryHeaderIconWell(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tint: Color,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isActive) AmberGold.copy(alpha = 0.20f) else Color(0xFF101D2C),
        shape = CircleShape,
        border = BorderStroke(
            width = 0.85.dp,
            color = if (isActive) AmberGold else Champagne.copy(alpha = 0.28f)
        ),
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun LuxuryTransportSatelliteButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    microLabel: String,
    contentDescription: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xD909131F),
        shape = CircleShape,
        border = BorderStroke(
            width = 1.1.dp,
            brush = Brush.verticalGradient(
                listOf(
                    accentColor.copy(alpha = 0.7f),
                    AmberGold.copy(alpha = 0.45f)
                )
            )
        ),
        modifier = modifier
            .size(54.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = PaleGold,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = microLabel,
                color = accentColor,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.3.sp
            )
        }
    }
}

@Composable
private fun LuxuryConsoleActionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isHighlighted: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isHighlighted) accentColor.copy(alpha = 0.22f) else Color(0xFF0E1B2A),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = if (isHighlighted) 1.1.dp else 0.8.dp,
            color = if (isHighlighted) accentColor else Champagne.copy(alpha = 0.28f)
        ),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isHighlighted) accentColor else AmberGold,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                color = if (isHighlighted) Color.White else PaleGold,
                fontSize = 11.sp,
                fontWeight = if (isHighlighted) FontWeight.ExtraBold else FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

/**
 * Edge-Lit Frosted-Glass Vertical Telemetry Tower for Brightness (left) and Volume + 200% Boost (right).
 */
@Composable
fun VerticalSideControlBar(
    icon: @Composable () -> Unit,
    valueLabel: String,
    progress: Float,
    isBoosted: Boolean,
    accentColor: Color = AmberGold,
    onStepUp: () -> Unit,
    onStepDown: () -> Unit,
    onVerticalDragRatio: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xD907101A),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.verticalGradient(
                colors = if (isBoosted) {
                    listOf(LiveRed, AmberGold)
                } else {
                    listOf(accentColor.copy(alpha = 0.65f), Champagne.copy(alpha = 0.25f))
                }
            )
        ),
        modifier = modifier
            .width(40.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val deltaRatio = -dragAmount.y / 210f
                    onVerticalDragRatio(deltaRatio)
                }
            }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Step Up (+) Button
            Box(
                modifier = Modifier
                    .size(25.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF122030))
                    .border(0.6.dp, Champagne.copy(alpha = 0.3f), CircleShape)
                    .clickable { onStepUp() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase", tint = PaleGold, modifier = Modifier.size(13.dp))
            }

            // Liquid Gradient Vertical Track
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(82.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF101C2B)),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(progress.coerceIn(0.05f, 1f))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = if (isBoosted) {
                                    listOf(LiveRed, AmberGold)
                                } else {
                                    listOf(AmberGold, accentColor)
                                }
                            ),
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }

            Text(
                text = valueLabel,
                color = if (isBoosted) LiveRed else PaleGold,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )

            icon()

            // Step Down (-) Button
            Box(
                modifier = Modifier
                    .size(25.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF122030))
                    .border(0.6.dp, Champagne.copy(alpha = 0.3f), CircleShape)
                    .clickable { onStepDown() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Champagne, modifier = Modifier.size(13.dp))
            }
        }
    }
}

@Composable
fun TechBadge(
    text: String,
    isHighlight: Boolean = false,
    accentColor: Color = AmberGold
) {
    Surface(
        color = if (isHighlight) AmberGold else Color(0xFF101E2E),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(
            width = 0.65.dp,
            color = if (isHighlight) PaleGold else accentColor.copy(alpha = 0.45f)
        )
    ) {
        Text(
            text = text,
            color = if (isHighlight) DarkGraphite else PaleGold,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.25.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun GestureBadge(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    progress: Float,
    isBoosted: Boolean = false,
    accentColor: Color = AmberGold,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xEB07101B),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = 1.1.dp,
            color = if (isBoosted) LiveRed else accentColor.copy(alpha = 0.65f)
        ),
        modifier = modifier
            .widthIn(min = 164.dp)
            .height(102.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon()
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    color = if (isBoosted) LiveRed else PaleGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                color = if (isBoosted) LiveRed else AmberGold,
                trackColor = Color(0xFF142234),
                modifier = Modifier
                    .width(132.dp)
                    .height(4.5.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}
