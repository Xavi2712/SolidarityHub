package com.example.solidarityhub.android.data.dto

data class DirectionsResponse(
    val routes: List<Route>,
    val status: String
)

data class Route(
    val overview_polyline: OverviewPolyline,
    val legs: List<Leg>
)

data class OverviewPolyline(
    val points: String
)

data class Leg(
    val distance: Distance,
    val duration: Duration,
    val start_address: String,
    val end_address: String
)

data class Distance(
    val text: String,
    val value: Int
)

data class Duration(
    val text: String,
    val value: Int
) 