package com.lossurvey.drone.ui.screens.mission

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lossurvey.drone.data.models.MissionStatus
import com.lossurvey.drone.data.models.SurveyType
import com.lossurvey.drone.ui.components.MissionMapPreview
import com.lossurvey.drone.ui.components.PreflightCheck
import com.lossurvey.drone.ui.components.PreflightChecklistDialog
import com.lossurvey.drone.ui.components.SiteProgressItem
import com.lossurvey.drone.ui.components.StatusChip
import com.lossurvey.drone.ui.navigation.Routes
import com.lossurvey.drone.ui.theme.LOSColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionDetailScreen(
    missionId: Long,
    navController: NavController,
    viewModel: MissionDetailViewModel = hiltViewModel()
) {
    val droneState by viewModel.droneState.collectAsState()
    val missionFlow = viewModel.missionFlow(missionId)
    val mission by missionFlow.collectAsState()
    var showPreflight by remember { mutableStateOf(false) }
    var allowGps by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = LOSColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        mission?.name?.uppercase() ?: "MISSION",
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = { mission?.let { StatusChip(it.status); Spacer(Modifier.width(16.dp)) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LOSColors.Surface1,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        val m = mission ?: return@Scaffold
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = LOSColors.Surface2),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, LOSColors.Border)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        SummaryRow("TYPE", m.type.name)
                        SummaryRow("SITES", "${m.sites.size}")
                        val captures = if (m.type == SurveyType.TOWER)
                            m.sites.sumOf { it.heights.size }
                        else m.sites.size * 12
                        SummaryRow("CAPTURES (planned)", "$captures")
                        SummaryRow(
                            "STATUS", m.status.name,
                            valueColor = when (m.status) {
                                MissionStatus.COMPLETED -> LOSColors.Success
                                MissionStatus.FLYING -> LOSColors.Warning
                                MissionStatus.ABORTED -> LOSColors.Error
                                else -> Color.White
                            }
                        )
                        SummaryRow("FOLDER", m.projectFolderPath, mono = true)
                    }
                }
                Box(
                    Modifier.weight(1f).height(220.dp)
                ) {
                    MissionMapPreview(droneState = droneState, sites = m.sites)
                }
            }

            Text(
                "SITES",
                style = MaterialTheme.typography.titleLarge,
                color = LOSColors.SecondaryText
            )
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 96.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(m.sites, key = { it.id }) { site ->
                    SiteProgressItem(
                        site = site,
                        isCurrent = false,
                        capturesAtSite = site.capturedCount,
                        expectedCaptures = if (site.type == SurveyType.TOWER) site.heights.size else 12
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { navController.navigate(Routes.report(m.id)) },
                    border = BorderStroke(1.dp, LOSColors.Border),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "VIEW REPORT",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Button(
                    onClick = { showPreflight = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White, contentColor = Color.Black
                    ),
                    modifier = Modifier.weight(1f),
                    enabled = m.status != MissionStatus.FLYING
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "PREFLIGHT & FLY",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Black
                    )
                }
            }
        }
    }

    if (showPreflight) {
        val checks = buildList {
            add(PreflightCheck(
                "Drone connected", droneState.isConnected,
                if (!droneState.isConnected) "No DJI product detected" else ""
            ))
            add(PreflightCheck(
                "Battery ≥ 30%", droneState.batteryPercent >= 30,
                "Battery is ${droneState.batteryPercent}%"
            ))
            add(PreflightCheck(
                "GPS ≥ 4/5", droneState.gpsSignal >= 4,
                "GPS ${droneState.gpsSignal}/5"
            ))
            add(PreflightCheck(
                "RTK fixed (or GPS fallback approved)",
                viewModel.rtkLocked() || allowGps,
                "RTK searching — toggle below to allow GPS fallback"
            ))
            add(PreflightCheck(
                "Sites configured", mission!!.sites.isNotEmpty(),
                "No sites in mission"
            ))
        }
        PreflightChecklistDialog(
            checks = checks,
            onProceed = {
                showPreflight = false
                navController.navigate(Routes.flight(missionId))
            },
            onCancel = { showPreflight = false }
        )
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    mono: Boolean = false,
    valueColor: Color = Color.White
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            modifier = Modifier.width(140.dp),
            style = MaterialTheme.typography.labelMedium,
            color = LOSColors.SecondaryText
        )
        Text(
            value,
            style = if (mono) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
            color = valueColor,
            modifier = Modifier.weight(1f)
        )
    }
}
