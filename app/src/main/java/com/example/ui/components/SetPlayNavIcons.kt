package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.AppScreen
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Unified bespoke Compose Canvas navigation icon dispatcher for SetPlay's 5 primary destinations.
 *
 * All icons share an identical `1.8.dp` stroke weight, `StrokeCap.Round`, `StrokeJoin.Round`,
 * dual-tone active interior illumination (`accentTint`), and subtle spring micro-motion so they
 * render razor-sharp at both `20.dp`–`22.dp` (Mobile Bottom Dock) and `24.dp` (Android TV Rail).
 */
@Composable
fun SetPlaySectionNavIcon(
    screen: AppScreen,
    isSelected: Boolean,
    tint: Color,
    accentTint: Color,
    modifier: Modifier = Modifier
) {
    when (screen) {
        AppScreen.HOME -> SetPlayKickoffNavIcon(
            isSelected = isSelected,
            tint = tint,
            accentTint = accentTint,
            modifier = modifier
        )
        AppScreen.LIVE_TV -> SetPlayLiveTvNavIcon(
            isSelected = isSelected,
            tint = tint,
            accentTint = accentTint,
            modifier = modifier
        )
        AppScreen.MOVIES -> SetPlayMoviesNavIcon(
            isSelected = isSelected,
            tint = tint,
            accentTint = accentTint,
            modifier = modifier
        )
        AppScreen.SERIES -> SetPlaySeriesNavIcon(
            isSelected = isSelected,
            tint = tint,
            accentTint = accentTint,
            modifier = modifier
        )
        AppScreen.FAVORITES -> SetPlayFavoritesNavIcon(
            isSelected = isSelected,
            tint = tint,
            accentTint = accentTint,
            modifier = modifier
        )
        else -> SetPlayKickoffNavIcon(
            isSelected = isSelected,
            tint = tint,
            accentTint = accentTint,
            modifier = modifier
        )
    }
}

/**
 * 1. KickOff — Geometric Stadium Football:
 * Crisp outer circle, central pentagon core (softly filled with `accentTint` when active),
 * and 5 radiating curved seam arcs connecting the pentagon vertices to the outer rim.
 * Micro-motion: smooth `90f` degree spring rotation settle on selection.
 */
