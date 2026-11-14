package com.tuan.islebridge.core.notifications

import android.app.Notification
import android.os.Bundle

data class ParsedProgress(
    val current: Int,
    val max: Int,
    val isIndeterminate: Boolean,
    val percentage: Int
)

object NotificationProgressExtractor {

    fun extractProgress(notification: Notification): ParsedProgress? {
        val extras: Bundle = notification.extras ?: return null

        val max = extras.getInt(Notification.EXTRA_PROGRESS_MAX, 0)
        val current = extras.getInt(Notification.EXTRA_PROGRESS, 0)
        val isIndeterminate = extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE, false)

        if (isIndeterminate) {
            return ParsedProgress(
                current = 0,
                max = 100,
                isIndeterminate = true,
                percentage = 0
            )
        }

        if (max > 0 && current >= 0) {
            val percentage = ((current.toDouble() / max.toDouble()) * 100).toInt().coerceIn(0, 100)
            return ParsedProgress(
                current = current,
                max = max,
                isIndeterminate = false,
                percentage = percentage
            )
        }

        return null
    }
}

