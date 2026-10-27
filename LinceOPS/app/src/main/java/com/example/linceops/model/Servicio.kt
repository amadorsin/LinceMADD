package com.example.linceops.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "servicios")
data class Servicio(
    @PrimaryKey val id: String,
    val titulo: String,
    val origenDestino: String,
    val fecha: String,
    val hora: String,
    val pickup: String,
    val estado: EstadoServicio,
    val pasajeros: Int,
    val conductorId: String? = null,
    val guiaId: String? = null,
    val reservaId: String = id,
    val tipo: String = "Traslado",
    val idioma: String = "Español",
    val agencia: String = "Agencia de prueba",
    val hotel: String = "Sin hotel",
    val vehiculo: String = "Por informar",
    val detallePasajeros: List<Pasajero> = emptyList(),
    val inclusiones: List<String> = emptyList(),
    val instrucciones: String = "",
    val observaciones: String = "",
    val participaciones: List<ParticipacionServicio> =
        listOfNotNull(conductorId, guiaId).distinct().map {
            ParticipacionServicio(usuarioId = it, estado = estado)
        }
) {
    fun estaAsignadoA(usuarioId: String): Boolean =
        participaciones.any { it.usuarioId == usuarioId }


    fun estadoPara(usuario: Usuario): EstadoServicio =
        if (usuario.rol == RolUsuario.ADMINISTRADOR) estado
        else participaciones.firstOrNull { it.usuarioId == usuario.id }?.estado ?: estado
}

data class Pasajero(
    val nombre: String,
    val idioma: String,
    val requerimientos: String = "Sin requerimientos especiales"
)


data class ParticipacionServicio(
    val usuarioId: String,
    val estado: EstadoServicio = EstadoServicio.PENDIENTE,
    val motivoRechazo: String? = null,
    val checkIn: Marcaje? = null,
    val checkOut: Marcaje? = null
)

data class Marcaje(
    val fechaHoraMillis: Long,
    val latitud: Double,
    val longitud: Double
)
