package com.kanishk.goldscanner.data.model

data class GoldArticleCalculation(
    val wastage: Double,
    val articleCostAsPerWeightRateAndKarat: Double,
    val makingCharge: Double,
    val totalCostBeforeTax: Double,
    val luxuryTax: Double,
    val totalCostAfterTax: Double
)

data class GoldArticleWithCalculation(
    val article: com.kanishk.goldscanner.data.model.response.GoldArticle,
    val calculation: GoldArticleCalculation
)

data class LookupEntryForWastageAndMakingCharge(
    val minNetWeight: Double,
    val maxNetWeight: Double,
    val karat: Int, // 0 means applies to all karats
    val wastage: (Double) -> Double,
    val makingCharge: (Double) -> Double
)