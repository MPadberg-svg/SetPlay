package com.example.util

/**
 * Centralized stream & category metadata parser for IPTV catalogs.
 * Extracts authentic language/region tags and resolution badges while cleaning raw provider prefixes
 * (e.g. "ES| Saw X", "[01] AR | BEIN SPORTS 02 FHD HEVC", "FR┃ Dune: Part Two [4K UHD]")
 * for display in the UI without mutating the underlying SQLite `StreamEntity.name`.
 */
object StreamMetadataParser {

    // Known region / language / bouquet codes that may appear before a colon or pipe
    private val KNOWN_REGION_CODES = setOf(
        "AR", "ARA", "ARAB", "ARABE", "ARABIC",
        "EN", "ENG", "ENGLISH", "UK", "US", "USA", "CA",
        "FR", "FRE", "FRA", "FRENCH",
        "ES", "ESP", "SPA", "SPANISH", "LAT", "LATAM",
        "DE", "GER", "DEU", "GERMAN",
        "IT", "ITA", "ITALIAN",
        "PT", "POR", "BR", "BRA", "PORTUGUESE",
        "TR", "TUR", "TURKISH",
        "NL", "NED", "DUTCH",
        "MA", "MAR", "MOR", "MOROCCO",
        "DZ", "ALG", "TN", "TUN", "EG", "EGY", "KSA", "SA", "UAE", "QA", "KW",
        "AF", "AFR", "ALB", "RU", "RUS", "PL", "POL", "GR", "GRE", "RO", "ROM", "IN", "IND",
        "24/7", "VIP", "RAW", "MULTI", "SPORT", "SPORTS"
    )

    // Matches leading index/tag brackets or pipe-enclosed tags like "[01]", "(12)", "[VIP]", "┃FR┃", "|AR|"
    private val LEADING_BRACKET_OR_BAR_TAG_REGEX = Regex(
        """^\s*(?:\[[^\]]{1,12}]|\([^\)]{1,12}\)|[|┃¦][A-Za-z0-9/+\-_ ]{2,8}[|┃¦]|★+|•+)\s*"""
    )

    // Matches leading prefix before a pipe/heavy-bar/dash separator: e.g. "ES|", "AR |", "FR┃", "24/7 |"
    private val LEADING_PIPE_OR_DASH_PREFIX_REGEX = Regex(
        """^\s*([A-Za-z]{2,7}|24/7|VIP|UHD|4K|MULTI)\s*(?:\||┃|¦| - | – | — )\s*""",
        RegexOption.IGNORE_CASE
    )

    // Matches leading known region/country code before a colon: e.g. "AR: ", "MA: ", "KSA: ", "EN: "
    // (Restricted to short uppercase/known codes so movie titles like "Dune: Part Two" are never stripped!)
    private val LEADING_COLON_PREFIX_REGEX = Regex(
        """^\s*([A-Za-z]{2,4})\s*:\s+"""
    )

    // Matches trailing technical brackets like "[4K UHD]", "[HEVC]", "[MP4A-LATM]", "[H265]", "(Server 1)"
    private val TRAILING_TECH_BRACKET_REGEX = Regex(
        """\s*(?:\[[^\]]*(?:4K|UHD|2160P?|FHD|1080[PI]?|HD|720P?|SD|HEVC|H\.?265|H\.?264|AAC|MP4A|RAW|MAX|VIP|MULTI|FPS|SERVER|BACKUP)[^\]]*]|\([^\)]*(?:Server\s*\d+|Backup|HEVC|FHD|HD|4K|UHD)[^\)]*\))\s*$""",
        RegexOption.IGNORE_CASE
    )

    // Matches trailing standalone technical tokens at the end of a title (e.g. "FHD HEVC", "4K UHD", "4K", "FHD", "HD", "HEVC", "H265")
    private val TRAILING_TECH_TOKENS_REGEX = Regex(
        """(?:\s+(?:4K|UHD|2160P?|FHD|1080[PI]?|HD|720P?|HEVC|H\.?265|H\.?264|RAW|50FPS|60FPS))+\s*$""",
        RegexOption.IGNORE_CASE
    )

