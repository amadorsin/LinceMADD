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
import com.example.linceops.data.local.HitoPendienteDao
import com.example.linceops.data.local.HitoPendienteEntity
import com.example.linceops.data.local.LinceOpsDatabase
import com.example.linceops.data.local.ServicioDao
import com.example.linceops.model.EstadoServicio
import com.example.linceops.network.BackendRetrofitClient
import com.example.linceops.network.dto.BackendErrorResponseDto
import com.example.linceops.network.dto.MarcajeRequestDto
import com.example.linceops.network.dto.MensajeResponseDto
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.concurrent.TimeUnit

internal enum class ResultadoSyncHitos {
    SUCCESS,
    RETRY
}

internal suspend fun sincronizarHitosPendientes(
    pendientes: List<HitoPendienteEntity>,
    dao: HitoPendienteDao,
    servicioDao: ServicioDao,
    enviarMarcaje: suspend (HitoPendienteEntity) -> Response<MensajeResponseDto>
): ResultadoSyncHitos {
    var requiereReintento = false
    val gson = Gson()

    for (hito in pendientes) {
        dao.actualizarEstado(
            clave = hito.claveIdempotencia,
            estado = EstadoSyncHito.SINCRONIZANDO,
            intentos = hito.intentos + 1,
            error = null
        )

        try {
            val response = enviarMarcaje(hito)
            Log.i(
                "SYNC_WORKER",
                "HTTP ${response.code()} recibido para hito ${hito.claveIdempotencia}"
            )
            when {
                response.isSuccessful -> {
                    dao.eliminar(hito.claveIdempotencia)
                    Log.i(
                        "SYNC_WORKER",
                        "Hito sincronizado HTTP ${response.code()}: ${hito.claveIdempotencia}"
                    )
                }

                response.code() == 400 -> {
                    val mensaje = extraerMensajeError(gson, response.errorBody()?.string())
                        ?: "Marcaje rechazado por validación del servidor."
                    if (mensaje.contains("ya fue registrado", ignoreCase = true)) {
                        dao.eliminar(hito.claveIdempotencia)
                        Log.i(
                            "SYNC_WORKER",
                            "Hito ya existía HTTP 400; eliminado por idempotencia: ${hito.claveIdempotencia}"
                        )
                    } else {
                        dao.actualizarEstado(
                            clave = hito.claveIdempotencia,
                            estado = EstadoSyncHito.ERROR_VALIDACION,
                            intentos = hito.intentos + 1,
                            error = mensaje
                        )
                        revertirMarcajeLocal(servicioDao, hito.servicioId, hito.usuarioId, hito.tipo)
                        Log.w(
                            "SYNC_WORKER",
                            "Hito HTTP 400 marcado ERROR_VALIDACION y revertido en Room: ${hito.claveIdempotencia}"
                        )
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
        } catch (error: Exception) {
            Log.w(
                "SYNC_WORKER",
                "Sin respuesta HTTP al sincronizar ${hito.claveIdempotencia} (${error.javaClass.simpleName})"
            )
            dao.actualizarEstado(
                clave = hito.claveIdempotencia,
                estado = EstadoSyncHito.PENDIENTE,
                intentos = hito.intentos + 1,
                error = "Esperando conexión con el servidor..."
            )
            requiereReintento = true
        }
    }

    return if (requiereReintento) ResultadoSyncHitos.RETRY else ResultadoSyncHitos.SUCCESS
}

private fun extraerMensajeError(gson: Gson, rawJson: String?): String? {
    if (rawJson.isNullOrBlank()) return null
    return try {
        gson.fromJson(rawJson, BackendErrorResponseDto::class.java)?.message
    } catch (_: Exception) {
        null
    }
}

internal suspend fun revertirMarcajeLocal(
    servicioDao: ServicioDao,
    servicioId: String,
    usuarioId: String,
    tipo: String
) {
    val servicio = servicioDao.obtenerServicioPorId(servicioId) ?: return
    val participantes = servicio.participaciones.map { participacion ->
        if (participacion.usuarioId != usuarioId) {
            participacion
        } else if (tipo.equals("CHECK_IN", ignoreCase = true)) {
            participacion.copy(
                estado = EstadoServicio.CONFIRMADO,
                checkIn = null,
                checkOut = null
            )
        } else {
            participacion.copy(
                estado = if (participacion.checkIn != null) {
                    EstadoServicio.EN_CURSO
                } else {
                    EstadoServicio.CONFIRMADO
                },
                checkOut = null
            )
        }
    }
    val estados = participantes.map { it.estado }
    val estadoGeneral = when {
        estados.all { it == EstadoServicio.FINALIZADO } -> EstadoServicio.FINALIZADO
        estados.any { it == EstadoServicio.EN_CURSO || it == EstadoServicio.FINALIZADO } -> EstadoServicio.EN_CURSO
        estados.any { it == EstadoServicio.RECHAZADO } -> EstadoServicio.RECHAZADO
        estados.all { it == EstadoServicio.CONFIRMADO } -> EstadoServicio.CONFIRMADO
        else -> EstadoServicio.PENDIENTE
    }
    servicioDao.guardarServicio(servicio.copy(participaciones = participantes, estado = estadoGeneral))
}

class SincronizarHitosWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        BackendRetrofitClient.cargarTokenPersistido(applicationContext)
        val db = LinceOpsDatabase.getDatabase(applicationContext)
        val dao = db.hitoPendienteDao()
        val servicioDao = db.servicioDao()
        val pendientes = dao.obtenerPendientesParaEnviar()

        if (pendientes.isEmpty()) {
            return Result.success()
        }

        if (BackendRetrofitClient.jwtToken.isNullOrBlank()) {
            Log.w("SYNC_WORKER", "Sin token JWT disponible; se reintentará al iniciar sesión.")
            return Result.retry()
        }

        val resultado = withContext(Dispatchers.IO) {
            sincronizarHitosPendientes(
                pendientes = pendientes,
                dao = dao,
                servicioDao = servicioDao
            ) { hito ->
                BackendRetrofitClient.api.registrarMarcaje(
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
            }
        }

        return when (resultado) {
            ResultadoSyncHitos.SUCCESS -> Result.success()
            ResultadoSyncHitos.RETRY -> Result.retry()
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
