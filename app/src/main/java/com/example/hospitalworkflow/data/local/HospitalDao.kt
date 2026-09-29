package com.example.hospitalworkflow.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.hospitalworkflow.data.model.AlertItem
import com.example.hospitalworkflow.data.model.AuditLog
import com.example.hospitalworkflow.data.model.InstrumentTray
import com.example.hospitalworkflow.data.model.OperatingTheatre
import com.example.hospitalworkflow.data.model.Patient
import com.example.hospitalworkflow.data.model.Surgery
import com.example.hospitalworkflow.data.model.WorkflowEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    @Query("SELECT * FROM patients ORDER BY scheduledTime ASC")
    fun getAllPatients(): Flow<List<Patient>>

    @Query("SELECT * FROM patients WHERE id = :id")
    suspend fun getPatientById(id: String): Patient?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(patient: Patient)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(patients: List<Patient>)

    @Update
    suspend fun update(patient: Patient)
}

@Dao
interface SurgeryDao {
    @Query("SELECT * FROM surgeries ORDER BY scheduledStart ASC")
    fun getAllSurgeries(): Flow<List<Surgery>>

    @Query("SELECT * FROM surgeries WHERE id = :id")
    suspend fun getSurgeryById(id: String): Surgery?

    @Query("SELECT * FROM surgeries WHERE otId = :otId")
    suspend fun getSurgeriesForOt(otId: String): List<Surgery>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(surgery: Surgery)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(surgeries: List<Surgery>)

    @Update
    suspend fun update(surgery: Surgery)
}

@Dao
interface OperatingTheatreDao {
    @Query("SELECT * FROM operating_theatres ORDER BY id ASC")
    fun getAllOts(): Flow<List<OperatingTheatre>>

    @Query("SELECT * FROM operating_theatres WHERE id = :id")
    suspend fun getOtById(id: String): OperatingTheatre?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(ot: OperatingTheatre)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ots: List<OperatingTheatre>)

    @Update
    suspend fun update(ot: OperatingTheatre)
}

@Dao
interface InstrumentTrayDao {
    @Query("SELECT * FROM instrument_trays ORDER BY lastUpdated DESC")
    fun getAllTrays(): Flow<List<InstrumentTray>>

    @Query("SELECT * FROM instrument_trays WHERE id = :id")
    suspend fun getTrayById(id: String): InstrumentTray?

    @Query("SELECT * FROM instrument_trays WHERE assignedSurgeryId = :surgeryId")
    suspend fun getTraysForSurgery(surgeryId: String): List<InstrumentTray>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(tray: InstrumentTray)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(trays: List<InstrumentTray>)

    @Update
    suspend fun update(tray: InstrumentTray)
}

@Dao
interface WorkflowEventDao {
    @Query("SELECT * FROM workflow_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<WorkflowEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: WorkflowEvent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<WorkflowEvent>)
}

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts ORDER BY detectedAt DESC")
    fun getAllAlerts(): Flow<List<AlertItem>>

    @Query("SELECT * FROM alerts WHERE id = :id")
    suspend fun getAlertById(id: String): AlertItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(alert: AlertItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(alerts: List<AlertItem>)

    @Query("DELETE FROM alerts WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: AuditLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<AuditLog>)
}

@Dao
interface ApiKeyDao {
    @Query("SELECT * FROM api_keys ORDER BY createdAt DESC")
    fun getAllApiKeys(): Flow<List<com.example.hospitalworkflow.data.model.ApiKeyEntity>>

    @Query("SELECT * FROM api_keys WHERE apiKey = :key AND isActive = 1")
    suspend fun getActiveKey(key: String): com.example.hospitalworkflow.data.model.ApiKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(key: com.example.hospitalworkflow.data.model.ApiKeyEntity)

    @Query("DELETE FROM api_keys WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface EmergencyIncidentDao {
    @Query("SELECT * FROM emergency_incidents ORDER BY detectedAt DESC")
    fun getAllIncidents(): Flow<List<com.example.hospitalworkflow.data.model.EmergencyIncident>>

    @Query("SELECT * FROM emergency_incidents WHERE id = :id")
    suspend fun getIncidentById(id: String): com.example.hospitalworkflow.data.model.EmergencyIncident?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(incident: com.example.hospitalworkflow.data.model.EmergencyIncident)

    @Query("DELETE FROM emergency_incidents WHERE id = :id")
    suspend fun deleteById(id: String)
}
