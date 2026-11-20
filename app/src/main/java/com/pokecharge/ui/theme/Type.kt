package com.pokecharge.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Two families only, matching the brief: a clean sans-serif for general
 * copy, and a monospaced "dot-matrix" stand-in for technical/numeric
 * readouts (the big battery counter, status labels). No font files are
 * bundled - see assets/ASSET_SOURCES.md §3 for why.
 */
val PcSans = FontFamily.Default
val PcMono = FontFamily.Monospace

val PcTypography = Typography(
    headlineLarge = TextStyle(fontFamily = PcSans, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, letterSpacing = 0.2.sp),
    headlineMedium = TextStyle(fontFamily = PcSans, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    titleLarge = TextStyle(fontFamily = PcSans, fontWeight = FontWeight.Medium, fontSize = 18.sp),
    titleMedium = TextStyle(fontFamily = PcSans, fontWeight = FontWeight.Medium, fontSize = 15.sp),
    bodyLarge = TextStyle(fontFamily = PcSans, fontWeight = FontWeight.Normal, fontSize = 15.sp),
    bodyMedium = TextStyle(fontFamily = PcSans, fontWeight = FontWeight.Normal, fontSize = 13.sp),
    labelLarge = TextStyle(fontFamily = PcMono, fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 1.2.sp),
    labelMedium = TextStyle(fontFamily = PcMono, fontWeight = FontWeight.Normal, fontSize = 11.sp, letterSpacing = 1.sp),
    labelSmall = TextStyle(fontFamily = PcMono, fontWeight = FontWeight.Normal, fontSize = 10.sp, letterSpacing = 1.sp),
)

/** Extra-large dot-matrix-flavoured numeral style for the battery readout. */
val PcBatteryNumberStyle = TextStyle(
    fontFamily = PcMono,
    fontWeight = FontWeight.Bold,
    fontSize = 72.sp,
    letterSpacing = 0.sp,
)
