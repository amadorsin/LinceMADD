package com.example.linceops.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LinceColorScheme = lightColorScheme(
    primary = VerdePrincipal,
    secondary = CremaSecundario,
    background = FondoCrema,
    surface = Blanco,
    onPrimary = Blanco,
    onSecondary = TextoPrincipal,
    onBackground = TextoPrincipal,
    onSurface = TextoPrincipal,
    tertiary = AcentoAmarillo
)

@Composable
fun LinceOPSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Usamos siempre nuestro esquema de colores (sin dark mode para este MVP)
    MaterialTheme(
        colorScheme = LinceColorScheme,
        typography = Typography,
        content = content
    )
}