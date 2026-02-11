package com.kanishk.goldscanner.domain.usecase.basket

import com.kanishk.goldscanner.domain.repository.BasketRepository

class GetActiveBasketIdUseCase(
    private val basketRepository: BasketRepository
) {
    suspend operator fun invoke(): String? {
        return basketRepository.getActiveBasketId()
    }
}