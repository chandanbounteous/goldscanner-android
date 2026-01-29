package com.goldscanner.data.common

/**
 * A generic wrapper class for handling API responses and UI states
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val errorResponse: ErrorResponse) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

/**
 * Error response data class containing error information
 */
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
data class ErrorDetail(
    val field: String,
    val message: String
)