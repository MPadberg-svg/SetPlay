package com.example.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CompBotolaCrimson
import com.example.ui.theme.CompBundesligaRed
import com.example.ui.theme.CompCafEmerald
import com.example.ui.theme.CompEplViolet
import com.example.ui.theme.CompIntlGold
import com.example.ui.theme.CompLaLigaCoral
import com.example.ui.theme.CompLigue1Gold
import com.example.ui.theme.CompSaudiJade
import com.example.ui.theme.CompSerieACyan
import com.example.ui.theme.CompUclBlue
import com.example.ui.theme.CompUelOrange
import com.example.ui.theme.KickoffCanopyElevated
import com.example.ui.theme.KickoffFloodlightVolt
import com.example.ui.theme.KickoffFloodlightWhite
import com.example.ui.theme.KickoffLiveCrimson
import com.example.ui.theme.KickoffMistSilver
import com.example.ui.theme.KickoffPitchEmerald
import com.example.ui.theme.KickoffStadiumForest
import com.example.ui.theme.KickoffTrophyGold
import com.example.ui.theme.ProviderAdSportsBorder
import com.example.ui.theme.ProviderAdSportsSurface
import com.example.ui.theme.ProviderAdSportsText
import com.example.ui.theme.ProviderArryadiaBorder
import com.example.ui.theme.ProviderArryadiaSurface
import com.example.ui.theme.ProviderArryadiaText
import com.example.ui.theme.ProviderBeinBorder
import com.example.ui.theme.ProviderBeinSurface
import com.example.ui.theme.ProviderBeinText
import com.example.ui.theme.ProviderCanalDaznBorder
import com.example.ui.theme.ProviderCanalDaznSurface
import com.example.ui.theme.ProviderCanalDaznText
import com.example.ui.theme.ProviderSkyTntBorder
import com.example.ui.theme.ProviderSkyTntSurface
import com.example.ui.theme.ProviderSkyTntText
import com.example.ui.theme.ProviderSscBorder
import com.example.ui.theme.ProviderSscSurface
import com.example.ui.theme.ProviderSscText
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * User display preferences for the Kickoff Match Center feed.
 */
data class KickoffUiPreferences(
    val hideLiveAndFinalScores: Boolean = false,
    val compactCardDensity: Boolean = false
)

/**
 * Broadcast Provider Identity Badge styling resolved from the channel or provider name.
 */
data class BroadcastProviderBadgeStyle(
    val surfaceColor: Color,
    val borderColor: Color,
    val textColor: Color,
    val providerTag: String
)

/**
 * Broadcaster visual identity tokens for match cards, TV grid cards, and studio docks.
 */
data class BroadcasterVisualIdentity(
    val containerColor: Color,
    val borderColor: Color,
    val textColor: Color,
    val providerShortTag: String
)

/**
 * Resolves the Broadcast Provider Identity colors (`Provider*Surface`, `Provider*Border`, `Provider*Text`)
 * for Match Cards, Match Center Studio Dock, and Country Zone TV Guide pills.
 */
