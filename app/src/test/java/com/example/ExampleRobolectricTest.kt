package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.StreamVaultDatabase
import com.example.data.model.CategoryEntity
import com.example.data.model.PlaylistEntity
import com.example.data.model.StreamEntity
import com.example.data.remote.XtreamClient
import com.example.data.repository.IptvRepository
import com.example.ui.player.PlayerViewModel
import com.example.ui.playlists.formatExpirationDate
import com.example.ui.theme.AmberGold
import com.example.ui.theme.Champagne
import com.example.ui.theme.CinemaMarqueeGold
import com.example.ui.theme.CinemaRuby
import com.example.ui.theme.DarkGraphite
import com.example.ui.theme.FavoritesBotanicalBorder
import com.example.ui.theme.FavoritesEmeraldCard
import com.example.ui.theme.FavoritesEvergreenSurface
import com.example.ui.theme.FavoritesObsidianBase
import com.example.ui.theme.FavoritesPaleMint
import com.example.ui.theme.FavoritesSagePrimary
import com.example.ui.theme.KickoffCeruleanBlue
import com.example.ui.theme.KickoffCoralRed
import com.example.ui.theme.KickoffPeachCream
import com.example.ui.theme.KickoffTropicalTeal
import com.example.ui.theme.LiveCrimson
import com.example.ui.theme.MetalCharcoal
import com.example.ui.theme.MoviesAquamarine
import com.example.ui.theme.MoviesAtlanticTeal
import com.example.ui.theme.MoviesCyanTeal
import com.example.ui.theme.MoviesMintSpring
import com.example.ui.theme.MoviesOceanTeal
import com.example.ui.theme.MoviesSapphireNavy
import com.example.ui.theme.PaleGold
import com.example.ui.theme.SeriesCypressCard
import com.example.ui.theme.SeriesEmerald
import com.example.ui.theme.SeriesForestSurface
import com.example.ui.theme.SeriesIceAqua
import com.example.ui.theme.SeriesJadeAccent
import com.example.ui.theme.SeriesMistTeal
import com.example.ui.theme.SeriesPineBase
import com.example.ui.theme.SeriesSeaJade
import com.example.ui.theme.TvBroadcastScarlet
import com.example.ui.theme.TvCrimsonRose
import com.example.ui.theme.TvDeepMaroon
import com.example.ui.theme.TvMidnightSlate
import com.example.ui.theme.TvPlumMagenta
import com.example.ui.theme.TvTwilightIndigo
import com.example.util.SecurityManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: StreamVaultDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, StreamVaultDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        SecurityManager.resetPinRateLimiter()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read string from context matches SetPlay`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SetPlay", appName)
    }

    @Test
    fun `verify Royal Gold luxury palette and section sub-palettes`() {
        assertEquals(Color(0xFF141619), DarkGraphite)
        assertEquals(Color(0xFF25282E), MetalCharcoal)
        assertEquals(Color(0xFFD4A238), AmberGold)
        assertEquals(Color(0xFFF6E8C8), PaleGold)
        assertEquals(Color(0xFFDFC69E), Champagne)

        // Legacy section signature sub-palettes
        assertEquals(Color(0xFFE63946), LiveCrimson)
        assertEquals(Color(0xFFC1121F), CinemaRuby)
        assertEquals(Color(0xFFE5A93C), CinemaMarqueeGold)
        assertEquals(Color(0xFF1B998B), SeriesEmerald)
        assertEquals(Color(0xFF2EC4B6), SeriesJadeAccent)

        // 1. KickOff Section Palette (#0081A7, #00AFB9, #FED9B7, #F07167)
        assertEquals(Color(0xFF0081A7), KickoffCeruleanBlue)
        assertEquals(Color(0xFF00AFB9), KickoffTropicalTeal)
        assertEquals(Color(0xFFFED9B7), KickoffPeachCream)
        assertEquals(Color(0xFFF07167), KickoffCoralRed)

        // 2. Live TV Section Palette (#1F313B, #383852, #784259, #B94E56, #BE4039, #683536)
        assertEquals(Color(0xFF1F313B), TvMidnightSlate)
        assertEquals(Color(0xFF383852), TvTwilightIndigo)
        assertEquals(Color(0xFF784259), TvPlumMagenta)
        assertEquals(Color(0xFFB94E56), TvCrimsonRose)
        assertEquals(Color(0xFFBE4039), TvBroadcastScarlet)
        assertEquals(Color(0xFF683536), TvDeepMaroon)

        // 3. Movies Section Palette (#80EE98, #46DFB1, #09D1C7, #15919B, #0C6478, #213A58)
        assertEquals(Color(0xFF80EE98), MoviesMintSpring)
        assertEquals(Color(0xFF46DFB1), MoviesAquamarine)
        assertEquals(Color(0xFF09D1C7), MoviesCyanTeal)
        assertEquals(Color(0xFF15919B), MoviesOceanTeal)
        assertEquals(Color(0xFF0C6478), MoviesAtlanticTeal)
        assertEquals(Color(0xFF213A58), MoviesSapphireNavy)

        // 4. Series Section Palette (#113B3A, #184948, #235E5D, #317978, #599D9C, #B7E6E5)
        assertEquals(Color(0xFF113B3A), SeriesPineBase)
        assertEquals(Color(0xFF184948), SeriesForestSurface)
        assertEquals(Color(0xFF235E5D), SeriesCypressCard)
        assertEquals(Color(0xFF317978), SeriesSeaJade)
        assertEquals(Color(0xFF599D9C), SeriesMistTeal)
        assertEquals(Color(0xFFB7E6E5), SeriesIceAqua)

        // 5. Favorites Section Palette (#051F20, #0B2B26, #163832, #235347, #8EB69B, #DAF1DE)
        assertEquals(Color(0xFF051F20), FavoritesObsidianBase)
        assertEquals(Color(0xFF0B2B26), FavoritesEvergreenSurface)
        assertEquals(Color(0xFF163832), FavoritesEmeraldCard)
        assertEquals(Color(0xFF235347), FavoritesBotanicalBorder)
        assertEquals(Color(0xFF8EB69B), FavoritesSagePrimary)
        assertEquals(Color(0xFFDAF1DE), FavoritesPaleMint)
    }

    @Test
    fun `test security manager AES-GCM credential encryption, PIN hashing, and URL hardening`() = runBlocking {
        // 1. AES-GCM encryption at rest & decryption
        val rawPass = "Sup3rS3cret!Pass#2026"
        val encrypted = SecurityManager.encryptCredential(rawPass)
        assertTrue(encrypted.startsWith(SecurityManager.ENCRYPTED_PREFIX))
        assertNotEquals(rawPass, encrypted)
        assertEquals(rawPass, SecurityManager.decryptCredential(encrypted))

        // Verify IptvRepository stores encrypted password in SQLite and decrypts on read
        val repository = IptvRepository(
            playlistDao = database.playlistDao(),
            categoryDao = database.categoryDao(),
            streamDao = database.streamDao(),
            epgDao = database.epgDao(),
            parentalDao = database.parentalSettingsDao()
        )
        val id = repository.addOrUpdatePlaylist(
            PlaylistEntity(
                name = "Encrypted Vault Account",
                serverUrl = "https://iptv.setplay.io",
                username = "vip_user",
                password = rawPass,
                isActive = true
            )
        )
        val rawDbEntity = database.playlistDao().getPlaylistById(id)
        assertNotNull(rawDbEntity)
        assertTrue(rawDbEntity!!.password.startsWith(SecurityManager.ENCRYPTED_PREFIX))

        val repoDecrypted = repository.getPlaylistById(id)
        assertEquals(rawPass, repoDecrypted?.password)

        // 2. Salted SHA-256 Parental PIN hashing & brute-force rate limiting
        repository.updateParentalPin("4826")
        val storedPin = database.parentalSettingsDao().getSettingsOnce()?.pin ?: ""
        assertTrue(storedPin.startsWith(SecurityManager.HASHED_PIN_PREFIX))
        assertTrue(repository.verifyParentalPin("4826"))
        assertFalse(repository.verifyParentalPin("9999"))

        // Trigger 5 failed attempts to verify rate-limiting lockout
        SecurityManager.resetPinRateLimiter()
        repeat(5) {
            assertFalse(SecurityManager.verifyPin("1111", storedPin, nowMs = 1_000_000L))
        }
        assertTrue(SecurityManager.getPinLockoutRemainingSeconds(nowMs = 1_005_000L) > 0)
        // Even correct PIN is rejected while locked out
        assertFalse(SecurityManager.verifyPin("4826", storedPin, nowMs = 1_005_000L))
        // Unlocked after 30s cooldown expires
        assertTrue(SecurityManager.verifyPin("4826", storedPin, nowMs = 1_035_000L))

        // 3. URL scheme sanitization, HTTPS-first candidates, and log redaction
        assertEquals("", SecurityManager.sanitizeServerUrl("file:///etc/passwd"))
        assertEquals("", SecurityManager.sanitizeServerUrl("javascript:alert(1)"))
        val candidates = SecurityManager.buildHttpsFirstCandidateUrls("stream.setplay.tv:8080")
        assertEquals(listOf("https://stream.setplay.tv:8080", "http://stream.setplay.tv:8080"), candidates)

        // Extension traversal prevention
        val safeUrl = XtreamClient.buildStreamUrl(
            serverUrl = "https://stream.setplay.tv",
            username = "vip_user",
            password = encrypted,
            streamType = "movie",
            streamId = 777L,
            containerExtension = "../../etc/passwd"
        )
        assertEquals("https://stream.setplay.tv/movie/vip_user/Sup3rS3cret!Pass%232026/777.mp4", safeUrl)

        // Log redaction
        val leakedLog = "Failed GET http://server.com/player_api.php?username=vip_user&password=$rawPass"
        val redacted = SecurityManager.redactSensitiveText(leakedLog, "vip_user", rawPass)
        assertFalse(redacted.contains(rawPass))
        assertFalse(redacted.contains("vip_user"))
    }

    @Test
    fun `test player VLC audio boost, pinch zoom, and stopAndClosePlayer dismissal`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val playerViewModel = PlayerViewModel(app)

        // Test VLC 200% Audio Boost state
        playerViewModel.setVolumePercent(175, showHudBadge = true)
        assertEquals(175, playerViewModel.uiState.value.volumePercent)
        assertEquals(175, playerViewModel.uiState.value.gestureVolume)

        // Test MX Player Pinch-to-Zoom & Reset
        playerViewModel.onTransformGesture(zoomChange = 2.0f, panX = 40f, panY = -20f)
        assertEquals(2.0f, playerViewModel.uiState.value.videoZoomScale, 0.01f)
        playerViewModel.resetZoomAndPan()
        assertEquals(1.0f, playerViewModel.uiState.value.videoZoomScale, 0.01f)

        // Test stopAndClosePlayer clears currentStream so PiP/MiniPlayer disappears
        playerViewModel.stopAndClosePlayer()
        assertNull(playerViewModel.uiState.value.currentStream)
        assertFalse(playerViewModel.uiState.value.isPlaying)
    }

    @Test
    fun `test xtream live stream url generation`() {
        val url = XtreamClient.buildStreamUrl(
            serverUrl = "http://iptv.example.com",
            username = "test_user",
            password = "test_password",
            streamType = "live",
            streamId = 12345L,
            containerExtension = "ts"
        )
        assertEquals("http://iptv.example.com/live/test_user/test_password/12345.ts", url)
    }

    @Test
    fun `test xtream movie and series vod stream url generation`() {
        val movieUrl = XtreamClient.buildStreamUrl(
            serverUrl = "http://iptv.example.com/",
            username = "test_user",
            password = "test_password",
            streamType = "movie",
            streamId = 9999L,
            containerExtension = "mp4"
        )
        assertEquals("http://iptv.example.com/movie/test_user/test_password/9999.mp4", movieUrl)

        val seriesEpUrl = XtreamClient.buildStreamUrl(
            serverUrl = "http://iptv.example.com/",
            username = "test_user",
            password = "test_password",
            streamType = "series",
            streamId = 54321L,
            containerExtension = "mkv"
        )
        assertEquals("http://iptv.example.com/series/test_user/test_password/54321.mkv", seriesEpUrl)
    }

    @Test
    fun `test xtream catchup stream url generation`() {
        val url = XtreamClient.buildCatchupUrl(
            serverUrl = "http://iptv.example.com",
            username = "test_user",
            password = "test_password",
            streamId = 12345L,
            startEpochSeconds = 1700000000L,
            durationMinutes = 60
        )
        assertTrue(url.contains("/streaming/timeshift.php"))
        assertTrue(url.contains("username=test_user"))
        assertTrue(url.contains("stream=12345"))
        assertTrue(url.contains("duration=60"))
    }

    @Test
    fun `test category first search with channel counts`() = runBlocking {
        val playlistId = database.playlistDao().insertPlaylist(
            PlaylistEntity(
                name = "Primary Account",
                serverUrl = "http://iptv.example.com",
                username = "user1",
                password = "pass1"
            )
        )

        database.categoryDao().insertCategories(
            listOf(
                CategoryEntity(playlistId = playlistId, categoryId = "10", categoryName = "UK Sports UHD", type = "LIVE"),
                CategoryEntity(playlistId = playlistId, categoryId = "20", categoryName = "US News Networks", type = "LIVE"),
                CategoryEntity(playlistId = playlistId, categoryId = "30", categoryName = "Cinema Action", type = "MOVIE")
            )
        )

        database.streamDao().insertStreamsBatch(
            listOf(
                StreamEntity(playlistId = playlistId, streamId = 101, categoryId = "10", name = "Sky Sports Main Event", streamType = "live"),
                StreamEntity(playlistId = playlistId, streamId = 102, categoryId = "10", name = "TNT Sports 1 UHD", streamType = "live"),
                StreamEntity(playlistId = playlistId, streamId = 201, categoryId = "20", name = "CNN HD", streamType = "live")
            )
        )

        val allLiveCats = database.categoryDao().searchCategoriesFast(playlistId, "LIVE", "").first()
        assertEquals(2, allLiveCats.size)

        val sportsCats = database.categoryDao().searchCategoriesFast(playlistId, "LIVE", "Sports").first()
        assertEquals(1, sportsCats.size)
        assertEquals("UK Sports UHD", sportsCats.first().categoryName)

        val counts = database.streamDao().getCategoryStreamCounts(playlistId, "live").first()
        val countMap = counts.associate { it.categoryId to it.streamCount }
        assertEquals(2, countMap["10"])
        assertEquals(1, countMap["20"])

        val initialLive = database.streamDao().getStreamsInitial(playlistId, "live", 180).first()
        assertEquals(3, initialLive.size)
    }

    @Test
    fun `test account credential editing and switching`() = runBlocking {
        val id1 = database.playlistDao().insertPlaylist(
            PlaylistEntity(name = "Account A", serverUrl = "http://a.example.com", username = "u1", password = "p1", isActive = true)
        )
        val id2 = database.playlistDao().insertPlaylist(
            PlaylistEntity(name = "Account B", serverUrl = "http://b.example.com", username = "u2", password = "p2", isActive = false)
        )

        database.playlistDao().updatePlaylistCredentials(
            id = id1,
            name = "Account A Updated",
            serverUrl = "https://secure.example.com",
            username = "u1_new",
            password = "p1_new"
        )

        val updated1 = database.playlistDao().getPlaylistById(id1)
        assertNotNull(updated1)
        assertEquals("Account A Updated", updated1?.name)
        assertEquals("https://secure.example.com", updated1?.serverUrl)

        database.playlistDao().setActivePlaylist(id2)
        val active = database.playlistDao().getActivePlaylistOnce()
        assertEquals(id2, active?.id)
    }

    @Test
    fun `test expiration date formatter`() {
        assertEquals("Unlimited", formatExpirationDate(""))
        assertEquals("Unlimited", formatExpirationDate("null"))
        val formatted = formatExpirationDate("1800000000")
        assertTrue(formatted.contains("2027"))
    }

    @Test
    fun `test Kickoff dual-API hybrid pipeline and Favorites-only channel lookup with word-boundary enforcement`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val footballRepository = com.example.data.repository.FootballRepository(
            context = context,
            favoriteTeamDao = database.favoriteTeamDao(),
            tournamentPreferenceDao = database.tournamentPreferenceDao(),
            streamDao = database.streamDao()
        )

        // 1. Verify team name normalization for merging SportAPI.ai and TheSportsDB records
        assertEquals("barcelona", footballRepository.normalizeTeamNameForMerge("FC Barcelona"))
        assertEquals("atletico madrid", footballRepository.normalizeTeamNameForMerge("Club Atlético de Madrid"))
        assertEquals("raja", footballRepository.normalizeTeamNameForMerge("Raja Club Athletic"))
        assertEquals("wydad", footballRepository.normalizeTeamNameForMerge("Wydad AC"))

        // 2. Verify TheSportsDB /tiny badge URL transformation
        assertEquals(
            "https://r2.thesportsdb.com/images/media/team/badge/xsuwtw1473538028.png/tiny",
            footballRepository.toTinySportsDbBadgeUrl("https://r2.thesportsdb.com/images/media/team/badge/xsuwtw1473538028.png")
        )

        // 3. Verify hybrid record merge: SportAPI.ai live score/clock + TheSportsDB /tiny badges & eventId
        val tsdbFixture = com.example.data.repository.FootballMatch(
            matchId = "tsdb_1001",
            tournamentId = "botola_pro",
            tournamentName = "Botola Pro Inwi",
            tournamentRegion = "Morocco",
            dayTab = com.example.data.repository.MatchDayTab.TODAY,
            kickoffTimeFormatted = "20:00",
            kickoffDateFormatted = "Today • GMT+1",
            status = com.example.data.repository.MatchStatus.UPCOMING,
            liveMinuteOrBadge = "20:00",
            homeTeamId = "tsdb_raja",
            homeTeamName = "Raja Club Athletic",
            homeTeamBadge = "https://r2.thesportsdb.com/images/media/team/badge/xsuwtw1473538028.png/tiny",
            homeScore = null,
            awayTeamId = "tsdb_wydad",
            awayTeamName = "Wydad AC",
            awayTeamBadge = "https://r2.thesportsdb.com/images/media/team/badge/vwpvry1467462651.png/tiny",
            awayScore = null,
            stadiumName = "Stade Mohammed V",
            cityName = "Casablanca",
            beinChannel = "Arryadia HD",
            beinChannelNumberHint = "Arryadia",
            sportsDbEventId = "2099111"
        )
        val sportApiLive = tsdbFixture.copy(
            matchId = "sportapi_999",
            status = com.example.data.repository.MatchStatus.LIVE,
            liveMinuteOrBadge = "2H • 67'",
            homeTeamBadge = "",
            awayTeamBadge = "",
            homeScore = 2,
            awayScore = 1,
            sportsDbEventId = ""
        )
        val merged = footballRepository.mergeMatchRecords(primaryLive = sportApiLive, badgeAndTvSource = tsdbFixture)
        assertEquals(com.example.data.repository.MatchStatus.LIVE, merged.status)
        assertEquals("2H • 67'", merged.liveMinuteOrBadge)
        assertEquals(2, merged.homeScore)
        assertEquals(1, merged.awayScore)
        assertEquals("2099111", merged.sportsDbEventId)
        assertTrue(merged.homeTeamBadge.endsWith("/tiny"))
        assertTrue(merged.awayTeamBadge.endsWith("/tiny"))

        // 4. Verify Exact Source Channel Preservation for beIN Sports & Non-beIN MENA Broadcaster Formatting
        assertEquals(
            "beIN Sports 1 4K",
            footballRepository.formatCanonicalMenaChannelName("beIN Sports 1 4K")
        )
        assertEquals(
            "beIN Sports max 5",
            footballRepository.formatCanonicalMenaChannelName("beIN Sports max 5")
        )
        assertEquals(
            "beIN Sports HD 2 Max",
            footballRepository.formatCanonicalMenaChannelName("beIN Sports HD 2 Max")
        )
        assertEquals(
            "beIN Sports Xtra 1",
            footballRepository.formatCanonicalMenaChannelName("beIN Sports Xtra 1")
        )
        assertEquals(
            "AD Sports Premium 1",
            footballRepository.formatCanonicalMenaChannelName("Abu Dhabi Sports 1")
        )
        assertEquals(
            "Arryadia TNT",
            footballRepository.formatCanonicalMenaChannelName("Al Riyadia TNT")
        )
        assertEquals(
            "SSC Extra 1",
            footballRepository.formatCanonicalMenaChannelName("SSC EXTRA 1 HD")
        )

        val uclSlot0 = footballRepository.getSmartMenaFallbackChannels("uefa_cl", "UEFA Champions League", slotIndex = 0)
        val uclSlot1 = footballRepository.getSmartMenaFallbackChannels("uefa_cl", "UEFA Champions League", slotIndex = 1)
        val uclSlot2 = footballRepository.getSmartMenaFallbackChannels("uefa_cl", "UEFA Champions League", slotIndex = 2)
        assertEquals("beIN Sports 1 HD", uclSlot0.first())
        assertEquals("beIN Sports 2 HD", uclSlot1.first())
        assertEquals("beIN Sports 3 HD", uclSlot2.first())

        assertEquals(
            "beIN Sports 3 HD",
            footballRepository.getSmartMenaFallbackChannels("laliga", "LaLiga EA Sports", slotIndex = 0).first()
        )
        assertEquals(
            "beIN Sports 4 HD",
            footballRepository.getSmartMenaFallbackChannels("ligue_1", "Ligue 1 McDonald's", slotIndex = 0).first()
        )
        assertEquals(
            "beIN Sports 5 HD",
            footballRepository.getSmartMenaFallbackChannels("bundesliga", "Bundesliga", slotIndex = 0).first()
        )
        assertEquals(
            "Arryadia HD",
            footballRepository.getSmartMenaFallbackChannels("botola_pro", "Botola Pro Inwi", slotIndex = 0).first()
        )
        assertEquals(
            "beIN Sports 6 HD",
            footballRepository.getSmartMenaFallbackChannels("caf_cl", "CAF Champions League", slotIndex = 0).first()
        )
        assertEquals(
            listOf("AD Sports Premium 1", "AD Sports Premium 2", "STARZPLAY"),
            footballRepository.getSmartMenaFallbackChannels("serie_a", "Serie A", slotIndex = 0)
        )
        assertEquals(
            "SSC 1 HD",
            footballRepository.getSmartMenaFallbackChannels("saudi_pro", "Roshn Saudi League", slotIndex = 0).first()
        )

        // Verify slot-aware day resolution assigns distinct authentic beIN Sports channels when no source channel is present,
        // and preserves the EXACT source channel name when provided by the source
        val simultaneousUclGames = listOf(
            tsdbFixture.copy(matchId = "ucl_1", tournamentId = "uefa_cl", tournamentName = "UEFA Champions League", kickoffTimeFormatted = "18:45", homeTeamName = "Shakhtar Donetsk", awayTeamName = "Sporting CP", beinChannel = "beIN Sports 1 MENA"),
            tsdbFixture.copy(matchId = "ucl_2", tournamentId = "uefa_cl", tournamentName = "UEFA Champions League", kickoffTimeFormatted = "18:45", homeTeamName = "Galatasaray", awayTeamName = "VfB Stuttgart", beinChannel = "beIN Sports 1 MENA"),
            tsdbFixture.copy(matchId = "ucl_3", tournamentId = "uefa_cl", tournamentName = "UEFA Champions League", kickoffTimeFormatted = "18:45", homeTeamName = "Lille", awayTeamName = "Monaco", beinChannel = "beIN Sports max 5", sourceTvChannels = listOf("beIN Sports max 5"))
        )
        val resolvedUclGames = footballRepository.resolveDayMatchesTvChannelsWithMenaPriority(simultaneousUclGames, emptyList())
        assertEquals("beIN Sports 1 HD", resolvedUclGames[0].beinChannel)
        assertEquals("beIN Sports 2 HD", resolvedUclGames[1].beinChannel)
        assertEquals("beIN Sports max 5", resolvedUclGames[2].beinChannel)

        // 5. Verify strict Favorites-only channel lookup with variation recognition ('AR| BEIN SPORTS 02 HD', '[01] VIP | beIN Sports 2', 'beIN Sports max 5' -> 'max 05', '5 max', '05 max')
        val playlistId = database.playlistDao().insertPlaylist(
            PlaylistEntity(
                name = "MENA IPTV",
                serverUrl = "https://iptv.setplay.io",
                username = "morocco_fan",
                password = "secret",
                isActive = true
            )
        )
        database.streamDao().insertStreamsBatch(
            listOf(
                StreamEntity(playlistId = playlistId, streamId = 1L, categoryId = "1", name = "AR: beIN Sports 1 4K [HEVC]", streamType = "live", isFavorite = true),
                StreamEntity(playlistId = playlistId, streamId = 2L, categoryId = "1", name = "AR | beIN Sport 1 FHD", streamType = "live", isFavorite = true),
                StreamEntity(playlistId = playlistId, streamId = 3L, categoryId = "1", name = "VIP ┃ beIN Sports 10 HD", streamType = "live", isFavorite = true),
                StreamEntity(playlistId = playlistId, streamId = 4L, categoryId = "1", name = "AR: beIN Sports 11 HD", streamType = "live", isFavorite = true),
                StreamEntity(playlistId = playlistId, streamId = 5L, categoryId = "1", name = "AR: beIN Sports 4 HD [H265]", streamType = "live", isFavorite = true),
                StreamEntity(playlistId = playlistId, streamId = 6L, categoryId = "1", name = "MA: Al Riyadia TNT HD", streamType = "live", isFavorite = true),
                StreamEntity(playlistId = playlistId, streamId = 7L, categoryId = "1", name = "KSA: SSC Sport 1 UHD", streamType = "live", isFavorite = true),
                // Non-favorite channel in full Xtream catalog — MUST NOT be matched
                StreamEntity(playlistId = playlistId, streamId = 8L, categoryId = "1", name = "AR: beIN Sports 3 HD", streamType = "live", isFavorite = false),
                // Variations of 'beIN Sports max 5' in Favorites
                StreamEntity(playlistId = playlistId, streamId = 9L, categoryId = "1", name = "beIN Sports max 05", streamType = "live", isFavorite = true),
                StreamEntity(playlistId = playlistId, streamId = 10L, categoryId = "1", name = "beIN Sports 5 max", streamType = "live", isFavorite = true),
                StreamEntity(playlistId = playlistId, streamId = 11L, categoryId = "1", name = "beIN Sports 05 max", streamType = "live", isFavorite = true),
                StreamEntity(playlistId = playlistId, streamId = 12L, categoryId = "1", name = "AR: beIN Sports Max 5 FHD", streamType = "live", isFavorite = true),
                // Different channel number with max — MUST NOT be matched for max 5
                StreamEntity(playlistId = playlistId, streamId = 13L, categoryId = "1", name = "beIN Sports max 15", streamType = "live", isFavorite = true),
                // Real-world IPTV naming for beIN Sports 2 HD (with zero-padding, MP4A-LATM codec, prefix index, and Max 2 which must NOT hijack standard 2 HD)
                StreamEntity(playlistId = playlistId, streamId = 14L, categoryId = "1", name = "AR| BEIN SPORTS 02 HD [MP4A-LATM]", streamType = "live", isFavorite = true),
                StreamEntity(playlistId = playlistId, streamId = 15L, categoryId = "1", name = "[01] VIP | beIN Sports Premium 2 FHD (Server 1)", streamType = "live", isFavorite = true),
                StreamEntity(playlistId = playlistId, streamId = 16L, categoryId = "1", name = "FR | beIN Sports Max 2 FHD", streamType = "live", isFavorite = true)
            )
        )

        // Case A0: Provider channel 'beIN Sports max 5' recognizes 'beIN Sports max 05', 'beIN Sports 5 max', 'beIN Sports 05 max', and 'beIN Sports Max 5'
        val matchBeinMax5 = footballRepository.findMenaChannelsWithFavoritesPriority(
            playlistId = playlistId,
            match = resolvedUclGames[2],
            overrideChannelLabel = "beIN Sports max 5"
        )
        assertEquals("beIN Sports max 5", matchBeinMax5.resolvedChannelLabel)
        assertTrue(matchBeinMax5.availableMenaChannelOptions.contains("beIN Sports max 5"))
        // None of the duplicate equivalent variations ('beIN Sports 5 Max') should clutter options alongside 'beIN Sports max 5'
        assertEquals(1, matchBeinMax5.availableMenaChannelOptions.count {
            footballRepository.areEquivalentMenaChannels(it, "beIN Sports max 5")
        })
        assertEquals(setOf(9L, 10L, 11L, 12L), matchBeinMax5.favoriteMatches.map { it.streamId }.toSet())

        // Case A0b: Standard "beIN Sports 2 HD" matches "AR| BEIN SPORTS 02 HD [MP4A-LATM]" and "[01] VIP | beIN Sports Premium 2 FHD (Server 1)",
        // and NEVER falsely matches "FR | beIN Sports Max 2 FHD"
        val matchBein2Standard = footballRepository.findMenaChannelsWithFavoritesPriority(
            playlistId = playlistId,
            match = resolvedUclGames[1],
            overrideChannelLabel = "beIN Sports 2 HD"
        )
        assertEquals(listOf(15L, 14L), matchBein2Standard.favoriteMatches.map { it.streamId })
        assertTrue(matchBein2Standard.optionsAvailableInFavorites.any { it.equals("beIN Sports 2 HD", ignoreCase = true) })

        // Case A1: "beIN Sports 1 4K" must ONLY match channel 1 (4K and FHD), NEVER 10, 11, or 4
        val matchBein1 = footballRepository.findMenaChannelsWithFavoritesPriority(
            playlistId = playlistId,
            match = merged,
            overrideChannelLabel = "beIN Sports 1 4K"
        )
        assertEquals(listOf(1L, 2L), matchBein1.favoriteMatches.map { it.streamId })
        assertTrue(matchBein1.livePlaylistCandidates.isEmpty())

        // Case A2: "beIN Sports 4 Max" falls back to standard channel 4 ("AR: beIN Sports 4 HD") when no Max 4 is in Favorites
        val matchBein4 = footballRepository.findMenaChannelsWithFavoritesPriority(
            playlistId = playlistId,
            match = merged,
            overrideChannelLabel = "beIN Sports 4 Max"
        )
        assertEquals(listOf(5L), matchBein4.favoriteMatches.map { it.streamId })

        // Case A3: "Arryadia TNT" matches "MA: Al Riyadia TNT HD" via MENA alias normalization
        val matchArryadia = footballRepository.findMenaChannelsWithFavoritesPriority(
            playlistId = playlistId,
            match = merged,
            overrideChannelLabel = "Arryadia TNT"
        )
        assertEquals(listOf(6L), matchArryadia.favoriteMatches.map { it.streamId })

        // Case A4: "SSC 1 HD" matches "KSA: SSC Sport 1 UHD" via MENA alias normalization
        val matchSsc1 = footballRepository.findMenaChannelsWithFavoritesPriority(
            playlistId = playlistId,
            match = merged,
            overrideChannelLabel = "SSC 1 HD"
        )
        assertEquals(listOf(7L), matchSsc1.favoriteMatches.map { it.streamId })

        // Case B: "beIN Sports 3 HD" is not in Favorites (even though streamId=8 is in the Xtream catalog)
        val matchMissingBein3 = footballRepository.findMenaChannelsWithFavoritesPriority(
            playlistId = playlistId,
            match = merged,
            overrideChannelLabel = "beIN Sports 3 HD"
        )
        assertTrue(matchMissingBein3.favoriteMatches.isEmpty())
        assertTrue(matchMissingBein3.livePlaylistCandidates.isEmpty())

        // 6. Verify 5-Tab Match Center Dossier (Summary Timeline + HT/FT, 2D Pitch Lineups, Stats, H2H/Standings, MENA TV Guide)
        val dossier = footballRepository.fetchMatchFullDossier(merged)
        assertEquals("HT 1 - 0", dossier.halfTimeScoreLabel)
        assertEquals(11, dossier.lineups.homeStartingXI.size)
        assertEquals(11, dossier.lineups.awayStartingXI.size)
        assertTrue(dossier.lineups.homeFormation.isNotBlank())
        assertTrue(dossier.lineups.awayFormation.isNotBlank())
        assertTrue(dossier.lineups.homeBench.isNotEmpty())
        assertTrue(dossier.lineups.missingPlayers.isNotEmpty())
        assertTrue(dossier.timelineEvents.isNotEmpty())
        assertTrue(dossier.stats.any { it.statLabel == "Ball Possession" })
        assertTrue(dossier.stats.any { it.statLabel == "Expected Goals (xG)" })
        assertTrue(dossier.h2hMeetings.isNotEmpty())
        assertTrue(dossier.standingsTable.any { it.isHomeTeam })
        assertTrue(dossier.standingsTable.any { it.isAwayTeam })
        assertTrue(dossier.menaBroadcastGuide.isNotEmpty())

        // 7. Verify Canonical Tournament Order:
        // UEFA CL, La Liga, Premier League, Ligue 1, Bundesliga, Serie A, CAF CL, Botola Pro, then others
        assertEquals(
            listOf(
                "uefa_cl",
                "laliga",
                "premier_league",
                "ligue_1",
                "bundesliga",
                "serie_a",
                "caf_cl",
                "botola_pro",
                "uefa_el",
                "saudi_pro"
            ),
            com.example.data.repository.FootballRepository.ALL_TOURNAMENTS.map { it.tournamentId }
        )

        // 8. Verify Live-First + Chronological Kickoff Time Ordering
        val upcoming2100 = tsdbFixture.copy(
            matchId = "ucl_2100",
            tournamentId = "uefa_cl",
            status = com.example.data.repository.MatchStatus.UPCOMING,
            kickoffTimeFormatted = "21:00"
        )
        val upcoming1800 = tsdbFixture.copy(
            matchId = "botola_1800",
            tournamentId = "botola_pro",
            status = com.example.data.repository.MatchStatus.UPCOMING,
            kickoffTimeFormatted = "18:00"
        )
        val liveMatch = tsdbFixture.copy(
            matchId = "live_botola_2000",
            tournamentId = "botola_pro",
            status = com.example.data.repository.MatchStatus.LIVE,
            kickoffTimeFormatted = "20:00"
        )
        val sortedList = listOf(upcoming2100, upcoming1800, liveMatch).sortedWith(
            compareBy<com.example.data.repository.FootballMatch> {
                com.example.ui.home.getMatchStatusPriority(it.status)
            }.thenBy {
                com.example.ui.home.parseKickoffTimeToMinutes(it.kickoffTimeFormatted)
            }
        )
        assertEquals(listOf("live_botola_2000", "botola_1800", "ucl_2100"), sortedList.map { it.matchId })

        // 9. Verify SportAPI.ai Live Score & Match Minute Parser (including stoppage time, HT breakdown, red cards & scorers)
        val sampleSportApiLiveJson = """
            {
              "data": [
                {
                  "id": "99001",
                  "league": { "name": "LaLiga", "country": "Spain" },
                  "home_team": { "id": "h1", "name": "Athletic Club", "red_cards": 0 },
                  "away_team": { "id": "a1", "name": "Real Sociedad", "red_cards": 1 },
                  "status": { "short": "2H", "elapsed": 78, "extra": 0 },
                  "start_time": "2026-11-01T20:00:00Z",
                  "scores": {
                    "home": 2,
                    "away": 1,
                    "halftime": { "home": 1, "away": 0 }
                  },
                  "events": [
                    { "type": "Goal", "player": "I. Williams", "minute": "24" },
                    { "type": "Goal", "player": "M. Oyarzabal", "minute": "58" },
                    { "type": "Goal", "player": "O. Sancet", "minute": "73" }
                  ]
                }
              ]
            }
        """.trimIndent()

        val parsedSportApiLive = footballRepository.parseSportApiAiMatchesJson(
            rawJson = sampleSportApiLiveJson,
            dayTab = com.example.data.repository.MatchDayTab.TODAY
        )
        assertEquals(1, parsedSportApiLive.size)
        val liveAthletic = parsedSportApiLive.first()
        assertEquals(com.example.data.repository.MatchStatus.LIVE, liveAthletic.status)
        assertEquals("2H • 78'", liveAthletic.liveMinuteOrBadge)
        assertEquals(2, liveAthletic.homeScore)
        assertEquals(1, liveAthletic.awayScore)
        assertEquals("1 - 0", liveAthletic.halfTimeScore)
        assertEquals(1, liveAthletic.awayRedCards)
        assertTrue(liveAthletic.goalScorersSummary?.contains("Williams 24'") == true)

        // Verify stoppage time formatting in parseLiveStatusAndMinuteBadge
        val (stoppageStatus, stoppageBadge) = footballRepository.parseLiveStatusAndMinuteBadge(
            rawStatus = "2H",
            minute = 90,
            extraMinute = 4,
            kickoffTime = "20:00"
        )
        assertEquals(com.example.data.repository.MatchStatus.LIVE, stoppageStatus)
        assertEquals("2H • 90+4'", stoppageBadge)
    }
}

