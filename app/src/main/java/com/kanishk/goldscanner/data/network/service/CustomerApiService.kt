package com.kanishk.goldscanner.data.network.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import com.kanishk.goldscanner.data.model.response.CustomerListResponse
import com.kanishk.goldscanner.data.model.response.CreateCustomerResponse
import com.kanishk.goldscanner.data.model.response.BasketSearchResponse
import com.kanishk.goldscanner.data.model.response.AddArticleToBasketResponse
import com.kanishk.goldscanner.data.model.response.BasketDetailResponse
import com.kanishk.goldscanner.data.model.request.CreateCustomerRequest
import com.kanishk.goldscanner.data.model.request.BasketSearchRequest
import com.kanishk.goldscanner.data.model.request.AddArticleToBasketRequest
import com.kanishk.goldscanner.data.model.CreateBasketRequest
import com.kanishk.goldscanner.data.model.CreateBasketResponse
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
    
    suspend fun createCustomer(request: CreateCustomerRequest): CreateCustomerResponse {
        return try {
            // Check if authentication is needed and get token
            val tokenManager = networkConfig.getTokenManager()
            val tokenResult = tokenManager.getValidAccessToken()
            
            val response: HttpResponse = when (tokenResult) {
                is com.kanishk.goldscanner.data.network.TokenResult.Success -> {
                    networkConfig.client.post("v1/customer/create") {
                        headers {
                            append(HttpHeaders.Authorization, "Bearer ${tokenResult.token}")
                            append(HttpHeaders.ContentType, "application/json")
                        }
                        setBody(request)
                    }
                }
                is com.kanishk.goldscanner.data.network.TokenResult.Error -> {
                    throw com.kanishk.goldscanner.data.network.AuthenticationException(tokenResult.message)
                }
            }
            
            if (response.status.isSuccess()) {
                val createCustomerResponse: CreateCustomerResponse = response.body()
                
                if (createCustomerResponse.responseCode in 200..299) {
                    createCustomerResponse
                } else {
                    throw ApiException.ClientError(
                        code = createCustomerResponse.responseCode,
                        message = createCustomerResponse.responseMessage,
                        body = createCustomerResponse.responseMessage
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

    suspend fun searchBaskets(
        searchRequest: BasketSearchRequest
    ): BasketSearchResponse {
        return try {
            // Check if authentication is needed and get token
            val tokenManager = networkConfig.getTokenManager()
            val tokenResult = tokenManager.getValidAccessToken()
            
            val response: HttpResponse = when (tokenResult) {
                is com.kanishk.goldscanner.data.network.TokenResult.Success -> {
                    networkConfig.client.post("v1/customer/basket/search") {
                        headers {
                            append(HttpHeaders.Authorization, "Bearer ${tokenResult.token}")
                            append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        }
                        setBody(searchRequest)
                    }
                }
                is com.kanishk.goldscanner.data.network.TokenResult.Error -> {
                    throw com.kanishk.goldscanner.data.network.AuthenticationException(tokenResult.message)
                }
            }

            val errorMessage = try {
                val errorBody: com.kanishk.goldscanner.data.model.response.ErrorBody = response.body()
                errorBody.message ?: "Unknown error"
            } catch (e: Exception) {
                "HTTP ${response.status.value}: ${response.status.description}"
            }

            when (response.status.value) {
                200 -> {
                    response.body<BasketSearchResponse>()
                }
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
        } catch (e: com.kanishk.goldscanner.data.network.AuthenticationException) {
            throw e
        } catch (e: ApiException) {
            throw e
        } catch (e: Exception) {
            throw ApiException.NetworkError(e.message ?: "Network error occurred")
        }
    }
    
    suspend fun createBasket(customerId: String, request: CreateBasketRequest): CreateBasketResponse {
        return try {
            // Check if authentication is needed and get token
            val tokenManager = networkConfig.getTokenManager()
            val tokenResult = tokenManager.getValidAccessToken()
            
            val response: HttpResponse = when (tokenResult) {
                is com.kanishk.goldscanner.data.network.TokenResult.Success -> {
                    networkConfig.client.post("v1/customer/$customerId/basket") {
                        contentType(ContentType.Application.Json)
                        headers {
                            append(HttpHeaders.Authorization, "Bearer ${tokenResult.token}")
                        }
                        setBody(request)
                    }
                }
                is com.kanishk.goldscanner.data.network.TokenResult.Error -> {
                    throw com.kanishk.goldscanner.data.network.AuthenticationException(tokenResult.message)
                }
            }
            
            if (response.status.isSuccess()) {
                response.body<CreateBasketResponse>()
            } else {
                val errorBody = try {
                    response.bodyAsText()
                } catch (e: Exception) {
                    null
                }
                throw ApiException.ClientError(response.status.value, response.status.description, errorBody!!)
            }
        } catch (e: com.kanishk.goldscanner.data.network.AuthenticationException) {
            throw e
        } catch (e: ApiException) {
            throw e
        } catch (e: Exception) {
            throw ApiException.NetworkError(e.message ?: "Network error occurred")
        }
    }
    
    suspend fun addArticleToBasket(
        basketId: String,
        request: AddArticleToBasketRequest
    ): AddArticleToBasketResponse {
        return try {
            // Check if authentication is needed and get token
            val tokenManager = networkConfig.getTokenManager()
            val tokenResult = tokenManager.getValidAccessToken()
            
            val response: HttpResponse = when (tokenResult) {
                is com.kanishk.goldscanner.data.network.TokenResult.Success -> {
                    networkConfig.client.post("v1/customer/basket/$basketId/article") {
                        headers {
                            append(HttpHeaders.Authorization, "Bearer ${tokenResult.token}")
                            append(HttpHeaders.ContentType, "application/json")
                        }
                        setBody(request)
                    }
                }
                is com.kanishk.goldscanner.data.network.TokenResult.Error -> {
                    throw com.kanishk.goldscanner.data.network.AuthenticationException(tokenResult.message)
                }
            }
            
            if (response.status.isSuccess()) {
                val addArticleResponse: AddArticleToBasketResponse = response.body()
                
                if (addArticleResponse.responseCode in 200..299) {
                    addArticleResponse
                } else {
                    throw ApiException.ClientError(
                        code = addArticleResponse.responseCode,
                        message = addArticleResponse.responseMessage,
                        body = addArticleResponse.responseMessage
                    )
                }
            } else {
                when (response.status.value) {
                    401 -> {
                        throw com.kanishk.goldscanner.data.network.AuthenticationException("Unauthorized access")
                    }
                    in 400..499 -> {
                        val errorBody = response.bodyAsText()
                        throw ApiException.ClientError(
                            code = response.status.value,
                            message = "Client error: ${response.status.description}",
                            body = errorBody
                        )
                    }
                    in 500..599 -> {
                        throw ApiException.ServerError(
                            code = response.status.value,
                            message = "Server error: ${response.status.description}",
                            body = response.bodyAsText()
                        )
                    }
                    else -> {
                        throw ApiException.NetworkError("HTTP error: ${response.status}")
                    }
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
    
    suspend fun getBasketDetails(basketId: String): BasketDetailResponse {
        return try {
            // Check if authentication is needed and get token
            val tokenManager = networkConfig.getTokenManager()
            val tokenResult = tokenManager.getValidAccessToken()
            
            val response: HttpResponse = when (tokenResult) {
                is com.kanishk.goldscanner.data.network.TokenResult.Success -> {
                    networkConfig.client.get("v1/customer/basket/$basketId") {
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
                val basketDetailResponse: BasketDetailResponse = response.body()
                
                if (basketDetailResponse.responseCode in 200..299) {
                    basketDetailResponse
                } else {
                    throw ApiException.ClientError(
                        code = basketDetailResponse.responseCode,
                        message = basketDetailResponse.responseMessage,
                        body = basketDetailResponse.responseMessage
                    )
                }
            } else {
                when (response.status.value) {
                    401 -> {
                        throw com.kanishk.goldscanner.data.network.AuthenticationException("Unauthorized access")
                    }
                    404 -> {
                        throw ApiException.ClientError(
                            code = 404,
                            message = "Basket not found",
                            body = "Basket with ID $basketId not found"
                        )
                    }
                    in 400..499 -> {
                        val errorBody = response.bodyAsText()
                        throw ApiException.ClientError(
                            code = response.status.value,
                            message = "Client error: ${response.status.description}",
                            body = errorBody
                        )
                    }
                    in 500..599 -> {
                        throw ApiException.ServerError(
                            code = response.status.value,
                            message = "Server error: ${response.status.description}",
                            body = response.bodyAsText()
                        )
                    }
                    else -> {
                        throw ApiException.NetworkError("HTTP error: ${response.status}")
                    }
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