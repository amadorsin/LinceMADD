package com.example.linceops

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.example.linceops.navigation.AppNavHost
import com.example.linceops.ui.theme.LinceOPSTheme
import com.example.linceops.utils.LocaleHelper

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val contextoLocalizado = remember { LocaleHelper.onAttach(this@MainActivity) }
            CompositionLocalProvider(
                LocalContext provides contextoLocalizado,
                LocalConfiguration provides contextoLocalizado.resources.configuration
            ) {
                LinceOPSTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavHost()
                    }
                }
            }
        }
    }
}
