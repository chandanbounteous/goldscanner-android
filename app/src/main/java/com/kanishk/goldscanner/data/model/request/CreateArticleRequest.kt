package com.kanishk.goldscanner.data.model.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateArticleRequest(
    val articleCode: String,
    val netWeight: Double,
    val grossWeight: Double,
    val addOnCost: Double,
    val karat: Int,
    val stoneWeight: Double = 0.0,
    val serialNumber: String? = null,
    val carigarNameCode: String? = null
)