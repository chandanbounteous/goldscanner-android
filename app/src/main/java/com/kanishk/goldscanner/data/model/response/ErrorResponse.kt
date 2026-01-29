package com.kanishk.goldscanner.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val message: String,
    val code: Int? = null,
    val details: String? = null,
    val timestamp: String? = null,
    val errors: List<ErrorDetail>? = null
)