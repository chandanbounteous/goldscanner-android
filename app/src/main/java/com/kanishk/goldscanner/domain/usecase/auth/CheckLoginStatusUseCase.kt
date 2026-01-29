package com.kanishk.goldscanner.domain.usecase.auth

import com.kanishk.goldscanner.domain.repository.AuthRepository

class CheckLoginStatusUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Boolean {
        return authRepository.isUserLoggedIn()
    }
}