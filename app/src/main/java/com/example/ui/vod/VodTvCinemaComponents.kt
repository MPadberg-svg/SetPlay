package com.example.ui.vod

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.focus.onFocusChanged
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
import com.example.ui.theme.AmberGold
import com.example.ui.theme.Champagne
import com.example.ui.theme.LiveRed
import com.example.ui.theme.MoviesAtlanticTeal
import com.example.ui.theme.MoviesSapphireNavy
import com.example.ui.theme.PaleGold
import com.example.ui.theme.SeriesForestSurface
import com.example.ui.theme.SeriesPineBase
import com.example.ui.theme.luxuryFocusableCard
import com.example.ui.theme.tvFocusableControl
import com.example.ui.theme.tvFocusablePill
import com.example.util.StreamMetadataParser
import java.util.Locale

private enum class TvCategoryQuickGroup(val label: String) {
    ALL("All"),
    POPULAR("Popular / 4K"),
    LANGUAGES("Languages"),
    A_TO_F("A–F"),
    G_TO_M("G–M"),
    N_TO_S("N–S"),
    T_TO_Z("T–Z")
}

private val TV_POPULAR_KEYWORDS = listOf(
    "4K", "UHD", "NEW", "LATEST", "RECENT", "TOP", "POPULAR",
    "NETFLIX", "APPLE", "HBO", "DISNEY", "PRIME", "IMAX", "2024", "2025", "2026"
)

private val TV_LANGUAGE_KEYWORDS = listOf(
    "ENGLISH", "EN ", "EN|", "ARABIC", "AR ", "AR|", "FRENCH", "FR ", "FR|",
    "SPANISH", "ES ", "ES|", "LATINO", "GERMAN", "DE ", "DE|", "ITALIAN", "IT ",
    "TURKISH", "TR ", "PORTUGUESE", "PT ", "HINDI", "BOLLYWOOD", "KOREAN", "ANIME",
    "RUSSIAN", "DUTCH", "POLISH", "SWEDISH", "KURDISH", "MULTI", "SUB"
)

private fun matchesTvQuickGroup(categoryName: String, group: TvCategoryQuickGroup): Boolean {
    if (group == TvCategoryQuickGroup.ALL) return true
    val upper = categoryName.uppercase(Locale.US)
    val cleanFirstChar = StreamMetadataParser.cleanDisplayTitle(categoryName)
        .trim()
        .firstOrNull { it.isLetter() }
        ?.uppercaseChar()
    return when (group) {
        TvCategoryQuickGroup.ALL -> true
        TvCategoryQuickGroup.POPULAR -> TV_POPULAR_KEYWORDS.any { upper.contains(it) }
        TvCategoryQuickGroup.LANGUAGES -> TV_LANGUAGE_KEYWORDS.any { upper.contains(it) }
        TvCategoryQuickGroup.A_TO_F -> cleanFirstChar != null && cleanFirstChar in 'A'..'F'
        TvCategoryQuickGroup.G_TO_M -> cleanFirstChar != null && cleanFirstChar in 'G'..'M'
        TvCategoryQuickGroup.N_TO_S -> cleanFirstChar != null && cleanFirstChar in 'N'..'S'
        TvCategoryQuickGroup.T_TO_Z -> cleanFirstChar == null || cleanFirstChar in 'T'..'Z'
    }
}

/**
 * Purpose-built 10-foot Android TV Top Control Strip (`48.dp`).
 * - Left: D-Pad-focusable `[ ☰ Categories • Active ]` pill opening the 2-column TV Category Overlay Dialog
 * - Right: Compact inline sync status pill + Discover/Grid toggle + D-Pad `Search` button + explicit `Sync Catalog` button
 */
