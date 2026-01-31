package com.kanishk.goldscanner.data.repository

import com.kanishk.goldscanner.domain.repository.AuthRepository
import com.kanishk.goldscanner.data.network.service.AuthApiService
import com.kanishk.goldscanner.data.network.ApiException
import com.kanishk.goldscanner.data.network.NetworkConfig
import com.kanishk.goldscanner.data.model.request.LoginRequest
import com.kanishk.goldscanner.data.model.response.LoginResponse
import com.kanishk.goldscanner.data.model.response.UserInfo
import com.kanishk.goldscanner.data.model.response.ErrorResponse
import com.kanishk.goldscanner.utils.LocalStorage
import com.kanishk.goldscanner.utils.JWTTokenUtils
import com.kanishk.goldscanner.utils.Result
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class AuthRepositoryImpl(
    private val authApiService: AuthApiService,
    private val localStorage: LocalStorage,
    private val networkConfig: NetworkConfig
) : AuthRepository {
    
    override suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val request = LoginRequest(username, password)
            val response = authApiService.login(request)
            
            // Save session locally
            saveUserSession(response)
            
            Result.Success(response)
        } catch (e: ApiException.ClientError) {
            Result.Error(ErrorResponse(
                message = "Invalid credentials",
                code = e.code,
                details = e.body
            ))
        } catch (e: ApiException.ServerError) {
            Result.Error(ErrorResponse(
                message = "Server error. Please try again later.",
                code = e.code,
                details = e.body
            ))
        } catch (e: ApiException.NetworkError) {
            Result.Error(ErrorResponse(
                message = "Network error. Please check your connection.",
                details = e.message
            ))
        } catch (e: Exception) {
            Result.Error(ErrorResponse(
                message = "An unexpected error occurred",
                details = e.message
            ))
        }
    }
    
    override suspend fun logout(): Result<Boolean> {
        return try {
            authApiService.logout()
            clearUserSession()
            Result.Success(true)
        } catch (e: Exception) {
            // Clear session even if API call fails
            clearUserSession()
            Result.Success(true)
        }
    }
    
    override suspend fun refreshToken(): Result<Boolean> {
        return try {
            // Use the centralized TokenManager for token refresh
            val tokenManager = networkConfig.getTokenManager()
            when (val tokenResult = tokenManager.getValidAccessToken()) {
                is com.kanishk.goldscanner.data.network.TokenResult.Success -> {
                    // Token is valid (either existing or refreshed)
                    Result.Success(true)
                }
                is com.kanishk.goldscanner.data.network.TokenResult.Error -> {
                    // Token refresh failed
                    clearUserSession()
                    Result.Error(ErrorResponse(
                        message = "Token refresh failed",
                        details = tokenResult.message
                    ))
                }
            }
        } catch (e: Exception) {
            clearUserSession()
            Result.Error(ErrorResponse(
                message = "Token refresh failed",
                details = e.message
            ))
        }
    }
    
    override suspend fun isUserLoggedIn(): Boolean {
        // Use the centralized TokenManager to check authentication status
        return networkConfig.getTokenManager().isAuthenticated()
    }
    
    override suspend fun getCurrentUser(): UserInfo? {
        return try {
            val userInfoJson = localStorage.getValue<String>(LocalStorage.StorageKey.USER_INFO)
            if (userInfoJson != null) {
                Json.decodeFromString<UserInfo>(userInfoJson)
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun saveUserSession(loginResponse: LoginResponse) {
        localStorage.save(LocalStorage.StorageKey.ACCESS_TOKEN, loginResponse.accessToken)
        localStorage.save(LocalStorage.StorageKey.REFRESH_TOKEN, loginResponse.refreshToken)
        localStorage.save(LocalStorage.StorageKey.USER_INFO, Json.encodeToString(loginResponse.user))
        localStorage.save(LocalStorage.StorageKey.IS_LOGGED_IN, true)
    }
    
    override suspend fun clearUserSession() {
        localStorage.remove(LocalStorage.StorageKey.ACCESS_TOKEN)
        localStorage.remove(LocalStorage.StorageKey.REFRESH_TOKEN)
        localStorage.remove(LocalStorage.StorageKey.USER_INFO)
        localStorage.save(LocalStorage.StorageKey.IS_LOGGED_IN, false)
    }
}