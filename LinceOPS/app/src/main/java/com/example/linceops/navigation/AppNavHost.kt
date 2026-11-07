package com.example.linceops.navigation
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.platform.LocalContext
import com.example.linceops.data.local.LinceOpsDatabase
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.linceops.model.RolUsuario
import com.example.linceops.ui.screens.*
import com.example.linceops.ui.components.BarraInferiorGlass
import com.example.linceops.ui.components.NotificacionFlotante
import com.example.linceops.viewmodel.ServiceViewModel
import com.example.linceops.model.EstadoServicio
import com.example.linceops.utils.isInternetAvailable
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import kotlinx.coroutines.delay

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

    // Aviso flotante propio: reemplaza el Snackbar oscuro y desaparece solo.
    var avisoTexto by remember { mutableStateOf("") }
    var avisoError by remember { mutableStateOf(false) }
    var avisoVisible by remember { mutableStateOf(false) }
    var avisoId by remember { mutableStateOf(0) }

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
            delay(1500)
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
        // El contenido se marca como fuente del desenfoque de la barra inferior.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .haze(state = hazeState)
        ) {
            Scaffold { paddingValues ->
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
                    },
                    onCrearServicio = {
                        navController.navigate("crear_servicio")
                    },
                    onScrollBarra = { barraVisible = it }
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
                    onLimpiarFiltros = { serviceViewModel.limpiarFiltros() },
                    onScrollBarra = { barraVisible = it }
                )
            }

            composable(Destino.Perfil.ruta) {
                PerfilScreen(
                    onScrollBarra = { barraVisible = it },
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