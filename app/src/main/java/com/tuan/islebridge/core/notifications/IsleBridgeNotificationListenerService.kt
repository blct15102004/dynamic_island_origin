package com.tuan.islebridge.core.notifications

import android.app.PendingIntent
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.tuan.islebridge.MainActivity
import com.tuan.islebridge.core.common.Constants
import com.tuan.islebridge.core.datastore.UserPreferencesRepository
import com.tuan.islebridge.core.originisland.CardPriority
import com.tuan.islebridge.core.originisland.IslandCardModel
import com.tuan.islebridge.core.originisland.IslandScene
import com.tuan.islebridge.core.originisland.OriginIslandAdapter
import com.tuan.islebridge.core.otp.ClipboardHelper
import com.tuan.islebridge.core.otp.OtpDetectorEngine
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class IsleBridgeNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var islandAdapter: OriginIslandAdapter
    private lateinit var preferencesRepo: UserPreferencesRepository

    companion object {
        var isConnected: Boolean = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        islandAdapter = OriginIslandAdapter(applicationContext)
        preferencesRepo = UserPreferencesRepository(applicationContext)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isConnected = true
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isConnected = false
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return

        // 1. Hard block self to avoid infinite loops
        if (sbn.packageName == applicationContext.packageName) return

        serviceScope.launch {
            val prefs = preferencesRepo.preferencesFlow.first()
            if (!prefs.isIslandEnabled) return@launch

            val parsed = NotificationParser.parse(sbn) ?: return@launch

            // 2. Check for OTP first if enabled
            if (prefs.isOtpDetectionEnabled) {
                val detectedOtp = OtpDetectorEngine.extractOtp(
                    sourcePackage = parsed.packageName,
                    title = parsed.title,
                    content = parsed.content
                )

                if (detectedOtp != null) {
                    handleDetectedOtp(parsed, detectedOtp, prefs.isOtpAutoCopyEnabled, prefs.otpTimeoutMs)
                    return@launch
                }
            }

            // 3. Application notification mirroring
            if (!prefs.isNotificationMirroringEnabled) return@launch

            val isAllowed = AppFilterManager.isAllowedForMirroring(
                packageName = parsed.packageName,
                allowedPackages = prefs.allowedPackages,
                blockedPackages = prefs.blockedPackages
            )
            if (!isAllowed) return@launch

            // Build Island Card
            val isProgress = parsed.progress != null
            val card = IslandCardModel(
                id = "notif_${sbn.key.hashCode()}",
                sourcePackage = parsed.packageName,
                scene = if (isProgress) IslandScene.PROGRESS else IslandScene.APP_MIRROR,
                title = parsed.title.ifBlank { parsed.packageName },
                subtitle = parsed.content,
                capsuleText = parsed.title.take(12),
                capsuleSubtext = if (isProgress) "${parsed.progress?.percentage}%" else null,
                progress = parsed.progress?.current,
                maxProgress = parsed.progress?.max,
                isIndeterminate = parsed.progress?.isIndeterminate ?: false,
                priority = if (isProgress) CardPriority.HIGH else CardPriority.NORMAL,
                timeoutMs = Constants.DEFAULT_TIMEOUT_NOTIF_MS
            )

            islandAdapter.displayCard(
                card = card,
                forceNative = prefs.forceNativeIsland,
                useOverlay = prefs.isOverlayFallbackEnabled
            )
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn ?: return
        if (sbn.packageName == applicationContext.packageName) return

        val cardId = "notif_${sbn.key.hashCode()}"
        islandAdapter.unmountCard(cardId, IslandScene.APP_MIRROR)
    }

    private fun handleDetectedOtp(
        parsed: ParsedNotification,
        otp: String,
        autoCopy: Boolean,
        timeoutMs: Long
    ) {
        val otpEvent = OtpDetectorEngine.recordOtpEvent(
            sourcePackage = parsed.packageName,
            code = otp,
            sender = parsed.title.ifBlank { "Tin nhắn OTP" },
            timeoutMs = timeoutMs
        )

        if (autoCopy) {
            ClipboardHelper.copyOtpToClipboard(applicationContext, otp, timeoutMs)
        }

        // Create copy pending intent
        val copyIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("EXTRA_ACTION_COPY_OTP", otp)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            otp.hashCode(),
            copyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val otpCard = IslandCardModel(
            id = "otp_${otpEvent.id}",
            sourcePackage = parsed.packageName,
            scene = IslandScene.OTP,
            title = "Mã xác thực: $otp",
            subtitle = "${parsed.title} - Chạm để sao chép",
            capsuleText = "OTP: $otp",
            capsuleSubtext = "Sao chép",
            priority = CardPriority.URGENT,
            timeoutMs = timeoutMs,
            actionLabel = "Sao chép",
            actionIntent = pendingIntent
        )

        islandAdapter.displayCard(
            card = otpCard,
            channelId = Constants.CHANNEL_OTP
        )
    }
}

