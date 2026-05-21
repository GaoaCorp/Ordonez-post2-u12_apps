package com.gaaocorp.taskflow.tasks.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gaaocorp.taskflow.domain.model.Task
import com.gaaocorp.taskflow.domain.usecase.GetTasksUseCase
import com.gaaocorp.taskflow.domain.usecase.UpdateTaskUseCase
import com.gaaocorp.taskflow.tasks.state.TaskListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase
) : ViewModel() {

    val uiState: StateFlow<TaskListUiState> = getTasksUseCase()
        .map<List<Task>, TaskListUiState> { tasks ->
            if (tasks.isEmpty()) TaskListUiState.Empty
            else TaskListUiState.Success(tasks)
        }
        .catch { throwable ->
            emit(TaskListUiState.Error(throwable.message ?: "Error desconocido"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TaskListUiState.Loading
        )

    fun toggleTaskComplete(task: Task) {
        viewModelScope.launch {
            updateTaskUseCase(
                task.copy(
                    isCompleted = !task.isCompleted,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
}
