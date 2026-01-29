package com.kanishk.goldscanner.domain.usecase.auth

import com.kanishk.goldscanner.domain.repository.AuthRepository
import com.kanishk.goldscanner.data.model.response.LoginResponse
import com.kanishk.goldscanner.utils.Result

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<LoginResponse> {
        // Basic validation
        if (email.isBlank()) {
            return Result.Error(com.kanishk.goldscanner.data.model.response.ErrorResponse("Email cannot be empty"))
        }
        
        if (password.isBlank()) {
            return Result.Error(com.kanishk.goldscanner.data.model.response.ErrorResponse("Password cannot be empty"))
        }
        
        return authRepository.login(email, password)
    }
}