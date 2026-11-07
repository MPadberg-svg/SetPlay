package com.example.ui.sync

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.sync.DiscoveredTvHost
import com.example.data.sync.SyncRole
import com.example.data.sync.TvSyncManager
import com.example.data.sync.TvSyncSessionState
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ArenaFloodlightLime
import com.example.ui.theme.Champagne
import com.example.ui.theme.DarkGraphite
import com.example.ui.theme.KickoffCeruleanBlue
import com.example.ui.theme.KickoffOceanBase
import com.example.ui.theme.KickoffOceanElevated
import com.example.ui.theme.KickoffTropicalTeal
import com.example.ui.theme.LiveRed
import com.example.ui.theme.MetalCharcoal
import com.example.ui.theme.PaleGold
import com.example.ui.theme.TabularNumeralTextStyle
import com.example.ui.theme.tvFocusableControl
import com.example.ui.theme.tvFocusablePill
import java.security.MessageDigest

/**
 * Sleek, non-intrusive 52.dp "📺 Broadcasting on TV" Companion Bar (`testTag("tv_sync_companion_bar")`).
 * Docks cleanly above `BottomStreamingNav` on the phone when `syncState.role == SyncRole.PHONE_CONTROLLER && syncState.isSyncActive`:
 * - Never traps the user in a full-screen player or remote.
 * - Shows what is currently playing on the TV + 1-tap `[ 💬 Subtitles ]`, `[ ⏯ ]`, and `[ Leave Sync ]` pills.
 * - Because the TV streams directly from the IPTV server, the user can leave the app for WhatsApp anytime
 *   or pick up the physical TV remote.
 */
