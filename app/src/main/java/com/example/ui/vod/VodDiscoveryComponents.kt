package com.example.ui.vod

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.CategoryEntity
import com.example.data.model.GroupedVodStream
import com.example.data.model.StreamEntity
import com.example.data.model.VodCollectionEntity
import com.example.data.model.VodGroupingNormalizer
import com.example.ui.theme.AmberGold
import com.example.ui.theme.Champagne
import com.example.ui.theme.LiveRed
import com.example.ui.theme.MoviesAquamarine
import com.example.ui.theme.MoviesAtlanticTeal
import com.example.ui.theme.MoviesCyanTeal
import com.example.ui.theme.MoviesMintSpring
import com.example.ui.theme.MoviesSapphireNavy
import com.example.ui.theme.PaleGold
import com.example.ui.theme.SeriesCypressCard
import com.example.ui.theme.SeriesForestSurface
import com.example.ui.theme.SeriesIceAqua
import com.example.ui.theme.SeriesMistTeal
import com.example.ui.theme.SeriesPineBase
import com.example.ui.theme.SeriesSeaJade
import com.example.ui.theme.calculateVodPosterMinCellDp
import com.example.ui.theme.luxuryFocusableCard
import com.example.ui.theme.tvFocusableControl
import com.example.util.StreamMetadataParser
import java.util.Locale

internal fun formatNormalizedRating(stream: StreamEntity, versionCount: Int = 1): String? {
    if (stream.ratingNumeric <= 0.0) return null
    if (VodGroupingNormalizer.isPlaceholderPerfectRating(stream.ratingNumeric, versionCount)) return null
    if (stream.ratingNumeric >= 9.95) return null
    return String.format(Locale.US, "%.1f", stream.ratingNumeric.coerceAtMost(10.0))
}

internal fun isTrendingPlaceholderRating(stream: StreamEntity, versionCount: Int): Boolean {
    return stream.ratingNumeric >= 9.95 && versionCount >= 5
}

internal fun vodShelfFilterTitle(filter: VodShelfFilter, isMovies: Boolean): String {
    return when (filter) {
        VodShelfFilter.RecentlyAdded -> "Recently Added"
        VodShelfFilter.TopRated -> if (isMovies) "Top Rated Cinema" else "Acclaimed Series"
        VodShelfFilter.Recommended -> "Recommended for You"
        is VodShelfFilter.Genre -> filter.keyword
        is VodShelfFilter.Platform -> filter.keyword
    }
}

internal fun formatDetailNormalizedRating(detailsRating: String?, stream: StreamEntity, versionCount: Int = 1): String? {
    val raw = detailsRating?.trim()?.replace(',', '.')
    val parsed = raw?.toDoubleOrNull()?.takeIf { it > 0.0 && it <= 10.0 }
    val normalized = when {
        parsed == null -> null
        parsed <= 5.0 -> {
            // Provider detail endpoints frequently return 5-star ratings ("4.0", "4.5");
            // normalize to the 10-point scale ("8.0", "9.0") to match poster cards.
            val (_, scaled) = VodGroupingNormalizer.normalizeRatingToTenScale("", raw)
            if (scaled > 0.0) scaled else (parsed * 2.0).coerceAtMost(10.0)
        }
        else -> parsed
    }
    if (normalized != null && !VodGroupingNormalizer.isPlaceholderPerfectRating(normalized, versionCount) && normalized < 9.95) {
        return String.format(Locale.US, "%.1f", normalized)
    }
    return formatNormalizedRating(stream, versionCount)
}

/**
 * Sleek 44.dp Mobile Top Command Bar replacing the legacy stacked telemetry bar,
 * horizontal 179-chip category strip, and manual Refresh button.
 */
