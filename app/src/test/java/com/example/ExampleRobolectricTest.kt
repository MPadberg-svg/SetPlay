package com.example

import android.app.Application
import android.content.Context
import android.view.KeyEvent
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
import com.example.ui.player.handlePlayerRemoteKeyEvent
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

        // Case B: "beIN Sports 3 HD" is not in Favorites -> Tier 1 favoriteMatches is empty,
        // while Smart Tier 2 livePlaylistCandidates finds streamId=8 ("AR: beIN Sports 3 HD") in the Live TV catalog!
        val matchMissingBein3 = footballRepository.findMenaChannelsWithFavoritesPriority(
            playlistId = playlistId,
            match = merged,
            overrideChannelLabel = "beIN Sports 3 HD"
        )
        assertTrue(matchMissingBein3.favoriteMatches.isEmpty())
        assertEquals(listOf(8L), matchMissingBein3.livePlaylistCandidates.map { it.streamId })

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

        // 6b. Verify Upcoming Match Lineups (Fenerbahce vs Liverpool):
        // - Official lineups expected 1 hour before kick-off
        // - Real named Fenerbahce & Liverpool starters with avatar URLs (zero "#1" placeholders)
        // - No fabricated cards, goals, or substitution arrows before kick-off
        val upcomingFenerLiverpool = tsdbFixture.copy(
            matchId = "ucl_fener_liv",
            homeTeamName = "Fenerbahce",
            awayTeamName = "Liverpool",
            status = com.example.data.repository.MatchStatus.UPCOMING
        )
        val upcomingLineups = footballRepository.fetchMatchFullDossier(upcomingFenerLiverpool).lineups
        assertEquals(
            "Official lineups expected 1 hour before kick-off",
            upcomingLineups.lineupStatusHeadline
        )
        assertEquals(11, upcomingLineups.homeStartingXI.size)
        assertEquals(11, upcomingLineups.awayStartingXI.size)
        assertTrue(upcomingLineups.homeStartingXI.none { it.shortName.startsWith("#") })
        assertTrue(upcomingLineups.awayStartingXI.none { it.shortName.startsWith("#") })
        assertTrue(upcomingLineups.homeStartingXI.all { it.photoUrl.isNotBlank() })
        assertTrue(upcomingLineups.awayStartingXI.all { it.photoUrl.isNotBlank() })
        assertTrue(upcomingLineups.homeStartingXI.none { it.hasYellowCard || it.hasRedCard || it.subbedOffMinute != null })
        assertTrue(upcomingLineups.awayStartingXI.none { it.hasYellowCard || it.hasRedCard || it.subbedOffMinute != null })

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
            kickoffTimeFormatted = "21:00",
            referenceMoroccoTime = "21:00"
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

        // 10. Verify Canonical Club Name Deduplication (e.g. Feyenoord Rotterdam vs Feyenoord, Internazionale vs Inter Milan)
        assertTrue(com.example.data.repository.FootballRepository.areSameClubNames("Feyenoord Rotterdam", "Feyenoord"))
        assertTrue(com.example.data.repository.FootballRepository.areSameClubNames("FC Internazionale Milano", "Inter Milan"))
        assertTrue(com.example.data.repository.FootballRepository.areSameClubNames("Athletic Bilbao", "Athletic Club"))
        assertTrue(com.example.data.repository.FootballRepository.areSameClubNames("Paris Saint-Germain", "PSG"))

        // 11. Verify Live Match Progress Fraction & 3-Tier Clock Telemetry Parser
        val progress86 = com.example.ui.home.parseLiveMatchProgressFraction("2H • 86'", com.example.data.repository.MatchStatus.LIVE)
        assertEquals(86f / 90f, progress86, 0.005f)
        val progress41 = com.example.ui.home.parseLiveMatchProgressFraction("1H • 41'", com.example.data.repository.MatchStatus.LIVE)
        assertEquals(41f / 90f, progress41, 0.005f)

        val sampleLiveMatch86 = liveAthletic.copy(
            liveMinuteOrBadge = "2H • 86'",
            halfTimeScore = "0 - 1"
        )
        val telemetry86 = com.example.ui.home.parseLiveMatchClockTelemetry(sampleLiveMatch86)
        assertEquals("2ND HALF", telemetry86.periodBadge)
        assertEquals("86'", telemetry86.heroReadout)
        assertEquals("HT 0 - 1", telemetry86.bottomBadge)
        assertEquals("2ND HALF • 86' PLAYED", telemetry86.statusExplanation)

        // 12. Verify Villarreal vs PSG Lineup Squads, Player Photos, MVP flag, and Team Average Ratings
        val villarrealMatch = tsdbFixture.copy(
            matchId = "ucl_vil_psg",
            tournamentId = "uefa_cl",
            homeTeamName = "Villarreal",
            awayTeamName = "Paris Saint-Germain",
            status = com.example.data.repository.MatchStatus.LIVE,
            homeScore = 0,
            awayScore = 4,
            liveMinuteOrBadge = "1H • 38'"
        )
        val vilDossier = com.example.data.repository.MatchCenterEnricher(okhttp3.OkHttpClient()).buildCompleteDossier(villarrealMatch)
        assertEquals("Luiz Júnior", vilDossier.lineups.homeStartingXI.first().name)
        assertTrue(vilDossier.lineups.awayStartingXI.any { it.name.contains("Hakimi") && it.photoUrl.startsWith("https://") })
        assertTrue((vilDossier.lineups.homeStartingXI + vilDossier.lineups.awayStartingXI).count { it.isMvp } == 1)
        assertTrue(vilDossier.lineups.homeAvgRating >= 6.0)
        assertTrue(vilDossier.lineups.awayAvgRating >= 6.0)

        // 13. Verify Kinetic 5-Day Broadcast Chrono-Stage Calendar Helpers on MatchDayTab
        assertEquals("TODAY", com.example.data.repository.MatchDayTab.TODAY.getShortDayCode())
        assertEquals("YEST", com.example.data.repository.MatchDayTab.YESTERDAY.getShortDayCode())
        assertEquals("TOM", com.example.data.repository.MatchDayTab.TOMORROW.getShortDayCode())
        assertEquals(2, com.example.data.repository.MatchDayTab.TODAY.getDayOfMonthNumber().length)
        assertEquals(3, com.example.data.repository.MatchDayTab.TODAY.getMonthShortUpper().length)
        assertTrue(com.example.data.repository.MatchDayTab.DAY_PLUS_2.getWeekdayFullUpper().isNotBlank())

        // 14. Verify THE USER'S ZONE: Country Selection, Dual-Season (Winter/Summer) Timezone Kickoff Conversion & Official TV Broadcaster Mapping
        assertEquals("MA", footballRepository.getSelectedCountryZone().countryCode)

        // Switch User's Zone to France (FR) -> Canal+ Foot for UEFA Champions League
        // France Winter Time (CET • GMT+1 = 21:00) vs Summer Time (CEST • GMT+2 = 22:00)
        val franceZone = footballRepository.setSelectedCountryZone("FR")
        assertEquals("FR", franceZone.countryCode)
        assertEquals("France", franceZone.countryName)
        assertTrue(franceZone.hasSeasonalTimeChange)
        val frWinter = footballRepository.reapplyCountryZoneToMatches(
            listOf(upcoming2100),
            franceZone,
            com.example.data.repository.SeasonTimeMode.WINTER
        ).first()
        assertTrue(frWinter.beinChannel.contains("Canal+", ignoreCase = true))
        assertEquals("Canal+ (France)", frWinter.menaProviderName)
        assertEquals("21:00", frWinter.kickoffTimeFormatted)
        assertEquals("GMT+1", franceZone.getUtcOffsetBadge(mode = com.example.data.repository.SeasonTimeMode.WINTER))

        val frSummer = footballRepository.reapplyCountryZoneToMatches(
            listOf(upcoming2100),
            franceZone,
            com.example.data.repository.SeasonTimeMode.SUMMER
        ).first()
        assertEquals("22:00", frSummer.kickoffTimeFormatted)
        assertEquals("GMT+2", franceZone.getUtcOffsetBadge(mode = com.example.data.repository.SeasonTimeMode.SUMMER))

        // Switch User's Zone to United Kingdom (GB) -> TNT Sports 1 HD
        // UK Winter Time (GMT+0 = 20:00) vs UK Summer Time (BST • GMT+1 = 21:00)
        val ukZone = footballRepository.setSelectedCountryZone("GB")
        assertEquals("GB", ukZone.countryCode)
        val ukWinter = footballRepository.reapplyCountryZoneToMatches(
            listOf(upcoming2100),
            ukZone,
            com.example.data.repository.SeasonTimeMode.WINTER
        ).first()
        assertTrue(ukWinter.beinChannel.contains("TNT Sports", ignoreCase = true))
        assertEquals("20:00", ukWinter.kickoffTimeFormatted)

        val ukSummer = footballRepository.reapplyCountryZoneToMatches(
            listOf(upcoming2100),
            ukZone,
            com.example.data.repository.SeasonTimeMode.SUMMER
        ).first()
        assertEquals("21:00", ukSummer.kickoffTimeFormatted)

        // Switch User's Zone to Egypt (EG) -> Winter Time (EET • GMT+2 = 22:00) vs Summer Time (EEST • GMT+3 = 23:00)
        val egyptZone = footballRepository.setSelectedCountryZone("EG")
        assertEquals(
            "22:00",
            egyptZone.convertMoroccoTimeToZoneTime("21:00", com.example.data.repository.SeasonTimeMode.WINTER)
        )
        assertEquals(
            "23:00",
            egyptZone.convertMoroccoTimeToZoneTime("21:00", com.example.data.repository.SeasonTimeMode.SUMMER)
        )

        // Switch User's Zone to Saudi Arabia (SA, Permanent GMT+3) -> 23:00 kickoff (converted from 21:00 GMT+1)
        val saudiZone = footballRepository.setSelectedCountryZone("SA")
        assertEquals("SA", saudiZone.countryCode)
        val saRelocalized = footballRepository.reapplyCountryZoneToMatches(listOf(upcoming2100), saudiZone).first()
        assertEquals("23:00", saRelocalized.kickoffTimeFormatted)
        assertEquals("GMT+3", saudiZone.getUtcOffsetBadge())

        // Restore default Morocco (MA) zone & verify Morocco Standard/Summer (GMT+1 = 21:00) vs Ramadan/Winter (GMT+0 = 20:00)
        val moroccoZone = footballRepository.setSelectedCountryZone("MA")
        footballRepository.setSelectedSeasonTimeMode(com.example.data.repository.SeasonTimeMode.AUTO)
        assertEquals("MA", footballRepository.getSelectedCountryZone().countryCode)
        assertEquals("21:00", moroccoZone.convertMoroccoTimeToZoneTime("21:00", com.example.data.repository.SeasonTimeMode.SUMMER))
        assertEquals("20:00", moroccoZone.convertMoroccoTimeToZoneTime("21:00", com.example.data.repository.SeasonTimeMode.WINTER))
    }

    @Test
    fun `test StreamMetadataParser cleanDisplayTitle, region badge, quality badge, and AAC audio codec mapping`() {
        // 1. Clean Display Title
        assertEquals(
            "Saw X",
            com.example.util.StreamMetadataParser.cleanDisplayTitle("ES| Saw X")
        )
        assertEquals(
            "La super risa en vacaciones 8",
            com.example.util.StreamMetadataParser.cleanDisplayTitle("ES| La super risa en vacaciones 8")
        )
        assertEquals(
            "beIN Sports 01",
            com.example.util.StreamMetadataParser.cleanDisplayTitle("AR| BEIN SPORTS 01 4K")
        )
        assertEquals(
            "beIN Sports 02",
            com.example.util.StreamMetadataParser.cleanDisplayTitle("[01] AR | BEIN SPORTS 02 FHD HEVC")
        )
        assertEquals(
            "Dune: Part Two",
            com.example.util.StreamMetadataParser.cleanDisplayTitle("FR┃ Dune: Part Two [4K UHD]")
        )

        // 2. Region / Language Badge Extraction
        assertEquals("ES", com.example.util.StreamMetadataParser.extractRegionOrLangBadge("ES| Saw X"))
        assertEquals("AR", com.example.util.StreamMetadataParser.extractRegionOrLangBadge("[01] AR | BEIN SPORTS 02 FHD HEVC"))
        assertEquals("FR", com.example.util.StreamMetadataParser.extractRegionOrLangBadge("FR┃ Dune: Part Two [4K UHD]"))
        assertEquals("EN", com.example.util.StreamMetadataParser.extractRegionOrLangBadge("EN| Hard Knocks: Training Camp"))
        assertNull(com.example.util.StreamMetadataParser.extractRegionOrLangBadge("Inception"))

        // 3. Authentic Quality Badge Extraction (never fabricates 4K when absent)
        assertEquals("4K UHD", com.example.util.StreamMetadataParser.extractQualityBadge("FR┃ Dune: Part Two [4K UHD]"))
        assertEquals("1080p FHD", com.example.util.StreamMetadataParser.extractQualityBadge("[01] AR | BEIN SPORTS 02 FHD HEVC"))
        assertEquals("HD", com.example.util.StreamMetadataParser.extractQualityBadge("AR: beIN Sports 4 HD"))
        assertNull(com.example.util.StreamMetadataParser.extractQualityBadge("ES| La super risa en vacaciones 8"))

        // 4. Audio Codec Badge (MP4A-LATM -> AAC, AC-4 -> AC4, DTS -> DTS)
        val app = ApplicationProvider.getApplicationContext<Application>()
        val playerViewModel = PlayerViewModel(app)
        assertEquals("AAC", playerViewModel.formatMimeToCodecBadge("audio/mp4a-latm"))
        assertEquals("AAC", playerViewModel.formatMimeToCodecBadge("audio/aac"))
        assertEquals("AC4", playerViewModel.formatMimeToCodecBadge("audio/ac-4"))
        assertEquals("DTS", playerViewModel.formatMimeToCodecBadge("audio/vnd.dts"))
    }

    @Test
    fun `test Parental Controls multi-section category locks and re-sync lock restoration`() = runBlocking {
        val repository = IptvRepository(
            playlistDao = database.playlistDao(),
            categoryDao = database.categoryDao(),
            streamDao = database.streamDao(),
            epgDao = database.epgDao(),
            parentalDao = database.parentalSettingsDao()
        )
        val playlistId = repository.addOrUpdatePlaylist(
            PlaylistEntity(
                name = "Family Vault Account",
                serverUrl = "https://iptv.setplay.io",
                username = "family_user",
                password = "secret",
                isActive = true
            )
        )

        // Insert Live TV, Movie, and Series categories
        val initialCategories = listOf(
            CategoryEntity(playlistId = playlistId, categoryId = "live_adult", categoryName = "18+ Live Channels", type = "LIVE"),
            CategoryEntity(playlistId = playlistId, categoryId = "live_sports", categoryName = "AR| beIN Sports", type = "LIVE"),
            CategoryEntity(playlistId = playlistId, categoryId = "vod_horror", categoryName = "ES| Horror & Thriller", type = "MOVIE"),
            CategoryEntity(playlistId = playlistId, categoryId = "series_mature", categoryName = "FR| Mature Drama", type = "SERIES")
        )
        database.categoryDao().insertCategories(initialCategories)

        // Enable Parental Lock and lock one category in each section (LIVE, MOVIE, SERIES)
        repository.toggleParentalEnabled(true)
        repository.setCategoryLocked(playlistId, "live_adult", true)
        repository.setCategoryLocked(playlistId, "vod_horror", true)
        repository.setCategoryLocked(playlistId, "series_mature", true)

        assertTrue(repository.isCategoryLocked("live_adult"))
        assertFalse(repository.isCategoryLocked("live_sports"))
        assertTrue(repository.isCategoryLocked("vod_horror"))
        assertTrue(repository.isCategoryLocked("series_mature"))

        // Simulate a playlist re-sync where XtreamClient inserts fresh CategoryEntity instances with isLocked = false
        database.categoryDao().insertCategories(initialCategories)
        // Before restoration, Room REPLACE resets CategoryEntity.isLocked to false
        assertFalse(repository.getCategories(playlistId, "LIVE").first().first { it.categoryId == "live_adult" }.isLocked)

        // Restore locked categories from ParentalSettingsEntity.lockedCategoriesJson
        repository.restoreLockedCategories(playlistId)

        val liveCatsAfterSync = repository.getCategories(playlistId, "LIVE").first()
        val movieCatsAfterSync = repository.getCategories(playlistId, "MOVIE").first()
        val seriesCatsAfterSync = repository.getCategories(playlistId, "SERIES").first()

        assertTrue(liveCatsAfterSync.first { it.categoryId == "live_adult" }.isLocked)
        assertFalse(liveCatsAfterSync.first { it.categoryId == "live_sports" }.isLocked)
        assertTrue(movieCatsAfterSync.first { it.categoryId == "vod_horror" }.isLocked)
        assertTrue(seriesCatsAfterSync.first { it.categoryId == "series_mature" }.isLocked)
    }

    @Test
    fun `test progressive catalog paging beyond 180 items and independent per-episode series resume tracking`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val app = ApplicationProvider.getApplicationContext<Application>()
        IptvRepository.initEpisodePreferences(context)
        IptvRepository.resetEpisodePreferencesForTesting()

        val repository = IptvRepository(
            playlistDao = database.playlistDao(),
            categoryDao = database.categoryDao(),
            streamDao = database.streamDao(),
            epgDao = database.epgDao(),
            parentalDao = database.parentalSettingsDao()
        )
        val playlist = PlaylistEntity(
            name = "Cinema & Series Scale Account",
            serverUrl = "https://iptv.setplay.io",
            username = "scale_user",
            password = "secret",
            isActive = true
        )
        val playlistId = repository.addOrUpdatePlaylist(playlist)
        val storedPlaylist = repository.getPlaylistById(playlistId)!!

        // 1. Insert 420 movies into Room and verify deterministic progressive paging (180 -> 360 -> 420)
        val moviesBatch = (1..420).map { idx ->
            StreamEntity(
                playlistId = playlistId,
                streamId = 10_000L + idx,
                categoryId = if (idx <= 250) "cat_action" else "cat_drama",
                name = "Cinema Feature #%03d".format(idx),
                streamType = "movie"
            )
        }
        database.streamDao().insertStreamsBatch(moviesBatch)

        val page1 = repository.queryStreamsFast(playlistId, "movie", null, "", limit = 180).first()
        assertEquals(180, page1.size)
        assertEquals("Cinema Feature #001", page1.first().name)
        assertEquals("Cinema Feature #180", page1.last().name)

        val page2 = repository.queryStreamsFast(playlistId, "movie", null, "", limit = 360).first()
        assertEquals(360, page2.size)
        assertEquals("Cinema Feature #001", page2.first().name)
        assertEquals("Cinema Feature #360", page2.last().name)

        val page3 = repository.queryStreamsFast(playlistId, "movie", null, "", limit = 540).first()
        assertEquals(420, page3.size)
        assertEquals("Cinema Feature #420", page3.last().name)

        // Verify category-filtered paging
        val catPage1 = repository.queryStreamsFast(playlistId, "movie", "cat_action", "", limit = 180).first()
        val catPage2 = repository.queryStreamsFast(playlistId, "movie", "cat_action", "", limit = 360).first()
        assertEquals(180, catPage1.size)
        assertEquals(250, catPage2.size)

        // Verify LiveTvViewModel and VodViewModel progressive paging & reset on category/query change
        val liveTvVm = com.example.ui.livetv.LiveTvViewModel(app)
        assertEquals(180, liveTvVm.currentLimit.value)
        liveTvVm.loadNextPage()
        assertEquals(360, liveTvVm.currentLimit.value)
        liveTvVm.onChannelFilterQueryChanged("beIN")
        assertEquals(180, liveTvVm.currentLimit.value)

        val vodVm = com.example.ui.vod.VodViewModel(app)
        assertEquals(180, vodVm.currentLimit.value)
        vodVm.loadNextPage()
        assertEquals(360, vodVm.currentLimit.value)
        vodVm.selectMovieCategory("cat_action")
        assertEquals(180, vodVm.currentLimit.value)
        vodVm.loadNextPage()
        assertEquals(360, vodVm.currentLimit.value)
        vodVm.onMovieSearchQueryChanged("Feature")
        assertEquals(180, vodVm.currentLimit.value)

        // 2. Verify Series per-episode resume tracking keyed by "${playlistId}_ep_${episodeId}"
        val seriesId = database.streamDao().let { dao ->
            dao.insertStreamsBatch(
                listOf(
                    StreamEntity(
                        playlistId = playlistId,
                        streamId = 900L,
                        categoryId = "series_vip",
                        name = "The Last of Us",
                        streamType = "series"
                    )
                )
            )
            dao.getStreamByProviderId(playlistId, 900L)!!.id
        }

        val ep1Stream = StreamEntity(
            id = seriesId,
            playlistId = playlistId,
            streamId = 5001L, // Episode 1 episodeId
            categoryId = "series_vip",
            name = "The Last of Us • S1E1 - When You're Lost in the Darkness",
            streamType = "series",
            epgChannelId = "900:1:1",
            containerExtension = "mp4"
        )
        val ep2Stream = StreamEntity(
            id = seriesId,
            playlistId = playlistId,
            streamId = 5002L, // Episode 2 episodeId
            categoryId = "series_vip",
            name = "The Last of Us • S1E2 - Infected",
            streamType = "series",
            epgChannelId = "900:1:2",
            containerExtension = "mp4"
        )

        val playerViewModel = PlayerViewModel(app)

        // Play Episode 1 and scrub to 18 minutes (1,080,000 ms)
        playerViewModel.playStream(ep1Stream, storedPlaylist)
        playerViewModel.seekToPosition(1_080_000L)
        assertEquals(1_080_000L, repository.getEpisodePositionMs(playlistId, 5001L))
        assertEquals(0L, repository.getEpisodePositionMs(playlistId, 5002L))

        // Play Episode 2 -> starts fresh at 0ms (does NOT collide with Episode 1's 18m position), then scrub to 7 minutes (420,000 ms)
        playerViewModel.playStream(ep2Stream, storedPlaylist)
        assertEquals(0L, playerViewModel.uiState.value.currentPositionMs)
        playerViewModel.seekToPosition(420_000L)
        assertEquals(420_000L, repository.getEpisodePositionMs(playlistId, 5002L))
        // Episode 1's saved resume position remains intact at 1,080,000 ms
        assertEquals(1_080_000L, repository.getEpisodePositionMs(playlistId, 5001L))

        // Re-open Episode 1 -> resumes at 1,080,000 ms
        playerViewModel.playStream(ep1Stream, storedPlaylist)
        assertEquals(1_080_000L, playerViewModel.uiState.value.currentPositionMs)
        assertEquals(1_080_000L, playerViewModel.uiState.value.currentStream?.lastPositionMs)
    }

    @Test
    fun `test Android TV remote keybindings for channel zapping, VOD seeking, and play pause`() {
        var centerTriggeredCount = 0
        var playPauseTriggeredCount = 0
        val zappedOffsets = mutableListOf<Int>()
        val seekDeltas = mutableListOf<Int>()

        // 1. Live TV with hidden controls: DPAD_UP / DPAD_DOWN / CHANNEL_UP / CHANNEL_DOWN zap channels
        assertTrue(
            handlePlayerRemoteKeyEvent(
                keyCode = KeyEvent.KEYCODE_DPAD_UP,
                isLive = true,
                isControlsVisible = false,
                isTouchLocked = false,
                onShowControlsOrTogglePlay = { centerTriggeredCount++ },
                onTogglePlayPause = { playPauseTriggeredCount++ },
                onZapChannel = { zappedOffsets.add(it) },
                onSeekRelative = { seekDeltas.add(it) }
            )
        )
        assertTrue(
            handlePlayerRemoteKeyEvent(
                keyCode = KeyEvent.KEYCODE_CHANNEL_DOWN,
                isLive = true,
                isControlsVisible = true,
                isTouchLocked = false,
                onShowControlsOrTogglePlay = { centerTriggeredCount++ },
                onTogglePlayPause = { playPauseTriggeredCount++ },
                onZapChannel = { zappedOffsets.add(it) },
                onSeekRelative = { seekDeltas.add(it) }
            )
        )
        assertEquals(listOf(1, -1), zappedOffsets)

        // 2. VOD mode: DPAD_LEFT / MEDIA_REWIND seek -10s, DPAD_RIGHT / MEDIA_FAST_FORWARD seek +10s
        assertTrue(
            handlePlayerRemoteKeyEvent(
                keyCode = KeyEvent.KEYCODE_DPAD_LEFT,
                isLive = false,
                isControlsVisible = false,
                isTouchLocked = false,
                onShowControlsOrTogglePlay = { centerTriggeredCount++ },
                onTogglePlayPause = { playPauseTriggeredCount++ },
                onZapChannel = { zappedOffsets.add(it) },
                onSeekRelative = { seekDeltas.add(it) }
            )
        )
        assertTrue(
            handlePlayerRemoteKeyEvent(
                keyCode = KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
                isLive = false,
                isControlsVisible = false,
                isTouchLocked = false,
                onShowControlsOrTogglePlay = { centerTriggeredCount++ },
                onTogglePlayPause = { playPauseTriggeredCount++ },
                onZapChannel = { zappedOffsets.add(it) },
                onSeekRelative = { seekDeltas.add(it) }
            )
        )
        assertEquals(listOf(-10, 10), seekDeltas)

        // 3. DPAD_CENTER and MEDIA_PLAY_PAUSE trigger controls/play-pause
        assertTrue(
            handlePlayerRemoteKeyEvent(
                keyCode = KeyEvent.KEYCODE_DPAD_CENTER,
                isLive = false,
                isControlsVisible = false,
                isTouchLocked = false,
                onShowControlsOrTogglePlay = { centerTriggeredCount++ },
                onTogglePlayPause = { playPauseTriggeredCount++ },
                onZapChannel = { zappedOffsets.add(it) },
                onSeekRelative = { seekDeltas.add(it) }
            )
        )
        assertTrue(
            handlePlayerRemoteKeyEvent(
                keyCode = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                isLive = false,
                isControlsVisible = false,
                isTouchLocked = false,
                onShowControlsOrTogglePlay = { centerTriggeredCount++ },
                onTogglePlayPause = { playPauseTriggeredCount++ },
                onZapChannel = { zappedOffsets.add(it) },
                onSeekRelative = { seekDeltas.add(it) }
            )
        )
        assertEquals(1, centerTriggeredCount)
        assertEquals(1, playPauseTriggeredCount)
    }
}





