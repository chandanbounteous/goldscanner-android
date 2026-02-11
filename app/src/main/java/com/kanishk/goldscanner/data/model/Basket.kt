package com.kanishk.goldscanner.data.model

import kotlinx.serialization.Serializable
import com.kanishk.goldscanner.data.model.response.NepaliDate

data class Basket(
    val id: String,
    val basketNumber: Int,
    val date: String, // Gregorian date-time string for sorting/comparison
    val nepaliDateFormatted: String, // Formatted display string like "26 Poush 2082"
    val customerName: String, // Combined firstName + lastName
    val phone: String?,
    val articleCount: Int,
    val isBilled: Boolean,
    val isDiscarded: Boolean
)

data class BasketSearchFilter(
    val customerName: String? = null,
    val phone: String? = null,
    val startDate: String? = null, // dd-mm-yyyy format for UI
    val endDate: String? = null, // dd-mm-yyyy format for UI
    val includeBilled: Boolean? = null,
    val includeDiscarded: Boolean? = null
)

// Helper for local storage
data class ActiveBasket(
    val id: String,
    val basketNumber: Int,
    // Add other properties as needed for active basket
)

// Request model for basket creation
@Serializable
data class CreateBasketRequest(
    val isGoldRateFixed: Boolean,
    val fixedGoldRate24KPerTola: Double? = null,
    val fixedGoldRateNepaliDate: NepaliDate? = null
)

// Response models for basket creation
@Serializable
data class CreatedBasket(
    val id: String,
    val basketNumber: Int,
    val customerId: String,
    val isGoldRateFixed: Boolean,
    val fixedGoldRate24KPerTola: Double? = null,
    val fixedGoldRateNepaliDate: NepaliDate? = null,
    val createdAt: String
)

@Serializable
data class CreateBasketResponse(
    val responseCode: Int,
    val responseMessage: String,
    val body: CreateBasketResponseBody
)

@Serializable
data class CreateBasketResponseBody(
    val basket: CreatedBasket
)