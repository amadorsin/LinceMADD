package com.example.linceops.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linceops.R
import com.example.linceops.model.EstadoServicio
import com.example.linceops.model.Marcaje
import com.example.linceops.model.ParticipacionServicio
import com.example.linceops.model.RolUsuario
import com.example.linceops.model.Servicio
import com.example.linceops.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(
    servicio: Servicio,
    rolUsuario: RolUsuario,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit,
    onReasignar: () -> Unit,
    onIrAMarcaje: () -> Unit,
    usuarioId: String? = null
) {

    val idReferencia = usuarioId ?: when (rolUsuario) {
        RolUsuario.CONDUCTOR -> servicio.conductorId
        RolUsuario.GUIA -> servicio.guiaId
        else -> null
    }

    val estadoActual = if (rolUsuario == RolUsuario.ADMINISTRADOR) {
        servicio.estado
    } else {
        servicio.participaciones
            .firstOrNull { it.usuarioId == idReferencia }
            ?.estado ?: servicio.estado
    }

    val puedeMarcar = (rolUsuario == RolUsuario.CONDUCTOR || rolUsuario == RolUsuario.GUIA) &&
            (estadoActual == EstadoServicio.CONFIRMADO || estadoActual == EstadoServicio.EN_CURSO)

    val tieneAcciones =
        (rolUsuario == RolUsuario.ADMINISTRADOR && estadoActual == EstadoServicio.RECHAZADO) ||
                puedeMarcar ||
                (estadoActual == EstadoServicio.PENDIENTE && rolUsuario != RolUsuario.ADMINISTRADOR)

    Scaffold(
        containerColor = LinceCreamBackground,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = stringResource(R.string.detalle_final_update, servicio.reservaId),
                                style = MaterialTheme.typography.labelSmall,
                                color = LinceTextSecondary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = servicio.titulo,
                                style = MaterialTheme.typography.titleLarge,
                                color = LinceGreenPrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    actions = {
                        EstadoBadge(
                            estado = estadoActual,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = LinceCreamBackground,
                        titleContentColor = LinceTextPrimary
                    )
                )
                HorizontalDivider(color = LinceOutlineVariant.copy(alpha = 0.55f))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = LinceSurfaceContainerHigh,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = stringResource(R.string.detalle_id, servicio.id),
                            style = MaterialTheme.typography.labelSmall,
                            color = LinceTextSecondary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    Surface(
                        color = LinceGreenSageLight,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = etiquetaTipo(servicio.tipo),
                            style = MaterialTheme.typography.labelSmall,
                            color = LinceGreenDark,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    if (!servicio.statusPL.isNullOrBlank()) {
                        Surface(
                            color = LinceSurfaceContainerHigh,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "PL: ${servicio.statusPL}",
                                style = MaterialTheme.typography.labelSmall,
                                color = LinceTextPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                TarjetaFinal(
                    icono = Icons.Default.LocationOn,
                    titulo = stringResource(R.string.detalle_ruta_titulo)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FichaDato(
                            etiqueta = stringResource(R.string.detalle_fecha),
                            valor = servicio.fecha,
                            modifier = Modifier.weight(1f),
                            icono = Icons.Default.DateRange
                        )
                        FichaDato(
                            etiqueta = stringResource(R.string.detalle_hora_salida),
                            valor = servicio.hora,
                            modifier = Modifier.weight(1f),
                            icono = Icons.Default.PlayArrow
                        )
                    }

                    ParadasRuta(
                        origenDestino = servicio.origenDestino,
                        hora = servicio.hora
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FichaDato(
                            etiqueta = stringResource(R.string.detalle_punto_recogida),
                            valor = servicio.pickup,
                            modifier = Modifier.weight(1f),
                            icono = Icons.Default.Place
                        )
                        FichaDato(
                            etiqueta = stringResource(R.string.detalle_hotel_referencia),
                            valor = servicio.hotel,
                            modifier = Modifier.weight(1f),
                            icono = Icons.Default.LocationOn
                        )
                    }

                    FichaDato(
                        etiqueta = "HOTEL DE REFERENCIA",
                        valor = servicio.hotel.ifBlank { "Sin hotel registrado" },
                        modifier = Modifier.fillMaxWidth(),
                        icono = Icons.Default.Home
                    )

                TarjetaFinal(
                    icono = Icons.Default.Person,
                    titulo = stringResource(R.string.detalle_equipo)
                ) {
                    val textoVehiculo = if (servicio.capacidadVehiculo > 0) {
                        "${servicio.vehiculo} (Cap. ${servicio.capacidadVehiculo} pax)"
                    } else {
                        servicio.vehiculo
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FichaDato(
                            etiqueta = stringResource(R.string.detalle_conductor),
                            valor = servicio.conductorId ?: stringResource(R.string.detalle_sin_conductor),
                            modifier = Modifier.weight(1f),
                            icono = Icons.Default.Person
                        )
                        FichaDato(
                            etiqueta = stringResource(R.string.detalle_vehiculo),
                            valor = servicio.vehiculo,
                            modifier = Modifier.weight(1f),
                            icono = Icons.AutoMirrored.Filled.Send
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FichaDato(
                            etiqueta = stringResource(R.string.detalle_guia),
                            valor = servicio.guiaId ?: stringResource(R.string.detalle_sin_guia),
                            modifier = Modifier.weight(1f),
                            icono = Icons.Default.Face
                        )
                        FichaDato(
                            etiqueta = stringResource(R.string.detalle_agencia),
                            valor = servicio.agencia,
                            modifier = Modifier.weight(1f),
                            icono = Icons.Default.AccountBox
                        )
                    }

                    FichaDato(
                        etiqueta = stringResource(R.string.detalle_idioma),
                        valor = servicio.idioma,
                        modifier = Modifier.fillMaxWidth(),
                        icono = Icons.Default.Face
                    )
                }

                TarjetaFinal(
                    icono = Icons.Default.Person,
                    titulo = stringResource(R.string.detalle_nomina)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.detalle_detalle_nominal),
                            style = MaterialTheme.typography.labelMedium,
                            color = LinceTextSecondary
                        )
                        Surface(
                            color = LinceGreenSageLight,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = stringResource(R.string.detalle_pasajeros_count, servicio.pasajeros),
                                style = MaterialTheme.typography.labelSmall,
                                color = LinceGreenDark,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    if (servicio.detallePasajeros.isEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = LinceCreamBackground,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.detalle_lista_pendiente, servicio.pasajeros),
                                style = MaterialTheme.typography.bodySmall,
                                color = LinceTextSecondary,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        servicio.detallePasajeros.forEach { pasajero ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = LinceCreamBackground,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = pasajero.nombre,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = LinceTextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Face,
                                            contentDescription = null,
                                            tint = LinceGreenPrimary.copy(alpha = 0.85f),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = pasajero.idioma,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = LinceTextSecondary
                                        )
                                    }
                                    val tieneRequerimientoReal = pasajero.requerimientos.isNotBlank() &&
                                            !pasajero.requerimientos.equals("Sin requerimientos especiales", ignoreCase = true)

                                    if (tieneRequerimientoReal) {
                                        Row(verticalAlignment = Alignment.Top) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = null,
                                                tint = LinceTertiary,
                                                modifier = Modifier
                                                    .padding(top = 1.dp)
                                                    .size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = pasajero.requerimientos,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = LinceTextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                TarjetaFinal(
                    icono = Icons.Default.CheckCircle,
                    titulo = stringResource(R.string.detalle_inclusiones)
                ) {
                    if (servicio.inclusiones.isEmpty()) {
                        Text(
                            text = stringResource(R.string.detalle_sin_inclusiones),
                            style = MaterialTheme.typography.bodySmall,
                            color = LinceTextSecondary
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            servicio.inclusiones.forEach { inclusion ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = LinceGreenSageLight,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = LinceGreenPrimary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = inclusion,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = LinceGreenDark,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    FichaDato(
                        etiqueta = stringResource(R.string.detalle_instrucciones),
                        valor = servicio.instrucciones.ifBlank { stringResource(R.string.detalle_sin_instrucciones) },
                        modifier = Modifier.fillMaxWidth(),
                        colorFondo = LinceTertiaryContainer.copy(alpha = 0.16f),
                        icono = Icons.Default.Info
                    )
                    FichaDato(
                        etiqueta = stringResource(R.string.detalle_observaciones),
                        valor = servicio.observaciones.ifBlank { stringResource(R.string.detalle_sin_observaciones) },
                        modifier = Modifier.fillMaxWidth(),
                        colorFondo = LinceSurfaceContainerLow,
                        icono = Icons.Default.Edit
                    )
                }

                val registrosMarcaje = servicio.participaciones.filter {
                    it.checkIn != null || it.checkOut != null
                }
                if (registrosMarcaje.isNotEmpty()) {
                    TarjetaFinal(
                        icono = Icons.Default.CheckCircle,
                        titulo = stringResource(R.string.detalle_trazabilidad)
                    ) {
                        registrosMarcaje.forEach { participacion ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = LinceSurfaceContainerLow,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = etiquetaParticipante(servicio, participacion.usuarioId),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = LinceGreenDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                    participacion.checkIn?.let { marcaje ->
                                        MarcajeFila(
                                            titulo = stringResource(R.string.comun_checkin),
                                            marcaje = marcaje,
                                            color = LinceGreenPrimary,
                                            icono = Icons.Default.PlayArrow
                                        )
                                        val motivo = motivoContingencia(
                                            servicio = servicio,
                                            participacion = participacion
                                        )
                                        if (motivo == null) {
                                            Surface(
                                                color = LinceGreenSageLight,
                                                shape = RoundedCornerShape(50)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(
                                                        horizontal = 10.dp,
                                                        vertical = 5.dp
                                                    ),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = LinceGreenPrimary,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(5.dp))
                                                    Text(
                                                        text = stringResource(R.string.detalle_verificado_rango),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = LinceGreenDark,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        } else {
                                            Surface(
                                                color = LinceTertiaryContainer.copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(
                                                        horizontal = 10.dp,
                                                        vertical = 7.dp
                                                    ),
                                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Info,
                                                            contentDescription = null,
                                                            tint = LinceTertiary,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(5.dp))
                                                        Text(
                                                            text = stringResource(R.string.detalle_contingencia_auditado),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = LinceOnTertiaryContainer,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                    Text(
                                                        text = stringResource(R.string.detalle_motivo, motivo),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = LinceOnTertiaryContainer
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    participacion.checkOut?.let { marcaje ->
                                        MarcajeFila(
                                            titulo = stringResource(R.string.comun_checkout),
                                            marcaje = marcaje,
                                            color = LinceTertiary,
                                            icono = Icons.Default.CheckCircle
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (tieneAcciones) {
                BarraAcciones(
                    rolUsuario = rolUsuario,
                    estadoActual = estadoActual,
                    onAceptar = onAceptar,
                    onRechazar = onRechazar,
                    onReasignar = onReasignar,
                    onIrAMarcaje = onIrAMarcaje
                )
            }
        }
    }
}

@Composable
private fun TarjetaFinal(
    icono: ImageVector,
    titulo: String,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LinceCardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, LinceOutlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SeccionHeader(icono = icono, titulo = titulo)
            HorizontalDivider(color = LinceOutlineVariant.copy(alpha = 0.5f))
            contenido()
        }
    }
}

@Composable
private fun MarcajeFila(
    titulo: String,
    marcaje: Marcaje,
    color: Color,
    icono: ImageVector
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .padding(top = 1.dp)
                .size(15.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            )
            Text(
                text = formatearFechaHora(marcaje.fechaHoraMillis),
                style = MaterialTheme.typography.bodySmall,
                color = LinceTextPrimary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = stringResource(
                    R.string.detalle_lat_lon,
                    formatearCoordenada(marcaje.latitud),
                    formatearCoordenada(marcaje.longitud)
                ),
                style = MaterialTheme.typography.labelSmall,
                color = LinceTextSecondary
            )
        }
    }
}

@Composable
private fun BarraAcciones(
    rolUsuario: RolUsuario,
    estadoActual: EstadoServicio,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit,
    onReasignar: () -> Unit,
    onIrAMarcaje: () -> Unit
) {
    Surface(
        color = LinceCardSurface,
        shadowElevation = 10.dp
    ) {
        Column {
            HorizontalDivider(color = LinceOutlineVariant.copy(alpha = 0.6f))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (rolUsuario == RolUsuario.ADMINISTRADOR && estadoActual == EstadoServicio.RECHAZADO) {
                    Button(
                        onClick = onReasignar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LinceGreenPrimary)
                    ) {
                        Text(stringResource(R.string.detalle_reasignar), color = LinceCardSurface, fontWeight = FontWeight.SemiBold)
                    }
                } else if (
                    (rolUsuario == RolUsuario.CONDUCTOR || rolUsuario == RolUsuario.GUIA) &&
                    (estadoActual == EstadoServicio.CONFIRMADO || estadoActual == EstadoServicio.EN_CURSO)
                ) {
                    Button(
                        onClick = onIrAMarcaje,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LinceGreenPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = LinceCardSurface,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.detalle_ir_marcaje), color = LinceCardSurface, fontWeight = FontWeight.SemiBold)
                    }
                } else if (estadoActual == EstadoServicio.PENDIENTE && rolUsuario != RolUsuario.ADMINISTRADOR) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onRechazar,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, LinceDangerRed),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LinceDangerRed)
                        ) {
                            Text(stringResource(R.string.accion_rechazar), fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = onAceptar,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LinceGreenPrimary)
                        ) {
                            Text(stringResource(R.string.accion_aceptar), color = LinceCardSurface, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeccionHeader(
    icono: ImageVector,
    titulo: String,
    colorIcono: Color = LinceGreenPrimary
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(28.dp),
            shape = RoundedCornerShape(9.dp),
            color = colorIcono.copy(alpha = 0.12f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icono,
                    contentDescription = titulo,
                    tint = colorIcono,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            color = LinceGreenDark,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FichaDato(
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier,
    colorFondo: Color = LinceSurfaceContainerLow,
    icono: ImageVector? = null
) {
    Surface(
        modifier = modifier,
        color = colorFondo,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icono?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = LinceGreenPrimary.copy(alpha = 0.85f),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                }
                Text(
                    text = etiqueta,
                    style = MaterialTheme.typography.labelSmall,
                    color = LinceTextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = valor,
                style = MaterialTheme.typography.bodyMedium,
                color = LinceTextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ParadasRuta(
    origenDestino: String,
    hora: String
) {
    val paradas = origenDestino
        .split("->")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = LinceSurfaceContainerLow,
        shape = RoundedCornerShape(12.dp)
    ) {
        if (paradas.isEmpty()) {
            Text(
                text = origenDestino,
                style = MaterialTheme.typography.bodyMedium,
                color = LinceTextPrimary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(12.dp)
            )
        } else {
            Column(modifier = Modifier.padding(12.dp)) {
                paradas.forEachIndexed { index, parada ->
                    val esUltima = index == paradas.lastIndex
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(9.dp),
                                shape = CircleShape,
                                color = when {
                                    index == 0 -> LinceGreenPrimary
                                    esUltima -> LinceTertiary
                                    else -> LinceGreenPrimary.copy(alpha = 0.45f)
                                }
                            ) {}
                            if (!esUltima) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(26.dp)
                                        .background(LinceOutlineVariant, RoundedCornerShape(1.dp))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when {
                                    index == 0 -> stringResource(R.string.detalle_pickup, hora)
                                    esUltima -> stringResource(R.string.detalle_destino_final)
                                    else -> stringResource(R.string.detalle_parada)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = LinceTextSecondary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            )
                            Text(
                                text = parada,
                                style = MaterialTheme.typography.bodyMedium,
                                color = LinceTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (!esUltima) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun etiquetaParticipante(servicio: Servicio, usuarioId: String): String {
    val rol = when (usuarioId) {
        servicio.conductorId -> stringResource(R.string.rol_conductor)
        servicio.guiaId -> stringResource(R.string.rol_guia)
        else -> stringResource(R.string.rol_participante)
    }
    return "$rol · $usuarioId"
}

@Composable
private fun etiquetaTipo(tipo: String): String = when {
    tipo.startsWith("Transfer", ignoreCase = true) -> stringResource(R.string.tipo_transfer)
    tipo.contains("Tour", ignoreCase = true) -> stringResource(R.string.tipo_tour_guiado)
    tipo.contains("Excursi", ignoreCase = true) -> stringResource(R.string.tipo_excursion)
    else -> tipo
}

private fun rolDeParticipacion(
    servicio: Servicio,
    participacion: ParticipacionServicio
): RolUsuario? = participacion.rol ?: when (participacion.usuarioId) {
    servicio.conductorId -> RolUsuario.CONDUCTOR
    servicio.guiaId -> RolUsuario.GUIA
    else -> null
}

private fun motivoContingencia(
    servicio: Servicio,
    participacion: ParticipacionServicio
): String? {
    val rol = rolDeParticipacion(servicio, participacion) ?: return null
    val prefijo = "[CHECK-IN CONTINGENCIA - ${rol.nombreMostrar}:"
    return servicio.observaciones
        .lineSequence()
        .map { it.trim() }
        .firstOrNull { it.startsWith(prefijo) }
        ?.removePrefix(prefijo)
        ?.substringBefore("| Dist:")
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
}

private fun formatearFechaHora(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(millis))

private fun formatearCoordenada(valor: Double): String =
    String.format(Locale.US, "%.5f", valor)
