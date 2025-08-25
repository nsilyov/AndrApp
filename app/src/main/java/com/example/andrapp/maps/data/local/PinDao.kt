package com.example.andrapp.maps.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PinDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPin(pin: PinEntity)

    @Query("SELECT * FROM pins WHERE userId = :userId")
    fun getPinsForUser(userId: String): Flow<List<PinEntity>>
}