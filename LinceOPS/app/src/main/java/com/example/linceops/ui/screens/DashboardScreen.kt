package com.example.linceops.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import com.example.linceops.ui.theme.*
import com.example.linceops.utils.LanguagePreferences
import com.example.linceops.viewmodel.ServiceUiState
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

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

    var filtroInicio by remember { mutableStateOf<EstadoServicio?>(null) }
    val usuario = uiState.usuarioLogueado
    val serviciosVisibles = if (filtroInicio == null) {
        uiState.serviciosTotales
    } else {
        uiState.serviciosTotales.filter { servicio ->
            val estado = usuario?.let { servicio.estadoPara(it) } ?: servicio.estado
            estado == filtroInicio
        }
    }

    val uiStateCompleto = uiState.copy(servicios = uiState.serviciosTotales)

    val nombreUsuario = uiState.usuarioLogueado?.nombre ?: stringResource(R.string.dashboard_usuario)
    val iniciales = nombreUsuario
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = modifier
                .fillMaxSize()
                .background(LinceCreamBackground),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 124.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "LINCE OPS",
                                style = MaterialTheme.typography.labelSmall,
                                color = LinceGreenPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = "  •  ",
                                style = MaterialTheme.typography.labelSmall,
                                color = LinceOutlineVariant
                            )
                            Text(
                                text = nombreRol(context, rolUsuario).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = LinceTextSecondary,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.8.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = LinceTertiary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.dashboard_base_operativa),
                                style = MaterialTheme.typography.labelMedium,
                                color = LinceTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        color = LinceGreenSageLight,
                        border = BorderStroke(1.dp, LinceGreenBorder)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = iniciales.ifBlank { "LO" },
                                style = MaterialTheme.typography.titleSmall,
                                color = LinceGreenDark,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            item {
                Column {
                    Text(
                        text = stringResource(R.string.dashboard_hola, nombreUsuario),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = LinceTextPrimary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = LinceGreenPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = stringResource(R.string.dashboard_turno),
                            style = MaterialTheme.typography.bodySmall,
                            color = LinceTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            item {
                when (rolUsuario) {
                    RolUsuario.ADMINISTRADOR ->
                        DashboardManagerView(
                            uiState = uiStateCompleto,
                            onCrearServicio = onCrearServicio
                        )

                    RolUsuario.CONDUCTOR ->
                        DashboardDriverView(
                            uiState = uiStateCompleto,
                            onIniciarMarcaje = onIniciarMarcaje,
                            onVerDetalleServicio = onVerDetalleServicio
                        )

                    RolUsuario.GUIA ->
                        DashboardGuiaView(
                            uiState = uiStateCompleto,
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

            if (serviciosVisibles.isNotEmpty()) {
                item {
                    ResumenServicios(
                        servicios = serviciosVisibles,
                        uiState = uiStateCompleto,
                        onVerDetalleServicio = onVerDetalleServicio
                    )
                }
            } else {
                item {
                    Surface(
                        color = LinceSurfaceContainerLow,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, LinceOutlineVariant.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = filtroInicio?.let {
                                stringResource(R.string.dashboard_sin_servicios_estado, nombreEstado(context, it))
                            } ?: stringResource(R.string.dashboard_sin_servicios),
                            style = MaterialTheme.typography.bodySmall,
                            color = LinceTextSecondary,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
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
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.dashboard_filtrar_estado),
            style = MaterialTheme.typography.labelSmall,
            color = LinceTextSecondary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = seleccionado == null,
                    onClick = { onSeleccionar(null) },
                    label = { Text(stringResource(R.string.dashboard_todos)) },
                    shape = RoundedCornerShape(50),
                    colors = filterChipColors()
                )
            }
            items(estadosOrdenados) { estado ->
                FilterChip(
                    selected = seleccionado == estado,
                    onClick = { onSeleccionar(estado) },
                    label = { Text(nombreEstado(context, estado)) },
                    shape = RoundedCornerShape(50),
                    colors = filterChipColors()
                )
            }
        }
    }
}

@Composable
private fun DashboardManagerView(
    uiState: ServiceUiState,
    onCrearServicio: () -> Unit
) {
    val usuario = uiState.usuarioLogueado

    val pendientes = uiState.servicios.count { servicio ->
        val estado = usuario?.let {
            servicio.estadoPara(it)
        } ?: servicio.estado

        estado == EstadoServicio.PENDIENTE
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LinceCardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, LinceOutlineVariant.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dashboard_panel_admin),
                    style = MaterialTheme.typography.titleMedium,
                    color = LinceGreenDark,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = LinceGreenSageLight,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, LinceGreenBorder)
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_hoy),
                        style = MaterialTheme.typography.labelSmall,
                        color = LinceGreenDark,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    modifier = Modifier.weight(1f),
                    etiqueta = stringResource(R.string.dashboard_kpi_visibles),
                    valor = "${uiState.servicios.size}",
                    detalle = stringResource(R.string.dashboard_kpi_seguimiento),
                    colorValor = LinceGreenDark,
                    colorFondo = LinceGreenSageLight.copy(alpha = 0.65f),
                    colorIcono = LinceGreenPrimary,
                    icono = Icons.AutoMirrored.Filled.List
                )

                KpiCard(
                    modifier = Modifier.weight(1f),
                    etiqueta = stringResource(R.string.dashboard_kpi_pendientes),
                    valor = "$pendientes",
                    detalle = stringResource(R.string.dashboard_kpi_gestion),
                    colorValor = StatusPendingText,
                    colorFondo = StatusPendingBg.copy(alpha = 0.72f),
                    colorIcono = StatusPendingText,
                    icono = Icons.Default.Warning
                )
            }

            Surface(
                color = LinceSurfaceContainerLow,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, LinceOutlineVariant.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = LinceGreenPrimary,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.dashboard_consulta_feed),
                        style = MaterialTheme.typography.bodySmall,
                        color = LinceTextSecondary
                    )
                }
            }

            uiState.resumenOperativo?.let { resumen ->
                val codigosBase = listOf("SCL", "PUX", "PUQ")
                val oficinasMostrar = if (resumen.porOficina.size == 3) {
                    resumen.porOficina.map { (it.clave ?: "-") to it.servicios }
                } else {
                    codigosBase.map { codigo ->
                        val encontrado = resumen.porOficina.firstOrNull {
                            it.clave.equals(codigo, ignoreCase = true)
                        }
                        codigo to (encontrado?.servicios ?: 0)
                    }
                }

                Surface(
                    color = LinceSurfaceContainerLow,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, LinceGreenBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    modifier = Modifier.size(30.dp),
                                    shape = RoundedCornerShape(9.dp),
                                    color = LinceGreenSageLight
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            tint = LinceGreenDark,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(9.dp))
                                Text(
                                    text = stringResource(R.string.dashboard_resumen_southbound_titulo),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = LinceGreenDark,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AdminBadgePill(
                                texto = stringResource(
                                    R.string.dashboard_resumen_servicios_pill,
                                    resumen.totalServicios
                                ),
                                colorFondo = LinceGreenSageLight,
                                colorTexto = LinceGreenDark,
                                colorBorde = LinceGreenBorder
                            )
                            AdminBadgePill(
                                texto = stringResource(
                                    R.string.dashboard_resumen_pax_pill,
                                    resumen.totalPax
                                ),
                                colorFondo = LinceCardSurface,
                                colorTexto = LinceTextPrimary,
                                colorBorde = LinceOutlineVariant
                            )
                        }

                        if (resumen.porOficina.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                oficinasMostrar.forEach { (codigo, cantidad) ->
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        color = LinceCardSurface,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, LinceGreenBorder.copy(alpha = 0.75f))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Text(
                                                text = codigo,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = LinceGreenPrimary,
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 0.8.sp
                                            )
                                            Text(
                                                text = "$cantidad",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = LinceTextPrimary,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.guiasSouthbound.isNotEmpty() || uiState.transportistasSouthbound.isNotEmpty()) {
                val guiasMostrados = uiState.guiasSouthbound.take(2)
                val transportistasMostrados = uiState.transportistasSouthbound.take(2)

                Surface(
                    color = LinceSurfaceContainerLow,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, LinceGreenBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(30.dp),
                                shape = RoundedCornerShape(9.dp),
                                color = LinceGreenSageLight
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = LinceGreenDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(9.dp))
                            Text(
                                text = stringResource(R.string.dashboard_recursos_activos),
                                style = MaterialTheme.typography.labelMedium,
                                color = LinceGreenDark,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AdminBadgePill(
                                texto = stringResource(
                                    R.string.dashboard_guias_activos_badge,
                                    uiState.guiasSouthbound.size
                                ),
                                colorFondo = LinceGreenSageLight,
                                colorTexto = LinceGreenDark,
                                colorBorde = LinceGreenBorder
                            )
                            AdminBadgePill(
                                texto = stringResource(
                                    R.string.dashboard_transportistas_badge,
                                    uiState.transportistasSouthbound.size
                                ),
                                colorFondo = LinceCardSurface,
                                colorTexto = LinceTextPrimary,
                                colorBorde = LinceOutlineVariant
                            )
                        }

                        Surface(
                            color = LinceCardSurface,
                            shape = RoundedCornerShape(13.dp),
                            border = BorderStroke(1.dp, LinceOutlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                val totalFilas = guiasMostrados.size + transportistasMostrados.size
                                var indiceFila = 0

                                guiasMostrados.forEach { guia ->
                                    val idiomas = guia.idiomas.mapNotNull { it.codigo }.joinToString(", ")
                                    RecursoActivoFila(
                                        esGuia = true,
                                        nombre = guia.nombre ?: "-",
                                        oficina = guia.oficina?.codigo ?: "-",
                                        detalleSecundario = idiomas
                                    )
                                    indiceFila++
                                    if (indiceFila < totalFilas) {
                                        HorizontalDivider(
                                            color = LinceOutlineVariant.copy(alpha = 0.35f),
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )
                                    }
                                }

                                transportistasMostrados.forEach { trans ->
                                    val patente = trans.vehiculos.firstOrNull()?.patente
                                        ?: stringResource(R.string.dashboard_sin_vehiculo)
                                    RecursoActivoFila(
                                        esGuia = false,
                                        nombre = trans.nombre ?: "-",
                                        oficina = trans.oficina?.codigo ?: "-",
                                        detalleSecundario = patente
                                    )
                                    indiceFila++
                                    if (indiceFila < totalFilas) {
                                        HorizontalDivider(
                                            color = LinceOutlineVariant.copy(alpha = 0.35f),
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.hitosCoordinacion.isNotEmpty() || uiState.incidenciasCoordinacion.isNotEmpty()) {
                val hitosMostrados = uiState.hitosCoordinacion.takeLast(2).reversed()
                val incidenciasMostradas = uiState.incidenciasCoordinacion.takeLast(2).reversed()

                Surface(
                    color = LinceSurfaceContainerLow,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, LinceGreenBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(30.dp),
                                shape = RoundedCornerShape(9.dp),
                                color = LinceGreenSageLight
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = LinceGreenDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(9.dp))
                            Text(
                                text = stringResource(R.string.dashboard_trazabilidad),
                                style = MaterialTheme.typography.labelMedium,
                                color = LinceGreenDark,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AdminBadgePill(
                                texto = stringResource(
                                    R.string.dashboard_hitos_badge,
                                    uiState.hitosCoordinacion.size
                                ),
                                colorFondo = StatusConfirmedBg,
                                colorTexto = StatusConfirmedText,
                                colorBorde = StatusConfirmedText.copy(alpha = 0.3f)
                            )
                            AdminBadgePill(
                                texto = stringResource(
                                    R.string.dashboard_incidencias_badge,
                                    uiState.incidenciasCoordinacion.size
                                ),
                                colorFondo = StatusRejectedBg,
                                colorTexto = StatusRejectedText,
                                colorBorde = StatusRejectedText.copy(alpha = 0.3f)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            hitosMostrados.forEach { hito ->
                                val coords = String.format(
                                    java.util.Locale.US,
                                    "%.4f, %.4f",
                                    hito.latitudMarcaje ?: 0.0,
                                    hito.longitudMarcaje ?: 0.0
                                )
                                ActividadFeedFila(
                                    esIncidencia = false,
                                    titulo = hito.tipo ?: stringResource(R.string.dashboard_hito),
                                    servicioId = hito.servicioId ?: "-",
                                    detalle = stringResource(
                                        R.string.dashboard_hito_detalle,
                                        hito.usuario?.nombre ?: "-",
                                        coords
                                    )
                                )
                            }

                            incidenciasMostradas.forEach { inc ->
                                ActividadFeedFila(
                                    esIncidencia = true,
                                    titulo = inc.motivo ?: stringResource(R.string.dashboard_incidencia),
                                    servicioId = inc.servicioId ?: "-",
                                    detalle = (inc.detalle ?: "").ifBlank { "-" }
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = onCrearServicio,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LinceGreenPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = LinceOnPrimary,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.dashboard_asignacion_local),
                    color = LinceOnPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun AdminBadgePill(
    texto: String,
    colorFondo: Color,
    colorTexto: Color,
    colorBorde: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = colorFondo,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, colorBorde)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            color = colorTexto,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun RecursoActivoFila(
    esGuia: Boolean,
    nombre: String,
    oficina: String,
    detalleSecundario: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            modifier = Modifier.size(30.dp),
            shape = CircleShape,
            color = if (esGuia) LinceGreenSageLight else LinceSurfaceContainerHigh,
            border = BorderStroke(
                1.dp,
                if (esGuia) LinceGreenBorder else LinceOutlineVariant
            )
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (esGuia) Icons.Default.Face else Icons.Default.AccountBox,
                    contentDescription = null,
                    tint = if (esGuia) LinceGreenDark else LinceTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Text(
            text = nombre,
            style = MaterialTheme.typography.bodySmall,
            color = LinceTextPrimary,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Surface(
            color = LinceGreenSageLight,
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, LinceGreenBorder)
        ) {
            Text(
                text = oficina,
                style = MaterialTheme.typography.labelSmall,
                color = LinceGreenDark,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
            )
        }

        if (detalleSecundario.isNotBlank()) {
            Surface(
                color = LinceSurfaceContainerLow,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, LinceOutlineVariant.copy(alpha = 0.65f))
            ) {
                Text(
                    text = detalleSecundario,
                    style = MaterialTheme.typography.labelSmall,
                    color = LinceTextSecondary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun ActividadFeedFila(
    esIncidencia: Boolean,
    titulo: String,
    servicioId: String,
    detalle: String
) {
    val colorIndicadorBg = if (esIncidencia) StatusRejectedBg else StatusConfirmedBg
    val colorIndicadorIcono = if (esIncidencia) StatusRejectedText else StatusConfirmedText
    val colorBorde = if (esIncidencia) StatusRejectedText.copy(alpha = 0.25f) else LinceOutlineVariant.copy(alpha = 0.55f)

    Surface(
        color = LinceCardSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, colorBorde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier
                    .padding(top = 1.dp)
                    .size(28.dp),
                shape = CircleShape,
                color = colorIndicadorBg
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (esIncidencia) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = colorIndicadorIcono,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = titulo,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (esIncidencia) StatusRejectedText else LinceTextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = if (esIncidencia) StatusRejectedBg else LinceGreenSageLight,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(
                            1.dp,
                            if (esIncidencia) StatusRejectedText.copy(alpha = 0.3f) else LinceGreenBorder
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.dashboard_srv_tag, servicioId),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (esIncidencia) StatusRejectedText else LinceGreenDark,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = detalle,
                    style = MaterialTheme.typography.bodySmall,
                    color = LinceTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun KpiCard(
    modifier: Modifier = Modifier,
    etiqueta: String,
    valor: String,
    detalle: String,
    colorValor: Color,
    colorFondo: Color,
    colorIcono: Color,
    icono: ImageVector
) {
    Surface(
        modifier = modifier,
        color = colorFondo,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, colorValor.copy(alpha = 0.22f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = etiqueta,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorValor.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = LinceCardSurface.copy(alpha = 0.72f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icono,
                            contentDescription = null,
                            tint = colorIcono,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = valor,
                style = MaterialTheme.typography.headlineMedium,
                color = colorValor,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = detalle,
                style = MaterialTheme.typography.labelSmall,
                color = colorValor.copy(alpha = 0.78f)
            )
        }
    }
}

@Composable
private fun DashboardDriverView(
    uiState: ServiceUiState,
    onIniciarMarcaje: (String) -> Unit,
    onVerDetalleServicio: (String) -> Unit = {}
) {
    val usuario = uiState.usuarioLogueado

    val servicioMarcaje = uiState.servicios.firstOrNull { servicio ->
        val estado = usuario?.let {
            servicio.estadoPara(it)
        } ?: servicio.estado

        estado == EstadoServicio.CONFIRMADO ||
                estado == EstadoServicio.EN_CURSO
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LinceCardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, LinceOutlineVariant.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dashboard_mis_servicios),
                    style = MaterialTheme.typography.titleMedium,
                    color = LinceGreenDark,
                    fontWeight = FontWeight.Bold
                )
                CountPill(texto = stringResource(R.string.dashboard_visibles, uiState.servicios.size))
            }

            if (servicioMarcaje == null) {
                Surface(
                    color = LinceSurfaceContainerLow,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, LinceOutlineVariant.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = LinceTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.dashboard_sin_confirmados),
                            style = MaterialTheme.typography.bodyMedium,
                            color = LinceTextSecondary
                        )
                    }
                }
            } else {
                val estadoActual = usuario?.let {
                    servicioMarcaje.estadoPara(it)
                } ?: servicioMarcaje.estado

                Surface(
                    onClick = { onVerDetalleServicio(servicioMarcaje.id) },
                    color = LinceSurfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, LinceGreenBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (estadoActual == EstadoServicio.EN_CURSO) {
                                    stringResource(R.string.dashboard_servicio_en_curso)
                                } else {
                                    stringResource(R.string.dashboard_proximo_servicio)
                                },
                                color = LinceGreenPrimary,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            EstadoBadge(estado = estadoActual)
                        }

                        Text(
                            text = servicioMarcaje.titulo,
                            style = MaterialTheme.typography.titleMedium,
                            color = LinceTextPrimary,
                            fontWeight = FontWeight.Bold
                        )

                        Surface(
                            color = LinceCardSurface.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = stringResource(R.string.comun_ruta),
                                    tint = LinceGreenPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = servicioMarcaje.origenDestino,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LinceTextSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        DashboardInfoRow(
                            icono = Icons.Default.DateRange,
                            texto = stringResource(
                                R.string.dashboard_fecha_hora_oficina,
                                servicioMarcaje.fecha,
                                servicioMarcaje.hora,
                                servicioMarcaje.oficina
                            )
                        )
                        DashboardInfoRow(
                            icono = Icons.Default.LocationOn,
                            texto = if (servicioMarcaje.dropoff.isNotBlank()) {
                                stringResource(
                                    R.string.dashboard_pickup_dropoff,
                                    servicioMarcaje.pickup,
                                    servicioMarcaje.dropoff
                                )
                            } else {
                                stringResource(R.string.servicios_pickup, servicioMarcaje.pickup)
                            }
                        )
                        DashboardInfoRow(
                            icono = Icons.Default.Person,
                            texto = if (servicioMarcaje.capacidadVehiculo > 0) {
                                stringResource(
                                    R.string.dashboard_pax_unidad,
                                    servicioMarcaje.pasajeros,
                                    servicioMarcaje.capacidadVehiculo,
                                    servicioMarcaje.vehiculo
                                )
                            } else {
                                stringResource(
                                    R.string.dashboard_pax_unidad_sin_cap,
                                    servicioMarcaje.pasajeros,
                                    servicioMarcaje.vehiculo
                                )
                            }
                        )
                    }
                }

                Button(
                    onClick = {
                        onIniciarMarcaje(servicioMarcaje.id)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LinceGreenPrimary)
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_iniciar_marcaje),
                        color = LinceOnPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardGuiaView(
    uiState: ServiceUiState,
    onIniciarMarcaje: (String) -> Unit,
    onVerDetalleServicio: (String) -> Unit = {}
) {
    val usuario = uiState.usuarioLogueado

    val servicioMarcaje = uiState.servicios.firstOrNull { servicio ->
        val estado = usuario?.let { servicio.estadoPara(it) } ?: servicio.estado
        estado == EstadoServicio.CONFIRMADO || estado == EstadoServicio.EN_CURSO
    }
    val servicio = servicioMarcaje ?: uiState.servicios.firstOrNull()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LinceCardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, LinceOutlineVariant.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dashboard_panel_guia),
                    style = MaterialTheme.typography.titleMedium,
                    color = LinceGreenDark,
                    fontWeight = FontWeight.Bold
                )
                CountPill(texto = stringResource(R.string.dashboard_asignados, uiState.servicios.size))
            }

            if (servicio == null) {
                Surface(
                    color = LinceSurfaceContainerLow,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, LinceOutlineVariant.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = LinceTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.dashboard_sin_asignados),
                            style = MaterialTheme.typography.bodyMedium,
                            color = LinceTextSecondary
                        )
                    }
                }
            } else {
                val estadoActual = usuario?.let {
                    servicio.estadoPara(it)
                } ?: servicio.estado

                Surface(
                    onClick = { onVerDetalleServicio(servicio.id) },
                    color = LinceSurfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, LinceGreenBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (estadoActual == EstadoServicio.EN_CURSO) {
                                    stringResource(R.string.dashboard_servicio_en_curso)
                                } else {
                                    stringResource(R.string.dashboard_proximo_asignado)
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = LinceGreenPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            EstadoBadge(estado = estadoActual)
                        }

                        Text(
                            text = servicio.titulo,
                            style = MaterialTheme.typography.titleMedium,
                            color = LinceTextPrimary,
                            fontWeight = FontWeight.Bold
                        )

                        Surface(
                            color = LinceCardSurface.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = stringResource(R.string.comun_ruta),
                                    tint = LinceGreenPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = servicio.origenDestino,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LinceTextSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        DashboardInfoRow(
                            icono = Icons.Default.DateRange,
                            texto = stringResource(R.string.dashboard_fecha_hora, servicio.fecha, servicio.hora)
                        )
                        DashboardInfoRow(
                            icono = Icons.Default.LocationOn,
                            texto = stringResource(R.string.dashboard_punto_encuentro, servicio.pickup)
                        )
                        DashboardInfoRow(
                            icono = Icons.Default.Person,
                            texto = stringResource(R.string.dashboard_pasajeros, servicio.pasajeros)
                        )
                    }
                }

                if (servicioMarcaje != null) {
                    Button(
                        onClick = { onIniciarMarcaje(servicioMarcaje.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LinceGreenPrimary)
                    ) {
                        Text(
                            text = stringResource(R.string.dashboard_iniciar_marcaje),
                            color = LinceOnPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumenServicios(
    servicios: List<Servicio>,
    uiState: ServiceUiState,
    onVerDetalleServicio: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.dashboard_resumen),
                style = MaterialTheme.typography.titleMedium,
                color = LinceTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.dashboard_ultimos, minOf(3, servicios.size)),
                style = MaterialTheme.typography.labelSmall,
                color = LinceTextSecondary
            )
        }

        servicios.take(3).forEach { servicio ->
            Surface(
                onClick = { onVerDetalleServicio(servicio.id) },
                color = LinceCardSurface,
                shape = RoundedCornerShape(14.dp),
                shadowElevation = 1.dp,
                border = BorderStroke(1.dp, LinceOutlineVariant.copy(alpha = 0.55f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = LinceGreenSageLight,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, LinceGreenBorder)
                    ) {
                        Text(
                            text = servicio.hora,
                            style = MaterialTheme.typography.labelMedium,
                            color = LinceGreenDark,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = servicio.titulo,
                                style = MaterialTheme.typography.bodyMedium,
                                color = LinceTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "[${servicio.oficina}]",
                                style = MaterialTheme.typography.labelSmall,
                                color = LinceGreenPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${servicio.origenDestino} · ${servicio.vehiculo}",
                            style = MaterialTheme.typography.labelSmall,
                            color = LinceTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    EstadoBadge(
                        estado = uiState.usuarioLogueado?.let {
                            servicio.estadoPara(it)
                        } ?: servicio.estado
                    )
                }
            }
        }
    }
}

@Composable
private fun CountPill(texto: String) {
    Surface(
        color = LinceGreenSageLight,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, LinceGreenBorder)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            color = LinceGreenDark,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun DashboardInfoRow(
    icono: ImageVector,
    texto: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = LinceGreenPrimary,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.bodySmall,
            color = LinceTextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
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
