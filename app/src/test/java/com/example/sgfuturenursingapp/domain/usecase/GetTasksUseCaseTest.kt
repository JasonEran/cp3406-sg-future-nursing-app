package com.example.sgfuturenursingapp.domain.usecase

import com.example.sgfuturenursingapp.ui.data.Task
import com.example.sgfuturenursingapp.ui.data.TaskRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class GetTasksUseCaseTest {

    private val taskRepository: TaskRepository = mockk()
    private val getTasksUseCase = GetTasksUseCase(taskRepository)

    @Test
    fun `invoke returns repository tasks flow`() = runTest {
        val expectedFlow = flowOf(emptyList<Task>())
        every { taskRepository.tasks } returns expectedFlow

        val result = getTasksUseCase()

        assertSame(expectedFlow, result)
        verify(exactly = 1) { taskRepository.tasks }
    }

    @Test
    fun `invoke emits tasks provided by repository`() = runTest {
        val taskList =
            listOf(
                Task(
                    id = 1,
                    title = "Morning medication",
                    time = "08:00 AM",
                    category = "Medication",
                    iconName = "medical_services",
                    isCompleted = false,
                    priority = 1,
                ),
            )
        every { taskRepository.tasks } returns flowOf(taskList)

        val result = getTasksUseCase()

        val emitted = result.first()
        assertEquals(taskList, emitted)
        verify(exactly = 1) { taskRepository.tasks }
    }
}
