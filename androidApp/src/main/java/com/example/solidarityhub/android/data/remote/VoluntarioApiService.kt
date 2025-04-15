package com.example.solidarityhub.android.data.remote

import com.example.solidarityhub.android.data.dto.VoluntarioDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface VoluntarioApiService {

    @POST("api/Voluntario/add-voluntario")
    suspend fun addVoluntario(
        @Body voluntarioDTO: VoluntarioDTO
    ): Response<ApiResponse>
}
