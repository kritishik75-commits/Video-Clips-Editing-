package com.example.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.UserRole
import com.example.ui.components.formatDate
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.MediaViewModel

@Composable
fun AdminAnalyticsScreen(
    mediaViewModel: MediaViewModel,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by authViewModel.userProfile.collectAsStateWithLifecycle()
    val apiKeys by authViewModel.apiKeys.collectAsStateWithLifecycle()
    val auditLogs by mediaViewModel.auditLogs.collectAsStateWithLifecycle()
    val duplicateItems by mediaViewModel.duplicateItems.collectAsStateWithLifecycle()

    var showAddApiKeyDialog by remember { mutableStateOf(false) }
    var newKeyName by remember { mutableStateOf("") }
    var newKeyRateLimit by remember { mutableStateOf("100") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Role & Status Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlueLight.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = PrimaryBlueLight
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Role-Based Access Control",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Current Role: ${userProfile.role.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = { authViewModel.toggleRole() },
                        modifier = Modifier.testTag("toggle_role_button")
                    ) {
                        Text(if (userProfile.role == UserRole.ADMIN) "Switch to USER" else "Switch to ADMIN")
                    }
                }
            }
        }

        // Space Cleaner & Duplicate Scanner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CleaningServices, contentDescription = null, tint = AccentCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Duplicate File Cleaner", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { mediaViewModel.scanDuplicates() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                        ) {
                            Text("Scan Duplicates")
                        }
                    }

                    if (duplicateItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Found ${duplicateItems.size} duplicate files that can be cleaned to free space.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentAmber
                        )
                    }
                }
            }
        }

        // API Key Management (ADMIN ONLY)
        if (userProfile.role == UserRole.ADMIN) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("API Keys Management", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Button(onClick = { showAddApiKeyDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New API Key")
                    }
                }
            }

            items(apiKeys, key = { it.id }) { keyItem ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = AccentEmerald)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(keyItem.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    "${keyItem.keyPrefix} • Rate limit: ${keyItem.rateLimitRps} RPS",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = { authViewModel.revokeApiKey(keyItem.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Revoke", tint = AccentRose)
                        }
                    }
                }
            }
        }

        // Audit Security Logs
        item {
            Text("Real-Time Audit & Security Logs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(auditLogs.take(15), key = { it.id }) { log ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(getLogColor(log.actionType))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(log.actionType, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text(formatDate(log.timestamp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(log.details, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }

    if (showAddApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showAddApiKeyDialog = false },
            title = { Text("Generate New API Key") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newKeyName,
                        onValueChange = { newKeyName = it },
                        label = { Text("Key Description Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newKeyRateLimit,
                        onValueChange = { newKeyRateLimit = it },
                        label = { Text("Rate Limit (RPS)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newKeyName.isNotBlank()) {
                            authViewModel.createApiKey(
                                name = newKeyName,
                                rateLimitRps = newKeyRateLimit.toIntOrNull() ?: 100
                            )
                            newKeyName = ""
                            showAddApiKeyDialog = false
                        }
                    }
                ) {
                    Text("Generate Key")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddApiKeyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun getLogColor(action: String): androidx.compose.ui.graphics.Color {
    return when (action) {
        "DOWNLOAD_COMPLETED" -> AccentEmerald
        "DOWNLOAD_QUEUED" -> PrimaryBlueLight
        "VAULT_ENCRYPTED" -> AccentAmber
        "FILE_DELETED" -> AccentRose
        else -> AccentCyan
    }
}
