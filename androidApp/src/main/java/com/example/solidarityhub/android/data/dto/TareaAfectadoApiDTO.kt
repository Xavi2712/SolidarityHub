package com.example.solidarityhub.android.data.dto

import com.google.gson.annotations.SerializedName

data class TareaAfectadoApiDTO(
    val id: Int,
    val descripcion: String,
    val nombre: String,
    val estado: String,
    @SerializedName("fecha_inicio")
    val fechaInicio: String?,
    val voluntarios: List<String>?
) 