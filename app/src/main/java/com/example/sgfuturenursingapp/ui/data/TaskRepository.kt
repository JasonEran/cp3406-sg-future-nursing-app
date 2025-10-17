package com.example.sgfuturenursingapp.ui.data

import com.example.sgfuturenursingapp.ui.data.auth.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@Singleton
class TaskRepository
    @Inject
    constructor(
        private val taskDao: TaskDao,
        private val authRepository: AuthRepository,
    ) {
        @OptIn(ExperimentalCoroutinesApi::class)
        val tasks: Flow<List<Task>> =
            authRepository.authStateFlow().flatMapLatest { user ->
                val userId = user?.uid ?: return@flatMapLatest flowOf(emptyList())
                taskDao.getTasks(userId)
            }

        suspend fun getTaskById(taskId: Int): Task? {
            val userId = requireUserId()
            return taskDao.getTaskById(taskId, userId)
        }

        suspend fun upsertTask(task: Task): Task {
            val userId = requireUserId()
            val taskToSave = task.copy(userId = userId)
            taskDao.insertTask(taskToSave)
            return taskToSave
        }

        suspend fun completeTask(taskId: Int) {
            val userId = requireUserId()
            taskDao.updateTaskCompletion(taskId, completed = true, userId = userId)
        }

        suspend fun getNextTaskId(): Int {
            val userId = requireUserId()
            return (taskDao.getMaxTaskId(userId) ?: 0) + 1
        }

        suspend fun deleteTask(taskId: Int) {
            val userId = requireUserId()
            taskDao.deleteTask(taskId, userId)
        }

        private suspend fun requireUserId(): String =
            authRepository.getCurrentUser()?.uid
                ?: error("No authenticated user found. Please log in to manage tasks.")
    }
