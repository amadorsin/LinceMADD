package com.example.linceops.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow

@Composable
fun EfectoScrollBarraInferior(
    listState: LazyListState,
    onVisibleChange: (Boolean) -> Unit
) {
    var posicionAnterior by remember(listState) { mutableIntStateOf(0) }

    LaunchedEffect(listState) {
        snapshotFlow {
            Triple(
                listState.isScrollInProgress,
                listState.firstVisibleItemIndex,
                listState.firstVisibleItemScrollOffset
            )
        }.collect { (desplazando, indice, desplazamiento) ->
            val posicion = indice * 100_000 + desplazamiento

            when {
                posicion <= 0 -> {
                    posicionAnterior = 0
                    onVisibleChange(true)
                }

                desplazando && posicion > posicionAnterior + 8 -> {
                    posicionAnterior = posicion
                    onVisibleChange(false)
                }

                desplazando && posicion < posicionAnterior - 8 -> {
                    posicionAnterior = posicion
                    onVisibleChange(true)
                }

                desplazando -> posicionAnterior = posicion
            }
        }
    }
}
