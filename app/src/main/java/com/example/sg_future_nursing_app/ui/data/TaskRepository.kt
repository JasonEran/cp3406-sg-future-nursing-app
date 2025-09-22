package com.example.sg_future_nursing_app.ui.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Use singleton objects to ensure that the entire app has only one data source
object TaskRepository {

    // Use MutableStateFlow to save a list of observable tasks
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    init {
        // Load fake data during initialization
        _tasks.value = DummyDataProvider.tasks
    }

    fun getTaskById(taskId: Int): Task? {
        return _tasks.value.find { it.id == taskId }
    }

    fun completeTask(taskId: Int) {
        _tasks.update { currentTasks ->
            currentTasks.map { task ->
                if (task.id == taskId) {
                    // Create a new Task object and update its isCompleted status
                    task.copy(isCompleted = true)
                } else {
                    task
                }
            }
        }
    }
}