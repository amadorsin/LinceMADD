package com.example.linceops.ui.screens

import com.example.linceops.network.dto.HitoDto
import com.example.linceops.network.dto.IncidenciaDto
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale

private val patronFechaHoraIso =
    Regex("""^(\d{4})-(\d{2})-(\d{2})[T ](\d{2}):(\d{2})(?::\d{2}(?:\.\d+)?)?(?:Z|[+-]\d{2}:?\d{2})?$""")

internal fun ordenarHitosPorFechaDescendente(hitos: List<HitoDto>): List<HitoDto> =
    hitos.sortedWith(
        compareByDescending<HitoDto> { claveFechaOrdenable(it.timestampRegistro) }
            .thenByDescending { it.id ?: 0L }
    )

internal fun ordenarIncidenciasPorFechaDescendente(
    incidencias: List<IncidenciaDto>
): List<IncidenciaDto> =
    incidencias.sortedWith(
        compareByDescending<IncidenciaDto> { claveFechaOrdenable(it.fechaHora) }
            .thenByDescending { it.id ?: 0L }
    )

internal fun formatearFechaIsoCorta(iso: String?): String {
    if (iso.isNullOrBlank()) return "Sin fecha"
    val match = partesFechaIso(iso) ?: return iso
    val (anio, mes, dia, hora, minuto) = match.destructured
    return "$dia/$mes/$anio $hora:$minuto"
}

private fun claveFechaOrdenable(iso: String?): String =
    partesFechaIso(iso)?.value.orEmpty()

private fun partesFechaIso(iso: String?): MatchResult? {
    val valor = iso?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val match = patronFechaHoraIso.matchEntire(valor) ?: return null
    val (anio, mes, dia, hora, minuto) = match.destructured
    val fechaParaValidar = "$anio-$mes-$dia" + "T$hora:$minuto:00"
    val formato = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT).apply {
        isLenient = false
    }
    val posicion = ParsePosition(0)
    formato.parse(fechaParaValidar, posicion) ?: return null
    if (posicion.index != fechaParaValidar.length) return null
    return match
}
