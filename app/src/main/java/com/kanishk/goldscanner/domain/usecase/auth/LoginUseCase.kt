package com.kanishk.goldscanner.domain.usecase.auth

import com.kanishk.goldscanner.domain.repository.AuthRepository
import com.kanishk.goldscanner.data.model.response.LoginResponse
import com.kanishk.goldscanner.data.model.response.Result

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<LoginResponse> {
        // Basic validation
        if (email.isBlank()) {
            return Result.Error(com.kanishk.goldscanner.data.model.response.ErrorResponse(responseMessage = "Email cannot be empty", message = "Email cannot be empty"))
        }
        
        if (password.isBlank()) {
            return Result.Error(com.kanishk.goldscanner.data.model.response.ErrorResponse(responseMessage = "Password cannot be empty", message = "Password cannot be empty"))
        }
        
        return authRepository.login(email, password)
    }
}