package com.kanishk.goldscanner.data.network.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import com.kanishk.goldscanner.data.model.response.GoldRateResponse
import com.kanishk.goldscanner.data.model.response.ApiResponse
import com.kanishk.goldscanner.data.model.response.ErrorBody
import com.kanishk.goldscanner.data.network.ApiException
import com.kanishk.goldscanner.data.network.NetworkConfig

class GoldRateApiService(
    private val networkConfig: NetworkConfig
) {
    
    suspend fun getCurrentGoldRate(): GoldRateResponse {
        return try {
            // Check if authentication is needed and get token
            val tokenManager = networkConfig.getTokenManager()
            val tokenResult = tokenManager.getValidAccessToken()
            
            val response: HttpResponse = when (tokenResult) {
                is com.kanishk.goldscanner.data.network.TokenResult.Success -> {
                    networkConfig.client.get("v1/gold/currentrate") {
                        headers {
                            append(HttpHeaders.Authorization, "Bearer ${tokenResult.token}")
                        }
                    }
                }
                is com.kanishk.goldscanner.data.network.TokenResult.Error -> {
                    throw com.kanishk.goldscanner.data.network.AuthenticationException(tokenResult.message)
                }
            }
            
            if (response.status.isSuccess()) {
                val apiResponse: ApiResponse<GoldRateResponse> = response.body()
                
                if (apiResponse.responseCode in 200..299) {
                    apiResponse.body
                } else {
                    throw ApiException.ClientError(
                        code = apiResponse.responseCode,
                        message = apiResponse.responseMessage,
                        body = apiResponse.responseMessage
                    )
                }
            } else {
                val errorResponse: ApiResponse<ErrorBody> = response.body()
                throw ApiException.ClientError(
                    code = errorResponse.responseCode,
                    message = errorResponse.responseMessage,
                    body = errorResponse.body.errors?.joinToString(", ") { it.message } ?: errorResponse.responseMessage
                )
            }
        } catch (e: com.kanishk.goldscanner.data.network.AuthenticationException) {
            throw e
        } catch (e: ApiException) {
            throw e
        } catch (e: Exception) {
            throw ApiException.NetworkError(e.message ?: "Network error occurred")
        }
    }
}