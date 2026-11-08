package com.example.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * Reusable Phone + Android TV D-Pad Focus & Scale Choreography Modifier:
 * - Tracks `.onFocusChanged { isFocused = it.isFocused }`
 * - Animates a `1.04f` scale when focused via an Android TV D-pad remote
 * - Renders a `2.dp` illuminated `accentColor` / `AmberGold` border ring when focused
 * - Triggers `onClick` on both touch tap and D-pad Center / Enter key press
 */
fun Modifier.luxuryFocusableCard(
    accentColor: Color = AmberGold,
    shape: Shape = RoundedCornerShape(16.dp),
    focusedScale: Float = 1.04f,
    onClick: () -> Unit
): Modifier = composed {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) focusedScale else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tv_focus_scale"
    )

    this
        .onFocusChanged { focusState ->
            isFocused = focusState.isFocused
        }
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .then(
            if (isFocused) {
                Modifier.border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(accentColor, AmberGold, accentColor)
                    ),
                    shape = shape
                )
            } else {
                Modifier
            }
        )
        .clickable { onClick() }
        .focusable()
}

/**
 * Reusable Phone + Android TV D-Pad Focus & Scale Choreography Modifier for HUD Pills,
 * Top Bar Controls, Navigation Chambers, and Dossier Tabs:
 * - Tracks `.onFocusChanged { isFocused = it.isFocused }`
 * - Animates a `1.06f` spring scale when focused via an Android TV D-Pad remote
 * - Draws a high-contrast `2.dp` `AmberGold` / `accentColor` focus border ring when focused
 * - Invokes `onClick` on both touch tap and D-Pad Center / Enter key press
 */
fun Modifier.tvFocusableControl(
    accentColor: Color = AmberGold,
    shape: Shape = RoundedCornerShape(50),
    focusedScale: Float = 1.06f,
    onClick: () -> Unit
): Modifier = composed {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) focusedScale else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tv_focus_control_scale"
    )

    this
        .onFocusChanged { focusState ->
            isFocused = focusState.isFocused
        }
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .then(
            if (isFocused) {
                Modifier.border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(accentColor, AmberGold, accentColor)
                    ),
                    shape = shape
                )
            } else {
                Modifier
            }
        )
        .clickable { onClick() }
        .focusable()
}

/**
 * Alias for pill-shaped controls on Android TV and Phone.
 */
fun Modifier.tvFocusablePill(
    accentColor: Color = AmberGold,
    shape: Shape = RoundedCornerShape(50),
    focusedScale: Float = 1.06f,
    onClick: () -> Unit
): Modifier = tvFocusableControl(
    accentColor = accentColor,
    shape = shape,
    focusedScale = focusedScale,
    onClick = onClick
)

/**
 * Adaptive VOD Poster minimum cell width (`dp`) across Phone, Tablet, and 10-foot Android TV:
 * - Phone (`< 600dp`): `112dp` (fast 3-column poster density on handheld screens)
 * - Medium Tablet (`600..839dp`): `136dp`
 * - Expanded Tablet / Widescreen Android TV (`>= 840dp`): `156dp`
 */
fun calculateVodPosterMinCellDp(screenWidthDp: Int): Int {
    return when {
        screenWidthDp >= 840 -> 156
        screenWidthDp >= 600 -> 136
        else -> 112
    }
}

/**
 * Adaptive Live TV channel grid column count across Phone, Tablet, and 10-foot Android TV:
 * - Phone (`< 600dp`): `1` column (clean broadcast row list)
 * - Medium Tablet / Landscape (`600..839dp`): `2` columns
 * - Widescreen Android TV (`>= 840dp`): `3` columns (prevents stretching single rows across 55" TV)
 */
fun calculateLiveTvGridColumns(screenWidthDp: Int): Int {
    return when {
        screenWidthDp >= 840 -> 3
        screenWidthDp >= 600 -> 2
        else -> 1
    }
}

