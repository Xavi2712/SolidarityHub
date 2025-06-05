package com.example.solidarityhub.android.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    const val BASE_URL = "http://10.0.2.2:5127/" // Para el emulador de Android - Puerto correcto del backend
    // const val BASE_URL = "http://localhost:5127/" // Para dispositivo físico
    // const val BASE_URL = "https://10.0.2.2:7115/" // Para HTTPS con emulador
    
    const val GOOGLE_MAPS_BASE_URL = "https://maps.googleapis.com/maps/api/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val googleMapsRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(GOOGLE_MAPS_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val voluntarioApiService: VoluntarioApiService by lazy {
        retrofit.create(VoluntarioApiService::class.java)
    }

    val usuarioApiService: UsuarioApiService by lazy {
        retrofit.create(UsuarioApiService::class.java)
    }

    val afectadoApiService: AfectadoApiService by lazy {
        retrofit.create(AfectadoApiService::class.java)
    }

    val necesidadApiService: NecesidadApiService by lazy {
        retrofit.create(NecesidadApiService::class.java)
    }

    val tareaApiService: TareaApiService by lazy {
        retrofit.create(TareaApiService::class.java)
    }
    
    val directionsApiService: DirectionsApiService by lazy {
        googleMapsRetrofit.create(DirectionsApiService::class.java)
    }
}