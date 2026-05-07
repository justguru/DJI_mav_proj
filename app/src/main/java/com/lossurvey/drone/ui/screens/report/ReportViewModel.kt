package com.lossurvey.drone.ui.screens.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lossurvey.drone.data.db.entities.CaptureLogEntity
import com.lossurvey.drone.data.models.Mission
import com.lossurvey.drone.data.repository.MissionRepository
import com.lossurvey.drone.report.PdfReportGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repository: MissionRepository,
    private val pdfGenerator: PdfReportGenerator
) : ViewModel() {

    private val _state = MutableStateFlow(ReportState())
    val state: StateFlow<ReportState> = _state.asStateFlow()

    fun load(missionId: Long) {
        viewModelScope.launch {
            val mission = repository.getMission(missionId)
            val captures = repository.getCaptures(missionId)
            _state.value = _state.value.copy(mission = mission, captures = captures)
        }
    }

    fun generatePdf(missionId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(generating = true, error = null)
            try {
                val mission = repository.getMission(missionId) ?: error("Mission not found")
                val captures = repository.getCaptures(missionId)
                val file = withContext(Dispatchers.IO) {
                    pdfGenerator.generateReport(
                        mission = mission,
                        sites = mission.sites,
                        captures = captures,
                        projectFolder = mission.projectFolderPath
                    )
                }
                _state.value = _state.value.copy(generating = false, pdfFile = file)
            } catch (e: Exception) {
                _state.value = _state.value.copy(generating = false, error = e.message)
            }
        }
    }
}

data class ReportState(
    val mission: Mission? = null,
    val captures: List<CaptureLogEntity> = emptyList(),
    val generating: Boolean = false,
    val pdfFile: File? = null,
    val error: String? = null
)
