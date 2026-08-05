package com.example.data.model

enum class DownloadStatus {
    QUEUED, DOWNLOADING, PAUSED, COMPLETED, FAILED
}

enum class MediaCategory {
    ALL, VIDEO, AUDIO, IMAGE, DOCUMENT, ARCHIVE
}

enum class UserRole {
    ADMIN, PRO_USER
}

data class StorageBreakdown(
    val videoBytes: Long = 0,
    val audioBytes: Long = 0,
    val imageBytes: Long = 0,
    val docBytes: Long = 0,
    val archiveBytes: Long = 0,
    val totalUsedBytes: Long = 0,
    val totalCapacityBytes: Long = 53687091200L // 50 GB default quota
) {
    val freeBytes: Long get() = (totalCapacityBytes - totalUsedBytes).coerceAtLeast(0)
    val usagePercentage: Float get() = (totalUsedBytes.toFloat() / totalCapacityBytes.toFloat()).coerceIn(0f, 1f)
}

data class PresetMediaSource(
    val id: String,
    val title: String,
    val sourceName: String, // e.g., "Wikimedia Commons", "Archive.org", "NASA Open Media"
    val url: String,
    val category: MediaCategory,
    val mimeType: String,
    val estimatedSizeBytes: Long
)

data class ApiKeyItem(
    val id: String,
    val name: String,
    val keyPrefix: String,
    val createdDate: String,
    val rateLimitRps: Int = 100,
    val isActive: Boolean = true
)

data class SessionDevice(
    val id: String,
    val name: String,
    val platform: String,
    val ipAddress: String,
    val location: String,
    val lastActive: String,
    val isCurrentDevice: Boolean = false
)
