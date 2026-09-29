# HospitalIQ Operational & AI Dispatcher — Mobile System Documentation

HospitalIQ is an offline-first, enterprise-grade Android hospital operations and surgical workflow application built with modern **Kotlin**, **Jetpack Compose**, **Room Database Event Sourcing**, and **Google AI Studio (Gemini) Emergency Intelligence**.

---

## 1. System Architecture & Working Mechanism

HospitalIQ is architected around the **MVVM (Model-View-ViewModel)** design pattern with single-source-of-truth local data persistence and reactive event streaming.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                             Jetpack Compose UI                              │
│ (AdminDashboard, Surgeries, OTs, CSSD, Patients, AI Emergency, Settings...) │
└────────────────────────────────────▲────────────────────────────────────────┘
                                     │ StateFlow / Compose State
┌────────────────────────────────────┴────────────────────────────────────────┐
│                             HospitalViewModel                               │
│      (State management, Coroutine scope, Settings & API Key handling)        │
└────────────────────────────────────▲────────────────────────────────────────┘
                                     │ Suspend Functions / Flows
┌────────────────────────────────────┴────────────────────────────────────────┐
│                             HospitalRepository                              │
│       (Event Sourcing Coordinator, State Transition Engine, Auditing)       │
└──────────────────▲───────────────────────────────────────▲──────────────────┘
                   │ Room DB Queries                       │ REST JSON Call
