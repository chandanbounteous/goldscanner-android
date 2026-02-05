package com.kanishk.goldscanner.utils

import com.kanishk.goldscanner.utils.Utils

/**
 * Reactive calculation engine for gold article dependency management
 * This handles transitive recalculations automatically when fields change
 */
class ReactiveCalculationEngine {
    
    /**
     * Evaluate dependencies and trigger recalculations transitively
     */
    fun evaluateDependencies(
        changedKey: String, 
        currentArticle: ReactiveGoldArticle,
        visited: MutableSet<String> = mutableSetOf()
    ): ReactiveGoldArticle {
        if (changedKey in visited) return currentArticle
        visited.add(changedKey)
        
        // Get impacted fields
        val impactedFields = ArticleDependencies.IMPACTS[changedKey] ?: return currentArticle
        
        var updatedArticle = currentArticle
        
        // Recalculate impacted fields
        impactedFields.forEach { impactedField ->
            updatedArticle = recalculateAttribute(impactedField, updatedArticle)
            // Recursively evaluate dependencies of the impacted field
            updatedArticle = evaluateDependencies(impactedField, updatedArticle, visited)
        }
        
        return updatedArticle
    }
    
    /**
     * Recalculate a specific attribute based on current article state
     */
    private fun recalculateAttribute(attr: String, current: ReactiveGoldArticle): ReactiveGoldArticle {
        val newValue = when (attr) {
            "wastage" -> {
                val value = GoldArticleCalculator.calculateWastage(current.netWeight, current.karat)
                Utils.roundToTwoDecimalPlaces(value, 2)
            }
            "totalWeight" -> {
                val value = GoldArticleCalculator.calculateTotalWeight(current.netWeight, current.wastage)
                Utils.roundToTwoDecimalPlaces(value, 2)
            }
            "articleCostAsPerWeightAndKarat" -> {
                val value = GoldArticleCalculator.calculateArticleCostAsPerWeightRateAndKarat(
                    current.totalWeight, current.karat, current.goldRate24KPerTola
                )
                Utils.roundToTwoDecimalPlaces(value, 2)
            }
            "makingCharge" -> {
                val value = GoldArticleCalculator.calculateMakingCharge(
                    current.netWeight, current.karat, current.articleCostAsPerWeightAndKarat
                )
                Utils.roundToTwoDecimalPlaces(value, 2)
            }
            "articleCostBeforeTax" -> {
                val value = GoldArticleCalculator.calculateTotalCostBeforeTax(
                    current.articleCostAsPerWeightAndKarat, current.makingCharge, current.discount
                )
                Utils.roundToTwoDecimalPlaces(value, 2)
            }
            "luxuryTax" -> {
                val value = GoldArticleCalculator.calculateLuxuryTax(current.articleCostBeforeTax)
                Utils.roundToTwoDecimalPlaces(value, 2)
            }
            "articleCostAfterTax" -> {
                val value = GoldArticleCalculator.calculateTotalCostAfterTax(
                    current.articleCostBeforeTax, current.luxuryTax
                )
                Utils.roundToTwoDecimalPlaces(value, 2)
            }
            "finalEstimatedCost" -> {
                val value = GoldArticleCalculator.calculateFinalEstimatedCost(
                    current.articleCostAfterTax, current.addOnCost
                )
                Utils.roundToTwoDecimalPlaces(value, 2)
            }
            "grossWeight" -> {
                // Auto-calculate gross weight based on net weight and add-on cost
                val value = if (current.addOnCost == 0.0) {
                    current.netWeight
                } else {
                    maxOf(current.grossWeight, current.netWeight) // Ensure gross >= net when add-on exists
                }
                Utils.roundToTwoDecimalPlaces(value, 2)
            }
            else -> return current
        }
        
        return current.copyField(attr, newValue)
    }
    
    /**
     * Validate a field value according to business rules
     */
    fun validateField(key: String, value: Any, currentArticle: ReactiveGoldArticle): Boolean {
        return when (key) {
            "netWeight" -> {
                val doubleValue = value as Double
                val rule = ArticleDependencies.VALIDATION_RULES[key] as? (Double) -> Boolean
                rule?.invoke(doubleValue) ?: true
            }
            "addOnCost", "discount" -> {
                val doubleValue = value as Double
                val rule = ArticleDependencies.VALIDATION_RULES[key] as? (Double) -> Boolean
                rule?.invoke(doubleValue) ?: true
            }
            "grossWeight" -> {
                val doubleValue = value as Double
                val baseValidation = ArticleDependencies.VALIDATION_RULES[key] as? (Double) -> Boolean
                val baseValid = baseValidation?.invoke(doubleValue) ?: true
                
                // Additional validation: gross weight should be >= net weight when addOnCost > 0
                val grossWeightValid = if (currentArticle.addOnCost == 0.0) {
                    doubleValue == currentArticle.netWeight
                } else {
                    doubleValue >= currentArticle.netWeight
                }
                
                baseValid && grossWeightValid
            }
            "wastage" -> {
                val doubleValue = value as Double
                doubleValue >= 0.0 && doubleValue <= 50.0  // Reasonable wastage limits
            }
            "makingCharge" -> {
                val doubleValue = value as Double
                doubleValue >= 0.0  // Making charge should be non-negative
            }
            "karat" -> {
                val rule = ArticleDependencies.VALIDATION_RULES[key] as? (Int) -> Boolean
                rule?.invoke(value as Int) ?: true
            }
            "articleCode" -> {
                // Article code pattern: [A-Z][A-Z][A-Z][0-9][0-9][0-9][0-9]
                val pattern = Regex("^[A-Z]{3}[0-9]{4}$")
                pattern.matches(value as String)
            }
            else -> true
        }
    }
    
    /**
     * Perform a full recalculation of all dependent fields
     */
    fun recalculateAll(article: ReactiveGoldArticle): ReactiveGoldArticle {
        var updatedArticle = article
        
        // Calculate fields in dependency order
        ArticleDependencies.getCalculationOrder().forEach { fieldKey ->
            updatedArticle = recalculateAttribute(fieldKey, updatedArticle)
        }
        
        return updatedArticle
    }
}