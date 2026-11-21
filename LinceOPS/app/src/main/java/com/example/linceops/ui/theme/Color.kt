package com.example.linceops.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class LinceColors(
    val greenPrimary: Color,
    val onPrimary: Color,
    val greenDark: Color,
    val greenSageLight: Color,
    val greenBorder: Color,
    val outlineVariant: Color,
    val background: Color,
    val cardSurface: Color,
    val surfaceVariant: Color,
    val surfaceContainerLow: Color,
    val surfaceContainerHigh: Color,
    val tertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val inverseSurface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val statusPendingBg: Color,
    val statusPendingText: Color,
    val statusConfirmedBg: Color,
    val statusConfirmedText: Color,
    val statusInProgressBg: Color,
    val statusInProgressText: Color,
    val statusCompletedBg: Color,
    val statusCompletedText: Color,
    val statusRejectedBg: Color,
    val statusRejectedText: Color,
    val dangerRed: Color,
    val mapLand: Color,
    val mapBlock: Color,
    val mapPark: Color,
    val mapRoad: Color,
    val mapRoadEdge: Color,
    val mapWater: Color,
    val mapGpsBlue: Color,
    val mapGpsHalo: Color,
    val glassSurface: Color,
    val glassBorder: Color,
    val shimmerBase: Color,
    val shimmerHighlight: Color,
    val isDark: Boolean
)

val LightLinceColors = LinceColors(
    greenPrimary = Color(0xFF3E7B52),
    onPrimary = Color(0xFFFFFFFF),
    greenDark = Color(0xFF285436),
    greenSageLight = Color(0xFFE4EFE7),
    greenBorder = Color(0xFFC2D8C8),
    outlineVariant = Color(0xFFC4C8BC),
    background = Color(0xFFFAF7F2),
    cardSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEFECE6),
    surfaceContainerLow = Color(0xFFF5F1EA),
    surfaceContainerHigh = Color(0xFFEAE6DE),
    tertiary = Color(0xFF705C30),
    tertiaryContainer = Color(0xFFC4A66A),
    onTertiaryContainer = Color(0xFF554020),
    inverseSurface = Color(0xFF2E3230),
    textPrimary = Color(0xFF1C241E),
    textSecondary = Color(0xFF526056),
    statusPendingBg = Color(0xFFFFF3CD),
    statusPendingText = Color(0xFF7A5A00),
    statusConfirmedBg = Color(0xFFDCF2E3),
    statusConfirmedText = Color(0xFF1E5631),
    statusInProgressBg = Color(0xFFD6EEF2),
    statusInProgressText = Color(0xFF0F4C5C),
    statusCompletedBg = Color(0xFFE8ECE9),
    statusCompletedText = Color(0xFF37473D),
    statusRejectedBg = Color(0xFFFADBD8),
    statusRejectedText = Color(0xFF78281F),
    dangerRed = Color(0xFFA93226),
    mapLand = Color(0xFFEDE9DF),
    mapBlock = Color(0xFFE0DBCC),
    mapPark = Color(0xFFD6E5D3),
    mapRoad = Color(0xFFFFFDF8),
    mapRoadEdge = Color(0xFFD9D2C2),
    mapWater = Color(0xFFC6D9D8),
    mapGpsBlue = Color(0xFF1E88E5),
    mapGpsHalo = Color(0xFFFFFFFF),
    glassSurface = Color(0xFFFDFBF7),
    glassBorder = Color(0xCCFFFFFF),
    shimmerBase = Color(0xFFE6E1D8),
    shimmerHighlight = Color(0xFFF7F4EE),
    isDark = false
)

