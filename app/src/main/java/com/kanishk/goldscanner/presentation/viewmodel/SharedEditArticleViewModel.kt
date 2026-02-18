package com.kanishk.goldscanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.kanishk.goldscanner.data.model.GoldArticleWithCalculation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Data class to hold basket editing context
 */
data class BasketEditContext(
    val basketDetail: com.kanishk.goldscanner.data.model.response.BasketDetail,
    val basketArticle: com.kanishk.goldscanner.data.model.response.BasketArticle
)

/**
 * Shared ViewModel to pass article data between screens for editing
 * Uses singleton pattern to ensure same instance across screens
 */
class SharedEditArticleViewModel : ViewModel() {
    
    private val _selectedArticle = MutableStateFlow<GoldArticleWithCalculation?>(null)
    val selectedArticle: StateFlow<GoldArticleWithCalculation?> = _selectedArticle.asStateFlow()
    
    // Basket editing context
    private val _basketEditContext = MutableStateFlow<BasketEditContext?>(null)
    val basketEditContext: StateFlow<BasketEditContext?> = _basketEditContext.asStateFlow()
    
    fun selectArticleForEdit(article: GoldArticleWithCalculation) {
        _selectedArticle.value = article
        _basketEditContext.value = null // Clear basket context for regular article editing
    }
    
    fun selectBasketArticleForEdit(
        basketDetail: com.kanishk.goldscanner.data.model.response.BasketDetail,
        basketArticle: com.kanishk.goldscanner.data.model.response.BasketArticle
    ) {
        _selectedArticle.value = null // Clear regular article selection
        _basketEditContext.value = BasketEditContext(basketDetail, basketArticle)
    }
    
    fun clearSelectedArticle() {
        _selectedArticle.value = null
        _basketEditContext.value = null
    }
    
    companion object {
        @Volatile
        private var INSTANCE: SharedEditArticleViewModel? = null
        
        fun getInstance(): SharedEditArticleViewModel {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SharedEditArticleViewModel().also { INSTANCE = it }
            }
        }
    }
}