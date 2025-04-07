package com.example.solidarityhub.android.data.model

import com.google.android.gms.maps.model.LatLng


data class PlaceSuggestion(
    val name: String,        // Nombre del lugar
    val placeId: String,     // ID único de Places API
    val latLng: LatLng?      // Coordenadas (puede ser null inicialmente)
)

