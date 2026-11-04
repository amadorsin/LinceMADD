package com.example.linceops.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linceops.model.EstadoServicio
import com.example.linceops.model.Servicio
import com.example.linceops.ui.theme.*

@Composable
fun ServiciosScreen(
    servicios: List<Servicio>,
    onVerDetalle: (String) -> Unit = {},
    filtroEstado: EstadoServicio?,
    filtroFecha: String?,
    onFiltrarPorEstado: (EstadoServicio?) -> Unit,
    onFiltrarPorFecha: (String) -> Unit,
    onLimpiarFiltros: () -> Unit
) {
    var textoFecha by remember(filtroFecha) { mutableStateOf(filtroFecha ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LinceCreamBackground)
    ) {
        // Encabezado con marca, título y contador de resultados
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 18.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "DESPACHO DE SERVICIOS",
                    style = MaterialTheme.typography.labelSmall,
                    color = LinceTextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Servicios Asignados",
                    style = MaterialTheme.typography.headlineSmall,
                    color = LinceGreenPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = LinceSurfaceContainerHigh,
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "${servicios.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = LinceGreenDark,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            FiltrosSection(
                filtroEstado = filtroEstado,
                textoFecha = textoFecha,
                onTextoFechaChange = { textoFecha = it },
                onFiltrarPorEstado = onFiltrarPorEstado,
                onFiltrarPorFecha = { onFiltrarPorFecha(textoFecha) },
                onLimpiarFiltros = {
                    textoFecha = ""
                    onLimpiarFiltros()
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (servicios.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = LinceSurfaceContainerHigh
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = LinceGreenPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Text(
                        text = "Sin resultados",
                        style = MaterialTheme.typography.titleMedium,
                        color = LinceTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "No hay servicios que coincidan con los filtros.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LinceTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(servicios) { servicio ->
                    ServicioItem(
                        servicio = servicio,
                        onClickDetalle = { onVerDetalle(servicio.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FiltrosSection(
    filtroEstado: EstadoServicio?,
    textoFecha: String,
    onTextoFechaChange: (String) -> Unit,
    onFiltrarPorEstado: (EstadoServicio?) -> Unit,
    onFiltrarPorFecha: () -> Unit,
    onLimpiarFiltros: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Chips de estado en varias líneas: visibles todos sin recortes
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filtroEstado == null,
                onClick = { onFiltrarPorEstado(null) },
                label = { Text("Todos") },
                shape = RoundedCornerShape(50),
                colors = filterChipColors()
            )

            EstadoServicio.entries
                .sortedBy { estado ->
                    when (estado) {
                        EstadoServicio.PENDIENTE -> 0
                        EstadoServicio.CONFIRMADO -> 1
                        EstadoServicio.EN_CURSO -> 2
                        EstadoServicio.FINALIZADO -> 3
                        EstadoServicio.RECHAZADO -> 4
                    }
                }
                .forEach { estado ->
                FilterChip(
                    selected = filtroEstado == estado,
                    onClick = { onFiltrarPorEstado(estado) },
                    label = { Text(nombreEstado(estado)) },
                    shape = RoundedCornerShape(50),
                    colors = filterChipColors()
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textoFecha,
                onValueChange = onTextoFechaChange,
                label = { Text("Fecha (dd/MM/yyyy)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Icono Calendario",
                        tint = LinceGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LinceGreenPrimary,
                    unfocusedBorderColor = LinceOutlineVariant,
                    focusedContainerColor = LinceCardSurface,
                    unfocusedContainerColor = LinceCardSurface
                )
            )

            Spacer(modifier = Modifier.width(10.dp))

            FilledIconButton(
                onClick = onFiltrarPorFecha,
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = LinceGreenPrimary,
                    contentColor = LinceCardSurface
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Filtrar por fecha"
                )
            }
        }

        TextButton(
            onClick = onLimpiarFiltros,
            modifier = Modifier.align(Alignment.End),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "Limpiar filtros",
                color = LinceGreenPrimary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun filterChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = LinceSurfaceContainerHigh,
    labelColor = LinceTextSecondary,
    selectedContainerColor = LinceGreenPrimary,
    selectedLabelColor = LinceCardSurface
)

private fun nombreEstado(estado: EstadoServicio): String = when (estado) {
    EstadoServicio.PENDIENTE -> "Pendiente"
    EstadoServicio.CONFIRMADO -> "Confirmado"
    EstadoServicio.RECHAZADO -> "Rechazado"
    EstadoServicio.EN_CURSO -> "En Curso"
    EstadoServicio.FINALIZADO -> "Finalizado"
}

@Composable
fun ServicioItem(
    servicio: Servicio,
    onClickDetalle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LinceSurfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Fila superior: hora destacada y badge de estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = LinceGreenPrimary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = servicio.hora,
                        style = MaterialTheme.typography.labelLarge,
                        color = LinceCardSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                EstadoBadge(estado = servicio.estado)
            }

            Text(
                text = servicio.titulo,
                style = MaterialTheme.typography.titleMedium,
                color = LinceTextPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Bloque de ruta sobre superficie blanca suave
            Surface(
                color = LinceCardSurface.copy(alpha = 0.72f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Ruta",
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Pasajeros",
                    tint = LinceGreenPrimary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${servicio.pasajeros} Pasajeros",
                    style = MaterialTheme.typography.labelMedium,
                    color = LinceTextSecondary
                )
                Spacer(modifier = Modifier.width(14.dp))
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Pickup",
                    tint = LinceTertiary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Pickup: ${servicio.pickup}",
                    style = MaterialTheme.typography.labelMedium,
                    color = LinceTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Button(
                onClick = onClickDetalle,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LinceGreenPrimary),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text(
                    text = "Ver Detalle del Servicio",
                    color = LinceCardSurface,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun EstadoBadge(
    estado: EstadoServicio,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, texto) = when (estado) {
        EstadoServicio.PENDIENTE -> Triple(StatusPendingBg, StatusPendingText, "Pendiente")
        EstadoServicio.CONFIRMADO -> Triple(StatusConfirmedBg, StatusConfirmedText, "Confirmado")
        EstadoServicio.EN_CURSO -> Triple(StatusInProgressBg, StatusInProgressText, "En Curso")
        EstadoServicio.FINALIZADO -> Triple(StatusCompletedBg, StatusCompletedText, "Finalizado")
        EstadoServicio.RECHAZADO -> Triple(StatusRejectedBg, StatusRejectedText, "Rechazado")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(textColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = texto,
                color = textColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
