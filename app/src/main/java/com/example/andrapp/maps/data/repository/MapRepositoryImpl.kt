package com.example.andrapp.maps.data.repository

import com.example.andrapp.maps.data.local.PinDao
import com.example.andrapp.maps.data.local.PinEntity
import com.example.andrapp.maps.data.mappers.toDataEntity
import com.example.andrapp.maps.data.mappers.toDomain
import com.example.andrapp.maps.domain.model.Pin
import com.example.andrapp.maps.domain.repository.MapRepository
import com.example.andrapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class MapRepositoryImpl(private val pinDao: PinDao) : MapRepository {

    override fun getPinsForUser(userId: String): Flow<Resource<List<Pin>>> {
        return pinDao.getPinsForUser(userId)
            .map<List<PinEntity>, Resource<List<Pin>>> { entities ->
                Resource.Success(entities.map { it.toDomain() })
            }
            .onStart {
                emit(Resource.Loading())
            }
            .catch { e ->
                emit(Resource.Error(e.message ?: "An unknown database error occurred"))
            }
    }

    override suspend fun addPin(pin: Pin) {
        pinDao.insertPin(pin.toDataEntity())
    }
}