@Composable
internal fun VodMobileTopCommandBar(
    isMovies: Boolean,
    activeCategoryLabel: String,
    selectedCategoryId: String?,
    searchQuery: String,
    isSearchExpanded: Boolean,
    forceGridMode: Boolean,
    totalCount: Int,
    sectionBaseBg: Color,
    sectionSurface: Color,
    sectionPrimaryAccent: Color,
    sectionHighlight: Color,
    onOpenCategories: () -> Unit,
    onToggleSearch: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleViewMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pillLabel = if (selectedCategoryId == null) {
        if (isMovies) "Categories • All Movies" else "Categories • All Series"
    } else {
        activeCategoryLabel
    }
    val showSearchField = isSearchExpanded || searchQuery.isNotBlank()
    val showModeToggle = selectedCategoryId == null && searchQuery.isBlank()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .height(44.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Dedicated Category Selector Pill (`vod_categories_drawer_btn`)
            Surface(
                color = sectionSurface.copy(alpha = 0.88f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    width = 0.9.dp,
                    color = if (selectedCategoryId != null) {
                        sectionHighlight.copy(alpha = 0.65f)
                    } else {
                        sectionPrimaryAccent.copy(alpha = 0.45f)
                    }
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onOpenCategories)
                    .testTag("vod_categories_drawer_btn")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Open Categories",
                            tint = sectionHighlight,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = pillLabel,
                            color = Color.White,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = sectionHighlight,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Right 1: Discover / All Grid 1-tap toggle (`vod_view_mode_btn`)
            if (showModeToggle) {
                Surface(
                    color = if (forceGridMode) {
                        sectionPrimaryAccent.copy(alpha = 0.28f)
                    } else {
                        sectionSurface.copy(alpha = 0.86f)
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        width = 0.9.dp,
                        color = if (forceGridMode) {
                            sectionHighlight.copy(alpha = 0.70f)
                        } else {
                            Color.White.copy(alpha = 0.16f)
                        }
                    ),
                    modifier = Modifier
                        .height(40.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onToggleViewMode)
                        .testTag("vod_view_mode_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = if (forceGridMode) Icons.Default.AutoAwesome else Icons.Default.GridView,
                            contentDescription = if (forceGridMode) "Switch to Discover" else "Switch to All Grid",
                            tint = sectionHighlight,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (forceGridMode) "Discover" else "All Grid",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }

            // Right 2: Compact Search Toggle (`vod_search_toggle_btn`)
            Surface(
                color = if (showSearchField) {
                    sectionPrimaryAccent.copy(alpha = 0.30f)
                } else {
                    sectionSurface.copy(alpha = 0.88f)
                },
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    width = 0.9.dp,
                    color = if (showSearchField) {
                        sectionHighlight.copy(alpha = 0.72f)
                    } else {
                        Color.White.copy(alpha = 0.16f)
                    }
                ),
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onToggleSearch)
                    .testTag("vod_search_toggle_btn")
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = if (showSearchField) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = if (showSearchField) "Close Search" else "Search Catalog",
                        tint = sectionHighlight,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Expandable Sleek Inline Search Field (`vod_search_input`)
        AnimatedVisibility(
            visible = showSearchField,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = when {
                                selectedCategoryId != null -> "Search in $activeCategoryLabel…"
                                isMovies && totalCount > 0 -> "Search %,d+ movies…".format(Locale.US, totalCount)
                                isMovies -> "Search movies…"
                                totalCount > 0 -> "Search %,d+ series…".format(Locale.US, totalCount)
                                else -> "Search series…"
                            },
                            color = sectionHighlight.copy(alpha = 0.65f),
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = sectionHighlight,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Search",
                                    tint = sectionHighlight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = sectionHighlight,
                        unfocusedBorderColor = sectionPrimaryAccent.copy(alpha = 0.35f),
                        focusedContainerColor = sectionBaseBg.copy(alpha = 0.85f),
                        unfocusedContainerColor = sectionSurface.copy(alpha = 0.82f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vod_search_input")
                )
            }
        }
    }
}

/**
 * Unobtrusive animated live sync status pill displayed during Pull-to-Refresh,
 * 6-stage background sync telemetry, interrupted resume state, pending updates,
 * brief completion feedback, or sync error with Retry action.
 */
@Composable
internal fun VodSyncStatusPill(
    bannerState: VodSyncBannerState,
    sectionSurface: Color,
    sectionHighlight: Color,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onDismissPendingUpdate: () -> Unit = {}
) {
    val showPendingChip = !bannerState.isSyncing &&
        !bannerState.isError &&
        !bannerState.isInterrupted &&
        !bannerState.isCompleted &&
        bannerState.pendingUpdateAvailable
    val isVisible = bannerState.isSyncing ||
        bannerState.isCompleted ||
        bannerState.isError ||
        bannerState.isInterrupted ||
        showPendingChip
    if (!isVisible) return

    val emeraldAccent = Color(0xFF10B981)
    if (showPendingChip) {
        Surface(
            color = sectionSurface.copy(alpha = 0.94f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(0.9.dp, sectionHighlight.copy(alpha = 0.58f)),
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp)
                .testTag("vod_pending_update_chip")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onRetry)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Updates available",
                        tint = sectionHighlight,
                        modifier = Modifier.size(15.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Updates available • Refresh",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (bannerState.pendingUpdateSummary.isNotBlank()) {
                            Text(
                                text = bannerState.pendingUpdateSummary,
                                color = sectionHighlight.copy(alpha = 0.85f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                IconButton(
                    onClick = onDismissPendingUpdate,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("vod_pending_update_dismiss_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss update notice",
                        tint = Color.White.copy(alpha = 0.72f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        return
    }

    val hasDeterminateProgress = bannerState.isSyncing &&
        !bannerState.isIndeterminate &&
        bannerState.progressPercent in 1..99
    val hasIndeterminateProgress = bannerState.isSyncing && !hasDeterminateProgress

    val borderColor = when {
        bannerState.isError -> LiveRed.copy(alpha = 0.65f)
        bannerState.isInterrupted -> AmberGold.copy(alpha = 0.72f)
        bannerState.isCompleted -> emeraldAccent.copy(alpha = 0.72f)
        else -> sectionHighlight.copy(alpha = 0.48f)
    }
    val bgColor = when {
        bannerState.isError -> Color(0xFF2B1018).copy(alpha = 0.92f)
        bannerState.isInterrupted -> Color(0xFF2C220B).copy(alpha = 0.94f)
        bannerState.isCompleted -> Color(0xFF0A2A22).copy(alpha = 0.94f)
        else -> sectionSurface.copy(alpha = 0.94f)
    }

    val primaryStageText = when {
        bannerState.isError -> "Sync failed • ${bannerState.statusText.ifBlank { "Check connection" }}"
        bannerState.isInterrupted -> "Sync interrupted — Resume"
        bannerState.isSyncing -> bannerState.stageLabel.ifBlank {
            bannerState.statusText.ifBlank { "Syncing catalog…" }
        }
        else -> bannerState.statusText.ifBlank { "Catalog ready" }
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.9.dp, borderColor),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .testTag("vod_sync_status_pill")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    when {
                        hasIndeterminateProgress -> {
                            CircularProgressIndicator(
                                color = sectionHighlight,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        bannerState.isError -> {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Sync Error",
                                tint = LiveRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        bannerState.isInterrupted -> {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync Interrupted",
                                tint = AmberGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        bannerState.isCompleted -> {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Catalog Ready",
                                tint = emeraldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        else -> Unit
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = primaryStageText,
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (bannerState.statsSubtitle.isNotBlank() && !bannerState.isError) {
                            Text(
                                text = bannerState.statsSubtitle,
                                color = Champagne.copy(alpha = 0.80f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (hasDeterminateProgress) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${bannerState.progressPercent}%",
                        color = sectionHighlight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                if (bannerState.isInterrupted) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = AmberGold.copy(alpha = 0.24f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.8.dp, AmberGold.copy(alpha = 0.82f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onRetry)
                            .testTag("vod_sync_resume_btn")
                    ) {
                        Text(
                            text = "Resume",
                            color = PaleGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                if (bannerState.isError) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = LiveRed.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.8.dp, LiveRed.copy(alpha = 0.8f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onRetry)
                            .testTag("vod_sync_retry_btn")
                    ) {
                        Text(
                            text = "Retry",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color.White.copy(alpha = 0.72f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            if (hasDeterminateProgress) {
                LinearProgressIndicator(
                    progress = { bannerState.progressPercent / 100f },
                    color = sectionHighlight,
                    trackColor = Color.White.copy(alpha = 0.14f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(50))
                )
            } else if (hasIndeterminateProgress) {
                LinearProgressIndicator(
                    color = sectionHighlight,
                    trackColor = Color.White.copy(alpha = 0.14f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(50))
                )
            }
        }
    }
}

/**
 * Semi-transparent frosted-glass Searchable Category Side Panel for Mobile Movies & Series.
 * Leaves the main feed partially visible on the right; tapping scrim or close dismisses cleanly.
 */
@Composable
internal fun VodCategoriesSidePanel(
    isMovies: Boolean,
    categories: List<CategoryEntity>,
    categoryCounts: Map<String, Int>,
    selectedCategoryId: String?,
    sectionBaseBg: Color,
    sectionSurface: Color,
    sectionPrimaryAccent: Color,
    sectionHighlight: Color,
    onSelectDiscoverHome: () -> Unit,
    onSelectCategory: (CategoryEntity) -> Unit,
    onDismiss: () -> Unit,
    organizedCategories: Map<VodCategoryBucket, List<CategorizedVodItem>> = emptyMap()
) {
    var filterQuery by remember { mutableStateOf("") }

    val baseOrganized = remember(organizedCategories, categories, categoryCounts) {
        if (organizedCategories.isNotEmpty()) {
            organizedCategories
        } else {
            VodViewModel.organizeVodCategoriesStatic(categories, categoryCounts)
        }
    }

    val filteredBuckets = remember(baseOrganized, filterQuery) {
        val trimmed = filterQuery.trim()
        val result = LinkedHashMap<VodCategoryBucket, List<CategorizedVodItem>>()
        for (bucket in VodCategoryBucket.entries) {
            val items = baseOrganized[bucket].orEmpty()
            val matched = if (trimmed.isEmpty()) {
                items
            } else {
                items.filter { item ->
                    item.cleanName.contains(trimmed, ignoreCase = true) ||
                        item.languageTag.contains(trimmed, ignoreCase = true) ||
                        item.category.categoryName.contains(trimmed, ignoreCase = true)
                }
            }
            if (matched.isNotEmpty()) {
                result[bucket] = matched
            }
        }
        result
    }

    val panelShape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp)
    val panelGradient = if (isMovies) {
        Brush.verticalGradient(
            listOf(
                MoviesSapphireNavy.copy(alpha = 0.96f),
                MoviesAtlanticTeal.copy(alpha = 0.94f),
                MoviesSapphireNavy.copy(alpha = 0.97f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                SeriesPineBase.copy(alpha = 0.96f),
                SeriesForestSurface.copy(alpha = 0.94f),
                SeriesPineBase.copy(alpha = 0.97f)
            )
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(sectionBaseBg.copy(alpha = 0.58f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(0.56f)
                    .widthIn(min = 230.dp, max = 285.dp)
                    .fillMaxHeight()
                    .clip(panelShape)
                    .background(panelGradient)
                    .border(1.dp, sectionPrimaryAccent.copy(alpha = 0.65f), panelShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .testTag("vod_categories_side_panel")
            ) {
                // Header: Title + Category Count + Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isMovies) "Movie Categories" else "Series Categories",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${categories.size} Categories",
                            color = sectionHighlight.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Categories",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Compact Instant Filter Box (`vod_category_search_input`)
                OutlinedTextField(
                    value = filterQuery,
                    onValueChange = { filterQuery = it },
                    placeholder = {
                        Text(
                            text = "Filter categories...",
                            color = sectionHighlight.copy(alpha = 0.62f),
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Filter Categories",
                            tint = sectionHighlight,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    trailingIcon = {
                        if (filterQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { filterQuery = "" },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Filter",
                                    tint = sectionHighlight,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = sectionHighlight,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.14f),
                        focusedContainerColor = sectionBaseBg.copy(alpha = 0.72f),
                        unfocusedContainerColor = sectionBaseBg.copy(alpha = 0.52f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vod_category_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Top Pinned Pill: "✨ Discover Home (All)"
                val isDiscoverActive = selectedCategoryId == null
                Surface(
                    color = if (isDiscoverActive) {
                        sectionPrimaryAccent.copy(alpha = 0.28f)
                    } else {
                        Color.White.copy(alpha = 0.06f)
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = if (isDiscoverActive) 1.1.dp else 0.7.dp,
                        color = if (isDiscoverActive) sectionHighlight else Color.White.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onSelectDiscoverHome()
                            onDismiss()
                        }
                        .testTag("vod_category_discover_all")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (isDiscoverActive) sectionHighlight else Champagne,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Discover Home (All)",
                            color = if (isDiscoverActive) Color.White else Champagne,
                            fontSize = 12.5.sp,
                            fontWeight = if (isDiscoverActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.10f), thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(6.dp))

                // Scrollable 4-Bucket Category List with Language Pills & Exact Item Count Pills
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    filteredBuckets.forEach { (bucket, bucketItems) ->
                        item(key = "vod_bucket_header_${bucket.name}") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, bottom = 3.dp, start = 4.dp, end = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = bucket.title.uppercase(Locale.US),
                                    color = sectionHighlight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.8.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${bucketItems.size}",
                                    color = sectionHighlight.copy(alpha = 0.65f),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        items(bucketItems, key = { it.category.categoryId }) { item ->
                            val cat = item.category
                            val isSelected = selectedCategoryId == cat.categoryId
                            val itemCount = item.itemCount

                            Surface(
                                color = if (isSelected) {
                                    sectionPrimaryAccent.copy(alpha = 0.28f)
                                } else {
                                    Color.White.copy(alpha = 0.04f)
                                },
                                shape = RoundedCornerShape(11.dp),
                                border = BorderStroke(
                                    width = if (isSelected) 1.dp else 0.5.dp,
                                    color = if (isSelected) sectionHighlight else Color.White.copy(alpha = 0.07f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(11.dp))
                                    .clickable {
                                        onSelectCategory(cat)
                                        onDismiss()
                                    }
                                    .testTag("vod_category_row_${cat.categoryId}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        if (cat.isLocked) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Locked Category",
                                                tint = LiveRed,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                        if (item.languageTag.isNotBlank()) {
                                            Surface(
                                                color = sectionPrimaryAccent.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(5.dp),
                                                border = BorderStroke(
                                                    0.5.dp,
                                                    sectionHighlight.copy(alpha = 0.45f)
                                                )
                                            ) {
                                                Text(
                                                    text = item.languageTag,
                                                    color = sectionHighlight,
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    letterSpacing = 0.3.sp,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = item.cleanName,
                                            color = if (isSelected) Color.White else Champagne,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    if (itemCount > 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = if (isSelected) {
                                                sectionHighlight.copy(alpha = 0.24f)
                                            } else {
                                                sectionBaseBg.copy(alpha = 0.65f)
                                            },
                                            shape = RoundedCornerShape(50),
                                            border = BorderStroke(
                                                0.5.dp,
                                                if (isSelected) sectionHighlight.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.12f)
                                            )
                                        ) {
                                            Text(
                                                text = "%,d".format(itemCount),
                                                color = if (isSelected) Color.White else sectionHighlight.copy(alpha = 0.90f),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Mobile Netflix/Prime-Style Curated Discovery Home:
 * - Featured Hero Spotlight Card (`220.dp`, rounded `18.dp`)
 * - Compact `156.dp` Landscape Continue Watching Rail (only when watch history exists)
 * - Bounded Curated Discovery Shelves (`116.dp`-wide 2:3 poster cards)
 * - 1-tap jump into full 3-column catalog grid
 */
@Composable
internal fun VodMobileDiscoveryHome(
    isMovies: Boolean,
    discoveryShelves: List<VodDiscoveryShelf>,
    fallbackGroupedItems: List<GroupedVodStream>,
    continueWatchingItems: List<StreamEntity>,
    sectionBaseBg: Color,
    sectionSurface: Color,
    sectionPrimaryAccent: Color,
    sectionHighlight: Color,
    onWatchNow: (StreamEntity) -> Unit,
    onOpenDetails: (StreamEntity) -> Unit,
    onContinueWatchingClick: (StreamEntity) -> Unit,
    onBrowseAllGrid: () -> Unit,
    onSeeAllShelf: (VodShelfFilter) -> Unit = {},
    onLoadMoreShelf: (String) -> Unit = {},
    movieCollections: List<VodCollectionEntity> = emptyList(),
    onOpenCollection: (VodCollectionEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val spotlightItem = remember(discoveryShelves, fallbackGroupedItems) {
        val topRatedOrRecent = discoveryShelves
            .sortedBy { shelf ->
                when {
                    shelf.id.endsWith("top_rated") -> 0
                    shelf.id.endsWith("recently_added") -> 1
                    else -> 2
                }
            }
            .flatMap { it.items }
        topRatedOrRecent.firstOrNull { it.stream.streamIcon.isNotBlank() }
            ?: fallbackGroupedItems.firstOrNull { it.stream.streamIcon.isNotBlank() }
            ?: topRatedOrRecent.firstOrNull()
            ?: fallbackGroupedItems.firstOrNull()
    }

    val spotlightHeroKey = remember(spotlightItem) {
        spotlightItem?.let { item ->
            item.stream.canonicalGroupKey.ifBlank { "id_${item.stream.id}" }
        }
    }

    val dedupedDiscoveryShelves = remember(discoveryShelves, spotlightHeroKey) {
        if (spotlightHeroKey == null || discoveryShelves.isEmpty()) {
            discoveryShelves
        } else {
            discoveryShelves.mapIndexed { index, shelf ->
                val shouldDeduplicate = index == 0 ||
                    shelf.id.endsWith("top_rated") ||
                    shelf.heroDedupKey == spotlightHeroKey
                if (shouldDeduplicate && shelf.items.size >= 4) {
                    val filtered = shelf.items.filterNot { candidate ->
                        candidate.stream.canonicalGroupKey.ifBlank { "id_${candidate.stream.id}" } == spotlightHeroKey
                    }
                    if (filtered.size != shelf.items.size) shelf.copy(items = filtered) else shelf
                } else {
                    shelf
                }
            }
        }
    }

    val dedupedFallbackItems = remember(fallbackGroupedItems, spotlightHeroKey) {
        val bounded = fallbackGroupedItems.take(24)
        if (spotlightHeroKey != null && bounded.size >= 4) {
            bounded.filterNot { candidate ->
                candidate.stream.canonicalGroupKey.ifBlank { "id_${candidate.stream.id}" } == spotlightHeroKey
            }
        } else {
            bounded
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("vod_discovery_home"),
        contentPadding = PaddingValues(top = 6.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // a) Featured Hero Spotlight Card
        if (spotlightItem != null) {
            item(key = "vod_hero_spotlight_${spotlightItem.stream.id}") {
                VodFeaturedHeroSpotlightCard(
                    grouped = spotlightItem,
                    isMovies = isMovies,
                    sectionBaseBg = sectionBaseBg,
                    sectionSurface = sectionSurface,
                    sectionPrimaryAccent = sectionPrimaryAccent,
                    sectionHighlight = sectionHighlight,
                    onWatchNow = { onWatchNow(spotlightItem.stream) },
                    onOpenDetails = { onOpenDetails(spotlightItem.stream) },
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
            }
        }

        // b) Compact 156.dp Landscape Continue Watching Rail (shown ONLY when items exist)
        if (continueWatchingItems.isNotEmpty()) {
            item(key = "vod_continue_watching_mobile") {
                MobileContinueWatchingRail(
                    items = continueWatchingItems,
                    isMovies = isMovies,
                    sectionBaseBg = sectionBaseBg,
                    sectionSurface = sectionSurface,
                    sectionHighlight = sectionHighlight,
                    onItemClick = onContinueWatchingClick
                )
            }
        }

        // b2) Official Movie Franchises & Collections Horizontal Shelf (Movies tab only)
        if (isMovies && movieCollections.isNotEmpty()) {
            item(key = "vod_movie_collections_mobile") {
                VodMobileCollectionsShelfRow(
                    collections = movieCollections,
                    sectionBaseBg = sectionBaseBg,
                    sectionSurface = sectionSurface,
                    sectionPrimaryAccent = sectionPrimaryAccent,
                    sectionHighlight = sectionHighlight,
                    onOpenCollection = onOpenCollection
                )
            }
        }

        // c) Curated Discovery Shelves with "See All →" & inline horizontal expansion
        items(dedupedDiscoveryShelves, key = { it.id }) { shelf ->
            VodDiscoveryShelfRow(
                shelf = shelf,
                isMovies = isMovies,
                sectionBaseBg = sectionBaseBg,
                sectionSurface = sectionSurface,
                sectionPrimaryAccent = sectionPrimaryAccent,
                sectionHighlight = sectionHighlight,
                onItemClick = { onOpenDetails(it.stream) },
                onSeeAllClick = { onSeeAllShelf(shelf.shelfFilter) },
                onLoadMoreShelf = { onLoadMoreShelf(shelf.id) }
            )
        }

        // Fallback shelf if discovery shelves haven't populated yet (e.g., legacy DB before re-sync)
        if (dedupedDiscoveryShelves.isEmpty() && dedupedFallbackItems.isNotEmpty()) {
            item(key = "vod_fallback_library_shelf") {
                VodDiscoveryShelfRow(
                    shelf = VodDiscoveryShelf(
                        id = "fallback_catalog",
                        title = if (isMovies) "Featured Cinema" else "Featured Series",
                        subtitle = "From your active library",
                        items = dedupedFallbackItems,
                        shelfFilter = VodShelfFilter.RecentlyAdded,
                        canLoadMore = false
                    ),
                    isMovies = isMovies,
                    sectionBaseBg = sectionBaseBg,
                    sectionSurface = sectionSurface,
                    sectionPrimaryAccent = sectionPrimaryAccent,
                    sectionHighlight = sectionHighlight,
                    onItemClick = { onOpenDetails(it.stream) },
                    onSeeAllClick = onBrowseAllGrid,
                    onLoadMoreShelf = {}
                )
            }
        }

        // d) Subtle Footer CTA to Browse Full 3-Column Deduplicated Grid
        if (fallbackGroupedItems.isNotEmpty()) {
            item(key = "vod_browse_all_footer") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = sectionSurface.copy(alpha = 0.80f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.9.dp, sectionPrimaryAccent.copy(alpha = 0.50f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onBrowseAllGrid)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = null,
                                tint = sectionHighlight,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = if (isMovies) "Browse Full Movie Grid" else "Browse Full Series Grid",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VodFeaturedHeroSpotlightCard(
    grouped: GroupedVodStream,
    isMovies: Boolean,
    sectionBaseBg: Color,
    sectionSurface: Color,
    sectionPrimaryAccent: Color,
    sectionHighlight: Color,
    onWatchNow: () -> Unit,
    onOpenDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stream = grouped.stream
    val cleanTitle = remember(stream.name) { VodGroupingNormalizer.extractCleanDisplayTitle(stream.name) }
    val ratingLabel = remember(stream.rating, stream.ratingNumeric, grouped.versionCount) {
        formatNormalizedRating(stream, grouped.versionCount)
    }
    val showTrendingBadge = remember(stream.ratingNumeric, grouped.versionCount) {
        isTrendingPlaceholderRating(stream, grouped.versionCount)
    }
    val yearLabel = stream.releaseYear.takeIf { it > 0 }?.toString()
    val metaSubtitle = remember(stream.genres, stream.versionTag, yearLabel) {
        buildList {
            if (!yearLabel.isNullOrBlank()) add(yearLabel)
            if (stream.genres.isNotBlank()) {
                add(stream.genres.split(",").take(2).joinToString(" • ") { it.trim() })
            } else if (stream.versionTag.isNotBlank()) {
                add(stream.versionTag)
            }
        }.joinToString("  •  ")
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = sectionSurface),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.verticalGradient(
                listOf(
                    sectionHighlight.copy(alpha = 0.55f),
                    sectionPrimaryAccent.copy(alpha = 0.25f),
                    Color.White.copy(alpha = 0.08f)
                )
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable(onClick = onOpenDetails)
            .testTag("vod_hero_spotlight_card")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (stream.streamIcon.isNotBlank()) {
                AsyncImage(
                    model = stream.streamIcon,
                    contentDescription = cleanTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(sectionSurface, sectionBaseBg)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isMovies) Icons.Default.Movie else Icons.Default.VideoLibrary,
                        contentDescription = null,
                        tint = sectionHighlight.copy(alpha = 0.45f),
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            // Atmospheric dark bottom gradient scrim for legible hero typography
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to sectionBaseBg.copy(alpha = 0.18f),
                                0.42f to sectionBaseBg.copy(alpha = 0.56f),
                                0.78f to sectionBaseBg.copy(alpha = 0.90f),
                                1.0f to sectionBaseBg.copy(alpha = 0.98f)
                            )
                        )
                    )
            )

            // Top-Start Badges: Spotlight + Normalized Rating + Version Count
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = sectionBaseBg.copy(alpha = 0.80f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.7.dp, sectionHighlight.copy(alpha = 0.65f))
                ) {
                    Text(
                        text = if (isMovies) "FEATURED FILM" else "FEATURED SERIES",
                        color = sectionHighlight,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.6.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                if (ratingLabel != null) {
                    Surface(
                        color = sectionBaseBg.copy(alpha = 0.84f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.7.dp, AmberGold.copy(alpha = 0.65f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = AmberGold,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = ratingLabel,
                                color = PaleGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                } else if (showTrendingBadge) {
                    Surface(
                        color = sectionBaseBg.copy(alpha = 0.84f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.7.dp, sectionHighlight.copy(alpha = 0.72f))
                    ) {
                        Text(
                            text = "TRENDING",
                            color = sectionHighlight,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }

                if (grouped.versionCount > 1) {
                    Surface(
                        color = sectionPrimaryAccent.copy(alpha = 0.28f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.7.dp, sectionHighlight.copy(alpha = 0.55f))
                    ) {
                        Text(
                            text = "${grouped.versionCount} Versions",
                            color = Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Bottom Title + Metadata + 1-Tap Watch & Details CTAs
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Text(
                    text = cleanTitle,
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 22.sp
                )
                if (metaSubtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = metaSubtitle,
                        color = Champagne.copy(alpha = 0.84f),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onWatchNow,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = sectionHighlight,
                            contentColor = sectionBaseBg
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("vod_hero_watch_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Watch",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenDetails,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = sectionBaseBg.copy(alpha = 0.55f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("vod_hero_details_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = sectionHighlight,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Details",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MobileContinueWatchingRail(
    items: List<StreamEntity>,
    isMovies: Boolean,
    sectionBaseBg: Color,
    sectionSurface: Color,
    sectionHighlight: Color,
    onItemClick: (StreamEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("continue_watching_rail")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
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
                    tint = sectionHighlight,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Continue Watching",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                text = "${items.size} in progress",
                color = sectionHighlight.copy(alpha = 0.80f),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items, key = { "mobile_cw_${it.id}_${it.streamId}" }) { item ->
                val cleanTitle = remember(item.name) { VodGroupingNormalizer.extractCleanDisplayTitle(item.name) }
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

                Column(
                    modifier = Modifier
                        .width(156.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onItemClick(item) }
                        .testTag("continue_watching_card_${item.streamId}")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(88.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(sectionSurface)
                            .border(0.8.dp, sectionHighlight.copy(alpha = 0.32f), RoundedCornerShape(12.dp))
                    ) {
                        if (item.streamIcon.isNotBlank()) {
                            AsyncImage(
                                model = item.streamIcon,
                                contentDescription = cleanTitle,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isMovies) Icons.Default.Movie else Icons.Default.VideoLibrary,
                                    contentDescription = null,
                                    tint = sectionHighlight,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        // Subtle dark gradient for play pill & progress contrast
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, sectionBaseBg.copy(alpha = 0.78f))
                                    )
                                )
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(sectionBaseBg.copy(alpha = 0.80f))
                                .border(1.dp, sectionHighlight.copy(alpha = 0.85f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Resume",
                                tint = sectionHighlight,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Playback progress bar along bottom edge
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .height(3.5.dp)
                                .background(Color.White.copy(alpha = 0.18f))
                                .testTag("continue_watching_progress_${item.streamId}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressFraction)
                                    .fillMaxHeight()
                                    .background(
                                        Brush.horizontalGradient(listOf(sectionHighlight, AmberGold))
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = cleanTitle,
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (item.lastPositionMs > 0L) "Resume • ${resumedMinutes}m in" else "Resume Playback",
                        color = sectionHighlight.copy(alpha = 0.82f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun VodDiscoveryShelfRow(
    shelf: VodDiscoveryShelf,
    isMovies: Boolean,
    sectionBaseBg: Color,
    sectionSurface: Color,
    sectionPrimaryAccent: Color,
    sectionHighlight: Color,
    onItemClick: (GroupedVodStream) -> Unit,
    onSeeAllClick: () -> Unit = {},
    onLoadMoreShelf: () -> Unit = {}
) {
    val rowState = rememberLazyListState()
    var lastExpandedAtSize by remember(shelf.id) { mutableIntStateOf(0) }
    val shouldExpandInline by remember(shelf.items.size, shelf.canLoadMore) {
        derivedStateOf {
            val lastVisible = rowState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            shelf.canLoadMore &&
                shelf.items.size > lastExpandedAtSize &&
                lastVisible >= (shelf.items.size - 4).coerceAtLeast(0)
        }
    }
    LaunchedEffect(shouldExpandInline) {
        if (shouldExpandInline) {
            lastExpandedAtSize = shelf.items.size
            onLoadMoreShelf()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vod_shelf_${shelf.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = shelf.title,
                    color = Color.White,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (shelf.subtitle.isNotBlank()) {
                    Text(
                        text = shelf.subtitle,
                        color = sectionHighlight.copy(alpha = 0.72f),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = sectionSurface.copy(alpha = 0.80f),
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.7.dp, sectionHighlight.copy(alpha = 0.45f)),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onSeeAllClick)
                    .testTag("vod_shelf_see_all_${shelf.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "See All →",
                        color = sectionHighlight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            state = rowState,
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(shelf.items, key = { "${shelf.id}_${it.stream.id}" }) { grouped ->
                DiscoveryShelfPosterCard(
                    grouped = grouped,
                    isMovies = isMovies,
                    sectionBaseBg = sectionBaseBg,
                    sectionSurface = sectionSurface,
                    sectionPrimaryAccent = sectionPrimaryAccent,
                    sectionHighlight = sectionHighlight,
                    onClick = { onItemClick(grouped) }
                )
            }

            // Compact "See All" end-cap card at the tail of the row
            item(key = "${shelf.id}_see_all_end_cap") {
                Box(
                    modifier = Modifier
                        .width(108.dp)
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    sectionSurface.copy(alpha = 0.85f),
                                    sectionBaseBg.copy(alpha = 0.92f)
                                )
                            )
                        )
                        .border(0.9.dp, sectionHighlight.copy(alpha = 0.42f), RoundedCornerShape(12.dp))
                        .clickable(onClick = onSeeAllClick)
                        .testTag("vod_shelf_end_see_all_${shelf.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(sectionPrimaryAccent.copy(alpha = 0.24f))
                                .border(1.dp, sectionHighlight.copy(alpha = 0.7f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "See All ${shelf.title}",
                                tint = sectionHighlight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "See All",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = shelf.title,
                            color = sectionHighlight.copy(alpha = 0.78f),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoveryShelfPosterCard(
    grouped: GroupedVodStream,
    isMovies: Boolean,
    sectionBaseBg: Color,
    sectionSurface: Color,
    sectionPrimaryAccent: Color,
    sectionHighlight: Color,
    onClick: () -> Unit
) {
    val stream = grouped.stream
    val cleanTitle = remember(stream.name) { VodGroupingNormalizer.extractCleanDisplayTitle(stream.name) }
    val ratingLabel = remember(stream.rating, stream.ratingNumeric, grouped.versionCount) {
        formatNormalizedRating(stream, grouped.versionCount)
    }
    val showTrendingBadge = remember(stream.ratingNumeric, grouped.versionCount) {
        isTrendingPlaceholderRating(stream, grouped.versionCount)
    }

    Column(
        modifier = Modifier
            .width(116.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("vod_shelf_card_${stream.streamId}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(12.dp))
                .background(sectionSurface)
                .border(0.8.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(12.dp))
        ) {
            if (stream.streamIcon.isNotBlank()) {
                AsyncImage(
                    model = stream.streamIcon,
                    contentDescription = cleanTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isMovies) Icons.Default.Movie else Icons.Default.VideoLibrary,
                        contentDescription = null,
                        tint = sectionHighlight.copy(alpha = 0.65f),
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            // Subtle vignette scrim for corner micro-pills
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to sectionBaseBg.copy(alpha = 0.25f),
                                0.35f to Color.Transparent,
                                0.72f to Color.Transparent,
                                1.0f to sectionBaseBg.copy(alpha = 0.80f)
                            )
                        )
                    )
            )

            // Top-Start Normalized Rating Pill or TRENDING badge for high-version placeholder 10.0
            if (ratingLabel != null) {
                Surface(
                    color = sectionBaseBg.copy(alpha = 0.84f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.6.dp, AmberGold.copy(alpha = 0.60f)),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 5.5.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AmberGold,
                            modifier = Modifier.size(9.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = ratingLabel,
                            color = PaleGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            } else if (showTrendingBadge) {
                Surface(
                    color = sectionBaseBg.copy(alpha = 0.86f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.6.dp, sectionHighlight.copy(alpha = 0.65f)),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = "TRENDING",
                        color = sectionHighlight,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.4.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Bottom-Start Subtle Multi-Version Micro-Badge (when versionCount > 1)
            if (grouped.versionCount > 1) {
                Surface(
                    color = sectionBaseBg.copy(alpha = 0.86f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(0.6.dp, sectionPrimaryAccent.copy(alpha = 0.75f)),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = "${grouped.versionCount} Versions",
                        color = sectionHighlight,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 5.5.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = cleanTitle,
            color = Color.White,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        val subLine = remember(stream.releaseYear, stream.versionTag) {
            listOfNotNull(
                stream.releaseYear.takeIf { it > 0 }?.toString(),
                stream.versionTag.takeIf { it.isNotBlank() }
            ).joinToString(" • ")
        }
        if (subLine.isNotBlank()) {
            Text(
                text = subLine,
                color = Champagne.copy(alpha = 0.62f),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private val VERSION_TOKEN_LABELS = mapOf(
    "EN" to "English",
    "AR" to "Arabic",
    "ES" to "Spanish",
    "FR" to "French",
    "PT" to "Portuguese",
    "IT" to "Italian",
    "DE" to "German",
    "TR" to "Turkish",
    "KU" to "Kurdish",
    "BG" to "Bulgarian",
    "NL" to "Dutch",
    "RU" to "Russian",
    "PL" to "Polish",
    "RO" to "Romanian",
    "GR" to "Greek",
    "AL" to "Albanian",
    "FA" to "Persian",
    "HI" to "Hindi",
    "IN" to "Hindi",
    "UR" to "Urdu",
    "KR" to "Korean",
    "JP" to "Japanese",
    "CN" to "Chinese",
    "SE" to "Swedish",
    "NO" to "Norwegian",
    "DK" to "Danish",
    "FI" to "Finnish",
    "LAT" to "Latino",
    "UK" to "UK",
    "US" to "US",
    "CA" to "Canada",
    "AU" to "Australia",
    "BR" to "Brazil",
    "MX" to "Mexico",
    "4K" to "4K",
    "UHD" to "UHD",
    "FHD" to "FHD",
    "HEVC" to "HEVC",
    "HDR" to "HDR",
    "SUB" to "Subbed",
    "DUB" to "Dubbed",
    "VOSTFR" to "VOSTFR",
    "MULTI" to "Multi",
    "3D" to "3D",
    "60FPS" to "60fps"
)

internal fun buildBaseVersionLabel(
    version: StreamEntity,
    categoryNameById: Map<String, String>,
    includeContainer: Boolean
): String {
    val categoryName = categoryNameById[version.categoryId].orEmpty()
    val categoryAwareTag = VodGroupingNormalizer.extractVersionTagWithCategory(
        rawName = version.name,
        categoryName = categoryName
    )

    val labelCore = if (categoryAwareTag.isNotBlank()) {
        categoryAwareTag
    } else {
        val rawTag = version.versionTag.ifBlank { VodGroupingNormalizer.extractVersionTag(version.name) }
        val mappedTokens = rawTag
            .split("•", "|", "/", ",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { token -> VERSION_TOKEN_LABELS[token.uppercase(Locale.US)] ?: token }
            .distinct()
        if (mappedTokens.isNotEmpty()) mappedTokens.joinToString(" • ") else "Standard"
    }

    val ext = version.containerExtension.trim().uppercase(Locale.US)
    return if (includeContainer && ext.isNotEmpty()) {
        "$labelCore • $ext"
    } else {
        labelCore
    }
}

internal fun formatVersionChipLabels(
    versions: List<StreamEntity>,
    categoryNameById: Map<String, String>,
    includeContainer: Boolean
): Map<Long, String> {
    val baseLabels = versions.associate { it.id to buildBaseVersionLabel(it, categoryNameById, includeContainer) }
    val counts = baseLabels.values.groupingBy { it }.eachCount()
    val assignedLabels = mutableSetOf<String>()
    val usedIndices = mutableMapOf<String, Int>()
    return versions.associate { version ->
        val base = baseLabels[version.id] ?: "Standard"
        if ((counts[base] ?: 0) <= 1) {
            assignedLabels.add(base)
            version.id to base
        } else {
            val rawCat = categoryNameById[version.categoryId].orEmpty()
            val cleanCat = if (rawCat.isNotBlank()) {
                VodViewModel.parseCategoryLanguageAndName(rawCat).second
                    .takeIf { it.isNotBlank() && it.length <= 18 }
            } else {
                null
            }
            val withCat = if (cleanCat != null && !base.contains(cleanCat, ignoreCase = true)) {
                "$base ($cleanCat)"
            } else {
                null
            }
            val candidate = if (withCat != null && assignedLabels.add(withCat)) {
                withCat
            } else {
                val idx = (usedIndices[base] ?: 0) + 1
                usedIndices[base] = idx
                val numbered = "$base #$idx"
                assignedLabels.add(numbered)
                numbered
            }
            version.id to candidate
        }
    }
}

internal data class ParsedVersionMetadata(
    val stream: StreamEntity,
    val languageGroup: String,
    val languageSubBadge: String,
    val qualityPill: String?,
    val containerPill: String?,
    val categoryName: String,
    val priorityRank: Int
)

private val VERSION_QUALITY_TOKENS = setOf("4K", "UHD", "FHD", "HD", "HEVC", "HDR", "1080P", "720P", "SD")

internal fun parseVersionMetadata(
    version: StreamEntity,
    categoryNameById: Map<String, String>,
    includeContainer: Boolean
): ParsedVersionMetadata {
    val rawCat = categoryNameById[version.categoryId].orEmpty()
    val cleanCat = if (rawCat.isNotBlank()) {
        VodViewModel.parseCategoryLanguageAndName(rawCat).second.ifBlank { rawCat.trim() }
    } else {
        ""
    }
    val combinedUpper = "${version.name} $rawCat ${version.versionTag}".uppercase(Locale.US)
    val categoryAwareTag = VodGroupingNormalizer.extractVersionTagWithCategory(
        rawName = version.name,
        categoryName = rawCat
    )
    val rawTokens = (categoryAwareTag.ifBlank { version.versionTag.ifBlank { VodGroupingNormalizer.extractVersionTag(version.name) } })
        .split("•", "|", "/", ",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    val qualityFromParser = StreamMetadataParser.extractCompactQualityBadge(version.name)
        ?.uppercase(Locale.US)
        ?.let { if (it == "4K UHD" || it == "UHD") "4K" else it }
    val qualityFromTokens = rawTokens.firstOrNull { it.uppercase(Locale.US) in VERSION_QUALITY_TOKENS }
        ?.uppercase(Locale.US)
        ?.let {
            when (it) {
                "UHD", "4K" -> "4K"
                "1080P", "FHD" -> "FHD"
                "720P", "HD" -> "HD"
                else -> it
            }
        }
    val qualityPill = qualityFromParser ?: qualityFromTokens ?: when {
        combinedUpper.contains("4K") || combinedUpper.contains("UHD") || combinedUpper.contains("2160P") -> "4K"
        combinedUpper.contains("FHD") || combinedUpper.contains("1080P") -> "FHD"
        Regex("\\bHD\\b").containsMatchIn(combinedUpper) || combinedUpper.contains("720P") -> "HD"
        else -> null
    }

    val containerPill = version.containerExtension.trim().uppercase(Locale.US)
        .takeIf { it.isNotEmpty() && (includeContainer || it == "MKV" || it == "MP4" || it == "AVI") }

    val isVostfr = combinedUpper.contains("VOSTFR") || combinedUpper.contains("VOST")
    val isSub = combinedUpper.contains("SUB") || combinedUpper.contains("مترجم")
    val isMulti = combinedUpper.contains("MULTI")

    val langTokens = rawTokens
        .filter { it.uppercase(Locale.US) !in VERSION_QUALITY_TOKENS }
        .map { token -> VERSION_TOKEN_LABELS[token.uppercase(Locale.US)] ?: token }
        .distinct()

    val trailingQualifierRegex = Regex("(?i)\\s*\\((sub|subbed|vostfr|vost|multi|dub|dubbed)\\)\\s*$")
    val rawPrimaryLang = langTokens.firstOrNull {
        !it.equals("Subbed", ignoreCase = true) &&
            !it.equals("Dubbed", ignoreCase = true) &&
            !it.equals("VOSTFR", ignoreCase = true) &&
            !it.equals("Multi", ignoreCase = true)
    }
    val baseLang = rawPrimaryLang
        ?.replace(trailingQualifierRegex, "")
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
    val isMultiSubToken = baseLang.equals("Multi-Sub", ignoreCase = true) ||
        baseLang.equals("Multi Sub", ignoreCase = true) ||
        baseLang.equals("MultiSub", ignoreCase = true)
    val hasSubQualifier = isSub || (rawPrimaryLang?.contains("(Sub)", ignoreCase = true) == true)
    val hasVostfrQualifier = isVostfr || (rawPrimaryLang?.contains("VOSTFR", ignoreCase = true) == true)
    val hasMultiQualifier = isMulti || (rawPrimaryLang?.contains("(Multi)", ignoreCase = true) == true)

    val (languageGroup, priorityRank) = when {
        isMultiSubToken -> "Multi-Sub" to 3
        baseLang.equals("English", ignoreCase = true) ||
            baseLang.equals("US", ignoreCase = true) ||
            baseLang.equals("UK", ignoreCase = true) ||
            baseLang.equals("Canada", ignoreCase = true) ||
            baseLang.equals("Australia", ignoreCase = true) -> {
            if (hasMultiQualifier) "English (Multi)" to 0 else "English" to 0
        }
        baseLang.equals("Arabic", ignoreCase = true) -> {
            (if (hasSubQualifier) "Arabic (Sub)" else "Arabic") to 1
        }
        baseLang.equals("French", ignoreCase = true) || hasVostfrQualifier -> {
            (if (hasVostfrQualifier) "French (VOSTFR)" else "French") to 2
        }
        baseLang.equals("Spanish", ignoreCase = true) || baseLang.equals("Latino", ignoreCase = true) -> {
            (if (hasSubQualifier) "Spanish (Sub)" else "Spanish") to 3
        }
        baseLang != null -> {
            val alreadyQualified = baseLang.contains("sub", ignoreCase = true) ||
                baseLang.contains("multi", ignoreCase = true) ||
                baseLang.contains("vostfr", ignoreCase = true)
            val suffix = when {
                alreadyQualified -> ""
                hasSubQualifier -> " (Sub)"
                hasMultiQualifier -> " (Multi)"
                else -> ""
            }
            "$baseLang$suffix" to 4
        }
        hasMultiQualifier -> "Multi-Audio" to 3
        else -> "Other" to 5
    }

    val languageSubBadge = when {
        languageGroup != "Other" -> languageGroup
        langTokens.isNotEmpty() -> langTokens.take(2).joinToString(" • ")
        else -> "Standard"
    }

    return ParsedVersionMetadata(
        stream = version,
        languageGroup = languageGroup,
        languageSubBadge = languageSubBadge,
        qualityPill = qualityPill,
        containerPill = containerPill,
        categoryName = cleanCat.ifBlank { rawCat.trim() },
        priorityRank = priorityRank
    )
}

internal fun groupVersionsByLanguage(
    parsedVersions: List<ParsedVersionMetadata>
): List<Pair<String, List<ParsedVersionMetadata>>> {
    val grouped = linkedMapOf<String, MutableList<ParsedVersionMetadata>>()
    val sorted = parsedVersions.sortedWith(
        compareBy<ParsedVersionMetadata> { it.priorityRank }
            .thenBy { it.languageGroup.lowercase(Locale.US) }
            .thenBy {
                when (it.qualityPill) {
                    "4K" -> 0
                    "FHD" -> 1
                    "HD" -> 2
                    else -> 3
                }
            }
    )
    for (item in sorted) {
        val bucket = grouped.getOrPut(item.languageGroup) { mutableListOf() }
        bucket.add(item)
    }
    return grouped.entries.map { it.key to it.value.toList() }
}

private fun selectTopInlineVersions(
    parsedVersions: List<ParsedVersionMetadata>,
    activeStreamId: Long,
    maxInline: Int = 4
): List<ParsedVersionMetadata> {
    if (parsedVersions.size <= maxInline) return parsedVersions
    val ranked = parsedVersions.sortedWith(
        compareBy<ParsedVersionMetadata> { it.priorityRank }
            .thenBy {
                when (it.qualityPill) {
                    "4K" -> 0
                    "FHD" -> 1
                    "HD" -> 2
                    else -> 3
                }
            }
    )
    val chosen = mutableListOf<ParsedVersionMetadata>()
    val seenLangs = mutableSetOf<String>()
    // First pass: pick best copy per preferred language group for diversity
    for (item in ranked) {
        if (chosen.size >= maxInline) break
        if (seenLangs.add(item.languageGroup)) {
            chosen.add(item)
        }
    }
    // Second pass: fill remaining slots up to maxInline
    for (item in ranked) {
        if (chosen.size >= maxInline) break
        if (chosen.none { it.stream.id == item.stream.id }) {
            chosen.add(item)
        }
    }
    // Guarantee the active selected version is visible among inline pills
    if (chosen.none { it.stream.id == activeStreamId }) {
        val activeItem = parsedVersions.firstOrNull { it.stream.id == activeStreamId }
        if (activeItem != null && chosen.isNotEmpty()) {
            chosen[chosen.lastIndex] = activeItem
        }
    }
    return chosen
}

/**
 * Interactive lossless multi-language / multi-quality version selector strip for Movie Dossier & Series Vault.
 * - `versions.size <= 4`: renders clean inline pills displaying Language/Subtitle + Quality (`4K`/`FHD`/`HD`) + Container (`MKV`/`MP4`).
 * - `versions.size > 4`: renders top 4 preferred language pills inline PLUS an interactive `"All (N) ▾"` pill (`${tagPrefix}_all_btn`)
 *   that opens a grouped Mobile `ModalBottomSheet` or 10-foot Android TV Right-Side Version Drawer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VodVersionSelectorStrip(
    headerTitle: String,
    versions: List<StreamEntity>,
    activeStreamId: Long,
    categoryNameById: Map<String, String>,
    includeContainer: Boolean,
    tagPrefix: String,
    sectionBaseBg: Color,
    sectionSurface: Color,
    sectionPrimaryAccent: Color,
    sectionHighlight: Color,
    onSelectVersion: (StreamEntity) -> Unit,
    modifier: Modifier = Modifier,
    isTvEdition: Boolean = false
) {
    if (versions.size <= 1) return

    var showAllVersionsPicker by remember(versions.size, activeStreamId) { mutableStateOf(false) }

    val parsedVersions = remember(versions, categoryNameById, includeContainer) {
        versions.map { parseVersionMetadata(it, categoryNameById, includeContainer) }
    }
    val inlineVersions = remember(parsedVersions, activeStreamId) {
        selectTopInlineVersions(parsedVersions, activeStreamId, maxInline = 4)
    }
    val groupedVersions = remember(parsedVersions) {
        groupVersionsByLanguage(parsedVersions)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("${tagPrefix}_strip")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 18.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Translate,
                contentDescription = null,
                tint = sectionHighlight,
                modifier = Modifier.size(if (isTvEdition) 16.dp else 13.dp)
            )
            Text(
                text = headerTitle,
                color = sectionHighlight.copy(alpha = 0.90f),
                fontSize = if (isTvEdition) 12.sp else 10.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.7.sp
            )
        }

        Spacer(modifier = Modifier.height(if (isTvEdition) 9.dp else 7.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = if (isTvEdition) 4.dp else 0.dp),
            horizontalArrangement = Arrangement.spacedBy(if (isTvEdition) 10.dp else 8.dp)
        ) {
            items(inlineVersions, key = { "${tagPrefix}_${it.stream.id}_${it.stream.streamId}" }) { meta ->
                val version = meta.stream
                val isSelected = version.id == activeStreamId
                val chipShape = RoundedCornerShape(if (isTvEdition) 14.dp else 12.dp)

                Surface(
                    color = if (isSelected) {
                        sectionPrimaryAccent.copy(alpha = 0.36f)
                    } else {
                        sectionSurface.copy(alpha = 0.74f)
                    },
                    shape = chipShape,
                    border = BorderStroke(
                        width = if (isSelected) 1.4.dp else 0.8.dp,
                        color = if (isSelected) sectionHighlight else Color.White.copy(alpha = 0.18f)
                    ),
                    modifier = Modifier
                        .tvFocusableControl(
                            accentColor = sectionHighlight,
                            shape = chipShape,
                            focusedScale = if (isTvEdition) 1.06f else 1.03f,
                            onClick = { onSelectVersion(version) }
                        )
                        .testTag("${tagPrefix}_${version.streamId}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(
                            horizontal = if (isTvEdition) 14.dp else 10.dp,
                            vertical = if (isTvEdition) 10.dp else 8.dp
                        )
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected Version",
                                tint = sectionHighlight,
                                modifier = Modifier.size(if (isTvEdition) 16.dp else 13.dp)
                            )
                        }
                        Text(
                            text = meta.languageSubBadge,
                            color = if (isSelected) Color.White else Champagne.copy(alpha = 0.94f),
                            fontSize = if (isTvEdition) 13.sp else 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (meta.qualityPill != null) {
                            Surface(
                                color = if (meta.qualityPill == "4K") {
                                    AmberGold.copy(alpha = 0.22f)
                                } else {
                                    sectionHighlight.copy(alpha = 0.18f)
                                },
                                shape = RoundedCornerShape(5.dp)
                            ) {
                                Text(
                                    text = meta.qualityPill,
                                    color = if (meta.qualityPill == "4K") PaleGold else sectionHighlight,
                                    fontSize = if (isTvEdition) 10.5.sp else 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                )
                            }
                        }
                        if (meta.containerPill != null) {
                            Surface(
                                color = Color.White.copy(alpha = 0.10f),
                                shape = RoundedCornerShape(5.dp)
                            ) {
                                Text(
                                    text = meta.containerPill,
                                    color = Champagne.copy(alpha = 0.85f),
                                    fontSize = if (isTvEdition) 10.sp else 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.5.dp, vertical = 1.5.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (versions.size > 4) {
                item(key = "${tagPrefix}_all_pill") {
                    val allShape = RoundedCornerShape(if (isTvEdition) 14.dp else 12.dp)
                    Surface(
                        color = sectionBaseBg.copy(alpha = 0.88f),
                        shape = allShape,
                        border = BorderStroke(1.dp, sectionHighlight.copy(alpha = 0.68f)),
                        modifier = Modifier
                            .tvFocusableControl(
                                accentColor = sectionHighlight,
                                shape = allShape,
                                focusedScale = if (isTvEdition) 1.06f else 1.03f,
                                onClick = { showAllVersionsPicker = true }
                            )
                            .testTag("${tagPrefix}_all_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(
                                horizontal = if (isTvEdition) 15.dp else 11.dp,
                                vertical = if (isTvEdition) 10.dp else 8.dp
                            )
                        ) {
                            Text(
                                text = "All (${versions.size}) ▾",
                                color = sectionHighlight,
                                fontSize = if (isTvEdition) 13.sp else 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAllVersionsPicker) {
        if (isTvEdition) {
            TvVersionSelectorDrawer(
                totalCount = versions.size,
                groupedVersions = groupedVersions,
                activeStreamId = activeStreamId,
                tagPrefix = tagPrefix,
                sectionBaseBg = sectionBaseBg,
                sectionSurface = sectionSurface,
                sectionPrimaryAccent = sectionPrimaryAccent,
                sectionHighlight = sectionHighlight,
                onSelectVersion = { selected ->
                    showAllVersionsPicker = false
                    onSelectVersion(selected)
                },
                onDismiss = { showAllVersionsPicker = false }
            )
        } else {
            MobileVersionSelectorBottomSheet(
                totalCount = versions.size,
                groupedVersions = groupedVersions,
                activeStreamId = activeStreamId,
                tagPrefix = tagPrefix,
                sectionBaseBg = sectionBaseBg,
                sectionSurface = sectionSurface,
                sectionPrimaryAccent = sectionPrimaryAccent,
                sectionHighlight = sectionHighlight,
                onSelectVersion = { selected ->
                    showAllVersionsPicker = false
                    onSelectVersion(selected)
                },
                onDismiss = { showAllVersionsPicker = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MobileVersionSelectorBottomSheet(
    totalCount: Int,
    groupedVersions: List<Pair<String, List<ParsedVersionMetadata>>>,
    activeStreamId: Long,
    tagPrefix: String,
    sectionBaseBg: Color,
    sectionSurface: Color,
    sectionPrimaryAccent: Color,
    sectionHighlight: Color,
    onSelectVersion: (StreamEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sectionBaseBg,
        contentColor = Color.White,
        scrimColor = Color.Black.copy(alpha = 0.72f),
        modifier = Modifier.testTag("${tagPrefix}_mobile_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        tint = sectionHighlight,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Select Version ($totalCount)",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Version Picker",
                        tint = Champagne
                    )
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.10f))
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                groupedVersions.forEach { (languageHeader, itemsInGroup) ->
                    item(key = "hdr_${languageHeader}") {
                        Text(
                            text = "$languageHeader (${itemsInGroup.size})",
                            color = sectionHighlight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.4.sp,
                            modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                        )
                    }
                    items(itemsInGroup, key = { "sheet_${it.stream.id}_${it.stream.streamId}" }) { meta ->
                        val isSelected = meta.stream.id == activeStreamId
                        Surface(
                            color = if (isSelected) {
                                sectionPrimaryAccent.copy(alpha = 0.30f)
                            } else {
                                sectionSurface.copy(alpha = 0.72f)
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = if (isSelected) 1.3.dp else 0.7.dp,
                                color = if (isSelected) sectionHighlight else Color.White.copy(alpha = 0.12f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectVersion(meta.stream) }
                                .testTag("${tagPrefix}_sheet_row_${meta.stream.streamId}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            color = sectionBaseBg.copy(alpha = 0.80f),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(0.6.dp, sectionHighlight.copy(alpha = 0.48f))
                                        ) {
                                            Text(
                                                text = meta.languageSubBadge,
                                                color = Color.White,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        if (meta.qualityPill != null) {
                                            Surface(
                                                color = if (meta.qualityPill == "4K") {
                                                    AmberGold.copy(alpha = 0.22f)
                                                } else {
                                                    sectionHighlight.copy(alpha = 0.18f)
                                                },
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = meta.qualityPill,
                                                    color = if (meta.qualityPill == "4K") PaleGold else sectionHighlight,
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.padding(horizontal = 5.5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        if (meta.containerPill != null) {
                                            Surface(
                                                color = Color.White.copy(alpha = 0.10f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = meta.containerPill,
                                                    color = Champagne,
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 5.5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (meta.categoryName.isNotBlank()) {
                                        Text(
                                            text = meta.categoryName,
                                            color = sectionHighlight.copy(alpha = 0.85f),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Text(
                                        text = meta.stream.name,
                                        color = Champagne.copy(alpha = 0.78f),
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active Version",
                                        tint = sectionHighlight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Horizontal "More Like This" / "Similar Series" poster shelf rendered inside Movie Dossier & Series Vault.
 */
@Composable
internal fun VodSimilarTitlesRow(
    title: String,
    items: List<GroupedVodStream>,
    isMovies: Boolean,
    sectionBaseBg: Color,
    sectionSurface: Color,
    sectionPrimaryAccent: Color,
    sectionHighlight: Color,
    onItemClick: (StreamEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(if (isMovies) "movie_similar_titles_row" else "series_similar_titles_row")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "${items.size} titles",
                color = sectionHighlight.copy(alpha = 0.78f),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items, key = { "similar_${it.stream.id}_${it.stream.streamId}" }) { grouped ->
                val stream = grouped.stream
                val cleanTitle = remember(stream.name) { VodGroupingNormalizer.extractCleanDisplayTitle(stream.name) }
                val ratingLabel = remember(stream.ratingNumeric, grouped.versionCount) {
                    formatNormalizedRating(stream, grouped.versionCount)
                }
                val showTrendingBadge = remember(stream.ratingNumeric, grouped.versionCount) {
                    isTrendingPlaceholderRating(stream, grouped.versionCount)
                }

                Column(
                    modifier = Modifier
                        .width(104.dp)
                        .luxuryFocusableCard(
                            accentColor = sectionHighlight,
                            shape = RoundedCornerShape(10.dp),
                            focusedScale = 1.05f,
                            onClick = { onItemClick(stream) }
                        )
                        .clip(RoundedCornerShape(10.dp))
                        .testTag("vod_similar_card_${stream.streamId}")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(sectionSurface)
                            .border(0.8.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(10.dp))
                    ) {
                        if (stream.streamIcon.isNotBlank()) {
                            AsyncImage(
                                model = stream.streamIcon,
                                contentDescription = cleanTitle,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isMovies) Icons.Default.Movie else Icons.Default.VideoLibrary,
                                    contentDescription = null,
                                    tint = sectionHighlight.copy(alpha = 0.65f),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colorStops = arrayOf(
                                            0.0f to sectionBaseBg.copy(alpha = 0.25f),
                                            0.35f to Color.Transparent,
                                            0.72f to Color.Transparent,
                                            1.0f to sectionBaseBg.copy(alpha = 0.78f)
                                        )
                                    )
                                )
                        )

                        if (ratingLabel != null) {
                            Surface(
                                color = sectionBaseBg.copy(alpha = 0.84f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.6.dp, AmberGold.copy(alpha = 0.60f)),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(5.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = AmberGold,
                                        modifier = Modifier.size(8.5.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = ratingLabel,
                                        color = PaleGold,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        } else if (showTrendingBadge) {
                            Surface(
                                color = sectionBaseBg.copy(alpha = 0.86f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.6.dp, sectionHighlight.copy(alpha = 0.65f)),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(5.dp)
                            ) {
                                Text(
                                    text = "TRENDING",
                                    color = sectionHighlight,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.4.sp,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                )
                            }
                        }

                        if (grouped.versionCount > 1) {
                            Surface(
                                color = sectionBaseBg.copy(alpha = 0.86f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.6.dp, sectionPrimaryAccent.copy(alpha = 0.75f)),
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(5.dp)
                            ) {
                                Text(
                                    text = "${grouped.versionCount}x",
                                    color = sectionHighlight,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = cleanTitle,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Mobile Horizontal "Franchises & Collections" Shelf rendered on Movies Discovery Home.
 * Uses a distinctive stacked box-set card silhouette so official film franchises stand out
 * clearly from individual movie posters.
 */
@Composable
internal fun VodMobileCollectionsShelfRow(
    collections: List<VodCollectionEntity>,
    sectionBaseBg: Color,
    sectionSurface: Color,
    sectionPrimaryAccent: Color,
    sectionHighlight: Color,
    onOpenCollection: (VodCollectionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (collections.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("vod_collections_shelf")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalMovies,
                    contentDescription = null,
                    tint = sectionHighlight,
                    modifier = Modifier.size(17.dp)
                )
                Column {
                    Text(
                        text = "Franchises & Collections",
                        color = Color.White,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Official cinema sagas in your library",
                        color = sectionHighlight.copy(alpha = 0.72f),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                color = sectionSurface.copy(alpha = 0.85f),
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.7.dp, sectionHighlight.copy(alpha = 0.45f))
            ) {
                Text(
                    text = "${collections.size} Sagas",
                    color = sectionHighlight,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(collections, key = { "mobile_col_${it.collectionId}" }) { collection ->
                Column(
                    modifier = Modifier
                        .width(156.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onOpenCollection(collection) }
                        .testTag("vod_collection_card_${collection.collectionId}"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Stacked box-set depth ledges above poster card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.78f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(sectionPrimaryAccent.copy(alpha = 0.42f))
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(sectionHighlight.copy(alpha = 0.55f))
                    )

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = sectionSurface),
                        border = BorderStroke(1.dp, sectionHighlight.copy(alpha = 0.42f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(168.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (collection.posterUrl.isNotBlank()) {
                                AsyncImage(
                                    model = collection.posterUrl,
                                    contentDescription = collection.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                listOf(sectionSurface, sectionBaseBg)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalMovies,
                                        contentDescription = null,
                                        tint = sectionHighlight.copy(alpha = 0.55f),
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colorStops = arrayOf(
                                                0.0f to sectionBaseBg.copy(alpha = 0.25f),
                                                0.45f to sectionBaseBg.copy(alpha = 0.50f),
                                                0.82f to sectionBaseBg.copy(alpha = 0.92f),
                                                1.0f to sectionBaseBg.copy(alpha = 0.98f)
                                            )
                                        )
                                    )
                            )

                            // Top-End Movie Count Pill
                            Surface(
                                color = sectionBaseBg.copy(alpha = 0.86f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.7.dp, AmberGold.copy(alpha = 0.65f)),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "${collection.movieCount} Movies",
                                    color = PaleGold,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                                )
                            }

                            // Bottom Franchise Title + Prompt
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = collection.name,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    lineHeight = 15.5.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Explore Collection ›",
                                    color = sectionHighlight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Purpose-built Mobile Movie Collection / Franchise Detail Screen with hero banner,
 * Release Order / Newest First / Top Rated sort pills, and responsive poster grid.
 */
@Composable
internal fun VodMobileCollectionDetailScreen(
    collection: VodCollectionEntity,
    movies: List<GroupedVodStream>,
    sortMode: CollectionSortMode,
    sectionBaseBg: Color,
    sectionSurface: Color,
    sectionPrimaryAccent: Color,
    sectionHighlight: Color,
    onBack: () -> Unit,
    onSelectSortMode: (CollectionSortMode) -> Unit,
    onOpenMovieDetails: (StreamEntity) -> Unit,
    onPlayImmediately: (StreamEntity) -> Unit,
    onToggleFavorite: (StreamEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayCount = movies.size.coerceAtLeast(collection.movieCount)
    val heroArtUrl = collection.posterUrl.ifBlank {
        movies.firstOrNull { it.stream.streamIcon.isNotBlank() }?.stream?.streamIcon.orEmpty()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("vod_collection_detail_screen")
    ) {
        // Collection Hero Header Banner
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = sectionSurface),
            border = BorderStroke(0.9.dp, sectionHighlight.copy(alpha = 0.42f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(144.dp)
            ) {
                if (heroArtUrl.isNotBlank()) {
                    AsyncImage(
                        model = heroArtUrl,
                        contentDescription = collection.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    sectionBaseBg.copy(alpha = 0.96f),
                                    sectionBaseBg.copy(alpha = 0.80f),
                                    sectionBaseBg.copy(alpha = 0.45f)
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(sectionBaseBg.copy(alpha = 0.78f))
                            .border(0.9.dp, sectionHighlight.copy(alpha = 0.55f), CircleShape)
                            .testTag("vod_collection_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Movies",
                            tint = sectionHighlight
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = sectionPrimaryAccent.copy(alpha = 0.30f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.7.dp, sectionHighlight.copy(alpha = 0.65f))
                            ) {
                                Text(
                                    text = "FRANCHISE",
                                    color = sectionHighlight,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.7.sp,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                color = AmberGold.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.7.dp, AmberGold.copy(alpha = 0.65f))
                            ) {
                                Text(
                                    text = "$displayCount Movies",
                                    color = PaleGold,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = collection.name,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 21.sp
                        )
                    }
                }
            }
        }

        // Sort Selector Strip: Release Order / Newest First / Top Rated
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(CollectionSortMode.entries) { mode ->
                val isSelected = sortMode == mode
                Surface(
                    color = if (isSelected) {
                        sectionPrimaryAccent.copy(alpha = 0.38f)
                    } else {
                        sectionSurface.copy(alpha = 0.80f)
                    },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        width = if (isSelected) 1.2.dp else 0.7.dp,
                        color = if (isSelected) sectionHighlight else Color.White.copy(alpha = 0.16f)
                    ),
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable { onSelectSortMode(mode) }
                        .testTag("vod_collection_sort_${mode.name.lowercase(Locale.US)}")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    ) {
                        Text(
                            text = mode.label,
                            color = if (isSelected) Color.White else Champagne.copy(alpha = 0.84f),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val columns = if (maxWidth < 600.dp) {
                GridCells.Fixed(3)
            } else {
                GridCells.Adaptive(minSize = calculateVodPosterMinCellDp(maxWidth.value.toInt()).dp)
            }
            LazyVerticalGrid(
                columns = columns,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("vod_collection_grid"),
                contentPadding = PaddingValues(
                    start = 12.dp,
                    end = 12.dp,
                    top = 4.dp,
                    bottom = 104.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(movies, key = { "col_movie_${it.stream.id}" }) { grouped ->
                    val item = grouped.stream
                    MoviePosterCard(
                        item = item,
                        versionCount = grouped.versionCount,
                        onClick = { onOpenMovieDetails(item) },
                        onPlayImmediately = { onPlayImmediately(item) },
                        onToggleFavorite = { onToggleFavorite(item) }
                    )
                }
            }
        }
    }
}

/**
 * Horizontal Franchise Shelf rendered inside `ImaxMovieDossierModalSheet` when the active movie
 * belongs to an official TMDb Collection (ordered in Release Order).
 */
@Composable
internal fun VodDossierCollectionRow(
    collectionName: String,
    items: List<GroupedVodStream>,
    activeStreamId: Long,
    activeCanonicalGroupKey: String,
    sectionBaseBg: Color,
    sectionSurface: Color,
    sectionPrimaryAccent: Color,
    sectionHighlight: Color,
    onItemClick: (StreamEntity) -> Unit,
    onViewCollection: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (collectionName.isBlank() || items.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("movie_dossier_collection_shelf")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalMovies,
                    contentDescription = null,
                    tint = AmberGold,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Part of $collectionName",
                    color = Color.White,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (onViewCollection != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = sectionPrimaryAccent.copy(alpha = 0.24f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.7.dp, sectionHighlight.copy(alpha = 0.65f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onViewCollection)
                        .testTag("movie_dossier_view_collection_btn")
                ) {
                    Text(
                        text = "${items.size} Movies ›",
                        color = sectionHighlight,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(items, key = { _, g -> "dossier_col_${g.stream.id}_${g.stream.streamId}" }) { idx, grouped ->
                val stream = grouped.stream
                val isCurrentMovie = stream.id == activeStreamId ||
                    (activeCanonicalGroupKey.isNotBlank() && stream.canonicalGroupKey == activeCanonicalGroupKey)
                val cleanTitle = remember(stream.name) { VodGroupingNormalizer.extractCleanDisplayTitle(stream.name) }
                val yearLabel = stream.releaseYear.takeIf { it > 0 }?.toString()

                Column(
                    modifier = Modifier
                        .width(106.dp)
                        .luxuryFocusableCard(
                            accentColor = if (isCurrentMovie) AmberGold else sectionHighlight,
                            shape = RoundedCornerShape(10.dp),
                            focusedScale = 1.05f,
                            onClick = { if (!isCurrentMovie) onItemClick(stream) }
                        )
                        .clip(RoundedCornerShape(10.dp))
                        .testTag("movie_dossier_collection_card_${stream.streamId}")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(sectionSurface)
                            .border(
                                width = if (isCurrentMovie) 1.6.dp else 0.8.dp,
                                color = if (isCurrentMovie) AmberGold else Color.White.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(10.dp)
                            )
                    ) {
                        if (stream.streamIcon.isNotBlank()) {
                            AsyncImage(
                                model = stream.streamIcon,
                                contentDescription = cleanTitle,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Movie,
                                    contentDescription = null,
                                    tint = sectionHighlight.copy(alpha = 0.65f),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            sectionBaseBg.copy(alpha = 0.28f),
                                            Color.Transparent,
                                            sectionBaseBg.copy(alpha = 0.80f)
                                        )
                                    )
                                )
                        )

                        // Installment Order Pill (#1, #2, ...)
                        Surface(
                            color = if (isCurrentMovie) AmberGold else sectionBaseBg.copy(alpha = 0.86f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(
                                0.6.dp,
                                if (isCurrentMovie) PaleGold else sectionHighlight.copy(alpha = 0.60f)
                            ),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(5.dp)
                        ) {
                            Text(
                                text = "#${idx + 1}",
                                color = if (isCurrentMovie) sectionBaseBg else PaleGold,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                            )
                        }

                        if (isCurrentMovie) {
                            Surface(
                                color = sectionBaseBg.copy(alpha = 0.88f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.6.dp, AmberGold),
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(5.dp)
                            ) {
                                Text(
                                    text = "CURRENT",
                                    color = PaleGold,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                )
                            }
                        } else if (yearLabel != null) {
                            Surface(
                                color = sectionBaseBg.copy(alpha = 0.84f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(5.dp)
                            ) {
                                Text(
                                    text = yearLabel,
                                    color = Champagne,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = cleanTitle,
                        color = if (isCurrentMovie) PaleGold else Color.White,
                        fontSize = 11.sp,
                        fontWeight = if (isCurrentMovie) FontWeight.ExtraBold else FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}


