package com.tuan.islebridge.core.tile

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.tuan.islebridge.MainActivity
import com.tuan.islebridge.core.datastore.UserPreferencesRepository
import com.tuan.islebridge.core.notifications.IsleBridgeNotificationListenerService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

@RequiresApi(Build.VERSION_CODES.N)
class IsleBridgeTileService : TileService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var preferencesRepo: UserPreferencesRepository

    override fun onCreate() {
        super.onCreate()
        preferencesRepo = UserPreferencesRepository(applicationContext)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        serviceScope.launch {
            val prefs = preferencesRepo.preferencesFlow.first()
            val newEnabled = !prefs.isIslandEnabled
            preferencesRepo.updateIslandEnabled(newEnabled)
            updateTileState()

            // If notification listener is not connected, open permissions/main screen
            if (!IsleBridgeNotificationListenerService.isConnected) {
                val intent = Intent(applicationContext, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    // PendingIntent for Android 14+
                    val pending = android.app.PendingIntent.getActivity(
                        applicationContext,
                        0,
                        intent,
                        android.app.PendingIntent.FLAG_IMMUTABLE
                    )
                    startActivityAndCollapse(pending)
                } else {
                    @Suppress("DEPRECATION")
                    startActivityAndCollapse(intent)
                }
            }
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        serviceScope.launch {
            val prefs = preferencesRepo.preferencesFlow.first()
            val isListenerActive = IsleBridgeNotificationListenerService.isConnected

            if (prefs.isIslandEnabled && isListenerActive) {
                tile.state = Tile.STATE_ACTIVE
                tile.label = "IsleBridge (Bật)"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = "Đang chạy ngầm"
                }
            } else if (prefs.isIslandEnabled) {
                tile.state = Tile.STATE_INACTIVE
                tile.label = "IsleBridge"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = "Cần cấp quyền Listener"
                }
            } else {
                tile.state = Tile.STATE_INACTIVE
                tile.label = "IsleBridge (Tắt)"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = "Tạm dừng"
                }
            }
            tile.updateTile()
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}

