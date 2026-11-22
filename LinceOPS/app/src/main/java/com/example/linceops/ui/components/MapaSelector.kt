package com.example.linceops.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.linceops.R
import com.example.linceops.ui.theme.LinceCardSurface
import com.example.linceops.ui.theme.LinceCreamBackground
import com.example.linceops.ui.theme.LinceGreenBorder
import com.example.linceops.ui.theme.LinceGreenDark
import com.example.linceops.ui.theme.LinceGreenPrimary
import com.example.linceops.ui.theme.LinceGreenSageLight
import com.example.linceops.ui.theme.LinceMapBlock
import com.example.linceops.ui.theme.LinceMapLand
import com.example.linceops.ui.theme.LinceMapPark
import com.example.linceops.ui.theme.LinceMapRoad
import com.example.linceops.ui.theme.LinceMapRoadEdge
import com.example.linceops.ui.theme.LinceMapWater
import com.example.linceops.ui.theme.LinceOutlineVariant
import com.example.linceops.ui.theme.LinceOnPrimary
import com.example.linceops.ui.theme.LinceRadius
import com.example.linceops.ui.theme.LinceSpacing
import com.example.linceops.ui.theme.LinceSurfaceContainerLow
import com.example.linceops.ui.theme.LinceSurfaceVariant
import com.example.linceops.ui.theme.LinceTertiary
import com.example.linceops.ui.theme.LinceTextPrimary
import com.example.linceops.ui.theme.LinceTextSecondary
import com.example.linceops.ui.theme.LinceTheme
import com.example.linceops.utils.isInternetAvailable
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class PuntoMapa(val latitud: Double, val longitud: Double)

data class LugarSugerido(val nombre: String, val punto: PuntoMapa)

data class ResultadoDireccion(val nombre: String, val punto: PuntoMapa)

val PUNTO_PUERTO_MONTT = PuntoMapa(-41.4717, -72.9397)

val LUGARES_SUGERIDOS = listOf(
    LugarSugerido("Aeropuerto El Tepual", PuntoMapa(-41.4389, -73.0940)),
    LugarSugerido("Hotel Centro", PuntoMapa(-41.4717, -72.9397)),
    LugarSugerido("Hotel Bah\u00EDa", PuntoMapa(-41.4742, -72.9425)),
    LugarSugerido("Terminal de Buses", PuntoMapa(-41.4708, -72.9340)),
    LugarSugerido("Costanera", PuntoMapa(-41.4770, -72.9435)),
    LugarSugerido("Hotel Lago", PuntoMapa(-41.1400, -72.9800))
)

private const val METROS_POR_GRADO = 111_320.0

fun distanciaKm(origen: PuntoMapa, destino: PuntoMapa): Double {
    val radioTierra = 6371.0
    val dLat = Math.toRadians(destino.latitud - origen.latitud)
    val dLon = Math.toRadians(destino.longitud - origen.longitud)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(origen.latitud)) * cos(Math.toRadians(destino.latitud)) *
            sin(dLon / 2) * sin(dLon / 2)
    return 2 * radioTierra * atan2(sqrt(a), sqrt(1 - a))
}

fun textoCoordenadas(punto: PuntoMapa): String =
    String.format(Locale.ROOT, "%.5f, %.5f", punto.latitud, punto.longitud)

fun textoDistancia(origen: PuntoMapa, destino: PuntoMapa): String {
    val km = distanciaKm(origen, destino)
    return if (km < 1.0) {
        "${(km * 1000).roundToInt()} m"
    } else {
        String.format(Locale.ROOT, "%.1f km", km)
    }
}

private suspend fun resolverDireccion(context: Context, punto: PuntoMapa): String? {
    if (!isInternetAvailable(context)) return null

    val desdeGeocoder = withContext(Dispatchers.IO) {
        runCatching {
            val geocoder = Geocoder(context, Locale("es", "CL"))
            @Suppress("DEPRECATION")
            geocoder.getFromLocation(punto.latitud, punto.longitud, 1)
                ?.firstOrNull()
                ?.let { direccion ->
                    val partes = listOfNotNull(
                        direccion.thoroughfare,
                        direccion.subThoroughfare?.let { numero -> " $numero" },
                        direccion.locality
                    )
                    partes.joinToString(", ").trim().ifBlank {
                        direccion.getAddressLine(0)
                    }
                }
        }.getOrNull()
    }

    if (!desdeGeocoder.isNullOrBlank()) return desdeGeocoder
    return resolverDireccionNominatim(punto)
}

