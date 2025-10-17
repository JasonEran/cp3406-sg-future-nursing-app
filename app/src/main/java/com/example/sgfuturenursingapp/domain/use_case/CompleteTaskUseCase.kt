package com.example.sgfuturenursingapp.domain.use_case

import com.example.sgfuturenursingapp.ui.data.TaskRepository
import javax.inject.Inject

class CompleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Int) {
        taskRepository.completeTask(taskId)
    }
}
