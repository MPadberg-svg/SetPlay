package com.example.data.sync

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Locale
import java.util.Random
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

enum class SyncRole {
    STANDALONE,
    TV_HOST,
    PHONE_CONTROLLER
}

data class DiscoveredTvHost(
    val serviceName: String,
    val hostIp: String,
    val port: Int
)

data class TvSyncSessionState(
    val role: SyncRole = SyncRole.STANDALONE,
    val isSyncActive: Boolean = false,
    val localIpAddress: String = "",
    val serverPort: Int = 8765,
    val pairingPin: String = "",
    val connectedPeerName: String = "",
    val tvNowPlayingTitle: String = "",
    val tvNowPlayingStreamId: String = "",
    val tvNowPlayingType: String = "",
    val tvNowPlayingUrl: String = "",
    val tvNowPlayingPositionMs: Long = 0L,
    val tvIsPlaying: Boolean = false,
    val tvActiveSubtitleLang: String = "",
    val discoveredTvHosts: List<DiscoveredTvHost> = emptyList(),
    val savedTvHostIp: String = "",
    val savedTvHostPort: Int = 8765,
    val savedTvHostPin: String = "",
    val savedTvHostName: String = "",
    val lastSyncStatusMessage: String = ""
)

data class VaultSyncPayload(
    val playlistName: String,
    val serverUrl: String,
    val username: String,
    val password: String,
    val playlistType: String = "XTREAM",
    val favoriteStreamIds: List<String> = emptyList(),
    val countryZoneCode: String = "MA",
    val preferredSubtitleLang: String = "",
    val episodeResumeMap: Map<String, Long> = emptyMap()
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("playlistName", playlistName)
            put("serverUrl", serverUrl)
            put("username", username)
            put("password", password)
            put("playlistType", playlistType)
            put("favoriteStreamIds", JSONArray(favoriteStreamIds))
            put("countryZoneCode", countryZoneCode)
            put("preferredSubtitleLang", preferredSubtitleLang)
            val resumeObj = JSONObject()
            episodeResumeMap.forEach { (k, v) -> resumeObj.put(k, v) }
            put("episodeResumeMap", resumeObj)
        }
    }

    companion object {
        fun fromJson(obj: JSONObject?): VaultSyncPayload {
            if (obj == null) {
                return VaultSyncPayload(
                    playlistName = "",
                    serverUrl = "",
                    username = "",
                    password = ""
                )
            }
            val favArr = obj.optJSONArray("favoriteStreamIds")
            val favs = buildList {
                if (favArr != null) {
                    for (i in 0 until favArr.length()) {
                        val id = favArr.optString(i).trim()
                        if (id.isNotEmpty()) add(id)
                    }
                }
            }
            val resumeObj = obj.optJSONObject("episodeResumeMap")
            val resumes = mutableMapOf<String, Long>()
            if (resumeObj != null) {
                val keys = resumeObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val pos = resumeObj.optLong(key, 0L)
                    if (pos > 0L) resumes[key] = pos
                }
            }
            return VaultSyncPayload(
                playlistName = obj.optString("playlistName", ""),
                serverUrl = obj.optString("serverUrl", ""),
                username = obj.optString("username", ""),
                password = obj.optString("password", ""),
                playlistType = obj.optString("playlistType", "XTREAM"),
                favoriteStreamIds = favs,
                countryZoneCode = obj.optString("countryZoneCode", "MA"),
                preferredSubtitleLang = obj.optString("preferredSubtitleLang", ""),
                episodeResumeMap = resumes
            )
        }
    }
}

data class RemoteNavMirrorCommand(
    val screenName: String,
    val categoryId: String? = null,
    val searchQuery: String = "",
    val matchDayName: String? = null
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("screenName", screenName)
            put("categoryId", categoryId ?: "")
            put("searchQuery", searchQuery)
            put("matchDayName", matchDayName ?: "")
        }
    }

    companion object {
        fun fromJson(obj: JSONObject): RemoteNavMirrorCommand {
            return RemoteNavMirrorCommand(
                screenName = obj.optString("screenName", "HOME"),
                categoryId = obj.optString("categoryId", "").takeIf { it.isNotEmpty() },
                searchQuery = obj.optString("searchQuery", ""),
                matchDayName = obj.optString("matchDayName", "").takeIf { it.isNotEmpty() }
            )
        }
    }
}

data class RemotePlayCommand(
    val streamId: String,
    val streamName: String,
    val streamType: String,
    val streamUrl: String = "",
    val categoryId: String = "",
    val streamIcon: String = "",
    val epgChannelId: String = "",
    val containerExtension: String = "",
    val resumePositionMs: Long = 0L,
    val preferredSubtitleLang: String = ""
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("streamId", streamId)
            put("streamName", streamName)
            put("streamType", streamType)
            put("streamUrl", streamUrl)
            put("categoryId", categoryId)
            put("streamIcon", streamIcon)
            put("epgChannelId", epgChannelId)
            put("containerExtension", containerExtension)
            put("resumePositionMs", resumePositionMs)
            put("preferredSubtitleLang", preferredSubtitleLang)
        }
    }

    companion object {
        fun fromJson(obj: JSONObject): RemotePlayCommand {
            return RemotePlayCommand(
                streamId = obj.optString("streamId", ""),
                streamName = obj.optString("streamName", ""),
                streamType = obj.optString("streamType", "live"),
                streamUrl = obj.optString("streamUrl", ""),
                categoryId = obj.optString("categoryId", ""),
                streamIcon = obj.optString("streamIcon", ""),
                epgChannelId = obj.optString("epgChannelId", ""),
                containerExtension = obj.optString("containerExtension", "ts"),
                resumePositionMs = obj.optLong("resumePositionMs", 0L),
                preferredSubtitleLang = obj.optString("preferredSubtitleLang", "")
            )
        }
    }
}

data class RemoteControlCommand(
    val action: String,
    val value: String = "",
    val seekSeconds: Int = 0
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("action", action)
            put("value", value)
            put("seekSeconds", seekSeconds)
        }
    }

    companion object {
        fun fromJson(obj: JSONObject): RemoteControlCommand {
            return RemoteControlCommand(
                action = obj.optString("action", ""),
                value = obj.optString("value", ""),
                seekSeconds = obj.optInt("seekSeconds", 0)
            )
        }
    }
}

/**
 * Local Wi-Fi Phone ↔ TV QR Companion & Sync Engine:
 * - Zero External Cloud required: uses a lightweight embedded HTTP/JSON server (`ServerSocket` on port 8765)
 *   + Android `NsdManager` (`_setplay._tcp`) LAN discovery + QR & 6-Digit Pairing PIN.
 * - Single-Connection Safe: When the phone is in `PHONE_CONTROLLER` Sync Mode, selecting any channel,
 *   movie, series episode, or match sends `POST /api/sync/play` to the TV so ONLY the TV's ExoPlayer
 *   connects to the IPTV provider.
 * - WhatsApp & TV Remote Freedom: The TV streams autonomously from the IPTV server; the phone user can
 *   leave the app at any time or rejoin in 1 tap.
 */
