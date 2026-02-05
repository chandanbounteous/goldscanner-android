package com.kanishk.goldscanner.utils

/**
 * Reactive data class representing gold article calculation state
 */
data class ReactiveGoldArticle(
    val goldRate24KPerTola: Double = 0.0,
    val karat: Int = 24,
    val netWeight: Double = 0.0,
    val wastage: Double = 0.0,
    val totalWeight: Double = 0.0,
    val articleCostAsPerWeightAndKarat: Double = 0.0,
    val makingCharge: Double = 0.0,
    val discount: Double = 0.0,
    val articleCostBeforeTax: Double = 0.0,
    val luxuryTax: Double = 0.0,
    val articleCostAfterTax: Double = 0.0,
    val addOnCost: Double = 0.0,
    val finalEstimatedCost: Double = 0.0,
    
    // Additional UI-specific fields
    val articleCode: String = "",
    val grossWeight: Double = 0.0
)

/**
 * Extension function to copy fields dynamically
 */
fun ReactiveGoldArticle.copyField(key: String, value: Any): ReactiveGoldArticle =
    when (key) {
        "goldRate24KPerTola" -> copy(goldRate24KPerTola = value as Double)
        "karat" -> copy(karat = value as Int)
        "netWeight" -> copy(netWeight = value as Double)
        "wastage" -> copy(wastage = value as Double)
        "totalWeight" -> copy(totalWeight = value as Double)
        "articleCostAsPerWeightAndKarat" -> copy(articleCostAsPerWeightAndKarat = value as Double)
        "makingCharge" -> copy(makingCharge = value as Double)
        "discount" -> copy(discount = value as Double)
        "articleCostBeforeTax" -> copy(articleCostBeforeTax = value as Double)
        "luxuryTax" -> copy(luxuryTax = value as Double)
        "articleCostAfterTax" -> copy(articleCostAfterTax = value as Double)
        "addOnCost" -> copy(addOnCost = value as Double)
        "finalEstimatedCost" -> copy(finalEstimatedCost = value as Double)
        "articleCode" -> copy(articleCode = value as String)
        "grossWeight" -> copy(grossWeight = value as Double)
        else -> this
    }