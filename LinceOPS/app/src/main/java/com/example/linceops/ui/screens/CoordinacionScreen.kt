package com.example.linceops.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.linceops.network.dto.GuiaDto
import com.example.linceops.network.dto.HitoDto
import com.example.linceops.network.dto.IncidenciaDto
import com.example.linceops.network.dto.TransportistaDto
import com.example.linceops.network.dto.VehiculoDto
import com.example.linceops.ui.components.LinceEmptyState
import com.example.linceops.ui.components.LinceStatusBadge
import com.example.linceops.ui.components.LinceStatusTone
import com.example.linceops.ui.components.LinceSurfaceCard
import com.example.linceops.ui.components.LinceSurfaceCardVariant
import com.example.linceops.ui.components.LinceUserAvatar
import com.example.linceops.ui.theme.LinceSpacing
import com.example.linceops.ui.theme.LinceTheme
import com.example.linceops.viewmodel.ServiceUiState
import java.util.Locale

enum class SeccionCoordinacion(val titulo: String) {
    HITOS("Hitos"),
    INCIDENCIAS("Incidencias"),
    EQUIPO("Equipo"),
    VEHICULOS("Vehículos")
}

private enum class FiltroRolEquipo(val etiqueta: String) {
    TODOS("Todos"),
    GUIAS("Guías"),
    CONDUCTORES("Conductores")
}