class TvSyncManager(
    context: Context,
    private val httpClient: OkHttpClient = defaultSyncHttpClient()
) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _sessionState = MutableStateFlow(
        TvSyncSessionState(
            localIpAddress = "",
            savedTvHostIp = prefs.getString(KEY_LAST_TV_IP, "").orEmpty(),
            savedTvHostPort = prefs.getInt(KEY_LAST_TV_PORT, DEFAULT_PORT),
            savedTvHostPin = prefs.getString(KEY_LAST_TV_PIN, "").orEmpty(),
            savedTvHostName = prefs.getString(KEY_LAST_TV_NAME, "").orEmpty()
        )
    )
    val sessionState: StateFlow<TvSyncSessionState> = _sessionState.asStateFlow()

    private val _incomingNavCommands = MutableSharedFlow<RemoteNavMirrorCommand>(extraBufferCapacity = 16)
    val incomingNavCommands: SharedFlow<RemoteNavMirrorCommand> = _incomingNavCommands.asSharedFlow()

    private val _incomingPlayCommands = MutableSharedFlow<RemotePlayCommand>(extraBufferCapacity = 16)
    val incomingPlayCommands: SharedFlow<RemotePlayCommand> = _incomingPlayCommands.asSharedFlow()

    private val _incomingControlCommands = MutableSharedFlow<RemoteControlCommand>(extraBufferCapacity = 16)
    val incomingControlCommands: SharedFlow<RemoteControlCommand> = _incomingControlCommands.asSharedFlow()

    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val serverGeneration = AtomicLong(0L)
    private val httpClientSemaphore = Semaphore(permits = MAX_CONCURRENT_HTTP_CLIENTS)
    private var nsdRegistrationListener: NsdManager.RegistrationListener? = null
    private var nsdDiscoveryListener: NsdManager.DiscoveryListener? = null
    private val nsdResolveLock = Any()
    private val pendingNsdResolveQueue = ArrayDeque<NsdServiceInfo>()
    private var isNsdResolveInFlight = false

    init {
        scope.launch(Dispatchers.IO) {
            val lanIp = detectLocalLanIpv4()
            _sessionState.update { current ->
                if (current.localIpAddress.isBlank()) current.copy(localIpAddress = lanIp) else current
            }
        }
    }

    // Active Phone Controller target connection details
    @Volatile
    private var activeTargetHostIp: String = ""
    @Volatile
    private var activeTargetPort: Int = DEFAULT_PORT
    @Volatile
    private var activeTargetPin: String = ""

    // Host callbacks wired by MainViewModel / MainActivity
    var onVaultPayloadReceived: (suspend (VaultSyncPayload) -> VaultSyncPayload)? = null
    var onNavMirrorReceived: (suspend (RemoteNavMirrorCommand) -> Unit)? = null
    var onPlayCommandReceived: (suspend (RemotePlayCommand) -> Unit)? = null
    var onControlCommandReceived: (suspend (RemoteControlCommand) -> Unit)? = null
    var tvPlaybackStatusProvider: (() -> TvSyncSessionState)? = null

    /**
     * Starts the embedded HTTP/JSON TV Host Server on `preferredPort` (default 8765, with automatic
     * fallback port selection if busy) and registers `_setplay._tcp.` on Android NSD.
     */
    fun startTvHostServer(
        preferredPort: Int = DEFAULT_PORT,
        customPin: String? = null
    ): TvSyncSessionState {
        if (_sessionState.value.role == SyncRole.TV_HOST && serverSocket?.isClosed == false) {
            return _sessionState.value
        }

        val persistedPin = prefs.getString(KEY_PERSISTENT_TV_PIN, "").orEmpty()
            .takeIf { it.length == 6 && it.all(Char::isDigit) }
        val pin = customPin?.takeIf { it.length == 6 && it.all(Char::isDigit) }
            ?: persistedPin
            ?: String.format(Locale.US, "%06d", Random().nextInt(900_000) + 100_000).also { generated ->
                prefs.edit().putString(KEY_PERSISTENT_TV_PIN, generated).apply()
            }

        val generation = serverGeneration.incrementAndGet()
        val pendingState = _sessionState.updateAndGet { current ->
            current.copy(
                role = SyncRole.TV_HOST,
                isSyncActive = true,
                serverPort = preferredPort,
                pairingPin = pin,
                lastSyncStatusMessage = "Starting TV Host on port $preferredPort (PIN: $pin)"
            )
        }

        serverJob?.cancel()
        serverJob = scope.launch(Dispatchers.IO) {
            stopServerSocketAndNsdOnIo()
            if (serverGeneration.get() != generation) return@launch

            var boundSocket: ServerSocket? = null
            var boundPort = preferredPort
            for (candidate in preferredPort..(preferredPort + 10)) {
                try {
                    boundSocket = ServerSocket().apply {
                        reuseAddress = true
                        bind(InetSocketAddress("0.0.0.0", candidate))
                    }
                    boundPort = candidate
                    break
                } catch (_: Exception) {
                    // Try next port
                }
            }
            if (boundSocket == null) {
                boundSocket = ServerSocket(0)
                boundPort = boundSocket.localPort
            }

            if (serverGeneration.get() != generation || !isActive) {
                runCatching { boundSocket.close() }
                return@launch
            }

            serverSocket = boundSocket
            val lanIp = detectLocalLanIpv4()
            _sessionState.update { current ->
                if (serverGeneration.get() != generation) {
                    current
                } else {
                    current.copy(
                        role = SyncRole.TV_HOST,
                        isSyncActive = true,
                        localIpAddress = lanIp,
                        serverPort = boundPort,
                        pairingPin = pin,
                        lastSyncStatusMessage = "TV Host listening on $lanIp:$boundPort (PIN: $pin)"
                    )
                }
            }

            registerNsdService(boundPort)

            while (isActive && boundSocket.isClosed.not() && serverGeneration.get() == generation) {
                val client = try {
                    boundSocket.accept()
                } catch (_: Exception) {
                    break
                }
                launch(Dispatchers.IO) {
                    handleIncomingHttpClient(client)
                }
            }
        }
        return pendingState
    }

    private fun writeHttpJsonResponse(
        socket: Socket,
        statusCode: Int,
        statusText: String,
        responseJson: JSONObject
    ) {
        val responseBytes = responseJson.toString().toByteArray(Charsets.UTF_8)
        val writer = PrintWriter(OutputStreamWriter(socket.getOutputStream(), Charsets.UTF_8), false)
        writer.print("HTTP/1.1 $statusCode $statusText\r\n")
        writer.print("Content-Type: application/json; charset=utf-8\r\n")
        writer.print("Content-Length: ${responseBytes.size}\r\n")
        writer.print("Connection: close\r\n\r\n")
        writer.flush()
        socket.getOutputStream().write(responseBytes)
        socket.getOutputStream().flush()
    }

    private suspend fun handleIncomingHttpClient(socket: Socket) = withContext(Dispatchers.IO) {
        httpClientSemaphore.withPermit {
            try {
                socket.soTimeout = 8_000
                val reader = BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.UTF_8))
                val requestLine = reader.readLine() ?: return@withPermit
                val parts = requestLine.split(" ")
                val method = parts.getOrNull(0)?.uppercase(Locale.ROOT) ?: "GET"
                val path = parts.getOrNull(1) ?: "/"

                var declaredContentLength: Long = 0L
                val headers = mutableMapOf<String, String>()
                while (true) {
                    val headerLine = reader.readLine() ?: break
                    if (headerLine.isEmpty()) break
                    val colonIdx = headerLine.indexOf(':')
                    if (colonIdx > 0) {
                        val key = headerLine.substring(0, colonIdx).trim().lowercase(Locale.ROOT)
                        val value = headerLine.substring(colonIdx + 1).trim()
                        headers[key] = value
                        if (key == "content-length") {
                            declaredContentLength = value.toLongOrNull() ?: -1L
                        }
                    }
                }

                if (declaredContentLength < 0L || declaredContentLength > MAX_HTTP_BODY_BYTES.toLong()) {
                    writeHttpJsonResponse(
                        socket = socket,
                        statusCode = 413,
                        statusText = "Payload Too Large",
                        responseJson = JSONObject().put("error", "Payload Too Large")
                    )
                    return@withPermit
                }

                val contentLength = declaredContentLength.toInt()
                val bodyText = if (contentLength > 0) {
                    val buffer = CharArray(contentLength)
                    var totalRead = 0
                    while (totalRead < contentLength) {
                        val read = reader.read(buffer, totalRead, contentLength - totalRead)
                        if (read == -1) break
                        totalRead += read
                    }
                    String(buffer, 0, totalRead)
                } else {
                    ""
                }

                val (statusCode, responseJson) = routeHttpRequest(method, path, bodyText, headers)
                val statusText = when (statusCode) {
                    200 -> "OK"
                    403 -> "Forbidden"
                    413 -> "Payload Too Large"
                    else -> "Bad Request"
                }
                writeHttpJsonResponse(socket, statusCode, statusText, responseJson)
            } catch (_: Exception) {
            } finally {
                runCatching { socket.close() }
            }
        }
    }

    private fun extractPinFromRequest(
        path: String,
        reqJson: JSONObject?,
        headers: Map<String, String>
    ): String {
        val headerPin = headers["x-setplay-pin"]?.trim().orEmpty()
        if (headerPin.isNotEmpty()) return headerPin
        val bodyPin = reqJson?.optString("pin", "")?.trim().orEmpty()
        if (bodyPin.isNotEmpty()) return bodyPin
        val query = path.substringAfter("?", "")
        if (query.isNotEmpty()) {
            for (pair in query.split("&")) {
                if (pair.startsWith("pin=", ignoreCase = true)) {
                    return pair.substringAfter("=").trim()
                }
            }
        }
        return ""
    }

    private fun isPinAuthorized(
        path: String,
        reqJson: JSONObject?,
        headers: Map<String, String>
    ): Boolean {
        val currentPin = _sessionState.value.pairingPin.trim()
        if (currentPin.isEmpty()) return true
        val providedPin = extractPinFromRequest(path, reqJson, headers)
        return providedPin == currentPin
    }

    private suspend fun routeHttpRequest(
        method: String,
        path: String,
        bodyText: String,
        headers: Map<String, String> = emptyMap()
    ): Pair<Int, JSONObject> {
        val cleanPath = path.substringBefore("?")
        return when {
            method == "GET" && cleanPath == "/api/sync/status" -> {
                if (!isPinAuthorized(path, null, headers)) {
                    return 403 to JSONObject().put("error", "Unauthorized: Invalid Pairing PIN")
                }
                val liveStatus = tvPlaybackStatusProvider?.invoke() ?: _sessionState.value
                val updatedStatus = _sessionState.updateAndGet { current ->
                    current.copy(
                        tvNowPlayingTitle = liveStatus.tvNowPlayingTitle,
                        tvNowPlayingStreamId = liveStatus.tvNowPlayingStreamId,
                        tvNowPlayingType = liveStatus.tvNowPlayingType,
                        tvNowPlayingUrl = liveStatus.tvNowPlayingUrl,
                        tvNowPlayingPositionMs = liveStatus.tvNowPlayingPositionMs,
                        tvIsPlaying = liveStatus.tvIsPlaying,
                        tvActiveSubtitleLang = liveStatus.tvActiveSubtitleLang
                    )
                }
                200 to buildStatusJson(updatedStatus)
            }

            method == "POST" && cleanPath == "/api/sync/pair" -> {
                val reqJson = runCatching { JSONObject(bodyText) }.getOrNull()
                    ?: return 400 to JSONObject().put("error", "Invalid JSON")
                if (!isPinAuthorized(path, reqJson, headers)) {
                    return 403 to JSONObject().put("error", "Invalid 6-digit Pairing PIN")
                }

                val peerName = reqJson.optString("peerName", "SetPlay Phone").ifBlank { "SetPlay Phone" }
                val incomingVault = VaultSyncPayload.fromJson(reqJson.optJSONObject("vault"))
                val replyVault = onVaultPayloadReceived?.invoke(incomingVault) ?: incomingVault

                _sessionState.update { current ->
                    current.copy(
                        isSyncActive = true,
                        connectedPeerName = peerName,
                        lastSyncStatusMessage = "Paired with $peerName • Accounts & Favorites Synced"
                    )
                }
                val statusSnapshot = tvPlaybackStatusProvider?.invoke() ?: _sessionState.value
                val resp = buildStatusJson(statusSnapshot).apply {
                    put("status", "paired")
                    put("hostName", getLocalDeviceName())
                    put("vault", replyVault.toJson())
                }
                200 to resp
            }

            method == "POST" && cleanPath == "/api/sync/navigate" -> {
                val reqJson = runCatching { JSONObject(bodyText) }.getOrNull()
                    ?: return 400 to JSONObject().put("error", "Invalid JSON")
                if (!isPinAuthorized(path, reqJson, headers)) {
                    return 403 to JSONObject().put("error", "Unauthorized: Invalid Pairing PIN")
                }
                val cmd = RemoteNavMirrorCommand.fromJson(reqJson)
                _incomingNavCommands.tryEmit(cmd)
                onNavMirrorReceived?.invoke(cmd)
                200 to buildStatusJson(_sessionState.value).put("status", "navigated")
            }

            method == "POST" && cleanPath == "/api/sync/play" -> {
                val reqJson = runCatching { JSONObject(bodyText) }.getOrNull()
                    ?: return 400 to JSONObject().put("error", "Invalid JSON")
                if (!isPinAuthorized(path, reqJson, headers)) {
                    return 403 to JSONObject().put("error", "Unauthorized: Invalid Pairing PIN")
                }
                val cmd = RemotePlayCommand.fromJson(reqJson)
                val playState = _sessionState.updateAndGet { current ->
                    current.copy(
                        tvNowPlayingTitle = cmd.streamName,
                        tvNowPlayingStreamId = cmd.streamId,
                        tvNowPlayingType = cmd.streamType,
                        tvNowPlayingUrl = cmd.streamUrl,
                        tvNowPlayingPositionMs = cmd.resumePositionMs,
                        tvIsPlaying = true,
                        tvActiveSubtitleLang = cmd.preferredSubtitleLang,
                        lastSyncStatusMessage = "Playing ${cmd.streamName} on TV"
                    )
                }
                _incomingPlayCommands.tryEmit(cmd)
                onPlayCommandReceived?.invoke(cmd)
                200 to buildStatusJson(playState).put("status", "playing_on_tv")
            }

            method == "POST" && cleanPath == "/api/sync/control" -> {
                val reqJson = runCatching { JSONObject(bodyText) }.getOrNull()
                    ?: return 400 to JSONObject().put("error", "Invalid JSON")
                if (!isPinAuthorized(path, reqJson, headers)) {
                    return 403 to JSONObject().put("error", "Unauthorized: Invalid Pairing PIN")
                }
                val cmd = RemoteControlCommand.fromJson(reqJson)
                val preControlSnapshot = tvPlaybackStatusProvider?.invoke() ?: _sessionState.value
                when (cmd.action.uppercase(Locale.ROOT)) {
                    "TOGGLE_PLAY_PAUSE" -> {
                        _sessionState.update { current ->
                            current.copy(tvIsPlaying = !current.tvIsPlaying)
                        }
                    }
                    "STOP" -> {
                        _sessionState.update { current ->
                            current.copy(
                                tvNowPlayingTitle = "",
                                tvNowPlayingStreamId = "",
                                tvNowPlayingUrl = "",
                                tvNowPlayingPositionMs = 0L,
                                tvIsPlaying = false
                            )
                        }
                    }
                    "SET_SUBTITLE_LANG" -> {
                        _sessionState.update { current ->
                            current.copy(tvActiveSubtitleLang = cmd.value)
                        }
                    }
                    "ZAP_NEXT", "ZAP_PREV" -> {
                        // Handled by onControlCommandReceived (zapChannelOffset(1) / zapChannelOffset(-1))
                        // followed by tvPlaybackStatusProvider refresh below
                    }
                }
                _incomingControlCommands.tryEmit(cmd)
                onControlCommandReceived?.invoke(cmd)
                val updated = if (cmd.action.equals("STOP", ignoreCase = true)) {
                    _sessionState.value
                } else {
                    val latest = tvPlaybackStatusProvider?.invoke()
                    if (latest != null) {
                        _sessionState.updateAndGet { current ->
                            current.copy(
                                tvNowPlayingTitle = latest.tvNowPlayingTitle,
                                tvNowPlayingStreamId = latest.tvNowPlayingStreamId,
                                tvNowPlayingType = latest.tvNowPlayingType,
                                tvNowPlayingUrl = latest.tvNowPlayingUrl,
                                tvNowPlayingPositionMs = latest.tvNowPlayingPositionMs,
                                tvIsPlaying = latest.tvIsPlaying,
                                tvActiveSubtitleLang = latest.tvActiveSubtitleLang
                            )
                        }
                    } else {
                        _sessionState.value
                    }
                }
                val resp = buildStatusJson(updated).apply {
                    put("status", "control_applied")
                    if (cmd.action.equals("STOP", ignoreCase = true)) {
                        put("stoppedStreamId", preControlSnapshot.tvNowPlayingStreamId)
                        put("stoppedTitle", preControlSnapshot.tvNowPlayingTitle)
                        put("stoppedType", preControlSnapshot.tvNowPlayingType)
                        put("stoppedUrl", preControlSnapshot.tvNowPlayingUrl)
                        put("stoppedPositionMs", preControlSnapshot.tvNowPlayingPositionMs)
                    }
                }
                200 to resp
            }

            else -> 400 to JSONObject().put("error", "Unknown endpoint: $cleanPath")
        }
    }

    private fun buildStatusJson(state: TvSyncSessionState): JSONObject {
        return JSONObject().apply {
            put("tvNowPlayingTitle", state.tvNowPlayingTitle)
            put("tvNowPlayingStreamId", state.tvNowPlayingStreamId)
            put("tvNowPlayingType", state.tvNowPlayingType)
            // Never broadcast raw Xtream URLs containing username/password in status responses
            put("tvNowPlayingUrl", "")
            put("tvNowPlayingPositionMs", state.tvNowPlayingPositionMs)
            put("tvIsPlaying", state.tvIsPlaying)
            put("tvActiveSubtitleLang", state.tvActiveSubtitleLang)
            put("connectedPeerName", state.connectedPeerName)
        }
    }

    private fun resolveActiveControllerPin(state: TvSyncSessionState = _sessionState.value): String {
        return activeTargetPin.ifBlank {
            state.savedTvHostPin.ifBlank { state.pairingPin }
        }.trim()
    }

    /**
     * Pairs this Phone with a TV Host over Local Wi-Fi using IP, Port, and 6-Digit Pairing PIN,
     * exchanging `VaultSyncPayload` so both devices share the same Xtream credentials, Favorites,
     * Country Zone, and Episode Resume positions.
     */
    suspend fun connectToTvHost(
        hostIp: String,
        port: Int = DEFAULT_PORT,
        pin: String,
        localVault: VaultSyncPayload,
        peerDeviceName: String = getLocalDeviceName(),
        onRemoteVaultReceived: (suspend (VaultSyncPayload) -> Unit)? = null
    ): Result<TvSyncSessionState> = withContext(Dispatchers.IO) {
        val cleanIp = hostIp.trim()
        val cleanPin = pin.trim()
        if (cleanIp.isEmpty() || cleanPin.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter both TV IP address and 6-digit PIN."))
        }

        try {
            val reqBodyJson = JSONObject().apply {
                put("pin", cleanPin)
                put("peerName", peerDeviceName)
                put("vault", localVault.toJson())
            }
            val request = Request.Builder()
                .url("http://$cleanIp:$port/api/sync/pair")
                .header("X-SetPlay-Pin", cleanPin)
                .post(reqBodyJson.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            httpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()
                val respJson = runCatching { JSONObject(bodyStr) }.getOrNull()
                if (!response.isSuccessful || respJson == null) {
                    val errMsg = respJson?.optString("error")?.takeIf { it.isNotEmpty() }
                        ?: "Pairing failed (HTTP ${response.code})"
                    return@withContext Result.failure(IllegalStateException(errMsg))
                }

                val remoteHostName = respJson.optString("hostName", "Living Room TV").ifBlank { "Living Room TV" }
                val remoteVault = VaultSyncPayload.fromJson(respJson.optJSONObject("vault"))
                onRemoteVaultReceived?.invoke(remoteVault)

                activeTargetHostIp = cleanIp
                activeTargetPort = port
                activeTargetPin = cleanPin
                saveLastPairedTvHost(cleanIp, port, cleanPin, remoteHostName)

                val localLanIp = detectLocalLanIpv4()
                val newState = _sessionState.updateAndGet { current ->
                    current.copy(
                        role = SyncRole.PHONE_CONTROLLER,
                        isSyncActive = true,
                        localIpAddress = localLanIp,
                        serverPort = port,
                        pairingPin = cleanPin,
                        connectedPeerName = remoteHostName,
                        tvNowPlayingTitle = respJson.optString("tvNowPlayingTitle", ""),
                        tvNowPlayingStreamId = respJson.optString("tvNowPlayingStreamId", ""),
                        tvNowPlayingType = respJson.optString("tvNowPlayingType", ""),
                        tvNowPlayingUrl = respJson.optString("tvNowPlayingUrl", ""),
                        tvNowPlayingPositionMs = respJson.optLong("tvNowPlayingPositionMs", 0L),
                        tvIsPlaying = respJson.optBoolean("tvIsPlaying", false),
                        tvActiveSubtitleLang = respJson.optString("tvActiveSubtitleLang", ""),
                        savedTvHostIp = cleanIp,
                        savedTvHostPort = port,
                        savedTvHostPin = cleanPin,
                        savedTvHostName = remoteHostName,
                        lastSyncStatusMessage = "Connected to $remoteHostName ($cleanIp)"
                    )
                }
                Result.success(newState)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sends a non-blocking navigation mirror command from Phone Controller to TV Host.
     */
    fun sendNavMirror(command: RemoteNavMirrorCommand) {
        val state = _sessionState.value
        if (state.role != SyncRole.PHONE_CONTROLLER || !state.isSyncActive || activeTargetHostIp.isBlank()) return
        val pin = resolveActiveControllerPin(state)
        scope.launch {
            runCatching {
                val payloadJson = command.toJson().apply { put("pin", pin) }
                val request = Request.Builder()
                    .url("http://$activeTargetHostIp:$activeTargetPort/api/sync/navigate")
                    .header("X-SetPlay-Pin", pin)
                    .post(payloadJson.toString().toRequestBody(JSON_MEDIA_TYPE))
                    .build()
                httpClient.newCall(request).execute().close()
            }
        }
    }

    /**
     * Dispatches `POST /api/sync/play` from Phone Controller to TV Host so ONLY the TV streams the media.
     */
    suspend fun sendPlayOnTv(command: RemotePlayCommand): Result<TvSyncSessionState> = withContext(Dispatchers.IO) {
        val state = _sessionState.value
        if (state.role != SyncRole.PHONE_CONTROLLER || !state.isSyncActive || activeTargetHostIp.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Phone is not currently in TV Sync Mode"))
        }
        val pin = resolveActiveControllerPin(state)

        // Optimistically update the companion bar immediately for zero-latency UX
        _sessionState.update { current ->
            current.copy(
                tvNowPlayingTitle = command.streamName,
                tvNowPlayingStreamId = command.streamId,
                tvNowPlayingType = command.streamType,
                tvNowPlayingUrl = command.streamUrl,
                tvNowPlayingPositionMs = command.resumePositionMs,
                tvIsPlaying = true,
                tvActiveSubtitleLang = command.preferredSubtitleLang,
                lastSyncStatusMessage = "Broadcasting \"${command.streamName}\" on ${current.connectedPeerName}"
            )
        }

        try {
            val payloadJson = command.toJson().apply { put("pin", pin) }
            val request = Request.Builder()
                .url("http://$activeTargetHostIp:$activeTargetPort/api/sync/play")
                .header("X-SetPlay-Pin", pin)
                .post(payloadJson.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()
            httpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()
                val respJson = runCatching { JSONObject(bodyStr) }.getOrNull()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IllegalStateException("TV play command failed (${response.code})"))
                }
                if (respJson != null) {
                    applyStatusJsonFromTv(respJson)
                }
                Result.success(_sessionState.value)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sends a lightweight remote control command (`TOGGLE_PLAY_PAUSE`, `STOP`, `SEEK_RELATIVE`, `SET_SUBTITLE_LANG`)
     * from the Phone's 52.dp Companion Bar to the TV Host.
     */
    fun sendControlToTv(action: String, value: String = "", seekSeconds: Int = 0) {
        val state = _sessionState.value
        if (state.role != SyncRole.PHONE_CONTROLLER || !state.isSyncActive || activeTargetHostIp.isBlank()) return

        // Optimistic UI update on the companion bar
        when (action.uppercase(Locale.ROOT)) {
            "TOGGLE_PLAY_PAUSE" -> {
                _sessionState.update { current -> current.copy(tvIsPlaying = !current.tvIsPlaying) }
            }
            "SET_SUBTITLE_LANG" -> {
                _sessionState.update { current -> current.copy(tvActiveSubtitleLang = value) }
            }
            "STOP" -> {
                _sessionState.update { current ->
                    current.copy(
                        tvNowPlayingTitle = "",
                        tvNowPlayingStreamId = "",
                        tvIsPlaying = false
                    )
                }
            }
        }

        scope.launch {
            sendControlToTvSuspend(action = action, value = value, seekSeconds = seekSeconds)
        }
    }

    suspend fun sendControlToTvSuspend(
        action: String,
        value: String = "",
        seekSeconds: Int = 0
    ): Result<TvSyncSessionState> = withContext(Dispatchers.IO) {
        val state = _sessionState.value
        if (state.role != SyncRole.PHONE_CONTROLLER || !state.isSyncActive || activeTargetHostIp.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Not in Phone Controller Sync Mode"))
        }
        val pin = resolveActiveControllerPin(state)
        try {
            val cmd = RemoteControlCommand(action = action, value = value, seekSeconds = seekSeconds)
            val payloadJson = cmd.toJson().apply { put("pin", pin) }
            val request = Request.Builder()
                .url("http://$activeTargetHostIp:$activeTargetPort/api/sync/control")
                .header("X-SetPlay-Pin", pin)
                .post(payloadJson.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()
            httpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()
                val respJson = runCatching { JSONObject(bodyStr) }.getOrNull()
                if (response.isSuccessful && respJson != null) {
                    applyStatusJsonFromTv(respJson)
                    Result.success(_sessionState.value)
                } else {
                    Result.failure(IllegalStateException("Control command failed (${response.code})"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pulls the currently playing TV stream back to the Phone ("📱 Watch Here"):
     * 1. Sends `POST /api/sync/control` (`action = "STOP"`) to the TV Host first and waits for confirmation
     *    so the TV closes its stream connection before the phone opens one (`max_connections = 1` safe).
     * 2. Returns a `RemotePlayCommand` with the TV's `stoppedPositionMs` so the phone can resume seamlessly.
     */
    suspend fun pullTvStreamToPhone(): Result<RemotePlayCommand> = withContext(Dispatchers.IO) {
        val state = _sessionState.value
        if (state.role != SyncRole.PHONE_CONTROLLER || !state.isSyncActive || activeTargetHostIp.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Not in Phone Controller Sync Mode"))
        }
        val pin = resolveActiveControllerPin(state)
        val fallbackStreamId = state.tvNowPlayingStreamId
        val fallbackTitle = state.tvNowPlayingTitle
        val fallbackType = state.tvNowPlayingType.ifBlank { "live" }
        val fallbackUrl = state.tvNowPlayingUrl
        val fallbackPos = state.tvNowPlayingPositionMs

        try {
            val stopCmd = RemoteControlCommand(action = "STOP")
            val payloadJson = stopCmd.toJson().apply { put("pin", pin) }
            val request = Request.Builder()
                .url("http://$activeTargetHostIp:$activeTargetPort/api/sync/control")
                .header("X-SetPlay-Pin", pin)
                .post(payloadJson.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()
            httpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()
                val respJson = runCatching { JSONObject(bodyStr) }.getOrNull()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IllegalStateException("Failed to stop TV playback (${response.code})"))
                }
                val streamId = respJson?.optString("stoppedStreamId")?.takeIf { it.isNotEmpty() } ?: fallbackStreamId
                val title = respJson?.optString("stoppedTitle")?.takeIf { it.isNotEmpty() } ?: fallbackTitle
                val type = respJson?.optString("stoppedType")?.takeIf { it.isNotEmpty() } ?: fallbackType
                val url = respJson?.optString("stoppedUrl")?.takeIf { it.isNotEmpty() } ?: fallbackUrl
                val posMs = respJson?.optLong("stoppedPositionMs", fallbackPos) ?: fallbackPos

                _sessionState.update { current ->
                    current.copy(
                        tvNowPlayingTitle = "",
                        tvNowPlayingStreamId = "",
                        tvNowPlayingType = "",
                        tvNowPlayingUrl = "",
                        tvNowPlayingPositionMs = 0L,
                        tvIsPlaying = false,
                        lastSyncStatusMessage = "Pulled \"$title\" to phone"
                    )
                }

                if (streamId.isEmpty() && title.isEmpty()) {
                    return@withContext Result.failure(IllegalStateException("No active stream on TV to pull"))
                }
                Result.success(
                    RemotePlayCommand(
                        streamId = streamId,
                        streamName = title,
                        streamType = type,
                        streamUrl = url,
                        resumePositionMs = posMs
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Queries `GET /api/sync/status` on the TV Host (called automatically when the user returns to
     * the phone app from WhatsApp or other apps) so the Companion Bar reflects live TV playback state.
     */
    suspend fun refreshTvStatus(): Result<TvSyncSessionState> = withContext(Dispatchers.IO) {
        val state = _sessionState.value
        if (state.role != SyncRole.PHONE_CONTROLLER || !state.isSyncActive || activeTargetHostIp.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Not in Phone Controller Sync Mode"))
        }
        val pin = resolveActiveControllerPin(state)
        try {
            val request = Request.Builder()
                .url("http://$activeTargetHostIp:$activeTargetPort/api/sync/status")
                .header("X-SetPlay-Pin", pin)
                .get()
                .build()
            httpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()
                val respJson = runCatching { JSONObject(bodyStr) }.getOrNull()
                if (response.isSuccessful && respJson != null) {
                    applyStatusJsonFromTv(respJson)
                    Result.success(_sessionState.value)
                } else {
                    Result.failure(IllegalStateException("Failed to fetch TV status"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun applyStatusJsonFromTv(respJson: JSONObject) {
        _sessionState.update { current ->
            current.copy(
                tvNowPlayingTitle = respJson.optString("tvNowPlayingTitle", current.tvNowPlayingTitle),
                tvNowPlayingStreamId = respJson.optString("tvNowPlayingStreamId", current.tvNowPlayingStreamId),
                tvNowPlayingType = respJson.optString("tvNowPlayingType", current.tvNowPlayingType),
                tvNowPlayingUrl = respJson.optString("tvNowPlayingUrl", current.tvNowPlayingUrl),
                tvNowPlayingPositionMs = respJson.optLong("tvNowPlayingPositionMs", current.tvNowPlayingPositionMs),
                tvIsPlaying = respJson.optBoolean("tvIsPlaying", current.tvIsPlaying),
                tvActiveSubtitleLang = respJson.optString("tvActiveSubtitleLang", current.tvActiveSubtitleLang)
            )
        }
    }

    /**
     * Leaves Sync Mode on the phone or stops the TV Host server, returning to `STANDALONE` mode
     * while keeping the last paired TV saved in `SharedPreferences` for 1-tap reconnection.
     */
    fun leaveSyncMode() {
        serverGeneration.incrementAndGet()
        serverJob?.cancel()
        serverJob = null
        stopNsdDiscovery()
        _sessionState.update { current ->
            TvSyncSessionState(
                role = SyncRole.STANDALONE,
                isSyncActive = false,
                localIpAddress = current.localIpAddress,
                discoveredTvHosts = current.discoveredTvHosts,
                savedTvHostIp = prefs.getString(KEY_LAST_TV_IP, "").orEmpty(),
                savedTvHostPort = prefs.getInt(KEY_LAST_TV_PORT, DEFAULT_PORT),
                savedTvHostPin = prefs.getString(KEY_LAST_TV_PIN, "").orEmpty(),
                savedTvHostName = prefs.getString(KEY_LAST_TV_NAME, "").orEmpty(),
                lastSyncStatusMessage = "Disconnected from TV Sync Mode"
            )
        }
        scope.launch(Dispatchers.IO) {
            stopServerSocketAndNsdOnIo()
            val lanIp = detectLocalLanIpv4()
            _sessionState.update { current -> current.copy(localIpAddress = lanIp) }
        }
    }

    fun shutdown() {
        serverGeneration.incrementAndGet()
        stopNsdDiscovery()
        serverJob?.cancel()
        serverJob = null
        runCatching { serverSocket?.close() }
        serverSocket = null
        runCatching {
            val nsdManager = appContext.getSystemService(Context.NSD_SERVICE) as? NsdManager
            nsdRegistrationListener?.let { nsdManager?.unregisterService(it) }
        }
        nsdRegistrationListener = null
        runCatching { scope.coroutineContext[Job]?.cancel() }
    }

    fun hasSavedTvHost(): Boolean {
        val savedIp = prefs.getString(KEY_LAST_TV_IP, "").orEmpty()
        val savedPin = prefs.getString(KEY_LAST_TV_PIN, "").orEmpty()
        return savedIp.isNotBlank() && savedPin.isNotBlank()
    }

    fun getSavedTvHostSummary(): Triple<String, Int, String>? {
        val savedIp = prefs.getString(KEY_LAST_TV_IP, "").orEmpty().trim()
        val savedPort = prefs.getInt(KEY_LAST_TV_PORT, DEFAULT_PORT)
        val savedPin = prefs.getString(KEY_LAST_TV_PIN, "").orEmpty().trim()
        if (savedIp.isEmpty() || savedPin.isEmpty()) return null
        return Triple(savedIp, savedPort, savedPin)
    }

    fun getSavedTvHostName(): String {
        return prefs.getString(KEY_LAST_TV_NAME, "Living Room TV").orEmpty().ifBlank { "Living Room TV" }
    }

    suspend fun rejoinLastTvHost(
        localVault: VaultSyncPayload,
        onRemoteVaultReceived: (suspend (VaultSyncPayload) -> Unit)? = null
    ): Result<TvSyncSessionState> {
        val (savedIp, savedPort, savedPin) = getSavedTvHostSummary()
            ?: return Result.failure(IllegalStateException("No previously paired TV found"))
        return connectToTvHost(
            hostIp = savedIp,
            port = savedPort,
            pin = savedPin,
            localVault = localVault,
            onRemoteVaultReceived = onRemoteVaultReceived
        )
    }

    private fun saveLastPairedTvHost(ip: String, port: Int, pin: String, hostName: String) {
        prefs.edit()
            .putString(KEY_LAST_TV_IP, ip)
            .putInt(KEY_LAST_TV_PORT, port)
            .putString(KEY_LAST_TV_PIN, pin)
            .putString(KEY_LAST_TV_NAME, hostName)
            .apply()
    }

    fun buildQrPairingPayload(): String {
        val s = _sessionState.value
        val ip = s.localIpAddress.ifBlank { detectLocalLanIpv4() }
        val encodedName = URLEncoder.encode(getLocalDeviceName(), "UTF-8")
        return "setplay://sync?ip=$ip&port=${s.serverPort}&pin=${s.pairingPin}&name=$encodedName"
    }

    fun startNsdDiscovery() {
        runCatching {
            stopNsdDiscovery()
            val nsdManager = appContext.getSystemService(Context.NSD_SERVICE) as? NsdManager ?: return
            val listener = object : NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(regType: String?) {}
                override fun onServiceFound(service: NsdServiceInfo?) {
                    val info = service ?: return
                    if (!info.serviceType.contains("_setplay._tcp")) return
                    enqueueNsdResolve(nsdManager, info)
                }
                override fun onServiceLost(service: NsdServiceInfo?) {
                    val lostName = service?.serviceName?.takeIf { it.isNotBlank() } ?: return
                    synchronized(nsdResolveLock) {
                        pendingNsdResolveQueue.removeAll { it.serviceName == lostName }
                    }
                    _sessionState.update { current ->
                        val remaining = current.discoveredTvHosts.filterNot {
                            it.serviceName.equals(lostName, ignoreCase = true)
                        }
                        if (remaining.size != current.discoveredTvHosts.size) {
                            current.copy(discoveredTvHosts = remaining)
                        } else {
                            current
                        }
                    }
                }
                override fun onDiscoveryStopped(serviceType: String?) {}
                override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {}
                override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {}
            }
            nsdDiscoveryListener = listener
            nsdManager.discoverServices(NSD_SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, listener)
        }
    }

    private fun enqueueNsdResolve(nsdManager: NsdManager, info: NsdServiceInfo) {
        val shouldStart = synchronized(nsdResolveLock) {
            if (pendingNsdResolveQueue.none { it.serviceName == info.serviceName }) {
                pendingNsdResolveQueue.addLast(info)
            }
            if (!isNsdResolveInFlight) {
                isNsdResolveInFlight = true
                true
            } else {
                false
            }
        }
        if (shouldStart) {
            drainNextNsdResolve(nsdManager)
        }
    }

    private fun drainNextNsdResolve(nsdManager: NsdManager) {
        val nextInfo = synchronized(nsdResolveLock) {
            if (pendingNsdResolveQueue.isEmpty()) {
                isNsdResolveInFlight = false
                null
            } else {
                pendingNsdResolveQueue.removeFirst()
            }
        } ?: return

        val resolveAttempt = runCatching {
            @Suppress("DEPRECATION")
            nsdManager.resolveService(nextInfo, object : NsdManager.ResolveListener {
                override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                    drainNextNsdResolve(nsdManager)
                }

                override fun onServiceResolved(resolvedInfo: NsdServiceInfo?) {
                    try {
                        val resolved = resolvedInfo ?: return
                        @Suppress("DEPRECATION")
                        val hostIp = resolved.host?.hostAddress ?: return
                        val port = resolved.port
                        val hostItem = DiscoveredTvHost(
                            serviceName = resolved.serviceName ?: "SetPlay TV",
                            hostIp = hostIp,
                            port = port
                        )
                        _sessionState.update { current ->
                            if (current.discoveredTvHosts.none { it.hostIp == hostIp && it.port == port }) {
                                current.copy(discoveredTvHosts = current.discoveredTvHosts + hostItem)
                            } else {
                                current
                            }
                        }
                    } finally {
                        drainNextNsdResolve(nsdManager)
                    }
                }
            })
        }
        if (resolveAttempt.isFailure) {
            drainNextNsdResolve(nsdManager)
        }
    }

    fun stopNsdDiscovery() {
        synchronized(nsdResolveLock) {
            pendingNsdResolveQueue.clear()
            isNsdResolveInFlight = false
        }
        runCatching {
            val nsdManager = appContext.getSystemService(Context.NSD_SERVICE) as? NsdManager
            nsdDiscoveryListener?.let { nsdManager?.stopServiceDiscovery(it) }
        }
        nsdDiscoveryListener = null
    }

    private fun registerNsdService(port: Int) {
        runCatching {
            val nsdManager = appContext.getSystemService(Context.NSD_SERVICE) as? NsdManager ?: return
            val serviceInfo = NsdServiceInfo().apply {
                serviceName = "SetPlay-TV-${Build.MODEL.take(12)}"
                serviceType = NSD_SERVICE_TYPE
                setPort(port)
            }
            val regListener = object : NsdManager.RegistrationListener {
                override fun onServiceRegistered(nsdServiceInfo: NsdServiceInfo?) {}
                override fun onRegistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {}
                override fun onServiceUnregistered(arg0: NsdServiceInfo?) {}
                override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {}
            }
            nsdRegistrationListener = regListener
            nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, regListener)
        }
    }

    private fun stopServerSocketAndNsdOnIo() {
        runCatching {
            val nsdManager = appContext.getSystemService(Context.NSD_SERVICE) as? NsdManager
            nsdRegistrationListener?.let { nsdManager?.unregisterService(it) }
        }
        nsdRegistrationListener = null
        runCatching { serverSocket?.close() }
        serverSocket = null
    }

    companion object {
        const val DEFAULT_PORT = 8765
        const val NSD_SERVICE_TYPE = "_setplay._tcp."
        internal const val MAX_HTTP_BODY_BYTES = 256 * 1024
        private const val MAX_CONCURRENT_HTTP_CLIENTS = 8
        private const val PREFS_NAME = "setplay_tv_sync_prefs"
        private const val KEY_PERSISTENT_TV_PIN = "persistent_tv_host_pin"
        private const val KEY_LAST_TV_IP = "last_tv_ip"
        private const val KEY_LAST_TV_PORT = "last_tv_port"
        private const val KEY_LAST_TV_PIN = "last_tv_pin"
        private const val KEY_LAST_TV_NAME = "last_tv_name"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        fun defaultSyncHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(4, TimeUnit.SECONDS)
                .readTimeout(6, TimeUnit.SECONDS)
                .writeTimeout(6, TimeUnit.SECONDS)
                .build()
        }

        fun getLocalDeviceName(): String {
            val model = Build.MODEL?.trim().orEmpty()
            return if (model.isNotEmpty()) model else "SetPlay Device"
        }

        private val IGNORED_INTERFACE_TOKENS = listOf(
            "rmnet",
            "ccmni",
            "pdp",
            "wwan",
            "clat",
            "tun",
            "tap",
            "ppp",
            "wg",
            "dummy",
            "lo"
        )

        private val PREFERRED_LAN_PREFIXES = listOf(
            "wlan",
            "eth",
            "en",
            "ap",
            "swlan"
        )

        fun isIgnoredCellularOrTunnelInterface(name: String, displayName: String = name): Boolean {
            val lowerName = name.trim().lowercase(Locale.ROOT)
            val lowerDisplay = displayName.trim().lowercase(Locale.ROOT)
            if (lowerName.isEmpty()) return true
            return IGNORED_INTERFACE_TOKENS.any { token ->
                lowerName.startsWith(token) ||
                    lowerName.contains(token) ||
                    lowerDisplay.startsWith(token) ||
                    lowerDisplay.contains(token)
            }
        }

        fun isPrivateIpv4(ip: String, allowClassA10: Boolean): Boolean {
            val parts = ip.trim().split(".")
            if (parts.size != 4) return false
            val o0 = parts[0].toIntOrNull() ?: return false
            val o1 = parts[1].toIntOrNull() ?: return false
            val o2 = parts[2].toIntOrNull() ?: return false
            val o3 = parts[3].toIntOrNull() ?: return false
            if (o0 !in 0..255 || o1 !in 0..255 || o2 !in 0..255 || o3 !in 0..255) return false
            if (o0 == 192 && o1 == 168) return true
            if (o0 == 172 && o1 in 16..31) return true
            if (allowClassA10 && o0 == 10) return true
            return false
        }

        /**
         * Selects the best Home Wi-Fi / Ethernet IPv4 address from `(interfaceName, ipAddress)` pairs:
         * - Explicitly ignores cellular (`rmnet*`, `ccmni*`, `pdp*`, `wwan*`, `clat*`) and VPN/tunnel (`tun*`, `tap*`, `ppp*`, `wg*`) interfaces.
         * - Pass 1 (Preferred Home Wi-Fi / Ethernet): Scans `wlan*`, `eth*`, `en*`, `ap*`, `swlan*` for `192.168.*`, `172.16-31.*`, or `10.*`.
         * - Pass 2 (Fallback Site-Local): Scans remaining non-cellular/non-VPN interfaces for `192.168.*` or `172.16-31.*`.
         */
        fun selectBestLanIpv4FromCandidates(candidates: List<Pair<String, String>>): String {
            // Pass 1: Preferred Home Wi-Fi / Ethernet interfaces
            for ((ifName, ip) in candidates) {
                if (isIgnoredCellularOrTunnelInterface(ifName)) continue
                val lower = ifName.trim().lowercase(Locale.ROOT)
                val isPreferredLan = PREFERRED_LAN_PREFIXES.any { lower.startsWith(it) }
                if (isPreferredLan && isPrivateIpv4(ip, allowClassA10 = true)) {
                    return ip
                }
            }
            // Pass 2: Remaining non-cellular/non-VPN interfaces with 192.168.* or 172.16-31.*
            for ((ifName, ip) in candidates) {
                if (isIgnoredCellularOrTunnelInterface(ifName)) continue
                if (isPrivateIpv4(ip, allowClassA10 = false)) {
                    return ip
                }
            }
            return "127.0.0.1"
        }

        fun detectLocalLanIpv4(): String {
            return try {
                val interfaces = NetworkInterface.getNetworkInterfaces()?.toList().orEmpty()
                val candidates = mutableListOf<Pair<String, String>>()
                for (intf in interfaces) {
                    if (!intf.isUp || intf.isLoopback || intf.isPointToPoint) continue
                    val name = intf.name.orEmpty()
                    val displayName = intf.displayName.orEmpty()
                    if (isIgnoredCellularOrTunnelInterface(name, displayName)) continue
                    for (addr in intf.inetAddresses.toList()) {
                        if (addr is Inet4Address && !addr.isLoopbackAddress && !addr.isLinkLocalAddress) {
                            val ip = addr.hostAddress.orEmpty()
                            if (ip.isNotEmpty()) {
                                candidates.add(name to ip)
                            }
                        }
                    }
                }
                selectBestLanIpv4FromCandidates(candidates)
            } catch (_: Exception) {
                "127.0.0.1"
            }
        }

        fun isHomeLanConnected(context: Context? = null): Boolean {
            if (detectLocalLanIpv4() != "127.0.0.1") return true
            if (context != null) {
                runCatching {
                    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
                    val activeNet = cm?.activeNetwork
                    val caps = if (activeNet != null) cm.getNetworkCapabilities(activeNet) else null
                    if (caps != null && (
                            caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) ||
                                caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET)
                        )
                    ) {
                        return true
                    }
                }
            }
            return false
        }

        /**
         * Parses a `setplay://sync?ip=192.168.1.50&port=8765&pin=123456` QR payload into `(ip, port, pin)`.
         */
        fun parseQrPairingPayload(raw: String): Triple<String, Int, String>? {
            val trimmed = raw.trim()
            if (!trimmed.startsWith("setplay://sync", ignoreCase = true)) return null
            val query = trimmed.substringAfter("?", "")
            if (query.isEmpty()) return null
            val params = query.split("&").associate { pair ->
                val k = pair.substringBefore("=").lowercase(Locale.ROOT)
                val v = runCatching { URLDecoder.decode(pair.substringAfter("=", ""), "UTF-8") }
                    .getOrDefault(pair.substringAfter("=", ""))
                k to v
            }
            val ip = params["ip"].orEmpty().trim()
            val port = params["port"]?.toIntOrNull() ?: DEFAULT_PORT
            val pin = params["pin"].orEmpty().trim()
            if (ip.isEmpty() || pin.isEmpty()) return null
            return Triple(ip, port, pin)
        }
    }
}
