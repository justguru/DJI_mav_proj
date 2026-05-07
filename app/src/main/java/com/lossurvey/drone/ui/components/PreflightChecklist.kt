package com.lossurvey.drone.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lossurvey.drone.ui.theme.LOSColors

data class PreflightCheck(
    val label: String,
    val passed: Boolean,
    val detail: String = ""
)

@Composable
fun PreflightChecklistDialog(
    checks: List<PreflightCheck>,
    onProceed: () -> Unit,
    onCancel: () -> Unit
) {
    val allPassed = checks.all { it.passed }
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = LOSColors.Surface2,
        title = {
            Text(
                "PRE-FLIGHT CHECKS",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                checks.forEach { check ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (check.passed)
                                Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (check.passed) LOSColors.Success else LOSColors.Error,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                check.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                            if (!check.passed && check.detail.isNotEmpty()) {
                                Text(
                                    check.detail,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LOSColors.Error
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onProceed,
                enabled = allPassed,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    disabledContainerColor = LOSColors.Disabled,
                    disabledContentColor = LOSColors.SecondaryText
                )
            ) {
                Text("START MISSION", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onCancel,
                border = BorderStroke(1.dp, LOSColors.Border)
            ) {
                Text("CANCEL", color = Color.White, style = MaterialTheme.typography.labelLarge)
            }
        }
    )
}
