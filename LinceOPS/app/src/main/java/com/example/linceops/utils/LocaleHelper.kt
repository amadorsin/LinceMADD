package com.example.linceops.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

object LocaleHelper {

    fun onAttach(context: Context): Context {
        val codigoIdioma = LanguagePreferences.obtenerIdiomaSincrono(context)
        return aplicarIdioma(context, codigoIdioma)
    }

    fun aplicarIdioma(context: Context, codigoIdioma: String): Context {
        val locale = Locale(codigoIdioma)
        Locale.setDefault(locale)
        val configuracion = Configuration(context.resources.configuration)
        configuracion.setLocale(locale)
        configuracion.setLayoutDirection(locale)
        return context.createConfigurationContext(configuracion)
    }

    fun crearContextoLocalizado(base: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val configuracion = Configuration(base.resources.configuration)
        configuracion.setLocale(locale)
        configuracion.setLayoutDirection(locale)
        val localizedContext = base.createConfigurationContext(configuracion)
        return object : ContextWrapper(base) {
            override fun getResources(): Resources = localizedContext.resources

            override fun getAssets(): AssetManager = localizedContext.assets
        }
    }

    fun buscarActivity(context: Context): Activity? {
        var actual = context
        while (actual is ContextWrapper) {
            if (actual is Activity) {
                return actual
            }
            actual = actual.baseContext
        }
        return null
    }
}
