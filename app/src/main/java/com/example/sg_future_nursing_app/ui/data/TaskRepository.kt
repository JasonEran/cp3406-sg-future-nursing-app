package com.example.sg_future_nursing_app.ui.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// 使用单例 Object 来确保整个 App 只有一个数据源
object TaskRepository {

    // 使用 MutableStateFlow 来保存可被观察的任务列表
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    init {
        // 初始化时加载假数据
        _tasks.value = DummyDataProvider.tasks
    }

    fun getTaskById(taskId: Int): Task? {
        return _tasks.value.find { it.id == taskId }
    }

    fun completeTask(taskId: Int) {
        _tasks.update { currentTasks ->
            currentTasks.map { task ->
                if (task.id == taskId) {
                    // 创建一个新的Task对象并更新其isCompleted状态
                    task.copy(isCompleted = true)
                } else {
                    task
                }
            }
        }
    }
}