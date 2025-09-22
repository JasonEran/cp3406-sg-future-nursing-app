package com.example.sg_future_nursing_app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import com.example.sg_future_nursing_app.ui.data.DummyDataProvider
import com.example.sg_future_nursing_app.ui.data.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Define the status of the dashboard UI
data class DashboardUiState(
    val tasks: List<Task> = emptyList(),
    val userName: String = "Mark"
)

class DashboardViewModel : ViewModel() {
    // Private mutable state
    private val _uiState = MutableStateFlow(DashboardUiState())
    // Expose read-only status to UI
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        // Simulate loading data from a data source
        _uiState.value = _uiState.value.copy(
            tasks = DummyDataProvider.tasks
        )
    }

    fun completeTask(taskId: Int) {
        // Process the logic of completing tasks here
        // (This is for the future, temporarily empty)
        println("Task with ID: $taskId marked as complete.")
    }
}