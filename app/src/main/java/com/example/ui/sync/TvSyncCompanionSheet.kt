package com.example.ui.sync

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface as AndroidSurface
import android.view.TextureView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
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
import com.example.util.SetPlayEdition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Sleek, non-intrusive 52.dp "📺 On TV" Companion Bar (`testTag("tv_sync_companion_bar")`).
 * Docks cleanly above `BottomStreamingNav` on the phone when `syncState.role == SyncRole.PHONE_CONTROLLER && syncState.isSyncActive`:
 * - Never traps the user in a full-screen player or remote.
 * - Shows what is currently playing on the TV + streamlined 1-tap controls (`[ ⏮ ]`, `[ ⏭ ]`, `[ ⏯ ]`, `[ 📱 Watch Here ]`, `[ Leave Sync ]`).
 * - Because the TV streams directly from the IPTV server, the user can leave the app for WhatsApp anytime
 *   or pick up the physical TV remote.
 */
@Composable
fun TvSyncCompanionBar(
    syncState: TvSyncSessionState,
    onTogglePlayPauseOnTv: () -> Unit,
    onZapPrevOnTv: () -> Unit = {},
    onZapNextOnTv: () -> Unit = {},
    onPullStreamToPhone: () -> Unit = {},
    onLeaveSyncMode: () -> Unit,
    onOpenSyncSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasActiveTvStream = syncState.tvNowPlayingStreamId.isNotBlank() || syncState.tvNowPlayingTitle.isNotBlank()
    val isLiveTvOnTv = syncState.tvNowPlayingType.equals("live", ignoreCase = true)
    val peerLabel = syncState.connectedPeerName.ifBlank { "Smart TV" }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 2.dp)
            .testTag("tv_sync_companion_bar")
    ) {
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
                // Left: Live Green TV Dot + Status & Now Playing Title
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
                        Text(
                            text = if (hasActiveTvStream) {
                                "📺 On TV: ${syncState.tvNowPlayingTitle}"
                            } else {
                                "📺 Synced with $peerLabel — Tap any item to play on TV"
                            },
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (hasActiveTvStream) {
                                "Direct TV Stream on $peerLabel • WhatsApp & TV Remote Ready"
                            } else {
                                "Ready — Tap any channel, movie or match to play on TV"
                            },
                            color = ArenaFloodlightLime,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Right: Streamlined Action Pills
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (isLiveTvOnTv) {
                        Surface(
                            color = KickoffOceanElevated,
                            shape = CircleShape,
                            border = BorderStroke(0.9.dp, PaleGold.copy(alpha = 0.65f)),
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .tvFocusableControl(
                                    accentColor = AmberGold,
                                    shape = CircleShape,
                                    onClick = onZapPrevOnTv
                                )
                                .testTag("tv_sync_zap_prev_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "⏮",
                                    color = PaleGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Surface(
                            color = KickoffOceanElevated,
                            shape = CircleShape,
                            border = BorderStroke(0.9.dp, PaleGold.copy(alpha = 0.65f)),
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .tvFocusableControl(
                                    accentColor = AmberGold,
                                    shape = CircleShape,
                                    onClick = onZapNextOnTv
                                )
                                .testTag("tv_sync_zap_next_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "⏭",
                                    color = PaleGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    // 1. Play / Pause on TV Pill
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

                    // 2. Pull Stream from TV to Phone ("[ 📱 Watch Here ]")
                    Surface(
                        color = KickoffTropicalTeal.copy(alpha = if (hasActiveTvStream) 0.22f else 0.12f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.9.dp, KickoffTropicalTeal.copy(alpha = if (hasActiveTvStream) 1f else 0.55f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .tvFocusablePill(
                                accentColor = KickoffTropicalTeal,
                                shape = RoundedCornerShape(50),
                                onClick = onPullStreamToPhone
                            )
                            .testTag("tv_sync_pull_to_phone_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Smartphone,
                                contentDescription = "Watch Here on Phone",
                                tint = KickoffTropicalTeal,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Watch Here",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
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
 * Strict Device-Context Separated ModalBottomSheet (`testTag("tv_sync_modal_sheet")`):
 * - On `SetPlayEdition.PHONE` (`SetPlay-Phone.apk`):
 *   Renders ONLY the Phone Controller UI (1-Tap Reconnect, Live Camera QR Scanner, Auto-Discovered LAN TVs, and Manual IP + 6-Digit PIN).
 *   Never shows TV Host QR generation on a phone.
 * - On `SetPlayEdition.TV` (`SetPlay-TV.apk`):
 *   Renders ONLY the TV Host Receiver UI (Reversible 21x21 `TvQrCodeCanvas`, 6-Digit Pairing Code, and TV Home Wi-Fi IP).
 *   Never shows phone manual IP inputs or camera buttons on a television.
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
    activeEdition: SetPlayEdition = SetPlayEdition.PHONE,
    defaultToTvHostTab: Boolean = false,
    isHomeLanConnected: Boolean = TvSyncManager.isHomeLanConnected(androidx.compose.ui.platform.LocalContext.current),
    onStartTvHostMode: () -> Unit,
    onStartPhoneDiscovery: () -> Unit,
    onConnectToTv: (ip: String, port: Int, pin: String) -> Unit,
    onReconnectLastTv: () -> Unit,
    onLeaveSyncMode: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val effectiveEdition = if (defaultToTvHostTab) SetPlayEdition.TV else activeEdition

    var ipInput by remember(savedTvHostSummary) {
        mutableStateOf(savedTvHostSummary?.first ?: "")
    }
    var pinInput by remember(savedTvHostSummary) {
        mutableStateOf(savedTvHostSummary?.third ?: "")
    }
    var showCameraScanner by remember { mutableStateOf(false) }

    LaunchedEffect(effectiveEdition) {
        if (effectiveEdition == SetPlayEdition.PHONE) {
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
        TvSyncSheetContent(
            syncState = syncState,
            qrPayload = qrPayload,
            hasSavedTvHost = hasSavedTvHost,
            savedTvHostName = savedTvHostName,
            savedTvHostSummary = savedTvHostSummary,
            statusErrorMessage = statusErrorMessage,
            activeEdition = effectiveEdition,
            isHomeLanConnected = isHomeLanConnected,
            ipInput = ipInput,
            onIpInputChange = { ipInput = it },
            pinInput = pinInput,
            onPinInputChange = { pinInput = it },
            showCameraScanner = showCameraScanner,
            onToggleCameraScanner = { showCameraScanner = !showCameraScanner },
            onConnectToTv = onConnectToTv,
            onReconnectLastTv = onReconnectLastTv,
            onLeaveSyncMode = onLeaveSyncMode,
            onDismiss = onDismiss
        )
    }
}

/**
 * Reusable inner content of `TvSyncModalSheet` so both `ModalBottomSheet` and unit/Robolectric
 * UI tests can verify strict device-context separation deterministically.
 */
@Composable
fun TvSyncSheetContent(
    syncState: TvSyncSessionState,
    qrPayload: String,
    hasSavedTvHost: Boolean,
    savedTvHostName: String,
    savedTvHostSummary: Triple<String, Int, String>?,
    statusErrorMessage: String?,
    activeEdition: SetPlayEdition,
    isHomeLanConnected: Boolean = true,
    ipInput: String = savedTvHostSummary?.first ?: "",
    onIpInputChange: (String) -> Unit = {},
    pinInput: String = savedTvHostSummary?.third ?: "",
    onPinInputChange: (String) -> Unit = {},
    showCameraScanner: Boolean = false,
    onToggleCameraScanner: () -> Unit = {},
    onConnectToTv: (ip: String, port: Int, pin: String) -> Unit,
    onReconnectLastTv: () -> Unit,
    onLeaveSyncMode: () -> Unit,
    onDismiss: () -> Unit
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
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
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
                        text = if (activeEdition == SetPlayEdition.TV) {
                            "SetPlay TV — Wi-Fi Companion Host"
                        } else {
                            "SetPlay Phone → TV Wi-Fi Sync"
                        },
                        color = PaleGold,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (activeEdition == SetPlayEdition.TV) {
                            "Scan with the SetPlay Phone App on the same Home Wi-Fi"
                        } else {
                            "Connect your phone to SetPlay TV on the same Home Wi-Fi"
                        },
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

        // Home Wi-Fi Connectivity Warning Banner when no private LAN interface is active
        if (!isHomeLanConnected && syncState.localIpAddress.let { it.isBlank() || it == "127.0.0.1" }) {
            Surface(
                color = LiveRed.copy(alpha = 0.16f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, LiveRed.copy(alpha = 0.75f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tv_sync_wifi_warning_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = Color(0xFFFF9E9E),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "⚠️ Wi-Fi Not Connected — Please connect to your Home Wi-Fi network.",
                        color = Color(0xFFFFD2D2),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
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
                                text = if (activeEdition == SetPlayEdition.TV || syncState.role == SyncRole.TV_HOST) {
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

        if (activeEdition == SetPlayEdition.PHONE) {
            // =====================================================================
            // PHONE EDITION ONLY (`SetPlayEdition.PHONE`): CONNECT TO TV
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

            // Prominent Camera QR Scanner Button & Live Camera2 Viewfinder Card
            Surface(
                color = KickoffTropicalTeal.copy(alpha = 0.20f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.2.dp, if (showCameraScanner) AmberGold else KickoffTropicalTeal),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onToggleCameraScanner() }
                    .testTag("tv_sync_scan_qr_camera_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AmberGold)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan TV QR Code",
                                tint = DarkGraphite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (showCameraScanner) {
                                    "📷 Close Camera QR Scanner"
                                } else {
                                    "📷 Scan TV QR Code with Camera"
                                },
                                color = PaleGold,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Point your phone camera at the SetPlay TV QR code for instant pairing",
                                color = Champagne,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = showCameraScanner) {
                PhoneCameraQrScannerCard(
                    onQrDecoded = { decodedIp, decodedPort, decodedPin ->
                        onIpInputChange("$decodedIp:$decodedPort")
                        onPinInputChange(decodedPin)
                        onToggleCameraScanner()
                        onConnectToTv(decodedIp, decodedPort, decodedPin)
                    },
                    onClose = onToggleCameraScanner
                )
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
                                    onIpInputChange("${host.hostIp}:${host.port}")
                                    if (pinInput.trim().length == 6) {
                                        onConnectToTv(host.hostIp, host.port, pinInput.trim())
                                    }
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
                                text = if (pinInput.trim().length == 6) "Tap to Pair Now" else "Tap to Select TV",
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
                        text = "Enter TV Wi-Fi IP & 6-Digit Pairing Code",
                        color = PaleGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = ipInput,
                        onValueChange = { raw ->
                            val parsed = TvSyncManager.parseQrPairingPayload(raw)
                            if (parsed != null) {
                                onIpInputChange("${parsed.first}:${parsed.second}")
                                onPinInputChange(parsed.third)
                            } else {
                                onIpInputChange(raw)
                            }
                        },
                        label = { Text("TV Home Wi-Fi IP Address (e.g. 192.168.1.50)", fontSize = 12.sp) },
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
                        onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) onPinInputChange(it) },
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
            // TV EDITION ONLY (`SetPlayEdition.TV`): SHOW REVERSIBLE QR & 6-DIGIT PIN
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
                            text = "Scan with SetPlay Phone App or Enter 6-Digit PIN",
                            color = PaleGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Reversible 21x21 Scannable QR Matrix Canvas
                    TvQrCodeCanvas(
                        payload = qrPayload,
                        ipOverride = syncState.localIpAddress,
                        portOverride = syncState.serverPort,
                        pinOverride = syncState.pairingPin,
                        modifier = Modifier
                            .size(184.dp)
                            .testTag("tv_sync_qr_canvas")
                    )

                    // Large 6-Digit Pairing PIN & Home Wi-Fi IP Address
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
                                text = "TV Home Wi-Fi IP: ${syncState.localIpAddress}:${syncState.serverPort}",
                                color = Color.White,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                style = TabularNumeralTextStyle
                            )
                        }
                    }

                    Text(
                        text = "Open SetPlay on your phone → Tap the 📺 Wi-Fi TV Sync icon in the top bar → Scan this QR code with your phone camera or enter the 6-Digit Code above.",
                        color = Champagne,
                        fontSize = 11.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 * Inline Live Camera2 QR Code Scanner (`testTag("tv_sync_camera_scanner_card")`) for `SetPlayEdition.PHONE`:
 * - Requests `android.permission.CAMERA` at runtime.
 * - Uses Android's built-in `android.hardware.camera2` API (`CameraManager` + `TextureView`) with zero external Maven dependencies.
 * - Samples frames every `350ms` on `Dispatchers.Default` and decodes via `SetPlayQrCodec.decodeFromBitmap(...)`.
 */
@SuppressLint("MissingPermission")
@Composable
fun PhoneCameraQrScannerCard(
    onQrDecoded: (ip: String, port: Int, pin: String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var cameraErrorText by remember { mutableStateOf<String?>(null) }
    var activeTextureView by remember { mutableStateOf<TextureView?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            cameraErrorText = "Camera permission is required to scan the TV QR code."
        } else {
            cameraErrorText = null
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(hasCameraPermission, activeTextureView) {
        val textureView = activeTextureView ?: return@LaunchedEffect
        if (!hasCameraPermission) return@LaunchedEffect
        while (isActive) {
            delay(350L)
            if (textureView.isAvailable) {
                val frameBitmap = runCatching { textureView.getBitmap(320, 320) }.getOrNull()
                if (frameBitmap != null) {
                    val decoded = withContext(Dispatchers.Default) {
                        SetPlayQrCodec.decodeFromBitmap(frameBitmap)
                    }
                    frameBitmap.recycle()
                    if (decoded != null) {
                        onQrDecoded(decoded.first, decoded.second, decoded.third)
                        break
                    }
                }
            }
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkGraphite),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, AmberGold),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tv_sync_camera_scanner_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = AmberGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Point Camera at SetPlay TV QR Code",
                        color = PaleGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    text = "Close",
                    color = Champagne,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onClose() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            if (hasCameraPermission && cameraErrorText == null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black)
                        .border(1.dp, AmberGold.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
                ) {
                    DisposableCamera2Preview(
                        context = context,
                        onTextureViewReady = { activeTextureView = it },
                        onCameraError = { err -> cameraErrorText = err }
                    )

                    // Gold QR Target Reticle Overlay
                    Canvas(modifier = Modifier.size(156.dp)) {
                        drawRoundRect(
                            color = AmberGold,
                            style = Stroke(width = 3.dp.toPx()),
                            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                        )
                    }
                }
                Text(
                    text = "Align the white TV QR square inside the gold frame — connects automatically",
                    color = Champagne,
                    fontSize = 11.sp
                )
            } else {
                Text(
                    text = cameraErrorText ?: "Grant Camera permission to scan the SetPlay TV QR code.",
                    color = Color(0xFFFFB4B4),
                    fontSize = 12.sp
                )
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberGold,
                        contentColor = DarkGraphite
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Allow Camera Access",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun DisposableCamera2Preview(
    context: Context,
    onTextureViewReady: (TextureView) -> Unit,
    onCameraError: (String) -> Unit
) {
    var cameraDeviceRef by remember { mutableStateOf<CameraDevice?>(null) }
    var captureSessionRef by remember { mutableStateOf<CameraCaptureSession?>(null) }
    val backgroundThread = remember {
        HandlerThread("SetPlayQrCameraThread").apply { start() }
    }
    val backgroundHandler = remember { Handler(backgroundThread.looper) }

    fun openRearCamera(textureView: TextureView) {
        runCatching {
            val manager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                ?: return@runCatching
            val cameraId = manager.cameraIdList.firstOrNull { id ->
                val chars = manager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            } ?: manager.cameraIdList.firstOrNull()

            if (cameraId == null) {
                onCameraError("No camera found on this device. Use the 6-digit Pairing PIN below.")
                return@runCatching
            }

            manager.openCamera(
                cameraId,
                object : CameraDevice.StateCallback() {
                    override fun onOpened(camera: CameraDevice) {
                        cameraDeviceRef = camera
                        runCatching {
                            val surfaceTexture = textureView.surfaceTexture ?: return@runCatching
                            surfaceTexture.setDefaultBufferSize(640, 640)
                            val previewSurface = AndroidSurface(surfaceTexture)
                            val requestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                                addTarget(previewSurface)
                                set(
                                    CaptureRequest.CONTROL_AF_MODE,
                                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                                )
                            }
                            @Suppress("DEPRECATION")
                            camera.createCaptureSession(
                                listOf(previewSurface),
                                object : CameraCaptureSession.StateCallback() {
                                    override fun onConfigured(session: CameraCaptureSession) {
                                        captureSessionRef = session
                                        runCatching {
                                            session.setRepeatingRequest(
                                                requestBuilder.build(),
                                                null,
                                                backgroundHandler
                                            )
                                        }
                                    }

                                    override fun onConfigureFailed(session: CameraCaptureSession) {
                                        onCameraError("Could not start camera preview. Use the 6-digit PIN below.")
                                    }
                                },
                                backgroundHandler
                            )
                        }
                    }

                    override fun onDisconnected(camera: CameraDevice) {
                        runCatching { camera.close() }
                        cameraDeviceRef = null
                    }

                    override fun onError(camera: CameraDevice, error: Int) {
                        runCatching { camera.close() }
                        cameraDeviceRef = null
                        onCameraError("Camera unavailable ($error). Enter the 6-digit PIN below.")
                    }
                },
                backgroundHandler
            )
        }.onFailure {
            onCameraError("Camera preview error: ${it.message ?: "Use 6-digit PIN below"}")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { captureSessionRef?.close() }
            runCatching { cameraDeviceRef?.close() }
            runCatching { backgroundThread.quitSafely() }
        }
    }

    AndroidView(
        factory = { ctx ->
            TextureView(ctx).apply {
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(
                        surface: SurfaceTexture,
                        width: Int,
                        height: Int
                    ) {
                        onTextureViewReady(this@apply)
                        openRearCamera(this@apply)
                    }

                    override fun onSurfaceTextureSizeChanged(
                        surface: SurfaceTexture,
                        width: Int,
                        height: Int
                    ) = Unit

                    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                        runCatching { captureSessionRef?.close() }
                        runCatching { cameraDeviceRef?.close() }
                        return true
                    }

                    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * Reversible 21x21 QR Matrix Codec (`SetPlayQrCodec`):
 * - Packs `(ip, port, pin)` into 76 bits:
 *   - 32 bits: 4 IPv4 octets (`o0, o1, o2, o3`)
 *   - 16 bits: TCP port (`1..65535`)
 *   - 20 bits: 6-digit pairing PIN (`0..999999`)
 *   -  8 bits: CRC-8 checksum (polynomial `0x07`, init `0xA5`)
 * - Repeats those 76 bits across the 232 non-finder/non-timing data modules of the 21x21 grid
 *   (Triple-Modular Redundancy so partial glare or single-cell camera errors decode cleanly).
 */
object SetPlayQrCodec {
    const val GRID_SIZE = 21
    const val PAYLOAD_BIT_COUNT = 76
    private const val DATA_BIT_COUNT = 68

    fun isFinderModule(r: Int, c: Int, topR: Int, leftC: Int): Boolean? {
        val dr = r - topR
        val dc = c - leftC
        if (dr !in 0..6 || dc !in 0..6) return null
        val isOuterRing = dr == 0 || dr == 6 || dc == 0 || dc == 6
        val isInnerCore = dr in 2..4 && dc in 2..4
        return isOuterRing || isInnerCore
    }

    fun isSeparatorModule(r: Int, c: Int): Boolean {
        return (r == 7 && (c <= 7 || c >= GRID_SIZE - 8)) ||
            (r == GRID_SIZE - 8 && c <= 7) ||
            (c == 7 && (r <= 7 || r >= GRID_SIZE - 8)) ||
            (c == GRID_SIZE - 8 && r <= 7)
    }

    fun isReservedModule(r: Int, c: Int): Boolean {
        if (isFinderModule(r, c, 0, 0) != null) return true
        if (isFinderModule(r, c, 0, GRID_SIZE - 7) != null) return true
        if (isFinderModule(r, c, GRID_SIZE - 7, 0) != null) return true
        if (isSeparatorModule(r, c)) return true
        if (r == 6 || c == 6) return true
        return false
    }

    private fun computeCrc8(bits: BooleanArray, count: Int): Int {
        var crc = 0xA5
        for (i in 0 until count) {
            val bit = if (bits[i]) 1 else 0
            val msb = (crc ushr 7) and 1
            crc = (crc shl 1) and 0xFF
            if ((msb xor bit) != 0) {
                crc = crc xor 0x07
            }
        }
        return crc and 0xFF
    }

    private fun writeIntBits(target: BooleanArray, startBit: Int, bitLength: Int, value: Int) {
        for (i in 0 until bitLength) {
            val shift = bitLength - 1 - i
            target[startBit + i] = ((value ushr shift) and 1) == 1
        }
    }

    private fun readIntBits(source: BooleanArray, startBit: Int, bitLength: Int): Int {
        var acc = 0
        for (i in 0 until bitLength) {
            acc = (acc shl 1) or (if (source[startBit + i]) 1 else 0)
        }
        return acc
    }

    fun encodePayloadToBits(ip: String, port: Int, pin: String): BooleanArray {
        val parts = ip.trim().split(".")
        val octets = if (parts.size == 4) {
            parts.map { (it.toIntOrNull() ?: 0).coerceIn(0, 255) }
        } else {
            listOf(127, 0, 0, 1)
        }
        val safePort = port.coerceIn(1, 65535)
        val safePin = (pin.trim().toIntOrNull() ?: 0).coerceIn(0, 999999)

        val bits = BooleanArray(PAYLOAD_BIT_COUNT)
        writeIntBits(bits, 0, 8, octets[0])
        writeIntBits(bits, 8, 8, octets[1])
        writeIntBits(bits, 16, 8, octets[2])
        writeIntBits(bits, 24, 8, octets[3])
        writeIntBits(bits, 32, 16, safePort)
        writeIntBits(bits, 48, 20, safePin)

        val crc = computeCrc8(bits, DATA_BIT_COUNT)
        writeIntBits(bits, DATA_BIT_COUNT, 8, crc)
        return bits
    }

    fun encodeToMatrix(ip: String, port: Int, pin: String): Array<BooleanArray> {
        val payloadBits = encodePayloadToBits(ip, port, pin)
        val matrix = Array(GRID_SIZE) { BooleanArray(GRID_SIZE) }
        var bitIndex = 0
        for (r in 0 until GRID_SIZE) {
            for (c in 0 until GRID_SIZE) {
                val finderTL = isFinderModule(r, c, 0, 0)
                val finderTR = isFinderModule(r, c, 0, GRID_SIZE - 7)
                val finderBL = isFinderModule(r, c, GRID_SIZE - 7, 0)
                matrix[r][c] = when {
                    finderTL != null -> finderTL
                    finderTR != null -> finderTR
                    finderBL != null -> finderBL
                    isSeparatorModule(r, c) -> false
                    r == 6 -> c % 2 == 0
                    c == 6 -> r % 2 == 0
                    else -> {
                        val b = payloadBits[bitIndex % PAYLOAD_BIT_COUNT]
                        bitIndex++
                        b
                    }
                }
            }
        }
        return matrix
    }

    private fun verifyFinderCorner(grid: Array<BooleanArray>, topR: Int, leftC: Int): Boolean {
        var matches = 0
        for (dr in 0..6) {
            for (dc in 0..6) {
                val expected = isFinderModule(topR + dr, leftC + dc, topR, leftC) ?: false
                if (grid[topR + dr][leftC + dc] == expected) {
                    matches++
                }
            }
        }
        // Require at least 38/49 matching modules so minor optical blur still passes while rejecting noise
        return matches >= 38
    }

    fun decodeFromMatrix(grid21x21: Array<BooleanArray>): Triple<String, Int, String>? {
        if (grid21x21.size != GRID_SIZE || grid21x21.any { it.size != GRID_SIZE }) return null
        if (!verifyFinderCorner(grid21x21, 0, 0)) return null
        if (!verifyFinderCorner(grid21x21, 0, GRID_SIZE - 7)) return null
        if (!verifyFinderCorner(grid21x21, GRID_SIZE - 7, 0)) return null

        val trueVotes = IntArray(PAYLOAD_BIT_COUNT)
        val totalVotes = IntArray(PAYLOAD_BIT_COUNT)
        var bitIndex = 0

        for (r in 0 until GRID_SIZE) {
            for (c in 0 until GRID_SIZE) {
                if (isReservedModule(r, c)) continue
                val slot = bitIndex % PAYLOAD_BIT_COUNT
                totalVotes[slot]++
                if (grid21x21[r][c]) {
                    trueVotes[slot]++
                }
                bitIndex++
            }
        }

        val recovered = BooleanArray(PAYLOAD_BIT_COUNT) { idx ->
            trueVotes[idx] * 2 >= totalVotes[idx]
        }

        val expectedCrc = computeCrc8(recovered, DATA_BIT_COUNT)
        val actualCrc = readIntBits(recovered, DATA_BIT_COUNT, 8)
        if (expectedCrc != actualCrc) return null

        val o0 = readIntBits(recovered, 0, 8)
        val o1 = readIntBits(recovered, 8, 8)
        val o2 = readIntBits(recovered, 16, 8)
        val o3 = readIntBits(recovered, 24, 8)
        val port = readIntBits(recovered, 32, 16)
        val pinInt = readIntBits(recovered, 48, 20)

        if (port !in 1..65535 || pinInt !in 0..999999) return null
        val ip = "$o0.$o1.$o2.$o3"
        val pinFormatted = String.format(Locale.US, "%06d", pinInt)
        return Triple(ip, port, pinFormatted)
    }

    private fun rotateMatrix90(src: Array<BooleanArray>): Array<BooleanArray> {
        val dst = Array(GRID_SIZE) { BooleanArray(GRID_SIZE) }
        for (r in 0 until GRID_SIZE) {
            for (c in 0 until GRID_SIZE) {
                dst[c][GRID_SIZE - 1 - r] = src[r][c]
            }
        }
        return dst
    }

    fun renderToBitmap(
        ip: String,
        port: Int,
        pin: String,
        sizePx: Int = 252,
        quietZoneCells: Int = 2
    ): Bitmap {
        val matrix = encodeToMatrix(ip, port, pin)
        val totalCells = GRID_SIZE + quietZoneCells * 2
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(sizePx * sizePx) { 0xFFFFFFFF.toInt() }
        for (y in 0 until sizePx) {
            val cellR = (y * totalCells) / sizePx - quietZoneCells
            for (x in 0 until sizePx) {
                val cellC = (x * totalCells) / sizePx - quietZoneCells
                if (cellR in 0 until GRID_SIZE && cellC in 0 until GRID_SIZE && matrix[cellR][cellC]) {
                    pixels[y * sizePx + x] = 0xFF061018.toInt()
                }
            }
        }
        bmp.setPixels(pixels, 0, sizePx, 0, 0, sizePx, sizePx)
        return bmp
    }

    fun decodeFromBitmap(bitmap: Bitmap): Triple<String, Int, String>? {
        val w = bitmap.width
        val h = bitmap.height
        if (w < GRID_SIZE || h < GRID_SIZE) return null

        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        fun luminanceAt(x: Int, y: Int): Int {
            val px = pixels[y.coerceIn(0, h - 1) * w + x.coerceIn(0, w - 1)]
            val r = (px ushr 16) and 0xFF
            val g = (px ushr 8) and 0xFF
            val b = px and 0xFF
            return (r * 77 + g * 150 + b * 29) ushr 8
        }

        var minLum = 255
        var maxLum = 0
        for (y in 0 until h step 2) {
            for (x in 0 until w step 2) {
                val lum = luminanceAt(x, y)
                if (lum < minLum) minLum = lum
                if (lum > maxLum) maxLum = lum
            }
        }
        val threshold = ((minLum + maxLum) / 2).coerceIn(32, 220)

        fun sampleBoxToGrid(left: Float, top: Float, boxW: Float, boxH: Float): Array<BooleanArray> {
            val cellW = boxW / GRID_SIZE.toFloat()
            val cellH = boxH / GRID_SIZE.toFloat()
            return Array(GRID_SIZE) { r ->
                BooleanArray(GRID_SIZE) { c ->
                    val cx = (left + (c + 0.5f) * cellW).toInt().coerceIn(0, w - 1)
                    val cy = (top + (r + 0.5f) * cellH).toInt().coerceIn(0, h - 1)
                    luminanceAt(cx, cy) < threshold
                }
            }
        }

        fun tryDecodeWithRotations(baseGrid: Array<BooleanArray>): Triple<String, Int, String>? {
            var current = baseGrid
            repeat(4) {
                decodeFromMatrix(current)?.let { return it }
                current = rotateMatrix90(current)
            }
            return null
        }

        // Candidate 1: Tight bounding box of dark QR modules inside the bitmap
        var minDarkX = w
        var maxDarkX = -1
        var minDarkY = h
        var maxDarkY = -1
        for (y in 0 until h) {
            for (x in 0 until w) {
                if (luminanceAt(x, y) < threshold) {
                    if (x < minDarkX) minDarkX = x
                    if (x > maxDarkX) maxDarkX = x
                    if (y < minDarkY) minDarkY = y
                    if (y > maxDarkY) maxDarkY = y
                }
            }
        }
        if (maxDarkX > minDarkX && maxDarkY > minDarkY) {
            val boxW = (maxDarkX - minDarkX + 1).toFloat()
            val boxH = (maxDarkY - minDarkY + 1).toFloat()
            if (boxW >= GRID_SIZE && boxH >= GRID_SIZE) {
                val gridTight = sampleBoxToGrid(minDarkX.toFloat(), minDarkY.toFloat(), boxW, boxH)
                tryDecodeWithRotations(gridTight)?.let { return it }
            }
        }

        // Candidate 2: Full bitmap evenly divided into 21x21 modules
        val gridFull = sampleBoxToGrid(0f, 0f, w.toFloat(), h.toFloat())
        tryDecodeWithRotations(gridFull)?.let { return it }

        // Candidate 3: Standard quiet-zone inset ratios
        for (insetRatio in listOf(0.068f, 0.08f, 0.12f, 0.16f)) {
            val insetX = w * insetRatio
            val insetY = h * insetRatio
            val gridInset = sampleBoxToGrid(insetX, insetY, w - insetX * 2f, h - insetY * 2f)
            tryDecodeWithRotations(gridInset)?.let { return it }
        }

        return null
    }
}

/**
 * Dependency-free reversible 21x21 QR matrix canvas powered by `SetPlayQrCodec`.
 */
@Composable
fun TvQrCodeCanvas(
    payload: String,
    ipOverride: String = "",
    portOverride: Int = TvSyncManager.DEFAULT_PORT,
    pinOverride: String = "",
    modifier: Modifier = Modifier
) {
    val matrix = remember(payload, ipOverride, portOverride, pinOverride) {
        val parsed = TvSyncManager.parseQrPairingPayload(payload)
        val ip = ipOverride.ifBlank { parsed?.first ?: "127.0.0.1" }
        val port = if (portOverride > 0) portOverride else (parsed?.second ?: TvSyncManager.DEFAULT_PORT)
        val pin = pinOverride.ifBlank { parsed?.third ?: "000000" }
        SetPlayQrCodec.encodeToMatrix(ip, port, pin)
    }

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(3.dp, AmberGold),
        modifier = modifier
    ) {
        Canvas(
            modifier = Modifier.padding(12.dp)
        ) {
            val gridSize = SetPlayQrCodec.GRID_SIZE
            val cellSize = size.minDimension / gridSize

            for (r in 0 until gridSize) {
                for (c in 0 until gridSize) {
                    if (matrix[r][c]) {
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
