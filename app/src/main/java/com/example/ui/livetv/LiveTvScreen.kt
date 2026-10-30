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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled. Podcasts
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.ui.theme.LiveBroadcastGold
import com.example.ui.theme.LiveCoralAccent
import com.example.ui.theme.LiveCrimson
import com.example.ui.theme.LiveCrimsonGlow
import com.example.ui.theme.LiveRed
import com.example.ui.theme.MetalCharcoal
import com.example.ui.theme.MetalCharcoalElevated
import com.example.ui.theme.PaleGold

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
                .background(DarkGraphite)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.LiveTv, contentDescription = null, tint = LiveCrimson, modifier = Modifier.size(56.dp))
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

    // Pulsing broadcast signal animation for Crimson Gold Live TV Studio identity
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
            .background(DarkGraphite)
            .testTag("live_tv_screen")
    ) {
        if (selectedCategory == null) {
            // ============================================================
            // STAGE 1: CRIMSON GOLD BROADCAST STUDIO & CATEGORY DIRECTORY
            // ============================================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // Crimson Gold Broadcast Studio Header Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MetalCharcoal),
                    border = BorderStroke(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(listOf(LiveCrimson, AmberGold, LiveCrimson.copy(alpha = 0.5f)))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        LiveCrimsonGlow,
                                        MetalCharcoal,
                                        DarkGraphite
                                    )
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = LiveCrimson.copy(alpha = 0.22f),
                                        shape = RoundedCornerShape(50),
                                        border = BorderStroke(1.dp, LiveCrimson)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .alpha(livePulseAlpha)
                                                    .background(LiveCrimson, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = "ON AIR • BROADCAST STUDIO",
                                                color = LiveCoralAccent,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 0.6.sp
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Live TV Channels & Bouquets",
                                    color = PaleGold,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Select a broadcast bouquet to tune into instant live feeds",
                                    color = Champagne,
                                    fontSize = 11.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Surface(
                                    color = AmberGold,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "${categories.size} BOUQUETS",
                                        color = DarkGraphite,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "%,d Live Feeds".format(activePlaylist.channelCount),
                                    color = LiveCoralAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Crimson-Gold Category Search Bar
                OutlinedTextField(
                    value = categorySearchQuery,
                    onValueChange = { viewModel.onCategorySearchQueryChanged(it) },
                    placeholder = {
                        Text(
                            text = "Search Live TV categories (e.g. Sports, News, UK, USA, 8K)…",
                            color = Champagne.copy(alpha = 0.65f),
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search Categories", tint = LiveCrimson)
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
                        focusedBorderColor = LiveCrimson,
                        unfocusedBorderColor = AmberGold.copy(alpha = 0.45f),
                        focusedContainerColor = MetalCharcoal,
                        unfocusedContainerColor = MetalCharcoal,
                        focusedTextColor = PaleGold,
                        unfocusedTextColor = PaleGold
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
                            tint = LiveCrimson.copy(alpha = 0.7f),
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
                            text = "Tap 'Sync Catalog' on the Home screen or Accounts page to download categories and channels.",
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
                            tint = Champagne,
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
            // ============================================================
            // STAGE 2: SELECTED BOUQUET CHANNELS & IN-CATEGORY FILTER
            // ============================================================
            val currentCat = selectedCategory!!

            Surface(
                color = MetalCharcoal,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(LiveCrimsonGlow.copy(alpha = 0.85f), MetalCharcoal)
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.selectCategory(null) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, LiveCrimson),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = DarkGraphite,
                                contentColor = PaleGold
                            ),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("back_to_categories_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Categories",
                                tint = LiveCoralAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Categories",
                                color = PaleGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentCat.categoryName,
                                color = PaleGold,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (currentCat.streamCount > 0) "%,d live channels in bouquet".format(currentCat.streamCount) else "Browse channels in bouquet",
                                color = LiveCoralAccent,
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
                            Icon(Icons.Default.Search, contentDescription = "Filter Channels", tint = LiveCrimson)
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
                            focusedBorderColor = LiveCrimson,
                            unfocusedBorderColor = AmberGold.copy(alpha = 0.4f),
                            focusedContainerColor = DarkGraphite,
                            unfocusedContainerColor = DarkGraphite,
                            focusedTextColor = PaleGold,
                            unfocusedTextColor = PaleGold
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
                            tint = Champagne,
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
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAllChannelsCard) LiveCrimsonGlow else MetalCharcoal
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isAllChannelsCard) LiveCrimson else AmberGold.copy(alpha = 0.28f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("category_card_${category.categoryId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Vertical Crimson-Gold Broadcast Accent Strip
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(68.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = if (isAllChannelsCard) {
                                listOf(PaleGold, LiveCrimson)
                            } else {
                                listOf(LiveCrimson, AmberGold)
                            }
                        )
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        if (isAllChannelsCard) LiveCrimson else DarkGraphite,
                        RoundedCornerShape(11.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isAllChannelsCard) PaleGold else LiveCrimson.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(11.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isAllChannelsCard) Icons.Default.LiveTv else Icons.Default.Sensors,
                    contentDescription = null,
                    tint = if (isAllChannelsCard) PaleGold else LiveCoralAccent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = category.categoryName,
                        color = PaleGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
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
                        "Live Broadcast Bouquet • Tap to tune in"
                    },
                    color = Champagne,
                    fontSize = 11.sp
                )
            }

            if (category.streamCount > 0) {
                Surface(
                    color = if (isAllChannelsCard) AmberGold else DarkGraphite,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(0.8.dp, if (isAllChannelsCard) PaleGold else LiveCrimson.copy(alpha = 0.65f))
                ) {
                    Text(
                        text = "%,d ch".format(category.streamCount),
                        color = if (isAllChannelsCard) DarkGraphite else LiveCoralAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open Category",
                tint = AmberGold
            )
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
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MetalCharcoal),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                listOf(LiveCrimson.copy(alpha = 0.55f), Champagne.copy(alpha = 0.25f), AmberGold.copy(alpha = 0.45f))
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("channel_item_${channel.streamId}")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Channel Logo Container with Crimson-Gold Frame
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkGraphite)
                    .border(1.dp, LiveCrimson.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
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
                        tint = LiveBroadcastGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = channel.name,
                        color = PaleGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (channel.isLocked) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = LiveRed,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(5.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Crimson Gold Live Broadcast Pill
                    Surface(
                        color = LiveCrimsonGlow,
                        shape = RoundedCornerShape(5.dp),
                        border = BorderStroke(0.8.dp, LiveCrimson)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .alpha(livePulseAlpha)
                                    .background(LiveCrimson, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "LIVE • ON AIR",
                                color = PaleGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        color = PaleGold,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "VIP",
                            color = DarkGraphite,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }

                    if (channel.tvArchive == 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = CatchupGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
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
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = if (channel.tvArchiveDuration > 0) "${channel.tvArchiveDuration}d Catchup" else "Catchup",
                                    color = CatchupGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("channel_fav_${channel.streamId}")
            ) {
                Icon(
                    imageVector = if (channel.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (channel.isFavorite) AmberGold else Champagne,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(2.dp))

            // Amber Gold Play Button with Crimson Broadcast Ring
            IconButton(
                onClick = onClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(AmberGold, CircleShape)
                    .border(1.5.dp, LiveCrimson, CircleShape)
                    .testTag("channel_play_${channel.streamId}")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = DarkGraphite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
