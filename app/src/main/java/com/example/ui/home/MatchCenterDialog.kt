package com.example.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil.compose.SubcomposeAsyncImage
import com.example.data.repository.FootballMatch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.repository.H2HMeetingItem
import com.example.data.repository.LeagueStandingRow
import com.example.data.repository.MatchCenterTab
import com.example.data.repository.MatchEventType
import com.example.data.repository.MatchFullDossier
import com.example.data.repository.MatchLineupsData
import com.example.data.repository.MatchStatComparison
import com.example.data.repository.MatchStatus
import com.example.data.repository.MatchTeamSide
import com.example.data.repository.MatchTimelineEvent
import com.example.data.repository.MenaBroadcastChannelInfo
import com.example.data.repository.PitchPlayerNode
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ArenaFloodlightLime
import com.example.ui.theme.ArenaPitchGreen
import com.example.ui.theme.ArenaTrophyGold
import com.example.ui.theme.ArenaTurfDark
import com.example.ui.theme.ArenaTurfElevated
import com.example.ui.theme.Champagne
import com.example.ui.theme.ChampagneMuted
import com.example.ui.theme.ChampagneSubtleBorder
import com.example.ui.theme.DarkGraphite
import com.example.ui.theme.KickoffCeruleanBlue
import com.example.ui.theme.KickoffCoralRed
import com.example.ui.theme.KickoffOceanBase
import com.example.ui.theme.KickoffOceanElevated
import com.example.ui.theme.KickoffOceanSurface
import com.example.ui.theme.KickoffPeachCream
import com.example.ui.theme.KickoffTropicalTeal
import com.example.ui.theme.LiveCrimson
import com.example.ui.theme.MetalCharcoal
import com.example.ui.theme.MetalCharcoalElevated
import com.example.ui.theme.PaleGold
import java.util.Locale

/**
 * Full 5-Tab Flashscore & 365Scores Match Center Modal (Dossier 2.0):
 * 1. Summary (1st Half / HT / 2nd Half / FT Timeline + Match Officials & Venue)
 * 2. Lineups (Interactive 2D Tactical Pitch with Formations, Ratings, Coaches, Bench & Missing Players)
 * 3. Stats (Possession, Expected Goals xG, Shots, Big Chances, Corners, Pass Accuracy...)
 * 4. H2H / Table (Last 5 Form Guide, Head-to-Head History, Live Competition Standings)
 * 5. TV Guide (Official MENA-only Broadcasters with Commentator & Satellite Info + 1-Tap Favorites Watch)
 */
