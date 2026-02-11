package com.kanishk.goldscanner.data.model.response

import kotlinx.serialization.Serializable

/**
 * Error response data class containing error information
 */
@Serializable
data class ErrorResponse(
    val responseCode: Int = 0,
    val responseMessage: String = "",
    val errors: List<ErrorDetail>? = null,
    val field: String? = null,
    val message: String = ""
) {
    companion object {
        fun networkError(message: String = "Network error occurred") = ErrorResponse(
            responseCode = -1,
            responseMessage = message,
            message = message
        )
        
        fun authenticationError(message: String = "Authentication failed") = ErrorResponse(
            responseCode = 401,
            responseMessage = message,
            message = message
        )
        
        fun clientError(code: Int, message: String, details: String? = null) = ErrorResponse(
            responseCode = code,
            responseMessage = message,
            message = details ?: message
        )
        
        fun noInternetError() = ErrorResponse(
            responseCode = -2,
            responseMessage = "No internet connection",
            message = "Please check your internet connection and try again"
        )
        
        fun unknownError(message: String = "An unknown error occurred") = ErrorResponse(
            responseCode = -3,
            responseMessage = message,
            message = message
        )
    }
}

/**
 * Error detail for field-specific validation errors
 */
@Serializable
data class ErrorDetail(
    val field: String,
    val message: String
)