package com.example.linceops.fakes

import com.example.linceops.data.local.ServicioDao
import com.example.linceops.model.Servicio

class FakeServicioDao : ServicioDao {

    private val servicios = linkedMapOf<String, Servicio>()

    override suspend fun obtenerTodosLosServicios(): List<Servicio> =
        servicios.values.toList()

    override suspend fun obtenerServicioPorId(servicioId: String): Servicio? =
        servicios[servicioId]

    override suspend fun guardarServicio(servicio: Servicio) {
        servicios[servicio.id] = servicio
    }

    override suspend fun guardarServiciosIniciales(servicios: List<Servicio>) {
        servicios.forEach { guardarServicio(it) }
    }

    override suspend fun limpiarTabla() {
        servicios.clear()
    }

    fun snapshot(): List<Servicio> = servicios.values.toList()
}
