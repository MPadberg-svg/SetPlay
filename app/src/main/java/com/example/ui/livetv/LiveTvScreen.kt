package com.example.ui.livetv

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.example.data.model.CategoryWithCount
import com.example.data.model.PlaylistEntity
import com.example.data.model.StreamEntity
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CatchupGreen
import com.example.ui.theme.Champagne
import com.example.ui.theme.DarkGraphite
import com.example.ui.theme.LiveRed
import com.example.ui.theme.PaleGold
import com.example.ui.theme.TvBroadcastScarlet
import com.example.ui.theme.TvCrimsonRose
import com.example.ui.theme.TvDeepMaroon
import com.example.ui.theme.TvMidnightSlate
import com.example.ui.theme.TvPlumMagenta
import com.example.ui.theme.TvTwilightIndigo

@Composable
fun LiveTvScreen(
    viewModel: LiveTvViewModel,
    activePlaylist: PlaylistEntity?,
    onChannelClick: (StreamEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (activePlaylist == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(TvMidnightSlate, TvTwilightIndigo, TvDeepMaroon.copy(alpha = 0.65f))
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.LiveTv,
                    contentDescription = null,
                    tint = TvBroadcastScarlet,
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

    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val categorySearchQuery by viewModel.categorySearchQuery.collectAsStateWithLifecycle()
    val channelFilterQuery by viewModel.channelFilterQuery.collectAsStateWithLifecycle()
    val channels by viewModel.channels.collectAsStateWithLifecycle()

    // Pulsing broadcast signal animation for Live TV Studio identity
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val livePulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_dot_alpha"
    )

    // Intercept system back button when viewing a category's channels to return to the Category Directory
    BackHandler(enabled = selectedCategory != null) {
        viewModel.selectCategory(null)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        TvMidnightSlate,
                        TvTwilightIndigo.copy(alpha = 0.72f),
                        TvMidnightSlate
                    )
                )
            )
            .testTag("live_tv_screen")
    ) {
        if (selectedCategory == null) {
            // =========================================================================
            // STAGE 1: BROADCAST STUDIO CONTROL DECK (#1F313B • #383852 • #784259 • #B94E56 • #BE4039 • #683536)
            // =========================================================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TvTwilightIndigo),
                    border = BorderStroke(
                        width = 0.8.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                TvBroadcastScarlet.copy(alpha = 0.5f),
                                Color.White.copy(alpha = 0.08f),
                                TvPlumMagenta.copy(alpha = 0.5f)
                            )
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        TvDeepMaroon.copy(alpha = 0.85f),
                                        TvTwilightIndigo,
                                        TvMidnightSlate
                                    )
                                )
                            )
                            .padding(horizontal = 14.dp, vertical = 11.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    // Unboxed inline status eyebrow
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .alpha(livePulseAlpha)
                                                .background(TvBroadcastScarlet, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "ON AIR • BROADCAST STUDIO",
                                            color = TvCrimsonRose,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.7.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Live TV Channels & Bouquets",
                                        color = Color.White,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "Select a broadcast bouquet to tune into instant live feeds",
                                        color = Champagne.copy(alpha = 0.8f),
                                        fontSize = 11.sp
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Surface(
                                        color = Color.White.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = "${categories.size} BOUQUETS",
                                            color = PaleGold,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "%,d Live Feeds".format(activePlaylist.channelCount),
                                        color = TvCrimsonRose,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Signature 6-Color Broadcast Studio Spectrum Strip
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(
                                    TvMidnightSlate,
                                    TvTwilightIndigo,
                                    TvPlumMagenta,
                                    TvCrimsonRose,
                                    TvBroadcastScarlet,
                                    TvDeepMaroon
                                ).forEach { swatch ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(2.5.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(swatch)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Twilight Indigo & Broadcast Scarlet Category Search Bar
                OutlinedTextField(
                    value = categorySearchQuery,
                    onValueChange = { viewModel.onCategorySearchQueryChanged(it) },
                    placeholder = {
                        Text(
                            text = "Search Live TV categories (e.g. Sports, News, Morocco, MENA, 4K)…",
                            color = Champagne.copy(alpha = 0.65f),
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search Categories", tint = TvCrimsonRose)
                    },
                    trailingIcon = {
                        if (categorySearchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onCategorySearchQueryChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Champagne)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TvBroadcastScarlet,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                        focusedContainerColor = TvTwilightIndigo,
                        unfocusedContainerColor = TvTwilightIndigo.copy(alpha = 0.85f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_search_input")
                )
            }

            if (categories.isEmpty() && activePlaylist.channelCount == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = TvCrimsonRose,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No Live TV Categories Synced Yet",
                            color = PaleGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap 'Sync Catalog' on the KickOff screen or Accounts page to download categories and channels.",
                            color = Champagne,
                            fontSize = 13.sp
                        )
                    }
                }
            } else if (categories.isEmpty() && categorySearchQuery.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TvCrimsonRose,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No categories match \"$categorySearchQuery\"",
                            color = PaleGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("live_categories_directory"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (categorySearchQuery.isBlank() && activePlaylist.channelCount > 0) {
                        item(key = "__all_channels__") {
                            val allCat = CategoryWithCount(
                                categoryId = LiveTvViewModel.ALL_CHANNELS_CATEGORY_ID,
                                categoryName = "All Live TV Channels",
                                type = "LIVE",
                                isLocked = false,
                                streamCount = activePlaylist.channelCount
                            )
                            LiveCategoryDirectoryCard(
                                category = allCat,
                                isAllChannelsCard = true,
                                onClick = { viewModel.selectCategory(allCat) }
                            )
                        }
                    }

                    items(categories, key = { it.categoryId }) { category ->
                        LiveCategoryDirectoryCard(
                            category = category,
                            isAllChannelsCard = false,
                            onClick = { viewModel.selectCategory(category) }
                        )
                    }
                }
            }
        } else {
            // =========================================================================
            // STAGE 2: SELECTED BOUQUET CHANNELS & IN-CATEGORY FILTER
            // =========================================================================
            val currentCat = selectedCategory!!

            Surface(
                color = TvTwilightIndigo,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(TvDeepMaroon, TvPlumMagenta.copy(alpha = 0.8f), TvTwilightIndigo)
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tier 2 Secondary Surface Back Pill (borderless bg-white/8)
                        Surface(
                            color = Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .height(32.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable { viewModel.selectCategory(null) }
                                .testTag("back_to_categories_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to Categories",
                                    tint = TvCrimsonRose,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Categories",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentCat.categoryName,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (currentCat.streamCount > 0) "%,d live channels in bouquet".format(currentCat.streamCount) else "Browse channels in bouquet",
                                color = TvCrimsonRose,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // In-category channel filter bar
                    OutlinedTextField(
                        value = channelFilterQuery,
                        onValueChange = { viewModel.onChannelFilterQueryChanged(it) },
                        placeholder = {
                            Text(
                                text = "Filter channels inside ${currentCat.categoryName}…",
                                color = Champagne.copy(alpha = 0.65f),
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Filter Channels", tint = TvCrimsonRose)
                        },
                        trailingIcon = {
                            if (channelFilterQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onChannelFilterQueryChanged("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Champagne)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(22.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TvBroadcastScarlet,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                            focusedContainerColor = TvMidnightSlate,
                            unfocusedContainerColor = TvMidnightSlate,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("channel_search_input")
                    )
                }
            }

            if (channels.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LiveTv,
                            contentDescription = null,
                            tint = TvCrimsonRose,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No channels found in this category.",
                            color = PaleGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("live_channels_list"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(channels, key = { it.id }) { channel ->
                        ChannelCard(
                            channel = channel,
                            livePulseAlpha = livePulseAlpha,
                            onClick = { onChannelClick(channel) },
                            onToggleFavorite = { viewModel.toggleFavorite(channel.id, channel.isFavorite) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LiveCategoryDirectoryCard(
    category: CategoryWithCount,
    isAllChannelsCard: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAllChannelsCard) TvDeepMaroon else TvTwilightIndigo
        ),
        border = BorderStroke(
            width = 1.dp,
            brush = if (isAllChannelsCard) {
                Brush.horizontalGradient(
                    listOf(
                        TvBroadcastScarlet.copy(alpha = 0.75f),
                        AmberGold.copy(alpha = 0.45f)
                    )
                )
            } else {
                Brush.horizontalGradient(
                    listOf(
                        TvCrimsonRose.copy(alpha = 0.28f),
                        Color.White.copy(alpha = 0.07f)
                    )
                )
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("category_card_${category.categoryId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            if (isAllChannelsCard) TvDeepMaroon else TvTwilightIndigo,
                            TvMidnightSlate.copy(alpha = 0.94f)
                        )
                    )
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Broadcast Studio Spine
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(64.dp)
                    .background(
                        Brush.verticalGradient(
                            if (isAllChannelsCard) {
                                listOf(AmberGold, TvBroadcastScarlet)
                            } else {
                                listOf(TvBroadcastScarlet, TvCrimsonRose.copy(alpha = 0.55f))
                            }
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 13.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isAllChannelsCard) {
                                Brush.linearGradient(listOf(TvBroadcastScarlet, TvDeepMaroon))
                            } else {
                                Brush.linearGradient(
                                    listOf(
                                        TvBroadcastScarlet.copy(alpha = 0.18f),
                                        Color.White.copy(alpha = 0.05f)
                                    )
                                )
                            }
                        )
                        .border(
                            0.8.dp,
                            if (isAllChannelsCard) AmberGold.copy(alpha = 0.55f) else TvCrimsonRose.copy(alpha = 0.35f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isAllChannelsCard) Icons.Default.LiveTv else Icons.Default.Sensors,
                        contentDescription = null,
                        tint = if (isAllChannelsCard) Color.White else TvCrimsonRose,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = category.categoryName,
                            color = Color.White,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (category.isLocked) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked Category",
                                tint = LiveRed,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isAllChannelsCard) {
                            "Full Live Broadcast Lineup • Instant Zap"
                        } else {
                            "Live Broadcast Bouquet • Studio Feed"
                        },
                        color = Champagne.copy(alpha = 0.72f),
                        fontSize = 11.sp
                    )
                }

                if (category.streamCount > 0) {
                    Surface(
                        color = if (isAllChannelsCard) TvBroadcastScarlet else Color(0xFF0A0D18),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(
                            0.7.dp,
                            if (isAllChannelsCard) AmberGold.copy(alpha = 0.6f) else TvCrimsonRose.copy(alpha = 0.40f)
                        )
                    ) {
                        Text(
                            text = "%,d".format(category.streamCount),
                            color = if (isAllChannelsCard) Color.White else PaleGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Open Category",
                    tint = TvCrimsonRose,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ChannelCard(
    channel: StreamEntity,
    livePulseAlpha: Float = 1f,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val liveProgressFraction = (0.35f + ((channel.streamId.hashCode() and 0x7FFFFFFF) % 50) / 100f).coerceIn(0.25f, 0.88f)
    val isUltraHd = remember(channel.name) {
        val upper = channel.name.uppercase()
        upper.contains("4K") || upper.contains("UHD") || upper.contains("RAW") || upper.contains("MAX") || upper.contains("HEVC")
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TvTwilightIndigo),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    if (channel.isFavorite) AmberGold.copy(alpha = 0.55f) else TvBroadcastScarlet.copy(alpha = 0.40f),
                    Color.White.copy(alpha = 0.08f)
                )
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("channel_item_${channel.streamId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            TvTwilightIndigo,
                            TvMidnightSlate.copy(alpha = 0.95f)
                        )
                    )
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Broadcast Control Room Status Spine
            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height(74.dp)
                    .background(
                        Brush.verticalGradient(
                            if (channel.isFavorite) {
                                listOf(AmberGold, TvBroadcastScarlet)
                            } else {
                                listOf(TvBroadcastScarlet, TvCrimsonRose.copy(alpha = 0.45f))
                            }
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 11.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Recessed Frosted Logo Well
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    TvBroadcastScarlet.copy(alpha = 0.14f),
                                    Color(0xFF0A0D18)
                                )
                            )
                        )
                        .border(0.8.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (channel.streamIcon.isNotEmpty()) {
                        AsyncImage(
                            model = channel.streamIcon,
                            contentDescription = channel.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(5.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = TvCrimsonRose,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(11.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = channel.name,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Broadcast Quality Micro-Tag (4K UHD / FHD)
                        Surface(
                            color = Color.Black.copy(alpha = 0.40f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(
                                0.6.dp,
                                if (isUltraHd) AmberGold.copy(alpha = 0.75f) else TvCrimsonRose.copy(alpha = 0.45f)
                            )
                        ) {
                            Text(
                                text = if (isUltraHd) "4K" else "FHD",
                                color = if (isUltraHd) AmberGold else TvCrimsonRose,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 4.5.dp, vertical = 1.dp)
                            )
                        }
                        if (channel.isLocked) {
                            Spacer(modifier = Modifier.width(5.dp))
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = LiveRed,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Inline Live Status & Catchup Metadata
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(5.5.dp)
                                .alpha(livePulseAlpha)
                                .background(TvBroadcastScarlet, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "ON AIR • ${(liveProgressFraction * 100).toInt()}%",
                            color = TvCrimsonRose,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.3.sp
                        )

                        if (channel.tvArchive == 1) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = CatchupGreen,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (channel.tvArchiveDuration > 0) "${channel.tvArchiveDuration}d Catchup" else "Catchup",
                                color = CatchupGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // "Now & Next" Live EPG Progress Strip (Scarlet to AmberGold gradient)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.90f)
                            .height(2.5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(liveProgressFraction)
                                .height(2.5.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(TvBroadcastScarlet, TvCrimsonRose, AmberGold)
                                    )
                                )
                        )
                    }
                }

                // Favorite Star Icon Button
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("channel_fav_${channel.streamId}")
                ) {
                    Icon(
                        imageVector = if (channel.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (channel.isFavorite) AmberGold else Champagne.copy(alpha = 0.65f),
                        modifier = Modifier.size(19.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Illuminated Broadcast Scarlet "(▶) WATCH" Capsule with Dark Play Medallion
                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.9.dp, Color.White.copy(alpha = 0.32f)),
                    modifier = Modifier
                        .height(31.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    TvBroadcastScarlet,
                                    TvCrimsonRose
                                )
                            )
                        )
                        .clickable(onClick = onClick)
                        .testTag("channel_play_${channel.streamId}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(start = 5.dp, end = 11.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0A0D18))
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = TvCrimsonRose,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "WATCH",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}
