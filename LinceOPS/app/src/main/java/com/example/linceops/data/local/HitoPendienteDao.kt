package com.example.linceops.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HitoPendienteDao {

    @Query("SELECT * FROM hitos_pendientes ORDER BY creadoEnMillis ASC")
    fun observarTodos(): Flow<List<HitoPendienteEntity>>

    @Query("SELECT * FROM hitos_pendientes WHERE estadoSync != 'ERROR_VALIDACION' ORDER BY creadoEnMillis ASC")
    suspend fun obtenerPendientesParaEnviar(): List<HitoPendienteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizar(hito: HitoPendienteEntity)

    @Query("UPDATE hitos_pendientes SET estadoSync = :estado, intentos = :intentos, ultimoError = :error WHERE claveIdempotencia = :clave")
    suspend fun actualizarEstado(clave: String, estado: String, intentos: Int, error: String?)

    @Query("DELETE FROM hitos_pendientes WHERE claveIdempotencia = :clave")
    suspend fun eliminar(clave: String)

    @Query("DELETE FROM hitos_pendientes WHERE estadoSync = 'ERROR_VALIDACION'")
    suspend fun limpiarErroresValidacion()
}