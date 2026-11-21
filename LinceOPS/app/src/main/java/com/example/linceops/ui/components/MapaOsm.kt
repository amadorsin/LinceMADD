package com.example.linceops.ui.components

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.preference.PreferenceManager
import android.view.MotionEvent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.linceops.BuildConfig
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.MapTileProviderBase
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

private const val USER_AGENT_OSM = "Mozilla/5.0 (Linux; Android 14) LinceOPS/1.0"
private const val USER_AGENT_MAPTILER = "LinceOPS-Android"

private class EsriWorldStreetSource : OnlineTileSourceBase(
    "EsriWorldStreetLince",
    3,
    19,
    256,
    ".jpg",
    arrayOf("https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/"),
    "\u00A9 Esri"
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val zoom = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        return baseUrl + zoom + "/" + y + "/" + x
    }
}

private val FUENTE_ESRI = EsriWorldStreetSource()

private class MapTilerStreetsSource(apiKey: String) : OnlineTileSourceBase(
    "MapTilerStreetsV4Lince",
    1,
    19,
    512,
    ".png",
    arrayOf("https://api.maptiler.com/maps/streets-v4/"),
    "\u00A9 MapTiler \u00A9 OpenStreetMap contributors"
) {
    private val clave = apiKey

    override fun getTileURLString(pMapTileIndex: Long): String {
        val zoom = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        return baseUrl + zoom + "/" + x + "/" + y + ".png?key=" + clave
    }
}

private fun fuenteCartografica(): OnlineTileSourceBase {
    val clave = BuildConfig.MAPTILER_KEY.trim()
    return if (clave.isEmpty()) FUENTE_ESRI else MapTilerStreetsSource(clave)
}

private class EstadoMapaOsm {
    var mapa: MapView? = null
    var marcadorGps: Marker? = null
    var handlerUi: Handler? = null
    var finMovimiento: Runnable? = null
    var ultimoEnvio = 0L
    var animandoHasta = 0L
    var ultimoCentroNotificado: PuntoMapa? = null
    var ultimoZoomNotificado: Int? = null
    var exitosTesela = 0
    var fallosTesela = 0
    var errorTeselasNotificado = false
}

private class EstadoMapaDistanciaOsm {
    var mapa: MapView? = null
    var marcadorObjetivo: Marker? = null
    var marcadorUsuario: Marker? = null
    var lineaDistancia: Polyline? = null
    var circuloRadio: Polygon? = null
    var ultimoObjetivo: PuntoMapa? = null
    var ultimoUsuario: PuntoMapa? = null
    var ultimoRadio: Int? = null
}