fun resolveBroadcastProviderBadgeStyle(channelOrProviderName: String): BroadcastProviderBadgeStyle {
    val lower = channelOrProviderName.lowercase(Locale.ROOT).trim()
    return when {
        lower.contains("arryadia") || lower.contains("snrt") || lower.contains("tamazight") || lower.contains("maghribia") || lower.contains("al aoula") ->
            BroadcastProviderBadgeStyle(
                surfaceColor = ProviderArryadiaSurface,
                borderColor = ProviderArryadiaBorder,
                textColor = ProviderArryadiaText,
                providerTag = "ARRYADIA"
            )
        lower.contains("ssc") || lower.contains("shahid") || lower.contains("saudi") ->
            BroadcastProviderBadgeStyle(
                surfaceColor = ProviderSscSurface,
                borderColor = ProviderSscBorder,
                textColor = ProviderSscText,
                providerTag = "SSC"
            )
        lower.contains("ad sport") || lower.contains("abu dhabi") || lower.contains("starz") || lower.contains("alkass") || lower.contains("al kass") || lower.contains("dubai") || lower.contains("on time") || lower.contains("ontime") ->
            BroadcastProviderBadgeStyle(
                surfaceColor = ProviderAdSportsSurface,
                borderColor = ProviderAdSportsBorder,
                textColor = ProviderAdSportsText,
                providerTag = "AD SPORTS"
            )
        lower.contains("canal") || lower.contains("dazn") || lower.contains("rmc") || lower.contains("movistar") || lower.contains("sport tv") || lower.contains("ziggo") ->
            BroadcastProviderBadgeStyle(
                surfaceColor = ProviderCanalDaznSurface,
                borderColor = ProviderCanalDaznBorder,
                textColor = ProviderCanalDaznText,
                providerTag = if (lower.contains("dazn")) "DAZN" else "CANAL+"
            )
        lower.contains("sky") || lower.contains("tnt") || lower.contains("bt sport") || lower.contains("premier sport") || lower.contains("cbs") || lower.contains("paramount") || lower.contains("espn") ->
            BroadcastProviderBadgeStyle(
                surfaceColor = ProviderSkyTntSurface,
                borderColor = ProviderSkyTntBorder,
                textColor = ProviderSkyTntText,
                providerTag = if (lower.contains("tnt")) "TNT SPORTS" else "SKY SPORTS"
            )
        lower.contains("bein") || lower.contains("be in") || lower.contains("tod") ->
            BroadcastProviderBadgeStyle(
                surfaceColor = ProviderBeinSurface,
                borderColor = ProviderBeinBorder,
                textColor = ProviderBeinText,
                providerTag = "beIN SPORTS"
            )
        else ->
            BroadcastProviderBadgeStyle(
                surfaceColor = KickoffStadiumForest,
                borderColor = KickoffPitchEmerald,
                textColor = KickoffFloodlightWhite,
                providerTag = "LIVE TV"
            )
    }
}

/**
 * Resolves the provider-branded [BroadcasterVisualIdentity] (`containerColor`, `borderColor`, `textColor`, `providerShortTag`).
 */
fun resolveBroadcasterVisualIdentity(channelOrProviderName: String): BroadcasterVisualIdentity {
    val style = resolveBroadcastProviderBadgeStyle(channelOrProviderName)
    val shortTag = when (style.providerTag) {
        "beIN SPORTS" -> "beIN"
        "ARRYADIA" -> "SNRT"
        "SSC" -> "SSC"
        "AD SPORTS" -> "AD"
        "DAZN" -> "DAZN"
        "CANAL+" -> "CANAL+"
        "TNT SPORTS" -> "TNT"
        "SKY SPORTS" -> "SKY"
        else -> "TV"
    }
    return BroadcasterVisualIdentity(
        containerColor = style.surfaceColor,
        borderColor = style.borderColor,
        textColor = style.textColor,
        providerShortTag = shortTag
    )
}

/**
 * Resolves the signature broadcast accent color for each supported football competition.
 */
fun resolveCompetitionAccentColor(tournamentId: String): Color {
    return when (tournamentId.lowercase(Locale.ROOT).trim()) {
        "ucl", "uefa_cl", "uefa_champions_league", "champions_league" -> CompUclBlue
        "uel", "uefa_el", "uecl", "europa_league", "conference_league" -> CompUelOrange
        "epl", "premier_league", "fa_cup", "efl_cup" -> CompEplViolet
        "laliga", "la_liga", "copa_del_rey" -> CompLaLigaCoral
        "seriea", "serie_a", "coppa_italia" -> CompSerieACyan
        "bundesliga", "dfb_pokal" -> CompBundesligaRed
        "ligue1", "ligue_1", "coupe_de_france" -> CompLigue1Gold
        "caf_cl", "caf_cc", "caf_confed", "caf_champions_league", "caf_confederation_cup" -> CompCafEmerald
        "botola_pro", "botola", "throne_cup" -> CompBotolaCrimson
        "saudi_pro", "spl", "egyptian_premier" -> CompSaudiJade
        "afcon", "fifa_wc", "world_cup", "uefa_nations", "euro", "copa_america", "intl_friendlies", "intl_official", "intl_friendly" -> CompIntlGold
        else -> KickoffPitchEmerald
    }
}

/**
 * 1. Bespoke Live Broadcast Beacon Vector Icon (replaces generic `🔥` or plain dot).
 * Renders a concentric stadium broadcast wave emitter around a luminous core.
 */
