package com.example.ui.vod

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.StreamVaultDatabase
import com.example.data.model.CategoryEntity
import com.example.data.model.GroupedVodStream
import com.example.data.model.PlaylistEntity
import com.example.data.model.StreamEntity
import com.example.data.model.VodCatalogGroupEntity
import com.example.data.model.VodCollectionEntity
import com.example.data.model.VodGroupingNormalizer
import com.example.data.remote.SeriesDetails
import com.example.data.remote.VodMovieDetails
import com.example.data.repository.EpisodeResumeInfo
import com.example.data.repository.IptvRepository
import com.example.data.repository.VodPipelinePhase
import com.example.data.repository.VodPipelineState
import com.example.data.sync.CatalogSyncScheduler
import com.example.data.sync.CatalogSyncWorker
import com.example.data.sync.SyncStage
import com.example.data.sync.SyncStatus
import com.example.data.sync.SyncTelemetry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

enum class VodTab {
    MOVIES,
    SERIES
}

enum class CollectionSortMode(val label: String) {
    RELEASE_ORDER("Release Order"),
    NEWEST_FIRST("Newest First"),
    TOP_RATED("Top Rated")
}

enum class VodCategoryBucket(val title: String) {
    YOUR_LANGUAGES("Your Languages"),
    STREAMING_PLATFORMS("Streaming Platforms"),
    GENRES_COLLECTIONS("Genres & Collections"),
    OTHER_REGIONS("Other Regions")
}

data class CategorizedVodItem(
    val category: CategoryEntity,
    val languageTag: String, // e.g. "EN", "AR", "FR", "ES", or ""
    val cleanName: String,   // e.g. "Netflix", "Amazon Prime", "Action"
    val itemCount: Int,
    val bucket: VodCategoryBucket
)

sealed class VodShelfFilter {
    data object RecentlyAdded : VodShelfFilter()
    data object TopRated : VodShelfFilter()
    data object Recommended : VodShelfFilter()
    data class Genre(val keyword: String) : VodShelfFilter()
    data class Platform(val keyword: String) : VodShelfFilter()
}

data class VodDiscoveryShelf(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val items: List<GroupedVodStream>,
    val shelfFilter: VodShelfFilter = VodShelfFilter.RecentlyAdded,
    val canLoadMore: Boolean = false
)

data class VodSyncBannerState(
    val isSyncing: Boolean = false,
    val statusText: String = "",
    val isError: Boolean = false,
    val isCompleted: Boolean = false,
    val progressPercent: Int = 0,
    val stageLabel: String = "",
    val statsSubtitle: String = "",
    val isIndeterminate: Boolean = false,
    val isInterrupted: Boolean = false,
    val pendingUpdateAvailable: Boolean = false,
    val pendingUpdateSummary: String = "",
    val lastSuccessfulSyncMs: Long = 0L
)

