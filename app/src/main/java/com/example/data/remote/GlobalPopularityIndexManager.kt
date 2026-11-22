package com.example.data.remote

import android.content.Context
import android.util.JsonReader
import android.util.JsonToken
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.StringReader
import java.text.Normalizer
import java.util.Locale

/**
 * V15 Inverted Cloud Global Popularity Index client.
 *
 * Performs a 100% read-only HTTP GET (never sending user playlists, Xtream URLs, or credentials)
 * to download and ETag-cache a static JSON popularity index (~350 KB raw, ~110 KB gzipped) from
 * GitHub Pages at most once every 24 hours. Fails silently to an empty map when offline or unconfigured.
 */
object GlobalPopularityIndexManager {
    const val DEFAULT_INDEX_URL = "https://MPadberg-svg.github.io/SetPlay/popularity_index.json"

    private const val TAG = "GlobalPopularityIndex"
    private const val CACHE_FILE_NAME = "global_popularity_index.json"
    private const val META_FILE_NAME = "global_popularity_index.meta"
    private const val REFRESH_INTERVAL_MS = 24L * 60L * 60L * 1000L
    private const val PLACEHOLDER_HOST_MARKER = "MPadberg-svg"

    private val COMBINING_MARKS_REGEX = Regex("\\p{Mn}+")
    private val NON_ALNUM_REGEX = Regex("[^\\p{L}\\p{N}]+")
    private val MULTI_SPACE_REGEX = Regex("\\s+")

    private data class IndexSnapshot(
        val movieByTmdb: Map<String, Double> = emptyMap(),
        val seriesByTmdb: Map<String, Double> = emptyMap(),
        val byTitleKey: Map<String, Double> = emptyMap()
    )

    @Volatile
    private var snapshot: IndexSnapshot = IndexSnapshot()

    @Volatile
    private var diskLoaded: Boolean = false

    private val loadMutex = Mutex()

    /**
     * Loads the cached index from `context.filesDir/global_popularity_index.json` if present,
     * and refreshes from [url] at most once every 24 hours using `If-None-Match` ETag validation.
     * Always fails silently without throwing when offline, unconfigured, or receiving non-200/304.
     *
     * @return `true` ONLY when a fresh HTTP 200 payload was downloaded and parsed cleanly; `false` otherwise.
     */
    suspend fun ensureLoaded(
        context: Context,
        okHttpClient: OkHttpClient,
        url: String = DEFAULT_INDEX_URL
    ): Boolean = withContext(Dispatchers.IO) {
        loadMutex.withLock {
            val cacheFile = File(context.filesDir, CACHE_FILE_NAME)
            val metaFile = File(context.filesDir, META_FILE_NAME)

            // 1. Hydrate from on-disk cache once per process lifetime
            if (!diskLoaded && cacheFile.exists() && cacheFile.length() > 2L) {
                runCatching {
                    val cachedJson = cacheFile.readText(Charsets.UTF_8)
                    val parsed = parseIndexJson(cachedJson)
                    if (parsed != null) {
                        snapshot = parsed
                    }
                }.onFailure { err ->
                    Log.w(TAG, "Failed reading cached popularity index: ${err.message}")
                }
                diskLoaded = true
            }

            // 2. Skip network fetch when URL is blank or still has the template placeholder
            val cleanUrl = url.trim()
            if (cleanUrl.isEmpty() || cleanUrl.contains(PLACEHOLDER_HOST_MARKER, ignoreCase = true)) {
                return@withLock false
            }

            // 3. Respect 24-hour refresh throttle
            val (savedEtag, lastFetchMs) = readMeta(metaFile)
            val nowMs = System.currentTimeMillis()
            if (lastFetchMs > 0L && (nowMs - lastFetchMs) < REFRESH_INTERVAL_MS && cacheFile.exists()) {
                return@withLock false
            }

            // 4. Perform strictly read-only GET with ETag caching (OkHttp negotiates gzip transparently)
            var downloadedFresh = false
            runCatching {
                val reqBuilder = Request.Builder()
                    .url(cleanUrl)
                    .get()
                if (savedEtag.isNotBlank() && cacheFile.exists()) {
                    reqBuilder.header("If-None-Match", savedEtag)
                }

                okHttpClient.newCall(reqBuilder.build()).execute().use { response ->
                    when (response.code) {
                        304 -> {
                            val updatedEtag = response.header("ETag")?.trim().orEmpty().ifBlank { savedEtag }
                            writeMeta(metaFile, updatedEtag, nowMs)
                        }
                        200 -> {
                            val bodyStr = response.body?.string().orEmpty()
                            if (bodyStr.isNotBlank()) {
                                val parsed = parseIndexJson(bodyStr)
                                if (parsed != null) {
                                    val tempFile = File(context.filesDir, "$CACHE_FILE_NAME.tmp")
                                    tempFile.writeText(bodyStr, Charsets.UTF_8)
                                    if (!tempFile.renameTo(cacheFile)) {
                                        cacheFile.writeText(bodyStr, Charsets.UTF_8)
                                        tempFile.delete()
                                    }
                                    val newEtag = response.header("ETag")?.trim().orEmpty()
                                    writeMeta(metaFile, newEtag, nowMs)
                                    snapshot = parsed
                                    diskLoaded = true
                                    downloadedFresh = true
                                }
                            }
                        }
                        else -> {
                            // Fail silently and keep existing snapshot (or empty map)
                        }
                    }
                }
            }.onFailure {
                // Offline or transient network error — fail silently to existing/empty map
            }
            downloadedFresh
        }
    }

