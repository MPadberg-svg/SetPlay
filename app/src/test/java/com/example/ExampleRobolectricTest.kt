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
            sportsDbEventId = "",
            goalScorersSummary = "Y. Bouzok 24', M. Niang 39', N. Zerhouni 61'"
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

        // 6. Verify 5-Tab Match Center Dossier (Summary Timeline + HT/FT, 3D Pitch Lineups, Stats, Honest Empty H2H/Standings when offline, MENA TV Guide)
        val dossier = footballRepository.fetchMatchFullDossier(merged)
        assertEquals("HT 1 - 0", dossier.halfTimeScoreLabel)
        assertEquals(11, dossier.lineups.homeStartingXI.size)
        assertEquals(11, dossier.lineups.awayStartingXI.size)
        assertTrue(dossier.lineups.homeFormation.isNotBlank())
        assertTrue(dossier.lineups.awayFormation.isNotBlank())
        assertTrue(dossier.lineups.homeBench.isNotEmpty())
        assertTrue(dossier.lineups.missingPlayers.isEmpty())
        assertEquals(3, dossier.timelineEvents.size)
        assertTrue(dossier.timelineEvents.all { it.eventType == com.example.data.repository.MatchEventType.GOAL })
        assertEquals("TBA / Official Broadcast Feed", dossier.refereeName)
        assertEquals("TBA / Official Broadcast Feed", dossier.attendanceLabel)
        assertTrue(dossier.stats.isEmpty())
        assertTrue(dossier.menaBroadcastGuide.isNotEmpty())

        // Verify Single-Call Team Squad Cutout Matching & Honest Empty Stats/H2H/Standings when upstream JSON is absent
        val enricher = com.example.data.repository.MatchCenterEnricher(okhttp3.OkHttpClient())
        val offlineDossier = enricher.buildCompleteDossier(merged, raw = null)
        assertTrue(offlineDossier.stats.isEmpty())
        assertTrue(offlineDossier.h2hMeetings.isEmpty())
        assertTrue(offlineDossier.standingsTable.isEmpty())
        assertEquals("TBA / Official Broadcast Feed", offlineDossier.refereeName)
        assertEquals("TBA / Official Broadcast Feed", offlineDossier.attendanceLabel)
        val mockSquad = listOf(
            com.example.data.repository.MatchCenterEnricher.RawPlayerEntry(
                name = "Vinicius Junior",
                jersey = 7,
                positionAbbr = "LW",
                photoUrl = "https://r2.thesportsdb.com/images/media/player/cutout/vini_cutout.png"
            ),
            com.example.data.repository.MatchCenterEnricher.RawPlayerEntry(
                name = "Kylian Mbappe",
                jersey = 9,
                positionAbbr = "ST",
                photoUrl = "https://r2.thesportsdb.com/images/media/player/cutout/mbappe_cutout.png"
            )
        )
        assertEquals(
            "https://r2.thesportsdb.com/images/media/player/cutout/vini_cutout.png",
            enricher.resolvePlayerPhotoFromSquad("Vinícius Júnior", mockSquad)
        )
        assertEquals(
            "https://r2.thesportsdb.com/images/media/player/cutout/mbappe_cutout.png",
            enricher.resolvePlayerPhotoFromSquad("K. Mbappé", mockSquad)
        )

        // 6b. Verify Upcoming Match Lineups (Fenerbahce vs Liverpool):
        // - Official lineups expected 1 hour before kick-off
        // - Real named Fenerbahce & Liverpool starters with avatar URLs (zero "#1" placeholders, zero dead espncdn /players/full/ URLs)
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
        assertTrue(upcomingLineups.homeStartingXI.none { it.photoUrl.contains("espncdn.com/i/headshots/soccer/players/full") })
        assertTrue(upcomingLineups.awayStartingXI.none { it.photoUrl.contains("espncdn.com/i/headshots/soccer/players/full") })
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

    @Test
    fun `test Sofascore country-channels TV parser, short-alias TV matching, playlist match-named channel resolution, and 30 req per min rate limit`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val footballRepository = com.example.data.repository.FootballRepository(
            context = context,
            favoriteTeamDao = database.favoriteTeamDao(),
            tournamentPreferenceDao = database.tournamentPreferenceDao(),
            streamDao = database.streamDao()
        )

        // 1. Verify THESPORTSDB_MAX_REQ_PER_MINUTE is set to 30
        assertEquals(30, com.example.data.repository.FootballRepository.THESPORTSDB_MAX_REQ_PER_MINUTE)

        // 2. Verify doesTvEntryMatchFixtureTeams matches short club aliases ("PSG", "Spurs", "Man Utd", "Inter", "Gladbach", "Raja", "Wydad", "AS FAR")
        val entries = listOf(
            com.example.data.repository.SportsDbTvBroadcastEntry(
                eventId = "1",
                eventTitleNormalized = footballRepository.normalizeTeamNameForMerge("PSG vs Inter"),
                eventTitleRaw = "PSG vs Inter",
                channelName = "beIN Sports 1 HD",
                country = "Qatar"
            ),
            com.example.data.repository.SportsDbTvBroadcastEntry(
                eventId = "2",
                eventTitleNormalized = footballRepository.normalizeTeamNameForMerge("Spurs v Man Utd"),
                eventTitleRaw = "Spurs v Man Utd",
                channelName = "beIN Sports 2 HD",
                country = "Qatar"
            ),
            com.example.data.repository.SportsDbTvBroadcastEntry(
                eventId = "3",
                eventTitleNormalized = footballRepository.normalizeTeamNameForMerge("Gladbach - Bayern Munich"),
                eventTitleRaw = "Gladbach - Bayern Munich",
                channelName = "beIN Sports 5 HD",
                country = "MENA"
            ),
            com.example.data.repository.SportsDbTvBroadcastEntry(
                eventId = "4",
                eventTitleNormalized = footballRepository.normalizeTeamNameForMerge("Raja vs Wydad"),
                eventTitleRaw = "Raja vs Wydad",
                channelName = "Arryadia HD",
                country = "Morocco"
            ),
            com.example.data.repository.SportsDbTvBroadcastEntry(
                eventId = "5",
                eventTitleNormalized = footballRepository.normalizeTeamNameForMerge("AS FAR vs RS Berkane"),
                eventTitleRaw = "AS FAR vs RS Berkane",
                channelName = "Tamazight HD",
                country = "Morocco"
            )
        )
        assertTrue(footballRepository.doesTvEntryMatchFixtureTeams(entries[0], "Paris Saint-Germain", "FC Internazionale Milano"))
        assertTrue(footballRepository.doesTvEntryMatchFixtureTeams(entries[1], "Tottenham Hotspur", "Manchester United"))
        assertTrue(footballRepository.doesTvEntryMatchFixtureTeams(entries[2], "Borussia Mönchengladbach", "FC Bayern München"))
        assertTrue(footballRepository.doesTvEntryMatchFixtureTeams(entries[3], "Raja Club Athletic", "Wydad AC"))
        assertTrue(footballRepository.doesTvEntryMatchFixtureTeams(entries[4], "AS FAR Rabat", "RSB Berkane"))

        // 3. Verify Sofascore country-channels JSON parser resolves channels for user's selected UserCountryZone
        val sampleSofascoreTvJson = """
            {
              "countryChannels": {
                "MA": [
                  { "name": "beIN Sports HD 1" },
                  { "name": "beIN 4K" }
                ],
                "FR": [
                  { "name": "Canal+ Foot" }
                ],
                "ES": [
                  { "name": "Movistar Liga de Campeones" }
                ]
              }
            }
        """.trimIndent()
        val maZone = com.example.data.repository.UserZoneRegistry.getZoneByCode("MA")
        val frZone = com.example.data.repository.UserZoneRegistry.getZoneByCode("FR")
        val maChannels = com.example.data.repository.FootballRepository.parseSofascoreCountryChannelsJson(sampleSofascoreTvJson, maZone)
        val frChannels = com.example.data.repository.FootballRepository.parseSofascoreCountryChannelsJson(sampleSofascoreTvJson, frZone)
        assertTrue(maChannels.any { it.contains("beIN Sports", ignoreCase = true) })
        assertTrue(frChannels.any { it.contains("Canal+", ignoreCase = true) })

        // 4. Verify resolveDayMatchesTvChannelsWithMenaPriority prioritizes live playlist channels that explicitly name the fixture
        val playlistId = database.playlistDao().insertPlaylist(
            PlaylistEntity(
                name = "MENA Sports Vault",
                serverUrl = "https://iptv.setplay.io",
                username = "sports_user",
                password = "secret",
                isActive = true
            )
        )
        database.streamDao().insertStreamsBatch(
            listOf(
                StreamEntity(
                    playlistId = playlistId,
                    streamId = 7701L,
                    categoryId = "cat_bein",
                    name = "beIN Sports 2 HD : Real Madrid vs Milan",
                    streamType = "live"
                )
            )
        )

        val realMilanMatch = com.example.data.repository.FootballMatch(
            matchId = "ucl_rm_mil",
            tournamentId = "uefa_cl",
            tournamentName = "UEFA Champions League",
            tournamentRegion = "Europe",
            dayTab = com.example.data.repository.MatchDayTab.TODAY,
            kickoffTimeFormatted = "21:00",
            kickoffDateFormatted = "Today • GMT+1",
            status = com.example.data.repository.MatchStatus.UPCOMING,
            liveMinuteOrBadge = "21:00",
            homeTeamId = "real_madrid",
            homeTeamName = "Real Madrid",
            homeTeamBadge = "",
            homeScore = null,
            awayTeamId = "ac_milan",
            awayTeamName = "AC Milan",
            awayTeamBadge = "",
            awayScore = null,
            stadiumName = "Santiago Bernabéu",
            cityName = "Madrid",
            beinChannel = "beIN Sports 1 HD",
            beinChannelNumberHint = "beIN 1",
            fallbackMenaChannels = listOf("beIN Sports 1 HD"),
            menaProviderName = "beIN Sports MENA"
        )

        val resolvedMatches = footballRepository.resolveDayMatchesTvChannelsWithMenaPriority(
            listOf(realMilanMatch),
            emptyList(),
            maZone
        )
        assertEquals("beIN Sports 2 HD", resolvedMatches.first().beinChannel)
    }

    @Test
    fun testPhase3SubtitlesAutoPromptOpenSubtitlesFallbackAndTitleCleaning() = runBlocking {
        val subtitleService = com.example.data.remote.WyzieSubtitleService()

        // 1. Verify cleanMediaTitle() strips 2-4 letter IPTV tags with | or ┃ or brackets,
        //    and NEVER strips real movie title words before a colon or hyphen!
        assertEquals(
            "Saw X: Jigsaw" to 2023,
            subtitleService.cleanMediaTitle("AR| Saw X: Jigsaw (2023) [4K]")
        )
        assertEquals(
            "Mission: Impossible - Dead Reckoning" to null,
            subtitleService.cleanMediaTitle("[VIP] EN ┃ Mission: Impossible - Dead Reckoning 1080p")
        )
        assertEquals(
            "Spider-Man: Across the Spider-Verse" to 2023,
            subtitleService.cleanMediaTitle("Spider-Man: Across the Spider-Verse (2023)")
        )

        // 2. Verify OpenSubtitles v3 JSON parser and 3-letter ISO-639-2 mapping
        val sampleOpenSubtitlesV3Json = """
            {
              "subtitles": [
                {
                  "id": "os_ar_1",
                  "url": "https://subs.strem.io/ar_1.srt",
                  "lang": "ara"
                },
                {
                  "id": "os_en_1",
                  "url": "https://subs.strem.io/en_1.srt",
                  "lang": "eng"
                },
                {
                  "id": "os_fr_1",
                  "url": "https://subs.strem.io/fr_1.srt",
                  "lang": "fre"
                },
                {
                  "id": "os_es_1",
                  "url": "https://subs.strem.io/es_1.srt",
                  "lang": "spa"
                },
                {
                  "id": "os_tr_1",
                  "url": "https://subs.strem.io/tr_1.srt",
                  "lang": "tur"
                }
              ]
            }
        """.trimIndent()

        val parsedOptions = subtitleService.parseOpenSubtitlesV3Json(sampleOpenSubtitlesV3Json, "Dune: Part Two")
        assertEquals(5, parsedOptions.size)
        assertEquals(listOf("ar", "en", "fr", "es", "tr"), parsedOptions.map { it.languageCode })
        assertEquals("UTF-8", parsedOptions.first().encoding)

        assertEquals("de", subtitleService.mapIso6392ToLanguageCode("ger"))
        assertEquals("it", subtitleService.mapIso6392ToLanguageCode("ita"))
        assertEquals("pt", subtitleService.mapIso6392ToLanguageCode("pob"))
        assertEquals("ru", subtitleService.mapIso6392ToLanguageCode("rus"))
        assertEquals("nl", subtitleService.mapIso6392ToLanguageCode("dut"))
        assertEquals("hi", subtitleService.mapIso6392ToLanguageCode("hin"))
        assertEquals("fa", subtitleService.mapIso6392ToLanguageCode("per"))

        // 3. Verify PlayerViewModel sets showSubtitleLanguagePrompt = true on Movie/Series playback start,
        //    false on Live TV playback start, and dismisses when choosing Off
        val app = ApplicationProvider.getApplicationContext<Context>() as Application
        val playerViewModel = com.example.ui.player.PlayerViewModel(app)
        val dummyPlaylist = PlaylistEntity(
            id = 1L,
            name = "Cinema Vault",
            serverUrl = "https://iptv.setplay.io",
            username = "user",
            password = "pw",
            isActive = true
        )
        val movieStream = StreamEntity(
            id = 101L,
            playlistId = 1L,
            streamId = 5001L,
            categoryId = "vod_4k",
            name = "EN| Dune: Part Two (2024)",
            streamType = "movie",
            containerExtension = "mkv",
            epgChannelId = "tt15239678"
        )
        playerViewModel.playStream(movieStream, dummyPlaylist)
        assertTrue(playerViewModel.uiState.value.showSubtitleLanguagePrompt)

        playerViewModel.chooseSubtitleLanguageFromPrompt(null)
        assertFalse(playerViewModel.uiState.value.showSubtitleLanguagePrompt)

        val liveStream = StreamEntity(
            id = 102L,
            playlistId = 1L,
            streamId = 6001L,
            categoryId = "live_sports",
            name = "beIN Sports 1 HD",
            streamType = "live"
        )
        playerViewModel.playStream(liveStream, dummyPlaylist)
        assertFalse(playerViewModel.uiState.value.showSubtitleLanguagePrompt)
        playerViewModel.stopAndClosePlayer()
    }

    @Test
    fun testPhase4MultiFeedQualityGroupingHalftimeTimerAndGoalAlertDetection() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Context>() as Application
        val playerViewModel = com.example.ui.player.PlayerViewModel(app)
        val mainViewModel = com.example.ui.viewmodel.MainViewModel(app)

        val dummyPlaylist = PlaylistEntity(
            id = 99L,
            name = "MENA 4K Sports Vault",
            serverUrl = "https://iptv.setplay.io",
            username = "vip_user",
            password = "pw",
            isActive = true
        )

        // 1. Verify normalizeChannelIdentityForFeedGrouping & resolveAlternateQualityFeeds (4K -> FHD -> HD -> SD)
        val bein1Hd = StreamEntity(id = 1L, playlistId = 99L, streamId = 101L, categoryId = "sports", name = "AR| beIN Sports 1 HD", streamType = "live")
        val bein14k = StreamEntity(id = 2L, playlistId = 99L, streamId = 102L, categoryId = "sports", name = "AR| beIN Sports 01 4K UHD", streamType = "live")
        val bein1Fhd = StreamEntity(id = 3L, playlistId = 99L, streamId = 103L, categoryId = "sports", name = "[VIP] beIN Sports 1 FHD HEVC", streamType = "live")
        val bein1Sd = StreamEntity(id = 4L, playlistId = 99L, streamId = 104L, categoryId = "sports", name = "beIN Sports 1 SD", streamType = "live")
        val bein2Hd = StreamEntity(id = 5L, playlistId = 99L, streamId = 105L, categoryId = "sports", name = "AR| beIN Sports 2 HD", streamType = "live")

        assertEquals(
            playerViewModel.normalizeChannelIdentityForFeedGrouping(bein1Hd.name),
            playerViewModel.normalizeChannelIdentityForFeedGrouping(bein14k.name)
        )
        assertEquals(
            playerViewModel.normalizeChannelIdentityForFeedGrouping(bein1Hd.name),
            playerViewModel.normalizeChannelIdentityForFeedGrouping(bein1Fhd.name)
        )

        val feeds = playerViewModel.resolveAlternateQualityFeeds(
            current = bein1Hd,
            candidates = listOf(bein1Sd, bein1Hd, bein2Hd, bein14k, bein1Fhd)
        )
        assertEquals(listOf(2L, 3L, 1L, 4L), feeds.map { it.id })

        // 2. Verify 15-Minute Halftime Explorer Timer remembers match channel across zapping and restores on return
        playerViewModel.playStream(bein14k, dummyPlaylist)
        playerViewModel.startOrToggleHalftimeTimer(durationSeconds = 900)
        assertEquals(900, playerViewModel.uiState.value.halftimeTimerRemainingSeconds)
        assertEquals(bein14k.id, playerViewModel.uiState.value.halftimeSavedStream?.id)

        // User zaps to another channel during halftime
        playerViewModel.playStream(bein2Hd, dummyPlaylist)
        assertEquals(bein2Hd.id, playerViewModel.uiState.value.currentStream?.id)
        assertEquals(bein14k.id, playerViewModel.uiState.value.halftimeSavedStream?.id)

        // User taps Return to Halftime Channel
        playerViewModel.returnToHalftimeChannel()
        assertEquals(bein14k.id, playerViewModel.uiState.value.currentStream?.id)
        playerViewModel.stopAndClosePlayer()

        // 3. Verify In-Player Live Goal Alert detection when TODAY match increments score
        val initialMatch = com.example.data.repository.FootballMatch(
            matchId = "ucl_rm_bar",
            tournamentId = "laliga",
            tournamentName = "LaLiga",
            tournamentRegion = "Spain",
            dayTab = com.example.data.repository.MatchDayTab.TODAY,
            kickoffTimeFormatted = "21:00",
            kickoffDateFormatted = "Today • GMT+1",
            status = com.example.data.repository.MatchStatus.LIVE,
            liveMinuteOrBadge = "2H • 64'",
            homeTeamId = "real_madrid",
            homeTeamName = "Real Madrid",
            homeTeamBadge = "",
            homeScore = 1,
            awayTeamId = "barcelona",
            awayTeamName = "Barcelona",
            awayTeamBadge = "",
            awayScore = 1,
            stadiumName = "Bernabéu",
            cityName = "Madrid",
            beinChannel = "beIN Sports 1 HD",
            beinChannelNumberHint = "beIN 1",
            fallbackMenaChannels = listOf("beIN Sports 1 HD"),
            menaProviderName = "beIN Sports MENA"
        )

        // Same score produces null alert
        assertNull(
            mainViewModel.detectGoalScoredAlert(
                previousMatches = listOf(initialMatch),
                updatedMatches = listOf(initialMatch),
                favoriteTeamIds = setOf("real_madrid")
            )
        )

        // Score increment from 1 -> 2 triggers LiveGoalAlertEvent
        val goalScoredMatch = initialMatch.copy(homeScore = 2, liveMinuteOrBadge = "2H • 67'")
        val alertEvent = mainViewModel.detectGoalScoredAlert(
            previousMatches = listOf(initialMatch),
            updatedMatches = listOf(goalScoredMatch),
            favoriteTeamIds = setOf("real_madrid")
        )
        assertNotNull(alertEvent)
        assertEquals("Real Madrid", alertEvent?.scoringTeamName)
        assertEquals(2, alertEvent?.match?.homeScore)
        assertEquals(1, alertEvent?.match?.awayScore)
        assertTrue(alertEvent?.isFavoriteTeamGoal == true)
    }

    @Test
    fun testPhase5MetadataParserVodDossierAndTabularNumerals() = runBlocking {
        // 1. StreamMetadataParser: VIP prefix, extractLanguageBadge, and extractQualityBadge
        assertEquals("Oppenheimer (2023)", com.example.util.StreamMetadataParser.cleanDisplayTitle("VIP : Oppenheimer (2023) 4K UHD"))
        assertEquals("AR", com.example.util.StreamMetadataParser.extractLanguageBadge("[01] AR | beIN Sports 1 HD"))
        assertEquals("FR", com.example.util.StreamMetadataParser.extractLanguageBadge("┃FR┃ Dune: Part Two [4K UHD]"))
        assertEquals("TR", com.example.util.StreamMetadataParser.extractLanguageBadge("TR| Ezel S01E01"))
        assertEquals("MA", com.example.util.StreamMetadataParser.extractLanguageBadge("MA: Arryadia HD"))

        // 2. Tabular numerals ("tnum") configured in Type.kt
        assertEquals("tnum", com.example.ui.theme.TabularNumeralTextStyle.fontFeatureSettings)
        assertEquals("tnum", com.example.ui.theme.Typography.labelLarge.fontFeatureSettings)
        assertEquals("tnum", com.example.ui.theme.Typography.labelSmall.fontFeatureSettings)

        // 3. XtreamClient get_vod_info JSON streaming parser
        val sampleVodJson = """
            {
              "info": {
                "name": "Dune: Part Two",
                "movie_image": "https://image.tmdb.org/t/p/w500/dune2.jpg",
                "backdrop_path": ["https://image.tmdb.org/t/p/original/dune2_bg.jpg"],
                "plot": "Paul Atreides unites with Chani and the Fremen while on a warpath of revenge.",
                "cast": "Timothée Chalamet, Zendaya, Rebecca Ferguson",
                "director": "Denis Villeneuve",
                "genre": "Sci-Fi, Adventure",
                "releasedate": "2024-02-28",
                "duration": "02:46:00",
                "rating": "8.8",
                "tmdb_id": "693134"
              },
              "movie_data": {
                "stream_id": 778899,
                "name": "FR| Dune: Part Two [4K UHD]",
                "container_extension": "mkv"
              }
            }
        """.trimIndent()

        val client = XtreamClient()
        val buffer = okio.Buffer().writeUtf8(sampleVodJson)
        val parsedDetails = client.parseVodMovieDetailsJson(
            reader = com.squareup.moshi.JsonReader.of(buffer),
            vodId = 778899L,
            fallbackTitle = "FR| Dune: Part Two [4K UHD]",
            fallbackCover = "",
            fallbackRating = "8.0",
            fallbackExtension = "mp4"
        )
        assertEquals("Dune: Part Two", parsedDetails.title)
        assertEquals("Denis Villeneuve", parsedDetails.director)
        assertTrue(parsedDetails.cast.contains("Timothée Chalamet"))
        assertEquals("2024-02-28", parsedDetails.releaseDate)
        assertEquals("02:46:00", parsedDetails.duration)
        assertEquals("8.8", parsedDetails.rating)
        assertEquals("mkv", parsedDetails.containerExtension)
        assertEquals("693134", parsedDetails.tmdbOrImdbId)

        // 4. VodViewModel instant fallback when opening IMAX Movie Dossier
        val app = ApplicationProvider.getApplicationContext<Context>() as Application
        val vodViewModel = com.example.ui.vod.VodViewModel(app)
        val movieEntity = StreamEntity(
            id = 500L,
            playlistId = 1L,
            streamId = 778899L,
            name = "FR| Dune: Part Two [4K UHD]",
            streamType = "movie",
            streamIcon = "https://image.tmdb.org/t/p/w500/dune2.jpg",
            categoryId = "vod_4k",
            containerExtension = "mkv",
            rating = "8.8"
        )
        vodViewModel.openMovieDetails(movieEntity, playlist = null)
        assertEquals(778899L, vodViewModel.selectedMovieStream.value?.streamId)
        assertEquals("FR| Dune: Part Two [4K UHD]", vodViewModel.movieDetails.value?.title)
        assertEquals("mkv", vodViewModel.movieDetails.value?.containerExtension)
        vodViewModel.closeMovieDetails()
        assertNull(vodViewModel.selectedMovieStream.value)
    }

    @Test
    fun testPhase6SingletonRepositorySharedOkHttpPoolAndLoadMorePagination() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Context>() as Application
        val singletonRepo = IptvRepository.getInstance(app)

        val mainVm = com.example.ui.viewmodel.MainViewModel(app)
        val liveTvVm = com.example.ui.livetv.LiveTvViewModel(app)
        val vodVm = com.example.ui.vod.VodViewModel(app)
        val playerVm = com.example.ui.player.PlayerViewModel(app)

        // 1. Verify all 4 ViewModels share the exact same singleton IptvRepository instance and shared OkHttpClient pool
        assertTrue(singletonRepo === mainVm.repository)
        assertTrue(singletonRepo === liveTvVm.repository)
        assertTrue(singletonRepo === vodVm.repository)
        assertTrue(singletonRepo === playerVm.repository)
        assertNotNull(IptvRepository.sharedOkHttpClient.connectionPool)

        // 2. Verify loadMore() and limit StateFlow progressive pagination in LiveTvViewModel & VodViewModel
        assertEquals(180, liveTvVm.limit.value)
        liveTvVm.loadMore()
        assertEquals(360, liveTvVm.limit.value)
        liveTvVm.loadMore()
        assertEquals(540, liveTvVm.limit.value)
        liveTvVm.selectCategory(null)
        assertEquals(180, liveTvVm.limit.value)

        assertEquals(180, vodVm.limit.value)
        vodVm.loadMore()
        assertEquals(360, vodVm.limit.value)
        vodVm.selectSeriesCategory("cat_series_drama")
        assertEquals(180, vodVm.limit.value)
        vodVm.loadMore()
        assertEquals(360, vodVm.limit.value)
        vodVm.onSeriesSearchQueryChanged("Breaking")
        assertEquals(180, vodVm.limit.value)

        playerVm.stopAndClosePlayer()
    }

    @Test
    fun testPhase7SecurityTvLeanbackAndDevOpsWorkflow() = runBlocking {
        // 1. Verify ProGuard rules contain Moshi (@JsonClass), Room (@Entity / @Dao), and DTO keep rules
        val proguardFile = java.io.File("proguard-rules.pro").let {
            if (it.exists()) it else java.io.File("app/proguard-rules.pro")
        }
        assertTrue(proguardFile.exists())
        val proguardText = proguardFile.readText()
        assertTrue(proguardText.contains("@com.squareup.moshi.JsonClass"))
        assertTrue(proguardText.contains("@androidx.room.Entity"))
        assertTrue(proguardText.contains("com.example.data.model.**"))
        assertTrue(proguardText.contains("com.example.data.remote.**"))

        // 2. Verify GitHub Actions workflow writes non-empty fallback values for SPORT_API_KEY, SPORTAPI_AI_KEY, WYZIE_API_KEY, and API_FOOTBALL_KEY into .env
        //    so secrets-gradle-plugin never generates `public static final String KEY = ;` when a GitHub Secret is unset
        val workflowFile = java.io.File("../.github/workflows/android-apk-release.yml").let {
            if (it.exists()) it else java.io.File(".github/workflows/android-apk-release.yml")
        }
        assertTrue(workflowFile.exists())
        val workflowText = workflowFile.readText()
        assertTrue(workflowText.contains("RESOLVED_SPORT_KEY=\"\${RESOLVED_SPORT_KEY:-YOUR_SPORTAPI_AI_KEY}\""))
        assertTrue(workflowText.contains("RESOLVED_WYZIE_KEY=\"\${WYZIE_API_KEY_SECRET:-YOUR_WYZIE_API_KEY}\""))
        assertTrue(workflowText.contains("RESOLVED_SUBDL_KEY=\"\${SUBDL_API_KEY_SECRET:-YOUR_SUBDL_API_KEY}\""))
        assertTrue(workflowText.contains("RESOLVED_TMDB_KEY=\"\${TMDB_API_KEY_SECRET:-YOUR_TMDB_API_KEY}\""))
        assertTrue(workflowText.contains("RESOLVED_FOOTBALL_KEY=\"\${API_FOOTBALL_KEY_SECRET:-YOUR_API_FOOTBALL_KEY}\""))
        assertTrue(workflowText.contains("RESOLVED_FOOTBALL_DATA_KEY=\"\${FOOTBALL_DATA_KEY_SECRET:-YOUR_FOOTBALL_DATA_KEY}\""))
        assertTrue(workflowText.contains("API_FOOTBALL_KEY=\${RESOLVED_FOOTBALL_KEY}"))
        assertTrue(workflowText.contains("FOOTBALL_DATA_KEY=\${RESOLVED_FOOTBALL_DATA_KEY}"))
        assertTrue(workflowText.contains("SPORT_API_KEY=\${RESOLVED_SPORT_KEY}"))
        assertTrue(workflowText.contains("SPORTAPI_AI_KEY=\${RESOLVED_SPORT_KEY}"))
        assertTrue(workflowText.contains("TMDB_API_KEY=\${RESOLVED_TMDB_KEY}"))
        assertTrue(workflowText.contains("WYZIE_API_KEY=\${RESOLVED_WYZIE_KEY}"))
        assertTrue(workflowText.contains("SUBDL_API_KEY=\${RESOLVED_SUBDL_KEY}"))

        // Verify .env.example has non-empty values for all 7 keys
        val envExampleFile = java.io.File("../.env.example").let {
            if (it.exists()) it else java.io.File(".env.example")
        }
        assertTrue(envExampleFile.exists())
        val envLines = envExampleFile.readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
        val envMap = envLines.associate {
            val idx = it.indexOf('=')
            it.substring(0, idx).trim() to it.substring(idx + 1).trim()
        }
        assertTrue(envMap["API_FOOTBALL_KEY"]?.isNotEmpty() == true)
        assertTrue(envMap["FOOTBALL_DATA_KEY"]?.isNotEmpty() == true)
        assertTrue(envMap["SPORT_API_KEY"]?.isNotEmpty() == true)
        assertTrue(envMap["SPORTAPI_AI_KEY"]?.isNotEmpty() == true)
        assertTrue(envMap["TMDB_API_KEY"]?.isNotEmpty() == true)
        assertTrue(envMap["WYZIE_API_KEY"]?.isNotEmpty() == true)
        assertTrue(envMap["SUBDL_API_KEY"]?.isNotEmpty() == true)

        // 3. Verify AndroidManifest.xml includes Leanback launcher and optional touchscreen features
        val manifestFile = java.io.File("src/main/AndroidManifest.xml").let {
            if (it.exists()) it else java.io.File("app/src/main/AndroidManifest.xml")
        }
        assertTrue(manifestFile.exists())
        val manifestText = manifestFile.readText()
        assertTrue(manifestText.contains("android.hardware.touchscreen"))
        assertTrue(manifestText.contains("android.software.leanback"))
        assertTrue(manifestText.contains("android.intent.category.LEANBACK_LAUNCHER"))
    }

    @Test
    fun testPhase1UpgradePlanApiKeysQuotaAndApiFootballDossier() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val footballRepository = com.example.data.repository.FootballRepository(
            context = context,
            favoriteTeamDao = database.favoriteTeamDao(),
            tournamentPreferenceDao = database.tournamentPreferenceDao(),
            streamDao = database.streamDao()
        )
        com.example.data.repository.FootballRepository.resetSharedSportsDbRateLimiterForTests()

        // 1. Verify Football-Data.org v4 JSON parser (CL, PL, PD, etc.)
        val sampleFootballDataJson = """
            {
              "matches": [
                {
                  "id": 498123,
                  "utcDate": "2026-04-19T19:00:00Z",
                  "status": "IN_PLAY",
                  "minute": 64,
                  "injuryTime": 0,
                  "matchday": 32,
                  "stage": "REGULAR_SEASON",
                  "venue": "Estadio Santiago Bernabéu",
                  "competition": { "code": "PD", "name": "Primera Division" },
                  "homeTeam": {
                    "id": 86,
                    "name": "Real Madrid CF",
                    "shortName": "Real Madrid",
                    "crest": "https://crests.football-data.org/86.png"
                  },
                  "awayTeam": {
                    "id": 81,
                    "name": "FC Barcelona",
                    "shortName": "Barcelona",
                    "crest": "https://crests.football-data.org/81.png"
                  },
                  "score": {
                    "fullTime": { "home": 2, "away": 1 },
                    "halfTime": { "home": 1, "away": 1 }
                  }
                }
              ]
            }
        """.trimIndent()
        val fdoMatches = footballRepository.parseFootballDataOrgMatchesJson(
            rawJson = sampleFootballDataJson,
            dayTab = com.example.data.repository.MatchDayTab.TODAY
        )
        assertEquals(1, fdoMatches.size)
        val clasico = fdoMatches.first()
        assertEquals("laliga", clasico.tournamentId)
        assertEquals(com.example.data.repository.MatchStatus.LIVE, clasico.status)
        assertEquals("2H • 64'", clasico.liveMinuteOrBadge)
        assertEquals(2, clasico.homeScore)
        assertEquals(1, clasico.awayScore)
        assertEquals("1 - 1", clasico.halfTimeScore)

        // 2. Verify Smart Quota Management for API-Football (reserves 15 requests for Match Center after 80 background requests)
        repeat(com.example.data.repository.FootballRepository.API_FOOTBALL_MAX_DAILY_BACKGROUND) {
            assertTrue(com.example.data.repository.FootballRepository.tryAcquireSharedApiFootballPermit(isMatchCenterDossier = false))
        }
        assertFalse(com.example.data.repository.FootballRepository.tryAcquireSharedApiFootballPermit(isMatchCenterDossier = false))
        assertTrue(com.example.data.repository.FootballRepository.tryAcquireSharedApiFootballPermit(isMatchCenterDossier = true))
        com.example.data.repository.FootballRepository.resetSharedSportsDbRateLimiterForTests()

        // 3. Verify 1-Call API-Football v3 Match Center Dossier Enrichment (lineups, media.api-sports.io photos, events, statistics)
        val homeXiArr = (1..11).joinToString(",") { idx ->
            val pId = 1000 + idx
            val pName = if (idx == 1) "Thibaut Courtois" else if (idx == 9) "Kylian Mbappé" else "Real Starter $idx"
            val pos = if (idx == 1) "G" else if (idx <= 5) "D" else if (idx <= 8) "M" else "F"
            """{"player":{"id":$pId,"name":"$pName","number":$idx,"pos":"$pos","grid":"1:$idx"}}"""
        }
        val awayXiArr = (1..11).joinToString(",") { idx ->
            val pId = 2000 + idx
            val pName = if (idx == 1) "Iñaki Peña" else if (idx == 9) "Robert Lewandowski" else "Barca Starter $idx"
            val pos = if (idx == 1) "G" else if (idx <= 5) "D" else if (idx <= 8) "M" else "F"
            """{"player":{"id":$pId,"name":"$pName","number":$idx,"pos":"$pos","grid":"1:$idx"}}"""
        }
        val apiFootballFixtureJson = org.json.JSONObject(
            """
            {
              "fixture": {
                "id": 1208550,
                "referee": "Jesús Gil Manzano"
              },
              "score": {
                "halftime": { "home": 1, "away": 1 }
              },
              "lineups": [
                {
                  "team": { "id": 541, "name": "Real Madrid" },
                  "coach": { "id": 1, "name": "Carlo Ancelotti" },
                  "formation": "4-3-3",
                  "startXI": [$homeXiArr],
                  "substitutes": [
                    {"player":{"id":1099,"name":"Luka Modrić","number":10,"pos":"M"}}
                  ]
                },
                {
                  "team": { "id": 529, "name": "Barcelona" },
                  "coach": { "id": 2, "name": "Hansi Flick" },
                  "formation": "4-2-3-1",
                  "startXI": [$awayXiArr],
                  "substitutes": [
                    {"player":{"id":2099,"name":"Gavi","number":6,"pos":"M"}}
                  ]
                }
              ],
              "players": [
                {
                  "team": { "id": 541, "name": "Real Madrid" },
                  "players": [
                    {
                      "player": { "id": 1009, "name": "Kylian Mbappé", "photo": "https://media.api-sports.io/football/players/1009.png" },
                      "statistics": [
                        {
                          "games": { "rating": "8.9", "captain": false },
                          "goals": { "total": 2, "assists": 0 },
                          "cards": { "yellow": 0, "red": 0 }
                        }
                      ]
                    }
                  ]
                }
              ],
              "events": [
                {
                  "time": { "elapsed": 18, "extra": null },
                  "team": { "id": 541, "name": "Real Madrid" },
                  "player": { "id": 1009, "name": "Kylian Mbappé" },
                  "assist": { "id": 1005, "name": "Jude Bellingham" },
                  "type": "Goal",
                  "detail": "Normal Goal"
                },
                {
                  "time": { "elapsed": 34, "extra": null },
                  "team": { "id": 529, "name": "Barcelona" },
                  "player": { "id": 2009, "name": "Robert Lewandowski" },
                  "assist": { "id": 2007, "name": "Lamine Yamal" },
                  "type": "Goal",
                  "detail": "Normal Goal"
                },
                {
                  "time": { "elapsed": 61, "extra": null },
                  "team": { "id": 541, "name": "Real Madrid" },
                  "player": { "id": 1009, "name": "Kylian Mbappé" },
                  "assist": { "id": null, "name": null },
                  "type": "Goal",
                  "detail": "Penalty"
                }
              ],
              "statistics": [
                {
                  "team": { "id": 541, "name": "Real Madrid" },
                  "statistics": [
                    { "type": "Ball Possession", "value": "54%" },
                    { "type": "expected_goals", "value": "2.41" },
                    { "type": "Total Shots", "value": 16 },
                    { "type": "Shots on Goal", "value": 8 },
                    { "type": "Corner Kicks", "value": 7 }
                  ]
                },
                {
                  "team": { "id": 529, "name": "Barcelona" },
                  "statistics": [
                    { "type": "Ball Possession", "value": "46%" },
                    { "type": "expected_goals", "value": "1.65" },
                    { "type": "Total Shots", "value": 12 },
                    { "type": "Shots on Goal", "value": 5 },
                    { "type": "Corner Kicks", "value": 5 }
                  ]
                }
              ]
            }
            """.trimIndent()
        )

        val enricher = com.example.data.repository.MatchCenterEnricher(okhttp3.OkHttpClient())
        val dossier = enricher.buildCompleteDossier(
            match = clasico.copy(apiFootballFixtureId = "1208550"),
            raw = com.example.data.repository.MatchCenterEnricher.LiveDossierRawPayload(
                apiFootballFixtureJson = apiFootballFixtureJson
            )
        )

        assertEquals("Jesús Gil Manzano", dossier.refereeName)
        assertEquals("HT 1 - 1", dossier.halfTimeScoreLabel)
        assertTrue(dossier.lineups.isConfirmed)
        assertEquals("4-3-3", dossier.lineups.homeFormation)
        assertEquals("4-2-3-1", dossier.lineups.awayFormation)
        val mbappeNode = dossier.lineups.homeStartingXI.first { it.name == "Kylian Mbappé" }
        assertEquals("https://media.api-sports.io/football/players/1009.png", mbappeNode.photoUrl)
        assertEquals(8.9, mbappeNode.rating, 0.01)
        assertEquals(2, mbappeNode.goals)
        assertTrue(mbappeNode.isMvp)

        assertEquals(3, dossier.timelineEvents.size)
        assertEquals("Kylian Mbappé", dossier.timelineEvents.first().primaryPlayer)
        assertEquals("1 - 0", dossier.timelineEvents.first().runningScore)
        assertEquals(com.example.data.repository.MatchEventType.PENALTY_GOAL, dossier.timelineEvents.last().eventType)

        assertTrue(dossier.stats.any { it.statLabel == "Ball Possession" && it.homeValueText == "54%" && it.awayValueText == "46%" })
        assertTrue(dossier.stats.any { it.statLabel == "Expected Goals (xG)" && it.homeValueText == "2.41" && it.awayValueText == "1.65" })
        assertTrue(dossier.lineups.missingPlayers.isEmpty())

        // Verify x-ratelimit-requests-remaining header tracking reserves final 15 calls for Match Center
        com.example.data.repository.FootballRepository.updateApiFootballRemainingQuota("12")
        assertEquals(12, footballRepository.apiFootballRemainingQuota.get())
        assertFalse(com.example.data.repository.FootballRepository.tryAcquireSharedApiFootballPermit(isMatchCenterDossier = false))
        assertTrue(com.example.data.repository.FootballRepository.tryAcquireSharedApiFootballPermit(isMatchCenterDossier = true))
        com.example.data.repository.FootballRepository.resetSharedSportsDbRateLimiterForTests()

        // Verify empty stats and 0.0 pre-match rating when no upstream stats exist
        val emptyUpcomingDossier = enricher.buildCompleteDossier(
            match = clasico.copy(status = com.example.data.repository.MatchStatus.UPCOMING),
            raw = com.example.data.repository.MatchCenterEnricher.LiveDossierRawPayload()
        )
        assertTrue(emptyUpcomingDossier.stats.isEmpty())
        assertTrue(emptyUpcomingDossier.lineups.missingPlayers.isEmpty())
        assertTrue(emptyUpcomingDossier.lineups.homeStartingXI.all { it.rating == 0.0 })
        assertTrue(emptyUpcomingDossier.lineups.awayStartingXI.all { it.rating == 0.0 })
    }

    @Test
    fun `testPhase2UpgradePlanTmdbSubdlZipExtractionAndParallelSubtitleMerging`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val wyzieService = com.example.data.remote.WyzieSubtitleService()
        val xtreamClient = com.example.data.remote.XtreamClient()

        // 1. Verify TMDb /search/movie + /external_ids JSON parsing to (imdb_id, tmdb_id)
        val tmdbSearchJson = """
            {
              "page": 1,
              "results": [
                {
                  "id": 872585,
                  "title": "Oppenheimer",
                  "release_date": "2023-07-19"
                }
              ]
            }
        """.trimIndent()
        val tmdbExternalIdsJson = """
            {
              "id": 872585,
              "imdb_id": "tt15398776",
              "wikidata_id": "Q108839994"
            }
        """.trimIndent()
        val (resolvedImdbId, resolvedTmdbId) = wyzieService.parseTmdbSearchAndExternalIdsJson(
            searchJson = tmdbSearchJson,
            externalIdsJson = tmdbExternalIdsJson
        )
        assertEquals("tt15398776", resolvedImdbId)
        assertEquals("872585", resolvedTmdbId)

        // Verify TMDb /movie/{id}?append_to_response=credits fallback enrichment for MovieDossierBottomSheet
        val tmdbMovieCreditsJson = """
            {
              "id": 872585,
              "imdb_id": "tt15398776",
              "title": "Oppenheimer",
              "backdrop_path": "/fm6KqXpk3M2HVveHwCrBSSBaO0V.jpg",
              "poster_path": "/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg",
              "overview": "The story of J. Robert Oppenheimer's role in the development of the atomic bomb.",
              "release_date": "2023-07-19",
              "runtime": 181,
              "vote_average": 8.12,
              "genres": [
                { "id": 18, "name": "Drama" },
                { "id": 36, "name": "History" }
              ],
              "credits": {
                "cast": [
                  { "name": "Cillian Murphy" },
                  { "name": "Emily Blunt" },
                  { "name": "Matt Damon" },
                  { "name": "Robert Downey Jr." },
                  { "name": "Florence Pugh" },
                  { "name": "Josh Hartnett" }
                ],
                "crew": [
                  { "job": "Producer", "name": "Emma Thomas" },
                  { "job": "Director", "name": "Christopher Nolan" }
                ]
              }
            }
        """.trimIndent()
        val baseMovieDetails = com.example.data.remote.VodMovieDetails(
            vodId = 99L,
            title = "EN | Oppenheimer [4K] (2023)",
            coverUrl = "",
            backdropUrl = ""
        )
        val enrichedDetails = xtreamClient.parseTmdbMovieDetailsWithCreditsJson(
            json = tmdbMovieCreditsJson,
            baseDetails = baseMovieDetails
        )
        assertEquals("https://image.tmdb.org/t/p/w1280/fm6KqXpk3M2HVveHwCrBSSBaO0V.jpg", enrichedDetails.backdropUrl)
        assertEquals("https://image.tmdb.org/t/p/w780/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg", enrichedDetails.coverUrl)
        assertTrue(enrichedDetails.plot.contains("J. Robert Oppenheimer"))
        assertEquals("Christopher Nolan", enrichedDetails.director)
        assertEquals("Cillian Murphy, Emily Blunt, Matt Damon, Robert Downey Jr., Florence Pugh", enrichedDetails.cast)
        assertEquals("181m", enrichedDetails.duration)
        assertEquals("8.1", enrichedDetails.rating)
        assertEquals("Drama, History", enrichedDetails.genre)
        assertEquals("tt15398776", enrichedDetails.tmdbOrImdbId)

        // 2. Verify SubDL JSON parsing & ZipInputStream .srt extraction in downloadSubtitleToCache()
        val subdlJson = """
            {
              "status": true,
              "subtitles": [
                {
                  "sd_id": 44101,
                  "release_name": "Oppenheimer.2023.2160p.UHD.BluRay.x265-SURCODE",
                  "lang": "arabic",
                  "url": "/subtitle/3192810-3218492.zip",
                  "hi": false
                },
                {
                  "sd_id": 44102,
                  "release_name": "Oppenheimer.2023.1080p.WEB-DL.DDP5.1.Atmos",
                  "lang": "tr",
                  "url": "https://dl.subdl.com/subtitle/3192810-9999999.zip",
                  "hi": 0
                }
              ]
            }
        """.trimIndent()
        val parsedSubdl = wyzieService.parseSubdlJson(subdlJson, fallbackTitle = "Oppenheimer")
        assertEquals(2, parsedSubdl.size)
        assertEquals("ar", parsedSubdl[0].languageCode)
        assertEquals("🇸🇦", parsedSubdl[0].flagEmoji)
        assertEquals("SubDL", parsedSubdl[0].source)
        assertEquals("https://dl.subdl.com/subtitle/3192810-3218492.zip", parsedSubdl[0].url)
        assertEquals("tr", parsedSubdl[1].languageCode)

        // Create a temporary PK ZIP archive containing an .srt file and verify downloadSubtitleToCache() + downloadAndParseSubtitle()
        val sampleSrtInsideZip = """
            1
            00:00:02,000 --> 00:00:05,500
            الانشطار النووي سيغير العالم للأبد

            2
            00:00:06,200 --> 00:00:09,800
            Now I am become Death, the destroyer of worlds.
        """.trimIndent()
        val tempZipFile = java.io.File.createTempFile("subdl_test_", ".zip", context.cacheDir)
        java.util.zip.ZipOutputStream(java.io.FileOutputStream(tempZipFile)).use { zos ->
            zos.putNextEntry(java.util.zip.ZipEntry("readme.txt"))
            zos.write("Downloaded from SubDL".toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(java.util.zip.ZipEntry("Oppenheimer.2023.Arabic.srt"))
            zos.write(sampleSrtInsideZip.toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }
        val zipBytes = tempZipFile.readBytes()
        assertEquals(0x50.toByte(), zipBytes[0])
        assertEquals(0x4B.toByte(), zipBytes[1])

        val zipOption = parsedSubdl[0].copy(url = "file://${tempZipFile.absolutePath}")
        val cachedSrtResult = wyzieService.downloadSubtitleToCache(zipOption)
        assertTrue(cachedSrtResult.isSuccess)
        assertTrue(cachedSrtResult.getOrThrow().contains("الانشطار النووي"))

        val parsedCuesResult = wyzieService.downloadAndParseSubtitle(zipOption)
        assertTrue(parsedCuesResult.isSuccess)
        val cues = parsedCuesResult.getOrThrow()
        assertEquals(2, cues.size)
        assertEquals(2000L, cues[0].startTimeMs)
        assertEquals("Now I am become Death, the destroyer of worlds.", cues[1].text)
        tempZipFile.delete()

        // 3. Verify Parallel Merging, Deduplication & Language Priority Ranking across Wyzie, SubDL, and OpenSubtitles v3
        val wyzieOpts = listOf(
            com.example.data.remote.WyzieSubtitleOption(
                id = "w_en",
                url = "https://sub.wyzie.io/dl/en_1.srt",
                languageCode = "en",
                languageDisplay = "English",
                flagEmoji = "🇬🇧",
                mediaName = "Oppenheimer 4K",
                format = "SRT",
                source = "Wyzie",
                encoding = "UTF-8",
                isHearingImpaired = false
            ),
            com.example.data.remote.WyzieSubtitleOption(
                id = "w_fr",
                url = "https://sub.wyzie.io/dl/fr_1.srt",
                languageCode = "fr",
                languageDisplay = "French",
                flagEmoji = "🇫🇷",
                mediaName = "Oppenheimer FR",
                format = "SRT",
                source = "Wyzie",
                encoding = "UTF-8",
                isHearingImpaired = false
            )
        )
        val openSubsOpts = listOf(
            com.example.data.remote.WyzieSubtitleOption(
                id = "os_es",
                url = "https://opensubtitles.strem.io/dl/es_1.srt",
                languageCode = "es",
                languageDisplay = "Spanish",
                flagEmoji = "🇪🇸",
                mediaName = "Oppenheimer ES",
                format = "SRT",
                source = "OpenSubtitles",
                encoding = "UTF-8",
                isHearingImpaired = false
            ),
            // Duplicate URL of w_en — must be deduplicated
            com.example.data.remote.WyzieSubtitleOption(
                id = "os_en_dup",
                url = "https://sub.wyzie.io/dl/en_1.srt",
                languageCode = "en",
                languageDisplay = "English",
                flagEmoji = "🇬🇧",
                mediaName = "Duplicate EN",
                format = "SRT",
                source = "OpenSubtitles",
                encoding = "UTF-8",
                isHearingImpaired = false
            )
        )

        val mergedGroups = wyzieService.mergeAndRankSubtitleOptions(
            wyzieOptions = wyzieOpts,
            subdlOptions = parsedSubdl,
            openSubsOptions = openSubsOpts
        )
        assertEquals(listOf("ar", "en", "fr", "es", "tr"), mergedGroups.map { it.languageCode })
        assertEquals(1, mergedGroups.first { it.languageCode == "en" }.options.size)
        assertEquals("SubDL", mergedGroups.first { it.languageCode == "ar" }.options.first().source)
        assertEquals("Wyzie", mergedGroups.first { it.languageCode == "en" }.options.first().source)
        assertEquals("OpenSubtitles", mergedGroups.first { it.languageCode == "es" }.options.first().source)

        // 4. Verify PlayerViewModel remembers lastSelectedSubtitleLanguageCode in SharedPreferences & PlayerUiState
        // and manages the 12-second subtitle language prompt countdown
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val playerVm = com.example.ui.player.PlayerViewModel(app)
        playerVm.startSubtitleLanguagePromptCountdown(12)
        assertTrue(playerVm.uiState.value.showSubtitleLanguagePrompt)
        assertEquals(12, playerVm.uiState.value.subtitlePromptRemainingSeconds)

        playerVm.chooseSubtitleLanguageFromPrompt("ar")
        assertFalse(playerVm.uiState.value.showSubtitleLanguagePrompt)
        assertEquals("ar", playerVm.uiState.value.lastSelectedSubtitleLanguageCode)
        val savedPrefsLang = app.getSharedPreferences(
            com.example.ui.player.PlayerViewModel.SUBTITLE_PREFS_NAME,
            Context.MODE_PRIVATE
        ).getString(com.example.ui.player.PlayerViewModel.KEY_LAST_SUBTITLE_LANG, null)
        assertEquals("ar", savedPrefsLang)

        // Verify a newly instantiated PlayerViewModel restores lastSelectedSubtitleLanguageCode on init
        val playerVmReloaded = com.example.ui.player.PlayerViewModel(app)
        assertEquals("ar", playerVmReloaded.uiState.value.lastSelectedSubtitleLanguageCode)
    }

    @Test
    fun `test Phase 3 Android TV D-Pad Enter trap delegation and adaptive Phone vs TV grid layout calculations`() {
        var centerCalls = 0
        var playPauseCalls = 0
        val zapped = mutableListOf<Int>()
        val seeks = mutableListOf<Int>()

        // 1. When HUD controls are visible AND a HUD child holds D-Pad focus, KEYCODE_DPAD_CENTER / ENTER
        // and directional D-Pad keys must return false so Compose dispatches click/focus to the focused button!
        assertFalse(
            handlePlayerRemoteKeyEvent(
                keyCode = KeyEvent.KEYCODE_DPAD_CENTER,
                isLive = false,
                isControlsVisible = true,
                isTouchLocked = false,
                onShowControlsOrTogglePlay = { centerCalls++ },
                onTogglePlayPause = { playPauseCalls++ },
                onZapChannel = { zapped.add(it) },
                onSeekRelative = { seeks.add(it) },
                hasOpenOverlayOrSheet = false,
                hasFocusedHudChild = true
            )
        )
        assertFalse(
            handlePlayerRemoteKeyEvent(
                keyCode = KeyEvent.KEYCODE_DPAD_LEFT,
                isLive = false,
                isControlsVisible = true,
                isTouchLocked = false,
                onShowControlsOrTogglePlay = { centerCalls++ },
                onTogglePlayPause = { playPauseCalls++ },
                onZapChannel = { zapped.add(it) },
                onSeekRelative = { seeks.add(it) },
                hasOpenOverlayOrSheet = false,
                hasFocusedHudChild = true
            )
        )
        assertEquals(0, centerCalls)
        assertTrue(seeks.isEmpty())

        // 2. When Subtitle Language Prompt or a BottomSheet is open (`hasOpenOverlayOrSheet = true`),
        // D-Pad Center/Enter and directional keys must also return false so overlay pills receive clicks!
        assertFalse(
            handlePlayerRemoteKeyEvent(
                keyCode = KeyEvent.KEYCODE_ENTER,
                isLive = false,
                isControlsVisible = false,
                isTouchLocked = false,
                onShowControlsOrTogglePlay = { centerCalls++ },
                onTogglePlayPause = { playPauseCalls++ },
                onZapChannel = { zapped.add(it) },
                onSeekRelative = { seeks.add(it) },
                hasOpenOverlayOrSheet = true,
                hasFocusedHudChild = false
            )
        )
        assertEquals(0, centerCalls)

        // 3. Dedicated hardware media keys (MEDIA_PLAY_PAUSE, CHANNEL_UP, MEDIA_FAST_FORWARD) remain active globally
        assertTrue(
            handlePlayerRemoteKeyEvent(
                keyCode = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                isLive = true,
                isControlsVisible = true,
                isTouchLocked = false,
                onShowControlsOrTogglePlay = { centerCalls++ },
                onTogglePlayPause = { playPauseCalls++ },
                onZapChannel = { zapped.add(it) },
                onSeekRelative = { seeks.add(it) },
                hasOpenOverlayOrSheet = true,
                hasFocusedHudChild = true
            )
        )
        assertEquals(1, playPauseCalls)

        // 4. Verify adaptive grid calculations for Phone (390dp) vs Tablet (720dp) vs Android TV (960dp / 1920dp)
        assertEquals(112, com.example.ui.theme.calculateVodPosterMinCellDp(390))
        assertEquals(136, com.example.ui.theme.calculateVodPosterMinCellDp(720))
        assertEquals(156, com.example.ui.theme.calculateVodPosterMinCellDp(960))
        assertEquals(156, com.example.ui.theme.calculateVodPosterMinCellDp(1920))

        assertEquals(1, com.example.ui.theme.calculateLiveTvGridColumns(390))
        assertEquals(2, com.example.ui.theme.calculateLiveTvGridColumns(720))
        assertEquals(3, com.example.ui.theme.calculateLiveTvGridColumns(960))
        assertEquals(3, com.example.ui.theme.calculateLiveTvGridColumns(1920))
    }

    @Test
    fun `test Phase 4 Local Wi-Fi TvSyncManager loopback pairing, VaultSyncPayload exchange, and Single-Connection Safe TV playback`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val tvHostManager = com.example.data.sync.TvSyncManager(app)
        val phoneControllerManager = com.example.data.sync.TvSyncManager(app)

        try {
            // 1. Start TV Host Server on loopback
            tvHostManager.startTvHostServer(preferredPort = 18765)
            val hostState = tvHostManager.sessionState.value
            assertEquals(com.example.data.sync.SyncRole.TV_HOST, hostState.role)
            assertTrue(hostState.isSyncActive)
            assertEquals(6, hostState.pairingPin.length)
            val boundPort = hostState.serverPort

            // Verify QR URI builder & parser
            val qrUri = tvHostManager.buildQrPairingPayload()
            val parsedQr = com.example.data.sync.TvSyncManager.parseQrPairingPayload(qrUri)
            assertNotNull(parsedQr)
            assertEquals(boundPort, parsedQr!!.second)
            assertEquals(hostState.pairingPin, parsedQr.third)

            // Configure TV Host vault exchange callback & live status provider
            var receivedPhoneVaultOnTv: com.example.data.sync.VaultSyncPayload? = null
            tvHostManager.onVaultPayloadReceived = { incomingPhoneVault ->
                receivedPhoneVaultOnTv = incomingPhoneVault
                com.example.data.sync.VaultSyncPayload(
                    playlistName = "Living Room 4K Vault",
                    serverUrl = "https://tv.setplay.io",
                    username = "tv_vip",
                    password = "tv_secret_password",
                    countryZoneCode = "MA",
                    preferredSubtitleLang = "ar",
                    favoriteStreamIds = listOf("live:101", "movie:505"),
                    episodeResumeMap = mapOf("1_ep_9001" to 420_000L)
                )
            }
            tvHostManager.tvPlaybackStatusProvider = {
                tvHostManager.sessionState.value.copy(
                    tvNowPlayingTitle = "AR| beIN Sports 1 HD",
                    tvIsPlaying = true,
                    tvActiveSubtitleLang = "ar"
                )
            }

            // 2. Pair Phone Controller to TV Host over 127.0.0.1 with wrong PIN first (must fail safely)
            val phoneLocalVault = com.example.data.sync.VaultSyncPayload(
                playlistName = "Phone Xtream Account",
                serverUrl = "https://phone.setplay.io",
                username = "phone_user",
                password = "phone_password",
                countryZoneCode = "FR",
                preferredSubtitleLang = "fr",
                favoriteStreamIds = listOf("live:777"),
                episodeResumeMap = mapOf("1_ep_888" to 125_000L)
            )
            val failedPair = phoneControllerManager.connectToTvHost(
                hostIp = "127.0.0.1",
                port = boundPort,
                pin = "000000",
                localVault = phoneLocalVault
            )
            assertTrue(failedPair.isFailure)

            // Now pair with the authentic 6-digit PIN from the TV Host
            var receivedTvVaultOnPhone: com.example.data.sync.VaultSyncPayload? = null
            val successPair = phoneControllerManager.connectToTvHost(
                hostIp = "127.0.0.1",
                port = boundPort,
                pin = hostState.pairingPin,
                localVault = phoneLocalVault,
                onRemoteVaultReceived = { remoteVault ->
                    receivedTvVaultOnPhone = remoteVault
                }
            )
            assertTrue(successPair.isSuccess)
            assertEquals(com.example.data.sync.SyncRole.PHONE_CONTROLLER, phoneControllerManager.sessionState.value.role)
            assertTrue(phoneControllerManager.sessionState.value.isSyncActive)

            // Verify bidirectional VaultSyncPayload exchange
            assertNotNull(receivedPhoneVaultOnTv)
            assertEquals("Phone Xtream Account", receivedPhoneVaultOnTv!!.playlistName)
            assertEquals("FR", receivedPhoneVaultOnTv!!.countryZoneCode)
            assertEquals(125_000L, receivedPhoneVaultOnTv!!.episodeResumeMap["1_ep_888"])

            assertNotNull(receivedTvVaultOnPhone)
            assertEquals("Living Room 4K Vault", receivedTvVaultOnPhone!!.playlistName)
            assertEquals("tv_vip", receivedTvVaultOnPhone!!.username)
            assertTrue(receivedTvVaultOnPhone!!.favoriteStreamIds.contains("live:101"))

            // 3. Verify Navigation Mirroring (`POST /api/sync/navigate`)
            var receivedNavCommandOnTv: com.example.data.sync.RemoteNavMirrorCommand? = null
            tvHostManager.onNavMirrorReceived = { cmd ->
                receivedNavCommandOnTv = cmd
            }
            phoneControllerManager.sendNavMirror(
                com.example.data.sync.RemoteNavMirrorCommand(
                    screenName = "LIVE_TV",
                    matchDayName = "TOMORROW"
                )
            )
            // Wait briefly for async nav mirror dispatch
            kotlinx.coroutines.delay(150L)
            assertNotNull(receivedNavCommandOnTv)
            assertEquals("LIVE_TV", receivedNavCommandOnTv!!.screenName)
            assertEquals("TOMORROW", receivedNavCommandOnTv!!.matchDayName)

            // 4. Verify Single-Connection Safe Direct TV Playback (`POST /api/sync/play`):
            // Phone PlayerViewModel remains stopped (`currentStream == null`) while TV Host receives the stream!
            val phonePlayerVm = com.example.ui.player.PlayerViewModel(app)
            phonePlayerVm.stopAndClosePlayer()
            assertNull(phonePlayerVm.uiState.value.currentStream)

            val playCmd = com.example.data.sync.RemotePlayCommand(
                streamId = "10101",
                streamName = "AR| beIN Sports 1 4K",
                streamType = "live",
                categoryId = "sports_bein",
                streamIcon = "https://setplay.io/bein1.png",
                epgChannelId = "bein1.ar",
                containerExtension = "ts",
                resumePositionMs = 0L,
                preferredSubtitleLang = "ar"
            )
            val playResult = phoneControllerManager.sendPlayOnTv(playCmd)
            assertTrue(playResult.isSuccess)
            assertNull(phonePlayerVm.uiState.value.currentStream)
            assertEquals("AR| beIN Sports 1 4K", phoneControllerManager.sessionState.value.tvNowPlayingTitle)
            assertTrue(phoneControllerManager.sessionState.value.tvIsPlaying)
            assertEquals("ar", phoneControllerManager.sessionState.value.tvActiveSubtitleLang)

            // 5. Verify WhatsApp / ON_RESUME Status Refresh (`GET /api/sync/status`)
            tvHostManager.tvPlaybackStatusProvider = {
                tvHostManager.sessionState.value.copy(
                    tvNowPlayingTitle = "Oppenheimer 4K IMAX",
                    tvNowPlayingStreamId = "90909",
                    tvNowPlayingType = "movie",
                    tvNowPlayingPositionMs = 1_845_000L,
                    tvIsPlaying = true
                )
            }
            val statusResult = phoneControllerManager.refreshTvStatus()
            assertTrue(statusResult.isSuccess)
            assertEquals("Oppenheimer 4K IMAX", phoneControllerManager.sessionState.value.tvNowPlayingTitle)
            assertEquals(1_845_000L, phoneControllerManager.sessionState.value.tvNowPlayingPositionMs)

            // 6. Verify 1-Tap "📱 Watch Here" (`pullTvStreamToPhone`) stops TV player first and returns exact resumePositionMs
            var tvReceivedStopCommand = false
            tvHostManager.onControlCommandReceived = { ctrlCmd ->
                if (ctrlCmd.action.equals("STOP", ignoreCase = true)) {
                    tvReceivedStopCommand = true
                }
            }
            val pulledResult = phoneControllerManager.pullTvStreamToPhone()
            assertTrue(pulledResult.isSuccess)
            assertTrue(tvReceivedStopCommand)
            val pulledCmd = pulledResult.getOrThrow()
            assertEquals("90909", pulledCmd.streamId)
            assertEquals("Oppenheimer 4K IMAX", pulledCmd.streamName)
            assertEquals(1_845_000L, pulledCmd.resumePositionMs)
            assertFalse(phoneControllerManager.sessionState.value.tvIsPlaying)
            assertEquals("", phoneControllerManager.sessionState.value.tvNowPlayingTitle)

            // 7. Verify Leave Sync Mode & 1-Tap Rejoin (`rejoinLastTvHost`)
            assertTrue(phoneControllerManager.hasSavedTvHost())
            phoneControllerManager.leaveSyncMode()
            assertEquals(com.example.data.sync.SyncRole.STANDALONE, phoneControllerManager.sessionState.value.role)
            assertFalse(phoneControllerManager.sessionState.value.isSyncActive)
            assertEquals("127.0.0.1", phoneControllerManager.sessionState.value.savedTvHostIp)

            val rejoinResult = phoneControllerManager.rejoinLastTvHost(localVault = phoneLocalVault)
            assertTrue(rejoinResult.isSuccess)
            assertEquals(com.example.data.sync.SyncRole.PHONE_CONTROLLER, phoneControllerManager.sessionState.value.role)
            assertTrue(phoneControllerManager.sessionState.value.isSyncActive)
        } finally {
            phoneControllerManager.leaveSyncMode()
            tvHostManager.leaveSyncMode()
        }
    }

    @Test
    fun `test Phase 4 persistent TV Host PIN and QR payload round-trip`() = kotlinx.coroutines.runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val tvHost1 = com.example.data.sync.TvSyncManager(app)
        try {
            val state1 = tvHost1.startTvHostServer()
            val pin1 = state1.pairingPin
            assertEquals(6, pin1.length)

            val qrPayload = "setplay://sync?ip=${state1.localIpAddress}&port=${state1.serverPort}&pin=$pin1"
            val parsedQr = com.example.data.sync.TvSyncManager.parseQrPairingPayload(qrPayload)
            assertNotNull(parsedQr)
            assertEquals(state1.localIpAddress, parsedQr!!.first)
            assertEquals(state1.serverPort, parsedQr.second)
            assertEquals(pin1, parsedQr.third)

            tvHost1.leaveSyncMode()

            // Starting a new TV Host instance on the same device reuses the persisted 6-digit PIN
            val tvHost2 = com.example.data.sync.TvSyncManager(app)
            val state2 = tvHost2.startTvHostServer()
            assertEquals(pin1, state2.pairingPin)
            tvHost2.leaveSyncMode()
        } finally {
            tvHost1.leaveSyncMode()
        }
    }

    @Test
    fun `test Phase 4_1 Silent Browse Mode prevents phone navigation from interrupting fullscreen TV playback`() = kotlinx.coroutines.runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val tvMainVm = com.example.ui.viewmodel.MainViewModel(app)
        val phoneController = com.example.data.sync.TvSyncManager(app)
        try {
            tvMainVm.startTvHostMode()
            val tvState = tvMainVm.tvSyncState.value
            assertEquals(com.example.data.sync.SyncRole.TV_HOST, tvState.role)

            val pairRes = phoneController.connectToTvHost(
                hostIp = "127.0.0.1",
                port = tvState.serverPort,
                pin = tvState.pairingPin,
                localVault = com.example.data.sync.VaultSyncPayload(
                    playlistName = "Phone Vault",
                    serverUrl = "https://setplay.io",
                    username = "u",
                    password = "p"
                )
            )
            assertTrue(pairRes.isSuccess)

            // 1. When TV is idle (isTvInFullScreenPlayback == false), phone navigation mirrors to TV
            tvMainVm.isTvInFullScreenPlayback = false
            phoneController.sendNavMirror(
                com.example.data.sync.RemoteNavMirrorCommand(screenName = "MOVIES")
            )
            kotlinx.coroutines.delay(180L)
            assertEquals(com.example.ui.viewmodel.AppScreen.MOVIES, tvMainVm.currentScreen.value)

            // 2. When TV is playing fullscreen video (isTvInFullScreenPlayback == true), phone navigation does NOT interrupt TV
            tvMainVm.isTvInFullScreenPlayback = true
            phoneController.sendNavMirror(
                com.example.data.sync.RemoteNavMirrorCommand(screenName = "SERIES")
            )
            kotlinx.coroutines.delay(180L)
            assertEquals(com.example.ui.viewmodel.AppScreen.MOVIES, tvMainVm.currentScreen.value)
        } finally {
            phoneController.leaveSyncMode()
            tvMainVm.leaveTvSyncMode()
        }
    }

    @Test
    fun `test Phase A EditionResolver resolves PHONE and TV editions and auto-starts TV Sync Host`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        try {
            com.example.util.EditionResolver.runtimeEditionOverride = com.example.util.SetPlayEdition.PHONE
            assertEquals(
                com.example.util.SetPlayEdition.PHONE,
                com.example.util.EditionResolver.resolveEdition(app)
            )

            com.example.util.EditionResolver.runtimeEditionOverride = com.example.util.SetPlayEdition.TV
            assertEquals(
                com.example.util.SetPlayEdition.TV,
                com.example.util.EditionResolver.resolveEdition(app)
            )

            val mainVm = com.example.ui.viewmodel.MainViewModel(app)
            try {
                assertEquals(com.example.data.sync.SyncRole.TV_HOST, mainVm.tvSyncState.value.role)
                assertTrue(mainVm.tvSyncState.value.isSyncActive)
                assertEquals(6, mainVm.tvSyncState.value.pairingPin.length)
            } finally {
                mainVm.leaveTvSyncMode()
            }
        } finally {
            com.example.util.EditionResolver.runtimeEditionOverride = null
        }
    }

    @Test
    fun `test Phase A Live TV Channel Zapper list and D-Pad Left zapper overlay trigger`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val playerVm = PlayerViewModel(app)
        val playlist = PlaylistEntity(
            id = 1L,
            name = "Test Vault",
            serverUrl = "https://setplay.io",
            username = "u",
            password = "p"
        )
        val ch1 = StreamEntity(
            id = 101L,
            playlistId = 1L,
            streamId = 10101L,
            categoryId = "sports",
            name = "AR| beIN Sports 1 4K",
            streamType = "live",
            streamIcon = "",
            epgChannelId = "",
            containerExtension = "ts"
        )
        playerVm.playStream(ch1, playlist)
        assertTrue(playerVm.uiState.value.isLive)
        assertTrue(playerVm.zapperChannelList.value.isNotEmpty())
        assertEquals(10101L, playerVm.zapperChannelList.value.first().streamId)

        var zapperDrawerOpened = false
        val handled = handlePlayerRemoteKeyEvent(
            keyCode = KeyEvent.KEYCODE_DPAD_LEFT,
            isLive = true,
            isControlsVisible = false,
            isTouchLocked = false,
            onShowControlsOrTogglePlay = {},
            onTogglePlayPause = {},
            onZapChannel = {},
            onSeekRelative = {},
            hasOpenOverlayOrSheet = false,
            hasFocusedHudChild = false,
            onOpenLiveChannelZapper = { zapperDrawerOpened = true }
        )
        assertTrue(handled)
        assertTrue(zapperDrawerOpened)
        playerVm.stopAndClosePlayer()
    }

    @Test
    fun `test Phase A TvSyncManager ZAP_NEXT and ZAP_PREV remote channel zapping`() = kotlinx.coroutines.runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val tvHost = com.example.data.sync.TvSyncManager(app)
        val phoneController = com.example.data.sync.TvSyncManager(app)
        try {
            val channels = listOf(
                "10101" to "AR| beIN Sports 1 4K",
                "10102" to "AR| beIN Sports 2 4K",
                "10103" to "AR| beIN Sports 3 FHD"
            )
            var currentIndex = 0
            tvHost.tvPlaybackStatusProvider = {
                val (streamId, title) = channels[currentIndex]
                tvHost.sessionState.value.copy(
                    tvNowPlayingStreamId = streamId,
                    tvNowPlayingTitle = title,
                    tvNowPlayingType = "live",
                    tvIsPlaying = true
                )
            }
            tvHost.onControlCommandReceived = { cmd ->
                when (cmd.action.uppercase()) {
                    "ZAP_NEXT" -> currentIndex = (currentIndex + 1) % channels.size
                    "ZAP_PREV" -> currentIndex = (currentIndex - 1 + channels.size) % channels.size
                }
            }

            val hostState = tvHost.startTvHostServer()
            val pairResult = phoneController.connectToTvHost(
                hostIp = "127.0.0.1",
                port = hostState.serverPort,
                pin = hostState.pairingPin,
                localVault = com.example.data.sync.VaultSyncPayload(
                    playlistName = "Phone Vault",
                    serverUrl = "https://setplay.io",
                    username = "u",
                    password = "p"
                )
            )
            assertTrue(pairResult.isSuccess)
            assertEquals("AR| beIN Sports 1 4K", phoneController.sessionState.value.tvNowPlayingTitle)

            val zapNextRes = phoneController.sendControlToTvSuspend(action = "ZAP_NEXT")
            assertTrue(zapNextRes.isSuccess)
            assertEquals("10102", phoneController.sessionState.value.tvNowPlayingStreamId)
            assertEquals("AR| beIN Sports 2 4K", phoneController.sessionState.value.tvNowPlayingTitle)

            val zapPrevRes = phoneController.sendControlToTvSuspend(action = "ZAP_PREV")
            assertTrue(zapPrevRes.isSuccess)
            assertEquals("10101", phoneController.sessionState.value.tvNowPlayingStreamId)
            assertEquals("AR| beIN Sports 1 4K", phoneController.sessionState.value.tvNowPlayingTitle)
        } finally {
            phoneController.leaveSyncMode()
            tvHost.leaveSyncMode()
        }
    }

    @Test
    fun `test SetPlayQrCodec round-trip encoding and decoding with bit-error tolerance and Bitmap scan`() {
        val ip = "192.168.1.45"
        val port = 8765
        val pin = "943746"

        val bits = com.example.ui.sync.SetPlayQrCodec.encodePayloadToBits(ip, port, pin)
        assertEquals(com.example.ui.sync.SetPlayQrCodec.PAYLOAD_BIT_COUNT, bits.size)

        val matrix = com.example.ui.sync.SetPlayQrCodec.encodeToMatrix(ip, port, pin)
        assertEquals(21, matrix.size)
        val decodedClean = com.example.ui.sync.SetPlayQrCodec.decodeFromMatrix(matrix)
        assertNotNull(decodedClean)
        assertEquals(ip, decodedClean!!.first)
        assertEquals(port, decodedClean.second)
        assertEquals(pin, decodedClean.third)

        // Corrupt 15 non-reserved data modules in the first copy — Triple-Modular Redundancy + CRC8 must still recover 100%
        var corruptedCount = 0
        for (r in 0 until 21) {
            for (c in 0 until 21) {
                if (!com.example.ui.sync.SetPlayQrCodec.isReservedModule(r, c) && corruptedCount < 15) {
                    matrix[r][c] = !matrix[r][c]
                    corruptedCount++
                }
            }
        }
        val decodedWithBitErrors = com.example.ui.sync.SetPlayQrCodec.decodeFromMatrix(matrix)
        assertNotNull(decodedWithBitErrors)
        assertEquals(ip, decodedWithBitErrors!!.first)
        assertEquals(port, decodedWithBitErrors.second)
        assertEquals(pin, decodedWithBitErrors.third)

        // Render to Bitmap with quiet zone and decode via decodeFromBitmap
        val qrBitmap = com.example.ui.sync.SetPlayQrCodec.renderToBitmap(ip, port, pin, sizePx = 252)
        val decodedFromBitmap = com.example.ui.sync.SetPlayQrCodec.decodeFromBitmap(qrBitmap)
        assertNotNull(decodedFromBitmap)
        assertEquals(ip, decodedFromBitmap!!.first)
        assertEquals(port, decodedFromBitmap.second)
        assertEquals(pin, decodedFromBitmap.third)
    }

    @Test
    fun `test TvSyncManager detectLocalLanIpv4 ignores cellular rmnet 10_x_x_x and VPN interfaces`() {
        assertTrue(com.example.data.sync.TvSyncManager.isIgnoredCellularOrTunnelInterface("rmnet_data0"))
        assertTrue(com.example.data.sync.TvSyncManager.isIgnoredCellularOrTunnelInterface("ccmni1"))
        assertTrue(com.example.data.sync.TvSyncManager.isIgnoredCellularOrTunnelInterface("tun0"))
        assertTrue(com.example.data.sync.TvSyncManager.isIgnoredCellularOrTunnelInterface("wg0"))
        assertFalse(com.example.data.sync.TvSyncManager.isIgnoredCellularOrTunnelInterface("wlan0"))
        assertFalse(com.example.data.sync.TvSyncManager.isIgnoredCellularOrTunnelInterface("eth0"))

        // When cellular rmnet_data0 (10.85.158.111) appears before wlan0 (192.168.1.45), wlan0 MUST win
        val mixedCandidates = listOf(
            "rmnet_data0" to "10.85.158.111",
            "tun0" to "10.8.0.2",
            "wlan0" to "192.168.1.45"
        )
        assertEquals(
            "192.168.1.45",
            com.example.data.sync.TvSyncManager.selectBestLanIpv4FromCandidates(mixedCandidates)
        )

        // When Wi-Fi is disconnected and only cellular/VPN interfaces exist, must return 127.0.0.1 (never cellular 10.85.158.111)
        val cellularOnlyCandidates = listOf(
            "rmnet_data0" to "10.85.158.111",
            "ccmni0" to "10.44.12.9",
            "tun0" to "172.16.0.5"
        )
        assertEquals(
            "127.0.0.1",
            com.example.data.sync.TvSyncManager.selectBestLanIpv4FromCandidates(cellularOnlyCandidates)
        )
    }

    @Test
    fun `test SecurityManager isAdultContent classifier and SQLite purgeAllAdultContent`() = runBlocking {
        // 1. Positive Adult / +18 matches (including exact requested strings)
        assertTrue(SecurityManager.isAdultContent("ADULT +18 PORN"))
        assertTrue(SecurityManager.isAdultContent("|XXX| Movies"))
        assertTrue(SecurityManager.isAdultContent("18+ VIP"))
        assertTrue(SecurityManager.isAdultContent("AR| Erotic"))
        assertTrue(SecurityManager.isAdultContent("VIP | +18 Adult Channels"))
        assertTrue(SecurityManager.isAdultContent("18+ Live Movies"))
        assertTrue(SecurityManager.isAdultContent("FR | XXX Late Night"))
        assertTrue(SecurityManager.isAdultContent("Playboy TV HD"))
        assertTrue(SecurityManager.isAdultContent("Brazzers Premium"))
        assertTrue(SecurityManager.isAdultContent("OnlyFans Exclusive"))
        assertTrue(SecurityManager.isAdultContent("Hentai Anime"))
        assertTrue(SecurityManager.isAdultContent("XVideos Stream"))
        assertTrue(SecurityManager.isAdultContent("XNXX Channel"))
        assertTrue(SecurityManager.isAdultContent("أفلام للكبار فقط"))
        assertTrue(SecurityManager.isAdultContent("Normal Title", isAdultFlag = "1"))
        assertTrue(SecurityManager.isAdultContent("Normal Title", isAdultFlag = "true"))

        // 2. False-positive immunity on normal words containing "ero" or "sex" substrings
        assertFalse(SecurityManager.isAdultContent("Hero"))
        assertFalse(SecurityManager.isAdultContent("Zero"))
        assertFalse(SecurityManager.isAdultContent("Romero"))
        assertFalse(SecurityManager.isAdultContent("Sussex"))
        assertFalse(SecurityManager.isAdultContent("beIN Sports 1 HD"))
        assertFalse(SecurityManager.isAdultContent("My Hero Academia"))
        assertFalse(SecurityManager.isAdultContent("Zero Dark Thirty"))
        assertFalse(SecurityManager.isAdultContent("Cristian Romero Highlights"))
        assertFalse(SecurityManager.isAdultContent("Sussex County Cricket"))
        assertFalse(SecurityManager.isAdultContent("✦●✦| AR - beIN Sports 1 FHD"))
        assertFalse(SecurityManager.isAdultContent("AR| Mr. Robot"))

        // 3. Database purge removes adult categories and streams while keeping family/sports content
        val repository = IptvRepository(
            playlistDao = database.playlistDao(),
            categoryDao = database.categoryDao(),
            streamDao = database.streamDao(),
            epgDao = database.epgDao(),
            parentalDao = database.parentalSettingsDao()
        )
        val playlistId = repository.addOrUpdatePlaylist(
            PlaylistEntity(
                name = "Purge Test Vault",
                serverUrl = "http://live.example.com:8080",
                username = "user",
                password = "pass",
                isActive = true
            )
        )
        database.categoryDao().insertCategories(
            listOf(
                CategoryEntity(playlistId = playlistId, categoryId = "cat_sports", categoryName = "AR | Sports FHD", type = "LIVE"),
                CategoryEntity(playlistId = playlistId, categoryId = "cat_xxx", categoryName = "VIP | +18 Adult XXX", type = "LIVE")
            )
        )
        database.streamDao().insertStreamsBatch(
            listOf(
                StreamEntity(playlistId = playlistId, streamId = 101L, categoryId = "cat_sports", name = "beIN Sports 1 FHD", streamType = "live"),
                StreamEntity(playlistId = playlistId, streamId = 998L, categoryId = "cat_xxx", name = "Channel In Adult Category", streamType = "live"),
                StreamEntity(playlistId = playlistId, streamId = 999L, categoryId = "cat_sports", name = "XXX Porn Stream +18", streamType = "live")
            )
        )

        repository.purgeAllAdultContent()

        val remainingCats = database.categoryDao().getCategoriesOnce(playlistId, "LIVE")
        assertEquals(1, remainingCats.size)
        assertEquals("cat_sports", remainingCats.first().categoryId)

        val remainingStreams = database.streamDao().getStreamsInitial(playlistId, "live", limit = 50).first()
        assertEquals(1, remainingStreams.size)
        assertEquals(101L, remainingStreams.first().streamId)
    }

    @Test
    fun `test TvSyncManager enforces X-SetPlay-Pin on all endpoints and scrubs tvNowPlayingUrl`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val tvHost = com.example.data.sync.TvSyncManager(context)
        try {
            val hostState = tvHost.startTvHostServer(preferredPort = 18920, customPin = "654321")
            val port = hostState.serverPort
            tvHost.tvPlaybackStatusProvider = {
                hostState.copy(
                    tvNowPlayingTitle = "beIN Sports 1",
                    tvNowPlayingStreamId = "101",
                    tvNowPlayingType = "live",
                    tvNowPlayingUrl = "http://secret.iptv.server:8080/live/myuser/mypassword/101.ts",
                    tvIsPlaying = true
                )
            }

            fun sendSyncHttp(
                method: String,
                path: String,
                pinHeader: String? = null,
                jsonBody: String? = null
            ): Pair<Int, String> {
                val conn = (java.net.URL("http://127.0.0.1:$port$path").openConnection() as java.net.HttpURLConnection).apply {
                    requestMethod = method
                    connectTimeout = 3000
                    readTimeout = 3000
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    if (pinHeader != null) {
                        setRequestProperty("X-SetPlay-Pin", pinHeader)
                    }
                    if (jsonBody != null) {
                        doOutput = true
                        outputStream.use { it.write(jsonBody.toByteArray(Charsets.UTF_8)) }
                    }
                }
                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                conn.disconnect()
                return code to body
            }

            // 1. Unauthenticated or wrong-PIN requests to /api/sync/navigate, /api/sync/play, /api/sync/control, /api/sync/status must return HTTP 403
            val postEndpoints = listOf(
                "/api/sync/navigate" to """{"screen":"LIVE_TV","category":"Sports"}""",
                "/api/sync/play" to """{"stream_id":"101","title":"beIN Sports 1","stream_type":"live","stream_url":"http://live.example.com/101.ts"}""",
                "/api/sync/control" to """{"action":"TOGGLE_PLAY_PAUSE"}"""
            )
            for ((path, payload) in postEndpoints) {
                val (unauthCode, _) = sendSyncHttp("POST", path, pinHeader = null, jsonBody = payload)
                assertEquals("Expected 403 for unauthenticated $path", 403, unauthCode)

                val (wrongPinCode, _) = sendSyncHttp("POST", path, pinHeader = "000000", jsonBody = payload)
                assertEquals("Expected 403 for wrong PIN on $path", 403, wrongPinCode)

                val (validPinCode, _) = sendSyncHttp("POST", path, pinHeader = "654321", jsonBody = payload)
                assertEquals("Expected 200 for authenticated $path", 200, validPinCode)
            }

            // Unauthenticated GET /api/sync/status must be rejected with HTTP 403
            val (unauthStatusCode, _) = sendSyncHttp("GET", "/api/sync/status", pinHeader = null)
            assertEquals(403, unauthStatusCode)

            // 2. Authenticated GET /api/sync/status with X-SetPlay-Pin succeeds and scrubs tvNowPlayingUrl
            val (authStatusCode, statusBody) = sendSyncHttp("GET", "/api/sync/status", pinHeader = "654321")
            assertEquals(200, authStatusCode)
            assertTrue(statusBody.contains("beIN Sports 1"))
            assertFalse(statusBody.contains("mypassword"))
            assertFalse(statusBody.contains("secret.iptv.server"))
        } finally {
            tvHost.shutdown()
        }
    }

    @Test
    fun `test StreamMetadataParser strips leading decorative Unicode symbols and compacts broadcast labels`() {
        assertEquals(
            "beIN Sports",
            com.example.util.StreamMetadataParser.cleanDisplayTitle("✦●✦| AR - beIN Sports FHD")
        )
        assertEquals(
            "AR",
            com.example.util.StreamMetadataParser.extractLanguageBadge("✦●✦| AR - beIN Sports FHD")
        )
        assertEquals(
            "Mr. Robot",
            com.example.util.StreamMetadataParser.cleanDisplayTitle("AR| Mr. Robot")
        )
        assertEquals(
            "beIN Sports 2 • ES",
            com.example.util.StreamMetadataParser.compactBroadcastChannelLabel(
                "✦●✦| ES | beIN Sports 2 FHD [LaLiga: Malaga vs. Racing Santander]"
            )
        )
        assertEquals(
            "TSN+ 002 • CA",
            com.example.util.StreamMetadataParser.compactBroadcastChannelLabel(
                "CA| TSN+ 002 [LaLiga: Malaga vs. Racing Santander...]"
            )
        )
        assertEquals(
            "S1 • E01 — Pilot",
            com.example.util.StreamMetadataParser.formatCleanEpisodeTitle(
                rawEpisodeTitle = "FR| Breaking Bad - S01E01 - Pilot",
                seasonNumber = 1,
                episodeNumber = 1
            )
        )
    }

    @Test
    fun `test 3-Stage ExoPlayer Reconnect and Quality-Tier Fallback in PlayerViewModel`() = runBlocking {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val playerVm = com.example.ui.player.PlayerViewModel(application)
        try {
            val playlist = PlaylistEntity(
                id = 1L,
                name = "Test Vault",
                serverUrl = "http://live.example.com:8080",
                username = "u",
                password = "p",
                isActive = true
            )
            val stream4K = StreamEntity(
                id = 101L,
                playlistId = 1L,
                streamId = 101L,
                name = "EN | Sky Sports Main Event 4K UHD",
                streamType = "live",
                categoryId = "sports",
                containerExtension = "ts"
            )
            val streamFhd = StreamEntity(
                id = 102L,
                playlistId = 1L,
                streamId = 102L,
                name = "EN | Sky Sports Main Event 1080p FHD",
                streamType = "live",
                categoryId = "sports",
                containerExtension = "ts"
            )
            val streamHd = StreamEntity(
                id = 103L,
                playlistId = 1L,
                streamId = 103L,
                name = "EN | Sky Sports Main Event HD",
                streamType = "live",
                categoryId = "sports",
                containerExtension = "ts"
            )

            playerVm.playStream(
                stream = stream4K,
                playlist = playlist
            )
            playerVm.setAlternateQualityFeedsForTesting(listOf(stream4K, streamFhd, streamHd))
            assertEquals(0, playerVm.uiState.value.reconnectStage)
            assertNull(playerVm.uiState.value.error)

            // Stage 1: Immediate Re-Arm (no user-facing error banner)
            playerVm.handlePlayerErrorRecovery("Source error 1", skipDelaysForTesting = true)
            assertEquals(1, playerVm.uiState.value.reconnectStage)
            assertNull(playerVm.uiState.value.error)
            assertEquals(101L, playerVm.uiState.value.currentStream?.streamId)

            // Stage 2: Fresh Session & Header Rebuild (no user-facing error banner)
            playerVm.handlePlayerErrorRecovery("Source error 2", skipDelaysForTesting = true)
            assertEquals(2, playerVm.uiState.value.reconnectStage)
            assertNull(playerVm.uiState.value.error)
            assertEquals(101L, playerVm.uiState.value.currentStream?.streamId)

            // Stage 3: Automatic Quality-Tier Step-Down (4K UHD -> 1080p FHD)
            playerVm.handlePlayerErrorRecovery("Source error 3", skipDelaysForTesting = true)
            assertEquals(3, playerVm.uiState.value.reconnectStage)
            assertNull(playerVm.uiState.value.error)
            assertEquals(102L, playerVm.uiState.value.currentStream?.streamId)
            assertTrue(
                playerVm.uiState.value.qualityFallbackNotice?.contains("Quality adjusted to FHD") == true
            )
        } finally {
            playerVm.stopAndClosePlayer()
        }
    }

    @Test
    fun `test Series 90 percent Up Next trigger and DAO fallback in PlayerViewModel`() = runBlocking {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val db = Room.inMemoryDatabaseBuilder(application, StreamVaultDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val playerVm = com.example.ui.player.PlayerViewModel(application)
        try {
            val playlistId = db.playlistDao().insertPlaylist(
                PlaylistEntity(
                    name = "Series Vault",
                    serverUrl = "http://vod.example.com:8080",
                    username = "u",
                    password = "p",
                    isActive = true
                )
            )
            val ep1 = StreamEntity(
                id = 501L,
                playlistId = playlistId,
                streamId = 501L,
                name = "EN | Severance S01E01",
                streamType = "series",
                categoryId = "cat_scifi",
                containerExtension = "mp4"
            )
            val ep2 = StreamEntity(
                id = 502L,
                playlistId = playlistId,
                streamId = 502L,
                name = "EN | Severance S01E02",
                streamType = "series",
                categoryId = "cat_scifi",
                containerExtension = "mp4"
            )
            db.streamDao().insertStreamsBatch(listOf(ep1, ep2))

            // Verify DAO fallback query
            val daoNext = db.streamDao().getNextSeriesStreamFallback(
                playlistId = playlistId,
                categoryId = "cat_scifi",
                currentStreamId = 501L
            )
            assertNotNull(daoNext)
            assertEquals(502L, daoNext?.streamId)

            val playlist = db.playlistDao().getPlaylistById(playlistId)!!
            playerVm.setSeriesEpisodeQueue(listOf(ep1, ep2))
            playerVm.playStream(stream = ep1, playlist = playlist)

            assertEquals(502L, playerVm.uiState.value.nextSeriesEpisodeStream?.streamId)
            assertFalse(playerVm.uiState.value.showNextEpisodePrompt)

            // Below 90% threshold -> prompt stays hidden
            playerVm.evaluateSeriesUpNextTrigger(positionMs = 800_000L, durationMs = 1_000_000L)
            assertFalse(playerVm.uiState.value.showNextEpisodePrompt)

            // At 91% threshold -> prompt appears with 10s countdown
            playerVm.evaluateSeriesUpNextTrigger(positionMs = 910_000L, durationMs = 1_000_000L)
            assertTrue(playerVm.uiState.value.showNextEpisodePrompt)
            assertEquals(10, playerVm.uiState.value.nextEpisodeCountdownSec)

            // Cancel hides prompt and prevents re-triggering for the same episode
            playerVm.cancelNextEpisodePrompt()
            assertFalse(playerVm.uiState.value.showNextEpisodePrompt)
            playerVm.evaluateSeriesUpNextTrigger(positionMs = 950_000L, durationMs = 1_000_000L)
            assertFalse(playerVm.uiState.value.showNextEpisodePrompt)

            // Play Next Episode transitions seamlessly to S01E02
            playerVm.playNextSeriesEpisode()
            assertEquals(502L, playerVm.uiState.value.currentStream?.streamId)
        } finally {
            playerVm.stopAndClosePlayer()
            db.close()
        }
    }

    @Test
    fun `test Database Housekeeping purges expired history orphans EPG and adult content`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, StreamVaultDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = IptvRepository(
                playlistDao = db.playlistDao(),
                categoryDao = db.categoryDao(),
                streamDao = db.streamDao(),
                epgDao = db.epgDao(),
                parentalDao = db.parentalSettingsDao()
            )
            val nowMs = 1_800_000_000_000L
            val seventyDaysAgoMs = nowMs - (70L * 24L * 60L * 60L * 1000L)
            val tenDaysAgoMs = nowMs - (10L * 24L * 60L * 60L * 1000L)

            val playlistId = db.playlistDao().insertPlaylist(
                PlaylistEntity(
                    name = "Active Server",
                    serverUrl = "http://live.example.com:8080",
                    username = "u",
                    password = "p",
                    isActive = true
                )
            )

            // Insert streams: 1 expired watch history (>60d), 1 recent watch history, 1 orphan playlist stream, 1 adult stream
            db.streamDao().insertStreamsBatch(
                listOf(
                    StreamEntity(
                        id = 1L,
                        playlistId = playlistId,
                        streamId = 1L,
                        name = "Inception (2010)",
                        streamType = "movie",
                        categoryId = "cat_1",
                        watchHistoryTimestamp = seventyDaysAgoMs,
                        lastPositionMs = 450_000L
                    ),
                    StreamEntity(
                        id = 2L,
                        playlistId = playlistId,
                        streamId = 2L,
                        name = "Interstellar (2014)",
                        streamType = "movie",
                        categoryId = "cat_1",
                        watchHistoryTimestamp = tenDaysAgoMs,
                        lastPositionMs = 300_000L
                    ),
                    StreamEntity(
                        id = 3L,
                        playlistId = 999L, // Orphan playlistId
                        streamId = 3L,
                        name = "Orphan Channel HD",
                        streamType = "live",
                        categoryId = "cat_orphan"
                    ),
                    StreamEntity(
                        id = 4L,
                        playlistId = playlistId,
                        streamId = 4L,
                        name = "XXX Adult Forbidden 18+",
                        streamType = "live",
                        categoryId = "cat_1"
                    )
                )
            )
            db.categoryDao().insertCategories(
                listOf(
                    CategoryEntity(
                        playlistId = playlistId,
                        categoryId = "cat_1",
                        categoryName = "Movies",
                        type = "MOVIE"
                    ),
                    CategoryEntity(
                        playlistId = 999L,
                        categoryId = "cat_orphan",
                        categoryName = "Orphan Category",
                        type = "LIVE"
                    )
                )
            )
            val nowSec = nowMs / 1000L
            db.epgDao().insertEpgPrograms(
                listOf(
                    com.example.data.model.EpgProgramEntity(
                        playlistId = playlistId,
                        streamId = 10L,
                        epgChannelId = "ch1",
                        title = "Old News",
                        description = "",
                        startEpoch = nowSec - 7_200L,
                        endEpoch = nowSec - 3_600L
                    ),
                    com.example.data.model.EpgProgramEntity(
                        playlistId = playlistId,
                        streamId = 10L,
                        epgChannelId = "ch1",
                        title = "Live Match",
                        description = "",
                        startEpoch = nowSec - 1_800L,
                        endEpoch = nowSec + 1_800L
                    )
                )
            )

            repo.purgeExpiredHousekeepingData(nowEpochMs = nowMs)

            // 1. Expired watch history (>60d) cleared while keeping the stream in the catalog
            val inception = db.streamDao().getStreamByProviderId(playlistId, 1L)
            assertNotNull(inception)
            assertEquals(0L, inception?.watchHistoryTimestamp)
            assertEquals(0L, inception?.lastPositionMs)

            // 2. Recent watch history (<60d) preserved
            val interstellar = db.streamDao().getStreamByProviderId(playlistId, 2L)
            assertEquals(tenDaysAgoMs, interstellar?.watchHistoryTimestamp)
            assertEquals(300_000L, interstellar?.lastPositionMs)

            // 3. Orphan stream & category removed
            assertNull(db.streamDao().getStreamByProviderId(999L, 3L))
            assertTrue(db.categoryDao().getCategories(999L, "LIVE").first().isEmpty())

            // 4. Adult stream purged
            assertNull(db.streamDao().getStreamByProviderId(playlistId, 4L))

            // 5. Expired EPG program purged while active program remains
            assertNull(db.epgDao().getCurrentProgram(playlistId, 10L, nowSec - 5_000L))
            assertNotNull(db.epgDao().getCurrentProgram(playlistId, 10L, nowSec))
        } finally {
            db.close()
        }
    }

    @Test
    fun `test KickOff Anchor-and-Tick client clock and adaptive event-horizon polling`() {
        fun sampleMatch(
            status: com.example.data.repository.MatchStatus,
            badge: String
        ): com.example.data.repository.FootballMatch {
            return com.example.data.repository.FootballMatch(
                matchId = "m_${badge.hashCode()}",
                tournamentId = "ucl",
                tournamentName = "UEFA Champions League",
                tournamentRegion = "Europe",
                dayTab = com.example.data.repository.MatchDayTab.TODAY,
                kickoffTimeFormatted = "20:00",
                kickoffDateFormatted = "Today • GMT+1",
                status = status,
                liveMinuteOrBadge = badge,
                homeTeamId = "rma",
                homeTeamName = "Real Madrid",
                homeTeamBadge = "",
                homeScore = 1,
                awayTeamId = "mci",
                awayTeamName = "Manchester City",
                awayTeamBadge = "",
                awayScore = 1,
                stadiumName = "Bernabeu",
                cityName = "Madrid",
                beinChannel = "beIN Sports 1 HD",
                beinChannelNumberHint = "1"
            )
        }

        // 1. Zero LIVE or HALFTIME matches -> 0L (sleep)
        assertEquals(
            0L,
            com.example.data.repository.FootballRepository.calculateAdaptiveLivePollIntervalMs(
                listOf(
                    sampleMatch(com.example.data.repository.MatchStatus.UPCOMING, "20:00"),
                    sampleMatch(com.example.data.repository.MatchStatus.FINISHED, "FT")
                )
            )
        )

        // 2. All in-play matches at Halftime (HT) -> 180_000L (3 min)
        assertEquals(
            180_000L,
            com.example.data.repository.FootballRepository.calculateAdaptiveLivePollIntervalMs(
                listOf(sampleMatch(com.example.data.repository.MatchStatus.HALFTIME, "HT"))
            )
        )

        // 3. Live match in critical transition window (minute 40..48 or >= 85) -> 30_000L (30s)
        assertEquals(
            30_000L,
            com.example.data.repository.FootballRepository.calculateAdaptiveLivePollIntervalMs(
                listOf(sampleMatch(com.example.data.repository.MatchStatus.LIVE, "1H • 44'"))
            )
        )
        assertEquals(
            30_000L,
            com.example.data.repository.FootballRepository.calculateAdaptiveLivePollIntervalMs(
                listOf(sampleMatch(com.example.data.repository.MatchStatus.LIVE, "2H • 88'"))
            )
        )

        // 4. Live match in standard play window (minute 1..39 or 49..84) -> 180_000L (3 min)
        assertEquals(
            180_000L,
            com.example.data.repository.FootballRepository.calculateAdaptiveLivePollIntervalMs(
                listOf(
                    sampleMatch(com.example.data.repository.MatchStatus.LIVE, "1H • 22'"),
                    sampleMatch(com.example.data.repository.MatchStatus.LIVE, "2H • 64'")
                )
            )
        )

        // 5. Client-side Anchor-and-Tick minute advancement and stoppage-time caps
        val initialMatches = listOf(
            sampleMatch(com.example.data.repository.MatchStatus.LIVE, "1H • 37'"),
            sampleMatch(com.example.data.repository.MatchStatus.LIVE, "1H • 45+2'"),
            sampleMatch(com.example.data.repository.MatchStatus.LIVE, "1H • 45+3'"),
            sampleMatch(com.example.data.repository.MatchStatus.LIVE, "2H • 90'"),
            sampleMatch(com.example.data.repository.MatchStatus.LIVE, "2H • 90+5'"),
            sampleMatch(com.example.data.repository.MatchStatus.HALFTIME, "HT")
        )
        val ticked = com.example.data.repository.FootballRepository.advanceClientSideLiveMatchClocks(initialMatches)
        assertEquals("1H • 38'", ticked[0].liveMinuteOrBadge)
        assertEquals("1H • 45+3'", ticked[1].liveMinuteOrBadge)
        assertEquals("1H • 45+3'", ticked[2].liveMinuteOrBadge) // Capped at 45+3'
        assertEquals("2H • 90+1'", ticked[3].liveMinuteOrBadge)
        assertEquals("2H • 90+5'", ticked[4].liveMinuteOrBadge) // Capped at 90+5'
        assertEquals("HT", ticked[5].liveMinuteOrBadge)
    }

    @Test
    fun `test Granular Per-Section Refresh syncPlaylistSection preserves other sections and favorites`() = runBlocking {
        val serverSocket = java.net.ServerSocket(0)
        val port = serverSocket.localPort
        val serverThread = Thread {
            try {
                while (!serverSocket.isClosed) {
                    val socket = serverSocket.accept()
                    val reader = socket.getInputStream().bufferedReader(Charsets.UTF_8)
                    val requestLine = reader.readLine().orEmpty()
                    while (reader.readLine()?.isNotEmpty() == true) {
                        // consume headers
                    }
                    val body = when {
                        requestLine.contains("action=get_live_categories") ->
                            """[{"category_id":"cat_live_1","category_name":"Sports HD"}]"""
                        requestLine.contains("action=get_live_streams") ->
                            """[{"stream_id":101,"name":"beIN Sports 1 FHD","category_id":"cat_live_1","container_extension":"ts"},{"stream_id":102,"name":"Sky Sports Main Event 4K","category_id":"cat_live_1","container_extension":"ts"}]"""
                        requestLine.contains("action=get_vod_categories") ->
                            """[{"category_id":"cat_vod_1","category_name":"Sci-Fi Movies"}]"""
                        requestLine.contains("action=get_vod_streams") ->
                            """[{"stream_id":201,"name":"EN| Dune: Part Two","category_id":"cat_vod_1","container_extension":"mp4"}]"""
                        requestLine.contains("action=get_series_categories") ->
                            """[{"category_id":"cat_ser_1","category_name":"Apple TV+ Series"}]"""
                        requestLine.contains("action=get_series") ->
                            """[{"series_id":301,"name":"FR| Severance","category_id":"cat_ser_1"}]"""
                        else ->
                            """{"user_info":{"auth":1,"status":"Active","exp_date":"1893456000","max_connections":"1"}}"""
                    }
                    val bytes = body.toByteArray(Charsets.UTF_8)
                    val out = socket.getOutputStream()
                    val header = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: ${bytes.size}\r\nConnection: close\r\n\r\n"
                    out.write(header.toByteArray(Charsets.UTF_8))
                    out.write(bytes)
                    out.flush()
                    socket.close()
                }
            } catch (_: Exception) {}
        }.apply { isDaemon = true; start() }

        try {
            val repo = IptvRepository(
                playlistDao = database.playlistDao(),
                categoryDao = database.categoryDao(),
                streamDao = database.streamDao(),
                epgDao = database.epgDao(),
                parentalDao = database.parentalSettingsDao()
            )
            val playlistId = repo.addOrUpdatePlaylist(
                PlaylistEntity(
                    name = "Granular Sync Vault",
                    serverUrl = "http://127.0.0.1:$port",
                    username = "u",
                    password = "p",
                    isActive = true,
                    channelCount = 1,
                    vodCount = 1,
                    seriesCount = 1
                )
            )
            // Seed initial Live, Movie, and Series items (with streamId 101 marked Favorite)
            database.categoryDao().insertCategories(
                listOf(
                    CategoryEntity(playlistId = playlistId, categoryId = "cat_live_old", categoryName = "Old Live", type = "LIVE"),
                    CategoryEntity(playlistId = playlistId, categoryId = "cat_vod_1", categoryName = "Sci-Fi Movies", type = "MOVIE"),
                    CategoryEntity(playlistId = playlistId, categoryId = "cat_ser_1", categoryName = "Apple TV+ Series", type = "SERIES")
                )
            )
            database.streamDao().insertStreamsBatch(
                listOf(
                    StreamEntity(playlistId = playlistId, streamId = 101L, categoryId = "cat_live_old", name = "beIN Sports 1 Old", streamType = "live", isFavorite = true),
                    StreamEntity(playlistId = playlistId, streamId = 201L, categoryId = "cat_vod_1", name = "EN| Dune: Part Two", streamType = "movie", isFavorite = true, lastPositionMs = 120_000L),
                    StreamEntity(playlistId = playlistId, streamId = 301L, categoryId = "cat_ser_1", name = "FR| Severance", streamType = "series", isFavorite = true)
                )
            )

            val activePlaylist = repo.getPlaylistById(playlistId)!!
            val liveSyncResult = repo.syncPlaylistSection(activePlaylist, "LIVE")
            assertTrue(liveSyncResult.isSuccess)

            // 1. Live TV section updated (2 channels, favorite preserved on streamId 101)
            val liveStreams = database.streamDao().getStreamsInitial(playlistId, "live", 50).first()
            assertEquals(2, liveStreams.size)
            assertTrue(liveStreams.first { it.streamId == 101L }.isFavorite)

            // 2. Movies and Series sections were NOT wiped or modified during Live TV section sync
            val movieStreams = database.streamDao().getStreamsInitial(playlistId, "movie", 50).first()
            assertEquals(1, movieStreams.size)
            assertEquals(120_000L, movieStreams.first().lastPositionMs)
            assertTrue(movieStreams.first().isFavorite)

            val seriesStreams = database.streamDao().getStreamsInitial(playlistId, "series", 50).first()
            assertEquals(1, seriesStreams.size)
            assertTrue(seriesStreams.first().isFavorite)
        } finally {
            runCatching { serverSocket.close() }
        }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TvSyncDeviceSeparationComposeTest {

    @get:org.junit.Rule
    val composeTestRule = androidx.compose.ui.test.junit4.createComposeRule()

    @Test
    fun `test TvSyncSheetContent enforces strict Phone vs TV edition UI separation`() {
        val currentEdition = androidx.compose.runtime.mutableStateOf(com.example.util.SetPlayEdition.PHONE)
        val sampleSyncState = com.example.data.sync.TvSyncSessionState(
            localIpAddress = "192.168.1.45",
            serverPort = 8765,
            pairingPin = "943746"
        )

        composeTestRule.setContent {
            com.example.ui.sync.TvSyncSheetContent(
                syncState = sampleSyncState,
                qrPayload = "setplay://sync?ip=192.168.1.45&port=8765&pin=943746",
                hasSavedTvHost = true,
                savedTvHostName = "Living Room Sony Bravia",
                savedTvHostSummary = Triple("192.168.1.45", 8765, "943746"),
                statusErrorMessage = null,
                activeEdition = currentEdition.value,
                isHomeLanConnected = true,
                onConnectToTv = { _, _, _ -> },
                onReconnectLastTv = {},
                onLeaveSyncMode = {},
                onDismiss = {}
            )
        }
        composeTestRule.waitForIdle()

        // 1. In SetPlayEdition.PHONE: Phone controls are displayed, TV QR Canvas is NOT displayed
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("tv_sync_scan_qr_camera_btn")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("tv_sync_ip_input")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("tv_sync_pin_input")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("tv_sync_connect_btn")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("tv_sync_qr_canvas")).assertDoesNotExist()

        // 2. Switch to SetPlayEdition.TV: TV QR Canvas is displayed, Phone inputs & camera button are NOT displayed
        currentEdition.value = com.example.util.SetPlayEdition.TV
        composeTestRule.waitForIdle()

        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("tv_sync_qr_canvas")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("tv_sync_ip_input")).assertDoesNotExist()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("tv_sync_scan_qr_camera_btn")).assertDoesNotExist()
    }

    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    @Test
    fun `test Automated TV Edition UI and D-Pad Verification`() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        com.example.util.EditionResolver.runtimeEditionOverride = com.example.util.SetPlayEdition.TV
        val playerVm = com.example.ui.player.PlayerViewModel(application)
        try {
            val activeScreen = androidx.compose.runtime.mutableStateOf(com.example.ui.viewmodel.AppScreen.HOME)
            val syncState = com.example.data.sync.TvSyncSessionState(
                localIpAddress = "192.168.1.45",
                serverPort = 8765,
                pairingPin = "943746"
            )
            val testPlaylist = PlaylistEntity(
                name = "TV Vault",
                serverUrl = "http://live.example.com:8080",
                username = "u",
                password = "p",
                isActive = true
            )

            composeTestRule.setContent {
                androidx.compose.foundation.layout.Row {
                    TvLeftNavigationRail(
                        currentScreen = activeScreen.value,
                        liveMatchesCount = 3,
                        favoritesCount = 5,
                        tvSyncState = syncState,
                        onScreenSelected = { activeScreen.value = it },
                        onOpenTvSyncQr = {}
                    )
                    com.example.ui.player.PlayerScreen(
                        viewModel = playerVm,
                        playlist = testPlaylist,
                        onBack = {},
                        onToggleFavorite = {},
                        activeEdition = com.example.util.SetPlayEdition.TV,
                        modifier = androidx.compose.ui.Modifier.weight(1f)
                    )
                }
            }
            composeTestRule.waitForIdle()

            // 1. Verify 10-Foot TV Left Navigation Rail and its D-Pad focusable items are visible
            composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("tv_left_navigation_rail")).assertExists()
            composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("nav_home")).assertExists()
            composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("nav_live_tv")).assertExists()
            composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("tv_rail_sync_status_pod")).assertExists()

            // 2. Verify TV Player HUD shows 10-foot remote hint pill and hides phone-only controls
            composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("tv_player_remote_hint_pill")).assertExists()
            composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("player_rotate_button")).assertDoesNotExist()
            composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("player_touch_lock_button")).assertDoesNotExist()
            composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("player_send_to_tv_button")).assertDoesNotExist()

            // 3. Verify D-Pad remote key choreography (OK/Center, Left Zapper, Up/Down Channel Zap)
            var toggledPlay = false
            var openedZapper = false
            var zappedOffset = 0
            val centerHandled = com.example.ui.player.handlePlayerRemoteKeyEvent(
                keyCode = android.view.KeyEvent.KEYCODE_DPAD_CENTER,
                isLive = true,
                isControlsVisible = false,
                isTouchLocked = false,
                onShowControlsOrTogglePlay = { toggledPlay = true },
                onTogglePlayPause = {},
                onZapChannel = {},
                onSeekRelative = {}
            )
            assertTrue(centerHandled)
            assertTrue(toggledPlay)

            val leftHandled = com.example.ui.player.handlePlayerRemoteKeyEvent(
                keyCode = android.view.KeyEvent.KEYCODE_DPAD_LEFT,
                isLive = true,
                isControlsVisible = false,
                isTouchLocked = false,
                onShowControlsOrTogglePlay = {},
                onTogglePlayPause = {},
                onZapChannel = {},
                onSeekRelative = {},
                onOpenLiveChannelZapper = { openedZapper = true }
            )
            assertTrue(leftHandled)
            assertTrue(openedZapper)

            val upHandled = com.example.ui.player.handlePlayerRemoteKeyEvent(
                keyCode = android.view.KeyEvent.KEYCODE_DPAD_UP,
                isLive = true,
                isControlsVisible = false,
                isTouchLocked = false,
                onShowControlsOrTogglePlay = {},
                onTogglePlayPause = {},
                onZapChannel = { zappedOffset = it },
                onSeekRelative = {}
            )
            assertTrue(upHandled)
            assertEquals(1, zappedOffset)

            val downHandled = com.example.ui.player.handlePlayerRemoteKeyEvent(
                keyCode = android.view.KeyEvent.KEYCODE_DPAD_DOWN,
                isLive = true,
                isControlsVisible = false,
                isTouchLocked = false,
                onShowControlsOrTogglePlay = {},
                onTogglePlayPause = {},
                onZapChannel = { zappedOffset = it },
                onSeekRelative = {}
            )
            assertTrue(downHandled)
            assertEquals(-1, zappedOffset)
        } finally {
            com.example.util.EditionResolver.runtimeEditionOverride = null
            playerVm.stopAndClosePlayer()
        }
    }
}



