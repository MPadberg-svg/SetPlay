package com.example.ui.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.theme.AmberGold
import com.example.ui.theme.Champagne
import com.example.ui.theme.CinemaDarkBackground
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaSurfaceElevated
import com.example.ui.theme.LiveRed
import com.example.ui.theme.MoviesAtlanticTeal
import com.example.ui.theme.MoviesCyanTeal
import com.example.ui.theme.MoviesMintSpring
import com.example.ui.theme.MoviesSapphireNavy
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PaleGold
import com.example.ui.theme.SeriesCypressCard
import com.example.ui.theme.SeriesIceAqua
import com.example.ui.theme.SeriesMistTeal
import com.example.ui.theme.SeriesPineBase
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TvBroadcastScarlet
import com.example.ui.theme.TvCrimsonRose
import com.example.ui.theme.TvMidnightSlate
import com.example.ui.theme.TvTwilightIndigo
import com.example.ui.theme.luxuryFocusableCard
import com.example.ui.viewmodel.MainViewModel
import com.example.util.StreamMetadataParser

@Composable
fun GlobalSearchScreen(
    viewModel: MainViewModel,
    activePlaylist: PlaylistEntity?,
    onStreamClick: (StreamEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (activePlaylist == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(CinemaDarkBackground),
            contentAlignment = Alignment.Center
        ) {
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

    val liveResults = remember(results) {
        results.filter { it.streamType.equals("live", ignoreCase = true) }
    }
    val movieResults = remember(results) {
        results.filter { it.streamType.equals("movie", ignoreCase = true) }
    }
    val seriesResults = remember(results) {
        results.filter { it.streamType.equals("series", ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaDarkBackground)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("global_search_screen")
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search across Live TV, Cinema Movies & TV Series…", color = TextMuted) },
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
            modifier = Modifier
                .fillMaxWidth()
                .testTag("global_search_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Type filter chips with live category counts
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = streamTypeFilter == null,
                    onClick = { streamTypeFilter = null },
                    label = { Text("All (${results.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan,
                        selectedLabelColor = Color.Black,
                        containerColor = CinemaSurface,
                        labelColor = TextPrimary
                    )
                )
            }
            item {
                FilterChip(
                    selected = streamTypeFilter == "live",
                    onClick = { streamTypeFilter = if (streamTypeFilter == "live") null else "live" },
                    label = { Text("Live TV Channels (${liveResults.size})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TvBroadcastScarlet,
                        selectedLabelColor = Color.White,
                        containerColor = CinemaSurface,
                        labelColor = TextPrimary
                    )
                )
            }
            item {
                FilterChip(
                    selected = streamTypeFilter == "movie",
                    onClick = { streamTypeFilter = if (streamTypeFilter == "movie") null else "movie" },
                    label = { Text("Cinema Movies (${movieResults.size})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MoviesMintSpring,
                        selectedLabelColor = Color.Black,
                        containerColor = CinemaSurface,
                        labelColor = TextPrimary
                    )
                )
            }
            item {
                FilterChip(
                    selected = streamTypeFilter == "series",
                    onClick = { streamTypeFilter = if (streamTypeFilter == "series") null else "series" },
                    label = { Text("TV Series (${seriesResults.size})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SeriesIceAqua,
                        selectedLabelColor = Color.Black,
                        containerColor = CinemaSurface,
                        labelColor = TextPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (results.isEmpty() && searchQuery.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No results for \"$searchQuery\"", color = TextSecondary, fontSize = 15.sp)
            }
        } else if (streamTypeFilter == null) {
            // 3-Carousel Categorized Global Search View (Live TV Channels, Cinema Movies, TV Series)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (liveResults.isNotEmpty()) {
                    item(key = "section_live_tv") {
                        CategorizedSearchCarouselSection(
                            title = "Live TV Channels",
                            count = liveResults.size,
                            icon = Icons.Default.LiveTv,
                            accentColor = TvCrimsonRose,
                            sectionTestTag = "search_section_live"
                        ) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(liveResults, key = { "live_${it.id}" }) { item ->
                                    GlobalSearchLiveChannelCard(
                                        item = item,
                                        onClick = { onStreamClick(item) }
                                    )
                                }
                            }
                        }
                    }
                }

                if (movieResults.isNotEmpty()) {
                    item(key = "section_cinema_movies") {
                        CategorizedSearchCarouselSection(
                            title = "Cinema Movies",
                            count = movieResults.size,
                            icon = Icons.Default.Movie,
                            accentColor = MoviesMintSpring,
                            sectionTestTag = "search_section_movies"
                        ) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(movieResults, key = { "movie_${it.id}" }) { item ->
                                    GlobalSearchPosterCarouselCard(
                                        item = item,
                                        isSeries = false,
                                        onClick = { onStreamClick(item) }
                                    )
                                }
                            }
                        }
                    }
                }

                if (seriesResults.isNotEmpty()) {
                    item(key = "section_tv_series") {
                        CategorizedSearchCarouselSection(
                            title = "TV Series",
                            count = seriesResults.size,
                            icon = Icons.Default.VideoLibrary,
                            accentColor = SeriesIceAqua,
                            sectionTestTag = "search_section_series"
                        ) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(seriesResults, key = { "series_${it.id}" }) { item ->
                                    GlobalSearchPosterCarouselCard(
                                        item = item,
                                        isSeries = true,
                                        onClick = { onStreamClick(item) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Filtered single-category vertical list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(results, key = { it.id }) { item ->
                    val cleanTitle = remember(item.name) { StreamMetadataParser.cleanDisplayTitle(item.name) }
                    val qualityBadge = remember(item.name) { StreamMetadataParser.extractQualityBadge(item.name) }
                    val langBadge = remember(item.name) { StreamMetadataParser.extractLanguageBadge(item.name) }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CinemaSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .luxuryFocusableCard(
                                accentColor = NeonCyan,
                                shape = RoundedCornerShape(12.dp),
                                onClick = { onStreamClick(item) }
                            )
                            .testTag("search_result_${item.streamId}")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CinemaSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                if (item.streamIcon.isNotEmpty()) {
                                    AsyncImage(
                                        model = item.streamIcon,
                                        contentDescription = cleanTitle,
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
                                    text = cleanTitle,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = when (item.streamType.lowercase()) {
                                            "live" -> LiveRed.copy(alpha = 0.15f)
                                            "movie" -> NeonCyan.copy(alpha = 0.15f)
                                            else -> NeonAmber.copy(alpha = 0.15f)
                                        },
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = when (item.streamType.lowercase()) {
                                                "live" -> "LIVE TV"
                                                "movie" -> "CINEMA MOVIE"
                                                else -> "TV SERIES"
                                            },
                                            color = when (item.streamType.lowercase()) {
                                                "live" -> LiveRed
                                                "movie" -> NeonCyan
                                                else -> NeonAmber
                                            },
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }

                                    if (qualityBadge != null) {
                                        Surface(
                                            color = Color.White.copy(alpha = 0.08f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = qualityBadge,
                                                color = PaleGold,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    if (langBadge != null) {
                                        Surface(
                                            color = Color.White.copy(alpha = 0.08f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = langBadge,
                                                color = Champagne,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            IconButton(
                                onClick = { onStreamClick(item) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(NeonCyan.copy(alpha = 0.15f), CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorizedSearchCarouselSection(
    title: String,
    count: Int,
    icon: ImageVector,
    accentColor: Color,
    sectionTestTag: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(sectionTestTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Surface(
                color = accentColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.7.dp, accentColor.copy(alpha = 0.45f))
            ) {
                Text(
                    text = "$count matches",
                    color = accentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        content()
    }
}

@Composable
private fun GlobalSearchLiveChannelCard(
    item: StreamEntity,
    onClick: () -> Unit
) {
    val cleanTitle = remember(item.name) { StreamMetadataParser.cleanDisplayTitle(item.name) }
    val qualityBadge = remember(item.name) { StreamMetadataParser.extractCompactQualityBadge(item.name) }
    val langBadge = remember(item.name) { StreamMetadataParser.extractLanguageBadge(item.name) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = TvTwilightIndigo),
        border = BorderStroke(0.9.dp, TvBroadcastScarlet.copy(alpha = 0.42f)),
        modifier = Modifier
            .width(210.dp)
            .luxuryFocusableCard(
                accentColor = TvCrimsonRose,
                shape = RoundedCornerShape(14.dp),
                onClick = onClick
            )
            .testTag("search_result_${item.streamId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(TvTwilightIndigo, TvMidnightSlate)
                    )
                )
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.92f))
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (item.streamIcon.isNotEmpty()) {
                    AsyncImage(
                        model = item.streamIcon,
                        contentDescription = cleanTitle,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.LiveTv,
                        contentDescription = null,
                        tint = TvBroadcastScarlet,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cleanTitle,
                    color = Color.White,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = TvBroadcastScarlet.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "LIVE",
                            color = TvCrimsonRose,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 4.5.dp, vertical = 1.dp)
                        )
                    }
                    if (qualityBadge != null) {
                        Text(
                            text = qualityBadge,
                            color = PaleGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (langBadge != null) {
                        Text(
                            text = "• $langBadge",
                            color = Champagne,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlobalSearchPosterCarouselCard(
    item: StreamEntity,
    isSeries: Boolean,
    onClick: () -> Unit
) {
    val cleanTitle = remember(item.name) { StreamMetadataParser.cleanDisplayTitle(item.name) }
    val qualityBadge = remember(item.name) { StreamMetadataParser.extractCompactQualityBadge(item.name) }
    val langBadge = remember(item.name) { StreamMetadataParser.extractLanguageBadge(item.name) }
    val cardBg = if (isSeries) SeriesCypressCard else MoviesAtlanticTeal
    val baseBg = if (isSeries) SeriesPineBase else MoviesSapphireNavy
    val accent = if (isSeries) SeriesIceAqua else MoviesMintSpring

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(0.9.dp, accent.copy(alpha = 0.45f)),
        modifier = Modifier
            .width(128.dp)
            .luxuryFocusableCard(
                accentColor = accent,
                shape = RoundedCornerShape(14.dp),
                onClick = onClick
            )
            .testTag("search_result_${item.streamId}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.70f)
                    .background(baseBg)
            ) {
                if (item.streamIcon.isNotEmpty()) {
                    AsyncImage(
                        model = item.streamIcon,
                        contentDescription = cleanTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isSeries) Icons.Default.VideoLibrary else Icons.Default.Movie,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                ) {
                    if (qualityBadge != null) {
                        Surface(
                            color = baseBg.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(0.6.dp, accent.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = qualityBadge,
                                color = accent,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.5.dp)
                            )
                        }
                    }
                    if (langBadge != null) {
                        Surface(
                            color = baseBg.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = langBadge,
                                color = Champagne,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.5.dp)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(baseBg)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = cleanTitle,
                    color = Color.White,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isSeries) "SERIES" else (qualityBadge ?: "CINEMA"),
                    color = accent,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

