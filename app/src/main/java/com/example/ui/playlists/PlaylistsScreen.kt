package com.example.ui.playlists

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PlaylistEntity
import com.example.ui.theme.CatchupGreen
import com.example.ui.theme.CinemaDarkBackground
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaSurfaceElevated
import com.example.ui.theme.LiveRed
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ConnectionTestState
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PlaylistsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val playlists by viewModel.allPlaylists.collectAsStateWithLifecycle()
    val activePlaylist by viewModel.activePlaylist.collectAsStateWithLifecycle()
    val syncProgress by viewModel.syncProgress.collectAsStateWithLifecycle()
    val connectionTestState by viewModel.connectionTestState.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingPlaylist by remember { mutableStateOf<PlaylistEntity?>(null) }
    var deletingPlaylist by remember { mutableStateOf<PlaylistEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaDarkBackground)
            .testTag("playlists_screen")
    ) {
        // Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Xtream Accounts",
                    color = TextPrimary,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Manage servers, verify connections & switch active catalog",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = {
                    viewModel.clearConnectionTestState()
                    showAddDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("add_playlist_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Account", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Sync Progress Banner
        AnimatedVisibility(visible = syncProgress.isSyncing || syncProgress.error != null) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (syncProgress.error != null) LiveRed.copy(alpha = 0.18f) else CinemaSurfaceElevated
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("accounts_sync_banner")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (syncProgress.isSyncing) {
                            CircularProgressIndicator(
                                color = NeonCyan,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = LiveRed, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = syncProgress.phase.ifBlank { syncProgress.error ?: "Synchronizing..." },
                            color = if (syncProgress.error != null) LiveRed else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (syncProgress.isSyncing) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            color = NeonCyan,
                            trackColor = Color(0xFF26324A),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        }

        if (playlists.isEmpty()) {
            // Empty State Onboarding with inline Add Xtream Account Form
            EmptyAccountOnboardingView(
                connectionTestState = connectionTestState,
                onTestConnection = { url, user, pass ->
                    viewModel.testConnection(url, user, pass)
                },
                onAddAccount = { name, url, user, pass ->
                    viewModel.addCustomPlaylist(name, url, user, pass)
                }
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("playlists_list")
            ) {
                // 1. Active Account Hero Dashboard
                activePlaylist?.let { active ->
                    item(key = "active_hero_${active.id}") {
                        ActiveAccountHeroDashboard(
                            playlist = active,
                            isSyncing = syncProgress.isSyncing,
                            connectionTestState = connectionTestState,
                            onSync = { viewModel.syncActiveAccount() },
                            onTestServer = {
                                viewModel.testConnection(active.serverUrl, active.username, active.password)
                            },
                            onEdit = {
                                viewModel.clearConnectionTestState()
                                editingPlaylist = active
                            }
                        )
                    }
                }

                // 2. Saved Accounts Switcher Header
                item(key = "saved_accounts_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Saved Accounts (${playlists.size})",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap an account to switch active server",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                // 3. Saved Accounts Cards
                items(playlists, key = { it.id }) { playlist ->
                    val isActive = playlist.id == activePlaylist?.id
                    SavedAccountCard(
                        playlist = playlist,
                        isActive = isActive,
                        isSyncing = syncProgress.isSyncing && isActive,
                        onSwitchActive = { viewModel.switchPlaylist(playlist.id) },
                        onSync = { viewModel.syncSpecificPlaylist(playlist.id) },
                        onEdit = {
                            viewModel.clearConnectionTestState()
                            editingPlaylist = playlist
                        },
                        onDelete = { deletingPlaylist = playlist }
                    )
                }
            }
        }
    }

    // Add Account Modal Dialog
    if (showAddDialog) {
        AccountFormDialog(
            title = "Add Xtream Account",
            confirmButtonText = "Save & Sync",
            initialName = "",
            initialUrl = "",
            initialUser = "",
            initialPass = "",
            connectionTestState = connectionTestState,
            onTestConnection = { url, user, pass ->
                viewModel.testConnection(url, user, pass)
            },
            onConfirm = { name, url, user, pass ->
                viewModel.addCustomPlaylist(name, url, user, pass)
                showAddDialog = false
            },
            onDismiss = {
                viewModel.clearConnectionTestState()
                showAddDialog = false
            }
        )
    }

    // Edit Account Modal Dialog
    editingPlaylist?.let { target ->
        AccountFormDialog(
            title = "Edit Xtream Account",
            confirmButtonText = "Update & Sync",
            initialName = target.name,
            initialUrl = target.serverUrl,
            initialUser = target.username,
            initialPass = target.password,
            connectionTestState = connectionTestState,
            onTestConnection = { url, user, pass ->
                viewModel.testConnection(url, user, pass)
            },
            onConfirm = { name, url, user, pass ->
                viewModel.updatePlaylist(
                    id = target.id,
                    name = name,
                    url = url,
                    user = user,
                    pass = pass,
                    syncAfterUpdate = true
                )
                editingPlaylist = null
            },
            onDismiss = {
                viewModel.clearConnectionTestState()
                editingPlaylist = null
            }
        )
    }

    // Delete Confirmation Dialog
    deletingPlaylist?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingPlaylist = null },
            title = {
                Text("Remove Account?", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to remove \"${target.name}\" (${target.serverUrl})? All cached categories, channels, and favorites for this account will be deleted from the device.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePlaylist(target.id)
                        deletingPlaylist = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LiveRed, contentColor = Color.White),
                    modifier = Modifier.testTag("confirm_delete_playlist_btn")
                ) {
                    Text("Delete Account", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingPlaylist = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CinemaSurface
        )
    }
}

@Composable
fun ActiveAccountHeroDashboard(
    playlist: PlaylistEntity,
    isSyncing: Boolean,
    connectionTestState: ConnectionTestState,
    onSync: () -> Unit,
    onTestServer: () -> Unit,
    onEdit: () -> Unit
) {
    val isHttps = playlist.serverUrl.startsWith("https", ignoreCase = true)
    val formattedExp = remember(playlist.expDate) { formatExpirationDate(playlist.expDate) }
    val formattedSync = remember(playlist.lastSyncTimestamp) { formatLastSync(playlist.lastSyncTimestamp) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CinemaSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, NeonCyan.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            .testTag("active_account_hero_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0F172A),
                            Color(0xFF0284C7).copy(alpha = 0.25f),
                            CinemaSurface
                        )
                    )
                )
                .padding(16.dp)
        ) {
            // Top badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = NeonCyan.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ACTIVE ACCOUNT",
                                color = NeonCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Surface(
                        color = if (isHttps) CatchupGreen.copy(alpha = 0.15f) else NeonAmber.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isHttps) "HTTPS" else "HTTP",
                            color = if (isHttps) CatchupGreen else NeonAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }

                Surface(
                    color = if (playlist.status.equals("Active", true)) CatchupGreen.copy(alpha = 0.18f) else LiveRed.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = playlist.status,
                        color = if (playlist.status.equals("Active", true)) CatchupGreen else LiveRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = playlist.name,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${playlist.serverUrl} • User: ${playlist.username}",
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Account Expiration & Connection Metadata Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CinemaSurfaceElevated.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AccountMetaColumn(label = "Expiration", value = formattedExp)
                AccountMetaColumn(label = "Max Connections", value = playlist.maxConnections.ifBlank { "1" })
                AccountMetaColumn(label = "Last Synced", value = formattedSync)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Catalog Counters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CatalogCountBadge(
                    label = "Live TV",
                    count = playlist.channelCount,
                    color = LiveRed,
                    modifier = Modifier.weight(1f)
                )
                CatalogCountBadge(
                    label = "Movies",
                    count = playlist.vodCount,
                    color = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
                CatalogCountBadge(
                    label = "Series",
                    count = playlist.seriesCount,
                    color = NeonAmber,
                    modifier = Modifier.weight(1f)
                )
            }

            // Connection Test Status Output (if user clicked Test Server)
            ConnectionTestFeedbackBanner(connectionTestState = connectionTestState)

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Sync Catalog, Test Server, Edit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSync,
                    enabled = !isSyncing,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("sync_active_account")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSyncing) "Syncing…" else "Sync Catalog",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onTestServer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("test_active_server_btn")
                ) {
                    Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("edit_active_account_btn")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Account", tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun AccountMetaColumn(label: String, value: String) {
    Column {
        Text(text = label, color = TextMuted, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CatalogCountBadge(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CinemaSurfaceElevated,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "%,d".format(count),
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun SavedAccountCard(
    playlist: PlaylistEntity,
    isActive: Boolean,
    isSyncing: Boolean,
    onSwitchActive: () -> Unit,
    onSync: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val formattedExp = remember(playlist.expDate) { formatExpirationDate(playlist.expDate) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CinemaSurface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (!isActive) onSwitchActive() }
            .then(
                if (isActive) Modifier.border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp)) else Modifier
            )
            .testTag("playlist_card_${playlist.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (isActive) NeonCyan.copy(alpha = 0.18f) else CinemaSurfaceElevated,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.Dns,
                        contentDescription = null,
                        tint = if (isActive) NeonCyan else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = playlist.name,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isActive) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = NeonCyan.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "IN USE",
                                    color = NeonCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "${playlist.serverUrl} • ${playlist.username}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    color = if (playlist.status.equals("Active", true)) CatchupGreen.copy(alpha = 0.15f) else LiveRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = playlist.status,
                        color = if (playlist.status.equals("Active", true)) CatchupGreen else LiveRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Live: %,d".format(playlist.channelCount),
                        color = LiveRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "VOD: %,d".format(playlist.vodCount),
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Series: %,d".format(playlist.seriesCount),
                        color = NeonAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Exp: $formattedExp",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = CinemaSurfaceElevated
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isActive) {
                    Button(
                        onClick = onSwitchActive,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan.copy(alpha = 0.16f),
                            contentColor = NeonCyan
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("switch_playlist_${playlist.id}")
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Switch Active", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = "Currently active catalog",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onSync,
                        enabled = !isSyncing,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("sync_playlist_${playlist.id}")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync Account", tint = NeonCyan, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("edit_playlist_${playlist.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Account", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("delete_playlist_${playlist.id}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Account", tint = LiveRed.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyAccountOnboardingView(
    connectionTestState: ConnectionTestState,
    onTestConnection: (String, String, String) -> Unit,
    onAddAccount: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var serverUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("empty_accounts_onboarding")
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CinemaSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(NeonCyan.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Dns, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Connect Xtream Codes Account",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Enter your provider server URL and credentials below",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Account Name (Optional)") },
                        placeholder = { Text("e.g. Living Room IPTV") },
                        singleLine = true,
                        colors = customFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it },
                        label = { Text("Server URL (http:// or https://)") },
                        placeholder = { Text("http://provider-domain.com:8080") },
                        singleLine = true,
                        colors = customFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_url_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary) },
                        singleLine = true,
                        colors = customFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_user_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility",
                                    tint = TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        colors = customFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_pass_input")
                    )

                    ConnectionTestFeedbackBanner(connectionTestState = connectionTestState)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onTestConnection(serverUrl, username, password) },
                            enabled = serverUrl.isNotBlank() && username.isNotBlank() && password.isNotBlank(),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("onboarding_test_btn")
                        ) {
                            Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Server", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = { onAddAccount(name, serverUrl, username, password) },
                            enabled = serverUrl.isNotBlank() && username.isNotBlank() && password.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("onboarding_connect_btn")
                        ) {
                            Text("Save & Connect", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionTestFeedbackBanner(connectionTestState: ConnectionTestState) {
    when (connectionTestState) {
        is ConnectionTestState.Idle -> {}
        is ConnectionTestState.Testing -> {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CinemaSurfaceElevated, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(color = NeonCyan, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Testing Xtream server authentication…", color = TextSecondary, fontSize = 12.sp)
            }
        }
        is ConnectionTestState.Success -> {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = CatchupGreen.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CatchupGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Server Verified (${connectionTestState.serverProtocol}) • Status: ${connectionTestState.status}",
                            color = CatchupGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Active Connections: ${connectionTestState.activeCons}/${connectionTestState.maxConnections} • Expires: ${formatExpirationDate(connectionTestState.expDate)}",
                        color = TextPrimary,
                        fontSize = 11.sp
                    )
                }
            }
        }
        is ConnectionTestState.Error -> {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = LiveRed.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = LiveRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = connectionTestState.message,
                        color = LiveRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountFormDialog(
    title: String,
    confirmButtonText: String,
    initialName: String,
    initialUrl: String,
    initialUser: String,
    initialPass: String,
    connectionTestState: ConnectionTestState,
    onTestConnection: (String, String, String) -> Unit,
    onConfirm: (String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var serverUrl by remember { mutableStateOf(initialUrl) }
    var username by remember { mutableStateOf(initialUser) }
    var password by remember { mutableStateOf(initialPass) }
    var showPassword by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account / Playlist Name") },
                    placeholder = { Text("e.g. Main Xtream Account") },
                    singleLine = true,
                    colors = customFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_account_name_input")
                )
                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text("Server URL (http:// or https://)") },
                    placeholder = { Text("http://example.com:8080") },
                    singleLine = true,
                    colors = customFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_server_url_input")
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true,
                    colors = customFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_username_input")
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password",
                                tint = TextSecondary
                            )
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    colors = customFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_password_input")
                )

                ConnectionTestFeedbackBanner(connectionTestState = connectionTestState)

                OutlinedButton(
                    onClick = { onTestConnection(serverUrl, username, password) },
                    enabled = serverUrl.isNotBlank() && username.isNotBlank() && password.isNotBlank(),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_test_connection_btn")
                ) {
                    Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test Connection", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, serverUrl, username, password) },
                enabled = serverUrl.isNotBlank() && username.isNotBlank() && password.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                modifier = Modifier.testTag("dialog_save_account_btn")
            ) {
                Text(confirmButtonText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = CinemaSurface
    )
}

fun formatExpirationDate(expEpochSeconds: String): String {
    if (expEpochSeconds.isBlank() || expEpochSeconds.equals("null", true)) return "Unlimited"
    val seconds = expEpochSeconds.toLongOrNull() ?: return expEpochSeconds
    if (seconds <= 0L) return "Unlimited"
    return try {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        sdf.format(Date(seconds * 1000L))
    } catch (_: Exception) {
        expEpochSeconds
    }
}

private fun formatLastSync(timestampMs: Long): String {
    if (timestampMs <= 0L) return "Not synced"
    return try {
        val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        sdf.format(Date(timestampMs))
    } catch (_: Exception) {
        "Synced"
    }
}

@Composable
private fun customFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NeonCyan,
    unfocusedBorderColor = Color(0xFF26324A),
    focusedContainerColor = CinemaSurfaceElevated,
    unfocusedContainerColor = CinemaSurfaceElevated,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary
)
