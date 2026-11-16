package com.example.linceops.network

import com.example.linceops.network.dto.HealthResponseDto
import com.example.linceops.network.dto.RespuestaPaginadaDto
import com.example.linceops.network.dto.ServicioDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SouthboundApi {

    @GET("health")
    suspend fun checkHealth(): Response<HealthResponseDto>

    @GET("api/v1/servicios")
    suspend fun obtenerServicios(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): Response<RespuestaPaginadaDto<ServicioDto>>

    @GET("api/v1/servicios/{id}")
    suspend fun obtenerServicioPorId(
        @Path("id") id: String
    ): Response<ServicioDto>
}
