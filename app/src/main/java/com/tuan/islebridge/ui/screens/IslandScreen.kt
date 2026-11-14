package com.tuan.islebridge.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tuan.islebridge.core.datastore.IsleBridgePreferences
import com.tuan.islebridge.core.originisland.CardPriority
import com.tuan.islebridge.core.originisland.IslandCardModel
import com.tuan.islebridge.core.originisland.IslandScene
import com.tuan.islebridge.ui.components.IslandPreviewCard

@Composable
fun IslandScreen(
    preferences: IsleBridgePreferences,
    onToggleForceNative: (Boolean) -> Unit,
    onToggleOverlayFallback: (Boolean) -> Unit,
    onUpdateTimeout: (Long) -> Unit,
    onTriggerTestCard: (IslandCardModel) -> Unit,
    onClearAllCards: () -> Unit,
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
            text = "Cấu hình Origin Island",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Tùy biến hiển thị thẻ Super Island trên vivo / iQOO và chế độ dự phòng",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Settings Cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Force Native Island
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ép buộc giao thức Native Super Island", fontWeight = FontWeight.Bold)
                        Text(
                            "Gửi các notification.superx.* extras ngay cả khi thiết bị không được xác định rõ là OriginOS.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                    Switch(
                        checked = preferences.forceNativeIsland,
                        onCheckedChange = onToggleForceNative
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Custom Overlay Fallback
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Chế độ Dynamic Island Overlay (Fallback)", fontWeight = FontWeight.Bold)
                        Text(
                            "Hiển thị viên thuốc nổi mô phỏng quanh camera nốt ruồi khi không dùng Origin Island gốc.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                    Switch(
                        checked = preferences.isOverlayFallbackEnabled,
                        onCheckedChange = onToggleOverlayFallback
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Thử nghiệm các mẫu thẻ Super Island",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Test Template 1: Music Playback Progress
        IslandPreviewCard(
            templateName = "Mẫu 1: Tiến trình Nhạc / Media",
            capsuleText = "Bài hát đang phát",
            capsuleSubtext = "68%",
            description = "Thẻ tiến trình hiển thị tiêu đề bài hát, ca sĩ và thanh tiến trình phát nhạc.",
            onTestClick = {
                val card = IslandCardModel(
                    id = "test_media",
                    sourcePackage = "com.spotify.music",
                    scene = IslandScene.MEDIA,
                    title = "See You Again",
                    subtitle = "Wiz Khalifa ft. Charlie Puth",
                    capsuleText = "Đang phát",
                    capsuleSubtext = "68%",
                    progress = 68,
                    maxProgress = 100,
                    priority = CardPriority.NORMAL,
                    timeoutMs = 15000L
                )
                onTriggerTestCard(card)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Test Template 2: Download Progress
        IslandPreviewCard(
            templateName = "Mẫu 2: Tải xuống tập tin",
            capsuleText = "IsleBridge_v1.0.apk",
            capsuleSubtext = "45%",
            description = "Hiển thị tiến trình tải xuống tệp với thanh phần trăm cập nhật động.",
            onTestClick = {
                val card = IslandCardModel(
                    id = "test_download",
                    sourcePackage = "com.android.providers.downloads",
                    scene = IslandScene.PROGRESS,
                    title = "Đang tải xuống IsleBridge_v1.0.apk",
                    subtitle = "18.5 MB / 41.2 MB (45%)",
                    capsuleText = "Tải xuống",
                    capsuleSubtext = "45%",
                    progress = 45,
                    maxProgress = 100,
                    priority = CardPriority.HIGH,
                    timeoutMs = 15000L
                )
                onTriggerTestCard(card)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Test Template 3: OTP Verification
        IslandPreviewCard(
            templateName = "Mẫu 3: Mã xác thực OTP khẩn cấp",
            capsuleText = "OTP: 958231",
            capsuleSubtext = "Chạm chép",
            description = "Thẻ mức ưu tiên URGENT với mã số nổi bật và nút sao chép một chạm.",
            onTestClick = {
                val card = IslandCardModel(
                    id = "test_otp",
                    sourcePackage = "com.google.android.apps.messaging",
                    scene = IslandScene.OTP,
                    title = "Mã xác thực: 958231",
                    subtitle = "Mã OTP của bạn là 958231. Hiệu lực 5 phút.",
                    capsuleText = "OTP: 958231",
                    capsuleSubtext = "Sao chép",
                    priority = CardPriority.URGENT,
                    timeoutMs = 30000L
                )
                onTriggerTestCard(card)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onClearAllCards,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Gỡ tất cả thẻ Island thử nghiệm (Unmount)")
        }
    }
}

