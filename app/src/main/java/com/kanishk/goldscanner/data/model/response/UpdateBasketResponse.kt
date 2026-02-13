package com.kanishk.goldscanner.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class UpdateBasketResponse(
    val responseCode: Int,
    val responseMessage: String,
    val body: UpdateBasketBody
)

@Serializable
data class UpdateBasketBody(
    val basket: UpdatedBasket
)

@Serializable
data class UpdatedBasket(
    val id: String,
    val basketNumber: Int,
    val customerId: String,
    val oldGoldItemCost: Double,
    val extraDiscount: Double,
    val billedGoldRate24KPerTola: Double? = null,
    val isBilled: Boolean,
    val isDiscarded: Boolean,
    val billingDate: String? = null,
    val discardedDate: String? = null,
    val updatedAt: String
)