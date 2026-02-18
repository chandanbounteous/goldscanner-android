package com.kanishk.goldscanner.data.model.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateBasketArticleRequest(
    val netWeight: Double,
    val grossWeight: Double,
    val addOnCost: Double,
    val wastage: Double,
    val makingCharge: Double,
    val discount: Double
)