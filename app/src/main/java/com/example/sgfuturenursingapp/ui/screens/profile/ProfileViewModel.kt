package com.example.sgfuturenursingapp.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sgfuturenursingapp.R
import com.example.sgfuturenursingapp.domain.usecase.LogoutUserUseCase
import com.example.sgfuturenursingapp.domain.usecase.UpdateUserLanguageUseCase
import com.example.sgfuturenursingapp.ui.data.CareGroup
import com.example.sgfuturenursingapp.ui.data.User
import com.example.sgfuturenursingapp.ui.data.auth.AuthRepository
import com.example.sgfuturenursingapp.ui.localization.AppLanguage
import com.example.sgfuturenursingapp.ui.localization.LanguageController
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ProfileUiState(
    val isLoading: Boolean = true,
    val email: String = "",
    val role: String = "",
    val careGroupName: String = "",
    val members: List<CareGroupMemberUi> = emptyList(),
    val canManageTeam: Boolean = false,
    val notificationEnabled: Boolean = true,
    val errorMessage: String? = null,
    val errorMessageRes: Int? = null,
    val isLoggingOut: Boolean = false,
    val logoutSuccess: Boolean = false,
    val selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    val isUpdatingLanguage: Boolean = false,
)

data class CareGroupMemberUi(
    val uid: String,
    val email: String,
    val role: String,
)

@HiltViewModel
class ProfileViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        private val firestore: FirebaseFirestore,
        private val logoutUserUseCase: LogoutUserUseCase,
        private val updateUserLanguageUseCase: UpdateUserLanguageUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ProfileUiState())
        val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

        init {
            refreshProfile()
        }

        fun refreshProfile() {
            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        isLoading = true,
                        errorMessage = null,
                        errorMessageRes = null,
                    )
                }

                val currentUser =
                    authRepository.getCurrentUser()
                if (currentUser == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            errorMessageRes = R.string.error_profile_expired_session,
                        )
                    }
                    return@launch
                }

                runCatching {
                    val userSnapshot =
                        firestore
                            .collection(USERS_COLLECTION)
                            .document(currentUser.uid)
                            .get()
                            .await()

                    val userRecord =
                        userSnapshot.toObject(User::class.java)
                            ?: error("profile.load_failed")

                    val careGroupId = userRecord.careGroupId

                    val careGroup =
                        careGroupId
                            ?.takeIf { it.isNotBlank() }
                            ?.let { id ->
                                firestore
                                    .collection(CARE_GROUPS_COLLECTION)
                                    .document(id)
                                    .get()
                                    .await()
                                    .toObject(CareGroup::class.java)
                            }

                    val membersMap = careGroup?.members.orEmpty()
                    val usersCollection = firestore.collection(USERS_COLLECTION)
                    val members =
                        if (membersMap.isEmpty()) {
                            emptyList()
                        } else {
                            membersMap.map { (uid, storedRole) ->
                                val memberSnapshot = usersCollection.document(uid).get().await()
                                val memberRecord = memberSnapshot.toObject(User::class.java)
                                CareGroupMemberUi(
                                    uid = uid,
                                    email = memberRecord?.email.orEmpty().ifBlank { uid },
                                    role = storedRole.ifBlank { memberRecord?.role.orEmpty() },
                                )
                            }.sortedBy { it.email.lowercase() }
                        }

                    val language = AppLanguage.fromCode(userRecord.language)
                    LanguageController.updateLanguage(language)

                    ProfileUiState(
                        isLoading = false,
                        email = userRecord.email.orEmpty(),
                        role = userRecord.role.orEmpty(),
                        careGroupName =
                            careGroup?.groupName
                                ?.takeIf { it.isNotBlank() }
                                ?: "",
                        members = members,
                        canManageTeam = userRecord.role.equals(ADMIN_ROLE, ignoreCase = true),
                        notificationEnabled = _uiState.value.notificationEnabled,
                        selectedLanguage = language,
                    )
                }.onSuccess { resolvedState ->
                    _uiState.value = resolvedState
                }.onFailure { throwable ->
                    val errorRes =
                        if (throwable.message == "profile.load_failed") {
                            R.string.error_profile_load_failed
                        } else {
                            R.string.error_profile_load_failed
                        }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message,
                            errorMessageRes = errorRes,
                        )
                    }
                }
            }
        }

        fun setNotificationsEnabled(enabled: Boolean) {
            _uiState.update { it.copy(notificationEnabled = enabled) }
        }

        fun onLanguageSelected(language: AppLanguage) {
            val currentState = _uiState.value
            if (language == currentState.selectedLanguage) return
            val previousLanguage = currentState.selectedLanguage

            _uiState.update {
                it.copy(
                    selectedLanguage = language,
                    isUpdatingLanguage = true,
                    errorMessage = null,
                    errorMessageRes = null,
                )
            }

            viewModelScope.launch {
                val result = updateUserLanguageUseCase(language.code)
                result
                    .onSuccess {
                        LanguageController.updateLanguage(language)
                        _uiState.update {
                            it.copy(
                                isUpdatingLanguage = false,
                                errorMessageRes = null,
                            )
                        }
                    }.onFailure { throwable ->
                        _uiState.update {
                            it.copy(
                                selectedLanguage = previousLanguage,
                                isUpdatingLanguage = false,
                                errorMessage = throwable.message,
                                errorMessageRes = R.string.error_language_update_failed,
                            )
                        }
                    }
            }
        }

        fun logout() {
            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        isLoggingOut = true,
                        logoutSuccess = false,
                        errorMessage = null,
                        errorMessageRes = null,
                    )
                }

                val result = logoutUserUseCase()
                result
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isLoggingOut = false,
                                logoutSuccess = true,
                                errorMessageRes = null,
                                selectedLanguage = AppLanguage.ENGLISH,
                            )
                        }
                    }.onFailure { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoggingOut = false,
                                errorMessage = throwable.message,
                                errorMessageRes = R.string.error_profile_logout_failed,
                            )
                        }
                    }
            }
        }

        fun consumeLogoutSuccess() {
            _uiState.update { it.copy(logoutSuccess = false) }
        }

        private companion object {
            const val USERS_COLLECTION = "users"
            const val CARE_GROUPS_COLLECTION = "care_groups"
            const val ADMIN_ROLE = "Admin"
        }
    }
