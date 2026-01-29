package com.kanishk.goldscanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.kanishk.goldscanner.domain.usecase.auth.CheckLoginStatusUseCase

class SplashViewModel(
    private val checkLoginStatusUseCase: CheckLoginStatusUseCase
) : ViewModel() {
    
    private val _splashState = MutableStateFlow<SplashState>(SplashState.Loading)
    val splashState: StateFlow<SplashState> = _splashState.asStateFlow()
    
    init {
        checkAuthStatus()
    }
    
    private fun checkAuthStatus() {
        viewModelScope.launch {
            // Show splash for minimum duration
            delay(2000) // 2 seconds minimum splash duration
            
            val isLoggedIn = checkLoginStatusUseCase()
            _splashState.value = if (isLoggedIn) {
                SplashState.NavigateToMain
            } else {
                SplashState.NavigateToLogin
            }
        }
    }
}

sealed class SplashState {
    object Loading : SplashState()
    object NavigateToLogin : SplashState()
    object NavigateToMain : SplashState()
}