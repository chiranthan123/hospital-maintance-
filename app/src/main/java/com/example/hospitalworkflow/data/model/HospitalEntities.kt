package com.example.hospitalworkflow.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole(val displayName: String, val description: String) {
    ADMIN("Hospital Administration", "Full operational oversight, analytics, and system audit"),
    OT_MANAGER("OT Manager", "Surgical scheduling, OT allocation, delay management"),
    WARD_STAFF("Ward Staff", "Patient pre-op preparation, readiness checklist, transfer requests"),
    CSSD_STAFF("CSSD Staff", "Instrument decontamination, sterilization, validation, dispatch"),
    OT_STAFF("Surgical Team", "In-OT patient receipt, instrument verification, surgery workflow")
}

enum class WorkflowStatus(val label: String) {
    READY("READY"),
    PENDING("PENDING"),
    BLOCKED("BLOCKED"),
    ACTIVE("ACTIVE"),
    DELAYED("DELAYED"),
    AVAILABLE("AVAILABLE"),
    COMPLETED("COMPLETED"),
    IN_TRANSIT("IN TRANSIT"),
    PREPARING("PREPARING"),
    WARNING("WARNING"),
    CRITICAL("CRITICAL")
}

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey val id: String, // e.g. "P-9821"
    val name: String,
    val ward: String,
    val procedure: String,
    val assignedOtId: String? = null,
    val surgeryId: String? = null,
    val scheduledTime: String,
    val state: String, // ADMITTED, PREPARING, READY, TRANSFER_REQUESTED, IN_TRANSIT, IN_OT, RECOVERY, COMPLETED
    val priority: String = "NORMAL", // NORMAL, URGENT, EMERGENCY
    val admissionComplete: Boolean = true,
    val procedureConfirmed: Boolean = true,
    val consentVerified: Boolean = true,
    val preOpComplete: Boolean = true,
    val anaesthesiaClearance: Boolean = true,
    val documentationComplete: Boolean = true,
    val patientPrepared: Boolean = true,
    val transportAvailable: Boolean = true,
    val blockingReason: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "surgeries")
data class Surgery(
    @PrimaryKey val id: String, // e.g. "S-101"
    val patientId: String,
    val procedure: String,
    val surgeon: String,
    val otId: String,
    val scheduledStart: String,
    val actualStart: String? = null,
    val actualEnd: String? = null,
    val currentState: String, // SCHEDULED, PREPARING, READY, PATIENT_TRANSFER, PATIENT_IN_OT, SURGERY_ACTIVE, SURGERY_COMPLETED, TURNOVER, CLOSED
    val assignedTrayId: String? = null,
    val isBlocked: Boolean = false,
    val blockingCause: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "operating_theatres")
data class OperatingTheatre(
    @PrimaryKey val id: String, // e.g. "OT-01"
    val name: String,
    val status: String, // AVAILABLE, PREPARING, PATIENT_WAITING, PATIENT_IN_TRANSIT, PATIENT_IN_OT, SURGERY_ACTIVE, TURNOVER, CLEANING, BLOCKED
    val currentSurgeryId: String? = null,
    val currentPatientId: String? = null,
    val turnoverStartTimestamp: Long? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "instrument_trays")
data class InstrumentTray(
    @PrimaryKey val id: String, // e.g. "TR-2048"
    val trayType: String,
    val currentState: String, // USED, COLLECTED, DECONTAMINATION, CLEANING, INSPECTION, PACKAGING, STERILIZATION, VALIDATION, STERILE_STORAGE, DISPATCHED, IN_OT, RETURNED, BLOCKED
    val location: String,
    val sterilityStatus: String = "VALID", // VALID, PENDING, FAILED, EXPIRED
    val validationStatus: String = "READY", // READY, PENDING, BLOCKED
    val assignedOtId: String? = null,
    val assignedSurgeryId: String? = null,
    val cycleId: String? = null,
    val operator: String = "Tech-CSSD",
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "workflow_events")
data class WorkflowEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String,
    val entityType: String,
    val entityId: String,
    val userId: String,
    val location: String,
    val previousState: String,
    val newState: String,
    val patientId: String? = null,
    val surgeryId: String? = null,
    val otId: String? = null,
    val trayId: String? = null,
    val details: String = ""
)

@Entity(tableName = "alerts")
data class AlertItem(
    @PrimaryKey val id: String,
    val severity: String, // WARNING, CRITICAL
    val title: String,
    val message: String,
    val patientId: String? = null,
    val surgeryId: String? = null,
    val otId: String? = null,
    val trayId: String? = null,
    val department: String,
    val detectedAt: Long = System.currentTimeMillis(),
    val status: String = "OPEN", // OPEN, ACKNOWLEDGED, IN_PROGRESS, RESOLVED
    val cause: String
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val userRole: String,
    val action: String,
    val entity: String,
    val previousState: String,
    val newState: String,
    val notes: String = ""
)

@Entity(tableName = "api_keys")
data class ApiKeyEntity(
    @PrimaryKey val id: String, // e.g. "key_101"
    val apiKey: String, // e.g. "hk_live_c92a71b308f1"
    val clientName: String, // e.g. "EHR Integration Pipeline"
    val scope: String = "FULL_ACCESS", // READ_ONLY, WARD_WRITE, CSSD_WRITE, FULL_ACCESS
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long? = null,
    val isActive: Boolean = true
)

@Entity(tableName = "emergency_incidents")
data class EmergencyIncident(
    @PrimaryKey val id: String, // e.g. "INC-101"
    val title: String,
    val incidentType: String, // MODULE_FAILURE, SURGE_ADMISSION, POWER_HVAC_OFFLINE, STERILIZER_BREAKDOWN
    val affectedDepartment: String,
    val severity: String, // CRITICAL, HIGH, MEDIUM
    val description: String,
    val status: String = "ACTIVE", // ACTIVE, AI_ANALYZED, RESOLVED
    val aiPlanJson: String? = null,
    val detectedAt: Long = System.currentTimeMillis()
)

data class SurgeryDependencyStatus(
    val surgeryId: String,
    val isPatientReady: Boolean,
    val isOtReady: Boolean,
    val isStaffReady: Boolean,
    val isDocumentationReady: Boolean,
    val isInstrumentReady: Boolean,
    val overallStatus: WorkflowStatus,
    val blockingCauses: List<String>
)
