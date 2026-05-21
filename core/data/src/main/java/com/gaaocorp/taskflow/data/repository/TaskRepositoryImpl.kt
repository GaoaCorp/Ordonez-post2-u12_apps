package com.gaaocorp.taskflow.data.repository

import com.gaaocorp.taskflow.data.local.SyncStatus
import com.gaaocorp.taskflow.data.local.TaskDao
import com.gaaocorp.taskflow.data.local.TaskEntity
import com.gaaocorp.taskflow.domain.model.Priority
import com.gaaocorp.taskflow.domain.model.Task
import com.gaaocorp.taskflow.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun getTasks(): Flow<List<Task>> =
        taskDao.getAllTasks().map { entities -> entities.map { it.toDomain() } }

    override fun getTaskById(id: String): Flow<Task?> =
        taskDao.getTaskById(id).map { it?.toDomain() }

    override suspend fun createTask(task: Task) {
        taskDao.insertTask(task.toEntity(SyncStatus.PENDING_UPLOAD))
    }

    override suspend fun updateTask(task: Task) {
        taskDao.updateTask(task.toEntity(SyncStatus.PENDING_UPLOAD))
    }

    override suspend fun deleteTask(id: String) {
        taskDao.markForDeletion(id)
    }

    override suspend fun syncPendingTasks() {
        // Implementado en SyncWorker
    }
}

private fun TaskEntity.toDomain() = Task(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
    priority = Priority.valueOf(priority),
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun Task.toEntity(syncStatus: SyncStatus) = TaskEntity(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
    priority = priority.name,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus
)
