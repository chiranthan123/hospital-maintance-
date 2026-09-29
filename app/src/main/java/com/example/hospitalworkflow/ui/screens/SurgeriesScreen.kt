package com.example.hospitalworkflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hospitalworkflow.data.model.InstrumentTray
import com.example.hospitalworkflow.data.model.OperatingTheatre
import com.example.hospitalworkflow.data.model.Patient
import com.example.hospitalworkflow.data.model.Surgery
import com.example.hospitalworkflow.ui.components.BlockingReasonBanner
import com.example.hospitalworkflow.ui.components.StatusBadge
import com.example.hospitalworkflow.ui.components.WorkflowTimelineStepper
import com.example.ui.theme.Crimson100
import com.example.ui.theme.Crimson600
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald600
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900

@Composable
fun SurgeriesScreen(
    surgeries: List<Surgery>,
    patients: List<Patient>,
    ots: List<OperatingTheatre>,
    trays: List<InstrumentTray>,
    onStartSurgery: (String) -> Unit,
    onCompleteSurgery: (String) -> Unit,
    onNavigateToCssd: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Operating Theatre Surgeries",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "Track surgical readiness, dependency checks, and active workflow transitions",
                    fontSize = 13.sp,
                    color = Slate500
                )
            }
        }

        items(surgeries, key = { it.id }) { surgery ->
            val patient = patients.find { it.id == surgery.patientId }
            val ot = ots.find { it.id == surgery.otId }
            val tray = surgery.assignedTrayId?.let { trayId -> trays.find { it.id == trayId } }

            val isPatientReady = patient?.state == "READY" || patient?.state == "IN_TRANSIT" || patient?.state == "IN_OT"
            val isOtReady = ot?.status == "AVAILABLE" || ot?.status == "PREPARING" || ot?.status == "SURGERY_ACTIVE" || ot?.status == "PATIENT_IN_OT"
            val isTrayReady = tray?.validationStatus == "READY" && tray.sterilityStatus == "VALID" && (tray.currentState == "STERILE_STORAGE" || tray.currentState == "DISPATCHED" || tray.currentState == "IN_OT")
            val isDocReady = patient?.documentationComplete == true && patient.consentVerified

            val canStartSurgery = isPatientReady && isOtReady && isTrayReady && isDocReady && (surgery.currentState == "READY" || surgery.currentState == "PATIENT_IN_OT" || surgery.currentState == "PATIENT_TRANSFER")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MedicalBlue,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = surgery.id,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = surgery.procedure, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                                Text(text = "Surgeon: ${surgery.surgeon} • Scheduled: ${surgery.scheduledStart}", fontSize = 12.sp, color = Slate500)
                            }
                        }
                        StatusBadge(status = surgery.currentState)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stepper timeline
                    val steps = listOf("SCHEDULED", "PREPARING", "TRANSIT", "IN OT", "ACTIVE", "COMPLETED")
                    val currentStepIdx = when (surgery.currentState) {
                        "SCHEDULED" -> 0
                        "PREPARING" -> 1
                        "PATIENT_TRANSFER" -> 2
                        "PATIENT_IN_OT", "READY" -> 3
                        "SURGERY_ACTIVE" -> 4
                        "SURGERY_COMPLETED", "TURNOVER", "CLOSED" -> 5
                        else -> 0
                    }
                    WorkflowTimelineStepper(steps = steps, currentStepIndex = currentStepIdx)

                    Spacer(modifier = Modifier.height(14.dp))

                    // Blocking Reason if blocked
                    if (surgery.isBlocked || surgery.blockingCause != null) {
                        BlockingReasonBanner(
                            cause = surgery.blockingCause ?: "One or more required dependencies failed validation.",
                            onActionClick = { onNavigateToCssd() },
                            actionLabel = "CSSD Sterilisation"
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Dependencies Grid
                    Text(text = "Surgical Dependency Engine Checklist:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate700)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DependencyChip("Patient", isPatientReady, modifier = Modifier.weight(1f))
                        DependencyChip("OT Status", isOtReady, modifier = Modifier.weight(1f))
                        DependencyChip("Doc / Consent", isDocReady, modifier = Modifier.weight(1f))
                        DependencyChip("Tray (${surgery.assignedTrayId ?: "None"})", isTrayReady, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (surgery.currentState == "SURGERY_ACTIVE") {
                            Button(
                                onClick = { onCompleteSurgery(surgery.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Complete Surgery & Start Turnover", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else if (surgery.currentState != "SURGERY_COMPLETED" && surgery.currentState != "CLOSED") {
                            Button(
                                onClick = { onStartSurgery(surgery.id) },
                                enabled = canStartSurgery,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MedicalBlue,
                                    disabledContainerColor = Slate200
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (canStartSurgery) "Start Surgery" else "Blocked (Check Dependencies)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DependencyChip(label: String, isReady: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = if (isReady) Emerald100 else Crimson100,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isReady) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (isReady) Emerald600 else Crimson600,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isReady) Emerald600 else Crimson600,
                maxLines = 1
            )
        }
    }
}
