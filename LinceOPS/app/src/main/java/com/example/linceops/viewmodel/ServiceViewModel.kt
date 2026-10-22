package com.example.linceops.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.ViewModel
import com.example.linceops.model.EstadoServicio
import com.example.linceops.model.Servicio
import com.example.linceops.repository.ServiceRepository
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale

class ServiceViewModel(
    private val repository: ServiceRepository = ServiceRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(ServiceUiState())
    val uiState: StateFlow<ServiceUiState> = _uiState.asStateFlow()

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
        val lista = repository.obtenerServicios(usuario.id)
        _uiState.value = _uiState.value.copy(servicios = lista.filter { servicio ->
            (_uiState.value.filtroEstado == null || servicio.estadoPara(usuario) == _uiState.value.filtroEstado) &&
                    (_uiState.value.filtroFecha == null || servicio.fecha == _uiState.value.filtroFecha)
        })
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
        val usuario = _uiState.value.usuarioLogueado ?: return null
        return repository.obtenerServicios(usuario.id).find { it.id == servicioId }
    }

    fun obtenerFinalUpdate(reservaId: String): List<Servicio> {
        val usuario = _uiState.value.usuarioLogueado ?: return emptyList()
        return repository.obtenerFinalUpdate(reservaId, usuario.id)
    }

    fun cambiarEstadoServicio(
        servicioId: String,
        nuevoEstado: EstadoServicio,
        motivo: String = "",
        latitud: Double? = null,
        longitud: Double? = null
    ) {
        val usuario = _uiState.value.usuarioLogueado
        if (usuario == null) {
            _uiState.value = _uiState.value.copy(error = "Primero debes iniciar sesión.", mensaje = null)
            return
        }
        repository.cambiarEstadoServicio(
            servicioId, usuario.id, nuevoEstado, motivo, latitud, longitud
        ).fold(
            onSuccess = {
                _uiState.value = _uiState.value.copy(mensaje = "Estado actualizado: ${nuevoEstado.estado}.", error = null)
                cargarServicios()
            },
            onFailure = {
                _uiState.value = _uiState.value.copy(error = it.message ?: "No se pudo guardar el cambio.", mensaje = null)
            }
        )
    }

    fun confirmarServicio(id: String) = cambiarEstadoServicio(id, EstadoServicio.CONFIRMADO)

    fun rechazarServicio(id: String, motivo: String) =
        cambiarEstadoServicio(id, EstadoServicio.RECHAZADO, motivo = motivo)

    fun registrarCheckIn(id: String, latitud: Double, longitud: Double) =
        cambiarEstadoServicio(id, EstadoServicio.EN_CURSO, latitud = latitud, longitud = longitud)

    fun registrarCheckOut(id: String, latitud: Double, longitud: Double) =
        cambiarEstadoServicio(id, EstadoServicio.FINALIZADO, latitud = latitud, longitud = longitud)

    fun reasignarServicio(servicioId: String) {
        val usuario = _uiState.value.usuarioLogueado
        if (usuario == null) return

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

    fun limpiarMensajes() {
        _uiState.value = _uiState.value.copy(mensaje = null, error = null, errorLogin = null)
    }

    private fun fechaValida(fecha: String): Boolean {
        if (!Regex("\\d{2}/\\d{2}/\\d{4}").matches(fecha)) return false
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).apply { isLenient = false }
        val posicion = ParsePosition(0)
        return formato.parse(fecha, posicion) != null && posicion.index == fecha.length
    }
}