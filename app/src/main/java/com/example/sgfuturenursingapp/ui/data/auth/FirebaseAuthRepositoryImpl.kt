package com.example.sgfuturenursingapp.ui.data.auth

import com.example.sgfuturenursingapp.ui.data.CareGroup
import com.example.sgfuturenursingapp.ui.data.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class FirebaseAuthRepositoryImpl
    @Inject
    constructor(
        private val firebaseAuth: FirebaseAuth,
        private val firestore: FirebaseFirestore,
    ) : AuthRepository {
        override suspend fun register(email: String, password: String): Result<FirebaseUser?> =
            runCatching {
                firebaseAuth
                    .createUserWithEmailAndPassword(email, password)
                    .awaitResult()
                    .user
                    ?.also { ensureUserRecord(it) }
            }

        override suspend fun login(email: String, password: String): Result<FirebaseUser?> =
            runCatching {
                firebaseAuth
                    .signInWithEmailAndPassword(email, password)
                    .awaitResult()
                    .user
                    ?.also { ensureUserRecord(it) }
            }

        override suspend fun logout(): Result<Unit> =
            runCatching {
                firebaseAuth.signOut()
            }

        override suspend fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

        override fun authStateFlow(): Flow<FirebaseUser?> =
            callbackFlow {
                trySend(firebaseAuth.currentUser).isSuccess
                val listener =
                    FirebaseAuth.AuthStateListener { auth ->
                        trySend(auth.currentUser).isSuccess
                    }
                firebaseAuth.addAuthStateListener(listener)

                awaitClose {
                    firebaseAuth.removeAuthStateListener(listener)
                }
            }

        override suspend fun sendPasswordResetEmail(email: String): Result<Unit> =
            runCatching {
                firebaseAuth
                    .sendPasswordResetEmail(email)
                    .awaitResult()
                Unit
            }

        override fun getCurrentUserRole(): String? =
            firebaseAuth.currentUser?.email?.let { determineRole(it) }

        override fun getRoleForEmail(email: String): String = determineRole(email)

        private fun determineRole(email: String): String =
            when {
                email.contains("admin", ignoreCase = true) -> "Admin"
                email.contains("primary", ignoreCase = true) -> "Primary Caregiver"
                else -> "Helper"
            }

        private suspend fun ensureUserRecord(user: FirebaseUser) {
            val email = user.email.orEmpty()
            val role = determineRole(email)
            val userRef = firestore.collection(USERS_COLLECTION).document(user.uid)
            val userSnapshot = userRef.get().await()
            var careGroupId = userSnapshot.toObject(User::class.java)?.careGroupId

            if (role == ADMIN_ROLE) {
                val resolvedCareGroupId = careGroupId?.takeIf { it.isNotBlank() } ?: user.uid
                val careGroupRef =
                    firestore
                        .collection(CARE_GROUPS_COLLECTION)
                        .document(resolvedCareGroupId)
                val careGroupSnapshot = careGroupRef.get().await()
                if (!careGroupSnapshot.exists()) {
                    val defaultGroupName =
                        if (email.isNotBlank()) {
                            "$email Care Group"
                        } else {
                            "Admin Care Group"
                        }
                    val careGroup =
                        CareGroup(
                            groupId = resolvedCareGroupId,
                            groupName = defaultGroupName,
                            adminUid = user.uid,
                            members = mapOf(user.uid to ADMIN_ROLE),
                        )
                    careGroupRef.set(careGroup).await()
                }

                careGroupId = resolvedCareGroupId
            }

            val userRecord =
                User(
                    uid = user.uid,
                    email = email,
                    role = role,
                    careGroupId = careGroupId,
                )

            userRef.set(userRecord, SetOptions.merge()).await()
        }

        private suspend fun <T> Task<T>.awaitResult(): T =
            suspendCancellableCoroutine { continuation ->
                addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(task.result)
                    } else {
                        val exception =
                            task.exception ?: IllegalStateException("Unknown FirebaseAuth error")
                        continuation.resumeWithException(exception)
                    }
                }
            }

        private companion object {
            const val USERS_COLLECTION = "users"
            const val CARE_GROUPS_COLLECTION = "care_groups"
            const val ADMIN_ROLE = "Admin"
        }
    }
