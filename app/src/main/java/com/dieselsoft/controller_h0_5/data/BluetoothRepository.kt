package com.dieselsoft.controller_h0_5.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

class BluetoothRepository {
    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var socket: BluetoothSocket? = null
    private var connected = false
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    companion object {
        private const val TAG = "BluetoothRepository"
    }

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice> {
        return try {
            adapter?.bondedDevices?.toList() ?: emptyList()
        } catch (e: SecurityException) {
            Log.e(TAG, "Error de permisos", e)
            emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun connectToDevice(device: BluetoothDevice): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            socket?.close()
            socket = device.createRfcommSocketToServiceRecord(uuid)
            socket?.connect()
            connected = true
            Log.d(TAG, "Conectado a ${device.name}")
            true
        } catch (e: IOException) {
            Log.w(TAG, "Error inicial, intentando reflexión...", e)
            try {
                val method = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                socket = method.invoke(device, 1) as BluetoothSocket
                socket?.connect()
                connected = true
                true
            } catch (e2: Exception) {
                Log.e(TAG, "Fallo total de conexión", e2)
                connected = false
                false
            }
        }
    }

    fun sendValue(value: Int) {
        if (!connected || socket == null) return
        try {
            socket?.outputStream?.write("$value\n".toByteArray())
            Log.d(TAG, "Enviado: $value")
        } catch (e: IOException) {
            Log.e(TAG, "Error en envío", e)
            connected = false
        }
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            socket?.close()
            socket = null
            connected = false
            Log.d(TAG, "Desconectado")
        } catch (e: IOException) {
            Log.e(TAG, "Error al desconectar", e)
        }
    }

    fun isBluetoothAvailable(): Boolean = adapter != null && adapter.isEnabled
}