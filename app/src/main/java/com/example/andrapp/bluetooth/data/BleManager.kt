package com.example.andrapp.bluetooth.data

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import androidx.compose.ui.res.stringResource
import com.example.andrapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class BleManager(private val context: Context) {

    private val TAG = "BleManager"

    private val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val bleScanner = bluetoothAdapter?.bluetoothLeScanner

    private val _scanResults = MutableSharedFlow<BleDevice>()
    val scanResults: SharedFlow<BleDevice> = _scanResults

    private val _connectionStatus = MutableStateFlow<BleConnectionState>(BleConnectionState.Idle)
    val connectionStatus: StateFlow<BleConnectionState> = _connectionStatus

    private var bluetoothGatt: BluetoothGatt? = null

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val deviceName = device.name ?: context.getString(R.string.unknown_device)
            coroutineScope.launch {
                _scanResults.emit(BleDevice(device, deviceName, device.address))
            }
        }

        override fun onScanFailed(errorCode: Int) {
            _connectionStatus.value = BleConnectionState.Error(errorCode)
        }
    }

    fun startScan() {
        if (bluetoothAdapter?.isEnabled == false || bleScanner == null) return

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        bleScanner.startScan(null, scanSettings, scanCallback)
    }

    fun stopScan() {
        bleScanner?.stopScan(scanCallback)
    }

    fun connectToDevice(device: BluetoothDevice) {
        _connectionStatus.value = BleConnectionState.Connecting
        device.connectGatt(context, false, gattCallback)
    }

    fun disconnect() {
        if (bluetoothGatt == null) return
        bluetoothGatt?.disconnect()
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        _connectionStatus.value = BleConnectionState.Connected(gatt.device)
                        bluetoothGatt = gatt
                        gatt.discoverServices()
                    }

                    BluetoothProfile.STATE_DISCONNECTED -> {
                        _connectionStatus.value = BleConnectionState.Disconnected()
                        gatt.close()
                        bluetoothGatt = null
                    }
                }
            } else {
                _connectionStatus.value = BleConnectionState.Error(status)
                gatt.close()
                bluetoothGatt = null
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                _connectionStatus.value = BleConnectionState.ServicesDiscovered(gatt.device)
                logDiscoveredServices(gatt)
            } else {
                _connectionStatus.value = BleConnectionState.Error(status)
            }
        }
    }

    private fun logDiscoveredServices(gatt: BluetoothGatt) {
        if (gatt.services.isEmpty()) {
            Log.d(TAG, "No service and characteristic available, call discoverServices() first?")
            return
        }

        Log.d(TAG, "Services discovered for ${gatt.device.name ?: gatt.device.address}:")
        for (service in gatt.services) {
            Log.d(TAG, "-> Service: ${service.uuid}")
            for (characteristic in service.characteristics) {
                Log.d(TAG, "  -> Characteristic: ${characteristic.uuid}")
            }
        }
    }
}