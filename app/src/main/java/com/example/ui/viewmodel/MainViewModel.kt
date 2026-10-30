package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.StreamVaultDatabase
import com.example.data.model.PlaylistEntity
import com.example.data.model.StreamEntity
import com.example.data.repository.IptvRepository
import com.example.data.repository.SyncProgress
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    LIVE_TV,
    MOVIES,
    SERIES,
    FAVORITES,
    EPG,
    GLOBAL_SEARCH,
    PLAYLISTS,
    PARENTAL_SETTINGS
}

sealed class ConnectionTestState {
    data object Idle : ConnectionTestState()
    data object Testing : ConnectionTestState()
    data class Success(
        val status: String,
        val expDate: String,
        val activeCons: String,
        val maxConnections: String,
        val serverProtocol: String
    ) : ConnectionTestState()
    data class Error(val message: String) : ConnectionTestState()
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = StreamVaultDatabase.getInstance(application)
    val repository = IptvRepository(
        playlistDao = database.playlistDao(),
        categoryDao = database.categoryDao(),
        streamDao = database.streamDao(),
        epgDao = database.epgDao(),
        parentalDao = database.parentalSettingsDao()
    )

    val allPlaylists: StateFlow<List<PlaylistEntity>> = repository.allPlaylists.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    val activePlaylist: StateFlow<PlaylistEntity?> = repository.activePlaylist.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    val recentlyWatched: StateFlow<List<StreamEntity>> = activePlaylist.flatMapLatest { playlist ->
        if (playlist == null) flowOf(emptyList()) else repository.getRecentlyWatched(playlist.id)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    val favorites: StateFlow<List<StreamEntity>> = activePlaylist.flatMapLatest { playlist ->
        if (playlist == null) flowOf(emptyList()) else repository.getFavoriteStreams(playlist.id)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    val featuredLiveStreams: StateFlow<List<StreamEntity>> = activePlaylist.flatMapLatest { playlist ->
        if (playlist == null) flowOf(emptyList())
        else repository.queryStreamsFast(playlist.id, "live", null, "").map { it.take(20) }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    val featuredMovies: StateFlow<List<StreamEntity>> = activePlaylist.flatMapLatest { playlist ->
        if (playlist == null) flowOf(emptyList())
        else repository.queryStreamsFast(playlist.id, "movie", null, "").map { it.take(20) }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    val featuredSeries: StateFlow<List<StreamEntity>> = activePlaylist.flatMapLatest { playlist ->
        if (playlist == null) flowOf(emptyList())
        else repository.queryStreamsFast(playlist.id, "series", null, "").map { it.take(20) }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    val syncProgress: StateFlow<SyncProgress> = repository.syncProgress

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Parental PIN authorization session state (cleared when lock resets)
    private val _isParentalUnlockedThisSession = MutableStateFlow(false)
    val isParentalUnlockedThisSession: StateFlow<Boolean> = _isParentalUnlockedThisSession.asStateFlow()

    // Connection tester state for Add/Edit/Hero Dashboard
    private val _connectionTestState = MutableStateFlow<ConnectionTestState>(ConnectionTestState.Idle)
    val connectionTestState: StateFlow<ConnectionTestState> = _connectionTestState.asStateFlow()

    fun setScreen(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun unlockParentalSession() {
        _isParentalUnlockedThisSession.value = true
    }

    fun lockParentalSession() {
        _isParentalUnlockedThisSession.value = false
    }

    fun clearConnectionTestState() {
        _connectionTestState.value = ConnectionTestState.Idle
    }

    fun testConnection(serverUrl: String, username: String, password: String) {
        if (serverUrl.isBlank() || username.isBlank() || password.isBlank()) {
            _connectionTestState.value = ConnectionTestState.Error("Please fill in Server URL, Username, and Password.")
            return
        }
        viewModelScope.launch {
            _connectionTestState.value = ConnectionTestState.Testing
            val result = repository.testAccountConnection(serverUrl, username, password)
            if (result.isSuccess) {
                val auth = result.getOrNull()
                val userInfo = auth?.userInfo
                val serverInfo = auth?.serverInfo
                _connectionTestState.value = ConnectionTestState.Success(
                    status = userInfo?.status ?: "Active",
                    expDate = userInfo?.expDate ?: "",
                    activeCons = userInfo?.activeCons ?: "0",
                    maxConnections = userInfo?.maxConnections ?: "1",
                    serverProtocol = (serverInfo?.serverProtocol ?: if (serverUrl.startsWith("https", true)) "https" else "http").uppercase()
                )
            } else {
                _connectionTestState.value = ConnectionTestState.Error(
                    result.exceptionOrNull()?.message ?: "Unable to connect to Xtream server"
                )
            }
        }
    }

    fun syncActiveAccount() {
        viewModelScope.launch {
            val playlist = repository.getActivePlaylistOnce() ?: return@launch
            repository.syncPlaylist(playlist)
        }
    }

    fun syncSpecificPlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.setActivePlaylist(playlistId)
            val playlist = repository.getPlaylistById(playlistId) ?: return@launch
            repository.syncPlaylist(playlist)
        }
    }

    fun addCustomPlaylist(name: String, url: String, user: String, pass: String) {
        viewModelScope.launch {
            val newPlaylist = PlaylistEntity(
                name = name.trim().ifBlank { "Xtream Account" },
                serverUrl = url.trim(),
                username = user.trim(),
                password = pass.trim(),
                isActive = true
            )
            val id = repository.addOrUpdatePlaylist(newPlaylist)
            repository.setActivePlaylist(id)
            _connectionTestState.value = ConnectionTestState.Idle
            val inserted = repository.getPlaylistById(id)
            if (inserted != null) {
                repository.syncPlaylist(inserted)
            }
        }
    }

    fun updatePlaylist(id: Long, name: String, url: String, user: String, pass: String, syncAfterUpdate: Boolean) {
        viewModelScope.launch {
            repository.updatePlaylistCredentials(id, name, url, user, pass)
            _connectionTestState.value = ConnectionTestState.Idle
            if (syncAfterUpdate) {
                val updated = repository.getPlaylistById(id)
                if (updated != null) {
                    repository.syncPlaylist(updated)
                }
            }
        }
    }

    fun switchPlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.setActivePlaylist(playlistId)
            _connectionTestState.value = ConnectionTestState.Idle
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            val wasActive = repository.getActivePlaylistOnce()?.id == playlistId
            repository.deletePlaylist(playlistId)
            if (wasActive) {
                val remaining = allPlaylists.value.filter { it.id != playlistId }
                if (remaining.isNotEmpty()) {
                    repository.setActivePlaylist(remaining.first().id)
                }
            }
        }
    }
}