@Composable
fun MapaOsm(
    centro: PuntoMapa,
    zoom: Int,
    onCentroChange: (PuntoMapa) -> Unit,
    onZoomChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    puntoGps: PuntoMapa? = null,
    onMapMovingChange: (Boolean) -> Unit = {},
    onTileError: () -> Unit = {}
) {
    val centroCallback by rememberUpdatedState(onCentroChange)
    val zoomCallback by rememberUpdatedState(onZoomChange)
    val movingCallback by rememberUpdatedState(onMapMovingChange)
    val tileErrorCallback by rememberUpdatedState(onTileError)
    val estado = remember { EstadoMapaOsm() }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { contexto ->
            val fuente = fuenteCartografica()
            Configuration.getInstance().apply {
                @Suppress("DEPRECATION")
                load(contexto, PreferenceManager.getDefaultSharedPreferences(contexto))
                userAgentValue = if (fuente is MapTilerStreetsSource) {
                    USER_AGENT_MAPTILER
                } else {
                    USER_AGENT_OSM
                }
                osmdroidBasePath = File(contexto.cacheDir, "osmdroid").apply { mkdirs() }
                osmdroidTileCache = File(contexto.cacheDir, "osmdroid/tiles").apply { mkdirs() }
            }

            val handlerUi = Handler(Looper.getMainLooper())
            estado.handlerUi = handlerUi

            val mapa = MapView(contexto).apply {
                setTileSource(fuente)
                setMultiTouchControls(true)
                setUseDataConnection(true)
                setTilesScaledToDpi(true)
                minZoomLevel = 3.0
                maxZoomLevel = 19.0
                setBackgroundColor(Color.TRANSPARENT)
                overlayManager.tilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT)
                overlayManager.tilesOverlay.setLoadingLineColor(Color.TRANSPARENT)
                controller.setZoom(zoom.coerceIn(3, 19).toDouble())
                controller.setCenter(GeoPoint(centro.latitud, centro.longitud))
            }

            val marcador = Marker(mapa).apply {
                position = GeoPoint(centro.latitud, centro.longitud)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                icon = crearIconoGps(contexto)
                isEnabled = false
                setOnMarkerClickListener { _, _ -> true }
            }
            mapa.overlays.add(marcador)

            val handlerTeselas = object : Handler(Looper.getMainLooper()) {
                override fun handleMessage(msg: Message) {
                    when (msg.what) {
                        MapTileProviderBase.MAPTILE_SUCCESS_ID -> {
                            estado.exitosTesela++
                            estado.fallosTesela = 0
                        }

                        MapTileProviderBase.MAPTILE_FAIL_ID -> {
                            estado.fallosTesela++
                            if (!estado.errorTeselasNotificado &&
                                estado.exitosTesela == 0 &&
                                estado.fallosTesela >= 6
                            ) {
                                estado.errorTeselasNotificado = true
                                tileErrorCallback()
                            }
                        }
                    }
                }
            }
            mapa.tileProvider.tileRequestCompleteHandlers.add(handlerTeselas)

            fun detenerMovimiento() {
                estado.finMovimiento?.let { handlerUi.removeCallbacks(it) }
                estado.finMovimiento = null
                movingCallback(false)
            }

            fun programarFinMovimiento() {
                estado.finMovimiento?.let { handlerUi.removeCallbacks(it) }
                val fin = Runnable { detenerMovimiento() }
                estado.finMovimiento = fin
                handlerUi.postDelayed(fin, 450L)
            }

            fun notificar(forzar: Boolean) {
                val ahora = System.currentTimeMillis()
                if (ahora < estado.animandoHasta) return
                if (!forzar && ahora - estado.ultimoEnvio < 60L) return
                estado.ultimoEnvio = ahora
                val actual = mapa.mapCenter
                val punto = PuntoMapa(actual.latitude, actual.longitude)
                val zoomActual = mapa.zoomLevelDouble.roundToInt().coerceIn(3, 19)
                estado.ultimoCentroNotificado = punto
                estado.ultimoZoomNotificado = zoomActual
                centroCallback(punto)
                zoomCallback(zoomActual)
            }

            mapa.addMapListener(object : MapListener {
                override fun onScroll(event: ScrollEvent): Boolean {
                    notificar(false)
                    programarFinMovimiento()
                    return false
                }

                override fun onZoom(event: ZoomEvent): Boolean {
                    notificar(true)
                    programarFinMovimiento()
                    return false
                }
            })

            mapa.setOnTouchListener { _, evento ->
                when (evento.actionMasked) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                        estado.finMovimiento?.let { handlerUi.removeCallbacks(it) }
                        estado.finMovimiento = null
                        movingCallback(true)
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> detenerMovimiento()
                }
                false
            }

            mapa.onResume()
            estado.mapa = mapa
            estado.marcadorGps = marcador
            estado.ultimoCentroNotificado = centro
            estado.ultimoZoomNotificado = zoom
            mapa
        },
        update = { mapa ->
            val centroNotificado = estado.ultimoCentroNotificado
            val zoomNotificado = estado.ultimoZoomNotificado
            val cambioCentroExterno = centroNotificado == null ||
                    abs(centro.latitud - centroNotificado.latitud) > 1e-5 ||
                    abs(centro.longitud - centroNotificado.longitud) > 1e-5
            val cambioZoomExterno = zoomNotificado == null || zoom != zoomNotificado

            if (cambioCentroExterno || cambioZoomExterno) {
                val zoomSeguro = zoom.coerceIn(3, 19)
                estado.animandoHasta = System.currentTimeMillis() + 420L
                estado.ultimoCentroNotificado = centro
                estado.ultimoZoomNotificado = zoomSeguro
                mapa.controller.animateTo(
                    GeoPoint(centro.latitud, centro.longitud),
                    zoomSeguro.toDouble(),
                    280L
                )
            }

            val marcador = estado.marcadorGps ?: return@AndroidView
            if (puntoGps == null) {
                if (marcador.isEnabled) {
                    marcador.isEnabled = false
                    mapa.invalidate()
                }
            } else {
                val posicion = marcador.position
                val cambio =
                    !marcador.isEnabled ||
                            abs(posicion.latitude - puntoGps.latitud) > 1e-6 ||
                            abs(posicion.longitude - puntoGps.longitud) > 1e-6
                if (cambio) {
                    marcador.position = GeoPoint(puntoGps.latitud, puntoGps.longitud)
                    marcador.isEnabled = true
                    mapa.invalidate()
                }
            }
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            estado.finMovimiento?.let { estado.handlerUi?.removeCallbacks(it) }
            estado.finMovimiento = null
            estado.handlerUi = null
            estado.mapa?.apply {
                onPause()
                onDetach()
            }
            estado.mapa = null
            estado.marcadorGps = null
        }
    }
}

