package com.example.solidarityhub.android.data.dto

data class VoluntarioDTO(
    val dni: String,
    val dias_disp: List<String>,   // Manejado como array de fechas en el backend
    val provincia: String?,
    val capacidades: List<String>
)
