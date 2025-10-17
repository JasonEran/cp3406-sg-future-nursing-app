package com.example.sgfuturenursingapp.domain.usecase

import com.example.sgfuturenursingapp.ui.data.Task
import com.example.sgfuturenursingapp.ui.data.TaskRepository
import javax.inject.Inject

class GetTaskByIdUseCase
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
    ) {
        suspend operator fun invoke(taskId: Int): Task? = taskRepository.getTaskById(taskId)
    }
