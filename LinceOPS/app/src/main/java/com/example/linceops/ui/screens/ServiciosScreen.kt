package com.example.linceops.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import com.example.linceops.R
import com.example.linceops.model.CodigoIdioma
import com.example.linceops.model.EstadoServicio
import com.example.linceops.model.RolUsuario
import com.example.linceops.model.Servicio
import com.example.linceops.model.TipoServicio
import com.example.linceops.model.TipoVehiculo
import com.example.linceops.ui.components.CampoFecha
import com.example.linceops.ui.components.EfectoScrollBarraInferior
import com.example.linceops.ui.components.ServiciosListSkeleton
import com.example.linceops.ui.components.esFechaCompleta
import com.example.linceops.ui.theme.*

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
            .background(LinceCreamBackground),
        contentPadding = PaddingValues(bottom = 124.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            EncabezadoServicios(cantidad = servicios.size)
        }

        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 48.dp),
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
                            text = stringResource(R.string.servicios_sin_resultados),
                            style = MaterialTheme.typography.titleMedium,
                            color = LinceTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.servicios_sin_coincidencias),
                            style = MaterialTheme.typography.bodyMedium,
                            color = LinceTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(
                items = servicios,
                key = { it.id }
            ) { servicio ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
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
            .padding(horizontal = 24.dp)
            .padding(top = 22.dp, bottom = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.servicios_despacho),
            style = MaterialTheme.typography.labelSmall,
            color = LinceTextSecondary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.8.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.servicios_asignados),
            style = MaterialTheme.typography.headlineSmall,
            color = LinceGreenPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            color = LinceGreenSageLight,
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, LinceGreenBorder)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(LinceGreenPrimary, CircleShape)
                )
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = if (cantidad == 1) {
                        stringResource(R.string.servicios_cantidad_uno, cantidad)
                    } else {
                        stringResource(R.string.servicios_cantidad_varios, cantidad)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = LinceGreenDark,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp
                )
            }
        }
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
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = LinceCardSurface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, LinceOutlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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

            Spacer(modifier = Modifier.height(12.dp))

            CampoFecha(
                valor = textoFecha,
                onValorChange = onTextoFechaChange,
                etiqueta = stringResource(R.string.servicios_filtrar_fecha),
                placeholder = "dd/MM/yyyy"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onLimpiarFiltros,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.servicios_limpiar_filtros),
                        color = LinceGreenPrimary,
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
    containerColor = LinceSurfaceContainerHigh,
    labelColor = LinceTextSecondary,
    selectedContainerColor = LinceGreenPrimary,
    selectedLabelColor = LinceOnPrimary
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
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LinceSurfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(
            width = if (esEnfocado) 1.8.dp else 1.dp,
            color = if (esEnfocado) LinceGreenPrimary else LinceOutlineVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = LinceGreenPrimary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = servicio.hora,
                            style = MaterialTheme.typography.labelLarge,
                            color = LinceOnPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        color = LinceGreenSageLight,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, LinceGreenBorder)
                    ) {
                        val tagOficina = if (servicio.statusPL != null) {
                            "${servicio.oficina.name} • ${servicio.statusPL.name}"
                        } else {
                            servicio.oficina.name
                        }
                        Text(
                            text = tagOficina,
                            style = MaterialTheme.typography.labelSmall,
                            color = LinceGreenDark,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
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

            Surface(
                color = LinceCardSurface.copy(alpha = 0.72f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                    if (servicio.dropoff.isNotBlank()) {
                        Text(
                            text = stringResource(R.string.servicios_dropoff, servicio.dropoff),
                            style = MaterialTheme.typography.labelSmall,
                            color = LinceTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 24.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(R.string.comun_pasajeros),
                    tint = LinceGreenPrimary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                val textoPax = if (servicio.capacidadVehiculo > 0) {
                    stringResource(R.string.servicios_pax_capacidad, servicio.pasajeros, servicio.capacidadVehiculo)
                } else {
                    stringResource(R.string.servicios_pax, servicio.pasajeros)
                }
                Text(
                    text = textoPax,
                    style = MaterialTheme.typography.labelMedium,
                    color = LinceTextSecondary
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "•  ${servicio.vehiculo}",
                    style = MaterialTheme.typography.labelMedium,
                    color = LinceGreenDark,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = stringResource(R.string.comun_pickup),
                    tint = LinceTertiary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.servicios_pickup, servicio.pickup),
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
                    text = stringResource(R.string.servicios_ver_detalle),
                    color = LinceOnPrimary,
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
        EstadoServicio.PENDIENTE -> Triple(StatusPendingBg, StatusPendingText, stringResource(R.string.estado_pendiente))
        EstadoServicio.CONFIRMADO -> Triple(StatusConfirmedBg, StatusConfirmedText, stringResource(R.string.estado_confirmado))
        EstadoServicio.EN_CURSO -> Triple(StatusInProgressBg, StatusInProgressText, stringResource(R.string.estado_en_curso))
        EstadoServicio.FINALIZADO -> Triple(StatusCompletedBg, StatusCompletedText, stringResource(R.string.estado_finalizado))
        EstadoServicio.RECHAZADO -> Triple(StatusRejectedBg, StatusRejectedText, stringResource(R.string.estado_rechazado))
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
