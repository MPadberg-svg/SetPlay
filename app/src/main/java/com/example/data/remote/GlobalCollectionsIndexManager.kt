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

data class CollectionMatch(
    val collectionId: String,
    val collectionName: String,
    val posterUrl: String,
    val releaseOrder: Int
)

/**
 * V16 Inverted Cloud Official TMDb Movie Collections / Franchises index client.
 *
 * Performs a 100% read-only HTTP GET (never sending user playlists, Xtream URLs, or credentials)
 * to download and ETag-cache a static JSON collections index (~80-120 KB raw, ~25 KB gzipped)
 * from GitHub Pages at most once every 24 hours. Fails silently to an empty snapshot when offline.
 */
object GlobalCollectionsIndexManager {
    const val DEFAULT_COLLECTIONS_URL = "https://MPadberg-svg.github.io/SetPlay/collections_index.json"

    private const val TAG = "GlobalCollectionsIndex"
    private const val CACHE_FILE_NAME = "global_collections_index.json"
    private const val META_FILE_NAME = "global_collections_index.meta"
    private const val REFRESH_INTERVAL_MS = 24L * 60L * 60L * 1000L
    private const val PLACEHOLDER_HOST_MARKER = "YOUR_GITHUB_USERNAME"
    private const val TMDB_POSTER_BASE = "https://image.tmdb.org/t/p/w500"

    private val COMBINING_MARKS_REGEX = Regex("\\p{Mn}+")
    private val NON_ALNUM_REGEX = Regex("[^\\p{L}\\p{N}]+")
    private val MULTI_SPACE_REGEX = Regex("\\s+")

    private data class CollectionMeta(
        val name: String,
        val posterUrl: String
    )

    private data class CollectionRef(
        val collectionId: String,
        val releaseOrder: Int
    )

    private data class CollectionsSnapshot(
        val collections: Map<String, CollectionMeta> = emptyMap(),
        val movieByTmdb: Map<String, CollectionRef> = emptyMap(),
        val byTitleYear: Map<String, CollectionRef> = emptyMap()
    )

    @Volatile
    private var snapshot: CollectionsSnapshot = CollectionsSnapshot()

    @Volatile
    private var diskLoaded: Boolean = false

    private val loadMutex = Mutex()

