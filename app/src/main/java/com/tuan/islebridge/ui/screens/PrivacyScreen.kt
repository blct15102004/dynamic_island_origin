package com.tuan.islebridge.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuan.islebridge.core.datastore.IsleBridgePreferences
import com.tuan.islebridge.core.originisland.CardPriority
import com.tuan.islebridge.core.originisland.IslandCardModel
import com.tuan.islebridge.core.originisland.IslandScene
import com.tuan.islebridge.core.privacy.PrivacyAccessEvent
import com.tuan.islebridge.core.privacy.PrivacyCapabilityStatus
import com.tuan.islebridge.ui.theme.IslandAccent
import com.tuan.islebridge.ui.theme.IslandWarning

@Composable
fun PrivacyScreen(
    preferences: IsleBridgePreferences,
    capability: PrivacyCapabilityStatus,
    lastPrivacyEvent: PrivacyAccessEvent?,
    onTogglePrivacyAlert: (Boolean) -> Unit,
    onTriggerTestAlert: (IslandCardModel) -> Unit,
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
            text = "Cảnh báo quyền riêng tư (Camera & Mic)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Phát hiện khi ứng dụng khác truy cập Camera hoặc Microphone và báo cáo lên Island",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

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
                    Text("Kích hoạt cảnh báo quyền riêng tư", fontWeight = FontWeight.Bold)
                    Text(
                        "Hiển thị cảnh báo tức thì khi có ứng dụng sử dụng Camera hoặc Mic.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                }
                Switch(
                    checked = preferences.isPrivacyAlertEnabled,
                    onCheckedChange = onTogglePrivacyAlert
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Technical System Limitation Card (Honest Reporting)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (capability.canMonitorCrossAppViaShizuku) IslandAccent else IslandWarning,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Khả năng giám sát hệ thống",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = capability.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = "Trạng thái Backend: " + if (capability.canMonitorCrossAppViaShizuku) "Shizuku AppOps Active" else "Hạn chế Android Standard (Không có WATCH_APPOPS)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Test Buttons
        Text(
            text = "Thử nghiệm cảnh báo Origin Island",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    val card = IslandCardModel(
                        id = "test_camera",
                        sourcePackage = "com.android.camera",
                        scene = IslandScene.PRIVACY,
                        title = "Camera đang hoạt động",
                        subtitle = "Ứng dụng: Máy ảnh (Camera)",
                        capsuleText = "Camera",
                        capsuleSubtext = "Active",
                        priority = CardPriority.URGENT,
                        timeoutMs = 6000L
                    )
                    onTriggerTestAlert(card)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Test Camera")
            }

            Button(
                onClick = {
                    val card = IslandCardModel(
                        id = "test_mic",
                        sourcePackage = "com.android.soundrecorder",
                        scene = IslandScene.PRIVACY,
                        title = "Microphone đang hoạt động",
                        subtitle = "Ứng dụng: Ghi âm (Sound Recorder)",
                        capsuleText = "Microphone",
                        capsuleSubtext = "Active",
                        priority = CardPriority.URGENT,
                        timeoutMs = 6000L
                    )
                    onTriggerTestAlert(card)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Test Mic")
            }
        }
    }
}

