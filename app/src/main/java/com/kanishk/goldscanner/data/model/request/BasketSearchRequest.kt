package com.kanishk.goldscanner.data.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BasketSearchRequest(
    @SerialName("customerName")
    val customerName: String? = null,
    @SerialName("phone")
    val phone: String? = null,
    @SerialName("startDate")
    val startDate: String? = null,
    @SerialName("endDate")
    val endDate: String? = null,
    @SerialName("includeBilled")
    val includeBilled: Boolean? = null,
    @SerialName("includeDiscarded")
    val includeDiscarded: Boolean? = null,
    @SerialName("offset")
    val offset: Int = 0,
    @SerialName("limit")
    val limit: Int = 10
)