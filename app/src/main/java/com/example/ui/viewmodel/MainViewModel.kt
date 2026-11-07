package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.StreamVaultDatabase
import com.example.data.model.FavoriteTeamEntity
import com.example.data.model.PlaylistEntity
import com.example.data.model.StreamEntity
import com.example.data.model.TournamentPreferenceEntity
import com.example.data.repository.FootballMatch
import com.example.data.repository.FootballRepository
import com.example.data.repository.FootballTeam
import com.example.data.repository.IptvRepository
import com.example.data.repository.LiveScoreSyncState
import com.example.data.repository.MatchDayTab
import com.example.data.repository.MatchFullDossier
import com.example.data.repository.MatchStatus
import com.example.data.repository.SeasonTimeMode
import com.example.data.repository.SyncProgress
import com.example.data.repository.UserCountryZone
import com.example.data.repository.UserZoneRegistry
import com.example.data.sync.RemoteControlCommand
import com.example.data.sync.RemoteNavMirrorCommand
import com.example.data.sync.RemotePlayCommand
import com.example.data.sync.SyncRole
import com.example.data.sync.TvSyncManager
import com.example.data.sync.TvSyncSessionState
import com.example.data.sync.VaultSyncPayload
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class AppScreen {
    HOME,
    LIVE_TV,
    MOVIES,
    SERIES,
    FAVORITES,
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

data class BeinChannelMatcherState(
    val match: FootballMatch,
    val favoriteMatchedStreams: List<StreamEntity> = emptyList(),
    val livePlaylistCandidates: List<StreamEntity> = emptyList(),
    val matchedStreams: List<StreamEntity> = emptyList(),
    val resolvedChannelLabel: String = match.beinChannel,
    val resolvedProviderName: String = match.menaProviderName,
    val usedMenaFallbackProvider: Boolean = false,
    val availableMenaChannelOptions: List<String> = (listOf(match.beinChannel) + match.fallbackMenaChannels).distinct(),
    val optionsAvailableInFavorites: Set<String> = emptySet(),
    val requiresAddToFavoritesPrompt: Boolean = false,
    val missingFavoriteNotificationMessage: String =
        "\"${match.beinChannel}\" is not in your Favorites yet. Please test and add \"${match.beinChannel}\" to your Favorites first to watch this game.",
    val isLoading: Boolean = false
)

data class LiveGoalAlertEvent(
    val alertId: String,
    val match: FootballMatch,
    val scoringTeamName: String,
    val homeTeamName: String,
    val awayTeamName: String,
    val homeScore: Int,
    val awayScore: Int,
    val minuteLabel: String,
    val isFavoriteTeamGoal: Boolean,
    val timestampMs: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = StreamVaultDatabase.getInstance(application)
    val repository = IptvRepository.getInstance(application)

    val footballRepository = FootballRepository(
        context = application,
        favoriteTeamDao = database.favoriteTeamDao(),
        tournamentPreferenceDao = database.tournamentPreferenceDao(),
        streamDao = database.streamDao()
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

    // =========================================================================
    // KickOff Arena State (USER's Zone, 5-Day Matchday Timeline, Broadcasters & Favorites Matcher)
    // =========================================================================
    private val _selectedCountryZone = MutableStateFlow(footballRepository.getSelectedCountryZone())
    val selectedCountryZone: StateFlow<UserCountryZone> = _selectedCountryZone.asStateFlow()

    private val _selectedSeasonTimeMode = MutableStateFlow(footballRepository.getSelectedSeasonTimeMode())
    val selectedSeasonTimeMode: StateFlow<SeasonTimeMode> = _selectedSeasonTimeMode.asStateFlow()

    private val _showCountryZoneModal = MutableStateFlow(false)
    val showCountryZoneModal: StateFlow<Boolean> = _showCountryZoneModal.asStateFlow()

    val favoriteTeams: StateFlow<List<FavoriteTeamEntity>> =
        footballRepository.favoriteTeamsFlow.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    val tournamentPreferences: StateFlow<List<TournamentPreferenceEntity>> =
        footballRepository.tournamentPreferencesFlow.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    private val _showFirstTimeTeamsOnboarding = MutableStateFlow(!footballRepository.hasCompletedTeamsOnboarding())
    val showFirstTimeTeamsOnboarding: StateFlow<Boolean> = _showFirstTimeTeamsOnboarding.asStateFlow()

    private val _showCustomizeTeamsModal = MutableStateFlow(false)
    val showCustomizeTeamsModal: StateFlow<Boolean> = _showCustomizeTeamsModal.asStateFlow()

    private val _selectedMatchDay = MutableStateFlow(MatchDayTab.TODAY)
    val selectedMatchDay: StateFlow<MatchDayTab> = _selectedMatchDay.asStateFlow()

    private val _selectedTournamentFilter = MutableStateFlow("ALL")
    val selectedTournamentFilter: StateFlow<String> = _selectedTournamentFilter.asStateFlow()

    private val _collapsedTournamentIds = MutableStateFlow<Set<String>>(emptySet())
    val collapsedTournamentIds: StateFlow<Set<String>> = _collapsedTournamentIds.asStateFlow()

    private val _matchesByDay = MutableStateFlow<Map<MatchDayTab, List<FootballMatch>>>(emptyMap())
    val matchesByDay: StateFlow<Map<MatchDayTab, List<FootballMatch>>> = _matchesByDay.asStateFlow()

    private val _todayMatches = MutableStateFlow<List<FootballMatch>>(emptyList())
    val todayMatches: StateFlow<List<FootballMatch>> = _todayMatches.asStateFlow()

    private val _tomorrowMatches = MutableStateFlow<List<FootballMatch>>(emptyList())
    val tomorrowMatches: StateFlow<List<FootballMatch>> = _tomorrowMatches.asStateFlow()

    private val _isLoadingMatches = MutableStateFlow(false)
    val isLoadingMatches: StateFlow<Boolean> = _isLoadingMatches.asStateFlow()

    private val _teamSearchQuery = MutableStateFlow("")
    val teamSearchQuery: StateFlow<String> = _teamSearchQuery.asStateFlow()

    private val _teamSearchResults = MutableStateFlow<List<FootballTeam>>(emptyList())
    val teamSearchResults: StateFlow<List<FootballTeam>> = _teamSearchResults.asStateFlow()

    private val _isSearchingTeams = MutableStateFlow(false)
    val isSearchingTeams: StateFlow<Boolean> = _isSearchingTeams.asStateFlow()

    private val _beinChannelMatcherState = MutableStateFlow<BeinChannelMatcherState?>(null)
    val beinChannelMatcherState: StateFlow<BeinChannelMatcherState?> = _beinChannelMatcherState.asStateFlow()

    private val _selectedMatchDossier = MutableStateFlow<MatchFullDossier?>(null)
    val selectedMatchDossier: StateFlow<MatchFullDossier?> = _selectedMatchDossier.asStateFlow()

    private val _isLoadingMatchDossier = MutableStateFlow(false)
    val isLoadingMatchDossier: StateFlow<Boolean> = _isLoadingMatchDossier.asStateFlow()

    private val _liveScoreSyncState = MutableStateFlow(LiveScoreSyncState())
    val liveScoreSyncState: StateFlow<LiveScoreSyncState> = _liveScoreSyncState.asStateFlow()

    // In-Player Live Goal Alert State (auto-dismissed after 5 seconds)
    private val _activeGoalAlert = MutableStateFlow<LiveGoalAlertEvent?>(null)
    val activeGoalAlert: StateFlow<LiveGoalAlertEvent?> = _activeGoalAlert.asStateFlow()

    // Local Wi-Fi Phone ↔ TV QR Companion & Sync Engine
    val tvSyncManager = TvSyncManager(application)
    val tvSyncState: StateFlow<TvSyncSessionState> = tvSyncManager.sessionState

    private val _tvSyncErrorMessage = MutableStateFlow<String?>(null)
    val tvSyncErrorMessage: StateFlow<String?> = _tvSyncErrorMessage.asStateFlow()

    @Volatile
    var isTvInFullScreenPlayback: Boolean = false

    private var teamSearchJob: Job? = null
    private var livePollingJob: Job? = null
    private var goalAlertDismissJob: Job? = null

    init {
        tvSyncManager.onVaultPayloadReceived = { incomingVault ->
            applyIncomingVaultPayload(incomingVault)
            buildCurrentVaultSyncPayload()
        }
        viewModelScope.launch {
            tvSyncManager.incomingNavCommands.collect { cmd ->
                if (tvSyncState.value.role == SyncRole.TV_HOST && !isTvInFullScreenPlayback) {
                    runCatching { AppScreen.valueOf(cmd.screenName) }.getOrNull()?.let { screen ->
                        _currentScreen.value = screen
                    }
                    cmd.matchDayName?.let { dayName ->
                        runCatching { MatchDayTab.valueOf(dayName) }.getOrNull()?.let { tab ->
                            if (_selectedMatchDay.value != tab) {
                                _selectedMatchDay.value = tab
                            }
                        }
                    }
                }
            }
        }
        viewModelScope.launch {
            footballRepository.ensureDefaultTournamentsInitialized()
            refreshFootballSchedules()
            startConditionalLivePolling()
        }
    }

    /**
     * Checks whether any match in the provided list is currently in-play (`LIVE` or `HALFTIME`)
     * OR is an `UPCOMING` match on `TODAY` within the active kickoff window (-10 min to +140 min in the user's Country Zone time)
     * so upcoming games automatically transition to `LIVE` as soon as they kick off without manual refresh.
     */
    private fun hasLiveOrImminentKickoffMatches(
        matches: List<FootballMatch>,
        dayTab: MatchDayTab
    ): Boolean {
        if (matches.any { it.status == MatchStatus.LIVE || it.status == MatchStatus.HALFTIME }) {
            return true
        }
        if (dayTab != MatchDayTab.TODAY) return false

        val zoneTz = runCatching {
            _selectedCountryZone.value.getEffectiveTimeZone(_selectedSeasonTimeMode.value)
        }.getOrElse { TimeZone.getTimeZone("Africa/Casablanca") }
        val nowCal = Calendar.getInstance(zoneTz)
        val nowMinutes = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE)

        return matches.any { match ->
            if (match.status != MatchStatus.UPCOMING) return@any false
            val parts = match.kickoffTimeFormatted.trim().split(":")
            val h = parts.getOrNull(0)?.filter { it.isDigit() }?.toIntOrNull() ?: return@any false
            val m = parts.getOrNull(1)?.take(2)?.filter { it.isDigit() }?.toIntOrNull() ?: return@any false
            val koMinutes = h * 60 + m
            val diff = nowMinutes - koMinutes
            diff in -10..140
        }
    }

    private fun formatCurrentZoneSyncTime(epochMs: Long): String {
        val zoneTz = runCatching {
            _selectedCountryZone.value.getEffectiveTimeZone(_selectedSeasonTimeMode.value, epochMs)
        }.getOrElse { TimeZone.getTimeZone("Africa/Casablanca") }
        return SimpleDateFormat("HH:mm:ss", Locale.US).apply {
            timeZone = zoneTz
        }.format(Date(epochMs))
    }

    /**
     * Compares `previousMatches` and `updatedMatches` on `TODAY` to detect live score increments.
     * Prioritizes matches involving a user's Favorite Team first, or any live match on `TODAY`.
     */
    internal fun detectGoalScoredAlert(
        previousMatches: List<FootballMatch>,
        updatedMatches: List<FootballMatch>,
        favoriteTeamIds: Set<String> = favoriteTeams.value.map { it.teamId.lowercase(Locale.ROOT) }.toSet(),
        favoriteTeamNames: Set<String> = favoriteTeams.value.map { it.teamName.lowercase(Locale.ROOT) }.toSet()
    ): LiveGoalAlertEvent? {
        if (previousMatches.isEmpty() || updatedMatches.isEmpty()) return null

        val prevById = previousMatches.associateBy { it.matchId }
        val detectedAlerts = mutableListOf<LiveGoalAlertEvent>()

        for (updated in updatedMatches) {
            val prev = prevById[updated.matchId] ?: previousMatches.find {
                it.homeTeamName.equals(updated.homeTeamName, ignoreCase = true) &&
                    it.awayTeamName.equals(updated.awayTeamName, ignoreCase = true)
            } ?: continue

            val newHome = updated.homeScore ?: continue
            val newAway = updated.awayScore ?: continue
            val oldHome = prev.homeScore ?: 0
            val oldAway = prev.awayScore ?: 0

            val homeIncremented = newHome > oldHome
            val awayIncremented = newAway > oldAway
            if (!homeIncremented && !awayIncremented) continue

            val isFavMatch = updated.homeTeamId.lowercase(Locale.ROOT) in favoriteTeamIds ||
                updated.awayTeamId.lowercase(Locale.ROOT) in favoriteTeamIds ||
                updated.homeTeamName.lowercase(Locale.ROOT) in favoriteTeamNames ||
                updated.awayTeamName.lowercase(Locale.ROOT) in favoriteTeamNames

            val scoringTeam = if (homeIncremented) updated.homeTeamName else updated.awayTeamName
            detectedAlerts.add(
                LiveGoalAlertEvent(
                    alertId = "${updated.matchId}_${newHome}_${newAway}",
                    match = updated,
                    scoringTeamName = scoringTeam,
                    homeTeamName = updated.homeTeamName,
                    awayTeamName = updated.awayTeamName,
                    homeScore = newHome,
                    awayScore = newAway,
                    minuteLabel = updated.liveMinuteOrBadge.ifBlank { "LIVE" },
                    isFavoriteTeamGoal = isFavMatch
                )
            )
        }

        return detectedAlerts.sortedByDescending { it.isFavoriteTeamGoal }.firstOrNull()
    }

    fun triggerGoalAlert(alert: LiveGoalAlertEvent) {
        _activeGoalAlert.value = alert
        goalAlertDismissJob?.cancel()
        goalAlertDismissJob = viewModelScope.launch {
            delay(5000L)
            if (_activeGoalAlert.value?.alertId == alert.alertId) {
                _activeGoalAlert.value = null
            }
        }
    }

    fun dismissGoalAlert() {
        goalAlertDismissJob?.cancel()
        _activeGoalAlert.value = null
    }

    /**
     * Background SportAPI.ai Live Score & Match Minute Polling Engine:
     * - Automatically syncs live scores (`homeScore`, `awayScore`, `halfTimeScore`, `fullTimeScore`)
     *   and match minute clocks (`1H • 38'`, `HT`, `2H • 67'`, `90+3'`, `FT`) in the background without user refreshing.
     * - Emits `LiveGoalAlertEvent` whenever a match on `TODAY` increments `homeScore` or `awayScore`.
     */
    private fun startConditionalLivePolling() {
        livePollingJob?.cancel()
        livePollingJob = viewModelScope.launch {
            while (true) {
                val activeDay = _selectedMatchDay.value
                val todayList = _matchesByDay.value[MatchDayTab.TODAY].orEmpty()
                val activeDayList = _matchesByDay.value[activeDay].orEmpty()

                val isFastLiveWindow = hasLiveOrImminentKickoffMatches(todayList, MatchDayTab.TODAY) ||
                    hasLiveOrImminentKickoffMatches(activeDayList, activeDay)

                val pollIntervalMs = if (isFastLiveWindow) 30_000L else 60_000L
                _liveScoreSyncState.value = _liveScoreSyncState.value.copy(
                    isPollingActive = _currentScreen.value == AppScreen.HOME || isFastLiveWindow,
                    activeLiveMatchesCount = todayList.count {
                        it.status == MatchStatus.LIVE || it.status == MatchStatus.HALFTIME
                    }
                )

                delay(pollIntervalMs)

                val currentActiveDay = _selectedMatchDay.value
                val shouldPollToday = isFastLiveWindow || currentActiveDay == MatchDayTab.TODAY
                val shouldPollSelectedDay = _currentScreen.value == AppScreen.HOME &&
                    currentActiveDay != MatchDayTab.TODAY &&
                    hasLiveOrImminentKickoffMatches(_matchesByDay.value[currentActiveDay].orEmpty(), currentActiveDay)

                if (!shouldPollToday && !shouldPollSelectedDay) continue

                _liveScoreSyncState.value = _liveScoreSyncState.value.copy(
                    isPollingActive = true,
                    isSyncingDelta = true
                )

                try {
                    val updatedMap = _matchesByDay.value.toMutableMap()

                    if (shouldPollToday) {
                        val existingToday = updatedMap[MatchDayTab.TODAY].orEmpty()
                        val refreshedToday = footballRepository.pollLiveScoresAndMinutes(
                            dayTab = MatchDayTab.TODAY,
                            existingMatches = existingToday
                        )
                        val goalAlert = detectGoalScoredAlert(
                            previousMatches = existingToday,
                            updatedMatches = refreshedToday
                        )
                        if (goalAlert != null) {
                            triggerGoalAlert(goalAlert)
                        }
                        updatedMap[MatchDayTab.TODAY] = refreshedToday
                        _todayMatches.value = refreshedToday
                    }

                    if (shouldPollSelectedDay) {
                        val existingSelected = updatedMap[currentActiveDay].orEmpty()
                        val refreshedSelected = footballRepository.pollLiveScoresAndMinutes(
                            dayTab = currentActiveDay,
                            existingMatches = existingSelected
                        )
                        updatedMap[currentActiveDay] = refreshedSelected
                        if (currentActiveDay == MatchDayTab.TOMORROW) {
                            _tomorrowMatches.value = refreshedSelected
                        }
                    }

                    _matchesByDay.value = updatedMap

                    // If the user has a match open in the 5-Tab Match Center, silently sync its live dossier too
                    val openDossier = _selectedMatchDossier.value
                    if (openDossier != null) {
                        val updatedMatch = updatedMap.values.flatten().find { candidate ->
                            candidate.matchId == openDossier.match.matchId ||
                                (candidate.homeTeamName.equals(openDossier.match.homeTeamName, ignoreCase = true) &&
                                    candidate.awayTeamName.equals(openDossier.match.awayTeamName, ignoreCase = true))
                        }
                        if (updatedMatch != null) {
                            runCatching {
                                footballRepository.fetchMatchFullDossier(updatedMatch, forceRefresh = true)
                            }.onSuccess { refreshedDossier ->
                                _selectedMatchDossier.value = refreshedDossier
                            }
                        }
                    }

                    val nowMs = System.currentTimeMillis()
                    val liveCount = updatedMap[ currentActiveDay ].orEmpty().count {
                        it.status == MatchStatus.LIVE || it.status == MatchStatus.HALFTIME
                    }
                    _liveScoreSyncState.value = LiveScoreSyncState(
                        isPollingActive = true,
                        isSyncingDelta = false,
                        lastSyncedEpochMs = nowMs,
                        lastSyncedTimeFormatted = formatCurrentZoneSyncTime(nowMs),
                        pollCycleCount = _liveScoreSyncState.value.pollCycleCount + 1,
                        activeLiveMatchesCount = liveCount,
                        primaryLiveSource = if (footballRepository.isSportApiKeyConfigured()) {
                            "SportAPI.ai Live"
                        } else {
                            "SportAPI.ai + Live Hybrid"
                        }
                    )
                } catch (_: Exception) {
                    _liveScoreSyncState.value = _liveScoreSyncState.value.copy(isSyncingDelta = false)
                }
            }
        }
    }

    fun refreshFootballSchedules() {
        viewModelScope.launch {
            _isLoadingMatches.value = true
            try {
                val updatedMap = coroutineScope {
                    MatchDayTab.entries.map { tab ->
                        async {
                            tab to footballRepository.fetchMatchesForDay(
                                dayTab = tab,
                                forceLiveRefresh = (tab == MatchDayTab.TODAY)
                            )
                        }
                    }.awaitAll().toMap()
                }
                _matchesByDay.value = updatedMap
                _todayMatches.value = updatedMap[MatchDayTab.TODAY].orEmpty()
                _tomorrowMatches.value = updatedMap[MatchDayTab.TOMORROW].orEmpty()
                val nowMs = System.currentTimeMillis()
                val liveCount = updatedMap[_selectedMatchDay.value].orEmpty().count {
                    it.status == MatchStatus.LIVE || it.status == MatchStatus.HALFTIME
                }
                _liveScoreSyncState.value = _liveScoreSyncState.value.copy(
                    isPollingActive = true,
                    isSyncingDelta = false,
                    lastSyncedEpochMs = nowMs,
                    lastSyncedTimeFormatted = formatCurrentZoneSyncTime(nowMs),
                    activeLiveMatchesCount = liveCount
                )
            } finally {
                _isLoadingMatches.value = false
            }
        }
    }

    /**
     * Switches the user's Country Zone (`MA`, `FR`, `ES`, `GB`, `SA`, `AE`, `EG`, `DZ`, `TN`, `QA`, `KW`, `DE`, `IT`, `PT`, `NL`, `TR`, `US`, `CA`, `BR`).
     * Immediately re-localizes all loaded match schedules, kickoff times, UTC offset badges, and TV broadcast providers
     * in 0ms on the UI, then triggers a background schedule sync.
     */
    fun selectCountryZone(countryCode: String) {
        val newZone = footballRepository.setSelectedCountryZone(countryCode)
        _selectedCountryZone.value = newZone
        val mode = _selectedSeasonTimeMode.value

        // Immediately re-localize all in-memory matches so kickoff times and TV channels update instantaneously
        val relocalizedMap = _matchesByDay.value.mapValues { (_, list) ->
            footballRepository.reapplyCountryZoneToMatches(list, newZone, mode)
        }
        _matchesByDay.value = relocalizedMap
        _todayMatches.value = relocalizedMap[MatchDayTab.TODAY].orEmpty()
        _tomorrowMatches.value = relocalizedMap[MatchDayTab.TOMORROW].orEmpty()

        // If a Match Center Dossier is currently open, re-localize its match and broadcast guide
        val openDossier = _selectedMatchDossier.value
        if (openDossier != null) {
            val relocalizedMatch = footballRepository.reapplyCountryZoneToMatches(
                listOf(openDossier.match),
                newZone,
                mode
            ).first()
            viewModelScope.launch {
                runCatching {
                    footballRepository.fetchMatchFullDossier(relocalizedMatch, forceRefresh = true, countryZone = newZone)
                }.onSuccess { updatedDossier ->
                    _selectedMatchDossier.value = updatedDossier
                }
            }
        }

        // Refresh schedules in background for any date-boundary or live updates
        refreshFootballSchedules()
    }

    /**
     * Switches the seasonal clock mode ([SeasonTimeMode.AUTO], [SeasonTimeMode.WINTER], or [SeasonTimeMode.SUMMER])
     * and immediately recalculates all match kickoff times and UTC badges in 0ms.
     */
    fun selectSeasonTimeMode(mode: SeasonTimeMode) {
        val updatedMode = footballRepository.setSelectedSeasonTimeMode(mode)
        _selectedSeasonTimeMode.value = updatedMode
        val activeZone = _selectedCountryZone.value

        val relocalizedMap = _matchesByDay.value.mapValues { (_, list) ->
            footballRepository.reapplyCountryZoneToMatches(list, activeZone, updatedMode)
        }
        _matchesByDay.value = relocalizedMap
        _todayMatches.value = relocalizedMap[MatchDayTab.TODAY].orEmpty()
        _tomorrowMatches.value = relocalizedMap[MatchDayTab.TOMORROW].orEmpty()

        val openDossier = _selectedMatchDossier.value
        if (openDossier != null) {
            val relocalizedMatch = footballRepository.reapplyCountryZoneToMatches(
                listOf(openDossier.match),
                activeZone,
                updatedMode
            ).first()
            _selectedMatchDossier.value = openDossier.copy(match = relocalizedMatch)
        }
    }

    fun prioritizeCountrySpotlightLeague() {
        viewModelScope.launch {
            footballRepository.prioritizeCountrySpotlightTournament(_selectedCountryZone.value)
        }
    }

    fun autoDetectAndSelectCountryZone(): UserCountryZone {
        val detected = UserZoneRegistry.detectDeviceCountryZone()
        selectSeasonTimeMode(SeasonTimeMode.AUTO)
        selectCountryZone(detected.countryCode)
        return detected
    }

    fun openCountryZoneModal() {
        _showCountryZoneModal.value = true
    }

    fun closeCountryZoneModal() {
        _showCountryZoneModal.value = false
    }

    fun selectMatchDay(dayTab: MatchDayTab) {
        _selectedMatchDay.value = dayTab
        if (tvSyncState.value.role == SyncRole.PHONE_CONTROLLER && tvSyncState.value.isSyncActive) {
            tvSyncManager.sendNavMirror(
                RemoteNavMirrorCommand(
                    screenName = _currentScreen.value.name,
                    matchDayName = dayTab.name
                )
            )
        }
        if (_matchesByDay.value[dayTab].isNullOrEmpty()) {
            viewModelScope.launch {
                val fetched = footballRepository.fetchMatchesForDay(dayTab)
                val current = _matchesByDay.value.toMutableMap()
                current[dayTab] = fetched
                _matchesByDay.value = current
            }
        }
    }

    fun selectTournamentFilter(filterId: String) {
        _selectedTournamentFilter.value = filterId
    }

    fun toggleTournamentCollapsed(tournamentId: String) {
        val current = _collapsedTournamentIds.value.toMutableSet()
        if (current.contains(tournamentId)) {
            current.remove(tournamentId)
        } else {
            current.add(tournamentId)
        }
        _collapsedTournamentIds.value = current
    }

    fun updateTeamSearchQuery(query: String) {
        _teamSearchQuery.value = query
        teamSearchJob?.cancel()
        if (query.trim().isBlank()) {
            _teamSearchResults.value = emptyList()
            _isSearchingTeams.value = false
            return
        }
        teamSearchJob = viewModelScope.launch {
            _isSearchingTeams.value = true
            delay(180)
            val results = footballRepository.searchGlobalTeams(query)
            _teamSearchResults.value = results
            _isSearchingTeams.value = false
        }
    }

    fun toggleFavoriteTeam(team: FootballTeam) {
        viewModelScope.launch {
            footballRepository.toggleFavoriteTeam(team)
        }
    }

    fun removeFavoriteTeam(teamId: String) {
        viewModelScope.launch {
            footballRepository.removeFavoriteTeam(teamId)
        }
    }

    fun completeFirstTimeTeamsOnboarding() {
        footballRepository.setCountryZoneOnboardingCompleted(true)
        footballRepository.setTeamsOnboardingCompleted(true)
        _showFirstTimeTeamsOnboarding.value = false
        _teamSearchQuery.value = ""
        _teamSearchResults.value = emptyList()
    }

    fun openCustomizeTeamsModal() {
        _teamSearchQuery.value = ""
        _teamSearchResults.value = emptyList()
        _showCustomizeTeamsModal.value = true
    }

    fun closeCustomizeTeamsModal() {
        _showCustomizeTeamsModal.value = false
        _teamSearchQuery.value = ""
        _teamSearchResults.value = emptyList()
    }

    fun setTournamentEnabled(tournamentId: String, enabled: Boolean) {
        viewModelScope.launch {
            footballRepository.setTournamentEnabled(tournamentId, enabled)
        }
    }

    fun toggleTournamentPinned(tournamentId: String) {
        viewModelScope.launch {
            footballRepository.toggleTournamentPinned(tournamentId)
        }
    }

    fun enableAllTournaments() {
        viewModelScope.launch {
            footballRepository.enableAllTournaments()
        }
    }

    /**
     * Triggered when the user taps a TV channel provider or "Watch" button on a match card in the Kickoff section.
     *
     * Workflow:
     * 1. Looks up `clickedChannelName` EXCLUSIVELY in the user's saved **Favorites** list (`favoriteMatches`).
     *    Does NOT search the full Xtream catalog.
     * 2. Case A — Match Found in Favorites:
     *    - If 1 match is found in Favorites (`favoriteMatches.size == 1`), immediately launches the video player.
     *    - If multiple matching versions of that same channel are in Favorites (e.g., both FHD and HD), opens the
     *      Favorites-only selector so the user can tap between those matched Favorites (or play immediately).
     * 3. Case B — No Match Found in Favorites (`favoriteMatches.isEmpty()`):
     *    - Displays a clear notification inside the Kickoff section:
     *      `"{channelName}" is not in your Favorites yet. Please test and add "{channelName}" to your Favorites first to watch this game.`
     */
    fun onWatchMatchOnBeinClicked(
        match: FootballMatch,
        clickedChannelName: String = match.beinChannel,
        onAutoPlaySingleStream: (StreamEntity) -> Unit
    ) {
        val targetChannel = clickedChannelName.trim().ifBlank { match.beinChannel }
        val missingMessage =
            "\"$targetChannel\" is not in your Favorites yet. Please test and add \"$targetChannel\" to your Favorites first to watch this game."

        val playlist = activePlaylist.value
        if (playlist == null) {
            _beinChannelMatcherState.value = BeinChannelMatcherState(
                match = match,
                favoriteMatchedStreams = emptyList(),
                matchedStreams = emptyList(),
                resolvedChannelLabel = targetChannel,
                resolvedProviderName = footballRepository.detectMenaProviderFromLabel(targetChannel),
                requiresAddToFavoritesPrompt = true,
                missingFavoriteNotificationMessage = missingMessage,
                isLoading = false
            )
            return
        }

        viewModelScope.launch {
            _beinChannelMatcherState.value = BeinChannelMatcherState(
                match = match,
                resolvedChannelLabel = targetChannel,
                resolvedProviderName = footballRepository.detectMenaProviderFromLabel(targetChannel),
                isLoading = true
            )
            val result = footballRepository.findMenaChannelsWithFavoritesPriority(
                playlistId = playlist.id,
                match = match,
                overrideChannelLabel = targetChannel
            )

            // Case A — Single Match Found in Favorites: immediately launch the video player!
            if (result.favoriteMatches.size == 1) {
                _beinChannelMatcherState.value = null
                onAutoPlaySingleStream(result.favoriteMatches.first())
            } else {
                // Either multiple versions of the channel exist in Favorites (Case A multi-version),
                // or no match was found in Favorites (Case B missing-favorite notification).
                _beinChannelMatcherState.value = BeinChannelMatcherState(
                    match = match,
                    favoriteMatchedStreams = result.favoriteMatches,
                    livePlaylistCandidates = result.livePlaylistCandidates,
                    matchedStreams = result.favoriteMatches,
                    resolvedChannelLabel = result.resolvedChannelLabel,
                    resolvedProviderName = result.resolvedProviderName,
                    usedMenaFallbackProvider = result.usedMenaFallbackProvider,
                    availableMenaChannelOptions = result.availableMenaChannelOptions,
                    optionsAvailableInFavorites = result.optionsAvailableInFavorites,
                    requiresAddToFavoritesPrompt = result.favoriteMatches.isEmpty(),
                    missingFavoriteNotificationMessage =
                        "\"${result.resolvedChannelLabel}\" is not in your Favorites yet. Please test and add \"${result.resolvedChannelLabel}\" to your Favorites first to watch this game.",
                    isLoading = false
                )
            }
        }
    }

    /**
     * Switches between the match's MENA broadcaster options inside the Kickoff Channel dialog.
     * Looks up the selected channel in Favorites (Tier 1) and Live TV catalog (Tier 2).
     */
    fun selectMenaChannelOptionInMatcher(
        channelLabel: String,
        onAutoPlaySingleStream: ((StreamEntity) -> Unit)? = null
    ) {
        val currentState = _beinChannelMatcherState.value ?: return
        val playlist = activePlaylist.value
        val targetChannel = channelLabel.trim().ifBlank { currentState.match.beinChannel }
        val missingMessage =
            "\"$targetChannel\" is not in your Favorites yet. Please test and add \"$targetChannel\" to your Favorites first to watch this game."

        if (playlist == null) {
            _beinChannelMatcherState.value = currentState.copy(
                favoriteMatchedStreams = emptyList(),
                livePlaylistCandidates = emptyList(),
                matchedStreams = emptyList(),
                resolvedChannelLabel = targetChannel,
                resolvedProviderName = footballRepository.detectMenaProviderFromLabel(targetChannel),
                requiresAddToFavoritesPrompt = true,
                missingFavoriteNotificationMessage = missingMessage,
                isLoading = false
            )
            return
        }

        viewModelScope.launch {
            _beinChannelMatcherState.value = currentState.copy(isLoading = true)
            val result = footballRepository.findMenaChannelsWithFavoritesPriority(
                playlistId = playlist.id,
                match = currentState.match,
                overrideChannelLabel = targetChannel
            )
            if (result.favoriteMatches.size == 1 && onAutoPlaySingleStream != null) {
                _beinChannelMatcherState.value = null
                onAutoPlaySingleStream(result.favoriteMatches.first())
            } else {
                _beinChannelMatcherState.value = BeinChannelMatcherState(
                    match = currentState.match,
                    favoriteMatchedStreams = result.favoriteMatches,
                    livePlaylistCandidates = result.livePlaylistCandidates,
                    matchedStreams = result.favoriteMatches,
                    resolvedChannelLabel = result.resolvedChannelLabel,
                    resolvedProviderName = result.resolvedProviderName,
                    usedMenaFallbackProvider = result.usedMenaFallbackProvider,
                    availableMenaChannelOptions = result.availableMenaChannelOptions,
                    optionsAvailableInFavorites = result.optionsAvailableInFavorites,
                    requiresAddToFavoritesPrompt = result.favoriteMatches.isEmpty(),
                    missingFavoriteNotificationMessage =
                        "\"${result.resolvedChannelLabel}\" is not in your Favorites yet. Please test and add \"${result.resolvedChannelLabel}\" to your Favorites first to watch this game.",
                    isLoading = false
                )
            }
        }
    }

    fun toggleStreamFavoriteInMatcher(stream: StreamEntity) {
        val currentState = _beinChannelMatcherState.value ?: return
        val playlist = activePlaylist.value ?: return

        viewModelScope.launch {
            repository.toggleFavorite(stream.id, !stream.isFavorite)
            val result = footballRepository.findMenaChannelsWithFavoritesPriority(
                playlistId = playlist.id,
                match = currentState.match,
                overrideChannelLabel = currentState.resolvedChannelLabel
            )
            _beinChannelMatcherState.value = currentState.copy(
                favoriteMatchedStreams = result.favoriteMatches,
                livePlaylistCandidates = result.livePlaylistCandidates,
                matchedStreams = result.favoriteMatches,
                requiresAddToFavoritesPrompt = result.favoriteMatches.isEmpty()
            )
        }
    }

    fun pinToFavoritesAndPlayStream(
        stream: StreamEntity,
        onPlayStream: (StreamEntity) -> Unit
    ) {
        viewModelScope.launch {
            repository.toggleFavorite(stream.id, true)
            _beinChannelMatcherState.value = null
            onPlayStream(stream.copy(isFavorite = true))
        }
    }

    fun dismissBeinChannelMatcher() {
        _beinChannelMatcherState.value = null
    }

    fun openMatchCenterDossier(match: FootballMatch) {
        viewModelScope.launch {
            _isLoadingMatchDossier.value = true
            try {
                val dossier = footballRepository.fetchMatchFullDossier(match)
                _selectedMatchDossier.value = dossier
            } finally {
                _isLoadingMatchDossier.value = false
            }
        }
    }

    fun closeMatchCenterDossier() {
        _selectedMatchDossier.value = null
        _isLoadingMatchDossier.value = false
    }

    fun setScreen(screen: AppScreen) {
        _currentScreen.value = screen
        if (tvSyncState.value.role == SyncRole.PHONE_CONTROLLER && tvSyncState.value.isSyncActive) {
            tvSyncManager.sendNavMirror(
                RemoteNavMirrorCommand(
                    screenName = screen.name,
                    matchDayName = _selectedMatchDay.value.name
                )
            )
        }
    }

    // =========================================================================
    // Local Wi-Fi Phone ↔ TV QR Companion & Sync Operations
    // =========================================================================
    suspend fun buildCurrentVaultSyncPayload(): VaultSyncPayload {
        val playlist = activePlaylist.value ?: repository.getActivePlaylistOnce()
        val favIds = if (playlist != null) {
            val liveFavs = database.streamDao().getFavoriteProviderIds(playlist.id, "live").map { "live:$it" }
            val movieFavs = database.streamDao().getFavoriteProviderIds(playlist.id, "movie").map { "movie:$it" }
            val seriesFavs = database.streamDao().getFavoriteProviderIds(playlist.id, "series").map { "series:$it" }
            liveFavs + movieFavs + seriesFavs
        } else {
            emptyList()
        }
        val app = getApplication<Application>()
        val subPrefs = app.getSharedPreferences(com.example.ui.player.PlayerViewModel.SUBTITLE_PREFS_NAME, android.content.Context.MODE_PRIVATE)
        val prefSubLang = subPrefs.getString(com.example.ui.player.PlayerViewModel.KEY_LAST_SUBTITLE_LANG, "").orEmpty()
        val resumeMap = repository.episodeResumeMap.value.mapValues { it.value.positionMs }

        return VaultSyncPayload(
            playlistName = playlist?.name.orEmpty(),
            serverUrl = playlist?.serverUrl.orEmpty(),
            username = playlist?.username.orEmpty(),
            password = playlist?.password.orEmpty(),
            playlistType = "XTREAM",
            favoriteStreamIds = favIds,
            countryZoneCode = _selectedCountryZone.value.countryCode,
            preferredSubtitleLang = prefSubLang,
            episodeResumeMap = resumeMap
        )
    }

    suspend fun applyIncomingVaultPayload(payload: VaultSyncPayload) {
        if (payload.countryZoneCode.isNotBlank() &&
            !payload.countryZoneCode.equals(_selectedCountryZone.value.countryCode, ignoreCase = true)
        ) {
            selectCountryZone(payload.countryZoneCode)
        }

        if (payload.preferredSubtitleLang.isNotBlank()) {
            val app = getApplication<Application>()
            app.getSharedPreferences(
                com.example.ui.player.PlayerViewModel.SUBTITLE_PREFS_NAME,
                android.content.Context.MODE_PRIVATE
            ).edit()
                .putString(com.example.ui.player.PlayerViewModel.KEY_LAST_SUBTITLE_LANG, payload.preferredSubtitleLang)
                .apply()
        }

        var targetPlaylist = activePlaylist.value ?: repository.getActivePlaylistOnce()
        if (payload.serverUrl.isNotBlank() && payload.username.isNotBlank()) {
            val existing = allPlaylists.value.find {
                it.serverUrl.equals(payload.serverUrl.trim(), ignoreCase = true) &&
                    it.username.equals(payload.username.trim(), ignoreCase = true)
            }
            if (existing != null) {
                repository.setActivePlaylist(existing.id)
                targetPlaylist = existing
            } else {
                val newPlaylist = PlaylistEntity(
                    name = payload.playlistName.ifBlank { "Synced Xtream Account" },
                    serverUrl = payload.serverUrl.trim(),
                    username = payload.username.trim(),
                    password = payload.password.trim(),
                    isActive = true
                )
                val newId = repository.addOrUpdatePlaylist(newPlaylist)
                repository.setActivePlaylist(newId)
                targetPlaylist = repository.getPlaylistById(newId)
                if (targetPlaylist != null) {
                    viewModelScope.launch {
                        repository.syncPlaylist(targetPlaylist!!)
                    }
                }
            }
        }

        val activeId = targetPlaylist?.id ?: return
        val liveFavs = mutableListOf<Long>()
        val movieFavs = mutableListOf<Long>()
        val seriesFavs = mutableListOf<Long>()
        payload.favoriteStreamIds.forEach { token ->
            val type = token.substringBefore(":", "live").lowercase(Locale.ROOT)
            val idLong = token.substringAfter(":", token).toLongOrNull() ?: return@forEach
            when (type) {
                "movie" -> movieFavs.add(idLong)
                "series" -> seriesFavs.add(idLong)
                else -> liveFavs.add(idLong)
            }
        }
        if (liveFavs.isNotEmpty()) database.streamDao().restoreFavorites(activeId, "live", liveFavs)
        if (movieFavs.isNotEmpty()) database.streamDao().restoreFavorites(activeId, "movie", movieFavs)
        if (seriesFavs.isNotEmpty()) database.streamDao().restoreFavorites(activeId, "series", seriesFavs)

        payload.episodeResumeMap.forEach { (rawKey, posMs) ->
            val epId = rawKey.substringAfter("_ep_", "").toLongOrNull()
            if (epId != null && posMs > 0L) {
                repository.saveEpisodePositionMs(activeId, epId, posMs)
            }
        }
    }

    fun startTvHostMode() {
        _tvSyncErrorMessage.value = null
        tvSyncManager.startTvHostServer()
    }

    fun startPhoneSyncDiscovery() {
        _tvSyncErrorMessage.value = null
        tvSyncManager.startNsdDiscovery()
    }

    fun connectPhoneToTv(ip: String, port: Int, pin: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _tvSyncErrorMessage.value = null
            val localVault = buildCurrentVaultSyncPayload()
            val result = tvSyncManager.connectToTvHost(
                hostIp = ip,
                port = port,
                pin = pin,
                localVault = localVault,
                onRemoteVaultReceived = { remoteVault ->
                    applyIncomingVaultPayload(remoteVault)
                }
            )
            if (result.isSuccess) {
                onSuccess()
            } else {
                _tvSyncErrorMessage.value = result.exceptionOrNull()?.message ?: "Could not connect to TV"
            }
        }
    }

    fun reconnectToLastTv(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _tvSyncErrorMessage.value = null
            val localVault = buildCurrentVaultSyncPayload()
            val result = tvSyncManager.rejoinLastTvHost(
                localVault = localVault,
                onRemoteVaultReceived = { remoteVault ->
                    applyIncomingVaultPayload(remoteVault)
                }
            )
            if (result.isSuccess) {
                onSuccess()
            } else {
                _tvSyncErrorMessage.value = result.exceptionOrNull()?.message ?: "Could not reconnect to saved TV"
            }
        }
    }

    fun leaveTvSyncMode() {
        _tvSyncErrorMessage.value = null
        tvSyncManager.leaveSyncMode()
    }

    fun playStreamOnSyncedTv(stream: StreamEntity, playlist: PlaylistEntity? = activePlaylist.value) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val subPrefs = app.getSharedPreferences(
                com.example.ui.player.PlayerViewModel.SUBTITLE_PREFS_NAME,
                android.content.Context.MODE_PRIVATE
            )
            val preferredSub = subPrefs.getString(com.example.ui.player.PlayerViewModel.KEY_LAST_SUBTITLE_LANG, "").orEmpty()
            val cmd = RemotePlayCommand(
                streamId = stream.streamId.toString(),
                streamName = stream.name,
                streamType = stream.streamType,
                categoryId = stream.categoryId,
                streamIcon = stream.streamIcon,
                epgChannelId = stream.epgChannelId,
                containerExtension = stream.containerExtension,
                resumePositionMs = stream.lastPositionMs,
                preferredSubtitleLang = preferredSub
            )
            tvSyncManager.sendPlayOnTv(cmd)
        }
    }

    fun sendControlToSyncedTv(action: String, value: String = "", seekSeconds: Int = 0) {
        tvSyncManager.sendControlToTv(action = action, value = value, seekSeconds = seekSeconds)
    }

    fun refreshSyncedTvStatusOnResume() {
        if (tvSyncState.value.role == SyncRole.PHONE_CONTROLLER && tvSyncState.value.isSyncActive) {
            viewModelScope.launch {
                tvSyncManager.refreshTvStatus()
            }
        }
    }

    suspend fun resolveOrBuildStreamForTvCommand(cmd: RemotePlayCommand): Pair<StreamEntity, PlaylistEntity>? {
        val playlist = activePlaylist.value ?: repository.getActivePlaylistOnce() ?: return null
        val providerStreamId = cmd.streamId.toLongOrNull() ?: 0L
        val existing = if (providerStreamId > 0L) {
            database.streamDao().getStreamByProviderId(playlist.id, providerStreamId)
        } else {
            null
        }
        val resolvedStream = existing?.copy(
            lastPositionMs = if (cmd.resumePositionMs > 0L) cmd.resumePositionMs else existing.lastPositionMs
        ) ?: StreamEntity(
            id = providerStreamId.coerceAtLeast(1L),
            playlistId = playlist.id,
            streamId = providerStreamId,
            categoryId = cmd.categoryId,
            name = cmd.streamName.ifBlank { "TV Stream" },
            streamType = cmd.streamType.ifBlank { "live" },
            streamIcon = cmd.streamIcon,
            epgChannelId = cmd.epgChannelId,
            containerExtension = cmd.containerExtension.ifBlank {
                if (cmd.streamType.equals("live", ignoreCase = true)) "ts" else "mp4"
            },
            lastPositionMs = cmd.resumePositionMs
        )
        return resolvedStream to playlist
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
