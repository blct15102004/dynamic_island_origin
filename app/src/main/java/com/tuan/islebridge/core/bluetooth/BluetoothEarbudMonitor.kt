package com.tuan.islebridge.core.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.tuan.islebridge.core.originisland.CardPriority
import com.tuan.islebridge.core.originisland.IslandCardModel
import com.tuan.islebridge.core.originisland.IslandScene
import com.tuan.islebridge.core.originisland.OriginIslandAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BluetoothEarbudMonitor(
    private val context: Context,
    private val islandAdapter: OriginIslandAdapter
) {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val _currentDeviceState = MutableStateFlow<EarbudBatteryInfo?>(null)
    val currentDeviceState: StateFlow<EarbudBatteryInfo?> = _currentDeviceState.asStateFlow()

    private var isMonitoring = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            intent ?: return
            val action = intent.action ?: return

            val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            }

            when (action) {
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    device?.let { handleDeviceConnected(it) }
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    device?.let { handleDeviceDisconnected(it) }
                }
                "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED" -> {
                    val battery = intent.getIntExtra("android.bluetooth.device.extra.BATTERY_LEVEL", -1)
                    device?.let { handleBatteryChanged(it, if (battery >= 0) battery else null) }
                }
            }
        }
    }

    fun startMonitoring() {
        if (isMonitoring) return

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED")
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        }

        context.registerReceiver(receiver, filter)
        isMonitoring = true
        checkConnectedAudioDevices()
    }

    fun stopMonitoring() {
        if (!isMonitoring) return
        try {
            context.unregisterReceiver(receiver)
        } catch (_: Exception) {}
        isMonitoring = false
    }

    @SuppressLint("MissingPermission")
    private fun checkConnectedAudioDevices() {
        if (!hasBluetoothPermission()) return

        try {
            val connectedDevices = bluetoothAdapter?.bondedDevices.orEmpty()
            for (dev in connectedDevices) {
                // If device is an audio device / headset
                val deviceClass = dev.bluetoothClass?.majorDeviceClass
                if (deviceClass == 1024) { // AUDIO_VIDEO
                    // Read battery level if available via reflection or battery extra
                    val battery = readBatteryLevelApi(dev)
                    val name = dev.name ?: "Bluetooth Device"
                    val info = EarbudBatteryInfo(
                        deviceName = name,
                        deviceAddress = dev.address,
                        isConnected = true,
                        overallBattery = battery
                    )
                    _currentDeviceState.value = info
                    break
                }
            }
        } catch (_: Exception) {}
    }

    @SuppressLint("MissingPermission")
    private fun handleDeviceConnected(device: BluetoothDevice) {
        if (!hasBluetoothPermission()) return
        val name = device.name ?: "Tai nghe Bluetooth"
        val battery = readBatteryLevelApi(device)

        val info = EarbudBatteryInfo(
            deviceName = name,
            deviceAddress = device.address,
            isConnected = true,
            overallBattery = battery
        )
        _currentDeviceState.value = info

        val batteryText = if (battery != null) "$battery%" else "Đã kết nối"
        val card = IslandCardModel(
            id = "bt_${device.address}",
            sourcePackage = "android.bluetooth",
            scene = IslandScene.BLUETOOTH,
            title = name,
            subtitle = "Pin tai nghe: $batteryText",
            capsuleText = name,
            capsuleSubtext = batteryText,
            priority = CardPriority.NORMAL,
            timeoutMs = 15000L
        )
        islandAdapter.displayCard(card)
    }

    private fun handleDeviceDisconnected(device: BluetoothDevice) {
        val current = _currentDeviceState.value
        if (current?.deviceAddress == device.address) {
            _currentDeviceState.value = current.copy(isConnected = false)
        }
        islandAdapter.unmountCard("bt_${device.address}", IslandScene.BLUETOOTH)
    }

    @SuppressLint("MissingPermission")
    private fun handleBatteryChanged(device: BluetoothDevice, battery: Int?) {
        val current = _currentDeviceState.value
        val name = if (hasBluetoothPermission()) device.name ?: "Tai nghe" else "Tai nghe"

        val updated = (current ?: EarbudBatteryInfo(name, device.address, true)).copy(
            overallBattery = battery,
            lastUpdated = System.currentTimeMillis()
        )
        _currentDeviceState.value = updated

        val batteryText = if (battery != null) "$battery%" else "Unknown"
        val card = IslandCardModel(
            id = "bt_${device.address}",
            sourcePackage = "android.bluetooth",
            scene = IslandScene.BLUETOOTH,
            title = updated.deviceName,
            subtitle = "Cập nhật pin: $batteryText",
            capsuleText = updated.deviceName,
            capsuleSubtext = batteryText,
            priority = CardPriority.NORMAL,
            timeoutMs = 15000L
        )
        islandAdapter.displayCard(card)
    }

    private fun readBatteryLevelApi(device: BluetoothDevice): Int? {
        return try {
            val method = device.javaClass.getMethod("getBatteryLevel")
            val level = method.invoke(device) as? Int
            if (level != null && level >= 0) level else null
        } catch (_: Exception) {
            null
        }
    }

    fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
        }
    }
}

