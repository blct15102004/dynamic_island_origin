package com.tuan.islebridge.core.notifications

import android.app.Notification
import android.os.Bundle
import android.service.notification.StatusBarNotification

data class ParsedNotification(
    val key: String,
    val packageName: String,
    val postTime: Long,
    val title: String,
    val content: String,
    val subText: String?,
    val isOngoing: Boolean,
    val progress: ParsedProgress?
)

object NotificationParser {

    fun parse(sbn: StatusBarNotification): ParsedNotification? {
        val notification = sbn.notification ?: return null
        val extras: Bundle = notification.extras ?: Bundle()

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()
            ?: ""

        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
            ?: ""

        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
        val isOngoing = (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0

        val progress = NotificationProgressExtractor.extractProgress(notification)

        if (title.isBlank() && text.isBlank()) {
            return null
        }

        return ParsedNotification(
            key = sbn.key,
            packageName = sbn.packageName,
            postTime = sbn.postTime,
            title = title,
            content = text,
            subText = subText,
            isOngoing = isOngoing,
            progress = progress
        )
    }
}

