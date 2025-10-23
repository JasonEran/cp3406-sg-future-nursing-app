package com.example.sgfuturenursingapp.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sgfuturenursingapp.domain.usecase.LogoutUserUseCase
import com.example.sgfuturenursingapp.ui.data.CareGroup
import com.example.sgfuturenursingapp.ui.data.User
import com.example.sgfuturenursingapp.ui.data.auth.AuthRepository
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
    val isLoggingOut: Boolean = false,
    val logoutError: String? = null,
    val logoutSuccess: Boolean = false,
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
                    )
                }

                runCatching {
                    val currentUser =
                        authRepository.getCurrentUser()
                            ?: error("\u5f53\u524d\u767b\u5f55\u4fe1\u606f\u5df2\u5931\u6548\uff0c\u8bf7\u91cd\u65b0\u767b\u5f55\u3002")

                    val userSnapshot =
                        firestore
                            .collection(USERS_COLLECTION)
                            .document(currentUser.uid)
                            .get()
                            .await()

                    val userRecord =
                        userSnapshot.toObject(User::class.java)
                            ?: error("\u65e0\u6cd5\u52a0\u8f7d\u7528\u6237\u8d44\u6599\u3002")

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
                                    email = memberRecord?.email.orEmpty().ifBlank { "\u672a\u77e5\u6210\u5458 ($uid)" },
                                    role = storedRole.ifBlank { memberRecord?.role.orEmpty() },
                                )
                            }.sortedBy { it.email.lowercase() }
                        }

                    ProfileUiState(
                        isLoading = false,
                        email = userRecord.email.orEmpty(),
                        role = userRecord.role.orEmpty(),
                        careGroupName =
                        careGroup?.groupName
                                ?.takeIf { it.isNotBlank() }
                                ?: "\u672a\u52a0\u5165\u62a4\u7406\u7ec4",
                        members = members,
                        canManageTeam = userRecord.role.equals(ADMIN_ROLE, ignoreCase = true),
                        notificationEnabled = _uiState.value.notificationEnabled,
                    )
                }.onSuccess { resolvedState ->
                    _uiState.value = resolvedState
                }.onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "\u65e0\u6cd5\u52a0\u8f7d\u4e2a\u4eba\u8d44\u6599\uff0c\u8bf7\u7a0d\u540e\u518d\u8bd5\u3002",
                        )
                    }
                }
            }
        }

        fun setNotificationsEnabled(enabled: Boolean) {
            _uiState.update { it.copy(notificationEnabled = enabled) }
        }

        fun logout() {
            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        isLoggingOut = true,
                        logoutError = null,
                        logoutSuccess = false,
                    )
                }

                val result = logoutUserUseCase()
                result
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isLoggingOut = false,
                                logoutSuccess = true,
                            )
                        }
                    }.onFailure { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoggingOut = false,
                                logoutError = throwable.message ?: "\u767b\u51fa\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5\u3002",
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
