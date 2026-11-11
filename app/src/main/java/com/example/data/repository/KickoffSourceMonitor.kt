package com.example.data.repository

import android.content.Context
import com.example.BuildConfig
import com.example.util.SecurityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Identifies each of the 6 upstream football data providers used by SetPlay Kickoff.
 */
enum class KickoffProviderId(
    val id: String,
    val displayName: String,
    val requiresApiKey: Boolean,
    val dependentFeatures: String,
    val endpointSummary: String
) {
    API_FOOTBALL(
        id = "api_football",
        displayName = "API-Football v3",
        requiresApiKey = true,
        dependentFeatures = "Official Lineups, Player Cutouts, Timeline Events & xG Stats",
        endpointSummary = "v3.football.api-sports.io"
    ),
    FOOTBALL_DATA_ORG(
        id = "football_data",
        displayName = "Football-Data.org v4",
        requiresApiKey = true,
        dependentFeatures = "UEFA Champions League & Europe Top 5 League Fixtures",
        endpointSummary = "api.football-data.org/v4"
    ),
    SPORT_API_AI(
        id = "sportapi_ai",
        displayName = "SportAPI.ai Live",
        requiresApiKey = true,
        dependentFeatures = "Real-Time Live Scores, Match Clocks & Broad Schedules",
        endpointSummary = "sportapi.ai/api/v1"
    ),
    ESPN_SOCCER(
        id = "espn_soccer",
        displayName = "ESPN Soccer API",
        requiresApiKey = false,
        dependentFeatures = "Live Scoreboards, H2H History, Form & Standings",
        endpointSummary = "site.api.espn.com"
    ),
    SOFASCORE_LIVE(
        id = "sofascore",
        displayName = "Sofascore Feed",
        requiresApiKey = false,
        dependentFeatures = "Regional TV Broadcast Channels & Player Ratings",
        endpointSummary = "api.sofascore.com"
    ),
    THESPORTSDB(
        id = "thesportsdb",
        displayName = "TheSportsDB v1",
        requiresApiKey = false,
        dependentFeatures = "High-Res Club Crests (/tiny), Squad Cutouts & TV Listings",
        endpointSummary = "thesportsdb.com/api/v1"
    )
}

/**
 * Real-time health and quota status for a Kickoff upstream data source.
 */
enum class SourceHealthStatus(val label: String) {
    HEALTHY("Active & Healthy"),
    QUOTA_WARNING("Quota Low / Rate-Limited"),
    KEY_INVALID_OR_EXPIRED("API Key Invalid or Expired"),
    UNREACHABLE("Source Unreachable"),
    UNCONFIGURED("Default / Unconfigured")
}

/**
 * Snapshot of a single Kickoff provider's health, quota, and key configuration state.
 */
data class KickoffSourceHealthState(
    val provider: KickoffProviderId,
    val status: SourceHealthStatus,
    val statusDetail: String,
    val remainingQuotaLabel: String,
    val isUsingCustomKey: Boolean,
    val maskedKeyPreview: String,
    val lastCheckedEpochMs: Long = 0L,
    val isEnabled: Boolean = true
)

/**
 * Result of an on-demand API key validation check before activating or saving a key.
 */
data class ApiKeyValidationResult(
    val provider: KickoffProviderId,
    val isValid: Boolean,
    val status: SourceHealthStatus,
    val message: String,
    val remainingQuotaLabel: String = ""
)

/**
 * Monitors, validates, and manages all 6 Kickoff data sources and API keys:
 * - Stores user-supplied replacement API keys encrypted at rest via [SecurityManager] in `SharedPreferences` (`setplay_kickoff_sources_v1`).
 * - Passively tracks runtime HTTP responses (`200`, `401`/`403`, `429`, network failures) and remaining quotas without extra API overhead.
 * - Supports on-demand validation of candidate API keys (`validateAndSaveApiKey`) and parallel health checks across all 6 sources (`checkAllSourcesHealth`).
 * - Emits non-intrusive user alerts via [activeSourceAlertFlow] whenever a configured key expires, hits its rate/quota limit, or becomes unreachable.
 */