    /**
     * Pure O(1) lookup returning the global popularity score in `0.0..100.0`, or `0.0` if absent.
     * Checks numeric TMDb ID first, then NFD-normalized title with exact year, year - 1, year + 1,
     * and yearless key `"$prefix:$alnumLower"` when `releaseYear == 0`.
     */
    fun lookupGlobalScore(
        streamType: String,
        tmdbId: String,
        cleanTitle: String,
        releaseYear: Int
    ): Double {
        val current = snapshot
        if (current.movieByTmdb.isEmpty() && current.seriesByTmdb.isEmpty() && current.byTitleKey.isEmpty()) {
            return 0.0
        }

        val isMovie = streamType.trim().lowercase(Locale.US) in setOf("movie", "movies", "vod")
        val prefix = if (isMovie) "m" else "s"

        // 1. Exact TMDb ID lookup
        val cleanTmdb = tmdbId.trim()
        if (cleanTmdb.isNotEmpty() && cleanTmdb != "0") {
            val tmdbScore = if (isMovie) {
                current.movieByTmdb[cleanTmdb]
            } else {
                current.seriesByTmdb[cleanTmdb]
            }
            if (tmdbScore != null && tmdbScore > 0.0) {
                return tmdbScore.coerceIn(0.0, 100.0)
            }
        }

        // 2. Fallback to NFD-normalized title + year lookup
        val trimmedTitle = cleanTitle.trim()
        if (trimmedTitle.isEmpty() || current.byTitleKey.isEmpty()) {
            return 0.0
        }

        val rawLower = trimmedTitle
            .lowercase(Locale.US)
            .replace(MULTI_SPACE_REGEX, " ")
            .trim()
        val alnumLower = Normalizer.normalize(trimmedTitle, Normalizer.Form.NFD)
            .replace(COMBINING_MARKS_REGEX, "")
            .lowercase(Locale.US)
            .replace(NON_ALNUM_REGEX, " ")
            .replace(MULTI_SPACE_REGEX, " ")
            .trim()

        fun lookupForYear(year: Int): Double? {
            val rawScore = current.byTitleKey["$prefix:${rawLower}_$year"]
            if (rawScore != null && rawScore > 0.0) return rawScore.coerceIn(0.0, 100.0)
            if (alnumLower.isNotEmpty() && alnumLower != rawLower) {
                val alnumScore = current.byTitleKey["$prefix:${alnumLower}_$year"]
                if (alnumScore != null && alnumScore > 0.0) return alnumScore.coerceIn(0.0, 100.0)
            }
            return null
        }

        if (releaseYear > 0) {
            lookupForYear(releaseYear)?.let { return it }
            lookupForYear(releaseYear - 1)?.let { return it }
            lookupForYear(releaseYear + 1)?.let { return it }
        } else {
            lookupForYear(0)?.let { return it }
            if (alnumLower.isNotEmpty()) {
                val yearlessAlnum = current.byTitleKey["$prefix:$alnumLower"]
                if (yearlessAlnum != null && yearlessAlnum > 0.0) {
                    return yearlessAlnum.coerceIn(0.0, 100.0)
                }
            }
            if (rawLower.isNotEmpty() && rawLower != alnumLower) {
                val yearlessRaw = current.byTitleKey["$prefix:$rawLower"]
                if (yearlessRaw != null && yearlessRaw > 0.0) {
                    return yearlessRaw.coerceIn(0.0, 100.0)
                }
            }
        }

        return 0.0
    }

