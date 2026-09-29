package com.example.hospitalworkflow.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hospitalworkflow.data.local.HospitalDatabase
import com.example.hospitalworkflow.data.model.AlertItem
import com.example.hospitalworkflow.data.model.AuditLog
import com.example.hospitalworkflow.data.model.InstrumentTray
import com.example.hospitalworkflow.data.model.OperatingTheatre
import com.example.hospitalworkflow.data.model.Patient
import com.example.hospitalworkflow.data.model.Surgery
import com.example.hospitalworkflow.data.model.UserRole
import com.example.hospitalworkflow.data.repository.HospitalRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HospitalViewModel(application: Application) : AndroidViewModel(application) {
    private val db = HospitalDatabase.getDatabase(application)
    val repository = HospitalRepository(db)

    val currentRole = androidx.compose.runtime.mutableStateOf(UserRole.ADMIN)

    val patients: StateFlow<List<Patient>> = repository.patients.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val surgeries: StateFlow<List<Surgery>> = repository.surgeries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val ots: StateFlow<List<OperatingTheatre>> = repository.operatingTheatres.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val trays: StateFlow<List<InstrumentTray>> = repository.instrumentTrays.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val alerts: StateFlow<List<AlertItem>> = repository.alerts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val auditLogs: StateFlow<List<AuditLog>> = repository.auditLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val workflowEvents: StateFlow<List<com.example.hospitalworkflow.data.model.WorkflowEvent>> = repository.workflowEvents.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val apiKeys: StateFlow<List<com.example.hospitalworkflow.data.model.ApiKeyEntity>> = repository.apiKeys.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val emergencyIncidents: StateFlow<List<com.example.hospitalworkflow.data.model.EmergencyIncident>> = repository.emergencyIncidents.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun createApiKey(clientName: String, scope: String) {
        viewModelScope.launch {
            repository.createApiKey(clientName, scope)
        }
    }

    fun revokeApiKey(keyId: String) {
        viewModelScope.launch {
            repository.revokeApiKey(keyId)
        }
    }

    fun simulateExternalApiUpdate(entityType: String, id: String, stateUpdate: String, apiKeyUsed: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val res = repository.simulateExternalDataUpdate(entityType, id, stateUpdate, apiKeyUsed)
            onResult(res)
        }
    }

    fun triggerEmergencyIncident(title: String, type: String, department: String, severity: String, description: String, onResult: (com.example.hospitalworkflow.data.model.EmergencyIncident) -> Unit) {
        viewModelScope.launch {
            val incident = repository.triggerEmergencyIncident(title, type, department, severity, description)
            onResult(incident)
        }
    }

    val userApiKey = androidx.compose.runtime.mutableStateOf(
        com.example.hospitalworkflow.data.settings.SettingsManager.getGoogleAiApiKey(application)
    )

    fun saveUserApiKey(apiKey: String) {
        val trimmed = apiKey.trim()
        com.example.hospitalworkflow.data.settings.SettingsManager.saveGoogleAiApiKey(getApplication(), trimmed)
        userApiKey.value = trimmed
    }

    fun clearUserApiKey() {
        com.example.hospitalworkflow.data.settings.SettingsManager.clearGoogleAiApiKey(getApplication())
        userApiKey.value = ""
    }

    fun testUserApiKey(apiKey: String, onResult: (Result<Boolean>) -> Unit) {
        viewModelScope.launch {
            val res = com.example.hospitalworkflow.data.ai.GeminiApiService.testApiKeyConnection(apiKey)
            onResult(res)
        }
    }

    fun runAiEmergencyAnalysis(incidentId: String, onResult: (Result<com.example.hospitalworkflow.data.ai.AiResourceAllocationPlan>) -> Unit) {
        viewModelScope.launch {
            val res = repository.runAiEmergencyAnalysis(incidentId, userApiKey.value)
            onResult(res)
        }
    }

    fun applyAiResourcePlan(plan: com.example.hospitalworkflow.data.ai.AiResourceAllocationPlan, incidentId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.applyAiResourcePlan(plan, incidentId)
            onComplete()
        }
    }

    fun setRole(role: UserRole) {
        currentRole.value = role
    }

    fun updateTrayState(trayId: String, newState: String) {
        viewModelScope.launch {
            repository.updateTrayState(trayId, newState, currentRole.value.name)
        }
    }

    fun validateTray(trayId: String, isValidated: Boolean) {
        viewModelScope.launch {
            repository.validateTray(trayId, isValidated, currentRole.value.name)
        }
    }

    fun dispatchTray(trayId: String, otId: String, surgeryId: String) {
        viewModelScope.launch {
            repository.dispatchTrayToOt(trayId, otId, surgeryId, currentRole.value.name)
        }
    }

    fun updatePatientReadiness(
        patientId: String,
        consent: Boolean,
        preOp: Boolean,
        anaesthesia: Boolean,
        doc: Boolean,
        prepared: Boolean,
        cause: String?
    ) {
        viewModelScope.launch {
            repository.updatePatientReadiness(patientId, consent, preOp, anaesthesia, doc, prepared, cause, currentRole.value.name)
        }
    }

    fun requestPatientTransfer(patientId: String) {
        viewModelScope.launch {
            repository.requestPatientTransfer(patientId, currentRole.value.name)
        }
    }

    fun markPatientInTransit(patientId: String) {
        viewModelScope.launch {
            repository.markPatientInTransit(patientId, currentRole.value.name)
        }
    }

    fun confirmPatientInOt(patientId: String, otId: String) {
        viewModelScope.launch {
            repository.confirmPatientInOt(patientId, otId, currentRole.value.name)
        }
    }

    fun startSurgery(surgeryId: String) {
        viewModelScope.launch {
            repository.startSurgery(surgeryId, currentRole.value.name)
        }
    }

    fun completeSurgery(surgeryId: String) {
        viewModelScope.launch {
            repository.completeSurgery(surgeryId, currentRole.value.name)
        }
    }

    fun finishOtTurnover(otId: String) {
        viewModelScope.launch {
            repository.finishOtTurnover(otId, currentRole.value.name)
        }
    }

    fun acknowledgeAlert(alertId: String) {
        viewModelScope.launch {
            repository.acknowledgeAlert(alertId, currentRole.value.name)
        }
    }

    fun resolveAlert(alertId: String) {
        viewModelScope.launch {
            repository.resolveAlert(alertId, currentRole.value.name)
        }
    }

    fun resetData() {
        viewModelScope.launch {
            repository.seedInitialData()
        }
    }
}
