package com.example.linceops.network.dto

import com.google.gson.annotations.SerializedName

data class RespuestaSimpleDto<T>(
    @SerializedName("data") val data: T? = null
)

data class OficinaDto(
    @SerializedName("codigo") val codigo: String? = null,
    @SerializedName("nombre") val nombre: String? = null
)

data class IdiomaDto(
    @SerializedName("codigo") val codigo: String? = null,
    @SerializedName("nombre") val nombre: String? = null
)

data class PersonaRefDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("codigo") val codigo: String? = null,
    @SerializedName("nombre") val nombre: String? = null
)

data class ReservaDto(
    @SerializedName("referencia") val referencia: String? = null,
    @SerializedName("estado") val estado: String? = null,
    @SerializedName("nombre") val nombre: String? = null
)

data class ClienteDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("codigo") val codigo: String? = null,
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("pais") val pais: String? = null,
    @SerializedName("totalServicios") val totalServicios: Int? = null,
    @SerializedName("totalPax") val totalPax: Int? = null
)

data class AsignacionDto(
    @SerializedName("statusPL") val statusPL: String? = null,
    @SerializedName("actualizadoEn") val actualizadoEn: String? = null
)

data class VehiculoDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("patente") val patente: String? = null,
    @SerializedName("tipo") val tipo: String? = null,
    @SerializedName("capacidad") val capacidad: Int? = null
)

data class GuiaDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("codigo") val codigo: String? = null,
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("oficina") val oficina: OficinaDto? = null,
    @SerializedName("idiomas") val idiomas: List<IdiomaDto> = emptyList(),
    @SerializedName("evaluacionPromedio") val evaluacionPromedio: Double? = null,
    @SerializedName("fechaIngreso") val fechaIngreso: String? = null,
    @SerializedName("activo") val activo: Boolean? = null,
    @SerializedName("totalServicios") val totalServicios: Int? = null
)

data class TransportistaDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("codigo") val codigo: String? = null,
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("oficina") val oficina: OficinaDto? = null,
    @SerializedName("vehiculos") val vehiculos: List<VehiculoDto> = emptyList(),
    @SerializedName("evaluacionPromedio") val evaluacionPromedio: Double? = null,
    @SerializedName("fechaIngreso") val fechaIngreso: String? = null,
    @SerializedName("activo") val activo: Boolean? = null,
    @SerializedName("totalServicios") val totalServicios: Int? = null
)

data class CodigoDescripcionDto(
    @SerializedName("codigo") val codigo: String? = null,
    @SerializedName("descripcion") val descripcion: String? = null
)

data class TipoServicioCatalogoDto(
    @SerializedName("tipo") val tipo: String? = null,
    @SerializedName("srv") val srv: String? = null
)

data class TipoVehiculoCatalogoDto(
    @SerializedName("tipo") val tipo: String? = null,
    @SerializedName("capacidad") val capacidad: Int? = null
)

data class CatalogosDto(
    @SerializedName("oficinas") val oficinas: List<OficinaDto> = emptyList(),
    @SerializedName("idiomas") val idiomas: List<IdiomaDto> = emptyList(),
    @SerializedName("tiposServicio") val tiposServicio: List<TipoServicioCatalogoDto> = emptyList(),
    @SerializedName("estadosServicio") val estadosServicio: List<CodigoDescripcionDto> = emptyList(),
    @SerializedName("statusPL") val statusPL: List<CodigoDescripcionDto> = emptyList(),
    @SerializedName("tiposVehiculo") val tiposVehiculo: List<TipoVehiculoCatalogoDto> = emptyList()
)

data class ConteoPorClaveDto(
    @SerializedName("clave") val clave: String? = null,
    @SerializedName("servicios") val servicios: Int = 0,
    @SerializedName("pax") val pax: Int = 0
)

data class ResumenDto(
    @SerializedName("fechaDesde") val fechaDesde: String? = null,
    @SerializedName("fechaHasta") val fechaHasta: String? = null,
    @SerializedName("totalServicios") val totalServicios: Int = 0,
    @SerializedName("totalPax") val totalPax: Int = 0,
    @SerializedName("porEstadoServicio") val porEstadoServicio: List<ConteoPorClaveDto> = emptyList(),
    @SerializedName("porStatusPL") val porStatusPL: List<ConteoPorClaveDto> = emptyList(),
    @SerializedName("porOficina") val porOficina: List<ConteoPorClaveDto> = emptyList(),
    @SerializedName("porTipo") val porTipo: List<ConteoPorClaveDto> = emptyList(),
    @SerializedName("porDia") val porDia: List<ConteoPorClaveDto> = emptyList()
)

data class ApiErrorWrapperDto(
    @SerializedName("error") val error: ApiErrorDto? = null
)

data class ApiErrorDto(
    @SerializedName("code") val code: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("details") val details: List<ApiErrorDetalleDto> = emptyList(),
    @SerializedName("requestId") val requestId: String? = null
)

data class ApiErrorDetalleDto(
    @SerializedName("field") val field: String? = null,
    @SerializedName("message") val message: String? = null
)