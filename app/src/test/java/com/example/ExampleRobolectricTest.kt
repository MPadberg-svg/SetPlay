package com.example

import android.app.Application
import android.content.Context
import android.view.KeyEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.performClick
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
import com.example.ui.theme.KickoffAgedParchment
import com.example.ui.theme.KickoffAlabasterIvory
import com.example.ui.theme.KickoffCeruleanBlue
import com.example.ui.theme.KickoffCharcoalElevated
import com.example.ui.theme.KickoffCharcoalSlate
import com.example.ui.theme.KickoffCoralRed
import com.example.ui.theme.KickoffImperialNavy
import com.example.ui.theme.KickoffNavyElevated
import com.example.ui.theme.KickoffObsidianBlack
import com.example.ui.theme.KickoffParchmentBorder
import com.example.ui.theme.KickoffParchmentMuted
import com.example.ui.theme.KickoffPeachCream
import com.example.ui.theme.KickoffTerracotta
import com.example.ui.theme.KickoffTerracottaGlow
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
import kotlinx.coroutines.launch
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

        // 1. KickOff Section Palette (Legacy & Official 6-Color Architectural Palette)
        assertEquals(Color(0xFF0081A7), KickoffCeruleanBlue)
        assertEquals(Color(0xFF00AFB9), KickoffTropicalTeal)
        assertEquals(Color(0xFFFED9B7), KickoffPeachCream)
        assertEquals(Color(0xFFF07167), KickoffCoralRed)
        assertEquals(Color(0xFFD9541E), KickoffTerracotta)
        assertEquals(Color(0xFF0F1B3D), KickoffImperialNavy)
        assertEquals(Color(0xFF111111), KickoffObsidianBlack)
        assertEquals(Color(0xFFF3EEE3), KickoffAlabasterIvory)
        assertEquals(Color(0xFFD8CBB3), KickoffAgedParchment)
        assertEquals(Color(0xFF1E1E1C), KickoffCharcoalSlate)
        assertEquals(Color(0xFF162654), KickoffNavyElevated)
        assertEquals(Color(0xFF272724), KickoffCharcoalElevated)
        assertEquals(Color(0x33D9541E), KickoffTerracottaGlow)
        assertEquals(Color(0x2ED8CBB3), KickoffParchmentBorder)
        assertEquals(Color(0xB3D8CBB3), KickoffParchmentMuted)

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

        // 7. Verify Canonical 3-Pillar Tournament Catalog (European, African/Arab incl. CAF Confederation Cup, International):
        assertEquals(
            listOf(
                "uefa_cl",
                "uefa_el",
                "premier_league",
                "laliga",
                "serie_a",
                "bundesliga",
                "ligue_1",
                "caf_cl",
                "caf_confed",
                "botola_pro",
                "saudi_pro",
                "intl_official",
                "intl_friendly"
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

        val liveSnapshot = footballRepository.loadActivePlaylistLiveStreamsSnapshot()
        val resolvedMatches = footballRepository.resolveDayMatchesTvChannelsWithMenaPriority(
            matches = listOf(realMilanMatch),
            tvBroadcasts = emptyList(),
            zone = maZone,
            playlistLiveStreamsSnapshot = liveSnapshot
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

    @Test
    fun `testKickOffPremierPart1PlayerCutoutsDeepStatsDossierCacheAnd365ScoresParsing`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, StreamVaultDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val footballRepository = com.example.data.repository.FootballRepository(
            context = context,
            favoriteTeamDao = db.favoriteTeamDao(),
            tournamentPreferenceDao = db.tournamentPreferenceDao(),
            streamDao = db.streamDao()
        )
        com.example.data.repository.FootballRepository.resetSharedSportsDbRateLimiterForTests()

        // 1. Verify Strategic API_FOOTBALL_KEY Reservation (4 background schedule calls max, 96 reserved for dossiers)
        assertEquals(4, com.example.data.repository.FootballRepository.MAX_BACKGROUND_SCHEDULE_CALLS_PER_DAY)
        assertEquals(96, com.example.data.repository.FootballRepository.API_FOOTBALL_RESERVED_FOR_DOSSIERS)
        repeat(4) {
            assertTrue(com.example.data.repository.FootballRepository.tryAcquireSharedApiFootballPermit(isMatchCenterDossier = false))
        }
        assertFalse(com.example.data.repository.FootballRepository.tryAcquireSharedApiFootballPermit(isMatchCenterDossier = false))
        assertTrue(com.example.data.repository.FootballRepository.tryAcquireSharedApiFootballPermit(isMatchCenterDossier = true))
        com.example.data.repository.FootballRepository.resetSharedSportsDbRateLimiterForTests()

        // 2. Verify 4-Tier Player Cutout URL Generation & Per-Player Deep Telemetry Parsing (API-Football & Sofascore)
        val enricher = com.example.data.repository.MatchCenterEnricher(okhttp3.OkHttpClient())
        assertEquals(
            "https://media.api-sports.io/football/players/278.png",
            enricher.buildApiSportsPlayerPhotoUrl(278L)
        )
        assertEquals(
            "https://api.sofascore.com/api/v1/player/814594/image",
            enricher.buildSofascorePlayerPhotoUrl(814594L)
        )
        assertEquals(
            "https://imagecache.365scores.com/image/upload/f_png,w_120,h_120,c_limit,q_auto:eco/v1/Athletes/55123",
            enricher.build365ScoresAthletePhotoUrl(55123L)
        )

        val sofascoreSideJson = org.json.JSONObject(
            """
            {
              "formation": "4-3-3",
              "players": [
                {
                  "player": { "id": 814594, "name": "Achraf Hakimi", "shirtNumber": 2, "position": "D" },
                  "shirtNumber": 2,
                  "position": "D",
                  "substitute": false,
                  "captain": true,
                  "statistics": {
                    "rating": 8.4,
                    "goals": 1,
                    "goalAssist": 1,
                    "minutesPlayed": 90,
                    "totalShots": 4,
                    "onTargetScoringAttempt": 2,
                    "expectedGoals": 0.48,
                    "expectedGoalsOnTarget": 0.72,
                    "accuratePass": 64,
                    "totalPass": 71,
                    "touches": 93,
                    "duelWon": 8,
                    "saves": 0
                  }
                }
              ]
            }
            """.trimIndent()
        )
        val (sofaStarters, _) = enricher.parseSofascoreSideEntries(sofascoreSideJson)
        assertEquals(1, sofaStarters.size)
        val hakimiEntry = sofaStarters.first()
        assertEquals("https://api.sofascore.com/api/v1/player/814594/image", hakimiEntry.photoUrl)
        assertEquals(90, hakimiEntry.minutesPlayed)
        assertEquals(4, hakimiEntry.shotsTotal)
        assertEquals(2, hakimiEntry.shotsOnTarget)
        assertEquals(0.48, hakimiEntry.expectedGoals, 0.001)
        assertEquals(0.72, hakimiEntry.expectedGoalsOnTarget, 0.001)
        assertEquals(64, hakimiEntry.passesAccurate)
        assertEquals(71, hakimiEntry.passesTotal)
        assertEquals(93, hakimiEntry.touches)
        assertEquals(8, hakimiEntry.duelsWon)

        // Verify API-Football deep player telemetry flows into PitchPlayerNode
        val homeXi = (1..11).joinToString(",") { idx ->
            val pId = 3000 + idx
            val pName = if (idx == 9) "Mohamed Salah" else "Home Starter $idx"
            val pos = if (idx == 1) "G" else if (idx <= 5) "D" else if (idx <= 8) "M" else "F"
            """{"player":{"id":$pId,"name":"$pName","number":$idx,"pos":"$pos"}}"""
        }
        val awayXi = (1..11).joinToString(",") { idx ->
            val pId = 4000 + idx
            val pName = "Away Starter $idx"
            val pos = if (idx == 1) "G" else if (idx <= 5) "D" else if (idx <= 8) "M" else "F"
            """{"player":{"id":$pId,"name":"$pName","number":$idx,"pos":"$pos"}}"""
        }
        val apifDeepStatsJson = org.json.JSONObject(
            """
            {
              "fixture": { "id": 990011, "referee": "Michael Oliver" },
              "lineups": [
                {
                  "team": { "id": 40, "name": "Liverpool" },
                  "coach": { "name": "Arne Slot" },
                  "formation": "4-3-3",
                  "startXI": [$homeXi],
                  "substitutes": []
                },
                {
                  "team": { "id": 42, "name": "Arsenal" },
                  "coach": { "name": "Mikel Arteta" },
                  "formation": "4-3-3",
                  "startXI": [$awayXi],
                  "substitutes": []
                }
              ],
              "players": [
                {
                  "team": { "id": 40, "name": "Liverpool" },
                  "players": [
                    {
                      "player": { "id": 3009, "name": "Mohamed Salah" },
                      "statistics": [
                        {
                          "games": { "minutes": 88, "rating": "8.7", "captain": false },
                          "goals": { "total": 1, "assists": 1, "saves": 0 },
                          "shots": { "total": 5, "on": 3 },
                          "passes": { "total": 42, "accuracy": "36" },
                          "duels": { "won": 7 },
                          "expected_goals": 0.64
                        }
                      ]
                    }
                  ]
                }
              ]
            }
            """.trimIndent()
        )

        // 3. Verify 365Scores allscores JSON parsing into FootballMatch items
        val sample365ScoresJson = """
            {
              "games": [
                {
                  "id": 4192831,
                  "competitionId": 552,
                  "competitionDisplayName": "Botola Pro Inwi",
                  "startTime": "2025-05-11T20:00:00Z",
                  "statusText": "2nd Half",
                  "gameTime": 68,
                  "homeCompetitor": {
                    "id": 7211,
                    "name": "Raja Casablanca",
                    "score": 2.0,
                    "redCards": 0
                  },
                  "awayCompetitor": {
                    "id": 7212,
                    "name": "Wydad AC",
                    "score": 1.0,
                    "redCards": 1
                  },
                  "tvNetworks": [
                    { "id": 1, "name": "Arryadia HD" },
                    { "id": 2, "name": "Arryadia TNT" }
                  ]
                },
                {
                  "id": 4192832,
                  "competitionId": 7,
                  "competitionDisplayName": "Premier League",
                  "startTime": "2025-05-11T16:30:00Z",
                  "statusText": "1st Half",
                  "gameTime": 34,
                  "homeCompetitor": {
                    "id": 108,
                    "name": "Liverpool",
                    "score": 1.0,
                    "redCards": 0
                  },
                  "awayCompetitor": {
                    "id": 104,
                    "name": "Arsenal",
                    "score": 0.0,
                    "redCards": 0
                  },
                  "tvNetworks": [
                    { "id": 10, "name": "beIN Sports 1 HD" }
                  ]
                }
              ]
            }
        """.trimIndent()
        val parsed365 = footballRepository.parse365ScoresAllScoresJson(
            rawJson = sample365ScoresJson,
            dayTab = com.example.data.repository.MatchDayTab.TODAY
        )
        assertEquals(2, parsed365.size)
        val derbyMatch = parsed365[0]
        assertEquals("botola_pro", derbyMatch.tournamentId)
        assertEquals("Raja Casablanca", derbyMatch.homeTeamName)
        assertEquals("Wydad AC", derbyMatch.awayTeamName)
        assertEquals(com.example.data.repository.MatchStatus.LIVE, derbyMatch.status)
        assertEquals("2H • 68'", derbyMatch.liveMinuteOrBadge)
        assertEquals(2, derbyMatch.homeScore)
        assertEquals(1, derbyMatch.awayScore)
        assertEquals(1, derbyMatch.awayRedCards)
        assertTrue(derbyMatch.sourceTvChannels.contains("Arryadia HD"))

        val eplMatch = parsed365[1]
        val apifDossier = enricher.buildCompleteDossier(
            match = eplMatch,
            raw = com.example.data.repository.MatchCenterEnricher.LiveDossierRawPayload(
                apiFootballFixtureJson = apifDeepStatsJson
            )
        )
        val salahNode = apifDossier.lineups.homeStartingXI.first { it.name == "Mohamed Salah" }
        assertEquals("https://media.api-sports.io/football/players/3009.png", salahNode.photoUrl)
        assertEquals(88, salahNode.minutesPlayed)
        assertEquals(5, salahNode.shotsTotal)
        assertEquals(3, salahNode.shotsOnTarget)
        assertEquals(0.64, salahNode.expectedGoals, 0.001)
        assertEquals(36, salahNode.passesAccurate)
        assertEquals(42, salahNode.passesTotal)
        assertEquals(7, salahNode.duelsWon)

        // 4. Verify 2-Hour Dossier Cache hit (0 redundant network calls when matchScoreSignature is unchanged)
        // and automatic cache invalidation when a live goal increments the score
        val zone = com.example.data.repository.UserZoneRegistry.getZoneByCode("MA")
        val cacheKey = "${eplMatch.matchId}_${zone.countryCode}"
        enricher.dossierCache[cacheKey] = com.example.data.repository.CachedMatchDossier(
            timestampMs = System.currentTimeMillis(),
            matchScoreSignature = enricher.buildMatchScoreSignature(eplMatch),
            dossier = apifDossier
        )
        val initialNetworkCalls = enricher.networkFetchInvocationCount.get()

        // Re-opening same match with unchanged 1-0 score hits 2-hour cache in 0ms with 0 network calls
        val cachedHitDossier = enricher.fetchMatchFullDossier(eplMatch, forceRefresh = false, countryZone = zone)
        assertEquals(initialNetworkCalls, enricher.networkFetchInvocationCount.get())
        assertEquals("Michael Oliver", cachedHitDossier.refereeName)

        // Live goal increments score to 2-0 -> matchScoreSignature changes -> cache invalidates and refreshes
        val eplMatchAfterGoal = eplMatch.copy(homeScore = 2)
        val refreshedAfterGoal = enricher.fetchMatchFullDossier(eplMatchAfterGoal, forceRefresh = false, countryZone = zone)
        assertEquals(initialNetworkCalls + 1, enricher.networkFetchInvocationCount.get())
        assertEquals(2, refreshedAfterGoal.match.homeScore)

        db.close()
    }

    @Test
    fun `testKickOffPremierPart2MomentumWaveShotMapLiveStandingsAndPinnedMatches`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val enricher = com.example.data.repository.MatchCenterEnricher(okhttp3.OkHttpClient())

        // 1. Verify MomentumMinutePoint parsing from Sofascore /event/{id}/graph JSON
        val sampleGraphJson = """
            {
              "graphPoints": [
                { "minute": 1, "value": 18.5 },
                { "minute": 15, "value": 64.0 },
                { "minute": 32, "value": -42.5 },
                { "minute": 45, "value": 12.0 },
                { "minute": 67, "value": -85.0 },
                { "minute": 89, "value": 92.0 }
              ]
            }
        """.trimIndent()
        val momentumPoints = enricher.parseSofascoreMomentumGraphJson(sampleGraphJson)
        assertEquals(6, momentumPoints.size)
        assertEquals(1, momentumPoints[0].minute)
        assertEquals(18.5f, momentumPoints[0].value, 0.01f)
        assertEquals(67, momentumPoints[4].minute)
        assertEquals(-85.0f, momentumPoints[4].value, 0.01f)
        assertEquals(89, momentumPoints[5].minute)
        assertEquals(92.0f, momentumPoints[5].value, 0.01f)

        // 2. Verify MatchShotPoint (xG / xGOT) parsing from Sofascore /event/{id}/shotmap JSON & Categorized Stats
        val sampleShotMapJson = """
            {
              "shotmap": [
                {
                  "id": "s1",
                  "player": { "name": "Mohamed Salah" },
                  "isHome": true,
                  "shotType": "goal",
                  "xg": 0.44,
                  "xgot": 0.78,
                  "time": 24,
                  "playerCoordinates": { "x": 11.5, "y": 54.0 }
                },
                {
                  "id": "s2",
                  "player": { "name": "Bukayo Saka" },
                  "isHome": false,
                  "shotType": "save",
                  "xg": 0.22,
                  "xgot": 0.35,
                  "time": 51,
                  "playerCoordinates": { "x": 16.0, "y": 38.0 }
                },
                {
                  "id": "s3",
                  "player": { "name": "Darwin Nunez" },
                  "isHome": true,
                  "shotType": "block",
                  "xg": 0.09,
                  "xgot": 0.0,
                  "time": 73,
                  "playerCoordinates": { "x": 20.0, "y": 48.0 }
                }
              ]
            }
        """.trimIndent()
        val shotMap = enricher.parseSofascoreShotMapJson(sampleShotMapJson)
        assertEquals(3, shotMap.size)
        assertEquals(com.example.data.repository.ShotOutcome.GOAL, shotMap[0].outcome)
        assertEquals(com.example.data.repository.MatchTeamSide.HOME, shotMap[0].teamSide)
        assertEquals(0.44, shotMap[0].xg, 0.001)
        assertEquals(0.78, shotMap[0].xgot, 0.001)
        assertEquals(com.example.data.repository.ShotOutcome.ON_TARGET_SAVED, shotMap[1].outcome)
        assertEquals(com.example.data.repository.MatchTeamSide.AWAY, shotMap[1].teamSide)
        assertEquals(com.example.data.repository.ShotOutcome.BLOCKED, shotMap[2].outcome)

        // Verify categorized stats tagging ("Top Stats", "Shots & xG", "Passes", "Defense & Duels", "Goalkeeping")
        val apifStatsPayload = org.json.JSONObject(
            """
            {
              "statistics": [
                {
                  "team": { "name": "Liverpool" },
                  "statistics": [
                    { "type": "Ball Possession", "value": "58%" },
                    { "type": "expected_goals", "value": "2.14" },
                    { "type": "xg_on_target", "value": "2.65" },
                    { "type": "Total Shots", "value": "16" },
                    { "type": "Shots on Goal", "value": "7" },
                    { "type": "Big Chances", "value": "4" },
                    { "type": "Shots insidebox", "value": "12" },
                    { "type": "Passes %", "value": "88%" },
                    { "type": "Duels Won", "value": "54" },
                    { "type": "Goalkeeper Saves", "value": "3" }
                  ]
                },
                {
                  "team": { "name": "Arsenal" },
                  "statistics": [
                    { "type": "Ball Possession", "value": "42%" },
                    { "type": "expected_goals", "value": "1.08" },
                    { "type": "xg_on_target", "value": "1.12" },
                    { "type": "Total Shots", "value": "9" },
                    { "type": "Shots on Goal", "value": "4" },
                    { "type": "Big Chances", "value": "2" },
                    { "type": "Shots insidebox", "value": "6" },
                    { "type": "Passes %", "value": "82%" },
                    { "type": "Duels Won", "value": "46" },
                    { "type": "Goalkeeper Saves", "value": "5" }
                  ]
                }
              ]
            }
            """.trimIndent()
        )
        val liveMatch = com.example.data.repository.FootballMatch(
            matchId = "epl_liv_ars",
            tournamentId = "premier_league",
            tournamentName = "Premier League",
            tournamentRegion = "England",
            dayTab = com.example.data.repository.MatchDayTab.TODAY,
            kickoffTimeFormatted = "17:30",
            kickoffDateFormatted = "Today • GMT+1",
            status = com.example.data.repository.MatchStatus.LIVE,
            liveMinuteOrBadge = "2H • 76'",
            homeTeamId = "liv",
            homeTeamName = "Liverpool",
            homeTeamBadge = "",
            homeScore = 2,
            awayTeamId = "ars",
            awayTeamName = "Arsenal",
            awayTeamBadge = "",
            awayScore = 1,
            stadiumName = "Anfield",
            cityName = "Liverpool",
            beinChannel = "beIN Sports 1 HD",
            beinChannelNumberHint = "1"
        )
        val fullDossier = enricher.buildCompleteDossier(
            match = liveMatch,
            raw = com.example.data.repository.MatchCenterEnricher.LiveDossierRawPayload(
                sofascoreGraphJson = org.json.JSONObject(sampleGraphJson),
                sofascoreShotmapJson = org.json.JSONObject(sampleShotMapJson),
                apiFootballFixtureJson = apifStatsPayload
            )
        )
        assertEquals(6, fullDossier.momentumPoints.size)
        assertEquals(3, fullDossier.shotMap.size)
        assertTrue(fullDossier.matchBuzzInsights.isNotEmpty())
        val topStatsFiltered = fullDossier.stats.filter { it.category == "Top Stats" || it.isKeyMetric }
        val shotsAndXgFiltered = fullDossier.stats.filter { it.category == "Shots & xG" }
        assertTrue(topStatsFiltered.any { it.statLabel == "Ball Possession" })
        assertTrue(shotsAndXgFiltered.any { it.statLabel == "Expected Goals (xG)" && it.homeValueText == "2.14" })
        assertTrue(shotsAndXgFiltered.any { it.statLabel == "xG on Target (xGOT)" && it.homeValueText == "2.65" })
        assertTrue(shotsAndXgFiltered.any { it.statLabel == "Big Chances" })
        assertTrue(shotsAndXgFiltered.any { it.statLabel == "Touches in Penalty Box" })

        // 3. Verify computeLiveInPlayStandings() live points and liveRankDelta (▲ +N / ▼ -N) calculation
        val officialStandings = listOf(
            com.example.data.repository.LeagueStandingRow(
                rank = 1,
                teamName = "Arsenal",
                teamBadge = "",
                played = 10,
                wins = 7,
                draws = 2,
                losses = 1,
                goalDiffFormatted = "+14",
                points = 23,
                formLast5 = listOf("W", "W", "D", "W", "W"),
                isAwayTeam = true
            ),
            com.example.data.repository.LeagueStandingRow(
                rank = 2,
                teamName = "Manchester City",
                teamBadge = "",
                played = 10,
                wins = 7,
                draws = 1,
                losses = 2,
                goalDiffFormatted = "+12",
                points = 22,
                formLast5 = listOf("W", "L", "W", "W", "D")
            ),
            com.example.data.repository.LeagueStandingRow(
                rank = 3,
                teamName = "Liverpool",
                teamBadge = "",
                played = 10,
                wins = 7,
                draws = 0,
                losses = 3,
                goalDiffFormatted = "+11",
                points = 21,
                formLast5 = listOf("W", "W", "L", "W", "W"),
                isHomeTeam = true
            )
        )
        // Liverpool (21 pts, rank 3) is leading Arsenal (23 pts, rank 1) 2-1 live -> Liverpool jumps to 24 pts (rank 1, liveRankDelta = +2)!
        val liveStandings = enricher.computeLiveInPlayStandings(officialStandings, liveMatch)
        assertEquals(3, liveStandings.size)
        val liveLeader = liveStandings[0]
        assertEquals("Liverpool", liveLeader.teamName)
        assertEquals(1, liveLeader.rank)
        assertEquals(11, liveLeader.played)
        assertEquals(8, liveLeader.wins)
        assertEquals(24, liveLeader.points)
        assertEquals("+12", liveLeader.goalDiffFormatted)
        assertEquals(2, liveLeader.liveRankDelta) // Moved up +2 places (from #3 to #1)

        val liveSecond = liveStandings[1]
        assertEquals("Arsenal", liveSecond.teamName)
        assertEquals(2, liveSecond.rank)
        assertEquals(11, liveSecond.played)
        assertEquals(23, liveSecond.points)
        assertEquals(-1, liveSecond.liveRankDelta) // Dropped -1 place (from #1 to #2)

        // 4. Verify togglePinMatch() persistence in SharedPreferences and priority goal alert detection
        app.getSharedPreferences(com.example.ui.viewmodel.MainViewModel.KICKOFF_PINNED_PREFS_NAME, Context.MODE_PRIVATE)
            .edit().clear().commit()
        val mainVm = com.example.ui.viewmodel.MainViewModel(app)
        assertTrue(mainVm.pinnedMatchIds.value.isEmpty())

        mainVm.togglePinMatch("epl_liv_ars")
        assertTrue(mainVm.pinnedMatchIds.value.contains("epl_liv_ars"))

        // Recreate MainViewModel to verify SharedPreferences persistence across app restarts
        val recreatedVm = com.example.ui.viewmodel.MainViewModel(app)
        assertTrue(recreatedVm.pinnedMatchIds.value.contains("epl_liv_ars"))

        // Verify pinned match gets priority in detectGoalScoredAlert when multiple matches score simultaneously
        val otherMatchBefore = liveMatch.copy(
            matchId = "other_match",
            homeTeamName = "Chelsea",
            awayTeamName = "Newcastle",
            homeScore = 0,
            awayScore = 0
        )
        val otherMatchAfter = otherMatchBefore.copy(homeScore = 1)
        val pinnedBefore = liveMatch.copy(homeScore = 1, awayScore = 1)
        val pinnedAfter = liveMatch.copy(homeScore = 2, awayScore = 1)

        val prioritizedAlert = recreatedVm.detectGoalScoredAlert(
            previousMatches = listOf(otherMatchBefore, pinnedBefore),
            updatedMatches = listOf(otherMatchAfter, pinnedAfter)
        )
        assertNotNull(prioritizedAlert)
        assertEquals("epl_liv_ars", prioritizedAlert?.match?.matchId)
        assertEquals("Liverpool", prioritizedAlert?.scoringTeamName)
        assertTrue(prioritizedAlert?.isFavoriteTeamGoal == true)

        // Unpin match and verify removal
        recreatedVm.togglePinMatch("epl_liv_ars")
        assertFalse(recreatedVm.pinnedMatchIds.value.contains("epl_liv_ars"))

        // 5. Verify Merged Option D adaptive launcher icons and 16:9 TV banner load cleanly
        val bgDrawable = androidx.core.content.ContextCompat.getDrawable(app, R.drawable.ic_launcher_background)
        val fgDrawable = androidx.core.content.ContextCompat.getDrawable(app, R.drawable.ic_launcher_foreground)
        val bannerDrawable = androidx.core.content.ContextCompat.getDrawable(app, R.drawable.tv_banner)
        assertNotNull("ic_launcher_background must inflate cleanly", bgDrawable)
        assertNotNull("ic_launcher_foreground must inflate cleanly", fgDrawable)
        assertNotNull("tv_banner must inflate cleanly", bannerDrawable)
        assertEquals(108, bgDrawable?.intrinsicWidth)
        assertEquals(108, bgDrawable?.intrinsicHeight)
        assertEquals(108, fgDrawable?.intrinsicWidth)
        assertEquals(108, fgDrawable?.intrinsicHeight)
        assertEquals(320, bannerDrawable?.intrinsicWidth)
        assertEquals(180, bannerDrawable?.intrinsicHeight)
    }

    @Test
    fun `test KickOff 4 core production fixes - strict Tier-1 filtering, dynamic timezone kickoff, MM_SS live clock, and tactical lineups`() {
        val footballRepo = com.example.data.repository.FootballRepository
        val enricher = com.example.data.repository.MatchCenterEnricher(okhttp3.OkHttpClient())

        // 1. Strict Tier-1 Tournament Filtering (reject 2nd division & wrong regional leagues)
        assertNotNull(footballRepo.matchTournamentCatalog("French Ligue 1", espnSlug = "fra.1", sportsDbLeagueId = "4334"))
        assertNull(footballRepo.matchTournamentCatalog("Côte d'Ivoire Ligue 1"))
        assertNull(footballRepo.matchTournamentCatalog("Ivory Coast Ligue 1"))
        assertNull(footballRepo.matchTournamentCatalog("Algeria Ligue 1"))
        assertNull(footballRepo.matchTournamentCatalog("Congo Ligue 1"))
        assertNull(footballRepo.matchTournamentCatalog("French Ligue 2"))

        assertNotNull(footballRepo.matchTournamentCatalog("Spanish LaLiga", espnSlug = "esp.1", sportsDbLeagueId = "4335"))
        assertNull(footballRepo.matchTournamentCatalog("LaLiga Hypermotion"))
        assertNull(footballRepo.matchTournamentCatalog("Spanish Segunda División"))
        assertNull(footballRepo.matchTournamentCatalog("Primera Federación"))
        assertNull(footballRepo.matchTournamentCatalog("Andorra Primera Divisió"))

        assertNotNull(footballRepo.matchTournamentCatalog("English Premier League", espnSlug = "eng.1", sportsDbLeagueId = "4328"))
        assertNull(footballRepo.matchTournamentCatalog("English League Championship"))
        assertNull(footballRepo.matchTournamentCatalog("English League One"))
        assertNull(footballRepo.matchTournamentCatalog("English League Two"))

        assertNotNull(footballRepo.matchTournamentCatalog("Italian Serie A", espnSlug = "ita.1", sportsDbLeagueId = "4332"))
        assertNull(footballRepo.matchTournamentCatalog("Italian Serie B"))
        assertNull(footballRepo.matchTournamentCatalog("Brazilian Serie A"))

        assertNotNull(footballRepo.matchTournamentCatalog("German Bundesliga", espnSlug = "ger.1", sportsDbLeagueId = "4331"))
        assertNull(footballRepo.matchTournamentCatalog("German 2. Bundesliga"))
        assertNull(footballRepo.matchTournamentCatalog("Austrian Bundesliga"))

        // 2. Timezone-Accurate Kickoff Times Synchronized with UserCountryZone
        // 2026-11-08T20:00:00Z (UTC) -> 1794168000000L
        val utcEpochMs = 1794168000000L
        val moroccoZone = com.example.data.repository.UserZoneRegistry.getZoneByCode("MA")
        val saudiZone = com.example.data.repository.UserZoneRegistry.getZoneByCode("SA")
        val usEastZone = com.example.data.repository.UserZoneRegistry.getZoneByCode("US")

        assertEquals("20:00", com.example.data.repository.formatDynamicKickoffTime(utcEpochMs, moroccoZone, isSummer = false)) // Ramadan GMT+0
        assertEquals("21:00", com.example.data.repository.formatDynamicKickoffTime(utcEpochMs, moroccoZone, isSummer = true))  // Morocco GMT+1
        assertEquals("23:00", com.example.data.repository.formatDynamicKickoffTime(utcEpochMs, saudiZone, isSummer = false))   // KSA GMT+3
        assertEquals("15:00", com.example.data.repository.formatDynamicKickoffTime(utcEpochMs, usEastZone, isSummer = false))  // US EST GMT-5

        // 3. Real-Time MM:SS Live Match Clocks
        val sampleLiveMatch = com.example.data.repository.FootballMatch(
            matchId = "ucl_rm_mci",
            tournamentId = "uefa_cl",
            tournamentName = "UEFA Champions League",
            tournamentRegion = "Europe",
            stageOrRound = "League Phase",
            homeTeamId = "rm",
            homeTeamName = "Real Madrid",
            homeTeamBadge = "",
            awayTeamId = "mci",
            awayTeamName = "Manchester City",
            awayTeamBadge = "",
            homeScore = 2,
            awayScore = 1,
            kickoffTimeFormatted = "21:00",
            kickoffDateFormatted = "Today",
            status = com.example.data.repository.MatchStatus.LIVE,
            liveMinuteOrBadge = "19'",
            stadiumName = "Santiago Bernabéu",
            cityName = "Madrid",
            beinChannel = "beIN Sports 1 HD",
            beinChannelNumberHint = "1",
            dayTab = com.example.data.repository.MatchDayTab.TODAY,
            kickoffEpochMs = utcEpochMs
        )
        val clock19 = com.example.ui.home.parseLiveMatchClockTelemetry(sampleLiveMatch, elapsedSecondOffset = 24)
        assertEquals("19:24", clock19.heroReadoutSeconds)

        val stoppageMatch = sampleLiveMatch.copy(liveMinuteOrBadge = "45+2'")
        val clockStoppage = com.example.ui.home.parseLiveMatchClockTelemetry(stoppageMatch, elapsedSecondOffset = 15)
        assertEquals("45:00+02:15", clockStoppage.heroReadoutSeconds)

        val secondHalfMatch = sampleLiveMatch.copy(liveMinuteOrBadge = "78'")
        val clock78 = com.example.ui.home.parseLiveMatchClockTelemetry(secondHalfMatch, elapsedSecondOffset = 42)
        assertEquals("78:42", clock78.heroReadoutSeconds)

        // 4. Accurate Lineups & 100% Real Player Cutouts (4-4-2, 4-1-4-1, 3-4-2-1, 4-2-3-1)
        assertEquals(listOf(1, 4, 4, 2), enricher.parseFormationLines("4-4-2"))
        assertEquals(listOf(1, 4, 1, 4, 1), enricher.parseFormationLines("4-1-4-1"))
        assertEquals(listOf(1, 3, 4, 2, 1), enricher.parseFormationLines("3-4-2-1"))
        assertEquals(listOf(1, 4, 2, 3, 1), enricher.parseFormationLines("4-2-3-1"))

        val apiFootballFixturePayload = org.json.JSONObject(
            """
            {
              "lineups": [
                {
                  "team": { "name": "Real Madrid" },
                  "formation": "4-1-4-1",
                  "coach": { "name": "Carlo Ancelotti" },
                  "startXI": [
                    { "player": { "id": 730, "name": "Thibaut Courtois", "number": 1, "pos": "G", "grid": "1:1" } },
                    { "player": { "id": 738, "name": "Dani Carvajal", "number": 2, "pos": "D", "grid": "2:1" } },
                    { "player": { "id": 742, "name": "Antonio Rüdiger", "number": 22, "pos": "D", "grid": "2:2" } },
                    { "player": { "id": 743, "name": "Éder Militão", "number": 3, "pos": "D", "grid": "2:3" } },
                    { "player": { "id": 745, "name": "Ferland Mendy", "number": 23, "pos": "D", "grid": "2:4" } },
                    { "player": { "id": 1271, "name": "Aurélien Tchouaméni", "number": 14, "pos": "M", "grid": "3:1" } },
                    { "player": { "id": 754, "name": "Federico Valverde", "number": 8, "pos": "M", "grid": "4:1" } },
                    { "player": { "id": 129718, "name": "Jude Bellingham", "number": 5, "pos": "M", "grid": "4:2" } },
                    { "player": { "id": 2285, "name": "Eduardo Camavinga", "number": 6, "pos": "M", "grid": "4:3" } },
                    { "player": { "id": 762, "name": "Vinícius Júnior", "number": 7, "pos": "F", "grid": "4:4" } },
                    { "player": { "id": 278, "name": "Kylian Mbappé", "number": 9, "pos": "F", "grid": "5:1" } }
                  ],
                  "substitutes": [
                    { "player": { "id": 752, "name": "Luka Modrić", "number": 10, "pos": "M" } }
                  ]
                },
                {
                  "team": { "name": "Manchester City" },
                  "formation": "3-4-2-1",
                  "coach": { "name": "Pep Guardiola" },
                  "startXI": [
                    { "player": { "id": 617, "name": "Ederson", "number": 31, "pos": "G", "grid": "1:1" } },
                    { "player": { "id": 626, "name": "Kyle Walker", "number": 2, "pos": "D", "grid": "2:1" } },
                    { "player": { "id": 567, "name": "Rúben Dias", "number": 3, "pos": "D", "grid": "2:2" } },
                    { "player": { "id": 129033, "name": "Joško Gvardiol", "number": 24, "pos": "D", "grid": "2:3" } },
                    { "player": { "id": 627, "name": "John Stones", "number": 5, "pos": "M", "grid": "3:1" } },
                    { "player": { "id": 44, "name": "Rodri", "number": 16, "pos": "M", "grid": "3:2" } },
                    { "player": { "id": 636, "name": "Bernardo Silva", "number": 20, "pos": "M", "grid": "3:3" } },
                    { "player": { "id": 1422, "name": "Jérémy Doku", "number": 11, "pos": "M", "grid": "3:4" } },
                    { "player": { "id": 629, "name": "Kevin De Bruyne", "number": 17, "pos": "M", "grid": "4:1" } },
                    { "player": { "id": 631, "name": "Phil Foden", "number": 47, "pos": "M", "grid": "4:2" } },
                    { "player": { "id": 1100, "name": "Erling Haaland", "number": 9, "pos": "F", "grid": "5:1" } }
                  ],
                  "substitutes": [
                    { "player": { "id": 633, "name": "İlkay Gündoğan", "number": 19, "pos": "M" } }
                  ]
                }
              ]
            }
            """.trimIndent()
        )

        val dossier = enricher.buildCompleteDossier(
            match = sampleLiveMatch,
            raw = com.example.data.repository.MatchCenterEnricher.LiveDossierRawPayload(
                apiFootballFixtureJson = apiFootballFixturePayload
            ),
            countryZone = moroccoZone
        )
        assertEquals("4-1-4-1", dossier.lineups.homeFormation)
        assertEquals("3-4-2-1", dossier.lineups.awayFormation)
        assertEquals(11, dossier.lineups.homeStartingXI.size)
        assertEquals(11, dossier.lineups.awayStartingXI.size)
        // Verify 4-1-4-1 row counts: row 0 = 1 (GK), row 1 = 4 (DF), row 2 = 1 (CDM), row 3 = 4 (MF), row 4 = 1 (ST)
        assertEquals(1, dossier.lineups.homeStartingXI.count { it.rowIndex == 0 })
        assertEquals(4, dossier.lineups.homeStartingXI.count { it.rowIndex == 1 })
        assertEquals(1, dossier.lineups.homeStartingXI.count { it.rowIndex == 2 })
        assertEquals(4, dossier.lineups.homeStartingXI.count { it.rowIndex == 3 })
        assertEquals(1, dossier.lineups.homeStartingXI.count { it.rowIndex == 4 })
        // Verify official API-Football player cutout URLs
        assertEquals("https://media.api-sports.io/football/players/278.png", dossier.lineups.homeStartingXI[10].photoUrl)
        assertEquals("https://media.api-sports.io/football/players/752.png", dossier.lineups.homeBench[0].photoUrl)
    }

    @Test
    fun `test MatchClockEngine epoch-anchored clock, stoppage horizon verification guard, and provider authority hierarchy`() {
        com.example.data.repository.MatchClockEngine.clearRegistryForTests()
        val syncEpochMs = 1_730_000_000_000L

        val baseMatch = com.example.data.repository.FootballMatch(
            matchId = "sportapi_ars_liv",
            tournamentId = "premier_league",
            tournamentName = "Premier League",
            tournamentRegion = "England",
            stageOrRound = "Round 11",
            homeTeamId = "ars",
            homeTeamName = "Arsenal",
            homeTeamBadge = "",
            awayTeamId = "liv",
            awayTeamName = "Liverpool",
            awayTeamBadge = "",
            homeScore = 1,
            awayScore = 1,
            kickoffTimeFormatted = "17:30",
            kickoffDateFormatted = "Today",
            status = com.example.data.repository.MatchStatus.LIVE,
            liveMinuteOrBadge = "1H • 44'",
            stadiumName = "Emirates Stadium",
            cityName = "London",
            beinChannel = "beIN Sports 1 HD",
            beinChannelNumberHint = "1",
            dayTab = com.example.data.repository.MatchDayTab.TODAY,
            kickoffEpochMs = syncEpochMs - 44 * 60_000L
        )

        // 1. 1st Half Horizon Guard: crossing 45:00 transitions to FIRST_HALF_STOPPAGE (45:00+MM:SS), NEVER fabricating "HT"
        val anchor1H = com.example.data.repository.MatchClockEngine.deriveAnchorFromMatch(
            match = baseMatch,
            nowEpochMs = syncEpochMs,
            deterministicSubMinuteSeed = false
        )
        assertEquals(com.example.data.repository.MatchPeriodPhase.FIRST_HALF, anchor1H.phase)

        val snapAt44m30s = com.example.data.repository.MatchClockEngine.evaluateClockAt(anchor1H, syncEpochMs + 30_000L)
        assertEquals(com.example.data.repository.MatchPeriodPhase.FIRST_HALF, snapAt44m30s.phase)
        assertEquals("44:30", snapAt44m30s.formattedSecondsClock)
        assertFalse(snapAt44m30s.isAwaitingProviderPhaseConfirmation)

        // Advance 2 minutes 15 seconds (past 45:00 into 46:15 elapsed) without provider HT confirmation
        val snapPast45 = com.example.data.repository.MatchClockEngine.evaluateClockAt(anchor1H, syncEpochMs + 135_000L)
        assertEquals(com.example.data.repository.MatchPeriodPhase.FIRST_HALF_STOPPAGE, snapPast45.phase)
        assertEquals("45:00+01:15", snapPast45.formattedSecondsClock)
        assertEquals("45+1'", snapPast45.formattedMinuteBadge)
        assertEquals("STOPPAGE", snapPast45.periodHeaderBadge)
        assertTrue(snapPast45.isAwaitingProviderPhaseConfirmation)

        // 2. Halftime Hold Guard: holds strictly at "HT" even after 20 minutes until upstream confirms 2H kickoff
        val htMatch = baseMatch.copy(status = com.example.data.repository.MatchStatus.HALFTIME, liveMinuteOrBadge = "HT")
        val anchorHt = com.example.data.repository.MatchClockEngine.registerOrUpdateAnchor(htMatch, syncEpochMs + 300_000L)
        val snapHtAfter20Min = com.example.data.repository.MatchClockEngine.evaluateClockAt(anchorHt, syncEpochMs + 1_500_000L)
        assertEquals(com.example.data.repository.MatchPeriodPhase.HALFTIME_BREAK, snapHtAfter20Min.phase)
        assertEquals("HT", snapHtAfter20Min.formattedSecondsClock)
        assertTrue(snapHtAfter20Min.isAwaitingProviderPhaseConfirmation)

        // 3. 2nd Half Horizon Guard: crossing 90:00 transitions to SECOND_HALF_STOPPAGE (90:00+MM:SS), NEVER fabricating "FT"
        val match2H89 = baseMatch.copy(status = com.example.data.repository.MatchStatus.LIVE, liveMinuteOrBadge = "2H • 89'")
        val anchor2H = com.example.data.repository.MatchClockEngine.deriveAnchorFromMatch(
            match = match2H89,
            nowEpochMs = syncEpochMs,
            deterministicSubMinuteSeed = false
        )
        val snapPast90 = com.example.data.repository.MatchClockEngine.evaluateClockAt(anchor2H, syncEpochMs + 190_000L)
        assertEquals(com.example.data.repository.MatchPeriodPhase.SECOND_HALF_STOPPAGE, snapPast90.phase)
        assertEquals("90:00+02:10", snapPast90.formattedSecondsClock)
        assertEquals("90+2'", snapPast90.formattedMinuteBadge)
        assertTrue(snapPast90.isAwaitingProviderPhaseConfirmation)

        // 4. Provider Authority Hierarchy: API-Football & 365Scores outrank TheSportsDB and prevent phase regression
        val tsdbCandidate = baseMatch.copy(matchId = "tsdb_100", status = com.example.data.repository.MatchStatus.LIVE, liveMinuteOrBadge = "1H • 42'")
        val apiFootballCandidate = baseMatch.copy(
            matchId = "apif_999",
            apiFootballFixtureId = "999",
            status = com.example.data.repository.MatchStatus.LIVE,
            liveMinuteOrBadge = "2H • 51'"
        )
        assertTrue(
            com.example.data.repository.MatchClockEngine.shouldPreferCandidateByAuthority(
                existing = tsdbCandidate,
                candidate = apiFootballCandidate
            )
        )
        assertFalse(
            com.example.data.repository.MatchClockEngine.shouldPreferCandidateByAuthority(
                existing = apiFootballCandidate,
                candidate = tsdbCandidate
            )
        )
    }

    @Test
    fun `test KickOff Phase 2 - InternationalRankingResolver, CAF Confederation Cup, 3-Pillar Competitions, and Empty Unannounced Lineups`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, StreamVaultDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val footballRepo = com.example.data.repository.FootballRepository(
                context = context,
                favoriteTeamDao = db.favoriteTeamDao(),
                tournamentPreferenceDao = db.tournamentPreferenceDao(),
                streamDao = db.streamDao()
            )
            val resolver = footballRepo.internationalRankingResolver

            // 1. Verify Dynamic Confederation Quotas: Top 5 CAF, Top 3 AFC, Top 7 UEFA, Top 4 CONMEBOL (19 total)
            val qualifiedByConfed = resolver.getQualifiedTeamsByConfederation()
            assertEquals(5, qualifiedByConfed[com.example.data.repository.FootballConfederation.CAF]?.size)
            assertEquals(3, qualifiedByConfed[com.example.data.repository.FootballConfederation.AFC]?.size)
            assertEquals(7, qualifiedByConfed[com.example.data.repository.FootballConfederation.UEFA]?.size)
            assertEquals(4, qualifiedByConfed[com.example.data.repository.FootballConfederation.CONMEBOL]?.size)
            assertEquals(19, resolver.getQualifiedNationalTeams().size)

            // Verify Top-5 CAF teams qualify while 7th-ranked CAF team alone vs non-qualified opponent is filtered out
            assertTrue(resolver.isQualifiedInternationalFixture("Morocco", "Brazil"))
            assertTrue(resolver.isQualifiedInternationalFixture("Japan", "San Marino"))
            assertFalse(resolver.isQualifiedInternationalFixture("Cameroon", "Tunisia"))

            // 2. Verify dynamic ranking table update automatically shifts qualified teams without code changes
            val updatedRankingJson = """
                [
                  {"fifaCode": "CMR", "iso2": "CM", "confederation": "CAF", "globalRank": 11, "points": 1715.0, "countryName": "Cameroon"},
                  {"fifaCode": "MAR", "iso2": "MA", "confederation": "CAF", "globalRank": 12, "points": 1710.0, "countryName": "Morocco"},
                  {"fifaCode": "SEN", "iso2": "SN", "confederation": "CAF", "globalRank": 17, "points": 1640.0, "countryName": "Senegal"},
                  {"fifaCode": "EGY", "iso2": "EG", "confederation": "CAF", "globalRank": 30, "points": 1525.0, "countryName": "Egypt"},
                  {"fifaCode": "NGA", "iso2": "NG", "confederation": "CAF", "globalRank": 36, "points": 1505.0, "countryName": "Nigeria"},
                  {"fifaCode": "ALG", "iso2": "DZ", "confederation": "CAF", "globalRank": 40, "points": 1495.0, "countryName": "Algeria"},
                  {"fifaCode": "JPN", "iso2": "JP", "confederation": "AFC", "globalRank": 15, "points": 1655.0, "countryName": "Japan"},
                  {"fifaCode": "IRN", "iso2": "IR", "confederation": "AFC", "globalRank": 19, "points": 1625.0, "countryName": "Iran"},
                  {"fifaCode": "KOR", "iso2": "KR", "confederation": "AFC", "globalRank": 22, "points": 1590.0, "countryName": "South Korea"},
                  {"fifaCode": "ESP", "iso2": "ES", "confederation": "UEFA", "globalRank": 2, "points": 1855.0, "countryName": "Spain"},
                  {"fifaCode": "FRA", "iso2": "FR", "confederation": "UEFA", "globalRank": 3, "points": 1850.0, "countryName": "France"},
                  {"fifaCode": "ENG", "iso2": "GB-ENG", "confederation": "UEFA", "globalRank": 4, "points": 1815.0, "countryName": "England"},
                  {"fifaCode": "POR", "iso2": "PT", "confederation": "UEFA", "globalRank": 6, "points": 1775.0, "countryName": "Portugal"},
                  {"fifaCode": "NED", "iso2": "NL", "confederation": "UEFA", "globalRank": 7, "points": 1765.0, "countryName": "Netherlands"},
                  {"fifaCode": "BEL", "iso2": "BE", "confederation": "UEFA", "globalRank": 8, "points": 1750.0, "countryName": "Belgium"},
                  {"fifaCode": "ITA", "iso2": "IT", "confederation": "UEFA", "globalRank": 9, "points": 1740.0, "countryName": "Italy"},
                  {"fifaCode": "ARG", "iso2": "AR", "confederation": "CONMEBOL", "globalRank": 1, "points": 1885.0, "countryName": "Argentina"},
                  {"fifaCode": "BRA", "iso2": "BR", "confederation": "CONMEBOL", "globalRank": 5, "points": 1785.0, "countryName": "Brazil"},
                  {"fifaCode": "COL", "iso2": "CO", "confederation": "CONMEBOL", "globalRank": 10, "points": 1730.0, "countryName": "Colombia"},
                  {"fifaCode": "URU", "iso2": "UY", "confederation": "CONMEBOL", "globalRank": 14, "points": 1665.0, "countryName": "Uruguay"}
                ]
            """.trimIndent()
            assertTrue(resolver.updateRankingsFromJson(updatedRankingJson).isNotEmpty())
            assertTrue(resolver.isQualifiedInternationalFixture("Cameroon", "Tunisia"))

            // 3. Verify CAF Confederation Cup & International Fixtures matching (separate from CAF CL)
            val cafConfedById = footballRepo.matchApiFootballLeague(20, "CAF Confederation Cup", "Africa")
            assertNotNull(cafConfedById)
            assertEquals("caf_confed", cafConfedById?.tournamentId)

            val cafConfedByName = footballRepo.matchTournamentCatalog("TotalEnergies CAF Confederation Cup", "Africa")
            assertNotNull(cafConfedByName)
            assertEquals("caf_confed", cafConfedByName?.tournamentId)

            val intlOfficial = footballRepo.matchTournamentCatalog("FIFA World Cup Qualifiers - Africa", "World")
            assertNotNull(intlOfficial)
            assertEquals("intl_official", intlOfficial?.tournamentId)

            val intlFriendly = footballRepo.matchTournamentCatalog("International Friendlies", "World")
            assertNotNull(intlFriendly)
            assertEquals("intl_friendly", intlFriendly?.tournamentId)

            // 4. Verify Unrecognized Club with unannounced lineups returns empty startingXI & bench (never Lille/Porto roster)
            val enricher = com.example.data.repository.MatchCenterEnricher(okhttp3.OkHttpClient())
            val unknownMatch = com.example.data.repository.FootballMatch(
                matchId = "caf_confed_test",
                tournamentId = "caf_confed",
                tournamentName = "CAF Confederation Cup",
                tournamentRegion = "Africa",
                dayTab = com.example.data.repository.MatchDayTab.TODAY,
                kickoffTimeFormatted = "20:00",
                kickoffDateFormatted = "Today",
                status = com.example.data.repository.MatchStatus.UPCOMING,
                liveMinuteOrBadge = "20:00",
                homeTeamId = "usma",
                homeTeamName = "USM Alger",
                homeTeamBadge = "",
                homeScore = null,
                awayTeamId = "asec",
                awayTeamName = "ASEC Mimosas",
                awayTeamBadge = "",
                awayScore = null,
                stadiumName = "Stade du 5 Juillet",
                cityName = "Algiers",
                beinChannel = "beIN Sports 6 HD",
                beinChannelNumberHint = "6"
            )
            val unknownDossier = enricher.buildCompleteDossier(unknownMatch, raw = null)
            assertTrue(unknownDossier.lineups.homeStartingXI.isEmpty())
            assertTrue(unknownDossier.lineups.awayStartingXI.isEmpty())
            assertTrue(unknownDossier.lineups.homeBench.isEmpty())
            assertTrue(unknownDossier.lineups.awayBench.isEmpty())
        } finally {
            db.close()
        }
    }

    @Test
    fun testKickoffV2ArchitectureAnchoredClockProviderAuthorityDynamicRankingsAndPalette() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Anchored Real-Time Match Clock (MatchClockEngine)
        com.example.data.repository.MatchClockEngine.clearRegistryForTests()
        val t0 = 1_000_000L
        val anchor1H = com.example.data.repository.MatchClockEngine.recordVerifiedObservation(
            matchId = "clock_1h_test",
            liveMinuteOrBadge = "1H • 15'",
            status = com.example.data.repository.MatchStatus.LIVE,
            observedAtEpochMs = t0
        )
        assertEquals(
            "1H • 22'",
            com.example.data.repository.MatchClockEngine.computeLocalBadgeFromAnchor(
                anchor = anchor1H,
                nowEpochMs = t0 + 7 * 60_000L
            )
        )

        val anchor2HStoppage = com.example.data.repository.MatchClockEngine.recordVerifiedObservation(
            matchId = "clock_2h_stoppage_test",
            liveMinuteOrBadge = "2H • 90'",
            status = com.example.data.repository.MatchStatus.LIVE,
            observedAtEpochMs = t0,
            officialAddedMinutes = 7
        )
        assertEquals(
            "2H • 90+7'",
            com.example.data.repository.MatchClockEngine.computeLocalBadgeFromAnchor(
                anchor = anchor2HStoppage,
                nowEpochMs = t0 + 7 * 60_000L
            )
        )

        // Verification Horizon checks: true at 1H • 43', false at 1H • 25', true at 2H • 88', true at HT elapsed 14 min
        val anchorAt43 = com.example.data.repository.MatchClockEngine.recordVerifiedObservation(
            matchId = "horizon_43",
            liveMinuteOrBadge = "1H • 43'",
            status = com.example.data.repository.MatchStatus.LIVE,
            observedAtEpochMs = t0
        )
        assertTrue(com.example.data.repository.MatchClockEngine.isMatchInVerificationHorizon(anchorAt43, t0))

        val anchorAt25 = com.example.data.repository.MatchClockEngine.recordVerifiedObservation(
            matchId = "horizon_25",
            liveMinuteOrBadge = "1H • 25'",
            status = com.example.data.repository.MatchStatus.LIVE,
            observedAtEpochMs = t0
        )
        assertFalse(com.example.data.repository.MatchClockEngine.isMatchInVerificationHorizon(anchorAt25, t0))

        val anchorAt88 = com.example.data.repository.MatchClockEngine.recordVerifiedObservation(
            matchId = "horizon_88",
            liveMinuteOrBadge = "2H • 88'",
            status = com.example.data.repository.MatchStatus.LIVE,
            observedAtEpochMs = t0
        )
        assertTrue(com.example.data.repository.MatchClockEngine.isMatchInVerificationHorizon(anchorAt88, t0))

        val anchorHt = com.example.data.repository.MatchClockEngine.recordVerifiedObservation(
            matchId = "horizon_ht",
            liveMinuteOrBadge = "HT",
            status = com.example.data.repository.MatchStatus.HALFTIME,
            observedAtEpochMs = t0
        )
        assertFalse(com.example.data.repository.MatchClockEngine.isMatchInVerificationHorizon(anchorHt, t0 + 5 * 60_000L))
        assertTrue(com.example.data.repository.MatchClockEngine.isMatchInVerificationHorizon(anchorHt, t0 + 14 * 60_000L))

        // 2. Provider Authority & VAR Disallowed-Goal Rollback (ProviderAuthorityEngine)
        val baseFixture = com.example.data.repository.FootballMatch(
            matchId = "var_rollback_fixture",
            tournamentId = "uefa_cl",
            tournamentName = "UEFA Champions League",
            tournamentRegion = "Europe",
            dayTab = com.example.data.repository.MatchDayTab.TODAY,
            kickoffTimeFormatted = "20:00",
            kickoffDateFormatted = "Today",
            status = com.example.data.repository.MatchStatus.LIVE,
            liveMinuteOrBadge = "2H • 64'",
            homeTeamId = "rm",
            homeTeamName = "Real Madrid",
            homeTeamBadge = "",
            homeScore = 1,
            awayTeamId = "bay",
            awayTeamName = "Bayern Munich",
            awayTeamBadge = "",
            awayScore = 0,
            stadiumName = "Santiago Bernabéu",
            cityName = "Madrid",
            beinChannel = "beIN Sports 1 HD",
            beinChannelNumberHint = "1"
        )

        val tsdbObservation = com.example.data.repository.ProviderMatchObservation(
            provider = com.example.data.repository.MatchDataProvider.THESPORTSDB_V1,
            match = baseFixture.copy(
                homeScore = 2,
                awayScore = 0,
                homeTeamBadge = "https://r2.thesportsdb.com/images/media/team/badge/rm/tiny",
                awayTeamBadge = "https://r2.thesportsdb.com/images/media/team/badge/bay/tiny"
            ),
            observedAtEpochMs = t0
        )
        val apiFootballObservation = com.example.data.repository.ProviderMatchObservation(
            provider = com.example.data.repository.MatchDataProvider.API_FOOTBALL_V3,
            match = baseFixture.copy(
                homeScore = 1,
                awayScore = 0,
                liveMinuteOrBadge = "2H • 65'",
                apiFootballFixtureId = "998877"
            ),
            observedAtEpochMs = t0 + 15_000L
        )
        val espnObservation = com.example.data.repository.ProviderMatchObservation(
            provider = com.example.data.repository.MatchDataProvider.ESPN_SCOREBOARD,
            match = baseFixture.copy(
                homeScore = 1,
                awayScore = 0,
                liveMinuteOrBadge = "2H • 65'",
                espnEventId = "776655"
            ),
            observedAtEpochMs = t0 + 12_000L
        )

        val resolvedFixture = com.example.data.repository.ProviderAuthorityEngine.resolveMultiProviderFixture(
            observations = listOf(tsdbObservation, apiFootballObservation, espnObservation),
            nowEpochMs = t0 + 15_000L
        )
        assertEquals(1, resolvedFixture.homeScore)
        assertEquals(0, resolvedFixture.awayScore)
        assertTrue(resolvedFixture.homeTeamBadge.endsWith("/tiny"))
        assertTrue(resolvedFixture.awayTeamBadge.endsWith("/tiny"))

        // 3. Restructured Competitions (CompetitionCategory & caf_confed)
        val allTournaments = com.example.data.repository.FootballRepository.ALL_TOURNAMENTS
        val europeanIds = allTournaments
            .filter { it.category == com.example.data.repository.CompetitionCategory.EUROPEAN }
            .map { it.tournamentId }
        val africanArabIds = allTournaments
            .filter { it.category == com.example.data.repository.CompetitionCategory.AFRICAN_ARAB }
            .map { it.tournamentId }
        val internationalIds = allTournaments
            .filter { it.category == com.example.data.repository.CompetitionCategory.INTERNATIONAL }
            .map { it.tournamentId }

        assertEquals(
            listOf("uefa_cl", "uefa_el", "premier_league", "laliga", "serie_a", "bundesliga", "ligue_1"),
            europeanIds
        )
        assertEquals(
            listOf("caf_cl", "caf_confed", "botola_pro", "saudi_pro"),
            africanArabIds
        )
        assertEquals(
            listOf("intl_official", "intl_friendly"),
            internationalIds
        )

        // 4. Dynamic International Ranking Filter (InternationalRankingResolver)
        val rankingResolver = com.example.data.repository.InternationalRankingResolver(context)
        val qualifiedMap = rankingResolver.getQualifiedTeamsByConfederation()
        assertEquals(5, qualifiedMap[com.example.data.repository.FootballConfederation.CAF]?.size)
        assertEquals(3, qualifiedMap[com.example.data.repository.FootballConfederation.AFC]?.size)
        assertEquals(7, qualifiedMap[com.example.data.repository.FootballConfederation.UEFA]?.size)
        assertEquals(4, qualifiedMap[com.example.data.repository.FootballConfederation.CONMEBOL]?.size)
        assertEquals(19, rankingResolver.getQualifiedNationalTeams().size)

        assertTrue(rankingResolver.isQualifiedInternationalFixture("Morocco", "Tanzania"))
        assertFalse(rankingResolver.isQualifiedInternationalFixture("Tanzania", "Zambia"))

        val simulatedRankingUpdateJson = """
            [
              {"fifaCode": "MLI", "iso2": "ML", "confederation": "CAF", "globalRank": 10, "points": 1735.0, "countryName": "Mali"},
              {"fifaCode": "MAR", "iso2": "MA", "confederation": "CAF", "globalRank": 12, "points": 1710.0, "countryName": "Morocco"},
              {"fifaCode": "SEN", "iso2": "SN", "confederation": "CAF", "globalRank": 17, "points": 1640.0, "countryName": "Senegal"},
              {"fifaCode": "EGY", "iso2": "EG", "confederation": "CAF", "globalRank": 30, "points": 1525.0, "countryName": "Egypt"},
              {"fifaCode": "NGA", "iso2": "NG", "confederation": "CAF", "globalRank": 36, "points": 1505.0, "countryName": "Nigeria"},
              {"fifaCode": "ALG", "iso2": "DZ", "confederation": "CAF", "globalRank": 40, "points": 1495.0, "countryName": "Algeria"},
              {"fifaCode": "JPN", "iso2": "JP", "confederation": "AFC", "globalRank": 15, "points": 1655.0, "countryName": "Japan"},
              {"fifaCode": "IRN", "iso2": "IR", "confederation": "AFC", "globalRank": 19, "points": 1625.0, "countryName": "Iran"},
              {"fifaCode": "KOR", "iso2": "KR", "confederation": "AFC", "globalRank": 22, "points": 1590.0, "countryName": "South Korea"},
              {"fifaCode": "ESP", "iso2": "ES", "confederation": "UEFA", "globalRank": 2, "points": 1855.0, "countryName": "Spain"},
              {"fifaCode": "FRA", "iso2": "FR", "confederation": "UEFA", "globalRank": 3, "points": 1850.0, "countryName": "France"},
              {"fifaCode": "ENG", "iso2": "GB-ENG", "confederation": "UEFA", "globalRank": 4, "points": 1815.0, "countryName": "England"},
              {"fifaCode": "POR", "iso2": "PT", "confederation": "UEFA", "globalRank": 6, "points": 1775.0, "countryName": "Portugal"},
              {"fifaCode": "NED", "iso2": "NL", "confederation": "UEFA", "globalRank": 7, "points": 1765.0, "countryName": "Netherlands"},
              {"fifaCode": "BEL", "iso2": "BE", "confederation": "UEFA", "globalRank": 8, "points": 1750.0, "countryName": "Belgium"},
              {"fifaCode": "ITA", "iso2": "IT", "confederation": "UEFA", "globalRank": 9, "points": 1740.0, "countryName": "Italy"},
              {"fifaCode": "ARG", "iso2": "AR", "confederation": "CONMEBOL", "globalRank": 1, "points": 1885.0, "countryName": "Argentina"},
              {"fifaCode": "BRA", "iso2": "BR", "confederation": "CONMEBOL", "globalRank": 5, "points": 1785.0, "countryName": "Brazil"},
              {"fifaCode": "COL", "iso2": "CO", "confederation": "CONMEBOL", "globalRank": 11, "points": 1720.0, "countryName": "Colombia"},
              {"fifaCode": "URU", "iso2": "UY", "confederation": "CONMEBOL", "globalRank": 14, "points": 1665.0, "countryName": "Uruguay"}
            ]
        """.trimIndent()
        assertTrue(rankingResolver.updateRankingsFromJson(simulatedRankingUpdateJson).isNotEmpty())
        assertTrue(rankingResolver.isQualifiedInternationalFixture("Mali", "Tanzania"))

        // 5. Official 6-Color Palette Verification (Color.kt)
        assertEquals(Color(0xFFD9541E), KickoffTerracotta)
        assertEquals(Color(0xFF0F1B3D), KickoffImperialNavy)
        assertEquals(Color(0xFF111111), KickoffObsidianBlack)
        assertEquals(Color(0xFFF3EEE3), KickoffAlabasterIvory)
        assertEquals(Color(0xFFD8CBB3), KickoffAgedParchment)
        assertEquals(Color(0xFF1E1E1C), KickoffCharcoalSlate)
        assertEquals(Color(0x33D9541E), KickoffTerracottaGlow)
        assertEquals(Color(0xFF162654), KickoffNavyElevated)
        assertEquals(Color(0x2ED8CBB3), KickoffParchmentBorder)
        assertEquals(Color(0xB3D8CBB3), KickoffParchmentMuted)
        assertEquals(Color(0xFF272724), KickoffCharcoalElevated)

        // 6. Phase 1 Strict Senior First-Team Guard, Date Verification, National Team Deduplication & Timezone Source-of-Truth
        assertFalse(
            "U21 fixtures must be rejected by isSeniorFirstTeamFixture",
            com.example.data.repository.FootballRepository.isSeniorFirstTeamFixture("Millwall U21", "Brighton U21", "Premier League 2", "Regular Season")
        )
        assertFalse(
            "Youth/Reserve teams must be rejected even if league name says Premier League",
            com.example.data.repository.FootballRepository.isSeniorFirstTeamFixture("Arsenal U21", "Chelsea U21", "English Premier League", "Round 12")
        )
        assertFalse(
            "Reserve squads (Castilla / B team) must be rejected",
            com.example.data.repository.FootballRepository.isSeniorFirstTeamFixture("Real Madrid Castilla", "Barcelona B", "LaLiga", "Matchday 5")
        )
        assertTrue(
            "Senior first-team fixtures must be accepted",
            com.example.data.repository.FootballRepository.isSeniorFirstTeamFixture("Arsenal", "Chelsea", "English Premier League", "Round 12")
        )

        // Verify expectedIsoDate filtering rejects wrong-date fixtures
        assertTrue(
            com.example.data.repository.FootballRepository.doesEventMatchExpectedIsoDate(
                rawIsoOrDate = "2026-03-31T19:00:00+00:00",
                expectedIsoDate = "2026-03-31"
            )
        )
        assertFalse(
            "Stale or wrong-date fixtures must be rejected when expectedIsoDate is provided",
            com.example.data.repository.FootballRepository.doesEventMatchExpectedIsoDate(
                rawIsoOrDate = "2026-02-14T19:00:00+00:00",
                expectedIsoDate = "2026-03-31"
            )
        )

        // Verify Turkiye / Türkiye / Turkey normalize identically and merge into a single fixture
        assertEquals("turkiye", com.example.data.repository.InternationalRankingResolver.normalizeNationalTeamName("Türkiye"))
        assertEquals("turkiye", com.example.data.repository.InternationalRankingResolver.normalizeNationalTeamName("Turkiye"))
        assertEquals("turkiye", com.example.data.repository.InternationalRankingResolver.normalizeNationalTeamName("Turkey"))

        val db = Room.inMemoryDatabaseBuilder(context, StreamVaultDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = com.example.data.repository.FootballRepository(
                context = context,
                favoriteTeamDao = db.favoriteTeamDao(),
                tournamentPreferenceDao = db.tournamentPreferenceDao(),
                streamDao = db.streamDao()
            )
            val multiProviderTurkiyeJson365 = """
                {
                  "games": [
                    {
                      "id": 99001,
                      "competitionId": 352,
                      "competitionDisplayName": "International Friendly",
                      "startTime": "2025-05-10T18:45:00+00:00",
                      "statusGroup": 2,
                      "statusText": "Kickoff",
                      "homeCompetitor": {"id": 801, "name": "Turkiye", "score": -1},
                      "awayCompetitor": {"id": 802, "name": "Belgium", "score": -1}
                    },
                    {
                      "id": 99002,
                      "competitionId": 352,
                      "competitionDisplayName": "Friendly Match",
                      "startTime": "2025-05-10T18:45:00+00:00",
                      "statusGroup": 2,
                      "statusText": "Kickoff",
                      "homeCompetitor": {"id": 803, "name": "Turkey", "score": -1},
                      "awayCompetitor": {"id": 804, "name": "Belgium", "score": -1}
                    }
                  ]
                }
            """.trimIndent()
            val parsedTurkiyeFixtures = repo.parse365ScoresAllScoresJson(
                rawJson = multiProviderTurkiyeJson365,
                dayTab = com.example.data.repository.MatchDayTab.TODAY,
                expectedIsoDate = "2025-05-10"
            )
            val mergedTurkiyeMatches = repo.deduplicateMatchesByCanonicalTeams(parsedTurkiyeFixtures)
            assertEquals(
                "Turkiye vs Belgium and Turkey vs Belgium must merge into 1 canonical fixture",
                1,
                mergedTurkiyeMatches.size
            )
            assertEquals("Türkiye", mergedTurkiyeMatches[0].homeTeamName)
            assertEquals("Belgium", mergedTurkiyeMatches[0].awayTeamName)
        } finally {
            db.close()
        }

        // Verify SeasonTimeMode overload of formatDynamicKickoffTime for Morocco Winter (GMT+0) vs Summer (GMT+1)
        val moroccoZone = com.example.data.repository.UserZoneRegistry.getZoneByCode("MA")
        val sampleUtc20hEpochMs = 1746907200000L // 2025-05-10T20:00:00Z
        assertEquals(
            "20:00",
            com.example.data.repository.formatDynamicKickoffTime(
                sampleUtc20hEpochMs,
                moroccoZone,
                com.example.data.repository.SeasonTimeMode.WINTER
            )
        )
        assertEquals(
            "21:00",
            com.example.data.repository.formatDynamicKickoffTime(
                sampleUtc20hEpochMs,
                moroccoZone,
                com.example.data.repository.SeasonTimeMode.SUMMER
            )
        )

        // =========================================================================
        // 8. Phase 2: KickoffSourceMonitor & Encrypted Runtime API Key Management
        // =========================================================================
        val sourceMonitor = com.example.data.repository.KickoffSourceMonitor(
            context = context,
            httpClient = IptvRepository.sharedOkHttpClient
        )
        assertEquals(6, com.example.data.repository.KickoffProviderId.values().size)
        val initialStates = sourceMonitor.sourceHealthFlow.value
        assertEquals(6, initialStates.size)
        assertTrue(initialStates.any { it.provider == com.example.data.repository.KickoffProviderId.API_FOOTBALL })
        assertTrue(initialStates.any { it.provider == com.example.data.repository.KickoffProviderId.FOOTBALL_DATA_ORG })
        assertTrue(initialStates.any { it.provider == com.example.data.repository.KickoffProviderId.SPORT_API_AI })
        assertTrue(initialStates.any { it.provider == com.example.data.repository.KickoffProviderId.ESPN_SOCCER })
        assertTrue(initialStates.any { it.provider == com.example.data.repository.KickoffProviderId.SOFASCORE_LIVE })
        assertTrue(initialStates.any { it.provider == com.example.data.repository.KickoffProviderId.THESPORTSDB })

        // Verify passive runtime HTTP monitoring (200 OK -> HEALTHY, 429 -> QUOTA_WARNING, 401 -> KEY_INVALID_OR_EXPIRED)
        sourceMonitor.recordRuntimeResponse(
            provider = com.example.data.repository.KickoffProviderId.API_FOOTBALL,
            httpCode = 200,
            remainingQuota = 84,
            errorNote = "Live match sync succeeded"
        )
        val healthyApiFootball = sourceMonitor.sourceHealthFlow.value.first {
            it.provider == com.example.data.repository.KickoffProviderId.API_FOOTBALL
        }
        assertEquals(com.example.data.repository.SourceHealthStatus.HEALTHY, healthyApiFootball.status)
        assertEquals("84 / 100 req today", healthyApiFootball.remainingQuotaLabel)

        sourceMonitor.recordRuntimeResponse(
            provider = com.example.data.repository.KickoffProviderId.FOOTBALL_DATA_ORG,
            httpCode = 429
        )
        val rateLimitedFd = sourceMonitor.sourceHealthFlow.value.first {
            it.provider == com.example.data.repository.KickoffProviderId.FOOTBALL_DATA_ORG
        }
        assertEquals(com.example.data.repository.SourceHealthStatus.QUOTA_WARNING, rateLimitedFd.status)
        // V8 non-alarmist rule: single-provider rate limit does not raise an alert while API_FOOTBALL & keyless feeds are healthy
        assertNull(sourceMonitor.activeSourceAlertFlow.value)

        sourceMonitor.recordRuntimeResponse(
            provider = com.example.data.repository.KickoffProviderId.SPORT_API_AI,
            httpCode = 401
        )
        val unconfiguredSportApi = sourceMonitor.sourceHealthFlow.value.first {
            it.provider == com.example.data.repository.KickoffProviderId.SPORT_API_AI
        }
        assertEquals(com.example.data.repository.SourceHealthStatus.UNCONFIGURED, unconfiguredSportApi.status)
        assertNull(sourceMonitor.activeSourceAlertFlow.value)

        // When both primary keyed feeds (API_FOOTBALL & FOOTBALL_DATA_ORG) fail with 401, the alert banner activates
        sourceMonitor.recordRuntimeResponse(
            provider = com.example.data.repository.KickoffProviderId.API_FOOTBALL,
            httpCode = 401
        )
        sourceMonitor.recordRuntimeResponse(
            provider = com.example.data.repository.KickoffProviderId.FOOTBALL_DATA_ORG,
            httpCode = 401
        )
        assertNotNull(sourceMonitor.activeSourceAlertFlow.value)
        sourceMonitor.dismissAlert()
        assertNull(sourceMonitor.activeSourceAlertFlow.value)

        // Verify API-Football /status validation response parser (Active Account vs Token Error)
        val validStatusJson = """
            {
              "errors": [],
              "response": {
                "account": {"firstname": "Demo", "lastname": "User"},
                "subscription": {"plan": "Pro", "active": true},
                "requests": {"current": 12, "limit_day": 7500}
              }
            }
        """.trimIndent()
        val parsedValid = sourceMonitor.parseValidationResponse(
            provider = com.example.data.repository.KickoffProviderId.API_FOOTBALL,
            httpCode = 200,
            responseBody = validStatusJson,
            remainingHeader = null
        )
        assertTrue(parsedValid.isValid)
        assertEquals(com.example.data.repository.SourceHealthStatus.HEALTHY, parsedValid.status)
        assertEquals("7488 / 7500 req today", parsedValid.remainingQuotaLabel)

        val invalidStatusJson = """
            {
              "errors": {"token": "Error/Missing application key. Go to https://www.api-football.com to learn how to get your API application key."}
            }
        """.trimIndent()
        val parsedInvalid = sourceMonitor.parseValidationResponse(
            provider = com.example.data.repository.KickoffProviderId.API_FOOTBALL,
            httpCode = 200,
            responseBody = invalidStatusJson,
            remainingHeader = null
        )
        assertFalse(parsedInvalid.isValid)
        assertEquals(com.example.data.repository.SourceHealthStatus.KEY_INVALID_OR_EXPIRED, parsedInvalid.status)

        // Verify encrypted custom key storage and resetToDefaultKey
        sourceMonitor.saveCustomApiKeyInternal(
            com.example.data.repository.KickoffProviderId.API_FOOTBALL,
            "custom_apifootball_test_key_998877"
        )
        assertEquals(
            "custom_apifootball_test_key_998877",
            sourceMonitor.resolveActiveApiKey(com.example.data.repository.KickoffProviderId.API_FOOTBALL)
        )
        assertTrue(
            sourceMonitor.sourceHealthFlow.value.first {
                it.provider == com.example.data.repository.KickoffProviderId.API_FOOTBALL
            }.isUsingCustomKey
        )
        sourceMonitor.resetToDefaultKey(com.example.data.repository.KickoffProviderId.API_FOOTBALL)
        assertFalse(
            sourceMonitor.sourceHealthFlow.value.first {
                it.provider == com.example.data.repository.KickoffProviderId.API_FOOTBALL
            }.isUsingCustomKey
        )

        // =========================================================================
        // 9. Phase 3: "Floodlight Noir & Pitch Emerald" Brand Color Kit & Competition Accents
        // =========================================================================
        assertEquals(Color(0xFF070B0A), com.example.ui.theme.KickoffPitchBlack)
        assertEquals(Color(0xFF0E1614), com.example.ui.theme.KickoffTurfSurface)
        assertEquals(Color(0xFF15211E), com.example.ui.theme.KickoffCanopyElevated)
        assertEquals(Color(0xFF1C2C28), com.example.ui.theme.KickoffGlassWell)
        assertEquals(Color(0xFF10B981), com.example.ui.theme.KickoffPitchEmerald)
        assertEquals(Color(0xFFD4FF33), com.example.ui.theme.KickoffFloodlightVolt)
        assertEquals(Color(0xFF0B3B2D), com.example.ui.theme.KickoffStadiumForest)
        assertEquals(Color(0xFF0F4C3A), com.example.ui.theme.KickoffStadiumTurfAlt)
        assertEquals(Color(0xFFFF2E54), com.example.ui.theme.KickoffLiveCrimson)
        assertEquals(Color(0xFFF59E0B), com.example.ui.theme.KickoffTrophyGold)
        assertEquals(Color(0xFFF2F7F5), com.example.ui.theme.KickoffFloodlightWhite)
        assertEquals(Color(0xFF99AEA7), com.example.ui.theme.KickoffMistSilver)
        assertEquals(Color(0xFF5C736B), com.example.ui.theme.KickoffSlateMuted)
        assertEquals(Color(0x2910B981), com.example.ui.theme.KickoffEmeraldBorder)
        assertEquals(Color(0x33D4FF33), com.example.ui.theme.KickoffVoltGlow)
        assertEquals(Color(0x33FF2E54), com.example.ui.theme.KickoffCrimsonGlow)
        assertEquals(Color(0x14F2F7F5), com.example.ui.theme.KickoffRowDivider)

        assertEquals(com.example.ui.theme.CompUclBlue, com.example.ui.home.resolveCompetitionAccentColor("ucl"))
        assertEquals(com.example.ui.theme.CompUclBlue, com.example.ui.home.resolveCompetitionAccentColor("uefa_cl"))
        assertEquals(com.example.ui.theme.CompUelOrange, com.example.ui.home.resolveCompetitionAccentColor("uefa_el"))
        assertEquals(com.example.ui.theme.CompEplViolet, com.example.ui.home.resolveCompetitionAccentColor("epl"))
        assertEquals(com.example.ui.theme.CompBotolaCrimson, com.example.ui.home.resolveCompetitionAccentColor("botola_pro"))
        assertEquals(com.example.ui.theme.CompCafEmerald, com.example.ui.home.resolveCompetitionAccentColor("caf_cl"))
        assertEquals(com.example.ui.theme.CompCafEmerald, com.example.ui.home.resolveCompetitionAccentColor("caf_confed"))
        assertEquals(com.example.ui.theme.CompIntlGold, com.example.ui.home.resolveCompetitionAccentColor("intl_friendlies"))
        assertEquals(com.example.ui.theme.CompIntlGold, com.example.ui.home.resolveCompetitionAccentColor("intl_official"))

        // =========================================================================
        // 10. Phase 4: HomeScreen Symmetrical Broadcast Telemetry & Chronological Grouping
        // =========================================================================
        val sampleLiveMatch = com.example.data.repository.FootballMatch(
            matchId = "m_live_38",
            tournamentId = "uefa_cl",
            tournamentName = "UEFA Champions League",
            tournamentRegion = "Europe",
            dayTab = com.example.data.repository.MatchDayTab.TODAY,
            kickoffTimeFormatted = "20:00",
            kickoffDateFormatted = "Today",
            status = com.example.data.repository.MatchStatus.LIVE,
            liveMinuteOrBadge = "1H • 38'",
            homeTeamId = "rm",
            homeTeamName = "Real Madrid",
            homeTeamBadge = "⚪",
            homeScore = 1,
            awayTeamId = "ars",
            awayTeamName = "Arsenal",
            awayTeamBadge = "🔴",
            awayScore = 0,
            stadiumName = "Santiago Bernabéu",
            cityName = "Madrid",
            beinChannel = "beIN Sports 1 HD",
            beinChannelNumberHint = "beIN 1"
        )
        val liveTelemetry1H = com.example.ui.home.parseLiveMatchClockTelemetry(sampleLiveMatch)
        assertTrue(liveTelemetry1H.periodBadge.contains("1ST HALF"))
        assertEquals("38'", liveTelemetry1H.heroReadout)
        assertTrue(liveTelemetry1H.progressFraction in 0.40f..0.45f)

        val liveTelemetryHT = com.example.ui.home.parseLiveMatchClockTelemetry(
            sampleLiveMatch.copy(status = com.example.data.repository.MatchStatus.HALFTIME, liveMinuteOrBadge = "HT")
        )
        assertEquals("HALF TIME", liveTelemetryHT.periodBadge)
        assertEquals("HT", liveTelemetryHT.heroReadout)
        assertEquals(0.50f, liveTelemetryHT.progressFraction, 0.01f)
    }

    @Test
    fun testKickoffV4SeniorTeamGateDateVerificationSourceMonitorAndBrandKit() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Senior First-Team Gate (isSeniorFirstTeamFixture)
        assertFalse(
            com.example.data.repository.FootballRepository.isSeniorFirstTeamFixture(
                "Millwall U21",
                "Brighton U21",
                "Premier League 2",
                "Regular Season"
            )
        )
        assertFalse(
            com.example.data.repository.FootballRepository.isSeniorFirstTeamFixture(
                "Arsenal U23",
                "Chelsea U23",
                "English Premier League",
                "Round 10"
            )
        )
        assertFalse(
            com.example.data.repository.FootballRepository.isSeniorFirstTeamFixture(
                "Barcelona B",
                "Real Madrid Castilla",
                "LaLiga",
                "Matchday 8"
            )
        )
        assertFalse(
            com.example.data.repository.FootballRepository.isSeniorFirstTeamFixture(
                "Chelsea Women",
                "Arsenal Women",
                "English Premier League",
                "Round 4"
            )
        )
        assertTrue(
            com.example.data.repository.FootballRepository.isSeniorFirstTeamFixture(
                "Arsenal",
                "Liverpool",
                "English Premier League",
                "Matchday 28"
            )
        )
        assertTrue(
            com.example.data.repository.FootballRepository.isSeniorFirstTeamFixture(
                "Raja Club Athletic",
                "Wydad AC",
                "Botola Pro Inwi",
                "Round 21"
            )
        )

        // 2. Foreign League & Wrong-Date Rejection in parse365ScoresAllScoresJson + National-Team Deduplication & Top-N Rule
        val db = Room.inMemoryDatabaseBuilder(context, StreamVaultDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = com.example.data.repository.FootballRepository(
                context = context,
                favoriteTeamDao = db.favoriteTeamDao(),
                tournamentPreferenceDao = db.tournamentPreferenceDao(),
                streamDao = db.streamDao()
            )
            val foreignAndStale365Json = """
                {
                  "games": [
                    {
                      "id": 88001,
                      "competitionId": 552,
                      "competitionDisplayName": "Egyptian Cup",
                      "countryName": "Egypt",
                      "startTime": "2025-05-10T18:00:00+00:00",
                      "statusGroup": 2,
                      "statusText": "Kickoff",
                      "homeCompetitor": {"id": 501, "name": "Al Mokawloon Al Arab", "score": -1},
                      "awayCompetitor": {"id": 502, "name": "Egypt Telecom", "score": -1}
                    },
                    {
                      "id": 88002,
                      "competitionId": 9991,
                      "competitionDisplayName": "Premier League",
                      "countryName": "Jamaica",
                      "startTime": "2025-05-10T19:00:00+00:00",
                      "statusGroup": 2,
                      "statusText": "Kickoff",
                      "homeCompetitor": {"id": 503, "name": "Arnett Gardens", "score": -1},
                      "awayCompetitor": {"id": 504, "name": "Waterhouse FC", "score": -1}
                    },
                    {
                      "id": 88003,
                      "competitionId": 7,
                      "competitionDisplayName": "Premier League",
                      "countryName": "England",
                      "startTime": "2025-05-09T19:00:00+00:00",
                      "statusGroup": 2,
                      "statusText": "Kickoff",
                      "homeCompetitor": {"id": 505, "name": "Arsenal", "score": -1},
                      "awayCompetitor": {"id": 506, "name": "Liverpool", "score": -1}
                    },
                    {
                      "id": 88004,
                      "competitionId": 7,
                      "competitionDisplayName": "Premier League",
                      "countryName": "England",
                      "startTime": "2025-05-10T19:00:00+00:00",
                      "statusGroup": 2,
                      "statusText": "Kickoff",
                      "homeCompetitor": {"id": 505, "name": "Arsenal", "score": -1},
                      "awayCompetitor": {"id": 506, "name": "Liverpool", "score": -1}
                    }
                  ]
                }
            """.trimIndent()

            val parsedValidMatches = repo.parse365ScoresAllScoresJson(
                rawJson = foreignAndStale365Json,
                dayTab = com.example.data.repository.MatchDayTab.TODAY,
                expectedIsoDate = "2025-05-10"
            )
            assertEquals(1, parsedValidMatches.size)
            assertEquals("Arsenal", parsedValidMatches[0].homeTeamName)
            assertEquals("Liverpool", parsedValidMatches[0].awayTeamName)

            // 3. National-Team Deduplication & Single-Team Top-N Rule
            assertEquals(
                com.example.data.repository.FootballRepository.normalizeTeamNameForMerge("Turkiye"),
                com.example.data.repository.FootballRepository.normalizeTeamNameForMerge("Turkey")
            )
            val rankingResolver = com.example.data.repository.InternationalRankingResolver(
                context = context,
                httpClient = IptvRepository.sharedOkHttpClient
            )
            assertTrue(rankingResolver.isQualifiedInternationalFixture("Morocco", "Niger"))
            assertFalse(rankingResolver.isQualifiedInternationalFixture("San Marino", "Liechtenstein"))
        } finally {
            db.close()
        }

        // 4. API Key Monitoring & Runtime Override (KickoffSourceMonitor)
        val sourceMonitor = com.example.data.repository.KickoffSourceMonitor(
            context = context,
            httpClient = IptvRepository.sharedOkHttpClient
        )
        sourceMonitor.saveCustomApiKeyInternal(
            com.example.data.repository.KickoffProviderId.API_FOOTBALL,
            "v4_runtime_override_key_12345"
        )
        assertEquals(
            "v4_runtime_override_key_12345",
            sourceMonitor.resolveActiveApiKey(com.example.data.repository.KickoffProviderId.API_FOOTBALL)
        )
        sourceMonitor.recordRuntimeResponse(
            provider = com.example.data.repository.KickoffProviderId.API_FOOTBALL,
            httpCode = 401
        )
        assertEquals(
            com.example.data.repository.SourceHealthStatus.KEY_INVALID_OR_EXPIRED,
            sourceMonitor.sourceHealthFlow.value.first {
                it.provider == com.example.data.repository.KickoffProviderId.API_FOOTBALL
            }.status
        )
        sourceMonitor.resetToDefaultKey(com.example.data.repository.KickoffProviderId.API_FOOTBALL)
        assertFalse(
            sourceMonitor.sourceHealthFlow.value.first {
                it.provider == com.example.data.repository.KickoffProviderId.API_FOOTBALL
            }.isUsingCustomKey
        )

        // 5. Floodlight Noir & Pitch Emerald Brand Color Kit (Color.kt) & Clean Competition Header
        assertEquals(Color(0xFF070B0A), com.example.ui.theme.KickoffPitchBlack)
        assertEquals(Color(0xFF0E1614), com.example.ui.theme.KickoffTurfSurface)
        assertEquals(Color(0xFF10B981), com.example.ui.theme.KickoffPitchEmerald)
        assertEquals(Color(0xFFD4FF33), com.example.ui.theme.KickoffFloodlightVolt)
        assertEquals(Color(0xFF0B3B2D), com.example.ui.theme.KickoffStadiumForest)
        assertEquals(Color(0xFFFF2E54), com.example.ui.theme.KickoffLiveCrimson)

        val intlMatch = com.example.data.repository.FootballMatch(
            matchId = "intl_1",
            tournamentId = "intl_official",
            tournamentName = "International Official Fixtures",
            tournamentRegion = "International",
            dayTab = com.example.data.repository.MatchDayTab.TODAY,
            kickoffTimeFormatted = "20:00",
            kickoffDateFormatted = "Today",
            status = com.example.data.repository.MatchStatus.UPCOMING,
            liveMinuteOrBadge = "20:00",
            homeTeamId = "mar",
            homeTeamName = "Morocco",
            homeTeamBadge = "",
            homeScore = null,
            awayTeamId = "nig",
            awayTeamName = "Niger",
            awayTeamBadge = "",
            awayScore = null,
            stadiumName = "Main Stadium",
            cityName = "International",
            beinChannel = "Arryadia TNT HD",
            beinChannelNumberHint = "Arryadia"
        )
        assertEquals(
            "INTERNATIONAL OFFICIAL FIXTURES",
            com.example.ui.home.buildCleanCompetitionHeaderTitle(intlMatch)
        )
    }

    @Test
    fun testKickoffV5OverhaulSevenDayTimelineBroadcastIdentityUnifiedDeckAndStageDedup() {
        // 1. 7-Day Forward Timeline (TODAY + 6 upcoming days, YESTERDAY excluded from forward tabs)
        val forwardTabs = com.example.data.repository.MatchDayTab.forwardTimelineTabs
        assertEquals(7, forwardTabs.size)
        assertEquals(com.example.data.repository.MatchDayTab.TODAY, forwardTabs[0])
        assertEquals(com.example.data.repository.MatchDayTab.DAY_PLUS_6, forwardTabs[6])
        assertFalse(forwardTabs.contains(com.example.data.repository.MatchDayTab.YESTERDAY))
        assertEquals(listOf(0, 1, 2, 3, 4, 5, 6), forwardTabs.map { it.dayOffset })
        assertEquals("TODAY", com.example.data.repository.MatchDayTab.TODAY.getShortDayCode())
        assertEquals("TOM", com.example.data.repository.MatchDayTab.TOMORROW.getShortDayCode())

        // 2. Broadcast Provider Identity Color Tokens & Resolver
        val beinIdentity = com.example.ui.home.resolveBroadcasterVisualIdentity("beIN Sports 1 HD Premium")
        assertEquals("beIN", beinIdentity.providerShortTag)
        assertEquals(com.example.ui.theme.ProviderBeinSurface, beinIdentity.containerColor)
        assertEquals(com.example.ui.theme.ProviderBeinBorder, beinIdentity.borderColor)

        val arryadiaIdentity = com.example.ui.home.resolveBroadcasterVisualIdentity("Arryadia TNT HD")
        assertEquals("SNRT", arryadiaIdentity.providerShortTag)
        assertEquals(com.example.ui.theme.ProviderArryadiaSurface, arryadiaIdentity.containerColor)

        val sscIdentity = com.example.ui.home.resolveBroadcasterVisualIdentity("SSC 1 HD")
        assertEquals("SSC", sscIdentity.providerShortTag)
        assertEquals(com.example.ui.theme.ProviderSscSurface, sscIdentity.containerColor)

        val daznIdentity = com.example.ui.home.resolveBroadcasterVisualIdentity("DAZN 1 FR")
        assertEquals("DAZN", daznIdentity.providerShortTag)
        assertEquals(com.example.ui.theme.ProviderCanalDaznSurface, daznIdentity.containerColor)

        // 3. Unified Competition Grouping (Each tournamentId appears at most ONCE per matchday)
        val eplMorning = com.example.data.repository.FootballMatch(
            matchId = "epl_early",
            tournamentId = "epl",
            tournamentName = "Premier League",
            tournamentRegion = "England",
            dayTab = com.example.data.repository.MatchDayTab.TODAY,
            kickoffTimeFormatted = "13:30",
            kickoffDateFormatted = "Today",
            status = com.example.data.repository.MatchStatus.FINISHED,
            liveMinuteOrBadge = "FT",
            homeTeamId = "ars",
            homeTeamName = "Arsenal",
            homeTeamBadge = "",
            homeScore = 2,
            awayTeamId = "che",
            awayTeamName = "Chelsea",
            awayTeamBadge = "",
            awayScore = 1,
            stadiumName = "Emirates Stadium",
            cityName = "London",
            beinChannel = "beIN Sports 1 English",
            beinChannelNumberHint = "beIN EN 1"
        )
        val laligaMid = eplMorning.copy(
            matchId = "laliga_mid",
            tournamentId = "laliga",
            tournamentName = "LaLiga",
            tournamentRegion = "Spain",
            kickoffTimeFormatted = "16:15",
            status = com.example.data.repository.MatchStatus.LIVE,
            liveMinuteOrBadge = "64'"
        )
        val eplEvening = eplMorning.copy(
            matchId = "epl_late",
            tournamentId = "epl",
            tournamentName = "Premier League",
            tournamentRegion = "England",
            kickoffTimeFormatted = "20:00",
            status = com.example.data.repository.MatchStatus.UPCOMING,
            liveMinuteOrBadge = "20:00",
            homeScore = null,
            awayScore = null
        )
        val unifiedSections = com.example.ui.home.buildUnifiedTournamentSections(
            matches = listOf(eplMorning, laligaMid, eplEvening)
        )
        assertEquals(2, unifiedSections.size)
        assertEquals(1, unifiedSections.count { it.tournamentId == "epl" })
        val eplSection = unifiedSections.first { it.tournamentId == "epl" }
        assertEquals(2, eplSection.matches.size)

        // 4. Rich Statuses (POSTPONED & CANCELLED) & Stage Label Deduplication
        val postponedMatch = eplEvening.copy(
            status = com.example.data.repository.MatchStatus.POSTPONED,
            liveMinuteOrBadge = "PST"
        )
        val telemetry = com.example.ui.home.parseLiveMatchClockTelemetry(postponedMatch)
        assertEquals("PST", telemetry.heroReadout)
        assertEquals("POSTPONED", telemetry.periodBadge)
        assertEquals(0f, telemetry.progressFraction, 0.001f)

        assertEquals(
            "",
            com.example.ui.home.buildDistinctStageLabel("Premier League", "Premier League")
        )
        assertEquals(
            "",
            com.example.ui.home.buildDistinctStageLabel("Botola Pro Inwi", "Botola Pro Inwi • Regular Season")
        )
        assertEquals(
            "Quarter-Finals",
            com.example.ui.home.buildDistinctStageLabel("UEFA Champions League", "UEFA Champions League - Quarter-Finals")
        )
    }

    @Test
    fun testKickoffV6OnDemandSelectorsTopBarSettingsCenterAndCrestMatchCards() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, StreamVaultDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = com.example.data.repository.FootballRepository(
                context = context,
                favoriteTeamDao = db.favoriteTeamDao(),
                tournamentPreferenceDao = db.tournamentPreferenceDao(),
                streamDao = db.streamDao()
            )

            // 1. Persisted KickoffUiPreferences Round-Trip
            val expectedPrefs = com.example.ui.home.KickoffUiPreferences(
                use24HourFormat = false,
                hideLiveAndFinalScores = true,
                compactCardDensity = true,
                autoFallbackEmptyLeagueDay = true
            )
            repo.saveKickoffUiPreferences(expectedPrefs)
            val loadedPrefs = repo.getKickoffUiPreferences()
            assertEquals(expectedPrefs, loadedPrefs)
            assertFalse(loadedPrefs.use24HourFormat)
            assertTrue(loadedPrefs.hideLiveAndFinalScores)
            assertTrue(loadedPrefs.compactCardDensity)
            assertTrue(loadedPrefs.autoFallbackEmptyLeagueDay)

            // 2. Competition Header Region Deduplication (shouldShowTournamentRegionBadge)
            assertFalse(
                com.example.ui.home.shouldShowTournamentRegionBadge(
                    "International Official Fixtures",
                    "International"
                )
            )
            assertFalse(
                com.example.ui.home.shouldShowTournamentRegionBadge(
                    "International Friendlies",
                    "International"
                )
            )
            assertTrue(
                com.example.ui.home.shouldShowTournamentRegionBadge(
                    "Premier League",
                    "England"
                )
            )

            // 3. Emoji-Free Match Buzz Insights
            val sampleMatch = com.example.data.repository.FootballMatch(
                matchId = "v6_match_1",
                tournamentId = "premier_league",
                tournamentName = "Premier League",
                tournamentRegion = "England",
                dayTab = com.example.data.repository.MatchDayTab.TODAY,
                kickoffTimeFormatted = "20:00",
                kickoffDateFormatted = "Today",
                status = com.example.data.repository.MatchStatus.UPCOMING,
                liveMinuteOrBadge = "20:00",
                homeTeamId = "ars",
                homeTeamName = "Arsenal",
                homeTeamBadge = "",
                homeScore = null,
                awayTeamId = "liv",
                awayTeamName = "Liverpool",
                awayTeamBadge = "",
                awayScore = null,
                stadiumName = "Emirates Stadium",
                cityName = "London",
                beinChannel = "beIN Sports 1 English",
                beinChannelNumberHint = "beIN EN 1"
            )
            val dossier = com.example.data.repository.MatchCenterEnricher(okhttp3.OkHttpClient())
                .buildCompleteDossier(sampleMatch)
            assertTrue(dossier.matchBuzzInsights.isNotEmpty())
            val forbiddenEmojis = listOf("🏟", "📺", "🔥", "⚽", "🏆")
            dossier.matchBuzzInsights.forEach { insight ->
                forbiddenEmojis.forEach { emoji ->
                    assertFalse(
                        "Match buzz insight should not start with raw emoji $emoji: $insight",
                        insight.trimStart().startsWith(emoji)
                    )
                }
            }

            // 4. Postponed & Cancelled Upstream Parser Mapping
            val postponed365Json = """
                {
                  "games": [
                    {
                      "id": 99001,
                      "competitionId": 7,
                      "competitionDisplayName": "Premier League",
                      "countryName": "England",
                      "startTime": "2025-05-10T19:00:00+00:00",
                      "statusGroup": 2,
                      "statusText": "Postponed",
                      "homeCompetitor": {"id": 505, "name": "Arsenal", "score": 0},
                      "awayCompetitor": {"id": 506, "name": "Liverpool", "score": 0}
                    }
                  ]
                }
            """.trimIndent()
            val parsed365 = repo.parse365ScoresAllScoresJson(
                rawJson = postponed365Json,
                dayTab = com.example.data.repository.MatchDayTab.TODAY,
                expectedIsoDate = "2025-05-10"
            )
            assertEquals(1, parsed365.size)
            assertEquals(com.example.data.repository.MatchStatus.POSTPONED, parsed365[0].status)
            assertNull(parsed365[0].homeScore)
            assertNull(parsed365[0].awayScore)

            val postponedFdoJson = """
                {
                  "matches": [
                    {
                      "id": 99002,
                      "utcDate": "2025-05-10T19:00:00Z",
                      "status": "POSTPONED",
                      "matchday": 36,
                      "stage": "REGULAR_SEASON",
                      "area": {"name": "England"},
                      "competition": {"code": "PL", "name": "Premier League"},
                      "homeTeam": {"id": 57, "name": "Arsenal FC", "shortName": "Arsenal"},
                      "awayTeam": {"id": 64, "name": "Liverpool FC", "shortName": "Liverpool"},
                      "score": {
                        "fullTime": {"home": 0, "away": 0},
                        "halfTime": {"home": 0, "away": 0}
                      }
                    }
                  ]
                }
            """.trimIndent()
            val parsedFdo = repo.parseFootballDataOrgMatchesJson(
                rawJson = postponedFdoJson,
                dayTab = com.example.data.repository.MatchDayTab.TODAY,
                expectedIsoDate = "2025-05-10"
            )
            assertEquals(1, parsedFdo.size)
            assertEquals(com.example.data.repository.MatchStatus.POSTPONED, parsedFdo[0].status)
            assertNull(parsedFdo[0].homeScore)
            assertNull(parsedFdo[0].awayScore)
        } finally {
            db.close()
        }
    }

    @Test
    fun testKickoffV7ApiFootballDateWindowStickyDateStripSidePanelAndCleanCards() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. API-Football v3 Free-Plan Date Window Protection
        val sourceMonitor = com.example.data.repository.KickoffSourceMonitor(
            context = context,
            httpClient = IptvRepository.sharedOkHttpClient
        )
        sourceMonitor.recordRuntimeResponse(
            provider = com.example.data.repository.KickoffProviderId.API_FOOTBALL,
            httpCode = 200,
            remainingQuota = 99,
            errorNote = "Free plans do not have access to this date, try from 2026-11-14 to 2026-11-16."
        )
        val apiFootballSnapshot = sourceMonitor.sourceHealthFlow.value.first {
            it.provider == com.example.data.repository.KickoffProviderId.API_FOOTBALL
        }
        assertEquals(
            com.example.data.repository.SourceHealthStatus.HEALTHY,
            apiFootballSnapshot.status
        )
        assertNull(sourceMonitor.activeSourceAlertFlow.value)

        // 2. 7-Day Forward Instant Date Strip & Non-Empty Competition Grouping
        val forwardTabs = com.example.data.repository.MatchDayTab.forwardTimelineTabs
        assertEquals(7, forwardTabs.size)
        assertEquals(com.example.data.repository.MatchDayTab.TODAY, forwardTabs.first())
        assertEquals(com.example.data.repository.MatchDayTab.DAY_PLUS_6, forwardTabs.last())

        val earlyFixture = com.example.data.repository.FootballMatch(
            matchId = "v7_epl_1",
            tournamentId = "premier_league",
            tournamentName = "Premier League",
            tournamentRegion = "England",
            dayTab = com.example.data.repository.MatchDayTab.TODAY,
            kickoffTimeFormatted = "14:00",
            kickoffDateFormatted = "Today",
            status = com.example.data.repository.MatchStatus.UPCOMING,
            liveMinuteOrBadge = "14:00",
            homeTeamId = "ars",
            homeTeamName = "Arsenal",
            homeTeamBadge = "",
            homeScore = null,
            awayTeamId = "che",
            awayTeamName = "Chelsea",
            awayTeamBadge = "",
            awayScore = null,
            stadiumName = "Emirates Stadium",
            cityName = "London",
            beinChannel = "beIN Sports 1 HD",
            beinChannelNumberHint = "beIN 1"
        )
        val midFixture = earlyFixture.copy(
            matchId = "v7_laliga_1",
            tournamentId = "laliga",
            tournamentName = "LaLiga",
            tournamentRegion = "Spain",
            kickoffTimeFormatted = "17:00"
        )
        val lateFixture = earlyFixture.copy(
            matchId = "v7_epl_2",
            kickoffTimeFormatted = "20:00",
            homeTeamId = "liv",
            homeTeamName = "Liverpool",
            awayTeamId = "mci",
            awayTeamName = "Manchester City"
        )
        val sections = com.example.ui.home.buildUnifiedTournamentSections(
            matches = listOf(earlyFixture, midFixture, lateFixture)
        )
        assertEquals(2, sections.size)
        assertEquals(1, sections.count { it.tournamentId == "premier_league" })
        assertEquals(2, sections.first { it.tournamentId == "premier_league" }.matches.size)

        // 3. Dark-Glass Medallion & Broadcaster Identity Resolution
        val beinIdentity = com.example.ui.home.resolveBroadcasterVisualIdentity("beIN Sports 1 HD")
        assertEquals("beIN", beinIdentity.providerShortTag)
        assertEquals(
            "Sports 1 HD",
            com.example.ui.home.formatCleanBroadcastChannelSuffix("beIN Sports 1 HD", beinIdentity.providerShortTag)
        )
        assertFalse(
            com.example.ui.home.shouldShowTournamentRegionBadge(
                "International Official Fixtures",
                "International"
            )
        )
    }

    @Test
    fun testKickoffV8SmartSourceAlertsSquircleEmblemsAndStreamlinedNavigation() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Smart Keyless & Secondary Source Health Protection (KickoffSourceMonitor)
        val sourceMonitor = com.example.data.repository.KickoffSourceMonitor(
            context = context,
            httpClient = IptvRepository.sharedOkHttpClient
        )
        sourceMonitor.resetToDefaultKey(com.example.data.repository.KickoffProviderId.SPORT_API_AI)
        assertFalse(sourceMonitor.isUsingCustomKey(com.example.data.repository.KickoffProviderId.SPORT_API_AI))

        // Simulate Cloudflare 403 on a keyless public feed
        sourceMonitor.recordRuntimeResponse(
            provider = com.example.data.repository.KickoffProviderId.SOFASCORE_LIVE,
            httpCode = 403
        )
        val sofascoreSnapshot = sourceMonitor.sourceHealthFlow.value.first {
            it.provider == com.example.data.repository.KickoffProviderId.SOFASCORE_LIVE
        }
        assertEquals(
            com.example.data.repository.SourceHealthStatus.HEALTHY,
            sofascoreSnapshot.status
        )
        assertNull(sourceMonitor.activeSourceAlertFlow.value)

        // Simulate 401 on optional secondary SPORT_API_AI when no custom key is configured
        sourceMonitor.recordRuntimeResponse(
            provider = com.example.data.repository.KickoffProviderId.SPORT_API_AI,
            httpCode = 401
        )
        val sportApiSnapshot = sourceMonitor.sourceHealthFlow.value.first {
            it.provider == com.example.data.repository.KickoffProviderId.SPORT_API_AI
        }
        assertEquals(
            com.example.data.repository.SourceHealthStatus.UNCONFIGURED,
            sportApiSnapshot.status
        )
        assertNull(sourceMonitor.activeSourceAlertFlow.value)

        // 2. Custom Competition Vector Emblem Registry (hasCustomCompetitionVectorEmblem)
        assertTrue(com.example.ui.home.hasCustomCompetitionVectorEmblem("intl_official"))
        assertTrue(com.example.ui.home.hasCustomCompetitionVectorEmblem("intl_friendly"))
        assertTrue(com.example.ui.home.hasCustomCompetitionVectorEmblem("caf_cl"))
        assertTrue(com.example.ui.home.hasCustomCompetitionVectorEmblem("caf_confed"))
        assertFalse(com.example.ui.home.hasCustomCompetitionVectorEmblem("uefa_cl"))
    }

    @Test
    fun testKickoffV9MultilingualDeduplicationCrestFallbackAndQuietDefaultAlerts() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Multilingual Club Deduplication
        assertTrue(
            com.example.data.repository.FootballRepository.areSameClubNames(
                "1. FC Köln",
                "FC Cologne"
            )
        )
        assertTrue(
            com.example.data.repository.FootballRepository.areSameClubNames(
                "FC Bayern München",
                "Bayern Munich"
            )
        )

        val db = Room.inMemoryDatabaseBuilder(context, StreamVaultDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = com.example.data.repository.FootballRepository(
                context = context,
                favoriteTeamDao = db.favoriteTeamDao(),
                tournamentPreferenceDao = db.tournamentPreferenceDao(),
                streamDao = db.streamDao()
            )
            val matchA = com.example.data.repository.FootballMatch(
                matchId = "v9_bundesliga_a",
                tournamentId = "bundesliga",
                tournamentName = "Bundesliga",
                tournamentRegion = "Germany",
                dayTab = com.example.data.repository.MatchDayTab.TODAY,
                kickoffTimeFormatted = "18:30",
                kickoffDateFormatted = "Today",
                status = com.example.data.repository.MatchStatus.UPCOMING,
                liveMinuteOrBadge = "18:30",
                homeTeamId = "bayern",
                homeTeamName = "Bayern Munich",
                homeTeamBadge = "https://a.espncdn.com/i/teamlogos/soccer/500/132.png",
                homeScore = null,
                awayTeamId = "koln_1",
                awayTeamName = "1. FC Köln",
                awayTeamBadge = "https://a.espncdn.com/i/teamlogos/soccer/500/122.png",
                awayScore = null,
                stadiumName = "Allianz Arena",
                cityName = "Munich",
                beinChannel = "beIN Sports 5 HD",
                beinChannelNumberHint = "beIN 5"
            )
            val matchB = com.example.data.repository.FootballMatch(
                matchId = "v9_bundesliga_b",
                tournamentId = "bundesliga",
                tournamentName = "Bundesliga",
                tournamentRegion = "Germany",
                dayTab = com.example.data.repository.MatchDayTab.TODAY,
                kickoffTimeFormatted = "18:30",
                kickoffDateFormatted = "Today",
                status = com.example.data.repository.MatchStatus.UPCOMING,
                liveMinuteOrBadge = "18:30",
                homeTeamId = "bayern_2",
                homeTeamName = "Bayern Munich",
                homeTeamBadge = "",
                homeScore = null,
                awayTeamId = "cologne_2",
                awayTeamName = "FC Cologne",
                awayTeamBadge = "",
                awayScore = null,
                stadiumName = "Allianz Arena",
                cityName = "Munich",
                beinChannel = "beIN Sports 5 HD",
                beinChannelNumberHint = "beIN 5"
            )
            val deduped = repo.deduplicateMatchesByCanonicalTeams(listOf(matchA, matchB))
            assertEquals(1, deduped.size)
            assertEquals(
                "https://a.espncdn.com/i/teamlogos/soccer/500/132.png",
                deduped[0].homeTeamBadge
            )
            assertEquals(
                "https://a.espncdn.com/i/teamlogos/soccer/500/122.png",
                deduped[0].awayTeamBadge
            )
        } finally {
            db.close()
        }

        // 2. Second-Tier Sandbox Fixture Rejection
        val premierLeagueCatalogItem = com.example.data.repository.FootballRepository.ALL_TOURNAMENTS
            .first { it.tournamentId == "premier_league" }
        val ligue1CatalogItem = com.example.data.repository.FootballRepository.ALL_TOURNAMENTS
            .first { it.tournamentId == "ligue_1" }

        assertFalse(
            com.example.data.repository.FootballRepository.isValidFixtureForTournament(
                premierLeagueCatalogItem,
                "Hull City",
                "Brighton",
                "England",
                "English Premier League"
            )
        )
        assertFalse(
            com.example.data.repository.FootballRepository.isValidFixtureForTournament(
                ligue1CatalogItem,
                "Troyes",
                "Le Havre",
                "France",
                "French Ligue 1"
            )
        )

        // 3. SVG / Missing Crest Fallback Resolution
        val resolvedTottenhamUrl = com.example.data.repository.FootballRepository.resolveFallbackClubCrestPngUrl(
            "Tottenham Hotspur",
            "https://crests.football-data.org/73.svg"
        )
        assertTrue(resolvedTottenhamUrl.endsWith(".png"))

        // 4. Built-In Suspended API-Football Key Grace
        val sourceMonitor = com.example.data.repository.KickoffSourceMonitor(
            context = context,
            httpClient = IptvRepository.sharedOkHttpClient
        )
        sourceMonitor.resetToDefaultKey(com.example.data.repository.KickoffProviderId.API_FOOTBALL)
        assertFalse(sourceMonitor.isUsingCustomKey(com.example.data.repository.KickoffProviderId.API_FOOTBALL))
        sourceMonitor.recordRuntimeResponse(
            com.example.data.repository.KickoffProviderId.API_FOOTBALL,
            200,
            99,
            "Your account is suspended, check on https://dashboard.api-football.com."
        )
        assertNull(sourceMonitor.activeSourceAlertFlow.value)
    }

    @Test
    fun testKickoffV10DataHygieneCleanDisplayNamesAndSourceReadiness() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()

        // 1. LaLiga second-tier club filtering (Deportivo A Coruña must be rejected from LaLiga EA Sports)
        val laligaCatalogItem = com.example.data.repository.FootballRepository.ALL_TOURNAMENTS
            .first { it.tournamentId == "laliga" }
        assertFalse(
            com.example.data.repository.FootballRepository.isValidFixtureForTournament(
                laligaCatalogItem,
                "Alavés",
                "Deportivo A Coruña",
                "Spain",
                "LaLiga EA Sports"
            )
        )

        // 2. Clean consumer-friendly Saudi Pro League display name canonicalization
        assertEquals(
            "Al Nassr",
            com.example.data.repository.FootballRepository.canonicalizeDisplayTeamName("Al Nassr FC Riyadh")
        )
        assertEquals(
            "Al Ahli",
            com.example.data.repository.FootballRepository.canonicalizeDisplayTeamName("Al Ahli Jeddah")
        )

        // 3. Transient Football-Data.org HTTP 429 (QUOTA_WARNING) must NOT emit a top-level alert banner
        // while keyless providers (ESPN, TheSportsDB, Sofascore) remain HEALTHY
        val sourceMonitor = com.example.data.repository.KickoffSourceMonitor(
            context = context,
            httpClient = IptvRepository.sharedOkHttpClient
        )
        sourceMonitor.recordRuntimeResponse(com.example.data.repository.KickoffProviderId.ESPN_SOCCER, 200)
        sourceMonitor.recordRuntimeResponse(com.example.data.repository.KickoffProviderId.THESPORTSDB, 200)
        sourceMonitor.recordRuntimeResponse(com.example.data.repository.KickoffProviderId.SOFASCORE_LIVE, 200)
        sourceMonitor.recordRuntimeResponse(com.example.data.repository.KickoffProviderId.FOOTBALL_DATA_ORG, 429)

        val fdState = sourceMonitor.sourceHealthFlow.value.first {
            it.provider == com.example.data.repository.KickoffProviderId.FOOTBALL_DATA_ORG
        }
        assertEquals(com.example.data.repository.SourceHealthStatus.QUOTA_WARNING, fdState.status)
        assertNull(sourceMonitor.activeSourceAlertFlow.value)
        assertEquals(
            0,
            com.example.data.repository.KickoffSourceMonitor.countActionableAlerts(
                sourceMonitor.sourceHealthFlow.value,
                sourceMonitor.activeSourceAlertFlow.value
            )
        )
    }

    @Test
    fun testDualPlatformPrismNavigationPrimaryScreensAndTestTags() {
        // 1. Confirm all 5 primary media AppScreen destinations exist
        val primaryScreens = listOf(
            com.example.ui.viewmodel.AppScreen.HOME,
            com.example.ui.viewmodel.AppScreen.LIVE_TV,
            com.example.ui.viewmodel.AppScreen.MOVIES,
            com.example.ui.viewmodel.AppScreen.SERIES,
            com.example.ui.viewmodel.AppScreen.FAVORITES
        )
        assertEquals(5, primaryScreens.distinct().size)
        primaryScreens.forEach { screen ->
            assertTrue(com.example.ui.viewmodel.AppScreen.entries.contains(screen))
        }

        // 2. Verify Mobile Liquid Prism Beam Floating Glass Dock & TV Collapsible Prism Glass Rail tags
        var selectedMobileScreen = com.example.ui.viewmodel.AppScreen.HOME
        var selectedTvScreen = com.example.ui.viewmodel.AppScreen.MOVIES
        val syncState = com.example.data.sync.TvSyncSessionState(
            pairingPin = "4829",
            localIpAddress = "192.168.1.44",
            serverPort = 8765
        )
        composeTestRule.setContent {
            androidx.compose.foundation.layout.Column {
                BottomStreamingNav(
                    currentScreen = selectedMobileScreen,
                    isStreamPlaying = false,
                    liveMatchesCount = 2,
                    favoritesCount = 4,
                    onScreenSelected = { selectedMobileScreen = it }
                )
                TvLeftNavigationRail(
                    currentScreen = selectedTvScreen,
                    liveMatchesCount = 2,
                    favoritesCount = 4,
                    tvSyncState = syncState,
                    onScreenSelected = { selectedTvScreen = it },
                    onOpenTvSyncQr = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("bottom_nav")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("tv_left_navigation_rail")).assertExists()
        assertEquals(2, composeTestRule.onAllNodes(androidx.compose.ui.test.hasTestTag("nav_home")).fetchSemanticsNodes().size)
        assertEquals(2, composeTestRule.onAllNodes(androidx.compose.ui.test.hasTestTag("nav_live_tv")).fetchSemanticsNodes().size)
        assertEquals(2, composeTestRule.onAllNodes(androidx.compose.ui.test.hasTestTag("nav_movies")).fetchSemanticsNodes().size)
        assertEquals(2, composeTestRule.onAllNodes(androidx.compose.ui.test.hasTestTag("nav_series")).fetchSemanticsNodes().size)
        assertEquals(2, composeTestRule.onAllNodes(androidx.compose.ui.test.hasTestTag("nav_favorites")).fetchSemanticsNodes().size)
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("top_search_btn")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("top_tv_sync_btn")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("top_playlists_btn")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("tv_rail_parental_lock_btn")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("tv_rail_sync_status_pod")).assertExists()
    }

    @Test
    fun testVodRedesignDeduplicationRatingNormalizationAndMigration() = runBlocking {
        // 1. TMDb Cross-Language Deduplication
        val enKey = com.example.data.model.VodGroupingNormalizer.computeCanonicalGroupKey(
            streamType = "movie",
            rawName = "The Beekeeper",
            tmdbId = "866398",
            releaseYear = 2024,
            streamId = 1L
        )
        val esKey = com.example.data.model.VodGroupingNormalizer.computeCanonicalGroupKey(
            streamType = "movie",
            rawName = "Beekeeper: El protector",
            tmdbId = "866398",
            releaseYear = 2024,
            streamId = 2L
        )
        assertEquals("tmdb_movie_866398", enKey)
        assertEquals(enKey, esKey)

        // 2. Title + Year Fallback & Zero False Merge on Remakes/Sequels
        val arSeriesKey = com.example.data.model.VodGroupingNormalizer.computeCanonicalGroupKey(
            streamType = "series",
            rawName = "AR | Mr. Robot",
            tmdbId = "",
            releaseYear = 2015,
            streamId = 10L
        )
        val enSeriesKey = com.example.data.model.VodGroupingNormalizer.computeCanonicalGroupKey(
            streamType = "series",
            rawName = "EN | Mr. Robot",
            tmdbId = "",
            releaseYear = 2015,
            streamId = 11L
        )
        assertEquals(arSeriesKey, enSeriesKey)

        val dune1984Key = com.example.data.model.VodGroupingNormalizer.computeCanonicalGroupKey(
            streamType = "movie",
            rawName = "Dune (1984)",
            tmdbId = "",
            releaseYear = 1984,
            streamId = 20L
        )
        val dune2021Key = com.example.data.model.VodGroupingNormalizer.computeCanonicalGroupKey(
            streamType = "movie",
            rawName = "Dune (2021)",
            tmdbId = "",
            releaseYear = 2021,
            streamId = 21L
        )
        assertNotEquals(dune1984Key, dune2021Key)

        // 3. Rating Normalization (Fixing 3.6 vs 7.2 Bug)
        assertEquals(
            "7.2" to 7.2,
            com.example.data.model.VodGroupingNormalizer.normalizeRatingToTenScale("7.2", "3.6")
        )
        assertEquals(
            "7.2" to 7.2,
            com.example.data.model.VodGroupingNormalizer.normalizeRatingToTenScale("", "3.6")
        )

        // 4. Version Tag Extraction
        val arTag = com.example.data.model.VodGroupingNormalizer.extractVersionTag("AR | Mr. Robot")
        assertTrue("Expected AR tag in '$arTag'", arTag.contains("AR"))
        val en4kTag = com.example.data.model.VodGroupingNormalizer.extractVersionTag("[EN] Breaking Bad 4K")
        assertTrue("Expected EN tag in '$en4kTag'", en4kTag.contains("EN"))
        assertTrue("Expected 4K tag in '$en4kTag'", en4kTag.contains("4K"))

        // 5. Room In-Memory SQL Deduplication & Sibling Lookup
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            com.example.data.local.StreamVaultDatabase::class.java
        ).allowMainThreadQueries().build()

        try {
            val playlistId = 42L
            val versionsToInsert = listOf(
                StreamEntity(
                    playlistId = playlistId,
                    streamId = 9001L,
                    name = "EN | The Beekeeper (2024) 4K",
                    streamType = "movie",
                    streamIcon = "https://image.tmdb.org/t/p/w500/beekeeper.jpg",
                    categoryId = "cat_4k",
                    containerExtension = "mkv",
                    rating = "7.2",
                    tmdbId = "866398",
                    canonicalGroupKey = "tmdb_movie_866398",
                    versionTag = "EN • 4K",
                    addedEpoch = 1710000300L,
                    ratingNumeric = 7.2,
                    genres = "Action, Thriller",
                    releaseYear = 2024
                ),
                StreamEntity(
                    playlistId = playlistId,
                    streamId = 9002L,
                    name = "ES | Beekeeper: El protector (2024)",
                    streamType = "movie",
                    streamIcon = "https://image.tmdb.org/t/p/w500/beekeeper_es.jpg",
                    categoryId = "cat_es",
                    containerExtension = "mp4",
                    rating = "7.2",
                    tmdbId = "866398",
                    canonicalGroupKey = "tmdb_movie_866398",
                    versionTag = "ES",
                    addedEpoch = 1710000200L,
                    ratingNumeric = 7.2,
                    genres = "Action, Thriller",
                    releaseYear = 2024
                ),
                StreamEntity(
                    playlistId = playlistId,
                    streamId = 9003L,
                    name = "AR | The Beekeeper (2024)",
                    streamType = "movie",
                    streamIcon = "https://image.tmdb.org/t/p/w500/beekeeper_ar.jpg",
                    categoryId = "cat_ar",
                    containerExtension = "mp4",
                    rating = "7.2",
                    tmdbId = "866398",
                    canonicalGroupKey = "tmdb_movie_866398",
                    versionTag = "AR",
                    addedEpoch = 1710000100L,
                    ratingNumeric = 7.2,
                    genres = "Action, Thriller",
                    releaseYear = 2024
                )
            )
            db.streamDao().insertStreamsBatch(versionsToInsert)

            val grouped = db.streamDao().getDeduplicatedStreams(
                playlistId = playlistId,
                streamType = "movie",
                limit = 50
            ).first()
            assertEquals(1, grouped.size)
            assertEquals(3, grouped.first().versionCount)
            assertEquals("tmdb_movie_866398", grouped.first().stream.canonicalGroupKey)

            val siblings = db.streamDao().getVersionsByGroupKey(
                playlistId = playlistId,
                streamType = "movie",
                canonicalGroupKey = "tmdb_movie_866398"
            )
            assertEquals(3, siblings.size)
            assertEquals(setOf(9001L, 9002L, 9003L), siblings.map { it.streamId }.toSet())
        } finally {
            db.close()
        }
    }

    @Test
    fun testVodV13EnterpriseIndexingRankingSubtitleGroupingAndSeriesParity() = runBlocking {
        // 1. 1-Vote 10.0 Guard vs Multi-Version Blockbuster
        val blockbusterScore = com.example.data.model.VodGroupingNormalizer.computePopularityQualityScore(
            ratingNumeric = 7.2,
            versionCount = 13,
            releaseYear = 2024,
            hasPoster = true,
            isPreferredLang = true
        )
        val obscureOneVoteScore = com.example.data.model.VodGroupingNormalizer.computePopularityQualityScore(
            ratingNumeric = 10.0,
            versionCount = 1,
            releaseYear = 2026,
            hasPoster = true,
            isPreferredLang = true
        )
        assertTrue(
            "Expected multi-version blockbuster ($blockbusterScore) to outrank 1-vote 10.0 entry ($obscureOneVoteScore)",
            blockbusterScore > obscureOneVoteScore
        )

        // 2. Cross-Language Poster-Hash Deduplication
        val enKey = com.example.data.model.VodGroupingNormalizer.computeCanonicalGroupKey(
            streamType = "movie",
            rawName = "Collision Course - 2026",
            tmdbId = "",
            releaseYear = 2026,
            streamId = 1L,
            posterUrl = "https://image.tmdb.org/t/p/w500/abc123def456xyz.jpg"
        )
        val deKey = com.example.data.model.VodGroupingNormalizer.computeCanonicalGroupKey(
            streamType = "movie",
            rawName = "Auf Kollisionskurs",
            tmdbId = "",
            releaseYear = 2026,
            streamId = 2L,
            posterUrl = "https://image.tmdb.org/t/p/w500/abc123def456xyz.jpg"
        )
        assertEquals(enKey, deKey)
        assertTrue("Expected poster-hash key, got $enKey", enKey.startsWith("poster_movie_"))

        // 3. Category-Aware Subtitle Language Extraction & Clean Title Guards
        val iranianTag = com.example.data.model.VodGroupingNormalizer.extractVersionTagWithCategory(
            rawName = "Ballistic",
            categoryName = "[IR] IRANIAN MOVIES"
        )
        assertTrue("Expected Persian tag in '$iranianTag'", iranianTag.contains("Persian"))

        assertEquals(
            "Karaganda: Red Mafia",
            com.example.data.model.VodGroupingNormalizer.extractCleanDisplayTitle("*Karaganda*: Red Mafia - 2026")
        )
        assertEquals(
            "2026",
            com.example.data.model.VodGroupingNormalizer.extractCleanDisplayTitle("2026")
        )

        // 4. Materialized vod_catalog_groups Read Parity for BOTH Movies & Series
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(
            context,
            StreamVaultDatabase::class.java
        ).allowMainThreadQueries().build()

        try {
            val playlistId = 77L
            val movieGroups = listOf(
                com.example.data.model.VodCatalogGroupEntity(
                    playlistId = playlistId,
                    streamType = "movie",
                    groupKey = "tmdb_movie_1001",
                    representativeStreamDbId = 101L,
                    representativeStreamId = 1001L,
                    cleanTitle = "Obscure Film",
                    posterUrl = "https://image.tmdb.org/t/p/w500/obscure123456.jpg",
                    rating = "10.0",
                    ratingNumeric = 10.0,
                    releaseYear = 2026,
                    addedEpoch = 1710000100L,
                    genres = "Drama",
                    primaryCategoryId = "cat_m1",
                    categoryIdsCsv = ",cat_m1,",
                    versionCount = 1,
                    defaultVersionTag = "English",
                    containerExtension = "mp4",
                    popularityScore = obscureOneVoteScore
                ),
                com.example.data.model.VodCatalogGroupEntity(
                    playlistId = playlistId,
                    streamType = "movie",
                    groupKey = "tmdb_movie_1002",
                    representativeStreamDbId = 102L,
                    representativeStreamId = 1002L,
                    cleanTitle = "Dune: Part Two",
                    posterUrl = "https://image.tmdb.org/t/p/w500/dune2poster12345.jpg",
                    rating = "8.3",
                    ratingNumeric = 8.3,
                    releaseYear = 2024,
                    addedEpoch = 1710000200L,
                    genres = "Sci-Fi, Action",
                    primaryCategoryId = "cat_m2",
                    categoryIdsCsv = ",cat_m2,",
                    versionCount = 13,
                    defaultVersionTag = "English • 4K",
                    containerExtension = "mkv",
                    popularityScore = blockbusterScore
                )
            )
            val seriesGroups = listOf(
                com.example.data.model.VodCatalogGroupEntity(
                    playlistId = playlistId,
                    streamType = "series",
                    groupKey = "tmdb_series_2001",
                    representativeStreamDbId = 201L,
                    representativeStreamId = 2001L,
                    cleanTitle = "Minor Show",
                    posterUrl = "https://image.tmdb.org/t/p/w500/minor12345678.jpg",
                    rating = "6.1",
                    ratingNumeric = 6.1,
                    releaseYear = 2022,
                    addedEpoch = 1710000100L,
                    genres = "Comedy",
                    primaryCategoryId = "cat_s1",
                    categoryIdsCsv = ",cat_s1,",
                    versionCount = 1,
                    defaultVersionTag = "English",
                    containerExtension = "mp4",
                    popularityScore = 7.4
                ),
                com.example.data.model.VodCatalogGroupEntity(
                    playlistId = playlistId,
                    streamType = "series",
                    groupKey = "tmdb_series_2002",
                    representativeStreamDbId = 202L,
                    representativeStreamId = 2002L,
                    cleanTitle = "Breaking Bad",
                    posterUrl = "https://image.tmdb.org/t/p/w500/bbposter123456.jpg",
                    rating = "9.5",
                    ratingNumeric = 9.5,
                    releaseYear = 2008,
                    addedEpoch = 1710000300L,
                    genres = "Crime, Drama",
                    primaryCategoryId = "cat_s2",
                    categoryIdsCsv = ",cat_s2,",
                    versionCount = 9,
                    defaultVersionTag = "English • 4K",
                    containerExtension = "mp4",
                    popularityScore = 15.8
                )
            )
            db.vodGroupDao().insertGroupsBatch(movieGroups + seriesGroups)

            val topMovies = db.vodGroupDao().getTopPopularGroups(playlistId, "movie", limit = 10).first()
            assertEquals(2, topMovies.size)
            assertEquals("Dune: Part Two", topMovies.first().cleanTitle)
            assertEquals(13, topMovies.first().versionCount)

            val topSeries = db.vodGroupDao().getTopPopularGroups(playlistId, "series", limit = 10).first()
            assertEquals(2, topSeries.size)
            assertEquals("Breaking Bad", topSeries.first().cleanTitle)
            assertEquals(9, topSeries.first().versionCount)
        } finally {
            db.close()
        }
    }

    @Test
    fun v14_5_stage2RebuildReadsStreamsWithoutRewriteAndEmitsDeterminateReadyState() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(
            context,
            StreamVaultDatabase::class.java
        ).allowMainThreadQueries().build()

        try {
            val repo = com.example.data.repository.IptvRepository(
                playlistDao = db.playlistDao(),
                categoryDao = db.categoryDao(),
                streamDao = db.streamDao(),
                epgDao = db.epgDao(),
                parentalDao = db.parentalSettingsDao(),
                vodGroupDao = db.vodGroupDao(),
                database = db,
                appContext = context
            )

            val playlistId = db.playlistDao().insertPlaylist(
                PlaylistEntity(
                    name = "V14.5 Stage2 Test",
                    serverUrl = "http://stage2.example.com",
                    username = "u",
                    password = "p",
                    isActive = true
                )
            )
            db.categoryDao().insertCategories(
                listOf(
                    CategoryEntity(
                        playlistId = playlistId,
                        categoryId = "cat_en",
                        categoryName = "[EN] 4K MOVIES",
                        type = "MOVIE"
                    ),
                    CategoryEntity(
                        playlistId = playlistId,
                        categoryId = "cat_fr",
                        categoryName = "[FR] FILMS",
                        type = "MOVIE"
                    )
                )
            )

            // Legacy pre-V14.5 rows with intentionally blank canonicalGroupKey & versionTag
            val legacyRows = listOf(
                StreamEntity(
                    playlistId = playlistId,
                    streamId = 5001L,
                    name = "[EN] Dune: Part Two (2024) 4K",
                    streamType = "movie",
                    streamIcon = "https://image.tmdb.org/t/p/w500/dune2poster12345.jpg",
                    categoryId = "cat_en",
                    rating = "8.4",
                    ratingNumeric = 8.4,
                    releaseYear = 2024,
                    addedEpoch = 1710000100L,
                    genres = "Sci-Fi, Action",
                    tmdbId = "693134",
                    canonicalGroupKey = "",
                    versionTag = ""
                ),
                StreamEntity(
                    playlistId = playlistId,
                    streamId = 5002L,
                    name = "[FR] Dune: Deuxieme Partie (2024)",
                    streamType = "movie",
                    streamIcon = "https://image.tmdb.org/t/p/w500/dune2poster12345.jpg",
                    categoryId = "cat_fr",
                    rating = "8.4",
                    ratingNumeric = 8.4,
                    releaseYear = 2024,
                    addedEpoch = 1710000050L,
                    genres = "Sci-Fi, Action",
                    tmdbId = "693134",
                    canonicalGroupKey = "",
                    versionTag = ""
                ),
                StreamEntity(
                    playlistId = playlistId,
                    streamId = 5003L,
                    name = "Interstellar (2014)",
                    streamType = "movie",
                    streamIcon = "https://image.tmdb.org/t/p/w500/interstellar123.jpg",
                    categoryId = "cat_en",
                    rating = "8.7",
                    ratingNumeric = 8.7,
                    releaseYear = 2014,
                    addedEpoch = 1710000000L,
                    genres = "Sci-Fi, Drama",
                    tmdbId = "157336",
                    canonicalGroupKey = "",
                    versionTag = ""
                )
            )
            db.streamDao().insertStreamsBatch(legacyRows)

            repo.rebuildVodCatalogGroups(playlistId, "movie")

            // 1. Materialized vod_catalog_groups populated and deduplicated (3 rows -> 2 groups)
            val groups = db.vodGroupDao().getTopPopularGroups(playlistId, "movie", limit = 20).first()
            assertEquals(2, groups.size)
            val duneGroup = groups.first { it.groupKey.contains("693134") }
            assertEquals(2, duneGroup.versionCount)
            assertEquals("Dune: Part Two", duneGroup.cleanTitle)

            // 2. Zero write-backs to streams table (canonicalGroupKey and versionTag remain untouched)
            val rawStreamsAfter = db.streamDao().getStreamsInitial(playlistId, "movie", limit = 20).first()
            assertEquals(3, rawStreamsAfter.size)
            for (row in rawStreamsAfter) {
                assertEquals(
                    "Expected Stage 2 read-only projection to NOT rewrite streams.canonicalGroupKey",
                    "",
                    row.canonicalGroupKey
                )
                assertEquals(
                    "Expected Stage 2 read-only projection to NOT rewrite streams.versionTag",
                    "",
                    row.versionTag
                )
            }

            // 3. Determinate progress ended in READY with 100%
            val finalState = repo.moviesPipelineState.value
            assertEquals(com.example.data.repository.VodPipelinePhase.READY, finalState.phase)
            assertEquals(100, finalState.progressPercent)
            assertEquals(3, finalState.processedCount)
            assertEquals(3, finalState.totalCount)
        } finally {
            db.close()
        }
    }

    @Test
    fun v14_5_moviesAndSeriesPipelinesAreCompletelyIndependent() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(
            context,
            StreamVaultDatabase::class.java
        ).allowMainThreadQueries().build()

        try {
            val repo = com.example.data.repository.IptvRepository(
                playlistDao = db.playlistDao(),
                categoryDao = db.categoryDao(),
                streamDao = db.streamDao(),
                epgDao = db.epgDao(),
                parentalDao = db.parentalSettingsDao(),
                vodGroupDao = db.vodGroupDao(),
                database = db,
                appContext = context
            )

            val playlistId = db.playlistDao().insertPlaylist(
                PlaylistEntity(
                    name = "V14.5 Pipeline Isolation Test",
                    serverUrl = "http://isolation.example.com",
                    username = "u",
                    password = "p",
                    isActive = true
                )
            )

            db.streamDao().insertStreamsBatch(
                listOf(
                    StreamEntity(
                        playlistId = playlistId,
                        streamId = 6001L,
                        name = "Inception (2010)",
                        streamType = "movie",
                        streamIcon = "https://image.tmdb.org/t/p/w500/inception.jpg",
                        categoryId = "m1",
                        rating = "8.8",
                        ratingNumeric = 8.8,
                        releaseYear = 2010,
                        addedEpoch = 1710000100L
                    ),
                    StreamEntity(
                        playlistId = playlistId,
                        streamId = 7001L,
                        name = "Severance (2022)",
                        streamType = "series",
                        streamIcon = "https://image.tmdb.org/t/p/w500/severance.jpg",
                        categoryId = "s1",
                        rating = "8.7",
                        ratingNumeric = 8.7,
                        releaseYear = 2022,
                        addedEpoch = 1710000200L
                    ),
                    StreamEntity(
                        playlistId = playlistId,
                        streamId = 7002L,
                        name = "Dark (2017)",
                        streamType = "series",
                        streamIcon = "https://image.tmdb.org/t/p/w500/dark.jpg",
                        categoryId = "s1",
                        rating = "8.8",
                        ratingNumeric = 8.8,
                        releaseYear = 2017,
                        addedEpoch = 1710000150L
                    )
                )
            )

            assertEquals(com.example.data.repository.VodPipelinePhase.IDLE, repo.moviesPipelineState.value.phase)
            assertEquals(com.example.data.repository.VodPipelinePhase.IDLE, repo.seriesPipelineState.value.phase)

            // Rebuild Series only -> Movies pipeline MUST stay IDLE
            repo.rebuildVodCatalogGroups(playlistId, "series")
            val seriesAfterSeriesRebuild = repo.seriesPipelineState.value
            assertEquals(com.example.data.repository.VodPipelinePhase.READY, seriesAfterSeriesRebuild.phase)
            assertEquals(100, seriesAfterSeriesRebuild.progressPercent)
            assertEquals(2, seriesAfterSeriesRebuild.totalCount)
            assertEquals(
                "Series finishing READY must not mutate moviesPipelineState",
                com.example.data.repository.VodPipelinePhase.IDLE,
                repo.moviesPipelineState.value.phase
            )

            // Rebuild Movies only -> Series pipeline MUST stay unchanged
            repo.rebuildVodCatalogGroups(playlistId, "movie")
            val moviesAfterMovieRebuild = repo.moviesPipelineState.value
            assertEquals(com.example.data.repository.VodPipelinePhase.READY, moviesAfterMovieRebuild.phase)
            assertEquals(100, moviesAfterMovieRebuild.progressPercent)
            assertEquals(1, moviesAfterMovieRebuild.totalCount)
            assertEquals(
                "Movies finishing READY must not overwrite seriesPipelineState",
                seriesAfterSeriesRebuild,
                repo.seriesPipelineState.value
            )
        } finally {
            db.close()
        }
    }

    @Test
    fun v14_5_migration5To6CreatesFts4AndSearchGroupsFtsMatchesUnder50ms() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Verify MIGRATION_5_6 upgrades a v5 vod_catalog_groups table & rebuilds FTS4 index
        val dbName = "v14_5_mig56_test_${System.nanoTime()}.db"
        val helperConfig = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(5) {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    StreamVaultDatabase.MIGRATION_4_5.migrate(db)
                    db.execSQL(
                        """
                        INSERT INTO vod_catalog_groups (
                            playlistId, streamType, groupKey, representativeStreamDbId, representativeStreamId,
                            cleanTitle, posterUrl, rating, ratingNumeric, releaseYear, addedEpoch, genres,
                            primaryCategoryId, categoryIdsCsv, versionCount, defaultVersionTag,
                            containerExtension, popularityScore
                        ) VALUES
                        (1, 'series', 'tmdb_series_1', 10, 100, 'Dune: Prophecy', 'https://p/1.jpg', '8.2', 8.2, 2024, 1710000200, 'Sci-Fi, Drama', 'c1', ',c1,', 4, 'English', 'mp4', 18.5),
                        (1, 'series', 'tmdb_series_2', 11, 101, 'The Bear', 'https://p/2.jpg', '8.6', 8.6, 2022, 1710000100, 'Comedy, Drama', 'c2', ',c2,', 3, 'English', 'mp4', 17.2)
                        """.trimIndent()
                    )
                }

                override fun onUpgrade(
                    db: androidx.sqlite.db.SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int
                ) = Unit
            })
            .build()
        val helper = androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory().create(helperConfig)
        try {
            val sqliteDb = helper.writableDatabase
            StreamVaultDatabase.MIGRATION_5_6.migrate(sqliteDb)

            sqliteDb.query(
                """
                SELECT g.cleanTitle
                FROM vod_catalog_groups g
                JOIN vod_catalog_groups_fts fts ON g.rowid = fts.rowid
                WHERE vod_catalog_groups_fts MATCH 'dune* proph*'
                """.trimIndent()
            ).use { cursor ->
                assertTrue("Expected MIGRATION_5_6 FTS4 rebuild to match 'Dune: Prophecy'", cursor.moveToFirst())
                assertEquals("Dune: Prophecy", cursor.getString(0))
                assertEquals(1, cursor.count)
            }
        } finally {
            helper.close()
            context.deleteDatabase(dbName)
        }

        // 2. Verify Room v6 searchGroupsFts returns matching titles in < 50ms
        val roomDb = Room.inMemoryDatabaseBuilder(
            context,
            StreamVaultDatabase::class.java
        ).allowMainThreadQueries().build()

        try {
            val playlistId = 88L
            val seedGroups = mutableListOf<com.example.data.model.VodCatalogGroupEntity>()
            seedGroups.add(
                com.example.data.model.VodCatalogGroupEntity(
                    playlistId = playlistId,
                    streamType = "movie",
                    groupKey = "tmdb_movie_9001",
                    representativeStreamDbId = 9001L,
                    representativeStreamId = 9001L,
                    cleanTitle = "Dune: Prophecy Extended",
                    posterUrl = "https://image.tmdb.org/t/p/w500/dune_proph.jpg",
                    rating = "8.5",
                    ratingNumeric = 8.5,
                    releaseYear = 2025,
                    addedEpoch = 1710009001L,
                    genres = "Sci-Fi, Adventure",
                    primaryCategoryId = "cat_scifi",
                    categoryIdsCsv = ",cat_scifi,",
                    versionCount = 6,
                    defaultVersionTag = "English • 4K",
                    containerExtension = "mkv",
                    popularityScore = 24.5
                )
            )
            for (i in 1..60) {
                seedGroups.add(
                    com.example.data.model.VodCatalogGroupEntity(
                        playlistId = playlistId,
                        streamType = "movie",
                        groupKey = "tmdb_movie_seed_$i",
                        representativeStreamDbId = 9100L + i,
                        representativeStreamId = 9100L + i,
                        cleanTitle = "Catalog Feature Film $i",
                        posterUrl = "https://image.tmdb.org/t/p/w500/film_$i.jpg",
                        rating = "7.4",
                        ratingNumeric = 7.4,
                        releaseYear = 2020 + (i % 6),
                        addedEpoch = 1710000000L + i,
                        genres = "Action, Thriller",
                        primaryCategoryId = "cat_action",
                        categoryIdsCsv = ",cat_action,",
                        versionCount = 2,
                        defaultVersionTag = "English",
                        containerExtension = "mp4",
                        popularityScore = 10.0 + (i % 5)
                    )
                )
            }
            roomDb.vodGroupDao().insertGroupsBatch(seedGroups)

            // Warm up Room Flow dispatcher once, then time the FTS4 query
            roomDb.vodGroupDao().searchGroupsFts(playlistId, "movie", null, "catalog*", 10).first()

            val startNs = System.nanoTime()
            val matches = roomDb.vodGroupDao().searchGroupsFts(
                playlistId = playlistId,
                streamType = "movie",
                categoryId = null,
                ftsQuery = "dune* proph*",
                limit = 20
            ).first()
            val elapsedMs = (System.nanoTime() - startNs) / 1_000_000L

            assertEquals(1, matches.size)
            assertEquals("Dune: Prophecy Extended", matches.first().cleanTitle)
            assertTrue("Expected searchGroupsFts to finish in < 50ms, took ${elapsedMs}ms", elapsedMs < 50L)
        } finally {
            roomDb.close()
        }
    }

    @Test
    fun v14_5_globalPopularityIndexBoostsBlockbustersAboveUnverifiedPlaceholderTen() {
        try {
            val sampleIndexJson = """
                {
                  "v": 1,
                  "m": {
                    "693134": 94.5
                  },
                  "s": {
                    "1396": 98.2
                  },
                  "t": {
                    "m:dune part two_2024": 94.5,
                    "s:breaking bad_2008": 98.2
                  }
                }
            """.trimIndent()
            com.example.data.remote.GlobalPopularityIndexManager.loadFromJsonStringForTest(sampleIndexJson)

            val byTmdb = com.example.data.remote.GlobalPopularityIndexManager.lookupGlobalScore(
                streamType = "movie",
                tmdbId = "693134",
                cleanTitle = "Dune: Part Two",
                releaseYear = 2024
            )
            assertEquals(94.5, byTmdb, 0.001)

            val byTitleFallback = com.example.data.remote.GlobalPopularityIndexManager.lookupGlobalScore(
                streamType = "movie",
                tmdbId = "",
                cleanTitle = "Dune: Part Two",
                releaseYear = 2024
            )
            assertEquals(94.5, byTitleFallback, 0.001)

            val blockbusterScore = com.example.data.model.VodGroupingNormalizer.computePopularityQualityScore(
                ratingNumeric = 8.3,
                versionCount = 3,
                releaseYear = 2024,
                hasPoster = true,
                isPreferredLang = true,
                globalTmdbScore = byTmdb
            )
            val unverifiedTenJunkScore = com.example.data.model.VodGroupingNormalizer.computePopularityQualityScore(
                ratingNumeric = 10.0,
                versionCount = 1,
                releaseYear = 2026,
                hasPoster = true,
                isPreferredLang = true,
                globalTmdbScore = 0.0
            )
            assertTrue(
                "Expected Global Popularity Index blockbuster ($blockbusterScore) to strongly outrank 1-version 10.0 junk ($unverifiedTenJunkScore)",
                blockbusterScore > unverifiedTenJunkScore + 5.0
            )

            // Verify unverified 10.0 rating badge suppression + 5+ version TRENDING badge rule
            assertTrue(com.example.data.model.VodGroupingNormalizer.isPlaceholderPerfectRating(10.0, 1))
            val placeholderOneVersion = StreamEntity(
                playlistId = 1L,
                streamId = 1L,
                name = "Obscure Upload",
                streamType = "movie",
                categoryId = "cat_obscure",
                rating = "10.0",
                ratingNumeric = 10.0
            )
            assertEquals(
                null,
                com.example.ui.vod.formatNormalizedRating(placeholderOneVersion, versionCount = 1)
            )
            assertEquals(
                false,
                com.example.ui.vod.isTrendingPlaceholderRating(placeholderOneVersion, versionCount = 1)
            )
            assertEquals(
                true,
                com.example.ui.vod.isTrendingPlaceholderRating(placeholderOneVersion, versionCount = 6)
            )
        } finally {
            com.example.data.remote.GlobalPopularityIndexManager.clearForTest()
        }
    }

    @Test
    fun v14_5_vodViewModelShelfPaginationBreaks20ItemCeiling() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val application = context.applicationContext as Application
        val db = StreamVaultDatabase.getInstance(context)
        val viewModel = com.example.ui.vod.VodViewModel(application)
        var seededPlaylistId = -1L

        try {
            // 1. Horizontal shelf expansion steps: 24 -> 48 -> 72 -> 120 (capped at 120)
            val shelfId = "movie_top_rated"
            viewModel.loadMoreForShelf(shelfId)
            assertEquals(48, viewModel.shelfLimits.value[shelfId])
            viewModel.loadMoreForShelf(shelfId)
            assertEquals(72, viewModel.shelfLimits.value[shelfId])
            viewModel.loadMoreForShelf(shelfId)
            assertEquals(120, viewModel.shelfLimits.value[shelfId])
            viewModel.loadMoreForShelf(shelfId)
            assertEquals(120, viewModel.shelfLimits.value[shelfId])

            // 2. Seed an active playlist + 75 high-rated movies in vod_catalog_groups
            seededPlaylistId = db.playlistDao().insertPlaylist(
                PlaylistEntity(
                    name = "V14.5 Shelf Pagination Test",
                    serverUrl = "http://pagination.example.com",
                    username = "u",
                    password = "p",
                    isActive = true
                )
            )
            db.playlistDao().setActivePlaylist(seededPlaylistId)
            val groups = (1..75).map { idx ->
                com.example.data.model.VodCatalogGroupEntity(
                    playlistId = seededPlaylistId,
                    streamType = "movie",
                    groupKey = "tmdb_movie_page_$idx",
                    representativeStreamDbId = 80000L + idx,
                    representativeStreamId = 80000L + idx,
                    cleanTitle = "Acclaimed Feature $idx",
                    posterUrl = "https://image.tmdb.org/t/p/w500/acclaimed_$idx.jpg",
                    rating = "8.6",
                    ratingNumeric = 8.6,
                    releaseYear = 2024,
                    addedEpoch = 1710000000L + idx,
                    genres = "Drama, Thriller",
                    primaryCategoryId = "cat_top",
                    categoryIdsCsv = ",cat_top,",
                    versionCount = 4,
                    defaultVersionTag = "English • 4K",
                    containerExtension = "mp4",
                    popularityScore = 25.0 - (idx * 0.05)
                )
            }
            db.vodGroupDao().insertGroupsBatch(groups)

            // 3. Open See All grid -> first page emits 60 items (breaks the old 20-item ceiling)
            viewModel.openShelfSeeAll(com.example.ui.vod.VodTab.MOVIES, com.example.ui.vod.VodShelfFilter.TopRated)
            assertEquals(com.example.ui.vod.VodShelfFilter.TopRated, viewModel.selectedMovieShelfFilter.value)

            val collectorScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default)
            val collectorJob = collectorScope.launch {
                viewModel.movieShelfGridItems.collect { /* keep WhileSubscribed active */ }
            }
            try {
                val awaitGridCount: (Int) -> Int = { expectedCount ->
                    val deadlineMs = System.currentTimeMillis() + 5_000L
                    while (System.currentTimeMillis() < deadlineMs && viewModel.movieShelfGridItems.value.size < expectedCount) {
                        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
                        Thread.sleep(20L)
                    }
                    org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
                    viewModel.movieShelfGridItems.value.size
                }

                assertEquals(60, awaitGridCount(60))

                // 4. Load next page (+60) -> emits all 75 seeded items
                viewModel.loadMoreShelfGrid(com.example.ui.vod.VodTab.MOVIES)
                assertEquals(75, awaitGridCount(75))

                // 5. Close See All grid clears filter cleanly
                viewModel.closeShelfSeeAll(com.example.ui.vod.VodTab.MOVIES)
                assertEquals(null, viewModel.selectedMovieShelfFilter.value)
            } finally {
                collectorJob.cancel()
            }
        } finally {
            if (seededPlaylistId > 0L) {
                db.vodGroupDao().deleteGroupsByType(seededPlaylistId, "movie")
                db.playlistDao().deletePlaylist(seededPlaylistId)
            }
        }
    }

    @Test
    fun phase5d_migration6To7PreservesUserDataAndCreatesV7Tables() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "phase5d_mig67_${System.nanoTime()}.db"
        val dbFile = context.getDatabasePath(dbName)
        val factory = androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory()
        val config = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(6) {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `playlists` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `name` TEXT NOT NULL,
                            `serverUrl` TEXT NOT NULL,
                            `username` TEXT NOT NULL,
                            `password` TEXT NOT NULL,
                            `isActive` INTEGER NOT NULL,
                            `lastSyncTimestamp` INTEGER NOT NULL,
                            `channelCount` INTEGER NOT NULL,
                            `vodCount` INTEGER NOT NULL,
                            `seriesCount` INTEGER NOT NULL
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `streams` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `playlistId` INTEGER NOT NULL,
                            `streamId` INTEGER NOT NULL,
                            `categoryId` TEXT NOT NULL,
                            `name` TEXT NOT NULL,
                            `streamType` TEXT NOT NULL,
                            `streamIcon` TEXT NOT NULL,
                            `epgChannelId` TEXT NOT NULL,
                            `rating` TEXT NOT NULL,
                            `tvArchive` INTEGER NOT NULL,
                            `tvArchiveDuration` INTEGER NOT NULL,
                            `containerExtension` TEXT NOT NULL,
                            `isFavorite` INTEGER NOT NULL,
                            `watchHistoryTimestamp` INTEGER NOT NULL,
                            `lastPositionMs` INTEGER NOT NULL,
                            `tmdbId` TEXT NOT NULL DEFAULT '',
                            `canonicalGroupKey` TEXT NOT NULL DEFAULT '',
                            `versionTag` TEXT NOT NULL DEFAULT '',
                            `addedEpoch` INTEGER NOT NULL DEFAULT 0,
                            `ratingNumeric` REAL NOT NULL DEFAULT 0.0,
                            `releaseYear` INTEGER NOT NULL DEFAULT 0,
                            `genres` TEXT NOT NULL DEFAULT ''
                        )
                        """.trimIndent()
                    )
                    StreamVaultDatabase.MIGRATION_4_5.migrate(db)
                    StreamVaultDatabase.MIGRATION_5_6.migrate(db)
                }

                override fun onUpgrade(
                    db: androidx.sqlite.db.SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int
                ) {
                    // Tested explicitly below
                }
            })
            .build()

        val helper = factory.create(config)
        try {
            val db = helper.writableDatabase
            db.execSQL(
                "INSERT INTO playlists (id, name, serverUrl, username, password, isActive, lastSyncTimestamp, channelCount, vodCount, seriesCount) " +
                    "VALUES (7, 'Living Room IPTV', 'http://demo.example.com', 'alice', 'secret', 1, 1700000000000, 10, 20, 5)"
            )
            db.execSQL(
                "INSERT INTO streams (id, playlistId, streamId, categoryId, name, streamType, streamIcon, epgChannelId, rating, tvArchive, tvArchiveDuration, containerExtension, isFavorite, watchHistoryTimestamp, lastPositionMs, tmdbId, canonicalGroupKey, versionTag, addedEpoch, ratingNumeric, releaseYear, genres) " +
                    "VALUES (101, 7, 5555, 'cat_1', 'The Beekeeper (2024)', 'movie', 'https://image.tmdb.org/t/p/w500/bee.jpg', '', '7.8', 0, 0, 'mp4', 1, 1710000000000, 245000, '866398', 'tmdb_movie_866398', 'English • 4K', 1710000000, 7.8, 2024, 'Action')"
            )
            db.execSQL(
                "INSERT INTO vod_catalog_groups (id, playlistId, streamType, groupKey, representativeStreamDbId, representativeStreamId, cleanTitle, posterUrl, rating, ratingNumeric, releaseYear, addedEpoch, genres, primaryCategoryId, categoryIdsCsv, versionCount, defaultVersionTag, containerExtension, popularityScore) " +
                    "VALUES (1, 7, 'movie', 'tmdb_movie_866398', 101, 5555, 'The Beekeeper', 'https://image.tmdb.org/t/p/w500/bee.jpg', '7.8', 7.8, 2024, 1710000000, 'Action', 'cat_1', ',cat_1,', 1, 'English • 4K', 'mp4', 82.0)"
            )

            // Execute non-destructive MIGRATION_6_7
            StreamVaultDatabase.MIGRATION_6_7.migrate(db)

            // 1. Verify playlist row preserved
            db.query("SELECT name, username FROM playlists WHERE id = 7").use { c ->
                assertTrue(c.moveToFirst())
                assertEquals("Living Room IPTV", c.getString(0))
                assertEquals("alice", c.getString(1))
            }

            // 2. Verify stream user state preserved + metadataHash column added with default 0
            db.query("SELECT isFavorite, lastPositionMs, metadataHash FROM streams WHERE id = 101").use { c ->
                assertTrue(c.moveToFirst())
                assertEquals(1, c.getInt(0))
                assertEquals(245000L, c.getLong(1))
                assertEquals(0L, c.getLong(2))
            }

            // 3. Verify vod_catalog_groups.platformTags column added with default ''
            db.query("SELECT platformTags FROM vod_catalog_groups WHERE id = 1").use { c ->
                assertTrue(c.moveToFirst())
                assertEquals("", c.getString(0))
            }

            // 4. Verify vod_group_members table exists and accepts inserts/queries
            db.execSQL(
                "INSERT INTO vod_group_members (playlistId, streamType, groupKey, streamDbId) VALUES (7, 'movie', 'tmdb_movie_866398', 101)"
            )
            db.query("SELECT streamDbId FROM vod_group_members WHERE playlistId = 7 AND groupKey = 'tmdb_movie_866398'").use { c ->
                assertTrue(c.moveToFirst())
                assertEquals(101L, c.getLong(0))
            }

            // 5. Verify sync_section_state table exists and accepts inserts/queries
            db.execSQL(
                "INSERT INTO sync_section_state (playlistId, sectionType, status, stage, sessionId, isIndeterminate, progressPercent, totalDiscovered, itemsDownloaded, itemsProcessed, itemsUnchanged, itemsAdded, itemsUpdated, itemsRemoved, itemsSkipped, itemsGrouped, itemsPersisted, lastSuccessfulSyncMs, lastAttemptedSyncMs, pendingUpdateAvailable, pendingUpdateSummary, etag, lastModified, lastContentLength, errorMessage) " +
                    "VALUES (7, 'MOVIE', 'COMPLETED', 'FINALIZING', 'sess-1', 0, 100, 20, 20, 20, 18, 2, 0, 0, 0, 15, 2, 1710000001000, 1710000000000, 0, '', '\"e1\"', '', 4096, '')"
            )
            db.query("SELECT status, progressPercent, itemsUnchanged FROM sync_section_state WHERE playlistId = 7 AND sectionType = 'MOVIE'").use { c ->
                assertTrue(c.moveToFirst())
                assertEquals("COMPLETED", c.getString(0))
                assertEquals(100, c.getInt(1))
                assertEquals(18, c.getInt(2))
            }
        } finally {
            helper.close()
            dbFile.delete()
            java.io.File(dbFile.path + "-wal").delete()
            java.io.File(dbFile.path + "-shm").delete()
        }
    }

    @Test
    fun phase5d_globalPopularityIndexAllowsConfiguredUrlFoldsDiacriticsAndMatchesYearTolerance() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val cacheFile = java.io.File(context.filesDir, "global_popularity_index.json")
        val metaFile = java.io.File(context.filesDir, "global_popularity_index.meta")
        cacheFile.delete()
        metaFile.delete()
        com.example.data.remote.GlobalPopularityIndexManager.clearForTest()

        try {
            val defaultUrl = com.example.data.remote.GlobalPopularityIndexManager.DEFAULT_INDEX_URL
            assertTrue("DEFAULT_INDEX_URL must point to configured GitHub Pages host", defaultUrl.contains("MPadberg-svg"))
            assertFalse("DEFAULT_INDEX_URL must not contain placeholder", defaultUrl.contains("YOUR_GITHUB_USERNAME"))

            val requestHitCount = java.util.concurrent.atomic.AtomicInteger(0)
            val payload = """
                {
                  "v": 1,
                  "generatedAt": 1730000000,
                  "m": { "866398": 91.0 },
                  "s": {},
                  "t": {
                    "m:amelie_2001": 92.4,
                    "s:elite_2018": 88.6,
                    "m:the beekeeper_2024": 91.0,
                    "m:the beekeeper": 91.0
                  }
                }
            """.trimIndent()

            val mockClient = okhttp3.OkHttpClient.Builder()
                .addInterceptor { chain ->
                    requestHitCount.incrementAndGet()
                    okhttp3.Response.Builder()
                        .request(chain.request())
                        .protocol(okhttp3.Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .header("ETag", "\"v15-test-etag\"")
                        .body(
                            okhttp3.ResponseBody.Companion.run {
                                payload.toResponseBody(
                                    okhttp3.MediaType.Companion.run { "application/json".toMediaType() }
                                )
                            }
                        )
                        .build()
                }
                .build()

            val downloaded = com.example.data.remote.GlobalPopularityIndexManager.ensureLoaded(
                context = context,
                okHttpClient = mockClient,
                url = defaultUrl
            )
            assertTrue("Configured DEFAULT_INDEX_URL must NOT be skipped", downloaded)
            assertEquals(1, requestHitCount.get())

            // Diacritic folding lookups
            assertEquals(
                92.4,
                com.example.data.remote.GlobalPopularityIndexManager.lookupGlobalScore("movie", "", "Amélie", 2001),
                0.01
            )
            assertEquals(
                88.6,
                com.example.data.remote.GlobalPopularityIndexManager.lookupGlobalScore("series", "", "Élite", 2018),
                0.01
            )

            // ±1 year tolerance & year-0 lookup
            assertEquals(
                91.0,
                com.example.data.remote.GlobalPopularityIndexManager.lookupGlobalScore("movie", "", "The Beekeeper", 2023),
                0.01
            )
            assertEquals(
                91.0,
                com.example.data.remote.GlobalPopularityIndexManager.lookupGlobalScore("movie", "", "The Beekeeper", 2025),
                0.01
            )
            assertEquals(
                91.0,
                com.example.data.remote.GlobalPopularityIndexManager.lookupGlobalScore("movie", "", "The Beekeeper", 0),
                0.01
            )
        } finally {
            com.example.data.remote.GlobalPopularityIndexManager.clearForTest()
            cacheFile.delete()
            metaFile.delete()
        }
    }

    @Test
    fun phase5d_unionFindBridgesFragmentedBeekeeperCopiesLosslessly() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, StreamVaultDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        try {
            val playlistId = db.playlistDao().insertPlaylist(
                PlaylistEntity(
                    name = "Union-Find Test",
                    serverUrl = "http://unionfind.example.com",
                    username = "u",
                    password = "p",
                    isActive = true
                )
            )
            val sharedPoster = "https://image.tmdb.org/t/p/w500/A7EByudX0eOzlkQ2FIbogzyazm2.jpg"
            val fragmentedStreams = listOf(
                StreamEntity(
                    playlistId = playlistId,
                    streamId = 9001L,
                    categoryId = "cat_en",
                    name = "[EN] The Beekeeper (2024) 4K",
                    streamType = "movie",
                    streamIcon = sharedPoster,
                    rating = "7.6",
                    ratingNumeric = 7.6,
                    tmdbId = "866398",
                    releaseYear = 2024,
                    containerExtension = "mkv",
                    addedEpoch = 1710000100L
                ),
                StreamEntity(
                    playlistId = playlistId,
                    streamId = 9002L,
                    categoryId = "cat_fr",
                    name = "[FR] The Beekeeper (2024)",
                    streamType = "movie",
                    streamIcon = "",
                    rating = "7.5",
                    ratingNumeric = 7.5,
                    tmdbId = "866398",
                    releaseYear = 2024,
                    containerExtension = "mp4",
                    addedEpoch = 1710000090L
                ),
                StreamEntity(
                    playlistId = playlistId,
                    streamId = 9003L,
                    categoryId = "cat_ar",
                    name = "[AR] The Beekeeper",
                    streamType = "movie",
                    streamIcon = sharedPoster,
                    rating = "",
                    ratingNumeric = 0.0,
                    tmdbId = "",
                    releaseYear = 0,
                    containerExtension = "mp4",
                    addedEpoch = 1710000080L
                ),
                StreamEntity(
                    playlistId = playlistId,
                    streamId = 9004L,
                    categoryId = "cat_es",
                    name = "[ES] El Apicultor",
                    streamType = "movie",
                    streamIcon = sharedPoster,
                    rating = "",
                    ratingNumeric = 0.0,
                    tmdbId = "",
                    releaseYear = 0,
                    containerExtension = "mp4",
                    addedEpoch = 1710000070L
                )
            )
            db.streamDao().insertStreamsBatch(fragmentedStreams)

            val projections = db.streamDao().getStreamIndexProjectionsChunk(playlistId, "movie", 100, 0)
            assertEquals(4, projections.size)

            val (groups, members) = com.example.data.sync.CatalogDiffEngine.buildLosslessGroups(
                playlistId = playlistId,
                streamType = "movie",
                streams = projections,
                categoryNameMap = mapOf(
                    "cat_en" to "EN | Action 4K",
                    "cat_fr" to "FR | Films Action",
                    "cat_ar" to "AR | Aflam",
                    "cat_es" to "ES | Cine"
                )
            )

            assertEquals("All 4 fragmented copies must unify into 1 group", 1, groups.size)
            assertEquals("Group versionCount must equal 4 losslessly", 4, groups.first().versionCount)
            assertEquals("Membership join table must contain all 4 streams", 4, members.size)

            db.vodGroupDao().insertGroupsBatch(groups)
            db.vodGroupDao().insertGroupMembersBatch(members)

            val persistedMembers = db.vodGroupDao().getMemberStreamsForGroup(
                playlistId = playlistId,
                streamType = "movie",
                groupKey = groups.first().groupKey
            )
            assertEquals(
                setOf(9001L, 9002L, 9003L, 9004L),
                persistedMembers.map { it.streamId }.toSet()
            )
        } finally {
            db.close()
        }
    }

    @Test
    fun phase5d_incrementalSmartDiffSkipsUnchangedPreservesUserStateAndSafetyFloorInterrupts() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, StreamVaultDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        // Avoid external network calls for global popularity index during this sync test
        val cacheFile = java.io.File(context.filesDir, "global_popularity_index.json")
        val metaFile = java.io.File(context.filesDir, "global_popularity_index.meta")
        cacheFile.writeText("{\"v\":1,\"generatedAt\":1730000000,\"m\":{},\"s\":{},\"t\":{}}")
        metaFile.writeText("etag=\nlastFetchMs=${System.currentTimeMillis()}\n")

        val serverPhase = java.util.concurrent.atomic.AtomicInteger(1)
        val stallStreamLatch = java.util.concurrent.CountDownLatch(1)
        val releaseStallLatch = java.util.concurrent.CountDownLatch(1)
        val serverSocket = java.net.ServerSocket(0)
        val port = serverSocket.localPort
        val serverRunning = java.util.concurrent.atomic.AtomicBoolean(true)

        fun buildVodJson(count: Int, renameFirst: Boolean): String {
            val sb = StringBuilder(count * 140)
            sb.append('[')
            for (i in 1..count) {
                if (i > 1) sb.append(',')
                val streamId = 1000 + i
                val title = if (i == 1 && renameFirst) {
                    "Action Hero 1 (Director's Cut) (2024)"
                } else {
                    "Action Hero $i (2024)"
                }
                sb.append(
                    """{"stream_id":$streamId,"name":"$title","category_id":"cat_movies","stream_icon":"https://image.tmdb.org/t/p/w500/p$i.jpg","rating":"7.5","added":"1710000000","container_extension":"mp4"}"""
                )
            }
            sb.append(']')
            return sb.toString()
        }

        val serverThread = Thread {
            while (serverRunning.get()) {
                try {
                    val client = serverSocket.accept()
                    Thread {
                        client.use { sock ->
                            val reader = sock.getInputStream().bufferedReader()
                            val requestLine = reader.readLine() ?: return@use
                            while (true) {
                                val headerLine = reader.readLine()
                                if (headerLine.isNullOrEmpty()) break
                            }
                            val out = sock.getOutputStream()
                            val body = when {
                                requestLine.contains("action=get_vod_categories") ->
                                    """[{"category_id":"cat_movies","category_name":"Movies Action"}]"""
                                requestLine.contains("action=get_vod_streams") -> {
                                    when (serverPhase.get()) {
                                        1, 2 -> buildVodJson(120, renameFirst = false)
                                        3 -> buildVodJson(120, renameFirst = true)
                                        4 -> buildVodJson(20, renameFirst = true) // < 50% of 120 -> safety floor
                                        else -> {
                                            // Phase 5: abrupt mid-stream truncation (incomplete JSON array)
                                            val partial = """[{"stream_id":1001,"name":"Action Hero 1 (2024)","category_id":"cat_movies"},"""
                                            val partialBytes = partial.toByteArray(Charsets.UTF_8)
                                            val respHeader = "HTTP/1.1 200 OK\r\n" +
                                                "Content-Type: application/json\r\n" +
                                                "Content-Length: ${partialBytes.size + 4096}\r\n" +
                                                "Connection: close\r\n\r\n"
                                            out.write(respHeader.toByteArray(Charsets.UTF_8))
                                            out.write(partialBytes)
                                            out.flush()
                                            return@use
                                        }
                                    }
                                }
                                else ->
                                    """{"user_info":{"auth":1,"status":"Active"},"server_info":{"url":"127.0.0.1","port":"$port","server_protocol":"http"}}"""
                            }
                            val bytes = body.toByteArray(Charsets.UTF_8)
                            val response = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: application/json\r\n" +
                                "Content-Length: ${bytes.size}\r\n" +
                                "Connection: close\r\n\r\n"
                            out.write(response.toByteArray(Charsets.UTF_8))
                            out.write(bytes)
                            out.flush()
                        }
                    }.start()
                } catch (_: Exception) {
                    // Socket closed on shutdown
                }
            }
        }
        serverThread.isDaemon = true
        serverThread.start()

        try {
            val playlistId = db.playlistDao().insertPlaylist(
                PlaylistEntity(
                    name = "Smart Diff Test",
                    serverUrl = "http://127.0.0.1:$port",
                    username = "demo",
                    password = "secret",
                    isActive = true
                )
            )
            val playlist = db.playlistDao().getPlaylistById(playlistId)!!
            val repo = IptvRepository(
                playlistDao = db.playlistDao(),
                categoryDao = db.categoryDao(),
                streamDao = db.streamDao(),
                epgDao = db.epgDao(),
                parentalDao = db.parentalSettingsDao(),
                vodGroupDao = db.vodGroupDao(),
                database = db,
                appContext = context,
                syncSectionStateDao = db.syncSectionStateDao()
            )

            // Sync 1: Initial load of 120 movies
            serverPhase.set(1)
            assertTrue(repo.syncPlaylistSection(playlist, "MOVIE").isSuccess)
            assertEquals(120, db.streamDao().countStreams(playlistId, "movie"))
            val state1 = db.syncSectionStateDao().getSectionOnce(playlistId, "MOVIE")!!
            assertEquals("COMPLETED", state1.status)
            assertEquals(120, state1.itemsAdded)
            assertEquals(0, state1.itemsUnchanged)

            // Mark stream 1001 as favorite with resume position
            val targetStream = db.streamDao().getStreamByProviderId(playlistId, 1001L)!!
            db.streamDao().toggleFavorite(targetStream.id, true)
            db.streamDao().updatePlaybackPosition(targetStream.id, 180_000L, 1715000000000L)

            // Sync 2: Identical payload -> 120 unchanged, 0 added, 0 updated, 0 removed
            serverPhase.set(2)
            assertTrue(repo.syncPlaylistSection(playlist, "MOVIE").isSuccess)
            val state2 = db.syncSectionStateDao().getSectionOnce(playlistId, "MOVIE")!!
            assertEquals("COMPLETED", state2.status)
            assertEquals(120, state2.itemsUnchanged)
            assertEquals(0, state2.itemsAdded)
            assertEquals(0, state2.itemsUpdated)
            assertEquals(0, state2.itemsRemoved)

            // Sync 3: 1 modified item -> 1 updated, 119 unchanged, favorite & resume position preserved
            serverPhase.set(3)
            assertTrue(repo.syncPlaylistSection(playlist, "MOVIE").isSuccess)
            val state3 = db.syncSectionStateDao().getSectionOnce(playlistId, "MOVIE")!!
            assertEquals(1, state3.itemsUpdated)
            assertEquals(119, state3.itemsUnchanged)
            assertEquals(0, state3.itemsRemoved)

            val updatedTarget = db.streamDao().getStreamByProviderId(playlistId, 1001L)!!
            assertEquals("Action Hero 1 (Director's Cut) (2024)", updatedTarget.name)
            assertTrue("isFavorite must survive incremental update", updatedTarget.isFavorite)
            assertEquals("lastPositionMs must survive incremental update", 180_000L, updatedTarget.lastPositionMs)

            // Sync 4: Provider returns < 50% (20 of 120) -> 50% safety floor blocks destructive deletions
            serverPhase.set(4)
            assertTrue(repo.syncPlaylistSection(playlist, "MOVIE").isSuccess)
            val state4 = db.syncSectionStateDao().getSectionOnce(playlistId, "MOVIE")!!
            assertEquals("Safety floor must block deletions when < 50% returned", 0, state4.itemsRemoved)
            assertEquals("All 120 existing movies must remain intact", 120, db.streamDao().countStreams(playlistId, "movie"))
            assertTrue("Safety floor should flag pending warning", state4.pendingUpdateAvailable)

            // Sync 5a: Abrupt mid-stream socket truncation never deletes existing movies
            serverPhase.set(5)
            repo.syncPlaylistSection(playlist, "MOVIE")
            assertEquals("Abrupt stream drop must never delete existing items", 120, db.streamDao().countStreams(playlistId, "movie"))

            // Sync 5b: Simulated process kill mid-sync -> repository startup marks RUNNING as INTERRUPTED
            db.syncSectionStateDao().upsert(
                state4.copy(
                    status = "RUNNING",
                    stage = "DOWNLOADING_AND_DIFFING",
                    progressPercent = 34
                )
            )
            IptvRepository(
                playlistDao = db.playlistDao(),
                categoryDao = db.categoryDao(),
                streamDao = db.streamDao(),
                epgDao = db.epgDao(),
                parentalDao = db.parentalSettingsDao(),
                vodGroupDao = db.vodGroupDao(),
                database = db,
                appContext = context,
                syncSectionStateDao = db.syncSectionStateDao()
            )
            val deadline = System.currentTimeMillis() + 3_000L
            var recovered = db.syncSectionStateDao().getSectionOnce(playlistId, "MOVIE")!!
            while (System.currentTimeMillis() < deadline && recovered.status != "INTERRUPTED") {
                Thread.sleep(20L)
                recovered = db.syncSectionStateDao().getSectionOnce(playlistId, "MOVIE")!!
            }
            assertEquals("INTERRUPTED", recovered.status)
            assertEquals(120, db.streamDao().countStreams(playlistId, "movie"))
        } finally {
            serverRunning.set(false)
            runCatching { serverSocket.close() }
            db.close()
            cacheFile.delete()
            metaFile.delete()
        }
    }

    @Test
    fun phase5d_playbackResourceGovernorScalesBatchSizeDuringActivePlayback() {
        try {
            com.example.data.sync.PlaybackResourceGovernor.setPlaybackActive(false)
            assertFalse(com.example.data.sync.PlaybackResourceGovernor.isPlaybackActive.value)
            assertEquals(1_000, com.example.data.sync.PlaybackResourceGovernor.recommendedDbBatchSize())

            com.example.data.sync.PlaybackResourceGovernor.setPlaybackActive(true)
            assertTrue(com.example.data.sync.PlaybackResourceGovernor.isPlaybackActive.value)
            assertEquals(250, com.example.data.sync.PlaybackResourceGovernor.recommendedDbBatchSize())
        } finally {
            com.example.data.sync.PlaybackResourceGovernor.setPlaybackActive(false)
        }
        assertEquals(1_000, com.example.data.sync.PlaybackResourceGovernor.recommendedDbBatchSize())
    }

    @Test
    fun phase5d_catalogUpdateCheckWorkerDetectsNewCategoriesWithoutDownloadingStreams() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = StreamVaultDatabase.getInstance(context)
        val cacheFile = java.io.File(context.filesDir, "global_popularity_index.json")
        val metaFile = java.io.File(context.filesDir, "global_popularity_index.meta")
        cacheFile.writeText("{\"v\":1,\"generatedAt\":1730000000,\"m\":{},\"s\":{},\"t\":{}}")
        metaFile.writeText("etag=\nlastFetchMs=${System.currentTimeMillis()}\n")

        val receivedRequests = java.util.Collections.synchronizedList(ArrayList<String>())
        val serverSocket = java.net.ServerSocket(0)
        val port = serverSocket.localPort
        val serverRunning = java.util.concurrent.atomic.AtomicBoolean(true)

        val serverThread = Thread {
            while (serverRunning.get()) {
                try {
                    val client = serverSocket.accept()
                    Thread {
                        client.use { sock ->
                            val reader = sock.getInputStream().bufferedReader()
                            val requestLine = reader.readLine() ?: return@use
                            receivedRequests.add(requestLine)
                            while (true) {
                                val headerLine = reader.readLine()
                                if (headerLine.isNullOrEmpty()) break
                            }
                            val body = when {
                                requestLine.contains("action=get_vod_categories") ->
                                    """[{"category_id":"cat_vod_1","category_name":"Action"},{"category_id":"cat_vod_2","category_name":"2026 Releases"}]"""
                                requestLine.contains("action=get_live_categories") ->
                                    """[{"category_id":"cat_live_1","category_name":"Sports"}]"""
                                requestLine.contains("action=get_series_categories") ->
                                    """[{"category_id":"cat_ser_1","category_name":"Drama"}]"""
                                else -> "[]"
                            }
                            val bytes = body.toByteArray(Charsets.UTF_8)
                            val response = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: application/json\r\n" +
                                "Content-Length: ${bytes.size}\r\n" +
                                "Connection: close\r\n\r\n"
                            val out = sock.getOutputStream()
                            out.write(response.toByteArray(Charsets.UTF_8))
                            out.write(bytes)
                            out.flush()
                        }
                    }.start()
                } catch (_: Exception) {
                    // Closed on teardown
                }
            }
        }
        serverThread.isDaemon = true
        serverThread.start()

        var seededPlaylistId = -1L
        try {
            seededPlaylistId = db.playlistDao().insertPlaylist(
                PlaylistEntity(
                    name = "Update Probe Test",
                    serverUrl = "http://127.0.0.1:$port",
                    username = "probe",
                    password = "secret",
                    isActive = true
                )
            )
            db.playlistDao().setActivePlaylist(seededPlaylistId)
            db.categoryDao().insertCategories(
                listOf(
                    CategoryEntity(playlistId = seededPlaylistId, categoryId = "cat_live_1", categoryName = "Sports", type = "LIVE"),
                    CategoryEntity(playlistId = seededPlaylistId, categoryId = "cat_vod_1", categoryName = "Action", type = "MOVIE"),
                    CategoryEntity(playlistId = seededPlaylistId, categoryId = "cat_ser_1", categoryName = "Drama", type = "SERIES")
                )
            )

            val worker = androidx.work.testing.TestListenableWorkerBuilder<com.example.data.sync.CatalogUpdateCheckWorker>(
                context
            ).build()
            val workResult = worker.doWork()
            assertEquals(androidx.work.ListenableWorker.Result.success(), workResult)

            // Verify MOVIE section flagged pendingUpdateAvailable due to new cat_vod_2
            val movieSyncState = db.syncSectionStateDao().getSectionOnce(seededPlaylistId, "MOVIE")
            assertNotNull("MOVIE sync state should be flagged for pending update", movieSyncState)
            assertTrue(movieSyncState!!.pendingUpdateAvailable)
            assertTrue(movieSyncState.pendingUpdateSummary.contains("1 new category"))

            // Verify ZERO GET requests were made to full stream catalog endpoints
            val forbiddenGets = receivedRequests.filter { req ->
                req.startsWith("GET ") && (
                    req.contains("action=get_vod_streams") ||
                        req.contains("action=get_live_streams") ||
                        req.contains("action=get_series&") ||
                        req.contains("action=get_series ")
                    )
            }
            assertTrue("Background probe must NEVER GET full stream catalogs, saw: $forbiddenGets", forbiddenGets.isEmpty())
            assertTrue(receivedRequests.any { it.contains("action=get_vod_categories") })
        } finally {
            serverRunning.set(false)
            runCatching { serverSocket.close() }
            if (seededPlaylistId > 0L) {
                db.openHelper.writableDatabase.execSQL(
                    "DELETE FROM sync_section_state WHERE playlistId = ?",
                    arrayOf(seededPlaylistId)
                )
                db.categoryDao().clearCategoriesForPlaylist(seededPlaylistId)
                db.playlistDao().deletePlaylist(seededPlaylistId)
            }
            cacheFile.delete()
            metaFile.delete()
        }
    }

    @Test
    fun phase6c_parseVersionMetadataNeverDoublesSubOrMultiAndPreservesLanguageRanks() {
        val catNames = mapOf(
            "c_alb" to "ALB SUB | MOVIES",
            "c_ar" to "ARABIC SUBTITLED MOVIES",
            "c_fr" to "FRENCH VOSTFR",
            "c_fa" to "PERSIAN SUB MOVIES",
            "c_bg" to "BULGARIAN SUB MOVIES",
            "c_ku" to "KURDISH SUB MOVIES",
            "c_multi" to "MULTI-SUB 4K",
            "c_en" to "ENGLISH 4K MOVIES"
        )

        val cases = listOf(
            Triple(
                StreamEntity(id = 1L, playlistId = 1L, streamId = 1L, name = "Dune Part Two (2024)", streamType = "movie", categoryId = "c_alb", versionTag = "Albanian (Sub)"),
                "Albanian (Sub)",
                4
            ),
            Triple(
                StreamEntity(id = 2L, playlistId = 1L, streamId = 2L, name = "Dune Part Two (2024)", streamType = "movie", categoryId = "c_ar", versionTag = "Arabic (Sub)"),
                "Arabic (Sub)",
                1
            ),
            Triple(
                StreamEntity(id = 3L, playlistId = 1L, streamId = 3L, name = "Dune Part Two (2024)", streamType = "movie", categoryId = "c_fr", versionTag = "French (VOSTFR)"),
                "French (VOSTFR)",
                2
            ),
            Triple(
                StreamEntity(id = 4L, playlistId = 1L, streamId = 4L, name = "Dune Part Two (2024)", streamType = "movie", categoryId = "c_fa", versionTag = "Persian (Sub)"),
                "Persian (Sub)",
                4
            ),
            Triple(
                StreamEntity(id = 5L, playlistId = 1L, streamId = 5L, name = "Dune Part Two (2024)", streamType = "movie", categoryId = "c_bg", versionTag = "Bulgarian (Sub)"),
                "Bulgarian (Sub)",
                4
            ),
            Triple(
                StreamEntity(id = 6L, playlistId = 1L, streamId = 6L, name = "Dune Part Two (2024)", streamType = "movie", categoryId = "c_ku", versionTag = "Kurdish (Sub)"),
                "Kurdish (Sub)",
                4
            ),
            Triple(
                StreamEntity(id = 7L, playlistId = 1L, streamId = 7L, name = "Dune Part Two (2024) 4K", streamType = "movie", categoryId = "c_multi", versionTag = "Multi-Sub"),
                "Multi-Sub",
                3
            ),
            Triple(
                StreamEntity(id = 8L, playlistId = 1L, streamId = 8L, name = "EN - Dune Part Two (2024) 4K", streamType = "movie", categoryId = "c_en", versionTag = "EN • 4K"),
                "English",
                0
            )
        )

        val parsedAll = cases.map { (stream, expectedGroup, expectedRank) ->
            val meta = com.example.ui.vod.parseVersionMetadata(
                version = stream,
                categoryNameById = catNames,
                includeContainer = true
            )
            assertFalse(
                "Language group must NEVER contain doubled (Sub): ${meta.languageGroup}",
                meta.languageGroup.contains("(Sub) (Sub)", ignoreCase = true)
            )
            assertFalse(
                "Language group must NEVER contain Multi-Sub (Multi): ${meta.languageGroup}",
                meta.languageGroup.contains("Multi-Sub (Multi)", ignoreCase = true)
            )
            assertEquals("Unexpected languageGroup for ${stream.versionTag}", expectedGroup, meta.languageGroup)
            assertEquals("Unexpected priorityRank for ${stream.versionTag}", expectedRank, meta.priorityRank)
            meta
        }

        val grouped = com.example.ui.vod.groupVersionsByLanguage(parsedAll)
        assertEquals("English", grouped[0].first)
        assertEquals("Arabic (Sub)", grouped[1].first)
        assertEquals("French (VOSTFR)", grouped[2].first)
    }

    @Test
    fun phase6c_catalogSyncSchedulerSerializesPrioritizesAndDeduplicatesQueue() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = StreamVaultDatabase.getInstance(context)
        val playlistId = 88991L
        try {
            com.example.data.sync.CatalogSyncScheduler.clearForTest()

            // Enqueue VOD (P2 MOVIE), VOD (P2 SERIES), LIVE (P1), and Category-scoped MOVIE refresh (P0)
            com.example.data.sync.CatalogSyncScheduler.enqueueSectionSync(
                context = context,
                playlistId = playlistId,
                sectionType = "SERIES"
            )
            // Duplicate SERIES enqueue must be deduplicated cleanly
            com.example.data.sync.CatalogSyncScheduler.enqueueSectionSync(
                context = context,
                playlistId = playlistId,
                sectionType = "SERIES"
            )
            com.example.data.sync.CatalogSyncScheduler.enqueueSectionSync(
                context = context,
                playlistId = playlistId,
                sectionType = "LIVE"
            )
            com.example.data.sync.CatalogSyncScheduler.enqueueSectionSync(
                context = context,
                playlistId = playlistId,
                sectionType = "MOVIE",
                categoryId = "cat_42"
            )

            assertTrue(com.example.data.sync.CatalogSyncScheduler.isSectionQueuedOrRunning(playlistId, "LIVE"))
            assertTrue(com.example.data.sync.CatalogSyncScheduler.isSectionQueuedOrRunning(playlistId, "SERIES"))
            assertTrue(com.example.data.sync.CatalogSyncScheduler.isSectionQueuedOrRunning(playlistId, "MOVIE"))

            // Priority drain order: P0 category-scoped MOVIE -> P1 LIVE -> P2 SERIES
            val first = com.example.data.sync.CatalogSyncScheduler.pollNextPendingRequest(context, null)
            assertNotNull(first)
            assertEquals("MOVIE", first!!.sectionType)
            assertEquals("cat_42", first.categoryId)
            assertEquals(com.example.data.sync.SyncPriority.CATEGORY_REFRESH, first.priority)
            com.example.data.sync.CatalogSyncScheduler.markRequestFinished(playlistId, first.sectionType)

            val second = com.example.data.sync.CatalogSyncScheduler.pollNextPendingRequest(context, null)
            assertNotNull(second)
            assertEquals("LIVE", second!!.sectionType)
            assertEquals(com.example.data.sync.SyncPriority.LIVE_SYNC, second.priority)
            com.example.data.sync.CatalogSyncScheduler.markRequestFinished(playlistId, second.sectionType)

            val third = com.example.data.sync.CatalogSyncScheduler.pollNextPendingRequest(context, null)
            assertNotNull(third)
            assertEquals("SERIES", third!!.sectionType)
            assertEquals(com.example.data.sync.SyncPriority.VOD_SYNC, third.priority)
            com.example.data.sync.CatalogSyncScheduler.markRequestFinished(playlistId, third.sectionType)

            assertFalse(com.example.data.sync.CatalogSyncScheduler.hasPendingRequests())

            // Verify waiting status telemetry formatting
            val waitingTelemetry = com.example.data.sync.SyncTelemetry(
                sectionType = "MOVIE",
                status = com.example.data.sync.SyncStatus.PENDING,
                pendingUpdateSummary = com.example.data.sync.CatalogSyncScheduler.STATUS_QUEUED_WAITING
            )
            val subtitle = com.example.ui.vod.formatSyncStatsSubtitle(waitingTelemetry)
            assertTrue("Expected single-queue waiting subtitle, got: $subtitle", subtitle.contains("Starts automatically"))
        } finally {
            com.example.data.sync.CatalogSyncScheduler.clearForTest()
            db.openHelper.writableDatabase.execSQL(
                "DELETE FROM sync_section_state WHERE playlistId = ?",
                arrayOf(playlistId)
            )
        }
    }

    @Test
    fun phase6c_threeLevelMovieOrganizationSeparatesDuplicatesFranchisesAndStandalone() {
        val collectionsJson = """
            {
              "v": 1,
              "c": {
                "645": {"n": "James Bond Collection", "p": "/bond_poster.jpg"},
                "726871": {"n": "Dune Collection", "p": "/dune_poster.jpg"}
              },
              "m": {
                "36557": ["645", 21],
                "37724": ["645", 23],
                "438631": ["726871", 1]
              },
              "t": {
                "casino royale_2006": ["645", 21],
                "skyfall_2012": ["645", 23],
                "dune_2021": ["726871", 1]
              }
            }
        """.trimIndent()
        try {
            com.example.data.remote.GlobalCollectionsIndexManager.loadFromJsonStringForTest(collectionsJson)

            val streams = listOf(
                // Level 1: Two duplicate versions of Casino Royale (same TMDb ID 36557) -> MUST merge into 1 group
                com.example.data.local.StreamIndexProjection(
                    id = 101L, streamId = 101L, categoryId = "cat_en",
                    name = "EN - Casino Royale (2006) 4K",
                    streamIcon = "https://image.tmdb.org/t/p/w500/casino.jpg",
                    rating = "8.1", ratingNumeric = 8.1, containerExtension = "mp4",
                    tmdbId = "36557", canonicalGroupKey = "tmdb_movie_36557",
                    versionTag = "EN • 4K", addedEpoch = 100L, releaseYear = 2006, genres = "Action"
                ),
                com.example.data.local.StreamIndexProjection(
                    id = 102L, streamId = 102L, categoryId = "cat_ar",
                    name = "AR - Casino Royale (2006) SUB",
                    streamIcon = "https://image.tmdb.org/t/p/w500/casino.jpg",
                    rating = "8.1", ratingNumeric = 8.1, containerExtension = "mp4",
                    tmdbId = "36557", canonicalGroupKey = "tmdb_movie_36557",
                    versionTag = "Arabic (Sub)", addedEpoch = 101L, releaseYear = 2006, genres = "Action"
                ),
                // Level 2: Second James Bond installment -> MUST remain independent group linked to collectionId 645
                com.example.data.local.StreamIndexProjection(
                    id = 103L, streamId = 103L, categoryId = "cat_en",
                    name = "EN - Skyfall (2012) 4K",
                    streamIcon = "https://image.tmdb.org/t/p/w500/skyfall.jpg",
                    rating = "8.8", ratingNumeric = 8.8, containerExtension = "mp4",
                    tmdbId = "37724", canonicalGroupKey = "tmdb_movie_37724",
                    versionTag = "EN • 4K", addedEpoch = 102L, releaseYear = 2012, genres = "Action"
                ),
                // Level 3a: Only 1 movie from Dune Collection in library -> MUST NOT emit a 1-movie collection
                com.example.data.local.StreamIndexProjection(
                    id = 104L, streamId = 104L, categoryId = "cat_en",
                    name = "EN - Dune (2021) 4K",
                    streamIcon = "https://image.tmdb.org/t/p/w500/dune.jpg",
                    rating = "8.4", ratingNumeric = 8.4, containerExtension = "mp4",
                    tmdbId = "438631", canonicalGroupKey = "tmdb_movie_438631",
                    versionTag = "EN • 4K", addedEpoch = 103L, releaseYear = 2021, genres = "Sci-Fi"
                ),
                // Level 3b: Standalone movie -> collectionId == ""
                com.example.data.local.StreamIndexProjection(
                    id = 105L, streamId = 105L, categoryId = "cat_en",
                    name = "EN - Inception (2010) 4K",
                    streamIcon = "https://image.tmdb.org/t/p/w500/inception.jpg",
                    rating = "8.9", ratingNumeric = 8.9, containerExtension = "mp4",
                    tmdbId = "27205", canonicalGroupKey = "tmdb_movie_27205",
                    versionTag = "EN • 4K", addedEpoch = 104L, releaseYear = 2010, genres = "Sci-Fi"
                )
            )

            val output = com.example.data.sync.CatalogDiffEngine.buildLosslessGroups(
                playlistId = 7L,
                streamType = "movie",
                streams = streams,
                categoryNameMap = mapOf("cat_en" to "EN | MOVIES", "cat_ar" to "AR | SUB MOVIES")
            )

            // 5 raw streams -> 4 independent groups (2 Casino Royale versions collapsed into 1;
            // Phase 7A emits vod_group_members only for multi-version groups -> 2 member rows)
            assertEquals(4, output.groups.size)
            assertEquals(2, output.members.size)

            val casinoGroup = output.groups.first { it.cleanTitle.contains("Casino Royale", ignoreCase = true) }
            val skyfallGroup = output.groups.first { it.cleanTitle.contains("Skyfall", ignoreCase = true) }
            val duneGroup = output.groups.first { it.cleanTitle.contains("Dune", ignoreCase = true) }
            val inceptionGroup = output.groups.first { it.cleanTitle.contains("Inception", ignoreCase = true) }

            // Level 1 check
            assertEquals(2, casinoGroup.versionCount)
            assertEquals(1, skyfallGroup.versionCount)

            // Level 2 check: Independent groups both linked to James Bond Collection with order
            assertEquals("645", casinoGroup.collectionId)
            assertEquals("James Bond Collection", casinoGroup.collectionName)
            assertEquals(21, casinoGroup.collectionOrder)
            assertEquals("645", skyfallGroup.collectionId)
            assertEquals("James Bond Collection", skyfallGroup.collectionName)
            assertEquals(23, skyfallGroup.collectionOrder)

            assertEquals(1, output.collections.size)
            assertEquals("645", output.collections[0].collectionId)
            assertEquals("James Bond Collection", output.collections[0].name)
            assertEquals(2, output.collections[0].movieCount)

            // Level 3 check: 1-movie match (Dune) and standalone (Inception) have empty collectionId
            assertEquals("", duneGroup.collectionId)
            assertEquals("", inceptionGroup.collectionId)
        } finally {
            com.example.data.remote.GlobalCollectionsIndexManager.clearForTest()
        }
    }

    @Test
    fun phase6c_vodViewModelCollectionsSortingDossierSiblingsAndLiveCounts() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = StreamVaultDatabase.getInstance(app)
        var seededPlaylistId = -1L

        try {
            // Seed playlist with stale huge vodCount (108,046) to confirm liveMovieCount uses Room count (3)
            seededPlaylistId = db.playlistDao().insertPlaylist(
                PlaylistEntity(
                    name = "Phase 6C Collections Test",
                    serverUrl = "http://127.0.0.1:9",
                    username = "u",
                    password = "p",
                    isActive = true,
                    vodCount = 108_046,
                    seriesCount = 12_345
                )
            )
            db.playlistDao().setActivePlaylist(seededPlaylistId)

            val s1 = StreamEntity(
                playlistId = seededPlaylistId, streamId = 1001L, name = "Casino Royale (2006)",
                streamType = "movie", categoryId = "cat_act", releaseYear = 2006, ratingNumeric = 8.1,
                canonicalGroupKey = "casino_royale_2006"
            )
            val s2 = StreamEntity(
                playlistId = seededPlaylistId, streamId = 1002L, name = "Skyfall (2012)",
                streamType = "movie", categoryId = "cat_act", releaseYear = 2012, ratingNumeric = 9.2,
                canonicalGroupKey = "skyfall_2012"
            )
            val s3 = StreamEntity(
                playlistId = seededPlaylistId, streamId = 1003L, name = "Inception (2010)",
                streamType = "movie", categoryId = "cat_sci", releaseYear = 2010, ratingNumeric = 8.9,
                canonicalGroupKey = "inception_2010"
            )
            db.streamDao().insertStreamsBatch(listOf(s1, s2, s3))
            val inserted = db.streamDao().getStreamsChunkByType(seededPlaylistId, "movie", 50, 0).associateBy { it.streamId }
            val b1 = inserted[1001L]!!
            val b2 = inserted[1002L]!!
            val m3 = inserted[1003L]!!

            db.vodGroupDao().deleteGroupsByType(seededPlaylistId, "movie")
            db.vodGroupDao().deleteGroupMembersByType(seededPlaylistId, "movie")
            db.vodGroupDao().insertGroupsBatch(
                listOf(
                    com.example.data.model.VodCatalogGroupEntity(
                        playlistId = seededPlaylistId, streamType = "movie", groupKey = "casino_royale_2006",
                        cleanTitle = "Casino Royale", releaseYear = 2006, ratingNumeric = 8.1,
                        representativeStreamDbId = b1.id, representativeStreamId = b1.streamId,
                        versionCount = 1, popularityScore = 82.0,
                        collectionId = "645", collectionName = "James Bond Collection", collectionOrder = 21
                    ),
                    com.example.data.model.VodCatalogGroupEntity(
                        playlistId = seededPlaylistId, streamType = "movie", groupKey = "skyfall_2012",
                        cleanTitle = "Skyfall", releaseYear = 2012, ratingNumeric = 9.2,
                        representativeStreamDbId = b2.id, representativeStreamId = b2.streamId,
                        versionCount = 1, popularityScore = 96.0,
                        collectionId = "645", collectionName = "James Bond Collection", collectionOrder = 23
                    ),
                    com.example.data.model.VodCatalogGroupEntity(
                        playlistId = seededPlaylistId, streamType = "movie", groupKey = "inception_2010",
                        cleanTitle = "Inception", releaseYear = 2010, ratingNumeric = 8.9,
                        representativeStreamDbId = m3.id, representativeStreamId = m3.streamId,
                        versionCount = 1, popularityScore = 91.0
                    )
                )
            )
            db.vodGroupDao().insertGroupMembersBatch(
                listOf(
                    com.example.data.model.VodGroupMemberEntity(seededPlaylistId, "movie", "casino_royale_2006", b1.id),
                    com.example.data.model.VodGroupMemberEntity(seededPlaylistId, "movie", "skyfall_2012", b2.id),
                    com.example.data.model.VodGroupMemberEntity(seededPlaylistId, "movie", "inception_2010", m3.id)
                )
            )
            db.vodGroupDao().deleteCollectionsForPlaylist(seededPlaylistId)
            db.vodGroupDao().insertCollectionsBatch(
                listOf(
                    com.example.data.model.VodCollectionEntity(
                        playlistId = seededPlaylistId,
                        collectionId = "645",
                        name = "James Bond Collection",
                        posterUrl = "https://image.tmdb.org/t/p/w500/bond.jpg",
                        movieCount = 2,
                        popularityScore = 96.0
                    )
                )
            )

            val vm = com.example.ui.vod.VodViewModel(app)
            val collectorScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default)
            val collectors = listOf(
                collectorScope.launch { vm.liveMovieCount.collect {} },
                collectorScope.launch { vm.movieCollections.collect {} },
                collectorScope.launch { vm.selectedCollectionMovies.collect {} },
                collectorScope.launch { vm.activeMovieCollectionItems.collect {} }
            )
            val awaitUntil: (Long, () -> Boolean) -> Unit = { timeoutMs, predicate ->
                val deadline = System.currentTimeMillis() + timeoutMs
                while (System.currentTimeMillis() < deadline && !predicate()) {
                    org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
                    Thread.sleep(20L)
                }
                org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
            }
            try {
                // Wait for Room flows to hydrate
                awaitUntil(5_000L) { vm.liveMovieCount.value == 3 && vm.movieCollections.value.isNotEmpty() }

                assertEquals("liveMovieCount must reflect actual Room count, not stale 108,046", 3, vm.liveMovieCount.value)
                assertEquals(1, vm.movieCollections.value.size)
                val bondCol = vm.movieCollections.value.first()
                assertEquals("James Bond Collection", bondCol.name)

                // Open collection & verify RELEASE_ORDER (Casino Royale 2006 -> Skyfall 2012)
                vm.openCollection(bondCol)
                awaitUntil(5_000L) { vm.selectedCollectionMovies.value.size >= 2 }
                assertEquals(listOf(1001L, 1002L), vm.selectedCollectionMovies.value.map { it.stream.streamId })

                // Switch to NEWEST_FIRST (Skyfall 2012 -> Casino Royale 2006)
                vm.setCollectionSortMode(com.example.ui.vod.CollectionSortMode.NEWEST_FIRST)
                awaitUntil(5_000L) { vm.selectedCollectionMovies.value.firstOrNull()?.stream?.streamId == 1002L }
                assertEquals(listOf(1002L, 1001L), vm.selectedCollectionMovies.value.map { it.stream.streamId })

                // Switch to TOP_RATED (Skyfall 96.0 -> Casino Royale 82.0)
                vm.setCollectionSortMode(com.example.ui.vod.CollectionSortMode.TOP_RATED)
                awaitUntil(2_000L) { vm.selectedCollectionMovies.value.firstOrNull()?.stream?.streamId == 1002L }
                assertEquals(listOf(1002L, 1001L), vm.selectedCollectionMovies.value.map { it.stream.streamId })

                vm.closeCollection()
                assertNull(vm.selectedCollection.value)

                // Open Movie Dossier for Casino Royale -> loads sibling franchise installments in Release Order
                val playlist = db.playlistDao().getActivePlaylistOnce()!!
                vm.openMovieDetails(b1, playlist, autoSelectPreferred = false)
                awaitUntil(5_000L) { vm.activeMovieCollectionItems.value.size >= 2 }
                assertEquals("James Bond Collection", vm.activeMovieCollectionName.value)
                assertEquals(listOf(1001L, 1002L), vm.activeMovieCollectionItems.value.map { it.stream.streamId })

                vm.closeMovieDetails()
                assertTrue(vm.activeMovieCollectionItems.value.isEmpty())
                assertEquals("", vm.activeMovieCollectionName.value)
            } finally {
                collectors.forEach { it.cancel() }
            }
        } finally {
            if (seededPlaylistId > 0L) {
                db.vodGroupDao().deleteCollectionsForPlaylist(seededPlaylistId)
                db.vodGroupDao().deleteGroupsByType(seededPlaylistId, "movie")
                db.vodGroupDao().deleteGroupMembersByType(seededPlaylistId, "movie")
                db.streamDao().deleteStreamsByType(seededPlaylistId, "movie")
                db.playlistDao().deletePlaylist(seededPlaylistId)
            }
        }
    }

    @Test
    fun phase6c_dualPlatformCollectionsUiAndQueuedStatusRendering() {
        val sampleCollection = com.example.data.model.VodCollectionEntity(
            playlistId = 1L,
            collectionId = "645",
            name = "James Bond Collection",
            posterUrl = "",
            movieCount = 2,
            popularityScore = 95.0
        )
        val bond1 = com.example.data.model.GroupedVodStream(
            stream = StreamEntity(
                id = 1L, playlistId = 1L, streamId = 1001L, categoryId = "cat_act",
                name = "Casino Royale (2006)", streamType = "movie",
                releaseYear = 2006, canonicalGroupKey = "casino_royale_2006"
            ),
            versionCount = 2
        )
        val bond2 = com.example.data.model.GroupedVodStream(
            stream = StreamEntity(
                id = 2L, playlistId = 1L, streamId = 1002L, categoryId = "cat_act",
                name = "Skyfall (2012)", streamType = "movie",
                releaseYear = 2012, canonicalGroupKey = "skyfall_2012"
            ),
            versionCount = 1
        )
        val sortModeState = androidx.compose.runtime.mutableStateOf(com.example.ui.vod.CollectionSortMode.RELEASE_ORDER)
        val openedCollection = androidx.compose.runtime.mutableStateOf<String?>(null)
        val viewMode = androidx.compose.runtime.mutableStateOf("MOBILE_SHELF")

        composeTestRule.setContent {
            when (viewMode.value) {
                "MOBILE_SHELF" -> {
                    androidx.compose.foundation.layout.Column {
                        com.example.ui.vod.VodSyncStatusPill(
                            bannerState = com.example.ui.vod.VodSyncBannerState(
                                isSyncing = true,
                                statusText = com.example.data.sync.CatalogSyncScheduler.STATUS_QUEUED_WAITING,
                                stageLabel = com.example.data.sync.CatalogSyncScheduler.STATUS_QUEUED_WAITING,
                                statsSubtitle = "Single-queue sync • Starts automatically",
                                isIndeterminate = true
                            ),
                            sectionSurface = MoviesAtlanticTeal,
                            sectionHighlight = MoviesMintSpring,
                            onRetry = {},
                            onDismiss = {}
                        )
                        com.example.ui.vod.VodMobileCollectionsShelfRow(
                            collections = listOf(sampleCollection),
                            sectionBaseBg = MoviesSapphireNavy,
                            sectionSurface = MoviesAtlanticTeal,
                            sectionPrimaryAccent = MoviesCyanTeal,
                            sectionHighlight = MoviesMintSpring,
                            onOpenCollection = { openedCollection.value = it.collectionId }
                        )
                        com.example.ui.vod.VodDossierCollectionRow(
                            collectionName = sampleCollection.name,
                            items = listOf(bond1, bond2),
                            activeStreamId = bond1.stream.id,
                            activeCanonicalGroupKey = bond1.stream.canonicalGroupKey,
                            sectionBaseBg = MoviesSapphireNavy,
                            sectionSurface = MoviesAtlanticTeal,
                            sectionPrimaryAccent = MoviesCyanTeal,
                            sectionHighlight = MoviesMintSpring,
                            onItemClick = {},
                            onViewCollection = {}
                        )
                    }
                }
                "MOBILE_DETAIL" -> {
                    com.example.ui.vod.VodMobileCollectionDetailScreen(
                        collection = sampleCollection,
                        movies = listOf(bond1, bond2),
                        sortMode = sortModeState.value,
                        sectionBaseBg = MoviesSapphireNavy,
                        sectionSurface = MoviesAtlanticTeal,
                        sectionPrimaryAccent = MoviesCyanTeal,
                        sectionHighlight = MoviesMintSpring,
                        onBack = {},
                        onSelectSortMode = { sortModeState.value = it },
                        onOpenMovieDetails = {},
                        onPlayImmediately = {},
                        onToggleFavorite = {}
                    )
                }
                "TV_SHELF_AND_LIVE" -> {
                    androidx.compose.foundation.layout.Column {
                        com.example.ui.vod.TvVodTopControlStrip(
                            isMovies = true,
                            activeCategoryLabel = "All Movies",
                            selectedCategoryId = null,
                            searchQuery = "",
                            isSearchExpanded = true,
                            forceGridMode = false,
                            totalCount = 0,
                            isRefreshing = true,
                            syncBannerState = com.example.ui.vod.VodSyncBannerState(
                                isSyncing = true,
                                statusText = com.example.data.sync.CatalogSyncScheduler.STATUS_QUEUED_WAITING,
                                stageLabel = com.example.data.sync.CatalogSyncScheduler.STATUS_QUEUED_WAITING,
                                statsSubtitle = "Single-queue sync • Starts automatically",
                                isIndeterminate = true
                            ),
                            sectionBaseBg = MoviesSapphireNavy,
                            sectionSurface = MoviesAtlanticTeal,
                            sectionPrimaryAccent = MoviesCyanTeal,
                            sectionHighlight = MoviesMintSpring,
                            onOpenCategories = {},
                            onToggleSearch = {},
                            onSearchQueryChange = {},
                            onToggleViewMode = {},
                            onSyncCatalog = {}
                        )
                        com.example.ui.vod.TvCollectionsShelfRow(
                            collections = listOf(sampleCollection),
                            sectionBaseBg = MoviesSapphireNavy,
                            sectionSurface = MoviesAtlanticTeal,
                            sectionPrimaryAccent = MoviesCyanTeal,
                            sectionHighlight = MoviesMintSpring,
                            onOpenCollection = { openedCollection.value = it.collectionId }
                        )
                        com.example.ui.livetv.LiveTvSyncStatusBanner(
                            telemetry = com.example.data.sync.SyncTelemetry(
                                sectionType = "LIVE",
                                status = com.example.data.sync.SyncStatus.PENDING,
                                pendingUpdateSummary = com.example.data.sync.CatalogSyncScheduler.STATUS_QUEUED_WAITING
                            ),
                            onRefresh = {},
                            onDismissPendingUpdate = {}
                        )
                    }
                }
                "TV_DETAIL" -> {
                    com.example.ui.vod.TvCollectionDetailScreen(
                        collection = sampleCollection,
                        movies = listOf(bond1, bond2),
                        sortMode = sortModeState.value,
                        sectionBaseBg = MoviesSapphireNavy,
                        sectionSurface = MoviesAtlanticTeal,
                        sectionPrimaryAccent = MoviesCyanTeal,
                        sectionHighlight = MoviesMintSpring,
                        onBack = {},
                        onSelectSortMode = { sortModeState.value = it },
                        onOpenDetails = {},
                        onPlayImmediately = {},
                        onToggleFavorite = {}
                    )
                }
            }
        }

        // 1. Mobile Shelf + Queued Status Pill + Movie Dossier Franchise Row
        composeTestRule.waitForIdle()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("vod_sync_status_pill")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasText("Queued (waiting for active sync…)")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("vod_collections_shelf")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("vod_collection_card_645")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("movie_dossier_collection_shelf")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasText("Part of James Bond Collection")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("vod_collection_card_645")).performClick()
        assertEquals("645", openedCollection.value)

        // 2. Mobile Collection Detail Screen & Sort Pills
        viewMode.value = "MOBILE_DETAIL"
        composeTestRule.waitForIdle()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("vod_collection_detail_screen")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("vod_collection_back_btn")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("vod_collection_sort_newest_first")).performClick()
        assertEquals(com.example.ui.vod.CollectionSortMode.NEWEST_FIRST, sortModeState.value)

        // 3. Android TV Shelf + Queued Top Control Strip + Live TV Queued Banner + Zero-Count Placeholder
        viewMode.value = "TV_SHELF_AND_LIVE"
        composeTestRule.waitForIdle()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("vod_collections_shelf")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("vod_collection_card_645")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("live_sync_status_banner")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasText("Search cinema movies…")).assertExists()
        assertEquals(
            2,
            composeTestRule.onAllNodes(androidx.compose.ui.test.hasText("Queued (waiting for active sync…)")).fetchSemanticsNodes().size
        )

        // 4. Android TV Collection Detail Screen & Sort Pills
        viewMode.value = "TV_DETAIL"
        composeTestRule.waitForIdle()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("vod_collection_detail_screen")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("vod_collection_back_btn")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("vod_collection_sort_top_rated")).performClick()
        assertEquals(com.example.ui.vod.CollectionSortMode.TOP_RATED, sortModeState.value)
    }

    @Test
    fun v17Phase7b_scoreVersionPreferenceRanksLanguagesAndFrench4KDoesNotBeatEnglish() {
        val englishFhd = com.example.data.model.VodGroupingNormalizer.scoreVersionPreference(
            versionTag = "English • FHD",
            hasPoster = true,
            containerExt = "mp4",
            ratingNumeric = 8.5,
            rawName = "EN - Inception (2010)"
        )
        val arabicFhd = com.example.data.model.VodGroupingNormalizer.scoreVersionPreference(
            versionTag = "Arabic • FHD",
            hasPoster = true,
            containerExt = "mp4",
            ratingNumeric = 8.5,
            rawName = "AR - Inception (2010)"
        )
        val frenchFhd = com.example.data.model.VodGroupingNormalizer.scoreVersionPreference(
            versionTag = "French • FHD",
            hasPoster = true,
            containerExt = "mp4",
            ratingNumeric = 8.5,
            rawName = "FR - Inception (2010)"
        )
        val french4kMkv = com.example.data.model.VodGroupingNormalizer.scoreVersionPreference(
            versionTag = "French • 4K",
            hasPoster = true,
            containerExt = "mkv",
            ratingNumeric = 8.5,
            rawName = "FR - Inception (2010) 4K"
        )
        val multiSubFhd = com.example.data.model.VodGroupingNormalizer.scoreVersionPreference(
            versionTag = "Multi-Sub • FHD",
            hasPoster = true,
            containerExt = "mp4",
            ratingNumeric = 8.5,
            rawName = "MULTI - Inception (2010)"
        )
        val unmarkedLatin = com.example.data.model.VodGroupingNormalizer.scoreVersionPreference(
            versionTag = "FHD",
            hasPoster = true,
            containerExt = "mp4",
            ratingNumeric = 8.5,
            rawName = "Inception (2010)"
        )
        val italianFhd = com.example.data.model.VodGroupingNormalizer.scoreVersionPreference(
            versionTag = "Italian • FHD",
            hasPoster = true,
            containerExt = "mp4",
            ratingNumeric = 8.5,
            rawName = "IT - Inception (2010)"
        )
        val hindiFhd = com.example.data.model.VodGroupingNormalizer.scoreVersionPreference(
            versionTag = "Hindi • FHD",
            hasPoster = true,
            containerExt = "mp4",
            ratingNumeric = 8.5,
            rawName = "HI - Inception (2010)"
        )
        val turkishFhd = com.example.data.model.VodGroupingNormalizer.scoreVersionPreference(
            versionTag = "Turkish • FHD",
            hasPoster = true,
            containerExt = "mp4",
            ratingNumeric = 8.5,
            rawName = "TR - Inception (2010)"
        )

        assertTrue("English ($englishFhd) must beat Arabic ($arabicFhd)", englishFhd > arabicFhd)
        assertTrue("Arabic ($arabicFhd) must beat French ($frenchFhd)", arabicFhd > frenchFhd)
        assertTrue("French ($frenchFhd) must beat Multi-Sub ($multiSubFhd)", frenchFhd > multiSubFhd)
        assertTrue("Multi-Sub ($multiSubFhd) must beat unmarked Latin ASCII ($unmarkedLatin)", multiSubFhd > unmarkedLatin)
        assertTrue("Unmarked Latin ASCII ($unmarkedLatin) must beat Italian ($italianFhd)", unmarkedLatin > italianFhd)
        assertTrue("Unmarked Latin ASCII ($unmarkedLatin) must beat Hindi ($hindiFhd)", unmarkedLatin > hindiFhd)
        assertTrue("Unmarked Latin ASCII ($unmarkedLatin) must beat Turkish ($turkishFhd)", unmarkedLatin > turkishFhd)
        assertTrue(
            "French 4K ($french4kMkv) must NEVER beat English ($englishFhd)",
            englishFhd > french4kMkv
        )
    }

    @Test
    fun v17Phase7b_extractTmdbPosterHashZeroRegexMatchesSizesAndRejectsNonTmdb() {
        assertEquals(
            "9gk7adHYeLzLq5T6O7H4N8P9Q1R",
            com.example.data.model.VodGroupingNormalizer.extractTmdbPosterHash("https://image.tmdb.org/t/p/w500/9gk7adHYeLzLq5T6O7H4N8P9Q1R.jpg")
        )
        assertEquals(
            "9gk7adHYeLzLq5T6O7H4N8P9Q1R",
            com.example.data.model.VodGroupingNormalizer.extractTmdbPosterHash("https://image.tmdb.org/t/p/w780/9gk7adHYeLzLq5T6O7H4N8P9Q1R.jpg?v=2")
        )
        assertEquals(
            "9gk7adHYeLzLq5T6O7H4N8P9Q1R",
            com.example.data.model.VodGroupingNormalizer.extractTmdbPosterHash("https://IMAGE.TMDB.ORG/t/p/original/9gk7adHYeLzLq5T6O7H4N8P9Q1R.webp#hero")
        )
        assertEquals(
            "aBcDeFgHiJkL1234",
            com.example.data.model.VodGroupingNormalizer.extractTmdbPosterHash("http://image.tmdb.org/t/p/w342/aBcDeFgHiJkL1234.png")
        )

        // Reject non-TMDb URLs, blank strings, and short stems (< 12 chars)
        assertEquals("", com.example.data.model.VodGroupingNormalizer.extractTmdbPosterHash(""))
        assertEquals("", com.example.data.model.VodGroupingNormalizer.extractTmdbPosterHash("https://cdn.example.com/posters/9gk7adHYeLzLq5T6O7H4N8P9Q1R.jpg"))
        assertEquals("", com.example.data.model.VodGroupingNormalizer.extractTmdbPosterHash("https://image.tmdb.org/t/p/w500/short123.jpg"))
        assertEquals("", com.example.data.model.VodGroupingNormalizer.extractTmdbPosterHash("https://image.tmdb.org/t/p/"))
    }

    @Test
    fun v17Phase7b_buildLosslessGroupsEmitsMembersOnlyForMultiVersionAndResolvesSingletons() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, StreamVaultDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repository = IptvRepository(
                playlistDao = db.playlistDao(),
                categoryDao = db.categoryDao(),
                streamDao = db.streamDao(),
                epgDao = db.epgDao(),
                parentalDao = db.parentalSettingsDao(),
                vodGroupDao = db.vodGroupDao(),
                database = db
            )
            val playlistId = repository.addOrUpdatePlaylist(
                PlaylistEntity(
                    name = "V17 Grouping Vault",
                    serverUrl = "http://vod.example.com:8080",
                    username = "u",
                    password = "p",
                    isActive = true
                )
            )
            db.categoryDao().insertCategories(
                listOf(
                    CategoryEntity(playlistId = playlistId, categoryId = "cat_en", categoryName = "EN | Cinema", type = "MOVIE"),
                    CategoryEntity(playlistId = playlistId, categoryId = "cat_fr", categoryName = "FR | Cinema 4K", type = "MOVIE")
                )
            )

            val multiEn = StreamEntity(
                playlistId = playlistId,
                streamId = 71001L,
                name = "EN - Interstellar (2014) 1080p",
                streamType = "movie",
                streamIcon = "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
                categoryId = "cat_en",
                containerExtension = "mp4",
                tmdbId = "157336",
                releaseYear = 2014,
                ratingNumeric = 8.6,
                versionTag = "English • 1080p",
                canonicalGroupKey = "movie_tmdb_157336"
            )
            val multiFr = StreamEntity(
                playlistId = playlistId,
                streamId = 71002L,
                name = "FR - Interstellar (2014) 4K",
                streamType = "movie",
                streamIcon = "https://image.tmdb.org/t/p/w780/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
                categoryId = "cat_fr",
                containerExtension = "mkv",
                tmdbId = "157336",
                releaseYear = 2014,
                ratingNumeric = 8.6,
                versionTag = "French • 4K",
                canonicalGroupKey = "movie_tmdb_157336"
            )
            val singletonMovie = StreamEntity(
                playlistId = playlistId,
                streamId = 71003L,
                name = "EN - Whiplash (2014) 1080p",
                streamType = "movie",
                streamIcon = "https://image.tmdb.org/t/p/w500/7fn624j5lj3xTme2SgiLCeuedmO.jpg",
                categoryId = "cat_en",
                containerExtension = "mp4",
                tmdbId = "244786",
                releaseYear = 2014,
                ratingNumeric = 8.5,
                versionTag = "English • 1080p",
                canonicalGroupKey = "movie_tmdb_244786"
            )

            db.streamDao().insertStreamsBatch(listOf(multiEn, multiFr, singletonMovie))
            val insertedProjections = db.streamDao().getStreamIndexProjectionsAfterId(
                playlistId = playlistId,
                streamType = "movie",
                lastId = 0L,
                limit = 50
            )
            assertEquals(3, insertedProjections.size)

            val grouping = com.example.data.sync.CatalogDiffEngine.buildLosslessGroups(
                playlistId = playlistId,
                streamType = "movie",
                streams = insertedProjections,
                categoryNameMap = mapOf("cat_en" to "EN | Cinema", "cat_fr" to "FR | Cinema 4K")
            )

            assertEquals("Expected 2 groups (1 multi-version + 1 singleton)", 2, grouping.groups.size)
            val multiGroup = grouping.groups.first { it.versionCount == 2 }
            val singletonGroup = grouping.groups.first { it.versionCount == 1 }

            // vod_group_members MUST ONLY contain multi-version group rows (zero rows for singletons)
            assertEquals(2, grouping.members.size)
            assertTrue(grouping.members.all { it.groupKey == multiGroup.groupKey })
            assertTrue(grouping.members.none { it.groupKey == singletonGroup.groupKey })

            // Persist groups and members and verify lossless resolution via getVodVersionsForGroup
            db.vodGroupDao().insertGroupsBatch(grouping.groups)
            db.vodGroupDao().insertGroupMembersBatch(grouping.members)

            val persistedStreams = db.streamDao().getStreamsInitial(playlistId, "movie", limit = 10).first()
            val persistedMultiFr = persistedStreams.first { it.streamId == 71002L }
            val persistedSingleton = persistedStreams.first { it.streamId == 71003L }

            val resolvedMultiVersions = repository.getVodVersionsForGroup(
                playlistId = playlistId,
                streamType = "movie",
                canonicalGroupKey = multiGroup.groupKey,
                fallbackStream = persistedMultiFr
            )
            assertEquals(2, resolvedMultiVersions.size)
            assertEquals(
                "English version must rank first ahead of French 4K",
                71001L,
                resolvedMultiVersions.first().streamId
            )

            val resolvedSingletonVersions = repository.getVodVersionsForGroup(
                playlistId = playlistId,
                streamType = "movie",
                canonicalGroupKey = singletonGroup.groupKey,
                fallbackStream = persistedSingleton
            )
            assertEquals(1, resolvedSingletonVersions.size)
            assertEquals(71003L, resolvedSingletonVersions.first().streamId)
        } finally {
            db.close()
        }
    }

    @Test
    fun v17Phase7b_keysetCursorQueryIteratesDeterministicallyWithoutDuplicatesOrSkips() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, StreamVaultDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val playlistId = db.playlistDao().insertPlaylist(
                PlaylistEntity(
                    name = "Keyset Cursor Vault",
                    serverUrl = "http://vod.example.com:8080",
                    username = "u",
                    password = "p",
                    isActive = true
                )
            )
            val movieEntities = (1..25).map { idx ->
                StreamEntity(
                    playlistId = playlistId,
                    streamId = 80000L + idx,
                    name = "Movie $idx (2024)",
                    streamType = "movie",
                    categoryId = "cat_movies"
                )
            }
            val liveNoise = (1..3).map { idx ->
                StreamEntity(
                    playlistId = playlistId,
                    streamId = 90000L + idx,
                    name = "Live Channel $idx",
                    streamType = "live",
                    categoryId = "cat_live"
                )
            }
            db.streamDao().insertStreamsBatch(movieEntities + liveNoise)

            val collectedIds = mutableListOf<Long>()
            val collectedStreamIds = mutableListOf<Long>()
            val pageSizes = mutableListOf<Int>()
            var lastId = 0L
            while (true) {
                val page = db.streamDao().getStreamIndexProjectionsAfterId(
                    playlistId = playlistId,
                    streamType = "movie",
                    lastId = lastId,
                    limit = 6
                )
                if (page.isEmpty()) break
                pageSizes.add(page.size)
                for (row in page) {
                    assertTrue("Each row id (${row.id}) must strictly advance past lastId ($lastId)", row.id > lastId)
                    collectedIds.add(row.id)
                    collectedStreamIds.add(row.streamId)
                    lastId = row.id
                }
            }

            assertEquals(listOf(6, 6, 6, 6, 1), pageSizes)
            assertEquals(25, collectedIds.size)
            assertEquals("Zero duplicate DB IDs", 25, collectedIds.toSet().size)
            assertEquals("Strictly ascending DB ID order", collectedIds.sorted(), collectedIds)
            assertEquals((80001L..80025L).toSet(), collectedStreamIds.toSet())
        } finally {
            db.close()
        }
    }

    @Test
    fun v17Phase7b_globalCollectionsBuiltinSeedResolvesIconicFranchisesOfflineInReleaseOrder() {
        try {
            // Clear any cached cloud snapshot to simulate 100% offline / fresh install state
            com.example.data.remote.GlobalCollectionsIndexManager.clearForTest()

            // 1. John Wick 1..4 in official release order
            val jw1 = com.example.data.remote.GlobalCollectionsIndexManager.lookupCollection("245891", "John Wick", 2014)
            val jw2 = com.example.data.remote.GlobalCollectionsIndexManager.lookupCollection("324552", "John Wick: Chapter 2", 2017)
            val jw3 = com.example.data.remote.GlobalCollectionsIndexManager.lookupCollection("458156", "John Wick: Chapter 3 - Parabellum", 2019)
            val jw4 = com.example.data.remote.GlobalCollectionsIndexManager.lookupCollection("603692", "John Wick: Chapter 4", 2023)
            assertNotNull(jw1)
            assertNotNull(jw2)
            assertNotNull(jw3)
            assertNotNull(jw4)
            assertEquals("404609", jw1!!.collectionId)
            assertEquals(listOf(1, 2, 3, 4), listOf(jw1.releaseOrder, jw2!!.releaseOrder, jw3!!.releaseOrder, jw4!!.releaseOrder))

            // Title + year fallback when IPTV stream lacks TMDb ID
            val jw2ByTitle = com.example.data.remote.GlobalCollectionsIndexManager.lookupCollection("", "John Wick: Chapter 2", 2017)
            assertNotNull(jw2ByTitle)
            assertEquals("404609", jw2ByTitle!!.collectionId)
            assertEquals(2, jw2ByTitle.releaseOrder)

            // 2. Mission: Impossible installments in official release order
            val mi1 = com.example.data.remote.GlobalCollectionsIndexManager.lookupCollection("954", "Mission: Impossible", 1996)
            val mi4 = com.example.data.remote.GlobalCollectionsIndexManager.lookupCollection("56292", "Mission: Impossible - Ghost Protocol", 2011)
            val mi6 = com.example.data.remote.GlobalCollectionsIndexManager.lookupCollection("353081", "Mission: Impossible - Fallout", 2018)
            val mi7 = com.example.data.remote.GlobalCollectionsIndexManager.lookupCollection("575264", "Mission: Impossible - Dead Reckoning Part One", 2023)
            assertNotNull(mi1)
            assertNotNull(mi4)
            assertNotNull(mi6)
            assertNotNull(mi7)
            assertEquals("87359", mi1!!.collectionId)
            assertEquals(listOf(1, 4, 6, 7), listOf(mi1.releaseOrder, mi4!!.releaseOrder, mi6!!.releaseOrder, mi7!!.releaseOrder))

            // 3. James Bond installments in official release order
            val jb1 = com.example.data.remote.GlobalCollectionsIndexManager.lookupCollection("646", "Dr. No", 1962)
            val jb21 = com.example.data.remote.GlobalCollectionsIndexManager.lookupCollection("36557", "Casino Royale", 2006)
            val jb23 = com.example.data.remote.GlobalCollectionsIndexManager.lookupCollection("37724", "Skyfall", 2012)
            val jb25 = com.example.data.remote.GlobalCollectionsIndexManager.lookupCollection("370172", "No Time to Die", 2021)
            assertNotNull(jb1)
            assertNotNull(jb21)
            assertNotNull(jb23)
            assertNotNull(jb25)
            assertEquals("645", jb1!!.collectionId)
            assertEquals(listOf(1, 21, 23, 25), listOf(jb1.releaseOrder, jb21!!.releaseOrder, jb23!!.releaseOrder, jb25!!.releaseOrder))

            // 4. Standalone movie must NOT falsely match any built-in franchise
            assertNull(com.example.data.remote.GlobalCollectionsIndexManager.lookupCollection("27205", "Inception", 2010))
        } finally {
            com.example.data.remote.GlobalCollectionsIndexManager.clearForTest()
        }
    }

    @Test
    fun v17Phase7b_catalogSyncSchedulerUserInitiatedBypassesBackoffAndPreservesSingleLane() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = StreamVaultDatabase.getInstance(context)
        val playlistId = 99441L
        try {
            com.example.data.sync.CatalogSyncScheduler.clearForTest()

            // 1. Idle queue (no active worker): userInitiated=true on an already-pending MOVIE job
            // upgrades metadata in-place (forceFull=true) and triggers REPLACE policy without duplicating queue rows
            com.example.data.sync.CatalogSyncScheduler.enqueueSectionSync(
                context = context,
                playlistId = playlistId,
                sectionType = "MOVIE",
                forceFull = false,
                userInitiated = false
            )
            com.example.data.sync.CatalogSyncScheduler.enqueueSectionSync(
                context = context,
                playlistId = playlistId,
                sectionType = "MOVIE",
                forceFull = true,
                expedited = true,
                userInitiated = true
            )

            assertTrue(com.example.data.sync.CatalogSyncScheduler.isSectionQueuedOrRunning(playlistId, "MOVIE"))
            val poppedMovie = com.example.data.sync.CatalogSyncScheduler.pollNextPendingRequest(context, null)
            assertNotNull(poppedMovie)
            assertEquals("MOVIE", poppedMovie!!.sectionType)
            assertTrue("userInitiated re-enqueue must upgrade forceFull in-place", poppedMovie.forceFull)
            assertFalse("Single pending MOVIE entry must not be duplicated", com.example.data.sync.CatalogSyncScheduler.hasPendingRequests())

            // 2. While MOVIE is actively running, userInitiated=true for SERIES MUST NOT interrupt MOVIE
            // and MUST queue cleanly in strict single-lane serialization
            com.example.data.sync.CatalogSyncScheduler.enqueueSectionSync(
                context = context,
                playlistId = playlistId,
                sectionType = "MOVIE",
                forceFull = true,
                userInitiated = true
            )
            assertFalse("Duplicate enqueue for currently active MOVIE must be ignored", com.example.data.sync.CatalogSyncScheduler.hasPendingRequests())

            com.example.data.sync.CatalogSyncScheduler.enqueueSectionSync(
                context = context,
                playlistId = playlistId,
                sectionType = "SERIES",
                forceFull = false,
                expedited = true,
                userInitiated = true
            )
            assertTrue(com.example.data.sync.CatalogSyncScheduler.isSectionQueuedOrRunning(playlistId, "MOVIE"))
            assertTrue(com.example.data.sync.CatalogSyncScheduler.isSectionQueuedOrRunning(playlistId, "SERIES"))
            assertTrue(com.example.data.sync.CatalogSyncScheduler.hasPendingRequests())

            // Finish MOVIE -> SERIES drains next in strict single-lane order
            com.example.data.sync.CatalogSyncScheduler.markRequestFinished(playlistId, "MOVIE")
            val poppedSeries = com.example.data.sync.CatalogSyncScheduler.pollNextPendingRequest(context, null)
            assertNotNull(poppedSeries)
            assertEquals("SERIES", poppedSeries!!.sectionType)
            com.example.data.sync.CatalogSyncScheduler.markRequestFinished(playlistId, "SERIES")
            assertFalse(com.example.data.sync.CatalogSyncScheduler.hasPendingRequests())
        } finally {
            com.example.data.sync.CatalogSyncScheduler.clearForTest()
            db.openHelper.writableDatabase.execSQL(
                "DELETE FROM sync_section_state WHERE playlistId = ?",
                arrayOf(playlistId)
            )
        }
    }

    @Test
    fun v18Phase8b_userReproInterruptedMovieResumePreemptsWaitingSeriesAndCompletesBoth() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = StreamVaultDatabase.getInstance(context)
        val dao = db.syncSectionStateDao()
        val playlistId = 98101L
        try {
            com.example.data.sync.CatalogTaskOrchestrator.clearForTest(context)
            db.openHelper.writableDatabase.execSQL("DELETE FROM sync_section_state WHERE playlistId = ?", arrayOf(playlistId))

            // 1. Account onboarding enqueues LIVE -> SERIES -> MOVIE at P3 (INITIAL_ACCOUNT_SYNC)
            com.example.data.sync.CatalogTaskOrchestrator.enqueueAccountSync(context, playlistId)
            val pendingInitial = dao.getPendingTasksForPlaylist(playlistId)
            assertEquals(listOf("LIVE", "SERIES", "MOVIE"), pendingInitial.map { it.sectionType })

            // 2. Worker claims and completes LIVE
            val claimedLive = com.example.data.sync.CatalogTaskOrchestrator.claimNextTask(context, "worker-a")
            assertNotNull(claimedLive)
            assertEquals("LIVE", claimedLive!!.sectionType)
            com.example.data.sync.CatalogTaskOrchestrator.markTaskTerminal(
                context = context,
                playlistId = playlistId,
                sectionType = "LIVE",
                workerId = "worker-a",
                terminalStatus = com.example.data.sync.SyncStatus.COMPLETED
            )

            // 3. Simulate MOVIE interrupted while SERIES is still PENDING at P3
            val movieRow = dao.getSectionOnce(playlistId, "MOVIE")!!
            dao.upsert(
                movieRow.copy(
                    status = com.example.data.sync.SyncStatus.INTERRUPTED.name,
                    ownerWorkerId = "",
                    heartbeatMs = 0L,
                    queuePriority = 99
                )
            )
            assertEquals("PENDING", dao.getSectionOnce(playlistId, "SERIES")!!.status)

            // 4. User taps Resume on MOVIE -> must receive P0 (USER_RESUME) and preempt waiting SERIES
            val resumed = com.example.data.sync.CatalogTaskOrchestrator.resumeSection(context, playlistId, "MOVIE")
            assertTrue("resumeSection(MOVIE) must succeed", resumed)
            val resumedMovieRow = dao.getSectionOnce(playlistId, "MOVIE")!!
            assertEquals("PENDING", resumedMovieRow.status)
            assertEquals(com.example.data.sync.TaskPriority.USER_RESUME.rank, resumedMovieRow.queuePriority)

            // 5. Next claim MUST return MOVIE ahead of waiting SERIES
            val claimedMovie = com.example.data.sync.CatalogTaskOrchestrator.claimNextTask(context, "worker-b")
            assertNotNull(claimedMovie)
            assertEquals("MOVIE", claimedMovie!!.sectionType)
            assertEquals("RUNNING", claimedMovie.status)
            assertEquals("worker-b", claimedMovie.ownerWorkerId)
            assertTrue(claimedMovie.heartbeatMs > 0L)

            // Complete MOVIE -> SERIES runs cleanly right after
            com.example.data.sync.CatalogTaskOrchestrator.markTaskTerminal(
                context = context,
                playlistId = playlistId,
                sectionType = "MOVIE",
                workerId = "worker-b",
                terminalStatus = com.example.data.sync.SyncStatus.COMPLETED
            )
            val claimedSeries = com.example.data.sync.CatalogTaskOrchestrator.claimNextTask(context, "worker-b")
            assertNotNull("SERIES must claim right after MOVIE completes", claimedSeries)
            assertEquals("SERIES", claimedSeries!!.sectionType)

            com.example.data.sync.CatalogTaskOrchestrator.markTaskTerminal(
                context = context,
                playlistId = playlistId,
                sectionType = "SERIES",
                workerId = "worker-b",
                terminalStatus = com.example.data.sync.SyncStatus.COMPLETED
            )
            assertNull(com.example.data.sync.CatalogTaskOrchestrator.claimNextTask(context, "worker-b"))
            assertFalse(com.example.data.sync.CatalogTaskOrchestrator.hasActiveOrPendingTasks(context))
        } finally {
            com.example.data.sync.CatalogTaskOrchestrator.clearForTest(context)
            db.openHelper.writableDatabase.execSQL("DELETE FROM sync_section_state WHERE playlistId = ?", arrayOf(playlistId))
        }
    }

    @Test
    fun v18Phase8b_strictFsmRejectsRunningToPendingAndAllowsValidLifecycle() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = StreamVaultDatabase.getInstance(context)
        val dao = db.syncSectionStateDao()
        val playlistId = 98102L
        try {
            // 1. Direct FSM transition table assertions
            assertFalse(
                "RUNNING -> PENDING must be strictly forbidden",
                com.example.data.sync.CatalogTaskStateMachine.canTransition(
                    com.example.data.sync.SyncStatus.RUNNING,
                    com.example.data.sync.SyncStatus.PENDING
                )
            )
            assertFalse(
                com.example.data.sync.CatalogTaskStateMachine.requireValidTransition(
                    com.example.data.sync.SyncStatus.RUNNING,
                    com.example.data.sync.SyncStatus.PENDING,
                    "test-illegal"
                )
            )
            assertFalse(
                com.example.data.sync.CatalogTaskStateMachine.canTransition(
                    com.example.data.sync.SyncStatus.IDLE,
                    com.example.data.sync.SyncStatus.RUNNING
                )
            )
            assertFalse(
                com.example.data.sync.CatalogTaskStateMachine.canTransition(
                    com.example.data.sync.SyncStatus.COMPLETED,
                    com.example.data.sync.SyncStatus.RUNNING
                )
            )

            // Valid full lifecycle: IDLE -> PENDING -> RUNNING -> INTERRUPTED -> PENDING -> RUNNING -> COMPLETED
            assertTrue(com.example.data.sync.CatalogTaskStateMachine.canTransition(com.example.data.sync.SyncStatus.IDLE, com.example.data.sync.SyncStatus.PENDING))
            assertTrue(com.example.data.sync.CatalogTaskStateMachine.canTransition(com.example.data.sync.SyncStatus.PENDING, com.example.data.sync.SyncStatus.RUNNING))
            assertTrue(com.example.data.sync.CatalogTaskStateMachine.canTransition(com.example.data.sync.SyncStatus.RUNNING, com.example.data.sync.SyncStatus.INTERRUPTED))
            assertTrue(com.example.data.sync.CatalogTaskStateMachine.canTransition(com.example.data.sync.SyncStatus.INTERRUPTED, com.example.data.sync.SyncStatus.PENDING))
            assertTrue(com.example.data.sync.CatalogTaskStateMachine.canTransition(com.example.data.sync.SyncStatus.PENDING, com.example.data.sync.SyncStatus.RUNNING))
            assertTrue(com.example.data.sync.CatalogTaskStateMachine.canTransition(com.example.data.sync.SyncStatus.RUNNING, com.example.data.sync.SyncStatus.COMPLETED))

            // 2. End-to-end Room verification that enqueuing a RUNNING section never overwrites it to PENDING
            com.example.data.sync.CatalogTaskOrchestrator.clearForTest(context)
            db.openHelper.writableDatabase.execSQL("DELETE FROM sync_section_state WHERE playlistId = ?", arrayOf(playlistId))

            assertTrue(
                com.example.data.sync.CatalogTaskOrchestrator.enqueueSection(
                    context = context,
                    playlistId = playlistId,
                    sectionType = "MOVIE",
                    priority = com.example.data.sync.TaskPriority.USER_SECTION_SYNC
                )
            )
            val claimed = com.example.data.sync.CatalogTaskOrchestrator.claimNextTask(context, "worker-fsm")
            assertNotNull(claimed)
            assertEquals("RUNNING", dao.getSectionOnce(playlistId, "MOVIE")!!.status)

            val duplicateEnqueue = com.example.data.sync.CatalogTaskOrchestrator.enqueueSection(
                context = context,
                playlistId = playlistId,
                sectionType = "MOVIE",
                priority = com.example.data.sync.TaskPriority.USER_RESUME
            )
            assertFalse("Enqueuing an already-RUNNING section must be an idempotent no-op", duplicateEnqueue)
            assertEquals("RUNNING", dao.getSectionOnce(playlistId, "MOVIE")!!.status)

            // Transition RUNNING -> INTERRUPTED -> PENDING -> RUNNING -> COMPLETED
            com.example.data.sync.CatalogTaskOrchestrator.markTaskTerminal(
                context, playlistId, "MOVIE", "worker-fsm", com.example.data.sync.SyncStatus.INTERRUPTED
            )
            assertEquals("INTERRUPTED", dao.getSectionOnce(playlistId, "MOVIE")!!.status)
            assertTrue(com.example.data.sync.CatalogTaskOrchestrator.resumeSection(context, playlistId, "MOVIE"))
            assertEquals("PENDING", dao.getSectionOnce(playlistId, "MOVIE")!!.status)
            assertNotNull(com.example.data.sync.CatalogTaskOrchestrator.claimNextTask(context, "worker-fsm-2"))
            assertEquals("RUNNING", dao.getSectionOnce(playlistId, "MOVIE")!!.status)
            com.example.data.sync.CatalogTaskOrchestrator.markTaskTerminal(
                context, playlistId, "MOVIE", "worker-fsm-2", com.example.data.sync.SyncStatus.COMPLETED
            )
            assertEquals("COMPLETED", dao.getSectionOnce(playlistId, "MOVIE")!!.status)
        } finally {
            com.example.data.sync.CatalogTaskOrchestrator.clearForTest(context)
            db.openHelper.writableDatabase.execSQL("DELETE FROM sync_section_state WHERE playlistId = ?", arrayOf(playlistId))
        }
    }

    @Test
    fun v18Phase8b_repeatedResumeAndEnqueueIdempotencyNeverDuplicatesOrCorruptsRunning() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = StreamVaultDatabase.getInstance(context)
        val dao = db.syncSectionStateDao()
        val playlistId = 98103L
        try {
            com.example.data.sync.CatalogTaskOrchestrator.clearForTest(context)
            db.openHelper.writableDatabase.execSQL("DELETE FROM sync_section_state WHERE playlistId = ?", arrayOf(playlistId))

            dao.upsert(
                com.example.data.model.SyncSectionStateEntity(
                    playlistId = playlistId,
                    sectionType = "MOVIE",
                    status = com.example.data.sync.SyncStatus.INTERRUPTED.name,
                    stage = "DOWNLOADING_AND_DIFFING",
                    progressPercent = 40
                )
            )

            // Rapidly tap Resume 5 times -> must produce exactly 1 PENDING row at P0
            for (attempt in 1..5) {
                com.example.data.sync.CatalogTaskOrchestrator.resumeSection(context, playlistId, "MOVIE")
            }
            val pendingAfterBursts = dao.getPendingTasksForPlaylist(playlistId)
            assertEquals("5 rapid resume taps must yield exactly 1 PENDING row", 1, pendingAfterBursts.size)
            assertEquals(com.example.data.sync.TaskPriority.USER_RESUME.rank, pendingAfterBursts.first().queuePriority)

            // Claim task -> now RUNNING
            val claimed = com.example.data.sync.CatalogTaskOrchestrator.claimNextTask(context, "worker-idempotent")
            assertNotNull(claimed)
            val originalHeartbeat = claimed!!.heartbeatMs

            // Fire 5 more rapid resume/enqueue bursts while RUNNING -> zero duplicates, lease untouched
            for (attempt in 1..5) {
                assertFalse(com.example.data.sync.CatalogTaskOrchestrator.resumeSection(context, playlistId, "MOVIE"))
                assertFalse(
                    com.example.data.sync.CatalogTaskOrchestrator.enqueueSection(
                        context = context,
                        playlistId = playlistId,
                        sectionType = "MOVIE",
                        priority = com.example.data.sync.TaskPriority.USER_SECTION_SYNC,
                        forceFull = true
                    )
                )
            }
            assertEquals(0, dao.getPendingTasksForPlaylist(playlistId).size)
            val activeRow = dao.getSectionOnce(playlistId, "MOVIE")!!
            assertEquals("RUNNING", activeRow.status)
            assertEquals("worker-idempotent", activeRow.ownerWorkerId)
            assertEquals(originalHeartbeat, activeRow.heartbeatMs)
        } finally {
            com.example.data.sync.CatalogTaskOrchestrator.clearForTest(context)
            db.openHelper.writableDatabase.execSQL("DELETE FROM sync_section_state WHERE playlistId = ?", arrayOf(playlistId))
        }
    }

    @Test
    fun v18Phase8b_staleLeaseWatchdogRecoversDeadWorkerAfter45sAndUnblocksQueue() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = StreamVaultDatabase.getInstance(context)
        val dao = db.syncSectionStateDao()
        val playlistId = 98104L
        try {
            com.example.data.sync.CatalogTaskOrchestrator.clearForTest(context)
            db.openHelper.writableDatabase.execSQL("DELETE FROM sync_section_state WHERE playlistId = ?", arrayOf(playlistId))

            val now = System.currentTimeMillis()
            // 1. Insert a dead RUNNING worker lease older than 45s (60s old) + a waiting PENDING SERIES task
            dao.upsert(
                com.example.data.model.SyncSectionStateEntity(
                    playlistId = playlistId,
                    sectionType = "MOVIE",
                    status = com.example.data.sync.SyncStatus.RUNNING.name,
                    stage = "DOWNLOADING_AND_DIFFING",
                    ownerWorkerId = "dead-worker-1",
                    heartbeatMs = now - 60_000L
                )
            )
            dao.upsert(
                com.example.data.model.SyncSectionStateEntity(
                    playlistId = playlistId,
                    sectionType = "SERIES",
                    status = com.example.data.sync.SyncStatus.PENDING.name,
                    stage = "PENDING",
                    queuePriority = com.example.data.sync.TaskPriority.INITIAL_ACCOUNT_SYNC.rank,
                    queueSequence = now
                )
            )

            // 2. Claiming next task triggers 45s stale lease recovery and unblocks SERIES
            val claimed = com.example.data.sync.CatalogTaskOrchestrator.claimNextTask(context, "live-worker-2")
            assertNotNull("Stale RUNNING lease must be recovered so waiting SERIES can claim", claimed)
            assertEquals("SERIES", claimed!!.sectionType)
            assertEquals("live-worker-2", claimed.ownerWorkerId)

            val recoveredMovie = dao.getSectionOnce(playlistId, "MOVIE")!!
            assertEquals("INTERRUPTED", recoveredMovie.status)
            assertEquals("", recoveredMovie.ownerWorkerId)
            assertTrue(recoveredMovie.errorMessage.contains("stale worker lease recovered", ignoreCase = true))

            // 3. Conversely, a fresh RUNNING lease (SERIES just claimed) MUST block another worker
            dao.upsert(
                com.example.data.model.SyncSectionStateEntity(
                    playlistId = playlistId,
                    sectionType = "LIVE",
                    status = com.example.data.sync.SyncStatus.PENDING.name,
                    stage = "PENDING",
                    queuePriority = com.example.data.sync.TaskPriority.USER_SECTION_SYNC.rank,
                    queueSequence = now + 10L
                )
            )
            val blockedClaim = com.example.data.sync.CatalogTaskOrchestrator.claimNextTask(context, "competing-worker-3")
            assertNull("Fresh RUNNING lease must block competing workers from claiming another task", blockedClaim)
        } finally {
            com.example.data.sync.CatalogTaskOrchestrator.clearForTest(context)
            db.openHelper.writableDatabase.execSQL("DELETE FROM sync_section_state WHERE playlistId = ?", arrayOf(playlistId))
        }
    }

    @Test
    fun v18Phase8b_failureIsolationAllowsWaitingSeriesToClaimAfterMovieFails() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = StreamVaultDatabase.getInstance(context)
        val dao = db.syncSectionStateDao()
        val playlistId = 98105L
        try {
            com.example.data.sync.CatalogTaskOrchestrator.clearForTest(context)
            db.openHelper.writableDatabase.execSQL("DELETE FROM sync_section_state WHERE playlistId = ?", arrayOf(playlistId))

            com.example.data.sync.CatalogTaskOrchestrator.enqueueSection(
                context = context,
                playlistId = playlistId,
                sectionType = "MOVIE",
                priority = com.example.data.sync.TaskPriority.USER_SECTION_SYNC
            )
            com.example.data.sync.CatalogTaskOrchestrator.enqueueSection(
                context = context,
                playlistId = playlistId,
                sectionType = "SERIES",
                priority = com.example.data.sync.TaskPriority.INITIAL_ACCOUNT_SYNC
            )

            // Claim MOVIE and fail it
            val claimedMovie = com.example.data.sync.CatalogTaskOrchestrator.claimNextTask(context, "worker-iso")
            assertNotNull(claimedMovie)
            assertEquals("MOVIE", claimedMovie!!.sectionType)
            com.example.data.sync.CatalogTaskOrchestrator.markTaskTerminal(
                context = context,
                playlistId = playlistId,
                sectionType = "MOVIE",
                workerId = "worker-iso",
                terminalStatus = com.example.data.sync.SyncStatus.FAILED,
                errorMessage = "HTTP 403 Forbidden"
            )

            val failedMovie = dao.getSectionOnce(playlistId, "MOVIE")!!
            assertEquals("FAILED", failedMovie.status)
            assertEquals("", failedMovie.ownerWorkerId)

            // Waiting SERIES must still be claimable immediately
            val claimedSeries = com.example.data.sync.CatalogTaskOrchestrator.claimNextTask(context, "worker-iso")
            assertNotNull("SERIES must never be stranded when MOVIE fails", claimedSeries)
            assertEquals("SERIES", claimedSeries!!.sectionType)
            assertEquals("RUNNING", claimedSeries.status)
        } finally {
            com.example.data.sync.CatalogTaskOrchestrator.clearForTest(context)
            db.openHelper.writableDatabase.execSQL("DELETE FROM sync_section_state WHERE playlistId = ?", arrayOf(playlistId))
        }
    }

    @Test
    fun v18Phase8b_accountSwitchCancelsPreviousPlaylistTasksBeforeEnqueueingNewAccount() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = StreamVaultDatabase.getInstance(context)
        val dao = db.syncSectionStateDao()
        val playlist1 = 98106L
        val playlist2 = 98107L
        try {
            com.example.data.sync.CatalogTaskOrchestrator.clearForTest(context)
            db.openHelper.writableDatabase.execSQL("DELETE FROM sync_section_state WHERE playlistId IN (?, ?)", arrayOf(playlist1, playlist2))

            // 1. Playlist 1 has LIVE RUNNING and SERIES/MOVIE PENDING
            com.example.data.sync.CatalogTaskOrchestrator.enqueueAccountSync(context, playlist1)
            val p1Live = com.example.data.sync.CatalogTaskOrchestrator.claimNextTask(context, "worker-p1")
            assertNotNull(p1Live)
            assertEquals(playlist1, p1Live!!.playlistId)
            assertEquals("LIVE", p1Live.sectionType)

            // 2. Account switch cancels Playlist 1 cleanly before enqueueing Playlist 2
            com.example.data.sync.CatalogTaskOrchestrator.cancelPlaylistTasks(context, playlist1)
            com.example.data.sync.CatalogTaskOrchestrator.enqueueAccountSync(context, playlist2)

            assertEquals("IDLE", dao.getSectionOnce(playlist1, "LIVE")!!.status)
            assertEquals("", dao.getSectionOnce(playlist1, "LIVE")!!.ownerWorkerId)
            assertEquals("IDLE", dao.getSectionOnce(playlist1, "SERIES")!!.status)
            assertEquals("IDLE", dao.getSectionOnce(playlist1, "MOVIE")!!.status)
            assertTrue(dao.getPendingTasksForPlaylist(playlist1).isEmpty())

            // 3. Playlist 2 tasks are queued and claim cleanly
            val p2Pending = dao.getPendingTasksForPlaylist(playlist2)
            assertEquals(listOf("LIVE", "SERIES", "MOVIE"), p2Pending.map { it.sectionType })
            val p2Claimed = com.example.data.sync.CatalogTaskOrchestrator.claimNextTask(context, "worker-p2")
            assertNotNull(p2Claimed)
            assertEquals(playlist2, p2Claimed!!.playlistId)
            assertEquals("LIVE", p2Claimed.sectionType)
        } finally {
            com.example.data.sync.CatalogTaskOrchestrator.clearForTest(context)
            db.openHelper.writableDatabase.execSQL("DELETE FROM sync_section_state WHERE playlistId IN (?, ?)", arrayOf(playlist1, playlist2))
        }
    }

    @Test
    fun v18Phase8b_resumeMovieWhileLiveRunningKeepsLiveRunningAndPromotesMovieAheadOfSeries() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = StreamVaultDatabase.getInstance(context)
        val dao = db.syncSectionStateDao()
        val playlistId = 98108L
        try {
            com.example.data.sync.CatalogTaskOrchestrator.clearForTest(context)
            db.openHelper.writableDatabase.execSQL("DELETE FROM sync_section_state WHERE playlistId = ?", arrayOf(playlistId))

            // 1. Account sync queues LIVE, SERIES, MOVIE; worker starts LIVE
            com.example.data.sync.CatalogTaskOrchestrator.enqueueAccountSync(context, playlistId)
            val claimedLive = com.example.data.sync.CatalogTaskOrchestrator.claimNextTask(context, "worker-live")
            assertNotNull(claimedLive)
            assertEquals("LIVE", claimedLive!!.sectionType)

            // 2. MOVIE was previously INTERRUPTED while SERIES waits at P3
            val movieExisting = dao.getSectionOnce(playlistId, "MOVIE")!!
            dao.upsert(
                movieExisting.copy(
                    status = com.example.data.sync.SyncStatus.INTERRUPTED.name,
                    ownerWorkerId = "",
                    heartbeatMs = 0L,
                    queuePriority = 99
                )
            )

            // 3. User resumes MOVIE while LIVE is actively RUNNING
            assertTrue(com.example.data.sync.CatalogTaskOrchestrator.resumeSection(context, playlistId, "MOVIE"))

            // LIVE must remain undisturbed and single-lane exclusion prevents concurrent claim
            val liveRow = dao.getSectionOnce(playlistId, "LIVE")!!
            assertEquals("RUNNING", liveRow.status)
            assertEquals("worker-live", liveRow.ownerWorkerId)
            assertNull("Single-lane execution must not claim MOVIE while LIVE is RUNNING", com.example.data.sync.CatalogTaskOrchestrator.claimNextTask(context, "worker-other"))

            // MOVIE must be promoted to P0 (USER_RESUME) with queued-waiting summary
            val movieRow = dao.getSectionOnce(playlistId, "MOVIE")!!
            assertEquals("PENDING", movieRow.status)
            assertEquals(com.example.data.sync.TaskPriority.USER_RESUME.rank, movieRow.queuePriority)
            assertEquals(com.example.data.sync.CatalogTaskOrchestrator.STATUS_QUEUED_WAITING, movieRow.pendingUpdateSummary)

            // 4. Once LIVE finishes, MOVIE (P0) claims immediately ahead of P3 SERIES, then SERIES claims next
            com.example.data.sync.CatalogTaskOrchestrator.markTaskTerminal(
                context = context,
                playlistId = playlistId,
                sectionType = "LIVE",
                workerId = "worker-live",
                terminalStatus = com.example.data.sync.SyncStatus.COMPLETED
            )
            val nextAfterLive = com.example.data.sync.CatalogTaskOrchestrator.claimNextTask(context, "worker-live")
            assertNotNull(nextAfterLive)
            assertEquals("MOVIE", nextAfterLive!!.sectionType)
            assertEquals(com.example.data.sync.TaskPriority.USER_RESUME.rank, nextAfterLive.queuePriority)

            com.example.data.sync.CatalogTaskOrchestrator.markTaskTerminal(
                context = context,
                playlistId = playlistId,
                sectionType = "MOVIE",
                workerId = "worker-live",
                terminalStatus = com.example.data.sync.SyncStatus.COMPLETED
            )
            val finalSeries = com.example.data.sync.CatalogTaskOrchestrator.claimNextTask(context, "worker-live")
            assertNotNull(finalSeries)
            assertEquals("SERIES", finalSeries!!.sectionType)
        } finally {
            com.example.data.sync.CatalogTaskOrchestrator.clearForTest(context)
            db.openHelper.writableDatabase.execSQL("DELETE FROM sync_section_state WHERE playlistId = ?", arrayOf(playlistId))
        }
    }

    @Test
    fun v19Phase9a_ianaDstBoundaryPerMatchOffsetAndFixedZone() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = StreamVaultDatabase.getInstance(context)
        val repo = com.example.data.repository.FootballRepository(
            context = context,
            favoriteTeamDao = db.favoriteTeamDao(),
            tournamentPreferenceDao = db.tournamentPreferenceDao(),
            streamDao = db.streamDao()
        )
        val utc = java.util.TimeZone.getTimeZone("UTC")
        // Europe/Paris DST 2026 begins on Sunday, March 29, 2026 at 01:00 UTC.
        val beforeDstEpochMs = java.util.Calendar.getInstance(utc).apply {
            set(2026, java.util.Calendar.MARCH, 28, 19, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        val afterDstEpochMs = java.util.Calendar.getInstance(utc).apply {
            set(2026, java.util.Calendar.MARCH, 30, 19, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis

        val parisZone = com.example.data.repository.UserZoneRegistry.getZoneByCode("FR")
        val riyadhZone = com.example.data.repository.UserZoneRegistry.getZoneByCode("SA")

        // 1. Europe/Paris: GMT+1 before DST -> GMT+2 after DST
        assertEquals(60, parisZone.getAutoIanaOffsetMinutes(beforeDstEpochMs))
        assertEquals("GMT+1", parisZone.getUtcOffsetBadge(beforeDstEpochMs, 0))
        assertEquals("Auto (Standard)", parisZone.getClockStatusLabel(beforeDstEpochMs, 0))
        assertEquals("20:00", parisZone.formatLocalTime(beforeDstEpochMs, 0, use24Hour = true))

        assertEquals(120, parisZone.getAutoIanaOffsetMinutes(afterDstEpochMs))
        assertEquals("GMT+2", parisZone.getUtcOffsetBadge(afterDstEpochMs, 0))
        assertEquals("Auto (DST active)", parisZone.getClockStatusLabel(afterDstEpochMs, 0))
        assertEquals("21:00", parisZone.formatLocalTime(afterDstEpochMs, 0, use24Hour = true))

        // 2. Both timestamps in the SAME match list must compute distinct per-match offsets
        val beforeMatch = com.example.data.repository.FootballMatch(
            matchId = "dst_before",
            tournamentId = "uefa_cl",
            tournamentName = "UEFA Champions League",
            tournamentRegion = "Europe",
            dayTab = com.example.data.repository.MatchDayTab.TODAY,
            kickoffTimeFormatted = "20:00",
            kickoffDateFormatted = "Today",
            status = com.example.data.repository.MatchStatus.UPCOMING,
            liveMinuteOrBadge = "20:00",
            homeTeamId = "psg",
            homeTeamName = "Paris Saint-Germain",
            homeTeamBadge = "",
            homeScore = null,
            awayTeamId = "rma",
            awayTeamName = "Real Madrid",
            awayTeamBadge = "",
            awayScore = null,
            stadiumName = "Parc des Princes",
            cityName = "Paris",
            beinChannel = "Canal+ Foot",
            beinChannelNumberHint = "Canal+",
            kickoffEpochMs = beforeDstEpochMs
        )
        val afterMatch = beforeMatch.copy(
            matchId = "dst_after",
            dayTab = com.example.data.repository.MatchDayTab.DAY_PLUS_2,
            kickoffEpochMs = afterDstEpochMs
        )

        val relocalizedTimes = repo.relocalizeMatchTimesOnly(
            matches = listOf(beforeMatch, afterMatch),
            zone = parisZone,
            manualOffsetMinutes = 0
        )
        assertEquals("20:00", relocalizedTimes[0].kickoffTimeFormatted)
        assertTrue(relocalizedTimes[0].kickoffDateFormatted.contains("GMT+1"))
        assertEquals("21:00", relocalizedTimes[1].kickoffTimeFormatted)
        assertTrue(relocalizedTimes[1].kickoffDateFormatted.contains("GMT+2"))

        // 3. Asia/Riyadh stays GMT+3 year-round across both timestamps
        assertEquals(180, riyadhZone.getAutoIanaOffsetMinutes(beforeDstEpochMs))
        assertEquals(180, riyadhZone.getAutoIanaOffsetMinutes(afterDstEpochMs))
        assertEquals("GMT+3", riyadhZone.getUtcOffsetBadge(beforeDstEpochMs, 0))
        assertEquals("GMT+3", riyadhZone.getUtcOffsetBadge(afterDstEpochMs, 0))
        assertEquals("Auto (Fixed)", riyadhZone.getClockStatusLabel(afterDstEpochMs, 0))
        val riyadhRelocalized = repo.relocalizeMatchTimesOnly(
            matches = listOf(beforeMatch, afterMatch),
            zone = riyadhZone,
            manualOffsetMinutes = 0
        )
        assertEquals("22:00", riyadhRelocalized[0].kickoffTimeFormatted)
        assertEquals("22:00", riyadhRelocalized[1].kickoffTimeFormatted)
    }

    @Test
    fun v19Phase9a_manualOffsetRamadanShiftAndResetRestoresAuto() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = StreamVaultDatabase.getInstance(context)
        val repo = com.example.data.repository.FootballRepository(
            context = context,
            favoriteTeamDao = db.favoriteTeamDao(),
            tournamentPreferenceDao = db.tournamentPreferenceDao(),
            streamDao = db.streamDao()
        )
        try {
            // Confirm setSelectedCountryZone does NOT prematurely complete onboarding
            repo.setCountryZoneOnboardingCompleted(false)
            repo.setSelectedCountryZone("MA")
            assertFalse(
                "Selecting a country zone must not prematurely mark onboarding completed",
                repo.hasCompletedCountryZoneOnboarding()
            )

            // July 15, 2026 20:00 UTC -> Morocco IANA Africa/Casablanca is GMT+1 (21:00)
            val utc = java.util.TimeZone.getTimeZone("UTC")
            val july20UtcMs = java.util.Calendar.getInstance(utc).apply {
                set(2026, java.util.Calendar.JULY, 15, 20, 0, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis

            val moroccoZone = com.example.data.repository.UserZoneRegistry.getZoneByCode("MA")
            assertEquals(60, moroccoZone.getEffectiveOffsetMinutes(july20UtcMs, 0))
            assertEquals("GMT+1", moroccoZone.getUtcOffsetBadge(july20UtcMs, 0))
            assertEquals("21:00", com.example.data.repository.formatDynamicKickoffTime(july20UtcMs, moroccoZone, 0))

            // Apply Ramadan -1h manual override (-60 minutes) -> GMT+0 (20:00)
            assertEquals(-60, repo.setManualTimeOffsetMinutes(-60))
            assertEquals(-60, repo.getManualTimeOffsetMinutes())
            assertEquals(0, moroccoZone.getEffectiveOffsetMinutes(july20UtcMs, -60))
            assertEquals("GMT+0", moroccoZone.getUtcOffsetBadge(july20UtcMs, -60))
            assertEquals("Manual (-1h)", moroccoZone.getClockStatusLabel(july20UtcMs, -60))
            assertEquals("20:00", com.example.data.repository.formatDynamicKickoffTime(july20UtcMs, moroccoZone, -60))

            val match = com.example.data.repository.FootballMatch(
                matchId = "ma_ramadan_test",
                tournamentId = "botola_pro",
                tournamentName = "Botola Pro Inwi",
                tournamentRegion = "Morocco",
                dayTab = com.example.data.repository.MatchDayTab.TODAY,
                kickoffTimeFormatted = "21:00",
                kickoffDateFormatted = "Today • GMT+1",
                status = com.example.data.repository.MatchStatus.UPCOMING,
                liveMinuteOrBadge = "21:00",
                homeTeamId = "raja",
                homeTeamName = "Raja Casablanca",
                homeTeamBadge = "",
                homeScore = null,
                awayTeamId = "wydad",
                awayTeamName = "Wydad AC",
                awayTeamBadge = "",
                awayScore = null,
                stadiumName = "Stade Mohammed V",
                cityName = "Casablanca",
                beinChannel = "Arryadia HD",
                beinChannelNumberHint = "Arryadia",
                kickoffEpochMs = july20UtcMs
            )

            val ramadanShifted = repo.relocalizeMatchTimesOnly(listOf(match), moroccoZone, -60)
            assertEquals("20:00", ramadanShifted.first().kickoffTimeFormatted)
            assertTrue(ramadanShifted.first().kickoffDateFormatted.contains("GMT+0"))

            // Reset to 0 -> restores Auto GMT+1 (21:00)
            assertEquals(0, repo.setManualTimeOffsetMinutes(0))
            val restoredAuto = repo.relocalizeMatchTimesOnly(ramadanShifted, moroccoZone, 0)
            assertEquals("21:00", restoredAuto.first().kickoffTimeFormatted)
            assertTrue(restoredAuto.first().kickoffDateFormatted.contains("GMT+1"))

            // Clamping check: [-180, +180] in 60-minute steps
            assertEquals(-180, repo.setManualTimeOffsetMinutes(-300))
            assertEquals(180, repo.setManualTimeOffsetMinutes(300))
            assertEquals(0, repo.setManualTimeOffsetMinutes(0))
        } finally {
            repo.setManualTimeOffsetMinutes(0)
            repo.setSelectedCountryZone("MA")
        }
    }

    @Test
    fun v19Phase9a_rapidZoneAndManualOffsetSwitchingSettlesCleanlyWithoutBlocking() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = com.example.ui.viewmodel.MainViewModel(app)
        try {
            // Rapidly fire multiple zone switches and manual offset adjustments without blocking
            viewModel.selectCountryZone("FR", triggerNetworkRefresh = false)
            viewModel.selectCountryZone("GB", triggerNetworkRefresh = false)
            viewModel.selectCountryZone("SA", triggerNetworkRefresh = false)
            viewModel.selectCountryZone("ES", triggerNetworkRefresh = false)
            viewModel.setManualTimeOffset(-60)
            viewModel.adjustManualTimeOffset(-60) // -120
            viewModel.adjustManualTimeOffset(60)  // -60
            viewModel.setManualTimeOffset(0)
            viewModel.selectCountryZone("DE", triggerNetworkRefresh = false)
            viewModel.setManualTimeOffset(60)

            // Immediate StateFlow values must already reflect the latest selections
            assertEquals("DE", viewModel.selectedCountryZone.value.countryCode)
            assertEquals(60, viewModel.manualTimeOffsetMinutes.value)

            // Deterministically wait for the serialized IO persistence job to settle on the final selection
            viewModel.awaitPendingZoneUpdate()

            assertEquals("DE", viewModel.selectedCountryZone.value.countryCode)
            assertEquals(60, viewModel.manualTimeOffsetMinutes.value)
            assertEquals("DE", viewModel.footballRepository.getSelectedCountryZone().countryCode)
            assertEquals(60, viewModel.footballRepository.getManualTimeOffsetMinutes())
        } finally {
            viewModel.awaitPendingZoneUpdate()
            viewModel.footballRepository.setManualTimeOffsetMinutes(0)
            viewModel.footballRepository.setSelectedCountryZone("MA")
        }
    }

    @Test
    fun v19Phase9b_welcomeOnboardingPhoneTwoStepFlowClockStepperAndCleanCopy() {
        val selectedZoneState = androidx.compose.runtime.mutableStateOf(
            com.example.data.repository.UserZoneRegistry.getZoneByCode("MA")
        )
        val manualOffsetState = androidx.compose.runtime.mutableIntStateOf(0)
        val favoritesState = androidx.compose.runtime.mutableStateOf<List<com.example.data.model.FavoriteTeamEntity>>(emptyList())
        val searchQueryState = androidx.compose.runtime.mutableStateOf("")
        var completedCount = 0

        composeTestRule.setContent {
            com.example.ui.home.FirstTimeFavoriteTeamsDialog(
                selectedCountryZone = selectedZoneState.value,
                selectedSeasonTimeMode = com.example.data.repository.SeasonTimeMode.AUTO,
                manualTimeOffsetMinutes = manualOffsetState.intValue,
                onSetManualTimeOffset = { manualOffsetState.intValue = it.coerceIn(-180, 180) },
                onAdjustManualTimeOffset = { delta ->
                    manualOffsetState.intValue = (manualOffsetState.intValue + delta).coerceIn(-180, 180)
                },
                use24HourFormat = true,
                activeEdition = com.example.util.SetPlayEdition.PHONE,
                searchQuery = searchQueryState.value,
                searchResults = emptyList(),
                isSearching = false,
                selectedFavorites = favoritesState.value,
                onSelectCountryZone = { code ->
                    selectedZoneState.value = com.example.data.repository.UserZoneRegistry.getZoneByCode(code)
                },
                onSelectSeasonTimeMode = {},
                onAutoDetectCountryZone = {},
                onOpenFullCountryZoneSheet = {},
                onSearchQueryChange = { searchQueryState.value = it },
                onToggleTeam = { team ->
                    val existing = favoritesState.value.any { it.teamId == team.teamId }
                    favoritesState.value = if (existing) {
                        favoritesState.value.filterNot { it.teamId == team.teamId }
                    } else {
                        favoritesState.value + com.example.data.model.FavoriteTeamEntity(
                            teamId = team.teamId,
                            teamName = team.name,
                            shortName = team.shortName,
                            badgeUrl = team.badgeUrl,
                            country = team.country,
                            primaryLeague = team.primaryLeague
                        )
                    }
                },
                onRemoveFavoriteTeam = { teamId ->
                    favoritesState.value = favoritesState.value.filterNot { it.teamId == teamId }
                },
                onCompleteOnboarding = { completedCount++ }
            )
        }
        composeTestRule.waitForIdle()

        // Step 1: Verify core dialog, region card, live clock stepper, and preserved skip button
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("first_time_teams_onboarding_dialog")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasText("Welcome to Kickoff Arena", substring = true)).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("onboarding_country_zone_card")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("local_clock_adjustment_control")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("local_clock_minus_1h_btn")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("local_clock_auto_btn")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("local_clock_plus_1h_btn")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("onboarding_skip_btn")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("onboarding_continue_step2_btn")).assertExists()

        // Zero raw internal IPTV pipe prefixes anywhere on Welcome screen
        composeTestRule.onNode(androidx.compose.ui.test.hasText("MA|", substring = true)).assertDoesNotExist()
        composeTestRule.onNode(androidx.compose.ui.test.hasText("AE|", substring = true)).assertDoesNotExist()
        composeTestRule.onNode(androidx.compose.ui.test.hasText("AR|", substring = true)).assertDoesNotExist()

        // Tap -1h and verify offset updates cleanly to -60 (Adjusted -1h), then Auto resets to 0
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("local_clock_minus_1h_btn")).performClick()
        composeTestRule.waitForIdle()
        assertEquals(-60, manualOffsetState.intValue)
        composeTestRule.onNode(
            androidx.compose.ui.test.hasTestTag("local_clock_status_text") and
                androidx.compose.ui.test.hasText("Adjusted -1h", substring = true)
        ).assertExists()

        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("local_clock_auto_btn")).performClick()
        composeTestRule.waitForIdle()
        assertEquals(0, manualOffsetState.intValue)
        composeTestRule.onNode(
            androidx.compose.ui.test.hasTestTag("local_clock_status_text") and
                androidx.compose.ui.test.hasText("Auto", substring = true)
        ).assertExists()

        // Advance cleanly to Step 2
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("onboarding_continue_step2_btn")).performClick()
        composeTestRule.waitForIdle()

        // Verify Step 2 preserved testTags and tappable local + global giant chips
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("onboarding_team_search_input")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("onboarding_skip_btn")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasText("Popular in Morocco", substring = true)).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasText("Global Giants", substring = true)).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("onboarding_local_club_chip_quick_ma_raja_casablanca")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("onboarding_giant_club_chip_giant_real_madrid")).assertExists()

        // Tap a local club chip -> updates favorites strip & CTA count
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("onboarding_local_club_chip_quick_ma_raja_casablanca")).performClick()
        composeTestRule.waitForIdle()
        assertEquals(1, favoritesState.value.size)
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("onboarding_selected_clubs_strip")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasText("Enter Kickoff Arena (1)", substring = true)).assertExists()

        // Finish onboarding
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("onboarding_save_teams_btn")).performClick()
        composeTestRule.waitForIdle()
        assertEquals(1, completedCount)
    }

    @Test
    fun v19Phase9b_welcomeOnboardingAndroidTvTwoColumnLayoutWorksCleanly() {
        val manualOffsetState = androidx.compose.runtime.mutableIntStateOf(0)

        composeTestRule.setContent {
            com.example.ui.home.FirstTimeFavoriteTeamsDialog(
                selectedCountryZone = com.example.data.repository.UserZoneRegistry.getZoneByCode("FR"),
                selectedSeasonTimeMode = com.example.data.repository.SeasonTimeMode.AUTO,
                manualTimeOffsetMinutes = manualOffsetState.intValue,
                onSetManualTimeOffset = { manualOffsetState.intValue = it },
                onAdjustManualTimeOffset = { delta -> manualOffsetState.intValue += delta },
                use24HourFormat = true,
                activeEdition = com.example.util.SetPlayEdition.TV,
                searchQuery = "",
                searchResults = emptyList(),
                isSearching = false,
                selectedFavorites = emptyList(),
                onSelectCountryZone = {},
                onSelectSeasonTimeMode = {},
                onAutoDetectCountryZone = {},
                onOpenFullCountryZoneSheet = {},
                onSearchQueryChange = {},
                onToggleTeam = {},
                onRemoveFavoriteTeam = {},
                onCompleteOnboarding = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("first_time_teams_onboarding_dialog")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("local_clock_adjustment_control")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("onboarding_skip_btn")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasText("FR|", substring = true)).assertDoesNotExist()

        // Step 1 -> Step 2 on TV
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("onboarding_continue_step2_btn")).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("onboarding_team_search_input")).assertExists()
        composeTestRule.onNode(androidx.compose.ui.test.hasText("Popular in France", substring = true)).assertExists()
    }
}









