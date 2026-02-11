package com.kanishk.goldscanner.domain.usecase.basket

import com.kanishk.goldscanner.domain.repository.CustomerRepository
import com.kanishk.goldscanner.data.model.request.AddArticleToBasketRequest
import com.kanishk.goldscanner.data.model.response.Result
import com.kanishk.goldscanner.data.model.response.AddArticleToBasketResponse
import com.kanishk.goldscanner.data.model.response.ErrorResponse
import android.util.Log

class AddArticleToBasketUseCase(
    private val customerRepository: CustomerRepository
) {
    suspend operator fun invoke(
        basketId: String,
        articleId: String,
        netWeight: Double,
        grossWeight: Double,
        addOnCost: Double,
        wastage: Double,
        makingCharge: Double,
        discount: Double
    ): Result<AddArticleToBasketResponse> {
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
            
            if (articleId.isBlank()) {
                return Result.Error(
                    ErrorResponse(
                        responseMessage = "Article ID cannot be empty", 
                        message = "Article ID cannot be empty"
                    )
                )
            }
            
            if (netWeight <= 0) {
                return Result.Error(
                    ErrorResponse(
                        responseMessage = "Net weight must be greater than 0",
                        message = "Net weight must be greater than 0"
                    )
                )
            }
            
            if (grossWeight <= 0) {
                return Result.Error(
                    ErrorResponse(
                        responseMessage = "Gross weight must be greater than 0",
                        message = "Gross weight must be greater than 0"
                    )
                )
            }
            
            if (netWeight > grossWeight) {
                return Result.Error(
                    ErrorResponse(
                        responseMessage = "Net weight cannot be greater than gross weight",
                        message = "Net weight cannot be greater than gross weight"
                    )
                )
            }
            
            if (wastage < 0) {
                return Result.Error(
                    ErrorResponse(
                        responseMessage = "Wastage cannot be negative",
                        message = "Wastage cannot be negative"
                    )
                )
            }
            
            if (makingCharge < 0) {
                return Result.Error(
                    ErrorResponse(
                        responseMessage = "Making charge cannot be negative",
                        message = "Making charge cannot be negative"
                    )
                )
            }
            
            // Create request and call repository
            val request = AddArticleToBasketRequest(
                articleId = articleId,
                netWeight = netWeight,
                grossWeight = grossWeight,
                addOnCost = addOnCost,
                wastage = wastage,
                makingCharge = makingCharge,
                discount = discount
            )
            
            return customerRepository.addArticleToBasket(basketId, request)
            
        } catch (e: Exception) {
            Log.e("AddArticleToBasketUseCase", "Error adding article to basket", e)
            return Result.Error(
                ErrorResponse(
                    responseMessage = "Failed to add article to basket: ${e.message}",
                    message = "Failed to add article to basket: ${e.message}"
                )
            )
        }
    }
}