package com.gaaocorp.taskflow.tasks.state

import com.gaaocorp.taskflow.domain.model.Task

/**
 * Estados explícitos de UI para la pantalla de lista de tareas.
 * Implementado como sealed interface para permitir exhaustive when() en el Composable.
 */
sealed interface TaskListUiState {
    /** Estado inicial mientras Room/red carga datos */
    data object Loading : TaskListUiState

    /** Sin tareas: rama feliz para usuario nuevo */
    data object Empty : TaskListUiState

    /** Datos disponibles */
    data class Success(val tasks: List<Task>) : TaskListUiState

    /** Error de red, base de datos o sincronización */
    data class Error(val message: String) : TaskListUiState
}
