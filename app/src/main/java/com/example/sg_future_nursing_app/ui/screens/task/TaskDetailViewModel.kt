package com.example.sg_future_nursing_app.ui.screens.task

import androidx.lifecycle.ViewModel
import com.example.sg_future_nursing_app.ui.data.DummyDataProvider
import com.example.sg_future_nursing_app.ui.data.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TaskDetailViewModel : ViewModel() {
    private val _task = MutableStateFlow<Task?>(null)
    val task: StateFlow<Task?> = _task.asStateFlow()

    fun loadTask(taskId: String?) {
        val id = taskId?.toIntOrNull()
        if (id != null) {
            // In practical applications, it will be obtained from databases or networks
            // Still using DummyData Provider
            _task.value = DummyDataProvider.tasks.find { it.id == id }
        }
    }
}