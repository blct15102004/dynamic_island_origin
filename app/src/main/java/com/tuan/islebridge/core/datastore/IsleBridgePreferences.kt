package com.tuan.islebridge.core.datastore

data class IsleBridgePreferences(
    val isIslandEnabled: Boolean = true,
    val isOverlayFallbackEnabled: Boolean = false,
    val forceNativeIsland: Boolean = false,
    val isNotificationMirroringEnabled: Boolean = true,
    val isBluetoothMonitoringEnabled: Boolean = true,
    val bluetoothModeAlwaysShow: Boolean = false, // false = Auto Hide
    val bluetoothTimeoutMs: Long = 15000L,
    val isOtpDetectionEnabled: Boolean = true,
    val isOtpAutoCopyEnabled: Boolean = true,
    val otpTimeoutMs: Long = 30000L,
    val otpClearClipboardTimeoutMs: Long = 30000L,
    val isPrivacyAlertEnabled: Boolean = true,
    val isShizukuEnabled: Boolean = false,
    val keepOriginalNotifications: Boolean = true,
    val appTheme: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val allowedPackages: Set<String> = emptySet(),
    val blockedPackages: Set<String> = setOf(
        "com.google.android.apps.walletnfcrel",
        "com.android.vending",
        "com.vnpay.bidv",
        "com.vng.zalopay",
        "com.mservice.momotransfer"
    )
)

