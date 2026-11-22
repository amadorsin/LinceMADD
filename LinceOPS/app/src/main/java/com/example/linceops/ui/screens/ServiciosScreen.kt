package com.example.linceops.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linceops.R
import com.example.linceops.model.CodigoIdioma
import com.example.linceops.model.EstadoServicio
import com.example.linceops.model.RolUsuario
import com.example.linceops.model.Servicio
import com.example.linceops.model.TipoServicio
import com.example.linceops.model.TipoVehiculo
import com.example.linceops.ui.components.CampoFecha
import com.example.linceops.ui.components.EfectoScrollBarraInferior
import com.example.linceops.ui.components.LinceButton
import com.example.linceops.ui.components.LinceButtonVariant
import com.example.linceops.ui.components.LinceDivider
import com.example.linceops.ui.components.LinceEmptyState
import com.example.linceops.ui.components.LinceSectionHeader
import com.example.linceops.ui.components.LinceStatusBadge
import com.example.linceops.ui.components.LinceStatusTone
import com.example.linceops.ui.components.LinceSurfaceCard
import com.example.linceops.ui.components.LinceSurfaceCardVariant
import com.example.linceops.ui.components.ServiciosListSkeleton
import com.example.linceops.ui.components.esFechaCompleta
import com.example.linceops.ui.theme.LinceSpacing
import com.example.linceops.ui.theme.LinceRadius
import com.example.linceops.ui.theme.LinceTheme

