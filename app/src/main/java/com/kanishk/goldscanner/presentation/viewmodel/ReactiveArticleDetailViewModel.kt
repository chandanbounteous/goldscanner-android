package com.kanishk.goldscanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kanishk.goldscanner.data.model.response.Result
import com.kanishk.goldscanner.data.model.request.CreateArticleRequest
import com.kanishk.goldscanner.data.model.request.UpdateArticleRequest
import com.kanishk.goldscanner.data.model.response.GoldRateResponse
import com.kanishk.goldscanner.data.model.response.GoldArticle
import com.kanishk.goldscanner.domain.usecase.CreateArticleUseCase
import com.kanishk.goldscanner.domain.usecase.UpdateArticleUseCase
import com.kanishk.goldscanner.domain.usecase.basket.GetActiveBasketIdUseCase
import com.kanishk.goldscanner.domain.repository.GoldRateRepository
import com.kanishk.goldscanner.presentation.ui.screen.ArticleDetailMode
import com.kanishk.goldscanner.presentation.ui.screen.ArticleDetailState
import com.kanishk.goldscanner.utils.ReactiveGoldArticle
import com.kanishk.goldscanner.utils.ReactiveCalculationEngine
import com.kanishk.goldscanner.utils.copyField
import com.kanishk.goldscanner.utils.GoldRateHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ReactiveArticleDetailViewModel(
    private val goldRateRepository: GoldRateRepository,
    private val createArticleUseCase: CreateArticleUseCase,
    private val updateArticleUseCase: UpdateArticleUseCase,
    private val getActiveBasketIdUseCase: GetActiveBasketIdUseCase
) : ViewModel() {
    
    // Reactive calculation engine
    private val calculationEngine = ReactiveCalculationEngine()
    
    // Core reactive article state
    private val _reactiveArticle = MutableStateFlow(ReactiveGoldArticle())
    val reactiveArticle: StateFlow<ReactiveGoldArticle> = _reactiveArticle.asStateFlow()
    
    // UI state for form management
    private val _uiState = MutableStateFlow(ArticleDetailState())
    val uiState: StateFlow<ArticleDetailState> = _uiState.asStateFlow()
    
    // Gold rate response cache
    private var goldRateResponse: GoldRateResponse? = null
    
    // Article being edited (for UPDATE_INDEPENDENT mode)
    private var editingArticle: GoldArticle? = null
    
    init {
        loadGoldRates()
        setupReactiveCalculations()
        checkActiveBasket()
    }
    
    /**
     * Setup reactive calculations that sync between reactive article and UI state
     */
    private fun setupReactiveCalculations() {
        viewModelScope.launch {
            reactiveArticle.collect { article ->
                syncToUiState(article)
            }
        }
    }
    
    /**
     * Check if there's an active basket to show/hide the "Save to basket" button
     */
    private fun checkActiveBasket() {
        viewModelScope.launch {
            val activeBasketId = getActiveBasketIdUseCase()
            _uiState.value = _uiState.value.copy(
                hasActiveBasket = activeBasketId != null
            )
        }
    }
    
    /**
     * Load gold rates and initialize reactive state
     */
    private fun loadGoldRates() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = goldRateRepository.getCurrentGoldRate()) {
                is Result.Success -> {
                    goldRateResponse = result.data
                    
                    // Update gold rate in reactive article
                    val goldRate24K = GoldRateHelper.getGoldRateForKarat(result.data, 24)
                    updateReactiveField("goldRate24KPerTola", goldRate24K)
                    
                    // If we have an article to edit and this is the first time loading gold rates,
                    // populate the fields now
                    editingArticle?.let { article ->
                        if (_uiState.value.mode == ArticleDetailMode.UPDATE_INDEPENDENT) {
                            populateFieldsFromArticle(article)
                        }
                    }
                    
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.errorResponse.message
                    )
                }
                is Result.Loading -> {
                    // Already in loading state
                }
            }
        }
    }
    
    /**
     * Update reactive field and trigger recalculations
     */
    private fun updateReactiveField(key: String, value: Any) {
        val currentArticle = _reactiveArticle.value
        
        // Update the field first
        val updatedArticle = currentArticle.copyField(key, value)
        
        // Validate the field with updated article context
        val isValid = calculationEngine.validateField(key, value, updatedArticle)
        
        // Update validation state in UI
        updateValidationState(key, isValid)
        
        // Only proceed with calculations if validation passes
        if (isValid) {
            // Trigger reactive recalculations
            val recalculatedArticle = calculationEngine.evaluateDependencies(key, updatedArticle)
            
            _reactiveArticle.value = recalculatedArticle
            
            // For fields that affect gross weight validation, revalidate gross weight
            if (key == "netWeight" || key == "addOnCost") {
                val grossWeightValid = calculationEngine.validateField("grossWeight", recalculatedArticle.grossWeight, recalculatedArticle)
                updateValidationState("grossWeight", grossWeightValid)
            }
        } else {
            // Even if validation fails, update the field value for user feedback
            _reactiveArticle.value = updatedArticle
        }
    }
    
    /**
     * Update validation state in UI
     */
    private fun updateValidationState(key: String, isValid: Boolean) {
        val currentState = _uiState.value
        val updatedState = when (key) {
            "articleCode" -> currentState.copy(isArticleCodeValid = isValid)
            "netWeight" -> currentState.copy(isNetWeightValid = isValid)
            "grossWeight" -> currentState.copy(isGrossWeightValid = isValid)
            "addOnCost" -> currentState.copy(isAddOnCostValid = isValid)
            "discount" -> currentState.copy(isDiscountValid = isValid)
            "wastage" -> currentState.copy(isWastageValid = isValid)
            "makingCharge" -> currentState.copy(isMakingChargeValid = isValid)
            else -> currentState
        }
        _uiState.value = updatedState
    }
    
    /**
     * Sync reactive article state to UI state
     */
    private fun syncToUiState(article: ReactiveGoldArticle) {
        val currentGoldRatePerKarat = goldRateResponse?.let { 
            GoldRateHelper.getGoldRateForKarat(it, article.karat) 
        } ?: 0.0
        
        val roundedCurrentGoldRate = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(currentGoldRatePerKarat, 2)
        
        // Update validation states for all fields
        val isNetWeightValid = calculationEngine.validateField("netWeight", article.netWeight, article)
        val isGrossWeightValid = calculationEngine.validateField("grossWeight", article.grossWeight, article)
        val isAddOnCostValid = calculationEngine.validateField("addOnCost", article.addOnCost, article)
        val isDiscountValid = calculationEngine.validateField("discount", article.discount, article)
        val isWastageValid = article.wastage >= 0.0
        val isMakingChargeValid = article.makingCharge >= 0.0
        val isArticleCodeValid = calculationEngine.validateField("articleCode", article.articleCode, article)
        
        _uiState.value = _uiState.value.copy(
            karat = article.karat,
            goldRateAsPerKaratPerTola = roundedCurrentGoldRate,
            articleCode = article.articleCode,
            netWeight = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(article.netWeight, 2),
            grossWeight = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(article.grossWeight, 2),
            addOnCost = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(article.addOnCost, 2),
            wastage = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(article.wastage, 2),
            totalWeight = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(article.totalWeight, 2),
            articleCostAsPerWeightAndKarat = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(article.articleCostAsPerWeightAndKarat, 2),
            makingCharge = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(article.makingCharge, 2),
            discount = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(article.discount, 2),
            articleCostBeforeTax = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(article.articleCostBeforeTax, 2),
            luxuryTax = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(article.luxuryTax, 2),
            articleCostAfterTax = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(article.articleCostAfterTax, 2),
            finalEstimatedCost = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(article.finalEstimatedCost, 2),
            
            // Update validation states
            isArticleCodeValid = isArticleCodeValid,
            isNetWeightValid = isNetWeightValid,
            isGrossWeightValid = isGrossWeightValid,
            isAddOnCostValid = isAddOnCostValid,
            isDiscountValid = isDiscountValid,
            isWastageValid = isWastageValid,
            isMakingChargeValid = isMakingChargeValid,
            
            // Update text states for input fields - format to show exactly 2 decimal places
            netWeightText = if (article.netWeight == 0.0) "" else String.format("%.2f", article.netWeight),
            grossWeightText = if (article.grossWeight == 0.0) "" else String.format("%.2f", article.grossWeight),
            addOnCostText = if (article.addOnCost == 0.0) "" else String.format("%.2f", article.addOnCost),
            wastageText = if (article.wastage == 0.0) "" else String.format("%.2f", article.wastage),
            makingChargeText = if (article.makingCharge == 0.0) "" else String.format("%.2f", article.makingCharge),
            discountText = if (article.discount == 0.0) "" else String.format("%.2f", article.discount)
        )
    }
    
    // Public API methods for UI
    fun setMode(mode: ArticleDetailMode) {
        _uiState.value = _uiState.value.copy(mode = mode)
    }
    
    /**
     * Populate the article fields for editing (UPDATE_INDEPENDENT mode)
     */
    fun populateArticleForEdit(article: GoldArticle) {
        editingArticle = article
        
        // Set mode first
        setMode(ArticleDetailMode.UPDATE_INDEPENDENT)
        
        // If gold rates are already loaded, populate immediately
        // Otherwise, population will happen when gold rates are loaded
        if (goldRateResponse != null) {
            populateFieldsFromArticle(article)
        }
    }
    
    private fun populateFieldsFromArticle(article: GoldArticle) {
        // Update reactive fields with article values
        updateReactiveField("articleCode", article.articleCode)
        updateReactiveField("karat", article.karat)
        updateReactiveField("netWeight", article.netWeight ?: 0.0)
        updateReactiveField("grossWeight", article.grossWeight ?: 0.0)
        updateReactiveField("addOnCost", article.addOnCost)
        
        // Update gold rate for the article's karat
        goldRateResponse?.let { response ->
            val goldRateForKarat = GoldRateHelper.getGoldRateForKarat(response, article.karat)
            updateReactiveField("goldRate24KPerTola", goldRateForKarat)
        }
    }
    
    fun updateKarat(karat: Int) {
        updateReactiveField("karat", karat)
        
        // Update gold rate per karat
        goldRateResponse?.let { response ->
            val newGoldRate = GoldRateHelper.getGoldRateForKarat(response, karat)
            updateReactiveField("goldRate24KPerTola", newGoldRate)
        }
    }
    
    fun updateArticleCode(articleCode: String) {
        updateReactiveField("articleCode", articleCode.uppercase())
    }
    
    fun updateNetWeight(netWeight: Double) {
        val roundedValue = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(netWeight, 2)
        updateReactiveField("netWeight", roundedValue)
    }
    
    fun updateGrossWeight(grossWeight: Double) {
        val roundedValue = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(grossWeight, 2)
        updateReactiveField("grossWeight", roundedValue)
    }
    
    fun updateAddOnCost(addOnCost: Double) {
        val roundedValue = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(addOnCost, 2)
        updateReactiveField("addOnCost", roundedValue)
    }
    
    fun updateDiscount(discount: Double) {
        val roundedValue = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(discount, 2)
        updateReactiveField("discount", roundedValue)
    }
    
    fun updateWastage(wastage: Double) {
        // Manual wastage update
        _uiState.value = _uiState.value.copy(isWastageManuallyEdited = true)
        val roundedValue = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(wastage, 2)
        
        // Validate manually entered wastage
        val isValid = calculationEngine.validateField("wastage", roundedValue, _reactiveArticle.value)
        updateValidationState("wastage", isValid)
        
        if (isValid) {
            updateReactiveField("wastage", roundedValue)
        } else {
            // Update the field value for display even if invalid
            val currentArticle = _reactiveArticle.value
            _reactiveArticle.value = currentArticle.copyField("wastage", roundedValue)
        }
    }
    
    fun updateMakingCharge(makingCharge: Double) {
        // Manual making charge update
        _uiState.value = _uiState.value.copy(isMakingChargeManuallyEdited = true)
        val roundedValue = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(makingCharge, 2)
        
        // Validate manually entered making charge
        val isValid = calculationEngine.validateField("makingCharge", roundedValue, _reactiveArticle.value)
        updateValidationState("makingCharge", isValid)
        
        if (isValid) {
            updateReactiveField("makingCharge", roundedValue)
        } else {
            // Update the field value for display even if invalid
            val currentArticle = _reactiveArticle.value
            _reactiveArticle.value = currentArticle.copyField("makingCharge", roundedValue)
        }
    }
    
    /**
     * Update text field states for partial input handling (e.g., "." before number)
     */
    fun updateNetWeightText(text: String) {
        _uiState.value = _uiState.value.copy(netWeightText = text)
        text.toDoubleOrNull()?.let { value ->
            val roundedValue = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(value, 2)
            updateReactiveField("netWeight", roundedValue)
        }
    }
    
    fun updateGrossWeightText(text: String) {
        _uiState.value = _uiState.value.copy(grossWeightText = text)
        text.toDoubleOrNull()?.let { value ->
            val roundedValue = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(value, 2)
            updateReactiveField("grossWeight", roundedValue)
        }
    }
    
    fun updateAddOnCostText(text: String) {
        _uiState.value = _uiState.value.copy(addOnCostText = text)
        text.toDoubleOrNull()?.let { value ->
            val roundedValue = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(value, 2)
            updateReactiveField("addOnCost", roundedValue)
        }
    }
    
    fun updateWastageText(text: String) {
        _uiState.value = _uiState.value.copy(wastageText = text)
        text.toDoubleOrNull()?.let { value ->
            val roundedValue = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(value, 2)
            updateWastage(roundedValue)
        }
    }
    
    fun updateMakingChargeText(text: String) {
        _uiState.value = _uiState.value.copy(makingChargeText = text)
        text.toDoubleOrNull()?.let { value ->
            val roundedValue = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(value, 2)
            updateMakingCharge(roundedValue)
        }
    }
    
    fun updateDiscountText(text: String) {
        _uiState.value = _uiState.value.copy(discountText = text)
        text.toDoubleOrNull()?.let { value ->
            val roundedValue = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(value, 2)
            updateReactiveField("discount", roundedValue)
        }
    }
    
    /**
     * Save article using reactive state
     */
    fun saveArticle() {
        val currentState = _uiState.value
        val currentArticle = _reactiveArticle.value
        
        if (!currentState.isFormValid) {
            _uiState.value = currentState.copy(
                errorMessage = "Please fill all required fields correctly"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true)
            
            try {
                when (currentState.mode) {
                    ArticleDetailMode.UPDATE_INDEPENDENT -> {
                        // Update existing article
                        val articleId = editingArticle?.id
                        if (articleId == null) {
                            _uiState.value = currentState.copy(
                                isLoading = false,
                                errorMessage = "Article ID not found for update"
                            )
                            return@launch
                        }
                        
                        val updateRequest = UpdateArticleRequest(
                            netWeight = currentArticle.netWeight,
                            grossWeight = currentArticle.grossWeight,
                            addOnCost = currentArticle.addOnCost
                        )
                        
                        when (val result = updateArticleUseCase(articleId, updateRequest)) {
                            is Result.Success -> {
                                _uiState.value = currentState.copy(
                                    isLoading = false,
                                    successMessage = "Article updated successfully",
                                    errorMessage = null
                                )
                            }
                            is Result.Error -> {
                                _uiState.value = currentState.copy(
                                    isLoading = false,
                                    errorMessage = result.errorResponse.message ?: "Failed to update article"
                                )
                            }
                            is Result.Loading -> {
                                // Already handled by setting isLoading = true above
                            }
                        }
                    }
                    else -> {
                        // Create new article
                        val createRequest = CreateArticleRequest(
                            articleCode = currentArticle.articleCode,
                            netWeight = currentArticle.netWeight,
                            grossWeight = currentArticle.grossWeight,
                            addOnCost = currentArticle.addOnCost,
                            karat = currentArticle.karat,
                            stoneWeight = 0.0,
                            serialNumber = null,
                            carigarNameCode = null
                        )
                        
                        when (val result = createArticleUseCase(createRequest)) {
                            is Result.Success -> {
                                _uiState.value = currentState.copy(
                                    isLoading = false,
                                    successMessage = "Article created successfully",
                                    errorMessage = null
                                )
                            }
                            is Result.Error -> {
                                _uiState.value = currentState.copy(
                                    isLoading = false,
                                    errorMessage = result.errorResponse.message ?: "Failed to create article"
                                )
                            }
                            is Result.Loading -> {
                                // Already handled by setting isLoading = true above
                            }
                        }
                    }
                }
                
            } catch (e: Exception) {
                val actionName = if (currentState.mode == ArticleDetailMode.UPDATE_INDEPENDENT) "update" else "create"
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to $actionName article"
                )
            }
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
    
    /**
     * Force recalculation of all fields (useful for initialization)
     */
    fun recalculateAll() {
        val recalculatedArticle = calculationEngine.recalculateAll(_reactiveArticle.value)
        _reactiveArticle.value = recalculatedArticle
    }
}