package com.example.andrapp.maps.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pins")
data class PinEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val name: String,
    val description: String?,
    val latitude: Double,
    val longitude: Double
)
