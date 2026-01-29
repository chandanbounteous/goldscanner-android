package com.kanishk.goldscanner.data.repository

import com.kanishk.goldscanner.domain.repository.AuthRepository
import com.kanishk.goldscanner.data.network.service.AuthApiService
import com.kanishk.goldscanner.data.network.ApiException
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
    private val localStorage: LocalStorage
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
            val refreshToken = localStorage.getString(LocalStorage.StorageKey.REFRESH_TOKEN)
                ?: return Result.Error(ErrorResponse("No refresh token found"))
            
            if (JWTTokenUtils.isTokenExpired(refreshToken)) {
                clearUserSession()
                return Result.Error(ErrorResponse("Refresh token expired"))
            }
            
            val response = authApiService.refreshToken(refreshToken)
            val newAccessToken = response["accessToken"]
            val newRefreshToken = response["refreshToken"]
            
            if (newAccessToken != null && newRefreshToken != null) {
                localStorage.storeString(LocalStorage.StorageKey.ACCESS_TOKEN, newAccessToken)
                localStorage.storeString(LocalStorage.StorageKey.REFRESH_TOKEN, newRefreshToken)
                Result.Success(true)
            } else {
                clearUserSession()
                Result.Error(ErrorResponse("Invalid token response"))
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
        val isLoggedIn = localStorage.getBoolean(LocalStorage.StorageKey.IS_LOGGED_IN) ?: false
        val accessToken = localStorage.getString(LocalStorage.StorageKey.ACCESS_TOKEN)
        
        if (!isLoggedIn || accessToken == null) {
            return false
        }
        
        // Check if access token is still valid
        if (JWTTokenUtils.isTokenExpired(accessToken)) {
            // Try to refresh token
            val refreshResult = refreshToken()
            return refreshResult is Result.Success
        }
        
        return true
    }
    
    override suspend fun getCurrentUser(): UserInfo? {
        return try {
            val userInfoJson = localStorage.getString(LocalStorage.StorageKey.USER_INFO)
            if (userInfoJson != null) {
                Json.decodeFromString<UserInfo>(userInfoJson)
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun saveUserSession(loginResponse: LoginResponse) {
        localStorage.storeString(LocalStorage.StorageKey.ACCESS_TOKEN, loginResponse.accessToken)
        localStorage.storeString(LocalStorage.StorageKey.REFRESH_TOKEN, loginResponse.refreshToken)
        localStorage.storeString(LocalStorage.StorageKey.USER_INFO, Json.encodeToString(loginResponse.user))
        localStorage.storeBoolean(LocalStorage.StorageKey.IS_LOGGED_IN, true)
    }
    
    override suspend fun clearUserSession() {
        localStorage.remove(LocalStorage.StorageKey.ACCESS_TOKEN)
        localStorage.remove(LocalStorage.StorageKey.REFRESH_TOKEN)
        localStorage.remove(LocalStorage.StorageKey.USER_INFO)
        localStorage.storeBoolean(LocalStorage.StorageKey.IS_LOGGED_IN, false)
    }
}