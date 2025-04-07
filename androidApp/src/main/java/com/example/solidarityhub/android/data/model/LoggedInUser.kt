package com.example.solidarityhub.android.data.model

import com.google.android.gms.maps.model.LatLng

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser(
    val userId: String,
    val displayName: String
)