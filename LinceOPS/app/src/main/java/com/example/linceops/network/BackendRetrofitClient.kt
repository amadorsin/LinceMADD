package com.example.linceops.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object BackendRetrofitClient {

    // 10.0.2.2 apunta al localhost de tu PC desde el emulador de Android.
    // Si pruebas en teléfono físico o despliegas en la nube, cambia esta URL.
    const val BASE_URL = "https://lincemadd.onrender.com/"

    @Volatile
    var jwtToken: String? = null

    private val clienteHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
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
            .build()
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