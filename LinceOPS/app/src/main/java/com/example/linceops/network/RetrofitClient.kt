package com.example.linceops.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    const val BASE_URL = "https://ws.southbound.travel/lince-api/"

    @Volatile
    var apiKey: String = ""

    private val clienteHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .writeTimeout(8, TimeUnit.SECONDS)
            .addInterceptor { cadena ->
                val solicitud = cadena.request()
                    .newBuilder()
                    .header("Accept", "application/json")
                if (apiKey.isNotBlank()) {
                    solicitud.header("X-API-Key", apiKey)
                }
                cadena.proceed(solicitud.build())
            }
            .build()
    }

    val api: SouthboundApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(clienteHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SouthboundApi::class.java)
    }
}
