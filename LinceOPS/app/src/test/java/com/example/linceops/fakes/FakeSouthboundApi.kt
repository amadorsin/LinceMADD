package com.example.linceops.fakes

import com.example.linceops.network.SouthboundApi
import com.example.linceops.network.dto.CatalogosDto
import com.example.linceops.network.dto.ClienteDto
import com.example.linceops.network.dto.GuiaDto
import com.example.linceops.network.dto.HealthResponseDto
import com.example.linceops.network.dto.ResumenDto
import com.example.linceops.network.dto.RespuestaPaginadaDto
import com.example.linceops.network.dto.RespuestaSimpleDto
import com.example.linceops.network.dto.ServicioDto
import com.example.linceops.network.dto.TransportistaDto
import retrofit2.Response

data class ConsultaServicios(
    val fechaDesde: String?,
    val fechaHasta: String?,
    val oficina: String?,
    val tipo: String?,
    val estadoServicio: String?,
    val estadoReserva: String?,
    val statusPL: String?,
    val clienteId: Int?,
    val guiaId: Int?,
    val transportistaId: Int?,
    val q: String?,
    val orden: String?,
    val page: Int,
    val pageSize: Int
)

class FakeSouthboundApi : SouthboundApi {

    var servicios: List<ServicioDto> = emptyList()
    var codigoServicios: Int = 200
    var errorServicios: String? = null
    var excepcionServicios: Exception? = null
    var ultimaConsultaServicios: ConsultaServicios? = null

    var detalleServicio: ServicioDto? = null
    var codigoDetalle: Int = 200
    var excepcionDetalle: Exception? = null

    var resumen: ResumenDto? = null
    var codigoResumen: Int = 200
    var excepcionResumen: Exception? = null

    var guias: List<GuiaDto> = emptyList()
    var codigoGuias: Int = 200
    var excepcionGuias: Exception? = null

    var transportistas: List<TransportistaDto> = emptyList()
    var codigoTransportistas: Int = 200
    var excepcionTransportistas: Exception? = null

    var llamadasServicios = 0
    var llamadasDetalle = 0

    override suspend fun checkHealth(): Response<HealthResponseDto> =
        Response.success(HealthResponseDto(status = "ok"))

    override suspend fun obtenerServicios(
        fechaDesde: String?,
        fechaHasta: String?,
        oficina: String?,
        tipo: String?,
        estadoServicio: String?,
        estadoReserva: String?,
        statusPL: String?,
        clienteId: Int?,
        guiaId: Int?,
        transportistaId: Int?,
        q: String?,
        orden: String?,
        page: Int,
        pageSize: Int
    ): Response<RespuestaPaginadaDto<ServicioDto>> {
        llamadasServicios += 1
        ultimaConsultaServicios = ConsultaServicios(
            fechaDesde = fechaDesde,
            fechaHasta = fechaHasta,
            oficina = oficina,
            tipo = tipo,
            estadoServicio = estadoServicio,
            estadoReserva = estadoReserva,
            statusPL = statusPL,
            clienteId = clienteId,
            guiaId = guiaId,
            transportistaId = transportistaId,
            q = q,
            orden = orden,
            page = page,
            pageSize = pageSize
        )
        excepcionServicios?.let { throw it }
        if (codigoServicios != 200) return respuestaError(codigoServicios, errorServicios)
        return Response.success(RespuestaPaginadaDto(data = servicios))
    }

    override suspend fun obtenerServicioPorId(id: Int): Response<RespuestaSimpleDto<ServicioDto>> {
        llamadasDetalle += 1
        excepcionDetalle?.let { throw it }
        if (codigoDetalle != 200) return respuestaError(codigoDetalle)
        return Response.success(RespuestaSimpleDto(data = detalleServicio))
    }

    override suspend fun obtenerGuias(
        oficina: String?,
        idioma: String?,
        activo: Boolean?,
        q: String?,
        page: Int,
        pageSize: Int
    ): Response<RespuestaPaginadaDto<GuiaDto>> {
        excepcionGuias?.let { throw it }
        if (codigoGuias != 200) return respuestaError(codigoGuias)
        return Response.success(RespuestaPaginadaDto(data = guias))
    }

    override suspend fun obtenerGuiaPorId(id: Int): Response<RespuestaSimpleDto<GuiaDto>> =
        Response.success(RespuestaSimpleDto(data = null))

    override suspend fun obtenerTransportistas(
        oficina: String?,
        activo: Boolean?,
        capacidadMin: Int?,
        q: String?,
        page: Int,
        pageSize: Int
    ): Response<RespuestaPaginadaDto<TransportistaDto>> {
        excepcionTransportistas?.let { throw it }
        if (codigoTransportistas != 200) return respuestaError(codigoTransportistas)
        return Response.success(RespuestaPaginadaDto(data = transportistas))
    }

    override suspend fun obtenerTransportistaPorId(id: Int): Response<RespuestaSimpleDto<TransportistaDto>> =
        Response.success(RespuestaSimpleDto(data = null))

    override suspend fun obtenerClientes(
        pais: String?,
        q: String?,
        page: Int,
        pageSize: Int
    ): Response<RespuestaPaginadaDto<ClienteDto>> =
        Response.success(RespuestaPaginadaDto())

    override suspend fun obtenerClientePorId(id: Int): Response<RespuestaSimpleDto<ClienteDto>> =
        Response.success(RespuestaSimpleDto(data = null))

    override suspend fun obtenerCatalogos(): Response<RespuestaSimpleDto<CatalogosDto>> =
        Response.success(RespuestaSimpleDto(data = CatalogosDto()))

    override suspend fun obtenerResumen(
        fechaDesde: String?,
        fechaHasta: String?,
        oficina: String?
    ): Response<RespuestaSimpleDto<ResumenDto>> {
        excepcionResumen?.let { throw it }
        if (codigoResumen != 200) return respuestaError(codigoResumen)
        return Response.success(RespuestaSimpleDto(data = resumen))
    }
}
