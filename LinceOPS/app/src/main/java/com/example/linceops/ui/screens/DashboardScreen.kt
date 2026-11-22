package com.example.linceops.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linceops.R
import com.example.linceops.model.EstadoServicio
import com.example.linceops.model.RolUsuario
import com.example.linceops.model.Servicio
import com.example.linceops.ui.components.DashboardSkeleton
import com.example.linceops.ui.components.EfectoScrollBarraInferior
import com.example.linceops.ui.components.LinceButton
import com.example.linceops.ui.components.LinceButtonVariant
import com.example.linceops.ui.components.LinceDivider
import com.example.linceops.ui.components.LinceEmptyState
import com.example.linceops.ui.components.LinceKpiCard
import com.example.linceops.ui.components.LinceSectionHeader
import com.example.linceops.ui.components.LinceStatusBadge
import com.example.linceops.ui.components.LinceStatusTone
import com.example.linceops.ui.components.LinceSurfaceCard
import com.example.linceops.ui.components.LinceSurfaceCardVariant
import com.example.linceops.ui.components.LinceUserAvatar
import com.example.linceops.ui.theme.LinceSpacing
import com.example.linceops.ui.theme.LinceTheme
import com.example.linceops.ui.theme.*
import com.example.linceops.viewmodel.ServiceUiState
import java.util.Locale

@Composable
fun DashboardScreen(
    rolUsuario: RolUsuario,
    uiState: ServiceUiState,
    onIniciarMarcaje: (String) -> Unit,
    onCrearServicio: () -> Unit = {},
    onVerDetalleServicio: (String) -> Unit = {},
    onScrollBarra: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (uiState.isLoading && uiState.serviciosTotales.isEmpty()) {
        DashboardSkeleton(modifier = modifier)
        return
    }

    val listState = rememberLazyListState()
    EfectoScrollBarraInferior(listState = listState, onVisibleChange = onScrollBarra)
    val context = LocalContext.current
    val usuario = uiState.usuarioLogueado
    val serviciosVisibles = uiState.serviciosTotales
    val nombreUsuario = usuario?.nombre ?: stringResource(R.string.dashboard_usuario)
    val iniciales = nombreUsuario
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")

    var filtroInicio by remember { mutableStateOf<EstadoServicio?>(null) }
    val serviciosFiltrados = if (filtroInicio == null) {
        serviciosVisibles
    } else {
        serviciosVisibles.filter { servicio ->
            (usuario?.let { servicio.estadoPara(it) } ?: servicio.estado) == filtroInicio
        }
    }
    val datosDashboard = uiState.copy(servicios = uiState.serviciosTotales)

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(LinceTheme.colors.background),
        contentPadding = PaddingValues(
            start = LinceSpacing.medium,
            end = LinceSpacing.medium,
            top = LinceSpacing.medium,
            bottom = 124.dp
        ),
        verticalArrangement = Arrangement.spacedBy(LinceSpacing.medium)
    ) {
        item {
            val topographicColor = LinceTheme.colors.greenPrimary.copy(alpha = 0.055f)
            Box(modifier = Modifier.fillMaxWidth()) {
                Canvas(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(width = 138.dp, height = 112.dp)
                ) {
                    for (index in 0..4) {
                        drawArc(
                            color = topographicColor,
                            startAngle = 185f + index * 5f,
                            sweepAngle = 145f,
                            useCenter = false,
                            topLeft = Offset(size.width * 0.16f + index * 5f, size.height * 0.04f + index * 6f),
                            size = androidx.compose.ui.geometry.Size(
                                size.width * 0.88f - index * 7f,
                                size.height * 0.84f - index * 7f
                            ),
                            style = Stroke(width = 1.1.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(LinceSpacing.small)
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "LINCE OPS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LinceTheme.colors.greenPrimary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.1.sp
                                )
                                Text(
                                    text = "  ·  ${nombreRol(context, rolUsuario).uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LinceTheme.colors.textSecondary,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = LinceTheme.colors.goldAccent,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(LinceSpacing.xSmall))
                                Text(
                                    text = stringResource(R.string.dashboard_base_operativa),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = LinceTheme.colors.textSecondary
                                )
                            }
                        }
                        LinceUserAvatar(
                            initials = iniciales.ifBlank { "LO" },
                            size = 44.dp
                        )
                    }

                    Text(
                        text = stringResource(R.string.dashboard_hola, nombreUsuario),
                        style = MaterialTheme.typography.headlineSmall,
                        color = LinceTheme.colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = LinceTheme.colors.success,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(LinceSpacing.xSmall))
                        Text(
                            text = stringResource(R.string.dashboard_turno),
                            style = MaterialTheme.typography.bodySmall,
                            color = LinceTheme.colors.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        item {
            when (rolUsuario) {
                RolUsuario.ADMINISTRADOR -> DashboardManagerView(
                    uiState = datosDashboard,
                    onCrearServicio = onCrearServicio
                )
                RolUsuario.CONDUCTOR -> DashboardDriverView(
                    uiState = datosDashboard,
                    onIniciarMarcaje = onIniciarMarcaje,
                    onVerDetalleServicio = onVerDetalleServicio
                )
                RolUsuario.GUIA -> DashboardGuiaView(
                    uiState = datosDashboard,
                    onIniciarMarcaje = onIniciarMarcaje,
                    onVerDetalleServicio = onVerDetalleServicio
                )
            }
        }

        item {
            FiltrosInicio(
                seleccionado = filtroInicio,
                onSeleccionar = { filtroInicio = it }
            )
        }

        if (serviciosFiltrados.isNotEmpty()) {
            item {
                ResumenServicios(
                    servicios = serviciosFiltrados,
                    uiState = datosDashboard,
                    onVerDetalleServicio = onVerDetalleServicio
                )
            }
        } else {
            item {
                LinceEmptyState(
                    title = filtroInicio?.let {
                        stringResource(R.string.dashboard_sin_servicios_estado, nombreEstado(context, it))
                    } ?: stringResource(R.string.dashboard_sin_servicios),
                    description = stringResource(R.string.dashboard_sin_servicios),
                    icon = Icons.AutoMirrored.Filled.List
                )
            }
        }
    }
}

@Composable
private fun FiltrosInicio(
    seleccionado: EstadoServicio?,
    onSeleccionar: (EstadoServicio?) -> Unit
) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(LinceSpacing.small)) {
        Text(
            text = stringResource(R.string.dashboard_filtrar_estado),
            style = MaterialTheme.typography.labelMedium,
            color = LinceTheme.colors.textSecondary,
            fontWeight = FontWeight.SemiBold
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LinceSpacing.small)
        ) {
            item {
                FilterChip(
                    selected = seleccionado == null,
                    onClick = { onSeleccionar(null) },
                    label = { Text(stringResource(R.string.dashboard_todos)) },
                    shape = RoundedCornerShape(50),
                    colors = filtrosInicioColors()
                )
            }
            items(estadosOrdenados) { estado ->
                FilterChip(
                    selected = seleccionado == estado,
                    onClick = { onSeleccionar(estado) },
                    label = { Text(nombreEstado(context, estado)) },
                    shape = RoundedCornerShape(50),
                    colors = filtrosInicioColors()
                )
            }
        }
    }
}

