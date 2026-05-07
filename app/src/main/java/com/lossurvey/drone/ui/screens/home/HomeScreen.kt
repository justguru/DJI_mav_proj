package com.lossurvey.drone.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lossurvey.drone.ui.components.MissionCard
import com.lossurvey.drone.ui.components.PulsingDot
import com.lossurvey.drone.ui.components.OfflineDot
import com.lossurvey.drone.ui.components.DroneStatusCard
import com.lossurvey.drone.ui.navigation.Routes
import com.lossurvey.drone.ui.theme.LOSColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val droneState by viewModel.droneState.collectAsState()
    val missions by viewModel.missions.collectAsState()

    Scaffold(
        containerColor = LOSColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "LOS SURVEY",
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LOSColors.Surface1,
                    titleContentColor = Color.White
                ),
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (droneState.isConnected) PulsingDot(
                            if (droneState.rtkLocked) LOSColors.Success else LOSColors.Warning
                        ) else OfflineDot()
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (droneState.isConnected) "${droneState.batteryPercent}%" else "OFFLINE",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Spacer(Modifier.width(20.dp))
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Routes.Upload) },
                containerColor = Color.White,
                contentColor = Color.Black,
                icon = { Icon(Icons.Default.Add, null) },
                text = {
                    Text(
                        "NEW MISSION",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Black
                    )
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            DroneStatusCard(state = droneState)
            Spacer(Modifier.height(20.dp))

            Text(
                "MISSIONS",
                style = MaterialTheme.typography.titleLarge,
                color = LOSColors.SecondaryText,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (missions.isEmpty()) {
                Box(
                    Modifier.fillMaxWidth().height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "NO MISSIONS YET. TAP NEW MISSION TO IMPORT A SURVEY FILE.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LOSColors.SecondaryText
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    itemsIndexed(missions, key = { _, m -> m.id }) { index, mission ->
                        val visible = remember {
                            MutableTransitionState(false).apply { targetState = true }
                        }
                        AnimatedVisibility(
                            visibleState = visible,
                            enter = fadeIn(tween(280, delayMillis = index * 45)) +
                                slideInVertically(
                                    animationSpec = tween(280, delayMillis = index * 45),
                                    initialOffsetY = { it / 3 }
                                )
                        ) {
                            MissionCard(mission = mission) {
                                navController.navigate(Routes.missionDetail(mission.id))
                            }
                        }
                    }
                }
            }
        }
    }
}
