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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FavoriteTeamEntity
import com.example.data.model.TournamentPreferenceEntity
import com.example.data.repository.ApiKeyValidationResult
import com.example.data.repository.BroadcastMacroRegion
import com.example.data.repository.CompetitionCategory
import com.example.data.repository.FootballRepository
import com.example.data.repository.FootballTeam
import com.example.data.repository.KickoffProviderId
import com.example.data.repository.KickoffSourceHealthState
import com.example.data.repository.KickoffSourceMonitor
import com.example.data.repository.SeasonTimeMode
import com.example.data.repository.SourceHealthStatus
import com.example.data.repository.UserCountryZone
import com.example.data.repository.UserZoneRegistry
import com.example.ui.theme.KickoffCanopyElevated
import com.example.ui.theme.KickoffEmeraldBorder
import com.example.ui.theme.KickoffFloodlightVolt
import com.example.ui.theme.KickoffFloodlightWhite
import com.example.ui.theme.KickoffGlassWell
import com.example.ui.theme.KickoffLiveCrimson
import com.example.ui.theme.KickoffMistSilver
import com.example.ui.theme.KickoffPitchBlack
import com.example.ui.theme.KickoffPitchEmerald
import com.example.ui.theme.KickoffStadiumForest
import com.example.ui.theme.KickoffTrophyGold
import com.example.ui.theme.KickoffTurfSurface
import com.example.ui.theme.TabularNumeralTextStyle
import com.example.ui.theme.tvFocusableControl
import com.example.ui.viewmodel.LiveGoalAlertEvent
import com.example.util.SetPlayEdition

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
        color = KickoffTurfSurface,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            width = 0.9.dp,
            color = KickoffEmeraldBorder
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
                color = KickoffFloodlightWhite,
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
                    .background(KickoffPitchEmerald)
            )
            Text(
                text = "$seasonEmoji$utcBadge",
                color = KickoffMistSilver,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                softWrap = false
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Change Country Zone",
                tint = KickoffMistSilver,
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

    val providerStyle = remember(selectedZone.primaryTvProviderName) {
        resolveBroadcastProviderBadgeStyle(selectedZone.primaryTvProviderName)
    }

    Surface(
        color = KickoffTurfSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.dp,
            color = KickoffEmeraldBorder
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
                        tint = KickoffPitchEmerald,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "USER'S ZONE • COUNTRY, TIME & TV",
                        color = KickoffFloodlightWhite,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.3.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = KickoffCanopyElevated,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.7.dp, KickoffEmeraldBorder),
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
                                tint = KickoffPitchEmerald,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Auto-Detect",
                                color = KickoffFloodlightWhite,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Surface(
                        color = KickoffPitchEmerald.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.7.dp, KickoffPitchEmerald),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onOpenFullZoneSheet() }
                            .testTag("onboarding_open_all_zones_btn")
                    ) {
                        Text(
                            text = "All Zones (${allZones.size})",
                            color = KickoffFloodlightWhite,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Active Country Summary Pill (Flag + Name + Timezone + Primary TV Broadcaster)
            Surface(
                color = KickoffGlassWell,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(0.8.dp, KickoffEmeraldBorder),
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
                                    color = KickoffFloodlightWhite,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = KickoffStadiumForest,
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(0.5.dp, KickoffEmeraldBorder)
                                ) {
                                    Text(
                                        text = utcBadge,
                                        color = KickoffFloodlightVolt,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "TV: ${selectedZone.primaryTvProviderName}",
                                color = providerStyle.textColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        color = providerStyle.surfaceColor,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.6.dp, providerStyle.borderColor)
                    ) {
                        Text(
                            text = selectedZone.iptvCategoryPrefixes.take(2).joinToString(" "),
                            color = providerStyle.textColor,
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
                        targetValue = if (isSelected) KickoffPitchEmerald else KickoffGlassWell,
                        animationSpec = tween(180),
                        label = "onboarding_zone_bg_${zone.countryCode}"
                    )
                    Surface(
                        color = bg,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(
                            width = if (isSelected) 1.dp else 0.7.dp,
                            color = if (isSelected) KickoffFloodlightVolt.copy(alpha = 0.75f) else KickoffEmeraldBorder
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
                                color = if (isSelected) KickoffPitchBlack else KickoffFloodlightWhite,
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
        color = KickoffGlassWell,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.7.dp, KickoffEmeraldBorder),
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
                    color = KickoffMistSilver,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.4.sp
                )
                Text(
                    text = zone.getActiveSeasonLabel(mode = selectedMode),
                    color = KickoffFloodlightVolt,
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
                        color = if (isSelected) KickoffPitchEmerald else KickoffTurfSurface,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            width = 0.8.dp,
                            color = if (isSelected) KickoffFloodlightVolt.copy(alpha = 0.75f) else KickoffEmeraldBorder
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
                                color = if (isSelected) KickoffPitchBlack else KickoffFloodlightWhite,
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
        containerColor = KickoffPitchBlack,
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
                            .background(KickoffStadiumForest)
                            .border(1.dp, KickoffPitchEmerald.copy(alpha = 0.70f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = KickoffPitchEmerald,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "The USER's Zone",
                            color = KickoffFloodlightWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Sync Winter/Summer timezone, kickoff clocks & TV channels",
                            color = KickoffMistSilver,
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
                        tint = KickoffFloodlightWhite
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
                        color = KickoffMistSilver.copy(alpha = 0.65f),
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Country Zone",
                        tint = KickoffPitchEmerald
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = KickoffMistSilver
                            )
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KickoffPitchEmerald,
                    unfocusedBorderColor = KickoffEmeraldBorder,
                    focusedContainerColor = KickoffTurfSurface,
                    unfocusedContainerColor = KickoffTurfSurface,
                    focusedTextColor = KickoffFloodlightWhite,
                    unfocusedTextColor = KickoffFloodlightWhite
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
                        color = if (isSelected) KickoffPitchEmerald else KickoffTurfSurface,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(
                            width = 0.9.dp,
                            color = if (isSelected) KickoffFloodlightVolt else KickoffEmeraldBorder
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { selectedMacroRegion = region }
                            .testTag("region_filter_${region.name.lowercase()}")
                    ) {
                        Text(
                            text = region.displayTitle,
                            color = if (isSelected) KickoffPitchBlack else KickoffFloodlightWhite,
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
    val providerStyle = remember(zone.primaryTvProviderName) {
        resolveBroadcastProviderBadgeStyle(zone.primaryTvProviderName)
    }

    Surface(
        color = KickoffStadiumForest,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.1.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    KickoffPitchEmerald,
                    KickoffFloodlightVolt.copy(alpha = 0.65f),
                    KickoffPitchEmerald
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
                            KickoffStadiumForest,
                            KickoffTurfSurface
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
                                color = KickoffFloodlightWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = KickoffPitchEmerald.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.7.dp, KickoffPitchEmerald)
                            ) {
                                Text(
                                    text = "ACTIVE ZONE",
                                    color = KickoffFloodlightVolt,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
                                )
                            }
                        }
                        Text(
                            text = zone.getDualSeasonBadgeSummary(),
                            color = KickoffMistSilver,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Auto-Detect Device Zone CTA
                Surface(
                    color = KickoffGlassWell,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.8.dp, KickoffPitchEmerald.copy(alpha = 0.7f)),
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
                            tint = KickoffPitchEmerald,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Auto-Detect",
                            color = KickoffFloodlightWhite,
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
                    color = KickoffGlassWell,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.7.dp, KickoffEmeraldBorder)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = KickoffPitchEmerald,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$localClock • $utcBadge",
                            color = KickoffFloodlightWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Surface(
                    color = providerStyle.surfaceColor,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.7.dp, providerStyle.borderColor),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LiveTv,
                            contentDescription = null,
                            tint = providerStyle.borderColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = zone.primaryTvProviderName,
                            color = providerStyle.textColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 1-Tap Pin Country's Domestic Spotlight Competition
                Surface(
                    color = if (spotlightPinnedFeedback) KickoffTrophyGold else KickoffGlassWell,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        0.7.dp,
                        if (spotlightPinnedFeedback) KickoffFloodlightWhite else KickoffTrophyGold.copy(alpha = 0.65f)
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
                            tint = if (spotlightPinnedFeedback) KickoffPitchBlack else KickoffTrophyGold,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (spotlightPinnedFeedback) "Pinned #1" else "Pin League",
                            color = if (spotlightPinnedFeedback) KickoffPitchBlack else KickoffFloodlightWhite,
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
    val providerStyle = remember(zone.primaryTvProviderName) {
        resolveBroadcastProviderBadgeStyle(zone.primaryTvProviderName)
    }

    Surface(
        color = if (isSelected) KickoffCanopyElevated else KickoffTurfSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = if (isSelected) 1.3.dp else 0.8.dp,
            color = if (isSelected) KickoffPitchEmerald else KickoffEmeraldBorder
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
                            if (isSelected) KickoffPitchEmerald.copy(alpha = 0.22f)
                            else KickoffGlassWell
                        )
                        .border(
                            width = 0.8.dp,
                            color = if (isSelected) KickoffPitchEmerald else KickoffEmeraldBorder,
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
                            color = KickoffFloodlightWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = KickoffGlassWell,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(0.5.dp, KickoffEmeraldBorder)
                        ) {
                            Text(
                                text = "$localClock • $utcBadge",
                                color = KickoffFloodlightVolt,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = dualSeasonSummary,
                            color = KickoffMistSilver,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "TV: ${zone.primaryTvProviderName}",
                        color = providerStyle.textColor,
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
                                color = providerStyle.surfaceColor,
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.5.dp, providerStyle.borderColor)
                            ) {
                                Text(
                                    text = prefix,
                                    color = providerStyle.textColor,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 4.5.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "• ${zone.satelliteOrPlatformSummary}",
                            color = KickoffMistSilver,
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
                        .background(KickoffPitchEmerald)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected Country Zone",
                        tint = KickoffPitchBlack,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Live Goal Alert Banner (`LiveGoalAlertBanner`) harmonized with the Floodlight Noir & Pitch Emerald Brand Kit.
 */
@Composable
fun LiveGoalAlertBanner(
    alert: LiveGoalAlertEvent,
    onOpenDossier: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = KickoffStadiumForest,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.3.dp, KickoffFloodlightVolt),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("live_goal_alert_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            KickoffStadiumForest,
                            KickoffTurfSurface,
                            KickoffPitchBlack
                        )
                    )
                )
                .clickable { onOpenDossier() }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = KickoffLiveCrimson,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.8.dp, KickoffFloodlightWhite.copy(alpha = 0.55f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsSoccer,
                            contentDescription = "Goal Scored",
                            tint = KickoffFloodlightWhite,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "GOAL! • ${alert.minuteLabel}",
                            color = KickoffFloodlightWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${alert.homeTeamName}  ${alert.homeScore} - ${alert.awayScore}  ${alert.awayTeamName}",
                        color = KickoffFloodlightWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        style = TabularNumeralTextStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "⚽ Scored by ${alert.scoringTeamName} • Tap for Match Center Dossier",
                        color = KickoffMistSilver,
                        fontSize = 10.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("dismiss_live_goal_alert_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss Goal Alert",
                    tint = KickoffMistSilver,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * First-Time Favorite Teams & Country Zone Onboarding Modal (`FirstTimeFavoriteTeamsDialog`)
 * styled with the Floodlight Noir & Pitch Emerald Brand Color Kit.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FirstTimeFavoriteTeamsDialog(
    selectedCountryZone: UserCountryZone = UserZoneRegistry.defaultCountryZone,
    selectedSeasonTimeMode: SeasonTimeMode = SeasonTimeMode.AUTO,
    searchQuery: String,
    searchResults: List<FootballTeam>,
    isSearching: Boolean,
    selectedFavorites: List<FavoriteTeamEntity>,
    onSelectCountryZone: (String) -> Unit = {},
    onSelectSeasonTimeMode: (SeasonTimeMode) -> Unit = {},
    onAutoDetectCountryZone: () -> Unit = {},
    onOpenFullCountryZoneSheet: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit,
    onToggleTeam: (FootballTeam) -> Unit,
    onRemoveFavoriteTeam: (String) -> Unit,
    onCompleteOnboarding: () -> Unit
) {
    Dialog(
        onDismissRequest = onCompleteOnboarding,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = KickoffPitchBlack,
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.2.dp, KickoffPitchEmerald),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp)
                .testTag("first_time_teams_onboarding_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SportsSoccer,
                            contentDescription = null,
                            tint = KickoffPitchEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Welcome to KickOff Arena",
                                color = KickoffFloodlightWhite,
                                fontSize = 17.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Choose your Country Zone (time & TV channels) and follow your teams",
                                color = KickoffMistSilver,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Step 1: User's Country Zone Selector (Timezone, Winter/Summer Clocks & Official TV Providers)
                OnboardingCountryZoneSelectorCard(
                    selectedZone = selectedCountryZone,
                    onSelectCountryCode = onSelectCountryZone,
                    onAutoDetectCountry = onAutoDetectCountryZone,
                    onOpenFullZoneSheet = onOpenFullCountryZoneSheet,
                    seasonTimeMode = selectedSeasonTimeMode,
                    onSelectSeasonTimeMode = onSelectSeasonTimeMode
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Manual Global Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = "Type a team name (e.g., Real Madrid, Raja, Arsenal, PSG)...",
                            color = KickoffMistSilver.copy(alpha = 0.65f),
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Global Teams",
                            tint = KickoffPitchEmerald
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = KickoffMistSilver
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KickoffPitchEmerald,
                        unfocusedBorderColor = KickoffEmeraldBorder,
                        focusedContainerColor = KickoffTurfSurface,
                        unfocusedContainerColor = KickoffTurfSurface,
                        focusedTextColor = KickoffFloodlightWhite,
                        unfocusedTextColor = KickoffFloodlightWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_team_search_input")
                )

                // Selected Favorite Teams Pills
                if (selectedFavorites.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "SELECTED TEAMS (${selectedFavorites.size})",
                        color = KickoffMistSilver,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        selectedFavorites.forEach { fav ->
                            Surface(
                                color = KickoffCanopyElevated,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, KickoffPitchEmerald)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    TeamCrestImage(
                                        badgeUrl = fav.badgeUrl,
                                        teamName = fav.teamName,
                                        sizeDp = 16
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = fav.teamName,
                                        color = KickoffFloodlightWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove ${fav.teamName}",
                                        tint = KickoffPitchEmerald,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { onRemoveFavoriteTeam(fav.teamId) }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Results Area
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(min = 140.dp, max = 230.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(KickoffTurfSurface)
                        .padding(8.dp)
                ) {
                    when {
                        isSearching -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = KickoffPitchEmerald)
                            }
                        }
                        searchQuery.isBlank() -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = KickoffPitchEmerald.copy(alpha = 0.85f),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Search Any Team Globally",
                                    color = KickoffFloodlightWhite,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Popular in ${selectedCountryZone.flagEmoji} ${selectedCountryZone.countryName}: ${selectedCountryZone.popularLocalClubs.joinToString(", ")}. Type any club above to add it.",
                                    color = KickoffMistSilver,
                                    fontSize = 11.5.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        searchResults.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No teams found for \"$searchQuery\". Try another club name.",
                                    color = KickoffMistSilver,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        else -> {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(searchResults, key = { it.teamId }) { team ->
                                    val isSelected = selectedFavorites.any {
                                        it.teamId.equals(team.teamId, ignoreCase = true) ||
                                            it.teamName.equals(team.name, ignoreCase = true)
                                    }
                                    TeamSearchResultRow(
                                        team = team,
                                        isSelected = isSelected,
                                        onToggle = { onToggleTeam(team) }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onCompleteOnboarding,
                        border = BorderStroke(1.dp, KickoffEmeraldBorder),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("onboarding_skip_btn")
                    ) {
                        Text("Skip for Now", color = KickoffMistSilver, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onCompleteOnboarding,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KickoffPitchEmerald,
                            contentColor = KickoffPitchBlack
                        ),
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("onboarding_save_teams_btn")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedFavorites.isEmpty()) "Enter KickOff Arena" else "Save (${selectedFavorites.size}) & Continue",
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun TeamSearchResultRow(
    team: FootballTeam,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        color = if (isSelected) KickoffCanopyElevated else KickoffGlassWell,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            width = 0.8.dp,
            color = if (isSelected) KickoffPitchEmerald else KickoffEmeraldBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .testTag("team_search_result_${team.teamId}")
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
                TeamCrestImage(
                    badgeUrl = team.badgeUrl,
                    teamName = team.name,
                    sizeDp = 28
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = team.name,
                        color = KickoffFloodlightWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${team.country} • ${team.primaryLeague}",
                        color = KickoffMistSilver,
                        fontSize = 11.sp
                    )
                }
            }

            Icon(
                imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = if (isSelected) "Remove Favorite" else "Add Favorite",
                tint = if (isSelected) KickoffTrophyGold else KickoffMistSilver
            )
        }
    }
}

/**
 * Sleek, non-intrusive status alert banner displayed when a configured Kickoff API key expires (401/403),
 * reaches its usage quota (429), or becomes unreachable.
 */
@Composable
fun KickoffSourceStatusAlertBanner(
    alertMessage: String,
    onManageSourcesClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = KickoffTurfSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, KickoffTrophyGold),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("kickoff_source_alert_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = KickoffTrophyGold,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "SOURCE ALERT",
                        color = KickoffPitchBlack,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = alertMessage,
                    color = KickoffFloodlightWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = KickoffCanopyElevated,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.8.dp, KickoffPitchEmerald),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onManageSourcesClick() }
                        .testTag("kickoff_source_alert_manage_btn")
                ) {
                    Text(
                        text = "Sources & Keys",
                        color = KickoffFloodlightWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(26.dp)
                        .testTag("dismiss_kickoff_source_alert_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss Source Alert",
                        tint = KickoffMistSilver,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

/**
 * Unified 4-Tab Kickoff Settings & Source Management Hub (`CustomizeTeamsAndTournamentsSheet`):
 * - Tab 0: My Teams (`"My Favorite Teams (N)"` — global team search & saved favorites)
 * - Tab 1: Competitions (`"Tournaments (13)"` — 3-Pillar Competition Manager: European, African & Arab, International)
 * - Tab 2: Region & Time (`"Zone & Clock"` — Country Broadcast Zone selector + Auto / Winter / Summer Seasonal Clock)
 * - Tab 3: Sources & API Keys (`"Sources & Keys"` — real-time health & quota monitor + live key validation & encrypted replacement)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizeTeamsAndTournamentsSheet(
    selectedCountryZone: UserCountryZone = UserZoneRegistry.defaultCountryZone,
    selectedSeasonTimeMode: SeasonTimeMode = SeasonTimeMode.AUTO,
    searchQuery: String,
    searchResults: List<FootballTeam>,
    isSearching: Boolean,
    favoriteTeams: List<FavoriteTeamEntity>,
    tournamentPreferences: List<TournamentPreferenceEntity>,
    onSelectCountryZone: (String) -> Unit = {},
    onSelectSeasonTimeMode: (SeasonTimeMode) -> Unit = {},
    onAutoDetectCountryZone: () -> Unit = {},
    onOpenFullCountryZoneSheet: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit,
    onToggleFavoriteTeam: (FootballTeam) -> Unit,
    onRemoveFavoriteTeam: (String) -> Unit,
    onSetTournamentEnabled: (String, Boolean) -> Unit,
    onToggleTournamentPinned: (String) -> Unit,
    onEnableAllTournaments: () -> Unit,
    onDismiss: () -> Unit,
    sourceHealthStates: List<KickoffSourceHealthState> = emptyList(),
    sourceAlertMessage: String? = null,
    isCheckingSourcesHealth: Boolean = false,
    initialTabIndex: Int = 0,
    onCheckAllSourcesHealth: () -> Unit = {},
    onValidateAndUpdateApiKey: (KickoffProviderId, String, (ApiKeyValidationResult) -> Unit) -> Unit = { _, _, _ -> },
    onResetApiKey: (KickoffProviderId) -> Unit = {},
    onDismissSourceAlert: () -> Unit = {},
    activeEdition: SetPlayEdition = SetPlayEdition.PHONE,
    uiPreferences: KickoffUiPreferences = KickoffUiPreferences(),
    onUpdateUiPreferences: ((KickoffUiPreferences) -> KickoffUiPreferences) -> Unit = {}
) {
    var activeTab by remember(initialTabIndex) { mutableIntStateOf(initialTabIndex.coerceIn(0, 3)) }
    // Domain 0 sub-filter: 0 = Favorite Teams, 1 = Followed Leagues, 2 = Both (All)
    var domainZeroSubFilter by remember { mutableIntStateOf(2) }
    var zoneTabSearchQuery by remember { mutableStateOf("") }

    val effectiveSourceStates = remember(sourceHealthStates) {
        if (sourceHealthStates.isNotEmpty()) {
            sourceHealthStates
        } else {
            KickoffProviderId.entries.map { provider ->
                val defaultKey = KickoffSourceMonitor.getDefaultBuildConfigKey(provider)
                val isConfigured = !provider.requiresApiKey || defaultKey.isNotBlank()
                KickoffSourceHealthState(
                    provider = provider,
                    status = if (isConfigured) SourceHealthStatus.HEALTHY else SourceHealthStatus.UNCONFIGURED,
                    statusDetail = if (!provider.requiresApiKey) {
                        "Public feed active • Zero API key required"
                    } else if (isConfigured) {
                        "Built-in API key active • Ready for live requests"
                    } else {
                        "Optional key not set • Using multi-provider fallback"
                    },
                    remainingQuotaLabel = KickoffSourceMonitor.defaultQuotaLabel(provider, isConfigured),
                    isUsingCustomKey = false,
                    maskedKeyPreview = KickoffSourceMonitor.maskApiKey(defaultKey, provider.requiresApiKey)
                )
            }
        }
    }

    val groupedTournamentsByCategory = remember(tournamentPreferences) {
        val categoryById = FootballRepository.ALL_TOURNAMENTS.associate { it.tournamentId to it.category }
        CompetitionCategory.entries.mapNotNull { category ->
            val itemsForCat = tournamentPreferences.filter { pref ->
                (categoryById[pref.tournamentId] ?: CompetitionCategory.EUROPEAN) == category
            }
            if (itemsForCat.isEmpty()) null else (category to itemsForCat)
        }
    }

    val unhealthySourcesCount = remember(effectiveSourceStates, sourceAlertMessage) {
        val count = effectiveSourceStates.count {
            it.status == SourceHealthStatus.KEY_INVALID_OR_EXPIRED ||
                it.status == SourceHealthStatus.QUOTA_WARNING ||
                it.status == SourceHealthStatus.UNREACHABLE
        }
        if (count == 0 && !sourceAlertMessage.isNullOrBlank()) 1 else count
    }

    val domainDescriptors = remember(
        favoriteTeams.size,
        tournamentPreferences,
        selectedCountryZone.shortIso3,
        unhealthySourcesCount
    ) {
        val enabledLeaguesCount = tournamentPreferences.count { it.isEnabled }
        listOf(
            Triple("Teams & Leagues", "${favoriteTeams.size} Clubs • $enabledLeaguesCount Leagues", false),
            Triple("Region & Clocks", "${selectedCountryZone.flagEmoji} ${selectedCountryZone.shortIso3} • Timezone", false),
            Triple("Viewing & UX", "Spoilers • Density • Fallback", false),
            Triple(
                "Sources & Keys",
                if (unhealthySourcesCount > 0) "$unhealthySourcesCount Alert(s) Active" else "6 Feeds Healthy",
                unhealthySourcesCount > 0
            )
        )
    }

    val renderActiveDomainContent: @Composable (Modifier) -> Unit = { contentModifier ->
        Column(modifier = contentModifier) {
            when (activeTab) {
                0 -> {
                    // Domain 0: Teams & Leagues (Segmented sub-toggle: Favorite Teams | Followed Leagues | Both)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(KickoffTurfSurface)
                            .border(0.8.dp, KickoffEmeraldBorder, RoundedCornerShape(10.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val subTabs = listOf(
                            "★ Favorite Teams (${favoriteTeams.size})",
                            "🏆 Followed Leagues (${tournamentPreferences.count { it.isEnabled }})",
                            "All"
                        )
                        subTabs.forEachIndexed { idx, label ->
                            val selected = domainZeroSubFilter == idx
                            Surface(
                                color = if (selected) KickoffPitchEmerald else Color.Transparent,
                                shape = RoundedCornerShape(7.dp),
                                modifier = Modifier
                                    .weight(if (idx == 2) 0.55f else 1f)
                                    .clip(RoundedCornerShape(7.dp))
                                    .tvFocusableControl(
                                        accentColor = KickoffPitchEmerald,
                                        shape = RoundedCornerShape(7.dp),
                                        onClick = { domainZeroSubFilter = idx }
                                    )
                                    .testTag("domain0_subfilter_$idx")
                            ) {
                                Text(
                                    text = label,
                                    color = if (selected) KickoffPitchBlack else KickoffFloodlightWhite,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (domainZeroSubFilter == 0 || domainZeroSubFilter == 2) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = {
                                Text(
                                    text = "Search global teams to add (e.g., Wydad, Barcelona, Liverpool)...",
                                    color = KickoffMistSilver.copy(alpha = 0.65f),
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = KickoffPitchEmerald)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { onSearchQueryChange("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = KickoffMistSilver)
                                    }
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KickoffPitchEmerald,
                                unfocusedBorderColor = KickoffEmeraldBorder,
                                focusedContainerColor = KickoffTurfSurface,
                                unfocusedContainerColor = KickoffTurfSurface,
                                focusedTextColor = KickoffFloodlightWhite,
                                unfocusedTextColor = KickoffFloodlightWhite
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("customize_team_search_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (domainZeroSubFilter == 1 || domainZeroSubFilter == 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Toggle tournaments or pin them to the top",
                                color = KickoffMistSilver,
                                fontSize = 11.5.sp
                            )
                            TextButton(onClick = onEnableAllTournaments) {
                                Text("Enable All", color = KickoffPitchEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (domainZeroSubFilter == 0 || domainZeroSubFilter == 2) {
                            if (searchQuery.isNotBlank()) {
                                if (isSearching) {
                                    item(key = "team_searching_spinner") {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = KickoffPitchEmerald)
                                        }
                                    }
                                } else {
                                    items(searchResults, key = { "search_${it.teamId}" }) { team ->
                                        val isFav = favoriteTeams.any {
                                            it.teamId.equals(team.teamId, ignoreCase = true) ||
                                                it.teamName.equals(team.name, ignoreCase = true)
                                        }
                                        TeamSearchResultRow(
                                            team = team,
                                            isSelected = isFav,
                                            onToggle = { onToggleFavoriteTeam(team) }
                                        )
                                    }
                                }
                            } else {
                                if (favoriteTeams.isEmpty()) {
                                    item(key = "empty_fav_teams_hint") {
                                        Text(
                                            text = "No favorite teams saved yet. Use the search bar above to find and follow your favorite clubs.",
                                            color = KickoffMistSilver,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                } else {
                                    items(favoriteTeams, key = { "fav_${it.teamId}" }) { fav ->
                                        Surface(
                                            color = KickoffTurfSurface,
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(0.8.dp, KickoffEmeraldBorder),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    TeamCrestImage(
                                                        badgeUrl = fav.badgeUrl,
                                                        teamName = fav.teamName,
                                                        sizeDp = 26
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column {
                                                        Text(
                                                            text = fav.teamName,
                                                            color = KickoffFloodlightWhite,
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        if (fav.primaryLeague.isNotBlank()) {
                                                            Text(
                                                                text = "${fav.country} • ${fav.primaryLeague}",
                                                                color = KickoffMistSilver,
                                                                fontSize = 11.sp
                                                            )
                                                        }
                                                    }
                                                }
                                                IconButton(
                                                    onClick = { onRemoveFavoriteTeam(fav.teamId) },
                                                    modifier = Modifier.testTag("remove_fav_team_${fav.teamId}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Remove ${fav.teamName}",
                                                        tint = KickoffLiveCrimson
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (domainZeroSubFilter == 1 || domainZeroSubFilter == 2) {
                            groupedTournamentsByCategory.forEach { (category, prefsForCategory) ->
                                val categoryHeaderTitle = when (category) {
                                    CompetitionCategory.EUROPEAN -> "European Competitions"
                                    CompetitionCategory.AFRICAN_ARAB -> "African & Arab Competitions"
                                    CompetitionCategory.INTERNATIONAL -> "International Fixtures"
                                }
                                item(key = "cat_header_${category.name}") {
                                    Surface(
                                        color = KickoffStadiumForest,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(0.8.dp, KickoffEmeraldBorder),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp)
                                            .testTag("customize_category_header_${category.name.lowercase()}")
                                    ) {
                                        Text(
                                            text = categoryHeaderTitle,
                                            color = KickoffFloodlightWhite,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }

                                items(prefsForCategory, key = { it.tournamentId }) { pref ->
                                    val compAccent = resolveCompetitionAccentColor(pref.tournamentId)
                                    Surface(
                                        color = KickoffTurfSurface,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(
                                            width = 0.8.dp,
                                            color = if (pref.isPinned) compAccent else KickoffEmeraldBorder
                                        ),
                                        modifier = Modifier.fillMaxWidth()
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
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                IconButton(
                                                    onClick = { onToggleTournamentPinned(pref.tournamentId) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PushPin,
                                                        contentDescription = "Pin ${pref.tournamentName}",
                                                        tint = if (pref.isPinned) KickoffTrophyGold else KickoffMistSilver.copy(alpha = 0.65f),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Column {
                                                    Text(
                                                        text = pref.tournamentName,
                                                        color = KickoffFloodlightWhite,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = pref.countryOrRegion,
                                                        color = KickoffMistSilver,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }

                                            Switch(
                                                checked = pref.isEnabled,
                                                onCheckedChange = { enabled ->
                                                    onSetTournamentEnabled(pref.tournamentId, enabled)
                                                },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = KickoffPitchBlack,
                                                    checkedTrackColor = KickoffPitchEmerald,
                                                    uncheckedThumbColor = KickoffMistSilver,
                                                    uncheckedTrackColor = KickoffGlassWell
                                                ),
                                                modifier = Modifier.testTag("toggle_tournament_${pref.tournamentId}")
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Domain 1: Region & Clocks (Unified Localization Center — Zero Duplication)
                    ActiveCountryZoneTelemetryBanner(
                        zone = selectedCountryZone,
                        seasonTimeMode = selectedSeasonTimeMode,
                        onSelectSeasonTimeMode = onSelectSeasonTimeMode,
                        onAutoDetect = onAutoDetectCountryZone,
                        spotlightPinnedFeedback = false,
                        onPrioritizeCountryLeague = {}
                    )

                    if (!selectedCountryZone.hasSeasonalTimeChange) {
                        Spacer(modifier = Modifier.height(8.dp))
                        SeasonTimeModeCompactSelectorRow(
                            zone = selectedCountryZone,
                            selectedMode = selectedSeasonTimeMode,
                            onSelectMode = onSelectSeasonTimeMode
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 24-Hour Broadcast Clock Preference Switch
                    Surface(
                        color = KickoffTurfSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.8.dp, KickoffEmeraldBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "24-Hour Broadcast Clock",
                                    color = KickoffFloodlightWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Display kickoff times in 24-hour stadium format (e.g. 20:00 vs 8:00 PM)",
                                    color = KickoffMistSilver,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Switch(
                                checked = uiPreferences.use24HourFormat,
                                onCheckedChange = { enabled ->
                                    onUpdateUiPreferences { it.copy(use24HourFormat = enabled) }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = KickoffPitchBlack,
                                    checkedTrackColor = KickoffPitchEmerald,
                                    uncheckedThumbColor = KickoffMistSilver,
                                    uncheckedTrackColor = KickoffGlassWell
                                ),
                                modifier = Modifier.testTag("pref_switch_24h_format")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val filteredTabZones = remember(zoneTabSearchQuery) {
                        val q = zoneTabSearchQuery.trim().lowercase()
                        if (q.isBlank()) {
                            UserZoneRegistry.allCountryZones
                        } else {
                            UserZoneRegistry.allCountryZones.filter { z ->
                                z.countryName.lowercase().contains(q) ||
                                    z.countryCode.lowercase().contains(q) ||
                                    z.shortIso3.lowercase().contains(q) ||
                                    z.primaryTvProviderName.lowercase().contains(q)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = zoneTabSearchQuery,
                        onValueChange = { zoneTabSearchQuery = it },
                        placeholder = {
                            Text(
                                text = "Filter Country Zone or TV broadcaster (e.g. Morocco, Spain, France)...",
                                color = KickoffMistSilver.copy(alpha = 0.65f),
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Public, contentDescription = null, tint = KickoffPitchEmerald)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KickoffPitchEmerald,
                            unfocusedBorderColor = KickoffEmeraldBorder,
                            focusedContainerColor = KickoffTurfSurface,
                            unfocusedContainerColor = KickoffTurfSurface,
                            focusedTextColor = KickoffFloodlightWhite,
                            unfocusedTextColor = KickoffFloodlightWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customize_zone_tab_search_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredTabZones, key = { "tab1_${it.countryCode}" }) { zone ->
                            val isSelected = zone.countryCode.equals(selectedCountryZone.countryCode, ignoreCase = true)
                            CountryZoneOptionCard(
                                zone = zone,
                                seasonTimeMode = if (isSelected) selectedSeasonTimeMode else SeasonTimeMode.AUTO,
                                isSelected = isSelected,
                                onClick = { onSelectCountryZone(zone.countryCode) }
                            )
                        }
                    }
                }

                2 -> {
                    // Domain 2: Viewing & UX Preferences
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KickoffPreferenceToggleCard(
                            title = "Spoiler-Free Scoreboard",
                            subtitle = "Hide live and final goal scores on match cards until you open the Match Center dossier.",
                            checked = uiPreferences.hideLiveAndFinalScores,
                            onCheckedChange = { enabled ->
                                onUpdateUiPreferences { it.copy(hideLiveAndFinalScores = enabled) }
                            },
                            testTag = "pref_switch_spoiler_free"
                        )

                        KickoffPreferenceToggleCard(
                            title = "Compact Match Card Density",
                            subtitle = "Use tighter vertical spacing across match cards for high-volume weekend matchdays.",
                            checked = uiPreferences.compactCardDensity,
                            onCheckedChange = { enabled ->
                                onUpdateUiPreferences { it.copy(compactCardDensity = enabled) }
                            },
                            testTag = "pref_switch_compact_density"
                        )

                        KickoffPreferenceToggleCard(
                            title = "Smart Empty-League Auto-Fallback",
                            subtitle = "Automatically reset the league filter to All Matches if the selected competition has 0 fixtures on a newly chosen day.",
                            checked = uiPreferences.autoFallbackEmptyLeagueDay,
                            onCheckedChange = { enabled ->
                                onUpdateUiPreferences { it.copy(autoFallbackEmptyLeagueDay = enabled) }
                            },
                            testTag = "pref_switch_auto_fallback_day"
                        )
                    }
                }

                3 -> {
                    // Domain 3: Data Sources & API Key Management Hub
                    if (!sourceAlertMessage.isNullOrBlank()) {
                        KickoffSourceStatusAlertBanner(
                            alertMessage = sourceAlertMessage,
                            onManageSourcesClick = { activeTab = 3 },
                            onDismiss = onDismissSourceAlert,
                            modifier = Modifier.padding(horizontal = 0.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    KickoffSourcesAndKeysTabContent(
                        sourceStates = effectiveSourceStates,
                        isCheckingSourcesHealth = isCheckingSourcesHealth,
                        onCheckAllSourcesHealth = onCheckAllSourcesHealth,
                        onValidateAndUpdateApiKey = onValidateAndUpdateApiKey,
                        onResetApiKey = onResetApiKey
                    )
                }
            }
        }
    }

    if (activeEdition == SetPlayEdition.TV) {
        // =========================================================================
        // 10-FOOT ANDROID TV TWO-PANE WIDESCREEN SETTINGS DIALOG
        // =========================================================================
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                color = KickoffPitchBlack,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("tv_kickoff_settings_hub_dialog")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Left Pane (250.dp): Vertical 4-Domain D-Pad Rail
                    Column(
                        modifier = Modifier
                            .width(250.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .background(KickoffTurfSurface)
                            .border(1.dp, KickoffEmeraldBorder, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                KickoffSettingsHexGearIcon(
                                    modifier = Modifier.size(20.dp),
                                    tint = KickoffFloodlightVolt
                                )
                                Column {
                                    Text(
                                        text = "KICKOFF CONTROL",
                                        color = KickoffFloodlightWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "4-Domain Studio Hub",
                                        color = KickoffMistSilver,
                                        fontSize = 10.5.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            domainDescriptors.forEachIndexed { index, (title, subtitle, hasAlert) ->
                                val isSelected = activeTab == index
                                val borderColor = when {
                                    isSelected -> KickoffFloodlightVolt
                                    hasAlert -> KickoffTrophyGold
                                    else -> KickoffEmeraldBorder
                                }
                                Surface(
                                    color = if (isSelected) KickoffPitchEmerald else KickoffCanopyElevated,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(if (isSelected) 1.4.dp else 0.8.dp, borderColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .tvFocusableControl(
                                            accentColor = KickoffFloodlightVolt,
                                            shape = RoundedCornerShape(12.dp),
                                            onClick = { activeTab = index }
                                        )
                                        .testTag("customize_tab_$index")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = title,
                                                color = if (isSelected) KickoffPitchBlack else KickoffFloodlightWhite,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                maxLines = 1
                                            )
                                            if (hasAlert) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(KickoffLiveCrimson)
                                                )
                                            }
                                        }
                                        Text(
                                            text = subtitle,
                                            color = if (isSelected) KickoffPitchBlack.copy(alpha = 0.8f) else KickoffMistSilver,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        Surface(
                            color = KickoffCanopyElevated,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.8.dp, KickoffEmeraldBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .tvFocusableControl(
                                    accentColor = KickoffPitchEmerald,
                                    shape = RoundedCornerShape(10.dp),
                                    onClick = onDismiss
                                )
                        ) {
                            Text(
                                text = "Close Settings",
                                color = KickoffFloodlightWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }

                    // Right Pane (weight 1f): Active Domain Content
                    Surface(
                        color = KickoffTurfSurface.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, KickoffEmeraldBorder),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        renderActiveDomainContent(
                            Modifier
                                .fillMaxSize()
                                .padding(18.dp)
                        )
                    }
                }
            }
        }
    } else {
        // =========================================================================
        // MOBILE FULL-HEIGHT SHEET WITH 2x2 DOMAIN GRID
        // =========================================================================
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = KickoffPitchBlack
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KickoffSettingsHexGearIcon(
                            modifier = Modifier.size(19.dp),
                            tint = KickoffFloodlightVolt
                        )
                        Column {
                            Text(
                                text = "KickOff Settings Center",
                                color = KickoffFloodlightWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Teams & Leagues • Region & Clocks • Viewing UX • Sources",
                                color = KickoffMistSilver,
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = KickoffFloodlightWhite)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2x2 Non-Truncating Domain Grid (`customize_tab_0`..`customize_tab_3`)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (rowIdx in 0..1) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (colIdx in 0..1) {
                                val domainIdx = rowIdx * 2 + colIdx
                                val (title, subtitle, hasAlert) = domainDescriptors[domainIdx]
                                val isSelected = activeTab == domainIdx
                                Surface(
                                    color = if (isSelected) KickoffPitchEmerald else KickoffTurfSurface,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(
                                        width = if (isSelected) 1.2.dp else 0.8.dp,
                                        color = when {
                                            isSelected -> KickoffFloodlightVolt
                                            hasAlert -> KickoffTrophyGold
                                            else -> KickoffEmeraldBorder
                                        }
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { activeTab = domainIdx }
                                        .testTag("customize_tab_$domainIdx")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = title,
                                                color = if (isSelected) KickoffPitchBlack else KickoffFloodlightWhite,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                maxLines = 1
                                            )
                                            if (hasAlert) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(7.dp)
                                                        .clip(CircleShape)
                                                        .background(KickoffLiveCrimson)
                                                )
                                            }
                                        }
                                        Text(
                                            text = subtitle,
                                            color = if (isSelected) KickoffPitchBlack.copy(alpha = 0.8f) else KickoffMistSilver,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                renderActiveDomainContent(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }
}

@Composable
private fun KickoffPreferenceToggleCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Surface(
        color = KickoffTurfSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.8.dp, if (checked) KickoffPitchEmerald else KickoffEmeraldBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = KickoffFloodlightWhite,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = KickoffMistSilver,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = KickoffPitchBlack,
                    checkedTrackColor = KickoffPitchEmerald,
                    uncheckedThumbColor = KickoffMistSilver,
                    uncheckedTrackColor = KickoffGlassWell
                ),
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}

@Composable
private fun KickoffSourcesAndKeysTabContent(
    sourceStates: List<KickoffSourceHealthState>,
    isCheckingSourcesHealth: Boolean,
    onCheckAllSourcesHealth: () -> Unit,
    onValidateAndUpdateApiKey: (KickoffProviderId, String, (ApiKeyValidationResult) -> Unit) -> Unit,
    onResetApiKey: (KickoffProviderId) -> Unit
) {
    val healthyCount = remember(sourceStates) {
        sourceStates.count { it.status == SourceHealthStatus.HEALTHY }
    }
    val warningOrInvalidCount = remember(sourceStates) {
        sourceStates.count {
            it.status == SourceHealthStatus.KEY_INVALID_OR_EXPIRED ||
                it.status == SourceHealthStatus.QUOTA_WARNING ||
                it.status == SourceHealthStatus.UNREACHABLE
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("kickoff_sources_and_keys_tab_content"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Summary Health Bar + "Verify All Sources" Button
        Surface(
            color = KickoffStadiumForest,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, KickoffEmeraldBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$healthyCount / ${sourceStates.size} DATA SOURCES ACTIVE",
                        color = KickoffFloodlightWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (warningOrInvalidCount == 0) {
                            "All configured feeds & multi-provider fallbacks are healthy"
                        } else {
                            "$warningOrInvalidCount source(s) need attention • Automatic fallback is active"
                        },
                        color = KickoffMistSilver,
                        fontSize = 10.5.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = KickoffPitchEmerald,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.8.dp, KickoffFloodlightVolt.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = !isCheckingSourcesHealth) { onCheckAllSourcesHealth() }
                        .testTag("verify_all_sources_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        if (isCheckingSourcesHealth) {
                            CircularProgressIndicator(
                                color = KickoffPitchBlack,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = if (isCheckingSourcesHealth) "Checking..." else "Verify All Sources",
                            color = KickoffPitchBlack,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // 6 Provider Cards List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 370.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sourceStates, key = { it.provider.id }) { state ->
                KickoffProviderHealthCard(
                    state = state,
                    onValidateAndUpdateApiKey = onValidateAndUpdateApiKey,
                    onResetApiKey = onResetApiKey
                )
            }
        }
    }
}

@Composable
private fun KickoffProviderHealthCard(
    state: KickoffSourceHealthState,
    onValidateAndUpdateApiKey: (KickoffProviderId, String, (ApiKeyValidationResult) -> Unit) -> Unit,
    onResetApiKey: (KickoffProviderId) -> Unit
) {
    var keyInput by remember(state.provider.id) { mutableStateOf("") }
    var isValidatingKey by remember(state.provider.id) { mutableStateOf(false) }
    var validationFeedback by remember(state.provider.id) { mutableStateOf<String?>(null) }
    var isEditorExpanded by remember(state.provider.id) {
        mutableStateOf(
            state.status == SourceHealthStatus.KEY_INVALID_OR_EXPIRED ||
                state.status == SourceHealthStatus.QUOTA_WARNING
        )
    }

    val badgeColor = when (state.status) {
        SourceHealthStatus.HEALTHY -> KickoffStadiumForest
        SourceHealthStatus.QUOTA_WARNING -> KickoffTrophyGold.copy(alpha = 0.25f)
        SourceHealthStatus.KEY_INVALID_OR_EXPIRED,
        SourceHealthStatus.UNREACHABLE -> KickoffLiveCrimson.copy(alpha = 0.25f)
        SourceHealthStatus.UNCONFIGURED -> KickoffGlassWell
    }

    val badgeBorderColor = when (state.status) {
        SourceHealthStatus.HEALTHY -> KickoffPitchEmerald
        SourceHealthStatus.QUOTA_WARNING -> KickoffTrophyGold
        SourceHealthStatus.KEY_INVALID_OR_EXPIRED,
        SourceHealthStatus.UNREACHABLE -> KickoffLiveCrimson
        SourceHealthStatus.UNCONFIGURED -> KickoffEmeraldBorder
    }

    Surface(
        color = KickoffTurfSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (state.status == SourceHealthStatus.KEY_INVALID_OR_EXPIRED || state.status == SourceHealthStatus.QUOTA_WARNING) 1.1.dp else 0.8.dp,
            color = if (state.status == SourceHealthStatus.KEY_INVALID_OR_EXPIRED) {
                KickoffLiveCrimson
            } else if (state.status == SourceHealthStatus.QUOTA_WARNING) {
                KickoffTrophyGold
            } else {
                KickoffEmeraldBorder
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("source_card_${state.provider.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = state.provider.displayName,
                            color = KickoffFloodlightWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${state.provider.endpointSummary}",
                            color = KickoffMistSilver,
                            fontSize = 9.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = state.provider.dependentFeatures,
                        color = KickoffMistSilver,
                        fontSize = 10.5.sp
                    )
                }

                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(0.7.dp, badgeBorderColor),
                    modifier = Modifier.testTag("source_status_badge_${state.provider.id}")
                ) {
                    Text(
                        text = state.status.label,
                        color = KickoffFloodlightWhite,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            // Quota + Key Mask Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quota: ${state.remainingQuotaLabel}",
                    color = KickoffFloodlightWhite,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    style = TabularNumeralTextStyle
                )
                Text(
                    text = if (state.isUsingCustomKey) {
                        "Key: ${state.maskedKeyPreview} (Custom)"
                    } else {
                        "Key: ${state.maskedKeyPreview}"
                    },
                    color = KickoffMistSilver,
                    fontSize = 10.sp
                )
            }

            Text(
                text = state.statusDetail,
                color = KickoffMistSilver,
                fontSize = 10.sp
            )

            if (state.provider.requiresApiKey) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = KickoffGlassWell,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.7.dp, KickoffPitchEmerald),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isEditorExpanded = !isEditorExpanded }
                            .testTag("toggle_key_editor_btn_${state.provider.id}")
                    ) {
                        Text(
                            text = if (isEditorExpanded) "Hide Key Editor" else "Update / Validate API Key",
                            color = KickoffFloodlightWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        )
                    }

                    if (state.isUsingCustomKey) {
                        Surface(
                            color = KickoffGlassWell,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.7.dp, KickoffEmeraldBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onResetApiKey(state.provider)
                                    validationFeedback = "Restored default BuildConfig key."
                                }
                                .testTag("reset_key_btn_${state.provider.id}")
                        ) {
                            Text(
                                text = "Reset Default",
                                color = KickoffMistSilver,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                if (isEditorExpanded) {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        placeholder = {
                            Text(
                                text = "Paste replacement ${state.provider.displayName} key...",
                                color = KickoffMistSilver.copy(alpha = 0.65f),
                                fontSize = 11.sp
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KickoffPitchEmerald,
                            unfocusedBorderColor = KickoffEmeraldBorder,
                            focusedContainerColor = KickoffGlassWell,
                            unfocusedContainerColor = KickoffGlassWell,
                            focusedTextColor = KickoffFloodlightWhite,
                            unfocusedTextColor = KickoffFloodlightWhite
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("source_api_key_input_${state.provider.id}")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (keyInput.isBlank()) {
                                    validationFeedback = "Please enter an API key to validate."
                                } else {
                                    isValidatingKey = true
                                    validationFeedback = "Validating with ${state.provider.endpointSummary}..."
                                    onValidateAndUpdateApiKey(state.provider, keyInput) { result ->
                                        isValidatingKey = false
                                        validationFeedback = result.message
                                        if (result.isValid) {
                                            keyInput = ""
                                        }
                                    }
                                }
                            },
                            enabled = !isValidatingKey,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KickoffPitchEmerald,
                                contentColor = KickoffPitchBlack
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("validate_save_key_btn_${state.provider.id}")
                        ) {
                            Text(
                                text = if (isValidatingKey) "Validating..." else "Validate & Save Key",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                onResetApiKey(state.provider)
                                keyInput = ""
                                validationFeedback = "Restored default BuildConfig key."
                            },
                            border = BorderStroke(0.8.dp, KickoffEmeraldBorder),
                            modifier = Modifier.testTag("inline_reset_key_btn_${state.provider.id}")
                        ) {
                            Text(
                                text = "Reset Default",
                                color = KickoffMistSilver,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    validationFeedback?.let { feedback ->
                        Text(
                            text = feedback,
                            color = KickoffFloodlightWhite,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.testTag("source_validation_feedback_${state.provider.id}")
                        )
                    }
                }
            }
        }
    }
}

