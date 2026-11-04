package com.example.linceops.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.linceops.ui.theme.*

@Composable
fun PerfilScreen(
    modifier: Modifier = Modifier,
    onCerrarSesion: () -> Unit = {}
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LinceCreamBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Encabezado de la pantalla
        item {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    text = "Cuenta Operativa",
                    style = MaterialTheme.typography.labelLarge,
                    color = LinceTextSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Mi Perfil",
                    style = MaterialTheme.typography.headlineMedium,
                    color = LinceGreenPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Tarjeta 1: Identidad del Trabajador y Unidad Asignada
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LinceCardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, LinceGreenBorder.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar circular con iniciales
                    Surface(
                        modifier = Modifier.size(68.dp),
                        shape = CircleShape,
                        color = LinceGreenSageLight,
                        border = BorderStroke(2.dp, LinceGreenPrimary)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "RF",
                                style = MaterialTheme.typography.headlineSmall,
                                color = LinceGreenDark,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Rodrigo Fuentes",
                        style = MaterialTheme.typography.titleLarge,
                        color = LinceTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Conductor Profesional Clase A2",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LinceTextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Contenedor destacado de vehículo asignado
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = LinceGreenSageLight.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, LinceGreenBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "UNIDAD EN OPERACIÓN",
                                style = MaterialTheme.typography.labelSmall,
                                color = LinceGreenDark,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Van Mercedes Sprinter 4x4",
                                style = MaterialTheme.typography.titleMedium,
                                color = LinceTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Tarjeta 2: Protocolo de Imprevistos y Contacto Rápido
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LinceCardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, LinceGreenBorder.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StatusPendingBg,
                            border = BorderStroke(1.dp, StatusPendingText.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = StatusPendingText,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "¿No puedes asistir a un servicio asignado?",
                            fontWeight = FontWeight.Bold,
                            color = LinceTextPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Si tienes un imprevisto médico o de fuerza mayor para un turno ya calendarizado, comunícate de inmediato con tu Manager para reasignar tu ruta.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LinceTextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LinceGreenPrimary),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = LinceCardSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Llamar al Manager",
                                style = MaterialTheme.typography.labelLarge,
                                color = LinceCardSurface,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }

                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, LinceGreenPrimary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LinceGreenPrimary),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = LinceGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "WhatsApp Directo",
                                style = MaterialTheme.typography.labelLarge,
                                color = LinceGreenPrimary,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Botón de Cerrar Sesión
        item {
            Button(
                onClick = onCerrarSesion,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LinceDangerRed)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = LinceCardSurface,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cerrar Sesión",
                    style = MaterialTheme.typography.titleMedium,
                    color = LinceCardSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PerfilScreenPreview() {
    LinceOPSTheme {
        PerfilScreen()
    }
}