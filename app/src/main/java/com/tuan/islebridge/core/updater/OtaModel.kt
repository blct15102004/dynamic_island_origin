package com.tuan.islebridge.core.updater

data class ReleaseInfo(
    val tagName: String,
    val versionName: String,
    val versionCode: Int,
    val releaseNotes: String,
    val downloadUrl: String,
    val apkSha256: String?,
    val fileSize: Long,
    val publishedAt: String
)

sealed class UpdateCheckResult {
    data class UpdateAvailable(val release: ReleaseInfo) : UpdateCheckResult()
    data object UpToDate : UpdateCheckResult()
    data class Error(val message: String) : UpdateCheckResult()
}

sealed class DownloadState {
    data object Idle : DownloadState()
    data class Downloading(val progressPercent: Int, val bytesDownloaded: Long, val totalBytes: Long) : DownloadState()
    data class Verifying(val message: String) : DownloadState()
    data class ReadyToInstall(val apkPath: String) : DownloadState()
    data class Failed(val reason: String) : DownloadState()
}

