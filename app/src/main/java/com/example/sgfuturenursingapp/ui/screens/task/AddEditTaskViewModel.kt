package com.example.sgfuturenursingapp.ui.screens.task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.sgfuturenursingapp.domain.usecase.AddTaskUseCase
import com.example.sgfuturenursingapp.domain.usecase.DeleteTaskUseCase
import com.example.sgfuturenursingapp.domain.usecase.GetTaskByIdUseCase
import com.example.sgfuturenursingapp.domain.usecase.UpdateTaskUseCase
import com.example.sgfuturenursingapp.ui.data.Task
import com.example.sgfuturenursingapp.worker.NotificationWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class AddEditTaskUiState(
    val taskId: Int? = null,
    val title: String = "",
    val time: String = "",
    val category: String = "",
    val iconName: String = DEFAULT_ICON,
    val isCompleted: Boolean = false,
    val priority: Int = 0,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val titleError: String? = "Title cannot be empty",
    val isSaveEnabled: Boolean = false,
    val errorMessage: String? = null,
) {
    companion object {
        const val DEFAULT_ICON = "event"
    }
}

@HiltViewModel
class AddEditTaskViewModel
    @Inject
    constructor(
        private val addTaskUseCase: AddTaskUseCase,
        private val getTaskByIdUseCase: GetTaskByIdUseCase,
        private val updateTaskUseCase: UpdateTaskUseCase,
        private val deleteTaskUseCase: DeleteTaskUseCase,
        private val workManager: WorkManager,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(AddEditTaskUiState())
        val uiState: StateFlow<AddEditTaskUiState> = _uiState.asStateFlow()

        init {
            val taskIdArg = savedStateHandle.get<Int?>(TASK_ID_KEY)?.takeIf { it != DEFAULT_TASK_ID }
            if (taskIdArg != null) {
                loadTask(taskIdArg)
            } else {
                validateTitle("")
            }
        }

        fun onTitleChanged(value: String) {
            val trimmed = value.trim()
            val error = if (trimmed.isEmpty()) "Title cannot be empty" else null
            _uiState.update {
                it.copy(
                    title = value,
                    errorMessage = null,
                    titleError = error,
                    isSaveEnabled = error == null,
                )
            }
        }

        fun onTimeChanged(value: String) {
            _uiState.update { it.copy(time = value, errorMessage = null) }
        }

        fun onCategoryChanged(value: String) {
            _uiState.update { it.copy(category = value, errorMessage = null) }
        }

        fun onPriorityChanged(value: Int) {
            _uiState.update { it.copy(priority = value.coerceAtLeast(0)) }
        }

        fun onSaveClicked() {
            val currentState = _uiState.value
            val title = currentState.title.trim()
            val time = currentState.time.trim()
            val category = currentState.category.trim()

            if (title.isEmpty()) {
                _uiState.update {
                    it.copy(
                        titleError = "Title cannot be empty",
                        isSaveEnabled = false,
                    )
                }
                return
            }
            if (time.isEmpty()) {
                _uiState.update { it.copy(errorMessage = "Time cannot be empty") }
                return
            }
            if (category.isEmpty()) {
                _uiState.update { it.copy(errorMessage = "Category cannot be empty") }
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true, errorMessage = null) }
                val result =
                    runCatching {
                        if (currentState.taskId == null) {
                            addTaskUseCase(
                                title = title,
                                time = time,
                                category = category,
                                iconName = currentState.iconName,
                                isCompleted = currentState.isCompleted,
                                priority = currentState.priority,
                            )
                        } else {
                            updateTaskUseCase(
                                taskId = currentState.taskId,
                                title = title,
                                time = time,
                                category = category,
                                iconName = currentState.iconName,
                                isCompleted = currentState.isCompleted,
                                priority = currentState.priority,
                            )
                        }
                    }
                result.onSuccess { task ->
                    _uiState.update {
                        it.copy(
                            taskId = task.id,
                            title = task.title,
                            time = task.time,
                            category = task.category,
                            iconName = task.iconName,
                            isCompleted = task.isCompleted,
                            priority = task.priority,
                            isSaving = false,
                            isSaved = true,
                            isDeleted = false,
                            titleError = null,
                            isSaveEnabled = true,
                        )
                    }
                    if (task.isCompleted) {
                        cancelTaskNotification(task.id)
                    } else {
                        scheduleTaskNotification(task)
                    }
                }.onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = throwable.message ?: "Unable to save task. Please try again.",
                        )
                    }
                }
            }
        }

        fun onDeleteClicked() {
            val currentId =
                _uiState.value.taskId ?: run {
                    _uiState.update { it.copy(errorMessage = "Unable to delete unsaved task.") }
                    return
                }
            viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true, errorMessage = null) }
                runCatching {
                    deleteTaskUseCase(currentId)
                }.onSuccess {
                    cancelTaskNotification(currentId)
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isDeleted = true,
                            isSaved = false,
                            isSaveEnabled = false,
                        )
                    }
                }.onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = throwable.message ?: "Unable to delete task. Please try again.",
                        )
                    }
                }
            }
        }

        private fun loadTask(taskId: Int) {
            viewModelScope.launch {
                val task = getTaskByIdUseCase(taskId) ?: return@launch
                _uiState.update {
                    it.copy(
                        taskId = task.id,
                        title = task.title,
                        time = task.time,
                        category = task.category,
                        iconName = task.iconName,
                        isCompleted = task.isCompleted,
                        priority = task.priority,
                        isSaved = false,
                        isDeleted = false,
                        titleError = null,
                        isSaveEnabled = true,
                    )
                }
            }
        }

        fun onActionConsumed() {
            _uiState.update { it.copy(isSaved = false, isDeleted = false) }
        }

        private fun scheduleTaskNotification(task: Task) {
            val tag = buildNotificationTag(task.id)
            val delay = calculateDelayUntilReminder(task.time) ?: Duration.ZERO
            val safeDelayMillis = delay.toMillis().coerceAtLeast(0)

            val workRequest =
                OneTimeWorkRequestBuilder<NotificationWorker>()
                    .setInitialDelay(safeDelayMillis, TimeUnit.MILLISECONDS)
                    .addTag(tag)
                    .build()

            workManager.enqueueUniqueWork(tag, ExistingWorkPolicy.REPLACE, workRequest)
        }

        private fun cancelTaskNotification(taskId: Int?) {
            if (taskId == null) return
            workManager.cancelAllWorkByTag(buildNotificationTag(taskId))
        }

        private fun calculateDelayUntilReminder(timeValue: String): Duration? {
            if (timeValue.isBlank()) return null
            val formatters =
                listOf(
                    DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()),
                    DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()),
                    DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()),
                )

            val parsedTime =
                formatters
                    .asSequence()
                    .mapNotNull { formatter -> runCatching { LocalTime.parse(timeValue, formatter) }.getOrNull() }
                    .firstOrNull()
                    ?: return null

            val zone = ZoneId.systemDefault()
            val now = ZonedDateTime.now(zone)
            var scheduled = LocalDate.now(zone).atTime(parsedTime).atZone(zone)

            if (scheduled.isBefore(now)) {
                scheduled = scheduled.plusDays(1)
            }

            var trigger = scheduled.minusMinutes(NOTIFICATION_LEAD_MINUTES)
            if (trigger.isBefore(now)) {
                trigger = now
            }

            return Duration.between(now, trigger)
        }

        private fun buildNotificationTag(taskId: Int): String = "$NOTIFICATION_WORK_TAG_PREFIX$taskId"

        companion object {
            const val TASK_ID_KEY = "taskId"
            private const val DEFAULT_TASK_ID = -1
            private const val NOTIFICATION_LEAD_MINUTES = 10L
            private const val NOTIFICATION_WORK_TAG_PREFIX = "task_notification_"
        }

        private fun validateTitle(value: String) {
            val trimmed = value.trim()
            val error = if (trimmed.isEmpty()) "Title cannot be empty" else null
            _uiState.update {
                it.copy(
                    titleError = error,
                    isSaveEnabled = error == null,
                )
            }
        }
    }
