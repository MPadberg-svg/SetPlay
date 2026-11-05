package com.example.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
}
