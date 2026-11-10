package com.example.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.text.Normalizer
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Football Confederations and their dynamic FIFA World Ranking qualification quotas for Kickoff:
 * - [CAF]: Top 5 African national teams
 * - [AFC]: Top 3 Asian national teams
 * - [UEFA]: Top 7 European national teams
 * - [CONMEBOL]: Top 4 South American national teams
 */
enum class FootballConfederation(
    val code: String,
    val displayName: String,
    val topTeamsQuota: Int
) {
    CAF("CAF", "Africa", 5),
    AFC("AFC", "Asia", 3),
    UEFA("UEFA", "Europe", 7),
    CONMEBOL("CONMEBOL", "South America", 4),
    OTHER("OTHER", "Rest of World", 0);

    companion object {
        fun fromCode(raw: String?): FootballConfederation {
            val clean = raw?.trim()?.uppercase(Locale.ROOT).orEmpty()
            return entries.firstOrNull { it.code == clean } ?: OTHER
        }
    }
}

/**
 * Represents a national association's FIFA World Ranking entry and derived confederation rank.
 */
data class RankedNationalTeam(
    val fifaCode: String,
    val iso2Code: String,
    val countryName: String,
    val confederation: FootballConfederation,
    val globalRank: Int,
    val confederationRank: Int,
    val rankingPoints: Double
)

/**
 * Dynamic FIFA World Ranking & Confederation Top-N Qualification Engine.
 *
 * Never hardcodes qualified national team names in filter logic. Instead:
 * 1. Syncs the FIFA Men's World Ranking table from the official FIFA Ranking API
 *    (`https://www.fifa.com/api/ranking-overview?locale=en` / `https://inside.fifa.com/api/ranking-overview?locale=en`)
 *    or Sofascore FIFA Rankings endpoint (`https://api.sofascore.com/api/v1/rankings/type/2`),
 *    caching in `SharedPreferences` (`setplay_fifa_rankings_cache_v2`, 7-day TTL) and bootstrapping offline
 *    from `assets/fifa_confederation_rankings_seed.json`.
 * 2. Dynamically computes the qualified Top-N national teams per confederation:
 *    - **CAF**: Top 5 (`confederationRank <= 5`)
 *    - **AFC**: Top 3 (`confederationRank <= 3`)
 *    - **UEFA**: Top 7 (`confederationRank <= 7`)
 *    - **CONMEBOL**: Top 4 (`confederationRank <= 4`)
 * 3. Evaluates international fixtures (`isQualifiedInternationalFixture`) purely against the currently
 *    computed Top-N qualified national teams.
 */
