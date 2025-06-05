package com.example.solidarityhub.android.service

import com.example.solidarityhub.android.data.dto.AfectadoADTO
import retrofit2.Response
import retrofit2.http.GET

interface AfectadoService {
    @GET("api/AfectadoA/obtener/todos")
    suspend fun obtenerTodosAfectados(): Response<List<AfectadoADTO>>
} 