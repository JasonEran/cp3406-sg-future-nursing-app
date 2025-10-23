package com.example.sgfuturenursingapp.ui.data.auth

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun register(
        email: String,
        password: String,
        preferredLanguage: String,
    ): Result<FirebaseUser?>
    suspend fun login(email: String, password: String): Result<FirebaseUser?>
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): FirebaseUser?
    fun authStateFlow(): Flow<FirebaseUser?>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    fun getCurrentUserRole(): String?
    fun getRoleForEmail(email: String): String
    suspend fun ensureCurrentUserRecord()
    suspend fun updateUserLanguage(language: String): Result<Unit>
    suspend fun getCurrentUserProfile(): com.example.sgfuturenursingapp.ui.data.User?
}
