package com.example.sgfuturenursingapp.domain.usecase

import com.example.sgfuturenursingapp.ui.data.auth.AuthRepository
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class RegisterUserUseCase
    @Inject
    constructor(
        private val authRepository: AuthRepository,
    ) {
        suspend operator fun invoke(
            email: String,
            password: String,
            preferredLanguage: String,
        ): Result<FirebaseUser?> = authRepository.register(email, password, preferredLanguage)
    }
