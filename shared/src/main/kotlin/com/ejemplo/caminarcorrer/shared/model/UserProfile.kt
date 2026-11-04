package com.ejemplo.caminarcorrer.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val weightKg: Double,
    val updatedAtEpochMillis: Long,
    val schemaVersion: Int = 1
)
