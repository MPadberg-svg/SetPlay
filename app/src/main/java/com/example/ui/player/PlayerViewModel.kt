package com.example.ui.player

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import androidx.annotation.OptIn
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.data.local.StreamVaultDatabase
import com.example.data.model.EpgProgramEntity
import com.example.data.model.PlaylistEntity
import com.example.data.model.StreamEntity
import com.example.data.remote.ResolvedMediaQuery
import com.example.data.remote.SubtitleCue
import com.example.data.remote.WyzieLanguageGroup
import com.example.data.remote.WyzieSubtitleOption
import com.example.data.remote.WyzieSubtitleService
import com.example.data.remote.XtreamClient
import com.example.data.repository.IptvRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

enum class AspectRatioMode {
    FIT,
    FILL,
    RATIO_16_9,
    ZOOM
}

enum class BufferProfile(val label: String, val description: String) {
    FAST_ZAP("Fast Zap (1.5s)", "Sub-second channel switching"),
    BALANCED("Balanced (5s)", "Optimal stability & speed"),
    STABLE_4K("Ultra Stable 4K (15s)", "Prevents freeze on heavy 4K streams")
}

data class TrackItem(
    val id: String,
    val name: String,
    val language: String,
    val isSelected: Boolean,
    val groupIndex: Int,
    val trackIndex: Int
)

data class PlayerUiState(
    val currentStream: StreamEntity? = null,
    val previousStream: StreamEntity? = null,
    val siblingChannels: List<StreamEntity> = emptyList(),
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val aspectRatioMode: AspectRatioMode = AspectRatioMode.FIT,
    val bufferProfile: BufferProfile = BufferProfile.BALANCED,
    val resolutionLabel: String = "",
    val videoCodecLabel: String = "",
    val audioCodecLabel: String = "",
    val fpsLabel: String = "",
    val is4K: Boolean = false,
    val isLive: Boolean = true,
    val isTouchLocked: Boolean = false,
    val isChannelZapperOpen: Boolean = false,
    val sleepTimerRemainingMinutes: Int? = null,
    val audioTracks: List<TrackItem> = emptyList(),
    val subtitleTracks: List<TrackItem> = emptyList(),
    val currentProgram: EpgProgramEntity? = null,
    val upcomingPrograms: List<EpgProgramEntity> = emptyList(),
    val catchupPrograms: List<EpgProgramEntity> = emptyList(),
    val error: String? = null,
    // VLC & MX Player Pro states
    val volumePercent: Int = 75,         // 0..200 (101..200 uses LoudnessEnhancer Audio Boost)
    val brightnessPercent: Int = 65,     // 1..100
    val videoZoomScale: Float = 1.0f,    // 1.0f..3.0f pinch-to-zoom
    val videoPanOffsetX: Float = 0f,
    val videoPanOffsetY: Float = 0f,
    val isLandscapeLocked: Boolean = false,
    // Gesture HUD overlay states
    val gestureBrightness: Float? = null, // 0..1
    val gestureVolume: Int? = null,       // 0..200
    val gestureSeekSeconds: Long? = null, // relative scrub seconds
    val isControlsVisible: Boolean = true,
    // Wyzie Online Subtitles (Movies & Series)
    val wyzieLanguageGroups: List<WyzieLanguageGroup> = emptyList(),
    val selectedWyzieLanguageCode: String? = null,
    val activeWyzieOption: WyzieSubtitleOption? = null,
    val activeWyzieCues: List<SubtitleCue> = emptyList(),
    val currentSubtitleText: String? = null,
    val isSearchingOnlineSubtitles: Boolean = false,
    val isDownloadingSubtitle: Boolean = false,
    val onlineSubtitleStatusMessage: String? = null,
    val resolvedMediaQuery: ResolvedMediaQuery? = null,
    val subtitleSyncOffsetMs: Long = 0L,
    val subtitleScaleFactor: Float = 1.0f
)

