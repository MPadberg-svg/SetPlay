package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.Normalizer
import java.util.Locale
import kotlin.math.ln

@Entity(
    tableName = "playlists"
)
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val serverUrl: String,
    val username: String,
    val password: String,
    val isActive: Boolean = true,
    val status: String = "Active",
    val expDate: String = "",
    val maxConnections: String = "1",
    val lastSyncTimestamp: Long = 0L,
    val channelCount: Int = 0,
    val vodCount: Int = 0,
    val seriesCount: Int = 0
)

@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["playlistId", "type", "categoryId"], unique = true),
        Index(value = ["playlistId", "type", "categoryName"])
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistId: Long,
    val categoryId: String,
    val categoryName: String,
    val type: String, // "LIVE", "MOVIE", "SERIES"
    val isLocked: Boolean = false
)

data class CategoryWithCount(
    val categoryId: String,
    val categoryName: String,
    val type: String,
    val isLocked: Boolean,
    val streamCount: Int
)

data class CategoryCountTuple(
    val categoryId: String,
    val streamCount: Int
)

@Entity(
    tableName = "streams",
    indices = [
        Index(value = ["playlistId", "streamType", "streamId"], unique = true),
        Index(value = ["playlistId", "streamType", "categoryId"]),
        Index(value = ["playlistId", "isFavorite"]),
        Index(value = ["playlistId", "watchHistoryTimestamp"]),
        Index(value = ["name"]),
        Index(value = ["playlistId", "streamType", "canonicalGroupKey"]),
        Index(value = ["playlistId", "streamType", "addedEpoch"]),
        Index(value = ["playlistId", "streamType", "ratingNumeric"]),
        Index(value = ["playlistId", "streamType", "streamId", "metadataHash"])
    ]
)
data class StreamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistId: Long,
    val streamId: Long,
    val categoryId: String,
    val name: String,
    val streamType: String, // "live", "movie", "series"
    val streamIcon: String = "",
    val epgChannelId: String = "",
    val rating: String = "",
    val tvArchive: Int = 0, // Catchup indicator: 1 = supported
    val tvArchiveDuration: Int = 0, // Catchup duration in days
    val containerExtension: String = "ts",
    val isFavorite: Boolean = false,
    val isLocked: Boolean = false,
    val watchHistoryTimestamp: Long = 0L,
    val lastPositionMs: Long = 0L,
    val tmdbId: String = "",
    val canonicalGroupKey: String = "",
    val versionTag: String = "",
    val addedEpoch: Long = 0L,
    val ratingNumeric: Double = 0.0,
    val releaseYear: Int = 0,
    val genres: String = "",
    val metadataHash: Long = 0L
)

data class GroupedVodStream(
    @Embedded val stream: StreamEntity,
    val versionCount: Int = 1
)

@Entity(
    tableName = "vod_catalog_groups",
    indices = [
        Index(value = ["playlistId", "streamType", "groupKey"], unique = true),
        Index(value = ["playlistId", "streamType", "popularityScore"]),
        Index(value = ["playlistId", "streamType", "addedEpoch"]),
        Index(value = ["playlistId", "streamType", "primaryCategoryId"]),
        Index(value = ["playlistId", "streamType", "cleanTitle"])
    ]
)
data class VodCatalogGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistId: Long,
    val streamType: String, // "movie" or "series"
    val groupKey: String,
    val representativeStreamDbId: Long,
    val representativeStreamId: Long,
    val cleanTitle: String,
    val posterUrl: String = "",
    val rating: String = "",
    val ratingNumeric: Double = 0.0,
    val releaseYear: Int = 0,
    val addedEpoch: Long = 0L,
    val genres: String = "",
    val primaryCategoryId: String = "",
    val categoryIdsCsv: String = "",
    val versionCount: Int = 1,
    val defaultVersionTag: String = "",
    val containerExtension: String = "mp4",
    val popularityScore: Double = 0.0,
    val platformTags: String = ""
)

@Fts4(contentEntity = VodCatalogGroupEntity::class)
@Entity(tableName = "vod_catalog_groups_fts")
data class VodCatalogGroupFtsEntity(
    val cleanTitle: String,
    val genres: String
)

