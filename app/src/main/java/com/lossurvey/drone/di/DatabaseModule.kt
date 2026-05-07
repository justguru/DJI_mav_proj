package com.lossurvey.drone.di

import android.content.Context
import androidx.room.Room
import com.lossurvey.drone.data.db.AppDatabase
import com.lossurvey.drone.data.db.dao.CaptureLogDao
import com.lossurvey.drone.data.db.dao.MissionDao
import com.lossurvey.drone.data.db.dao.SiteDao
import com.lossurvey.drone.data.repository.MissionRepository
import com.lossurvey.drone.data.repository.MissionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideMissionDao(db: AppDatabase): MissionDao = db.missionDao()
    @Provides fun provideSiteDao(db: AppDatabase): SiteDao = db.siteDao()
    @Provides fun provideCaptureLogDao(db: AppDatabase): CaptureLogDao = db.captureLogDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindMissionRepository(impl: MissionRepositoryImpl): MissionRepository
}
