package com.example.ui.livetv

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.StreamVaultDatabase
import com.example.data.model.CategoryWithCount
import com.example.data.model.PlaylistEntity
import com.example.data.model.StreamEntity
import com.example.data.repository.IptvRepository
import com.example.data.sync.CatalogTaskOrchestrator
import com.example.data.sync.SyncStatus
import com.example.data.sync.SyncTelemetry
import com.example.data.sync.TaskPriority
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class LiveTvViewModel(application: Application) : AndroidViewModel(application) {
    val repository = IptvRepository.getInstance(application)

    // Stage 1: Category Search Query (searches Live TV categories by name)
    private val _categorySearchQuery = MutableStateFlow("")
    val categorySearchQuery: StateFlow<String> = _categorySearchQuery.asStateFlow()

    // Stage 2: Currently selected category (null = Stage 1 Category Directory; non-null = Stage 2 Category Channels)
    private val _selectedCategory = MutableStateFlow<CategoryWithCount?>(null)
    val selectedCategory: StateFlow<CategoryWithCount?> = _selectedCategory.asStateFlow()

    // Stage 2: In-category channel filter query
    private val _channelFilterQuery = MutableStateFlow("")
    val channelFilterQuery: StateFlow<String> = _channelFilterQuery.asStateFlow()

    // Progressive paging limit (starts at 180, resets on category/query change, increments by +180 on scroll)
    val pageSize: Int = PAGE_SIZE
    private val _currentLimit = MutableStateFlow(PAGE_SIZE)
    val currentLimit: StateFlow<Int> = _currentLimit.asStateFlow()
    val limit: StateFlow<Int> = _currentLimit.asStateFlow()

    // Live TV categories StateFlow with debounced search and WhileSubscribed lifecycle pausing
    val categories: StateFlow<List<CategoryWithCount>> = combine(
        repository.activePlaylist,
        _categorySearchQuery.debounce(250L).distinctUntilChanged()
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
        SharingStarted.WhileSubscribed(5_000L),
        emptyList()
    )

    // Channels StateFlow for the selected category with debounced search and progressive paging
    val channels: StateFlow<List<StreamEntity>> = combine(
        repository.activePlaylist,
        _selectedCategory,
        _channelFilterQuery.debounce(250L).distinctUntilChanged(),
        _currentLimit
    ) { playlist, selectedCat, filterQuery, limit ->
        ChannelQueryParams(
            playlistId = playlist?.id,
            selectedCategory = selectedCat,
            filterQuery = filterQuery,
            limit = limit
        )
    }.flatMapLatest { params ->
        if (params.playlistId == null || params.selectedCategory == null) {
            flowOf(emptyList())
        } else {
            val categoryFilterId = if (params.selectedCategory.categoryId == ALL_CHANNELS_CATEGORY_ID) {
                null
            } else {
                params.selectedCategory.categoryId
            }
            repository.searchLiveChannels(
                playlistId = params.playlistId,
                query = params.filterQuery.trim(),
                categoryId = categoryFilterId,
                limit = params.limit
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000L),
        emptyList()
    )

    fun onCategorySearchQueryChanged(query: String) {
        _categorySearchQuery.value = query
    }

    fun selectCategory(category: CategoryWithCount?) {
        _selectedCategory.value = category
        _channelFilterQuery.value = ""
        _currentLimit.value = pageSize
    }

    fun onChannelFilterQueryChanged(query: String) {
        _channelFilterQuery.value = query
        _currentLimit.value = pageSize
    }

    fun loadMore() {
        _currentLimit.value += pageSize
    }

    fun loadNextPage() {
        loadMore()
    }

    // Persistent Live TV sync telemetry (shared architecture with Movies & Series)
    val liveSyncTelemetry: StateFlow<SyncTelemetry> = repository.activePlaylist
        .flatMapLatest { playlist ->
            if (playlist == null) {
                flowOf(SyncTelemetry(sectionType = "LIVE"))
            } else {
                repository.observeSectionSyncState(playlist.id, "LIVE")
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), SyncTelemetry(sectionType = "LIVE"))

    private val _isRefreshingTrigger = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = combine(
        _isRefreshingTrigger,
        liveSyncTelemetry
    ) { trigger, telemetry ->
        trigger || telemetry.isActive
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), false)

    private var livePendingWakePlaylistId: Long = -1L

    init {
        viewModelScope.launch {
            liveSyncTelemetry.collect { telemetry ->
                if (telemetry.status == SyncStatus.PENDING && telemetry.playlistId > 0L) {
                    if (livePendingWakePlaylistId != telemetry.playlistId) {
                        livePendingWakePlaylistId = telemetry.playlistId
                        CatalogTaskOrchestrator.ensureRunnerActive(
                            context = getApplication(),
                            forceWakeIfIdle = false
                        )
                    }
                } else {
                    livePendingWakePlaylistId = -1L
                }
            }
        }
    }

    fun refreshLiveTv(
        playlist: PlaylistEntity? = null,
        categoryId: String? = null,
        force: Boolean = false
    ) {
        val currentStatus = liveSyncTelemetry.value.status
        if (currentStatus == SyncStatus.RUNNING || _isRefreshingTrigger.value) return
        val isResumingInterrupted = currentStatus == SyncStatus.INTERRUPTED
        val isResumingFailed = currentStatus == SyncStatus.FAILED
        val resolvedCatId = if (isResumingInterrupted || isResumingFailed) {
            null
        } else {
            categoryId?.trim()?.takeIf { it.isNotEmpty() && it != ALL_CHANNELS_CATEGORY_ID }
        }
        viewModelScope.launch {
            val target = playlist ?: repository.getActivePlaylistOnce() ?: return@launch
            _isRefreshingTrigger.value = true
            try {
                if (isResumingInterrupted || (isResumingFailed && resolvedCatId == null)) {
                    CatalogTaskOrchestrator.resumeSection(
                        context = getApplication(),
                        playlistId = target.id,
                        sectionType = "LIVE"
                    )
                } else if (resolvedCatId != null) {
                    CatalogTaskOrchestrator.enqueueSection(
                        context = getApplication(),
                        playlistId = target.id,
                        sectionType = "LIVE",
                        priority = TaskPriority.CATEGORY_REFRESH,
                        categoryId = resolvedCatId,
                        forceFull = force
                    )
                } else {
                    CatalogTaskOrchestrator.enqueueSection(
                        context = getApplication(),
                        playlistId = target.id,
                        sectionType = "LIVE",
                        priority = TaskPriority.USER_SECTION_SYNC,
                        categoryId = null,
                        forceFull = force
                    )
                }
            } finally {
                _isRefreshingTrigger.value = false
            }
        }
    }

    fun resumeLiveTv(playlist: PlaylistEntity? = null) {
        if (liveSyncTelemetry.value.status == SyncStatus.RUNNING || _isRefreshingTrigger.value) return
        viewModelScope.launch {
            val target = playlist ?: repository.getActivePlaylistOnce() ?: return@launch
            _isRefreshingTrigger.value = true
            try {
                CatalogTaskOrchestrator.resumeSection(
                    context = getApplication(),
                    playlistId = target.id,
                    sectionType = "LIVE"
                )
            } finally {
                _isRefreshingTrigger.value = false
            }
        }
    }

    fun dismissPendingUpdate(playlist: PlaylistEntity? = null) {
        viewModelScope.launch {
            val target = playlist ?: repository.getActivePlaylistOnce() ?: return@launch
            repository.markPendingUpdate(target.id, "LIVE", "")
        }
    }

    fun toggleFavorite(streamId: Long, currentFav: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(streamId, !currentFav)
        }
    }

    private data class ChannelQueryParams(
        val playlistId: Long?,
        val selectedCategory: CategoryWithCount?,
        val filterQuery: String,
        val limit: Int
    )

    companion object {
        const val PAGE_SIZE = 180
        const val ALL_CHANNELS_CATEGORY_ID = "__ALL_LIVE_CHANNELS__"
    }
}
