package com.kanishk.goldscanner.domain.usecase.basket

import com.kanishk.goldscanner.domain.repository.BasketRepository

/**
 * Use case for setting the active basket ID in local storage
 * Following CLEAN architecture principles
 */
class SetActiveBasketIdUseCase(
    private val basketRepository: BasketRepository
) {
    suspend operator fun invoke(basketId: String) {
        basketRepository.setActiveBasketId(basketId)
    }
}