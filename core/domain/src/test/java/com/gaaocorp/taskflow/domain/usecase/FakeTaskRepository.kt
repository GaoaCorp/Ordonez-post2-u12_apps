package com.gaaocorp.taskflow.domain.usecase

import com.gaaocorp.taskflow.domain.model.Task
import com.gaaocorp.taskflow.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Fake repository duplicado en :core:domain para no introducir dependencia circular. */
class FakeTaskRepository : TaskRepository {

    private val tasksFlow = MutableSharedFlow<List<Task>>(replay = 1)
    private val singleTaskFlow = MutableStateFlow<Task?>(null)

    val createdTasks = mutableListOf<Task>()
    val updatedTasks = mutableListOf<Task>()
    val deletedIds = mutableListOf<String>()

    suspend fun emit(tasks: List<Task>) = tasksFlow.emit(tasks)
    fun emitSingle(task: Task?) { singleTaskFlow.value = task }

    override fun getTasks(): Flow<List<Task>> = tasksFlow.asSharedFlow()
    override fun getTaskById(id: String): Flow<Task?> = singleTaskFlow
    override suspend fun createTask(task: Task) { createdTasks += task }
    override suspend fun updateTask(task: Task) { updatedTasks += task }
    override suspend fun deleteTask(id: String) { deletedIds += id }
    override suspend fun syncPendingTasks() = Unit
}
