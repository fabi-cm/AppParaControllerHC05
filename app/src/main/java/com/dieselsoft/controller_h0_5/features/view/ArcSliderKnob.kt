package com.dieselsoft.controller_h0_5.features.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcSliderKnob(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    minValue: Float = 0f,
    maxValue: Float = 255f,
    enabled: Boolean = true
) {
    var currentValue by remember { mutableFloatStateOf(value) }
    var isDragging by remember { mutableStateOf(false) }

    // Sincronizar valor cuando cambia externamente
    if (!isDragging && value != currentValue) {
        currentValue = value
    }

    Box(
        modifier = modifier
            .size(340.dp)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput

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
        Canvas(modifier = Modifier.size(340.dp)) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth / 2
            val centerY = canvasHeight / 2
            val radius = canvasWidth * 0.38f

            val startAngle = 135f
            val sweepAngle = 270f
            val strokeWidth = 32f

            // Dibujar arco de fondo (track)
            drawArc(
                color = Color.White.copy(alpha = 0.15f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Calcular ángulo actual basado en el valor
            val currentAngle = valueToAngle(currentValue, minValue, maxValue)
            val progressSweep = currentAngle - startAngle

            // Dibujar arco de progreso con gradiente
            if (progressSweep > 0) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0f to Color(0xFF4CAF50),
                        0.5f to Color(0xFF2196F3),
                        1f to Color(0xFF9C27B0),
                        center = Offset(centerX, centerY)
                    ),
                    startAngle = startAngle,
                    sweepAngle = progressSweep,
                    useCenter = false,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Dibujar marcas de escala con valores
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 36f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }

            for (i in 0..10) {
                val markAngle = startAngle + (sweepAngle * i / 10f)
                val markAngleRad = Math.toRadians(markAngle.toDouble())

                val innerRadius = radius * 0.78f
                val outerRadius = radius * 1.12f

                val startX = centerX + innerRadius * cos(markAngleRad).toFloat()
                val startY = centerY + innerRadius * sin(markAngleRad).toFloat()
                val endX = centerX + outerRadius * cos(markAngleRad).toFloat()
                val endY = centerY + outerRadius * sin(markAngleRad).toFloat()

                // Marcas principales cada 2 (0, 2, 4, 6, 8, 10)
                val isMainMark = i % 2 == 0

                drawLine(
                    color = Color.White.copy(alpha = if (isMainMark) 0.5f else 0.25f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (isMainMark) 5f else 3f,
                    cap = StrokeCap.Round
                )

                // Dibujar valores numéricos en las marcas principales
                if (isMainMark) {
                    val labelValue = ((maxValue - minValue) * i / 10f + minValue).toInt()
                    val labelRadius = radius * 1.28f
                    val labelX = centerX + labelRadius * cos(markAngleRad).toFloat()
                    val labelY = centerY + labelRadius * sin(markAngleRad).toFloat() + 12f // +12 para centrar verticalmente

                    drawContext.canvas.nativeCanvas.drawText(
                        labelValue.toString(),
                        labelX,
                        labelY,
                        paint
                    )
                }
            }

            // Calcular posición del cursor
            val cursorAngleRad = Math.toRadians(currentAngle.toDouble())
            val cursorX = centerX + radius * cos(cursorAngleRad).toFloat()
            val cursorY = centerY + radius * sin(cursorAngleRad).toFloat()

            // Dibujar sombra del cursor
            drawCircle(
                color = Color.Black.copy(alpha = 0.4f),
                center = Offset(cursorX + 3f, cursorY + 3f),
                radius = 28f
            )

            // Dibujar cursor exterior
            drawCircle(
                color = Color.White,
                center = Offset(cursorX, cursorY),
                radius = 28f
            )

            // Dibujar cursor interior con color del gradiente
            val normalizedValue = (currentValue - minValue) / (maxValue - minValue)
            val cursorColor = when {
                normalizedValue < 0.33f -> Color(0xFF4CAF50)
                normalizedValue < 0.66f -> Color(0xFF2196F3)
                else -> Color(0xFF9C27B0)
            }

            drawCircle(
                color = cursorColor,
                center = Offset(cursorX, cursorY),
                radius = 20f
            )

            // Punto brillante en el cursor
            drawCircle(
                color = Color.White.copy(alpha = 0.6f),
                center = Offset(cursorX - 6f, cursorY - 6f),
                radius = 6f
            )

            // Punto central decorativo
            drawCircle(
                color = Color.White.copy(alpha = 0.2f),
                center = Offset(centerX, centerY),
                radius = 12f
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.4f),
                center = Offset(centerX, centerY),
                radius = 6f
            )
        }
    }
}

// Calcular ángulo desde el centro basado en la posición del toque
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