package com.ejemplo.caminarcorrer.shared.model

import kotlinx.serialization.Serializable

@Serializable
enum class ActivityType {
    WALKING,
    RUNNING;

    val displayName: String
        get() = when (this) {
            WALKING -> "Caminata"
            RUNNING -> "Carrera"
        }
}

@Serializable
enum class DistanceSource {
    GPS,
    STEPS,
    NONE;

    val displayName: String
        get() = when (this) {
            GPS -> "GPS / Health Services"
            STEPS -> "Estimada por pasos"
            NONE -> "Sin datos de distancia"
        }
}
