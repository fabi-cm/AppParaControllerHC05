package com.dieselsoft.controller_h0_5.features.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun KnobControl(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..255f,
    size: Int = 200
) {
    var angle by remember { mutableStateOf(135f + (270f * (value / valueRange.endInclusive))) }

    Canvas(
        modifier = Modifier
            .size(size.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val center = Offset(size / 2f, size / 2f)
                    val touchAngle = atan2(
                        change.position.y - center.y,
                        change.position.x - center.x
                    ) * (180f / PI.toFloat())

                    // Limitamos el ángulo entre 135° y 405° (270° útiles)
                    val fixedAngle = ((touchAngle + 360) % 360).coerceIn(135f, 405f)

                    val newValue = ((fixedAngle - 135f) / 270f) * valueRange.endInclusive
                    onValueChange(newValue.coerceIn(valueRange.start, valueRange.endInclusive))
                    angle = fixedAngle
                }
            }
    ) {
        val radius = size / 2f
        val strokeWidth = 20f

        // Fondo del potenciómetro
        drawArc(
            color = Color.LightGray,
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Arco de progreso
        drawArc(
            color = Color.LightGray,
            startAngle = 135f,
            sweepAngle = angle - 135f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Indicador central
        val knobAngleRad = Math.toRadians(angle.toDouble())
        val indicatorLength = radius * 0.6f
        drawLine(
            color = Color.Black,
            start = Offset(radius, radius),
            end = Offset(
                radius + cos(knobAngleRad).toFloat() * indicatorLength,
                radius + sin(knobAngleRad).toFloat() * indicatorLength
            ),
            strokeWidth = 6f,
            cap = StrokeCap.Round
        )
    }
}
