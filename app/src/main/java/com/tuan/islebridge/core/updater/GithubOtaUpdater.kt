package com.tuan.islebridge.core.updater

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.tuan.islebridge.BuildConfig
import com.tuan.islebridge.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class GithubOtaUpdater(private val context: Context) {

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    suspend fun checkForUpdates(
        owner: String = BuildConfig.GITHUB_OWNER,
        repo: String = BuildConfig.GITHUB_REPO
    ): UpdateCheckResult = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.github.com/repos/$owner/$repo/releases/latest")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                connectTimeout = 10000
                readTimeout = 10000
            }

            if (connection.responseCode != 200) {
                return@withContext UpdateCheckResult.Error("HTTP Error: ${connection.responseCode}")
            }

            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseText)

            val tagName = json.getString("tag_name")
            val releaseNotes = json.optString("body", "Không có ghi chú phát hành.")
            val publishedAt = json.optString("published_at", "")

            // Parse version code from tag name (e.g. "v1.0.1" -> 101 or tag format)
            val cleanVersion = tagName.removePrefix("v")
            val remoteVersionCode = parseVersionCode(cleanVersion)
            val currentVersionCode = BuildConfig.VERSION_CODE

            if (remoteVersionCode <= currentVersionCode) {
                return@withContext UpdateCheckResult.UpToDate
            }

            // Find APK asset
            val assets = json.getJSONArray("assets")
            var apkUrl: String? = null
            var fileSize = 0L
            var apkSha256: String? = null

            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val name = asset.getString("name")
                if (name.endsWith(".apk", ignoreCase = true)) {
                    apkUrl = asset.getString("browser_download_url")
                    fileSize = asset.getLong("size")
                } else if (name.endsWith(".sha256", ignoreCase = true)) {
                    // Optional sha256 asset
                    val shaUrl = asset.getString("browser_download_url")
                    apkSha256 = fetchSha256(shaUrl)
                }
            }

            if (apkUrl == null) {
                return@withContext UpdateCheckResult.Error("Không tìm thấy file APK trong bản phát hành.")
            }

            val releaseInfo = ReleaseInfo(
                tagName = tagName,
                versionName = cleanVersion,
                versionCode = remoteVersionCode,
                releaseNotes = releaseNotes,
                downloadUrl = apkUrl,
                apkSha256 = apkSha256,
                fileSize = fileSize,
                publishedAt = publishedAt
            )

            UpdateCheckResult.UpdateAvailable(releaseInfo)
        } catch (e: Exception) {
            UpdateCheckResult.Error(e.message ?: "Kiểm tra cập nhật thất bại")
        }
    }

    suspend fun downloadAndVerifyApk(release: ReleaseInfo) = withContext(Dispatchers.IO) {
        try {
            _downloadState.value = DownloadState.Downloading(0, 0, release.fileSize)

            val updateDir = File(context.cacheDir, "updates").apply { mkdirs() }
            val apkFile = File(updateDir, "islebridge_update_${release.versionName}.apk")
            if (apkFile.exists()) apkFile.delete()

            val url = URL(release.downloadUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 30000
            }

            var downloadedBytes = 0L
            val totalBytes = if (release.fileSize > 0) release.fileSize else connection.contentLengthLong

            connection.inputStream.use { input ->
                FileOutputStream(apkFile).use { output ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloadedBytes += read
                        val percent = if (totalBytes > 0) ((downloadedBytes * 100) / totalBytes).toInt() else 0
                        _downloadState.value = DownloadState.Downloading(percent, downloadedBytes, totalBytes)
                    }
                }
            }

            // Verification phase
            _downloadState.value = DownloadState.Verifying("Đang xác thực tính toàn vẹn và chữ ký bảo mật...")

            // 1. Verify SHA-256 if available
            if (!release.apkSha256.isNullOrBlank()) {
                val isShaValid = SecurityVerifier.verifySha256(apkFile, release.apkSha256)
                if (!isShaValid) {
                    apkFile.delete()
                    _downloadState.value = DownloadState.Failed("Xác thực SHA-256 thất bại: File tải về có thể đã bị can thiệp.")
                    return@withContext
                }
            }

            // 2. Verify Package Identity
            val isPackageValid = SecurityVerifier.verifyPackageIdentity(context, apkFile, "com.tuan.islebridge")
            if (!isPackageValid) {
                apkFile.delete()
                _downloadState.value = DownloadState.Failed("Xác thực Application ID thất bại: Gói không khớp với com.tuan.islebridge")
                return@withContext
            }

            // 3. Verify Signature Certificate
            val isCertValid = SecurityVerifier.verifyApkCertificate(context, apkFile, BuildConfig.EXPECTED_SIGNING_CERT_SHA256)
            if (!isCertValid) {
                apkFile.delete()
                _downloadState.value = DownloadState.Failed("Xác thực chứng chỉ bảo mật thất bại: Chữ ký không khớp với nhà phát hành đáng tin cậy.")
                return@withContext
            }

            _downloadState.value = DownloadState.ReadyToInstall(apkFile.absolutePath)
        } catch (e: Exception) {
            _downloadState.value = DownloadState.Failed(e.message ?: "Tải bản cập nhật thất bại")
        }
    }

    fun installApk(apkPath: String) {
        val apkFile = File(apkPath)
        if (!apkFile.exists()) return

        try {
            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            _downloadState.value = DownloadState.Failed("Khởi chạy bộ cài đặt hệ thống thất bại: ${e.message}")
        }
    }

    private fun parseVersionCode(versionName: String): Int {
        return try {
            val parts = versionName.split(".").map { it.toIntOrNull() ?: 0 }
            when (parts.size) {
                1 -> parts[0]
                2 -> parts[0] * 100 + parts[1]
                3 -> parts[0] * 10000 + parts[1] * 100 + parts[2]
                else -> 1
            }
        } catch (_: Exception) {
            1
        }
    }

    private fun fetchSha256(urlStr: String): String? {
        return try {
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.inputStream.bufferedReader().use { reader ->
                reader.readLine()?.split(" ")?.firstOrNull()?.trim()
            }
        } catch (_: Exception) {
            null
        }
    }
}

