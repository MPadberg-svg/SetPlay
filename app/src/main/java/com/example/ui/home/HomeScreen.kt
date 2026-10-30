package com.example.ui.home

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.PlaylistEntity
import com.example.data.model.StreamEntity
import com.example.ui.playlists.formatExpirationDate
import com.example.ui.theme.AmberGold
import com.example.ui.theme.Champagne
import com.example.ui.theme.DarkGraphite
import com.example.ui.theme.LiveRed
import com.example.ui.theme.MetalCharcoal
import com.example.ui.theme.MetalCharcoalElevated
import com.example.ui.theme.PaleGold
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

private enum class HomeCategoryChip(val label: String) {
    ALL("All Featured"),
    LIVE("Live TV"),
    MOVIES("Cinema Movies"),
    SERIES("VIP Series"),
    FAVORITES("Favorites")
}

@Composable
fun HomeScreen(
    mainViewModel: MainViewModel,
    onStreamClick: (StreamEntity) -> Unit,
    onNavigateScreen: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val activePlaylist by mainViewModel.activePlaylist.collectAsStateWithLifecycle()
    val syncProgress by mainViewModel.syncProgress.collectAsStateWithLifecycle()
    val recentlyWatched by mainViewModel.recentlyWatched.collectAsStateWithLifecycle()
    val favorites by mainViewModel.favorites.collectAsStateWithLifecycle()
    val featuredLive by mainViewModel.featuredLiveStreams.collectAsStateWithLifecycle()
    val featuredMovies by mainViewModel.featuredMovies.collectAsStateWithLifecycle()
    val featuredSeries by mainViewModel.featuredSeries.collectAsStateWithLifecycle()

    var selectedHomeChip by remember { mutableStateOf(HomeCategoryChip.ALL) }

    val heroFeaturedStream = remember(recentlyWatched, favorites, featuredMovies, featuredLive) {
        recentlyWatched.firstOrNull()
            ?: favorites.firstOrNull()
            ?: featuredMovies.firstOrNull()
            ?: featuredLive.firstOrNull()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkGraphite)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. Sync Progress Notification Banner in Royal Gold
        item {
            AnimatedVisibility(visible = syncProgress.isSyncing || syncProgress.error != null) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (syncProgress.error != null) LiveRed.copy(alpha = 0.2f) else MetalCharcoal
                    ),
                    border = BorderStroke(1.dp, Champagne.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("sync_progress_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (syncProgress.isSyncing) {
                                CircularProgressIndicator(
                                    color = AmberGold,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = null, tint = AmberGold, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = syncProgress.phase.ifBlank { syncProgress.error ?: "Synchronizing..." },
                                color = if (syncProgress.error != null) LiveRed else PaleGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        if (syncProgress.isSyncing) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                color = AmberGold,
                                trackColor = DarkGraphite,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }
            }
        }

        // 2. Featured Hero Banner (or Empty Account Onboarding Card) directly below Top Bar
        item {
            if (activePlaylist == null) {
                WelcomeOnboardingHeroCard(
                    onAddAccountClick = { onNavigateScreen(AppScreen.PLAYLISTS) }
                )
            } else {
                HeroSpotlightBanner(
                    activePlaylist = activePlaylist!!,
                    featuredStream = heroFeaturedStream,
                    onWatchNow = {
                        if (heroFeaturedStream != null) {
                            onStreamClick(heroFeaturedStream)
                        } else {
                            onNavigateScreen(AppScreen.LIVE_TV)
                        }
                    },
                    onQuickSync = { mainViewModel.syncActiveAccount() },
                    onManageAccounts = { onNavigateScreen(AppScreen.PLAYLISTS) }
                )
            }
        }

        // 3. Horizontal Scrolling Row of Pill-Shaped Category Chips
        // Selected chip uses solid Amber Gold (#D4A238) fill with Dark Graphite (#141619) text;
        // Unselected chips use Metal Charcoal (#25282E) fills with Champagne (#DFC69E) text.
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("home_category_chips_row"),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(HomeCategoryChip.entries) { chip ->
                    val isSelected = selectedHomeChip == chip
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedHomeChip = chip },
                        label = {
                            Text(
                                text = chip.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                            )
                        },
                        shape = RoundedCornerShape(50),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Champagne.copy(alpha = 0.35f),
                            selectedBorderColor = AmberGold
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberGold,
                            selectedLabelColor = DarkGraphite,
                            containerColor = MetalCharcoal,
                            labelColor = Champagne
                        ),
                        modifier = Modifier.testTag("home_chip_${chip.name.lowercase()}")
                    )
                }
            }
        }

        // 4. Quick Catalog Stats Hub
        item {
            Spacer(modifier = Modifier.height(8.dp))
            CatalogStatsHub(
                activePlaylist = activePlaylist,
                onNavigateLive = { onNavigateScreen(AppScreen.LIVE_TV) },
                onNavigateMovies = { onNavigateScreen(AppScreen.MOVIES) },
                onNavigateSeries = { onNavigateScreen(AppScreen.SERIES) }
            )
        }

        // 5. Horizontal Scrolling Poster Carousels with Rounded Corners, Crisp Cover Art & Gold VIP Badges
        if ((selectedHomeChip == HomeCategoryChip.ALL) && recentlyWatched.isNotEmpty()) {
            item {
                RoyalGoldPosterCarousel(
                    title = "Continue Watching",
                    subtitle = "Pick up right where you left off",
                    badgeText = "RESUME",
                    items = recentlyWatched,
                    onItemClick = onStreamClick,
                    onSeeAll = null
                )
            }
        }

        if ((selectedHomeChip == HomeCategoryChip.ALL || selectedHomeChip == HomeCategoryChip.FAVORITES) && favorites.isNotEmpty()) {
            item {
                RoyalGoldPosterCarousel(
                    title = "Your VIP Favorites",
                    subtitle = "Starred channels, movies & series",
                    badgeText = "VIP",
                    items = favorites,
                    onItemClick = onStreamClick,
                    onSeeAll = { onNavigateScreen(AppScreen.FAVORITES) }
                )
            }
        }

        if ((selectedHomeChip == HomeCategoryChip.ALL || selectedHomeChip == HomeCategoryChip.LIVE) && featuredLive.isNotEmpty()) {
            item {
                RoyalGoldPosterCarousel(
                    title = "Live TV Channels",
                    subtitle = "Direct instant-zap broadcast streams",
                    badgeText = "LIVE",
                    items = featuredLive,
                    onItemClick = onStreamClick,
                    onSeeAll = { onNavigateScreen(AppScreen.LIVE_TV) }
                )
            }
        }

        if ((selectedHomeChip == HomeCategoryChip.ALL || selectedHomeChip == HomeCategoryChip.MOVIES) && featuredMovies.isNotEmpty()) {
            item {
                RoyalGoldPosterCarousel(
                    title = "Premier Cinema Movies",
                    subtitle = "High-definition on-demand box office",
                    badgeText = "4K VIP",
                    items = featuredMovies,
                    onItemClick = onStreamClick,
                    onSeeAll = { onNavigateScreen(AppScreen.MOVIES) }
                )
            }
        }

        if ((selectedHomeChip == HomeCategoryChip.ALL || selectedHomeChip == HomeCategoryChip.SERIES) && featuredSeries.isNotEmpty()) {
            item {
                RoyalGoldPosterCarousel(
                    title = "Featured VIP Series",
                    subtitle = "Multi-season shows ready to binge",
                    badgeText = "SERIES",
                    items = featuredSeries,
                    onItemClick = {
                        onNavigateScreen(AppScreen.SERIES)
                    },
                    onSeeAll = { onNavigateScreen(AppScreen.SERIES) }
                )
            }
        }

        // 6. Fast Section Cards
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                Text(
                    text = "Explore SetPlay Catalog",
                    color = PaleGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CategoryHubCard(
                        title = "Live TV",
                        subtitle = "${activePlaylist?.channelCount ?: 0} Channels",
                        icon = Icons.Default.LiveTv,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateScreen(AppScreen.LIVE_TV) }
                    )
                    CategoryHubCard(
                        title = "Movies & Series",
                        subtitle = "${(activePlaylist?.vodCount ?: 0) + (activePlaylist?.seriesCount ?: 0)} Titles",
                        icon = Icons.Default.Movie,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateScreen(AppScreen.MOVIES) }
                    )
                }
            }
        }
    }
}

