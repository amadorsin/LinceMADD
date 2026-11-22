package com.example.linceops.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linceops.R
import com.example.linceops.ui.components.LinceButton
import com.example.linceops.ui.components.LinceButtonVariant
import com.example.linceops.ui.components.LinceSurfaceCard
import com.example.linceops.ui.components.LinceSurfaceCardVariant
import com.example.linceops.ui.theme.*

@Composable
fun LoginScreen(
    onLogin: (correo: String, password: String) -> Unit,
    errorLogin: String? = null,
    modifier: Modifier = Modifier
) {
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mensajeErrorLocal by remember { mutableStateOf("") }

    val errorVisible = mensajeErrorLocal.ifBlank { errorLogin.orEmpty() }
    val errorCampos = stringResource(R.string.login_error_campos)

    val topographicColor = LinceTheme.colors.greenPrimary.copy(alpha = 0.055f)
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinceTheme.colors.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LinceSpacing.medium)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(174.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(width = 174.dp, height = 150.dp)
            ) {
                for (index in 0..5) {
                    drawArc(
                        color = topographicColor,
                        startAngle = 190f + index * 4f,
                        sweepAngle = 145f,
                        useCenter = false,
                        topLeft = Offset(index * 5f, index * 7f),
                        size = androidx.compose.ui.geometry.Size(
                            size.width - index * 10f,
                            size.height - index * 12f
                        ),
                        style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LinceSpacing.xSmall)
            ) {
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = RoundedCornerShape(LinceRadius.xLarge),
                    color = LinceTheme.colors.greenPrimary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "L",
                            style = MaterialTheme.typography.headlineMedium,
                            color = LinceTheme.colors.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "LINCE OPS",
                    style = MaterialTheme.typography.headlineMedium,
                    color = LinceTheme.colors.greenPrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = stringResource(R.string.login_operaciones_terreno),
                    style = MaterialTheme.typography.labelSmall,
                    color = LinceTheme.colors.textSecondary,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            }
        }

        LinceSurfaceCard(
            variant = LinceSurfaceCardVariant.Default,
            contentPadding = LinceSpacing.large
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(LinceSpacing.medium)
            ) {
                Text(
                    text = stringResource(R.string.login_acceso_operativo),
                    style = MaterialTheme.typography.titleSmall,
                    color = LinceTheme.colors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = usuario,
                    onValueChange = {
                        usuario = it
                        mensajeErrorLocal = ""
                    },
                    label = { Text(stringResource(R.string.login_correo_usuario)) },
                    placeholder = { Text(stringResource(R.string.login_correo_placeholder)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = stringResource(R.string.login_usuario),
                            tint = LinceTheme.colors.greenPrimary
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(LinceRadius.medium),
                    colors = loginFieldColors()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        mensajeErrorLocal = ""
                    },
                    label = { Text(stringResource(R.string.login_contrasena)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = stringResource(R.string.login_contrasena),
                            tint = LinceTheme.colors.greenPrimary
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(LinceRadius.medium),
                    colors = loginFieldColors()
                )
                if (errorVisible.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = LinceTheme.colors.errorContainer,
                        shape = RoundedCornerShape(LinceRadius.small)
                    ) {
                        Text(
                            text = errorVisible,
                            color = LinceTheme.colors.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.padding(LinceSpacing.small)
                        )
                    }
                }
                LinceButton(
                    text = stringResource(R.string.login_ingresar),
                    onClick = {
                        val correoResuelto = resolverCorreo(usuario)
                        if (correoResuelto.isBlank() || password.isBlank()) {
                            mensajeErrorLocal = errorCampos
                        } else {
                            mensajeErrorLocal = ""
                            onLogin(correoResuelto, password)
                        }
                    },
                    variant = LinceButtonVariant.Primary,
                    fullWidth = true
                )
            }
        }

        LinceSurfaceCard(
            variant = LinceSurfaceCardVariant.Subtle,
            contentPadding = LinceSpacing.medium
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LinceSpacing.small)
            ) {
                Text(
                    text = stringResource(R.string.login_atajos),
                    style = MaterialTheme.typography.labelMedium,
                    color = LinceTheme.colors.textSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LinceSpacing.xSmall)
                ) {
                    AtajoRol(
                        texto = stringResource(R.string.login_atajo_manager),
                        modifier = Modifier.weight(1f),
                        onClick = { onLogin("admin@linceops.cl", "123456") }
                    )
                    AtajoRol(
                        texto = stringResource(R.string.login_atajo_driver),
                        modifier = Modifier.weight(1f),
                        onClick = { onLogin("driver@linceops.cl", "123456") }
                    )
                    AtajoRol(
                        texto = stringResource(R.string.login_atajo_guia),
                        modifier = Modifier.weight(1f),
                        onClick = { onLogin("guia@linceops.cl", "123456") }
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.login_footer),
            style = MaterialTheme.typography.labelSmall,
            color = LinceTheme.colors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

private fun resolverCorreo(entrada: String): String {
    return when (entrada.trim().lowercase()) {
        "admin", "manager", "administrador" -> "admin@linceops.cl"
        "driver", "conductor" -> "driver@linceops.cl"
        "guia", "guía" -> "guia@linceops.cl"
        else -> entrada.trim()
    }
}

@Composable
private fun AtajoRol(
    texto: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(40.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, LinceGreenBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = LinceGreenPrimary,
            containerColor = LinceCardSurface
        ),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun loginFieldColors() = OutlinedTextFieldDefaults.colors(
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
