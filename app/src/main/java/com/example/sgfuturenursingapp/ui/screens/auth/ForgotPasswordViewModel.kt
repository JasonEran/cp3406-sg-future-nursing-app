package com.example.sgfuturenursingapp.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sgfuturenursingapp.domain.usecase.ForgotPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
) {
    val canSubmit: Boolean get() = email.isNotBlank()
}

@HiltViewModel
class ForgotPasswordViewModel
    @Inject
    constructor(
        private val forgotPasswordUseCase: ForgotPasswordUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ForgotPasswordUiState())
        val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

        fun onEmailChanged(value: String) {
            _uiState.update {
                it.copy(
                    email = value,
                    successMessage = null,
                    errorMessage = null,
                )
            }
        }

        fun sendResetEmail() {
            val email = _uiState.value.email.trim()
            if (email.isBlank()) {
                _uiState.update {
                    it.copy(errorMessage = "Please enter your email address.")
                }
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, successMessage = null, errorMessage = null) }
                val result = forgotPasswordUseCase(email)
                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "Password reset email sent to $email.",
                                errorMessage = null,
                            )
                        }
                    },
                    onFailure = { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = throwable.message ?: "Unable to send reset email right now.",
                            )
                        }
                    },
                )
            }
        }

        fun clearMessages() {
            _uiState.update { it.copy(successMessage = null, errorMessage = null) }
        }
    }