    private val QUALITY_4K_REGEX = Regex("""\b(?:4K|UHD|2160P?)\b""", RegexOption.IGNORE_CASE)
    private val QUALITY_FHD_REGEX = Regex("""\b(?:FHD|1080[PI]?)\b""", RegexOption.IGNORE_CASE)
    private val QUALITY_HD_REGEX = Regex("""\b(?:HD|720P?)\b""", RegexOption.IGNORE_CASE)

    private val BEIN_BRAND_REGEX = Regex("""\bbein\s+sports?\b""", RegexOption.IGNORE_CASE)

    /**
     * Strips leading country/provider prefixes (e.g., `ES|`, `AR |`, `[01]`, `FR┃`, `┃FR┃`) and trailing
     * technical codec/resolution tags while preserving the clean human-readable title.
     */
    fun cleanDisplayTitle(rawName: String): String {
        val trimmed = rawName.trim()
        if (trimmed.isEmpty()) return trimmed

        var working = trimmed

        // 1. Repeatedly strip leading bracketed/bar tags and pipe/colon prefixes (up to 3 layers, e.g. "[01] AR | VIP |")
        repeat(3) {
            var changed = false

            val bracketOrBarMatch = LEADING_BRACKET_OR_BAR_TAG_REGEX.find(working)
            if (bracketOrBarMatch != null) {
                val remainder = working.substring(bracketOrBarMatch.range.last + 1).trim()
                if (remainder.isNotEmpty()) {
                    working = remainder
                    changed = true
                }
            }

            val pipeMatch = LEADING_PIPE_OR_DASH_PREFIX_REGEX.find(working)
            if (pipeMatch != null) {
                val remainder = working.substring(pipeMatch.range.last + 1).trim()
                if (remainder.isNotEmpty()) {
                    working = remainder
                    changed = true
                }
            }

            val colonMatch = LEADING_COLON_PREFIX_REGEX.find(working)
            if (colonMatch != null) {
                val codeCandidate = colonMatch.groupValues[1].uppercase()
                if (codeCandidate in KNOWN_REGION_CODES) {
                    val remainder = working.substring(colonMatch.range.last + 1).trim()
                    if (remainder.isNotEmpty()) {
                        working = remainder
                        changed = true
                    }
                }
            }

            if (!changed) return@repeat
        }

        // 2. Strip trailing technical brackets like "[MP4A-LATM]", "[4K UHD]", "(Server 1)"
        repeat(2) {
            val next = TRAILING_TECH_BRACKET_REGEX.replaceFirst(working, "").trim()
            if (next.isNotEmpty() && next != working) {
                working = next
            }
        }

        // 3. Strip trailing technical resolution/codec tokens like "FHD HEVC", "4K", "HD"
        val withoutTechTokens = TRAILING_TECH_TOKENS_REGEX.replaceFirst(working, "").trim()
        if (withoutTechTokens.isNotEmpty()) {
            working = withoutTechTokens
        }

        // 4. Normalize "BEIN SPORTS" casing to "beIN Sports" for luxury broadcast polish
        if (BEIN_BRAND_REGEX.containsMatchIn(working)) {
            working = BEIN_BRAND_REGEX.replace(working) { m ->
                val text = m.value.lowercase()
                if (text.endsWith("s")) "beIN Sports" else "beIN Sport"
            }
        }

        return working.ifBlank { trimmed }
    }

