package com.tuan.islebridge.core.common

import android.os.Build
import java.io.BufferedReader
import java.io.InputStreamReader

enum class DetectionConfidence {
    CONFIRMED,
    ESTIMATED,
    UNKNOWN
}

enum class OriginOsCompatibilityStatus {
    NATIVE_ORIGIN_OS_SUPPORTED,
    NATIVE_ORIGIN_OS_EXPERIMENTAL,
    VIVO_LEGACY_OR_UNCONFIRMED,
    NON_VIVO_DEVICE
}

data class DeviceCompatibilityInfo(
    val manufacturer: String,
    val brand: String,
    val model: String,
    val deviceCodename: String,
    val product: String,
    val androidSdk: Int,
    val androidRelease: String,
    val buildDisplay: String,
    val buildFingerprint: String,
    val originOsVersion: String?,
    val originOsBuildNumber: String?,
    val isVivoDevice: Boolean,
    val status: OriginOsCompatibilityStatus,
    val confidence: DetectionConfidence,
    val statusDescription: String
) {
    val isSuperIslandSupported: Boolean
        get() = status == OriginOsCompatibilityStatus.NATIVE_ORIGIN_OS_SUPPORTED ||
                status == OriginOsCompatibilityStatus.NATIVE_ORIGIN_OS_EXPERIMENTAL
}

object CompatibilityDetector {

    private val VIVO_PROPERTY_KEYS = listOf(
        "ro.vivo.os.version",
        "ro.vivo.os.build.display.id",
        "ro.vivo.os.name",
        "ro.vivo.rom",
        "ro.vivo.rom.version",
        "ro.build.version.vivo",
        "ro.vivo.product.version",
        "ro.vivo.product.model",
        "ro.product.model"
    )

    fun detectCompatibility(): DeviceCompatibilityInfo {
        val manufacturer = Build.MANUFACTURER.orEmpty()
        val brand = Build.BRAND.orEmpty()
        val model = Build.MODEL.orEmpty()
        val deviceCodename = Build.DEVICE.orEmpty()
        val product = Build.PRODUCT.orEmpty()
        val sdk = Build.VERSION.SDK_INT
        val release = Build.VERSION.RELEASE.orEmpty()
        val display = Build.DISPLAY.orEmpty()
        val fingerprint = Build.FINGERPRINT.orEmpty()

        val isVivo = manufacturer.equals("vivo", ignoreCase = true) ||
                brand.equals("vivo", ignoreCase = true) ||
                manufacturer.equals("iqoo", ignoreCase = true) ||
                brand.equals("iqoo", ignoreCase = true) ||
                fingerprint.contains("vivo", ignoreCase = true) ||
                fingerprint.contains("iqoo", ignoreCase = true)

        // Query system properties via reflection and getprop fallback
        var detectedVersion: String? = null
        var detectedBuildNumber: String? = null

        for (key in VIVO_PROPERTY_KEYS) {
            val value = getSystemProperty(key)
            if (!value.isNullOrBlank()) {
                if (key == "ro.vivo.os.version" || key == "ro.vivo.rom.version" || key == "ro.vivo.os.name") {
                    if (detectedVersion == null) detectedVersion = value
                }
                if (key == "ro.vivo.os.build.display.id" || key == "ro.build.version.vivo") {
                    if (detectedBuildNumber == null) detectedBuildNumber = value
                }
            }
        }

        // Secondary search in build display string if properties were restricted
        if (detectedVersion == null) {
            if (display.contains("OriginOS", ignoreCase = true)) {
                detectedVersion = display
            }
        }

        val (status, confidence, desc) = when {
            !isVivo -> {
                Triple(
                    OriginOsCompatibilityStatus.NON_VIVO_DEVICE,
                    DetectionConfidence.CONFIRMED,
                    "Thiết bị không phải vivo/iQOO ($manufacturer $model). Origin Island chỉ hỗ trợ trên vivo/iQOO chạy OriginOS."
                )
            }

            detectedVersion != null && detectedVersion.contains("OriginOS 7", ignoreCase = true) -> {
                Triple(
                    OriginOsCompatibilityStatus.NATIVE_ORIGIN_OS_EXPERIMENTAL,
                    DetectionConfidence.CONFIRMED,
                    "Phát hiện OriginOS 7 ($detectedVersion): Hỗ trợ Super Island Native ở trạng thái thử nghiệm."
                )
            }

            detectedVersion != null && (
                    detectedVersion.contains("OriginOS 6", ignoreCase = true) ||
                            detectedVersion.contains("OriginOS 5", ignoreCase = true) ||
                            detectedVersion.contains("OriginOS 4", ignoreCase = true) ||
                            detectedVersion.contains("OriginOS", ignoreCase = true)
                    ) -> {
                Triple(
                    OriginOsCompatibilityStatus.NATIVE_ORIGIN_OS_SUPPORTED,
                    DetectionConfidence.CONFIRMED,
                    "Xác nhận OriginOS ($detectedVersion): Thiết bị hỗ trợ đầy đủ giao thức Super Island gốc."
                )
            }

            // Modern vivo/iQOO devices (Android 14+, e.g. V2403A on Android 16/15) where property access was restricted
            isVivo && sdk >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                Triple(
                    OriginOsCompatibilityStatus.NATIVE_ORIGIN_OS_SUPPORTED,
                    DetectionConfidence.ESTIMATED,
                    "Thiết bị $manufacturer $model chạy Android $release (API $sdk). Tương thích với nền tảng OriginOS hiện đại và Super Island."
                )
            }

            isVivo && sdk >= Build.VERSION_CODES.TIRAMISU -> {
                Triple(
                    OriginOsCompatibilityStatus.NATIVE_ORIGIN_OS_SUPPORTED,
                    DetectionConfidence.ESTIMATED,
                    "Thiết bị $manufacturer $model (Android $release). Đủ điều kiện hỗ trợ Super Island native."
                )
            }

            else -> {
                Triple(
                    OriginOsCompatibilityStatus.VIVO_LEGACY_OR_UNCONFIRMED,
                    DetectionConfidence.ESTIMATED,
                    "Thiết bị $manufacturer $model (Android $release). Chưa xác nhận phiên bản OriginOS tương thích."
                )
            }
        }

        return DeviceCompatibilityInfo(
            manufacturer = manufacturer,
            brand = brand,
            model = model,
            deviceCodename = deviceCodename,
            product = product,
            androidSdk = sdk,
            androidRelease = release,
            buildDisplay = display,
            buildFingerprint = fingerprint,
            originOsVersion = detectedVersion,
            originOsBuildNumber = detectedBuildNumber,
            isVivoDevice = isVivo,
            status = status,
            confidence = confidence,
            statusDescription = desc
        )
    }

    private fun getSystemProperty(key: String): String? {
        // Reflection on android.os.SystemProperties
        try {
            val c = Class.forName("android.os.SystemProperties")
            val getMethod = c.getMethod("get", String::class.java)
            val value = getMethod.invoke(null, key) as? String
            if (!value.isNullOrBlank()) return value.trim()
        } catch (_: Throwable) {}

        // Fallback: getprop CLI
        try {
            val process = Runtime.getRuntime().exec(arrayOf("getprop", key))
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                val line = reader.readLine()?.trim()
                if (!line.isNullOrBlank()) return line
            }
        } catch (_: Throwable) {}

        return null
    }
}
