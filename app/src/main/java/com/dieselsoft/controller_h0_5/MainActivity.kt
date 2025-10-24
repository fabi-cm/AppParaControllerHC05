package com.dieselsoft.controller_h0_5

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import com.dieselsoft.controller_h0_5.features.view.BluetoothScreen
import com.dieselsoft.controller_h0_5.features.viewmodel.BluetoothViewModel
import com.dieselsoft.controller_h0_5.ui.theme.Controllerh05Theme

class MainActivity : ComponentActivity() {
    private val viewModel: BluetoothViewModel by viewModels()
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            viewModel.checkBluetoothAvailability()
        } else {
            viewModel.setError("Permisos de Bluetooth denegados")
        }
    }

    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.checkBluetoothAvailability()
        } else {
            viewModel.setError("Bluetooth debe estar activado")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Controllerh05Theme {
                LaunchedEffect(Unit) {
                    viewModel.checkBluetoothAvailability()
                }

                BluetoothScreen(
                    viewModel = viewModel,
                    onRequestPermissions = { requestBluetoothPermissions() },
                    onEnableBluetooth = { enableBluetooth() }
                )
            }
        }
    }

    private fun requestBluetoothPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        requestPermissionLauncher.launch(permissions)
    }

    private fun enableBluetooth() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            viewModel.setError("Este dispositivo no soporta Bluetooth")
            return
        }

        if (!adapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
        } else {
            viewModel.checkBluetoothAvailability()
        }
    }
}