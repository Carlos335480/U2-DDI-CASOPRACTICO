package com.ejemplo.caminarcorrer.shared.logic

import com.ejemplo.caminarcorrer.shared.model.ActivityType
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalorieCalculatorTest {

    @Test
    fun `walking 30 minutes at 5 km per hour returns a reasonable estimate`() {
        val kcal = CalorieCalculator.estimateNetKcal(
            type = ActivityType.WALKING,
            weightKg = 70.0,
            distanceMeters = 2_500.0,
            activeDurationMillis = 30 * 60 * 1000L
        )

        assertTrue(kcal != null && kcal in 70..110)
    }

    @Test
    fun `running 30 minutes at 10 km per hour returns a reasonable estimate`() {
        val kcal = CalorieCalculator.estimateNetKcal(
            type = ActivityType.RUNNING,
            weightKg = 70.0,
            distanceMeters = 5_000.0,
            activeDurationMillis = 30 * 60 * 1000L
        )

        assertTrue(kcal != null && kcal in 300..400)
    }

    @Test
    fun `missing weight returns null`() {
        val kcal = CalorieCalculator.estimateNetKcal(
            type = ActivityType.WALKING,
            weightKg = null,
            distanceMeters = 2_500.0,
            activeDurationMillis = 30 * 60 * 1000L
        )

        assertNull(kcal)
    }

    @Test
    fun `missing distance returns null`() {
        val kcal = CalorieCalculator.estimateNetKcal(
            type = ActivityType.WALKING,
            weightKg = 70.0,
            distanceMeters = null,
            activeDurationMillis = 30 * 60 * 1000L
        )

        assertNull(kcal)
    }

    @Test
    fun `very short activity returns null`() {
        val kcal = CalorieCalculator.estimateNetKcal(
            type = ActivityType.RUNNING,
            weightKg = 70.0,
            distanceMeters = 100.0,
            activeDurationMillis = 10_000L
        )

        assertNull(kcal)
    }
}
