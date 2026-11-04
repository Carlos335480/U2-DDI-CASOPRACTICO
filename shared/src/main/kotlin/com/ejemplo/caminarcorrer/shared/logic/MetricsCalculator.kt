package com.ejemplo.caminarcorrer.shared.logic

import kotlin.math.roundToLong

object MetricsCalculator {

    fun averagePaceSecondsPerKm(
        distanceMeters: Double?,
        activeDurationMillis: Long
    ): Long? {
        if (distanceMeters == null || distanceMeters < 10.0) return null
        if (activeDurationMillis < 1_000L) return null
        val seconds = activeDurationMillis / 1000.0
        val kilometers = distanceMeters / 1000.0
        return (seconds / kilometers).roundToLong().takeIf { it > 0 }
    }
}
