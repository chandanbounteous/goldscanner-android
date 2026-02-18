package com.kanishk.goldscanner.presentation.ui.screen

import com.kanishk.goldscanner.data.model.response.BasketArticle
import com.kanishk.goldscanner.data.model.response.BasketCustomer
import com.kanishk.goldscanner.data.model.response.BasketDetail

data class BasketDetailState(
    // Loading and error states
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    
    // Basket information
    val basketDetail: BasketDetail? = null,
    val customer: BasketCustomer? = null,
    val articles: List<BasketArticle> = emptyList(),
    
    // Editable fields
    val oldGoldItemCost: Double = 0.0,
    val extraDiscount: Double = 0.0,
    val oldGoldItemCostText: String = "0.00",
    val extraDiscountText: String = "0.00",
    val updatedOldGoldItemCost: Double = 0.0,
    val updatedExtraDiscount: Double = 0.0,
    
    // Calculated fields
    val originalPreTaxAmount: Double = 0.0, // Original from API
    val preTaxBasketAmount: Double = 0.0,   // Calculated
    val luxuryTax: Double = 0.0,             // Calculated
    val postTaxBasketAmount: Double = 0.0,   // Calculated
    val totalAddOnCost: Double = 0.0,        // Total add-on cost from articles
    val totalBasketAmount: Double = 0.0,     // Final total (post-tax + add-on costs)
    
    // Validation states
    val isOldGoldItemCostValid: Boolean = true,
    val isExtraDiscountValid: Boolean = true,
    
    // UI states
    val isDataLoaded: Boolean = false,
    
    // Delete confirmation dialog
    val showDeleteConfirmationDialog: Boolean = false,
    val articleToDelete: BasketArticle? = null,
    
    // Discard confirmation dialog
    val showDiscardConfirmationDialog: Boolean = false
) {
    val isFormValid: Boolean
        get() = isOldGoldItemCostValid && isExtraDiscountValid && isDataLoaded
        
    val customerDisplayName: String
        get() = customer?.let { 
            if (it.lastName.isNullOrBlank()) it.firstName else "${it.firstName} ${it.lastName}"
        } ?: ""
}