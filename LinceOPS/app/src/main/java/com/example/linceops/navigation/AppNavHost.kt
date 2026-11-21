package com.example.linceops.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.linceops.R
import com.example.linceops.data.local.LinceOpsDatabase
import com.example.linceops.model.EstadoServicio
import com.example.linceops.model.RolUsuario
import com.example.linceops.ui.components.BarraInferiorGlass
import com.example.linceops.ui.components.DetalleSkeleton
import com.example.linceops.ui.components.NotificacionFlotante
import com.example.linceops.ui.screens.*
import com.example.linceops.utils.isInternetAvailable
import com.example.linceops.viewmodel.ServiceViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import kotlinx.coroutines.delay

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current

    val database = remember { LinceOpsDatabase.getDatabase(context) }

    val serviceViewModel: ServiceViewModel = remember {
        ServiceViewModel(database.servicioDao())
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    val uiState by serviceViewModel.uiState.collectAsState()
    val rolActual = uiState.usuarioLogueado?.rol ?: RolUsuario.CONDUCTOR

    var avisoTexto by remember { mutableStateOf("") }
    var avisoError by remember { mutableStateOf(false) }
    var avisoVisible by remember { mutableStateOf(false) }
    var avisoId by remember { mutableStateOf(0) }

    var servicioEnfocadoId by rememberSaveable { mutableStateOf<String?>(null) }
    var abrirDetallePendienteId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.mensaje, uiState.error) {
        val texto = uiState.mensaje ?: uiState.error
        if (texto != null) {
            avisoTexto = texto
            avisoError = uiState.error != null
            avisoVisible = true
            avisoId += 1
            serviceViewModel.limpiarMensajes()
        }
    }

    LaunchedEffect(avisoId) {
        if (avisoId > 0) {
            delay(2500)
            avisoVisible = false
        }
    }

    val pantallasMenu = listOf(Destino.Dashboard, Destino.Feed, Destino.Perfil)
    val rutaBase = rutaActual?.substringBefore("/")
    val mostrarBottomBar = rutaBase != null &&
            pantallasMenu.any { it.ruta.substringBefore("/") == rutaBase }

    var barraVisible by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(rutaActual) { barraVisible = true }

    val hazeState = remember { HazeState() }

    Box(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .haze(state = hazeState)
        ) {
            Scaffold { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = Destino.Login.ruta,
                    modifier = Modifier.padding(paddingValues),
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None },
                    popEnterTransition = { EnterTransition.None },
                    popExitTransition = { ExitTransition.None }
                ) {

                    composable(
                        route = Destino.Login.ruta,
                        enterTransition = { EnterTransition.None },
                        exitTransition = { ExitTransition.None },
                        popEnterTransition = { EnterTransition.None },
                        popExitTransition = { ExitTransition.None }
                    ) {
                        val usuarioLogueado = uiState.usuarioLogueado
                        LaunchedEffect(usuarioLogueado) {
                            if (usuarioLogueado != null) {
                                navController.navigate(Destino.Dashboard.createRoute(usuarioLogueado.rol.name)) {
                                    popUpTo(Destino.Login.ruta) { inclusive = true }
                                }
                            }
                        }

                        LoginScreen(
                            errorLogin = uiState.errorLogin,
                            onLogin = { correo, password ->
                                serviceViewModel.iniciarSesion(correo, password)
                            }
                        )
                    }

                    composable(
                        route = Destino.Dashboard.ruta,
                        arguments = listOf(navArgument("rol") { type = NavType.StringType }),
                        enterTransition = { EnterTransition.None },
                        exitTransition = { ExitTransition.None },
                        popEnterTransition = { EnterTransition.None },
                        popExitTransition = { ExitTransition.None }
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
                            },
                            onCrearServicio = {
                                navController.navigate("crear_servicio")
                            },
                            onVerDetalleServicio = { servicioId ->
                                if (uiState.servicios.none { it.id == servicioId }) {
                                    serviceViewModel.limpiarFiltros()
                                }
                                servicioEnfocadoId = servicioId
                                abrirDetallePendienteId = servicioId
                                barraVisible = true
                                navController.navigate(Destino.Feed.ruta) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onScrollBarra = { barraVisible = it }
                        )
                    }

                    composable(
                        route = Destino.Feed.ruta,
                        enterTransition = { EnterTransition.None },
                        exitTransition = { ExitTransition.None },
                        popEnterTransition = { EnterTransition.None },
                        popExitTransition = { ExitTransition.None }
                    ) {
                        ServiciosScreen(
                            servicios = uiState.servicios,
                            onVerDetalle = { servicioId ->
                                servicioEnfocadoId = servicioId
                                navController.navigate(Destino.Detalle.createRoute(servicioId)) {
                                    launchSingleTop = true
                                }
                            },
                            filtroEstado = uiState.filtroEstado,
                            filtroFecha = uiState.filtroFecha,
                            onFiltrarPorEstado = { estado -> serviceViewModel.filtrarPorEstado(estado) },
                            onFiltrarPorFecha = { fecha -> serviceViewModel.filtrarPorFecha(fecha) },
                            onLimpiarFiltros = { serviceViewModel.limpiarFiltros() },
                            onScrollBarra = { barraVisible = it },
                            isLoading = uiState.isLoading,
                            servicioEnfocadoId = servicioEnfocadoId,
                            abrirDetallePendienteId = abrirDetallePendienteId,
                            onDetallePendienteAbierto = { servicioId ->
                                abrirDetallePendienteId = null
                                navController.navigate(Destino.Detalle.createRoute(servicioId)) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable(
                        route = Destino.Perfil.ruta,
                        enterTransition = { EnterTransition.None },
                        exitTransition = { ExitTransition.None },
                        popEnterTransition = { EnterTransition.None },
                        popExitTransition = { ExitTransition.None }
                    ) {
                        PerfilScreen(
                            usuario = uiState.usuarioLogueado,
                            servicios = uiState.serviciosTotales,
                            diasBloqueadosRemotos = uiState.diasBloqueados,
                            onBloquearFecha = { fecha -> serviceViewModel.bloquearFecha(fecha) },
                            onDesbloquearFecha = { fecha -> serviceViewModel.desbloquearFecha(fecha) },
                            onScrollBarra = { barraVisible = it },
                            onCerrarSesion = {
                                servicioEnfocadoId = null
                                abrirDetallePendienteId = null
                                serviceViewModel.cerrarSesion()
                                navController.navigate(Destino.Login.ruta) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(
                        route = Destino.Detalle.ruta,
                        arguments = listOf(navArgument("servicioId") { type = NavType.StringType }),
                        enterTransition = { EnterTransition.None },
                        exitTransition = { ExitTransition.None },
                        popEnterTransition = { EnterTransition.None },
                        popExitTransition = { ExitTransition.None }
                    ) { backStackEntry ->
                        val servicioId = backStackEntry.arguments?.getString("servicioId") ?: "1"
                        val servicio = serviceViewModel.obtenerServicio(servicioId)
                        LaunchedEffect(servicioId) {
                            serviceViewModel.refrescarDetalleServicio(servicioId)
                        }
                        if (servicio != null) {
                            DetalleScreen(
                                servicio = servicio,
                                rolUsuario = rolActual,
                                usuarioId = uiState.usuarioLogueado?.id,
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
                                    serviceViewModel.rechazarServicio(
                                        servicio.id,
                                        context.getString(R.string.detalle_motivo_no_especificado)
                                    )
                                    navController.popBackStack()
                                },
                                onReasignar = {
                                    navController.popBackStack()
                                },
                                onIrAMarcaje = {
                                    navController.navigate(Destino.Marcaje.createRoute(servicio.id))
                                }
                            )
                        } else {
                            DetalleSkeleton()
                        }
                    }

                    composable(
                        route = Destino.Marcaje.ruta,
                        arguments = listOf(navArgument("servicioId") { type = NavType.StringType }),
                        enterTransition = { EnterTransition.None },
                        exitTransition = { ExitTransition.None },
                        popEnterTransition = { EnterTransition.None },
                        popExitTransition = { ExitTransition.None }
                    ) { backStackEntry ->
                        val servicioId = backStackEntry.arguments?.getString("servicioId") ?: "1"

                        val servicio = serviceViewModel.obtenerServicio(servicioId)
                        MarcajeScreen(
                            servicioId = servicioId,
                            servicio = servicio,
                            geocercas = uiState.geocercas,
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

                    composable(
                        route = "crear_servicio",
                        enterTransition = { EnterTransition.None },
                        exitTransition = { ExitTransition.None },
                        popEnterTransition = { EnterTransition.None },
                        popExitTransition = { ExitTransition.None }
                    ) {
                        CrearServicioScreen(
                            onGuardar = { titulo, ruta, fecha, hora, tipo, pax ->
                                serviceViewModel.crearNuevoServicio(
                                    titulo, ruta, fecha, hora, tipo, pax
                                )
                                navController.popBackStack()
                            },
                            onCancelar = { navController.popBackStack() }
                        )
                    }
                }
            }
        }

        if (mostrarBottomBar) {
            BarraInferiorGlass(
                rutaActual = rutaActual,
                onNavegar = { id ->
                    barraVisible = true
                    val rutaNavegacion = when (id) {
                        "inicio" -> Destino.Dashboard.createRoute(rolActual.name)
                        "servicios" -> Destino.Feed.ruta
                        else -> Destino.Perfil.ruta
                    }
                    navController.navigate(rutaNavegacion) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                visible = barraVisible,
                onVisibleChange = { barraVisible = it },
                hazeState = hazeState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        AnimatedVisibility(
            visible = avisoVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            NotificacionFlotante(texto = avisoTexto, esError = avisoError)
        }
    }
}
