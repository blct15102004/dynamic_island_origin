package com.tuan.islebridge.core.privacy

import android.app.AppOpsManager
import android.content.Context
import android.os.Build
import android.os.Process
import com.tuan.islebridge.core.originisland.CardPriority
import com.tuan.islebridge.core.originisland.IslandCardModel
import com.tuan.islebridge.core.originisland.IslandScene
import com.tuan.islebridge.core.originisland.OriginIslandAdapter
import com.tuan.islebridge.core.shizuku.ShizukuShellExecutor
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PrivacyAccessEvent(
    val type: String, // "CAMERA" or "MICROPHONE"
    val packageName: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class PrivacyCapabilityStatus(
    val isSystemRestricted: Boolean,
    val canMonitorCrossAppViaShizuku: Boolean,
    val description: String
)

class PrivacyMonitor(
    private val context: Context,
    private val islandAdapter: OriginIslandAdapter
) {

    private val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _lastPrivacyEvent = MutableStateFlow<PrivacyAccessEvent?>(null)
    val lastPrivacyEvent: StateFlow<PrivacyAccessEvent?> = _lastPrivacyEvent.asStateFlow()

    fun checkPrivacyCapability(): PrivacyCapabilityStatus {
        val hasShizuku = ShizukuShellExecutor.isShizukuAvailable()
        return if (hasShizuku) {
            PrivacyCapabilityStatus(
                isSystemRestricted = false,
                canMonitorCrossAppViaShizuku = true,
                description = "Shizuku được kích hoạt: Có thể giám sát quyền truy cập Camera/Mic thông qua Shizuku AppOps diagnostics."
            )
        } else {
            PrivacyCapabilityStatus(
                isSystemRestricted = true,
                canMonitorCrossAppViaShizuku = false,
                description = "Giới hạn hệ thống Android: Ứng dụng tiêu chuẩn không thể theo dõi AppOps của ứng dụng khác nếu không có quyền hệ thống hoặc Shizuku. Đèn xanh lá trên thanh trạng thái Android là nguồn chính xác nhất."
            )
        }
    }

    fun pollAppOpsWithShizuku() {
        if (!ShizukuShellExecutor.isShizukuAvailable()) return

        scope.launch {
            try {
                val output = ShizukuShellExecutor.executeCommand("cmd appops query-op --op CAMERA --active")
                if (output.isSuccess && output.stdout.isNotBlank()) {
                    val activePackage = parseActivePackage(output.stdout)
                    if (activePackage != null && activePackage != context.packageName) {
                        notifyPrivacyAlert("CAMERA", activePackage)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun parseActivePackage(cmdOutput: String): String? {
        val lines = cmdOutput.lines()
        for (line in lines) {
            if (line.contains("package", ignoreCase = true) || line.contains("uid", ignoreCase = true)) {
                val parts = line.split(" ")
                for (p in parts) {
                    if (p.contains(".") && !p.startsWith("android.")) {
                        return p.trim()
                    }
                }
            }
        }
        return null
    }

    private fun notifyPrivacyAlert(type: String, packageName: String) {
        val event = PrivacyAccessEvent(type, packageName)
        _lastPrivacyEvent.value = event

        val title = if (type == "CAMERA") "Camera đang hoạt động" else "Microphone đang hoạt động"
        val card = IslandCardModel(
            id = "privacy_${type}_${packageName}",
            sourcePackage = packageName,
            scene = IslandScene.PRIVACY,
            title = title,
            subtitle = "Ứng dụng: $packageName",
            capsuleText = type,
            capsuleSubtext = "Active",
            priority = CardPriority.URGENT,
            timeoutMs = 6000L
        )
        islandAdapter.displayCard(card)
    }

    fun destroy() {
        scope.cancel()
    }
}

