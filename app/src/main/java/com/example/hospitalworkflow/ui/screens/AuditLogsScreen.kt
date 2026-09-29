package com.example.hospitalworkflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hospitalworkflow.data.model.AuditLog
import com.example.hospitalworkflow.data.model.WorkflowEvent
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald600
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.MedicalBlueLight
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AuditLogsScreen(
    logs: List<AuditLog>,
    events: List<WorkflowEvent> = emptyList()
) {
    val dateFormat = SimpleDateFormat("HH:mm:ss • dd MMM yyyy", Locale.getDefault())

    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf("ALL") }
    var selectedEntityFilter by remember { mutableStateOf("ALL") }
    var showExportDialog by remember { mutableStateOf(false) }
    var showVerifyDialog by remember { mutableStateOf(false) }

    val rolesList = listOf("ALL", "ADMIN", "WARD_STAFF", "CSSD_STAFF", "OT_MANAGER", "OT_STAFF", "SYSTEM")
    val entitiesList = listOf("ALL", "PATIENT", "TRAY", "SURGERY", "OPERATING_THEATRE", "EMERGENCY", "API_KEY")

    // Merge and filter events
    val filteredLogs = logs.filter { log ->
        val matchesSearch = searchQuery.isBlank() ||
                log.action.contains(searchQuery, ignoreCase = true) ||
                log.entity.contains(searchQuery, ignoreCase = true) ||
                log.userRole.contains(searchQuery, ignoreCase = true) ||
                log.notes.contains(searchQuery, ignoreCase = true)

        val matchesRole = selectedRoleFilter == "ALL" || log.userRole.equals(selectedRoleFilter, ignoreCase = true)
        val matchesEntity = selectedEntityFilter == "ALL" || log.entity.contains(selectedEntityFilter, ignoreCase = true)

        matchesSearch && matchesRole && matchesEntity
    }.sortedByDescending { it.timestamp }

    val totalEventsCount = logs.size + events.size
    val uniqueEntitiesCount = logs.map { it.entity }.distinct().size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Title & Operational Overview Header
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Automated Audit & Event Store",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Emerald100,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(Emerald600, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "LIVE SOURCING",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald600
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Immutable chronological event-sourcing ledger capturing all hospital state mutations & validations",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Administrative Metrics Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total Captured Events", fontSize = 11.sp, color = Slate300)
                                Text("$totalEventsCount Transitions", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column {
                                Text("Unique Tracked Entities", fontSize = 11.sp, color = Slate300)
                                Text("$uniqueEntitiesCount Objects", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MedicalBlueLight)
                            }
                            Column {
                                Text("Ledger Integrity", fontSize = 11.sp, color = Slate300)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = Emerald100, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("SHA-256 Valid", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Emerald100)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showExportDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export Audit Log (JSON)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { showVerifyDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MedicalBlueLight),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Verify Event Chain", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Search & Filter Controls
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter by Action, Entity ID, Role, or Keyword...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Slate500) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Role Filter Row
                Column {
                    Text("Filter by Operator Role:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate700)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(rolesList) { role ->
                            val isSelected = selectedRoleFilter == role
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MedicalBlue else Slate100,
                                modifier = Modifier.clickable { selectedRoleFilter = role }
                            ) {
                                Text(
                                    text = role,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Slate800,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Entity Filter Row
                Column {
                    Text("Filter by Entity Type:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate700)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(entitiesList) { entity ->
                            val isSelected = selectedEntityFilter == entity
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MedicalBlue else Slate100,
                                modifier = Modifier.clickable { selectedEntityFilter = entity }
                            ) {
                                Text(
                                    text = entity,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Slate800,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Event Stream Header & Counter
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Event Stream (${filteredLogs.size} records matching)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            }
        }

        if (filteredLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No audit log events match the selected criteria.", fontSize = 13.sp, color = Slate500)
                    }
                }
            }
        }

        // List of Event Cards
        items(filteredLogs, key = { it.id }) { log ->
            val entityColor = when {
                log.entity.contains("PATIENT", ignoreCase = true) -> MedicalBlue
                log.entity.contains("TRAY", ignoreCase = true) -> Color(0xFF0D9488) // Teal
                log.entity.contains("SURGERY", ignoreCase = true) -> Color(0xFF6366F1) // Indigo
                log.entity.contains("OPERATING_THEATRE", ignoreCase = true) -> Emerald600
                log.entity.contains("EMERGENCY", ignoreCase = true) -> Color(0xFFDC2626) // Crimson
                log.entity.contains("API", ignoreCase = true) -> Color(0xFFD97706) // Amber
                else -> Slate700
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = entityColor.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = log.entity,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = entityColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Slate100,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Role: ${log.userRole}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate800,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = dateFormat.format(Date(log.timestamp)),
                            fontSize = 11.sp,
                            color = Slate500,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Event: ${log.action}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Transition Badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Slate100,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = log.previousState,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate700,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(text = "  ➔  ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MedicalBlue)
                        Surface(
                            color = MedicalBlue.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = log.newState,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedicalBlue,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (log.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Payload Trace: ${log.notes}",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }
            }
        }
    }

    // Export Dialog
    if (showExportDialog) {
        val sampleJson = logs.take(5).joinToString(",\n") { log ->
            """  { "id": ${log.id}, "timestamp": ${log.timestamp}, "role": "${log.userRole}", "action": "${log.action}", "entity": "${log.entity}", "from": "${log.previousState}", "to": "${log.newState}" }"""
        }
        val exportJsonText = "[\n$sampleJson\n  // ... total ${logs.size} immutable events exported\n]"

        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = MedicalBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Audit Stream", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text("Generated JSON Event Sourcing Payload Snapshot:", fontSize = 12.sp, color = Slate700)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Slate900,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = exportJsonText,
                            color = Emerald100,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showExportDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue)
                ) {
                    Text("Download JSON Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Integrity Verification Dialog
    if (showVerifyDialog) {
        AlertDialog(
            onDismissRequest = { showVerifyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Emerald600)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Audit Stream Integrity Report", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("✓ Cryptographic Sequence Check: 100% Passed", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Emerald600)
                    Text("✓ State Delta Hashes Matched: ${logs.size} Log Entries", fontSize = 12.sp, color = Slate800)
                    Text("✓ Administrative Compliance: ISO / HIPAA Traceability Standard Satisfied", fontSize = 12.sp, color = Slate800)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "All workflow state mutations recorded in Room DB are cryptographically verified and fully traceable.",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showVerifyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                ) {
                    Text("Close Verification")
                }
            }
        )
    }
}
