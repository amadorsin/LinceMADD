package com.example.linceops.repository

import com.example.linceops.data.local.EstadoSyncHito
import com.example.linceops.data.local.HitoPendienteEntity
import com.example.linceops.fakes.FakeHitoPendienteDao
import com.example.linceops.fakes.FakeBackendApi
import com.example.linceops.fakes.FakeSouthboundApi
import com.example.linceops.fakes.FakeServicioDao
import com.example.linceops.fakes.respuestaError
import com.example.linceops.model.Administrador
import com.example.linceops.model.Conductor
import com.example.linceops.model.EstadoServicio
import com.example.linceops.model.Guia
import com.example.linceops.model.Marcaje
import com.example.linceops.model.ParticipacionServicio
import com.example.linceops.model.Servicio
import com.example.linceops.network.BackendRetrofitClient
import com.example.linceops.network.dto.AuthResponseDto
import com.example.linceops.network.dto.BloqueoCalendarioDto
import com.example.linceops.network.dto.GeocercaDto
import com.example.linceops.network.dto.HitoDto
import com.example.linceops.network.dto.IncidenciaDto
import com.example.linceops.network.dto.ResumenDto
import com.example.linceops.network.dto.RespuestaSimpleDto
import com.example.linceops.network.dto.ServicioDto
import com.example.linceops.network.dto.PersonaRefDto
import com.example.linceops.network.dto.PaxDto
import com.example.linceops.network.dto.OficinaDto
import com.example.linceops.network.dto.AsignacionDto
import com.example.linceops.network.dto.VehiculoDto
import com.example.linceops.network.dto.ClienteDto
import com.example.linceops.network.dto.ReservaDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ServiceRepositoryTest {

    private val dao = FakeServicioDao()
    private val hitoDao = FakeHitoPendienteDao()
    private val backend = FakeBackendApi()
    private val southbound = FakeSouthboundApi()
    private var ahoraMillis = 2_000L
    private val repository = ServiceRepository(
        dao,
        hitoDao,
        ahora = { ahoraMillis },
        backendApi = backend,
        southboundApi = southbound
    )

    private fun servicio(
        id: String = "100",
        estado: EstadoServicio = EstadoServicio.PENDIENTE,
        participaciones: List<ParticipacionServicio> = listOf(
            ParticipacionServicio(usuarioId = "20", estado = estado)
        )
    ) = Servicio(
        id = id,
        titulo = "Traslado",
        origenDestino = "Hotel -> Aeropuerto",
        fecha = "2026-11-20",
        hora = "09:00",
        pickup = "Hotel",
        estado = estado,
        pasajeros = 2,
        participaciones = participaciones
    )

    private fun hito(
        clave: String,
        estado: String,
        error: String? = null
    ) = HitoPendienteEntity(
        claveIdempotencia = clave,
        servicioId = "100",
        usuarioId = "20",
        tipo = "CHECK_IN",
        latitud = -41.47,
        longitud = -72.94,
        timestampIso = "2026-11-20T09:00:00",
        pickup = "Hotel",
        oficina = "SCL",
        horaPickupIso = null,
        estadoSync = estado,
        ultimoError = error
    )

    private fun dtoServicio(
        id: Int = 100,
        statusPL: String? = "RQ",
        transportistaId: Int? = 20,
        guiaId: Int? = null
    ) = ServicioDto(
        id = id,
        codigo = "SRV$id",
        srv = "TR",
        tipo = "TRANSFER",
        fecha = "2026-11-20",
        horaPickup = "09:00",
        descripcion = "Traslado",
        pickup = "Hotel",
        dropoff = "Aeropuerto",
        oficina = OficinaDto(codigo = "SCL"),
        asignacion = AsignacionDto(statusPL = statusPL),
        transportista = transportistaId?.let { PersonaRefDto(id = it) },
        guia = guiaId?.let { PersonaRefDto(id = it) },
        pax = PaxDto(total = 2),
        reserva = ReservaDto(referencia = "RES$id"),
        cliente = ClienteDto(nombre = "Agencia"),
        vehiculo = VehiculoDto(patente = "ABCD12", tipo = "VAN", capacidad = 8)
    )

    @Test
    fun autenticar_correosDelSistema() = runBlocking {
        assertTrue(repository.autenticar("driver@linceops.cl") is Conductor)
        assertTrue(repository.autenticar("guia@linceops.cl") is Guia)
        assertEquals("A301", repository.autenticar("admin@linceops.cl")?.id)
        assertTrue(repository.autenticar("  DRIVER@LINCEOPS.CL  ") is Conductor)
        assertEquals("12", repository.autenticar("GUIA@LINCEOPS.CL")?.id)
    }

    @Test
    fun autenticar_aliasDeRol() = runBlocking {
        assertTrue(repository.autenticar("driver") is Conductor)
        assertTrue(repository.autenticar("conductor") is Conductor)
        assertTrue(repository.autenticar("guia") is Guia)
        assertEquals("A301", repository.autenticar("manager")?.id)
        assertEquals("A301", repository.autenticar("administrador")?.id)
    }

    @Test
    fun autenticar_credencialInvalidaDevuelveNull() = runBlocking {
        assertNull(repository.autenticar("nadie@linceops.cl"))
        assertNull(repository.autenticar(""))
        assertNull(repository.autenticar("   "))
    }

    @Test
    fun obtenerServicioPorId() = runBlocking {
        dao.guardarServicio(servicio())
        assertNotNull(repository.obtenerServicioPorId("100"))
        assertNull(repository.obtenerServicioPorId("999"))
    }

    @Test
    fun inicializarDatosSiVacio_noAlteraLaBaseLocal() = runBlocking {
        dao.guardarServicio(servicio())
        repository.inicializarDatosSiVacio()
        assertEquals(1, dao.snapshot().size)
    }

    @Test
    fun observarHitosPendientes_emiteElEstadoDelDao() = runBlocking {
        hitoDao.insertarOActualizar(hito("100_20_CHECK_IN", EstadoSyncHito.PENDIENTE))
        val lista = withTimeout(2_000) { repository.observarHitosPendientes().first() }
        assertEquals(1, lista.size)
        assertEquals("100_20_CHECK_IN", lista.first().claveIdempotencia)
    }

    @Test
    fun hayHitosPendientes_ignoraErroresDeValidacion() = runBlocking {
        assertFalse(repository.hayHitosPendientes())

        hitoDao.insertarOActualizar(hito("a", EstadoSyncHito.ERROR_VALIDACION, "Fuera de geocerca"))
        assertFalse(repository.hayHitosPendientes())

        hitoDao.insertarOActualizar(hito("b", EstadoSyncHito.PENDIENTE))
        assertTrue(repository.hayHitosPendientes())
    }

    @Test
    fun limpiarErroresSync_eliminaSoloErroresDeValidacion() = runBlocking {
        hitoDao.insertarOActualizar(hito("ok", EstadoSyncHito.PENDIENTE))
        hitoDao.insertarOActualizar(hito("error", EstadoSyncHito.ERROR_VALIDACION, "Rechazado"))

        repository.limpiarErroresSync()

        val restantes = hitoDao.estado.value
        assertEquals(1, restantes.size)
        assertEquals("ok", restantes.first().claveIdempotencia)
    }

    @Test
    fun insertarHito_dosVecesMismaClave_esIdempotente() = runBlocking {
        hitoDao.insertarOActualizar(hito("100_20_CHECK_IN", EstadoSyncHito.PENDIENTE))
        hitoDao.insertarOActualizar(
            hito("100_20_CHECK_IN", EstadoSyncHito.SINCRONIZANDO, "Reintentando")
        )

        assertEquals(1, hitoDao.estado.value.size)
        val unico = hitoDao.estado.value.first()
        assertEquals(EstadoSyncHito.SINCRONIZANDO, unico.estadoSync)
        assertEquals("Reintentando", unico.ultimoError)
    }

    @Test
    fun cambiarEstadoServicio_servicioInexistente() = runBlocking {
        val resultado = repository.cambiarEstadoServicio(
            "999", "20", EstadoServicio.CONFIRMADO
        )
        assertTrue(resultado.isFailure)
        assertEquals("El servicio no existe.", resultado.exceptionOrNull()?.message)
    }

    @Test
    fun cambiarEstadoServicio_usuarioNoAsignado() = runBlocking {
        dao.guardarServicio(servicio())
        val resultado = repository.cambiarEstadoServicio(
            "100", "99", EstadoServicio.CONFIRMADO
        )
        assertTrue(resultado.isFailure)
        assertEquals("No estás asignado a este servicio.", resultado.exceptionOrNull()?.message)
    }

    @Test
    fun cambiarEstadoServicio_transicionInvalidaDesdePendiente() = runBlocking {
        dao.guardarServicio(servicio(estado = EstadoServicio.PENDIENTE))
        val resultado = repository.cambiarEstadoServicio(
            "100", "20", EstadoServicio.EN_CURSO,
            latitud = -41.47, longitud = -72.94
        )
        assertTrue(resultado.isFailure)
        assertEquals("Cambio de estado no permitido.", resultado.exceptionOrNull()?.message)
    }

    @Test
    fun cambiarEstadoServicio_transicionInvalidaDesdeConfirmado() = runBlocking {
        dao.guardarServicio(servicio(estado = EstadoServicio.CONFIRMADO))
        val resultado = repository.cambiarEstadoServicio(
            "100", "20", EstadoServicio.FINALIZADO,
            latitud = -41.47, longitud = -72.94
        )
        assertTrue(resultado.isFailure)
        assertEquals("Cambio de estado no permitido.", resultado.exceptionOrNull()?.message)
    }

    @Test
    fun cambiarEstadoServicio_transicionInvalidaDesdeFinalizado() = runBlocking {
        dao.guardarServicio(servicio(estado = EstadoServicio.FINALIZADO))
        val resultado = repository.cambiarEstadoServicio(
            "100", "20", EstadoServicio.EN_CURSO,
            latitud = -41.47, longitud = -72.94
        )
        assertTrue(resultado.isFailure)
        assertEquals("Cambio de estado no permitido.", resultado.exceptionOrNull()?.message)
    }

    @Test
    fun cambiarEstadoServicio_checkOutSinCheckIn() = runBlocking {
        dao.guardarServicio(servicio(estado = EstadoServicio.EN_CURSO))
        val resultado = repository.cambiarEstadoServicio(
            "100", "20", EstadoServicio.FINALIZADO,
            latitud = -41.47, longitud = -72.94
        )
        assertTrue(resultado.isFailure)
        assertEquals("Primero debes registrar el check-in.", resultado.exceptionOrNull()?.message)
    }

    @Test
    fun cambiarEstadoServicio_salidaAnteriorALaEntrada() = runBlocking {
        ahoraMillis = 500L
        dao.guardarServicio(
            servicio(
                estado = EstadoServicio.EN_CURSO,
                participaciones = listOf(
                    ParticipacionServicio(
                        usuarioId = "20",
                        estado = EstadoServicio.EN_CURSO,
                        checkIn = Marcaje(1_000L, -41.47, -72.94)
                    )
                )
            )
        )
        val resultado = repository.cambiarEstadoServicio(
            "100", "20", EstadoServicio.FINALIZADO,
            latitud = -41.47, longitud = -72.94
        )
        assertTrue(resultado.isFailure)
        assertEquals(
            "La salida no puede ser anterior a la entrada.",
            resultado.exceptionOrNull()?.message
        )
    }

    @Test
    fun cambiarEstadoServicio_marcajeSinUbicacion() = runBlocking {
        dao.guardarServicio(servicio(estado = EstadoServicio.EN_CURSO))
        val resultado = repository.cambiarEstadoServicio(
            "100", "20", EstadoServicio.FINALIZADO
        )
        assertTrue(resultado.isFailure)
        assertEquals(
            "Se necesita una ubicación válida para registrar el marcaje.",
            resultado.exceptionOrNull()?.message
        )
    }

    @Test
    fun cambiarEstadoServicio_ubicacionNoFinita() = runBlocking {
        dao.guardarServicio(servicio(estado = EstadoServicio.EN_CURSO))
        val resultado = repository.cambiarEstadoServicio(
            "100", "20", EstadoServicio.FINALIZADO,
            latitud = Double.NaN, longitud = -72.94
        )
        assertTrue(resultado.isFailure)
        assertEquals(
            "Se necesita una ubicación válida para registrar el marcaje.",
            resultado.exceptionOrNull()?.message
        )
    }

    @Test
    fun reasignarServicio_inexistente() = runBlocking {
        val resultado = repository.reasignarServicio("999")
        assertTrue(resultado.isFailure)
        assertEquals("El servicio no existe.", resultado.exceptionOrNull()?.message)
    }

    @Test
    fun reasignarServicio_reiniciaParticipacionesYEstado() = runBlocking {
        dao.guardarServicio(
            servicio(
                estado = EstadoServicio.RECHAZADO,
                participaciones = listOf(
                    ParticipacionServicio(
                        usuarioId = "20",
                        estado = EstadoServicio.RECHAZADO,
                        motivoRechazo = "Sin disponibilidad"
                    )
                )
            )
        )

        val resultado = repository.reasignarServicio("100")

        assertTrue(resultado.isSuccess)
        val actualizado = resultado.getOrThrow()
        assertEquals(EstadoServicio.CONFIRMADO, actualizado.estado)
        assertEquals(EstadoServicio.CONFIRMADO, actualizado.participaciones.first().estado)
        assertNull(actualizado.participaciones.first().motivoRechazo)
        assertEquals(actualizado, dao.snapshot().first())
    }

    @Test
    fun obtenerServicios_usuarioDesconocido_noConsultaLaRed() = runBlocking {
        val (lista, esOffline) = repository.obtenerServicios("999")
        assertTrue(lista.isEmpty())
        assertFalse(esOffline)
    }

    @Test
    fun autenticarConBackend_mapeaLosTresRolesYGuardaToken() = runBlocking {
        val respuestas = listOf(
            "CONDUCTOR" to Conductor::class.java,
            "GUIA" to Guia::class.java,
            "ADMINISTRADOR" to Administrador::class.java
        )
        respuestas.forEachIndexed { index, (rol, clase) ->
            backend.respuestaLogin = {
                retrofit2.Response.success(
                    AuthResponseDto("token-$rol", "id-$index", "Usuario $rol", rol)
                )
            }
            val resultado = repository.autenticarConBackend(" usuario@linceops.cl ", "pw")
            assertTrue(resultado.isSuccess)
            assertTrue(clase.isInstance(resultado.getOrThrow()))
            assertEquals("token-$rol", BackendRetrofitClient.jwtToken)
        }
        assertEquals("usuario@linceops.cl", backend.llamadasLogin.last().correo)
    }

    @Test
    fun autenticarConBackend_retornaErrorHttpYExcepcionDeRed() = runBlocking {
        backend.respuestaLogin = { respuestaError(401, "No autorizado") }
        val noAutorizado = repository.autenticarConBackend("a@b.cl", "x")
        assertTrue(noAutorizado.isFailure)
        assertEquals("No autorizado", noAutorizado.exceptionOrNull()?.message)

        backend.respuestaLogin = { respuestaError(400) }
        val solicitudInvalida = repository.autenticarConBackend("a@b.cl", "x")
        assertTrue(solicitudInvalida.isFailure)
        assertTrue(solicitudInvalida.exceptionOrNull()?.message.orEmpty().contains("HTTP 400"))

        backend.respuestaLogin = { throw IOException("sin red") }
        val sinRed = repository.autenticarConBackend("a@b.cl", "x")
        assertTrue(sinRed.isFailure)
        assertEquals(
            "No se pudo conectar con el backend de autenticación.",
            sinRed.exceptionOrNull()?.message
        )
    }

    @Test
    fun obtenerServicios_sincronizaSouthboundYFiltraPorAsignacion() = runBlocking {
        southbound.servicios = listOf(dtoServicio())
        val (lista, offline) = repository.obtenerServicios("20", oficina = "SCL", q = "traslado")

        assertFalse(offline)
        assertEquals(listOf("100"), lista.map { it.id })
        assertEquals("100", dao.snapshot().single().id)
        assertEquals(20, southbound.ultimaConsultaServicios?.transportistaId)
        assertEquals("SCL", southbound.ultimaConsultaServicios?.oficina)
        assertEquals("traslado", southbound.ultimaConsultaServicios?.q)

        val (noAsignados, _) = repository.obtenerServicios("12")
        assertTrue(noAsignados.isEmpty())
    }

    @Test
    fun obtenerServicios_fallaSouthboundYUsaRoomOffline() = runBlocking {
        dao.guardarServicio(servicio())
        southbound.excepcionServicios = IOException("offline")

        val (lista, offline) = repository.obtenerServicios("20")

        assertTrue(offline)
        assertEquals(listOf("100"), lista.map { it.id })
    }

    @Test
    fun refrescarServicioPorId_actualizaRoomYPropagaFallos() = runBlocking {
        dao.guardarServicio(servicio(estado = EstadoServicio.CONFIRMADO))
        southbound.detalleServicio = dtoServicio(statusPL = "RQ")
        val actualizado = repository.refrescarServicioPorId("100")

        assertTrue(actualizado.isSuccess)
        assertEquals(EstadoServicio.CONFIRMADO, dao.snapshot().single().estado)
        assertEquals("Traslado", dao.snapshot().single().titulo)
        assertTrue(repository.refrescarServicioPorId("abc").isFailure)

        southbound.codigoDetalle = 404
        assertTrue(repository.refrescarServicioPorId("100").isFailure)
        southbound.codigoDetalle = 200
        southbound.detalleServicio = null
        assertTrue(repository.refrescarServicioPorId("100").isFailure)
        southbound.detalleServicio = dtoServicio()
        southbound.excepcionDetalle = IOException("sin red")
        assertTrue(repository.refrescarServicioPorId("100").isFailure)
    }

    @Test
    fun endpointsDeLecturaBackendYSouthbound_devuelvenDatos() = runBlocking {
        val geocerca = GeocercaDto(id = 1, puntoReferencia = "Hotel")
        backend.respuestaObtenerGeocercas = { retrofit2.Response.success(listOf(geocerca)) }
        assertEquals(geocerca, repository.obtenerGeocercas().getOrThrow().single())

        val bloqueo = BloqueoCalendarioDto(1, "2026-11-20", "Libre")
        backend.respuestaObtenerMisBloqueos = { retrofit2.Response.success(listOf(bloqueo)) }
        assertEquals(bloqueo, repository.obtenerMisBloqueos().getOrThrow().single())

        val hito = HitoDto(id = 2, servicioId = "100")
        backend.respuestaObtenerTodosLosHitos = { retrofit2.Response.success(listOf(hito)) }
        assertEquals(hito, repository.obtenerTodosLosHitos().getOrThrow().single())

        val incidencia = IncidenciaDto(id = 3, servicioId = "100", motivo = "Atraso")
        backend.respuestaObtenerTodasLasIncidencias = { retrofit2.Response.success(listOf(incidencia)) }
        assertEquals(incidencia, repository.obtenerTodasLasIncidencias().getOrThrow().single())

        val registrada = repository.registrarIncidencia("100", "Atraso", "Detalle").getOrThrow()
        assertEquals("Atraso", registrada.motivo)
        val resumen = ResumenDto(totalServicios = 7)
        southbound.resumen = resumen
        assertEquals(resumen, repository.obtenerResumenOperativo().getOrThrow())
        southbound.guias = listOf(com.example.linceops.network.dto.GuiaDto(id = 12))
        assertEquals(12, repository.obtenerGuiasActivos().getOrThrow().single().id)
        southbound.transportistas = listOf(com.example.linceops.network.dto.TransportistaDto(id = 20))
        assertEquals(20, repository.obtenerTransportistasActivos().getOrThrow().single().id)
    }

    @Test
    fun endpointsDeLecturaYEscritura_procesanErroresYExcepciones() = runBlocking {
        backend.respuestaObtenerGeocercas = { respuestaError(500) }
        assertTrue(repository.obtenerGeocercas().isFailure)
        backend.respuestaObtenerTodosLosHitos = { throw IOException("red") }
        assertTrue(repository.obtenerTodosLosHitos().isFailure)
        backend.respuestaObtenerTodasLasIncidencias = { respuestaError(500) }
        assertTrue(repository.obtenerTodasLasIncidencias().isFailure)
        backend.respuestaObtenerMisBloqueos = { respuestaError(401, "Token expirado") }
        assertTrue(repository.obtenerMisBloqueos().isFailure)

        backend.respuestaRegistrarIncidencia = { respuestaError(400, "Incidencia inválida") }
        assertTrue(repository.registrarIncidencia("100", "x").isFailure)
        backend.respuestaRegistrarIncidencia = { throw IOException("red") }
        assertTrue(repository.registrarIncidencia("100", "x").isFailure)

        southbound.codigoResumen = 500
        assertTrue(repository.obtenerResumenOperativo().isFailure)
        southbound.excepcionResumen = IOException("red")
        assertTrue(repository.obtenerResumenOperativo().isFailure)
        southbound.codigoGuias = 500
        assertTrue(repository.obtenerGuiasActivos().isFailure)
        southbound.excepcionGuias = IOException("red")
        assertTrue(repository.obtenerGuiasActivos().isFailure)
        southbound.codigoTransportistas = 500
        assertTrue(repository.obtenerTransportistasActivos().isFailure)
        southbound.excepcionTransportistas = IOException("red")
        assertTrue(repository.obtenerTransportistasActivos().isFailure)
    }

    @Test
    fun bloquearYDesbloquearFecha_manejaExitoRechazoYExcepcion() = runBlocking {
        val bloqueo = repository.bloquearFecha("2026-11-20", "Descanso").getOrThrow()
        assertEquals("2026-11-20", bloqueo.fechaBloqueada)
        backend.respuestaBloquearFecha = { respuestaError(400, "Fecha ocupada") }
        assertEquals("Fecha ocupada", repository.bloquearFecha("2026-11-20", "x").exceptionOrNull()?.message)
        backend.respuestaBloquearFecha = { throw IOException("red") }
        assertTrue(repository.bloquearFecha("2026-11-20", "x").isFailure)

        assertTrue(repository.desbloquearFecha("2026-11-20").isSuccess)
        assertEquals("2026-11-20", backend.llamadasDesbloqueo.last())
        backend.respuestaDesbloquearFecha = { respuestaError(404) }
        assertTrue(repository.desbloquearFecha("2026-11-20").isFailure)
        backend.respuestaDesbloquearFecha = { throw IOException("red") }
        assertTrue(repository.desbloquearFecha("2026-11-20").isFailure)
    }

    @Test
    fun cambiarEstadoServicio_confirmarYRechazarOnlineYConErrorBackend() = runBlocking {
        dao.guardarServicio(servicio())
        val confirmado = repository.cambiarEstadoServicio("100", "20", EstadoServicio.CONFIRMADO)
        assertTrue(confirmado.isSuccess)
        assertEquals("CONFIRMADO", backend.llamadasConfirmacion.last().estado)

        dao.guardarServicio(servicio())
        val rechazado = repository.cambiarEstadoServicio(
            "100", "20", EstadoServicio.RECHAZADO, motivo = "  No disponible  "
        )
        assertTrue(rechazado.isSuccess)
        assertEquals("No disponible", backend.llamadasConfirmacion.last().motivo)

        dao.guardarServicio(servicio())
        backend.respuestaRegistrarConfirmacion = { respuestaError(400, "Confirmación rechazada") }
        val errorConfirmar = repository.cambiarEstadoServicio("100", "20", EstadoServicio.CONFIRMADO)
        assertEquals("Confirmación rechazada", errorConfirmar.exceptionOrNull()?.message)
        val errorRechazar = repository.cambiarEstadoServicio("100", "20", EstadoServicio.RECHAZADO)
        assertEquals("Confirmación rechazada", errorRechazar.exceptionOrNull()?.message)
        assertTrue(hitoDao.estado.value.isEmpty())
    }

    @Test
    fun checkInYCheckOutOnlineNoEncolanHitos() = runBlocking {
        dao.guardarServicio(servicio(estado = EstadoServicio.CONFIRMADO))
        val entrada = repository.cambiarEstadoServicio(
            "100", "20", EstadoServicio.EN_CURSO, latitud = -41.47, longitud = -72.94
        )
        assertTrue(entrada.isSuccess)
        assertEquals("CHECK_IN", backend.llamadasMarcaje.last().tipo)
        assertTrue(hitoDao.estado.value.isEmpty())

        ahoraMillis = 3_000L
        val salida = repository.cambiarEstadoServicio(
            "100", "20", EstadoServicio.FINALIZADO, latitud = -41.47, longitud = -72.94
        )
        assertTrue(salida.isSuccess)
        assertEquals("CHECK_OUT", backend.llamadasMarcaje.last().tipo)
        assertTrue(hitoDao.estado.value.isEmpty())
    }

    @Test
    fun checkInErrorDeNegocioNoSeEncolaPero500YRedSeEncolanIdempotente() = runBlocking {
        dao.guardarServicio(servicio(estado = EstadoServicio.CONFIRMADO))
        backend.respuestaRegistrarMarcaje = { _, _, _, _ -> respuestaError(400, "Fuera de geocerca") }
        val rechazado = repository.cambiarEstadoServicio(
            "100", "20", EstadoServicio.EN_CURSO, latitud = -41.47, longitud = -72.94
        )
        assertTrue(rechazado.isFailure)
        assertEquals("Fuera de geocerca", rechazado.exceptionOrNull()?.message)
        assertTrue(hitoDao.estado.value.isEmpty())

        backend.respuestaRegistrarMarcaje = { _, _, _, _ -> respuestaError(500) }
        val conErrorServidor = repository.cambiarEstadoServicio(
            "100", "20", EstadoServicio.EN_CURSO, latitud = -41.47, longitud = -72.94
        )
        assertTrue(conErrorServidor.isSuccess)
        assertEquals("100_20_CHECK_IN", hitoDao.estado.value.single().claveIdempotencia)

        dao.guardarServicio(servicio(estado = EstadoServicio.CONFIRMADO))
        hitoDao.eliminar("100_20_CHECK_IN")
        backend.respuestaRegistrarMarcaje = { _, _, _, _ -> throw IOException("sin red") }
        val sinRed = repository.cambiarEstadoServicio(
            "100", "20", EstadoServicio.EN_CURSO, latitud = -41.47, longitud = -72.94
        )
        assertTrue(sinRed.isSuccess)
        assertEquals("100_20_CHECK_IN", hitoDao.estado.value.single().claveIdempotencia)
    }

    @Test
    fun cambiarEstadoServicio_cubreRechazos401403YFallbackConfirmacion() = runBlocking {
        dao.guardarServicio(servicio())
        backend.respuestaRegistrarConfirmacion = { respuestaError(403) }
        assertTrue(repository.cambiarEstadoServicio("100", "20", EstadoServicio.CONFIRMADO).isFailure)

        dao.guardarServicio(servicio())
        backend.respuestaRegistrarConfirmacion = { respuestaError(500) }
        assertTrue(repository.cambiarEstadoServicio("100", "20", EstadoServicio.CONFIRMADO).isSuccess)

        dao.guardarServicio(servicio())
        backend.respuestaRegistrarConfirmacion = { throw IOException("sin red") }
        assertTrue(repository.cambiarEstadoServicio("100", "20", EstadoServicio.RECHAZADO).isSuccess)
    }
}
