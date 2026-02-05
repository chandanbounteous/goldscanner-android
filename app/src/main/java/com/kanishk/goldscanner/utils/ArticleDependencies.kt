package com.kanishk.goldscanner.utils

/**
 * Dependency and impact graph for reactive gold article calculations
 * This object defines the relationships between different attributes and how changes propagate
 */
object ArticleDependencies {
    
    /**
     * Defines what each field depends on for calculation
     */
    val DEPENDS_ON = mapOf(
        "wastage" to listOf("karat", "netWeight"),
        "totalWeight" to listOf("netWeight", "wastage"),
        "articleCostAsPerWeightAndKarat" to listOf("goldRate24KPerTola", "karat", "totalWeight"),
        "makingCharge" to listOf("karat", "netWeight", "articleCostAsPerWeightAndKarat"),
        "articleCostBeforeTax" to listOf("articleCostAsPerWeightAndKarat", "makingCharge", "discount"),
        "luxuryTax" to listOf("articleCostBeforeTax"),
        "articleCostAfterTax" to listOf("articleCostBeforeTax", "luxuryTax"),
        "finalEstimatedCost" to listOf("articleCostAfterTax", "addOnCost"),
        // UI-specific dependencies
        "grossWeight" to listOf("netWeight", "addOnCost") // grossWeight should be >= netWeight
    )
    
    /**
     * Defines what fields are impacted when a field changes
     */
    val IMPACTS = mapOf(
        "goldRate24KPerTola" to listOf("articleCostAsPerWeightAndKarat"),
        "karat" to listOf("wastage", "articleCostAsPerWeightAndKarat", "makingCharge"),
        "netWeight" to listOf("wastage", "totalWeight", "grossWeight"),
        "wastage" to listOf("totalWeight"),
        "totalWeight" to listOf("articleCostAsPerWeightAndKarat"),
        "articleCostAsPerWeightAndKarat" to listOf("makingCharge", "articleCostBeforeTax"),
        "makingCharge" to listOf("articleCostBeforeTax"),
        "discount" to listOf("articleCostBeforeTax"),
        "articleCostBeforeTax" to listOf("luxuryTax", "articleCostAfterTax"),
        "luxuryTax" to listOf("articleCostAfterTax"),
        "articleCostAfterTax" to listOf("finalEstimatedCost"),
        "addOnCost" to listOf("finalEstimatedCost", "grossWeight")
    )
    
    /**
     * Fields that should be validated when changed
     */
    val VALIDATION_RULES = mapOf(
        "netWeight" to { value: Double -> value > 0.0 && value <= 999.0 },
        "grossWeight" to { value: Double -> value > 0.0 && value <= 999.0 },
        "addOnCost" to { value: Double -> value >= 0.0 && value <= 500000.0 },
        "discount" to { value: Double -> value >= 0.0 && value <= 5000.0 },
        "karat" to { value: Int -> value in listOf(14, 18, 22, 24) }
    )
    
    /**
     * Get topologically sorted calculation order to avoid circular dependencies
     */
    fun getCalculationOrder(): List<String> {
        return listOf(
            "wastage",
            "totalWeight", 
            "articleCostAsPerWeightAndKarat",
            "makingCharge",
            "articleCostBeforeTax",
            "luxuryTax",
            "articleCostAfterTax",
            "finalEstimatedCost",
            "grossWeight"
        )
    }
}