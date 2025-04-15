package com.example.solidarityhub.android.data.remote

import com.example.solidarityhub.android.data.dto.AfectadoADTO
import com.example.solidarityhub.android.data.dto.AfectadoBDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AfectadoApiService {
    @POST("api/AfectadoA/agregar-afectadoA")
    suspend fun addAfectadoA(@Body afectadoADto: AfectadoADTO): Response<ApiResponse>
}