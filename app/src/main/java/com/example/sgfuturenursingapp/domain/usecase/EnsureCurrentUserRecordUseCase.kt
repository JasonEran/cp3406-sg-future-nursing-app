package com.example.sgfuturenursingapp.domain.usecase

import com.example.sgfuturenursingapp.ui.data.auth.AuthRepository
import javax.inject.Inject

class EnsureCurrentUserRecordUseCase
    @Inject
    constructor(
        private val authRepository: AuthRepository,
    ) {
        suspend operator fun invoke() {
            authRepository.ensureCurrentUserRecord()
        }
    }
