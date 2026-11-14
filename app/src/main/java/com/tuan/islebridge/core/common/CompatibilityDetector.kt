package com.tuan.islebridge.core.common

import android.os.Build
import java.io.BufferedReader
import java.io.InputStreamReader

enum class OriginOsCompatibilityStatus {
    NATIVE_ORIGIN_OS_6_SUPPORTED,
    NATIVE_ORIGIN_OS_7_EXPERIMENTAL,
    VIVO_LEGACY_UNSUPPORTED,
    NON_VIVO_FALLBACK
}

data class DeviceCompatibilityInfo(
    val manufacturer: String,
    val brand: String,
    val model: String,
    val androidSdk: Int,
    val androidRelease: String,
    val originOsVersion: String?,
    val isVivoDevice: Boolean,
    val status: OriginOsCompatibilityStatus,
    val statusDescription: String
)

object CompatibilityDetector {

    fun detectCompatibility(): DeviceCompatibilityInfo {
        val manufacturer = Build.MANUFACTURER.orEmpty()
        val brand = Build.BRAND.orEmpty()
        val model = Build.MODEL.orEmpty()
        val sdk = Build.VERSION.SDK_INT
        val release = Build.VERSION.RELEASE.orEmpty()

        val isVivo = manufacturer.equals("vivo", ignoreCase = true) ||
                brand.equals("vivo", ignoreCase = true) ||
                manufacturer.equals("iqoo", ignoreCase = true) ||
                brand.equals("iqoo", ignoreCase = true)

        val osVersionRaw = getSystemProperty("ro.vivo.os.version")
            ?.takeIf { it.isNotBlank() }
            ?: getSystemProperty("ro.vivo.os.build.display.id")
            ?.takeIf { it.isNotBlank() }
            ?: getSystemProperty("ro.vivo.product.version")

        val (status, desc) = if (!isVivo) {
            OriginOsCompatibilityStatus.NON_VIVO_FALLBACK to
                    "Thiết bị không phải vivo/iQOO. Kích hoạt chế độ Native Notification & Custom Overlay Fallback."
        } else {
            when {
                osVersionRaw != null && osVersionRaw.contains("OriginOS 6", ignoreCase = true) -> {
                    OriginOsCompatibilityStatus.NATIVE_ORIGIN_OS_6_SUPPORTED to
                            "Phát hiện OriginOS 6: Hỗ trợ đầy đủ native Super Island protocol."
                }
                osVersionRaw != null && osVersionRaw.contains("OriginOS 7", ignoreCase = true) -> {
                    OriginOsCompatibilityStatus.NATIVE_ORIGIN_OS_7_EXPERIMENTAL to
                            "Phát hiện OriginOS 7: Tính năng Origin Island đang ở trạng thái thử nghiệm (Experimental)."
                }
                osVersionRaw != null && (osVersionRaw.contains("OriginOS", ignoreCase = true) || osVersionRaw.startsWith("6.")) -> {
                    OriginOsCompatibilityStatus.NATIVE_ORIGIN_OS_6_SUPPORTED to
                            "Phát hiện OriginOS: Kích hoạt native Super Island integration."
                }
                else -> {
                    OriginOsCompatibilityStatus.VIVO_LEGACY_UNSUPPORTED to
                            "Thiết bị vivo nhưng không xác định rõ OriginOS 6/7. Hỗ trợ qua Fallback Overlay và Android Notifications."
                }
            }
        }

        return DeviceCompatibilityInfo(
            manufacturer = manufacturer,
            brand = brand,
            model = model,
            androidSdk = sdk,
            androidRelease = release,
            originOsVersion = osVersionRaw,
            isVivoDevice = isVivo,
            status = status,
            statusDescription = desc
        )
    }

    private fun getSystemProperty(key: String): String? {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("getprop", key))
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                reader.readLine()?.trim()
            }
        } catch (_: Exception) {
            null
        }
    }
}

