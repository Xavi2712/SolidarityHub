package com.example.solidarityhub.android.data.remote

data class ApiResponse(
    val success: Boolean,
    val message: String,
    val data: Any? = null,
    val token: String? = null,
    val userName: String? = null,
    val role: String? = null
)