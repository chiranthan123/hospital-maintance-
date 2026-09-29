package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hospitalworkflow.data.model.UserRole
import com.example.hospitalworkflow.ui.HospitalViewModel
import com.example.hospitalworkflow.ui.components.AppGuideDialog
import com.example.hospitalworkflow.ui.components.RoleSelectorBar
import com.example.ui.theme.*
import com.example.hospitalworkflow.ui.screens.AdminDashboardScreen
import com.example.hospitalworkflow.ui.screens.AiEmergencyScreen
import com.example.hospitalworkflow.ui.screens.AlertsScreen
import com.example.hospitalworkflow.ui.screens.AnalyticsScreen
import com.example.hospitalworkflow.ui.screens.ApiGatewayScreen
import com.example.hospitalworkflow.ui.screens.AuditLogsScreen
import com.example.hospitalworkflow.ui.screens.CssdScreen
import com.example.hospitalworkflow.ui.screens.OperatingTheatresScreen
import com.example.hospitalworkflow.ui.screens.PatientsScreen
import com.example.hospitalworkflow.ui.screens.SettingsScreen
import com.example.hospitalworkflow.ui.screens.SurgeriesScreen
import com.example.ui.theme.HospitalIQTheme
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

enum class NavigationTab(val label: String, val icon: ImageVector, val allowedRoles: List<UserRole>) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard, listOf(UserRole.ADMIN, UserRole.OT_MANAGER, UserRole.WARD_STAFF, UserRole.CSSD_STAFF, UserRole.OT_STAFF)),
    SURGERIES("Surgeries", Icons.Default.LocalHospital, listOf(UserRole.ADMIN, UserRole.OT_MANAGER, UserRole.OT_STAFF)),
    OTS("OTs", Icons.Default.MeetingRoom, listOf(UserRole.ADMIN, UserRole.OT_MANAGER, UserRole.OT_STAFF)),
    PATIENTS("Patients", Icons.Default.People, listOf(UserRole.ADMIN, UserRole.WARD_STAFF)),
    CSSD("CSSD", Icons.Default.MedicalServices, listOf(UserRole.ADMIN, UserRole.CSSD_STAFF)),
    AI_EMERGENCY("AI Emergency", Icons.Default.AutoAwesome, listOf(UserRole.ADMIN, UserRole.OT_MANAGER)),
    SETTINGS("Settings", Icons.Default.Settings, listOf(UserRole.ADMIN, UserRole.OT_MANAGER, UserRole.WARD_STAFF, UserRole.CSSD_STAFF, UserRole.OT_STAFF)),
    API_GATEWAY("API Gateway", Icons.Default.Key, listOf(UserRole.ADMIN)),
    ALERTS("Alerts", Icons.Default.Warning, listOf(UserRole.ADMIN, UserRole.OT_MANAGER, UserRole.WARD_STAFF, UserRole.CSSD_STAFF, UserRole.OT_STAFF)),
    ANALYTICS("Analytics", Icons.Default.Assessment, listOf(UserRole.ADMIN, UserRole.OT_MANAGER)),
    AUDIT("Audit", Icons.Default.History, listOf(UserRole.ADMIN))
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HospitalIQTheme {
                MainAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: HospitalViewModel = viewModel()) {
    val currentRole by viewModel.currentRole
    var selectedTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
    var showGuideDialog by remember { mutableStateOf(false) }

    val patients by viewModel.patients.collectAsState()
    val surgeries by viewModel.surgeries.collectAsState()
    val ots by viewModel.ots.collectAsState()
    val trays by viewModel.trays.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val workflowEvents by viewModel.workflowEvents.collectAsState()
    val apiKeys by viewModel.apiKeys.collectAsState()
    val emergencyIncidents by viewModel.emergencyIncidents.collectAsState()
    val userApiKey by viewModel.userApiKey

    val availableTabs = NavigationTab.values().filter { currentRole in it.allowedRoles }

    if (selectedTab !in availableTabs) {
        selectedTab = availableTabs.firstOrNull() ?: NavigationTab.DASHBOARD
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(text = "HOSPITAL IQ", fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 1.sp)
                            Text(text = "Workflow Intelligence Platform", fontSize = 11.sp, fontWeight = FontWeight.Normal)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Slate900,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    actions = {
                        OutlinedButton(
                            onClick = { showGuideDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MedicalBlueLight),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "App Guided Tour",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Guide", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { selectedTab = NavigationTab.SETTINGS }) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                        }
                        IconButton(onClick = { viewModel.resetData() }) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset Demo Data")
                        }
                    }
                )
                RoleSelectorBar(
                    currentRole = currentRole,
                    onRoleSelected = { newRole ->
                        viewModel.setRole(newRole)
                    }
                )
            }
        },
        bottomBar = {
            Surface(
                color = Slate900,
                contentColor = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    availableTabs.forEach { tab ->
                        val isSelected = selectedTab == tab
                        Surface(
                            onClick = { selectedTab = tab },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MedicalBlue else Slate800,
                            contentColor = if (isSelected) Color.White else Slate300
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (isSelected) Color.White else Slate300
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tab.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Slate100)
        ) {
            when (selectedTab) {
                NavigationTab.DASHBOARD -> AdminDashboardScreen(
                    ots = ots,
                    surgeries = surgeries,
                    patients = patients,
                    trays = trays,
                    alerts = alerts,
                    onNavigateToSurgeries = { selectedTab = NavigationTab.SURGERIES },
                    onNavigateToCssd = { selectedTab = NavigationTab.CSSD },
                    onNavigateToOts = { selectedTab = NavigationTab.OTS },
                    onNavigateToAlerts = { selectedTab = NavigationTab.ALERTS }
                )
                NavigationTab.SURGERIES -> SurgeriesScreen(
                    surgeries = surgeries,
                    patients = patients,
                    ots = ots,
                    trays = trays,
                    onStartSurgery = { surgeryId -> viewModel.startSurgery(surgeryId) },
                    onCompleteSurgery = { surgeryId -> viewModel.completeSurgery(surgeryId) },
                    onNavigateToCssd = { selectedTab = NavigationTab.CSSD }
                )
                NavigationTab.OTS -> OperatingTheatresScreen(
                    ots = ots,
                    surgeries = surgeries,
                    patients = patients,
                    onFinishTurnover = { otId -> viewModel.finishOtTurnover(otId) }
                )
                NavigationTab.PATIENTS -> PatientsScreen(
                    patients = patients,
                    onUpdateReadiness = { pId, consent, preOp, anaest, doc, prep, cause ->
                        viewModel.updatePatientReadiness(pId, consent, preOp, anaest, doc, prep, cause)
                    },
                    onRequestTransfer = { pId -> viewModel.requestPatientTransfer(pId) },
                    onMarkInTransit = { pId -> viewModel.markPatientInTransit(pId) },
                    onConfirmInOt = { pId, otId -> viewModel.confirmPatientInOt(pId, otId) }
                )
                NavigationTab.CSSD -> CssdScreen(
                    trays = trays,
                    surgeries = surgeries,
                    ots = ots,
                    onUpdateTrayState = { tId, state -> viewModel.updateTrayState(tId, state) },
                    onValidateTray = { tId, valid -> viewModel.validateTray(tId, valid) },
                    onDispatchTray = { tId, otId, sId -> viewModel.dispatchTray(tId, otId, sId) }
                )
                NavigationTab.AI_EMERGENCY -> AiEmergencyScreen(
                    incidents = emergencyIncidents,
                    onTriggerIncident = { title, type, dept, sev, desc, cb ->
                        viewModel.triggerEmergencyIncident(title, type, dept, sev, desc, cb)
                    },
                    onRunAiAnalysis = { incId, cb ->
                        viewModel.runAiEmergencyAnalysis(incId, cb)
                    },
                    onApplyAiPlan = { plan, incId, cb ->
                        viewModel.applyAiResourcePlan(plan, incId, cb)
                    },
                    onNavigateToSettings = { selectedTab = NavigationTab.SETTINGS }
                )
                NavigationTab.SETTINGS -> SettingsScreen(
                    currentApiKey = userApiKey,
                    onSaveApiKey = { newKey -> viewModel.saveUserApiKey(newKey) },
                    onClearApiKey = { viewModel.clearUserApiKey() },
                    onTestApiKey = { key, cb -> viewModel.testUserApiKey(key, cb) }
                )
                NavigationTab.API_GATEWAY -> ApiGatewayScreen(
                    apiKeys = apiKeys,
                    onCreateApiKey = { name, scope -> viewModel.createApiKey(name, scope) },
                    onRevokeApiKey = { keyId -> viewModel.revokeApiKey(keyId) },
                    onSimulateApiUpdate = { type, id, state, key, cb ->
                        viewModel.simulateExternalApiUpdate(type, id, state, key, cb)
                    }
                )
                NavigationTab.ALERTS -> AlertsScreen(
                    alerts = alerts,
                    currentUserRole = currentRole,
                    onAcknowledgeAlert = { aId -> viewModel.acknowledgeAlert(aId) },
                    onResolveAlert = { aId -> viewModel.resolveAlert(aId) }
                )
                NavigationTab.ANALYTICS -> AnalyticsScreen(
                    ots = ots,
                    surgeries = surgeries,
                    trays = trays,
                    alerts = alerts
                )
                NavigationTab.AUDIT -> AuditLogsScreen(
                    logs = auditLogs,
                    events = workflowEvents
                )
            }
        }
    }

    if (showGuideDialog) {
        AppGuideDialog(onDismiss = { showGuideDialog = false })
    }
}
