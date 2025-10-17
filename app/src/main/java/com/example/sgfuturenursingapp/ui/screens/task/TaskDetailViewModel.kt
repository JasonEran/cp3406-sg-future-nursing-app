package com.example.sgfuturenursingapp.ui.screens.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sgfuturenursingapp.domain.use_case.CompleteTaskUseCase
import com.example.sgfuturenursingapp.domain.use_case.GetTaskByIdUseCase
import com.example.sgfuturenursingapp.ui.data.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase
) : ViewModel() {
    private val _task = MutableStateFlow<Task?>(null)
    val task: StateFlow<Task?> = _task.asStateFlow()

    fun loadTask(taskId: String?) {
        val id = taskId?.toIntOrNull() ?: return
        viewModelScope.launch {
            _task.value = getTaskByIdUseCase(id)
        }
    }

    fun completeTask() {
        val currentTask = _task.value ?: return
        viewModelScope.launch {
            completeTaskUseCase(currentTask.id)
            _task.value = getTaskByIdUseCase(currentTask.id)
        }
    }
}
