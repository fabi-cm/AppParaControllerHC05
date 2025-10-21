package com.dieselsoft.controller_h0_5.features.viewmodel

import com.dieselsoft.controller_h0_5.data.BluetoothRepository
import com.dieselsoft.controller_h0_5.domain.SendBluetoothValueUseCase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BluetoothViewModel : ViewModel() {

    private val repository = BluetoothRepository()
    private val sendValueUseCase = SendBluetoothValueUseCase(repository)

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    fun connect() {
        viewModelScope.launch {
            val success = repository.connect()
            _isConnected.value = success
        }
    }

    fun sendValue(value: Int) {
        viewModelScope.launch {
            sendValueUseCase.execute(value)
        }
    }

    fun disconnect() {
        repository.disconnect()
        _isConnected.value = false
    }
}