package com.tuan.islebridge.core.shizuku

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import rikka.shizuku.Shizuku

data class ShizukuStatus(
    val isInstalled: Boolean,
    val isRunning: Boolean,
    val hasPermission: Boolean,
    val version: Int
)

class ShizukuManager(private val context: Context) {

    private val _statusFlow = MutableStateFlow(
        ShizukuStatus(
            isInstalled = false,
            isRunning = false,
            hasPermission = false,
            version = 0
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

        _statusFlow.value = ShizukuStatus(
            isInstalled = isInstalled,
            isRunning = isRunning,
            hasPermission = hasPermission,
            version = version
        )
    }

    fun requestPermission(requestCode: Int = 1001) {
        try {
            if (Shizuku.pingBinder() && !statusFlow.value.hasPermission) {
                Shizuku.requestPermission(requestCode)
            }
        } catch (_: Throwable) {}
    }

    fun optimizeDozeWhitelist(): Boolean {
        if (!statusFlow.value.hasPermission) return false
        val pkg = context.packageName
        val res = ShizukuShellExecutor.executeCommand("dumpsys deviceidle whitelist +$pkg")
        return res.isSuccess
    }

    private fun isShizukuAppInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo("moe.shizuku.privileged.api", 0)
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

