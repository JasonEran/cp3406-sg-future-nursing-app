package com.example.sgfuturenursingapp.ui.compose

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.waitUntil
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import com.example.sgfuturenursingapp.domain.usecase.AddTaskUseCase
import com.example.sgfuturenursingapp.domain.usecase.CompleteTaskUseCase
import com.example.sgfuturenursingapp.domain.usecase.DeleteTaskUseCase
import com.example.sgfuturenursingapp.domain.usecase.GetTaskByIdUseCase
import com.example.sgfuturenursingapp.domain.usecase.GetTasksUseCase
import com.example.sgfuturenursingapp.domain.usecase.UpdateTaskUseCase
import com.example.sgfuturenursingapp.ui.data.Task
import com.example.sgfuturenursingapp.ui.data.TaskRepository
import com.example.sgfuturenursingapp.ui.data.auth.AuthRepository
import com.example.sgfuturenursingapp.ui.screens.dashboard.DashboardViewModel
import com.example.sgfuturenursingapp.ui.screens.task.AddEditTaskViewModel
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CollaborationUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var taskRepository: FakeTaskRepository
    private lateinit var addTaskUseCase: AddTaskUseCase
    private lateinit var updateTaskUseCase: UpdateTaskUseCase
    private lateinit var deleteTaskUseCase: DeleteTaskUseCase
    private lateinit var getTasksUseCase: GetTasksUseCase
    private lateinit var getTaskByIdUseCase: GetTaskByIdUseCase
    private lateinit var completeTaskUseCase: CompleteTaskUseCase
    private lateinit var workManager: WorkManager
    private lateinit var adminAuthRepository: FakeAuthRepository
    private lateinit var helperAuthRepository: FakeAuthRepository
    private lateinit var adminAddEditViewModel: AddEditTaskViewModel
    private lateinit var adminDashboardViewModel: DashboardViewModel
    private lateinit var helperDashboardViewModel: DashboardViewModel

    @Before
    fun setUp() {
        taskRepository = FakeTaskRepository()
        addTaskUseCase = AddTaskUseCase(taskRepository)
        updateTaskUseCase = UpdateTaskUseCase(taskRepository)
        deleteTaskUseCase = DeleteTaskUseCase(taskRepository)
        getTasksUseCase = GetTasksUseCase(taskRepository)
        getTaskByIdUseCase = GetTaskByIdUseCase(taskRepository)
        completeTaskUseCase = CompleteTaskUseCase(taskRepository)
        workManager =
            mockk(relaxed = true) {
                every { enqueueUniqueWork(any(), any(), any()) } returns mockk(relaxed = true)
                every { cancelAllWorkByTag(any()) } returns mockk(relaxed = true)
            }
        adminAuthRepository = FakeAuthRepository(uid = "adminUid", email = "admin@test.com", role = "Admin")
        helperAuthRepository = FakeAuthRepository(uid = "helperUid", email = "helper@test.com", role = "Helper")

        adminAddEditViewModel =
            AddEditTaskViewModel(
                addTaskUseCase = addTaskUseCase,
                getTaskByIdUseCase = getTaskByIdUseCase,
                updateTaskUseCase = updateTaskUseCase,
                deleteTaskUseCase = deleteTaskUseCase,
                workManager = workManager,
                savedStateHandle = SavedStateHandle(),
            )

        adminDashboardViewModel =
            DashboardViewModel(
                getTasksUseCase = getTasksUseCase,
                completeTaskUseCase = completeTaskUseCase,
                deleteTaskUseCase = deleteTaskUseCase,
                updateTaskUseCase = updateTaskUseCase,
                authRepository = adminAuthRepository,
            )

        helperDashboardViewModel =
            DashboardViewModel(
                getTasksUseCase = getTasksUseCase,
                completeTaskUseCase = completeTaskUseCase,
                deleteTaskUseCase = deleteTaskUseCase,
                updateTaskUseCase = updateTaskUseCase,
                authRepository = helperAuthRepository,
            )
    }

    @Test
    fun helperReceivesTaskCreatedByAdmin() {
        composeRule.setContent {
            CollaborationTestScreen(
                adminViewModel = adminDashboardViewModel,
                helperViewModel = helperDashboardViewModel,
            )
        }

        composeRule.onNodeWithText("helper:Shared Medication").assertDoesNotExist()

        composeRule.runOnIdle {
            adminAddEditViewModel.onTitleChanged("Shared Medication")
            adminAddEditViewModel.onTimeChanged("08:30 AM")
            adminAddEditViewModel.onCategoryChanged("Medication")
            adminAddEditViewModel.onSaveClicked()
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("helper:Shared Medication").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("helper:Shared Medication").assertExists()
        composeRule.onNodeWithText("admin:Shared Medication").assertExists()
    }
}

@Composable
private fun CollaborationTestScreen(
    adminViewModel: DashboardViewModel,
    helperViewModel: DashboardViewModel,
) {
    val adminState by adminViewModel.uiState.collectAsState()
    val helperState by helperViewModel.uiState.collectAsState()

    Column {
        Text("Admin tasks")
        adminState.tasks.forEach { task ->
            Text("admin:${task.title}")
        }
        Text("Helper tasks")
        helperState.tasks.forEach { task ->
            Text("helper:${task.title}")
        }
    }
}

private class FakeTaskRepository : TaskRepository {
    private val tasks = mutableListOf<Task>()
    private val tasksFlow = MutableStateFlow<List<Task>>(emptyList())
    private var nextId = 1

    override fun getTasks(): Flow<List<Task>> = tasksFlow.asStateFlow()

    override suspend fun getTaskById(taskId: Int): Task? = tasks.firstOrNull { it.id == taskId }

    override suspend fun upsertTask(task: Task): Task {
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index >= 0) {
            tasks[index] = task
        } else {
            tasks.add(task)
        }
        tasks.sortBy { it.id }
        tasksFlow.value = tasks.toList()
        return task
    }

    override suspend fun completeTask(taskId: Int) {
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index >= 0) {
            val existing = tasks[index]
            tasks[index] = existing.copy(isCompleted = true)
            tasksFlow.value = tasks.toList()
        }
    }

    override suspend fun getNextTaskId(): Int = nextId++

    override suspend fun deleteTask(taskId: Int) {
        tasks.removeAll { it.id == taskId }
        tasksFlow.value = tasks.toList()
    }
}

private class FakeAuthRepository(
    uid: String,
    email: String,
    private val role: String,
) : AuthRepository {
    private val firebaseUser =
        mockk<FirebaseUser>(relaxed = true) {
            every { this@mockk.uid } returns uid
            every { this@mockk.email } returns email
        }
    private val userFlow = MutableStateFlow<FirebaseUser?>(firebaseUser)
    private val storedEmail = email

    override suspend fun register(email: String, password: String): Result<FirebaseUser?> =
        Result.failure(UnsupportedOperationException("Not supported in tests"))

    override suspend fun login(email: String, password: String): Result<FirebaseUser?> =
        Result.failure(UnsupportedOperationException("Not supported in tests"))

    override suspend fun logout(): Result<Unit> =
        Result.failure(UnsupportedOperationException("Not supported in tests"))

    override suspend fun getCurrentUser(): FirebaseUser? = userFlow.value

    override fun authStateFlow(): Flow<FirebaseUser?> = userFlow.asStateFlow()

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> =
        Result.failure(UnsupportedOperationException("Not supported in tests"))

    override fun getCurrentUserRole(): String? = role

    override fun getRoleForEmail(email: String): String =
        if (email == storedEmail) role else "Helper"

    override suspend fun ensureCurrentUserRecord() = Unit
}
