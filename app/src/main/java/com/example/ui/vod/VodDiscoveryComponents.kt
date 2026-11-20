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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.theme.luxuryFocusableCard
import com.example.ui.theme.tvFocusableControl
import com.example.util.StreamMetadataParser
import java.util.Locale

private val PREFERRED_CATEGORY_KEYWORDS = listOf(
    "4K", "UHD", "NEW", "LATEST", "RECENT", "TOP", "POPULAR",
    "NETFLIX", "APPLE", "HBO", "DISNEY", "PRIME",
    "ENGLISH", "ARABIC", "FRENCH", "ACTION", "CRIME", "DRAMA", "COMEDY"
)

private fun isPreferredVodCategory(categoryName: String): Boolean {
    val upper = categoryName.uppercase(Locale.US)
    return PREFERRED_CATEGORY_KEYWORDS.any { keyword -> upper.contains(keyword) }
}

internal fun formatNormalizedRating(stream: StreamEntity): String? {
    if (stream.ratingNumeric <= 0.0) return null
    return String.format(Locale.US, "%.1f", stream.ratingNumeric.coerceAtMost(10.0))
}

internal fun formatDetailNormalizedRating(detailsRating: String?, stream: StreamEntity): String? {
    val parsed = detailsRating?.trim()?.replace(',', '.')?.toDoubleOrNull()
        ?.takeIf { it > 0.0 && it <= 10.0 }
    if (parsed != null) {
        return String.format(Locale.US, "%.1f", parsed)
    }
    return formatNormalizedRating(stream)
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
                                isMovies -> "Search %,d+ movies…".format(totalCount)
                                else -> "Search %,d+ series…".format(totalCount)
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
 * brief completion feedback, or sync error with Retry action.
 */
@Composable
internal fun VodSyncStatusPill(
    bannerState: VodSyncBannerState,
    sectionSurface: Color,
    sectionHighlight: Color,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isVisible = bannerState.isSyncing || bannerState.isCompleted || bannerState.isError
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        val borderColor = when {
            bannerState.isError -> LiveRed.copy(alpha = 0.65f)
            bannerState.isCompleted -> sectionHighlight.copy(alpha = 0.65f)
            else -> sectionHighlight.copy(alpha = 0.45f)
        }
        val bgColor = when {
            bannerState.isError -> Color(0xFF2B1018).copy(alpha = 0.92f)
            else -> sectionSurface.copy(alpha = 0.92f)
        }

        Surface(
            color = bgColor,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(0.9.dp, borderColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp)
                .testTag("vod_sync_status_pill")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    when {
                        bannerState.isSyncing -> {
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
                        else -> {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Sync Complete",
                                tint = sectionHighlight,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = if (bannerState.isError) {
                            "Sync failed • ${bannerState.statusText.ifBlank { "Check connection" }}"
                        } else {
                            bannerState.statusText.ifBlank { "Syncing catalog…" }
                        },
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
    onDismiss: () -> Unit
) {
    var filterQuery by remember { mutableStateOf("") }

    val sortedFilteredCategories = remember(categories, selectedCategoryId, filterQuery) {
        val trimmed = filterQuery.trim()
        val filtered = if (trimmed.isEmpty()) {
            categories
        } else {
            categories.filter { cat ->
                cat.categoryName.contains(trimmed, ignoreCase = true)
            }
        }
        filtered.sortedWith(
            compareBy<CategoryEntity>(
                { if (it.categoryId == selectedCategoryId) 0 else if (isPreferredVodCategory(it.categoryName)) 1 else 2 },
                { StreamMetadataParser.cleanDisplayTitle(it.categoryName).lowercase(Locale.US) }
            )
        )
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

                // Scrollable Category List with Exact Item Count Pills
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(sortedFilteredCategories, key = { it.categoryId }) { cat ->
                        val isSelected = selectedCategoryId == cat.categoryId
                        val displayName = remember(cat.categoryName) {
                            StreamMetadataParser.cleanDisplayTitle(cat.categoryName)
                                .ifBlank { cat.categoryName }
                        }
                        val itemCount = categoryCounts[cat.categoryId] ?: 0

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
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
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
                                    Text(
                                        text = displayName,
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

        // c) Curated Discovery Shelves
        items(discoveryShelves, key = { it.id }) { shelf ->
            VodDiscoveryShelfRow(
                shelf = shelf,
                isMovies = isMovies,
                sectionBaseBg = sectionBaseBg,
                sectionSurface = sectionSurface,
                sectionPrimaryAccent = sectionPrimaryAccent,
                sectionHighlight = sectionHighlight,
                onItemClick = { onOpenDetails(it.stream) }
            )
        }

        // Fallback shelf if discovery shelves haven't populated yet (e.g., legacy DB before re-sync)
        if (discoveryShelves.isEmpty() && fallbackGroupedItems.isNotEmpty()) {
            item(key = "vod_fallback_library_shelf") {
                VodDiscoveryShelfRow(
                    shelf = VodDiscoveryShelf(
                        id = "fallback_catalog",
                        title = if (isMovies) "Featured Cinema" else "Featured Series",
                        subtitle = "From your active library",
                        items = fallbackGroupedItems.take(20)
                    ),
                    isMovies = isMovies,
                    sectionBaseBg = sectionBaseBg,
                    sectionSurface = sectionSurface,
                    sectionPrimaryAccent = sectionPrimaryAccent,
                    sectionHighlight = sectionHighlight,
                    onItemClick = { onOpenDetails(it.stream) }
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
    val cleanTitle = remember(stream.name) { StreamMetadataParser.cleanDisplayTitle(stream.name) }
    val ratingLabel = remember(stream.rating, stream.ratingNumeric) { formatNormalizedRating(stream) }
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
                val cleanTitle = remember(item.name) { StreamMetadataParser.cleanDisplayTitle(item.name) }
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
    onItemClick: (GroupedVodStream) -> Unit
) {
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
            verticalAlignment = Alignment.Bottom
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
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
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
    val cleanTitle = remember(stream.name) { StreamMetadataParser.cleanDisplayTitle(stream.name) }
    val ratingLabel = remember(stream.rating, stream.ratingNumeric) { formatNormalizedRating(stream) }

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

            // Top-Start Normalized Rating Pill
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
    val rawTag = version.versionTag.ifBlank { VodGroupingNormalizer.extractVersionTag(version.name) }
    val mappedTokens = rawTag
        .split("•", "|", "/", ",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { token -> VERSION_TOKEN_LABELS[token.uppercase(Locale.US)] ?: token }
        .distinct()

    val labelCore = when {
        mappedTokens.isNotEmpty() -> mappedTokens.joinToString(" • ")
        else -> {
            val catHint = categoryNameById[version.categoryId]
                ?.let { StreamMetadataParser.cleanDisplayTitle(it) }
                ?.takeIf { it.isNotBlank() && it.length <= 22 }
            catHint ?: "Standard"
        }
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
    val usedIndices = mutableMapOf<String, Int>()
    return versions.associate { version ->
        val base = baseLabels[version.id] ?: "Standard"
        if ((counts[base] ?: 0) <= 1) {
            version.id to base
        } else {
            val catHint = categoryNameById[version.categoryId]
                ?.let { StreamMetadataParser.cleanDisplayTitle(it) }
                ?.takeIf { it.isNotBlank() }
            val candidate = if (catHint != null && !base.contains(catHint, ignoreCase = true)) {
                val shortCat = if (catHint.length > 16) catHint.take(15) + "…" else catHint
                "$base ($shortCat)"
            } else {
                val idx = (usedIndices[base] ?: 0) + 1
                usedIndices[base] = idx
                "$base #$idx"
            }
            version.id to candidate
        }
    }
}

/**
 * Interactive multi-language / multi-quality version selector strip for Movie Dossier & Series Vault.
 * Switching versions selects the exact original StreamEntity without mutating URLs or streamIds.
 */
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

    val chipLabels = remember(versions, categoryNameById, includeContainer) {
        formatVersionChipLabels(versions, categoryNameById, includeContainer)
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
            items(versions, key = { "${tagPrefix}_${it.id}_${it.streamId}" }) { version ->
                val isSelected = version.id == activeStreamId
                val label = chipLabels[version.id] ?: "Standard"
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
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(
                            horizontal = if (isTvEdition) 15.dp else 11.dp,
                            vertical = if (isTvEdition) 10.dp else 8.dp
                        )
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected Version",
                                tint = sectionHighlight,
                                modifier = Modifier.size(if (isTvEdition) 16.dp else 14.dp)
                            )
                        }
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else Champagne.copy(alpha = 0.92f),
                            fontSize = if (isTvEdition) 13.sp else 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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
                val cleanTitle = remember(stream.name) { StreamMetadataParser.cleanDisplayTitle(stream.name) }
                val ratingLabel = remember(stream.ratingNumeric) { formatNormalizedRating(stream) }

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

