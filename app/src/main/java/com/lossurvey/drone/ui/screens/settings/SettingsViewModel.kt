package com.lossurvey.drone.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lossurvey.drone.data.models.DroneState
import com.lossurvey.drone.data.preferences.AppPreferences
import com.lossurvey.drone.data.preferences.AppSettings
import com.lossurvey.drone.drone.DJIManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: AppPreferences,
    djiManager: DJIManager
) : ViewModel() {

    val settings: StateFlow<AppSettings> = prefs.flow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), prefs.current)

    val droneState: StateFlow<DroneState> = djiManager.droneState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DroneState())

    fun setSimulatorMode(value: Boolean) = viewModelScope.launch { prefs.setSimulatorMode(value) }
    fun setMinBattery(value: Int) = viewModelScope.launch { prefs.setMinBattery(value) }
    fun setCriticalBattery(value: Int) = viewModelScope.launch { prefs.setCriticalBattery(value) }
    fun setMinGpsSatellites(value: Int) = viewModelScope.launch { prefs.setMinGpsSatellites(value) }
    fun setAllowGpsFallback(value: Boolean) = viewModelScope.launch { prefs.setAllowGpsFallback(value) }
    fun resetDefaults() = viewModelScope.launch { prefs.resetToDefaults() }
}
