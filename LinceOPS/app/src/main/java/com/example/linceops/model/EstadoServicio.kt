package com.example.linceops.model


enum class EstadoServicio(val estado: String) {
    PENDIENTE("Pendiente"),
    CONFIRMADO("Confirmado"),
    RECHAZADO("Rechazado"),
    EN_CURSO("En Curso"),
    FINALIZADO("Finalizado")
}