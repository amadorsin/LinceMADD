package com.example.linceops.network

import android.util.Log
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

internal class ApiHttpLoggingInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startedAt = System.nanoTime()
        val path = request.url.encodedPath

        val response = try {
            chain.proceed(request)
        } catch (error: IOException) {
            val elapsedMs = (System.nanoTime() - startedAt) / 1_000_000
            Log.w(
                TAG,
                "${request.method} $path no HTTP response (${elapsedMs}ms, ${error.javaClass.simpleName})"
            )
            throw error
        }

        val elapsedMs = (System.nanoTime() - startedAt) / 1_000_000
        if (response.isSuccessful) {
            Log.i(TAG, "HTTP ${response.code} ${request.method} $path (${elapsedMs}ms)")
        } else {
            val summary = runCatching {
                resumirErrorHttp(response.peekBody(MAX_ERROR_BODY_BYTES).string())
            }.getOrDefault("error body unavailable")
            Log.w(TAG, "HTTP ${response.code} ${request.method} $path (${elapsedMs}ms) $summary")
        }
        return response
    }

    private companion object {
        const val TAG = "API_HTTP"
        const val MAX_ERROR_BODY_BYTES = 16 * 1024L
    }
}

internal fun resumirErrorHttp(rawBody: String): String {
    if (rawBody.isBlank()) return "error body empty"

    val root = runCatching { JsonParser.parseString(rawBody).asJsonObject }.getOrNull()
        ?: return "error body non-JSON"
    val nestedError = root.get("error").asJsonObjectOrNull()
    val code = nestedError?.get("code").asScalarString()
        ?: root.get("code").asScalarString()
        ?: root.get("error").asScalarString()
        ?: root.get("status").asScalarString()
    val status = root.get("status").asScalarString()
    val message = nestedError?.get("message").asScalarString()
        ?: root.get("message").asScalarString()

    return listOfNotNull(
        code?.let { "code=${sanearValorHttp(it)}" },
        status?.let { "status=${sanearValorHttp(it)}" },
        message?.let { "message=${sanearValorHttp(it)}" }
    ).joinToString(" ").ifBlank { "error body has no safe fields" }
}

private fun JsonElement?.asJsonObjectOrNull(): JsonObject? =
    this?.takeIf { it.isJsonObject }?.asJsonObject

private fun JsonElement?.asScalarString(): String? =
    this?.takeIf { it.isJsonPrimitive && !it.asJsonPrimitive.isJsonNull }
        ?.asString

private fun sanearValorHttp(value: String): String =
    value
        .replace(
            Regex("""(?i)\b(password|pass|contraseña|correo|email|token|authorization|jwt)\b\s*[:=]\s*(?:"[^"]*"|'[^']*'|[^,\s;]+)""")
        ) { match -> "${match.groupValues[1]}=<redacted>" }
        .replace(Regex("""(?i)\bBearer\s+[A-Za-z0-9._~+/-]+=*"""), "Bearer <redacted>")
        .replace(Regex("""\b[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}\b"""), "<redacted-jwt>")
        .replace(Regex("""[\w.+-]+@[\w.-]+\.[A-Za-z]{2,}"""), "<redacted-email>")
        .replace(Regex("""[\r\n\t]+"""), " ")
        .take(240)
