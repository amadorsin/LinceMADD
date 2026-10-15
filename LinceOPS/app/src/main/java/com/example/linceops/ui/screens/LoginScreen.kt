package com.example.linceops.ui.screens

import android.R.attr.label
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.linceops.model.RolUsuario

@Composable
fun LoginScreen(
    onLoginSuccess: (RolUsuario) -> Unit,
    modifier: Modifier = Modifier
) {

    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "LinceOPS",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(48.dp))


        OutlinedTextField(
            value = usuario,
            onValueChange = {
                usuario = it
                mensajeError = ""
            },
            label = { Text("Usuario (Ej: admin, driver, guia)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))


        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                mensajeError = ""
            },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )


        if (mensajeError.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = mensajeError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(32.dp))


        Button(
            onClick = {

                when (usuario.lowercase().trim()) {
                    "admin", "manager" -> onLoginSuccess(RolUsuario.ADMINISTRADOR)
                    "driver", "conductor" -> onLoginSuccess(RolUsuario.CONDUCTOR)
                    "guia" -> onLoginSuccess(RolUsuario.GUIA)
                    else -> {
                        if (usuario.isEmpty() || password.isEmpty()) {
                            mensajeError = "Por favor ingresa tus credenciales."
                        } else {
                            mensajeError = "Usuario no válido. Usa 'admin', 'driver' o 'guia'."
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            Text("Ingresar")
        }

        Spacer(modifier = Modifier.height(48.dp))


        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Atajos de desarrollador (Testing):",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = { onLoginSuccess(RolUsuario.ADMINISTRADOR) }) { Text("Manager") }
            TextButton(onClick = { onLoginSuccess(RolUsuario.CONDUCTOR) }) { Text("Driver") }
            TextButton(onClick = { onLoginSuccess(RolUsuario.GUIA) }) { Text("Guía") }
        }
    }
}