    /**
     * Loads the cached collections index from `context.filesDir/global_collections_index.json`
     * if present, and refreshes from [url] at most once every 24 hours using `If-None-Match` ETag
     * validation. Always fails silently without throwing when offline or unconfigured.
     *
     * @return `true` ONLY when a fresh HTTP 200 payload was downloaded and parsed cleanly.
     */
    suspend fun ensureLoaded(
        context: Context,
        okHttpClient: OkHttpClient,
        url: String = DEFAULT_COLLECTIONS_URL
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
                    Log.w(TAG, "Failed reading cached collections index: ${err.message}")
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
     * Pure O(1) lookup returning the official TMDb collection match for a movie, or `null` if absent.
     *
     * Checks exact numeric `tmdbId` first, then NFD-normalized `cleanTitle` with exact `releaseYear`,
     * `releaseYear - 1`, and `releaseYear + 1`. NEVER matches year-0 titles ambiguously so standalone
     * remakes or undated films are never falsely assigned to a franchise.
     */
    fun lookupCollection(
        tmdbId: String,
        cleanTitle: String,
        releaseYear: Int
    ): CollectionMatch? {
        val current = snapshot
        if (current.collections.isEmpty()) return null

        fun resolve(ref: CollectionRef?): CollectionMatch? {
            if (ref == null) return null
            val meta = current.collections[ref.collectionId] ?: return null
            if (meta.name.isBlank()) return null
            return CollectionMatch(
                collectionId = ref.collectionId,
                collectionName = meta.name,
                posterUrl = meta.posterUrl,
                releaseOrder = ref.releaseOrder
            )
        }

        // 1. Exact numeric TMDb ID lookup
        val cleanTmdb = tmdbId.trim()
        if (cleanTmdb.isNotEmpty() && cleanTmdb != "0") {
            val match = resolve(current.movieByTmdb[cleanTmdb])
            if (match != null) return match
        }

        // 2. Fallback to NFD-normalized cleanTitle + year (exact, year - 1, year + 1)
        // Strictly requires releaseYear > 0 to avoid false positives on standalone remakes.
        if (releaseYear <= 0 || current.byTitleYear.isEmpty()) return null
        val trimmedTitle = cleanTitle.trim()
        if (trimmedTitle.isEmpty()) return null

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

        fun lookupForYear(year: Int): CollectionMatch? {
            if (alnumLower.isNotEmpty()) {
                val alnumMatch = resolve(current.byTitleYear["${alnumLower}_$year"])
                if (alnumMatch != null) return alnumMatch
            }
            if (rawLower.isNotEmpty() && rawLower != alnumLower) {
                val rawMatch = resolve(current.byTitleYear["${rawLower}_$year"])
                if (rawMatch != null) return rawMatch
            }
            return null
        }

        lookupForYear(releaseYear)?.let { return it }
        lookupForYear(releaseYear - 1)?.let { return it }
        lookupForYear(releaseYear + 1)?.let { return it }

        return null
    }

    internal fun loadFromJsonStringForTest(json: String) {
        val parsed = parseIndexJson(json)
        snapshot = parsed ?: CollectionsSnapshot()
        diskLoaded = true
    }

    internal fun clearForTest() {
        snapshot = CollectionsSnapshot()
        diskLoaded = false
    }

    private fun parseIndexJson(json: String): CollectionsSnapshot? {
        if (json.isBlank()) return null
        return try {
            val collectionsMap = HashMap<String, CollectionMeta>()
            val movieMap = HashMap<String, CollectionRef>()
            val titleYearMap = HashMap<String, CollectionRef>()

            JsonReader(StringReader(json)).use { reader ->
                if (reader.peek() != JsonToken.BEGIN_OBJECT) return null
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "c" -> readCollectionsMetaMap(reader, collectionsMap)
                        "m" -> readRefMap(reader, movieMap, normalizeKeysLower = false)
                        "t" -> readRefMap(reader, titleYearMap, normalizeKeysLower = true)
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
            }

            CollectionsSnapshot(
                collections = collectionsMap,
                movieByTmdb = movieMap,
                byTitleYear = titleYearMap
            )
        } catch (e: Exception) {
            Log.w(TAG, "Malformed collections index JSON: ${e.message}")
            null
        }
    }

    private fun readCollectionsMetaMap(
        reader: JsonReader,
        target: MutableMap<String, CollectionMeta>
    ) {
        if (reader.peek() != JsonToken.BEGIN_OBJECT) {
            reader.skipValue()
            return
        }
        reader.beginObject()
        while (reader.hasNext()) {
            val colId = reader.nextName().trim()
            if (reader.peek() != JsonToken.BEGIN_OBJECT) {
                reader.skipValue()
                continue
            }
            var name = ""
            var rawPoster = ""
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "n" -> {
                        if (reader.peek() == JsonToken.STRING) {
                            name = reader.nextString().trim()
                        } else {
                            reader.skipValue()
                        }
                    }
                    "p" -> {
                        if (reader.peek() == JsonToken.STRING) {
                            rawPoster = reader.nextString().trim()
                        } else {
                            reader.skipValue()
                        }
                    }
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
            if (colId.isNotEmpty() && name.isNotEmpty()) {
                val posterUrl = when {
                    rawPoster.isEmpty() -> ""
                    rawPoster.startsWith("http://", ignoreCase = true) ||
                        rawPoster.startsWith("https://", ignoreCase = true) -> rawPoster
                    rawPoster.startsWith("/") -> "$TMDB_POSTER_BASE$rawPoster"
                    else -> "$TMDB_POSTER_BASE/$rawPoster"
                }
                target[colId] = CollectionMeta(name = name, posterUrl = posterUrl)
            }
        }
        reader.endObject()
    }

    private fun readRefMap(
        reader: JsonReader,
        target: MutableMap<String, CollectionRef>,
        normalizeKeysLower: Boolean
    ) {
        if (reader.peek() != JsonToken.BEGIN_OBJECT) {
            reader.skipValue()
            return
        }
        reader.beginObject()
        while (reader.hasNext()) {
            val rawKey = reader.nextName().trim()
            if (reader.peek() != JsonToken.BEGIN_ARRAY) {
                reader.skipValue()
                continue
            }
            var colId = ""
            var order = 0
            reader.beginArray()
            if (reader.hasNext()) {
                colId = when (reader.peek()) {
                    JsonToken.STRING -> reader.nextString().trim()
                    JsonToken.NUMBER -> runCatching { reader.nextLong().toString() }.getOrDefault("")
                    else -> {
                        reader.skipValue()
                        ""
                    }
                }
            }
            if (reader.hasNext()) {
                order = when (reader.peek()) {
                    JsonToken.NUMBER -> runCatching { reader.nextInt() }.getOrDefault(0)
                    JsonToken.STRING -> runCatching { reader.nextString().trim().toIntOrNull() ?: 0 }.getOrDefault(0)
                    else -> {
                        reader.skipValue()
                        0
                    }
                }
            }
            while (reader.hasNext()) {
                reader.skipValue()
            }
            reader.endArray()

            if (rawKey.isNotEmpty() && colId.isNotEmpty()) {
                val key = if (normalizeKeysLower) rawKey.lowercase(Locale.US) else rawKey
                target[key] = CollectionRef(collectionId = colId, releaseOrder = order.coerceAtLeast(0))
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
