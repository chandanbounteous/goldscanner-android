package com.kanishk.goldscanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kanishk.goldscanner.domain.usecase.GetCustomerListUseCase
import com.kanishk.goldscanner.data.model.Customer
import com.kanishk.goldscanner.data.model.response.PaginationInfo
import com.goldscanner.data.common.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

data class CustomerListUiState(
    val customers: List<Customer> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)

class CustomerListViewModel(
    private val getCustomerListUseCase: GetCustomerListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerListUiState())
    val uiState: StateFlow<CustomerListUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private val searchDebounceTime = 500L // 500ms debounce

    init {
        loadCustomers()
    }

    fun loadCustomers() {
        viewModelScope.launch {
            val currentState = _uiState.value
            
            _uiState.value = currentState.copy(
                isLoading = true,
                error = null
            )
            
            when (val result = getCustomerListUseCase(currentState.searchQuery)) {
                is Result.Success -> {
                    _uiState.value = currentState.copy(
                        customers = result.data,
                        isLoading = false,
                        error = null
                    )
                }
                is Result.Error -> {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        error = result.errorResponse.message
                    )
                }
                is Result.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        
        // Cancel previous search job
        searchJob?.cancel()
        
        // Start new debounced search
        searchJob = viewModelScope.launch {
            delay(searchDebounceTime)
            loadCustomers()
        }
    }

    fun onCustomerSelected(customer: Customer) {
        // TODO: Handle customer selection for basket creation
        // This will be implemented when basket functionality is added
    }

    fun onAddNewCustomer() {
        // TODO: Navigate to add customer screen
        // This will be implemented when customer creation functionality is added
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun refreshCustomers() {
        loadCustomers()
    }
}