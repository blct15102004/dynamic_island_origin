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
import com.tuan.islebridge.core.shizuku.ShizukuServiceState
import com.tuan.islebridge.core.shizuku.ShizukuStatus
import com.tuan.islebridge.ui.components.PermissionRow
import com.tuan.islebridge.ui.theme.IslandAccent
import com.tuan.islebridge.ui.theme.IslandSuccess

@Composable
fun PermissionsScreen(
    hasNotificationListener: Boolean,
    hasPostNotification: Boolean,
    hasBluetoothPermission: Boolean,
    isBatteryOptimizedExempt: Boolean,
    shizukuStatus: ShizukuStatus,
    onRequestNotificationListener: () -> Unit,
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
            text = "Trung tâm quyền hệ thống",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Quản lý các quyền Android cần thiết cho IsleBridge và Origin Island",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Privacy & Architecture Guarantee Banner
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
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = IslandSuccess,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Cam kết quyền hạn: IsleBridge KHÔNG yêu cầu quyền Accessibility Service, KHÔNG yêu cầu Root và KHÔNG yêu cầu quyền vẽ đè màn hình (Overlay). Toàn bộ hoạt động dựa trên API hệ thống và Shizuku tùy chọn.",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Notification Listener (Crucial)
        PermissionRow(
            title = "Truy cập thông báo (Notification Listener)",
            description = "Bắt buộc: Để IsleBridge nhận diện thông báo từ ứng dụng và OTP để tích hợp lên Origin Island.",
            isGranted = hasNotificationListener,
            icon = Icons.Default.NotificationsActive,
            onRequestPermission = onRequestNotificationListener
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Post Notifications
        PermissionRow(
            title = "Gửi thông báo (POST_NOTIFICATIONS)",
            description = "Bắt buộc trên Android 13+: Đảm bảo hiển thị thông báo tiến trình và cập nhật thẻ lên Origin Island.",
            isGranted = hasPostNotification,
            icon = Icons.Default.NotificationAdd,
            onRequestPermission = onRequestPostNotification
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Bluetooth Permissions
        PermissionRow(
            title = "Thiết bị ở gần (Bluetooth Connect)",
            description = "Cần thiết: Giám sát tai nghe kết nối và cập nhật mức pin tai nghe / hộp sạc lên đảo.",
            isGranted = hasBluetoothPermission,
            icon = Icons.Default.Bluetooth,
            onRequestPermission = onRequestBluetooth
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Battery Optimization Exemption
        PermissionRow(
            title = "Bỏ qua tối ưu hóa pin (Doze Exemption)",
            description = "Khuyên dùng: Đảm bảo tiến trình chạy ngầm ổn định, không bị hệ điều hành tắt khi khóa màn hình.",
            isGranted = isBatteryOptimizedExempt,
            icon = Icons.Default.BatteryChargingFull,
            onRequestPermission = onRequestBatteryExemption
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 5. Shizuku Authorization (Optional)
        val shizukuActionLabel = when (shizukuStatus.state) {
            ShizukuServiceState.AUTHORIZED -> "Đã cấp quyền ✓"
            ShizukuServiceState.UNAUTHORIZED -> "Ủy quyền"
            ShizukuServiceState.SERVICE_STOPPED -> "Mở Shizuku"
            ShizukuServiceState.NOT_INSTALLED -> "Hướng dẫn"
        }

        PermissionRow(
            title = "Ủy quyền Shizuku (Tùy chọn nâng cao)",
            description = "${shizukuStatus.description}\nKhông bắt buộc: IsleBridge vẫn hoạt động độc lập bình thường.",
            isGranted = shizukuStatus.hasPermission,
            icon = Icons.Default.Code,
            onRequestPermission = onRequestShizuku
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Auto-start OEM Guidance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
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
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    lineHeight = 18.sp
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
