package com.example.linceops.ui.screens

import com.example.linceops.network.dto.HitoDto
import com.example.linceops.network.dto.IncidenciaDto
import com.example.linceops.network.dto.UsuarioResumenDto
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardTrazabilidadTest {

    @Test
    fun gsonMapeaHitoEIncidenciaConAutorYFecha() {
        val gson = Gson()
        val hito = gson.fromJson(
            """{"id":4,"servicioId":"100","usuario":{"id":"20","nombre":"Matías","rol":"CONDUCTOR"},"tipo":"CHECK_IN","timestampRegistro":"2026-11-22T09:15:30","latitudMarcaje":-41.47,"longitudMarcaje":-72.94}""",
            HitoDto::class.java
        )
        val incidencia = gson.fromJson(
            """{"id":8,"servicioId":"100","usuario":{"id":"20","nombre":"Matías","rol":"CONDUCTOR"},"motivo":"FUERA_DE_RADIO","detalle":"Fuera de geocerca","fechaHora":"2026-11-22T09:16:00"}""",
            IncidenciaDto::class.java
        )

        assertEquals(4L, hito.id)
        assertEquals(UsuarioResumenDto("20", "Matías", "CONDUCTOR"), hito.usuario)
        assertEquals("2026-11-22T09:15:30", hito.timestampRegistro)
        assertEquals(8L, incidencia.id)
        assertEquals("Matías", incidencia.usuario?.nombre)
        assertEquals("2026-11-22T09:16:00", incidencia.fechaHora)
    }

    @Test
    fun ordenaHitosDescendentePorFechaYUsaIdParaDesempatar() {
        val lista = listOf(
            HitoDto(id = 1, timestampRegistro = "2026-01-02T09:00:00"),
            HitoDto(id = 2, timestampRegistro = "2026-03-01T10:00:00"),
            HitoDto(id = 3, timestampRegistro = "2026-03-01T10:00:00"),
            HitoDto(id = 4, timestampRegistro = null),
            HitoDto(id = 5, timestampRegistro = "fecha-invalida")
        )

        assertEquals(
            listOf(3L, 2L, 1L, 5L, 4L),
            ordenarHitosPorFechaDescendente(lista).map { it.id }
        )
    }

    @Test
    fun ordenaIncidenciasDescendenteConFechaNulaOMalformadaAlFinal() {
        val lista = listOf(
            IncidenciaDto(id = 1, fechaHora = "2026-02-01T10:00:00"),
            IncidenciaDto(id = 2, fechaHora = "2026-04-01T10:00:00"),
            IncidenciaDto(id = 3, fechaHora = null),
            IncidenciaDto(id = 4, fechaHora = "no-es-fecha")
        )

        assertEquals(
            listOf(2L, 1L, 4L, 3L),
            ordenarIncidenciasPorFechaDescendente(lista).map { it.id }
        )
    }

    @Test
    fun formateaFechaHoraYConservaValoresNulosOValoresMalformed() {
        assertEquals("09/03/2026 04:05", formatearFechaIsoCorta("2026-03-09T04:05:21"))
        assertEquals("09/03/2026 04:05", formatearFechaIsoCorta("2026-03-09 04:05"))
        assertEquals("Sin fecha", formatearFechaIsoCorta(null))
        assertEquals("Sin fecha", formatearFechaIsoCorta("   "))
        assertEquals("2026-13-40T25:61:00", formatearFechaIsoCorta("2026-13-40T25:61:00"))
        assertEquals("fecha-invalida", formatearFechaIsoCorta("fecha-invalida"))
    }
}
