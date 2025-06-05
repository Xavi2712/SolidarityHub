package com.example.solidarityhub.android.data.dto

data class LoginResponse(
    val message: String,
    val usuario: UsuarioLoginDTO
)

data class UsuarioLoginDTO(
    val dni: String,
    val nombre: String,
    val correo: String,
    val telefono: String,
    val rol: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double
) 