package com.kanishk.goldscanner.data.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable



@Serializable
data class BasketItem(
    @SerialName("id")
    val id: String,
    @SerialName("basketNumber")
    val basketNumber: Int,
    @SerialName("date")
    val date: String, // Gregorian date-time string
    @SerialName("nepaliDate")
    val nepaliDate: NepaliDate,
    @SerialName("firstName")
    val firstName: String,
    @SerialName("lastName")
    val lastName: String? = null,
    @SerialName("phone")
    val phone: String? = null,
    @SerialName("count")
    val count: Int, // Total articles in basket
    @SerialName("isBilled")
    val isBilled: Boolean,
    @SerialName("billingDateNepali")
    val billingDateNepali: NepaliDate? = null,
    @SerialName("isDiscarded")
    val isDiscarded: Boolean? = null,
    @SerialName("discardedDateNepali")
    val discardedDateNepali: NepaliDate? = null
)



@Serializable
data class BasketSearchResponseBody(
    @SerialName("baskets")
    val baskets: List<BasketItem>,
    @SerialName("pagination")
    val pagination: PaginationInfo
)

@Serializable
data class BasketSearchResponse(
    @SerialName("responseCode")
    val responseCode: Int,
    @SerialName("responseMessage")
    val responseMessage: String,
    @SerialName("body")
    val body: BasketSearchResponseBody
)