package com.ejemplo.caminarcorrer.wear.exercise

import com.ejemplo.caminarcorrer.shared.model.ActivityRecord
import com.ejemplo.caminarcorrer.shared.model.ActivityType
import com.ejemplo.caminarcorrer.shared.model.DistanceSource

enum class ExercisePhase {
    IDLE,
    STARTING,
    ACTIVE,
    PAUSED,
    FINISHED,
    ERROR
}

enum class ExerciseMode {
    HEALTH_SERVICES,
    TIMER_ONLY
}

data class ExerciseSessionState(
    val phase: ExercisePhase = ExercisePhase.IDLE,
    val mode: ExerciseMode = ExerciseMode.HEALTH_SERVICES,
    val type: ActivityType? = null,
    val activeDurationMillis: Long = 0L,
    val distanceMeters: Double? = null,
    val caloriesKcal: Int? = null,
    val paceSecondsPerKm: Long? = null,
    val gpsEnabled: Boolean = false,
    val distanceSource: DistanceSource = DistanceSource.NONE,
    val errorMessage: String? = null,
    val notice: String? = null,
    val summary: ActivityRecord? = null,
    val weightKg: Double? = null
) {
    val isRunning: Boolean
        get() = phase == ExercisePhase.ACTIVE || phase == ExercisePhase.PAUSED

    val isPaused: Boolean
        get() = phase == ExercisePhase.PAUSED
}
