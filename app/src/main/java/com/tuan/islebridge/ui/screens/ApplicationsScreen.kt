package com.tuan.islebridge.ui.screens

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuan.islebridge.core.datastore.IsleBridgePreferences
import com.tuan.islebridge.ui.theme.IslandAccent
import com.tuan.islebridge.ui.theme.IslandError
import com.tuan.islebridge.ui.theme.IslandSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class AppCompatibilityStatus(val label: String) {
    ORIGIN_PLAYER_COMPATIBLE("Origin Player"),
    SUPERX_NATIVE_ELIGIBLE("SuperX Island"),
    GENERAL_NOTIFICATION("Thông báo tiêu chuẩn"),
    FINANCIAL_PROTECTED("Bảo mật (Khóa)")
}

data class AppItem(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    val isFinancial: Boolean,
    val isOriginPlayerCandidate: Boolean,
    val compatibilityStatus: AppCompatibilityStatus
)

enum class AppFilterOption(val label: String) {
    ALL("Tất cả"),
    ALLOWED("Đã bật"),
    BLOCKED("Bị chặn"),
    ORIGIN_PLAYER("Origin Player")
}

// In-memory icon cache for smooth 60/120fps scrolling
private val iconCache = mutableMapOf<String, ImageBitmap?>()

@Composable
fun ApplicationsScreen(
    preferences: IsleBridgePreferences,
    installedApps: List<AppItem>,
    onToggleAppAllowed: (String, Boolean) -> Unit,
    onToggleAppBlocked: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(AppFilterOption.ALL) }
    var showSystemApps by remember { mutableStateOf(false) }

    val filteredApps = remember(installedApps, searchQuery, selectedFilter, showSystemApps, preferences.allowedPackages, preferences.blockedPackages) {
        installedApps.filter { app ->
            val matchesSearch = searchQuery.isBlank() ||
                    app.appName.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true)

            val matchesSystem = if (showSystemApps) true else !app.isSystemApp

            val isBlocked = preferences.blockedPackages.contains(app.packageName) || app.isFinancial
            val isAllowed = !isBlocked && (preferences.allowedPackages.isEmpty() || preferences.allowedPackages.contains(app.packageName))

            val matchesFilter = when (selectedFilter) {
                AppFilterOption.ALL -> true
                AppFilterOption.ALLOWED -> isAllowed
                AppFilterOption.BLOCKED -> isBlocked
                AppFilterOption.ORIGIN_PLAYER -> app.isOriginPlayerCandidate
            }

            matchesSearch && matchesSystem && matchesFilter
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
            text = "Chọn ứng dụng cho phép tích hợp và hiển thị trên Origin Island",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Tìm kiếm tên app hoặc package...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Xóa", modifier = Modifier.size(18.dp))
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppFilterOption.values().forEach { option ->
                FilterChip(
                    selected = selectedFilter == option,
                    onClick = { selectedFilter = option },
                    label = { Text(option.label, fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // System Apps Toggle
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
                Text("Hiện ứng dụng hệ thống", fontSize = 12.sp)
            }

            Text(
                text = "${filteredApps.size} ứng dụng",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Apps List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredApps, key = { it.packageName }) { app ->
                val isBlocked = preferences.blockedPackages.contains(app.packageName) || app.isFinancial
                val isAllowed = !isBlocked && (preferences.allowedPackages.isEmpty() || preferences.allowedPackages.contains(app.packageName))

                AppRowItem(
                    app = app,
                    isAllowed = isAllowed,
                    isBlocked = isBlocked,
                    onToggleAllowed = { allowed ->
                        if (allowed) {
                            onToggleAppBlocked(app.packageName, false)
                            onToggleAppAllowed(app.packageName, true)
                        } else {
                            onToggleAppBlocked(app.packageName, true)
                            onToggleAppAllowed(app.packageName, false)
                        }
                    },
                    context = context
                )
            }
        }
    }
}

@Composable
private fun AppRowItem(
    app: AppItem,
    isAllowed: Boolean,
    isBlocked: Boolean,
    onToggleAllowed: (Boolean) -> Unit,
    context: Context
) {
    var iconBitmap by remember(app.packageName) { mutableStateOf(iconCache[app.packageName]) }

    LaunchedEffect(app.packageName) {
        if (iconBitmap == null) {
            withContext(Dispatchers.IO) {
                val bitmap = loadAppIcon(context, app.packageName)
                iconCache[app.packageName] = bitmap
                withContext(Dispatchers.Main) {
                    iconBitmap = bitmap
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Real App Icon
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(44.dp)
            ) {
                if (iconBitmap != null) {
                    Image(
                        bitmap = iconBitmap!!,
                        contentDescription = app.appName,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = app.appName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // App Names and Badges
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    // Origin Player Indicator
                    if (app.isOriginPlayerCandidate) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = IslandAccent.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = IslandAccent,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "Origin Player",
                                    color = IslandAccent,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Financial Protection Tag
                    if (app.isFinancial) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = IslandError.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Bảo mật",
                                color = IslandError,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Switch or Lock Icon
            if (app.isFinancial) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Tự động khóa bảo vệ",
                    tint = IslandError,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Switch(
                    checked = isAllowed,
                    onCheckedChange = onToggleAllowed
                )
            }
        }
    }
}

private fun loadAppIcon(context: Context, packageName: String): ImageBitmap? {
    return try {
        val pm = context.packageManager
        val drawable: Drawable = pm.getApplicationIcon(packageName)
        drawableToBitmap(drawable)?.asImageBitmap()
    } catch (_: Throwable) {
        null
    }
}

private fun drawableToBitmap(drawable: Drawable): Bitmap? {
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        return drawable.bitmap
    }
    return try {
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    } catch (_: Throwable) {
        null
    }
}
