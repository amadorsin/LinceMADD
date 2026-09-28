package com.example.linceops.model

data class Servicio(
    val id: String,
    val titulo: String,
    val origenDestino: String,
    val fecha: String,
    val hora: String,
    val pickup: String,
    val estado: EstadoServicio,
    val pasajeros: Int,
    val conductorId: String? = null
)