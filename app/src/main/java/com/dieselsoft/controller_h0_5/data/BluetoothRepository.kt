package com.dieselsoft.controller_h0_5.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

class BluetoothRepository {

    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var socket: BluetoothSocket? = null

    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    /**
     * Obtiene la lista de dispositivos Bluetooth emparejados
     */
    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice> {
        return try {
            adapter?.bondedDevices?.toList() ?: emptyList()
        } catch (e: SecurityException) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Conecta a un dispositivo Bluetooth específico
     */
    @SuppressLint("MissingPermission")
    suspend fun connectToDevice(device: BluetoothDevice): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            // Cerrar conexión anterior si existe
            socket?.close()

            // Crear socket y conectar
            socket = device.createRfcommSocketToServiceRecord(uuid)
            socket?.connect()
            true
        } catch (e: IOException) {
            e.printStackTrace()

            // Intento alternativo usando reflexión (fallback method)
            try {
                val method = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                socket = method.invoke(device, 1) as BluetoothSocket
                socket?.connect()
                true
            } catch (e2: Exception) {
                e2.printStackTrace()
                false
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Método legacy para mantener compatibilidad (opcional)
     */
    @SuppressLint("MissingPermission")
    suspend fun connect(deviceName: String = "HC-05"): Boolean = withContext(Dispatchers.IO) {
        val device = adapter?.bondedDevices?.firstOrNull { it.name == deviceName }
        if (device == null) return@withContext false

        return@withContext try {
            socket = device.createRfcommSocketToServiceRecord(uuid)
            socket?.connect()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Envía un valor al dispositivo conectado
     */
    fun sendValue(value: Int) {
        try {
            socket?.outputStream?.write("$value\n".toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Desconecta del dispositivo Bluetooth
     */
    fun disconnect() {
        try {
            socket?.close()
            socket = null
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}