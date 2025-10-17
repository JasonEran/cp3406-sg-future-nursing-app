package com.example.sgfuturenursingapp.ui.data.auth

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class FirebaseAuthRepositoryImpl
    @Inject
    constructor(
        private val firebaseAuth: FirebaseAuth,
    ) : AuthRepository {
        override suspend fun register(email: String, password: String): Result<FirebaseUser?> =
            runCatching {
                firebaseAuth
                    .createUserWithEmailAndPassword(email, password)
                    .awaitResult()
                    .user
            }

        override suspend fun login(email: String, password: String): Result<FirebaseUser?> =
            runCatching {
                firebaseAuth
                    .signInWithEmailAndPassword(email, password)
                    .awaitResult()
                    .user
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
    }

