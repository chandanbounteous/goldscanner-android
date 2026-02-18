package com.kanishk.goldscanner.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class UpdateBasketArticleResponse(
    val responseCode: Int,
    val responseMessage: String,
    val body: UpdateBasketArticleResponseBody
)

@Serializable
data class UpdateBasketArticleResponseBody(
    val basketArticle: UpdatedBasketArticleDetails
)

@Serializable
data class UpdatedBasketArticleDetails(
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