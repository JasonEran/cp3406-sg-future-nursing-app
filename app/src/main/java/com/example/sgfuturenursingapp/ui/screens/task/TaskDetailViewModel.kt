package com.example.sgfuturenursingapp.ui.screens.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sgfuturenursingapp.ui.data.Task
import com.example.sgfuturenursingapp.ui.data.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {
    private val _task = MutableStateFlow<Task?>(null)
    val task: StateFlow<Task?> = _task.asStateFlow()

    fun loadTask(taskId: String?) {
        val id = taskId?.toIntOrNull() ?: return
        viewModelScope.launch {
            _task.value = taskRepository.getTaskById(id)
        }
    }

    fun completeTask() {
        val currentTask = _task.value ?: return
        viewModelScope.launch {
            taskRepository.completeTask(currentTask.id)
            _task.value = taskRepository.getTaskById(currentTask.id)
        }
    }
}
