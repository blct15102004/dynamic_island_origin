package com.tuan.islebridge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuan.islebridge.ui.theme.IslandAccent
import com.tuan.islebridge.ui.theme.IslandError
import com.tuan.islebridge.ui.theme.IslandSuccess
import com.tuan.islebridge.ui.theme.IslandWarning

@Composable
fun StatusCard(
    title: String,
    statusText: String,
    description: String,
    icon: ImageVector,
    isPositive: Boolean = true,
    confidenceText: String? = null,
    actionButtonLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Adaptive Header
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = (if (isPositive) IslandSuccess else IslandWarning).copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isPositive) IslandSuccess else IslandWarning,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Status Badge - never breaks vertically
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = (if (isPositive) IslandSuccess else IslandWarning).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = statusText,
                                color = if (isPositive) IslandSuccess else IslandWarning,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        // Optional Confidence Badge
                        if (confidenceText != null) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = IslandAccent.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = confidenceText,
                                    color = IslandAccent,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                lineHeight = 18.sp
            )

            if (actionButtonLabel != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(text = actionButtonLabel, fontSize = 13.sp)
                }
            }
        }
    }
}
