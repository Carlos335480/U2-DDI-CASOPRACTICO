package com.ejemplo.caminarcorrer.shared.format

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    @Test
    fun `duration formats minutes and seconds`() {
        assertEquals("05:07", Formatters.duration(307_000L))
    }

    @Test
    fun `duration includes hours when needed`() {
        assertEquals("1:05:07", Formatters.duration(3_907_000L))
    }

    @Test
    fun `pace formats minutes and seconds`() {
        assertEquals("6:00 /km", Formatters.pace(360L))
    }

    @Test
    fun `missing values use a dash`() {
        assertEquals("—", Formatters.distanceKm(null))
        assertEquals("—", Formatters.pace(null))
        assertEquals("—", Formatters.calories(null))
        assertEquals("—", Formatters.weight(null))
    }
}
