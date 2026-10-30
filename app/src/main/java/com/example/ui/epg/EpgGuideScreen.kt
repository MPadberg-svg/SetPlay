package com.example.ui.epg

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.EpgProgramEntity
import com.example.data.model.PlaylistEntity
import com.example.data.model.StreamEntity
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
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EpgGuideScreen(
    viewModel: MainViewModel,
    activePlaylist: PlaylistEntity?,
    onChannelClick: (StreamEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (activePlaylist == null) {
        Box(modifier = modifier.fillMaxSize().background(CinemaDarkBackground), contentAlignment = Alignment.Center) {
            Text("No active playlist.", color = TextSecondary)
        }
        return
    }

    val categories by viewModel.repository.getCategories(activePlaylist.id, "LIVE").collectAsStateWithLifecycle(emptyList())
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }

    val channels by viewModel.repository.searchLiveChannels(activePlaylist.id, "", selectedCategoryId)
        .collectAsStateWithLifecycle(emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaDarkBackground)
            .padding(16.dp)
            .testTag("epg_guide_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(NeonCyan.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Electronic Program Guide (EPG)",
                    color = TextPrimary,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Live timetable, program progress & catchup archive",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Category filter row for EPG
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedCategoryId == null,
                    onClick = { selectedCategoryId = null },
                    label = { Text("All Guide", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan,
                        selectedLabelColor = Color.Black,
                        containerColor = CinemaSurface,
                        labelColor = TextPrimary
                    )
                )
            }
            items(categories, key = { it.categoryId }) { cat ->
                val isSelected = selectedCategoryId == cat.categoryId
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategoryId = if (isSelected) null else cat.categoryId },
                    label = { Text(cat.categoryName, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan,
                        selectedLabelColor = Color.Black,
                        containerColor = CinemaSurface,
                        labelColor = TextPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (channels.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No channels loaded for TV Guide.",
                        color = TextSecondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Sync the playlist to view the live EPG timetable.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(channels.take(100), key = { it.id }) { channel ->
                    EpgChannelRow(
                        channel = channel,
                        playlist = activePlaylist,
                        viewModel = viewModel,
                        onChannelClick = { onChannelClick(channel) }
                    )
                }
            }
        }
    }
}

@Composable
fun EpgChannelRow(
    channel: StreamEntity,
    playlist: PlaylistEntity,
    viewModel: MainViewModel,
    onChannelClick: () -> Unit
) {
    val upcoming by viewModel.repository.getUpcomingEpg(playlist.id, channel.streamId).collectAsStateWithLifecycle(emptyList())
    val scope = rememberCoroutineScope()

    // Lazy fetch EPG when row becomes visible if not cached
    LaunchedEffect(channel.streamId) {
        if (upcoming.isEmpty()) {
            viewModel.repository.refreshEpgForStream(playlist, channel.streamId)
        }
    }

    val nowSec = System.currentTimeMillis() / 1000
    val currentProg = upcoming.firstOrNull { nowSec in it.startEpoch..it.endEpoch } ?: upcoming.firstOrNull()
    val nextProg = if (upcoming.size > 1) upcoming[1] else null

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CinemaSurface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChannelClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CinemaSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    if (channel.streamIcon.isNotEmpty()) {
                        AsyncImage(
                            model = channel.streamIcon,
                            contentDescription = channel.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize().padding(4.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = channel.name,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    if (currentProg != null) {
                        val startStr = if (currentProg.startEpoch > 0) timeFormat.format(Date(currentProg.startEpoch * 1000)) else ""
                        val endStr = if (currentProg.endEpoch > 0) timeFormat.format(Date(currentProg.endEpoch * 1000)) else ""
                        Text(
                            text = "Now: ${currentProg.title} ${if (startStr.isNotEmpty()) "($startStr - $endStr)" else ""}",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).background(LiveRed, CircleShape))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Live Broadcast",
                                color = LiveRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (nextProg != null) {
                        Text(
                            text = "Up Next: ${nextProg.title}",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (channel.tvArchive == 1) {
                    Surface(
                        color = CatchupGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = CatchupGreen,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Catchup",
                                color = CatchupGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onChannelClick,
                    modifier = Modifier
                        .size(34.dp)
                        .background(NeonCyan.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Watch", tint = NeonCyan, modifier = Modifier.size(18.dp))
                }
            }

            // Live EPG Program Progress Bar
            if (currentProg != null && currentProg.endEpoch > currentProg.startEpoch && nowSec >= currentProg.startEpoch) {
                val progress = ((nowSec - currentProg.startEpoch).toFloat() / (currentProg.endEpoch - currentProg.startEpoch).toFloat()).coerceIn(0f, 1f)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    color = NeonCyan,
                    trackColor = CinemaSurfaceElevated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
            }
        }
    }
}