@Entity(
    tableName = "vod_group_members",
    primaryKeys = ["playlistId", "streamType", "groupKey", "streamDbId"],
    indices = [
        Index(value = ["playlistId", "streamType", "groupKey"]),
        Index(value = ["streamDbId"])
    ]
)
data class VodGroupMemberEntity(
    val playlistId: Long,
    val streamType: String,
    val groupKey: String,
    val streamDbId: Long
)

@Entity(
    tableName = "sync_section_state",
    primaryKeys = ["playlistId", "sectionType"]
)
data class SyncSectionStateEntity(
    val playlistId: Long,
    val sectionType: String, // "LIVE", "MOVIE", "SERIES"
    val status: String = "IDLE", // IDLE, PENDING, RUNNING, COMPLETED, FAILED, INTERRUPTED
    val stage: String = "IDLE",
    val sessionId: String = "",
    val isIndeterminate: Boolean = false,
    val progressPercent: Int = 0,
    val totalDiscovered: Int = 0,
    val itemsDownloaded: Int = 0,
    val itemsProcessed: Int = 0,
    val itemsUnchanged: Int = 0,
    val itemsAdded: Int = 0,
    val itemsUpdated: Int = 0,
    val itemsRemoved: Int = 0,
    val itemsSkipped: Int = 0,
    val itemsGrouped: Int = 0,
    val itemsPersisted: Int = 0,
    val lastSuccessfulSyncMs: Long = 0L,
    val lastAttemptedSyncMs: Long = 0L,
    val pendingUpdateAvailable: Boolean = false,
    val pendingUpdateSummary: String = "",
    val etag: String = "",
    val lastModified: String = "",
    val lastContentLength: Long = -1L,
    val errorMessage: String = ""
)

object VodGroupingNormalizer {
    private val COMBINING_MARKS_REGEX = Regex("\\p{Mn}+")
    private val NON_ALNUM_REGEX = Regex("[^\\p{L}\\p{N}]+")
    private val ANY_YEAR_REGEX = Regex("(?<!\\d)(19[2-9]\\d|20[0-3]\\d)(?!\\d)")
    private val BRACKET_YEAR_REGEX = Regex("[\\(\\[]\\s*(19[2-9]\\d|20[0-3]\\d)\\s*[\\)\\]]")
    private val SEPARATOR_YEAR_REGEX = Regex("[-|:]\\s*(19[2-9]\\d|20[0-3]\\d)(?!\\d)")
    private val TRAILING_YEAR_REGEX = Regex("(?:^|\\s)(19[2-9]\\d|20[0-3]\\d)\\s*$")

    private val LEADING_BRACKET_PREFIX_REGEX = Regex("^\\s*[\\[\\(]([^\\]\\)]{1,18})[\\]\\)]\\s*[-|│┃:]?\\s*")
    private val LEADING_PIPE_PREFIX_REGEX = Regex("^\\s*([A-Za-z0-9]{1,8}(?:\\s*[/&-]?\\s*[A-Za-z0-9]{1,8})?)\\s*[|│┃]\\s*")
    private val LEADING_DASH_COLON_PREFIX_REGEX = Regex(
        "(?i)^\\s*(EN|ENG|AR|ARB|ARA|FR|FRE|ES|ESP|SPA|PT|POR|DE|GER|IT|ITA|TR|TUR|NL|RU|RUS|PL|GR|IN|IND|HI|KR|KOR|JP|JPN|LAT|UK|US|CA|AU|BR|MX|MULTI|4K|UHD|FHD|HD|HEVC|VOD|TOP|NEW)(?:\\s*[/&-]\\s*(4K|UHD|FHD|HD|HEVC|SUB|DUB|MULTI|VOSTFR))?\\s*[-:]\\s+"
    )
    private val ANY_BRACKET_TOKEN_REGEX = Regex("[\\[\\(]([^\\]\\)]{1,20})[\\]\\)]")
    private val STANDALONE_QUALITY_REGEX = Regex("(?i)(?<![A-Za-z0-9])(4K|2160P|UHD|HEVC|H265|X265|HDR10|HDR|VOSTFR|SUBBED|DUBBED|MULTI|MULTISUB|60FPS|3D)(?![A-Za-z0-9])")
    private val STANDALONE_UPPER_EDITION_REGEX = Regex("(?<![A-Za-z0-9])(SUB|DUB|FHD)(?![A-Za-z0-9])")
    private val TRAILING_TECH_SUFFIX_REGEX = Regex(
        "(?i)(?:\\s*[-|│┃:]\\s*|\\s+)(?:4K|2160P|UHD|FHD|HD|SD|HEVC|H265|X265|HDR10|HDR|1080P|720P|SUB|SUBBED|DUB|DUBBED|MULTI|MULTISUB|VOSTFR|CAM|TS|3D|60FPS)\\s*$"
    )
    private val TRAILING_EMPTY_DELIMITERS_REGEX = Regex("[\\s\\-|│┃:]+$")

