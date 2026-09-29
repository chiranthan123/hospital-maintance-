package com.example.hospitalworkflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hospitalworkflow.data.ai.AiResourceAllocationPlan
import com.example.hospitalworkflow.data.model.EmergencyIncident
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.Rose500
import com.example.ui.theme.Rose600
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiEmergencyScreen(
    incidents: List<EmergencyIncident>,
    onTriggerIncident: (String, String, String, String, String, (EmergencyIncident) -> Unit) -> Unit,
    onRunAiAnalysis: (String, (Result<AiResourceAllocationPlan>) -> Unit) -> Unit,
    onApplyAiPlan: (AiResourceAllocationPlan, String, () -> Unit) -> Unit,
    onNavigateToSettings: (() -> Unit)? = null
) {
    var showTriggerDialog by remember { mutableStateOf(false) }
    var selectedIncident by remember { mutableStateOf<EmergencyIncident?>(incidents.firstOrNull()) }
    var aiPlanResult by remember { mutableStateOf<AiResourceAllocationPlan?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (onNavigateToSettings != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Amber500,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Google AI Studio API Key",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    OutlinedButton(
                        onClick = { onNavigateToSettings() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Amber500),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Amber500),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text("Configure Key in Settings", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        statusMessage?.let { msg ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Emerald500.copy(alpha = 0.15f))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Emerald500)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(msg, color = Slate900, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Rose600, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Emergency Incidents",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Slate900
                        )
                    }

                    Button(
                        onClick = { showTriggerDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Rose600),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("+ Log Incident", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (incidents.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No active module failures or emergency incidents reported.", color = Slate700)
                        }
                    }
                }
            } else {
                items(incidents) { incident ->
                    val isSelected = selectedIncident?.id == incident.id
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Rose500.copy(alpha = 0.08f) else Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Rose500 else Slate200
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (incident.severity == "CRITICAL") Icons.Default.Dangerous else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (incident.severity == "CRITICAL") Rose600 else Amber500
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(incident.id, fontWeight = FontWeight.Bold, color = Slate900)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(incident.title, fontWeight = FontWeight.Bold, color = Slate900, fontSize = 15.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (incident.status == "RESOLVED") Emerald500.copy(alpha = 0.2f) else Rose500.copy(alpha = 0.2f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        incident.status,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (incident.status == "RESOLVED") Emerald500 else Rose600
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(incident.description, fontSize = 13.sp, color = Slate700)

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        selectedIncident = incident
                                        isAnalyzing = true
                                        statusMessage = null
                                        onRunAiAnalysis(incident.id) { result ->
                                            isAnalyzing = false
                                            result.onSuccess { plan ->
                                                aiPlanResult = plan
                                            }.onFailure {
                                                statusMessage = "AI Analysis Failed: ${it.message}"
                                            }
                                        }
                                    },
                                    enabled = !isAnalyzing && incident.status != "RESOLVED",
                                    colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue)
                                ) {
                                    if (isAnalyzing && selectedIncident?.id == incident.id) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Gemini AI Analyzing...", fontSize = 12.sp)
                                    } else {
                                        Icon(imageVector = Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Run Gemini AI Analysis", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // AI Allocation Plan Section
            aiPlanResult?.let { plan ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Slate900)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Amber500)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Gemini AI Emergency Redistribution Plan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Operational Root Cause Analysis:", color = Slate200, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(plan.analysis, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))

                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Recommended Action Steps:", color = Slate200, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            plan.recommendedActions.forEach { action ->
                                Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                                    Text("• ", color = Amber500, fontWeight = FontWeight.Bold)
                                    Text(action, color = Color.White, fontSize = 13.sp)
                                }
                            }

                            if (plan.otReallocations.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("OT Re-allocation Map:", color = Slate200, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                plan.otReallocations.forEach { alloc ->
                                    Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.MeetingRoom, contentDescription = null, tint = MedicalBlue, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("${alloc.otId} ➔ Surgery ${alloc.surgeryId}: ${alloc.reason}", color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }

                            if (plan.trayReroutes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Sterile Tray Re-routes:", color = Slate200, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                plan.trayReroutes.forEach { tray ->
                                    Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.MedicalServices, contentDescription = null, tint = Emerald500, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Tray ${tray.trayId} ➔ Surgery ${tray.targetSurgeryId} (${tray.targetOtId}): ${tray.reason}", color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }

                            if (plan.patientPriorityUpdates.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Patient Priority Escalations:", color = Slate200, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                plan.patientPriorityUpdates.forEach { prio ->
                                    Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.LocalHospital, contentDescription = null, tint = Rose500, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Patient ${prio.patientId} ➔ Priority ${prio.newPriority}: ${prio.reason}", color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    selectedIncident?.let { incident ->
                                        onApplyAiPlan(plan, incident.id) {
                                            statusMessage = "Gemini AI Plan executed! Resources re-allocated in Room Database."
                                            aiPlanResult = null
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Apply AI Resource Redistribution Plan to Database", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTriggerDialog) {
        var title by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("STERILIZER_BREAKDOWN") }
        var department by remember { mutableStateOf("CSSD") }
        var severity by remember { mutableStateOf("CRITICAL") }
        var description by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showTriggerDialog = false },
            title = { Text("Log Emergency or Module Failure", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Incident Title (e.g., CSSD Sterilizer Pressure Failure)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Affected Department (CSSD / OT / WARD / ICU)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Detailed Failure Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onTriggerIncident(title, type, department, severity, description) { incident ->
                                selectedIncident = incident
                                showTriggerDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose600)
                ) {
                    Text("Trigger Incident")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTriggerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