private suspend fun resolverDireccionNominatim(punto: PuntoMapa): String? =
    withContext(Dispatchers.IO) {
        runCatching {
            val parametros = String.format(
                Locale.US,
                "lat=%.6f&lon=%.6f&format=jsonv2&zoom=18&addressdetails=1&accept-language=es",
                punto.latitud,
                punto.longitud
            )
            val conexion = (
                    URL("https://nominatim.openstreetmap.org/reverse?$parametros")
                        .openConnection() as HttpURLConnection
                    ).apply {
                    requestMethod = "GET"
                    setRequestProperty(
                        "User-Agent",
                        "LinceOPS/1.0 (Aplicacion Android de gestion turistica)"
                    )
                    setRequestProperty("Accept", "application/json")
                    connectTimeout = 5_000
                    readTimeout = 5_000
                }

            try {
                if (conexion.responseCode !in 200..299) return@runCatching null
                val cuerpo = conexion.inputStream.bufferedReader().use { it.readText() }
                val objeto = JSONObject(cuerpo)
                val direccion = objeto.optJSONObject("address")

                val via = direccion?.optString("road").orEmpty()
                val numero = direccion?.optString("house_number").orEmpty()
                val comuna = listOf(
                    "city",
                    "town",
                    "village",
                    "municipality",
                    "suburb",
                    "county"
                ).firstNotNullOfOrNull { clave ->
                    direccion?.optString(clave)?.takeIf { it.isNotBlank() }
                }.orEmpty()

                listOf(via, numero, comuna)
                    .filter { it.isNotBlank() }
                    .joinToString(", ")
                    .ifBlank { objeto.optString("display_name") }
                    .ifBlank { null }
            } finally {
                conexion.disconnect()
            }
        }.getOrNull()
    }

private suspend fun buscarDirecciones(
    consulta: String,
    limite: Int = 6
): List<ResultadoDireccion> =
    withContext(Dispatchers.IO) {
        runCatching {
            val parametros = "q=" + URLEncoder.encode(consulta, "UTF-8") +
                    "&format=json&limit=$limite&accept-language=es&countrycodes=cl"
            val conexion = (
                    URL("https://nominatim.openstreetmap.org/search?$parametros")
                        .openConnection() as HttpURLConnection
                    ).apply {
                    requestMethod = "GET"
                    setRequestProperty(
                        "User-Agent",
                        "LinceOPS/1.0 (Aplicacion Android de gestion turistica)"
                    )
                    setRequestProperty("Accept", "application/json")
                    connectTimeout = 8_000
                    readTimeout = 8_000
                }

            try {
                if (conexion.responseCode !in 200..299) return@runCatching emptyList()
                val cuerpo = conexion.inputStream.bufferedReader().use { it.readText() }
                val arreglo = JSONArray(cuerpo)

                buildList {
                    for (indice in 0 until arreglo.length()) {
                        val objeto = arreglo.optJSONObject(indice) ?: continue
                        val latitud = objeto.optString("lat").toDoubleOrNull() ?: continue
                        val longitud = objeto.optString("lon").toDoubleOrNull() ?: continue
                        val nombreCompleto =
                            objeto.optString("display_name").ifBlank { consulta }
                        val nombre = nombreCompleto
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .take(2)
                            .joinToString(", ")
                        add(
                            ResultadoDireccion(
                                nombre.ifBlank { consulta },
                                PuntoMapa(latitud, longitud)
                            )
                        )
                    }
                }
            } finally {
                conexion.disconnect()
            }
        }.getOrDefault(emptyList())
    }

private suspend fun geocodificarDireccion(
    context: Context,
    texto: String
): PuntoMapa? = withContext(Dispatchers.IO) {
    val limpio = texto.trim()
    if (limpio.isBlank()) return@withContext null

    if (isInternetAvailable(context)) {
        buscarDirecciones(limpio, limite = 1).firstOrNull()?.punto?.let { return@withContext it }
    }

    val desdeGeocoder = runCatching {
        val geocoder = Geocoder(context, Locale("es", "CL"))
        @Suppress("DEPRECATION")
        geocoder.getFromLocationName(
            limpio,
            1,
            -56.0,
            -76.0,
            -17.0,
            -66.0
        )?.firstOrNull()?.let { PuntoMapa(it.latitude, it.longitude) }
    }.getOrNull()

    if (desdeGeocoder != null) return@withContext desdeGeocoder

    LUGARES_SUGERIDOS.firstOrNull {
        it.nombre.contains(limpio, ignoreCase = true) || limpio.contains(it.nombre, ignoreCase = true)
    }?.punto
}

