package com.ejemplo.caminarcorrer.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class ProfilePayload(
    val weightKg: Double,
    val updatedAtEpochMillis: Long,
    val schemaVersion: Int = 1
)

@Serializable
data class ActivityPayload(
    val activity: ActivityRecord,
    val schemaVersion: Int = 1
)

@Serializable
data class AckPayload(
    val activityId: String,
    val receivedAtEpochMillis: Long,
    val schemaVersion: Int = 1
)
