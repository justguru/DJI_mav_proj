package com.lossurvey.drone.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lossurvey.drone.ui.screens.flight.FlightScreen
import com.lossurvey.drone.ui.screens.home.HomeScreen
import com.lossurvey.drone.ui.screens.mission.MissionDetailScreen
import com.lossurvey.drone.ui.screens.report.ReportScreen
import com.lossurvey.drone.ui.screens.settings.SettingsScreen
import com.lossurvey.drone.ui.screens.upload.UploadScreen

object Routes {
    const val Home = "home"
    const val Upload = "upload"
    const val Settings = "settings"
    const val MissionDetail = "mission/{id}"
    const val Flight = "flight/{id}"
    const val Report = "report/{id}"

    fun missionDetail(id: Long) = "mission/$id"
    fun flight(id: Long) = "flight/$id"
    fun report(id: Long) = "report/$id"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Home,
        enterTransition = { fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.97f) },
        exitTransition = { fadeOut(tween(220)) + scaleOut(tween(220), targetScale = 0.97f) },
        popEnterTransition = { fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 1.03f) },
        popExitTransition = { fadeOut(tween(220)) + scaleOut(tween(220), targetScale = 1.03f) }
    ) {
        composable(Routes.Home) { HomeScreen(navController) }
        composable(Routes.Upload) { UploadScreen(navController) }
        composable(Routes.Settings) { SettingsScreen(navController) }
        composable(
            Routes.MissionDetail,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { back ->
            MissionDetailScreen(
                missionId = back.arguments?.getLong("id") ?: 0L,
                navController = navController
            )
        }
        composable(
            Routes.Flight,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { back ->
            FlightScreen(
                missionId = back.arguments?.getLong("id") ?: 0L,
                navController = navController
            )
        }
        composable(
            Routes.Report,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { back ->
            ReportScreen(
                missionId = back.arguments?.getLong("id") ?: 0L,
                navController = navController
            )
        }
    }
}
