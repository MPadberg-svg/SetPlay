package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.PlaylistEntity
import com.example.data.model.StreamEntity
import com.squareup.moshi.JsonReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Buffer
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.Locale
import java.util.concurrent.TimeUnit

data class WyzieSubtitleOption(
    val id: String,
    val url: String,
    val languageCode: String,
    val languageDisplay: String,
    val flagEmoji: String,
    val mediaName: String,
    val format: String,
    val source: String,
    val encoding: String,
    val isHearingImpaired: Boolean,
    val season: Int? = null,
    val episode: Int? = null
)

data class WyzieLanguageGroup(
    val languageCode: String,
    val languageDisplay: String,
    val flagEmoji: String,
    val options: List<WyzieSubtitleOption>
)

data class SubtitleCue(
    val startTimeMs: Long,
    val endTimeMs: Long,
    val text: String
)

data class ResolvedMediaQuery(
    val cleanTitle: String,
    val year: Int? = null,
    val imdbId: String? = null,
    val tmdbId: String? = null,
    val isSeries: Boolean = false,
    val season: Int? = null,
    val episode: Int? = null
)

data class WyzieSearchResponse(
    val resolvedQuery: ResolvedMediaQuery,
    val languageGroups: List<WyzieLanguageGroup>,
    val totalCount: Int,
    val statusMessage: String? = null
)

