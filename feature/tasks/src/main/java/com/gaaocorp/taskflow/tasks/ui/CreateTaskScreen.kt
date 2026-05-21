package com.gaaocorp.taskflow.tasks.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gaaocorp.taskflow.domain.model.Priority
import com.gaaocorp.taskflow.tasks.viewmodel.CreateTaskViewModel
import com.gaaocorp.taskflow.ui.components.TaskFlowButton
import com.gaaocorp.taskflow.ui.components.TaskFlowTextField
import com.gaaocorp.taskflow.ui.components.TaskFlowTopAppBar

@Composable
fun CreateTaskScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateTaskViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isTaskCreated) {
        if (uiState.isTaskCreated) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TaskFlowTopAppBar(
                title = "Nueva Tarea",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            TaskFlowTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                label = "Título *",
                isError = uiState.titleError != null,
                errorMessage = uiState.titleError
            )

            TaskFlowTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = "Descripción",
                singleLine = false,
                maxLines = 4
            )

            Text("Prioridad", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Priority.values().forEach { priority ->
                    FilterChip(
                        selected = uiState.priority == priority,
                        onClick = { viewModel.onPriorityChange(priority) },
                        label = {
                            Text(
                                when (priority) {
                                    Priority.LOW -> "Baja"
                                    Priority.MEDIUM -> "Media"
                                    Priority.HIGH -> "Alta"
                                }
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            TaskFlowButton(
                text = "Crear Tarea",
                onClick = viewModel::createTask,
                isLoading = uiState.isLoading,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}
