package com.example.sg_future_nursing_app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sg_future_nursing_app.ui.data.Task
import com.example.sg_future_nursing_app.ui.data.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// Define the status of the dashboard UI
data class DashboardUiState(
    val tasks: List<Task> = emptyList(),
    val userName: String = "Mark"
)

class DashboardViewModel : ViewModel() {

    // Directly observe and transform data streams from the Repository
    val uiState: StateFlow<DashboardUiState> =
        TaskRepository.tasks.map { tasks ->
            DashboardUiState(tasks = tasks)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState()
        )

    fun completeTask(taskId: Int) {
        // Delegate the operation to the Repository
        TaskRepository.completeTask(taskId)
    }
}