internal fun formatSyncStatsSubtitle(telemetry: SyncTelemetry): String {
    val checked = maxOf(telemetry.itemsDownloaded, telemetry.itemsProcessed)
    return when {
        telemetry.status == SyncStatus.INTERRUPTED -> {
            if (checked > 0) {
                "Paused after %,d checked • Tap Resume".format(Locale.US, checked)
            } else {
                "Sync paused mid-stream • Tap Resume"
            }
        }
        telemetry.status == SyncStatus.FAILED -> {
            telemetry.errorMessage.ifBlank { "Check your connection and retry" }
        }
        telemetry.status == SyncStatus.PENDING -> {
            if (telemetry.pendingUpdateSummary.contains("waiting", ignoreCase = true)) {
                "Single-queue sync • Starts automatically"
            } else {
                "Queued for background sync…"
            }
        }
        telemetry.status == SyncStatus.RUNNING -> {
            when (telemetry.stage) {
                SyncStage.CONNECTING_AND_CHECKING -> {
                    if (telemetry.totalDiscovered > 0) {
                        "%,d items discovered".format(Locale.US, telemetry.totalDiscovered)
                    } else {
                        "Checking catalog changes…"
                    }
                }
                SyncStage.DOWNLOADING_AND_DIFFING, SyncStage.PARSING_AND_VALIDATING -> {
                    if (checked > 0) {
                        val base = "%,d checked • %,d new • %,d updated".format(
                            Locale.US,
                            checked,
                            telemetry.itemsAdded,
                            telemetry.itemsUpdated
                        )
                        if (telemetry.itemsRemoved > 0) {
                            "$base • %,d removed".format(Locale.US, telemetry.itemsRemoved)
                        } else {
                            base
                        }
                    } else {
                        "Streaming catalog changes…"
                    }
                }
                SyncStage.GROUPING_AND_ORGANIZING -> {
                    if (telemetry.totalDiscovered > 0) {
                        "%,d / %,d grouped".format(
                            Locale.US,
                            telemetry.itemsGrouped.coerceAtMost(telemetry.totalDiscovered),
                            telemetry.totalDiscovered
                        )
                    } else if (telemetry.itemsGrouped > 0) {
                        "%,d titles grouped".format(Locale.US, telemetry.itemsGrouped)
                    } else {
                        "Grouping & ranking versions…"
                    }
                }
                SyncStage.PERSISTING_DATABASE -> {
                    if (telemetry.itemsPersisted > 0) {
                        "%,d changes saved".format(Locale.US, telemetry.itemsPersisted)
                    } else if (telemetry.itemsAdded > 0 || telemetry.itemsUpdated > 0) {
                        "%,d new • %,d updated".format(Locale.US, telemetry.itemsAdded, telemetry.itemsUpdated)
                    } else {
                        "Committing changes…"
                    }
                }
                SyncStage.FINALIZING -> {
                    if (telemetry.totalDiscovered > 0) {
                        "%,d total • %,d new • %,d updated".format(
                            Locale.US,
                            telemetry.totalDiscovered,
                            telemetry.itemsAdded,
                            telemetry.itemsUpdated
                        )
                    } else {
                        "Finalizing catalog…"
                    }
                }
                SyncStage.IDLE -> ""
            }
        }
        telemetry.status == SyncStatus.COMPLETED -> {
            when {
                telemetry.itemsAdded > 0 || telemetry.itemsUpdated > 0 || telemetry.itemsRemoved > 0 -> {
                    val base = "%,d new • %,d updated".format(Locale.US, telemetry.itemsAdded, telemetry.itemsUpdated)
                    if (telemetry.itemsRemoved > 0) {
                        "$base • %,d removed".format(Locale.US, telemetry.itemsRemoved)
                    } else {
                        base
                    }
                }
                telemetry.totalDiscovered > 0 -> "%,d items checked • Up to date".format(Locale.US, telemetry.totalDiscovered)
                else -> ""
            }
        }
        else -> ""
    }
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class VodViewModel(application: Application) : AndroidViewModel(application) {
    val repository = IptvRepository.getInstance(application)

    private val indexedTriggerGuard = ConcurrentHashMap.newKeySet<String>()
    private val autoSectionRecoveryGuard = ConcurrentHashMap.newKeySet<String>()

    // Progressive paging limit (starts at 180, resets on category/query change, increments by +180 on scroll)
    val pageSize: Int = PAGE_SIZE
    private val _currentLimit = MutableStateFlow(PAGE_SIZE)
    val currentLimit: StateFlow<Int> = _currentLimit.asStateFlow()
    val limit: StateFlow<Int> = _currentLimit.asStateFlow()

    // Recently watched streams for Continue Watching rail
    val recentlyWatched: StateFlow<List<StreamEntity>> = repository.activePlaylist
        .flatMapLatest { playlist ->
            if (playlist == null) flowOf(emptyList()) else repository.getRecentlyWatched(playlist.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    // Reactive per-episode resume map keyed by "${playlistId}_ep_${episodeId}"
    val episodeResumeMap: StateFlow<Map<String, EpisodeResumeInfo>> = repository.episodeResumeMap

    // Independent Movies State
    private val _selectedMovieCategoryId = MutableStateFlow<String?>(null)
    val selectedMovieCategoryId: StateFlow<String?> = _selectedMovieCategoryId.asStateFlow()

    private val _movieSearchQuery = MutableStateFlow("")
    val movieSearchQuery: StateFlow<String> = _movieSearchQuery.asStateFlow()

    val movieCategories: StateFlow<List<CategoryEntity>> = repository.activePlaylist
        .flatMapLatest { playlist ->
            if (playlist == null) flowOf(emptyList()) else repository.getCategories(playlist.id, "MOVIE")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    val movieCategoryCounts: StateFlow<Map<String, Int>> = repository.activePlaylist
        .flatMapLatest { playlist ->
            if (playlist == null) flowOf(emptyMap()) else repository.getCategoryStreamCounts(playlist.id, "movie")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyMap())

    val liveMovieCount: StateFlow<Int> = movieCategoryCounts
        .map { counts -> counts.values.sum() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), 0)

    val organizedMovieCategories: StateFlow<Map<VodCategoryBucket, List<CategorizedVodItem>>> = combine(
        movieCategories,
        movieCategoryCounts
    ) { categories, counts ->
        organizeVodCategories(categories, counts)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyMap())

    val movieItems: StateFlow<List<StreamEntity>> = combine(
        repository.activePlaylist,
        _selectedMovieCategoryId,
        _movieSearchQuery,
        _currentLimit
    ) { playlist, catId, query, limit ->
        VodQueryParams(playlist?.id, catId, query, limit)
    }.flatMapLatest { params ->
        if (params.playlistId == null) {
            flowOf(emptyList())
        } else {
            repository.queryStreamsFast(
                playlistId = params.playlistId,
                streamType = "movie",
                categoryId = params.categoryId,
                query = params.query,
                limit = params.limit
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    val groupedMovieItems: StateFlow<List<GroupedVodStream>> = combine(
        repository.activePlaylist,
        _selectedMovieCategoryId,
        _movieSearchQuery,
        _currentLimit
    ) { playlist, catId, query, limit ->
        VodQueryParams(playlist?.id, catId, query, limit)
    }.flatMapLatest { params ->
        if (params.playlistId == null) {
            flowOf(emptyList())
        } else {
            triggerLazyIndexIfNeeded(params.playlistId, "movie")
            repository.queryDeduplicatedVodStreams(
                playlistId = params.playlistId,
                streamType = "movie",
                categoryId = params.categoryId,
                query = params.query,
                limit = params.limit
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    // Independent Series State (100% architecture parity with Movies)
    private val _selectedSeriesCategoryId = MutableStateFlow<String?>(null)
    val selectedSeriesCategoryId: StateFlow<String?> = _selectedSeriesCategoryId.asStateFlow()

    private val _seriesSearchQuery = MutableStateFlow("")
    val seriesSearchQuery: StateFlow<String> = _seriesSearchQuery.asStateFlow()

    val seriesCategories: StateFlow<List<CategoryEntity>> = repository.activePlaylist
        .flatMapLatest { playlist ->
            if (playlist == null) flowOf(emptyList()) else repository.getCategories(playlist.id, "SERIES")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    val seriesCategoryCounts: StateFlow<Map<String, Int>> = repository.activePlaylist
        .flatMapLatest { playlist ->
            if (playlist == null) flowOf(emptyMap()) else repository.getCategoryStreamCounts(playlist.id, "series")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyMap())

    val liveSeriesCount: StateFlow<Int> = seriesCategoryCounts
        .map { counts -> counts.values.sum() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), 0)

    val organizedSeriesCategories: StateFlow<Map<VodCategoryBucket, List<CategorizedVodItem>>> = combine(
        seriesCategories,
        seriesCategoryCounts
    ) { categories, counts ->
        organizeVodCategories(categories, counts)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyMap())

    val seriesItems: StateFlow<List<StreamEntity>> = combine(
        repository.activePlaylist,
        _selectedSeriesCategoryId,
        _seriesSearchQuery,
        _currentLimit
    ) { playlist, catId, query, limit ->
        VodQueryParams(playlist?.id, catId, query, limit)
    }.flatMapLatest { params ->
        if (params.playlistId == null) {
            flowOf(emptyList())
        } else {
            repository.queryStreamsFast(
                playlistId = params.playlistId,
                streamType = "series",
                categoryId = params.categoryId,
                query = params.query,
                limit = params.limit
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    val groupedSeriesItems: StateFlow<List<GroupedVodStream>> = combine(
        repository.activePlaylist,
        _selectedSeriesCategoryId,
        _seriesSearchQuery,
        _currentLimit
    ) { playlist, catId, query, limit ->
        VodQueryParams(playlist?.id, catId, query, limit)
    }.flatMapLatest { params ->
        if (params.playlistId == null) {
            flowOf(emptyList())
        } else {
            triggerLazyIndexIfNeeded(params.playlistId, "series")
            repository.queryDeduplicatedVodStreams(
                playlistId = params.playlistId,
                streamType = "series",
                categoryId = params.categoryId,
                query = params.query,
                limit = params.limit
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    // Unlimited Shelf "See All" Filter & Paginated Grid Flows (60 initial, +60 per page)
    private val _selectedMovieShelfFilter = MutableStateFlow<VodShelfFilter?>(null)
    val selectedMovieShelfFilter: StateFlow<VodShelfFilter?> = _selectedMovieShelfFilter.asStateFlow()

    private val _selectedSeriesShelfFilter = MutableStateFlow<VodShelfFilter?>(null)
    val selectedSeriesShelfFilter: StateFlow<VodShelfFilter?> = _selectedSeriesShelfFilter.asStateFlow()

    private val _movieShelfGridLimit = MutableStateFlow(SHELF_GRID_PAGE_SIZE)
    private val _seriesShelfGridLimit = MutableStateFlow(SHELF_GRID_PAGE_SIZE)

    val movieShelfGridItems: StateFlow<List<GroupedVodStream>> = buildShelfGridFlow(
        streamType = "movie",
        filterFlow = _selectedMovieShelfFilter,
        limitFlow = _movieShelfGridLimit
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    val seriesShelfGridItems: StateFlow<List<GroupedVodStream>> = buildShelfGridFlow(
        streamType = "series",
        filterFlow = _selectedSeriesShelfFilter,
        limitFlow = _seriesShelfGridLimit
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    // Per-shelf horizontal expansion (default 24 -> 48 -> 72 -> 120)
    private val _shelfLimits = MutableStateFlow<Map<String, Int>>(emptyMap())
    val shelfLimits: StateFlow<Map<String, Int>> = _shelfLimits.asStateFlow()

    // Materialized <5ms Discovery Shelves (section-isolated pipeline observation + dynamic platform shelves)
    val movieDiscoveryShelves: StateFlow<List<VodDiscoveryShelf>> =
        buildDiscoveryShelvesFlow(streamType = "movie")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    val seriesDiscoveryShelves: StateFlow<List<VodDiscoveryShelf>> =
        buildDiscoveryShelvesFlow(streamType = "series")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    // Series Detail Hierarchy State (Season selector chips, Episode cards, Versions & Similar Shows)
    private val _selectedSeriesStream = MutableStateFlow<StreamEntity?>(null)
    val selectedSeriesStream: StateFlow<StreamEntity?> = _selectedSeriesStream.asStateFlow()

    private val _seriesDetails = MutableStateFlow<SeriesDetails?>(null)
    val seriesDetails: StateFlow<SeriesDetails?> = _seriesDetails.asStateFlow()

    private val _isLoadingSeriesDetails = MutableStateFlow(false)
    val isLoadingSeriesDetails: StateFlow<Boolean> = _isLoadingSeriesDetails.asStateFlow()

    private val _selectedSeasonNumber = MutableStateFlow(1)
    val selectedSeasonNumber: StateFlow<Int> = _selectedSeasonNumber.asStateFlow()

    private val _availableSeriesVersions = MutableStateFlow<List<StreamEntity>>(emptyList())
    val availableSeriesVersions: StateFlow<List<StreamEntity>> = _availableSeriesVersions.asStateFlow()

    private val _similarSeries = MutableStateFlow<List<GroupedVodStream>>(emptyList())
    val similarSeries: StateFlow<List<GroupedVodStream>> = _similarSeries.asStateFlow()

    // Official Movie Franchises & Collections State (Room v8 non-destructive Level 2 organization)
    val movieCollections: StateFlow<List<VodCollectionEntity>> = repository.activePlaylist
        .flatMapLatest { playlist ->
            if (playlist == null) flowOf(emptyList()) else repository.observeMovieCollections(playlist.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    private val _selectedCollection = MutableStateFlow<VodCollectionEntity?>(null)
    val selectedCollection: StateFlow<VodCollectionEntity?> = _selectedCollection.asStateFlow()

    private val _collectionSortMode = MutableStateFlow(CollectionSortMode.RELEASE_ORDER)
    val collectionSortMode: StateFlow<CollectionSortMode> = _collectionSortMode.asStateFlow()

    val selectedCollectionMovies: StateFlow<List<GroupedVodStream>> = combine(
        repository.activePlaylist,
        _selectedCollection,
        _collectionSortMode
    ) { playlist, collection, sortMode ->
        Triple(playlist?.id, collection?.collectionId, sortMode)
    }.flatMapLatest { (playlistId, collectionId, sortMode) ->
        if (playlistId == null || collectionId.isNullOrBlank()) {
            flowOf(emptyList())
        } else {
            combine(
                repository.getCollectionMovies(playlistId, collectionId),
                repository.getCollectionMovieGroups(playlistId, collectionId)
            ) { movies, groups ->
                sortCollectionMovies(movies, groups, sortMode)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    // IMAX Movie Dossier State (Xtream get_vod_info with instant fallback, Versions, Collection & Similar Movies)
    private val _selectedMovieStream = MutableStateFlow<StreamEntity?>(null)
    val selectedMovieStream: StateFlow<StreamEntity?> = _selectedMovieStream.asStateFlow()

    private val _movieDetails = MutableStateFlow<VodMovieDetails?>(null)
    val movieDetails: StateFlow<VodMovieDetails?> = _movieDetails.asStateFlow()

    private val _isLoadingMovieDetails = MutableStateFlow(false)
    val isLoadingMovieDetails: StateFlow<Boolean> = _isLoadingMovieDetails.asStateFlow()

    private val _availableMovieVersions = MutableStateFlow<List<StreamEntity>>(emptyList())
    val availableMovieVersions: StateFlow<List<StreamEntity>> = _availableMovieVersions.asStateFlow()

    private val _similarMovies = MutableStateFlow<List<GroupedVodStream>>(emptyList())
    val similarMovies: StateFlow<List<GroupedVodStream>> = _similarMovies.asStateFlow()

    private val _activeMovieCollectionItems = MutableStateFlow<List<GroupedVodStream>>(emptyList())
    val activeMovieCollectionItems: StateFlow<List<GroupedVodStream>> = _activeMovieCollectionItems.asStateFlow()

    private val _activeMovieCollectionName = MutableStateFlow("")
    val activeMovieCollectionName: StateFlow<String> = _activeMovieCollectionName.asStateFlow()

    private val _activeMovieCollection = MutableStateFlow<VodCollectionEntity?>(null)
    val activeMovieCollection: StateFlow<VodCollectionEntity?> = _activeMovieCollection.asStateFlow()

    // Persisted per-section SyncTelemetry (survives app restarts, backgrounding, and process death)
    val movieSyncTelemetry: StateFlow<SyncTelemetry> = repository.activePlaylist
        .flatMapLatest { playlist ->
            if (playlist == null) {
                flowOf(SyncTelemetry(sectionType = "MOVIE"))
            } else {
                repository.observeSectionSyncState(playlist.id, "MOVIE")
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SyncTelemetry(sectionType = "MOVIE"))

    val seriesSyncTelemetry: StateFlow<SyncTelemetry> = repository.activePlaylist
        .flatMapLatest { playlist ->
            if (playlist == null) {
                flowOf(SyncTelemetry(sectionType = "SERIES"))
            } else {
                repository.observeSectionSyncState(playlist.id, "SERIES")
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SyncTelemetry(sectionType = "SERIES"))

    // Pull-to-Refresh & Isolated Per-Section Sync Banner Telemetry
    private val _isRefreshingMoviesTrigger = MutableStateFlow(false)
    val isRefreshingMovies: StateFlow<Boolean> = combine(
        _isRefreshingMoviesTrigger,
        movieSyncTelemetry
    ) { trigger, telemetry ->
        trigger || telemetry.isActive
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), false)

    private val _isRefreshingSeriesTrigger = MutableStateFlow(false)
    val isRefreshingSeries: StateFlow<Boolean> = combine(
        _isRefreshingSeriesTrigger,
        seriesSyncTelemetry
    ) { trigger, telemetry ->
        trigger || telemetry.isActive
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), false)

    private val _movieBannerState = MutableStateFlow(VodSyncBannerState())
    val movieBannerState: StateFlow<VodSyncBannerState> = _movieBannerState.asStateFlow()

    private val _seriesBannerState = MutableStateFlow(VodSyncBannerState())
    val seriesBannerState: StateFlow<VodSyncBannerState> = _seriesBannerState.asStateFlow()

    // Compatibility alias pointing to movieBannerState
    val vodSyncBannerState: StateFlow<VodSyncBannerState> = movieBannerState

    private var movieBannerAutoClearJob: Job? = null
    private var seriesBannerAutoClearJob: Job? = null
    private var lastMovieTelemetryStatus: SyncStatus = SyncStatus.IDLE
    private var lastSeriesTelemetryStatus: SyncStatus = SyncStatus.IDLE
    private var lastMoviePipelinePhase: VodPipelinePhase = VodPipelinePhase.IDLE
    private var lastSeriesPipelinePhase: VodPipelinePhase = VodPipelinePhase.IDLE

    init {
        viewModelScope.launch {
            combine(movieSyncTelemetry, repository.moviesPipelineState) { telemetry, pipeline ->
                telemetry to pipeline
            }.collect { (telemetry, pipeline) ->
                val transitionedToComplete =
                    (lastMovieTelemetryStatus == SyncStatus.RUNNING || lastMovieTelemetryStatus == SyncStatus.PENDING) &&
                        telemetry.status == SyncStatus.COMPLETED
                val pipelineTransitionedReady =
                    (lastMoviePipelinePhase == VodPipelinePhase.DOWNLOADING || lastMoviePipelinePhase == VodPipelinePhase.ORGANIZING) &&
                        pipeline.phase == VodPipelinePhase.READY
                lastMovieTelemetryStatus = telemetry.status
                lastMoviePipelinePhase = pipeline.phase
                applyCombinedSyncStateToBanner(
                    telemetry = telemetry,
                    pipeline = pipeline,
                    transitionedToComplete = transitionedToComplete || pipelineTransitionedReady,
                    targetBanner = _movieBannerState,
                    cancelExistingAutoClear = { movieBannerAutoClearJob?.cancel() },
                    setAutoClearJob = { job -> movieBannerAutoClearJob = job }
                )
            }
        }
        viewModelScope.launch {
            combine(seriesSyncTelemetry, repository.seriesPipelineState) { telemetry, pipeline ->
                telemetry to pipeline
            }.collect { (telemetry, pipeline) ->
                val transitionedToComplete =
                    (lastSeriesTelemetryStatus == SyncStatus.RUNNING || lastSeriesTelemetryStatus == SyncStatus.PENDING) &&
                        telemetry.status == SyncStatus.COMPLETED
                val pipelineTransitionedReady =
                    (lastSeriesPipelinePhase == VodPipelinePhase.DOWNLOADING || lastSeriesPipelinePhase == VodPipelinePhase.ORGANIZING) &&
                        pipeline.phase == VodPipelinePhase.READY
                lastSeriesTelemetryStatus = telemetry.status
                lastSeriesPipelinePhase = pipeline.phase
                applyCombinedSyncStateToBanner(
                    telemetry = telemetry,
                    pipeline = pipeline,
                    transitionedToComplete = transitionedToComplete || pipelineTransitionedReady,
                    targetBanner = _seriesBannerState,
                    cancelExistingAutoClear = { seriesBannerAutoClearJob?.cancel() },
                    setAutoClearJob = { job -> seriesBannerAutoClearJob = job }
                )
            }
        }
    }

    private fun applyCombinedSyncStateToBanner(
        telemetry: SyncTelemetry,
        pipeline: VodPipelineState,
        transitionedToComplete: Boolean,
        targetBanner: MutableStateFlow<VodSyncBannerState>,
        cancelExistingAutoClear: () -> Unit,
        setAutoClearJob: (Job?) -> Unit
    ) {
        val pendingAvailable = telemetry.pendingUpdateAvailable
        val pendingSummary = telemetry.pendingUpdateSummary
        val lastSuccessMs = telemetry.lastSuccessfulSyncMs

        when {
            telemetry.isActive -> {
                cancelExistingAutoClear()
                setAutoClearJob(null)
                val stageText = if (telemetry.status == SyncStatus.PENDING) {
                    telemetry.pendingUpdateSummary.takeIf { it.isNotBlank() }
                        ?: pipeline.statusText.takeIf { it.contains("Queued", ignoreCase = true) }
                        ?: CatalogSyncScheduler.STATUS_QUEUED
                } else {
                    telemetry.stage.label
                }
                targetBanner.value = VodSyncBannerState(
                    isSyncing = true,
                    statusText = stageText,
                    stageLabel = stageText,
                    statsSubtitle = formatSyncStatsSubtitle(telemetry),
                    isIndeterminate = telemetry.isIndeterminate || telemetry.status == SyncStatus.PENDING,
                    isError = false,
                    isCompleted = false,
                    isInterrupted = false,
                    progressPercent = telemetry.progressPercent.coerceIn(0, 99),
                    pendingUpdateAvailable = false,
                    pendingUpdateSummary = "",
                    lastSuccessfulSyncMs = lastSuccessMs
                )
            }
            pipeline.phase == VodPipelinePhase.DOWNLOADING || pipeline.phase == VodPipelinePhase.ORGANIZING -> {
                cancelExistingAutoClear()
                setAutoClearJob(null)
                val stageText = pipeline.statusText.ifBlank {
                    if (pipeline.phase == VodPipelinePhase.DOWNLOADING) {
                        SyncStage.DOWNLOADING_AND_DIFFING.label
                    } else {
                        SyncStage.GROUPING_AND_ORGANIZING.label
                    }
                }
                val stats = if (pipeline.totalCount > 0) {
                    "%,d / %,d grouped".format(
                        Locale.US,
                        pipeline.processedCount.coerceAtMost(pipeline.totalCount),
                        pipeline.totalCount
                    )
                } else {
                    ""
                }
                val pct = pipeline.progressPercent.coerceIn(0, 99)
                targetBanner.value = VodSyncBannerState(
                    isSyncing = true,
                    statusText = stageText,
                    stageLabel = stageText,
                    statsSubtitle = stats,
                    isIndeterminate = pct <= 0,
                    isError = false,
                    isCompleted = false,
                    isInterrupted = false,
                    progressPercent = pct,
                    pendingUpdateAvailable = false,
                    pendingUpdateSummary = "",
                    lastSuccessfulSyncMs = lastSuccessMs
                )
            }
            telemetry.status == SyncStatus.INTERRUPTED -> {
                cancelExistingAutoClear()
                setAutoClearJob(null)
                targetBanner.value = VodSyncBannerState(
                    isSyncing = false,
                    statusText = "Sync interrupted",
                    stageLabel = "Sync interrupted",
                    statsSubtitle = formatSyncStatsSubtitle(telemetry),
                    isIndeterminate = false,
                    isError = false,
                    isCompleted = false,
                    isInterrupted = true,
                    progressPercent = telemetry.progressPercent.coerceIn(0, 99),
                    pendingUpdateAvailable = pendingAvailable,
                    pendingUpdateSummary = pendingSummary,
                    lastSuccessfulSyncMs = lastSuccessMs
                )
            }
            telemetry.status == SyncStatus.FAILED || pipeline.phase == VodPipelinePhase.ERROR -> {
                cancelExistingAutoClear()
                setAutoClearJob(null)
                val errText = telemetry.errorMessage.takeIf { it.isNotBlank() }
                    ?: pipeline.error?.takeIf { it.isNotBlank() }
                    ?: pipeline.statusText.ifBlank { "Sync failed" }
                targetBanner.value = VodSyncBannerState(
                    isSyncing = false,
                    statusText = errText,
                    stageLabel = "Sync failed",
                    statsSubtitle = errText,
                    isIndeterminate = false,
                    isError = true,
                    isCompleted = false,
                    isInterrupted = false,
                    progressPercent = 0,
                    pendingUpdateAvailable = pendingAvailable,
                    pendingUpdateSummary = pendingSummary,
                    lastSuccessfulSyncMs = lastSuccessMs
                )
            }
            transitionedToComplete -> {
                cancelExistingAutoClear()
                val completionStats = formatSyncStatsSubtitle(telemetry)
                val msg = pipeline.statusText.takeIf { it.isNotBlank() && pipeline.phase == VodPipelinePhase.READY }
                    ?: "Catalog updated"
                targetBanner.value = VodSyncBannerState(
                    isSyncing = false,
                    statusText = msg,
                    stageLabel = msg,
                    statsSubtitle = completionStats,
                    isIndeterminate = false,
                    isError = false,
                    isCompleted = true,
                    isInterrupted = false,
                    progressPercent = 100,
                    pendingUpdateAvailable = pendingAvailable,
                    pendingUpdateSummary = pendingSummary,
                    lastSuccessfulSyncMs = lastSuccessMs
                )
                val job = viewModelScope.launch {
                    delay(READY_BANNER_AUTO_DISMISS_MS)
                    val cur = targetBanner.value
                    if (cur.isCompleted && !cur.isSyncing) {
                        targetBanner.value = VodSyncBannerState(
                            pendingUpdateAvailable = cur.pendingUpdateAvailable,
                            pendingUpdateSummary = cur.pendingUpdateSummary,
                            lastSuccessfulSyncMs = cur.lastSuccessfulSyncMs
                        )
                    }
                }
                setAutoClearJob(job)
            }
            else -> {
                val cur = targetBanner.value
                if (!cur.isCompleted) {
                    targetBanner.value = VodSyncBannerState(
                        pendingUpdateAvailable = pendingAvailable,
                        pendingUpdateSummary = pendingSummary,
                        lastSuccessfulSyncMs = lastSuccessMs
                    )
                } else {
                    targetBanner.value = cur.copy(
                        pendingUpdateAvailable = pendingAvailable,
                        pendingUpdateSummary = pendingSummary,
                        lastSuccessfulSyncMs = lastSuccessMs
                    )
                }
            }
        }
    }

    private fun showTransientCompletionBanner(
        tab: VodTab,
        message: String,
        durationMs: Long = READY_BANNER_AUTO_DISMISS_MS
    ) {
        val targetBanner = if (tab == VodTab.MOVIES) _movieBannerState else _seriesBannerState
        val telemetry = if (tab == VodTab.MOVIES) movieSyncTelemetry.value else seriesSyncTelemetry.value
        if (tab == VodTab.MOVIES) {
            movieBannerAutoClearJob?.cancel()
        } else {
            seriesBannerAutoClearJob?.cancel()
        }
        targetBanner.value = VodSyncBannerState(
            isSyncing = false,
            statusText = message,
            stageLabel = message,
            statsSubtitle = formatSyncStatsSubtitle(telemetry),
            isError = false,
            isCompleted = true,
            progressPercent = 100,
            pendingUpdateAvailable = telemetry.pendingUpdateAvailable,
            pendingUpdateSummary = telemetry.pendingUpdateSummary,
            lastSuccessfulSyncMs = telemetry.lastSuccessfulSyncMs
        )
        val job = viewModelScope.launch {
            delay(durationMs)
            val cur = targetBanner.value
            if (cur.isCompleted && !cur.isSyncing) {
                targetBanner.value = VodSyncBannerState(
                    pendingUpdateAvailable = cur.pendingUpdateAvailable,
                    pendingUpdateSummary = cur.pendingUpdateSummary,
                    lastSuccessfulSyncMs = cur.lastSuccessfulSyncMs
                )
            }
        }
        if (tab == VodTab.MOVIES) {
            movieBannerAutoClearJob = job
        } else {
            seriesBannerAutoClearJob = job
        }
    }

    fun dismissSyncBanner(tab: VodTab? = null) {
        if (tab == null || tab == VodTab.MOVIES) {
            movieBannerAutoClearJob?.cancel()
            movieBannerAutoClearJob = null
            val tel = movieSyncTelemetry.value
            _movieBannerState.value = VodSyncBannerState(
                pendingUpdateAvailable = tel.pendingUpdateAvailable,
                pendingUpdateSummary = tel.pendingUpdateSummary,
                lastSuccessfulSyncMs = tel.lastSuccessfulSyncMs
            )
        }
        if (tab == null || tab == VodTab.SERIES) {
            seriesBannerAutoClearJob?.cancel()
            seriesBannerAutoClearJob = null
            val tel = seriesSyncTelemetry.value
            _seriesBannerState.value = VodSyncBannerState(
                pendingUpdateAvailable = tel.pendingUpdateAvailable,
                pendingUpdateSummary = tel.pendingUpdateSummary,
                lastSuccessfulSyncMs = tel.lastSuccessfulSyncMs
            )
        }
    }

    fun dismissPendingUpdate(tab: VodTab) {
        viewModelScope.launch {
            val target = repository.getActivePlaylistOnce() ?: return@launch
            val sectionType = if (tab == VodTab.MOVIES) "MOVIE" else "SERIES"
            repository.markPendingUpdate(target.id, sectionType, "")
        }
    }

    fun openCollection(collection: VodCollectionEntity) {
        _selectedCollection.value = collection
    }

    fun closeCollection() {
        _selectedCollection.value = null
    }

    fun setCollectionSortMode(mode: CollectionSortMode) {
        _collectionSortMode.value = mode
    }

    fun openShelfSeeAll(tab: VodTab, filter: VodShelfFilter) {
        if (tab == VodTab.MOVIES) {
            _selectedCollection.value = null
            _selectedMovieShelfFilter.value = filter
            _movieShelfGridLimit.value = SHELF_GRID_PAGE_SIZE
        } else {
            _selectedSeriesShelfFilter.value = filter
            _seriesShelfGridLimit.value = SHELF_GRID_PAGE_SIZE
        }
    }

    fun closeShelfSeeAll(tab: VodTab) {
        if (tab == VodTab.MOVIES) {
            _selectedMovieShelfFilter.value = null
            _movieShelfGridLimit.value = SHELF_GRID_PAGE_SIZE
        } else {
            _selectedSeriesShelfFilter.value = null
            _seriesShelfGridLimit.value = SHELF_GRID_PAGE_SIZE
        }
    }

    fun loadMoreShelfGrid(tab: VodTab) {
        if (tab == VodTab.MOVIES) {
            _movieShelfGridLimit.value += SHELF_GRID_PAGE_SIZE
        } else {
            _seriesShelfGridLimit.value += SHELF_GRID_PAGE_SIZE
        }
    }

    fun loadMoreForShelf(shelfId: String) {
        if (shelfId.isBlank()) return
        val currentMap = _shelfLimits.value
        val currentLimit = currentMap[shelfId] ?: DEFAULT_SHELF_LIMIT
        val nextLimit = when {
            currentLimit < 48 -> 48
            currentLimit < 72 -> 72
            currentLimit < MAX_HORIZONTAL_SHELF_LIMIT -> MAX_HORIZONTAL_SHELF_LIMIT
            else -> MAX_HORIZONTAL_SHELF_LIMIT
        }
        if (nextLimit != currentLimit) {
            val updated = LinkedHashMap<String, Int>(currentMap)
            updated[shelfId] = nextLimit
            _shelfLimits.value = updated
        }
    }

    private fun triggerLazyIndexIfNeeded(playlistId: Long, streamType: String) {
        val key = "${playlistId}_$streamType"
        if (indexedTriggerGuard.add(key)) {
            viewModelScope.launch {
                repository.ensureVodCatalogIndexed(playlistId, streamType)
            }
        }
    }

    /**
     * Ensures the active Movies or Series section is populated:
     * 1. Triggers automatic background materialization (`ensureVodCatalogIndexed`) if raw rows
     *    exist from an upgraded database without `vod_catalog_groups` entries yet.
     * 2. If zero raw streams exist in Room for the section while a playlist is configured and no
     *    sync is running, triggers a one-time cooldown-bypassing section refresh so Series (or
     *    Movies) recovers automatically instead of staying stuck on an empty screen.
     */
    fun ensureSectionLoaded(sectionTab: VodTab, playlist: PlaylistEntity?) {
        val targetPlaylist = playlist ?: return
        val isMovies = sectionTab == VodTab.MOVIES
        val streamType = if (isMovies) "movie" else "series"
        val currentlyEmpty = if (isMovies) {
            groupedMovieItems.value.isEmpty() && movieDiscoveryShelves.value.isEmpty()
        } else {
            groupedSeriesItems.value.isEmpty() && seriesDiscoveryShelves.value.isEmpty()
        }
        if (!currentlyEmpty) return
        val sectionPhase = if (isMovies) {
            repository.moviesPipelineState.value.phase
        } else {
            repository.seriesPipelineState.value.phase
        }
        val sectionPipelineBusy = sectionPhase == VodPipelinePhase.DOWNLOADING ||
            sectionPhase == VodPipelinePhase.ORGANIZING
        if (sectionPipelineBusy) return
        if (isMovies && (_isRefreshingMoviesTrigger.value || movieSyncTelemetry.value.isActive)) return
        if (!isMovies && (_isRefreshingSeriesTrigger.value || seriesSyncTelemetry.value.isActive)) return

        val recoveryKey = "${targetPlaylist.id}_$streamType"
        if (!autoSectionRecoveryGuard.add(recoveryKey)) return

        viewModelScope.launch {
            try {
                repository.ensureVodCatalogIndexed(targetPlaylist.id, streamType)
                val rawCount = repository.countStreamsByType(targetPlaylist.id, streamType)
                val latestPhase = if (isMovies) {
                    repository.moviesPipelineState.value.phase
                } else {
                    repository.seriesPipelineState.value.phase
                }
                val stillBusy = latestPhase == VodPipelinePhase.DOWNLOADING ||
                    latestPhase == VodPipelinePhase.ORGANIZING
                if (rawCount == 0 && !stillBusy) {
                    if (isMovies) {
                        refreshMovies(targetPlaylist, force = true)
                    } else {
                        refreshSeries(targetPlaylist, force = true)
                    }
                }
            } catch (_: Exception) {
                autoSectionRecoveryGuard.remove(recoveryKey)
            }
        }
    }

    /**
     * Organizes raw IPTV VOD/Series categories into 4 clean, scannable buckets while preserving
     * distinct language tags (so `[EN] NETFLIX`, `[AR] NETFLIX`, and `[FR] NETFLIX` remain
     * distinguishable with `EN`, `AR`, `FR` badges instead of collapsing into identical labels).
     */
    fun organizeVodCategories(
        categories: List<CategoryEntity>,
        counts: Map<String, Int>
    ): Map<VodCategoryBucket, List<CategorizedVodItem>> {
        return organizeVodCategoriesStatic(categories, counts)
    }

    private fun buildShelfGridFlow(
        streamType: String,
        filterFlow: StateFlow<VodShelfFilter?>,
        limitFlow: StateFlow<Int>
    ): Flow<List<GroupedVodStream>> {
        return combine(repository.activePlaylist, filterFlow, limitFlow) { playlist, filter, limit ->
            Triple(playlist?.id, filter, limit)
        }.flatMapLatest { (playlistId, filter, limit) ->
            if (playlistId == null || filter == null) {
                flowOf(emptyList())
            } else {
                triggerLazyIndexIfNeeded(playlistId, streamType)
                when (filter) {
                    VodShelfFilter.RecentlyAdded -> repository.getRecentlyAddedVod(playlistId, streamType, limit = limit)
                    VodShelfFilter.TopRated -> repository.getTopRatedVod(playlistId, streamType, limit = limit)
                    VodShelfFilter.Recommended -> repository.getRecommendedVodForUser(playlistId, streamType, limit = limit)
                    is VodShelfFilter.Genre -> repository.getGenreShelfVod(playlistId, streamType, filter.keyword, limit = limit)
                    is VodShelfFilter.Platform -> repository.getPlatformShelfVod(playlistId, streamType, filter.keyword, limit = limit)
                }
            }
        }
    }

    private data class ShelfPageResult(
        val items: List<GroupedVodStream>,
        val canLoadMore: Boolean
    )

    private fun shelfQueryWithLimit(
        shelfId: String,
        queryByLimit: (Int) -> Flow<List<GroupedVodStream>>
    ): Flow<ShelfPageResult> {
        return _shelfLimits
            .map { limits -> limits[shelfId] ?: DEFAULT_SHELF_LIMIT }
            .distinctUntilChanged()
            .flatMapLatest { limit ->
                queryByLimit(limit + 1).map { fetched ->
                    val hasMore = fetched.size > limit && limit < MAX_HORIZONTAL_SHELF_LIMIT
                    ShelfPageResult(
                        items = if (fetched.size > limit) fetched.take(limit) else fetched,
                        canLoadMore = hasMore
                    )
                }
            }
    }

    private fun buildDiscoveryShelvesFlow(streamType: String): Flow<List<VodDiscoveryShelf>> {
        val isMovie = streamType == "movie"
        val sectionPipelineFlow = if (isMovie) {
            repository.moviesPipelineState
        } else {
            repository.seriesPipelineState
        }
        val isSectionBusyFlow = sectionPipelineFlow
            .map { it.phase == VodPipelinePhase.DOWNLOADING || it.phase == VodPipelinePhase.ORGANIZING }
            .distinctUntilChanged()

        return combine(repository.activePlaylist, isSectionBusyFlow) { playlist, isBusy ->
            playlist to isBusy
        }.flatMapLatest { (playlist, isBusy) ->
            when {
                playlist == null -> flowOf(emptyList())
                isBusy -> emptyFlow()
                else -> {
                    triggerLazyIndexIfNeeded(playlist.id, streamType)

                    val recentId = "${streamType}_recently_added"
                    val topRatedId = "${streamType}_top_rated"
                    val recommendedId = "${streamType}_recommended"

                    val recentFlow = shelfQueryWithLimit(recentId) { fetchLimit ->
                        repository.getRecentlyAddedVod(playlist.id, streamType, limit = fetchLimit)
                    }
                    val topRatedFlow = shelfQueryWithLimit(topRatedId) { fetchLimit ->
                        repository.getTopRatedVod(playlist.id, streamType, limit = fetchLimit)
                    }
                    val recommendedFlow = shelfQueryWithLimit(recommendedId) { fetchLimit ->
                        repository.getRecommendedVodForUser(playlist.id, streamType, limit = fetchLimit)
                    }

                    val platformFlows: List<Flow<Pair<String, ShelfPageResult>>> = CURATED_PLATFORM_KEYWORDS.map { platform ->
                        val shelfId = "${streamType}_platform_${platform.lowercase(Locale.US).replace(Regex("[^a-z0-9]+"), "_")}"
                        shelfQueryWithLimit(shelfId) { fetchLimit ->
                            repository.getPlatformShelfVod(playlist.id, streamType, platform, limit = fetchLimit)
                        }.map { page -> platform to page }
                    }
                    val combinedPlatformsFlow: Flow<List<Pair<String, ShelfPageResult>>> = combine(platformFlows) { array ->
                        array.toList()
                    }

                    val genreFlows: List<Flow<Pair<String, ShelfPageResult>>> = CURATED_GENRE_KEYWORDS.map { keyword ->
                        val shelfId = "${streamType}_genre_${keyword.lowercase(Locale.US)}"
                        shelfQueryWithLimit(shelfId) { fetchLimit ->
                            repository.getGenreShelfVod(playlist.id, streamType, keyword, limit = fetchLimit)
                        }.map { page -> keyword to page }
                    }
                    val combinedGenresFlow: Flow<List<Pair<String, ShelfPageResult>>> = combine(genreFlows) { array ->
                        array.toList()
                    }

                    combine(
                        recentFlow,
                        topRatedFlow,
                        recommendedFlow,
                        combinedPlatformsFlow,
                        combinedGenresFlow
                    ) { recent, topRated, recommended, platformPairs, genrePairs ->
                        buildList {
                            if (recent.items.isNotEmpty()) {
                                add(
                                    VodDiscoveryShelf(
                                        id = recentId,
                                        title = "Recently Added",
                                        subtitle = if (isMovie) "Newest arrivals in your library" else "Latest series additions",
                                        items = recent.items,
                                        shelfFilter = VodShelfFilter.RecentlyAdded,
                                        canLoadMore = recent.canLoadMore
                                    )
                                )
                            }
                            if (topRated.items.isNotEmpty()) {
                                add(
                                    VodDiscoveryShelf(
                                        id = topRatedId,
                                        title = if (isMovie) "Top Rated Cinema" else "Acclaimed Series",
                                        subtitle = "Most popular & highest rated",
                                        items = topRated.items,
                                        shelfFilter = VodShelfFilter.TopRated,
                                        canLoadMore = topRated.canLoadMore
                                    )
                                )
                            }
                            if (recommended.items.isNotEmpty()) {
                                add(
                                    VodDiscoveryShelf(
                                        id = recommendedId,
                                        title = "Recommended for You",
                                        subtitle = "Based on your favorites & watch history",
                                        items = recommended.items,
                                        shelfFilter = VodShelfFilter.Recommended,
                                        canLoadMore = recommended.canLoadMore
                                    )
                                )
                            }
                            for ((platform, page) in platformPairs) {
                                if (page.items.size >= MIN_PLATFORM_SHELF_ITEMS) {
                                    val shelfId = "${streamType}_platform_${platform.lowercase(Locale.US).replace(Regex("[^a-z0-9]+"), "_")}"
                                    add(
                                        VodDiscoveryShelf(
                                            id = shelfId,
                                            title = platform,
                                            subtitle = if (isMovie) "Popular on $platform" else "$platform Originals & Series",
                                            items = page.items,
                                            shelfFilter = VodShelfFilter.Platform(platform),
                                            canLoadMore = page.canLoadMore
                                        )
                                    )
                                }
                            }
                            for ((keyword, page) in genrePairs) {
                                if (page.items.size >= MIN_GENRE_SHELF_ITEMS) {
                                    add(
                                        VodDiscoveryShelf(
                                            id = "${streamType}_genre_${keyword.lowercase(Locale.US)}",
                                            title = keyword,
                                            subtitle = if (isMovie) "$keyword Cinema" else "$keyword Series",
                                            items = page.items,
                                            shelfFilter = VodShelfFilter.Genre(keyword),
                                            canLoadMore = page.canLoadMore
                                        )
                                    )
                                }
                            }
                        }
                    }.debounce(150L)
                }
            }
        }
    }

    fun selectMovieCategory(categoryId: String?) {
        _selectedMovieCategoryId.value = categoryId
        _currentLimit.value = pageSize
    }

    fun onMovieSearchQueryChanged(query: String) {
        _movieSearchQuery.value = query
        _currentLimit.value = pageSize
    }

    fun selectSeriesCategory(categoryId: String?) {
        _selectedSeriesCategoryId.value = categoryId
        _currentLimit.value = pageSize
    }

    fun onSeriesSearchQueryChanged(query: String) {
        _seriesSearchQuery.value = query
        _currentLimit.value = pageSize
    }

    fun loadMore() {
        _currentLimit.value += pageSize
    }

    fun loadNextPage() {
        loadMore()
    }

    fun openMovieDetails(
        movieStream: StreamEntity,
        playlist: PlaylistEntity?,
        autoSelectPreferred: Boolean = true
    ) {
        val isSameGroup = _selectedMovieStream.value?.canonicalGroupKey?.isNotBlank() == true &&
            _selectedMovieStream.value?.canonicalGroupKey == movieStream.canonicalGroupKey
        _selectedMovieStream.value = movieStream
        if (!isSameGroup) {
            _availableMovieVersions.value = listOf(movieStream)
            _similarMovies.value = emptyList()
            _activeMovieCollectionItems.value = emptyList()
            _activeMovieCollectionName.value = ""
            _activeMovieCollection.value = null
        }

        // Provide instant fallback from StreamEntity metadata immediately
        val instantFallback = VodMovieDetails(
            vodId = movieStream.streamId,
            title = movieStream.name,
            coverUrl = movieStream.streamIcon,
            backdropUrl = movieStream.streamIcon,
            genre = movieStream.genres,
            releaseDate = if (movieStream.releaseYear > 0) movieStream.releaseYear.toString() else "",
            rating = movieStream.rating,
            containerExtension = movieStream.containerExtension.ifBlank { "mp4" },
            tmdbOrImdbId = movieStream.tmdbId
        )
        _movieDetails.value = instantFallback
        _isLoadingMovieDetails.value = true

        // 1. Load sibling versions from Room (sorted by preferred language & quality)
        viewModelScope.launch {
            val versions = repository.getVodVersionsForGroup(
                playlistId = movieStream.playlistId,
                streamType = "movie",
                canonicalGroupKey = movieStream.canonicalGroupKey,
                fallbackStream = movieStream
            )
            if (_selectedMovieStream.value?.id == movieStream.id) {
                _availableMovieVersions.value = versions
                val topPreferred = versions.firstOrNull()
                if (autoSelectPreferred && !isSameGroup && topPreferred != null && topPreferred.id != movieStream.id) {
                    openMovieDetails(topPreferred, playlist, autoSelectPreferred = false)
                    return@launch
                }
            }
        }

        // 1b. Load sibling franchise / collection installments in Release Order when movie belongs to a collection
        if (!isSameGroup) {
            viewModelScope.launch {
                val resolvedGroupKey = movieStream.canonicalGroupKey.ifBlank {
                    VodGroupingNormalizer.computeCanonicalGroupKey(
                        streamType = "movie",
                        rawName = movieStream.name,
                        tmdbId = movieStream.tmdbId,
                        releaseYear = movieStream.releaseYear,
                        streamId = movieStream.streamId,
                        posterUrl = movieStream.streamIcon
                    )
                }
                if (resolvedGroupKey.isNotBlank()) {
                    val group = StreamVaultDatabase.getInstance(getApplication())
                        .vodGroupDao()
                        .getGroupByKey(movieStream.playlistId, "movie", resolvedGroupKey)
                    if (group != null && group.collectionId.isNotBlank()) {
                        val franchiseMovies = repository.getCollectionMovies(
                            playlistId = movieStream.playlistId,
                            collectionId = group.collectionId
                        ).first()
                        val currentSelected = _selectedMovieStream.value
                        val stillViewingSameMovie = currentSelected != null && (
                            currentSelected.id == movieStream.id ||
                                (currentSelected.canonicalGroupKey.isNotBlank() &&
                                    currentSelected.canonicalGroupKey == resolvedGroupKey)
                            )
                        if (stillViewingSameMovie && franchiseMovies.isNotEmpty()) {
                            val matchingCollection = movieCollections.value
                                .firstOrNull { it.collectionId == group.collectionId }
                            val resolvedName = group.collectionName.ifBlank {
                                matchingCollection?.name.orEmpty()
                            }
                            _activeMovieCollectionItems.value = franchiseMovies
                            _activeMovieCollectionName.value = resolvedName
                            _activeMovieCollection.value = matchingCollection ?: VodCollectionEntity(
                                playlistId = movieStream.playlistId,
                                collectionId = group.collectionId,
                                name = resolvedName,
                                posterUrl = movieStream.streamIcon,
                                movieCount = franchiseMovies.size,
                                popularityScore = group.popularityScore
                            )
                        }
                    }
                }
            }
        }

        // 2. Load initial similar movies if stream already carries genre metadata
        val initialGenreKeyword = extractPrimaryGenreKeyword(movieStream.genres)
        if (initialGenreKeyword.isNotEmpty() && !isSameGroup) {
            viewModelScope.launch {
                val similar = repository.getSimilarVodTitles(
                    playlistId = movieStream.playlistId,
                    streamType = "movie",
                    genreKeyword = initialGenreKeyword,
                    excludeGroupKey = movieStream.canonicalGroupKey,
                    limit = SIMILAR_TITLES_LIMIT
                )
                if (_selectedMovieStream.value?.id == movieStream.id) {
                    _similarMovies.value = similar
                }
            }
        }

        // 3. Load full Xtream/TMDb movie dossier and refresh similar movies if genre came from details
        viewModelScope.launch {
            val details = repository.fetchVodMovieDetails(playlist, movieStream)
            if (_selectedMovieStream.value?.id == movieStream.id) {
                _movieDetails.value = details
                _isLoadingMovieDetails.value = false

                if (_similarMovies.value.isEmpty()) {
                    val resolvedGenre = extractPrimaryGenreKeyword(details.genre)
                    if (resolvedGenre.isNotEmpty()) {
                        val similar = repository.getSimilarVodTitles(
                            playlistId = movieStream.playlistId,
                            streamType = "movie",
                            genreKeyword = resolvedGenre,
                            excludeGroupKey = movieStream.canonicalGroupKey,
                            limit = SIMILAR_TITLES_LIMIT
                        )
                        if (_selectedMovieStream.value?.id == movieStream.id) {
                            _similarMovies.value = similar
                        }
                    }
                }
            }
        }
    }

    fun switchMovieVersion(versionStream: StreamEntity, playlist: PlaylistEntity?) {
        if (_selectedMovieStream.value?.id == versionStream.id) return
        openMovieDetails(versionStream, playlist, autoSelectPreferred = false)
    }

    fun closeMovieDetails() {
        _selectedMovieStream.value = null
        _movieDetails.value = null
        _isLoadingMovieDetails.value = false
        _availableMovieVersions.value = emptyList()
        _similarMovies.value = emptyList()
        _activeMovieCollectionItems.value = emptyList()
        _activeMovieCollectionName.value = ""
        _activeMovieCollection.value = null
    }

    fun openSeriesDetails(
        seriesStream: StreamEntity,
        playlist: PlaylistEntity,
        autoSelectPreferred: Boolean = true
    ) {
        val isSameGroup = _selectedSeriesStream.value?.canonicalGroupKey?.isNotBlank() == true &&
            _selectedSeriesStream.value?.canonicalGroupKey == seriesStream.canonicalGroupKey
        _selectedSeriesStream.value = seriesStream
        _seriesDetails.value = null
        _isLoadingSeriesDetails.value = true
        if (!isSameGroup) {
            _availableSeriesVersions.value = listOf(seriesStream)
            _similarSeries.value = emptyList()
        }

        // 1. Load sibling versions from Room (sorted by preferred language & quality)
        viewModelScope.launch {
            val versions = repository.getVodVersionsForGroup(
                playlistId = seriesStream.playlistId,
                streamType = "series",
                canonicalGroupKey = seriesStream.canonicalGroupKey,
                fallbackStream = seriesStream
            )
            if (_selectedSeriesStream.value?.id == seriesStream.id) {
                _availableSeriesVersions.value = versions
                val topPreferred = versions.firstOrNull()
                if (autoSelectPreferred && !isSameGroup && topPreferred != null && topPreferred.id != seriesStream.id) {
                    openSeriesDetails(topPreferred, playlist, autoSelectPreferred = false)
                    return@launch
                }
            }
        }

        // 2. Load initial similar series if genre metadata exists on the stream
        val initialGenreKeyword = extractPrimaryGenreKeyword(seriesStream.genres)
        if (initialGenreKeyword.isNotEmpty() && !isSameGroup) {
            viewModelScope.launch {
                val similar = repository.getSimilarVodTitles(
                    playlistId = seriesStream.playlistId,
                    streamType = "series",
                    genreKeyword = initialGenreKeyword,
                    excludeGroupKey = seriesStream.canonicalGroupKey,
                    limit = SIMILAR_TITLES_LIMIT
                )
                if (_selectedSeriesStream.value?.id == seriesStream.id) {
                    _similarSeries.value = similar
                }
            }
        }

        // 3. Load seasons, episodes, and metadata from Xtream get_series_info
        viewModelScope.launch {
            val details = repository.fetchSeriesDetails(playlist, seriesStream)
            if (_selectedSeriesStream.value?.id == seriesStream.id) {
                _seriesDetails.value = details
                _selectedSeasonNumber.value = details.seasons.firstOrNull()?.seasonNumber ?: 1
                _isLoadingSeriesDetails.value = false

                if (_similarSeries.value.isEmpty()) {
                    val resolvedGenre = extractPrimaryGenreKeyword(details.genre)
                    if (resolvedGenre.isNotEmpty()) {
                        val similar = repository.getSimilarVodTitles(
                            playlistId = seriesStream.playlistId,
                            streamType = "series",
                            genreKeyword = resolvedGenre,
                            excludeGroupKey = seriesStream.canonicalGroupKey,
                            limit = SIMILAR_TITLES_LIMIT
                        )
                        if (_selectedSeriesStream.value?.id == seriesStream.id) {
                            _similarSeries.value = similar
                        }
                    }
                }
            }
        }
    }

    fun switchSeriesVersion(versionStream: StreamEntity, playlist: PlaylistEntity) {
        if (_selectedSeriesStream.value?.id == versionStream.id) return
        openSeriesDetails(versionStream, playlist, autoSelectPreferred = false)
    }

    fun selectSeason(seasonNumber: Int) {
        _selectedSeasonNumber.value = seasonNumber
    }

    fun closeSeriesDetails() {
        _selectedSeriesStream.value = null
        _seriesDetails.value = null
        _isLoadingSeriesDetails.value = false
        _availableSeriesVersions.value = emptyList()
        _similarSeries.value = emptyList()
    }

    fun refreshMovies(playlist: PlaylistEntity? = null, force: Boolean = false) {
        val telemetry = movieSyncTelemetry.value
        if (telemetry.isActive || _isRefreshingMoviesTrigger.value) return
        val selectedCat = _selectedMovieCategoryId.value?.takeIf { it.isNotBlank() }
        val now = System.currentTimeMillis()
        val canCooldownSkip = !force &&
            selectedCat == null &&
            !telemetry.pendingUpdateAvailable &&
            telemetry.status != SyncStatus.INTERRUPTED &&
            telemetry.status != SyncStatus.FAILED &&
            telemetry.lastSuccessfulSyncMs > 0L &&
            (now - telemetry.lastSuccessfulSyncMs) < REFRESH_COOLDOWN_MS
        if (canCooldownSkip) {
            showTransientCompletionBanner(VodTab.MOVIES, "Catalog is up to date", durationMs = 2_000L)
            return
        }
        viewModelScope.launch {
            val target = playlist ?: repository.getActivePlaylistOnce() ?: return@launch
            _isRefreshingMoviesTrigger.value = true
            try {
                CatalogSyncWorker.enqueueSectionSync(
                    context = getApplication(),
                    playlistId = target.id,
                    sectionType = "MOVIE",
                    categoryId = selectedCat,
                    forceFull = force,
                    expedited = true
                )
            } finally {
                _isRefreshingMoviesTrigger.value = false
            }
        }
    }

    fun refreshMoviesCategory(categoryId: String, playlist: PlaylistEntity? = null) {
        val cleanCatId = categoryId.trim()
        if (cleanCatId.isEmpty()) {
            refreshMovies(playlist = playlist, force = false)
            return
        }
        if (movieSyncTelemetry.value.isActive || _isRefreshingMoviesTrigger.value) return
        viewModelScope.launch {
            val target = playlist ?: repository.getActivePlaylistOnce() ?: return@launch
            _isRefreshingMoviesTrigger.value = true
            try {
                CatalogSyncWorker.enqueueSectionSync(
                    context = getApplication(),
                    playlistId = target.id,
                    sectionType = "MOVIE",
                    categoryId = cleanCatId,
                    forceFull = false,
                    expedited = true
                )
            } finally {
                _isRefreshingMoviesTrigger.value = false
            }
        }
    }

    fun refreshSeries(playlist: PlaylistEntity? = null, force: Boolean = false) {
        val telemetry = seriesSyncTelemetry.value
        if (telemetry.isActive || _isRefreshingSeriesTrigger.value) return
        val selectedCat = _selectedSeriesCategoryId.value?.takeIf { it.isNotBlank() }
        val now = System.currentTimeMillis()
        val canCooldownSkip = !force &&
            selectedCat == null &&
            !telemetry.pendingUpdateAvailable &&
            telemetry.status != SyncStatus.INTERRUPTED &&
            telemetry.status != SyncStatus.FAILED &&
            telemetry.lastSuccessfulSyncMs > 0L &&
            (now - telemetry.lastSuccessfulSyncMs) < REFRESH_COOLDOWN_MS
        if (canCooldownSkip) {
            showTransientCompletionBanner(VodTab.SERIES, "Catalog is up to date", durationMs = 2_000L)
            return
        }
        viewModelScope.launch {
            val target = playlist ?: repository.getActivePlaylistOnce() ?: return@launch
            _isRefreshingSeriesTrigger.value = true
            try {
                CatalogSyncWorker.enqueueSectionSync(
                    context = getApplication(),
                    playlistId = target.id,
                    sectionType = "SERIES",
                    categoryId = selectedCat,
                    forceFull = force,
                    expedited = true
                )
            } finally {
                _isRefreshingSeriesTrigger.value = false
            }
        }
    }

    fun refreshSeriesCategory(categoryId: String, playlist: PlaylistEntity? = null) {
        val cleanCatId = categoryId.trim()
        if (cleanCatId.isEmpty()) {
            refreshSeries(playlist = playlist, force = false)
            return
        }
        if (seriesSyncTelemetry.value.isActive || _isRefreshingSeriesTrigger.value) return
        viewModelScope.launch {
            val target = playlist ?: repository.getActivePlaylistOnce() ?: return@launch
            _isRefreshingSeriesTrigger.value = true
            try {
                CatalogSyncWorker.enqueueSectionSync(
                    context = getApplication(),
                    playlistId = target.id,
                    sectionType = "SERIES",
                    categoryId = cleanCatId,
                    forceFull = false,
                    expedited = true
                )
            } finally {
                _isRefreshingSeriesTrigger.value = false
            }
        }
    }

    fun toggleFavorite(streamId: Long, currentFav: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(streamId, !currentFav)
            // Keep selectedSeriesStream and selectedMovieStream favorite flags in sync
            _selectedSeriesStream.value?.let { current ->
                if (current.id == streamId) {
                    _selectedSeriesStream.value = current.copy(isFavorite = !currentFav)
                }
            }
            _selectedMovieStream.value?.let { current ->
                if (current.id == streamId) {
                    _selectedMovieStream.value = current.copy(isFavorite = !currentFav)
                }
            }
        }
    }

    private fun extractPrimaryGenreKeyword(genresRaw: String): String {
        return genresRaw
            .split(Regex("[,/|•]+"))
            .map { it.trim() }
            .firstOrNull { it.length >= 3 }
            .orEmpty()
    }

    private fun sortCollectionMovies(
        movies: List<GroupedVodStream>,
        groups: List<VodCatalogGroupEntity>,
        sortMode: CollectionSortMode
    ): List<GroupedVodStream> {
        if (movies.size <= 1) return movies
        val groupByRepId = groups.associateBy { it.representativeStreamDbId }
        val groupByKey = groups.associateBy { it.groupKey }
        fun metaFor(item: GroupedVodStream): VodCatalogGroupEntity? {
            return groupByRepId[item.stream.id] ?: groupByKey[item.stream.canonicalGroupKey]
        }
        return when (sortMode) {
            CollectionSortMode.RELEASE_ORDER -> movies.sortedWith(
                compareBy<GroupedVodStream>(
                    { item ->
                        val ord = metaFor(item)?.collectionOrder ?: 0
                        if (ord > 0) ord else Int.MAX_VALUE
                    },
                    { item ->
                        val yr = metaFor(item)?.releaseYear?.takeIf { it > 0 }
                            ?: item.stream.releaseYear.takeIf { it > 0 }
                            ?: Int.MAX_VALUE
                        yr
                    },
                    { item ->
                        metaFor(item)?.cleanTitle?.lowercase(Locale.US)
                            ?: item.stream.name.lowercase(Locale.US)
                    }
                )
            )
            CollectionSortMode.NEWEST_FIRST -> movies.sortedWith(
                compareByDescending<GroupedVodStream> { item ->
                    metaFor(item)?.releaseYear?.takeIf { it > 0 } ?: item.stream.releaseYear
                }.thenByDescending { item ->
                    metaFor(item)?.popularityScore ?: item.stream.ratingNumeric
                }.thenBy { item ->
                    metaFor(item)?.cleanTitle?.lowercase(Locale.US)
                        ?: item.stream.name.lowercase(Locale.US)
                }
            )
            CollectionSortMode.TOP_RATED -> movies.sortedWith(
                compareByDescending<GroupedVodStream> { item ->
                    metaFor(item)?.popularityScore ?: item.stream.ratingNumeric
                }.thenByDescending { item ->
                    item.stream.ratingNumeric
                }.thenByDescending { item ->
                    metaFor(item)?.releaseYear?.takeIf { it > 0 } ?: item.stream.releaseYear
                }
            )
        }
    }

    private data class VodQueryParams(
        val playlistId: Long?,
        val categoryId: String?,
        val query: String,
        val limit: Int
    )

    companion object {
        const val PAGE_SIZE = 180
        const val SHELF_GRID_PAGE_SIZE = 60
        const val DEFAULT_SHELF_LIMIT = 24
        const val MAX_HORIZONTAL_SHELF_LIMIT = 120
        const val REFRESH_COOLDOWN_MS = 30_000L
        private const val READY_BANNER_AUTO_DISMISS_MS = 2_500L
        private const val SIMILAR_TITLES_LIMIT = 15
        private const val MIN_GENRE_SHELF_ITEMS = 3
        private const val MIN_PLATFORM_SHELF_ITEMS = 6
        private val CURATED_PLATFORM_KEYWORDS = listOf(
            "Netflix",
            "Prime Video",
            "Apple TV+",
            "Disney+",
            "HBO"
        )
        private val CURATED_GENRE_KEYWORDS = listOf(
            "Action",
            "Crime",
            "Drama",
            "Comedy",
            "Thriller",
            "Sci-Fi",
            "Documentary"
        )

        private val LEADING_DECORATION_REGEX = Regex("^[\\s✦●★☆◆◇►▶•*\\-_|│┃:]+")
        private val LEADING_SECTION_PREFIX_REGEX = Regex("(?i)^\\s*(?:VOD|SRS|MOV|MOVIE|MOVIES|SERIES|FILM|FILMS)\\s*[|│┃:\\-]\\s*")
        private val LEADING_BRACKET_LANG_REGEX = Regex("^\\s*[\\[\\(]([A-Za-z]{2,8})(?:\\s*[/&-]\\s*[A-Za-z0-9]{1,8})?[\\]\\)]\\s*[|│┃:\\-]?\\s*")
        private val LEADING_DELIM_LANG_REGEX = Regex("^\\s*([A-Za-z]{2,8})\\s*[|│┃:\\-]\\s+")
        private val MULTI_SPACE_REGEX = Regex("\\s+")

        private val CATEGORY_LANGUAGE_CANONICAL_MAP = mapOf(
            "EN" to "EN", "ENG" to "EN", "ENGLISH" to "EN",
            "AR" to "AR", "ARB" to "AR", "ARA" to "AR", "ARABIC" to "AR",
            "FR" to "FR", "FRE" to "FR", "FRENCH" to "FR", "VOSTFR" to "FR",
            "US" to "US", "USA" to "US",
            "UK" to "UK",
            "MULTI" to "MULTI", "MULTISUB" to "MULTI",
            "ES" to "ES", "ESP" to "ES", "SPA" to "ES", "SPANISH" to "ES", "LAT" to "ES", "LATINO" to "ES",
            "TR" to "TR", "TUR" to "TR", "TURKISH" to "TR",
            "DE" to "DE", "GER" to "DE", "GERMAN" to "DE",
            "IT" to "IT", "ITA" to "IT", "ITALIAN" to "IT",
            "PT" to "PT", "POR" to "PT", "BR" to "PT",
            "ALB" to "ALB", "AL" to "ALB", "ALBANIAN" to "ALB",
            "IR" to "IR", "FA" to "IR", "IRAN" to "IR", "PERSIAN" to "IR", "FARSI" to "IR",
            "BG" to "BG", "BULGARIAN" to "BG",
            "NL" to "NL", "DUTCH" to "NL",
            "RU" to "RU", "RUS" to "RU",
            "PL" to "PL", "GR" to "GR", "RO" to "RO",
            "IN" to "IN", "IND" to "IN", "HI" to "IN", "HINDI" to "IN",
            "KU" to "KU", "KR" to "KR", "JP" to "JP",
            "SE" to "SE", "NO" to "NO", "DK" to "DK", "FI" to "FI",
            "CA" to "CA", "AU" to "AU", "MX" to "MX", "EU" to "EU", "EXYU" to "EXYU"
        )

        private val YOUR_LANGUAGE_TAGS = setOf("EN", "AR", "FR", "US", "UK", "MULTI")

        private val YOUR_LANGUAGE_PRIORITY = mapOf(
            "EN" to 0,
            "US" to 1,
            "UK" to 2,
            "AR" to 3,
            "FR" to 4,
            "MULTI" to 5
        )

        private val STREAMING_PLATFORM_REGEX = Regex(
            "(?i)(?:netflix|amazon|prime|apple|disney|hbo|\\bmax\\b|shahid|hulu|paramount|osn|starz)"
        )

        private val GENRE_COLLECTION_REGEX = Regex(
            "(?i)(?:action|comedy|drama|crime|thriller|horror|sci[\\s\\-]?fi|documentary|animation|kids|4k|uhd|\\btop\\b|latest|2025|2026)"
        )

        private val UPPERCASE_ACRONYMS = setOf(
            "HBO", "OSN", "VIP", "4K", "UHD", "FHD", "HD", "SD", "TV", "BBC", "USA", "UK",
            "UFC", "WWE", "VOD", "VOSTFR", "HDR", "HEVC", "II", "III", "IV", "MCU", "DC"
        )

        fun organizeVodCategoriesStatic(
            categories: List<CategoryEntity>,
            counts: Map<String, Int>
        ): Map<VodCategoryBucket, List<CategorizedVodItem>> {
            if (categories.isEmpty()) return emptyMap()

            val buckets = linkedMapOf<VodCategoryBucket, MutableList<CategorizedVodItem>>(
                VodCategoryBucket.YOUR_LANGUAGES to mutableListOf(),
                VodCategoryBucket.STREAMING_PLATFORMS to mutableListOf(),
                VodCategoryBucket.GENRES_COLLECTIONS to mutableListOf(),
                VodCategoryBucket.OTHER_REGIONS to mutableListOf()
            )

            for (cat in categories) {
                val (languageTag, cleanName) = parseCategoryLanguageAndName(cat.categoryName)
                val bucket = when {
                    languageTag in YOUR_LANGUAGE_TAGS -> VodCategoryBucket.YOUR_LANGUAGES
                    STREAMING_PLATFORM_REGEX.containsMatchIn(cleanName) -> VodCategoryBucket.STREAMING_PLATFORMS
                    GENRE_COLLECTION_REGEX.containsMatchIn(cleanName) -> VodCategoryBucket.GENRES_COLLECTIONS
                    else -> VodCategoryBucket.OTHER_REGIONS
                }
                buckets[bucket]?.add(
                    CategorizedVodItem(
                        category = cat,
                        languageTag = languageTag,
                        cleanName = cleanName,
                        itemCount = counts[cat.categoryId] ?: 0,
                        bucket = bucket
                    )
                )
            }

            val result = LinkedHashMap<VodCategoryBucket, List<CategorizedVodItem>>()
            for ((bucket, items) in buckets) {
                if (items.isEmpty()) continue
                val sorted = when (bucket) {
                    VodCategoryBucket.YOUR_LANGUAGES -> items.sortedWith(
                        compareBy<CategorizedVodItem>(
                            { YOUR_LANGUAGE_PRIORITY[it.languageTag] ?: 99 },
                            { it.cleanName.lowercase(Locale.US) },
                            { it.category.categoryId }
                        )
                    )
                    else -> items.sortedWith(
                        compareBy<CategorizedVodItem>(
                            { it.cleanName.lowercase(Locale.US) },
                            { it.languageTag },
                            { it.category.categoryId }
                        )
                    )
                }
                result[bucket] = sorted
            }
            return result
        }

        internal fun parseCategoryLanguageAndName(rawCategoryName: String): Pair<String, String> {
            val trimmedOriginal = rawCategoryName.trim()
            if (trimmedOriginal.isEmpty()) return "" to ""

            var working = trimmedOriginal.replace(LEADING_DECORATION_REGEX, "").trim()
            var detectedLang = ""

            var guard = 0
            while (guard++ < 3) {
                val sectionMatch = LEADING_SECTION_PREFIX_REGEX.find(working)
                if (sectionMatch != null) {
                    working = working.substring(sectionMatch.range.last + 1).trim()
                    continue
                }
                val bracketMatch = LEADING_BRACKET_LANG_REGEX.find(working)
                if (bracketMatch != null) {
                    val token = bracketMatch.groupValues[1].trim().uppercase(Locale.US)
                    val mapped = CATEGORY_LANGUAGE_CANONICAL_MAP[token]
                    if (mapped != null) {
                        if (detectedLang.isEmpty()) detectedLang = mapped
                        working = working.substring(bracketMatch.range.last + 1).trim()
                        continue
                    }
                }
                val delimMatch = LEADING_DELIM_LANG_REGEX.find(working)
                if (delimMatch != null) {
                    val token = delimMatch.groupValues[1].trim().uppercase(Locale.US)
                    val mapped = CATEGORY_LANGUAGE_CANONICAL_MAP[token]
                    if (mapped != null) {
                        if (detectedLang.isEmpty()) detectedLang = mapped
                        working = working.substring(delimMatch.range.last + 1).trim()
                        continue
                    }
                }
                break
            }

            working = working
                .replace(LEADING_DECORATION_REGEX, "")
                .replace(MULTI_SPACE_REGEX, " ")
                .trim()

            val baseDisplay = working.ifBlank { trimmedOriginal }
            return detectedLang to formatCleanCategoryName(baseDisplay)
        }

        private fun formatCleanCategoryName(text: String): String {
            val hasLower = text.any { it.isLowerCase() }
            val hasUpper = text.any { it.isUpperCase() }
            if (hasLower || !hasUpper) return text

            return text.split(" ").joinToString(" ") { word ->
                if (word.isEmpty()) {
                    word
                } else {
                    val stripped = word.trim('(', ')', '[', ']', '+', '&', '-', '/', ':')
                    val upperStripped = stripped.uppercase(Locale.US)
                    when {
                        upperStripped in UPPERCASE_ACRONYMS -> word.uppercase(Locale.US)
                        upperStripped == "SCI-FI" -> "Sci-Fi"
                        else -> word.lowercase(Locale.US).replaceFirstChar { ch ->
                            if (ch.isLowerCase()) ch.titlecase(Locale.US) else ch.toString()
                        }
                    }
                }
            }
        }
    }
}
