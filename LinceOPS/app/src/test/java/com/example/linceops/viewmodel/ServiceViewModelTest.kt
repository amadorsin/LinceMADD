package com.example.linceops.viewmodel

import com.example.linceops.data.local.EstadoSyncHito
import com.example.linceops.data.local.HitoPendienteEntity
import com.example.linceops.fakes.FakeHitoPendienteDao
import com.example.linceops.fakes.FakeBackendApi
import com.example.linceops.fakes.FakeSouthboundApi
import com.example.linceops.fakes.FakeServicioDao
import com.example.linceops.fakes.MainDispatcherRule
import com.example.linceops.model.Conductor
import com.example.linceops.model.EstadoServicio
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
import com.example.linceops.network.dto.ServicioDto
import com.example.linceops.network.dto.PersonaRefDto
import com.example.linceops.network.dto.OficinaDto
import com.example.linceops.network.dto.AsignacionDto
import com.example.linceops.repository.ServiceRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ProxySelector
import java.net.SocketAddress
import java.net.URI

class ServiceViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var dao: FakeServicioDao
    private lateinit var hitoDao: FakeHitoPendienteDao
    private lateinit var backend: FakeBackendApi
    private lateinit var southbound: FakeSouthboundApi
    private lateinit var viewModel: ServiceViewModel
    private var syncCalls = 0
    private val tokensGuardados = mutableListOf<String?>()
    private var proxyOriginal: ProxySelector? = null

    private object ProxySinSalida : ProxySelector() {
        override fun select(uri: URI?): List<Proxy> =
            listOf(Proxy(Proxy.Type.HTTP, InetSocketAddress("127.0.0.1", 1)))

        override fun connectFailed(uri: URI?, sa: SocketAddress?, ioe: IOException?) = Unit
    }

    @Before
    fun setUp() {
        proxyOriginal = ProxySelector.getDefault()
        ProxySelector.setDefault(ProxySinSalida)
        dao = FakeServicioDao()
        hitoDao = FakeHitoPendienteDao()
        backend = FakeBackendApi()
        southbound = FakeSouthboundApi()
        syncCalls = 0
        tokensGuardados.clear()
        val repository = ServiceRepository(
            dao = dao,
            hitoPendienteDao = hitoDao,
            backendApi = backend,
            southboundApi = southbound
        )
        viewModel = ServiceViewModel(
            dao = dao,
            hitoPendienteDao = hitoDao,
            onProgramarSync = { syncCalls++ },
            onGuardarToken = { tokensGuardados.add(it) },
            repository = repository
        )
    }

    @After
    fun tearDown() {
        ProxySelector.setDefault(proxyOriginal)
        BackendRetrofitClient.jwtToken = null
    }

    private fun esperar(timeoutMs: Long = 5_000, condicion: () -> Boolean) {
        val limite = System.currentTimeMillis() + timeoutMs
        while (!condicion() && System.currentTimeMillis() < limite) {
            Thread.sleep(10)
        }
        assertTrue("Condición no alcanzada en $timeoutMs ms", condicion())
    }

    private fun hito(clave: String, estado: String, error: String? = null) =
        HitoPendienteEntity(
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

    private fun servicioPrueba(id: String) = Servicio(
        id = id,
        titulo = "Traslado $id",
        origenDestino = "Hotel -> Aeropuerto",
        fecha = "2026-11-20",
        hora = "09:00",
        pickup = "Hotel",
        estado = EstadoServicio.PENDIENTE,
        pasajeros = 2,
        participaciones = listOf()
    )

    private fun servicioAsignado(
        id: String = "100",
        estado: EstadoServicio = EstadoServicio.PENDIENTE,
        checkIn: Marcaje? = null
    ) = servicioPrueba(id).copy(
        estado = estado,
        participaciones = listOf(
            ParticipacionServicio(usuarioId = "20", estado = estado, checkIn = checkIn)
        )
    )

    private fun servicioDto(id: Int = 100) = ServicioDto(
        id = id,
        codigo = "SRV$id",
        fecha = "2026-11-20",
        horaPickup = "09:00",
        descripcion = "Detalle remoto $id",
        pickup = "Hotel",
        dropoff = "Aeropuerto",
        oficina = OficinaDto(codigo = "SCL"),
        asignacion = AsignacionDto(statusPL = "CF"),
        transportista = PersonaRefDto(id = 20)
    )

    @Test
    fun iniciarSesion_correoValido_modoLocal() = runBlocking {
        viewModel.iniciarSesion("driver@linceops.cl")

        assertNull(viewModel.uiState.value.errorLogin)
        assertNotNull(viewModel.uiState.value.usuarioLogueado)
        assertTrue(viewModel.uiState.value.usuarioLogueado is Conductor)
        assertEquals("20", viewModel.uiState.value.usuarioLogueado?.id)
    }

    @Test
    fun iniciarSesion_correoInvalido_muestraErrorLogin() = runBlocking {
        viewModel.iniciarSesion("nadie@linceops.cl")

        assertNull(viewModel.uiState.value.usuarioLogueado)
        assertEquals(
            "Ingresa un correo de prueba válido.",
            viewModel.uiState.value.errorLogin
        )
    }

    @Test
    fun cerrarSesion_limpiaUsuarioTokenYNotifica() = runBlocking {
        viewModel.iniciarSesion("driver@linceops.cl")
        BackendRetrofitClient.jwtToken = "jwt-de-prueba"

        viewModel.cerrarSesion()

        assertNull(viewModel.uiState.value.usuarioLogueado)
        assertNull(BackendRetrofitClient.jwtToken)
        assertEquals(listOf<String?>(null), tokensGuardados)
        assertNull(viewModel.uiState.value.errorLogin)
    }

    @Test
    fun filtrarPorFecha_aceptaDdMmAaaaYyyy() = runBlocking {
        viewModel.filtrarPorFecha("20/11/2026")

        assertNull(viewModel.uiState.value.error)
        assertEquals("20/11/2026", viewModel.uiState.value.filtroFecha)

        viewModel.filtrarPorFecha("2026-11-20")

        assertNull(viewModel.uiState.value.error)
        assertEquals("2026-11-20", viewModel.uiState.value.filtroFecha)
    }

    @Test
    fun filtrarPorFecha_rechazaFechasInvalidas() = runBlocking {
        viewModel.filtrarPorFecha("32/13/2026")

        assertEquals(
            "Usa una fecha válida (dd/MM/yyyy o yyyy-MM-dd).",
            viewModel.uiState.value.error
        )
        assertNull(viewModel.uiState.value.filtroFecha)

        viewModel.filtrarPorFecha("abc")

        assertEquals(
            "Usa una fecha válida (dd/MM/yyyy o yyyy-MM-dd).",
            viewModel.uiState.value.error
        )
        assertNull(viewModel.uiState.value.filtroFecha)
    }

    @Test
    fun filtrarPorFecha_vaciaLimpiaElFiltro() = runBlocking {
        viewModel.filtrarPorFecha("20/11/2026")
        viewModel.filtrarPorFecha("   ")

        assertNull(viewModel.uiState.value.filtroFecha)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun filtros_estadoOficinaStatusPL_limpiar() = runBlocking {
        viewModel.filtrarPorEstado(EstadoServicio.CONFIRMADO)
        assertEquals(EstadoServicio.CONFIRMADO, viewModel.uiState.value.filtroEstado)

        viewModel.filtrarPorOficina("PUQ")
        assertEquals("PUQ", viewModel.uiState.value.filtroOficina)

        viewModel.filtrarPorStatusPL("CF")
        assertEquals("CF", viewModel.uiState.value.filtroStatusPL)

        viewModel.limpiarFiltros()

        assertNull(viewModel.uiState.value.filtroEstado)
        assertNull(viewModel.uiState.value.filtroFecha)
        assertNull(viewModel.uiState.value.filtroOficina)
        assertNull(viewModel.uiState.value.filtroStatusPL)
        assertNull(viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.mensaje)
    }

    @Test
    fun crearNuevoServicio_persisteYMuestraMensaje() = runBlocking {
        viewModel.crearNuevoServicio(
            titulo = "Tour Frutillar",
            origenDestino = "Puerto Varas -> Frutillar",
            fecha = "2026-11-20",
            hora = "09:00",
            tipo = "GUIDED_SERVICE",
            pasajeros = 3
        )

        val creado = dao.snapshot().single()
        assertEquals("Tour Frutillar", creado.titulo)
        assertEquals("Puerto Varas -> Frutillar", creado.origenDestino)
        assertEquals(3, creado.pasajeros)
        assertEquals(EstadoServicio.PENDIENTE, creado.estado)
        assertEquals("Servicio creado exitosamente", viewModel.uiState.value.mensaje)
    }

    @Test
    fun obtenerServicio_loEncuentraTrasCargarServicios() = runBlocking {
        viewModel.iniciarSesion("admin@linceops.cl")
        dao.guardarServicio(servicioPrueba("777"))

        viewModel.cargarServicios()
        esperar { viewModel.uiState.value.serviciosTotales.any { it.id == "777" } }

        assertEquals("777", viewModel.obtenerServicio("777")?.id)
        assertNull(viewModel.obtenerServicio("000"))
    }

    @Test
    fun flujoOffline_actualizaContadoresYError() = runBlocking {
        hitoDao.insertarOActualizar(hito("pendiente", EstadoSyncHito.PENDIENTE))
        hitoDao.insertarOActualizar(hito("sincronizando", EstadoSyncHito.SINCRONIZANDO))
        hitoDao.insertarOActualizar(
            hito("error", EstadoSyncHito.ERROR_VALIDACION, "Fuera de geocerca (150m)")
        )

        esperar {
            val estado = viewModel.uiState.value
            estado.hitosPendientesSync == 2 &&
                    estado.sincronizandoOffline &&
                    estado.errorSyncOffline == "Fuera de geocerca (150m)"
        }
    }

    @Test
    fun sincronizarAhora_invocaProgramarSync() = runBlocking {
        viewModel.sincronizarAhora()
        assertEquals(1, syncCalls)
    }

    @Test
    fun descartarErrorSync_limpiaErroresDeValidacion() = runBlocking {
        hitoDao.insertarOActualizar(hito("pendiente", EstadoSyncHito.PENDIENTE))
        hitoDao.insertarOActualizar(
            hito("error", EstadoSyncHito.ERROR_VALIDACION, "Fuera de geocerca (150m)")
        )
        esperar { viewModel.uiState.value.errorSyncOffline != null }

        viewModel.descartarErrorSync()

        esperar {
            viewModel.uiState.value.errorSyncOffline == null &&
                    viewModel.uiState.value.hitosPendientesSync == 1
        }
        assertEquals(1, hitoDao.estado.value.size)
        assertEquals("pendiente", hitoDao.estado.value.first().claveIdempotencia)
    }

    @Test
    fun cambiarEstadoServicio_sinSesion_muestraError() = runBlocking {
        viewModel.cambiarEstadoServicio("100", EstadoServicio.CONFIRMADO)

        assertEquals("Primero debes iniciar sesión.", viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.mensaje)
    }

    @Test
    fun limpiarMensajes_limpiaTodosLosCampos() = runBlocking {
        viewModel.iniciarSesion("credencial-invalida")
        viewModel.filtrarPorFecha("abc")

        assertNotNull(viewModel.uiState.value.errorLogin)
        assertNotNull(viewModel.uiState.value.error)

        viewModel.limpiarMensajes()

        assertNull(viewModel.uiState.value.mensaje)
        assertNull(viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.errorLogin)
    }

    @Test
    fun iniciarSesionBackend_driverCargaDatosGuardaTokenYProgramaPendientes() = runBlocking {
        backend.respuestaLogin = {
            retrofit2.Response.success(AuthResponseDto("jwt-driver", "20", "Driver", "CONDUCTOR"))
        }
        backend.respuestaObtenerMisBloqueos = {
            retrofit2.Response.success(listOf(BloqueoCalendarioDto(1, "2026-11-20", "Libre")))
        }
        backend.respuestaObtenerGeocercas = {
            retrofit2.Response.success(listOf(GeocercaDto(id = 1, puntoReferencia = "Hotel")))
        }
        southbound.servicios = listOf(servicioDto())
        hitoDao.insertarOActualizar(hito("pendiente", EstadoSyncHito.PENDIENTE))

        viewModel.iniciarSesion("driver@linceops.cl", "secreto")

        assertTrue(viewModel.uiState.value.usuarioLogueado is Conductor)
        assertEquals(listOf("100"), viewModel.uiState.value.servicios.map { it.id })
        assertEquals(listOf("20/11/2026"), viewModel.uiState.value.diasBloqueados)
        assertEquals("Hotel", viewModel.uiState.value.geocercas.single().puntoReferencia)
        assertEquals(listOf<String?>("jwt-driver"), tokensGuardados)
        assertEquals(1, syncCalls)
        assertEquals(1, southbound.llamadasServicios)
    }

    @Test
    fun iniciarSesionBackend_adminCargaCoordinacion() = runBlocking {
        backend.respuestaLogin = {
            retrofit2.Response.success(AuthResponseDto("jwt-admin", "A301", "Admin", "ADMINISTRADOR"))
        }
        backend.respuestaObtenerGeocercas = {
            retrofit2.Response.success(listOf(GeocercaDto(id = 7)))
        }
        backend.respuestaObtenerMisBloqueos = {
            retrofit2.Response.success(emptyList())
        }
        southbound.resumen = ResumenDto(totalServicios = 8)
        southbound.guias = listOf(com.example.linceops.network.dto.GuiaDto(id = 12))
        southbound.transportistas = listOf(com.example.linceops.network.dto.TransportistaDto(id = 20))
        backend.respuestaObtenerTodosLosHitos = {
            retrofit2.Response.success(listOf(HitoDto(id = 3)))
        }
        backend.respuestaObtenerTodasLasIncidencias = {
            retrofit2.Response.success(listOf(IncidenciaDto(id = 4)))
        }

        viewModel.iniciarSesion("admin@linceops.cl", "secreto")

        assertEquals("A301", viewModel.uiState.value.usuarioLogueado?.id)
        assertEquals(8, viewModel.uiState.value.resumenOperativo?.totalServicios)
        assertEquals(12, viewModel.uiState.value.guiasSouthbound.single().id)
        assertEquals(20, viewModel.uiState.value.transportistasSouthbound.single().id)
        assertEquals(3L, viewModel.uiState.value.hitosCoordinacion.single().id)
        assertEquals(4L, viewModel.uiState.value.incidenciasCoordinacion.single().id)
        assertEquals(listOf<String?>("jwt-admin"), tokensGuardados)
        assertEquals(1, southbound.llamadasServicios)
    }

    @Test
    fun iniciarSesionBackend_fallidoExponeErrorLogin() = runBlocking {
        backend.respuestaLogin = { com.example.linceops.fakes.respuestaError(401, "Credenciales inválidas") }

        viewModel.iniciarSesion("driver@linceops.cl", "incorrecta")

        assertNull(viewModel.uiState.value.usuarioLogueado)
        assertEquals("Credenciales inválidas", viewModel.uiState.value.errorLogin)
        assertTrue(tokensGuardados.isEmpty())
    }

    @Test
    fun bloquearYDesbloquearFecha_actualizanEstadoYErrores() = runBlocking {
        viewModel.bloquearFecha("21/11/2026", "Descanso")
        assertEquals(listOf("21/11/2026"), viewModel.uiState.value.diasBloqueados)
        assertEquals(
            "Fecha 21/11/2026 bloqueada en el servidor.",
            viewModel.uiState.value.mensaje
        )
        assertNull(viewModel.uiState.value.error)

        viewModel.desbloquearFecha("21/11/2026")
        assertTrue(viewModel.uiState.value.diasBloqueados.isEmpty())
        assertEquals(
            "Fecha 21/11/2026 desbloqueada en el servidor.",
            viewModel.uiState.value.mensaje
        )

        backend.respuestaBloquearFecha = { com.example.linceops.fakes.respuestaError(400, "Fecha ocupada") }
        viewModel.bloquearFecha("22/11/2026")
        assertEquals("Fecha ocupada", viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.mensaje)

        backend.respuestaDesbloquearFecha = { com.example.linceops.fakes.respuestaError(404, "No existe") }
        viewModel.desbloquearFecha("21/11/2026")
        assertEquals("No existe", viewModel.uiState.value.error)
    }

    @Test
    fun cargarBloqueosGeocercasYCoordinacion() = runBlocking {
        backend.respuestaObtenerMisBloqueos = {
            retrofit2.Response.success(
                listOf(
                    BloqueoCalendarioDto(1, "2026-11-20", "A"),
                    BloqueoCalendarioDto(2, null, "B")
                )
            )
        }
        backend.respuestaObtenerGeocercas = {
            retrofit2.Response.success(listOf(GeocercaDto(id = 5)))
        }
        southbound.resumen = ResumenDto(totalServicios = 2)
        backend.respuestaObtenerTodosLosHitos = {
            retrofit2.Response.success(listOf(HitoDto(id = 6)))
        }
        backend.respuestaObtenerTodasLasIncidencias = {
            retrofit2.Response.success(listOf(IncidenciaDto(id = 7)))
        }

        viewModel.iniciarSesion("admin@linceops.cl")
        viewModel.cargarBloqueos()
        viewModel.cargarGeocercas()
        viewModel.cargarDatosCoordinacion()

        assertEquals(listOf("20/11/2026"), viewModel.uiState.value.diasBloqueados)
        assertEquals(5L, viewModel.uiState.value.geocercas.single().id)
        assertEquals(2, viewModel.uiState.value.resumenOperativo?.totalServicios)
        assertEquals(6L, viewModel.uiState.value.hitosCoordinacion.single().id)
        assertEquals(7L, viewModel.uiState.value.incidenciasCoordinacion.single().id)
    }

    @Test
    fun refrescarDetalleActualizaServiciosEnEstado() = runBlocking {
        dao.guardarServicio(servicioAsignado())
        viewModel.iniciarSesion("driver@linceops.cl")
        assertEquals("100", viewModel.uiState.value.servicios.single().id)

        southbound.detalleServicio = servicioDto()
        viewModel.refrescarDetalleServicio("100")

        assertEquals("Detalle remoto 100", viewModel.uiState.value.servicios.single().titulo)
        assertEquals("Detalle remoto 100", viewModel.uiState.value.serviciosTotales.single().titulo)
    }

    @Test
    fun confirmarYRechazarServicioActualizanMensajeOnline() = runBlocking {
        dao.guardarServicio(servicioAsignado())
        viewModel.iniciarSesion("driver@linceops.cl")

        viewModel.confirmarServicio("100")
        assertEquals(EstadoServicio.CONFIRMADO, dao.snapshot().single().participaciones.single().estado)
        assertEquals("CONFIRMADO", backend.llamadasConfirmacion.last().estado)

        dao.guardarServicio(servicioAsignado(id = "101"))
        viewModel.cargarServicios()
        viewModel.rechazarServicio("101", "No puedo")
        assertEquals(EstadoServicio.RECHAZADO, dao.snapshot().first { it.id == "101" }.participaciones.single().estado)
        assertEquals("RECHAZADO", backend.llamadasConfirmacion.last().estado)
    }

    @Test
    fun registrarCheckInYCheckOutOnlineNoProgramaSync() = runBlocking {
        dao.guardarServicio(servicioAsignado(estado = EstadoServicio.CONFIRMADO))
        viewModel.iniciarSesion("driver@linceops.cl")

        viewModel.registrarCheckIn("100", -41.47, -72.94)
        assertEquals("CHECK_IN", backend.llamadasMarcaje.last().tipo)
        assertEquals(EstadoServicio.EN_CURSO, dao.snapshot().single().participaciones.single().estado)
        assertEquals(0, syncCalls)

        dao.guardarServicio(
            servicioAsignado(
                estado = EstadoServicio.EN_CURSO,
                checkIn = Marcaje(1_000L, -41.47, -72.94)
            )
        )
        viewModel.cargarServicios()
        viewModel.registrarCheckOut("100", -41.47, -72.94)
        assertEquals("CHECK_OUT", backend.llamadasMarcaje.last().tipo)
        assertEquals(0, syncCalls)
    }

    @Test
    fun registrarCheckInOfflineEncolaYProgramaSync() = runBlocking {
        dao.guardarServicio(servicioAsignado(estado = EstadoServicio.CONFIRMADO))
        backend.respuestaRegistrarMarcaje = { _, _, _, _ ->
            com.example.linceops.fakes.respuestaError(500)
        }
        viewModel.iniciarSesion("driver@linceops.cl")

        viewModel.registrarCheckIn("100", -41.47, -72.94)

        assertEquals(1, syncCalls)
        assertEquals("100_20_CHECK_IN", hitoDao.estado.value.single().claveIdempotencia)
        assertEquals(EstadoServicio.EN_CURSO, dao.snapshot().single().participaciones.single().estado)
    }

    @Test
    fun registrarCheckOutOfflineEncolaYProgramaSync() = runBlocking {
        dao.guardarServicio(
            servicioAsignado(
                estado = EstadoServicio.EN_CURSO,
                checkIn = Marcaje(1_000L, -41.47, -72.94)
            )
        )
        backend.respuestaRegistrarMarcaje = { _, _, _, _ ->
            com.example.linceops.fakes.respuestaError(500)
        }
        viewModel.iniciarSesion("driver@linceops.cl")

        viewModel.registrarCheckOut("100", -41.47, -72.94)

        assertEquals(1, syncCalls)
        assertEquals("100_20_CHECK_OUT", hitoDao.estado.value.single().claveIdempotencia)
        assertEquals(EstadoServicio.FINALIZADO, dao.snapshot().single().participaciones.single().estado)
    }

    @Test
    fun reasignarServicioActualizaMensajeEstadoYError() = runBlocking {
        dao.guardarServicio(servicioAsignado(estado = EstadoServicio.RECHAZADO))
        viewModel.iniciarSesion("admin@linceops.cl")

        viewModel.reasignarServicio("100")
        assertEquals(EstadoServicio.CONFIRMADO, dao.snapshot().single().estado)

        viewModel.reasignarServicio("999")
        assertEquals("El servicio no existe.", viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.mensaje)
    }
}
