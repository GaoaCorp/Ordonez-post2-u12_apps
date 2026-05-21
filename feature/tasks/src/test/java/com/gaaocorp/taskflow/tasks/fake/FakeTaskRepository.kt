package com.gaaocorp.taskflow.tasks.fake

import com.gaaocorp.taskflow.domain.model.Task
import com.gaaocorp.taskflow.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Implementación in-memory de TaskRepository para tests unitarios.
 * Permite emitir valores arbitrarios o lanzar excepciones controladas.
 */
class FakeTaskRepository : TaskRepository {

    private val tasksFlow = MutableSharedFlow<List<Task>>(replay = 1)
    private val singleTaskFlow = MutableStateFlow<Task?>(null)

    private val _createdTasks = mutableListOf<Task>()
    val createdTasks: List<Task> get() = _createdTasks

    private val _updatedTasks = mutableListOf<Task>()
    val updatedTasks: List<Task> get() = _updatedTasks

    private val _deletedIds = mutableListOf<String>()
    val deletedIds: List<String> get() = _deletedIds

    suspend fun emit(tasks: List<Task>) {
        tasksFlow.emit(tasks)
    }

    suspend fun emitError(throwable: Throwable) {
        tasksFlow.emit(emptyList())
        throw throwable
    }

    fun emitSingle(task: Task?) {
        singleTaskFlow.value = task
    }

    override fun getTasks(): Flow<List<Task>> = tasksFlow.asSharedFlow()

    override fun getTaskById(id: String): Flow<Task?> = singleTaskFlow

    override suspend fun createTask(task: Task) {
        _createdTasks += task
    }

    override suspend fun updateTask(task: Task) {
        _updatedTasks += task
    }

    override suspend fun deleteTask(id: String) {
        _deletedIds += id
    }

    override suspend fun syncPendingTasks() = Unit
}
