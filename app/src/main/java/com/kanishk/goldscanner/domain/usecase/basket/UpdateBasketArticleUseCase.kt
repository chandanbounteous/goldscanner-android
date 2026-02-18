package com.kanishk.goldscanner.domain.usecase.basket

import com.kanishk.goldscanner.domain.repository.CustomerRepository
import com.kanishk.goldscanner.data.model.request.UpdateBasketArticleRequest
import com.kanishk.goldscanner.data.model.response.Result
import com.kanishk.goldscanner.data.model.response.UpdateBasketArticleResponse
import com.kanishk.goldscanner.data.model.response.ErrorResponse
import android.util.Log

class UpdateBasketArticleUseCase(
    private val customerRepository: CustomerRepository
) {
    suspend operator fun invoke(
        articleId: String,
        netWeight: Double,
        grossWeight: Double,
        addOnCost: Double,
        wastage: Double,
        makingCharge: Double,
        discount: Double
    ): Result<UpdateBasketArticleResponse> {
        try {
            // Basic validation
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
            
            if (makingCharge < 0) {
                return Result.Error(
                    ErrorResponse(
                        responseMessage = "Making charge cannot be negative",
                        message = "Making charge cannot be negative"
                    )
                )
            }
            
            if (addOnCost < 0) {
                return Result.Error(
                    ErrorResponse(
                        responseMessage = "Add-on cost cannot be negative",
                        message = "Add-on cost cannot be negative"
                    )
                )
            }
            
            if (discount < 0) {
                return Result.Error(
                    ErrorResponse(
                        responseMessage = "Discount cannot be negative",
                        message = "Discount cannot be negative"
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

            // Create request object
            val request = UpdateBasketArticleRequest(
                netWeight = netWeight,
                grossWeight = grossWeight,
                addOnCost = addOnCost,
                wastage = wastage,
                makingCharge = makingCharge,
                discount = discount
            )

            // Call repository to update the basket article
            return customerRepository.updateBasketArticle(articleId, request)

        } catch (e: Exception) {
            Log.e("UpdateBasketArticleUseCase", "Error updating basket article: ${e.message}", e)
            return Result.Error(
                ErrorResponse(
                    responseMessage = "Failed to update article in basket",
                    message = e.message ?: "An unexpected error occurred"
                )
            )
        }
    }
}