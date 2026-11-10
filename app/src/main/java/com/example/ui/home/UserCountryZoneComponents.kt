package com.example.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.BroadcastMacroRegion
import com.example.data.repository.SeasonTimeMode
import com.example.data.repository.UserCountryZone
import com.example.data.repository.UserZoneRegistry
import com.example.ui.theme.KickoffAgedParchment
import com.example.ui.theme.KickoffAlabasterIvory
import com.example.ui.theme.KickoffCharcoalElevated
import com.example.ui.theme.KickoffCharcoalSlate
import com.example.ui.theme.KickoffImperialNavy
import com.example.ui.theme.KickoffObsidianBlack
import com.example.ui.theme.KickoffParchmentBorder
import com.example.ui.theme.KickoffTerracotta
import com.example.ui.theme.KickoffTerracottaGlow

/**
 * Interactive Country Zone Capsule Button displayed in the KickOff Arena Header Bar.
 * Shows the user's selected Country Flag, ISO3 code, seasonal indicator (`☀️` / `❄️`),
 * and active UTC offset (`🇲🇦 MAR • ☀️ GMT+1` / `🇫🇷 FRA • ❄️ GMT+1`),
 * allowing 1-tap access to change the Country Zone or Winter/Summer time at any time.
 */
@Composable
fun UserCountryZoneCapsuleButton(
    selectedZone: UserCountryZone,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    seasonTimeMode: SeasonTimeMode = SeasonTimeMode.AUTO
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "country_zone_capsule_scale"
    )
    val utcBadge = remember(selectedZone.countryCode, seasonTimeMode) {
        selectedZone.getUtcOffsetBadge(mode = seasonTimeMode)
    }
    val seasonEmoji = remember(selectedZone.countryCode, seasonTimeMode) {
        if (!selectedZone.hasSeasonalTimeChange) {
            ""
        } else if (selectedZone.isSummerTimeActive(mode = seasonTimeMode)) {
            "☀️ "
        } else {
            "❄️ "
        }
    }

    Surface(
        color = KickoffCharcoalSlate,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            width = 0.9.dp,
            color = KickoffParchmentBorder
        ),
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .height(30.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("user_country_zone_btn")
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.5.dp)
                .height(30.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.5.dp)
        ) {
            Text(
                text = selectedZone.flagEmoji,
                fontSize = 12.sp
            )
            Text(
                text = selectedZone.shortIso3,
                color = KickoffAlabasterIvory,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.3.sp,
                maxLines = 1,
                softWrap = false
            )
            Box(
                modifier = Modifier
                    .size(3.dp)
                    .clip(CircleShape)
                    .background(KickoffTerracotta)
            )
            Text(
                text = "$seasonEmoji$utcBadge",
                color = KickoffAgedParchment,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                softWrap = false
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Change Country Zone",
                tint = KickoffAgedParchment,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

/**
 * Compact Country Zone Selector Banner embedded inside the First-Time KickOff Setup Dialog
 * and the Customize KickOff Arena sheet.
 * Lets the user pick their Country Zone AND toggle Winter vs. Summer time from the very first launch.
 */
@Composable
fun OnboardingCountryZoneSelectorCard(
    selectedZone: UserCountryZone,
    onSelectCountryCode: (String) -> Unit,
    onAutoDetectCountry: () -> Unit,
    onOpenFullZoneSheet: () -> Unit,
    seasonTimeMode: SeasonTimeMode = SeasonTimeMode.AUTO,
    onSelectSeasonTimeMode: (SeasonTimeMode) -> Unit = {}
) {
    val allZones = UserZoneRegistry.allCountryZones
    val utcBadge = remember(selectedZone.countryCode, seasonTimeMode) {
        selectedZone.getUtcOffsetBadge(mode = seasonTimeMode)
    }

    Surface(
        color = KickoffCharcoalSlate,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.dp,
            color = KickoffParchmentBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("onboarding_country_zone_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Row: Title + Auto-Detect & All Countries buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = KickoffTerracotta,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "USER'S ZONE • COUNTRY, TIME & TV",
                        color = KickoffAlabasterIvory,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.3.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = KickoffImperialNavy,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.7.dp, KickoffParchmentBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onAutoDetectCountry() }
                            .testTag("onboarding_auto_detect_zone_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = KickoffTerracotta,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Auto-Detect",
                                color = KickoffAlabasterIvory,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Surface(
                        color = KickoffTerracottaGlow,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.7.dp, KickoffTerracotta),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onOpenFullZoneSheet() }
                            .testTag("onboarding_open_all_zones_btn")
                    ) {
                        Text(
                            text = "All Zones (${allZones.size})",
                            color = KickoffAlabasterIvory,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Active Country Summary Pill (Flag + Name + Timezone + Primary TV Broadcaster)
            Surface(
                color = KickoffObsidianBlack,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(0.8.dp, KickoffParchmentBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = selectedZone.flagEmoji,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedZone.countryName,
                                    color = KickoffAlabasterIvory,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = KickoffImperialNavy,
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(0.5.dp, KickoffParchmentBorder)
                                ) {
                                    Text(
                                        text = utcBadge,
                                        color = KickoffAgedParchment,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "TV: ${selectedZone.primaryTvProviderName}",
                                color = KickoffAgedParchment,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        color = KickoffCharcoalSlate,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.5.dp, KickoffParchmentBorder)
                    ) {
                        Text(
                            text = selectedZone.iptvCategoryPrefixes.take(2).joinToString(" "),
                            color = KickoffAgedParchment,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Seasonal Clock Switcher (Winter Time vs. Summer Time) when the selected country uses 2 seasonal offsets
            if (selectedZone.hasSeasonalTimeChange) {
                SeasonTimeModeCompactSelectorRow(
                    zone = selectedZone,
                    selectedMode = seasonTimeMode,
                    onSelectMode = onSelectSeasonTimeMode
                )
            }

            // Horizontal Quick-Switch Country Pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 2.dp),
                modifier = Modifier.testTag("onboarding_country_pills_row")
            ) {
                items(allZones, key = { it.countryCode }) { zone ->
                    val isSelected = zone.countryCode.equals(selectedZone.countryCode, ignoreCase = true)
                    val bg by animateColorAsState(
                        targetValue = if (isSelected) KickoffTerracotta else KickoffObsidianBlack,
                        animationSpec = tween(180),
                        label = "onboarding_zone_bg_${zone.countryCode}"
                    )
                    Surface(
                        color = bg,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(
                            width = if (isSelected) 1.dp else 0.7.dp,
                            color = if (isSelected) KickoffAlabasterIvory.copy(alpha = 0.55f) else KickoffParchmentBorder
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onSelectCountryCode(zone.countryCode) }
                            .testTag("onboarding_zone_chip_${zone.countryCode}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.5.dp)
                        ) {
                            Text(text = zone.flagEmoji, fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = zone.countryName,
                                color = if (isSelected) KickoffAlabasterIvory else KickoffAgedParchment,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive 3-way Seasonal Time Switcher (`⚡ Auto`, `❄️ Winter Time`, `☀️ Summer Time`)
 * for countries that observe two different times during the year (Summer DST vs. Winter Standard Time,
 * or Morocco Standard `GMT+1` vs. Ramadan/Winter `GMT+0`).
 */
@Composable
fun SeasonTimeModeCompactSelectorRow(
    zone: UserCountryZone,
    selectedMode: SeasonTimeMode,
    onSelectMode: (SeasonTimeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = KickoffObsidianBlack,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.7.dp, KickoffParchmentBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("season_time_mode_selector_row")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SEASONAL CLOCK (WINTER / SUMMER TIME)",
                    color = KickoffAgedParchment,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.4.sp
                )
                Text(
                    text = zone.getActiveSeasonLabel(mode = selectedMode),
                    color = KickoffAlabasterIvory,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SeasonTimeMode.entries.forEach { mode ->
                    val isSelected = selectedMode == mode
                    val chipLabel = when (mode) {
                        SeasonTimeMode.AUTO -> "⚡ Auto"
                        SeasonTimeMode.WINTER -> "❄️ Winter (${zone.winterGmtBadge})"
                        SeasonTimeMode.SUMMER -> "☀️ Summer (${zone.summerGmtBadge})"
                    }
                    Surface(
                        color = if (isSelected) KickoffTerracotta else KickoffCharcoalSlate,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            width = 0.8.dp,
                            color = if (isSelected) KickoffAlabasterIvory.copy(alpha = 0.55f) else KickoffParchmentBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectMode(mode) }
                            .testTag("season_mode_btn_${mode.code.lowercase()}")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 5.5.dp)
                        ) {
                            Text(
                                text = chipLabel,
                                color = if (isSelected) KickoffAlabasterIvory else KickoffAgedParchment,
                                fontSize = 9.5.sp,
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
}

/**
 * Full "USER'S ZONE HUB" Modal Bottom Sheet (`UserCountryZoneBottomSheet`).
 * Allows the user to switch their Country Zone and Winter/Summer Seasonal Time anytime from anywhere in KickOff Arena.
 * Synchronizes:
 * 1. Local Timezone, Winter vs. Summer DST Clocks (`GMT+1`, `GMT+2`, `GMT+0`, `GMT+3`, `GMT-5`, `GMT-4`, etc.)
 * 2. Official Domestic & Continental TV Broadcasters per Tournament (`beIN Sports MENA`, `Canal+ Foot`, `Sky Sports / TNT Sports`, `Movistar LaLiga TV`, `DAZN`, `SSC Sports`, `CBS / Paramount+`, etc.)
 * 3. IPTV Bouquet Prefix Hints (`MA|`, `FR|`, `UK|`, `ES|`, `AR|`, `DE|`, `IT|`, `PT|`, `TR|`, `US|`, `BR|`) for 1-Tap Favorites matching.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UserCountryZoneBottomSheet(
    selectedZone: UserCountryZone,
    onSelectCountryZone: (String) -> Unit,
    onAutoDetectCountryZone: () -> Unit,
    onDismiss: () -> Unit,
    seasonTimeMode: SeasonTimeMode = SeasonTimeMode.AUTO,
    onSelectSeasonTimeMode: (SeasonTimeMode) -> Unit = {},
    onPrioritizeCountryLeague: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMacroRegion by remember { mutableStateOf(BroadcastMacroRegion.ALL) }
    var spotlightPinnedFeedback by remember(selectedZone.countryCode) { mutableStateOf(false) }

    val filteredZones = remember(searchQuery, selectedMacroRegion) {
        val byRegion = UserZoneRegistry.getZonesByMacroRegion(selectedMacroRegion)
        val q = searchQuery.trim().lowercase()
        if (q.isBlank()) {
            byRegion
        } else {
            byRegion.filter { zone ->
                zone.countryName.lowercase().contains(q) ||
                    zone.nativeOrRegionSubtitle.lowercase().contains(q) ||
                    zone.countryCode.lowercase().contains(q) ||
                    zone.shortIso3.lowercase().contains(q) ||
                    zone.primaryTvProviderName.lowercase().contains(q) ||
                    zone.secondaryTvProviders.any { it.lowercase().contains(q) }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = KickoffObsidianBlack,
        modifier = Modifier.testTag("user_country_zone_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 6.dp)
        ) {
            // 1. Sheet Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(KickoffImperialNavy)
                            .border(1.dp, KickoffTerracotta.copy(alpha = 0.70f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = KickoffTerracotta,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "The USER's Zone",
                            color = KickoffAlabasterIvory,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Sync Winter/Summer timezone, kickoff clocks & TV channels",
                            color = KickoffAgedParchment,
                            fontSize = 11.5.sp
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_country_zone_sheet_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = KickoffAlabasterIvory
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Active Country Zone Live Telemetry Banner + Winter/Summer Switcher + Auto-Detect Button
            ActiveCountryZoneTelemetryBanner(
                zone = selectedZone,
                seasonTimeMode = seasonTimeMode,
                onSelectSeasonTimeMode = onSelectSeasonTimeMode,
                onAutoDetect = onAutoDetectCountryZone,
                spotlightPinnedFeedback = spotlightPinnedFeedback,
                onPrioritizeCountryLeague = {
                    onPrioritizeCountryLeague()
                    spotlightPinnedFeedback = true
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Search Input (Country, ISO Code, or TV Provider)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Search country or TV network (Morocco, France, UK, Canal+, Sky, beIN)...",
                        color = KickoffAgedParchment.copy(alpha = 0.65f),
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Country Zone",
                        tint = KickoffTerracotta
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = KickoffAgedParchment
                            )
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KickoffTerracotta,
                    unfocusedBorderColor = KickoffParchmentBorder,
                    focusedContainerColor = KickoffCharcoalSlate,
                    unfocusedContainerColor = KickoffCharcoalSlate,
                    focusedTextColor = KickoffAlabasterIvory,
                    unfocusedTextColor = KickoffAlabasterIvory
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("country_zone_search_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Macro-Region Filter Tabs (All Zones | MENA & Arab World | Europe | Americas)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(BroadcastMacroRegion.entries, key = { it.name }) { region ->
                    val isSelected = selectedMacroRegion == region
                    Surface(
                        color = if (isSelected) KickoffTerracotta else KickoffCharcoalSlate,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(
                            width = 0.9.dp,
                            color = if (isSelected) KickoffAlabasterIvory else KickoffParchmentBorder
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { selectedMacroRegion = region }
                            .testTag("region_filter_${region.name.lowercase()}")
                    ) {
                        Text(
                            text = region.displayTitle,
                            color = if (isSelected) KickoffAlabasterIvory else KickoffAgedParchment,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5. Country Zones List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 220.dp, max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredZones, key = { it.countryCode }) { zone ->
                    val isSelected = zone.countryCode.equals(selectedZone.countryCode, ignoreCase = true)
                    CountryZoneOptionCard(
                        zone = zone,
                        seasonTimeMode = if (isSelected) seasonTimeMode else SeasonTimeMode.AUTO,
                        isSelected = isSelected,
                        onClick = {
                            onSelectCountryZone(zone.countryCode)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActiveCountryZoneTelemetryBanner(
    zone: UserCountryZone,
    seasonTimeMode: SeasonTimeMode,
    onSelectSeasonTimeMode: (SeasonTimeMode) -> Unit,
    onAutoDetect: () -> Unit,
    spotlightPinnedFeedback: Boolean,
    onPrioritizeCountryLeague: () -> Unit
) {
    val localClock = remember(zone.countryCode, seasonTimeMode) {
        zone.formatCurrentLocalClock(mode = seasonTimeMode)
    }
    val utcBadge = remember(zone.countryCode, seasonTimeMode) {
        zone.getUtcOffsetBadge(mode = seasonTimeMode)
    }

    Surface(
        color = KickoffImperialNavy,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.1.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    KickoffTerracotta,
                    KickoffAgedParchment.copy(alpha = 0.65f),
                    KickoffTerracotta
                )
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            KickoffImperialNavy,
                            KickoffCharcoalSlate
                        )
                    )
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = zone.flagEmoji,
                        fontSize = 26.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = zone.countryName,
                                color = KickoffAlabasterIvory,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = KickoffTerracotta.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.7.dp, KickoffTerracotta)
                            ) {
                                Text(
                                    text = "ACTIVE ZONE",
                                    color = KickoffAlabasterIvory,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
                                )
                            }
                        }
                        Text(
                            text = zone.getDualSeasonBadgeSummary(),
                            color = KickoffAgedParchment,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Auto-Detect Device Zone CTA
                Surface(
                    color = KickoffObsidianBlack,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.8.dp, KickoffTerracotta.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onAutoDetect() }
                        .testTag("auto_detect_country_zone_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Auto-Detect Country",
                            tint = KickoffTerracotta,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Auto-Detect",
                            color = KickoffAlabasterIvory,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Seasonal Time Mode Switcher (Auto vs. Winter Time vs. Summer Time)
            if (zone.hasSeasonalTimeChange) {
                SeasonTimeModeCompactSelectorRow(
                    zone = zone,
                    selectedMode = seasonTimeMode,
                    onSelectMode = onSelectSeasonTimeMode
                )
            }

            // Telemetry Row: Local Clock + UTC Offset + Primary TV Broadcasters + Pin Domestic League CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = KickoffObsidianBlack,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.7.dp, KickoffAgedParchment.copy(alpha = 0.50f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = KickoffAgedParchment,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$localClock • $utcBadge",
                            color = KickoffAlabasterIvory,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Surface(
                    color = KickoffObsidianBlack,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.7.dp, KickoffTerracotta.copy(alpha = 0.55f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LiveTv,
                            contentDescription = null,
                            tint = KickoffTerracotta,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = zone.primaryTvProviderName,
                            color = KickoffAlabasterIvory,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 1-Tap Pin Country's Domestic Spotlight Competition
                Surface(
                    color = if (spotlightPinnedFeedback) KickoffTerracotta else KickoffObsidianBlack,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        0.7.dp,
                        if (spotlightPinnedFeedback) KickoffAlabasterIvory else KickoffTerracotta.copy(alpha = 0.65f)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onPrioritizeCountryLeague() }
                        .testTag("pin_country_spotlight_league_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (spotlightPinnedFeedback) Icons.Default.Check else Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = if (spotlightPinnedFeedback) KickoffAlabasterIvory else KickoffTerracotta,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (spotlightPinnedFeedback) "Pinned #1" else "Pin League",
                            color = KickoffAlabasterIvory,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CountryZoneOptionCard(
    zone: UserCountryZone,
    seasonTimeMode: SeasonTimeMode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val utcBadge = remember(zone.countryCode, seasonTimeMode) {
        zone.getUtcOffsetBadge(mode = seasonTimeMode)
    }
    val localClock = remember(zone.countryCode, seasonTimeMode) {
        zone.formatCurrentLocalClock(mode = seasonTimeMode)
    }
    val dualSeasonSummary = remember(zone.countryCode) {
        if (zone.hasSeasonalTimeChange) {
            "❄️ ${zone.winterGmtBadge} / ☀️ ${zone.summerGmtBadge}"
        } else {
            "Permanent ${zone.winterGmtBadge}"
        }
    }

    Surface(
        color = if (isSelected) KickoffImperialNavy else KickoffCharcoalSlate,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = if (isSelected) 1.3.dp else 0.8.dp,
            color = if (isSelected) KickoffTerracotta else KickoffParchmentBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("country_zone_card_${zone.countryCode}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Country Flag Medallion
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) KickoffTerracotta.copy(alpha = 0.22f)
                            else KickoffObsidianBlack
                        )
                        .border(
                            width = 0.8.dp,
                            color = if (isSelected) KickoffTerracotta else KickoffParchmentBorder,
                            shape = CircleShape
                        )
                ) {
                    Text(
                        text = zone.flagEmoji,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = zone.countryName,
                            color = KickoffAlabasterIvory,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = KickoffObsidianBlack,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(0.5.dp, KickoffAgedParchment.copy(alpha = 0.55f))
                        ) {
                            Text(
                                text = "$localClock • $utcBadge",
                                color = KickoffAgedParchment,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = dualSeasonSummary,
                            color = KickoffAgedParchment,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "TV: ${zone.primaryTvProviderName}",
                        color = if (isSelected) KickoffAlabasterIvory else KickoffAgedParchment,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        zone.iptvCategoryPrefixes.take(3).forEach { prefix ->
                            Surface(
                                color = KickoffObsidianBlack,
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.5.dp, KickoffTerracotta.copy(alpha = 0.55f))
                            ) {
                                Text(
                                    text = prefix,
                                    color = KickoffAlabasterIvory,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 4.5.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "• ${zone.satelliteOrPlatformSummary}",
                            color = KickoffAgedParchment,
                            fontSize = 9.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isSelected) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(KickoffTerracotta)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected Country Zone",
                        tint = KickoffAlabasterIvory,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

