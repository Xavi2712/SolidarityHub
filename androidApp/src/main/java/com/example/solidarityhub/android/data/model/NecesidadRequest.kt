package com.example.solidarityhub.android.data.model

data class NecesidadRequest(
    val afectado: String,
    val descripcion: String,
    val nombre: String,
    val zona: String,
    val estado: String = "SIN COMENZAR",
    val fecha_creacion: String
)