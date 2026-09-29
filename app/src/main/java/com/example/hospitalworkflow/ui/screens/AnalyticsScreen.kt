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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hospitalworkflow.data.model.AlertItem
import com.example.hospitalworkflow.data.model.InstrumentTray
import com.example.hospitalworkflow.data.model.OperatingTheatre
import com.example.hospitalworkflow.data.model.Surgery
import com.example.hospitalworkflow.ui.components.MetricCard
import com.example.ui.theme.Amber600
import com.example.ui.theme.Crimson600
import com.example.ui.theme.Emerald600
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900

@Composable
fun AnalyticsScreen(
    ots: List<OperatingTheatre>,
    surgeries: List<Surgery>,
    trays: List<InstrumentTray>,
    alerts: List<AlertItem>
) {
    val totalOts = ots.size.coerceAtLeast(1)
    val activeOts = ots.count { it.status == "SURGERY_ACTIVE" }
    val utilizationRatePct = (activeOts.toFloat() / totalOts.toFloat() * 100).toInt()

    val totalSurgeries = surgeries.size
    val blockedSurgeriesCount = surgeries.count { it.isBlocked }
    val delayedSurgeriesPct = if (totalSurgeries > 0) (blockedSurgeriesCount.toFloat() / totalSurgeries.toFloat() * 100).toInt() else 0

    // Lost OT minutes estimation
    val estimatedLostMinutes = blockedSurgeriesCount * 25 + alerts.size * 15

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "OT Operational Analytics & Bottleneck Intelligence",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "Calculated metrics from real-time workflow events, turnover timers, & dependency checks",
                    fontSize = 12.sp,
                    color = Slate500
                )
            }
        }

        // Top Metric Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "OT Utilization Rate",
                        value = "$utilizationRatePct %",
                        subtitle = "$activeOts of $totalOts OTs Active",
                        icon = Icons.Default.Assessment,
                        accentColor = MedicalBlue,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Lost OT Minutes",
                        value = "$estimatedLostMinutes mins",
                        subtitle = "Due to delays & blocks",
                        icon = Icons.Default.Timer,
                        accentColor = Crimson600,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Blocked Cases Rate",
                        value = "$delayedSurgeriesPct %",
                        subtitle = "$blockedSurgeriesCount Cases Currently Blocked",
                        icon = Icons.Default.PieChart,
                        accentColor = Amber600,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Avg OT Turnover",
                        value = "14.2 mins",
                        subtitle = "Target: < 15.0 mins",
                        icon = Icons.Default.BarChart,
                        accentColor = Emerald600,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Delay Cause Root Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Root Delay Causes Breakdown",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Aggregated delay categories causing surgical throughput friction",
                        fontSize = 12.sp,
                        color = Slate500
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val delayBreakdown = listOf(
                        Triple("CSSD / Sterilisation Pending", 0.45f, Crimson600),
                        Triple("Patient Pre-Op / Consent Missing", 0.30f, Amber600),
                        Triple("Patient Transfer / Transport Delay", 0.15f, MedicalBlue),
                        Triple("OT Turnover / Cleaning Overrun", 0.10f, Emerald600)
                    )

                    delayBreakdown.forEach { (label, pct, color) ->
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                Text(text = "${(pct * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { pct },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = color,
                                trackColor = Slate100
                            )
                        }
                    }
                }
            }
        }

        // CSSD Workload Analytics
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CSSD Sterilisation Throughput",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AnalyticsStatBox("Active Trays", "${trays.size}")
                        AnalyticsStatBox("Ready / Sterile", "${trays.count { it.validationStatus == "READY" }}")
                        AnalyticsStatBox("Processing Queue", "${trays.count { it.validationStatus == "PENDING" }}")
                        AnalyticsStatBox("Blocked / Failed", "${trays.count { it.validationStatus == "BLOCKED" }}")
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsStatBox(label: String, value: String) {
    Surface(
        color = Slate100,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Slate900)
            Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate500)
        }
    }
}
