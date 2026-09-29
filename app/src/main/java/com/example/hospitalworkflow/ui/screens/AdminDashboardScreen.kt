package com.example.hospitalworkflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hospitalworkflow.data.model.AlertItem
import com.example.hospitalworkflow.data.model.InstrumentTray
import com.example.hospitalworkflow.data.model.OperatingTheatre
import com.example.hospitalworkflow.data.model.Patient
import com.example.hospitalworkflow.data.model.Surgery
import com.example.hospitalworkflow.ui.components.BlockingReasonBanner
import com.example.hospitalworkflow.ui.components.MetricCard
import com.example.hospitalworkflow.ui.components.StatusBadge
import com.example.ui.theme.Amber600
import com.example.ui.theme.Crimson600
import com.example.ui.theme.Emerald600
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900

@Composable
fun AdminDashboardScreen(
    ots: List<OperatingTheatre>,
    surgeries: List<Surgery>,
    patients: List<Patient>,
    trays: List<InstrumentTray>,
    alerts: List<AlertItem>,
    onNavigateToSurgeries: () -> Unit,
    onNavigateToCssd: () -> Unit,
    onNavigateToOts: () -> Unit,
    onNavigateToAlerts: () -> Unit
) {
    val activeSurgeries = surgeries.filter { it.currentState == "SURGERY_ACTIVE" }
    val blockedSurgeries = surgeries.filter { it.isBlocked }
    val pendingTrays = trays.filter { it.validationStatus == "PENDING" || it.currentState == "STERILIZATION" }
    val openAlerts = alerts.filter { it.status != "RESOLVED" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Column(modifier = Modifier.padding(bottom = 2.dp)) {
                Text(
                    text = "Hospital Command Center",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Real-time surgical readiness, OT allocation, & CSSD workflow intelligence",
                    fontSize = 13.sp,
                    color = Slate500,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Active Bottleneck Warning if any
        val primaryBlockedSurgery = blockedSurgeries.firstOrNull()
        if (primaryBlockedSurgery != null) {
            item {
                BlockingReasonBanner(
                    title = "CRITICAL BOTTLENECK: SURGERY ${primaryBlockedSurgery.id} BLOCKED",
                    cause = primaryBlockedSurgery.blockingCause ?: "Dependency missing",
                    onActionClick = {
                        if (primaryBlockedSurgery.blockingCause?.contains("CSSD") == true || primaryBlockedSurgery.blockingCause?.contains("Tray") == true) {
                            onNavigateToCssd()
                        } else {
                            onNavigateToSurgeries()
                        }
                    },
                    actionLabel = "Inspect & Unblock"
                )
            }
        }

        // KPI Metric Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Active Surgeries",
                        value = "${activeSurgeries.size} / ${surgeries.size}",
                        subtitle = "${ots.count { it.status == "AVAILABLE" }} OTs Available",
                        icon = Icons.Default.LocalHospital,
                        accentColor = MedicalBlue,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Blocked Cases",
                        value = "${blockedSurgeries.size}",
                        subtitle = "${openAlerts.size} Open Alerts",
                        icon = Icons.Default.Warning,
                        accentColor = Crimson600,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Pending Sterilisation",
                        value = "${pendingTrays.size} Trays",
                        subtitle = "CSSD Queue Active",
                        icon = Icons.Default.MedicalServices,
                        accentColor = Amber600,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Patient Readiness",
                        value = "${patients.count { it.state == "READY" || it.state == "IN_OT" }} / ${patients.size}",
                        subtitle = "Pre-op Clearance",
                        icon = Icons.Default.People,
                        accentColor = Emerald600,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Live OT Status Grid
        item {
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
                        Text(
                            text = "Operating Theatre Overview",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onNavigateToOts,
                            colors = ButtonDefaults.buttonColors(containerColor = Slate100, contentColor = MedicalBlue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("View All OTs", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ots.forEach { ot ->
                            val currentSurgery = surgeries.find { it.id == ot.currentSurgeryId }
                            val currentPatient = patients.find { it.id == ot.currentPatientId }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToOts() },
                                colors = CardDefaults.cardColors(containerColor = Slate100),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(Slate900, RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = ot.id.takeLast(2),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = ot.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Slate900,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            if (currentSurgery != null) {
                                                Text(
                                                    text = "${currentSurgery.procedure} (${currentPatient?.name ?: "Patient"})",
                                                    fontSize = 12.sp,
                                                    color = Slate700,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            } else {
                                                Text(
                                                    text = "No surgery actively in progress",
                                                    fontSize = 12.sp,
                                                    color = Slate500,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StatusBadge(status = ot.status)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Workflow Kanban Board Summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Live Surgery Pipeline State",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val stages = listOf(
                        "SCHEDULED" to surgeries.filter { it.currentState == "SCHEDULED" },
                        "PREPARING" to surgeries.filter { it.currentState == "PREPARING" },
                        "READY" to surgeries.filter { it.currentState == "READY" },
                        "ACTIVE" to surgeries.filter { it.currentState == "SURGERY_ACTIVE" },
                        "TURNOVER" to surgeries.filter { it.currentState == "TURNOVER" }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        stages.forEach { (stageLabel, list) ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Slate100, RoundedCornerShape(10.dp))
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stageLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate700,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${list.size}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (stageLabel == "READY") Emerald600 else Slate900,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // System Alerts Quick Panel
        item {
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
                        Text(
                            text = "Active Operational Alerts",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onNavigateToAlerts,
                            colors = ButtonDefaults.buttonColors(containerColor = Slate100, contentColor = Crimson600),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Manage Alerts", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (openAlerts.isEmpty()) {
                        Text("No active alerts detected.", fontSize = 12.sp, color = Slate500)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            openAlerts.take(3).forEach { alert ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (alert.severity == "CRITICAL") Crimson600.copy(alpha = 0.08f)
                                            else Amber600.copy(alpha = 0.08f),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StatusBadge(status = alert.severity)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = alert.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Slate900,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = alert.message,
                                            fontSize = 12.sp,
                                            color = Slate700,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
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
}
