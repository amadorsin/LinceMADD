package com.example.linceops.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

object EstadoSyncHito {
    const val PENDIENTE = "PENDIENTE"
    const val SINCRONIZANDO = "SINCRONIZANDO"
    const val ERROR_VALIDACION = "ERROR_VALIDACION"
}

@Entity(tableName = "hitos_pendientes")
data class HitoPendienteEntity(
    // Clave compuesta para idempotencia local: "${servicioId}_${usuarioId}_${tipo}"
    @PrimaryKey val claveIdempotencia: String,
    val servicioId: String,
    val usuarioId: String,
    val tipo: String, // "CHECK_IN" o "CHECK_OUT"
    val latitud: Double,
    val longitud: Double,
    val timestampIso: String,
    val pickup: String?,
    val oficina: String?,
    val horaPickupIso: String?,
    val estadoSync: String = EstadoSyncHito.PENDIENTE,
    val intentos: Int = 0,
    val ultimoError: String? = null,
    val creadoEnMillis: Long = System.currentTimeMillis()
)