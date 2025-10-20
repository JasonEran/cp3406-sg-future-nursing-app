package com.example.sgfuturenursingapp.ui.screens.dashboard

import com.example.sgfuturenursingapp.domain.usecase.CompleteTaskUseCase
import com.example.sgfuturenursingapp.domain.usecase.DeleteTaskUseCase
import com.example.sgfuturenursingapp.domain.usecase.GetTasksUseCase
import com.example.sgfuturenursingapp.domain.usecase.UpdateTaskUseCase
import com.example.sgfuturenursingapp.testing.MainDispatcherRule
import com.example.sgfuturenursingapp.ui.data.Task
import com.example.sgfuturenursingapp.ui.data.auth.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getTasksUseCase: GetTasksUseCase = mockk()
    private val completeTaskUseCase: CompleteTaskUseCase = mockk()
    private val deleteTaskUseCase: DeleteTaskUseCase = mockk()
    private val updateTaskUseCase: UpdateTaskUseCase = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk()

    private val tasksFlow = MutableStateFlow<List<Task>>(emptyList())

    private fun buildViewModel(): DashboardViewModel {
        every { getTasksUseCase() } returns tasksFlow
        every { authRepository.authStateFlow() } returns flowOf(null)
        every { authRepository.getCurrentUserRole() } returns "Helper"
        coEvery { deleteTaskUseCase.invoke(any()) } returns Unit

        return DashboardViewModel(
            getTasksUseCase = getTasksUseCase,
            completeTaskUseCase = completeTaskUseCase,
            deleteTaskUseCase = deleteTaskUseCase,
            updateTaskUseCase = updateTaskUseCase,
            authRepository = authRepository,
        )
    }

    @Test
    fun `onTaskDismissed emits snackbar update`() = runTest {
        val task =
            Task(
                id = 1,
                title = "Morning medication",
                time = "08:00 AM",
                category = "Medication",
                iconName = "medical_services",
                isCompleted = false,
                priority = 0,
            )
        tasksFlow.value = listOf(task)

        val viewModel = buildViewModel()
        val job = launch { viewModel.uiState.collect() }

        try {
            viewModel.onTaskDismissed(task)
            advanceUntilIdle()

            val snackbar = requireNotNull(viewModel.uiState.value.snackbar)
            assertEquals("Task deleted", snackbar.message)
            assertEquals(SnackbarType.UndoDelete, snackbar.type)
            coVerify(exactly = 1) { deleteTaskUseCase.invoke(task.id) }
        } finally {
            job.cancel()
        }
    }

    @Test
    fun `completeTask delegates to use case`() = runTest {
        val viewModel = buildViewModel()
        coEvery { completeTaskUseCase.invoke(any()) } returns Unit

        viewModel.completeTask(42)
        advanceUntilIdle()

        coVerify(exactly = 1) { completeTaskUseCase.invoke(42) }
    }
}
