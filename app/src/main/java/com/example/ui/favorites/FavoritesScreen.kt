package com.example.ui.favorites

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
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
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
import com.example.ui.theme.FavoritesBotanicalBorder
import com.example.ui.theme.FavoritesEmeraldCard
import com.example.ui.theme.FavoritesEvergreenSurface
import com.example.ui.theme.FavoritesObsidianBase
import com.example.ui.theme.FavoritesPaleMint
import com.example.ui.theme.FavoritesSagePrimary
import com.example.ui.theme.PaleGold
import com.example.ui.viewmodel.MainViewModel

@Composable
fun FavoritesScreen(
    viewModel: MainViewModel,
    activePlaylist: PlaylistEntity?,
    onStreamClick: (StreamEntity) -> Unit,
    onToggleFavorite: (StreamEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (activePlaylist == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(FavoritesObsidianBase, FavoritesEvergreenSurface, FavoritesEmeraldCard)
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = FavoritesSagePrimary,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Active Xtream Account",
                    color = FavoritesPaleMint,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Connect your Xtream account to save and organize your VIP Favorites.",
                    color = FavoritesSagePrimary,
                    fontSize = 13.sp
                )
            }
        }
        return
    }

    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf<String?>(null) } // null = All, "live", "movie", "series"
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(favorites, selectedFilter, searchQuery) {
        favorites.filter { stream ->
            val matchesType = selectedFilter == null || stream.streamType.equals(selectedFilter, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() || stream.name.contains(searchQuery.trim(), ignoreCase = true)
            matchesType && matchesQuery
        }
    }

    val liveFavCount = remember(favorites) { favorites.count { it.streamType.equals("live", ignoreCase = true) } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        FavoritesObsidianBase,
                        FavoritesEvergreenSurface,
                        FavoritesObsidianBase
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("favorites_screen")
    ) {
        // =========================================================================
        // VIP FAVORITES BOTANICAL EMERALD & SAGE VAULT HEADER
        // (#051F20 • #0B2B26 • #163832 • #235347 • #8EB69B • #DAF1DE)
        // =========================================================================
        val movieFavCount = remember(favorites) { favorites.count { it.streamType.equals("movie", ignoreCase = true) } }
        val seriesFavCount = remember(favorites) { favorites.count { it.streamType.equals("series", ignoreCase = true) } }

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = FavoritesEmeraldCard),
            border = BorderStroke(
                width = 0.8.dp,
                color = FavoritesSagePrimary.copy(alpha = 0.38f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                FavoritesEvergreenSurface,
                                FavoritesEmeraldCard,
                                FavoritesBotanicalBorder.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = FavoritesSagePrimary.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.7.dp, FavoritesSagePrimary.copy(alpha = 0.50f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = FavoritesPaleMint,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "SAVED COLLECTION",
                                color = Color.White,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.4.sp
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.Center) {
                        Text(
                            text = "${favorites.size} Saved Items",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                        Text(
                            text = "$liveFavCount Live TV • $movieFavCount Movies • $seriesFavCount Series",
                            color = FavoritesSagePrimary.copy(alpha = 0.92f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.6.dp, FavoritesSagePrimary.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "$liveFavCount LIVE READY",
                        color = FavoritesPaleMint,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botanical Sage Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = "Filter saved VIP channels, movies & series (e.g. beIN, Arryadia, SSC)…",
                    color = FavoritesSagePrimary.copy(alpha = 0.65f),
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Favorites",
                    tint = FavoritesPaleMint
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = FavoritesSagePrimary
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FavoritesSagePrimary,
                unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                focusedContainerColor = FavoritesEmeraldCard,
                unfocusedContainerColor = FavoritesEvergreenSurface,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("favorites_search_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Botanical Sage & Mint Pill Filter Strip (Tier 1 Solid Active vs Tier 2 Surface Inactive)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            val filters = listOf(
                null to "All (${favorites.size})",
                "live" to "Live TV ($liveFavCount)",
                "movie" to "Movies",
                "series" to "Series"
            )
            items(filters, key = { it.first ?: "all" }) { (key, label) ->
                val isSelected = selectedFilter == key
                Surface(
                    color = if (isSelected) FavoritesSagePrimary else Color.White.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { selectedFilter = if (isSelected && key != null) null else key }
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) FavoritesObsidianBase else FavoritesPaleMint,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = FavoritesSagePrimary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No VIP Favorites yet.",
                        color = FavoritesPaleMint,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap the star icon on any Live TV channel (such as beIN Sports or Arryadia), movie, or series to pin it into your VIP Vault.",
                        color = FavoritesSagePrimary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList, key = { it.id }) { item ->
                    val isLiveChannel = item.streamType.equals("live", ignoreCase = true)
                    val isMenaSportsChannel = remember(item.name) {
                        val lower = item.name.lowercase()
                        lower.contains("bein") || lower.contains("arryadia") ||
                            lower.contains("riyadia") || lower.contains("snrt") ||
                            lower.contains("ssc") || lower.contains("ad sport") ||
                            lower.contains("alkass")
                    }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = FavoritesEmeraldCard),
                        border = BorderStroke(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.07f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStreamClick(item) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            FavoritesEvergreenSurface,
                                            FavoritesEmeraldCard
                                        )
                                    )
                                )
                                .padding(horizontal = 12.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.10f),
                                                FavoritesObsidianBase
                                            )
                                        )
                                    )
                                    .border(0.8.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(11.dp)),
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
                                        imageVector = when (item.streamType.lowercase()) {
                                            "live" -> Icons.Default.LiveTv
                                            "series" -> Icons.Default.VideoLibrary
                                            else -> Icons.Default.Movie
                                        },
                                        contentDescription = null,
                                        tint = FavoritesSagePrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    color = Color.White,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                // Unboxed Inline Metadata Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "VIP • ${item.streamType.uppercase()}",
                                        color = FavoritesSagePrimary,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    if (isLiveChannel && isMenaSportsChannel) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "•",
                                                color = FavoritesSagePrimary.copy(alpha = 0.6f),
                                                fontSize = 10.sp
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.SportsSoccer,
                                                contentDescription = null,
                                                tint = FavoritesPaleMint,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "KickOff Linked",
                                                color = FavoritesPaleMint,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }

                            // Tier 3: Borderless Ghost Favorite Star Button
                            IconButton(
                                onClick = { onToggleFavorite(item) },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Remove Favorite",
                                    tint = AmberGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Tier 1: Primary Solid Botanical Sage "▶ Watch" Pill CTA
                            Surface(
                                color = FavoritesSagePrimary,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .height(30.dp)
                                    .clip(RoundedCornerShape(50))
                                    .clickable { onStreamClick(item) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = FavoritesObsidianBase,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Watch",
                                        color = FavoritesObsidianBase,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        softWrap = false
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
