package com.example.ui.parental

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PlaylistEntity
import com.example.ui.theme.CatchupGreen
import com.example.ui.theme.CinemaDarkBackground
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaSurfaceElevated
import com.example.ui.theme.LiveRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import org.json.JSONArray

@Composable
fun ParentalControlsScreen(
    viewModel: MainViewModel,
    activePlaylist: PlaylistEntity?,
    modifier: Modifier = Modifier
) {
    val parentalSettings by viewModel.repository.parentalSettings.collectAsStateWithLifecycle(null)
    val isSessionUnlocked by viewModel.isParentalUnlockedThisSession.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var showPinDialog by remember { mutableStateOf(!isSessionUnlocked) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    var showChangePinDialog by remember { mutableStateOf(false) }

    val categoriesFlow = remember(activePlaylist?.id) {
        if (activePlaylist != null) {
            viewModel.repository.getCategories(activePlaylist.id, "LIVE")
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }
    val categories by categoriesFlow.collectAsStateWithLifecycle(emptyList())

    val lockedCatIds = remember(parentalSettings?.lockedCategoriesJson) {
        try {
            val arr = JSONArray(parentalSettings?.lockedCategoriesJson ?: "[]")
            val list = mutableSetOf<String>()
            for (i in 0 until arr.length()) list.add(arr.getString(i))
            list
        } catch (_: Exception) {
            emptySet()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaDarkBackground)
            .padding(16.dp)
            .testTag("parental_screen")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(if (parentalSettings?.isEnabled == true) LiveRed.copy(alpha = 0.15f) else CatchupGreen.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (parentalSettings?.isEnabled == true) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = if (parentalSettings?.isEnabled == true) LiveRed else CatchupGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Parental Controls & PIN",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Lock sensitive categories and protect viewing with a 4-digit PIN",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Master Enable Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CinemaSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enable Parental Lock",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Require PIN to view locked channels & adult categories",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Switch(
                    checked = parentalSettings?.isEnabled == true,
                    onCheckedChange = { isChecked ->
                        scope.launch {
                            viewModel.repository.toggleParentalEnabled(isChecked)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = NeonCyan,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = CinemaSurfaceElevated
                    ),
                    modifier = Modifier.testTag("parental_master_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Change PIN button
        Button(
            onClick = { showChangePinDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = CinemaSurfaceElevated, contentColor = TextPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("change_pin_button")
        ) {
            Icon(Icons.Default.Security, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Change PIN (Default: 0000)", fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Locked Categories",
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Select categories that should require PIN authentication",
            color = TextMuted,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize().testTag("categories_lock_list")
        ) {
            items(categories, key = { it.categoryId }) { cat ->
                val isLocked = lockedCatIds.contains(cat.categoryId)
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = CinemaSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (activePlaylist != null) {
                                scope.launch {
                                    viewModel.repository.setCategoryLocked(activePlaylist.id, cat.categoryId, !isLocked)
                                }
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = cat.categoryName,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Icon(
                            imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = if (isLocked) "Locked" else "Unlocked",
                            tint = if (isLocked) LiveRed else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // Change PIN Dialog
    if (showChangePinDialog) {
        var newPin by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = { Text("Set New 4-Digit PIN", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 4) newPin = it },
                    placeholder = { Text("4 Digits (e.g. 1234)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color(0xFF26324A),
                        focusedContainerColor = CinemaSurfaceElevated,
                        unfocusedContainerColor = CinemaSurfaceElevated,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPin.length == 4) {
                            scope.launch {
                                viewModel.repository.updateParentalPin(newPin)
                                showChangePinDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black)
                ) {
                    Text("Save PIN", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CinemaSurface
        )
    }
}
