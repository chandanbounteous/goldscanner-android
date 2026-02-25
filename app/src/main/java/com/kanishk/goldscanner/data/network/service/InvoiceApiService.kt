package com.kanishk.goldscanner.data.network.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import com.kanishk.goldscanner.data.network.ApiException
import com.kanishk.goldscanner.data.network.NetworkConfig

class InvoiceApiService(
    private val networkConfig: NetworkConfig
) {
    
    suspend fun generateInvoicePdf(basketId: String): ByteArray {
        return try {
            // Check if authentication is needed and get token
            val tokenManager = networkConfig.getTokenManager()
            val tokenResult = tokenManager.getValidAccessToken()
            
            val response: HttpResponse = when (tokenResult) {
                is com.kanishk.goldscanner.data.network.TokenResult.Success -> {
                    networkConfig.client.get("v1/invoice/basket/$basketId/pdf") {
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
                response.body()
            } else {
                when (response.status) {
                    HttpStatusCode.NotFound -> {
                        throw ApiException.ClientError(
                            code = 404,
                            message = "Invoice not found for the given basket",
                            body = "Invoice not found"
                        )
                    }
                    HttpStatusCode.InternalServerError -> {
                        throw ApiException.ServerError(
                            code = 500,
                            message = "Failed to generate PDF invoice",
                            body = "Internal server error"
                        )
                    }
                    else -> {
                        throw ApiException.NetworkError("Failed to generate invoice PDF: ${response.status}")
                    }
                }
            }
        } catch (e: Exception) {
            when (e) {
                is ApiException -> throw e
                is com.kanishk.goldscanner.data.network.AuthenticationException -> throw e
                else -> throw ApiException.NetworkError("Network error while generating PDF: ${e.message}")
            }
        }
    }
}