class KickoffSourceMonitor(
    context: Context,
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _sourceHealthFlow = MutableStateFlow(buildInitialHealthStates())
    val sourceHealthFlow: StateFlow<List<KickoffSourceHealthState>> = _sourceHealthFlow.asStateFlow()

    private val _activeSourceAlertFlow = MutableStateFlow<String?>(null)
    val activeSourceAlertFlow: StateFlow<String?> = _activeSourceAlertFlow.asStateFlow()

    init {
        syncCustomKeysIntoCompanionCache()
        _sourceHealthFlow.value = buildInitialHealthStates()
    }

    private fun syncCustomKeysIntoCompanionCache() {
        for (provider in KickoffProviderId.entries) {
            if (provider.requiresApiKey) {
                val custom = readDecryptedCustomKey(provider)
                if (custom.isNotBlank()) {
                    activeCustomKeyCache[provider] = custom
                } else {
                    activeCustomKeyCache.remove(provider)
                }
            }
        }
    }

    private fun readDecryptedCustomKey(provider: KickoffProviderId): String {
        val rawStored = prefs.getString(prefKeyForProvider(provider), "").orEmpty().trim()
        if (rawStored.isBlank()) return ""
        val decrypted = SecurityManager.decryptString(rawStored).trim()
        return if (isCleanedKeyUsable(decrypted)) decrypted else ""
    }

    /**
     * Resolves the active API key for [provider], preferring a user-saved custom key (decrypted via [SecurityManager])
     * and falling back to [buildConfigFallback] if usable.
     */
    fun resolveActiveApiKey(
        provider: KickoffProviderId,
        buildConfigFallback: String = getDefaultBuildConfigKey(provider)
    ): String {
        val custom = readDecryptedCustomKey(provider)
        if (custom.isNotBlank()) {
            activeCustomKeyCache[provider] = custom
            return custom
        }
        activeCustomKeyCache.remove(provider)
        val cleanedFallback = buildConfigFallback.trim()
        return if (isCleanedKeyUsable(cleanedFallback)) cleanedFallback else ""
    }

    fun isUsingCustomKey(provider: KickoffProviderId): Boolean {
        return readDecryptedCustomKey(provider).isNotBlank()
    }

    /**
     * Clears any custom API key override for [provider] and restores the default BuildConfig state.
     */
    fun resetToDefaultKey(provider: KickoffProviderId) {
        prefs.edit().remove(prefKeyForProvider(provider)).apply()
        activeCustomKeyCache.remove(provider)
        val defaultKey = getDefaultBuildConfigKey(provider)
        val isConfigured = isCleanedKeyUsable(defaultKey)
        updateSingleProviderState(
            provider = provider,
            status = if (!provider.requiresApiKey || isConfigured) {
                SourceHealthStatus.HEALTHY
            } else {
                SourceHealthStatus.UNCONFIGURED
            },
            statusDetail = if (!provider.requiresApiKey) {
                "Public feed active • Zero API key required"
            } else if (isConfigured) {
                "Restored built-in default API key"
            } else {
                "Using keyless fallback providers • Optional custom key supported"
            },
            remainingQuotaLabel = defaultQuotaLabel(provider, isConfigured),
            lastCheckedEpochMs = System.currentTimeMillis()
        )
        refreshAlertBannerFromCurrentStates()
    }

    fun dismissAlert() {
        _activeSourceAlertFlow.value = null
    }

    /**
     * Passively records a runtime HTTP response from `FootballRepository` so expired keys (`401`/`403`),
     * rate limits (`429`), or quota exhaustion are surfaced immediately without extra network calls.
     */
    fun recordRuntimeResponse(
        provider: KickoffProviderId,
        httpCode: Int,
        remainingQuota: Int? = null,
        errorNote: String? = null
    ) {
        val now = System.currentTimeMillis()
        val currentEntry = _sourceHealthFlow.value.find { it.provider == provider }
        val quotaLabel = when {
            remainingQuota != null && provider == KickoffProviderId.API_FOOTBALL ->
                "$remainingQuota / 100 req today"
            remainingQuota != null ->
                "$remainingQuota remaining"
            else -> currentEntry?.remainingQuotaLabel ?: defaultQuotaLabel(provider, true)
        }

        val (newStatus, detail) = when {
            httpCode == 401 || httpCode == 403 -> {
                SourceHealthStatus.KEY_INVALID_OR_EXPIRED to
                    (errorNote?.takeIf { it.isNotBlank() }
                        ?: "${provider.displayName} rejected the API key (HTTP $httpCode). Update or reset your key in Sources & Keys.")
            }
            httpCode == 429 || (remainingQuota != null && remainingQuota <= 0) -> {
                SourceHealthStatus.QUOTA_WARNING to
                    (errorNote?.takeIf { it.isNotBlank() }
                        ?: "${provider.displayName} reached its rate or daily quota limit ($quotaLabel). Fallback sources are active.")
            }
            httpCode in 200..299 -> {
                if (remainingQuota != null && remainingQuota in 1..15 && provider == KickoffProviderId.API_FOOTBALL) {
                    SourceHealthStatus.QUOTA_WARNING to
                        "Low daily quota ($remainingQuota / 100 remaining) • Reserved for Match Center dossiers"
                } else {
                    SourceHealthStatus.HEALTHY to
                        (errorNote?.takeIf { it.isNotBlank() } ?: "Verified live response (HTTP $httpCode)")
                }
            }
            httpCode >= 500 || httpCode <= 0 -> {
                SourceHealthStatus.UNREACHABLE to
                    (errorNote?.takeIf { it.isNotBlank() }
                        ?: "${provider.displayName} is temporarily unreachable. Multi-provider fallback is active.")
            }
            else -> {
                SourceHealthStatus.UNREACHABLE to
                    (errorNote?.takeIf { it.isNotBlank() } ?: "Unexpected HTTP $httpCode response")
            }
        }

        updateSingleProviderState(
            provider = provider,
            status = newStatus,
            statusDetail = detail,
            remainingQuotaLabel = quotaLabel,
            lastCheckedEpochMs = now
        )
        refreshAlertBannerFromCurrentStates()
    }

    /**
     * Validates a candidate API key against the provider's lightweight status/competition endpoint
     * and saves it encrypted via [SecurityManager] when [saveIfValid] is true.
     */
    suspend fun validateAndSaveApiKey(
        provider: KickoffProviderId,
        candidateKey: String,
        saveIfValid: Boolean = true
    ): ApiKeyValidationResult = withContext(Dispatchers.IO) {
        val cleaned = candidateKey.trim()
        if (!provider.requiresApiKey) {
            return@withContext ApiKeyValidationResult(
                provider = provider,
                isValid = true,
                status = SourceHealthStatus.HEALTHY,
                message = "${provider.displayName} is a keyless public feed and does not require an API key."
            )
        }
        if (!isCleanedKeyUsable(cleaned) || cleaned.length < 8) {
            return@withContext ApiKeyValidationResult(
                provider = provider,
                isValid = false,
                status = SourceHealthStatus.KEY_INVALID_OR_EXPIRED,
                message = "Please enter a valid ${provider.displayName} key (at least 8 characters)."
            )
        }

        val result = executeLiveKeyValidationRequest(provider, cleaned)
        if (result.isValid && saveIfValid) {
            saveCustomApiKeyInternal(provider, cleaned)
        }
        updateSingleProviderState(
            provider = provider,
            status = result.status,
            statusDetail = result.message,
            remainingQuotaLabel = result.remainingQuotaLabel.ifBlank {
                defaultQuotaLabel(provider, true)
            },
            lastCheckedEpochMs = System.currentTimeMillis()
        )
        refreshAlertBannerFromCurrentStates()
        result
    }

    /**
     * Directly saves a custom API key encrypted via [SecurityManager] and updates the in-memory and flow states.
     */
    fun saveCustomApiKeyInternal(provider: KickoffProviderId, cleanedKey: String) {
        val trimmed = cleanedKey.trim()
        if (!isCleanedKeyUsable(trimmed)) return
        val encrypted = SecurityManager.encryptString(trimmed)
        prefs.edit().putString(prefKeyForProvider(provider), encrypted).apply()
        activeCustomKeyCache[provider] = trimmed
        updateSingleProviderState(
            provider = provider,
            status = SourceHealthStatus.HEALTHY,
            statusDetail = "Custom API key configured & encrypted",
            remainingQuotaLabel = defaultQuotaLabel(provider, true),
            lastCheckedEpochMs = System.currentTimeMillis()
        )
        refreshAlertBannerFromCurrentStates()
    }

    private fun executeLiveKeyValidationRequest(
        provider: KickoffProviderId,
        candidateKey: String
    ): ApiKeyValidationResult {
        return try {
            val request = when (provider) {
                KickoffProviderId.API_FOOTBALL -> {
                    Request.Builder()
                        .url("https://v3.football.api-sports.io/status")
                        .header("x-apisports-key", candidateKey)
                        .header("Accept", "application/json")
                        .build()
                }
                KickoffProviderId.FOOTBALL_DATA_ORG -> {
                    Request.Builder()
                        .url("https://api.football-data.org/v4/competitions/PL")
                        .header("X-Auth-Token", candidateKey)
                        .header("Accept", "application/json")
                        .build()
                }
                KickoffProviderId.SPORT_API_AI -> {
                    Request.Builder()
                        .url("https://sportapi.ai/api/v1/football/matches/live")
                        .header("X-Api-Key", candidateKey)
                        .header("Accept", "application/json")
                        .build()
                }
                else -> return ApiKeyValidationResult(
                    provider = provider,
                    isValid = true,
                    status = SourceHealthStatus.HEALTHY,
                    message = "${provider.displayName} active"
                )
            }

            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                val remainingHeader = response.header("x-ratelimit-requests-remaining")
                    ?: response.header("X-Requests-Available-Minute")
                parseValidationResponse(
                    provider = provider,
                    httpCode = response.code,
                    responseBody = body,
                    remainingHeader = remainingHeader
                )
            }
        } catch (e: Exception) {
            ApiKeyValidationResult(
                provider = provider,
                isValid = false,
                status = SourceHealthStatus.UNREACHABLE,
                message = "Could not reach ${provider.endpointSummary}: ${e.localizedMessage ?: "Network timeout"}"
            )
        }
    }

    /**
     * Parses an HTTP status code, JSON body, and quota header from a provider validation call into an [ApiKeyValidationResult].
     */
    internal fun parseValidationResponse(
        provider: KickoffProviderId,
        httpCode: Int,
        responseBody: String,
        remainingHeader: String? = null
    ): ApiKeyValidationResult {
        val remainingInt = remainingHeader?.trim()?.toIntOrNull()
        if (httpCode == 401 || httpCode == 403) {
            return ApiKeyValidationResult(
                provider = provider,
                isValid = false,
                status = SourceHealthStatus.KEY_INVALID_OR_EXPIRED,
                message = "${provider.displayName} rejected the key (HTTP $httpCode: Unauthorized / Expired)."
            )
        }
        if (httpCode == 429) {
            return ApiKeyValidationResult(
                provider = provider,
                isValid = false,
                status = SourceHealthStatus.QUOTA_WARNING,
                message = "${provider.displayName} key is rate-limited or out of quota (HTTP 429).",
                remainingQuotaLabel = "0 remaining (HTTP 429)"
            )
        }
        if (httpCode !in 200..299) {
            return ApiKeyValidationResult(
                provider = provider,
                isValid = false,
                status = SourceHealthStatus.UNREACHABLE,
                message = "${provider.displayName} returned HTTP $httpCode."
            )
        }

        val root = runCatching { JSONObject(responseBody) }.getOrNull()
        if (provider == KickoffProviderId.API_FOOTBALL && root != null) {
            val errorsObj = root.optJSONObject("errors")
            if (errorsObj != null && errorsObj.length() > 0) {
                val firstError = errorsObj.keys().asSequence().firstOrNull()?.let { k ->
                    errorsObj.optString(k, "")
                }.orEmpty()
                val lowerErr = firstError.lowercase(Locale.ROOT)
                return if (lowerErr.contains("limit") || lowerErr.contains("quota") || lowerErr.contains("request")) {
                    ApiKeyValidationResult(
                        provider = provider,
                        isValid = false,
                        status = SourceHealthStatus.QUOTA_WARNING,
                        message = firstError.ifBlank { "API-Football daily quota reached." },
                        remainingQuotaLabel = "0 / 100 req today"
                    )
                } else {
                    ApiKeyValidationResult(
                        provider = provider,
                        isValid = false,
                        status = SourceHealthStatus.KEY_INVALID_OR_EXPIRED,
                        message = firstError.ifBlank { "Invalid or expired API-Football key." }
                    )
                }
            }

            val responseObj = root.optJSONObject("response")
            val requestsObj = responseObj?.optJSONObject("requests")
            val currentUsed = requestsObj?.optInt("current", -1) ?: -1
            val limitDay = requestsObj?.optInt("limit_day", 100) ?: 100
            val subActive = responseObj?.optJSONObject("subscription")?.optBoolean("active", true) ?: true
            if (!subActive) {
                return ApiKeyValidationResult(
                    provider = provider,
                    isValid = false,
                    status = SourceHealthStatus.KEY_INVALID_OR_EXPIRED,
                    message = "API-Football subscription is inactive or expired."
                )
            }
            val computedRemaining = when {
                remainingInt != null -> remainingInt
                currentUsed >= 0 && limitDay > 0 -> (limitDay - currentUsed).coerceAtLeast(0)
                else -> 100
            }
            val quotaStr = "$computedRemaining / $limitDay req today"
            if (computedRemaining <= 0) {
                return ApiKeyValidationResult(
                    provider = provider,
                    isValid = false,
                    status = SourceHealthStatus.QUOTA_WARNING,
                    message = "API-Football key is valid, but 0 / $limitDay daily requests remain today.",
                    remainingQuotaLabel = quotaStr
                )
            }
            return ApiKeyValidationResult(
                provider = provider,
                isValid = true,
                status = if (computedRemaining <= 15) SourceHealthStatus.QUOTA_WARNING else SourceHealthStatus.HEALTHY,
                message = "API-Football v3 key verified ($quotaStr).",
                remainingQuotaLabel = quotaStr
            )
        }

        if (root != null) {
            val errorMsg = root.optString("message", root.optString("error", "")).trim()
            val errorCode = root.optInt("errorCode", 200)
            if (errorCode == 401 || errorCode == 403 ||
                errorMsg.contains("invalid", ignoreCase = true) ||
                errorMsg.contains("unauthorized", ignoreCase = true) ||
                errorMsg.contains("token", ignoreCase = true) && errorMsg.contains("error", ignoreCase = true)
            ) {
                return ApiKeyValidationResult(
                    provider = provider,
                    isValid = false,
                    status = SourceHealthStatus.KEY_INVALID_OR_EXPIRED,
                    message = errorMsg.ifBlank { "${provider.displayName} rejected the API key." }
                )
            }
        }

        val quotaLabel = when {
            remainingInt != null && provider == KickoffProviderId.FOOTBALL_DATA_ORG ->
                "$remainingInt / 10 req/min"
            remainingInt != null ->
                "$remainingInt remaining"
            else -> defaultQuotaLabel(provider, true)
        }
        return ApiKeyValidationResult(
            provider = provider,
            isValid = true,
            status = SourceHealthStatus.HEALTHY,
            message = "${provider.displayName} key verified and active.",
            remainingQuotaLabel = quotaLabel
        )
    }

    /**
     * Pings all 6 Kickoff providers in parallel via `supervisorScope` and updates [sourceHealthFlow] and [activeSourceAlertFlow].
     */
    suspend fun checkAllSourcesHealth(): List<KickoffSourceHealthState> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val updatedStates = supervisorScope {
            KickoffProviderId.entries.map { provider ->
                async {
                    checkSingleProviderHealth(provider, now)
                }
            }.awaitAll()
        }
        _sourceHealthFlow.value = updatedStates
        refreshAlertBannerFromCurrentStates()
        updatedStates
    }

    private fun checkSingleProviderHealth(
        provider: KickoffProviderId,
        nowEpochMs: Long
    ): KickoffSourceHealthState {
        val customKey = readDecryptedCustomKey(provider)
        val activeKey = resolveActiveApiKey(provider)
        val isCustom = customKey.isNotBlank()
        val masked = maskApiKey(activeKey, provider.requiresApiKey)

        if (provider.requiresApiKey) {
            if (activeKey.isBlank()) {
                return KickoffSourceHealthState(
                    provider = provider,
                    status = SourceHealthStatus.UNCONFIGURED,
                    statusDetail = "No API key configured • Multi-provider fallback active",
                    remainingQuotaLabel = defaultQuotaLabel(provider, false),
                    isUsingCustomKey = false,
                    maskedKeyPreview = "Not Set (Optional)",
                    lastCheckedEpochMs = nowEpochMs
                )
            }
            val validation = executeLiveKeyValidationRequest(provider, activeKey)
            return KickoffSourceHealthState(
                provider = provider,
                status = validation.status,
                statusDetail = validation.message,
                remainingQuotaLabel = validation.remainingQuotaLabel.ifBlank {
                    defaultQuotaLabel(provider, true)
                },
                isUsingCustomKey = isCustom,
                maskedKeyPreview = masked,
                lastCheckedEpochMs = nowEpochMs
            )
        }

        // Keyless public feed probe
        val probeRequest = when (provider) {
            KickoffProviderId.ESPN_SOCCER -> Request.Builder()
                .url("https://site.api.espn.com/apis/site/v2/sports/soccer/eng.1/scoreboard")
                .header("Accept", "application/json")
                .build()
            KickoffProviderId.SOFASCORE_LIVE -> Request.Builder()
                .url("https://api.sofascore.com/api/v1/sport/football/events/live")
                .header("User-Agent", FootballRepository.SOFASCORE_BROWSER_USER_AGENT)
                .header("Referer", FootballRepository.SOFASCORE_REFERER)
                .header("Accept", "application/json")
                .build()
            KickoffProviderId.THESPORTSDB -> Request.Builder()
                .url("https://www.thesportsdb.com/api/v1/json/3/searchteams.php?t=Arsenal")
                .header("Accept", "application/json")
                .build()
            else -> null
        }

        if (probeRequest == null) {
            return KickoffSourceHealthState(
                provider = provider,
                status = SourceHealthStatus.HEALTHY,
                statusDetail = "Public feed active",
                remainingQuotaLabel = defaultQuotaLabel(provider, true),
                isUsingCustomKey = false,
                maskedKeyPreview = "Keyless Public API",
                lastCheckedEpochMs = nowEpochMs
            )
        }

        return try {
            httpClient.newCall(probeRequest).execute().use { response ->
                val code = response.code
                val status = when {
                    code in 200..299 -> SourceHealthStatus.HEALTHY
                    code == 429 -> SourceHealthStatus.QUOTA_WARNING
                    else -> SourceHealthStatus.UNREACHABLE
                }
                val detail = when (status) {
                    SourceHealthStatus.HEALTHY -> "Public feed online & responding (HTTP $code)"
                    SourceHealthStatus.QUOTA_WARNING -> "Rate-limited (HTTP 429) • Cooldown window active"
                    else -> "Endpoint returned HTTP $code"
                }
                KickoffSourceHealthState(
                    provider = provider,
                    status = status,
                    statusDetail = detail,
                    remainingQuotaLabel = defaultQuotaLabel(provider, true),
                    isUsingCustomKey = false,
                    maskedKeyPreview = "Keyless Public API",
                    lastCheckedEpochMs = nowEpochMs
                )
            }
        } catch (e: Exception) {
            KickoffSourceHealthState(
                provider = provider,
                status = SourceHealthStatus.UNREACHABLE,
                statusDetail = "Unreachable: ${e.localizedMessage ?: "Network timeout"}",
                remainingQuotaLabel = defaultQuotaLabel(provider, true),
                isUsingCustomKey = false,
                maskedKeyPreview = "Keyless Public API",
                lastCheckedEpochMs = nowEpochMs
            )
        }
    }

    private fun buildInitialHealthStates(): List<KickoffSourceHealthState> {
        return KickoffProviderId.entries.map { provider ->
            val customKey = readDecryptedCustomKey(provider)
            val activeKey = resolveActiveApiKey(provider)
            val isConfigured = !provider.requiresApiKey || activeKey.isNotBlank()
            KickoffSourceHealthState(
                provider = provider,
                status = if (isConfigured) SourceHealthStatus.HEALTHY else SourceHealthStatus.UNCONFIGURED,
                statusDetail = when {
                    !provider.requiresApiKey -> "Public feed active • Zero API key required"
                    customKey.isNotBlank() -> "Custom API key active (AES-256 encrypted)"
                    isConfigured -> "Built-in API key active • Ready for live requests"
                    else -> "Optional key not set • Using multi-provider fallback"
                },
                remainingQuotaLabel = defaultQuotaLabel(provider, isConfigured),
                isUsingCustomKey = customKey.isNotBlank(),
                maskedKeyPreview = maskApiKey(activeKey, provider.requiresApiKey),
                lastCheckedEpochMs = 0L
            )
        }
    }

    private fun updateSingleProviderState(
        provider: KickoffProviderId,
        status: SourceHealthStatus,
        statusDetail: String,
        remainingQuotaLabel: String,
        lastCheckedEpochMs: Long
    ) {
        val customKey = readDecryptedCustomKey(provider)
        val activeKey = resolveActiveApiKey(provider)
        val masked = maskApiKey(activeKey, provider.requiresApiKey)
        _sourceHealthFlow.value = _sourceHealthFlow.value.map { existing ->
            if (existing.provider == provider) {
                existing.copy(
                    status = status,
                    statusDetail = statusDetail,
                    remainingQuotaLabel = remainingQuotaLabel,
                    isUsingCustomKey = customKey.isNotBlank(),
                    maskedKeyPreview = masked,
                    lastCheckedEpochMs = lastCheckedEpochMs
                )
            } else {
                existing
            }
        }
    }

    private fun refreshAlertBannerFromCurrentStates() {
        val states = _sourceHealthFlow.value
        val invalidKeyState = states.firstOrNull { it.status == SourceHealthStatus.KEY_INVALID_OR_EXPIRED }
        if (invalidKeyState != null) {
            _activeSourceAlertFlow.value =
                "${invalidKeyState.provider.displayName}: ${invalidKeyState.statusDetail}"
            return
        }
        val quotaState = states.firstOrNull { it.status == SourceHealthStatus.QUOTA_WARNING }
        if (quotaState != null) {
            _activeSourceAlertFlow.value =
                "${quotaState.provider.displayName}: ${quotaState.statusDetail}"
            return
        }
        val unreachableKeyedState = states.firstOrNull {
            it.provider.requiresApiKey && it.status == SourceHealthStatus.UNREACHABLE
        }
        if (unreachableKeyedState != null) {
            _activeSourceAlertFlow.value =
                "${unreachableKeyedState.provider.displayName}: ${unreachableKeyedState.statusDetail}"
            return
        }
        _activeSourceAlertFlow.value = null
    }

    companion object {
        const val PREFS_NAME = "setplay_kickoff_sources_v1"
        private const val KEY_PREFIX = "custom_api_key_"

        internal val activeCustomKeyCache = ConcurrentHashMap<KickoffProviderId, String>()

        private fun prefKeyForProvider(provider: KickoffProviderId): String =
            "$KEY_PREFIX${provider.id}"

        fun isCleanedKeyUsable(raw: String): Boolean {
            val trimmed = raw.trim()
            return trimmed.isNotBlank() &&
                !trimmed.startsWith("YOUR_", ignoreCase = true) &&
                !trimmed.startsWith("MY_", ignoreCase = true) &&
                !trimmed.equals("null", ignoreCase = true)
        }

        fun getDefaultBuildConfigKey(provider: KickoffProviderId): String {
            return when (provider) {
                KickoffProviderId.API_FOOTBALL -> {
                    val k = runCatching { BuildConfig.API_FOOTBALL_KEY.trim() }.getOrDefault("")
                    if (isCleanedKeyUsable(k)) k else ""
                }
                KickoffProviderId.FOOTBALL_DATA_ORG -> {
                    val k = runCatching { BuildConfig.FOOTBALL_DATA_KEY.trim() }.getOrDefault("")
                    if (isCleanedKeyUsable(k)) k else ""
                }
                KickoffProviderId.SPORT_API_AI -> {
                    val primary = runCatching { BuildConfig.SPORTAPI_AI_KEY.trim() }.getOrDefault("")
                    if (isCleanedKeyUsable(primary)) {
                        primary
                    } else {
                        val fallback = runCatching { BuildConfig.SPORT_API_KEY.trim() }.getOrDefault("")
                        if (isCleanedKeyUsable(fallback)) fallback else ""
                    }
                }
                else -> ""
            }
        }

        fun resolveCompanionActiveKey(provider: KickoffProviderId): String {
            val custom = activeCustomKeyCache[provider]?.trim().orEmpty()
            if (isCleanedKeyUsable(custom)) return custom
            return getDefaultBuildConfigKey(provider)
        }

        fun maskApiKey(key: String, requiresApiKey: Boolean): String {
            if (!requiresApiKey) return "Keyless Public API"
            val trimmed = key.trim()
            if (!isCleanedKeyUsable(trimmed)) return "Not Set (Optional)"
            if (trimmed.length <= 8) return "••••${trimmed.takeLast(2)}"
            return "${trimmed.take(4)}••••••••${trimmed.takeLast(4)}"
        }

        fun defaultQuotaLabel(provider: KickoffProviderId, isConfigured: Boolean): String {
            if (provider.requiresApiKey && !isConfigured) {
                return "Key not configured"
            }
            return when (provider) {
                KickoffProviderId.API_FOOTBALL -> "100 req/day (15 reserved for Dossiers)"
                KickoffProviderId.FOOTBALL_DATA_ORG -> "10 req/min sliding window"
                KickoffProviderId.SPORT_API_AI -> "2,000 req/day"
                KickoffProviderId.ESPN_SOCCER -> "Unlimited Public Scoreboard"
                KickoffProviderId.SOFASCORE_LIVE -> "Browser-Header Protected Feed"
                KickoffProviderId.THESPORTSDB -> "30 req/min shared limiter"
            }
        }
    }
}
