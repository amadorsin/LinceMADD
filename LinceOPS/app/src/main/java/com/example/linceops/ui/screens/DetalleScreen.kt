package com.example.linceops.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.rounded.AccountBox
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linceops.R
import com.example.linceops.model.EstadoServicio
import com.example.linceops.model.Marcaje
import com.example.linceops.model.RolUsuario
import com.example.linceops.model.Servicio
import com.example.linceops.ui.components.LinceButton
import com.example.linceops.ui.components.LinceButtonVariant
import com.example.linceops.ui.components.LinceDivider
import com.example.linceops.ui.components.LinceSectionHeader
import com.example.linceops.ui.components.LinceStatusBadge
import com.example.linceops.ui.components.LinceStatusTone
import com.example.linceops.ui.components.LinceSurfaceCard
import com.example.linceops.ui.components.LinceSurfaceCardVariant
import com.example.linceops.ui.components.LinceUserAvatar
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

    val context = LocalContext.current
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
        containerColor = LinceTheme.colors.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.detalle_final_update, servicio.reservaId),
                                style = MaterialTheme.typography.labelSmall,
                                color = LinceTheme.colors.textSecondary,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(LinceSpacing.small))
                            EstadoBadge(
                                estado = estadoActual
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = LinceTheme.colors.background,
                        titleContentColor = LinceTheme.colors.textPrimary
                    )
                )
                HorizontalDivider(color = LinceTheme.colors.outlineVariant.copy(alpha = 0.6f))
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
                    .padding(
                        start = LinceSpacing.medium,
                        end = LinceSpacing.medium,
                        top = LinceSpacing.small,
                        bottom = 100.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(LinceSpacing.mediumSmall)
            ) {

                LinceSurfaceCard(
                    variant = LinceSurfaceCardVariant.Highlighted,
                    contentPadding = LinceSpacing.medium
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(LinceSpacing.small)) {
                        Text(
                            text = servicio.titulo,
                            style = MaterialTheme.typography.headlineSmall,
                            color = LinceTheme.colors.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            softWrap = true,
                            overflow = TextOverflow.Clip
                        )
                        LinceDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(LinceSpacing.small),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LinceStatusBadge(
                                modifier = Modifier.weight(1f),
                                text = stringResource(R.string.detalle_id, servicio.id),
                                tone = LinceStatusTone.Neutral
                            )
                            servicio.statusPL?.let { pl ->
                                LinceStatusBadge(
                                    text = stringResource(R.string.detalle_pl, pl.name),
                                    tone = LinceStatusTone.Neutral
                                )
                            }
                        }
                        LinceStatusBadge(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(
                                R.string.detalle_tipo_linea,
                                nombreTipo(context, servicio.tipo).uppercase(Locale.getDefault()),
                                servicio.srv.name,
                                servicio.oficina.name
                            ),
                            tone = LinceStatusTone.Info
                        )
                    }
                }

                TarjetaFinal(
                    icono = Icons.Rounded.LocationOn,
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
                            icono = Icons.Rounded.DateRange
                        )
                        FichaDato(
                            etiqueta = stringResource(R.string.detalle_hora_salida),
                            valor = servicio.hora,
                            modifier = Modifier.weight(1f),
                            icono = Icons.Rounded.PlayArrow
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
                            etiqueta = stringResource(R.string.detalle_punto_recogida_full),
                            valor = servicio.pickup.ifBlank { stringResource(R.string.detalle_por_definir) },
                            modifier = Modifier.weight(1f),
                            icono = Icons.Rounded.Place
                        )
                        FichaDato(
                            etiqueta = stringResource(R.string.detalle_punto_destino_full),
                            valor = servicio.dropoff.ifBlank { stringResource(R.string.detalle_segun_ruta) },
                            modifier = Modifier.weight(1f),
                            icono = Icons.Rounded.LocationOn
                        )
                    }

                    FichaDato(
                        etiqueta = stringResource(R.string.detalle_hotel_referencia),
                        valor = servicio.hotel.ifBlank { stringResource(R.string.detalle_sin_hotel) },
                        modifier = Modifier.fillMaxWidth(),
                        icono = Icons.Rounded.Home
                    )
                }

                TarjetaFinal(
                    icono = Icons.Rounded.Person,
                    titulo = stringResource(R.string.detalle_equipo)
                ) {
                    val tipoVeh = servicio.tipoVehiculo?.let {
                        "${nombreTipoVehiculo(context, it)} \u00B7 "
                    } ?: ""
                    val textoVehiculo = if (servicio.capacidadVehiculo > 0) {
                        "$tipoVeh${servicio.vehiculo} ${stringResource(R.string.detalle_capacidad_pax, servicio.capacidadVehiculo)}"
                    } else {
                        "$tipoVeh${servicio.vehiculo}"
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(LinceSpacing.medium)
                    ) {
                        RecursoDetalle(
                            titulo = stringResource(R.string.detalle_conductor),
                            valor = servicio.conductorId ?: stringResource(R.string.detalle_sin_conductor),
                            guia = false,
                            modifier = Modifier.weight(1f),
                        )
                        RecursoDetalle(
                            titulo = stringResource(R.string.detalle_guia),
                            valor = servicio.guiaId ?: stringResource(R.string.detalle_sin_guia),
                            guia = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(LinceSpacing.medium)
                    ) {
                        FichaDato(
                            etiqueta = stringResource(R.string.detalle_vehiculo),
                            valor = textoVehiculo,
                            modifier = Modifier.weight(1f),
                            icono = Icons.Rounded.AccountBox
                        )
                        FichaDato(
                            etiqueta = stringResource(R.string.detalle_agencia),
                            valor = servicio.agencia.takeIf { it.isNotBlank() && it != "Agencia de prueba" }
                                ?: stringResource(R.string.detalle_no_especificada),
                            modifier = Modifier.weight(1f),
                            icono = Icons.Rounded.AccountBox
                        )
                    }

                    FichaDato(
                        etiqueta = stringResource(R.string.detalle_idioma),
                        valor = servicio.idiomaRequerido?.let {
                            "${nombreIdioma(context, it)} (${it.name})"
                        } ?: stringResource(R.string.detalle_no_especificado),
                        modifier = Modifier.fillMaxWidth(),
                        icono = Icons.Rounded.Face
                    )
                }

                TarjetaFinal(
                    icono = Icons.Rounded.Person,
                    titulo = stringResource(R.string.detalle_nomina)
                ) {
                    LinceSectionHeader(
                        title = stringResource(R.string.detalle_detalle_nominal),
                        action = {
                            LinceStatusBadge(
                                text = stringResource(R.string.detalle_pasajeros_count, servicio.pasajeros),
                                tone = LinceStatusTone.Info
                            )
                        }
                    )

                    if (servicio.detallePasajeros.isEmpty()) {
                        Text(
                            text = stringResource(R.string.detalle_lista_pendiente, servicio.pasajeros),
                            style = MaterialTheme.typography.bodySmall,
                            color = LinceTheme.colors.textSecondary
                        )
                    } else {
                        servicio.detallePasajeros.forEachIndexed { index, pasajero ->
                            if (index > 0) LinceDivider()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = LinceSpacing.xSmall),
                                horizontalArrangement = Arrangement.spacedBy(LinceSpacing.small),
                                verticalAlignment = Alignment.Top
                            ) {
                                LinceUserAvatar(
                                    initials = pasajero.nombre.split(" ")
                                        .filter { it.isNotBlank() }
                                        .take(2)
                                        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                        .joinToString("")
                                        .ifBlank { "P" },
                                    size = 34.dp
                                )
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(LinceSpacing.xSmall)
                                ) {
                                    Text(
                                        text = pasajero.nombre,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = LinceTheme.colors.textPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    LinceStatusBadge(
                                        text = pasajero.idioma,
                                        tone = LinceStatusTone.Info
                                    )
                                    val tieneRequerimientoReal = pasajero.requerimientos.isNotBlank() &&
                                            !pasajero.requerimientos.equals("Sin requerimientos especiales", ignoreCase = true)

                                    if (tieneRequerimientoReal) {
                                        Text(
                                            text = pasajero.requerimientos,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = LinceTheme.colors.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                TarjetaFinal(
                    icono = Icons.Rounded.CheckCircle,
                    titulo = stringResource(R.string.detalle_inclusiones)
                ) {
                    if (servicio.inclusiones.isEmpty()) {
                        Text(
                            text = stringResource(R.string.detalle_sin_inclusiones),
                            style = MaterialTheme.typography.bodySmall,
                            color = LinceTextSecondary
                        )
                    } else {
                        servicio.inclusiones.forEachIndexed { index, inclusion ->
                            if (index > 0) LinceDivider()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = LinceSpacing.xSmall),
                                horizontalArrangement = Arrangement.spacedBy(LinceSpacing.small),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = LinceTheme.colors.success,
                                    modifier = Modifier.size(17.dp)
                                )
                                Text(
                                    text = inclusion,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LinceTheme.colors.textPrimary
                                )
                            }
                        }
                    }

                    LinceDivider()
                    FichaDato(
                        etiqueta = stringResource(R.string.detalle_instrucciones),
                        valor = servicio.instrucciones.ifBlank { stringResource(R.string.detalle_sin_instrucciones) },
                        modifier = Modifier.fillMaxWidth(),
                        icono = Icons.Rounded.Info
                    )
                    FichaDato(
                        etiqueta = stringResource(R.string.detalle_observaciones),
                        valor = servicio.observaciones.ifBlank { stringResource(R.string.detalle_sin_observaciones) },
                        modifier = Modifier.fillMaxWidth(),
                        icono = Icons.Rounded.Edit
                    )
                }

                val registrosMarcaje = servicio.participaciones.filter {
                    it.checkIn != null || it.checkOut != null
                }
                if (registrosMarcaje.isNotEmpty()) {
                    TarjetaFinal(
                        icono = Icons.Rounded.CheckCircle,
                        titulo = stringResource(R.string.detalle_trazabilidad)
                    ) {
                        registrosMarcaje.forEachIndexed { index, participacion ->
                            if (index > 0) LinceDivider()
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = LinceSpacing.small),
                                verticalArrangement = Arrangement.spacedBy(LinceSpacing.small)
                            ) {
                                LinceSectionHeader(
                                    title = etiquetaParticipante(context, servicio, participacion.usuarioId)
                                )
                                participacion.checkIn?.let { marcaje ->
                                    MarcajeFila(
                                        titulo = stringResource(R.string.comun_checkin),
                                        marcaje = marcaje,
                                        color = LinceTheme.colors.success,
                                        icono = Icons.Rounded.PlayArrow
                                    )
                                }
                                participacion.checkOut?.let { marcaje ->
                                    MarcajeFila(
                                        titulo = stringResource(R.string.comun_checkout),
                                        marcaje = marcaje,
                                        color = LinceTheme.colors.goldAccent,
                                        icono = Icons.Rounded.CheckCircle
                                    )
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
private fun RecursoDetalle(
    titulo: String,
    valor: String,
    guia: Boolean,
    modifier: Modifier = Modifier
) {
    val iniciales = valor.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(LinceSpacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LinceUserAvatar(
            initials = iniciales.ifBlank { if (guia) "G" else "C" },
            size = 36.dp
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(LinceSpacing.xSmall)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelSmall,
                color = LinceTheme.colors.textSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.bodySmall,
                color = LinceTheme.colors.textPrimary,
                fontWeight = FontWeight.Medium,
                softWrap = true,
                maxLines = 3,
                overflow = TextOverflow.Clip
            )
        }
    }
}

@Composable
fun TarjetaFinal(
    icono: ImageVector,
    titulo: String,
    contenido: @Composable ColumnScope.() -> Unit
) {
    LinceSurfaceCard(
        variant = LinceSurfaceCardVariant.Default,
        contentPadding = LinceSpacing.medium
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(LinceSpacing.small)) {
            LinceSectionHeader(
                title = titulo,
                action = {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = LinceTheme.colors.greenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
            LinceDivider()
            contenido()
        }
    }
}

@Composable
fun MarcajeFila(
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
fun BarraAcciones(
    rolUsuario: RolUsuario,
    estadoActual: EstadoServicio,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit,
    onReasignar: () -> Unit,
    onIrAMarcaje: () -> Unit
) {
    Surface(
        color = LinceTheme.colors.cardSurface
    ) {
        Column {
            LinceDivider()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = LinceSpacing.medium,
                        end = LinceSpacing.medium,
                        top = LinceSpacing.small,
                        bottom = LinceSpacing.small
                    ),
                verticalArrangement = Arrangement.spacedBy(LinceSpacing.small)
            ) {
                if (rolUsuario == RolUsuario.ADMINISTRADOR && estadoActual == EstadoServicio.RECHAZADO) {
                    LinceButton(
                        text = stringResource(R.string.detalle_reasignar),
                        onClick = onReasignar,
                        variant = LinceButtonVariant.Primary,
                        fullWidth = true
                    )
                } else if (
                    (rolUsuario == RolUsuario.CONDUCTOR || rolUsuario == RolUsuario.GUIA) &&
                    (estadoActual == EstadoServicio.CONFIRMADO || estadoActual == EstadoServicio.EN_CURSO)
                ) {
                    LinceButton(
                        text = stringResource(R.string.detalle_ir_marcaje),
                        onClick = onIrAMarcaje,
                        icon = Icons.Rounded.LocationOn,
                        variant = LinceButtonVariant.Primary,
                        fullWidth = true
                    )
                } else if (estadoActual == EstadoServicio.PENDIENTE && rolUsuario != RolUsuario.ADMINISTRADOR) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(LinceSpacing.small)
                    ) {
                        LinceButton(
                            text = stringResource(R.string.accion_rechazar),
                            onClick = onRechazar,
                            modifier = Modifier.weight(1f),
                            variant = LinceButtonVariant.Destructive
                        )
                        LinceButton(
                            text = stringResource(R.string.accion_aceptar),
                            onClick = onAceptar,
                            modifier = Modifier.weight(1f),
                            variant = LinceButtonVariant.Primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SeccionHeader(
    icono: ImageVector,
    titulo: String,
    colorIcono: Color = LinceGreenPrimary
) {
    LinceSectionHeader(
        title = titulo,
        action = {
            Icon(
                imageVector = icono,
                contentDescription = titulo,
                tint = colorIcono,
                modifier = Modifier.size(19.dp)
            )
        }
    )
}

@Composable
fun FichaDato(
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") colorFondo: Color = LinceSurfaceContainerLow,
    icono: ImageVector? = null
) {
    Column(
        modifier = modifier.padding(vertical = LinceSpacing.xSmall),
        verticalArrangement = Arrangement.spacedBy(LinceSpacing.xSmall)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icono?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = LinceTheme.colors.greenPrimary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(LinceSpacing.xSmall))
            }
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelSmall,
                color = LinceTheme.colors.textSecondary,
                fontWeight = FontWeight.SemiBold,
                softWrap = true,
                maxLines = 2,
                overflow = TextOverflow.Clip
            )
        }
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            color = LinceTheme.colors.textPrimary,
            fontWeight = FontWeight.Medium,
            softWrap = true,
            maxLines = 4,
            overflow = TextOverflow.Clip
        )
    }
}

@Composable
fun ParadasRuta(
    origenDestino: String,
    hora: String
) {
    val paradas = origenDestino
        .split("->")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    if (paradas.isEmpty()) {
        Text(
            text = origenDestino,
            style = MaterialTheme.typography.bodyMedium,
            color = LinceTheme.colors.textPrimary,
            fontWeight = FontWeight.Medium
        )
    } else {
        Column {
            paradas.forEachIndexed { index, parada ->
                val esUltima = index == paradas.lastIndex
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier
                                .padding(top = LinceSpacing.xSmall)
                                .size(9.dp),
                            shape = CircleShape,
                            color = when {
                                index == 0 -> LinceTheme.colors.greenPrimary
                                esUltima -> LinceTheme.colors.goldAccent
                                else -> LinceTheme.colors.greenPrimary.copy(alpha = 0.45f)
                            }
                        ) {}
                        if (!esUltima) {
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(26.dp)
                                    .background(
                                        LinceTheme.colors.outlineVariant,
                                        RoundedCornerShape(1.dp)
                                    )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(LinceSpacing.small))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                index == 0 -> stringResource(R.string.detalle_pickup, hora)
                                esUltima -> stringResource(R.string.detalle_destino_final)
                                else -> stringResource(R.string.detalle_parada)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = LinceTheme.colors.textSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = parada,
                            style = MaterialTheme.typography.bodyMedium,
                            color = LinceTheme.colors.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!esUltima) Spacer(modifier = Modifier.height(LinceSpacing.small))
                    }
                }
            }
        }
    }
}

fun etiquetaParticipante(context: Context, servicio: Servicio, usuarioId: String): String {
    val rol = when (usuarioId) {
        servicio.conductorId -> context.getString(R.string.rol_conductor)
        servicio.guiaId -> context.getString(R.string.rol_guia)
        else -> context.getString(R.string.rol_participante)
    }
    return "$rol \u00B7 $usuarioId"
}

fun formatearFechaHora(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(millis))

fun formatearCoordenada(valor: Double): String =
    String.format(Locale.US, "%.5f", valor)
