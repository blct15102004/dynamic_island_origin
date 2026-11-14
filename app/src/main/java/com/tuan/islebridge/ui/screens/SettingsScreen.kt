package com.tuan.islebridge.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuan.islebridge.BuildConfig
import com.tuan.islebridge.core.datastore.IsleBridgePreferences
import com.tuan.islebridge.core.updater.DownloadState
import com.tuan.islebridge.core.updater.ReleaseInfo
import com.tuan.islebridge.core.updater.UpdateCheckResult
import com.tuan.islebridge.ui.theme.IslandAccent
import com.tuan.islebridge.ui.theme.IslandError
import com.tuan.islebridge.ui.theme.IslandSuccess

@Composable
fun SettingsScreen(
    preferences: IsleBridgePreferences,
    downloadState: DownloadState,
    updateCheckResult: UpdateCheckResult?,
    isCheckingUpdate: Boolean,
    onSelectTheme: (String) -> Unit,
    onCheckForUpdates: () -> Unit,
    onDownloadUpdate: (ReleaseInfo) -> Unit,
    onInstallUpdate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Cài đặt & Hệ thống",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Tùy chỉnh giao diện, cập nhật OTA và thông tin phần mềm",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Theme Selection Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Giao diện ứng dụng",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                val themes = listOf(
                    "SYSTEM" to "Theo hệ thống",
                    "LIGHT" to "Giao diện Sáng",
                    "DARK" to "Giao diện Tối"
                )

                themes.forEach { (key, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = preferences.appTheme == key,
                            onClick = { onSelectTheme(key) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = label, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // GitHub OTA Update Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = IslandAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Cập nhật ứng dụng (GitHub OTA)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Phiên bản hiện tại: v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Update Check Result States
                when (updateCheckResult) {
                    is UpdateCheckResult.UpdateAvailable -> {
                        val release = updateCheckResult.release
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = IslandAccent.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Đã có bản cập nhật mới: ${release.tagName}",
                                    fontWeight = FontWeight.Bold,
                                    color = IslandAccent
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = release.releaseNotes,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { onDownloadUpdate(release) },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Tải xuống và cài đặt")
                                }
                            }
                        }
                    }
                    is UpdateCheckResult.UpToDate -> {
                        Text(
                            text = "Bạn đang sử dụng phiên bản mới nhất.",
                            color = IslandSuccess,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                    is UpdateCheckResult.Error -> {
                        Text(
                            text = "Lỗi kiểm tra: ${updateCheckResult.message}",
                            color = IslandError,
                            fontSize = 12.sp
                        )
                    }
                    null -> {}
                }

                // Download / Install Progress States
                when (downloadState) {
                    is DownloadState.Downloading -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { downloadState.progressPercent / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Đang tải xuống: ${downloadState.progressPercent}% (${downloadState.bytesDownloaded / 1024} KB / ${downloadState.totalBytes / 1024} KB)",
                            fontSize = 11.sp
                        )
                    }
                    is DownloadState.Verifying -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = downloadState.message, color = IslandAccent, fontSize = 12.sp)
                    }
                    is DownloadState.ReadyToInstall -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onInstallUpdate(downloadState.apkPath) },
                            colors = ButtonDefaults.buttonColors(containerColor = IslandSuccess)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cài đặt APK đã xác thực")
                        }
                    }
                    is DownloadState.Failed -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Cập nhật thất bại: ${downloadState.reason}", color = IslandError, fontSize = 12.sp)
                    }
                    DownloadState.Idle -> {}
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onCheckForUpdates,
                    enabled = !isCheckingUpdate,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    if (isCheckingUpdate) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Đang kiểm tra...")
                    } else {
                        Text("Kiểm tra bản cập nhật")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // About Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Về IsleBridge",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "IsleBridge là ứng dụng đồng hành hệ thống thông minh, tích hợp giao thức Super Island trên OriginOS 6/7 (vivo & iQOO). Hoạt động hoàn toàn không cần Root, không yêu cầu Accessibility Service.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Application ID: com.tuan.islebridge\nTác giả: Tuan (Senior Android Engineer)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

