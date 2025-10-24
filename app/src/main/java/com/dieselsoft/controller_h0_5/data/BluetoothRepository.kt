package com.dieselsoft.controller_h0_5.data

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

    suspend fun connect(deviceName: String = "HC-05"): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Buscando dispositivo: $deviceName")

            // Verificar si Bluetooth está disponible
            if (adapter == null) {
                Log.e(TAG, "Bluetooth no disponible")
                return@withContext false
            }

            // Buscar dispositivo emparejado
            val device: BluetoothDevice? = adapter.bondedDevices?.firstOrNull {
                it.name.equals(deviceName, ignoreCase = true)
            }

            if (device == null) {
                Log.e(TAG, "Dispositivo $deviceName no encontrado en dispositivos emparejados")
                Log.d(TAG, "Dispositivos emparejados: ${adapter.bondedDevices?.map { it.name }}")
                return@withContext false
            }

            Log.d(TAG, "Dispositivo encontrado: ${device.name} - ${device.address}")

            // Crear socket y conectar
            socket = device.createRfcommSocketToServiceRecord(uuid)
            socket?.let {
                it.connect()
                connected = true
                Log.d(TAG, "Conexión exitosa")
                return@withContext true
            }

            return@withContext false

        } catch (e: IOException) {
            Log.e(TAG, "Error de conexión: ${e.message}", e)
            connected = false
            return@withContext false
        } catch (e: SecurityException) {
            Log.e(TAG, "Error de permisos: ${e.message}", e)
            connected = false
            return@withContext false
        }
    }

    suspend fun sendValue(value: Int) = withContext(Dispatchers.IO) {
        if (!connected || socket == null) {
            Log.e(TAG, "No conectado, no se puede enviar valor")
            return@withContext
        }

        try {
            val message = "$value\n"
            socket?.outputStream?.write(message.toByteArray())
            Log.d(TAG, "Valor enviado: $value")
        } catch (e: IOException) {
            Log.e(TAG, "Error enviando dato: ${e.message}", e)
            connected = false
        } catch (e: SecurityException) {
            Log.e(TAG, "Error de permisos al enviar: ${e.message}", e)
        }
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            socket?.close()
            connected = false
            Log.d(TAG, "Desconectado")
        } catch (e: IOException) {
            Log.e(TAG, "Error al desconectar: ${e.message}", e)
        }
    }

    fun isBluetoothAvailable(): Boolean {
        return adapter != null && adapter.isEnabled
    }

    fun getPairedDevices(): List<String> {
        return adapter?.bondedDevices?.map { it.name ?: "Desconocido" } ?: emptyList()
    }
}