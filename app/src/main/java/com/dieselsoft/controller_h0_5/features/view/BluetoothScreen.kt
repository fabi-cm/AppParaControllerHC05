package com.dieselsoft.controller_h0_5.features.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    val errorMessage by viewModel.errorMessage.collectAsState()
    val bluetoothAvailable by viewModel.bluetoothAvailable.collectAsState()
    val pairedDevices by viewModel.pairedDevices.collectAsState()
    var value by remember { mutableFloatStateOf(0f) }

    // Estado efectivo de conexión (real o simulación)
    val effectiveConnected = isConnected || simulationMode

    // Mostrar diálogo de error
    if (!errorMessage.isNullOrEmpty()) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = {
                Column {
                    Text(errorMessage ?: "")
                    if (pairedDevices.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Dispositivos emparejados:", style = MaterialTheme.typography.labelMedium)
                        pairedDevices.forEach { device ->
                            Text("• $device")
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Control HC-05")
                        if (simulationMode) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "🔧 MODO DISEÑO",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                },
                actions = {
                    // Botón para modo simulación/diseño
                    IconButton(
                        onClick = {
                            if (simulationMode) {
                                viewModel.disableSimulationMode()
                            } else {
                                viewModel.enableSimulationMode()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (simulationMode) Icons.Filled.Settings
                            else Icons.Filled.Edit,
                            contentDescription = if (simulationMode) "Salir del modo diseño"
                            else "Modo diseño",
                            tint = if (simulationMode) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Estado de Bluetooth
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        simulationMode -> MaterialTheme.colorScheme.tertiaryContainer
                        effectiveConnected -> MaterialTheme.colorScheme.primaryContainer
                        !bluetoothAvailable -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = when {
                            simulationMode -> Icons.Filled.Settings
                            effectiveConnected -> Icons.Filled.Done
                            !bluetoothAvailable -> Icons.Default.Warning
                            else -> Icons.Filled.Warning
                        },
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = when {
                            simulationMode -> "🔧 MODO DISEÑO ACTIVADO"
                            effectiveConnected -> "Conectado al HC-05 ✅"
                            !bluetoothAvailable -> "Bluetooth No Disponible"
                            else -> "Desconectado"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )

                    if (simulationMode) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Puedes probar la interfaz sin Bluetooth",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Botones de control
            if (!bluetoothAvailable && !simulationMode) {
                Button(
                    onClick = {
                        onRequestPermissions()
                        onEnableBluetooth()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Activar Bluetooth y Permisos")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón Conectar/Simular
                    Button(
                        onClick = {
                            if (simulationMode) {
                                // En modo simulación, mostrar mensaje
                                viewModel.setError("Ya estás en modo diseño. Puedes probar todos los controles.")
                            } else {
                                viewModel.connect()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !effectiveConnected || simulationMode
                    ) {
                        Text(if (simulationMode) "Probar Conexión" else "Conectar")
                    }

                    if (effectiveConnected) {
                        Button(
                            onClick = {
                                if (simulationMode) {
                                    viewModel.disableSimulationMode()
                                } else {
                                    viewModel.disconnect()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (simulationMode) MaterialTheme.colorScheme.tertiaryContainer
                                else MaterialTheme.colorScheme.errorContainer,
                                contentColor = if (simulationMode) MaterialTheme.colorScheme.onTertiaryContainer
                                else MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Text(if (simulationMode) "Salir Diseño" else "Desconectar")
                        }
                    }
                }
            }

            // Botón rápido para modo diseño
            if (!simulationMode && !effectiveConnected) {
                OutlinedButton(
                    onClick = { viewModel.enableSimulationMode() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Modo Diseño (Probar sin Bluetooth)")
                }
            }

            // Control Rotary Knob - Ahora visible en modo simulación también
            if (effectiveConnected) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (simulationMode) "🎨 Diseño - Control de Valor"
                            else "Control de Valor",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (simulationMode) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Simulando envío de valores: ${value.toInt()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Rotary Knob
//                        RotaryKnob(
//                            value = value,
//                            onValueChange = { newValue ->
//                                value = newValue
//                                if (!simulationMode) {
//                                    // Solo enviar por Bluetooth si no estamos en simulación
//                                    viewModel.sendValue(newValue.toInt())
//                                }
//                                // En modo simulación, solo actualizamos la UI
//                            },
//                            modifier = Modifier,
//                            minValue = 0f,
//                            maxValue = 255f,
//                            enabled = true
//                        )

                        ArcSliderKnob(
                            value = value,
                            onValueChange = { newValue ->
                                value = newValue
                                if (!simulationMode) {
                                    viewModel.sendValue(newValue.toInt())
                                }
                            },
                            minValue = 0f,
                            maxValue = 255f,
                            enabled = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Indicador de valor
                        Text(
                            text = "Valor actual: ${value.toInt()}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Barra de progreso
                        LinearProgressIndicator(
                        progress = { value / 255f },
                        modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(8.dp),
                        color = if (simulationMode) MaterialTheme.colorScheme.tertiary
                                                    else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botones de preset rápidos
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(0, 64, 128, 192, 255).forEach { presetValue ->
                                Button(
                                    onClick = {
                                        value = presetValue.toFloat()
                                        if (!simulationMode) {
                                            viewModel.sendValue(presetValue)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (simulationMode) MaterialTheme.colorScheme.tertiaryContainer
                                        else MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = if (simulationMode) MaterialTheme.colorScheme.onTertiaryContainer
                                        else MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                ) {
                                    Text("$presetValue")
                                }
                            }
                        }

                        // Botón de envío manual (útil para pruebas)
                        if (simulationMode) {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = {
                                    // Simular envío
                                    viewModel.setError("🔧 Modo Diseño: Simulando envío de valor ${value.toInt()}")
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Simular Envío (Solo prueba)")
                            }
                        }
                    }
                }
            }

            // Información de dispositivos emparejados
            if (pairedDevices.isNotEmpty() && !simulationMode) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Dispositivos Emparejados",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        pairedDevices.forEach { device ->
                            Text("• $device")
                        }
                    }
                }
            }
        }
    }
}