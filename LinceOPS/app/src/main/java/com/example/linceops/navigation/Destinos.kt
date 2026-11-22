package com.example.linceops.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.linceops.R

sealed class Destino(
    val ruta: String,
    val icon: ImageVector? = null,
    @StringRes val titleRes: Int? = null
) {
    object Login : Destino("login")

    object Dashboard : Destino("dashboard/{rol}", Icons.Filled.Home, R.string.nav_inicio) {
        fun createRoute(rol: String) = "dashboard/$rol"
    }

    object Feed : Destino("feed", Icons.AutoMirrored.Filled.List, R.string.nav_servicios)
    object Perfil : Destino("perfil", Icons.Filled.Person, R.string.nav_perfil)

    object Detalle : Destino("detalle/{servicioId}") {
        fun createRoute(id: String) = "detalle/$id"
    }

    object Marcaje : Destino("marcaje/{servicioId}") {
        fun createRoute(id: String) = "marcaje/$id"
    }
}
