package com.example.solidarityhub.android.data.remote

import com.example.solidarityhub.android.data.dto.UsuarioDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UsuarioApiService {
    @POST("api/Usuario")
    suspend fun registrarUsuario(
        @Body usuarioDTO: UsuarioDTO
    ): Response<ApiResponse>

    @POST("api/Auth/login")
    suspend fun loginUsuario(
        @Body loginDTO: Map<String, String>
    ): Response<ApiResponse>
}