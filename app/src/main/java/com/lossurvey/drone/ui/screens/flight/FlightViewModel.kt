package com.lossurvey.drone.ui.screens.flight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lossurvey.drone.data.models.DroneState
import com.lossurvey.drone.data.models.Mission
import com.lossurvey.drone.data.repository.MissionRepository
import com.lossurvey.drone.drone.DJIManager
import com.lossurvey.drone.drone.ExecutionState
import com.lossurvey.drone.drone.MissionExecutor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FlightViewModel @Inject constructor(
    private val executor: MissionExecutor,
    private val repository: MissionRepository,
    djiManager: DJIManager
) : ViewModel() {

    val droneState: StateFlow<DroneState> = djiManager.droneState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DroneState())

    val executionState: StateFlow<ExecutionState> = executor.executionState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExecutionState())

    private val _flash = MutableStateFlow(false)
    val flash: StateFlow<Boolean> = _flash.asStateFlow()

    init {
        viewModelScope.launch {
            executor.captureFlash.collectLatest {
                _flash.value = true
                kotlinx.coroutines.delay(180)
                _flash.value = false
            }
        }
    }

    fun startMission(missionId: Long, allowGpsFallback: Boolean = true) {
        viewModelScope.launch {
            val mission = repository.getMission(missionId) ?: return@launch
            executor.executeMission(mission, allowGpsFallback)
        }
    }

    fun pause() = executor.pause()
    fun resume() = executor.resume()
    fun abort() = executor.abort()

    suspend fun loadMission(id: Long): Mission? = repository.getMission(id)
}