    /**
     * Extracts the region, language, or bouquet code prefix from a raw stream or category name
     * (e.g. `"ES"`, `"AR"`, `"FR"`, `"EN"`, `"MA"`, `"ALB"`, `"AF"`, `"24/7"`), or `null` if none is present.
     */
    fun extractRegionOrLangBadge(rawName: String): String? {
        var working = rawName.trim()
        if (working.isEmpty()) return null

        repeat(3) {
            // 1. Check leading bracket "[AF]" or "[01]"
            val bracketMatch = Regex("""^\s*\[([^\]]{1,10})]\s*""").find(working)
            if (bracketMatch != null) {
                val inside = bracketMatch.groupValues[1].trim().uppercase()
                if (inside.any { it.isLetter() } && inside !in setOf("HEVC", "H265", "H264", "FHD", "UHD", "4K", "HD", "VIP")) {
                    return normalizeRegionCode(inside)
                }
                working = working.substring(bracketMatch.range.last + 1).trim()
                return@repeat
            }

            // 2. Check leading bar-enclosed tag "┃FR┃" or "|AR|"
            val barMatch = Regex("""^\s*[|┃¦]([A-Za-z0-9/+\-_ ]{2,8})[|┃¦]\s*""").find(working)
            if (barMatch != null) {
                val inside = barMatch.groupValues[1].trim().uppercase()
                if (inside.any { it.isLetter() } && inside !in setOf("HEVC", "H265", "H264", "FHD", "UHD", "4K", "HD", "VIP")) {
                    return normalizeRegionCode(inside)
                }
                working = working.substring(barMatch.range.last + 1).trim()
                return@repeat
            }

            // 3. Check leading pipe/dash prefix "ES|", "AR |", "FR┃", "24/7 |"
            val pipeMatch = LEADING_PIPE_OR_DASH_PREFIX_REGEX.find(working)
            if (pipeMatch != null) {
                val candidate = pipeMatch.groupValues[1].trim().uppercase()
                if (candidate == "VIP") {
                    working = working.substring(pipeMatch.range.last + 1).trim()
                    return@repeat
                }
                if (candidate.isNotEmpty() && !candidate.all { it.isDigit() } &&
                    candidate !in setOf("HEVC", "H265", "H264", "FHD", "UHD", "4K", "HD")
                ) {
                    return normalizeRegionCode(candidate)
                }
            }

            // 4. Check leading colon prefix "AR: ", "MA: ", "KSA: "
            val colonMatch = LEADING_COLON_PREFIX_REGEX.find(working)
            if (colonMatch != null) {
                val candidate = colonMatch.groupValues[1].trim().uppercase()
                if (candidate in KNOWN_REGION_CODES && candidate != "VIP") {
                    return normalizeRegionCode(candidate)
                }
            }
        }

        return null
    }

    private fun normalizeRegionCode(rawCode: String): String {
        return when (rawCode.uppercase()) {
            "ARABE", "ARABIC", "ARA", "ARAB" -> "AR"
            "ENG", "ENGLISH" -> "EN"
            "FRE", "FRA", "FRENCH" -> "FR"
            "SPA", "ESP", "SPANISH" -> "ES"
            "GER", "GERMAN", "DEU" -> "DE"
            "ITA", "ITALIAN" -> "IT"
            "POR", "PORTUGUESE" -> "PT"
            "TUR", "TURKISH" -> "TR"
            "MOR", "MAR", "MOROCCO" -> "MA"
            else -> rawCode.uppercase()
        }
    }

    /**
     * Extracts an authentic resolution badge from the stream title:
     * - `"4K UHD"` only if the name contains `4K`, `UHD`, or `2160`
     * - `"1080p FHD"` if the name contains `FHD` or `1080`
     * - `"HD"` if the name contains `HD` or `720`
     * - `null` otherwise (never fabricates a resolution badge when none is in the title).
     */
    fun extractQualityBadge(rawName: String): String? {
        if (rawName.isBlank()) return null
        return when {
            QUALITY_4K_REGEX.containsMatchIn(rawName) -> "4K UHD"
            QUALITY_FHD_REGEX.containsMatchIn(rawName) -> "1080p FHD"
            QUALITY_HD_REGEX.containsMatchIn(rawName) -> "HD"
            else -> null
        }
    }

    /**
     * Extracts the language or region prefix tag (`"AR"`, `"FR"`, `"EN"`, `"ES"`, `"MA"`, `"TR"`, etc.)
     * when present in [rawName], or `null` if none is present.
     */
    fun extractLanguageBadge(rawName: String): String? = extractRegionOrLangBadge(rawName)

    /**
     * Short compact version of the quality badge for tight poster corners (`"4K"`, `"FHD"`, `"HD"`).
     */
    fun extractCompactQualityBadge(rawName: String): String? {
        return when (extractQualityBadge(rawName)) {
            "4K UHD" -> "4K"
            "1080p FHD" -> "FHD"
            "HD" -> "HD"
            else -> null
        }
    }
}
