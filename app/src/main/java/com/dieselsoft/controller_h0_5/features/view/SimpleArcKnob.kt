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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

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
                        isDragging = true
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val angle = calculateAngle(offset, center)
                        val newValue = angleToValue(angle, minValue, maxValue)
                        currentValue = newValue
                        onValueChange(newValue)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val angle = calculateAngle(change.position, center)
                        val newValue = angleToValue(angle, minValue, maxValue)
                        currentValue = newValue
                        onValueChange(newValue)
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

            // Cursor
            val cursorAngleRad = Math.toRadians(currentAngle.toDouble())
            val cursorX = centerX + radius * cos(cursorAngleRad).toFloat()
            val cursorY = centerY + radius * sin(cursorAngleRad).toFloat()

            // Sombra del cursor - usa onSurface con transparencia
            drawCircle(
                color = onSurfaceColor.copy(alpha = 0.2f),
                center = Offset(cursorX + 2f, cursorY + 2f),
                radius = 24f
            )

            // Cursor exterior - usa onSurface del tema
            drawCircle(
                color = onSurfaceColor,
                center = Offset(cursorX, cursorY),
                radius = 24f
            )

            // Cursor interior - usa primary del tema
            drawCircle(
                color = primaryColor,
                center = Offset(cursorX, cursorY),
                radius = 16f
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