package com.example.andrapp.maps.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PinEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PinDatabase : RoomDatabase() {
    abstract fun dao(): PinDao

    companion object {
        @Volatile
        private var INSTANCE: PinDatabase? = null

        fun getDatabase(context: Context): PinDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PinDatabase::class.java,
                    "pins_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}