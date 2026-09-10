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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dieselsoft.controller_h0_5.R
import com.dieselsoft.controller_h0_5.features.viewmodel.BluetoothViewModel
import kotlin.math.max
import kotlin.math.round

/**
 * Convierte km/h al valor del potenciómetro usando la tabla dinámica
 * Hace interpolación lineal entre los puntos conocidos
 */
private fun speedToPotenValue(speedKmh: Float, calibrationMap: Map<Int, Int>): Int {
    val sortedSpeeds = calibrationMap.keys.sorted()
    if (sortedSpeeds.isEmpty()) return 0
    
    val clamped = speedKmh.coerceIn(sortedSpeeds.first().toFloat(), sortedSpeeds.last().toFloat())

    for (i in 0 until sortedSpeeds.size - 1) {
        val lowSpeed = sortedSpeeds[i]
        val highSpeed = sortedSpeeds[i + 1]
        
        val lowPoten = calibrationMap[lowSpeed] ?: 0
        val highPoten = calibrationMap[highSpeed] ?: 0

        if (clamped <= highSpeed) {
            val ratio = (clamped - lowSpeed) / (highSpeed - lowSpeed).toFloat()
            return round(lowPoten + ratio * (highPoten - lowPoten)).toInt()
        }
    }
    return calibrationMap[sortedSpeeds.last()] ?: 0
}

/**
 * Calcula el ángulo de la aguja adaptado a la nueva imagen.
 */
private fun calculateNeedleAngle(speed: Float): Float {
    val startAngle = 250f   // Posición física del inicio en la imagen
    val endAngle   = 106f   // Posición física del final en la imagen (140 km/h)

    val sweepAngle = (360f - startAngle) + endAngle

    // De 0 a 18: la aguja se queda fija en el inicio
    if (speed <= 18f) {
        val microRatio = speed / 18f * 0.03f
        val angle = startAngle + (microRatio * sweepAngle)
        return if (angle >= 360f) angle - 360f else angle
    }

    // De 18 a 140: distribuir en todo el arco
    val ratio = ((speed - 18f) / (120f - 18f)).coerceIn(0f, 1f)
    val angle = startAngle + (ratio * sweepAngle)
    return if (angle >= 360f) angle - 360f else angle
}

/**
 * Pantalla de velocímetro
 */
@Composable
fun SpeedometerScreen(viewModel: BluetoothViewModel) {
    val isConnected by viewModel.isConnected.collectAsState()
    val calibrationMap by viewModel.calibrationMap.collectAsState()

    var targetSpeed by remember { mutableFloatStateOf(0f) }

    val animatedSpeed by animateFloatAsState(
        targetValue = targetSpeed,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "speed_animation"
    )

    val displaySpeed = max(0f, animatedSpeed)

    LaunchedEffect(targetSpeed, calibrationMap) {
        if (isConnected) {
            val potenValue = speedToPotenValue(targetSpeed, calibrationMap)
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

        // Botones de velocidades fijas dinámicos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Mostrar solo los puntos principales para no saturar
            val mainPoints = listOf(0, 30, 60, 90, 120, 140)
            mainPoints.forEach { speed ->
                FilledTonalButton(
                    onClick = { targetSpeed = speed.toFloat() },
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("$speed")
                }
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
                            text = "[${speedToPotenValue(targetSpeed, calibrationMap)}]",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = targetSpeed,
                    onValueChange = { targetSpeed = it },
                    valueRange = 0f..140f,
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
                            targetSpeed = (targetSpeed - 10f).coerceIn(0f, 140f)
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
                            targetSpeed = (targetSpeed - 1f).coerceIn(0f, 140f)
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
                            targetSpeed = (targetSpeed + 1f).coerceIn(0f, 140f)
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
                            targetSpeed = (targetSpeed + 10f).coerceIn(0f, 140f)
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

                // Indicador de estado
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
            painter = painterResource(id = R.drawable.velocimetro),
            contentDescription = "Velocímetro",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        SpeedDisplay(speed = speed)

        SpeedNeedle(
            speed = speed,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun SpeedDisplay(speed: Float) {
    val dotMatrixFontFamily = FontFamily(
        Font(R.font.doto_extra_bold, FontWeight.Normal)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset(y = (-30).dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "DieselSoft",
                style = LocalTextStyle.current.copy(
                    fontFamily = dotMatrixFontFamily,
                    fontSize =  18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFFCECDCD)
                )
            )
        }
    }
}

@Composable
private fun SpeedNeedle(
    speed: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val needleLength = (minOf(size.width, size.height) / 2f) * 0.65f

        val needleAngle = calculateNeedleAngle(speed)

        rotate(degrees = needleAngle, pivot = Offset(centerX, centerY)) {
            drawNeedle(centerX, centerY, needleLength)
        }

        drawCenterCircles(centerX, centerY)
    }
}

private fun DrawScope.drawNeedle(
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

private fun DrawScope.drawCenterCircles(
    centerX: Float,
    centerY: Float
) {
    drawCircle(color = Color(0xFF3a3a3a), radius = 25f, center = Offset(centerX, centerY))
    drawCircle(color = Color(0xFFff3838), radius = 15f, center = Offset(centerX, centerY))
    drawCircle(color = Color(0xFF1a1a1a), radius = 5f, center = Offset(centerX, centerY))
}