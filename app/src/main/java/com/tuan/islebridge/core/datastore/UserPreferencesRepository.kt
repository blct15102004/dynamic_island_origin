package com.tuan.islebridge.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.tuan.islebridge.core.common.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.PREF_DATASTORE_NAME)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val KEY_ISLAND_ENABLED = booleanPreferencesKey("island_enabled")
        val KEY_FORCE_NATIVE_ISLAND = booleanPreferencesKey("force_native_island")
        val KEY_NOTIF_MIRRORING_ENABLED = booleanPreferencesKey("notif_mirroring_enabled")
        val KEY_BLUETOOTH_MONITORING = booleanPreferencesKey("bluetooth_monitoring")
        val KEY_BLUETOOTH_MODE_ALWAYS_SHOW = booleanPreferencesKey("bluetooth_mode_always_show")
        val KEY_BLUETOOTH_TIMEOUT_MS = longPreferencesKey("bluetooth_timeout_ms")
        val KEY_OTP_DETECTION = booleanPreferencesKey("otp_detection")
        val KEY_OTP_AUTO_COPY = booleanPreferencesKey("otp_auto_copy")
        val KEY_OTP_TIMEOUT_MS = longPreferencesKey("otp_timeout_ms")
        val KEY_OTP_CLEAR_CLIPBOARD_TIMEOUT_MS = longPreferencesKey("otp_clear_clipboard_timeout_ms")
        val KEY_PRIVACY_ALERT = booleanPreferencesKey("privacy_alert")
        val KEY_SHIZUKU_ENABLED = booleanPreferencesKey("shizuku_enabled")
        val KEY_KEEP_ORIGINAL_NOTIFS = booleanPreferencesKey("keep_original_notifs")
        val KEY_APP_THEME = stringPreferencesKey("app_theme")
        val KEY_ALLOWED_PACKAGES = stringSetPreferencesKey("allowed_packages")
        val KEY_BLOCKED_PACKAGES = stringSetPreferencesKey("blocked_packages")
    }

    val preferencesFlow: Flow<IsleBridgePreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val defaultBlocked = IsleBridgePreferences().blockedPackages
            IsleBridgePreferences(
                isIslandEnabled = preferences[PreferencesKeys.KEY_ISLAND_ENABLED] ?: true,
                forceNativeIsland = preferences[PreferencesKeys.KEY_FORCE_NATIVE_ISLAND] ?: false,
                isNotificationMirroringEnabled = preferences[PreferencesKeys.KEY_NOTIF_MIRRORING_ENABLED] ?: true,
                isBluetoothMonitoringEnabled = preferences[PreferencesKeys.KEY_BLUETOOTH_MONITORING] ?: true,
                bluetoothModeAlwaysShow = preferences[PreferencesKeys.KEY_BLUETOOTH_MODE_ALWAYS_SHOW] ?: false,
                bluetoothTimeoutMs = preferences[PreferencesKeys.KEY_BLUETOOTH_TIMEOUT_MS] ?: Constants.DEFAULT_TIMEOUT_BLUETOOTH_MS,
                isOtpDetectionEnabled = preferences[PreferencesKeys.KEY_OTP_DETECTION] ?: true,
                isOtpAutoCopyEnabled = preferences[PreferencesKeys.KEY_OTP_AUTO_COPY] ?: true,
                otpTimeoutMs = preferences[PreferencesKeys.KEY_OTP_TIMEOUT_MS] ?: Constants.DEFAULT_TIMEOUT_OTP_MS,
                otpClearClipboardTimeoutMs = preferences[PreferencesKeys.KEY_OTP_CLEAR_CLIPBOARD_TIMEOUT_MS] ?: Constants.DEFAULT_TIMEOUT_OTP_MS,
                isPrivacyAlertEnabled = preferences[PreferencesKeys.KEY_PRIVACY_ALERT] ?: true,
                isShizukuEnabled = preferences[PreferencesKeys.KEY_SHIZUKU_ENABLED] ?: false,
                keepOriginalNotifications = preferences[PreferencesKeys.KEY_KEEP_ORIGINAL_NOTIFS] ?: true,
                appTheme = preferences[PreferencesKeys.KEY_APP_THEME] ?: "SYSTEM",
                allowedPackages = preferences[PreferencesKeys.KEY_ALLOWED_PACKAGES] ?: emptySet(),
                blockedPackages = preferences[PreferencesKeys.KEY_BLOCKED_PACKAGES] ?: defaultBlocked
            )
        }

    suspend fun updateIslandEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.KEY_ISLAND_ENABLED] = enabled }
    }

    suspend fun updateForceNativeIsland(forced: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.KEY_FORCE_NATIVE_ISLAND] = forced }
    }

    suspend fun updateNotificationMirroring(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.KEY_NOTIF_MIRRORING_ENABLED] = enabled }
    }

    suspend fun updateBluetoothMonitoring(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.KEY_BLUETOOTH_MONITORING] = enabled }
    }

    suspend fun updateBluetoothMode(alwaysShow: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.KEY_BLUETOOTH_MODE_ALWAYS_SHOW] = alwaysShow }
    }

    suspend fun updateBluetoothTimeout(timeoutMs: Long) {
        context.dataStore.edit { it[PreferencesKeys.KEY_BLUETOOTH_TIMEOUT_MS] = timeoutMs }
    }

    suspend fun updateOtpDetection(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.KEY_OTP_DETECTION] = enabled }
    }

    suspend fun updateOtpAutoCopy(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.KEY_OTP_AUTO_COPY] = enabled }
    }

    suspend fun updateOtpTimeout(timeoutMs: Long) {
        context.dataStore.edit { it[PreferencesKeys.KEY_OTP_TIMEOUT_MS] = timeoutMs }
    }

    suspend fun updatePrivacyAlert(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.KEY_PRIVACY_ALERT] = enabled }
    }

    suspend fun updateShizukuEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.KEY_SHIZUKU_ENABLED] = enabled }
    }

    suspend fun updateKeepOriginalNotifications(keep: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.KEY_KEEP_ORIGINAL_NOTIFS] = keep }
    }

    suspend fun updateAppTheme(theme: String) {
        context.dataStore.edit { it[PreferencesKeys.KEY_APP_THEME] = theme }
    }

    suspend fun toggleAppAllowed(packageName: String, allowed: Boolean) {
        context.dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.KEY_ALLOWED_PACKAGES]?.toMutableSet() ?: mutableSetOf()
            if (allowed) current.add(packageName) else current.remove(packageName)
            prefs[PreferencesKeys.KEY_ALLOWED_PACKAGES] = current
        }
    }

    suspend fun toggleAppBlocked(packageName: String, blocked: Boolean) {
        context.dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.KEY_BLOCKED_PACKAGES]?.toMutableSet() ?: mutableSetOf()
            if (blocked) current.add(packageName) else current.remove(packageName)
            prefs[PreferencesKeys.KEY_BLOCKED_PACKAGES] = current
        }
    }
}
