package com.tuan.islebridge.ui.screens

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
import com.tuan.islebridge.core.shizuku.ShizukuStatus
import com.tuan.islebridge.ui.components.PermissionRow
import com.tuan.islebridge.ui.theme.IslandSuccess

@Composable
fun PermissionsScreen(
    hasNotificationListener: Boolean,
    hasOverlayPermission: Boolean,
    hasPostNotification: Boolean,
    hasBluetoothPermission: Boolean,
    isBatteryOptimizedExempt: Boolean,
    shizukuStatus: ShizukuStatus,
    onRequestNotificationListener: () -> Unit,
    onRequestOverlay: () -> Unit,
    onRequestPostNotification: () -> Unit,
    onRequestBluetooth: () -> Unit,
    onRequestBatteryExemption: () -> Unit,
    onRequestShizuku: () -> Unit,
    onOpenAutoStartSettings: () -> Unit,
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
            text = "Trung tâm kiểm soát quyền hệ thống",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Quản lý tập trung các quyền Android cần thiết cho IsleBridge",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // No Accessibility Guarantee Banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = IslandSuccess.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = IslandSuccess,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Cam kết quyền hạn: IsleBridge KHÔNG yêu cầu quyền Accessibility Service và KHÔNG yêu cầu Root. Toàn bộ tính năng hoạt động dựa trên Android API chuẩn và Shizuku tùy chọn.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Notification Listener
        PermissionRow(
            title = "Truy cập thông báo (Notification Listener)",
            description = "Bắt buộc: Cho phép ứng dụng nhận diện thông báo đến để tạo thẻ Super Island và mã OTP.",
            isGranted = hasNotificationListener,
            icon = Icons.Default.NotificationsActive,
            onRequestPermission = onRequestNotificationListener
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Post Notifications
        PermissionRow(
            title = "Gửi thông báo (POST_NOTIFICATIONS)",
            description = "Bắt buộc trên Android 13+: Để hiển thị thông báo và gửi thẻ lên Origin Island.",
            isGranted = hasPostNotification,
            icon = Icons.Default.NotificationAdd,
            onRequestPermission = onRequestPostNotification
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Draw Over Other Apps (Overlay)
        PermissionRow(
            title = "Hiển thị đè lên ứng dụng khác (Overlay)",
            description = "Tùy chọn: Dành cho chế độ Fallback Dynamic Island trên thiết bị không hỗ trợ native OriginOS.",
            isGranted = hasOverlayPermission,
            icon = Icons.Default.Layers,
            onRequestPermission = onRequestOverlay
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Bluetooth Permissions
        PermissionRow(
            title = "Thiết bị ở gần (Bluetooth Connect)",
            description = "Cần thiết: Giám sát tai nghe kết nối và cập nhật mức pin tai nghe / hộp sạc.",
            isGranted = hasBluetoothPermission,
            icon = Icons.Default.Bluetooth,
            onRequestPermission = onRequestBluetooth
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 5. Battery Optimization Exemption
        PermissionRow(
            title = "Bỏ qua tối ưu hóa pin (Doze Exemption)",
            description = "Khuyên dùng: Đảm bảo tiến trình chạy ngầm ổn định không bị hệ điều hành tắt đột ngột.",
            isGranted = isBatteryOptimizedExempt,
            icon = Icons.Default.BatteryChargingFull,
            onRequestPermission = onRequestBatteryExemption
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 6. Shizuku Authorization
        PermissionRow(
            title = "Ủy quyền Shizuku (Tùy chọn)",
            description = "Nâng cao: Tự động miễn trừ Doze và truy vấn chẩn đoán AppOps mà không cần Root.",
            isGranted = shizukuStatus.hasPermission,
            icon = Icons.Default.Terminal,
            onRequestPermission = onRequestShizuku
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 7. Auto-start OEM Guidance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SettingsPower,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Tự khởi chạy trên vivo / iQOO (iManager)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Để giữ kết nối đảo liên tục sau khi khởi động lại máy trên OriginOS:\n1. Mở Cài đặt > Pin > Quản lý pin dưới nền > IsleBridge: Chọn 'Cho phép sử dụng pin nền cao'.\n2. Mở ứng dụng iManager > Quản lý quyền > Tự khởi chạy: Bật IsleBridge.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onOpenAutoStartSettings,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Mở thông tin ứng dụng")
                }
            }
        }
    }
}

