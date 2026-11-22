package com.example.linceops

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.linceops.ui.screens.LoginScreen
import com.example.linceops.ui.theme.LinceOPSTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class LoginUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun montarLogin(
        errorLogin: String? = null,
        onLogin: (String, String) -> Unit = { _, _ -> }
    ) {
        composeRule.setContent {
            LinceOPSTheme {
                LoginScreen(onLogin = onLogin, errorLogin = errorLogin)
            }
        }
    }

    @Test
    fun camposBotonesYAtajosVisibles() {
        montarLogin()

        composeRule.onNodeWithTag("login_input_correo").assertIsDisplayed()
        composeRule.onNodeWithTag("login_input_password").assertIsDisplayed()
        composeRule.onNodeWithTag("login_btn_ingresar").assertExists()
        composeRule.onNodeWithTag("login_atajo_manager").assertExists()
        composeRule.onNodeWithTag("login_atajo_driver").assertExists()
        composeRule.onNodeWithTag("login_atajo_guia").assertExists()
    }

    @Test
    fun ingresarSinDatosMuestraBannerDeError() {
        montarLogin()

        composeRule.onNodeWithTag("login_btn_ingresar").performClick()

        composeRule.onNodeWithTag("login_error_banner").assertIsDisplayed()
    }

    @Test
    fun errorDelViewModelSeMuestraEnElBanner() {
        montarLogin(errorLogin = "Credenciales inválidas")

        composeRule.onNodeWithTag("login_error_banner").assertIsDisplayed()
    }

    @Test
    fun credencialesValidasInvocaCallback() {
        var recibido: Pair<String, String>? = null
        montarLogin(onLogin = { correo, password -> recibido = correo to password })

        composeRule.onNodeWithTag("login_input_correo").performTextInput("driver@linceops.cl")
        composeRule.onNodeWithTag("login_input_password").performTextInput("123456")
        composeRule.onNodeWithTag("login_btn_ingresar").performClick()

        composeRule.runOnIdle {
            assertNotNull(recibido)
            assertEquals("driver@linceops.cl", recibido?.first)
            assertEquals("123456", recibido?.second)
        }
    }

    @Test
    fun atajoManagerInvocaCallbackConCredencialesDePrueba() {
        var recibido: Pair<String, String>? = null
        montarLogin(onLogin = { correo, password -> recibido = correo to password })

        composeRule.onNodeWithTag("login_atajo_manager").performScrollTo().performClick()

        composeRule.runOnIdle {
            assertEquals("admin@linceops.cl", recibido?.first)
            assertEquals("123456", recibido?.second)
        }
    }

    @Test
    fun aliasDeRolSeResuelveAntesDeInvocarCallback() {
        var recibido: Pair<String, String>? = null
        montarLogin(onLogin = { correo, password -> recibido = correo to password })

        composeRule.onNodeWithTag("login_input_correo").performTextInput("driver")
        composeRule.onNodeWithTag("login_input_password").performTextInput("123456")
        composeRule.onNodeWithTag("login_btn_ingresar").performClick()

        composeRule.runOnIdle {
            assertEquals("driver@linceops.cl", recibido?.first)
        }
    }
}
