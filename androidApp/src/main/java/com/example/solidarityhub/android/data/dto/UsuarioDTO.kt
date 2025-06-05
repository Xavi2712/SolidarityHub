package com.example.solidarityhub.android.data.dto

data class UsuarioDTO(
    val dni: String,
    val nombre: String,
    val contraseña: String? = null,
    val correo: String,
    val telefono: String,
    val rol: String? = null,
    val direccion: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null
) 