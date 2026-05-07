package com.lossurvey.drone.ui.screens.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lossurvey.drone.data.preferences.AppSettings
import com.lossurvey.drone.ui.theme.LOSColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val droneState by viewModel.droneState.collectAsState()
    val flying = droneState.isFlying

    Scaffold(
        containerColor = LOSColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SETTINGS",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LOSColors.Surface1,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (flying) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(LOSColors.Warning.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        "Drone is flying. Changes apply on the next mission.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LOSColors.Warning
                    )
                }
            }

            SettingsSection(title = "SIMULATOR") {
                ToggleRow(
                    label = "Simulator mode",
                    description = "Mock telemetry + synthetic captures. Disable on real hardware.",
                    checked = settings.simulatorMode,
                    onChange = viewModel::setSimulatorMode
                )
            }

            SettingsSection(title = "PRE-FLIGHT THRESHOLDS") {
                SliderRow(
                    label = "Minimum battery",
                    valueText = "${settings.minBatteryPercent}%",
                    value = settings.minBatteryPercent.toFloat(),
                    range = AppSettings.MIN_BATTERY_RANGE_LOW.toFloat()..AppSettings.MIN_BATTERY_RANGE_HIGH.toFloat(),
                    steps = AppSettings.MIN_BATTERY_RANGE_HIGH - AppSettings.MIN_BATTERY_RANGE_LOW - 1,
                    description = "Mission start blocked below this level."
                ) { viewModel.setMinBattery(it.toInt()) }

                SliderRow(
                    label = "Minimum GPS satellites",
                    valueText = "${settings.minGpsSatellites}",
                    value = settings.minGpsSatellites.toFloat(),
                    range = AppSettings.GPS_RANGE_LOW.toFloat()..AppSettings.GPS_RANGE_HIGH.toFloat(),
                    steps = AppSettings.GPS_RANGE_HIGH - AppSettings.GPS_RANGE_LOW - 1,
                    description = "GPS lock required before take-off."
                ) { viewModel.setMinGpsSatellites(it.toInt()) }

                ToggleRow(
                    label = "Allow GPS fallback (default)",
                    description = "Pre-checks the operator override on the pre-flight dialog when RTK is searching.",
                    checked = settings.allowGpsFallback,
                    onChange = viewModel::setAllowGpsFallback
                )
            }

            SettingsSection(title = "ALERTS") {
                SliderRow(
                    label = "Critical battery (RTH trigger)",
                    valueText = "${settings.criticalBatteryPercent}%",
                    value = settings.criticalBatteryPercent.toFloat(),
                    range = AppSettings.CRITICAL_BATTERY_RANGE_LOW.toFloat()..AppSettings.CRITICAL_BATTERY_RANGE_HIGH.toFloat(),
                    steps = AppSettings.CRITICAL_BATTERY_RANGE_HIGH - AppSettings.CRITICAL_BATTERY_RANGE_LOW - 1,
                    description = "Telemetry monitor raises a critical alert at or below this level."
                ) { viewModel.setCriticalBattery(it.toInt()) }
            }

            OutlinedButton(
                onClick = { viewModel.resetDefaults() },
                border = BorderStroke(1.dp, LOSColors.Border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text(
                    "RESET TO DEFAULTS",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = LOSColors.SecondaryText,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LOSColors.Surface2),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, LOSColors.Border)
        ) {
            Column(
                Modifier.padding(PaddingValues(horizontal = 16.dp, vertical = 8.dp))
            ) {
                content()
            }
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = LOSColors.SecondaryText
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = LOSColors.AccentWhite,
                checkedBorderColor = LOSColors.AccentWhite,
                uncheckedThumbColor = LOSColors.SecondaryText,
                uncheckedTrackColor = LOSColors.Surface3,
                uncheckedBorderColor = LOSColors.Border
            )
        )
    }
}

@Composable
private fun SliderRow(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    description: String,
    onChange: (Float) -> Unit
) {
    Column(Modifier.padding(vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            Text(
                valueText,
                style = MaterialTheme.typography.titleMedium,
                color = LOSColors.AccentWhite,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            description,
            style = MaterialTheme.typography.bodySmall,
            color = LOSColors.SecondaryText
        )
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            steps = steps.coerceAtLeast(0),
            colors = SliderDefaults.colors(
                thumbColor = LOSColors.AccentWhite,
                activeTrackColor = LOSColors.AccentWhite,
                inactiveTrackColor = LOSColors.Border,
                activeTickColor = LOSColors.Surface3,
                inactiveTickColor = LOSColors.Surface3
            )
        )
    }
}
