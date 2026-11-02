package com.example.ui.livetv

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.StreamVaultDatabase
import com.example.data.model.CategoryWithCount
import com.example.data.model.StreamEntity
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

@OptIn(ExperimentalCoroutinesApi::class)
class LiveTvViewModel(application: Application) : AndroidViewModel(application) {
    private val database = StreamVaultDatabase.getInstance(application)
    val repository = IptvRepository(
        playlistDao = database.playlistDao(),
        categoryDao = database.categoryDao(),
        streamDao = database.streamDao(),
        epgDao = database.epgDao(),
        parentalDao = database.parentalSettingsDao()
    )

    // Stage 1: Category Search Query (searches Live TV categories by name)
    private val _categorySearchQuery = MutableStateFlow("")
    val categorySearchQuery: StateFlow<String> = _categorySearchQuery.asStateFlow()

    // Stage 2: Currently selected category (null = Stage 1 Category Directory; non-null = Stage 2 Category Channels)
    private val _selectedCategory = MutableStateFlow<CategoryWithCount?>(null)
    val selectedCategory: StateFlow<CategoryWithCount?> = _selectedCategory.asStateFlow()

    // Stage 2: In-category channel filter query
    private val _channelFilterQuery = MutableStateFlow("")
    val channelFilterQuery: StateFlow<String> = _channelFilterQuery.asStateFlow()

    // Pre-warmed, persistent StateFlow of Live TV categories so switching tabs never recreates flows or flashes empty
    val categories: StateFlow<List<CategoryWithCount>> = combine(
        repository.activePlaylist,
        _categorySearchQuery
    ) { playlist, query ->
        Pair(playlist?.id, query)
    }.flatMapLatest { (playlistId, query) ->
        if (playlistId == null) {
            flowOf(emptyList())
        } else {
            repository.searchCategoriesWithCounts(playlistId, "LIVE", query)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    // Pre-warmed, persistent StateFlow of channels for the selected category
    val channels: StateFlow<List<StreamEntity>> = combine(
        repository.activePlaylist,
        _selectedCategory,
        _channelFilterQuery
    ) { playlist, selectedCat, filterQuery ->
        Triple(playlist?.id, selectedCat, filterQuery)
    }.flatMapLatest { (playlistId, selectedCat, filterQuery) ->
        if (playlistId == null || selectedCat == null) {
            flowOf(emptyList())
        } else {
            val categoryFilterId = if (selectedCat.categoryId == ALL_CHANNELS_CATEGORY_ID) null else selectedCat.categoryId
            repository.searchLiveChannels(playlistId, filterQuery.trim(), categoryFilterId)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    fun onCategorySearchQueryChanged(query: String) {
        _categorySearchQuery.value = query
    }

    fun selectCategory(category: CategoryWithCount?) {
        _selectedCategory.value = category
        _channelFilterQuery.value = ""
    }

    fun onChannelFilterQueryChanged(query: String) {
        _channelFilterQuery.value = query
    }

    fun toggleFavorite(streamId: Long, currentFav: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(streamId, !currentFav)
        }
    }

    companion object {
        const val ALL_CHANNELS_CATEGORY_ID = "__ALL_LIVE_CHANNELS__"
    }
}
