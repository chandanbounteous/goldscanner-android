package com.kanishk.goldscanner.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val responseCode: Int,
    val responseMessage: String,
    val body: T
)

@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
    val user: UserInfo
)

@Serializable
data class UserInfo(
    val id: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val role: String
)

@Serializable
data class ErrorBody(
    val errors: List<ErrorDetail>? = null,
    val message: String? = null,
    val details: String? = null
)