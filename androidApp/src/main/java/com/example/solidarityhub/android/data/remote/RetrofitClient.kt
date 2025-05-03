package com.example.solidarityhub.android.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:5127/"

    val voluntarioApiService: VoluntarioApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VoluntarioApiService::class.java)
    }

    val usuarioApiService: UsuarioApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UsuarioApiService::class.java)
    }

    // Agregar esta línea dentro del objeto RetrofitClient:
    val afectadoApiService: AfectadoApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AfectadoApiService::class.java)
    }
}