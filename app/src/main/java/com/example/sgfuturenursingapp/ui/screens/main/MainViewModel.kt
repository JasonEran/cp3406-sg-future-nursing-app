package com.example.sgfuturenursingapp.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sgfuturenursingapp.domain.usecase.GetTasksUseCase
import com.example.sgfuturenursingapp.ui.data.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class MainUiState(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val pendingTasks: Int = 0,
    val urgentTasks: List<Task> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        getTasksUseCase: GetTasksUseCase,
    ) : ViewModel() {
        val uiState: StateFlow<MainUiState> =
            getTasksUseCase()
                .map { tasks ->
                    val total = tasks.size
                    val completed = tasks.count { it.isCompleted }
                    val pending = (total - completed).coerceAtLeast(0)
                    val urgent =
                        tasks
                            .asSequence()
                            .filter { !it.isCompleted }
                            .sortedWith(
                                compareByDescending<Task> { it.priority }
                                    .thenBy { it.time },
                            ).take(MAX_URGENT_TASKS)
                            .toList()

                    MainUiState(
                        totalTasks = total,
                        completedTasks = completed,
                        pendingTasks = pending,
                        urgentTasks = urgent,
                        isLoading = false,
                    )
                }.catch { throwable ->
                    emit(
                        MainUiState(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unable to load overview data.",
                        ),
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                    initialValue = MainUiState(),
                )

        private companion object {
            const val MAX_URGENT_TASKS = 5
            const val STOP_TIMEOUT_MILLIS = 5_000L
        }
    }
