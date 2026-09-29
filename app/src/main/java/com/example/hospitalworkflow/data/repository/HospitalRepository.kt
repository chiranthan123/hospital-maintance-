package com.example.hospitalworkflow.data.repository

import com.example.hospitalworkflow.data.local.HospitalDatabase
import com.example.hospitalworkflow.data.model.AlertItem
import com.example.hospitalworkflow.data.model.AuditLog
import com.example.hospitalworkflow.data.model.InstrumentTray
import com.example.hospitalworkflow.data.model.OperatingTheatre
import com.example.hospitalworkflow.data.model.Patient
import com.example.hospitalworkflow.data.model.Surgery
import com.example.hospitalworkflow.data.model.SurgeryDependencyStatus
import com.example.hospitalworkflow.data.model.WorkflowEvent
import com.example.hospitalworkflow.data.model.WorkflowStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HospitalRepository(private val db: HospitalDatabase) {

    val patients: Flow<List<Patient>> = db.patientDao().getAllPatients()
    val surgeries: Flow<List<Surgery>> = db.surgeryDao().getAllSurgeries()
    val operatingTheatres: Flow<List<OperatingTheatre>> = db.operatingTheatreDao().getAllOts()
    val instrumentTrays: Flow<List<InstrumentTray>> = db.instrumentTrayDao().getAllTrays()
    val workflowEvents: Flow<List<WorkflowEvent>> = db.workflowEventDao().getAllEvents()
    val alerts: Flow<List<AlertItem>> = db.alertDao().getAllAlerts()
    val auditLogs: Flow<List<AuditLog>> = db.auditLogDao().getAllLogs()
    val apiKeys: Flow<List<com.example.hospitalworkflow.data.model.ApiKeyEntity>> = db.apiKeyDao().getAllApiKeys()
    val emergencyIncidents: Flow<List<com.example.hospitalworkflow.data.model.EmergencyIncident>> = db.emergencyIncidentDao().getAllIncidents()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val currentPatients = db.patientDao().getAllPatients().first()
            if (currentPatients.isEmpty()) {
                seedInitialData()
            }
        }
    }

    suspend fun seedInitialData() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()

        // 1. Operating Theatres (4 OTs)
        val seedOts = listOf(
            OperatingTheatre("OT-01", "Main Surgical Suite 1", "SURGERY_ACTIVE", "S-101", "P-9821"),
            OperatingTheatre("OT-02", "Suite 2 (Orthopedics)", "PREPARING", "S-102", "P-4432"),
            OperatingTheatre("OT-03", "Suite 3 (Cardiothoracic)", "AVAILABLE", null, null),
            OperatingTheatre("OT-04", "Suite 4 (Emergency/Trauma)", "TURNOVER", "S-108", "P-8801", turnoverStartTimestamp = now - 15 * 60 * 1000)
        )
        db.operatingTheatreDao().insertAll(seedOts)

        // 2. Patients (12 Patients)
        val seedPatients = listOf(
            Patient("P-9821", "John Doe", "Ward 4A", "Total Hip Arthroplasty", "OT-01", "S-101", "08:30", "IN_OT", "NORMAL",
                admissionComplete = true, procedureConfirmed = true, consentVerified = true, preOpComplete = true, anaesthesiaClearance = true, documentationComplete = true, patientPrepared = true, transportAvailable = true),
            Patient("P-4432", "Jane Smith", "Ward 2B", "Laparoscopic Appendectomy", "OT-02", "S-102", "09:15", "READY", "URGENT",
                admissionComplete = true, procedureConfirmed = true, consentVerified = true, preOpComplete = true, anaesthesiaClearance = true, documentationComplete = true, patientPrepared = true, transportAvailable = true),
            Patient("P-7710", "Robert Chen", "ICU-1", "Coronary Artery Bypass Graft", "OT-03", "S-103", "10:00", "PREPARING", "EMERGENCY",
                admissionComplete = true, procedureConfirmed = true, consentVerified = false, preOpComplete = false, anaesthesiaClearance = true, documentationComplete = true, patientPrepared = false, transportAvailable = true, blockingReason = "Consent pending from family & Pre-op bloods incomplete"),
            Patient("P-3301", "Maria Garcia", "Ward 3C", "Cholecystectomy", "OT-01", "S-104", "11:30", "ADMITTED", "NORMAL"),
            Patient("P-5520", "David Kim", "Ward 5A", "Lumbar Discectomy", "OT-02", "S-105", "12:45", "ADMITTED", "NORMAL"),
            Patient("P-6612", "Sarah Jenkins", "Day Surgery", "Knee Arthroscopy", "OT-03", "S-106", "13:30", "ADMITTED", "NORMAL"),
            Patient("P-1190", "Ahmed Hassan", "Ward 1A", "Inguinal Hernia Repair", "OT-01", "S-107", "15:00", "ADMITTED", "NORMAL"),
            Patient("P-8801", "Elena Rossi", "Emergency Dept", "Open Femur Reduction", "OT-04", "S-108", "07:00", "RECOVERY", "EMERGENCY"),
            Patient("P-2245", "William Taylor", "Ward 4B", "Thyroidectomy", null, null, "14:00", "ADMITTED", "NORMAL"),
            Patient("P-9011", "Grace Lee", "Ward 2A", "Mastectomy", null, null, "15:30", "ADMITTED", "NORMAL"),
            Patient("P-3112", "Thomas Wright", "Ward 3B", "Tonsillectomy", null, null, "16:15", "ADMITTED", "NORMAL"),
            Patient("P-7409", "Olivia Martinez", "Ward 5C", "Colectomy", null, null, "17:00", "ADMITTED", "NORMAL")
        )
        db.patientDao().insertAll(seedPatients)

        // 3. Instrument Trays (15 Trays)
        val seedTrays = listOf(
            // Scenario A - Valid tray in OT
            InstrumentTray("TR-9940", "Major Joint Ortho Set", "IN_OT", "OT-01", "VALID", "READY", "OT-01", "S-101"),
            // Scenario B - CSSD Sterilization Pending (Blocks S-102)
            InstrumentTray("TR-5012", "General Surgery Laparoscopy Kit", "STERILIZATION", "CSSD-Sterilizer-2", "PENDING", "PENDING", "OT-02", "S-102", cycleId = "CYC-8821"),
            // Scenario C & D - Invalid / Blocked Tray
            InstrumentTray("TR-1102", "Neurovascular Micro Tray", "DECONTAMINATION", "CSSD-Decon-1", "PENDING", "BLOCKED", null, null),
            InstrumentTray("TR-2048", "Cardiac Bypass Specialty Set", "STERILE_STORAGE", "CSSD-Storage-A3", "VALID", "READY", "OT-03", "S-103"),
            InstrumentTray("TR-3310", "Major Vascular Kit", "STERILE_STORAGE", "CSSD-Storage-B1", "VALID", "READY"),
            InstrumentTray("TR-4412", "Ortho Trauma Reduction Set", "DISPATCHED", "En Route to OT-04", "VALID", "READY", "OT-04", "S-108"),
            InstrumentTray("TR-8820", "Basic Surgical Tray 1", "STERILE_STORAGE", "CSSD-Storage-C1", "VALID", "READY"),
            InstrumentTray("TR-8821", "Basic Surgical Tray 2", "CLEANING", "CSSD-Wash-1", "PENDING", "PENDING"),
            InstrumentTray("TR-8822", "Basic Surgical Tray 3", "PACKAGING", "CSSD-Pack-2", "PENDING", "PENDING"),
            InstrumentTray("TR-8823", "ENT Specialty Kit", "STERILE_STORAGE", "CSSD-Storage-C2", "VALID", "READY"),
            InstrumentTray("TR-8824", "Spine Surgery Instrumentation", "STERILE_STORAGE", "CSSD-Storage-A1", "VALID", "READY"),
            InstrumentTray("TR-8825", "Laparoscopy Backup Set", "STERILE_STORAGE", "CSSD-Storage-B3", "VALID", "READY"),
            InstrumentTray("TR-8826", "Emergency Trauma Pack", "STERILE_STORAGE", "CSSD-Storage-A2", "VALID", "READY"),
            InstrumentTray("TR-8827", "Used Hernia Set", "RETURNED", "CSSD-Receiving", "EXPIRED", "PENDING"),
            InstrumentTray("TR-8828", "Used Arthroscopy Set", "USED", "OT-03 Dirty Utility", "EXPIRED", "PENDING")
        )
        db.instrumentTrayDao().insertAll(seedTrays)

        // 4. Surgeries (8 Surgeries)
        val seedSurgeries = listOf(
            // SCENARIO E: Active Surgery
            Surgery("S-101", "P-9821", "Total Hip Arthroplasty", "Dr. Miller", "OT-01", "08:30", actualStart = "08:40", currentState = "SURGERY_ACTIVE", assignedTrayId = "TR-9940"),
            // SCENARIO B: CSSD Blocked Surgery (Instrument TR-5012 in Sterilization)
            Surgery("S-102", "P-4432", "Laparoscopic Appendectomy", "Dr. Wilson", "OT-02", "09:15", currentState = "PREPARING", assignedTrayId = "TR-5012", isBlocked = true, blockingCause = "CSSD / INSTRUMENT - Tray TR-5012 in STERILIZATION stage (Validation Pending)"),
            // SCENARIO C: Transfer Delay Surgery
            Surgery("S-103", "P-7710", "Coronary Artery Bypass Graft", "Dr. Gupta", "OT-03", "10:00", currentState = "SCHEDULED", assignedTrayId = "TR-2048", isBlocked = true, blockingCause = "PATIENT READINESS - Consent missing & Pre-op incomplete"),
            Surgery("S-104", "P-3301", "Cholescystectomy", "Dr. Miller", "OT-01", "11:30", currentState = "SCHEDULED", assignedTrayId = "TR-8825"),
            Surgery("S-105", "P-5520", "Lumbar Discectomy", "Dr. Chen", "OT-02", "12:45", currentState = "SCHEDULED", assignedTrayId = "TR-8824"),
            Surgery("S-106", "P-6612", "Knee Arthroscopy", "Dr. Wilson", "OT-03", "13:30", currentState = "SCHEDULED", assignedTrayId = "TR-8820"),
            Surgery("S-107", "P-1190", "Inguinal Hernia Repair", "Dr. Miller", "OT-01", "15:00", currentState = "SCHEDULED", assignedTrayId = "TR-8821"),
            // SCENARIO F: Surgery Completed / Turnover
            Surgery("S-108", "P-8801", "Open Femur Reduction", "Dr. Vance", "OT-04", "07:00", actualStart = "07:10", actualEnd = "08:45", currentState = "TURNOVER", assignedTrayId = "TR-4412")
        )
        db.surgeryDao().insertAll(seedSurgeries)

        // 5. Initial Alerts
        val seedAlerts = listOf(
            AlertItem("ALT-001", "CRITICAL", "Tray TR-5012 Sterilization Pending", "Laparoscopic Appendectomy (S-102) is blocked awaiting Sterile Tray TR-5012 in CSSD.", patientId = "P-4432", surgeryId = "S-102", otId = "OT-02", trayId = "TR-5012", department = "CSSD", detectedAt = now - 20 * 60 * 1000, cause = "CSSD / INSTRUMENT"),
            AlertItem("ALT-002", "WARNING", "Patient P-7710 Documentation Delay", "Consent form & pre-op blood work pending for scheduled CABG surgery.", patientId = "P-7710", surgeryId = "S-103", otId = "OT-03", department = "WARD", detectedAt = now - 35 * 60 * 1000, cause = "DOCUMENTATION"),
            AlertItem("ALT-003", "WARNING", "OT-04 Turnover Exceeding Target", "Turnover time for OT-04 has passed 15 minute target threshold.", otId = "OT-04", surgeryId = "S-108", department = "OPERATING_THEATRE", detectedAt = now - 10 * 60 * 1000, cause = "OT_TURNOVER")
        )
        db.alertDao().insertAll(seedAlerts)

        // 6. Initial Events & Audit Logs
        val seedEvents = listOf(
            WorkflowEvent(timestamp = now - 60 * 60 * 1000, eventType = "PATIENT_ADMITTED", entityType = "PATIENT", entityId = "P-9821", userId = "WARD_STAFF", location = "Ward 4A", previousState = "NONE", newState = "ADMITTED", patientId = "P-9821"),
            WorkflowEvent(timestamp = now - 40 * 60 * 1000, eventType = "PATIENT_READY", entityType = "PATIENT", entityId = "P-9821", userId = "WARD_STAFF", location = "Ward 4A", previousState = "PREPARING", newState = "READY", patientId = "P-9821"),
            WorkflowEvent(timestamp = now - 30 * 60 * 1000, eventType = "PATIENT_IN_OT", entityType = "PATIENT", entityId = "P-9821", userId = "OT_STAFF", location = "OT-01", previousState = "IN_TRANSIT", newState = "IN_OT", patientId = "P-9821", otId = "OT-01", surgeryId = "S-101"),
            WorkflowEvent(timestamp = now - 25 * 60 * 1000, eventType = "SURGERY_STARTED", entityType = "SURGERY", entityId = "S-101", userId = "OT_MANAGER", location = "OT-01", previousState = "PATIENT_IN_OT", newState = "SURGERY_ACTIVE", patientId = "P-9821", otId = "OT-01", surgeryId = "S-101"),
            WorkflowEvent(timestamp = now - 20 * 60 * 1000, eventType = "STERILIZATION_STARTED", entityType = "INSTRUMENT_TRAY", entityId = "TR-5012", userId = "CSSD_STAFF", location = "CSSD-Sterilizer-2", previousState = "PACKAGING", newState = "STERILIZATION", trayId = "TR-5012")
        )
        db.workflowEventDao().insertAll(seedEvents)

        val seedLogs = listOf(
            AuditLog(timestamp = now - 60 * 60 * 1000, userRole = "WARD_STAFF", action = "ADMIT_PATIENT", entity = "P-9821", previousState = "NEW", newState = "ADMITTED", notes = "Initial ward intake complete"),
            AuditLog(timestamp = now - 25 * 60 * 1000, userRole = "OT_MANAGER", action = "START_SURGERY", entity = "S-101", previousState = "READY", newState = "SURGERY_ACTIVE", notes = "All dependencies satisfied")
        )
        db.auditLogDao().insertAll(seedLogs)

        // 7. Initial API Keys
        val seedApiKeys = listOf(
            com.example.hospitalworkflow.data.model.ApiKeyEntity("KEY-001", "client_tok_epic_gateway", "Epic EHR Integration Gateway", "FULL_ACCESS", createdAt = now - 86400000 * 5, lastUsedAt = now - 3600000),
            com.example.hospitalworkflow.data.model.ApiKeyEntity("KEY-002", "client_tok_cssd_scanner", "CSSD Mobile Scanner Service", "CSSD_WRITE", createdAt = now - 86400000 * 2, lastUsedAt = now - 7200000)
        )
        db.apiKeyDao().insertOrUpdate(seedApiKeys[0])
        db.apiKeyDao().insertOrUpdate(seedApiKeys[1])

        // 8. Initial Emergency Incidents
        val seedIncidents = listOf(
            com.example.hospitalworkflow.data.model.EmergencyIncident("INC-101", "CSSD Sterilizer #2 Pressure Failure", "STERILIZER_BREAKDOWN", "CSSD", "CRITICAL", "Autoclave #2 lost vacuum pressure during Sterilization cycle. Tray TR-5012 cycle interrupted.", detectedAt = now - 25 * 60 * 1000)
        )
        db.emergencyIncidentDao().insertOrUpdate(seedIncidents[0])
    }

    // --- API KEY & EXTERNAL DATA GATEWAY METHODS ---
    suspend fun createApiKey(clientName: String, scope: String): com.example.hospitalworkflow.data.model.ApiKeyEntity = withContext(Dispatchers.IO) {
        val randomHex = java.util.UUID.randomUUID().toString().replace("-", "").take(12)
        val apiKeyStr = "hk_live_$randomHex"
        val keyId = "KEY-${System.currentTimeMillis().toString().takeLast(4)}"
        val entity = com.example.hospitalworkflow.data.model.ApiKeyEntity(
            id = keyId,
            apiKey = apiKeyStr,
            clientName = clientName,
            scope = scope,
            createdAt = System.currentTimeMillis()
        )
        db.apiKeyDao().insertOrUpdate(entity)
        logEventAndAudit("API_KEY_CREATED", "API_KEY", keyId, "ADMIN", "Gateway", "NONE", scope, notes = "API Key created for $clientName")
        entity
    }

    suspend fun revokeApiKey(keyId: String) = withContext(Dispatchers.IO) {
        db.apiKeyDao().deleteById(keyId)
        logEventAndAudit("API_KEY_REVOKED", "API_KEY", keyId, "ADMIN", "Gateway", "ACTIVE", "REVOKED")
    }

    suspend fun simulateExternalDataUpdate(entityType: String, id: String, stateUpdate: String, apiKeyUsed: String): String = withContext(Dispatchers.IO) {
        val activeKey = db.apiKeyDao().getActiveKey(apiKeyUsed)
            ?: return@withContext "Error: Invalid or revoked API key ($apiKeyUsed)"

        when (entityType.uppercase()) {
            "PATIENT" -> {
                val p = db.patientDao().getPatientById(id) ?: return@withContext "Error: Patient $id not found"
                val updated = p.copy(state = stateUpdate, lastUpdated = System.currentTimeMillis())
                db.patientDao().update(updated)
                logEventAndAudit("EXTERNAL_API_PATIENT_UPDATE", "PATIENT", id, "API_CLIENT_${activeKey.clientName}", "External Rest API", p.state, stateUpdate)
            }
            "TRAY" -> {
                updateTrayState(id, stateUpdate, "API_CLIENT_${activeKey.clientName}")
            }
            "OT" -> {
                val ot = db.operatingTheatreDao().getOtById(id) ?: return@withContext "Error: OT $id not found"
                val updated = ot.copy(status = stateUpdate, lastUpdated = System.currentTimeMillis())
                db.operatingTheatreDao().update(updated)
                logEventAndAudit("EXTERNAL_API_OT_UPDATE", "OPERATING_THEATRE", id, "API_CLIENT_${activeKey.clientName}", "External Rest API", ot.status, stateUpdate)
            }
        }
        "Success: $entityType $id updated to '$stateUpdate' via API Key (${activeKey.clientName})"
    }

    // --- AI EMERGENCY & RESOURCE REDISTRIBUTION ENGINE ---
    suspend fun triggerEmergencyIncident(
        title: String,
        type: String,
        department: String,
        severity: String,
        description: String
    ): com.example.hospitalworkflow.data.model.EmergencyIncident = withContext(Dispatchers.IO) {
        val incidentId = "INC-${System.currentTimeMillis().toString().takeLast(4)}"
        val incident = com.example.hospitalworkflow.data.model.EmergencyIncident(
            id = incidentId,
            title = title,
            incidentType = type,
            affectedDepartment = department,
            severity = severity,
            description = description
        )
        db.emergencyIncidentDao().insertOrUpdate(incident)

        val alert = AlertItem(
            id = "ALT-EMG-${incidentId}",
            severity = severity,
            title = "EMERGENCY INCIDENT: $title",
            message = description,
            department = department,
            cause = "MODULE_FAILURE / EMERGENCY"
        )
        db.alertDao().insertOrUpdate(alert)

        logEventAndAudit("EMERGENCY_INCIDENT_TRIGGERED", "EMERGENCY", incidentId, "SYSTEM", department, "NORMAL", severity, notes = title)
        incident
    }

    suspend fun runAiEmergencyAnalysis(incidentId: String, userApiKey: String? = null): Result<com.example.hospitalworkflow.data.ai.AiResourceAllocationPlan> = withContext(Dispatchers.IO) {
        val incident = db.emergencyIncidentDao().getIncidentById(incidentId)
            ?: return@withContext Result.failure(Exception("Incident $incidentId not found"))

        val patientsList = db.patientDao().getAllPatients().first().joinToString { "${it.id}: ${it.name} (${it.procedure}, ${it.state}, Priority: ${it.priority})" }
        val surgeriesList = db.surgeryDao().getAllSurgeries().first().joinToString { "${it.id}: ${it.procedure} (${it.currentState}, OT: ${it.otId}, Tray: ${it.assignedTrayId}, Blocked: ${it.isBlocked})" }
        val otsList = db.operatingTheatreDao().getAllOts().first().joinToString { "${it.id}: ${it.name} (${it.status}, CurrentSurgery: ${it.currentSurgeryId})" }
        val traysList = db.instrumentTrayDao().getAllTrays().first().joinToString { "${it.id}: ${it.trayType} (${it.currentState}, Sterility: ${it.sterilityStatus}, AssignedSurgery: ${it.assignedSurgeryId})" }
        val alertsList = db.alertDao().getAllAlerts().first().filter { it.status != "RESOLVED" }.joinToString { "${it.id}: ${it.title} (${it.severity})" }

        val res = com.example.hospitalworkflow.data.ai.GeminiApiService.analyzeEmergencyAndAllocateResources(
            incidentDescription = "${incident.title} - ${incident.description} (Department: ${incident.affectedDepartment}, Severity: ${incident.severity})",
            patientsSnapshot = patientsList,
            surgeriesSnapshot = surgeriesList,
            otsSnapshot = otsList,
            traysSnapshot = traysList,
            alertsSnapshot = alertsList,
            customApiKey = userApiKey
        )

        res.onSuccess { plan ->
            val updatedIncident = incident.copy(
                status = "AI_ANALYZED",
                aiPlanJson = plan.rawJsonResponse
            )
            db.emergencyIncidentDao().insertOrUpdate(updatedIncident)
        }

        res
    }

    suspend fun applyAiResourcePlan(plan: com.example.hospitalworkflow.data.ai.AiResourceAllocationPlan, incidentId: String) = withContext(Dispatchers.IO) {
        // 1. Execute OT Re-allocations
        plan.otReallocations.forEach { otAlloc ->
            val surgery = db.surgeryDao().getSurgeryById(otAlloc.surgeryId)
            if (surgery != null) {
                db.surgeryDao().update(surgery.copy(otId = otAlloc.otId, isBlocked = false, blockingCause = null, lastUpdated = System.currentTimeMillis()))
            }
        }

        // 2. Execute Tray Re-routes
        plan.trayReroutes.forEach { trayReroute ->
            val tray = db.instrumentTrayDao().getTrayById(trayReroute.trayId)
            if (tray != null) {
                db.instrumentTrayDao().update(tray.copy(
                    currentState = "STERILE_STORAGE",
                    sterilityStatus = "VALID",
                    validationStatus = "READY",
                    assignedOtId = trayReroute.targetOtId,
                    assignedSurgeryId = trayReroute.targetSurgeryId,
                    location = "Re-routed to ${trayReroute.targetOtId}",
                    lastUpdated = System.currentTimeMillis()
                ))
            }
            val surgery = db.surgeryDao().getSurgeryById(trayReroute.targetSurgeryId)
            if (surgery != null) {
                db.surgeryDao().update(surgery.copy(assignedTrayId = trayReroute.trayId, isBlocked = false, blockingCause = null, lastUpdated = System.currentTimeMillis()))
            }
        }

        // 3. Execute Patient Priority Updates
        plan.patientPriorityUpdates.forEach { prio ->
            val patient = db.patientDao().getPatientById(prio.patientId)
            if (patient != null) {
                db.patientDao().update(patient.copy(priority = prio.newPriority, lastUpdated = System.currentTimeMillis()))
            }
        }

        // 4. Mark Incident & Alerts as RESOLVED / AI_APPLIED
        val incident = db.emergencyIncidentDao().getIncidentById(incidentId)
        if (incident != null) {
            db.emergencyIncidentDao().insertOrUpdate(incident.copy(status = "RESOLVED"))
        }

        logEventAndAudit("AI_RESOURCE_PLAN_APPLIED", "EMERGENCY", incidentId, "AI_DISPATCHER", "Hospital-Wide", "ACTIVE", "RESOLVED", notes = "Applied AI re-allocations for $incidentId")
    }

    // --- DEPENDENCY CALCULATOR ---
    suspend fun calculateDependenciesForSurgery(surgeryId: String): SurgeryDependencyStatus = withContext(Dispatchers.IO) {
        val surgery = db.surgeryDao().getSurgeryById(surgeryId)
            ?: return@withContext SurgeryDependencyStatus(surgeryId, false, false, false, false, false, WorkflowStatus.BLOCKED, listOf("Surgery not found"))

        val patient = db.patientDao().getPatientById(surgery.patientId)
        val ot = db.operatingTheatreDao().getOtById(surgery.otId)
        val tray = surgery.assignedTrayId?.let { db.instrumentTrayDao().getTrayById(it) }

        val causes = mutableListOf<String>()

        val isPatientReady = patient?.let {
            val readyState = it.state == "READY" || it.state == "IN_TRANSIT" || it.state == "IN_OT"
            val checksReady = it.consentVerified && it.preOpComplete && it.anaesthesiaClearance && it.documentationComplete && it.patientPrepared
            if (!readyState || !checksReady) {
                if (!it.consentVerified) causes.add("Patient missing verified Consent")
                if (!it.preOpComplete) causes.add("Pre-operative checklist incomplete")
                if (!it.anaesthesiaClearance) causes.add("Anaesthesia clearance pending")
                if (it.blockingReason != null) causes.add("Patient Blocked: ${it.blockingReason}")
            }
            readyState && checksReady
        } ?: run {
            causes.add("Patient record missing")
            false
        }

        val isOtReady = ot?.let {
            val otOk = it.status == "AVAILABLE" || it.status == "PREPARING" || it.status == "SURGERY_ACTIVE" || it.status == "PATIENT_IN_OT"
            if (!otOk) causes.add("OT ${it.id} unavailable (Current state: ${it.status})")
            otOk
        } ?: run {
            causes.add("Operating Theatre not assigned")
            false
        }

        val isStaffReady = true // Baseline operational assumption
        val isDocReady = patient?.documentationComplete ?: false
        if (!isDocReady) causes.add("Surgical documentation missing")

        val isTrayReady = if (surgery.assignedTrayId == null) {
            causes.add("No sterile instrument tray assigned to surgery")
            false
        } else tray?.let {
            val isSterile = it.sterilityStatus == "VALID"
            val isValidated = it.validationStatus == "READY"
            val isAvailableState = it.currentState == "STERILE_STORAGE" || it.currentState == "DISPATCHED" || it.currentState == "IN_OT"
            if (!isSterile || !isValidated || !isAvailableState) {
                causes.add("Tray ${it.id} (${it.trayType}) is in '${it.currentState}' stage - Sterility: ${it.sterilityStatus}, Validation: ${it.validationStatus}")
            }
            isSterile && isValidated && isAvailableState
        } ?: run {
            causes.add("Assigned tray ${surgery.assignedTrayId} not found in inventory")
            false
        }

        val isAllReady = isPatientReady && isOtReady && isStaffReady && isDocReady && isTrayReady
        val status = if (isAllReady) WorkflowStatus.READY else WorkflowStatus.BLOCKED

        SurgeryDependencyStatus(
            surgeryId = surgeryId,
            isPatientReady = isPatientReady,
            isOtReady = isOtReady,
            isStaffReady = isStaffReady,
            isDocumentationReady = isDocReady,
            isInstrumentReady = isTrayReady,
            overallStatus = status,
            blockingCauses = causes
        )
    }

    // --- CASCADING BUSINESS ACTIONS ---

    suspend fun updateTrayState(trayId: String, newState: String, operatorRole: String = "CSSD_STAFF") = withContext(Dispatchers.IO) {
        val tray = db.instrumentTrayDao().getTrayById(trayId) ?: return@withContext
        val prevState = tray.currentState

        val (sterility, validation) = when (newState) {
            "STERILE_STORAGE", "DISPATCHED", "IN_OT" -> Pair("VALID", "READY")
            "STERILIZATION", "VALIDATION" -> Pair("PENDING", "PENDING")
            "CLEANING", "INSPECTION", "PACKAGING", "DECONTAMINATION" -> Pair("PENDING", "PENDING")
            "USED", "RETURNED", "BLOCKED" -> Pair("EXPIRED", "BLOCKED")
            else -> Pair(tray.sterilityStatus, tray.validationStatus)
        }

        val updatedTray = tray.copy(
            currentState = newState,
            sterilityStatus = sterility,
            validationStatus = validation,
            lastUpdated = System.currentTimeMillis()
        )
        db.instrumentTrayDao().update(updatedTray)

        logEventAndAudit("TRAY_STATE_CHANGE", "INSTRUMENT_TRAY", trayId, operatorRole, tray.location, prevState, newState, trayId = trayId)

        // Cascade update: Re-evaluate any surgery assigned to this tray
        tray.assignedSurgeryId?.let { surgeryId ->
            recalculateAndApplySurgeryState(surgeryId, operatorRole)
        }
    }

    suspend fun validateTray(trayId: String, isValidated: Boolean, operatorRole: String = "CSSD_STAFF") = withContext(Dispatchers.IO) {
        val tray = db.instrumentTrayDao().getTrayById(trayId) ?: return@withContext
        val prevState = tray.validationStatus
        val newValidation = if (isValidated) "READY" else "BLOCKED"
        val newSterility = if (isValidated) "VALID" else "FAILED"

        val updated = tray.copy(
            validationStatus = newValidation,
            sterilityStatus = newSterility,
            currentState = if (isValidated) "STERILE_STORAGE" else "BLOCKED",
            lastUpdated = System.currentTimeMillis()
        )
        db.instrumentTrayDao().update(updated)

        logEventAndAudit("TRAY_VALIDATION", "INSTRUMENT_TRAY", trayId, operatorRole, tray.location, prevState, newValidation, trayId = trayId)

        tray.assignedSurgeryId?.let { surgeryId ->
            recalculateAndApplySurgeryState(surgeryId, operatorRole)
        }
    }

    suspend fun dispatchTrayToOt(trayId: String, otId: String, surgeryId: String, operatorRole: String = "CSSD_STAFF") = withContext(Dispatchers.IO) {
        val tray = db.instrumentTrayDao().getTrayById(trayId) ?: return@withContext
        val updated = tray.copy(
            currentState = "DISPATCHED",
            location = "En Route to $otId",
            assignedOtId = otId,
            assignedSurgeryId = surgeryId,
            lastUpdated = System.currentTimeMillis()
        )
        db.instrumentTrayDao().update(updated)

        logEventAndAudit("TRAY_DISPATCH", "INSTRUMENT_TRAY", trayId, operatorRole, "CSSD", tray.currentState, "DISPATCHED", otId = otId, surgeryId = surgeryId, trayId = trayId)

        recalculateAndApplySurgeryState(surgeryId, operatorRole)
    }

    suspend fun updatePatientReadiness(
        patientId: String,
        consent: Boolean,
        preOp: Boolean,
        anaesthesia: Boolean,
        documentation: Boolean,
        patientPrepared: Boolean,
        blockingReason: String?,
        operatorRole: String = "WARD_STAFF"
    ) = withContext(Dispatchers.IO) {
        val patient = db.patientDao().getPatientById(patientId) ?: return@withContext
        val isAllChecksReady = consent && preOp && anaesthesia && documentation && patientPrepared
        val newPatientState = if (isAllChecksReady) "READY" else "PREPARING"

        val updated = patient.copy(
            consentVerified = consent,
            preOpComplete = preOp,
            anaesthesiaClearance = anaesthesia,
            documentationComplete = documentation,
            patientPrepared = patientPrepared,
            state = if (patient.state == "ADMITTED" || patient.state == "PREPARING") newPatientState else patient.state,
            blockingReason = if (isAllChecksReady) null else blockingReason,
            lastUpdated = System.currentTimeMillis()
        )
        db.patientDao().update(updated)

        logEventAndAudit("PATIENT_READINESS_UPDATE", "PATIENT", patientId, operatorRole, patient.ward, patient.state, newPatientState, patientId = patientId)

        patient.surgeryId?.let { surgeryId ->
            recalculateAndApplySurgeryState(surgeryId, operatorRole)
        }
    }

    suspend fun requestPatientTransfer(patientId: String, operatorRole: String = "WARD_STAFF") = withContext(Dispatchers.IO) {
        val patient = db.patientDao().getPatientById(patientId) ?: return@withContext
        val updated = patient.copy(state = "TRANSFER_REQUESTED", lastUpdated = System.currentTimeMillis())
        db.patientDao().update(updated)

        logEventAndAudit("TRANSFER_REQUESTED", "PATIENT", patientId, operatorRole, patient.ward, patient.state, "TRANSFER_REQUESTED", patientId = patientId)

        patient.surgeryId?.let { surgeryId ->
            recalculateAndApplySurgeryState(surgeryId, operatorRole)
        }
    }

    suspend fun markPatientInTransit(patientId: String, operatorRole: String = "WARD_STAFF") = withContext(Dispatchers.IO) {
        val patient = db.patientDao().getPatientById(patientId) ?: return@withContext
        val updated = patient.copy(state = "IN_TRANSIT", lastUpdated = System.currentTimeMillis())
        db.patientDao().update(updated)

        logEventAndAudit("PATIENT_IN_TRANSIT", "PATIENT", patientId, operatorRole, "En Route to OT", patient.state, "IN_TRANSIT", patientId = patientId)

        patient.surgeryId?.let { surgeryId ->
            val surgery = db.surgeryDao().getSurgeryById(surgeryId)
            if (surgery != null) {
                db.surgeryDao().update(surgery.copy(currentState = "PATIENT_TRANSFER"))
            }
        }
    }

    suspend fun confirmPatientInOt(patientId: String, otId: String, operatorRole: String = "OT_STAFF") = withContext(Dispatchers.IO) {
        val patient = db.patientDao().getPatientById(patientId) ?: return@withContext
        db.patientDao().update(patient.copy(state = "IN_OT", assignedOtId = otId, lastUpdated = System.currentTimeMillis()))

        val ot = db.operatingTheatreDao().getOtById(otId)
        if (ot != null) {
            db.operatingTheatreDao().update(ot.copy(status = "PATIENT_IN_OT", currentPatientId = patientId, lastUpdated = System.currentTimeMillis()))
        }

        patient.surgeryId?.let { surgeryId ->
            val surgery = db.surgeryDao().getSurgeryById(surgeryId)
            if (surgery != null) {
                db.surgeryDao().update(surgery.copy(currentState = "PATIENT_IN_OT", otId = otId))
            }
        }

        logEventAndAudit("PATIENT_ARRIVED_OT", "PATIENT", patientId, operatorRole, otId, patient.state, "IN_OT", patientId = patientId, otId = otId)
    }

    suspend fun startSurgery(surgeryId: String, operatorRole: String = "OT_MANAGER") = withContext(Dispatchers.IO) {
        val surgery = db.surgeryDao().getSurgeryById(surgeryId) ?: return@withContext
        val nowTime = "08:30" // Simplified display time format
        val updatedSurgery = surgery.copy(
            currentState = "SURGERY_ACTIVE",
            actualStart = nowTime,
            isBlocked = false,
            blockingCause = null,
            lastUpdated = System.currentTimeMillis()
        )
        db.surgeryDao().update(updatedSurgery)

        val ot = db.operatingTheatreDao().getOtById(surgery.otId)
        if (ot != null) {
            db.operatingTheatreDao().update(ot.copy(status = "SURGERY_ACTIVE", currentSurgeryId = surgeryId, currentPatientId = surgery.patientId))
        }

        surgery.assignedTrayId?.let { trayId ->
            val tray = db.instrumentTrayDao().getTrayById(trayId)
            if (tray != null) {
                db.instrumentTrayDao().update(tray.copy(currentState = "IN_OT", location = surgery.otId))
            }
        }

        logEventAndAudit("SURGERY_STARTED", "SURGERY", surgeryId, operatorRole, surgery.otId, surgery.currentState, "SURGERY_ACTIVE", patientId = surgery.patientId, surgeryId = surgeryId, otId = surgery.otId)
    }

    suspend fun completeSurgery(surgeryId: String, operatorRole: String = "OT_STAFF") = withContext(Dispatchers.IO) {
        val surgery = db.surgeryDao().getSurgeryById(surgeryId) ?: return@withContext
        val now = System.currentTimeMillis()
        val updatedSurgery = surgery.copy(
            currentState = "SURGERY_COMPLETED",
            actualEnd = "10:15",
            lastUpdated = now
        )
        db.surgeryDao().update(updatedSurgery)

        // Move patient to RECOVERY
        val patient = db.patientDao().getPatientById(surgery.patientId)
        if (patient != null) {
            db.patientDao().update(patient.copy(state = "RECOVERY", lastUpdated = now))
        }

        // Move OT to TURNOVER
        val ot = db.operatingTheatreDao().getOtById(surgery.otId)
        if (ot != null) {
            db.operatingTheatreDao().update(ot.copy(
                status = "TURNOVER",
                currentSurgeryId = null,
                currentPatientId = null,
                turnoverStartTimestamp = now,
                lastUpdated = now
            ))
        }

        // Mark tray as RETURNED
        surgery.assignedTrayId?.let { trayId ->
            val tray = db.instrumentTrayDao().getTrayById(trayId)
            if (tray != null) {
                db.instrumentTrayDao().update(tray.copy(
                    currentState = "RETURNED",
                    location = "${surgery.otId} Dirty Utility",
                    sterilityStatus = "EXPIRED",
                    validationStatus = "PENDING",
                    lastUpdated = now
                ))
            }
        }

        logEventAndAudit("SURGERY_COMPLETED", "SURGERY", surgeryId, operatorRole, surgery.otId, "SURGERY_ACTIVE", "SURGERY_COMPLETED", patientId = surgery.patientId, surgeryId = surgeryId, otId = surgery.otId)
    }

    suspend fun finishOtTurnover(otId: String, operatorRole: String = "OT_STAFF") = withContext(Dispatchers.IO) {
        val ot = db.operatingTheatreDao().getOtById(otId) ?: return@withContext
        val updated = ot.copy(
            status = "AVAILABLE",
            turnoverStartTimestamp = null,
            lastUpdated = System.currentTimeMillis()
        )
        db.operatingTheatreDao().update(updated)

        logEventAndAudit("OT_TURNOVER_FINISHED", "OPERATING_THEATRE", otId, operatorRole, otId, "TURNOVER", "AVAILABLE", otId = otId)
    }

    suspend fun acknowledgeAlert(alertId: String, operatorRole: String) = withContext(Dispatchers.IO) {
        val alert = db.alertDao().getAlertById(alertId) ?: return@withContext
        db.alertDao().insertOrUpdate(alert.copy(status = "ACKNOWLEDGED"))
        logEventAndAudit("ALERT_ACKNOWLEDGED", "ALERT", alertId, operatorRole, "SYSTEM", alert.status, "ACKNOWLEDGED")
    }

    suspend fun resolveAlert(alertId: String, operatorRole: String) = withContext(Dispatchers.IO) {
        val alert = db.alertDao().getAlertById(alertId) ?: return@withContext
        db.alertDao().insertOrUpdate(alert.copy(status = "RESOLVED"))
        logEventAndAudit("ALERT_RESOLVED", "ALERT", alertId, operatorRole, "SYSTEM", alert.status, "RESOLVED")
    }

    private suspend fun recalculateAndApplySurgeryState(surgeryId: String, operatorRole: String) {
        val dep = calculateDependenciesForSurgery(surgeryId)
        val surgery = db.surgeryDao().getSurgeryById(surgeryId) ?: return

        val isNowBlocked = dep.overallStatus == WorkflowStatus.BLOCKED
        val primaryCause = dep.blockingCauses.firstOrNull()

        if (surgery.currentState != "SURGERY_ACTIVE" && surgery.currentState != "SURGERY_COMPLETED" && surgery.currentState != "CLOSED") {
            val newState = if (dep.overallStatus == WorkflowStatus.READY) "READY" else "PREPARING"
            val updatedSurgery = surgery.copy(
                isBlocked = isNowBlocked,
                blockingCause = primaryCause,
                currentState = newState,
                lastUpdated = System.currentTimeMillis()
            )
            db.surgeryDao().update(updatedSurgery)

            // Auto-resolve or create Alert
            if (!isNowBlocked) {
                // If blocked alert existed for this surgery, resolve it!
                val existingAlerts = db.alertDao().getAllAlerts().first()
                existingAlerts.filter { it.surgeryId == surgeryId && it.status != "RESOLVED" }.forEach {
                    db.alertDao().insertOrUpdate(it.copy(status = "RESOLVED"))
                }
            } else if (primaryCause != null) {
                val alert = AlertItem(
                    id = "ALT-${System.currentTimeMillis().toString().takeLast(4)}",
                    severity = if (primaryCause.contains("CSSD") || primaryCause.contains("Consent")) "CRITICAL" else "WARNING",
                    title = "Surgery ${surgery.id} Blocked",
                    message = primaryCause,
                    surgeryId = surgeryId,
                    patientId = surgery.patientId,
                    otId = surgery.otId,
                    department = "OPERATIONS",
                    cause = primaryCause
                )
                db.alertDao().insertOrUpdate(alert)
            }
        }
    }

    private suspend fun logEventAndAudit(
        eventType: String,
        entityType: String,
        entityId: String,
        role: String,
        location: String,
        prevState: String,
        newState: String,
        patientId: String? = null,
        surgeryId: String? = null,
        otId: String? = null,
        trayId: String? = null,
        notes: String = ""
    ) {
        val event = WorkflowEvent(
            eventType = eventType,
            entityType = entityType,
            entityId = entityId,
            userId = role,
            location = location,
            previousState = prevState,
            newState = newState,
            patientId = patientId,
            surgeryId = surgeryId,
            otId = otId,
            trayId = trayId,
            details = if (notes.isNotBlank()) notes else "Transitioned from $prevState to $newState"
        )
        db.workflowEventDao().insert(event)

        val log = AuditLog(
            userRole = role,
            action = eventType,
            entity = "$entityType: $entityId",
            previousState = prevState,
            newState = newState,
            notes = if (notes.isNotBlank()) notes else "Action performed by $role in $location"
        )
        db.auditLogDao().insert(log)
    }
}
