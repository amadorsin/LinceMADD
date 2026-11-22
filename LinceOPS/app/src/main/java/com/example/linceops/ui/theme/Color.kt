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
    val isDark: Boolean,
    val goldAccent: Color = tertiary,
    val goldContainer: Color = tertiaryContainer,
    val success: Color = statusConfirmedText,
    val successContainer: Color = statusConfirmedBg,
    val warning: Color = statusPendingText,
    val warningContainer: Color = statusPendingBg,
    val error: Color = statusRejectedText,
    val errorContainer: Color = statusRejectedBg,
    val info: Color = statusInProgressText,
    val infoContainer: Color = statusInProgressBg,
    val neutral: Color = statusCompletedText,
    val neutralContainer: Color = statusCompletedBg
)

val LightLinceColors = LinceColors(
    greenPrimary = Color(0xFF397A55),
    onPrimary = Color(0xFFFFFFFF),
    greenDark = Color(0xFF244C37),
    greenSageLight = Color(0xFFE5EDE5),
    greenBorder = Color(0xFFE3E9E2),
    outlineVariant = Color(0xFFE3E9E2),
    background = Color(0xFFF7F4ED),
    cardSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1F0EA),
    surfaceContainerLow = Color(0xFFF7F6F1),
    surfaceContainerHigh = Color(0xFFEEF1EA),
    tertiary = Color(0xFFD9B96C),
    tertiaryContainer = Color(0xFFF4EBD4),
    onTertiaryContainer = Color(0xFF5B4A24),
    inverseSurface = Color(0xFF2E3230),
    textPrimary = Color(0xFF26342A),
    textSecondary = Color(0xFF6B756E),
    statusPendingBg = Color(0xFFFFF3CD),
    statusPendingText = Color(0xFF7A5A00),
    statusConfirmedBg = Color(0xFFE5EDE5),
    statusConfirmedText = Color(0xFF244C37),
    statusInProgressBg = Color(0xFFE4EFF2),
    statusInProgressText = Color(0xFF245767),
    statusCompletedBg = Color(0xFFF0F1EE),
    statusCompletedText = Color(0xFF525B55),
    statusRejectedBg = Color(0xFFFADBD8),
    statusRejectedText = Color(0xFF78281F),
    dangerRed = Color(0xFFA93226),
    mapLand = Color(0xFFF0EDE4),
    mapBlock = Color(0xFFE7E3D8),
    mapPark = Color(0xFFDCE8D9),
    mapRoad = Color(0xFFFFFEFA),
    mapRoadEdge = Color(0xFFDCD8CD),
    mapWater = Color(0xFFD6E4E3),
    mapGpsBlue = Color(0xFF1E88E5),
    mapGpsHalo = Color(0xFFFFFFFF),
    glassSurface = Color(0xFFFDFBF7),
    glassBorder = Color(0xCCFFFFFF),
    shimmerBase = Color(0xFFEFECE4),
    shimmerHighlight = Color(0xFFE5EDE5),
    isDark = false,
    goldAccent = Color(0xFFD9B96C),
    goldContainer = Color(0xFFF4EBD4),
    success = Color(0xFF2F7D4A),
    successContainer = Color(0xFFE5EDE5),
    warning = Color(0xFF8A6200),
    warningContainer = Color(0xFFFFF3CD),
    error = Color(0xFFA93226),
    errorContainer = Color(0xFFFADBD8),
    info = Color(0xFF245767),
    infoContainer = Color(0xFFE4EFF2),
    neutral = Color(0xFF525B55),
    neutralContainer = Color(0xFFF0F1EE)
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
    shimmerBase = Color(0xFF141414),
    shimmerHighlight = Color(0xFF1F1F1F),
    isDark = true,
    goldAccent = Color(0xFFE0C27A),
    goldContainer = Color(0xFF292317),
    success = Color(0xFF7CE39E),
    successContainer = Color(0xFF14261B),
    warning = Color(0xFFF6C958),
    warningContainer = Color(0xFF2B2210),
    error = Color(0xFFF58F86),
    errorContainer = Color(0xFF2B1615),
    info = Color(0xFF74D6EE),
    infoContainer = Color(0xFF13262B),
    neutral = Color(0xFFD4D4D4),
    neutralContainer = Color(0xFF1F1F1F)
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