@Composable
fun KickoffLiveBroadcastBeaconIcon(
    modifier: Modifier = Modifier.size(14.dp),
    color: Color = KickoffLiveCrimson,
    pulseFraction: Float = 1f
) {
    Canvas(modifier = modifier) {
        val c = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f
        val strokeW = (size.minDimension * 0.11f).coerceAtLeast(1.2f)

        // Outer broadcast wave halo
        drawCircle(
            color = color.copy(alpha = (0.28f * pulseFraction).coerceIn(0.08f, 0.45f)),
            radius = maxR * (0.72f + 0.25f * pulseFraction),
            center = c
        )
        // Left & right broadcast signal arcs
        val arcR = maxR * 0.78f
        val arcRect = Rect(c.x - arcR, c.y - arcR, c.x + arcR, c.y + arcR)
        drawArc(
            color = color.copy(alpha = 0.85f),
            startAngle = -42f,
            sweepAngle = 84f,
            useCenter = false,
            topLeft = arcRect.topLeft,
            size = arcRect.size,
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )
        drawArc(
            color = color.copy(alpha = 0.85f),
            startAngle = 138f,
            sweepAngle = 84f,
            useCenter = false,
            topLeft = arcRect.topLeft,
            size = arcRect.size,
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )
        // Core signal transmitter dot
        drawCircle(
            color = color,
            radius = maxR * 0.34f,
            center = c
        )
    }
}

/**
 * 2. Bespoke 2D Tactical Football Pitch Icon (replaces generic `🏟️` emoji).
 * Renders touchlines, halfway line, center circle, and both penalty areas.
 */
@Composable
fun KickoffTacticalPitchIcon(
    modifier: Modifier = Modifier.size(16.dp),
    tint: Color = KickoffPitchEmerald
) {
    Canvas(modifier = modifier) {
        val strokeW = (size.minDimension * 0.09f).coerceAtLeast(1.1f)
        val pad = strokeW
        val w = (size.width - pad * 2f).coerceAtLeast(2f)
        val h = (size.height - pad * 2f).coerceAtLeast(2f)
        val topLeft = Offset(pad, pad)

        // Outer pitch boundary
        drawRoundRect(
            color = tint,
            topLeft = topLeft,
            size = Size(w, h),
            cornerRadius = CornerRadius(size.minDimension * 0.14f, size.minDimension * 0.14f),
            style = Stroke(width = strokeW)
        )
        // Halfway line
        drawLine(
            color = tint.copy(alpha = 0.85f),
            start = Offset(size.width / 2f, pad),
            end = Offset(size.width / 2f, pad + h),
            strokeWidth = strokeW * 0.85f
        )
        // Center circle
        drawCircle(
            color = tint.copy(alpha = 0.9f),
            radius = minOf(w, h) * 0.20f,
            center = Offset(size.width / 2f, size.height / 2f),
            style = Stroke(width = strokeW * 0.85f)
        )
        // Left & right penalty boxes
        val boxW = w * 0.18f
        val boxH = h * 0.48f
        val boxTop = pad + (h - boxH) / 2f
        drawRect(
            color = tint.copy(alpha = 0.8f),
            topLeft = Offset(pad, boxTop),
            size = Size(boxW, boxH),
            style = Stroke(width = strokeW * 0.8f)
        )
        drawRect(
            color = tint.copy(alpha = 0.8f),
            topLeft = Offset(pad + w - boxW, boxTop),
            size = Size(boxW, boxH),
            style = Stroke(width = strokeW * 0.8f)
        )
    }
}

/**
 * 3. Bespoke Championship Trophy Cup Vector Icon (replaces generic `🏆` emoji).
 */
