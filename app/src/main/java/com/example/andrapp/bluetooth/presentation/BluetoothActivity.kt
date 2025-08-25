package com.example.andrapp.bluetooth.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.andrapp.R
import com.example.andrapp.bluetooth.data.BleConnectionState
import com.example.andrapp.di.DependencyContainer
import kotlinx.coroutines.launch

class BluetoothActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var btnScan: Button
    private lateinit var btnDisconnect: Button
    private lateinit var rvDevices: RecyclerView

    private val deviceAdapter = BleDeviceAdapter(mutableListOf()) { device ->
        viewModel.connectToDevice(device)
    }

    private val viewModel: BluetoothViewModel by viewModels {
        DependencyContainer.BluetoothViewModelFactory()
    }

    private val requestBluetoothEnable = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.startScan()
        } else {
            Toast.makeText(this, getString(R.string.bluetooth_must_be_enabled), Toast.LENGTH_SHORT).show()
        }
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            checkBluetoothAndStartScan()
        } else {
            Toast.makeText(this, getString(R.string.permissions_not_granted), Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)

        tvStatus = findViewById(R.id.tv_status)
        btnScan = findViewById(R.id.btn_scan)
        btnDisconnect = findViewById(R.id.btn_disconnect)
        rvDevices = findViewById(R.id.rv_devices)

        rvDevices.apply {
            layoutManager = LinearLayoutManager(this@BluetoothActivity)
            adapter = deviceAdapter
        }

        btnScan.setOnClickListener {
            if (viewModel.isScanning.value) {
                viewModel.stopScan()
            } else {
                checkPermissionsAndStartScan()
            }
        }

        btnDisconnect.setOnClickListener {
            viewModel.disconnect()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isScanning.collect { isScanning ->
                        btnScan.text =
                            resources.getString(if (isScanning) R.string.stop_scan else R.string.start_scan)
                    }
                }
                launch {
                    viewModel.devices.collect { devices ->
                        deviceAdapter.clear()
                        devices.forEach { deviceAdapter.addDevice(it) }
                    }
                }
                launch {
                    viewModel.connectionStatus.collect { state ->
                        val statusMessage: String = state.toUIString()
                        if (state is BleConnectionState.Connected){
                            btnScan.visibility = View.GONE
                            btnDisconnect.visibility = View.VISIBLE
                            Toast.makeText(this@BluetoothActivity, getString(R.string.connected), Toast.LENGTH_SHORT).show()
                        }
                        if(state is BleConnectionState.Disconnected){
                            btnScan.visibility = View.VISIBLE
                            btnDisconnect.visibility = View.GONE
                            Toast.makeText(this@BluetoothActivity, getString(R.string.disconnected), Toast.LENGTH_SHORT).show()
                        }
                        tvStatus.text = statusMessage
                    }
                }
            }
        }
    }

    private fun checkPermissionsAndStartScan() {
        val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val allPermissionsGranted = permissionsToRequest.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allPermissionsGranted) {
            checkBluetoothAndStartScan()
        } else {
            requestPermissionsLauncher.launch(permissionsToRequest)
        }
    }

    private fun checkBluetoothAndStartScan() {
        val bluetoothAdapter = (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter

        if (bluetoothAdapter == null) {
            Toast.makeText(this, getString(R.string.device_does_not_support_bluetooth), Toast.LENGTH_SHORT).show()
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetoothEnable.launch(enableBtIntent)
        } else {
            viewModel.startScan()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun BleConnectionState.toUIString(): String {
        return when (this) {
            is BleConnectionState.Idle -> getString(R.string.status_idle)
            is BleConnectionState.Connecting -> getString(R.string.status_connecting)
            is BleConnectionState.Connected -> getString(R.string.status_connected_discovering)

            is BleConnectionState.ServicesDiscovered -> {
                var deviceName = this.device.name
                if (deviceName == null) {
                    val discoveredDevice =
                        viewModel.devices.value.find { it.address == this.device.address }
                    deviceName = discoveredDevice?.name ?: this.device.address
                }

                getString(R.string.status_ready_to_communicate, deviceName)
            }
            is BleConnectionState.Disconnected -> getString(R.string.disconnected)
            is BleConnectionState.Error -> getString(R.string.status_error)
        }
    }
}