@SuppressLint("MissingPermission")
@Composable
fun SelectorUbicacionDialog(
    titulo: String,
    puntoInicial: PuntoMapa?,
    onDismiss: () -> Unit,
    onConfirmar: (PuntoMapa, String) -> Unit,
    direccionInicial: String? = null
) {
    val context = LocalContext.current
    val teclado = LocalSoftwareKeyboardController.current
    val foco = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val focusBuscador = remember { FocusRequester() }

    var hayRed by remember { mutableStateOf(isInternetAvailable(context)) }
    var centro by remember { mutableStateOf(puntoInicial ?: PUNTO_PUERTO_MONTT) }
    var zoomOsm by remember { mutableStateOf(16) }
    var direccion by remember { mutableStateOf(direccionInicial?.trim().orEmpty()) }
    var direccionManual by remember { mutableStateOf(false) }
    var buscandoDireccion by remember { mutableStateOf(false) }
    var geocodificando by remember { mutableStateOf(false) }
    var errorDireccion by remember { mutableStateOf<String?>(null) }
    var usandoGps by remember { mutableStateOf(false) }
    var saltarGeocodificacion by remember { mutableStateOf(false) }
    var disparadorGps by remember { mutableStateOf(0) }
    var ubicacionGps by remember { mutableStateOf<PuntoMapa?>(null) }
    var moviendoMapa by remember { mutableStateOf(false) }
    var errorTeselas by remember { mutableStateOf(false) }
    var atribucionMapa by remember {
        mutableStateOf("\u00A9 MapTiler \u2022 \u00A9 OpenStreetMap contributors")
    }
    var editandoDireccion by remember { mutableStateOf(false) }

    var consulta by remember { mutableStateOf("") }
    var resultados by remember { mutableStateOf<List<ResultadoDireccion>>(emptyList()) }
    var buscandoSugerencias by remember { mutableStateOf(false) }
    var busquedaRealizada by remember { mutableStateOf(false) }

    val clienteUbicacion = remember { LocationServices.getFusedLocationProviderClient(context) }
    val esOrigen = titulo.equals("origen", ignoreCase = true)
    val esParada = titulo.startsWith("parada", ignoreCase = true)
    val indiceParada = remember(titulo) {
        titulo.substringAfter(" ", "").toIntOrNull() ?: 1
    }
    val etiquetaPunto = when {
        esOrigen -> stringResource(R.string.mapa_punto_recogida)
        esParada -> stringResource(R.string.mapa_punto_parada)
        else -> stringResource(R.string.mapa_punto_destino)
    }
    val nombrePunto = when {
        esOrigen -> stringResource(R.string.crear_origen)
        esParada -> stringResource(R.string.crear_parada, indiceParada)
        else -> stringResource(R.string.crear_destino)
    }
    val tituloDialogo = stringResource(
        R.string.mapa_elegir_generico,
        nombrePunto.lowercase(Locale.getDefault())
    )

    val lanzadorPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val concedido = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (concedido) disparadorGps++
    }

    LaunchedEffect(direccionInicial, puntoInicial) {
        val textoApi = direccionInicial?.trim().orEmpty()
        if (puntoInicial == null && textoApi.isNotBlank()) {
            geocodificando = true
            errorDireccion = null
            val puntoGeocodificado = geocodificarDireccion(context, textoApi)
            if (puntoGeocodificado != null) {
                saltarGeocodificacion = true
                direccionManual = false
                centro = puntoGeocodificado
                direccion = textoApi
            } else {
                direccion = textoApi
            }
            geocodificando = false
        }
    }

    LaunchedEffect(Unit) {
        val permiso = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permiso == PackageManager.PERMISSION_GRANTED) {
            val token = CancellationTokenSource()
            clienteUbicacion.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                token.token
            ).addOnSuccessListener { ubicacion ->
                if (ubicacion != null) {
                    ubicacionGps = PuntoMapa(ubicacion.latitude, ubicacion.longitude)
                }
            }
        }
    }

    LaunchedEffect(disparadorGps) {
        if (disparadorGps == 0) return@LaunchedEffect
        val permiso = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permiso != PackageManager.PERMISSION_GRANTED) return@LaunchedEffect

        usandoGps = true
        val token = CancellationTokenSource()
        val tarea = clienteUbicacion.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            token.token
        )
        tarea.addOnSuccessListener { ubicacion ->
            if (ubicacion != null) {
                val punto = PuntoMapa(ubicacion.latitude, ubicacion.longitude)
                ubicacionGps = punto
                saltarGeocodificacion = false
                direccionManual = false
                errorDireccion = null
                centro = punto
            }
            usandoGps = false
        }
        tarea.addOnFailureListener { usandoGps = false }
    }

    LaunchedEffect(centro) {
        if (saltarGeocodificacion) {
            saltarGeocodificacion = false
            return@LaunchedEffect
        }
        buscandoDireccion = true
        try {
            delay(if (hayRed) 700L else 0L)
            if (!direccionManual) {
                val resuelta = withTimeoutOrNull(6_000L) {
                    resolverDireccion(context, centro)
                }
                if (!direccionManual) {
                    direccion = resuelta ?: textoCoordenadas(centro)
                }
            }
        } finally {
            buscandoDireccion = false
        }
    }

    fun centrarEnDireccion(texto: String, confirmarAlEncontrar: Boolean = false) {
        val limpio = texto.trim()
        if (limpio.isBlank() || geocodificando) return

        geocodificando = true
        errorDireccion = null
        scope.launch {
            val punto = geocodificarDireccion(context, limpio)
            if (punto != null) {
                saltarGeocodificacion = true
                direccionManual = false
                centro = punto
                direccion = limpio
                consulta = ""
                resultados = emptyList()
                busquedaRealizada = false
                editandoDireccion = false
                foco.clearFocus()
                teclado?.hide()
                if (confirmarAlEncontrar) onConfirmar(punto, limpio)
            } else {
                errorDireccion =
                    context.getString(R.string.mapa_error_no_encontrado, limpio)
            }
            geocodificando = false
        }
    }

    fun confirmarSeleccion() {
        val textoFinal = direccion.trim().ifBlank {
            consulta.trim().ifBlank { textoCoordenadas(centro) }
        }
        if (direccionManual && direccion.isNotBlank() && hayRed) {
            centrarEnDireccion(textoFinal, confirmarAlEncontrar = true)
        } else {
            onConfirmar(centro, textoFinal)
        }
    }

    fun solicitarUbicacion() {
        val permiso = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permiso == PackageManager.PERMISSION_GRANTED) {
            disparadorGps++
        } else {
            lanzadorPermiso.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            val conectado = isInternetAvailable(context)
            if (conectado != hayRed) {
                hayRed = conectado
                if (conectado) errorTeselas = false
            }
            delay(4_000)
        }
    }

    LaunchedEffect(hayRed, errorTeselas) {
        if (!hayRed || errorTeselas) moviendoMapa = false
    }

    LaunchedEffect(editandoDireccion) {
        if (editandoDireccion) {
            focusBuscador.requestFocus()
        }
    }

    LaunchedEffect(consulta, editandoDireccion) {
        if (!editandoDireccion) return@LaunchedEffect
        val texto = consulta.trim()
        busquedaRealizada = false
        if (texto.length < 3) {
            resultados = emptyList()
            buscandoSugerencias = false
            return@LaunchedEffect
        }

        delay(600)
        buscandoSugerencias = true
        if (hayRed) {
            resultados = buscarDirecciones(texto)
        } else {
            resultados = LUGARES_SUGERIDOS
                .filter { it.nombre.contains(texto, ignoreCase = true) }
                .map { ResultadoDireccion(it.nombre, it.punto) }
        }
        buscandoSugerencias = false
        busquedaRealizada = true
    }

    fun elegirResultado(resultado: ResultadoDireccion) {
        saltarGeocodificacion = true
        direccionManual = false
        errorDireccion = null
        centro = resultado.punto
        direccion = resultado.nombre
        consulta = ""
        resultados = emptyList()
        busquedaRealizada = false
        editandoDireccion = false
        foco.clearFocus()
        teclado?.hide()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = LinceCreamBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 12.dp, top = 14.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.mapa_ubicacion_servicio),
                            style = MaterialTheme.typography.labelSmall,
                            color = LinceTextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = tituloDialogo,
                            style = MaterialTheme.typography.headlineSmall,
                            color = LinceGreenPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.mapa_cerrar),
                            tint = LinceTextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(LinceRadius.xLarge))
                ) {
                    MapaMovil(
                        centro = centro,
                        zoom = (16f / zoomOsm).coerceIn(0.22f, 2.2f),
                        onCentroChange = {
                            if (!hayRed || errorTeselas) {
                                direccionManual = false
                                errorDireccion = null
                                centro = it
                            }
                        },
                        onMapMovingChange = { moviendoMapa = it },
                        puntoGps = ubicacionGps,
                        interactivo = !hayRed || errorTeselas,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (hayRed && !errorTeselas) {
                        MapaOsm(
                            centro = centro,
                            zoom = zoomOsm,
                            onCentroChange = { nuevo ->
                                direccionManual = false
                                errorDireccion = null
                                centro = nuevo
                            },
                            onZoomChange = { nuevoZoom ->
                                zoomOsm = nuevoZoom.coerceIn(3, 19)
                            },
                            puntoGps = ubicacionGps,
                            onMapMovingChange = { moviendoMapa = it },
                            onTileError = { errorTeselas = true },
                            onFuenteChange = { atribucionMapa = it },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    if (!hayRed || errorTeselas) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 12.dp, start = 16.dp, end = 16.dp)
                                .clickable(enabled = errorTeselas) { errorTeselas = false },
                            shape = RoundedCornerShape(50),
                            color = LinceCardSurface.copy(alpha = 0.94f),
                            shadowElevation = 3.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    start = LinceSpacing.small,
                                    end = LinceSpacing.small,
                                    top = LinceSpacing.xSmall,
                                    bottom = LinceSpacing.xSmall
                                ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(LinceTertiary, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (errorTeselas) {
                                        stringResource(R.string.mapa_teselas_error)
                                    } else {
                                        stringResource(R.string.mapa_sin_conexion)
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LinceGreenDark,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(LinceSpacing.small)
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable(enabled = !usandoGps && !geocodificando) {
                                solicitarUbicacion()
                            },
                        shape = CircleShape,
                        color = LinceTheme.colors.cardSurface,
                        border = BorderStroke(1.dp, LinceTheme.colors.outlineVariant),
                        shadowElevation = 2.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (usandoGps) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = LinceTheme.colors.greenPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = stringResource(R.string.mapa_centrar_ubicacion),
                                    tint = LinceTheme.colors.greenPrimary,
                                    modifier = Modifier.size(25.dp)
                                )
                            }
                        }
                    }

                    val elevacionPin by animateDpAsState(
                        targetValue = if (moviendoMapa) (-10).dp else 0.dp,
                        label = "elevacionPin"
                    )

                    Surface(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = (-112).dp + elevacionPin),
                        shape = RoundedCornerShape(LinceRadius.medium),
                        color = LinceTheme.colors.greenPrimary,
                        shadowElevation = 3.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(
                                start = LinceSpacing.mediumSmall,
                                end = LinceSpacing.mediumSmall,
                                top = LinceSpacing.small,
                                bottom = LinceSpacing.small
                            ),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (moviendoMapa || buscandoDireccion) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = LinceOnPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.mapa_buscando_direccion),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = LinceOnPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                Text(
                                    text = etiquetaPunto.uppercase(Locale.ROOT),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LinceOnPrimary.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = direccion.ifBlank { textoCoordenadas(centro) },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LinceOnPrimary,
                                    fontWeight = FontWeight.Bold,
                                    softWrap = true,
                                    maxLines = 3,
                                    overflow = TextOverflow.Clip
                                )
                            }
                        }
                    }

                    val colorPin = LinceTheme.colors.greenPrimary
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = (-28).dp + elevacionPin),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(46.dp),
                            shape = CircleShape,
                            color = LinceTheme.colors.greenPrimary,
                            border = BorderStroke(3.dp, LinceTheme.colors.onPrimary),
                            shadowElevation = 6.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = stringResource(R.string.mapa_punto_seleccionado),
                                    tint = LinceTheme.colors.onPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Canvas(modifier = Modifier.size(width = 14.dp, height = 11.dp)) {
                            val aguja = Path().apply {
                                moveTo(size.width / 2f, size.height)
                                lineTo(0f, 0f)
                                lineTo(size.width, 0f)
                                close()
                            }
                            drawPath(aguja, colorPin)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(width = 14.dp, height = 5.dp)
                            .background(LinceTheme.colors.greenDark.copy(alpha = 0.28f), CircleShape)
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BotonMapa(texto = "+") {
                            zoomOsm = (zoomOsm + 1).coerceAtMost(19)
                        }
                        BotonMapa(texto = "-") {
                            zoomOsm = (zoomOsm - 1).coerceAtLeast(3)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 64.dp)
                            .widthIn(max = 240.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = LinceCardSurface.copy(alpha = 0.88f)
                    ) {
                        Text(
                            text = atribucionMapa,
                            style = MaterialTheme.typography.labelSmall,
                            color = LinceTextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(
                                start = LinceSpacing.xSmall + 2.dp,
                                end = LinceSpacing.xSmall + 2.dp,
                                top = 2.dp,
                                bottom = 2.dp
                            )
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = LinceSpacing.small),
                    shape = RoundedCornerShape(
                        topStart = LinceRadius.xLarge,
                        topEnd = LinceRadius.xLarge
                    ),
                    color = LinceTheme.colors.cardSurface,
                    border = BorderStroke(1.dp, LinceTheme.colors.outlineVariant),
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = LinceSpacing.medium,
                                end = LinceSpacing.medium,
                                top = LinceSpacing.small,
                                bottom = LinceSpacing.medium
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .size(width = 40.dp, height = 4.dp)
                                .background(LinceTheme.colors.outlineVariant, RoundedCornerShape(50))
                        )
                        Spacer(modifier = Modifier.height(LinceSpacing.small))
                        Text(
                            text = stringResource(
                                R.string.mapa_fija_punto,
                                nombrePunto.lowercase(Locale.getDefault())
                            ),
                            style = MaterialTheme.typography.titleSmall,
                            color = LinceTheme.colors.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            softWrap = true,
                            maxLines = 2,
                            overflow = TextOverflow.Clip
                        )
                        Text(
                            text = stringResource(R.string.mapa_arrastra),
                            style = MaterialTheme.typography.bodySmall,
                            color = LinceTheme.colors.textSecondary,
                            softWrap = true
                        )

                        Spacer(modifier = Modifier.height(LinceSpacing.small))

                        if (editandoDireccion) {
                            OutlinedTextField(
                                value = consulta,
                                onValueChange = {
                                    consulta = it
                                    errorDireccion = null
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusBuscador),
                                singleLine = true,
                                shape = RoundedCornerShape(LinceRadius.medium),
                                placeholder = {
                                    Text(
                                        text = stringResource(R.string.mapa_busca_placeholder),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = LinceTextSecondary
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = LinceGreenPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                trailingIcon = {
                                    when {
                                        buscandoSugerencias || geocodificando -> CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = LinceGreenPrimary
                                        )

                                        else -> IconButton(
                                            onClick = {
                                                editandoDireccion = false
                                                consulta = ""
                                                resultados = emptyList()
                                                busquedaRealizada = false
                                                foco.clearFocus()
                                                teclado?.hide()
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = stringResource(R.string.mapa_cerrar_buscador),
                                                tint = LinceTextSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                },
                                textStyle = MaterialTheme.typography.bodyMedium,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        val texto = consulta.trim()
                                        if (texto.isNotBlank()) {
                                            val primero = resultados.firstOrNull()
                                            if (primero != null) {
                                                elegirResultado(primero)
                                            } else {
                                                centrarEnDireccion(texto)
                                            }
                                        }
                                    }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = LinceTheme.colors.surfaceVariant,
                                    unfocusedContainerColor = LinceTheme.colors.surfaceVariant,
                                    focusedBorderColor = LinceTheme.colors.greenPrimary,
                                    unfocusedBorderColor = LinceTheme.colors.outlineVariant,
                                    focusedTextColor = LinceTheme.colors.textPrimary,
                                    unfocusedTextColor = LinceTheme.colors.textPrimary,
                                    cursorColor = LinceTheme.colors.greenPrimary
                                )
                            )

                            if (resultados.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(LinceSpacing.xSmall))
                                Surface(
                                    shape = RoundedCornerShape(LinceRadius.medium),
                                    color = LinceTheme.colors.surfaceVariant,
                                    border = BorderStroke(1.dp, LinceTheme.colors.outlineVariant)
                                ) {
                                    LazyColumn(modifier = Modifier.heightIn(max = 190.dp)) {
                                        items(resultados) { resultado ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { elegirResultado(resultado) }
                                                    .padding(
                                                        start = LinceSpacing.small,
                                                        end = LinceSpacing.small,
                                                        top = LinceSpacing.small,
                                                        bottom = LinceSpacing.small
                                                    ),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.LocationOn,
                                                    contentDescription = null,
                                                     tint = LinceTheme.colors.greenPrimary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = resultado.nombre,
                                                    style = MaterialTheme.typography.bodySmall,
                                                     color = LinceTheme.colors.textPrimary,
                                                     softWrap = true,
                                                     maxLines = 3,
                                                     overflow = TextOverflow.Clip,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = textoDistancia(centro, resultado.punto),
                                                    style = MaterialTheme.typography.labelSmall,
                                                     color = LinceTheme.colors.greenDark,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            } else if (busquedaRealizada && consulta.trim().length >= 3) {
                                Spacer(modifier = Modifier.height(LinceSpacing.xSmall))
                                Surface(
                                    shape = RoundedCornerShape(LinceRadius.small),
                                    color = LinceTheme.colors.surfaceVariant,
                                    border = BorderStroke(1.dp, LinceTheme.colors.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { centrarEnDireccion(consulta.trim()) }
                                            .padding(
                                                start = LinceSpacing.small,
                                                end = LinceSpacing.small,
                                                top = LinceSpacing.small,
                                                bottom = LinceSpacing.small
                                            ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = LinceTheme.colors.greenPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.mapa_buscar_usar, consulta.trim()),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = LinceTheme.colors.greenDark,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        } else {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        consulta = if (direccion == textoCoordenadas(centro)) "" else direccion
                                        editandoDireccion = true
                                    },
                                shape = RoundedCornerShape(LinceRadius.medium),
                                color = LinceTheme.colors.surfaceVariant,
                                border = BorderStroke(1.dp, LinceTheme.colors.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        start = LinceSpacing.mediumSmall,
                                        end = LinceSpacing.mediumSmall,
                                        top = LinceSpacing.small,
                                        bottom = LinceSpacing.small
                                    ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = LinceTheme.colors.greenPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(R.string.mapa_direccion_marcador),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = LinceTheme.colors.textSecondary,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.6.sp
                                        )
                                        Text(
                                            text = if (buscandoDireccion) {
                                                stringResource(R.string.mapa_ubicacion_marcador)
                                            } else {
                                                direccion.ifBlank {
                                                    stringResource(R.string.mapa_ubicacion_marcador)
                                                }
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = LinceTheme.colors.textPrimary,
                                            softWrap = true,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 3,
                                            overflow = TextOverflow.Clip
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = stringResource(R.string.mapa_editar_direccion),
                                        tint = LinceTheme.colors.greenPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        errorDireccion?.let { mensaje ->
                            Spacer(modifier = Modifier.height(LinceSpacing.xSmall))
                            Text(
                                text = mensaje,
                                style = MaterialTheme.typography.bodySmall,
                                color = LinceTheme.colors.error,
                                softWrap = true
                            )
                        }

                        Spacer(modifier = Modifier.height(LinceSpacing.small))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LinceButton(
                                text = if (geocodificando) {
                                    stringResource(R.string.mapa_buscando_direccion)
                                } else {
                                    stringResource(
                                        R.string.mapa_confirmar_punto,
                                        nombrePunto.lowercase(Locale.getDefault())
                                    )
                                },
                                onClick = { confirmarSeleccion() },
                                modifier = Modifier.fillMaxWidth(),
                                variant = LinceButtonVariant.Primary,
                                enabled = !geocodificando,
                                fullWidth = true
                            )
                            if (geocodificando) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .padding(start = LinceSpacing.medium)
                                        .size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = LinceTheme.colors.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BotonMapa(texto: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = LinceTheme.colors.cardSurface,
        border = BorderStroke(1.dp, LinceTheme.colors.outlineVariant),
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = texto,
                style = MaterialTheme.typography.titleMedium,
                color = LinceTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MapaMovil(
    centro: PuntoMapa,
    zoom: Float,
    onCentroChange: (PuntoMapa) -> Unit,
    modifier: Modifier = Modifier,
    onMapMovingChange: (Boolean) -> Unit = {},
    puntoGps: PuntoMapa? = null,
    interactivo: Boolean = true
) {
    val centroActual by rememberUpdatedState(centro)
    val metrosPorPixel = 9.0 * zoom
    val gesto = if (interactivo) {
        Modifier.pointerInput(zoom) {
            detectDragGestures(
                onDragStart = { onMapMovingChange(true) },
                onDragEnd = { onMapMovingChange(false) },
                onDragCancel = { onMapMovingChange(false) }
            ) { cambio, arrastre ->
                cambio.consume()
                val actual = centroActual
                val metrosPorGradoLongitud =
                    METROS_POR_GRADO * cos(Math.toRadians(actual.latitud))
                val nuevaLat = actual.latitud + arrastre.y * metrosPorPixel / METROS_POR_GRADO
                val nuevaLon = actual.longitud -
                        arrastre.x * metrosPorPixel / metrosPorGradoLongitud
                onCentroChange(
                    PuntoMapa(
                        latitud = nuevaLat.coerceIn(-56.0, -17.0),
                        longitud = normalizarLongitud(nuevaLon)
                    )
                )
            }
        }
    } else {
        Modifier
    }

    val coloresMapa = LinceTheme.colors
    Canvas(
        modifier = modifier
            .background(LinceMapLand)
            .then(gesto)
    ) {
        val ancho = size.width
        val alto = size.height
        val metrosPorGradoLongitud = METROS_POR_GRADO * cos(Math.toRadians(centro.latitud))

        fun x(lon: Double): Float =
            (ancho / 2f + ((lon - centro.longitud) * metrosPorGradoLongitud / metrosPorPixel).toFloat())

        fun y(lat: Double): Float =
            (alto / 2f - ((lat - centro.latitud) * METROS_POR_GRADO / metrosPorPixel).toFloat())

        drawRect(color = coloresMapa.mapLand)

        val pasoLat = 0.0038
        val pasoLon = 0.0046
        val latMin = centro.latitud - (alto / 2f * metrosPorPixel / METROS_POR_GRADO)
        val latMax = centro.latitud + (alto / 2f * metrosPorPixel / METROS_POR_GRADO)
        val lonMin = centro.longitud - (ancho / 2f * metrosPorPixel / metrosPorGradoLongitud)
        val lonMax = centro.longitud + (ancho / 2f * metrosPorPixel / metrosPorGradoLongitud)

        val iInicio = floor(latMin / pasoLat).toInt()
        val iFin = ceil(latMax / pasoLat).toInt()
        val jInicio = floor(lonMin / pasoLon).toInt()
        val jFin = ceil(lonMax / pasoLon).toInt()

        val anchoCeldaPx = (pasoLon * metrosPorGradoLongitud / metrosPorPixel).toFloat()
        val altoCeldaPx = (pasoLat * METROS_POR_GRADO / metrosPorPixel).toFloat()

        for (i in iInicio..iFin) {
            for (j in jInicio..jFin) {
                val semilla = ((i * 37 + j * 53) % 11 + 11) % 11
                if (semilla < 3) continue

                val lat = i * pasoLat + pasoLat * 0.5
                val lon = j * pasoLon + pasoLon * 0.5
                val anchoPx = anchoCeldaPx * (0.54f + (semilla % 4) * 0.07f)
                val altoPx = altoCeldaPx * (0.46f + (semilla % 3) * 0.08f)

                drawRoundRect(
                    color = coloresMapa.mapBlock,
                    topLeft = Offset(x(lon) - anchoPx / 2f, y(lat) - altoPx / 2f),
                    size = Size(anchoPx, altoPx),
                    cornerRadius = CornerRadius(2.5.dp.toPx())
                )

                if (semilla % 4 == 0) {
                    drawRoundRect(
                        color = coloresMapa.mapLand.copy(alpha = 0.55f),
                        topLeft = Offset(
                            x(lon) - anchoPx * 0.28f,
                            y(lat) - altoPx * 0.26f
                        ),
                        size = Size(anchoPx * 0.56f, altoPx * 0.52f),
                        cornerRadius = CornerRadius(1.5.dp.toPx())
                    )
                }
            }
        }

        listOf(
            PuntoMapa(-41.4726, -72.9425),
            PuntoMapa(-41.4687, -72.9368),
            PuntoMapa(-41.4758, -72.9460)
        ).forEach { parque ->
            drawRoundRect(
                color = coloresMapa.mapPark,
                topLeft = Offset(x(parque.longitud) - 26.dp.toPx(), y(parque.latitud) - 18.dp.toPx()),
                size = Size(52.dp.toPx(), 36.dp.toPx()),
                cornerRadius = CornerRadius(10.dp.toPx())
            )
        }

        for (i in iInicio..iFin + 1) {
            val yy = y(i * pasoLat)
            if (yy < -20f || yy > alto + 20f) continue
            val avenida = i % 4 == 0
            drawLine(
                color = coloresMapa.mapRoadEdge,
                start = Offset(0f, yy),
                end = Offset(ancho, yy),
                strokeWidth = if (avenida) 9.dp.toPx() else 5.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = coloresMapa.mapRoad,
                start = Offset(0f, yy),
                end = Offset(ancho, yy),
                strokeWidth = if (avenida) 7.dp.toPx() else 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        for (j in jInicio..jFin + 1) {
            val xx = x(j * pasoLon)
            if (xx < -20f || xx > ancho + 20f) continue
            val avenida = j % 5 == 0
            drawLine(
                color = coloresMapa.mapRoadEdge,
                start = Offset(xx, 0f),
                end = Offset(xx, alto),
                strokeWidth = if (avenida) 9.dp.toPx() else 5.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = coloresMapa.mapRoad,
                start = Offset(xx, 0f),
                end = Offset(xx, alto),
                strokeWidth = if (avenida) 7.dp.toPx() else 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        val camino = Path().apply {
            moveTo(0f, alto * 0.78f)
            cubicTo(
                ancho * 0.3f, alto * 0.62f,
                ancho * 0.65f, alto * 0.52f,
                ancho, alto * 0.4f
            )
        }
        drawPath(
            camino,
            coloresMapa.mapRoadEdge,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 11.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
        drawPath(
            camino,
            coloresMapa.mapRoad,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 8.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        val costaBase = -72.9560
        val xCosta = x(costaBase)
        if (xCosta > -4f && xCosta < ancho + 4f) {
            val costa = Path()
            costa.moveTo(0f, -10f)
            var yy = -10f
            while (yy <= alto + 10f) {
                val desvio = 0.0016 * sin((yy / alto) * 2.4 * PI + 0.6)
                costa.lineTo(x(costaBase + desvio), yy)
                yy += 16f
            }
            costa.lineTo(0f, alto + 10f)
            costa.close()
            drawPath(costa, coloresMapa.mapWater)

            val borde = Path()
            borde.moveTo(x(costaBase + 0.0016 * sin(0.6)), 0f)
            var yBorde = 0f
            while (yBorde <= alto) {
                val desvio = 0.0016 * sin((yBorde / alto) * 2.4 * PI + 0.6)
                borde.lineTo(x(costaBase + desvio), yBorde)
                yBorde += 16f
            }
            drawPath(
                borde,
                coloresMapa.mapWater.copy(alpha = 0.85f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
            )
        }

        puntoGps?.let { gps ->
            val xg = x(gps.longitud)
            val yg = y(gps.latitud)
            if (xg in -24f..ancho + 24f && yg in -24f..alto + 24f) {
                drawCircle(
                    color = coloresMapa.mapGpsHalo,
                    radius = 12.dp.toPx(),
                    center = Offset(xg, yg)
                )
                drawCircle(
                    color = coloresMapa.mapGpsBlue,
                    radius = 8.dp.toPx(),
                    center = Offset(xg, yg)
                )
            }
        }
    }
}

private fun normalizarLongitud(valor: Double): Double {
    var resultado = valor
    while (resultado > 180.0) resultado -= 360.0
    while (resultado < -180.0) resultado += 360.0
    return resultado
}
