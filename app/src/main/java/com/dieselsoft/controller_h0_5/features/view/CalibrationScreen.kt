package com.dieselsoft.controller_h0_5.features.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dieselsoft.controller_h0_5.R
import com.dieselsoft.controller_h0_5.features.viewmodel.BluetoothViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrationScreen(viewModel: BluetoothViewModel) {
    val calibrationMap by viewModel.calibrationMap.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    
    // Estado para el diálogo de edición
    var editingPoint by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Ajusta los valores del modulo para cada velocidad. La app enviará el valor al hardware mientras editas para que puedas verificarlo.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tabla de Calibración",
                style = MaterialTheme.typography.titleLarge
            )
            
            TextButton(onClick = { viewModel.resetCalibration() }) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(calibrationMap.toList().sortedBy { it.first }) { (speed, value) ->
                CalibrationRow(
                    speed = speed,
                    value = value,
                    onClick = { editingPoint = speed to value }
                )
            }
        }
        
        if (!isConnected) {
            Text(
                text = "⚠️ Conéctate a un dispositivo para calibrar en tiempo real",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }

    // Diálogo de Edición mejorado con entrada numérica y botones +/-
    editingPoint?.let { (speed, value) ->
        var currentValue by remember { mutableFloatStateOf(value.toFloat()) }
        
        AlertDialog(
            onDismissRequest = { editingPoint = null },
            title = { Text("Calibrar: $speed km/h") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Control de valor con botones y entrada directa
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalIconButton(
                            onClick = {
                                currentValue = (currentValue - 1f).coerceIn(0f, 255f)
                                viewModel.sendValue(currentValue.toInt())
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.outline_remove_24),
                                contentDescription = "Restar 1"
                            )
                        }

                        // Input numérico directo
                        var textValue by remember { mutableStateOf(currentValue.toInt().toString()) }
                        
                        // Sincronizar el texto si el slider mueve el valor
                        LaunchedEffect(currentValue) {
                            val currentIntStr = currentValue.toInt().toString()
                            if (textValue != currentIntStr) {
                                textValue = currentIntStr
                            }
                        }

                        OutlinedTextField(
                            value = textValue,
                            onValueChange = { newValue ->
                                val filtered = newValue.filter { it.isDigit() }
                                textValue = filtered
                                filtered.toIntOrNull()?.let {
                                    val clamped = it.coerceIn(0, 255)
                                    currentValue = clamped.toFloat()
                                    viewModel.sendValue(clamped)
                                }
                            },
                            modifier = Modifier
                                .width(100.dp)
                                .padding(horizontal = 8.dp),
                            textStyle = MaterialTheme.typography.headlineMedium.copy(
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        FilledTonalIconButton(
                            onClick = {
                                currentValue = (currentValue + 1f).coerceIn(0f, 255f)
                                viewModel.sendValue(currentValue.toInt())
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Sumar 1"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Slider(
                        value = currentValue,
                        onValueChange = { 
                            currentValue = it
                            viewModel.sendValue(it.toInt())
                        },
                        valueRange = 0f..255f
                    )
                    
                    Text(
                        text = "Ajusta hasta que la máquina marque $speed km/h",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateCalibration(speed, currentValue.toInt())
                    editingPoint = null
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPoint = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CalibrationRow(
    speed: Int,
    value: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "$speed km/h",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Valor enviado: $value",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Button(onClick = onClick) {
                Text("Ajustar")
            }
        }
    }
}