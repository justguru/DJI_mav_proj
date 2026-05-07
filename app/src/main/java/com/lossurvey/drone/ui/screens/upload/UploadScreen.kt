package com.lossurvey.drone.ui.screens.upload

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lossurvey.drone.ui.navigation.Routes
import com.lossurvey.drone.ui.theme.LOSColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    navController: NavController,
    viewModel: UploadViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val pickFile = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.parseFile(uri, context)
    }

    Scaffold(
        containerColor = LOSColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "UPLOAD SURVEY FILE",
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
            Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = LOSColors.Surface2),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, LOSColors.Border)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "1. SELECT FILE",
                        style = MaterialTheme.typography.labelLarge,
                        color = LOSColors.SecondaryText
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "CSV or XLSX. App auto-detects Tower vs Greenfield by columns.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LOSColors.SecondaryText
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = {
                            pickFile.launch(arrayOf(
                                "text/csv",
                                "text/comma-separated-values",
                                "application/vnd.ms-excel",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                "application/octet-stream",
                                "*/*"
                            ))
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White, contentColor = Color.Black
                        )
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "PICK CSV / XLSX",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Black
                        )
                    }
                }
            }

            if (state.isParsing) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = LOSColors.AccentWhite, strokeWidth = 2.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Parsing file…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }

            state.error?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = LOSColors.Surface2),
                    border = BorderStroke(1.dp, LOSColors.Error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        it,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = LOSColors.Error
                    )
                }
            }

            state.parserResult?.let { parsed ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = LOSColors.Surface2),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, LOSColors.Border)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            "2. REVIEW",
                            style = MaterialTheme.typography.labelLarge,
                            color = LOSColors.SecondaryText
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Detected ${parsed.surveyType.name} survey • ${parsed.sites.size} sites",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Spacer(Modifier.height(8.dp))
                        if (parsed.validationErrors.isNotEmpty()) {
                            Text(
                                "${parsed.validationErrors.size} validation issues — these rows will be skipped or flagged.",
                                style = MaterialTheme.typography.bodySmall,
                                color = LOSColors.Warning
                            )
                            Spacer(Modifier.height(6.dp))
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .background(LOSColors.Surface3, RoundedCornerShape(6.dp)),
                                contentPadding = PaddingValues(8.dp)
                            ) {
                                items(parsed.validationErrors) { err ->
                                    Text(
                                        "• $err",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LOSColors.Warning
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        OutlinedTextField(
                            value = state.missionName,
                            onValueChange = viewModel::setMissionName,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("MISSION NAME", color = LOSColors.SecondaryText) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LOSColors.AccentWhite,
                                unfocusedBorderColor = LOSColors.Border,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = LOSColors.AccentWhite
                            )
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        border = BorderStroke(1.dp, LOSColors.Border),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "CANCEL",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Button(
                        onClick = {
                            viewModel.saveMission(context) { id ->
                                navController.navigate(Routes.missionDetail(id)) {
                                    popUpTo(Routes.Home)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White, contentColor = Color.Black
                        ),
                        enabled = !state.isSaving && state.missionName.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                color = Color.Black, strokeWidth = 2.dp,
                                modifier = Modifier.height(18.dp).width(18.dp)
                            )
                        } else {
                            Text(
                                "SAVE MISSION",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(LOSColors.Surface1, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    "Tower CSV columns: Site_ID, Latitude, Longitude, Tower_Lat, Tower_Lon, Heights_M, Azimuth_Degrees, Camera_Type, Distance_From_Tower_M\n" +
                        "Greenfield CSV columns: Site_ID, Latitude, Longitude, Survey_Height_M",
                    style = MaterialTheme.typography.bodySmall,
                    color = LOSColors.SecondaryText
                )
            }
        }
    }
}
