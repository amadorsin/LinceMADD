package com.example.linceops.data.sync

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.linceops.data.local.EstadoSyncHito
import com.example.linceops.data.local.LinceOpsDatabase
import com.example.linceops.network.BackendRetrofitClient
import com.example.linceops.network.dto.BackendErrorResponseDto
import com.example.linceops.network.dto.MarcajeRequestDto
import com.google.gson.Gson
import java.util.concurrent.TimeUnit

class SincronizarHitosWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val gson = Gson()

    override suspend fun doWork(): Result {
        BackendRetrofitClient.cargarTokenPersistido(applicationContext)
        val dao = LinceOpsDatabase.getDatabase(applicationContext).hitoPendienteDao()
        val pendientes = dao.obtenerPendientesParaEnviar()

        if (pendientes.isEmpty()) {
            return Result.success()
        }

        if (BackendRetrofitClient.jwtToken.isNullOrBlank()) {
            Log.w("SYNC_WORKER", "Sin token JWT disponible; se reintentará al iniciar sesión.")
            return Result.retry()
        }

        var requiereReintento = false

        for (hito in pendientes) {
            dao.actualizarEstado(
                clave = hito.claveIdempotencia,
                estado = EstadoSyncHito.SINCRONIZANDO,
                intentos = hito.intentos + 1,
                error = null
            )

            try {
                val response = BackendRetrofitClient.api.registrarMarcaje(
                    request = MarcajeRequestDto(
                        servicioId = hito.servicioId,
                        tipo = hito.tipo,
                        latitud = hito.latitud,
                        longitud = hito.longitud,
                        timestamp = hito.timestampIso
                    ),
                    pickup = hito.pickup,
                    oficina = hito.oficina,
                    horaPickup = hito.horaPickupIso
                )

                when {
                    response.isSuccessful -> {
                        dao.eliminar(hito.claveIdempotencia)
                        Log.i("SYNC_WORKER", "Hito sincronizado: ${hito.claveIdempotencia}")
                    }
                    response.code() == 400 -> {
                        val msg = extraerMensaje(response.errorBody()?.string())
                            ?: "Marcaje rechazado por validación del servidor."
                        // Idempotencia: si Spring Boot indica que ya existe, se considera sincronizado
                        if (msg.contains("ya fue registrado", ignoreCase = true)) {
                            dao.eliminar(hito.claveIdempotencia)
                            Log.i("SYNC_WORKER", "Idempotencia: hito ya existía en backend (${hito.claveIdempotencia})")
                        } else {
                            // Error de negocio definitivo (geocerca 150m o atraso 15 min): no reintentar
                            dao.actualizarEstado(
                                clave = hito.claveIdempotencia,
                                estado = EstadoSyncHito.ERROR_VALIDACION,
                                intentos = hito.intentos + 1,
                                error = msg
                            )
                            Log.w("SYNC_WORKER", "Hito rechazado por negocio: $msg")
                        }
                    }
                    response.code() == 401 || response.code() == 403 -> {
                        dao.actualizarEstado(
                            clave = hito.claveIdempotencia,
                            estado = EstadoSyncHito.PENDIENTE,
                            intentos = hito.intentos + 1,
                            error = "Sesión expirada. Inicia sesión para sincronizar."
                        )
                        requiereReintento = true
                    }
                    else -> {
                        dao.actualizarEstado(
                            clave = hito.claveIdempotencia,
                            estado = EstadoSyncHito.PENDIENTE,
                            intentos = hito.intentos + 1,
                            error = "Error servidor HTTP ${response.code()}"
                        )
                        requiereReintento = true
                    }
                }
            } catch (e: Exception) {
                Log.w("SYNC_WORKER", "Fallo de red al sincronizar ${hito.claveIdempotencia}", e)
                dao.actualizarEstado(
                    clave = hito.claveIdempotencia,
                    estado = EstadoSyncHito.PENDIENTE,
                    intentos = hito.intentos + 1,
                    error = "Esperando conexión con el servidor..."
                )
                requiereReintento = true
            }
        }

        return if (requiereReintento) Result.retry() else Result.success()
    }

    private fun extraerMensaje(rawJson: String?): String? {
        if (rawJson.isNullOrBlank()) return null
        return try {
            gson.fromJson(rawJson, BackendErrorResponseDto::class.java)?.message
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val WORK_NAME = "linceops_sync_hitos_offline"

        fun programar(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<SincronizarHitosWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context.applicationContext)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
        }
    }
}