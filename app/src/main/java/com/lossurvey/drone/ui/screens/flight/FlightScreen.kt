package com.lossurvey.drone.ui.screens.flight

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lossurvey.drone.data.models.MissionStatus
import com.lossurvey.drone.data.models.SurveyType
import com.lossurvey.drone.ui.components.CaptureFlash
import com.lossurvey.drone.ui.components.MissionMapPreview
import com.lossurvey.drone.ui.components.SiteProgressItem
import com.lossurvey.drone.ui.components.TelemetryBar
import com.lossurvey.drone.ui.navigation.Routes
import com.lossurvey.drone.ui.theme.LOSColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightScreen(
    missionId: Long,
    navController: NavController,
    viewModel: FlightViewModel = hiltViewModel()
) {
    val droneState by viewModel.droneState.collectAsState()
    val executionState by viewModel.executionState.collectAsState()
    val showFlash by viewModel.flash.collectAsState()

    LaunchedEffect(missionId) {
        viewModel.startMission(missionId)
    }

    Box(Modifier.fillMaxSize().background(LOSColors.Background)) {
        Scaffold(
            containerColor = LOSColors.Background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "FLIGHT • ${executionState.mission?.name ?: ""}",
                            style = MaterialTheme.typography.displayMedium,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            viewModel.abort()
                            navController.popBackStack()
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = LOSColors.Surface1,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        MissionMapPreview(
                            droneState = droneState,
                            sites = executionState.mission?.sites ?: emptyList()
                        )
                    }
                    Card(
                        modifier = Modifier.weight(1f).fillMaxSize(),
                        colors = CardDefaults.cardColors(containerColor = LOSColors.Surface2),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, LOSColors.Border)
                    ) {
                        Column(Modifier.fillMaxSize().padding(14.dp)) {
                            Text(
                                "MISSION PROGRESS",
                                style = MaterialTheme.typography.labelLarge,
                                color = LOSColors.SecondaryText
                            )
                            Spacer(Modifier.height(8.dp))
                            val total = executionState.totalExpected.coerceAtLeast(1)
                            val done = executionState.totalCaptured
                            LinearProgressIndicator(
                                progress = (done.toFloat() / total).coerceIn(0f, 1f),
                                modifier = Modifier.fillMaxWidth(),
                                color = LOSColors.AccentWhite,
                                trackColor = LOSColors.Border
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "$done / $total captures",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                            executionState.currentSite?.let { site ->
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "ACTIVE SITE",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = LOSColors.SecondaryText
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "${site.siteId} • ${site.type.name}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                                Text(
                                    if (site.type == SurveyType.TOWER)
                                        "Tower offset @ ${site.distanceFromTower.toInt()}m, az ${site.azimuthDegrees?.toInt() ?: 0}°"
                                    else
                                        "Greenfield panorama @ ${site.surveyHeightM?.toInt() ?: 0}m",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LOSColors.SecondaryText
                                )
                            }

                            Spacer(Modifier.height(12.dp))
                            Text(
                                "SITES",
                                style = MaterialTheme.typography.labelLarge,
                                color = LOSColors.SecondaryText
                            )
                            Spacer(Modifier.height(4.dp))
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                contentPadding = PaddingValues(bottom = 8.dp)
                            ) {
                                val sites = executionState.mission?.sites ?: emptyList()
                                itemsIndexed(sites, key = { _, s -> s.id }) { index, site ->
                                    val isCurrent = index == executionState.currentSiteIndex &&
                                        executionState.status == MissionStatus.FLYING
                                    SiteProgressItem(
                                        site = site,
                                        isCurrent = isCurrent,
                                        capturesAtSite = if (isCurrent) executionState.capturedAtSite else site.capturedCount,
                                        expectedCaptures = if (site.type == SurveyType.TOWER)
                                            site.heights.size else 12
                                    )
                                }
                            }
                        }
                    }
                }

                TelemetryBar(state = droneState)

                Row(
                    Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.pause() },
                        border = BorderStroke(1.dp, LOSColors.Warning),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = null, tint = LOSColors.Warning)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "PAUSE",
                            color = LOSColors.Warning,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    OutlinedButton(
                        onClick = { viewModel.resume() },
                        border = BorderStroke(1.dp, LOSColors.Success),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = LOSColors.Success)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "RESUME",
                            color = LOSColors.Success,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Button(
                        onClick = {
                            viewModel.abort()
                            navController.popBackStack()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LOSColors.Error, contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "ABORT",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }

                if (executionState.status == MissionStatus.COMPLETED) {
                    Box(
                        Modifier.fillMaxWidth().background(LOSColors.Success).padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "MISSION COMPLETE — ${executionState.totalCaptured} captures",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Black
                            )
                            Spacer(Modifier.width(12.dp))
                            Button(
                                onClick = {
                                    val id = executionState.mission?.id ?: return@Button
                                    navController.navigate(Routes.report(id))
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black, contentColor = Color.White
                                )
                            ) {
                                Text(
                                    "VIEW REPORT",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        CaptureFlash(visible = showFlash)
    }
}
