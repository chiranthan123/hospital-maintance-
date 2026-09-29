package com.example.hospitalworkflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hospitalworkflow.data.model.Patient
import com.example.hospitalworkflow.ui.components.BlockingReasonBanner
import com.example.hospitalworkflow.ui.components.StatusBadge
import com.example.ui.theme.Emerald600
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900

@Composable
fun PatientsScreen(
    patients: List<Patient>,
    onUpdateReadiness: (patientId: String, consent: Boolean, preOp: Boolean, anaesthesia: Boolean, doc: Boolean, prepared: Boolean, cause: String?) -> Unit,
    onRequestTransfer: (String) -> Unit,
    onMarkInTransit: (String) -> Unit,
    onConfirmInOt: (patientId: String, otId: String) -> Unit
) {
    var selectedPatientForEdit by remember { mutableStateOf<Patient?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Ward Patient Readiness & Transfers",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "Verify pre-op checklists, consent verification, and dispatch patient transfer to OT",
                    fontSize = 13.sp,
                    color = Slate500
                )
            }
        }

        items(patients, key = { it.id }) { patient ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                    text = patient.id,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = patient.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                                Text(text = "Ward: ${patient.ward} • Procedure: ${patient.procedure}", fontSize = 12.sp, color = Slate500)
                            }
                        }
                        StatusBadge(status = patient.state)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (patient.blockingReason != null) {
                        BlockingReasonBanner(
                            title = "PRE-OP CHECKLIST INCOMPLETE",
                            cause = patient.blockingReason
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Checklist summary
                    Text(text = "Pre-op Readiness Checklist:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate700)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ChecklistPill("Consent", patient.consentVerified)
                        ChecklistPill("Pre-Op", patient.preOpComplete)
                        ChecklistPill("Anaesthesia", patient.anaesthesiaClearance)
                        ChecklistPill("Prepared", patient.patientPrepared)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { selectedPatientForEdit = patient },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate100, contentColor = MedicalBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Checklist", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            when (patient.state) {
                                "READY", "PREPARING" -> {
                                    Button(
                                        onClick = { onRequestTransfer(patient.id) },
                                        enabled = patient.consentVerified && patient.preOpComplete,
                                        colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Request Transfer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                "TRANSFER_REQUESTED" -> {
                                    Button(
                                        onClick = { onMarkInTransit(patient.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Mark In Transit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                "IN_TRANSIT" -> {
                                    Button(
                                        onClick = { onConfirmInOt(patient.id, patient.assignedOtId ?: "OT-01") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Confirm In OT (${patient.assignedOtId ?: "OT-01"})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Checklist Dialog
    selectedPatientForEdit?.let { patient ->
        var consent by remember { mutableStateOf(patient.consentVerified) }
        var preOp by remember { mutableStateOf(patient.preOpComplete) }
        var anaesthesia by remember { mutableStateOf(patient.anaesthesiaClearance) }
        var doc by remember { mutableStateOf(patient.documentationComplete) }
        var prepared by remember { mutableStateOf(patient.patientPrepared) }
        var causeText by remember { mutableStateOf(patient.blockingReason ?: "") }

        AlertDialog(
            onDismissRequest = { selectedPatientForEdit = null },
            title = { Text(text = "Patient Pre-Op Checklist (${patient.id})", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = consent, onCheckedChange = { consent = it })
                        Text("Surgical Consent Signed & Verified", fontSize = 13.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = preOp, onCheckedChange = { preOp = it })
                        Text("Pre-Op Vitals & Blood Work Complete", fontSize = 13.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = anaesthesia, onCheckedChange = { anaesthesia = it })
                        Text("Anaesthesia Clearance Approved", fontSize = 13.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = doc, onCheckedChange = { doc = it })
                        Text("Surgical Documentation Complete", fontSize = 13.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = prepared, onCheckedChange = { prepared = it })
                        Text("Patient NPO & Prepared for Transfer", fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = causeText,
                        onValueChange = { causeText = it },
                        label = { Text("Blocking Reason (if incomplete)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateReadiness(
                            patient.id,
                            consent,
                            preOp,
                            anaesthesia,
                            doc,
                            prepared,
                            if (causeText.isBlank()) null else causeText
                        )
                        selectedPatientForEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue)
                ) {
                    Text("Save Readiness")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPatientForEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ChecklistPill(label: String, isChecked: Boolean) {
    Surface(
        color = if (isChecked) Emerald600.copy(alpha = 0.12f) else Slate100,
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = if (isChecked) Emerald600 else Slate500,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isChecked) Emerald600 else Slate700)
        }
    }
}
