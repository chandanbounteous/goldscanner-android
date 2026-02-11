package com.kanishk.goldscanner.data.model.request

import kotlinx.serialization.Serializable

@Serializable
data class AddArticleToBasketRequest(
    val articleId: String,
    val netWeight: Double,
    val grossWeight: Double,
    val addOnCost: Double = 0.0,
    val wastage: Double,
    val makingCharge: Double,
    val discount: Double = 0.0
)