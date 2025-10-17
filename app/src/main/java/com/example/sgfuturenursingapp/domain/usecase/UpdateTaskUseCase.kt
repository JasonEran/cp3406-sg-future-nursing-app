package com.example.sgfuturenursingapp.domain.usecase

import com.example.sgfuturenursingapp.ui.data.Task
import com.example.sgfuturenursingapp.ui.data.TaskRepository
import javax.inject.Inject

class UpdateTaskUseCase
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
    ) {
        suspend operator fun invoke(
            taskId: Int,
            title: String,
            time: String,
            category: String,
            iconName: String,
            isCompleted: Boolean,
            priority: Int,
        ): Task {
            val task =
                Task(
                    id = taskId,
                    title = title,
                    time = time,
                    category = category,
                    iconName = iconName,
                    isCompleted = isCompleted,
                    priority = priority,
                )
            return taskRepository.upsertTask(task)
        }
    }
