package com.kanishk.goldscanner.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class GoldArticlesResponse(
    val responseCode: Int,
    val responseMessage: String,
    val body: GoldArticlesBody
)

@Serializable
data class GoldArticlesBody(
    val articles: List<GoldArticle>,
    val pagination: PaginationInfo
)

@Serializable
data class GoldArticle(
    val id: String,
    val articleCode: String,
    val serialNumber: String,
    val issueDate: String,
    val issueDateNepali: NepaliDate,
    val carigarId: String,
    val netWeight: Double,
    val grossWeight: Double,
    val stoneWeight: Double,
    val createdAt: String,
    val updatedAt: String,
    val karat: Int,
    val addOnCost: Double,
    val carigar: CarigarInfo
)

@Serializable
data class CarigarInfo(
    val id: String,
    val codeName: String
)

@Serializable
data class PaginationInfo(
    val offset: Int,
    val limit: Int,
    val total: Int,
    val hasMore: Boolean
)