package com.example.linceops.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.linceops.R
import com.example.linceops.model.EstadoServicio
import com.example.linceops.model.Servicio
import com.example.linceops.network.dto.GeocercaDto
import com.example.linceops.ui.components.MapaRutaMarcaje
import com.example.linceops.ui.components.MarcajeSkeleton
import com.example.linceops.ui.components.PuntoMapa
import com.example.linceops.ui.components.LinceButton
import com.example.linceops.ui.components.LinceButtonVariant
import com.example.linceops.ui.components.LinceDivider
import com.example.linceops.ui.components.LinceSectionHeader
import com.example.linceops.ui.components.LinceStatusBadge
import com.example.linceops.ui.components.LinceStatusTone
import com.example.linceops.ui.components.LinceSurfaceCard
import com.example.linceops.ui.components.LinceSurfaceCardVariant
import com.example.linceops.ui.theme.LinceRadius
import com.example.linceops.ui.theme.LinceSpacing
import com.example.linceops.ui.theme.LinceTheme
import com.example.linceops.ui.theme.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import com.example.linceops.utils.isInternetAvailable

@Composable
fun MarcajeScreen(
    servicioId: String,
    servicio: Servicio? = null,
    geocercas: List<GeocercaDto> = emptyList(),
    hitosPendientesSync: Int = 0,
    sincronizandoOffline: Boolean = false,
    errorSyncOffline: String? = null,
    onSincronizarAhora: () -> Unit = {},
    onDescartarErrorSync: () -> Unit = {},
    onCheckIn: (Double, Double) -> Unit,
    onCheckOut: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    if (servicio == null) {
        MarcajeSkeleton(modifier = modifier)
        return
    }
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var mensajeEstado by remember { mutableStateOf(context.getString(R.string.marcaje_esperando)) }
    var accionPendiente by remember { mutableStateOf<(() -> Unit)?>(null) }
    var precisionGpsMetros by remember { mutableStateOf<Float?>(null) }
    var distanciaMetros by remember { mutableStateOf<Double?>(null) }
    var puntoUsuarioActual by remember { mutableStateOf<PuntoMapa?>(null) }
    var esVistaCheckOut by remember(servicio.id, servicio.estado) {
        mutableStateOf(servicio.estado == EstadoServicio.EN_CURSO)
    }

    val geocercaPickup = remember(servicio, geocercas) {
        geocercas.firstOrNull { it.puntoReferencia.equals(servicio.pickup, ignoreCase = true) }
            ?: geocercas.firstOrNull {
                !it.puntoReferencia.isNullOrBlank() &&
                        servicio.pickup.contains(it.puntoReferencia, ignoreCase = true)
            }
            ?: geocercas.firstOrNull { it.puntoReferencia.equals(servicio.oficina.name, ignoreCase = true) }
    }

    val geocercaTermino = remember(servicio, geocercas, geocercaPickup) {
        val destino = servicio.dropoff.takeIf { it.isNotBlank() } ?: servicio.origenDestino
        geocercas.firstOrNull { it.puntoReferencia.equals(destino, ignoreCase = true) }
            ?: geocercas.firstOrNull {
                !it.puntoReferencia.isNullOrBlank() &&
                        destino.contains(it.puntoReferencia, ignoreCase = true)
            }
            ?: geocercaPickup
    }

    val geocercaObjetivo = if (esVistaCheckOut) geocercaTermino else geocercaPickup
    val radioPermitido = geocercaObjetivo?.radioMetros ?: 150

    val puntoObjetivoMapa = remember(geocercaObjetivo) {
        val lat = geocercaObjetivo?.latitudCentro
        val lon = geocercaObjetivo?.longitudCentro
        if (lat != null && lon != null && (abs(lat) > 1e-5 || abs(lon) > 1e-5)) {
            PuntoMapa(lat, lon)
        } else {
            null
        }
    }

    LaunchedEffect(puntoObjetivoMapa, puntoUsuarioActual) {
        val obj = puntoObjetivoMapa
        val usr = puntoUsuarioActual
        if (obj != null && usr != null) {
            distanciaMetros = calcularHaversineMetros(
                obj.latitud,
                obj.longitud,
                usr.latitud,
                usr.longitud
            )
        }
    }

    @SuppressLint("MissingPermission")
    LaunchedEffect(servicio.id, puntoObjetivoMapa) {
        val tienePermiso = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
        if (tienePermiso) {
            runCatching {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        puntoUsuarioActual = PuntoMapa(location.latitude, location.longitude)
                        precisionGpsMetros = location.accuracy
                        puntoObjetivoMapa?.let { obj ->
                            distanciaMetros = calcularHaversineMetros(
                                obj.latitud,
                                obj.longitud,
                                location.latitude,
                                location.longitude
                            )
                        }
                    }
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val concedido = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (concedido) {
            accionPendiente?.invoke()
        } else {
            mensajeEstado = context.getString(R.string.marcaje_permiso_denegado)
        }
        accionPendiente = null
    }

    fun procesarUbicacion(
        location: android.location.Location,
        esCheckIn: Boolean,
        onSuccess: (Double, Double) -> Unit
    ) {
        esVistaCheckOut = !esCheckIn
        val geocercaPaso = if (esCheckIn) geocercaPickup else geocercaTermino
        puntoUsuarioActual = PuntoMapa(location.latitude, location.longitude)
        precisionGpsMetros = location.accuracy
        val dist = if (geocercaPaso?.latitudCentro != null && geocercaPaso.longitudCentro != null) {
            calcularHaversineMetros(
                geocercaPaso.latitudCentro, geocercaPaso.longitudCentro,
                location.latitude, location.longitude
            )
        } else null
        distanciaMetros = dist

        if (esCheckIn && isInternetAvailable(context) && location.accuracy > 50f) {
            mensajeEstado = context.getString(
                R.string.marcaje_gps_impreciso,
                String.format(Locale.ROOT, "%.0f", location.accuracy)
            )
            return
        }
        mensajeEstado = context.getString(
            R.string.marcaje_ubicacion,
            String.format(Locale.ROOT, "%.5f", location.latitude),
            String.format(Locale.ROOT, "%.5f", location.longitude),
            String.format(Locale.ROOT, "%.0f", location.accuracy)
        )
        onSuccess(location.latitude, location.longitude)
    }

    @SuppressLint("MissingPermission")
    fun obtenerUbicacion(esCheckIn: Boolean, onSuccess: (Double, Double) -> Unit) {
        esVistaCheckOut = !esCheckIn
        val tieneFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val tieneCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (tieneFine || tieneCoarse) {
            mensajeEstado = context.getString(R.string.marcaje_obteniendo_coords)
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            procesarUbicacion(location, esCheckIn, onSuccess)
                        } else {
                            val token = CancellationTokenSource()
                            val prioridad = if (tieneFine) {
                                Priority.PRIORITY_HIGH_ACCURACY
                            } else {
                                Priority.PRIORITY_BALANCED_POWER_ACCURACY
                            }
                            fusedLocationClient.getCurrentLocation(prioridad, token.token)
                                .addOnSuccessListener { ubicacionActual ->
                                    if (ubicacionActual != null) {
                                        procesarUbicacion(ubicacionActual, esCheckIn, onSuccess)
                                    } else {
                                        mensajeEstado = context.getString(R.string.marcaje_no_ubicacion_activa)
                                    }
                                }
                                .addOnFailureListener {
                                    mensajeEstado = context.getString(R.string.marcaje_no_ubicacion_satelital)
                                }
                        }
                    }
                    .addOnFailureListener {
                        mensajeEstado = context.getString(R.string.marcaje_error_sensor)
                    }
            } catch (e: SecurityException) {
                mensajeEstado = context.getString(R.string.marcaje_permiso_insuficiente)
            } catch (e: Exception) {
                mensajeEstado = context.getString(R.string.marcaje_error_inesperado)
            }
        } else {
            accionPendiente = { obtenerUbicacion(esCheckIn, onSuccess) }
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    val transicionRadar = rememberInfiniteTransition(label = "radar")
    val pulsoRadar by transicionRadar.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsoRadar"
    )

    val tonoGps = when {
        precisionGpsMetros == null -> LinceStatusTone.Neutral
        precisionGpsMetros!! <= 50f -> LinceStatusTone.Success
        else -> LinceStatusTone.Warning
    }
    val checkInRegistrado = servicio.participaciones.any { it.checkIn != null }
    val checkOutRegistrado = servicio.participaciones.any { it.checkOut != null }
    val estadoEnRuta = servicio.estado == com.example.linceops.model.EstadoServicio.EN_CURSO ||
            servicio.estado == com.example.linceops.model.EstadoServicio.FINALIZADO
    val distanciaTexto = distanciaMetros?.let { metros ->
        if (metros >= 1000.0) {
            stringResource(
                R.string.marcaje_distancia_km,
                String.format(Locale.US, "%.2f", metros / 1000.0)
            )
        } else {
            stringResource(R.string.marcaje_distancia_valor, metros.toInt())
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinceTheme.colors.background)
            .padding(horizontal = LinceSpacing.medium, vertical = LinceSpacing.small)
    ) {
        LinceSurfaceCard(
            variant = LinceSurfaceCardVariant.Default,
            contentPadding = LinceSpacing.mediumSmall
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(LinceSpacing.xSmall)
                ) {
                    Text(
                        text = stringResource(R.string.marcaje_control_ruta),
                        style = MaterialTheme.typography.labelSmall,
                        color = LinceTheme.colors.textSecondary,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = servicio.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        color = LinceTheme.colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.marcaje_id, servicioId),
                        style = MaterialTheme.typography.labelSmall,
                        color = LinceTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(LinceSpacing.small))
                EstadoBadge(estado = servicio.estado)
            }
        }

        if (hitosPendientesSync > 0 || sincronizandoOffline || errorSyncOffline != null) {
            Spacer(modifier = Modifier.height(LinceSpacing.small))
            val esError = errorSyncOffline != null
            Surface(
                modifier = Modifier.fillMaxWidth().testTag("marcaje_banner_offline"),
                color = if (esError) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    LinceTheme.colors.surfaceContainerHigh
                },
                shape = RoundedCornerShape(LinceRadius.medium),
                border = BorderStroke(
                    1.dp,
                    if (esError) {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                    } else {
                        LinceTheme.colors.greenPrimary.copy(alpha = 0.35f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                sincronizandoOffline -> "Sincronizando con el servidor..."
                                esError -> "Error al sincronizar hito offline"
                                else -> "Cola Offline: $hitosPendientesSync hito(s) pendiente(s)"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (esError) {
                                MaterialTheme.colorScheme.error
                            } else {
                                LinceTheme.colors.greenDark
                            },
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = errorSyncOffline
                                ?: "Se enviarán automáticamente al recuperar conexión.",
                            style = MaterialTheme.typography.bodySmall,
                            color = LinceTheme.colors.textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (esError) {
                        TextButton(
                            onClick = onDescartarErrorSync,
                            modifier = Modifier.testTag("marcaje_btn_cerrar_error_sync")
                        ) {
                            Text("Cerrar", color = MaterialTheme.colorScheme.error)
                        }
                    } else if (!sincronizandoOffline) {
                        TextButton(
                            onClick = onSincronizarAhora,
                            modifier = Modifier.testTag("marcaje_btn_reintentar_sync")
                        ) {
                            Text(
                                "Reintentar",
                                color = LinceTheme.colors.greenPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(LinceSpacing.small))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(LinceSpacing.mediumSmall)
        ) {
            LinceSurfaceCard(
                variant = LinceSurfaceCardVariant.Default,
                contentPadding = LinceSpacing.medium
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(LinceSpacing.small)) {
                    LinceSectionHeader(
                        title = stringResource(R.string.marcaje_geo_titulo),
                        action = {
                            LinceStatusBadge(
                                text = stringResource(R.string.marcaje_gps),
                                tone = tonoGps,
                                icon = Icons.Default.LocationOn
                            )
                        }
                    )
                    LinceDivider()
                    Text(
                        text = stringResource(R.string.marcaje_estado_geo),
                        style = MaterialTheme.typography.labelMedium,
                        color = LinceTheme.colors.textSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = mensajeEstado,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LinceTheme.colors.textPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    val precisionTexto = precisionGpsMetros?.let {
                        stringResource(R.string.marcaje_gps_valor, it.toInt())
                    }
                    if (precisionTexto != null || distanciaTexto != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(LinceSpacing.small)
                        ) {
                            precisionTexto?.let {
                                LinceStatusBadge(text = it, tone = tonoGps)
                            }
                            distanciaTexto?.let {
                                LinceStatusBadge(text = it, tone = LinceStatusTone.Info)
                            }
                        }
                    }
                    LinceDivider()
                    NotaOperativaMarcaje(texto = stringResource(R.string.marcaje_recordatorio))
                    NotaOperativaMarcaje(texto = stringResource(R.string.marcaje_info_adjunta))
                }
            }

            LinceSurfaceCard(
                variant = LinceSurfaceCardVariant.Default,
                contentPadding = LinceSpacing.medium
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(LinceSpacing.small)) {
                    LinceSectionHeader(
                        title = stringResource(R.string.marcaje_titulo),
                        action = {
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { esVistaCheckOut = !esVistaCheckOut },
                                shape = RoundedCornerShape(50),
                                color = LinceTheme.colors.surfaceContainerHigh
                            ) {
                                Text(
                                    text = if (esVistaCheckOut) {
                                        stringResource(R.string.marcaje_mapa_punto_termino)
                                    } else {
                                        stringResource(R.string.marcaje_mapa_punto_recogida)
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LinceTheme.colors.greenDark,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(
                                        horizontal = LinceSpacing.small,
                                        vertical = LinceSpacing.xSmall
                                    )
                                )
                            }
                        }
                    )

                    if (puntoObjetivoMapa != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .clip(RoundedCornerShape(LinceRadius.large))
                                .border(
                                    BorderStroke(1.dp, LinceTheme.colors.outlineVariant),
                                    RoundedCornerShape(LinceRadius.large)
                                )
                        ) {
                            MapaRutaMarcaje(
                                puntoObjetivo = puntoObjetivoMapa,
                                puntoUsuario = puntoUsuarioActual,
                                radioMetros = radioPermitido,
                                modifier = Modifier.fillMaxSize(),
                                contextoPunto = if (esVistaCheckOut) {
                                    stringResource(R.string.marcaje_mapa_punto_termino)
                                } else {
                                    stringResource(R.string.marcaje_mapa_punto_recogida)
                                },
                                distanciaDirecta = distanciaTexto
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(168.dp)
                                .clip(RoundedCornerShape(LinceRadius.large))
                                .background(LinceTheme.colors.surfaceContainerLow),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                modifier = Modifier.size(124.dp),
                                shape = CircleShape,
                                color = LinceTheme.colors.greenPrimary.copy(
                                    alpha = 0.04f + (0.07f * pulsoRadar)
                                )
                            ) {}
                            Surface(
                                modifier = Modifier.size(76.dp),
                                shape = CircleShape,
                                color = LinceTheme.colors.greenSageLight
                            ) {}
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = stringResource(R.string.marcaje_ubicacion_gps),
                                tint = LinceTheme.colors.greenPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    if (puntoObjetivoMapa != null || puntoUsuarioActual != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(LinceSpacing.mediumSmall),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (puntoObjetivoMapa != null) {
                                LeyendaMapaMarcaje(
                                    color = LinceTheme.colors.greenPrimary,
                                    texto = if (esVistaCheckOut) {
                                        stringResource(R.string.marcaje_mapa_punto_termino)
                                    } else {
                                        stringResource(R.string.marcaje_mapa_punto_recogida)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (puntoUsuarioActual != null) {
                                LeyendaMapaMarcaje(
                                    color = LinceTheme.colors.mapGpsBlue,
                                    texto = stringResource(R.string.marcaje_mapa_tu_ubicacion),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    LinceStatusBadge(
                        text = buildString {
                            append(context.getString(R.string.marcaje_radio_valor, radioPermitido))
                            distanciaTexto?.let {
                                append(" · ")
                                append(it)
                            }
                        },
                        tone = LinceStatusTone.Neutral
                    )
                }
            }

            LinceSurfaceCard(
                variant = LinceSurfaceCardVariant.Default,
                contentPadding = LinceSpacing.medium
            ) {
                LinceSectionHeader(title = stringResource(R.string.detalle_trazabilidad))
                Spacer(modifier = Modifier.height(LinceSpacing.small))
                LineaTiempoMarcaje(
                    pasos = listOf(
                        PasoMarcaje(
                            titulo = stringResource(R.string.marcaje_mapa_punto_recogida),
                            completado = checkInRegistrado ||
                                    servicio.estado == EstadoServicio.EN_CURSO ||
                                    servicio.estado == EstadoServicio.FINALIZADO,
                            actual = !checkInRegistrado && servicio.estado == EstadoServicio.CONFIRMADO
                        ),
                        PasoMarcaje(
                            titulo = stringResource(R.string.dashboard_servicio_en_curso),
                            completado = servicio.estado == EstadoServicio.FINALIZADO ||
                                    (estadoEnRuta && checkOutRegistrado),
                            actual = servicio.estado == com.example.linceops.model.EstadoServicio.EN_CURSO
                        ),
                        PasoMarcaje(
                            titulo = stringResource(R.string.marcaje_mapa_punto_termino),
                            completado = checkOutRegistrado || servicio.estado == com.example.linceops.model.EstadoServicio.FINALIZADO,
                            actual = esVistaCheckOut &&
                                    !checkOutRegistrado &&
                                    servicio.estado != EstadoServicio.FINALIZADO
                        )
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(LinceSpacing.small))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(LinceSpacing.small)
        ) {
            val enCurso = servicio.estado == EstadoServicio.EN_CURSO

            LinceButton(
                text = stringResource(R.string.marcaje_registrar_checkin),
                onClick = {
                    obtenerUbicacion(esCheckIn = true) { lat, lon -> onCheckIn(lat, lon) }
                },
                icon = Icons.Default.LocationOn,
                variant = if (enCurso) LinceButtonVariant.Outline else LinceButtonVariant.Primary,
                fullWidth = true
            )
            LinceButton(
                text = stringResource(R.string.marcaje_checkout),
                onClick = {
                    obtenerUbicacion(esCheckIn = false) { lat, lon -> onCheckOut(lat, lon) }
                },
                icon = Icons.Default.CheckCircle,
                variant = if (enCurso) LinceButtonVariant.Primary else LinceButtonVariant.Outline,
                fullWidth = true
            )
            Text(
                text = stringResource(R.string.marcaje_verificacion),
                style = MaterialTheme.typography.labelSmall,
                color = LinceTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private data class PasoMarcaje(
    val titulo: String,
    val completado: Boolean,
    val actual: Boolean
)

@Composable
private fun LineaTiempoMarcaje(pasos: List<PasoMarcaje>) {
    val colores = LinceTheme.colors
    Column {
        pasos.forEachIndexed { index, paso ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(LinceSpacing.small),
                verticalAlignment = Alignment.Top
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(12.dp),
                        shape = CircleShape,
                        color = when {
                            paso.completado -> colores.success
                            paso.actual -> colores.greenPrimary
                            else -> colores.surfaceContainerHigh
                        },
                        border = BorderStroke(
                            1.dp,
                            when {
                                paso.completado -> colores.success
                                paso.actual -> colores.greenPrimary
                                else -> colores.outlineVariant
                            }
                        )
                    ) {}
                    if (index < pasos.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(22.dp)
                                .background(colores.outlineVariant)
                        )
                    }
                }
                Text(
                    text = paso.titulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (paso.actual || paso.completado) {
                        colores.textPrimary
                    } else {
                        colores.textSecondary
                    },
                    fontWeight = if (paso.actual) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.padding(top = 1.dp)
                )
                if (paso.actual) {
                    Spacer(modifier = Modifier.weight(1f))
                    LinceStatusBadge(
                        text = paso.titulo,
                        tone = LinceStatusTone.Info
                    )
                }
            }
        }
    }
}

@Composable
private fun LeyendaMapaMarcaje(
    color: androidx.compose.ui.graphics.Color,
    texto: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(LinceSpacing.xSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            color = LinceTheme.colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun NotaOperativaMarcaje(texto: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(LinceSpacing.xSmall),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = LinceTheme.colors.goldAccent,
            modifier = Modifier
                .padding(top = 1.dp)
                .size(15.dp)
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.bodySmall,
            color = LinceTheme.colors.textSecondary
        )
    }
}

private fun calcularHaversineMetros(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    return 2 * r * atan2(sqrt(a), sqrt(1 - a))
}