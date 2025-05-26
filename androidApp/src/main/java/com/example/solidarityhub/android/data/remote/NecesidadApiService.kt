package com.example.solidarityhub.android.data.remote

import com.example.solidarityhub.android.data.model.NecesidadRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface NecesidadApiService {
    @POST("api/Necesidad/registrar")
    suspend fun registrarNecesidad(
        @Body request: NecesidadRequest
    ): Response<ApiResponse>
} 