package com.example.data.repository

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

enum class BroadcastMacroRegion(val displayName: String) {
    ALL("All Zones"),
    MENA("MENA & Arab World"),
    EUROPE("Europe"),
    AMERICAS("Americas");

    val displayTitle: String get() = displayName
}

/**
 * Controls how seasonal Daylight Saving Time (Summer vs. Winter Time) is applied
 * for countries that observe two different times during the year:
 * - [AUTO]: Automatically selects Winter or Summer time based on the calendar date & timezone rules.
 * - [WINTER]: Explicitly applies the country's Winter / Standard time offset (e.g. CET GMT+1, UK GMT+0, Morocco GMT+0 Ramadan/Winter, Egypt GMT+2).
 * - [SUMMER]: Explicitly applies the country's Summer / Daylight Saving time offset (e.g. CEST GMT+2, UK BST GMT+1, Morocco GMT+1, Egypt EEST GMT+3).
 */
enum class SeasonTimeMode(
    val code: String,
    val shortLabel: String,
    val iconEmoji: String,
    val description: String
) {
    AUTO(
        code = "AUTO",
        shortLabel = "Auto Season",
        iconEmoji = "⚡",
        description = "Automatically switches between Winter & Summer time by calendar date"
    ),
    WINTER(
        code = "WINTER",
        shortLabel = "Winter Time",
        iconEmoji = "❄️",
        description = "Standard / Winter clock (e.g. CET GMT+1, UK GMT+0, Morocco GMT+0)"
    ),
    SUMMER(
        code = "SUMMER",
        shortLabel = "Summer Time",
        iconEmoji = "☀️",
        description = "Daylight Saving / Summer clock (e.g. CEST GMT+2, UK BST GMT+1, Morocco GMT+1)"
    );

    companion object {
        fun fromCode(code: String?): SeasonTimeMode {
            return entries.firstOrNull { it.code.equals(code?.trim(), ignoreCase = true) } ?: AUTO
        }
    }
}

/**
 * Represents a Country Broadcast & Timezone Zone in SetPlay KickOff Arena ("User's Zone").
 * Controls:
 * 1. Local Timezone & Seasonal UTC Offset (Winter Time vs. Summer Time) across all 5 matchday tabs and live clocks.
 * 2. Official Domestic & International TV Rights Holders per competition.
 * 3. IPTV Category Prefix hints (e.g. `MA|`, `AR|`, `FR|`, `UK|`, `ES|`) & Satellite transponders.
 * 4. Suggested local clubs and spotlight domestic league for quick onboarding.
 */
