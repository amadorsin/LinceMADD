package com.example.linceops.repository

import com.example.linceops.model.*

class ServiceRepository {

    // Lista de usuarios ficticios del sistema (1 Conductor, 1 Guía, 1 Admin)
    private val usuariosSistema = listOf(
        Conductor(
            id = "D101",
            nombre = "Carlos Conductor",
            correo = "driver@linceops.cl",
            licencia = "A-2 Profesional",
            vehiculoAsignado = "Mercedes-Benz Sprinter (KXYZ-88)"
        ),
        Guia(
            id = "G201",
            nombre = "Gabriela Guía",
            correo = "guia@linceops.cl",
            idiomas = listOf("Español", "Inglés")
        ),
        Administrador(
            id = "A301",
            nombre = "Ana Administradora",
            correo = "admin@linceops.cl",
            departamento = "Coordinación Logística"
        )
    )

    fun autenticar(correo: String): Usuario? {
        // Busca el usuario por correo (simula validación de credenciales)
        return usuariosSistema.find { it.correo.equals(correo.trim(), ignoreCase = true) }
    }

    fun getServiciosIniciales(): List<Servicio> {
        return listOf(
            Servicio(
                id = "1",
                titulo = "Traslado Aeropuerto - Hotel",
                origenDestino = "Aeropuerto -> Hotel Centro",
                fecha = "28/09/2026",
                hora = "08:30",
                pickup = "Terminal 1",
                estado = EstadoServicio.PENDIENTE,
                pasajeros = 3,
                conductorId = "D101"
            ),
            Servicio(
                id = "2",
                titulo = "Tour Pelluco Exclusivo",
                origenDestino = "Centro -> Pelluco",
                fecha = "28/09/2026",
                hora = "11:00",
                pickup = "Lobby Hotel",
                estado = EstadoServicio.CONFIRMADO,
                pasajeros = 4,
                conductorId = "D101"
            )
        )
    }
}