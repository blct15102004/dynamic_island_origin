package com.tuan.islebridge.ui.screens

import android.content.pm.ApplicationInfo
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuan.islebridge.core.datastore.IsleBridgePreferences
import com.tuan.islebridge.core.notifications.AppFilterManager
import com.tuan.islebridge.ui.theme.IslandError
import com.tuan.islebridge.ui.theme.IslandSuccess

data class AppItem(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    val isFinancial: Boolean
)

@Composable
fun ApplicationsScreen(
    preferences: IsleBridgePreferences,
    installedApps: List<AppItem>,
    onToggleAppAllowed: (String, Boolean) -> Unit,
    onToggleAppBlocked: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showSystemApps by remember { mutableStateOf(false) }

    val filteredApps = remember(installedApps, searchQuery, showSystemApps) {
        installedApps.filter { app ->
            val matchesSearch = app.appName.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true)
            val matchesSystem = if (showSystemApps) true else !app.isSystemApp
            matchesSearch && matchesSystem
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Quản lý ứng dụng chuyển tiếp",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Chọn ứng dụng cho phép hiển thị thông báo trên Origin Island",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Tìm kiếm tên app hoặc package...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // System Apps Filter & Keep Original Notifs Tag
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = showSystemApps,
                    onCheckedChange = { showSystemApps = it }
                )
                Text("Hiện app hệ thống", fontSize = 12.sp)
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = IslandSuccess.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "Giữ nguyên thông báo gốc: BẬT",
                    color = IslandSuccess,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Apps List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredApps, key = { it.packageName }) { app ->
                val isBlocked = preferences.blockedPackages.contains(app.packageName) || app.isFinancial
                val isAllowed = !isBlocked && (preferences.allowedPackages.isEmpty() || preferences.allowedPackages.contains(app.packageName))

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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = app.appName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                if (app.isFinancial) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = IslandError.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "Ngân hàng (Khóa)",
                                            color = IslandError,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = app.packageName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }

                        if (app.isFinancial) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Tự động khóa bảo mật",
                                tint = IslandError,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Switch(
                                checked = isAllowed,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        onToggleAppBlocked(app.packageName, false)
                                        onToggleAppAllowed(app.packageName, true)
                                    } else {
                                        onToggleAppBlocked(app.packageName, true)
                                        onToggleAppAllowed(app.packageName, false)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

