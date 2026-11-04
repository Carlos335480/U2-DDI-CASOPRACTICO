package com.ejemplo.caminarcorrer.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class ActivityRecord(
    val id: String,
    val type: ActivityType,
    val startEpochMillis: Long,
    val endEpochMillis: Long,
    val activeDurationMillis: Long,
    val distanceMeters: Double? = null,
    val caloriesKcal: Int? = null,
    val averagePaceSecondsPerKm: Long? = null,
    val weightKg: Double? = null,
    val distanceSource: DistanceSource = DistanceSource.NONE,
    val synced: Boolean = false,
    val schemaVersion: Int = 1
) {
    val hasDistance: Boolean
        get() = distanceMeters != null && distanceMeters > 0.0

    val hasCalories: Boolean
        get() = caloriesKcal != null
}
