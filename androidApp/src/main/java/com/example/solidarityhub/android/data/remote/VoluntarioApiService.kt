package com.example.solidarityhub.android.data.remote

import com.example.solidarityhub.android.data.model.ConvertirVoluntarioRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface VoluntarioApiService {
    @POST("api/Usuario/convertirVoluntario/{dni}")
    suspend fun convertirVoluntario(
        @Path("dni") dni: String,
        @Body request: ConvertirVoluntarioRequest
    ): Response<ResponseBody>
}