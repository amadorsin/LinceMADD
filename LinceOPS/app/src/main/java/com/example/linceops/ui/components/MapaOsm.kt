package com.example.linceops.ui.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Mapa interactivo real sin API Key (Leaflet + CartoDB Voyager / Esri Street Map).
 * Nunca devuelve error 403 ni pide API Key, soporta arrastrar con el dedo, pellizcar
 * para hacer zoom y mantiene sincronizado el punto central con el formulario.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MapaOsm(
    centro: PuntoMapa,
    zoom: Int,
    onCentroChange: (PuntoMapa) -> Unit,
    onZoomChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val centroCallback by rememberUpdatedState(onCentroChange)
    val zoomCallback by rememberUpdatedState(onZoomChange)

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var paginaCargada by remember { mutableStateOf(false) }
    var ultimoCentroDesdeJs by remember { mutableStateOf(centro) }
    var ultimoZoomDesdeJs by remember { mutableStateOf(zoom) }

    val htmlMapa = remember {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                html, body, #map {
                    width: 100%;
                    height: 100%;
                    margin: 0;
                    padding: 0;
                    background: #FAF7F2;
                }
                .leaflet-control-attribution, .leaflet-control-zoom {
                    display: none !important;
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map', {
                    zoomControl: false,
                    attributionControl: false,
                    minZoom: 3,
                    maxZoom: 19,
                    zoomSnap: 1,
                    zoomDelta: 1
                }).setView([${centro.latitud}, ${centro.longitud}], ${zoom.coerceIn(3, 19)});

                // Servidor principal estilo Google Maps (Carto Voyager - 100% libre y sin API Key)
                var capaPrincipal = L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
                    maxZoom: 20,
                    subdomains: 'abcd'
                }).addTo(map);

                // Respaldo automático con Esri World Street Map si falla alguna tesela.
                // Se activa una sola vez para no apilar capas duplicadas.
                var respaldoActivo = false;
                capaPrincipal.on('tileerror', function() {
                    if (respaldoActivo) return;
                    respaldoActivo = true;
                    L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}', {
                        maxZoom: 19
                    }).addTo(map);
                });

                var moviendoDesdeKotlin = false;

                map.on('moveend', function() {
                    if (moviendoDesdeKotlin) return;
                    var c = map.getCenter();
                    if (window.AndroidMapBridge) {
                        window.AndroidMapBridge.onMapMoved(c.lat, c.lng, map.getZoom());
                    }
                });

                function actualizarVista(lat, lng, z) {
                    moviendoDesdeKotlin = true;
                    map.setView([lat, lng], z, { animate: true });
                    setTimeout(function() { moviendoDesdeKotlin = false; }, 350);
                }
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    LaunchedEffect(centro, zoom, paginaCargada) {
        if (!paginaCargada) return@LaunchedEffect
        val cambioCentro = abs(centro.latitud - ultimoCentroDesdeJs.latitud) > 1e-4 ||
                abs(centro.longitud - ultimoCentroDesdeJs.longitud) > 1e-4
        val cambioZoom = zoom != ultimoZoomDesdeJs

        if (cambioCentro || cambioZoom) {
            ultimoCentroDesdeJs = centro
            ultimoZoomDesdeJs = zoom
            val comando = String.format(
                Locale.US,
                "javascript:actualizarVista(%.6f, %.6f, %d);",
                centro.latitud,
                centro.longitud,
                zoom.coerceIn(3, 19)
            )
            webViewRef?.evaluateJavascript(comando, null)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { contexto ->
            WebView(contexto).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                settings.userAgentString = "Mozilla/5.0 (Linux; Android 14) Chrome/124.0.0.0 Mobile Safari/537.36"

                addJavascriptInterface(
                    object {
                        @JavascriptInterface
                        fun onMapMoved(lat: Double, lng: Double, z: Double) {
                            val nuevoPunto = PuntoMapa(lat, lng)
                            val nuevoZoom = z.roundToInt().coerceIn(3, 19)
                            ultimoCentroDesdeJs = nuevoPunto
                            ultimoZoomDesdeJs = nuevoZoom
                            post {
                                centroCallback(nuevoPunto)
                                zoomCallback(nuevoZoom)
                            }
                        }
                    },
                    "AndroidMapBridge"
                )

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        paginaCargada = true
                    }
                }

                loadDataWithBaseURL(
                    "https://linceops.local/",
                    htmlMapa,
                    "text/html",
                    "UTF-8",
                    null
                )
                webViewRef = this
            }
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.destroy()
            webViewRef = null
        }
    }
}