package com.example.linceops.ui.components

import android.content.Context
import android.graphics.Color
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
import java.io.File
import kotlin.math.abs
import kotlin.math.roundToInt

private const val USER_AGENT_OSM = "Mozilla/5.0 (Linux; Android 14) LinceOPS/1.0"

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
            Configuration.getInstance().apply {
                userAgentValue = USER_AGENT_OSM
                @Suppress("DEPRECATION")
                load(contexto, PreferenceManager.getDefaultSharedPreferences(contexto))
                osmdroidBasePath = File(contexto.cacheDir, "osmdroid").apply { mkdirs() }
                osmdroidTileCache = File(contexto.cacheDir, "osmdroid/tiles").apply { mkdirs() }
            }

            val handlerUi = Handler(Looper.getMainLooper())
            estado.handlerUi = handlerUi

            val mapa = MapView(contexto).apply {
                setTileSource(FUENTE_ESRI)
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

private fun crearIconoGps(contexto: Context): GradientDrawable {
    val densidad = contexto.resources.displayMetrics.density
    val tamano = (24 * densidad).toInt()
    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(0xFF1E88E5.toInt())
        setStroke((5 * densidad).toInt(), Color.WHITE)
        setSize(tamano, tamano)
    }
}
