package com.example.linceops.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.linceops.network.RetrofitClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager

    val network = connectivityManager.activeNetwork
        ?: return false

    val activeNetwork =
        connectivityManager.getNetworkCapabilities(network)
            ?: return false

    return activeNetwork.hasCapability(
        NetworkCapabilities.NET_CAPABILITY_INTERNET
    )
}

suspend fun isServerAvailable(context: Context): Boolean {
    if (!isInternetAvailable(context)) return false

    return withContext(Dispatchers.IO) {
        try {
            val respuesta = RetrofitClient.api.checkHealth()
            respuesta.code() in 200..299 && respuesta.code() != 503
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (excepcion: Exception) {
            false
        }
    }
}
