package com.kanishk.goldscanner.data.network.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import com.kanishk.goldscanner.data.model.request.LoginRequest
import com.kanishk.goldscanner.data.model.response.LoginResponse
import com.kanishk.goldscanner.data.model.response.ApiResponse
import com.kanishk.goldscanner.data.model.response.ErrorBody
import com.kanishk.goldscanner.data.network.ApiException

class AuthApiService(
    private val client: HttpClient
) {
    
    suspend fun login(loginRequest: LoginRequest): LoginResponse {
        return try {
            val response: HttpResponse = client.post("v1/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(loginRequest)
            }
            
            // Always parse as ApiResponse first to check responseCode
            if (response.status.isSuccess()) {
                val apiResponse: ApiResponse<LoginResponse> = response.body()
                
                // Check the responseCode in the JSON body
                if (apiResponse.responseCode in 200..299) {
                    apiResponse.body
                } else {
                    // API returned success HTTP status but error responseCode
                    throw ApiException.ClientError(
                        code = apiResponse.responseCode,
                        message = apiResponse.responseMessage,
                        body = apiResponse.responseMessage
                    )
                }
            } else {
                // HTTP error status - parse as error response
                val errorResponse: ApiResponse<ErrorBody> = response.body()
                throw ApiException.ClientError(
                    code = errorResponse.responseCode,
                    message = errorResponse.responseMessage,
                    body = errorResponse.body.errors?.joinToString(", ") { it.message } ?: errorResponse.responseMessage
                )
            }
        } catch (e: ApiException) {
            throw e
        } catch (e: Exception) {
            throw when (e) {
                is ApiException -> e
                else -> ApiException.NetworkError(e.message ?: "Login failed")
            }
        }
    }
    
    suspend fun refreshToken(refreshToken: String): Map<String, String> {
        return try {
            client.post("auth/refresh") {
                setBody(mapOf("refreshToken" to refreshToken))
            }.body()
        } catch (e: Exception) {
            throw when (e) {
                is ApiException -> e
                else -> ApiException.NetworkError(e.message ?: "Token refresh failed")
            }
        }
    }
    
    suspend fun logout(): Boolean {
        return try {
            client.post("auth/logout").status.value in 200..299
        } catch (e: Exception) {
            // Even if logout fails on server, we consider it successful locally
            true
        }
    }
}