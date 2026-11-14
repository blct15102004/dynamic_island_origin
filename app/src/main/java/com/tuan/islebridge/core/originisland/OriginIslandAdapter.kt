package com.tuan.islebridge.core.originisland

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.tuan.islebridge.core.common.CompatibilityDetector
import com.tuan.islebridge.core.common.Constants
import com.tuan.islebridge.core.common.OriginOsCompatibilityStatus
import com.tuan.islebridge.core.overlay.OverlayService

class OriginIslandAdapter(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val priorityQueue = IslandPriorityQueue()

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
                    description = "Channal for Super Island and Island notifications"
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
        useOverlay: Boolean = false
    ) {
        priorityQueue.postOrUpdate(card)

        val compatibility = CompatibilityDetector.detectCompatibility()
        val isNativeSupported = forceNative ||
                compatibility.status == OriginOsCompatibilityStatus.NATIVE_ORIGIN_OS_6_SUPPORTED ||
                compatibility.status == OriginOsCompatibilityStatus.NATIVE_ORIGIN_OS_7_EXPERIMENTAL

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
            timeoutMs = card.timeoutMs
        )

        var action: NotificationCompat.Action? = null
        if (card.actionLabel != null && card.actionIntent != null) {
            action = NotificationCompat.Action.Builder(
                android.R.drawable.ic_menu_send,
                card.actionLabel,
                card.actionIntent
            ).build()
        }

        // Post notification (which includes Super Island protocol extras)
        val notification = SuperIslandPayloadBuilder.buildNotification(
            context = context,
            channelId = channelId,
            payload = payload,
            contentIntent = card.actionIntent,
            action = action
        )

        val notificationId = card.id.hashCode()
        notificationManager.notify(notificationId, notification)

        // Overlay fallback handling
        if (useOverlay || (!isNativeSupported && useOverlay)) {
            OverlayService.showCard(context, card)
        }
    }

    fun unmountCard(
        cardId: String,
        scene: IslandScene = IslandScene.SYSTEM,
        channelId: String = Constants.CHANNEL_ISLAND_NOTIFICATION
    ) {
        val removed = priorityQueue.remove(cardId)
        val notificationId = cardId.hashCode()

        // Send unmount payload so OriginOS Island dismisses the card smoothly
        val unmountNotification = SuperIslandPayloadBuilder.buildUnmountNotification(
            context = context,
            channelId = channelId,
            eventId = cardId,
            scene = scene
        )
        notificationManager.notify(notificationId, unmountNotification)
        notificationManager.cancel(notificationId)

        OverlayService.dismissCard(context, cardId)
    }

    fun cleanExpiredCards(channelId: String = Constants.CHANNEL_ISLAND_NOTIFICATION) {
        val expired = priorityQueue.cleanExpiredCards()
        for (card in expired) {
            unmountCard(card.id, card.scene, channelId)
        }
    }
}

