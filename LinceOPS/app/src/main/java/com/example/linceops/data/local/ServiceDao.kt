package com.example.linceops.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.linceops.model.Servicio

@Dao
interface ServicioDao {
    @Query("SELECT * FROM servicios")
    suspend fun obtenerTodosLosServicios(): List<Servicio>

    @Query("SELECT * FROM servicios WHERE id = :servicioId")
    suspend fun obtenerServicioPorId(servicioId: String): Servicio?

    // Si insertamos un servicio que ya existe, REPLACE lo actualiza (ideal para cambiar estados)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarServicio(servicio: Servicio)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarServiciosIniciales(servicios: List<Servicio>)

    @Query("DELETE FROM servicios")
    suspend fun limpiarTabla()
}