package com.example.andrapp.di


import android.app.Application
import android.content.Context
import com.example.andrapp.maps.data.local.PinDatabase
import com.example.andrapp.maps.data.repository.MapRepositoryImpl
import com.example.andrapp.maps.domain.repository.MapRepository
import com.example.andrapp.maps.domain.usecase.AddPinUseCase
import com.example.andrapp.maps.domain.usecase.GetPinsUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.andrapp.bluetooth.data.BleManager
import com.example.andrapp.bluetooth.presentation.BluetoothViewModel
import com.example.andrapp.maps.presentation.MapViewModel

object DependencyContainer {

    private lateinit var applicationContext: Context

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

    // --- Bluetooth Module Dependencies ---
    val bleManager: BleManager by lazy { BleManager(applicationContext) }

    @Suppress("UNCHECKED_CAST")
    class BluetoothViewModelFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BluetoothViewModel::class.java)) {
                return BluetoothViewModel(bleManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    // --- Maps Module Dependencies ---
    private val database: PinDatabase by lazy { PinDatabase.getDatabase(applicationContext) }
    val mapRepository: MapRepository by lazy { MapRepositoryImpl(database.dao()) }
    val getPinsUseCase: GetPinsUseCase by lazy { GetPinsUseCase(mapRepository) }
    val addPinUseCase: AddPinUseCase by lazy { AddPinUseCase(mapRepository) }

    @Suppress("UNCHECKED_CAST")
    class MapViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
                return MapViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}