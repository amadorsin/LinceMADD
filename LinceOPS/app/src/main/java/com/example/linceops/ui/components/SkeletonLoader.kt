package com.example.linceops.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.linceops.ui.theme.LinceCardSurface
import com.example.linceops.ui.theme.LinceCreamBackground
import com.example.linceops.ui.theme.LinceOutlineVariant
import com.example.linceops.ui.theme.LinceTheme

fun Modifier.shimmerEffect(shape: Shape = RoundedCornerShape(12.dp)): Modifier = composed {
    val colores = LinceTheme.colors
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = -400f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1050, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerPulse"
    )
    val brush = Brush.linearGradient(
        colors = listOf(
            colores.shimmerBase,
            colores.shimmerHighlight,
            colores.shimmerBase
        ),
        start = Offset(translateAnim - 260f, translateAnim - 260f),
        end = Offset(translateAnim, translateAnim)
    )
    this
        .clip(shape)
        .background(brush, shape)
        .graphicsLayer { alpha = pulseAlpha }
}

@Composable
fun DashboardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinceCreamBackground)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(14.dp)
                        .shimmerEffect(RoundedCornerShape(6.dp))
                )
                Box(
                    modifier = Modifier
                        .width(190.dp)
                        .height(12.dp)
                        .shimmerEffect(RoundedCornerShape(6.dp))
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .height(30.dp)
                        .shimmerEffect(RoundedCornerShape(50))
                )
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shimmerEffect(CircleShape)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .width(210.dp)
                    .height(24.dp)
                    .shimmerEffect(RoundedCornerShape(8.dp))
            )
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(14.dp)
                    .shimmerEffect(RoundedCornerShape(6.dp))
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = LinceCardSurface),
            border = BorderStroke(1.dp, LinceOutlineVariant.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .width(150.dp)
                            .height(18.dp)
                            .shimmerEffect(RoundedCornerShape(6.dp))
                    )
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .height(22.dp)
                            .shimmerEffect(RoundedCornerShape(50))
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(96.dp)
                            .shimmerEffect(RoundedCornerShape(14.dp))
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(96.dp)
                            .shimmerEffect(RoundedCornerShape(14.dp))
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(74.dp)
                        .shimmerEffect(RoundedCornerShape(14.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .shimmerEffect(RoundedCornerShape(14.dp))
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(4) {
                Box(
                    modifier = Modifier
                        .width(78.dp)
                        .height(32.dp)
                        .shimmerEffect(RoundedCornerShape(50))
                )
            }
        }

        repeat(2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .shimmerEffect(RoundedCornerShape(14.dp))
            )
        }
    }
}

@Composable
fun ServiciosListSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinceCreamBackground)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(12.dp)
                    .shimmerEffect(RoundedCornerShape(6.dp))
            )
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(24.dp)
                    .shimmerEffect(RoundedCornerShape(8.dp))
            )
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .height(24.dp)
                    .shimmerEffect(RoundedCornerShape(50))
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = LinceCardSurface),
            border = BorderStroke(1.dp, LinceOutlineVariant.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(4) {
                        Box(
                            modifier = Modifier
                                .width(76.dp)
                                .height(32.dp)
                                .shimmerEffect(RoundedCornerShape(50))
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shimmerEffect(RoundedCornerShape(14.dp))
                )
            }
        }

        repeat(3) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LinceCardSurface),
                border = BorderStroke(1.dp, LinceOutlineVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .width(58.dp)
                                    .height(26.dp)
                                    .shimmerEffect(RoundedCornerShape(8.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .width(64.dp)
                                    .height(26.dp)
                                    .shimmerEffect(RoundedCornerShape(8.dp))
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(92.dp)
                                .height(26.dp)
                                .shimmerEffect(RoundedCornerShape(50))
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(20.dp)
                            .shimmerEffect(RoundedCornerShape(6.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .shimmerEffect(RoundedCornerShape(12.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .shimmerEffect(RoundedCornerShape(10.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun DetalleSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinceCreamBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(12.dp)
                    .shimmerEffect(RoundedCornerShape(6.dp))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(26.dp)
                    .shimmerEffect(RoundedCornerShape(8.dp))
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LinceCardSurface),
            border = BorderStroke(1.dp, LinceOutlineVariant.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(18.dp)
                        .shimmerEffect(RoundedCornerShape(6.dp))
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .shimmerEffect(RoundedCornerShape(12.dp))
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .shimmerEffect(RoundedCornerShape(12.dp))
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .shimmerEffect(RoundedCornerShape(12.dp))
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LinceCardSurface),
            border = BorderStroke(1.dp, LinceOutlineVariant.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(170.dp)
                        .height(18.dp)
                        .shimmerEffect(RoundedCornerShape(6.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .shimmerEffect(RoundedCornerShape(14.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shimmerEffect(RoundedCornerShape(12.dp))
                )
            }
        }
    }
}

@Composable
fun MarcajeSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinceCreamBackground)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .width(140.dp)
                .height(12.dp)
                .shimmerEffect(RoundedCornerShape(6.dp))
        )
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(24.dp)
                .shimmerEffect(RoundedCornerShape(8.dp))
        )
        Box(
            modifier = Modifier
                .width(110.dp)
                .height(24.dp)
                .shimmerEffect(RoundedCornerShape(50))
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = LinceCardSurface),
            border = BorderStroke(1.dp, LinceOutlineVariant.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(175.dp)
                        .shimmerEffect(RoundedCornerShape(16.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .shimmerEffect(RoundedCornerShape(12.dp))
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .shimmerEffect(RoundedCornerShape(12.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .shimmerEffect(RoundedCornerShape(12.dp))
        )
    }
}

@Composable
fun PerfilSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinceCreamBackground)
            .padding(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .height(12.dp)
                    .shimmerEffect(RoundedCornerShape(6.dp))
            )
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(24.dp)
                    .shimmerEffect(RoundedCornerShape(8.dp))
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = LinceCardSurface),
            border = BorderStroke(1.dp, LinceOutlineVariant.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .shimmerEffect(CircleShape)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(18.dp)
                            .shimmerEffect(RoundedCornerShape(6.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(14.dp)
                            .shimmerEffect(RoundedCornerShape(6.dp))
                    )
                }
            }
        }

        repeat(2) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LinceCardSurface),
                border = BorderStroke(1.dp, LinceOutlineVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(160.dp)
                            .height(18.dp)
                            .shimmerEffect(RoundedCornerShape(6.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shimmerEffect(RoundedCornerShape(12.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shimmerEffect(RoundedCornerShape(12.dp))
                    )
                }
            }
        }
    }
}
