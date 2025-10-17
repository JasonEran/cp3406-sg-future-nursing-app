package com.example.sg_future_nursing_app.ui.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {

    val tasks: Flow<List<Task>> = taskDao.getTasks()

    suspend fun getTaskById(taskId: Int): Task? = taskDao.getTaskById(taskId)

    suspend fun completeTask(taskId: Int) {
        taskDao.updateTaskCompletion(taskId, completed = true)
    }
}
