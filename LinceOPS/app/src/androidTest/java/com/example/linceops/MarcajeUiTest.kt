package com.example.linceops

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.linceops.model.EstadoServicio
import com.example.linceops.model.Servicio
import com.example.linceops.ui.screens.MarcajeScreen
import com.example.linceops.ui.theme.LinceOPSTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MarcajeUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val servicio = Servicio(
        id = "100",
        titulo = "Traslado de prueba",
        origenDestino = "Hotel -> Aeropuerto",
        fecha = "2026-11-20",
        hora = "09:00",
        pickup = "Hotel",
        estado = EstadoServicio.CONFIRMADO,
        pasajeros = 2,
        participaciones = emptyList()
    )

    private fun montarMarcaje(
        hitosPendientesSync: Int = 0,
        sincronizandoOffline: Boolean = false,
        errorSyncOffline: String? = null,
        onSincronizarAhora: () -> Unit = {},
        onDescartarErrorSync: () -> Unit = {}
    ) {
        composeRule.setContent {
            LinceOPSTheme {
                MarcajeScreen(
                    servicioId = servicio.id,
                    servicio = servicio,
                    hitosPendientesSync = hitosPendientesSync,
                    sincronizandoOffline = sincronizandoOffline,
                    errorSyncOffline = errorSyncOffline,
                    onSincronizarAhora = onSincronizarAhora,
                    onDescartarErrorSync = onDescartarErrorSync,
                    onCheckIn = { _, _ -> },
                    onCheckOut = { _, _ -> }
                )
            }
        }
    }

    @Test
    fun sinPendientesNoMuestraBanner() {
        montarMarcaje()

        composeRule.onNodeWithTag("marcaje_banner_offline").assertDoesNotExist()
    }

    @Test
    fun colaPendienteMuestraBannerYReintentar() {
        var clics = 0
        montarMarcaje(hitosPendientesSync = 3, onSincronizarAhora = { clics++ })

        composeRule.onNodeWithTag("marcaje_banner_offline").assertIsDisplayed()
        composeRule.onNodeWithTag("marcaje_btn_reintentar_sync").assertIsDisplayed().performClick()

        composeRule.runOnIdle { assertEquals(1, clics) }
    }

    @Test
    fun errorDeSincronizacionMuestraBannerYCerrar() {
        var clics = 0
        montarMarcaje(
            errorSyncOffline = "Fuera de geocerca (150m)",
            onDescartarErrorSync = { clics++ }
        )

        composeRule.onNodeWithTag("marcaje_banner_offline").assertIsDisplayed()
        composeRule.onNodeWithTag("marcaje_btn_cerrar_error_sync").assertIsDisplayed().performClick()

        composeRule.runOnIdle { assertEquals(1, clics) }
    }

    @Test
    fun sincronizandoMuestraBannerSinAcciones() {
        montarMarcaje(hitosPendientesSync = 1, sincronizandoOffline = true)

        composeRule.onNodeWithTag("marcaje_banner_offline").assertIsDisplayed()
        composeRule.onNodeWithTag("marcaje_btn_reintentar_sync").assertDoesNotExist()
        composeRule.onNodeWithTag("marcaje_btn_cerrar_error_sync").assertDoesNotExist()
    }
}
