package com.dieselsoft.controller_h0_5.features.viewmodel

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dieselsoft.controller_h0_5.data.BluetoothRepository
import com.dieselsoft.controller_h0_5.domain.SendBluetoothValueUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BluetoothViewModel : ViewModel() {
    private val repository = BluetoothRepository()
    private val sendValueUseCase = SendBluetoothValueUseCase(repository)

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    private val _currentValue = MutableStateFlow(0)
    val currentValue = _currentValue.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val pairedDevices = _pairedDevices.asStateFlow()

    private val _connectionError = MutableStateFlow<String?>(null)
    val connectionError = _connectionError.asStateFlow()

    private val _connectionSuccess = MutableStateFlow<String?>(null)
    val connectionSuccess = _connectionSuccess.asStateFlow()

    private val _connectedDeviceName = MutableStateFlow<String?>(null)
    val connectedDeviceName = _connectedDeviceName.asStateFlow()

    private val _simulationMode = MutableStateFlow(false)
    val simulationMode = _simulationMode.asStateFlow()

    private var lastConnectedDevice: BluetoothDevice? = null
    private val _lastDeviceName = MutableStateFlow<String?>(null)
    val lastDeviceName = _lastDeviceName.asStateFlow()

    fun checkBluetoothAvailability() {
        if (!repository.isBluetoothAvailable()) {
            _connectionError.value = "Bluetooth desactivado"
        }
        loadPairedDevices()
    }

    fun enableSimulationMode() { _simulationMode.value = true }
    fun disableSimulationMode() { _simulationMode.value = false }

    fun connectToDevice(device: BluetoothDevice) {
        viewModelScope.launch {
            _connectionError.value = null
            val success = repository.connectToDevice(device)
            _isConnected.value = success
            if (success) {
                _connectionSuccess.value = "Conectado a ${device.name}"
                _connectedDeviceName.value = device.name
                lastConnectedDevice = device
                _lastDeviceName.value = device.name
            } else {
                _connectionError.value = "Error al conectar"
            }
        }
    }

    fun reconnectLastDevice(): Boolean {
        val device = lastConnectedDevice ?: return false
        connectToDevice(device)
        return true
    }

    fun updateAndSendValue(value: Int) {
        viewModelScope.launch {
            _currentValue.value = value
            if (_isConnected.value || _simulationMode.value) {
                sendValueUseCase.execute(value)
            }
        }
    }

    fun sendValue(value: Int) {
        viewModelScope.launch {
            if (_isConnected.value || _simulationMode.value) {
                sendValueUseCase.execute(value)
            }
        }
    }

    fun disconnect() {
        repository.disconnect()
        _isConnected.value = false
        _connectedDeviceName.value = null
        _connectionSuccess.value = null
    }

    fun loadPairedDevices() {
        _pairedDevices.value = repository.getPairedDevices()
    }

    fun clearMessages() {
        _connectionError.value = null
        _connectionSuccess.value = null
    }
}