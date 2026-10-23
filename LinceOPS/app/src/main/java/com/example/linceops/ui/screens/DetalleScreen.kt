package com.example.linceops.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.linceops.model.EstadoServicio
import com.example.linceops.model.RolUsuario
import com.example.linceops.model.Servicio

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(
    servicio: Servicio,
    rolUsuario: RolUsuario,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit,
    onReasignar: () -> Unit,
    onIrAMarcaje: () -> Unit // Nuevo parámetro para navegar al Check-In
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Final Update") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Volcado de datos reales
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Fecha: ${servicio.fecha} | Hora: ${servicio.hora}")
                Text(text = "Ruta: ${servicio.origenDestino}")
                Text(text = "Punto de encuentro: ${servicio.pickup}")
                Text(text = "Agencia: ${servicio.agencia} | Hotel: ${servicio.hotel}")
                Text(text = "Pasajeros: ${servicio.pasajeros} (${servicio.idioma})")
                Text(text = "Tipo: ${servicio.tipo} | Vehículo: ${servicio.vehiculo}")
                Text(text = "Inclusiones: ${servicio.inclusiones.joinToString(", ")}")
                Text(text = "Instrucciones: ${servicio.instrucciones}")
                Text(text = "Observaciones: ${servicio.observaciones}")
            }

            // Lógica de botones inteligentes
            val estadoActual = if (rolUsuario == RolUsuario.ADMINISTRADOR) {
                servicio.estado
            } else {
                servicio.participaciones.firstOrNull { it.usuarioId == servicio.conductorId || it.usuarioId == servicio.guiaId }?.estado ?: servicio.estado
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (rolUsuario == RolUsuario.ADMINISTRADOR && estadoActual == EstadoServicio.RECHAZADO) {
                    Button(onClick = onReasignar, modifier = Modifier.fillMaxWidth()) {
                        Text("Reasignar Recurso")
                    }
                } else if (rolUsuario == RolUsuario.CONDUCTOR && (estadoActual == EstadoServicio.CONFIRMADO || estadoActual == EstadoServicio.EN_CURSO)) {
                    Button(onClick = onIrAMarcaje, modifier = Modifier.fillMaxWidth()) {
                        Text("Ir a Check-In / Check-Out")
                    }
                } else if (estadoActual == EstadoServicio.PENDIENTE && rolUsuario != RolUsuario.ADMINISTRADOR) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(onClick = onRechazar, modifier = Modifier.weight(1f)) {
                            Text("Rechazar")
                        }
                        Button(onClick = onAceptar, modifier = Modifier.weight(1f)) {
                            Text("Aceptar")
                        }
                    }
                }
            }
        }
    }
}