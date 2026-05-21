package com.gaaocorp.taskflow.tasks.viewmodel

import app.cash.turbine.test
import com.gaaocorp.taskflow.domain.model.Priority
import com.gaaocorp.taskflow.domain.model.Task
import com.gaaocorp.taskflow.domain.usecase.GetTasksUseCase
import com.gaaocorp.taskflow.domain.usecase.UpdateTaskUseCase
import com.gaaocorp.taskflow.tasks.MainCoroutineRule
import com.gaaocorp.taskflow.tasks.fake.FakeTaskRepository
import com.gaaocorp.taskflow.tasks.state.TaskListUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskListViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private lateinit var fakeRepository: FakeTaskRepository
    private lateinit var getTasksUseCase: GetTasksUseCase
    private lateinit var updateTaskUseCase: UpdateTaskUseCase
    private lateinit var viewModel: TaskListViewModel

    @Before
    fun setup() {
        fakeRepository = FakeTaskRepository()
        getTasksUseCase = GetTasksUseCase(fakeRepository)
        updateTaskUseCase = UpdateTaskUseCase(fakeRepository)
        viewModel = TaskListViewModel(getTasksUseCase, updateTaskUseCase)
    }

    private fun sampleTask(id: String = "1", title: String = "Test") = Task(
        id = id,
        title = title,
        description = "Description",
        isCompleted = false,
        priority = Priority.MEDIUM,
        createdAt = 1000L,
        updatedAt = 1000L
    )

    // ─── Test 1: Estado inicial Loading ───────────────────────────────────────
    @Test
    fun `uiState emits Loading initially before any data arrives`() = runTest {
        // Given: ViewModel recién creado, sin que el repositorio emita aún
        // Then: el estado inicial es Loading
        assertEquals(TaskListUiState.Loading, viewModel.uiState.value)
    }

    // ─── Test 2: Estado Success ───────────────────────────────────────────────
    @Test
    fun `uiState emits Success when repository returns items`() = runTest {
        val tasks = listOf(sampleTask("1", "Tarea 1"), sampleTask("2", "Tarea 2"))

        viewModel.uiState.test {
            assertEquals(TaskListUiState.Loading, awaitItem())
            fakeRepository.emit(tasks)
            advanceUntilIdle()

            val state = awaitItem()
            assertTrue("Esperaba Success pero fue $state", state is TaskListUiState.Success)
            assertEquals(tasks, (state as TaskListUiState.Success).tasks)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── Test 3: Estado Empty (edge case) ─────────────────────────────────────
    @Test
    fun `uiState emits Empty when repository returns empty list`() = runTest {
        viewModel.uiState.test {
            assertEquals(TaskListUiState.Loading, awaitItem())
            fakeRepository.emit(emptyList())
            advanceUntilIdle()

            assertEquals(TaskListUiState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── Test 4: Estado Error ─────────────────────────────────────────────────
    @Test
    fun `uiState emits Error when repository flow throws`() = runTest {
        val errorMessage = "Network failure"
        val failingRepo = object : com.gaaocorp.taskflow.domain.repository.TaskRepository {
            override fun getTasks() = kotlinx.coroutines.flow.flow<List<Task>> {
                throw RuntimeException(errorMessage)
            }
            override fun getTaskById(id: String) = kotlinx.coroutines.flow.flowOf<Task?>(null)
            override suspend fun createTask(task: Task) = Unit
            override suspend fun updateTask(task: Task) = Unit
            override suspend fun deleteTask(id: String) = Unit
            override suspend fun syncPendingTasks() = Unit
        }
        val errorViewModel = TaskListViewModel(
            GetTasksUseCase(failingRepo),
            UpdateTaskUseCase(failingRepo)
        )

        errorViewModel.uiState.test {
            assertEquals(TaskListUiState.Loading, awaitItem())
            advanceUntilIdle()

            val state = awaitItem()
            assertTrue("Esperaba Error pero fue $state", state is TaskListUiState.Error)
            assertEquals(errorMessage, (state as TaskListUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── Test 5: Acción del usuario (toggle complete) ─────────────────────────
    @Test
    fun `toggleTaskComplete invokes update with inverted completion flag`() = runTest {
        val task = sampleTask(id = "abc", title = "Original")

        viewModel.toggleTaskComplete(task)
        advanceUntilIdle()

        assertEquals(1, fakeRepository.updatedTasks.size)
        val updated = fakeRepository.updatedTasks.first()
        assertEquals("abc", updated.id)
        assertEquals(true, updated.isCompleted) // Original era false → ahora true
        assertTrue("updatedAt debió actualizarse", updated.updatedAt >= task.updatedAt)
    }

    // ─── Test 6: Toggle de tarea ya completada ────────────────────────────────
    @Test
    fun `toggleTaskComplete on completed task marks it as pending`() = runTest {
        val completedTask = sampleTask().copy(isCompleted = true)

        viewModel.toggleTaskComplete(completedTask)
        advanceUntilIdle()

        assertEquals(false, fakeRepository.updatedTasks.first().isCompleted)
    }
}
