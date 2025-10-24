package com.dieselsoft.controller_h0_5.domain

import com.dieselsoft.controller_h0_5.data.BluetoothRepository

class SendBluetoothValueUseCase(
    private val repository: BluetoothRepository
) {
    suspend fun execute(value: Int) {
        repository.sendValue(value)
    }
}
