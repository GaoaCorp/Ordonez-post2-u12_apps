package com.gaaocorp.taskflow.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE syncStatus != 'PENDING_DELETE' ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    fun getTaskById(id: String): Flow<TaskEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET syncStatus = 'PENDING_DELETE' WHERE id = :id")
    suspend fun markForDeletion(id: String)

    @Query("SELECT * FROM tasks WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingTasks(): List<TaskEntity>

    @Query("UPDATE tasks SET syncStatus = 'SYNCED' WHERE id = :id")
    suspend fun markAsSynced(id: String)
}