@Composable
fun SetPlayKickoffNavIcon(
    isSelected: Boolean,
    tint: Color,
    accentTint: Color,
    modifier: Modifier = Modifier.size(22.dp)
) {
    val activeProgress by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "kickoff_icon_active"
    )
    val rotationDeg by animateFloatAsState(
        targetValue = if (isSelected) 90f else 0f,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "kickoff_icon_rotation"
    )

    Canvas(modifier = modifier) {
        val s = size.minDimension / 24f
        val cx = size.width / 2f
        val cy = size.height / 2f
        val center = Offset(cx, cy)
        val strokePx = 1.8.dp.toPx()
        val outerR = 9.1f * s
        val pentR = 3.55f * s

        val primaryStroke = Stroke(
            width = strokePx,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
        val seamStroke = Stroke(
            width = strokePx * 0.86f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        // Soft dual-tone interior fill when active
        if (activeProgress > 0.01f) {
            drawCircle(
                color = accentTint.copy(alpha = 0.16f * activeProgress),
                radius = outerR,
                center = center
            )
        }

        rotate(degrees = rotationDeg, pivot = center) {
            val pentVertices = List(5) { idx ->
                val angleRad = ((-90.0 + idx * 72.0) * PI / 180.0)
                Offset(
                    x = cx + (pentR * cos(angleRad)).toFloat(),
                    y = cy + (pentR * sin(angleRad)).toFloat()
                )
            }

            // Central pentagon core
            val pentagonPath = Path().apply {
                pentVertices.forEachIndexed { i, pt ->
                    if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                }
                close()
            }

            if (activeProgress > 0.01f) {
                drawPath(
                    path = pentagonPath,
                    color = accentTint.copy(alpha = 0.34f * activeProgress)
                )
            }
            drawPath(
                path = pentagonPath,
                color = tint,
                style = seamStroke
            )

            // 5 precision radiating seam arcs connecting pentagon vertices to the outer rim
            for (idx in 0 until 5) {
                val angleDeg = -90.0 + idx * 72.0
                val angleRad = angleDeg * PI / 180.0
                val startPt = pentVertices[idx]
                val endPt = Offset(
                    x = cx + (outerR * cos(angleRad)).toFloat(),
                    y = cy + (outerR * sin(angleRad)).toFloat()
                )
                // Slight spherical curvature control point offset perpendicular to radius
                val midR = (pentR + outerR) * 0.52f
                val curveAngleRad = (angleDeg + 7.5) * PI / 180.0
                val ctrlPt = Offset(
                    x = cx + (midR * cos(curveAngleRad)).toFloat(),
                    y = cy + (midR * sin(curveAngleRad)).toFloat()
                )

                val seamPath = Path().apply {
                    moveTo(startPt.x, startPt.y)
                    quadraticTo(ctrlPt.x, ctrlPt.y, endPt.x, endPt.y)
                }
                drawPath(
                    path = seamPath,
                    color = tint,
                    style = seamStroke
                )
            }
        }

        // Crisp outer perimeter ring
        drawCircle(
            color = tint,
            radius = outerR,
            center = center,
            style = primaryStroke
        )
    }
}

/**
 * 2. Live TV — Modern Frameless Broadcast Signal Display (zero rabbit-ear antennas):
 * Sleek rounded 16:10 widescreen display frame (`4.dp` radius), minimal floating stand line
 * underneath, crisp central live broadcast dot, and twin curved radio signal arcs.
 * Micro-motion: subtle `0.88f -> 1.0f` spring scale-in on the outer broadcast wave when active.
 */
@Composable
fun SetPlayLiveTvNavIcon(
    isSelected: Boolean,
    tint: Color,
    accentTint: Color,
    modifier: Modifier = Modifier.size(22.dp)
) {
    val activeProgress by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "livetv_icon_active"
    )
    val outerWaveScale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.88f,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "livetv_outer_wave_scale"
    )

    Canvas(modifier = modifier) {
        val s = size.minDimension / 24f
        val ox = (size.width - 24f * s) / 2f
        val oy = (size.height - 24f * s) / 2f
        val strokePx = 1.8.dp.toPx()

        val primaryStroke = Stroke(
            width = strokePx,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
        val waveStroke = Stroke(
            width = strokePx * 0.84f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        val screenLeft = ox + 2.4f * s
        val screenTop = oy + 4.4f * s
        val screenW = 19.2f * s
        val screenH = 12.6f * s
        val cornerPx = 4.dp.toPx().coerceAtMost(screenH * 0.34f)
        val cornerRadius = CornerRadius(cornerPx, cornerPx)

        // Soft dual-tone screen illumination when selected
        if (activeProgress > 0.01f) {
            drawRoundRect(
                color = accentTint.copy(alpha = 0.22f * activeProgress),
                topLeft = Offset(screenLeft, screenTop),
                size = Size(screenW, screenH),
                cornerRadius = cornerRadius
            )
        }

        // Sleek rounded 16:10 widescreen bezel
        drawRoundRect(
            color = tint,
            topLeft = Offset(screenLeft, screenTop),
            size = Size(screenW, screenH),
            cornerRadius = cornerRadius,
            style = primaryStroke
        )

        // Minimal floating pedestal stand line underneath
        val standY = oy + 20.1f * s
        drawLine(
            color = tint,
            start = Offset(ox + 8.3f * s, standY),
            end = Offset(ox + 15.7f * s, standY),
            strokeWidth = strokePx,
            cap = StrokeCap.Round
        )

        // Crisp central live broadcast dot
        val emitterCenter = Offset(ox + 12.0f * s, screenTop + screenH * 0.52f)
        val dotColor = if (isSelected) accentTint else tint
        drawCircle(
            color = dotColor,
            radius = 1.55f * s,
            center = emitterCenter
        )

        // Twin curved radio signal arcs emanating cleanly on left & right of the broadcast core
        val innerR = 3.55f * s
        val outerR = 5.75f * s

        // Inner pair of broadcast waves
        drawArc(
            color = tint,
            startAngle = -36f,
            sweepAngle = 72f,
            useCenter = false,
            topLeft = Offset(emitterCenter.x - innerR, emitterCenter.y - innerR),
            size = Size(innerR * 2f, innerR * 2f),
            style = waveStroke
        )
        drawArc(
            color = tint,
            startAngle = 144f,
            sweepAngle = 72f,
            useCenter = false,
            topLeft = Offset(emitterCenter.x - innerR, emitterCenter.y - innerR),
            size = Size(innerR * 2f, innerR * 2f),
            style = waveStroke
        )

        // Outer pair of broadcast waves with spring scale-in (`0.88f -> 1f`) when selected
        scale(scaleX = outerWaveScale, scaleY = outerWaveScale, pivot = emitterCenter) {
            val outerColor = if (isSelected) tint else tint.copy(alpha = 0.72f)
            drawArc(
                color = outerColor,
                startAngle = -42f,
                sweepAngle = 84f,
                useCenter = false,
                topLeft = Offset(emitterCenter.x - outerR, emitterCenter.y - outerR),
                size = Size(outerR * 2f, outerR * 2f),
                style = waveStroke
            )
            drawArc(
                color = outerColor,
                startAngle = 138f,
                sweepAngle = 84f,
                useCenter = false,
                topLeft = Offset(emitterCenter.x - outerR, emitterCenter.y - outerR),
                size = Size(outerR * 2f, outerR * 2f),
                style = waveStroke
            )
        }
    }
}

/**
 * 3. Movies — Cinema Play-Aperture:
 * Rounded cinema film-frame with 2 subtle top & bottom sprocket notches and SetPlay's
 * signature geometric `▶` play triangle centered inside (filled with `accentTint` when selected).
 * Micro-motion: gentle `1.08f` spring scale settle on the inner play triangle when selected.
 */
@Composable
fun SetPlayMoviesNavIcon(
    isSelected: Boolean,
    tint: Color,
    accentTint: Color,
    modifier: Modifier = Modifier.size(22.dp)
) {
    val activeProgress by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "movies_icon_active"
    )
    val playScale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.62f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "movies_play_scale"
    )

    Canvas(modifier = modifier) {
        val s = size.minDimension / 24f
        val ox = (size.width - 24f * s) / 2f
        val oy = (size.height - 24f * s) / 2f
        val strokePx = 1.8.dp.toPx()

        val primaryStroke = Stroke(
            width = strokePx,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
        val playStroke = Stroke(
            width = strokePx * 0.92f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        val frameLeft = ox + 2.6f * s
        val frameTop = oy + 3.8f * s
        val frameW = 18.8f * s
        val frameH = 16.4f * s
        val cornerPx = 4.dp.toPx().coerceAtMost(frameH * 0.28f)
        val cornerRadius = CornerRadius(cornerPx, cornerPx)

        // Soft dual-tone interior fill when active
        if (activeProgress > 0.01f) {
            drawRoundRect(
                color = accentTint.copy(alpha = 0.20f * activeProgress),
                topLeft = Offset(frameLeft, frameTop),
                size = Size(frameW, frameH),
                cornerRadius = cornerRadius
            )
        }

        // Rounded cinema film frame
        drawRoundRect(
            color = tint,
            topLeft = Offset(frameLeft, frameTop),
            size = Size(frameW, frameH),
            cornerRadius = cornerRadius,
            style = primaryStroke
        )

        // 2 subtle top & 2 subtle bottom cinema sprocket notches
        val notchX1 = ox + 7.4f * s
        val notchX2 = ox + 16.6f * s
        val notchLen = 2.0f * s
        val notchStrokePx = strokePx * 0.85f
        val frameBottom = frameTop + frameH

        drawLine(
            color = tint,
            start = Offset(notchX1, frameTop + 0.4f * s),
            end = Offset(notchX1, frameTop + notchLen),
            strokeWidth = notchStrokePx,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(notchX2, frameTop + 0.4f * s),
            end = Offset(notchX2, frameTop + notchLen),
            strokeWidth = notchStrokePx,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(notchX1, frameBottom - notchLen),
            end = Offset(notchX1, frameBottom - 0.4f * s),
            strokeWidth = notchStrokePx,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(notchX2, frameBottom - notchLen),
            end = Offset(notchX2, frameBottom - 0.4f * s),
            strokeWidth = notchStrokePx,
            cap = StrokeCap.Round
        )

        // SetPlay signature geometric ▶ play triangle centered optically inside
        val pivot = Offset(ox + 12.0f * s, oy + 12.0f * s)
        scale(scaleX = playScale, scaleY = playScale, pivot = pivot) {
            val playPath = Path().apply {
                moveTo(ox + 9.9f * s, oy + 9.0f * s)
                lineTo(ox + 15.3f * s, oy + 12.0f * s)
                lineTo(ox + 9.9f * s, oy + 15.0f * s)
                close()
            }
            if (activeProgress > 0.01f) {
                drawPath(
                    path = playPath,
                    color = accentTint.copy(alpha = 0.48f * activeProgress)
                )
            }
            drawPath(
                path = playPath,
                color = tint,
                style = playStroke
            )
        }
    }
}

/**
 * 4. Series — Layered Episode Deck:
 * Sleek foreground rounded media card with a crisp geometric play notch + two clean offset
 * stacked season/episode rim arcs rising gently behind the top edge.
 * Micro-motion: subtle `1.5.dp` upward spring lift of the back episode layers when selected.
 */
@Composable
fun SetPlaySeriesNavIcon(
    isSelected: Boolean,
    tint: Color,
    accentTint: Color,
    modifier: Modifier = Modifier.size(22.dp)
) {
    val activeProgress by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "series_icon_active"
    )
    val layerLiftDp by animateDpAsState(
        targetValue = if (isSelected) 1.5.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = 0.62f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "series_layer_lift"
    )

    Canvas(modifier = modifier) {
        val s = size.minDimension / 24f
        val ox = (size.width - 24f * s) / 2f
        val oy = (size.height - 24f * s) / 2f
        val strokePx = 1.8.dp.toPx()
        val liftPx = layerLiftDp.toPx()

        val primaryStroke = Stroke(
            width = strokePx,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
        val layerStroke = Stroke(
            width = strokePx * 0.88f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        // Back-most stacked season rim arc (lifts full `1.5.dp` when selected)
        val topRimY = (oy + 4.0f * s - liftPx).coerceAtLeast(oy + 1.6f * s)
        val topRimPath = Path().apply {
            moveTo(ox + 7.6f * s, topRimY + 0.45f * s)
            quadraticTo(ox + 12.0f * s, topRimY - 0.35f * s, ox + 16.4f * s, topRimY + 0.45f * s)
        }
        drawPath(
            path = topRimPath,
            color = tint.copy(alpha = 0.56f + 0.34f * activeProgress),
            style = layerStroke
        )

        // Middle stacked episode rim arc (lifts half-step for layered depth)
        val midRimY = (oy + 6.7f * s - liftPx * 0.55f).coerceAtLeast(oy + 4.2f * s)
        val midRimPath = Path().apply {
            moveTo(ox + 5.1f * s, midRimY + 0.55f * s)
            quadraticTo(ox + 12.0f * s, midRimY - 0.35f * s, ox + 18.9f * s, midRimY + 0.55f * s)
        }
        drawPath(
            path = midRimPath,
            color = tint.copy(alpha = 0.78f + 0.22f * activeProgress),
            style = layerStroke
        )

        // Foreground rounded media card
        val cardLeft = ox + 3.0f * s
        val cardTop = oy + 9.2f * s
        val cardW = 18.0f * s
        val cardH = 11.6f * s
        val cornerPx = 3.5.dp.toPx().coerceAtMost(cardH * 0.34f)
        val cornerRadius = CornerRadius(cornerPx, cornerPx)

        if (activeProgress > 0.01f) {
            drawRoundRect(
                color = accentTint.copy(alpha = 0.22f * activeProgress),
                topLeft = Offset(cardLeft, cardTop),
                size = Size(cardW, cardH),
                cornerRadius = cornerRadius
            )
        }

        drawRoundRect(
            color = tint,
            topLeft = Offset(cardLeft, cardTop),
            size = Size(cardW, cardH),
            cornerRadius = cornerRadius,
            style = primaryStroke
        )

        // Crisp centered play notch inside the foreground episode card
        val playCy = cardTop + cardH * 0.5f
        val playPath = Path().apply {
            moveTo(ox + 10.3f * s, playCy - 2.25f * s)
            lineTo(ox + 14.5f * s, playCy)
            lineTo(ox + 10.3f * s, playCy + 2.25f * s)
            close()
        }
        if (activeProgress > 0.01f) {
            drawPath(
                path = playPath,
                color = accentTint.copy(alpha = 0.42f * activeProgress)
            )
        }
        drawPath(
            path = playPath,
            color = tint,
            style = Stroke(
                width = strokePx * 0.86f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

/**
 * 5. Favorites — Celestial Star Crest:
 * Softly rounded 5-point star with balanced geometric proportions and an inner luminous
 * core highlight when active.
 * Micro-motion: gentle spring scale bloom (`1.0f -> ~1.12f -> 1.08f` settle) on selection.
 */
@Composable
fun SetPlayFavoritesNavIcon(
    isSelected: Boolean,
    tint: Color,
    accentTint: Color,
    modifier: Modifier = Modifier.size(22.dp)
) {
    val activeProgress by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "favorites_icon_active"
    )
    val bloomScale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.50f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "favorites_star_bloom"
    )

    Canvas(modifier = modifier) {
        val s = size.minDimension / 24f
        val cx = size.width / 2f
        val cy = size.height / 2f + 0.35f * s
        val pivot = Offset(cx, cy)
        val strokePx = 1.8.dp.toPx()

        val primaryStroke = Stroke(
            width = strokePx,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        scale(scaleX = bloomScale, scaleY = bloomScale, pivot = pivot) {
            val outerR = 8.9f * s
            val innerR = 4.25f * s

            // Build 10 alternating outer/inner star vertices
            val points = List(10) { idx ->
                val angleRad = ((-90.0 + idx * 36.0) * PI / 180.0)
                val r = if (idx % 2 == 0) outerR else innerR
                Offset(
                    x = cx + (r * cos(angleRad)).toFloat(),
                    y = cy + (r * sin(angleRad)).toFloat()
                )
            }

            // Softly rounded star path using quadratic corner blending at each vertex
            val cornerFactor = 0.20f
            val starPath = Path().apply {
                for (i in points.indices) {
                    val prev = points[(i + points.size - 1) % points.size]
                    val curr = points[i]
                    val next = points[(i + 1) % points.size]
                    val entry = Offset(
                        x = curr.x + (prev.x - curr.x) * cornerFactor,
                        y = curr.y + (prev.y - curr.y) * cornerFactor
                    )
                    val exit = Offset(
                        x = curr.x + (next.x - curr.x) * cornerFactor,
                        y = curr.y + (next.y - curr.y) * cornerFactor
                    )
                    if (i == 0) {
                        moveTo(entry.x, entry.y)
                    } else {
                        lineTo(entry.x, entry.y)
                    }
                    quadraticTo(curr.x, curr.y, exit.x, exit.y)
                }
                close()
            }

            // Dual-tone interior glow + luminous celestial core when selected
            if (activeProgress > 0.01f) {
                drawPath(
                    path = starPath,
                    color = accentTint.copy(alpha = 0.24f * activeProgress)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentTint.copy(alpha = 0.78f * activeProgress),
                            accentTint.copy(alpha = 0.18f * activeProgress),
                            Color.Transparent
                        ),
                        center = pivot,
                        radius = innerR * 0.95f
                    ),
                    radius = innerR * 0.95f,
                    center = pivot
                )
            }

            drawPath(
                path = starPath,
                color = tint,
                style = primaryStroke
            )
        }
    }
}
