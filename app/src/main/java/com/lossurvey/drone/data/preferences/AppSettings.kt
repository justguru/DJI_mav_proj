package com.lossurvey.drone.data.preferences

data class AppSettings(
    val simulatorMode: Boolean = true,
    val minBatteryPercent: Int = 30,
    val criticalBatteryPercent: Int = 20,
    val minGpsSatellites: Int = 4,
    val allowGpsFallback: Boolean = false
) {
    companion object {
        val DEFAULT = AppSettings()
        const val MIN_BATTERY_RANGE_LOW = 10
        const val MIN_BATTERY_RANGE_HIGH = 50
        const val CRITICAL_BATTERY_RANGE_LOW = 5
        const val CRITICAL_BATTERY_RANGE_HIGH = 25
        const val GPS_RANGE_LOW = 1
        const val GPS_RANGE_HIGH = 12
    }
}
