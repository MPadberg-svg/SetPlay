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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.data.remote.VodMovieDetails
import com.example.data.repository.EpisodeResumeInfo
import com.example.ui.theme.AmberGold
import com.example.ui.theme.Champagne
import com.example.ui.theme.DarkGraphite
import com.example.ui.theme.LiveRed
import com.example.ui.theme.MoviesAquamarine
import com.example.ui.theme.MoviesAtlanticTeal
import com.example.ui.theme.MoviesCyanTeal
import com.example.ui.theme.MoviesMintSpring
import com.example.ui.theme.MoviesOceanTeal
import com.example.ui.theme.MoviesSapphireNavy
import com.example.ui.theme.PaleGold
import com.example.ui.theme.SeriesCypressCard
import com.example.ui.theme.SeriesForestSurface
import com.example.ui.theme.SeriesIceAqua
import com.example.ui.theme.SeriesMistTeal
import com.example.ui.theme.SeriesPineBase
import com.example.ui.theme.SeriesSeaJade
import com.example.ui.theme.luxuryFocusableCard
import com.example.util.StreamMetadataParser

@Composable
fun VodScreen(
    sectionTab: VodTab,
    viewModel: VodViewModel,
    activePlaylist: PlaylistEntity?,
    onItemClick: (StreamEntity) -> Unit,
    isParentalUnlocked: Boolean = false,
    onRequestUnlockCategory: ((String, () -> Unit) -> Unit)? = null,
    recentlyWatched: List<StreamEntity>? = null,
    modifier: Modifier = Modifier
) {
    val isMovies = sectionTab == VodTab.MOVIES
    val sectionBaseBg = if (isMovies) MoviesSapphireNavy else SeriesPineBase
    val sectionSurface = if (isMovies) MoviesAtlanticTeal else SeriesForestSurface
    val sectionPrimaryAccent = if (isMovies) MoviesCyanTeal else SeriesSeaJade
    val sectionSecondaryAccent = if (isMovies) MoviesAquamarine else SeriesMistTeal
    val sectionHighlight = if (isMovies) MoviesMintSpring else SeriesIceAqua

    if (activePlaylist == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(sectionBaseBg, sectionSurface)
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (isMovies) Icons.Default.Movie else Icons.Default.VideoLibrary,
                    contentDescription = null,
                    tint = sectionHighlight,
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
    val selectedMovieStream by viewModel.selectedMovieStream.collectAsStateWithLifecycle()
    val movieDetails by viewModel.movieDetails.collectAsStateWithLifecycle()
    val isLoadingMovieDetails by viewModel.isLoadingMovieDetails.collectAsStateWithLifecycle()
    val episodeResumeMap by viewModel.episodeResumeMap.collectAsStateWithLifecycle()
    val fallbackRecentlyWatched by viewModel.recentlyWatched.collectAsStateWithLifecycle()
    val currentLimit by viewModel.currentLimit.collectAsStateWithLifecycle()

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
            episodeResumeMap = episodeResumeMap,
            onSelectSeason = { viewModel.selectSeason(it) },
            onBack = { viewModel.closeSeriesDetails() },
            onToggleFavorite = {
                viewModel.toggleFavorite(selectedSeriesStream!!.id, selectedSeriesStream!!.isFavorite)
            },
            onPlayEpisode = { episode ->
                val savedEpPositionMs = viewModel.repository.getEpisodePositionMs(
                    playlistId = activePlaylist.id,
                    episodeId = episode.episodeId
                )
                val seriesMediaId = seriesDetails?.tmdbOrImdbId?.trim().orEmpty()
                val encodedEpgChannelId = if (seriesMediaId.isNotBlank()) {
                    "${selectedSeriesStream!!.streamId}:${episode.seasonNumber}:${episode.episodeNumber}:$seriesMediaId"
                } else {
                    "${selectedSeriesStream!!.streamId}:${episode.seasonNumber}:${episode.episodeNumber}"
                }
                val playableEpisodeStream = StreamEntity(
                    id = selectedSeriesStream!!.id,
                    playlistId = activePlaylist.id,
                    streamId = episode.episodeId,
                    name = "${selectedSeriesStream!!.name} • S${episode.seasonNumber}E${episode.episodeNumber} - ${episode.title}",
                    streamType = "series",
                    streamIcon = episode.thumbnail.ifBlank { selectedSeriesStream!!.streamIcon },
                    epgChannelId = encodedEpgChannelId,
                    categoryId = selectedSeriesStream!!.categoryId,
                    containerExtension = episode.containerExtension.ifBlank { "mp4" },
                    isFavorite = selectedSeriesStream!!.isFavorite,
                    rating = episode.rating.ifBlank { selectedSeriesStream!!.rating },
                    lastPositionMs = savedEpPositionMs
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

    val resolvedRecentlyWatched = recentlyWatched ?: fallbackRecentlyWatched
    val targetStreamType = if (isMovies) "movie" else "series"
    val continueWatchingItems = remember(resolvedRecentlyWatched, targetStreamType) {
        resolvedRecentlyWatched.filter { it.streamType.equals(targetStreamType, ignoreCase = true) }
    }

    val onSelectCategory: (String?) -> Unit = { catId ->
        if (isMovies) viewModel.selectMovieCategory(catId) else viewModel.selectSeriesCategory(catId)
    }
    val onSearchChange: (String) -> Unit = { q ->
        if (isMovies) viewModel.onMovieSearchQueryChanged(q) else viewModel.onSeriesSearchQueryChanged(q)
    }

    val totalCount = if (isMovies) activePlaylist.vodCount else activePlaylist.seriesCount

    // IMAX Movie Pre-Play Details ModalBottomSheet
    if (isMovies && selectedMovieStream != null) {
        ImaxMovieDossierModalSheet(
            movieStream = selectedMovieStream!!,
            movieDetails = movieDetails,
            isLoading = isLoadingMovieDetails,
            onDismiss = { viewModel.closeMovieDetails() },
            onWatchMovie = {
                val streamToPlay = selectedMovieStream!!
                val extFromDetails = movieDetails?.containerExtension?.takeIf { it.isNotBlank() } ?: streamToPlay.containerExtension
                val tmdbOrImdb = movieDetails?.tmdbOrImdbId?.takeIf { it.isNotBlank() } ?: streamToPlay.epgChannelId
                viewModel.closeMovieDetails()
                onItemClick(
                    streamToPlay.copy(
                        containerExtension = extFromDetails,
                        epgChannelId = tmdbOrImdb
                    )
                )
            },
            onToggleFavorite = {
                viewModel.toggleFavorite(selectedMovieStream!!.id, selectedMovieStream!!.isFavorite)
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    if (isMovies) {
                        listOf(MoviesSapphireNavy, MoviesAtlanticTeal.copy(alpha = 0.65f), MoviesSapphireNavy)
                    } else {
                        listOf(SeriesPineBase, SeriesForestSurface, SeriesPineBase)
                    }
                )
            )
            .testTag(if (isMovies) "movies_screen" else "series_screen")
    ) {
        val activeCategoryLabel = categories.find { it.categoryId == selectedCatId }?.categoryName?.let {
            StreamMetadataParser.cleanDisplayTitle(it)
        } ?: if (isMovies) "All Cinema" else "All Series"

        // Sleek 36.dp Single-Line Glass Telemetry Bar (Reclaims 35% of Vertical Screen Space)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = (if (isMovies) MoviesAtlanticTeal else SeriesCypressCard).copy(alpha = 0.86f),
            border = BorderStroke(
                width = 0.8.dp,
                color = sectionHighlight.copy(alpha = 0.32f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .height(36.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = if (isMovies) {
                                listOf(MoviesSapphireNavy, MoviesAtlanticTeal, MoviesOceanTeal.copy(alpha = 0.78f))
                            } else {
                                listOf(SeriesPineBase, SeriesForestSurface, SeriesCypressCard)
                            }
                        )
                    )
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isMovies) Icons.Default.LocalMovies else Icons.Default.Layers,
                        contentDescription = null,
                        tint = sectionHighlight,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isMovies) "CINEMA" else "SERIES",
                        color = sectionHighlight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.4.sp
                    )
                    Text(
                        text = "•",
                        color = Champagne.copy(alpha = 0.45f),
                        fontSize = 10.sp
                    )
                    Text(
                        text = if (isMovies) {
                            "%,d Movies • %s".format(totalCount, activeCategoryLabel)
                        } else {
                            "%,d Series • %s".format(totalCount, activeCategoryLabel)
                        },
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.6.dp, sectionHighlight.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "${categories.size} GENRES",
                        color = sectionHighlight,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp)
                    )
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
                        color = sectionHighlight.copy(alpha = 0.65f),
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = sectionHighlight
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = sectionHighlight)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = sectionHighlight,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                    focusedContainerColor = sectionSurface,
                    unfocusedContainerColor = sectionSurface.copy(alpha = 0.85f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vod_search_input")
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Horizontal Scrolling Row of Luxury Category Capsules (Multi-Stop Gradient Active vs Frosted Glass Inactive)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "__all_vod_categories__") {
                val isSelected = selectedCatId == null
                Surface(
                    color = if (isSelected) Color.Transparent else sectionSurface.copy(alpha = 0.78f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        width = if (isSelected) 1.dp else 0.8.dp,
                        color = if (isSelected) Color.White.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.10f)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .then(
                            if (isSelected) {
                                Modifier.background(
                                    Brush.horizontalGradient(
                                        if (isMovies) {
                                            listOf(MoviesMintSpring, MoviesCyanTeal)
                                        } else {
                                            listOf(SeriesIceAqua, SeriesMistTeal)
                                        }
                                    )
                                )
                            } else {
                                Modifier
                            }
                        )
                        .clickable { onSelectCategory(null) }
                ) {
                    Text(
                        text = if (isMovies) "All Movies" else "All Series",
                        color = if (isSelected) sectionBaseBg else Champagne,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.5.dp)
                    )
                }
            }
            items(categories, key = { it.categoryId }) { cat ->
                val isSelected = selectedCatId == cat.categoryId
                Surface(
                    color = if (isSelected) Color.Transparent else sectionSurface.copy(alpha = 0.78f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        width = if (isSelected) 1.dp else 0.8.dp,
                        color = if (isSelected) Color.White.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.10f)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .then(
                            if (isSelected) {
                                Modifier.background(
                                    Brush.horizontalGradient(
                                        if (isMovies) {
                                            listOf(MoviesMintSpring, MoviesCyanTeal)
                                        } else {
                                            listOf(SeriesIceAqua, SeriesMistTeal)
                                        }
                                    )
                                )
                            } else {
                                Modifier
                            }
                        )
                        .clickable {
                            if (isSelected) {
                                onSelectCategory(null)
                            } else if (cat.isLocked && !isParentalUnlocked) {
                                onRequestUnlockCategory?.invoke(cat.categoryId) {
                                    onSelectCategory(cat.categoryId)
                                }
                            } else {
                                onSelectCategory(cat.categoryId)
                            }
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.5.dp)
                    ) {
                        Text(
                            text = cat.categoryName,
                            color = if (isSelected) sectionBaseBg else Champagne,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                        )
                        if (cat.isLocked) {
                            Spacer(modifier = Modifier.width(5.dp))
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked Category",
                                tint = if (isSelected) sectionBaseBg else LiveRed,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Sleek Horizontal "Continue Watching" Rail when at root (selectedCatId == null && searchQuery.isBlank())
        if (selectedCatId == null && searchQuery.isBlank() && continueWatchingItems.isNotEmpty()) {
            ContinueWatchingVodRail(
                items = continueWatchingItems,
                isMovies = isMovies,
                onItemClick = { item ->
                    if (isMovies) {
                        onItemClick(item)
                    } else {
                        viewModel.openSeriesDetails(item, activePlaylist)
                    }
                }
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Grid of Tailored Section Cards with Progressive Paging
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
                        tint = sectionHighlight,
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
                            "Tap 'Sync Catalog' on the KickOff screen or Accounts page."
                        } else {
                            "Try clearing the search filter or selecting another category."
                        },
                        color = sectionHighlight.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            val gridState = rememberLazyGridState()
            val shouldLoadMore by remember(items.size, currentLimit) {
                derivedStateOf {
                    val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    items.size >= currentLimit && lastVisible >= (items.size - 24).coerceAtLeast(0)
                }
            }
            LaunchedEffect(shouldLoadMore) {
                if (shouldLoadMore) {
                    viewModel.loadMore()
                }
            }

            LazyVerticalGrid(
                state = gridState,
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
                        MoviePosterCard(
                            item = item,
                            onClick = { viewModel.openMovieDetails(item, activePlaylist) },
                            onPlayImmediately = { onItemClick(item) },
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

@Composable
private fun ContinueWatchingVodRail(
    items: List<StreamEntity>,
    isMovies: Boolean,
    onItemClick: (StreamEntity) -> Unit
) {
    val accentColor = if (isMovies) MoviesMintSpring else SeriesIceAqua
    val secondaryAccent = if (isMovies) MoviesCyanTeal else SeriesMistTeal
    val cardBg = if (isMovies) MoviesAtlanticTeal else SeriesCypressCard
    val baseBg = if (isMovies) MoviesSapphireNavy else SeriesPineBase

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("continue_watching_rail")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "CONTINUE WATCHING",
                    color = PaleGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
            Text(
                text = "${items.size} in progress",
                color = secondaryAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items, key = { "cw_${it.id}_${it.streamId}" }) { item ->
                val cleanTitle = remember(item.name) {
                    StreamMetadataParser.cleanDisplayTitle(item.name)
                }
                val progressFraction = remember(item.lastPositionMs) {
                    if (item.lastPositionMs > 0L) {
                        val refDurationMs = if (isMovies) 105 * 60_000f else 45 * 60_000f
                        (item.lastPositionMs.toFloat() / refDurationMs).coerceIn(0.08f, 0.94f)
                    } else {
                        0.15f
                    }
                }
                val resumedMinutes = remember(item.lastPositionMs) {
                    (item.lastPositionMs / 60_000L).toInt().coerceAtLeast(1)
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(0.9.dp, accentColor.copy(alpha = 0.42f)),
                    modifier = Modifier
                        .width(196.dp)
                        .luxuryFocusableCard(
                            accentColor = accentColor,
                            shape = RoundedCornerShape(14.dp),
                            onClick = { onItemClick(item) }
                        )
                        .testTag("continue_watching_card_${item.streamId}")
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(cardBg, baseBg.copy(alpha = 0.94f))
                                    )
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(baseBg)
                                    .border(0.6.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(9.dp)),
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
                                        imageVector = if (isMovies) Icons.Default.Movie else Icons.Default.VideoLibrary,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(9.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cleanTitle,
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (item.lastPositionMs > 0L) {
                                        "Resume • ${resumedMinutes}m in"
                                    } else {
                                        "Resume Playback"
                                    },
                                    color = accentColor,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(accentColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Resume",
                                    tint = baseBg,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        // Progress Bar Along Bottom Edge of Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.5.dp)
                                .background(Color.White.copy(alpha = 0.12f))
                                .testTag("continue_watching_progress_${item.streamId}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressFraction)
                                    .height(3.5.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(accentColor, AmberGold)
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * IMAX Glass Movie Poster Card — Sapphire, Cyan-Teal & Electric Mint Premiere Design
 * Features edge-lit specular gradient border, authentic parsed quality/language micro-badges,
 * and a sculpted backlit circular Play Medallion that plays immediately while tapping the card body
 * opens the IMAX Movie Dossier ModalBottomSheet.
 */
@Composable
fun MoviePosterCard(
    item: StreamEntity,
    onClick: () -> Unit,
    onPlayImmediately: () -> Unit = onClick,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    MovieCinemaPosterCard(
        item = item,
        onClick = onClick,
        onPlayImmediately = onPlayImmediately,
        onToggleFavorite = onToggleFavorite,
        modifier = modifier
    )
}

@Composable
fun MovieCinemaPosterCard(
    item: StreamEntity,
    onClick: () -> Unit,
    onPlayImmediately: () -> Unit = onClick,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cleanTitle = remember(item.name) {
        StreamMetadataParser.cleanDisplayTitle(item.name)
    }
    val languageBadge = remember(item.name) {
        StreamMetadataParser.extractLanguageBadge(item.name)
    }
    val qualityBadge = remember(item.name) {
        StreamMetadataParser.extractQualityBadge(item.name)
    }
    val compactQualityBadge = remember(item.name) {
        StreamMetadataParser.extractCompactQualityBadge(item.name)
    }
    val containerLabel = remember(item.containerExtension) {
        item.containerExtension.ifBlank { "MP4" }.uppercase()
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MoviesAtlanticTeal),
        border = BorderStroke(
            width = 1.1.dp,
            brush = Brush.verticalGradient(
                listOf(
                    if (item.isFavorite) AmberGold.copy(alpha = 0.75f) else MoviesMintSpring.copy(alpha = 0.48f),
                    MoviesCyanTeal.copy(alpha = 0.22f),
                    Color.White.copy(alpha = 0.08f)
                )
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .luxuryFocusableCard(
                accentColor = MoviesMintSpring,
                shape = RoundedCornerShape(16.dp),
                onClick = onClick
            )
            .testTag("vod_item_${item.streamId}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.68f)
                    .background(MoviesSapphireNavy)
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
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = MoviesAquamarine,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                // Bottom-to-top Sapphire Navy vignette scrim so poster art shines
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    MoviesSapphireNavy.copy(alpha = 0.40f),
                                    MoviesSapphireNavy.copy(alpha = 0.96f)
                                ),
                                startY = 110f
                            )
                        )
                )

                // Top-Start Floating Frosted Glass Rating + Authentic Language & Quality Micro-Badge Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    if (item.rating.isNotEmpty() && item.rating != "0" && item.rating != "0.0") {
                        Surface(
                            color = MoviesSapphireNavy.copy(alpha = 0.84f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.6.dp, AmberGold.copy(alpha = 0.55f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.5.dp, vertical = 2.5.dp)
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AmberGold,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(2.5.dp))
                                Text(
                                    text = item.rating.take(3),
                                    color = PaleGold,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    if (compactQualityBadge != null) {
                        Surface(
                            color = MoviesSapphireNavy.copy(alpha = 0.84f),
                            shape = RoundedCornerShape(5.dp),
                            border = BorderStroke(0.6.dp, MoviesMintSpring.copy(alpha = 0.65f))
                        ) {
                            Text(
                                text = compactQualityBadge,
                                color = MoviesMintSpring,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (languageBadge != null) {
                        Surface(
                            color = MoviesSapphireNavy.copy(alpha = 0.82f),
                            shape = RoundedCornerShape(5.dp),
                            border = BorderStroke(0.6.dp, MoviesCyanTeal.copy(alpha = 0.55f))
                        ) {
                            Text(
                                text = languageBadge,
                                color = Champagne,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Top-End Frosted Glass Favorite Button
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .background(MoviesSapphireNavy.copy(alpha = 0.78f), CircleShape)
                        .border(0.6.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (item.isFavorite) AmberGold else Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Bottom-End Sculpted Backlit IMAX Play Medallion (Plays Immediately)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(MoviesMintSpring, MoviesCyanTeal)
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.55f), CircleShape)
                        .clickable { onPlayImmediately() }
                        .testTag("movie_poster_play_medallion_${item.streamId}"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MoviesSapphireNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Play Movie Immediately",
                            tint = MoviesMintSpring,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // Cleaned Movie Title & Authentic Format Footer (Falls back to "CINEMA" and containerExtension.uppercase())
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(MoviesSapphireNavy, Color(0xFF040B16))
                        )
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = qualityBadge ?: languageBadge ?: "CINEMA",
                        color = MoviesMintSpring.copy(alpha = 0.90f),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = containerLabel,
                        color = Champagne.copy(alpha = 0.65f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

/**
 * Luxury IMAX Movie Pre-Play Details ModalBottomSheet.
 * Fetches plot, cast, director, release year, duration, and ★ rating asynchronously from Xtream `get_vod_info`,
 * with instant fallback to `StreamEntity` metadata, plus `[ ▶ Watch Movie ]` and `[ ★ Favorite ]` CTAs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImaxMovieDossierModalSheet(
    movieStream: StreamEntity,
    movieDetails: VodMovieDetails?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onWatchMovie: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val rawTitle = movieDetails?.title?.takeIf { it.isNotBlank() } ?: movieStream.name
    val cleanTitle = remember(rawTitle) { StreamMetadataParser.cleanDisplayTitle(rawTitle) }
    val qualityBadge = remember(movieStream.name) { StreamMetadataParser.extractQualityBadge(movieStream.name) ?: "CINEMA" }
    val languageBadge = remember(movieStream.name) { StreamMetadataParser.extractLanguageBadge(movieStream.name) }
    val containerBadge = (movieDetails?.containerExtension?.takeIf { it.isNotBlank() } ?: movieStream.containerExtension)
        .ifBlank { "MP4" }.uppercase()
    val ratingText = (movieDetails?.rating?.takeIf { it.isNotBlank() && it != "0" } ?: movieStream.rating)
        .takeIf { it.isNotBlank() && it != "0" && it != "0.0" }
    val releaseYear = remember(movieDetails?.releaseDate, movieStream.name) {
        val rawDate = movieDetails?.releaseDate?.trim().orEmpty()
        val yearFromDate = Regex("""\b(19\d{2}|20\d{2})\b""").find(rawDate)?.value
        yearFromDate ?: Regex("""\((19\d{2}|20\d{2})\)""").find(movieStream.name)?.groupValues?.getOrNull(1).orEmpty()
    }
    val durationText = movieDetails?.duration?.trim().orEmpty()
    val genreText = movieDetails?.genre?.trim().orEmpty()
    val directorText = movieDetails?.director?.trim().orEmpty()
    val castText = movieDetails?.cast?.trim().orEmpty()
    val plotText = movieDetails?.plot?.trim().orEmpty()
    val posterUrl = movieDetails?.coverUrl?.takeIf { it.isNotBlank() } ?: movieStream.streamIcon
    val backdropUrl = movieDetails?.backdropUrl?.takeIf { it.isNotBlank() } ?: posterUrl

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MoviesSapphireNavy,
        contentColor = Color.White,
        scrimColor = Color.Black.copy(alpha = 0.76f),
        modifier = Modifier.testTag("movie_dossier_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // 1. Hero Backdrop + Poster Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .background(MoviesAtlanticTeal)
            ) {
                if (backdropUrl.isNotEmpty()) {
                    AsyncImage(
                        model = backdropUrl,
                        contentDescription = cleanTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MoviesSapphireNavy.copy(alpha = 0.30f),
                                    MoviesSapphireNavy.copy(alpha = 0.85f),
                                    MoviesSapphireNavy
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Framed Movie Poster Thumbnail
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MoviesAtlanticTeal),
                        border = BorderStroke(1.dp, MoviesMintSpring.copy(alpha = 0.55f)),
                        modifier = Modifier
                            .width(92.dp)
                            .height(134.dp)
                    ) {
                        if (posterUrl.isNotEmpty()) {
                            AsyncImage(
                                model = posterUrl,
                                contentDescription = cleanTitle,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Movie,
                                    contentDescription = null,
                                    tint = MoviesMintSpring,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        // Badges Row: Quality + Language + Container + Rating
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MoviesMintSpring.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.7.dp, MoviesMintSpring.copy(alpha = 0.65f))
                            ) {
                                Text(
                                    text = qualityBadge,
                                    color = MoviesMintSpring,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            if (languageBadge != null) {
                                Surface(
                                    color = MoviesCyanTeal.copy(alpha = 0.20f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(0.7.dp, MoviesCyanTeal.copy(alpha = 0.55f))
                                ) {
                                    Text(
                                        text = languageBadge,
                                        color = Champagne,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Surface(
                                color = Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.6.dp, Color.White.copy(alpha = 0.20f))
                            ) {
                                Text(
                                    text = containerBadge,
                                    color = Champagne,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            if (ratingText != null) {
                                Surface(
                                    color = AmberGold.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(0.7.dp, AmberGold.copy(alpha = 0.65f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = AmberGold,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = ratingText.take(3),
                                            color = PaleGold,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(7.dp))

                        Text(
                            text = cleanTitle,
                            color = Color.White,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        val metaSubline = buildList {
                            if (releaseYear.isNotBlank()) add(releaseYear)
                            if (durationText.isNotBlank()) add(durationText)
                            if (genreText.isNotBlank()) add(genreText)
                        }.joinToString(" • ")

                        if (metaSubline.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = metaSubline,
                                color = MoviesAquamarine,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // 2. Primary Action Deck: [ ▶ Watch Movie ] CTA + [ ★ Favorite ] Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onWatchMovie,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MoviesMintSpring,
                        contentColor = MoviesSapphireNavy
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("movie_dossier_watch_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (movieStream.lastPositionMs > 60_000L) {
                            "Resume Movie (${movieStream.lastPositionMs / 60_000L}m)"
                        } else {
                            "Watch Movie"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                OutlinedButton(
                    onClick = onToggleFavorite,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.dp,
                        if (movieStream.isFavorite) AmberGold else MoviesCyanTeal.copy(alpha = 0.65f)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MoviesAtlanticTeal.copy(alpha = 0.55f),
                        contentColor = if (movieStream.isFavorite) AmberGold else Color.White
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("movie_dossier_favorite_button")
                ) {
                    Icon(
                        imageVector = if (movieStream.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (movieStream.isFavorite) AmberGold else MoviesMintSpring,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (movieStream.isFavorite) "Favorited" else "Favorite",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 3. Plot, Director, and Cast Dossier Metadata
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isLoading && plotText.isBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MoviesMintSpring,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Fetching synopsis, cast & director from Xtream server…",
                            color = Champagne.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Text(
                        text = plotText.ifBlank {
                            "Stream $cleanTitle in $qualityBadge ($containerBadge) directly from your active Xtream Cinema library with instant multi-language subtitle lookup."
                        },
                        color = Champagne.copy(alpha = 0.92f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (directorText.isNotBlank() || castText.isNotBlank()) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                }

                if (directorText.isNotBlank()) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "Director: ",
                            color = MoviesMintSpring,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = directorText,
                            color = Color.White,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (castText.isNotBlank()) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "Cast: ",
                            color = MoviesMintSpring,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = castText,
                            color = Champagne,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * IMAX Glass Series Vault Card — Pine, Cypress, Sea-Jade & Ice-Aqua Stacked Deck Design
 * Features multi-layer stacked season header bars, edge-lit specular border, floating ★ Rating + SEASONS tag,
 * and a sculpted backlit circular Play Medallion.
 */
@Composable
fun SeriesVaultCard(
    item: StreamEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cleanTitle = remember(item.name) {
        StreamMetadataParser.cleanDisplayTitle(item.name)
    }
    val regionBadge = remember(item.name) {
        StreamMetadataParser.extractRegionOrLangBadge(item.name)
    }
    val compactQualityBadge = remember(item.name) {
        StreamMetadataParser.extractCompactQualityBadge(item.name)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .luxuryFocusableCard(
                accentColor = SeriesIceAqua,
                shape = RoundedCornerShape(16.dp),
                onClick = onClick
            )
            .testTag("vod_item_${item.streamId}")
    ) {
        // Multi-Season Stacked-Deck Accent Bars in Mist-Teal & Ice-Aqua
        Box(
            modifier = Modifier
                .fillMaxWidth(0.80f)
                .height(2.5.dp)
                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                .background(SeriesMistTeal.copy(alpha = 0.65f))
        )
        Spacer(modifier = Modifier.height(1.5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .height(2.5.dp)
                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(SeriesIceAqua, SeriesSeaJade)
                    )
                )
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SeriesCypressCard),
            border = BorderStroke(
                width = 1.1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        if (item.isFavorite) AmberGold.copy(alpha = 0.75f) else SeriesIceAqua.copy(alpha = 0.50f),
                        SeriesSeaJade.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.08f)
                    )
                )
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.68f)
                        .background(SeriesPineBase)
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
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = null,
                                tint = SeriesIceAqua,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    // Bottom-to-top Pine vignette scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        SeriesPineBase.copy(alpha = 0.40f),
                                        SeriesPineBase.copy(alpha = 0.96f)
                                    ),
                                    startY = 110f
                                )
                            )
                    )

                    // Top-Start Floating Frosted Glass Rating + Region + SERIES Micro-Badge Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    ) {
                        if (item.rating.isNotEmpty() && item.rating != "0" && item.rating != "0.0") {
                            Surface(
                                color = SeriesPineBase.copy(alpha = 0.84f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.6.dp, AmberGold.copy(alpha = 0.55f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.5.dp, vertical = 2.5.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = AmberGold,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.5.dp))
                                    Text(
                                        text = item.rating.take(3),
                                        color = PaleGold,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        Surface(
                            color = SeriesPineBase.copy(alpha = 0.82f),
                            shape = RoundedCornerShape(5.dp),
                            border = BorderStroke(0.6.dp, SeriesIceAqua.copy(alpha = 0.55f))
                        ) {
                            Text(
                                text = compactQualityBadge ?: "SERIES",
                                color = SeriesIceAqua,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }

                        if (regionBadge != null) {
                            Surface(
                                color = SeriesPineBase.copy(alpha = 0.82f),
                                shape = RoundedCornerShape(5.dp),
                                border = BorderStroke(0.6.dp, SeriesMistTeal.copy(alpha = 0.55f))
                            ) {
                                Text(
                                    text = regionBadge,
                                    color = Champagne,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Top-End Frosted Glass Favorite Button
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(28.dp)
                            .background(SeriesPineBase.copy(alpha = 0.78f), CircleShape)
                            .border(0.6.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (item.isFavorite) AmberGold else Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Bottom-End Sculpted Backlit Ice-Aqua Play Medallion
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(SeriesIceAqua, SeriesMistTeal)
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.55f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(SeriesPineBase),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Open Seasons",
                                tint = SeriesIceAqua,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                // Cleaned Series Title & Seasons Footer
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(SeriesForestSurface, SeriesPineBase)
                            )
                        )
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Explore Seasons",
                            color = SeriesIceAqua,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ALL EPISODES",
                            color = Champagne.copy(alpha = 0.65f),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
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
    episodeResumeMap: Map<String, EpisodeResumeInfo> = emptyMap(),
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
            .background(SeriesPineBase)
            .testTag("series_detail_screen")
    ) {
        // 1. Series Hero Metadata Header with Pine, Forest & Ice-Aqua Scrim
        Card(
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = SeriesCypressCard),
            border = BorderStroke(1.dp, SeriesMistTeal),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SeriesForestSurface)
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

                // Bottom-to-top Pine & Forest gradient scrim
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    SeriesForestSurface.copy(alpha = 0.45f),
                                    SeriesPineBase.copy(alpha = 0.88f),
                                    SeriesPineBase
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
                        color = SeriesCypressCard.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, SeriesIceAqua),
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
                                tint = SeriesIceAqua,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "All Series",
                                color = SeriesIceAqua,
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
                            color = SeriesSeaJade,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.8.dp, SeriesIceAqua)
                        ) {
                            Text(
                                text = "SERIES VAULT",
                                color = SeriesIceAqua,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier
                                .size(36.dp)
                                .background(SeriesCypressCard.copy(alpha = 0.9f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (seriesStream.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Favorite Series",
                                tint = if (seriesStream.isFavorite) AmberGold else SeriesIceAqua,
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
                                color = SeriesSeaJade,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, SeriesIceAqua)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = SeriesIceAqua,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = ratingText.take(3),
                                        color = SeriesIceAqua,
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
                                color = SeriesIceAqua,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (!seriesDetails?.plot.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = seriesDetails?.plot ?: "",
                            color = SeriesIceAqua.copy(alpha = 0.85f),
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
                    CircularProgressIndicator(color = SeriesIceAqua, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Loading seasons & episodes…",
                        color = SeriesIceAqua,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            return
        }

        // 2. Horizontal Scrolling Row of Season Selector Chips (Ice-Aqua & Cypress)
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
                        borderColor = SeriesMistTeal,
                        selectedBorderColor = SeriesIceAqua
                    ),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SeriesIceAqua,
                        selectedLabelColor = SeriesPineBase,
                        containerColor = SeriesCypressCard,
                        labelColor = SeriesIceAqua
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
                    val resumeKey = "${seriesStream.playlistId}_ep_${episode.episodeId}"
                    val resumeInfo = episodeResumeMap[resumeKey]
                    SeriesEpisodeCard(
                        episode = episode,
                        fallbackCover = seriesStream.streamIcon,
                        resumeInfo = resumeInfo,
                        onPlay = { onPlayEpisode(episode) }
                    )
                }
            }
        }
    }
}

private fun resolveEpisodeResumeUi(
    resumeInfo: EpisodeResumeInfo?,
    durationLabel: String
): Pair<Float, String>? {
    val posMs = resumeInfo?.positionMs ?: 0L
    if (posMs <= 0L) return null

    val parsedDurationMs = run {
        val clean = durationLabel.trim()
        val colonParts = clean.split(":")
        when {
            colonParts.size == 3 -> {
                val h = colonParts[0].trim().toLongOrNull() ?: 0L
                val m = colonParts[1].trim().toLongOrNull() ?: 0L
                val s = colonParts[2].trim().toLongOrNull() ?: 0L
                (h * 3600L + m * 60L + s) * 1000L
            }
            colonParts.size == 2 -> {
                val m = colonParts[0].trim().toLongOrNull() ?: 0L
                val s = colonParts[1].trim().toLongOrNull() ?: 0L
                (m * 60L + s) * 1000L
            }
            else -> {
                val mins = Regex("(\\d+)").find(clean)?.groupValues?.getOrNull(1)?.toLongOrNull() ?: 0L
                mins * 60_000L
            }
        }
    }

    val effectiveDurationMs = when {
        (resumeInfo?.durationMs ?: 0L) > posMs -> resumeInfo!!.durationMs
        parsedDurationMs > posMs -> parsedDurationMs
        else -> (posMs + 15 * 60_000L).coerceAtLeast(45 * 60_000L)
    }

    val progressFraction = (posMs.toFloat() / effectiveDurationMs.toFloat()).coerceIn(0.05f, 0.96f)
    val remainingMinutes = ((effectiveDurationMs - posMs) / 60_000L).toInt().coerceAtLeast(1)
    return progressFraction to "Resume • ${remainingMinutes}m left"
}

@Composable
fun SeriesEpisodeCard(
    episode: SeriesEpisode,
    fallbackCover: String,
    resumeInfo: EpisodeResumeInfo? = null,
    onPlay: () -> Unit
) {
    val resumeUi = remember(resumeInfo, episode.duration) {
        resolveEpisodeResumeUi(resumeInfo, episode.duration)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SeriesCypressCard),
        border = BorderStroke(
            width = 1.dp,
            color = if (resumeUi != null) AmberGold.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.08f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .luxuryFocusableCard(
                accentColor = SeriesIceAqua,
                shape = RoundedCornerShape(14.dp),
                onClick = onPlay
            )
            .testTag("episode_card_${episode.seasonNumber}_${episode.episodeNumber}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(SeriesForestSurface, SeriesCypressCard)
                        )
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Episode Artwork or Number Emblem
                Box(
                    modifier = Modifier
                        .width(104.dp)
                        .height(62.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SeriesPineBase)
                        .border(0.8.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(10.dp)),
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
                            tint = SeriesIceAqua,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Episode Number Frosted Badge
                    Surface(
                        color = SeriesPineBase.copy(alpha = 0.82f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(4.dp)
                    ) {
                        Text(
                            text = "EP ${episode.episodeNumber}",
                            color = SeriesIceAqua,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = episode.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    // Unboxed Inline Episode Metadata
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "S${episode.seasonNumber} • E${episode.episodeNumber}",
                            color = SeriesIceAqua,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (episode.duration.isNotBlank()) {
                            Text(
                                text = "• ${episode.duration}",
                                color = Champagne.copy(alpha = 0.75f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "• ${episode.containerExtension.uppercase()}",
                            color = Champagne.copy(alpha = 0.6f),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (resumeUi != null) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = resumeUi.second,
                            color = AmberGold,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.testTag("episode_resume_label_${episode.seasonNumber}_${episode.episodeNumber}")
                        )
                    } else if (episode.plot.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = episode.plot,
                            color = Champagne.copy(alpha = 0.72f),
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Tier 1 Solid Ice-Aqua Play / Resume Pill Button
                Surface(
                    color = if (resumeUi != null) AmberGold else SeriesIceAqua,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .height(30.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onPlay)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Episode",
                            tint = SeriesPineBase,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (resumeUi != null) "Resume" else "Play",
                            color = SeriesPineBase,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Subtle Episode Progress Bar at the bottom of the card when saved progress exists
            if (resumeUi != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(Color.White.copy(alpha = 0.12f))
                        .testTag("episode_progress_bar_${episode.seasonNumber}_${episode.episodeNumber}")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(resumeUi.first)
                            .height(3.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(SeriesIceAqua, AmberGold)
                                )
                            )
                    )
                }
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
