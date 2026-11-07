package com.example.linceops.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.linceops.ui.theme.LinceGlassBorder
import com.example.linceops.ui.theme.LinceGreenDark
import com.example.linceops.ui.theme.LinceGreenSageLight
import com.example.linceops.ui.theme.LinceOutlineVariant
import com.example.linceops.ui.theme.LinceTextSecondary
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeChild

private data class ItemBarra(
    val id: String,
    val icono: ImageVector,
    val etiqueta: String
)

private val ITEMS_BARRA = listOf(
    ItemBarra("inicio", Icons.Filled.Home, "Inicio"),
    ItemBarra("servicios", Icons.AutoMirrored.Filled.List, "Servicios"),
    ItemBarra("perfil", Icons.Filled.Person, "Perfil")
)

/**
 * Barra inferior flotante con efecto vidrio esmerilado (blur real sin franjas sólidas).
 * Se oculta al hacer scroll o deslizar hacia abajo y reaparece al subir.
 */
@Composable
fun BarraInferiorGlass(
    rutaActual: String?,
    onNavegar: (String) -> Unit,
    visible: Boolean,
    onVisibleChange: (Boolean) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val densidad = LocalDensity.current
    val umbral = with(densidad) { 24.dp.toPx() }
    val alturaTotal = with(densidad) { 104.dp.toPx() }
    var acumulado by remember { mutableStateOf(0f) }

    val progreso by animateFloatAsState(
        targetValue = if (visible) 0f else 1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f),
        label = "barraInferior"
    )

    val formaPildora = RoundedCornerShape(32.dp)

    Box(modifier = modifier.fillMaxWidth()) {
        // Zona de gesto inferior para volver a mostrar la barra cuando está oculta
        if (!visible) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(64.dp)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = { acumulado = 0f },
                            onVerticalDrag = { cambio, arrastre ->
                                acumulado += arrastre
                                cambio.consume()
                            },
                            onDragEnd = {
                                if (acumulado < -umbral) onVisibleChange(true)
                                acumulado = 0f
                            },
                            onDragCancel = { acumulado = 0f }
                        )
                    }
            )

            // Indicador sutil de que la barra está oculta
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(LinceOutlineVariant.copy(alpha = 0.55f), RoundedCornerShape(50))
            )
        }

        if (progreso < 1f) {
            // Contenedor único sin Surface ni shadowElevation nativo que genere bandas sólidas
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 18.dp)
                    .fillMaxWidth()
                    .height(64.dp)
                    .graphicsLayer {
                        translationY = progreso * alturaTotal
                        alpha = 1f - (progreso * 0.55f)
                        scaleX = 1f - (progreso * 0.08f)
                        scaleY = 1f - (progreso * 0.08f)
                    }
                    .clip(formaPildora)
                    .hazeChild(
                        state = hazeState,
                        shape = formaPildora,
                        style = HazeStyle(
                            tint = Color(0xFFFDFBF7).copy(alpha = 0.52f),
                            blurRadius = 26.dp,
                            noiseFactor = 0.04f
                        )
                    )
                    .background(
                        color = Color(0xFFFDFBF7).copy(alpha = 0.18f),
                        shape = formaPildora
                    )
                    .border(
                        width = 1.dp,
                        color = LinceGlassBorder.copy(alpha = 0.85f),
                        shape = formaPildora
                    )
                    .pointerInput(visible) {
                        detectVerticalDragGestures(
                            onDragStart = { acumulado = 0f },
                            onVerticalDrag = { cambio, arrastre ->
                                acumulado += arrastre
                                cambio.consume()
                            },
                            onDragEnd = {
                                if (acumulado > umbral) onVisibleChange(false)
                                acumulado = 0f
                            },
                            onDragCancel = { acumulado = 0f }
                        )
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ITEMS_BARRA.forEach { item ->
                        val seleccionado = cuandoSeleccionado(rutaActual, item.id)
                        val interactionSource = remember { MutableInteractionSource() }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) { onNavegar(item.id) }
                                .semantics { contentDescription = item.etiqueta },
                            contentAlignment = Alignment.Center
                        ) {
                            if (seleccionado) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(LinceGreenSageLight.copy(alpha = 0.88f))
                                )
                            }
                            Icon(
                                imageVector = item.icono,
                                contentDescription = null,
                                tint = if (seleccionado) LinceGreenDark else LinceTextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun cuandoSeleccionado(rutaActual: String?, id: String): Boolean {
    val base = rutaActual?.substringBefore("/") ?: return false
    return when (id) {
        "inicio" -> base == "dashboard"
        "servicios" -> base == "feed"
        "perfil" -> base == "perfil"
        else -> false
    }
}