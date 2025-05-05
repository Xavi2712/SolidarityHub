package com.example.solidarityhub.android.data.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AfectadoADTO(
    val dni: String,
    val nombre: String,
    val telefono: String,

    // Aquí mapeas la variable contrasena a "contraseña" en el JSON
    @SerializedName("contraseña")
    val contraseña: String,

    val correo: String,
    val rol: String,
    val direccion: String,
    val longitud : Double,
    val latitud : Double,
    val necesidades : List<String>
) : Parcelable
