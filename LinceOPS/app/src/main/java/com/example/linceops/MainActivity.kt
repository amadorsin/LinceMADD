package com.example.linceops

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.linceops.navigation.AppNavHost
import com.example.linceops.network.RetrofitClient
import com.example.linceops.ui.theme.LinceOPSTheme
import com.example.linceops.utils.LanguagePreferences
import com.example.linceops.utils.LocaleHelper

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.apiKey = BuildConfig.SOUTHBOUND_API_KEY
        setContent {
            val idiomaInicial = remember {
                LanguagePreferences.obtenerIdiomaSincrono(applicationContext)
            }
            val idiomaFlow = remember {
                LanguagePreferences.observarIdioma(applicationContext)
            }
            val idiomaActual by idiomaFlow.collectAsState(initial = idiomaInicial)

            val modoOscuroInicial = remember {
                LanguagePreferences.obtenerModoOscuroSincrono(applicationContext)
            }
            val modoOscuroFlow = remember {
                LanguagePreferences.observarModoOscuro(applicationContext)
            }
            val modoOscuro by modoOscuroFlow.collectAsState(initial = modoOscuroInicial)

            val localizedContext = remember(idiomaActual) {
                LocaleHelper.crearContextoLocalizado(this@MainActivity, idiomaActual)
            }
            val localizedConfig = remember(idiomaActual) {
                localizedContext.resources.configuration
            }
            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedConfig
            ) {
                LinceOPSTheme(darkTheme = modoOscuro) {
                    val view = LocalView.current
                    val backgroundColor = MaterialTheme.colorScheme.background
                    if (!view.isInEditMode) {
                        SideEffect {
                            val window = this@MainActivity.window
                            @Suppress("DEPRECATION")
                            window.statusBarColor = backgroundColor.toArgb()
                            @Suppress("DEPRECATION")
                            window.navigationBarColor = backgroundColor.toArgb()
                            val insetsController = WindowCompat.getInsetsController(window, view)
                            insetsController.isAppearanceLightStatusBars = !modoOscuro
                            insetsController.isAppearanceLightNavigationBars = !modoOscuro
                        }
                    }
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = backgroundColor
                    ) {
                        AppNavHost()
                    }
                }
            }
        }
    }
}
