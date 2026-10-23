package com.example.linceops.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.linceops.model.EstadoServicio
import com.example.linceops.model.Servicio
import com.example.linceops.ui.components.ServicioCard

@Composable
fun ServiciosScreen(
    servicios: List<Servicio>,
    onVerDetalle: (String) -> Unit,
    filtroEstado: EstadoServicio?,
    filtroFecha: String?,
    onFiltrarPorEstado: (EstadoServicio?) -> Unit,
    onFiltrarPorFecha: (String?) -> Unit,
    onLimpiarFiltros: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fechaInput by remember(filtroFecha) {
        mutableStateOf(filtroFecha.orEmpty())
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = "Servicios Asignados",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp)
        )

        Text(
            text = "Filtrar por estado",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 8.dp
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filtroEstado == null,
                onClick = {
                    onFiltrarPorEstado(null)
                },
                label = {
                    Text("Todos")
                }
            )

            EstadoServicio.values().forEach { estado ->
                FilterChip(
                    selected = filtroEstado == estado,
                    onClick = {
                        onFiltrarPorEstado(
                            if (filtroEstado == estado) null else estado
                        )
                    },
                    label = {
                        Text(estado.estado)
                    }
                )
            }
        }

        OutlinedTextField(
            value = fechaInput,
            onValueChange = {
                fechaInput = it
            },
            label = {
                Text("Fecha (dd/MM/yyyy)")
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    onFiltrarPorFecha(
                        fechaInput.takeIf { it.isNotBlank() }
                    )
                }
            ) {
                Text("Filtrar por fecha")
            }

            TextButton(
                onClick = {
                    fechaInput = ""
                    onLimpiarFiltros()
                }
            ) {
                Text("Limpiar filtros")
            }
        }

        if (servicios.isEmpty()) {
            Text(
                text = "No hay servicios que coincidan con los filtros.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(servicios) { servicio ->
                    ServicioCard(
                        servicio = servicio,
                        onVerDetalle = onVerDetalle
                    )
                }
            }
        }
    }
}