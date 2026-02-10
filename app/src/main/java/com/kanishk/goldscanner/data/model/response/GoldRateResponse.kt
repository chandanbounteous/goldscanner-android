package com.kanishk.goldscanner.data.model.response

import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializable as KotlinxSerializable
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import com.kanishk.goldscanner.utils.SimpleNepaliDateSerializer

@Serializable
data class GoldRateResponse(
    val rates: Map<String, Double>,
    val date: NepaliDate,
    val lastUpdated: String
)

// Type alias to use the library's SimpleDate as NepaliDate with custom serializer
typealias NepaliDate = @KotlinxSerializable(with = SimpleNepaliDateSerializer::class) SimpleDate

// Original NepaliDate data class - commented out as we're now using the library's SimpleDate
/*
@Serializable
data class NepaliDate(
    val year: Int,
    val month: Int,
    val dayOfMonth: Int
)
*/

@Serializable
data class GoldRateApiResponse(
    val responseCode: Int,
    val responseMessage: String,
    val body: GoldRateResponse
)