val DarkLinceColors = LinceColors(
    greenPrimary = Color(0xFF4C9F68),
    onPrimary = Color(0xFFFFFFFF),
    greenDark = Color(0xFF68C288),
    greenSageLight = Color(0xFF1A1A1A),
    greenBorder = Color(0xFF262626),
    outlineVariant = Color(0xFF2E2E2E),
    background = Color(0xFF000000),
    cardSurface = Color(0xFF121212),
    surfaceVariant = Color(0xFF1A1A1A),
    surfaceContainerLow = Color(0xFF141414),
    surfaceContainerHigh = Color(0xFF222222),
    tertiary = Color(0xFFE0BA6E),
    tertiaryContainer = Color(0xFF262015),
    onTertiaryContainer = Color(0xFFF5D99B),
    inverseSurface = Color(0xFFF5F5F5),
    textPrimary = Color(0xFFF5F5F5),
    textSecondary = Color(0xFFA3A3A3),
    statusPendingBg = Color(0xFF2B2210),
    statusPendingText = Color(0xFFF6C958),
    statusConfirmedBg = Color(0xFF14261B),
    statusConfirmedText = Color(0xFF7CE39E),
    statusInProgressBg = Color(0xFF13262B),
    statusInProgressText = Color(0xFF74D6EE),
    statusCompletedBg = Color(0xFF1F1F1F),
    statusCompletedText = Color(0xFFD4D4D4),
    statusRejectedBg = Color(0xFF2B1615),
    statusRejectedText = Color(0xFFF58F86),
    dangerRed = Color(0xFFD9534F),
    mapLand = Color(0xFF0D0D0D),
    mapBlock = Color(0xFF171717),
    mapPark = Color(0xFF142218),
    mapRoad = Color(0xFF242424),
    mapRoadEdge = Color(0xFF1C1C1C),
    mapWater = Color(0xFF111D22),
    mapGpsBlue = Color(0xFF42A5F5),
    mapGpsHalo = Color(0xFFF5F5F5),
    glassSurface = Color(0xFF121212),
    glassBorder = Color(0xFF2E2E2E),
    shimmerBase = Color(0xFF161616),
    shimmerHighlight = Color(0xFF262626),
    isDark = true
)

val LocalLinceColors = staticCompositionLocalOf { LightLinceColors }

object LinceTheme {
    val colors: LinceColors
        @Composable
        @ReadOnlyComposable
        get() = LocalLinceColors.current
}

val LinceGreenPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.greenPrimary

val LinceOnPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.onPrimary

val LinceGreenDark: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.greenDark

val LinceGreenSageLight: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.greenSageLight

val LinceGreenBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.greenBorder

val LinceBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.outlineVariant

val LinceOutlineVariant: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.outlineVariant

val LinceCreamBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.background

val LinceBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.background

val LinceCardSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.cardSurface

val LinceSurfaceVariant: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.surfaceVariant

val LinceSurfaceContainerLow: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.surfaceContainerLow

val LinceSurfaceContainerHigh: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.surfaceContainerHigh

val LinceTertiary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.tertiary

val LinceTertiaryContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.tertiaryContainer

val LinceOnTertiaryContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.onTertiaryContainer

val LinceInverseSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.inverseSurface

val LinceTextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.textPrimary

val LinceTextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.textSecondary

val StatusPendingBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.statusPendingBg

val StatusPendingText: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.statusPendingText

val StatusConfirmedBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.statusConfirmedBg

val StatusConfirmedText: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.statusConfirmedText

val StatusInProgressBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.statusInProgressBg

val StatusInProgressText: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.statusInProgressText

val StatusCompletedBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.statusCompletedBg

val StatusCompletedText: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.statusCompletedText

val StatusRejectedBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.statusRejectedBg

val StatusRejectedText: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.statusRejectedText

val LinceDangerRed: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.dangerRed

val LinceMapLand: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.mapLand

val LinceMapBlock: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.mapBlock

val LinceMapPark: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.mapPark

val LinceMapRoad: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.mapRoad

val LinceMapRoadEdge: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.mapRoadEdge

val LinceMapWater: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.mapWater

val LinceGlassSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.glassSurface

val LinceGlassBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalLinceColors.current.glassBorder
