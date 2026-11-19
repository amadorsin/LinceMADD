package com.example.linceops.network

import com.example.linceops.network.dto.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SouthboundApi {

    @GET("health")
    suspend fun checkHealth(): Response<HealthResponseDto>

    @GET("api/v1/servicios")
    suspend fun obtenerServicios(
        @Query("fechaDesde") fechaDesde: String? = null,
        @Query("fechaHasta") fechaHasta: String? = null,
        @Query("oficina") oficina: String? = null,
        @Query("tipo") tipo: String? = null,
        @Query("estadoServicio") estadoServicio: String? = null,
        @Query("estadoReserva") estadoReserva: String? = null,
        @Query("statusPL") statusPL: String? = null,
        @Query("clienteId") clienteId: Int? = null,
        @Query("guiaId") guiaId: Int? = null,
        @Query("transportistaId") transportistaId: Int? = null,
        @Query("q") q: String? = null,
        @Query("orden") orden: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): Response<RespuestaPaginadaDto<ServicioDto>>

    @GET("api/v1/servicios/{id}")
    suspend fun obtenerServicioPorId(
        @Path("id") id: Int
    ): Response<RespuestaSimpleDto<ServicioDto>>

    @GET("api/v1/guias")
    suspend fun obtenerGuias(
        @Query("oficina") oficina: String? = null,
        @Query("idioma") idioma: String? = null,
        @Query("activo") activo: Boolean? = null,
        @Query("q") q: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): Response<RespuestaPaginadaDto<GuiaDto>>

    @GET("api/v1/guias/{id}")
    suspend fun obtenerGuiaPorId(
        @Path("id") id: Int
    ): Response<RespuestaSimpleDto<GuiaDto>>

    @GET("api/v1/transportistas")
    suspend fun obtenerTransportistas(
        @Query("oficina") oficina: String? = null,
        @Query("activo") activo: Boolean? = null,
        @Query("capacidadMin") capacidadMin: Int? = null,
        @Query("q") q: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): Response<RespuestaPaginadaDto<TransportistaDto>>

    @GET("api/v1/transportistas/{id}")
    suspend fun obtenerTransportistaPorId(
        @Path("id") id: Int
    ): Response<RespuestaSimpleDto<TransportistaDto>>

    @GET("api/v1/clientes")
    suspend fun obtenerClientes(
        @Query("pais") pais: String? = null,
        @Query("q") q: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): Response<RespuestaPaginadaDto<ClienteDto>>

    @GET("api/v1/clientes/{id}")
    suspend fun obtenerClientePorId(
        @Path("id") id: Int
    ): Response<RespuestaSimpleDto<ClienteDto>>

    @GET("api/v1/catalogos")
    suspend fun obtenerCatalogos(): Response<RespuestaSimpleDto<CatalogosDto>>

    @GET("api/v1/resumen")
    suspend fun obtenerResumen(
        @Query("fechaDesde") fechaDesde: String? = null,
        @Query("fechaHasta") fechaHasta: String? = null,
        @Query("oficina") oficina: String? = null
    ): Response<RespuestaSimpleDto<ResumenDto>>
}