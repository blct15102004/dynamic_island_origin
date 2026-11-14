package com.tuan.islebridge

import com.tuan.islebridge.core.bluetooth.EarbudBatteryInfo
import org.junit.Assert.*
import org.junit.Test

class BluetoothBatteryTest {

    @Test
    fun testBatteryFormat_WhenLevelsKnown() {
        val info = EarbudBatteryInfo(
            deviceName = "vivo TWS 4",
            deviceAddress = "00:11:22:33:44:55",
            isConnected = true,
            overallBattery = 90,
            leftBattery = 92,
            rightBattery = 88,
            caseBattery = 75
        )

        val formatted = info.formatBatteryText()
        assertTrue(formatted.contains("L: 92%"))
        assertTrue(formatted.contains("R: 88%"))
        assertTrue(formatted.contains("Case: 75%"))
    }

    @Test
    fun testBatteryFormat_WhenLevelsUnknown_NeverInventsValues() {
        val info = EarbudBatteryInfo(
            deviceName = "Standard BT Headset",
            deviceAddress = "11:22:33:44:55:66",
            isConnected = true,
            overallBattery = null,
            leftBattery = null,
            rightBattery = null,
            caseBattery = null
        )

        val formatted = info.formatBatteryText()
        assertTrue("Unknown values must be reported as Unknown", formatted.contains("L: Unknown"))
        assertTrue("Unknown values must be reported as Unknown", formatted.contains("R: Unknown"))
        assertFalse("Unreported case battery should not be shown", formatted.contains("Case:"))
    }
}

