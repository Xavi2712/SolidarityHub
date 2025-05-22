package com.example.solidarityhub.android.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AsignarNecesidadRequest(
    val afectado: String,
    val nombre: String,
    val descripcion: String,
    val zona: String = "Valencia"
) 