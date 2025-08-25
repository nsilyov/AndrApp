package com.example.andrapp.maps.domain.repository

import com.example.andrapp.maps.domain.model.Pin
import com.example.andrapp.util.Resource
import kotlinx.coroutines.flow.Flow

interface MapRepository {
    fun getPinsForUser(userId: String): Flow<Resource<List<Pin>>>
    suspend fun addPin(pin: Pin)
}