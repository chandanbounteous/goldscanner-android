package com.kanishk.goldscanner.domain.usecase.basket

import com.kanishk.goldscanner.domain.repository.CustomerRepository
import com.kanishk.goldscanner.data.model.response.Result
import com.kanishk.goldscanner.data.model.response.BasketDetailResponse
import com.kanishk.goldscanner.data.model.response.ErrorResponse
import android.util.Log

class GetBasketDetailsUseCase(
    private val customerRepository: CustomerRepository
) {
    suspend operator fun invoke(basketId: String): Result<BasketDetailResponse> {
        try {
            // Basic validation
            if (basketId.isBlank()) {
                return Result.Error(
                    ErrorResponse(
                        responseMessage = "Basket ID cannot be empty",
                        message = "Basket ID cannot be empty"
                    )
                )
            }
            
            return customerRepository.getBasketDetails(basketId)
            
        } catch (e: Exception) {
            Log.e("GetBasketDetailsUseCase", "Error getting basket details", e)
            return Result.Error(
                ErrorResponse(
                    responseMessage = "Failed to get basket details: ${e.message}",
                    message = "Failed to get basket details: ${e.message}"
                )
            )
        }
    }
}