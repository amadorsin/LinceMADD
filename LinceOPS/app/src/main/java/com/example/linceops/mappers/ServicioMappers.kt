package com.example.linceops.mappers

import com.example.linceops.model.EstadoServicio
import com.example.linceops.model.ParticipacionServicio
import com.example.linceops.model.Servicio
import com.example.linceops.network.dto.ServicioDto

fun ServicioDto.aEntidadLocal(servicioExistente: Servicio?): Servicio {
    val srvId = this.id?.toString() ?: ""
    val driverId = this.transportista?.id?.toString()
    val guideId = this.guia?.id?.toString()

    val estadoRemoto = mapearEstadoRemoto(
        statusPL = this.asignacion?.statusPL,
        estadoServicio = this.estadoServicio
    )

    // Preservamos el estado local si el usuario ya inició o finalizó el servicio en Room
    val estadoLocal = servicioExistente?.estado ?: estadoRemoto

    val participacionesLocales = servicioExistente?.participaciones ?: listOfNotNull(driverId, guideId)
        .distinct()
        .map { ParticipacionServicio(usuarioId = it, estado = estadoRemoto) }

    val tituloServicio = when {
        !this.descripcionPadre.isNullOrBlank() && !this.descripcion.isNullOrBlank() ->
            "${this.descripcionPadre} · ${this.descripcion}"
        !this.descripcion.isNullOrBlank() -> this.descripcion
        !this.descripcionPadre.isNullOrBlank() -> this.descripcionPadre
        else -> "Servicio ${this.codigo ?: srvId}"
    }

    return Servicio(
        id = srvId,
        titulo = tituloServicio,
        origenDestino = "${this.pickup ?: "Por definir"} -> ${this.dropoff ?: "Por definir"}",
        fecha = this.fecha ?: "",
        hora = this.horaPickup ?: "",
        pickup = this.pickup ?: "Por definir",
        dropoff = this.dropoff ?: "",
        estado = estadoLocal,
        pasajeros = this.pax?.total ?: 1,
        conductorId = driverId,
        guiaId = guideId,
        reservaId = this.reserva?.referencia ?: srvId,
        tipo = this.tipo ?: "TRANSFER",
        idioma = this.idiomaRequerido?.nombre ?: this.idiomaRequerido?.codigo ?: "Español",
        agencia = this.cliente?.nombre ?: "Sin Agencia",
        hotel = this.pickup ?: "Sin hotel",
        vehiculo = this.vehiculo?.patente ?: "Por informar",
        capacidadVehiculo = this.vehiculo?.capacidad ?: 0,
        oficina = this.oficina?.codigo ?: "SCL",
        statusPL = this.asignacion?.statusPL,
        detallePasajeros = servicioExistente?.detallePasajeros ?: emptyList(),
        inclusiones = servicioExistente?.inclusiones ?: emptyList(),
        instrucciones = "",
        observaciones = this.notas ?: "",
        participaciones = participacionesLocales
    )
}

private fun mapearEstadoRemoto(statusPL: String?, estadoServicio: String?): EstadoServicio {
    if (estadoServicio == "XX" || statusPL == "CX" || statusPL == "DL") {
        return EstadoServicio.RECHAZADO
    }
    return when (statusPL) {
        "CF" -> EstadoServicio.CONFIRMADO
        "RQ", null -> EstadoServicio.PENDIENTE
        else -> EstadoServicio.PENDIENTE
    }
}