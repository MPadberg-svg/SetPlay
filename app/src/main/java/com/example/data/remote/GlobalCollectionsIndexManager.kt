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

    private data class SeedMovie(
        val tmdbId: String,
        val title: String,
        val year: Int,
        val order: Int,
        val aliases: List<String> = emptyList()
    )

    private val BUILTIN_SEED_SNAPSHOT: CollectionsSnapshot by lazy { buildBuiltinSeedSnapshot() }

    /**
     * Pure O(1) lookup returning the official TMDb collection match for a movie, or `null` if absent.
     *
     * Checks the live downloaded/cached cloud [snapshot] first, then falls back to the built-in
     * offline seed of the top 25 iconic global movie franchises when offline or before the first
     * cloud fetch completes.
     *
     * Within each tier, checks exact numeric `tmdbId` first, then NFD-normalized `cleanTitle`
     * with exact `releaseYear`, `releaseYear - 1`, and `releaseYear + 1`. NEVER matches year-0
     * titles ambiguously so standalone remakes or undated films are never falsely assigned.
     */
    fun lookupCollection(
        tmdbId: String,
        cleanTitle: String,
        releaseYear: Int
    ): CollectionMatch? {
        val cleanTmdb = tmdbId.trim()
        val trimmedTitle = cleanTitle.trim()
        val rawLower = if (releaseYear > 0 && trimmedTitle.isNotEmpty()) {
            trimmedTitle
                .lowercase(Locale.US)
                .replace(MULTI_SPACE_REGEX, " ")
                .trim()
        } else {
            ""
        }
        val alnumLower = if (releaseYear > 0 && trimmedTitle.isNotEmpty()) {
            normalizeTitleKey(trimmedTitle)
        } else {
            ""
        }

        // 1. Prefer live downloaded/cached cloud index snapshot
        val current = snapshot
        if (current.collections.isNotEmpty()) {
            lookupInSnapshot(current, cleanTmdb, rawLower, alnumLower, releaseYear)?.let { return it }
        }

        // 2. Fallback to offline-ready built-in seed of the top 25 iconic franchises
        return lookupInSnapshot(BUILTIN_SEED_SNAPSHOT, cleanTmdb, rawLower, alnumLower, releaseYear)
    }

    private fun lookupInSnapshot(
        source: CollectionsSnapshot,
        cleanTmdb: String,
        rawLower: String,
        alnumLower: String,
        releaseYear: Int
    ): CollectionMatch? {
        if (source.collections.isEmpty()) return null

        fun resolve(ref: CollectionRef?): CollectionMatch? {
            if (ref == null) return null
            val meta = source.collections[ref.collectionId] ?: return null
            if (meta.name.isBlank()) return null
            return CollectionMatch(
                collectionId = ref.collectionId,
                collectionName = meta.name,
                posterUrl = meta.posterUrl,
                releaseOrder = ref.releaseOrder
            )
        }

        // 1. Exact numeric TMDb ID lookup
        if (cleanTmdb.isNotEmpty() && cleanTmdb != "0") {
            val match = resolve(source.movieByTmdb[cleanTmdb])
            if (match != null) return match
        }

        // 2. Fallback to NFD-normalized cleanTitle + year (exact, year - 1, year + 1)
        if (releaseYear <= 0 || source.byTitleYear.isEmpty()) return null
        if (alnumLower.isEmpty() && rawLower.isEmpty()) return null

        fun lookupForYear(year: Int): CollectionMatch? {
            if (alnumLower.isNotEmpty()) {
                val alnumMatch = resolve(source.byTitleYear["${alnumLower}_$year"])
                if (alnumMatch != null) return alnumMatch
            }
            if (rawLower.isNotEmpty() && rawLower != alnumLower) {
                val rawMatch = resolve(source.byTitleYear["${rawLower}_$year"])
                if (rawMatch != null) return rawMatch
            }
            return null
        }

        lookupForYear(releaseYear)?.let { return it }
        lookupForYear(releaseYear - 1)?.let { return it }
        lookupForYear(releaseYear + 1)?.let { return it }

        return null
    }

    private fun normalizeTitleKey(rawTitle: String): String {
        return Normalizer.normalize(rawTitle, Normalizer.Form.NFD)
            .replace(COMBINING_MARKS_REGEX, "")
            .lowercase(Locale.US)
            .replace(NON_ALNUM_REGEX, " ")
            .replace(MULTI_SPACE_REGEX, " ")
            .trim()
    }

    private fun buildBuiltinSeedSnapshot(): CollectionsSnapshot {
        val collectionsMap = HashMap<String, CollectionMeta>(32)
        val movieMap = HashMap<String, CollectionRef>(256)
        val titleYearMap = HashMap<String, CollectionRef>(320)

        fun addFranchise(
            collectionId: String,
            name: String,
            movies: List<SeedMovie>
        ) {
            collectionsMap[collectionId] = CollectionMeta(name = name, posterUrl = "")
            for (movie in movies) {
                val ref = CollectionRef(collectionId = collectionId, releaseOrder = movie.order)
                if (movie.tmdbId.isNotBlank()) {
                    movieMap[movie.tmdbId] = ref
                }
                if (movie.year > 0) {
                    val primaryKey = normalizeTitleKey(movie.title)
                    if (primaryKey.isNotEmpty()) {
                        titleYearMap["${primaryKey}_${movie.year}"] = ref
                    }
                    for (alias in movie.aliases) {
                        val aliasKey = normalizeTitleKey(alias)
                        if (aliasKey.isNotEmpty()) {
                            titleYearMap["${aliasKey}_${movie.year}"] = ref
                        }
                    }
                }
            }
        }

        // 1. James Bond Collection (645)
        addFranchise(
            collectionId = "645",
            name = "James Bond Collection",
            movies = listOf(
                SeedMovie("646", "Dr. No", 1962, 1),
                SeedMovie("657", "From Russia with Love", 1963, 2),
                SeedMovie("658", "Goldfinger", 1964, 3),
                SeedMovie("660", "Thunderball", 1965, 4),
                SeedMovie("667", "You Only Live Twice", 1967, 5),
                SeedMovie("668", "On Her Majesty's Secret Service", 1969, 6),
                SeedMovie("681", "Diamonds Are Forever", 1971, 7),
                SeedMovie("253", "Live and Let Die", 1973, 8),
                SeedMovie("682", "The Man with the Golden Gun", 1974, 9),
                SeedMovie("691", "The Spy Who Loved Me", 1977, 10),
                SeedMovie("698", "Moonraker", 1979, 11),
                SeedMovie("699", "For Your Eyes Only", 1981, 12),
                SeedMovie("700", "Octopussy", 1983, 13),
                SeedMovie("707", "A View to a Kill", 1985, 14),
                SeedMovie("708", "The Living Daylights", 1987, 15),
                SeedMovie("709", "Licence to Kill", 1989, 16, listOf("License to Kill")),
                SeedMovie("710", "GoldenEye", 1995, 17),
                SeedMovie("714", "Tomorrow Never Dies", 1997, 18),
                SeedMovie("36643", "The World Is Not Enough", 1999, 19),
                SeedMovie("36669", "Die Another Day", 2002, 20),
                SeedMovie("36557", "Casino Royale", 2006, 21),
                SeedMovie("10764", "Quantum of Solace", 2008, 22),
                SeedMovie("37724", "Skyfall", 2012, 23),
                SeedMovie("206647", "Spectre", 2015, 24),
                SeedMovie("370172", "No Time to Die", 2021, 25)
            )
        )

        // 2. Mission: Impossible Collection (87359)
        addFranchise(
            collectionId = "87359",
            name = "Mission: Impossible Collection",
            movies = listOf(
                SeedMovie("954", "Mission: Impossible", 1996, 1),
                SeedMovie("955", "Mission: Impossible II", 2000, 2, listOf("Mission: Impossible 2")),
                SeedMovie("956", "Mission: Impossible III", 2006, 3, listOf("Mission: Impossible 3")),
                SeedMovie("56292", "Mission: Impossible - Ghost Protocol", 2011, 4),
                SeedMovie("177677", "Mission: Impossible - Rogue Nation", 2015, 5),
                SeedMovie("353081", "Mission: Impossible - Fallout", 2018, 6),
                SeedMovie("575264", "Mission: Impossible - Dead Reckoning Part One", 2023, 7, listOf("Mission: Impossible - Dead Reckoning")),
                SeedMovie("575265", "Mission: Impossible - The Final Reckoning", 2025, 8)
            )
        )

        // 3. The Fast and the Furious Collection (9485)
        addFranchise(
            collectionId = "9485",
            name = "The Fast and the Furious Collection",
            movies = listOf(
                SeedMovie("9799", "The Fast and the Furious", 2001, 1),
                SeedMovie("584", "2 Fast 2 Furious", 2003, 2),
                SeedMovie("9615", "The Fast and the Furious: Tokyo Drift", 2006, 3),
                SeedMovie("13804", "Fast & Furious", 2009, 4, listOf("Fast and Furious")),
                SeedMovie("51497", "Fast Five", 2011, 5, listOf("Fast & Furious 5")),
                SeedMovie("82992", "Fast & Furious 6", 2013, 6, listOf("Fast and Furious 6")),
                SeedMovie("168259", "Furious 7", 2015, 7, listOf("Fast & Furious 7")),
                SeedMovie("337339", "The Fate of the Furious", 2017, 8, listOf("Fast & Furious 8")),
                SeedMovie("385128", "F9", 2021, 9, listOf("Fast & Furious 9", "F9: The Fast Saga")),
                SeedMovie("385687", "Fast X", 2023, 10, listOf("Fast & Furious 10"))
            )
        )

        // 4. John Wick Collection (404609)
        addFranchise(
            collectionId = "404609",
            name = "John Wick Collection",
            movies = listOf(
                SeedMovie("245891", "John Wick", 2014, 1, listOf("John Wick 1")),
                SeedMovie("324552", "John Wick: Chapter 2", 2017, 2, listOf("John Wick 2")),
                SeedMovie("458156", "John Wick: Chapter 3 - Parabellum", 2019, 3, listOf("John Wick 3", "John Wick: Parabellum")),
                SeedMovie("603692", "John Wick: Chapter 4", 2023, 4, listOf("John Wick 4"))
            )
        )

        // 5. Pirates of the Caribbean Collection (295)
        addFranchise(
            collectionId = "295",
            name = "Pirates of the Caribbean Collection",
            movies = listOf(
                SeedMovie("22", "Pirates of the Caribbean: The Curse of the Black Pearl", 2003, 1),
                SeedMovie("58", "Pirates of the Caribbean: Dead Man's Chest", 2006, 2),
                SeedMovie("285", "Pirates of the Caribbean: At World's End", 2007, 3),
                SeedMovie("1865", "Pirates of the Caribbean: On Stranger Tides", 2011, 4),
                SeedMovie("166426", "Pirates of the Caribbean: Dead Men Tell No Tales", 2017, 5, listOf("Pirates of the Caribbean: Salazar's Revenge"))
            )
        )

        // 6. Harry Potter Collection (1241)
        addFranchise(
            collectionId = "1241",
            name = "Harry Potter Collection",
            movies = listOf(
                SeedMovie("671", "Harry Potter and the Philosopher's Stone", 2001, 1, listOf("Harry Potter and the Sorcerer's Stone")),
                SeedMovie("672", "Harry Potter and the Chamber of Secrets", 2002, 2),
                SeedMovie("673", "Harry Potter and the Prisoner of Azkaban", 2004, 3),
                SeedMovie("674", "Harry Potter and the Goblet of Fire", 2005, 4),
                SeedMovie("675", "Harry Potter and the Order of the Phoenix", 2007, 5),
                SeedMovie("767", "Harry Potter and the Half-Blood Prince", 2009, 6),
                SeedMovie("12444", "Harry Potter and the Deathly Hallows: Part 1", 2010, 7),
                SeedMovie("12445", "Harry Potter and the Deathly Hallows: Part 2", 2011, 8)
            )
        )

        // 7. Star Wars Collection (10)
        addFranchise(
            collectionId = "10",
            name = "Star Wars Collection",
            movies = listOf(
                SeedMovie("11", "Star Wars", 1977, 1, listOf("Star Wars: Episode IV - A New Hope", "Star Wars: A New Hope")),
                SeedMovie("1891", "The Empire Strikes Back", 1980, 2, listOf("Star Wars: Episode V - The Empire Strikes Back")),
                SeedMovie("1892", "Return of the Jedi", 1983, 3, listOf("Star Wars: Episode VI - Return of the Jedi")),
                SeedMovie("1893", "Star Wars: Episode I - The Phantom Menace", 1999, 4, listOf("The Phantom Menace")),
                SeedMovie("1894", "Star Wars: Episode II - Attack of the Clones", 2002, 5, listOf("Attack of the Clones")),
                SeedMovie("1895", "Star Wars: Episode III - Revenge of the Sith", 2005, 6, listOf("Revenge of the Sith")),
                SeedMovie("140607", "Star Wars: The Force Awakens", 2015, 7, listOf("Star Wars: Episode VII - The Force Awakens")),
                SeedMovie("181808", "Star Wars: The Last Jedi", 2017, 8, listOf("Star Wars: Episode VIII - The Last Jedi")),
                SeedMovie("181812", "Star Wars: The Rise of Skywalker", 2019, 9, listOf("Star Wars: Episode IX - The Rise of Skywalker"))
            )
        )

        // 8. The Lord of the Rings Collection (119)
        addFranchise(
            collectionId = "119",
            name = "The Lord of the Rings Collection",
            movies = listOf(
                SeedMovie("120", "The Lord of the Rings: The Fellowship of the Ring", 2001, 1),
                SeedMovie("121", "The Lord of the Rings: The Two Towers", 2002, 2),
                SeedMovie("122", "The Lord of the Rings: The Return of the King", 2003, 3)
            )
        )

        // 9. The Hobbit Collection (121938)
        addFranchise(
            collectionId = "121938",
            name = "The Hobbit Collection",
            movies = listOf(
                SeedMovie("49051", "The Hobbit: An Unexpected Journey", 2012, 1),
                SeedMovie("57158", "The Hobbit: The Desolation of Smaug", 2013, 2),
                SeedMovie("122917", "The Hobbit: The Battle of the Five Armies", 2014, 3)
            )
        )

        // 10. The Avengers Collection (86311)
        addFranchise(
            collectionId = "86311",
            name = "The Avengers Collection",
            movies = listOf(
                SeedMovie("24428", "The Avengers", 2012, 1, listOf("Marvel's The Avengers")),
                SeedMovie("99861", "Avengers: Age of Ultron", 2015, 2),
                SeedMovie("299536", "Avengers: Infinity War", 2018, 3),
                SeedMovie("299534", "Avengers: Endgame", 2019, 4)
            )
        )

        // 11. Spider-Man (Avengers) Collection (531241)
        addFranchise(
            collectionId = "531241",
            name = "Spider-Man (Avengers) Collection",
            movies = listOf(
                SeedMovie("315635", "Spider-Man: Homecoming", 2017, 1),
                SeedMovie("429617", "Spider-Man: Far From Home", 2019, 2),
                SeedMovie("634649", "Spider-Man: No Way Home", 2021, 3)
            )
        )

        // 12. The Dark Knight Collection (263)
        addFranchise(
            collectionId = "263",
            name = "The Dark Knight Collection",
            movies = listOf(
                SeedMovie("272", "Batman Begins", 2005, 1),
                SeedMovie("155", "The Dark Knight", 2008, 2),
                SeedMovie("49026", "The Dark Knight Rises", 2012, 3)
            )
        )

        // 13. Jurassic Park Collection (328)
        addFranchise(
            collectionId = "328",
            name = "Jurassic Park Collection",
            movies = listOf(
                SeedMovie("329", "Jurassic Park", 1993, 1),
                SeedMovie("330", "The Lost World: Jurassic Park", 1997, 2),
                SeedMovie("331", "Jurassic Park III", 2001, 3, listOf("Jurassic Park 3")),
                SeedMovie("135397", "Jurassic World", 2015, 4),
                SeedMovie("351286", "Jurassic World: Fallen Kingdom", 2018, 5),
                SeedMovie("507086", "Jurassic World Dominion", 2022, 6)
            )
        )

        // 14. Toy Story Collection (10194)
        addFranchise(
            collectionId = "10194",
            name = "Toy Story Collection",
            movies = listOf(
                SeedMovie("862", "Toy Story", 1995, 1),
                SeedMovie("863", "Toy Story 2", 1999, 2),
                SeedMovie("10193", "Toy Story 3", 2010, 3),
                SeedMovie("301528", "Toy Story 4", 2019, 4)
            )
        )

        // 15. The Matrix Collection (2344)
        addFranchise(
            collectionId = "2344",
            name = "The Matrix Collection",
            movies = listOf(
                SeedMovie("603", "The Matrix", 1999, 1),
                SeedMovie("604", "The Matrix Reloaded", 2003, 2),
                SeedMovie("605", "The Matrix Revolutions", 2003, 3),
                SeedMovie("624860", "The Matrix Resurrections", 2021, 4)
            )
        )

        // 16. The Terminator Collection (528)
        addFranchise(
            collectionId = "528",
            name = "The Terminator Collection",
            movies = listOf(
                SeedMovie("218", "The Terminator", 1984, 1),
                SeedMovie("280", "Terminator 2: Judgment Day", 1991, 2),
                SeedMovie("296", "Terminator 3: Rise of the Machines", 2003, 3),
                SeedMovie("534", "Terminator Salvation", 2009, 4),
                SeedMovie("87101", "Terminator Genisys", 2015, 5),
                SeedMovie("290859", "Terminator: Dark Fate", 2019, 6)
            )
        )

        // 17. Indiana Jones Collection (84)
        addFranchise(
            collectionId = "84",
            name = "Indiana Jones Collection",
            movies = listOf(
                SeedMovie("85", "Raiders of the Lost Ark", 1981, 1, listOf("Indiana Jones and the Raiders of the Lost Ark")),
                SeedMovie("87", "Indiana Jones and the Temple of Doom", 1984, 2),
                SeedMovie("89", "Indiana Jones and the Last Crusade", 1989, 3),
                SeedMovie("217", "Indiana Jones and the Kingdom of the Crystal Skull", 2008, 4),
                SeedMovie("335977", "Indiana Jones and the Dial of Destiny", 2023, 5)
            )
        )

        // 18. The Bourne Collection (31562)
        addFranchise(
            collectionId = "31562",
            name = "The Bourne Collection",
            movies = listOf(
                SeedMovie("2501", "The Bourne Identity", 2002, 1),
                SeedMovie("2502", "The Bourne Supremacy", 2004, 2),
                SeedMovie("2503", "The Bourne Ultimatum", 2007, 3),
                SeedMovie("49040", "The Bourne Legacy", 2012, 4),
                SeedMovie("324668", "Jason Bourne", 2016, 5)
            )
        )

        // 19. Die Hard Collection (1570)
        addFranchise(
            collectionId = "1570",
            name = "Die Hard Collection",
            movies = listOf(
                SeedMovie("562", "Die Hard", 1988, 1),
                SeedMovie("1573", "Die Hard 2", 1990, 2, listOf("Die Hard 2: Die Harder")),
                SeedMovie("1572", "Die Hard: With a Vengeance", 1995, 3, listOf("Die Hard 3")),
                SeedMovie("1571", "Live Free or Die Hard", 2007, 4, listOf("Die Hard 4.0", "Die Hard 4")),
                SeedMovie("47964", "A Good Day to Die Hard", 2013, 5, listOf("Die Hard 5"))
            )
        )

        // 20. Rocky Collection (1575)
        addFranchise(
            collectionId = "1575",
            name = "Rocky Collection",
            movies = listOf(
                SeedMovie("1366", "Rocky", 1976, 1),
                SeedMovie("1367", "Rocky II", 1979, 2, listOf("Rocky 2")),
                SeedMovie("1371", "Rocky III", 1982, 3, listOf("Rocky 3")),
                SeedMovie("1374", "Rocky IV", 1985, 4, listOf("Rocky 4")),
                SeedMovie("1375", "Rocky V", 1990, 5, listOf("Rocky 5")),
                SeedMovie("1246", "Rocky Balboa", 2006, 6, listOf("Rocky 6"))
            )
        )

        // 21. Transformers Collection (8650)
        addFranchise(
            collectionId = "8650",
            name = "Transformers Collection",
            movies = listOf(
                SeedMovie("1858", "Transformers", 2007, 1),
                SeedMovie("8373", "Transformers: Revenge of the Fallen", 2009, 2),
                SeedMovie("38356", "Transformers: Dark of the Moon", 2011, 3),
                SeedMovie("91314", "Transformers: Age of Extinction", 2014, 4),
                SeedMovie("335988", "Transformers: The Last Knight", 2017, 5)
            )
        )

        // 22. The Hunger Games Collection (131635)
        addFranchise(
            collectionId = "131635",
            name = "The Hunger Games Collection",
            movies = listOf(
                SeedMovie("70160", "The Hunger Games", 2012, 1),
                SeedMovie("101299", "The Hunger Games: Catching Fire", 2013, 2),
                SeedMovie("131631", "The Hunger Games: Mockingjay - Part 1", 2014, 3),
                SeedMovie("131634", "The Hunger Games: Mockingjay - Part 2", 2015, 4),
                SeedMovie("695721", "The Hunger Games: The Ballad of Songbirds & Snakes", 2023, 5, listOf("The Hunger Games: The Ballad of Songbirds and Snakes"))
            )
        )

        // 23. Dune Collection (726871)
        addFranchise(
            collectionId = "726871",
            name = "Dune Collection",
            movies = listOf(
                SeedMovie("438631", "Dune", 2021, 1, listOf("Dune: Part One")),
                SeedMovie("693134", "Dune: Part Two", 2024, 2, listOf("Dune 2"))
            )
        )

        // 24. Avatar Collection (87096)
        addFranchise(
            collectionId = "87096",
            name = "Avatar Collection",
            movies = listOf(
                SeedMovie("19995", "Avatar", 2009, 1),
                SeedMovie("76600", "Avatar: The Way of Water", 2022, 2, listOf("Avatar 2"))
            )
        )

        // 25. The Godfather Collection (230)
        addFranchise(
            collectionId = "230",
            name = "The Godfather Collection",
            movies = listOf(
                SeedMovie("238", "The Godfather", 1972, 1),
                SeedMovie("240", "The Godfather Part II", 1974, 2, listOf("The Godfather: Part II", "The Godfather 2")),
                SeedMovie("242", "The Godfather Part III", 1990, 3, listOf("The Godfather: Part III", "The Godfather 3"))
            )
        )

        return CollectionsSnapshot(
            collections = collectionsMap,
            movieByTmdb = movieMap,
            byTitleYear = titleYearMap
        )
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
