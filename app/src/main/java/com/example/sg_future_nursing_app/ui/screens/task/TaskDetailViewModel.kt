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
            // 从Repository获取任务
            _task.value = TaskRepository.getTaskById(id)
        }
    }

    fun completeTask() {
        // 确保任务不为空时才执行
        _task.value?.let { currentTask ->
            TaskRepository.completeTask(currentTask.id)
            // 更新本地状态以立即反映UI变化
            _task.value = currentTask.copy(isCompleted = true)
        }
    }
}