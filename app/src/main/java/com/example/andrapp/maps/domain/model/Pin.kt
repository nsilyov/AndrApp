package com.example.andrapp.maps.domain.model

data class Pin(
    val id: Int = 0,
    val userId: String,
    val name: String,
    val description: String?,
    val latitude: Double,
    val longitude: Double
)