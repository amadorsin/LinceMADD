package com.example.linceops.fakes

import com.example.linceops.data.local.EstadoSyncHito
import com.example.linceops.data.local.HitoPendienteDao
import com.example.linceops.data.local.HitoPendienteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeHitoPendienteDao : HitoPendienteDao {

    private val hitos = MutableStateFlow<List<HitoPendienteEntity>>(emptyList())

    val estado: StateFlow<List<HitoPendienteEntity>> = hitos.asStateFlow()

    override fun observarTodos(): Flow<List<HitoPendienteEntity>> =
        hitos.map { lista -> lista.sortedBy { it.creadoEnMillis } }

    override suspend fun obtenerPendientesParaEnviar(): List<HitoPendienteEntity> =
        hitos.value
            .filter { it.estadoSync != EstadoSyncHito.ERROR_VALIDACION }
            .sortedBy { it.creadoEnMillis }

    override suspend fun insertarOActualizar(hito: HitoPendienteEntity) {
        hitos.value = hitos.value
            .filterNot { it.claveIdempotencia == hito.claveIdempotencia } + hito
    }

    override suspend fun actualizarEstado(
        clave: String,
        estado: String,
        intentos: Int,
        error: String?
    ) {
        hitos.value = hitos.value.map {
            if (it.claveIdempotencia == clave) {
                it.copy(estadoSync = estado, intentos = intentos, ultimoError = error)
            } else {
                it
            }
        }
    }

    override suspend fun eliminar(clave: String) {
        hitos.value = hitos.value.filterNot { it.claveIdempotencia == clave }
    }

    override suspend fun limpiarErroresValidacion() {
        hitos.value = hitos.value.filterNot { it.estadoSync == EstadoSyncHito.ERROR_VALIDACION }
    }
}
