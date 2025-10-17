package com.example.sgfuturenursingapp.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sgfuturenursingapp.domain.usecase.CompleteTaskUseCase
import com.example.sgfuturenursingapp.domain.usecase.GetTasksUseCase
import com.example.sgfuturenursingapp.ui.data.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// Define the status of the dashboard UI
data class DashboardUiState(
    val tasks: List<Task> = emptyList(),
    val userName: String = "Mark",
    val snackbarMessage: String? = null,
)

@HiltViewModel
class DashboardViewModel
    @Inject
    constructor(
        private val getTasksUseCase: GetTasksUseCase,
        private val completeTaskUseCase: CompleteTaskUseCase,
    ) : ViewModel() {
        private val snackbarMessage = MutableStateFlow<String?>(null)

        // Directly observe and transform data streams from the Repository
        val uiState: StateFlow<DashboardUiState> =
            combine(
                getTasksUseCase(),
                snackbarMessage,
            ) { tasks, message ->
                DashboardUiState(tasks = tasks, snackbarMessage = message)
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
            snackbarMessage.value = message
        }

        fun clearSnackbarMessage() {
            snackbarMessage.value = null
        }
    }
