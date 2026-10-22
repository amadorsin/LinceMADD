package com.example.linceops.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
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

    val pantallasMenu = listOf(Destino.Dashboard, Destino.Feed, Destino.Perfil)
    val mostrarBottomBar = rutaActual != Destino.Login.ruta

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, // 3. Conexión visual
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
                        val correoPrueba = when (rolUsuario) {
                            RolUsuario.ADMINISTRADOR -> "admin@linceops.cl"
                            RolUsuario.GUIA -> "guia@linceops.cl"
                            RolUsuario.CONDUCTOR -> "driver@linceops.cl"
                        }
                        serviceViewModel.iniciarSesion(correoPrueba)

                        navController.navigate(Destino.Dashboard.createRoute(rolUsuario.name)) {
                            popUpTo(Destino.Login.ruta) { NavOptionsBuilder.inclusive = true }
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

                DashboardScreen(
                    rolUsuario = rolUsuario,
                    uiState = uiState,
                    onIniciarMarcaje = { servicioId ->
                        navController.navigate(Destino.Marcaje.createRoute(servicioId))
                    }
                )
            }

            composable(Destino.Feed.ruta) {
                ServiciosScreen(
                    servicios = uiState.servicios,
                    onVerDetalle = { servicioId ->
                        navController.navigate(Destino.Detalle.createRoute(servicioId))
                    },
                    filtroEstado = uiState.filtroEstado,
                    filtroFecha = uiState.filtroFecha,
                    onFiltrarPorEstado = { estado -> serviceViewModel.filtrarPorEstado(estado) },
                    onFiltrarPorFecha = { fecha -> serviceViewModel.filtrarPorFecha(fecha) },
                    onLimpiarFiltros = { serviceViewModel.limpiarFiltros() }
                )
            }

            composable(Destino.Perfil.ruta) {
                PerfilScreen()
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
                        // NOTA: Ignoramos onIrAMarcaje aquí porque estamos aislando la versión de David.
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