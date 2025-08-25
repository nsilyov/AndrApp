package com.example.andrapp.maps.domain.usecase

import com.example.andrapp.maps.domain.model.Pin
import com.example.andrapp.maps.domain.repository.MapRepository
import com.example.andrapp.util.Resource
import kotlinx.coroutines.flow.Flow

class GetPinsUseCase(private val mapRepository: MapRepository) {
    operator fun invoke(userId: String): Flow<Resource<List<Pin>>> = mapRepository.getPinsForUser(userId)
}