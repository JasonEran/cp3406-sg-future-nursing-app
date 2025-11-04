package com.example.sgfuturenursingapp.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sgfuturenursingapp.R
import com.example.sgfuturenursingapp.domain.usecase.EnsureCurrentUserRecordUseCase
import com.example.sgfuturenursingapp.domain.usecase.GetCurrentUserProfileUseCase
import com.example.sgfuturenursingapp.domain.usecase.GetCurrentUserUseCase
import com.example.sgfuturenursingapp.domain.usecase.LoginUserUseCase
import com.example.sgfuturenursingapp.domain.usecase.LogoutUserUseCase
import com.example.sgfuturenursingapp.domain.usecase.ObserveAuthStateUseCase
import com.example.sgfuturenursingapp.domain.usecase.RegisterUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.sgfuturenursingapp.ui.demo.DemoModeController
import com.example.sgfuturenursingapp.ui.localization.AppLanguage
import com.example.sgfuturenursingapp.ui.localization.LanguageController

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val userEmail: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val errorMessageRes: Int? = null,
    val selectedLanguage: AppLanguage = LanguageController.languageFlow.value,
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
        private val ensureCurrentUserRecordUseCase: EnsureCurrentUserRecordUseCase,
        private val getCurrentUserProfileUseCase: GetCurrentUserProfileUseCase,
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
                    errorMessageRes = null,
                )
            }
        }

        fun onPasswordChanged(value: String) {
            _uiState.update {
                it.copy(
                    password = value,
                    errorMessage = null,
                    errorMessageRes = null,
                )
            }
        }

        fun onLanguageSelected(language: AppLanguage) {
            _uiState.update {
                it.copy(
                    selectedLanguage = language,
                    errorMessage = null,
                    errorMessageRes = null,
                )
            }
            LanguageController.updateLanguage(language)
        }

        fun login() {
            val currentState = _uiState.value
            val email = currentState.email.trim()
            val password = currentState.password
            if (!currentState.isFormValid) {
                _uiState.update {
                    it.copy(
                        email = email,
                        errorMessage = null,
                        errorMessageRes = R.string.auth_error_invalid_input,
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
                        errorMessageRes = null,
                    )
                }
                val result = loginUserUseCase(email, password)
                result.fold(
                    onSuccess = { user ->
                        DemoModeController.disableDemoMode()
                        _uiState.update {
                            it.copy(
                                userEmail = user?.email ?: it.userEmail,
                                isLoading = false,
                                errorMessage = null,
                                errorMessageRes = null,
                                password = "",
                            )
                        }
                        applyLanguageFromProfile()
                    },
                    onFailure = { throwable ->
                        if (throwable !is CancellationException) {
                            Firebase.crashlytics.recordException(throwable)
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = throwable.message ?: "Unable to log in right now.",
                                errorMessageRes = null,
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
            val language = currentState.selectedLanguage
            if (!currentState.isFormValid) {
                _uiState.update {
                    it.copy(
                        email = email,
                        errorMessage = null,
                        errorMessageRes = R.string.auth_error_invalid_input,
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
                        errorMessageRes = null,
                    )
                }
                val result = registerUserUseCase(email, password, language.code)
                result.fold(
                    onSuccess = { user ->
                        DemoModeController.disableDemoMode()
                        _uiState.update {
                            it.copy(
                                userEmail = user?.email ?: it.userEmail,
                                isLoading = false,
                                errorMessage = null,
                                errorMessageRes = null,
                                password = "",
                            )
                        }
                        LanguageController.updateLanguage(language)
                        applyLanguageFromProfile()
                    },
                    onFailure = { throwable ->
                        if (throwable !is CancellationException) {
                            Firebase.crashlytics.recordException(throwable)
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = throwable.message ?: "Unable to register right now.",
                                errorMessageRes = null,
                            )
                        }
                    },
                )
            }
        }

        fun logout() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null, errorMessageRes = null) }
                val result = logoutUserUseCase()
                result.fold(
                    onSuccess = {
                        DemoModeController.disableDemoMode()
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = null,
                                errorMessageRes = null,
                                userEmail = null,
                                password = "",
                            )
                        }
                    },
                    onFailure = { throwable ->
                        if (throwable !is CancellationException) {
                            Firebase.crashlytics.recordException(throwable)
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = throwable.message ?: "Unable to log out right now.",
                                errorMessageRes = null,
                            )
                        }
                    },
                )
            }
        }

        fun clearError() {
            _uiState.update { it.copy(errorMessage = null, errorMessageRes = null) }
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
                        errorMessageRes = null,
                    )
                }
                if (currentUser != null) {
                    try {
                        ensureCurrentUserRecordUseCase()
                    } catch (ignored: Throwable) {
                        // Intentionally ignore to avoid blocking UI if record sync fails
                    }
                }

                observeAuthStateUseCase().collect { user ->
                    if (user != null) {
                        DemoModeController.disableDemoMode()
                        try {
                            ensureCurrentUserRecordUseCase()
                        } catch (ignored: Throwable) {
                            // Intentionally ignore to avoid blocking UI if record sync fails
                        }
                        applyLanguageFromProfile()
                    }
                    _uiState.update {
                        it.copy(
                            email = user?.email ?: it.email,
                            userEmail = user?.email,
                            isLoading = false,
                            errorMessage = if (user != null) null else it.errorMessage,
                            errorMessageRes = if (user != null) null else it.errorMessageRes,
                            password = if (user != null) "" else it.password,
                        )
                    }
                }
            }
        }

        private fun applyLanguageFromProfile() {
            viewModelScope.launch {
                val profile = getCurrentUserProfileUseCase()
                val language = AppLanguage.fromCode(profile?.language)
                LanguageController.updateLanguage(language)
                _uiState.update {
                    it.copy(
                        selectedLanguage = language,
                        errorMessage = null,
                        errorMessageRes = null,
                    )
                }
            }
        }
    }

