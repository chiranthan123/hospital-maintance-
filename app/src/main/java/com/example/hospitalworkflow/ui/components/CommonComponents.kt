package com.example.hospitalworkflow.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hospitalworkflow.data.model.UserRole
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber600
import com.example.ui.theme.Crimson100
import com.example.ui.theme.Crimson600
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald600
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.MedicalBlueLight
import com.example.ui.theme.Purple100
import com.example.ui.theme.Purple600
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, icon) = when (status.uppercase()) {
        "READY", "AVAILABLE", "VALID" -> Triple(Emerald100, Emerald600, Icons.Default.CheckCircle)
        "SURGERY_ACTIVE", "ACTIVE", "IN_OT", "DISPATCHED" -> Triple(MedicalBlueLight, MedicalBlue, Icons.Default.Info)
        "BLOCKED", "FAILED", "CRITICAL" -> Triple(Crimson100, Crimson600, Icons.Default.Error)
        "PENDING", "PREPARING", "WARNING", "TURNOVER", "STERILIZATION" -> Triple(Amber100, Amber600, Icons.Default.Warning)
        "CLEANING", "DECONTAMINATION", "PACKAGING" -> Triple(Purple100, Purple600, Icons.Default.Info)
        else -> Triple(Slate100, Slate700, Icons.Default.Info)
    }

    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status.replace("_", " "),
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BlockingReasonBanner(
    title: String = "WORKFLOW BLOCKED",
    cause: String,
    onActionClick: (() -> Unit)? = null,
    actionLabel: String = "Resolve Bottleneck"
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Crimson100),
        border = androidx.compose.foundation.BorderStroke(1.dp, Crimson600.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = Crimson600,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Crimson600,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = cause,
                    color = Slate900,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            if (onActionClick != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Crimson600),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = actionLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    accentColor: Color = MedicalBlue,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(accentColor.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Slate500,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun WorkflowTimelineStepper(
    steps: List<String>,
    currentStepIndex: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, stepName ->
            val isCompleted = index < currentStepIndex
            val isCurrent = index == currentStepIndex
            val stepColor = when {
                isCompleted -> Emerald600
                isCurrent -> MedicalBlue
                else -> Slate300
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(stepColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text(text = "${index + 1}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stepName,
                    fontSize = 9.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrent) Slate900 else Slate500,
                    maxLines = 1
                )
            }

            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .weight(0.5f)
                        .background(if (index < currentStepIndex) Emerald600 else Slate200)
                )
            }
        }
    }
}