@Composable
fun MarcajeMapaDistancia(
    puntoObjetivo: PuntoMapa,
    puntoUsuario: PuntoMapa?,
    radioMetros: Int,
    modifier: Modifier = Modifier
) {
    val estado = remember { EstadoMapaDistanciaOsm() }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { contexto ->
            val fuente = fuenteCartografica()
            Configuration.getInstance().apply {
                @Suppress("DEPRECATION")
                load(contexto, PreferenceManager.getDefaultSharedPreferences(contexto))
                userAgentValue = if (fuente is MapTilerStreetsSource) {
                    USER_AGENT_MAPTILER
                } else {
                    USER_AGENT_OSM
                }
                osmdroidBasePath = File(contexto.cacheDir, "osmdroid").apply { mkdirs() }
                osmdroidTileCache = File(contexto.cacheDir, "osmdroid/tiles").apply { mkdirs() }
            }

            val densidad = contexto.resources.displayMetrics.density
            val mapa = MapView(contexto).apply {
                setTileSource(fuente)
                setMultiTouchControls(true)
                setUseDataConnection(true)
                setTilesScaledToDpi(true)
                minZoomLevel = 4.0
                maxZoomLevel = 19.0
                setBackgroundColor(Color.TRANSPARENT)
                overlayManager.tilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT)
                overlayManager.tilesOverlay.setLoadingLineColor(Color.TRANSPARENT)
                setOnTouchListener { vista, evento ->
                    when (evento.actionMasked) {
                        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE ->
                            vista.parent?.requestDisallowInterceptTouchEvent(true)
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                            vista.parent?.requestDisallowInterceptTouchEvent(false)
                    }
                    false
                }
            }

            val circulo = Polygon(mapa).apply {
                fillPaint.color = 0x263E7B52
                fillPaint.style = Paint.Style.FILL
                outlinePaint.color = 0xFF3E7B52.toInt()
                outlinePaint.strokeWidth = 2f * densidad
                outlinePaint.isAntiAlias = true
            }

            val linea = Polyline(mapa).apply {
                outlinePaint.color = 0xFF3E7B52.toInt()
                outlinePaint.strokeWidth = 3.2f * densidad
                outlinePaint.strokeCap = Paint.Cap.ROUND
                outlinePaint.isAntiAlias = true
                isEnabled = false
            }

            val marcadorObj = Marker(mapa).apply {
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                icon = crearIconoObjetivo(contexto)
                setOnMarkerClickListener { _, _ -> true }
            }

            val marcadorUsr = Marker(mapa).apply {
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                icon = crearIconoGps(contexto)
                isEnabled = false
                setOnMarkerClickListener { _, _ -> true }
            }

            mapa.overlays.add(circulo)
            mapa.overlays.add(linea)
            mapa.overlays.add(marcadorObj)
            mapa.overlays.add(marcadorUsr)

            mapa.onResume()
            estado.mapa = mapa
            estado.circuloRadio = circulo
            estado.lineaDistancia = linea
            estado.marcadorObjetivo = marcadorObj
            estado.marcadorUsuario = marcadorUsr
            mapa
        },
        update = { mapa ->
            val geoObjetivo = GeoPoint(puntoObjetivo.latitud, puntoObjetivo.longitud)
            val cambioObjetivo = estado.ultimoObjetivo != puntoObjetivo
            val cambioUsuario = estado.ultimoUsuario != puntoUsuario
            val cambioRadio = estado.ultimoRadio != radioMetros

            if (cambioObjetivo || cambioRadio) {
                estado.marcadorObjetivo?.position = geoObjetivo
                estado.circuloRadio?.points = Polygon.pointsAsCircle(
                    geoObjetivo,
                    radioMetros.coerceAtLeast(30).toDouble()
                )
            }

            val marcadorUsr = estado.marcadorUsuario
            val linea = estado.lineaDistancia
            if (puntoUsuario == null) {
                marcadorUsr?.isEnabled = false
                linea?.isEnabled = false
            } else {
                val geoUsuario = GeoPoint(puntoUsuario.latitud, puntoUsuario.longitud)
                marcadorUsr?.position = geoUsuario
                marcadorUsr?.isEnabled = true
                linea?.setPoints(listOf(geoUsuario, geoObjetivo))
                linea?.isEnabled = true
            }

            if (cambioObjetivo || cambioUsuario || cambioRadio) {
                estado.ultimoObjetivo = puntoObjetivo
                estado.ultimoUsuario = puntoUsuario
                estado.ultimoRadio = radioMetros
                encuadrarPuntos(mapa, puntoObjetivo, puntoUsuario)
                mapa.invalidate()
            }
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            estado.mapa?.apply {
                onPause()
                onDetach()
            }
            estado.mapa = null
            estado.marcadorObjetivo = null
            estado.marcadorUsuario = null
            estado.lineaDistancia = null
            estado.circuloRadio = null
        }
    }
}

