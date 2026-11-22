package com.example.linceops.data.sync

import com.example.linceops.data.local.EstadoSyncHito
import com.example.linceops.data.local.HitoPendienteEntity
import com.example.linceops.fakes.FakeHitoPendienteDao
import com.example.linceops.fakes.FakeServicioDao
import com.example.linceops.fakes.respuestaError
import com.example.linceops.model.EstadoServicio
import com.example.linceops.model.Marcaje
import com.example.linceops.model.ParticipacionServicio
import com.example.linceops.model.Servicio
import com.example.linceops.network.dto.MensajeResponseDto
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class SincronizarHitosWorkerTest {

    private fun hito(tipo: String = "CHECK_IN") = HitoPendienteEntity(
        claveIdempotencia = "100_20_$tipo",
        servicioId = "100",
        usuarioId = "20",
        tipo = tipo,
        latitud = -41.47,
        longitud = -72.94,
        timestampIso = "2026-11-20T09:00:00",
        pickup = "Hotel",
        oficina = "SCL",
        horaPickupIso = "2026-11-20T09:00:00"
    )

    private fun servicio(
        estado: EstadoServicio,
        participacionEstado: EstadoServicio,
        checkIn: Marcaje? = null,
        checkOut: Marcaje? = null
    ) = Servicio(
        id = "100",
        titulo = "Traslado de prueba",
        origenDestino = "Hotel -> Aeropuerto",
        fecha = "2026-11-20",
        hora = "09:00",
        pickup = "Hotel",
        estado = estado,
        pasajeros = 1,
        participaciones = listOf(
            ParticipacionServicio(
                usuarioId = "20",
                estado = participacionEstado,
                checkIn = checkIn,
                checkOut = checkOut
            )
        )
    )

    @Test
    fun respuestas200Y201EliminanDeLaColaYNoRequierenRetry() = runBlocking {
        for (codigo in listOf(200, 201)) {
            val hito = hito()
            val hitoDao = FakeHitoPendienteDao().apply { insertarOActualizar(hito) }
            val servicioDao = FakeServicioDao()

            val resultado = sincronizarHitosPendientes(
                pendientes = listOf(hito),
                dao = hitoDao,
                servicioDao = servicioDao
            ) {
                Response.success(codigo, MensajeResponseDto("Marcaje registrado"))
            }

            assertEquals(ResultadoSyncHitos.SUCCESS, resultado)
            assertTrue(hitoDao.estado.value.isEmpty())
        }
    }

    @Test
    fun error400IdempotenteEliminaDeLaColaSinRollback() = runBlocking {
        val hito = hito()
        val hitoDao = FakeHitoPendienteDao().apply { insertarOActualizar(hito) }
        val servicioDao = FakeServicioDao().apply {
            guardarServicio(
                servicio(
                    estado = EstadoServicio.EN_CURSO,
                    participacionEstado = EstadoServicio.EN_CURSO,
                    checkIn = Marcaje(1_000L, -41.47, -72.94)
                )
            )
        }
        val original = servicioDao.snapshot().single()

        val resultado = sincronizarHitosPendientes(
            pendientes = listOf(hito),
            dao = hitoDao,
            servicioDao = servicioDao
        ) {
            respuestaError(400, "El CHECK_IN ya fue registrado para este servicio.")
        }

        assertEquals(ResultadoSyncHitos.SUCCESS, resultado)
        assertTrue(hitoDao.estado.value.isEmpty())
        assertEquals(original, servicioDao.snapshot().single())
    }

    @Test
    fun error400DeCheckInMarcaErrorYRevierteCamposLocales() = runBlocking {
        val hito = hito()
        val hitoDao = FakeHitoPendienteDao().apply { insertarOActualizar(hito) }
        val servicioDao = FakeServicioDao().apply {
            guardarServicio(
                servicio(
                    estado = EstadoServicio.EN_CURSO,
                    participacionEstado = EstadoServicio.EN_CURSO,
                    checkIn = Marcaje(1_000L, -41.47, -72.94),
                    checkOut = Marcaje(2_000L, -41.47, -72.94)
                )
            )
        }

        val resultado = sincronizarHitosPendientes(
            pendientes = listOf(hito),
            dao = hitoDao,
            servicioDao = servicioDao
        ) {
            respuestaError(400, "Estás fuera de la geocerca permitida.")
        }

        val error = hitoDao.estado.value.single()
        val actualizado = servicioDao.snapshot().single()
        assertEquals(ResultadoSyncHitos.SUCCESS, resultado)
        assertEquals(EstadoSyncHito.ERROR_VALIDACION, error.estadoSync)
        assertEquals("Estás fuera de la geocerca permitida.", error.ultimoError)
        assertEquals(EstadoServicio.CONFIRMADO, actualizado.estado)
        assertEquals(EstadoServicio.CONFIRMADO, actualizado.participaciones.single().estado)
        assertNull(actualizado.participaciones.single().checkIn)
        assertNull(actualizado.participaciones.single().checkOut)
    }

    @Test
    fun error400DeCheckOutRevierteSalidaYConservaCheckIn() = runBlocking {
        val hito = hito("CHECK_OUT")
        val hitoDao = FakeHitoPendienteDao().apply { insertarOActualizar(hito) }
        val checkIn = Marcaje(1_000L, -41.47, -72.94)
        val servicioDao = FakeServicioDao().apply {
            guardarServicio(
                servicio(
                    estado = EstadoServicio.FINALIZADO,
                    participacionEstado = EstadoServicio.FINALIZADO,
                    checkIn = checkIn,
                    checkOut = Marcaje(2_000L, -41.47, -72.94)
                )
            )
        }

        sincronizarHitosPendientes(
            pendientes = listOf(hito),
            dao = hitoDao,
            servicioDao = servicioDao
        ) {
            respuestaError(400, "No puedes registrar Check-Out sin un Check-In válido previo.")
        }

        val actualizado = servicioDao.snapshot().single()
        assertEquals(EstadoServicio.EN_CURSO, actualizado.estado)
        assertEquals(EstadoServicio.EN_CURSO, actualizado.participaciones.single().estado)
        assertEquals(checkIn, actualizado.participaciones.single().checkIn)
        assertNull(actualizado.participaciones.single().checkOut)
        assertEquals(EstadoSyncHito.ERROR_VALIDACION, hitoDao.estado.value.single().estadoSync)
    }

    @Test
    fun respuestas401Y403MantienenPendienteYRequierenRetry() = runBlocking {
        for (codigo in listOf(401, 403)) {
            val hito = hito()
            val hitoDao = FakeHitoPendienteDao().apply { insertarOActualizar(hito) }
            val resultado = sincronizarHitosPendientes(
                pendientes = listOf(hito),
                dao = hitoDao,
                servicioDao = FakeServicioDao()
            ) {
                respuestaError(codigo, "Sesión expirada")
            }

            assertEquals(ResultadoSyncHitos.RETRY, resultado)
            assertEquals(EstadoSyncHito.PENDIENTE, hitoDao.estado.value.single().estadoSync)
            assertEquals(1, hitoDao.estado.value.single().intentos)
        }
    }

    @Test
    fun error500YIOExceptionMantienenPendienteYRequierenRetry() = runBlocking {
        val hito = hito()
        val hitoDao = FakeHitoPendienteDao().apply { insertarOActualizar(hito) }
        val resultado500 = sincronizarHitosPendientes(
            pendientes = listOf(hito),
            dao = hitoDao,
            servicioDao = FakeServicioDao()
        ) {
            respuestaError(500, "Error interno")
        }
        assertEquals(ResultadoSyncHitos.RETRY, resultado500)
        assertEquals(EstadoSyncHito.PENDIENTE, hitoDao.estado.value.single().estadoSync)

        val hitoRed = hito().copy(claveIdempotencia = "100_20_CHECK_IN_RED")
        val hitoDaoRed = FakeHitoPendienteDao().apply { insertarOActualizar(hitoRed) }
        val resultadoRed = sincronizarHitosPendientes(
            pendientes = listOf(hitoRed),
            dao = hitoDaoRed,
            servicioDao = FakeServicioDao()
        ) {
            throw IOException("sin conexión")
        }
        assertEquals(ResultadoSyncHitos.RETRY, resultadoRed)
        assertEquals(EstadoSyncHito.PENDIENTE, hitoDaoRed.estado.value.single().estadoSync)
        assertEquals(1, hitoDaoRed.estado.value.single().intentos)
    }
}
