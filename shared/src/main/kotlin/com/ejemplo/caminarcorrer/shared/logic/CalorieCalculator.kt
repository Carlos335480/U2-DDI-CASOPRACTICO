package com.ejemplo.caminarcorrer.shared.logic

import com.ejemplo.caminarcorrer.shared.model.ActivityType
import kotlin.math.roundToInt

/**
 * Estimación de calorías activas (netas) a partir de ecuaciones ACSM.
 *
 * Caminar: VO2 (ml/kg/min) = 0,1 * velocidad(m/min) + 3,5
 * Correr:  VO2 (ml/kg/min) = 0,2 * velocidad(m/min) + 3,5
 * MET = VO2 / 3,5
 * kcal netas = (MET - 1) * 3,5 * peso(kg) / 200 * minutos
 *
 * Es una estimación poblacional. No considera edad, sexo, composición corporal,
 * terreno, temperatura, frecuencia cardiaca ni eficiencia individual.
 */
object CalorieCalculator {

    private const val MIN_WEIGHT_KG = 20.0
    private const val MAX_WEIGHT_KG = 300.0
    private const val MIN_DISTANCE_METERS = 10.0
    private const val MIN_DURATION_MILLIS = 30_000L
    private const val MIN_SPEED_MPS = 0.5
    private const val MAX_SPEED_MPS = 8.0

    const val FORMULA_DESCRIPTION =
        "MET estimado con ecuaciones ACSM. Caminar: VO2 = 0,1 x velocidad(m/min) + 3,5; " +
            "correr: VO2 = 0,2 x velocidad(m/min) + 3,5. " +
            "kcal netas = (MET - 1) x 3,5 x peso(kg) / 200 x minutos."

    fun estimateMet(
        type: ActivityType,
        distanceMeters: Double?,
        activeDurationMillis: Long
    ): Double? {
        if (distanceMeters == null || distanceMeters < MIN_DISTANCE_METERS) return null
        if (activeDurationMillis < MIN_DURATION_MILLIS) return null

        val speedMps = distanceMeters / (activeDurationMillis / 1000.0)
        if (speedMps !in MIN_SPEED_MPS..MAX_SPEED_MPS) return null

        val speedMetersPerMinute = speedMps * 60.0
        return when (type) {
            ActivityType.WALKING ->
                ((0.1 * speedMetersPerMinute + 3.5) / 3.5).coerceIn(2.0, 8.0)

            ActivityType.RUNNING ->
                ((0.2 * speedMetersPerMinute + 3.5) / 3.5).coerceIn(4.0, 20.0)
        }
    }

    fun estimateNetKcal(
        type: ActivityType,
        weightKg: Double?,
        distanceMeters: Double?,
        activeDurationMillis: Long
    ): Int? {
        if (weightKg == null || weightKg !in MIN_WEIGHT_KG..MAX_WEIGHT_KG) return null
        val met = estimateMet(type, distanceMeters, activeDurationMillis) ?: return null
        val minutes = activeDurationMillis / 60_000.0
        val netKcal = (met - 1.0) * 3.5 * weightKg / 200.0 * minutes
        return netKcal.coerceAtLeast(0.0).roundToInt()
    }
}
