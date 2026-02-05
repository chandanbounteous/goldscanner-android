package com.kanishk.goldscanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.kanishk.goldscanner.data.model.GoldArticleWithCalculation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared ViewModel to pass article data between screens for editing
 * Uses singleton pattern to ensure same instance across screens
 */
class SharedEditArticleViewModel : ViewModel() {
    
    private val _selectedArticle = MutableStateFlow<GoldArticleWithCalculation?>(null)
    val selectedArticle: StateFlow<GoldArticleWithCalculation?> = _selectedArticle.asStateFlow()
    
    fun selectArticleForEdit(article: GoldArticleWithCalculation) {
        _selectedArticle.value = article
    }
    
    fun clearSelectedArticle() {
        _selectedArticle.value = null
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