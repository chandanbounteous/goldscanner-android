package com.kanishk.goldscanner.presentation.ui.screen

data class ArticleDetailState(
    val mode: ArticleDetailMode = ArticleDetailMode.CREATE_NEW,
    
    // Core article fields
    val karat: Int = 24,
    val goldRateAsPerKaratPerTola: Double = 0.0,
    val articleCode: String = "",
    val netWeight: Double = 0.0,
    val grossWeight: Double = 0.0,
    val addOnCost: Double = 0.0,
    
    // Text input states for decimal fields (to support partial inputs like ".")
    val netWeightText: String = "",
    val grossWeightText: String = "",
    val addOnCostText: String = "",
    val wastageText: String = "",
    val makingChargeText: String = "",
    val discountText: String = "",
    
    // Calculated fields
    val wastage: Double = 0.0,
    val totalWeight: Double = 0.0,
    val articleCostAsPerWeightAndKarat: Double = 0.0,
    val makingCharge: Double = 0.0,
    val discount: Double = 0.0,
    val articleCostBeforeTax: Double = 0.0,
    val luxuryTax: Double = 0.0,
    val articleCostAfterTax: Double = 0.0,
    val finalEstimatedCost: Double = 0.0,
    
    // Validation states
    val isArticleCodeValid: Boolean = false,
    val isNetWeightValid: Boolean = false,
    val isGrossWeightValid: Boolean = false,
    val isAddOnCostValid: Boolean = true,  // 0.0 is valid default
    val isDiscountValid: Boolean = true,   // 0.0 is valid default
    val isWastageValid: Boolean = false,
    val isMakingChargeValid: Boolean = false,
    
    // Manual edit flags (to track if user has manually edited calculated fields)
    val isWastageManuallyEdited: Boolean = false,
    val isMakingChargeManuallyEdited: Boolean = false,
    
    // UI states
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    
    // Basket availability
    val hasActiveBasket: Boolean = false,
    
    // Available karat options
    val karatOptions: List<Int> = listOf(24, 22, 18, 14)
) {
    val isFormValid: Boolean
        get() = isArticleCodeValid && isNetWeightValid && isGrossWeightValid && 
                isAddOnCostValid && isDiscountValid && isWastageValid && 
                isMakingChargeValid && netWeight > 0
    
    val headerTitle: String
        get() = when (mode) {
            ArticleDetailMode.CREATE_NEW -> "Create New Article"
            ArticleDetailMode.UPDATE_INDEPENDENT -> "Update Article"
            ArticleDetailMode.UPDATE_BASKET_ITEM -> "Update Basket Item"
        }
}

data class FieldError(
    val field: String,
    val message: String
)