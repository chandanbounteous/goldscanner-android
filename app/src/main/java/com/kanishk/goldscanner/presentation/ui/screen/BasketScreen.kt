package com.kanishk.goldscanner.presentation.ui.screen

import androidx.compose.runtime.*
import org.koin.androidx.compose.koinViewModel
import com.kanishk.goldscanner.presentation.viewmodel.BasketListViewModel
import com.kanishk.goldscanner.data.model.Basket

/**
 * Unified basket screen that handles navigation between list and detail views
 */
@Composable
fun BasketScreen(
    basketListViewModel: BasketListViewModel = koinViewModel()
) {
    val uiState by basketListViewModel.uiState.collectAsState()
    
    // Show basket detail if there's an active basket
    if (uiState.hasActiveBasket && uiState.activeBasketId != null) {
        BasketDetailScreen()
    } else {
        BasketListScreen(
            onBasketClick = { basket ->
                // TODO: Handle basket selection if needed
                // For now, showing active basket has priority
            },
            viewModel = basketListViewModel
        )
    }
}