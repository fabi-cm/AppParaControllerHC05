package com.dieselsoft.controller_h0_5.data

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import java.io.IOException
import java.util.*

class BluetoothRepository {

    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var socket: BluetoothSocket? = null

    private val uuid: UUID =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // SPP estándar

    fun connect(deviceName: String = "HC-05"): Boolean {
        val device = adapter?.bondedDevices?.firstOrNull { it.name == deviceName }
        if (device == null) return false

        return try {
            socket = device.createRfcommSocketToServiceRecord(uuid)
            socket?.connect()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun sendValue(value: Int) {
        try {
            socket?.outputStream?.write("$value\n".toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        try {
            socket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
