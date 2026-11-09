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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
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
import androidx.compose.ui.graphics.PathEffect
import com.example.data.repository.MatchCenterEnricher
import com.example.data.repository.MatchCenterTab
import com.example.data.repository.MatchEventType
import com.example.data.repository.MatchFullDossier
import com.example.data.repository.MatchLineupsData
import com.example.data.repository.MatchShotPoint
import com.example.data.repository.MatchStatComparison
import com.example.data.repository.MatchStatus
import com.example.data.repository.MatchTeamSide
import com.example.data.repository.MatchTimelineEvent
import com.example.data.repository.MenaBroadcastChannelInfo
import com.example.data.repository.MomentumMinutePoint
import com.example.data.repository.PitchPlayerNode
import com.example.data.repository.ShotOutcome
import com.example.data.repository.formatDynamicKickoffTime
import kotlinx.coroutines.delay
import kotlin.math.abs
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
import com.example.ui.theme.TabularNumeralTextStyle
import com.example.ui.theme.tvFocusableControl
import com.example.ui.theme.tvFocusablePill
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
                        .tvFocusableControl(
                            accentColor = AmberGold,
                            shape = CircleShape,
                            onClick = onDismiss
                        )
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
                val isSummerTime = remember(dossier.countryZone) {
                    dossier.countryZone.isSummerTimeActive()
                }
                val dynamicKickoffTime = remember(match.kickoffEpochMs, match.kickoffTimeFormatted, dossier.countryZone, isSummerTime) {
                    if (match.kickoffEpochMs > 0L) {
                        formatDynamicKickoffTime(match.kickoffEpochMs, dossier.countryZone, isSummerTime)
                    } else {
                        match.kickoffTimeFormatted
                    }
                }
                val periodStartEpochMs = remember(match.matchId, match.liveMinuteOrBadge, match.status) {
                    val seedSec = (abs(match.matchId.hashCode()) % 52)
                    System.currentTimeMillis() - (seedSec * 1000L)
                }
                var liveElapsedSeconds by remember(periodStartEpochMs) {
                    mutableIntStateOf(((System.currentTimeMillis() - periodStartEpochMs) / 1000L).toInt().coerceAtLeast(0))
                }
                if (match.status == MatchStatus.LIVE) {
                    LaunchedEffect(Unit) {
                        while (true) {
                            liveElapsedSeconds = ((System.currentTimeMillis() - periodStartEpochMs) / 1000L).toInt().coerceAtLeast(0)
                            delay(1000L)
                        }
                    }
                }
                val liveClockTelemetry = remember(
                    match.liveMinuteOrBadge,
                    match.status,
                    match.halfTimeScore,
                    dynamicKickoffTime,
                    liveElapsedSeconds
                ) {
                    parseLiveMatchClockTelemetry(
                        match = match,
                        utcOffsetBadge = dossier.countryZone.getUtcOffsetBadge(),
                        elapsedSecondOffset = if (match.status == MatchStatus.LIVE) liveElapsedSeconds else 0,
                        dynamicKickoffTime = dynamicKickoffTime
                    )
                }
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
                        // Status / Minute Pill (1Hz MM:SS Live Clock)
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
                                    MatchStatus.LIVE -> "LIVE • ${liveClockTelemetry.heroReadoutSeconds}"
                                    MatchStatus.HALFTIME -> "HALF TIME"
                                    MatchStatus.FINISHED -> "FULL TIME"
                                    MatchStatus.UPCOMING -> "${match.kickoffDateFormatted} • $dynamicKickoffTime"
                                },
                                color = when {
                                    isLive -> LiveCrimson
                                    match.status == MatchStatus.FINISHED -> Champagne
                                    else -> KickoffTropicalTeal
                                },
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                style = TabularNumeralTextStyle,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 2.5.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(5.dp))

                        // Big Score or Kickoff Time
                        Text(
                            text = if (match.homeScore != null && match.awayScore != null) {
                                "${match.homeScore}  :  ${match.awayScore}"
                            } else {
                                dynamicKickoffTime
                            },
                            color = if (isLive) Color.White else KickoffPeachCream,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            style = TabularNumeralTextStyle,
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
                            .tvFocusablePill(
                                accentColor = AmberGold,
                                shape = RoundedCornerShape(50),
                                onClick = onWatchPrimaryChannel
                            )
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
                    .tvFocusablePill(
                        accentColor = AmberGold,
                        shape = RoundedCornerShape(50),
                        onClick = { onSelectTab(tab) }
                    )
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
        if (dossier.momentumPoints.isNotEmpty()) {
            item {
                AttackMomentumWaveCard(dossier = dossier)
            }
        }

        if (dossier.matchBuzzInsights.isNotEmpty()) {
            item {
                MatchBuzzInsightsCard(insights = dossier.matchBuzzInsights)
            }
        }

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
                        val isSummerBriefing = dossier.countryZone.isSummerTimeActive()
                        val briefingKickoff = if (match.kickoffEpochMs > 0L) {
                            formatDynamicKickoffTime(match.kickoffEpochMs, dossier.countryZone, isSummerBriefing)
                        } else {
                            match.kickoffTimeFormatted
                        }
                        Text(
                            text = "Kickoff is scheduled at $briefingKickoff (${dossier.countryZone.countryName} ${dossier.countryZone.getUtcOffsetBadge()}). Probable starting lineups (${dossier.lineups.homeFormation} vs ${dossier.lineups.awayFormation}), H2H history, and MENA broadcast channels are available across the tabs above.",
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

        // Quick Country Zone Broadcast Strip inside Summary
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ArenaTurfDark),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.55f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "${dossier.countryZone.flagEmoji} OFFICIAL ${dossier.countryZone.countryName.uppercase()} BROADCASTERS (TAP TO WATCH VIA FAVORITES)",
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
                                modifier = Modifier.tvFocusablePill(
                                    accentColor = ArenaFloodlightLime,
                                    shape = RoundedCornerShape(8.dp),
                                    onClick = { onWatchChannel(item.channelName) }
                                )
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

        // Match Details Footer (Venue, Referee, Attendance, Kickoff Time in User's Country Zone)
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
                    MatchInfoKeyValueRow(
                        "Kickoff (${dossier.countryZone.countryName} • ${dossier.countryZone.getUtcOffsetBadge()})",
                        "${match.kickoffTimeFormatted} (${match.kickoffDateFormatted})"
                    )
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
// TAB 2: LINEUPS (365SCORES 3D PERSPECTIVE PITCH + SOFASCORE RATINGS + SEPARATED TEAMS)
// =============================================================================
private enum class PitchTeamFilterMode(val label: String) {
    HOME_ONLY("HOME XI"),
    AWAY_ONLY("AWAY XI"),
    BOTH("BOTH (22)")
}

@Composable
private fun MatchLineups2DPitchTabContent(
    dossier: MatchFullDossier
) {
    val lineups = dossier.lineups
    val match = dossier.match
    val isPlayedOrLive = match.status == MatchStatus.LIVE ||
        match.status == MatchStatus.HALFTIME ||
        match.status == MatchStatus.FINISHED

    // Default to HOME_ONLY so each team's lineup is cleanly separated on a spacious 3D pitch (like 365Scores)
    var pitchFilterMode by remember { mutableStateOf(PitchTeamFilterMode.HOME_ONLY) }
    var showPassingNetwork by remember { mutableStateOf(false) }

    val activeTeamStarters = when (pitchFilterMode) {
        PitchTeamFilterMode.HOME_ONLY -> lineups.homeStartingXI
        PitchTeamFilterMode.AWAY_ONLY -> lineups.awayStartingXI
        PitchTeamFilterMode.BOTH -> lineups.homeStartingXI + lineups.awayStartingXI
    }
    val defaultSpotlightPlayer = remember(lineups, pitchFilterMode) {
        activeTeamStarters.firstOrNull { it.isMvp }
            ?: activeTeamStarters.maxByOrNull { it.rating }
            ?: activeTeamStarters.lastOrNull()
    }
    var inspectedPlayer by remember(lineups, pitchFilterMode) {
        mutableStateOf<PitchPlayerNode?>(defaultSpotlightPlayer)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. 365Scores-Style Dual Team Switcher Header + 1-Hour Pre-Match Status Pill
        item {
            Surface(
                color = Color(0xFF071923),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF1E3E52)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Top status row: Confirmed / Probable XI (1h rule) + Passing Links toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            val badgeColor = if (lineups.isConfirmed) Color(0xFF2ECC71) else AmberGold
                            Surface(
                                color = badgeColor.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.8.dp, badgeColor)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.5.dp)
                                ) {
                                    Icon(
                                        imageVector = if (lineups.isConfirmed) Icons.Default.EmojiEvents else Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = badgeColor,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (lineups.isConfirmed) "✓ OFFICIAL LINEUPS" else "PROBABLE XI • 1H PRE-MATCH",
                                        color = badgeColor,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = lineups.ratingSourceLabel,
                                color = ChampagneMuted,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Tactical Passing Links toggle pill
                        Surface(
                            color = if (showPassingNetwork) ArenaPitchGreen.copy(alpha = 0.22f) else DarkGraphite,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(
                                0.7.dp,
                                if (showPassingNetwork) ArenaFloodlightLime else Color.White.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .tvFocusablePill(
                                    accentColor = ArenaFloodlightLime,
                                    shape = RoundedCornerShape(50),
                                    onClick = { showPassingNetwork = !showPassingNetwork }
                                )
                                .testTag("toggle_passing_links_btn")
                        ) {
                            Text(
                                text = if (showPassingNetwork) "LINKS: ON" else "LINKS: OFF",
                                color = if (showPassingNetwork) ArenaFloodlightLime else ChampagneMuted,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp)
                            )
                        }
                    }

                    // 365Scores-Style Segmented Team Switcher: [ Home Team | Away Team | 22 View ]
                    Surface(
                        color = Color(0xFF040F16),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.8.dp, Color(0xFF1C8ADB).copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // HOME TEAM XI TAB
                            val isHomeSelected = pitchFilterMode == PitchTeamFilterMode.HOME_ONLY
                            Surface(
                                color = if (isHomeSelected) Color(0xFF1589FF) else Color.Transparent,
                                shape = RoundedCornerShape(9.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(9.dp))
                                    .tvFocusableControl(
                                        accentColor = AmberGold,
                                        shape = RoundedCornerShape(9.dp),
                                        focusedScale = 1.04f,
                                        onClick = { pitchFilterMode = PitchTeamFilterMode.HOME_ONLY }
                                    )
                                    .testTag("pitch_mode_home_only")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 6.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TeamCrestImage(
                                        badgeUrl = match.homeTeamBadge,
                                        teamName = match.homeTeamName,
                                        sizeDp = 16
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "${match.homeTeamName} XI",
                                        color = if (isHomeSelected) Color.White else Champagne,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // AWAY TEAM XI TAB
                            val isAwaySelected = pitchFilterMode == PitchTeamFilterMode.AWAY_ONLY
                            Surface(
                                color = if (isAwaySelected) Color(0xFF1589FF) else Color.Transparent,
                                shape = RoundedCornerShape(9.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(9.dp))
                                    .tvFocusableControl(
                                        accentColor = AmberGold,
                                        shape = RoundedCornerShape(9.dp),
                                        focusedScale = 1.04f,
                                        onClick = { pitchFilterMode = PitchTeamFilterMode.AWAY_ONLY }
                                    )
                                    .testTag("pitch_mode_away_only")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 6.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TeamCrestImage(
                                        badgeUrl = match.awayTeamBadge,
                                        teamName = match.awayTeamName,
                                        sizeDp = 16
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "${match.awayTeamName} XI",
                                        color = if (isAwaySelected) Color.White else Champagne,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // STACKED 3D DUAL VIEW TAB
                            val isBothSelected = pitchFilterMode == PitchTeamFilterMode.BOTH
                            Surface(
                                color = if (isBothSelected) AmberGold.copy(alpha = 0.24f) else Color.Transparent,
                                shape = RoundedCornerShape(9.dp),
                                border = if (isBothSelected) BorderStroke(0.8.dp, AmberGold) else null,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9.dp))
                                    .tvFocusableControl(
                                        accentColor = AmberGold,
                                        shape = RoundedCornerShape(9.dp),
                                        focusedScale = 1.04f,
                                        onClick = { pitchFilterMode = PitchTeamFilterMode.BOTH }
                                    )
                                    .testTag("pitch_mode_both")
                            ) {
                                Text(
                                    text = "🏟️ Stacked 3D Dual View",
                                    color = if (isBothSelected) AmberGold else ChampagneMuted,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. 365Scores-Style 3D Perspective Stadium Pitch with Circular Player Portraits & Formation Pill
        item {
            TacticalPitch2DView(
                match = match,
                lineups = lineups,
                filterMode = pitchFilterMode,
                showPassingNetwork = showPassingNetwork,
                isPlayedOrLive = isPlayedOrLive,
                selectedPlayer = inspectedPlayer,
                onSelectPlayer = { inspectedPlayer = it }
            )
        }

        // 3. Official 1-Hour Pre-Match Lineup Release Notice Banner (Directly below pitch like 365Scores)
        item {
            OfficialLineupTimingBanner(
                lineups = lineups,
                isPlayedOrLive = isPlayedOrLive
            )
        }

        // 4. Tapped Player Tactical Dossier Spotlight Card (with Player Headshot & Role Metrics)
        inspectedPlayer?.let { player ->
            item {
                val isHomePlayer = lineups.homeStartingXI.any { it.playerId == player.playerId } ||
                    lineups.homeBench.any { it.playerId == player.playerId }
                PlayerTacticalSpotlightCard(
                    player = player,
                    teamName = if (isHomePlayer) match.homeTeamName else match.awayTeamName,
                    teamBadge = if (isHomePlayer) match.homeTeamBadge else match.awayTeamBadge,
                    isHomeTeam = isHomePlayer,
                    isPlayedOrLive = isPlayedOrLive
                )
            }
        }

        // 5. Helpful Tactical Recommendations & Team DNA Card (Requirement 5)
        item {
            TeamTacticalRecommendationsCard(
                match = match,
                lineups = lineups,
                filterMode = pitchFilterMode,
                isPlayedOrLive = isPlayedOrLive,
                onSelectPlayer = { inspectedPlayer = it }
            )
        }

        // 6. Separated Manager / Head Coach Card for Selected Team (or Both if 22 View)
        item {
            SeparatedManagerCard(
                match = match,
                lineups = lineups,
                filterMode = pitchFilterMode
            )
        }

        // 7. Separated Substitutes Bench with Circular Player Avatars
        item {
            SeparatedSubstitutesBenchCard(
                match = match,
                lineups = lineups,
                filterMode = pitchFilterMode,
                isPlayedOrLive = isPlayedOrLive,
                inspectedPlayer = inspectedPlayer,
                onSelectPlayer = { inspectedPlayer = it }
            )
        }

        // 8. Separated Missing / Injured / Suspended Players
        val filteredMissing = when (pitchFilterMode) {
            PitchTeamFilterMode.HOME_ONLY -> lineups.missingPlayers.filter { it.teamSide == MatchTeamSide.HOME }
            PitchTeamFilterMode.AWAY_ONLY -> lineups.missingPlayers.filter { it.teamSide == MatchTeamSide.AWAY }
            PitchTeamFilterMode.BOTH -> lineups.missingPlayers
        }
        if (filteredMissing.isNotEmpty()) {
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
                                text = "MISSING & INJURED PLAYERS",
                                color = LiveCrimson,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp
                            )
                        }
                        filteredMissing.forEach { missing ->
                            val missingPhoto = remember(missing.playerName) {
                                // Try to resolve photo if available in cache
                                ""
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    PlayerCircularAvatarMedallion(
                                        photoUrl = missingPhoto,
                                        playerName = missing.playerName,
                                        jerseyNumber = 0,
                                        primaryColor = if (missing.teamSide == MatchTeamSide.HOME) Color(0xFF1B4F72) else Color(0xFF78281F),
                                        ringColor = LiveCrimson.copy(alpha = 0.7f),
                                        sizeDp = 34
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
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
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
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
private fun OfficialLineupTimingBanner(
    lineups: MatchLineupsData,
    isPlayedOrLive: Boolean
) {
    val accentColor = when {
        isPlayedOrLive -> Color(0xFF2ECC71)
        lineups.isConfirmed -> Color(0xFF00E5FF)
        else -> AmberGold
    }
    Surface(
        color = Color(0xFF081B26),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = accentColor.copy(alpha = 0.16f),
                shape = CircleShape,
                border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.6f)),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (lineups.isConfirmed || isPlayedOrLive) Icons.Default.EmojiEvents else Icons.Default.Schedule,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lineups.lineupStatusHeadline,
                    color = Color.White,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = lineups.lineupStatusSubtext,
                    color = ChampagneMuted,
                    fontSize = 10.sp,
                    lineHeight = 13.5.sp
                )
            }
        }
    }
}

@Composable
private fun TeamTacticalRecommendationsCard(
    match: FootballMatch,
    lineups: MatchLineupsData,
    filterMode: PitchTeamFilterMode,
    isPlayedOrLive: Boolean,
    onSelectPlayer: (PitchPlayerNode) -> Unit
) {
    val isHomeFocus = filterMode != PitchTeamFilterMode.AWAY_ONLY
    val teamName = if (isHomeFocus) match.homeTeamName else match.awayTeamName
    val formation = if (isHomeFocus) lineups.homeFormation else lineups.awayFormation
    val starters = if (isHomeFocus) lineups.homeStartingXI else lineups.awayStartingXI
    val avgRating = if (isHomeFocus) lineups.homeAvgRating else lineups.awayAvgRating
    val keyPlayer = remember(starters) {
        starters.maxByOrNull { it.rating + it.goals * 0.3 }
    }
    val tacticalStyle = remember(formation) {
        when {
            formation.startsWith("4-2-3-1") -> "Double Pivot Build-Up • Half-Space #10 Overload"
            formation.startsWith("4-3-3") -> "High-Pressing Wing Triangles • Inverted Full-Backs"
            formation.startsWith("3-5-2") || formation.startsWith("3-4-") -> "3-Back Wing-Back Width • Central Box Overload"
            else -> "Compact Mid-Block • Rapid Vertical Transitions"
        }
    }

    Surface(
        color = Color(0xFF0A1D29),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.9.dp, Color(0xFF00E5FF).copy(alpha = 0.35f)),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = AmberGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TACTICAL INSIGHTS • ${teamName.uppercase()}",
                        color = AmberGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.7.sp
                    )
                }
                Surface(
                    color = ratingBadgeColor(avgRating).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(0.7.dp, ratingBadgeColor(avgRating))
                ) {
                    Text(
                        text = "${if (isPlayedOrLive) "LIVE XI AVG" else "FORM INDEX"} ${String.format(Locale.US, "%.2f", avgRating)}",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "System: $formation • $tacticalStyle",
                color = Champagne,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )

            if (keyPlayer != null) {
                Surface(
                    color = DarkGraphite.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.6.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelectPlayer(keyPlayer) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            PlayerCircularAvatarMedallion(
                                photoUrl = keyPlayer.photoUrl,
                                playerName = keyPlayer.name,
                                jerseyNumber = keyPlayer.jerseyNumber,
                                primaryColor = if (isHomeFocus) Color(0xFF0055A4) else Color(0xFFC8102E),
                                ringColor = AmberGold,
                                sizeDp = 32
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Key Focal Point: ${keyPlayer.name} (#${keyPlayer.jerseyNumber})",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Primary creative hub in the ${keyPlayer.positionCode} zone • Tap to inspect",
                                    color = ChampagneMuted,
                                    fontSize = 9.5.sp
                                )
                            }
                        }
                        Surface(
                            color = ratingBadgeColor(keyPlayer.rating),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "★ ${String.format(Locale.US, "%.1f", keyPlayer.rating)}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeparatedManagerCard(
    match: FootballMatch,
    lineups: MatchLineupsData,
    filterMode: PitchTeamFilterMode
) {
    Surface(
        color = ArenaTurfElevated,
        shape = RoundedCornerShape(13.dp),
        border = BorderStroke(0.8.dp, ArenaPitchGreen.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        when (filterMode) {
            PitchTeamFilterMode.HOME_ONLY, PitchTeamFilterMode.AWAY_ONLY -> {
                val isHome = filterMode == PitchTeamFilterMode.HOME_ONLY
                val teamName = if (isHome) match.homeTeamName else match.awayTeamName
                val teamBadge = if (isHome) match.homeTeamBadge else match.awayTeamBadge
                val coachName = if (isHome) lineups.homeCoach else lineups.awayCoach
                val formation = if (isHome) lineups.homeFormation else lineups.awayFormation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TeamCrestImage(
                            badgeUrl = teamBadge,
                            teamName = teamName,
                            sizeDp = 32
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "MANAGER / HEAD COACH",
                                color = ChampagneMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.7.sp
                            )
                            Text(
                                text = coachName,
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    Surface(
                        color = Color(0xFF061B26),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.8.dp, ArenaFloodlightLime.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = formation.toCharArray().joinToString(" "),
                            color = ArenaFloodlightLime,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            PitchTeamFilterMode.BOTH -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${match.homeTeamName.uppercase()} COACH",
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
                            text = "${match.awayTeamName.uppercase()} COACH",
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
    }
}

@Composable
private fun SeparatedSubstitutesBenchCard(
    match: FootballMatch,
    lineups: MatchLineupsData,
    filterMode: PitchTeamFilterMode,
    isPlayedOrLive: Boolean,
    inspectedPlayer: PitchPlayerNode?,
    onSelectPlayer: (PitchPlayerNode) -> Unit
) {
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
            val headerTitle = when (filterMode) {
                PitchTeamFilterMode.HOME_ONLY -> "${match.homeTeamName.uppercase()} SUBSTITUTES BENCH"
                PitchTeamFilterMode.AWAY_ONLY -> "${match.awayTeamName.uppercase()} SUBSTITUTES BENCH"
                PitchTeamFilterMode.BOTH -> "SUBSTITUTES BENCH (BOTH TEAMS)"
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = headerTitle,
                    color = AmberGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.7.sp
                )
                Text(
                    text = "Tap player for details",
                    color = ChampagneMuted,
                    fontSize = 9.5.sp
                )
            }
            HorizontalDivider(color = ArenaPitchGreen.copy(alpha = 0.25f))

            when (filterMode) {
                PitchTeamFilterMode.HOME_ONLY, PitchTeamFilterMode.AWAY_ONLY -> {
                    val isHome = filterMode == PitchTeamFilterMode.HOME_ONLY
                    val benchList = if (isHome) lineups.homeBench else lineups.awayBench
                    val rows = benchList.chunked(2)
                    rows.forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { sub ->
                                Box(modifier = Modifier.weight(1f)) {
                                    BenchPlayerCell(
                                        player = sub,
                                        isHome = isHome,
                                        showLiveRating = isPlayedOrLive,
                                        isSelected = inspectedPlayer?.playerId == sub.playerId,
                                        onClick = { onSelectPlayer(sub) }
                                    )
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                PitchTeamFilterMode.BOTH -> {
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
                                        showLiveRating = isPlayedOrLive,
                                        isSelected = inspectedPlayer?.playerId == homeSub.playerId,
                                        onClick = { onSelectPlayer(homeSub) }
                                    )
                                }
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                if (awaySub != null) {
                                    BenchPlayerCell(
                                        player = awaySub,
                                        isHome = false,
                                        showLiveRating = isPlayedOrLive,
                                        isSelected = inspectedPlayer?.playerId == awaySub.playerId,
                                        onClick = { onSelectPlayer(awaySub) }
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

private fun ratingBadgeColor(rating: Double): Color = when {
    rating >= 8.0 -> Color(0xFF00B4D8)
    rating >= 7.0 -> Color(0xFF2ECC71)
    rating >= 6.5 -> Color(0xFFF39C12)
    else -> Color(0xFFE67E22)
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
    isPlayedOrLive: Boolean,
    selectedPlayer: PitchPlayerNode?,
    onSelectPlayer: (PitchPlayerNode) -> Unit
) {
    val homeKitPrimary = remember(match.homeTeamName) { resolveTeamPrimaryKitColor(match.homeTeamName, isHome = true) }
    val awayKitPrimary = remember(match.awayTeamName) { resolveTeamPrimaryKitColor(match.awayTeamName, isHome = false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tactical_2d_pitch"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (filterMode != PitchTeamFilterMode.AWAY_ONLY) {
            SingleTeam3DPitchCard(
                teamName = match.homeTeamName,
                teamBadge = match.homeTeamBadge,
                formation = lineups.homeFormation,
                coachName = lineups.homeCoach,
                avgRating = lineups.homeAvgRating,
                startingXI = lineups.homeStartingXI,
                kitPrimaryColor = homeKitPrimary,
                gkColor = Color(0xFF29B6F6),
                accentColor = ArenaFloodlightLime,
                passingNetColor = Color(0xFF00E5FF),
                isHomeTeam = true,
                showPassingNetwork = showPassingNetwork,
                isPlayedOrLive = isPlayedOrLive,
                selectedPlayer = selectedPlayer,
                onSelectPlayer = onSelectPlayer
            )
        }

        if (filterMode != PitchTeamFilterMode.HOME_ONLY) {
            SingleTeam3DPitchCard(
                teamName = match.awayTeamName,
                teamBadge = match.awayTeamBadge,
                formation = lineups.awayFormation,
                coachName = lineups.awayCoach,
                avgRating = lineups.awayAvgRating,
                startingXI = lineups.awayStartingXI,
                kitPrimaryColor = awayKitPrimary,
                gkColor = Color(0xFF26A69A),
                accentColor = AmberGold,
                passingNetColor = Color(0xFFFFD166),
                isHomeTeam = false,
                showPassingNetwork = showPassingNetwork,
                isPlayedOrLive = isPlayedOrLive,
                selectedPlayer = selectedPlayer,
                onSelectPlayer = onSelectPlayer
            )
        }
    }
}

@Composable
private fun SingleTeam3DPitchCard(
    teamName: String,
    teamBadge: String,
    formation: String,
    coachName: String,
    avgRating: Double,
    startingXI: List<PitchPlayerNode>,
    kitPrimaryColor: Color,
    gkColor: Color,
    accentColor: Color,
    passingNetColor: Color,
    isHomeTeam: Boolean,
    showPassingNetwork: Boolean,
    isPlayedOrLive: Boolean,
    selectedPlayer: PitchPlayerNode?,
    onSelectPlayer: (PitchPlayerNode) -> Unit
) {
    Surface(
        color = Color(0xFF071A12),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.2.dp, accentColor.copy(alpha = 0.65f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TacticalEndZoneTeamBar(
                teamName = teamName,
                teamBadge = teamBadge,
                formation = formation,
                coachName = coachName,
                avgRating = avgRating,
                accentColor = accentColor,
                isTopBar = isHomeTeam
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
            ) {
                val pitchW = maxWidth
                val pitchH = maxHeight
                val rowMax = (startingXI.maxOfOrNull { it.rowIndex } ?: 3).coerceAtLeast(1)

                // 3D Perspective Stadium Pitch coordinates:
                // - Goalkeeper (rowIndex = 0) at bottom near goal box (y = 0.77f)
                // - Defenders, Midfielders, and Forwards advancing up to top attacking box (y = 0.11f)
                // - Horizontal spread narrows toward the top to match the 3D trapezoid perspective
                fun compute3DPlayerNormalizedCoords(player: PitchPlayerNode): Pair<Float, Float> {
                    val progressFromGkToAttack = (player.rowIndex.toFloat() / rowMax.toFloat()).coerceIn(0f, 1f)
                    val isWidePlayer = player.colCountInRow >= 3 &&
                        (player.colIndex == 0 || player.colIndex == player.colCountInRow - 1)
                    val wingPushUp = if (rowMax < 4 && isWidePlayer && player.rowIndex in 1 until rowMax) 0.025f else 0f

                    val yFraction = (0.77f - progressFromGkToAttack * 0.66f - wingPushUp).coerceIn(0.10f, 0.78f)
                    val baseSpan = 0.78f - (1f - yFraction) * 0.20f
                    val isCompactCentralRow = (player.rowIndex == 1 && player.colCountInRow == 3) ||
                        (player.rowIndex >= 2 && player.colCountInRow == 2)
                    val perspectiveSpan = if (isCompactCentralRow) baseSpan * 0.74f else baseSpan
                    val leftMargin = (1f - perspectiveSpan) / 2f
                    val xFraction = if (player.colCountInRow <= 1) {
                        0.5f
                    } else {
                        leftMargin + (player.colIndex.toFloat() / (player.colCountInRow - 1).toFloat()) * perspectiveSpan
                    }
                    return xFraction to yFraction
                }

                // 1. 3D Perspective Trapezoid Stadium Pitch Turf & Lines
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    val topY = h * 0.10f
                    val bottomY = h * 0.89f
                    val topLeftX = w * 0.13f
                    val topRightX = w * 0.87f
                    val bottomLeftX = w * 0.02f
                    val bottomRightX = w * 0.98f

                    val turfPath = Path().apply {
                        moveTo(topLeftX, topY)
                        lineTo(topRightX, topY)
                        lineTo(bottomRightX, bottomY)
                        lineTo(bottomLeftX, bottomY)
                        close()
                    }

                    // Bottom turf apron
                    drawRect(
                        color = Color(0xFF1F6E3C),
                        topLeft = Offset(0f, bottomY),
                        size = Size(w, h - bottomY)
                    )

                    // Mowed grass stripes clipped inside 3D trapezoid
                    clipPath(turfPath) {
                        val stripeCount = 10
                        val turfH = bottomY - topY
                        val stripeH = turfH / stripeCount
                        for (i in 0 until stripeCount) {
                            drawRect(
                                color = if (i % 2 == 0) Color(0xFF237A43) else Color(0xFF1D6938),
                                topLeft = Offset(0f, topY + i * stripeH),
                                size = Size(w, stripeH + 1f)
                            )
                        }
                    }

                    val lineColor = Color.White.copy(alpha = 0.62f)
                    val stroke = Stroke(width = 1.8.dp.toPx())

                    drawPath(path = turfPath, color = lineColor, style = stroke)

                    fun leftSideX(t: Float) = topLeftX + (bottomLeftX - topLeftX) * t
                    fun rightSideX(t: Float) = topRightX + (bottomRightX - topRightX) * t
                    fun yAt(t: Float) = topY + (bottomY - topY) * t

                    val midT = 0.48f
                    val midY = yAt(midT)
                    drawLine(
                        color = lineColor,
                        start = Offset(leftSideX(midT), midY),
                        end = Offset(rightSideX(midT), midY),
                        strokeWidth = 1.8.dp.toPx()
                    )
                    drawOval(
                        color = lineColor,
                        topLeft = Offset(w * 0.36f, midY - h * 0.075f),
                        size = Size(w * 0.28f, h * 0.15f),
                        style = stroke
                    )

                    // Top attacking penalty box
                    val topPenBottomT = 0.18f
                    val topPenPath = Path().apply {
                        moveTo(w * 0.30f, topY)
                        lineTo(w * 0.70f, topY)
                        lineTo(w * 0.72f, yAt(topPenBottomT))
                        lineTo(w * 0.28f, yAt(topPenBottomT))
                        close()
                    }
                    drawPath(topPenPath, color = lineColor, style = stroke)

                    val topSixBottomT = 0.07f
                    val topSixPath = Path().apply {
                        moveTo(w * 0.40f, topY)
                        lineTo(w * 0.60f, topY)
                        lineTo(w * 0.61f, yAt(topSixBottomT))
                        lineTo(w * 0.39f, yAt(topSixBottomT))
                        close()
                    }
                    drawPath(topSixPath, color = lineColor, style = stroke)

                    // Bottom Goalkeeper penalty box
                    val botPenTopT = 0.79f
                    val botPenPath = Path().apply {
                        moveTo(w * 0.24f, yAt(botPenTopT))
                        lineTo(w * 0.76f, yAt(botPenTopT))
                        lineTo(w * 0.79f, bottomY)
                        lineTo(w * 0.21f, bottomY)
                        close()
                    }
                    drawPath(botPenPath, color = lineColor, style = stroke)

                    val botSixTopT = 0.91f
                    val botSixPath = Path().apply {
                        moveTo(w * 0.36f, yAt(botSixTopT))
                        lineTo(w * 0.64f, yAt(botSixTopT))
                        lineTo(w * 0.66f, bottomY)
                        lineTo(w * 0.34f, bottomY)
                        close()
                    }
                    drawPath(botSixPath, color = lineColor, style = stroke)

                    // Optional Tactical Passing Network Lines
                    if (showPassingNetwork) {
                        val byRow = startingXI.groupBy { it.rowIndex }
                        val rows = byRow.keys.sorted()
                        for (rIdx in rows.indices) {
                            val currentRowPlayers = byRow[rows[rIdx]].orEmpty()
                            for (c in 0 until currentRowPlayers.size - 1) {
                                val (x1, y1) = compute3DPlayerNormalizedCoords(currentRowPlayers[c])
                                val (x2, y2) = compute3DPlayerNormalizedCoords(currentRowPlayers[c + 1])
                                drawLine(
                                    color = passingNetColor.copy(alpha = 0.28f),
                                    start = Offset(w * x1, h * y1),
                                    end = Offset(w * x2, h * y2),
                                    strokeWidth = 1.6.dp.toPx()
                                )
                            }
                            if (rIdx + 1 < rows.size) {
                                val nextRowPlayers = byRow[rows[rIdx + 1]].orEmpty()
                                currentRowPlayers.forEach { p1 ->
                                    val (x1, y1) = compute3DPlayerNormalizedCoords(p1)
                                    val closestNext = nextRowPlayers.minByOrNull { p2 ->
                                        val (x2, _) = compute3DPlayerNormalizedCoords(p2)
                                        kotlin.math.abs(x1 - x2)
                                    }
                                    if (closestNext != null) {
                                        val (x2, y2) = compute3DPlayerNormalizedCoords(closestNext)
                                        drawLine(
                                            color = passingNetColor.copy(alpha = 0.24f),
                                            start = Offset(w * x1, h * y1),
                                            end = Offset(w * x2, h * y2),
                                            strokeWidth = 1.5.dp.toPx()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Bottom Turf Apron: Brand Watermark + Floating Formation Pill (`4 - 2 - 3 - 1`)
                val formattedFormation = formation.split("-").joinToString(" - ")
                Text(
                    text = "${teamName.uppercase()} • 3D TACTICAL PITCH",
                    color = Color.White.copy(alpha = 0.34f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 14.dp, bottom = 12.dp)
                )

                Surface(
                    color = Color(0xFF0D2818).copy(alpha = 0.90f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 14.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = formattedFormation,
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                // 3. Starting XI 3D Pitch Player Nodes (42.dp Cutout Medallions + #Jersey + Rating Pill + Name Plate)
                startingXI.forEach { player ->
                    val (xFraction, yFraction) = compute3DPlayerNormalizedCoords(player)
                    PitchPlayerAvatarNode(
                        player = player,
                        xPos = pitchW * xFraction,
                        yPos = pitchH * yFraction,
                        shirtColor = if (player.positionCode == "GK") gkColor else kitPrimaryColor,
                        ringColor = Color.White,
                        isSingleTeamMode = true,
                        isPlayedOrLive = isPlayedOrLive,
                        isSelected = selectedPlayer?.playerId == player.playerId,
                        onClick = { onSelectPlayer(player) }
                    )
                }
            }
        }
    }
}

private fun resolveTeamPrimaryKitColor(teamName: String, isHome: Boolean): Color {
    val lower = teamName.lowercase(Locale.ROOT)
    return when {
        lower.contains("fenerbahce") || lower.contains("fenerbahçe") || lower.contains("villarreal") || lower.contains("dortmund") -> Color(0xFFE5B80B)
        lower.contains("liverpool") || lower.contains("arsenal") || lower.contains("bayern") || lower.contains("manchester united") || lower.contains("benfica") || lower.contains("olympiacos") || lower.contains("wydad") || lower.contains("rennais") -> Color(0xFFC8102E)
        lower.contains("real madrid") -> Color(0xFF3B4C66)
        lower.contains("barcelona") || lower.contains("psg") || lower.contains("paris") || lower.contains("inter") || lower.contains("chelsea") || lower.contains("porto") -> Color(0xFF004D98)
        lower.contains("manchester city") || lower.contains("lazio") || lower.contains("napoli") -> Color(0xFF38BDF8)
        lower.contains("raja") || lower.contains("sporting") || lower.contains("celtic") || lower.contains("betis") -> Color(0xFF16A34A)
        lower.contains("galatasaray") || lower.contains("berkane") || lower.contains("roma") -> Color(0xFFD97706)
        else -> if (isHome) Color(0xFF0284C7) else Color(0xFFDC2626)
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
    val context = LocalContext.current
    val targetPx = with(LocalDensity.current) { sizeDp.dp.roundToPx().coerceAtLeast(64) }
    val imageRequest = remember(photoUrl, context, targetPx) {
        if (photoUrl.isNotBlank()) {
            ImageRequest.Builder(context)
                .data(photoUrl)
                .size(targetPx, targetPx)
                .precision(coil.size.Precision.EXACT)
                .bitmapConfig(android.graphics.Bitmap.Config.ARGB_8888)
                .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
                .addHeader("Referer", "https://www.sofascore.com/")
                .crossfade(true)
                .build()
        } else {
            null
        }
    }

    // Dark obsidian-slate medallion backdrop so transparent PNG cutouts & jersey fallbacks never flash as a blank white circle
    Surface(
        color = Color(0xFF0A1926),
        shape = CircleShape,
        border = BorderStroke(1.8.dp, ringColor),
        modifier = Modifier.size(sizeDp.dp)
    ) {
        if (imageRequest != null) {
            SubcomposeAsyncImage(
                model = imageRequest,
                contentDescription = playerName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                loading = {
                    TacticalAthletePortraitAvatar(
                        playerName = playerName,
                        jerseyNumber = jerseyNumber,
                        kitColor = primaryColor
                    )
                },
                error = {
                    TacticalAthletePortraitAvatar(
                        playerName = playerName,
                        jerseyNumber = jerseyNumber,
                        kitColor = primaryColor
                    )
                }
            )
        } else {
            TacticalAthletePortraitAvatar(
                playerName = playerName,
                jerseyNumber = jerseyNumber,
                kitColor = primaryColor
            )
        }
    }
}

/**
 * Styled Club Jersey Medallion Fallback:
 * Renders a tailored football kit silhouette with collar/sleeves and the player's real jersey number
 * and initials over a dark stadium slate backdrop whenever a remote cutout PNG is unavailable or fails to load.
 */
@Composable
private fun TacticalAthletePortraitAvatar(
    playerName: String,
    jerseyNumber: Int = 0,
    kitColor: Color
) {
    val initials = remember(playerName) {
        val parts = playerName.replace(Regex("#\\d+"), "").trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        when {
            parts.size >= 2 -> "${parts.first().first().uppercaseChar()}${parts.last().first().uppercaseChar()}"
            parts.size == 1 && parts.first().length >= 2 -> parts.first().take(2).uppercase(Locale.ROOT)
            parts.size == 1 -> parts.first().uppercase(Locale.ROOT)
            else -> "XI"
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF132A3E),
                        Color(0xFF091520)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Jersey torso + sleeves silhouette
            val jerseyPath = Path().apply {
                moveTo(w * 0.28f, h * 0.22f)
                lineTo(w * 0.10f, h * 0.38f)
                lineTo(w * 0.20f, h * 0.52f)
                lineTo(w * 0.27f, h * 0.45f)
                lineTo(w * 0.27f, h * 0.86f)
                lineTo(w * 0.73f, h * 0.86f)
                lineTo(w * 0.73f, h * 0.45f)
                lineTo(w * 0.80f, h * 0.52f)
                lineTo(w * 0.90f, h * 0.38f)
                lineTo(w * 0.72f, h * 0.22f)
                close()
            }
            drawPath(
                path = jerseyPath,
                color = kitColor
            )
            drawPath(
                path = jerseyPath,
                color = Color.White.copy(alpha = 0.38f),
                style = Stroke(width = w * 0.035f)
            )
            // Collar V-neck trim
            drawCircle(
                color = Color(0xFF091520),
                radius = w * 0.10f,
                center = Offset(w * 0.50f, h * 0.22f)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = 3.dp)
        ) {
            if (jerseyNumber > 0) {
                Text(
                    text = jerseyNumber.toString(),
                    color = Color.White,
                    fontSize = 10.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    style = TabularNumeralTextStyle,
                    maxLines = 1
                )
            }
            Text(
                text = initials,
                color = Color.White.copy(alpha = 0.92f),
                fontSize = if (jerseyNumber > 0) 6.5.sp else 9.sp,
                lineHeight = 7.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PitchPlayerAvatarNode(
    player: PitchPlayerNode,
    xPos: Dp,
    yPos: Dp,
    shirtColor: Color,
    ringColor: Color,
    isSingleTeamMode: Boolean,
    isPlayedOrLive: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val avatarSizeDp = if (isSingleTeamMode) 42 else 34
    val nodeW = if (isSingleTeamMode) 84.dp else 72.dp
    val nodeH = if (isSingleTeamMode) 68.dp else 52.dp
    val ratingColor = ratingBadgeColor(player.rating)
    val effectiveRingColor = when {
        isSelected -> AmberGold
        player.isMvp && isPlayedOrLive -> AmberGold
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
                    .width(if (isSingleTeamMode) 64.dp else 56.dp)
                    .height(if (isSingleTeamMode) 44.dp else 36.dp)
            ) {
                PlayerCircularAvatarMedallion(
                    photoUrl = player.photoUrl,
                    playerName = player.name,
                    jerseyNumber = player.jerseyNumber,
                    primaryColor = shirtColor,
                    ringColor = effectiveRingColor,
                    sizeDp = avatarSizeDp
                )

                // Top-Right 365Scores-style Circular White Jersey Number Badge (#10, #9, #11)
                Surface(
                    color = Color.White,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Color(0xFF1E293B).copy(alpha = 0.45f)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-1).dp, y = (-1).dp)
                        .size(if (isSingleTeamMode) 18.dp else 15.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "#${player.jerseyNumber}",
                            color = Color(0xFF0F172A),
                            fontSize = if (isSingleTeamMode) 7.5.sp else 6.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Top-Left Badge:
                // - When LIVE or FINISHED: Show Sofascore Real-Time Rating Badge (or MVP badge)
                // - When UPCOMING: Show sleek Position Badge (no fake live ratings, no fake cards, no fake sub arrows!)
                if (isPlayedOrLive) {
                    Surface(
                        color = if (player.isMvp) AmberGold else ratingColor,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.6.dp, Color.Black.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = 0.dp, y = (-1).dp)
                    ) {
                        Text(
                            text = if (player.isMvp) "${String.format(Locale.US, "%.1f", player.rating)} ★ MVP"
                            else String.format(Locale.US, "%.1f", player.rating),
                            color = if (player.isMvp) DarkGraphite else Color.White,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 3.5.dp, vertical = 0.5.dp)
                        )
                    }

                    // Bottom-Left Live Event Indicator (Yellow/Red Card or Subbed Off arrow)
                    if (player.hasRedCard || player.hasYellowCard || player.subbedOffMinute != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .offset(x = 2.dp, y = 0.dp)
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
                } else {
                    // Pre-match: subtle Captain / Position badge on top-left
                    Surface(
                        color = if (player.isCaptain) AmberGold else Color(0xFF0F172A).copy(alpha = 0.88f),
                        shape = CircleShape,
                        border = BorderStroke(0.7.dp, Color.White.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = 1.dp, y = 0.dp)
                    ) {
                        Text(
                            text = if (player.isCaptain) "C" else player.positionCode.take(2),
                            color = if (player.isCaptain) DarkGraphite else Color.White,
                            fontSize = 6.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // 365Scores-style Crisp Player Name Label Underneath Portrait
            Surface(
                color = if (isSelected) AmberGold.copy(alpha = 0.95f) else Color(0xFF061E11).copy(alpha = 0.82f),
                shape = RoundedCornerShape(6.dp),
                border = if (isSelected) {
                    BorderStroke(0.7.dp, Color.White)
                } else {
                    BorderStroke(0.4.dp, Color.White.copy(alpha = 0.18f))
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 5.5.dp, vertical = 1.5.dp)
                ) {
                    if (isPlayedOrLive && player.goals > 0) {
                        Text(
                            text = if (player.goals > 1) "⚽${player.goals} " else "⚽ ",
                            fontSize = 7.5.sp
                        )
                    } else if (isPlayedOrLive && player.assists > 0) {
                        Text(
                            text = "🎯 ",
                            fontSize = 7.5.sp
                        )
                    }
                    Text(
                        text = player.shortName,
                        color = if (isSelected) DarkGraphite else Color.White,
                        fontSize = if (isSingleTeamMode) 9.5.sp else 8.5.sp,
                        fontWeight = FontWeight.ExtraBold,
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
    isHomeTeam: Boolean,
    isPlayedOrLive: Boolean = true
) {
    val accentColor = if (isHomeTeam) ArenaFloodlightLime else AmberGold
    val seed = remember(player.playerId) { kotlin.math.abs(player.name.hashCode()) }
    val displayMinutes = if (player.minutesPlayed > 0) "${player.minutesPlayed}'" else "90'"
    val displayGoalsAssists = "${player.goals} / ${player.assists}"
    val displayXg = if (player.expectedGoals > 0.0 || player.expectedGoalsOnTarget > 0.0) {
        "${String.format(Locale.US, "%.2f", player.expectedGoals)} / ${String.format(Locale.US, "%.2f", player.expectedGoalsOnTarget)}"
    } else {
        String.format(Locale.US, "%.2f", player.expectedGoals)
    }
    val displayShots = "${player.shotsTotal} (${player.shotsOnTarget})"
    val displayPasses = if (player.passesTotal > 0) {
        "${player.passesAccurate}/${player.passesTotal}"
    } else {
        "${81 + (seed % 14)}%"
    }
    val displayTouchesDuels = if (player.touches > 0 || player.duelsWon > 0) {
        "${player.touches} • ${player.duelsWon} won"
    } else {
        "${38 + (seed % 42)} • ${3 + (seed % 7)} won"
    }

    val subBadgeText = when {
        player.subbedOnMinute != null -> "🔄 In ${player.subbedOnMinute}"
        player.subbedOffMinute != null -> "🔄 Out ${player.subbedOffMinute}"
        else -> null
    }

    Surface(
        color = MetalCharcoal,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (player.isMvp && isPlayedOrLive) AmberGold else accentColor.copy(alpha = 0.65f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("player_spotlight_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("player_tactical_spotlight_card"),
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
                        primaryColor = resolveTeamPrimaryKitColor(teamName, isHomeTeam),
                        ringColor = if (player.isMvp && isPlayedOrLive) AmberGold else accentColor,
                        sizeDp = 46
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (player.isMvp && isPlayedOrLive) {
                                Surface(
                                    color = AmberGold.copy(alpha = 0.20f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(0.6.dp, AmberGold)
                                ) {
                                    Text(
                                        text = "★ POTM • STAR OF THE MATCH",
                                        color = AmberGold,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "#${player.jerseyNumber} • ${player.positionCode}",
                                color = accentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            if (subBadgeText != null && isPlayedOrLive) {
                                Surface(
                                    color = KickoffTropicalTeal.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(0.5.dp, KickoffTropicalTeal.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = subBadgeText,
                                        color = KickoffPeachCream,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 4.5.dp, vertical = 1.dp)
                                    )
                                }
                            }
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
                            if (isPlayedOrLive) {
                                if (player.goals > 0) add("⚽ ${player.goals} Goal(s)")
                                if (player.assists > 0) add("🎯 ${player.assists} Assist(s)")
                                if (player.hasYellowCard) add("🟨 Yellow Card")
                                if (player.hasRedCard) add("🟥 Red Card")
                                player.subbedOffMinute?.let { add("▼ Subbed off $it") }
                                player.subbedOnMinute?.let { add("▲ Subbed on $it") }
                            } else {
                                add("Expected Starter • Pre-Match Season Form")
                            }
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
                            text = if (isPlayedOrLive) "RATING" else "FORM",
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

            // 365Scores / Flashscore Deep Match Performance 6-Cell Grid (2 rows x 3 columns)
            val perfItems = listOf(
                "Minutes" to displayMinutes,
                "Goals / Assists" to displayGoalsAssists,
                "xG / xGOT" to displayXg,
                "Shots (On Target)" to displayShots,
                "Passes" to displayPasses,
                "Touches & Duels" to displayTouchesDuels
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                perfItems.chunked(3).forEach { rowCells ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowCells.forEach { (label, value) ->
                            Surface(
                                color = DarkGraphite,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.6.dp, Color.White.copy(alpha = 0.09f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = label.uppercase(),
                                        color = ChampagneMuted,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = value,
                                        color = PaleGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        style = TabularNumeralTextStyle,
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
    }
}

@Composable
private fun BenchPlayerCell(
    player: PitchPlayerNode,
    isHome: Boolean,
    showLiveRating: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val accent = if (isHome) ArenaFloodlightLime else AmberGold
    Surface(
        color = if (isSelected) accent.copy(alpha = 0.14f) else DarkGraphite.copy(alpha = 0.72f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            width = if (isSelected) 0.9.dp else 0.5.dp,
            color = if (isSelected) accent else Color.White.copy(alpha = 0.09f)
        ),
        modifier = Modifier
            .fillMaxWidth()
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
                    primaryColor = if (isHome) Color(0xFF0055A4) else Color(0xFFC8102E),
                    ringColor = Color.White.copy(alpha = 0.85f),
                    sizeDp = 30
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = player.name,
                        color = PaleGold,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = buildString {
                            append("#${player.jerseyNumber} • ${player.positionCode}")
                            if (showLiveRating) {
                                player.subbedOnMinute?.let { append(" • ▲ $it") }
                            }
                        },
                        color = if (showLiveRating && player.subbedOnMinute != null) Color(0xFF2ECC71) else ChampagneMuted,
                        fontSize = 8.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (showLiveRating && (player.subbedOnMinute != null || player.goals > 0)) {
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
// TAB 3: STATS (ATTACK MOMENTUM WAVE, SHOT MAP & CATEGORIZED COMPARISON BARS)
// =============================================================================
private val STAT_CATEGORY_FILTERS = listOf(
    "All",
    "Top Stats",
    "Shots & xG",
    "Passes",
    "Defense & Duels",
    "Goalkeeping"
)

@Composable
private fun MatchStatsTabContent(
    dossier: MatchFullDossier
) {
    var selectedCategory by remember { mutableStateOf("All") }
    val filteredStats = remember(dossier.stats, selectedCategory) {
        when (selectedCategory) {
            "All" -> dossier.stats
            "Top Stats" -> dossier.stats.filter { it.category == "Top Stats" || it.isKeyMetric }
            else -> dossier.stats.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Categorized Stat Filter Pills Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("match_stats_category_row")
            ) {
                items(STAT_CATEGORY_FILTERS, key = { it }) { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        color = if (isSelected) KickoffTropicalTeal else MetalCharcoal,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(
                            width = 0.8.dp,
                            color = if (isSelected) KickoffPeachCream else ChampagneSubtleBorder
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { selectedCategory = cat }
                            .testTag("stat_cat_chip_${cat.lowercase(Locale.ROOT).replace(" ", "_").replace("&", "and")}")
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) DarkGraphite else Champagne,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 2. Attack Momentum / Pressure Wave Graph
        if (dossier.momentumPoints.isNotEmpty() && (selectedCategory == "All" || selectedCategory == "Top Stats")) {
            item {
                AttackMomentumWaveCard(dossier = dossier)
            }
        }

        // 3. Visual Half-Pitch Shot Map (xG & xGOT)
        if (dossier.shotMap.isNotEmpty() && (selectedCategory == "All" || selectedCategory == "Shots & xG" || selectedCategory == "Top Stats")) {
            item {
                ShotMapVisualCard(dossier = dossier)
            }
        }

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
                    text = "${selectedCategory.uppercase()} METRICS",
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

        if (filteredStats.isEmpty()) {
            item {
                Surface(
                    color = MetalCharcoal,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.7.dp, ChampagneSubtleBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (dossier.stats.isEmpty()) {
                            "Official team statistics will appear here once published by the live match feed."
                        } else {
                            "No $selectedCategory metrics recorded yet for this fixture."
                        },
                        color = ChampagneMuted,
                        fontSize = 11.5.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(filteredStats, key = { it.statLabel }) { stat ->
                StatComparisonBarCard(stat = stat)
            }
        }
    }
}

@Composable
internal fun AttackMomentumWaveCard(
    dossier: MatchFullDossier
) {
    val points = dossier.momentumPoints
    val goalEvents = remember(dossier.timelineEvents) {
        dossier.timelineEvents.filter {
            it.eventType == MatchEventType.GOAL || it.eventType == MatchEventType.PENALTY_GOAL
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MetalCharcoal),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.9.dp, KickoffTropicalTeal.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("match_momentum_wave_chart")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(KickoffTropicalTeal)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = dossier.match.homeTeamName.uppercase(),
                        color = KickoffTropicalTeal,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "⚡ ATTACK MOMENTUM WAVE",
                    color = PaleGold,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dossier.match.awayTeamName.uppercase(),
                        color = AmberGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AmberGold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0A141D))
                    .border(0.7.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 6.dp, vertical = 8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerY = size.height / 2f
                    val maxHalfHeight = (size.height / 2f) * 0.88f
                    val totalMinutes = maxOf(90, points.maxOfOrNull { it.minute } ?: 90)
                    val slotWidth = size.width / totalMinutes.toFloat()
                    val barWidth = (slotWidth * 0.72f).coerceAtLeast(1.8f)

                    // Center horizontal zero-axis
                    drawLine(
                        color = Color.White.copy(alpha = 0.22f),
                        start = Offset(0f, centerY),
                        end = Offset(size.width, centerY),
                        strokeWidth = 1.2f
                    )

                    // HT (45') vertical dashed divider
                    val htX = (45f / totalMinutes.toFloat()) * size.width
                    drawLine(
                        color = ChampagneMuted.copy(alpha = 0.45f),
                        start = Offset(htX, 0f),
                        end = Offset(htX, size.height),
                        strokeWidth = 1.3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f), 0f)
                    )

                    // Pressure wave bars per minute
                    points.forEach { pt ->
                        val fraction = (pt.value / 100f).coerceIn(-1f, 1f)
                        val barH = (kotlin.math.abs(fraction) * maxHalfHeight).coerceAtLeast(2f)
                        val x = ((pt.minute - 1).coerceAtLeast(0).toFloat() / totalMinutes.toFloat()) * size.width
                        if (fraction >= 0f) {
                            drawRoundRect(
                                color = KickoffTropicalTeal.copy(alpha = 0.88f),
                                topLeft = Offset(x, centerY - barH),
                                size = Size(barWidth, barH),
                                cornerRadius = CornerRadius(2f, 2f)
                            )
                        } else {
                            drawRoundRect(
                                color = AmberGold.copy(alpha = 0.88f),
                                topLeft = Offset(x, centerY),
                                size = Size(barWidth, barH),
                                cornerRadius = CornerRadius(2f, 2f)
                            )
                        }
                    }

                    // Plot goal highlight markers on scoring minutes
                    goalEvents.forEach { goal ->
                        val gx = ((goal.minute.coerceIn(1, totalMinutes) - 0.5f) / totalMinutes.toFloat()) * size.width
                        val gy = if (goal.teamSide == MatchTeamSide.HOME) 9f else size.height - 9f
                        drawCircle(
                            color = if (goal.teamSide == MatchTeamSide.HOME) ArenaFloodlightLime else AmberGold,
                            radius = 5.5f,
                            center = Offset(gx, gy)
                        )
                        drawCircle(
                            color = DarkGraphite,
                            radius = 2.4f,
                            center = Offset(gx, gy)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("1'", color = ChampagneMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text("HT (45')", color = Champagne, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                if (goalEvents.isNotEmpty()) {
                    Text(
                        text = goalEvents.joinToString(" • ") { "⚽ ${it.minute}'" },
                        color = KickoffPeachCream,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text("90'", color = ChampagneMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
internal fun ShotMapVisualCard(
    dossier: MatchFullDossier
) {
    val shots = dossier.shotMap
    val homeShots = remember(shots) { shots.filter { it.teamSide == MatchTeamSide.HOME } }
    val awayShots = remember(shots) { shots.filter { it.teamSide == MatchTeamSide.AWAY } }
    val homeXg = remember(homeShots) { homeShots.sumOf { it.xg } }
    val awayXg = remember(awayShots) { awayShots.sumOf { it.xg } }
    val homeXgot = remember(homeShots) { homeShots.sumOf { it.xgot } }
    val awayXgot = remember(awayShots) { awayShots.sumOf { it.xgot } }

    Card(
        colors = CardDefaults.cardColors(containerColor = MetalCharcoal),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.9.dp, ArenaPitchGreen.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("match_shotmap_canvas")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = dossier.match.homeTeamName.uppercase(),
                        color = ArenaFloodlightLime,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "xG ${String.format(Locale.US, "%.2f", homeXg)} • xGOT ${String.format(Locale.US, "%.2f", homeXgot)}",
                        color = Champagne,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        style = TabularNumeralTextStyle
                    )
                }

                Text(
                    text = "🎯 HALF-PITCH SHOT MAP",
                    color = PaleGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = dossier.match.awayTeamName.uppercase(),
                        color = AmberGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "xG ${String.format(Locale.US, "%.2f", awayXg)} • xGOT ${String.format(Locale.US, "%.2f", awayXgot)}",
                        color = Champagne,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        style = TabularNumeralTextStyle
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Half-Pitch Penalty Box Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0B291C))
                    .border(0.9.dp, ArenaPitchGreen.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val lineStroke = Stroke(width = 1.6f)
                    val lineColor = Color.White.copy(alpha = 0.28f)

                    // Goal mouth at top center
                    val goalWidth = size.width * 0.22f
                    val goalLeft = (size.width - goalWidth) / 2f
                    drawRect(
                        color = Color.White.copy(alpha = 0.65f),
                        topLeft = Offset(goalLeft, 0f),
                        size = Size(goalWidth, 6f),
                        style = lineStroke
                    )

                    // 6-yard box
                    val sixYardW = size.width * 0.38f
                    val sixYardH = size.height * 0.22f
                    drawRect(
                        color = lineColor,
                        topLeft = Offset((size.width - sixYardW) / 2f, 0f),
                        size = Size(sixYardW, sixYardH),
                        style = lineStroke
                    )

                    // 18-yard penalty box
                    val penBoxW = size.width * 0.68f
                    val penBoxH = size.height * 0.56f
                    drawRect(
                        color = lineColor,
                        topLeft = Offset((size.width - penBoxW) / 2f, 0f),
                        size = Size(penBoxW, penBoxH),
                        style = lineStroke
                    )

                    // Penalty spot
                    drawCircle(
                        color = lineColor,
                        radius = 3f,
                        center = Offset(size.width / 2f, size.height * 0.38f)
                    )

                    // Plot each shot sized proportionally to xG
                    shots.forEach { shot ->
                        val cx = (shot.normalizedX.coerceIn(0.08f, 0.92f)) * size.width
                        val cy = (shot.normalizedY.coerceIn(0.08f, 0.90f)) * size.height
                        val baseRadius = (5.5f + (shot.xg.toFloat().coerceIn(0.04f, 0.85f) * 12f))
                        val shotColor = when (shot.outcome) {
                            ShotOutcome.GOAL -> AmberGold
                            ShotOutcome.ON_TARGET_SAVED -> ArenaFloodlightLime
                            ShotOutcome.BLOCKED -> Color(0xFF94A3B8)
                            ShotOutcome.OFF_TARGET -> Color(0xFF64748B)
                        }
                        drawCircle(
                            color = shotColor.copy(alpha = if (shot.outcome == ShotOutcome.GOAL) 0.95f else 0.78f),
                            radius = baseRadius,
                            center = Offset(cx, cy)
                        )
                        drawCircle(
                            color = if (shot.teamSide == MatchTeamSide.HOME) KickoffTropicalTeal else Color.White,
                            radius = baseRadius,
                            center = Offset(cx, cy),
                            style = Stroke(width = 1.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Shot Map Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ShotLegendItem(color = AmberGold, label = "Goal (${shots.count { it.outcome == ShotOutcome.GOAL }})")
                    ShotLegendItem(color = ArenaFloodlightLime, label = "On Target (${shots.count { it.outcome == ShotOutcome.ON_TARGET_SAVED }})")
                    ShotLegendItem(color = Color(0xFF94A3B8), label = "Off/Blocked (${shots.count { it.outcome == ShotOutcome.OFF_TARGET || it.outcome == ShotOutcome.BLOCKED }})")
                }
                Text(
                    text = "Size = xG Value",
                    color = ChampagneMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ShotLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = Champagne,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
internal fun MatchBuzzInsightsCard(
    insights: List<String>
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MetalCharcoal),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.9.dp, AmberGold.copy(alpha = 0.55f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("match_buzz_insights_card")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "🔥 365SCORES MATCH BUZZ & H2H INSIGHTS",
                color = AmberGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
            insights.forEach { insight ->
                Surface(
                    color = DarkGraphite,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.6.dp, ChampagneSubtleBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = insight,
                        color = PaleGold,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    )
                }
            }
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
// TAB 4: H2H & STANDINGS TABLE (WITH FLASHSCORE LIVE TABLE & 365SCORES BUZZ)
// =============================================================================
@Composable
private fun MatchH2HAndStandingsTabContent(
    dossier: MatchFullDossier
) {
    val hasLiveOrPlayedScore = dossier.match.homeScore != null && dossier.match.awayScore != null
    var showLiveTable by remember { mutableStateOf(hasLiveOrPlayedScore) }
    val enricher = remember { MatchCenterEnricher(okhttp3.OkHttpClient()) }
    val displayedStandings = remember(dossier.standingsTable, dossier.match, showLiveTable) {
        if (showLiveTable && hasLiveOrPlayedScore) {
            enricher.computeLiveInPlayStandings(dossier.standingsTable, dossier.match)
        } else {
            dossier.standingsTable
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 0. 365Scores Pre-Match Buzz & H2H Streaks Card
        if (dossier.matchBuzzInsights.isNotEmpty()) {
            item {
                MatchBuzzInsightsCard(insights = dossier.matchBuzzInsights)
            }
        }

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
                    if (dossier.h2hMeetings.isEmpty()) {
                        Text(
                            text = "No historical head-to-head meetings available from official feed for this fixture.",
                            color = ChampagneMuted,
                            fontSize = 11.5.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    } else {
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
        }

        // 3. Competition Standings Table (with Official Table | 🔴 Live Table toggle)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MetalCharcoal),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(0.9.dp, ArenaPitchGreen.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${dossier.match.tournamentName.uppercase()} STANDINGS",
                            color = ArenaFloodlightLime,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(1f)
                        )

                        // Flashscore [ Official Table | 🔴 Live Table ] Toggle Pill
                        Surface(
                            color = DarkGraphite,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(
                                0.8.dp,
                                if (showLiveTable) LiveCrimson.copy(alpha = 0.75f) else ChampagneSubtleBorder
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { showLiveTable = !showLiveTable }
                                .testTag("standings_live_table_toggle")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (showLiveTable) "🔴 Live Table" else "Official Table",
                                    color = if (showLiveTable) KickoffPeachCream else Champagne,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (displayedStandings.isEmpty()) {
                        Text(
                            text = "Official live competition standings table is not available from the upstream feed right now.",
                            color = ChampagneMuted,
                            fontSize = 11.5.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    } else {
                        // Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("#", color = ChampagneMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp))
                            Text("TEAM", color = ChampagneMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("P", color = ChampagneMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                            Text("W-D-L", color = ChampagneMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(46.dp), textAlign = TextAlign.Center)
                            Text("GD", color = ChampagneMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
                            Text("PTS", color = AmberGold, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.width(30.dp), textAlign = TextAlign.End)
                        }

                        HorizontalDivider(color = ChampagneSubtleBorder, thickness = 0.6.dp)

                        displayedStandings.forEach { row ->
                            StandingsTableRow(row = row, showLiveDelta = showLiveTable)
                        }
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
private fun StandingsTableRow(
    row: LeagueStandingRow,
    showLiveDelta: Boolean = false
) {
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(36.dp)
        ) {
            Text(
                text = row.rank.toString(),
                color = if (row.rank <= 4) ArenaFloodlightLime else Champagne,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
            if (showLiveDelta && row.liveRankDelta != 0) {
                Spacer(modifier = Modifier.width(2.dp))
                val isUp = row.liveRankDelta > 0
                Text(
                    text = if (isUp) "▲+${row.liveRankDelta}" else "▼${row.liveRankDelta}",
                    color = if (isUp) Color(0xFF10B981) else Color(0xFFEF4444),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
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
// TAB 5: USER'S ZONE OFFICIAL TV CHANNEL GUIDE (1-TAP WATCH VIA FAVORITES)
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${dossier.countryZone.flagEmoji} ${dossier.countryZone.countryName.uppercase()} • OFFICIAL TV GUIDE",
                            color = ArenaFloodlightLime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Surface(
                            color = DarkGraphite,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.7.dp, AmberGold.copy(alpha = 0.7f))
                        ) {
                            Text(
                                text = dossier.countryZone.getUtcOffsetBadge(),
                                color = AmberGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap any channel below to launch it directly from your saved Favorites list. Recommended IPTV bouquet prefixes: ${dossier.countryZone.iptvCategoryPrefixes.joinToString(" • ")}.",
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
            .tvFocusableControl(
                accentColor = ArenaFloodlightLime,
                shape = RoundedCornerShape(14.dp),
                onClick = onWatchClicked
            )
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
