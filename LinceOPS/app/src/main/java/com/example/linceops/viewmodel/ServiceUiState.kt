package com.example.linceops.viewmodel

import com.example.linceops.model.EstadoServicio
import com.example.linceops.model.Servicio
import com.example.linceops.model.Usuario
import com.example.linceops.network.dto.GeocercaDto
import com.example.linceops.network.dto.GuiaDto
import com.example.linceops.network.dto.HitoDto
import com.example.linceops.network.dto.IncidenciaDto
import com.example.linceops.network.dto.ResumenDto
import com.example.linceops.network.dto.TransportistaDto

data class ServiceUiState(
    val usuarioLogueado: Usuario? = null,
    // Lista filtrada que consume la pantalla de Servicios.
    val servicios: List<Servicio> = emptyList(),
    // Lista completa del usuario. La usa Inicio para no heredar los filtros.
    val serviciosTotales: List<Servicio> = emptyList(),
    val errorLogin: String? = null,
    val filtroEstado: EstadoServicio? = null,
    val filtroFecha: String? = null,
    val filtroOficina: String? = null,
    val filtroStatusPL: String? = null,
    val mensaje: String? = null,
    val error: String? = null,
    val diasBloqueados: List<String> = emptyList(),
    val resumenOperativo: ResumenDto? = null,
    val guiasSouthbound: List<GuiaDto> = emptyList(),
    val transportistasSouthbound: List<TransportistaDto> = emptyList(),
    val geocercas: List<GeocercaDto> = emptyList(),
    val hitosCoordinacion: List<HitoDto> = emptyList(),
    val incidenciasCoordinacion: List<IncidenciaDto> = emptyList(),

    val hitosPendientesSync: Int = 0,
    val sincronizandoOffline: Boolean = false,
    val errorSyncOffline: String? = null
)