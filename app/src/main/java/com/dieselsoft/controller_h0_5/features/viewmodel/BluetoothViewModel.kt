package com.dieselsoft.controller_h0_5.features.viewmodel

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

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _bluetoothAvailable = MutableStateFlow(false)
    val bluetoothAvailable = _bluetoothAvailable.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<String>>(emptyList())
    val pairedDevices = _pairedDevices.asStateFlow()

    fun checkBluetoothAvailability() {
        _bluetoothAvailable.value = repository.isBluetoothAvailable()
        _pairedDevices.value = repository.getPairedDevices()

        if (!_bluetoothAvailable.value) {
            _errorMessage.value = "Bluetooth no disponible o desactivado"
        }
    }

    private val _simulationMode = MutableStateFlow(false)
    val simulationMode = _simulationMode.asStateFlow()

    fun enableSimulationMode() {
        _simulationMode.value = true
        _isConnected.value = true
        _bluetoothAvailable.value = true
    }

    fun disableSimulationMode() {
        _simulationMode.value = false
        _isConnected.value = false
    }

    fun connect() {
        viewModelScope.launch {
            _errorMessage.value = null
            val success = repository.connect()
            _isConnected.value = success

            if (!success) {
                _errorMessage.value = "No se pudo conectar al HC-05. Verifica:\n" +
                        "1. Que el HC-05 esté emparejado\n" +
                        "2. Que esté encendido\n" +
                        "3. Dispositivos emparejados: ${_pairedDevices.value}"
            }
        }
    }

    fun sendValue(value: Int) {
        viewModelScope.launch {
            if (_isConnected.value) {
                sendValueUseCase.execute(value)
            } else {
                _errorMessage.value = "No conectado. Conéctate primero al HC-05"
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            repository.disconnect()
            _isConnected.value = false
        }
    }

    fun setError(message: String) {
        _errorMessage.value = message
    }

    fun clearError() {
        _errorMessage.value = null
    }

    val effectiveConnected: Boolean
        get() = isConnected.value || simulationMode.value
}