@Composable
fun MatchCenterFullDialog(
    dossier: MatchFullDossier,
    onDismiss: () -> Unit,
    onWatchOnMenaChannel: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(MatchCenterTab.SUMMARY) }
    val match = dossier.match
    val isLive = match.status == MatchStatus.LIVE || match.status == MatchStatus.HALFTIME

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = KickoffOceanBase,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(
                width = 1.3.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        if (isLive) LiveCrimson else KickoffTropicalTeal,
                        KickoffPeachCream.copy(alpha = 0.55f),
                        KickoffCoralRed
                    )
                )
            ),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .testTag("match_center_full_dialog")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 1. Stadium Scoreboard Header (Dual-Club Ambient Glow + Recessed Digital Well + MENA Launch Bar)
                MatchCenterScoreboardHeader(
                    dossier = dossier,
                    isLive = isLive,
                    onDismiss = onDismiss,
                    onWatchPrimaryChannel = { onWatchOnMenaChannel(match.beinChannel) }
                )

                // 2. 5-Tab Luxury Capsule Navigation Bar
                MatchCenterTabBar(
                    selectedTab = selectedTab,
                    onSelectTab = { selectedTab = it }
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)

                // 3. Tab Content Body
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    KickoffOceanSurface.copy(alpha = 0.65f),
                                    KickoffOceanBase
                                )
                            )
                        )
                ) {
                    when (selectedTab) {
                        MatchCenterTab.SUMMARY -> MatchSummaryTabContent(
                            dossier = dossier,
                            onWatchChannel = onWatchOnMenaChannel
                        )
                        MatchCenterTab.LINEUPS -> MatchLineups2DPitchTabContent(
                            dossier = dossier
                        )
                        MatchCenterTab.STATS -> MatchStatsTabContent(
                            dossier = dossier
                        )
                        MatchCenterTab.H2H_TABLE -> MatchH2HAndStandingsTabContent(
                            dossier = dossier
                        )
                        MatchCenterTab.TV_GUIDE -> MatchMenaTvGuideTabContent(
                            dossier = dossier,
                            onWatchChannel = onWatchOnMenaChannel
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchCenterScoreboardHeader(
    dossier: MatchFullDossier,
    isLive: Boolean,
    onDismiss: () -> Unit,
    onWatchPrimaryChannel: () -> Unit
) {
    val match = dossier.match
    val isUltraHdChannel = remember(match.beinChannel) {
        val upper = match.beinChannel.uppercase()
        upper.contains("4K") || upper.contains("MAX") || upper.contains("PREMIUM") || upper.contains("1 ")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(KickoffOceanElevated, KickoffOceanSurface, KickoffOceanBase)
                )
            )
    ) {
        // Dual-Club Ambient Stadium Spotlights behind Home & Away Crests
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        KickoffTropicalTeal.copy(alpha = 0.22f),
                        KickoffCeruleanBlue.copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.16f, size.height * 0.44f),
                    radius = size.width * 0.40f
                ),
                radius = size.width * 0.40f,
                center = Offset(size.width * 0.16f, size.height * 0.44f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        KickoffCoralRed.copy(alpha = 0.20f),
                        AmberGold.copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.84f, size.height * 0.44f),
                    radius = size.width * 0.40f
                ),
                radius = size.width * 0.40f,
                center = Offset(size.width * 0.84f, size.height * 0.44f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Competition & Stage Bar + Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(AmberGold.copy(alpha = 0.16f))
                            .border(0.7.dp, AmberGold.copy(alpha = 0.50f), RoundedCornerShape(7.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = AmberGold,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = "${match.tournamentRegion.uppercase()} • ${match.tournamentName.uppercase()}",
                        color = KickoffPeachCream,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${match.stageOrRound})",
                        color = ChampagneMuted,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.07f))
                        .testTag("close_match_center_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Match Center",
                        tint = KickoffPeachCream,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Scoreboard Row: Home Crest/Name | Recessed Digital Stadium Scoreboard Well | Away Crest/Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Home Team Column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(KickoffTropicalTeal.copy(alpha = 0.14f))
                            .border(0.9.dp, KickoffTropicalTeal.copy(alpha = 0.45f), CircleShape)
                    ) {
                        TeamCrestImage(
                            badgeUrl = match.homeTeamBadge,
                            teamName = match.homeTeamName,
                            sizeDp = 42
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = match.homeTeamName,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (match.homeRedCards > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            RedCardIndicatorBadge(count = match.homeRedCards)
                        }
                    }
                    Text(
                        text = dossier.lineups.homeFormation,
                        color = KickoffTropicalTeal,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Center Recessed Digital Scoreboard Well
                Surface(
                    color = Color(0xFF051118),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isLive) LiveCrimson.copy(alpha = 0.70f) else KickoffTropicalTeal.copy(alpha = 0.40f)
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        // Status / Minute Pill
                        Surface(
                            color = when {
                                isLive -> LiveCrimson.copy(alpha = 0.22f)
                                match.status == MatchStatus.FINISHED -> KickoffOceanElevated
                                else -> KickoffTropicalTeal.copy(alpha = 0.16f)
                            },
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(
                                width = 0.8.dp,
                                color = when {
                                    isLive -> LiveCrimson
                                    match.status == MatchStatus.FINISHED -> Color.White.copy(alpha = 0.15f)
                                    else -> KickoffTropicalTeal.copy(alpha = 0.65f)
                                }
                            )
                        ) {
                            Text(
                                text = when (match.status) {
                                    MatchStatus.LIVE -> "LIVE • ${match.liveMinuteOrBadge}"
                                    MatchStatus.HALFTIME -> "HALF TIME"
                                    MatchStatus.FINISHED -> "FULL TIME"
                                    MatchStatus.UPCOMING -> "${match.kickoffDateFormatted} • ${match.kickoffTimeFormatted}"
                                },
                                color = when {
                                    isLive -> LiveCrimson
                                    match.status == MatchStatus.FINISHED -> Champagne
                                    else -> KickoffTropicalTeal
                                },
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 2.5.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(5.dp))

                        // Big Score or Kickoff Time
                        Text(
                            text = if (match.homeScore != null && match.awayScore != null) {
                                "${match.homeScore}  :  ${match.awayScore}"
                            } else {
                                match.kickoffTimeFormatted
                            },
                            color = if (isLive) Color.White else KickoffPeachCream,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp
                        )

                        // HT / FT Score Breakdown Pill
                        Spacer(modifier = Modifier.height(4.dp))
                        val breakdownText = buildString {
                            append("(${dossier.halfTimeScoreLabel})")
                            dossier.fullTimeScoreLabel?.let { ft ->
                                append(" • $ft")
                            }
                        }
                        Text(
                            text = breakdownText,
                            color = AmberGold,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Away Team Column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(KickoffCoralRed.copy(alpha = 0.14f))
                            .border(0.9.dp, KickoffCoralRed.copy(alpha = 0.45f), CircleShape)
                    ) {
                        TeamCrestImage(
                            badgeUrl = match.awayTeamBadge,
                            teamName = match.awayTeamName,
                            sizeDp = 42
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = match.awayTeamName,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (match.awayRedCards > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            RedCardIndicatorBadge(count = match.awayRedCards)
                        }
                    }
                    Text(
                        text = dossier.lineups.awayFormation,
                        color = KickoffCoralRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dual-Stage MENA TV Broadcast Bar + Illuminated Watch Capsule
            Surface(
                color = Color(0xFF06131C),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            KickoffTropicalTeal.copy(alpha = 0.55f),
                            KickoffCoralRed.copy(alpha = 0.40f)
                        )
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LiveTv,
                            contentDescription = null,
                            tint = KickoffTropicalTeal,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = match.beinChannel,
                                    color = KickoffPeachCream,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color.Black.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(0.6.dp, AmberGold.copy(alpha = 0.7f))
                                ) {
                                    Text(
                                        text = if (isUltraHdChannel) "4K UHD" else "FHD",
                                        color = AmberGold,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 4.5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${match.stadiumName}, ${match.cityName} • ${dossier.refereeName}",
                                color = ChampagneMuted,
                                fontSize = 9.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Illuminated Floodlight Watch Pill with Dark Play Medallion
                    Surface(
                        color = Color.Transparent,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.9.dp, Color.White.copy(alpha = 0.40f)),
                        modifier = Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF46DFB1),
                                        KickoffTropicalTeal,
                                        KickoffCeruleanBlue
                                    )
                                )
                            )
                            .clickable { onWatchPrimaryChannel() }
                            .testTag("match_center_header_watch_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 5.dp, end = 12.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(DarkGraphite)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Watch Match",
                                    tint = KickoffTropicalTeal,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isLive) "WATCH LIVE" else "WATCH",
                                color = DarkGraphite,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RedCardIndicatorBadge(count: Int) {
    Surface(
        color = Color(0xFFDC2626),
        shape = RoundedCornerShape(3.dp)
    ) {
        Text(
            text = if (count > 1) "$count" else "RC",
            color = Color.White,
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
        )
    }
}

@Composable
private fun MatchCenterTabBar(
    selectedTab: MatchCenterTab,
    onSelectTab: (MatchCenterTab) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(KickoffOceanSurface)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(MatchCenterTab.entries) { tab ->
            val isSelected = tab == selectedTab
            Surface(
                color = if (isSelected) Color.Transparent else KickoffOceanElevated.copy(alpha = 0.78f),
                shape = RoundedCornerShape(50),
                border = BorderStroke(
                    width = if (isSelected) 1.dp else 0.8.dp,
                    color = if (isSelected) KickoffPeachCream.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.10f)
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .then(
                        if (isSelected) {
                            Modifier.background(
                                Brush.horizontalGradient(
                                    listOf(
                                        KickoffTropicalTeal,
                                        KickoffCeruleanBlue
                                    )
                                )
                            )
                        } else {
                            Modifier
                        }
                    )
                    .clickable { onSelectTab(tab) }
                    .testTag("match_center_tab_${tab.name.lowercase(Locale.ROOT)}")
            ) {
                Text(
                    text = tab.title,
                    color = if (isSelected) DarkGraphite else KickoffPeachCream,
                    fontSize = 11.5.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}

// =============================================================================
// TAB 1: SUMMARY (FLASHSCORE CHRONOLOGICAL TIMELINE + HT/FT BREAKDOWN + VENUE)
// =============================================================================
@Composable
private fun MatchSummaryTabContent(
    dossier: MatchFullDossier,
    onWatchChannel: (String) -> Unit
) {
    val match = dossier.match
    val firstHalfEvents = remember(dossier.timelineEvents) {
        dossier.timelineEvents.filter { it.halfLabel == "1ST HALF" }
    }
    val secondHalfEvents = remember(dossier.timelineEvents) {
        dossier.timelineEvents.filter { it.halfLabel != "1ST HALF" }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (dossier.timelineEvents.isEmpty()) {
            // Upcoming Match Pre-Game Summary Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MetalCharcoal),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ArenaPitchGreen.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = ArenaFloodlightLime,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PRE-MATCH KICKOFF BRIEFING",
                                color = PaleGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Kickoff is scheduled at ${match.kickoffTimeFormatted} (Morocco GMT+1). Probable starting lineups (${dossier.lineups.homeFormation} vs ${dossier.lineups.awayFormation}), H2H history, and MENA broadcast channels are available across the tabs above.",
                            color = Champagne,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            // 1st Half Section Header
            item {
                TimelineHalfBanner(
                    title = "1ST HALF",
                    scoreBadge = dossier.halfTimeScoreLabel
                )
            }
            items(firstHalfEvents, key = { it.id }) { ev ->
                FlashscoreTimelineEventRow(event = ev)
            }

            if (secondHalfEvents.isNotEmpty() || match.status == MatchStatus.FINISHED) {
                item {
                    TimelineHalfBanner(
                        title = "2ND HALF",
                        scoreBadge = dossier.fullTimeScoreLabel ?: "LIVE ${match.homeScore ?: 0} - ${match.awayScore ?: 0}"
                    )
                }
                items(secondHalfEvents, key = { it.id }) { ev ->
                    FlashscoreTimelineEventRow(event = ev)
                }
            }
        }

        // Quick MENA Broadcast Strip inside Summary
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ArenaTurfDark),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.55f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "OFFICIAL MENA BROADCASTERS (TAP TO WATCH VIA FAVORITES)",
                        color = AmberGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(dossier.menaBroadcastGuide, key = { it.channelName }) { item ->
                            Surface(
                                color = ArenaTurfElevated,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.9.dp, ArenaPitchGreen),
                                modifier = Modifier.clickable { onWatchChannel(item.channelName) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LiveTv,
                                        contentDescription = null,
                                        tint = ArenaFloodlightLime,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = item.channelName,
                                        color = PaleGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Match Details Footer (Venue, Referee, Attendance, Kickoff Time)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MetalCharcoal),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(0.8.dp, ChampagneSubtleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "MATCH INFORMATION",
                        color = ArenaFloodlightLime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    MatchInfoKeyValueRow("Stadium / Venue", "${match.stadiumName}, ${match.cityName}")
                    MatchInfoKeyValueRow("Referee", dossier.refereeName)
                    MatchInfoKeyValueRow("Attendance", dossier.attendanceLabel)
                    MatchInfoKeyValueRow("Pitch & Weather", dossier.pitchConditionLabel)
                    MatchInfoKeyValueRow("Kickoff (Morocco Time)", "${match.kickoffTimeFormatted} (${match.kickoffDateFormatted})")
                }
            }
        }
    }
}

@Composable
private fun TimelineHalfBanner(
    title: String,
    scoreBadge: String
) {
    Surface(
        color = ArenaTurfElevated,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.8.dp, ArenaPitchGreen.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = ArenaFloodlightLime,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = scoreBadge,
                color = PaleGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun FlashscoreTimelineEventRow(
    event: MatchTimelineEvent
) {
    val isHome = event.teamSide == MatchTeamSide.HOME
    Surface(
        color = MetalCharcoal,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.6.dp, ChampagneSubtleBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isHome) Arrangement.Start else Arrangement.End
        ) {
            if (isHome) {
                TimelineMinutePill(event.minuteLabel)
                Spacer(modifier = Modifier.width(8.dp))
                TimelineEventBadge(event.eventType)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = event.primaryPlayer,
                            color = PaleGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (event.runningScore.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = ArenaPitchGreen.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = event.runningScore,
                                    color = ArenaFloodlightLime,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    if (event.secondaryPlayer.isNotBlank() || event.detailNote.isNotBlank()) {
                        Text(
                            text = listOf(event.secondaryPlayer, event.detailNote).filter { it.isNotBlank() }.joinToString(" • "),
                            color = ChampagneMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (event.runningScore.isNotBlank()) {
                            Surface(
                                color = AmberGold.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = event.runningScore,
                                    color = AmberGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = event.primaryPlayer,
                            color = PaleGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    if (event.secondaryPlayer.isNotBlank() || event.detailNote.isNotBlank()) {
                        Text(
                            text = listOf(event.secondaryPlayer, event.detailNote).filter { it.isNotBlank() }.joinToString(" • "),
                            color = ChampagneMuted,
                            fontSize = 10.sp,
                            textAlign = TextAlign.End
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                TimelineEventBadge(event.eventType)
                Spacer(modifier = Modifier.width(8.dp))
                TimelineMinutePill(event.minuteLabel)
            }
        }
    }
}

@Composable
private fun TimelineMinutePill(minuteLabel: String) {
    Surface(
        color = DarkGraphite,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(0.6.dp, ChampagneSubtleBorder)
    ) {
        Text(
            text = minuteLabel,
            color = AmberGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun TimelineEventBadge(type: MatchEventType) {
    when (type) {
        MatchEventType.GOAL, MatchEventType.PENALTY_GOAL, MatchEventType.OWN_GOAL -> {
            Surface(
                color = ArenaPitchGreen.copy(alpha = 0.25f),
                shape = CircleShape,
                border = BorderStroke(1.dp, ArenaFloodlightLime),
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.SportsSoccer,
                        contentDescription = "Goal",
                        tint = ArenaFloodlightLime,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        MatchEventType.YELLOW_CARD -> {
            Box(
                modifier = Modifier
                    .size(width = 13.dp, height = 18.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFFACC15))
                    .border(0.6.dp, DarkGraphite, RoundedCornerShape(3.dp))
            )
        }
        MatchEventType.RED_CARD -> {
            Box(
                modifier = Modifier
                    .size(width = 13.dp, height = 18.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFEF4444))
                    .border(0.6.dp, DarkGraphite, RoundedCornerShape(3.dp))
            )
        }
        MatchEventType.SUBSTITUTION -> {
            Surface(
                color = MetalCharcoalElevated,
                shape = CircleShape,
                border = BorderStroke(0.8.dp, AmberGold),
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Substitution",
                        tint = AmberGold,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        MatchEventType.VAR_DECISION -> {
            Surface(
                color = LiveCrimson.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(0.8.dp, LiveCrimson)
            ) {
                Text(
                    text = "VAR",
                    color = LiveCrimson,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun MatchInfoKeyValueRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = ChampagneMuted, fontSize = 11.sp)
        Text(
            text = value,
            color = PaleGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End
        )
    }
}

// =============================================================================
// TAB 2: LINEUPS (365SCORES 2D TACTICAL PITCH + RATINGS + BENCH + INJURIES)
// =============================================================================
private enum class PitchTeamFilterMode(val label: String) {
    BOTH("BOTH TEAMS (22)"),
    HOME_ONLY("HOME XI"),
    AWAY_ONLY("AWAY XI")
}

@Composable
private fun MatchLineups2DPitchTabContent(
    dossier: MatchFullDossier
) {
    val lineups = dossier.lineups
    val match = dossier.match
    val defaultMvp = remember(lineups) {
        (lineups.homeStartingXI + lineups.awayStartingXI).firstOrNull { it.isMvp }
            ?: (lineups.homeStartingXI + lineups.awayStartingXI).maxByOrNull { it.rating }
            ?: lineups.homeStartingXI.lastOrNull()
    }
    var inspectedPlayer by remember(lineups) { mutableStateOf<PitchPlayerNode?>(defaultMvp) }
    var pitchFilterMode by remember { mutableStateOf(PitchTeamFilterMode.BOTH) }
    var showPassingNetwork by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Confirmed / Probable XI & Interactive Pitch View Controls
        item {
            Surface(
                color = ArenaTurfElevated,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(0.9.dp, ArenaPitchGreen.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = if (lineups.isConfirmed) Color(0xFF2ECC71).copy(alpha = 0.16f) else AmberGold.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(
                                    0.7.dp,
                                    if (lineups.isConfirmed) Color(0xFF2ECC71) else AmberGold
                                )
                            ) {
                                Text(
                                    text = if (lineups.isConfirmed) "✓ CONFIRMED XI" else "PROBABLE XI",
                                    color = if (lineups.isConfirmed) Color(0xFF2ECC71) else AmberGold,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tap any player for live dossier",
                                color = ChampagneMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Tactical Passing Links toggle pill
                        Surface(
                            color = if (showPassingNetwork) ArenaPitchGreen.copy(alpha = 0.18f) else DarkGraphite,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(
                                0.7.dp,
                                if (showPassingNetwork) ArenaFloodlightLime else ArenaPitchGreen.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { showPassingNetwork = !showPassingNetwork }
                                .testTag("toggle_passing_links_btn")
                        ) {
                            Text(
                                text = if (showPassingNetwork) "TACTICAL LINES: ON" else "TACTICAL LINES: OFF",
                                color = if (showPassingNetwork) ArenaFloodlightLime else ChampagneMuted,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Interactive Team Pitch Filter Mode: BOTH TEAMS | HOME XI | AWAY XI
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PitchTeamFilterMode.entries.forEach { mode ->
                            val isSelected = pitchFilterMode == mode
                            val labelText = when (mode) {
                                PitchTeamFilterMode.BOTH -> "BOTH TEAMS"
                                PitchTeamFilterMode.HOME_ONLY -> "${match.homeTeamName.take(12)} (${lineups.homeFormation})"
                                PitchTeamFilterMode.AWAY_ONLY -> "${match.awayTeamName.take(12)} (${lineups.awayFormation})"
                            }
                            val accentColor = when (mode) {
                                PitchTeamFilterMode.BOTH -> KickoffPeachCream
                                PitchTeamFilterMode.HOME_ONLY -> ArenaFloodlightLime
                                PitchTeamFilterMode.AWAY_ONLY -> AmberGold
                            }
                            Surface(
                                color = if (isSelected) accentColor.copy(alpha = 0.18f) else DarkGraphite.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(9.dp),
                                border = BorderStroke(
                                    width = if (isSelected) 1.dp else 0.6.dp,
                                    color = if (isSelected) accentColor else Color.White.copy(alpha = 0.10f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(9.dp))
                                    .clickable { pitchFilterMode = mode }
                                    .testTag("pitch_mode_${mode.name.lowercase(Locale.ROOT)}")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = labelText,
                                        color = if (isSelected) accentColor else ChampagneMuted,
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

        // 2. Star of the Match (MVP) & Top Performer Dual Spotlight Banner
        item {
            StarOfTheMatchComparisonStrip(
                match = match,
                lineups = lineups,
                onSelectPlayer = { inspectedPlayer = it }
            )
        }

        // 3. Interactive 2D Tactical Pitch with Real Player Photos, Team End-Zones & Zero Overlap
        item {
            TacticalPitch2DView(
                match = match,
                lineups = lineups,
                filterMode = pitchFilterMode,
                showPassingNetwork = showPassingNetwork,
                selectedPlayer = inspectedPlayer,
                onSelectPlayer = { inspectedPlayer = it }
            )
        }

        // 4. Tapped Player Tactical Dossier Spotlight Card (with Player Photo & Role Metrics)
        inspectedPlayer?.let { player ->
            item {
                val isHomePlayer = lineups.homeStartingXI.any { it.playerId == player.playerId } ||
                    lineups.homeBench.any { it.playerId == player.playerId }
                PlayerTacticalSpotlightCard(
                    player = player,
                    teamName = if (isHomePlayer) match.homeTeamName else match.awayTeamName,
                    teamBadge = if (isHomePlayer) match.homeTeamBadge else match.awayTeamBadge,
                    isHomeTeam = isHomePlayer
                )
            }
        }

        // 5. Coaches & Tactical Managers Card
        item {
            Surface(
                color = ArenaTurfElevated,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.8.dp, ArenaPitchGreen.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "HOME MANAGER",
                            color = ChampagneMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = lineups.homeCoach,
                            color = PaleGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = AmberGold,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(20.dp)
                    )
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "AWAY MANAGER",
                            color = ChampagneMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = lineups.awayCoach,
                            color = PaleGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 6. Substitutes Bench (Two-Column Split with Player Headshots & Ratings)
        item {
            Surface(
                color = ArenaTurfElevated,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(0.8.dp, ArenaPitchGreen.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SUBSTITUTES BENCH",
                            color = AmberGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Tap substitute to inspect",
                            color = ChampagneMuted,
                            fontSize = 9.5.sp
                        )
                    }
                    HorizontalDivider(color = ArenaPitchGreen.copy(alpha = 0.25f))

                    val maxBenchRows = maxOf(lineups.homeBench.size, lineups.awayBench.size)
                    for (idx in 0 until maxBenchRows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val homeSub = lineups.homeBench.getOrNull(idx)
                            val awaySub = lineups.awayBench.getOrNull(idx)

                            Box(modifier = Modifier.weight(1f)) {
                                if (homeSub != null) {
                                    BenchPlayerCell(
                                        player = homeSub,
                                        isHome = true,
                                        isSelected = inspectedPlayer?.playerId == homeSub.playerId,
                                        onClick = { inspectedPlayer = homeSub }
                                    )
                                }
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                if (awaySub != null) {
                                    BenchPlayerCell(
                                        player = awaySub,
                                        isHome = false,
                                        isSelected = inspectedPlayer?.playerId == awaySub.playerId,
                                        onClick = { inspectedPlayer = awaySub }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Missing / Injured / Suspended Players
        if (lineups.missingPlayers.isNotEmpty()) {
            item {
                Surface(
                    color = ArenaTurfElevated,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(0.8.dp, LiveCrimson.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = LiveCrimson,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MISSING & SUSPENDED PLAYERS",
                                color = LiveCrimson,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp
                            )
                        }
                        lineups.missingPlayers.forEach { missing ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${missing.playerName} (${missing.positionCode})",
                                        color = PaleGold,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${if (missing.teamSide == MatchTeamSide.HOME) match.homeTeamName else match.awayTeamName} • ${missing.reason}",
                                        color = ChampagneMuted,
                                        fontSize = 10.sp
                                    )
                                }
                                Surface(
                                    color = LiveCrimson.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(0.6.dp, LiveCrimson)
                                ) {
                                    Text(
                                        text = missing.statusBadge,
                                        color = LiveCrimson,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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

@Composable
private fun StarOfTheMatchComparisonStrip(
    match: FootballMatch,
    lineups: MatchLineupsData,
    onSelectPlayer: (PitchPlayerNode) -> Unit
) {
    val homeStar = remember(lineups.homeStartingXI) {
        lineups.homeStartingXI.maxByOrNull { it.rating + it.goals * 0.2 }
    }
    val awayStar = remember(lineups.awayStartingXI) {
        lineups.awayStartingXI.maxByOrNull { it.rating + it.goals * 0.2 }
    }
    if (homeStar == null || awayStar == null) return

    Surface(
        color = Color(0xFF0A1D29),
        shape = RoundedCornerShape(13.dp),
        border = BorderStroke(0.9.dp, AmberGold.copy(alpha = 0.42f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = AmberGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "TOP PERFORMERS & MATCH MVP",
                        color = AmberGold,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.7.sp
                    )
                }
                Text(
                    text = "AVG ${String.format(Locale.US, "%.1f", lineups.homeAvgRating)} vs ${String.format(Locale.US, "%.1f", lineups.awayAvgRating)}",
                    color = KickoffPeachCream,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TopPerformerMiniPill(
                    player = homeStar,
                    teamName = match.homeTeamName,
                    isHome = true,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectPlayer(homeStar) }
                )
                TopPerformerMiniPill(
                    player = awayStar,
                    teamName = match.awayTeamName,
                    isHome = false,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectPlayer(awayStar) }
                )
            }
        }
    }
}

@Composable
private fun TopPerformerMiniPill(
    player: PitchPlayerNode,
    teamName: String,
    isHome: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val accent = if (isHome) ArenaFloodlightLime else AmberGold
    Surface(
        color = DarkGraphite.copy(alpha = 0.85f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            width = if (player.isMvp) 1.1.dp else 0.7.dp,
            color = if (player.isMvp) AmberGold else accent.copy(alpha = 0.35f)
        ),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                PlayerCircularAvatarMedallion(
                    photoUrl = player.photoUrl,
                    playerName = player.name,
                    jerseyNumber = player.jerseyNumber,
                    primaryColor = if (isHome) Color(0xFF00B4D8) else Color(0xFFE0AC3E),
                    ringColor = if (player.isMvp) AmberGold else accent,
                    sizeDp = 30
                )
                Spacer(modifier = Modifier.width(7.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (player.isMvp) {
                            Text(
                                text = "★ MVP ",
                                color = AmberGold,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            text = player.shortName,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "#${player.jerseyNumber} • ${player.positionCode} • ${teamName.take(10)}",
                        color = ChampagneMuted,
                        fontSize = 8.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
                color = ratingBadgeColor(player.rating),
                shape = RoundedCornerShape(5.dp)
            ) {
                Text(
                    text = String.format(Locale.US, "%.1f", player.rating),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
        }
    }
}

private fun ratingBadgeColor(rating: Double): Color = when {
    rating >= 8.2 -> Color(0xFF00B4D8)
    rating >= 7.5 -> Color(0xFF2ECC71)
    rating >= 6.8 -> Color(0xFFF39C12)
    else -> Color(0xFF7F8C8D)
}

@Composable
private fun PlayerRatingBadge(
    rating: Double,
    isLarge: Boolean,
    modifier: Modifier = Modifier
) {
    val bg = ratingBadgeColor(rating)
    Surface(
        color = bg,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Text(
            text = String.format(Locale.US, "%.1f", rating),
            color = DarkGraphite,
            fontSize = if (isLarge) 12.sp else 8.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(
                horizontal = if (isLarge) 7.dp else 3.dp,
                vertical = if (isLarge) 3.dp else 1.dp
            )
        )
    }
}

@Composable
private fun TacticalPitch2DView(
    match: FootballMatch,
    lineups: MatchLineupsData,
    filterMode: PitchTeamFilterMode,
    showPassingNetwork: Boolean,
    selectedPlayer: PitchPlayerNode?,
    onSelectPlayer: (PitchPlayerNode) -> Unit
) {
    val pitchCanvasHeight = when (filterMode) {
        PitchTeamFilterMode.BOTH -> 700.dp
        PitchTeamFilterMode.HOME_ONLY, PitchTeamFilterMode.AWAY_ONLY -> 470.dp
    }

    Surface(
        color = Color(0xFF0C3B24),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.5.dp, ArenaPitchGreen),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tactical_2d_pitch")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Home Team Tactical End-Zone Bar
            if (filterMode != PitchTeamFilterMode.AWAY_ONLY) {
                TacticalEndZoneTeamBar(
                    teamName = match.homeTeamName,
                    teamBadge = match.homeTeamBadge,
                    formation = lineups.homeFormation,
                    coachName = lineups.homeCoach,
                    avgRating = lineups.homeAvgRating,
                    accentColor = ArenaFloodlightLime,
                    isTopBar = true
                )
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(pitchCanvasHeight)
            ) {
                val pitchW = maxWidth
                val pitchH = maxHeight

                val homeRowMax = (lineups.homeStartingXI.maxOfOrNull { it.rowIndex } ?: 3).coerceAtLeast(1)
                val awayRowMax = (lineups.awayStartingXI.maxOfOrNull { it.rowIndex } ?: 3).coerceAtLeast(1)

                fun computePlayerNormalizedCoords(
                    player: PitchPlayerNode,
                    isHome: Boolean
                ): Pair<Float, Float> {
                    val xFraction = if (player.colCountInRow <= 1) {
                        0.5f
                    } else {
                        0.13f + (player.colIndex.toFloat() / (player.colCountInRow - 1).toFloat()) * 0.74f
                    }
                    // Realistic wing stagger for 3+ player rows so adjacent wide players & center players never collide
                    val isWidePlayer = player.colCountInRow >= 3 &&
                        (player.colIndex == 0 || player.colIndex == player.colCountInRow - 1)
                    val wingAdvance = if (isWidePlayer) 0.015f else 0f

                    val yFraction = when (filterMode) {
                        PitchTeamFilterMode.HOME_ONLY -> {
                            val base = 0.09f + (player.rowIndex.toFloat() / homeRowMax.toFloat()) * 0.78f
                            (base + wingAdvance).coerceIn(0.07f, 0.89f)
                        }
                        PitchTeamFilterMode.AWAY_ONLY -> {
                            val base = 0.89f - (player.rowIndex.toFloat() / awayRowMax.toFloat()) * 0.78f
                            (base - wingAdvance).coerceIn(0.09f, 0.91f)
                        }
                        PitchTeamFilterMode.BOTH -> {
                            if (isHome) {
                                val base = 0.062f + (player.rowIndex.toFloat() / homeRowMax.toFloat()) * 0.390f
                                (base + wingAdvance).coerceIn(0.055f, 0.462f)
                            } else {
                                val base = 0.918f - (player.rowIndex.toFloat() / awayRowMax.toFloat()) * 0.378f
                                (base - wingAdvance).coerceIn(0.530f, 0.924f)
                            }
                        }
                    }
                    return xFraction to yFraction
                }

                // 1. Pitch Grass Stripes, Markings, and Optional Tactical Passing Lines
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val stripeCount = 12
                    val stripeH = h / stripeCount
                    for (i in 0 until stripeCount) {
                        drawRect(
                            color = if (i % 2 == 0) Color(0xFF134B30) else Color(0xFF0F3F27),
                            topLeft = Offset(0f, i * stripeH),
                            size = Size(w, stripeH)
                        )
                    }

                    val lineColor = Color.White.copy(alpha = 0.28f)
                    val stroke = Stroke(width = 2.dp.toPx())

                    // Outer boundary
                    drawRect(
                        color = lineColor,
                        topLeft = Offset(8.dp.toPx(), 8.dp.toPx()),
                        size = Size(w - 16.dp.toPx(), h - 16.dp.toPx()),
                        style = stroke
                    )

                    // Halfway line & center circle
                    val centerY = when (filterMode) {
                        PitchTeamFilterMode.BOTH -> h / 2f
                        PitchTeamFilterMode.HOME_ONLY -> h * 0.93f
                        PitchTeamFilterMode.AWAY_ONLY -> h * 0.07f
                    }
                    drawLine(
                        color = lineColor,
                        start = Offset(8.dp.toPx(), centerY),
                        end = Offset(w - 8.dp.toPx(), centerY),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawCircle(
                        color = lineColor,
                        radius = 44.dp.toPx(),
                        center = Offset(w / 2f, centerY),
                        style = stroke
                    )
                    drawCircle(
                        color = lineColor,
                        radius = 3.5.dp.toPx(),
                        center = Offset(w / 2f, centerY)
                    )

                    // Top penalty box & 6-yard box
                    val penBoxW = w * 0.56f
                    val penBoxH = h * 0.14f
                    val sixBoxW = w * 0.28f
                    val sixBoxH = h * 0.055f
                    drawRect(
                        color = lineColor,
                        topLeft = Offset((w - penBoxW) / 2f, 8.dp.toPx()),
                        size = Size(penBoxW, penBoxH),
                        style = stroke
                    )
                    drawRect(
                        color = lineColor,
                        topLeft = Offset((w - sixBoxW) / 2f, 8.dp.toPx()),
                        size = Size(sixBoxW, sixBoxH),
                        style = stroke
                    )

                    // Bottom penalty box & 6-yard box
                    drawRect(
                        color = lineColor,
                        topLeft = Offset((w - penBoxW) / 2f, h - 8.dp.toPx() - penBoxH),
                        size = Size(penBoxW, penBoxH),
                        style = stroke
                    )
                    drawRect(
                        color = lineColor,
                        topLeft = Offset((w - sixBoxW) / 2f, h - 8.dp.toPx() - sixBoxH),
                        size = Size(sixBoxW, sixBoxH),
                        style = stroke
                    )

                    // Tactical Formation Passing Network Lines
                    if (showPassingNetwork) {
                        fun drawTeamPassingNetwork(players: List<PitchPlayerNode>, isHome: Boolean, netColor: Color) {
                            val byRow = players.groupBy { it.rowIndex }
                            val rows = byRow.keys.sorted()
                            for (rIdx in 0 until rows.size) {
                                val currentRowPlayers = byRow[rows[rIdx]].orEmpty()
                                for (c in 0 until currentRowPlayers.size - 1) {
                                    val (x1, y1) = computePlayerNormalizedCoords(currentRowPlayers[c], isHome)
                                    val (x2, y2) = computePlayerNormalizedCoords(currentRowPlayers[c + 1], isHome)
                                    drawLine(
                                        color = netColor.copy(alpha = 0.18f),
                                        start = Offset(w * x1, h * y1),
                                        end = Offset(w * x2, h * y2),
                                        strokeWidth = 1.4.dp.toPx()
                                    )
                                }
                                if (rIdx + 1 < rows.size) {
                                    val nextRowPlayers = byRow[rows[rIdx + 1]].orEmpty()
                                    currentRowPlayers.forEach { p1 ->
                                        val (x1, y1) = computePlayerNormalizedCoords(p1, isHome)
                                        val closestNext = nextRowPlayers.minByOrNull { p2 ->
                                            val (x2, _) = computePlayerNormalizedCoords(p2, isHome)
                                            kotlin.math.abs(x1 - x2)
                                        }
                                        if (closestNext != null) {
                                            val (x2, y2) = computePlayerNormalizedCoords(closestNext, isHome)
                                            drawLine(
                                                color = netColor.copy(alpha = 0.16f),
                                                start = Offset(w * x1, h * y1),
                                                end = Offset(w * x2, h * y2),
                                                strokeWidth = 1.3.dp.toPx()
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (filterMode != PitchTeamFilterMode.AWAY_ONLY) {
                            drawTeamPassingNetwork(lineups.homeStartingXI, isHome = true, netColor = Color(0xFF00E5FF))
                        }
                        if (filterMode != PitchTeamFilterMode.HOME_ONLY) {
                            drawTeamPassingNetwork(lineups.awayStartingXI, isHome = false, netColor = Color(0xFFFFD166))
                        }
                    }
                }

                // 2. Home Team Starting XI Nodes
                if (filterMode != PitchTeamFilterMode.AWAY_ONLY) {
                    lineups.homeStartingXI.forEach { player ->
                        val (xFraction, yFraction) = computePlayerNormalizedCoords(player, isHome = true)
                        PitchPlayerAvatarNode(
                            player = player,
                            xPos = pitchW * xFraction,
                            yPos = pitchH * yFraction,
                            shirtColor = Color(0xFF00B4D8),
                            ringColor = Color(0xFF90E0EF),
                            isSelected = selectedPlayer?.playerId == player.playerId,
                            onClick = { onSelectPlayer(player) }
                        )
                    }
                }

                // 3. Away Team Starting XI Nodes
                if (filterMode != PitchTeamFilterMode.HOME_ONLY) {
                    lineups.awayStartingXI.forEach { player ->
                        val (xFraction, yFraction) = computePlayerNormalizedCoords(player, isHome = false)
                        PitchPlayerAvatarNode(
                            player = player,
                            xPos = pitchW * xFraction,
                            yPos = pitchH * yFraction,
                            shirtColor = Color(0xFFE0AC3E),
                            ringColor = Color(0xFFFFEAAC),
                            isSelected = selectedPlayer?.playerId == player.playerId,
                            onClick = { onSelectPlayer(player) }
                        )
                    }
                }
            }

            // Bottom Away Team Tactical End-Zone Bar
            if (filterMode != PitchTeamFilterMode.HOME_ONLY) {
                TacticalEndZoneTeamBar(
                    teamName = match.awayTeamName,
                    teamBadge = match.awayTeamBadge,
                    formation = lineups.awayFormation,
                    coachName = lineups.awayCoach,
                    avgRating = lineups.awayAvgRating,
                    accentColor = AmberGold,
                    isTopBar = false
                )
            }
        }
    }
}

@Composable
private fun TacticalEndZoneTeamBar(
    teamName: String,
    teamBadge: String,
    formation: String,
    coachName: String,
    avgRating: Double,
    accentColor: Color,
    isTopBar: Boolean
) {
    Surface(
        color = Color(0xFF072315).copy(alpha = 0.94f),
        border = BorderStroke(0.6.dp, ArenaPitchGreen.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                TeamCrestImage(
                    badgeUrl = teamBadge,
                    teamName = teamName,
                    sizeDp = 22
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = teamName,
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = accentColor.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(5.dp),
                            border = BorderStroke(0.6.dp, accentColor.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = formation,
                                color = accentColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = "${if (isTopBar) "HOME XI" else "AWAY XI"} • Coach: $coachName",
                        color = ChampagneMuted,
                        fontSize = 8.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                color = ratingBadgeColor(avgRating).copy(alpha = 0.22f),
                shape = RoundedCornerShape(7.dp),
                border = BorderStroke(0.8.dp, ratingBadgeColor(avgRating))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "AVG ",
                        color = Champagne,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f", avgRating),
                        color = Color.White,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerCircularAvatarMedallion(
    photoUrl: String,
    playerName: String,
    jerseyNumber: Int,
    primaryColor: Color,
    ringColor: Color,
    sizeDp: Int
) {
    Surface(
        color = Color(0xFF0A1D29),
        shape = CircleShape,
        border = BorderStroke(1.8.dp, ringColor),
        modifier = Modifier.size(sizeDp.dp)
    ) {
        if (photoUrl.isNotBlank()) {
            SubcomposeAsyncImage(
                model = photoUrl,
                contentDescription = playerName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                loading = {
                    TacticalKitVectorAvatar(
                        jerseyNumber = jerseyNumber,
                        kitColor = primaryColor,
                        sizeDp = sizeDp
                    )
                },
                error = {
                    TacticalKitVectorAvatar(
                        jerseyNumber = jerseyNumber,
                        kitColor = primaryColor,
                        sizeDp = sizeDp
                    )
                }
            )
        } else {
            TacticalKitVectorAvatar(
                jerseyNumber = jerseyNumber,
                kitColor = primaryColor,
                sizeDp = sizeDp
            )
        }
    }
}

@Composable
private fun TacticalKitVectorAvatar(
    jerseyNumber: Int,
    kitColor: Color,
    sizeDp: Int
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        kitColor.copy(alpha = 0.95f),
                        kitColor.copy(alpha = 0.55f),
                        Color(0xFF071620)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawRect(
                color = Color.White.copy(alpha = 0.16f),
                topLeft = Offset(w * 0.42f, 0f),
                size = Size(w * 0.16f, h)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.25f),
                radius = w * 0.22f,
                center = Offset(w * 0.5f, h * 0.06f),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
        Text(
            text = jerseyNumber.toString(),
            color = Color.White,
            fontSize = if (sizeDp >= 36) 13.sp else 11.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun PitchPlayerAvatarNode(
    player: PitchPlayerNode,
    xPos: Dp,
    yPos: Dp,
    shirtColor: Color,
    ringColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val nodeW = 74.dp
    val nodeH = 52.dp
    val ratingColor = ratingBadgeColor(player.rating)
    val effectiveRingColor = when {
        isSelected -> Color.White
        player.isMvp -> AmberGold
        else -> ringColor
    }

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .width(nodeW)
            .height(nodeH)
            .offset(x = xPos - nodeW / 2, y = yPos - nodeH / 2)
            .clickable { onClick() }
            .testTag("pitch_player_${player.playerId}")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(58.dp)
                    .height(33.dp)
            ) {
                PlayerCircularAvatarMedallion(
                    photoUrl = player.photoUrl,
                    playerName = player.name,
                    jerseyNumber = player.jerseyNumber,
                    primaryColor = shirtColor,
                    ringColor = effectiveRingColor,
                    sizeDp = 32
                )

                // Bottom-left Mini Jersey Number Shield
                Surface(
                    color = Color(0xFF06131C).copy(alpha = 0.92f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(0.6.dp, effectiveRingColor.copy(alpha = 0.85f)),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 3.dp, y = 1.dp)
                ) {
                    Text(
                        text = "${player.jerseyNumber}",
                        color = Color.White,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                    )
                }

                // Top-right Live Rating Badge (with ★ for Match MVP)
                Surface(
                    color = if (player.isMvp) AmberGold else ratingColor,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-1).dp, y = (-1).dp)
                ) {
                    Text(
                        text = if (player.isMvp) "★${String.format(Locale.US, "%.1f", player.rating)}"
                        else String.format(Locale.US, "%.1f", player.rating),
                        color = if (player.isMvp) DarkGraphite else Color.White,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 3.5.dp, vertical = 0.5.dp)
                    )
                }

                // Top-left Event Indicator (Yellow/Red Card or Subbed Off arrow)
                if (player.hasRedCard || player.hasYellowCard || player.subbedOffMinute != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = 3.dp, y = 0.dp)
                    ) {
                        when {
                            player.hasRedCard -> Box(
                                modifier = Modifier
                                    .size(width = 6.dp, height = 8.dp)
                                    .clip(RoundedCornerShape(1.5.dp))
                                    .background(LiveCrimson)
                            )
                            player.hasYellowCard -> Box(
                                modifier = Modifier
                                    .size(width = 6.dp, height = 8.dp)
                                    .clip(RoundedCornerShape(1.5.dp))
                                    .background(Color(0xFFFFD600))
                            )
                            player.subbedOffMinute != null -> Surface(
                                color = Color(0xFF10281E).copy(alpha = 0.92f),
                                shape = CircleShape,
                                border = BorderStroke(0.5.dp, Color(0xFFFF6B6B))
                            ) {
                                Text(
                                    text = "▼",
                                    color = Color(0xFFFF6B6B),
                                    fontSize = 6.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(1.dp))

            // Frosted Name & Goal/Assist Pill
            Surface(
                color = if (isSelected) AmberGold.copy(alpha = 0.92f) else Color(0xFF04140B).copy(alpha = 0.88f),
                shape = RoundedCornerShape(4.dp),
                border = if (isSelected || player.isMvp) {
                    BorderStroke(0.6.dp, AmberGold)
                } else {
                    BorderStroke(0.4.dp, Color.White.copy(alpha = 0.14f))
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.5.dp, vertical = 1.dp)
                ) {
                    if (player.goals > 0) {
                        Text(
                            text = if (player.goals > 1) "⚽${player.goals} " else "⚽ ",
                            fontSize = 7.5.sp
                        )
                    } else if (player.assists > 0) {
                        Text(
                            text = "🎯 ",
                            fontSize = 7.5.sp
                        )
                    }
                    Text(
                        text = if (player.isCaptain) "${player.shortName} (C)" else player.shortName,
                        color = if (isSelected) DarkGraphite else Color.White,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerTacticalSpotlightCard(
    player: PitchPlayerNode,
    teamName: String,
    teamBadge: String,
    isHomeTeam: Boolean
) {
    val accentColor = if (isHomeTeam) ArenaFloodlightLime else AmberGold
    val seed = remember(player.playerId) { kotlin.math.abs(player.name.hashCode()) }
    val touches = 38 + (seed % 42)
    val passAccuracy = 81 + (seed % 14)
    val duelsWon = 3 + (seed % 7)
    val keyActionLabel = when {
        player.positionCode == "GK" -> "Saves: ${2 + (seed % 4)}"
        player.rowIndex == 1 -> "Interceptions: ${2 + (seed % 5)}"
        player.rowIndex in 2..3 -> "Key Passes: ${1 + (seed % 4)}"
        else -> "Shots: ${1 + player.goals + (seed % 3)}"
    }

    Surface(
        color = MetalCharcoal,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (player.isMvp) AmberGold else accentColor.copy(alpha = 0.65f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("player_spotlight_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    PlayerCircularAvatarMedallion(
                        photoUrl = player.photoUrl,
                        playerName = player.name,
                        jerseyNumber = player.jerseyNumber,
                        primaryColor = if (isHomeTeam) Color(0xFF00B4D8) else Color(0xFFE0AC3E),
                        ringColor = if (player.isMvp) AmberGold else accentColor,
                        sizeDp = 46
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (player.isMvp) {
                                Surface(
                                    color = AmberGold.copy(alpha = 0.20f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(0.6.dp, AmberGold)
                                ) {
                                    Text(
                                        text = "★ STAR OF THE MATCH",
                                        color = AmberGold,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = "#${player.jerseyNumber} • ${player.positionCode}",
                                color = accentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            text = if (player.isCaptain) "${player.name} (Captain)" else player.name,
                            color = PaleGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val detailParts = buildList {
                            add(teamName)
                            if (player.goals > 0) add("⚽ ${player.goals} Goal(s)")
                            if (player.assists > 0) add("🎯 ${player.assists} Assist(s)")
                            if (player.hasYellowCard) add("🟨 Yellow Card")
                            if (player.hasRedCard) add("🟥 Red Card")
                            player.subbedOffMinute?.let { add("▼ Subbed off $it") }
                            player.subbedOnMinute?.let { add("▲ Subbed on $it") }
                        }
                        Text(
                            text = detailParts.joinToString(" • "),
                            color = ChampagneMuted,
                            fontSize = 10.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = ratingBadgeColor(player.rating).copy(alpha = 0.22f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ratingBadgeColor(player.rating))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "RATING",
                            color = ChampagneMuted,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f", player.rating),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Live Player Telemetry Strip (4 tactical metrics)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "Touches: $touches",
                    "Pass Acc: $passAccuracy%",
                    "Duels Won: $duelsWon",
                    keyActionLabel
                ).forEach { metric ->
                    Surface(
                        color = DarkGraphite,
                        shape = RoundedCornerShape(7.dp),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = metric,
                            color = Champagne,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BenchPlayerCell(
    player: PitchPlayerNode,
    isHome: Boolean,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val accent = if (isHome) ArenaFloodlightLime else AmberGold
    Surface(
        color = if (isSelected) accent.copy(alpha = 0.14f) else DarkGraphite.copy(alpha = 0.65f),
        shape = RoundedCornerShape(9.dp),
        border = BorderStroke(
            width = if (isSelected) 0.9.dp else 0.5.dp,
            color = if (isSelected) accent else Color.White.copy(alpha = 0.07f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 7.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                PlayerCircularAvatarMedallion(
                    photoUrl = player.photoUrl,
                    playerName = player.name,
                    jerseyNumber = player.jerseyNumber,
                    primaryColor = if (isHome) Color(0xFF00B4D8) else Color(0xFFE0AC3E),
                    ringColor = accent.copy(alpha = 0.75f),
                    sizeDp = 26
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = player.name,
                        color = PaleGold,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = buildString {
                            append("#${player.jerseyNumber} • ${player.positionCode}")
                            player.subbedOnMinute?.let { append(" • ▲ $it") }
                        },
                        color = if (player.subbedOnMinute != null) Color(0xFF2ECC71) else ChampagneMuted,
                        fontSize = 8.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (player.subbedOnMinute != null || player.goals > 0) {
                Surface(
                    color = ratingBadgeColor(player.rating).copy(alpha = 0.22f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(0.5.dp, ratingBadgeColor(player.rating))
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1f", player.rating),
                        color = Color.White,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

// =============================================================================
// TAB 3: STATS (DUAL-DIRECTION COMPARISON BARS)
// =============================================================================
@Composable
private fun MatchStatsTabContent(
    dossier: MatchFullDossier
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dossier.match.homeTeamName.uppercase(),
                    color = ArenaFloodlightLime,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "TEAM STATISTICS",
                    color = Champagne,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dossier.match.awayTeamName.uppercase(),
                    color = AmberGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        items(dossier.stats, key = { it.statLabel }) { stat ->
            StatComparisonBarCard(stat = stat)
        }
    }
}

@Composable
private fun StatComparisonBarCard(
    stat: MatchStatComparison
) {
    Surface(
        color = MetalCharcoal,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 0.7.dp,
            color = if (stat.isKeyMetric) ArenaPitchGreen.copy(alpha = 0.4f) else ChampagneSubtleBorder
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stat.homeValueText,
                    color = ArenaFloodlightLime,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = stat.statLabel,
                    color = PaleGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stat.awayValueText,
                    color = AmberGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Dual Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(50))
                    .background(DarkGraphite)
            ) {
                Box(
                    modifier = Modifier
                        .weight(stat.homeRatio.coerceIn(0.08f, 0.92f))
                        .fillMaxHeight()
                        .background(ArenaPitchGreen)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Box(
                    modifier = Modifier
                        .weight((1f - stat.homeRatio).coerceIn(0.08f, 0.92f))
                        .fillMaxHeight()
                        .background(AmberGold)
                )
            }
        }
    }
}

// =============================================================================
// TAB 4: H2H & STANDINGS TABLE
// =============================================================================
@Composable
private fun MatchH2HAndStandingsTabContent(
    dossier: MatchFullDossier
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Team Form (Last 5 Matches)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MetalCharcoal),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(0.8.dp, ChampagneSubtleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "RECENT FORM (LAST 5 MATCHES)",
                        color = ArenaFloodlightLime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TeamFormRow(teamName = dossier.match.homeTeamName, form = dossier.homeFormLast5)
                    Spacer(modifier = Modifier.height(6.dp))
                    TeamFormRow(teamName = dossier.match.awayTeamName, form = dossier.awayFormLast5)
                }
            }
        }

        // 2. Head-to-Head Meetings
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MetalCharcoal),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(0.8.dp, ChampagneSubtleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "HEAD-TO-HEAD MEETINGS",
                        color = AmberGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    dossier.h2hMeetings.forEachIndexed { idx, h2h ->
                        H2HMeetingRow(item = h2h)
                        if (idx < dossier.h2hMeetings.size - 1) {
                            HorizontalDivider(
                                color = ChampagneSubtleBorder.copy(alpha = 0.4f),
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Competition Standings Table
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MetalCharcoal),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(0.9.dp, ArenaPitchGreen.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "${dossier.match.tournamentName.uppercase()} STANDINGS",
                        color = ArenaFloodlightLime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("#", color = ChampagneMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(22.dp))
                        Text("TEAM", color = ChampagneMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("P", color = ChampagneMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                        Text("W-D-L", color = ChampagneMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(46.dp), textAlign = TextAlign.Center)
                        Text("GD", color = ChampagneMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
                        Text("PTS", color = AmberGold, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.width(30.dp), textAlign = TextAlign.End)
                    }

                    HorizontalDivider(color = ChampagneSubtleBorder, thickness = 0.6.dp)

                    dossier.standingsTable.forEach { row ->
                        StandingsTableRow(row = row)
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamFormRow(
    teamName: String,
    form: List<String>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = teamName,
            color = PaleGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            form.forEach { res ->
                val bg = when (res) {
                    "W" -> Color(0xFF10B981)
                    "D" -> Color(0xFFF59E0B)
                    else -> Color(0xFFEF4444)
                }
                Surface(
                    color = bg,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.size(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = res,
                            color = DarkGraphite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun H2HMeetingRow(item: H2HMeetingItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.width(78.dp)) {
            Text(item.dateLabel, color = Champagne, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            Text(item.competitionName, color = ChampagneMuted, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(
            text = "${item.homeTeamName}  ${item.homeScore} - ${item.awayScore}  ${item.awayTeamName}",
            color = PaleGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StandingsTableRow(row: LeagueStandingRow) {
    val isHighlighted = row.isHomeTeam || row.isAwayTeam
    val rowBg = when {
        row.isHomeTeam -> ArenaPitchGreen.copy(alpha = 0.22f)
        row.isAwayTeam -> AmberGold.copy(alpha = 0.18f)
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg, RoundedCornerShape(6.dp))
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = row.rank.toString(),
            color = if (row.rank <= 4) ArenaFloodlightLime else Champagne,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.width(22.dp)
        )
        Text(
            text = row.teamName,
            color = if (isHighlighted) PaleGold else Champagne,
            fontSize = 12.sp,
            fontWeight = if (isHighlighted) FontWeight.ExtraBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = row.played.toString(),
            color = Champagne,
            fontSize = 11.sp,
            modifier = Modifier.width(24.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = "${row.wins}-${row.draws}-${row.losses}",
            color = ChampagneMuted,
            fontSize = 10.sp,
            modifier = Modifier.width(46.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = row.goalDiffFormatted,
            color = Champagne,
            fontSize = 11.sp,
            modifier = Modifier.width(30.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = row.points.toString(),
            color = AmberGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.width(30.dp),
            textAlign = TextAlign.End
        )
    }
}

// =============================================================================
// TAB 5: MENA-ONLY REAL TV CHANNEL GUIDE (1-TAP WATCH VIA FAVORITES)
// =============================================================================
@Composable
private fun MatchMenaTvGuideTabContent(
    dossier: MatchFullDossier,
    onWatchChannel: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Surface(
                color = ArenaTurfElevated,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.9.dp, ArenaPitchGreen.copy(alpha = 0.55f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "MENA OFFICIAL TV BROADCAST GUIDE",
                        color = ArenaFloodlightLime,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap any MENA channel below to launch it directly from your saved Favorites list (with strict channel number matching).",
                        color = Champagne,
                        fontSize = 11.sp
                    )
                }
            }
        }

        items(dossier.menaBroadcastGuide, key = { it.channelName }) { channelInfo ->
            MenaBroadcastChannelCard(
                info = channelInfo,
                onWatchClicked = { onWatchChannel(channelInfo.channelName) }
            )
        }
    }
}

@Composable
private fun MenaBroadcastChannelCard(
    info: MenaBroadcastChannelInfo,
    onWatchClicked: () -> Unit
) {
    Surface(
        color = MetalCharcoal,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = if (info.isPrimaryBroadcaster) 1.2.dp else 0.8.dp,
            color = if (info.isPrimaryBroadcaster) ArenaPitchGreen else ChampagneSubtleBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onWatchClicked() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LiveTv,
                        contentDescription = null,
                        tint = ArenaFloodlightLime,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = info.channelName,
                        color = PaleGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = AmberGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.6.dp, AmberGold)
                    ) {
                        Text(
                            text = info.qualityBadge,
                            color = AmberGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${info.providerGroup} • ${info.commentaryInfo}",
                    color = Champagne,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = info.satelliteInfo,
                    color = ChampagneMuted,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = onWatchClicked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ArenaPitchGreen,
                    contentColor = DarkGraphite
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Watch", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}
