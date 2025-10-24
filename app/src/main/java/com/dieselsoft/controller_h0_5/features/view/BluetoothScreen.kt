package com.dieselsoft.controller_h0_5.features.view

import com.dieselsoft.controller_h0_5.features.viewmodel.BluetoothViewModel

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BluetoothScreen(viewModel: BluetoothViewModel) {
    val isConnected by viewModel.isConnected.collectAsState()
    var value by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isConnected) "Conectado al HC-05 ✅" else "Desconectado ❌",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.connect() }) {
            Text("Conectar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Valor: ${value.toInt()}")
        SimpleArcKnob(
            value = value,
            onValueChange = { value = it },
            minValue = 0f,
            maxValue = 255f
        )

        Button(
            onClick = { viewModel.sendValue(value.toInt()) },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Enviar")
        }

        if (isConnected) {
            Button(
                onClick = { viewModel.disconnect() },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Desconectar")
            }
        }
    }
}

