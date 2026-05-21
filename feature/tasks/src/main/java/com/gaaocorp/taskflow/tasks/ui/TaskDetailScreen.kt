package com.gaaocorp.taskflow.tasks.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gaaocorp.taskflow.domain.model.Priority
import com.gaaocorp.taskflow.tasks.viewmodel.TaskDetailViewModel
import com.gaaocorp.taskflow.ui.components.TaskFlowButton
import com.gaaocorp.taskflow.ui.components.TaskFlowCard
import com.gaaocorp.taskflow.ui.components.TaskFlowTopAppBar
import com.gaaocorp.taskflow.ui.theme.PriorityHigh
import com.gaaocorp.taskflow.ui.theme.PriorityLow
import com.gaaocorp.taskflow.ui.theme.PriorityMedium
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TaskDetailScreen(
    taskId: String,
    onNavigateBack: () -> Unit,
    viewModel: TaskDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(taskId) { viewModel.loadTask(taskId) }
    LaunchedEffect(uiState.isDeleted) { if (uiState.isDeleted) onNavigateBack() }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar tarea") },
            text = { Text("¿Estás seguro de que quieres eliminar esta tarea? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTask()
                    showDeleteDialog = false
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TaskFlowTopAppBar(
                title = "Detalle de Tarea",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { paddingValues ->
        val task = uiState.task
        if (task == null) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TaskFlowCard {
                    Text(task.title, style = MaterialTheme.typography.headlineSmall)
                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(task.description, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val (label, color) = when (task.priority) {
                            Priority.HIGH -> "Alta" to PriorityHigh
                            Priority.MEDIUM -> "Media" to PriorityMedium
                            Priority.LOW -> "Baja" to PriorityLow
                        }
                        SuggestionChip(onClick = {}, label = { Text("Prioridad: $label", color = color) })
                        if (task.isCompleted) {
                            SuggestionChip(onClick = {}, label = { Text("✓ Completada") })
                        }
                    }
                    Text(
                        "Creada: ${SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date(task.createdAt))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                TaskFlowButton(
                    text = if (task.isCompleted) "Marcar como pendiente" else "Marcar como completada",
                    onClick = { viewModel.toggleComplete() }
                )
            }
        }
    }
}