data class UserCountryZone(
    val countryCode: String,
    val countryName: String,
    val shortIso3: String,
    val flagEmoji: String,
    val regionSubtitle: String,
    val macroRegion: BroadcastMacroRegion,
    val timeZoneId: String,
    val fixedOrStandardUtcOffsetLabel: String,
    val hasSeasonalTimeChange: Boolean = false,
    val winterUtcOffsetMinutes: Int = 60,
    val summerUtcOffsetMinutes: Int = 60,
    val winterTimeCode: String = "GMT+1",
    val summerTimeCode: String = "GMT+1",
    val iptvCategoryPrefixes: List<String>,
    val satelliteOrPlatformInfo: String,
    val commentaryLanguageLabel: String,
    val primaryBroadcastersSummary: String,
    val spotlightTournamentId: String,
    val sportsDbCountryMatchers: Set<String>,
    val suggestedClubIds: List<String> = emptyList()
) {
    val nativeOrRegionSubtitle: String
        get() = regionSubtitle

    val primaryTvProviderName: String
        get() = primaryBroadcastersSummary.split("•").firstOrNull()?.trim() ?: primaryBroadcastersSummary

    val secondaryTvProviders: List<String>
        get() = primaryBroadcastersSummary.split("•").drop(1).map { it.trim() }.filter { it.isNotEmpty() }

    val satelliteOrPlatformSummary: String
        get() = satelliteOrPlatformInfo

    val popularLocalClubs: List<String>
        get() = when (countryCode) {
            "MA" -> listOf("Raja Casablanca", "Wydad AC", "RS Berkane")
            "SA" -> listOf("Al-Hilal", "Al-Nassr", "Al-Ittihad")
            "AE" -> listOf("Al-Ain", "Al-Wasl", "Shabab Al-Ahli")
            "EG" -> listOf("Al Ahly", "Zamalek", "Pyramids FC")
            "DZ" -> listOf("MC Alger", "CR Belouizdad", "USM Alger")
            "TN" -> listOf("Espérance de Tunis", "Club Africain", "Étoile du Sahel")
            "QA" -> listOf("Al-Sadd", "Al-Duhail", "Al-Rayyan")
            "KW" -> listOf("Kuwait SC", "Qadsia SC", "Al-Arabi")
            "IQ" -> listOf("Al-Shorta", "Al-Quwa Al-Jawiya", "Al-Zawraa")
            "FR" -> listOf("Paris Saint-Germain", "Marseille", "Monaco")
            "ES" -> listOf("Real Madrid", "FC Barcelona", "Atlético Madrid")
            "GB" -> listOf("Arsenal", "Liverpool", "Manchester City")
            "DE" -> listOf("Bayern Munich", "Borussia Dortmund", "Bayer Leverkusen")
            "IT" -> listOf("Inter Milan", "AC Milan", "Juventus")
            "PT" -> listOf("Sporting CP", "Benfica", "FC Porto")
            "NL" -> listOf("Ajax", "PSV Eindhoven", "Feyenoord")
            "BE" -> listOf("Club Brugge", "Anderlecht", "Union SG")
            "CH" -> listOf("Young Boys", "FC Basel", "FC Zürich")
            "TR" -> listOf("Galatasaray", "Fenerbahce", "Besiktas")
            "US" -> listOf("Inter Miami", "LA Galaxy", "Real Madrid")
            "CA" -> listOf("Toronto FC", "CF Montréal", "Vancouver Whitecaps")
            "BR" -> listOf("Flamengo", "Palmeiras", "Real Madrid")
            else -> listOf("Real Madrid", "FC Barcelona", "Liverpool")
        }

    /**
     * Determines whether Summer / Daylight Saving Time is active at [epochMs] under [mode].
     */
    fun isSummerTimeActive(
        mode: SeasonTimeMode = SeasonTimeMode.AUTO,
        epochMs: Long = System.currentTimeMillis()
    ): Boolean {
        if (!hasSeasonalTimeChange) return false
        return when (mode) {
            SeasonTimeMode.WINTER -> false
            SeasonTimeMode.SUMMER -> true
            SeasonTimeMode.AUTO -> {
                when (countryCode) {
                    "MA" -> {
                        // Morocco uses GMT+1 (Summer/Standard) year-round except during Ramadan (GMT+0)
                        true
                    }
                    "EG" -> {
                        // Egypt observes DST (GMT+3 EEST) from the last Friday of April to the last Thursday of October,
                        // and Winter Time (GMT+2 EET) from November through April.
                        val tz = TimeZone.getTimeZone(timeZoneId)
                        if (tz.useDaylightTime()) {
                            tz.inDaylightTime(Date(epochMs))
                        } else {
                            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = epochMs
                            }
                            val month = cal.get(Calendar.MONTH) // 0 = Jan .. 11 = Dec
                            month in Calendar.MAY..Calendar.OCTOBER
                        }
                    }
                    else -> {
                        val tz = TimeZone.getTimeZone(timeZoneId)
                        tz.inDaylightTime(Date(epochMs))
                    }
                }
            }
        }
    }

    /**
     * Returns the effective UTC offset in minutes (e.g., +60 for GMT+1, +120 for GMT+2, 0 for GMT+0, -300 for GMT-5)
     * taking into account both the country's Winter/Summer rules and the user's [SeasonTimeMode].
     */
    fun getEffectiveOffsetMinutes(
        mode: SeasonTimeMode = SeasonTimeMode.AUTO,
        epochMs: Long = System.currentTimeMillis()
    ): Int {
        if (!hasSeasonalTimeChange) {
            return winterUtcOffsetMinutes
        }
        return if (isSummerTimeActive(mode, epochMs)) {
            summerUtcOffsetMinutes
        } else {
            winterUtcOffsetMinutes
        }
    }

    /**
     * Formats any offset in minutes as a canonical `GMT` badge (e.g. `"GMT+1"`, `"GMT+2"`, `"GMT+0"`, `"GMT-5"`).
     */
    fun formatOffsetMinutesToGmtBadge(totalMinutes: Int): String {
        val hours = totalMinutes / 60
        val mins = abs(totalMinutes % 60)
        return when {
            hours == 0 && mins == 0 -> "GMT+0"
            mins == 0 && hours > 0 -> "GMT+$hours"
            mins == 0 && hours < 0 -> "GMT$hours"
            hours > 0 -> String.format(Locale.US, "GMT+%d:%02d", hours, mins)
            else -> String.format(Locale.US, "GMT%d:%02d", hours, mins)
        }
    }

    /**
     * Returns the formatted UTC/GMT offset badge (e.g. "GMT+1", "GMT+2", "GMT+0", "GMT-5")
     * for the country's timezone and active [SeasonTimeMode].
     */
    fun getUtcOffsetBadge(
        epochMs: Long = System.currentTimeMillis(),
        mode: SeasonTimeMode = SeasonTimeMode.AUTO
    ): String {
        val totalMinutes = getEffectiveOffsetMinutes(mode, epochMs)
        return formatOffsetMinutesToGmtBadge(totalMinutes)
    }

    val winterGmtBadge: String
        get() = formatOffsetMinutesToGmtBadge(winterUtcOffsetMinutes)

    val summerGmtBadge: String
        get() = formatOffsetMinutesToGmtBadge(summerUtcOffsetMinutes)

    /**
     * Returns a clear summary of both seasonal clocks for this country
     * (e.g., `"❄️ Winter GMT+1 (CET) • ☀️ Summer GMT+2 (CEST)"` or `"Permanent GMT+3 (Year-Round)"`).
     */
    fun getDualSeasonBadgeSummary(): String {
        return if (hasSeasonalTimeChange) {
            if (countryCode == "MA") {
                "☀️ Standard $summerGmtBadge • ❄️ Ramadan/Winter $winterGmtBadge"
            } else {
                "❄️ Winter $winterGmtBadge ($winterTimeCode) • ☀️ Summer $summerGmtBadge ($summerTimeCode)"
            }
        } else {
            "⏱️ Permanent $winterGmtBadge ($winterTimeCode • No Seasonal Shift)"
        }
    }

    /**
     * Returns a human-readable badge for the currently active season at [epochMs] under [mode].
     */
    fun getActiveSeasonLabel(
        mode: SeasonTimeMode = SeasonTimeMode.AUTO,
        epochMs: Long = System.currentTimeMillis()
    ): String {
        if (!hasSeasonalTimeChange) {
            return "Permanent $winterGmtBadge"
        }
        val isSummer = isSummerTimeActive(mode, epochMs)
        val modeSuffix = if (mode == SeasonTimeMode.AUTO) "Auto" else "Manual"
        return if (isSummer) {
            if (countryCode == "MA") {
                "☀️ Standard/Summer $summerGmtBadge ($modeSuffix)"
            } else {
                "☀️ Summer Time • $summerTimeCode ($summerGmtBadge)"
            }
        } else {
            if (countryCode == "MA") {
                "❄️ Winter/Ramadan $winterGmtBadge ($modeSuffix)"
            } else {
                "❄️ Winter Time • $winterTimeCode ($winterGmtBadge)"
            }
        }
    }

    /**
     * Formats the current local time in this country's timezone (e.g. "18:45"),
     * honoring the active [SeasonTimeMode] (Auto, Winter, or Summer).
     */
    fun formatCurrentLocalClock(
        epochMs: Long = System.currentTimeMillis(),
        mode: SeasonTimeMode = SeasonTimeMode.AUTO
    ): String {
        val offsetMins = getEffectiveOffsetMinutes(mode, epochMs)
        val customTz = buildCustomGmtTimeZone(offsetMins)
        return SimpleDateFormat("HH:mm", Locale.US).apply {
            timeZone = customTz
        }.format(Date(epochMs))
    }

    /**
     * Returns the exact GMT timezone ID (e.g., `"GMT+00:00"`, `"GMT+01:00"`, `"GMT+03:00"`, `"GMT-05:00"`)
     * for this Country Zone according to seasonal time preference ([isSummer]).
     */
    fun getTimeZoneId(isSummer: Boolean): String {
        val totalOffsetMinutes = if (hasSeasonalTimeChange) {
            if (isSummer) summerUtcOffsetMinutes else winterUtcOffsetMinutes
        } else {
            winterUtcOffsetMinutes
        }
        val sign = if (totalOffsetMinutes >= 0) "+" else "-"
        val absMins = abs(totalOffsetMinutes)
        val hours = absMins / 60
        val mins = absMins % 60
        return String.format(Locale.US, "GMT%s%02d:%02d", sign, hours, mins)
    }

    /**
     * Builds a deterministic [TimeZone] matching the effective offset under [mode] at [epochMs].
     */
    fun getEffectiveTimeZone(
        mode: SeasonTimeMode = SeasonTimeMode.AUTO,
        epochMs: Long = System.currentTimeMillis()
    ): TimeZone {
        val isSummer = isSummerTimeActive(mode, epochMs)
        return TimeZone.getTimeZone(getTimeZoneId(isSummer))
    }

    private fun buildCustomGmtTimeZone(totalOffsetMinutes: Int): TimeZone {
        val sign = if (totalOffsetMinutes >= 0) "+" else "-"
        val absMins = abs(totalOffsetMinutes)
        val hours = absMins / 60
        val mins = absMins % 60
        return TimeZone.getTimeZone(String.format(Locale.US, "GMT%s%02d:%02d", sign, hours, mins))
    }

    /**
     * Converts a kickoff time formatted in Morocco reference timezone (`GMT+1`, i.e., UTC+60m, e.g. `"20:00"`)
     * into this Country Zone's local `HH:mm` time, respecting Winter vs. Summer time ([mode] and [epochMs]).
     */
    fun convertMoroccoTimeToZoneTime(
        moroccoTimeHHmm: String,
        mode: SeasonTimeMode = SeasonTimeMode.AUTO,
        epochMs: Long = System.currentTimeMillis()
    ): String {
        val cleaned = moroccoTimeHHmm.trim()
        val parts = cleaned.split(":")
        if (parts.size < 2) return cleaned
        val hour = parts[0].filter { it.isDigit() }.toIntOrNull() ?: return cleaned
        val minute = parts[1].take(2).filter { it.isDigit() }.toIntOrNull() ?: return cleaned

        val referenceMoroccoOffsetMinutes = 60 // Reference time is always GMT+1 (+60 mins)
        val targetOffsetMinutes = getEffectiveOffsetMinutes(mode = mode, epochMs = epochMs)
        val diffMinutes = targetOffsetMinutes - referenceMoroccoOffsetMinutes

        if (diffMinutes == 0) {
            return String.format(Locale.US, "%02d:%02d", hour, minute)
        }

        val totalMinutes = ((hour * 60 + minute + diffMinutes) % (24 * 60) + (24 * 60)) % (24 * 60)
        val targetHour = totalMinutes / 60
        val targetMin = totalMinutes % 60
        return String.format(Locale.US, "%02d:%02d", targetHour, targetMin)
    }
}

fun formatDynamicKickoffTime(kickoffEpochMs: Long, zone: UserCountryZone, isSummer: Boolean): String {
    val mode = if (isSummer) SeasonTimeMode.SUMMER else SeasonTimeMode.WINTER
    val offsetMins = zone.getEffectiveOffsetMinutes(mode, kickoffEpochMs)
    val sign = if (offsetMins >= 0) "+" else "-"
    val absMins = kotlin.math.abs(offsetMins)
    val hours = absMins / 60
    val mins = absMins % 60
    val tz = TimeZone.getTimeZone(String.format(Locale.US, "GMT%s%02d:%02d", sign, hours, mins))
    return SimpleDateFormat("HH:mm", Locale.US).apply { timeZone = tz }.format(Date(kickoffEpochMs))
}

object UserZoneRegistry {
    const val DEFAULT_COUNTRY_CODE = "MA"

