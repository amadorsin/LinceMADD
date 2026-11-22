package com.example.linceops.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linceops.ui.theme.LinceRadius
import com.example.linceops.ui.theme.LinceSpacing
import com.example.linceops.ui.theme.LinceTheme

enum class LinceSurfaceCardVariant {
    Default,
    Highlighted,
    Subtle
}

enum class LinceButtonVariant {
    Primary,
    Secondary,
    Outline,
    Ghost,
    Destructive
}

enum class LinceStatusTone {
    Success,
    Warning,
    Error,
    Info,
    Neutral
}

@Composable
fun LinceSurfaceCard(
    modifier: Modifier = Modifier,
    variant: LinceSurfaceCardVariant = LinceSurfaceCardVariant.Default,
    contentPadding: Dp = LinceSpacing.medium,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LinceTheme.colors
    val containerColor = when (variant) {
        LinceSurfaceCardVariant.Default -> colors.cardSurface
        LinceSurfaceCardVariant.Highlighted -> colors.greenSageLight
        LinceSurfaceCardVariant.Subtle -> colors.surfaceContainerLow
    }
    val borderColor = when (variant) {
        LinceSurfaceCardVariant.Highlighted -> colors.greenBorder
        else -> colors.outlineVariant.copy(alpha = 0.7f)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
        shape = RoundedCornerShape(LinceRadius.large),
        border = BorderStroke(0.75.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun LinceKpiCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    supportingText: String? = null,
    accentColor: Color = LinceTheme.colors.greenPrimary
) {
    LinceSurfaceCard(
        modifier = modifier.fillMaxHeight(),
        variant = LinceSurfaceCardVariant.Default,
        contentPadding = LinceSpacing.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = LinceTheme.colors.textSecondary,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.displaySmall,
                    color = LinceTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                supportingText?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = LinceTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            icon?.let {
                Surface(
                    shape = RoundedCornerShape(LinceRadius.small),
                    color = accentColor.copy(alpha = if (LinceTheme.colors.isDark) 0.16f else 0.1f)
                ) {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.padding(LinceSpacing.small).size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LinceButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: LinceButtonVariant = LinceButtonVariant.Primary,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    fullWidth: Boolean = false
) {
    val colors = LinceTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.98f else 1f,
        animationSpec = tween(durationMillis = 135),
        label = "linceButtonScale"
    )
    val (container, content, border) = when (variant) {
        LinceButtonVariant.Primary -> Triple(colors.greenPrimary, colors.onPrimary, null)
        LinceButtonVariant.Secondary -> Triple(colors.greenSageLight, colors.greenDark, colors.greenBorder)
        LinceButtonVariant.Outline -> Triple(colors.cardSurface.copy(alpha = 0f), colors.greenPrimary, colors.greenBorder)
        LinceButtonVariant.Ghost -> Triple(colors.cardSurface.copy(alpha = 0f), colors.greenPrimary, null)
        LinceButtonVariant.Destructive -> Triple(colors.error, colors.onPrimary, null)
    }
    val shape = RoundedCornerShape(LinceRadius.medium)

    Row(
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .defaultMinSize(minHeight = 48.dp)
            .clip(shape)
            .background(if (enabled) container else container.copy(alpha = 0.5f))
            .then(
                if (border != null) {
                    Modifier.border(BorderStroke(0.75.dp, border), shape)
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(LinceSpacing.small))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = content,
            fontWeight = FontWeight.SemiBold,
            softWrap = true,
            maxLines = 2,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Clip
        )
    }
}

@Composable
fun LinceStatusBadge(
    text: String,
    tone: LinceStatusTone,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val colors = LinceTheme.colors
    val (container, content) = when (tone) {
        LinceStatusTone.Success -> colors.successContainer to colors.success
        LinceStatusTone.Warning -> colors.warningContainer to colors.warning
        LinceStatusTone.Error -> colors.errorContainer to colors.error
        LinceStatusTone.Info -> colors.infoContainer to colors.info
        LinceStatusTone.Neutral -> colors.neutralContainer to colors.neutral
    }

    Surface(
        modifier = modifier,
        color = container,
        shape = RoundedCornerShape(50),
        border = BorderStroke(0.75.dp, content.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(
                start = LinceSpacing.small,
                end = LinceSpacing.small,
                top = LinceSpacing.xSmall,
                bottom = LinceSpacing.xSmall
            ),
            horizontalArrangement = Arrangement.spacedBy(LinceSpacing.xSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = content,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = content,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun LinceSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(LinceSpacing.xSmall)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = LinceTheme.colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
                softWrap = true,
                maxLines = 2,
                overflow = TextOverflow.Clip
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = LinceTheme.colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        action?.let {
            Spacer(modifier = Modifier.width(LinceSpacing.medium))
            it()
        }
    }
}

@Composable
fun LinceUserAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    contentDescription: String? = null
) {
    Surface(
        modifier = modifier
            .size(size)
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                }
            ),
        shape = CircleShape,
        color = LinceTheme.colors.greenSageLight,
        border = BorderStroke(0.75.dp, LinceTheme.colors.greenBorder)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = initials.trim().take(3).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = LinceTheme.colors.greenDark,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
fun LinceEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Rounded.Info
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = LinceSpacing.xLarge,
                end = LinceSpacing.xLarge,
                top = LinceSpacing.xxLarge,
                bottom = LinceSpacing.xxLarge
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LinceSpacing.small)
    ) {
        Surface(
            shape = CircleShape,
            color = LinceTheme.colors.greenSageLight
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = LinceTheme.colors.greenPrimary,
                modifier = Modifier.padding(LinceSpacing.medium).size(24.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = LinceTheme.colors.textPrimary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = LinceTheme.colors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LinceDivider(modifier: Modifier = Modifier) {
    androidx.compose.material3.HorizontalDivider(
        modifier = modifier.fillMaxWidth(),
        thickness = 0.75.dp,
        color = LinceTheme.colors.outlineVariant.copy(alpha = 0.75f)
    )
}
