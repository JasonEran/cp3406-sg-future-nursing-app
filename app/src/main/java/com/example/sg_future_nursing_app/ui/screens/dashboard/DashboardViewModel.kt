package com.example.sg_future_nursing_app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sg_future_nursing_app.ui.data.Task
import com.example.sg_future_nursing_app.ui.data.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// 定义仪表盘UI的状态
data class DashboardUiState(
    val tasks: List<Task> = emptyList(),
    val userName: String = "Mark"
)

class DashboardViewModel : ViewModel() {

    // 直接从Repository中观察并转换数据流
    val uiState: StateFlow<DashboardUiState> =
        TaskRepository.tasks.map { tasks ->
            DashboardUiState(tasks = tasks)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState()
        )

    fun completeTask(taskId: Int) {
        // 将操作委托给Repository
        TaskRepository.completeTask(taskId)
    }
}