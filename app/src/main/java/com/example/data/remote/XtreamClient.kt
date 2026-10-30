package com.example.data.remote

import android.util.Base64
import android.util.Log
import com.example.data.model.CategoryEntity
import com.example.data.model.EpgProgramEntity
import com.example.data.model.StreamEntity
import com.example.util.SecurityManager
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class XtreamClient(
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()
) {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val authAdapter = moshi.adapter(XtreamAuthResponse::class.java)
    private val epgAdapter = moshi.adapter(XtreamShortEpgResponse::class.java)

    /**
     * Authenticates with Xtream Codes server and fetches account & server metadata.
     * Supports HTTPS-first connection fallback when a bare hostname is entered,
     * strict HttpUrl encoding, and credential log redaction.
     */
    suspend fun authenticate(
        serverUrl: String,
        username: String,
        password: String
    ): Result<XtreamAuthResponse> = withContext(Dispatchers.IO) {
        val plainPassword = SecurityManager.decryptCredential(password)
        val candidates = SecurityManager.buildHttpsFirstCandidateUrls(serverUrl)
        if (candidates.isEmpty()) {
            return@withContext Result.failure(IOException("Invalid server URL. Please enter a valid http:// or https:// address."))
        }

        var lastError: Exception = IOException("Unable to reach IPTV server")
        for (candidateBaseUrl in candidates) {
            val targetUrl = buildPlayerApiUrl(
                serverUrl = candidateBaseUrl,
                username = username,
                password = plainPassword
            ) ?: continue

            try {
                val request = Request.Builder()
                    .url(targetUrl)
                    .header("User-Agent", USER_AGENT)
                    .build()

                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        lastError = IOException("Server error HTTP ${response.code}")
                        return@use
                    }
                    val bodyStr = response.body?.string()
                    if (bodyStr.isNullOrBlank()) {
                        lastError = IOException("Empty response from server")
                        return@use
                    }
                    val authRes = authAdapter.fromJson(bodyStr)
                    if (authRes?.userInfo == null || authRes.userInfo.auth == 0) {
                        val msg = authRes?.userInfo?.message ?: "Invalid username or password"
                        return@withContext Result.failure(IOException(msg))
                    }
                    return@withContext Result.success(authRes)
                }
            } catch (e: Exception) {
                val safeMsg = SecurityManager.redactSensitiveText(e.message, username, plainPassword)
                Log.w("XtreamClient", "Auth attempt failed for $candidateBaseUrl: $safeMsg")
                lastError = IOException(safeMsg)
            }
        }

        Result.failure(lastError)
    }

    /**
     * Fetches categories using streaming JSON reader to handle any number of categories efficiently.
     */
    suspend fun fetchCategories(
        serverUrl: String,
        username: String,
        password: String,
        action: String, // get_live_categories, get_vod_categories, get_series_categories
        categoryType: String, // "LIVE", "MOVIE", "SERIES"
        playlistId: Long
    ): List<CategoryEntity> = withContext(Dispatchers.IO) {
        val plainPassword = SecurityManager.decryptCredential(password)
        val targetUrl = buildPlayerApiUrl(
            serverUrl = serverUrl,
            username = username,
            password = plainPassword,
            queryParams = mapOf("action" to action)
        ) ?: return@withContext emptyList()

        val categories = mutableListOf<CategoryEntity>()
        try {
            val request = Request.Builder()
                .url(targetUrl)
                .header("User-Agent", USER_AGENT)
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful || response.body == null) return@withContext emptyList()
                val reader = JsonReader.of(response.body!!.source())
                if (reader.peek() == JsonReader.Token.BEGIN_ARRAY) {
                    reader.beginArray()
                    while (reader.hasNext()) {
                        if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
                            var catId = ""
                            var catName = ""
                            try {
                                reader.beginObject()
                                while (reader.hasNext()) {
                                    when (reader.nextName()) {
                                        "category_id" -> catId = readSafeString(reader)
                                        "category_name" -> catName = readSafeString(reader)
                                        else -> reader.skipValue()
                                    }
                                }
                                reader.endObject()
                                if (catId.isNotEmpty() && catName.isNotEmpty()) {
                                    categories.add(
                                        CategoryEntity(
                                            playlistId = playlistId,
                                            categoryId = catId,
                                            categoryName = catName,
                                            type = categoryType
                                        )
                                    )
                                }
                            } catch (_: Exception) {
                                skipRestOfObject(reader)
                            }
                        } else {
                            reader.skipValue()
                        }
                    }
                    reader.endArray()
                }
            }
        } catch (e: Exception) {
            val safeMsg = SecurityManager.redactSensitiveText(e.message, username, plainPassword)
            Log.e("XtreamClient", "Failed to fetch categories ($action): $safeMsg")
        }
        categories
    }

    /**
     * Streams catalog items directly in bounded batches, calling onBatchReady(batch) so items
     * are persisted to Room immediately without accumulating huge lists in RAM.
     */
    suspend fun streamCatalogEntries(
        serverUrl: String,
        username: String,
        password: String,
        action: String, // get_live_streams, get_vod_streams, get_series
        streamType: String, // "live", "movie", "series"
        playlistId: Long,
        batchSize: Int = 500,
        onBatchReady: suspend (List<StreamEntity>, Int) -> Unit
    ): Int = withContext(Dispatchers.IO) {
        val plainPassword = SecurityManager.decryptCredential(password)
        val targetUrl = buildPlayerApiUrl(
            serverUrl = serverUrl,
            username = username,
            password = plainPassword,
            queryParams = mapOf("action" to action)
        ) ?: return@withContext 0

        var totalProcessed = 0
        val currentBatch = ArrayList<StreamEntity>(batchSize)
        val defaultExt = if (streamType == "live") "ts" else "mp4"

        try {
            val request = Request.Builder()
                .url(targetUrl)
                .header("User-Agent", USER_AGENT)
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful || response.body == null) return@withContext 0
                val reader = JsonReader.of(response.body!!.source())
                if (reader.peek() == JsonReader.Token.BEGIN_ARRAY) {
                    reader.beginArray()
                    while (reader.hasNext()) {
                        if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
                            var streamId = 0L
                            var categoryId = ""
                            var name = ""
                            var streamIcon = ""
                            var epgChannelId = ""
                            var rating = ""
                            var tvArchive = 0
                            var tvArchiveDuration = 0
                            var containerExt = defaultExt

                            try {
                                reader.beginObject()
                                while (reader.hasNext()) {
                                    when (reader.nextName()) {
                                        "stream_id", "series_id" -> streamId = readSafeLong(reader)
                                        "category_id" -> categoryId = readSafeString(reader)
                                        "name", "title" -> name = readSafeString(reader).trim()
                                        "stream_icon", "cover" -> streamIcon = readSafeString(reader)
                                        "epg_channel_id" -> epgChannelId = readSafeString(reader)
                                        "rating", "rating_5based" -> rating = readSafeString(reader)
                                        "tv_archive" -> tvArchive = readSafeInt(reader)
                                        "tv_archive_duration" -> tvArchiveDuration = readSafeInt(reader)
                                        "container_extension" -> {
                                            val ext = readSafeString(reader)
                                            if (ext.isNotEmpty()) {
                                                containerExt = SecurityManager.sanitizeContainerExtension(ext, defaultExt)
                                            }
                                        }
                                        else -> reader.skipValue()
                                    }
                                }
                                reader.endObject()

                                if (streamId > 0 && name.isNotEmpty()) {
                                    currentBatch.add(
                                        StreamEntity(
                                            playlistId = playlistId,
                                            streamId = streamId,
                                            categoryId = categoryId,
                                            name = name,
                                            streamType = streamType,
                                            streamIcon = streamIcon,
                                            epgChannelId = epgChannelId,
                                            rating = rating,
                                            tvArchive = tvArchive,
                                            tvArchiveDuration = tvArchiveDuration,
                                            containerExtension = containerExt
                                        )
                                    )
                                    totalProcessed++

                                    if (currentBatch.size >= batchSize) {
                                        onBatchReady(ArrayList(currentBatch), totalProcessed)
                                        currentBatch.clear()
                                    }
                                }
                            } catch (_: Exception) {
                                skipRestOfObject(reader)
                            }
                        } else {
                            reader.skipValue()
                        }
                    }
                    reader.endArray()

                    if (currentBatch.isNotEmpty()) {
                        onBatchReady(ArrayList(currentBatch), totalProcessed)
                        currentBatch.clear()
                    }
                }
            }
        } catch (e: Exception) {
            val safeMsg = SecurityManager.redactSensitiveText(e.message, username, plainPassword)
            Log.e("XtreamClient", "Error streaming catalog ($action): $safeMsg")
        }
        totalProcessed
    }

    /**
     * Fetches short EPG data for a given stream ID.
     */
    suspend fun fetchShortEpg(
        serverUrl: String,
        username: String,
        password: String,
        streamId: Long,
        playlistId: Long
    ): List<EpgProgramEntity> = withContext(Dispatchers.IO) {
        val plainPassword = SecurityManager.decryptCredential(password)
        val targetUrl = buildPlayerApiUrl(
            serverUrl = serverUrl,
            username = username,
            password = plainPassword,
            queryParams = mapOf(
                "action" to "get_short_epg",
                "stream_id" to streamId.toString(),
                "limit" to "10"
            )
        ) ?: return@withContext emptyList()

        val programs = mutableListOf<EpgProgramEntity>()
        try {
            val request = Request.Builder()
                .url(targetUrl)
                .header("User-Agent", USER_AGENT)
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful || response.body == null) return@withContext emptyList()
                val bodyStr = response.body?.string() ?: return@withContext emptyList()
                val epgData = epgAdapter.fromJson(bodyStr)
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

                epgData?.epgListings?.forEach { item ->
                    var decodedTitle = item.title ?: "Program"
                    if (decodedTitle.startsWith("=") || isBase64(decodedTitle)) {
                        try {
                            val decodedBytes = Base64.decode(decodedTitle, Base64.DEFAULT)
                            decodedTitle = String(decodedBytes, Charsets.UTF_8)
                        } catch (_: Exception) {}
                    }

                    var startEpoch = 0L
                    var stopEpoch = 0L

                    if (!item.startTimestamp.isNullOrEmpty()) {
                        startEpoch = item.startTimestamp.toLongOrNull() ?: 0L
                    } else if (!item.start.isNullOrEmpty()) {
                        startEpoch = try { sdf.parse(item.start)?.time?.div(1000) ?: 0L } catch (_: Exception) { 0L }
                    }

                    if (!item.stopTimestamp.isNullOrEmpty()) {
                        stopEpoch = item.stopTimestamp.toLongOrNull() ?: 0L
                    } else if (!item.end.isNullOrEmpty()) {
                        stopEpoch = try { sdf.parse(item.end)?.time?.div(1000) ?: 0L } catch (_: Exception) { 0L }
                    }

                    programs.add(
                        EpgProgramEntity(
                            playlistId = playlistId,
                            streamId = streamId,
                            title = decodedTitle,
                            description = item.description ?: "",
                            startEpoch = startEpoch,
                            endEpoch = stopEpoch
                        )
                    )
                }
            }
        } catch (e: Exception) {
            val safeMsg = SecurityManager.redactSensitiveText(e.message, username, plainPassword)
            Log.e("XtreamClient", "Failed to fetch EPG for stream $streamId: $safeMsg")
        }
        programs
    }

    /**
     * Fetches full Series metadata, seasons, and episodes from get_series_info.
     */
    suspend fun fetchSeriesDetails(
        serverUrl: String,
        username: String,
        password: String,
        seriesId: Long,
        fallbackTitle: String,
        fallbackCover: String,
        fallbackRating: String
    ): SeriesDetails = withContext(Dispatchers.IO) {
        val plainPassword = SecurityManager.decryptCredential(password)
        val targetUrl = buildPlayerApiUrl(
            serverUrl = serverUrl,
            username = username,
            password = plainPassword,
            queryParams = mapOf(
                "action" to "get_series_info",
                "series_id" to seriesId.toString()
            )
        )

        var title = fallbackTitle
        var cover = fallbackCover
        var plot = ""
        var genre = ""
        var cast = ""
        var releaseDate = ""
        var rating = fallbackRating
        val episodesBySeason = linkedMapOf<Int, MutableList<SeriesEpisode>>()

        if (targetUrl != null) {
            try {
                val request = Request.Builder()
                    .url(targetUrl)
                    .header("User-Agent", USER_AGENT)
                    .build()

                okHttpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful && response.body != null) {
                        val reader = JsonReader.of(response.body!!.source())
                        if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
                            reader.beginObject()
                            while (reader.hasNext()) {
                                when (reader.nextName()) {
                                    "info" -> {
                                        if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
                                            reader.beginObject()
                                            while (reader.hasNext()) {
                                                when (reader.nextName()) {
                                                    "name", "title" -> {
                                                        val s = readSafeString(reader)
                                                        if (s.isNotBlank()) title = s
                                                    }
                                                    "cover", "backdrop_path" -> {
                                                        val s = readSafeString(reader)
                                                        if (s.isNotBlank()) cover = s
                                                    }
                                                    "plot" -> plot = readSafeString(reader)
                                                    "genre" -> genre = readSafeString(reader)
                                                    "cast" -> cast = readSafeString(reader)
                                                    "releaseDate", "release_date" -> releaseDate = readSafeString(reader)
                                                    "rating", "rating_5based" -> {
                                                        val r = readSafeString(reader)
                                                        if (r.isNotBlank()) rating = r
                                                    }
                                                    else -> reader.skipValue()
                                                }
                                            }
                                            reader.endObject()
                                        } else {
                                            reader.skipValue()
                                        }
                                    }
                                    "episodes" -> {
                                        when (reader.peek()) {
                                            JsonReader.Token.BEGIN_OBJECT -> {
                                                reader.beginObject()
                                                while (reader.hasNext()) {
                                                    val seasonKey = reader.nextName()
                                                    val seasonNum = seasonKey.toIntOrNull() ?: 1
                                                    val parsed = parseEpisodesArray(reader, seasonNum, cover)
                                                    if (parsed.isNotEmpty()) {
                                                        episodesBySeason.getOrPut(seasonNum) { mutableListOf() }.addAll(parsed)
                                                    }
                                                }
                                                reader.endObject()
                                            }
                                            JsonReader.Token.BEGIN_ARRAY -> {
                                                reader.beginArray()
                                                var idxSeason = 1
                                                while (reader.hasNext()) {
                                                    val parsed = parseEpisodesArray(reader, idxSeason, cover)
                                                    if (parsed.isNotEmpty()) {
                                                        val sNum = parsed.first().seasonNumber.takeIf { it > 0 } ?: idxSeason
                                                        episodesBySeason.getOrPut(sNum) { mutableListOf() }.addAll(parsed)
                                                    }
                                                    idxSeason++
                                                }
                                                reader.endArray()
                                            }
                                            else -> reader.skipValue()
                                        }
                                    }
                                    else -> reader.skipValue()
                                }
                            }
                            reader.endObject()
                        }
                    }
                }
            } catch (e: Exception) {
                val safeMsg = SecurityManager.redactSensitiveText(e.message, username, plainPassword)
                Log.e("XtreamClient", "Failed to fetch series details for $seriesId: $safeMsg")
            }
        }

        val seasons = if (episodesBySeason.isNotEmpty()) {
            episodesBySeason.entries
                .sortedBy { it.key }
                .map { (seasonNum, epList) ->
                    SeriesSeason(
                        seasonNumber = seasonNum,
                        name = "Season $seasonNum",
                        episodes = epList.sortedBy { it.episodeNumber }
                    )
                }
        } else {
            listOf(
                SeriesSeason(
                    seasonNumber = 1,
                    name = "Season 1",
                    episodes = listOf(
                        SeriesEpisode(
                            episodeId = seriesId,
                            seasonNumber = 1,
                            episodeNumber = 1,
                            title = "$title - Episode 1",
                            containerExtension = "mp4",
                            duration = "45m",
                            plot = plot.ifBlank { "Stream episode directly from active Xtream server." },
                            thumbnail = cover,
                            rating = rating
                        )
                    )
                )
            )
        }

        SeriesDetails(
            seriesId = seriesId,
            title = title,
            coverUrl = cover,
            plot = plot,
            genre = genre,
            cast = cast,
            releaseDate = releaseDate,
            rating = rating,
            seasons = seasons
        )
    }

    private fun parseEpisodesArray(
        reader: JsonReader,
        defaultSeasonNum: Int,
        fallbackCover: String
    ): List<SeriesEpisode> {
        val list = mutableListOf<SeriesEpisode>()
        if (reader.peek() != JsonReader.Token.BEGIN_ARRAY) {
            reader.skipValue()
            return list
        }
        try {
            reader.beginArray()
            var index = 1
            while (reader.hasNext()) {
                if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
                    var epId = 0L
                    var epNum = index
                    var seasonNum = defaultSeasonNum
                    var epTitle = ""
                    var ext = "mp4"
                    var duration = ""
                    var plot = ""
                    var thumb = fallbackCover
                    var epRating = ""

                    try {
                        reader.beginObject()
                        while (reader.hasNext()) {
                            when (reader.nextName()) {
                                "id" -> epId = readSafeLong(reader)
                                "episode_num" -> {
                                    val n = readSafeInt(reader)
                                    if (n > 0) epNum = n
                                }
                                "season" -> {
                                    val s = readSafeInt(reader)
                                    if (s > 0) seasonNum = s
                                }
                                "title" -> epTitle = readSafeString(reader).trim()
                                "container_extension" -> {
                                    val e = readSafeString(reader).trim()
                                    if (e.isNotEmpty()) ext = SecurityManager.sanitizeContainerExtension(e, "mp4")
                                }
                                "info" -> {
                                    if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
                                        reader.beginObject()
                                        while (reader.hasNext()) {
                                            when (reader.nextName()) {
                                                "movie_image", "cover_big" -> {
                                                    val img = readSafeString(reader)
                                                    if (img.isNotBlank()) thumb = img
                                                }
                                                "plot" -> plot = readSafeString(reader)
                                                "duration" -> duration = readSafeString(reader)
                                                "rating" -> epRating = readSafeString(reader)
                                                else -> reader.skipValue()
                                            }
                                        }
                                        reader.endObject()
                                    } else {
                                        reader.skipValue()
                                    }
                                }
                                else -> reader.skipValue()
                            }
                        }
                        reader.endObject()
                        if (epId > 0L) {
                            list.add(
                                SeriesEpisode(
                                    episodeId = epId,
                                    seasonNumber = seasonNum,
                                    episodeNumber = epNum,
                                    title = epTitle.ifBlank { "Episode $epNum" },
                                    containerExtension = ext,
                                    duration = duration,
                                    plot = plot,
                                    thumbnail = thumb,
                                    rating = epRating
                                )
                            )
                        }
                    } catch (_: Exception) {
                        skipRestOfObject(reader)
                    }
                    index++
                } else {
                    reader.skipValue()
                }
            }
            reader.endArray()
        } catch (_: Exception) {
            try { reader.skipValue() } catch (_: Exception) {}
        }
        return list
    }

    private fun readSafeString(reader: JsonReader): String {
        return try {
            when (reader.peek()) {
                JsonReader.Token.STRING -> reader.nextString()
                JsonReader.Token.NUMBER -> {
                    try {
                        reader.nextString()
                    } catch (_: Exception) {
                        try { reader.nextLong().toString() } catch (_: Exception) { "" }
                    }
                }
                JsonReader.Token.BOOLEAN -> reader.nextBoolean().toString()
                JsonReader.Token.NULL -> {
                    reader.nextNull<Unit>()
                    ""
                }
                else -> {
                    reader.skipValue()
                    ""
                }
            }
        } catch (_: Exception) {
            try { reader.skipValue() } catch (_: Exception) {}
            ""
        }
    }

    private fun readSafeLong(reader: JsonReader): Long {
        return try {
            when (reader.peek()) {
                JsonReader.Token.NUMBER -> {
                    try {
                        reader.nextLong()
                    } catch (_: Exception) {
                        try { reader.nextDouble().toLong() } catch (_: Exception) { 0L }
                    }
                }
                JsonReader.Token.STRING -> {
                    val str = reader.nextString()
                    str.toLongOrNull() ?: 0L
                }
                JsonReader.Token.NULL -> {
                    reader.nextNull<Unit>()
                    0L
                }
                else -> {
                    reader.skipValue()
                    0L
                }
            }
        } catch (_: Exception) {
            try { reader.skipValue() } catch (_: Exception) {}
            0L
        }
    }

    private fun readSafeInt(reader: JsonReader): Int {
        return try {
            when (reader.peek()) {
                JsonReader.Token.NUMBER -> {
                    try {
                        reader.nextInt()
                    } catch (_: Exception) {
                        try { reader.nextDouble().toInt() } catch (_: Exception) { 0 }
                    }
                }
                JsonReader.Token.STRING -> {
                    val str = reader.nextString()
                    str.toIntOrNull() ?: 0
                }
                JsonReader.Token.NULL -> {
                    reader.nextNull<Unit>()
                    0
                }
                else -> {
                    reader.skipValue()
                    0
                }
            }
        } catch (_: Exception) {
            try { reader.skipValue() } catch (_: Exception) {}
            0
        }
    }

    private fun skipRestOfObject(reader: JsonReader) {
        try {
            while (reader.hasNext()) {
                reader.skipValue()
            }
            if (reader.peek() == JsonReader.Token.END_OBJECT) {
                reader.endObject()
            }
        } catch (_: Exception) {
            try { reader.skipValue() } catch (_: Exception) {}
        }
    }

    private fun isBase64(s: String): Boolean {
        if (s.length < 4 || s.length % 4 != 0) return false
        return s.all { it.isLetterOrDigit() || it == '+' || it == '/' || it == '=' }
    }

    companion object {
        private const val USER_AGENT = "SetPlay-IPTV/3.0"

        fun cleanBaseUrl(url: String): String {
            return SecurityManager.sanitizeServerUrl(url, defaultScheme = "http")
        }

        /**
         * Safely builds an Xtream `player_api.php` URL using OkHttp [HttpUrl.Builder]
         * so credentials and parameters are strictly URL-encoded against query injection.
         */
        fun buildPlayerApiUrl(
            serverUrl: String,
            username: String,
            password: String,
            queryParams: Map<String, String> = emptyMap()
        ): HttpUrl? {
            val base = cleanBaseUrl(serverUrl)
            if (base.isEmpty()) return null
            val parsedBase = base.toHttpUrlOrNull() ?: return null
            val plainPass = SecurityManager.decryptCredential(password)

            val builder = parsedBase.newBuilder()
                .addPathSegment("player_api.php")
                .addQueryParameter("username", username.trim())
                .addQueryParameter("password", plainPass.trim())

            for ((k, v) in queryParams) {
                builder.addQueryParameter(k, v)
            }
            return builder.build()
        }

        /**
         * Safely builds a playback stream URL with sanitized extensions and encoded path segments.
         */
        fun buildStreamUrl(
            serverUrl: String,
            username: String,
            password: String,
            streamType: String,
            streamId: Long,
            containerExtension: String
        ): String {
            val base = cleanBaseUrl(serverUrl)
            val plainPass = SecurityManager.decryptCredential(password).trim()
            val cleanUser = username.trim()
            val typeLower = streamType.lowercase()
            val defaultExt = if (typeLower == "live") "ts" else "mp4"
            val safeExt = SecurityManager.sanitizeContainerExtension(containerExtension, defaultExt)

            val parsedBase = base.toHttpUrlOrNull()
            if (parsedBase != null) {
                val folder = when (typeLower) {
                    "movie" -> "movie"
                    "series" -> "series"
                    else -> "live"
                }
                val fileSegment = if (typeLower == "live") "$streamId.ts" else "$streamId.$safeExt"
                return parsedBase.newBuilder()
                    .addPathSegment(folder)
                    .addPathSegment(cleanUser)
                    .addPathSegment(plainPass)
                    .addPathSegment(fileSegment)
                    .build()
                    .toString()
            }

            return when (typeLower) {
                "movie" -> "$base/movie/$cleanUser/$plainPass/$streamId.$safeExt"
                "series" -> "$base/series/$cleanUser/$plainPass/$streamId.$safeExt"
                else -> "$base/live/$cleanUser/$plainPass/$streamId.ts"
            }
        }

        fun buildCatchupUrl(
            serverUrl: String,
            username: String,
            password: String,
            streamId: Long,
            startEpochSeconds: Long,
            durationMinutes: Int
        ): String {
            val base = cleanBaseUrl(serverUrl)
            val plainPass = SecurityManager.decryptCredential(password).trim()
            val cleanUser = username.trim()
            val sdf = SimpleDateFormat("yyyy-MM-dd:HH-mm", Locale.US)
            val formattedStart = sdf.format(java.util.Date(startEpochSeconds * 1000))

            val parsedBase = base.toHttpUrlOrNull()
            if (parsedBase != null) {
                return parsedBase.newBuilder()
                    .addPathSegment("streaming")
                    .addPathSegment("timeshift.php")
                    .addQueryParameter("username", cleanUser)
                    .addQueryParameter("password", plainPass)
                    .addQueryParameter("stream", streamId.toString())
                    .addQueryParameter("start", formattedStart)
                    .addQueryParameter("duration", durationMinutes.toString())
                    .build()
                    .toString()
            }

            return "$base/streaming/timeshift.php?username=$cleanUser&password=$plainPass&stream=$streamId&start=$formattedStart&duration=$durationMinutes"
        }
    }
}
