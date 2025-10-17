package com.example.sgfuturenursingapp.domain.usecase

import com.example.sgfuturenursingapp.ui.data.Task
import com.example.sgfuturenursingapp.ui.data.TaskRepository
import javax.inject.Inject

class AddTaskUseCase
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
    ) {
        suspend operator fun invoke(
            title: String,
            time: String,
            category: String,
            taskId: Int? = null,
            iconName: String = DEFAULT_ICON_NAME,
            isCompleted: Boolean = false,
            priority: Int = 0,
        ): Task {
            val id = taskId ?: taskRepository.getNextTaskId()
            val task =
                Task(
                    id = id,
                    title = title,
                    time = time,
                    category = category,
                    iconName = iconName,
                    isCompleted = isCompleted,
                    priority = priority,
                )
            taskRepository.upsertTask(task)
            return task
        }

        private companion object {
            const val DEFAULT_ICON_NAME = "event"
        }
    }
