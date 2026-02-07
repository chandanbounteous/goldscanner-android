package com.kanishk.goldscanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kanishk.goldscanner.domain.usecase.GetCustomerListUseCase
import com.kanishk.goldscanner.domain.usecase.CreateCustomerUseCase
import com.kanishk.goldscanner.data.model.Customer
import com.kanishk.goldscanner.data.model.response.PaginationInfo
import com.goldscanner.data.common.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import android.util.Patterns

data class CustomerFormState(
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val email: String = "",
    val firstNameError: String? = null,
    val emailError: String? = null,
    val isSubmitting: Boolean = false
)

data class CustomerListUiState(
    val customers: List<Customer> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val showAddCustomerModal: Boolean = false,
    val customerForm: CustomerFormState = CustomerFormState(),
    val showSuccessMessage: String? = null
)

class CustomerListViewModel(
    private val getCustomerListUseCase: GetCustomerListUseCase,
    private val createCustomerUseCase: CreateCustomerUseCase
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
        _uiState.value = _uiState.value.copy(
            showAddCustomerModal = true,
            customerForm = CustomerFormState()
        )
    }
    
    fun onCloseAddCustomerModal() {
        _uiState.value = _uiState.value.copy(
            showAddCustomerModal = false,
            customerForm = CustomerFormState()
        )
    }
    
    fun onFirstNameChanged(firstName: String) {
        val currentForm = _uiState.value.customerForm
        _uiState.value = _uiState.value.copy(
            customerForm = currentForm.copy(
                firstName = firstName,
                firstNameError = if (firstName.trim().isEmpty()) "First name is required" else null
            )
        )
    }
    
    fun onLastNameChanged(lastName: String) {
        val currentForm = _uiState.value.customerForm
        _uiState.value = _uiState.value.copy(
            customerForm = currentForm.copy(lastName = lastName)
        )
    }
    
    fun onPhoneChanged(phone: String) {
        val currentForm = _uiState.value.customerForm
        _uiState.value = _uiState.value.copy(
            customerForm = currentForm.copy(phone = phone)
        )
    }
    
    fun onEmailChanged(email: String) {
        val currentForm = _uiState.value.customerForm
        val emailError = if (email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            "Please enter a valid email address"
        } else null
        
        _uiState.value = _uiState.value.copy(
            customerForm = currentForm.copy(
                email = email,
                emailError = emailError
            )
        )
    }
    
    fun onCreateCustomer() {
        val currentForm = _uiState.value.customerForm
        
        // Validate form
        val firstNameError = if (currentForm.firstName.trim().isEmpty()) "First name is required" else null
        val emailError = if (currentForm.email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(currentForm.email).matches()) {
            "Please enter a valid email address"
        } else null
        
        // Update form with validation errors
        _uiState.value = _uiState.value.copy(
            customerForm = currentForm.copy(
                firstNameError = firstNameError,
                emailError = emailError
            )
        )
        
        // Return if validation fails
        if (firstNameError != null || emailError != null) {
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                customerForm = currentForm.copy(isSubmitting = true)
            )
            
            when (val result = createCustomerUseCase(
                firstName = currentForm.firstName.trim(),
                lastName = if (currentForm.lastName.trim().isEmpty()) null else currentForm.lastName.trim(),
                phone = if (currentForm.phone.trim().isEmpty()) null else currentForm.phone.trim(),
                email = if (currentForm.email.trim().isEmpty()) null else currentForm.email.trim()
            )) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        showAddCustomerModal = false,
                        customerForm = CustomerFormState(),
                        showSuccessMessage = "Customer created successfully"
                    )
                    // Reload customer list
                    loadCustomers()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        customerForm = currentForm.copy(isSubmitting = false),
                        error = result.errorResponse.message
                    )
                }
                is Result.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(showSuccessMessage = null)
    }

    fun refreshCustomers() {
        loadCustomers()
    }
}