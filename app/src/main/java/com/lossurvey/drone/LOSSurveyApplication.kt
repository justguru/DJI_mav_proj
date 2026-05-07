package com.lossurvey.drone

import android.app.Application
import com.lossurvey.drone.drone.DJIManager
import com.lossurvey.drone.drone.RTKManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LOSSurveyApplication : Application() {

    @Inject lateinit var djiManager: DJIManager
    @Inject lateinit var rtkManager: RTKManager

    override fun onCreate() {
        super.onCreate()
        djiManager.init()
        rtkManager.init()
    }

    override fun onTerminate() {
        djiManager.shutdown()
        super.onTerminate()
    }
}
