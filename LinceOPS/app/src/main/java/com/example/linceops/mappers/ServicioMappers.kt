package com.example.linceops.mappers

import com.example.linceops.model.EstadoServicio
import com.example.linceops.model.ParticipacionServicio
import com.example.linceops.model.Servicio
import com.example.linceops.network.dto.ServicioDto

fun ServicioDto.aEntidadLocal(servicioExistente: Servicio?): Servicio {
    val srvId = this.id ?: ""
    val driverId = this.transportista?.id
    val guideId = this.guia?.id

    // MAGIA OFFLINE-FIRST: Si el servicio ya existía en Room, preservamos sus
    // estados y marcajes locales. Si es nuevo, arranca como PENDIENTE.
    val estadoLocal = servicioExistente?.estado ?: EstadoServicio.PENDIENTE

    val participacionesLocales = servicioExistente?.participaciones ?: listOfNotNull(driverId, guideId)
        .distinct()
        .map { ParticipacionServicio(usuarioId = it, estado = EstadoServicio.PENDIENTE) }

    return Servicio(
        id = srvId,
        titulo = this.descripcionPadre ?: this.descripcion ?: "Servicio sin título",
        origenDestino = "${this.pickup ?: "Por definir"} -> ${this.dropoff ?: "Por definir"}",
        fecha = this.fecha ?: "",
        hora = this.horaPickup ?: "",
        pickup = this.pickup ?: "Por definir",
        dropoff = this.dropoff ?: "",
        estado = estadoLocal, // ¡Clave! Mantenemos los check-ins locales
        pasajeros = this.pax?.total ?: 1,
        conductorId = driverId,
        guiaId = guideId,
        reservaId = this.reserva?.referencia ?: srvId,
        tipo = this.tipo ?: "Transfer",
        idioma = this.idioma ?: "Español",
        agencia = this.cliente?.nombre ?: "Sin Agencia",
        hotel = "Sin hotel",
        vehiculo = this.transportista?.vehiculo?.patente ?: "Por informar",
        capacidadVehiculo = this.transportista?.vehiculo?.capacidad ?: 0,
        oficina = this.oficina ?: "SCL",
        statusPL = this.asignacion?.statusPL,
        detallePasajeros = servicioExistente?.detallePasajeros ?: emptyList(),
        inclusiones = this.inclusiones ?: emptyList(),
        instrucciones = "",
        observaciones = this.observaciones ?: "",
        participaciones = participacionesLocales // ¡Clave! Mantenemos los marcajes GPS
    )
}