    val ALL_COUNTRY_ZONES: List<UserCountryZone> = listOf(
        // ==================== MENA & ARAB WORLD ====================
        UserCountryZone(
            countryCode = "MA",
            countryName = "Morocco",
            shortIso3 = "MAR",
            flagEmoji = "🇲🇦",
            regionSubtitle = "North Africa • MENA Broadcast Zone",
            macroRegion = BroadcastMacroRegion.MENA,
            timeZoneId = "Africa/Casablanca",
            fixedOrStandardUtcOffsetLabel = "GMT+1",
            hasSeasonalTimeChange = true,
            winterUtcOffsetMinutes = 0,
            summerUtcOffsetMinutes = 60,
            winterTimeCode = "WET",
            summerTimeCode = "GMT+1",
            iptvCategoryPrefixes = listOf("MA|", "AR|", "MENA|", "VIP AR|"),
            satelliteOrPlatformInfo = "Es'hailSat 25.5°E • Nilesat 7°W • SNRT TNT",
            commentaryLanguageLabel = "Arabic / French / English Commentary",
            primaryBroadcastersSummary = "beIN Sports MENA • Arryadia TNT • AD Sports • SSC",
            spotlightTournamentId = "botola_pro",
            sportsDbCountryMatchers = setOf("morocco", "mena", "middle east", "arab", "north africa"),
            suggestedClubIds = listOf("team_raja", "team_wydad", "team_far", "team_berkane", "nat_morocco", "team_realmadrid", "team_barcelona")
        ),
        UserCountryZone(
            countryCode = "SA",
            countryName = "Saudi Arabia",
            shortIso3 = "KSA",
            flagEmoji = "🇸🇦",
            regionSubtitle = "Arabian Gulf • MENA Broadcast Zone",
            macroRegion = BroadcastMacroRegion.MENA,
            timeZoneId = "Asia/Riyadh",
            fixedOrStandardUtcOffsetLabel = "GMT+3",
            hasSeasonalTimeChange = false,
            winterUtcOffsetMinutes = 180,
            summerUtcOffsetMinutes = 180,
            winterTimeCode = "AST",
            summerTimeCode = "AST",
            iptvCategoryPrefixes = listOf("KSA|", "SSC|", "AR|", "MENA|"),
            satelliteOrPlatformInfo = "Arabsat Badr 26°E • Es'hailSat 25.5°E • Shahid VIP",
            commentaryLanguageLabel = "Arabic Commentary • KSA / MENA Feed",
            primaryBroadcastersSummary = "SSC Sports • beIN Sports MENA • Shahid VIP • AD Sports",
            spotlightTournamentId = "saudi_pro",
            sportsDbCountryMatchers = setOf("saudi arabia", "saudi", "mena", "middle east", "arab"),
            suggestedClubIds = listOf("team_alhilal", "team_alnassr", "team_alittihad", "team_alahli_ksa", "team_realmadrid", "team_mancity")
        ),
        UserCountryZone(
            countryCode = "AE",
            countryName = "United Arab Emirates",
            shortIso3 = "UAE",
            flagEmoji = "🇦🇪",
            regionSubtitle = "Arabian Gulf • MENA Broadcast Zone",
            macroRegion = BroadcastMacroRegion.MENA,
            timeZoneId = "Asia/Dubai",
            fixedOrStandardUtcOffsetLabel = "GMT+4",
            hasSeasonalTimeChange = false,
            winterUtcOffsetMinutes = 240,
            summerUtcOffsetMinutes = 240,
            winterTimeCode = "GST",
            summerTimeCode = "GST",
            iptvCategoryPrefixes = listOf("AE|", "UAE|", "AD|", "AR|"),
            satelliteOrPlatformInfo = "Yahsat 52.5°E • Nilesat 7°W • STARZPLAY",
            commentaryLanguageLabel = "Arabic / English Commentary • UAE / MENA Feed",
            primaryBroadcastersSummary = "AD Sports Premium • STARZPLAY • beIN Sports MENA • Dubai Sports",
            spotlightTournamentId = "serie_a",
            sportsDbCountryMatchers = setOf("united arab emirates", "uae", "mena", "middle east", "arab"),
            suggestedClubIds = listOf("team_mancity", "team_realmadrid", "team_inter", "team_acmilan", "team_alhilal")
        ),
        UserCountryZone(
            countryCode = "EG",
            countryName = "Egypt",
            shortIso3 = "EGY",
            flagEmoji = "🇪🇬",
            regionSubtitle = "North Africa • MENA Broadcast Zone",
            macroRegion = BroadcastMacroRegion.MENA,
            timeZoneId = "Africa/Cairo",
            fixedOrStandardUtcOffsetLabel = "GMT+2",
            hasSeasonalTimeChange = true,
            winterUtcOffsetMinutes = 120,
            summerUtcOffsetMinutes = 180,
            winterTimeCode = "EET",
            summerTimeCode = "EEST",
            iptvCategoryPrefixes = listOf("EG|", "AR|", "ONTIME|", "MENA|"),
            satelliteOrPlatformInfo = "Nilesat 7°W • Es'hailSat 25.5°E • ON Time Terrestrial",
            commentaryLanguageLabel = "Arabic Commentary • Cairo / MENA Feed",
            primaryBroadcastersSummary = "beIN Sports MENA • ON Time Sports • AD Sports • SSC",
            spotlightTournamentId = "caf_cl",
            sportsDbCountryMatchers = setOf("egypt", "mena", "middle east", "arab", "north africa"),
            suggestedClubIds = listOf("team_alahly", "team_zamalek", "team_liverpool", "team_realmadrid", "team_mancity")
        ),
        UserCountryZone(
            countryCode = "DZ",
            countryName = "Algeria",
            shortIso3 = "ALG",
            flagEmoji = "🇩🇿",
            regionSubtitle = "Maghreb • MENA Broadcast Zone",
            macroRegion = BroadcastMacroRegion.MENA,
            timeZoneId = "Africa/Algiers",
            fixedOrStandardUtcOffsetLabel = "GMT+1",
            hasSeasonalTimeChange = false,
            winterUtcOffsetMinutes = 60,
            summerUtcOffsetMinutes = 60,
            winterTimeCode = "CET",
            summerTimeCode = "CET",
            iptvCategoryPrefixes = listOf("DZ|", "ALG|", "AR|", "MENA|"),
            satelliteOrPlatformInfo = "Alcomsat-1 24.8°W • Nilesat 7°W • Es'hailSat 25.5°E",
            commentaryLanguageLabel = "Arabic / French Commentary • Maghreb / MENA Feed",
            primaryBroadcastersSummary = "beIN Sports MENA • ENTV Algérie • TV6 Youth • AD Sports",
            spotlightTournamentId = "caf_cl",
            sportsDbCountryMatchers = setOf("algeria", "mena", "middle east", "arab", "north africa"),
            suggestedClubIds = listOf("team_realmadrid", "team_barcelona", "team_mancity", "team_psg", "team_acmilan")
        ),
        UserCountryZone(
            countryCode = "TN",
            countryName = "Tunisia",
            shortIso3 = "TUN",
            flagEmoji = "🇹🇳",
            regionSubtitle = "Maghreb • MENA Broadcast Zone",
            macroRegion = BroadcastMacroRegion.MENA,
            timeZoneId = "Africa/Tunis",
            fixedOrStandardUtcOffsetLabel = "GMT+1",
            hasSeasonalTimeChange = false,
            winterUtcOffsetMinutes = 60,
            summerUtcOffsetMinutes = 60,
            winterTimeCode = "CET",
            summerTimeCode = "CET",
            iptvCategoryPrefixes = listOf("TN|", "TUN|", "AR|", "MENA|"),
            satelliteOrPlatformInfo = "Nilesat 7°W • Es'hailSat 25.5°E • Watania TNT",
            commentaryLanguageLabel = "Arabic / French Commentary • Tunis / MENA Feed",
            primaryBroadcastersSummary = "beIN Sports MENA • El Watania 1/2 • AD Sports • SSC",
            spotlightTournamentId = "caf_cl",
            sportsDbCountryMatchers = setOf("tunisia", "mena", "middle east", "arab", "north africa"),
            suggestedClubIds = listOf("team_esperance", "team_realmadrid", "team_psg", "team_barcelona")
        ),
        UserCountryZone(
            countryCode = "QA",
            countryName = "Qatar",
            shortIso3 = "QAT",
            flagEmoji = "🇶🇦",
            regionSubtitle = "Arabian Gulf • Flagship beIN Hub",
            macroRegion = BroadcastMacroRegion.MENA,
            timeZoneId = "Asia/Qatar",
            fixedOrStandardUtcOffsetLabel = "GMT+3",
            hasSeasonalTimeChange = false,
            winterUtcOffsetMinutes = 180,
            summerUtcOffsetMinutes = 180,
            winterTimeCode = "AST",
            summerTimeCode = "AST",
            iptvCategoryPrefixes = listOf("QA|", "AR|", "BEIN|", "ALKASS|"),
            satelliteOrPlatformInfo = "Es'hailSat 25.5°E / 26°E • TOD 4K",
            commentaryLanguageLabel = "Arabic / English / French Commentary • Doha HQ",
            primaryBroadcastersSummary = "beIN Sports MENA (1-9 HD & 4K) • Alkass One/Two HD • TOD",
            spotlightTournamentId = "uefa_cl",
            sportsDbCountryMatchers = setOf("qatar", "mena", "middle east", "arab"),
            suggestedClubIds = listOf("team_psg", "team_realmadrid", "team_barcelona", "team_mancity", "team_liverpool")
        ),
        UserCountryZone(
            countryCode = "KW",
            countryName = "Kuwait",
            shortIso3 = "KWT",
            flagEmoji = "🇰🇼",
            regionSubtitle = "Arabian Gulf • MENA Broadcast Zone",
            macroRegion = BroadcastMacroRegion.MENA,
            timeZoneId = "Asia/Kuwait",
            fixedOrStandardUtcOffsetLabel = "GMT+3",
            hasSeasonalTimeChange = false,
            winterUtcOffsetMinutes = 180,
            summerUtcOffsetMinutes = 180,
            winterTimeCode = "AST",
            summerTimeCode = "AST",
            iptvCategoryPrefixes = listOf("KW|", "AR|", "MENA|"),
            satelliteOrPlatformInfo = "Es'hailSat 25.5°E • Nilesat 7°W • KTV Sport",
            commentaryLanguageLabel = "Arabic / English Commentary • Gulf / MENA Feed",
            primaryBroadcastersSummary = "beIN Sports MENA • Kuwait Sports HD • SSC • AD Sports",
            spotlightTournamentId = "premier_league",
            sportsDbCountryMatchers = setOf("kuwait", "mena", "middle east", "arab"),
            suggestedClubIds = listOf("team_realmadrid", "team_arsenal", "team_liverpool", "team_mancity", "team_alhilal")
        ),
        UserCountryZone(
            countryCode = "IQ",
            countryName = "Iraq",
            shortIso3 = "IRQ",
            flagEmoji = "🇮🇶",
            regionSubtitle = "Mesopotamia • MENA Broadcast Zone",
            macroRegion = BroadcastMacroRegion.MENA,
            timeZoneId = "Asia/Baghdad",
            fixedOrStandardUtcOffsetLabel = "GMT+3",
            hasSeasonalTimeChange = false,
            winterUtcOffsetMinutes = 180,
            summerUtcOffsetMinutes = 180,
            winterTimeCode = "AST",
            summerTimeCode = "AST",
            iptvCategoryPrefixes = listOf("IQ|", "IRQ|", "AR|", "MENA|"),
            satelliteOrPlatformInfo = "Es'hailSat 25.5°E • Nilesat 7°W • Al-Rabiaa / TOD",
            commentaryLanguageLabel = "Arabic Commentary • Baghdad / MENA Feed",
            primaryBroadcastersSummary = "beIN Sports MENA • Al-Rabiaa Sports • Iraqiya Sports • SSC",
            spotlightTournamentId = "uefa_cl",
            sportsDbCountryMatchers = setOf("iraq", "mena", "middle east", "arab"),
            suggestedClubIds = listOf("team_realmadrid", "team_barcelona", "team_mancity", "team_liverpool", "team_alhilal")
        ),

        // ==================== EUROPE ====================
        UserCountryZone(
            countryCode = "FR",
            countryName = "France",
            shortIso3 = "FRA",
            flagEmoji = "🇫🇷",
            regionSubtitle = "Western Europe • Canal+ & beIN FR Zone",
            macroRegion = BroadcastMacroRegion.EUROPE,
            timeZoneId = "Europe/Paris",
            fixedOrStandardUtcOffsetLabel = "GMT+1",
            hasSeasonalTimeChange = true,
            winterUtcOffsetMinutes = 60,
            summerUtcOffsetMinutes = 120,
            winterTimeCode = "CET",
            summerTimeCode = "CEST",
            iptvCategoryPrefixes = listOf("FR|", "FRANCE|", "VIP FR|"),
            satelliteOrPlatformInfo = "Astra 19.2°E • Canal+ / beIN Sports FR / DAZN FR",
            commentaryLanguageLabel = "French Commentary • VF HD / 4K UHD",
            primaryBroadcastersSummary = "Canal+ Foot • DAZN France • beIN Sports 1-3 France",
            spotlightTournamentId = "ligue_1",
            sportsDbCountryMatchers = setOf("france"),
            suggestedClubIds = listOf("team_psg", "team_marseille", "team_monaco", "team_lyon", "team_lille", "nat_france")
        ),
        UserCountryZone(
            countryCode = "ES",
            countryName = "Spain",
            shortIso3 = "ESP",
            flagEmoji = "🇪🇸",
            regionSubtitle = "Southern Europe • Movistar+ & DAZN ES Zone",
            macroRegion = BroadcastMacroRegion.EUROPE,
            timeZoneId = "Europe/Madrid",
            fixedOrStandardUtcOffsetLabel = "GMT+1",
            hasSeasonalTimeChange = true,
            winterUtcOffsetMinutes = 60,
            summerUtcOffsetMinutes = 120,
            winterTimeCode = "CET",
            summerTimeCode = "CEST",
            iptvCategoryPrefixes = listOf("ES|", "SPAIN|", "ESP|"),
            satelliteOrPlatformInfo = "Astra 19.2°E • Movistar Plus+ / DAZN España",
            commentaryLanguageLabel = "Spanish Commentary • Castellano UHD",
            primaryBroadcastersSummary = "Movistar LaLiga TV • DAZN LaLiga • Movistar Liga de Campeones",
            spotlightTournamentId = "laliga",
            sportsDbCountryMatchers = setOf("spain"),
            suggestedClubIds = listOf("team_realmadrid", "team_barcelona", "team_atletico", "team_realsociedad", "team_villarreal", "nat_spain")
        ),
        UserCountryZone(
            countryCode = "GB",
            countryName = "United Kingdom",
            shortIso3 = "GBR",
            flagEmoji = "🇬🇧",
            regionSubtitle = "British Isles • Sky Sports & TNT Sports Zone",
            macroRegion = BroadcastMacroRegion.EUROPE,
            timeZoneId = "Europe/London",
            fixedOrStandardUtcOffsetLabel = "GMT+0",
            hasSeasonalTimeChange = true,
            winterUtcOffsetMinutes = 0,
            summerUtcOffsetMinutes = 60,
            winterTimeCode = "GMT",
            summerTimeCode = "BST",
            iptvCategoryPrefixes = listOf("UK|", "EN|", "GBR|", "VIP UK|"),
            satelliteOrPlatformInfo = "Astra 28.2°E • Sky UK / TNT Sports / Premier Sports",
            commentaryLanguageLabel = "English Commentary • UK Broadcast Feed",
            primaryBroadcastersSummary = "Sky Sports Premier League • TNT Sports 1-4 • Premier Sports",
            spotlightTournamentId = "premier_league",
            sportsDbCountryMatchers = setOf("united kingdom", "uk", "england", "great britain", "scotland"),
            suggestedClubIds = listOf("team_liverpool", "team_arsenal", "team_mancity", "team_chelsea", "team_manutd", "team_newcastle")
        ),
        UserCountryZone(
            countryCode = "DE",
            countryName = "Germany",
            shortIso3 = "GER",
            flagEmoji = "🇩🇪",
            regionSubtitle = "Central Europe • Sky DE & DAZN DE Zone",
            macroRegion = BroadcastMacroRegion.EUROPE,
            timeZoneId = "Europe/Berlin",
            fixedOrStandardUtcOffsetLabel = "GMT+1",
            hasSeasonalTimeChange = true,
            winterUtcOffsetMinutes = 60,
            summerUtcOffsetMinutes = 120,
            winterTimeCode = "CET",
            summerTimeCode = "CEST",
            iptvCategoryPrefixes = listOf("DE|", "GER|", "DAZN DE|", "SKY DE|"),
            satelliteOrPlatformInfo = "Astra 19.2°E • Sky Deutschland / DAZN Bar HD",
            commentaryLanguageLabel = "German Commentary • Deutsch FHD / UHD",
            primaryBroadcastersSummary = "Sky Sport Bundesliga • DAZN 1/2 Bar HD • RTL Deutschland",
            spotlightTournamentId = "bundesliga",
            sportsDbCountryMatchers = setOf("germany", "austria"),
            suggestedClubIds = listOf("team_bayern", "team_dortmund", "team_leverkusen", "team_leipzig")
        ),
        UserCountryZone(
            countryCode = "IT",
            countryName = "Italy",
            shortIso3 = "ITA",
            flagEmoji = "🇮🇹",
            regionSubtitle = "Southern Europe • DAZN IT & Sky Italia Zone",
            macroRegion = BroadcastMacroRegion.EUROPE,
            timeZoneId = "Europe/Rome",
            fixedOrStandardUtcOffsetLabel = "GMT+1",
            hasSeasonalTimeChange = true,
            winterUtcOffsetMinutes = 60,
            summerUtcOffsetMinutes = 120,
            winterTimeCode = "CET",
            summerTimeCode = "CEST",
            iptvCategoryPrefixes = listOf("IT|", "ITA|", "SKY IT|", "DAZN IT|"),
            satelliteOrPlatformInfo = "HotBird 13°E • Sky Italia / Zona DAZN",
            commentaryLanguageLabel = "Italian Commentary • Italiano FHD / 4K",
            primaryBroadcastersSummary = "DAZN 1 Italia • Sky Sport Uno • Sky Sport Calcio",
            spotlightTournamentId = "serie_a",
            sportsDbCountryMatchers = setOf("italy"),
            suggestedClubIds = listOf("team_inter", "team_acmilan", "team_juventus", "team_napoli", "team_roma")
        ),
        UserCountryZone(
            countryCode = "PT",
            countryName = "Portugal",
            shortIso3 = "POR",
            flagEmoji = "🇵🇹",
            regionSubtitle = "Iberia • Sport TV & DAZN Portugal Zone",
            macroRegion = BroadcastMacroRegion.EUROPE,
            timeZoneId = "Europe/Lisbon",
            fixedOrStandardUtcOffsetLabel = "GMT+0",
            hasSeasonalTimeChange = true,
            winterUtcOffsetMinutes = 0,
            summerUtcOffsetMinutes = 60,
            winterTimeCode = "WET",
            summerTimeCode = "WEST",
            iptvCategoryPrefixes = listOf("PT|", "POR|", "SPORT TV|"),
            satelliteOrPlatformInfo = "Hispasat 30°W • Sport TV / DAZN Eleven Portugal",
            commentaryLanguageLabel = "Portuguese Commentary • PT FHD",
            primaryBroadcastersSummary = "Sport TV 1-5 HD • DAZN 1-3 Portugal • BTV",
            spotlightTournamentId = "uefa_cl",
            sportsDbCountryMatchers = setOf("portugal"),
            suggestedClubIds = listOf("nat_portugal", "team_realmadrid", "team_mancity", "team_psg")
        ),
        UserCountryZone(
            countryCode = "NL",
            countryName = "Netherlands",
            shortIso3 = "NED",
            flagEmoji = "🇳🇱",
            regionSubtitle = "Benelux • Ziggo Sport, ESPN NL & Viaplay",
            macroRegion = BroadcastMacroRegion.EUROPE,
            timeZoneId = "Europe/Amsterdam",
            fixedOrStandardUtcOffsetLabel = "GMT+1",
            hasSeasonalTimeChange = true,
            winterUtcOffsetMinutes = 60,
            summerUtcOffsetMinutes = 120,
            winterTimeCode = "CET",
            summerTimeCode = "CEST",
            iptvCategoryPrefixes = listOf("NL|", "NED|", "ZIGGO|"),
            satelliteOrPlatformInfo = "Astra 23.5°E • Ziggo Sport Totaal / ESPN NL / Viaplay",
            commentaryLanguageLabel = "Dutch Commentary • NL FHD",
            primaryBroadcastersSummary = "Ziggo Sport 1-3 • Viaplay NL • ESPN 1-4 NL",
            spotlightTournamentId = "uefa_cl",
            sportsDbCountryMatchers = setOf("netherlands", "holland"),
            suggestedClubIds = listOf("team_liverpool", "team_barcelona", "team_arsenal", "team_mancity")
        ),
        UserCountryZone(
            countryCode = "BE",
            countryName = "Belgium",
            shortIso3 = "BEL",
            flagEmoji = "🇧🇪",
            regionSubtitle = "Benelux • DAZN Belgium, Play Sports & RTL Zone",
            macroRegion = BroadcastMacroRegion.EUROPE,
            timeZoneId = "Europe/Brussels",
            fixedOrStandardUtcOffsetLabel = "GMT+1",
            hasSeasonalTimeChange = true,
            winterUtcOffsetMinutes = 60,
            summerUtcOffsetMinutes = 120,
            winterTimeCode = "CET",
            summerTimeCode = "CEST",
            iptvCategoryPrefixes = listOf("BE|", "BEL|", "FR|", "NL|"),
            satelliteOrPlatformInfo = "Astra 19.2°E / 23.5°E • DAZN BE / Play Sports / VOO Sport",
            commentaryLanguageLabel = "French & Dutch Commentary • Belgium HD",
            primaryBroadcastersSummary = "DAZN 1 Belgium • Play Sports 1 • RTL Club • VTM 2",
            spotlightTournamentId = "uefa_cl",
            sportsDbCountryMatchers = setOf("belgium"),
            suggestedClubIds = listOf("team_realmadrid", "team_mancity", "team_arsenal", "team_psg")
        ),
        UserCountryZone(
            countryCode = "CH",
            countryName = "Switzerland",
            shortIso3 = "SUI",
            flagEmoji = "🇨🇭",
            regionSubtitle = "Alpine Europe • blue Sport, SRF & RTS Zone",
            macroRegion = BroadcastMacroRegion.EUROPE,
            timeZoneId = "Europe/Zurich",
            fixedOrStandardUtcOffsetLabel = "GMT+1",
            hasSeasonalTimeChange = true,
            winterUtcOffsetMinutes = 60,
            summerUtcOffsetMinutes = 120,
            winterTimeCode = "CET",
            summerTimeCode = "CEST",
            iptvCategoryPrefixes = listOf("CH|", "SUI|", "BLUE|", "DE|", "FR|"),
            satelliteOrPlatformInfo = "HotBird 13°E • Swisscom blue Sport / SRG SSR",
            commentaryLanguageLabel = "German / French / Italian Commentary • Swiss UHD",
            primaryBroadcastersSummary = "blue Sport 1 HD • SRF zwei HD • RTS 2 HD • DAZN",
            spotlightTournamentId = "uefa_cl",
            sportsDbCountryMatchers = setOf("switzerland"),
            suggestedClubIds = listOf("team_bayern", "team_inter", "team_arsenal", "team_realmadrid")
        ),
        UserCountryZone(
            countryCode = "TR",
            countryName = "Turkey",
            shortIso3 = "TUR",
            flagEmoji = "🇹🇷",
            regionSubtitle = "Eurasia • Tabii Spor, beIN TR & S Sport Zone",
            macroRegion = BroadcastMacroRegion.EUROPE,
            timeZoneId = "Europe/Istanbul",
            fixedOrStandardUtcOffsetLabel = "GMT+3",
            hasSeasonalTimeChange = false,
            winterUtcOffsetMinutes = 180,
            summerUtcOffsetMinutes = 180,
            winterTimeCode = "TRT",
            summerTimeCode = "TRT",
            iptvCategoryPrefixes = listOf("TR|", "TUR|", "BEIN TR|"),
            satelliteOrPlatformInfo = "Türksat 42°E • Digiturk beIN TR / Tabii / S Sport",
            commentaryLanguageLabel = "Turkish Commentary • Türkçe HD",
            primaryBroadcastersSummary = "Tabii Spor • TRT 1 HD • beIN Sports Türkiye • S Sport",
            spotlightTournamentId = "uefa_cl",
            sportsDbCountryMatchers = setOf("turkey", "turkiye"),
            suggestedClubIds = listOf("team_realmadrid", "team_inter", "team_liverpool", "team_mancity")
        ),

        // ==================== AMERICAS ====================
        UserCountryZone(
            countryCode = "US",
            countryName = "United States",
            shortIso3 = "USA",
            flagEmoji = "🇺🇸",
            regionSubtitle = "North America • CBS, NBC/USA, ESPN & TUDN Zone",
            macroRegion = BroadcastMacroRegion.AMERICAS,
            timeZoneId = "America/New_York",
            fixedOrStandardUtcOffsetLabel = "GMT-5",
            hasSeasonalTimeChange = true,
            winterUtcOffsetMinutes = -300,
            summerUtcOffsetMinutes = -240,
            winterTimeCode = "EST",
            summerTimeCode = "EDT",
            iptvCategoryPrefixes = listOf("US|", "USA|", "EN|"),
            satelliteOrPlatformInfo = "Paramount+ • Peacock • ESPN+ • DirecTV Sports",
            commentaryLanguageLabel = "English & Spanish Commentary (US / TUDN)",
            primaryBroadcastersSummary = "CBS Sports / Paramount+ • USA Network • ESPN+ • beIN USA",
            spotlightTournamentId = "uefa_cl",
            sportsDbCountryMatchers = setOf("united states", "usa", "us"),
            suggestedClubIds = listOf("team_realmadrid", "team_barcelona", "team_liverpool", "team_arsenal", "team_mancity", "team_acmilan")
        ),
        UserCountryZone(
            countryCode = "CA",
            countryName = "Canada",
            shortIso3 = "CAN",
            flagEmoji = "🇨🇦",
            regionSubtitle = "North America • DAZN CA, fuboTV & TSN Zone",
            macroRegion = BroadcastMacroRegion.AMERICAS,
            timeZoneId = "America/Toronto",
            fixedOrStandardUtcOffsetLabel = "GMT-5",
            hasSeasonalTimeChange = true,
            winterUtcOffsetMinutes = -300,
            summerUtcOffsetMinutes = -240,
            winterTimeCode = "EST",
            summerTimeCode = "EDT",
            iptvCategoryPrefixes = listOf("CA|", "CAN|", "US|"),
            satelliteOrPlatformInfo = "DAZN Canada • fuboTV Canada • TSN / RDS",
            commentaryLanguageLabel = "English & French Commentary • Canada Feed",
            primaryBroadcastersSummary = "DAZN Canada • fuboTV Canada • TSN • beIN Sports CA",
            spotlightTournamentId = "premier_league",
            sportsDbCountryMatchers = setOf("canada"),
            suggestedClubIds = listOf("team_bayern", "team_liverpool", "team_arsenal", "team_realmadrid", "team_lille")
        ),
        UserCountryZone(
            countryCode = "BR",
            countryName = "Brazil",
            shortIso3 = "BRA",
            flagEmoji = "🇧🇷",
            regionSubtitle = "South America • TNT Sports BR & ESPN Brasil Zone",
            macroRegion = BroadcastMacroRegion.AMERICAS,
            timeZoneId = "America/Sao_Paulo",
            fixedOrStandardUtcOffsetLabel = "GMT-3",
            hasSeasonalTimeChange = false,
            winterUtcOffsetMinutes = -180,
            summerUtcOffsetMinutes = -180,
            winterTimeCode = "BRT",
            summerTimeCode = "BRT",
            iptvCategoryPrefixes = listOf("BR|", "BRA|", "PT-BR|"),
            satelliteOrPlatformInfo = "Star One C2 70°W • TNT Sports / ESPN Brasil / SporTV",
            commentaryLanguageLabel = "Brazilian Portuguese Commentary • PT-BR",
            primaryBroadcastersSummary = "TNT Brasil • ESPN Brasil • SporTV • CazéTV",
            spotlightTournamentId = "uefa_cl",
            sportsDbCountryMatchers = setOf("brazil", "brasil"),
            suggestedClubIds = listOf("nat_brazil", "team_realmadrid", "team_barcelona", "team_arsenal", "team_mancity")
        )
    )

