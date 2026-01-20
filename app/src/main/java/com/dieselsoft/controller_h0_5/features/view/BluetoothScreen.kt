package com.dieselsoft.controller_h0_5.features.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import com.dieselsoft.controller_h0_5.features.viewmodel.BluetoothViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dieselsoft.controller_h0_5.R

@Composable
fun BluetoothScreen(viewModel: BluetoothViewModel) {
    val isConnected by viewModel.isConnected.collectAsState()
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
        verticalArrangement = Arrangement.Center
    ) {
        // Card con mejor visibilidad
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.5f)
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
                    text = if (isConnected) "Conectado al HC-05 ✅" else "Desconectado ❌",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.connect() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Conectar")
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
                containerColor = Color.White.copy(alpha = 0.5f)
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
                    text = "Valor: ${knobValue.toInt()}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                SimpleArcKnob(
                    value = knobValue,
                    onValueChange = { knobValue = it },
                    minValue = 0f,
                    maxValue = 255f
                )
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
                containerColor = Color.White.copy(alpha = 0.5f)
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
                    modifier = Modifier.fillMaxWidth(0.8f),
                    enabled = isConnected,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text ="Enviar Manualmente",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White
                        )
                    )
                }

                if (isConnected) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.disconnect() },
                        modifier = Modifier.fillMaxWidth(0.8f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Desconectar")
                    }
                }
            }
        }
    }
}