class WyzieSubtitleService(
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()
) {
    /**
     * Resolves a Movie or Series stream (or custom user query) to its IMDb/TMDb ID
     * and fetches all available online subtitles from Wyzie Subs API, grouped by language.
     */
    suspend fun fetchSubtitlesForStream(
        stream: StreamEntity,
        playlist: PlaylistEntity?,
        customSearchQuery: String? = null,
        overrideSeason: Int? = null,
        overrideEpisode: Int? = null
    ): WyzieSearchResponse = withContext(Dispatchers.IO) {
        val resolved = resolveMediaQuery(
            stream = stream,
            playlist = playlist,
            customSearchQuery = customSearchQuery,
            overrideSeason = overrideSeason,
            overrideEpisode = overrideEpisode
        )

        val candidateIds = buildList {
            if (!resolved.imdbId.isNullOrBlank()) add(resolved.imdbId)
            if (!resolved.tmdbId.isNullOrBlank() && resolved.tmdbId != resolved.imdbId) add(resolved.tmdbId)
        }

        if (candidateIds.isEmpty()) {
            return@withContext WyzieSearchResponse(
                resolvedQuery = resolved,
                languageGroups = emptyList(),
                totalCount = 0,
                statusMessage = "Could not match '${resolved.cleanTitle}' to an IMDb/TMDb ID. Try entering the movie title or IMDb ID (e.g. tt15398776) above."
            )
        }

        val apiKey = resolveWyzieApiKey()
        var options = emptyList<WyzieSubtitleOption>()
        var lastError: String? = null

        for (mediaId in candidateIds) {
            val result = queryWyzieEndpoints(
                mediaId = mediaId,
                apiKey = apiKey,
                season = if (resolved.isSeries) resolved.season else null,
                episode = if (resolved.isSeries) resolved.episode else null,
                fallbackTitle = resolved.cleanTitle
            )
            if (result.first.isNotEmpty()) {
                options = result.first
                lastError = null
                break
            } else if (result.second != null) {
                lastError = result.second
            }
        }

        // If series search with season/episode returned 0, retry without episode filter as fallback
        if (options.isEmpty() && resolved.isSeries && resolved.season != null) {
            for (mediaId in candidateIds) {
                val fallbackResult = queryWyzieEndpoints(
                    mediaId = mediaId,
                    apiKey = apiKey,
                    season = null,
                    episode = null,
                    fallbackTitle = resolved.cleanTitle
                )
                if (fallbackResult.first.isNotEmpty()) {
                    options = fallbackResult.first
                    lastError = null
                    break
                }
            }
        }

        val grouped = groupSubtitlesByLanguage(options)
        val statusMsg = when {
            grouped.isNotEmpty() -> null
            lastError != null -> lastError
            else -> "No subtitles found for '${resolved.cleanTitle}' (${candidateIds.first()})."
        }

        WyzieSearchResponse(
            resolvedQuery = resolved,
            languageGroups = grouped,
            totalCount = options.size,
            statusMessage = statusMsg
        )
    }

    /**
     * Downloads a subtitle file (.srt or .vtt) from Wyzie and parses it into timed [SubtitleCue] items.
     */
    suspend fun downloadAndParseSubtitle(
        option: WyzieSubtitleOption
    ): Result<List<SubtitleCue>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(option.url)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "*/*")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful || response.body == null) {
                    return@withContext Result.failure(
                        IllegalStateException("Subtitle download failed (HTTP ${response.code})")
                    )
                }
                val bytes = response.body!!.bytes()
                val charset = try {
                    if (option.encoding.isNotBlank()) Charset.forName(option.encoding) else Charsets.UTF_8
                } catch (_: Exception) {
                    Charsets.UTF_8
                }
                val rawText = String(bytes, charset).removePrefix("\uFEFF")
                val cues = parseSubtitleText(rawText)
                if (cues.isEmpty()) {
                    return@withContext Result.failure(
                        IllegalStateException("Subtitle file contained no readable timestamp cues")
                    )
                }
                Result.success(cues)
            }
        } catch (e: Exception) {
            Log.w("WyzieSubtitleService", "Failed to download subtitle: ${e.message}")
            Result.failure(e)
        }
    }

    private fun resolveWyzieApiKey(): String {
        val buildConfigKey = try {
            BuildConfig.WYZIE_API_KEY.trim()
        } catch (_: Exception) {
            ""
        }
        return if (
            buildConfigKey.isNotEmpty() &&
            !buildConfigKey.startsWith("YOUR_", ignoreCase = true) &&
            buildConfigKey != "null"
        ) {
            buildConfigKey
        } else {
            ""
        }
    }

    private fun queryWyzieEndpoints(
        mediaId: String,
        apiKey: String,
        season: Int?,
        episode: Int?,
        fallbackTitle: String
    ): Pair<List<WyzieSubtitleOption>, String?> {
        val baseUrls = listOf(
            "https://sub.wyzie.ru/search",
            "https://sub.wyzie.io/search"
        )
        var lastErrorMsg: String? = null

        for (baseUrl in baseUrls) {
            val httpUrlBuilder = baseUrl.toHttpUrlOrNull()?.newBuilder() ?: continue
            httpUrlBuilder.addQueryParameter("id", mediaId)
            if (apiKey.isNotEmpty()) {
                httpUrlBuilder.addQueryParameter("key", apiKey)
            }
            if (season != null && season > 0 && episode != null && episode > 0) {
                httpUrlBuilder.addQueryParameter("season", season.toString())
                httpUrlBuilder.addQueryParameter("episode", episode.toString())
            }

            try {
                val request = Request.Builder()
                    .url(httpUrlBuilder.build())
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "application/json")
                    .build()

                okHttpClient.newCall(request).execute().use { response ->
                    val bodyStr = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        lastErrorMsg = if (response.code == 401 || response.code == 403) {
                            if (apiKey.isEmpty()) {
                                "Wyzie API key required: add WYZIE_API_KEY in AI Studio Secrets panel."
                            } else {
                                "Wyzie API authentication error (HTTP ${response.code})."
                            }
                        } else {
                            "Wyzie API returned HTTP ${response.code}"
                        }
                        return@use
                    }
                    if (bodyStr.isNotBlank()) {
                        val parsed = parseWyzieJsonArray(bodyStr, fallbackTitle)
                        if (parsed.isNotEmpty()) {
                            return parsed to null
                        }
                    }
                }
            } catch (e: Exception) {
                lastErrorMsg = "Network error reaching Wyzie Subs: ${e.localizedMessage ?: "Check connection"}"
            }
        }

        return emptyList<WyzieSubtitleOption>() to lastErrorMsg
    }

    internal fun parseWyzieJsonArray(
        json: String,
        fallbackTitle: String
    ): List<WyzieSubtitleOption> {
        val list = mutableListOf<WyzieSubtitleOption>()
        try {
            val buffer = Buffer().writeUtf8(json.trim())
            val reader = JsonReader.of(buffer)
            if (reader.peek() == JsonReader.Token.BEGIN_ARRAY) {
                reader.beginArray()
                var index = 1
                while (reader.hasNext()) {
                    if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
                        var id = ""
                        var url = ""
                        var lang = ""
                        var display = ""
                        var media = ""
                        var format = "srt"
                        var source = "Wyzie"
                        var encoding = "UTF-8"
                        var isHi = false
                        var season: Int? = null
                        var episode: Int? = null

                        reader.beginObject()
                        while (reader.hasNext()) {
                            when (reader.nextName()) {
                                "id" -> id = readSafeString(reader)
                                "url" -> url = readSafeString(reader)
                                "language", "lang" -> lang = readSafeString(reader).lowercase(Locale.ROOT)
                                "display", "languageName" -> display = readSafeString(reader)
                                "media", "release", "fileName", "title" -> {
                                    val v = readSafeString(reader)
                                    if (v.isNotBlank() && media.isBlank()) media = v
                                }
                                "format" -> format = readSafeString(reader).ifBlank { "srt" }
                                "source", "origin" -> source = readSafeString(reader).ifBlank { "Wyzie" }
                                "encoding" -> encoding = readSafeString(reader).ifBlank { "UTF-8" }
                                "isHearingImpaired", "hearingImpaired", "hi" -> isHi = readSafeBoolean(reader)
                                "season" -> season = readSafeIntOrNull(reader)
                                "episode" -> episode = readSafeIntOrNull(reader)
                                else -> reader.skipValue()
                            }
                        }
                        reader.endObject()

                        if (url.isNotBlank()) {
                            val normLang = lang.ifBlank { "en" }
                            val cleanDisplay = display.ifBlank { languageCodeToDisplayName(normLang) }
                            val flag = languageCodeToFlag(normLang)
                            val prettySource = source.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                            }
                            list.add(
                                WyzieSubtitleOption(
                                    id = id.ifBlank { "wyzie_$index" },
                                    url = url,
                                    languageCode = normLang,
                                    languageDisplay = cleanDisplay,
                                    flagEmoji = flag,
                                    mediaName = media.ifBlank { fallbackTitle },
                                    format = format.uppercase(Locale.ROOT),
                                    source = prettySource,
                                    encoding = encoding.uppercase(Locale.ROOT),
                                    isHearingImpaired = isHi,
                                    season = season,
                                    episode = episode
                                )
                            )
                            index++
                        }
                    } else {
                        reader.skipValue()
                    }
                }
                reader.endArray()
            }
        } catch (e: Exception) {
            Log.w("WyzieSubtitleService", "Error parsing Wyzie JSON: ${e.message}")
        }
        return list
    }

    private fun groupSubtitlesByLanguage(options: List<WyzieSubtitleOption>): List<WyzieLanguageGroup> {
        if (options.isEmpty()) return emptyList()
        val priorityOrder = listOf(
            "ar", "en", "fr", "es", "tr", "de", "it", "pt", "ru", "nl", "hi", "ur", "fa", "ku", "ja", "ko", "zh"
        )
        return options
            .groupBy { it.languageCode.lowercase(Locale.ROOT) }
            .map { (code, items) ->
                val first = items.first()
                WyzieLanguageGroup(
                    languageCode = code,
                    languageDisplay = first.languageDisplay,
                    flagEmoji = first.flagEmoji,
                    options = items
                )
            }
            .sortedWith(
                compareBy<WyzieLanguageGroup> { group ->
                    val idx = priorityOrder.indexOf(group.languageCode)
                    if (idx >= 0) idx else 100
                }.thenBy { it.languageDisplay }
            )
    }

    private fun resolveMediaQuery(
        stream: StreamEntity,
        playlist: PlaylistEntity?,
        customSearchQuery: String?,
        overrideSeason: Int?,
        overrideEpisode: Int?
    ): ResolvedMediaQuery {
        val isSeries = stream.streamType.equals("series", ignoreCase = true)

        // Parse encoded seriesId:season:episode from stream.epgChannelId if available
        val encodedParts = stream.epgChannelId.split(":")
        val parentSeriesId = if (isSeries && encodedParts.size >= 3) {
            encodedParts[0].toLongOrNull() ?: stream.streamId
        } else {
            stream.streamId
        }
        val parsedSeasonFromChannelId = if (isSeries && encodedParts.size >= 3) encodedParts[1].toIntOrNull() else null
        val parsedEpisodeFromChannelId = if (isSeries && encodedParts.size >= 3) encodedParts[2].toIntOrNull() else null

        // Also check S01E02 pattern in stream.name
        val seRegex = Regex("""(?i)\bS(\d{1,2})\s*E(\d{1,3})\b""")
        val seMatch = seRegex.find(stream.name)
        val seasonFromName = seMatch?.groupValues?.getOrNull(1)?.toIntOrNull()
        val episodeFromName = seMatch?.groupValues?.getOrNull(2)?.toIntOrNull()

        val finalSeason = overrideSeason ?: parsedSeasonFromChannelId ?: seasonFromName ?: if (isSeries) 1 else null
        val finalEpisode = overrideEpisode ?: parsedEpisodeFromChannelId ?: episodeFromName ?: if (isSeries) 1 else null

        val rawInput = customSearchQuery?.trim()?.takeIf { it.isNotEmpty() } ?: stream.name
        // If user entered an explicit IMDb ID (tt...) or numeric TMDb ID
        if (rawInput.matches(Regex("""^tt\d{5,10}$""", RegexOption.IGNORE_CASE))) {
            return ResolvedMediaQuery(
                cleanTitle = cleanMediaTitle(stream.name).first,
                imdbId = rawInput.lowercase(Locale.ROOT),
                isSeries = isSeries,
                season = finalSeason,
                episode = finalEpisode
            )
        }
        if (customSearchQuery != null && rawInput.matches(Regex("""^\d{2,9}$"""))) {
            return ResolvedMediaQuery(
                cleanTitle = cleanMediaTitle(stream.name).first,
                tmdbId = rawInput,
                isSeries = isSeries,
                season = finalSeason,
                episode = finalEpisode
            )
        }

        val (cleanTitle, extractedYear) = cleanMediaTitle(rawInput)

        // 1. If no custom query override, check Xtream get_vod_info / get_series_info first
        var xtreamImdbId: String? = null
        var xtreamTmdbId: String? = null
        if (customSearchQuery.isNullOrBlank() && playlist != null) {
            val ids = fetchXtreamMediaIds(
                playlist = playlist,
                isSeries = isSeries,
                targetId = if (isSeries) parentSeriesId else stream.streamId
            )
            xtreamImdbId = ids.first
            xtreamTmdbId = ids.second
        }

        // 2. Resolve IMDb ID via public IMDb instant suggestion endpoint if still needed
        val resolvedImdbId = xtreamImdbId ?: queryImdbSuggestionId(
            cleanTitle = cleanTitle,
            expectedYear = extractedYear,
            isSeries = isSeries
        )

        return ResolvedMediaQuery(
            cleanTitle = cleanTitle.ifBlank { stream.name },
            year = extractedYear,
            imdbId = resolvedImdbId,
            tmdbId = xtreamTmdbId,
            isSeries = isSeries,
            season = finalSeason,
            episode = finalEpisode
        )
    }

    /**
     * Queries Xtream `get_vod_info` or `get_series_info` to extract `imdb_id` or `tmdb_id` if present.
     */
    private fun fetchXtreamMediaIds(
        playlist: PlaylistEntity,
        isSeries: Boolean,
        targetId: Long
    ): Pair<String?, String?> {
        val action = if (isSeries) "get_series_info" else "get_vod_info"
        val idParam = if (isSeries) "series_id" else "vod_id"
        val url = XtreamClient.buildPlayerApiUrl(
            serverUrl = playlist.serverUrl,
            username = playlist.username,
            password = playlist.password,
            queryParams = mapOf(
                "action" to action,
                idParam to targetId.toString()
            )
        ) ?: return null to null

        var imdbId: String? = null
        var tmdbId: String? = null
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful || response.body == null) return null to null
                val reader = JsonReader.of(response.body!!.source())
                if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
                    reader.beginObject()
                    while (reader.hasNext()) {
                        when (reader.nextName()) {
                            "info", "movie_data" -> {
                                if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
                                    reader.beginObject()
                                    while (reader.hasNext()) {
                                        when (reader.nextName()) {
                                            "tmdb_id", "tmdb" -> {
                                                val v = readSafeString(reader).trim()
                                                if (v.isNotEmpty() && v != "0" && v != "null") {
                                                    if (v.startsWith("tt", ignoreCase = true)) imdbId = v else tmdbId = v
                                                }
                                            }
                                            "imdb_id", "imdb" -> {
                                                val v = readSafeString(reader).trim()
                                                if (v.startsWith("tt", ignoreCase = true)) imdbId = v
                                            }
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
                }
            }
        } catch (_: Exception) {}
        return imdbId to tmdbId
    }

    /**
     * Uses IMDb's public, zero-key instant suggestion JSON endpoint to resolve a clean
     * Movie or TV Series title to its canonical `tt...` IMDb ID.
     */
    private fun queryImdbSuggestionId(
        cleanTitle: String,
        expectedYear: Int?,
        isSeries: Boolean
    ): String? {
        val normalized = cleanTitle.trim()
        if (normalized.isEmpty()) return null
        val firstAlnum = normalized.firstOrNull { it.isLetterOrDigit() }?.lowercaseChar() ?: 'x'
        val encoded = try {
            URLEncoder.encode(normalized, "UTF-8").replace("+", "%20")
        } catch (_: Exception) {
            return null
        }

        val url = "https://v3.sg.media-imdb.com/suggestion/$firstAlnum/$encoded.json"
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful || response.body == null) return null
                val reader = JsonReader.of(response.body!!.source())
                if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) return null

                data class Candidate(
                    val id: String,
                    val title: String,
                    val year: Int?,
                    val qid: String
                )

                val candidates = mutableListOf<Candidate>()
                reader.beginObject()
                while (reader.hasNext()) {
                    if (reader.nextName() == "d" && reader.peek() == JsonReader.Token.BEGIN_ARRAY) {
                        reader.beginArray()
                        while (reader.hasNext()) {
                            if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
                                var id = ""
                                var label = ""
                                var year: Int? = null
                                var qid = ""
                                reader.beginObject()
                                while (reader.hasNext()) {
                                    when (reader.nextName()) {
                                        "id" -> id = readSafeString(reader)
                                        "l" -> label = readSafeString(reader)
                                        "y" -> year = readSafeIntOrNull(reader)
                                        "qid", "q" -> qid = readSafeString(reader).lowercase(Locale.ROOT)
                                        else -> reader.skipValue()
                                    }
                                }
                                reader.endObject()
                                if (id.startsWith("tt", ignoreCase = true)) {
                                    candidates.add(Candidate(id, label, year, qid))
                                }
                            } else {
                                reader.skipValue()
                            }
                        }
                        reader.endArray()
                    } else {
                        reader.skipValue()
                    }
                }
                reader.endObject()

                if (candidates.isEmpty()) return null

                // Score candidates by type (movie vs tvSeries), year match, and title similarity
                val best = candidates.maxByOrNull { c ->
                    var score = 0
                    if (isSeries && (c.qid.contains("series") || c.qid.contains("tv"))) score += 40
                    if (!isSeries && (c.qid.contains("movie") || c.qid.contains("feature"))) score += 40
                    if (expectedYear != null && c.year == expectedYear) score += 50
                    if (c.title.equals(normalized, ignoreCase = true)) score += 60
                    else if (c.title.contains(normalized, ignoreCase = true)) score += 25
                    score
                }
                return best?.id
            }
        } catch (_: Exception) {
            return null
        }
    }

    /**
     * Cleans IPTV VOD / Series titles by stripping provider prefixes ("AR|", "EN -", "[4K]"),
     * episode suffixes ("• S1E1 - ..."), and extracting the 4-digit release year if present.
     */
    internal fun cleanMediaTitle(rawName: String): Pair<String, Int?> {
        var working = rawName.trim()

        // Strip episode suffix after "•" or " - S01E01"
        working = working.substringBefore("•").trim()
        working = working.replace(Regex("""(?i)\bS\d{1,2}\s*E\d{1,3}\b.*$"""), "").trim()
        working = working.replace(Regex("""(?i)\bSeason\s*\d+.*$"""), "").trim()

        // Strip leading IPTV country/category tags like "AR|", "EN - ", "[NF]", "|VIP|"
        working = working.replace(Regex("""^(?:\[[^\]]+\]\s*|\([^\)]+\)\s*|[A-Z0-9]{2,6}\s*[\|:\-]\s*)+"""), "").trim()

        // Extract 4-digit year (1950..2030)
        val yearRegex = Regex("""[\(\[\-\s](19\d{2}|20[0-3]\d)[\)\]\s]*$""")
        val yearMatch = yearRegex.find(working)
        val year = yearMatch?.groupValues?.getOrNull(1)?.toIntOrNull()
        if (yearMatch != null) {
            working = working.removeRange(yearMatch.range).trim()
        }

        // Remove quality tags like 4K, UHD, FHD, 1080p, BluRay, WEB-DL
        working = working.replace(
            Regex("""(?i)\b(4K|UHD|FHD|HD|1080p|720p|2160p|BluRay|WEB-DL|WEBRip|x264|x265|HEVC|MULTI|VOSTFR|SUB|DUB)\b"""),
            ""
        )
        working = working.replace(Regex("""[\[\]\(\)\|]"""), " ")
        working = working.replace(Regex("""\s{2,}"""), " ").trim(' ', '-', ':', '.')

        return working.ifBlank { rawName.trim() } to year
    }

    /**
     * Parses SRT and WebVTT subtitle files into sorted [SubtitleCue] items.
     */
    internal fun parseSubtitleText(rawText: String): List<SubtitleCue> {
        val normalized = rawText.replace("\r\n", "\n").replace('\r', '\n')
        val blocks = normalized.split(Regex("""\n\s*\n"""))
        val cues = mutableListOf<SubtitleCue>()

        val timestampArrowRegex = Regex(
            """(?:(\d{1,2}):)?(\d{2}):(\d{2})[,\.](\d{3})\s*-->\s*(?:(\d{1,2}):)?(\d{2}):(\d{2})[,\.](\d{3})"""
        )

        for (block in blocks) {
            val lines = block.lines().map { it.trim() }.filter { it.isNotEmpty() }
            if (lines.isEmpty()) continue
            if (lines[0].startsWith("WEBVTT", ignoreCase = true) || lines[0].startsWith("NOTE", ignoreCase = true)) {
                continue
            }

            var timeLineIdx = -1
            var matchResult: MatchResult? = null
            for (i in lines.indices) {
                val m = timestampArrowRegex.find(lines[i])
                if (m != null) {
                    timeLineIdx = i
                    matchResult = m
                    break
                }
            }

            if (timeLineIdx >= 0 && matchResult != null && timeLineIdx + 1 < lines.size) {
                val startMs = parseTimestampPartsToMs(
                    hours = matchResult.groupValues[1],
                    minutes = matchResult.groupValues[2],
                    seconds = matchResult.groupValues[3],
                    millis = matchResult.groupValues[4]
                )
                val endMs = parseTimestampPartsToMs(
                    hours = matchResult.groupValues[5],
                    minutes = matchResult.groupValues[6],
                    seconds = matchResult.groupValues[7],
                    millis = matchResult.groupValues[8]
                )

                val textLines = lines.subList(timeLineIdx + 1, lines.size)
                    .map { line ->
                        line.replace(Regex("""<[^>]*>"""), "")
                            .replace(Regex("""\{\\[^}]*\}"""), "")
                            .trim()
                    }
                    .filter { it.isNotEmpty() }

                if (textLines.isNotEmpty() && endMs > startMs) {
                    cues.add(
                        SubtitleCue(
                            startTimeMs = startMs,
                            endTimeMs = endMs,
                            text = textLines.joinToString("\n")
                        )
                    )
                }
            }
        }
        return cues.sortedBy { it.startTimeMs }
    }

    private fun parseTimestampPartsToMs(
        hours: String,
        minutes: String,
        seconds: String,
        millis: String
    ): Long {
        val h = hours.toLongOrNull() ?: 0L
        val m = minutes.toLongOrNull() ?: 0L
        val s = seconds.toLongOrNull() ?: 0L
        val ms = millis.padEnd(3, '0').take(3).toLongOrNull() ?: 0L
        return ((h * 3600L) + (m * 60L) + s) * 1000L + ms
    }

    private fun languageCodeToDisplayName(code: String): String {
        return when (code.lowercase(Locale.ROOT)) {
            "ar" -> "Arabic (العربية)"
            "en" -> "English"
            "fr" -> "French (Français)"
            "es" -> "Spanish (Español)"
            "tr" -> "Turkish (Türkçe)"
            "de" -> "German (Deutsch)"
            "it" -> "Italian (Italiano)"
            "pt", "pb" -> "Portuguese (Português)"
            "ru" -> "Russian (Русский)"
            "nl" -> "Dutch (Nederlands)"
            "hi" -> "Hindi"
            "ur" -> "Urdu"
            "fa" -> "Persian (فارسی)"
            "ku" -> "Kurdish"
            "ja" -> "Japanese"
            "ko" -> "Korean"
            "zh", "ze", "zt" -> "Chinese"
            "pl" -> "Polish"
            "ro" -> "Romanian"
            "el" -> "Greek"
            "sv" -> "Swedish"
            "id" -> "Indonesian"
            else -> Locale(code).displayLanguage.ifBlank { code.uppercase(Locale.ROOT) }
        }
    }

    private fun languageCodeToFlag(code: String): String {
        return when (code.lowercase(Locale.ROOT)) {
            "ar" -> "🇸🇦"
            "en" -> "🇬🇧"
            "fr" -> "🇫🇷"
            "es" -> "🇪🇸"
            "tr" -> "🇹🇷"
            "de" -> "🇩🇪"
            "it" -> "🇮🇹"
            "pt", "pb" -> "🇵🇹"
            "ru" -> "🇷🇺"
            "nl" -> "🇳🇱"
            "hi" -> "🇮🇳"
            "ur" -> "🇵🇰"
            "fa" -> "🇮🇷"
            "ja" -> "🇯🇵"
            "ko" -> "🇰🇷"
            "zh", "ze", "zt" -> "🇨🇳"
            "pl" -> "🇵🇱"
            "ro" -> "🇷🇴"
            "el" -> "🇬🇷"
            "sv" -> "🇸🇪"
            "id" -> "🇮🇩"
            else -> "🌐"
        }
    }

    private fun readSafeString(reader: JsonReader): String {
        return try {
            when (reader.peek()) {
                JsonReader.Token.STRING -> reader.nextString()
                JsonReader.Token.NUMBER -> reader.nextString()
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

    private fun readSafeBoolean(reader: JsonReader): Boolean {
        return try {
            when (reader.peek()) {
                JsonReader.Token.BOOLEAN -> reader.nextBoolean()
                JsonReader.Token.STRING -> reader.nextString().equals("true", ignoreCase = true)
                JsonReader.Token.NUMBER -> reader.nextInt() != 0
                else -> {
                    reader.skipValue()
                    false
                }
            }
        } catch (_: Exception) {
            try { reader.skipValue() } catch (_: Exception) {}
            false
        }
    }

    private fun readSafeIntOrNull(reader: JsonReader): Int? {
        return try {
            when (reader.peek()) {
                JsonReader.Token.NUMBER -> reader.nextInt()
                JsonReader.Token.STRING -> reader.nextString().toIntOrNull()
                else -> {
                    reader.skipValue()
                    null
                }
            }
        } catch (_: Exception) {
            try { reader.skipValue() } catch (_: Exception) {}
            null
        }
    }

    companion object {
        private const val USER_AGENT = "SetPlay-IPTV/3.0 (Android; WyzieSubsClient)"
    }
}
