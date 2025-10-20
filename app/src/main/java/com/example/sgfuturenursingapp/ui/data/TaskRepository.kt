package com.example.sgfuturenursingapp.ui.data

import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasks(): Flow<List<Task>>
    suspend fun getTaskById(taskId: Int): Task?
    suspend fun upsertTask(task: Task): Task
    suspend fun completeTask(taskId: Int)
    suspend fun getNextTaskId(): Int
    suspend fun deleteTask(taskId: Int)
}
