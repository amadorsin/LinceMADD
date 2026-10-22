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
import com.example.linceops.viewmodel.ServiceViewModel

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

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.mensaje, uiState.error) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            serviceViewModel.limpiarMensajes()
        }
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            serviceViewModel.limpiarMensajes()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (mostrarBottomBar) {
                NavigationBar {
                    pantallasMenu.forEach { destino ->
                        val rutaBase = rutaActual?.substringBefore("/") ?: ""
                        val destinoBase = destino.ruta.substringBefore("/")
                        val seleccionado = rutaBase == destinoBase

                        NavigationBarItem(
                            icon = { Icon(destino.icon!!, contentDescription = destino.title) },
                            label = { Text(destino.title!!) },
                            selected = seleccionado,
                            onClick = {
                                val rutaNavegacion = if (destino == Destino.Dashboard) {
                                    Destino.Dashboard.createRoute(rolActual.name)
                                } else {
                                    destino.ruta.substringBefore("/")
                                }

                                navController.navigate(rutaNavegacion) {
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
                        val correoPrueba = when (rolUsuario) {
                            RolUsuario.ADMINISTRADOR -> "admin@linceops.cl"
                            RolUsuario.GUIA -> "guia@linceops.cl"
                            RolUsuario.CONDUCTOR -> "driver@linceops.cl"
                        }
                        serviceViewModel.iniciarSesion(correoPrueba)

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
                val rolUsuario = try { RolUsuario.valueOf(rolString) } catch (e: Exception) { RolUsuario.CONDUCTOR }

                DashboardScreen(rolUsuario = rolUsuario)
            }

            composable(Destino.Feed.ruta) {
                ServiciosScreen(
                    servicios = uiState.servicios,
                    onVerDetalle = { servicioId ->
                        navController.navigate(Destino.Detalle.createRoute(servicioId))
                    }
                )
            }

            composable(Destino.Perfil.ruta) {
                PerfilScreen(
                    onCerrarSesion = {
                        serviceViewModel.cerrarSesion()
                        navController.navigate(Destino.Login.ruta) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Destino.Detalle.ruta,
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
                            serviceViewModel.rechazarServicio(servicio.id, "Motivo no especificado")
                            navController.popBackStack()
                        },
                        onReasignar = {
                            navController.popBackStack()
                        }
                    )
                }
            }

            composable(
                route = Destino.Marcaje.ruta,
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