    private val LANG_CANONICAL_MAP = mapOf(
        "EN" to "EN", "ENG" to "EN",
        "AR" to "AR", "ARB" to "AR", "ARA" to "AR",
        "FR" to "FR", "FRE" to "FR",
        "ES" to "ES", "ESP" to "ES", "SPA" to "ES",
        "PT" to "PT", "POR" to "PT",
        "DE" to "DE", "GER" to "DE",
        "IT" to "IT", "ITA" to "IT",
        "TR" to "TR", "TUR" to "TR",
        "NL" to "NL",
        "RU" to "RU", "RUS" to "RU",
        "PL" to "PL",
        "GR" to "GR",
        "HI" to "HI",
        "IN" to "IN", "IND" to "IN",
        "KR" to "KR", "KOR" to "KR",
        "JP" to "JP", "JPN" to "JP",
        "LAT" to "LAT",
        "UK" to "UK",
        "US" to "US",
        "CA" to "CA",
        "AU" to "AU",
        "BR" to "BR",
        "MX" to "MX",
        "MULTI" to "MULTI"
    )

    private val QUALITY_CANONICAL_MAP = mapOf(
        "4K" to "4K", "2160P" to "4K",
        "UHD" to "UHD",
        "FHD" to "FHD",
        "HEVC" to "HEVC", "H265" to "HEVC", "X265" to "HEVC",
        "HDR" to "HDR", "HDR10" to "HDR",
        "SUB" to "SUB", "SUBBED" to "SUB",
        "DUB" to "DUB", "DUBBED" to "DUB",
        "VOSTFR" to "VOSTFR",
        "MULTI" to "MULTI", "MULTISUB" to "MULTI",
        "3D" to "3D",
        "60FPS" to "60FPS"
    )

    fun extractVersionTag(rawName: String): String {
        if (rawName.isBlank()) return ""
        val langTags = LinkedHashSet<String>()
        val qualityTags = LinkedHashSet<String>()

        fun collectTokens(chunk: String, allowLanguage: Boolean) {
            val parts = chunk.split(Regex("[\\s/|│┃,&:-]+"))
            for (part in parts) {
                val upper = part.trim().uppercase(Locale.US)
                if (upper.isEmpty()) continue
                if (allowLanguage) {
                    val mapped = LANG_CANONICAL_MAP[upper]
                    if (mapped != null) {
                        if (mapped == "MULTI") qualityTags.add(mapped) else langTags.add(mapped)
                        continue
                    }
                }
                val qualityMapped = QUALITY_CANONICAL_MAP[upper]
                if (qualityMapped != null) {
                    qualityTags.add(qualityMapped)
                }
            }
        }

        // 1. Peel leading IPTV prefix tags ([AR], EN |, FR -, ES |, 4K |, etc.)
        var remainder = rawName.trim()
        var guard = 0
        while (guard++ < 4) {
            val bracketMatch = LEADING_BRACKET_PREFIX_REGEX.find(remainder)
            if (bracketMatch != null) {
                val inner = bracketMatch.groupValues[1].trim()
                if (!inner.matches(Regex("^\\d{4}$"))) {
                    collectTokens(inner, allowLanguage = true)
                    remainder = remainder.substring(bracketMatch.range.last + 1)
                    continue
                }
            }
            val pipeMatch = LEADING_PIPE_PREFIX_REGEX.find(remainder)
            if (pipeMatch != null) {
                collectTokens(pipeMatch.groupValues[1], allowLanguage = true)
                remainder = remainder.substring(pipeMatch.range.last + 1)
                continue
            }
            val dashMatch = LEADING_DASH_COLON_PREFIX_REGEX.find(remainder)
            if (dashMatch != null) {
                collectTokens(dashMatch.groupValues[1], allowLanguage = true)
                if (dashMatch.groupValues.size > 2 && dashMatch.groupValues[2].isNotBlank()) {
                    collectTokens(dashMatch.groupValues[2], allowLanguage = false)
                }
                remainder = remainder.substring(dashMatch.range.last + 1)
                continue
            }
            break
        }

        // 2. Scan bracketed/parenthesized tokens anywhere in rawName (only if all tokens inside are known tags)
        for (match in ANY_BRACKET_TOKEN_REGEX.findAll(rawName)) {
            val inner = match.groupValues[1].trim()
            if (inner.isEmpty() || inner.matches(Regex("^\\d{4}$"))) continue
            val parts = inner.split(Regex("[\\s/|│┃,&:-]+")).filter { it.isNotBlank() }
            if (parts.isNotEmpty() && parts.all { p ->
                    val u = p.uppercase(Locale.US)
                    LANG_CANONICAL_MAP.containsKey(u) || QUALITY_CANONICAL_MAP.containsKey(u)
                }
            ) {
                collectTokens(inner, allowLanguage = true)
            }
        }

        // 3. Scan standalone technical/edition tokens anywhere in the title
        for (match in STANDALONE_QUALITY_REGEX.findAll(rawName)) {
            QUALITY_CANONICAL_MAP[match.groupValues[1].uppercase(Locale.US)]?.let { qualityTags.add(it) }
        }
        for (match in STANDALONE_UPPER_EDITION_REGEX.findAll(rawName)) {
            QUALITY_CANONICAL_MAP[match.groupValues[1]]?.let { qualityTags.add(it) }
        }

        val combined = LinkedHashSet<String>()
        combined.addAll(langTags)
        combined.addAll(qualityTags)
        return combined.take(3).joinToString(" • ")
    }

