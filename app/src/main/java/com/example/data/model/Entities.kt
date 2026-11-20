package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.Normalizer
import java.util.Locale

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
        Index(value = ["playlistId", "streamType", "ratingNumeric"])
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
    val genres: String = ""
)

data class GroupedVodStream(
    @Embedded val stream: StreamEntity,
    val versionCount: Int = 1
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
                    LANG_CANONICAL_MAP[upper]?.let { mapped ->
                        if (mapped == "MULTI") qualityTags.add(mapped) else langTags.add(mapped)
                        continue
                    }
                }
                QUALITY_CANONICAL_MAP[upper]?.let { mapped ->
                    qualityTags.add(mapped)
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

    fun computeCanonicalGroupKey(
        streamType: String,
        rawName: String,
        tmdbId: String,
        releaseYear: Int,
        streamId: Long
    ): String {
        val cleanType = streamType.trim().lowercase(Locale.US).ifBlank { "vod" }

        // Tier 1 (Zero False Positives): Numeric TMDb ID > 0
        val cleanTmdb = tmdbId.trim()
        if (cleanTmdb.isNotEmpty() && cleanTmdb.all { it.isDigit() } && (cleanTmdb.toLongOrNull() ?: 0L) > 0L) {
            return "tmdb_${cleanType}_$cleanTmdb"
        }

        // Tier 2 (Strict Title + Year)
        var cleaned = rawName.trim()
        var prefixGuard = 0
        while (prefixGuard++ < 4) {
            val bracketMatch = LEADING_BRACKET_PREFIX_REGEX.find(cleaned)
            if (bracketMatch != null) {
                val inner = bracketMatch.groupValues[1].trim()
                if (!inner.matches(Regex("^\\d{4}$"))) {
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
            var next = cleaned.replace(TRAILING_TECH_SUFFIX_REGEX, "").trim()
            if (releaseYear in 1920..2030) {
                next = next.replace(Regex("(?:\\s*[-|│┃:]\\s*|\\s+)$releaseYear\\s*$"), "").trim()
            } else {
                next = next.replace(Regex("\\s*[-|│┃:]\\s*(?:19[2-9]\\d|20[0-3]\\d)\\s*$"), "").trim()
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

