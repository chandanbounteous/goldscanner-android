package com.kanishk.goldscanner.domain.usecase.basket

import com.kanishk.goldscanner.domain.repository.CustomerRepository
import com.kanishk.goldscanner.data.model.response.Result
import com.kanishk.goldscanner.data.model.response.ErrorResponse
import android.util.Log

class DeleteBasketArticleUseCase(
    private val customerRepository: CustomerRepository
) {
    suspend operator fun invoke(
        articleId: String
    ): Result<Unit> {
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

            // Call repository to delete the basket article
            return customerRepository.deleteBasketArticle(articleId)

        } catch (e: Exception) {
            Log.e("DeleteBasketArticleUseCase", "Error deleting basket article: ${e.message}", e)
            return Result.Error(
                ErrorResponse(
                    responseMessage = "Failed to delete article from basket",
                    message = e.message ?: "An unexpected error occurred"
                )
            )
        }
    }
}