    fun extractReleaseYear(rawName: String, rawYearField: String): Int {
        // 1. Prefer explicit provider year / release_date field
        if (rawYearField.isNotBlank()) {
            ANY_YEAR_REGEX.find(rawYearField)?.groupValues?.getOrNull(1)?.toIntOrNull()?.let { y ->
                if (y in 1920..2030) return y
            }
        }
        if (rawName.isBlank()) return 0

        // 2. Prefer parenthesized/bracketed year e.g. "(2024)" or "[2024]" (take last occurrence)
        BRACKET_YEAR_REGEX.findAll(rawName)
            .mapNotNull { it.groupValues.getOrNull(1)?.toIntOrNull() }
            .filter { it in 1920..2030 }
            .lastOrNull()
            ?.let { return it }

        // 3. Fallback to separator-prefixed year e.g. "- 2024" or "| 2024"
        SEPARATOR_YEAR_REGEX.findAll(rawName)
            .mapNotNull { it.groupValues.getOrNull(1)?.toIntOrNull() }
            .filter { it in 1920..2030 }
            .lastOrNull()
            ?.let { return it }

        // 4. Fallback to trailing year after stripping technical suffixes
        var stripped = rawName.trim()
        var guard = 0
        while (guard++ < 4) {
            val next = stripped.replace(TRAILING_TECH_SUFFIX_REGEX, "").trim()
            if (next == stripped) break
            stripped = next
        }
        TRAILING_YEAR_REGEX.find(stripped)?.groupValues?.getOrNull(1)?.toIntOrNull()?.let { y ->
            if (y in 1920..2030) return y
        }
        return 0
    }

    private val TMDB_POSTER_PATH_REGEX = Regex("(?i)image\\.tmdb\\.org/t/p/[^/]+/([^/?#]+)")
    private val ONLY_FOUR_DIGITS_REGEX = Regex("^\\d{4}$")
    private val MULTI_WHITESPACE_REGEX = Regex("\\s+")
    private val TRAILING_SUB_BRACKET_REGEX = Regex(
        "(?i)\\s*[\\[\\(]\\s*(?:VOSTFR|SUB(?:\\s+[A-Za-z]{2,12})?|DUB(?:\\s+[A-Za-z]{2,12})?|MULTI[\\s-]?SUB|MULTI|4K|UHD|FHD|HD|HEVC|HDR10?|1080P|720P|2160P|CAM|TS)\\s*[\\]\\)]\\s*$"
    )
    private val TRAILING_SEP_YEAR_REGEX = Regex("\\s*[-|│┃:]\\s*(?:19[2-9]\\d|20[0-3]\\d)\\s*$")
    private val TRAILING_BRACKET_YEAR_REGEX = Regex("\\s*[\\(\\[]\\s*(?:19[2-9]\\d|20[0-3]\\d)\\s*[\\)\\]]\\s*$")

