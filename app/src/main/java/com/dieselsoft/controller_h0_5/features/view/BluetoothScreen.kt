package com.dieselsoft.controller_h0_5.features.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dieselsoft.controller_h0_5.R
import com.dieselsoft.controller_h0_5.features.viewmodel.BluetoothViewModel

@Composable
fun BluetoothScreen(viewModel: BluetoothViewModel) {
    val isConnected by viewModel.isConnected.collectAsState()
    val currentValue by viewModel.currentValue.collectAsState()
    val connectedDeviceName by viewModel.connectedDeviceName.collectAsState()
    var knobValue by remember { mutableFloatStateOf(currentValue.toFloat()) }

    LaunchedEffect(currentValue) {
        knobValue = currentValue.toFloat()
    }

    var previousKnobValue by remember { mutableFloatStateOf(knobValue) }
    LaunchedEffect(knobValue) {
        val intValue = knobValue.toInt()
        if (intValue != previousKnobValue.toInt() && isConnected) {
            viewModel.updateAndSendValue(intValue)
        }
        previousKnobValue = knobValue
    }

    // Fondo de imagen
    Image(
        painter = painterResource(id = R.drawable.wallpaper_camion),
        contentDescription = "Fondo",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )

    // Fondo oscuro semi-transparente para mejorar contraste
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Logo de la empresa en la parte superior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_empresa),
                contentDescription = "Logo de la empresa",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card de estado de conexión (compacto)
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isConnected)
                    Color(0xFF4CAF50).copy(alpha = 0.8f)
                else
                    Color(0xFFF44336).copy(alpha = 0.8f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isConnected)
                            R.drawable.outline_bluetooth_connected_24
                        else
                            R.drawable.outline_bluetooth_24
                    ),
                    contentDescription = if (isConnected) "Conectado" else "Desconectado",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (isConnected) "Conectado" else "Desconectado",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    if (isConnected && connectedDeviceName != null) {
                        Text(
                            text = connectedDeviceName!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card para el knob
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Control de Velocidad",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Valor: ${knobValue.toInt()}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                SimpleArcKnob(
                    value = knobValue,
                    onValueChange = { knobValue = it },
                    minValue = 0f,
                    maxValue = 255f
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Indicador visual si no está conectado
                if (!isConnected) {
                    Text(
                        text = "⚠️ Ve a 'Devices' para conectar un dispositivo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card para los botones de acción
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        if (isConnected) {
                            viewModel.sendValue(knobValue.toInt())
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.9f),
                    enabled = isConnected,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Enviar Manualmente",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                if (isConnected) {
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { viewModel.disconnect() },
                        modifier = Modifier.fillMaxWidth(0.9f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.outline_bluetooth_disabled_24),
                            contentDescription = "Desconectar",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Desconectar")
                    }
                }
            }
        }
    }
}