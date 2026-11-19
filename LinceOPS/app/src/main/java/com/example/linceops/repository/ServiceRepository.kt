package com.example.linceops.repository

import android.util.Log
import com.example.linceops.data.local.ServicioDao
import com.example.linceops.mappers.aEntidadLocal
import com.example.linceops.model.*
import com.example.linceops.network.BackendRetrofitClient
import com.example.linceops.network.RetrofitClient
import com.example.linceops.network.dto.*
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ServiceRepository(
    private val dao: ServicioDao,
    private val ahora: () -> Long = { System.currentTimeMillis() }
) {
    private val gson = Gson()

    private val usuariosSistema: List<Usuario> = listOf(
        // IDs alineados con Southbound API
        Conductor("20", "Matías Soto Herrera", "driver@linceops.cl", "A-2 Profesional", "Sprinter de prueba"),
        Guia("12", "Camila Rojas Fuentes", "guia@linceops.cl", listOf("Español", "Inglés", "Alemán")),
        Administrador("A301", "Ana Administradora", "admin@linceops.cl")
    )

    suspend fun inicializarDatosSiVacio() {
        // La app sincroniza directamente con Southbound API.
    }

    // Autenticación local de respaldo (offline / pruebas rápidas)
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

    // Autenticación real contra Spring Boot (/api/auth/login) para obtener JWT
    suspend fun autenticarConBackend(correo: String, password: String): Result<Usuario> {
        return try {
            val response = BackendRetrofitClient.api.login(LoginRequestDto(correo.trim(), password))
            if (response.isSuccessful) {
                val body = response.body() ?: return Result.failure(IllegalStateException("Respuesta vacía del servidor."))
                BackendRetrofitClient.jwtToken = body.token

                val usuario = when (body.rol.uppercase()) {
                    "ADMINISTRADOR", "ADMIN", "ROLE_ADMINISTRADOR" ->
                        Administrador(body.usuarioId, body.nombre, correo.trim())
                    "GUIA", "ROLE_GUIA" ->
                        Guia(body.usuarioId, body.nombre, correo.trim(), listOf("Español", "Inglés"))
                    else ->
                        Conductor(body.usuarioId, body.nombre, correo.trim(), "A-2 Profesional", "Vehículo asignado")
                }
                Result.success(usuario)
            } else {
                val msg = extraerMensajeErrorBackend(response.errorBody()?.string())
                    ?: "Credenciales inválidas (HTTP ${response.code()})."
                Result.failure(IllegalArgumentException(msg))
            }
        } catch (e: Exception) {
            Log.e("API_BACKEND", "Error en login remoto", e)
            Result.failure(IllegalStateException("No se pudo conectar con el backend de autenticación."))
        }
    }

    suspend fun obtenerServicioPorId(servicioId: String): Servicio? =
        dao.obtenerServicioPorId(servicioId)

    suspend fun obtenerServicios(
        usuarioId: String,
        fechaDesde: String? = null,
        fechaHasta: String? = null,
        oficina: String? = null,
        statusPL: String? = null,
        q: String? = null
    ): Pair<List<Servicio>, Boolean> {
        val usuario = usuariosSistema.find { it.id == usuarioId } ?: return Pair(emptyList(), false)
        var esOffline = false

        // Delegamos el filtro por rol al servidor de Southbound
        val guiaIdQuery = if (usuario.rol == RolUsuario.GUIA) usuario.id.toIntOrNull() else null
        val transportistaIdQuery = if (usuario.rol == RolUsuario.CONDUCTOR) usuario.id.toIntOrNull() else null

        try {
            val response = RetrofitClient.api.obtenerServicios(
                fechaDesde = fechaDesde,
                fechaHasta = fechaHasta,
                oficina = oficina,
                statusPL = statusPL,
                guiaId = guiaIdQuery,
                transportistaId = transportistaIdQuery,
                q = q,
                page = 1,
                pageSize = 50
            )
            if (response.isSuccessful) {
                val dtos = response.body()?.data ?: emptyList()

                val serviciosSincronizados = dtos.map { dto ->
                    val existente = dao.obtenerServicioPorId(dto.id?.toString() ?: "")
                    dto.aEntidadLocal(existente)
                }

                dao.guardarServiciosIniciales(serviciosSincronizados)
            } else {
                val rawError = response.errorBody()?.string()
                val apiError = try {
                    gson.fromJson(rawError, ApiErrorWrapperDto::class.java)?.error
                } catch (_: Exception) { null }

                Log.e(
                    "API_LINCE",
                    "Error HTTP ${response.code()} | code=${apiError?.code} | requestId=${apiError?.requestId} | msg=${apiError?.message ?: rawError}"
                )
                esOffline = true
            }
        } catch (e: Exception) {
            Log.e("API_LINCE", "Fallo al conectar con Southbound", e)
            esOffline = true
        }

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

            // Enviar Check-In / Check-Out al backend propio (Spring Boot) para validar geocerca y tiempo
            val tipoMarcaje = if (nuevoEstado == EstadoServicio.EN_CURSO) "CHECK_IN" else "CHECK_OUT"
            val validacionBackend = enviarMarcajeAlBackend(
                servicio = servicio,
                tipo = tipoMarcaje,
                latitud = latitud,
                longitud = longitud,
                instanteMillis = instante
            )
            // Si el backend rechaza con 400 (ej: fuera de geocerca 150m), abortamos y alertamos al usuario
            if (validacionBackend.isFailure) {
                return Result.failure(validacionBackend.exceptionOrNull()!!)
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

    private suspend fun enviarMarcajeAlBackend(
        servicio: Servicio,
        tipo: String,
        latitud: Double,
        longitud: Double,
        instanteMillis: Long
    ): Result<Unit> {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT)
        val timestampIso = isoFormat.format(Date(instanteMillis))
        val horaProgramadaIso = if (servicio.fecha.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) &&
            servicio.hora.matches(Regex("\\d{2}:\\d{2}"))
        ) {
            "${servicio.fecha}T${servicio.hora}:00"
        } else null

        val request = MarcajeRequestDto(
            servicioId = servicio.id,
            tipo = tipo,
            latitud = latitud,
            longitud = longitud,
            timestamp = timestampIso
        )

        return try {
            val response = BackendRetrofitClient.api.registrarMarcaje(
                request = request,
                pickup = servicio.pickup,
                oficina = servicio.oficina.name,
                horaPickup = horaProgramadaIso
            )
            when {
                response.isSuccessful -> Result.success(Unit)
                response.code() == 400 -> {
                    val mensajeGeocerca = extraerMensajeErrorBackend(response.errorBody()?.string())
                        ?: "Marcaje rechazado: fuera de geocerca (150m) o fuera de ventana horaria."
                    Log.w("API_BACKEND", "HTTP 400 en marcaje: $mensajeGeocerca")
                    Result.failure(IllegalArgumentException(mensajeGeocerca))
                }
                response.code() == 401 || response.code() == 403 -> {
                    Result.failure(IllegalStateException("Sesión expirada o sin permisos en el servidor (HTTP ${response.code()})."))
                }
                else -> {
                    Log.e("API_BACKEND", "Error HTTP ${response.code()} en marcaje")
                    Result.success(Unit) // Fallback offline-first si el servidor tiene error 5xx
                }
            }
        } catch (e: Exception) {
            // Sin conexión con Spring Boot: permitimos guardar localmente en Room para sincronizar con WorkManager
            Log.w("API_BACKEND", "Backend no alcanzable, guardando marcaje en modo offline", e)
            Result.success(Unit)
        }
    }

    suspend fun bloquearFecha(fechaIso: String, motivo: String): Result<BloqueoCalendarioDto> {
        return try {
            val response = BackendRetrofitClient.api.bloquearFecha(BloqueoRequestDto(fechaIso, motivo))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val msg = extraerMensajeErrorBackend(response.errorBody()?.string())
                    ?: "No se pudo bloquear la fecha (HTTP ${response.code()})."
                Result.failure(IllegalArgumentException(msg))
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException("Sin conexión con el servidor para bloquear la fecha."))
        }
    }

    suspend fun registrarIncidencia(servicioId: String, motivo: String, detalle: String? = null): Result<IncidenciaDto> {
        return try {
            val response = BackendRetrofitClient.api.registrarIncidencia(
                IncidenciaRequestDto(servicioId, motivo, detalle)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val msg = extraerMensajeErrorBackend(response.errorBody()?.string())
                    ?: "No se pudo registrar la incidencia (HTTP ${response.code()})."
                Result.failure(IllegalArgumentException(msg))
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException("Sin conexión con el servidor para registrar la incidencia."))
        }
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

    private fun extraerMensajeErrorBackend(rawJson: String?): String? {
        if (rawJson.isNullOrBlank()) return null
        return try {
            gson.fromJson(rawJson, BackendErrorResponseDto::class.java)?.message
        } catch (_: Exception) {
            null
        }
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