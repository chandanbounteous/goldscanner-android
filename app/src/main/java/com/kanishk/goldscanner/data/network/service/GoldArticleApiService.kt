package com.kanishk.goldscanner.data.network.service

import com.kanishk.goldscanner.data.model.request.CreateArticleRequest
import com.kanishk.goldscanner.data.model.response.GoldArticleResponse
import com.kanishk.goldscanner.data.model.response.ApiResponse
import com.kanishk.goldscanner.data.model.response.ErrorBody
import com.kanishk.goldscanner.data.network.ApiException
import com.kanishk.goldscanner.data.network.NetworkConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class GoldArticleApiService(
    private val networkConfig: NetworkConfig
) {
    
    suspend fun createArticle(request: CreateArticleRequest): GoldArticleResponse {
        return try {
            // Check if authentication is needed and get token
            val tokenManager = networkConfig.getTokenManager()
            val tokenResult = tokenManager.getValidAccessToken()
            
            val response: HttpResponse = when (tokenResult) {
                is com.kanishk.goldscanner.data.network.TokenResult.Success -> {
                    networkConfig.client.post("v1/gold/article") {
                        headers {
                            append(HttpHeaders.Authorization, "Bearer ${tokenResult.token}")
                        }
                        contentType(ContentType.Application.Json)
                        setBody(request)
                    }
                }
                is com.kanishk.goldscanner.data.network.TokenResult.Error -> {
                    throw com.kanishk.goldscanner.data.network.AuthenticationException(tokenResult.message)
                }
            }
            
            if (response.status.isSuccess()) {
                val goldArticleResponse: GoldArticleResponse = response.body()
                
                if (goldArticleResponse.responseCode in 200..299) {
                    goldArticleResponse
                } else {
                    throw ApiException.ClientError(
                        code = goldArticleResponse.responseCode,
                        message = goldArticleResponse.responseMessage,
                        body = goldArticleResponse.responseMessage
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