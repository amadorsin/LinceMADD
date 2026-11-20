package com.example.linceops.network.dto

import com.google.gson.annotations.SerializedName

// --- Auth ---
data class LoginRequestDto(
    @SerializedName("correo") val correo: String,
    @SerializedName("password") val password: String
)

data class AuthResponseDto(
    @SerializedName("token") val token: String,
    @SerializedName("usuarioId") val usuarioId: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("rol") val rol: String
)

// --- Hitos (Check-In / Check-Out) ---
data class MarcajeRequestDto(
    @SerializedName("servicioId") val servicioId: String,
    @SerializedName("tipo") val tipo: String, // "CHECK_IN" o "CHECK_OUT"
    @SerializedName("latitud") val latitud: Double,
    @SerializedName("longitud") val longitud: Double,
    @SerializedName("timestamp") val timestamp: String // ISO-8601: "yyyy-MM-dd'T'HH:mm:ss"
)

data class MensajeResponseDto(
    @SerializedName("mensaje") val mensaje: String
)

// --- Disponibilidad ---
data class BloqueoRequestDto(
    @SerializedName("fechaBloqueada") val fechaBloqueada: String, // "yyyy-MM-dd"
    @SerializedName("motivo") val motivo: String
)

data class BloqueoCalendarioDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("fechaBloqueada") val fechaBloqueada: String? = null,
    @SerializedName("motivo") val motivo: String? = null
)

// --- Incidencias ---
data class IncidenciaRequestDto(
    @SerializedName("servicioId") val servicioId: String,
    @SerializedName("motivo") val motivo: String,
    @SerializedName("detalle") val detalle: String? = null
)

data class IncidenciaDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("servicioId") val servicioId: String? = null,
    @SerializedName("motivo") val motivo: String? = null,
    @SerializedName("detalle") val detalle: String? = null
)

// --- Error estándar de tu Spring Boot (HTTP 400 Geocerca / Tiempo) ---
data class BackendErrorResponseDto(
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("status") val status: Int? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("message") val message: String? = null
)
// --- Confirmaciones / Rechazos ---
data class ConfirmacionRequestDto(
    @SerializedName("servicioId") val servicioId: String,
    @SerializedName("estado") val estado: String, // "CONFIRMADO" o "RECHAZADO"
    @SerializedName("motivo") val motivo: String? = null
)

data class ConfirmacionResponseDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("servicioId") val servicioId: String? = null,
    @SerializedName("estado") val estado: String? = null,
    @SerializedName("motivo") val motivo: String? = null,
    @SerializedName("fechaHora") val fechaHora: String? = null
)
// --- Geocercas y Hitos (Coordinación y Validación Doble) ---
data class GeocercaDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("puntoReferencia") val puntoReferencia: String? = null,
    @SerializedName("latitudCentro") val latitudCentro: Double? = null,
    @SerializedName("longitudCentro") val longitudCentro: Double? = null,
    @SerializedName("radioMetros") val radioMetros: Int? = 150
)

data class UsuarioResumenDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("rol") val rol: String? = null
)

data class HitoDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("servicioId") val servicioId: String? = null,
    @SerializedName("usuario") val usuario: UsuarioResumenDto? = null,
    @SerializedName("tipo") val tipo: String? = null,
    @SerializedName("timestampRegistro") val timestampRegistro: String? = null,
    @SerializedName("latitudMarcaje") val latitudMarcaje: Double? = null,
    @SerializedName("longitudMarcaje") val longitudMarcaje: Double? = null
)