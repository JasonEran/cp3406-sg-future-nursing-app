package com.example.sg_future_nursing_app.ui.screens.task

import androidx.lifecycle.ViewModel
import com.example.sg_future_nursing_app.ui.data.Task
import com.example.sg_future_nursing_app.ui.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TaskDetailViewModel : ViewModel() {
    private val _task = MutableStateFlow<Task?>(null)
    val task: StateFlow<Task?> = _task.asStateFlow()

    fun loadTask(taskId: String?) {
        val id = taskId?.toIntOrNull()
        if (id != null) {
            // Retrieve tasks from the Repository
            _task.value = TaskRepository.getTaskById(id)
        }
    }

    fun completeTask() {
        // Ensure that the task is not empty before execution
        _task.value?.let { currentTask ->
            TaskRepository.completeTask(currentTask.id)
            // Update local status to immediately reflect UI changes
            _task.value = currentTask.copy(isCompleted = true)
        }
    }
}