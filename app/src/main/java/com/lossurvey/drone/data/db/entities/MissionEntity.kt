package com.lossurvey.drone.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "missions")
data class MissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val status: String,
    val createdAt: Long,
    val completedAt: Long? = null,
    val projectFolderPath: String,
    val totalSites: Int,
    val completedSites: Int = 0
)
