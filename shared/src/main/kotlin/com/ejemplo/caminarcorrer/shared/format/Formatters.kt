package com.ejemplo.caminarcorrer.shared.format

import com.ejemplo.caminarcorrer.shared.model.ActivityType
import com.ejemplo.caminarcorrer.shared.model.DistanceSource
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val SPANISH_LOCALE = Locale.forLanguageTag("es-ES")
private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", SPANISH_LOCALE)

object Formatters {

    fun duration(activeDurationMillis: Long): String {
        val totalSeconds = (activeDurationMillis / 1000L).coerceAtLeast(0L)
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L
        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    fun distanceKm(distanceMeters: Double?): String {
        if (distanceMeters == null || distanceMeters < 0.0) return "—"
        return String.format(SPANISH_LOCALE, "%.2f km", distanceMeters / 1000.0)
    }

    fun pace(secondsPerKm: Long?): String {
        if (secondsPerKm == null || secondsPerKm <= 0L) return "—"
        return String.format(
            Locale.US,
            "%d:%02d /km",
            secondsPerKm / 60L,
            secondsPerKm % 60L
        )
    }

    fun calories(caloriesKcal: Int?): String =
        caloriesKcal?.let { "$it kcal" } ?: "—"

    fun weight(weightKg: Double?): String =
        weightKg?.let { String.format(SPANISH_LOCALE, "%.1f kg", it) } ?: "—"

    fun date(epochMillis: Long): String =
        Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .format(DATE_FORMATTER)

    fun activityType(type: ActivityType): String = type.displayName

    fun distanceSource(source: DistanceSource): String = source.displayName
}
