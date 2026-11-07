package com.example.linceops.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.linceops.ui.theme.LinceCardSurface
import com.example.linceops.ui.theme.LinceCreamBackground
import com.example.linceops.ui.theme.LinceGreenBorder
import com.example.linceops.ui.theme.LinceGreenDark
import com.example.linceops.ui.theme.LinceGreenPrimary
import com.example.linceops.ui.theme.LinceGreenSageLight
import com.example.linceops.ui.theme.LinceTextPrimary
import com.example.linceops.ui.theme.LinceTextSecondary
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

/**
 * Selector de hora estilo alarma de teléfono con ruedas verticales deslizables (Wheel Picker).
 * Permite hacer scroll suave en horas (00..23) y minutos (00..59) con snap automático al centro.
 */
@Composable
fun SelectorHoraDialog(
    horaInicial: String,
    onDismiss: () -> Unit,
    onConfirmar: (String) -> Unit
) {
    val (horaBase, minutoBase) = remember(horaInicial) {
        val partes = horaInicial.split(":")
        val h = partes.getOrNull(0)?.toIntOrNull()
        val m = partes.getOrNull(1)?.toIntOrNull()
        if (h != null && m != null && h in 0..23 && m in 0..59) {
            h to m
        } else {
            val cal = Calendar.getInstance()
            cal.get(Calendar.HOUR_OF_DAY) to cal.get(Calendar.MINUTE)
        }
    }

    var horaSeleccionada by remember { mutableIntStateOf(horaBase) }
    var minutoSeleccionado by remember { mutableIntStateOf(minutoBase) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = true)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = LinceCardSurface,
            shadowElevation = 18.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "HORA DE SALIDA",
                    style = MaterialTheme.typography.labelSmall,
                    color = LinceTextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Desliza hacia arriba o abajo para ajustar",
                    style = MaterialTheme.typography.bodySmall,
                    color = LinceTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Contenedor de las dos ruedas (Hora : Minuto)
                val itemHeight = 56.dp
                val wheelHeight = itemHeight * 3 // 3 filas visibles (anterior, actual, siguiente)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(wheelHeight)
                        .background(
                            color = LinceCreamBackground.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Banda central que destaca el valor activo
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(itemHeight),
                        shape = RoundedCornerShape(14.dp),
                        color = LinceGreenSageLight.copy(alpha = 0.75f),
                        border = BorderStroke(1.dp, LinceGreenPrimary.copy(alpha = 0.35f))
                    ) {}

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rueda de Horas (00 - 23)
                        RuedaNumericaInfinita(
                            rango = 0..23,
                            valorInicial = horaBase,
                            alturaItem = itemHeight,
                            onValorCambio = { horaSeleccionada = it }
                        )

                        Text(
                            text = ":",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = LinceGreenDark,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        // Rueda de Minutos (00 - 59)
                        RuedaNumericaInfinita(
                            rango = 0..59,
                            valorInicial = minutoBase,
                            alturaItem = itemHeight,
                            onValorCambio = { minutoSeleccionado = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, LinceGreenBorder)
                    ) {
                        Text(
                            text = "Cancelar",
                            color = LinceTextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            onConfirmar(
                                String.format(
                                    Locale.ROOT,
                                    "%02d:%02d",
                                    horaSeleccionada,
                                    minutoSeleccionado
                                )
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LinceGreenPrimary)
                    ) {
                        Text(
                            text = "Aceptar",
                            color = LinceCardSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RuedaNumericaInfinita(
    rango: IntRange,
    valorInicial: Int,
    alturaItem: Dp,
    onValorCambio: (Int) -> Unit
) {
    val cantidadValores = rango.count()
    val repeticiones = 200
    val totalElementos = cantidadValores * repeticiones
    val mitad = (totalElementos / 2) - ((totalElementos / 2) % cantidadValores)
    val indiceInicial = mitad + (valorInicial - rango.first)

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = indiceInicial)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val densidad = LocalDensity.current
    val alturaItemPx = with(densidad) { alturaItem.toPx() }

    // Calcula cuál es el ítem que está en el centro exacto mientras se desliza la rueda
    val indiceCentral by remember {
        derivedStateOf {
            val offsetFraccion = if (alturaItemPx > 0f) {
                (listState.firstVisibleItemScrollOffset / alturaItemPx)
            } else {
                0f
            }
            listState.firstVisibleItemIndex + if (offsetFraccion > 0.5f) 1 else 0
        }
    }

    LaunchedEffect(indiceCentral) {
        val valorActual = rango.first + ((indiceCentral % cantidadValores + cantidadValores) % cantidadValores)
        onValorCambio(valorActual)
    }

    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        contentPadding = PaddingValues(vertical = alturaItem), // Permite centrar el primer y último elemento
        modifier = Modifier
            .width(92.dp)
            .height(alturaItem * 3),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(totalElementos) { index ->
            val valor = rango.first + (index % cantidadValores)
            val distanciaAlCentro = abs(index - indiceCentral)
            val esSeleccionado = distanciaAlCentro == 0

            Box(
                modifier = Modifier
                    .height(alturaItem)
                    .fillMaxWidth()
                    .alpha(if (esSeleccionado) 1f else 0.35f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format(Locale.ROOT, "%02d", valor),
                    fontSize = if (esSeleccionado) 34.sp else 24.sp,
                    fontWeight = if (esSeleccionado) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (esSeleccionado) LinceGreenDark else LinceTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}