@Composable
fun RoleSelectorBar(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Slate900,
        contentColor = Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Emerald600, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "ROLE:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate300)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = currentRole.displayName, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MedicalBlueLight)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                UserRole.values().forEach { role ->
                    val isSelected = role == currentRole
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) MedicalBlue else Slate800)
                            .clickable { onRoleSelected(role) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = role.name.take(4),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Slate300
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TrayScannerDialog(
    onTrayScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var manualInput by remember { mutableStateOf("") }
    var detectedQrCode by remember { mutableStateOf<String?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var scanMode by remember { mutableStateOf("QR_CAMERA") } // QR_CAMERA or MANUAL

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, tint = MedicalBlue) },
        title = { Text(text = "Tray QR Scanner & Lookup", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { scanMode = "QR_CAMERA" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (scanMode == "QR_CAMERA") MedicalBlue else Slate100,
                            contentColor = if (scanMode == "QR_CAMERA") Color.White else Slate800
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("📷 Live QR Reader", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { scanMode = "MANUAL" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (scanMode == "MANUAL") MedicalBlue else Slate100,
                            contentColor = if (scanMode == "MANUAL") Color.White else Slate800
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("⌨️ Manual Entry", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (scanMode == "QR_CAMERA") {
                    // QR Viewfinder Box
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            // Camera Scan Target Corners Overlay
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .size(130.dp)
                                    .border(2.dp, MedicalBlueLight, RoundedCornerShape(12.dp))
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = MedicalBlueLight,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (detectedQrCode != null) "QR MATCHED!" else "ALIGN QR CODE HERE",
                                    color = if (detectedQrCode != null) Emerald100 else Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Tap a QR Code below to scan automatically:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate700)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("TR-2048", "TR-5012", "TR-1102", "TR-9940").forEach { qrTrayId ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (manualInput == qrTrayId) MedicalBlueLight else Slate100,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (manualInput == qrTrayId) MedicalBlue else Slate300),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        manualInput = qrTrayId
                                        detectedQrCode = qrTrayId
                                        errorMsg = null
                                    }
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(20.dp), tint = Slate800)
                                    Text(text = qrTrayId, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = manualInput,
                    onValueChange = {
                        manualInput = it.uppercase()
                        detectedQrCode = if (it.isNotBlank()) it.uppercase() else null
                        errorMsg = null
                    },
                    label = { Text("Scanned / Manual Tray ID") },
                    placeholder = { Text("e.g. TR-2048") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg != null) {
                    Text(text = errorMsg!!, color = Crimson600, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (manualInput.isBlank()) {
                        errorMsg = "Please scan or enter a Tray ID"
                    } else {
                        onTrayScanned(manualInput.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue)
            ) {
                Text(if (detectedQrCode != null) "Confirm Scanned Tray ($manualInput)" else "Lookup Tray")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AppGuideDialog(
    onDismiss: () -> Unit
) {
    var activeStep by remember { mutableStateOf(1) }
    val totalSteps = 6

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MedicalBlue,
                    shape = CircleShape,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("$activeStep", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Hospital IQ Guided Tour", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Step $activeStep of $totalSteps • Complete Process Walkthrough", fontSize = 11.sp, color = Slate500)
                }
            }
        },
        text = {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                when (activeStep) {
                    1 -> {
                        Text("1. Multi-Role Operating Engine", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MedicalBlue)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "• Use the top ROLE selector bar (ADMIN, OT Manager, Ward Staff, CSSD Staff, OT Staff) to switch perspectives.\n" +
                            "• Each role highlights specific tabs and actions tailored to clinical responsibilities.",
                            fontSize = 12.sp, color = Slate800, lineHeight = 18.sp
                        )
                    }
                    2 -> {
                        Text("2. Ward & Pre-Op Patient Verification", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MedicalBlue)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "• Navigate to the 'Patients' tab.\n" +
                            "• Ensure all 5 pre-op checks (Consent, Pre-Op Assessment, Anaesthesia Clearance, Documentation, Site Prep) are marked GREEN.\n" +
                            "• Request Transit to send patients from Ward to the Operating Theatre.",
                            fontSize = 12.sp, color = Slate800, lineHeight = 18.sp
                        )
                    }
                    3 -> {
                        Text("3. CSSD & QR Code Sterile Tray Lifecycle", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MedicalBlue)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "• Navigate to 'CSSD' tab and click 'Scan Tray ID'.\n" +
                            "• Use the live QR reader or manual entry to detect sterile instrument sets.\n" +
                            "• Advance trays through Decon -> Clean -> Sterilise -> Storage.\n" +
                            "• Validate biological indicator indicators and dispatch ready trays directly to target OTs.",
                            fontSize = 12.sp, color = Slate800, lineHeight = 18.sp
                        )
                    }
                    4 -> {
                        Text("4. OT Readiness & Turnover Management", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MedicalBlue)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "• Navigate to 'OTs' and 'Surgeries' tabs.\n" +
                            "• Check dependencies (Patient in OT, Sterile Tray present, Room Sanitised).\n" +
                            "• Click 'Finish Turnover' to clear room status between surgical cases.",
                            fontSize = 12.sp, color = Slate800, lineHeight = 18.sp
                        )
                    }
                    5 -> {
                        Text("5. Gemini AI Emergency & Resource Re-allocation", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MedicalBlue)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "• Navigate to 'AI Emergency' tab when hardware fails or surge occurs.\n" +
                            "• Click 'Trigger Emergency' to report breakdowns (e.g. Steriliser offline).\n" +
                            "• Run 'Gemini AI Analysis' to generate a redistribution strategy.\n" +
                            "• Click 'Apply AI Resource Plan' to automatically execute database transactions.",
                            fontSize = 12.sp, color = Slate800, lineHeight = 18.sp
                        )
                    }
                    6 -> {
                        Text("6. API Gateway & External Integrations", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MedicalBlue)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "• Navigate to 'API Gateway' tab to create scoped access keys for external EHR systems or mobile scanners.\n" +
                            "• Use the Live Database REST Dispatcher to send simulated JSON updates into the Room database.",
                            fontSize = 12.sp, color = Slate800, lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                // Stepper Progress Dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..totalSteps).forEach { step ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(if (step == activeStep) 10.dp else 6.dp)
                                .background(
                                    if (step == activeStep) MedicalBlue else Slate300,
                                    CircleShape
                                )
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (activeStep < totalSteps) {
                Button(
                    onClick = { activeStep++ },
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue)
                ) {
                    Text("Next Step >")
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                ) {
                    Text("Got It! Start Using")
                }
            }
        },
        dismissButton = {
            if (activeStep > 1) {
                TextButton(onClick = { activeStep-- }) {
                    Text("< Previous")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}
