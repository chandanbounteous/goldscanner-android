package com.kanishk.goldscanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kanishk.goldscanner.domain.usecase.GetCurrentGoldRateUseCase
import com.kanishk.goldscanner.data.model.response.GoldRateResponse
import com.goldscanner.data.common.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GoldRateUiState(
    val isLoading: Boolean = false,
    val goldRateData: GoldRateResponse? = null,
    val errorMessage: String? = null,
    val isAuthenticationError: Boolean = false
)

class GoldRateViewModel(
    private val getCurrentGoldRateUseCase: GetCurrentGoldRateUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GoldRateUiState())
    val uiState: StateFlow<GoldRateUiState> = _uiState.asStateFlow()
    
    init {
        getCurrentGoldRate()
    }
    
    fun getCurrentGoldRate() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                isAuthenticationError = false
            )
            
            when (val result = getCurrentGoldRateUseCase()) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        goldRateData = result.data,
                        errorMessage = null,
                        isAuthenticationError = false
                    )
                }
                is Result.Error -> {
                    val isAuthError = result.errorResponse.responseCode == 401
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        goldRateData = null,
                        errorMessage = result.errorResponse.message,
                        isAuthenticationError = isAuthError
                    )
                }
                is Result.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }
    
    fun clearAuthenticationError() {
        _uiState.value = _uiState.value.copy(isAuthenticationError = false)
    }
}