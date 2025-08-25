package com.example.andrapp.bluetooth.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andrapp.bluetooth.data.BleConnectionState
import com.example.andrapp.bluetooth.data.BleDevice
import com.example.andrapp.bluetooth.data.BleManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BluetoothViewModel(private val bleManager: BleManager) : ViewModel() {

    private val TAG = "BluetoothViewModel"

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private val _devices = MutableStateFlow<List<BleDevice>>(emptyList())
    val devices: StateFlow<List<BleDevice>> = _devices

    val connectionStatus: StateFlow<BleConnectionState> = bleManager.connectionStatus

    private val scanTimeout: Long = 10000

    init {
        viewModelScope.launch {
            bleManager.scanResults.collect { newDevice ->
                _devices.value = _devices.value.toMutableList().apply {
                    if (!any { it.address == newDevice.address }) {
                        add(newDevice)
                    }
                }
            }
        }
    }

    fun startScan() {
        if (_isScanning.value) return

        viewModelScope.launch {
            _isScanning.value = true
            _devices.value = emptyList()

            bleManager.startScan()

            delay(scanTimeout)
            stopScan()
        }
    }

    fun stopScan() {
        if (!_isScanning.value) return

        _isScanning.value = false
        bleManager.stopScan()
    }

    fun connectToDevice(device: BleDevice) {
        val currentState = connectionStatus.value
        if(currentState is BleConnectionState.Connecting) return

        val currentDeviceAddress = when (currentState) {
            is BleConnectionState.Connected -> currentState.device.address
            is BleConnectionState.ServicesDiscovered -> currentState.device.address
            else -> null
        }

        if (currentDeviceAddress == device.address) {
            Log.d(TAG, "Already connected to ${device.address}. Ignoring request.")
            return
        }

        stopScan()
        bleManager.disconnect()

        bleManager.connectToDevice(device.device)
    }

    fun disconnect() {
        bleManager.disconnect()
    }
}