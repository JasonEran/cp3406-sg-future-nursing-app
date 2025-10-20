package com.example.sgfuturenursingapp.ui.data

import com.example.sgfuturenursingapp.ui.data.auth.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObject
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.comparisons.compareByDescending
import kotlin.comparisons.thenBy

@Singleton
class FirestoreTaskRepositoryImpl
    @Inject
    constructor(
        private val firestore: FirebaseFirestore,
        private val authRepository: AuthRepository,
    ) : TaskRepository {
        override fun getTasks(): Flow<List<Task>> =
            authRepository
                .authStateFlow()
                .flatMapLatest { user ->
                    val userId = user?.uid ?: return@flatMapLatest flowOf(emptyList())
                    tasksCollection(userId)
                        .orderBy(TASK_FIELD_ID, Query.Direction.ASCENDING)
                        .snapshotFlow()
                        .map { snapshot ->
                            snapshot.documents
                                .mapNotNull { document ->
                                    document.toObject<TaskFirestoreDto>()?.toDomain()
                                }.sortedWith(
                                    compareByDescending<Task> { it.priority }
                                        .thenBy { it.time },
                                )
                        }.distinctUntilChanged()
                }.flowOn(Dispatchers.IO)

        override suspend fun getTaskById(taskId: Int): Task? {
            val userId = requireUserId()
            val snapshot =
                tasksCollection(userId)
                    .document(taskId.toString())
                    .get()
                    .await()
            return snapshot.toObject<TaskFirestoreDto>()?.toDomain()
        }

        override suspend fun upsertTask(task: Task): Task {
            val userId = requireUserId()
            val taskToSave = task.copy(userId = userId)
            tasksCollection(userId)
                .document(taskToSave.id.toString())
                .set(taskToSave.toDto())
                .await()
            return taskToSave
        }

        override suspend fun completeTask(taskId: Int) {
            val userId = requireUserId()
            tasksCollection(userId)
                .document(taskId.toString())
                .update(TASK_FIELD_IS_COMPLETED, true)
                .await()
        }

        override suspend fun getNextTaskId(): Int {
            val userId = requireUserId()
            val snapshot =
                tasksCollection(userId)
                    .orderBy(TASK_FIELD_ID, Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .await()
            val currentMax =
                snapshot.documents
                    .firstOrNull()
                    ?.getLong(TASK_FIELD_ID)
                    ?.toInt() ?: 0
            return currentMax + 1
        }

        override suspend fun deleteTask(taskId: Int) {
            val userId = requireUserId()
            tasksCollection(userId)
                .document(taskId.toString())
                .delete()
                .await()
        }

        private suspend fun requireUserId(): String =
            authRepository.getCurrentUser()?.uid
                ?: error("No authenticated user found. Please log in to manage tasks.")

        private fun tasksCollection(userId: String) =
            firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(TASKS_COLLECTION)

        private data class TaskFirestoreDto(
            val id: Int? = null,
            val title: String = "",
            val time: String = "",
            val category: String = "",
            val iconName: String = "",
            val isCompleted: Boolean = false,
            val priority: Int = 0,
            val userId: String = "",
        ) {
            fun toDomain(): Task? =
                id?.let {
                    Task(
                        id = it,
                        title = title,
                        time = time,
                        category = category,
                        iconName = iconName,
                        isCompleted = isCompleted,
                        priority = priority,
                        userId = userId,
                    )
                }
        }

        private fun Task.toDto(): TaskFirestoreDto =
            TaskFirestoreDto(
                id = id,
                title = title,
                time = time,
                category = category,
                iconName = iconName,
                isCompleted = isCompleted,
                priority = priority,
                userId = userId,
            )

        private companion object {
            const val USERS_COLLECTION = "users"
            const val TASKS_COLLECTION = "tasks"
            const val TASK_FIELD_ID = "id"
            const val TASK_FIELD_IS_COMPLETED = "isCompleted"
        }

        private fun Query.snapshotFlow() = snapshots()
    }
