package com.example.sgfuturenursingapp.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sgfuturenursingapp.domain.usecase.GetCurrentUserUseCase
import com.example.sgfuturenursingapp.domain.usecase.LoginUserUseCase
import com.example.sgfuturenursingapp.domain.usecase.LogoutUserUseCase
import com.example.sgfuturenursingapp.domain.usecase.ObserveAuthStateUseCase
import com.example.sgfuturenursingapp.domain.usecase.RegisterUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val userEmail: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val isAuthenticated: Boolean get() = userEmail != null
    val isFormValid: Boolean get() = email.isNotBlank() && password.length >= 6
}

@HiltViewModel
class AuthViewModel
    @Inject
    constructor(
        private val loginUserUseCase: LoginUserUseCase,
        private val registerUserUseCase: RegisterUserUseCase,
        private val logoutUserUseCase: LogoutUserUseCase,
        private val getCurrentUserUseCase: GetCurrentUserUseCase,
        private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(AuthUiState(isLoading = true))
        val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

        init {
            observeAuthState()
        }

        fun onEmailChanged(value: String) {
            _uiState.update {
                it.copy(
                    email = value,
                    errorMessage = null,
                )
            }
        }

        fun onPasswordChanged(value: String) {
            _uiState.update {
                it.copy(
                    password = value,
                    errorMessage = null,
                )
            }
        }

        fun login() {
            val currentState = _uiState.value
            val email = currentState.email.trim()
            val password = currentState.password
            if (!currentState.isFormValid) {
                _uiState.update {
                    it.copy(
                        email = email,
                        errorMessage = "Please enter a valid email and a password with at least 6 characters.",
                    )
                }
                return
            }
            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        email = email,
                        isLoading = true,
                        errorMessage = null,
                    )
                }
                val result = loginUserUseCase(email, password)
                result.fold(
                    onSuccess = { user ->
                        _uiState.update {
                            it.copy(
                                userEmail = user?.email ?: it.userEmail,
                                isLoading = false,
                                errorMessage = null,
                                password = "",
                            )
                        }
                    },
                    onFailure = { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = throwable.message ?: "Unable to log in right now.",
                            )
                        }
                    },
                )
            }
        }

        fun register() {
            val currentState = _uiState.value
            val email = currentState.email.trim()
            val password = currentState.password
            if (!currentState.isFormValid) {
                _uiState.update {
                    it.copy(
                        email = email,
                        errorMessage = "Please enter a valid email and a password with at least 6 characters.",
                    )
                }
                return
            }
            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        email = email,
                        isLoading = true,
                        errorMessage = null,
                    )
                }
                val result = registerUserUseCase(email, password)
                result.fold(
                    onSuccess = { user ->
                        _uiState.update {
                            it.copy(
                                userEmail = user?.email ?: it.userEmail,
                                isLoading = false,
                                errorMessage = null,
                                password = "",
                            )
                        }
                    },
                    onFailure = { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = throwable.message ?: "Unable to register right now.",
                            )
                        }
                    },
                )
            }
        }

        fun logout() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                val result = logoutUserUseCase()
                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = null,
                                userEmail = null,
                                password = "",
                            )
                        }
                    },
                    onFailure = { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = throwable.message ?: "Unable to log out right now.",
                            )
                        }
                    },
                )
            }
        }

        fun clearError() {
            _uiState.update { it.copy(errorMessage = null) }
        }

        private fun observeAuthState() {
            viewModelScope.launch {
                val currentUser = getCurrentUserUseCase()
                _uiState.update {
                    it.copy(
                        email = currentUser?.email ?: it.email,
                        userEmail = currentUser?.email,
                        isLoading = false,
                        errorMessage = null,
                    )
                }

                observeAuthStateUseCase().collect { user ->
                    _uiState.update {
                        it.copy(
                            email = user?.email ?: it.email,
                            userEmail = user?.email,
                            isLoading = false,
                            errorMessage = if (user != null) null else it.errorMessage,
                            password = if (user != null) "" else it.password,
                        )
                    }
                }
            }
        }
    }
