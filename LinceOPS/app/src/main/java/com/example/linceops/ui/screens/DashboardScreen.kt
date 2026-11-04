package com.example.linceops.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linceops.model.EstadoServicio
import com.example.linceops.model.RolUsuario
import com.example.linceops.model.Servicio
import com.example.linceops.ui.theme.*
import com.example.linceops.viewmodel.ServiceUiState

@Composable
fun DashboardScreen(
    rolUsuario: RolUsuario,
    uiState: ServiceUiState,
    onIniciarMarcaje: (String) -> Unit,
    onCrearServicio: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val nombreUsuario = uiState.usuarioLogueado?.nombre ?: "Usuario"
    val iniciales = nombreUsuario
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LinceCreamBackground),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Barra superior de marca y usuario (patrón de los bocetos)
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
                            text = rolUsuario.nombreMostrar.uppercase(),
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
                            text = "Base Operativa",
                            style = MaterialTheme.typography.labelMedium,
                            color = LinceTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = LinceGreenSageLight
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
                    text = "Hola, $nombreUsuario",
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
                        text = "Panel Operativo · Turno activo",
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
                        uiState = uiState,
                        onCrearServicio = onCrearServicio
                    )

                RolUsuario.CONDUCTOR ->
                    DashboardDriverView(
                        uiState = uiState,
                        onIniciarMarcaje = onIniciarMarcaje
                    )

                RolUsuario.GUIA ->
                    DashboardGuiaView(uiState)
            }
        }

        if (uiState.servicios.isNotEmpty()) {
            item {
                ResumenServicios(
                    servicios = uiState.servicios,
                    uiState = uiState
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
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LinceCardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    text = "Panel de administración",
                    style = MaterialTheme.typography.titleMedium,
                    color = LinceGreenDark,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = LinceGreenSageLight.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "HOY",
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
                    etiqueta = "SERVICIOS VISIBLES",
                    valor = "${uiState.servicios.size}",
                    detalle = "En seguimiento",
                    colorValor = LinceGreenDark,
                    colorFondo = LinceGreenSageLight.copy(alpha = 0.55f),
                    colorIcono = LinceGreenPrimary,
                    icono = Icons.Default.List
                )

                KpiCard(
                    modifier = Modifier.weight(1f),
                    etiqueta = "PENDIENTES",
                    valor = "$pendientes",
                    detalle = "Requieren gestión",
                    colorValor = StatusPendingText,
                    colorFondo = StatusPendingBg.copy(alpha = 0.6f),
                    colorIcono = StatusPendingText,
                    icono = Icons.Default.Warning
                )
            }

            Surface(
                color = LinceSurfaceContainerLow,
                shape = RoundedCornerShape(12.dp),
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
                        text = "Consulta el feed para revisar los servicios.",
                        style = MaterialTheme.typography.bodySmall,
                        color = LinceTextSecondary
                    )
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
                    tint = LinceCardSurface,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Crear Nuevo Servicio",
                    color = LinceCardSurface,
                    fontWeight = FontWeight.SemiBold
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
        shape = RoundedCornerShape(14.dp)
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
                    color = colorValor.copy(alpha = 0.78f),
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
                    color = LinceCardSurface.copy(alpha = 0.65f)
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
                color = colorValor.copy(alpha = 0.72f)
            )
        }
    }
}

@Composable
private fun DashboardDriverView(
    uiState: ServiceUiState,
    onIniciarMarcaje: (String) -> Unit
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
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    text = "Mis servicios",
                    style = MaterialTheme.typography.titleMedium,
                    color = LinceGreenDark,
                    fontWeight = FontWeight.Bold
                )
                CountPill(texto = "Visibles: ${uiState.servicios.size}")
            }

            if (servicioMarcaje == null) {
                Surface(
                    color = LinceSurfaceContainerLow,
                    shape = RoundedCornerShape(14.dp),
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
                            text = "No tienes servicios confirmados o en curso disponibles para marcaje.",
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
                    color = LinceSurfaceVariant,
                    shape = RoundedCornerShape(14.dp),
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
                                    "Servicio en curso"
                                } else {
                                    "Próximo servicio"
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
                                    contentDescription = "Ruta",
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
                            texto = "Fecha: ${servicioMarcaje.fecha} | Hora: ${servicioMarcaje.hora}"
                        )
                        DashboardInfoRow(
                            icono = Icons.Default.LocationOn,
                            texto = "Punto de encuentro: ${servicioMarcaje.pickup}"
                        )
                        DashboardInfoRow(
                            icono = Icons.Default.Person,
                            texto = "Pasajeros: ${servicioMarcaje.pasajeros}"
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
                        text = "Iniciar Marcaje",
                        color = LinceCardSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardGuiaView(
    uiState: ServiceUiState
) {
    val servicio = uiState.servicios.firstOrNull()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LinceCardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    text = "Panel del guía",
                    style = MaterialTheme.typography.titleMedium,
                    color = LinceGreenDark,
                    fontWeight = FontWeight.Bold
                )
                CountPill(texto = "Asignados: ${uiState.servicios.size}")
            }

            if (servicio == null) {
                Surface(
                    color = LinceSurfaceContainerLow,
                    shape = RoundedCornerShape(14.dp),
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
                            text = "No tienes servicios asignados.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LinceTextSecondary
                        )
                    }
                }
            } else {
                Surface(
                    color = LinceSurfaceVariant,
                    shape = RoundedCornerShape(14.dp),
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
                                text = "Próximo servicio asignado",
                                style = MaterialTheme.typography.labelLarge,
                                color = LinceGreenPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            EstadoBadge(
                                estado = uiState.usuarioLogueado?.let {
                                    servicio.estadoPara(it)
                                } ?: servicio.estado
                            )
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

                        DashboardInfoRow(
                            icono = Icons.Default.DateRange,
                            texto = "Fecha: ${servicio.fecha} | Hora: ${servicio.hora}"
                        )
                        DashboardInfoRow(
                            icono = Icons.Default.LocationOn,
                            texto = "Punto de encuentro: ${servicio.pickup}"
                        )
                        DashboardInfoRow(
                            icono = Icons.Default.Person,
                            texto = "Pasajeros: ${servicio.pasajeros}"
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
    uiState: ServiceUiState
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Resumen de servicios",
                style = MaterialTheme.typography.titleMedium,
                color = LinceTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Últimos ${minOf(3, servicios.size)}",
                style = MaterialTheme.typography.labelSmall,
                color = LinceTextSecondary
            )
        }

        servicios.take(3).forEach { servicio ->
            Surface(
                color = LinceCardSurface,
                shape = RoundedCornerShape(14.dp),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = LinceGreenSageLight,
                        shape = RoundedCornerShape(8.dp)
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
                        Text(
                            text = servicio.titulo,
                            style = MaterialTheme.typography.bodyMedium,
                            color = LinceTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = servicio.origenDestino,
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
        shape = RoundedCornerShape(50)
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
