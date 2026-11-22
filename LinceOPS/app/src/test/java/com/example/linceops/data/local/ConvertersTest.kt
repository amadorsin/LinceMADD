package com.example.linceops.data.local

import com.example.linceops.model.CodigoIdioma
import com.example.linceops.model.CodigoOficina
import com.example.linceops.model.CodigoSrv
import com.example.linceops.model.EstadoServicio
import com.example.linceops.model.EstadoTourplan
import com.example.linceops.model.Marcaje
import com.example.linceops.model.Pasajero
import com.example.linceops.model.ParticipacionServicio
import com.example.linceops.model.StatusPL
import com.example.linceops.model.TipoServicio
import com.example.linceops.model.TipoVehiculo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun estadoServicio_idaYVuelta() {
        EstadoServicio.entries.forEach { valor ->
            assertEquals(
                valor,
                converters.toEstadoServicio(converters.fromEstadoServicio(valor))
            )
        }
    }

    @Test
    fun codigoOficina_idaYVuelta() {
        CodigoOficina.entries.forEach { valor ->
            assertEquals(
                valor,
                converters.toCodigoOficina(converters.fromCodigoOficina(valor))
            )
        }
    }

    @Test
    fun tipoServicio_idaYVuelta() {
        TipoServicio.entries.forEach { valor ->
            assertEquals(
                valor,
                converters.toTipoServicio(converters.fromTipoServicio(valor))
            )
        }
    }

    @Test
    fun codigoSrv_idaYVuelta() {
        CodigoSrv.entries.forEach { valor ->
            assertEquals(valor, converters.toCodigoSrv(converters.fromCodigoSrv(valor)))
        }
    }

    @Test
    fun estadoTourplan_idaYVuelta() {
        EstadoTourplan.entries.forEach { valor ->
            assertEquals(
                valor,
                converters.toEstadoTourplan(converters.fromEstadoTourplan(valor))
            )
        }
    }

    @Test
    fun statusPL_idaYVueltaYNull() {
        StatusPL.entries.forEach { valor ->
            assertEquals(valor, converters.toStatusPL(converters.fromStatusPL(valor)))
        }
        assertNull(converters.fromStatusPL(null))
        assertNull(converters.toStatusPL(null))
    }

    @Test
    fun codigoIdioma_idaYVueltaYNull() {
        CodigoIdioma.entries.forEach { valor ->
            assertEquals(
                valor,
                converters.toCodigoIdioma(converters.fromCodigoIdioma(valor))
            )
        }
        assertNull(converters.fromCodigoIdioma(null))
        assertNull(converters.toCodigoIdioma(null))
    }

    @Test
    fun tipoVehiculo_idaYVueltaYNull() {
        TipoVehiculo.entries.forEach { valor ->
            assertEquals(
                valor,
                converters.toTipoVehiculo(converters.fromTipoVehiculo(valor))
            )
        }
        assertNull(converters.fromTipoVehiculo(null))
        assertNull(converters.toTipoVehiculo(null))
    }

    @Test
    fun listaString_idaYVuelta() {
        val original = listOf("Español", "Inglés", "Alemán")
        assertEquals(original, converters.toStringList(converters.fromStringList(original)))
    }

    @Test
    fun listaPasajeros_idaYVuelta() {
        val original = listOf(
            Pasajero("Ana Pérez", "ES"),
            Pasajero("John Smith", "EN", "Silla de ruedas")
        )
        assertEquals(original, converters.toPasajeroList(converters.fromPasajeroList(original)))
    }

    @Test
    fun listaParticipaciones_idaYVuelta() {
        val original = listOf(
            ParticipacionServicio(
                usuarioId = "20",
                estado = EstadoServicio.EN_CURSO,
                checkIn = Marcaje(1_700_000_000_000L, -41.47, -72.94)
            ),
            ParticipacionServicio(
                usuarioId = "12",
                estado = EstadoServicio.RECHAZADO,
                motivoRechazo = "Sin disponibilidad"
            )
        )
        assertEquals(
            original,
            converters.toParticipacionList(converters.fromParticipacionList(original))
        )
    }

    @Test
    fun listasVacias_idaYVuelta() {
        assertEquals(emptyList<String>(), converters.toStringList(converters.fromStringList(emptyList())))
        assertEquals(emptyList<Pasajero>(), converters.toPasajeroList(converters.fromPasajeroList(emptyList())))
        assertEquals(
            emptyList<ParticipacionServicio>(),
            converters.toParticipacionList(converters.fromParticipacionList(emptyList()))
        )
    }
}
