@file:Suppress("ktlint:standard:function-naming")

package com.example.sgfuturenursingapp.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sgfuturenursingapp.ui.data.User
import com.example.sgfuturenursingapp.ui.data.auth.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class HelperManagementUiState(
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val isUpdating: Boolean = false,
    val foundUser: User? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isMemberOfGroup: Boolean = false,
)

@HiltViewModel
class HelperManagementViewModel
    @Inject
    constructor(
        private val firestore: FirebaseFirestore,
        private val authRepository: AuthRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(HelperManagementUiState())
        val uiState: StateFlow<HelperManagementUiState> = _uiState.asStateFlow()

        private val usersCollection = firestore.collection(USERS_COLLECTION)
        private val careGroupsCollection = firestore.collection(CARE_GROUPS_COLLECTION)

        private var adminUid: String? = null
        private var careGroupId: String? = null

        init {
            viewModelScope.launch {
                runCatching {
                    val admin = authRepository.getCurrentUser()
                    val uid = admin?.uid ?: error("No authenticated admin.")
                    adminUid = uid
                    val userSnapshot = usersCollection.document(uid).get().await()
                    val userRecord = userSnapshot.toObject(User::class.java)
                    val resolvedGroupId = userRecord?.careGroupId?.takeIf { it.isNotBlank() }
                    careGroupId = resolvedGroupId ?: uid
                }.onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            errorMessage = throwable.message ?: "Unable to load admin information.",
                        )
                    }
                }
            }
        }

        fun onSearchQueryChange(value: String) {
            _uiState.update {
                it.copy(
                    searchQuery = value,
                    errorMessage = null,
                    successMessage = null,
                )
            }
        }

        fun searchHelperByEmail() {
            val query = _uiState.value.searchQuery.trim()
            if (query.isEmpty()) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Please enter an email address to search.",
                        successMessage = null,
                        foundUser = null,
                        isMemberOfGroup = false,
                    )
                }
                return
            }

            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        isSearching = true,
                        errorMessage = null,
                        successMessage = null,
                        foundUser = null,
                        isMemberOfGroup = false,
                    )
                }

                runCatching {
                    val snapshot =
                        usersCollection
                            .whereEqualTo(USER_EMAIL_FIELD, query)
                            .limit(1)
                            .get()
                            .await()

                    val user =
                        snapshot.documents
                            .firstOrNull()
                            ?.toObject(User::class.java)

                    if (user == null) {
                        throw IllegalStateException("No helper found for $query.")
                    }

                    val currentAdmin = adminUid
                    if (user.uid == currentAdmin) {
                        throw IllegalStateException("You cannot add yourself to the care group.")
                    }

                    val currentCareGroupId = careGroupId
                    if (currentCareGroupId != null && user.careGroupId == currentCareGroupId) {
                        _uiState.update {
                            it.copy(
                                foundUser = user,
                                successMessage = "${user.email} is already in this care group.",
                                isMemberOfGroup = true,
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                foundUser = user,
                                successMessage = null,
                                isMemberOfGroup = false,
                            )
                        }
                    }
                }.onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            errorMessage = throwable.message ?: "Unable to find helper.",
                            foundUser = null,
                            successMessage = null,
                            isMemberOfGroup = false,
                        )
                    }
                }.also {
                    _uiState.update { state -> state.copy(isSearching = false) }
                }
            }
        }

        fun addHelperToCareGroup() {
            val user = _uiState.value.foundUser ?: run {
                _uiState.update {
                    it.copy(errorMessage = "Please search and select a helper first.")
                }
                return
            }

            val groupId = careGroupId
            val adminId = adminUid
            if (groupId.isNullOrBlank() || adminId.isNullOrBlank()) {
                _uiState.update {
                    it.copy(errorMessage = "Unable to resolve admin care group.")
                }
                return
            }

            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        isUpdating = true,
                        errorMessage = null,
                        successMessage = null,
                    )
                }

                runCatching {
                    firestore
                        .runBatch { batch ->
                            val helperRef = usersCollection.document(user.uid)
                            val careGroupRef = careGroupsCollection.document(groupId)

                            batch.set(
                                careGroupRef,
                                mapOf(CARE_GROUP_ID_FIELD to groupId),
                                SetOptions.merge(),
                            )

                            batch.set(
                                helperRef,
                                mapOf(
                                    USER_CARE_GROUP_FIELD to groupId,
                                    USER_ROLE_FIELD to HELPER_ROLE,
                                ),
                                SetOptions.merge(),
                            )

                            batch.update(
                                careGroupRef,
                                mapOf("$CARE_GROUP_MEMBERS_FIELD.${user.uid}" to HELPER_ROLE),
                            )

                            batch.update(
                                careGroupRef,
                                mapOf(CARE_GROUP_ADMIN_FIELD to adminId),
                            )
                        }.await()
                }.onSuccess {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            successMessage = "${user.email} added to care group.",
                            foundUser = user.copy(
                                careGroupId = groupId,
                                role = HELPER_ROLE,
                            ),
                            isMemberOfGroup = true,
                        )
                    }
                }.onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            errorMessage = throwable.message ?: "Unable to add helper.",
                        )
                    }
                }
            }
        }

        fun clearMessages() {
            _uiState.update {
                it.copy(
                    errorMessage = null,
                    successMessage = null,
                )
            }
        }

        companion object {
            private const val USERS_COLLECTION = "users"
            private const val CARE_GROUPS_COLLECTION = "care_groups"
            private const val USER_EMAIL_FIELD = "email"
            private const val USER_CARE_GROUP_FIELD = "careGroupId"
            private const val USER_ROLE_FIELD = "role"
            private const val CARE_GROUP_MEMBERS_FIELD = "members"
            private const val CARE_GROUP_ADMIN_FIELD = "adminUid"
            private const val CARE_GROUP_ID_FIELD = "groupId"
            private const val HELPER_ROLE = "Helper"
        }
    }
