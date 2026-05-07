package com.lossurvey.drone.ui.screens.report

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.lossurvey.drone.ui.theme.LOSColors
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    missionId: Long,
    navController: NavController,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(missionId) { viewModel.load(missionId) }

    Scaffold(
        containerColor = LOSColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "REPORT • ${state.mission?.name ?: ""}",
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
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = LOSColors.Surface2),
                border = BorderStroke(1.dp, LOSColors.Border),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "SUMMARY",
                        style = MaterialTheme.typography.labelLarge,
                        color = LOSColors.SecondaryText
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${state.mission?.type?.name ?: "—"} • ${state.mission?.sites?.size ?: 0} sites • ${state.captures.size} captures",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        "Folder: ${state.mission?.projectFolderPath ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LOSColors.SecondaryText
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { viewModel.generatePdf(missionId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White, contentColor = Color.Black
                            ),
                            enabled = !state.generating
                        ) {
                            if (state.generating) {
                                CircularProgressIndicator(
                                    color = Color.Black,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.height(18.dp).width(18.dp)
                                )
                            } else {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "GENERATE PDF",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.Black
                            )
                        }
                        state.pdfFile?.let { file ->
                            OutlinedButton(
                                onClick = { sharePdf(context, file) },
                                border = BorderStroke(1.dp, LOSColors.Border)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "SHARE PDF",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                    state.error?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            it,
                            color = LOSColors.Error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    state.pdfFile?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "PDF: ${it.absolutePath}",
                            style = MaterialTheme.typography.bodySmall,
                            color = LOSColors.SecondaryText
                        )
                    }
                }
            }

            Text(
                "CAPTURED IMAGES",
                style = MaterialTheme.typography.titleLarge,
                color = LOSColors.SecondaryText
            )
            if (state.captures.isEmpty()) {
                Box(
                    Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No captures yet. Run the mission first.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LOSColors.SecondaryText
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(180.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(state.captures) { capture ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = LOSColors.Surface2),
                            border = BorderStroke(1.dp, LOSColors.Border),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column {
                                AsyncImage(
                                    model = capture.imagePath,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9f),
                                    contentScale = ContentScale.Crop
                                )
                                Column(Modifier.padding(8.dp)) {
                                    Text(
                                        capture.siteId,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White
                                    )
                                    Text(
                                        "Az ${capture.azimuthDegrees.toInt()}° • Alt ${capture.altitudeM.toInt()}m",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LOSColors.SecondaryText
                                    )
                                    Text(
                                        "RTK ±${"%.3f".format(capture.rtkAccuracyM)}m • Bat ${capture.batteryPercent}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LOSColors.SecondaryText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun sharePdf(context: android.content.Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share report"))
}
