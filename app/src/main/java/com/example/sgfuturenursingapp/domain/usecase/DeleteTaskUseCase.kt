package com.example.sgfuturenursingapp.domain.usecase

import com.example.sgfuturenursingapp.ui.data.TaskRepository
import javax.inject.Inject

class DeleteTaskUseCase
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
    ) {
        suspend operator fun invoke(taskId: Int) {
            taskRepository.deleteTask(taskId)
        }
    }
