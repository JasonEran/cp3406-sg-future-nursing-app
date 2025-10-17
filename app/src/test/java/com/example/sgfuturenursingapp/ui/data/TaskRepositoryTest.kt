package com.example.sgfuturenursingapp.ui.data

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskRepositoryTest {

    @Test
    fun completeTask_delegatesToDaoUpdate() = runTest {
        val taskDao = mockk<TaskDao>()
        every { taskDao.getTasks() } returns flowOf(emptyList())
        coEvery { taskDao.updateTaskCompletion(any(), any()) } returns Unit

        val repository = TaskRepository(taskDao)

        repository.completeTask(42)

        coVerify(exactly = 1) { taskDao.updateTaskCompletion(42, true) }
    }

    @Test
    fun tasks_returnsFlowProvidedByDao() = runTest {
        val expectedTasks = listOf(
            Task(
                id = 1,
                title = "Assess vital signs",
                time = "08:00",
                category = "Assessment",
                iconName = "ic_assessment",
                isCompleted = false
            ),
            Task(
                id = 2,
                title = "Medication review",
                time = "10:00",
                category = "Medication",
                iconName = "ic_medication",
                isCompleted = true
            )
        )
        val taskDao = mockk<TaskDao>()
        every { taskDao.getTasks() } returns flowOf(expectedTasks)
        coEvery { taskDao.updateTaskCompletion(any(), any()) } returns Unit

        val repository = TaskRepository(taskDao)

        val result = repository.tasks.first()

        assertEquals(expectedTasks, result)
    }
}
