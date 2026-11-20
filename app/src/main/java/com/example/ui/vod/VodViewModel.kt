package com.example.ui.vod

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CategoryEntity
import com.example.data.model.GroupedVodStream
import com.example.data.model.PlaylistEntity
import com.example.data.model.StreamEntity
import com.example.data.remote.SeriesDetails
import com.example.data.remote.VodMovieDetails
import com.example.data.repository.EpisodeResumeInfo
import com.example.data.repository.IptvRepository
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

data class VodDiscoveryShelf(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val items: List<GroupedVodStream>
)

data class VodSyncBannerState(
    val isSyncing: Boolean = false,
    val statusText: String = "",
    val isError: Boolean = false,
    val isCompleted: Boolean = false
)

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

    // Materialized <5ms Discovery Shelves (LIMIT 20 per shelf; paused during active sync to prevent stutter)
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

    // IMAX Movie Dossier State (Xtream get_vod_info with instant fallback, Versions & Similar Movies)
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

    // Pull-to-Refresh & Sync Banner Telemetry
    private val _isRefreshingMovies = MutableStateFlow(false)
    val isRefreshingMovies: StateFlow<Boolean> = _isRefreshingMovies.asStateFlow()

    private val _isRefreshingSeries = MutableStateFlow(false)
    val isRefreshingSeries: StateFlow<Boolean> = _isRefreshingSeries.asStateFlow()

    private val _vodSyncBannerState = MutableStateFlow(VodSyncBannerState())
    val vodSyncBannerState: StateFlow<VodSyncBannerState> = _vodSyncBannerState.asStateFlow()

    private var lastMoviesRefreshMs: Long = 0L
    private var lastSeriesRefreshMs: Long = 0L
    private var bannerAutoClearJob: Job? = null

    init {
        viewModelScope.launch {
            repository.syncProgress.collect { progress ->
                when {
                    progress.isSyncing -> {
                        bannerAutoClearJob?.cancel()
                        _vodSyncBannerState.value = VodSyncBannerState(
                            isSyncing = true,
                            statusText = progress.phase.ifBlank { "Syncing catalog…" },
                            isError = false,
                            isCompleted = false
                        )
                    }
                    !progress.error.isNullOrBlank() -> {
                        bannerAutoClearJob?.cancel()
                        _vodSyncBannerState.value = VodSyncBannerState(
                            isSyncing = false,
                            statusText = progress.error,
                            isError = true,
                            isCompleted = false
                        )
                    }
                    progress.phase.startsWith("Complete", ignoreCase = true) -> {
                        showTransientCompletionBanner("Catalog updated")
                    }
                }
            }
        }
    }

    private fun showTransientCompletionBanner(message: String, durationMs: Long = 3_000L) {
        bannerAutoClearJob?.cancel()
        _vodSyncBannerState.value = VodSyncBannerState(
            isSyncing = false,
            statusText = message,
            isError = false,
            isCompleted = true
        )
        bannerAutoClearJob = viewModelScope.launch {
            delay(durationMs)
            if (_vodSyncBannerState.value.isCompleted && !_vodSyncBannerState.value.isSyncing) {
                _vodSyncBannerState.value = VodSyncBannerState()
            }
        }
    }

    fun dismissSyncBanner() {
        bannerAutoClearJob?.cancel()
        _vodSyncBannerState.value = VodSyncBannerState()
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
        if (repository.syncProgress.value.isSyncing) return
        if (isMovies && _isRefreshingMovies.value) return
        if (!isMovies && _isRefreshingSeries.value) return

        val recoveryKey = "${targetPlaylist.id}_$streamType"
        if (!autoSectionRecoveryGuard.add(recoveryKey)) return

        viewModelScope.launch {
            try {
                repository.ensureVodCatalogIndexed(targetPlaylist.id, streamType)
                val rawCount = repository.countStreamsByType(targetPlaylist.id, streamType)
                if (rawCount == 0 && !repository.syncProgress.value.isSyncing) {
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

    private fun buildDiscoveryShelvesFlow(streamType: String): Flow<List<VodDiscoveryShelf>> {
        val isMovie = streamType == "movie"
        val isSyncingFlow = repository.syncProgress
            .map { it.isSyncing }
            .distinctUntilChanged()

        return combine(repository.activePlaylist, isSyncingFlow) { playlist, isSyncing ->
            playlist to isSyncing
        }.flatMapLatest { (playlist, isSyncing) ->
            when {
                playlist == null -> flowOf(emptyList())
                isSyncing -> emptyFlow()
                else -> {
                    triggerLazyIndexIfNeeded(playlist.id, streamType)
                    val recentFlow = repository.getRecentlyAddedVod(playlist.id, streamType, limit = DISCOVERY_SHELF_LIMIT)
                    val topRatedFlow = repository.getTopRatedVod(playlist.id, streamType, limit = DISCOVERY_SHELF_LIMIT)
                    val recommendedFlow = repository.getRecommendedVodForUser(playlist.id, streamType, limit = DISCOVERY_SHELF_LIMIT)
                    val genreFlows: List<Flow<Pair<String, List<GroupedVodStream>>>> = CURATED_GENRE_KEYWORDS.map { keyword ->
                        repository.getGenreShelfVod(playlist.id, streamType, keyword, limit = DISCOVERY_SHELF_LIMIT)
                            .map { items -> keyword to items }
                    }
                    val combinedGenresFlow: Flow<List<Pair<String, List<GroupedVodStream>>>> = combine(genreFlows) { array ->
                        array.toList()
                    }

                    combine(recentFlow, topRatedFlow, recommendedFlow, combinedGenresFlow) { recent, topRated, recommended, genrePairs ->
                        buildList {
                            if (recent.isNotEmpty()) {
                                add(
                                    VodDiscoveryShelf(
                                        id = "${streamType}_recently_added",
                                        title = "Recently Added",
                                        subtitle = if (isMovie) "Newest arrivals in your library" else "Latest series additions",
                                        items = recent
                                    )
                                )
                            }
                            if (topRated.isNotEmpty()) {
                                add(
                                    VodDiscoveryShelf(
                                        id = "${streamType}_top_rated",
                                        title = if (isMovie) "Top Rated Cinema" else "Acclaimed Series",
                                        subtitle = "Most popular & highest rated",
                                        items = topRated
                                    )
                                )
                            }
                            if (recommended.isNotEmpty()) {
                                add(
                                    VodDiscoveryShelf(
                                        id = "${streamType}_recommended",
                                        title = "Recommended for You",
                                        subtitle = "Based on your favorites & watch history",
                                        items = recommended
                                    )
                                )
                            }
                            for ((keyword, items) in genrePairs) {
                                if (items.size >= MIN_GENRE_SHELF_ITEMS) {
                                    add(
                                        VodDiscoveryShelf(
                                            id = "${streamType}_genre_${keyword.lowercase()}",
                                            title = keyword,
                                            subtitle = if (isMovie) "$keyword Cinema" else "$keyword Series",
                                            items = items
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
        if (_isRefreshingMovies.value) return
        val now = System.currentTimeMillis()
        if (!force && lastMoviesRefreshMs > 0L && (now - lastMoviesRefreshMs) < REFRESH_COOLDOWN_MS) {
            showTransientCompletionBanner("Catalog is up to date", durationMs = 2_000L)
            return
        }
        viewModelScope.launch {
            val target = playlist ?: repository.getActivePlaylistOnce() ?: return@launch
            _isRefreshingMovies.value = true
            try {
                val result = repository.syncPlaylistSection(target, "MOVIE")
                if (result.isSuccess) {
                    lastMoviesRefreshMs = System.currentTimeMillis()
                }
            } finally {
                _isRefreshingMovies.value = false
            }
        }
    }

    fun refreshSeries(playlist: PlaylistEntity? = null, force: Boolean = false) {
        if (_isRefreshingSeries.value) return
        val now = System.currentTimeMillis()
        if (!force && lastSeriesRefreshMs > 0L && (now - lastSeriesRefreshMs) < REFRESH_COOLDOWN_MS) {
            showTransientCompletionBanner("Catalog is up to date", durationMs = 2_000L)
            return
        }
        viewModelScope.launch {
            val target = playlist ?: repository.getActivePlaylistOnce() ?: return@launch
            _isRefreshingSeries.value = true
            try {
                val result = repository.syncPlaylistSection(target, "SERIES")
                if (result.isSuccess) {
                    lastSeriesRefreshMs = System.currentTimeMillis()
                }
            } finally {
                _isRefreshingSeries.value = false
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

    private data class VodQueryParams(
        val playlistId: Long?,
        val categoryId: String?,
        val query: String,
        val limit: Int
    )

    companion object {
        const val PAGE_SIZE = 180
        const val REFRESH_COOLDOWN_MS = 30_000L
        private const val DISCOVERY_SHELF_LIMIT = 20
        private const val SIMILAR_TITLES_LIMIT = 15
        private const val MIN_GENRE_SHELF_ITEMS = 3
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
