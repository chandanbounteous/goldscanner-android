package com.kanishk.goldscanner.presentation.ui.screen

import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import org.koin.androidx.compose.koinViewModel
import com.kanishk.goldscanner.presentation.viewmodel.BasketListViewModel
import com.kanishk.goldscanner.data.model.Basket

/**
 * Unified basket screen that handles navigation between list and detail views
 */
@Composable
fun BasketScreen(
    basketListViewModel: BasketListViewModel = koinViewModel(),
    onNavigateToArticleListing: () -> Unit = {},
    onNavigateAway: () -> Unit = {}
) {
    val uiState by basketListViewModel.uiState.collectAsState()
    
    // Refresh active basket check when screen becomes visible
    // This ensures we get the latest active basket state after navigation
    LaunchedEffect(Unit) {
        basketListViewModel.refreshActiveBasketCheck()
    }
    
    // Show basket detail if there's an active basket
    if (uiState.hasActiveBasket && uiState.activeBasketId != null) {
        BasketDetailScreen(
            onNavigateToArticleListing = onNavigateToArticleListing,
            onNavigateAway = onNavigateAway
        )
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