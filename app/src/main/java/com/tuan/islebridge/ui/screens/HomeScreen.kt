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
import com.tuan.islebridge.core.bluetooth.EarbudBatteryInfo
import com.tuan.islebridge.core.common.CompatibilityDetector
import com.tuan.islebridge.core.common.DeviceCompatibilityInfo
import com.tuan.islebridge.core.common.OriginOsCompatibilityStatus
import com.tuan.islebridge.core.datastore.IsleBridgePreferences
import com.tuan.islebridge.core.notifications.IsleBridgeNotificationListenerService
import com.tuan.islebridge.core.originisland.IslandCardModel
import com.tuan.islebridge.core.shizuku.ShizukuStatus
import com.tuan.islebridge.ui.components.StatusCard

@Composable
fun HomeScreen(
    preferences: IsleBridgePreferences,
    compatibility: DeviceCompatibilityInfo,
    shizukuStatus: ShizukuStatus,
    bluetoothInfo: EarbudBatteryInfo?,
    activeCards: List<IslandCardModel>,
    onToggleIsland: (Boolean) -> Unit,
    onToggleOverlayFallback: (Boolean) -> Unit,
    onOpenPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isListenerActive = IsleBridgeNotificationListenerService.isConnected

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
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
                    .padding(20.dp),
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
                    Text(
                        text = if (preferences.isIslandEnabled) "Dịch vụ đang hoạt động" else "Dịch vụ đang tạm dừng",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                Switch(
                    checked = preferences.isIslandEnabled,
                    onCheckedChange = onToggleIsland
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Device Compatibility Card
        val isOriginOsSupported = compatibility.status == OriginOsCompatibilityStatus.NATIVE_ORIGIN_OS_6_SUPPORTED ||
                compatibility.status == OriginOsCompatibilityStatus.NATIVE_ORIGIN_OS_7_EXPERIMENTAL

        StatusCard(
            title = "Khả năng tương thích thiết bị",
            statusText = when (compatibility.status) {
                OriginOsCompatibilityStatus.NATIVE_ORIGIN_OS_6_SUPPORTED -> "OriginOS 6 Native"
                OriginOsCompatibilityStatus.NATIVE_ORIGIN_OS_7_EXPERIMENTAL -> "OriginOS 7 (Thử nghiệm)"
                OriginOsCompatibilityStatus.VIVO_LEGACY_UNSUPPORTED -> "vivo (Legacy Fallback)"
                OriginOsCompatibilityStatus.NON_VIVO_FALLBACK -> "Fallback Mode"
            },
            description = "${compatibility.manufacturer} ${compatibility.model} (Android ${compatibility.androidRelease})\n${compatibility.statusDescription}",
            icon = Icons.Default.PhoneAndroid,
            isPositive = isOriginOsSupported
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Notification Listener Status Card
        StatusCard(
            title = "Quyền Notification Listener",
            statusText = if (isListenerActive) "Đã kết nối" else "Chưa cấp quyền",
            description = if (isListenerActive) {
                "Dịch vụ lắng nghe thông báo đang chạy ngầm an toàn. Không ảnh hưởng đến thông báo gốc."
            } else {
                "Cần cấp quyền Notification Listener trong Cài đặt hệ thống để IsleBridge nhận diện thông báo và OTP."
            },
            icon = Icons.Default.NotificationsActive,
            isPositive = isListenerActive,
            actionButtonLabel = if (!isListenerActive) "Cấp quyền ngay" else null,
            onActionClick = onOpenPermissions
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Bluetooth Earbuds Status
        StatusCard(
            title = "Tai nghe Bluetooth",
            statusText = if (bluetoothInfo?.isConnected == true) "Đã kết nối" else "Chưa kết nối",
            description = if (bluetoothInfo?.isConnected == true) {
                "${bluetoothInfo.deviceName} | Pin: ${bluetoothInfo.overallBattery?.let { "$it%" } ?: "Unknown"}"
            } else {
                "Tự động phát hiện tai nghe và hiển thị thẻ pin khi kết nối."
            },
            icon = Icons.Default.Headphones,
            isPositive = bluetoothInfo?.isConnected == true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Shizuku Status
        StatusCard(
            title = "Shizuku Service (Tùy chọn)",
            statusText = if (shizukuStatus.hasPermission) "Đã kích hoạt" else "Không dùng / Chưa kết nối",
            description = if (shizukuStatus.hasPermission) {
                "Shizuku v${shizukuStatus.version} đang hoạt động, hỗ trợ tối ưu hóa Doze và AppOps nâng cao."
            } else {
                "Không bắt buộc. IsleBridge vẫn hoạt động độc lập không cần Shizuku hay Root."
            },
            icon = Icons.Default.Terminal,
            isPositive = shizukuStatus.hasPermission
        )

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
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "Hiện chưa có thẻ nào trên Origin Island.",
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
}

