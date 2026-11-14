package com.tuan.islebridge.core.otp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper

object ClipboardHelper {

    fun copyOtpToClipboard(
        context: Context,
        otp: String,
        clearTimeoutMs: Long = 30000L
    ) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("IsleBridge OTP", otp)

        // On Android 13+ (API 33), mark sensitive content to prevent preview
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            clip.description.extras = android.os.PersistableBundle().apply {
                putBoolean("android.content.extra.IS_SENSITIVE", true)
            }
        }

        clipboard.setPrimaryClip(clip)

        // Clear clipboard automatically after timeout for security
        if (clearTimeoutMs > 0) {
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    val currentClip = clipboard.primaryClip
                    if (currentClip != null && currentClip.itemCount > 0) {
                        val currentText = currentClip.getItemAt(0)?.text?.toString()
                        if (currentText == otp) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                clipboard.clearPrimaryClip()
                            } else {
                                clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
                            }
                        }
                    }
                } catch (_: Exception) {}
            }, clearTimeoutMs)
        }
    }
}

