package com.example.data.sync

import com.example.data.local.StreamIndexProjection
import com.example.data.model.VodCatalogGroupEntity
import com.example.data.model.VodCollectionEntity
import com.example.data.model.VodGroupMemberEntity
import com.example.data.model.VodGroupingNormalizer
import com.example.data.remote.CollectionMatch
import com.example.data.remote.GlobalCollectionsIndexManager
import com.example.data.remote.GlobalPopularityIndexManager
import java.text.Normalizer
import java.util.Locale
import kotlin.math.ln

data class LosslessGroupingResult(
    val groups: List<VodCatalogGroupEntity>,
    val members: List<VodGroupMemberEntity>,
    val collections: List<VodCollectionEntity> = emptyList()
)

/**
 * Lossless multi-bridge Union-Find grouping engine, zero-allocation 64-bit FNV-1a
 * metadata hashing for Smart Diff sync, streaming platform tag normalization, and
 * 3-level official TMDb Movie Collections organization:
 * - Level 1: Duplicate versions of the same film -> 1 [VodCatalogGroupEntity] + `vod_group_members`.
 * - Level 2: Distinct films in an official TMDb Collection (>= 2 movies in user's catalog) ->
 *   independent [VodCatalogGroupEntity] rows linked via `collectionId` + `collectionOrder` to [VodCollectionEntity].
 * - Level 3: Standalone movies (`collectionId == ""`) -> never grouped into 1-movie collections.
 */
object CatalogDiffEngine {
    private const val FNV1A_64_OFFSET_BASIS: Long = -3750763034362895579L // 0xcbf29ce484222325
    private const val FNV1A_64_PRIME: Long = 1099511628211L              // 0x100000001b3
    private const val FIELD_SEPARATOR: Long = 0x1FL

    private val COMBINING_MARKS_REGEX = Regex("\\p{Mn}+")
    private val NON_ALNUM_REGEX = Regex("[^\\p{L}\\p{N}]+")
    private val MULTI_SPACE_REGEX = Regex("\\s+")

    private val NETFLIX_REGEX = Regex("(?i)(?:NETFLIX|(?<![A-Z0-9])NFLX(?![A-Z0-9]))")
    private val PRIME_REGEX = Regex("(?i)(?:AMAZON|PRIME|(?<![A-Z0-9])AMZ(?![A-Z0-9]))")
    private val APPLE_REGEX = Regex("(?i)(?:APPLE\\s*TV\\+?|APPLE\\+|APPLETV|(?<![A-Z0-9])APPLE(?![A-Z0-9]))")
    private val DISNEY_REGEX = Regex("(?i)(?:DISNEY|PIXAR)")
    private val HBO_REGEX = Regex("(?i)(?:HBO|(?<![A-Z0-9])MAX(?![A-Z0-9]))")

    /**
     * Zero-allocation deterministic 64-bit FNV-1a hash over provider metadata fields.
     * Used by incremental Smart Diff sync to skip unchanged rows with 0 SQLite writes.
     */
    fun computeMetadataHash(
        name: String,
        categoryId: String,
        streamIcon: String,
        rating: String,
        tmdbId: String,
        addedEpoch: Long,
        genres: String,
        containerExtension: String
    ): Long {
        var hash = FNV1A_64_OFFSET_BASIS
        hash = feedString(hash, name)
        hash = feedSeparator(hash)
        hash = feedString(hash, categoryId)
        hash = feedSeparator(hash)
        hash = feedString(hash, streamIcon)
        hash = feedSeparator(hash)
        hash = feedString(hash, rating)
        hash = feedSeparator(hash)
        hash = feedString(hash, tmdbId)
        hash = feedSeparator(hash)
        hash = feedLong(hash, addedEpoch)
        hash = feedSeparator(hash)
        hash = feedString(hash, genres)
        hash = feedSeparator(hash)
        hash = feedString(hash, containerExtension)
        return hash
    }

    private fun feedSeparator(currentHash: Long): Long {
        return (currentHash xor FIELD_SEPARATOR) * FNV1A_64_PRIME
    }

