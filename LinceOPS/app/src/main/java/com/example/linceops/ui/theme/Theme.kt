package com.example.linceops.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object LinceSpacing {
    val xSmall: Dp = 4.dp
    val small: Dp = 8.dp
    val mediumSmall: Dp = 12.dp
    val medium: Dp = 16.dp
    val large: Dp = 20.dp
    val xLarge: Dp = 24.dp
    val xxLarge: Dp = 32.dp
}

object LinceRadius {
    val small: Dp = 10.dp
    val medium: Dp = 14.dp
    val large: Dp = 16.dp
    val xLarge: Dp = 20.dp
}

private val LightLinceColorScheme = lightColorScheme(
    primary = LightLinceColors.greenPrimary,
    onPrimary = LightLinceColors.onPrimary,
    primaryContainer = LightLinceColors.greenSageLight,
    onPrimaryContainer = LightLinceColors.greenDark,
    secondary = LightLinceColors.greenDark,
    onSecondary = LightLinceColors.onPrimary,
    secondaryContainer = LightLinceColors.greenSageLight,
    onSecondaryContainer = LightLinceColors.greenDark,
    tertiary = LightLinceColors.tertiary,
    onTertiary = LightLinceColors.textPrimary,
    tertiaryContainer = LightLinceColors.tertiaryContainer,
    onTertiaryContainer = LightLinceColors.onTertiaryContainer,
    background = LightLinceColors.background,
    onBackground = LightLinceColors.textPrimary,
    surface = LightLinceColors.cardSurface,
    onSurface = LightLinceColors.textPrimary,
    surfaceVariant = LightLinceColors.surfaceVariant,
    onSurfaceVariant = LightLinceColors.textSecondary,
    surfaceContainerLowest = LightLinceColors.cardSurface,
    surfaceContainerLow = LightLinceColors.surfaceContainerLow,
    surfaceContainer = LightLinceColors.surfaceVariant,
    surfaceContainerHigh = LightLinceColors.surfaceContainerHigh,
    surfaceContainerHighest = LightLinceColors.surfaceContainerHigh,
    surfaceTint = LightLinceColors.greenPrimary,
    outline = LightLinceColors.outlineVariant,
    outlineVariant = LightLinceColors.outlineVariant,
    error = LightLinceColors.dangerRed,
    onError = LightLinceColors.onPrimary
)

private val DarkLinceColorScheme = darkColorScheme(
    primary = DarkLinceColors.greenPrimary,
    onPrimary = DarkLinceColors.onPrimary,
    primaryContainer = DarkLinceColors.greenSageLight,
    onPrimaryContainer = DarkLinceColors.greenDark,
    secondary = DarkLinceColors.greenDark,
    onSecondary = DarkLinceColors.background,
    secondaryContainer = DarkLinceColors.greenSageLight,
    onSecondaryContainer = DarkLinceColors.greenDark,
    tertiary = DarkLinceColors.tertiary,
    onTertiary = DarkLinceColors.background,
    tertiaryContainer = DarkLinceColors.tertiaryContainer,
    onTertiaryContainer = DarkLinceColors.onTertiaryContainer,
    background = DarkLinceColors.background,
    onBackground = DarkLinceColors.textPrimary,
    surface = DarkLinceColors.cardSurface,
    onSurface = DarkLinceColors.textPrimary,
    surfaceVariant = DarkLinceColors.surfaceVariant,
    onSurfaceVariant = DarkLinceColors.textSecondary,
    surfaceContainerLowest = DarkLinceColors.background,
    surfaceContainerLow = DarkLinceColors.surfaceContainerLow,
    surfaceContainer = DarkLinceColors.cardSurface,
    surfaceContainerHigh = DarkLinceColors.surfaceContainerHigh,
    surfaceContainerHighest = DarkLinceColors.surfaceContainerHigh,
    surfaceTint = DarkLinceColors.greenPrimary,
    outline = DarkLinceColors.outlineVariant,
    outlineVariant = DarkLinceColors.outlineVariant,
    error = DarkLinceColors.dangerRed,
    onError = DarkLinceColors.onPrimary
)

@Composable
fun LinceOPSTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val linceColors = if (darkTheme) DarkLinceColors else LightLinceColors
    val colorScheme = if (darkTheme) DarkLinceColorScheme else LightLinceColorScheme

    CompositionLocalProvider(LocalLinceColors provides linceColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
