package com.example.linceops.model

// Clase Base Padre
open class Usuario(
    open val id: String,
    open val nombre: String,
    open val correo: String,
    open val rol: RolUsuario
)

// Subclase Conductor (Hereda de Usuario)
data class Conductor(
    override val id: String,
    override val nombre: String,
    override val correo: String,
    val licencia: String,
    val vehiculoAsignado: String? = null,
    val estaDisponible: Boolean = true
) : Usuario(id, nombre, correo, RolUsuario.CONDUCTOR)

// Subclase Guía (Hereda de Usuario - Futura implementación)
data class Guia(
    override val id: String,
    override val nombre: String,
    override val correo: String,
    val idiomas: List<String> = listOf("Español"),
    val especialidad: String = "Tours Urbanos"
) : Usuario(id, nombre, correo, RolUsuario.GUIA)

// Subclase Administrador (Hereda de Usuario - Futura implementación)
data class Administrador(
    override val id: String,
    override val nombre: String,
    override val correo: String,
    val departamento: String = "Operaciones"
) : Usuario(id, nombre, correo, RolUsuario.ADMINISTRADOR)