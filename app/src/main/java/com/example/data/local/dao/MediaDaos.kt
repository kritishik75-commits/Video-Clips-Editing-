package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.AuditLogEntity
import com.example.data.local.entities.FolderEntity
import com.example.data.local.entities.MediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_items ORDER BY createdTimestamp DESC")
    fun getAllMediaItems(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE isVault = 0 ORDER BY createdTimestamp DESC")
    fun getPublicMediaItems(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE isVault = 1 ORDER BY createdTimestamp DESC")
    fun getVaultMediaItems(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE status IN ('QUEUED', 'DOWNLOADING', 'PAUSED') ORDER BY createdTimestamp DESC")
    fun getActiveDownloads(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE isFavorite = 1 ORDER BY createdTimestamp DESC")
    fun getFavoriteMediaItems(): Flow<List<MediaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: MediaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMedia(mediaList: List<MediaEntity>)

    @Update
    suspend fun updateMedia(media: MediaEntity)

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteMediaById(id: Long)

    @Query("UPDATE media_items SET status = :status, downloadedBytes = :downloadedBytes, downloadSpeedMbps = :speed WHERE id = :id")
    suspend fun updateDownloadProgress(id: Long, status: String, downloadedBytes: Long, speed: Double)

    @Query("UPDATE media_items SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE media_items SET isVault = :isVault WHERE id = :id")
    suspend fun setVaultLocked(id: Long, isVault: Boolean)

    @Query("UPDATE media_items SET title = :newTitle WHERE id = :id")
    suspend fun renameMedia(id: Long, newTitle: String)

    @Query("SELECT COUNT(*) FROM media_items")
    suspend fun getMediaCount(): Int
}

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY name ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity): Long

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteFolderById(id: Long)

    @Query("SELECT COUNT(*) FROM folders")
    suspend fun getFolderCount(): Int
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)
}
