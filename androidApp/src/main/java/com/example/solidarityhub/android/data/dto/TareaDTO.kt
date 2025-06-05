package com.example.solidarityhub.android.data.dto

import com.google.gson.annotations.SerializedName

data class TareaDTO(
    val id: Int,
    val descripcion: String,
    @SerializedName("fecha_inicio")
    val fechaInicio: String,
    @SerializedName("fecha_final")
    val fechaFinal: String,
    @SerializedName("hora_inicio")
    val horaInicio: String,
    @SerializedName("hora_final")
    val horaFinal: String,
    val cubre: Int,
    val nombre: String,
    val ayudaA: String,
    val estado: String,
    val afectadoNombre: String,
    val afectadoTelefono: String
) 