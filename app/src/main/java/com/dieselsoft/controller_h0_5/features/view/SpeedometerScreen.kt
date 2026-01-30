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

@Composable
fun SpeedometerScreen(
    startAngleDegrees: Float = 245f,  // Ángulo donde debe estar el 0 km/h
    endAngleDegrees: Float = 475f,    // Ángulo donde debe estar el 120 km/h
    needleLengthScale: Float = 0.65f, // Longitud de la aguja (0.5 a 0.8)
    centerOffsetX: Float = 0f,        // Desplazamiento horizontal del centro
    centerOffsetY: Float = 0f         // Desplazamiento vertical del centro
    ) {
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
            // Velocímetro con imagen de fondo
            CustomSpeedometerWithImage(
                speed = animatedSpeed,
                startAngleDegrees = startAngleDegrees,
                endAngleDegrees = endAngleDegrees,
                needleLengthScale = needleLengthScale,
                centerOffsetX = centerOffsetX,
                centerOffsetY = centerOffsetY,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Controles de prueba
            Text(
                text = "Controles de Prueba",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            // Slider para control preciso
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

            Spacer(modifier = Modifier.height(16.dp))

            // Información de calibración
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "📐 Calibración Actual",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val currentAngle = calculateCustomNeedleAngle(
                        animatedSpeed, 120f, startAngleDegrees, endAngleDegrees
                    )

                    Text(
                        text = "Velocidad: ${animatedSpeed.toInt()} km/h",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Ángulo aguja: ${currentAngle.toInt()}°",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Rango: ${startAngleDegrees.toInt()}° → ${endAngleDegrees.toInt()}°",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "💡 Ajusta startAngleDegrees y endAngleDegrees en el código",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    @Composable
    fun CustomSpeedometerWithImage(
        speed: Float,
        modifier: Modifier = Modifier,
        startAngleDegrees: Float = 210f,
        endAngleDegrees: Float = 330f,
        needleLengthScale: Float = 0.65f,
        centerOffsetX: Float = 0f,
        centerOffsetY: Float = 0f,
        maxSpeed: Float = 120f,
        showSpeedText: Boolean = true,
        speedTextColor: Color = Color(0xFFc8f0d4),
        needleColor: Color = Color(0xFFff3838)
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

            // Canvas para dibujar la aguja
            CustomSpeedNeedle(
                speed = speed,
                maxSpeed = maxSpeed,
                startAngleDegrees = startAngleDegrees,
                endAngleDegrees = endAngleDegrees,
                needleLengthScale = needleLengthScale,
                centerOffsetX = centerOffsetX,
                centerOffsetY = centerOffsetY,
                needleColor = needleColor,
                modifier = Modifier.fillMaxSize()
            )

            // Overlay con el valor de velocidad
            if (showSpeedText) {
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
                        // Valor grande de velocidad
                        Text(
                            text = "${speed.toInt()}",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-2).sp
                            ),
                            color = speedTextColor
                        )

                        // Unidad km/h
                        Text(
                            text = "km/h",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.sp
                            ),
                            color = speedTextColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }

    /**
     * Calcula el ángulo de la aguja basándose en la velocidad
     * con ángulos de inicio y fin personalizables
     */
    private fun calculateCustomNeedleAngle(
        speed: Float,
        maxSpeed: Float,
        startAngle: Float,
        endAngle: Float
    ): Float {
        // Normalizar la velocidad entre 0 y 1
        val speedPercentage = (speed / maxSpeed).coerceIn(0f, 1f)

        // Calcular el recorrido total del arco
        // Manejar el caso donde endAngle < startAngle (cruza el punto 0°)
        val sweepAngle = if (endAngle >= startAngle) {
            endAngle - startAngle
        } else {
            (360f - startAngle) + endAngle
        }

        // Calcular ángulo final
        val finalAngle = startAngle + (speedPercentage * sweepAngle)

        // Normalizar a 0-360°
        return if (finalAngle >= 360f) finalAngle - 360f else finalAngle
    }

    @Composable
    fun CustomSpeedNeedle(
        speed: Float,
        maxSpeed: Float,
        modifier: Modifier = Modifier,
        startAngleDegrees: Float = 210f,
        endAngleDegrees: Float = 330f,
        needleLengthScale: Float = 0.65f,
        centerOffsetX: Float = 0f,
        centerOffsetY: Float = 0f,
        needleColor: Color = Color(0xFFff3838)
    ) {
        Canvas(modifier = modifier) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Centro con desplazamiento personalizable
            val centerX = (canvasWidth / 2f) + centerOffsetX
            val centerY = (canvasHeight / 2f) + centerOffsetY

            // Radio de la aguja ajustable
            val needleLength = (minOf(canvasWidth, canvasHeight) / 2f) * needleLengthScale

            // Calcular ángulo correcto usando los parámetros personalizados
            val needleAngle = calculateCustomNeedleAngle(
                speed, maxSpeed, startAngleDegrees, endAngleDegrees
            )

            // Dibujar la aguja
            rotate(degrees = needleAngle, pivot = Offset(centerX, centerY)) {
                val needlePath = Path().apply {
                    val needleWidth = 8f
                    val needleBaseWidth = 12f

                    // Forma de flecha de la aguja
                    moveTo(centerX - needleBaseWidth, centerY)
                    lineTo(centerX - needleWidth / 2, centerY - needleLength * 0.15f)
                    lineTo(centerX, centerY - needleLength)
                    lineTo(centerX + needleWidth / 2, centerY - needleLength * 0.15f)
                    lineTo(centerX + needleBaseWidth, centerY)

                    // Cola de la aguja
                    lineTo(centerX + needleBaseWidth / 2, centerY)
                    lineTo(centerX + needleBaseWidth / 2, centerY + 15f)
                    lineTo(centerX - needleBaseWidth / 2, centerY + 15f)
                    lineTo(centerX - needleBaseWidth / 2, centerY)

                    close()
                }

                // Sombra de la aguja
                drawPath(
                    path = needlePath,
                    color = Color.Black.copy(alpha = 0.3f)
                )

                // Aguja principal
                drawPath(
                    path = needlePath,
                    color = needleColor
                )
            }

            // Círculo central base (gris oscuro)
            drawCircle(
                color = Color(0xFF3a3a3a),
                radius = 25f,
                center = Offset(centerX, centerY)
            )

            // Círculo central superior
            drawCircle(
                color = needleColor,
                radius = 15f,
                center = Offset(centerX, centerY)
            )

            // Punto central pequeño
            drawCircle(
                color = Color(0xFF1a1a1a),
                radius = 5f,
                center = Offset(centerX, centerY)
            )
        }
    }


// ============================================
// EJEMPLO CON VALORES AJUSTADOS SEGÚN TU DESCRIPCIÓN
// ============================================

    /**
     * Según mencionaste:
     * - En valor 48 la aguja debería estar en 0 km/h
     * - En valor 168 la aguja debería estar en 120 km/h
     *
     * Esto significa que estás trabajando con un rango de 48-168 (120 unidades)
     * Voy a crear una versión que acepta ese rango directamente
     */
    @Composable
    fun SpeedometerWithRawValues(
        rawValue: Float,  // Valor entre 48 y 168
        minRawValue: Float = 48f,
        maxRawValue: Float = 168f
    ) {
        // Convertir el valor crudo a km/h (0-120)
        val speed = ((rawValue - minRawValue) / (maxRawValue - minRawValue) * 120f)
            .coerceIn(0f, 120f)

        var targetRawValue by remember { mutableFloatStateOf(minRawValue) }
        val animatedRawValue by animateFloatAsState(
            targetValue = targetRawValue,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "raw_value_animation"
        )

        val displaySpeed = ((animatedRawValue - minRawValue) / (maxRawValue - minRawValue) * 120f)
            .coerceIn(0f, 120f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CustomSpeedometerWithImage(
                speed = displaySpeed,
                startAngleDegrees = 210f,  // Ajusta esto
                endAngleDegrees = 330f,    // Y esto
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Controles con valores crudos
            Text(
                text = "Controles (valores crudos: 48-168)",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilledTonalButton(onClick = { targetRawValue = 48f }) {
                    Text("48\n(0)")
                }
                FilledTonalButton(onClick = { targetRawValue = 78f }) {
                    Text("78\n(30)")
                }
                FilledTonalButton(onClick = { targetRawValue = 108f }) {
                    Text("108\n(60)")
                }
                FilledTonalButton(onClick = { targetRawValue = 138f }) {
                    Text("138\n(90)")
                }
                FilledTonalButton(onClick = { targetRawValue = 168f }) {
                    Text("168\n(120)")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Valor crudo: ${animatedRawValue.toInt()}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Velocidad: ${displaySpeed.toInt()} km/h",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = targetRawValue,
                        onValueChange = { targetRawValue = it },
                        valueRange = minRawValue..maxRawValue,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }