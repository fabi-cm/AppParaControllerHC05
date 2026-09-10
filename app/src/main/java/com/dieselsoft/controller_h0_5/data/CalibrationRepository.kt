package com.dieselsoft.controller_h0_5.data

import android.content.Context
import android.content.SharedPreferences

class CalibrationRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("calibration_prefs", Context.MODE_PRIVATE)

    // Valores por defecto basados en tu última calibración
    private val defaultValues = mapOf(
        0 to 0,
        15 to 18,
        20 to 25, // Punto intermedio sugerido
        30 to 37,
        60 to 73,
        90 to 111,
        120 to 149,
        140 to 173
    )

    fun getCalibrationMap(): Map<Int, Int> {
        val map = mutableMapOf<Int, Int>()
        defaultValues.keys.forEach { speed ->
            val value = prefs.getInt("speed_$speed", defaultValues[speed] ?: 0)
            map[speed] = value
        }
        return map.toSortedMap()
    }

    fun saveCalibrationPoint(speed: Int, value: Int) {
        prefs.edit().putInt("speed_$speed", value).apply()
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
    }
}