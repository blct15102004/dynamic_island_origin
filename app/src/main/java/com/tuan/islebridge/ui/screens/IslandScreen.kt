package com.tuan.islebridge.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuan.islebridge.core.datastore.IsleBridgePreferences
import com.tuan.islebridge.core.originisland.CardPriority
import com.tuan.islebridge.core.originisland.IslandCardModel
import com.tuan.islebridge.core.originisland.IslandScene
import com.tuan.islebridge.ui.components.IslandPreviewCard
import com.tuan.islebridge.ui.theme.IslandAccent

@Composable
fun IslandScreen(
    preferences: IsleBridgePreferences,
    onToggleForceNative: (Boolean) -> Unit,
    onUpdateTimeout: (Long) -> Unit,
    onTriggerTestCard: (IslandCardModel) -> Unit,
    onClearAllCards: () -> Int,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

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
            text = "Tích hợp trực tiếp với Origin Island (Super Island) gốc trên vivo / iQOO",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Native Island Architecture Banner (No Custom Overlay Guarantee)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = IslandAccent.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = IslandAccent,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Cam kết kiến trúc: IsleBridge KHÔNG vẽ đảo giả và KHÔNG dùng cửa sổ nổi (Overlay). Toàn bộ viên thuốc và thẻ mở rộng được hiển thị bởi Origin Island gốc của hệ thống vivo/iQOO.",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

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
                        Text("Giao thức Native Super Island", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Đính kèm metadata notification.superx.* để kích hoạt hiệu ứng viên thuốc quanh nốt ruồi OriginOS.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = preferences.forceNativeIsland,
                        onCheckedChange = onToggleForceNative
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Thử nghiệm thẻ Origin Island",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Mỗi lần nhấn sẽ gửi 1 sự kiện thử nghiệm. Thẻ sẽ tự động biến mất sau 3 giây.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Test Template 1: Music Playback Progress
        IslandPreviewCard(
            templateName = "Mẫu 1: Tiến trình Nhạc / Media",
            capsuleText = "Đang phát",
            capsuleSubtext = "68%",
            description = "Hiển thị viên thuốc phát nhạc trên Origin Island. Tự động ẩn sau 3 giây.",
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
                    timeoutMs = 3000L
                )
                onTriggerTestCard(card)
                Toast.makeText(context, "Đã gửi thẻ thử nghiệm (Tự biến mất sau 3s)", Toast.LENGTH_SHORT).show()
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Test Template 2: Download Progress
        IslandPreviewCard(
            templateName = "Mẫu 2: Tải xuống tập tin",
            capsuleText = "Tải xuống",
            capsuleSubtext = "45%",
            description = "Hiển thị tiến trình phần trăm trên viên thuốc Origin Island. Tự động ẩn sau 3 giây.",
            onTestClick = {
                val card = IslandCardModel(
                    id = "test_download",
                    sourcePackage = "com.android.providers.downloads",
                    scene = IslandScene.PROGRESS,
                    title = "IsleBridge_v1.0.apk",
                    subtitle = "18.5 MB / 41.2 MB (45%)",
                    capsuleText = "Tải xuống",
                    capsuleSubtext = "45%",
                    progress = 45,
                    maxProgress = 100,
                    priority = CardPriority.HIGH,
                    timeoutMs = 3000L
                )
                onTriggerTestCard(card)
                Toast.makeText(context, "Đã gửi thẻ tiến trình thử nghiệm (Tự biến mất sau 3s)", Toast.LENGTH_SHORT).show()
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Test Template 3: OTP Verification
        IslandPreviewCard(
            templateName = "Mẫu 3: Mã xác thực OTP",
            capsuleText = "OTP: 958231",
            capsuleSubtext = "Chép",
            description = "Thẻ ưu tiên cao hiển thị mã OTP lên đảo. Tự động ẩn sau 3 giây.",
            onTestClick = {
                val card = IslandCardModel(
                    id = "test_otp",
                    sourcePackage = "com.google.android.apps.messaging",
                    scene = IslandScene.OTP,
                    title = "Mã xác thực: 958231",
                    subtitle = "Mã OTP của bạn là 958231.",
                    capsuleText = "OTP: 958231",
                    capsuleSubtext = "Sao chép",
                    priority = CardPriority.URGENT,
                    timeoutMs = 3000L
                )
                onTriggerTestCard(card)
                Toast.makeText(context, "Đã gửi thẻ OTP thử nghiệm (Tự biến mất sau 3s)", Toast.LENGTH_SHORT).show()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                val removedCount = onClearAllCards()
                val message = if (removedCount > 0) {
                    "Đã gỡ $removedCount thẻ thử nghiệm khỏi Origin Island."
                } else {
                    "Không có thẻ thử nghiệm nào đang hoạt động để gỡ."
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LayersClear,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Gỡ tất cả thẻ Island thử nghiệm (Unmount)")
        }
    }
}