@Composable
fun ServiciosScreen(
    servicios: List<Servicio>,
    onVerDetalle: (String) -> Unit = {},
    filtroEstado: EstadoServicio?,
    filtroFecha: String?,
    onFiltrarPorEstado: (EstadoServicio?) -> Unit,
    onFiltrarPorFecha: (String) -> Unit,
    onLimpiarFiltros: () -> Unit,
    onScrollBarra: (Boolean) -> Unit = {},
    isLoading: Boolean = false,
    servicioEnfocadoId: String? = null,
    abrirDetallePendienteId: String? = null,
    onDetallePendienteAbierto: (String) -> Unit = {}
) {
    if (isLoading && servicios.isEmpty()) {
        ServiciosListSkeleton()
        return
    }

    val listState = rememberLazyListState()
    EfectoScrollBarraInferior(listState = listState, onVisibleChange = onScrollBarra)
    var textoFecha by remember(filtroFecha) { mutableStateOf(filtroFecha ?: "") }

    LaunchedEffect(textoFecha) {
        if (textoFecha.isBlank()) {
            if (!filtroFecha.isNullOrBlank()) onFiltrarPorFecha("")
        } else if (esFechaCompleta(textoFecha) && textoFecha != filtroFecha) {
            onFiltrarPorFecha(textoFecha)
        }
    }

    LaunchedEffect(servicioEnfocadoId, abrirDetallePendienteId, servicios) {
        val idObjetivo = abrirDetallePendienteId ?: servicioEnfocadoId
        if (idObjetivo != null) {
            val indice = servicios.indexOfFirst { it.id == idObjetivo }
            if (indice >= 0) {
                listState.scrollToItem(indice + 2)
                if (abrirDetallePendienteId == idObjetivo) {
                    onDetallePendienteAbierto(idObjetivo)
                }
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(LinceTheme.colors.background),
        contentPadding = PaddingValues(bottom = 124.dp),
        verticalArrangement = Arrangement.spacedBy(LinceSpacing.mediumSmall)
    ) {
        item {
            EncabezadoServicios(cantidad = servicios.size)
        }

        item {
            Box(modifier = Modifier.padding(horizontal = LinceSpacing.medium)) {
                FiltrosSection(
                    filtroEstado = filtroEstado,
                    textoFecha = textoFecha,
                    onTextoFechaChange = { textoFecha = it },
                    onFiltrarPorEstado = onFiltrarPorEstado,
                    onLimpiarFiltros = {
                        textoFecha = ""
                        onLimpiarFiltros()
                    }
                )
            }
        }

        if (servicios.isEmpty()) {
            item {
                LinceEmptyState(
                    title = stringResource(R.string.servicios_sin_resultados),
                    description = stringResource(R.string.servicios_sin_coincidencias)
                )
            }
        } else {
            items(
                items = servicios,
                key = { it.id }
            ) { servicio ->
                Box(modifier = Modifier.padding(horizontal = LinceSpacing.medium)) {
                    ServicioItem(
                        servicio = servicio,
                        esEnfocado = servicio.id == servicioEnfocadoId,
                        onClickDetalle = { onVerDetalle(servicio.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EncabezadoServicios(cantidad: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = LinceSpacing.xLarge)
            .padding(top = LinceSpacing.xLarge, bottom = LinceSpacing.small),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LinceSpacing.xSmall)
    ) {
        Text(
            text = stringResource(R.string.servicios_despacho),
            style = MaterialTheme.typography.labelSmall,
            color = LinceTheme.colors.textSecondary,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp
        )
        Text(
            text = stringResource(R.string.servicios_asignados),
            style = MaterialTheme.typography.headlineSmall,
            color = LinceTheme.colors.textPrimary,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        LinceStatusBadge(
            text = if (cantidad == 1) {
                stringResource(R.string.servicios_cantidad_uno, cantidad)
            } else {
                stringResource(R.string.servicios_cantidad_varios, cantidad)
            },
            tone = LinceStatusTone.Success
        )
    }
}

@Composable
fun FiltrosSection(
    filtroEstado: EstadoServicio?,
    textoFecha: String,
    onTextoFechaChange: (String) -> Unit,
    onFiltrarPorEstado: (EstadoServicio?) -> Unit,
    onLimpiarFiltros: () -> Unit
) {
    val context = LocalContext.current
    LinceSurfaceCard(
        variant = LinceSurfaceCardVariant.Subtle,
        contentPadding = LinceSpacing.mediumSmall
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(LinceSpacing.small)) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(LinceSpacing.small)
            ) {
                item {
                    FilterChip(
                        selected = filtroEstado == null,
                        onClick = { onFiltrarPorEstado(null) },
                        label = { Text(stringResource(R.string.dashboard_todos)) },
                        shape = RoundedCornerShape(50),
                        colors = filterChipColors()
                    )
                }
                items(estadosOrdenados) { estado ->
                    FilterChip(
                        selected = filtroEstado == estado,
                        onClick = { onFiltrarPorEstado(estado) },
                        label = { Text(nombreEstado(context, estado)) },
                        shape = RoundedCornerShape(50),
                        colors = filterChipColors()
                    )
                }
            }
            CampoFecha(
                valor = textoFecha,
                onValorChange = onTextoFechaChange,
                etiqueta = stringResource(R.string.servicios_filtrar_fecha),
                placeholder = "dd/MM/yyyy"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.TextButton(
                    onClick = onLimpiarFiltros,
                    contentPadding = PaddingValues(horizontal = LinceSpacing.small, vertical = 2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.servicios_limpiar_filtros),
                        color = LinceTheme.colors.greenPrimary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
internal fun filterChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = LinceTheme.colors.surfaceContainerHigh,
    labelColor = LinceTheme.colors.textSecondary,
    selectedContainerColor = LinceTheme.colors.greenPrimary,
    selectedLabelColor = LinceTheme.colors.onPrimary
)

internal val estadosOrdenados = listOf(
    EstadoServicio.PENDIENTE,
    EstadoServicio.CONFIRMADO,
    EstadoServicio.EN_CURSO,
    EstadoServicio.FINALIZADO,
    EstadoServicio.RECHAZADO
)

internal fun nombreEstado(context: Context, estado: EstadoServicio): String = when (estado) {
    EstadoServicio.PENDIENTE -> context.getString(R.string.estado_pendiente)
    EstadoServicio.CONFIRMADO -> context.getString(R.string.estado_confirmado)
    EstadoServicio.RECHAZADO -> context.getString(R.string.estado_rechazado)
    EstadoServicio.EN_CURSO -> context.getString(R.string.estado_en_curso)
    EstadoServicio.FINALIZADO -> context.getString(R.string.estado_finalizado)
}

internal fun nombreRol(context: Context, rol: RolUsuario): String = when (rol) {
    RolUsuario.CONDUCTOR -> context.getString(R.string.rol_conductor)
    RolUsuario.GUIA -> context.getString(R.string.rol_guia)
    RolUsuario.ADMINISTRADOR -> context.getString(R.string.rol_administrador)
}

internal fun nombreTipo(context: Context, tipo: TipoServicio): String = when (tipo) {
    TipoServicio.TRANSFER -> context.getString(R.string.tipo_srv_transfer)
    TipoServicio.GUIDED_SERVICE -> context.getString(R.string.tipo_srv_guiado)
    TipoServicio.EXCURSION -> context.getString(R.string.tipo_srv_excursion)
}

internal fun nombreTipoVehiculo(context: Context, tipo: TipoVehiculo): String = when (tipo) {
    TipoVehiculo.SEDAN -> context.getString(R.string.vehiculo_sedan)
    TipoVehiculo.VAN -> context.getString(R.string.vehiculo_van)
    TipoVehiculo.MINIBUS -> context.getString(R.string.vehiculo_minibus)
    TipoVehiculo.BUS -> context.getString(R.string.vehiculo_bus)
}

internal fun nombreIdioma(context: Context, idioma: CodigoIdioma): String = when (idioma) {
    CodigoIdioma.ES -> context.getString(R.string.idioma_es)
    CodigoIdioma.EN -> context.getString(R.string.idioma_en)
    CodigoIdioma.PT -> context.getString(R.string.idioma_pt)
    CodigoIdioma.DE -> context.getString(R.string.idioma_de)
    CodigoIdioma.FR -> context.getString(R.string.idioma_fr)
}

@Composable
fun ServicioItem(
    servicio: Servicio,
    esEnfocado: Boolean = false,
    onClickDetalle: () -> Unit
) {
    Box(
        modifier = if (esEnfocado) {
            Modifier
                .fillMaxWidth()
                .border(
                    BorderStroke(1.5.dp, LinceTheme.colors.greenPrimary),
                    RoundedCornerShape(LinceRadius.large)
                )
        } else {
            Modifier.fillMaxWidth()
        }
    ) {
        LinceSurfaceCard(
            modifier = if (esEnfocado) Modifier.padding(1.dp) else Modifier,
            variant = if (esEnfocado) {
                LinceSurfaceCardVariant.Highlighted
            } else {
                LinceSurfaceCardVariant.Default
            },
            contentPadding = LinceSpacing.medium
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(LinceSpacing.small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(LinceSpacing.xSmall)
                ) {
                    Text(
                        text = servicio.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        color = LinceTheme.colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (servicio.statusPL != null) {
                            "${servicio.oficina.name} · ${servicio.statusPL.name}"
                        } else {
                            servicio.oficina.name
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = LinceTheme.colors.textSecondary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(LinceSpacing.small))
                EstadoBadge(estado = servicio.estado)
            }

            LinceDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(LinceSpacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetadataServicio(
                    icono = Icons.Default.DateRange,
                    texto = stringResource(
                        R.string.dashboard_fecha_hora_oficina,
                        servicio.fecha,
                        servicio.hora,
                        servicio.oficina
                    ),
                    modifier = Modifier.weight(1f)
                )
                MetadataServicio(
                    icono = Icons.Default.Person,
                    texto = if (servicio.capacidadVehiculo > 0) {
                        stringResource(
                            R.string.servicios_pax_capacidad,
                            servicio.pasajeros,
                            servicio.capacidadVehiculo
                        )
                    } else {
                        stringResource(R.string.servicios_pax, servicio.pasajeros)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            MetadataServicio(
                icono = Icons.Default.LocationOn,
                texto = servicio.origenDestino
            )
            if (servicio.dropoff.isNotBlank()) {
                MetadataServicio(
                    icono = Icons.Default.LocationOn,
                    texto = stringResource(R.string.servicios_dropoff, servicio.dropoff)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.servicios_pickup, servicio.pickup),
                    style = MaterialTheme.typography.labelMedium,
                    color = LinceTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(LinceSpacing.small))
                Text(
                    text = servicio.vehiculo,
                    style = MaterialTheme.typography.labelMedium,
                    color = LinceTheme.colors.greenDark,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            LinceButton(
                text = stringResource(R.string.servicios_ver_detalle),
                onClick = onClickDetalle,
                variant = LinceButtonVariant.Outline,
                fullWidth = true
            )
            }
        }
    }
}

@Composable
private fun MetadataServicio(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    texto: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(LinceSpacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Icon(
            imageVector = icono,
            contentDescription = null,
            tint = LinceTheme.colors.greenPrimary,
            modifier = Modifier.size(16.dp)
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
fun EstadoBadge(
    estado: EstadoServicio,
    modifier: Modifier = Modifier
) {
    val (texto, tono) = when (estado) {
        EstadoServicio.PENDIENTE -> stringResource(R.string.estado_pendiente) to LinceStatusTone.Warning
        EstadoServicio.CONFIRMADO -> stringResource(R.string.estado_confirmado) to LinceStatusTone.Success
        EstadoServicio.EN_CURSO -> stringResource(R.string.estado_en_curso) to LinceStatusTone.Info
        EstadoServicio.FINALIZADO -> stringResource(R.string.estado_finalizado) to LinceStatusTone.Neutral
        EstadoServicio.RECHAZADO -> stringResource(R.string.estado_rechazado) to LinceStatusTone.Error
    }
    LinceStatusBadge(text = texto, tone = tono, modifier = modifier)
}