@Composable
fun KickoffChampionshipTrophyIcon(
    modifier: Modifier = Modifier.size(16.dp),
    tint: Color = KickoffTrophyGold
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeW = (size.minDimension * 0.095f).coerceAtLeast(1.2f)

        // Cup chalice path
        val cupPath = Path().apply {
            moveTo(w * 0.26f, h * 0.16f)
            lineTo(w * 0.74f, h * 0.16f)
            lineTo(w * 0.68f, h * 0.48f)
            cubicTo(
                w * 0.65f, h * 0.62f,
                w * 0.35f, h * 0.62f,
                w * 0.32f, h * 0.48f
            )
            close()
        }
        drawPath(
            path = cupPath,
            color = tint,
            style = Stroke(width = strokeW, join = StrokeJoin.Round)
        )
        // Handles
        val leftHandle = Path().apply {
            moveTo(w * 0.26f, h * 0.22f)
            cubicTo(w * 0.10f, h * 0.22f, w * 0.12f, h * 0.44f, w * 0.30f, h * 0.44f)
        }
        val rightHandle = Path().apply {
            moveTo(w * 0.74f, h * 0.22f)
            cubicTo(w * 0.90f, h * 0.22f, w * 0.88f, h * 0.44f, w * 0.70f, h * 0.44f)
        }
        drawPath(leftHandle, color = tint, style = Stroke(width = strokeW * 0.85f, cap = StrokeCap.Round))
        drawPath(rightHandle, color = tint, style = Stroke(width = strokeW * 0.85f, cap = StrokeCap.Round))

        // Stem & pedestal base
        drawLine(
            color = tint,
            start = Offset(w * 0.50f, h * 0.58f),
            end = Offset(w * 0.50f, h * 0.80f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.28f, h * 0.84f),
            end = Offset(w * 0.72f, h * 0.84f),
            strokeWidth = strokeW * 1.15f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * 4. Bespoke Match Goal Ball Vector Icon (replaces generic `⚽` emoji).
 * Renders a crisp geometric football sphere with central pentagon and seam struts.
 */
@Composable
fun KickoffMatchGoalBallIcon(
    modifier: Modifier = Modifier.size(15.dp),
    tint: Color = KickoffFloodlightWhite,
    accentColor: Color = KickoffPitchEmerald
) {
    Canvas(modifier = modifier) {
        val c = Offset(size.width / 2f, size.height / 2f)
        val r = (size.minDimension / 2f) * 0.86f
        val strokeW = (size.minDimension * 0.09f).coerceAtLeast(1.1f)

        // Outer ball rim
        drawCircle(
            color = tint,
            radius = r,
            center = c,
            style = Stroke(width = strokeW)
        )

        // Center pentagon + 5 radial struts
        val pentagonR = r * 0.42f
        val pentagonPath = Path()
        for (i in 0 until 5) {
            val angleRad = Math.toRadians((-90.0 + i * 72.0))
            val px = c.x + (pentagonR * cos(angleRad)).toFloat()
            val py = c.y + (pentagonR * sin(angleRad)).toFloat()
            val ox = c.x + (r * 0.95f * cos(angleRad)).toFloat()
            val oy = c.y + (r * 0.95f * sin(angleRad)).toFloat()
            if (i == 0) pentagonPath.moveTo(px, py) else pentagonPath.lineTo(px, py)
            drawLine(
                color = tint.copy(alpha = 0.85f),
                start = Offset(px, py),
                end = Offset(ox, oy),
                strokeWidth = strokeW * 0.8f
            )
        }
        pentagonPath.close()
        drawPath(path = pentagonPath, color = accentColor)
    }
}

/**
 * 5. Bespoke Referee Booking Card Vector Icon (Yellow Card `#F59E0B`, Red Card `#FF2E54`, or Second Yellow + Red).
 */
@Composable
fun KickoffBookingCardIcon(
    isRedCard: Boolean,
    isSecondYellow: Boolean = false,
    modifier: Modifier = Modifier.size(14.dp)
) {
    Canvas(modifier = modifier) {
        val cardW = size.width * 0.56f
        val cardH = size.height * 0.78f
        val corner = CornerRadius(size.minDimension * 0.12f, size.minDimension * 0.12f)

        if (isSecondYellow) {
            // Back yellow card tilted left
            rotate(degrees = -10f, pivot = center) {
                drawRoundRect(
                    color = KickoffTrophyGold,
                    topLeft = Offset(size.width * 0.14f, size.height * 0.14f),
                    size = Size(cardW * 0.88f, cardH * 0.88f),
                    cornerRadius = corner
                )
            }
            // Front red card tilted right
            rotate(degrees = 8f, pivot = center) {
                drawRoundRect(
                    color = KickoffLiveCrimson,
                    topLeft = Offset(size.width * 0.32f, size.height * 0.10f),
                    size = Size(cardW * 0.88f, cardH * 0.88f),
                    cornerRadius = corner
                )
            }
        } else {
            val fill = if (isRedCard) KickoffLiveCrimson else KickoffTrophyGold
            rotate(degrees = -7f, pivot = center) {
                drawRoundRect(
                    color = fill,
                    topLeft = Offset((size.width - cardW) / 2f, (size.height - cardH) / 2f),
                    size = Size(cardW, cardH),
                    cornerRadius = corner
                )
                drawRoundRect(
                    color = KickoffFloodlightWhite.copy(alpha = 0.35f),
                    topLeft = Offset((size.width - cardW) / 2f, (size.height - cardH) / 2f),
                    size = Size(cardW, cardH),
                    cornerRadius = corner,
                    style = Stroke(width = 0.8.dp.toPx())
                )
            }
        }
    }
}

/**
 * 6. Bespoke Substitution Dual-Arrow Vector Icon (Emerald In `#10B981` + Crimson Out `#FF2E54`).
 */
@Composable
fun KickoffSubstitutionArrowsIcon(
    modifier: Modifier = Modifier.size(15.dp),
    inColor: Color = KickoffPitchEmerald,
    outColor: Color = KickoffLiveCrimson
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeW = (size.minDimension * 0.11f).coerceAtLeast(1.3f)

        // Top right-pointing arrow (Player In — Pitch Emerald)
        val topY = h * 0.32f
        drawLine(
            color = inColor,
            start = Offset(w * 0.16f, topY),
            end = Offset(w * 0.82f, topY),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
        drawLine(
            color = inColor,
            start = Offset(w * 0.62f, topY - h * 0.16f),
            end = Offset(w * 0.84f, topY),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
        drawLine(
            color = inColor,
            start = Offset(w * 0.62f, topY + h * 0.16f),
            end = Offset(w * 0.84f, topY),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )

        // Bottom left-pointing arrow (Player Out — Live Crimson)
        val botY = h * 0.68f
        drawLine(
            color = outColor,
            start = Offset(w * 0.84f, botY),
            end = Offset(w * 0.18f, botY),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
        drawLine(
            color = outColor,
            start = Offset(w * 0.38f, botY - h * 0.16f),
            end = Offset(w * 0.16f, botY),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
        drawLine(
            color = outColor,
            start = Offset(w * 0.38f, botY + h * 0.16f),
            end = Offset(w * 0.16f, botY),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
    }
}

/**
 * 7. Bespoke VAR Review Monitor Vector Icon.
 */
@Composable
fun KickoffVarMonitorIcon(
    modifier: Modifier = Modifier.size(16.dp),
    tint: Color = KickoffFloodlightVolt
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeW = (size.minDimension * 0.10f).coerceAtLeast(1.2f)

        // Monitor frame
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.10f, h * 0.16f),
            size = Size(w * 0.80f, h * 0.56f),
            cornerRadius = CornerRadius(w * 0.10f, w * 0.10f),
            style = Stroke(width = strokeW)
        )
        // Inner VAR frame brackets
        drawLine(
            color = tint,
            start = Offset(w * 0.26f, h * 0.34f),
            end = Offset(w * 0.38f, h * 0.54f),
            strokeWidth = strokeW * 0.9f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.50f, h * 0.34f),
            end = Offset(w * 0.38f, h * 0.54f),
            strokeWidth = strokeW * 0.9f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.66f, h * 0.34f),
            end = Offset(w * 0.66f, h * 0.54f),
            strokeWidth = strokeW * 0.9f,
            cap = StrokeCap.Round
        )
        // Stand
        drawLine(
            color = tint,
            start = Offset(w * 0.50f, h * 0.72f),
            end = Offset(w * 0.50f, h * 0.86f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.30f, h * 0.86f),
            end = Offset(w * 0.70f, h * 0.86f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
    }
}

/**
 * 8. Bespoke TV Broadcast Screen & Signal Icon (replaces generic `📺` emoji).
 */
@Composable
fun KickoffTvBroadcastIcon(
    modifier: Modifier = Modifier.size(14.dp),
    tint: Color = KickoffPitchEmerald
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeW = (size.minDimension * 0.10f).coerceAtLeast(1.1f)

        // Widescreen display frame
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.08f, h * 0.20f),
            size = Size(w * 0.84f, h * 0.56f),
            cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
            style = Stroke(width = strokeW)
        )
        // Play triangle inside screen
        val playPath = Path().apply {
            moveTo(w * 0.42f, h * 0.36f)
            lineTo(w * 0.62f, h * 0.48f)
            lineTo(w * 0.42f, h * 0.60f)
            close()
        }
        drawPath(path = playPath, color = tint)

        // Base stand
        drawLine(
            color = tint.copy(alpha = 0.85f),
            start = Offset(w * 0.28f, h * 0.86f),
            end = Offset(w * 0.72f, h * 0.86f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
    }
}

/**
 * 9. Bespoke Geometric Club Shield Crest Placeholder when a team badge is loading or unavailable.
 */
@Composable
fun KickoffCrestShieldPlaceholder(
    teamName: String,
    size: Dp = 32.dp,
    primaryColor: Color = KickoffPitchEmerald,
    containerColor: Color = KickoffCanopyElevated,
    modifier: Modifier = Modifier
) {
    val initials = teamName
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.take(1).uppercase(Locale.ROOT) }
        .ifBlank { "FC" }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val shieldPath = Path().apply {
                moveTo(w * 0.50f, h * 0.06f)
                lineTo(w * 0.88f, h * 0.20f)
                lineTo(w * 0.84f, h * 0.58f)
                cubicTo(
                    w * 0.80f, h * 0.78f,
                    w * 0.64f, h * 0.90f,
                    w * 0.50f, h * 0.95f
                )
                cubicTo(
                    w * 0.36f, h * 0.90f,
                    w * 0.20f, h * 0.78f,
                    w * 0.16f, h * 0.58f
                )
                lineTo(w * 0.12f, h * 0.20f)
                close()
            }
            drawPath(
                path = shieldPath,
                brush = Brush.verticalGradient(
                    listOf(
                        containerColor,
                        primaryColor.copy(alpha = 0.28f)
                    )
                )
            )
            drawPath(
                path = shieldPath,
                color = primaryColor.copy(alpha = 0.65f),
                style = Stroke(width = (this.size.minDimension * 0.055f).coerceAtLeast(1.1f))
            )
        }
        Text(
            text = initials,
            color = KickoffFloodlightWhite,
            fontSize = (size.value * 0.31f).coerceIn(8.5f, 13f).sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.4.sp
        )
    }
}

