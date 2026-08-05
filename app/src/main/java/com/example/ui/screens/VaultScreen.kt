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
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.FileItemCard
import com.example.ui.components.SimpleInputDialog
import com.example.ui.components.VaultPinDialog
import com.example.ui.components.formatFileSize
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.MediaViewModel

@Composable
fun VaultScreen(
    mediaViewModel: MediaViewModel,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val isVaultUnlocked by mediaViewModel.isVaultUnlocked.collectAsStateWithLifecycle()
    val vaultMedia by mediaViewModel.vaultMedia.collectAsStateWithLifecycle()
    val userProfile by authViewModel.userProfile.collectAsStateWithLifecycle()
    val selectedItemIds by mediaViewModel.selectedItemIds.collectAsStateWithLifecycle()

    var showPinDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }

    if (!isVaultUnlocked) {
        // Locked Vault Screen
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(AccentAmber.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Vault Locked",
                            tint = AccentAmber,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Encrypted Private Vault",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Protected by hardware-backed AES-256 encryption. Authentication required.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { showPinDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("unlock_vault_button")
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Authenticate with PIN")
                    }
                }
            }
        }
    } else {
        // Unlocked Vault View
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(AccentEmerald.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = AccentEmerald)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Vault Unlocked", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "${vaultMedia.size} protected files (${formatFileSize(vaultMedia.sumOf { it.sizeBytes })})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = { showChangePinDialog = true }) {
                            Icon(Icons.Default.Key, contentDescription = "Change PIN")
                        }
                        Button(
                            onClick = { mediaViewModel.lockVault() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Lock Vault")
                        }
                    }
                }
            }

            Text(
                text = "Protected Vault Items",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (vaultMedia.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No items inside Private Vault. You can move files into Vault from the File Manager screen.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(vaultMedia, key = { it.id }) { media ->
                        FileItemCard(
                            media = media,
                            isGridView = false,
                            isSelected = selectedItemIds.contains(media.id),
                            onSelectToggle = { mediaViewModel.toggleItemSelection(media.id) },
                            onFavoriteToggle = { mediaViewModel.toggleFavorite(media.id, media.isFavorite) },
                            onVaultToggle = { mediaViewModel.toggleVault(media.id, media.isVault) },
                            onRename = { },
                            onMetadata = { },
                            onDelete = { mediaViewModel.deleteMedia(media.id) }
                        )
                    }
                }
            }
        }
    }

    if (showPinDialog) {
        VaultPinDialog(
            title = "Enter Vault PIN",
            onConfirm = { pin ->
                val success = mediaViewModel.unlockVault(pin, userProfile.vaultPin)
                if (success) {
                    showPinDialog = false
                }
            },
            onDismiss = { showPinDialog = false }
        )
    }

    if (showChangePinDialog) {
        SimpleInputDialog(
            title = "Set New 4-Digit Vault PIN",
            initialValue = userProfile.vaultPin,
            label = "4-Digit PIN",
            onConfirm = { newPin ->
                if (newPin.length == 4 && newPin.all { it.isDigit() }) {
                    authViewModel.updateVaultPin(newPin)
                    showChangePinDialog = false
                }
            },
            onDismiss = { showChangePinDialog = false }
        )
    }
}
