package com.example.andrapp.maps.data.mappers

import com.example.andrapp.maps.data.local.PinEntity
import com.example.andrapp.maps.domain.model.Pin

fun PinEntity.toDomain(): Pin {
    return Pin(
        id = this.id,
        userId = this.userId,
        name = this.name,
        description = this.description,
        latitude = this.latitude,
        longitude = this.longitude
    )
}

fun Pin.toDataEntity(): PinEntity {
    return PinEntity(
        id = this.id,
        userId = this.userId,
        name = this.name,
        description = this.description,
        latitude = this.latitude,
        longitude = this.longitude
    )
}