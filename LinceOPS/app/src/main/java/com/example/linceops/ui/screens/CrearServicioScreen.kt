package com.example.linceops.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearServicioScreen(
    onGuardar: (titulo: String, ruta: String, fecha: String, hora: String, tipo: String, pax: Int) -> Unit,
    onCancelar: () -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var ruta by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var pasajeros by remember { mutableStateOf("1") }
    var fechaSeleccionada by remember { mutableStateOf("") }

    var mostrarDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Crear Nuevo Servicio", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = titulo, onValueChange = { titulo = it },
            label = { Text("Título del Servicio") }, modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = ruta, onValueChange = { ruta = it },
            label = { Text("Origen -> Destino") }, modifier = Modifier.fillMaxWidth()
        )

        // Campo de Fecha con DatePicker nativo
        OutlinedButton(
            onClick = { mostrarDatePicker = true },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Text(if (fechaSeleccionada.isEmpty()) "Seleccionar Fecha" else "Fecha: $fechaSeleccionada")
        }

        if (mostrarDatePicker) {
            DatePickerDialog(
                onDismissRequest = { mostrarDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT)
                            fechaSeleccionada = formatter.format(Date(millis))
                        }
                        mostrarDatePicker = false
                    }) { Text("Aceptar") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        OutlinedTextField(
            value = hora, onValueChange = { hora = it },
            label = { Text("Hora (HH:mm)") }, modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
            Button(
                onClick = { onGuardar(titulo, ruta, fechaSeleccionada, hora, "Transfer", pasajeros.toIntOrNull() ?: 1) },
                enabled = titulo.isNotBlank() && fechaSeleccionada.isNotBlank()
            ) {
                Text("Guardar Servicio")
            }
        }
    }
}
