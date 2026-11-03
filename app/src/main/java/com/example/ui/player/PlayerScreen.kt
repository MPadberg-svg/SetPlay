package com.example.ui.player

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ClosedCaptionDisabled
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
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
import com.example.ui.theme.DarkGraphite
import com.example.ui.theme.LiveRed
import com.example.ui.theme.PaleGold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private val PlayerTextPrimary = Color(0xFFFFFFFF)
private val PlayerTextSecondary = Color(0xB8FFFFFF)
private val PlayerTextMuted = Color(0x80FFFFFF)
private val PlayerSheetBg = Color(0xFF10131A)
private val PlayerSheetCardBg = Color(0xFF181C26)

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

    // True Full-Screen Immersive System Bars Management:
    // Hides both statusBars() and navigationBars() with transient swipe behavior so the
    // phone's 3-button / gesture navigation bar never takes part of the screen during playback.
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

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("player_screen")
    ) {
        val screenWidth = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val screenHeight = constraints.maxHeight.toFloat().coerceAtLeast(1f)

        // 1. Edge-to-Edge Media3 Video Surface
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

        // 2. Gesture Layer (Tap to show/hide controls, Double-Tap +/-10s, Left Swipe Brightness, Right Swipe Volume, Pinch Zoom)
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

        // 3. Clean Cinema Subtitle Overlay (Movies & Series)
        val subtitleBottomPad by animateDpAsState(
            targetValue = if (uiState.isControlsVisible && !uiState.isTouchLocked) 96.dp else 24.dp,
            animationSpec = tween(200),
            label = "subtitle_bottom_pad"
        )
        if (!uiState.isLive && !uiState.currentSubtitleText.isNullOrBlank()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 36.dp, end = 36.dp, bottom = subtitleBottomPad)
                    .testTag("player_active_subtitle_overlay")
            ) {
                Surface(
                    color = Color(0xB3000000),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = uiState.currentSubtitleText!!,
                        color = Color.White,
                        fontSize = (17f * uiState.subtitleScaleFactor).sp,
                        lineHeight = (23f * uiState.subtitleScaleFactor).sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(0f, 1.5f),
                                blurRadius = 4f
                            )
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // 4. Minimal Gesture Feedback Pill (only visible while swiping Brightness, Volume, or Scrubbing)
        if (uiState.gestureBrightness != null) {
            MinimalGesturePill(
                icon = Icons.Default.BrightnessMedium,
                label = "${uiState.brightnessPercent}%",
                progress = uiState.brightnessPercent / 100f,
                isBoosted = false,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (uiState.gestureVolume != null) {
            val volPct = uiState.gestureVolume!!
            MinimalGesturePill(
                icon = if (volPct == 0) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                label = "$volPct%",
                progress = (volPct / 200f).coerceIn(0f, 1f),
                isBoosted = volPct > 100,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (uiState.gestureSeekSeconds != null) {
            val sec = uiState.gestureSeekSeconds!!
            val prefix = if (sec >= 0) "+$sec" else "$sec"
            MinimalGesturePill(
                icon = if (sec >= 0) Icons.Default.FastForward else Icons.Default.FastRewind,
                label = "${prefix}s",
                progress = null,
                isBoosted = false,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Pinch-to-Zoom Reset Pill (only visible when zoomed in)
        if (uiState.videoZoomScale > 1.02f) {
            Surface(
                color = Color(0xBF000000),
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.25f)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 64.dp)
                    .clickable { viewModel.resetZoomAndPan() }
                    .testTag("player_reset_zoom_pill")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.ZoomOutMap, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${String.format(Locale.US, "%.1fx", uiState.videoZoomScale)} • Reset",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 5. Clean Buffering Spinner
        if (uiState.isBuffering) {
            CircularProgressIndicator(
                color = AmberGold,
                trackColor = Color.White.copy(alpha = 0.18f),
                strokeWidth = 2.8.dp,
                modifier = Modifier
                    .size(44.dp)
                    .align(Alignment.Center)
            )
        }

        // 6. Subtle Error Banner with Retry
        if (uiState.error != null) {
            Surface(
                color = Color(0xE6180B0D),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(0.8.dp, LiveRed.copy(alpha = 0.6f)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 68.dp, start = 24.dp, end = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = uiState.error!!,
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        color = AmberGold,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .clickable { viewModel.retryCurrentStream() }
                            .testTag("player_retry_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retry", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 7. Touch Lock Capsule (when screen is locked)
        if (uiState.isTouchLocked && uiState.isControlsVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .displayCutoutPadding()
                    .padding(20.dp)
            ) {
                Surface(
                    color = Color(0xCC000000),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .clickable { viewModel.toggleTouchLock() }
                        .testTag("player_unlock_touch_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Unlock", tint = AmberGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Screen Locked • Tap to Unlock",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 8. Clean, Simple, Professional Cinema Player Controls Overlay
        AnimatedVisibility(
            visible = uiState.isControlsVisible && !uiState.isTouchLocked,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(180)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Black.copy(alpha = 0.78f),
                            0.22f to Color.Black.copy(alpha = 0.25f),
                            0.50f to Color.Black.copy(alpha = 0.15f),
                            0.76f to Color.Black.copy(alpha = 0.30f),
                            1.0f to Color.Black.copy(alpha = 0.86f)
                        )
                    )
                    .displayCutoutPadding()
            ) {
                // =========================================================================
                // A. CLEAN MINIMAL TOP BAR (Frameless on subtle top gradient)
                // =========================================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("player_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PlayerTextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Title + Subtle Single-Line Metadata
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = uiState.currentStream?.name ?: "Stream",
                            color = PlayerTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val subtitleMeta = buildString {
                            if (uiState.currentProgram != null) {
                                append(uiState.currentProgram!!.title)
                            } else if (!uiState.isLive && uiState.activeWyzieOption != null) {
                                append("CC: ${uiState.activeWyzieOption!!.languageDisplay}")
                            }
                            val specs = listOf(
                                uiState.resolutionLabel,
                                uiState.videoCodecLabel,
                                uiState.fpsLabel
                            ).filter { it.isNotEmpty() }
                            if (specs.isNotEmpty()) {
                                if (isNotEmpty()) append("  •  ")
                                append(specs.joinToString(" • "))
                            }
                        }

                        if (subtitleMeta.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = subtitleMeta,
                                color = PlayerTextSecondary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Top-Right Clean Utility Icons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Previous Channel Recall (Live TV only)
                        if (uiState.previousStream != null) {
                            Surface(
                                color = Color.White.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .clickable { viewModel.recallPreviousChannel() }
                                    .testTag("player_recall_channel_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Replay,
                                        contentDescription = "Recall",
                                        tint = PlayerTextPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Recall",
                                        color = PlayerTextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Channel List Drawer (Live TV only)
                        if (uiState.isLive && uiState.siblingChannels.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.toggleChannelZapper() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("player_zapper_drawer_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                                    contentDescription = "Channels",
                                    tint = if (uiState.isChannelZapperOpen) AmberGold else PlayerTextPrimary,
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                        }

                        // Catchup Archive (Live TV only when supported)
                        if (uiState.currentStream?.tvArchive == 1 || uiState.catchupPrograms.isNotEmpty()) {
                            IconButton(
                                onClick = { showCatchupSheet = true },
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("player_catchup_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "Catchup",
                                    tint = PlayerTextPrimary,
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                        }

                        // Favorite Toggle
                        val isFav = uiState.currentStream?.isFavorite == true
                        IconButton(
                            onClick = { uiState.currentStream?.let { onToggleFavorite(it) } },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("player_favorite_button")
                        ) {
                            Icon(
                                imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Favorite",
                                tint = if (isFav) AmberGold else PlayerTextPrimary,
                                modifier = Modifier.size(21.dp)
                            )
                        }

                        // In-App Picture-in-Picture
                        IconButton(
                            onClick = { onEnterInAppPip() },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("player_pip_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureInPictureAlt,
                                contentDescription = "Picture in Picture",
                                tint = PlayerTextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // =========================================================================
                // B. MINIMAL CENTER PLAYBACK CONTROLS (Netflix / Apple TV+ Style)
                // =========================================================================
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(36.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.isLive && uiState.siblingChannels.size > 1) {
                        CleanCenterCircleButton(
                            icon = Icons.Default.SkipPrevious,
                            contentDescription = "Previous Channel",
                            sizeDp = 48,
                            iconSizeDp = 24,
                            onClick = { viewModel.zapChannelOffset(-1) },
                            modifier = Modifier.testTag("player_prev_channel")
                        )
                    } else if (!uiState.isLive) {
                        CleanCenterCircleButton(
                            icon = Icons.Default.FastRewind,
                            contentDescription = "Rewind 10 seconds",
                            sizeDp = 48,
                            iconSizeDp = 24,
                            onClick = { viewModel.seekRelative(-10) },
                            modifier = Modifier.testTag("player_rewind_10")
                        )
                    }

                    // Primary Play / Pause Button
                    CleanCenterCircleButton(
                        icon = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                        sizeDp = 64,
                        iconSizeDp = 32,
                        isPrimary = true,
                        onClick = { viewModel.togglePlayPause() },
                        modifier = Modifier.testTag("player_play_pause")
                    )

                    if (!uiState.isLive) {
                        CleanCenterCircleButton(
                            icon = Icons.Default.FastForward,
                            contentDescription = "Forward 10 seconds",
                            sizeDp = 48,
                            iconSizeDp = 24,
                            onClick = { viewModel.seekRelative(10) },
                            modifier = Modifier.testTag("player_forward_10")
                        )
                    } else if (uiState.isLive && uiState.siblingChannels.size > 1) {
                        CleanCenterCircleButton(
                            icon = Icons.Default.SkipNext,
                            contentDescription = "Next Channel",
                            sizeDp = 48,
                            iconSizeDp = 24,
                            onClick = { viewModel.zapChannelOffset(1) },
                            modifier = Modifier.testTag("player_next_channel")
                        )
                    }
                }

                // =========================================================================
                // C. CLEAN BOTTOM TIMELINE & ACTION ROW (No Heavy Boxed Container)
                // =========================================================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    // 1. Sleek Cinema Progress Line (VOD) OR Minimal Live Status Line (Live TV)
                    if (!uiState.isLive) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatTime(uiState.currentPositionMs),
                                color = PlayerTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )

                            CinemaTimelineScrubber(
                                positionMs = uiState.currentPositionMs,
                                durationMs = uiState.durationMs,
                                onSeekTo = { targetMs -> viewModel.seekToPosition(targetMs) },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp)
                                    .testTag("player_timeline_slider")
                            )

                            Text(
                                text = formatTime(uiState.durationMs),
                                color = PlayerTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    } else {
                        // Clean Live Broadcast Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(LiveRed, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LIVE",
                                color = PlayerTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            val nextTitle = when {
                                uiState.upcomingPrograms.size > 1 -> "Next: ${uiState.upcomingPrograms[1].title}"
                                uiState.currentProgram != null -> uiState.currentProgram!!.title
                                else -> ""
                            }
                            if (nextTitle.isNotEmpty()) {
                                Text(
                                    text = nextTitle,
                                    color = PlayerTextSecondary,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }

                            Text(
                                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                                color = PlayerTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 2. Clean, Frameless Bottom Utility Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Subtitles (Movies & Series)
                        if (!uiState.isLive) {
                            val hasActiveSub = uiState.activeWyzieOption != null ||
                                uiState.subtitleTracks.any { it.isSelected }
                            val subLabel = when {
                                uiState.activeWyzieOption != null ->
                                    "Subtitles (${uiState.activeWyzieOption!!.languageCode.uppercase(Locale.ROOT)})"
                                uiState.subtitleTracks.any { it.isSelected } -> "Subtitles (ON)"
                                else -> "Subtitles"
                            }
                            MinimalBottomBarAction(
                                icon = Icons.Default.ClosedCaption,
                                label = subLabel,
                                isActive = hasActiveSub,
                                onClick = {
                                    showSubtitleSheet = true
                                    if (uiState.wyzieLanguageGroups.isEmpty() && !uiState.isSearchingOnlineSubtitles) {
                                        viewModel.searchOnlineSubtitles()
                                    }
                                },
                                modifier = Modifier.testTag("player_subtitle_tracks")
                            )
                        }

                        // Audio & Volume Boost
                        MinimalBottomBarAction(
                            icon = Icons.AutoMirrored.Filled.VolumeUp,
                            label = if (uiState.volumePercent > 100) "Audio (${uiState.volumePercent}%)" else "Audio",
                            isActive = uiState.volumePercent > 100,
                            onClick = { showAudioSheet = true },
                            modifier = Modifier.testTag("player_audio_tracks")
                        )

                        // Playback Speed (Movies & Series)
                        if (!uiState.isLive) {
                            MinimalBottomBarAction(
                                icon = Icons.Default.Speed,
                                label = "Speed (${uiState.playbackSpeed}x)",
                                isActive = uiState.playbackSpeed != 1.0f,
                                onClick = { showSpeedSheet = true },
                                modifier = Modifier.testTag("player_speed_button")
                            )
                        }

                        // Aspect Ratio
                        val aspectLabel = when (uiState.aspectRatioMode) {
                            AspectRatioMode.FIT -> "Fit"
                            AspectRatioMode.FILL -> "Fill"
                            AspectRatioMode.RATIO_16_9 -> "16:9"
                            AspectRatioMode.ZOOM -> "Zoom"
                        }
                        MinimalBottomBarAction(
                            icon = Icons.Default.AspectRatio,
                            label = aspectLabel,
                            isActive = uiState.aspectRatioMode != AspectRatioMode.FIT,
                            onClick = { viewModel.toggleAspectRatio() },
                            modifier = Modifier.testTag("player_aspect_ratio_button")
                        )

                        // Screen Orientation
                        MinimalBottomBarAction(
                            icon = Icons.Default.ScreenRotation,
                            label = if (uiState.isLandscapeLocked) "Landscape" else "Rotate",
                            isActive = uiState.isLandscapeLocked,
                            onClick = { viewModel.toggleScreenOrientation(activity) },
                            modifier = Modifier.testTag("player_rotate_button")
                        )

                        // Buffer Mode
                        MinimalBottomBarAction(
                            icon = Icons.Default.Memory,
                            label = when (uiState.bufferProfile) {
                                BufferProfile.FAST_ZAP -> "Fast Zap"
                                BufferProfile.BALANCED -> "Buffer"
                                BufferProfile.STABLE_4K -> "4K Buffer"
                            },
                            isActive = uiState.bufferProfile != BufferProfile.BALANCED,
                            onClick = { showBufferSheet = true },
                            modifier = Modifier.testTag("player_buffer_button")
                        )

                        // Sleep Timer
                        MinimalBottomBarAction(
                            icon = Icons.Default.Bedtime,
                            label = if (uiState.sleepTimerRemainingMinutes != null) {
                                "${uiState.sleepTimerRemainingMinutes}m"
                            } else {
                                "Sleep"
                            },
                            isActive = uiState.sleepTimerRemainingMinutes != null,
                            onClick = { showSleepTimerSheet = true },
                            modifier = Modifier.testTag("player_sleep_timer_button")
                        )

                        // Touch Lock
                        MinimalBottomBarAction(
                            icon = Icons.Default.LockOpen,
                            label = "Lock",
                            isActive = false,
                            onClick = { viewModel.toggleTouchLock() },
                            modifier = Modifier.testTag("player_touch_lock_button")
                        )
                    }
                }
            }
        }

        // 9. Quick Channel Zapper Drawer (Live TV)
        AnimatedVisibility(
            visible = uiState.isChannelZapperOpen,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Surface(
                color = Color(0xF210131A),
                modifier = Modifier
                    .width(290.dp)
                    .fillMaxHeight()
                    .testTag("in_player_channel_zapper")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .displayCutoutPadding()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Channels (${uiState.siblingChannels.size})",
                            color = PlayerTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Close",
                            color = AmberGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable { viewModel.toggleChannelZapper(false) }
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.siblingChannels, key = { it.id }) { ch ->
                            val isCurrent = ch.id == uiState.currentStream?.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isCurrent) AmberGold.copy(alpha = 0.18f) else PlayerSheetCardBg
                                    )
                                    .border(
                                        width = if (isCurrent) 1.dp else 0.dp,
                                        color = if (isCurrent) AmberGold else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        viewModel.playStream(ch, playlist)
                                        viewModel.toggleChannelZapper(false)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black.copy(alpha = 0.35f)),
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
                                        Icon(Icons.Default.Tv, contentDescription = null, tint = PlayerTextSecondary, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = ch.name,
                                    color = if (isCurrent) AmberGold else PlayerTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // =================================================================================
    // MODAL SHEETS (AUDIO, WYZIE SUBTITLES, CATCHUP, BUFFER, SLEEP, SPEED)
    // =================================================================================

    // 1. Audio Tracks & Volume Boost Sheet
    if (showAudioSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAudioSheet = false },
            containerColor = PlayerSheetBg
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Audio & Volume",
                    color = PlayerTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Volume: ${uiState.volumePercent}% ${
                        if (uiState.volumePercent > 100) "(Audio Boost +${((uiState.volumePercent - 100) * 0.12f).roundToInt()} dB)" else ""
                    }",
                    color = if (uiState.volumePercent > 100) AmberGold else PlayerTextSecondary,
                    fontSize = 13.sp
                )
                Slider(
                    value = uiState.volumePercent.toFloat(),
                    onValueChange = { viewModel.setVolumePercent(it.roundToInt(), showHudBadge = false) },
                    valueRange = 0f..200f,
                    colors = SliderDefaults.colors(
                        thumbColor = AmberGold,
                        activeTrackColor = AmberGold,
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(8.dp))
                if (uiState.audioTracks.isEmpty()) {
                    Text(
                        text = "Default Audio Track",
                        color = PlayerTextSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
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
                                    color = if (track.isSelected) AmberGold else PlayerTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = if (track.isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                                if (track.isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = AmberGold, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // 2. Clean 2-Step Wyzie Online Subtitle Sheet (Movies & Series)
    if (showSubtitleSheet && !uiState.isLive) {
        WyzieSubtitleStudioSheet(
            uiState = uiState,
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

    // 3. Catchup Archive Sheet
    if (showCatchupSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCatchupSheet = false },
            containerColor = PlayerSheetBg
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Catchup & Timeshift",
                    color = PlayerTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = uiState.currentStream?.name ?: "",
                    color = PlayerTextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        uiState.currentStream?.let { viewModel.playStream(it, playlist) }
                        showCatchupSheet = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Return to Live Broadcast", color = Color.Black, fontWeight = FontWeight.SemiBold)
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
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PlayerSheetCardBg)
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
                                    Text(prog.title, color = PlayerTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Text(
                                        text = "${sdf.format(Date(prog.startEpoch * 1000))} ($durationMin min)",
                                        color = PlayerTextSecondary,
                                        fontSize = 11.5.sp
                                    )
                                }
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = AmberGold)
                            }
                        }
                    }
                } else {
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
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(label, color = PlayerTextPrimary, fontSize = 14.sp)
                            Icon(Icons.Default.History, contentDescription = null, tint = PlayerTextSecondary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // 4. Buffer Profile Sheet
    if (showBufferSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBufferSheet = false },
            containerColor = PlayerSheetBg
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                Text("Stream Buffer", color = PlayerTextPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(10.dp))
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
                                color = if (isSelected) AmberGold else PlayerTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                            Text(text = profile.description, color = PlayerTextSecondary, fontSize = 12.sp)
                        }
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = AmberGold, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // 5. Sleep Timer Sheet
    if (showSleepTimerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSleepTimerSheet = false },
            containerColor = PlayerSheetBg
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                Text("Sleep Timer", color = PlayerTextPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(10.dp))
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
                            color = if (isSelected) AmberGold else PlayerTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = AmberGold, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // 6. Playback Speed Sheet (VOD only)
    if (showSpeedSheet && !uiState.isLive) {
        ModalBottomSheet(
            onDismissRequest = { showSpeedSheet = false },
            containerColor = PlayerSheetBg
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                Text("Playback Speed", color = PlayerTextPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(10.dp))
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
                            color = if (isSelected) AmberGold else PlayerTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = AmberGold, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

/**
 * Custom thin cinema timeline scrubber (replaces the bulky default Material3 Slider).
 */
@Composable
private fun CinemaTimelineScrubber(
    positionMs: Long,
    durationMs: Long,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val safeDuration = durationMs.coerceAtLeast(1L)
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }
    var trackWidthPx by remember { mutableFloatStateOf(1f) }

    val currentFraction = if (isDragging) {
        dragFraction.coerceIn(0f, 1f)
    } else {
        (positionMs.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)
    }

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .height(28.dp)
            .onSizeChanged { size ->
                trackWidthPx = size.width.toFloat().coerceAtLeast(1f)
            }
            .pointerInput(safeDuration) {
                detectTapGestures { offset ->
                    val frac = (offset.x / trackWidthPx).coerceIn(0f, 1f)
                    onSeekTo((frac * safeDuration).toLong())
                }
            }
            .pointerInput(safeDuration) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        dragFraction = (offset.x / trackWidthPx).coerceIn(0f, 1f)
                    },
                    onDragEnd = {
                        val target = (dragFraction * safeDuration).toLong()
                        isDragging = false
                        onSeekTo(target)
                    },
                    onDragCancel = {
                        isDragging = false
                    },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        dragFraction = (change.position.x / trackWidthPx).coerceIn(0f, 1f)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val trackHeight = 3.5.dp.toPx()
            val centerY = size.height / 2f
            val cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f)

            // Unplayed background track
            drawRoundRect(
                color = Color.White.copy(alpha = 0.26f),
                topLeft = Offset(0f, centerY - trackHeight / 2f),
                size = Size(size.width, trackHeight),
                cornerRadius = cornerRadius
            )

            // Active played track
            val activeWidth = (size.width * currentFraction).coerceIn(0f, size.width)
            if (activeWidth > 0f) {
                drawRoundRect(
                    color = AmberGold,
                    topLeft = Offset(0f, centerY - trackHeight / 2f),
                    size = Size(activeWidth, trackHeight),
                    cornerRadius = cornerRadius
                )
            }

            // Sleek circular thumb
            val thumbRadius = if (isDragging) 7.5.dp.toPx() else 5.5.dp.toPx()
            drawCircle(
                color = AmberGold,
                radius = thumbRadius,
                center = Offset(activeWidth, centerY)
            )
        }
    }
}

/**
 * Clean, understated circular center transport button (Netflix / Apple TV+ style).
 */
@Composable
private fun CleanCenterCircleButton(
    icon: ImageVector,
    contentDescription: String,
    sizeDp: Int,
    iconSizeDp: Int,
    isPrimary: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.Black.copy(alpha = if (isPrimary) 0.52f else 0.42f),
        shape = CircleShape,
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = if (isPrimary) 0.32f else 0.20f)
        ),
        modifier = modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(iconSizeDp.dp)
            )
        }
    }
}

/**
 * Frameless, clean bottom utility action item (Icon + Text).
 */
@Composable
private fun MinimalBottomBarAction(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tint = if (isActive) AmberGold else PlayerTextPrimary
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = tint,
            fontSize = 12.5.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1
        )
    }
}

/**
 * Minimal gesture feedback pill shown only while actively swiping brightness, volume, or timeline.
 */
@Composable
private fun MinimalGesturePill(
    icon: ImageVector,
    label: String,
    progress: Float?,
    isBoosted: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xCC0D1017),
        shape = RoundedCornerShape(50),
        border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.18f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isBoosted) LiveRed else AmberGold,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (progress != null) {
                Spacer(modifier = Modifier.width(10.dp))
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    color = if (isBoosted) LiveRed else AmberGold,
                    trackColor = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier
                        .width(72.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

/**
 * Clean 2-Step Online Subtitles Sheet (Powered by Wyzie Subs API):
 * - Step 1: Choose Language
 * - Step 2: Select Subtitle Option to start watching immediately
 */
@ExperimentalMaterial3Api
@Composable
private fun WyzieSubtitleStudioSheet(
    uiState: PlayerUiState,
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
        containerColor = PlayerSheetBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Subtitles",
                        color = PlayerTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    val matchedId = uiState.resolvedMediaQuery?.imdbId
                        ?: uiState.resolvedMediaQuery?.tmdbId?.let { "TMDB:$it" }
                    Text(
                        text = buildString {
                            append(uiState.resolvedMediaQuery?.cleanTitle ?: uiState.currentStream?.name ?: "")
                            if (!matchedId.isNullOrBlank()) append(" • $matchedId")
                        },
                        color = PlayerTextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Turn Subtitles Off
                val allOff = uiState.activeWyzieOption == null && uiState.subtitleTracks.none { it.isSelected }
                Surface(
                    color = if (allOff) AmberGold.copy(alpha = 0.2f) else PlayerSheetCardBg,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.8.dp, if (allOff) AmberGold else Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .clickable { onTurnSubtitlesOff() }
                        .testTag("subtitle_off_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ClosedCaptionDisabled,
                            contentDescription = "Off",
                            tint = if (allOff) AmberGold else PlayerTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Off",
                            color = if (allOff) AmberGold else PlayerTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Input Bar (for custom title / IMDb ID / Season / Episode)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchInput,
                    onValueChange = { searchInput = it },
                    placeholder = {
                        Text("Title or IMDb ID (tt...)", color = PlayerTextMuted, fontSize = 12.sp)
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PlayerTextPrimary,
                        unfocusedTextColor = PlayerTextPrimary,
                        focusedBorderColor = AmberGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.18f),
                        focusedContainerColor = PlayerSheetCardBg,
                        unfocusedContainerColor = PlayerSheetCardBg
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("wyzie_search_input")
                )

                if (isSeries) {
                    OutlinedTextField(
                        value = seasonInput,
                        onValueChange = { seasonInput = it.filter { ch -> ch.isDigit() }.take(2) },
                        label = { Text("S", fontSize = 10.sp, color = PlayerTextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PlayerTextPrimary,
                            unfocusedTextColor = PlayerTextPrimary,
                            focusedBorderColor = AmberGold,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.18f)
                        ),
                        modifier = Modifier.width(54.dp)
                    )
                    OutlinedTextField(
                        value = episodeInput,
                        onValueChange = { episodeInput = it.filter { ch -> ch.isDigit() }.take(3) },
                        label = { Text("E", fontSize = 10.sp, color = PlayerTextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PlayerTextPrimary,
                            unfocusedTextColor = PlayerTextPrimary,
                            focusedBorderColor = AmberGold,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.18f)
                        ),
                        modifier = Modifier.width(56.dp)
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
                        containerColor = AmberGold,
                        contentColor = Color.Black
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    modifier = Modifier.testTag("wyzie_search_button")
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Search", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Subtitle Sync & Size Bar (visible when an online subtitle is active)
            if (uiState.activeWyzieOption != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PlayerSheetCardBg)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Sync ${String.format(Locale.US, "%+.1fs", uiState.subtitleSyncOffsetMs / 1000f)}",
                            color = PlayerTextSecondary,
                            fontSize = 12.sp
                        )
                        SmallTextButton(label = "-0.5s", onClick = { onAdjustSyncOffset(-500L) })
                        SmallTextButton(label = "+0.5s", onClick = { onAdjustSyncOffset(500L) })
                        if (uiState.subtitleSyncOffsetMs != 0L) {
                            SmallTextButton(label = "Reset", onClick = onResetSyncOffset)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(0.85f to "S", 1.0f to "M", 1.2f to "L", 1.4f to "XL").forEach { (scale, label) ->
                            val selected = abs(uiState.subtitleScaleFactor - scale) < 0.05f
                            Surface(
                                color = if (selected) AmberGold else Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.clickable { onSelectScaleFactor(scale) }
                            ) {
                                Text(
                                    text = label,
                                    color = if (selected) Color.Black else PlayerTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step 1 (Language) & Step 2 (Subtitle Options)
            if (uiState.isSearchingOnlineSubtitles || uiState.isDownloadingSubtitle) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AmberGold, strokeWidth = 2.5.dp, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (uiState.isDownloadingSubtitle) "Loading subtitle..." else "Searching subtitles...",
                            color = PlayerTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else if (uiState.wyzieLanguageGroups.isNotEmpty()) {
                Text(
                    text = "Language",
                    color = PlayerTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wyzie_language_row")
                ) {
                    items(uiState.wyzieLanguageGroups, key = { it.languageCode }) { group ->
                        val isSelected = selectedGroup?.languageCode == group.languageCode
                        val bg by animateColorAsState(
                            targetValue = if (isSelected) AmberGold else PlayerSheetCardBg,
                            label = "lang_${group.languageCode}"
                        )
                        Surface(
                            color = bg,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) AmberGold else Color.White.copy(alpha = 0.12f)
                            ),
                            modifier = Modifier
                                .clickable { onSelectLanguage(group.languageCode) }
                                .testTag("wyzie_lang_${group.languageCode}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(text = group.flagEmoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = group.languageDisplay,
                                    color = if (isSelected) Color.Black else PlayerTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "(${group.options.size})",
                                    color = if (isSelected) Color.Black.copy(alpha = 0.7f) else PlayerTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedGroup != null) {
                    Text(
                        text = "${selectedGroup.languageDisplay} Options",
                        color = PlayerTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .testTag("wyzie_options_list")
                    ) {
                        itemsIndexed(
                            selectedGroup.options,
                            key = { idx, opt -> "${opt.id}_$idx" }
                        ) { idx, option ->
                            val isOptionActive = uiState.activeWyzieOption?.url == option.url
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isOptionActive) AmberGold.copy(alpha = 0.16f) else PlayerSheetCardBg
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isOptionActive) AmberGold else Color.White.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onSelectWyzieOption(option) }
                                    .padding(horizontal = 14.dp, vertical = 11.dp)
                                    .testTag("wyzie_option_${idx + 1}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${option.languageDisplay} #${idx + 1}  •  ${option.format}",
                                        color = if (isOptionActive) AmberGold else PlayerTextPrimary,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = option.mediaName,
                                        color = PlayerTextSecondary,
                                        fontSize = 11.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                if (isOptionActive) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = AmberGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Text(
                                        text = "Apply",
                                        color = AmberGold,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = uiState.onlineSubtitleStatusMessage
                        ?: "Enter the movie/series title or IMDb ID above and tap Search.",
                    color = PlayerTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            // Embedded Stream Subtitle Tracks (if present in stream)
            if (uiState.subtitleTracks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Embedded Tracks",
                    color = PlayerTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.subtitleTracks) { track ->
                        val selected = track.isSelected && uiState.activeWyzieOption == null
                        Surface(
                            color = if (selected) AmberGold else PlayerSheetCardBg,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.clickable { onSelectEmbeddedTrack(track) }
                        ) {
                            Text(
                                text = track.name,
                                color = if (selected) Color.Black else PlayerTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SmallTextButton(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.08f),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            color = PlayerTextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
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
