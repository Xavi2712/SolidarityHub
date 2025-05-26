package com.example.solidarityhub.android.data.remote

data class UsuarioResponse(
    val dni: String,
    val nombre: String,
    val correo: String,
    val telefono: String,
    val rol: String?
)

data class ApiResponse(
    val message: String,
    val usuario: UsuarioResponse? = null
)