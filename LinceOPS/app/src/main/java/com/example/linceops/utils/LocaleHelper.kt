package com.example.linceops.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    private const val PREFS_LOCALE = "linceops_locale_prefs"
    private const val CLAVE_IDIOMA = "selected_language"
    private const val IDIOMA_POR_DEFECTO = "es"

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_LOCALE, Context.MODE_PRIVATE)
        return prefs.getString(CLAVE_IDIOMA, IDIOMA_POR_DEFECTO) ?: IDIOMA_POR_DEFECTO
    }

    fun setLocale(context: Context, languageCode: String): Context {
        val codigo = languageCode.ifBlank { IDIOMA_POR_DEFECTO }
        val prefs = context.getSharedPreferences(PREFS_LOCALE, Context.MODE_PRIVATE)
        prefs.edit().putString(CLAVE_IDIOMA, codigo).apply()

        val locale = Locale(codigo)
        Locale.setDefault(locale)

        val configuracion = Configuration(context.resources.configuration)
        configuracion.setLocale(locale)
        return context.createConfigurationContext(configuracion)
    }

    fun onAttach(context: Context): Context = setLocale(context, getLanguage(context))

    fun cambiarIdiomaYRecrear(context: Context, languageCode: String) {
        val codigo = languageCode.ifBlank { IDIOMA_POR_DEFECTO }
        val prefs = context.getSharedPreferences(PREFS_LOCALE, Context.MODE_PRIVATE)
        prefs.edit().putString(CLAVE_IDIOMA, codigo).apply()

        val locale = Locale(codigo)
        Locale.setDefault(locale)

        val configuracion = Configuration(context.resources.configuration)
        configuracion.setLocale(locale)
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(configuracion, context.resources.displayMetrics)

        buscarActivity(context)?.recreate()
    }

    private fun buscarActivity(context: Context): Activity? {
        var actual: Context? = context
        while (actual is ContextWrapper) {
            if (actual is Activity) return actual
            actual = actual.baseContext
        }
        return null
    }
}
