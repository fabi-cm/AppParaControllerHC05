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

    private val _showDeviceDialog = MutableStateFlow(false)
    val showDeviceDialog: StateFlow<Boolean> = _showDeviceDialog.asStateFlow()

    private val _connectionError = MutableStateFlow<String?>(null)
    val connectionError: StateFlow<String?> = _connectionError.asStateFlow()

    fun openDeviceDialog() {
        val devices = repository.getPairedDevices()
        if (devices.isEmpty()) {
            _connectionError.value = "No hay dispositivos Bluetooth emparejados"
        } else {
            _pairedDevices.value = devices
            _showDeviceDialog.value = true
        }
    }

    fun closeDeviceDialog() {
        _showDeviceDialog.value = false
        _connectionError.value = null
    }

    fun connectToDevice(device: BluetoothDevice) {
        viewModelScope.launch {
            _showDeviceDialog.value = false
            val success = repository.connectToDevice(device)
            _isConnected.value = success
            if (!success) {
                _connectionError.value = "Error al conectar con ${device.name}"
            } else {
                _connectionError.value = null
            }
        }
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

    fun connect() {
        viewModelScope.launch {
            val success = repository.connect()
            _isConnected.value = success
            if (!success) {
                _connectionError.value = "Error al conectar con el dispositivo"
            } else {
                _connectionError.value = null
            }
        }
    }

    fun disconnect() {
        repository.disconnect()
        _isConnected.value = false
    }

    fun loadPairedDevices() {
        val devices = repository.getPairedDevices()
        if (devices.isEmpty()) {
            _connectionError.value = "No hay dispositivos Bluetooth emparejados"
        } else {
            _pairedDevices.value = devices
            _connectionError.value = null
        }
    }

    fun clearError() {
        _connectionError.value = null
    }
}