private fun encuadrarPuntos(
    mapa: MapView,
    objetivo: PuntoMapa,
    usuario: PuntoMapa?
) {
    if (usuario == null) {
        mapa.controller.setZoom(16.2)
        mapa.controller.setCenter(GeoPoint(objetivo.latitud, objetivo.longitud))
        return
    }

    val centroLat = (objetivo.latitud + usuario.latitud) / 2.0
    val centroLon = (objetivo.longitud + usuario.longitud) / 2.0
    val deltaLat = abs(objetivo.latitud - usuario.latitud)
    val deltaLon = abs(objetivo.longitud - usuario.longitud)
    val span = max(deltaLat, deltaLon)

    val zoomCalculado = when {
        span < 0.0018 -> 16.8
        span < 0.0040 -> 15.8
        span < 0.0090 -> 14.8
        span < 0.0200 -> 13.8
        span < 0.0450 -> 12.6
        span < 0.1000 -> 11.4
        span < 0.2500 -> 10.2
        span < 0.6000 -> 9.0
        else -> 7.5
    }

    mapa.controller.setZoom(zoomCalculado)
    mapa.controller.setCenter(GeoPoint(centroLat, centroLon))
}

private fun crearIconoGps(contexto: Context): GradientDrawable {
    val densidad = contexto.resources.displayMetrics.density
    val tamano = (22 * densidad).toInt()
    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(0xFF1E88E5.toInt())
        setStroke((4 * densidad).toInt(), Color.WHITE)
        setSize(tamano, tamano)
    }
}

private fun crearIconoObjetivo(contexto: Context): GradientDrawable {
    val densidad = contexto.resources.displayMetrics.density
    val tamano = (24 * densidad).toInt()
    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(0xFF3E7B52.toInt())
        setStroke((4 * densidad).toInt(), Color.WHITE)
        setSize(tamano, tamano)
    }
}
