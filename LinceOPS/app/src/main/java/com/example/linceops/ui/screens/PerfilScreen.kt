package com.example.linceops.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.linceops.R
import com.example.linceops.model.EstadoServicio
import com.example.linceops.model.RolUsuario
import com.example.linceops.model.Servicio
import com.example.linceops.model.Usuario
import com.example.linceops.ui.components.LinceButton
import com.example.linceops.ui.components.LinceButtonVariant
import com.example.linceops.ui.components.LinceDivider
import com.example.linceops.ui.components.LinceSectionHeader
import com.example.linceops.ui.components.LinceStatusBadge
import com.example.linceops.ui.components.LinceStatusTone
import com.example.linceops.ui.components.LinceSurfaceCard
import com.example.linceops.ui.components.LinceSurfaceCardVariant
import com.example.linceops.ui.components.LinceUserAvatar
import com.example.linceops.ui.components.PerfilSkeleton
import com.example.linceops.ui.theme.*
import com.example.linceops.utils.LanguagePreferences
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

private const val PREFS_PERFIL = "linceops_perfil"

@Composable
fun PerfilScreen(
    modifier: Modifier = Modifier,
    usuario: Usuario? = null,
    servicios: List<Servicio> = emptyList(),
    diasBloqueadosRemotos: List<String> = emptyList(),
    isLoading: Boolean = false,
    onBloquearFecha: ((String) -> Unit)? = null,
    onDesbloquearFecha: ((String) -> Unit)? = null,
    onCerrarSesion: () -> Unit = {},
    onScrollBarra: (Boolean) -> Unit = {}
){
    if (isLoading && usuario == null) {
        PerfilSkeleton(modifier = modifier)
        return
    }
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val idUsuario = usuario?.id ?: "invitado"
    val prefs = remember(idUsuario) {
        context.getSharedPreferences(PREFS_PERFIL, Context.MODE_PRIVATE)
    }
    val base = remember(usuario?.id, usuario?.rol) { perfilBase(usuario, context) }
    val idiomaActual by LanguagePreferences.observarIdioma(context).collectAsState(initial = "es")
    val modoOscuro by LanguagePreferences.observarModoOscuro(context)
        .collectAsState(initial = LinceTheme.colors.isDark)
    val alcanceCorrutina = rememberCoroutineScope()

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
            .map { normalizarFechaMostrar(it.fecha) }
            .distinct()
            .sortedBy { parseFecha(it)?.time ?: Long.MAX_VALUE }
    }

    var diasBloqueados by remember(idUsuario) {
        mutableStateOf(cargarDiasBloqueados(prefs, idUsuario, fechasServicio))
    }
    LaunchedEffect(diasBloqueadosRemotos) {
        if (onBloquearFecha != null) {
            diasBloqueados = diasBloqueadosRemotos.toSet()
            guardarDiasBloqueados(prefs, idUsuario, diasBloqueados)
        }
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
                text = stringResource(R.string.perfil_cuenta),
                style = MaterialTheme.typography.labelLarge,
                color = LinceTextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.perfil_titulo),
                style = MaterialTheme.typography.headlineMedium,
                color = LinceGreenPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LinceSurfaceCard(
            variant = LinceSurfaceCardVariant.Default,
            contentPadding = LinceSpacing.medium
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(LinceSpacing.small)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinceUserAvatar(initials = iniciales, size = 64.dp)
                    Spacer(modifier = Modifier.width(LinceSpacing.small))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = nombre,
                            style = MaterialTheme.typography.titleLarge,
                            color = LinceTheme.colors.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = base.subtitulo,
                            style = MaterialTheme.typography.bodySmall,
                            color = LinceTheme.colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = usuario?.correo ?: stringResource(R.string.perfil_sesion_demo),
                            style = MaterialTheme.typography.labelSmall,
                            color = LinceTheme.colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        LinceStatusBadge(
                            text = when (usuario?.rol) {
                                RolUsuario.ADMINISTRADOR -> stringResource(R.string.rol_administrador)
                                RolUsuario.CONDUCTOR -> stringResource(R.string.rol_conductor)
                                RolUsuario.GUIA -> stringResource(R.string.rol_guia)
                                null -> stringResource(R.string.rol_participante)
                            },
                            tone = LinceStatusTone.Success
                        )
                    }

                    Spacer(modifier = Modifier.width(LinceSpacing.xSmall))
                    LinceButton(
                        text = stringResource(R.string.accion_editar),
                        onClick = { mostrarEditor = true },
                        variant = LinceButtonVariant.Outline,
                        icon = Icons.Default.Edit
                    )
                }

                LinceDivider()

                DatoPerfil(
                    icono = Icons.Default.Call,
                    etiqueta = stringResource(R.string.perfil_telefono),
                    valor = telefono
                )
                DatoPerfil(
                    icono = Icons.Default.Face,
                    etiqueta = stringResource(R.string.perfil_idiomas),
                    valor = idiomas
                )

                LinceDivider()
                DatoPerfil(
                    icono = Icons.Default.Build,
                    etiqueta = stringResource(R.string.perfil_unidad),
                    valor = unidad
                )

                mensajePerfil?.let {
                    BannerMensaje(texto = it, esError = perfilError)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LinceSurfaceCard(variant = LinceSurfaceCardVariant.Default) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(LinceSpacing.small)
            ) {
                LinceSectionHeader(
                    title = stringResource(R.string.perfil_idioma_titulo),
                    subtitle = stringResource(R.string.perfil_idioma_subtitulo),
                    action = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = LinceTheme.colors.greenPrimary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                )
                LinceDivider()
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SelectorIdiomaChip(
                        texto = stringResource(R.string.perfil_idioma_es),
                        seleccionado = idiomaActual == "es",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            alcanceCorrutina.launch {
                                LanguagePreferences.guardarIdioma(context.applicationContext, "es")
                            }
                        }
                    )
                    SelectorIdiomaChip(
                        texto = stringResource(R.string.perfil_idioma_en),
                        seleccionado = idiomaActual == "en",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            alcanceCorrutina.launch {
                                LanguagePreferences.guardarIdioma(context.applicationContext, "en")
                            }
                        }
                    )
                }

                LinceDivider()
                LinceSectionHeader(
                    title = stringResource(R.string.perfil_apariencia_titulo),
                    subtitle = if (modoOscuro) {
                        stringResource(R.string.perfil_modo_oscuro)
                    } else {
                        stringResource(R.string.perfil_modo_claro)
                    },
                    action = {
                        ThemeModeSwitch(
                            modoOscuro = modoOscuro,
                            onToggle = {
                                alcanceCorrutina.launch {
                                    LanguagePreferences.guardarModoOscuro(
                                        context.applicationContext,
                                        !modoOscuro
                                    )
                                }
                            }
                        )
                    }
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = LinceSpacing.xSmall),
                    horizontalArrangement = Arrangement.spacedBy(LinceSpacing.small)
                ) {
                    Text(
                        text = stringResource(R.string.perfil_modo_claro),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (!modoOscuro) LinceTheme.colors.greenPrimary else LinceTheme.colors.textSecondary,
                        fontWeight = if (!modoOscuro) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = stringResource(R.string.perfil_modo_oscuro),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (modoOscuro) LinceTheme.colors.greenPrimary else LinceTheme.colors.textSecondary,
                        fontWeight = if (modoOscuro) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        if (usuario != null && usuario.rol != RolUsuario.ADMINISTRADOR) {

        Spacer(modifier = Modifier.height(16.dp))

        LinceSurfaceCard(
            variant = LinceSurfaceCardVariant.Default,
            contentPadding = LinceSpacing.medium
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(LinceSpacing.small)
            ) {
                LinceSectionHeader(
                    title = stringResource(R.string.perfil_disponibilidad),
                    action = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = LinceTheme.colors.greenPrimary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                )
                LinceDivider()

                Text(
                    text = stringResource(R.string.perfil_fechas_asignadas),
                    style = MaterialTheme.typography.labelSmall,
                    color = LinceTextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )
                if (fechasServicio.isEmpty()) {
                    Text(
                        text = stringResource(R.string.perfil_sin_servicios_activos),
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
                    text = stringResource(R.string.perfil_dias_bloqueados),
                    style = MaterialTheme.typography.labelSmall,
                    color = LinceTextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )
                if (diasBloqueadosOrdenados.isEmpty()) {
                    Text(
                        text = stringResource(R.string.perfil_sin_bloqueados),
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
                                    if (onDesbloquearFecha != null) {
                                        onDesbloquearFecha(fecha)
                                    } else {
                                        val actualizados = diasBloqueados - fecha
                                        diasBloqueados = actualizados
                                        guardarDiasBloqueados(prefs, idUsuario, actualizados)
                                        mensajeDisponibilidad = context.getString(R.string.perfil_fecha_desbloqueada, fecha)
                                        disponibilidadError = false
                                    }
                                }
                            )
                        }
                    }
                }

                LinceButton(
                    text = stringResource(R.string.perfil_bloquear_fecha),
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
                                            context.getString(R.string.perfil_error_anticipacion)
                                        disponibilidadError = true
                                    }

                                    fecha in fechasServicio -> {
                                        mensajeDisponibilidad =
                                            context.getString(R.string.perfil_error_servicio, fecha)
                                        disponibilidadError = true
                                    }

                                    fecha in diasBloqueados -> {
                                        mensajeDisponibilidad = context.getString(R.string.perfil_error_ya_bloqueada, fecha)
                                        disponibilidadError = true
                                    }

                                    else -> {
                                        if (onBloquearFecha != null) {
                                            onBloquearFecha(fecha)
                                            mensajeDisponibilidad = null
                                            disponibilidadError = false
                                        } else {
                                            val actualizados = diasBloqueados + fecha
                                            diasBloqueados = actualizados
                                            guardarDiasBloqueados(prefs, idUsuario, actualizados)
                                            mensajeDisponibilidad = context.getString(R.string.perfil_fecha_bloqueada, fecha)
                                            disponibilidadError = false
                                        }
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
                    icon = Icons.Default.DateRange,
                    variant = LinceButtonVariant.Primary,
                    fullWidth = true
                )

                mensajeDisponibilidad?.let {
                    BannerMensaje(texto = it, esError = disponibilidadError)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LinceSurfaceCard(
            variant = LinceSurfaceCardVariant.Default,
            contentPadding = LinceSpacing.medium
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(LinceSpacing.small)) {
                LinceSectionHeader(
                    title = stringResource(R.string.perfil_no_asistir),
                    action = {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = LinceTheme.colors.warning,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
                LinceDivider()
                Text(
                    text = stringResource(R.string.perfil_no_asistir_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LinceTheme.colors.textSecondary
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(LinceSpacing.small)
                ) {
                    LinceButton(
                        text = stringResource(R.string.perfil_llamar_manager),
                        onClick = { },
                        variant = LinceButtonVariant.Primary,
                        icon = Icons.Default.Call,
                        fullWidth = true
                    )
                    LinceButton(
                        text = stringResource(R.string.perfil_whatsapp),
                        onClick = { },
                        variant = LinceButtonVariant.Outline,
                        icon = Icons.AutoMirrored.Filled.Send,
                        fullWidth = true
                    )
                }
            }
        }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LinceButton(
            text = stringResource(R.string.perfil_cerrar_sesion),
            onClick = onCerrarSesion,
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            variant = LinceButtonVariant.Destructive,
            fullWidth = true
        )

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
                mensajePerfil = context.getString(R.string.perfil_actualizado)
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
    LinceSectionHeader(
        title = titulo,
        action = {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = LinceTheme.colors.greenPrimary,
                modifier = Modifier.size(19.dp)
            )
        }
    )
}

@Composable
private fun DatoPerfil(
    icono: ImageVector,
    etiqueta: String,
    valor: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(LinceSpacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = LinceTheme.colors.greenPrimary,
            modifier = Modifier.size(18.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelSmall,
                color = LinceTheme.colors.textSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.bodyMedium,
                color = LinceTheme.colors.textPrimary,
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
                    contentDescription = stringResource(R.string.perfil_desbloquear, fecha),
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
private fun ThemeModeSwitch(
    modoOscuro: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val descripcion = stringResource(R.string.dashboard_cambiar_tema)
    val thumbOffset by animateDpAsState(
        targetValue = if (modoOscuro) 28.dp else 3.dp,
        animationSpec = tween(durationMillis = 220),
        label = "themeThumbOffset"
    )
    val colores = LinceTheme.colors
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .width(56.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(50))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onToggle
            )
            .semantics { contentDescription = descripcion },
        shape = RoundedCornerShape(50),
        color = LinceSurfaceContainerHigh,
        border = BorderStroke(1.dp, LinceGreenBorder)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            IconoSolLuna(
                esLuna = false,
                color = if (!modoOscuro) colores.onPrimary else colores.textSecondary.copy(alpha = 0.55f),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
                    .size(14.dp)
            )
            IconoSolLuna(
                esLuna = true,
                color = if (modoOscuro) colores.onPrimary else colores.textSecondary.copy(alpha = 0.55f),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
                    .size(14.dp)
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = thumbOffset)
                    .size(24.dp),
                shape = CircleShape,
                color = LinceGreenPrimary,
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    IconoSolLuna(
                        esLuna = modoOscuro,
                        color = colores.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun IconoSolLuna(
    esLuna: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension / 2f
        if (esLuna) {
            val circuloPrincipal = Path().apply {
                addOval(
                    androidx.compose.ui.geometry.Rect(
                        cx - r * 0.78f,
                        cy - r * 0.78f,
                        cx + r * 0.78f,
                        cy + r * 0.78f
                    )
                )
            }
            val circuloRecorte = Path().apply {
                addOval(
                    androidx.compose.ui.geometry.Rect(
                        cx - r * 0.35f,
                        cy - r * 1.02f,
                        cx + r * 1.05f,
                        cy + r * 0.38f
                    )
                )
            }
            val lunaPath = Path().apply {
                op(circuloPrincipal, circuloRecorte, PathOperation.Difference)
            }
            drawPath(path = lunaPath, color = color)
        } else {
            drawCircle(
                color = color,
                radius = r * 0.42f,
                center = Offset(cx, cy)
            )
            val rayoInicio = r * 0.62f
            val rayoFin = r * 0.92f
            for (i in 0 until 8) {
                val angulo = Math.toRadians((i * 45).toDouble())
                val cosA = cos(angulo).toFloat()
                val sinA = sin(angulo).toFloat()
                drawLine(
                    color = color,
                    start = Offset(cx + cosA * rayoInicio, cy + sinA * rayoInicio),
                    end = Offset(cx + cosA * rayoFin, cy + sinA * rayoFin),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun SelectorIdiomaChip(
    texto: String,
    seleccionado: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    LinceButton(
        text = texto,
        onClick = onClick,
        modifier = modifier,
        variant = if (seleccionado) LinceButtonVariant.Primary else LinceButtonVariant.Outline,
        fullWidth = true
    )
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
            modifier = Modifier.padding(
                start = 12.dp,
                end = 12.dp,
                top = 10.dp,
                bottom = 10.dp
            ),
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
    val mensajeCamposObligatorios = stringResource(R.string.perfil_campos_obligatorios)

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
                    text = stringResource(R.string.perfil_editar_titulo),
                    style = MaterialTheme.typography.titleLarge,
                    color = LinceGreenPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = mensajeCamposObligatorios,
                    style = MaterialTheme.typography.bodySmall,
                    color = LinceTextSecondary
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        error = null
                    },
                    label = { Text(stringResource(R.string.perfil_nombre)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = telefono,
                    onValueChange = {
                        telefono = it
                        error = null
                    },
                    label = { Text(stringResource(R.string.perfil_telefono_label)) },
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
                    label = { Text(stringResource(R.string.perfil_idiomas_label)) },
                    placeholder = { Text(stringResource(R.string.perfil_idiomas_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = unidad,
                    onValueChange = {
                        unidad = it
                        error = null
                    },
                    label = { Text(stringResource(R.string.perfil_unidad_label)) },
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
                        Text(stringResource(R.string.accion_cancelar), fontWeight = FontWeight.SemiBold)
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
                                error = mensajeCamposObligatorios
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
                            text = stringResource(R.string.perfil_guardar),
                            color = LinceOnPrimary,
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

private fun perfilBase(usuario: Usuario?, context: Context): PerfilBase {
    val nombreUsuario = usuario?.nombre?.takeIf { it.isNotBlank() }
    return when (usuario?.rol) {
        RolUsuario.CONDUCTOR -> PerfilBase(
            nombre = nombreUsuario ?: "Carlos Conductor",
            telefono = "+56 9 8412 3901",
            idiomas = context.getString(R.string.perfil_idiomas_conductor),
            unidad = "Van Mercedes Sprinter 4x4",
            subtitulo = context.getString(R.string.perfil_sub_conductor)
        )

        RolUsuario.GUIA -> PerfilBase(
            nombre = nombreUsuario ?: "Gabriela Guía",
            telefono = "+56 9 7654 3210",
            idiomas = context.getString(R.string.perfil_idiomas_guia),
            unidad = context.getString(R.string.perfil_unidad_guia),
            subtitulo = context.getString(R.string.perfil_sub_guia)
        )

        RolUsuario.ADMINISTRADOR -> PerfilBase(
            nombre = nombreUsuario ?: "Ana Administradora",
            telefono = "+56 9 6123 4567",
            idiomas = context.getString(R.string.perfil_idiomas_admin),
            unidad = context.getString(R.string.perfil_unidad_admin),
            subtitulo = context.getString(R.string.perfil_sub_admin)
        )

        null -> PerfilBase(
            nombre = nombreUsuario ?: "Operador LinceOPS",
            telefono = "+56 9 0000 0000",
            idiomas = context.getString(R.string.perfil_idiomas_admin),
            unidad = context.getString(R.string.perfil_unidad_operador),
            subtitulo = context.getString(R.string.perfil_sub_operador)
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

private fun parseFecha(fecha: String): Date? {
    val texto = fecha.trim()
    val patrones = listOf("yyyy-MM-dd", "dd/MM/yyyy")
    for (patron in patrones) {
        val parsed = runCatching {
            SimpleDateFormat(patron, Locale.ROOT).apply { isLenient = false }.parse(texto)
        }.getOrNull()
        if (parsed != null) return parsed
    }
    return null
}

private fun normalizarFechaMostrar(fecha: String): String {
    val date = parseFecha(fecha) ?: return fecha.trim()
    return SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).format(date)
}
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
