package com.pokecharge.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PcColorScheme = darkColorScheme(
    primary = PcWhite,
    onPrimary = PcBlack,
    secondary = PcAccentRed,
    onSecondary = PcWhite,
    background = PcBlack,
    onBackground = PcWhite,
    surface = PcMutedSurface,
    onSurface = PcWhite,
    surfaceVariant = PcMutedSurface,
    onSurfaceVariant = PcSecondaryText,
    outline = PcBorder,
    error = PcAccentRed,
)

@Composable
fun PokeChargeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PcColorScheme,
        typography = PcTypography,
        content = content,
    )
}
