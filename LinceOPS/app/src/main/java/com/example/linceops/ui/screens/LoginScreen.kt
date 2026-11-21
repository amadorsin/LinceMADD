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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linceops.R
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
                    color = LinceOnPrimary,
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
                        mensajeErrorLocal = ""
                    },
                    label = { Text(stringResource(R.string.login_correo_usuario)) },
                    placeholder = { Text(stringResource(R.string.login_correo_placeholder)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = stringResource(R.string.login_usuario),
                            tint = LinceGreenPrimary
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
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

                if (errorVisible.isNotEmpty()) {
                    Surface(
                        color = StatusRejectedBg,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorVisible,
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
                val correoResuelto = resolverCorreo(usuario)
                if (correoResuelto.isBlank() || password.isBlank()) {
                    mensajeErrorLocal = errorCampos
                } else {
                    mensajeErrorLocal = ""
                    onLogin(correoResuelto, password)
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
                color = LinceOnPrimary,
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

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = stringResource(R.string.login_footer),
            style = MaterialTheme.typography.labelSmall,
            color = LinceTextSecondary
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