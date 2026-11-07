package com.example.linceops.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
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
import com.example.linceops.ui.theme.LinceSurfaceContainerLow
import com.example.linceops.ui.theme.LinceSurfaceVariant
import com.example.linceops.ui.theme.LinceTertiary
import com.example.linceops.ui.theme.LinceTextPrimary
import com.example.linceops.ui.theme.LinceTextSecondary
import com.example.linceops.utils.isInternetAvailable
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt

/** Coordenada simple para el selector de mapa. */
data class PuntoMapa(val latitud: Double, val longitud: Double)

data class LugarSugerido(val nombre: String, val punto: PuntoMapa)

data class ResultadoDireccion(val nombre: String, val punto: PuntoMapa)

val PUNTO_PUERTO_MONTT = PuntoMapa(-41.4717, -72.9397)

val LUGARES_SUGERIDOS = listOf(
    LugarSugerido("Aeropuerto El Tepual", PuntoMapa(-41.4389, -73.0940)),
    LugarSugerido("Hotel Centro", PuntoMapa(-41.4717, -72.9397)),
    LugarSugerido("Hotel Bahía", PuntoMapa(-41.4742, -72.9425)),
    LugarSugerido("Terminal de Buses", PuntoMapa(-41.4708, -72.9340)),
    LugarSugerido("Costanera", PuntoMapa(-41.4770, -72.9435)),
    LugarSugerido("Hotel Lago", PuntoMapa(-41.1400, -72.9800))
)

private const val METROS_POR_GRADO = 111_320.0

/** Distancia aproximada entre dos puntos (fórmula de Haversine). */
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

