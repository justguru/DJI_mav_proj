package com.lossurvey.drone.di

import android.content.Context
import com.lossurvey.drone.drone.CameraController
import com.lossurvey.drone.drone.DJIManager
import com.lossurvey.drone.drone.RTKManager
import com.lossurvey.drone.drone.WaypointMissionBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DroneModule {

    @Provides @Singleton
    fun provideDJIManager(@ApplicationContext ctx: Context): DJIManager = DJIManager(ctx)

    @Provides @Singleton
    fun provideRTKManager(@ApplicationContext ctx: Context, dji: DJIManager): RTKManager =
        RTKManager(ctx, dji)

    @Provides @Singleton
    fun provideWaypointBuilder(): WaypointMissionBuilder = WaypointMissionBuilder()

    @Provides @Singleton
    fun provideCameraController(@ApplicationContext ctx: Context): CameraController =
        CameraController(ctx)
}
