package com.example.linceops.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linceops.R
import com.example.linceops.model.RolUsuario
import com.example.linceops.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: (RolUsuario) -> Unit,
    modifier: Modifier = Modifier
) {
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinceCreamBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Surface(
            modifier = Modifier.size(68.dp),
            shape = RoundedCornerShape(20.dp),
            color = LinceGreenPrimary,
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "L",
                    style = MaterialTheme.typography.headlineMedium,
                    color = LinceCardSurface,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "LINCE OPS",
            style = MaterialTheme.typography.headlineMedium,
            color = LinceGreenPrimary,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = stringResource(R.string.login_operaciones_terreno),
            style = MaterialTheme.typography.labelSmall,
            color = LinceTextSecondary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = LinceCardSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = BorderStroke(1.dp, LinceOutlineVariant.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.login_acceso_operativo),
                    style = MaterialTheme.typography.labelSmall,
                    color = LinceTextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                OutlinedTextField(
                    value = usuario,
                    onValueChange = {
                        usuario = it
                        mensajeError = ""
                    },
                    label = { Text(stringResource(R.string.login_usuario)) },
                    placeholder = { Text(stringResource(R.string.login_usuario_placeholder)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = stringResource(R.string.login_usuario),
                            tint = LinceGreenPrimary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = loginFieldColors()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        mensajeError = ""
                    },
                    label = { Text(stringResource(R.string.login_contrasena)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = stringResource(R.string.login_contrasena),
                            tint = LinceGreenPrimary
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = loginFieldColors()
                )

                if (mensajeError.isNotEmpty()) {
                    Surface(
                        color = StatusRejectedBg,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = mensajeError,
                            color = StatusRejectedText,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                when (usuario.lowercase().trim()) {
                    "admin", "manager" -> onLoginSuccess(RolUsuario.ADMINISTRADOR)
                    "driver", "conductor" -> onLoginSuccess(RolUsuario.CONDUCTOR)
                    "guia" -> onLoginSuccess(RolUsuario.GUIA)
                    else -> {
                        if (usuario.isEmpty() || password.isEmpty()) {
                            mensajeError = context.getString(R.string.login_error_credenciales)
                        } else {
                            mensajeError = context.getString(R.string.login_error_usuario)
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LinceGreenPrimary)
        ) {
            Text(
                text = stringResource(R.string.login_ingresar),
                style = MaterialTheme.typography.titleMedium,
                color = LinceCardSurface,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = LinceSurfaceVariant.copy(alpha = 0.55f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, LinceOutlineVariant.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.login_atajos),
                    style = MaterialTheme.typography.labelSmall,
                    color = LinceTextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AtajoRol(
                        texto = stringResource(R.string.rol_administrador),
                        modifier = Modifier.weight(1f),
                        onClick = { onLoginSuccess(RolUsuario.ADMINISTRADOR) }
                    )
                    AtajoRol(
                        texto = stringResource(R.string.rol_conductor),
                        modifier = Modifier.weight(1f),
                        onClick = { onLoginSuccess(RolUsuario.CONDUCTOR) }
                    )
                    AtajoRol(
                        texto = stringResource(R.string.rol_guia),
                        modifier = Modifier.weight(1f),
                        onClick = { onLoginSuccess(RolUsuario.GUIA) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = stringResource(R.string.login_footer),
            style = MaterialTheme.typography.labelSmall,
            color = LinceTextSecondary
        )
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
    focusedLabelColor = LinceGreenPrimary
)