    val defaultCountryZone: UserCountryZone
        get() = ALL_COUNTRY_ZONES.first()

    val allCountryZones: List<UserCountryZone>
        get() = ALL_COUNTRY_ZONES

    fun getZonesByMacroRegion(region: BroadcastMacroRegion): List<UserCountryZone> {
        return if (region == BroadcastMacroRegion.ALL) {
            ALL_COUNTRY_ZONES
        } else {
            ALL_COUNTRY_ZONES.filter { it.macroRegion == region }
        }
    }

    fun getZoneByCode(code: String?): UserCountryZone {
        if (code.isNullOrBlank()) return ALL_COUNTRY_ZONES.first()
        return ALL_COUNTRY_ZONES.firstOrNull {
            it.countryCode.equals(code.trim(), ignoreCase = true) ||
                it.shortIso3.equals(code.trim(), ignoreCase = true) ||
                it.countryName.equals(code.trim(), ignoreCase = true)
        } ?: ALL_COUNTRY_ZONES.first()
    }

    /**
     * Auto-detects the closest matching `UserCountryZone` from the Android device's current Locale and TimeZone.
     * Falls back to Morocco (`MA`) if the device locale/timezone is generic UTC or unmapped.
     */
    fun detectDeviceCountryZone(): UserCountryZone {
        val deviceCountry = Locale.getDefault().country.uppercase(Locale.ROOT)
        val byLocale = ALL_COUNTRY_ZONES.firstOrNull { it.countryCode == deviceCountry }
        if (byLocale != null && deviceCountry != "US") {
            return byLocale
        }
        val deviceTz = TimeZone.getDefault().id
        val byTz = ALL_COUNTRY_ZONES.firstOrNull { it.timeZoneId.equals(deviceTz, ignoreCase = true) }
        if (byTz != null) {
            return byTz
        }
        return when {
            deviceTz.startsWith("Africa/Casablanca", true) -> getZoneByCode("MA")
            deviceTz.startsWith("Asia/Riyadh", true) -> getZoneByCode("SA")
            deviceTz.startsWith("Asia/Dubai", true) -> getZoneByCode("AE")
            deviceTz.startsWith("Africa/Cairo", true) -> getZoneByCode("EG")
            deviceTz.startsWith("Africa/Algiers", true) -> getZoneByCode("DZ")
            deviceTz.startsWith("Africa/Tunis", true) -> getZoneByCode("TN")
            deviceTz.startsWith("Asia/Baghdad", true) -> getZoneByCode("IQ")
            deviceTz.startsWith("Europe/Paris", true) -> getZoneByCode("FR")
            deviceTz.startsWith("Europe/Madrid", true) -> getZoneByCode("ES")
            deviceTz.startsWith("Europe/London", true) -> getZoneByCode("GB")
            deviceTz.startsWith("Europe/Berlin", true) -> getZoneByCode("DE")
            deviceTz.startsWith("Europe/Rome", true) -> getZoneByCode("IT")
            deviceTz.startsWith("Europe/Lisbon", true) -> getZoneByCode("PT")
            deviceTz.startsWith("Europe/Amsterdam", true) -> getZoneByCode("NL")
            deviceTz.startsWith("Europe/Brussels", true) -> getZoneByCode("BE")
            deviceTz.startsWith("Europe/Zurich", true) -> getZoneByCode("CH")
            deviceTz.startsWith("Europe/Istanbul", true) -> getZoneByCode("TR")
            deviceTz.startsWith("America/Sao_Paulo", true) -> getZoneByCode("BR")
            deviceTz.startsWith("America/Toronto", true) -> getZoneByCode("CA")
            deviceCountry == "US" && deviceTz.startsWith("America/", true) -> getZoneByCode("US")
            else -> getZoneByCode("MA")
        }
    }

