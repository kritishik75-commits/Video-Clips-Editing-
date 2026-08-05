package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val sourceUrl: String,
    val localPath: String,
    val mimeType: String,
    val sizeBytes: Long,
    val downloadedBytes: Long,
    val status: String, // QUEUED, DOWNLOADING, PAUSED, COMPLETED, FAILED
    val downloadSpeedMbps: Double = 0.0,
    val category: String, // VIDEO, AUDIO, IMAGE, DOCUMENT, ARCHIVE
    val folderId: Long = 0,
    val isFavorite: Boolean = false,
    val isVault: Boolean = false,
    val checksumMd5: String = "",
    val createdTimestamp: Long = System.currentTimeMillis(),
    val completedTimestamp: Long = 0
)

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val parentPath: String = "/",
    val itemCount: Int = 0,
    val isProtected: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actionType: String,
    val details: String,
    val userRole: String = "ADMIN",
    val ipAddress: String = "192.168.1.105",
    val timestamp: Long = System.currentTimeMillis()
)
