package com.dieselsoft.controller_h0_5.features.view

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dieselsoft.controller_h0_5.R
import com.dieselsoft.controller_h0_5.features.viewmodel.BluetoothViewModel
import kotlin.math.max
import kotlin.math.round

// ─────────────────────────────────────────────
// Tabla de mapeo: km/h → valor del potenciómetro
// ─────────────────────────────────────────────
private val SPEED_MAP = listOf(
    0   to 0,
    20  to 29,
    40  to 57,
    60  to 87,
    80  to 117,
    100 to 146,
    120 to 175
)

/**
 * Convierte km/h (0-120) al valor del potenciómetro (0-175)
 * Hace interpolación lineal entre los puntos conocidos
 */
private fun speedToPotenValue(speedKmh: Float): Int {
    val clamped = speedKmh.coerceIn(0f, 120f)

    // Encontrar los dos puntos entre los que cae la velocidad
    for (i in 0 until SPEED_MAP.size - 1) {
        val (lowSpeed, lowPoten) = SPEED_MAP[i]
        val (highSpeed, highPoten) = SPEED_MAP[i + 1]

        if (clamped <= highSpeed) {
            // Interpolación lineal entre los dos puntos
            val ratio = (clamped - lowSpeed) / (highSpeed - lowSpeed).toFloat()
            return round(lowPoten + ratio * (highPoten - lowPoten)).toInt()
        }
    }
    return 175 // Valor máximo si por alguna razón supera 120
}

/**
 * Pantalla de velocímetro
 *
 * - Muestra velocidad en km/h (0-120)
 * - Envía valor del potenciómetro (0-175) al módulo Bluetooth
 * - Funciona en modo local sin necesidad de conexión
 *
 * Calibración de la aguja:
 * - 0 km/h en 245°
 * - 120 km/h en 115°
 */
