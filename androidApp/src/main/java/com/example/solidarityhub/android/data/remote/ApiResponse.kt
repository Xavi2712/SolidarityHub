package com.example.solidarityhub.android.data.remote

data class UsuarioResponse(
    val dni: String,
    val nombre: String,
    val correo: String,
    val telefono: String,
    val rol: String? = null,
    val direccion: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null
)

data class ApiResponse(
    val message: String,
    val usuario: UsuarioResponse? = null
)