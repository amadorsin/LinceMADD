package com.example.linceops.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linceops.ui.theme.*
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LinceCreamBackground)
    ) {
        // Encabezado de la pantalla
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 14.dp)
        ) {
            Text(
                text = "MÓDULO DE GESTIÓN",
                style = MaterialTheme.typography.labelSmall,
                color = LinceTextSecondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Crear Nuevo Servicio",
                style = MaterialTheme.typography.headlineSmall,
                color = LinceGreenPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Completa los datos operativos para programar el servicio.",
                style = MaterialTheme.typography.bodySmall,
                color = LinceTextSecondary
            )
        }

        // Formulario desplazable
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = LinceCardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, LinceOutlineVariant.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SeccionFormulario(
                        icono = Icons.Default.Info,
                        titulo = "Datos del servicio"
                    )

                    HorizontalDivider(color = LinceOutlineVariant.copy(alpha = 0.5f))

                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { titulo = it },
                        label = { Text("Título del Servicio") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = LinceGreenPrimary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors()
                    )

                    OutlinedTextField(
                        value = ruta,
                        onValueChange = { ruta = it },
                        label = { Text("Origen -> Destino") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = LinceGreenPrimary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = LinceCardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, LinceOutlineVariant.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SeccionFormulario(
                        icono = Icons.Default.DateRange,
                        titulo = "Programación y operación"
                    )

                    HorizontalDivider(color = LinceOutlineVariant.copy(alpha = 0.5f))

                    // Selector de fecha en una sola línea
                    OutlinedButton(
                        onClick = { mostrarDatePicker = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            1.dp,
                            if (fechaSeleccionada.isEmpty()) LinceOutlineVariant else LinceGreenPrimary
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = LinceTextPrimary,
                            containerColor = LinceSurfaceContainerLow
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Calendario",
                                tint = LinceGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "FECHA DEL SERVICIO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LinceTextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = if (fechaSeleccionada.isEmpty()) "Seleccionar fecha" else fechaSeleccionada,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (fechaSeleccionada.isEmpty()) LinceTextSecondary else LinceTextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "dd/MM/yyyy",
                                style = MaterialTheme.typography.labelSmall,
                                color = LinceTextSecondary,
                                maxLines = 1
                            )
                        }
                    }

                    OutlinedTextField(
                        value = hora,
                        onValueChange = { hora = it },
                        label = { Text("Hora (HH:mm)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.DateRange, contentDescription = null, tint = LinceGreenPrimary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors()
                    )

                    OutlinedTextField(
                        value = pasajeros,
                        onValueChange = { pasajeros = it },
                        label = { Text("Cantidad de Pasajeros") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = LinceGreenPrimary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors()
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // Barra de acciones inferior
        Surface(
            color = LinceCardSurface,
            shadowElevation = 10.dp
        ) {
            Column {
                HorizontalDivider(color = LinceOutlineVariant.copy(alpha = 0.6f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onCancelar,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, LinceGreenPrimary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LinceGreenPrimary)
                    ) {
                        Text("Cancelar", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            onGuardar(
                                titulo,
                                ruta,
                                fechaSeleccionada,
                                hora,
                                "Transfer",
                                pasajeros.toIntOrNull() ?: 1
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LinceGreenPrimary,
                            disabledContainerColor = LinceGreenPrimary.copy(alpha = 0.4f)
                        ),
                        enabled = titulo.isNotBlank() && fechaSeleccionada.isNotBlank()
                    ) {
                        Text(
                            text = "Guardar Servicio",
                            color = LinceCardSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
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
                    }) {
                        Text("Aceptar", color = LinceGreenPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDatePicker = false }) {
                        Text("Cancelar", color = LinceTextSecondary)
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
private fun SeccionFormulario(
    icono: ImageVector,
    titulo: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(28.dp),
            shape = RoundedCornerShape(9.dp),
            color = LinceGreenSageLight
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = LinceGreenPrimary,
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
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = LinceGreenPrimary,
    unfocusedBorderColor = LinceOutlineVariant,
    focusedContainerColor = LinceCardSurface,
    unfocusedContainerColor = LinceSurfaceContainerLow,
    focusedLabelColor = LinceGreenPrimary
)
