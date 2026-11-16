package com.example.linceops.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.linceops.ui.components.LUGARES_SUGERIDOS
import com.example.linceops.ui.components.PuntoMapa
import com.example.linceops.ui.components.distanciaKm
import com.example.linceops.ui.components.textoCoordenadas
import com.example.linceops.ui.theme.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.roundToInt

private const val RADIO_GPS_METROS = 500
private val PUNTO_OPERATIVO_RESPALDO = PuntoMapa(-41.4693, -72.9424)
private val PATRON_COORDENADAS = Regex("""(-?\d{1,3}[.,]\d+)\s*[,;]\s*(-?\d{1,3}[.,]\d+)""")

@Composable
fun MarcajeScreen(
    servicio: Servicio,
    estadoActual: EstadoServicio?,
    hayInternet: Boolean,
    onCheckIn: (Double, Double, Int) -> Unit,
    onCheckInContingencia: (Double, Double, String, String, Int) -> Unit,
    onCheckOut: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var mensajeEstado by remember(estadoActual) { mutableStateOf(mensajeParaEstado(context, estadoActual)) }
    var accionPendiente by remember { mutableStateOf<(() -> Unit)?>(null) }
    var ubicacionEnProceso by remember { mutableStateOf(false) }
    var ubicacionActual by remember { mutableStateOf<PuntoMapa?>(null) }
    var puntoInicio by remember(servicio.id) {
        mutableStateOf(puntoLocal(servicio.pickup, servicio.origenDestino) ?: PUNTO_OPERATIVO_RESPALDO)
    }
    var mostrarDialogoContingencia by remember { mutableStateOf(false) }
    var motivoContingencia by remember { mutableStateOf("") }
    var notaContingencia by remember { mutableStateOf("") }

    val distanciaActual = ubicacionActual?.let { distanciaMetrosEntre(puntoInicio, it) }
    val dentroDeRango = distanciaActual != null && distanciaActual <= RADIO_GPS_METROS
    val puedeCheckIn = estadoActual == EstadoServicio.CONFIRMADO
    val puedeCheckOut = estadoActual == EstadoServicio.EN_CURSO

    val textoDistancia = when (val distancia = distanciaActual) {
        null -> stringResource(R.string.marcaje_sin_gps)
        in 0..RADIO_GPS_METROS -> stringResource(R.string.marcaje_dentro_rango, formatearMetros(distancia))
        else -> stringResource(R.string.marcaje_fuera_rango, formatearMetros(distancia))
    }
    val colorRango = when {
        distanciaActual == null -> LinceTertiary
        dentroDeRango -> LinceGreenPrimary
        else -> LinceDangerRed
    }
    val fondoRango = when {
        distanciaActual == null -> LinceSurfaceContainerLow
        dentroDeRango -> LinceGreenSageLight
        else -> LinceTertiaryContainer.copy(alpha = 0.22f)
    }
    val textoAdvertencia = distanciaActual?.let {
        stringResource(R.string.marcaje_advertencia_fuera, formatearMetros(it))
    } ?: stringResource(R.string.marcaje_advertencia_sin_gps)

    val motivosContingencia = listOf(
        stringResource(R.string.marcaje_motivo_gps),
        stringResource(R.string.marcaje_motivo_offline),
        stringResource(R.string.marcaje_motivo_pickup)
    )

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

    fun obtenerUbicacion(onSuccess: (Double, Double) -> Unit) {
        if (ubicacionEnProceso) return

        val permisoFino = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val permisoAproximado = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!permisoFino && !permisoAproximado) {
            accionPendiente = { obtenerUbicacion(onSuccess) }
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }

        ubicacionEnProceso = true
        mensajeEstado = context.getString(R.string.marcaje_obteniendo)
        val prioridad = if (permisoFino) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }
        val token = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(prioridad, token.token)
            .addOnSuccessListener { ubicacion ->
                if (ubicacion != null) {
                    val punto = PuntoMapa(ubicacion.latitude, ubicacion.longitude)
                    ubicacionActual = punto
                    onSuccess(ubicacion.latitude, ubicacion.longitude)
                    ubicacionEnProceso = false
                } else {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { ultima ->
                            if (ultima != null) {
                                val punto = PuntoMapa(ultima.latitude, ultima.longitude)
                                ubicacionActual = punto
                                onSuccess(ultima.latitude, ultima.longitude)
                            } else {
                                mensajeEstado = context.getString(R.string.marcaje_no_ubicacion_activa)
                            }
                            ubicacionEnProceso = false
                        }
                        .addOnFailureListener {
                            mensajeEstado = context.getString(R.string.marcaje_no_ubicacion)
                            ubicacionEnProceso = false
                        }
                }
            }
            .addOnFailureListener {
                mensajeEstado = context.getString(R.string.marcaje_error_ubicacion)
                ubicacionEnProceso = false
            }
    }

    fun actualizarUbicacion() {
        obtenerUbicacion { _, _ ->
            mensajeEstado = context.getString(R.string.marcaje_ubicacion_actualizada)
        }
    }

    LaunchedEffect(servicio.id, hayInternet) {
        if (puntoLocal(servicio.pickup, servicio.origenDestino) == null && hayInternet) {
            val consulta = servicio.pickup.ifBlank {
                servicio.origenDestino.substringBefore("->").trim()
            }
            val geocodificado = geocodificarPunto(context, consulta)
            if (geocodificado != null) {
                puntoInicio = geocodificado
            }
        }
    }

    LaunchedEffect(servicio.id) {
        val permisoFino = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val permisoAproximado = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (permisoFino || permisoAproximado) {
            actualizarUbicacion()
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
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.marcaje_control_ruta),
                    style = MaterialTheme.typography.labelSmall,
                    color = LinceTextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = stringResource(R.string.marcaje_titulo),
                    style = MaterialTheme.typography.headlineMedium,
                    color = LinceGreenPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = LinceSurfaceContainerHigh,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = stringResource(R.string.marcaje_id, servicio.id),
                        style = MaterialTheme.typography.labelMedium,
                        color = LinceGreenDark,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.marcaje_geo_titulo),
                            style = MaterialTheme.typography.titleMedium,
                            color = LinceGreenDark,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = LinceGreenSageLight,
                            shape = RoundedCornerShape(50)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(LinceGreenPrimary, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.marcaje_gps),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LinceGreenDark,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

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
                                    tint = LinceCardSurface,
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
                                    text = stringResource(R.string.marcaje_radio),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = LinceTextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = fondoRango,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = colorRango,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.marcaje_punto_inicio),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LinceTextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                )
                            }
                            Text(
                                text = servicio.pickup.ifBlank {
                                    servicio.origenDestino.substringBefore("->").trim()
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = LinceTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stringResource(R.string.marcaje_coordenada, textoCoordenadas(puntoInicio)),
                                style = MaterialTheme.typography.labelSmall,
                                color = LinceTextSecondary
                            )
                            Text(
                                text = textoDistancia,
                                style = MaterialTheme.typography.labelMedium,
                                color = colorRango,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(
                                onClick = { actualizarUbicacion() },
                                enabled = !ubicacionEnProceso,
                                colors = ButtonDefaults.textButtonColors(contentColor = colorRango)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.marcaje_actualizar))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

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
                            textAlign = TextAlign.Start,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(104.dp))
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = LinceCardSurface,
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        obtenerUbicacion { lat, lon ->
                            val punto = PuntoMapa(lat, lon)
                            val distancia = distanciaMetrosEntre(puntoInicio, punto)
                            if (distancia <= RADIO_GPS_METROS) {
                                onCheckIn(lat, lon, distancia)
                            } else {
                                mensajeEstado = context.getString(
                                    R.string.marcaje_checkin_bloqueado,
                                    formatearMetros(distancia)
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = puedeCheckIn && !ubicacionEnProceso && (distanciaActual == null || dentroDeRango),
                    colors = ButtonDefaults.buttonColors(containerColor = LinceGreenPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = LinceCardSurface,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.marcaje_confirmar_checkin),
                        style = MaterialTheme.typography.titleMedium,
                        color = LinceCardSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (puedeCheckIn && !dentroDeRango) {
                    Text(
                        text = textoAdvertencia,
                        style = MaterialTheme.typography.labelMedium,
                        color = colorRango,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedButton(
                        onClick = {
                            motivoContingencia = ""
                            notaContingencia = ""
                            mostrarDialogoContingencia = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 54.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, LinceTertiary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = LinceOnTertiaryContainer,
                            containerColor = LinceTertiaryContainer.copy(alpha = 0.18f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = LinceTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.marcaje_contingencia),
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                OutlinedButton(
                    onClick = { obtenerUbicacion { lat, lon -> onCheckOut(lat, lon) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = puedeCheckOut && !ubicacionEnProceso,
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

    if (mostrarDialogoContingencia) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoContingencia = false },
            title = {
                Text(
                    text = stringResource(R.string.marcaje_dialogo_titulo),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.marcaje_dialogo_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = LinceTextSecondary
                    )
                    motivosContingencia.forEach { opcion ->
                        FilterChip(
                            selected = motivoContingencia == opcion,
                            onClick = { motivoContingencia = opcion },
                            label = { Text(opcion) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    OutlinedTextField(
                        value = notaContingencia,
                        onValueChange = { notaContingencia = it },
                        label = { Text(stringResource(R.string.marcaje_nota_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val referencia = ubicacionActual ?: puntoInicio
                        onCheckInContingencia(
                            referencia.latitud,
                            referencia.longitud,
                            motivoContingencia,
                            notaContingencia,
                            distanciaActual ?: 0
                        )
                        mostrarDialogoContingencia = false
                        motivoContingencia = ""
                        notaContingencia = ""
                    },
                    enabled = motivoContingencia.isNotBlank() &&
                        notaContingencia.isNotBlank() &&
                        !ubicacionEnProceso,
                    colors = ButtonDefaults.buttonColors(containerColor = LinceTertiary)
                ) {
                    Text(
                        text = stringResource(R.string.marcaje_registrar_justificacion),
                        color = LinceCardSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoContingencia = false }) {
                    Text(stringResource(R.string.accion_cancelar))
                }
            }
        )
    }
}

private fun puntoLocal(pickup: String, origenDestino: String): PuntoMapa? =
    buscarPuntoConocido(pickup, origenDestino)
        ?: extraerCoordenadas(pickup)
        ?: extraerCoordenadas(origenDestino)

private fun buscarPuntoConocido(pickup: String, origenDestino: String): PuntoMapa? {
    val origen = origenDestino.substringBefore("->").trim()
    val candidatos = listOf(pickup, origen).filter { it.isNotBlank() }
    return candidatos.firstNotNullOfOrNull { candidato ->
        LUGARES_SUGERIDOS.firstOrNull { lugar ->
            candidato.contains(lugar.nombre, ignoreCase = true) ||
                lugar.nombre.contains(candidato, ignoreCase = true)
        }?.punto
    }
}

private fun extraerCoordenadas(texto: String): PuntoMapa? {
    val coincidencia = PATRON_COORDENADAS.find(texto) ?: return null
    val latitud = coincidencia.groupValues[1].replace(',', '.').toDoubleOrNull() ?: return null
    val longitud = coincidencia.groupValues[2].replace(',', '.').toDoubleOrNull() ?: return null
    if (latitud !in -90.0..90.0 || longitud !in -180.0..180.0) return null
    return PuntoMapa(latitud, longitud)
}

private suspend fun geocodificarPunto(context: Context, consulta: String): PuntoMapa? {
    if (consulta.isBlank()) return null
    return withContext(Dispatchers.IO) {
        runCatching {
            if (!Geocoder.isPresent()) return@runCatching null
            val geocoder = Geocoder(context, Locale("es", "CL"))
            @Suppress("DEPRECATION")
            val resultados = geocoder.getFromLocationName(consulta, 1)
            resultados?.firstOrNull()?.let { PuntoMapa(it.latitude, it.longitude) }
        }.getOrNull()
    }
}

private fun distanciaMetrosEntre(origen: PuntoMapa, destino: PuntoMapa): Int =
    (distanciaKm(origen, destino) * 1000).roundToInt()

private fun formatearMetros(metros: Int): String =
    String.format(Locale.US, "%,d", metros).replace(',', '.')

private fun mensajeParaEstado(context: Context, estado: EstadoServicio?): String = when (estado) {
    EstadoServicio.CONFIRMADO -> context.getString(R.string.marcaje_estado_listo)
    EstadoServicio.EN_CURSO -> context.getString(R.string.marcaje_estado_en_curso)
    EstadoServicio.FINALIZADO -> context.getString(R.string.marcaje_estado_finalizado)
    EstadoServicio.RECHAZADO -> context.getString(R.string.marcaje_estado_rechazado)
    EstadoServicio.PENDIENTE -> context.getString(R.string.marcaje_estado_pendiente)
    null -> context.getString(R.string.marcaje_estado_nulo)
}