private suspend fun resolverDireccion(context: Context, punto: PuntoMapa): String? =
    withContext(Dispatchers.IO) {
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
                        // display_name trae calle, comuna, provincia, región y país.
                        // Guardamos solo los dos primeros tramos para que quepan en el formulario.
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

/**
 * Convierte un texto libre en coordenadas. Intentar Nominatim primero y, si no
 * hay resultados o no hay red, usa el Geocoder del sistema acotado a Chile.
 */
private suspend fun geocodificarDireccion(
    context: Context,
    texto: String
): PuntoMapa? = withContext(Dispatchers.IO) {
    val desdeNominatim = buscarDirecciones(texto, limite = 1).firstOrNull()?.punto
    desdeNominatim ?: runCatching {
        val geocoder = Geocoder(context, Locale("es", "CL"))
        @Suppress("DEPRECATION")
        geocoder.getFromLocationName(
            texto,
            1,
            -56.0,
            -76.0,
            -17.0,
            -66.0
        )?.firstOrNull()?.let { PuntoMapa(it.latitude, it.longitude) }
    }.getOrNull()
}

@SuppressLint("MissingPermission")
@Composable
fun SelectorUbicacionDialog(
    titulo: String,
    puntoInicial: PuntoMapa?,
    onDismiss: () -> Unit,
    onConfirmar: (PuntoMapa, String) -> Unit
) {
    val context = LocalContext.current
    val teclado = LocalSoftwareKeyboardController.current
    val foco = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    var hayRed by remember { mutableStateOf(isInternetAvailable(context)) }

    var centro by remember { mutableStateOf(puntoInicial ?: PUNTO_PUERTO_MONTT) }
    var zoomOsm by remember { mutableStateOf(16) }
    var direccion by remember { mutableStateOf("") }
    var direccionManual by remember { mutableStateOf(false) }
    var buscandoDireccion by remember { mutableStateOf(false) }
    var geocodificando by remember { mutableStateOf(false) }
    var errorDireccion by remember { mutableStateOf<String?>(null) }
    var usandoGps by remember { mutableStateOf(false) }
    var saltarGeocodificacion by remember { mutableStateOf(false) }
    var disparadorGps by remember { mutableStateOf(0) }

    var consulta by remember { mutableStateOf("") }
    var resultados by remember { mutableStateOf<List<ResultadoDireccion>>(emptyList()) }
    var buscandoSugerencias by remember { mutableStateOf(false) }
    var busquedaRealizada by remember { mutableStateOf(false) }

    val clienteUbicacion = remember { LocationServices.getFusedLocationProviderClient(context) }

    val lanzadorPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val concedido = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (concedido) disparadorGps++
    }

    LaunchedEffect(Unit) {
        val permiso = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permiso == PackageManager.PERMISSION_GRANTED) {
            disparadorGps++
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
                saltarGeocodificacion = false
                direccionManual = false
                errorDireccion = null
                centro = PuntoMapa(ubicacion.latitude, ubicacion.longitude)
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
        delay(600)
        // Si el usuario escribió una dirección, no la pisamos con la geocodificación inversa.
        if (direccionManual) {
            buscandoDireccion = false
            return@LaunchedEffect
        }
        val resuelta = resolverDireccion(context, centro)
        if (!direccionManual) {
            direccion = resuelta ?: textoCoordenadas(centro)
        }
        buscandoDireccion = false
    }

    /**
     * Geocodifica un texto y mueve el pin a ese punto. Se usa al elegir una
     * sugerencia del buscador o al confirmar una dirección escrita a mano.
     */
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
                foco.clearFocus()
                teclado?.hide()
                if (confirmarAlEncontrar) onConfirmar(punto, limpio)
            } else {
                errorDireccion =
                    "No encontramos \"$limpio\". Prueba con calle y comuna, o mueve el mapa."
            }
            geocodificando = false
        }
    }

    fun confirmarSeleccion() {
        val textoFinal = direccion.trim().ifBlank {
            consulta.trim().ifBlank { textoCoordenadas(centro) }
        }

        // Si el usuario escribió una dirección distinta a la detectada, el pin
        // debe moverse a esa dirección antes de guardar.
        if (direccionManual && direccion.isNotBlank()) {
            centrarEnDireccion(textoFinal, confirmarAlEncontrar = true)
        } else {
            onConfirmar(centro, textoFinal)
        }
    }

    LaunchedEffect(consulta) {
        val texto = consulta.trim()
        busquedaRealizada = false
        if (texto.length < 3) {
            resultados = emptyList()
            buscandoSugerencias = false
            return@LaunchedEffect
        }

        delay(450)
        buscandoSugerencias = true
        resultados = buscarDirecciones(texto)
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
                // Encabezado
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 12.dp, top = 14.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "UBICACIÓN DEL SERVICIO",
                            style = MaterialTheme.typography.labelSmall,
                            color = LinceTextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Elegir $titulo",
                            style = MaterialTheme.typography.headlineSmall,
                            color = LinceGreenPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = LinceTextSecondary
                        )
                    }
                }

                // Mapa
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    if (hayRed) {
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
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        MapaMovil(
                            centro = centro,
                            zoom = (16f / zoomOsm).coerceIn(0.22f, 2.2f),
                            onCentroChange = { nuevo ->
                                direccionManual = false
                                errorDireccion = null
                                centro = nuevo
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Si se abrió el diálogo sin red, permite reintentar sin cerrarlo.
                    if (!hayRed) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                                .clickable {
                                    if (isInternetAvailable(context)) hayRed = true
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = LinceCardSurface.copy(alpha = 0.94f),
                            shadowElevation = 3.dp
                        ) {
                            Text(
                                text = "Sin conexión · Reintentar",
                                style = MaterialTheme.typography.labelSmall,
                                color = LinceGreenDark,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Pin fijo en el centro: la punta queda exactamente sobre el punto elegido.
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = (-23).dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(46.dp),
                            shape = CircleShape,
                            color = LinceCardSurface,
                            shadowElevation = 6.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Punto seleccionado",
                                    tint = LinceGreenPrimary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(6.dp)
                            .background(LinceGreenDark.copy(alpha = 0.55f), CircleShape)
                    )

                    // Buscador de direcciones sobre el mapa
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = LinceCardSurface,
                            shadowElevation = 4.dp
                        ) {
                            OutlinedTextField(
                                value = consulta,
                                onValueChange = { consulta = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                placeholder = {
                                    Text(
                                        text = "Buscar dirección en el mapa",
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

                                        consulta.isNotEmpty() -> IconButton(
                                            onClick = {
                                                consulta = ""
                                                resultados = emptyList()
                                                busquedaRealizada = false
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Borrar búsqueda",
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
                                        foco.clearFocus()
                                        teclado?.hide()
                                    }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = LinceCardSurface,
                                    unfocusedContainerColor = LinceCardSurface,
                                    focusedBorderColor = LinceGreenPrimary,
                                    unfocusedBorderColor = Color.Transparent,
                                    cursorColor = LinceGreenPrimary
                                )
                            )
                        }

                        if (resultados.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = LinceCardSurface,
                                shadowElevation = 6.dp
                            ) {
                                LazyColumn(modifier = Modifier.heightIn(max = 220.dp)) {
                                    items(resultados) { resultado ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { elegirResultado(resultado) }
                                                .padding(horizontal = 14.dp, vertical = 11.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = LinceGreenPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = resultado.nombre,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = LinceTextPrimary,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        } else if (busquedaRealizada && consulta.trim().length >= 3) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = LinceCardSurface.copy(alpha = 0.96f),
                                shadowElevation = 3.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { centrarEnDireccion(consulta.trim()) }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = LinceGreenPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Buscar y usar: \"${consulta.trim()}\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LinceGreenDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    // Controles de Zoom
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BotonMapa(texto = "+") {
                            zoomOsm = (zoomOsm + 1).coerceAtMost(19)
                        }
                        BotonMapa(texto = "−") {
                            zoomOsm = (zoomOsm - 1).coerceAtLeast(3)
                        }
                    }

                    // Atribución
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = LinceCardSurface.copy(alpha = 0.88f)
                    ) {
                        Text(
                            text = "© OpenStreetMap • CARTO",
                            style = MaterialTheme.typography.labelSmall,
                            color = LinceTextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Panel inferior con campo editable de dirección y sugerencias rápidas
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
                    color = LinceCardSurface,
                    shadowElevation = 18.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .size(width = 40.dp, height = 4.dp)
                                .background(LinceOutlineVariant, RoundedCornerShape(50))
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PUNTO DE ${titulo.uppercase(Locale.ROOT)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = LinceTextSecondary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = textoCoordenadas(centro),
                                style = MaterialTheme.typography.labelSmall,
                                color = LinceTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Campo editable: si el usuario escribe una dirección distinta,
                        // al confirmar se geocodifica y el pin se mueve a ese punto.
                        OutlinedTextField(
                            value = direccion,
                            onValueChange = {
                                direccion = it
                                direccionManual = it.isNotBlank()
                                errorDireccion = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            placeholder = {
                                Text(
                                    text = if (buscandoDireccion) "Detectando dirección..." else "Escribe aquí la dirección exacta...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = LinceTextSecondary
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = LinceGreenPrimary
                                )
                            },
                            trailingIcon = {
                                when {
                                    buscandoDireccion || geocodificando -> CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = LinceGreenPrimary
                                    )
                                    direccion.isNotEmpty() -> IconButton(
                                        onClick = {
                                            direccion = ""
                                            direccionManual = false
                                            errorDireccion = null
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Limpiar dirección",
                                            tint = LinceTextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    foco.clearFocus()
                                    teclado?.hide()
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LinceGreenPrimary,
                                unfocusedBorderColor = LinceGreenBorder,
                                focusedContainerColor = LinceCreamBackground.copy(alpha = 0.45f),
                                unfocusedContainerColor = LinceCreamBackground.copy(alpha = 0.45f)
                            )
                        )

                        errorDireccion?.let { mensaje ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = mensaje,
                                style = MaterialTheme.typography.bodySmall,
                                color = LinceTertiary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LUGARES_SUGERIDOS.forEach { lugar ->
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = LinceSurfaceVariant,
                                    modifier = Modifier.clickable {
                                        saltarGeocodificacion = true
                                        direccionManual = false
                                        errorDireccion = null
                                        centro = lugar.punto
                                        direccion = lugar.nombre
                                    }
                                ) {
                                    Text(
                                        text = lugar.nombre,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = LinceTextPrimary,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
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
                                },
                                modifier = Modifier.height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                enabled = !usandoGps && !geocodificando
                            ) {
                                if (usandoGps) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = LinceGreenPrimary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = LinceGreenPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (usandoGps) "Ubicando..." else "Mi ubicación",
                                    color = LinceGreenDark
                                )
                            }

                            Button(
                                onClick = { confirmarSeleccion() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                enabled = !geocodificando,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LinceGreenPrimary,
                                    disabledContainerColor = LinceGreenPrimary.copy(alpha = 0.45f)
                                )
                            ) {
                                if (geocodificando) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = LinceCardSurface
                                    )
                                } else {
                                    Text(
                                        text = if (direccionManual) "Buscar y usar" else "Usar este punto",
                                        color = LinceCardSurface,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
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
            .size(38.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = LinceCardSurface.copy(alpha = 0.94f),
        shadowElevation = 3.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = texto,
                style = MaterialTheme.typography.titleMedium,
                color = LinceGreenDark,
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
    modifier: Modifier = Modifier
) {
    val centroActual by rememberUpdatedState(centro)
    val metrosPorPixel = 9.0 * zoom

    Canvas(
        modifier = modifier
            .background(LinceMapLand)
            .pointerInput(zoom) {
                detectDragGestures(
                    onDragEnd = { },
                    onDragCancel = { }
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
    ) {
        val ancho = size.width
        val alto = size.height
        val metrosPorGradoLongitud = METROS_POR_GRADO * cos(Math.toRadians(centro.latitud))

        fun x(lon: Double): Float =
            (ancho / 2f + ((lon - centro.longitud) * metrosPorGradoLongitud / metrosPorPixel).toFloat())

        fun y(lat: Double): Float =
            (alto / 2f - ((lat - centro.latitud) * METROS_POR_GRADO / metrosPorPixel).toFloat())

        drawRect(color = LinceMapLand)

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
                    color = LinceMapBlock,
                    topLeft = Offset(x(lon) - anchoPx / 2f, y(lat) - altoPx / 2f),
                    size = Size(anchoPx, altoPx),
                    cornerRadius = CornerRadius(2.5.dp.toPx())
                )

                if (semilla % 4 == 0) {
                    drawRoundRect(
                        color = LinceMapLand.copy(alpha = 0.55f),
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
                color = LinceMapPark,
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
                color = LinceMapRoadEdge,
                start = Offset(0f, yy),
                end = Offset(ancho, yy),
                strokeWidth = if (avenida) 9.dp.toPx() else 5.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = LinceMapRoad,
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
                color = LinceMapRoadEdge,
                start = Offset(xx, 0f),
                end = Offset(xx, alto),
                strokeWidth = if (avenida) 9.dp.toPx() else 5.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = LinceMapRoad,
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
        drawPath(camino, LinceMapRoadEdge, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 11.dp.toPx(), cap = StrokeCap.Round))
        drawPath(camino, LinceMapRoad, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round))

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
            drawPath(costa, LinceMapWater)

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
                LinceMapWater.copy(alpha = 0.85f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
            )
        }
    }
}

private fun normalizarLongitud(valor: Double): Double {
    var resultado = valor
    while (resultado > 180.0) resultado -= 360.0
    while (resultado < -180.0) resultado += 360.0
    return resultado
}

/** Fila compacta para el formulario: muestra el punto elegido o la invitación a elegirlo. */
@Composable
fun FilaUbicacion(
    etiqueta: String,
    valor: String,
    esDestino: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = LinceSurfaceContainerLow
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (esDestino) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(LinceTertiary, RoundedCornerShape(3.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(LinceGreenPrimary, CircleShape)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = etiqueta.uppercase(Locale.ROOT),
                    style = MaterialTheme.typography.labelSmall,
                    color = LinceTextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = valor.ifBlank { "Elegir en el mapa o escribir" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (valor.isBlank()) LinceTextSecondary else LinceTextPrimary,
                    fontWeight = if (valor.isBlank()) FontWeight.Normal else FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (valor.isBlank()) "Elegir" else "Cambiar",
                style = MaterialTheme.typography.labelMedium,
                color = LinceGreenPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}