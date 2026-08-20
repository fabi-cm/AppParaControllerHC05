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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
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
    20  to 39,
    30  to 58,
    40  to 78,
    60  to 117,
    90  to 174,
    120 to 232
)

/**
 * Convierte km/h (0-120) al valor del potenciómetro (0-232)
 * Hace interpolación lineal entre los puntos conocidos
 */
private fun speedToPotenValue(speedKmh: Float): Int {
    val clamped = speedKmh.coerceIn(0f, 120f)

    for (i in 0 until SPEED_MAP.size - 1) {
        val (lowSpeed, lowPoten) = SPEED_MAP[i]
        val (highSpeed, highPoten) = SPEED_MAP[i + 1]

        if (clamped <= highSpeed) {
            val ratio = (clamped - lowSpeed) / (highSpeed - lowSpeed).toFloat()
            return round(lowPoten + ratio * (highPoten - lowPoten)).toInt()
        }
    }
    return 232
}

/**
 * Calcula el ángulo de la aguja adaptado a la nueva imagen.
 *
 * Nueva imagen:
 * - El arco físico va de ~225° (donde está el 20) hasta ~135° (donde está el 120)
 * - El centro del arco es el 70 km/h (no 60)
 * - De 0 a 18 km/h la aguja se mantiene en la posición del 20 (inicio del arco)
 * - De 18 a 120 la aguja se distribuye en todo el recorrido del arco
 */
private fun calculateNeedleAngle(speed: Float): Float {
    val startAngle = 250f   // Posición física del 20 en la imagen (inicio del arco)
    val endAngle   = 106f   // Posición física del 120 en la imagen (final del arco)
    // Recorrido total cruza el 0°/360°: de 228° → 360° + 0° → 132° = 264°
    val sweepAngle = (360f - startAngle) + endAngle

    // De 0 a 18: la aguja se queda fija en el inicio (posición del 20)
    if (speed <= 18f) {
        // Pequeño movimiento proporcional de 0 a 18 para que no esté 100% fija
        // Solo recorre el 3% del arco total (se mueve apenas)
        val microRatio = speed / 18f * 0.03f
        val angle = startAngle + (microRatio * sweepAngle)
        return if (angle >= 360f) angle - 360f else angle
    }

    // De 18 a 120: distribuir en todo el arco
    // El 18 mapea al inicio (0%) y el 120 mapea al final (100%)
    val ratio = ((speed - 18f) / (120f - 18f)).coerceIn(0f, 1f)
    val angle = startAngle + (ratio * sweepAngle)
    return if (angle >= 360f) angle - 360f else angle
}

/**
 * Pantalla de velocímetro
 *
 * - Muestra velocidad en km/h (0-120)
 * - Envía valor del potenciómetro (0-232) al módulo Bluetooth
 * - Funciona en modo local sin necesidad de conexión
 */
@Composable
fun SpeedometerScreen(viewModel: BluetoothViewModel) {
    val isConnected by viewModel.isConnected.collectAsState()

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

//@Composable
//private fun SpeedDisplay(speed: Float) {
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .offset(y = (-35).dp),
//        contentAlignment = Alignment.Center
//    ) {
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            Text(
//                text = "${speed.toInt()}",
//                style = MaterialTheme.typography.displayLarge.copy(
//                    fontSize = 60.sp,
//                    fontWeight = FontWeight.Bold,
//                    letterSpacing = (-2).sp
//                ),
//                color = Color(0xFFc8f0d4)
//            )
//        }
//    }
//}

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