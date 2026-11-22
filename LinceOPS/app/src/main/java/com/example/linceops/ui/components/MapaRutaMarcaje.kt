package com.example.linceops.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.view.MotionEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.linceops.R
import com.example.linceops.ui.theme.LinceRadius
import com.example.linceops.ui.theme.LinceSpacing
import com.example.linceops.ui.theme.LinceTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import org.json.JSONObject
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.roundToInt

private data class RutaMarcaje(
    val puntos: List<PuntoMapa>,
    val distanciaMetros: Double,
    val duracionSegundos: Double
)

private class EstadoMapaRutaMarcaje {
    var mapa: MapView? = null
    var marcadorObjetivo: Marker? = null
    var marcadorUsuario: Marker? = null
    var circuloRadio: Polygon? = null
    var lineaDirecta: Polyline? = null
    var lineaRutaHalo: Polyline? = null
    var lineaRuta: Polyline? = null
    var ultimoObjetivo: PuntoMapa? = null
    var ultimoUsuario: PuntoMapa? = null
    var ultimoRadio: Int? = null
    var ultimaRuta: RutaMarcaje? = null
}

@Composable
fun MapaRutaMarcaje(
    puntoObjetivo: PuntoMapa,
    puntoUsuario: PuntoMapa?,
    radioMetros: Int,
    contextoPunto: String,
    distanciaDirecta: String? = null,
    modifier: Modifier = Modifier
) {
    val contexto = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val colores = LinceTheme.colors
    val estado = remember { EstadoMapaRutaMarcaje() }
    val cacheRutas = remember { mutableMapOf<String, RutaMarcaje>() }
    var ruta by remember(puntoObjetivo) { mutableStateOf<RutaMarcaje?>(null) }
    var cargandoRuta by remember(puntoObjetivo) { mutableStateOf(false) }

    val objetivoValido = puntoObjetivo.latitud.isFinite() &&
            puntoObjetivo.longitud.isFinite() &&
            puntoObjetivo.latitud in -90.0..90.0 &&
            puntoObjetivo.longitud in -180.0..180.0

    if (!objetivoValido) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(LinceTheme.colors.background)
        )
        return
    }

    LaunchedEffect(puntoObjetivo) {
        var ultimoOrigenConsultado: PuntoMapa? = null
        snapshotFlow { puntoUsuario }.collect { origen ->
            if (origen == null || !origen.latitud.isFinite() || !origen.longitud.isFinite()) {
                ruta = null
                cargandoRuta = false
                ultimoOrigenConsultado = null
                return@collect
            }
            val anterior = ultimoOrigenConsultado
            if (anterior != null && distanciaEntreMarcaje(anterior, origen) <= 30f) {
                return@collect
            }
            ultimoOrigenConsultado = origen
            if (distanciaEntreMarcaje(origen, puntoObjetivo) <= 1f) {
                ruta = null
                cargandoRuta = false
                return@collect
            }
            val key = claveRutaMarcaje(origen, puntoObjetivo)
            val cacheada = cacheRutas[key]
            if (cacheada != null) {
                ruta = cacheada
                cargandoRuta = false
                return@collect
            }
            cargandoRuta = true
            val calculada = withContext(Dispatchers.IO) {
                solicitarRutaOsrm(origen, puntoObjetivo, contexto.packageName)
            }
            if (calculada != null) {
                if (cacheRutas.size >= 32) cacheRutas.clear()
                cacheRutas[key] = calculada
            }
            ruta = calculada
            cargandoRuta = false
        }
    }

    val detalleRuta = ruta?.let { resultado ->
        val distancia = formatearDistanciaMarcaje(contexto, resultado.distanciaMetros)
        val minutos = (resultado.duracionSegundos / 60.0).roundToInt().coerceAtLeast(1)
        val tiempo = if (minutos >= 60) {
            contexto.getString(R.string.marcaje_duracion_horas, minutos / 60, minutos % 60)
        } else {
            contexto.getString(R.string.marcaje_duracion_minutos, minutos)
        }
        contexto.getString(R.string.marcaje_ruta_info, distancia, tiempo)
    } ?: distanciaDirecta

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { androidContext ->
                val fuente = configurarFuenteCartografica(androidContext)
                val density = androidContext.resources.displayMetrics.density
                val mapView = MapView(androidContext).apply {
                    setTileSource(fuente)
                    setMultiTouchControls(true)
                    setUseDataConnection(true)
                    setTilesScaledToDpi(true)
                    minZoomLevel = 2.0
                    maxZoomLevel = 19.0
                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    overlayManager.tilesOverlay.setLoadingBackgroundColor(android.graphics.Color.TRANSPARENT)
                    overlayManager.tilesOverlay.setLoadingLineColor(android.graphics.Color.TRANSPARENT)
                    controller.setZoom(16.0)
                    controller.setCenter(GeoPoint(puntoObjetivo.latitud, puntoObjetivo.longitud))
                    setOnTouchListener { view, event ->
                        when (event.actionMasked) {
                            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE ->
                                view.parent?.requestDisallowInterceptTouchEvent(true)
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                                view.parent?.requestDisallowInterceptTouchEvent(false)
                        }
                        false
                    }
                }
                registrarFallbackTeselas(mapView, fuente)

                val circle = Polygon(mapView).apply {
                    fillPaint.color = colores.greenPrimary.copy(alpha = 0.12f).toArgb()
                    fillPaint.style = Paint.Style.FILL
                    outlinePaint.color = colores.greenPrimary.copy(alpha = 0.8f).toArgb()
                    outlinePaint.strokeWidth = 2f * density
                    outlinePaint.isAntiAlias = true
                }
                val directLine = Polyline(mapView).apply {
                    outlinePaint.color = colores.greenPrimary.toArgb()
                    outlinePaint.strokeWidth = 3f * density
                    outlinePaint.strokeCap = Paint.Cap.ROUND
                    outlinePaint.pathEffect = DashPathEffect(floatArrayOf(8f * density, 5f * density), 0f)
                    outlinePaint.isAntiAlias = true
                    isEnabled = false
                }
                val routeHalo = Polyline(mapView).apply {
                    outlinePaint.color = colores.mapGpsHalo.toArgb()
                    outlinePaint.strokeWidth = 8f * density
                    outlinePaint.strokeCap = Paint.Cap.ROUND
                    outlinePaint.isAntiAlias = true
                    isEnabled = false
                }
                val routeLine = Polyline(mapView).apply {
                    outlinePaint.color = colores.greenPrimary.toArgb()
                    outlinePaint.strokeWidth = 4.5f * density
                    outlinePaint.strokeCap = Paint.Cap.ROUND
                    outlinePaint.isAntiAlias = true
                    isEnabled = false
                }
                val targetMarker = Marker(mapView).apply {
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = IconoPinMarcaje(androidContext)
                    setOnMarkerClickListener { _, _ -> true }
                }
                val userMarker = Marker(mapView).apply {
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    icon = crearIconoMarcaje(
                        androidContext,
                        colores.mapGpsBlue.toArgb(),
                        colores.mapGpsHalo.toArgb(),
                        24
                    )
                    isEnabled = false
                    setOnMarkerClickListener { _, _ -> true }
                }

                mapView.overlays.add(circle)
                mapView.overlays.add(directLine)
                mapView.overlays.add(routeHalo)
                mapView.overlays.add(routeLine)
                mapView.overlays.add(targetMarker)
                mapView.overlays.add(userMarker)
                mapView.onResume()

                estado.mapa = mapView
                estado.marcadorObjetivo = targetMarker
                estado.marcadorUsuario = userMarker
                estado.circuloRadio = circle
                estado.lineaDirecta = directLine
                estado.lineaRutaHalo = routeHalo
                estado.lineaRuta = routeLine
                mapView.post {
                    if (mapView.width > 0 && mapView.height > 0) {
                        encuadrarRutaMarcaje(mapView, puntoObjetivo, puntoUsuario, ruta?.puntos.orEmpty())
                        mapView.invalidate()
                    }
                }
                mapView
            },
            update = { mapView ->
                val targetGeoPoint = GeoPoint(puntoObjetivo.latitud, puntoObjetivo.longitud)
                val targetChanged = estado.ultimoObjetivo != puntoObjetivo
                val userChanged = estado.ultimoUsuario != puntoUsuario
                val radiusChanged = estado.ultimoRadio != radioMetros
                val routeChanged = estado.ultimaRuta != ruta

                if (targetChanged || radiusChanged) {
                    if (targetChanged) {
                        estado.marcadorObjetivo?.position = targetGeoPoint
                    }
                    estado.circuloRadio?.points = Polygon.pointsAsCircle(
                        targetGeoPoint,
                        radioMetros.coerceAtLeast(30).toDouble()
                    )
                }

                if (puntoUsuario == null) {
                    if (userChanged || targetChanged || routeChanged) {
                        estado.marcadorUsuario?.isEnabled = false
                        estado.lineaDirecta?.isEnabled = false
                        estado.lineaRutaHalo?.isEnabled = false
                        estado.lineaRuta?.isEnabled = false
                    }
                } else {
                    val userGeoPoint = GeoPoint(puntoUsuario.latitud, puntoUsuario.longitud)
                    if (userChanged) {
                        estado.marcadorUsuario?.position = userGeoPoint
                        estado.marcadorUsuario?.isEnabled = true
                    }
                    if (userChanged || targetChanged || routeChanged) {
                        estado.lineaDirecta?.setPoints(listOf(userGeoPoint, targetGeoPoint))
                        estado.lineaDirecta?.isEnabled = ruta == null
                        if (ruta != null) {
                            val routePoints = ruta!!.puntos.map { GeoPoint(it.latitud, it.longitud) }
                            estado.lineaRutaHalo?.setPoints(routePoints)
                            estado.lineaRuta?.setPoints(routePoints)
                            estado.lineaRutaHalo?.isEnabled = true
                            estado.lineaRuta?.isEnabled = true
                        } else {
                            estado.lineaRutaHalo?.isEnabled = false
                            estado.lineaRuta?.isEnabled = false
                        }
                    }
                }

                if (targetChanged || userChanged || radiusChanged || routeChanged) {
                    estado.ultimoObjetivo = puntoObjetivo
                    estado.ultimoUsuario = puntoUsuario
                    estado.ultimoRadio = radioMetros
                    estado.ultimaRuta = ruta
                    if (mapView.width > 0 && mapView.height > 0) {
                        encuadrarRutaMarcaje(mapView, puntoObjetivo, puntoUsuario, ruta?.puntos.orEmpty())
                    } else {
                        mapView.post {
                            if (mapView.width > 0 && mapView.height > 0) {
                                encuadrarRutaMarcaje(mapView, puntoObjetivo, puntoUsuario, ruta?.puntos.orEmpty())
                                mapView.invalidate()
                            }
                        }
                    }
                    mapView.invalidate()
                }
            }
        )

        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(LinceSpacing.small)
                .fillMaxWidth(0.74f),
            color = colores.cardSurface.copy(alpha = if (colores.isDark) 0.94f else 0.92f),
            shape = RoundedCornerShape(LinceRadius.medium),
            border = BorderStroke(1.dp, colores.outlineVariant.copy(alpha = 0.8f))
        ) {
            Column(
                modifier = Modifier.padding(
                    start = LinceSpacing.small,
                    end = LinceSpacing.small,
                    top = LinceSpacing.xSmall,
                    bottom = LinceSpacing.xSmall
                ),
                verticalArrangement = Arrangement.spacedBy(LinceSpacing.xSmall)
            ) {
                Text(
                    text = contextoPunto,
                    style = MaterialTheme.typography.labelMedium,
                    color = colores.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    softWrap = true,
                    maxLines = 2,
                    overflow = TextOverflow.Clip
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(LinceSpacing.xSmall)
                ) {
                    Text(
                        text = detalleRuta ?: contexto.getString(R.string.marcaje_radio_valor, radioMetros),
                        style = MaterialTheme.typography.labelSmall,
                        color = colores.textSecondary,
                        softWrap = true,
                        maxLines = 2,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier.weight(1f)
                    )
                    if (cargandoRuta) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = colores.greenPrimary
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = LinceSpacing.small),
            verticalArrangement = Arrangement.spacedBy(LinceSpacing.small)
        ) {
            BotonControlRuta(texto = "+") {
                estado.mapa?.let { mapView ->
                    mapView.controller.setZoom((mapView.zoomLevelDouble + 1.0).coerceAtMost(19.0))
                    mapView.invalidate()
                }
            }
            BotonControlRuta(texto = "−") {
                estado.mapa?.let { mapView ->
                    mapView.controller.setZoom((mapView.zoomLevelDouble - 1.0).coerceAtLeast(2.0))
                    mapView.invalidate()
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(LinceSpacing.small)
                .size(48.dp)
                .clip(CircleShape)
                .clickable {
                    estado.mapa?.let { mapView ->
                        encuadrarRutaMarcaje(mapView, puntoObjetivo, puntoUsuario, ruta?.puntos.orEmpty())
                        mapView.invalidate()
                    }
                },
            shape = CircleShape,
            color = colores.cardSurface,
            border = BorderStroke(1.dp, colores.outlineVariant),
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = stringResource(R.string.marcaje_mapa_tu_ubicacion),
                    tint = colores.greenPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(LinceSpacing.small),
            color = colores.cardSurface.copy(alpha = if (colores.isDark) 0.92f else 0.88f),
            shape = RoundedCornerShape(LinceRadius.small),
            border = BorderStroke(0.75.dp, colores.outlineVariant.copy(alpha = 0.7f))
        ) {
            Text(
                text = stringResource(R.string.mapa_atribucion_osm),
                style = MaterialTheme.typography.labelSmall,
                color = colores.textSecondary,
                modifier = Modifier.padding(
                    start = LinceSpacing.xSmall,
                    end = LinceSpacing.xSmall,
                    top = 2.dp,
                    bottom = 2.dp
                )
            )
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> estado.mapa?.onResume()
                Lifecycle.Event.ON_PAUSE -> estado.mapa?.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            estado.mapa?.onResume()
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            estado.mapa?.apply {
                onPause()
                onDetach()
            }
            estado.mapa = null
            estado.marcadorObjetivo = null
            estado.marcadorUsuario = null
            estado.circuloRadio = null
            estado.lineaDirecta = null
            estado.lineaRutaHalo = null
            estado.lineaRuta = null
        }
    }
}

private fun claveRutaMarcaje(origen: PuntoMapa, destino: PuntoMapa): String =
    String.format(
        Locale.US,
        "%.4f,%.4f;%.5f,%.5f",
        origen.longitud,
        origen.latitud,
        destino.longitud,
        destino.latitud
    )

private fun distanciaEntreMarcaje(origen: PuntoMapa, destino: PuntoMapa): Float {
    val resultado = FloatArray(1)
    Location.distanceBetween(
        origen.latitud,
        origen.longitud,
        destino.latitud,
        destino.longitud,
        resultado
    )
    return resultado[0]
}

private fun formatearDistanciaMarcaje(context: Context, metros: Double): String =
    if (metros >= 1000.0) {
        context.getString(
            R.string.marcaje_distancia_km,
            String.format(Locale.US, "%.1f", metros / 1000.0)
        )
    } else {
        context.getString(R.string.marcaje_distancia_valor, metros.roundToInt())
    }

private fun solicitarRutaOsrm(
    origen: PuntoMapa,
    destino: PuntoMapa,
    userAgent: String
): RutaMarcaje? {
    var conexion: HttpURLConnection? = null
    return try {
        val url = URL(
            "https://router.project-osrm.org/route/v1/driving/" +
                    "${origen.longitud},${origen.latitud};${destino.longitud},${destino.latitud}" +
                    "?overview=full&geometries=geojson"
        )
        conexion = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 6000
            readTimeout = 6000
            useCaches = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", userAgent)
        }
        if (conexion.responseCode != HttpURLConnection.HTTP_OK) return null
        val contenido = conexion.inputStream.bufferedReader().use { it.readText() }
        val rutas = JSONObject(contenido).optJSONArray("routes") ?: return null
        val ruta = rutas.optJSONObject(0) ?: return null
        val coordenadas = ruta.optJSONObject("geometry")?.optJSONArray("coordinates") ?: return null
        if (coordenadas.length() < 2) return null
        val puntos = buildList {
            for (indice in 0 until coordenadas.length()) {
                val coordenada = coordenadas.optJSONArray(indice) ?: continue
                if (coordenada.length() < 2) continue
                val longitud = coordenada.optDouble(0, Double.NaN)
                val latitud = coordenada.optDouble(1, Double.NaN)
                if (longitud.isFinite() && latitud.isFinite()) {
                    add(PuntoMapa(latitud, longitud))
                }
            }
        }
        if (puntos.size < 2) return null
        val distancia = ruta.optDouble("distance", Double.NaN)
        val duracion = ruta.optDouble("duration", Double.NaN)
        if (!distancia.isFinite() || !duracion.isFinite()) return null
        RutaMarcaje(puntos, distancia, duracion)
    } catch (_: Exception) {
        null
    } finally {
        conexion?.disconnect()
    }
}

