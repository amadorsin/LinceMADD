package com.example.linceops.repository

import com.example.linceops.data.local.ServicioDao
import com.example.linceops.mappers.aEntidadLocal
import com.example.linceops.model.*
import com.example.linceops.network.RetrofitClient
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ServiceRepository(
    private val dao: ServicioDao,
    private val ahora: () -> Long = { System.currentTimeMillis() }
) {
    private val usuariosSistema: List<Usuario> = listOf(
        // IDs modificados para que coincidan con los de la API de Southbound
        Conductor("20", "Matías Soto Herrera", "driver@linceops.cl", "A-2 Profesional", "Sprinter de prueba"),
        Guia("12", "Camila Rojas Fuentes", "guia@linceops.cl", listOf("Español", "Inglés", "Alemán")),
        Administrador("A301", "Ana Administradora", "admin@linceops.cl")
    )

    suspend fun inicializarDatosSiVacio() {
        // Ya no necesitamos mock. La app ahora sincroniza con Southbound API.
    }

    fun autenticar(credencial: String): Usuario? {
        val entrada = credencial.trim().lowercase()
        return usuariosSistema.find { it.correo.lowercase() == entrada }
            ?: when (entrada) {
                "admin", "manager", "administrador" -> usuariosSistema.find { it.rol == RolUsuario.ADMINISTRADOR }
                "driver", "conductor" -> usuariosSistema.find { it.rol == RolUsuario.CONDUCTOR }
                "guia", "guía" -> usuariosSistema.find { it.rol == RolUsuario.GUIA }
                else -> null
            }
    }

    suspend fun obtenerServicioPorId(servicioId: String): Servicio? =
        dao.obtenerServicioPorId(servicioId)

    suspend fun obtenerServicios(usuarioId: String): Pair<List<Servicio>, Boolean> {
        val usuario = usuariosSistema.find { it.id == usuarioId } ?: return Pair(emptyList(), false)
        var esOffline = false

        // 1. Intentar HTTP GET a la API
        try {
            val response = RetrofitClient.api.obtenerServicios(page = 1, pageSize = 50)
            if (response.isSuccessful) {
                val dtos = response.body()?.data ?: emptyList()

                // 2. Mapear DTOs a Entities, haciendo MERGE con lo que ya existe en Room
                val serviciosSincronizados = dtos.map { dto ->
                    val existente = dao.obtenerServicioPorId(dto.id ?: "")
                    dto.aEntidadLocal(existente)
                }

                // Guardamos masivamente (El REPLACE hará un Upsert limpio)
                dao.guardarServiciosIniciales(serviciosSincronizados)
            } else {
                esOffline = true // Error HTTP 503 o 500
            }
        } catch (e: Exception) {
            esOffline = true // Timeout o Sin Internet
        }

        // 3. Retornar los datos desde Room (Única Fuente de la Verdad)
        val serviciosLocales = dao.obtenerTodosLosServicios().filter {
            usuario.rol == RolUsuario.ADMINISTRADOR || it.estaAsignadoA(usuarioId)
        }

        return Pair(serviciosLocales, esOffline)
    }

    suspend fun cambiarEstadoServicio(
        servicioId: String,
        usuarioId: String,
        nuevoEstado: EstadoServicio,
        motivo: String = "",
        latitud: Double? = null,
        longitud: Double? = null
    ): Result<Servicio> {
        val servicio = dao.obtenerServicioPorId(servicioId) ?: return error("El servicio no existe.")
        val participante = servicio.participaciones.find { it.usuarioId == usuarioId }
            ?: return error("No estás asignado a este servicio.")

        val permitido = when (participante.estado) {
            EstadoServicio.PENDIENTE -> nuevoEstado == EstadoServicio.CONFIRMADO || nuevoEstado == EstadoServicio.RECHAZADO
            EstadoServicio.CONFIRMADO -> nuevoEstado == EstadoServicio.EN_CURSO
            EstadoServicio.EN_CURSO -> nuevoEstado == EstadoServicio.FINALIZADO
            EstadoServicio.RECHAZADO, EstadoServicio.FINALIZADO -> false
        }
        if (!permitido) return error("Cambio de estado no permitido.")
        if (nuevoEstado == EstadoServicio.RECHAZADO && motivo.isBlank()) {
            return error("Escribe un motivo para rechazar el servicio.")
        }

        val requiereMarcaje = nuevoEstado == EstadoServicio.EN_CURSO || nuevoEstado == EstadoServicio.FINALIZADO
        val marcaje = if (requiereMarcaje) {
            if (latitud == null || longitud == null || !latitud.isFinite() || !longitud.isFinite()) {
                return error("Se necesita una ubicación válida para registrar el marcaje.")
            }
            val instante = ahora()
            if (nuevoEstado == EstadoServicio.FINALIZADO) {
                val entrada = participante.checkIn ?: return error("Primero debes registrar el check-in.")
                if (instante < entrada.fechaHoraMillis) return error("La salida no puede ser anterior a la entrada.")
            }
            Marcaje(instante, latitud, longitud)
        } else null

        val actualizado = participante.copy(
            estado = nuevoEstado,
            motivoRechazo = if (nuevoEstado == EstadoServicio.RECHAZADO) motivo.trim() else participante.motivoRechazo,
            checkIn = if (nuevoEstado == EstadoServicio.EN_CURSO) marcaje else participante.checkIn,
            checkOut = if (nuevoEstado == EstadoServicio.FINALIZADO) marcaje else participante.checkOut
        )
        val participantes = servicio.participaciones.map {
            if (it.usuarioId == usuarioId) actualizado else it
        }
        val resultado = servicio.copy(
            participaciones = participantes,
            estado = calcularEstadoGeneral(participantes)
        )

        dao.guardarServicio(resultado)
        return Result.success(resultado)
    }

    suspend fun reasignarServicio(servicioId: String): Result<Servicio> {
        val servicio = dao.obtenerServicioPorId(servicioId) ?: return error("El servicio no existe.")
        val participantesReiniciados = servicio.participaciones.map {
            it.copy(estado = EstadoServicio.CONFIRMADO, motivoRechazo = null)
        }
        val actualizado = servicio.copy(
            estado = EstadoServicio.CONFIRMADO,
            participaciones = participantesReiniciados
        )
        dao.guardarServicio(actualizado)
        return Result.success(actualizado)
    }

    private fun error(mensaje: String): Result<Servicio> =
        Result.failure(IllegalArgumentException(mensaje))

    private fun calcularEstadoGeneral(participantes: List<ParticipacionServicio>): EstadoServicio {
        val estados = participantes.map { it.estado }
        return when {
            estados.all { it == EstadoServicio.FINALIZADO } -> EstadoServicio.FINALIZADO
            estados.any { it == EstadoServicio.EN_CURSO || it == EstadoServicio.FINALIZADO } -> EstadoServicio.EN_CURSO
            estados.any { it == EstadoServicio.RECHAZADO } -> EstadoServicio.RECHAZADO
            estados.all { it == EstadoServicio.CONFIRMADO } -> EstadoServicio.CONFIRMADO
            else -> EstadoServicio.PENDIENTE
        }
    }
}