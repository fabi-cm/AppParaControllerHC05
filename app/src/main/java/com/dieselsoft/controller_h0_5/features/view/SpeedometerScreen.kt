package com.dieselsoft.controller_h0_5.features.view

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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

/**
 * Pantalla de velocímetro simplificada
 *
 * Calibración actual:
 * - 0 km/h en 245° (abajo-izquierda)
 * - 120 km/h en 115° (normalizado de 475°, arriba-derecha)
 */
@Composable
fun SpeedometerScreen() {
    var targetSpeed by remember { mutableFloatStateOf(0f) }
    val animatedSpeed by animateFloatAsState(
        targetValue = targetSpeed,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "speed_animation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Velocímetro
        SpeedometerGauge(
            speed = animatedSpeed,
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

        // Control manual con slider
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
                    Text(
                        text = "${targetSpeed.toInt()} km/h",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = targetSpeed,
                    onValueChange = { targetSpeed = it },
                    valueRange = 0f..120f,
                    modifier = Modifier.fillMaxWidth()
                )
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
        // Imagen de fondo del velocímetro
        Image(
            painter = painterResource(id = R.drawable.dashboar),
            contentDescription = "Velocímetro",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // Aguja
        SpeedNeedle(
            speed = speed,
            modifier = Modifier.fillMaxSize()
        )

        // Valor numérico de velocidad
        SpeedDisplay(speed = speed)
    }
}

@Composable
private fun SpeedDisplay(speed: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset(y = (-20).dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${speed.toInt()}",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-2).sp
                ),
                color = Color(0xFFc8f0d4)
            )
            Text(
                text = "km/h",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                ),
                color = Color(0xFFc8f0d4).copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun SpeedNeedle(
    speed: Float,
    modifier: Modifier = Modifier
) {
    // Constantes de calibración
    val START_ANGLE = 245f  // Posición del 0 km/h
    val END_ANGLE = 115f    // Posición del 120 km/h (equivalente a 475° normalizado)
    val MAX_SPEED = 120f
    val NEEDLE_LENGTH_SCALE = 0.65f

    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val needleLength = (minOf(size.width, size.height) / 2f) * NEEDLE_LENGTH_SCALE

        // Calcular ángulo de la aguja
        val needleAngle = calculateNeedleAngle(speed, MAX_SPEED, START_ANGLE, END_ANGLE)

        // Dibujar la aguja
        rotate(degrees = needleAngle, pivot = Offset(centerX, centerY)) {
            drawNeedle(centerX, centerY, needleLength)
        }

        // Círculo central
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
        // Forma de flecha de la aguja
        moveTo(centerX - needleBaseWidth, centerY)
        lineTo(centerX - needleWidth / 2, centerY - length * 0.15f)
        lineTo(centerX, centerY - length)
        lineTo(centerX + needleWidth / 2, centerY - length * 0.15f)
        lineTo(centerX + needleBaseWidth, centerY)

        // Cola de la aguja
        lineTo(centerX + needleBaseWidth / 2, centerY)
        lineTo(centerX + needleBaseWidth / 2, centerY + 15f)
        lineTo(centerX - needleBaseWidth / 2, centerY + 15f)
        lineTo(centerX - needleBaseWidth / 2, centerY)

        close()
    }

    // Sombra
    drawPath(
        path = needlePath,
        color = Color.Black.copy(alpha = 0.3f)
    )

    // Aguja roja
    drawPath(
        path = needlePath,
        color = Color(0xFFff3838)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCenterCircles(
    centerX: Float,
    centerY: Float
) {
    // Base gris oscura
    drawCircle(
        color = Color(0xFF3a3a3a),
        radius = 25f,
        center = Offset(centerX, centerY)
    )

    // Círculo rojo
    drawCircle(
        color = Color(0xFFff3838),
        radius = 15f,
        center = Offset(centerX, centerY)
    )

    // Punto central
    drawCircle(
        color = Color(0xFF1a1a1a),
        radius = 5f,
        center = Offset(centerX, centerY)
    )
}

/**
 * Calcula el ángulo de la aguja basándose en la velocidad
 *
 * @param speed Velocidad actual (0-120 km/h)
 * @param maxSpeed Velocidad máxima (120 km/h)
 * @param startAngle Ángulo donde está el 0 (245°)
 * @param endAngle Ángulo donde está el 120 (115°, que es 475° normalizado)
 * @return Ángulo en grados para la aguja
 */
private fun calculateNeedleAngle(
    speed: Float,
    maxSpeed: Float,
    startAngle: Float,
    endAngle: Float
): Float {
    val speedPercentage = (speed / maxSpeed).coerceIn(0f, 1f)

    // Como endAngle (115°) es menor que startAngle (245°),
    // el arco cruza el punto 0°/360°
    // Recorrido total: desde 245° hasta 360° + desde 0° hasta 115° = 230°
    val sweepAngle = (360f - startAngle) + endAngle

    // Calcular ángulo final
    val finalAngle = startAngle + (speedPercentage * sweepAngle)

    // Normalizar a rango 0-360°
    return if (finalAngle >= 360f) finalAngle - 360f else finalAngle
}