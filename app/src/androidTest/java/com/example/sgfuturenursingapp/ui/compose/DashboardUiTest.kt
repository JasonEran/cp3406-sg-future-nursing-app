package com.example.sgfuturenursingapp.ui.compose

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.waitUntil
import androidx.compose.ui.test.waitForIdle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sgfuturenursingapp.domain.usecase.AddTaskUseCase
import com.example.sgfuturenursingapp.domain.usecase.CompleteTaskUseCase
import com.example.sgfuturenursingapp.domain.usecase.DeleteTaskUseCase
import com.example.sgfuturenursingapp.domain.usecase.GetTaskByIdUseCase
import com.example.sgfuturenursingapp.domain.usecase.GetTasksUseCase
import com.example.sgfuturenursingapp.domain.usecase.UpdateTaskUseCase
import com.example.sgfuturenursingapp.ui.data.Task
import com.example.sgfuturenursingapp.ui.data.auth.AuthRepository
import com.example.sgfuturenursingapp.ui.screens.dashboard.DashboardScreen
import com.example.sgfuturenursingapp.ui.screens.dashboard.DashboardViewModel
import com.example.sgfuturenursingapp.ui.screens.task.AddEditTaskScreen
import com.example.sgfuturenursingapp.ui.screens.task.AddEditTaskViewModel
import com.example.sgfuturenursingapp.ui.theme.CP3406SGFutureNursingAppTheme
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.work.Operation
import androidx.work.WorkManager

@RunWith(AndroidJUnit4::class)
class DashboardUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @MockK(relaxed = true)
    lateinit var completeTaskUseCase: CompleteTaskUseCase

    @MockK(relaxed = true)
    lateinit var deleteTaskUseCase: DeleteTaskUseCase

    @MockK(relaxed = true)
    lateinit var updateTaskUseCase: UpdateTaskUseCase

    @MockK
    lateinit var getTasksUseCase: GetTasksUseCase

    @MockK
    lateinit var addTaskUseCase: AddTaskUseCase

    @MockK
    lateinit var getTaskByIdUseCase: GetTaskByIdUseCase

    @MockK(relaxed = true)
    lateinit var authRepository: AuthRepository

    @MockK
    lateinit var workManager: WorkManager

    private var nextTaskId = 100
    private lateinit var tasksFlow: MutableStateFlow<List<Task>>

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { authRepository.authStateFlow() } returns flowOf(null)
        every { authRepository.getCurrentUserRole() } returns "Admin"
        every { authRepository.getRoleForEmail(any()) } returns "Admin"
        coEvery { deleteTaskUseCase.invoke(any()) } returns Unit
        coEvery { completeTaskUseCase.invoke(any()) } returns Unit
        coEvery { updateTaskUseCase.invoke(any(), any(), any(), any(), any(), any(), any()) } returns mockTask()
        every { workManager.enqueueUniqueWork(any(), any(), any()) } returns mockk<Operation>(relaxed = true)
        every { workManager.cancelAllWorkByTag(any()) } returns mockk(relaxed = true)
        coEvery { getTaskByIdUseCase.invoke(any()) } returns null
    }

    @Test
    fun dashboardDisplaysTaskTitles() {
        val task =
            Task(
                id = 1,
                title = "Morning Medication",
                time = "08:00 AM",
                category = "Medication",
                iconName = "medical_services",
                isCompleted = false,
                priority = 0,
            )
        tasksFlow = MutableStateFlow(listOf(task))
        every { getTasksUseCase.invoke() } returns tasksFlow
        val dashboardViewModel = createDashboardViewModel()

        composeRule.setContent {
            CP3406SGFutureNursingAppTheme {
                DashboardScreen(
                    onTaskClick = {},
                    onProfileClick = {},
                    onAddTaskClick = {},
                    viewModel = dashboardViewModel,
                )
            }
        }

        composeRule.onNodeWithText(task.title).assertIsDisplayed()
    }

    @Test
    fun addTaskFlowAddsItemToDashboard() {
        tasksFlow = MutableStateFlow(emptyList())
        every { getTasksUseCase.invoke() } returns tasksFlow
        setupAddTaskMock()
        val dashboardViewModel = createDashboardViewModel()
        val addEditTaskViewModel = createAddEditTaskViewModel()

        composeRule.setContent {
            CP3406SGFutureNursingAppTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "dashboard") {
                    composable("dashboard") {
                        DashboardScreen(
                            onTaskClick = {},
                            onProfileClick = {},
                            onAddTaskClick = { navController.navigate("addTask") },
                            viewModel = dashboardViewModel,
                        )
                    }
                    composable("addTask") {
                        AddEditTaskScreen(
                            onNavigateUp = { navController.popBackStack() },
                            onActionFinished = { navController.popBackStack() },
                            viewModel = addEditTaskViewModel,
                        )
                    }
                }
            }
        }

        composeRule.onNodeWithContentDescription("Add Task").performClick()

        enterTextIntoField(label = "Title", text = "Daily Walk")
        enterTextIntoField(label = "Time", text = "09:00 AM")
        enterTextIntoField(label = "Category", text = "Activity")

        composeRule.onNodeWithText("Save").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            tasksFlow.value.any { it.title == "Daily Walk" }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Daily Walk").assertIsDisplayed()
    }

    private fun enterTextIntoField(label: String, text: String) {
        composeRule.onNode(
            hasText(label) and hasSetTextAction(),
            useUnmergedTree = true,
        ).performTextInput(text)
    }

    private fun createDashboardViewModel(): DashboardViewModel =
        DashboardViewModel(
            getTasksUseCase = getTasksUseCase,
            completeTaskUseCase = completeTaskUseCase,
            deleteTaskUseCase = deleteTaskUseCase,
            updateTaskUseCase = updateTaskUseCase,
            authRepository = authRepository,
        )

    private fun createAddEditTaskViewModel(): AddEditTaskViewModel =
        AddEditTaskViewModel(
            addTaskUseCase = addTaskUseCase,
            getTaskByIdUseCase = getTaskByIdUseCase,
            updateTaskUseCase = updateTaskUseCase,
            deleteTaskUseCase = deleteTaskUseCase,
            workManager = workManager,
            savedStateHandle = SavedStateHandle(),
        )

    private fun setupAddTaskMock() {
        coEvery {
            addTaskUseCase.invoke(
                title = any(),
                time = any(),
                category = any(),
                iconName = any(),
                isCompleted = any(),
                priority = any(),
            )
        } coAnswers {
            val title = arg<String>(0)
            val time = arg<String>(1)
            val category = arg<String>(2)
            val iconName = arg<String>(3)
            val isCompleted = arg<Boolean>(4)
            val priority = arg<Int>(5)
            val newTask =
                Task(
                    id = nextTaskId++,
                    title = title,
                    time = time,
                    category = category,
                    iconName = iconName,
                    isCompleted = isCompleted,
                    priority = priority,
                )
            tasksFlow.value = tasksFlow.value + newTask
            newTask
        }
    }

    private fun mockTask(): Task =
        Task(
            id = 1,
            title = "Sample",
            time = "00:00",
            category = "General",
            iconName = "icon",
            isCompleted = false,
            priority = 0,
        )
}
