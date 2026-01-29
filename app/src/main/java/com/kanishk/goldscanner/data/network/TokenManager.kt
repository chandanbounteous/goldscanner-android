package com.kanishk.goldscanner.data.network

import com.kanishk.goldscanner.utils.LocalStorage
import com.kanishk.goldscanner.utils.JWTTokenUtils
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TokenManager(
    private val localStorage: LocalStorage,
    private val httpClient: HttpClient? = null
) {
    private val tokenMutex = Mutex()
    
    /**
     * Get valid access token following the centralized mechanism
     * Returns the token if valid or refreshes it if needed
     * Returns null if authentication fails and user should be redirected to login
     */
    suspend fun getValidAccessToken(): TokenResult {
        return tokenMutex.withLock {
            // Step 1 & 2: Fetch ACCESS_TOKEN from local storage
            val accessToken = localStorage.getString(LocalStorage.StorageKey.ACCESS_TOKEN)
            if (accessToken == null) {
                return@withLock TokenResult.Error("Session expired. Please login again.")
            }
            
            // Step 3 & 4: Check if access token is expired
            if (!JWTTokenUtils.isTokenExpired(accessToken)) {
                return@withLock TokenResult.Success(accessToken)
            }
            
            // Step 5 & 6: Access token expired, fetch REFRESH_TOKEN
            val refreshToken = localStorage.getString(LocalStorage.StorageKey.REFRESH_TOKEN)
            if (refreshToken == null) {
                return@withLock TokenResult.Error("Session expired. Please login again.")
            }
            
            // Step 7 & 8: Check if refresh token is expired
            if (JWTTokenUtils.isTokenExpired(refreshToken)) {
                clearAuthTokens()
                return@withLock TokenResult.Error("Session expired. Please login again.")
            }
            
            // Step 9: Refresh the access token
            return@withLock refreshAccessToken(refreshToken)
        }
    }
    
    /**
     * Refresh access token using refresh token
     */
    private suspend fun refreshAccessToken(refreshToken: String): TokenResult {
        return try {
            val client = httpClient ?: throw Exception("HTTP client not available")
            
            val response: HttpResponse = client.post("v1/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("refreshToken" to refreshToken))
            }
            
            if (response.status.isSuccess()) {
                val responseBody = response.body<Map<String, Any>>()
                val newAccessToken = responseBody["accessToken"] as? String
                val newRefreshToken = responseBody["refreshToken"] as? String
                
                if (newAccessToken != null && newRefreshToken != null) {
                    // Store new tokens
                    localStorage.storeString(LocalStorage.StorageKey.ACCESS_TOKEN, newAccessToken)
                    localStorage.storeString(LocalStorage.StorageKey.REFRESH_TOKEN, newRefreshToken)
                    
                    TokenResult.Success(newAccessToken)
                } else {
                    clearAuthTokens()
                    TokenResult.Error("Failed to refresh session. Please login again.")
                }
            } else {
                clearAuthTokens()
                TokenResult.Error("Session expired. Please login again.")
            }
        } catch (e: Exception) {
            clearAuthTokens()
            TokenResult.Error("Session expired. Please login again.")
        }
    }
    
    /**
     * Clear all authentication tokens
     */
    fun clearAuthTokens() {
        localStorage.remove(LocalStorage.StorageKey.ACCESS_TOKEN)
        localStorage.remove(LocalStorage.StorageKey.REFRESH_TOKEN)
        localStorage.storeBoolean(LocalStorage.StorageKey.IS_LOGGED_IN, false)
    }
    
    /**
     * Check if user is authenticated (has valid tokens)
     */
    suspend fun isAuthenticated(): Boolean {
        return when (getValidAccessToken()) {
            is TokenResult.Success -> true
            is TokenResult.Error -> false
        }
    }
}

sealed class TokenResult {
    data class Success(val token: String) : TokenResult()
    data class Error(val message: String) : TokenResult()
}