    private val CATEGORY_LANG_RULES: List<Pair<Regex, String>> = listOf(
        Regex("(?i)(?<![A-Za-z])(EN|ENG|ENGLISH)(?![A-Za-z])") to "English",
        Regex("(?i)(?<![A-Za-z])(AR|ARB|ARA|ARABIC)(?![A-Za-z])") to "Arabic (Sub)",
        Regex("(?i)(?<![A-Za-z])(VOSTFR|FR|FRE|FRENCH)(?![A-Za-z])") to "French (VOSTFR)",
        Regex("(?i)(?<![A-Za-z])(ES|ESP|SPA|SPANISH|LAT|LATINO)(?![A-Za-z])") to "Spanish",
        Regex("(?i)(?<![A-Za-z])(PT|POR|PORTUGUESE|BR|BRAZIL)(?![A-Za-z])") to "Portuguese",
        Regex("(?i)(?<![A-Za-z])(DE|GER|GERMAN|DEUTSCH)(?![A-Za-z])") to "German",
        Regex("(?i)(?<![A-Za-z])(IT|ITA|ITALIAN)(?![A-Za-z])") to "Italian",
        Regex("(?i)(?<![A-Za-z])(TR|TUR|TURKISH)(?![A-Za-z])") to "Turkish",
        Regex("(?i)(?<![A-Za-z])(IR|IRANIAN|IRAN|PERSIAN|FARSI|FA)(?![A-Za-z])") to "Persian (Sub)",
        Regex("(?i)(?<![A-Za-z])(BG|BULGARIAN)(?![A-Za-z])") to "Bulgarian (Sub)",
        Regex("(?i)(?<![A-Za-z])(ALB|ALBANIAN|FILMET)(?![A-Za-z])") to "Albanian (Sub)",
        Regex("(?i)(?<![A-Za-z])(KU|KURDISH)(?![A-Za-z])") to "Kurdish (Sub)",
        Regex("(?i)(?<![A-Za-z])(IN|IND|HI|HINDI|BOLLYWOOD)(?![A-Za-z])") to "Hindi",
        Regex("(?i)(?<![A-Za-z])(EX[-\\s]?YU)(?![A-Za-z])") to "Ex-YU",
        Regex("(?i)(?<![A-Za-z])(MULTI[-\\s]?SUB|MULTISUB|MULTI)(?![A-Za-z])") to "Multi-Sub"
    )

    fun extractTmdbPosterHash(posterUrl: String): String {
        if (posterUrl.isBlank()) return ""
        val match = TMDB_POSTER_PATH_REGEX.find(posterUrl.trim()) ?: return ""
        val filename = match.groupValues.getOrNull(1)?.trim().orEmpty()
        if (filename.isEmpty()) return ""
        val dotIndex = filename.lastIndexOf('.')
        val stem = if (dotIndex > 0) filename.substring(0, dotIndex) else filename
        if (stem.length < 12) return ""
        return stem
    }

    fun extractCleanDisplayTitle(rawName: String): String {
        val originalTrimmed = rawName.trim()
        if (originalTrimmed.isEmpty()) return ""

        var cleaned = originalTrimmed
            .replace("*", "")
            .trim()

        // 1. Peel leading IPTV prefix tags ([AR], EN |, FR -, 4K |, etc.)
        var prefixGuard = 0
        while (prefixGuard++ < 4) {
            val bracketMatch = LEADING_BRACKET_PREFIX_REGEX.find(cleaned)
            if (bracketMatch != null) {
                val inner = bracketMatch.groupValues[1].trim()
                if (!ONLY_FOUR_DIGITS_REGEX.matches(inner)) {
                    cleaned = cleaned.substring(bracketMatch.range.last + 1).trim()
                    continue
                }
            }
            val pipeMatch = LEADING_PIPE_PREFIX_REGEX.find(cleaned)
            if (pipeMatch != null) {
                cleaned = cleaned.substring(pipeMatch.range.last + 1).trim()
                continue
            }
            val dashMatch = LEADING_DASH_COLON_PREFIX_REGEX.find(cleaned)
            if (dashMatch != null) {
                cleaned = cleaned.substring(dashMatch.range.last + 1).trim()
                continue
            }
            break
        }

        // 2. Strip trailing subtitle/quality tags & trailing duplicate year suffixes
        var suffixGuard = 0
        while (suffixGuard++ < 6) {
            var next = cleaned
            next = next.replace(TRAILING_SUB_BRACKET_REGEX, "").trim()
            next = next.replace(TRAILING_TECH_SUFFIX_REGEX, "").trim()
            next = next.replace(TRAILING_BRACKET_YEAR_REGEX, "").trim()
            next = next.replace(TRAILING_SEP_YEAR_REGEX, "").trim()
            next = next.replace(TRAILING_EMPTY_DELIMITERS_REGEX, "").trim()
            if (next == cleaned) break
            cleaned = next
        }

        cleaned = cleaned.replace(MULTI_WHITESPACE_REGEX, " ").trim()

        // Guard: if stripping leaves blank or only a 4-digit year (e.g., "2026"), keep the original name safely
        if (cleaned.isBlank() || ONLY_FOUR_DIGITS_REGEX.matches(cleaned)) {
            val safeFallback = originalTrimmed.replace("*", "").replace(MULTI_WHITESPACE_REGEX, " ").trim()
            return if (safeFallback.isNotBlank()) safeFallback else originalTrimmed
        }
        return cleaned
    }

