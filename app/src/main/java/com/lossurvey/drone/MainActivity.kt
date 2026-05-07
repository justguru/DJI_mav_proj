package com.lossurvey.drone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.lossurvey.drone.ui.navigation.AppNavigation
import com.lossurvey.drone.ui.theme.LOSColors
import com.lossurvey.drone.ui.theme.LOSSurveyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LOSSurveyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize().background(LOSColors.Background),
                    color = LOSColors.Background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
