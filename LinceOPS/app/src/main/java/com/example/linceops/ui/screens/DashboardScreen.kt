package com.example.linceops.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.linceops.model.EstadoServicio
import com.example.linceops.model.RolUsuario
import com.example.linceops.model.Servicio
import com.example.linceops.model.Usuario
import com.example.linceops.ui.theme.LinceOPSTheme
import com.example.linceops.viewmodel.ServiceUiState

@Composable
fun DashboardScreen(
    rolUsuario: RolUsuario,
    uiState: ServiceUiState,
    onIniciarMarcaje: (String) -> Unit,
    onCrearServicio: () -> Unit = {}, // <- Nuevo parámetro para la navegación
    modifier: Modifier = Modifier
) {
    val nombreUsuario = uiState.usuarioLogueado?.nombre ?: "Usuario"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LINCEOPS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                AssistChip(
                    onClick = {},
                    label = {
                        Text("Rol: ${rolUsuario.nombreMostrar}")
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }

        item {
            Text(
                text = "Bienvenido/a, $nombreUsuario",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            when (rolUsuario) {
                RolUsuario.ADMINISTRADOR ->
                    DashboardManagerView(
                        uiState = uiState,
                        onCrearServicio = onCrearServicio // <- Pasamos la acción al manager
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
    }
}

@Composable
private fun DashboardManagerView(
    uiState: ServiceUiState,
    onCrearServicio: () -> Unit // <- Recibe la acción
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
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Panel de administración",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text("Servicios visibles: ${uiState.servicios.size}")
            Text("Servicios pendientes: $pendientes")

            Text(
                text = "Consulta el feed para revisar los servicios.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // <- NUEVO BOTÓN EXCLUSIVO DEL MANAGER
            Button(
                onClick = onCrearServicio,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Crear Nuevo Servicio")
            }
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
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Mis servicios",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text("Servicios visibles: ${uiState.servicios.size}")

            if (servicioMarcaje == null) {
                Text(
                    text = "No tienes servicios confirmados o en curso disponibles para marcaje."
                )
            } else {
                val estadoActual = usuario?.let {
                    servicioMarcaje.estadoPara(it)
                } ?: servicioMarcaje.estado

                Text(
                    text = if (estadoActual == EstadoServicio.EN_CURSO) {
                        "Servicio en curso"
                    } else {
                        "Próximo servicio"
                    },
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = servicioMarcaje.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Fecha: ${servicioMarcaje.fecha} | Hora: ${servicioMarcaje.hora}"
                )

                Text("Ruta: ${servicioMarcaje.origenDestino}")
                Text("Punto de encuentro: ${servicioMarcaje.pickup}")
                Text("Pasajeros: ${servicioMarcaje.pasajeros}")

                Button(
                    onClick = {
                        onIniciarMarcaje(servicioMarcaje.id)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Iniciar Marcaje")
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
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Panel del guía",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text("Servicios asignados: ${uiState.servicios.size}")

            if (servicio == null) {
                Text("No tienes servicios asignados.")
            } else {
                Text(
                    text = "Servicio: ${servicio.titulo}",
                    style = MaterialTheme.typography.titleMedium
                )

                Text("Fecha: ${servicio.fecha} | Hora: ${servicio.hora}")
                Text("Ruta: ${servicio.origenDestino}")
                Text("Punto de encuentro: ${servicio.pickup}")
                Text("Pasajeros: ${servicio.pasajeros}")
            }
        }
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