package com.example.linceops.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.linceops.model.RolUsuario
import com.example.linceops.model.Servicio

@Composable
fun DetalleScreen(
    servicio: Servicio,
    rolUsuario: RolUsuario,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit,
    onReasignar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Final Update",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Datos ficticios de momento
                Text("Destino: ${servicio.origenDestino}", style = MaterialTheme.typography.titleMedium)
                Text("Fecha y Hora: 26 Oct 2026 - 08:00 AM")
                Text("Pasajeros: 4 (Origen: Alemania)")
                Text("Idiomas requeridos: Inglés, Alemán")
                Text("Extras: Incluye almuerzo en ruta")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // botones segun roles
        when (rolUsuario) {
            RolUsuario.CONDUCTOR, RolUsuario.GUIA -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onRechazar,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Rechazar")
                    }
                    Button(
                        onClick = onAceptar,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Confirmar")
                    }
                }
            }
            RolUsuario.ADMINISTRADOR -> {
                FilledTonalButton(
                    onClick = onReasignar,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reasignar Recurso")
                }
            }
        }
    }
}