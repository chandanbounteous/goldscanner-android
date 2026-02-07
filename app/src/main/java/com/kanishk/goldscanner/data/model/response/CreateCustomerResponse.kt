package com.kanishk.goldscanner.data.model.response

import com.kanishk.goldscanner.data.model.Customer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateCustomerResponse(
    @SerialName("responseCode")
    val responseCode: Int,
    @SerialName("responseMessage") 
    val responseMessage: String,
    @SerialName("body")
    val body: CustomerBody
) {
    @Serializable
    data class CustomerBody(
        @SerialName("customer")
        val customer: Customer
    )
}