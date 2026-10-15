package com.example.linceops

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.linceops.model.RolUsuario
import com.example.linceops.ui.screens.DashboardScreen
import com.example.linceops.ui.theme.LinceOPSTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LinceOPSTheme {
                // Un contenedor Surface usa el color de fondo del tema
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Por ahora, llamaremos directamente al Dashboard (InicioScreen)
                    // para que puedas ver tu maquetación.
                    DashboardScreen(RolUsuario.GUIA)
                }
            }
        }
    }
}