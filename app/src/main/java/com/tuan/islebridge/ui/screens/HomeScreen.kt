package com.tuan.islebridge.ui.screens

import androidx.compose.foundation.clickable
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
import com.tuan.islebridge.core.bluetooth.EarbudBatteryInfo
import com.tuan.islebridge.core.common.DetectionConfidence
import com.tuan.islebridge.core.common.DeviceCompatibilityInfo
import com.tuan.islebridge.core.common.OriginOsCompatibilityStatus
import com.tuan.islebridge.core.datastore.IsleBridgePreferences
import com.tuan.islebridge.core.originisland.IslandCardModel
import com.tuan.islebridge.core.shizuku.ShizukuServiceState
import com.tuan.islebridge.core.shizuku.ShizukuStatus
import com.tuan.islebridge.ui.components.ShizukuGuideDialog
import com.tuan.islebridge.ui.components.StatusCard
import com.tuan.islebridge.ui.theme.IslandAccent
import com.tuan.islebridge.ui.theme.IslandError
import com.tuan.islebridge.ui.theme.IslandSuccess
import com.tuan.islebridge.ui.theme.IslandWarning

@Composable
fun HomeScreen(
    preferences: IsleBridgePreferences,
    compatibility: DeviceCompatibilityInfo,
    shizukuStatus: ShizukuStatus,
    bluetoothInfo: EarbudBatteryInfo?,
    activeCards: List<IslandCardModel>,
    isListenerActive: Boolean,
    onToggleIsland: (Boolean) -> Unit,
    onOpenPermissions: () -> Unit,
    onNavigateToBluetooth: () -> Unit,
    onNavigateToOtp: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onRequestShizuku: () -> Unit,
    onOpenShizukuApp: () -> Unit,
    onOpenShizukuDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showShizukuGuide by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // High-Priority Permission Alert Banner (if Notification Listener is not granted)
        if (!isListenerActive) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenPermissions() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Chưa cấp quyền Đọc thông báo!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Cần quyền Notification Listener để chuyển tiếp thông báo & OTP lên Origin Island.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onOpenPermissions,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Cấp ngay", fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Master Island Switch
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Origin Island Companion",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (preferences.isIslandEnabled && isListenerActive) {
                            "Dịch vụ đang hoạt động • Tích hợp Origin Island gốc"
                        } else if (!isListenerActive) {
                            "Đang tạm dừng • Cần cấp quyền Đọc thông báo"
                        } else {
                            "Dịch vụ đang tạm dừng theo tùy chọn người dùng"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = preferences.isIslandEnabled,
                    onCheckedChange = onToggleIsland
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Device Compatibility Card
        val statusText = when (compatibility.status) {
            OriginOsCompatibilityStatus.NATIVE_ORIGIN_OS_SUPPORTED -> "OriginOS Native"
            OriginOsCompatibilityStatus.NATIVE_ORIGIN_OS_EXPERIMENTAL -> "OriginOS 7 (Thử nghiệm)"
            OriginOsCompatibilityStatus.VIVO_LEGACY_OR_UNCONFIRMED -> "Chưa xác nhận OriginOS"
            OriginOsCompatibilityStatus.NON_VIVO_DEVICE -> "Thiết bị ngoài vivo"
        }

        val confidenceText = when (compatibility.confidence) {
            DetectionConfidence.CONFIRMED -> "Xác nhận"
            DetectionConfidence.ESTIMATED -> "Ước tính"
            DetectionConfidence.UNKNOWN -> "Chưa rõ"
        }

        StatusCard(
            title = "Khả năng tương thích thiết bị",
            statusText = statusText,
            confidenceText = confidenceText,
            description = "${compatibility.manufacturer} ${compatibility.model} (Android ${compatibility.androidRelease}, API ${compatibility.androidSdk})\n${compatibility.statusDescription}",
            icon = Icons.Default.PhoneAndroid,
            isPositive = compatibility.isSuperIslandSupported
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Notification Listener Status Card
        StatusCard(
            title = "Quyền Notification Listener",
            statusText = if (isListenerActive) "Đã kết nối" else "Chưa cấp quyền",
            description = if (isListenerActive) {
                "Dịch vụ lắng nghe thông báo đang hoạt động. IsleBridge tích hợp trực tiếp với Origin Island hệ thống."
            } else {
                "Cần cấp quyền Notification Listener trong Cài đặt hệ thống để IsleBridge nhận diện thông báo và OTP."
            },
            icon = Icons.Default.NotificationsActive,
            isPositive = isListenerActive,
            actionButtonLabel = if (!isListenerActive) "Cấp quyền ngay" else null,
            onActionClick = onOpenPermissions
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Navigation to Features - Balanced 2x2 Grid
        Text(
            text = "Tính năng mở rộng",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Row 1: Bluetooth & OTP
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Bluetooth Card Shortcut
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToBluetooth() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = null,
                        tint = if (bluetoothInfo?.isConnected == true) IslandSuccess else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tai nghe",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (bluetoothInfo?.isConnected == true) "${bluetoothInfo.overallBattery ?: 0}% Pin" else "Chưa kết nối",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            // OTP Detection Shortcut
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToOtp() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Icon(
                        imageVector = Icons.Default.Password,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Mã OTP",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (preferences.isOtpDetectionEnabled) "Tự động bắt" else "Tắt",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 2: Privacy & Shizuku
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Privacy Monitor Shortcut
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToPrivacy() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Riêng tư",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (preferences.isPrivacyAlertEnabled) "Cam/Mic On" else "Tắt",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            // Shizuku Status Shortcut
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showShizukuGuide = true },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = if (shizukuStatus.hasPermission) IslandSuccess else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Shizuku",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    val shizukuBrief = when (shizukuStatus.state) {
                        ShizukuServiceState.AUTHORIZED -> "Đã cấp quyền"
                        ShizukuServiceState.UNAUTHORIZED -> "Chưa cấp quyền"
                        ShizukuServiceState.SERVICE_STOPPED -> "Dịch vụ tắt"
                        ShizukuServiceState.NOT_INSTALLED -> "Chưa cài đặt"
                    }
                    Text(
                        text = shizukuBrief,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Active Cards Preview Section
        Text(
            text = "Thẻ Origin Island đang hoạt động (${activeCards.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (activeCards.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ) {
                Text(
                    text = "Hiện chưa có thẻ nào trên Origin Island hệ thống.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                activeCards.forEach { card ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = card.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = card.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = card.capsuleText,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showShizukuGuide) {
        ShizukuGuideDialog(
            status = shizukuStatus,
            onAuthorizeClick = onRequestShizuku,
            onOpenShizukuApp = onOpenShizukuApp,
            onOpenDownloadSite = onOpenShizukuDownload,
            onDismiss = { showShizukuGuide = false }
        )
    }
}
