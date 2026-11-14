package com.tuan.islebridge.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuan.islebridge.core.bluetooth.EarbudBatteryInfo
import com.tuan.islebridge.core.datastore.IsleBridgePreferences
import com.tuan.islebridge.ui.theme.IslandAccent
import com.tuan.islebridge.ui.theme.IslandSuccess

@Composable
fun BluetoothScreen(
    preferences: IsleBridgePreferences,
    bluetoothInfo: EarbudBatteryInfo?,
    hasBluetoothPermission: Boolean,
    onToggleBluetoothMonitoring: (Boolean) -> Unit,
    onToggleBluetoothMode: (Boolean) -> Unit,
    onUpdateTimeout: (Long) -> Unit,
    onRequestPermission: () -> Unit,
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
            text = "Giám sát tai nghe Bluetooth",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Tự động hiển thị mức pin tai nghe và hộp sạc trên Origin Island khi kết nối",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Permission warning if missing
        if (!hasBluetoothPermission) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Chưa cấp quyền Bluetooth",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Android yêu cầu quyền Thiết bị ở gần (BLUETOOTH_CONNECT) để phát hiện tai nghe và đọc phần trăm pin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onRequestPermission,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Cấp quyền Bluetooth ngay")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Master Switch
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Kích hoạt giám sát Bluetooth", fontWeight = FontWeight.Bold)
                    Text(
                        "Lắng nghe sự kiện kết nối/ngắt kết nối tai nghe.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                }
                Switch(
                    checked = preferences.isBluetoothMonitoringEnabled,
                    onCheckedChange = onToggleBluetoothMonitoring
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Current Connected Device Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (bluetoothInfo?.isConnected == true) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                        contentDescription = null,
                        tint = if (bluetoothInfo?.isConnected == true) IslandSuccess else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = bluetoothInfo?.deviceName ?: "Chưa kết nối tai nghe",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (bluetoothInfo?.isConnected == true) "Đang kết nối: ${bluetoothInfo.deviceAddress}" else "Không có thiết bị âm thanh nào",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Battery levels grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BatteryTile(
                        label = "Tổng quan",
                        value = bluetoothInfo?.overallBattery?.let { "$it%" } ?: "Unknown"
                    )
                    BatteryTile(
                        label = "Tai trái (L)",
                        value = bluetoothInfo?.leftBattery?.let { "$it%" } ?: "Unknown"
                    )
                    BatteryTile(
                        label = "Tai phải (R)",
                        value = bluetoothInfo?.rightBattery?.let { "$it%" } ?: "Unknown"
                    )
                    BatteryTile(
                        label = "Hộp sạc (Case)",
                        value = bluetoothInfo?.caseBattery?.let { "$it%" } ?: "Unknown"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "* IsleBridge tuân thủ nguyên tắc không tạo phần trăm pin giả mạo. Nếu thiết bị không gửi thông số qua Bluetooth API chuẩn, giá trị sẽ hiển thị Unknown.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display Mode Configuration
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Chế độ hiển thị thẻ Island",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (preferences.bluetoothModeAlwaysShow) "Chế độ A: Luôn hiển thị" else "Chế độ B: Tự động ẩn (Auto Hide)",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (preferences.bluetoothModeAlwaysShow) {
                                "Giữ thẻ pin tai nghe trên Island trong suốt thời gian kết nối."
                            } else {
                                "Hiển thị thẻ khi kết nối và tự động ẩn sau thời gian quy định."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                    Switch(
                        checked = preferences.bluetoothModeAlwaysShow,
                        onCheckedChange = onToggleBluetoothMode
                    )
                }
            }
        }
    }
}

@Composable
private fun BatteryTile(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.width(76.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (value != "Unknown") IslandSuccess else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

