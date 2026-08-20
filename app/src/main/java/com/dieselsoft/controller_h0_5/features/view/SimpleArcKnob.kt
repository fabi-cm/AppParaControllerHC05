package com.dieselsoft.controller_h0_5.features.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun SimpleArcKnob(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    minValue: Float = 0f,
    maxValue: Float = 255f
) {
    var currentValue by remember { mutableFloatStateOf(value) }
    var isDragging by remember { mutableStateOf(false) }

    if (!isDragging && value != currentValue) {
        currentValue = value
    }

    // Colores del tema de Material 3
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier
            .size(280.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.width * 0.4f

                        if (isTouchNearArc(offset, center, radius)) {
                            isDragging = true
                            val angle = calculateAngle(offset, center)
                            val newValue = angleToValue(angle, minValue, maxValue)
                            currentValue = newValue
                            onValueChange(newValue)
                        }
                    },
                    onDrag = { change, _ ->
                        if (isDragging) {
                            change.consume()
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val angle = calculateAngle(change.position, center)
                            val newValue = angleToValue(angle, minValue, maxValue)
                            currentValue = newValue
                            onValueChange(newValue)
                        }
                    },
                    onDragEnd = {
                        isDragging = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(280.dp)) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.width * 0.4f

            val startAngle = 135f
            val sweepAngle = 270f
            val strokeWidth = 28f

            // Arco de fondo - usa surfaceVariant del tema
            drawArc(
                color = surfaceVariantColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Arco de progreso - usa primary del tema
            val currentAngle = valueToAngle(currentValue, minValue, maxValue)
            val progressSweep = currentAngle - startAngle

            if (progressSweep > 0) {
                drawArc(
                    color = primaryColor,
                    startAngle = startAngle,
                    sweepAngle = progressSweep,
                    useCenter = false,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Marcas de escala - usa onSurface del tema
            for (i in 0..10) {
                val markAngle = startAngle + (sweepAngle * i / 10f)
                val markAngleRad = Math.toRadians(markAngle.toDouble())

                val innerRadius = radius * 0.82f
                val outerRadius = radius * 1.08f

                val startX = centerX + innerRadius * cos(markAngleRad).toFloat()
                val startY = centerY + innerRadius * sin(markAngleRad).toFloat()
                val endX = centerX + outerRadius * cos(markAngleRad).toFloat()
                val endY = centerY + outerRadius * sin(markAngleRad).toFloat()

                drawLine(
                    color = onSurfaceColor.copy(alpha = 0.4f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (i % 5 == 0) 4f else 2f,
                    cap = StrokeCap.Round
                )
            }

            // Posición del cursor (camión)
            val cursorAngleRad = Math.toRadians(currentAngle.toDouble())
            val cursorX = centerX + radius * cos(cursorAngleRad).toFloat()
            val cursorY = centerY + radius * sin(cursorAngleRad).toFloat()

            // Dibujar el camión
            drawTruck(
                center = Offset(cursorX, cursorY),
                primaryColor = primaryColor,
                onSurfaceColor = onSurfaceColor,
                rotationAngle = currentAngle
            )

            // Punto central - usa onSurface con transparencia
            drawCircle(
                color = onSurfaceColor.copy(alpha = 0.2f),
                center = Offset(centerX, centerY),
                radius = 8f
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTruck(
    center: Offset,
    primaryColor: Color,
    onSurfaceColor: Color,
    rotationAngle: Float
) {
    rotate(rotationAngle + 90f, center) {
        val truckWidth = 40f
        val truckHeight = 28f

        // Sombra del camión
        drawRect(
            color = onSurfaceColor.copy(alpha = 0.2f),
            topLeft = Offset(center.x - truckWidth/2 + 2f, center.y - truckHeight/2 + 2f),
            size = androidx.compose.ui.geometry.Size(truckWidth * 0.4f, truckHeight)
        )

        // Cabina
        drawRect(
            color = primaryColor,
            topLeft = Offset(center.x - truckWidth/2, center.y - truckHeight/2),
            size = androidx.compose.ui.geometry.Size(truckWidth * 0.4f, truckHeight)
        )

        // Caja del camión
        drawRect(
            color = primaryColor.copy(alpha = 0.85f),
            topLeft = Offset(center.x - truckWidth/2 + truckWidth * 0.4f, center.y - truckHeight/2 + truckHeight * 0.2f),
            size = androidx.compose.ui.geometry.Size(truckWidth * 0.6f, truckHeight * 0.8f)
        )

        // Ventana
        drawRect(
            color = Color.White.copy(alpha = 0.9f),
            topLeft = Offset(center.x - truckWidth/2 + 6f, center.y - truckHeight/2 + 5f),
            size = androidx.compose.ui.geometry.Size(10f, 8f)
        )

        // Ruedas
        val wheel1X = center.x - truckWidth/2 + 10f
        val wheel2X = center.x + truckWidth/2 - 10f
        val wheelY = center.y + truckHeight/2

        // Rueda trasera
        drawCircle(
            color = onSurfaceColor,
            center = Offset(wheel1X, wheelY),
            radius = 6f
        )
        drawCircle(
            color = Color.DarkGray,
            center = Offset(wheel1X, wheelY),
            radius = 4f
        )

        // Rueda delantera
        drawCircle(
            color = onSurfaceColor,
            center = Offset(wheel2X, wheelY),
            radius = 6f
        )
        drawCircle(
            color = Color.DarkGray,
            center = Offset(wheel2X, wheelY),
            radius = 4f
        )

        // Parrilla frontal
        drawLine(
            color = onSurfaceColor,
            start = Offset(center.x + truckWidth/2, center.y - truckHeight/2 + 8f),
            end = Offset(center.x + truckWidth/2, center.y - truckHeight/2 + 16f),
            strokeWidth = 2f
        )

        // Faros
        drawCircle(
            color = Color(0xFFFFEB3B),
            center = Offset(center.x + truckWidth/2 - 2f, center.y - truckHeight/2 + 20f),
            radius = 2f
        )
    }
}

private fun isTouchNearArc(touch: Offset, center: Offset, radius: Float): Boolean {
    val dx = touch.x - center.x
    val dy = touch.y - center.y
    val distance = sqrt(dx * dx + dy * dy)

    val tolerance = 60f
    return distance >= (radius - tolerance) && distance <= (radius + tolerance)
}

private fun calculateAngle(position: Offset, center: Offset): Float {
    val dx = position.x - center.x
    val dy = position.y - center.y
    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()

    if (angle < 0) angle += 360f

    return when {
        angle >= 135f && angle <= 360f -> angle.coerceIn(135f, 360f)
        angle >= 0f && angle <= 45f -> angle + 360f
        angle > 45f && angle < 135f -> {
            if (angle < 90f) 45f + 360f else 135f
        }
        else -> angle
    }.coerceIn(135f, 405f)
}

private fun valueToAngle(value: Float, minValue: Float, maxValue: Float): Float {
    val normalized = ((value - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)
    return 135f + (normalized * 270f)
}

private fun angleToValue(angle: Float, minValue: Float, maxValue: Float): Float {
    val clampedAngle = angle.coerceIn(135f, 405f)
    val normalized = (clampedAngle - 135f) / 270f
    return (normalized * (maxValue - minValue) + minValue).coerceIn(minValue, maxValue)
}