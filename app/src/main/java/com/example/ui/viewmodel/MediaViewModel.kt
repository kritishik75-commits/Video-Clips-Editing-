package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.AuditLogEntity
import com.example.data.local.entities.FolderEntity
import com.example.data.local.entities.MediaEntity
import com.example.data.model.MediaCategory
import com.example.data.model.StorageBreakdown
import com.example.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOption {
    DATE_DESC, DATE_ASC, NAME_ASC, SIZE_DESC
}

class MediaViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val repository = MediaRepository(
        database.mediaDao(),
        database.folderDao(),
        database.auditLogDao()
    )

    init {
        viewModelScope.launch {
            repository.seedSampleDataIfEmpty()
        }
    }

    // Filters & UI States
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(MediaCategory.ALL)
    val selectedCategory: StateFlow<MediaCategory> = _selectedCategory.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.DATE_DESC)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _isGridView = MutableStateFlow(false)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    private val _selectedItemIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedItemIds: StateFlow<Set<Long>> = _selectedItemIds.asStateFlow()

    private val _duplicateItems = MutableStateFlow<List<MediaEntity>>(emptyList())
    val duplicateItems: StateFlow<List<MediaEntity>> = _duplicateItems.asStateFlow()

    // Database Flows
    val allMedia: StateFlow<List<MediaEntity>> = repository.allMedia.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val activeDownloads: StateFlow<List<MediaEntity>> = repository.activeDownloads.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val vaultMedia: StateFlow<List<MediaEntity>> = repository.vaultMedia.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val favoriteMedia: StateFlow<List<MediaEntity>> = repository.favoriteMedia.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val folders: StateFlow<List<FolderEntity>> = repository.allFolders.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val storageBreakdown: StateFlow<StorageBreakdown> = repository.storageBreakdown.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), StorageBreakdown()
    )

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.auditLogs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Filtered Public Media List
    val filteredMedia: StateFlow<List<MediaEntity>> = combine(
        repository.publicMedia,
        _searchQuery,
        _selectedCategory,
        _sortOption
    ) { items, query, category, sort ->
        var list = items

        if (category != MediaCategory.ALL) {
            list = list.filter { it.category.equals(category.name, ignoreCase = true) }
        }

        if (query.isNotBlank()) {
            list = list.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.mimeType.contains(query, ignoreCase = true)
            }
        }

        when (sort) {
            SortOption.DATE_DESC -> list.sortedByDescending { it.createdTimestamp }
            SortOption.DATE_ASC -> list.sortedBy { it.createdTimestamp }
            SortOption.NAME_ASC -> list.sortedBy { it.title.lowercase() }
            SortOption.SIZE_DESC -> list.sortedByDescending { it.sizeBytes }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: MediaCategory) {
        _selectedCategory.value = category
    }

    fun setSortOption(sort: SortOption) {
        _sortOption.value = sort
    }

    fun toggleViewMode() {
        _isGridView.value = !_isGridView.value
    }

    fun addDownload(
        url: String,
        category: MediaCategory,
        isVault: Boolean = false,
        title: String = "",
        sizeEstimateBytes: Long = 250000000L
    ) {
        viewModelScope.launch {
            val id = repository.addDownload(
                titleInput = title,
                sourceUrl = url,
                category = category,
                isVault = isVault,
                sizeEstimateBytes = sizeEstimateBytes
            )
            repository.startDownloadSimulation(id, viewModelScope)
        }
    }

    fun startDownload(id: Long) {
        repository.startDownloadSimulation(id, viewModelScope)
    }

    fun pauseDownload(id: Long) {
        viewModelScope.launch {
            repository.pauseDownload(id)
        }
    }

    fun cancelDownload(id: Long) {
        viewModelScope.launch {
            repository.cancelAndDeleteMedia(id)
        }
    }

    fun toggleFavorite(id: Long, current: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(id, current)
        }
    }

    fun toggleVault(id: Long, current: Boolean) {
        viewModelScope.launch {
            repository.toggleVault(id, current)
        }
    }

    fun renameMedia(id: Long, newName: String) {
        viewModelScope.launch {
            repository.renameMedia(id, newName)
        }
    }

    fun deleteMedia(id: Long) {
        viewModelScope.launch {
            repository.cancelAndDeleteMedia(id)
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            repository.createFolder(name)
        }
    }

    fun toggleItemSelection(id: Long) {
        val current = _selectedItemIds.value
        _selectedItemIds.value = if (current.contains(id)) current - id else current + id
    }

    fun clearSelection() {
        _selectedItemIds.value = emptySet()
    }

    fun deleteSelectedItems() {
        viewModelScope.launch {
            val ids = _selectedItemIds.value
            ids.forEach { repository.cancelAndDeleteMedia(it) }
            _selectedItemIds.value = emptySet()
        }
    }

    fun scanDuplicates() {
        viewModelScope.launch {
            _duplicateItems.value = repository.findDuplicates()
        }
    }

    fun unlockVault(pin: String, correctPin: String): Boolean {
        return if (pin == correctPin) {
            _isVaultUnlocked.value = true
            true
        } else {
            false
        }
    }

    fun lockVault() {
        _isVaultUnlocked.value = false
    }
}
