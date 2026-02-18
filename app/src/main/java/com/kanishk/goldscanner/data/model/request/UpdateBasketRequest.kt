package com.kanishk.goldscanner.data.model.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateBasketRequest(
    val oldGoldItemCost: Double,
    val extraDiscount: Double,
    val isBilled: Boolean,
    val billingDate: String? = null,
    val billingDateNepali: NepaliDateRequest? = null,
    val billedGoldRate24KPerTola: Double? = null,
    val isDiscarded: Boolean? = null,
    val discardedDate: String? = null,
    val discardedDateNepali: NepaliDateRequest? = null
)

@Serializable
data class NepaliDateRequest(
    val year: Int,
    val month: Int,
    val dayOfMonth: Int
)