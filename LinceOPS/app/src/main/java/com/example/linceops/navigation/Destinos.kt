package com.example.linceops.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Destino(val ruta: String, val icon: ImageVector? = null, val title: String? = null) {
    object Login : Destino("login")

    object Dashboard : Destino("dashboard/{rol}", Icons.Filled.Home, "Inicio") {
        fun createRoute(rol: String) = "dashboard/$rol"
    }
    object Feed : Destino("feed", Icons.Filled.List, "Servicios")
    object Perfil : Destino("perfil", Icons.Filled.Person, "Perfil")
    object Detalle : Destino("detalle/{servicioId}") {
        fun createRoute(id: String) = "detalle/$id"
    }

    object Marcaje : Destino("marcaje/{servicioId}") {
        fun createRoute(id: String) = "marcaje/$id"
    }
}