package com.example.andrapp.maps.domain.usecase

import com.example.andrapp.maps.domain.model.Pin
import com.example.andrapp.maps.domain.repository.MapRepository

class AddPinUseCase(private val mapRepository: MapRepository) {
    suspend operator fun invoke(pin: Pin) {
        mapRepository.addPin(pin)
    }
}