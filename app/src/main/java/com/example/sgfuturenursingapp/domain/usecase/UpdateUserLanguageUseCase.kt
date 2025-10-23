package com.example.sgfuturenursingapp.domain.usecase

import com.example.sgfuturenursingapp.ui.data.auth.AuthRepository
import javax.inject.Inject

class UpdateUserLanguageUseCase
    @Inject
    constructor(
        private val authRepository: AuthRepository,
    ) {
        suspend operator fun invoke(languageCode: String): Result<Unit> =
            authRepository.updateUserLanguage(languageCode)
    }