@Composable
internal fun TvVodTopControlStrip(
    isMovies: Boolean,
    activeCategoryLabel: String,
    selectedCategoryId: String?,
    searchQuery: String,
    isSearchExpanded: Boolean,
    forceGridMode: Boolean,
    totalCount: Int,
    isRefreshing: Boolean,
    syncBannerState: VodSyncBannerState,
    sectionBaseBg: Color,
    sectionSurface: Color,
    sectionPrimaryAccent: Color,
    sectionHighlight: Color,
    onOpenCategories: () -> Unit,
    onToggleSearch: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleViewMode: () -> Unit,
    onSyncCatalog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryButtonLabel = if (selectedCategoryId == null) {
        if (isMovies) "Categories • All Movies" else "Categories • All Series"
    } else {
        "Categories • $activeCategoryLabel"
    }
    val showSearchField = isSearchExpanded || searchQuery.isNotBlank()
    val showModeToggle = selectedCategoryId == null && searchQuery.isBlank()
    val showInlineSyncPill = syncBannerState.isSyncing || syncBannerState.isCompleted || syncBannerState.isError

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .height(48.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: D-Pad-focusable [ ☰ Categories • Active ] Button (`vod_categories_drawer_btn`)
            Surface(
                color = sectionSurface.copy(alpha = 0.88f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (selectedCategoryId != null) {
                        sectionHighlight.copy(alpha = 0.72f)
                    } else {
                        sectionPrimaryAccent.copy(alpha = 0.50f)
                    }
                ),
                modifier = Modifier
                    .height(42.dp)
                    .tvFocusableControl(
                        accentColor = sectionHighlight,
                        shape = RoundedCornerShape(14.dp),
                        focusedScale = 1.04f,
                        onClick = onOpenCategories
                    )
                    .testTag("vod_categories_drawer_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open Categories",
                        tint = sectionHighlight,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = categoryButtonLabel,
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = sectionHighlight,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Compact inline sync status pill (when syncing / completed / error)
            if (showInlineSyncPill) {
                Surface(
                    color = if (syncBannerState.isError) {
                        Color(0xFF2B1018).copy(alpha = 0.92f)
                    } else {
                        sectionSurface.copy(alpha = 0.90f)
                    },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        width = 0.8.dp,
                        color = if (syncBannerState.isError) LiveRed.copy(alpha = 0.75f) else sectionHighlight.copy(alpha = 0.55f)
                    ),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("vod_sync_status_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        when {
                            syncBannerState.isSyncing -> {
                                CircularProgressIndicator(
                                    color = sectionHighlight,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            syncBannerState.isError -> {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "Sync Error",
                                    tint = LiveRed,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Sync Complete",
                                    tint = sectionHighlight,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                        Text(
                            text = syncBannerState.statusText.ifBlank { "Syncing catalog…" },
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Right 1: Discover / All Grid 1-tap toggle (when on All categories)
            if (showModeToggle) {
                Surface(
                    color = if (forceGridMode) {
                        sectionPrimaryAccent.copy(alpha = 0.32f)
                    } else {
                        sectionSurface.copy(alpha = 0.86f)
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        width = 0.9.dp,
                        color = if (forceGridMode) sectionHighlight.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.16f)
                    ),
                    modifier = Modifier
                        .height(42.dp)
                        .tvFocusableControl(
                            accentColor = sectionHighlight,
                            shape = RoundedCornerShape(14.dp),
                            focusedScale = 1.05f,
                            onClick = onToggleViewMode
                        )
                        .testTag("vod_view_mode_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (forceGridMode) Icons.Default.AutoAwesome else Icons.Default.GridView,
                            contentDescription = if (forceGridMode) "Switch to Discovery Stage" else "Switch to Full Grid",
                            tint = sectionHighlight,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (forceGridMode) "Discover" else "All Grid",
                            color = Color.White,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Right 2: D-Pad Search Button (`vod_search_toggle_btn`)
            Surface(
                color = if (showSearchField) {
                    sectionPrimaryAccent.copy(alpha = 0.34f)
                } else {
                    sectionSurface.copy(alpha = 0.88f)
                },
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    width = 0.9.dp,
                    color = if (showSearchField) sectionHighlight.copy(alpha = 0.78f) else Color.White.copy(alpha = 0.16f)
                ),
                modifier = Modifier
                    .height(42.dp)
                    .tvFocusableControl(
                        accentColor = sectionHighlight,
                        shape = RoundedCornerShape(14.dp),
                        focusedScale = 1.05f,
                        onClick = onToggleSearch
                    )
                    .testTag("vod_search_toggle_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (showSearchField) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = if (showSearchField) "Close Search" else "Search Catalog",
                        tint = sectionHighlight,
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = if (showSearchField) "Close" else "Search",
                        color = Color.White,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Right 3: Explicit D-Pad Sync Catalog Button (`section_refresh_button`)
            Surface(
                color = sectionPrimaryAccent.copy(alpha = 0.28f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, sectionHighlight.copy(alpha = 0.65f)),
                modifier = Modifier
                    .height(42.dp)
                    .tvFocusableControl(
                        accentColor = sectionHighlight,
                        shape = RoundedCornerShape(14.dp),
                        focusedScale = 1.05f,
                        onClick = onSyncCatalog
                    )
                    .testTag("section_refresh_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            color = sectionHighlight,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(15.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync Catalog",
                            tint = sectionHighlight,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Text(
                        text = if (isRefreshing) "Syncing…" else "Sync Catalog",
                        color = Color.White,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // Expandable 10-foot Inline Search Bar (`vod_search_input`)
        AnimatedVisibility(
            visible = showSearchField,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = when {
                                selectedCategoryId != null -> "Search in $activeCategoryLabel…"
                                isMovies -> "Search %,d+ cinema movies…".format(totalCount)
                                else -> "Search %,d+ series & shows…".format(totalCount)
                            },
                            color = sectionHighlight.copy(alpha = 0.68f),
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = sectionHighlight,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Search",
                                    tint = sectionHighlight,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = sectionHighlight,
                        unfocusedBorderColor = sectionPrimaryAccent.copy(alpha = 0.40f),
                        focusedContainerColor = sectionBaseBg.copy(alpha = 0.88f),
                        unfocusedContainerColor = sectionSurface.copy(alpha = 0.85f),
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
 * Widescreen 2-Column Android TV Category Overlay Dialog with D-Pad focus rings,
 * item counts, and quick letter/language grouping for effortless remote browsing across 179+ categories.
 */
@Composable
internal fun TvVodCategoryOverlayDialog(
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
    var activeGroup by remember { mutableStateOf(TvCategoryQuickGroup.ALL) }

    val filteredCategories = remember(categories, selectedCategoryId, activeGroup) {
        categories
            .filter { matchesTvQuickGroup(it.categoryName, activeGroup) }
            .sortedWith(
                compareBy<CategoryEntity>(
                    { if (it.categoryId == selectedCategoryId) 0 else 1 },
                    { StreamMetadataParser.cleanDisplayTitle(it.categoryName).lowercase(Locale.US) }
                )
            )
    }

    val panelShape = RoundedCornerShape(22.dp)
    val panelGradient = if (isMovies) {
        Brush.verticalGradient(
            listOf(
                MoviesSapphireNavy.copy(alpha = 0.97f),
                MoviesAtlanticTeal.copy(alpha = 0.94f),
                MoviesSapphireNavy.copy(alpha = 0.98f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                SeriesPineBase.copy(alpha = 0.97f),
                SeriesForestSurface.copy(alpha = 0.94f),
                SeriesPineBase.copy(alpha = 0.98f)
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
                .background(Color.Black.copy(alpha = 0.72f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .fillMaxHeight(0.84f)
                    .clip(panelShape)
                    .background(panelGradient)
                    .border(1.2.dp, sectionHighlight.copy(alpha = 0.55f), panelShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .padding(22.dp)
                    .testTag("tv_vod_categories_dialog")
            ) {
                // Header: Title + Count + Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isMovies) "Movie Categories" else "Series Categories",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${filteredCategories.size} of ${categories.size} categories • Use D-Pad to browse",
                            color = sectionHighlight.copy(alpha = 0.85f),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.9.dp, Color.White.copy(alpha = 0.22f)),
                        modifier = Modifier
                            .tvFocusablePill(
                                accentColor = sectionHighlight,
                                onClick = onDismiss
                            )
                            .testTag("tv_categories_close_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Categories",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Close",
                                color = Color.White,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Letter / Language Group Filter Strip for Fast Remote Navigation
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(TvCategoryQuickGroup.entries) { group ->
                        val isActive = activeGroup == group
                        Surface(
                            color = if (isActive) {
                                sectionPrimaryAccent.copy(alpha = 0.42f)
                            } else {
                                sectionBaseBg.copy(alpha = 0.60f)
                            },
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(
                                width = if (isActive) 1.2.dp else 0.8.dp,
                                color = if (isActive) sectionHighlight else Color.White.copy(alpha = 0.16f)
                            ),
                            modifier = Modifier
                                .tvFocusablePill(
                                    accentColor = sectionHighlight,
                                    focusedScale = 1.05f,
                                    onClick = { activeGroup = group }
                                )
                                .testTag("tv_cat_group_${group.name.lowercase(Locale.US)}")
                        ) {
                            Text(
                                text = group.label,
                                color = if (isActive) Color.White else Champagne,
                                fontSize = 12.5.sp,
                                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Top Pinned Full-Width Pill: Discover Home (All)
                val isDiscoverSelected = selectedCategoryId == null
                Surface(
                    color = if (isDiscoverSelected) {
                        sectionPrimaryAccent.copy(alpha = 0.34f)
                    } else {
                        Color.White.copy(alpha = 0.06f)
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        width = if (isDiscoverSelected) 1.2.dp else 0.8.dp,
                        color = if (isDiscoverSelected) sectionHighlight else Color.White.copy(alpha = 0.14f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .tvFocusableControl(
                            accentColor = sectionHighlight,
                            shape = RoundedCornerShape(14.dp),
                            focusedScale = 1.02f,
                            onClick = {
                                onSelectDiscoverHome()
                                onDismiss()
                            }
                        )
                        .testTag("vod_category_discover_all")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = sectionHighlight,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isMovies) "Discover Home • All Movies" else "Discover Home • All Series",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.10f), thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Widescreen 2-Column D-Pad Category Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(filteredCategories, key = { it.categoryId }) { cat ->
                        val isSelected = selectedCategoryId == cat.categoryId
                        val displayName = remember(cat.categoryName) {
                            StreamMetadataParser.cleanDisplayTitle(cat.categoryName)
                                .ifBlank { cat.categoryName }
                        }
                        val itemCount = categoryCounts[cat.categoryId] ?: 0

                        Surface(
                            color = if (isSelected) {
                                sectionPrimaryAccent.copy(alpha = 0.34f)
                            } else {
                                sectionSurface.copy(alpha = 0.56f)
                            },
                            shape = RoundedCornerShape(13.dp),
                            border = BorderStroke(
                                width = if (isSelected) 1.2.dp else 0.7.dp,
                                color = if (isSelected) sectionHighlight else Color.White.copy(alpha = 0.10f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .tvFocusableControl(
                                    accentColor = sectionHighlight,
                                    shape = RoundedCornerShape(13.dp),
                                    focusedScale = 1.03f,
                                    onClick = {
                                        onSelectCategory(cat)
                                        onDismiss()
                                    }
                                )
                                .testTag("vod_category_row_${cat.categoryId}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (cat.isLocked) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Locked Category",
                                            tint = LiveRed,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    Text(
                                        text = displayName,
                                        color = if (isSelected) Color.White else Champagne,
                                        fontSize = 13.5.sp,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (itemCount > 0) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = sectionBaseBg.copy(alpha = 0.72f),
                                        shape = RoundedCornerShape(50),
                                        border = BorderStroke(
                                            0.6.dp,
                                            if (isSelected) sectionHighlight else Color.White.copy(alpha = 0.16f)
                                        )
                                    ) {
                                        Text(
                                            text = "%,d".format(itemCount),
                                            color = if (isSelected) Color.White else sectionHighlight,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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
 * Top ~38% Dynamic 10-Foot Live Preview Stage for Android TV Discovery Home.
 * Updates smoothly whenever the user moves D-Pad focus across any poster card on the shelves below.
 */
@Composable
internal fun TvLiveBackdropBanner(
    grouped: GroupedVodStream,
    isMovies: Boolean,
    sectionBaseBg: Color,
    sectionSurface: Color,
    sectionPrimaryAccent: Color,
    sectionHighlight: Color,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = grouped,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "tv_live_backdrop_transition",
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(sectionSurface)
            .border(1.dp, sectionHighlight.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
            .testTag("tv_live_backdrop_stage")
    ) { active ->
        val stream = active.stream
        val cleanTitle = remember(stream.name) { StreamMetadataParser.cleanDisplayTitle(stream.name) }
        val ratingLabel = remember(stream.ratingNumeric) { formatNormalizedRating(stream) }
        val yearLabel = stream.releaseYear.takeIf { it > 0 }?.toString()
        val genreSummary = remember(stream.genres, stream.versionTag) {
            buildList {
                if (!yearLabel.isNullOrBlank()) add(yearLabel)
                if (stream.genres.isNotBlank()) {
                    add(stream.genres.split(",").take(3).joinToString(" • ") { it.trim() })
                }
                if (stream.versionTag.isNotBlank()) {
                    add(stream.versionTag)
                }
            }.joinToString("  •  ")
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Right-weighted full-bleed backdrop artwork
            if (stream.streamIcon.isNotBlank()) {
                AsyncImage(
                    model = stream.streamIcon,
                    contentDescription = cleanTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxWidth(0.68f)
                        .fillMaxHeight()
                )
            }

            // Left-to-right dark cinema gradient scrim so 10-foot typography pops effortlessly
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colorStops = arrayOf(
                                0.0f to sectionBaseBg.copy(alpha = 0.98f),
                                0.36f to sectionBaseBg.copy(alpha = 0.94f),
                                0.62f to sectionBaseBg.copy(alpha = 0.62f),
                                1.0f to sectionBaseBg.copy(alpha = 0.18f)
                            )
                        )
                    )
            )
            // Subtle bottom atmospheric fade
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.72f to Color.Transparent,
                                1.0f to sectionBaseBg.copy(alpha = 0.90f)
                            )
                        )
                    )
            )

            // Left 10-Foot Metadata Stage
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(0.64f)
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = sectionPrimaryAccent.copy(alpha = 0.28f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.8.dp, sectionHighlight.copy(alpha = 0.65f))
                    ) {
                        Text(
                            text = if (isMovies) "FEATURED CINEMA" else "FEATURED SERIES",
                            color = sectionHighlight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }

                    if (ratingLabel != null) {
                        Surface(
                            color = sectionBaseBg.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.8.dp, AmberGold.copy(alpha = 0.70f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AmberGold,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = ratingLabel,
                                    color = PaleGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    if (active.versionCount > 1) {
                        Surface(
                            color = sectionBaseBg.copy(alpha = 0.82f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.8.dp, sectionHighlight.copy(alpha = 0.62f))
                        ) {
                            Text(
                                text = "${active.versionCount} Versions Available",
                                color = sectionHighlight,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = cleanTitle,
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 30.sp
                )

                if (genreSummary.isNotBlank()) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = genreSummary,
                        color = Champagne.copy(alpha = 0.88f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Remote Hint Pill: OK to Play / Details
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = sectionHighlight.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.9.dp, sectionHighlight.copy(alpha = 0.72f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = sectionHighlight,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "OK to Play / Details",
                                color = Color.White,
                                fontSize = 11.5.sp,
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
 * 10-Foot Continue Watching Horizontal Shelf for Android TV Discovery Home.
 */
@Composable
internal fun TvContinueWatchingShelfRow(
    items: List<StreamEntity>,
    isMovies: Boolean,
    sectionBaseBg: Color,
    sectionSurface: Color,
    sectionHighlight: Color,
    onItemFocused: (GroupedVodStream) -> Unit,
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
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = sectionHighlight,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Continue Watching",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                text = "${items.size} in progress",
                color = sectionHighlight.copy(alpha = 0.80f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(items, key = { "tv_cw_${it.id}_${it.streamId}" }) { item ->
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
                        .width(196.dp)
                        .onFocusChanged { if (it.isFocused) onItemFocused(GroupedVodStream(item, 1)) }
                        .luxuryFocusableCard(
                            accentColor = sectionHighlight,
                            shape = RoundedCornerShape(14.dp),
                            focusedScale = 1.06f,
                            onClick = { onItemClick(item) }
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .background(sectionSurface.copy(alpha = 0.72f))
                        .padding(bottom = 8.dp)
                        .testTag("continue_watching_card_${item.streamId}")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(106.dp)
                            .background(sectionBaseBg)
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
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, sectionBaseBg.copy(alpha = 0.80f))
                                    )
                                )
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(sectionBaseBg.copy(alpha = 0.82f))
                                .border(1.dp, sectionHighlight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Resume",
                                tint = sectionHighlight,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(Color.White.copy(alpha = 0.18f))
                                .testTag("continue_watching_progress_${item.streamId}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressFraction)
                                    .fillMaxHeight()
                                    .background(Brush.horizontalGradient(listOf(sectionHighlight, AmberGold)))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = cleanTitle,
                        color = Color.White,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Text(
                        text = if (item.lastPositionMs > 0L) "Resume • ${resumedMinutes}m in" else "Resume Playback",
                        color = sectionHighlight.copy(alpha = 0.85f),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * 10-Foot Android TV Horizontal Discovery Shelf with `144.dp`-wide 2:3 poster cards,
 * `1.06f` focus scaling, bright focus borders, and live backdrop focus tracking.
 */
@Composable
internal fun TvDiscoveryShelfRow(
    shelf: VodDiscoveryShelf,
    isMovies: Boolean,
    sectionBaseBg: Color,
    sectionSurface: Color,
    sectionPrimaryAccent: Color,
    sectionHighlight: Color,
    onItemFocused: (GroupedVodStream) -> Unit,
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
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = shelf.title,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                if (shelf.subtitle.isNotBlank()) {
                    Text(
                        text = shelf.subtitle,
                        color = sectionHighlight.copy(alpha = 0.72f),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(shelf.items, key = { "tv_${shelf.id}_${it.stream.id}" }) { grouped ->
                val stream = grouped.stream
                val cleanTitle = remember(stream.name) { StreamMetadataParser.cleanDisplayTitle(stream.name) }
                val ratingLabel = remember(stream.ratingNumeric) { formatNormalizedRating(stream) }

                Column(
                    modifier = Modifier
                        .width(144.dp)
                        .onFocusChanged { if (it.isFocused) onItemFocused(grouped) }
                        .luxuryFocusableCard(
                            accentColor = sectionHighlight,
                            shape = RoundedCornerShape(14.dp),
                            focusedScale = 1.06f,
                            onClick = { onItemClick(grouped) }
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .background(sectionSurface.copy(alpha = 0.65f))
                        .padding(bottom = 8.dp)
                        .testTag("vod_shelf_card_${stream.streamId}")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f)
                            .background(sectionBaseBg)
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
                                            0.0f to sectionBaseBg.copy(alpha = 0.28f),
                                            0.35f to Color.Transparent,
                                            0.72f to Color.Transparent,
                                            1.0f to sectionBaseBg.copy(alpha = 0.85f)
                                        )
                                    )
                                )
                        )

                        if (ratingLabel != null) {
                            Surface(
                                color = sectionBaseBg.copy(alpha = 0.86f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.7.dp, AmberGold.copy(alpha = 0.65f)),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(7.dp)
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
                                    Spacer(modifier = Modifier.width(2.5.dp))
                                    Text(
                                        text = ratingLabel,
                                        color = PaleGold,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        if (grouped.versionCount > 1) {
                            Surface(
                                color = sectionBaseBg.copy(alpha = 0.88f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.7.dp, sectionPrimaryAccent.copy(alpha = 0.78f)),
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(7.dp)
                            ) {
                                Text(
                                    text = "${grouped.versionCount} Versions",
                                    color = sectionHighlight,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = cleanTitle,
                        color = Color.White,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp)
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
                            color = Champagne.copy(alpha = 0.65f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
