package com.kanishk.goldscanner.utils

import com.kanishk.goldscanner.data.model.response.ErrorResponse
import kotlinx.serialization.Serializable

@Serializable
sealed class Result<T> {
    @Serializable
    data class Success<T>(val data: T) : Result<T>()
    
    @Serializable
    data class Error<T>(val errorResponse: ErrorResponse) : Result<T>()
    
    @Serializable
    class Loading<T> : Result<T>()
}