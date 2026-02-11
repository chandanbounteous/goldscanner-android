package com.kanishk.goldscanner.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class AddArticleToBasketResponse(
    val responseCode: Int,
    val responseMessage: String,
    val body: AddArticleToBasketResponseBody
)

@Serializable
data class AddArticleToBasketResponseBody(
    val basketArticleId: String,
    val basketArticle: BasketArticleDetails
)

@Serializable
data class BasketArticleDetails(
    val id: String,
    val basketId: String,
    val articleId: String,
    val netWeight: Double,
    val grossWeight: Double,
    val addOnCost: Double,
    val wastage: Double,
    val makingCharge: Double,
    val discount: Double,
    val createdAt: String,
    val updatedAt: String
)