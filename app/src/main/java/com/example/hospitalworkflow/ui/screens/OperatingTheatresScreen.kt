package com.example.hospitalworkflow.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
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
import com.example.hospitalworkflow.data.model.OperatingTheatre
import com.example.hospitalworkflow.data.model.Patient
import com.example.hospitalworkflow.data.model.Surgery
import com.example.hospitalworkflow.ui.components.StatusBadge
import com.example.ui.theme.Amber600
import com.example.ui.theme.Emerald600
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun OperatingTheatresScreen(
    ots: List<OperatingTheatre>,
    surgeries: List<Surgery>,
    patients: List<Patient>,
    onFinishTurnover: (String) -> Unit
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
                    text = "Operating Theatres (OT 01 - OT 04)",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "Live OT status monitoring, turnover tracking, & room readiness management",
                    fontSize = 13.sp,
                    color = Slate500
                )
            }
        }

        items(ots, key = { it.id }) { ot ->
            val surgery = surgeries.find { it.id == ot.currentSurgeryId }
            val patient = patients.find { it.id == ot.currentPatientId }

            val turnoverDurationMins = if (ot.turnoverStartTimestamp != null) {
                ((System.currentTimeMillis() - ot.turnoverStartTimestamp) / (1000 * 60)).toInt()
            } else 0

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
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Slate900, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.MeetingRoom, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = ot.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                                Text(text = "Theatre ID: ${ot.id}", fontSize = 12.sp, color = Slate500)
                            }
                        }
                        StatusBadge(status = ot.status)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (surgery != null) {
                        Surface(
                            color = Slate100,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = "Active Case Details:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate700)
                                Text(text = "Procedure: ${surgery.procedure}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                Text(text = "Patient: ${patient?.name ?: surgery.patientId} (${patient?.ward ?: "Ward"})", fontSize = 12.sp, color = Slate700)
                                Text(text = "Surgeon: ${surgery.surgeon} • Scheduled: ${surgery.scheduledStart}", fontSize = 12.sp, color = Slate500)
                            }
                        }
                    } else if (ot.status == "TURNOVER") {
                        Surface(
                            color = Amber600.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = Amber600)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = "OT Turnover & Sanitisation In Progress", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                        Text(text = "Elapsed Time: $turnoverDurationMins minutes (Target: < 15 mins)", fontSize = 12.sp, color = Slate700)
                                    }
                                }
                                Button(
                                    onClick = { onFinishTurnover(ot.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Finish Turnover", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Theatre is currently vacant and available for next case assignment.",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }
                }
            }
        }
    }
}
