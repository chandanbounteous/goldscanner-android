package com.kanishk.goldscanner.data.model.response

import com.kanishk.goldscanner.data.model.Customer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CustomerListResponse(
    @SerialName("responseCode")
    val responseCode: Int,
    @SerialName("responseMessage")
    val responseMessage: String,
    @SerialName("body")
    val body: CustomerListBody
)

@Serializable
data class CustomerListBody(
    @SerialName("customers")
    val customers: List<Customer>,
    @SerialName("total")
    val total: Int
)