class InternationalRankingResolver(
    private val context: Context? = null,
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
) {
    private val prefs by lazy {
        context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val rankedTeamsRef = AtomicReference<List<RankedNationalTeam>>(emptyList())

    init {
        bootstrapRankingsFromCacheOrSeed()
    }

    private fun bootstrapRankingsFromCacheOrSeed() {
        val cachedJson = runCatching { prefs?.getString(KEY_RANKINGS_JSON, null) }.getOrNull()
        if (!cachedJson.isNullOrBlank()) {
            val parsed = parseAndComputeConfederationRanks(cachedJson)
            if (parsed.isNotEmpty()) {
                rankedTeamsRef.set(parsed)
                return
            }
        }

        val assetJson = runCatching {
            context?.assets?.open(SEED_ASSET_FILE_NAME)?.bufferedReader()?.use { it.readText() }
        }.getOrNull()

        val initialJson = if (!assetJson.isNullOrBlank()) assetJson else EMBEDDED_SEED_JSON
        val parsedSeed = parseAndComputeConfederationRanks(initialJson)
        rankedTeamsRef.set(parsedSeed)
    }

    /**
     * Syncs the FIFA Men's World Ranking table if the 7-day cache TTL has expired or [forceRefresh] is true.
     */
    suspend fun syncInternationalRankingsIfNeeded(forceRefresh: Boolean = false): List<RankedNationalTeam> =
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val lastSync = runCatching { prefs?.getLong(KEY_LAST_SYNC_MS, 0L) ?: 0L }.getOrDefault(0L)
            val current = rankedTeamsRef.get()
            if (!forceRefresh && current.isNotEmpty() && (now - lastSync) in 1 until CACHE_TTL_MS) {
                return@withContext current
            }

            val candidateEndpoints = listOf(
                "https://www.fifa.com/api/ranking-overview?locale=en",
                "https://inside.fifa.com/api/ranking-overview?locale=en",
                "https://api.sofascore.com/api/v1/rankings/type/2"
            )

            for (endpoint in candidateEndpoints) {
                try {
                    val request = Request.Builder()
                        .url(endpoint)
                        .header("User-Agent", FootballRepository.SOFASCORE_BROWSER_USER_AGENT)
                        .header("Referer", FootballRepository.SOFASCORE_REFERER)
                        .header("Accept", "application/json")
                        .build()

                    httpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) return@use
                        val body = response.body?.string()?.trim().orEmpty()
                        if (body.isBlank()) return@use
                        val updated = updateRankingsFromJson(body, now)
                        if (updated.size >= 19) {
                            return@withContext updated
                        }
                    }
                } catch (_: Exception) {
                    // Try next ranking endpoint or retain cached/seed rankings
                }
            }

            if (current.isEmpty()) {
                bootstrapRankingsFromCacheOrSeed()
            }
            rankedTeamsRef.get()
        }

    /**
     * Parses a FIFA / Sofascore / Seed rankings JSON payload, recomputes global and per-confederation
     * ranks dynamically, updates the in-memory table, and persists to `SharedPreferences`.
     */
    fun updateRankingsFromJson(
        rawJson: String,
        syncedAtMs: Long = System.currentTimeMillis()
    ): List<RankedNationalTeam> {
        val computed = parseAndComputeConfederationRanks(rawJson)
        if (computed.isNotEmpty()) {
            rankedTeamsRef.set(computed)
            runCatching {
                prefs?.edit()
                    ?.putString(KEY_RANKINGS_JSON, serializeRankedTeamsToJson(computed))
                    ?.putLong(KEY_LAST_SYNC_MS, syncedAtMs)
                    ?.apply()
            }
        }
        return rankedTeamsRef.get()
    }

    fun getAllRankedTeams(): List<RankedNationalTeam> {
        val current = rankedTeamsRef.get()
        if (current.isEmpty()) {
            bootstrapRankingsFromCacheOrSeed()
        }
        return rankedTeamsRef.get()
    }

    /**
     * Groups all ranked national associations by their [FootballConfederation], sorts by `globalRank`
     * ascending (tie-breaking by `rankingPoints` descending), and slices the top `confederation.topTeamsQuota`:
     * - CAF: Top 5
     * - AFC: Top 3
     * - UEFA: Top 7
     * - CONMEBOL: Top 4
     */
    fun getQualifiedTeamsByConfederation(): Map<FootballConfederation, List<RankedNationalTeam>> {
        val allTeams = getAllRankedTeams()
        val result = LinkedHashMap<FootballConfederation, List<RankedNationalTeam>>()
        for (conf in listOf(
            FootballConfederation.CAF,
            FootballConfederation.AFC,
            FootballConfederation.UEFA,
            FootballConfederation.CONMEBOL
        )) {
            val topForConf = allTeams
                .filter { it.confederation == conf }
                .sortedWith(
                    compareBy<RankedNationalTeam> { it.globalRank }
                        .thenByDescending { it.rankingPoints }
                )
                .take(conf.topTeamsQuota)
                .mapIndexed { idx, team ->
                    team.copy(confederationRank = idx + 1)
                }
            result[conf] = topForConf
        }
        return result
    }

    /**
     * Flat list of all currently qualified national teams across CAF (5), AFC (3), UEFA (7), and CONMEBOL (4).
     */
    fun getQualifiedNationalTeams(): List<RankedNationalTeam> {
        return getQualifiedTeamsByConfederation().values.flatten()
    }

    /**
     * Resolves a team name or FIFA/ISO country code to its [RankedNationalTeam] if and only if that
     * national team is currently in the dynamically computed Top-N qualified set.
     */
    fun resolveQualifiedNationalTeam(
        teamName: String,
        countryCode: String = ""
    ): RankedNationalTeam? {
        val qualified = getQualifiedNationalTeams()
        if (qualified.isEmpty()) return null

        val cleanCode = countryCode.trim().uppercase(Locale.ROOT)
        if (cleanCode.length in 2..3) {
            val byCode = qualified.firstOrNull {
                it.fifaCode.equals(cleanCode, ignoreCase = true) ||
                    it.iso2Code.equals(cleanCode, ignoreCase = true)
            }
            if (byCode != null) return byCode
        }

        val normalizedInput = normalizeNationalTeamName(teamName)
        if (normalizedInput.isBlank()) return null

        return qualified.firstOrNull { candidate ->
            val normCandidate = normalizeNationalTeamName(candidate.countryName)
            normCandidate == normalizedInput ||
                candidate.fifaCode.equals(normalizedInput, ignoreCase = true)
        }
    }

    /**
     * Returns `true` if and only if at least one of the two participating national teams is currently
     * qualified in the dynamic FIFA World Ranking Top-N per confederation (Top 5 CAF, Top 3 AFC,
     * Top 7 UEFA, Top 4 CONMEBOL).
     */
    fun isQualifiedInternationalFixture(
        homeTeamName: String,
        awayTeamName: String,
        homeCountryCode: String = "",
        awayCountryCode: String = ""
    ): Boolean {
        return resolveQualifiedNationalTeam(homeTeamName, homeCountryCode) != null ||
            resolveQualifiedNationalTeam(awayTeamName, awayCountryCode) != null
    }

    internal fun parseAndComputeConfederationRanks(rawJson: String): List<RankedNationalTeam> {
        val trimmed = rawJson.trim()
        if (trimmed.isBlank()) return emptyList()
        val root = if (trimmed.startsWith("[")) {
            runCatching { JSONObject().put("teams", JSONArray(trimmed)) }.getOrNull() ?: return emptyList()
        } else {
            runCatching { JSONObject(trimmed) }.getOrNull() ?: return emptyList()
        }

        val rawList = mutableListOf<RankedNationalTeam>()

        // 1. Seed / Cache / Array JSON format: { "teams": [ { "fifaCode", "iso2", "confederation", "globalRank", "points", "countryName" } ] }
        val teamsArr = root.optJSONArray("teams")
        if (teamsArr != null && teamsArr.length() > 0) {
            for (i in 0 until teamsArr.length()) {
                val obj = teamsArr.optJSONObject(i) ?: continue
                val fifaCode = obj.optString("fifaCode", "").trim().uppercase(Locale.ROOT)
                val iso2 = obj.optString("iso2", "").trim().uppercase(Locale.ROOT)
                val countryName = obj.optString("countryName", obj.optString("name", "")).trim()
                if (countryName.isBlank()) continue
                val confRaw = obj.optString("confederation", "")
                val conf = FootballConfederation.fromCode(confRaw).let {
                    if (it == FootballConfederation.OTHER) resolveConfederationForAssociation(fifaCode, iso2, countryName) else it
                }
                if (conf == FootballConfederation.OTHER) continue
                val globalRank = obj.optInt("globalRank", obj.optInt("rank", i + 1)).coerceAtLeast(1)
                val points = obj.optDouble("points", obj.optDouble("rankingPoints", 1500.0))
                rawList.add(
                    RankedNationalTeam(
                        fifaCode = fifaCode.ifBlank { countryName.take(3).uppercase(Locale.ROOT) },
                        iso2Code = iso2,
                        countryName = countryName,
                        confederation = conf,
                        globalRank = globalRank,
                        confederationRank = 0,
                        rankingPoints = points
                    )
                )
            }
        }

        // 2. FIFA Official / Sofascore `rankings` array format
        val rankingsArr = root.optJSONArray("rankings")
        if (rawList.isEmpty() && rankingsArr != null && rankingsArr.length() > 0) {
            for (i in 0 until rankingsArr.length()) {
                val item = rankingsArr.optJSONObject(i) ?: continue
                val rankingItem = item.optJSONObject("rankingItem")
                val teamObj = item.optJSONObject("team")
                val countryObj = teamObj?.optJSONObject("country")

                val countryName = when {
                    rankingItem != null -> rankingItem.optString("name", "").trim()
                    teamObj != null -> teamObj.optString("name", countryObj?.optString("name", "").orEmpty()).trim()
                    else -> item.optString("countryName", item.optString("name", "")).trim()
                }
                if (countryName.isBlank()) continue

                val fifaCode = when {
                    rankingItem != null -> rankingItem.optString("countryCode", "").trim().uppercase(Locale.ROOT)
                    countryObj != null -> countryObj.optString("alpha3", teamObj.optString("nameCode", "")).trim().uppercase(Locale.ROOT)
                    else -> item.optString("fifaCode", item.optString("countryCode", "")).trim().uppercase(Locale.ROOT)
                }
                val iso2 = countryObj?.optString("alpha2", "")?.trim()?.uppercase(Locale.ROOT)
                    ?: item.optString("iso2", "").trim().uppercase(Locale.ROOT)

                val tagText = item.optJSONObject("tag")?.optString("id", item.optJSONObject("tag")?.optString("text", "").orEmpty())
                    ?: item.optString("confederation", "")
                val conf = FootballConfederation.fromCode(tagText).let {
                    if (it == FootballConfederation.OTHER) resolveConfederationForAssociation(fifaCode, iso2, countryName) else it
                }
                if (conf == FootballConfederation.OTHER) continue

                val globalRank = when {
                    rankingItem != null -> rankingItem.optInt("rank", i + 1)
                    else -> item.optInt("ranking", item.optInt("rank", i + 1))
                }.coerceAtLeast(1)

                val points = when {
                    rankingItem != null -> rankingItem.optDouble("totalPoints", 1500.0)
                    else -> item.optDouble("points", item.optDouble("totalPoints", 1500.0))
                }

                rawList.add(
                    RankedNationalTeam(
                        fifaCode = fifaCode.ifBlank { countryName.take(3).uppercase(Locale.ROOT) },
                        iso2Code = iso2,
                        countryName = countryName,
                        confederation = conf,
                        globalRank = globalRank,
                        confederationRank = 0,
                        rankingPoints = points
                    )
                )
            }
        }

        if (rawList.isEmpty()) return emptyList()

        // Sort globally by globalRank ASC, then rankingPoints DESC, and assign 1-based confederationRank per confederation
        val sortedGlobal = rawList.sortedWith(
            compareBy<RankedNationalTeam> { it.globalRank }
                .thenByDescending { it.rankingPoints }
        )
        val confCounters = mutableMapOf<FootballConfederation, Int>()
        return sortedGlobal.map { team ->
            val nextConfRank = (confCounters[team.confederation] ?: 0) + 1
            confCounters[team.confederation] = nextConfRank
            team.copy(confederationRank = nextConfRank)
        }
    }

    private fun serializeRankedTeamsToJson(teams: List<RankedNationalTeam>): String {
        val root = JSONObject()
        val arr = JSONArray()
        for (team in teams) {
            arr.put(
                JSONObject().apply {
                    put("fifaCode", team.fifaCode)
                    put("iso2", team.iso2Code)
                    put("confederation", team.confederation.code)
                    put("globalRank", team.globalRank)
                    put("points", team.rankingPoints)
                    put("countryName", team.countryName)
                }
            )
        }
        root.put("teams", arr)
        return root.toString()
    }

    companion object {
        private const val PREFS_NAME = "setplay_fifa_rankings_cache_v2"
        private const val KEY_RANKINGS_JSON = "cached_fifa_rankings_json_v2"
        private const val KEY_LAST_SYNC_MS = "cached_fifa_rankings_sync_ms_v2"
        private const val SEED_ASSET_FILE_NAME = "fifa_confederation_rankings_seed.json"
        internal const val CACHE_TTL_MS = 7L * 24L * 60L * 60L * 1000L // 7 days

        fun normalizeNationalTeamName(rawName: String): String {
            if (rawName.isBlank()) return ""
            val decomposed = Normalizer.normalize(rawName, Normalizer.Form.NFD)
                .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
            val cleaned = decomposed
                .lowercase(Locale.ROOT)
                .replace(Regex("\\b(national|team|men|mens|football|soccer|federation|selection|rep|republic)\\b"), " ")
                .replace(Regex("[^a-z0-9\\s]"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()

            return when {
                cleaned == "cote d ivoire" || cleaned == "cote divoire" || cleaned == "ivory coast" -> "ivory coast"
                cleaned == "korea" || cleaned == "south korea" || cleaned == "korea south" || cleaned == "taeguk warriors" -> "south korea"
                cleaned == "ir iran" || cleaned == "iran" -> "iran"
                cleaned == "holland" || cleaned == "the netherlands" || cleaned == "nederland" || cleaned == "netherlands" -> "netherlands"
                cleaned == "maroc" || cleaned == "al maghrib" || cleaned == "morocco" -> "morocco"
                cleaned == "espagne" || cleaned == "espana" || cleaned == "spain" -> "spain"
                cleaned == "angleterre" || cleaned == "england" -> "england"
                cleaned == "allemagne" || cleaned == "deutschland" || cleaned == "germany" -> "germany"
                cleaned == "italie" || cleaned == "italia" || cleaned == "italy" -> "italy"
                cleaned == "belgique" || cleaned == "belgium" -> "belgium"
                cleaned == "bresil" || cleaned == "brasil" || cleaned == "brazil" -> "brazil"
                cleaned == "argentine" || cleaned == "argentina" -> "argentina"
                cleaned == "colombie" || cleaned == "colombia" -> "colombia"
                cleaned == "japon" || cleaned == "japan" -> "japan"
                cleaned == "egypte" || cleaned == "misr" || cleaned == "egypt" -> "egypt"
                cleaned == "algerie" || cleaned == "algeria" -> "algeria"
                cleaned == "tunisie" || cleaned == "tunisia" -> "tunisia"
                cleaned == "cameroun" || cleaned == "cameroon" -> "cameroon"
                cleaned == "afrique du sud" || cleaned == "south africa" -> "south africa"
                cleaned == "arabie saoudite" || cleaned == "saudi arabia" -> "saudi arabia"
                else -> cleaned
            }
        }

        internal fun resolveConfederationForAssociation(
            fifaCode: String,
            iso2Code: String,
            countryName: String
        ): FootballConfederation {
            val code = fifaCode.uppercase(Locale.ROOT)
            val iso = iso2Code.uppercase(Locale.ROOT)
            val normName = normalizeNationalTeamName(countryName)

            if (code in CAF_CODES || iso in CAF_ISO2 || normName in CAF_NAMES) return FootballConfederation.CAF
            if (code in AFC_CODES || iso in AFC_ISO2 || normName in AFC_NAMES) return FootballConfederation.AFC
            if (code in UEFA_CODES || iso in UEFA_ISO2 || normName in UEFA_NAMES) return FootballConfederation.UEFA
            if (code in CONMEBOL_CODES || iso in CONMEBOL_ISO2 || normName in CONMEBOL_NAMES) return FootballConfederation.CONMEBOL
            return FootballConfederation.OTHER
        }

        private val CAF_CODES = setOf(
            "MAR", "SEN", "EGY", "NGA", "ALG", "CIV", "TUN", "CMR", "MLI", "COD",
            "RSA", "BFA", "CPV", "GHA", "GUI", "EQG", "GAB", "ANG", "ZAM", "BEN", "MOZ", "MTN", "TAN"
        )
        private val CAF_ISO2 = setOf(
            "MA", "SN", "EG", "NG", "DZ", "CI", "TN", "CM", "ML", "CD", "ZA", "BF", "CV", "GH", "GN", "GA", "AO", "ZM"
        )
        private val CAF_NAMES = setOf(
            "morocco", "senegal", "egypt", "nigeria", "algeria", "ivory coast", "tunisia",
            "cameroon", "mali", "dr congo", "south africa", "burkina faso", "cape verde", "ghana", "guinea", "gabon", "angola"
        )

        private val AFC_CODES = setOf(
            "JPN", "IRN", "KOR", "AUS", "QAT", "IRQ", "UZB", "KSA", "UAE", "JOR", "OMA", "BHR", "CHN", "THA", "PLE", "SYR"
        )
        private val AFC_ISO2 = setOf(
            "JP", "IR", "KR", "AU", "QA", "IQ", "UZ", "SA", "AE", "JO", "OM", "BH", "CN", "TH", "PS", "SY"
        )
        private val AFC_NAMES = setOf(
            "japan", "iran", "south korea", "australia", "qatar", "iraq", "uzbekistan",
            "saudi arabia", "united arab emirates", "uae", "jordan", "oman", "bahrain", "china", "thailand", "palestine"
        )

        private val UEFA_CODES = setOf(
            "FRA", "ESP", "ENG", "BEL", "POR", "NED", "ITA", "GER", "CRO", "SUI",
            "DEN", "AUT", "UKR", "TUR", "POL", "SWE", "WAL", "SRB", "HUN", "SCO", "CZE", "NOR", "GRE", "ROU", "SVK"
        )
        private val UEFA_ISO2 = setOf(
            "FR", "ES", "GB", "BE", "PT", "NL", "IT", "DE", "HR", "CH", "DK", "AT", "UA", "TR", "PL", "SE", "RS", "HU", "NO", "GR", "RO", "SK"
        )
        private val UEFA_NAMES = setOf(
            "france", "spain", "england", "belgium", "portugal", "netherlands", "italy", "germany",
            "croatia", "switzerland", "denmark", "austria", "ukraine", "turkey", "turkiye", "poland", "sweden", "wales", "serbia", "hungary", "scotland", "norway"
        )

        private val CONMEBOL_CODES = setOf(
            "ARG", "BRA", "COL", "URU", "ECU", "VEN", "PER", "PAR", "CHL", "CHI", "BOL"
        )
        private val CONMEBOL_ISO2 = setOf(
            "AR", "BR", "CO", "UY", "EC", "VE", "PE", "PY", "CL", "BO"
        )
        private val CONMEBOL_NAMES = setOf(
            "argentina", "brazil", "colombia", "uruguay", "ecuador", "venezuela", "peru", "paraguay", "chile", "bolivia"
        )

        internal val EMBEDDED_SEED_JSON = """
            {
              "version": "2026-Q1",
              "source": "FIFA Men's World Ranking Seed",
              "teams": [
                { "fifaCode": "ARG", "iso2": "AR", "confederation": "CONMEBOL", "globalRank": 1, "points": 1883.50, "countryName": "Argentina" },
                { "fifaCode": "FRA", "iso2": "FR", "confederation": "UEFA", "globalRank": 2, "points": 1859.85, "countryName": "France" },
                { "fifaCode": "ESP", "iso2": "ES", "confederation": "UEFA", "globalRank": 3, "points": 1844.33, "countryName": "Spain" },
                { "fifaCode": "ENG", "iso2": "GB", "confederation": "UEFA", "globalRank": 4, "points": 1807.83, "countryName": "England" },
                { "fifaCode": "BRA", "iso2": "BR", "confederation": "CONMEBOL", "globalRank": 5, "points": 1784.37, "countryName": "Brazil" },
                { "fifaCode": "BEL", "iso2": "BE", "confederation": "UEFA", "globalRank": 6, "points": 1761.27, "countryName": "Belgium" },
                { "fifaCode": "POR", "iso2": "PT", "confederation": "UEFA", "globalRank": 7, "points": 1752.68, "countryName": "Portugal" },
                { "fifaCode": "NED", "iso2": "NL", "confederation": "UEFA", "globalRank": 8, "points": 1748.24, "countryName": "Netherlands" },
                { "fifaCode": "ITA", "iso2": "IT", "confederation": "UEFA", "globalRank": 9, "points": 1729.40, "countryName": "Italy" },
                { "fifaCode": "COL", "iso2": "CO", "confederation": "CONMEBOL", "globalRank": 10, "points": 1724.37, "countryName": "Colombia" },
                { "fifaCode": "GER", "iso2": "DE", "confederation": "UEFA", "globalRank": 11, "points": 1703.59, "countryName": "Germany" },
                { "fifaCode": "CRO", "iso2": "HR", "confederation": "UEFA", "globalRank": 12, "points": 1701.36, "countryName": "Croatia" },
                { "fifaCode": "MAR", "iso2": "MA", "confederation": "CAF", "globalRank": 13, "points": 1681.57, "countryName": "Morocco" },
                { "fifaCode": "URU", "iso2": "UY", "confederation": "CONMEBOL", "globalRank": 14, "points": 1680.36, "countryName": "Uruguay" },
                { "fifaCode": "JPN", "iso2": "JP", "confederation": "AFC", "globalRank": 15, "points": 1645.09, "countryName": "Japan" },
                { "fifaCode": "SEN", "iso2": "SN", "confederation": "CAF", "globalRank": 17, "points": 1637.25, "countryName": "Senegal" },
                { "fifaCode": "IRN", "iso2": "IR", "confederation": "AFC", "globalRank": 19, "points": 1627.58, "countryName": "Iran" },
                { "fifaCode": "KOR", "iso2": "KR", "confederation": "AFC", "globalRank": 22, "points": 1589.93, "countryName": "South Korea" },
                { "fifaCode": "AUS", "iso2": "AU", "confederation": "AFC", "globalRank": 24, "points": 1552.77, "countryName": "Australia" },
                { "fifaCode": "ECU", "iso2": "EC", "confederation": "CONMEBOL", "globalRank": 27, "points": 1536.32, "countryName": "Ecuador" },
                { "fifaCode": "EGY", "iso2": "EG", "confederation": "CAF", "globalRank": 30, "points": 1526.25, "countryName": "Egypt" },
                { "fifaCode": "NGA", "iso2": "NG", "confederation": "CAF", "globalRank": 36, "points": 1503.29, "countryName": "Nigeria" },
                { "fifaCode": "ALG", "iso2": "DZ", "confederation": "CAF", "globalRank": 37, "points": 1496.94, "countryName": "Algeria" },
                { "fifaCode": "CIV", "iso2": "CI", "confederation": "CAF", "globalRank": 40, "points": 1493.57, "countryName": "Ivory Coast" },
                { "fifaCode": "TUN", "iso2": "TN", "confederation": "CAF", "globalRank": 47, "points": 1478.52, "countryName": "Tunisia" },
                { "fifaCode": "CMR", "iso2": "CM", "confederation": "CAF", "globalRank": 49, "points": 1467.80, "countryName": "Cameroon" },
                { "fifaCode": "QAT", "iso2": "QA", "confederation": "AFC", "globalRank": 46, "points": 1481.69, "countryName": "Qatar" },
                { "fifaCode": "KSA", "iso2": "SA", "confederation": "AFC", "globalRank": 59, "points": 1422.66, "countryName": "Saudi Arabia" }
              ]
            }
        """.trimIndent()
    }
}