@OptIn(UnstableApi::class)
class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val database = StreamVaultDatabase.getInstance(application)
    private val repository = IptvRepository(
        playlistDao = database.playlistDao(),
        categoryDao = database.categoryDao(),
        streamDao = database.streamDao(),
        epgDao = database.epgDao(),
        parentalDao = database.parentalSettingsDao()
    )
    private val wyzieSubtitleService = WyzieSubtitleService()

    private val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var activePlaylistCache: PlaylistEntity? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var volumeFloatAccumulator: Float = 75f
    private var brightnessFloatAccumulator: Float = 65f
    private var subtitleSearchJob: Job? = null
    private var subtitleDownloadJob: Job? = null

    val player: ExoPlayer by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()

        val okHttpDataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
            .setUserAgent("SetPlay-IPTV/3.0")

        val dataSourceFactory = DefaultDataSource.Factory(application, okHttpDataSourceFactory)
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

        val renderersFactory = DefaultRenderersFactory(application)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
            .setEnableDecoderFallback(true)

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 15_000,
                /* maxBufferMs = */ 50_000,
                /* bufferForPlaybackMs = */ 1_500,
                /* bufferForPlaybackAfterRebufferMs = */ 3_000
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        ExoPlayer.Builder(application, renderersFactory)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .setSeekBackIncrementMs(10000)
            .setSeekForwardIncrementMs(10000)
            .build().apply {
                playWhenReady = true
                addListener(playerListener)
            }
    }

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var progressTrackingJob: Job? = null
    private var controlsFadeJob: Job? = null
    private var gestureFadeJob: Job? = null
    private var sleepTimerJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
        }

        override fun onAudioSessionIdChanged(audioSessionId: Int) {
            setupAudioBoostEnhancer(audioSessionId)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    _uiState.value = _uiState.value.copy(isBuffering = true, error = null)
                }
                Player.STATE_READY -> {
                    val duration = if (player.duration > 0 && player.duration != C.TIME_UNSET) player.duration else 0L
                    _uiState.value = _uiState.value.copy(
                        isBuffering = false,
                        durationMs = duration,
                        error = null
                    )
                    extractTracks(player.currentTracks)
                    if (player.audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
                        setupAudioBoostEnhancer(player.audioSessionId)
                    }
                }
                Player.STATE_ENDED -> {
                    _uiState.value = _uiState.value.copy(isPlaying = false, isBuffering = false)
                }
                Player.STATE_IDLE -> {
                    _uiState.value = _uiState.value.copy(isBuffering = false)
                }
            }
        }

        override fun onVideoSizeChanged(videoSize: VideoSize) {
            val width = videoSize.width
            val height = videoSize.height
            val is4k = width >= 3840 || height >= 2160
            val label = when {
                width == 0 || height == 0 -> ""
                is4k -> "4K UHD"
                width >= 1920 || height >= 1080 -> "1080p FHD"
                width >= 1280 || height >= 720 -> "720p HD"
                else -> "${height}p SD"
            }
            _uiState.value = _uiState.value.copy(
                resolutionLabel = label,
                is4K = is4k
            )
        }

        override fun onTracksChanged(tracks: Tracks) {
            extractTracks(tracks)
        }

        override fun onPlayerError(error: PlaybackException) {
            _uiState.value = _uiState.value.copy(
                isBuffering = false,
                error = "Stream interrupted: ${error.localizedMessage ?: "Tap Retry to reconnect"}"
            )
        }
    }

    init {
        syncInitialSystemVolume()
        startProgressTracking()
    }

    private fun syncInitialSystemVolume() {
        try {
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
            val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val pct = ((currentVol.toFloat() / maxVol) * 100f).roundToInt().coerceIn(0, 100)
            volumeFloatAccumulator = pct.toFloat()
            _uiState.value = _uiState.value.copy(volumePercent = pct)
        } catch (_: Exception) {}
    }

    private fun setupAudioBoostEnhancer(audioSessionId: Int) {
        if (audioSessionId == C.AUDIO_SESSION_ID_UNSET || audioSessionId <= 0) return
        try {
            loudnessEnhancer?.release()
            loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                val boostPercent = (_uiState.value.volumePercent - 100).coerceAtLeast(0)
                if (boostPercent > 0) {
                    setTargetGain(boostPercent * 12) // up to +1200 mB (+12 dB) at 200%
                    enabled = true
                } else {
                    setTargetGain(0)
                    enabled = false
                }
            }
        } catch (_: Exception) {}
    }

    fun playStream(
        stream: StreamEntity,
        playlist: PlaylistEntity,
        catchupStartEpoch: Long? = null,
        catchupDurationMin: Int? = null
    ) {
        activePlaylistCache = playlist
        val previous = _uiState.value.currentStream?.takeIf { it.id != stream.id } ?: _uiState.value.previousStream

        val streamUrl = if (catchupStartEpoch != null && catchupDurationMin != null) {
            XtreamClient.buildCatchupUrl(
                serverUrl = playlist.serverUrl,
                username = playlist.username,
                password = playlist.password,
                streamId = stream.streamId,
                startEpochSeconds = catchupStartEpoch,
                durationMinutes = catchupDurationMin
            )
        } else {
            XtreamClient.buildStreamUrl(
                serverUrl = playlist.serverUrl,
                username = playlist.username,
                password = playlist.password,
                streamType = stream.streamType,
                streamId = stream.streamId,
                containerExtension = stream.containerExtension
            )
        }

        val isLiveStream = stream.streamType.equals("live", ignoreCase = true) && catchupStartEpoch == null
        val isSeriesStream = stream.streamType.equals("series", ignoreCase = true)

        // Resolve per-episode resume timestamp for Series using stream.streamId (episodeId),
        // or movie resume timestamp from stream.lastPositionMs
        val resolvedResumePositionMs = when {
            isLiveStream -> 0L
            isSeriesStream -> {
                val savedEpPos = repository.getEpisodePositionMs(playlist.id, stream.streamId)
                if (savedEpPos > 0L) {
                    savedEpPos
                } else if (!stream.epgChannelId.contains(":")) {
                    stream.lastPositionMs
                } else {
                    0L
                }
            }
            else -> stream.lastPositionMs
        }
        val resolvedDurationMs = if (isSeriesStream) {
            repository.getEpisodeDurationMs(playlist.id, stream.streamId)
        } else {
            0L
        }
        val effectiveStream = stream.copy(lastPositionMs = resolvedResumePositionMs)

        // Reset playback speed to 1.0x when switching to a Live TV stream
        if (isLiveStream && _uiState.value.playbackSpeed != 1.0f) {
            player.playbackParameters = PlaybackParameters(1.0f)
        }

        val mediaItemBuilder = MediaItem.Builder()
            .setUri(Uri.parse(streamUrl))
            .setMediaId("${stream.id}_${stream.streamId}")

        if (isLiveStream) {
            val targetOffsetMs = when (_uiState.value.bufferProfile) {
                BufferProfile.FAST_ZAP -> 1_500L
                BufferProfile.BALANCED -> 5_000L
                BufferProfile.STABLE_4K -> 15_000L
            }
            mediaItemBuilder.setLiveConfiguration(
                MediaItem.LiveConfiguration.Builder()
                    .setTargetOffsetMs(targetOffsetMs)
                    .setMinPlaybackSpeed(0.97f)
                    .setMaxPlaybackSpeed(1.03f)
                    .build()
            )
        }

        _uiState.value = _uiState.value.copy(
            currentStream = effectiveStream,
            previousStream = previous,
            isLive = isLiveStream,
            currentPositionMs = if (!isLiveStream && resolvedResumePositionMs > 5000L) resolvedResumePositionMs else 0L,
            durationMs = resolvedDurationMs,
            playbackSpeed = if (isLiveStream) 1.0f else _uiState.value.playbackSpeed,
            error = null,
            isBuffering = true,
            resolutionLabel = "",
            videoCodecLabel = "",
            audioCodecLabel = "",
            fpsLabel = "",
            currentProgram = null,
            videoZoomScale = 1.0f,
            videoPanOffsetX = 0f,
            videoPanOffsetY = 0f,
            wyzieLanguageGroups = emptyList(),
            selectedWyzieLanguageCode = null,
            activeWyzieOption = null,
            activeWyzieCues = emptyList(),
            currentSubtitleText = null,
            isSearchingOnlineSubtitles = false,
            isDownloadingSubtitle = false,
            onlineSubtitleStatusMessage = null,
            resolvedMediaQuery = null,
            subtitleSyncOffsetMs = 0L
        )

        player.setMediaItem(mediaItemBuilder.build())
        player.prepare()
        if (!isLiveStream && resolvedResumePositionMs > 5000L) {
            player.seekTo(resolvedResumePositionMs)
        }
        player.play()

        // Update watch history, sibling channels for fast zapping, or Wyzie VOD subtitles (no EPG network overhead)
        viewModelScope.launch {
            if (stream.id > 0L) {
                repository.updateWatchHistory(stream.id)
            }
            if (stream.streamType.equals("live", ignoreCase = true)) {
                val siblings = repository.getSiblingLiveChannels(playlist.id, stream.categoryId)
                val catchupList = if (stream.tvArchive == 1) {
                    repository.getCatchupEpg(playlist.id, stream.streamId)
                } else {
                    emptyList()
                }
                _uiState.value = _uiState.value.copy(
                    siblingChannels = siblings,
                    catchupPrograms = catchupList
                )
            } else {
                // Pre-resolve and fetch Wyzie online subtitles for Movies & Series
                searchOnlineSubtitles()
            }
        }

        scheduleControlsFade()
    }

    /**
     * Completely stops playback and clears `currentStream` so closing the floating PiP window
     * or playback bar immediately dismisses the player UI.
     */
    fun stopAndClosePlayer() {
        sleepTimerJob?.cancel()
        controlsFadeJob?.cancel()
        gestureFadeJob?.cancel()
        subtitleSearchJob?.cancel()
        subtitleDownloadJob?.cancel()
        try {
            loudnessEnhancer?.enabled = false
        } catch (_: Exception) {}
        player.stop()
        player.clearMediaItems()
        _uiState.value = _uiState.value.copy(
            currentStream = null,
            isPlaying = false,
            isBuffering = false,
            error = null,
            isChannelZapperOpen = false,
            isTouchLocked = false,
            videoZoomScale = 1.0f,
            videoPanOffsetX = 0f,
            videoPanOffsetY = 0f,
            sleepTimerRemainingMinutes = null,
            wyzieLanguageGroups = emptyList(),
            selectedWyzieLanguageCode = null,
            activeWyzieOption = null,
            activeWyzieCues = emptyList(),
            currentSubtitleText = null,
            isSearchingOnlineSubtitles = false,
            isDownloadingSubtitle = false,
            onlineSubtitleStatusMessage = null
        )
    }

    fun retryCurrentStream() {
        val stream = _uiState.value.currentStream ?: return
        val playlist = activePlaylistCache ?: return
        playStream(stream, playlist)
    }

    fun recallPreviousChannel() {
        val prev = _uiState.value.previousStream ?: return
        val playlist = activePlaylistCache ?: return
        playStream(prev, playlist)
    }

    fun zapChannelOffset(offset: Int) {
        val current = _uiState.value.currentStream ?: return
        val siblings = _uiState.value.siblingChannels
        val playlist = activePlaylistCache ?: return
        if (siblings.isEmpty()) return

        val currentIndex = siblings.indexOfFirst { it.id == current.id }
        val nextIndex = if (currentIndex == -1) {
            0
        } else {
            (currentIndex + offset + siblings.size) % siblings.size
        }
        playStream(siblings[nextIndex], playlist)
    }

    fun setBufferProfile(profile: BufferProfile) {
        _uiState.value = _uiState.value.copy(bufferProfile = profile)
        if (_uiState.value.isLive) {
            retryCurrentStream()
        }
    }

    fun toggleTouchLock() {
        val locked = !_uiState.value.isTouchLocked
        _uiState.value = _uiState.value.copy(
            isTouchLocked = locked,
            isControlsVisible = true
        )
        scheduleControlsFade()
    }

    fun toggleChannelZapper(open: Boolean? = null) {
        val nextState = open ?: !_uiState.value.isChannelZapperOpen
        _uiState.value = _uiState.value.copy(isChannelZapperOpen = nextState)
    }

    fun toggleScreenOrientation(activity: Activity?) {
        val nextLandscape = !_uiState.value.isLandscapeLocked
        _uiState.value = _uiState.value.copy(isLandscapeLocked = nextLandscape)
        activity?.requestedOrientation = if (nextLandscape) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        showControls()
    }

    fun resetScreenOrientationToPortrait(activity: Activity?) {
        if (_uiState.value.isLandscapeLocked) {
            _uiState.value = _uiState.value.copy(isLandscapeLocked = false)
        }
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    fun onTransformGesture(zoomChange: Float, panX: Float, panY: Float) {
        if (_uiState.value.isTouchLocked) return
        val currentZoom = _uiState.value.videoZoomScale
        val newZoom = (currentZoom * zoomChange).coerceIn(1.0f, 3.0f)
        if (newZoom <= 1.01f) {
            _uiState.value = _uiState.value.copy(
                videoZoomScale = 1.0f,
                videoPanOffsetX = 0f,
                videoPanOffsetY = 0f
            )
        } else {
            val maxPan = (newZoom - 1.0f) * 420f
            val nextX = (_uiState.value.videoPanOffsetX + panX).coerceIn(-maxPan, maxPan)
            val nextY = (_uiState.value.videoPanOffsetY + panY).coerceIn(-maxPan, maxPan)
            _uiState.value = _uiState.value.copy(
                videoZoomScale = newZoom,
                videoPanOffsetX = nextX,
                videoPanOffsetY = nextY
            )
        }
    }

    fun resetZoomAndPan() {
        _uiState.value = _uiState.value.copy(
            videoZoomScale = 1.0f,
            videoPanOffsetX = 0f,
            videoPanOffsetY = 0f
        )
    }

    fun setSleepTimer(minutes: Int?) {
        sleepTimerJob?.cancel()
        _uiState.value = _uiState.value.copy(sleepTimerRemainingMinutes = minutes)
        if (minutes != null && minutes > 0) {
            sleepTimerJob = viewModelScope.launch {
                var remaining = minutes
                while (remaining > 0 && isActive) {
                    delay(60_000L)
                    remaining--
                    _uiState.value = _uiState.value.copy(sleepTimerRemainingMinutes = remaining)
                }
                if (remaining <= 0) {
                    player.pause()
                    _uiState.value = _uiState.value.copy(sleepTimerRemainingMinutes = null)
                }
            }
        }
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
        showControls()
    }

    fun seekRelative(seconds: Long) {
        if (_uiState.value.isLive) return
        val target = (player.currentPosition + seconds * 1000).coerceAtLeast(0)
        player.seekTo(target)
        showControls()
    }

    fun seekToPosition(positionMs: Long) {
        val clamped = positionMs.coerceAtLeast(0L)
        player.seekTo(clamped)
        val state = _uiState.value
        _uiState.value = state.copy(currentPositionMs = clamped)
        if (!state.isLive) {
            state.currentStream?.let { s ->
                val dur = state.durationMs
                if (s.streamType.equals("series", ignoreCase = true)) {
                    repository.saveEpisodePositionMs(s.playlistId, s.streamId, clamped, dur)
                }
                viewModelScope.launch {
                    if (s.id > 0L) {
                        repository.updatePlaybackPosition(s.id, clamped)
                    }
                }
            }
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        if (_uiState.value.isLive) return
        player.playbackParameters = PlaybackParameters(speed)
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }

    fun toggleAspectRatio() {
        val nextMode = when (_uiState.value.aspectRatioMode) {
            AspectRatioMode.FIT -> AspectRatioMode.FILL
            AspectRatioMode.FILL -> AspectRatioMode.RATIO_16_9
            AspectRatioMode.RATIO_16_9 -> AspectRatioMode.ZOOM
            AspectRatioMode.ZOOM -> AspectRatioMode.FIT
        }
        _uiState.value = _uiState.value.copy(aspectRatioMode = nextMode)
    }

    fun selectAudioTrack(track: TrackItem) {
        val trackGroup = player.currentTracks.groups.getOrNull(track.groupIndex) ?: return
        player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
            .setOverrideForType(
                TrackSelectionOverride(trackGroup.mediaTrackGroup, track.trackIndex)
            )
            .build()
    }

    fun selectSubtitleTrack(track: TrackItem?) {
        // When selecting an embedded track or turning all subtitles off, clear active online Wyzie cues
        _uiState.value = _uiState.value.copy(
            activeWyzieOption = null,
            activeWyzieCues = emptyList(),
            currentSubtitleText = null
        )
        val paramsBuilder = player.trackSelectionParameters.buildUpon()
        if (track == null) {
            paramsBuilder.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
        } else {
            val trackGroup = player.currentTracks.groups.getOrNull(track.groupIndex) ?: return
            paramsBuilder.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                .setOverrideForType(
                    TrackSelectionOverride(trackGroup.mediaTrackGroup, track.trackIndex)
                )
        }
        player.trackSelectionParameters = paramsBuilder.build()
    }

    /**
     * Searches Wyzie Subs API for the current Movie or Series stream (or a custom title / IMDb ID / S/E).
     */
    fun searchOnlineSubtitles(
        customQuery: String? = null,
        season: Int? = null,
        episode: Int? = null
    ) {
        val stream = _uiState.value.currentStream ?: return
        if (stream.streamType.equals("live", ignoreCase = true)) return

        subtitleSearchJob?.cancel()
        subtitleSearchJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSearchingOnlineSubtitles = true,
                onlineSubtitleStatusMessage = null
            )

            val response = wyzieSubtitleService.fetchSubtitlesForStream(
                stream = stream,
                playlist = activePlaylistCache,
                customSearchQuery = customQuery,
                overrideSeason = season,
                overrideEpisode = episode
            )

            val currentSelectedLang = _uiState.value.selectedWyzieLanguageCode
            val resolvedSelectedLang = when {
                currentSelectedLang != null && response.languageGroups.any { it.languageCode == currentSelectedLang } -> {
                    currentSelectedLang
                }
                else -> response.languageGroups.firstOrNull()?.languageCode
            }

            _uiState.value = _uiState.value.copy(
                isSearchingOnlineSubtitles = false,
                resolvedMediaQuery = response.resolvedQuery,
                wyzieLanguageGroups = response.languageGroups,
                selectedWyzieLanguageCode = resolvedSelectedLang,
                onlineSubtitleStatusMessage = response.statusMessage
            )
        }
    }

    /**
     * Selects a language in the Wyzie Subtitle Studio to display all subtitle options for that language.
     */
    fun selectOnlineSubtitleLanguage(languageCode: String?) {
        _uiState.value = _uiState.value.copy(selectedWyzieLanguageCode = languageCode)
    }

    /**
     * Downloads and activates the chosen [WyzieSubtitleOption] so playback immediately displays subtitles.
     */
    fun applyOnlineSubtitleOption(option: WyzieSubtitleOption, onReady: () -> Unit = {}) {
        subtitleDownloadJob?.cancel()
        subtitleDownloadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDownloadingSubtitle = true,
                onlineSubtitleStatusMessage = null
            )

            val result = wyzieSubtitleService.downloadAndParseSubtitle(option)
            result.fold(
                onSuccess = { cues ->
                    // Disable embedded ExoPlayer text track so it doesn't overlap with Wyzie subtitles
                    try {
                        player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                            .build()
                    } catch (_: Exception) {}

                    val currentPos = player.currentPosition + _uiState.value.subtitleSyncOffsetMs
                    val immediateCue = findCueAtTime(cues, currentPos)
                    _uiState.value = _uiState.value.copy(
                        isDownloadingSubtitle = false,
                        activeWyzieOption = option,
                        activeWyzieCues = cues,
                        currentSubtitleText = immediateCue,
                        onlineSubtitleStatusMessage = null
                    )
                    onReady()
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        isDownloadingSubtitle = false,
                        onlineSubtitleStatusMessage = err.localizedMessage ?: "Failed to load subtitle option"
                    )
                }
            )
        }
    }

    fun clearOnlineSubtitles() {
        _uiState.value = _uiState.value.copy(
            activeWyzieOption = null,
            activeWyzieCues = emptyList(),
            currentSubtitleText = null
        )
    }

    fun adjustSubtitleSyncOffset(deltaMs: Long) {
        val nextOffset = (_uiState.value.subtitleSyncOffsetMs + deltaMs).coerceIn(-30_000L, 30_000L)
        val cues = _uiState.value.activeWyzieCues
        val updatedText = if (cues.isNotEmpty()) {
            findCueAtTime(cues, player.currentPosition + nextOffset)
        } else {
            null
        }
        _uiState.value = _uiState.value.copy(
            subtitleSyncOffsetMs = nextOffset,
            currentSubtitleText = updatedText
        )
    }

    fun resetSubtitleSyncOffset() {
        val cues = _uiState.value.activeWyzieCues
        val updatedText = if (cues.isNotEmpty()) {
            findCueAtTime(cues, player.currentPosition)
        } else {
            null
        }
        _uiState.value = _uiState.value.copy(
            subtitleSyncOffsetMs = 0L,
            currentSubtitleText = updatedText
        )
    }

    fun setSubtitleScaleFactor(scale: Float) {
        _uiState.value = _uiState.value.copy(subtitleScaleFactor = scale.coerceIn(0.8f, 1.5f))
    }

    /**
     * Direct setter for Volume & VLC 200% Audio Boost (0..200%).
     */
    fun setVolumePercent(targetPercent: Int, showHudBadge: Boolean = true) {
        if (_uiState.value.isTouchLocked) return
        val clamped = targetPercent.coerceIn(0, 200)
        volumeFloatAccumulator = clamped.toFloat()

        try {
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
            val systemRatio = (clamped.coerceAtMost(100) / 100f)
            val newSystemVol = (systemRatio * maxVol).roundToInt().coerceIn(0, maxVol)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newSystemVol, 0)
        } catch (_: Exception) {}

        // Apply VLC-style Audio Boost for 101%..200%
        val boostPercent = (clamped - 100).coerceAtLeast(0)
        try {
            if (loudnessEnhancer == null && player.audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
                setupAudioBoostEnhancer(player.audioSessionId)
            }
            loudnessEnhancer?.let { enhancer ->
                if (boostPercent > 0) {
                    enhancer.setTargetGain(boostPercent * 12) // +1200 mB max
                    enhancer.enabled = true
                } else {
                    enhancer.setTargetGain(0)
                    enhancer.enabled = false
                }
            }
        } catch (_: Exception) {}

        _uiState.value = _uiState.value.copy(
            volumePercent = clamped,
            gestureVolume = if (showHudBadge) clamped else _uiState.value.gestureVolume,
            gestureBrightness = if (showHudBadge) null else _uiState.value.gestureBrightness,
            gestureSeekSeconds = if (showHudBadge) null else _uiState.value.gestureSeekSeconds
        )
        if (showHudBadge) {
            scheduleGestureFade()
        }
    }

    /**
     * Direct setter for Screen Brightness (1..100%).
     */
    fun setBrightnessPercent(targetPercent: Int, activity: Activity?, showHudBadge: Boolean = true) {
        if (_uiState.value.isTouchLocked) return
        val clamped = targetPercent.coerceIn(1, 100)
        brightnessFloatAccumulator = clamped.toFloat()
        val normalized = clamped / 100f

        activity?.window?.let { window ->
            val attributes = window.attributes
            attributes.screenBrightness = normalized
            window.attributes = attributes
        }

        _uiState.value = _uiState.value.copy(
            brightnessPercent = clamped,
            gestureBrightness = if (showHudBadge) normalized else _uiState.value.gestureBrightness,
            gestureVolume = if (showHudBadge) null else _uiState.value.gestureVolume,
            gestureSeekSeconds = if (showHudBadge) null else _uiState.value.gestureSeekSeconds
        )
        if (showHudBadge) {
            scheduleGestureFade()
        }
    }

    // Smooth vertical swipe gesture handlers
    fun onBrightnessSwipe(deltaNormalized: Float, activity: Activity?) {
        if (_uiState.value.isTouchLocked) return
        brightnessFloatAccumulator = (brightnessFloatAccumulator + deltaNormalized * 120f).coerceIn(1f, 100f)
        setBrightnessPercent(brightnessFloatAccumulator.roundToInt(), activity, showHudBadge = true)
    }

    fun onVolumeSwipeRatio(deltaNormalized: Float) {
        if (_uiState.value.isTouchLocked) return
        volumeFloatAccumulator = (volumeFloatAccumulator + deltaNormalized * 160f).coerceIn(0f, 200f)
        setVolumePercent(volumeFloatAccumulator.roundToInt(), showHudBadge = true)
    }

    fun onVolumeSwipe(deltaSteps: Int) {
        if (_uiState.value.isTouchLocked) return
        val next = (_uiState.value.volumePercent + deltaSteps * 5).coerceIn(0, 200)
        setVolumePercent(next, showHudBadge = true)
    }

    fun onScrubSwipe(deltaSeconds: Long) {
        if (_uiState.value.isTouchLocked || _uiState.value.isLive) return
        val current = _uiState.value.gestureSeekSeconds ?: 0L
        val updated = current + deltaSeconds
        _uiState.value = _uiState.value.copy(gestureSeekSeconds = updated, gestureBrightness = null, gestureVolume = null)
        scheduleGestureFade()
    }

    fun onScrubComplete() {
        if (_uiState.value.isTouchLocked || _uiState.value.isLive) return
        val seekSec = _uiState.value.gestureSeekSeconds ?: return
        seekRelative(seekSec)
        _uiState.value = _uiState.value.copy(gestureSeekSeconds = null)
    }

    fun showControls() {
        _uiState.value = _uiState.value.copy(isControlsVisible = true)
        scheduleControlsFade()
    }

    fun toggleControls() {
        if (_uiState.value.isControlsVisible) {
            _uiState.value = _uiState.value.copy(isControlsVisible = false)
        } else {
            showControls()
        }
    }

    private fun scheduleControlsFade() {
        controlsFadeJob?.cancel()
        controlsFadeJob = viewModelScope.launch {
            delay(4500)
            if (_uiState.value.isPlaying && !_uiState.value.isChannelZapperOpen) {
                _uiState.value = _uiState.value.copy(isControlsVisible = false)
            }
        }
    }

    private fun scheduleGestureFade() {
        gestureFadeJob?.cancel()
        gestureFadeJob = viewModelScope.launch {
            delay(1300)
            _uiState.value = _uiState.value.copy(
                gestureBrightness = null,
                gestureVolume = null,
                gestureSeekSeconds = null
            )
        }
    }

    private fun extractTracks(tracks: Tracks) {
        val audioList = mutableListOf<TrackItem>()
        val subtitleList = mutableListOf<TrackItem>()
        var vCodec = ""
        var aCodec = ""
        var fps = ""

        for (gIndex in 0 until tracks.groups.size) {
            val group = tracks.groups[gIndex]
            if (group.type == C.TRACK_TYPE_VIDEO) {
                for (tIndex in 0 until group.length) {
                    if (group.isTrackSelected(tIndex)) {
                        val format = group.getTrackFormat(tIndex)
                        vCodec = formatMimeToCodecBadge(format.sampleMimeType)
                        if (format.frameRate > 0) {
                            fps = "${format.frameRate.toInt()} FPS"
                        }
                    }
                }
            } else if (group.type == C.TRACK_TYPE_AUDIO) {
                for (tIndex in 0 until group.length) {
                    val format = group.getTrackFormat(tIndex)
                    val lang = format.language ?: "Default"
                    val codecTag = formatMimeToCodecBadge(format.sampleMimeType)
                    if (group.isTrackSelected(tIndex)) {
                        aCodec = codecTag
                    }
                    val channelsLabel = if (format.channelCount >= 6) "5.1ch" else if (format.channelCount == 2) "Stereo" else ""
                    val label = buildString {
                        append(format.label ?: "Audio ${audioList.size + 1}")
                        append(" ($lang")
                        if (codecTag.isNotEmpty()) append(" • $codecTag")
                        if (channelsLabel.isNotEmpty()) append(" $channelsLabel")
                        append(")")
                    }
                    audioList.add(
                        TrackItem(
                            id = format.id ?: "$gIndex-$tIndex",
                            name = label,
                            language = lang,
                            isSelected = group.isTrackSelected(tIndex),
                            groupIndex = gIndex,
                            trackIndex = tIndex
                        )
                    )
                }
            } else if (group.type == C.TRACK_TYPE_TEXT) {
                for (tIndex in 0 until group.length) {
                    val format = group.getTrackFormat(tIndex)
                    val lang = format.language ?: "Sub"
                    val label = format.label ?: "Subtitle ${subtitleList.size + 1} ($lang)"
                    subtitleList.add(
                        TrackItem(
                            id = format.id ?: "$gIndex-$tIndex",
                            name = label,
                            language = lang,
                            isSelected = group.isTrackSelected(tIndex),
                            groupIndex = gIndex,
                            trackIndex = tIndex
                        )
                    )
                }
            }
        }

        _uiState.value = _uiState.value.copy(
            audioTracks = audioList,
            subtitleTracks = subtitleList,
            videoCodecLabel = vCodec,
            audioCodecLabel = aCodec,
            fpsLabel = fps
        )
    }

    internal fun formatMimeToCodecBadge(mime: String?): String {
        if (mime == null) return ""
        return when {
            mime.contains("hevc", true) || mime.contains("h265", true) -> "HEVC"
            mime.contains("avc", true) || mime.contains("h264", true) -> "H.264"
            mime.contains("av01", true) -> "AV1"
            mime.contains("vp9", true) -> "VP9"
            mime.contains("eac3", true) -> "EAC3+"
            mime.contains("ac-4", true) || mime.contains("ac4", true) -> "AC4"
            mime.contains("ac3", true) -> "AC3"
            mime.contains("dts", true) -> "DTS"
            mime.contains("aac", true) || mime.contains("mp4a", true) -> "AAC"
            mime.contains("mp3", true) -> "MP3"
            mime.contains("opus", true) -> "OPUS"
            else -> mime.substringAfterLast("/").uppercase()
        }
    }

    private fun findCueAtTime(cues: List<SubtitleCue>, positionMs: Long): String? {
        if (cues.isEmpty() || positionMs < 0L) return null
        var low = 0
        var high = cues.lastIndex
        while (low <= high) {
            val mid = (low + high) ushr 1
            val cue = cues[mid]
            when {
                positionMs < cue.startTimeMs -> high = mid - 1
                positionMs > cue.endTimeMs -> low = mid + 1
                else -> return cue.text
            }
        }
        return null
    }

    private fun startProgressTracking() {
        progressTrackingJob?.cancel()
        progressTrackingJob = viewModelScope.launch {
            var tickCount = 0
            while (isActive) {
                if (player.isPlaying) {
                    val pos = player.currentPosition
                    val dur = if (player.duration > 0 && player.duration != C.TIME_UNSET) player.duration else _uiState.value.durationMs
                    val state = _uiState.value
                    val activeCueText = if (state.activeWyzieCues.isNotEmpty() && !state.isLive) {
                        findCueAtTime(state.activeWyzieCues, pos + state.subtitleSyncOffsetMs)
                    } else {
                        null
                    }
                    if (state.currentPositionMs != pos || state.durationMs != dur || state.currentSubtitleText != activeCueText) {
                        _uiState.value = state.copy(
                            currentPositionMs = pos,
                            durationMs = dur,
                            currentSubtitleText = activeCueText
                        )
                    }
                    tickCount++
                    if (tickCount % 20 == 0 && !state.isLive) {
                        state.currentStream?.let { s ->
                            if (s.streamType.equals("series", ignoreCase = true)) {
                                repository.saveEpisodePositionMs(s.playlistId, s.streamId, pos, dur)
                            }
                            if (s.id > 0L) {
                                repository.updatePlaybackPosition(s.id, pos)
                            }
                        }
                    }
                }
                delay(250)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        progressTrackingJob?.cancel()
        controlsFadeJob?.cancel()
        gestureFadeJob?.cancel()
        sleepTimerJob?.cancel()
        subtitleSearchJob?.cancel()
        subtitleDownloadJob?.cancel()
        try {
            loudnessEnhancer?.release()
        } catch (_: Exception) {}
        player.removeListener(playerListener)
        player.release()
    }
}

