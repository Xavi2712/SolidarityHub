package com.example.solidarityhub.android.data.remote

import com.example.solidarityhub.android.data.dto.AfectadoADTO
import com.example.solidarityhub.android.data.model.NecesidadRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class UbicacionRequest(
    val latitud: Double,
    val longitud: Double,
    val direccion: String
    // Agrega otros campos que estés enviando
)

interface AfectadoApiService {
    @POST("api/AfectadoA/convertir-usuario/{dni}")
    suspend fun convertirUsuarioAfectado(
        @Path("dni") dni: String,
        @Body ubicacionData: UbicacionRequest
    ): Response<ApiResponse>

    @GET("api/AfectadoA/obtener/todos")
    suspend fun getAllAfectados(): Response<List<AfectadoADTO>>

    @POST("api/Necesidad")
    suspend fun crearNecesidad(@Body req: NecesidadRequest)
}


