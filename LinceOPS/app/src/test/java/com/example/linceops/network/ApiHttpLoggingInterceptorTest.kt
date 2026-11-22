package com.example.linceops.network

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiHttpLoggingInterceptorTest {

    @Test
    fun resumenErrorIncluyeCodigoYMensajePeroRedactaCredenciales() {
        val jwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyMCJ9.abcdefghijklmnop"
        val resumen = resumirErrorHttp(
            """{"status":401,"error":"UNAUTHORIZED","message":"password=secreto Bearer $jwt user@example.com"}"""
        )

        assertTrue(resumen.contains("code=UNAUTHORIZED"))
        assertTrue(resumen.contains("status=401"))
        assertTrue(resumen.contains("message="))
        assertFalse(resumen.contains("secreto"))
        assertFalse(resumen.contains(jwt))
        assertFalse(resumen.contains("user@example.com"))
        assertTrue(resumen.contains("<redacted>"))
        assertTrue(resumen.contains("<redacted-email>"))
    }

    @Test
    fun resumenErrorSoportaObjetoDeErrorAnidadoYNoImprimeCuerpoNoJson() {
        val anidado = resumirErrorHttp(
            """{"error":{"code":"GEOCERCA","message":"Fuera de rango"}}"""
        )
        assertTrue(anidado.contains("code=GEOCERCA"))
        assertTrue(anidado.contains("message=Fuera de rango"))

        val noJson = resumirErrorHttp("password=secreto")
        assertEquals("error body non-JSON", noJson)
        assertFalse(noJson.contains("secreto"))
    }
}
