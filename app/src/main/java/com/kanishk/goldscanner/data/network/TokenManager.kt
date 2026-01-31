package com.kanishk.goldscanner.data.network

import android.util.Log
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
    
    companion object {
        private const val TAG = "TokenManager"
    }
    
    /**
     * Get valid access token following the centralized mechanism
     * Returns the token if valid or refreshes it if needed
     * Returns null if authentication fails and user should be redirected to login
     */
    suspend fun getValidAccessToken(): TokenResult {
        return tokenMutex.withLock {
            // Step 1 & 2: Fetch ACCESS_TOKEN from local storage
            val accessToken = localStorage.getValue<String>(LocalStorage.StorageKey.ACCESS_TOKEN)
            if (accessToken == null) {
                return@withLock TokenResult.Error("Session expired. Please login again.")
            }
            
            // Step 3 & 4: Check if access token is expired
            if (!JWTTokenUtils.isTokenExpired(accessToken)) {
                return@withLock TokenResult.Success(accessToken)
            }
            
            // Step 5 & 6: Access token expired, fetch REFRESH_TOKEN
            val refreshToken = localStorage.getValue<String>(LocalStorage.StorageKey.REFRESH_TOKEN)
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
        Log.d(TAG, "refreshAccessToken: Starting token refresh process")
        
        return try {
            val client = httpClient ?: run {
                Log.e(TAG, "refreshAccessToken: HTTP client not available")
                throw Exception("HTTP client not available")
            }
            
            Log.d(TAG, "refreshAccessToken: Making POST request to v1/auth/refresh")
            Log.d(TAG, "refreshAccessToken: Refresh token length: ${refreshToken.length}")
            
            val response: HttpResponse = client.post("v1/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("refreshToken" to refreshToken))
            }
            
            Log.d(TAG, "refreshAccessToken: Response status: ${response.status}")
            Log.d(TAG, "refreshAccessToken: Response headers: ${response.headers}")
            
            if (response.status.isSuccess()) {
                Log.d(TAG, "refreshAccessToken: Success response received, parsing body")
                
                try {
                    // Try to parse as ApiResponse<RefreshTokenResponse> first (standard API format)
                    val apiResponse: com.kanishk.goldscanner.data.model.response.ApiResponse<com.kanishk.goldscanner.data.model.response.RefreshTokenResponse> = response.body()
                    Log.d(TAG, "refreshAccessToken: API response parsed, responseCode: ${apiResponse.responseCode}")
                    
                    if (apiResponse.responseCode in 200..299) {
                        val refreshResponse = apiResponse.body
                        Log.d(TAG, "refreshAccessToken: New access token present: true")
                        Log.d(TAG, "refreshAccessToken: Refresh token remains the same")
                        Log.d(TAG, "refreshAccessToken: Storing new access token in local storage")
                        
                        // Store new access token (refresh token stays the same)
                        localStorage.save(LocalStorage.StorageKey.ACCESS_TOKEN, refreshResponse.accessToken)
                        
                        Log.d(TAG, "refreshAccessToken: Token refresh successful")
                        TokenResult.Success(refreshResponse.accessToken)
                    } else {
                        Log.w(TAG, "refreshAccessToken: API returned error code: ${apiResponse.responseCode}")
                        clearAuthTokens()
                        TokenResult.Error("Session expired. Please login again.")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "refreshAccessToken: Failed to parse as ApiResponse, trying fallback", e)
                    // Fallback: try to parse as direct Map (for backward compatibility)
                    try {
                        val responseBody = response.body<Map<String, String>>()
                        Log.d(TAG, "refreshAccessToken: Fallback parsing successful, response keys: ${responseBody.keys}")
                        
                        val newAccessToken = responseBody["accessToken"]
                        val newRefreshToken = responseBody["refreshToken"] // This might not exist
                        
                        Log.d(TAG, "refreshAccessToken: New access token present: ${newAccessToken != null}")
                        Log.d(TAG, "refreshAccessToken: New refresh token present: ${newRefreshToken != null}")
                        
                        if (newAccessToken != null) {
                            Log.d(TAG, "refreshAccessToken: Storing new access token in local storage")
                            
                            // Store new access token (refresh token might not change)
                            localStorage.save(LocalStorage.StorageKey.ACCESS_TOKEN, newAccessToken)
                            if (newRefreshToken != null) {
                                localStorage.save(LocalStorage.StorageKey.REFRESH_TOKEN, newRefreshToken)
                            }
                            
                            Log.d(TAG, "refreshAccessToken: Token refresh successful")
                            TokenResult.Success(newAccessToken)
                        } else {
                            Log.w(TAG, "refreshAccessToken: Access token missing in fallback response, clearing auth")
                            clearAuthTokens()
                            TokenResult.Error("Failed to refresh session. Please login again.")
                        }
                    } catch (fallbackException: Exception) {
                        Log.e(TAG, "refreshAccessToken: Both parsing attempts failed", fallbackException)
                        clearAuthTokens()
                        TokenResult.Error("Failed to refresh session. Please login again.")
                    }
                }
            } else {
                Log.w(TAG, "refreshAccessToken: Error response received: ${response.status}")
                clearAuthTokens()
                TokenResult.Error("Session expired. Please login again.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "refreshAccessToken: Exception occurred", e)
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
        localStorage.save(LocalStorage.StorageKey.IS_LOGGED_IN, false)
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