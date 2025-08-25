package com.example.andrapp.bluetooth.data

import android.bluetooth.BluetoothDevice

sealed class BleConnectionState {
    object Idle : BleConnectionState()
    object Connecting : BleConnectionState()
    data class Connected(val device: BluetoothDevice) : BleConnectionState()
    data class ServicesDiscovered(val device: BluetoothDevice) : BleConnectionState()
    data class Disconnected(val reason: String? = null) : BleConnectionState()
    data class Error(val errorStatus: Int) : BleConnectionState()
}