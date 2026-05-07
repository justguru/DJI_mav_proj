package com.lossurvey.drone.ui.screens.mission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lossurvey.drone.data.models.DroneState
import com.lossurvey.drone.data.models.Mission
import com.lossurvey.drone.data.repository.MissionRepository
import com.lossurvey.drone.drone.DJIManager
import com.lossurvey.drone.drone.MissionExecutor
import com.lossurvey.drone.drone.RTKManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MissionDetailViewModel @Inject constructor(
    private val repository: MissionRepository,
    djiManager: DJIManager,
    private val rtkManager: RTKManager,
    private val executor: MissionExecutor
) : ViewModel() {

    val droneState: StateFlow<DroneState> = djiManager.droneState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DroneState())

    fun missionFlow(id: Long): StateFlow<Mission?> =
        repository.observeMission(id)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun preflight(mission: Mission, allowGpsFallback: Boolean) =
        executor.validatePreflight(mission, allowGpsFallback)

    fun rtkLocked(): Boolean = rtkManager.isLocked
}