    fun extractVersionTagWithCategory(rawName: String, categoryName: String = ""): String {
        val detectedLanguages = LinkedHashSet<String>()
        val detectedQualities = LinkedHashSet<String>()

        fun scanLanguageText(source: String) {
            if (source.isBlank()) return
            for (rule in CATEGORY_LANG_RULES) {
                if (rule.first.containsMatchIn(source)) {
                    detectedLanguages.add(rule.second)
                }
            }
        }

        fun scanQualityText(source: String) {
            if (source.isBlank()) return
            for (match in STANDALONE_QUALITY_REGEX.findAll(source)) {
                val mapped = QUALITY_CANONICAL_MAP[match.groupValues[1].uppercase(Locale.US)]
                if (mapped != null && mapped in setOf("4K", "UHD", "HDR", "HEVC")) {
                    detectedQualities.add(mapped)
                }
            }
        }

        // 1. Inspect explicit prefix/bracket tags in rawName first
        val baseTag = extractVersionTag(rawName)
        if (baseTag.isNotBlank()) {
            val baseParts = baseTag.split("•")
            for (part in baseParts) {
                val token = part.trim().uppercase(Locale.US)
                if (token.isEmpty()) continue
                when (token) {
                    "EN", "UK", "US" -> detectedLanguages.add("English")
                    "AR" -> detectedLanguages.add("Arabic (Sub)")
                    "FR", "VOSTFR" -> detectedLanguages.add("French (VOSTFR)")
                    "ES", "LAT" -> detectedLanguages.add("Spanish")
                    "PT", "BR" -> detectedLanguages.add("Portuguese")
                    "DE" -> detectedLanguages.add("German")
                    "IT" -> detectedLanguages.add("Italian")
                    "TR" -> detectedLanguages.add("Turkish")
                    "IN", "HI" -> detectedLanguages.add("Hindi")
                    "MULTI" -> detectedLanguages.add("Multi-Sub")
                    "4K", "UHD", "HDR", "HEVC" -> detectedQualities.add(token)
                }
            }
        }

        // Scan rawName for extended languages (e.g., IR, BG, ALB, FILMET, KU, EXYU)
        scanLanguageText(rawName)
        scanQualityText(rawName)

        // 2. Enrich from categoryName if language wasn't explicitly found in rawName (or add category quality)
        if (detectedLanguages.isEmpty() && categoryName.isNotBlank()) {
            scanLanguageText(categoryName)
        }
        if (categoryName.isNotBlank()) {
            scanQualityText(categoryName)
        }

        val combined = LinkedHashSet<String>()
        val primaryLang = detectedLanguages.firstOrNull()
        if (primaryLang != null) {
            combined.add(primaryLang)
        }
        for (q in detectedQualities) {
            if (combined.size < 3) {
                combined.add(q)
            }
        }
        return combined.joinToString(" • ")
    }

    fun isPlaceholderPerfectRating(ratingNumeric: Double, versionCount: Int): Boolean {
        return ratingNumeric >= 9.95 && versionCount <= 3
    }