@Composable
fun SpeedometerScreen(viewModel: BluetoothViewModel) {
    val isConnected by viewModel.isConnected.collectAsState()

    // Estado local de velocidad en km/h (siempre funciona)
    var targetSpeed by remember { mutableFloatStateOf(0f) }

    // Animación suave
    val animatedSpeed by animateFloatAsState(
        targetValue = targetSpeed,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "speed_animation"
    )

    // Nunca negativo en el display
    val displaySpeed = max(0f, animatedSpeed)

    // Enviar al módulo solo cuando cambie y esté conectado
    LaunchedEffect(targetSpeed) {
        if (isConnected) {
            val potenValue = speedToPotenValue(targetSpeed)
            viewModel.sendValue(potenValue)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Velocímetro
        SpeedometerGauge(
            speed = displaySpeed,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Botones de velocidades fijas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilledTonalButton(onClick = { targetSpeed = 0f }) {
                Text("0")
            }
            FilledTonalButton(onClick = { targetSpeed = 30f }) {
                Text("30")
            }
            FilledTonalButton(onClick = { targetSpeed = 60f }) {
                Text("60")
            }
            FilledTonalButton(onClick = { targetSpeed = 90f }) {
                Text("90")
            }
            FilledTonalButton(onClick = { targetSpeed = 120f }) {
                Text("120")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Control manual
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Control Manual",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // Muestra km/h y entre paréntesis el valor real que se envía
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${targetSpeed.toInt()} km/h",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "[${speedToPotenValue(targetSpeed)}]",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = targetSpeed,
                    onValueChange = { targetSpeed = it },
                    valueRange = 0f..120f,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Botones +/-
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = {
                            targetSpeed = (targetSpeed - 10f).coerceIn(0f, 120f)
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(R.drawable.outline_remove_24),
                                contentDescription = "Restar 10",
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "10",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp
                            )
                        }
                    }

                    FilledTonalIconButton(
                        onClick = {
                            targetSpeed = (targetSpeed - 1f).coerceIn(0f, 120f)
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_remove_24),
                            contentDescription = "Restar 1",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    FilledTonalIconButton(
                        onClick = {
                            targetSpeed = (targetSpeed + 1f).coerceIn(0f, 120f)
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Sumar 1",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    FilledIconButton(
                        onClick = {
                            targetSpeed = (targetSpeed + 10f).coerceIn(0f, 120f)
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Sumar 10",
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "10",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Indicador de estado de conexión (pequeño, no invasivo)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dotColor = if (isConnected) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
                    Canvas(modifier = Modifier.size(8.dp)) {
                        drawCircle(color = dotColor, radius = size.width / 2f)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isConnected) "Conectado — enviando al módulo"
                        else "Sin conexión — modo local",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeedometerGauge(
    speed: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Image(
//            painter = painterResource(id = R.drawable.dashboar),
            painter = painterResource(id = R.drawable.velocimetro),
            contentDescription = "Velocímetro",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        SpeedNeedle(
            speed = speed,
            modifier = Modifier.fillMaxSize()
        )

        SpeedDisplay(speed = speed)
    }
}

@Composable
private fun SpeedDisplay(speed: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset(y = (-35).dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${speed.toInt()}",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 60.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-2).sp
                ),
                color = Color(0xFFc8f0d4)
            )
//            Text(
//                text = "km/h",
//                style = MaterialTheme.typography.titleMedium.copy(
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Medium,
//                    letterSpacing = 1.sp
//                ),
//                color = Color(0xFFc8f0d4).copy(alpha = 0.8f)
//            )
        }
    }
}

@Composable
private fun SpeedNeedle(
    speed: Float,
    modifier: Modifier = Modifier
) {
    val startAngle = 245f
    val endAngle = 115f
    val maxSpeed = 120f
    val needleLengthScale = 0.65f

    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val needleLength = (minOf(size.width, size.height) / 2f) * needleLengthScale

        val needleAngle = calculateNeedleAngle(speed, maxSpeed, startAngle, endAngle)

        rotate(degrees = needleAngle, pivot = Offset(centerX, centerY)) {
            drawNeedle(centerX, centerY, needleLength)
        }

        drawCenterCircles(centerX, centerY)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawNeedle(
    centerX: Float,
    centerY: Float,
    length: Float
) {
    val needleWidth = 8f
    val needleBaseWidth = 12f

    val needlePath = Path().apply {
        moveTo(centerX - needleBaseWidth, centerY)
        lineTo(centerX - needleWidth / 2, centerY - length * 0.15f)
        lineTo(centerX, centerY - length)
        lineTo(centerX + needleWidth / 2, centerY - length * 0.15f)
        lineTo(centerX + needleBaseWidth, centerY)
        lineTo(centerX + needleBaseWidth / 2, centerY)
        lineTo(centerX + needleBaseWidth / 2, centerY + 15f)
        lineTo(centerX - needleBaseWidth / 2, centerY + 15f)
        lineTo(centerX - needleBaseWidth / 2, centerY)
        close()
    }

    drawPath(path = needlePath, color = Color.Black.copy(alpha = 0.3f))
    drawPath(path = needlePath, color = Color(0xFFff3838))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCenterCircles(
    centerX: Float,
    centerY: Float
) {
    drawCircle(color = Color(0xFF3a3a3a), radius = 25f, center = Offset(centerX, centerY))
    drawCircle(color = Color(0xFFff3838), radius = 15f, center = Offset(centerX, centerY))
    drawCircle(color = Color(0xFF1a1a1a), radius = 5f, center = Offset(centerX, centerY))
}

private fun calculateNeedleAngle(
    speed: Float,
    maxSpeed: Float,
    startAngle: Float,
    endAngle: Float
): Float {
    val speedPercentage = (speed / maxSpeed).coerceIn(0f, 1f)
    val sweepAngle = (360f - startAngle) + endAngle
    val finalAngle = startAngle + (speedPercentage * sweepAngle)
    return if (finalAngle >= 360f) finalAngle - 360f else finalAngle
}