package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Permanent Dark Theme using the "Royal Gold (Premium & Luxury)" Brand Kit:
 * - Background: Dark Graphite (#141619)
 * - Elevated Containers / Cards / Nav Bars: Metal Charcoal (#25282E)
 * - Primary Accent: Amber Gold (#D4A238) with Dark Graphite (#141619) onPrimary contrast
 * - Primary Titles / Active Icons / VIP Badges: Pale Gold (#F6E8C8)
 * - Unselected Chips / Subtle Borders / Secondary Metadata: Champagne (#DFC69E)
 */
val RoyalGoldColorScheme = darkColorScheme(
    primary = AmberGold,
    onPrimary = DarkGraphite,
    primaryContainer = MetalCharcoal,
    onPrimaryContainer = PaleGold,
    secondary = Champagne,
    onSecondary = DarkGraphite,
    secondaryContainer = MetalCharcoal,
    onSecondaryContainer = Champagne,
    tertiary = PaleGold,
    onTertiary = DarkGraphite,
    background = DarkGraphite,
    onBackground = PaleGold,
    surface = MetalCharcoal,
    onSurface = PaleGold,
    surfaceVariant = MetalCharcoal,
    onSurfaceVariant = Champagne,
    surfaceContainer = MetalCharcoal,
    surfaceContainerHigh = MetalCharcoalElevated,
    surfaceContainerHighest = MetalCharcoalElevated,
    surfaceTint = AmberGold,
    outline = Champagne,
    outlineVariant = ChampagneSubtleBorder,
    error = LiveRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Permanent Royal Gold dark theme as specified
    MaterialTheme(
        colorScheme = RoyalGoldColorScheme,
        typography = Typography,
        content = content
    )
}
