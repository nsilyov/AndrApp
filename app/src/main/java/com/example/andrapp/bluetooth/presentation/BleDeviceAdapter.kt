package com.example.andrapp.bluetooth.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.andrapp.R
import com.example.andrapp.bluetooth.data.BleDevice

class BleDeviceAdapter(
    private val devices: MutableList<BleDevice>,
    private val onItemClick: (BleDevice) -> Unit
) : RecyclerView.Adapter<BleDeviceAdapter.DeviceViewHolder>() {

    class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceName: TextView = view.findViewById(R.id.tv_device_name)
        val deviceAddress: TextView = view.findViewById(R.id.tv_device_address)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ble_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]
        holder.deviceName.text = device.name
        holder.deviceAddress.text = device.address
        holder.itemView.setOnClickListener { onItemClick(device) }
    }

    override fun getItemCount() = devices.size

    fun addDevice(device: BleDevice) {
        if (!devices.any { it.address == device.address }) {
            devices.add(device)
            notifyItemInserted(devices.size - 1)
        }
    }

    fun clear() {
        devices.clear()
        notifyDataSetChanged()
    }
}