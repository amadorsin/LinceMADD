package com.example.linceops.repository

import com.example.linceops.model.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ServiceRepository(private val ahora: () -> Long = { System.currentTimeMillis() }) {
    private val usuariosSistema: List<Usuario> = listOf(
        Conductor("D101", "Carlos Conductor", "driver@linceops.cl",
            "A-2 Profesional", "Sprinter de prueba"),
        Guia("G201", "Gabriela Guía", "guia@linceops.cl",
            listOf("Español", "Inglés", "Alemán")),
        Administrador("A301", "Ana Administradora", "admin@linceops.cl")
    )

    private val servicios = crearServiciosFicticios().toMutableList()

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

    fun autenticarPorRol(rol: RolUsuario): Usuario? =
        usuariosSistema.find { it.rol == rol }

    fun getServiciosIniciales(): List<Servicio> = servicios.toList()

    fun obtenerServicioPorId(servicioId: String): Servicio? =
        servicios.find { it.id == servicioId }

    fun obtenerServicios(usuarioId: String): List<Servicio> {
        val usuario = usuariosSistema.find { it.id == usuarioId } ?: return emptyList()
        return servicios.filter {
            usuario.rol == RolUsuario.ADMINISTRADOR || it.estaAsignadoA(usuarioId)
        }
    }

    fun obtenerFinalUpdate(reservaId: String, usuarioId: String): List<Servicio> {
        val usuario = usuariosSistema.find { it.id == usuarioId } ?: return emptyList()
        val itinerario = servicios.filter { it.reservaId == reservaId }
        return if (usuario.rol == RolUsuario.ADMINISTRADOR ||
            itinerario.any { it.estaAsignadoA(usuarioId) }) itinerario else emptyList()
    }

    fun cambiarEstadoServicio(
        servicioId: String,
        usuarioId: String,
        nuevoEstado: EstadoServicio,
        motivo: String = "",
        latitud: Double? = null,
        longitud: Double? = null
    ): Result<Servicio> {
        val indice = servicios.indexOfFirst { it.id == servicioId }
        if (indice == -1) return error("El servicio no existe.")
        val servicio = servicios[indice]
        val participante = servicio.participaciones.find { it.usuarioId == usuarioId }
            ?: return error("No estás asignado a este servicio.")

        val permitido = when (participante.estado) {
            EstadoServicio.PENDIENTE -> nuevoEstado == EstadoServicio.CONFIRMADO ||
                    nuevoEstado == EstadoServicio.RECHAZADO
            EstadoServicio.CONFIRMADO -> nuevoEstado == EstadoServicio.EN_CURSO
            EstadoServicio.EN_CURSO -> nuevoEstado == EstadoServicio.FINALIZADO
            EstadoServicio.RECHAZADO, EstadoServicio.FINALIZADO -> false
        }
        if (!permitido) return error("Cambio de estado no permitido.")
        if (nuevoEstado == EstadoServicio.RECHAZADO && motivo.isBlank()) {
            return error("Escribe un motivo para rechazar el servicio.")
        }

        val requiereMarcaje = nuevoEstado == EstadoServicio.EN_CURSO ||
                nuevoEstado == EstadoServicio.FINALIZADO
        val marcaje = if (requiereMarcaje) {
            if (latitud == null || longitud == null ||
                !latitud.isFinite() || !longitud.isFinite() ||
                latitud !in -90.0..90.0 || longitud !in -180.0..180.0) {
                return error("Se necesita una ubicación válida para registrar el marcaje.")
            }
            val instante = ahora()
            if (nuevoEstado == EstadoServicio.FINALIZADO) {
                val entrada = participante.checkIn
                    ?: return error("Primero debes registrar el check-in.")
                if (instante < entrada.fechaHoraMillis) {
                    return error("La salida no puede ser anterior a la entrada.")
                }
            }
            Marcaje(instante, latitud, longitud)
        } else null

        val actualizado = participante.copy(
            estado = nuevoEstado,
            motivoRechazo = if (nuevoEstado == EstadoServicio.RECHAZADO) motivo.trim()
            else participante.motivoRechazo,
            checkIn = if (nuevoEstado == EstadoServicio.EN_CURSO) marcaje
            else participante.checkIn,
            checkOut = if (nuevoEstado == EstadoServicio.FINALIZADO) marcaje
            else participante.checkOut
        )
        val participantes = servicio.participaciones.map {
            if (it.usuarioId == usuarioId) actualizado else it
        }
        val resultado = servicio.copy(
            participaciones = participantes,
            estado = calcularEstadoGeneral(participantes)
        )
        servicios[indice] = resultado
        return Result.success(resultado)
    fun autenticar(correo: String): Usuario? {
        // Busca el usuario por correo (simula validación de credenciales).
        return usuariosSistema.find { it.correo.equals(correo.trim(), ignoreCase = true) }
    }

    fun reasignarServicio(servicioId: String): Result<Servicio> {
        val indice = servicios.indexOfFirst { it.id == servicioId }
        if (indice == -1) return error("El servicio no existe.")
        val servicio = servicios[indice]
        val participantesReiniciados = servicio.participaciones.map {
            it.copy(estado = EstadoServicio.CONFIRMADO, motivoRechazo = null)
        }
        val actualizado = servicio.copy(
            estado = EstadoServicio.CONFIRMADO,
            participaciones = participantesReiniciados
        )
        servicios[indice] = actualizado
        return Result.success(actualizado)
    }

    private fun error(mensaje: String): Result<Servicio> =
        Result.failure(IllegalArgumentException(mensaje))

    private fun calcularEstadoGeneral(participantes: List<ParticipacionServicio>): EstadoServicio {
        val estados = participantes.map { it.estado }
        return when {
            estados.all { it == EstadoServicio.FINALIZADO } -> EstadoServicio.FINALIZADO
            estados.any { it == EstadoServicio.EN_CURSO || it == EstadoServicio.FINALIZADO } ->
                EstadoServicio.EN_CURSO
            estados.any { it == EstadoServicio.RECHAZADO } -> EstadoServicio.RECHAZADO
            estados.all { it == EstadoServicio.CONFIRMADO } -> EstadoServicio.CONFIRMADO
            else -> EstadoServicio.PENDIENTE
        }
    }

    private fun crearServiciosFicticios(): List<Servicio> {
        val base = ahora()
        fun fecha(dias: Int): String {
            val calendario = Calendar.getInstance().apply {
                timeInMillis = base
                add(Calendar.DAY_OF_MONTH, dias)
            }
            return SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).format(calendario.time)
        }
        val familia = listOf(
            Pasajero("Alex Demo", "Inglés"),
            Pasajero("Sam Demo", "Inglés", "Menú vegetariano"),
            Pasajero("Taylor Demo", "Inglés")
        )
        val pareja = listOf(Pasajero("Lena Prueba", "Alemán"), Pasajero("Kai Prueba", "Alemán"))
        return listOf(
            Servicio(
                id = "1", titulo = "Traslado Aeropuerto - Hotel",
                origenDestino = "Aeropuerto El Tepual -> Hotel Centro",
                fecha = fecha(0), hora = "08:30", pickup = "Llegadas nacionales",
                estado = EstadoServicio.PENDIENTE, pasajeros = familia.size,
                conductorId = "D101", guiaId = "G201", reservaId = "RES-001",
                tipo = "Transfer IN", idioma = "Inglés", agencia = "Agencia Austral Demo",
                hotel = "Hotel Centro (ficticio)", vehiculo = "Sprinter de prueba",
                detallePasajeros = familia, inclusiones = listOf("Traslado", "Recepción bilingüe"),
                instrucciones = "Recibir con letrero RES-001. Llegar 15 minutos antes.",
                observaciones = "Vuelo de prueba LA-DEMO. Tres maletas."
            ),
            Servicio(
                id = "2", titulo = "Tour Puerto Varas y Frutillar",
                origenDestino = "Hotel Centro -> Frutillar -> Hotel Centro",
                fecha = fecha(1), hora = "09:00", pickup = "Lobby Hotel Centro",
                estado = EstadoServicio.CONFIRMADO, pasajeros = familia.size,
                conductorId = "D101", guiaId = "G201", reservaId = "RES-001",
                tipo = "Excursión", idioma = "Inglés", agencia = "Agencia Austral Demo",
                hotel = "Hotel Centro (ficticio)", vehiculo = "Sprinter de prueba",
                detallePasajeros = familia, inclusiones = listOf("Guía", "Traslado", "Almuerzo"),
                instrucciones = "Confirmar menú vegetariano. Llevar ropa impermeable.",
                observaciones = "Segundo día de la reserva RES-001."
            ),
            Servicio(
                id = "3", titulo = "City tour Puerto Montt",
                origenDestino = "Hotel Bahía -> Costanera -> Angelmó -> Hotel Bahía",
                fecha = fecha(0), hora = "15:00", pickup = "Recepción Hotel Bahía",
                estado = EstadoServicio.PENDIENTE, pasajeros = pareja.size,
                conductorId = "D101", guiaId = "G201", reservaId = "RES-002",
                tipo = "City tour", idioma = "Alemán", agencia = "Patagonia Demo",
                hotel = "Hotel Bahía (ficticio)", vehiculo = "Van de prueba",
                detallePasajeros = pareja, inclusiones = listOf("Guía en alemán", "Traslado"),
                instrucciones = "Presentarse con identificación y letrero de la agencia.",
                observaciones = "Duración estimada de tres horas. Entradas no incluidas."
            ),
            Servicio(
                id = "4", titulo = "Traslado Hotel - Aeropuerto",
                origenDestino = "Hotel Centro -> Aeropuerto El Tepual",
                fecha = fecha(2), hora = "10:00", pickup = "Lobby Hotel Centro",
                estado = EstadoServicio.CONFIRMADO, pasajeros = familia.size,
                conductorId = "D101", reservaId = "RES-001",
                tipo = "Transfer OUT", idioma = "Inglés", agencia = "Agencia Austral Demo",
                hotel = "Hotel Centro (ficticio)", vehiculo = "Sprinter de prueba",
                detallePasajeros = familia, inclusiones = listOf("Traslado", "Ayuda con equipaje"),
                instrucciones = "Dejar pasajeros en salidas nacionales a las 11:00.",
                observaciones = "Servicio sin guía. Vuelo ficticio a las 13:00."
            ),
            Servicio(
                id = "5", titulo = "Excursión Saltos del Petrohué",
                origenDestino = "Hotel Lago -> Petrohué -> Hotel Lago",
                fecha = fecha(3), hora = "08:00", pickup = "Recepción Hotel Lago",
                estado = EstadoServicio.PENDIENTE, pasajeros = pareja.size,
                conductorId = "D101", guiaId = "G201", reservaId = "RES-003",
                tipo = "Full day", idioma = "Alemán", agencia = "Lagos Demo",
                hotel = "Hotel Lago (ficticio)", vehiculo = "Van de prueba",
                detallePasajeros = pareja, inclusiones = listOf("Traslado", "Guía", "Entradas"),
                instrucciones = "Avisar que lleven calzado para lluvia y colación.",
                observaciones = "Jornada completa. Regreso estimado a las 18:00."
            )
        )
    }
}