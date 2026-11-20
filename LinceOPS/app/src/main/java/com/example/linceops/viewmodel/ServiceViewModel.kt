package com.example.linceops.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linceops.data.local.ServicioDao
import com.example.linceops.model.EstadoServicio
import com.example.linceops.model.Servicio
import com.example.linceops.model.TipoServicio
import com.example.linceops.network.BackendRetrofitClient
import com.example.linceops.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import com.example.linceops.model.RolUsuario

class ServiceViewModel(
    private val dao: ServicioDao
) : ViewModel() {

    private val repository = ServiceRepository(dao)
    private val _uiState = MutableStateFlow(ServiceUiState())
    val uiState: StateFlow<ServiceUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.inicializarDatosSiVacio()
        }
    }

    fun iniciarSesion(correo: String, password: String? = null) {
        if (!password.isNullOrBlank()) {
            viewModelScope.launch {
                repository.autenticarConBackend(correo, password).fold(
                    onSuccess = { usuario ->
                        _uiState.value = ServiceUiState(usuarioLogueado = usuario)
                        cargarServicios()
                        cargarBloqueos()
                        cargarGeocercas()
                        if (usuario.rol == RolUsuario.ADMINISTRADOR) {
                            cargarDatosCoordinacion()
                        }
                    },
                    onFailure = { err ->
                        _uiState.value = ServiceUiState(errorLogin = err.message ?: "Error al iniciar sesión.")
                    }
                )
            }
            return
        }

        val usuario = repository.autenticar(correo)
        _uiState.value = if (usuario == null) {
            ServiceUiState(errorLogin = "Ingresa un correo de prueba válido.")
        } else {
            ServiceUiState(usuarioLogueado = usuario)
        }
        cargarServicios()
    }
    fun cargarGeocercas() {
        viewModelScope.launch {
            repository.obtenerGeocercas().onSuccess { lista ->
                _uiState.value = _uiState.value.copy(geocercas = lista)
            }
        }
    }
    fun cerrarSesion() {
        BackendRetrofitClient.jwtToken = null
        _uiState.value = ServiceUiState()
    }

    fun cargarServicios() {
        val usuario = _uiState.value.usuarioLogueado
        if (usuario == null) {
            _uiState.value = _uiState.value.copy(servicios = emptyList())
            return
        }
        viewModelScope.launch {
            val fechaIso = _uiState.value.filtroFecha?.let { convertirAFechaIso(it) }

            val (lista, esOffline) = repository.obtenerServicios(
                usuarioId = usuario.id,
                fechaDesde = fechaIso,
                fechaHasta = fechaIso,
                oficina = _uiState.value.filtroOficina,
                statusPL = _uiState.value.filtroStatusPL
            )

            val filtrados = lista.filter { servicio ->
                (_uiState.value.filtroEstado == null || servicio.estadoPara(usuario) == _uiState.value.filtroEstado) &&
                        (fechaIso == null || servicio.fecha == fechaIso) &&
                        (_uiState.value.filtroOficina == null || servicio.oficina.name.equals(_uiState.value.filtroOficina, ignoreCase = true)) &&
                        (_uiState.value.filtroStatusPL == null || servicio.statusPL?.name.equals(_uiState.value.filtroStatusPL, ignoreCase = true))
            }

            _uiState.value = _uiState.value.copy(
                servicios = filtrados,
                serviciosTotales = lista,
                error = if (esOffline) "Sin conexión con el servidor. Trabajando en Modo Offline." else null,
                mensaje = null
            )
        }
    }
    fun cargarBloqueos() {
        if (_uiState.value.usuarioLogueado == null) return
        viewModelScope.launch {
            repository.obtenerMisBloqueos().onSuccess { lista ->
                val fechasLatam = lista.mapNotNull { dto ->
                    dto.fechaBloqueada?.let { convertirIsoALatam(it) }
                }
                _uiState.value = _uiState.value.copy(diasBloqueados = fechasLatam)
            }
        }
    }

    fun bloquearFecha(fechaLatam: String, motivo: String = "Día libre solicitado") {
        val fechaIso = convertirAFechaIso(fechaLatam)
        viewModelScope.launch {
            repository.bloquearFecha(fechaIso, motivo).fold(
                onSuccess = {
                    val actualizados = (_uiState.value.diasBloqueados + fechaLatam).distinct()
                    _uiState.value = _uiState.value.copy(
                        diasBloqueados = actualizados,
                        mensaje = "Fecha $fechaLatam bloqueada en el servidor.",
                        error = null
                    )
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        error = err.message ?: "No se pudo bloquear la fecha.",
                        mensaje = null
                    )
                }
            )
        }
    }

    fun desbloquearFecha(fechaLatam: String) {
        val fechaIso = convertirAFechaIso(fechaLatam)
        viewModelScope.launch {
            repository.desbloquearFecha(fechaIso).fold(
                onSuccess = {
                    val actualizados = _uiState.value.diasBloqueados.filterNot { it == fechaLatam }
                    _uiState.value = _uiState.value.copy(
                        diasBloqueados = actualizados,
                        mensaje = "Fecha $fechaLatam desbloqueada en el servidor.",
                        error = null
                    )
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        error = err.message ?: "No se pudo desbloquear la fecha.",
                        mensaje = null
                    )
                }
            )
        }
    }

    private fun convertirIsoALatam(fechaIso: String): String {
        return try {
            val entrada = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { isLenient = false }
            val salida = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT)
            val date = entrada.parse(fechaIso)
            if (date != null) salida.format(date) else fechaIso
        } catch (_: Exception) {
            fechaIso
        }
    }

    fun filtrarPorEstado(estado: EstadoServicio?) {
        _uiState.value = _uiState.value.copy(filtroEstado = estado, mensaje = null, error = null)
        cargarServicios()
    }

    fun filtrarPorFecha(fecha: String?) {
        val normalizada = fecha?.trim()?.takeIf { it.isNotEmpty() }
        if (normalizada != null && !fechaValida(normalizada)) {
            _uiState.value = _uiState.value.copy(error = "Usa una fecha válida (dd/MM/yyyy o yyyy-MM-dd).", mensaje = null)
            return
        }
        _uiState.value = _uiState.value.copy(filtroFecha = normalizada, error = null, mensaje = null)
        cargarServicios()
    }

    fun filtrarPorOficina(oficina: String?) {
        _uiState.value = _uiState.value.copy(filtroOficina = oficina, error = null, mensaje = null)
        cargarServicios()
    }

    fun filtrarPorStatusPL(statusPL: String?) {
        _uiState.value = _uiState.value.copy(filtroStatusPL = statusPL, error = null, mensaje = null)
        cargarServicios()
    }

    fun limpiarFiltros() {
        _uiState.value = _uiState.value.copy(
            filtroEstado = null,
            filtroFecha = null,
            filtroOficina = null,
            filtroStatusPL = null,
            error = null,
            mensaje = null
        )
        cargarServicios()
    }

    fun obtenerServicio(servicioId: String): Servicio? {
        return _uiState.value.serviciosTotales.find { it.id == servicioId }
            ?: _uiState.value.servicios.find { it.id == servicioId }
    }
    fun refrescarDetalleServicio(servicioId: String) {
        viewModelScope.launch {
            repository.refrescarServicioPorId(servicioId).onSuccess { actualizado ->
                _uiState.value = _uiState.value.copy(
                    serviciosTotales = _uiState.value.serviciosTotales.map {
                        if (it.id == actualizado.id) actualizado else it
                    },
                    servicios = _uiState.value.servicios.map {
                        if (it.id == actualizado.id) actualizado else it
                    }
                )
            }
        }
    }

    fun cargarDatosCoordinacion() {
        viewModelScope.launch {
            repository.obtenerResumenOperativo().onSuccess { resumen ->
                _uiState.value = _uiState.value.copy(resumenOperativo = resumen)
            }
            repository.obtenerGuiasActivos().onSuccess { guias ->
                _uiState.value = _uiState.value.copy(guiasSouthbound = guias)
            }
            repository.obtenerTransportistasActivos().onSuccess { transportistas ->
                _uiState.value = _uiState.value.copy(transportistasSouthbound = transportistas)
            }
            repository.obtenerTodosLosHitos().onSuccess { hitos ->
                _uiState.value = _uiState.value.copy(hitosCoordinacion = hitos)
            }
            repository.obtenerTodasLasIncidencias().onSuccess { incidencias ->
                _uiState.value = _uiState.value.copy(incidenciasCoordinacion = incidencias)
            }
        }
    }
    fun cambiarEstadoServicio(
        servicioId: String,
        nuevoEstado: EstadoServicio,
        motivo: String = "",
        latitud: Double? = null,
        longitud: Double? = null,
        hayInternet: Boolean = true
    ) {
        val usuario = _uiState.value.usuarioLogueado
        if (usuario == null) {
            _uiState.value = _uiState.value.copy(error = "Primero debes iniciar sesión.", mensaje = null)
            return
        }

        viewModelScope.launch {
            repository.cambiarEstadoServicio(
                servicioId, usuario.id, nuevoEstado, motivo, latitud, longitud
            ).fold(
                onSuccess = {
                    val msjRed = if (hayInternet) "Sincronizado con el servidor." else "Guardado localmente (Offline)."
                    _uiState.value = _uiState.value.copy(mensaje = "Estado actualizado. $msjRed", error = null)
                    cargarServicios()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        error = it.message ?: "No se pudo guardar el cambio.",
                        mensaje = null
                    )
                }
            )
        }
    }

    fun confirmarServicio(id: String) = cambiarEstadoServicio(id, EstadoServicio.CONFIRMADO)

    fun rechazarServicio(id: String, motivo: String) =
        cambiarEstadoServicio(id, EstadoServicio.RECHAZADO, motivo = motivo)

    fun registrarCheckIn(id: String, latitud: Double, longitud: Double, hayInternet: Boolean = true) =
        cambiarEstadoServicio(id, EstadoServicio.EN_CURSO, latitud = latitud, longitud = longitud, hayInternet = hayInternet)

    fun registrarCheckOut(id: String, latitud: Double, longitud: Double, hayInternet: Boolean = true) =
        cambiarEstadoServicio(id, EstadoServicio.FINALIZADO, latitud = latitud, longitud = longitud, hayInternet = hayInternet)

    fun reasignarServicio(servicioId: String) {
        val usuario = _uiState.value.usuarioLogueado
        if (usuario == null) return

        viewModelScope.launch {
            repository.reasignarServicio(servicioId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(mensaje = "Servicio reasignado a flota", error = null)
                    cargarServicios()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(error = it.message, mensaje = null)
                }
            )
        }
    }

    fun limpiarMensajes() {
        _uiState.value = _uiState.value.copy(mensaje = null, error = null, errorLogin = null)
    }

    private fun fechaValida(fecha: String): Boolean {
        val formatoIso = Regex("\\d{4}-\\d{2}-\\d{2}")
        val formatoLatam = Regex("\\d{2}/\\d{2}/\\d{4}")
        val patron = when {
            formatoIso.matches(fecha) -> "yyyy-MM-dd"
            formatoLatam.matches(fecha) -> "dd/MM/yyyy"
            else -> return false
        }
        val formato = SimpleDateFormat(patron, Locale.ROOT).apply { isLenient = false }
        val posicion = ParsePosition(0)
        return formato.parse(fecha, posicion) != null && posicion.index == fecha.length
    }

    private fun convertirAFechaIso(fecha: String): String {
        if (Regex("\\d{4}-\\d{2}-\\d{2}").matches(fecha)) return fecha
        return try {
            val entrada = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).apply { isLenient = false }
            val salida = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
            val date = entrada.parse(fecha)
            if (date != null) salida.format(date) else fecha
        } catch (_: Exception) {
            fecha
        }
    }

    fun crearNuevoServicio(
        titulo: String, origenDestino: String, fecha: String, hora: String,
        tipo: String, pasajeros: Int
    ) {
        val nuevoServicio = Servicio(
            id = UUID.randomUUID().toString(),
            titulo = titulo,
            origenDestino = origenDestino,
            fecha = fecha,
            hora = hora,
            pickup = "Por definir",
            tipo = TipoServicio.fromApi(tipo),
            pasajeros = pasajeros,
            estado = EstadoServicio.PENDIENTE,
            participaciones = emptyList()
        )
        viewModelScope.launch {
            dao.guardarServicio(nuevoServicio)
            _uiState.value = _uiState.value.copy(mensaje = "Servicio creado exitosamente")
            cargarServicios()
        }
    }
}