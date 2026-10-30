package com.example.ui.vod

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.data.remote.SeriesDetails
import com.example.data.remote.SeriesEpisode
import com.example.ui.theme.AmberGold
import com.example.ui.theme.Champagne
import com.example.ui.theme.CinemaMarqueeGold
import com.example.ui.theme.CinemaRuby
import com.example.ui.theme.CinemaSpotlight
import com.example.ui.theme.CinemaVelvetSurface
import com.example.ui.theme.DarkGraphite
import com.example.ui.theme.MetalCharcoal
import com.example.ui.theme.MetalCharcoalElevated
import com.example.ui.theme.PaleGold
import com.example.ui.theme.SeriesDeepVault
import com.example.ui.theme.SeriesEmerald
import com.example.ui.theme.SeriesJadeAccent
import com.example.ui.theme.SeriesVaultGold

@Composable
fun VodScreen(
    sectionTab: VodTab,
    viewModel: VodViewModel,
    activePlaylist: PlaylistEntity?,
    onItemClick: (StreamEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val isMovies = sectionTab == VodTab.MOVIES
    val sectionPrimaryAccent = if (isMovies) CinemaMarqueeGold else SeriesEmerald
    val sectionSecondaryAccent = if (isMovies) CinemaRuby else SeriesJadeAccent

    if (activePlaylist == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DarkGraphite)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (isMovies) Icons.Default.Movie else Icons.Default.VideoLibrary,
                    contentDescription = null,
                    tint = sectionPrimaryAccent,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Active Xtream Account",
                    color = PaleGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Open the navigation menu or tap the server icon to connect your SetPlay IPTV account.",
                    color = Champagne,
                    fontSize = 13.sp
                )
            }
        }
        return
    }

    val selectedSeriesStream by viewModel.selectedSeriesStream.collectAsStateWithLifecycle()
    val seriesDetails by viewModel.seriesDetails.collectAsStateWithLifecycle()
    val isLoadingSeriesDetails by viewModel.isLoadingSeriesDetails.collectAsStateWithLifecycle()
    val selectedSeasonNumber by viewModel.selectedSeasonNumber.collectAsStateWithLifecycle()

    // If in Series tab and a Series item is selected, display the Series Detail hierarchy view
    if (!isMovies && selectedSeriesStream != null) {
        BackHandler {
            viewModel.closeSeriesDetails()
        }
        SeriesDetailHierarchyScreen(
            seriesStream = selectedSeriesStream!!,
            seriesDetails = seriesDetails,
            isLoading = isLoadingSeriesDetails,
            selectedSeasonNumber = selectedSeasonNumber,
            onSelectSeason = { viewModel.selectSeason(it) },
            onBack = { viewModel.closeSeriesDetails() },
            onToggleFavorite = {
                viewModel.toggleFavorite(selectedSeriesStream!!.id, selectedSeriesStream!!.isFavorite)
            },
            onPlayEpisode = { episode ->
                val playableEpisodeStream = StreamEntity(
                    id = selectedSeriesStream!!.id,
                    playlistId = activePlaylist.id,
                    streamId = episode.episodeId,
                    name = "${selectedSeriesStream!!.name} • S${episode.seasonNumber}E${episode.episodeNumber} - ${episode.title}",
                    streamType = "series",
                    streamIcon = episode.thumbnail.ifBlank { selectedSeriesStream!!.streamIcon },
                    epgChannelId = "",
                    categoryId = selectedSeriesStream!!.categoryId,
                    containerExtension = episode.containerExtension.ifBlank { "mp4" },
                    isFavorite = selectedSeriesStream!!.isFavorite,
                    rating = episode.rating.ifBlank { selectedSeriesStream!!.rating }
                )
                onItemClick(playableEpisodeStream)
            },
            modifier = modifier
        )
        return
    }

    val categories by (if (isMovies) viewModel.movieCategories else viewModel.seriesCategories).collectAsStateWithLifecycle()
    val items by (if (isMovies) viewModel.movieItems else viewModel.seriesItems).collectAsStateWithLifecycle()
    val selectedCatId by (if (isMovies) viewModel.selectedMovieCategoryId else viewModel.selectedSeriesCategoryId).collectAsStateWithLifecycle()
    val searchQuery by (if (isMovies) viewModel.movieSearchQuery else viewModel.seriesSearchQuery).collectAsStateWithLifecycle()

    val onSelectCategory: (String?) -> Unit = { catId ->
        if (isMovies) viewModel.selectMovieCategory(catId) else viewModel.selectSeriesCategory(catId)
    }
    val onSearchChange: (String) -> Unit = { q ->
        if (isMovies) viewModel.onMovieSearchQueryChanged(q) else viewModel.onSeriesSearchQueryChanged(q)
    }

    val totalCount = if (isMovies) activePlaylist.vodCount else activePlaylist.seriesCount

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkGraphite)
            .testTag(if (isMovies) "movies_screen" else "series_screen")
    ) {
        // Section-Specific Luxury Header Banner (Ruby Cinema Gold vs Emerald Royal Gold)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MetalCharcoal),
            border = BorderStroke(
                width = 1.dp,
                brush = if (isMovies) {
                    Brush.horizontalGradient(listOf(CinemaRuby, CinemaMarqueeGold, CinemaRuby.copy(alpha = 0.5f)))
                } else {
                    Brush.horizontalGradient(listOf(SeriesEmerald, AmberGold, SeriesJadeAccent.copy(alpha = 0.5f)))
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = if (isMovies) {
                                listOf(CinemaVelvetSurface, MetalCharcoal, DarkGraphite)
                            } else {
                                listOf(SeriesDeepVault, MetalCharcoal, DarkGraphite)
                            }
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            color = if (isMovies) CinemaRuby.copy(alpha = 0.28f) else SeriesEmerald.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(
                                1.dp,
                                if (isMovies) CinemaMarqueeGold else SeriesJadeAccent
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = if (isMovies) Icons.Default.LocalMovies else Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = if (isMovies) CinemaSpotlight else SeriesJadeAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isMovies) "RUBY CINEMA PREMIERE" else "EMERALD SERIES VAULT",
                                    color = if (isMovies) CinemaSpotlight else SeriesJadeAccent,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.6.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isMovies) "Cinema & Box Office Movies" else "VIP Series & Multi-Season Shows",
                            color = PaleGold,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (isMovies) {
                                "Widescreen theatre releases • Instant 4K UHD playback"
                            } else {
                                "Tap any show card to explore seasons & episode hierarchies"
                            },
                            color = Champagne,
                            fontSize = 11.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            color = if (isMovies) CinemaMarqueeGold else SeriesEmerald,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isMovies) "%,d MOVIES".format(totalCount) else "%,d SERIES".format(totalCount),
                                color = if (isMovies) DarkGraphite else PaleGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${categories.size} Categories",
                            color = if (isMovies) CinemaSpotlight else SeriesJadeAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Section-Tinted Search Bar
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    val selectedCatName = categories.find { it.categoryId == selectedCatId }?.categoryName
                    Text(
                        text = when {
                            selectedCatName != null -> "Search in $selectedCatName…"
                            isMovies -> "Search %,d+ cinema movies…".format(totalCount)
                            else -> "Search %,d+ series & shows…".format(totalCount)
                        },
                        color = Champagne.copy(alpha = 0.65f),
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (isMovies) CinemaMarqueeGold else SeriesJadeAccent
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Champagne)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = sectionSecondaryAccent,
                    unfocusedBorderColor = sectionPrimaryAccent.copy(alpha = 0.45f),
                    focusedContainerColor = MetalCharcoal,
                    unfocusedContainerColor = MetalCharcoal,
                    focusedTextColor = PaleGold,
                    unfocusedTextColor = PaleGold
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vod_search_input")
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Horizontal Scrolling Row of Pill-Shaped Category Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "__all_vod_categories__") {
                val isSelected = selectedCatId == null
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectCategory(null) },
                    label = {
                        Text(
                            text = if (isMovies) "All Movies" else "All Series",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                        )
                    },
                    shape = RoundedCornerShape(50),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = Champagne.copy(alpha = 0.35f),
                        selectedBorderColor = if (isMovies) CinemaRuby else SeriesJadeAccent
                    ),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (isMovies) CinemaMarqueeGold else AmberGold,
                        selectedLabelColor = DarkGraphite,
                        containerColor = MetalCharcoal,
                        labelColor = Champagne
                    )
                )
            }
            items(categories, key = { it.categoryId }) { cat ->
                val isSelected = selectedCatId == cat.categoryId
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectCategory(if (isSelected) null else cat.categoryId) },
                    label = {
                        Text(
                            text = cat.categoryName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                        )
                    },
                    shape = RoundedCornerShape(50),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = Champagne.copy(alpha = 0.35f),
                        selectedBorderColor = if (isMovies) CinemaRuby else SeriesJadeAccent
                    ),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (isMovies) CinemaMarqueeGold else AmberGold,
                        selectedLabelColor = DarkGraphite,
                        containerColor = MetalCharcoal,
                        labelColor = Champagne
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Grid of Tailored Section Cards
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (isMovies) Icons.Default.Movie else Icons.Default.VideoLibrary,
                        contentDescription = null,
                        tint = sectionPrimaryAccent.copy(alpha = 0.7f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (totalCount == 0) {
                            if (isMovies) "No movies synced yet." else "No series synced yet."
                        } else {
                            "No matching titles found."
                        },
                        color = PaleGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (totalCount == 0) {
                            "Tap 'Sync Catalog' on the Home screen or Accounts page."
                        } else {
                            "Try clearing the search filter or selecting another category."
                        },
                        color = Champagne,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("vod_grid"),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    if (isMovies) {
                        MovieCinemaPosterCard(
                            item = item,
                            onClick = { onItemClick(item) },
                            onToggleFavorite = { viewModel.toggleFavorite(item.id, item.isFavorite) }
                        )
                    } else {
                        SeriesVaultCard(
                            item = item,
                            onClick = { viewModel.openSeriesDetails(item, activePlaylist) },
                            onToggleFavorite = { viewModel.toggleFavorite(item.id, item.isFavorite) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tailored Movie Poster Card — Ruby Cinema Gold Premiere Design
 */
@Composable
fun MovieCinemaPosterCard(
    item: StreamEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MetalCharcoal),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.verticalGradient(
                listOf(CinemaRuby.copy(alpha = 0.75f), CinemaMarqueeGold.copy(alpha = 0.45f))
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("vod_item_${item.streamId}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.68f)
                    .background(CinemaVelvetSurface)
            ) {
                if (item.streamIcon.isNotEmpty()) {
                    AsyncImage(
                        model = item.streamIcon,
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = CinemaMarqueeGold.copy(alpha = 0.7f),
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                // Bottom-to-top Ruby & Dark Graphite gradient scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    CinemaVelvetSurface.copy(alpha = 0.55f),
                                    DarkGraphite.copy(alpha = 0.94f)
                                ),
                                startY = 100f
                            )
                        )
                )

                // Top-Start Velvet Ruby Cinema Ribbon
                Surface(
                    color = CinemaRuby.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(bottomEnd = 10.dp),
                    border = BorderStroke(0.5.dp, CinemaMarqueeGold),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = "CINEMA",
                        color = PaleGold,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }

                // Favorite button (Top-End)
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(30.dp)
                        .background(DarkGraphite.copy(alpha = 0.75f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (item.isFavorite) CinemaMarqueeGold else PaleGold,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Bottom-Start Rating Badge
                if (item.rating.isNotEmpty() && item.rating != "0") {
                    Surface(
                        color = DarkGraphite.copy(alpha = 0.88f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.8.dp, CinemaMarqueeGold),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = CinemaMarqueeGold,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = item.rating.take(3),
                                color = PaleGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Bottom-End Marquee Gold Play Button with Velvet Ruby Ring
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(34.dp)
                        .background(CinemaMarqueeGold, CircleShape)
                        .border(1.5.dp, CinemaRuby, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play Movie",
                        tint = DarkGraphite,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            // Movie Title & Ruby-Gold Premiere Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MetalCharcoal)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = item.name,
                    color = PaleGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WATCH MOVIE",
                        color = CinemaSpotlight,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.4.sp
                    )
                    Surface(
                        color = CinemaVelvetSurface,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.5.dp, CinemaMarqueeGold.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = item.containerExtension.uppercase(),
                            color = CinemaMarqueeGold,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tailored Series Vault Card — Emerald Royal Gold Multi-Season Stacked Deck Design
 */
@Composable
fun SeriesVaultCard(
    item: StreamEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("vod_item_${item.streamId}")
    ) {
        // Top Stacked-Deck Accent Bars to visually distinguish multi-season Series cards from single Movies
        Box(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .height(3.dp)
                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                .background(SeriesEmerald.copy(alpha = 0.55f))
        )
        Spacer(modifier = Modifier.height(1.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.91f)
                .height(3.dp)
                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                .background(AmberGold.copy(alpha = 0.65f))
        )

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MetalCharcoal),
            border = BorderStroke(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(SeriesJadeAccent.copy(alpha = 0.75f), AmberGold.copy(alpha = 0.5f))
                )
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.68f)
                        .background(SeriesDeepVault)
                ) {
                    if (item.streamIcon.isNotEmpty()) {
                        AsyncImage(
                            model = item.streamIcon,
                            contentDescription = item.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = null,
                                tint = SeriesJadeAccent.copy(alpha = 0.75f),
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    // Bottom-to-top Deep Emerald & Dark Graphite scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        SeriesDeepVault.copy(alpha = 0.6f),
                                        DarkGraphite.copy(alpha = 0.94f)
                                    ),
                                    startY = 100f
                                )
                            )
                    )

                    // Top-Start Emerald Vault Badge
                    Surface(
                        color = SeriesEmerald.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(bottomEnd = 10.dp),
                        border = BorderStroke(0.5.dp, PaleGold),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = PaleGold,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "SERIES",
                                color = PaleGold,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // Favorite button
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(30.dp)
                            .background(DarkGraphite.copy(alpha = 0.75f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (item.isFavorite) AmberGold else PaleGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Bottom-Start Rating Badge
                    if (item.rating.isNotEmpty() && item.rating != "0") {
                        Surface(
                            color = DarkGraphite.copy(alpha = 0.88f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(0.8.dp, SeriesJadeAccent),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AmberGold,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = item.rating.take(3),
                                    color = PaleGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Bottom-End Emerald-Gold Seasons Emblem
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(34.dp)
                            .background(AmberGold, CircleShape)
                            .border(1.5.dp, SeriesEmerald, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Open Seasons",
                            tint = DarkGraphite,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                // Series Title & Emerald-Gold Seasons Footer
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MetalCharcoal)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = item.name,
                        color = PaleGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EXPLORE SEASONS",
                            color = SeriesJadeAccent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.4.sp
                        )
                        Surface(
                            color = SeriesDeepVault,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(0.5.dp, SeriesJadeAccent.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = "SEASONS",
                                color = PaleGold,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SeriesDetailHierarchyScreen(
    seriesStream: StreamEntity,
    seriesDetails: SeriesDetails?,
    isLoading: Boolean,
    selectedSeasonNumber: Int,
    onSelectSeason: (Int) -> Unit,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onPlayEpisode: (SeriesEpisode) -> Unit,
    modifier: Modifier = Modifier
) {
    val seasons = seriesDetails?.seasons ?: emptyList()
    val activeSeason = seasons.find { it.seasonNumber == selectedSeasonNumber } ?: seasons.firstOrNull()
    val episodes = activeSeason?.episodes ?: emptyList()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkGraphite)
            .testTag("series_detail_screen")
    ) {
        // 1. Series Hero Metadata Header with Emerald Royal Gold Scrim
        Card(
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MetalCharcoal),
            border = BorderStroke(1.dp, SeriesJadeAccent.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SeriesDeepVault)
            ) {
                val coverUrl = seriesDetails?.coverUrl?.ifBlank { seriesStream.streamIcon } ?: seriesStream.streamIcon
                if (coverUrl.isNotEmpty()) {
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = seriesStream.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )
                }

                // Bottom-to-top Emerald Vault + Dark Graphite gradient scrim
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    SeriesDeepVault.copy(alpha = 0.45f),
                                    DarkGraphite.copy(alpha = 0.85f),
                                    DarkGraphite
                                )
                            )
                        )
                )

                // Top Row: Back Button + VIP Series Vault Pill + Favorite Star
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MetalCharcoal.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, SeriesJadeAccent),
                        modifier = Modifier
                            .clickable { onBack() }
                            .testTag("series_detail_back_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Series",
                                tint = SeriesJadeAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "All Series",
                                color = PaleGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = SeriesEmerald,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.8.dp, PaleGold)
                        ) {
                            Text(
                                text = "VIP SERIES VAULT",
                                color = PaleGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier
                                .size(36.dp)
                                .background(MetalCharcoal.copy(alpha = 0.85f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (seriesStream.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Favorite Series",
                                tint = if (seriesStream.isFavorite) AmberGold else PaleGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Series Info Overlay
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 62.dp, start = 16.dp, end = 16.dp, bottom = 14.dp)
                ) {
                    Text(
                        text = seriesDetails?.title?.ifBlank { seriesStream.name } ?: seriesStream.name,
                        color = PaleGold,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val ratingText = seriesDetails?.rating?.ifBlank { seriesStream.rating } ?: seriesStream.rating
                        if (ratingText.isNotEmpty() && ratingText != "0") {
                            Surface(
                                color = AmberGold.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = AmberGold,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = ratingText.take(3),
                                        color = PaleGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (!seriesDetails?.genre.isNullOrBlank()) {
                            Text(
                                text = seriesDetails?.genre ?: "",
                                color = Champagne,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (seasons.isNotEmpty()) {
                            Text(
                                text = "• ${seasons.size} ${if (seasons.size == 1) "Season" else "Seasons"}",
                                color = SeriesJadeAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (!seriesDetails?.plot.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = seriesDetails?.plot ?: "",
                            color = Champagne,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = SeriesJadeAccent, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Loading seasons & episodes…",
                        color = PaleGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            return
        }

        // 2. Horizontal Scrolling Row of Season Selector Chips (Emerald Royal Gold)
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("season_selector_row"),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(seasons, key = { it.seasonNumber }) { season ->
                val isSelected = season.seasonNumber == (activeSeason?.seasonNumber ?: selectedSeasonNumber)
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectSeason(season.seasonNumber) },
                    label = {
                        Text(
                            text = "${season.name} (${season.episodes.size})",
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                        )
                    },
                    shape = RoundedCornerShape(50),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = SeriesJadeAccent.copy(alpha = 0.45f),
                        selectedBorderColor = SeriesJadeAccent
                    ),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AmberGold,
                        selectedLabelColor = DarkGraphite,
                        containerColor = MetalCharcoal,
                        labelColor = Champagne
                    ),
                    modifier = Modifier.testTag("season_chip_${season.seasonNumber}")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Vertical List of Episode Cards
        AnimatedContent(
            targetState = activeSeason?.seasonNumber ?: 1,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "season_episodes_transition",
            modifier = Modifier.fillMaxSize()
        ) { _ ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("episode_vertical_list"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(episodes, key = { "${it.seasonNumber}_${it.episodeNumber}_${it.episodeId}" }) { episode ->
                    SeriesEpisodeCard(
                        episode = episode,
                        fallbackCover = seriesStream.streamIcon,
                        onPlay = { onPlayEpisode(episode) }
                    )
                }
            }
        }
    }
}

@Composable
fun SeriesEpisodeCard(
    episode: SeriesEpisode,
    fallbackCover: String,
    onPlay: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MetalCharcoal),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                listOf(SeriesEmerald.copy(alpha = 0.6f), AmberGold.copy(alpha = 0.35f))
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() }
            .testTag("episode_card_${episode.seasonNumber}_${episode.episodeNumber}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Episode Artwork or Number Emblem
            Box(
                modifier = Modifier
                    .width(108.dp)
                    .height(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SeriesDeepVault)
                    .border(1.dp, SeriesJadeAccent.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                val thumbUrl = episode.thumbnail.ifBlank { fallbackCover }
                if (thumbUrl.isNotEmpty()) {
                    AsyncImage(
                        model = thumbUrl,
                        contentDescription = episode.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = SeriesJadeAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Episode Number Badge
                Surface(
                    color = SeriesEmerald.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(4.dp)
                ) {
                    Text(
                        text = "EP ${episode.episodeNumber}",
                        color = PaleGold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = episode.title,
                    color = PaleGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Season ${episode.seasonNumber} • Episode ${episode.episodeNumber}",
                        color = SeriesJadeAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (episode.duration.isNotBlank()) {
                        Text(
                            text = "• ${episode.duration}",
                            color = AmberGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Surface(
                        color = DarkGraphite,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.5.dp, Champagne.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = episode.containerExtension.uppercase(),
                            color = Champagne,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                if (episode.plot.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = episode.plot,
                        color = Champagne.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Amber Gold Play Button with Emerald Ring
            Button(
                onClick = onPlay,
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberGold,
                    contentColor = DarkGraphite
                ),
                modifier = Modifier
                    .size(40.dp)
                    .border(1.5.dp, SeriesEmerald, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Episode",
                    tint = DarkGraphite,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun VodPosterCard(
    item: StreamEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (item.streamType.equals("series", ignoreCase = true)) {
        SeriesVaultCard(
            item = item,
            onClick = onClick,
            onToggleFavorite = onToggleFavorite,
            modifier = modifier
        )
    } else {
        MovieCinemaPosterCard(
            item = item,
            onClick = onClick,
            onToggleFavorite = onToggleFavorite,
            modifier = modifier
        )
    }
}
