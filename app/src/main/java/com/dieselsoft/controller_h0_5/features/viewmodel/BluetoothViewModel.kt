package com.dieselsoft.controller_h0_5.features.viewmodel

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dieselsoft.controller_h0_5.data.BluetoothRepository
import com.dieselsoft.controller_h0_5.domain.SendBluetoothValueUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BluetoothViewModel : ViewModel() {

    private val repository = BluetoothRepository()
    private val sendValueUseCase = SendBluetoothValueUseCase(repository)

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    private val _currentValue = MutableStateFlow(0)
    val currentValue: StateFlow<Int> = _currentValue.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothDevice>> = _pairedDevices.asStateFlow()

    private val _connectionError = MutableStateFlow<String?>(null)
    val connectionError: StateFlow<String?> = _connectionError.asStateFlow()

    private val _connectionSuccess = MutableStateFlow<String?>(null)
    val connectionSuccess: StateFlow<String?> = _connectionSuccess.asStateFlow()

    private val _connectedDeviceName = MutableStateFlow<String?>(null)
    val connectedDeviceName: StateFlow<String?> = _connectedDeviceName.asStateFlow()

    // NUEVO: guarda el último dispositivo para reconectar rápido
    private var lastConnectedDevice: BluetoothDevice? = null

    private val _lastDeviceName = MutableStateFlow<String?>(null)
    val lastDeviceName: StateFlow<String?> = _lastDeviceName.asStateFlow()

    fun connectToDevice(device: BluetoothDevice) {
        viewModelScope.launch {
            val success = repository.connectToDevice(device)
            _isConnected.value = success
            if (!success) {
                _connectionError.value = "Error al conectar con ${device.name}"
                _connectionSuccess.value = null
                _connectedDeviceName.value = null
            } else {
                _connectionError.value = null
                _connectionSuccess.value = "Conectado exitosamente a ${device.name}"
                _connectedDeviceName.value = device.name
                // NUEVO: guardar referencia al último dispositivo
                lastConnectedDevice = device
                _lastDeviceName.value = device.name
            }
        }
    }

    /** Reconecta al último dispositivo usado, retorna false si no hay ninguno */
    fun reconnectLastDevice(): Boolean {
        val device = lastConnectedDevice ?: return false
        connectToDevice(device)
        return true
    }

    fun updateAndSendValue(value: Int) {
        viewModelScope.launch {
            _currentValue.value = value
            if (_isConnected.value) {
                sendValueUseCase.execute(value)
            }
        }
    }

    fun sendValue(value: Int) {
        viewModelScope.launch {
            if (_isConnected.value) {
                sendValueUseCase.execute(value)
            }
        }
    }

    fun disconnect() {
        repository.disconnect()
        _isConnected.value = false
        _connectedDeviceName.value = null
        _connectionSuccess.value = null
        _connectionError.value = null
        // NO limpiamos lastConnectedDevice ni _lastDeviceName para poder reconectar
    }

    fun loadPairedDevices() {
        val devices = repository.getPairedDevices()
        if (devices.isEmpty()) {
            _connectionError.value = "No hay dispositivos Bluetooth emparejados"
            _connectionSuccess.value = null
        } else {
            _pairedDevices.value = devices
            _connectionError.value = null
        }
    }

    fun clearMessages() {
        _connectionError.value = null
        _connectionSuccess.value = null
    }
}