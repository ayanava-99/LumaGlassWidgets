package com.lumaglass.widgets.glance

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import kotlin.math.roundToInt

data class DeviceTelemetry(
    val batteryPercent: Int,
    val isCharging: Boolean,
    val storageUsedGb: Float,
    val storageTotalGb: Float,
    val ramUsedGb: Float,
    val ramTotalGb: Float,
    val isWifiConnected: Boolean,
    val wifiSsidOrStatus: String,
    val brightnessPercent: Int
) {
    val storagePercent: Int
        get() = if (storageTotalGb > 0f) ((storageUsedGb / storageTotalGb) * 100f).roundToInt().coerceIn(0, 100) else 0

    val ramPercent: Int
        get() = if (ramTotalGb > 0f) ((ramUsedGb / ramTotalGb) * 100f).roundToInt().coerceIn(0, 100) else 0
}

object DeviceTelemetryReader {
    fun read(context: Context): DeviceTelemetry {
        val batteryStatus = runCatching {
            context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        }.getOrNull()

        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val batteryPct = if (level >= 0 && scale > 0) {
            ((level * 100f) / scale).roundToInt().coerceIn(0, 100)
        } else {
            batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)?.coerceIn(0, 100) ?: 82
        }

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        // Internal storage via StatFs
        val statFs = runCatching { StatFs(Environment.getDataDirectory().path) }.getOrNull()
        val totalBytes = statFs?.totalBytes ?: 128L * 1024 * 1024 * 1024
        val availBytes = statFs?.availableBytes ?: 48L * 1024 * 1024 * 1024
        val bytesPerGb = 1024f * 1024f * 1024f
        val storageTotalGb = totalBytes / bytesPerGb
        val storageUsedGb = (totalBytes - availBytes).coerceAtLeast(0L) / bytesPerGb

        // RAM via ActivityManager
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memInfo)
        val ramTotalGb = (memInfo.totalMem.takeIf { it > 0L } ?: (8L * 1024 * 1024 * 1024)) / bytesPerGb
        val ramAvailGb = (memInfo.availMem.takeIf { it > 0L } ?: (3L * 1024 * 1024 * 1024)) / bytesPerGb
        val ramUsedGb = (ramTotalGb - ramAvailGb).coerceAtLeast(0.5f)

        // Wi-Fi / Network state
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val caps = cm?.getNetworkCapabilities(cm.activeNetwork)
        val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val wifiMgr = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val wifiEnabled = runCatching { wifiMgr?.isWifiEnabled == true }.getOrDefault(false)
        val wifiLabel = when {
            isWifi -> "Wi-Fi Connected"
            wifiEnabled -> "Wi-Fi On"
            else -> "Wi-Fi Off"
        }

        // Screen brightness (0..255)
        val rawBrightness = runCatching {
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        }.getOrDefault(150)
        val brightnessPct = ((rawBrightness / 255f) * 100f).roundToInt().coerceIn(5, 100)

        return DeviceTelemetry(
            batteryPercent = batteryPct,
            isCharging = isCharging,
            storageUsedGb = storageUsedGb,
            storageTotalGb = storageTotalGb,
            ramUsedGb = ramUsedGb,
            ramTotalGb = ramTotalGb,
            isWifiConnected = isWifi,
            wifiSsidOrStatus = wifiLabel,
            brightnessPercent = brightnessPct
        )
    }
}
