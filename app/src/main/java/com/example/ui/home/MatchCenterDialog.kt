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
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Warning
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
@Composable
private fun MatchLineups2DPitchTabContent(
    dossier: MatchFullDossier
) {
    val lineups = dossier.lineups
    val match = dossier.match
    var inspectedPlayer by remember { mutableStateOf<PitchPlayerNode?>(lineups.homeStartingXI.lastOrNull()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Confirmed / Probable XI & Formation Banner
        item {
            Surface(
                color = ArenaTurfElevated,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.9.dp, ArenaPitchGreen.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${match.homeTeamName}: ${lineups.homeFormation}",
                        color = ArenaFloodlightLime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Surface(
                        color = DarkGraphite,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.6.dp, AmberGold)
                    ) {
                        Text(
                            text = if (lineups.isConfirmed) "CONFIRMED XI" else "PROBABLE XI",
                            color = AmberGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "${match.awayTeamName}: ${lineups.awayFormation}",
                        color = AmberGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // 2. Interactive 2D Tactical Pitch with Formations & Player Ratings
        item {
            TacticalPitch2DView(
                homeTeamName = match.homeTeamName,
                awayTeamName = match.awayTeamName,
                lineups = lineups,
                selectedPlayer = inspectedPlayer,
                onSelectPlayer = { inspectedPlayer = it }
            )
        }

        // 3. Tapped Player Tactical Inspector Card
        inspectedPlayer?.let { player ->
            item {
                Surface(
                    color = MetalCharcoal,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = ArenaPitchGreen,
                                shape = CircleShape,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "#${player.jerseyNumber}",
                                        color = DarkGraphite,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${player.name} (${player.positionCode})",
                                    color = PaleGold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                val detailParts = buildList {
                                    if (player.goals > 0) add("⚽ ${player.goals} Goal(s)")
                                    if (player.assists > 0) add("🎯 ${player.assists} Assist(s)")
                                    if (player.hasYellowCard) add("🟨 Yellow Card")
                                    player.subbedOffMinute?.let { add("⇅ Subbed out $it") }
                                    player.subbedOnMinute?.let { add("⇅ Subbed in $it") }
                                    if (isEmpty()) add("Starting XI • Tactical Role: ${player.positionCode}")
                                }
                                Text(
                                    text = detailParts.joinToString(" • "),
                                    color = Champagne,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        PlayerRatingBadge(rating = player.rating, isLarge = true)
                    }
                }
            }
        }

        // 4. Coaches Row
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MetalCharcoal),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.8.dp, ChampagneSubtleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("HOME MANAGER", color = ChampagneMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(lineups.homeCoach, color = PaleGold, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Icon(Icons.Default.Groups, contentDescription = null, tint = AmberGold, modifier = Modifier.size(20.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text("AWAY MANAGER", color = ChampagneMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(lineups.awayCoach, color = PaleGold, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        // 5. Substitutes Bench (Dual Column)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MetalCharcoal),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(0.8.dp, ChampagneSubtleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "SUBSTITUTES BENCH",
                        color = ArenaFloodlightLime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val maxBench = maxOf(lineups.homeBench.size, lineups.awayBench.size)
                    for (i in 0 until maxBench) {
                        val hSub = lineups.homeBench.getOrNull(i)
                        val aSub = lineups.awayBench.getOrNull(i)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            BenchPlayerCell(
                                player = hSub,
                                alignRight = false,
                                modifier = Modifier.weight(1f),
                                onClick = { if (hSub != null) inspectedPlayer = hSub }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            BenchPlayerCell(
                                player = aSub,
                                alignRight = true,
                                modifier = Modifier.weight(1f),
                                onClick = { if (aSub != null) inspectedPlayer = aSub }
                            )
                        }
                        if (i < maxBench - 1) {
                            HorizontalDivider(color = ChampagneSubtleBorder.copy(alpha = 0.4f), thickness = 0.5.dp)
                        }
                    }
                }
            }
        }

        // 6. Missing Players (Injuries & Suspensions)
        if (lineups.missingPlayers.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MetalCharcoal),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(0.8.dp, LiveCrimson.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = LiveCrimson,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MISSING PLAYERS (INJURIES & SUSPENSIONS)",
                                color = LiveCrimson,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        lineups.missingPlayers.forEach { missing ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
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
                                        text = missing.reason,
                                        color = ChampagneMuted,
                                        fontSize = 10.sp
                                    )
                                }
                                Surface(
                                    color = LiveCrimson.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp),
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

/**
 * 2D Tactical Stadium Pitch rendered with Jetpack Compose Canvas & positioned player nodes.
 * Home team occupies the top half (playing downward); Away team occupies the bottom half (playing upward).
 */
@Composable
private fun TacticalPitch2DView(
    homeTeamName: String,
    awayTeamName: String,
    lineups: MatchLineupsData,
    selectedPlayer: PitchPlayerNode?,
    onSelectPlayer: (PitchPlayerNode) -> Unit
) {
    val homeRowsCount = (lineups.homeStartingXI.maxOfOrNull { it.rowIndex } ?: 3) + 1
    val awayRowsCount = (lineups.awayStartingXI.maxOfOrNull { it.rowIndex } ?: 3) + 1

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(540.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.4.dp, ArenaPitchGreen, RoundedCornerShape(16.dp))
            .testTag("tactical_2d_pitch")
    ) {
        val pitchWidthDp = maxWidth
        val pitchHeightDp = maxHeight

        // 1. Stadium Pitch Canvas (Alternating Turf Stripes + Penalty Areas + Halfway Line + Center Circle)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val stripeCount = 10
            val stripeH = h / stripeCount

            for (i in 0 until stripeCount) {
                drawRect(
                    color = if (i % 2 == 0) Color(0xFF0E2F23) else Color(0xFF12382A),
                    topLeft = Offset(0f, i * stripeH),
                    size = Size(w, stripeH)
                )
            }

            val lineStroke = Stroke(width = 2.5f)
            val lineColor = Color.White.copy(alpha = 0.35f)
            val pad = 18f

            // Outer Pitch Boundary
            drawRoundRect(
                color = lineColor,
                topLeft = Offset(pad, pad),
                size = Size(w - pad * 2, h - pad * 2),
                cornerRadius = CornerRadius(8f, 8f),
                style = lineStroke
            )

            // Halfway Line & Center Circle
            drawLine(
                color = lineColor,
                start = Offset(pad, h / 2f),
                end = Offset(w - pad, h / 2f),
                strokeWidth = 2.5f
            )
            drawCircle(
                color = lineColor,
                radius = w * 0.14f,
                center = Offset(w / 2f, h / 2f),
                style = lineStroke
            )
            drawCircle(
                color = lineColor,
                radius = 5f,
                center = Offset(w / 2f, h / 2f)
            )

            // Top Penalty Box & 6-Yard Box (Home)
            val penW = w * 0.56f
            val penH = h * 0.14f
            drawRect(
                color = lineColor,
                topLeft = Offset((w - penW) / 2f, pad),
                size = Size(penW, penH),
                style = lineStroke
            )
            val sixW = w * 0.28f
            val sixH = h * 0.055f
            drawRect(
                color = lineColor,
                topLeft = Offset((w - sixW) / 2f, pad),
                size = Size(sixW, sixH),
                style = lineStroke
            )

            // Bottom Penalty Box & 6-Yard Box (Away)
            drawRect(
                color = lineColor,
                topLeft = Offset((w - penW) / 2f, h - pad - penH),
                size = Size(penW, penH),
                style = lineStroke
            )
            drawRect(
                color = lineColor,
                topLeft = Offset((w - sixW) / 2f, h - pad - sixH),
                size = Size(sixW, sixH),
                style = lineStroke
            )
        }

        // 2. Home Starting XI Nodes (Top Half: yFraction 0.07 .. 0.44)
        lineups.homeStartingXI.forEach { node ->
            val yFraction = 0.07f + (node.rowIndex.toFloat() / (homeRowsCount - 1).coerceAtLeast(1)) * 0.36f
            val xFraction = (node.colIndex + 1).toFloat() / (node.colCountInRow + 1).toFloat()
            val xOffset = pitchWidthDp * xFraction - 28.dp
            val yOffset = pitchHeightDp * yFraction - 22.dp

            PitchPlayerShirtMarker(
                node = node,
                isHomeTeam = true,
                isSelected = selectedPlayer?.playerId == node.playerId,
                onClick = { onSelectPlayer(node) },
                modifier = Modifier.offset(x = xOffset, y = yOffset)
            )
        }

        // 3. Away Starting XI Nodes (Bottom Half: yFraction 0.91 down to 0.55)
        lineups.awayStartingXI.forEach { node ->
            val yFraction = 0.90f - (node.rowIndex.toFloat() / (awayRowsCount - 1).coerceAtLeast(1)) * 0.35f
            val xFraction = (node.colIndex + 1).toFloat() / (node.colCountInRow + 1).toFloat()
            val xOffset = pitchWidthDp * xFraction - 28.dp
            val yOffset = pitchHeightDp * yFraction - 22.dp

            PitchPlayerShirtMarker(
                node = node,
                isHomeTeam = false,
                isSelected = selectedPlayer?.playerId == node.playerId,
                onClick = { onSelectPlayer(node) },
                modifier = Modifier.offset(x = xOffset, y = yOffset)
            )
        }
    }
}

@Composable
private fun PitchPlayerShirtMarker(
    node: PitchPlayerNode,
    isHomeTeam: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shirtFill = if (isHomeTeam) ArenaPitchGreen else AmberGold
    val shirtBorder = if (isSelected) Color.White else DarkGraphite

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(56.dp)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            // Shirt Circle with Squad Number
            Surface(
                color = shirtFill,
                shape = CircleShape,
                border = BorderStroke(if (isSelected) 2.dp else 1.2.dp, shirtBorder),
                modifier = Modifier.size(30.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = node.jerseyNumber.toString(),
                        color = DarkGraphite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // 365Scores Rating Mini Pill on Top-Right
            PlayerRatingBadge(
                rating = node.rating,
                isLarge = false,
                modifier = Modifier.offset(x = 8.dp, y = (-4).dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Player Short Name Pill + Goal/Card Icon
        Surface(
            color = DarkGraphite.copy(alpha = 0.86f),
            shape = RoundedCornerShape(4.dp)
        ) {
            val prefix = when {
                node.goals > 0 -> "⚽ "
                node.hasYellowCard -> "🟨 "
                else -> ""
            }
            Text(
                text = "$prefix${node.shortName}",
                color = PaleGold,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }
    }
}

@Composable
private fun PlayerRatingBadge(
    rating: Double,
    isLarge: Boolean,
    modifier: Modifier = Modifier
) {
    val bg = when {
        rating >= 7.5 -> Color(0xFF10B981)
        rating >= 6.8 -> Color(0xFFF59E0B)
        else -> Color(0xFF64748B)
    }
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
private fun BenchPlayerCell(
    player: PitchPlayerNode?,
    alignRight: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    if (player == null) {
        Spacer(modifier = modifier)
        return
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (alignRight) Arrangement.End else Arrangement.Start,
        modifier = modifier.clickable { onClick() }
    ) {
        if (!alignRight) {
            Text(
                text = "#${player.jerseyNumber}",
                color = ArenaFloodlightLime,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.width(26.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.name,
                    color = PaleGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = buildString {
                        append(player.positionCode)
                        player.subbedOnMinute?.let { append(" • ⇅ $it") }
                    },
                    color = ChampagneMuted,
                    fontSize = 9.sp
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = player.name,
                    color = PaleGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = buildString {
                        player.subbedOnMinute?.let { append("⇅ $it • ") }
                        append(player.positionCode)
                    },
                    color = ChampagneMuted,
                    fontSize = 9.sp
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "#${player.jerseyNumber}",
                color = AmberGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
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
