package com.gaaocorp.taskflow.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SyncStatus { SYNCED, PENDING_UPLOAD, PENDING_DELETE }

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val priority: String,
    val createdAt: Long,
    val updatedAt: Long,
    val syncStatus: SyncStatus = SyncStatus.PENDING_UPLOAD
)
