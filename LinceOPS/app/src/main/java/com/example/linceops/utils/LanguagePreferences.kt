package com.example.linceops.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "linceops_settings")

object LanguagePreferences {

    private val CLAVE_IDIOMA = stringPreferencesKey("app_language")
    private val CLAVE_MODO_OSCURO = booleanPreferencesKey("dark_mode")
    private const val IDIOMA_POR_DEFECTO = "es"
    private const val MODO_OSCURO_POR_DEFECTO = false

    fun observarIdioma(context: Context): Flow<String> {
        return context.applicationContext.dataStore.data.map { preferencias ->
            preferencias[CLAVE_IDIOMA] ?: IDIOMA_POR_DEFECTO
        }
    }

    suspend fun guardarIdioma(context: Context, languageCode: String) {
        context.applicationContext.dataStore.edit { preferencias ->
            preferencias[CLAVE_IDIOMA] = languageCode
        }
    }

    fun obtenerIdiomaSincrono(context: Context): String {
        return runBlocking {
            context.applicationContext.dataStore.data.map { preferencias ->
                preferencias[CLAVE_IDIOMA] ?: IDIOMA_POR_DEFECTO
            }.first()
        }
    }

    fun observarModoOscuro(context: Context): Flow<Boolean> {
        return context.applicationContext.dataStore.data.map { preferencias ->
            preferencias[CLAVE_MODO_OSCURO] ?: MODO_OSCURO_POR_DEFECTO
        }
    }

    suspend fun guardarModoOscuro(context: Context, enabled: Boolean) {
        context.applicationContext.dataStore.edit { preferencias ->
            preferencias[CLAVE_MODO_OSCURO] = enabled
        }
    }

    fun obtenerModoOscuroSincrono(context: Context): Boolean {
        return runBlocking {
            context.applicationContext.dataStore.data.map { preferencias ->
                preferencias[CLAVE_MODO_OSCURO] ?: MODO_OSCURO_POR_DEFECTO
            }.first()
        }
    }
}
