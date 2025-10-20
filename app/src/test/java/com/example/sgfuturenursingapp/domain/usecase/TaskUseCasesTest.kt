package com.example.sgfuturenursingapp.domain.usecase

import com.example.sgfuturenursingapp.ui.data.Task
import com.example.sgfuturenursingapp.ui.data.TaskRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class TaskUseCasesTest {
    private val taskRepository: TaskRepository = mockk(relaxed = true)

    private val addTaskUseCase = AddTaskUseCase(taskRepository)
    private val updateTaskUseCase = UpdateTaskUseCase(taskRepository)
    private val completeTaskUseCase = CompleteTaskUseCase(taskRepository)
    private val deleteTaskUseCase = DeleteTaskUseCase(taskRepository)

    @Test
    fun `add task delegates id generation and persistence to repository`() =
        runTest {
            coEvery { taskRepository.getNextTaskId() } returns 42
            coEvery { taskRepository.upsertTask(any()) } answers { firstArg() }

            val result =
                addTaskUseCase(
                    title = "Vitals assessment",
                    time = "08:00",
                    category = "Assessment",
                    iconName = "event",
                    isCompleted = false,
                    priority = 3,
                )

            assertEquals(42, result.id)
            coVerify {
                taskRepository.upsertTask(
                    Task(
                        id = 42,
                        title = "Vitals assessment",
                        time = "08:00",
                        category = "Assessment",
                        iconName = "event",
                        isCompleted = false,
                        priority = 3,
                    ),
                )
            }
        }

    @Test
    fun `update task delegates to repository`() =
        runTest {
            val updatedTask =
                Task(
                    id = 7,
                    title = "Medication review",
                    time = "10:00",
                    category = "Medication",
                    iconName = "medical_services",
                    isCompleted = true,
                    priority = 2,
                )
            coEvery { taskRepository.upsertTask(any()) } returns updatedTask

            val result =
                updateTaskUseCase(
                    taskId = 7,
                    title = "Medication review",
                    time = "10:00",
                    category = "Medication",
                    iconName = "medical_services",
                    isCompleted = true,
                    priority = 2,
                )

            assertEquals(updatedTask, result)
            coVerify { taskRepository.upsertTask(updatedTask) }
        }

    @Test
    fun `complete task delegates to repository`() =
        runTest {
            completeTaskUseCase(99)

            coVerify { taskRepository.completeTask(99) }
        }

    @Test
    fun `delete task delegates to repository`() =
        runTest {
            deleteTaskUseCase(73)

            coVerify { taskRepository.deleteTask(73) }
        }
}
