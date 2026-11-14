package com.tuan.islebridge.core.bluetooth

data class EarbudBatteryInfo(
    val deviceName: String,
    val deviceAddress: String,
    val isConnected: Boolean,
    val overallBattery: Int? = null,
    val leftBattery: Int? = null,
    val rightBattery: Int? = null,
    val caseBattery: Int? = null,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun formatBatteryText(): String {
        val parts = mutableListOf<String>()
        if (leftBattery != null) parts.add("L: $leftBattery%") else parts.add("L: Unknown")
        if (rightBattery != null) parts.add("R: $rightBattery%") else parts.add("R: Unknown")
        if (caseBattery != null) parts.add("Case: $caseBattery%")
        return parts.joinToString(" | ")
    }
}

