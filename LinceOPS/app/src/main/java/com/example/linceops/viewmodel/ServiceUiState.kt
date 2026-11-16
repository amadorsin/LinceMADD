package com.example.linceops.viewmodel

import com.example.linceops.model.EstadoServicio
import com.example.linceops.model.Servicio
import com.example.linceops.model.Usuario

data class ServiceUiState(
    val usuarioLogueado: Usuario? = null,
    // Lista filtrada que consume la pantalla de Servicios.
    val servicios: List<Servicio> = emptyList(),
    // Lista completa del usuario. La usa Inicio para no heredar los filtros.
    val serviciosTotales: List<Servicio> = emptyList(),
    val errorLogin: String? = null,
    val filtroEstado: EstadoServicio? = null,
    val filtroFecha: String? = null,
    val mensaje: String? = null,
    val error: String? = null
)