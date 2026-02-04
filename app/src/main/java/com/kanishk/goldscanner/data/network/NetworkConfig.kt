package com.kanishk.goldscanner.data.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import com.kanishk.goldscanner.utils.LocalStorage
import com.kanishk.goldscanner.data.model.response.ApiResponse
import com.kanishk.goldscanner.data.model.response.ErrorBody

class NetworkConfig(
    private val localStorage: LocalStorage
) {
    
    companion object {
        private const val BASE_URL = "http://192.168.1.4:3000/api/"
        private const val CONNECTION_TIMEOUT = 30000L
        private const val REQUEST_TIMEOUT = 30000L
        
        // Endpoints that don't require authentication
        private val NO_AUTH_ENDPOINTS = setOf("health", "v1/auth/login")
    }
    
    private lateinit var tokenManager: TokenManager
    
    val client: HttpClient by lazy {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
            
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.BODY
            }
            
            install(HttpTimeout) {
                connectTimeoutMillis = CONNECTION_TIMEOUT
                requestTimeoutMillis = REQUEST_TIMEOUT
                socketTimeoutMillis = REQUEST_TIMEOUT
            }
            
            defaultRequest {
                url(BASE_URL)
                contentType(ContentType.Application.Json)
            }
        }.also { client ->
            tokenManager = TokenManager(localStorage, client)
        }
    }
    
    suspend fun executeWithAuth(block: suspend HttpClient.() -> HttpResponse): HttpResponse {
        val url = "" // This will be set by the calling service
        val needsAuth = NO_AUTH_ENDPOINTS.none { endpoint -> 
            url.endsWith(endpoint) || url.contains("/$endpoint")
        }
        
        return if (needsAuth) {
            when (val tokenResult = tokenManager.getValidAccessToken()) {
                is TokenResult.Success -> {
                    client.block()
                }
                is TokenResult.Error -> {
                    throw AuthenticationException(tokenResult.message)
                }
            }
        } else {
            client.block()
        }
    }
    
    /**
     * Get token manager instance for authentication operations
     */
    fun getTokenManager(): TokenManager = tokenManager
    
    private fun clearAuthTokens() {
        localStorage.remove(LocalStorage.StorageKey.ACCESS_TOKEN)
        localStorage.remove(LocalStorage.StorageKey.REFRESH_TOKEN)
        localStorage.save(LocalStorage.StorageKey.IS_LOGGED_IN, false)
    }
}

sealed class ApiException(message: String) : Exception(message) {
    data class ClientError(val code: Int, val body: String, override val message: String) : ApiException(message)
    data class ServerError(val code: Int, val body: String, override val message: String) : ApiException(message)
    data class NetworkError(override val message: String) : ApiException(message)
}

class AuthenticationException(message: String) : Exception(message)