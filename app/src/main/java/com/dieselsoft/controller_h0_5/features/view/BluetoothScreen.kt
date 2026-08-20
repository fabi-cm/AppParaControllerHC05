package com.dieselsoft.controller_h0_5.features.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dieselsoft.controller_h0_5.R
import com.dieselsoft.controller_h0_5.features.viewmodel.BluetoothViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothScreen(
    viewModel: BluetoothViewModel,
    onRequestPermissions: () -> Unit = {},
    onEnableBluetooth: () -> Unit = {}
) {
    val isConnected by viewModel.isConnected.collectAsState()
    val simulationMode by viewModel.simulationMode.collectAsState()
    val connectionError by viewModel.connectionError.collectAsState()
    val currentValue by viewModel.currentValue.collectAsState()
    val connectedDeviceName by viewModel.connectedDeviceName.collectAsState()
    
    val effectiveConnected = isConnected || simulationMode
    var knobValue by remember { mutableFloatStateOf(currentValue.toFloat()) }

    LaunchedEffect(currentValue) {
        knobValue = currentValue.toFloat()
    }

    LaunchedEffect(knobValue) {
        val intValue = knobValue.toInt()
        if (intValue != currentValue && (isConnected || simulationMode)) {
            viewModel.updateAndSendValue(intValue)
        }
    }

    // Fondo y UI "bonita"
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.wallpaper_camion),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))

        if (!connectionError.isNullOrEmpty()) {
            AlertDialog(
                onDismissRequest = { viewModel.clearMessages() },
                title = { Text("Aviso") },
                text = { Text(connectionError ?: "") },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearMessages() }) {
                        Text("OK")
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón rápido para modo simulación
            IconButton(
                onClick = { 
                    if (simulationMode) viewModel.disableSimulationMode() 
                    else viewModel.enableSimulationMode() 
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Modo Diseño",
                    tint = if (simulationMode) Color.Yellow else Color.White
                )
            }

            // Card de Estado
            Card(
                modifier = Modifier.fillMaxWidth(0.9f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (effectiveConnected) 
                        Color(0xFF4CAF50).copy(alpha = 0.8f) 
                    else 
                        Color(0xFFF44336).copy(alpha = 0.8f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isConnected) R.drawable.outline_bluetooth_connected_24 
                                 else R.drawable.outline_bluetooth_24
                        ),
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (simulationMode) "Modo Diseño" 
                               else if (isConnected) "Conectado: $connectedDeviceName" 
                               else "Desconectado",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Knob de Control
            Card(
                modifier = Modifier.fillMaxWidth(0.9f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Valor: ${knobValue.toInt()}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.Blue
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SimpleArcKnob(
                        value = knobValue,
                        onValueChange = { knobValue = it },
                        minValue = 0f,
                        maxValue = 255f
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botones Preset
            Card(
                modifier = Modifier.fillMaxWidth(0.9f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        PresetButton(20, effectiveConnected) { knobValue = it.toFloat() }
                        PresetButton(40, effectiveConnected) { knobValue = it.toFloat() }
                        PresetButton(60, effectiveConnected) { knobValue = it.toFloat() }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        PresetButton(80, effectiveConnected) { knobValue = it.toFloat() }
                        PresetButton(100, effectiveConnected) { knobValue = it.toFloat() }
                        PresetButton(120, effectiveConnected) { knobValue = it.toFloat() }
                    }
                }
            }

            if (isConnected) {
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = { viewModel.disconnect() },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Text("Desconectar")
                }
            }
        }
    }
}

@Composable
fun PresetButton(value: Int, isEnabled: Boolean, onClick: (Int) -> Unit) {
    Button(
        onClick = { onClick(value) },
        enabled = isEnabled,
        modifier = Modifier.width(80.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text = value.toString(), fontWeight = FontWeight.Bold)
    }
}