package com.example.linceops.network

import com.example.linceops.network.dto.*
import retrofit2.Response
import retrofit2.http.*

interface BackendApi {

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): Response<AuthResponseDto>

    @POST("api/hitos/marcaje")
    suspend fun registrarMarcaje(
        @Body request: MarcajeRequestDto,
        @Query("pickup") pickup: String?,
        @Query("oficina") oficina: String?,
        @Query("horaPickup") horaPickup: String?
    ): Response<MensajeResponseDto>

    @POST("api/incidencias/registrar")
    suspend fun registrarIncidencia(
        @Body request: IncidenciaRequestDto
    ): Response<IncidenciaDto>

    @GET("api/incidencias/servicio/{servicioId}")
    suspend fun obtenerIncidenciasPorServicio(
        @Path("servicioId") servicioId: String
    ): Response<List<IncidenciaDto>>
    @POST("api/disponibilidad/bloquear")
    suspend fun bloquearFecha(@Body request: BloqueoRequestDto): Response<BloqueoCalendarioDto>

    @GET("api/disponibilidad/mis-bloqueos")
    suspend fun obtenerMisBloqueos(): Response<List<BloqueoCalendarioDto>>

    @DELETE("api/disponibilidad/desbloquear/{fecha}")
    suspend fun desbloquearFecha(@Path("fecha") fechaIso: String): Response<Unit>
}