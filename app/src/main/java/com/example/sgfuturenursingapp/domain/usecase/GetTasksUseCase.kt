package com.example.sgfuturenursingapp.domain.usecase

import com.example.sgfuturenursingapp.ui.data.Task
import com.example.sgfuturenursingapp.ui.data.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTasksUseCase
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
    ) {
        operator fun invoke(): Flow<List<Task>> = taskRepository.tasks
    }
