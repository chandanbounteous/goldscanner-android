package com.kanishk.goldscanner.data.model.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateArticleRequest(
    val netWeight: Double? = null,
    val grossWeight: Double? = null,
    val addOnCost: Double? = null
)