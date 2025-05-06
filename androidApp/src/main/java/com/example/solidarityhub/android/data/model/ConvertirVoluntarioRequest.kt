package com.example.solidarityhub.android.data.model

data class ConvertirVoluntarioRequest(
    val dias_disp: List<String>,
    val capacidades: List<String>,
    val latitud: Double,
    val longitud: Double,
    val alcance: Int
)