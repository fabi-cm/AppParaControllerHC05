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
//    var value by remember { mutableFloatStateOf(0f) }
    val currentValue by viewModel.currentValue.collectAsState()
    var knobValue by remember { mutableFloatStateOf(currentValue.toFloat()) }

    LaunchedEffect(currentValue) {
        knobValue = currentValue.toFloat()
    }

    var previousKnobValue by remember { mutableStateOf(knobValue) }
    LaunchedEffect(knobValue) {
        val intValue = knobValue.toInt()
        if (intValue != previousKnobValue.toInt() && isConnected) {
            viewModel.updateAndSendValue(intValue)
        }
        previousKnobValue = knobValue
    }

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

        Text("Valor: ${knobValue.toInt()}")
        SimpleArcKnob(
            value = knobValue,
            onValueChange = { knobValue = it },
            minValue = 0f,
            maxValue = 255f
        )

        Button(
            onClick = {
                if (isConnected) {
                    viewModel.sendValue(knobValue.toInt())
                }
            },
            modifier = Modifier.padding(top = 16.dp),
            enabled = isConnected
        ) {
            Text("Enviar Manualmente")
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

