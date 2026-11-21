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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.linceops.ui.components.MarcajeMapaDistancia
import com.example.linceops.ui.components.MarcajeSkeleton
import com.example.linceops.ui.components.PuntoMapa
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

@Composable
fun MarcajeScreen(
    servicioId: String,
    servicio: Servicio? = null,
    geocercas: List<GeocercaDto> = emptyList(),
    onCheckIn: (Double, Double) -> Unit,
    onCheckOut: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
){
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

        if (esCheckIn && location.accuracy > 50f) {
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinceCreamBackground)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.marcaje_control_ruta),
                    style = MaterialTheme.typography.labelSmall,
                    color = LinceTextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = stringResource(R.string.marcaje_titulo),
                    style = MaterialTheme.typography.headlineSmall,
                    color = LinceGreenPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Surface(
                color = LinceSurfaceContainerHigh,
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = stringResource(R.string.marcaje_id, servicioId),
                    style = MaterialTheme.typography.labelMedium,
                    color = LinceGreenDark,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = LinceGreenSageLight,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = LinceGreenDark,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.marcaje_recordatorio),
                    style = MaterialTheme.typography.bodySmall,
                    color = LinceGreenDark,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = LinceCardSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = BorderStroke(1.dp, LinceOutlineVariant.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.marcaje_geo_titulo),
                        style = MaterialTheme.typography.titleMedium,
                        color = LinceGreenDark,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = LinceGreenSageLight,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, LinceGreenBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable { esVistaCheckOut = !esVistaCheckOut }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(LinceGreenPrimary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (esVistaCheckOut) {
                                    stringResource(R.string.marcaje_mapa_punto_termino)
                                } else {
                                    stringResource(R.string.marcaje_mapa_punto_recogida)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = LinceGreenDark,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val etiquetaObjetivoActual = if (esVistaCheckOut) {
                    stringResource(R.string.marcaje_mapa_punto_termino)
                } else {
                    stringResource(R.string.marcaje_mapa_punto_recogida)
                }

                val textoDistanciaActual = distanciaMetros?.let { metros ->
                    if (metros >= 1000.0) {
                        stringResource(
                            R.string.marcaje_distancia_km,
                            String.format(Locale.US, "%.2f", metros / 1000.0)
                        )
                    } else {
                        stringResource(R.string.marcaje_distancia_valor, metros.toInt())
                    }
                }

                if (puntoObjetivoMapa != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(LinceSurfaceContainerLow, RoundedCornerShape(16.dp))
                            .border(1.dp, LinceOutlineVariant, RoundedCornerShape(16.dp))
                    ) {
                        MarcajeMapaDistancia(
                            puntoObjetivo = puntoObjetivoMapa,
                            puntoUsuario = puntoUsuarioActual,
                            radioMetros = radioPermitido,
                            modifier = Modifier.fillMaxSize()
                        )

                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(10.dp),
                            color = LinceCardSurface.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, LinceOutlineVariant.copy(alpha = 0.6f))
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)) {
                                Text(
                                    text = stringResource(R.string.marcaje_margen),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LinceTextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = buildString {
                                        append(context.getString(R.string.marcaje_radio_valor, radioPermitido))
                                        textoDistanciaActual?.let {
                                            append(" · ")
                                            append(it)
                                        }
                                        precisionGpsMetros?.let {
                                            append(" · ")
                                            append(context.getString(R.string.marcaje_gps_valor, it.toInt()))
                                        }
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = LinceTextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(10.dp),
                            color = LinceCardSurface.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, LinceOutlineVariant.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(LinceGreenPrimary, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = etiquetaObjetivoActual,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LinceTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (puntoUsuarioActual != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(LinceTheme.colors.mapGpsBlue, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = stringResource(R.string.marcaje_mapa_tu_ubicacion),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = LinceTextSecondary,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(LinceSurfaceContainerLow, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(184.dp),
                            shape = CircleShape,
                            color = LinceGreenPrimary.copy(alpha = 0.04f + (0.07f * pulsoRadar))
                        ) {}
                        Surface(
                            modifier = Modifier.size(132.dp),
                            shape = CircleShape,
                            color = LinceGreenPrimary.copy(alpha = 0.09f)
                        ) {}
                        Surface(
                            modifier = Modifier.size(84.dp),
                            shape = CircleShape,
                            color = LinceGreenSageLight
                        ) {}
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            color = LinceGreenPrimary,
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = stringResource(R.string.marcaje_ubicacion_gps),
                                    tint = LinceOnPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(10.dp),
                            color = LinceCardSurface.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
                                Text(
                                    text = stringResource(R.string.marcaje_margen),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LinceTextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = buildString {
                                        append(context.getString(R.string.marcaje_radio_valor, radioPermitido))
                                        textoDistanciaActual?.let {
                                            append(" · ")
                                            append(it)
                                        }
                                        precisionGpsMetros?.let {
                                            append(" · ")
                                            append(context.getString(R.string.marcaje_gps_valor, it.toInt()))
                                        }
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = LinceTextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = LinceSurfaceContainerLow,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(34.dp),
                            shape = CircleShape,
                            color = LinceGreenSageLight
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = LinceGreenPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.marcaje_estado_geo),
                                style = MaterialTheme.typography.labelSmall,
                                color = LinceTextSecondary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            )
                            Text(
                                text = mensajeEstado,
                                style = MaterialTheme.typography.titleSmall,
                                color = LinceTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = LinceTertiary,
                        modifier = Modifier
                            .padding(top = 1.dp)
                            .size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.marcaje_info_adjunta),
                        style = MaterialTheme.typography.bodySmall,
                        color = LinceTextSecondary,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { obtenerUbicacion(esCheckIn = true) { lat, lon -> onCheckIn(lat, lon) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LinceGreenPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = LinceOnPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.marcaje_registrar_checkin),
                    style = MaterialTheme.typography.titleMedium,
                    color = LinceOnPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedButton(
                onClick = { obtenerUbicacion(esCheckIn = false) { lat, lon -> onCheckOut(lat, lon) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, LinceGreenPrimary),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = LinceGreenDark,
                    containerColor = LinceCardSurface
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = LinceGreenDark,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.marcaje_checkout),
                    style = MaterialTheme.typography.titleMedium,
                    color = LinceGreenDark,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = stringResource(R.string.marcaje_verificacion),
                style = MaterialTheme.typography.labelSmall,
                color = LinceTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun calcularHaversineMetros(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    return 2 * r * atan2(sqrt(a), sqrt(1 - a))
}
