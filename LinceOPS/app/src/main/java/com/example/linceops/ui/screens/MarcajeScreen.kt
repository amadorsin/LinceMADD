package com.example.linceops.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun MarcajeScreen(
    servicioId: String,
    onCheckIn: (lat: Double, lon: Double) -> Unit,
    onCheckOut: (lat: Double, lon: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    // Estado local para simular la vista del pin estático
    var mensajeEstado by remember { mutableStateOf("Esperando inicio de ruta...") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Control de Ruta",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón Check-In
        Button(
            onClick = {
                mensajeEstado = "Check-In registrado con éxito.\nLat: -41.3194, Lon: -72.9831"
                onCheckIn(-41.3194, -72.9831)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp) // Botones anchos para uso en terreno
        ) {
            Text("Registrar Check-In", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Check-Out
        FilledTonalButton(
            onClick = {
                mensajeEstado = "Check-Out finalizado.\nLat: -41.4693, Lon: -72.9423"
                onCheckOut(-41.4693, -72.9423)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Text("Registrar Check-Out", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Feedback visual para el usuario
        Text(
            text = mensajeEstado,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}