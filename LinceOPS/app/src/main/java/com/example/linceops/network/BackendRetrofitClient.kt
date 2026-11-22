package com.example.linceops.network

import android.content.Context
import com.example.linceops.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object BackendRetrofitClient {

    const val BASE_URL = "https://lincemadd.onrender.com/"
    private const val PREFS_NAME = "linceops_auth_prefs"
    private const val KEY_JWT = "jwt_token"

    @Volatile
    var jwtToken: String? = null

    fun cargarTokenPersistido(context: Context) {
        if (jwtToken.isNullOrBlank()) {
            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            jwtToken = prefs.getString(KEY_JWT, null)
        }
    }

    fun guardarToken(context: Context, token: String?) {
        jwtToken = token
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            if (token.isNullOrBlank()) remove(KEY_JWT) else putString(KEY_JWT, token)
        }.apply()
    }

    private val clienteHttp: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(45, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .addInterceptor { cadena ->
                val solicitud = cadena.request()
                    .newBuilder()
                    .header("Accept", "application/json")

                val token = jwtToken
                if (!token.isNullOrBlank()) {
                    solicitud.header("Authorization", "Bearer $token")
                }

                cadena.proceed(solicitud.build())
            }
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(ApiHttpLoggingInterceptor())
        }
        builder.build()
    }

    val api: BackendApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(clienteHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BackendApi::class.java)
    }
}
