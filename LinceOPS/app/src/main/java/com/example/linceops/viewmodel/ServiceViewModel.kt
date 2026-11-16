package com.example.linceops.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linceops.data.local.ServicioDao
import com.example.linceops.model.EstadoServicio
import com.example.linceops.model.ParticipacionServicio
import com.example.linceops.model.RolUsuario
import com.example.linceops.model.Servicio
import com.example.linceops.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

private const val RADIO_MAXIMO_CHECKIN_METROS = 500

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

    fun iniciarSesion(correo: String) {
        val usuario = repository.autenticar(correo)
        _uiState.value = if (usuario == null) {
            ServiceUiState(errorLogin = "Ingresa un correo de prueba válido.")
        } else {
            ServiceUiState(usuarioLogueado = usuario)
        }
        cargarServicios()
    }

    fun cerrarSesion() {
        _uiState.value = ServiceUiState()
    }

    fun cargarServicios() {
        val usuario = _uiState.value.usuarioLogueado
        if (usuario == null) {
            _uiState.value = _uiState.value.copy(servicios = emptyList())
            return
        }
        viewModelScope.launch {
            val lista = repository.obtenerServicios(usuario.id)
            val filtrados = lista.filter { servicio ->
                (_uiState.value.filtroEstado == null || servicio.estadoPara(usuario) == _uiState.value.filtroEstado) &&
                        (_uiState.value.filtroFecha == null || servicio.fecha == _uiState.value.filtroFecha)
            }
            _uiState.value = _uiState.value.copy(
                servicios = filtrados,
                serviciosTotales = lista
            )
        }
    }

    fun filtrarPorEstado(estado: EstadoServicio?) {
        _uiState.value = _uiState.value.copy(filtroEstado = estado, mensaje = null, error = null)
        cargarServicios()
    }

    fun filtrarPorFecha(fecha: String?) {
        val normalizada = fecha?.trim()?.takeIf { it.isNotEmpty() }
        if (normalizada != null && !fechaValida(normalizada)) {
            _uiState.value = _uiState.value.copy(error = "Usa una fecha válida con formato dd/MM/yyyy.", mensaje = null)
            return
        }
        _uiState.value = _uiState.value.copy(filtroFecha = normalizada, error = null, mensaje = null)
        cargarServicios()
    }

    fun limpiarFiltros() {
        _uiState.value = _uiState.value.copy(filtroEstado = null, filtroFecha = null, error = null, mensaje = null)
        cargarServicios()
    }

    fun obtenerServicio(servicioId: String): Servicio? {
        return _uiState.value.serviciosTotales.find { it.id == servicioId }
            ?: _uiState.value.servicios.find { it.id == servicioId }
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
                    val msjRed = if (hayInternet) "Sincronizado en la nube." else "Guardado localmente (Offline)."
                    _uiState.value = _uiState.value.copy(mensaje = "Estado actualizado. $msjRed", error = null)
                    cargarServicios()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(error = it.message ?: "No se pudo guardar el cambio.", mensaje = null)
                }
            )
        }
    }

    fun confirmarServicio(id: String) = cambiarEstadoServicio(id, EstadoServicio.CONFIRMADO)

    fun rechazarServicio(id: String, motivo: String) =
        cambiarEstadoServicio(id, EstadoServicio.RECHAZADO, motivo = motivo)

    fun registrarCheckIn(
        id: String,
        latitud: Double,
        longitud: Double,
        distanciaMetros: Int,
        hayInternet: Boolean = true
    ) {
        if (distanciaMetros > RADIO_MAXIMO_CHECKIN_METROS) {
            _uiState.value = _uiState.value.copy(
                error = "Check-In fuera del rango de 500 m. Usa el Check-In de Contingencia.",
                mensaje = null
            )
            return
        }
        cambiarEstadoServicio(
            id,
            EstadoServicio.EN_CURSO,
            latitud = latitud,
            longitud = longitud,
            hayInternet = hayInternet
        )
    }

    fun registrarCheckInContingencia(
        id: String,
        latitud: Double,
        longitud: Double,
        motivo: String,
        nota: String,
        distanciaMetros: Int,
        hayInternet: Boolean = true
    ) {
        val usuario = _uiState.value.usuarioLogueado
        if (usuario == null) {
            _uiState.value = _uiState.value.copy(error = "Primero debes iniciar sesión.", mensaje = null)
            return
        }
        if (motivo.isBlank() || nota.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Selecciona un motivo y escribe una observación para el Check-In de contingencia.",
                mensaje = null
            )
            return
        }
        viewModelScope.launch {
            repository.cambiarEstadoServicio(
                id,
                usuario.id,
                EstadoServicio.EN_CURSO,
                "",
                latitud,
                longitud
            ).fold(
                onSuccess = { servicio ->
                    val participacion = servicio.participaciones.find { it.usuarioId == usuario.id }
                    val rol = participacion?.rol ?: usuario.rol
                    val registro = "[CHECK-IN CONTINGENCIA - ${rol.nombreMostrar}: ${motivo.trim()} | " +
                        "Dist: ${distanciaMetros}m | Nota: ${nota.trim()}]"
                    val observaciones = if (servicio.observaciones.isBlank()) {
                        registro
                    } else {
                        "${servicio.observaciones}\n$registro"
                    }
                    dao.guardarServicio(servicio.copy(observaciones = observaciones))
                    val msjRed = if (hayInternet) {
                        "Sincronizado en la nube."
                    } else {
                        "Guardado localmente (Offline)."
                    }
                    _uiState.value = _uiState.value.copy(
                        mensaje = "Check-In de contingencia registrado. $msjRed",
                        error = null
                    )
                    cargarServicios()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        error = it.message ?: "No se pudo registrar el Check-In de contingencia.",
                        mensaje = null
                    )
                }
            )
        }
    }

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
        if (!Regex("\\d{2}/\\d{2}/\\d{4}").matches(fecha)) return false
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).apply { isLenient = false }
        val posicion = ParsePosition(0)
        return formato.parse(fecha, posicion) != null && posicion.index == fecha.length
    }

    fun crearNuevoServicio(
        titulo: String, origenDestino: String, fecha: String, hora: String,
        tipo: String, pasajeros: Int,
        conductorId: String? = null, guiaId: String? = null
    ) {
        val conductorAsignado = conductorId?.takeIf { it.isNotBlank() }
        val guiaAsignada = guiaId?.takeIf { it.isNotBlank() }
        val participaciones = buildList {
            conductorAsignado?.let {
                add(
                    ParticipacionServicio(
                        usuarioId = it,
                        rol = RolUsuario.CONDUCTOR,
                        estado = EstadoServicio.PENDIENTE
                    )
                )
            }
            guiaAsignada?.let {
                add(
                    ParticipacionServicio(
                        usuarioId = it,
                        rol = RolUsuario.GUIA,
                        estado = EstadoServicio.PENDIENTE
                    )
                )
            }
        }.distinctBy { it.usuarioId }
        val primerTramo = origenDestino.substringBefore("->").trim()
        val nuevoServicio = Servicio(
            id = UUID.randomUUID().toString(),
            titulo = titulo,
            origenDestino = origenDestino,
            fecha = fecha,
            hora = hora,
            pickup = primerTramo.ifBlank { "Por definir" },
            tipo = tipo,
            pasajeros = pasajeros,
            estado = EstadoServicio.PENDIENTE,
            conductorId = conductorAsignado,
            guiaId = guiaAsignada,
            participaciones = participaciones
        )
        viewModelScope.launch {
            dao.guardarServicio(nuevoServicio)
            _uiState.value = _uiState.value.copy(mensaje = "Servicio creado exitosamente")
            cargarServicios()
        }
    }
}