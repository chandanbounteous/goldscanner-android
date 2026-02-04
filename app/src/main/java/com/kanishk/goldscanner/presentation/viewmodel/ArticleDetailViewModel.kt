package com.kanishk.goldscanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldscanner.data.common.Result
import com.kanishk.goldscanner.data.model.request.CreateArticleRequest
import com.kanishk.goldscanner.data.model.response.GoldRateResponse
import com.kanishk.goldscanner.domain.usecase.CreateArticleUseCase
import com.kanishk.goldscanner.domain.repository.GoldRateRepository
import com.kanishk.goldscanner.presentation.ui.screen.ArticleDetailMode
import com.kanishk.goldscanner.presentation.ui.screen.ArticleDetailState
import com.kanishk.goldscanner.utils.GoldArticleCalculator
import com.kanishk.goldscanner.utils.GoldRateHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import kotlin.math.round

class ArticleDetailViewModel(
    private val goldRateRepository: GoldRateRepository,
    private val createArticleUseCase: CreateArticleUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ArticleDetailState())
    val uiState: StateFlow<ArticleDetailState> = _uiState.asStateFlow()
    
    private var goldRateResponse: GoldRateResponse? = null
    
    // Article code pattern: [A-Z][A-Z][A-Z][0-9][0-9][0-9][0-9]
    private val articleCodePattern = Pattern.compile("^[A-Z]{3}[0-9]{4}$")
    
    init {
        loadGoldRates()
    }
    
    fun setMode(mode: ArticleDetailMode) {
        _uiState.value = _uiState.value.copy(mode = mode)
    }
    
    private fun loadGoldRates() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = goldRateRepository.getCurrentGoldRate()) {
                is Result.Success -> {
                    goldRateResponse = result.data
                    recalculateAllFields()
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
    
    // Field update methods
    fun updateKarat(karat: Int) {
        _uiState.value = _uiState.value.copy(karat = karat)
        recalculateAllFields()
    }
    
    fun updateArticleCode(articleCode: String) {
        val upperCode = articleCode.uppercase()
        val isValid = articleCodePattern.matcher(upperCode).matches()
        
        _uiState.value = _uiState.value.copy(
            articleCode = upperCode,
            isArticleCodeValid = isValid
        )
    }
    
    fun updateNetWeight(netWeight: Double) {
        val currentState = _uiState.value
        val isValid = netWeight > 0 && netWeight <= 999.0
        
        // Update gross weight if it's empty or less than net weight
        val shouldUpdateGrossWeight = currentState.grossWeight == 0.0 || currentState.grossWeight < netWeight
        val newGrossWeight = if (shouldUpdateGrossWeight) netWeight else currentState.grossWeight
        val newGrossWeightText = if (shouldUpdateGrossWeight) {
            if (netWeight == 0.0) "" else netWeight.toString()
        } else currentState.grossWeightText
        
        _uiState.value = currentState.copy(
            netWeight = netWeight,
            netWeightText = if (netWeight == 0.0) "" else netWeight.toString(),
            isNetWeightValid = isValid,
            grossWeight = newGrossWeight,
            grossWeightText = newGrossWeightText,
            isGrossWeightValid = if (shouldUpdateGrossWeight) {
                newGrossWeight > 0 && newGrossWeight <= 999.0 &&
                (currentState.addOnCost == 0.0 && newGrossWeight == netWeight ||
                 currentState.addOnCost > 0.0 && newGrossWeight >= netWeight)
            } else currentState.isGrossWeightValid
        )
        
        if (isValid) {
            recalculateAllFields()
        }
    }

    fun updateNetWeightText(text: String) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            netWeightText = text
        )
        
        // Try to parse and update numeric value
        val numericValue = text.toDoubleOrNull()
        if (numericValue != null) {
            val isValid = numericValue > 0 && numericValue <= 999.0
            
            // Update gross weight if it's empty or less than net weight
            val shouldUpdateGrossWeight = currentState.grossWeight == 0.0 || currentState.grossWeight < numericValue
            val newGrossWeight = if (shouldUpdateGrossWeight) numericValue else currentState.grossWeight
            val newGrossWeightText = if (shouldUpdateGrossWeight) {
                if (numericValue == 0.0) "" else numericValue.toString()
            } else currentState.grossWeightText
            
            _uiState.value = _uiState.value.copy(
                netWeight = numericValue,
                isNetWeightValid = isValid,
                grossWeight = newGrossWeight,
                grossWeightText = newGrossWeightText,
                isGrossWeightValid = if (shouldUpdateGrossWeight) {
                    newGrossWeight > 0 && newGrossWeight <= 999.0 &&
                    (currentState.addOnCost == 0.0 && newGrossWeight == numericValue ||
                     currentState.addOnCost > 0.0 && newGrossWeight >= numericValue)
                } else currentState.isGrossWeightValid
            )
            
            if (isValid) {
                recalculateAllFields()
            }
        } else {
            // Invalid input, but keep the text for user editing
            _uiState.value = _uiState.value.copy(
                isNetWeightValid = false
            )
        }
    }
    
    fun updateGrossWeight(grossWeight: Double) {
        val currentState = _uiState.value
        val isValid = grossWeight > 0 && grossWeight <= 999.0 &&
                (currentState.addOnCost == 0.0 && grossWeight == currentState.netWeight ||
                 currentState.addOnCost > 0.0 && grossWeight >= currentState.netWeight)
        
        _uiState.value = currentState.copy(
            grossWeight = grossWeight,
            grossWeightText = if (grossWeight == 0.0) "" else grossWeight.toString(),
            isGrossWeightValid = isValid
        )
        
        if (isValid) {
            recalculateAllFields()
        }
    }

    fun updateGrossWeightText(text: String) {
        _uiState.value = _uiState.value.copy(
            grossWeightText = text
        )
        
        // Try to parse and update numeric value
        val numericValue = text.toDoubleOrNull()
        if (numericValue != null) {
            val currentState = _uiState.value
            val isValid = numericValue > 0 && numericValue <= 999.0 &&
                    (currentState.addOnCost == 0.0 && numericValue == currentState.netWeight ||
                     currentState.addOnCost > 0.0 && numericValue >= currentState.netWeight)
            _uiState.value = _uiState.value.copy(
                grossWeight = numericValue,
                isGrossWeightValid = isValid
            )
            
            if (isValid) {
                recalculateAllFields()
            }
        } else {
            // Invalid input, but keep the text for user editing
            _uiState.value = _uiState.value.copy(
                isGrossWeightValid = false
            )
        }
    }
    
    fun updateAddOnCost(addOnCost: Double) {
        val isValid = addOnCost >= 0 && addOnCost <= 500000.0
        
        _uiState.value = _uiState.value.copy(
            addOnCost = addOnCost,
            addOnCostText = if (addOnCost == 0.0) "" else addOnCost.toString(),
            isAddOnCostValid = isValid
        )
        
        if (isValid) {
            recalculateAllFields()
        }
    }

    fun updateAddOnCostText(text: String) {
        _uiState.value = _uiState.value.copy(
            addOnCostText = text
        )
        
        // Try to parse and update numeric value
        val numericValue = text.toDoubleOrNull()
        if (numericValue != null) {
            val isValid = numericValue >= 0 && numericValue <= 500000.0
            _uiState.value = _uiState.value.copy(
                addOnCost = numericValue,
                isAddOnCostValid = isValid
            )
            
            if (isValid) {
                recalculateAllFields()
            }
        } else {
            // Invalid input, but keep the text for user editing
            _uiState.value = _uiState.value.copy(
                isAddOnCostValid = text.isEmpty() // Empty is valid (default 0)
            )
        }
    }
    
    fun updateDiscount(discount: Double) {
        val isValid = discount >= 0 && discount <= 5000.0
        
        _uiState.value = _uiState.value.copy(
            discount = discount,
            discountText = if (discount == 0.0) "" else discount.toString(),
            isDiscountValid = isValid
        )
        
        if (isValid) {
            recalculateAllFields()
        }
    }

    fun updateDiscountText(text: String) {
        _uiState.value = _uiState.value.copy(
            discountText = text
        )
        
        // Try to parse and update numeric value
        val numericValue = text.toDoubleOrNull()
        if (numericValue != null) {
            val isValid = numericValue >= 0 && numericValue <= 5000.0
            _uiState.value = _uiState.value.copy(
                discount = numericValue,
                isDiscountValid = isValid
            )
            
            if (isValid) {
                recalculateAllFields()
            }
        } else {
            // Invalid input, but keep the text for user editing
            _uiState.value = _uiState.value.copy(
                isDiscountValid = text.isEmpty() // Empty is valid (default 0)
            )
        }
    }
    
    fun updateWastage(wastage: Double) {
        val isValid = wastage >= 0 && wastage <= 999.0
        
        _uiState.value = _uiState.value.copy(
            wastage = wastage,
            wastageText = if (wastage == 0.0) "" else wastage.toString(),
            isWastageValid = isValid,
            isWastageManuallyEdited = true
        )
        
        if (isValid) {
            recalculateFromWastage()
        }
    }

    fun updateWastageText(text: String) {
        _uiState.value = _uiState.value.copy(
            wastageText = text
        )
        
        // Try to parse and update numeric value
        val numericValue = text.toDoubleOrNull()
        if (numericValue != null) {
            val isValid = numericValue >= 0 && numericValue <= 999.0
            _uiState.value = _uiState.value.copy(
                wastage = numericValue,
                isWastageValid = isValid,
                isWastageManuallyEdited = true
            )
            
            if (isValid) {
                recalculateFromWastage()
            }
        } else {
            // Invalid input, but keep the text for user editing
            _uiState.value = _uiState.value.copy(
                isWastageValid = false
            )
        }
    }
    
    fun updateMakingCharge(makingCharge: Double) {
        val isValid = makingCharge >= 0 && makingCharge <= 100000.0
        
        _uiState.value = _uiState.value.copy(
            makingCharge = makingCharge,
            makingChargeText = if (makingCharge == 0.0) "" else makingCharge.toString(),
            isMakingChargeValid = isValid,
            isMakingChargeManuallyEdited = true
        )
        
        if (isValid) {
            recalculateFromMakingCharge()
        }
    }

    fun updateMakingChargeText(text: String) {
        _uiState.value = _uiState.value.copy(
            makingChargeText = text
        )
        
        // Try to parse and update numeric value
        val numericValue = text.toDoubleOrNull()
        if (numericValue != null) {
            val isValid = numericValue >= 0 && numericValue <= 100000.0
            _uiState.value = _uiState.value.copy(
                makingCharge = numericValue,
                isMakingChargeValid = isValid,
                isMakingChargeManuallyEdited = true
            )
            
            if (isValid) {
                recalculateFromMakingCharge()
            }
        } else {
            // Invalid input, but keep the text for user editing
            _uiState.value = _uiState.value.copy(
                isMakingChargeValid = false
            )
        }
    }
    
    private fun recalculateAllFields() {
        val currentState = _uiState.value
        
        if (goldRateResponse == null) {
            _uiState.value = currentState.copy(isLoading = false)
            return
        }
        
        // Step 1: Calculate gold rate for selected karat
        val goldRateForKarat = GoldRateHelper.getGoldRateForKarat(goldRateResponse, currentState.karat)
        val goldRate24K = GoldRateHelper.get24KaratGoldRate(goldRateResponse)
        
        // Step 2: Get calculated values or use manual values if edited
        val calculatedWastage = if (currentState.isWastageManuallyEdited) {
            currentState.wastage
        } else {
            GoldArticleCalculator.calculateArticleCost(
                currentGoldRate24KPerTola = goldRate24K,
                netWeight = currentState.netWeight,
                karat = currentState.karat,
                addOnCost = 0.0, // Only for wastage calculation
                discount = 0.0
            ).wastage
        }
        
        // Step 3: Calculate total weight (net + wastage)
        val totalWeight = currentState.netWeight + calculatedWastage
        
        // Step 4: Calculate article cost based on weight and karat
        val articleCostAsPerWeight = calculateArticleCostByWeight(totalWeight, currentState.karat, goldRate24K)
        
        // Step 5: Get calculated making charge or use manual value if edited
        val calculatedMakingCharge = if (currentState.isMakingChargeManuallyEdited) {
            currentState.makingCharge
        } else {
            GoldArticleCalculator.calculateArticleCost(
                currentGoldRate24KPerTola = goldRate24K,
                netWeight = currentState.netWeight,
                karat = currentState.karat,
                addOnCost = 0.0,
                discount = 0.0
            ).makingCharge
        }
        
        // Step 6: Calculate costs
        val articleCostBeforeTax = roundToTwoDecimal(articleCostAsPerWeight + calculatedMakingCharge - currentState.discount)
        val luxuryTax = roundToTwoDecimal(articleCostBeforeTax * 0.02)
        val articleCostAfterTax = roundToTwoDecimal(articleCostBeforeTax + luxuryTax)
        val finalEstimatedCost = roundToTwoDecimal(articleCostAfterTax + currentState.addOnCost)
        
        _uiState.value = currentState.copy(
            goldRateAsPerKaratPerTola = roundToTwoDecimal(goldRateForKarat),
            wastage = roundToTwoDecimal(calculatedWastage),
            wastageText = if (!currentState.isWastageManuallyEdited) {
                if (roundToTwoDecimal(calculatedWastage) == 0.0) "" else roundToTwoDecimal(calculatedWastage).toString()
            } else currentState.wastageText,
            totalWeight = roundToTwoDecimal(totalWeight),
            articleCostAsPerWeightAndKarat = roundToTwoDecimal(articleCostAsPerWeight),
            makingCharge = roundToTwoDecimal(calculatedMakingCharge),
            makingChargeText = if (!currentState.isMakingChargeManuallyEdited) {
                if (roundToTwoDecimal(calculatedMakingCharge) == 0.0) "" else roundToTwoDecimal(calculatedMakingCharge).toString()
            } else currentState.makingChargeText,
            articleCostBeforeTax = articleCostBeforeTax,
            luxuryTax = luxuryTax,
            articleCostAfterTax = articleCostAfterTax,
            finalEstimatedCost = finalEstimatedCost,
            isWastageValid = calculatedWastage >= 0 && calculatedWastage <= 999.0,
            isMakingChargeValid = calculatedMakingCharge >= 0 && calculatedMakingCharge <= 100000.0,
            isLoading = false,
            errorMessage = null
        )
        
        // Validate gross weight with updated calculations
        validateGrossWeight()
    }
    
    private fun validateGrossWeight() {
        val currentState = _uiState.value
        val isValid = currentState.grossWeight > 0 && currentState.grossWeight <= 999.0 &&
                (currentState.addOnCost == 0.0 && currentState.grossWeight == currentState.netWeight ||
                 currentState.addOnCost > 0.0 && currentState.grossWeight >= currentState.netWeight)
        
        _uiState.value = currentState.copy(isGrossWeightValid = isValid)
    }
    
    private fun recalculateFromWastage() {
        val currentState = _uiState.value
        if (goldRateResponse == null) return
        
        val goldRate24K = GoldRateHelper.get24KaratGoldRate(goldRateResponse)
        val goldRateForKarat = GoldRateHelper.getGoldRateForKarat(goldRateResponse, currentState.karat)
        
        // Recalculate dependent fields when wastage is manually changed
        val totalWeight = currentState.netWeight + currentState.wastage
        val articleCostAsPerWeight = calculateArticleCostByWeight(totalWeight, currentState.karat, goldRate24K)
        
        // Get making charge (calculated or manual)
        val makingCharge = if (currentState.isMakingChargeManuallyEdited) {
            currentState.makingCharge
        } else {
            GoldArticleCalculator.calculateArticleCost(
                currentGoldRate24KPerTola = goldRate24K,
                netWeight = currentState.netWeight,
                karat = currentState.karat,
                addOnCost = 0.0,
                discount = 0.0
            ).makingCharge
        }
        
        val articleCostBeforeTax = roundToTwoDecimal(articleCostAsPerWeight + makingCharge - currentState.discount)
        val luxuryTax = roundToTwoDecimal(articleCostBeforeTax * 0.02)
        val articleCostAfterTax = roundToTwoDecimal(articleCostBeforeTax + luxuryTax)
        val finalEstimatedCost = roundToTwoDecimal(articleCostAfterTax + currentState.addOnCost)
        
        _uiState.value = currentState.copy(
            totalWeight = roundToTwoDecimal(totalWeight),
            articleCostAsPerWeightAndKarat = roundToTwoDecimal(articleCostAsPerWeight),
            makingCharge = roundToTwoDecimal(makingCharge),
            makingChargeText = if (!currentState.isMakingChargeManuallyEdited) {
                if (roundToTwoDecimal(makingCharge) == 0.0) "" else roundToTwoDecimal(makingCharge).toString()
            } else currentState.makingChargeText,
            articleCostBeforeTax = articleCostBeforeTax,
            luxuryTax = luxuryTax,
            articleCostAfterTax = articleCostAfterTax,
            finalEstimatedCost = finalEstimatedCost
        )
    }
    
    private fun recalculateFromMakingCharge() {
        val currentState = _uiState.value
        if (goldRateResponse == null) return
        
        val goldRate24K = GoldRateHelper.get24KaratGoldRate(goldRateResponse)
        
        // Get wastage and calculate total weight
        val wastage = if (currentState.isWastageManuallyEdited) {
            currentState.wastage
        } else {
            GoldArticleCalculator.calculateArticleCost(
                currentGoldRate24KPerTola = goldRate24K,
                netWeight = currentState.netWeight,
                karat = currentState.karat,
                addOnCost = 0.0,
                discount = 0.0
            ).wastage
        }
        
        val totalWeight = currentState.netWeight + wastage
        val articleCostAsPerWeight = calculateArticleCostByWeight(totalWeight, currentState.karat, goldRate24K)
        
        // Recalculate costs with manual making charge
        val articleCostBeforeTax = roundToTwoDecimal(articleCostAsPerWeight + currentState.makingCharge - currentState.discount)
        val luxuryTax = roundToTwoDecimal(articleCostBeforeTax * 0.02)
        val articleCostAfterTax = roundToTwoDecimal(articleCostBeforeTax + luxuryTax)
        val finalEstimatedCost = roundToTwoDecimal(articleCostAfterTax + currentState.addOnCost)
        
        _uiState.value = currentState.copy(
            wastage = roundToTwoDecimal(wastage),
            wastageText = if (!currentState.isWastageManuallyEdited) {
                if (roundToTwoDecimal(wastage) == 0.0) "" else roundToTwoDecimal(wastage).toString()
            } else currentState.wastageText,
            totalWeight = roundToTwoDecimal(totalWeight),
            articleCostAsPerWeightAndKarat = roundToTwoDecimal(articleCostAsPerWeight),
            articleCostBeforeTax = articleCostBeforeTax,
            luxuryTax = luxuryTax,
            articleCostAfterTax = articleCostAfterTax,
            finalEstimatedCost = finalEstimatedCost
        )
    }
    
    private fun calculateArticleCostByWeight(totalWeight: Double, karat: Int, goldRate24K: Double): Double {
        val weightInTolas = totalWeight / 11.664 // ONE_TOLA_IN_GMS = 11.664
        val purityFactor = if (karat == 24) 1.0 else 0.92 // Simplified purity factor
        return weightInTolas * purityFactor * goldRate24K
    }
    
    fun saveArticle() {
        val currentState = _uiState.value
        
        if (!currentState.isFormValid) {
            _uiState.value = currentState.copy(
                errorMessage = "Please fill all required fields correctly"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true)
            
            try {
                val request = CreateArticleRequest(
                    articleCode = currentState.articleCode,
                    netWeight = currentState.netWeight,
                    grossWeight = currentState.grossWeight,
                    addOnCost = currentState.addOnCost,
                    karat = currentState.karat,
                    stoneWeight = 0.0,
                    serialNumber = null,
                    carigarNameCode = null
                )
                
                when (val result = createArticleUseCase(request)) {
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
                
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to create article"
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
    
    private fun roundToTwoDecimal(value: Double): Double {
        return round(value * 100) / 100
    }
}