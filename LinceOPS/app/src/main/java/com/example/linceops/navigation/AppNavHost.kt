package com.example.linceops.navigation
import androidx.compose.ui.platform.LocalContext
import com.example.linceops.data.local.LinceOpsDatabase
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
import com.example.linceops.model.EstadoServicio
import com.example.linceops.utils.isInternetAvailable

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    // 1. Obtenemos el contexto actual
    val context = LocalContext.current

    // 2. Instanciamos la base de datos
    val database = remember { LinceOpsDatabase.getDatabase(context) }

    // 3. Le pasamos el DAO al ViewModel
    val serviceViewModel: ServiceViewModel = remember {
        ServiceViewModel(database.servicioDao())
    }
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
                val rolString =
                    backStackEntry.arguments?.getString("rol") ?: RolUsuario.CONDUCTOR.name
                val rolUsuario = try {
                    RolUsuario.valueOf(rolString)
                } catch (e: Exception) {
                    RolUsuario.CONDUCTOR
                }

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
                            val hayRed = isInternetAvailable(context)

                            serviceViewModel.cambiarEstadoServicio(
                                servicioId = servicio.id,
                                nuevoEstado = EstadoServicio.CONFIRMADO,
                                hayInternet = hayRed
                            )

                            navController.popBackStack()
                        },
                        onRechazar = {
                            serviceViewModel.rechazarServicio(servicio.id, "Motivo no especificado")
                            navController.popBackStack()
                        },
                        onReasignar = {
                            navController.popBackStack()
                        },
                        onIrAMarcaje = {
                            navController.navigate(Destino.Marcaje.createRoute(servicio.id))
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
                    onCheckIn = { lat, lon ->
                        val hayRed = isInternetAvailable(context)
                        serviceViewModel.registrarCheckIn(servicioId, lat, lon, hayRed)
                    },
                    onCheckOut = { lat, lon ->
                        val hayRed = isInternetAvailable(context)
                        serviceViewModel.registrarCheckOut(servicioId, lat, lon, hayRed)
                    }
                )
            }
            composable("crear_servicio") {
                CrearServicioScreen(
                    onGuardar = { titulo, ruta, fecha, hora, tipo, pax ->
                        serviceViewModel.crearNuevoServicio(titulo, ruta, fecha,
                            hora, tipo, pax)
                        navController.popBackStack()
                    },
                    onCancelar = { navController.popBackStack() }
                )
            }
        }
    }
}