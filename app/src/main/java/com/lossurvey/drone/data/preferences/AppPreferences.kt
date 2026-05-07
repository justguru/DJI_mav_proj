package com.lossurvey.drone.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore("los_settings")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val store get() = context.settingsDataStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _flow = MutableStateFlow(AppSettings.DEFAULT)
    val flow: StateFlow<AppSettings> = _flow.asStateFlow()

    val current: AppSettings get() = _flow.value

    init {
        scope.launch {
            store.data.map { it.toSettings() }.collect { _flow.value = it }
        }
    }

    suspend fun setSimulatorMode(value: Boolean) =
        store.edit { it[Keys.SIMULATOR_MODE] = value }

    suspend fun setMinBattery(value: Int) =
        store.edit { it[Keys.MIN_BATTERY] = value.clamp(AppSettings.MIN_BATTERY_RANGE_LOW, AppSettings.MIN_BATTERY_RANGE_HIGH) }

    suspend fun setCriticalBattery(value: Int) =
        store.edit { it[Keys.CRITICAL_BATTERY] = value.clamp(AppSettings.CRITICAL_BATTERY_RANGE_LOW, AppSettings.CRITICAL_BATTERY_RANGE_HIGH) }

    suspend fun setMinGpsSatellites(value: Int) =
        store.edit { it[Keys.MIN_GPS_SAT] = value.clamp(AppSettings.GPS_RANGE_LOW, AppSettings.GPS_RANGE_HIGH) }

    suspend fun setAllowGpsFallback(value: Boolean) =
        store.edit { it[Keys.ALLOW_GPS_FALLBACK] = value }

    suspend fun resetToDefaults() {
        store.edit { p ->
            p[Keys.SIMULATOR_MODE] = AppSettings.DEFAULT.simulatorMode
            p[Keys.MIN_BATTERY] = AppSettings.DEFAULT.minBatteryPercent
            p[Keys.CRITICAL_BATTERY] = AppSettings.DEFAULT.criticalBatteryPercent
            p[Keys.MIN_GPS_SAT] = AppSettings.DEFAULT.minGpsSatellites
            p[Keys.ALLOW_GPS_FALLBACK] = AppSettings.DEFAULT.allowGpsFallback
        }
    }

    private fun Preferences.toSettings() = AppSettings(
        simulatorMode = this[Keys.SIMULATOR_MODE] ?: AppSettings.DEFAULT.simulatorMode,
        minBatteryPercent = this[Keys.MIN_BATTERY] ?: AppSettings.DEFAULT.minBatteryPercent,
        criticalBatteryPercent = this[Keys.CRITICAL_BATTERY] ?: AppSettings.DEFAULT.criticalBatteryPercent,
        minGpsSatellites = this[Keys.MIN_GPS_SAT] ?: AppSettings.DEFAULT.minGpsSatellites,
        allowGpsFallback = this[Keys.ALLOW_GPS_FALLBACK] ?: AppSettings.DEFAULT.allowGpsFallback
    )

    private object Keys {
        val SIMULATOR_MODE = booleanPreferencesKey("simulator_mode")
        val MIN_BATTERY = intPreferencesKey("min_battery_pct")
        val CRITICAL_BATTERY = intPreferencesKey("critical_battery_pct")
        val MIN_GPS_SAT = intPreferencesKey("min_gps_sat")
        val ALLOW_GPS_FALLBACK = booleanPreferencesKey("allow_gps_fallback")
    }

    private fun Int.clamp(low: Int, high: Int) = coerceIn(low, high)
}
