package com.lossurvey.drone.data.models

data class Mission(
    val id: Long = 0,
    val name: String,
    val type: SurveyType,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val status: MissionStatus = MissionStatus.PENDING,
    val sites: List<Site> = emptyList(),
    val projectFolderPath: String = ""
)

enum class MissionStatus { PENDING, PREFLIGHT, FLYING, PAUSED, COMPLETED, ABORTED }