    /**
     * Returns the official TV channels for a given tournament in the specified `UserCountryZone`.
     */
    fun getCountryZoneChannelsForTournament(
        zone: UserCountryZone,
        tournamentId: String,
        tournamentName: String,
        slotIndex: Int,
        isMarquee: Boolean,
        homeTeamName: String,
        awayTeamName: String,
        baseMenaFallbacks: List<String>
    ): List<String> {
        val idx = slotIndex.coerceAtLeast(0)
        val lowerTourney = tournamentName.lowercase(Locale.ROOT)

        // 1. MENA Macro-Region: keep baseMenaFallbacks as backbone, with country-specific domestic enhancements
        if (zone.macroRegion == BroadcastMacroRegion.MENA) {
            return when (zone.countryCode) {
                "SA" -> {
                    when {
                        tournamentId == "saudi_pro" || lowerTourney.contains("saudi") || lowerTourney.contains("roshn") -> {
                            val pool = listOf("SSC 1 HD", "SSC 1 4K", "SSC Extra 1", "SSC Extra 2", "Shahid VIP")
                            val primary = pool[idx % pool.size]
                            (listOf(primary) + pool.filter { it != primary }).distinct()
                        }
                        else -> (baseMenaFallbacks + listOf("TOD 4K")).distinct()
                    }
                }
                "AE" -> {
                    when {
                        tournamentId == "serie_a" || lowerTourney.contains("serie a") -> {
                            val pool = listOf("AD Sports Premium 1", "AD Sports Premium 2", "STARZPLAY", "Abu Dhabi Sports 1 HD")
                            val primary = pool[idx % pool.size]
                            (listOf(primary) + pool.filter { it != primary }).distinct()
                        }
                        else -> (baseMenaFallbacks + listOf("Dubai Sports 1 HD", "AD Sports Premium 1")).distinct()
                    }
                }
                "EG" -> {
                    when {
                        tournamentId == "caf_cl" || lowerTourney.contains("caf") -> {
                            val combinedTeams = "${homeTeamName.lowercase(Locale.ROOT)} ${awayTeamName.lowercase(Locale.ROOT)}"
                            if (combinedTeams.contains("ahly") || combinedTeams.contains("zamalek") || combinedTeams.contains("pyramids")) {
                                (listOf("beIN Sports 6 HD", "ON Time Sports 1 HD", "ON Time Sports 2 HD") + baseMenaFallbacks).distinct()
                            } else {
                                (baseMenaFallbacks + listOf("ON Time Sports 1 HD")).distinct()
                            }
                        }
                        else -> baseMenaFallbacks
                    }
                }
                "DZ" -> {
                    if (tournamentId == "caf_cl") {
                        (baseMenaFallbacks + listOf("ENTV Terrestre", "TV6 Algérie")).distinct()
                    } else {
                        baseMenaFallbacks
                    }
                }
                "TN" -> {
                    if (tournamentId == "caf_cl") {
                        (baseMenaFallbacks + listOf("El Watania 1", "El Watania 2")).distinct()
                    } else {
                        baseMenaFallbacks
                    }
                }
                "QA" -> {
                    (baseMenaFallbacks + listOf("Alkass One HD", "Alkass Two HD", "TOD 4K")).distinct()
                }
                "KW" -> {
                    (baseMenaFallbacks + listOf("Kuwait Sports HD")).distinct()
                }
                "IQ" -> {
                    (baseMenaFallbacks + listOf("Al-Rabiaa Sports HD", "Iraqiya Sports HD")).distinct()
                }
                else -> baseMenaFallbacks // "MA" Morocco default
            }
        }

        // 2. Non-MENA Country Zones (Europe & Americas)
        val regionalPool: List<String> = when (zone.countryCode) {
            "FR" -> when (tournamentId) {
                "uefa_cl", "uefa_el" -> listOf("Canal+ Foot", "Canal+ Sport", "Canal+ Sport 360", "Canal+ Live 1", "Canal+ Live 2")
                "premier_league" -> listOf("Canal+ Foot", "Canal+ Sport 360", "Canal+ Premier League", "Canal+")
                "ligue_1" -> listOf("DAZN 1 France", "beIN Sports 1 France", "DAZN 2 France", "beIN Sports 2 France")
                "laliga", "bundesliga", "serie_a", "caf_cl" -> listOf("beIN Sports 1 France", "beIN Sports 2 France", "beIN Sports 3 France", "beIN Sports Max 4", "beIN Sports Max 5")
                "botola_pro" -> listOf("Arryadia HD", "beIN Sports 2 France", "SNRT Live")
                "saudi_pro" -> listOf("Canal+ Foot", "Canal+ Sport 360", "SSC 1 HD")
                else -> listOf("Canal+ Foot", "beIN Sports 1 France", "DAZN 1 France")
            }

            "ES" -> when (tournamentId) {
                "uefa_cl", "uefa_el" -> listOf("Movistar Liga de Campeones", "Movistar Liga de Campeones 2", "Movistar Liga de Campeones 3", "Movistar Plus+")
                "laliga" -> if (isMarquee) {
                    listOf("Movistar LaLiga TV", "DAZN LaLiga", "Movistar LaLiga TV UHD", "LaLiga TV Bar")
                } else {
                    listOf("DAZN LaLiga", "Movistar LaLiga TV", "Movistar LaLiga TV 2", "LaLiga TV Bar")
                }
                "premier_league", "serie_a", "bundesliga" -> listOf("DAZN 1 España", "DAZN 2 España", "Movistar Plus+")
                "ligue_1" -> listOf("Eurosport 1 España", "Eurosport 2 España", "DAZN 1 España")
                "saudi_pro" -> listOf("Marca.com Live", "DAZN 1 España", "SSC 1 HD")
                "botola_pro" -> listOf("Arryadia HD", "SNRT Live")
                else -> listOf("Movistar Liga de Campeones", "DAZN 1 España")
            }

            "GB" -> when (tournamentId) {
                "uefa_cl", "uefa_el" -> listOf("TNT Sports 1 HD", "TNT Sports 2 HD", "TNT Sports 3 HD", "TNT Sports Ultimate 4K", "Amazon Prime Video UK")
                "premier_league" -> if (isMarquee) {
                    listOf("Sky Sports Main Event", "Sky Sports Premier League", "Sky Sports Ultra HDR", "TNT Sports 1 HD")
                } else {
                    listOf("Sky Sports Premier League", "TNT Sports 1 HD", "Sky Sports Main Event", "Sky Sports Football")
                }
                "laliga", "ligue_1" -> listOf("Premier Sports 1 HD", "Premier Sports 2 HD", "LaLigaTV UK")
                "bundesliga" -> listOf("Sky Sports Football", "Sky Sports Mix", "Sky Sports Main Event")
                "serie_a" -> listOf("TNT Sports 1 HD", "TNT Sports 2 HD", "OneFootball UK")
                "saudi_pro" -> listOf("DAZN UK", "SSC 1 HD")
                else -> listOf("TNT Sports 1 HD", "Sky Sports Football")
            }

            "DE" -> when (tournamentId) {
                "uefa_cl" -> listOf("DAZN 1 Bar HD", "DAZN 2 Bar HD", "Prime Video DE")
                "bundesliga" -> listOf("Sky Sport Bundesliga 1 HD", "Sky Sport Top Event", "DAZN 1 Bar HD", "Sky Sport Bundesliga UHD")
                "premier_league" -> listOf("Sky Sport Premier League", "Sky Sport Top Event", "Sky Sport Mix")
                "laliga", "serie_a", "ligue_1", "saudi_pro" -> listOf("DAZN 1 Bar HD", "DAZN 2 Bar HD", "Sportdigital Fussball")
                "uefa_el" -> listOf("RTL Deutschland", "Sky Sport Mix", "DAZN 1 Bar HD")
                else -> listOf("DAZN 1 Bar HD", "Sky Sport Top Event")
            }

            "IT" -> when (tournamentId) {
                "uefa_cl", "uefa_el" -> listOf("Sky Sport Uno HD", "Sky Sport Arena", "Sky Sport 251", "Sky Sport 4K", "Prime Video IT")
                "serie_a" -> listOf("DAZN 1 Italia", "Sky Sport Calcio", "Sky Sport Uno HD", "Zona DAZN")
                "premier_league", "bundesliga" -> listOf("Sky Sport Uno HD", "Sky Sport Arena", "Sky Sport Max")
                "laliga" -> listOf("DAZN 1 Italia", "Zona DAZN")
                "saudi_pro" -> listOf("Sportitalia HD", "SoloCalcio")
                else -> listOf("Sky Sport Uno HD", "DAZN 1 Italia")
            }

            "PT" -> when (tournamentId) {
                "uefa_cl", "uefa_el", "premier_league", "laliga", "bundesliga" ->
                    listOf("DAZN 1 Portugal", "DAZN 2 Portugal", "DAZN 3 Portugal", "Sport TV 5 HD")
                "serie_a", "ligue_1", "saudi_pro" ->
                    listOf("Sport TV 1 HD", "Sport TV 2 HD", "Sport TV 3 HD")
                else -> listOf("Sport TV 1 HD", "DAZN 1 Portugal")
            }

            "NL" -> when (tournamentId) {
                "uefa_cl", "uefa_el", "laliga", "serie_a", "ligue_1" ->
                    listOf("Ziggo Sport 1", "Ziggo Sport 2", "Ziggo Sport 3", "Ziggo Sport Voetbal")
                "premier_league", "bundesliga" ->
                    listOf("Viaplay NL", "ESPN 1 NL", "Ziggo Sport 1")
                else -> listOf("Ziggo Sport 1", "ESPN 1 NL")
            }

            "BE" -> when (tournamentId) {
                "uefa_cl", "uefa_el" ->
                    listOf("RTL Club HD", "VTM 2 HD", "Play Sports 1", "Proximus Pickx+ Sports")
                "premier_league", "bundesliga" ->
                    listOf("Play Sports 1", "Play Sports 2", "VOO Sport World 1")
                "laliga", "serie_a", "ligue_1" ->
                    listOf("DAZN 1 Belgium", "DAZN 2 Belgium", "Play Sports 1")
                else -> listOf("DAZN 1 Belgium", "Play Sports 1", "RTL Club HD")
            }

            "CH" -> when (tournamentId) {
                "uefa_cl", "uefa_el", "laliga", "serie_a" ->
                    listOf("blue Sport 1 HD", "blue Sport 2 HD", "SRF zwei HD", "RTS 2 HD")
                "bundesliga", "premier_league" ->
                    listOf("Sky Sport CH", "DAZN 1 Bar HD", "blue Sport 1 HD")
                "ligue_1" ->
                    listOf("blue Sport 1 HD", "Canal+ Suisse", "DAZN CH")
                else -> listOf("blue Sport 1 HD", "SRF zwei HD", "RTS 2 HD")
            }

            "TR" -> when (tournamentId) {
                "uefa_cl", "uefa_el" -> listOf("Tabii Spor", "TRT 1 HD", "TRT Spor HD", "Tabii Spor 1")
                "premier_league", "ligue_1", "bundesliga" ->
                    listOf("beIN Sports 3 Türkiye", "beIN Sports 4 Türkiye", "beIN Sports 1 Türkiye", "beIN Sports Max 1 TR")
                "laliga", "serie_a", "saudi_pro" ->
                    listOf("S Sport 1 HD", "S Sport 2 HD", "S Sport Plus")
                else -> listOf("Tabii Spor", "beIN Sports 1 Türkiye", "S Sport 1 HD")
            }

            "US" -> when (tournamentId) {
                "uefa_cl", "uefa_el", "serie_a" ->
                    listOf("CBS Sports Network", "Paramount+ US", "TUDN USA", "Univision")
                "premier_league" ->
                    listOf("USA Network", "NBC Sports", "Peacock", "Telemundo")
                "laliga", "bundesliga" ->
                    listOf("ESPN Deportes", "ESPN+ US", "ABC Sports")
                "ligue_1", "caf_cl" ->
                    listOf("beIN Sports USA", "beIN Sports en Español", "Fanatiz USA")
                "saudi_pro" ->
                    listOf("FS2 (Fox Sports 2)", "Fox Deportes")
                else -> listOf("CBS Sports Network", "ESPN+ US", "beIN Sports USA")
            }

            "CA" -> when (tournamentId) {
                "uefa_cl", "uefa_el", "bundesliga" ->
                    listOf("DAZN 1 Canada", "DAZN 2 Canada")
                "premier_league", "serie_a" ->
                    listOf("fuboTV Canada", "Sportsnet World")
                "laliga" ->
                    listOf("TSN 4", "TSN+", "RDS Info")
                else ->
                    listOf("beIN Sports Canada", "DAZN 1 Canada", "TSN 4")
            }

            "BR" -> when (tournamentId) {
                "uefa_cl" ->
                    listOf("TNT Brasil", "Space Brasil", "Max Brasil", "SBT HD")
                "premier_league", "laliga", "serie_a", "uefa_el" ->
                    listOf("ESPN Brasil", "ESPN 2 Brasil", "ESPN 4 Brasil", "Disney+ BR")
                "bundesliga", "ligue_1", "saudi_pro" ->
                    listOf("SporTV Brasil", "Canal GOAT", "CazéTV")
                else ->
                    listOf("TNT Brasil", "ESPN Brasil", "SporTV Brasil")
            }

            else -> baseMenaFallbacks
        }

        val primary = regionalPool[idx % regionalPool.size]
        val next1 = regionalPool[(idx + 1) % regionalPool.size]
        // Include regional channels first, followed by top MENA beIN fallback so expats with MENA IPTV Favorites can still 1-tap switch
        return (listOf(primary, next1) + regionalPool + baseMenaFallbacks.take(2)).distinct()
    }

