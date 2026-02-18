package com.kanishk.goldscanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kanishk.goldscanner.data.model.response.Result
import com.kanishk.goldscanner.data.model.request.UpdateBasketRequest
import com.kanishk.goldscanner.data.model.request.NepaliDateRequest
import com.kanishk.goldscanner.domain.usecase.basket.GetActiveBasketIdUseCase
import com.kanishk.goldscanner.domain.usecase.basket.GetBasketDetailsUseCase
import com.kanishk.goldscanner.domain.usecase.basket.UpdateBasketUseCase
import com.kanishk.goldscanner.domain.usecase.basket.DeleteBasketArticleUseCase
import com.kanishk.goldscanner.domain.usecase.GetCurrentGoldRateUseCase
import com.kanishk.goldscanner.domain.usecase.ClearActiveBasketIdUseCase
import com.kanishk.goldscanner.presentation.ui.screen.BasketDetailState
import com.kanishk.goldscanner.utils.GoldArticleCalculator
import com.kanishk.goldscanner.utils.Utils
import com.kanishk.goldscanner.utils.NepaliDateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class BasketDetailViewModel(
    private val getActiveBasketIdUseCase: GetActiveBasketIdUseCase,
    private val getBasketDetailsUseCase: GetBasketDetailsUseCase,
    private val updateBasketUseCase: UpdateBasketUseCase,
    private val deleteBasketArticleUseCase: DeleteBasketArticleUseCase,
    private val getCurrentGoldRateUseCase: GetCurrentGoldRateUseCase,
    private val clearActiveBasketIdUseCase: ClearActiveBasketIdUseCase
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
                            updatedOldGoldItemCost = basket.oldGoldItemCost,
                            updatedExtraDiscount = basket.extraDiscount,
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
            updatedOldGoldItemCost = if (isValid) parsedValue else currentState.updatedOldGoldItemCost,
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
            updatedExtraDiscount = if (isValid) parsedValue else currentState.updatedExtraDiscount,
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
     * Save basket with updated values
     */
    fun saveBasket(isBilled: Boolean) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                // Get active basket ID
                val basketId = getActiveBasketIdUseCase()
                if (basketId == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "No active basket found"
                    )
                    return@launch
                }
                
                val currentState = _uiState.value
                
                // Prepare the request
                val request = if (isBilled) {
                    // Get current date and gold rate for billing (using GMT/UTC time)
                    val currentDateTime = ZonedDateTime.now(ZoneOffset.UTC)
                    val isoDateTime = currentDateTime.format(DateTimeFormatter.ISO_INSTANT)
                    
                    // Get current gold rate
                    val goldRateResult = getCurrentGoldRateUseCase()
                    val currentGoldRate = when (goldRateResult) {
                        is Result.Success -> goldRateResult.data.rates["24"] ?: 0.0
                        is Result.Error -> 0.0
                        is Result.Loading -> 0.0
                    }
                    
                    val nepaliDate = when (goldRateResult) {
                        is Result.Success -> NepaliDateRequest(
                            year = goldRateResult.data.date.year,
                            month = goldRateResult.data.date.month,
                            dayOfMonth = goldRateResult.data.date.dayOfMonth
                        )
                        is Result.Error -> NepaliDateRequest(
                            year = 2081,
                            month = 1,
                            dayOfMonth = 1
                        )
                        is Result.Loading -> NepaliDateRequest(
                            year = 2081,
                            month = 1,
                            dayOfMonth = 1
                        )
                    }
                    
                    UpdateBasketRequest(
                        oldGoldItemCost = currentState.updatedOldGoldItemCost,
                        extraDiscount = currentState.updatedExtraDiscount,
                        isBilled = true,
                        billingDate = isoDateTime,
                        billingDateNepali = nepaliDate,
                        billedGoldRate24KPerTola = currentGoldRate
                    )
                } else {
                    UpdateBasketRequest(
                        oldGoldItemCost = currentState.updatedOldGoldItemCost,
                        extraDiscount = currentState.updatedExtraDiscount,
                        isBilled = false
                    )
                }
                
                // Make the API call
                when (val result = updateBasketUseCase(basketId, request)) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = if (isBilled) "Basket billed successfully!" else "Basket saved successfully!",
                            errorMessage = null
                        )
                        
                        // If billed, refresh to show updated status with new UI
                        if (isBilled) {
                            loadBasketDetails() // Refresh to show updated status
                        } else {
                            // Refresh basket details to show updated values
                            loadBasketDetails()
                        }
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.errorResponse.message,
                            successMessage = null
                        )
                    }
                    is Result.Loading -> {
                        // Already in loading state
                    }
                }
            } catch (e: Exception) {
                Log.e("BasketDetailViewModel", "Error saving basket", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "An unexpected error occurred while saving the basket",
                    successMessage = null
                )
            }
        }
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
    
    /**
     * Clear active basket ID from local storage
     * Used when navigating away from basket detail to other screens
     */
    fun clearActiveBasket() {
        viewModelScope.launch {
            try {
                clearActiveBasketIdUseCase()
            } catch (e: Exception) {
                Log.e("BasketDetailViewModel", "Error clearing active basket", e)
            }
        }
    }
    
    /**
     * Delete an article from the basket
     */
    fun deleteArticle(articleId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                errorMessage = null, 
                successMessage = null
            )
            
            try {
                when (val result = deleteBasketArticleUseCase(articleId)) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "Article removed from basket successfully",
                            errorMessage = null
                        )
                        // Refresh basket details to update the UI
                        loadBasketDetails()
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.errorResponse.message ?: "Failed to delete article from basket",
                            successMessage = null
                        )
                    }
                    is Result.Loading -> {
                        // Already in loading state
                    }
                }
            } catch (e: Exception) {
                Log.e("BasketDetailViewModel", "Error deleting article", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "An unexpected error occurred while deleting the article",
                    successMessage = null
                )
            }
        }
    }
    
    /**
     * Show delete confirmation dialog for an article
     */
    fun showDeleteConfirmationDialog(article: com.kanishk.goldscanner.data.model.response.BasketArticle) {
        _uiState.value = _uiState.value.copy(
            showDeleteConfirmationDialog = true,
            articleToDelete = article
        )
    }
    
    /**
     * Dismiss delete confirmation dialog
     */
    fun dismissDeleteConfirmationDialog() {
        _uiState.value = _uiState.value.copy(
            showDeleteConfirmationDialog = false,
            articleToDelete = null
        )
    }
    
    /**
     * Confirm and execute article deletion
     */
    fun confirmDeleteArticle() {
        val articleToDelete = _uiState.value.articleToDelete
        if (articleToDelete != null) {
            dismissDeleteConfirmationDialog()
            deleteArticle(articleToDelete.id)
        }
    }
    
    /**
     * Show discard basket confirmation dialog
     */
    fun showDiscardBasketDialog() {
        _uiState.value = _uiState.value.copy(showDiscardConfirmationDialog = true)
    }
    
    /**
     * Dismiss discard basket confirmation dialog
     */
    fun dismissDiscardBasketDialog() {
        _uiState.value = _uiState.value.copy(showDiscardConfirmationDialog = false)
    }
    
    /**
     * Discard the current basket
     */
    fun discardBasket(onNavigateToBasketListing: () -> Unit) {
        viewModelScope.launch {
            val basketId = getActiveBasketIdUseCase() ?: return@launch
            
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                errorMessage = null,
                showDiscardConfirmationDialog = false
            )
            
            try {
                // Get current UTC date and Nepali date
                val currentDateTime = ZonedDateTime.now(ZoneOffset.UTC)
                val isoDateTime = currentDateTime.format(DateTimeFormatter.ISO_INSTANT)
                val nepaliDate = NepaliDateUtils.getCurrentNepaliDate()
                
                val request = UpdateBasketRequest(
                    oldGoldItemCost = _uiState.value.updatedOldGoldItemCost,
                    extraDiscount = _uiState.value.updatedExtraDiscount,
                    isBilled = false,
                    isDiscarded = true,
                    discardedDate = isoDateTime,
                    discardedDateNepali = NepaliDateRequest(
                        year = nepaliDate.year,
                        month = nepaliDate.month,
                        dayOfMonth = nepaliDate.dayOfMonth
                    )
                )
                
                when (val result = updateBasketUseCase(basketId, request)) {
                    is Result.Success -> {
                        // Clear active basket ID from local storage
                        clearActiveBasketIdUseCase()
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "Basket discarded successfully!",
                            errorMessage = null
                        )
                        
                        // Navigate to basket listing screen
                        onNavigateToBasketListing()
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.errorResponse.message ?: "Failed to discard basket"
                        )
                    }
                    is Result.Loading -> {
                        // Already handled above
                    }
                }
            } catch (e: Exception) {
                Log.e("BasketDetailViewModel", "Error discarding basket", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "An unexpected error occurred while discarding the basket"
                )
            }
        }
    }
}