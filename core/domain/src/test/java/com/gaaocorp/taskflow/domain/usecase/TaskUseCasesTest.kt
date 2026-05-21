package com.gaaocorp.taskflow.domain.usecase

import app.cash.turbine.test
import com.gaaocorp.taskflow.domain.model.Priority
import com.gaaocorp.taskflow.domain.model.Task
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskUseCasesTest {

    private lateinit var fakeRepository: FakeTaskRepository

    @Before
    fun setup() {
        fakeRepository = FakeTaskRepository()
    }

    private fun sampleTask(
        id: String = "abc-123",
        title: String = "Comprar café",
        completed: Boolean = false,
        priority: Priority = Priority.MEDIUM
    ) = Task(
        id = id,
        title = title,
        description = "Descripción de prueba",
        isCompleted = completed,
        priority = priority,
        createdAt = 1_700_000_000L,
        updatedAt = 1_700_000_000L
    )

    // ─── GetTasksUseCase ──────────────────────────────────────────────────────
    @Test
    fun `GetTasksUseCase emits items from repository`() = runTest {
        val useCase = GetTasksUseCase(fakeRepository)
        val tasks = listOf(sampleTask("1"), sampleTask("2"))

        useCase().test {
            fakeRepository.emit(tasks)
            assertEquals(tasks, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GetTasksUseCase emits empty list when repository has none`() = runTest {
        val useCase = GetTasksUseCase(fakeRepository)

        useCase().test {
            fakeRepository.emit(emptyList())
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── GetTaskByIdUseCase ───────────────────────────────────────────────────
    @Test
    fun `GetTaskByIdUseCase emits task when present`() = runTest {
        val useCase = GetTaskByIdUseCase(fakeRepository)
        val task = sampleTask(id = "xyz")
        fakeRepository.emitSingle(task)

        useCase("xyz").test {
            assertEquals(task, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GetTaskByIdUseCase emits null when task does not exist`() = runTest {
        val useCase = GetTaskByIdUseCase(fakeRepository)
        fakeRepository.emitSingle(null)

        useCase("nope").test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── CreateTaskUseCase ────────────────────────────────────────────────────
    @Test
    fun `CreateTaskUseCase delegates to repository`() = runTest {
        val useCase = CreateTaskUseCase(fakeRepository)
        val newTask = sampleTask(id = "new-1", title = "Nueva tarea")

        useCase(newTask)

        assertEquals(1, fakeRepository.createdTasks.size)
        assertEquals(newTask, fakeRepository.createdTasks.first())
    }

    @Test
    fun `CreateTaskUseCase preserves all task fields`() = runTest {
        val useCase = CreateTaskUseCase(fakeRepository)
        val task = sampleTask(
            id = "preserve-1",
            title = "Título exacto",
            priority = Priority.HIGH
        )

        useCase(task)

        val stored = fakeRepository.createdTasks.first()
        assertEquals("preserve-1", stored.id)
        assertEquals("Título exacto", stored.title)
        assertEquals(Priority.HIGH, stored.priority)
        assertEquals(false, stored.isCompleted)
    }

    // ─── UpdateTaskUseCase ────────────────────────────────────────────────────
    @Test
    fun `UpdateTaskUseCase delegates to repository`() = runTest {
        val useCase = UpdateTaskUseCase(fakeRepository)
        val task = sampleTask(id = "u-1").copy(isCompleted = true)

        useCase(task)

        assertEquals(1, fakeRepository.updatedTasks.size)
        assertEquals(true, fakeRepository.updatedTasks.first().isCompleted)
    }

    // ─── DeleteTaskUseCase ────────────────────────────────────────────────────
    @Test
    fun `DeleteTaskUseCase delegates id to repository`() = runTest {
        val useCase = DeleteTaskUseCase(fakeRepository)

        useCase("delete-me")

        assertEquals(listOf("delete-me"), fakeRepository.deletedIds)
    }

    // ─── Edge case: validación de modelo de dominio ───────────────────────────
    @Test
    fun `Task data class equality works correctly for caching`() {
        val task1 = sampleTask()
        val task2 = sampleTask()
        val task3 = sampleTask(id = "different")

        // Mismos campos → iguales (importante para Room/diffUtil)
        assertEquals(task1, task2)
        assertEquals(task1.hashCode(), task2.hashCode())

        // Diferentes campos → distintos
        assertTrue(task1 != task3)
    }

    @Test
    fun `Priority enum has exactly three levels`() {
        val all = Priority.values()
        assertEquals(3, all.size)
        assertTrue(all.contains(Priority.LOW))
        assertTrue(all.contains(Priority.MEDIUM))
        assertTrue(all.contains(Priority.HIGH))
    }
}
