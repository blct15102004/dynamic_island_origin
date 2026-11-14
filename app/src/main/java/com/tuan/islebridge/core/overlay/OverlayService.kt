package com.tuan.islebridge.core.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.tuan.islebridge.R
import com.tuan.islebridge.core.common.Constants
import com.tuan.islebridge.core.originisland.IslandCardModel

class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: IslandOverlayView? = null
    private var isOverlayAttached = false

    companion object {
        private const val NOTIFICATION_ID = 9001
        private const val ACTION_SHOW_CARD = "ACTION_SHOW_CARD"
        private const val ACTION_DISMISS_CARD = "ACTION_DISMISS_CARD"

        private var activeCard: IslandCardModel? = null

        fun showCard(context: Context, card: IslandCardModel) {
            if (!Settings.canDrawOverlays(context)) return
            activeCard = card
            val intent = Intent(context, OverlayService::class.java).apply {
                action = ACTION_SHOW_CARD
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun dismissCard(context: Context, cardId: String) {
            if (activeCard?.id == cardId) {
                activeCard = null
                val intent = Intent(context, OverlayService::class.java).apply {
                    action = ACTION_DISMISS_CARD
                }
                context.startService(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        startForeground(NOTIFICATION_ID, createForegroundNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW_CARD -> {
                activeCard?.let { showOverlay(it) }
            }
            ACTION_DISMISS_CARD -> {
                hideOverlay()
            }
        }
        return START_NOT_STICKY
    }

    private fun showOverlay(card: IslandCardModel) {
        if (!Settings.canDrawOverlays(this)) return

        if (overlayView == null) {
            overlayView = IslandOverlayView(this)
        }

        overlayView?.bind(card)

        if (!isOverlayAttached) {
            val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                y = 30 // Distance below status bar/punch hole
            }

            try {
                windowManager?.addView(overlayView, params)
                isOverlayAttached = true
            } catch (_: Exception) {}
        }
    }

    private fun hideOverlay() {
        if (isOverlayAttached && overlayView != null) {
            try {
                windowManager?.removeView(overlayView)
            } catch (_: Exception) {}
            isOverlayAttached = false
        }
    }

    private fun createForegroundNotification(): Notification {
        val channelId = Constants.CHANNEL_OVERLAY_SERVICE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "IsleBridge Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("IsleBridge Overlay")
            .setContentText("Overlay Fallback đang hoạt động")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        hideOverlay()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

