package com.example.sgfuturenursingapp.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sgfuturenursingapp.domain.usecase.CompleteTaskUseCase
import com.example.sgfuturenursingapp.domain.usecase.DeleteTaskUseCase
import com.example.sgfuturenursingapp.domain.usecase.GetTasksUseCase
import com.example.sgfuturenursingapp.domain.usecase.UpdateTaskUseCase
import com.example.sgfuturenursingapp.ui.data.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// Define the status of the dashboard UI
data class DashboardUiState(
    val tasks: List<Task> = emptyList(),
    val userName: String = "Mark",
    val snackbar: DashboardSnackbar? = null,
    val isLoading: Boolean = true,
)

data class DashboardSnackbar(
    val message: String,
    val actionLabel: String? = null,
    val type: SnackbarType = SnackbarType.Info,
)

enum class SnackbarType {
    Info,
    UndoDelete,
}

@HiltViewModel
class DashboardViewModel
    @Inject
    constructor(
        private val getTasksUseCase: GetTasksUseCase,
        private val completeTaskUseCase: CompleteTaskUseCase,
        private val deleteTaskUseCase: DeleteTaskUseCase,
        private val updateTaskUseCase: UpdateTaskUseCase,
    ) : ViewModel() {
        private data class TaskFeedState(
            val tasks: List<Task>,
            val isLoading: Boolean,
        )

        private val snackbarMessage = MutableStateFlow<DashboardSnackbar?>(null)
        private val tasksState =
            getTasksUseCase()
                .map { tasks -> TaskFeedState(tasks = tasks, isLoading = false) }
                .onStart { emit(TaskFeedState(tasks = emptyList(), isLoading = true)) }
        private var recentlyDeletedTask: Task? = null

        // Directly observe and transform data streams from the Repository
        val uiState: StateFlow<DashboardUiState> =
            combine(
                tasksState,
                snackbarMessage,
            ) { taskFeedState, snackbar ->
                DashboardUiState(
                    tasks = taskFeedState.tasks,
                    snackbar = snackbar,
                    isLoading = taskFeedState.isLoading,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = DashboardUiState(),
            )

        fun completeTask(taskId: Int) {
            viewModelScope.launch {
                completeTaskUseCase(taskId)
            }
        }

        fun showSnackbarMessage(message: String) {
            snackbarMessage.value = DashboardSnackbar(message = message)
        }

        fun onTaskDismissed(task: Task) {
            viewModelScope.launch {
                recentlyDeletedTask = task
                deleteTaskUseCase(task.id)
                snackbarMessage.value =
                    DashboardSnackbar(
                        message = "任务已删除",
                        actionLabel = "撤销",
                        type = SnackbarType.UndoDelete,
                    )
            }
        }

        fun onSnackbarResult(
            snackbar: DashboardSnackbar,
            actionPerformed: Boolean,
        ) {
            snackbarMessage.value = null
            if (snackbar.type == SnackbarType.UndoDelete) {
                if (actionPerformed) {
                    undoDelete()
                } else {
                    recentlyDeletedTask = null
                }
            }
        }

        private fun undoDelete() {
            val taskToRestore = recentlyDeletedTask ?: return
            viewModelScope.launch {
                runCatching {
                    updateTaskUseCase(
                        taskId = taskToRestore.id,
                        title = taskToRestore.title,
                        time = taskToRestore.time,
                        category = taskToRestore.category,
                        iconName = taskToRestore.iconName,
                        isCompleted = taskToRestore.isCompleted,
                        priority = taskToRestore.priority,
                    )
                }.onSuccess {
                    recentlyDeletedTask = null
                    snackbarMessage.value = DashboardSnackbar(message = "任务已恢复")
                }.onFailure { throwable ->
                    recentlyDeletedTask = null
                    snackbarMessage.value =
                        DashboardSnackbar(
                            message = throwable.message ?: "无法恢复任务",
                        )
                }
            }
        }
    }
