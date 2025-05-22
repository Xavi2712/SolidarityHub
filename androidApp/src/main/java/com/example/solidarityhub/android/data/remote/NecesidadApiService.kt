package com.example.solidarityhub.android.data.remote

import com.example.solidarityhub.android.data.model.AsignarNecesidadRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface NecesidadApiService {
    @POST("api/Necesidad/asignarA")
    suspend fun asignarNecesidad(
        @Body request: AsignarNecesidadRequest
    ): Response<ResponseBody>
} 