@Composable
fun WelcomeOnboardingHeroCard(
    onAddAccountClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MetalCharcoal),
        border = BorderStroke(1.dp, Champagne.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("welcome_onboarding_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MetalCharcoalElevated,
                            MetalCharcoal,
                            DarkGraphite
                        )
                    )
                )
                .padding(22.dp)
        ) {
            // Top-Right VIP Pill Badge in Pale Gold
            Surface(
                color = PaleGold,
                shape = RoundedCornerShape(50),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = DarkGraphite,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "VIP LUXURY EDITION",
                        color = DarkGraphite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Column {
                Surface(
                    color = AmberGold.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "XTREAM CODES • 4K UHD",
                        color = AmberGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Welcome to SetPlay",
                    color = PaleGold,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Connect your Xtream Codes IPTV account to unlock Live TV channels, 4K Cinema Movies, and multi-season Series in Royal Gold quality.",
                    color = Champagne,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onAddAccountClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberGold,
                        contentColor = DarkGraphite
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("hero_add_account_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = DarkGraphite, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Xtream Account", color = DarkGraphite, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
fun HeroSpotlightBanner(
    activePlaylist: PlaylistEntity,
    featuredStream: StreamEntity?,
    onWatchNow: () -> Unit,
    onQuickSync: () -> Unit,
    onManageAccounts: () -> Unit
) {
    val expFormatted = remember(activePlaylist.expDate) { formatExpirationDate(activePlaylist.expDate) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MetalCharcoal),
        border = BorderStroke(1.dp, Champagne.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("hero_spotlight")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(235.dp)
                .background(MetalCharcoal)
        ) {
            // Optional Cover Artwork Background
            if (featuredStream != null && featuredStream.streamIcon.isNotEmpty()) {
                AsyncImage(
                    model = featuredStream.streamIcon,
                    contentDescription = featuredStream.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Bottom-to-Top Dark Graphite Gradient Scrim over the artwork
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                DarkGraphite.copy(alpha = 0.35f),
                                DarkGraphite.copy(alpha = 0.78f),
                                DarkGraphite
                            )
                        )
                    )
            )

            // Top-Right VIP Pill Badge in Pale Gold (#F6E8C8) with Dark Graphite text
            Surface(
                color = PaleGold,
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = DarkGraphite,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "VIP • EXP $expFormatted",
                        color = DarkGraphite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Content Overlay at Bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(18.dp)
            ) {
                Surface(
                    color = AmberGold.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = if (featuredStream != null) "FEATURED PREMIERE" else "ACTIVE XTREAM SERVER",
                        color = AmberGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bold Title Overlay in Pale Gold
                Text(
                    text = featuredStream?.name ?: activePlaylist.name,
                    color = PaleGold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (activePlaylist.channelCount > 0 || activePlaylist.vodCount > 0) {
                        "${activePlaylist.name} • %,d Live • %,d Movies • %,d Series".format(
                            activePlaylist.channelCount,
                            activePlaylist.vodCount,
                            activePlaylist.seriesCount
                        )
                    } else {
                        "Connected to ${activePlaylist.serverUrl} • Tap 'Sync Catalog' to load streams"
                    },
                    color = Champagne,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Primary Action Button in Amber Gold with Dark Graphite text & icon
                    Button(
                        onClick = onWatchNow,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberGold,
                            contentColor = DarkGraphite
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("hero_watch_now")
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = DarkGraphite,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (featuredStream != null) "Watch Now" else "Browse Live TV",
                            color = DarkGraphite,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    OutlinedButton(
                        onClick = onQuickSync,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Champagne.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MetalCharcoal.copy(alpha = 0.85f),
                            contentColor = PaleGold
                        ),
                        modifier = Modifier.testTag("hero_sync_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = AmberGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sync Catalog", color = PaleGold, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onManageAccounts,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Champagne.copy(alpha = 0.4f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MetalCharcoal.copy(alpha = 0.85f),
                            contentColor = Champagne
                        ),
                        modifier = Modifier.testTag("hero_accounts_button")
                    ) {
                        Icon(Icons.Default.Dns, contentDescription = "Accounts", tint = AmberGold, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CatalogStatsHub(
    activePlaylist: PlaylistEntity?,
    onNavigateLive: () -> Unit,
    onNavigateMovies: () -> Unit,
    onNavigateSeries: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatPill(
            count = activePlaylist?.channelCount ?: 0,
            label = "Live TV",
            modifier = Modifier
                .weight(1f)
                .clickable { onNavigateLive() }
        )
        StatPill(
            count = activePlaylist?.vodCount ?: 0,
            label = "Movies",
            modifier = Modifier
                .weight(1f)
                .clickable { onNavigateMovies() }
        )
        StatPill(
            count = activePlaylist?.seriesCount ?: 0,
            label = "Series",
            modifier = Modifier
                .weight(1f)
                .clickable { onNavigateSeries() }
        )
    }
}

@Composable
fun StatPill(
    count: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MetalCharcoal,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Champagne.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (count > 0) "%,d".format(count) else "—",
                color = AmberGold,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = Champagne,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun RoyalGoldPosterCarousel(
    title: String,
    subtitle: String,
    badgeText: String,
    items: List<StreamEntity>,
    onItemClick: (StreamEntity) -> Unit,
    onSeeAll: (() -> Unit)?
) {
    Column(modifier = Modifier.padding(top = 18.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = PaleGold,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = subtitle,
                    color = Champagne,
                    fontSize = 11.sp
                )
            }
            if (onSeeAll != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onSeeAll() }
                        .padding(4.dp)
                ) {
                    Text(
                        text = "See All",
                        color = AmberGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = AmberGold,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { it.id }) { stream ->
                val isPortrait = stream.streamType != "live"
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MetalCharcoal),
                    border = BorderStroke(1.dp, Champagne.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .width(if (isPortrait) 136.dp else 168.dp)
                        .clickable { onItemClick(stream) }
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(if (isPortrait) 0.72f else 1.45f)
                                .background(MetalCharcoalElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            if (stream.streamIcon.isNotEmpty()) {
                                AsyncImage(
                                    model = stream.streamIcon,
                                    contentDescription = stream.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = when (stream.streamType) {
                                        "live" -> Icons.Default.LiveTv
                                        "series" -> Icons.Default.VideoLibrary
                                        else -> Icons.Default.Movie
                                    },
                                    contentDescription = null,
                                    tint = AmberGold,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            // Subtle bottom-to-top Dark Graphite gradient scrim
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, DarkGraphite.copy(alpha = 0.8f))
                                        )
                                    )
                            )

                            // Top-Right Pale Gold VIP / Stream Badge
                            Surface(
                                color = PaleGold,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    color = DarkGraphite,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            // Bottom-Right Amber Gold Play Button with Dark Graphite icon
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(6.dp)
                                    .size(28.dp)
                                    .background(AmberGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = DarkGraphite,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                            Text(
                                text = stream.name,
                                color = PaleGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = stream.streamType.uppercase(),
                                color = Champagne,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryHubCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MetalCharcoal),
        border = BorderStroke(1.dp, Champagne.copy(alpha = 0.25f)),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(AmberGold, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = DarkGraphite, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = PaleGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = Champagne, fontSize = 11.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Champagne)
        }
    }
}
