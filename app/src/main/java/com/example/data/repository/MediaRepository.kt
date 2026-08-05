package com.example.data.repository

import com.example.data.local.dao.AuditLogDao
import com.example.data.local.dao.FolderDao
import com.example.data.local.dao.MediaDao
import com.example.data.local.entities.AuditLogEntity
import com.example.data.local.entities.FolderEntity
import com.example.data.local.entities.MediaEntity
import com.example.data.model.MediaCategory
import com.example.data.model.PresetMediaSource
import com.example.data.model.StorageBreakdown
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class MediaRepository(
    private val mediaDao: MediaDao,
    private val folderDao: FolderDao,
    private val auditLogDao: AuditLogDao
) {
    val allMedia: Flow<List<MediaEntity>> = mediaDao.getAllMediaItems()
    val publicMedia: Flow<List<MediaEntity>> = mediaDao.getPublicMediaItems()
    val vaultMedia: Flow<List<MediaEntity>> = mediaDao.getVaultMediaItems()
    val activeDownloads: Flow<List<MediaEntity>> = mediaDao.getActiveDownloads()
    val favoriteMedia: Flow<List<MediaEntity>> = mediaDao.getFavoriteMediaItems()
    val allFolders: Flow<List<FolderEntity>> = folderDao.getAllFolders()
    val auditLogs: Flow<List<AuditLogEntity>> = auditLogDao.getAllAuditLogs()

    private val activeDownloadJobs = mutableMapOf<Long, Job>()

    val storageBreakdown: Flow<StorageBreakdown> = mediaDao.getAllMediaItems().map { list ->
        var video = 0L
        var audio = 0L
        var image = 0L
        var doc = 0L
        var archive = 0L

        list.forEach { item ->
            val size = if (item.status == "COMPLETED") item.sizeBytes else item.downloadedBytes
            when (item.category) {
                "VIDEO" -> video += size
                "AUDIO" -> audio += size
                "IMAGE" -> image += size
                "DOCUMENT" -> doc += size
                "ARCHIVE" -> archive += size
            }
        }
        val total = video + audio + image + doc + archive
        StorageBreakdown(
            videoBytes = video,
            audioBytes = audio,
            imageBytes = image,
            docBytes = doc,
            archiveBytes = archive,
            totalUsedBytes = total
        )
    }

    suspend fun seedSampleDataIfEmpty() {
        if (mediaDao.getMediaCount() == 0) {
            val sampleItems = listOf(
                MediaEntity(
                    title = "Cybernetic_Nebula_4K_HDR.mp4",
                    sourceUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                    localPath = "/storage/emulated/0/Download/MediaManager/Cybernetic_Nebula_4K_HDR.mp4",
                    mimeType = "video/mp4",
                    sizeBytes = 1420000000L, // 1.42 GB
                    downloadedBytes = 1420000000L,
                    status = "COMPLETED",
                    category = "VIDEO",
                    isFavorite = true,
                    checksumMd5 = "e99a18c428cb38d5f260853678922e03",
                    createdTimestamp = System.currentTimeMillis() - 86400000L * 3
                ),
                MediaEntity(
                    title = "Quantum_Computing_Architecture.pdf",
                    sourceUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                    localPath = "/storage/emulated/0/Download/MediaManager/Quantum_Computing_Architecture.pdf",
                    mimeType = "application/pdf",
                    sizeBytes = 45800000L, // 45.8 MB
                    downloadedBytes = 45800000L,
                    status = "COMPLETED",
                    category = "DOCUMENT",
                    checksumMd5 = "7b230491a6291a18c4210928374182f2",
                    createdTimestamp = System.currentTimeMillis() - 86400000L * 2
                ),
                MediaEntity(
                    title = "Synthwave_Master_Session.flac",
                    sourceUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                    localPath = "/storage/emulated/0/Download/MediaManager/Synthwave_Master_Session.flac",
                    mimeType = "audio/flac",
                    sizeBytes = 184000000L, // 184 MB
                    downloadedBytes = 184000000L,
                    status = "COMPLETED",
                    category = "AUDIO",
                    isFavorite = true,
                    checksumMd5 = "3a9210c8293b48f9102847d92039481b",
                    createdTimestamp = System.currentTimeMillis() - 86400000L
                ),
                MediaEntity(
                    title = "Enterprise_Backup_2026.zip",
                    sourceUrl = "https://github.com/aistudio/archive.zip",
                    localPath = "/storage/emulated/0/Download/MediaManager/Enterprise_Backup_2026.zip",
                    mimeType = "application/zip",
                    sizeBytes = 3450000000L, // 3.45 GB
                    downloadedBytes = 2340000000L,
                    status = "DOWNLOADING",
                    downloadSpeedMbps = 42.5,
                    category = "ARCHIVE",
                    checksumMd5 = "99f81a72bca019842f1029845729103c",
                    createdTimestamp = System.currentTimeMillis() - 3600000L
                ),
                MediaEntity(
                    title = "Deep_Space_Observation_Hubble.raw",
                    sourceUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa",
                    localPath = "/storage/emulated/0/Download/MediaManager/Deep_Space_Observation_Hubble.raw",
                    mimeType = "image/png",
                    sizeBytes = 98000000L, // 98 MB
                    downloadedBytes = 98000000L,
                    status = "COMPLETED",
                    category = "IMAGE",
                    checksumMd5 = "102947d8a92834b71203947b19283f01",
                    createdTimestamp = System.currentTimeMillis() - 1800000L
                ),
                MediaEntity(
                    title = "High_Speed_HLS_Video_Stream.m3u8",
                    sourceUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                    localPath = "/storage/emulated/0/Download/MediaManager/High_Speed_HLS_Video_Stream.m3u8",
                    mimeType = "video/mp4",
                    sizeBytes = 850000000L, // 850 MB
                    downloadedBytes = 320000000L,
                    status = "PAUSED",
                    downloadSpeedMbps = 0.0,
                    category = "VIDEO",
                    checksumMd5 = "902847d1a29384b721092837f102839b",
                    createdTimestamp = System.currentTimeMillis() - 900000L
                ),
                MediaEntity(
                    title = "Confidential_Financial_Ledger.enc",
                    sourceUrl = "https://secure.internal/vault/ledger.enc",
                    localPath = "/storage/emulated/0/Download/MediaManager/Vault/Confidential_Financial_Ledger.enc",
                    mimeType = "application/octet-stream",
                    sizeBytes = 24000000L,
                    downloadedBytes = 24000000L,
                    status = "COMPLETED",
                    category = "DOCUMENT",
                    isVault = true,
                    checksumMd5 = "77102984b729103847c10293847b2910",
                    createdTimestamp = System.currentTimeMillis() - 400000L
                )
            )
            mediaDao.insertAllMedia(sampleItems)
        }

        if (folderDao.getFolderCount() == 0) {
            folderDao.insertFolder(FolderEntity(name = "4K Cinema", parentPath = "/", itemCount = 2))
            folderDao.insertFolder(FolderEntity(name = "Podcast & Audiobooks", parentPath = "/", itemCount = 4))
            folderDao.insertFolder(FolderEntity(name = "Technical Documents", parentPath = "/", itemCount = 6))
            folderDao.insertFolder(FolderEntity(name = "System Vault", parentPath = "/Vault", itemCount = 1, isProtected = true))
        }

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actionType = "SYSTEM_INITIALIZED",
                details = "Media Manager engine started with Room SQLite persistence.",
                userRole = "ADMIN"
            )
        )
    }

    suspend fun addDownload(
        titleInput: String,
        sourceUrl: String,
        category: MediaCategory,
        isVault: Boolean = false,
        sizeEstimateBytes: Long = 250000000L
    ): Long {
        val title = if (titleInput.isNotBlank()) titleInput else getTitleFromUrl(sourceUrl, category)
        val mimeType = when (category) {
            MediaCategory.VIDEO -> "video/mp4"
            MediaCategory.AUDIO -> "audio/mp3"
            MediaCategory.IMAGE -> "image/jpeg"
            MediaCategory.DOCUMENT -> "application/pdf"
            MediaCategory.ARCHIVE -> "application/zip"
            MediaCategory.ALL -> "application/octet-stream"
        }

        val entity = MediaEntity(
            title = title,
            sourceUrl = sourceUrl,
            localPath = "/storage/emulated/0/Download/MediaManager/${if (isVault) "Vault/" else ""}$title",
            mimeType = mimeType,
            sizeBytes = sizeEstimateBytes,
            downloadedBytes = 0L,
            status = "QUEUED",
            category = category.name,
            isVault = isVault,
            checksumMd5 = UUID.randomUUID().toString().replace("-", "").take(32)
        )

        val id = mediaDao.insertMedia(entity)
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actionType = "DOWNLOAD_QUEUED",
                details = "Queued download: '$title' (${sizeEstimateBytes / 1024 / 1024} MB)",
                userRole = "USER"
            )
        )
        return id
    }

    fun startDownloadSimulation(id: Long, scope: CoroutineScope) {
        if (activeDownloadJobs[id]?.isActive == true) return

        val job = scope.launch(Dispatchers.IO) {
            val item = mediaDao.getAllMediaItems().first().find { it.id == id } ?: return@launch
            var currentDownloaded = item.downloadedBytes
            val totalSize = item.sizeBytes

            mediaDao.updateDownloadProgress(id, "DOWNLOADING", currentDownloaded, 15.0)

            while (currentDownloaded < totalSize) {
                delay(800)
                val chunk = (Random.nextLong(15000000L, 45000000L)).coerceAtMost(totalSize - currentDownloaded)
                currentDownloaded += chunk
                val speed = Random.nextDouble(25.0, 68.0)

                val status = if (currentDownloaded >= totalSize) "COMPLETED" else "DOWNLOADING"
                mediaDao.updateDownloadProgress(id, status, currentDownloaded, if (status == "COMPLETED") 0.0 else speed)

                if (status == "COMPLETED") {
                    auditLogDao.insertAuditLog(
                        AuditLogEntity(
                            actionType = "DOWNLOAD_COMPLETED",
                            details = "Finished download: '${item.title}'",
                            userRole = "USER"
                        )
                    )
                    break
                }
            }
        }
        activeDownloadJobs[id] = job
    }

    suspend fun pauseDownload(id: Long) {
        activeDownloadJobs[id]?.cancel()
        activeDownloadJobs.remove(id)
        val item = mediaDao.getAllMediaItems().first().find { it.id == id }
        if (item != null) {
            mediaDao.updateDownloadProgress(id, "PAUSED", item.downloadedBytes, 0.0)
            auditLogDao.insertAuditLog(
                AuditLogEntity(
                    actionType = "DOWNLOAD_PAUSED",
                    details = "Paused download: '${item.title}'",
                    userRole = "USER"
                )
            )
        }
    }

    suspend fun cancelAndDeleteMedia(id: Long) {
        activeDownloadJobs[id]?.cancel()
        activeDownloadJobs.remove(id)
        val item = mediaDao.getAllMediaItems().first().find { it.id == id }
        mediaDao.deleteMediaById(id)
        if (item != null) {
            auditLogDao.insertAuditLog(
                AuditLogEntity(
                    actionType = "FILE_DELETED",
                    details = "Deleted file: '${item.title}'",
                    userRole = "USER"
                )
            )
        }
    }

    suspend fun toggleFavorite(id: Long, current: Boolean) {
        mediaDao.setFavorite(id, !current)
    }

    suspend fun toggleVault(id: Long, current: Boolean) {
        mediaDao.setVaultLocked(id, !current)
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actionType = if (!current) "VAULT_ENCRYPTED" else "VAULT_DECRYPTED",
                details = "${if (!current) "Moved to Private Vault" else "Moved to Public Storage"} (ID: $id)",
                userRole = "USER"
            )
        )
    }

    suspend fun renameMedia(id: Long, newName: String) {
        mediaDao.renameMedia(id, newName)
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actionType = "FILE_RENAMED",
                details = "Renamed file ID $id to '$newName'",
                userRole = "USER"
            )
        )
    }

    suspend fun createFolder(name: String) {
        folderDao.insertFolder(FolderEntity(name = name))
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actionType = "FOLDER_CREATED",
                details = "Created folder '$name'",
                userRole = "USER"
            )
        )
    }

    suspend fun findDuplicates(): List<MediaEntity> {
        val list = mediaDao.getAllMediaItems().first()
        return list.groupBy { it.sizeBytes }
            .filter { it.value.size > 1 }
            .flatMap { it.value }
    }

    private fun getTitleFromUrl(url: String, category: MediaCategory): String {
        val clean = url.substringAfterLast("/").substringBefore("?")
        if (clean.length > 5 && clean.contains(".")) return clean

        val timestamp = System.currentTimeMillis().toString().takeLast(4)
        return when (category) {
            MediaCategory.VIDEO -> "Media_Stream_$timestamp.mp4"
            MediaCategory.AUDIO -> "Audio_Track_$timestamp.mp3"
            MediaCategory.IMAGE -> "HighRes_Capture_$timestamp.jpg"
            MediaCategory.DOCUMENT -> "Document_Archive_$timestamp.pdf"
            MediaCategory.ARCHIVE -> "Package_Bundle_$timestamp.zip"
            MediaCategory.ALL -> "Download_$timestamp.bin"
        }
    }

    companion object {
        val PRESET_SOURCES = listOf(
            PresetMediaSource(
                id = "p1",
                title = "Tears of Steel (4K Open Movie)",
                sourceName = "Blender Foundation",
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                category = MediaCategory.VIDEO,
                mimeType = "video/mp4",
                estimatedSizeBytes = 720000000L
            ),
            PresetMediaSource(
                id = "p2",
                title = "NASA Mars Rover Imagery Pack",
                sourceName = "NASA Open API",
                url = "https://images.nasa.gov/details-PIA23764",
                category = MediaCategory.IMAGE,
                mimeType = "image/png",
                estimatedSizeBytes = 185000000L
            ),
            PresetMediaSource(
                id = "p3",
                title = "Ambient Soundscapes & Lo-Fi Session",
                sourceName = "Archive.org Audio",
                url = "https://archive.org/details/ambient_audio_loop.mp3",
                category = MediaCategory.AUDIO,
                mimeType = "audio/mp3",
                estimatedSizeBytes = 94000000L
            ),
            PresetMediaSource(
                id = "p4",
                title = "Distributed File Systems & CDN Whitepaper",
                sourceName = "ACM Digital Library",
                url = "https://arxiv.org/pdf/2104.00001.pdf",
                category = MediaCategory.DOCUMENT,
                mimeType = "application/pdf",
                estimatedSizeBytes = 38000000L
            ),
            PresetMediaSource(
                id = "p5",
                title = "Linux Kernel v6.12 Source Tarball",
                sourceName = "Kernel.org Mirror",
                url = "https://cdn.kernel.org/pub/linux/kernel/v6.x/linux-6.12.tar.xz",
                category = MediaCategory.ARCHIVE,
                mimeType = "application/zip",
                estimatedSizeBytes = 1450000000L
            )
        )
    }
}
