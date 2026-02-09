package com.kanishk.goldscanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.kanishk.goldscanner.domain.usecase.basket.SearchBasketsUseCase
import com.kanishk.goldscanner.domain.usecase.basket.GetActiveBasketUseCase
import com.kanishk.goldscanner.data.model.Basket
import com.kanishk.goldscanner.data.model.BasketSearchFilter
import com.kanishk.goldscanner.utils.Result

class BasketListViewModel(
    private val searchBasketsUseCase: SearchBasketsUseCase,
    private val getActiveBasketUseCase: GetActiveBasketUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BasketListUiState())
    val uiState: StateFlow<BasketListUiState> = _uiState.asStateFlow()

    private val _searchFilter = MutableStateFlow(BasketSearchFilter())
    val searchFilter: StateFlow<BasketSearchFilter> = _searchFilter.asStateFlow()

    private var currentOffset = 0
    private val pageSize = 10

    init {
        checkActiveBasket()
    }

    private fun checkActiveBasket() {
        viewModelScope.launch {
            try {
                @Suppress("RedundantSuspendModifier")
                val activeBasket = getActiveBasketUseCase()
                if (activeBasket != null) {
                    _uiState.value = _uiState.value.copy(
                        hasActiveBasket = true,
                        activeBasket = activeBasket
                    )
                } else {
                    // No active basket, load initial basket list
                    loadInitialBaskets()
                }
            } catch (e: Exception) {
                // If error checking active basket, proceed with loading list
                loadInitialBaskets()
            }
        }
    }

    private fun loadInitialBaskets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            currentOffset = 0
            
            when (val result = searchBasketsUseCase(_searchFilter.value, currentOffset, pageSize)) {
                is Result.Success -> {
                    val (baskets, hasMore) = result.data
                    _uiState.value = _uiState.value.copy(
                        baskets = baskets,
                        hasMore = hasMore,
                        isLoading = false,
                        hasActiveBasket = false
                    )
                    currentOffset = pageSize
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.errorResponse.message,
                        hasActiveBasket = false
                    )
                }
                is Result.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    fun searchBaskets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            currentOffset = 0
            
            when (val result = searchBasketsUseCase(_searchFilter.value, currentOffset, pageSize)) {
                is Result.Success -> {
                    val (baskets, hasMore) = result.data
                    _uiState.value = _uiState.value.copy(
                        baskets = baskets,
                        hasMore = hasMore,
                        isLoading = false
                    )
                    currentOffset = pageSize
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.errorResponse.message
                    )
                }
                is Result.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    fun loadMoreBaskets() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMore = true)
            
            when (val result = searchBasketsUseCase(_searchFilter.value, currentOffset, pageSize)) {
                is Result.Success -> {
                    val (newBaskets, hasMore) = result.data
                    val updatedBaskets = _uiState.value.baskets + newBaskets
                    _uiState.value = _uiState.value.copy(
                        baskets = updatedBaskets,
                        hasMore = hasMore,
                        isLoadingMore = false
                    )
                    currentOffset += pageSize
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingMore = false,
                        errorMessage = result.errorResponse.message
                    )
                }
                is Result.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    fun updateSearchFilter(filter: BasketSearchFilter) {
        _searchFilter.value = filter
    }

    fun clearSearchFilter() {
        _searchFilter.value = BasketSearchFilter()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun refreshBaskets() {
        loadInitialBaskets()
    }
}

data class BasketListUiState(
    val baskets: List<Basket> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val errorMessage: String? = null,
    val hasActiveBasket: Boolean = false,
    val activeBasket: com.kanishk.goldscanner.data.model.ActiveBasket? = null
)