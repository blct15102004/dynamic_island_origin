package com.tuan.islebridge

import com.tuan.islebridge.core.notifications.ParsedProgress
import org.junit.Assert.*
import org.junit.Test

class NotificationProgressExtractorTest {

    @Test
    fun testPercentageCalculation() {
        val current = 45
        val max = 100
        val percentage = ((current.toDouble() / max.toDouble()) * 100).toInt().coerceIn(0, 100)
        val progress = ParsedProgress(current, max, false, percentage)

        assertEquals(45, progress.percentage)
        assertFalse(progress.isIndeterminate)
    }

    @Test
    fun testIndeterminateProgress() {
        val progress = ParsedProgress(0, 100, true, 0)
        assertTrue(progress.isIndeterminate)
        assertEquals(0, progress.percentage)
    }

    @Test
    fun testProgressCoerceInBounds() {
        val current = 150
        val max = 100
        val percentage = ((current.toDouble() / max.toDouble()) * 100).toInt().coerceIn(0, 100)
        val progress = ParsedProgress(current, max, false, percentage)
        assertEquals(100, progress.percentage)
    }
}

