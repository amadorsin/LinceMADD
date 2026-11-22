package com.example.linceops.mappers

import com.example.linceops.model.CodigoIdioma
import com.example.linceops.model.CodigoOficina
import com.example.linceops.model.CodigoSrv
import com.example.linceops.model.EstadoServicio
import com.example.linceops.model.EstadoTourplan
import com.example.linceops.model.ParticipacionServicio
import com.example.linceops.model.Pasajero
import com.example.linceops.model.Servicio
import com.example.linceops.model.StatusPL
import com.example.linceops.model.TipoServicio
import com.example.linceops.model.TipoVehiculo
import com.example.linceops.network.dto.AsignacionDto
import com.example.linceops.network.dto.ClienteDto
import com.example.linceops.network.dto.IdiomaDto
import com.example.linceops.network.dto.OficinaDto
import com.example.linceops.network.dto.PaxDto
import com.example.linceops.network.dto.PersonaRefDto
import com.example.linceops.network.dto.ReservaDto
import com.example.linceops.network.dto.ServicioDto
import com.example.linceops.network.dto.VehiculoDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ServicioMappersTest {

    private fun dto(
        id: Int? = 1,
        codigo: String? = null,
        descripcion: String? = null,
        descripcionPadre: String? = null,
        estadoServicio: String? = null,
        statusPL: String? = null
    ) = ServicioDto(
        id = id,
        codigo = codigo,
        descripcion = descripcion,
        descripcionPadre = descripcionPadre,
        estadoServicio = estadoServicio,
        asignacion = statusPL?.let { AsignacionDto(statusPL = it) }
    )

    @Test
    fun titulo_combinaPadreYDescripcion() {
        val entidad = dto(descripcionPadre = "City Tour", descripcion = "Frutillar").aEntidadLocal(null)
        assertEquals("City Tour · Frutillar", entidad.titulo)
    }

    @Test
    fun titulo_soloDescripcion() {
        val entidad = dto(descripcion = "Frutillar").aEntidadLocal(null)
        assertEquals("Frutillar", entidad.titulo)
    }

    @Test
    fun titulo_soloDescripcionPadre() {
        val entidad = dto(descripcionPadre = "City Tour").aEntidadLocal(null)
        assertEquals("City Tour", entidad.titulo)
    }

    @Test
    fun titulo_fallbackConCodigo() {
        val entidad = dto(id = 77, codigo = "TR-77").aEntidadLocal(null)
        assertEquals("Servicio TR-77", entidad.titulo)
    }

    @Test
    fun titulo_fallbackConId() {
        val entidad = dto(id = 77, codigo = null).aEntidadLocal(null)
        assertEquals("Servicio 77", entidad.titulo)
    }

    @Test
    fun estadoRemoto_confirmado() {
        val entidad = dto(statusPL = "CF").aEntidadLocal(null)
        assertEquals(EstadoServicio.CONFIRMADO, entidad.estado)
    }

    @Test
    fun estadoRemoto_pendientePorSolicitud() {
        val entidad = dto(statusPL = "RQ").aEntidadLocal(null)
        assertEquals(EstadoServicio.PENDIENTE, entidad.estado)
    }

    @Test
    fun estadoRemoto_pendienteSinStatus() {
        val entidad = dto().aEntidadLocal(null)
        assertEquals(EstadoServicio.PENDIENTE, entidad.estado)
        assertNull(entidad.statusPL)
    }

    @Test
    fun estadoRemoto_rechazadoPorTourplanXX() {
        val entidad = dto(estadoServicio = "XX").aEntidadLocal(null)
        assertEquals(EstadoServicio.RECHAZADO, entidad.estado)
        assertEquals(EstadoTourplan.XX, entidad.estadoTourplan)
    }

    @Test
    fun estadoRemoto_rechazadoPorStatusCX() {
        val entidad = dto(statusPL = "CX").aEntidadLocal(null)
        assertEquals(EstadoServicio.RECHAZADO, entidad.estado)
    }

    @Test
    fun estadoRemoto_rechazadoPorStatusDL() {
        val entidad = dto(statusPL = "DL").aEntidadLocal(null)
        assertEquals(EstadoServicio.RECHAZADO, entidad.estado)
    }

    @Test
    fun servicioExistente_preservaEstadoParticipacionesDetalleEInclusiones() {
        val participaciones = listOf(
            ParticipacionServicio(usuarioId = "20", estado = EstadoServicio.EN_CURSO)
        )
        val existente = Servicio(
            id = "1",
            titulo = "Anterior",
            origenDestino = "A -> B",
            fecha = "2026-11-20",
            hora = "09:00",
            pickup = "Hotel",
            estado = EstadoServicio.EN_CURSO,
            pasajeros = 2,
            detallePasajeros = listOf(Pasajero("Ana", "ES")),
            inclusiones = listOf("Almuerzo"),
            participaciones = participaciones
        )

        val entidad = dto(statusPL = "CF").aEntidadLocal(existente)

        assertEquals(EstadoServicio.EN_CURSO, entidad.estado)
        assertEquals(participaciones, entidad.participaciones)
        assertEquals(listOf(Pasajero("Ana", "ES")), entidad.detallePasajeros)
        assertEquals(listOf("Almuerzo"), entidad.inclusiones)
    }

    @Test
    fun valoresPorDefecto_camposNulos() {
        val entidad = dto(id = 5).aEntidadLocal(null)

        assertEquals("5", entidad.id)
        assertEquals("Por definir -> Por definir", entidad.origenDestino)
        assertEquals("Por definir", entidad.pickup)
        assertEquals("", entidad.dropoff)
        assertEquals(1, entidad.pasajeros)
        assertEquals("Sin Agencia", entidad.agencia)
        assertEquals("Sin hotel", entidad.hotel)
        assertEquals("Por informar", entidad.vehiculo)
        assertEquals(0, entidad.capacidadVehiculo)
        assertEquals(CodigoOficina.SCL, entidad.oficina)
        assertEquals(CodigoSrv.TR, entidad.srv)
        assertEquals(TipoServicio.TRANSFER, entidad.tipo)
        assertEquals(EstadoTourplan.RQ, entidad.estadoTourplan)
        assertNull(entidad.idiomaRequerido)
        assertNull(entidad.tipoVehiculo)
        assertNull(entidad.conductorId)
        assertNull(entidad.guiaId)
        assertEquals("5", entidad.reservaId)
        assertEquals("", entidad.fecha)
        assertEquals("", entidad.hora)
        assertEquals("", entidad.observaciones)
        assertTrue(entidad.participaciones.isEmpty())
    }

    @Test
    fun mapeoCompleto_camposRemotos() {
        val remoto = ServicioDto(
            id = 321,
            codigo = "PUQ-321",
            srv = "GS",
            tipo = "GUIDED_SERVICE",
            fecha = "2026-12-01",
            horaPickup = "08:30",
            descripcionPadre = "Excursión",
            descripcion = "Torres del Paine",
            estadoServicio = "CF",
            pickup = "Hotel Lago",
            dropoff = "Aeropuerto",
            notas = "Llevar agua",
            oficina = OficinaDto(codigo = "PUQ", nombre = "Punta Arenas"),
            idiomaRequerido = IdiomaDto(codigo = "EN", nombre = "Inglés"),
            asignacion = AsignacionDto(statusPL = "CF"),
            reserva = ReservaDto(referencia = "RES-9"),
            cliente = ClienteDto(nombre = "Agencia Andes"),
            pax = PaxDto(total = 7),
            guia = PersonaRefDto(id = 12),
            transportista = PersonaRefDto(id = 20),
            vehiculo = VehiculoDto(patente = "ABCD12", tipo = "VAN", capacidad = 9)
        )

        val entidad = remoto.aEntidadLocal(null)

        assertEquals("321", entidad.id)
        assertEquals("Excursión · Torres del Paine", entidad.titulo)
        assertEquals("Hotel Lago -> Aeropuerto", entidad.origenDestino)
        assertEquals("2026-12-01", entidad.fecha)
        assertEquals("08:30", entidad.hora)
        assertEquals(EstadoServicio.CONFIRMADO, entidad.estado)
        assertEquals(7, entidad.pasajeros)
        assertEquals("20", entidad.conductorId)
        assertEquals("12", entidad.guiaId)
        assertEquals("RES-9", entidad.reservaId)
        assertEquals(CodigoSrv.GS, entidad.srv)
        assertEquals(TipoServicio.GUIDED_SERVICE, entidad.tipo)
        assertEquals(EstadoTourplan.CF, entidad.estadoTourplan)
        assertEquals(CodigoIdioma.EN, entidad.idiomaRequerido)
        assertEquals("Agencia Andes", entidad.agencia)
        assertEquals("Hotel Lago", entidad.hotel)
        assertEquals("ABCD12", entidad.vehiculo)
        assertEquals(TipoVehiculo.VAN, entidad.tipoVehiculo)
        assertEquals(9, entidad.capacidadVehiculo)
        assertEquals(CodigoOficina.PUQ, entidad.oficina)
        assertEquals(StatusPL.CF, entidad.statusPL)
        assertEquals("Llevar agua", entidad.observaciones)
        assertEquals(2, entidad.participaciones.size)
        assertEquals(setOf("20", "12"), entidad.participaciones.map { it.usuarioId }.toSet())
        assertTrue(entidad.participaciones.all { it.estado == EstadoServicio.CONFIRMADO })
    }
}
