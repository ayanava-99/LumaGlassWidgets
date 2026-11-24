package com.lumaglass.widgets.glance

import android.app.ActivityManager
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.provider.Settings
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

data class DeviceTelemetry(
    val batteryPercent: Int,
    val isCharging: Boolean,
    val batteryTempCelsius: Float,
    val batteryVoltageVolts: Float,
    val storageUsedGb: Float,
    val storageTotalGb: Float,
    val ramUsedGb: Float,
    val ramTotalGb: Float,
    val isWifiConnected: Boolean,
    val wifiSsidOrStatus: String,
    val brightnessPercent: Int,
    val nextAlarmFormatted: String,
    val uptimeFormatted: String,
    val deviceModelLabel: String
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

        val rawTempTenths = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 315) ?: 315
        val tempC = (rawTempTenths / 10f).coerceIn(15f, 65f)

        val rawVoltageMv = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4120) ?: 4120
        val voltageV = if (rawVoltageMv > 100) rawVoltageMv / 1000f else 4.12f

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

        // Next scheduled system alarm
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val nextAlarmClock = runCatching { alarmMgr?.nextAlarmClock }.getOrNull()
        val nextAlarmFormatted = if (nextAlarmClock != null && nextAlarmClock.triggerTime > 0L) {
            val zdt = Instant.ofEpochMilli(nextAlarmClock.triggerTime).atZone(ZoneId.systemDefault())
            zdt.format(DateTimeFormatter.ofPattern("EEE HH:mm"))
        } else {
            "No alarm set"
        }

        // System uptime since boot
        val uptimeTotalMins = (SystemClock.elapsedRealtime() / 60_000L).coerceAtLeast(1L)
        val uptimeDays = uptimeTotalMins / (60 * 24)
        val uptimeHours = (uptimeTotalMins % (60 * 24)) / 60
        val uptimeMins = uptimeTotalMins % 60
        val uptimeFormatted = when {
            uptimeDays > 0 -> "${uptimeDays}d ${uptimeHours}h ${uptimeMins}m"
            uptimeHours > 0 -> "${uptimeHours}h ${uptimeMins}m"
            else -> "${uptimeMins}m"
        }

        val deviceModelLabel = "${Build.MODEL ?: "Android"} • API ${Build.VERSION.SDK_INT}"

        return DeviceTelemetry(
            batteryPercent = batteryPct,
            isCharging = isCharging,
            batteryTempCelsius = tempC,
            batteryVoltageVolts = voltageV,
            storageUsedGb = storageUsedGb,
            storageTotalGb = storageTotalGb,
            ramUsedGb = ramUsedGb,
            ramTotalGb = ramTotalGb,
            isWifiConnected = isWifi,
            wifiSsidOrStatus = wifiLabel,
            brightnessPercent = brightnessPct,
            nextAlarmFormatted = nextAlarmFormatted,
            uptimeFormatted = uptimeFormatted,
            deviceModelLabel = deviceModelLabel
        )
    }
}
