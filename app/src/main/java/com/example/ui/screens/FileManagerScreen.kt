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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.MediaEntity
import com.example.data.model.MediaCategory
import com.example.ui.components.FileItemCard
import com.example.ui.components.MetadataDialog
import com.example.ui.components.SimpleInputDialog
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentRose
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.viewmodel.MediaViewModel
import com.example.ui.viewmodel.SortOption

@Composable
fun FileManagerScreen(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    val filteredMedia by viewModel.filteredMedia.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val isGridView by viewModel.isGridView.collectAsStateWithLifecycle()
    val selectedItemIds by viewModel.selectedItemIds.collectAsStateWithLifecycle()
    val folders by viewModel.folders.collectAsStateWithLifecycle()

    var showFolderDialog by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var activeMetadataMedia by remember { mutableStateOf<MediaEntity?>(null) }
    var activeRenameMedia by remember { mutableStateOf<MediaEntity?>(null) }
    var showZipExportDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search & Controls Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search files, MIME types...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("file_search_input")
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = { viewModel.toggleViewMode() }) {
                Icon(
                    imageVector = if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                    contentDescription = "Toggle Grid/List View"
                )
            }

            Box {
                IconButton(onClick = { sortMenuExpanded = true }) {
                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort Options")
                }
                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Date (Newest First)") },
                        onClick = {
                            viewModel.setSortOption(SortOption.DATE_DESC)
                            sortMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Date (Oldest First)") },
                        onClick = {
                            viewModel.setSortOption(SortOption.DATE_ASC)
                            sortMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Name (A-Z)") },
                        onClick = {
                            viewModel.setSortOption(SortOption.NAME_ASC)
                            sortMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Size (Largest First)") },
                        onClick = {
                            viewModel.setSortOption(SortOption.SIZE_DESC)
                            sortMenuExpanded = false
                        }
                    )
                }
            }
        }

        // Category Pills
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(MediaCategory.entries.toTypedArray()) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { viewModel.setCategory(category) },
                    label = { Text(category.name) }
                )
            }
        }

        // Folders Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Folders (${folders.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { showFolderDialog = true }) {
                Icon(Icons.Default.CreateNewFolder, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Folder")
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(folders, key = { it.id }) { folder ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.width(140.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = null,
                            tint = if (folder.isProtected) AccentAmber else PrimaryBlueLight
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                folder.name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            Text(
                                "${folder.itemCount} items",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Batch Action Floating Banner
        if (selectedItemIds.isNotEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${selectedItemIds.size} files selected",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showZipExportDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueLight)
                        ) {
                            Icon(Icons.Default.Archive, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export ZIP")
                        }
                        Button(
                            onClick = { viewModel.deleteSelectedItems() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentRose)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                }
            }
        }

        // File List / Grid
        if (filteredMedia.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No media files found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredMedia, key = { it.id }) { media ->
                    FileItemCard(
                        media = media,
                        isGridView = true,
                        isSelected = selectedItemIds.contains(media.id),
                        onSelectToggle = { viewModel.toggleItemSelection(media.id) },
                        onFavoriteToggle = { viewModel.toggleFavorite(media.id, media.isFavorite) },
                        onVaultToggle = { viewModel.toggleVault(media.id, media.isVault) },
                        onRename = { activeRenameMedia = media },
                        onMetadata = { activeMetadataMedia = media },
                        onDelete = { viewModel.deleteMedia(media.id) }
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredMedia, key = { it.id }) { media ->
                    FileItemCard(
                        media = media,
                        isGridView = false,
                        isSelected = selectedItemIds.contains(media.id),
                        onSelectToggle = { viewModel.toggleItemSelection(media.id) },
                        onFavoriteToggle = { viewModel.toggleFavorite(media.id, media.isFavorite) },
                        onVaultToggle = { viewModel.toggleVault(media.id, media.isVault) },
                        onRename = { activeRenameMedia = media },
                        onMetadata = { activeMetadataMedia = media },
                        onDelete = { viewModel.deleteMedia(media.id) }
                    )
                }
            }
        }
    }

    // Dialogs
    if (showFolderDialog) {
        SimpleInputDialog(
            title = "Create New Folder",
            label = "Folder Name",
            onConfirm = { name ->
                viewModel.createFolder(name)
                showFolderDialog = false
            },
            onDismiss = { showFolderDialog = false }
        )
    }

    activeMetadataMedia?.let { media ->
        MetadataDialog(
            media = media,
            onDismiss = { activeMetadataMedia = null }
        )
    }

    activeRenameMedia?.let { media ->
        SimpleInputDialog(
            title = "Rename File",
            initialValue = media.title,
            label = "New Title",
            onConfirm = { newName ->
                viewModel.renameMedia(media.id, newName)
                activeRenameMedia = null
            },
            onDismiss = { activeRenameMedia = null }
        )
    }

    if (showZipExportDialog) {
        AlertDialog(
            onDismissRequest = { showZipExportDialog = false },
            icon = { Icon(Icons.Default.Archive, contentDescription = null, tint = PrimaryBlueLight) },
            title = { Text("Export ZIP Archive Bundle") },
            text = {
                Text("Packaging ${selectedItemIds.size} selected files into 'Export_Bundle_${System.currentTimeMillis().toString().takeLast(4)}.zip'.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearSelection()
                        showZipExportDialog = false
                    }
                ) {
                    Text("Generate ZIP Package")
                }
            },
            dismissButton = {
                TextButton(onClick = { showZipExportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
