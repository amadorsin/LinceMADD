package com.example.linceops.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linceops.R
import com.example.linceops.ui.theme.LinceRadius
import com.example.linceops.ui.theme.LinceSpacing
import com.example.linceops.ui.theme.LinceTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeChild

private data class ItemBarra(
    val id: String,
    val icono: ImageVector,
    @StringRes val etiquetaRes: Int
)

private val ITEMS_BARRA = listOf(
    ItemBarra("inicio", Icons.Rounded.Home, R.string.nav_inicio),
    ItemBarra("servicios", Icons.AutoMirrored.Rounded.List, R.string.nav_servicios),
    ItemBarra("perfil", Icons.Rounded.Person, R.string.nav_perfil)
)

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
    val alturaTotal = with(densidad) { 96.dp.toPx() }
    var acumulado by remember { mutableStateOf(0f) }
    val colores = LinceTheme.colors

    val progreso by animateFloatAsState(
        targetValue = if (visible) 0f else 1f,
        animationSpec = spring(dampingRatio = 0.86f, stiffness = 420f),
        label = "barraInferior"
    )

    val formaBarra = RoundedCornerShape(LinceRadius.xLarge)

    Box(modifier = modifier.fillMaxWidth()) {
        if (!visible) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(60.dp)
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
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = LinceSpacing.small)
                    .size(width = 36.dp, height = 4.dp)
                    .background(
                        colores.outlineVariant.copy(alpha = 0.75f),
                        RoundedCornerShape(50)
                    )
            )
        }

        if (progreso < 1f) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = LinceSpacing.medium)
                    .padding(bottom = LinceSpacing.medium)
                    .fillMaxWidth()
                    .height(76.dp)
                    .graphicsLayer {
                        translationY = progreso * alturaTotal
                        alpha = 1f - (progreso * 0.55f)
                        scaleX = 1f - (progreso * 0.035f)
                        scaleY = 1f - (progreso * 0.035f)
                    }
                    .clip(formaBarra)
                    .hazeChild(
                        state = hazeState,
                        shape = formaBarra,
                        style = HazeStyle(
                            tint = colores.glassSurface.copy(
                                alpha = if (colores.isDark) 0.86f else 0.68f
                            ),
                            blurRadius = 20.dp,
                            noiseFactor = 0.025f
                        )
                    )
                    .background(
                        color = colores.glassSurface.copy(
                            alpha = if (colores.isDark) 0.78f else 0.72f
                        ),
                        shape = formaBarra
                    )
                    .border(
                        width = 0.75.dp,
                        color = colores.glassBorder.copy(alpha = 0.88f),
                        shape = formaBarra
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
                        .height(76.dp)
                        .padding(horizontal = LinceSpacing.small, vertical = LinceSpacing.small),
                    horizontalArrangement = Arrangement.spacedBy(LinceSpacing.xSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ITEMS_BARRA.forEach { item ->
                        val seleccionado = cuandoSeleccionado(rutaActual, item.id)
                        val interactionSource = remember { MutableInteractionSource() }
                        val etiquetaItem = stringResource(item.etiquetaRes)

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(LinceRadius.medium))
                                .background(
                                    if (seleccionado) {
                                        colores.greenSageLight.copy(
                                            alpha = if (colores.isDark) 0.92f else 0.96f
                                        )
                                    } else {
                                        colores.glassSurface.copy(alpha = 0f)
                                    }
                                )
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    role = Role.Tab
                                ) { onNavegar(item.id) }
                                .semantics { contentDescription = etiquetaItem }
                                .padding(horizontal = LinceSpacing.small, vertical = LinceSpacing.xSmall),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = item.icono,
                                contentDescription = null,
                                tint = if (seleccionado) colores.greenDark else colores.textSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = etiquetaItem,
                                color = if (seleccionado) colores.greenDark else colores.textSecondary,
                                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                fontSize = 12.sp,
                                maxLines = 1
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
