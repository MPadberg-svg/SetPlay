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
import com.example.ui.theme.LiveCrimson
import com.example.ui.theme.MetalCharcoal
import com.example.ui.theme.PaleGold
import com.example.ui.theme.SeriesEmerald
import com.example.ui.theme.SeriesJadeAccent
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

        // Section signature sub-palettes
        assertEquals(Color(0xFFE63946), LiveCrimson)
        assertEquals(Color(0xFFC1121F), CinemaRuby)
        assertEquals(Color(0xFFE5A93C), CinemaMarqueeGold)
        assertEquals(Color(0xFF1B998B), SeriesEmerald)
        assertEquals(Color(0xFF2EC4B6), SeriesJadeAccent)
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
}
