package com.tuan.islebridge.core.shizuku

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import rikka.shizuku.Shizuku

enum class ShizukuServiceState {
    NOT_INSTALLED,
    SERVICE_STOPPED,
    UNAUTHORIZED,
    AUTHORIZED
}

data class ShizukuStatus(
    val state: ShizukuServiceState,
    val version: Int,
    val description: String
) {
    val isInstalled: Boolean
        get() = state != ShizukuServiceState.NOT_INSTALLED

    val isRunning: Boolean
        get() = state == ShizukuServiceState.UNAUTHORIZED || state == ShizukuServiceState.AUTHORIZED

    val hasPermission: Boolean
        get() = state == ShizukuServiceState.AUTHORIZED
}

class ShizukuManager(private val context: Context) {

    companion object {
        const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
        const val REQUEST_CODE_SHIZUKU = 1001
        const val SHIZUKU_WEBSITE = "https://shizuku.rikka.app"
    }

    private val _statusFlow = MutableStateFlow(
        ShizukuStatus(
            state = ShizukuServiceState.NOT_INSTALLED,
            version = 0,
            description = "Đang kiểm tra Shizuku..."
        )
    )
    val statusFlow: StateFlow<ShizukuStatus> = _statusFlow.asStateFlow()

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        updateStatus()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        updateStatus()
    }

    private val requestPermissionResultListener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
        updateStatus()
    }

    fun initialize() {
        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)
            updateStatus()
        } catch (_: Throwable) {
            updateStatus()
        }
    }

    fun updateStatus() {
        val isInstalled = isShizukuAppInstalled()

        if (!isInstalled) {
            _statusFlow.value = ShizukuStatus(
                state = ShizukuServiceState.NOT_INSTALLED,
                version = 0,
                description = "Chưa cài đặt Shizuku trên thiết bị."
            )
            return
        }

        var isRunning = false
        var hasPermission = false
        var version = 0

        try {
            if (Shizuku.pingBinder()) {
                isRunning = true
                version = Shizuku.getVersion()
                hasPermission = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            }
        } catch (_: Throwable) {}

        val state = when {
            hasPermission -> ShizukuServiceState.AUTHORIZED
            isRunning -> ShizukuServiceState.UNAUTHORIZED
            else -> ShizukuServiceState.SERVICE_STOPPED
        }

        val description = when (state) {
            ShizukuServiceState.AUTHORIZED -> "Đã cấp quyền Shizuku v$version. Các lệnh chẩn đoán hệ thống đã sẵn sàng."
            ShizukuServiceState.UNAUTHORIZED -> "Dịch vụ Shizuku đang chạy nhưng IsleBridge chưa được cấp quyền."
            ShizukuServiceState.SERVICE_STOPPED -> "Shizuku đã cài đặt nhưng dịch vụ đang tắt. Cần khởi chạy qua Wireless Debugging."
            ShizukuServiceState.NOT_INSTALLED -> "Chưa cài đặt Shizuku."
        }

        _statusFlow.value = ShizukuStatus(
            state = state,
            version = version,
            description = description
        )
    }

    fun requestPermission(requestCode: Int = REQUEST_CODE_SHIZUKU): Boolean {
        return try {
            if (Shizuku.pingBinder()) {
                if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                    updateStatus()
                    true
                } else {
                    Shizuku.requestPermission(requestCode)
                    true
                }
            } else {
                updateStatus()
                false
            }
        } catch (_: Throwable) {
            updateStatus()
            false
        }
    }

    fun openShizukuApp(): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(SHIZUKU_PACKAGE)
        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            true
        } else {
            false
        }
    }

    fun openDownloadPage() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(SHIZUKU_WEBSITE)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Throwable) {}
    }

    fun optimizeDozeWhitelist(): Boolean {
        if (!statusFlow.value.hasPermission) return false
        val pkg = context.packageName
        val res = ShizukuShellExecutor.executeCommand("dumpsys deviceidle whitelist +$pkg")
        return res.isSuccess
    }

    fun isShizukuAppInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo(SHIZUKU_PACKAGE, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun cleanup() {
        try {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(requestPermissionResultListener)
        } catch (_: Throwable) {}
    }
}
