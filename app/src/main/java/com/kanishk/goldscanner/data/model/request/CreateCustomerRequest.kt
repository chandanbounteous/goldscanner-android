package com.kanishk.goldscanner.data.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateCustomerRequest(
    @SerialName("firstName")
    val firstName: String,
    @SerialName("lastName")
    val lastName: String? = null,
    @SerialName("phone")
    val phone: String? = null,
    @SerialName("email")
    val email: String? = null
)