package com.lossurvey.drone.ui.screens.upload

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lossurvey.drone.data.models.Mission
import com.lossurvey.drone.data.models.MissionStatus
import com.lossurvey.drone.data.parser.ParserResult
import com.lossurvey.drone.data.parser.SurveyFileParser
import com.lossurvey.drone.data.repository.MissionRepository
import com.lossurvey.drone.storage.ProjectFolderManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val parser: SurveyFileParser,
    private val repository: MissionRepository,
    private val projectFolderManager: ProjectFolderManager
) : ViewModel() {

    private val _state = MutableStateFlow(UploadState())
    val state: StateFlow<UploadState> = _state.asStateFlow()

    fun setMissionName(name: String) {
        _state.value = _state.value.copy(missionName = name)
    }

    fun parseFile(uri: Uri, context: Context) {
        _state.value = _state.value.copy(isParsing = true, error = null)
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { parser.parseFile(uri, context) }
            when (result) {
                is ParserResult.Success -> {
                    val suggestedName = result.fileName.substringBeforeLast('.')
                    _state.value = _state.value.copy(
                        isParsing = false,
                        parserResult = result,
                        sourceUri = uri,
                        missionName = _state.value.missionName.ifBlank { suggestedName }
                    )
                }
                is ParserResult.Error -> _state.value = _state.value.copy(
                    isParsing = false,
                    error = result.message,
                    parserResult = null
                )
            }
        }
    }

    fun saveMission(context: Context, onSaved: (Long) -> Unit) {
        val current = _state.value
        val parsed = current.parserResult as? ParserResult.Success ?: return
        val name = current.missionName.ifBlank { parsed.fileName.substringBeforeLast('.') }
        val uri = current.sourceUri ?: return

        viewModelScope.launch {
            _state.value = current.copy(isSaving = true)
            val folderPath = withContext(Dispatchers.IO) {
                projectFolderManager.createProjectFolder(name).also { path ->
                    projectFolderManager.saveInputFile(uri, path, "survey_input.${parsed.fileName.substringAfterLast('.', "csv")}")
                }
            }
            val mission = Mission(
                name = name,
                type = parsed.surveyType,
                status = MissionStatus.PENDING,
                projectFolderPath = folderPath
            )
            val id = repository.saveMission(mission, parsed.sites)
            _state.value = _state.value.copy(isSaving = false, savedMissionId = id)
            onSaved(id)
        }
    }
}

data class UploadState(
    val missionName: String = "",
    val sourceUri: Uri? = null,
    val parserResult: ParserResult.Success? = null,
    val isParsing: Boolean = false,
    val isSaving: Boolean = false,
    val savedMissionId: Long? = null,
    val error: String? = null
)
