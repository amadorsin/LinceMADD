package com.example.linceops.mappers

import com.example.linceops.model.*
import com.example.linceops.network.dto.ServicioDto

fun ServicioDto.aEntidadLocal(servicioExistente: Servicio?): Servicio {
    val srvId = this.id?.toString() ?: ""
    val driverId = this.transportista?.id?.toString()
    val guideId = this.guia?.id?.toString()

    val statusPLEnum = StatusPL.fromApi(this.asignacion?.statusPL)
    val estadoTourplanEnum = EstadoTourplan.fromApi(this.estadoServicio)

    val estadoRemoto = mapearEstadoRemoto(
        statusPL = statusPLEnum,
        estadoTourplan = estadoTourplanEnum
    )

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
        srv = CodigoSrv.fromApi(this.srv),
        tipo = TipoServicio.fromApi(this.tipo),
        estadoTourplan = estadoTourplanEnum,
        idiomaRequerido = CodigoIdioma.fromApi(this.idiomaRequerido?.codigo),
        agencia = this.cliente?.nombre ?: "Sin Agencia",
        hotel = this.pickup ?: "Sin hotel",
        vehiculo = this.vehiculo?.patente ?: "Por informar",
        tipoVehiculo = TipoVehiculo.fromApi(this.vehiculo?.tipo),
        capacidadVehiculo = this.vehiculo?.capacidad ?: 0,
        oficina = CodigoOficina.fromApi(this.oficina?.codigo),
        statusPL = statusPLEnum,
        detallePasajeros = servicioExistente?.detallePasajeros ?: emptyList(),
        inclusiones = servicioExistente?.inclusiones ?: emptyList(),
        instrucciones = "",
        observaciones = this.notas ?: "",
        participaciones = participacionesLocales
    )
}

private fun mapearEstadoRemoto(statusPL: StatusPL?, estadoTourplan: EstadoTourplan): EstadoServicio {
    if (estadoTourplan == EstadoTourplan.XX || statusPL == StatusPL.CX || statusPL == StatusPL.DL) {
        return EstadoServicio.RECHAZADO
    }
    return when (statusPL) {
        StatusPL.CF -> EstadoServicio.CONFIRMADO
        StatusPL.RQ, null -> EstadoServicio.PENDIENTE
        else -> EstadoServicio.PENDIENTE
    }
}