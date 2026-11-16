package com.example.linceops.network.dto

import com.google.gson.annotations.SerializedName

data class ReservaDto(
    @SerializedName("referencia") val referencia: String? = null,
    @SerializedName("estado") val estado: String? = null,
    @SerializedName("nombre") val nombre: String? = null
)

data class ClienteDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("nombre") val nombre: String? = null
)

data class AsignacionDto(
    @SerializedName("statusPL") val statusPL: String? = null
)

data class GuiaDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("idiomas") val idiomas: List<String>? = null,
    @SerializedName("oficina") val oficina: String? = null
)

data class VehiculoDto(
    @SerializedName("patente") val patente: String? = null,
    @SerializedName("tipo") val tipo: String? = null,
    @SerializedName("capacidad") val capacidad: Int? = null
)

data class TransportistaDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("vehiculo") val vehiculo: VehiculoDto? = null
)