    fun computePopularityQualityScore(
        ratingNumeric: Double,
        versionCount: Int,
        releaseYear: Int,
        hasPoster: Boolean,
        isPreferredLang: Boolean,
        globalTmdbScore: Double = 0.0
    ): Double {
        val safeVersions = versionCount.coerceAtLeast(1)
        val sanitizedRating = when {
            isPlaceholderPerfectRating(ratingNumeric, safeVersions) -> 5.4
            ratingNumeric >= 9.8 && safeVersions <= 2 -> 5.6
            ratingNumeric in 4.95..5.05 && safeVersions <= 1 -> 4.9
            ratingNumeric <= 0.0 -> 5.2
            else -> ratingNumeric.coerceIn(1.0, 9.6)
        }
        val multiRegionBoost = ln(safeVersions.toDouble() + 1.0) * 1.9
        val recencyBonus = when {
            releaseYear >= 2024 -> 0.85
            releaseYear >= 2021 -> 0.45
            else -> 0.0
        }
        val posterBonus = if (hasPoster) 0.5 else -2.0
        val langBonus = if (isPreferredLang) 0.65 else 0.0
        val localBayesianScore = sanitizedRating + multiRegionBoost + recencyBonus + posterBonus + langBonus

        val clampedGlobal = globalTmdbScore.coerceIn(0.0, 100.0)
        return if (clampedGlobal > 0.0) {
            (clampedGlobal * 0.65) + localBayesianScore
        } else {
            localBayesianScore
        }
    }

    fun scoreVersionPreference(
        versionTag: String,
        hasPoster: Boolean,
        containerExt: String,
        ratingNumeric: Double
    ): Int {
        val upper = versionTag.uppercase(Locale.US)
        var score = when {
            upper.contains("ENGLISH") || Regex("(?<![A-Z])(EN|UK|US)(?![A-Z])").containsMatchIn(upper) -> 100
            upper.contains("ARABIC") || Regex("(?<![A-Z])AR(?![A-Z])").containsMatchIn(upper) -> 95
            upper.contains("FRENCH") || upper.contains("VOSTFR") || Regex("(?<![A-Z])FR(?![A-Z])").containsMatchIn(upper) -> 85
            upper.contains("MULTI") -> 70
            else -> 20
        }
        if (upper.contains("4K") || upper.contains("UHD")) {
            score += 15
        }
        if (hasPoster) {
            score += 25
        }
        val extLower = containerExt.trim().lowercase(Locale.US)
        if (extLower == "mp4" || extLower == "mkv") {
            score += 5
        }
        if (ratingNumeric > 0.0) {
            score += ratingNumeric.coerceIn(0.0, 10.0).toInt()
        }
        return score
    }

