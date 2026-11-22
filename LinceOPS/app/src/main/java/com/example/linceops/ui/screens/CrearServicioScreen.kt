package com.example.linceops.ui.screens

import com.example.linceops.model.TipoServicio
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linceops.R
import com.example.linceops.ui.components.CampoFecha
import com.example.linceops.ui.components.LinceButton
import com.example.linceops.ui.components.LinceButtonVariant
import com.example.linceops.ui.components.LinceDivider
import com.example.linceops.ui.components.LinceSectionHeader
import com.example.linceops.ui.components.LinceStatusBadge
import com.example.linceops.ui.components.LinceStatusTone
import com.example.linceops.ui.components.LinceSurfaceCard
import com.example.linceops.ui.components.LinceSurfaceCardVariant
import com.example.linceops.ui.components.PuntoMapa
import com.example.linceops.ui.components.SelectorHoraDialog
import com.example.linceops.ui.components.SelectorUbicacionDialog
import com.example.linceops.ui.components.distanciaKm
import com.example.linceops.ui.components.esFechaCompleta
import com.example.linceops.ui.components.textoCoordenadas
import com.example.linceops.ui.theme.*
import java.util.Locale
import kotlin.math.roundToInt
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.example.linceops.network.dto.GuiaDto
import com.example.linceops.network.dto.TransportistaDto

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
    val coloresTema = LinceTheme.colors
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
            val colorLinea = coloresTema.outlineVariant

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
                    color = coloresTema.greenPrimary,
                    radius = radio,
                    center = Offset(cx, cy)
                )

                PosicionRuta.DESTINO -> drawRect(
                    color = coloresTema.tertiary,
                    topLeft = Offset(cx - radio, cy - radio),
                    size = Size(radio * 2f, radio * 2f)
                )

                PosicionRuta.PARADA -> {
                    drawCircle(
                        color = coloresTema.cardSurface,
                        radius = radio,
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = coloresTema.greenDark,
                        radius = radio,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    val texto = numero?.toString().orEmpty()
                    if (texto.isNotEmpty()) {
                        val pintura = android.graphics.Paint().apply {
                            isAntiAlias = true
                            color = coloresTema.greenDark.toArgb()
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

        Row(
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = 56.dp)
                .clip(RoundedCornerShape(LinceRadius.medium))
                .clickable(onClick = onClick)
                .padding(
                    start = LinceSpacing.small,
                    end = LinceSpacing.small,
                    top = LinceSpacing.xSmall,
                    bottom = LinceSpacing.xSmall
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = etiqueta.uppercase(Locale.ROOT),
                    style = MaterialTheme.typography.labelSmall,
                    color = LinceTheme.colors.textSecondary,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.6.sp
                )
                Text(
                    text = valor.ifBlank { stringResource(R.string.crear_elegir_mapa) },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (valor.isBlank()) {
                        LinceTheme.colors.textSecondary
                    } else {
                        LinceTheme.colors.textPrimary
                    },
                    fontWeight = if (valor.isBlank()) FontWeight.Normal else FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                punto?.let {
                    Text(
                        text = textoCoordenadas(it),
                        style = MaterialTheme.typography.labelSmall,
                        color = LinceTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(LinceSpacing.small))
            if (onEliminar != null) {
                IconButton(
                    onClick = onEliminar,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.crear_eliminar, etiqueta),
                        tint = LinceTheme.colors.goldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Text(
                    text = if (valor.isBlank()) {
                        stringResource(R.string.crear_elegir)
                    } else {
                        stringResource(R.string.crear_cambiar)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = LinceTheme.colors.greenPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearServicioScreen(
    guias: List<GuiaDto> = emptyList(),
    transportistas: List<TransportistaDto> = emptyList(),
    onGuardar: (
        titulo: String,
        ruta: String,
        fecha: String,
        hora: String,
        tipo: String,
        pax: Int,
        pickup: String,
        dropoff: String,
        vehiculo: String,
        conductorId: String?,
        guiaId: String?,
        observaciones: String,
        confirmarDirecto: Boolean
    ) -> Unit,
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
    var tipoSeleccionado by remember { mutableStateOf(TipoServicio.TRANSFER) }
    var motivoContingencia by remember { mutableStateOf("") }
    val context = LocalContext.current
    var conductorIdSeleccionado by remember { mutableStateOf<String?>("20") }
    var guiaIdSeleccionado by remember { mutableStateOf<String?>("12") }
    var confirmarDirecto by remember { mutableStateOf(true) }

    val opcionesConductores = remember(transportistas) {
        buildList {
            add("20" to "Matías Soto Herrera (Demo)")
            transportistas.forEach { t ->
                val id = t.id?.toString()
                val nombre = t.nombre
                if (!id.isNullOrBlank() && !nombre.isNullOrBlank() && id != "20") {
                    add(id to nombre)
                }
            }
        }
    }
    val opcionesGuias = remember(guias) {
        buildList {
            add("12" to "Camila Rojas Fuentes (Demo)")
            guias.forEach { g ->
                val id = g.id?.toString()
                val nombre = g.nombre
                if (!id.isNullOrBlank() && !nombre.isNullOrBlank() && id != "12") {
                    add(id to nombre)
                }
            }
        }
    }

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
            .background(LinceTheme.colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = LinceSpacing.large,
                    end = LinceSpacing.large,
                    top = LinceSpacing.medium,
                    bottom = LinceSpacing.small
                )
        ) {
            Text(
                text = stringResource(R.string.crear_contingencia),
                style = MaterialTheme.typography.labelSmall,
                color = LinceTheme.colors.goldAccent,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp
            )
            LinceSectionHeader(
                title = stringResource(R.string.crear_asignacion_local),
                subtitle = stringResource(R.string.crear_contingencia_desc),
                action = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = LinceTheme.colors.greenPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = LinceSpacing.large, end = LinceSpacing.large),
            verticalArrangement = Arrangement.spacedBy(LinceSpacing.medium)
        ) {
            LinceSurfaceCard(
                variant = LinceSurfaceCardVariant.Default,
                contentPadding = LinceSpacing.medium
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(LinceSpacing.small)
                ) {
                    SeccionFormulario(
                        icono = Icons.Default.Info,
                        titulo = stringResource(R.string.crear_datos)
                    )

                    LinceDivider()
                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { titulo = it },
                        label = { Text(stringResource(R.string.crear_referencia_titulo)) },
                        placeholder = { Text(stringResource(R.string.crear_titulo_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors()
                    )

                    Text(
                        text = stringResource(R.string.crear_tipo_servicio),
                        style = MaterialTheme.typography.labelSmall,
                        color = LinceTextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TipoServicio.entries.forEach { tipo ->
                            FilterChip(
                                selected = tipoSeleccionado == tipo,
                                onClick = { tipoSeleccionado = tipo },
                                label = { Text(nombreTipo(context, tipo), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.weight(1f),
                                colors = filterChipColors()
                            )
                        }
                    }
                }
            }

            LinceSurfaceCard(
                variant = LinceSurfaceCardVariant.Default,
                contentPadding = LinceSpacing.medium
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(LinceSpacing.small)
                ) {
                    SeccionFormulario(
                        icono = Icons.Default.LocationOn,
                        titulo = stringResource(R.string.crear_ruta)
                    )

                    LinceDivider()

                    Column(modifier = Modifier.fillMaxWidth()) {
                        RutaItem(
                            posicion = PosicionRuta.ORIGEN,
                            etiqueta = stringResource(R.string.crear_origen),
                            valor = origenNombre,
                            punto = origenPunto,
                            onClick = { selectorAbierto = "origen" }
                        )

                        paradasNombres.forEachIndexed { indice, nombre ->
                            RutaItem(
                                posicion = PosicionRuta.PARADA,
                                numero = indice + 1,
                                etiqueta = stringResource(R.string.crear_parada, indice + 1),
                                valor = nombre,
                                punto = paradasPuntos.getOrNull(indice),
                                onClick = { selectorAbierto = "parada:$indice" },
                                onEliminar = { eliminarParada(indice) }
                            )
                        }

                        RutaItem(
                            posicion = PosicionRuta.DESTINO,
                            etiqueta = stringResource(R.string.crear_destino),
                            valor = destinoNombre,
                            punto = destinoPunto,
                            onClick = { selectorAbierto = "destino" }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(LinceSpacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { agregarParada() },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .defaultMinSize(minHeight = 48.dp),
                            shape = RoundedCornerShape(LinceRadius.medium),
                            border = BorderStroke(1.dp, LinceTheme.colors.greenPrimary),
                            enabled = paradasNombres.size < 4,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = LinceTheme.colors.greenPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = LinceTheme.colors.greenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(LinceSpacing.xSmall))
                            Text(
                                text = stringResource(R.string.crear_anadir_parada, paradasNombres.size),
                                style = MaterialTheme.typography.labelSmall,
                                color = LinceTheme.colors.greenPrimary,
                                fontWeight = FontWeight.SemiBold,
                                softWrap = true,
                                maxLines = 2,
                                textAlign = TextAlign.Center,
                                overflow = TextOverflow.Clip
                            )
                        }

                        LinceButton(
                            text = stringResource(R.string.crear_intercambiar),
                            onClick = {
                                val nombreTemporal = origenNombre
                                origenNombre = destinoNombre
                                destinoNombre = nombreTemporal
                                val puntoTemporal = origenPunto
                                origenPunto = destinoPunto
                                destinoPunto = puntoTemporal
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            icon = Icons.Default.Refresh,
                            variant = LinceButtonVariant.Secondary
                        )
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
                        LinceStatusBadge(
                            text = context.getString(
                                R.string.crear_recorrido,
                                kilometros,
                                minutos,
                                paradasNombres.count { it.isNotBlank() }
                            ),
                            tone = LinceStatusTone.Info,
                            icon = Icons.Default.LocationOn
                        )
                    }
                }
            }

            LinceSurfaceCard(
                variant = LinceSurfaceCardVariant.Default,
                contentPadding = LinceSpacing.medium
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(LinceSpacing.small)
                ) {
                    SeccionFormulario(
                        icono = Icons.Default.DateRange,
                        titulo = stringResource(R.string.crear_programacion)
                    )

                    LinceDivider()

                    CampoFecha(
                        valor = fechaSeleccionada,
                        onValorChange = { fechaSeleccionada = it },
                        etiqueta = stringResource(R.string.crear_fecha_servicio)
                    )

                    LinceDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(LinceRadius.medium))
                            .clickable { mostrarReloj = true }
                            .padding(
                                start = LinceSpacing.small,
                                end = LinceSpacing.small,
                                top = LinceSpacing.small,
                                bottom = LinceSpacing.small
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconoReloj(
                            modifier = Modifier.size(20.dp),
                            color = LinceTheme.colors.greenPrimary
                        )
                        Spacer(modifier = Modifier.width(LinceSpacing.small))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.crear_hora_salida),
                                style = MaterialTheme.typography.labelSmall,
                                color = LinceTheme.colors.textSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (hora.isBlank()) stringResource(R.string.crear_seleccionar_hora) else hora,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (hora.isBlank()) {
                                    LinceTheme.colors.textSecondary
                                } else {
                                    LinceTheme.colors.textPrimary
                                },
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    LinceDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(LinceSpacing.small)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = LinceTheme.colors.greenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.crear_pasajeros),
                                style = MaterialTheme.typography.labelSmall,
                                color = LinceTheme.colors.textSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (pasajeros == 1) {
                                    stringResource(R.string.crear_pasajero_uno)
                                } else {
                                    stringResource(R.string.crear_pasajeros_varios, pasajeros)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = LinceTheme.colors.textPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        BotonCantidad(texto = "−") {
                            if (pasajeros > 1) pasajeros--
                        }
                        BotonCantidad(texto = "+") {
                            if (pasajeros < 40) pasajeros++
                        }
                    }
                }
            }

            LinceSurfaceCard(
                variant = LinceSurfaceCardVariant.Default,
                contentPadding = LinceSpacing.medium
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(LinceSpacing.small)
                ) {
                    SeccionFormulario(
                        icono = Icons.Default.Person,
                        titulo = stringResource(R.string.crear_asignar_recurso)
                    )
                    LinceDivider()
                    Text(
                        text = "CONDUCTOR ASIGNADO",
                        style = MaterialTheme.typography.labelSmall,
                        color = LinceTheme.colors.textSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    )
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = conductorIdSeleccionado == null,
                                onClick = { conductorIdSeleccionado = null },
                                label = { Text("Sin conductor") },
                                shape = RoundedCornerShape(50),
                                colors = filterChipColors()
                            )
                        }
                        items(opcionesConductores) { (id, nombre) ->
                            FilterChip(
                                selected = conductorIdSeleccionado == id,
                                onClick = { conductorIdSeleccionado = id },
                                label = { Text(nombre, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                shape = RoundedCornerShape(50),
                                colors = filterChipColors()
                            )
                        }
                    }

                    Text(
                        text = "GUÍA ASIGNADO",
                        style = MaterialTheme.typography.labelSmall,
                        color = LinceTheme.colors.textSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    )
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = guiaIdSeleccionado == null,
                                onClick = { guiaIdSeleccionado = null },
                                label = { Text("Sin guía") },
                                shape = RoundedCornerShape(50),
                                colors = filterChipColors()
                            )
                        }
                        items(opcionesGuias) { (id, nombre) ->
                            FilterChip(
                                selected = guiaIdSeleccionado == id,
                                onClick = { guiaIdSeleccionado = id },
                                label = { Text(nombre, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                shape = RoundedCornerShape(50),
                                colors = filterChipColors()
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Confirmar inmediatamente",
                                style = MaterialTheme.typography.bodyMedium,
                                color = LinceTheme.colors.textPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Queda listo para marcaje (si no, queda Pendiente)",
                                style = MaterialTheme.typography.labelSmall,
                                color = LinceTheme.colors.textSecondary
                            )
                        }
                        Switch(
                            checked = confirmarDirecto,
                            onCheckedChange = { confirmarDirecto = it }
                        )
                    }
                    OutlinedTextField(
                        value = vehiculoContingencia,
                        onValueChange = { vehiculoContingencia = it },
                        label = { Text(stringResource(R.string.crear_unidad_patente)) },
                        placeholder = { Text(stringResource(R.string.crear_unidad_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(LinceRadius.medium),
                        colors = fieldColors()
                    )
                    OutlinedTextField(
                        value = motivoContingencia,
                        onValueChange = { motivoContingencia = it },
                        label = { Text(stringResource(R.string.crear_observacion_contingencia)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(LinceRadius.medium),
                        colors = fieldColors()
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }

        Surface(color = LinceTheme.colors.cardSurface) {
            Column {
                LinceDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = LinceSpacing.medium,
                            end = LinceSpacing.medium,
                            top = LinceSpacing.small,
                            bottom = LinceSpacing.small
                        ),
                    horizontalArrangement = Arrangement.spacedBy(LinceSpacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinceButton(
                        text = stringResource(R.string.accion_cancelar),
                        onClick = onCancelar,
                        modifier = Modifier.weight(1f),
                        variant = LinceButtonVariant.Outline
                    )
                    LinceButton(
                        text = stringResource(R.string.crear_asignar_recurso),
                        onClick = {
                            onGuardar(
                                titulo.trim(),
                                ruta,
                                fechaSeleccionada,
                                hora,
                                tipoSeleccionado.name,
                                pasajeros,
                                origenNombre.trim(),
                                destinoNombre.trim(),
                                vehiculoContingencia.trim().ifBlank { "Unidad asignada" },
                                conductorIdSeleccionado,
                                guiaIdSeleccionado,
                                motivoContingencia.trim(),
                                confirmarDirecto
                            )
                        },
                        modifier = Modifier.weight(1f),
                        variant = LinceButtonVariant.Primary,
                        enabled = formularioValido,
                        fullWidth = false
                    )
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
    LinceSectionHeader(
        title = titulo,
        action = {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = LinceTheme.colors.greenPrimary,
                modifier = Modifier.size(19.dp)
            )
        }
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = LinceGreenPrimary,
    unfocusedBorderColor = LinceOutlineVariant,
    focusedContainerColor = LinceCardSurface,
    unfocusedContainerColor = LinceSurfaceContainerLow,
    focusedLabelColor = LinceGreenPrimary,
    unfocusedLabelColor = LinceTextSecondary,
    focusedTextColor = LinceTextPrimary,
    unfocusedTextColor = LinceTextPrimary,
    cursorColor = LinceGreenPrimary
)
