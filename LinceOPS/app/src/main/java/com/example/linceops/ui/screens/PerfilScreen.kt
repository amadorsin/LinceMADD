package com.example.linceops.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.linceops.model.EstadoServicio
import com.example.linceops.model.RolUsuario
import com.example.linceops.model.Servicio
import com.example.linceops.model.Usuario
import com.example.linceops.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val PREFS_PERFIL = "linceops_perfil"

@Composable
fun PerfilScreen(
    modifier: Modifier = Modifier,
    usuario: Usuario? = null,
    servicios: List<Servicio> = emptyList(),
    onCerrarSesion: () -> Unit = {},
    onScrollBarra: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val idUsuario = usuario?.id ?: "invitado"
    val prefs = remember(idUsuario) {
        context.getSharedPreferences(PREFS_PERFIL, Context.MODE_PRIVATE)
    }
    val base = remember(usuario?.id, usuario?.rol) { perfilBase(usuario) }

    LaunchedEffect(scrollState) {
        var posicionAnterior = 0
        snapshotFlow { scrollState.value }.collect { actual ->
            when {
                actual <= 0 -> onScrollBarra(true)
                actual > posicionAnterior + 8 -> onScrollBarra(false)
                actual < posicionAnterior - 8 -> onScrollBarra(true)
            }
            posicionAnterior = actual
        }
    }

    var nombre by remember(idUsuario) {
        mutableStateOf(prefs.getString(clavePerfil(idUsuario, "nombre"), base.nombre) ?: base.nombre)
    }
    var telefono by remember(idUsuario) {
        mutableStateOf(prefs.getString(clavePerfil(idUsuario, "telefono"), base.telefono) ?: base.telefono)
    }
    var idiomas by remember(idUsuario) {
        mutableStateOf(prefs.getString(clavePerfil(idUsuario, "idiomas"), base.idiomas) ?: base.idiomas)
    }
    var unidad by remember(idUsuario) {
        mutableStateOf(prefs.getString(clavePerfil(idUsuario, "unidad"), base.unidad) ?: base.unidad)
    }
    var mostrarEditor by remember { mutableStateOf(false) }
    var mensajePerfil by remember { mutableStateOf<String?>(null) }
    var perfilError by remember { mutableStateOf(false) }

    val fechasServicio = remember(servicios, usuario?.id) {
        servicios
            .filter { servicio ->
                val estado = usuario?.let { servicio.estadoPara(it) } ?: servicio.estado
                estado != EstadoServicio.RECHAZADO
            }
            .map { it.fecha }
            .distinct()
            .sortedBy { parseFecha(it)?.time ?: Long.MAX_VALUE }
    }

    var diasBloqueados by remember(idUsuario) {
        mutableStateOf(cargarDiasBloqueados(prefs, idUsuario, fechasServicio))
    }
    var mensajeDisponibilidad by remember { mutableStateOf<String?>(null) }
    var disponibilidadError by remember { mutableStateOf(false) }

    val diasBloqueadosOrdenados = diasBloqueados.sortedBy { parseFecha(it)?.time ?: Long.MAX_VALUE }
    val iniciales = nombre
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifBlank { "OP" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinceCreamBackground)
            .verticalScroll(scrollState)
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {

        Column(modifier = Modifier.padding(top = 4.dp)) {
            Text(
                text = "Cuenta Operativa",
                style = MaterialTheme.typography.labelLarge,
                color = LinceTextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Mi Perfil",
                style = MaterialTheme.typography.headlineMedium,
                color = LinceGreenPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LinceCardSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, LinceGreenBorder.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(60.dp),
                        shape = CircleShape,
                        color = LinceGreenSageLight,
                        border = BorderStroke(2.dp, LinceGreenPrimary)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = iniciales,
                                style = MaterialTheme.typography.titleLarge,
                                color = LinceGreenDark,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = nombre,
                            style = MaterialTheme.typography.titleLarge,
                            color = LinceTextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = base.subtitulo,
                            style = MaterialTheme.typography.bodySmall,
                            color = LinceTextSecondary
                        )
                        Text(
                            text = usuario?.correo ?: "Sesión de demostración",
                            style = MaterialTheme.typography.labelSmall,
                            color = LinceTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = { mostrarEditor = true },
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, LinceGreenBorder),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LinceGreenDark)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Editar",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                HorizontalDivider(color = LinceOutlineVariant.copy(alpha = 0.5f))

                DatoPerfil(
                    icono = Icons.Default.Call,
                    etiqueta = "TELÉFONO",
                    valor = telefono
                )
                DatoPerfil(
                    icono = Icons.Default.Face,
                    etiqueta = "IDIOMAS",
                    valor = idiomas
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = LinceGreenSageLight.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, LinceGreenBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = LinceGreenDark,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "UNIDAD / CERTIFICACIÓN",
                                style = MaterialTheme.typography.labelSmall,
                                color = LinceGreenDark,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = unidad,
                            style = MaterialTheme.typography.titleMedium,
                            color = LinceTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                mensajePerfil?.let {
                    BannerMensaje(texto = it, esError = perfilError)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LinceCardSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, LinceGreenBorder.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EncabezadoTarjeta(
                    icono = Icons.Default.DateRange,
                    titulo = "Disponibilidad y Días Bloqueados"
                )
                HorizontalDivider(color = LinceOutlineVariant.copy(alpha = 0.5f))

                Text(
                    text = "FECHAS CON SERVICIO ASIGNADO",
                    style = MaterialTheme.typography.labelSmall,
                    color = LinceTextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )
                if (fechasServicio.isEmpty()) {
                    Text(
                        text = "Sin servicios activos asignados.",
                        style = MaterialTheme.typography.bodySmall,
                        color = LinceTextSecondary
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        fechasServicio.forEach { fecha ->
                            ChipFecha(
                                fecha = fecha,
                                fondo = LinceGreenSageLight,
                                colorTexto = LinceGreenDark
                            )
                        }
                    }
                }

                Text(
                    text = "DÍAS BLOQUEADOS",
                    style = MaterialTheme.typography.labelSmall,
                    color = LinceTextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )
                if (diasBloqueadosOrdenados.isEmpty()) {
                    Text(
                        text = "No tienes días bloqueados.",
                        style = MaterialTheme.typography.bodySmall,
                        color = LinceTextSecondary
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        diasBloqueadosOrdenados.forEach { fecha ->
                            ChipFecha(
                                fecha = fecha,
                                fondo = StatusRejectedBg,
                                colorTexto = StatusRejectedText,
                                onQuitar = {
                                    val actualizados = diasBloqueados - fecha
                                    diasBloqueados = actualizados
                                    guardarDiasBloqueados(prefs, idUsuario, actualizados)
                                    mensajeDisponibilidad = "Fecha $fecha desbloqueada."
                                    disponibilidadError = false
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        val calendario = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val fecha = String.format(
                                    Locale.ROOT,
                                    "%02d/%02d/%04d",
                                    dayOfMonth,
                                    month + 1,
                                    year
                                )
                                val anticipacion = diasDeAnticipacion(fecha)
                                when {
                                    anticipacion == null || anticipacion < 7L -> {
                                        mensajeDisponibilidad =
                                            "Solo puedes bloquear con al menos 7 días de anticipación."
                                        disponibilidadError = true
                                    }

                                    fecha in fechasServicio -> {
                                        mensajeDisponibilidad =
                                            "No puedes bloquear el $fecha porque tienes un servicio asignado."
                                        disponibilidadError = true
                                    }

                                    fecha in diasBloqueados -> {
                                        mensajeDisponibilidad = "La fecha $fecha ya está bloqueada."
                                        disponibilidadError = true
                                    }

                                    else -> {
                                        val actualizados = diasBloqueados + fecha
                                        diasBloqueados = actualizados
                                        guardarDiasBloqueados(prefs, idUsuario, actualizados)
                                        mensajeDisponibilidad = "Fecha $fecha bloqueada correctamente."
                                        disponibilidadError = false
                                    }
                                }
                            },
                            calendario.get(Calendar.YEAR),
                            calendario.get(Calendar.MONTH),
                            calendario.get(Calendar.DAY_OF_MONTH)
                        ).apply {
                            datePicker.minDate = System.currentTimeMillis() - 1000L
                        }.show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LinceGreenPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = LinceCardSurface,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bloquear Fecha",
                        color = LinceCardSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                mensajeDisponibilidad?.let {
                    BannerMensaje(texto = it, esError = disponibilidadError)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LinceCardSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, LinceGreenBorder.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StatusPendingBg,
                        border = BorderStroke(1.dp, StatusPendingText.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = StatusPendingText,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "¿No puedes asistir a un servicio asignado?",
                        fontWeight = FontWeight.Bold,
                        color = LinceTextPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Si tienes un imprevisto médico o de fuerza mayor para un turno ya calendarizado, comunícate de inmediato con tu Manager para reasignar tu ruta.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LinceTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LinceGreenPrimary),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = LinceCardSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Llamar al Manager",
                            style = MaterialTheme.typography.labelLarge,
                            color = LinceCardSurface,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }

                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, LinceGreenPrimary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LinceGreenPrimary),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = LinceGreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "WhatsApp Directo",
                            style = MaterialTheme.typography.labelLarge,
                            color = LinceGreenPrimary,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCerrarSesion,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LinceDangerRed)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                tint = LinceCardSurface,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Cerrar Sesión",
                style = MaterialTheme.typography.titleMedium,
                color = LinceCardSurface,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(104.dp))
    }

    if (mostrarEditor) {
        EditarPerfilDialog(
            nombreInicial = nombre,
            telefonoInicial = telefono,
            idiomasInicial = idiomas,
            unidadInicial = unidad,
            onDismiss = { mostrarEditor = false },
            onGuardar = { nuevoNombre, nuevoTelefono, nuevosIdiomas, nuevaUnidad ->
                prefs.edit()
                    .putString(clavePerfil(idUsuario, "nombre"), nuevoNombre)
                    .putString(clavePerfil(idUsuario, "telefono"), nuevoTelefono)
                    .putString(clavePerfil(idUsuario, "idiomas"), nuevosIdiomas)
                    .putString(clavePerfil(idUsuario, "unidad"), nuevaUnidad)
                    .apply()

                nombre = nuevoNombre
                telefono = nuevoTelefono
                idiomas = nuevosIdiomas
                unidad = nuevaUnidad
                mostrarEditor = false
                mensajePerfil = "Perfil actualizado correctamente."
                perfilError = false
            }
        )
    }
}

@Composable
private fun EncabezadoTarjeta(
    icono: ImageVector,
    titulo: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(30.dp),
            shape = RoundedCornerShape(9.dp),
            color = LinceGreenSageLight
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = LinceGreenDark,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            color = LinceGreenDark,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DatoPerfil(
    icono: ImageVector,
    etiqueta: String,
    valor: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(30.dp),
            shape = RoundedCornerShape(9.dp),
            color = LinceSurfaceContainerLow
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = LinceGreenPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelSmall,
                color = LinceTextSecondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.bodyMedium,
                color = LinceTextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ChipFecha(
    fecha: String,
    fondo: Color,
    colorTexto: Color,
    onQuitar: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = fondo,
        border = BorderStroke(1.dp, colorTexto.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(
                start = 12.dp,
                end = if (onQuitar == null) 12.dp else 8.dp,
                top = 7.dp,
                bottom = 7.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = fecha,
                style = MaterialTheme.typography.labelMedium,
                color = colorTexto,
                fontWeight = FontWeight.SemiBold
            )
            if (onQuitar != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Desbloquear $fecha",
                    tint = colorTexto,
                    modifier = Modifier
                        .size(15.dp)
                        .clickable { onQuitar() }
                )
            }
        }
    }
}

@Composable
private fun BannerMensaje(
    texto: String,
    esError: Boolean
) {
    val colorTexto = if (esError) StatusRejectedText else LinceGreenDark
    val colorFondo = if (esError) StatusRejectedBg else LinceGreenSageLight

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colorFondo,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, colorTexto.copy(alpha = 0.22f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (esError) Icons.Default.Warning else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = colorTexto,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.bodySmall,
                color = colorTexto,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun EditarPerfilDialog(
    nombreInicial: String,
    telefonoInicial: String,
    idiomasInicial: String,
    unidadInicial: String,
    onDismiss: () -> Unit,
    onGuardar: (String, String, String, String) -> Unit
) {
    var nombre by remember { mutableStateOf(nombreInicial) }
    var telefono by remember { mutableStateOf(telefonoInicial) }
    var idiomas by remember { mutableStateOf(idiomasInicial) }
    var unidad by remember { mutableStateOf(unidadInicial) }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = LinceCardSurface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Editar perfil",
                    style = MaterialTheme.typography.titleLarge,
                    color = LinceGreenPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Todos los campos son obligatorios.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LinceTextSecondary
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        error = null
                    },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = telefono,
                    onValueChange = {
                        telefono = it
                        error = null
                    },
                    label = { Text("Teléfono") },
                    placeholder = { Text("+56 9 8412 3901") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = idiomas,
                    onValueChange = {
                        idiomas = it
                        error = null
                    },
                    label = { Text("Idiomas") },
                    placeholder = { Text("Español, Inglés, Alemán") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = unidad,
                    onValueChange = {
                        unidad = it
                        error = null
                    },
                    label = { Text("Unidad o certificación") },
                    placeholder = { Text("Van Mercedes Sprinter 4x4") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                error?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = LinceDangerRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, LinceOutlineVariant),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LinceTextSecondary)
                    ) {
                        Text("Cancelar", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = {
                            val nombreLimpio = nombre.trim()
                            val telefonoLimpio = telefono.trim()
                            val idiomasLimpios = idiomas.trim()
                            val unidadLimpia = unidad.trim()

                            if (listOf(
                                    nombreLimpio,
                                    telefonoLimpio,
                                    idiomasLimpios,
                                    unidadLimpia
                                ).any { it.isBlank() }
                            ) {
                                error = "Todos los campos son obligatorios."
                            } else {
                                onGuardar(
                                    nombreLimpio,
                                    telefonoLimpio,
                                    idiomasLimpios,
                                    unidadLimpia
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LinceGreenPrimary)
                    ) {
                        Text(
                            text = "Guardar",
                            color = LinceCardSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

private data class PerfilBase(
    val nombre: String,
    val telefono: String,
    val idiomas: String,
    val unidad: String,
    val subtitulo: String
)

private fun perfilBase(usuario: Usuario?): PerfilBase {
    val nombreUsuario = usuario?.nombre?.takeIf { it.isNotBlank() }
    return when (usuario?.rol) {
        RolUsuario.CONDUCTOR -> PerfilBase(
            nombre = nombreUsuario ?: "Carlos Conductor",
            telefono = "+56 9 8412 3901",
            idiomas = "Español, Inglés",
            unidad = "Van Mercedes Sprinter 4x4",
            subtitulo = "Conductor Profesional Clase A2"
        )

        RolUsuario.GUIA -> PerfilBase(
            nombre = nombreUsuario ?: "Gabriela Guía",
            telefono = "+56 9 7654 3210",
            idiomas = "Español, Inglés, Alemán",
            unidad = "Registro SERNATUR #4821",
            subtitulo = "Guía Turístico Bilingüe"
        )

        RolUsuario.ADMINISTRADOR -> PerfilBase(
            nombre = nombreUsuario ?: "Ana Administradora",
            telefono = "+56 9 6123 4567",
            idiomas = "Español",
            unidad = "Coordinación de Operaciones",
            subtitulo = "Coordinador de Operaciones"
        )

        null -> PerfilBase(
            nombre = nombreUsuario ?: "Operador LinceOPS",
            telefono = "+56 9 0000 0000",
            idiomas = "Español",
            unidad = "Unidad por asignar",
            subtitulo = "Operador"
        )
    }
}

private fun clavePerfil(usuarioId: String, campo: String): String =
    "perfil_${usuarioId}_$campo"

private fun claveBloqueos(usuarioId: String): String =
    "bloqueos_$usuarioId"

private fun cargarDiasBloqueados(
    prefs: SharedPreferences,
    usuarioId: String,
    fechasServicio: List<String>
): Set<String> {
    val existentes = prefs.getStringSet(claveBloqueos(usuarioId), null)
    if (existentes != null) return existentes.toSet()

    val iniciales = mutableSetOf<String>()
    var dias = 7
    while (iniciales.size < 2 && dias < 90) {
        val fecha = fechaFutura(dias)
        if (fecha !in fechasServicio) iniciales.add(fecha)
        dias++
    }
    prefs.edit().putStringSet(claveBloqueos(usuarioId), iniciales).apply()
    return iniciales
}

private fun guardarDiasBloqueados(
    prefs: SharedPreferences,
    usuarioId: String,
    fechas: Set<String>
) {
    prefs.edit().putStringSet(claveBloqueos(usuarioId), fechas).apply()
}

private fun fechaFutura(dias: Int): String {
    val calendario = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_MONTH, dias)
    }
    return SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).format(calendario.time)
}

private fun parseFecha(fecha: String): Date? =
    runCatching {
        SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).apply { isLenient = false }.parse(fecha)
    }.getOrNull()

private fun diasDeAnticipacion(fecha: String): Long? {
    val seleccionada = parseFecha(fecha) ?: return null

    val hoy = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val objetivo = Calendar.getInstance().apply {
        time = seleccionada
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    if (objetivo.before(hoy)) return -1L

    var dias = 0L
    val cursor = Calendar.getInstance().apply { timeInMillis = hoy.timeInMillis }
    while (cursor.before(objetivo)) {
        cursor.add(Calendar.DAY_OF_MONTH, 1)
        dias++
    }
    return dias
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PerfilScreenPreview() {
    LinceOPSTheme {
        PerfilScreen()
    }
}
