package com.example.linceops.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices

@Composable
fun MarcajeScreen(
    servicioId: String,
    onCheckIn: (Double, Double) -> Unit,
    onCheckOut: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var mensajeEstado by remember { mutableStateOf("Esperando inicio de ruta...") }
    var accionPendiente by remember { mutableStateOf<(() -> Unit)?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            accionPendiente?.invoke()
        } else {
            mensajeEstado = "Permiso de ubicación denegado."
        }
        accionPendiente = null
    }

    fun obtenerUbicacion(onSuccess: (Double, Double) -> Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    onSuccess(location.latitude, location.longitude)
                } else {
                    mensajeEstado = "No se pudo obtener la ubicación."
                }
            }
        } else {
            accionPendiente = { obtenerUbicacion(onSuccess) }
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = mensajeEstado)
        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { obtenerUbicacion { lat, lon -> onCheckIn(lat, lon) } }) {
            Text("Registrar Check-In")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { obtenerUbicacion { lat, lon -> onCheckOut(lat, lon) } }) {
            Text("Registrar Check-Out")
        }
    }
}