    /**
     * Deterministic test helper for Robolectric unit tests to load an index JSON payload directly.
     */
    internal fun loadFromJsonStringForTest(json: String) {
        val parsed = parseIndexJson(json)
        snapshot = parsed ?: IndexSnapshot()
        diskLoaded = true
    }

    internal fun clearForTest() {
        snapshot = IndexSnapshot()
        diskLoaded = false
    }

    private fun parseIndexJson(json: String): IndexSnapshot? {
        if (json.isBlank()) return null
        return try {
            val movieMap = HashMap<String, Double>()
            val seriesMap = HashMap<String, Double>()
            val titleMap = HashMap<String, Double>()

            JsonReader(StringReader(json)).use { reader ->
                if (reader.peek() != JsonToken.BEGIN_OBJECT) return null
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "m" -> readScoreMap(reader, movieMap, normalizeKeysLower = false)
                        "s" -> readScoreMap(reader, seriesMap, normalizeKeysLower = false)
                        "t" -> readScoreMap(reader, titleMap, normalizeKeysLower = true)
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
            }

            IndexSnapshot(
                movieByTmdb = movieMap,
                seriesByTmdb = seriesMap,
                byTitleKey = titleMap
            )
        } catch (e: Exception) {
            Log.w(TAG, "Malformed popularity index JSON: ${e.message}")
            null
        }
    }

    private fun readScoreMap(
        reader: JsonReader,
        target: MutableMap<String, Double>,
        normalizeKeysLower: Boolean
    ) {
        if (reader.peek() != JsonToken.BEGIN_OBJECT) {
            reader.skipValue()
            return
        }
        reader.beginObject()
        while (reader.hasNext()) {
            val rawKey = reader.nextName().trim()
            val token = reader.peek()
            val score: Double? = when (token) {
                JsonToken.NUMBER -> runCatching { reader.nextDouble() }.getOrNull()
                JsonToken.STRING -> runCatching { reader.nextString().trim().toDoubleOrNull() }.getOrNull()
                else -> {
                    reader.skipValue()
                    null
                }
            }
            if (rawKey.isNotEmpty() && score != null && score > 0.0) {
                val key = if (normalizeKeysLower) rawKey.lowercase(Locale.US) else rawKey
                target[key] = score.coerceIn(0.0, 100.0)
            }
        }
        reader.endObject()
    }

    private fun readMeta(metaFile: File): Pair<String, Long> {
        if (!metaFile.exists()) return "" to 0L
        return runCatching {
            val lines = metaFile.readLines(Charsets.UTF_8)
            val etag = lines.getOrNull(0)?.trim().orEmpty()
            val ts = lines.getOrNull(1)?.trim()?.toLongOrNull() ?: 0L
            etag to ts
        }.getOrDefault("" to 0L)
    }

    private fun writeMeta(metaFile: File, etag: String, timestampMs: Long) {
        runCatching {
            metaFile.writeText("$etag\n$timestampMs", Charsets.UTF_8)
        }
    }
}