┌──────────────────┴───────────────────┐    ┌──────────────┴───────────────────┐
│     Room Local SQLite Database       │    │      GeminiApiService (OkHttp)   │
│  (HospitalDatabase / HospitalDao)    │    │ (Direct Google AI Studio API)    │
└──────────────────────────────────────┘    └──────────────────────────────────┘
```

### Data Flow & Event Sourcing
1. **Command Execution**: User actions (e.g., advancing a surgery state, sterilising a tray, or triggering an emergency incident) invoke high-level repository functions through `HospitalViewModel`.
2. **Atomic Room State Mutation**: State changes update entity tables (`operating_theatres`, `surgeries`, `patients`, `instrument_trays`, `emergency_incidents`, `alerts`) in the local Room SQLite database.
3. **Immutable Audit & Event Logging**: Every state transition generates an immutable `WorkflowEvent` record and an `AuditLog` entry in Room, guaranteeing auditability for hospital compliance.
4. **Reactive State Flow**: DAO queries return `Flow<List<T>>` streams, allowing the UI to reactively re-render in real time upon database changes.

### AI Emergency Redistribution Engine
- **Trigger**: When an emergency incident occurs (e.g., OT power outage, sterilizer failure, admission surge), the user requests AI analysis.
- **Payload Synthesis**: The repository compiles real-time JSON snapshots of active surgeries, OT availability, CSSD tray status, and open alerts.
- **REST Call**: `GeminiApiService` executes a direct HTTP POST request to Google's `generativeLanguage.googleapis.com` using the user's stored API key.
- **Fallback Rule Engine**: If no API key is set or network connectivity is unavailable, the system automatically executes an internal algorithmic fallback engine to produce a valid resource reallocation plan without crashing.
- **Database Transaction**: Clicking "Apply AI Resource Plan" executes atomic Room DB updates—reallocating surgeries, rerouting trays, and reprioritizing patients.

---

## 2. Navigation & User Flow

The application features role-based access control and top bar/bottom bar navigation across **11 core screens**:

### Screen & Route Breakdown

| Navigation Tab | Route / Screen | User Roles Allowed | Primary Function & Capabilities |
| :--- | :--- | :--- | :--- |
| **Command Center** | `AdminDashboardScreen` | Admin | Real-time operational KPI metric cards, active OT status summary, surgical funnel breakdown, and operational alerts. |
| **Operating Theatres**| `OperatingTheatresScreen` | Admin, OT Manager, OT Staff | Visual grid of all 6 surgical suites. Monitor environment parameters (HVAC, power, air pressure), toggle suite status, and manage turnover. |
| **Surgeries** | `SurgeriesScreen` | Admin, OT Manager, OT Staff, Ward Staff | Surgical master schedule. Advance surgical lifecycle stages (`SCHEDULED` -> `PATIENT_IN_OT` -> `SURGERY_ACTIVE` -> `COMPLETED`), check tray dependencies, and filter by status. |
| **Patients** | `PatientsScreen` | Admin, Ward Staff | Patient pre-op staging, priority classification (Emergency, Urgent, Elective), prep checklists, and ward room assignments. |
| **CSSD** | `CssdScreen` | Admin, CSSD Staff | Central Sterile Supply Department lifecycle pipeline (`DECONTAMINATION` -> `CLEANING` -> `STERILIZATION` -> `STERILE_STORAGE` -> `DISPATCHED`). Quality validation and QR code tray scanner simulation. |
| **AI Emergency** | `AiEmergencyScreen` | Admin, OT Manager | Trigger emergency incidents, execute Gemini AI resource reallocation analysis, review proposed tray/OT/surgery updates, and apply plans to DB. |
| **Settings** | `SettingsScreen` | All Roles | Manage local Google AI Studio API key, test key connectivity, and view privacy storage guarantees. |
| **API Gateway** | `ApiGatewayScreen` | Admin | Manage integration API keys for external EHR systems (Epic, Cerner) and mobile scanner clients. Revoke/generate access tokens. |
| **Alerts** | `AlertsScreen` | All Roles | System-wide operational alerts feed (Critical, High, Medium). Filter by status and acknowledge or resolve alerts. |
| **Analytics** | `AnalyticsScreen` | Admin, OT Manager | Performance metrics, average OT turnover times, tray sterilization success rates, and surgical delay distribution charts. |
| **Audit Logs** | `AuditLogsScreen` | Admin | Immutable timeline of every workflow state change, user action, role override, and timestamped event log. |

### UI Component Structure & Hierarchy

- **Top Bar**: Displays app title, current active Role selector (`ADMIN`, `OT_MANAGER`, `WARD_STAFF`, `CSSD_STAFF`, `OT_STAFF`), user app guide dialog button, Settings shortcut, and demo data reset action.
- **Role Selector Bar**: Dynamically filters accessible bottom navigation tabs based on selected role permissions.
- **Shared Components (`CommonComponents.kt`)**:
  - `MetricCard`: Standardized KPI display container with max-line clipping and colored accent badges.
  - `StatusBadge`: Unified color-coded pill indicators (`Emerald` for ready/valid, `Amber` for warning/pending, `Crimson` for critical/blocked).
  - `WorkflowTimelineStepper`: Interactive step indicator showing surgical or sterilization stage progression.
  - `TrayScannerDialog`: QR scanner modal dialog for scanning CSSD tray IDs.

---

## 3. API Key & Settings Configuration

HospitalIQ strictly enforces **Privacy-First Local Storage** for API keys.

```
┌────────────────────────────────────────────────────────┐
│                   SettingsScreen UI                    │
│ [ OutlinedTextField for Google AI Studio API Key ]     │
└───────────────────────────┬────────────────────────────┘
                            │
               Save Key / Clear Key / Test Key
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│             SettingsManager (SharedPrefs)              │
│ Stored locally in /data/data/com.example/shared_prefs  │
└───────────────────────────┬────────────────────────────┘
                            │
                     Key Injection
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│                   GeminiApiService                     │
│  Post request to generativeLanguage.googleapis.com     │
└────────────────────────────────────────────────────────┘
```

### Entering and Saving Your Key
1. Open the **Settings** screen via the bottom navigation bar or the top bar gear icon.
2. Paste your key into the **Google AI Studio Key** field. Keys typically start with `AIzaSy...`.
3. Tap **Save Key**. The key is immediately written to private Android `SharedPreferences` (`hospital_workflow_settings_prefs`).
4. Tap **Test Connection**. The app makes a lightweight JSON request to Google's endpoint to verify key validity.

### Key Retrieval Mechanism
- When running AI Emergency Analysis, `HospitalViewModel` retrieves the active key from `SettingsManager.getGoogleAiApiKey(context)`.
- If a custom key is present, `GeminiApiService` passes `?key=<USER_KEY>` to the REST API.
- If no custom key exists, the service checks `BuildConfig.GEMINI_API_KEY`.
- If neither key is configured, the system cleanly degrades to the built-in algorithmic rule engine.

---

## 4. File & Folder Structure

```
app/src/main/java/com/
├── MainActivity.kt                      # Main entry point, Scaffold, TopBar, and Navigation setup
├── ui/
│   └── theme/                           # Material 3 design theme, colors, typography
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── hospitalworkflow/
    ├── data/
    │   ├── ai/                          # AI integration layer
    │   │   └── GeminiApiService.kt      # OkHttp REST client for Google AI Studio Gemini API
    │   ├── local/                       # Room Local Database persistence
    │   │   ├── HospitalDao.kt           # Room Data Access Object (queries & updates)
    │   │   └── HospitalDatabase.kt      # Room Database configuration & migrations
    │   ├── model/                       # Data entities & domain models
    │   │   └── HospitalEntities.kt      # OperatingTheatre, Surgery, Patient, InstrumentTray, etc.
    │   ├── repository/                  # Business logic repository
    │   │   └── HospitalRepository.kt    # Event sourcing orchestrator & database mutator
    │   └── settings/                    # App configuration settings
    │       └── SettingsManager.kt       # Local SharedPreferences API key storage manager
    └── ui/
        ├── HospitalViewModel.kt         # Jetpack ViewModel managing app state & UI coroutines
        ├── components/
        │   └── CommonComponents.kt      # Reusable Compose UI elements (MetricCard, StatusBadge, etc.)
        └── screens/                     # Jetpack Compose Screen Views
            ├── AdminDashboardScreen.kt  # Command Center dashboard
            ├── AiEmergencyScreen.kt     # AI Emergency dispatch screen
            ├── AlertsScreen.kt          # Operational alerts feed
            ├── AnalyticsScreen.kt       # Hospital KPI charts & stats
            ├── ApiGatewayScreen.kt      # EHR & scanner API token management
            ├── AuditLogsScreen.kt       # Immutable audit event log viewer
            ├── CssdScreen.kt            # Sterilization department lifecycle screen
            ├── OperatingTheatresScreen.kt# OT suite status and environment monitors
            ├── PatientsScreen.kt        # Pre-op patient staging and prep
            ├── SettingsScreen.kt       # API key management & privacy settings
            └── SurgeriesScreen.kt       # Master surgical schedule & workflow stepper
