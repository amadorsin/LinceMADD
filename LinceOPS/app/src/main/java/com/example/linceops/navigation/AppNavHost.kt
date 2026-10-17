package com.example.linceops.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.linceops.model.RolUsuario
import com.example.linceops.ui.screens.*


@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    serviceViewModel: ServiceViewModel = remember { ServiceViewModel() }
) {

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    val uiState by serviceViewModel.uiState.collectAsState()
    val rolActual = uiState.usuarioLogueado?.rol ?: RolUsuario.CONDUCTOR

    val pantallasMenu = listOf(Destino.Dashboard, Destino.Feed, Destino.Perfil)
    val mostrarBottomBar = rutaActual != Destino.Login.ruta

    Scaffold(
        bottomBar = {
            if (mostrarBottomBar) {
                NavigationBar {
                    pantallasMenu.forEach { destino ->
                        val seleccionado = rutaActual?.substringBefore("/") == destino.ruta.substringBefore("/")

                        NavigationBarItem(
                            icon = { Icon(destino.icon!!, contentDescription = destino.title) },
                            label = { Text(destino.title!!) },
                            selected = seleccionado,
                            onClick = {
                                navController.navigate(destino.ruta.substringBefore("/")) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Destino.Login.ruta,
            modifier = Modifier.padding(paddingValues)
        ) {


            composable(Destino.Login.ruta) {
                LoginScreen(
                    onLoginSuccess = { rolUsuario ->

                        serviceViewModel.iniciarSesionPorRol(rolUsuario)
                        navController.navigate(Destino.Dashboard.createRoute(rolUsuario.name)) {
                            popUpTo(Destino.Login.ruta) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Destino.Dashboard.ruta,
                arguments = listOf(navArgument("rol") { type = NavType.StringType })
            ) { backStackEntry ->
                val rolString = backStackEntry.arguments?.getString("rol") ?: RolUsuario.CONDUCTOR.name
                val rolUsuario = RolUsuario.valueOf(rolString)

                DashboardScreen(rolUsuario = rolUsuario)
            }

            composable(Destino.Feed.ruta) {

                ServiciosScreen()
            }

            composable(Destino.Perfil.ruta) {
                PerfilScreen()
            }

            composable(
                route = "detalle/{servicioId}",
                arguments = listOf(navArgument("servicioId") { type = NavType.StringType })
            ) { backStackEntry ->
                val servicioId = backStackEntry.arguments?.getString("servicioId") ?: "1"

                val servicio = serviceViewModel.obtenerServicio(servicioId)

                if (servicio != null) {
                    DetalleScreen(
                        servicio = servicio,
                        rolUsuario = rolActual,
                        onAceptar = {
                            serviceViewModel.confirmarServicio(servicio.id)
                            navController.popBackStack()
                        },
                        onRechazar = {
                            serviceViewModel.rechazarServicio(servicio.id)
                            navController.popBackStack()
                        },
                        onReasignar = {
                            serviceViewModel.reasignarServicio(servicio.id)
                            navController.popBackStack()
                        }
                    )
                }
            }


            composable(
                route = "marcaje/{servicioId}",
                arguments = listOf(navArgument("servicioId") { type = NavType.StringType })
            ) { backStackEntry ->
                val servicioId = backStackEntry.arguments?.getString("servicioId") ?: "1"

                MarcajeScreen(
                    servicioId = servicioId,
                    onCheckIn = { lat, lon -> serviceViewModel.registrarCheckIn(servicioId, lat, lon) },
                    onCheckOut = { lat, lon -> serviceViewModel.registrarCheckOut(servicioId, lat, lon) }
                )
            }
        }
    }
}