package com.example.linceops.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelosLogicaTest {

    @Test
    fun codigoOficina_fromApi() {
        assertEquals(CodigoOficina.SCL, CodigoOficina.fromApi("SCL"))
        assertEquals(CodigoOficina.PUX, CodigoOficina.fromApi("pux"))
        assertEquals(CodigoOficina.PUQ, CodigoOficina.fromApi("Puq"))
        assertEquals(CodigoOficina.SCL, CodigoOficina.fromApi(" PUQ "))
        assertEquals(CodigoOficina.SCL, CodigoOficina.fromApi("desconocido"))
        assertEquals(CodigoOficina.SCL, CodigoOficina.fromApi(null))
    }

    @Test
    fun tipoServicio_fromApi() {
        assertEquals(TipoServicio.TRANSFER, TipoServicio.fromApi("TRANSFER"))
        assertEquals(TipoServicio.GUIDED_SERVICE, TipoServicio.fromApi("guided_service"))
        assertEquals(TipoServicio.EXCURSION, TipoServicio.fromApi("Excursion"))
        assertEquals(TipoServicio.TRANSFER, TipoServicio.fromApi(" EXCURSION "))
        assertEquals(TipoServicio.TRANSFER, TipoServicio.fromApi("otro"))
        assertEquals(TipoServicio.TRANSFER, TipoServicio.fromApi(null))
    }

    @Test
    fun codigoSrv_fromApi() {
        assertEquals(CodigoSrv.TR, CodigoSrv.fromApi("TR"))
        assertEquals(CodigoSrv.GS, CodigoSrv.fromApi("gs"))
        assertEquals(CodigoSrv.EX, CodigoSrv.fromApi("Ex"))
        assertEquals(CodigoSrv.TR, CodigoSrv.fromApi(" EX "))
        assertEquals(CodigoSrv.TR, CodigoSrv.fromApi("ZZ"))
        assertEquals(CodigoSrv.TR, CodigoSrv.fromApi(null))
    }

    @Test
    fun estadoTourplan_fromApi() {
        assertEquals(EstadoTourplan.CF, EstadoTourplan.fromApi("CF"))
        assertEquals(EstadoTourplan.RQ, EstadoTourplan.fromApi("rq"))
        assertEquals(EstadoTourplan.WL, EstadoTourplan.fromApi("Wl"))
        assertEquals(EstadoTourplan.XX, EstadoTourplan.fromApi("xx"))
        assertEquals(EstadoTourplan.RQ, EstadoTourplan.fromApi(" XX "))
        assertEquals(EstadoTourplan.RQ, EstadoTourplan.fromApi("otro"))
        assertEquals(EstadoTourplan.RQ, EstadoTourplan.fromApi(null))
    }

    @Test
    fun statusPL_fromApi() {
        assertEquals(StatusPL.CF, StatusPL.fromApi("CF"))
        assertEquals(StatusPL.RQ, StatusPL.fromApi("rq"))
        assertEquals(StatusPL.DL, StatusPL.fromApi("Dl"))
        assertEquals(StatusPL.CX, StatusPL.fromApi("cx"))
        assertNull(StatusPL.fromApi(" CX "))
        assertNull(StatusPL.fromApi("otro"))
        assertNull(StatusPL.fromApi(null))
    }

    @Test
    fun codigoIdioma_fromApi() {
        assertEquals(CodigoIdioma.ES, CodigoIdioma.fromApi("ES"))
        assertEquals(CodigoIdioma.EN, CodigoIdioma.fromApi("en"))
        assertEquals(CodigoIdioma.PT, CodigoIdioma.fromApi("Pt"))
        assertEquals(CodigoIdioma.DE, CodigoIdioma.fromApi("de"))
        assertEquals(CodigoIdioma.FR, CodigoIdioma.fromApi("fr"))
        assertNull(CodigoIdioma.fromApi(" DE "))
        assertNull(CodigoIdioma.fromApi("otro"))
        assertNull(CodigoIdioma.fromApi(null))
    }

    @Test
    fun tipoVehiculo_fromApi() {
        assertEquals(TipoVehiculo.SEDAN, TipoVehiculo.fromApi("SEDAN"))
        assertEquals(TipoVehiculo.VAN, TipoVehiculo.fromApi("van"))
        assertEquals(TipoVehiculo.MINIBUS, TipoVehiculo.fromApi("Minibus"))
        assertEquals(TipoVehiculo.BUS, TipoVehiculo.fromApi("bus"))
        assertNull(TipoVehiculo.fromApi(" BUS "))
        assertNull(TipoVehiculo.fromApi("otro"))
        assertNull(TipoVehiculo.fromApi(null))
    }

    private fun servicioConParticipaciones() = Servicio(
        id = "100",
        titulo = "Traslado",
        origenDestino = "Hotel -> Aeropuerto",
        fecha = "2026-11-20",
        hora = "09:00",
        pickup = "Hotel",
        estado = EstadoServicio.CONFIRMADO,
        pasajeros = 2,
        participaciones = listOf(
            ParticipacionServicio(usuarioId = "20", estado = EstadoServicio.EN_CURSO),
            ParticipacionServicio(usuarioId = "12", estado = EstadoServicio.RECHAZADO)
        )
    )

    @Test
    fun estadoPara_administradorUsaEstadoGeneral() {
        val servicio = servicioConParticipaciones()
        val admin = Administrador("A301", "Ana", "admin@linceops.cl")
        assertEquals(EstadoServicio.CONFIRMADO, servicio.estadoPara(admin))
    }

    @Test
    fun estadoPara_conductorUsaSuParticipacion() {
        val servicio = servicioConParticipaciones()
        val conductor = Conductor("20", "Matías", "driver@linceops.cl", "A-2")
        assertEquals(EstadoServicio.EN_CURSO, servicio.estadoPara(conductor))
    }

    @Test
    fun estadoPara_guiaUsaSuParticipacion() {
        val servicio = servicioConParticipaciones()
        val guia = Guia("12", "Camila", "guia@linceops.cl")
        assertEquals(EstadoServicio.RECHAZADO, servicio.estadoPara(guia))
    }

    @Test
    fun estadoPara_sinParticipacionUsaEstadoGeneral() {
        val servicio = servicioConParticipaciones()
        val guia = Guia("99", "Otra", "otra@linceops.cl")
        assertEquals(EstadoServicio.CONFIRMADO, servicio.estadoPara(guia))
    }

    @Test
    fun estaAsignadoA() {
        val servicio = servicioConParticipaciones()
        assertTrue(servicio.estaAsignadoA("20"))
        assertTrue(servicio.estaAsignadoA("12"))
        assertFalse(servicio.estaAsignadoA("99"))
    }
}
