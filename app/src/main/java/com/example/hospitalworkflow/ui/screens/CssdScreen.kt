package com.example.hospitalworkflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sanitizer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hospitalworkflow.data.model.InstrumentTray
import com.example.hospitalworkflow.data.model.OperatingTheatre
import com.example.hospitalworkflow.data.model.Surgery
import com.example.hospitalworkflow.ui.components.BlockingReasonBanner
import com.example.hospitalworkflow.ui.components.StatusBadge
import com.example.hospitalworkflow.ui.components.TrayScannerDialog
import com.example.ui.theme.Emerald600
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.Purple600
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900

@Composable
fun CssdScreen(
    trays: List<InstrumentTray>,
    surgeries: List<Surgery>,
    ots: List<OperatingTheatre>,
    onUpdateTrayState: (trayId: String, newState: String) -> Unit,
    onValidateTray: (trayId: String, isValidated: Boolean) -> Unit,
    onDispatchTray: (trayId: String, otId: String, surgeryId: String) -> Unit
) {
    var showScannerDialog by remember { mutableStateOf(false) }
    var selectedTrayForDispatch by remember { mutableStateOf<InstrumentTray?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredTrays = if (searchQuery.isBlank()) trays else trays.filter {
        it.id.contains(searchQuery, ignoreCase = true) || it.trayType.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Central Sterile Supply Department (CSSD)",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Track sterilisation lifecycle, tray validation, & OT dispatch",
                            fontSize = 12.sp,
                            color = Slate500,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { showScannerDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scan Tray ID", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Pipeline Stats Summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PipelineCounter(
                        label = "Cleaning",
                        count = trays.count { it.currentState == "CLEANING" || it.currentState == "DECONTAMINATION" },
                        modifier = Modifier.weight(1f)
                    )
                    PipelineCounter(
                        label = "Sterilising",
                        count = trays.count { it.currentState == "STERILIZATION" || it.currentState == "PACKAGING" },
                        modifier = Modifier.weight(1f)
                    )
                    PipelineCounter(
                        label = "Storage",
                        count = trays.count { it.currentState == "STERILE_STORAGE" },
                        modifier = Modifier.weight(1f)
                    )
                    PipelineCounter(
                        label = "Dispatched",
                        count = trays.count { it.currentState == "DISPATCHED" || it.currentState == "IN_OT" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        items(filteredTrays, key = { it.id }) { tray ->
            val assignedSurgery = surgeries.find { it.id == tray.assignedSurgeryId }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                color = Slate900,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = tray.id,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tray.trayType,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Slate900,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Location: ${tray.location}",
                                    fontSize = 12.sp,
                                    color = Slate500,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusBadge(status = tray.validationStatus)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (assignedSurgery != null) {
                        Surface(
                            color = Slate100,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Assigned Case: ${assignedSurgery.procedure} (${assignedSurgery.id} • ${assignedSurgery.otId})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate700,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Pipeline Stage Row Buttons
                    Text(
                        text = "Advance Sterilisation Lifecycle Stage:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate700
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StageButton("Decon", tray.currentState == "DECONTAMINATION", modifier = Modifier.weight(1f)) {
                            onUpdateTrayState(tray.id, "DECONTAMINATION")
                        }
                        StageButton("Clean", tray.currentState == "CLEANING", modifier = Modifier.weight(1f)) {
                            onUpdateTrayState(tray.id, "CLEANING")
                        }
                        StageButton("Sterilise", tray.currentState == "STERILIZATION", modifier = Modifier.weight(1f)) {
                            onUpdateTrayState(tray.id, "STERILIZATION")
                        }
                        StageButton("Storage", tray.currentState == "STERILE_STORAGE", modifier = Modifier.weight(1f)) {
                            onUpdateTrayState(tray.id, "STERILE_STORAGE")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dispatch & Validate Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { onValidateTray(tray.id, true) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Emerald600, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Validate Quality & BI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald600,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = { selectedTrayForDispatch = tray },
                            enabled = tray.validationStatus == "READY" && (tray.currentState == "STERILE_STORAGE" || tray.currentState == "DISPATCHED"),
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Dispatch to OT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

    if (showScannerDialog) {
        TrayScannerDialog(
            onTrayScanned = { scannedId ->
                searchQuery = scannedId
                showScannerDialog = false
            },
            onDismiss = { showScannerDialog = false }
        )
    }

    // Dispatch Modal
    selectedTrayForDispatch?.let { tray ->
        var targetOtId by remember { mutableStateOf(tray.assignedOtId ?: "OT-02") }
        val matchingSurgeries = surgeries.filter { it.otId == targetOtId }
        var targetSurgeryId by remember { mutableStateOf(matchingSurgeries.firstOrNull()?.id ?: "S-102") }

        AlertDialog(
            onDismissRequest = { selectedTrayForDispatch = null },
            title = { Text(text = "Dispatch Tray ${tray.id} to OT", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(text = "Tray Type: ${tray.trayType}", fontSize = 13.sp)
                    Text(text = "Validation: READY • Biological Indicator PASSED", fontSize = 12.sp, color = Emerald600, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Select Destination Theatre:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("OT-01", "OT-02", "OT-03", "OT-04").forEach { otId ->
                            Button(
                                onClick = {
                                    targetOtId = otId
                                    targetSurgeryId = surgeries.find { it.otId == otId }?.id ?: "S-101"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = if (targetOtId == otId) MedicalBlue else Slate100),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(otId, fontSize = 11.sp, color = if (targetOtId == otId) Color.White else Slate900)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Target Surgery Case: $targetSurgeryId", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate700)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDispatchTray(tray.id, targetOtId, targetSurgeryId)
                        selectedTrayForDispatch = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue)
                ) {
                    Text("Confirm Dispatch")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedTrayForDispatch = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PipelineCounter(
    label: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = "$count",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Slate900,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Slate500,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StageButton(
    label: String,
    isCurrent: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = if (isCurrent) MedicalBlue else Slate100),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
        modifier = modifier.height(36.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isCurrent) Color.White else Slate700,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
