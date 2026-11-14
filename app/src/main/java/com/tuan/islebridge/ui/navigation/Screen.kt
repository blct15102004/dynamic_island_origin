package com.tuan.islebridge.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Tổng quan", Icons.Default.Home)
    data object Island : Screen("island", "Origin Island", Icons.Default.Tune)
    data object Bluetooth : Screen("bluetooth", "Bluetooth", Icons.Default.Bluetooth)
    data object Otp : Screen("otp", "Mã OTP", Icons.Default.Pin)
    data object Applications : Screen("applications", "Ứng dụng", Icons.Default.Apps)
    data object Permissions : Screen("permissions", "Cấp quyền", Icons.Default.Security)
    data object Privacy : Screen("privacy", "Riêng tư", Icons.Default.Visibility)
    data object Settings : Screen("settings", "Cài đặt", Icons.Default.Settings)
}

val bottomNavScreens = listOf(
    Screen.Home,
    Screen.Island,
    Screen.Permissions,
    Screen.Applications,
    Screen.Settings
)
