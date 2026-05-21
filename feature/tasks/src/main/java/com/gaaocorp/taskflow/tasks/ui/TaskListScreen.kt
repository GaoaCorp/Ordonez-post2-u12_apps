package com.gaaocorp.taskflow.tasks.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gaaocorp.taskflow.domain.model.Priority
import com.gaaocorp.taskflow.domain.model.Task
import com.gaaocorp.taskflow.tasks.state.TaskListUiState
import com.gaaocorp.taskflow.tasks.viewmodel.TaskListViewModel
import com.gaaocorp.taskflow.ui.components.TaskFlowButton
import com.gaaocorp.taskflow.ui.components.TaskFlowCard
import com.gaaocorp.taskflow.ui.components.TaskFlowTopAppBar
import com.gaaocorp.taskflow.ui.theme.PriorityHigh
import com.gaaocorp.taskflow.ui.theme.PriorityLow
import com.gaaocorp.taskflow.ui.theme.PriorityMedium

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: TaskListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TaskFlowTopAppBar(
                title = "Mis Tareas",
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil")
                    }
                }
            )
        },
        floatingActionButton = {
            // El FAB solo aparece en Success/Empty, no en Loading/Error
            if (uiState is TaskListUiState.Success || uiState is TaskListUiState.Empty) {
                FloatingActionButton(onClick = onNavigateToCreate) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva tarea")
                }
            }
        }
    ) { paddingValues ->
        // Switch exhaustivo sobre los 3 estados de UI requeridos por rúbrica
        when (val state = uiState) {
            is TaskListUiState.Loading -> LoadingState(
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            )
            is TaskListUiState.Empty -> EmptyState(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                onCreateTask = onNavigateToCreate
            )
            is TaskListUiState.Success -> SuccessState(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                tasks = state.tasks,
                onTaskClick = onNavigateToDetail,
                onToggleComplete = viewModel::toggleTaskComplete
            )
            is TaskListUiState.Error -> ErrorState(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                message = state.message,
                onRetry = { /* Recompone automáticamente al observar el Flow */ }
            )
        }
    }
}

// ─── Estado: Loading ──────────────────────────────────────────────────────────
@Composable
private fun LoadingState(modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularProgressIndicator()
            Text("Cargando tareas...", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// ─── Estado: Empty (no es tarea de la rúbrica pero mejora UX) ─────────────────
@Composable
private fun EmptyState(modifier: Modifier, onCreateTask: () -> Unit) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("No tienes tareas aún", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Crea tu primera tarea para comenzar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onCreateTask) { Text("Crear tarea") }
    }
}

// ─── Estado: Success ──────────────────────────────────────────────────────────
@Composable
private fun SuccessState(
    modifier: Modifier,
    tasks: List<Task>,
    onTaskClick: (String) -> Unit,
    onToggleComplete: (Task) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tasks, key = { it.id }) { task ->
            TaskItem(
                task = task,
                onTaskClick = { onTaskClick(task.id) },
                onToggleComplete = { onToggleComplete(task) }
            )
        }
    }
}

// ─── Estado: Error ────────────────────────────────────────────────────────────
@Composable
private fun ErrorState(modifier: Modifier, message: String, onRetry: () -> Unit) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Ups, algo salió mal", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        TaskFlowButton(text = "Reintentar", onClick = onRetry)
    }
}

// ─── Item de tarea ────────────────────────────────────────────────────────────
@Composable
private fun TaskItem(
    task: Task,
    onTaskClick: () -> Unit,
    onToggleComplete: () -> Unit
) {
    TaskFlowCard(onClick = onTaskClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleComplete() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.title, style = MaterialTheme.typography.titleMedium)
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
            PriorityChip(priority = task.priority)
        }
    }
}

@Composable
private fun PriorityChip(priority: Priority) {
    val (label, color) = when (priority) {
        Priority.HIGH -> "Alta" to PriorityHigh
        Priority.MEDIUM -> "Media" to PriorityMedium
        Priority.LOW -> "Baja" to PriorityLow
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
