package com.example.ui.search

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.data.model.PlaylistEntity
import com.example.data.model.StreamEntity
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

@Composable
fun GlobalSearchScreen(
    viewModel: MainViewModel,
    activePlaylist: PlaylistEntity?,
    onStreamClick: (StreamEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (activePlaylist == null) {
        Box(modifier = modifier.fillMaxSize().background(CinemaDarkBackground), contentAlignment = Alignment.Center) {
            Text("No active playlist.", color = TextSecondary)
        }
        return
    }

    var searchQuery by remember { mutableStateOf("") }
    var streamTypeFilter by remember { mutableStateOf<String?>(null) } // null = All, "live", "movie", "series"

    val searchFlow = remember(activePlaylist.id, searchQuery, streamTypeFilter) {
        viewModel.repository.searchAllStreams(activePlaylist.id, searchQuery, streamTypeFilter)
    }
    val results by searchFlow.collectAsStateWithLifecycle(emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaDarkBackground)
            .padding(16.dp)
            .testTag("global_search_screen")
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search across Live, Movies & Series…", color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = NeonCyan) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = Color(0xFF26324A),
                focusedContainerColor = CinemaSurface,
                unfocusedContainerColor = CinemaSurface,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth().testTag("global_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Type filter chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = streamTypeFilter == null,
                onClick = { streamTypeFilter = null },
                label = { Text("All (${results.size})", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NeonCyan,
                    selectedLabelColor = Color.Black,
                    containerColor = CinemaSurface,
                    labelColor = TextPrimary
                )
            )
            FilterChip(
                selected = streamTypeFilter == "live",
                onClick = { streamTypeFilter = if (streamTypeFilter == "live") null else "live" },
                label = { Text("Live Channels", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NeonCyan,
                    selectedLabelColor = Color.Black,
                    containerColor = CinemaSurface,
                    labelColor = TextPrimary
                )
            )
            FilterChip(
                selected = streamTypeFilter == "movie",
                onClick = { streamTypeFilter = if (streamTypeFilter == "movie") null else "movie" },
                label = { Text("Movies (VOD)", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NeonCyan,
                    selectedLabelColor = Color.Black,
                    containerColor = CinemaSurface,
                    labelColor = TextPrimary
                )
            )
            FilterChip(
                selected = streamTypeFilter == "series",
                onClick = { streamTypeFilter = if (streamTypeFilter == "series") null else "series" },
                label = { Text("Series", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NeonCyan,
                    selectedLabelColor = Color.Black,
                    containerColor = CinemaSurface,
                    labelColor = TextPrimary
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (results.isEmpty() && searchQuery.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No results for \"$searchQuery\"", color = TextSecondary, fontSize = 15.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(results, key = { it.id }) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CinemaSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStreamClick(item) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CinemaSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                if (item.streamIcon.isNotEmpty()) {
                                    AsyncImage(
                                        model = item.streamIcon,
                                        contentDescription = item.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (item.streamType == "live") Icons.Default.LiveTv else Icons.Default.Movie,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Surface(
                                    color = when (item.streamType.lowercase()) {
                                        "live" -> LiveRed.copy(alpha = 0.15f)
                                        "movie" -> NeonCyan.copy(alpha = 0.15f)
                                        else -> NeonAmber.copy(alpha = 0.15f)
                                    },
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = item.streamType.uppercase(),
                                        color = when (item.streamType.lowercase()) {
                                            "live" -> LiveRed
                                            "movie" -> NeonCyan
                                            else -> NeonAmber
                                        },
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onStreamClick(item) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(NeonCyan.copy(alpha = 0.15f), CircleShape)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = NeonCyan, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
