package com.example.linceops.network.dto

import com.google.gson.annotations.SerializedName

data class RespuestaPaginadaDto<T>(
    @SerializedName("data") val data: List<T> = emptyList(),
    @SerializedName("meta") val meta: MetaPaginacionDto? = null
)

data class MetaPaginacionDto(
    @SerializedName("page") val page: Int? = 1,
    @SerializedName("pageSize") val pageSize: Int? = 20,
    @SerializedName("total") val total: Int? = 0,
    @SerializedName("totalPages") val totalPages: Int? = 1
)

data class HealthResponseDto(
    @SerializedName("status") val status: String? = null,
    @SerializedName("db") val db: String? = null,
    @SerializedName("version") val version: String? = null,
    @SerializedName("time") val time: String? = null
)

data class ServicioDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("codigo") val codigo: String? = null,
    @SerializedName("srv") val srv: String? = null,
    @SerializedName("tipo") val tipo: String? = null,
    @SerializedName("fecha") val fecha: String? = null,
    @SerializedName("horaPickup") val horaPickup: String? = null,
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("descripcionPadre") val descripcionPadre: String? = null,
    @SerializedName("estadoServicio") val estadoServicio: String? = null,
    @SerializedName("pickup") val pickup: String? = null,
    @SerializedName("dropoff") val dropoff: String? = null,
    @SerializedName("notas") val notas: String? = null,
    @SerializedName("oficina") val oficina: OficinaDto? = null,
    @SerializedName("idiomaRequerido") val idiomaRequerido: IdiomaDto? = null,
    @SerializedName("asignacion") val asignacion: AsignacionDto? = null,
    @SerializedName("reserva") val reserva: ReservaDto? = null,
    @SerializedName("cliente") val cliente: ClienteDto? = null,
    @SerializedName("pax") val pax: PaxDto? = null,
    @SerializedName("guia") val guia: PersonaRefDto? = null,
    @SerializedName("transportista") val transportista: PersonaRefDto? = null,
    @SerializedName("vehiculo") val vehiculo: VehiculoDto? = null
)