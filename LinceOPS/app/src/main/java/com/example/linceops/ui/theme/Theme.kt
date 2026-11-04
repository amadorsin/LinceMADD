package com.example.linceops.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LinceColorScheme = lightColorScheme(
    primary = LinceGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = LinceGreenSageLight,
    onPrimaryContainer = LinceGreenDark,

    // Reemplaza el morado en FilterChips seleccionados y píldora del NavigationBar inferior
    secondary = LinceGreenDark,
    onSecondary = Color.White,
    secondaryContainer = LinceGreenSageLight,
    onSecondaryContainer = LinceGreenDark,

    // Acento cálido del prototipo (badges, metadatos y acentos)
    tertiary = LinceTertiary,
    onTertiary = Color.White,
    tertiaryContainer = LinceTertiaryContainer,
    onTertiaryContainer = LinceOnTertiaryContainer,

    // Fondos principales y tarjetas (elimina el fondo lila del Dashboard)
    background = LinceCreamBackground,
    onBackground = LinceTextPrimary,
    surface = LinceCardSurface,
    onSurface = LinceTextPrimary,
    surfaceVariant = LinceSurfaceVariant,
    onSurfaceVariant = LinceTextSecondary,

    // Capas tonales cálidas (tarjetas, píldoras y contenedores anidados)
    surfaceContainerLowest = LinceCardSurface,
    surfaceContainerLow = LinceSurfaceContainerLow,
    surfaceContainer = LinceSurfaceVariant,
    surfaceContainerHigh = LinceSurfaceContainerHigh,
    surfaceContainerHighest = LinceSurfaceContainerHigh,

    // Barra inferior y bordes
    outline = LinceOutlineVariant,
    outlineVariant = LinceOutlineVariant,
    error = LinceDangerRed,
    onError = Color.White
)

@Composable
fun LinceOPSTheme(
    // Desactivamos dynamicColor por defecto para mantener siempre la identidad de LinceOPS
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LinceColorScheme,
        typography = Typography,
        content = content
    )
}