    /**
     * Resolves the official provider group name for a channel in the active `UserCountryZone`.
     */
    fun detectProviderNameForChannel(channelLabel: String, zone: UserCountryZone): String {
        val lower = channelLabel.lowercase(Locale.ROOT)
        return when {
            lower.contains("canal+") || lower.contains("canal plus") -> "Canal+ (${zone.countryName})"
            lower.contains("movistar") || lower.contains("laliga tv") -> "Movistar Plus+ (Spain)"
            lower.contains("tnt sport") || lower.contains("tnt brasil") -> "TNT Sports (${zone.shortIso3})"
            lower.contains("sky sport") -> "Sky Sports (${zone.shortIso3})"
            lower.contains("dazn") -> "DAZN (${zone.countryName})"
            lower.contains("premier sport") -> "Premier Sports (UK)"
            lower.contains("sport tv") -> "Sport TV (Portugal)"
            lower.contains("ziggo") -> "Ziggo Sport (NL)"
            lower.contains("viaplay") -> "Viaplay (${zone.shortIso3})"
            lower.contains("play sport") || lower.contains("rtl club") || lower.contains("vtm") -> "Play Sports / RTL (Belgium)"
            lower.contains("blue sport") || lower.contains("srf") || lower.contains("rts") -> "blue Sport / SRG (Switzerland)"
            lower.contains("tabii") || lower.contains("trt") -> "TRT / Tabii Spor (Türkiye)"
            lower.contains("s sport") -> "S Sport (Türkiye)"
            lower.contains("cbs") || lower.contains("paramount") -> "CBS Sports / Paramount+ (USA)"
            lower.contains("usa network") || lower.contains("peacock") || lower.contains("nbc") -> "NBC / USA Network"
            lower.contains("tudn") || lower.contains("univision") -> "TUDN / Univision"
            lower.contains("espn") -> "ESPN (${zone.shortIso3})"
            lower.contains("fubotv") || lower.contains("tsn") -> "fuboTV / TSN (Canada)"
            lower.contains("sportv") -> "SporTV (Brasil)"
            lower.contains("arryadia") || lower.contains("riyadia") || lower.contains("snrt") || lower.contains("al aoula") || lower.contains("tamazight") ->
                "Arryadia / SNRT (Morocco)"
            lower.contains("ssc") || lower.contains("thmanyah") || lower.contains("shahid") ->
                "SSC Sports (KSA / MENA)"
            lower.contains("ad sport") || lower.contains("abu dhabi") || lower.contains("starz") ->
                "AD Sports / STARZPLAY"
            lower.contains("alkass") || lower.contains("al kass") ->
                "Alkass Sports (Qatar / MENA)"
            lower.contains("on time") || lower.contains("ontime") ->
                "ON Time Sports (Egypt)"
            lower.contains("entv") || lower.contains("tv6") ->
                "ENTV Algérie"
            lower.contains("watania") ->
                "El Watania (Tunisia)"
            lower.contains("rabiaa") || lower.contains("iraqiya") ->
                "Al-Rabiaa / Iraqiya (Iraq)"
            lower.contains("kuwait sport") ->
                "Kuwait Sports"
            lower.contains("dubai sport") ->
                "Dubai Sports (UAE)"
            lower.contains("bein") && lower.contains("france") ->
                "beIN Sports France"
            lower.contains("bein") && (lower.contains("turk") || lower.contains("türkiye") || lower.contains(" tr")) ->
                "beIN Sports Türkiye"
            lower.contains("bein") && (lower.contains("usa") || lower.contains("espanol")) ->
                "beIN Sports USA"
            else ->
                "beIN Sports MENA"
        }
    }
}
