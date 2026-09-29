package com.example.hospitalworkflow.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.hospitalworkflow.data.model.AlertItem
import com.example.hospitalworkflow.data.model.ApiKeyEntity
import com.example.hospitalworkflow.data.model.AuditLog
import com.example.hospitalworkflow.data.model.EmergencyIncident
import com.example.hospitalworkflow.data.model.InstrumentTray
import com.example.hospitalworkflow.data.model.OperatingTheatre
import com.example.hospitalworkflow.data.model.Patient
import com.example.hospitalworkflow.data.model.Surgery
import com.example.hospitalworkflow.data.model.WorkflowEvent

@Database(
    entities = [
        Patient::class,
        Surgery::class,
        OperatingTheatre::class,
        InstrumentTray::class,
        WorkflowEvent::class,
        AlertItem::class,
        AuditLog::class,
        ApiKeyEntity::class,
        EmergencyIncident::class
    ],
    version = 2,
    exportSchema = false
)
abstract class HospitalDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun surgeryDao(): SurgeryDao
    abstract fun operatingTheatreDao(): OperatingTheatreDao
    abstract fun instrumentTrayDao(): InstrumentTrayDao
    abstract fun workflowEventDao(): WorkflowEventDao
    abstract fun alertDao(): AlertDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun apiKeyDao(): ApiKeyDao
    abstract fun emergencyIncidentDao(): EmergencyIncidentDao

    companion object {
        @Volatile
        private var INSTANCE: HospitalDatabase? = null

        fun getDatabase(context: Context): HospitalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HospitalDatabase::class.java,
                    "hospital_workflow_db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
