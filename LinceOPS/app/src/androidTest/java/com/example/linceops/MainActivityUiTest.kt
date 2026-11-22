package com.example.linceops

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun esperarLogin() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("login_input_correo")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun pantallaLoginSeMuestraAlIniciarLaApp() {
        esperarLogin()

        composeRule.onNodeWithTag("login_input_correo").assertIsDisplayed()
        composeRule.onNodeWithTag("login_input_password").assertIsDisplayed()
        composeRule.onNodeWithTag("login_btn_ingresar").assertIsDisplayed()
    }

    @Test
    fun escribirCredencialesNoNavegaSinPresionarIngresar() {
        esperarLogin()

        composeRule.onNodeWithTag("login_input_correo").performTextInput("driver@linceops.cl")
        composeRule.onNodeWithTag("login_input_password").performTextInput("123456")

        composeRule.onNodeWithTag("login_input_correo").assertIsDisplayed()
        composeRule.onNodeWithTag("login_input_password").assertIsDisplayed()
    }
}
