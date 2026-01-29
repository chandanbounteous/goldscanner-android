package com.kanishk.goldscanner.domain.repository

import com.kanishk.goldscanner.data.model.response.LoginResponse
import com.kanishk.goldscanner.data.model.response.UserInfo
import com.kanishk.goldscanner.utils.Result

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<LoginResponse>
    suspend fun logout(): Result<Boolean>
    suspend fun refreshToken(): Result<Boolean>
    suspend fun isUserLoggedIn(): Boolean
    suspend fun getCurrentUser(): UserInfo?
    suspend fun saveUserSession(loginResponse: LoginResponse)
    suspend fun clearUserSession()
}