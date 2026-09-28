package com.example.linceops.viewmodel

import com.example.linceops.model.Servicio
import com.example.linceops.model.Usuario

data class ServiceUiState(
    val usuarioLogueado: Usuario? = null,
    val servicios: List<Servicio> = emptyList(),
    val errorLogin: String? = null
)