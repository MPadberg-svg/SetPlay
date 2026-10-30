package com.example.ui.player

import android.app.Activity
import android.app.PictureInPictureParams
import android.os.Build
import android.util.Rational
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.data.model.PlaylistEntity
import com.example.data.model.StreamEntity
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CatchupGreen
import com.example.ui.theme.Champagne
import com.example.ui.theme.DarkGraphite
import com.example.ui.theme.LiveRed
import com.example.ui.theme.MetalCharcoal
import com.example.ui.theme.MetalCharcoalElevated
import com.example.ui.theme.PaleGold
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
    val activity = context as? Activity

    // Automatically restore screen orientation when exiting full-screen player
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetScreenOrientationToPortrait(activity)
        }
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
            .background(DarkGraphite)
            .testTag("player_screen")
    ) {
        val screenWidth = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val screenHeight = constraints.maxHeight.toFloat().coerceAtLeast(1f)

        // 1. Media3 Video View with VLC/MX Player Pro Pinch-to-Zoom & Pan
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
                                // On Live TV, double-tap toggles Aspect Ratio / Full-screen fit (MX Player style)
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

        // 3. Gesture Feedback Overlays (Brightness, Volume + 200% Audio Boost, Scrub) in Royal Gold
        if (uiState.gestureBrightness != null) {
            GestureBadge(
                icon = { Icon(Icons.Default.BrightnessMedium, contentDescription = null, tint = AmberGold) },
                label = "Brightness",
                value = "${uiState.brightnessPercent}%",
                progress = uiState.brightnessPercent / 100f,
                isBoosted = false,
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
                        tint = if (isBoost) AmberGold else PaleGold
                    )
                },
                label = if (isBoost) "VLC Audio Boost" else "Volume",
                value = "$volPct%",
                progress = (volPct / 200f).coerceIn(0f, 1f),
                isBoosted = isBoost,
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
                label = "Scrub",
                value = "$prefix s",
                progress = 1f,
                isBoosted = false,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Pinch-to-Zoom Reset Pill when zoomed in
        if (uiState.videoZoomScale > 1.02f) {
            Surface(
                color = MetalCharcoal.copy(alpha = 0.92f),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, AmberGold),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 58.dp)
                    .clickable { viewModel.resetZoomAndPan() }
                    .testTag("player_reset_zoom_pill")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
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

        // 4. Buffering Indicator in Royal Gold
        if (uiState.isBuffering) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(DarkGraphite.copy(alpha = 0.78f), CircleShape)
                    .border(1.dp, Champagne.copy(alpha = 0.35f), CircleShape)
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AmberGold, strokeWidth = 3.dp)
            }
        }

        // 5. Error Banner with Instant Retry
        if (uiState.error != null) {
            Surface(
                color = LiveRed.copy(alpha = 0.92f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 64.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
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

        // 6. Touch Lock Mode (MX Player Pro style)
        if (uiState.isTouchLocked && uiState.isControlsVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(20.dp)
            ) {
                Surface(
                    color = MetalCharcoal.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .border(1.dp, AmberGold, RoundedCornerShape(24.dp))
                        .clickable { viewModel.toggleTouchLock() }
                        .testTag("player_unlock_touch_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Unlock Screen", tint = AmberGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Screen Locked • Tap to Unlock", color = PaleGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 7. Full Royal Gold VLC / MX Player Pro Controls HUD Overlay (when unlocked)
        AnimatedVisibility(
            visible = uiState.isControlsVisible && !uiState.isTouchLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                DarkGraphite.copy(alpha = 0.88f),
                                Color.Transparent,
                                Color.Transparent,
                                DarkGraphite.copy(alpha = 0.94f)
                            )
                        )
                    )
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("player_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PaleGold
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp)
                    ) {
                        Text(
                            text = uiState.currentStream?.name ?: "SetPlay Stream",
                            color = PaleGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (uiState.currentProgram != null) {
                                Text(
                                    text = "Now: ${uiState.currentProgram?.title}",
                                    color = Champagne,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                            if (uiState.resolutionLabel.isNotEmpty()) {
                                TechBadge(
                                    text = uiState.resolutionLabel,
                                    isHighlight = uiState.is4K
                                )
                            }
                            if (uiState.videoCodecLabel.isNotEmpty()) {
                                TechBadge(text = uiState.videoCodecLabel)
                            }
                            if (uiState.audioCodecLabel.isNotEmpty()) {
                                TechBadge(text = uiState.audioCodecLabel)
                            }
                            if (uiState.fpsLabel.isNotEmpty()) {
                                TechBadge(text = uiState.fpsLabel)
                            }
                        }
                    }

                    // Screen Rotation Toggle (Full-Screen Landscape / Portrait)
                    IconButton(
                        onClick = { viewModel.toggleScreenOrientation(activity) },
                        modifier = Modifier.testTag("player_rotate_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ScreenRotation,
                            contentDescription = "Rotate Landscape / Portrait",
                            tint = if (uiState.isLandscapeLocked) AmberGold else PaleGold
                        )
                    }

                    // Last Channel Recall Button
                    if (uiState.previousStream != null) {
                        Surface(
                            color = AmberGold,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .clickable { viewModel.recallPreviousChannel() }
                                .testTag("player_recall_channel_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Icon(Icons.Default.Replay, contentDescription = "Previous Channel", tint = DarkGraphite, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Recall", color = DarkGraphite, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }

                    // Live Channel Zapper Drawer Toggle
                    if (uiState.isLive && uiState.siblingChannels.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.toggleChannelZapper() },
                            modifier = Modifier.testTag("player_zapper_drawer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatListBulleted,
                                contentDescription = "Channel List Drawer",
                                tint = AmberGold
                            )
                        }
                    }

                    // Catchup button if stream supports archive
                    if (uiState.currentStream?.tvArchive == 1 || uiState.catchupPrograms.isNotEmpty()) {
                        IconButton(
                            onClick = { showCatchupSheet = true },
                            modifier = Modifier.testTag("player_catchup_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Catchup Archive",
                                tint = CatchupGreen
                            )
                        }
                    }

                    // Favorite Button
                    val isFav = uiState.currentStream?.isFavorite == true
                    IconButton(
                        onClick = { uiState.currentStream?.let { onToggleFavorite(it) } },
                        modifier = Modifier.testTag("player_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (isFav) AmberGold else PaleGold
                        )
                    }

                    // In-App Picture-in-Picture (PiP) Button
                    IconButton(
                        onClick = { onEnterInAppPip() },
                        modifier = Modifier.testTag("player_pip_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureInPictureAlt,
                            contentDescription = "Picture in Picture",
                            tint = AmberGold
                        )
                    }
                }

                // LEFT SIDE: VLC / MX Player Pro Vertical Brightness Slider Bar
                VerticalSideControlBar(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.BrightnessMedium,
                            contentDescription = "Brightness",
                            tint = AmberGold,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    valueLabel = "${uiState.brightnessPercent}%",
                    progress = (uiState.brightnessPercent / 100f).coerceIn(0f, 1f),
                    isBoosted = false,
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
                        .padding(start = 12.dp)
                        .testTag("player_left_brightness_bar")
                )

                // RIGHT SIDE: VLC / MX Player Pro Vertical Volume & 200% Audio Boost Slider Bar
                VerticalSideControlBar(
                    icon = {
                        Icon(
                            imageVector = if (uiState.volumePercent == 0) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Volume & Audio Boost",
                            tint = if (uiState.volumePercent > 100) AmberGold else PaleGold,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    valueLabel = "${uiState.volumePercent}%",
                    progress = (uiState.volumePercent / 200f).coerceIn(0f, 1f),
                    isBoosted = uiState.volumePercent > 100,
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
                        .padding(end = 12.dp)
                        .testTag("player_right_volume_bar")
                )

                // Center Playback & Channel Zap Controls in Royal Gold
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.isLive && uiState.siblingChannels.size > 1) {
                        IconButton(
                            onClick = { viewModel.zapChannelOffset(-1) },
                            modifier = Modifier
                                .size(46.dp)
                                .background(MetalCharcoal.copy(alpha = 0.85f), CircleShape)
                                .border(1.dp, Champagne.copy(alpha = 0.35f), CircleShape)
                                .testTag("player_prev_channel")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Previous Channel",
                                tint = AmberGold,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    if (!uiState.isLive) {
                        IconButton(
                            onClick = { viewModel.seekRelative(-10) },
                            modifier = Modifier
                                .size(50.dp)
                                .background(MetalCharcoal.copy(alpha = 0.85f), CircleShape)
                                .border(1.dp, Champagne.copy(alpha = 0.35f), CircleShape)
                                .testTag("player_rewind_10")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FastRewind,
                                contentDescription = "Rewind 10s",
                                tint = PaleGold,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    // Primary Amber Gold Play/Pause Button with Dark Graphite Icon
                    IconButton(
                        onClick = { viewModel.togglePlayPause() },
                        modifier = Modifier
                            .size(68.dp)
                            .background(AmberGold, CircleShape)
                            .border(2.dp, PaleGold.copy(alpha = 0.6f), CircleShape)
                            .testTag("player_play_pause")
                    ) {
                        Icon(
                            imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                            tint = DarkGraphite,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    if (!uiState.isLive) {
                        IconButton(
                            onClick = { viewModel.seekRelative(10) },
                            modifier = Modifier
                                .size(50.dp)
                                .background(MetalCharcoal.copy(alpha = 0.85f), CircleShape)
                                .border(1.dp, Champagne.copy(alpha = 0.35f), CircleShape)
                                .testTag("player_forward_10")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FastForward,
                                contentDescription = "Forward 10s",
                                tint = PaleGold,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    if (uiState.isLive && uiState.siblingChannels.size > 1) {
                        IconButton(
                            onClick = { viewModel.zapChannelOffset(1) },
                            modifier = Modifier
                                .size(46.dp)
                                .background(MetalCharcoal.copy(alpha = 0.85f), CircleShape)
                                .border(1.dp, Champagne.copy(alpha = 0.35f), CircleShape)
                                .testTag("player_next_channel")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Next Channel",
                                tint = AmberGold,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                // Bottom HUD Control Bar in Royal Gold
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    if (!uiState.isLive) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatTime(uiState.currentPositionMs),
                                color = PaleGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Slider(
                                value = if (uiState.durationMs > 0) uiState.currentPositionMs.toFloat() else 0f,
                                onValueChange = { pos -> viewModel.seekToPosition(pos.toLong()) },
                                valueRange = 0f..(uiState.durationMs.toFloat().coerceAtLeast(1f)),
                                colors = SliderDefaults.colors(
                                    thumbColor = AmberGold,
                                    activeTrackColor = AmberGold,
                                    inactiveTrackColor = MetalCharcoalElevated
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp)
                                    .testTag("player_timeline_slider")
                            )
                            Text(
                                text = formatTime(uiState.durationMs),
                                color = Champagne,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        // Live TV status bar with EPG progress
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(LiveRed, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LIVE BROADCAST",
                                color = PaleGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            if (uiState.upcomingPrograms.size > 1) {
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Next: ${uiState.upcomingPrograms[1].title}",
                                    color = Champagne,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                            if (uiState.sleepTimerRemainingMinutes != null) {
                                Text(
                                    text = "Sleep: ${uiState.sleepTimerRemainingMinutes}m",
                                    color = AmberGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                            Text(
                                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                                color = Champagne,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Context-Aware Bottom Action Pills Row in Metal Charcoal & Champagne
                    // NOTE: For Live TV channels (`uiState.isLive == true`), Play Speed and Subtitles are REMOVED.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Touch Lock Button (MX Player Pro style)
                        IconButton(
                            onClick = { viewModel.toggleTouchLock() },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("player_touch_lock_button")
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = "Lock Touch", tint = Champagne, modifier = Modifier.size(20.dp))
                        }

                        // 2. Audio Track Selector
                        IconButton(
                            onClick = { showAudioSheet = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("player_audio_tracks")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Audio Tracks", tint = Champagne, modifier = Modifier.size(20.dp))
                        }

                        // 3. Subtitle Track Selector — ONLY shown for VOD (Movies & Series), removed for Live TV
                        if (!uiState.isLive) {
                            IconButton(
                                onClick = { showSubtitleSheet = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("player_subtitle_tracks")
                            ) {
                                Icon(Icons.Default.ClosedCaption, contentDescription = "Subtitles", tint = Champagne, modifier = Modifier.size(20.dp))
                            }
                        }

                        // 4. Sleep Timer Button
                        IconButton(
                            onClick = { showSleepTimerSheet = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("player_sleep_timer_button")
                        ) {
                            Icon(
                                Icons.Default.Bedtime,
                                contentDescription = "Sleep Timer",
                                tint = if (uiState.sleepTimerRemainingMinutes != null) AmberGold else Champagne,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // 5. Screen Rotation Pill (Landscape / Portrait)
                        Surface(
                            color = if (uiState.isLandscapeLocked) AmberGold else MetalCharcoal,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (uiState.isLandscapeLocked) PaleGold else Champagne.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .clickable { viewModel.toggleScreenOrientation(activity) }
                                .testTag("player_rotate_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    Icons.Default.ScreenRotation,
                                    contentDescription = "Rotate Screen",
                                    tint = if (uiState.isLandscapeLocked) DarkGraphite else AmberGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (uiState.isLandscapeLocked) "Landscape" else "Rotate",
                                    color = if (uiState.isLandscapeLocked) DarkGraphite else PaleGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // 6. Buffer Profile Selector Pill
                        Surface(
                            color = MetalCharcoal,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Champagne.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .clickable { showBufferSheet = true }
                                .testTag("player_buffer_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Icon(Icons.Default.Memory, contentDescription = "Buffer", tint = AmberGold, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (uiState.bufferProfile) {
                                        BufferProfile.FAST_ZAP -> "Zap"
                                        BufferProfile.BALANCED -> "Auto"
                                        BufferProfile.STABLE_4K -> "4K Buf"
                                    },
                                    color = PaleGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // 7. Playback Speed Pill — ONLY shown for VOD (Movies & Series), removed for Live TV
                        if (!uiState.isLive) {
                            Surface(
                                color = MetalCharcoal,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Champagne.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .clickable { showSpeedSheet = true }
                                    .testTag("player_speed_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                ) {
                                    Icon(Icons.Default.Speed, contentDescription = "Speed", tint = AmberGold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${uiState.playbackSpeed}x",
                                        color = PaleGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // 8. Aspect Ratio Toggle Pill
                        Surface(
                            color = MetalCharcoal,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Champagne.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .clickable { viewModel.toggleAspectRatio() }
                                .testTag("player_aspect_ratio_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Icon(Icons.Default.AspectRatio, contentDescription = "Aspect Ratio", tint = AmberGold, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = uiState.aspectRatioMode.name.replace("RATIO_", "").replace("_", ":"),
                                    color = PaleGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 8. In-Player Quick Channel Zapper Drawer in Royal Gold
        AnimatedVisibility(
            visible = uiState.isChannelZapperOpen,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Surface(
                color = DarkGraphite.copy(alpha = 0.96f),
                border = BorderStroke(1.dp, Champagne.copy(alpha = 0.3f)),
                modifier = Modifier
                    .width(290.dp)
                    .fillMaxHeight()
                    .testTag("in_player_channel_zapper")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(12.dp)
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
                        Text(
                            text = "Close",
                            color = AmberGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { viewModel.toggleChannelZapper(false) }
                                .padding(6.dp)
                        )
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
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isCurrent) AmberGold else MetalCharcoal)
                                    .border(
                                        width = 1.dp,
                                        color = if (isCurrent) PaleGold else Champagne.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(10.dp)
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
                                        .clip(RoundedCornerShape(6.dp))
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

    // Audio Tracks & VLC Audio Boost Modal Sheet
    if (showAudioSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAudioSheet = false },
            containerColor = MetalCharcoal
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Text("Audio Tracks & VLC Audio Boost", color = PaleGold, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Volume & Audio Boost: ${uiState.volumePercent}% ${if (uiState.volumePercent > 100) "(+${((uiState.volumePercent - 100) * 0.12f).roundToInt()} dB Boost)" else ""}",
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
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AmberGold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Subtitle Tracks Modal Sheet (VOD only)
    if (showSubtitleSheet && !uiState.isLive) {
        ModalBottomSheet(
            onDismissRequest = { showSubtitleSheet = false },
            containerColor = MetalCharcoal
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Text("Select Subtitles", color = PaleGold, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.selectSubtitleTrack(null)
                            showSubtitleSheet = false
                        }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtitles Off", color = PaleGold, fontSize = 14.sp)
                    if (uiState.subtitleTracks.none { it.isSelected }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AmberGold)
                    }
                }

                LazyColumn {
                    items(uiState.subtitleTracks) { track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectSubtitleTrack(track)
                                    showSubtitleSheet = false
                                }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = track.name,
                                color = if (track.isSelected) AmberGold else PaleGold,
                                fontSize = 14.sp
                            )
                            if (track.isSelected) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AmberGold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Catchup Archive Modal Sheet
    if (showCatchupSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCatchupSheet = false },
            containerColor = MetalCharcoal
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
                                    .clip(RoundedCornerShape(8.dp))
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

    // Buffer Profile Modal Sheet
    if (showBufferSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBufferSheet = false },
            containerColor = MetalCharcoal
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
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AmberGold)
                        }
                    }
                }
            }
        }
    }

    // Sleep Timer Modal Sheet
    if (showSleepTimerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSleepTimerSheet = false },
            containerColor = MetalCharcoal
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
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AmberGold)
                        }
                    }
                }
            }
        }
    }

    // Playback Speed Modal Sheet (VOD only)
    if (showSpeedSheet && !uiState.isLive) {
        ModalBottomSheet(
            onDismissRequest = { showSpeedSheet = false },
            containerColor = MetalCharcoal
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
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AmberGold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * VLC / MX Player Pro vertical side slider capsule for Brightness (left) and Volume + 200% Boost (right).
 */
@Composable
fun VerticalSideControlBar(
    icon: @Composable () -> Unit,
    valueLabel: String,
    progress: Float,
    isBoosted: Boolean,
    onStepUp: () -> Unit,
    onStepDown: () -> Unit,
    onVerticalDragRatio: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkGraphite.copy(alpha = 0.82f),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, if (isBoosted) AmberGold else Champagne.copy(alpha = 0.35f)),
        modifier = modifier
            .width(42.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val deltaRatio = -dragAmount.y / 220f
                    onVerticalDragRatio(deltaRatio)
                }
            }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Step Up (+) Button
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(MetalCharcoal)
                    .clickable { onStepUp() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase", tint = PaleGold, modifier = Modifier.size(14.dp))
            }

            // Vertical Track
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(86.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MetalCharcoal),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(progress.coerceIn(0.04f, 1f))
                        .background(if (isBoosted) AmberGold else PaleGold, RoundedCornerShape(3.dp))
                )
            }

            Text(
                text = valueLabel,
                color = if (isBoosted) AmberGold else PaleGold,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )

            icon()

            // Step Down (-) Button
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(MetalCharcoal)
                    .clickable { onStepDown() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Champagne, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun TechBadge(
    text: String,
    isHighlight: Boolean = false
) {
    Surface(
        color = if (isHighlight) PaleGold else MetalCharcoal,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(0.5.dp, if (isHighlight) AmberGold else Champagne.copy(alpha = 0.4f))
    ) {
        Text(
            text = text,
            color = if (isHighlight) DarkGraphite else Champagne,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
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
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkGraphite.copy(alpha = 0.90f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (isBoosted) AmberGold else Champagne.copy(alpha = 0.35f)),
        modifier = modifier.size(width = 156.dp, height = 98.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon()
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    color = if (isBoosted) AmberGold else PaleGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, color = AmberGold, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                color = if (isBoosted) AmberGold else PaleGold,
                trackColor = MetalCharcoal,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
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
