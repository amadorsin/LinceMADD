package com.example.linceops.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linceops.ui.components.CampoFecha
import com.example.linceops.ui.components.FilaUbicacion
import com.example.linceops.ui.components.PuntoMapa
import com.example.linceops.ui.components.SelectorHoraDialog
import com.example.linceops.ui.components.SelectorUbicacionDialog
import com.example.linceops.ui.components.distanciaKm
import com.example.linceops.ui.components.esFechaCompleta
import com.example.linceops.ui.theme.*
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearServicioScreen(
    onGuardar: (titulo: String, ruta: String, fecha: String, hora: String, tipo: String, pax: Int) -> Unit,
    onCancelar: () -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var origenNombre by remember { mutableStateOf("") }
    var destinoNombre by remember { mutableStateOf("") }
    var origenPunto by remember { mutableStateOf<PuntoMapa?>(null) }
    var destinoPunto by remember { mutableStateOf<PuntoMapa?>(null) }
    var selectorAbierto by remember { mutableStateOf<String?>(null) }
    var hora by remember { mutableStateOf("") }
    var pasajeros by remember { mutableStateOf(1) }
    var fechaSeleccionada by remember { mutableStateOf("") }
    var mostrarReloj by remember { mutableStateOf(false) }

    val ruta = listOf(origenNombre, destinoNombre)
        .filter { it.isNotBlank() }
        .joinToString(" -> ")

    val formularioValido = titulo.isNotBlank() &&
        fechaSeleccionada.isNotBlank() &&
        esFechaCompleta(fechaSeleccionada) &&
        origenNombre.isNotBlank() &&
        destinoNombre.isNotBlank() &&
        hora.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LinceCreamBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp)
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
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = LinceGreenPrimary
                            )
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SeccionFormulario(
                        icono = Icons.Default.LocationOn,
                        titulo = "Ruta del servicio"
                    )

                    HorizontalDivider(color = LinceOutlineVariant.copy(alpha = 0.5f))

                    FilaUbicacion(
                        etiqueta = "Origen",
                        valor = origenNombre,
                        esDestino = false,
                        onClick = { selectorAbierto = "origen" }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Conector vertical entre origen y destino
                        Box(
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .size(width = 3.dp, height = 18.dp)
                                .background(LinceOutlineVariant, RoundedCornerShape(50))
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = LinceGreenSageLight,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    val nombreTemporal = origenNombre
                                    origenNombre = destinoNombre
                                    destinoNombre = nombreTemporal
                                    val puntoTemporal = origenPunto
                                    origenPunto = destinoPunto
                                    destinoPunto = puntoTemporal
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Intercambiar origen y destino",
                                    tint = LinceGreenDark,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Intercambiar",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LinceGreenDark,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    FilaUbicacion(
                        etiqueta = "Destino",
                        valor = destinoNombre,
                        esDestino = true,
                        onClick = { selectorAbierto = "destino" }
                    )

                    val origen = origenPunto
                    val destino = destinoPunto
                    if (origen != null && destino != null) {
                        val kilometros = distanciaKm(origen, destino)
                        val minutos = ((kilometros / 45.0) * 60.0).roundToInt().coerceAtLeast(5)
                        Surface(
                            color = LinceSurfaceContainerLow,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = LinceTertiary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(7.dp))
                                Text(
                                    text = String.format(
                                        Locale.ROOT,
                                        "Recorrido aproximado: %.1f km · %d min",
                                        kilometros,
                                        minutos
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = LinceTextSecondary
                                )
                            }
                        }
                    }
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

                    CampoFecha(
                        valor = fechaSeleccionada,
                        onValorChange = { fechaSeleccionada = it },
                        etiqueta = "Fecha del servicio"
                    )

                    // Hora con reloj analógico
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { mostrarReloj = true },
                        shape = RoundedCornerShape(14.dp),
                        color = LinceSurfaceContainerLow,
                        border = BorderStroke(
                            1.dp,
                            if (hora.isBlank()) LinceOutlineVariant else LinceGreenPrimary
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconoReloj(
                                modifier = Modifier.size(20.dp),
                                color = LinceGreenPrimary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "HORA DE SALIDA",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LinceTextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                )
                                Text(
                                    text = if (hora.isBlank()) "Seleccionar hora" else hora,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (hora.isBlank()) LinceTextSecondary else LinceTextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Pasajeros con control de cantidad
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = LinceSurfaceContainerLow
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = LinceGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "PASAJEROS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LinceTextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                )
                                Text(
                                    text = if (pasajeros == 1) "1 pasajero" else "$pasajeros pasajeros",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = LinceTextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            BotonCantidad(texto = "−") {
                                if (pasajeros > 1) pasajeros--
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            BotonCantidad(texto = "+") {
                                if (pasajeros < 40) pasajeros++
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

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
                                pasajeros
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
                        enabled = formularioValido
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
    }

    when (selectorAbierto) {
        "origen" -> SelectorUbicacionDialog(
            titulo = "origen",
            puntoInicial = origenPunto,
            onDismiss = { selectorAbierto = null },
            onConfirmar = { punto, direccion ->
                origenPunto = punto
                origenNombre = direccion
                selectorAbierto = null
            }
        )

        "destino" -> SelectorUbicacionDialog(
            titulo = "destino",
            puntoInicial = destinoPunto,
            onDismiss = { selectorAbierto = null },
            onConfirmar = { punto, direccion ->
                destinoPunto = punto
                destinoNombre = direccion
                selectorAbierto = null
            }
        )
    }

    if (mostrarReloj) {
        SelectorHoraDialog(
            horaInicial = hora,
            onDismiss = { mostrarReloj = false },
            onConfirmar = { horaElegida ->
                hora = horaElegida
                mostrarReloj = false
            }
        )
    }
}

@Composable
private fun BotonCantidad(texto: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = LinceGreenSageLight
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = texto,
                style = MaterialTheme.typography.titleMedium,
                color = LinceGreenDark,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** Reloj pequeño dibujado para no depender de iconos extendidos. */
@Composable
private fun IconoReloj(modifier: Modifier = Modifier, color: Color = LinceGreenPrimary) {
    Canvas(modifier = modifier) {
        val radio = size.minDimension / 2f - 1.dp.toPx()
        val centro = Offset(size.width / 2f, size.height / 2f)
        drawCircle(
            color = color,
            radius = radio,
            center = centro,
            style = Stroke(width = 1.6.dp.toPx())
        )
        drawLine(
            color = color,
            start = centro,
            end = Offset(centro.x, centro.y - radio * 0.55f),
            strokeWidth = 1.6.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = centro,
            end = Offset(centro.x + radio * 0.42f, centro.y),
            strokeWidth = 1.6.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun SeccionFormulario(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
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