private fun encuadrarRutaMarcaje(
    mapView: MapView,
    objetivo: PuntoMapa,
    usuario: PuntoMapa?,
    ruta: List<PuntoMapa> = emptyList()
) {
    if (mapView.width <= 0 || mapView.height <= 0) return
    if (usuario == null) {
        mapView.controller.setZoom(16.0)
        mapView.controller.setCenter(GeoPoint(objetivo.latitud, objetivo.longitud))
        return
    }

    val puntos = ruta + listOf(objetivo, usuario)
    val minLat = puntos.minOf { it.latitud }
    val maxLat = puntos.maxOf { it.latitud }
    val minLon = puntos.minOf { it.longitud }
    val maxLon = puntos.maxOf { it.longitud }
    if (listOf(minLat, maxLat, minLon, maxLon).any { !it.isFinite() }) return

    val mercatorLatSpan = abs(proyectarLatitudMercator(maxLat) - proyectarLatitudMercator(minLat))
        .coerceAtLeast(0.000001)
    val lonSpan = abs(maxLon - minLon).coerceAtLeast(0.000001)
    val ancho = (mapView.width - mapView.paddingLeft - mapView.paddingRight).coerceAtLeast(1).toDouble()
    val alto = (mapView.height - mapView.paddingTop - mapView.paddingBottom).coerceAtLeast(1).toDouble()
    val zoomHorizontal = ln(ancho * 0.72 * 360.0 / (256.0 * lonSpan)) / ln(2.0)
    val zoomVertical = ln(alto * 0.72 * 2.0 * Math.PI / (256.0 * mercatorLatSpan)) / ln(2.0)
    val zoom = (minOf(zoomHorizontal, zoomVertical) - 0.2)
        .coerceIn(4.0, 17.0)
    mapView.controller.setZoom(zoom)
    mapView.controller.setCenter(GeoPoint((minLat + maxLat) / 2.0, (minLon + maxLon) / 2.0))
}

