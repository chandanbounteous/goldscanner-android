package com.kanishk.goldscanner.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class GoldArticleResponse(
    val responseCode: Int,
    val responseMessage: String,
    val body: GoldArticleBody
)

@Serializable
data class GoldArticleBody(
    val article: GoldArticle
)