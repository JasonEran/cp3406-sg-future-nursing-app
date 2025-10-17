package com.example.sgfuturenursingapp.ui.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository
    @Inject
    constructor(
        private val taskDao: TaskDao,
    ) {
        val tasks: Flow<List<Task>> = taskDao.getTasks()

        suspend fun getTaskById(taskId: Int): Task? = taskDao.getTaskById(taskId)

        suspend fun upsertTask(task: Task) {
            taskDao.insertTask(task)
        }

        suspend fun completeTask(taskId: Int) {
            taskDao.updateTaskCompletion(taskId, completed = true)
        }

        suspend fun getNextTaskId(): Int = (taskDao.getMaxTaskId() ?: 0) + 1
    }
