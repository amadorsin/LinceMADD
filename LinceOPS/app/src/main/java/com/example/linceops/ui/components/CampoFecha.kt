package com.example.linceops.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.linceops.ui.theme.LinceCardSurface
import com.example.linceops.ui.theme.LinceGreenPrimary
import com.example.linceops.ui.theme.LinceOutlineVariant
import com.example.linceops.ui.theme.LinceSurfaceContainerLow
import com.example.linceops.ui.theme.LinceTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val FORMATO_FECHA = "dd/MM/yyyy"
private val ZONA_UTC: TimeZone = TimeZone.getTimeZone("UTC")

/**
 * Da formato dd/MM/yyyy mientras el usuario escribe: solo acepta dígitos y
 * agrega las barras automáticamente (2 y 4 dígitos).
 */
fun formatearFechaEntrada(entrada: String): String {
    val digitos = entrada.filter { it.isDigit() }.take(8)
    val constructor = StringBuilder()
    digitos.forEachIndexed { indice, digito ->
        if (indice == 2 || indice == 4) constructor.append('/')
        constructor.append(digito)
    }
    return constructor.toString()
}

/** Comprueba que el texto sea una fecha real con formato dd/MM/yyyy. */
fun esFechaCompleta(texto: String): Boolean {
    if (!Regex("\\d{2}/\\d{2}/\\d{4}").matches(texto)) return false
    return runCatching {
        SimpleDateFormat(FORMATO_FECHA, Locale.ROOT)
            .apply { isLenient = false }
            .parse(texto) != null
    }.getOrDefault(false)
}

/** Convierte una fecha dd/MM/yyyy a milisegundos para el DatePicker. */
private fun fechaAMillis(fecha: String): Long? = runCatching {
    SimpleDateFormat(FORMATO_FECHA, Locale.ROOT)
        .apply {
            isLenient = false
            // El DatePicker trabaja en UTC: evitamos que la fecha se corra un día
            // en zonas horarias negativas como la de Chile.
            timeZone = ZONA_UTC
        }
        .parse(fecha)
        ?.time
}.getOrNull()

/**
 * Campo de fecha con barras automáticas y calendario integrado.
 * Al elegir un día en el calendario, el valor se escribe con formato dd/MM/yyyy.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoFecha(
    valor: String,
    onValorChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    etiqueta: String = "Fecha",
    placeholder: String = "dd/MM/yyyy",
    habilitado: Boolean = true
) {
    var campo by remember { mutableStateOf(TextFieldValue(valor, TextRange(valor.length))) }
    var mostrarCalendario by remember { mutableStateOf(false) }

    // Mantiene sincronizado el texto cuando el valor llega desde fuera
    LaunchedEffect(valor) {
        if (valor != campo.text) {
            campo = TextFieldValue(valor, TextRange(valor.length))
        }
    }

    val estadoCalendario = rememberDatePickerState(
        initialSelectedDateMillis = fechaAMillis(valor)
    )

    OutlinedTextField(
        value = campo,
        onValueChange = { nuevo ->
            val formateado = formatearFechaEntrada(nuevo.text)
            campo = TextFieldValue(formateado, TextRange(formateado.length))
            onValorChange(formateado)
        },
        modifier = modifier.fillMaxWidth(),
        enabled = habilitado,
        singleLine = true,
        label = { Text(etiqueta) },
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        shape = RoundedCornerShape(14.dp),
        textStyle = MaterialTheme.typography.bodyMedium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LinceGreenPrimary,
            unfocusedBorderColor = LinceOutlineVariant,
            focusedContainerColor = LinceCardSurface,
            unfocusedContainerColor = LinceSurfaceContainerLow,
            focusedLabelColor = LinceGreenPrimary
        ),
        trailingIcon = {
            Row {
                if (valor.isNotEmpty()) {
                    IconButton(onClick = { onValorChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Borrar fecha",
                            tint = LinceTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                IconButton(onClick = { mostrarCalendario = true }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Abrir calendario",
                        tint = LinceGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    )

    if (mostrarCalendario) {
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(onClick = {
                    estadoCalendario.selectedDateMillis?.let { millis ->
                        val formateada = SimpleDateFormat(FORMATO_FECHA, Locale.ROOT)
                            .apply { timeZone = ZONA_UTC }
                            .format(Date(millis))
                        onValorChange(formateada)
                        campo = TextFieldValue(formateada, TextRange(formateada.length))
                    }
                    mostrarCalendario = false
                }) {
                    Text("Aceptar", color = LinceGreenPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) {
                    Text("Cancelar", color = LinceTextSecondary)
                }
            }
        ) {
            Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                DatePicker(state = estadoCalendario)
            }
        }
    }
}
