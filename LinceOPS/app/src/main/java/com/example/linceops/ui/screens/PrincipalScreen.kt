package com.example.linceops.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.linceops.navigation.Destino
import com.example.linceops.ui.theme.*

@Composable
fun PrincipalScreen(
    navController: NavHostController,
    content: @Composable (PaddingValues) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    val pantallasMenu = listOf(
        Destino.Dashboard,
        Destino.Feed,
        Destino.Perfil
    )

    val mostrarBottomBar = rutaActual != null && !rutaActual.startsWith("login")

    Scaffold(
        containerColor = LinceCreamBackground,
        bottomBar = {
            if (mostrarBottomBar) {
                NavigationBar(
                    containerColor = LinceCardSurface,
                    tonalElevation = 8.dp
                ) {
                    pantallasMenu.forEach { destino ->
                        val seleccionado = rutaActual?.substringBefore("/") == destino.ruta.substringBefore("/")

                        NavigationBarItem(
                            icon = {
                                destino.icon?.let {
                                    Icon(
                                        imageVector = it,
                                        contentDescription = destino.title
                                    )
                                }
                            },
                            label = {
                                destino.title?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            },
                            selected = seleccionado,
                            onClick = {
                                if (!seleccionado) {
                                    navController.navigate(destino.ruta.substringBefore("/")) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = LinceGreenDark,
                                selectedTextColor = LinceGreenDark,
                                indicatorColor = LinceGreenSageLight,
                                unselectedIconColor = LinceTextSecondary,
                                unselectedTextColor = LinceTextSecondary
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LinceCreamBackground)
        ) {
            content(paddingValues)
        }
    }
}
