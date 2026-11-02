package com.example.ui.vod

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.StreamVaultDatabase
import com.example.data.model.CategoryEntity
import com.example.data.model.PlaylistEntity
import com.example.data.model.StreamEntity
import com.example.data.remote.SeriesDetails
import com.example.data.repository.IptvRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class VodTab {
    MOVIES,
    SERIES
}

@OptIn(ExperimentalCoroutinesApi::class)
class VodViewModel(application: Application) : AndroidViewModel(application) {
    private val database = StreamVaultDatabase.getInstance(application)
    val repository = IptvRepository(
        playlistDao = database.playlistDao(),
        categoryDao = database.categoryDao(),
        streamDao = database.streamDao(),
        epgDao = database.epgDao(),
        parentalDao = database.parentalSettingsDao()
    )

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

    val movieItems: StateFlow<List<StreamEntity>> = combine(
        repository.activePlaylist,
        _selectedMovieCategoryId,
        _movieSearchQuery
    ) { playlist, catId, query ->
        Triple(playlist?.id, catId, query)
    }.flatMapLatest { (playlistId, catId, query) ->
        if (playlistId == null) {
            flowOf(emptyList())
        } else {
            repository.queryStreamsFast(playlistId, "movie", catId, query)
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

    val seriesItems: StateFlow<List<StreamEntity>> = combine(
        repository.activePlaylist,
        _selectedSeriesCategoryId,
        _seriesSearchQuery
    ) { playlist, catId, query ->
        Triple(playlist?.id, catId, query)
    }.flatMapLatest { (playlistId, catId, query) ->
        if (playlistId == null) {
            flowOf(emptyList())
        } else {
            repository.queryStreamsFast(playlistId, "series", catId, query)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Series Detail Hierarchy State (Season selector chips + Episode cards)
    private val _selectedSeriesStream = MutableStateFlow<StreamEntity?>(null)
    val selectedSeriesStream: StateFlow<StreamEntity?> = _selectedSeriesStream.asStateFlow()

    private val _seriesDetails = MutableStateFlow<SeriesDetails?>(null)
    val seriesDetails: StateFlow<SeriesDetails?> = _seriesDetails.asStateFlow()

    private val _isLoadingSeriesDetails = MutableStateFlow(false)
    val isLoadingSeriesDetails: StateFlow<Boolean> = _isLoadingSeriesDetails.asStateFlow()

    private val _selectedSeasonNumber = MutableStateFlow(1)
    val selectedSeasonNumber: StateFlow<Int> = _selectedSeasonNumber.asStateFlow()

    fun selectMovieCategory(categoryId: String?) {
        _selectedMovieCategoryId.value = categoryId
    }

    fun onMovieSearchQueryChanged(query: String) {
        _movieSearchQuery.value = query
    }

    fun selectSeriesCategory(categoryId: String?) {
        _selectedSeriesCategoryId.value = categoryId
    }

    fun onSeriesSearchQueryChanged(query: String) {
        _seriesSearchQuery.value = query
    }

    fun openSeriesDetails(seriesStream: StreamEntity, playlist: PlaylistEntity) {
        _selectedSeriesStream.value = seriesStream
        _seriesDetails.value = null
        _isLoadingSeriesDetails.value = true
        viewModelScope.launch {
            val details = repository.fetchSeriesDetails(playlist, seriesStream)
            _seriesDetails.value = details
            _selectedSeasonNumber.value = details.seasons.firstOrNull()?.seasonNumber ?: 1
            _isLoadingSeriesDetails.value = false
        }
    }

    fun selectSeason(seasonNumber: Int) {
        _selectedSeasonNumber.value = seasonNumber
    }

    fun closeSeriesDetails() {
        _selectedSeriesStream.value = null
        _seriesDetails.value = null
        _isLoadingSeriesDetails.value = false
    }

    fun toggleFavorite(streamId: Long, currentFav: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(streamId, !currentFav)
            // Keep selectedSeriesStream favorite flag in sync
            _selectedSeriesStream.value?.let { current ->
                if (current.id == streamId) {
                    _selectedSeriesStream.value = current.copy(isFavorite = !currentFav)
                }
            }
        }
    }
}
