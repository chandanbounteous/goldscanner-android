package com.kanishk.goldscanner.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Customer(
    @SerialName("id")
    val id: String,
    @SerialName("firstName")
    val firstName: String,
    @SerialName("lastName")
    val lastName: String? = null,
    @SerialName("phone")
    val phone: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("createdAt")
    val createdAt: String,
    @SerialName("updatedAt")
    val updatedAt: String
) {
    val fullName: String
        get() = "$firstName ${lastName ?: ""}".trim()
}