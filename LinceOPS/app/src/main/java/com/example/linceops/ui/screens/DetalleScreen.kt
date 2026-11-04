package com.example.linceops.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linceops.model.EstadoServicio
import com.example.linceops.model.RolUsuario
import com.example.linceops.model.Servicio
import com.example.linceops.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(
    servicio: Servicio,
    rolUsuario: RolUsuario,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit,
    onReasignar: () -> Unit,
    onIrAMarcaje: () -> Unit
) {
    val estadoActual = if (rolUsuario == RolUsuario.ADMINISTRADOR) {
        servicio.estado
    } else {
        servicio.participaciones.firstOrNull { it.usuarioId == servicio.conductorId || it.usuarioId == servicio.guiaId }?.estado ?: servicio.estado
    }

    val tieneAcciones =
        (rolUsuario == RolUsuario.ADMINISTRADOR && estadoActual == EstadoServicio.RECHAZADO) ||
                (rolUsuario == RolUsuario.CONDUCTOR && (estadoActual == EstadoServicio.CONFIRMADO || estadoActual == EstadoServicio.EN_CURSO)) ||
                (estadoActual == EstadoServicio.PENDIENTE && rolUsuario != RolUsuario.ADMINISTRADOR)

    Scaffold(
        containerColor = LinceCreamBackground,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "DETALLE DEL SERVICIO",
                                style = MaterialTheme.typography.labelSmall,
                                color = LinceTextSecondary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
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
                    .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Código del servicio y resumen rápido
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
                            text = "ID ${servicio.id}",
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
                            text = "${servicio.fecha}  •  ${servicio.hora}",
                            style = MaterialTheme.typography.labelSmall,
                            color = LinceGreenDark,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                // Bloque 1: ruta, fecha y punto de encuentro
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
                        SeccionHeader(icono = Icons.Default.LocationOn, titulo = "Ruta y Horario")
                        HorizontalDivider(color = LinceOutlineVariant.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FichaDato(
                                etiqueta = "FECHA",
                                valor = servicio.fecha,
                                modifier = Modifier.weight(1f)
                            )
                            FichaDato(
                                etiqueta = "HORA DE SALIDA",
                                valor = servicio.hora,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        ParadasRuta(
                            origenDestino = servicio.origenDestino,
                            hora = servicio.hora
                        )

                        FichaDato(
                            etiqueta = "PUNTO DE ENCUENTRO",
                            valor = servicio.pickup,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Bloque 2: operación, pasajeros y unidad
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
                        SeccionHeader(icono = Icons.Default.Person, titulo = "Operación y Pasajeros")
                        HorizontalDivider(color = LinceOutlineVariant.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FichaDato(
                                etiqueta = "AGENCIA",
                                valor = servicio.agencia,
                                modifier = Modifier.weight(1f)
                            )
                            FichaDato(
                                etiqueta = "HOTEL",
                                valor = servicio.hotel,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FichaDato(
                                etiqueta = "PASAJEROS",
                                valor = "${servicio.pasajeros}",
                                modifier = Modifier.weight(1f)
                            )
                            FichaDato(
                                etiqueta = "IDIOMA",
                                valor = servicio.idioma,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        FichaDato(
                            etiqueta = "TIPO Y VEHÍCULO",
                            valor = "${servicio.tipo} • ${servicio.vehiculo}",
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (servicio.inclusiones.isNotEmpty()) {
                            FichaDato(
                                etiqueta = "INCLUSIONES",
                                valor = servicio.inclusiones.joinToString(", "),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Bloque 3: notas operativas destacadas en tono cálido
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = LinceTertiaryContainer.copy(alpha = 0.14f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(1.dp, LinceTertiaryContainer.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SeccionHeader(
                            icono = Icons.Default.Info,
                            titulo = "Notas Operativas",
                            colorIcono = LinceTertiary
                        )
                        HorizontalDivider(color = LinceTertiaryContainer.copy(alpha = 0.35f))

                        FichaDato(
                            etiqueta = "INSTRUCCIONES",
                            valor = servicio.instrucciones.ifBlank { "Sin instrucciones especiales" },
                            modifier = Modifier.fillMaxWidth(),
                            colorFondo = LinceCardSurface.copy(alpha = 0.7f)
                        )
                        FichaDato(
                            etiqueta = "OBSERVACIONES",
                            valor = servicio.observaciones.ifBlank { "Sin observaciones" },
                            modifier = Modifier.fillMaxWidth(),
                            colorFondo = LinceCardSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
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
                        Text("Reasignar Recurso", color = LinceCardSurface, fontWeight = FontWeight.SemiBold)
                    }
                } else if (rolUsuario == RolUsuario.CONDUCTOR && (estadoActual == EstadoServicio.CONFIRMADO || estadoActual == EstadoServicio.EN_CURSO)) {
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
                        Text("Ir a Check-In / Check-Out", color = LinceCardSurface, fontWeight = FontWeight.SemiBold)
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
                            Text("Rechazar", fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = onAceptar,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LinceGreenPrimary)
                        ) {
                            Text("Aceptar", color = LinceCardSurface, fontWeight = FontWeight.SemiBold)
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
    colorIcono: androidx.compose.ui.graphics.Color = LinceGreenPrimary
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
    colorFondo: androidx.compose.ui.graphics.Color = LinceSurfaceContainerLow
) {
    Surface(
        modifier = modifier,
        color = colorFondo,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelSmall,
                color = LinceTextSecondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
                                    index == 0 -> "PICK-UP · $hora"
                                    esUltima -> "DESTINO FINAL"
                                    else -> "PARADA"
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
