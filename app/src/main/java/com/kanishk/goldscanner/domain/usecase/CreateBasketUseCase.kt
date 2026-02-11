package com.kanishk.goldscanner.domain.usecase

import com.kanishk.goldscanner.domain.repository.CustomerRepository
import com.kanishk.goldscanner.domain.repository.GoldRateRepository
import com.kanishk.goldscanner.domain.repository.BasketRepository
import com.kanishk.goldscanner.data.model.CreateBasketRequest
import com.kanishk.goldscanner.data.model.CreatedBasket
import com.goldscanner.data.common.Result
import android.util.Log

class CreateBasketUseCase(
    private val customerRepository: CustomerRepository,
    private val goldRateRepository: GoldRateRepository,
    private val basketRepository: BasketRepository
) {
    suspend operator fun invoke(
        customerId: String,
        isGoldRateFixed: Boolean
    ): Result<CreatedBasket> {
        try {
            val request = if (isGoldRateFixed) {
                // Get current gold rate for 24K
                when (val goldRateResult = goldRateRepository.getCurrentGoldRate()) {
                    is Result.Success -> {
                        val goldRate24K = goldRateResult.data.rates["24"] ?: 0.0
                        val nepaliDate = goldRateResult.data.date
                        
                        CreateBasketRequest(
                            isGoldRateFixed = true,
                            fixedGoldRate24KPerTola = goldRate24K,
                            fixedGoldRateNepaliDate = nepaliDate
                        )
                    }
                    is Result.Error -> {
                        return Result.Error(goldRateResult.errorResponse)
                    }
                    is Result.Loading -> {
                        return Result.Error(
                            com.goldscanner.data.common.ErrorResponse(
                                400, 
                                "Unable to fetch current gold rate", 
                                null
                            )
                        )
                    }
                }
            } else {
                CreateBasketRequest(
                    isGoldRateFixed = false
                )
            }
            
            val result = customerRepository.createBasket(customerId, request)
            
            // Store the created basket as active basket using repository
            if (result is Result.Success) {
                basketRepository.setActiveBasketId(result.data.id)
                Log.d("CreateBasketUseCase", "Basket created and stored as active: ${result.data.id}")
            }
            
            return result
            
        } catch (e: Exception) {
            Log.e("CreateBasketUseCase", "Error creating basket", e)
            return Result.Error(
                com.goldscanner.data.common.ErrorResponse(
                    500, 
                    "Failed to create basket: ${e.message}", 
                    null
                )
            )
        }
    }
}