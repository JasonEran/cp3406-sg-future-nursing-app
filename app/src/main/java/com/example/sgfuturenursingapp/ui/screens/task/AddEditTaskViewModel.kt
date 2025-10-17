package com.example.sgfuturenursingapp.ui.screens.task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sgfuturenursingapp.domain.usecase.AddTaskUseCase
import com.example.sgfuturenursingapp.domain.usecase.GetTaskByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(AddEditTaskUiState())
        val uiState: StateFlow<AddEditTaskUiState> = _uiState.asStateFlow()

        init {
            val taskIdArg = savedStateHandle.get<Int?>(TASK_ID_KEY)?.takeIf { it != DEFAULT_TASK_ID }
            if (taskIdArg != null) {
                loadTask(taskIdArg)
            }
        }

        fun onTitleChanged(value: String) {
            _uiState.update { it.copy(title = value, errorMessage = null) }
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
                _uiState.update { it.copy(errorMessage = "Title cannot be empty") }
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
                runCatching {
                    addTaskUseCase(
                        title = title,
                        time = time,
                        category = category,
                        taskId = currentState.taskId,
                        iconName = currentState.iconName,
                        isCompleted = currentState.isCompleted,
                        priority = currentState.priority,
                    )
                }.onSuccess {
                    _uiState.update { it.copy(isSaving = false, isSaved = true) }
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
                    )
                }
            }
        }

        companion object {
            const val TASK_ID_KEY = "taskId"
            private const val DEFAULT_TASK_ID = -1
        }
    }
