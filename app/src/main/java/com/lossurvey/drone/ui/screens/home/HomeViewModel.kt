package com.lossurvey.drone.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lossurvey.drone.data.models.DroneState
import com.lossurvey.drone.data.models.Mission
import com.lossurvey.drone.data.repository.MissionRepository
import com.lossurvey.drone.drone.DJIManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    djiManager: DJIManager,
    repository: MissionRepository
) : ViewModel() {

    val droneState: StateFlow<DroneState> = djiManager.droneState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DroneState())

    val missions: StateFlow<List<Mission>> = repository.observeMissions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
