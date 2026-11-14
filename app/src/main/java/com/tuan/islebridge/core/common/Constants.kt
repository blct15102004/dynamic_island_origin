package com.tuan.islebridge.core.common

object Constants {
    const val TAG = "IsleBridge"
    const val APPLICATION_ID = "com.tuan.islebridge"

    // Notification Channel IDs
    const val CHANNEL_ISLAND_NOTIFICATION = "islebridge_island_channel"
    const val CHANNEL_BLUETOOTH = "islebridge_bluetooth_channel"
    const val CHANNEL_OTP = "islebridge_otp_channel"
    const val CHANNEL_PRIVACY = "islebridge_privacy_channel"

    // OriginOS Super Island Protocol Keys
    object SuperXProtocol {
        const val KEY_OPERATION = "notification.superx.operation"
        const val KEY_SHOW_NOTIFY = "notification.superx.showNotify"
        const val KEY_TEMPLATE = "notification.superx.template"
        const val KEY_SCENE = "notification.superx.scene"
        const val KEY_BASE_INFOS = "notification.superx.baseInfos"
        const val KEY_CAPSULE = "notification.superx.capsule"
        const val KEY_INFOS = "notification.superx.infos"
        const val KEY_SHORT_INFOS = "notification.superx.shortInfos"
        const val KEY_ISLAND = "notification.superx.island"

        // Operations
        const val OP_MOUNT = 0
        const val OP_UNMOUNT = 1
        const val OP_UPDATE = 2

        // Known Templates
        const val TEMPLATE_BASE_INFO = 1
        const val TEMPLATE_PROGRESS = 2
        const val TEMPLATE_CAPSULE_ONLY = 3
        const val TEMPLATE_TEXT_AND_ICON = 4
        const val TEMPLATE_ICON_AND_TEXT = 5
        const val TEMPLATE_MEDIA_PROGRESS = 6
    }

    // Default Timeouts in milliseconds
    const val DEFAULT_TIMEOUT_BLUETOOTH_MS = 15000L
    const val DEFAULT_TIMEOUT_OTP_MS = 30000L
    const val DEFAULT_TIMEOUT_NOTIF_MS = 8000L
    const val DEFAULT_TIMEOUT_PRIVACY_MS = 6000L
    const val DEFAULT_TIMEOUT_TEST_MS = 3000L

    // Preference Keys
    const val PREF_DATASTORE_NAME = "islebridge_preferences"
}
