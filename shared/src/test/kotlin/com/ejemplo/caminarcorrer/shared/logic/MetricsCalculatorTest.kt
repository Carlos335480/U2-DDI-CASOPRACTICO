package com.ejemplo.caminarcorrer.shared.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MetricsCalculatorTest {

    @Test
    fun `five kilometers in thirty minutes is six minutes per kilometer`() {
        val pace = MetricsCalculator.averagePaceSecondsPerKm(
            distanceMeters = 5_000.0,
            activeDurationMillis = 30 * 60 * 1000L
        )

        assertEquals(360L, pace)
    }

    @Test
    fun `zero distance returns null`() {
        assertNull(
            MetricsCalculator.averagePaceSecondsPerKm(
                distanceMeters = 0.0,
                activeDurationMillis = 10 * 60 * 1000L
            )
        )
    }

    @Test
    fun `missing distance returns null`() {
        assertNull(
            MetricsCalculator.averagePaceSecondsPerKm(
                distanceMeters = null,
                activeDurationMillis = 10 * 60 * 1000L
            )
        )
    }
}
