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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tuan.islebridge.ui.theme.IslandError
import com.tuan.islebridge.ui.theme.IslandSuccess

@Composable
fun PermissionActivationDialog(
    hasNotificationListener: Boolean,
    hasPostNotification: Boolean,
    hasBluetoothPermission: Boolean,
    isBatteryOptimizedExempt: Boolean,
    onRequestQuickPermissions: () -> Unit,
    onRequestNotificationListener: () -> Unit,
    onRequestPostNotification: () -> Unit,
    onRequestBluetooth: () -> Unit,
    onRequestBatteryExemption: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allRequiredGranted = hasNotificationListener && hasPostNotification

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
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
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Kích hoạt IsleBridge",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cấp quyền để kết nối Origin Island hệ thống",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // One-tap quick grant banner if runtime permissions are missing
                if (!hasPostNotification || !hasBluetoothPermission) {
                    FilledTonalButton(
                        onClick = onRequestQuickPermissions,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cấp nhanh quyền hệ thống (1 chạm)", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Scrollable permissions checklist
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Notification Listener (Crucial)
                    PermissionStepItem(
                        title = "1. Đọc thông báo (Notification Listener)",
                        description = "Bắt buộc: Để IsleBridge nhận diện thông báo từ Grab, Shopee, tin nhắn và mã OTP đưa lên Đảo.",
                        isRequired = true,
                        isGranted = hasNotificationListener,
                        icon = Icons.Default.NotificationsActive,
                        buttonLabel = "Mở cài đặt",
                        onAction = onRequestNotificationListener
                    )

                    // 2. Post Notification (Android 13+)
                    PermissionStepItem(
                        title = "2. Gửi thông báo (POST_NOTIFICATIONS)",
                        description = "Bắt buộc (Android 13+): Đảm bảo hiển thị thẻ Origin Island và thông báo dịch vụ.",
                        isRequired = true,
                        isGranted = hasPostNotification,
                        icon = Icons.Default.NotificationAdd,
                        buttonLabel = "Cho phép",
                        onAction = onRequestPostNotification
                    )

                    // 3. Bluetooth Connect
                    PermissionStepItem(
                        title = "3. Thiết bị Bluetooth ở gần",
                        description = "Để tự động phát hiện tai nghe không dây và hiển thị pin tai nghe / hộp sạc lên đảo.",
                        isRequired = false,
                        isGranted = hasBluetoothPermission,
                        icon = Icons.Default.Bluetooth,
                        buttonLabel = "Cho phép",
                        onAction = onRequestBluetooth
                    )

                    // 4. Battery Optimization Exemption
                    PermissionStepItem(
                        title = "4. Bỏ qua tối ưu hóa pin (Doze)",
                        description = "Khuyên dùng: Giúp dịch vụ chạy ngầm ổn định, không bị hệ điều hành tắt khi khóa màn hình.",
                        isRequired = false,
                        isGranted = isBatteryOptimizedExempt,
                        icon = Icons.Default.BatteryChargingFull,
                        buttonLabel = "Bỏ qua tối ưu",
                        onAction = onRequestBatteryExemption
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Để sau")
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (allRequiredGranted) IslandSuccess else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = if (allRequiredGranted) "Bắt đầu sử dụng" else "Xong",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionStepItem(
    title: String,
    description: String,
    isRequired: Boolean,
    isGranted: Boolean,
    icon: ImageVector,
    buttonLabel: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isGranted) IslandSuccess else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                if (isGranted) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = IslandSuccess.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Đã cấp ✓",
                            color = IslandSuccess,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else if (isRequired) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = IslandError.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Bắt buộc",
                            color = IslandError,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )

            if (!isGranted) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onAction,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(buttonLabel, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