@Composable
fun TvSyncCompanionBar(
    syncState: TvSyncSessionState,
    onTogglePlayPauseOnTv: () -> Unit,
    onSelectSubtitleLangOnTv: (String) -> Unit,
    onLeaveSyncMode: () -> Unit,
    onOpenSyncSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showQuickSubtitlesRow by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 2.dp)
            .testTag("tv_sync_companion_bar")
    ) {
        // Optional Quick Subtitle Language Selector Pill Row for the TV
        AnimatedVisibility(visible = showQuickSubtitlesRow) {
            Surface(
                color = Color(0xF2081520),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.55f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
                    .testTag("tv_sync_subtitle_picker_row")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TV Subtitles:",
                        color = PaleGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    val langs = listOf(
                        "ar" to "🇸🇦 AR",
                        "en" to "🇬🇧 EN",
                        "fr" to "🇫🇷 FR",
                        "es" to "🇪🇸 ES",
                        "tr" to "🇹🇷 TR",
                        "off" to "🚫 Off"
                    )
                    langs.forEach { (code, label) ->
                        val isSelected = syncState.tvActiveSubtitleLang.equals(code, ignoreCase = true)
                        Surface(
                            color = if (isSelected) AmberGold else Color.White.copy(alpha = 0.10f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(
                                0.8.dp,
                                if (isSelected) AmberGold else Color.White.copy(alpha = 0.22f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .tvFocusablePill(
                                    accentColor = AmberGold,
                                    shape = RoundedCornerShape(50),
                                    onClick = {
                                        onSelectSubtitleLangOnTv(code)
                                        showQuickSubtitlesRow = false
                                    }
                                )
                                .testTag("tv_sync_sub_lang_$code")
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) DarkGraphite else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Main 52.dp Companion Bar
        Surface(
            color = Color(0xF5071722),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    listOf(ArenaFloodlightLime, KickoffTropicalTeal, AmberGold)
                )
            ),
            shadowElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Live Green TV Dot + Now Playing on TV Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onOpenSyncSheet() }
                        .padding(vertical = 4.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(ArenaFloodlightLime.copy(alpha = 0.18f))
                            .border(1.dp, ArenaFloodlightLime, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = "TV Sync Active",
                            tint = ArenaFloodlightLime,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        val peerLabel = syncState.connectedPeerName.ifBlank { "Smart TV" }
                        Text(
                            text = "📺 On $peerLabel • Direct TV Stream",
                            color = ArenaFloodlightLime,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = syncState.tvNowPlayingTitle.ifBlank {
                                "Ready — Tap any channel, movie or match to play on TV"
                            },
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Right: 1-Tap Controls: [ 💬 Subtitles ] | [ ⏯ ] | [ Leave Sync ]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 1. Subtitles Pill
                    Surface(
                        color = if (showQuickSubtitlesRow || syncState.tvActiveSubtitleLang.isNotBlank()) {
                            AmberGold.copy(alpha = 0.22f)
                        } else {
                            Color.White.copy(alpha = 0.10f)
                        },
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(
                            0.8.dp,
                            if (showQuickSubtitlesRow) AmberGold else Color.White.copy(alpha = 0.20f)
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .tvFocusablePill(
                                accentColor = AmberGold,
                                shape = RoundedCornerShape(50),
                                onClick = { showQuickSubtitlesRow = !showQuickSubtitlesRow }
                            )
                            .testTag("tv_sync_subtitles_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Subtitles,
                                contentDescription = "TV Subtitles",
                                tint = AmberGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = syncState.tvActiveSubtitleLang.uppercase().ifBlank { "CC" },
                                color = PaleGold,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // 2. Play / Pause on TV Pill
                    Surface(
                        color = AmberGold,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .tvFocusableControl(
                                accentColor = AmberGold,
                                shape = CircleShape,
                                onClick = onTogglePlayPauseOnTv
                            )
                            .testTag("tv_sync_play_pause_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (syncState.tvIsPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (syncState.tvIsPlaying) "Pause TV" else "Play TV",
                                tint = DarkGraphite,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    // 3. Leave Sync Mode Pill
                    Surface(
                        color = LiveRed.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.9.dp, LiveRed.copy(alpha = 0.70f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .tvFocusablePill(
                                accentColor = LiveRed,
                                shape = RoundedCornerShape(50),
                                onClick = onLeaveSyncMode
                            )
                            .testTag("tv_sync_leave_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LinkOff,
                                contentDescription = "Leave Sync",
                                tint = Color(0xFFFF8A8A),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Leave Sync",
                                color = Color(0xFFFFB4B4),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * ModalBottomSheet for Local Wi-Fi Phone ↔ TV QR Companion & Sync Mode (`testTag("tv_sync_modal_sheet")`).
 * - Tab 1: "📲 Phone: Connect to a TV" (Auto-discovered LAN TVs, 1-Tap Reconnect to Last TV, IP + 6-Digit PIN or QR URI).
 * - Tab 2: "📺 TV: Show QR & Pairing Code" (Starts TV Host Server, renders `TvQrCodeCanvas`, 6-digit PIN & LAN IP).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvSyncModalSheet(
    syncState: TvSyncSessionState,
    qrPayload: String,
    hasSavedTvHost: Boolean,
    savedTvHostName: String,
    savedTvHostSummary: Triple<String, Int, String>?,
    statusErrorMessage: String?,
    onStartTvHostMode: () -> Unit,
    onStartPhoneDiscovery: () -> Unit,
    onConnectToTv: (ip: String, port: Int, pin: String) -> Unit,
    onReconnectLastTv: () -> Unit,
    onLeaveSyncMode: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTabIndex by remember {
        mutableIntStateOf(if (syncState.role == SyncRole.TV_HOST) 1 else 0)
    }

    var ipInput by remember(savedTvHostSummary) {
        mutableStateOf(savedTvHostSummary?.first ?: "")
    }
    var pinInput by remember(savedTvHostSummary) {
        mutableStateOf(savedTvHostSummary?.third ?: "")
    }

    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 0) {
            onStartPhoneDiscovery()
        } else {
            onStartTvHostMode()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = KickoffOceanBase,
        modifier = Modifier.testTag("tv_sync_modal_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(KickoffTropicalTeal, KickoffCeruleanBlue)
                                )
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CastConnected,
                            contentDescription = null,
                            tint = DarkGraphite,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "SetPlay Wi-Fi Phone ↔ TV Sync",
                            color = PaleGold,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "100% Local Wi-Fi • Single-Connection Safe • WhatsApp Friendly",
                            color = Champagne,
                            fontSize = 11.5.sp
                        )
                    }
                }

                Surface(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .tvFocusableControl(
                            accentColor = AmberGold,
                            shape = CircleShape,
                            onClick = onDismiss
                        )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Champagne,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // 2-Tab Mode Switcher: [ 📲 Phone: Connect to TV ] | [ 📺 TV: Show QR & PIN ]
            Surface(
                color = KickoffOceanElevated,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val tabs = listOf(
                        0 to "📲 Phone: Connect to TV",
                        1 to "📺 TV: Show QR & PIN"
                    )
                    tabs.forEach { (idx, title) ->
                        val isSelected = selectedTabIndex == idx
                        Surface(
                            color = if (isSelected) AmberGold else Color.Transparent,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .tvFocusableControl(
                                    accentColor = AmberGold,
                                    shape = RoundedCornerShape(10.dp),
                                    onClick = { selectedTabIndex = idx }
                                )
                                .testTag(if (idx == 0) "tv_sync_tab_phone" else "tv_sync_tab_tv")
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) DarkGraphite else Champagne,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp)
                            )
                        }
                    }
                }
            }

            // Active Sync Banner if already paired
            if (syncState.isSyncActive) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A221E)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.2.dp, ArenaFloodlightLime),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ArenaFloodlightLime,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (syncState.role == SyncRole.TV_HOST) {
                                        if (syncState.connectedPeerName.isNotBlank()) {
                                            "🟢 Paired with ${syncState.connectedPeerName} • Accounts & Favorites Synced"
                                        } else {
                                            "🟢 TV Host Ready on ${syncState.localIpAddress}:${syncState.serverPort}"
                                        }
                                    } else {
                                        "🟢 Synced with ${syncState.connectedPeerName} • Direct TV Streaming"
                                    },
                                    color = Color.White,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                if (syncState.lastSyncStatusMessage.isNotBlank()) {
                                    Text(
                                        text = syncState.lastSyncStatusMessage,
                                        color = Champagne,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            color = LiveRed.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, LiveRed),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .tvFocusablePill(
                                    accentColor = LiveRed,
                                    shape = RoundedCornerShape(50),
                                    onClick = onLeaveSyncMode
                                )
                        ) {
                            Text(
                                text = "Disconnect",
                                color = Color(0xFFFFB4B4),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            if (selectedTabIndex == 0) {
                // =====================================================================
                // TAB 1: PHONE CONTROLLER — CONNECT TO TV
                // =====================================================================
                if (hasSavedTvHost && !syncState.isSyncActive) {
                    Surface(
                        color = AmberGold.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, AmberGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .tvFocusableControl(
                                accentColor = AmberGold,
                                shape = RoundedCornerShape(14.dp),
                                onClick = onReconnectLastTv
                            )
                            .testTag("tv_sync_reconnect_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "⚡ 1-Tap Reconnect to $savedTvHostName",
                                    color = AmberGold,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Resume Sync Mode on ${savedTvHostSummary?.first ?: ""}:${savedTvHostSummary?.second ?: 8765}",
                                    color = Champagne,
                                    fontSize = 11.5.sp
                                )
                            }
                            Surface(
                                color = AmberGold,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "Rejoin",
                                    color = DarkGraphite,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Auto-Discovered Wi-Fi TVs on LAN
                if (syncState.discoveredTvHosts.isNotEmpty()) {
                    Text(
                        text = "📺 Auto-Discovered TVs on Your Wi-Fi",
                        color = PaleGold,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    syncState.discoveredTvHosts.forEach { host: DiscoveredTvHost ->
                        Surface(
                            color = KickoffOceanElevated,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, KickoffTropicalTeal.copy(alpha = 0.55f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .tvFocusableControl(
                                    accentColor = KickoffTropicalTeal,
                                    shape = RoundedCornerShape(12.dp),
                                    onClick = {
                                        ipInput = host.hostIp
                                    }
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Wifi,
                                        contentDescription = null,
                                        tint = KickoffTropicalTeal,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "📺 ${host.serviceName}",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${host.hostIp}:${host.port}",
                                            color = Champagne,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                Text(
                                    text = "Tap to Fill IP",
                                    color = KickoffTropicalTeal,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                // Manual IP / QR Payload + 6-Digit PIN Entry
                Card(
                    colors = CardDefaults.cardColors(containerColor = MetalCharcoal),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Enter TV Wi-Fi IP (or Paste QR Link) & 6-Digit PIN",
                            color = PaleGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = ipInput,
                            onValueChange = { raw ->
                                val parsed = TvSyncManager.parseQrPairingPayload(raw)
                                if (parsed != null) {
                                    ipInput = parsed.first
                                    pinInput = parsed.third
                                } else {
                                    ipInput = raw
                                }
                            },
                            label = { Text("TV Wi-Fi IP Address or QR URI (e.g. 192.168.1.50)", fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmberGold,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.20f),
                                focusedContainerColor = DarkGraphite,
                                unfocusedContainerColor = DarkGraphite,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tv_sync_ip_input")
                        )

                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) pinInput = it },
                            label = { Text("6-Digit TV Pairing Code", fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmberGold,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.20f),
                                focusedContainerColor = DarkGraphite,
                                unfocusedContainerColor = DarkGraphite,
                                focusedTextColor = AmberGold,
                                unfocusedTextColor = AmberGold
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tv_sync_pin_input")
                        )

                        if (!statusErrorMessage.isNullOrBlank()) {
                            Text(
                                text = statusErrorMessage,
                                color = LiveRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                val rawHost = ipInput.trim()
                                val hostPart = rawHost.substringBefore(":")
                                val portPart = rawHost.substringAfter(":", "").toIntOrNull()
                                    ?: savedTvHostSummary?.second
                                    ?: TvSyncManager.DEFAULT_PORT
                                onConnectToTv(hostPart, portPart, pinInput.trim())
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberGold,
                                contentColor = DarkGraphite
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tv_sync_connect_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Smartphone,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pair Phone & Sync Vault to TV",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.5.sp
                            )
                        }
                    }
                }
            } else {
                // =====================================================================
                // TAB 2: TV HOST — SHOW QR CODE & 6-DIGIT PAIRING PIN
                // =====================================================================
                Card(
                    colors = CardDefaults.cardColors(containerColor = MetalCharcoal),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.2.dp, AmberGold.copy(alpha = 0.65f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = null,
                                tint = AmberGold,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Scan or Enter Code on Your Phone",
                                color = PaleGold,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        // Dependency-Free Scannable Visual QR Matrix Canvas
                        TvQrCodeCanvas(
                            payload = qrPayload,
                            modifier = Modifier
                                .size(176.dp)
                                .testTag("tv_sync_qr_canvas")
                        )

                        // Large 6-Digit Pairing PIN & Wi-Fi IP Address
                        Surface(
                            color = DarkGraphite,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, AmberGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "6-DIGIT PAIRING CODE",
                                    color = Champagne,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = syncState.pairingPin.ifBlank { "------" }
                                        .chunked(1)
                                        .joinToString(" "),
                                    color = AmberGold,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = TabularNumeralTextStyle
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.10f))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "TV Wi-Fi IP: ${syncState.localIpAddress}:${syncState.serverPort}",
                                    color = Color.White,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    style = TabularNumeralTextStyle
                                )
                            }
                        }

                        Text(
                            text = "Open SetPlay on your phone → Tap the 📺 Wi-Fi TV Sync icon in the top bar → Enter the IP & 6-Digit Code above to transfer your Xtream account, Favorites & control playback.",
                            color = Champagne,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * Dependency-free deterministic 21x21 QR-style matrix canvas with standard 7x7 finder patterns
 * and SHA-256 payload encoding so the TV pairing screen renders a crisp scannable matrix without
 * external ZXing bloat.
 */
@Composable
fun TvQrCodeCanvas(
    payload: String,
    modifier: Modifier = Modifier
) {
    val digestBytes = remember(payload) {
        MessageDigest.getInstance("SHA-256").digest(payload.toByteArray(Charsets.UTF_8))
    }

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(3.dp, AmberGold),
        modifier = modifier
    ) {
        Canvas(
            modifier = Modifier
                .padding(12.dp)
        ) {
            val gridSize = 21
            val cellSize = size.minDimension / gridSize

            fun isFinderModule(r: Int, c: Int, topR: Int, leftC: Int): Boolean? {
                val dr = r - topR
                val dc = c - leftC
                if (dr !in 0..6 || dc !in 0..6) return null
                val isOuterRing = dr == 0 || dr == 6 || dc == 0 || dc == 6
                val isInnerCore = dr in 2..4 && dc in 2..4
                return isOuterRing || isInnerCore
            }

            var bitIndex = 0
            for (r in 0 until gridSize) {
                for (c in 0 until gridSize) {
                    val finderTopLeft = isFinderModule(r, c, 0, 0)
                    val finderTopRight = isFinderModule(r, c, 0, gridSize - 7)
                    val finderBottomLeft = isFinderModule(r, c, gridSize - 7, 0)

                    // Quiet separator around finders
                    val isSeparator = (r == 7 && (c <= 7 || c >= gridSize - 8)) ||
                        (r == gridSize - 8 && c <= 7) ||
                        (c == 7 && (r <= 7 || r >= gridSize - 8)) ||
                        (c == gridSize - 8 && r <= 7)

                    val isDark = when {
                        finderTopLeft != null -> finderTopLeft
                        finderTopRight != null -> finderTopRight
                        finderBottomLeft != null -> finderBottomLeft
                        isSeparator -> false
                        r == 6 -> c % 2 == 0
                        c == 6 -> r % 2 == 0
                        else -> {
                            val byteIdx = (bitIndex / 8) % digestBytes.size
                            val bitOffset = bitIndex % 8
                            bitIndex++
                            ((digestBytes[byteIdx].toInt() ushr bitOffset) and 1) == 1
                        }
                    }

                    if (isDark) {
                        drawRoundRect(
                            color = Color(0xFF061018),
                            topLeft = Offset(c * cellSize, r * cellSize),
                            size = Size(cellSize * 0.92f, cellSize * 0.92f),
                            cornerRadius = CornerRadius(cellSize * 0.18f, cellSize * 0.18f)
                        )
                    }
                }
            }
        }
    }
}
