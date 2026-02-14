package com.kanishk.goldscanner.domain.usecase

import com.kanishk.goldscanner.domain.repository.BasketRepository

/**
 * Use case for clearing the active basket ID from local storage
 * Following CLEAN architecture principles
 */
class ClearActiveBasketIdUseCase(
    private val basketRepository: BasketRepository
) {
    suspend operator fun invoke() {
        basketRepository.clearActiveBasketId()
    }
}