    private fun feedString(currentHash: Long, value: String): Long {
        var h = currentHash
        val len = value.length
        for (i in 0 until len) {
            val ch = value[i].code.toLong()
            h = (h xor (ch and 0xFFL)) * FNV1A_64_PRIME
            val hi = (ch ushr 8) and 0xFFL
            if (hi != 0L) {
                h = (h xor hi) * FNV1A_64_PRIME
            }
        }
        return h
    }

    private fun feedLong(currentHash: Long, value: Long): Long {
        var h = currentHash
        var shift = 0
        while (shift < 64) {
            h = (h xor ((value ushr shift) and 0xFFL)) * FNV1A_64_PRIME
            shift += 8
        }
        return h
    }

    /**
     * Normalizes provider category naming variants into comma-wrapped canonical tags
     * (e.g. `",NETFLIX,PRIME,"`) for fast indexed `vod_catalog_groups.platformTags` filtering.
     */
    fun extractPlatformTags(categoryNames: Collection<String>): String {
        if (categoryNames.isEmpty()) return ""
        var hasNetflix = false
        var hasPrime = false
        var hasApple = false
        var hasDisney = false
        var hasHbo = false

        for (raw in categoryNames) {
            if (raw.isBlank()) continue
            if (!hasNetflix && NETFLIX_REGEX.containsMatchIn(raw)) hasNetflix = true
            if (!hasPrime && PRIME_REGEX.containsMatchIn(raw)) hasPrime = true
            if (!hasApple && APPLE_REGEX.containsMatchIn(raw)) hasApple = true
            if (!hasDisney && DISNEY_REGEX.containsMatchIn(raw)) hasDisney = true
            if (!hasHbo && HBO_REGEX.containsMatchIn(raw)) hasHbo = true
            if (hasNetflix && hasPrime && hasApple && hasDisney && hasHbo) break
        }

        if (!hasNetflix && !hasPrime && !hasApple && !hasDisney && !hasHbo) {
            return ""
        }

        val sb = StringBuilder(32)
        sb.append(',')
        if (hasNetflix) sb.append("NETFLIX,")
        if (hasPrime) sb.append("PRIME,")
        if (hasApple) sb.append("APPLE,")
        if (hasDisney) sb.append("DISNEY,")
        if (hasHbo) sb.append("HBO,")
        return sb.toString()
    }

