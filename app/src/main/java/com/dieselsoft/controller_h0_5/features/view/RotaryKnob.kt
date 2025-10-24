package com.dieselsoft.controller_h0_5.features.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RotaryKnob(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    minValue: Float = 0f,
    maxValue: Float = 255f,
    enabled: Boolean = true
) {
    var rotation by remember { mutableFloatStateOf(valueToRotation(value, minValue, maxValue)) }
    var isDragging by remember { mutableStateOf(false) }

    // Sincronizar rotación cuando el valor cambia externamente
    LaunchedEffect(value) {
        if (!isDragging) {
            rotation = valueToRotation(value, minValue, maxValue)
        }
    }

    Box(
        modifier = modifier
            .size(120.dp)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput

                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()

                        // Calcular nueva rotación basada en el movimiento
                        val dragChange = -dragAmount.y * 0.5f + dragAmount.x * 0.5f
                        val newRotation = (rotation + dragChange).coerceIn(0f, 270f)

                        rotation = newRotation

                        // Convertir rotación a valor y notificar
                        val newValue = rotationToValue(newRotation, minValue, maxValue)
                        onValueChange(newValue)
                    },
                    onDragEnd = {
                        isDragging = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Fondo del knob
        Canvas(
            modifier = Modifier
                .size(100.dp)
                .rotate(rotation)
        ) {
            // Círculo exterior
            drawCircle(
                color = Color.LightGray.copy(alpha = 0.1f),
                radius = size.minDimension / 2
            )

            // Marca indicadora
            val indicatorLength = size.minDimension / 2 * 0.7f
            val indicatorEnd = Offset(
                x = size.width / 2,
                y = size.height / 2 - indicatorLength
            )

            drawLine(
                color = Color.White,
                start = Offset(size.width / 2, size.height / 2),
                end = indicatorEnd,
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )

            // Punto central
            drawCircle(
                color = Color.White,
                center = Offset(size.width / 2, size.height / 2),
                radius = 4f
            )
        }

        // Marcas de escala (fondo fijo)
        Canvas(modifier = Modifier.size(120.dp)) {
            // Dibujar marcas de escala
            for (i in 0..8) {
                val angle = (i * 30 - 135).toDouble()
                val startAngle = Math.toRadians(angle)

                val innerRadius = size.minDimension / 2 * 0.6f
                val outerRadius = size.minDimension / 2 * 0.9f

                val startX = size.width / 2 + innerRadius * sin(startAngle).toFloat()
                val startY = size.height / 2 + innerRadius * cos(startAngle).toFloat()
                val endX = size.width / 2 + outerRadius * sin(startAngle).toFloat()
                val endY = size.height / 2 + outerRadius * cos(startAngle).toFloat()

                drawLine(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 2f
                )
            }
        }
    }
}

private fun valueToRotation(value: Float, minValue: Float, maxValue: Float): Float {
    val normalized = (value - minValue) / (maxValue - minValue)
    return normalized * 270f // 270 grados de rotación total
}

private fun rotationToValue(rotation: Float, minValue: Float, maxValue: Float): Float {
    val normalized = rotation / 270f
    return normalized * (maxValue - minValue) + minValue
}