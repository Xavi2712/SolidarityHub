package com.example.solidarityhub.android.data.remote

import com.example.solidarityhub.android.data.dto.TareaDTO
import com.example.solidarityhub.android.data.dto.TareaAfectadoApiDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.PUT

interface TareaApiService {
    @GET("api/Tarea/voluntario/{dni}")
    suspend fun getTareasVoluntario(
        @Path("dni") dni: String
    ): Response<List<TareaDTO>>
    
    @GET("api/Tarea/afectado/{dni}")
    suspend fun getTareasAfectado(
        @Path("dni") dni: String
    ): Response<List<TareaAfectadoApiDTO>>

    @POST("api/Tarea/asignar-voluntario")
    suspend fun asignarVoluntario(
        @Body request: AsignarVoluntarioRequest
    ): Response<Unit>

    @PUT("api/Tarea/{id}/finalizar")
    suspend fun finalizarTarea(
        @Path("id") tareaId: Int
    ): Response<Unit>
}

data class AsignarVoluntarioRequest(
    val tarea_id: Int,
    val voluntario_dni: String
) 