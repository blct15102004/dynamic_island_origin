package com.tuan.islebridge.core.originisland

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.tuan.islebridge.core.common.CompatibilityDetector
import com.tuan.islebridge.core.common.Constants

class OriginIslandAdapter(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val mainHandler = Handler(Looper.getMainLooper())
    val priorityQueue = IslandPriorityQueue()

    private val activeTestEventIds = mutableSetOf<String>()

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    Constants.CHANNEL_ISLAND_NOTIFICATION,
                    "Origin Island Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Channel for Origin Island notifications"
                    setShowBadge(false)
                },
                NotificationChannel(
                    Constants.CHANNEL_BLUETOOTH,
                    "Bluetooth Status",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Channel for Bluetooth earbuds monitoring"
                    setShowBadge(false)
                },
                NotificationChannel(
                    Constants.CHANNEL_OTP,
                    "OTP Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Channel for One-Time Password alerts"
                    setShowBadge(true)
                },
                NotificationChannel(
                    Constants.CHANNEL_PRIVACY,
                    "Privacy Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Channel for Camera and Mic privacy alerts"
                    setShowBadge(false)
                }
            )
            channels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }

    fun displayCard(
        card: IslandCardModel,
        channelId: String = Constants.CHANNEL_ISLAND_NOTIFICATION,
        forceNative: Boolean = false,
        isTestEvent: Boolean = false
    ) {
        priorityQueue.postOrUpdate(card)

        if (isTestEvent) {
            activeTestEventIds.add(card.id)
        }

        val templateId = when {
            card.progress != null -> Constants.SuperXProtocol.TEMPLATE_PROGRESS
            card.capsuleSubtext != null -> Constants.SuperXProtocol.TEMPLATE_BASE_INFO
            else -> Constants.SuperXProtocol.TEMPLATE_CAPSULE_ONLY
        }

        val baseInfoList = card.extraDetails.map { BaseInfoItem(it.key, it.value) }

        val payload = SuperIslandPayload(
            eventId = card.id,
            operation = SuperIslandOperation.MOUNT,
            templateId = templateId,
            scene = card.scene,
            title = card.title,
            content = card.subtitle,
            capsule = CapsulePayload(
                text = card.capsuleText,
                subText = card.capsuleSubtext
            ),
            showNotify = true,
            isIsland = true,
            progress = card.progress,
            maxProgress = card.maxProgress,
            baseInfos = baseInfoList,
            shortInfos = "${card.title}: ${card.subtitle}",
            timeoutMs = if (isTestEvent) Constants.DEFAULT_TIMEOUT_TEST_MS else card.timeoutMs
        )

        var action: NotificationCompat.Action? = null
        if (card.actionLabel != null && card.actionIntent != null) {
            action = NotificationCompat.Action.Builder(
                android.R.drawable.ic_menu_send,
                card.actionLabel,
                card.actionIntent
            ).build()
        }

        // Post notification (with Super Island protocol extras)
        // Test events are strictly NEVER ongoing so they do not get stuck
        val isOngoing = !isTestEvent && card.priority != CardPriority.LOW
        val notification = SuperIslandPayloadBuilder.buildNotification(
            context = context,
            channelId = channelId,
            payload = payload,
            contentIntent = card.actionIntent,
            action = action,
            isOngoing = isOngoing
        )

        val notificationId = card.id.hashCode()
        notificationManager.notify(notificationId, notification)

        // If test event, automatically schedule unmount after 3 seconds as required by specification
        if (isTestEvent) {
            mainHandler.postDelayed({
                unmountCard(card.id, card.scene, channelId)
                activeTestEventIds.remove(card.id)
            }, Constants.DEFAULT_TIMEOUT_TEST_MS)
        }
    }

    fun unmountCard(
        cardId: String,
        scene: IslandScene = IslandScene.SYSTEM,
        channelId: String = Constants.CHANNEL_ISLAND_NOTIFICATION
    ) {
        priorityQueue.remove(cardId)
        activeTestEventIds.remove(cardId)
        val notificationId = cardId.hashCode()

        // Send unmount payload so OriginOS Island dismisses the card smoothly
        val unmountNotification = SuperIslandPayloadBuilder.buildUnmountNotification(
            context = context,
            channelId = channelId,
            eventId = cardId,
            scene = scene
        )
        try {
            notificationManager.notify(notificationId, unmountNotification)
        } catch (_: Throwable) {}
        try {
            notificationManager.cancel(notificationId)
        } catch (_: Throwable) {}
    }

    /**
     * Unmounts ONLY IsleBridge test cards and cancels test notifications.
     * Returns the count of removed active test events.
     */
    fun unmountAllTestCards(): Int {
        val testCards = activeTestEventIds.toList()
        var count = 0

        for (testId in testCards) {
            unmountCard(testId)
            count++
        }

        // Also clean any known test IDs just in case
        val standardTestIds = listOf("test_media", "test_download", "test_otp", "test_alert")
        for (stdId in standardTestIds) {
            val notifId = stdId.hashCode()
            try {
                notificationManager.cancel(notifId)
            } catch (_: Throwable) {}
            priorityQueue.remove(stdId)
        }

        activeTestEventIds.clear()
        return count
    }

    fun cleanExpiredCards(channelId: String = Constants.CHANNEL_ISLAND_NOTIFICATION) {
        val expired = priorityQueue.cleanExpiredCards()
        for (card in expired) {
            unmountCard(card.id, card.scene, channelId)
        }
    }
}