/**
 * 10. Bespoke Referee Whistle / Match Official Vector Icon.
 */
@Composable
fun KickoffRefereeWhistleIcon(
    modifier: Modifier = Modifier.size(14.dp),
    tint: Color = KickoffMistSilver
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeW = (size.minDimension * 0.10f).coerceAtLeast(1.1f)

        // Whistle chamber circle
        drawCircle(
            color = tint,
            radius = w * 0.28f,
            center = Offset(w * 0.38f, h * 0.58f),
            style = Stroke(width = strokeW)
        )
        // Whistle mouthpiece stem
        val stemPath = Path().apply {
            moveTo(w * 0.48f, h * 0.32f)
            lineTo(w * 0.88f, h * 0.26f)
            lineTo(w * 0.88f, h * 0.44f)
            lineTo(w * 0.62f, h * 0.52f)
        }
        drawPath(
            path = stemPath,
            color = tint,
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun KickoffPulseBeacon(
    color: Color = KickoffLiveCrimson,
    size: Dp = 14.dp,
    modifier: Modifier = Modifier
) {
    KickoffLiveBroadcastBeaconIcon(
        modifier = modifier.size(size),
        color = color
    )
}

@Composable
fun KickoffSoccerCardIcon(
    color: Color,
    isSecondYellow: Boolean = false,
    size: Dp = 14.dp,
    modifier: Modifier = Modifier
) {
    KickoffBookingCardIcon(
        isRedCard = color == KickoffLiveCrimson,
        isSecondYellow = isSecondYellow,
        modifier = modifier.size(size)
    )
}

@Composable
fun KickoffSubDualArrowIcon(
    inColor: Color = KickoffPitchEmerald,
    outColor: Color = KickoffLiveCrimson,
    size: Dp = 15.dp,
    modifier: Modifier = Modifier
) {
    KickoffSubstitutionArrowsIcon(
        modifier = modifier.size(size),
        inColor = inColor,
        outColor = outColor
    )
}

@Composable
fun KickoffBroadcastWaveIcon(
    tint: Color = KickoffPitchEmerald,
    size: Dp = 14.dp,
    modifier: Modifier = Modifier
) {
    KickoffTvBroadcastIcon(
        modifier = modifier.size(size),
        tint = tint
    )
}

