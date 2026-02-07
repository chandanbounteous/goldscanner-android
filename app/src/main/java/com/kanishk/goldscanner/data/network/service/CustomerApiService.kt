package com.kanishk.goldscanner.data.network.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import com.kanishk.goldscanner.data.model.response.CustomerListResponse
import com.kanishk.goldscanner.data.network.ApiException
import com.kanishk.goldscanner.data.network.NetworkConfig

class CustomerApiService(
    private val networkConfig: NetworkConfig
) {
    
    suspend fun getCustomerList(
        query: String?
    ): CustomerListResponse {
        return try {
            // Check if authentication is needed and get token
            val tokenManager = networkConfig.getTokenManager()
            val tokenResult = tokenManager.getValidAccessToken()
            
            val response: HttpResponse = when (tokenResult) {
                is com.kanishk.goldscanner.data.network.TokenResult.Success -> {
                    networkConfig.client.get("v1/customer/list") {
                        headers {
                            append(HttpHeaders.Authorization, "Bearer ${tokenResult.token}")
                        }
                        
                        // Add query parameter if provided
                        if (!query.isNullOrEmpty()) {
                            parameter("query", query)
                        }
                    }
                }
                is com.kanishk.goldscanner.data.network.TokenResult.Error -> {
                    throw com.kanishk.goldscanner.data.network.AuthenticationException(tokenResult.message)
                }
            }
            
            if (response.status.isSuccess()) {
                val customerListResponse: CustomerListResponse = response.body()
                
                if (customerListResponse.responseCode in 200..299) {
                    customerListResponse
                } else {
                    throw ApiException.ClientError(
                        code = customerListResponse.responseCode,
                        message = customerListResponse.responseMessage,
                        body = customerListResponse.responseMessage
                    )
                }
            } else {
                val errorMessage = try {
                    val errorBody: com.kanishk.goldscanner.data.model.response.ErrorBody = response.body()
                    errorBody.message ?: "Unknown error"
                } catch (e: Exception) {
                    "HTTP ${response.status.value}: ${response.status.description}"
                }
                
                when (response.status.value) {
                    in 400..499 -> throw ApiException.ClientError(
                        code = response.status.value,
                        message = errorMessage,
                        body = errorMessage
                    )
                    in 500..599 -> throw ApiException.ServerError(
                        code = response.status.value,
                        message = errorMessage,
                        body = errorMessage
                    )
                    else -> throw ApiException.NetworkError("Unexpected status code: ${response.status.value}")
                }
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