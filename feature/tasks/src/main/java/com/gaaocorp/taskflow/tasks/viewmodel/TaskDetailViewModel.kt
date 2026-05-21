package com.gaaocorp.taskflow.tasks.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gaaocorp.taskflow.domain.model.Task
import com.gaaocorp.taskflow.domain.usecase.DeleteTaskUseCase
import com.gaaocorp.taskflow.domain.usecase.GetTaskByIdUseCase
import com.gaaocorp.taskflow.domain.usecase.UpdateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskDetailUiState(
    val task: Task? = null,
    val isDeleted: Boolean = false
)

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    fun loadTask(taskId: String) {
        viewModelScope.launch {
            getTaskByIdUseCase(taskId).collect { task ->
                _uiState.update { it.copy(task = task) }
            }
        }
    }

    fun toggleComplete() {
        val task = _uiState.value.task ?: return
        viewModelScope.launch {
            updateTaskUseCase(task.copy(isCompleted = !task.isCompleted, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteTask() {
        val task = _uiState.value.task ?: return
        viewModelScope.launch {
            deleteTaskUseCase(task.id)
            _uiState.update { it.copy(isDeleted = true) }
        }
    }
}
