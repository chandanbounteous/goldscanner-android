package com.kanishk.goldscanner.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class GoldRateResponse(
    val rates: Map<String, Double>,
    val date: NepaliDate,
    val lastUpdated: String
)

@Serializable
data class NepaliDate(
    val year: Int,
    val month: Int,
    val dayOfMonth: Int
)

@Serializable
data class GoldRateApiResponse(
    val responseCode: Int,
    val responseMessage: String,
    val body: GoldRateResponse
)