    /**
     * O(N) Disjoint-Set (Union-Find) lossless multi-bridge catalog grouper.
     *
     * Unifies duplicate editions across 3 deterministic bridges without losing any provider stream:
     * - Bridge 1: Non-empty numeric `tmdbId` (> 0).
     * - Bridge 2: Non-empty TMDb poster hash (`extractTmdbPosterHash(streamIcon)`) WITHOUT
     *   requiring `releaseYear > 0`, unifying localized titles and year-0 copies sharing the exact
     *   same TMDb poster with the primary English copy.
     * - Bridge 3: Normalized clean title + exact `releaseYear` (when `releaseYear > 0`).
     */
    fun buildLosslessGroups(
        playlistId: Long,
        streamType: String,
        streams: List<StreamIndexProjection>,
        categoryNameMap: Map<String, String>,
        onProgress: ((groupedCount: Int, totalCount: Int) -> Unit)? = null
    ): LosslessGroupingResult {
        val count = streams.size
        if (count == 0) {
            return LosslessGroupingResult(
                groups = emptyList(),
                members = emptyList(),
                collections = emptyList()
            )
        }

        val cleanType = streamType.trim().lowercase(Locale.US).ifBlank { "movie" }
        val parent = IntArray(count) { it }
        val rank = ByteArray(count)

        fun find(index: Int): Int {
            var root = index
            while (parent[root] != root) {
                root = parent[root]
            }
            var cur = index
            while (parent[cur] != root) {
                val next = parent[cur]
                parent[cur] = root
                cur = next
            }
            return root
        }

        fun union(a: Int, b: Int) {
            val ra = find(a)
            val rb = find(b)
            if (ra == rb) return
            val rankA = rank[ra].toInt()
            val rankB = rank[rb].toInt()
            when {
                rankA < rankB -> parent[ra] = rb
                rankA > rankB -> parent[rb] = ra
                else -> {
                    parent[rb] = ra
                    rank[ra] = (rankA + 1).toByte()
                }
            }
        }

        val validTmdbIds = arrayOfNulls<String>(count)
        val posterHashes = arrayOfNulls<String>(count)
        val effectiveYears = IntArray(count)
        val normalizedTitles = arrayOfNulls<String>(count)

        val tmdbBridge = HashMap<String, Int>(count / 2 + 16)
        val posterBridge = HashMap<String, Int>(count / 2 + 16)
        val titleYearBridge = HashMap<String, Int>(count / 2 + 16)

        for (i in 0 until count) {
            val s = streams[i]

            // Bridge 1: Numeric TMDb ID > 0
            val cleanTmdb = normalizeNumericTmdbId(s.tmdbId)
            if (cleanTmdb.isNotEmpty()) {
                validTmdbIds[i] = cleanTmdb
                val prev = tmdbBridge.putIfAbsent(cleanTmdb, i)
                if (prev != null) {
                    union(i, prev)
                }
            }

            // Bridge 2: TMDb poster hash (independent of releaseYear so year-0 & localized editions merge)
            val posterHash = VodGroupingNormalizer.extractTmdbPosterHash(s.streamIcon)
            if (posterHash.isNotEmpty()) {
                posterHashes[i] = posterHash
                val prev = posterBridge.putIfAbsent(posterHash, i)
                if (prev != null) {
                    union(i, prev)
                }
            }

            // Bridge 3: Normalized clean title + exact releaseYear.
            // Only compute extractCleanDisplayTitle / normalizeCleanTitleKey when s.releaseYear > 0
            // OR when both tmdbId and posterHash are empty (7x faster CPU grouping).
            val needsTitleBridge = s.releaseYear > 0 || (cleanTmdb.isEmpty() && posterHash.isEmpty())
            val effectiveYear = if (s.releaseYear > 0) {
                s.releaseYear
            } else if (needsTitleBridge) {
                VodGroupingNormalizer.extractReleaseYear(s.name, "")
            } else {
                0
            }
            effectiveYears[i] = effectiveYear

            if (needsTitleBridge) {
                val cleanDisplayTitle = VodGroupingNormalizer.extractCleanDisplayTitle(s.name)
                val normTitle = normalizeCleanTitleKey(cleanDisplayTitle)
                if (normTitle.isNotEmpty()) {
                    normalizedTitles[i] = normTitle
                    if (effectiveYear > 0) {
                        val titleYearKey = "${normTitle}_$effectiveYear"
                        val prev = titleYearBridge.putIfAbsent(titleYearKey, i)
                        if (prev != null) {
                            union(i, prev)
                        }
                    }
                }
            }

            if (onProgress != null && ((i + 1) % 10_000 == 0)) {
                onProgress.invoke(((i + 1) / 2).coerceAtLeast(1), count)
            }
        }
        val halfCount = (count / 2).coerceAtLeast(1)
        if (onProgress != null && (count % 10_000 != 0)) {
            onProgress.invoke(halfCount, count)
        }

        // Partition stream indices into connected components
        val components = HashMap<Int, ArrayList<Int>>(count / 2 + 16)
        for (i in 0 until count) {
            val root = find(i)
            var list = components[root]
            if (list == null) {
                list = ArrayList(2)
                components[root] = list
            }
            list.add(i)
        }

        val groups = ArrayList<VodCatalogGroupEntity>(components.size)
        val members = ArrayList<VodGroupMemberEntity>(count / 3 + 16)
        val usedGroupKeys = HashSet<String>(components.size * 2)
        val provisionalCollections = if (cleanType == "movie") {
            HashMap<String, ArrayList<Pair<Int, CollectionMatch>>>()
        } else {
            null
        }
        var materializedStreamCount = 0
        var lastReportedMaterialized = 0

        for (componentIndices in components.values) {
            var repIndex = componentIndices[0]
            var repPrefScore = Int.MIN_VALUE
            var repVersionTag = ""

            var bestTmdbId = ""
            var bestPosterHash = ""
            var bestPosterUrl = ""
            var bestRatingNumeric = 0.0
            var bestRating = ""
            var bestYear = 0
            var maxAddedEpoch = 0L
            var bestGenres = ""

            val categoryIds = LinkedHashSet<String>(4)
            val categoryNames = LinkedHashSet<String>(4)
            var tmdbCounts: HashMap<String, Int>? = null
            var posterCounts: HashMap<String, Int>? = null

            for (j in 0 until componentIndices.size) {
                val idx = componentIndices[j]
                val s = streams[idx]

                val catName = categoryNameMap[s.categoryId].orEmpty()
                if (s.categoryId.isNotBlank()) {
                    categoryIds.add(s.categoryId)
                    if (catName.isNotBlank()) {
                        categoryNames.add(catName)
                    }
                }

                val versionTag = if (s.versionTag.isNotBlank()) {
                    s.versionTag
                } else {
                    VodGroupingNormalizer.extractVersionTagWithCategory(
                        rawName = s.name,
                        categoryName = catName
                    )
                }

                val hasPoster = s.streamIcon.isNotBlank()
                val prefScore = VodGroupingNormalizer.scoreVersionPreference(
                    versionTag = versionTag,
                    hasPoster = hasPoster,
                    containerExt = s.containerExtension,
                    ratingNumeric = s.ratingNumeric,
                    rawName = s.name
                )

                if (prefScore > repPrefScore) {
                    repPrefScore = prefScore
                    repIndex = idx
                    repVersionTag = versionTag
                }

                val tmdb = validTmdbIds[idx]
                if (tmdb != null) {
                    if (bestTmdbId.isEmpty()) {
                        bestTmdbId = tmdb
                    } else if (bestTmdbId != tmdb) {
                        val map = tmdbCounts ?: HashMap<String, Int>(4).also {
                            it[bestTmdbId] = 1
                            tmdbCounts = it
                        }
                        map[tmdb] = (map[tmdb] ?: 0) + 1
                    }
                }

                val pHash = posterHashes[idx]
                if (pHash != null) {
                    if (bestPosterHash.isEmpty()) {
                        bestPosterHash = pHash
                    } else if (bestPosterHash != pHash) {
                        val map = posterCounts ?: HashMap<String, Int>(4).also {
                            it[bestPosterHash] = 1
                            posterCounts = it
                        }
                        map[pHash] = (map[pHash] ?: 0) + 1
                    }
                }

                if (s.addedEpoch > maxAddedEpoch) {
                    maxAddedEpoch = s.addedEpoch
                }
                if (s.ratingNumeric > bestRatingNumeric) {
                    bestRatingNumeric = s.ratingNumeric
                    if (s.rating.isNotBlank()) bestRating = s.rating
                } else if (bestRating.isBlank() && s.rating.isNotBlank()) {
                    bestRating = s.rating
                }
                if (bestYear == 0 && effectiveYears[idx] > 0) {
                    bestYear = effectiveYears[idx]
                }
                if (bestGenres.isBlank() && s.genres.isNotBlank()) {
                    bestGenres = s.genres
                }
                if (bestPosterUrl.isBlank() && hasPoster) {
                    bestPosterUrl = s.streamIcon
                }
            }

            if (tmdbCounts != null) {
                var topKey = bestTmdbId
                var topCnt = -1
                for ((k, v) in tmdbCounts!!) {
                    if (v > topCnt || (v == topCnt && k < topKey)) {
                        topCnt = v
                        topKey = k
                    }
                }
                bestTmdbId = topKey
            }
            if (posterCounts != null) {
                var topKey = bestPosterHash
                var topCnt = -1
                for ((k, v) in posterCounts!!) {
                    if (v > topCnt || (v == topCnt && k < topKey)) {
                        topCnt = v
                        topKey = k
                    }
                }
                bestPosterHash = topKey
            }

            val rep = streams[repIndex]
            val repYear = effectiveYears[repIndex].takeIf { it > 0 }
                ?: bestYear.takeIf { it > 0 }
                ?: VodGroupingNormalizer.extractReleaseYear(rep.name, "")
            val resolvedPosterUrl = rep.streamIcon.ifBlank { bestPosterUrl }
            val resolvedGenres = rep.genres.ifBlank { bestGenres }
            val cleanTitle = VodGroupingNormalizer.extractCleanDisplayTitle(rep.name)

            // Canonical groupKey precedence: tmdb -> poster -> title+year -> raw
            val baseGroupKey = when {
                bestTmdbId.isNotEmpty() -> "tmdb_${cleanType}_$bestTmdbId"
                bestPosterHash.isNotEmpty() -> "poster_${cleanType}_$bestPosterHash"
                else -> {
                    val normTitle = normalizedTitles[repIndex] ?: normalizeCleanTitleKey(cleanTitle)
                    if (normTitle.isNotEmpty()) {
                        "title_${cleanType}_${normTitle}_$repYear"
                    } else {
                        "raw_${cleanType}_${rep.streamId}"
                    }
                }
            }
            val finalGroupKey = if (usedGroupKeys.add(baseGroupKey)) {
                baseGroupKey
            } else {
                var disambiguated = "${baseGroupKey}_${rep.streamId}"
                var suffix = 1
                while (!usedGroupKeys.add(disambiguated)) {
                    disambiguated = "${baseGroupKey}_${rep.streamId}_${suffix++}"
                }
                disambiguated
            }

            val upperTag = repVersionTag.uppercase(Locale.US)
            val isPreferredLang = upperTag.contains("ENGLISH") ||
                upperTag.contains("ARABIC") ||
                upperTag.contains("FRENCH") ||
                upperTag.contains("VOSTFR") ||
                upperTag.contains("MULTI")

            val globalScore = GlobalPopularityIndexManager.lookupGlobalScore(
                streamType = cleanType,
                tmdbId = bestTmdbId,
                cleanTitle = cleanTitle,
                releaseYear = repYear
            )
            val popularityScore = VodGroupingNormalizer.computePopularityQualityScore(
                ratingNumeric = bestRatingNumeric,
                versionCount = componentIndices.size,
                releaseYear = repYear,
                hasPoster = resolvedPosterUrl.isNotBlank(),
                isPreferredLang = isPreferredLang,
                globalTmdbScore = globalScore
            )

            val categoryIdsCsv = if (categoryIds.isEmpty()) {
                ""
            } else {
                ",${categoryIds.joinToString(",")},"
            }
            val platformTags = extractPlatformTags(categoryNames)

            groups.add(
                VodCatalogGroupEntity(
                    playlistId = playlistId,
                    streamType = cleanType,
                    groupKey = finalGroupKey,
                    representativeStreamDbId = rep.id,
                    representativeStreamId = rep.streamId,
                    cleanTitle = cleanTitle,
                    posterUrl = resolvedPosterUrl,
                    rating = bestRating,
                    ratingNumeric = bestRatingNumeric,
                    releaseYear = repYear,
                    addedEpoch = maxAddedEpoch,
                    genres = resolvedGenres,
                    primaryCategoryId = rep.categoryId.ifBlank { categoryIds.firstOrNull().orEmpty() },
                    categoryIdsCsv = categoryIdsCsv,
                    versionCount = componentIndices.size,
                    defaultVersionTag = repVersionTag,
                    containerExtension = rep.containerExtension.ifBlank { "mp4" },
                    popularityScore = popularityScore,
                    platformTags = platformTags
                )
            )

            if (provisionalCollections != null) {
                val colMatch = GlobalCollectionsIndexManager.lookupCollection(
                    tmdbId = bestTmdbId,
                    cleanTitle = cleanTitle,
                    releaseYear = repYear
                )
                if (colMatch != null) {
                    var bucket = provisionalCollections[colMatch.collectionId]
                    if (bucket == null) {
                        bucket = ArrayList(4)
                        provisionalCollections[colMatch.collectionId] = bucket
                    }
                    bucket.add((groups.size - 1) to colMatch)
                }
            }

            // Phase 7A: ONLY emit vod_group_members rows for multi-version groups (> 1 stream).
            // Singleton groups already store representativeStreamDbId on VodCatalogGroupEntity.
            if (componentIndices.size > 1) {
                for (j in 0 until componentIndices.size) {
                    val memberStream = streams[componentIndices[j]]
                    members.add(
                        VodGroupMemberEntity(
                            playlistId = playlistId,
                            streamType = cleanType,
                            groupKey = finalGroupKey,
                            streamDbId = memberStream.id
                        )
                    )
                }
            }

            materializedStreamCount += componentIndices.size
            if (onProgress != null && (materializedStreamCount - lastReportedMaterialized) >= 10_000) {
                lastReportedMaterialized = materializedStreamCount
                val reported = (halfCount + (materializedStreamCount / 2)).coerceIn(halfCount, count)
                onProgress.invoke(reported, count)
            }
        }

        // Level 2: Materialize official TMDb Movie Collections ONLY when >= 2 distinct movies exist
        val collections = ArrayList<VodCollectionEntity>()
        if (provisionalCollections != null && provisionalCollections.isNotEmpty()) {
            for ((colId, entries) in provisionalCollections) {
                if (entries.size < 2) continue

                var colName = ""
                var officialPoster = ""
                var fallbackPoster = ""
                var bestMemberScore = Double.NEGATIVE_INFINITY
                val memberScores = ArrayList<Double>(entries.size)

                for (entry in entries) {
                    val groupIdx = entry.first
                    val match = entry.second
                    val group = groups[groupIdx]

                    groups[groupIdx] = group.copy(
                        collectionId = match.collectionId,
                        collectionName = match.collectionName,
                        collectionOrder = match.releaseOrder
                    )

                    if (colName.isEmpty() && match.collectionName.isNotBlank()) {
                        colName = match.collectionName
                    }
                    if (officialPoster.isEmpty() && match.posterUrl.isNotBlank()) {
                        officialPoster = match.posterUrl
                    }
                    if (group.popularityScore > bestMemberScore && group.posterUrl.isNotBlank()) {
                        bestMemberScore = group.popularityScore
                        fallbackPoster = group.posterUrl
                    }
                    memberScores.add(group.popularityScore)
                }

                if (colName.isNotEmpty()) {
                    memberScores.sortDescending()
                    val topSliceCount = memberScores.size.coerceAtMost(3)
                    var topSum = 0.0
                    for (k in 0 until topSliceCount) {
                        topSum += memberScores[k]
                    }
                    val avgTopScore = if (topSliceCount > 0) topSum / topSliceCount else 0.0
                    val franchiseDepthBonus = ln(entries.size.toDouble() + 1.0) * 2.5
                    val collectionScore = avgTopScore + franchiseDepthBonus

                    collections.add(
                        VodCollectionEntity(
                            playlistId = playlistId,
                            collectionId = colId,
                            name = colName,
                            posterUrl = officialPoster.ifBlank { fallbackPoster },
                            movieCount = entries.size,
                            popularityScore = collectionScore
                        )
                    )
                }
            }

            collections.sortWith(
                compareByDescending<VodCollectionEntity> { it.popularityScore }
                    .thenByDescending { it.movieCount }
                    .thenBy { it.name }
            )
        }

        onProgress?.invoke(count, count)
        return LosslessGroupingResult(
            groups = groups,
            members = members,
            collections = collections
        )
    }

    private fun normalizeNumericTmdbId(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty() || trimmed == "0") return ""
        for (i in 0 until trimmed.length) {
            if (!trimmed[i].isDigit()) return ""
        }
        return if ((trimmed.toLongOrNull() ?: 0L) > 0L) trimmed else ""
    }

    private fun normalizeCleanTitleKey(cleanTitle: String): String {
        if (cleanTitle.isBlank()) return ""
        return Normalizer.normalize(cleanTitle, Normalizer.Form.NFD)
            .replace(COMBINING_MARKS_REGEX, "")
            .replace(NON_ALNUM_REGEX, " ")
            .replace(MULTI_SPACE_REGEX, " ")
            .trim()
            .lowercase(Locale.US)
    }
}