@Composable
private fun filtrosInicioColors() = FilterChipDefaults.filterChipColors(
    containerColor = LinceTheme.colors.surfaceContainerHigh,
    labelColor = LinceTheme.colors.textSecondary,
    selectedContainerColor = LinceTheme.colors.greenPrimary,
    selectedLabelColor = LinceTheme.colors.onPrimary
)

@Composable
private fun DashboardManagerView(
    uiState: ServiceUiState,
    onCrearServicio: () -> Unit
) {
    val usuario = uiState.usuarioLogueado
    val pendientes = uiState.servicios.count { servicio ->
        (usuario?.let { servicio.estadoPara(it) } ?: servicio.estado) == EstadoServicio.PENDIENTE
    }

    Column(verticalArrangement = Arrangement.spacedBy(LinceSpacing.medium)) {
        LinceSectionHeader(
            title = stringResource(R.string.dashboard_panel_admin),
            action = {
                LinceStatusBadge(
                    text = stringResource(R.string.dashboard_hoy),
                    tone = LinceStatusTone.Info
                )
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(LinceSpacing.mediumSmall)
        ) {
            LinceKpiCard(
                value = "${uiState.servicios.size}",
                label = stringResource(R.string.dashboard_kpi_visibles),
                supportingText = stringResource(R.string.dashboard_kpi_seguimiento),
                icon = Icons.AutoMirrored.Filled.List,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            LinceKpiCard(
                value = "$pendientes",
                label = stringResource(R.string.dashboard_kpi_pendientes),
                supportingText = stringResource(R.string.dashboard_kpi_gestion),
                icon = Icons.Default.Warning,
                accentColor = LinceTheme.colors.warning,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }

        uiState.resumenOperativo?.let { resumen ->
            val codigosBase = listOf("SCL", "PUX", "PUQ")
            val oficinasMostrar = if (resumen.porOficina.size == 3) {
                resumen.porOficina.map { (it.clave ?: "-") to it.servicios }
            } else {
                codigosBase.map { codigo ->
                    val oficina = resumen.porOficina.firstOrNull {
                        it.clave.equals(codigo, ignoreCase = true)
                    }
                    codigo to (oficina?.servicios ?: 0)
                }
            }
            LinceSurfaceCard(variant = LinceSurfaceCardVariant.Default) {
                Column(verticalArrangement = Arrangement.spacedBy(LinceSpacing.medium)) {
                    LinceSectionHeader(
                        title = stringResource(R.string.dashboard_resumen_southbound_titulo),
                        action = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = LinceTheme.colors.goldAccent,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(LinceSpacing.small)
                    ) {
                        LinceStatusBadge(
                            text = stringResource(
                                R.string.dashboard_resumen_servicios_pill,
                                resumen.totalServicios
                            ),
                            tone = LinceStatusTone.Success
                        )
                        LinceStatusBadge(
                            text = stringResource(
                                R.string.dashboard_resumen_pax_pill,
                                resumen.totalPax
                            ),
                            tone = LinceStatusTone.Neutral
                        )
                    }
                    if (resumen.porOficina.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            val pesosOficinas = oficinasMostrar.map {
                                it.second.toFloat().coerceAtLeast(0.18f)
                            }
                            val coloresOficina = listOf(
                                LinceTheme.colors.greenPrimary,
                                LinceTheme.colors.greenDark,
                                LinceTheme.colors.goldAccent
                            )
                            oficinasMostrar.forEachIndexed { index, (codigo, cantidad) ->
                                Column(
                                    modifier = Modifier.weight(pesosOficinas[index]),
                                    verticalArrangement = Arrangement.spacedBy(LinceSpacing.xSmall)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(9.dp)
                                            .background(
                                                coloresOficina[index % coloresOficina.size],
                                                RectangleShape
                                            )
                                    )
                                    Text(
                                        text = codigo,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LinceTheme.colors.textSecondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "$cantidad",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = LinceTheme.colors.textPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (uiState.guiasSouthbound.isNotEmpty() || uiState.transportistasSouthbound.isNotEmpty()) {
            LinceSurfaceCard(variant = LinceSurfaceCardVariant.Default) {
                Column(verticalArrangement = Arrangement.spacedBy(LinceSpacing.small)) {
                    Text(
                        text = stringResource(R.string.dashboard_recursos_activos),
                        style = MaterialTheme.typography.titleMedium,
                        color = LinceTheme.colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        softWrap = true,
                        maxLines = 2,
                        overflow = TextOverflow.Clip
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(LinceSpacing.xSmall)
                    ) {
                        LinceStatusBadge(
                            text = stringResource(
                                R.string.dashboard_guias_activos_badge,
                                uiState.guiasSouthbound.size
                            ),
                            tone = LinceStatusTone.Success,
                            modifier = Modifier.weight(1f)
                        )
                        LinceStatusBadge(
                            text = stringResource(
                                R.string.dashboard_transportistas_badge,
                                uiState.transportistasSouthbound.size
                            ),
                            tone = LinceStatusTone.Neutral,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    val guias = uiState.guiasSouthbound.take(2)
                    val conductores = uiState.transportistasSouthbound.take(2)
                    guias.forEachIndexed { index, guia ->
                        if (index > 0) LinceDivider()
                        RecursoFila(
                            nombre = guia.nombre ?: "-",
                            oficina = guia.oficina?.codigo ?: "-",
                            detalle = guia.idiomas.mapNotNull { it.codigo }.joinToString(", "),
                            guia = true
                        )
                    }
                    conductores.forEachIndexed { index, conductor ->
                        if (guias.isNotEmpty() || index > 0) LinceDivider()
                        RecursoFila(
                            nombre = conductor.nombre ?: "-",
                            oficina = conductor.oficina?.codigo ?: "-",
                            detalle = conductor.vehiculos.firstOrNull()?.patente
                                ?: stringResource(R.string.dashboard_sin_vehiculo),
                            guia = false
                        )
                    }
                }
            }
        }

        if (uiState.hitosCoordinacion.isNotEmpty() || uiState.incidenciasCoordinacion.isNotEmpty()) {
            LinceSurfaceCard(variant = LinceSurfaceCardVariant.Default) {
                Column(verticalArrangement = Arrangement.spacedBy(LinceSpacing.small)) {
                    Text(
                        text = stringResource(R.string.dashboard_trazabilidad),
                        style = MaterialTheme.typography.titleMedium,
                        color = LinceTheme.colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        softWrap = true,
                        maxLines = 2,
                        overflow = TextOverflow.Clip
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(LinceSpacing.xSmall)
                    ) {
                        LinceStatusBadge(
                            text = stringResource(
                                R.string.dashboard_hitos_badge,
                                uiState.hitosCoordinacion.size
                            ),
                            tone = LinceStatusTone.Success,
                            modifier = Modifier.weight(1f)
                        )
                        LinceStatusBadge(
                            text = stringResource(
                                R.string.dashboard_incidencias_badge,
                                uiState.incidenciasCoordinacion.size
                            ),
                            tone = LinceStatusTone.Error,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    val hitos = uiState.hitosCoordinacion.takeLast(2).reversed()
                    val incidencias = uiState.incidenciasCoordinacion.takeLast(2).reversed()
                    hitos.forEachIndexed { index, hito ->
                        if (index > 0) LinceDivider()
                        val coordenadas = String.format(
                            Locale.US,
                            "%.4f, %.4f",
                            hito.latitudMarcaje ?: 0.0,
                            hito.longitudMarcaje ?: 0.0
                        )
                        ActividadFila(
                            titulo = hito.tipo ?: stringResource(R.string.dashboard_hito),
                            servicioId = hito.servicioId ?: "-",
                            detalle = stringResource(
                                R.string.dashboard_hito_detalle,
                                hito.usuario?.nombre ?: "-",
                                coordenadas
                            ),
                            incidencia = false
                        )
                    }
                    incidencias.forEachIndexed { index, incidencia ->
                        if (hitos.isNotEmpty() || index > 0) LinceDivider()
                        ActividadFila(
                            titulo = incidencia.motivo ?: stringResource(R.string.dashboard_incidencia),
                            servicioId = incidencia.servicioId ?: "-",
                            detalle = (incidencia.detalle ?: "").ifBlank { "-" },
                            incidencia = true
                        )
                    }
                }
            }
        }

        LinceButton(
            text = stringResource(R.string.dashboard_asignacion_local),
            onClick = onCrearServicio,
            icon = Icons.Default.Add,
            variant = LinceButtonVariant.Primary,
            fullWidth = true
        )
    }
}

@Composable
private fun RecursoFila(
    nombre: String,
    oficina: String,
    detalle: String,
    guia: Boolean
) {
    val iniciales = nombre.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = LinceSpacing.xSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LinceSpacing.small)
    ) {
        LinceUserAvatar(
            initials = iniciales.ifBlank { if (guia) "G" else "C" },
            size = 34.dp
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = nombre,
                style = MaterialTheme.typography.bodyMedium,
                color = LinceTheme.colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = detalle.ifBlank { oficina },
                style = MaterialTheme.typography.labelSmall,
                color = LinceTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(LinceSpacing.xSmall)
        ) {
            LinceStatusBadge(
                text = oficina,
                tone = LinceStatusTone.Info
            )
            if (detalle.isNotBlank()) {
                LinceStatusBadge(
                    text = detalle,
                    tone = if (guia) LinceStatusTone.Success else LinceStatusTone.Neutral
                )
            }
        }
    }
}

@Composable
private fun ActividadFila(
    titulo: String,
    servicioId: String,
    detalle: String,
    incidencia: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = LinceSpacing.xSmall),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(LinceSpacing.small)
    ) {
        Icon(
            imageVector = if (incidencia) Icons.Default.Warning else Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (incidencia) LinceTheme.colors.error else LinceTheme.colors.success,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (incidencia) LinceTheme.colors.error else LinceTheme.colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(LinceSpacing.small))
                LinceStatusBadge(
                    text = stringResource(R.string.dashboard_srv_tag, servicioId),
                    tone = if (incidencia) LinceStatusTone.Error else LinceStatusTone.Success
                )
            }
            Text(
                text = detalle,
                style = MaterialTheme.typography.bodySmall,
                color = LinceTheme.colors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DashboardDriverView(
    uiState: ServiceUiState,
    onIniciarMarcaje: (String) -> Unit,
    onVerDetalleServicio: (String) -> Unit
) {
    val usuario = uiState.usuarioLogueado
    val servicio = uiState.servicios.firstOrNull { item ->
        val estado = usuario?.let { item.estadoPara(it) } ?: item.estado
        estado == EstadoServicio.CONFIRMADO || estado == EstadoServicio.EN_CURSO
    }

    if (servicio == null) {
        LinceSurfaceCard {
            LinceSectionHeader(
                title = stringResource(R.string.dashboard_mis_servicios),
                action = {
                    LinceStatusBadge(
                    text = stringResource(R.string.dashboard_visibles, uiState.servicios.size),
                    tone = LinceStatusTone.Neutral
                    )
                }
            )
            LinceEmptyState(
                title = stringResource(R.string.dashboard_sin_confirmados),
                description = stringResource(R.string.dashboard_consulta_feed),
                icon = Icons.Default.DateRange
            )
        }
        return
    }

    val estado = usuario?.let { servicio.estadoPara(it) } ?: servicio.estado
    LinceSurfaceCard(
        modifier = Modifier.clickable { onVerDetalleServicio(servicio.id) },
        variant = LinceSurfaceCardVariant.Highlighted
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(LinceSpacing.medium)) {
            LinceSectionHeader(
                title = stringResource(R.string.dashboard_proximo_servicio),
                action = {
                    LinceStatusBadge(
                        text = nombreEstado(LocalContext.current, estado),
                        tone = tonoEstado(estado)
                    )
                }
            )
            Text(
                text = servicio.titulo,
                style = MaterialTheme.typography.titleLarge,
                color = LinceTheme.colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            LinceDivider()
            MetadatoServicio(
                icono = Icons.Default.DateRange,
                texto = stringResource(
                    R.string.dashboard_fecha_hora_oficina,
                    servicio.fecha,
                    servicio.hora,
                    servicio.oficina
                )
            )
            MetadatoServicio(
                icono = Icons.Default.LocationOn,
                texto = if (servicio.dropoff.isNotBlank()) {
                    stringResource(
                        R.string.dashboard_pickup_dropoff,
                        servicio.pickup,
                        servicio.dropoff
                    )
                } else {
                    stringResource(R.string.servicios_pickup, servicio.pickup)
                }
            )
            MetadatoServicio(
                icono = Icons.Default.Person,
                texto = if (servicio.capacidadVehiculo > 0) {
                    stringResource(
                        R.string.dashboard_pax_unidad,
                        servicio.pasajeros,
                        servicio.capacidadVehiculo,
                        servicio.vehiculo
                    )
                } else {
                    stringResource(
                        R.string.dashboard_pax_unidad_sin_cap,
                        servicio.pasajeros,
                        servicio.vehiculo
                    )
                }
            )
            LinceButton(
                text = stringResource(R.string.dashboard_iniciar_marcaje),
                onClick = { onIniciarMarcaje(servicio.id) },
                variant = LinceButtonVariant.Primary,
                fullWidth = true
            )
        }
    }
}

@Composable
private fun DashboardGuiaView(
    uiState: ServiceUiState,
    onIniciarMarcaje: (String) -> Unit,
    onVerDetalleServicio: (String) -> Unit
) {
    val usuario = uiState.usuarioLogueado
    val servicioMarcaje = uiState.servicios.firstOrNull { item ->
        val estado = usuario?.let { item.estadoPara(it) } ?: item.estado
        estado == EstadoServicio.CONFIRMADO || estado == EstadoServicio.EN_CURSO
    }
    val servicio = servicioMarcaje ?: uiState.servicios.firstOrNull()

    if (servicio == null) {
        LinceSurfaceCard {
            LinceSectionHeader(
                title = stringResource(R.string.dashboard_panel_guia),
                action = {
                    LinceStatusBadge(
                        text = stringResource(R.string.dashboard_asignados, uiState.servicios.size),
                        tone = LinceStatusTone.Neutral
                    )
                }
            )
            LinceEmptyState(
                title = stringResource(R.string.dashboard_sin_asignados),
                description = stringResource(R.string.dashboard_consulta_feed),
                icon = Icons.Default.Face
            )
        }
        return
    }

    val estado = usuario?.let { servicio.estadoPara(it) } ?: servicio.estado
    LinceSurfaceCard(
        modifier = Modifier.clickable { onVerDetalleServicio(servicio.id) },
        variant = LinceSurfaceCardVariant.Highlighted
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(LinceSpacing.medium)) {
            LinceSectionHeader(
                title = if (estado == EstadoServicio.EN_CURSO) {
                    stringResource(R.string.dashboard_servicio_en_curso)
                } else {
                    stringResource(R.string.dashboard_proximo_asignado)
                },
                action = {
                    LinceStatusBadge(
                        text = nombreEstado(LocalContext.current, estado),
                        tone = tonoEstado(estado)
                    )
                }
            )
            Text(
                text = servicio.titulo,
                style = MaterialTheme.typography.titleLarge,
                color = LinceTheme.colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            LinceDivider()
            MetadatoServicio(
                icono = Icons.Default.LocationOn,
                texto = servicio.origenDestino
            )
            MetadatoServicio(
                icono = Icons.Default.DateRange,
                texto = stringResource(R.string.dashboard_fecha_hora, servicio.fecha, servicio.hora)
            )
            MetadatoServicio(
                icono = Icons.Default.Person,
                texto = stringResource(R.string.dashboard_pasajeros, servicio.pasajeros)
            )
            if (servicioMarcaje != null) {
                LinceButton(
                    text = stringResource(R.string.dashboard_iniciar_marcaje),
                    onClick = { onIniciarMarcaje(servicioMarcaje.id) },
                    variant = LinceButtonVariant.Primary,
                    fullWidth = true
                )
            }
        }
    }
}

@Composable
private fun MetadatoServicio(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    texto: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LinceSpacing.small)
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = LinceTheme.colors.greenPrimary,
            modifier = Modifier.size(17.dp)
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.bodySmall,
            color = LinceTheme.colors.textSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ResumenServicios(
    servicios: List<Servicio>,
    uiState: ServiceUiState,
    onVerDetalleServicio: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(LinceSpacing.small)) {
        LinceSectionHeader(
            title = stringResource(R.string.dashboard_resumen),
            subtitle = stringResource(R.string.dashboard_ultimos, minOf(3, servicios.size))
        )
        servicios.take(3).forEach { servicio ->
            val estado = uiState.usuarioLogueado?.let {
                servicio.estadoPara(it)
            } ?: servicio.estado
            LinceSurfaceCard(
                modifier = Modifier.clickable { onVerDetalleServicio(servicio.id) },
                variant = LinceSurfaceCardVariant.Default,
                contentPadding = LinceSpacing.mediumSmall
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LinceSpacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(LinceSpacing.xSmall)
                    ) {
                        Text(
                            text = servicio.titulo,
                            style = MaterialTheme.typography.titleSmall,
                            color = LinceTheme.colors.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${servicio.hora} · ${servicio.origenDestino}",
                            style = MaterialTheme.typography.bodySmall,
                            color = LinceTheme.colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    LinceStatusBadge(
                        text = nombreEstado(LocalContext.current, estado),
                        tone = tonoEstado(estado)
                    )
                }
            }
        }
    }
}

@Composable
private fun tonoEstado(estado: EstadoServicio): LinceStatusTone = when (estado) {
    EstadoServicio.PENDIENTE -> LinceStatusTone.Warning
    EstadoServicio.CONFIRMADO -> LinceStatusTone.Success
    EstadoServicio.EN_CURSO -> LinceStatusTone.Info
    EstadoServicio.FINALIZADO -> LinceStatusTone.Neutral
    EstadoServicio.RECHAZADO -> LinceStatusTone.Error
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun InicioScreenPreview() {
    LinceOPSTheme {
        DashboardScreen(
            rolUsuario = RolUsuario.CONDUCTOR,
            uiState = ServiceUiState(),
            onIniciarMarcaje = {},
            onCrearServicio = {}
        )
    }
}
