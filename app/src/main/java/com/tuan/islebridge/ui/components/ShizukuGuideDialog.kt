package com.tuan.islebridge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tuan.islebridge.core.shizuku.ShizukuServiceState
import com.tuan.islebridge.core.shizuku.ShizukuStatus
import com.tuan.islebridge.ui.theme.IslandAccent
import com.tuan.islebridge.ui.theme.IslandError
import com.tuan.islebridge.ui.theme.IslandSuccess
import com.tuan.islebridge.ui.theme.IslandWarning

@Composable
fun ShizukuGuideDialog(
    status: ShizukuStatus,
    onAuthorizeClick: () -> Unit,
    onOpenShizukuApp: () -> Unit,
    onOpenDownloadSite: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hướng dẫn Shizuku (Tùy chọn)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Kết nối để tối ưu hóa nâng cao không cần Root",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Current Status Banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = when (status.state) {
                        ShizukuServiceState.AUTHORIZED -> IslandSuccess.copy(alpha = 0.15f)
                        ShizukuServiceState.UNAUTHORIZED -> IslandWarning.copy(alpha = 0.15f)
                        ShizukuServiceState.SERVICE_STOPPED -> IslandWarning.copy(alpha = 0.15f)
                        ShizukuServiceState.NOT_INSTALLED -> IslandError.copy(alpha = 0.15f)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val statusText = when (status.state) {
                            ShizukuServiceState.AUTHORIZED -> "Đã kết nối và ủy quyền Shizuku v${status.version}"
                            ShizukuServiceState.UNAUTHORIZED -> "Dịch vụ đang chạy - Chưa cấp quyền cho IsleBridge"
                            ShizukuServiceState.SERVICE_STOPPED -> "Dịch vụ Shizuku chưa khởi chạy"
                            ShizukuServiceState.NOT_INSTALLED -> "Chưa cài đặt ứng dụng Shizuku"
                        }
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = when (status.state) {
                                ShizukuServiceState.AUTHORIZED -> IslandSuccess
                                ShizukuServiceState.UNAUTHORIZED -> IslandWarning
                                ShizukuServiceState.SERVICE_STOPPED -> IslandWarning
                                ShizukuServiceState.NOT_INSTALLED -> IslandError
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Instructions list
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GuideStepItem(
                        step = "1",
                        title = "Cài đặt Shizuku",
                        description = "Cài đặt Shizuku từ Google Play hoặc trang chủ chính thức (shizuku.rikka.app)."
                    )

                    GuideStepItem(
                        step = "2",
                        title = "Bật Tùy chọn nhà phát triển",
                        description = "Vào Cài đặt > Giới thiệu điện thoại > Nhấn 7 lần vào 'Số hiệu bản dựng' để mở khóa Tùy chọn nhà phát triển."
                    )

                    GuideStepItem(
                        step = "3",
                        title = "Bật Gỡ lỗi không dây (Wireless Debugging)",
                        description = "Vào Cài đặt > Hệ thống > Tùy chọn nhà phát triển > Bật 'Gỡ lỗi không dây' (kết nối cùng mạng Wi-Fi)."
                    )

                    GuideStepItem(
                        step = "4",
                        title = "Khởi chạy Shizuku",
                        description = "Mở app Shizuku > Chọn 'Ghép nối' với mã ghép nối Gỡ lỗi không dây, sau đó nhấn 'Bắt đầu' (Start)."
                    )

                    GuideStepItem(
                        step = "5",
                        title = "Ủy quyền cho IsleBridge",
                        description = "Quay lại IsleBridge, nhấn nút 'Yêu cầu ủy quyền' bên dưới và chọn 'Cho phép luôn' trong popup Shizuku."
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons based on state
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Đóng")
                    }

                    when (status.state) {
                        ShizukuServiceState.NOT_INSTALLED -> {
                            Button(
                                onClick = onOpenDownloadSite,
                                modifier = Modifier.weight(1.5f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Tải Shizuku")
                            }
                        }
                        ShizukuServiceState.SERVICE_STOPPED -> {
                            Button(
                                onClick = onOpenShizukuApp,
                                modifier = Modifier.weight(1.5f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Mở Shizuku")
                            }
                        }
                        ShizukuServiceState.UNAUTHORIZED -> {
                            Button(
                                onClick = onAuthorizeClick,
                                modifier = Modifier.weight(1.5f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Yêu cầu ủy quyền")
                            }
                        }
                        ShizukuServiceState.AUTHORIZED -> {
                            Button(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1.5f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = IslandSuccess)
                            ) {
                                Text("Đã sẵn sàng ✓")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideStepItem(
    step: String,
    title: String,
    description: String
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = step,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}

