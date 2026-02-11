package com.kanishk.goldscanner.data.model.response

import kotlinx.serialization.Serializable

/**
 * A generic wrapper class for handling API responses and UI states
 */
@Serializable
sealed class Result<out T> {
    @Serializable
    data class Success<T>(val data: T) : Result<T>()
    
    @Serializable
    data class Error(val errorResponse: ErrorResponse) : Result<Nothing>()
    
    @Serializable
    object Loading : Result<Nothing>()
}