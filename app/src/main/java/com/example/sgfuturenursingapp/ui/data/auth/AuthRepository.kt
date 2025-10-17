package com.example.sgfuturenursingapp.ui.data.auth

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun register(email: String, password: String): Result<FirebaseUser?>
    suspend fun login(email: String, password: String): Result<FirebaseUser?>
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): FirebaseUser?
    fun authStateFlow(): Flow<FirebaseUser?>
}

