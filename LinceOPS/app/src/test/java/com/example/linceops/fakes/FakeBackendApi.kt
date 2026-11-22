package com.example.linceops.fakes

import com.example.linceops.network.BackendApi
import com.example.linceops.network.dto.AuthResponseDto
import com.example.linceops.network.dto.BloqueoCalendarioDto
import com.example.linceops.network.dto.BloqueoRequestDto
import com.example.linceops.network.dto.ConfirmacionRequestDto
import com.example.linceops.network.dto.ConfirmacionResponseDto
import com.example.linceops.network.dto.GeocercaDto
import com.example.linceops.network.dto.HitoDto
import com.example.linceops.network.dto.IncidenciaDto
import com.example.linceops.network.dto.IncidenciaRequestDto
import com.example.linceops.network.dto.LoginRequestDto
import com.example.linceops.network.dto.MarcajeRequestDto
import com.example.linceops.network.dto.MensajeResponseDto
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

fun <T> respuestaError(codigo: Int, mensaje: String? = null): Response<T> {
    val cuerpo = if (mensaje == null) {
        "".toResponseBody(null)
    } else {
        """{"timestamp":"2026-01-01T00:00:00","status":$codigo,"error":"Error","message":"$mensaje"}"""
            .toResponseBody("application/json".toMediaType())
    }
    return Response.error(codigo, cuerpo)
}

class FakeBackendApi : BackendApi {

    var respuestaLogin: suspend (LoginRequestDto) -> Response<AuthResponseDto> = { peticion ->
        Response.success(
            AuthResponseDto(
                token = "jwt-${peticion.correo}",
                usuarioId = "20",
                nombre = "Matías Soto Herrera",
                rol = "CONDUCTOR"
            )
        )
    }

    var respuestaRegistrarMarcaje: suspend (
        MarcajeRequestDto, String?, String?, String?
    ) -> Response<MensajeResponseDto> =
        { _, _, _, _ -> Response.success(MensajeResponseDto("Marcaje registrado")) }

    var respuestaRegistrarIncidencia: suspend (IncidenciaRequestDto) -> Response<IncidenciaDto> =
        { request ->
            Response.success(
                IncidenciaDto(
                    id = 1,
                    servicioId = request.servicioId,
                    motivo = request.motivo,
                    detalle = request.detalle
                )
            )
        }

    var respuestaObtenerIncidenciasPorServicio: suspend (String) -> Response<List<IncidenciaDto>> =
        { servicioId -> Response.success(listOf(IncidenciaDto(id = 1, servicioId = servicioId))) }

    var respuestaBloquearFecha: suspend (BloqueoRequestDto) -> Response<BloqueoCalendarioDto> =
        { request ->
            Response.success(
                BloqueoCalendarioDto(
                    id = 1,
                    fechaBloqueada = request.fechaBloqueada,
                    motivo = request.motivo
                )
            )
        }

    var respuestaObtenerMisBloqueos: suspend () -> Response<List<BloqueoCalendarioDto>> =
        { Response.success(emptyList()) }

    var respuestaRegistrarConfirmacion: suspend (ConfirmacionRequestDto) -> Response<ConfirmacionResponseDto> =
        { request ->
            Response.success(
                ConfirmacionResponseDto(
                    id = 1,
                    servicioId = request.servicioId,
                    estado = request.estado,
                    motivo = request.motivo
                )
            )
        }

    var respuestaDesbloquearFecha: suspend (String) -> Response<Unit> =
        { Response.success(Unit) }

    var respuestaObtenerGeocercas: suspend () -> Response<List<GeocercaDto>> =
        { Response.success(emptyList()) }

    var respuestaObtenerTodosLosHitos: suspend () -> Response<List<HitoDto>> =
        { Response.success(emptyList()) }

    var respuestaObtenerTodasLasIncidencias: suspend () -> Response<List<IncidenciaDto>> =
        { Response.success(emptyList()) }

    val llamadasLogin = mutableListOf<LoginRequestDto>()
    val llamadasMarcaje = mutableListOf<MarcajeRequestDto>()
    val llamadasConfirmacion = mutableListOf<ConfirmacionRequestDto>()
    val llamadasBloqueo = mutableListOf<BloqueoRequestDto>()
    val llamadasDesbloqueo = mutableListOf<String>()
    val llamadasIncidencia = mutableListOf<IncidenciaRequestDto>()
    var llamadasObtenerMisBloqueos = 0
    var llamadasObtenerGeocercas = 0
    var llamadasObtenerTodosLosHitos = 0
    var llamadasObtenerTodasLasIncidencias = 0

    override suspend fun login(request: LoginRequestDto): Response<AuthResponseDto> {
        llamadasLogin += request
        return respuestaLogin(request)
    }

    override suspend fun registrarMarcaje(
        request: MarcajeRequestDto,
        pickup: String?,
        oficina: String?,
        horaPickup: String?
    ): Response<MensajeResponseDto> {
        llamadasMarcaje += request
        return respuestaRegistrarMarcaje(request, pickup, oficina, horaPickup)
    }

    override suspend fun registrarIncidencia(request: IncidenciaRequestDto): Response<IncidenciaDto> {
        llamadasIncidencia += request
        return respuestaRegistrarIncidencia(request)
    }

    override suspend fun obtenerIncidenciasPorServicio(servicioId: String): Response<List<IncidenciaDto>> =
        respuestaObtenerIncidenciasPorServicio(servicioId)

    override suspend fun bloquearFecha(request: BloqueoRequestDto): Response<BloqueoCalendarioDto> {
        llamadasBloqueo += request
        return respuestaBloquearFecha(request)
    }

    override suspend fun obtenerMisBloqueos(): Response<List<BloqueoCalendarioDto>> {
        llamadasObtenerMisBloqueos += 1
        return respuestaObtenerMisBloqueos()
    }

    override suspend fun registrarConfirmacion(request: ConfirmacionRequestDto): Response<ConfirmacionResponseDto> {
        llamadasConfirmacion += request
        return respuestaRegistrarConfirmacion(request)
    }

    override suspend fun desbloquearFecha(fechaIso: String): Response<Unit> {
        llamadasDesbloqueo += fechaIso
        return respuestaDesbloquearFecha(fechaIso)
    }

    override suspend fun obtenerGeocercas(): Response<List<GeocercaDto>> {
        llamadasObtenerGeocercas += 1
        return respuestaObtenerGeocercas()
    }

    override suspend fun obtenerTodosLosHitos(): Response<List<HitoDto>> {
        llamadasObtenerTodosLosHitos += 1
        return respuestaObtenerTodosLosHitos()
    }

    override suspend fun obtenerTodasLasIncidencias(): Response<List<IncidenciaDto>> {
        llamadasObtenerTodasLasIncidencias += 1
        return respuestaObtenerTodasLasIncidencias()
    }
}
