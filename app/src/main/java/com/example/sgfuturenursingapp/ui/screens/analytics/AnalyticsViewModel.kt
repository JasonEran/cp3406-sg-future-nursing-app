package com.example.sgfuturenursingapp.ui.screens.analytics

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sgfuturenursingapp.R
import com.example.sgfuturenursingapp.ui.data.CareGroup
import com.example.sgfuturenursingapp.ui.data.Task
import com.example.sgfuturenursingapp.ui.data.User
import com.example.sgfuturenursingapp.ui.data.auth.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

private const val DEFAULT_LOOKBACK_DAYS = 7

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val accessDenied: Boolean = false,
    val lookbackDays: Int = DEFAULT_LOOKBACK_DAYS,
    val pieSegments: List<AnalyticsPieSegment> = emptyList(),
    val helperTaskCounts: List<HelperTaskCount> = emptyList(),
    val hasHelperData: Boolean = false,
    val errorMessageRes: Int? = null,
)

data class AnalyticsPieSegment(
    @StringRes val labelRes: Int,
    val value: Int,
)

data class HelperTaskCount(
    val memberId: String,
    val displayName: String,
    val completedCount: Int,
)

@HiltViewModel
class AnalyticsViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        private val firestore: FirebaseFirestore,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(AnalyticsUiState())
        val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

        init {
            refreshAnalytics()
        }

        fun refreshAnalytics(lookbackDays: Int = DEFAULT_LOOKBACK_DAYS) {
            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        isLoading = true,
                        errorMessageRes = null,
                        accessDenied = false,
                        lookbackDays = lookbackDays,
                        hasHelperData = false,
                        pieSegments = emptyList(),
                        helperTaskCounts = emptyList(),
                    )
                }

                runCatching {
                    val currentUser =
                        authRepository.getCurrentUser()
                            ?: error("analytics.session_expired")

                    val usersCollection = firestore.collection(USERS_COLLECTION)
                    val userSnapshot = usersCollection.document(currentUser.uid).get().await()
                    val userRecord =
                        userSnapshot.toObject(User::class.java)
                            ?: error("analytics.user_profile_missing")

                    if (!userRecord.role.equals(ADMIN_ROLE, ignoreCase = true)) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                accessDenied = true,
                            )
                        }
                        return@launch
                    }

                    val careGroupId = userRecord.careGroupId?.takeIf { it.isNotBlank() } ?: currentUser.uid
                    val careGroupSnapshot =
                        firestore
                            .collection(CARE_GROUPS_COLLECTION)
                            .document(careGroupId)
                            .get()
                            .await()

                    val careGroup = careGroupSnapshot.toObject(CareGroup::class.java)
                    val tasksSnapshot =
                        firestore
                            .collection(CARE_GROUPS_COLLECTION)
                            .document(careGroupId)
                            .collection(TASKS_COLLECTION)
                            .get()
                            .await()

                    val analyticsData =
                        buildAnalyticsData(
                            taskSnapshots = tasksSnapshot,
                            careGroup = careGroup,
                            lookbackDays = lookbackDays,
                            usersCollectionPath = usersCollection,
                        )

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pieSegments = analyticsData.pieSegments,
                            helperTaskCounts = analyticsData.helperCounts,
                            hasHelperData = analyticsData.hasHelperData,
                            errorMessageRes = analyticsData.errorMessageRes,
                        )
                    }
                }.onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessageRes = R.string.analytics_error_generic,
                        )
                    }
                }
            }
        }

        private suspend fun buildAnalyticsData(
            taskSnapshots: QuerySnapshot,
            careGroup: CareGroup?,
            lookbackDays: Int,
            usersCollectionPath: com.google.firebase.firestore.CollectionReference,
        ): AnalyticsComputationResult {
            if (taskSnapshots.isEmpty) {
                return AnalyticsComputationResult(
                    pieSegments =
                        listOf(
                            AnalyticsPieSegment(R.string.analytics_segment_completed, 0),
                            AnalyticsPieSegment(R.string.analytics_segment_overdue, 0),
                            AnalyticsPieSegment(R.string.analytics_segment_pending, 0),
                        ),
                    helperCounts = emptyList(),
                    hasHelperData = false,
                    errorMessageRes = null,
                )
            }

            val helperMembers =
                careGroup?.members
                    ?.filterValues { role -> role.equals(HELPER_ROLE, ignoreCase = true) }
                    .orEmpty()

            val memberEmails = mutableMapOf<String, String>()
            helperMembers.keys.forEach { memberId ->
                val snapshot = usersCollectionPath.document(memberId).get().await()
                val memberUser = snapshot.toObject(User::class.java)
                if (!memberUser?.email.isNullOrBlank()) {
                    memberEmails[memberId] = memberUser!!.email!!
                }
            }

            val thresholdInstant = ZonedDateTime.now(ZoneId.systemDefault())
                .minus(lookbackDays.toLong(), ChronoUnit.DAYS)
                .toInstant()

            var completedCount = 0
            var overdueCount = 0
            var pendingCount = 0

            val helperCompletedTotals =
                helperMembers.keys.associateWith { 0 }.toMutableMap()

            var anyTimestampAvailable = false

            taskSnapshots.documents.forEach { document ->
                val task = document.toObject(Task::class.java) ?: return@forEach

                val timestamp =
                    document.getTimestamp("updatedAt")
                        ?: document.getTimestamp("createdAt")
                val includeByTime =
                    if (timestamp != null) {
                        anyTimestampAvailable = true
                        timestamp.toDate().toInstant().isAfter(thresholdInstant)
                    } else {
                        true
                    }

                if (!includeByTime) {
                    return@forEach
                }

                if (task.isCompleted) {
                    completedCount += 1
                    if (task.userId.isNotBlank() && helperCompletedTotals.containsKey(task.userId)) {
                        helperCompletedTotals[task.userId] = helperCompletedTotals.getValue(task.userId) + 1
                    }
                } else {
                    if (task.priority >= PRIORITY_HIGH_THRESHOLD) {
                        overdueCount += 1
                    } else {
                        pendingCount += 1
                    }
                }
            }

            val pieSegments =
                listOf(
                    AnalyticsPieSegment(R.string.analytics_segment_completed, completedCount),
                    AnalyticsPieSegment(R.string.analytics_segment_overdue, overdueCount),
                    AnalyticsPieSegment(R.string.analytics_segment_pending, pendingCount),
                )

            val helperCounts =
                helperCompletedTotals.entries.map { entry ->
                    val displayName = memberEmails[entry.key] ?: entry.key
                    HelperTaskCount(
                        memberId = entry.key,
                        displayName = displayName,
                        completedCount = entry.value,
                    )
                }

            val dataEmpty = (completedCount + overdueCount + pendingCount) == 0
            val hasHelperData = helperCounts.isNotEmpty()
            val errorMessageRes =
                if (!anyTimestampAvailable) {
                    R.string.analytics_error_missing_timestamp
                } else {
                    null
                }

            return AnalyticsComputationResult(
                pieSegments =
                    if (dataEmpty) {
                        listOf(
                            AnalyticsPieSegment(R.string.analytics_segment_completed, 0),
                            AnalyticsPieSegment(R.string.analytics_segment_overdue, 0),
                            AnalyticsPieSegment(R.string.analytics_segment_pending, 0),
                        )
                    } else {
                        pieSegments
                    },
                helperCounts = helperCounts,
                hasHelperData = hasHelperData,
                errorMessageRes = errorMessageRes,
            )
        }

        private data class AnalyticsComputationResult(
            val pieSegments: List<AnalyticsPieSegment>,
            val helperCounts: List<HelperTaskCount>,
            val hasHelperData: Boolean,
            val errorMessageRes: Int?,
        )

        private companion object {
            const val USERS_COLLECTION = "users"
            const val CARE_GROUPS_COLLECTION = "care_groups"
            const val TASKS_COLLECTION = "tasks"
            const val ADMIN_ROLE = "Admin"
            const val HELPER_ROLE = "Helper"
            const val PRIORITY_HIGH_THRESHOLD = 2
        }
    }