```

---

## 5. Troubleshooting & FAQs

### Q1: The AI Emergency Analysis displays "Fallback Rule Engine Active". Why?
- **Cause**: No Google AI Studio API key is configured, or the provided key is invalid.
- **Resolution**: Navigate to **Settings**, paste a valid Gemini API key from Google AI Studio (`aistudio.google.com/app/apikey`), tap **Save Key**, and test the connection.

### Q2: "API Key Validation Failed (HTTP 400 / 403)" during Test Connection.
- **Cause**: The key was typed incorrectly, or the key does not have permission to access `gemini-1.5-flash` / `generativeLanguage` endpoints.
- **Resolution**:
  1. Ensure there are no leading or trailing whitespace characters.
  2. Verify in Google AI Studio that your key is active and not restricted by IP/quota limits.

### Q3: Network Timeout / Connection Failed in AI Emergency tab.
- **Cause**: Device network offline or hospital firewall blocking outbound connections to `generativeLanguage.googleapis.com`.
- **Resolution**: Verify device internet access. If offline, HospitalIQ automatically uses the local fallback rule engine to ensure uninterrupted surgical workflows.

### Q4: Why can't I access certain navigation tabs?
- **Cause**: Tab visibility is gated by your selected **User Role**.
- **Resolution**: Use the **Role Selector** dropdown in the top bar to switch roles (e.g., switch to `ADMIN` or `OT_MANAGER` to view AI Emergency, Analytics, or API Gateway).

### Q5: How do I reset demo data to factory state?
- **Resolution**: Tap the **Refresh Icon** in the top-right app header to clear and re-seed the local Room SQLite database.
