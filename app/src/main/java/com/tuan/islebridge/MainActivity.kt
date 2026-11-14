package com.tuan.islebridge

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tuan.islebridge.core.bluetooth.BluetoothEarbudMonitor
import com.tuan.islebridge.core.common.CompatibilityDetector
import com.tuan.islebridge.core.datastore.IsleBridgePreferences
import com.tuan.islebridge.core.datastore.UserPreferencesRepository
import com.tuan.islebridge.core.notifications.AppFilterManager
import com.tuan.islebridge.core.notifications.IsleBridgeNotificationListenerService
import com.tuan.islebridge.core.originisland.OriginIslandAdapter
import com.tuan.islebridge.core.otp.ClipboardHelper
import com.tuan.islebridge.core.otp.OtpDetectorEngine
import com.tuan.islebridge.core.privacy.PrivacyMonitor
import com.tuan.islebridge.core.updater.GithubOtaUpdater
import com.tuan.islebridge.core.updater.ReleaseInfo
import com.tuan.islebridge.core.updater.UpdateCheckResult
import com.tuan.islebridge.ui.navigation.Screen
import com.tuan.islebridge.ui.navigation.bottomNavScreens
import com.tuan.islebridge.ui.screens.*
import com.tuan.islebridge.ui.theme.IsleBridgeTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var preferencesRepo: UserPreferencesRepository
    private lateinit var islandAdapter: OriginIslandAdapter
    private lateinit var bluetoothMonitor: BluetoothEarbudMonitor
    private lateinit var privacyMonitor: PrivacyMonitor
    private lateinit var otaUpdater: GithubOtaUpdater

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* refreshed via onResume */ }

    private val requestBluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) bluetoothMonitor.startMonitoring()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as IsleBridgeApp
        islandAdapter = app.islandAdapter
        preferencesRepo = UserPreferencesRepository(this)
        bluetoothMonitor = BluetoothEarbudMonitor(this, islandAdapter)
        privacyMonitor = PrivacyMonitor(this, islandAdapter)
        otaUpdater = GithubOtaUpdater(this)

        handleIncomingIntent(intent)

        setContent {
            val preferences by preferencesRepo.preferencesFlow.collectAsState(initial = IsleBridgePreferences())
            val shizukuStatus by app.shizukuManager.statusFlow.collectAsState()
            val bluetoothInfo by bluetoothMonitor.currentDeviceState.collectAsState()
            val volatileOtpEvents by OtpDetectorEngine.volatileOtpEvents.collectAsState()
            val activeCards by islandAdapter.priorityQueue.activeCardsFlow.collectAsState()
            val downloadState by otaUpdater.downloadState.collectAsState()
            val lastPrivacyEvent by privacyMonitor.lastPrivacyEvent.collectAsState()

            var updateCheckResult by remember { mutableStateOf<UpdateCheckResult?>(null) }
            var isCheckingUpdate by remember { mutableStateOf(false) }

            val compatibility = remember { CompatibilityDetector.detectCompatibility() }
            val privacyCapability = remember { privacyMonitor.checkPrivacyCapability() }
            val installedApps = remember { loadInstalledApps() }

            val navController = rememberNavController()

            IsleBridgeTheme(appThemeSetting = preferences.appTheme) {
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentRoute = navBackStackEntry?.destination?.route

                            bottomNavScreens.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = screen.title) },
                                    label = { Text(screen.title, maxLines = 1) },
                                    selected = currentRoute == screen.route,
                                    onClick = {
                                        if (currentRoute != screen.route) {
                                            navController.navigate(screen.route) {
                                                popUpTo(Screen.Home.route) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                preferences = preferences,
                                compatibility = compatibility,
                                shizukuStatus = shizukuStatus,
                                bluetoothInfo = bluetoothInfo,
                                activeCards = activeCards,
                                onToggleIsland = { enabled ->
                                    lifecycleScope.launch { preferencesRepo.updateIslandEnabled(enabled) }
                                },
                                onToggleOverlayFallback = { enabled ->
                                    lifecycleScope.launch { preferencesRepo.updateOverlayFallback(enabled) }
                                },
                                onOpenPermissions = {
                                    navController.navigate(Screen.Permissions.route)
                                }
                            )
                        }

                        composable(Screen.Island.route) {
                            IslandScreen(
                                preferences = preferences,
                                onToggleForceNative = { forced ->
                                    lifecycleScope.launch { preferencesRepo.updateForceNativeIsland(forced) }
                                },
                                onToggleOverlayFallback = { enabled ->
                                    lifecycleScope.launch { preferencesRepo.updateOverlayFallback(enabled) }
                                },
                                onUpdateTimeout = { timeout ->
                                    lifecycleScope.launch { preferencesRepo.updateOtpTimeout(timeout) }
                                },
                                onTriggerTestCard = { card ->
                                    islandAdapter.displayCard(
                                        card = card,
                                        forceNative = preferences.forceNativeIsland,
                                        useOverlay = preferences.isOverlayFallbackEnabled
                                    )
                                },
                                onClearAllCards = {
                                    islandAdapter.priorityQueue.clear()
                                }
                            )
                        }

                        composable(Screen.Bluetooth.route) {
                            BluetoothScreen(
                                preferences = preferences,
                                bluetoothInfo = bluetoothInfo,
                                hasBluetoothPermission = bluetoothMonitor.hasBluetoothPermission(),
                                onToggleBluetoothMonitoring = { enabled ->
                                    lifecycleScope.launch {
                                        preferencesRepo.updateBluetoothMonitoring(enabled)
                                        if (enabled) bluetoothMonitor.startMonitoring() else bluetoothMonitor.stopMonitoring()
                                    }
                                },
                                onToggleBluetoothMode = { alwaysShow ->
                                    lifecycleScope.launch { preferencesRepo.updateBluetoothMode(alwaysShow) }
                                },
                                onUpdateTimeout = { timeout ->
                                    lifecycleScope.launch { preferencesRepo.updateBluetoothTimeout(timeout) }
                                },
                                onRequestPermission = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        requestBluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                                    }
                                }
                            )
                        }

                        composable(Screen.Otp.route) {
                            OtpScreen(
                                preferences = preferences,
                                volatileOtpEvents = volatileOtpEvents,
                                onToggleOtpDetection = { enabled ->
                                    lifecycleScope.launch { preferencesRepo.updateOtpDetection(enabled) }
                                },
                                onToggleAutoCopy = { enabled ->
                                    lifecycleScope.launch { preferencesRepo.updateOtpAutoCopy(enabled) }
                                },
                                onCopyOtp = { code ->
                                    ClipboardHelper.copyOtpToClipboard(this@MainActivity, code, preferences.otpClearClipboardTimeoutMs)
                                    Toast.makeText(this@MainActivity, "Đã sao chép: $code", Toast.LENGTH_SHORT).show()
                                },
                                onClearHistory = {
                                    OtpDetectorEngine.clearVolatileHistory()
                                }
                            )
                        }

                        composable(Screen.Applications.route) {
                            ApplicationsScreen(
                                preferences = preferences,
                                installedApps = installedApps,
                                onToggleAppAllowed = { pkg, allowed ->
                                    lifecycleScope.launch { preferencesRepo.toggleAppAllowed(pkg, allowed) }
                                },
                                onToggleAppBlocked = { pkg, blocked ->
                                    lifecycleScope.launch { preferencesRepo.toggleAppBlocked(pkg, blocked) }
                                }
                            )
                        }

                        composable(Screen.Permissions.route) {
                            PermissionsScreen(
                                hasNotificationListener = IsleBridgeNotificationListenerService.isConnected,
                                hasOverlayPermission = Settings.canDrawOverlays(this@MainActivity),
                                hasPostNotification = hasPostNotificationPermission(),
                                hasBluetoothPermission = bluetoothMonitor.hasBluetoothPermission(),
                                isBatteryOptimizedExempt = isBatteryOptimizationExempt(),
                                shizukuStatus = shizukuStatus,
                                onRequestNotificationListener = {
                                    startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                                },
                                onRequestOverlay = {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:$packageName")
                                    )
                                    startActivity(intent)
                                },
                                onRequestPostNotification = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                },
                                onRequestBluetooth = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        requestBluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                                    }
                                },
                                onRequestBatteryExemption = {
                                    requestBatteryOptimizationExemption()
                                },
                                onRequestShizuku = {
                                    app.shizukuManager.requestPermission()
                                },
                                onOpenAutoStartSettings = {
                                    openApplicationDetails()
                                }
                            )
                        }

                        composable(Screen.Privacy.route) {
                            PrivacyScreen(
                                preferences = preferences,
                                capability = privacyCapability,
                                lastPrivacyEvent = lastPrivacyEvent,
                                onTogglePrivacyAlert = { enabled ->
                                    lifecycleScope.launch { preferencesRepo.updatePrivacyAlert(enabled) }
                                },
                                onTriggerTestAlert = { card ->
                                    islandAdapter.displayCard(card)
                                }
                            )
                        }

                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                preferences = preferences,
                                downloadState = downloadState,
                                updateCheckResult = updateCheckResult,
                                isCheckingUpdate = isCheckingUpdate,
                                onSelectTheme = { theme ->
                                    lifecycleScope.launch { preferencesRepo.updateAppTheme(theme) }
                                },
                                onCheckForUpdates = {
                                    isCheckingUpdate = true
                                    lifecycleScope.launch {
                                        updateCheckResult = otaUpdater.checkForUpdates()
                                        isCheckingUpdate = false
                                    }
                                },
                                onDownloadUpdate = { release ->
                                    lifecycleScope.launch {
                                        otaUpdater.downloadAndVerifyApk(release)
                                    }
                                },
                                onInstallUpdate = { path ->
                                    otaUpdater.installApk(path)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val app = application as IsleBridgeApp
        app.shizukuManager.updateStatus()
        lifecycleScope.launch {
            val prefs = preferencesRepo.preferencesFlow.first()
            if (prefs.isBluetoothMonitoringEnabled && bluetoothMonitor.hasBluetoothPermission()) {
                bluetoothMonitor.startMonitoring()
            }
        }
    }

    override fun onDestroy() {
        bluetoothMonitor.stopMonitoring()
        privacyMonitor.destroy()
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIncomingIntent(it) }
    }

    private fun handleIncomingIntent(intent: Intent) {
        val otpToCopy = intent.getStringExtra("EXTRA_ACTION_COPY_OTP")
        if (!otpToCopy.isNullOrBlank()) {
            ClipboardHelper.copyOtpToClipboard(this, otpToCopy)
            Toast.makeText(this, "Đã sao chép mã OTP: $otpToCopy", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadInstalledApps(): List<AppItem> {
        val pm = packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return packages.map { appInfo ->
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val appName = pm.getApplicationLabel(appInfo).toString()
            val isFinancial = AppFilterManager.isFinancialApp(appInfo.packageName)
            AppItem(
                packageName = appInfo.packageName,
                appName = appName,
                isSystemApp = isSystem,
                isFinancial = isFinancial
            )
        }.sortedBy { it.appName.lowercase() }
    }

    private fun hasPostNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun isBatteryOptimizationExempt(): Boolean {
        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(packageName) ?: false
    }

    @SuppressLint("BatteryLife")
    private fun requestBatteryOptimizationExemption() {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        } catch (_: Exception) {
            openApplicationDetails()
        }
    }

    private fun openApplicationDetails() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }
}

