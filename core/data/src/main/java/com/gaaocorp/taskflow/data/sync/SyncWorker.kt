package com.gaaocorp.taskflow.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gaaocorp.taskflow.domain.repository.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker que sincroniza las tareas pendientes con el backend.
 * Se ejecuta periódicamente cuando hay conexión, según política del ADR-003.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskRepository: TaskRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = try {
        taskRepository.syncPendingTasks()
        Result.success()
    } catch (e: Exception) {
        // Reintenta con backoff exponencial gestionado por WorkManager
        Result.retry()
    }
}
