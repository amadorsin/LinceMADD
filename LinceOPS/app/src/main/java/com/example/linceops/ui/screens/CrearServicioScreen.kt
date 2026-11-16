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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linceops.ui.components.CampoFecha
import com.example.linceops.ui.components.PuntoMapa
import com.example.linceops.ui.components.SelectorHoraDialog
import com.example.linceops.ui.components.SelectorUbicacionDialog
import com.example.linceops.ui.components.distanciaKm
import com.example.linceops.ui.components.esFechaCompleta
import com.example.linceops.ui.components.textoCoordenadas
import com.example.linceops.ui.theme.*
import java.util.Locale
import kotlin.math.roundToInt

private enum class PosicionRuta { ORIGEN, PARADA, DESTINO }

@Composable
private fun RutaItem(
    posicion: PosicionRuta,
    etiqueta: String,
    valor: String,
    punto: PuntoMapa?,
    onClick: () -> Unit,
    numero: Int? = null,
    onEliminar: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top
    ) {
        Canvas(
            modifier = Modifier
                .width(26.dp)
                .fillMaxHeight()
        ) {
            val cx = size.width / 2f
            val cy = 26.dp.toPx()
            val radio = 6.dp.toPx()
            val colorLinea = LinceOutlineVariant

            if (posicion != PosicionRuta.ORIGEN) {
                drawLine(
                    color = colorLinea,
                    start = Offset(cx, 0f),
                    end = Offset(cx, cy - radio - 3.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
            }
            if (posicion != PosicionRuta.DESTINO) {
                drawLine(
                    color = colorLinea,
                    start = Offset(cx, cy + radio + 3.dp.toPx()),
                    end = Offset(cx, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }

            when (posicion) {
                PosicionRuta.ORIGEN -> drawCircle(
                    color = LinceGreenPrimary,
                    radius = radio,
                    center = Offset(cx, cy)
                )

                PosicionRuta.DESTINO -> drawRect(
                    color = LinceTertiary,
                    topLeft = Offset(cx - radio, cy - radio),
                    size = Size(radio * 2f, radio * 2f)
                )

                PosicionRuta.PARADA -> {
                    drawCircle(
                        color = LinceCardSurface,
                        radius = radio,
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = LinceGreenDark,
                        radius = radio,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    val texto = numero?.toString().orEmpty()
                    if (texto.isNotEmpty()) {
                        val pintura = android.graphics.Paint().apply {
                            isAntiAlias = true
                            color = LinceGreenDark.toArgb()
                            textSize = 7.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                        }
                        drawContext.canvas.nativeCanvas.drawText(
                            texto,
                            cx,
                            cy + pintura.textSize / 3f,
                            pintura
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Surface(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            color = LinceSurfaceContainerLow,
            border = BorderStroke(1.dp, LinceOutlineVariant.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = etiqueta.uppercase(Locale.ROOT),
                        style = MaterialTheme.typography.labelSmall,
                        color = LinceTextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = valor.ifBlank { "Elegir en el mapa o escribir" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (valor.isBlank()) LinceTextSecondary else LinceTextPrimary,
                        fontWeight = if (valor.isBlank()) FontWeight.Normal else FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    punto?.let {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = textoCoordenadas(it),
                            style = MaterialTheme.typography.labelSmall,
                            color = LinceTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (onEliminar != null) {
                    IconButton(
                        onClick = onEliminar,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Eliminar $etiqueta",
                            tint = LinceTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Text(
                        text = if (valor.isBlank()) "Elegir" else "Cambiar",
                        style = MaterialTheme.typography.labelMedium,
                        color = LinceGreenPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

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
    val paradasNombres = remember { mutableStateListOf<String>() }
    val paradasPuntos = remember { mutableStateListOf<PuntoMapa?>() }
    var selectorAbierto by remember { mutableStateOf<String?>(null) }
    var hora by remember { mutableStateOf("") }
    var pasajeros by remember { mutableStateOf(1) }
    var fechaSeleccionada by remember { mutableStateOf("") }
    var mostrarReloj by remember { mutableStateOf(false) }
    var vehiculoContingencia by remember { mutableStateOf("") }
    var motivoContingencia by remember { mutableStateOf("") }


    val ruta = buildList {
        if (origenNombre.isNotBlank()) add(origenNombre)
        paradasNombres.filter { it.isNotBlank() }.forEach { add(it) }
        if (destinoNombre.isNotBlank()) add(destinoNombre)
    }.joinToString(" -> ")

    fun agregarParada() {
        if (paradasNombres.size < 4) {
            paradasNombres.add("")
            paradasPuntos.add(null)
            selectorAbierto = "parada:${paradasNombres.lastIndex}"
        }
    }

    fun eliminarParada(indice: Int) {
        if (indice in paradasNombres.indices) {
            paradasNombres.removeAt(indice)
            paradasPuntos.removeAt(indice)
        }
    }

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
                text = "CONTINGENCIA OPERATIVA",
                style = MaterialTheme.typography.labelSmall,
                color = LinceTertiary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Asignación Local de Recursos",
                style = MaterialTheme.typography.headlineSmall,
                color = LinceGreenPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Usa este formulario solo cuando Tourplan no esté disponible o requieras despachar una unidad de respaldo.",
                style = MaterialTheme.typography.bodySmall,
                color = LinceTextSecondary
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
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
                        .padding(18.dp),
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
                        label = { Text("Referencia / Título del Servicio") },
                        placeholder = { Text("Ej. Traslado Contingencia - Vuelo LA402") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors()
                    )

                    OutlinedTextField(
                        value = vehiculoContingencia,
                        onValueChange = { vehiculoContingencia = it },
                        label = { Text("Unidad / Patente Asignada") },
                        placeholder = { Text("Ej. Sprinter ABCD-12") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors()
                    )

                    OutlinedTextField(
                        value = motivoContingencia,
                        onValueChange = { motivoContingencia = it },
                        label = { Text("Observación de Contingencia (Opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors()
                    )
                }
            }

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
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SeccionFormulario(
                        icono = Icons.Default.LocationOn,
                        titulo = "Ruta del servicio"
                    )

                    HorizontalDivider(color = LinceOutlineVariant.copy(alpha = 0.5f))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        RutaItem(
                            posicion = PosicionRuta.ORIGEN,
                            etiqueta = "Origen",
                            valor = origenNombre,
                            punto = origenPunto,
                            onClick = { selectorAbierto = "origen" }
                        )

                        paradasNombres.forEachIndexed { indice, nombre ->
                            RutaItem(
                                posicion = PosicionRuta.PARADA,
                                numero = indice + 1,
                                etiqueta = "Parada ${indice + 1}",
                                valor = nombre,
                                punto = paradasPuntos.getOrNull(indice),
                                onClick = { selectorAbierto = "parada:$indice" },
                                onEliminar = { eliminarParada(indice) }
                            )
                        }

                        RutaItem(
                            posicion = PosicionRuta.DESTINO,
                            etiqueta = "Destino",
                            valor = destinoNombre,
                            punto = destinoPunto,
                            onClick = { selectorAbierto = "destino" }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { agregarParada() },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, LinceGreenPrimary),
                            enabled = paradasNombres.size < 4
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = LinceGreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Añadir parada (${paradasNombres.size}/4)",
                                style = MaterialTheme.typography.labelMedium,
                                color = LinceGreenPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = LinceGreenSageLight,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
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
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Intercambiar origen y destino",
                                    tint = LinceGreenDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Intercambiar",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LinceGreenDark,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    val puntosRuta = buildList {
                        origenPunto?.let { add(it) }
                        paradasPuntos.forEach { punto -> punto?.let { add(it) } }
                        destinoPunto?.let { add(it) }
                    }
                    if (puntosRuta.size >= 2) {
                        val kilometros = puntosRuta.zipWithNext().sumOf { (desde, hasta) ->
                            distanciaKm(desde, hasta)
                        }
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
                                        "Recorrido aproximado: %.1f km · %d min · %d parada(s)",
                                        kilometros,
                                        minutos,
                                        paradasNombres.count { it.isNotBlank() }
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = LinceTextSecondary
                                )
                            }
                        }
                    }
                }
            }

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
                        .padding(18.dp),
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

            Spacer(modifier = Modifier.height(36.dp))
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
                            val tituloFinal = if (vehiculoContingencia.isNotBlank()) {
                                "$titulo [${vehiculoContingencia.uppercase()}]"
                            } else {
                                titulo
                            }
                            onGuardar(
                                tituloFinal,
                                ruta,
                                fechaSeleccionada,
                                hora,
                                "Contingencia Local",
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
                        enabled = formularioValido && vehiculoContingencia.isNotBlank()
                    ) {
                        Text(
                            text = "Asignar Recurso",
                            color = LinceCardSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    when {
        selectorAbierto == "origen" -> SelectorUbicacionDialog(
            titulo = "origen",
            puntoInicial = origenPunto ?: destinoPunto,
            onDismiss = { selectorAbierto = null },
            onConfirmar = { punto, direccion ->
                origenPunto = punto
                origenNombre = direccion
                selectorAbierto = null
            }
        )

        selectorAbierto == "destino" -> SelectorUbicacionDialog(
            titulo = "destino",
            puntoInicial = destinoPunto ?: origenPunto,
            onDismiss = { selectorAbierto = null },
            onConfirmar = { punto, direccion ->
                destinoPunto = punto
                destinoNombre = direccion
                selectorAbierto = null
            }
        )

        selectorAbierto?.startsWith("parada:") == true -> {
            val indice = selectorAbierto
                ?.substringAfter("parada:")
                ?.toIntOrNull()
            if (indice != null && indice in paradasNombres.indices) {
                SelectorUbicacionDialog(
                    titulo = "parada ${indice + 1}",
                    puntoInicial = paradasPuntos.getOrNull(indice)
                        ?: origenPunto
                        ?: destinoPunto,
                    onDismiss = { selectorAbierto = null },
                    onConfirmar = { punto, direccion ->
                        if (indice in paradasNombres.indices) {
                            paradasNombres[indice] = direccion
                            paradasPuntos[indice] = punto
                        }
                        selectorAbierto = null
                    }
                )
            }
        }
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
