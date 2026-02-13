package com.kanishk.goldscanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kanishk.goldscanner.data.model.response.Result
import com.kanishk.goldscanner.domain.usecase.basket.GetActiveBasketIdUseCase
import com.kanishk.goldscanner.domain.usecase.basket.GetBasketDetailsUseCase
import com.kanishk.goldscanner.presentation.ui.screen.BasketDetailState
import com.kanishk.goldscanner.utils.GoldArticleCalculator
import com.kanishk.goldscanner.utils.Utils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

class BasketDetailViewModel(
    private val getActiveBasketIdUseCase: GetActiveBasketIdUseCase,
    private val getBasketDetailsUseCase: GetBasketDetailsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BasketDetailState())
    val uiState: StateFlow<BasketDetailState> = _uiState.asStateFlow()
    
    init {
        loadBasketDetails()
    }
    
    /**
     * Load basket details for the active basket
     */
    fun loadBasketDetails() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                // Get active basket ID
                val basketId = getActiveBasketIdUseCase()
                if (basketId == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "No active basket found"
                    )
                    return@launch
                }
                
                // Get basket details
                when (val result = getBasketDetailsUseCase(basketId)) {
                    is Result.Success -> {
                        val response = result.data
                        val basket = response.body.basket
                        val articles = response.body.articles
                        val totals = response.body.totals
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            basketDetail = basket,
                            customer = basket.customer,
                            articles = articles,
                            oldGoldItemCost = basket.oldGoldItemCost,
                            extraDiscount = basket.extraDiscount,
                            oldGoldItemCostText = Utils.formatDoubleToString(basket.oldGoldItemCost),
                            extraDiscountText = Utils.formatDoubleToString(basket.extraDiscount),
                            originalPreTaxAmount = totals.preTaxBasketAmount,
                            preTaxBasketAmount = calculatePreTaxBasketAmount(
                                totals.preTaxBasketAmount,
                                basket.oldGoldItemCost,
                                basket.extraDiscount
                            ),
                            luxuryTax = totals.luxuryTax,
                            postTaxBasketAmount = totals.postTaxBasketAmount,
                            totalAddOnCost = totals.totalAddOnCost,
                            totalBasketAmount = totals.totalBasketAmount,
                            isDataLoaded = true,
                            errorMessage = null
                        )
                        
                        // Recalculate totals with current values
                        recalculateAmounts()
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.errorResponse.message ?: "Failed to load basket details"
                        )
                    }
                    is Result.Loading -> {
                        // Already in loading state
                    }
                }
            } catch (e: Exception) {
                Log.e("BasketDetailViewModel", "Error loading basket details", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "An unexpected error occurred: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Update old gold item cost
     */
    fun updateOldGoldItemCost(value: String) {
        val currentState = _uiState.value
        val parsedValue = Utils.parseDoubleFromString(value)
        val isValid = parsedValue >= 0
        
        _uiState.value = currentState.copy(
            oldGoldItemCostText = value,
            oldGoldItemCost = if (isValid) parsedValue else currentState.oldGoldItemCost,
            isOldGoldItemCostValid = isValid,
            errorMessage = null
        )
        
        if (isValid) {
            recalculateAmounts()
        }
    }
    
    /**
     * Update extra discount
     */
    fun updateExtraDiscount(value: String) {
        val currentState = _uiState.value
        val parsedValue = Utils.parseDoubleFromString(value)
        val isValid = parsedValue >= 0
        
        _uiState.value = currentState.copy(
            extraDiscountText = value,
            extraDiscount = if (isValid) parsedValue else currentState.extraDiscount,
            isExtraDiscountValid = isValid,
            errorMessage = null
        )
        
        if (isValid) {
            recalculateAmounts()
        }
    }
    
    /**
     * Recalculate basket amounts based on current inputs
     */
    private fun recalculateAmounts() {
        val currentState = _uiState.value
        
        // Calculate updated pre-tax basket amount
        val updatedPreTaxAmount = calculatePreTaxBasketAmount(
            currentState.originalPreTaxAmount,
            currentState.oldGoldItemCost,
            currentState.extraDiscount
        )
        
        // Calculate luxury tax on updated amount
        val updatedLuxuryTax = GoldArticleCalculator.calculateBasketLuxuryTax(updatedPreTaxAmount)
        
        // Calculate post-tax amount
        val updatedPostTaxAmount = GoldArticleCalculator.calculatePostTaxBasketAmount(
            updatedPreTaxAmount,
            updatedLuxuryTax
        )
        
        // Calculate total basket amount
        val updatedTotalBasketAmount = GoldArticleCalculator.calculateTotalBasketAmount(
            updatedPostTaxAmount,
            currentState.totalAddOnCost
        )
        
        _uiState.value = currentState.copy(
            preTaxBasketAmount = updatedPreTaxAmount,
            luxuryTax = updatedLuxuryTax,
            postTaxBasketAmount = updatedPostTaxAmount,
            totalBasketAmount = updatedTotalBasketAmount
        )
    }
    
    /**
     * Calculate pre-tax basket amount
     */
    private fun calculatePreTaxBasketAmount(
        originalAmount: Double,
        oldGoldCost: Double,
        extraDiscount: Double
    ): Double {
        return GoldArticleCalculator.calculatePreTaxBasketAmount(
            originalAmount,
            oldGoldCost,
            extraDiscount
        )
    }
    
    /**
     * Refresh basket details
     */
    fun refresh() {
        loadBasketDetails()
    }
    
    /**
     * Clear error and success messages
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}