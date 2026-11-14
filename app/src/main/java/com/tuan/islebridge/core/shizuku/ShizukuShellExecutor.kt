package com.tuan.islebridge.core.shizuku

import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

data class ShellResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String
) {
    val isSuccess: Boolean get() = exitCode == 0
}

object ShizukuShellExecutor {

    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder() && Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
        } catch (_: Throwable) {
            false
        }
    }

    fun executeCommand(command: String): ShellResult {
        return try {
            if (isShizukuAvailable()) {
                val method = Shizuku::class.java.getMethod("newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java)
                val process = method.invoke(null, arrayOf("sh", "-c", command), null, null) as Process
                val stdout = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
                val stderr = BufferedReader(InputStreamReader(process.errorStream)).use { it.readText() }
                val code = process.waitFor()
                ShellResult(code, stdout.trim(), stderr.trim())
            } else {
                val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
                val stdout = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
                val stderr = BufferedReader(InputStreamReader(process.errorStream)).use { it.readText() }
                val code = process.waitFor()
                ShellResult(code, stdout.trim(), stderr.trim())
            }
        } catch (e: Exception) {
            ShellResult(-1, "", e.message ?: "Execution failed")
        }
    }
}

