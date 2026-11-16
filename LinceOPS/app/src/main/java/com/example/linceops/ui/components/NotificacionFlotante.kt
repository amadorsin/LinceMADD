package com.example.linceops.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.linceops.ui.theme.LinceCardSurface
import com.example.linceops.ui.theme.LinceDangerRed
import com.example.linceops.ui.theme.LinceGreenPrimary
import com.example.linceops.ui.theme.LinceGreenSageLight
import com.example.linceops.ui.theme.LinceTextPrimary
import com.example.linceops.ui.theme.StatusRejectedBg

@Composable
fun NotificacionFlotante(
    texto: String,
    esError: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colorAcento = if (esError) LinceDangerRed else LinceGreenPrimary
    val fondoIcono = if (esError) StatusRejectedBg else LinceGreenSageLight

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = LinceCardSurface,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, colorAcento.copy(alpha = 0.32f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(30.dp),
                shape = CircleShape,
                color = fondoIcono
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (esError) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = colorAcento,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyMedium,
                color = LinceTextPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
