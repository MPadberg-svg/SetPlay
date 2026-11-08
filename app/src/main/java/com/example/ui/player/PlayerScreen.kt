package com.example.ui.player

import android.app.Activity
import android.os.Build
import android.view.KeyEvent
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
import androidx.compose.foundation.focusable
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.onKeyEvent
import com.example.ui.theme.tvFocusableControl
import com.example.ui.theme.tvFocusablePill
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
import com.example.data.repository.FootballMatch
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DarkGraphite
import com.example.ui.theme.LiveRed
import com.example.ui.theme.PaleGold
import com.example.ui.theme.TabularNumeralTextStyle
import com.example.ui.viewmodel.LiveGoalAlertEvent
import com.example.util.EditionResolver
import com.example.util.SetPlayEdition
import com.example.util.StreamMetadataParser
import kotlinx.coroutines.delay
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
    activeGoalAlert: LiveGoalAlertEvent? = null,
    onWatchGoalMatch: ((FootballMatch) -> Unit)? = null,
    onDismissGoalAlert: (() -> Unit)? = null,
    canSendToTv: Boolean = false,
    onSendToTv: (() -> Unit)? = null,
    activeEdition: SetPlayEdition = EditionResolver.resolveEdition(LocalContext.current),
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
    var showFeedSwitcherSheet by remember { mutableStateOf(false) }
    var showLiveChannelZapperDrawer by remember { mutableStateOf(false) }

    val zapperChannelList by viewModel.zapperChannelList.collectAsStateWithLifecycle()

    // Double-tap left/right VOD ripple state (-1 = left -10s, +1 = right +10s)
    var doubleTapRippleSide by remember { mutableStateOf<Int?>(null) }
    var doubleTapAccumulatedSec by remember { mutableIntStateOf(0) }
    var doubleTapRippleTick by remember { mutableIntStateOf(0) }

    LaunchedEffect(doubleTapRippleTick) {
        if (doubleTapRippleSide != null) {
            delay(650L)
            doubleTapRippleSide = null
            doubleTapAccumulatedSec = 0
        }
    }

    val playerFocusRequester = remember { FocusRequester() }
    var hasFocusedHudChild by remember { mutableStateOf(false) }

    val hasOpenOverlayOrSheet = uiState.showSubtitleLanguagePrompt ||
        uiState.isChannelZapperOpen ||
        showLiveChannelZapperDrawer ||
        showAudioSheet ||
        showSubtitleSheet ||
        showSpeedSheet ||
        showCatchupSheet ||
        showBufferSheet ||
        showSleepTimerSheet ||
        showFeedSwitcherSheet

    LaunchedEffect(uiState.isControlsVisible, hasOpenOverlayOrSheet) {
        if (!uiState.isControlsVisible && !hasOpenOverlayOrSheet) {
            hasFocusedHudChild = false
            runCatching { playerFocusRequester.requestFocus() }
        }
    }

    val isLiveMode = uiState.isLive

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(playerFocusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                val nativeEvent = keyEvent.nativeKeyEvent
                if (nativeEvent.action != KeyEvent.ACTION_DOWN) return@onKeyEvent false
                handlePlayerRemoteKeyEvent(
                    keyCode = nativeEvent.keyCode,
                    isLive = isLiveMode,
                    isControlsVisible = uiState.isControlsVisible,
                    isTouchLocked = uiState.isTouchLocked,
                    onShowControlsOrTogglePlay = {
                        if (!uiState.isControlsVisible) {
                            viewModel.showControls()
                        } else {
                            viewModel.togglePlayPause()
                        }
                    },
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onZapChannel = { offset -> viewModel.zapChannelOffset(offset) },
                    onSeekRelative = { seconds -> viewModel.seekRelative(seconds.toLong()) },
                    hasOpenOverlayOrSheet = hasOpenOverlayOrSheet,
                    hasFocusedHudChild = hasFocusedHudChild,
                    onOpenLiveChannelZapper = {
                        if (isLiveMode && !showLiveChannelZapperDrawer) {
                            showLiveChannelZapperDrawer = true
                        }
                    }
                )
            }
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

        // 2. Gesture Layer (Tap to show/hide controls; Phone-only: Double-Tap +/-10s with ripple, Left Swipe Brightness, Right Swipe Volume, Pinch Zoom)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(uiState.isTouchLocked, uiState.isLive, activeEdition) {
                    detectTapGestures(
                        onTap = {
                            if (uiState.isChannelZapperOpen) {
                                viewModel.toggleChannelZapper(false)
                            } else {
                                viewModel.toggleControls()
                            }
                        },
                        onDoubleTap = { offset ->
                            if (activeEdition != SetPlayEdition.PHONE) return@detectTapGestures
                            if (!uiState.isTouchLocked && !uiState.isLive) {
                                if (offset.x < screenWidth / 2f) {
                                    viewModel.seekRelative(-10)
                                    doubleTapAccumulatedSec = if (doubleTapRippleSide == -1) doubleTapAccumulatedSec + 10 else 10
                                    doubleTapRippleSide = -1
                                    doubleTapRippleTick++
                                } else {
                                    viewModel.seekRelative(10)
                                    doubleTapAccumulatedSec = if (doubleTapRippleSide == 1) doubleTapAccumulatedSec + 10 else 10
                                    doubleTapRippleSide = 1
                                    doubleTapRippleTick++
                                }
                            } else if (!uiState.isTouchLocked && uiState.isLive) {
                                viewModel.toggleAspectRatio()
                            }
                        }
                    )
                }
                .pointerInput(uiState.isTouchLocked, activeEdition) {
                    if (activeEdition != SetPlayEdition.PHONE || uiState.isTouchLocked) return@pointerInput
                    detectTransformGestures { _, pan, zoom, _ ->
                        if (abs(zoom - 1.0f) > 0.01f || uiState.videoZoomScale > 1.02f) {
                            viewModel.onTransformGesture(zoom, pan.x, pan.y)
                        }
                    }
                }
                .pointerInput(uiState.isTouchLocked, uiState.isLive, activeEdition) {
                    if (activeEdition != SetPlayEdition.PHONE || uiState.isTouchLocked) return@pointerInput
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

        // 2b. Double-Tap Left/Right Seek Ripple Indicator (-10s / +10s) on VOD Playback
        AnimatedVisibility(
            visible = !uiState.isLive && doubleTapRippleSide == -1,
            enter = fadeIn(tween(120)),
            exit = fadeOut(tween(220)),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 36.dp)
                .testTag("player_double_tap_rewind_ripple")
        ) {
            Surface(
                color = Color(0x990A0E17),
                shape = CircleShape,
                border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.55f))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Rewind",
                        tint = AmberGold,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "-${doubleTapAccumulatedSec.coerceAtLeast(10)}s",
                        color = PlayerTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = !uiState.isLive && doubleTapRippleSide == 1,
            enter = fadeIn(tween(120)),
            exit = fadeOut(tween(220)),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 36.dp)
                .testTag("player_double_tap_forward_ripple")
        ) {
            Surface(
                color = Color(0x990A0E17),
                shape = CircleShape,
                border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.55f))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Forward",
                        tint = AmberGold,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "+${doubleTapAccumulatedSec.coerceAtLeast(10)}s",
                        color = PlayerTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

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

        // 3b. Floating Auto-Prompt: "Subtitles — Choose Your Language" (Movies & Series)
        var promptRemainingSeconds by remember(uiState.showSubtitleLanguagePrompt, uiState.currentStream?.id) {
            mutableIntStateOf(uiState.subtitlePromptRemainingSeconds.coerceAtLeast(1))
        }
        LaunchedEffect(uiState.subtitlePromptRemainingSeconds) {
            if (uiState.subtitlePromptRemainingSeconds in 0..12) {
                promptRemainingSeconds = uiState.subtitlePromptRemainingSeconds
            }
        }
        LaunchedEffect(uiState.showSubtitleLanguagePrompt, uiState.currentStream?.id) {
            if (!uiState.isLive && uiState.showSubtitleLanguagePrompt) {
                promptRemainingSeconds = uiState.subtitlePromptRemainingSeconds.coerceIn(1, 12)
                while (promptRemainingSeconds > 0 && uiState.showSubtitleLanguagePrompt) {
                    delay(1_000L)
                    promptRemainingSeconds--
                }
                if (promptRemainingSeconds <= 0 && uiState.showSubtitleLanguagePrompt) {
                    viewModel.dismissSubtitleLanguagePrompt()
                }
            }
        }

        if (!uiState.isLive && uiState.showSubtitleLanguagePrompt) {
            val coreLanguagePills = listOf(
                Triple("ar", "🇸🇦", "Arabic"),
                Triple("en", "🇬🇧", "English"),
                Triple("fr", "🇫🇷", "Français"),
                Triple("es", "🇪🇸", "Español"),
                Triple("tr", "🇹🇷", "Türkçe")
            )
            val coreCodes = coreLanguagePills.map { it.first }.toSet()
            val extraLanguagePills = uiState.wyzieLanguageGroups
                .filter { it.languageCode.lowercase(Locale.ROOT) !in coreCodes }
                .map { group ->
                    Triple(
                        group.languageCode,
                        group.flagEmoji,
                        group.languageDisplay.substringBefore(" (").trim().ifBlank { group.languageCode.uppercase(Locale.ROOT) }
                    )
                }
            val preferredLang = uiState.lastSelectedSubtitleLanguageCode?.lowercase(Locale.ROOT)
            val allLanguagePills = (coreLanguagePills + extraLanguagePills).sortedByDescending { (code, _, _) ->
                preferredLang != null && code.equals(preferredLang, ignoreCase = true)
            }

            Surface(
                color = Color(0xEB0A0E17),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.45f)),
                shadowElevation = 14.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = if (uiState.isControlsVisible && !uiState.isTouchLocked) 96.dp else 28.dp
                    )
                    .widthIn(max = 560.dp)
                    .onFocusChanged { focusState ->
                        if (focusState.hasFocus) {
                            hasFocusedHudChild = true
                        }
                    }
                    .testTag("subtitle_language_prompt_card")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Subtitles,
                                    contentDescription = "Subtitles",
                                    tint = AmberGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Subtitles — Choose Your Language",
                                    color = PlayerTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (uiState.isSearchingOnlineSubtitles) {
                                    CircularProgressIndicator(
                                        color = AmberGold,
                                        strokeWidth = 1.8.dp,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    color = Color.White.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(0.6.dp, AmberGold.copy(alpha = 0.35f)),
                                    modifier = Modifier.testTag("subtitle_prompt_countdown_badge")
                                ) {
                                    Text(
                                        text = "${promptRemainingSeconds}s",
                                        color = PaleGold,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        style = TabularNumeralTextStyle,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "All Options ›",
                                    color = AmberGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .tvFocusablePill(
                                            accentColor = AmberGold,
                                            shape = RoundedCornerShape(8.dp),
                                            onClick = {
                                                viewModel.dismissSubtitleLanguagePrompt()
                                                showSubtitleSheet = true
                                                if (uiState.wyzieLanguageGroups.isEmpty() && !uiState.isSearchingOnlineSubtitles) {
                                                    viewModel.searchOnlineSubtitles()
                                                }
                                            }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        allLanguagePills.forEach { (code, flag, name) ->
                            val isPreferred = preferredLang != null && code.equals(preferredLang, ignoreCase = true)
                            val matchingGroup = uiState.wyzieLanguageGroups.find {
                                it.languageCode.equals(code, ignoreCase = true)
                            }
                            val countSuffix = matchingGroup?.options?.size?.takeIf { it > 0 }?.let { " ($it)" } ?: ""
                            Surface(
                                color = if (isPreferred) Color(0xFF1F2418) else Color(0xFF141C2B),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(
                                    width = if (isPreferred) 1.5.dp else 1.dp,
                                    color = when {
                                        isPreferred -> AmberGold
                                        matchingGroup != null -> AmberGold.copy(alpha = 0.55f)
                                        else -> Color(0x26FFFFFF)
                                    }
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .tvFocusablePill(
                                        accentColor = AmberGold,
                                        shape = RoundedCornerShape(50),
                                        onClick = { viewModel.chooseSubtitleLanguageFromPrompt(code) }
                                    )
                                    .testTag("subtitle_prompt_lang_$code")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(text = flag, fontSize = 13.sp)
                                    Text(
                                        text = "$name$countSuffix",
                                        color = if (isPreferred) PaleGold else PlayerTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isPreferred) FontWeight.ExtraBold else FontWeight.SemiBold
                                    )
                                    if (isPreferred) {
                                        Text(
                                            text = "★ Preferred",
                                            color = AmberGold,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }

                        // Off Pill
                        Surface(
                            color = Color(0xFF1A1418),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, Color(0x33FF6B6B)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .tvFocusablePill(
                                    accentColor = LiveRed,
                                    shape = RoundedCornerShape(50),
                                    onClick = { viewModel.chooseSubtitleLanguageFromPrompt(null) }
                                )
                                .testTag("subtitle_prompt_lang_off")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(text = "🚫", fontSize = 12.sp)
                                Text(
                                    text = "Off",
                                    color = PlayerTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    }

                    // 12-Second Countdown Progress Bar along bottom edge of prompt card
                    LinearProgressIndicator(
                        progress = { (promptRemainingSeconds.toFloat() / 12f).coerceIn(0f, 1f) },
                        color = AmberGold,
                        trackColor = Color.White.copy(alpha = 0.08f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.5.dp)
                    )
                }
            }
        }

        // 3c. Brief Confirmation Pill When Online Subtitles Activate
        if (!uiState.isLive && !uiState.subtitleActivationBannerMessage.isNullOrBlank()) {
            Surface(
                color = Color(0xD90A0E17),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.55f)),
                shadowElevation = 10.dp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 64.dp, start = 20.dp, end = 20.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        showSubtitleSheet = true
                    }
                    .testTag("subtitle_active_confirmation_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ClosedCaption,
                        contentDescription = "Active Subtitles",
                        tint = AmberGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = uiState.subtitleActivationBannerMessage!!,
                        color = PlayerTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // 3d. Flagship In-Player Live Goal Alert Banner (5-second Frosted Glass at Top-Right)
        AnimatedVisibility(
            visible = activeGoalAlert != null,
            enter = fadeIn(tween(200)) + slideInHorizontally(initialOffsetX = { it / 2 }),
            exit = fadeOut(tween(200)) + slideOutHorizontally(targetOffsetX = { it / 2 }),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .displayCutoutPadding()
                .padding(top = 64.dp, end = 20.dp, start = 20.dp)
                .testTag("player_live_goal_alert_banner")
        ) {
            activeGoalAlert?.let { alert ->
                Surface(
                    color = Color(0xEB0A141E),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.2.dp, AmberGold),
                    shadowElevation = 14.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.widthIn(max = 260.dp)) {
                            Text(
                                text = "⚽ GOAL! ${alert.homeTeamName} ${alert.homeScore} - ${alert.awayScore} ${alert.awayTeamName}",
                                color = PlayerTextPrimary,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${alert.scoringTeamName} scored • ${alert.minuteLabel} • ${alert.match.beinChannel}",
                                color = PaleGold,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (onWatchGoalMatch != null) {
                            Surface(
                                color = AmberGold,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .tvFocusablePill(
                                        accentColor = AmberGold,
                                        shape = RoundedCornerShape(50),
                                        onClick = {
                                            onDismissGoalAlert?.invoke()
                                            onWatchGoalMatch(alert.match)
                                        }
                                    )
                                    .testTag("player_goal_alert_watch_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Watch",
                                        tint = Color.Black,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Watch",
                                        color = Color.Black,
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

        // 3e. 15-Minute Halftime Explorer Timer Return Banner ("⚽ 2nd Half Starting — Return to [Channel]")
        if (uiState.showHalftimeReturnPrompt && uiState.halftimeSavedStream != null) {
            val savedChannelTitle = remember(uiState.halftimeSavedStream?.name) {
                StreamMetadataParser.cleanDisplayTitle(uiState.halftimeSavedStream?.name.orEmpty())
            }
            Surface(
                color = Color(0xF00B1622),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.2.dp, AmberGold),
                shadowElevation = 14.dp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .displayCutoutPadding()
                    .padding(top = 64.dp, start = 20.dp, end = 20.dp)
                    .clip(RoundedCornerShape(50))
                    .tvFocusablePill(
                        accentColor = AmberGold,
                        shape = RoundedCornerShape(50),
                        onClick = { viewModel.returnToHalftimeChannel() }
                    )
                    .testTag("halftime_return_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "⚽ 2nd Half Starting — Return to $savedChannelTitle",
                        color = PlayerTextPrimary,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Surface(
                        color = AmberGold,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "Switch Now",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = PlayerTextSecondary,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { viewModel.dismissHalftimeReturnPrompt() }
                    )
                }
            }
        }

        // 3f. 3-Stage Quality-Tier Fallback Notice Pill ("⚡ Quality adjusted to ... for stream stability")
        if (!uiState.qualityFallbackNotice.isNullOrBlank()) {
            Surface(
                color = Color(0xEB0A141E),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.65f)),
                shadowElevation = 12.dp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .displayCutoutPadding()
                    .padding(top = 64.dp, start = 20.dp, end = 20.dp)
                    .testTag("player_quality_fallback_notice")
            ) {
                Text(
                    text = uiState.qualityFallbackNotice!!,
                    color = PaleGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // 3g. Series 90% "Up Next" 10-Second Countdown & Auto-Play Card (`player_next_episode_card`)
        val nextEpPlayNowFocusRequester = remember { FocusRequester() }
        LaunchedEffect(uiState.showNextEpisodePrompt, uiState.nextSeriesEpisodeStream?.streamId, activeEdition) {
            if (uiState.showNextEpisodePrompt && uiState.nextSeriesEpisodeStream != null && activeEdition == SetPlayEdition.TV) {
                runCatching { nextEpPlayNowFocusRequester.requestFocus() }
            }
        }
        AnimatedVisibility(
            visible = uiState.showNextEpisodePrompt && uiState.nextSeriesEpisodeStream != null,
            enter = fadeIn(tween(200)) + slideInHorizontally(initialOffsetX = { it / 2 }),
            exit = fadeOut(tween(200)) + slideOutHorizontally(targetOffsetX = { it / 2 }),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .displayCutoutPadding()
                .padding(
                    end = 24.dp,
                    start = 24.dp,
                    bottom = if (uiState.isControlsVisible && !uiState.isTouchLocked) 104.dp else 32.dp
                )
                .testTag("player_next_episode_card")
        ) {
            uiState.nextSeriesEpisodeStream?.let { nextStream ->
                val cleanNextTitle = remember(nextStream.name) {
                    StreamMetadataParser.cleanDisplayTitle(nextStream.name).ifBlank { nextStream.name }
                }
                Surface(
                    color = Color(0xF20B1622),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.2.dp, AmberGold),
                    shadowElevation = 16.dp,
                    modifier = Modifier.widthIn(max = 420.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Up Next (${uiState.nextEpisodeCountdownSec}s): $cleanNextTitle",
                            color = PlayerTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = AmberGold,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .focusRequester(nextEpPlayNowFocusRequester)
                                    .tvFocusablePill(
                                        accentColor = AmberGold,
                                        shape = RoundedCornerShape(50),
                                        onClick = { viewModel.playNextSeriesEpisode() }
                                    )
                                    .testTag("player_next_episode_play_now_btn")
                            ) {
                                Text(
                                    text = "▶ Play Now",
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }

                            Surface(
                                color = Color.White.copy(alpha = 0.10f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .tvFocusablePill(
                                        accentColor = AmberGold,
                                        shape = RoundedCornerShape(50),
                                        onClick = { viewModel.cancelNextEpisodePrompt() }
                                    )
                                    .testTag("player_next_episode_cancel_btn")
                            ) {
                                Text(
                                    text = "Cancel",
                                    color = PlayerTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
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
                            .tvFocusablePill(
                                accentColor = AmberGold,
                                shape = RoundedCornerShape(6.dp),
                                onClick = { viewModel.retryCurrentStream() }
                            )
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
                    .onFocusChanged { focusState ->
                        hasFocusedHudChild = focusState.hasFocus
                    }
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Black.copy(alpha = 0.88f),
                            0.26f to Color.Black.copy(alpha = 0.48f),
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
                            .tvFocusableControl(
                                accentColor = AmberGold,
                                shape = CircleShape,
                                onClick = onBack
                            )
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
                        val rawStreamName = uiState.currentStream?.name ?: "Stream"
                        val cleanedStreamTitle = remember(rawStreamName) {
                            StreamMetadataParser.cleanDisplayTitle(rawStreamName)
                        }
                        val regionBadge = remember(rawStreamName) {
                            StreamMetadataParser.extractRegionOrLangBadge(rawStreamName)
                        }
                        val parsedQualityBadge = remember(rawStreamName) {
                            StreamMetadataParser.extractQualityBadge(rawStreamName)
                        }

                        Text(
                            text = cleanedStreamTitle,
                            color = PlayerTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val subtitleMeta = buildString {
                            if (!uiState.isLive && uiState.activeWyzieOption != null) {
                                append("CC: ${uiState.activeWyzieOption!!.languageDisplay}")
                            } else if (regionBadge != null) {
                                append(regionBadge)
                            }
                            val specs = listOf(
                                uiState.resolutionLabel.ifEmpty { parsedQualityBadge.orEmpty() },
                                uiState.videoCodecLabel,
                                uiState.audioCodecLabel,
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
                        // 1-Tap "📺 Send to TV" Handover Pill (Phone Edition ONLY)
                        if (activeEdition == SetPlayEdition.PHONE && canSendToTv && onSendToTv != null) {
                            Surface(
                                color = AmberGold.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, AmberGold),
                                modifier = Modifier
                                    .tvFocusablePill(
                                        accentColor = AmberGold,
                                        shape = RoundedCornerShape(50),
                                        onClick = onSendToTv
                                    )
                                    .testTag("player_send_to_tv_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tv,
                                        contentDescription = "Send to TV",
                                        tint = AmberGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "📺 Send to TV",
                                        color = AmberGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                        // Active 15m Halftime Explorer Countdown Badge (visible in Player Top Bar)
                        val htSeconds = uiState.halftimeTimerRemainingSeconds
                        if (htSeconds != null && htSeconds > 0) {
                            val mm = htSeconds / 60
                            val ss = htSeconds % 60
                            val countdownFormatted = String.format(Locale.US, "%02d:%02d", mm, ss)
                            Surface(
                                color = AmberGold.copy(alpha = 0.20f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.65f)),
                                modifier = Modifier
                                    .tvFocusablePill(
                                        accentColor = AmberGold,
                                        shape = RoundedCornerShape(50),
                                        onClick = {
                                            if (uiState.halftimeSavedStream != null && uiState.halftimeSavedStream?.id != uiState.currentStream?.id) {
                                                viewModel.returnToHalftimeChannel()
                                            } else {
                                                viewModel.startOrToggleHalftimeTimer()
                                            }
                                        }
                                    )
                                    .testTag("halftime_timer_countdown_badge")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "⏱️ HT $countdownFormatted",
                                        color = AmberGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        style = TabularNumeralTextStyle
                                    )
                                }
                            }
                        }

                        // Previous Channel Recall (Live TV only)
                        if (uiState.previousStream != null) {
                            Surface(
                                color = Color.White.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .tvFocusablePill(
                                        accentColor = AmberGold,
                                        shape = RoundedCornerShape(50),
                                        onClick = { viewModel.recallPreviousChannel() }
                                    )
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

                        // Channel List Drawer (Live TV only — toggles unified TvLiveChannelZapperOverlay)
                        if (uiState.isLive && (zapperChannelList.isNotEmpty() || uiState.siblingChannels.isNotEmpty())) {
                            IconButton(
                                onClick = { showLiveChannelZapperDrawer = !showLiveChannelZapperDrawer },
                                modifier = Modifier
                                    .size(40.dp)
                                    .tvFocusableControl(
                                        accentColor = AmberGold,
                                        shape = CircleShape,
                                        onClick = { showLiveChannelZapperDrawer = !showLiveChannelZapperDrawer }
                                    )
                                    .testTag("player_zapper_drawer_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                                    contentDescription = "Channels",
                                    tint = if (showLiveChannelZapperDrawer) AmberGold else PlayerTextPrimary,
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
                                .tvFocusableControl(
                                    accentColor = AmberGold,
                                    shape = CircleShape,
                                    onClick = { uiState.currentStream?.let { onToggleFavorite(it) } }
                                )
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
                                .tvFocusableControl(
                                    accentColor = AmberGold,
                                    shape = CircleShape,
                                    onClick = { onEnterInAppPip() }
                                )
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
                // C. CLEAN BOTTOM TIMELINE & 2-CLUSTER UTILITY BAR
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
                                fontWeight = FontWeight.Medium,
                                style = TabularNumeralTextStyle
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
                                fontWeight = FontWeight.Normal,
                                style = TabularNumeralTextStyle
                            )
                        }
                    } else {
                        // Clean Live Broadcast Row (Authentic Stream Specs, No EPG Clutter)
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
                                text = "LIVE BROADCAST",
                                color = PlayerTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Text(
                                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                                color = PlayerTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (activeEdition == SetPlayEdition.TV) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = Color(0xCC0D1826),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.8.dp, AmberGold.copy(alpha = 0.45f)),
                            modifier = Modifier.testTag("tv_player_remote_hint_pill")
                        ) {
                            Text(
                                text = "📺 Remote: OK Play/Pause • Left Channel List • Up/Down Zap",
                                color = PaleGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 2. Reorganized 2-Cluster Bottom Control Bar:
                    //    - Left Cluster: Primary Labeled Pills (Subtitles, Audio & Boost, Live Multi-Feed Switcher, Catchup, 15m HT Timer)
                    //    - Right Cluster: Compact Icon Pills (Aspect Ratio, Speed, Buffer, Sleep, Rotate, Lock)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // LEFT PRIMARY LABELED PILLS CLUSTER
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                PrimaryLabeledHudPill(
                                    icon = Icons.Default.ClosedCaption,
                                    label = subLabel,
                                    isActive = hasActiveSub,
                                    onClick = {
                                        viewModel.dismissSubtitleLanguagePrompt()
                                        showSubtitleSheet = true
                                        if (uiState.wyzieLanguageGroups.isEmpty() && !uiState.isSearchingOnlineSubtitles) {
                                            viewModel.searchOnlineSubtitles()
                                        }
                                    },
                                    modifier = Modifier.testTag("player_subtitle_tracks")
                                )
                            }

                            // Audio & Boost
                            PrimaryLabeledHudPill(
                                icon = Icons.AutoMirrored.Filled.VolumeUp,
                                label = if (uiState.volumePercent > 100) "Audio & Boost (${uiState.volumePercent}%)" else "Audio & Boost",
                                isActive = uiState.volumePercent > 100,
                                onClick = { showAudioSheet = true },
                                modifier = Modifier.testTag("player_audio_tracks")
                            )

                            // Live TV Specific Primary Pills: Channels Zapper, Multi-Feed Switcher, Catchup, 15m Halftime Timer
                            if (uiState.isLive) {
                                PrimaryLabeledHudPill(
                                    icon = Icons.Default.Tv,
                                    label = "Channels",
                                    isActive = showLiveChannelZapperDrawer,
                                    onClick = { showLiveChannelZapperDrawer = !showLiveChannelZapperDrawer },
                                    modifier = Modifier.testTag("player_channel_zapper_button")
                                )

                                val feedCount = uiState.alternateQualityFeeds.size.coerceAtLeast(1)
                                PrimaryLabeledHudPill(
                                    icon = Icons.Default.Bolt,
                                    label = "Feed ($feedCount)",
                                    isActive = feedCount > 1,
                                    onClick = { showFeedSwitcherSheet = true },
                                    modifier = Modifier.testTag("player_feed_switcher_button")
                                )

                                PrimaryLabeledHudPill(
                                    icon = Icons.Default.History,
                                    label = "Catchup",
                                    isActive = uiState.currentStream?.tvArchive == 1 || uiState.catchupPrograms.isNotEmpty(),
                                    onClick = { showCatchupSheet = true },
                                    modifier = Modifier.testTag("player_catchup_button")
                                )

                                val htRem = uiState.halftimeTimerRemainingSeconds
                                val htLabel = if (htRem != null && htRem > 0) {
                                    String.format(Locale.US, "%02d:%02d HT", htRem / 60, htRem % 60)
                                } else {
                                    "15m HT Timer"
                                }
                                PrimaryLabeledHudPill(
                                    icon = Icons.Default.Timer,
                                    label = htLabel,
                                    isActive = htRem != null && htRem > 0,
                                    onClick = { viewModel.startOrToggleHalftimeTimer() },
                                    modifier = Modifier.testTag("player_halftime_timer_button")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // RIGHT COMPACT ICON PILLS CLUSTER
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Aspect Ratio
                            val aspectBadge = when (uiState.aspectRatioMode) {
                                AspectRatioMode.FIT -> null
                                AspectRatioMode.FILL -> "FILL"
                                AspectRatioMode.RATIO_16_9 -> "16:9"
                                AspectRatioMode.ZOOM -> "ZOOM"
                            }
                            CompactIconHudPill(
                                icon = Icons.Default.AspectRatio,
                                contentDescription = "Aspect Ratio",
                                badgeText = aspectBadge,
                                isActive = uiState.aspectRatioMode != AspectRatioMode.FIT,
                                onClick = { viewModel.toggleAspectRatio() },
                                modifier = Modifier.testTag("player_aspect_ratio_button")
                            )

                            // Playback Speed (Movies & Series)
                            if (!uiState.isLive) {
                                CompactIconHudPill(
                                    icon = Icons.Default.Speed,
                                    contentDescription = "Playback Speed",
                                    badgeText = if (uiState.playbackSpeed != 1.0f) "${uiState.playbackSpeed}x" else null,
                                    isActive = uiState.playbackSpeed != 1.0f,
                                    onClick = { showSpeedSheet = true },
                                    modifier = Modifier.testTag("player_speed_button")
                                )
                            }

                            // Buffer Mode
                            val bufferBadge = when (uiState.bufferProfile) {
                                BufferProfile.FAST_ZAP -> "FAST"
                                BufferProfile.BALANCED -> null
                                BufferProfile.STABLE_4K -> "4K"
                            }
                            CompactIconHudPill(
                                icon = Icons.Default.Memory,
                                contentDescription = "Stream Buffer",
                                badgeText = bufferBadge,
                                isActive = uiState.bufferProfile != BufferProfile.BALANCED,
                                onClick = { showBufferSheet = true },
                                modifier = Modifier.testTag("player_buffer_button")
                            )

                            // Sleep Timer
                            CompactIconHudPill(
                                icon = Icons.Default.Bedtime,
                                contentDescription = "Sleep Timer",
                                badgeText = uiState.sleepTimerRemainingMinutes?.let { "${it}m" },
                                isActive = uiState.sleepTimerRemainingMinutes != null,
                                onClick = { showSleepTimerSheet = true },
                                modifier = Modifier.testTag("player_sleep_timer_button")
                            )

                            if (activeEdition == SetPlayEdition.PHONE) {
                                // Rotate (Phone & Tablet touch edition only)
                                CompactIconHudPill(
                                    icon = Icons.Default.ScreenRotation,
                                    contentDescription = "Rotate Screen",
                                    badgeText = null,
                                    isActive = uiState.isLandscapeLocked,
                                    onClick = { viewModel.toggleScreenOrientation(activity) },
                                    modifier = Modifier.testTag("player_rotate_button")
                                )

                                // Touch Lock (Phone & Tablet touch edition only)
                                CompactIconHudPill(
                                    icon = Icons.Default.LockOpen,
                                    contentDescription = "Lock Controls",
                                    badgeText = null,
                                    isActive = false,
                                    onClick = { viewModel.toggleTouchLock() },
                                    modifier = Modifier.testTag("player_touch_lock_button")
                                )
                            }
                        }
                    }
                }
            }
        }

        // 8b. 10-Foot & Touch Live TV Channel Zapper Left Sidebar (`player_live_channel_zapper_drawer`)
        if (showLiveChannelZapperDrawer && isLiveMode) {
            val channelsToRender = when {
                zapperChannelList.isNotEmpty() -> zapperChannelList
                uiState.siblingChannels.isNotEmpty() -> uiState.siblingChannels
                uiState.currentStream != null -> listOf(uiState.currentStream!!)
                else -> emptyList()
            }
            TvLiveChannelZapperOverlay(
                channels = channelsToRender,
                currentStreamId = uiState.currentStream?.streamId ?: -1L,
                onSelectChannel = { channel ->
                    viewModel.playStream(channel, playlist)
                },
                onClose = { showLiveChannelZapperDrawer = false },
                modifier = Modifier.align(Alignment.CenterStart)
            )
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
                                    .tvFocusableControl(
                                        accentColor = AmberGold,
                                        shape = RoundedCornerShape(8.dp),
                                        onClick = {
                                            viewModel.playStream(ch, playlist)
                                            viewModel.toggleChannelZapper(false)
                                        }
                                    )
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
    // MODAL SHEETS (AUDIO, WYZIE SUBTITLES, CATCHUP, BUFFER, SLEEP, SPEED, MULTI-FEED)
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

    // 5. Sleep Timer & 15m Halftime Explorer Sheet
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
                Text("Sleep & Match Timers", color = PlayerTextPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(10.dp))

                if (uiState.isLive) {
                    val htRem = uiState.halftimeTimerRemainingSeconds
                    val isHtActive = htRem != null && htRem > 0
                    Surface(
                        color = if (isHtActive) AmberGold.copy(alpha = 0.16f) else PlayerSheetCardBg,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (isHtActive) AmberGold else Color.White.copy(alpha = 0.10f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.startOrToggleHalftimeTimer()
                                showSleepTimerSheet = false
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isHtActive) "⏱️ Cancel 15m Halftime Timer" else "⏱️ Start 15m Halftime Explorer Timer",
                                    color = AmberGold,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Remembers ${uiState.currentStream?.name ?: "this channel"} while you zap or browse VOD, and alerts when 2nd Half starts.",
                                    color = PlayerTextSecondary,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.10f))
                    Spacer(modifier = Modifier.height(8.dp))
                }

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

    // 7. 1-Tap Multi-Feed Quality Switcher Sheet (Live TV 4K / FHD / HD / SD / Backup Streams)
    if (showFeedSwitcherSheet && uiState.isLive) {
        ModalBottomSheet(
            onDismissRequest = { showFeedSwitcherSheet = false },
            containerColor = PlayerSheetBg,
            modifier = Modifier.testTag("player_feed_switcher_sheet")
        ) {
            val feeds = uiState.alternateQualityFeeds.ifEmpty {
                listOfNotNull(uiState.currentStream)
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "⚡ Multi-Feed Quality & Backup Switcher (${feeds.size})",
                    color = PlayerTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Instant 1-tap switch between 4K, FHD, HD, SD, and backup servers for this channel.",
                    color = PlayerTextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(feeds, key = { it.id }) { feed ->
                        val isCurrent = feed.id == uiState.currentStream?.id
                        val upper = feed.name.uppercase(Locale.ROOT)
                        val qualityBadge = when {
                            upper.contains("4K") || upper.contains("UHD") || upper.contains("2160") -> "4K UHD"
                            upper.contains("FHD") || upper.contains("1080") -> "FHD 1080p"
                            upper.contains("HEVC") || upper.contains("H265") -> "HEVC"
                            upper.contains("HD") || upper.contains("720") -> "HD 720p"
                            upper.contains("SD") || upper.contains("480") || upper.contains("LOW") -> "SD"
                            else -> "LIVE FEED"
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isCurrent) AmberGold.copy(alpha = 0.16f) else PlayerSheetCardBg)
                                .border(
                                    width = 1.dp,
                                    color = if (isCurrent) AmberGold else Color.White.copy(alpha = 0.10f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .tvFocusableControl(
                                    accentColor = AmberGold,
                                    shape = RoundedCornerShape(10.dp),
                                    onClick = {
                                        viewModel.playStream(feed, playlist)
                                        showFeedSwitcherSheet = false
                                    }
                                )
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = feed.name,
                                    color = if (isCurrent) AmberGold else PlayerTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isCurrent) "Currently Playing" else "Tap to switch stream immediately",
                                    color = PlayerTextSecondary,
                                    fontSize = 11.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Surface(
                                color = if (isCurrent) AmberGold else Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = qualityBadge,
                                    color = if (isCurrent) Color.Black else AmberGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
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
            .tvFocusableControl(
                accentColor = AmberGold,
                shape = CircleShape,
                focusedScale = 1.08f,
                onClick = onClick
            )
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
 * Left Primary Labeled HUD Pill (Subtitles, Audio & Boost, Live Multi-Feed Switcher, Catchup, 15m HT Timer).
 */
@Composable
private fun PrimaryLabeledHudPill(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tint = if (isActive) AmberGold else PlayerTextPrimary
    Surface(
        color = if (isActive) AmberGold.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.10f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(
            width = 1.dp,
            color = if (isActive) AmberGold.copy(alpha = 0.60f) else Color.White.copy(alpha = 0.14f)
        ),
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .tvFocusablePill(
                accentColor = AmberGold,
                shape = RoundedCornerShape(50),
                onClick = onClick
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = tint,
                fontSize = 12.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

/**
 * Right Compact Icon HUD Pill (Aspect Ratio, Speed, Buffer, Sleep, Rotate, Lock).
 */
@Composable
private fun CompactIconHudPill(
    icon: ImageVector,
    contentDescription: String,
    badgeText: String? = null,
    isActive: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tint = if (isActive) AmberGold else PlayerTextPrimary
    Surface(
        color = if (isActive) AmberGold.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.09f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(
            width = 1.dp,
            color = if (isActive) AmberGold.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.12f)
        ),
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .tvFocusablePill(
                accentColor = AmberGold,
                shape = RoundedCornerShape(50),
                onClick = onClick
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = if (badgeText != null) 10.dp else 9.dp, vertical = 7.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(17.dp)
            )
            if (!badgeText.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = badgeText,
                    color = tint,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
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
                        .tvFocusablePill(
                            accentColor = AmberGold,
                            shape = RoundedCornerShape(50),
                            onClick = { onTurnSubtitlesOff() }
                        )
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
                                .tvFocusablePill(
                                    accentColor = AmberGold,
                                    shape = RoundedCornerShape(50),
                                    onClick = { onSelectLanguage(group.languageCode) }
                                )
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
                                    .tvFocusableControl(
                                        accentColor = AmberGold,
                                        shape = RoundedCornerShape(10.dp),
                                        onClick = { onSelectWyzieOption(option) }
                                    )
                                    .padding(horizontal = 14.dp, vertical = 11.dp)
                                    .testTag("wyzie_option_${idx + 1}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "${option.languageDisplay} #${idx + 1}  •  ${option.format}",
                                            color = if (isOptionActive) AmberGold else PlayerTextPrimary,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Surface(
                                            color = when {
                                                option.source.equals("SubDL", ignoreCase = true) -> Color(0x2629D9C2)
                                                option.source.equals("OpenSubtitles", ignoreCase = true) -> Color(0x2663B3ED)
                                                else -> AmberGold.copy(alpha = 0.18f)
                                            },
                                            shape = RoundedCornerShape(4.dp),
                                            border = BorderStroke(
                                                width = 0.6.dp,
                                                color = when {
                                                    option.source.equals("SubDL", ignoreCase = true) -> Color(0x8829D9C2)
                                                    option.source.equals("OpenSubtitles", ignoreCase = true) -> Color(0x8863B3ED)
                                                    else -> AmberGold.copy(alpha = 0.55f)
                                                }
                                            )
                                        ) {
                                            Text(
                                                text = option.source.ifBlank { "Wyzie" },
                                                color = when {
                                                    option.source.equals("SubDL", ignoreCase = true) -> Color(0xFF5EEAD4)
                                                    option.source.equals("OpenSubtitles", ignoreCase = true) -> Color(0xFF90CDF4)
                                                    else -> PaleGold
                                                },
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                            )
                                        }
                                        if (option.isHearingImpaired) {
                                            Surface(
                                                color = Color.White.copy(alpha = 0.12f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "HI",
                                                    color = PlayerTextSecondary,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
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

@Composable
internal fun TvLiveChannelZapperOverlay(
    channels: List<StreamEntity>,
    currentStreamId: Long,
    onSelectChannel: (StreamEntity) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeChannelFocusRequester = remember { FocusRequester() }
    val hasCurrentChannelMatch = remember(channels, currentStreamId) {
        channels.any { it.streamId == currentStreamId }
    }

    LaunchedEffect(currentStreamId, channels.size) {
        if (channels.isNotEmpty()) {
            runCatching { activeChannelFocusRequester.requestFocus() }
        }
    }

    Surface(
        color = Color(0xEB081018),
        modifier = modifier
            .width(310.dp)
            .fillMaxHeight()
            .border(
                width = 1.dp,
                color = AmberGold.copy(alpha = 0.55f),
                shape = RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp)
            )
            .testTag("player_live_channel_zapper_drawer")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .displayCutoutPadding()
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = "Live Channel Zapper",
                        tint = AmberGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "📋 Live Channel Zapper",
                            color = PlayerTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${channels.size} channels • Instant 1-tap switch",
                            color = PaleGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    color = Color.White.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.8.dp, AmberGold.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .tvFocusablePill(
                            accentColor = AmberGold,
                            shape = RoundedCornerShape(50),
                            onClick = onClose
                        )
                        .testTag("player_live_channel_zapper_close_btn")
                ) {
                    Text(
                        text = "✕ Close",
                        color = AmberGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = AmberGold.copy(alpha = 0.22f))
            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(
                    items = channels,
                    key = { idx, ch -> "${ch.id}_${ch.streamId}_$idx" }
                ) { index, ch ->
                    val isCurrent = ch.streamId == currentStreamId
                    val isInitialFocusTarget = isCurrent || (!hasCurrentChannelMatch && index == 0)
                    val cleanTitle = remember(ch.name) {
                        StreamMetadataParser.cleanDisplayTitle(ch.name).ifBlank { ch.name }
                    }
                    val qualityBadge = remember(ch.name) {
                        StreamMetadataParser.extractQualityBadge(ch.name)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isCurrent) AmberGold.copy(alpha = 0.18f) else Color(0xFF121A26)
                            )
                            .border(
                                width = if (isCurrent) 1.4.dp else 0.8.dp,
                                color = if (isCurrent) AmberGold else Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .then(
                                if (isInitialFocusTarget) {
                                    Modifier.focusRequester(activeChannelFocusRequester)
                                } else {
                                    Modifier
                                }
                            )
                            .tvFocusableControl(
                                accentColor = AmberGold,
                                shape = RoundedCornerShape(12.dp),
                                onClick = { onSelectChannel(ch) }
                            )
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("zapper_channel_item_${ch.streamId}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Channel Number Pill
                        Text(
                            text = String.format(Locale.US, "%02d", index + 1),
                            color = if (isCurrent) AmberGold else PlayerTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            style = TabularNumeralTextStyle,
                            modifier = Modifier.width(24.dp)
                        )

                        // Channel Logo
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(Color.Black.copy(alpha = 0.45f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (ch.streamIcon.isNotEmpty()) {
                                AsyncImage(
                                    model = ch.streamIcon,
                                    contentDescription = cleanTitle,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(2.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Tv,
                                    contentDescription = null,
                                    tint = if (isCurrent) AmberGold else PlayerTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(9.dp))

                        // Channel Name & Quality / Playing Status
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cleanTitle,
                                color = if (isCurrent) AmberGold else PlayerTextPrimary,
                                fontSize = 12.5.sp,
                                fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isCurrent) {
                                Text(
                                    text = "● PLAYING",
                                    color = LiveRed,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        if (!qualityBadge.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = AmberGold.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.7.dp, AmberGold.copy(alpha = 0.55f))
                            ) {
                                Text(
                                    text = qualityBadge,
                                    color = PaleGold,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

internal fun handlePlayerRemoteKeyEvent(
    keyCode: Int,
    isLive: Boolean,
    isControlsVisible: Boolean,
    isTouchLocked: Boolean,
    onShowControlsOrTogglePlay: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onZapChannel: (Int) -> Unit,
    onSeekRelative: (Int) -> Unit,
    hasOpenOverlayOrSheet: Boolean = false,
    hasFocusedHudChild: Boolean = false,
    onOpenLiveChannelZapper: (() -> Unit)? = null
): Boolean {
    if (isTouchLocked) return false
    val delegateDpadToFocusedUi = hasOpenOverlayOrSheet || (isControlsVisible && hasFocusedHudChild)
    return when (keyCode) {
        KeyEvent.KEYCODE_DPAD_CENTER,
        KeyEvent.KEYCODE_ENTER,
        KeyEvent.KEYCODE_NUMPAD_ENTER -> {
            if (delegateDpadToFocusedUi) {
                false
            } else {
                onShowControlsOrTogglePlay()
                true
            }
        }
        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
            onTogglePlayPause()
            true
        }
        KeyEvent.KEYCODE_CHANNEL_UP -> {
            if (isLive) {
                onZapChannel(1)
                true
            } else {
                false
            }
        }
        KeyEvent.KEYCODE_CHANNEL_DOWN -> {
            if (isLive) {
                onZapChannel(-1)
                true
            } else {
                false
            }
        }
        KeyEvent.KEYCODE_DPAD_UP -> {
            if (delegateDpadToFocusedUi) {
                false
            } else if (isLive && !isControlsVisible) {
                onZapChannel(1)
                true
            } else {
                false
            }
        }
        KeyEvent.KEYCODE_DPAD_DOWN -> {
            if (delegateDpadToFocusedUi) {
                false
            } else if (isLive && !isControlsVisible) {
                onZapChannel(-1)
                true
            } else {
                false
            }
        }
        KeyEvent.KEYCODE_DPAD_LEFT -> {
            if (delegateDpadToFocusedUi) {
                false
            } else if (isLive && !isControlsVisible && onOpenLiveChannelZapper != null) {
                onOpenLiveChannelZapper()
                true
            } else if (!isLive && !isControlsVisible) {
                onSeekRelative(-10)
                true
            } else {
                false
            }
        }
        KeyEvent.KEYCODE_DPAD_RIGHT -> {
            if (delegateDpadToFocusedUi) {
                false
            } else if (!isLive && !isControlsVisible) {
                onSeekRelative(10)
                true
            } else {
                false
            }
        }
        KeyEvent.KEYCODE_MEDIA_REWIND -> {
            if (!isLive) {
                onSeekRelative(-10)
                true
            } else {
                false
            }
        }
        KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
            if (!isLive) {
                onSeekRelative(10)
                true
            } else {
                false
            }
        }
        else -> false
    }
}


