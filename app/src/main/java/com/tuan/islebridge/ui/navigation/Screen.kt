package com.tuan.islebridge.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Tổng quan", Icons.Default.Dashboard)
    data object Island : Screen("island", "Origin Island", Icons.Default.BubbleChart)
    data object Bluetooth : Screen("bluetooth", "Bluetooth", Icons.Default.Bluetooth)
    data object Otp : Screen("otp", "Mã OTP", Icons.Default.Pin)
    data object Applications : Screen("applications", "Ứng dụng", Icons.Default.Apps)
    data object Permissions : Screen("permissions", "Quyền hạn", Icons.Default.Security)
    data object Privacy : Screen("privacy", "Quyền riêng tư", Icons.Default.Visibility)
    data object Settings : Screen("settings", "Cài đặt", Icons.Default.Settings)
}

val bottomNavScreens = listOf(
    Screen.Home,
    Screen.Island,
    Screen.Bluetooth,
    Screen.Otp,
    Screen.Applications,
    Screen.Permissions,
    Screen.Privacy,
    Screen.Settings
)