private data class VehiculoConConductor(
    val vehiculo: VehiculoDto,
    val conductorNombre: String,
    val oficinaCodigo: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoordinacionScreen(
    uiState: ServiceUiState,
    seccionInicial: SeccionCoordinacion = SeccionCoordinacion.HITOS,
    onVolver: () -> Unit,
    onRefrescar: () -> Unit
) {
    var seccionActual by remember(seccionInicial) { mutableStateOf(seccionInicial) }
    var filtroRol by remember { mutableStateOf(FiltroRolEquipo.TODOS) }
    var filtroOficina by remember { mutableStateOf<String?>(null) }

    val titulosPorServicio = remember(uiState.serviciosTotales) {
        uiState.serviciosTotales.associate { it.id to it.titulo }
    }
    val hitosOrdenados = remember(uiState.hitosCoordinacion) {
        ordenarHitosPorFechaDescendente(uiState.hitosCoordinacion)
    }
    val incidenciasOrdenadas = remember(uiState.incidenciasCoordinacion) {
        ordenarIncidenciasPorFechaDescendente(uiState.incidenciasCoordinacion)
    }
    val vehiculosLista = remember(uiState.transportistasSouthbound) {
        uiState.transportistasSouthbound.flatMap { conductor ->
            val nombre = conductor.nombre ?: "Sin conductor"
            val oficina = conductor.oficina?.codigo ?: "-"
            conductor.vehiculos.map { v -> VehiculoConConductor(v, nombre, oficina) }
        }
    }
    val oficinasDisponibles = remember(uiState.guiasSouthbound, uiState.transportistasSouthbound) {
        (uiState.guiasSouthbound.mapNotNull { it.oficina?.codigo } +
                uiState.transportistasSouthbound.mapNotNull { it.oficina?.codigo })
            .distinct()
            .sorted()
    }

    val guiasFiltrados = remember(uiState.guiasSouthbound, filtroRol, filtroOficina) {
        if (filtroRol == FiltroRolEquipo.CONDUCTORES) emptyList()
        else uiState.guiasSouthbound.filter {
            filtroOficina == null || it.oficina?.codigo.equals(filtroOficina, ignoreCase = true)
        }
    }
    val conductoresFiltrados = remember(uiState.transportistasSouthbound, filtroRol, filtroOficina) {
        if (filtroRol == FiltroRolEquipo.GUIAS) emptyList()
        else uiState.transportistasSouthbound.filter {
            filtroOficina == null || it.oficina?.codigo.equals(filtroOficina, ignoreCase = true)
        }
    }
    val vehiculosFiltrados = remember(vehiculosLista, filtroOficina) {
        vehiculosLista.filter {
            filtroOficina == null || it.oficinaCodigo.equals(filtroOficina, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = LinceTheme.colors.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Centro de Coordinación",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onVolver) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onRefrescar) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Actualizar"
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
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LinceSpacing.medium, vertical = LinceSpacing.small),
                horizontalArrangement = Arrangement.spacedBy(LinceSpacing.small)
            ) {
                items(SeccionCoordinacion.entries) { seccion ->
                    val cantidad = when (seccion) {
                        SeccionCoordinacion.HITOS -> hitosOrdenados.size
                        SeccionCoordinacion.INCIDENCIAS -> incidenciasOrdenadas.size
                        SeccionCoordinacion.EQUIPO -> uiState.guiasSouthbound.size + uiState.transportistasSouthbound.size
                        SeccionCoordinacion.VEHICULOS -> vehiculosLista.size
                    }
                    FilterChip(
                        selected = seccionActual == seccion,
                        onClick = { seccionActual = seccion },
                        label = { Text("${seccion.titulo} ($cantidad)") },
                        shape = RoundedCornerShape(50),
                        colors = coordinacionChipColors()
                    )
                }
            }

            if (seccionActual == SeccionCoordinacion.EQUIPO || seccionActual == SeccionCoordinacion.VEHICULOS) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LinceSpacing.medium)
                        .padding(bottom = LinceSpacing.small),
                    horizontalArrangement = Arrangement.spacedBy(LinceSpacing.small)
                ) {
                    if (seccionActual == SeccionCoordinacion.EQUIPO) {
                        items(FiltroRolEquipo.entries) { rol ->
                            FilterChip(
                                selected = filtroRol == rol,
                                onClick = { filtroRol = rol },
                                label = { Text(rol.etiqueta) },
                                shape = RoundedCornerShape(50),
                                colors = coordinacionChipColors()
                            )
                        }
                    }
                    item {
                        FilterChip(
                            selected = filtroOficina == null,
                            onClick = { filtroOficina = null },
                            label = { Text("Todas las oficinas") },
                            shape = RoundedCornerShape(50),
                            colors = coordinacionChipColors()
                        )
                    }
                    items(oficinasDisponibles) { oficina ->
                        FilterChip(
                            selected = filtroOficina == oficina,
                            onClick = { filtroOficina = oficina },
                            label = { Text(oficina) },
                            shape = RoundedCornerShape(50),
                            colors = coordinacionChipColors()
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = LinceSpacing.medium,
                    end = LinceSpacing.medium,
                    top = LinceSpacing.xSmall,
                    bottom = 32.dp
                ),
                verticalArrangement = Arrangement.spacedBy(LinceSpacing.small)
            ) {
                when (seccionActual) {
                    SeccionCoordinacion.HITOS -> {
                        if (hitosOrdenados.isEmpty()) {
                            item {
                                LinceEmptyState(
                                    title = "Sin hitos registrados",
                                    description = "Aquí aparecerán todos los Check-In y Check-Out.",
                                    icon = Icons.Default.CheckCircle
                                )
                            }
                        } else {
                            items(hitosOrdenados) { hito ->
                                HitoCompletoCard(hito, titulosPorServicio)
                            }
                        }
                    }

                    SeccionCoordinacion.INCIDENCIAS -> {
                        if (incidenciasOrdenadas.isEmpty()) {
                            item {
                                LinceEmptyState(
                                    title = "Sin incidencias",
                                    description = "No se registran alertas de geocerca ni atrasos.",
                                    icon = Icons.Default.Warning
                                )
                            }
                        } else {
                            items(incidenciasOrdenadas) { incidencia ->
                                IncidenciaCompletaCard(incidencia, titulosPorServicio)
                            }
                        }
                    }

                    SeccionCoordinacion.EQUIPO -> {
                        if (guiasFiltrados.isEmpty() && conductoresFiltrados.isEmpty()) {
                            item {
                                LinceEmptyState(
                                    title = "Sin personal en este filtro",
                                    description = "Prueba cambiando el rol o la oficina.",
                                    icon = Icons.Default.Person
                                )
                            }
                        } else {
                            items(guiasFiltrados) { guia ->
                                GuiaCompletoCard(guia)
                            }
                            items(conductoresFiltrados) { conductor ->
                                ConductorCompletoCard(conductor)
                            }
                        }
                    }

                    SeccionCoordinacion.VEHICULOS -> {
                        if (vehiculosFiltrados.isEmpty()) {
                            item {
                                LinceEmptyState(
                                    title = "Sin vehículos disponibles",
                                    description = "No hay unidades registradas para esta oficina.",
                                    icon = Icons.Default.AccountBox
                                )
                            }
                        } else {
                            items(vehiculosFiltrados) { item ->
                                VehiculoCompletoCard(item)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HitoCompletoCard(hito: HitoDto, titulosPorServicio: Map<String, String>) {
    val autor = hito.usuario?.nombre?.takeIf { it.isNotBlank() } ?: "Sin autor"
    val fecha = formatearFechaIsoCorta(hito.timestampRegistro)
    val tituloServicio = hito.servicioId?.let { titulosPorServicio[it] }
    val coords = if (hito.latitudMarcaje != null && hito.longitudMarcaje != null) {
        String.format(Locale.US, "%.4f, %.4f", hito.latitudMarcaje, hito.longitudMarcaje)
    } else null

    LinceSurfaceCard(variant = LinceSurfaceCardVariant.Default) {
        Column(verticalArrangement = Arrangement.spacedBy(LinceSpacing.xSmall)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = LinceTheme.colors.success,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(LinceSpacing.xSmall))
                    Text(
                        text = hito.tipo ?: "Hito",
                        style = MaterialTheme.typography.titleSmall,
                        color = LinceTheme.colors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                LinceStatusBadge(
                    text = "Srv #${hito.servicioId ?: "-"}",
                    tone = LinceStatusTone.Success
                )
            }
            Text(
                text = "$autor · $fecha",
                style = MaterialTheme.typography.bodySmall,
                color = LinceTheme.colors.textSecondary
            )
            if (!tituloServicio.isNullOrBlank()) {
                Text(
                    text = tituloServicio,
                    style = MaterialTheme.typography.bodySmall,
                    color = LinceTheme.colors.textPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
            if (coords != null) {
                Text(
                    text = "GPS: $coords",
                    style = MaterialTheme.typography.labelSmall,
                    color = LinceTheme.colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun IncidenciaCompletaCard(incidencia: IncidenciaDto, titulosPorServicio: Map<String, String>) {
    val autor = incidencia.usuario?.nombre?.takeIf { it.isNotBlank() } ?: "Sin autor"
    val fecha = formatearFechaIsoCorta(incidencia.fechaHora)
    val tituloServicio = incidencia.servicioId?.let { titulosPorServicio[it] }

    LinceSurfaceCard(variant = LinceSurfaceCardVariant.Default) {
        Column(verticalArrangement = Arrangement.spacedBy(LinceSpacing.xSmall)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = LinceTheme.colors.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(LinceSpacing.xSmall))
                    Text(
                        text = incidencia.motivo ?: "Incidencia",
                        style = MaterialTheme.typography.titleSmall,
                        color = LinceTheme.colors.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                LinceStatusBadge(
                    text = "Srv #${incidencia.servicioId ?: "-"}",
                    tone = LinceStatusTone.Error
                )
            }
            Text(
                text = "$autor · $fecha",
                style = MaterialTheme.typography.bodySmall,
                color = LinceTheme.colors.textSecondary
            )
            if (!tituloServicio.isNullOrBlank()) {
                Text(
                    text = tituloServicio,
                    style = MaterialTheme.typography.bodySmall,
                    color = LinceTheme.colors.textPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
            if (!incidencia.detalle.isNullOrBlank()) {
                Text(
                    text = incidencia.detalle,
                    style = MaterialTheme.typography.bodySmall,
                    color = LinceTheme.colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun GuiaCompletoCard(guia: GuiaDto) {
    val nombre = guia.nombre ?: "Sin nombre"
    val iniciales = nombre.split(" ").filter { it.isNotBlank() }.take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").ifBlank { "G" }
    val idiomas = guia.idiomas.mapNotNull { it.codigo }.joinToString(", ").ifBlank { "Sin idiomas" }

    LinceSurfaceCard(variant = LinceSurfaceCardVariant.Default) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LinceSpacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LinceUserAvatar(initials = iniciales, size = 40.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.titleSmall,
                    color = LinceTheme.colors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(LinceSpacing.xSmall)) {
                    LinceStatusBadge(text = "Guía", tone = LinceStatusTone.Success)
                    LinceStatusBadge(text = guia.oficina?.codigo ?: "-", tone = LinceStatusTone.Info)
                }
                Text(
                    text = "Idiomas: $idiomas",
                    style = MaterialTheme.typography.bodySmall,
                    color = LinceTheme.colors.textSecondary
                )
                val meta = buildList {
                    guia.evaluacionPromedio?.let { add("★ ${String.format(Locale.US, "%.1f", it)}") }
                    guia.totalServicios?.let { add("$it servicios") }
                }.joinToString(" · ")
                if (meta.isNotBlank()) {
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.labelSmall,
                        color = LinceTheme.colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ConductorCompletoCard(conductor: TransportistaDto) {
    val nombre = conductor.nombre ?: "Sin nombre"
    val iniciales = nombre.split(" ").filter { it.isNotBlank() }.take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").ifBlank { "C" }
    val vehiculosTexto = conductor.vehiculos.joinToString(", ") { v ->
        listOfNotNull(v.patente, v.tipo).joinToString(" ")
    }.ifBlank { "Sin vehículo asignado" }

    LinceSurfaceCard(variant = LinceSurfaceCardVariant.Default) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LinceSpacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LinceUserAvatar(initials = iniciales, size = 40.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.titleSmall,
                    color = LinceTheme.colors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(LinceSpacing.xSmall)) {
                    LinceStatusBadge(text = "Conductor", tone = LinceStatusTone.Neutral)
                    LinceStatusBadge(text = conductor.oficina?.codigo ?: "-", tone = LinceStatusTone.Info)
                }
                Text(
                    text = "Flota: $vehiculosTexto",
                    style = MaterialTheme.typography.bodySmall,
                    color = LinceTheme.colors.textSecondary
                )
                val meta = buildList {
                    conductor.evaluacionPromedio?.let { add("★ ${String.format(Locale.US, "%.1f", it)}") }
                    conductor.totalServicios?.let { add("$it servicios") }
                }.joinToString(" · ")
                if (meta.isNotBlank()) {
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.labelSmall,
                        color = LinceTheme.colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun VehiculoCompletoCard(item: VehiculoConConductor) {
    val v = item.vehiculo
    LinceSurfaceCard(variant = LinceSurfaceCardVariant.Default) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = v.patente ?: "Sin patente",
                    style = MaterialTheme.typography.titleSmall,
                    color = LinceTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${v.tipo ?: "Unidad"} · ${v.capacidad ?: 0} pax",
                    style = MaterialTheme.typography.bodySmall,
                    color = LinceTheme.colors.textSecondary
                )
                Text(
                    text = "Conductor: ${item.conductorNombre}",
                    style = MaterialTheme.typography.labelSmall,
                    color = LinceTheme.colors.textSecondary
                )
            }
            LinceStatusBadge(text = item.oficinaCodigo, tone = LinceStatusTone.Info)
        }
    }
}

@Composable
private fun coordinacionChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = LinceTheme.colors.surfaceContainerHigh,
    labelColor = LinceTheme.colors.textSecondary,
    selectedContainerColor = LinceTheme.colors.greenPrimary,
    selectedLabelColor = LinceTheme.colors.onPrimary
)