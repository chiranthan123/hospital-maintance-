package com.example.hospitalworkflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hospitalworkflow.data.model.ApiKeyEntity
import com.example.ui.theme.Emerald500
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiGatewayScreen(
    apiKeys: List<ApiKeyEntity>,
    onCreateApiKey: (String, String) -> Unit,
    onRevokeApiKey: (String) -> Unit,
    onSimulateApiUpdate: (String, String, String, String, (String) -> Unit) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedEntityType by remember { mutableStateOf("TRAY") }
    var entityIdInput by remember { mutableStateOf("TR-5012") }
    var stateUpdateInput by remember { mutableStateOf("STERILE_STORAGE") }
    var apiKeyInput by remember { mutableStateOf(apiKeys.firstOrNull()?.apiKey ?: "hk_live_c92a71b308f1") }
    var executionResult by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Compact Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MedicalBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("API Gateway", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                    Text("External DB Keys & REST Endpoints", fontSize = 11.sp, color = Slate500)
                }
            }
            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue),
                shape = RoundedCornerShape(6.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("+ Create API Key", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // API Keys Management
            item {
                Text("Registered External API Keys & Clients", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
            }

            if (apiKeys.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Box(modifier = Modifier.padding(20.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No API keys generated yet.", color = Slate700)
                        }
                    }
                }
            } else {
                items(apiKeys) { keyEntity ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(keyEntity.clientName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(MedicalBlue.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(keyEntity.scope, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MedicalBlue)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Key: ${keyEntity.apiKey}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = Slate700,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            IconButton(onClick = { onRevokeApiKey(keyEntity.id) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Revoke Key", tint = Rose500)
                            }
                        }
                    }
                }
            }

            // Live Data REST Update Simulator
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Storage, contentDescription = null, tint = MedicalBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Live Database REST Payload Dispatcher", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                        }
                        Text("Simulate an external project or EHR server updating Hospital IQ database records via API key", fontSize = 12.sp, color = Slate700, modifier = Modifier.padding(top = 4.dp))

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("TRAY", "PATIENT", "OT").forEach { type ->
                                val isSelected = selectedEntityType == type
                                Button(
                                    onClick = {
                                        selectedEntityType = type
                                        entityIdInput = when (type) {
                                            "TRAY" -> "TR-5012"
                                            "PATIENT" -> "P-7710"
                                            else -> "OT-02"
                                        }
                                        stateUpdateInput = when (type) {
                                            "TRAY" -> "STERILE_STORAGE"
                                            "PATIENT" -> "READY"
                                            else -> "AVAILABLE"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) MedicalBlue else Slate200,
                                        contentColor = if (isSelected) Color.White else Slate800
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(type, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = { apiKeyInput = it },
                            label = { Text("API Key Header (x-api-key)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = entityIdInput,
                                onValueChange = { entityIdInput = it },
                                label = { Text("Target ID") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = stateUpdateInput,
                                onValueChange = { stateUpdateInput = it },
                                label = { Text("New Target State") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                onSimulateApiUpdate(selectedEntityType, entityIdInput, stateUpdateInput, apiKeyInput) { result ->
                                    executionResult = result
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue)
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Dispatch REST Update to Database", fontWeight = FontWeight.Bold)
                        }

                        executionResult?.let { res ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (res.startsWith("Error")) Rose500.copy(alpha = 0.15f) else Emerald500.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(12.dp)
                            ) {
                                Text(res, fontSize = 12.sp, color = Slate900, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // Integration Code Sample Export
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate900)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = Emerald500)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Integration Code Snippet for Other Projects", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = """
                                # cURL Request to update tray status remotely
                                curl -X POST https://hospital-iq.api/v1/cssd/trays \
                                  -H "x-api-key: ${apiKeyInput}" \
                                  -H "Content-Type: application/json" \
                                  -d '{
                                    "trayId": "${entityIdInput}",
                                    "state": "${stateUpdateInput}"
                                  }'
                            """.trimIndent(),
                            color = Emerald500,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var clientName by remember { mutableStateOf("") }
        var scope by remember { mutableStateOf("FULL_ACCESS") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Generate New API Key", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Client System Name (e.g. Cerner EHR, CSSD Scanner)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Access Scope Level:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("FULL_ACCESS", "CSSD_WRITE", "WARD_WRITE", "READ_ONLY").forEach { s ->
                            TextButton(
                                onClick = { scope = s },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = if (scope == s) MedicalBlue else Slate500
                                )
                            ) {
                                Text(s, fontSize = 10.sp, fontWeight = if (scope == s) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (clientName.isNotBlank()) {
                            onCreateApiKey(clientName, scope)
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue)
                ) {
                    Text("Generate Key")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
