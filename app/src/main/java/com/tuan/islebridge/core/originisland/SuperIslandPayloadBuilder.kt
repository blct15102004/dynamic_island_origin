package com.tuan.islebridge.core.originisland

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.tuan.islebridge.R
import com.tuan.islebridge.core.common.Constants

object SuperIslandPayloadBuilder {

    fun buildNotificationExtras(payload: SuperIslandPayload): Bundle {
        return Bundle().apply {
            putInt(Constants.SuperXProtocol.KEY_OPERATION, payload.operation.code)
            putBoolean(Constants.SuperXProtocol.KEY_SHOW_NOTIFY, payload.showNotify)
            putInt(Constants.SuperXProtocol.KEY_TEMPLATE, payload.templateId)
            putString(Constants.SuperXProtocol.KEY_SCENE, payload.scene.sceneName)
            putBoolean(Constants.SuperXProtocol.KEY_ISLAND, payload.isIsland)
            putString(Constants.SuperXProtocol.KEY_SHORT_INFOS, payload.shortInfos)

            // Capsule Bundle
            val capsuleBundle = Bundle().apply {
                putString("text", payload.capsule.text)
                payload.capsule.subText?.let { putString("subText", it) }
                payload.capsule.iconResId?.let { putInt("iconResId", it) }
                payload.capsule.accentColor?.let { putInt("accentColor", it) }
            }
            putBundle(Constants.SuperXProtocol.KEY_CAPSULE, capsuleBundle)

            // Base Infos
            if (payload.baseInfos.isNotEmpty()) {
                val baseInfosBundle = Bundle().apply {
                    payload.baseInfos.forEach { item ->
                        putString(item.key, item.value)
                    }
                }
                putBundle(Constants.SuperXProtocol.KEY_BASE_INFOS, baseInfosBundle)
            }

            // Info lines
            if (payload.infos.isNotEmpty()) {
                putStringArrayList(Constants.SuperXProtocol.KEY_INFOS, ArrayList(payload.infos))
            }
        }
    }

    fun buildNotification(
        context: Context,
        channelId: String,
        payload: SuperIslandPayload,
        contentIntent: PendingIntent? = null,
        action: NotificationCompat.Action? = null
    ): Notification {
        val extras = buildNotificationExtras(payload)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(payload.title)
            .setContentText(payload.content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setOngoing(payload.operation != SuperIslandOperation.UNMOUNT)
            .setAutoCancel(false)
            .addExtras(extras)

        if (payload.progress != null && payload.maxProgress != null) {
            builder.setProgress(payload.maxProgress, payload.progress, false)
        }

        contentIntent?.let { builder.setContentIntent(it) }
        action?.let { builder.addAction(it) }

        return builder.build()
    }

    fun buildUnmountNotification(
        context: Context,
        channelId: String,
        eventId: String,
        scene: IslandScene
    ): Notification {
        val unmountPayload = SuperIslandPayload(
            eventId = eventId,
            operation = SuperIslandOperation.UNMOUNT,
            scene = scene,
            title = "",
            content = "",
            capsule = CapsulePayload(text = ""),
            showNotify = false,
            isIsland = false
        )
        return buildNotification(context, channelId, unmountPayload)
    }
}