private fun proyectarLatitudMercator(latitud: Double): Double {
    val acotada = latitud.coerceIn(-85.05112878, 85.05112878)
    val radianes = Math.toRadians(acotada)
    return ln(Math.tan(Math.PI / 4.0 + radianes / 2.0))
}

private fun crearIconoMarcaje(
    context: Context,
    color: Int,
    halo: Int,
    sizeDp: Int
): GradientDrawable {
    val density = context.resources.displayMetrics.density
    val size = (sizeDp * density).toInt()
    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
        setStroke((4 * density).toInt(), halo)
        setSize(size, size)
    }
}

private class IconoPinMarcaje(context: Context) : Drawable() {
    private val densidad = context.resources.displayMetrics.density
    private val tamano = (32 * densidad).toInt()
    private val relleno = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF397A55.toInt()
        style = Paint.Style.FILL
    }
    private val borde = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * densidad
    }
    private val centro = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        val ancho = bounds.width().toFloat()
        val alto = bounds.height().toFloat()
        val centroX = bounds.left + ancho / 2f
        val path = Path().apply {
            moveTo(centroX, bounds.top + alto * 0.98f)
            cubicTo(
                bounds.left + ancho * 0.43f,
                bounds.top + alto * 0.72f,
                bounds.left + ancho * 0.12f,
                bounds.top + alto * 0.48f,
                bounds.left + ancho * 0.12f,
                bounds.top + alto * 0.31f
            )
            cubicTo(
                bounds.left + ancho * 0.12f,
                bounds.top + alto * 0.12f,
                bounds.left + ancho * 0.32f,
                bounds.top + alto * 0.04f,
                centroX,
                bounds.top + alto * 0.04f
            )
            cubicTo(
                bounds.left + ancho * 0.68f,
                bounds.top + alto * 0.04f,
                bounds.left + ancho * 0.88f,
                bounds.top + alto * 0.12f,
                bounds.left + ancho * 0.88f,
                bounds.top + alto * 0.31f
            )
            cubicTo(
                bounds.left + ancho * 0.88f,
                bounds.top + alto * 0.48f,
                bounds.left + ancho * 0.57f,
                bounds.top + alto * 0.72f,
                centroX,
                bounds.top + alto * 0.98f
            )
            close()
        }
        canvas.drawPath(path, relleno)
        canvas.drawPath(path, borde)
        canvas.drawCircle(centroX, bounds.top + alto * 0.31f, ancho * 0.12f, centro)
    }

    override fun setAlpha(alpha: Int) {
        relleno.alpha = alpha
        borde.alpha = alpha
        centro.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        relleno.colorFilter = colorFilter
        borde.colorFilter = colorFilter
        centro.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Android")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun getIntrinsicWidth(): Int = tamano

    override fun getIntrinsicHeight(): Int = tamano
}

@Composable
private fun BotonControlRuta(texto: String, onClick: () -> Unit) {
    val colors = LinceTheme.colors
    Surface(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = colors.cardSurface,
        border = BorderStroke(1.dp, colors.outlineVariant),
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = texto,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
