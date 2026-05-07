package com.lossurvey.drone.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lossurvey.drone.data.db.dao.CaptureLogDao
import com.lossurvey.drone.data.db.dao.MissionDao
import com.lossurvey.drone.data.db.dao.SiteDao
import com.lossurvey.drone.data.db.entities.CaptureLogEntity
import com.lossurvey.drone.data.db.entities.MissionEntity
import com.lossurvey.drone.data.db.entities.SiteEntity

@Database(
    entities = [MissionEntity::class, SiteEntity::class, CaptureLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun missionDao(): MissionDao
    abstract fun siteDao(): SiteDao
    abstract fun captureLogDao(): CaptureLogDao

    companion object {
        const val DATABASE_NAME = "los_survey.db"
    }
}
