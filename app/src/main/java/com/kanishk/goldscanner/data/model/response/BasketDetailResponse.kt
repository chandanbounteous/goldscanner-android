package com.kanishk.goldscanner.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class BasketDetailResponse(
    val responseCode: Int,
    val responseMessage: String,
    val body: BasketDetailResponseBody
)

@Serializable
data class BasketDetailResponseBody(
    val basket: BasketDetail,
    val articles: List<BasketArticle>,
    val totals: BasketTotals
)

@Serializable
data class BasketDetail(
    val id: String,
    val basketNumber: Int,
    val customerId: String,
    val customer: BasketCustomer,
    val isGoldRateFixed: Boolean,
    val fixedGoldRate24KPerTola: Double? = null,
    val effectiveGoldRate24KPerTola: Double,
    val oldGoldItemCost: Double,
    val extraDiscount: Double,
    val isBilled: Boolean,
    val isDiscarded: Boolean,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class BasketCustomer(
    val id: String,
    val firstName: String,
    val lastName: String? = null,
    val phone: String? = null,
    val email: String? = null
)

@Serializable
data class BasketArticle(
    val id: String,
    val basketId: String,
    val articleId: String,
    val articleCode: String,
    val netWeight: Double,
    val grossWeight: Double,
    val addOnCost: Double,
    val wastage: Double,
    val makingCharge: Double,
    val discount: Double,
    val karat: Int,
    val preTaxArticleCost: Double,
    val luxuryTaxAmount: Double,
    val postTaxArticleCost: Double,
    val finalCost: Double
)

@Serializable
data class BasketTotals(
    val preTaxBasketAmount: Double,
    val luxuryTax: Double,
    val postTaxBasketAmount: Double,
    val totalAddOnCost: Double,
    val totalBasketAmount: Double
)