    fun computeCanonicalGroupKey(
        streamType: String,
        rawName: String,
        tmdbId: String,
        releaseYear: Int,
        streamId: Long,
        posterUrl: String = ""
    ): String {
        val cleanType = streamType.trim().lowercase(Locale.US).ifBlank { "vod" }

        // Tier 1 (Zero False Positives): Numeric TMDb ID > 0
        val cleanTmdb = tmdbId.trim()
        if (cleanTmdb.isNotEmpty() && cleanTmdb.all { it.isDigit() } && (cleanTmdb.toLongOrNull() ?: 0L) > 0L) {
            return "tmdb_${cleanType}_$cleanTmdb"
        }

        // Tier 2 (Cross-Language Split Guard): TMDb Poster Hash + Release Year
        val posterHash = extractTmdbPosterHash(posterUrl)
        if (posterHash.isNotBlank() && releaseYear > 0) {
            return "poster_${cleanType}_${posterHash}_${releaseYear}"
        }

        // Tier 3 (Strict Normalized Clean Title + Year)
        var cleaned = rawName.trim().replace("*", "").trim()
        var prefixGuard = 0
        while (prefixGuard++ < 4) {
            val bracketMatch = LEADING_BRACKET_PREFIX_REGEX.find(cleaned)
            if (bracketMatch != null) {
                val inner = bracketMatch.groupValues[1].trim()
                if (!ONLY_FOUR_DIGITS_REGEX.matches(inner)) {
                    cleaned = cleaned.substring(bracketMatch.range.last + 1)
                    continue
                }
            }
            val pipeMatch = LEADING_PIPE_PREFIX_REGEX.find(cleaned)
            if (pipeMatch != null) {
                cleaned = cleaned.substring(pipeMatch.range.last + 1)
                continue
            }
            val dashMatch = LEADING_DASH_COLON_PREFIX_REGEX.find(cleaned)
            if (dashMatch != null) {
                cleaned = cleaned.substring(dashMatch.range.last + 1)
                continue
            }
            break
        }

        // Remove bracketed/parenthesized years and known language/quality tag brackets
        cleaned = cleaned.replace(BRACKET_YEAR_REGEX, " ")
        cleaned = ANY_BRACKET_TOKEN_REGEX.replace(cleaned) { match ->
            val inner = match.groupValues[1].trim()
            val parts = inner.split(Regex("[\\s/|│┃,&:-]+")).filter { it.isNotBlank() }
            if (parts.isNotEmpty() && parts.all { p ->
                    val u = p.uppercase(Locale.US)
                    LANG_CANONICAL_MAP.containsKey(u) || QUALITY_CANONICAL_MAP.containsKey(u) || u in setOf("HD", "SD", "1080P", "720P", "CAM", "TS")
                }
            ) " " else match.value
        }

        // Strip trailing technical/quality suffixes and trailing release year
        var suffixGuard = 0
        while (suffixGuard++ < 5) {
            var next = cleaned.replace(TRAILING_SUB_BRACKET_REGEX, "").trim()
            next = next.replace(TRAILING_TECH_SUFFIX_REGEX, "").trim()
            if (releaseYear in 1920..2030) {
                next = next.replace(Regex("(?:\\s*[-|│┃:]\\s*|\\s+)$releaseYear\\s*$"), "").trim()
            } else {
                next = next.replace(TRAILING_SEP_YEAR_REGEX, "").trim()
            }
            next = next.replace(TRAILING_EMPTY_DELIMITERS_REGEX, "").trim()
            if (next == cleaned) break
            cleaned = next
        }

        val normalizedTitle = Normalizer.normalize(cleaned, Normalizer.Form.NFD)
            .replace(COMBINING_MARKS_REGEX, "")
            .replace(NON_ALNUM_REGEX, " ")
            .trim()
            .lowercase(Locale.US)

        return if (normalizedTitle.isNotBlank()) {
            "title_${cleanType}_${normalizedTitle}_${if (releaseYear > 0) releaseYear else 0}"
        } else {
            "raw_${cleanType}_$streamId"
        }
    }

    fun normalizeRatingToTenScale(rawRating10: String, rawRating5: String): Pair<String, Double> {
        val parsed10 = rawRating10.trim().replace(',', '.').toDoubleOrNull()
            ?.takeIf { it > 0.0 && it <= 10.0 }
        val resolved = parsed10 ?: rawRating5.trim().replace(',', '.').toDoubleOrNull()
            ?.takeIf { it > 0.0 && it <= 5.0 }
            ?.let { (it * 2.0).coerceIn(0.0, 10.0) }
        return if (resolved != null && resolved > 0.0) {
            val formatted = String.format(Locale.US, "%.1f", resolved)
            Pair(formatted, formatted.toDoubleOrNull() ?: resolved)
        } else {
            Pair("", 0.0)
        }
    }
}

@Entity(
    tableName = "epg_programs",
    indices = [
        Index(value = ["playlistId", "streamId"]),
        Index(value = ["startEpoch", "endEpoch"])
    ]
)
data class EpgProgramEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistId: Long,
    val streamId: Long,
    val epgChannelId: String = "",
    val title: String,
    val description: String = "",
    val startEpoch: Long,
    val endEpoch: Long
)

@Entity(tableName = "parental_settings")
data class ParentalSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val pin: String = "0000",
    val isEnabled: Boolean = false,
    val lockedCategoriesJson: String = "[]" // JSON array of category IDs
)

@Entity(
    tableName = "favorite_teams",
    indices = [Index(value = ["teamName"])]
)
data class FavoriteTeamEntity(
    @PrimaryKey val teamId: String,
    val teamName: String,
    val shortName: String = "",
    val badgeUrl: String = "",
    val country: String = "",
    val primaryLeague: String = "",
    val addedTimestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tournament_preferences"
)
data class TournamentPreferenceEntity(
    @PrimaryKey val tournamentId: String,
    val tournamentName: String,
    val countryOrRegion: String = "",
    val isEnabled: Boolean = true,
    val isPinned: Boolean = false,
    val sortOrder: Int = 0
)

