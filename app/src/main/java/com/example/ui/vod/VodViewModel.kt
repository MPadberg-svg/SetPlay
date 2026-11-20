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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class VodTab {
    MOVIES,
    SERIES
}

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

@OptIn(ExperimentalCoroutinesApi::class)
class VodViewModel(application: Application) : AndroidViewModel(application) {
    val repository = IptvRepository.getInstance(application)

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
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

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
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val movieCategoryCounts: StateFlow<Map<String, Int>> = repository.activePlaylist
        .flatMapLatest { playlist ->
            if (playlist == null) flowOf(emptyMap()) else repository.getCategoryStreamCounts(playlist.id, "movie")
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

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
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

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
            repository.queryDeduplicatedVodStreams(
                playlistId = params.playlistId,
                streamType = "movie",
                categoryId = params.categoryId,
                query = params.query,
                limit = params.limit
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Independent Series State
    private val _selectedSeriesCategoryId = MutableStateFlow<String?>(null)
    val selectedSeriesCategoryId: StateFlow<String?> = _selectedSeriesCategoryId.asStateFlow()

    private val _seriesSearchQuery = MutableStateFlow("")
    val seriesSearchQuery: StateFlow<String> = _seriesSearchQuery.asStateFlow()

    val seriesCategories: StateFlow<List<CategoryEntity>> = repository.activePlaylist
        .flatMapLatest { playlist ->
            if (playlist == null) flowOf(emptyList()) else repository.getCategories(playlist.id, "SERIES")
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val seriesCategoryCounts: StateFlow<Map<String, Int>> = repository.activePlaylist
        .flatMapLatest { playlist ->
            if (playlist == null) flowOf(emptyMap()) else repository.getCategoryStreamCounts(playlist.id, "series")
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

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
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

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
            repository.queryDeduplicatedVodStreams(
                playlistId = params.playlistId,
                streamType = "series",
                categoryId = params.categoryId,
                query = params.query,
                limit = params.limit
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Bounded SQL Discovery Shelves (LIMIT 20 per shelf; never loads catalog into RAM)
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

    private fun buildDiscoveryShelvesFlow(streamType: String): Flow<List<VodDiscoveryShelf>> {
        val isMovie = streamType == "movie"
        return repository.activePlaylist.flatMapLatest { playlist ->
            if (playlist == null) {
                flowOf(emptyList())
            } else {
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
                                    subtitle = "Rated 6.5+ out of 10",
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

    fun openMovieDetails(movieStream: StreamEntity, playlist: PlaylistEntity?) {
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

        // 1. Load sibling versions from Room
        viewModelScope.launch {
            val versions = repository.getVodVersionsForGroup(
                playlistId = movieStream.playlistId,
                streamType = "movie",
                canonicalGroupKey = movieStream.canonicalGroupKey,
                fallbackStream = movieStream
            )
            if (_selectedMovieStream.value?.id == movieStream.id) {
                _availableMovieVersions.value = versions
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
        openMovieDetails(versionStream, playlist)
    }

    fun closeMovieDetails() {
        _selectedMovieStream.value = null
        _movieDetails.value = null
        _isLoadingMovieDetails.value = false
        _availableMovieVersions.value = emptyList()
        _similarMovies.value = emptyList()
    }

    fun openSeriesDetails(seriesStream: StreamEntity, playlist: PlaylistEntity) {
        val isSameGroup = _selectedSeriesStream.value?.canonicalGroupKey?.isNotBlank() == true &&
            _selectedSeriesStream.value?.canonicalGroupKey == seriesStream.canonicalGroupKey
        _selectedSeriesStream.value = seriesStream
        _seriesDetails.value = null
        _isLoadingSeriesDetails.value = true
        if (!isSameGroup) {
            _availableSeriesVersions.value = listOf(seriesStream)
            _similarSeries.value = emptyList()
        }

        // 1. Load sibling versions from Room
        viewModelScope.launch {
            val versions = repository.getVodVersionsForGroup(
                playlistId = seriesStream.playlistId,
                streamType = "series",
                canonicalGroupKey = seriesStream.canonicalGroupKey,
                fallbackStream = seriesStream
            )
            if (_selectedSeriesStream.value?.id == seriesStream.id) {
                _availableSeriesVersions.value = versions
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
        openSeriesDetails(versionStream, playlist)
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
    }
}
