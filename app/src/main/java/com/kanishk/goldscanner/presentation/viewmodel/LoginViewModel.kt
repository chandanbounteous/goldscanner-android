package com.kanishk.goldscanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.kanishk.goldscanner.domain.usecase.auth.LoginUseCase
import com.kanishk.goldscanner.data.model.response.Result

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()
    
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()
    
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()
    
    private val _isPasswordVisible = MutableStateFlow(false)
    val isPasswordVisible: StateFlow<Boolean> = _isPasswordVisible.asStateFlow()
    
    fun updateUsername(username: String) {
        _username.value = username
    }
    
    fun updatePassword(password: String) {
        _password.value = password
    }
    
    fun togglePasswordVisibility() {
        _isPasswordVisible.value = !_isPasswordVisible.value
    }
    
    fun login() {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            
            when (val result = loginUseCase(_username.value, _password.value)) {
                is Result.Success -> {
                    _loginState.value = LoginState.Success
                }
                is Result.Error -> {
                    _loginState.value = LoginState.Error(result.errorResponse.message)
                }
                is Result.Loading -> {
                    // Already set to loading
                }
            }
        }
    }
    
    fun resetState() {
        _loginState.value = LoginState.Idle
    }
    
    fun clearForm() {
        _username.value = ""
        _password.value = ""
        _isPasswordVisible.value = false
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}